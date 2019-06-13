package com.dizzion.portal.domain.helpdesk;

import com.dizzion.portal.domain.exception.EntityNotFoundException;
import com.dizzion.portal.domain.filter.FieldFilter;
import com.dizzion.portal.domain.helpdesk.dto.ConnectWiseCompany;
import com.dizzion.portal.domain.helpdesk.dto.ConnectWiseContact;
import com.dizzion.portal.domain.helpdesk.dto.ConnectWiseMember;
import com.dizzion.portal.domain.helpdesk.dto.Contact;
import com.dizzion.portal.domain.organization.OrganizationService;
import com.dizzion.portal.domain.user.UserService;
import com.dizzion.portal.domain.user.dto.ShortUserInfo;
import com.dizzion.portal.domain.user.dto.User;
import com.dizzion.portal.domain.user.persistence.UserRepository;
import com.google.common.collect.ImmutableSet;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.dizzion.portal.domain.filter.FieldFilter.LogicOperator.AND;
import static com.dizzion.portal.domain.filter.FieldFilter.Operator.EQUALS;
import static com.dizzion.portal.domain.filter.FieldFilter.Operator.IN;
import static com.dizzion.portal.domain.helpdesk.ConnectWiseQueryFactory.queryFromFilters;
import static com.dizzion.portal.domain.helpdesk.ConnectWiseTicketingService.MAX_PAGE_SIZE;
import static com.dizzion.portal.domain.helpdesk.ConnectWiseUtils.urlWithConditions;
import static com.google.common.collect.Iterables.getOnlyElement;
import static java.lang.String.join;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

@Service
public class ConnectWiseCompanyService {
    private final String companiesUrl;
    private final String contactsUrl;
    private final String contactByIdUrl;
    private final String membersUrl;

    private final UserService userService;
    private final UserRepository userRepo;
    private final OrganizationService organizationService;
    private final RestTemplate restTemplate;

    public ConnectWiseCompanyService(@Value("${connectwise.service.base-url}") String baseUrl,
                                     @Qualifier("connectWiseRestTemplate") RestTemplate restTemplate,
                                     UserService userService,
                                     UserRepository userRepo,
                                     OrganizationService organizationService) {
        this.userService = userService;
        this.userRepo = userRepo;
        this.organizationService = organizationService;
        this.restTemplate = restTemplate;

        this.companiesUrl = baseUrl + "/company/companies";
        this.contactsUrl = baseUrl + "/company/contacts";
        this.contactByIdUrl = baseUrl + "/company/contacts/{id}";
        this.membersUrl = baseUrl + "system/members";
    }

    public Set<ConnectWiseCompany> getCompanies(String... cids) {
        URI url = urlWithConditions(companiesUrl, new FieldFilter("identifier", AND, IN, join(",", cids)));
        return ImmutableSet.copyOf(restTemplate.getForObject(url, ConnectWiseCompany[].class));
    }

    public Optional<ConnectWiseCompany> getCompany(String cid) {
        return Optional.ofNullable(getOnlyElement(getCompanies(cid), null));
    }

    public Optional<ConnectWiseContact> getContact(User user) {
        return getContact(user.getEmail(), getConnectWiseCid(user));
    }

    public Contact getContactOrDefault(long userId) {
        User user = userService.getUser(userId);
        return getContact(user)
                .map(Contact::from)
                .orElseGet(() -> getDefaultContact(getConnectWiseCid(user))
                        .map(Contact::from)
                        .orElseThrow(EntityNotFoundException::new));
    }

    public Contact createContact(long userId) {
        User user = userService.getUser(userId);
        ConnectWiseContact contact = ConnectWiseContact.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .companyCid(getConnectWiseCid(user))
                .email(user.getEmail())
                .phone(user.getWorkPhoneNumber().orElse(user.getMobilePhoneNumber().orElse(null)))
                .build();
        return Contact.from(restTemplate.postForObject(contactsUrl, contact, ConnectWiseContact.class));
    }

    public Set<ConnectWiseMember> getMembers(Set<FieldFilter> filters) {
        URI url = urlWithConditions(membersUrl, filters, new PageRequest(0, MAX_PAGE_SIZE));

        Map<String, ConnectWiseMember> membersByEmail = stream(restTemplate.getForObject(url, ConnectWiseMember[].class))
                .filter(member -> member.getOfficeEmail() != null)
                .collect(toMap(
                        member -> member.getOfficeEmail().toLowerCase(),
                        member -> member,
                        (email1, email2) -> email1));

        return userRepo.findByEmailIn(membersByEmail.keySet()).stream()
                .filter(user -> membersByEmail.get(user.getEmail().toLowerCase()) != null)
                .map(user -> membersByEmail.get(user.getEmail().toLowerCase()).toBuilder()
                        .portalUser(ShortUserInfo.from(user))
                        .build())
                .collect(toSet());
    }

    public String getConnectWiseCid(User user) {
        if (user.isPortalAdmin()) {
            return organizationService.getDizzionOrganization().getCustomerId();
        }
        return user.getOrganization().getCustomerId();
    }

    private Optional<ConnectWiseContact> getContact(String email, String customerId) {
        FieldFilter emailFilter = new FieldFilter("communicationItems/value", AND, EQUALS, email);
        FieldFilter cidFilter = new FieldFilter("company/identifier", AND, EQUALS, customerId);
        URI url = fromUriString(contactsUrl)
                .queryParam("conditions", queryFromFilters(cidFilter))
                .queryParam("childConditions", queryFromFilters(emailFilter))
                .build().toUri();

        ConnectWiseContact[] contacts = restTemplate.getForObject(url, ConnectWiseContact[].class);
        return contacts.length == 0
                ? Optional.empty()
                : Optional.of(contacts[0]);
    }

    private Optional<ConnectWiseContact> getDefaultContact(String cid) {
        return getCompany(cid)
                .flatMap(ConnectWiseCompany::getDefaultContact)
                .map(contactRef -> restTemplate.getForObject(contactByIdUrl, ConnectWiseContact.class, contactRef.getId()));
    }
}

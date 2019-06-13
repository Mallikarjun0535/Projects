package com.dizzion.portal.domain.user;

import com.dizzion.portal.domain.common.AbstractCrudService;
import com.dizzion.portal.domain.common.ScopedEntityService;
import com.dizzion.portal.domain.common.persistence.CriteriaPathFactory;
import com.dizzion.portal.domain.common.persistence.RepositoryRegistry;
import com.dizzion.portal.domain.exception.EntityNotFoundException;
import com.dizzion.portal.domain.exception.MissingPhoneNumber;
import com.dizzion.portal.domain.filter.FieldFilter;
import com.dizzion.portal.domain.filter.persistence.FilterSpecification;
import com.dizzion.portal.domain.filter.persistence.FiltersMapper;
import com.dizzion.portal.domain.organization.persistence.OrganizationRepository;
import com.dizzion.portal.domain.organization.persistence.entity.OrganizationEntity;
import com.dizzion.portal.domain.role.persistence.RoleRepository;
import com.dizzion.portal.domain.role.persistence.entity.RoleEntity;
import com.dizzion.portal.domain.user.dto.BatchUserCreateCsvRecord;
import com.dizzion.portal.domain.user.dto.User;
import com.dizzion.portal.domain.user.dto.UserCreateUpdateRequest;
import com.dizzion.portal.domain.user.persistence.UserRepository;
import com.dizzion.portal.domain.user.persistence.entity.UserEntity;
import com.dizzion.portal.domain.user.registration.PasswordSetupLinkService;
import com.dizzion.portal.domain.user.registration.dto.PhoneNumberWithCredentials;
import com.dizzion.portal.domain.user.registration.dto.RegistrationRequest;
import com.dizzion.portal.domain.user.registration.dto.ResetPasswordRequest;
import com.dizzion.portal.domain.user.registration.dto.SetupPasswordRequest;
import com.dizzion.portal.domain.user.registration.persistence.RegistrationLinkRepository;
import com.dizzion.portal.infra.messaging.EmailQueueMessage;
import com.dizzion.portal.infra.messaging.MessageQueues;
import com.dizzion.portal.infra.messaging.SmsQueueMessage;
import com.dizzion.portal.security.auth.AuthenticatedUserAccessor;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static com.dizzion.portal.domain.common.GeneratorUtils.generateInt;
import static com.dizzion.portal.domain.scope.TenantPathUtils.tenantScope;
import static com.dizzion.portal.domain.user.dto.User.NotificationMethod.EMAIL;
import static com.dizzion.portal.infra.template.TemplateService.EmailTemplate.*;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Arrays.stream;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.*;
import static org.apache.commons.lang3.StringUtils.leftPad;

@Service
@Transactional
public class UserService extends AbstractCrudService<UserEntity> {
    private static final int PADDED_PIN_LENGTH = 6;

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticatedUserAccessor auth;
    private final RegistrationLinkRepository registrationLinkRepository;
    private final ObjectReader csvUserReader;
    private final OrganizationRepository organizationRepo;
    private final RoleRepository roleRepo;
    private final MessageQueues messageQueues;
    private final PasswordSetupLinkService passwordSetupLinkService;
    private final FiltersMapper<UserEntity> filtersMapper;
    private final CriteriaPathFactory pathFactory;
    private final Class<UserEntity> userEntity;
    private final RepositoryRegistry repoRegistry;

    public UserService(UserRepository userRepo,
                       PasswordEncoder passwordEncoder,
                       AuthenticatedUserAccessor auth,
                       ScopedEntityService scopedEntityService,
                       RegistrationLinkRepository registrationLinkRepository,
                       @Qualifier("csvUserReader") ObjectReader csvUserReader,
                       OrganizationRepository organizationRepo,
                       RoleRepository roleRepo,
                       MessageQueues messageQueues,
                       PasswordSetupLinkService passwordSetupLinkService,
                       FiltersMapper<UserEntity> filtersMapper,
                       CriteriaPathFactory pathFactory,
                       RepositoryRegistry repoRegistry) {
        super(scopedEntityService);
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.auth = auth;
        this.registrationLinkRepository = registrationLinkRepository;
        this.csvUserReader = csvUserReader;
        this.organizationRepo = organizationRepo;
        this.roleRepo = roleRepo;
        this.messageQueues = messageQueues;
        this.passwordSetupLinkService = passwordSetupLinkService;

        this.filtersMapper = filtersMapper;
        this.pathFactory = pathFactory;
        this.userEntity = getEntityClass();
        this.repoRegistry = repoRegistry;
    }

    @Transactional(readOnly = true)
    public User getUser(long id) {
        User authUser = auth.getAuthenticatedUser().getUser();
        if (id == authUser.getId()) {
            return authUser;
        }
        return User.from(getForRead(id));
    }

    @Transactional(readOnly = true)
    public Optional<User> getByEmailAndPassword(String email, String password) {
        return userRepo.findByEmail(email)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .map(User::from);
    }

    @Transactional(readOnly = true)
    public Optional<User> getByEmail(String email) {
        return Optional.ofNullable(userRepo.findByEmailAndOrganizationTenantPathLike(email, tenantScope(auth.getTenantPath())))
                .map(User::from);
    }

    @Transactional(readOnly = true)
    public Page<User> getPage(Pageable pageRequest, Set<FieldFilter> filters) {
        return getEntitiesPage(pageRequest, filters).map(User::from);
    }

    @Transactional(readOnly = true)
    public Page<User> getAdminPortalPage(Pageable pageRequest, Set<FieldFilter> filters) {
        Specification<UserEntity> spec = Specifications.where(new FilterSpecification<UserEntity>(filters, userEntity, filtersMapper, pathFactory));
        return (repoRegistry.repositoryFor(userEntity).findAll(spec, pageRequest)).map(User::from);
    }

    public User register(RegistrationRequest registrationRequest) {
        UserEntity user = userRepo.findByRegistrationLink_linkSecretPath(registrationRequest.getSecret())
                .orElseThrow(IllegalArgumentException::new);

        user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        user.setPin(registrationRequest.getPin());
        user.setMobilePhoneNumber(registrationRequest.getPhoneNumber());

        registrationLinkRepository.delete(user.getRegistrationLink());
        return User.from(user);
    }

    public User setupPassword(SetupPasswordRequest setupPasswordRequest) {
        UserEntity user = userRepo.findByRegistrationLink_linkSecretPath(setupPasswordRequest.getSecret())
                .orElseThrow(IllegalArgumentException::new);

        user.setPassword(passwordEncoder.encode(setupPasswordRequest.getPassword()));
        registrationLinkRepository.delete(user.getRegistrationLink());
        return User.from(user);
    }

    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        UserEntity user = userRepo.findByEmail(resetPasswordRequest.getEmail())
                .orElseThrow(IllegalArgumentException::new);

        if (user.getPassword() == null) {
            sendRegistrationEmails(user);
        } else {
            messageQueues.enqueueEmail(new EmailQueueMessage(RESET_PASSWORD,
                    ImmutableMap.of(
                            "firstName", user.getFirstName(),
                            "lastName", user.getLastName(),
                            "link", passwordSetupLinkService.generatePasswordResetLink(user.getId())),
                    singleton(user.getEmail())));
        }
    }

    public User create(UserCreateUpdateRequest user) {
        OrganizationEntity organization =
                scopedEntityService.getForWrite(user.getOrganizationId(), OrganizationEntity.class);
        RoleEntity role = roleRepo.findByName(user.getRole());
        throwExceptionIfRoleUnavailable(role, organization);

        UserEntity userEntity = save(UserEntity.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .mobilePhoneNumber(user.getMobilePhoneNumber().orElse(null))
                .workPhoneNumber(user.getWorkPhoneNumber().orElse(null))
                .email(user.getEmail())
                .notificationMethods(singleton(EMAIL))
                .role(role)
                .organization(organization)
                .pin(generateInt())
                .build());

        sendRegistrationEmails(userEntity);
        return User.from(userEntity);
    }

    public Set<User> createBatch(MultipartFile file) {
        List<BatchUserCreateCsvRecord> records = readUsers(file);

        List<String> orgNames = records.stream()
                .map(BatchUserCreateCsvRecord::getOrganization)
                .distinct()
                .collect(toList());

        Map<String, OrganizationEntity> orgEntities = organizationRepo
                .findByNameInAndTenantPathLike(orgNames, tenantScope(auth.getTenantPath()))
                .stream()
                .collect(toMap(entity -> entity.getName().toLowerCase(), Function.identity()));

        if (orgEntities.size() != orgNames.size()) {
            throw new EntityNotFoundException("Can't access requested organizations. Requested=" + orgNames
                    + ". User has access to=" + orgEntities.keySet());
        }

        records.forEach(record -> {
                    OrganizationEntity organizationEntity = orgEntities.get(record.getOrganization().toLowerCase());
                    throwExceptionIfRoleUnavailable(roleRepo.findByName(record.getRole()), organizationEntity);
                }
        );

        List<UserEntity> users = records.stream()
                .map(record -> {
                    OrganizationEntity organization = orgEntities.get(record.getOrganization().toLowerCase());
                    return UserEntity.builder()
                            .firstName(record.getFirstName())
                            .lastName(record.getLastName())
                            .email(record.getEmail())
                            .notificationMethods(singleton(EMAIL))
                            .organization(organization)
                            .role(roleRepo.findByName(record.getRole()))
                            .mobilePhoneNumber(record.getMobilePhone())
                            .workPhoneNumber(record.getWorkPhone())
                            .pin(generateInt())
                            .build();
                })
                .collect(toList());
        Set<UserEntity> savedUsers = scopedEntityService.save(users, UserEntity.class);

        sendRegistrationEmails(savedUsers.stream().toArray(UserEntity[]::new));
        return savedUsers.stream()
                .map(User::from)
                .collect(toSet());
    }

    private void sendRegistrationEmails(UserEntity... userEntities) {
        EmailQueueMessage[] regMessages = stream(userEntities)
                .map(user -> new EmailQueueMessage(REGISTRATION,
                        ImmutableMap.of("firstName", user.getFirstName(),
                                "lastName", user.getLastName(),
                                "link", passwordSetupLinkService.generateRegistrationLink(user.getId())),
                        singleton(user.getEmail())))
                .toArray(EmailQueueMessage[]::new);
        messageQueues.enqueueEmail(regMessages);
    }

    private List<BatchUserCreateCsvRecord> readUsers(MultipartFile file) {
        try {
            MappingIterator<BatchUserCreateCsvRecord> fileContent = csvUserReader.readValues(file.getInputStream());
            return fileContent.readAll();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void throwExceptionIfRoleUnavailable(RoleEntity role, OrganizationEntity organization) {
        String authRole = auth.getAuthenticatedUser().getUser().getRole().getName();
        Set<RoleEntity> subordinateRoles = roleRepo.findSubordinateRoles(authRole);
        if (!subordinateRoles.contains(role) || !role.getOrganizationTypes().contains(organization.getType())) {
            throw new IllegalArgumentException();
        }
    }

    public User update(long id, UserCreateUpdateRequest user) {
        checkNotSelf(id);

        UserEntity existing = getForWrite(id);
        OrganizationEntity organization =
                scopedEntityService.getForWrite(user.getOrganizationId(), OrganizationEntity.class);
        RoleEntity role = roleRepo.findByName(user.getRole());

        throwExceptionIfRoleUnavailable(role, organization);
        if (organization.isTwoFactorAuth() && !user.getMobilePhoneNumber().isPresent()) {
            throw new MissingPhoneNumber();
        }

        existing.setEmail(user.getEmail());
        existing.setFirstName(user.getFirstName());
        existing.setLastName(user.getLastName());
        user.getMobilePhoneNumber().ifPresent(existing::setMobilePhoneNumber);
        user.getWorkPhoneNumber().ifPresent(existing::setWorkPhoneNumber);
        existing.setRole(role);

        if (!existing.getOrganization().equals(organization)) {
            existing.setStarredApplications(emptySet());
        }

        existing.setOrganization(organization);

        return User.from(save(existing));
    }

    @Transactional(readOnly = true)
    public boolean isEmailTaken(String email) {
        return userRepo.findByEmail(email).isPresent();
    }

    @Override
    @Transactional
    public void delete(long id) {
        checkNotSelf(id);
        super.delete(id);
    }

    @Transactional(readOnly = true)
    public void sendPin(long userId) {
        UserEntity user = getForRead(userId);
        messageQueues.enqueueEmail(new EmailQueueMessage(PIN,
                ImmutableMap.of(
                        "firstName", user.getFirstName(),
                        "lastName", user.getLastName(),
                        "pin", padPin(user.getPin())),
                singleton(user.getEmail())));
        user.getMobilePhoneNumber().ifPresent(phone ->
                messageQueues.enqueueSms(new SmsQueueMessage(singleton(phone), "Your PIN is " + user.getPin())));
    }

    public User setupMobileNumberIfNotExists(PhoneNumberWithCredentials request) {
        User user = getByEmailAndPassword(request.getEmail(), request.getPassword())
                .orElseThrow(() -> new BadCredentialsException(""));
        checkState(!user.getMobilePhoneNumber().isPresent(), "Mobile number is already set");

        UserEntity userEntity = userRepo.findOne(user.getId());
        userEntity.setMobilePhoneNumber(request.getPhoneNumber());
        return User.from(userEntity);
    }

    private void checkNotSelf(long id) {
        if (id == auth.getAuthenticatedUser().getUser().getId()) {
            throw new EntityNotFoundException();
        }
    }

    private String padPin(int pin) {
        return leftPad(String.valueOf(pin), PADDED_PIN_LENGTH, "0");
    }
}

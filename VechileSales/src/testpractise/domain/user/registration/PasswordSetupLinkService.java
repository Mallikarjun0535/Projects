package com.dizzion.portal.domain.user.registration;

import com.dizzion.portal.domain.user.registration.persistence.RegistrationLinkRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

@Service
@Transactional
public class PasswordSetupLinkService {

    private final String frontendUrl;
    private final RegistrationLinkRepository registrationLinkRepository;

    public PasswordSetupLinkService(@Value("${dizzion.frontend.url}") String frontendUrl,
                                    RegistrationLinkRepository registrationLinkRepository) {
        this.registrationLinkRepository = registrationLinkRepository;
        this.frontendUrl = frontendUrl;
    }

    public String generateRegistrationLink(long userId) {
        return fromHttpUrl(frontendUrl).pathSegment("confirmation", generateSecret(userId)).build().toUriString();
    }

    public String generatePasswordResetLink(long userId) {
        return fromHttpUrl(frontendUrl)
                .pathSegment("confirmation", generateSecret(userId))
                .queryParam("reset-password", true)
                .build().toUriString();
    }

    private String generateSecret(long userId) {
        String linkUniquePart = UUID.randomUUID().toString();
        registrationLinkRepository.insertOrUpdate(userId, linkUniquePart);
        return linkUniquePart;
    }
}

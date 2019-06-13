package com.dizzion.portal.security.auth.token;

import com.dizzion.portal.domain.exception.MissingPhoneNumber;
import com.dizzion.portal.domain.exception.TwoFactorAuthException;
import com.dizzion.portal.domain.user.UserService;
import com.dizzion.portal.domain.user.dto.User;
import com.dizzion.portal.infra.sms.TwilioClient;
import com.dizzion.portal.security.auth.persistence.TwoFactorTokenRepository;
import com.dizzion.portal.security.auth.persistence.entity.TwoFactorTokenEntity;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.dizzion.portal.domain.common.GeneratorUtils.generateInt;
import static com.dizzion.portal.domain.role.dto.Role.ISTONISH;
import static com.dizzion.portal.security.auth.token.DuoResponse.Result.ALLOW;
import static com.dizzion.portal.security.auth.token.TwoFactorAuthType.*;
import static java.util.concurrent.CompletableFuture.*;

@Service
@Transactional
public class TwoFactorAuthService {

    private final UserService userService;
    private final TwoFactorTokenRepository twoFactorTokenRepository;
    private final RestTemplate duoRestTemplate;
    private final String duoHost;
    private final boolean twoFactorDuoEnabled;
    private final TwilioClient twilioClient;

    private final static String SMS_TEXT = "Dizzion Portal authentication code: ";
    private static final int DUO_USERNAME_CHARS_LIMIT = 20;

    public TwoFactorAuthService(UserService userService,
                                TwoFactorTokenRepository twoFactorTokenRepository,
                                @Qualifier("duoRestTemplate") RestTemplate duoRestTemplate,
                                @Value("${duo.hostname}") String duoHost,
                                @Value("${two-factor.duo.enabled}") boolean twoFactorDuoEnabled,
                                TwilioClient twilioClient) {
        this.userService = userService;
        this.twoFactorTokenRepository = twoFactorTokenRepository;
        this.duoRestTemplate = duoRestTemplate;
        this.duoHost = "https://" + duoHost;
        this.twoFactorDuoEnabled = twoFactorDuoEnabled;
        this.twilioClient = twilioClient;
    }

    @Transactional(readOnly = true)
    public TwoFactorAuthType getTwoFactorAuthType(EmailAndPassword credentials) {
        User user = userService.getByEmailAndPassword(credentials.getEmail(), credentials.getPassword())
                .orElseThrow(() -> new BadCredentialsException(""));
        if (adminWithEnabledTwoFactorAuth(user)) {
            return DUO;
        } else if (userWithEnabledTwoFactorAuth(user)) {
            return SMS;
        } else {
            return NONE;
        }
    }

    public CompletableFuture<Void> secondFactorAuthenticate(User user, Optional<Integer> twoFactorAuthToken) {
        if (adminWithEnabledTwoFactorAuth(user)) {
            return authenticateAdmin(user);
        }

        if (userWithEnabledTwoFactorAuth(user)) {
            Integer token = twoFactorAuthToken.orElseThrow(() -> new IllegalArgumentException("2 factor auth token required"));
            return validateSmsToken(user, token);
        }

        return completedFuture(null);
    }

    public void requestToken(EmailAndPassword credentials) {
        User user = userService.getByEmailAndPassword(credentials.getEmail(), credentials.getPassword())
                .orElseThrow(() -> new BadCredentialsException(""));
        if (!userWithEnabledTwoFactorAuth(user)) {
            throw new IllegalArgumentException("The requested user's org doesn't have 2f auth enabled");
        }

        if (!user.getMobilePhoneNumber().isPresent()) {
            throw new MissingPhoneNumber();
        }

        int token = generateInt();
        twoFactorTokenRepository.insertOrUpdate(user.getId(), token);
        twilioClient.sendSMS(ImmutableSet.of(user.getMobilePhoneNumber().get()), SMS_TEXT + String.valueOf(token));
    }

    private CompletableFuture<Void> authenticateAdmin(User user) {
        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("username", getDuoUsername(user));
        request.add("device", "auto");
        request.add("factor", "auto");

        return supplyAsync(() -> duoRestTemplate.postForObject(duoHost + "/auth/v2/auth", request, DuoResponse.class))
                .thenAccept(response -> {
                    if (response.getResponse().getResult() != ALLOW) {
                        throw new TwoFactorAuthException();
                    }
                });
    }

    private String getDuoUsername(User user) {
        String username = StringUtils.substringBefore(user.getEmail(), "@");
        if (username.length() > DUO_USERNAME_CHARS_LIMIT) {
            return username.substring(0, DUO_USERNAME_CHARS_LIMIT);
        }
        return username;
    }

    private CompletableFuture<Void> validateSmsToken(User user, int twoFactorAuthToken) {
        return runAsync(() -> {
            TwoFactorTokenEntity token = twoFactorTokenRepository.findByUserId(user.getId()).orElseThrow(
                    () -> new IllegalArgumentException("Token not found for the requested user=" + user.getId()));
            if (token.getToken() == twoFactorAuthToken) {
                twoFactorTokenRepository.delete(token);
            } else {
                throw new TwoFactorAuthException();
            }
        });
    }


    private boolean adminWithEnabledTwoFactorAuth(User user) {
        return twoFactorDuoEnabled
                && user.isPortalAdmin()
                && !user.getRole().getName().equals(ISTONISH)
                && !user.getEmail().equals("admin@dizzion.com");
    }

    private boolean userWithEnabledTwoFactorAuth(User user) {
        return user.getOrganization().isTwoFactorAuth()
                || user.getRole().getName().equals(ISTONISH);
    }
}

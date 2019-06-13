package com.dizzion.portal.domain.user.registration.web;

import com.dizzion.portal.domain.user.UserService;
import com.dizzion.portal.domain.user.dto.User;
import com.dizzion.portal.domain.user.registration.dto.PhoneNumberWithCredentials;
import com.dizzion.portal.domain.user.registration.dto.RegistrationRequest;
import com.dizzion.portal.domain.user.registration.dto.ResetPasswordRequest;
import com.dizzion.portal.domain.user.registration.dto.SetupPasswordRequest;
import com.dizzion.portal.security.auth.JwtService;
import com.dizzion.portal.security.auth.Token;
import com.dizzion.portal.security.auth.token.TokenResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class RegistrationController {

    private final UserService userService;
    private final JwtService jwtService;

    public RegistrationController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/registration")
    public TokenResponse register(@RequestBody @Valid RegistrationRequest registrationRequest) {
        User user = userService.register(registrationRequest);
        return new TokenResponse(jwtService.generate(Token.from(user)));
    }

    @PostMapping("/registration/setup-password")
    public TokenResponse setupPassword(@RequestBody @Valid SetupPasswordRequest setupPasswordRequest) {
        User user = userService.setupPassword(setupPasswordRequest);
        return new TokenResponse(jwtService.generate(Token.from(user)));
    }

    @PostMapping("/registration/reset-password")
    public void resetPassword(@RequestBody @Valid ResetPasswordRequest resetPasswordRequest) {
        userService.resetPassword(resetPasswordRequest);
    }

    @PostMapping("/registration/setup-mobile-number")
    public User setupMobileNumberIfNotExists(@Valid @RequestBody PhoneNumberWithCredentials request) {
        return userService.setupMobileNumberIfNotExists(request);
    }
}

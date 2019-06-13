package com.dizzion.portal.security.auth.web;

import com.dizzion.portal.security.auth.token.EmailAndPassword;
import com.dizzion.portal.security.auth.token.TwoFactorAuthService;
import com.dizzion.portal.security.auth.token.TwoFactorAuthType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class TwoFactorAuthController {

    private final TwoFactorAuthService twoFactorAuthService;

    public TwoFactorAuthController(TwoFactorAuthService twoFactorAuthService) {
        this.twoFactorAuthService = twoFactorAuthService;
    }

    @PostMapping("/two-factor-auth-type")
    public TwoFactorAuthType checkTwoFactorAuthSupport(@RequestBody @Valid EmailAndPassword emailAndPassword) {
        return twoFactorAuthService.getTwoFactorAuthType(emailAndPassword);
    }

    @PostMapping("/two-factor-auth-token")
    public void requestToken(@RequestBody @Valid EmailAndPassword emailAndPassword) {
        twoFactorAuthService.requestToken(emailAndPassword);
    }
}

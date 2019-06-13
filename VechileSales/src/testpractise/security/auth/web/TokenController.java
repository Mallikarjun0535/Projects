package com.dizzion.portal.security.auth.web;

import com.dizzion.portal.security.auth.token.AuthCredentials;
import com.dizzion.portal.security.auth.token.ScopeChangeRequest;
import com.dizzion.portal.security.auth.token.TokenResponse;
import com.dizzion.portal.security.auth.token.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.concurrent.Future;

import static com.dizzion.portal.domain.role.Permission.Constants.CHANGE_SCOPE;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.ResponseEntity.status;

@RestController
public class TokenController {

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/token")
    public Future<TokenResponse> generateToken(@RequestBody @Valid AuthCredentials authCredentials) {
        return tokenService.generateToken(authCredentials).thenApply(TokenResponse::new);
    }

    @PostMapping("/token/scope")
    @Secured(CHANGE_SCOPE)
    public TokenResponse generateScopedToken(@RequestBody @Valid ScopeChangeRequest scopeChangeRequest) {
        return new TokenResponse(tokenService.generateScopedToken(scopeChangeRequest));
    }

    @PostMapping("/token/support-scope")
    public TokenResponse generateScopedTokenForDizzionTeam(@RequestBody @Valid ScopeChangeRequest scopeChangeRequest) {
        return new TokenResponse(tokenService.generateScopedTokenForDizzionTeam(scopeChangeRequest));
    }

    @DeleteMapping("/token/scope")
    public TokenResponse generateUnscopedToken() {
        return new TokenResponse(tokenService.generateUnscopedToken());
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity handleDisabled() {
        return status(FORBIDDEN).body("Organization is disabled");
    }
}

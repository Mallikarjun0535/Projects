package com.dizzion.portal.security.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;

import static io.jsonwebtoken.SignatureAlgorithm.HS512;
import static java.time.Instant.now;

@Service
public class JwtService {

    private final String jwtSecret;
    private final Duration tokenTTL;
    private final Duration externalTokenTTL;

    public JwtService(@Value("${auth.jwt.signature}") String jwtSecret,
                      @Value("${auth.token.ttl.hours}") int tokenTTLInHours,
                      @Value("${auth.external-token.ttl.hours}") int externalTokenTTLInHours) {
        this.jwtSecret = jwtSecret;
        this.tokenTTL = Duration.ofHours(tokenTTLInHours);
        this.externalTokenTTL = Duration.ofHours(externalTokenTTLInHours);
    }

    public String generate(Token token) {
        return Jwts.builder()
                .setClaims(new HashMap<>(token.asMap()))
                .setExpiration(newExpirationDate(tokenTTL))
                .signWith(HS512, jwtSecret)
                .compact();
    }

    public String generateExternalToken(Token token) {
        return Jwts.builder()
                .setClaims(new HashMap<>(token.asMap()))
                .setExpiration(newExpirationDate(externalTokenTTL))
                .signWith(HS512, jwtSecret)
                .compact();
    }

    public Token parse(String token) throws BadCredentialsException, CredentialsExpiredException {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody();
            return Token.from(claims);
        } catch (ExpiredJwtException ex) {
            throw new CredentialsExpiredException("Token expired. ExpirationTime=" + ex.getClaims().getExpiration());
        } catch (JwtException | ClassCastException ex) {
            throw new BadCredentialsException("Invalid token");
        }
    }

    private Date newExpirationDate(Duration ttl) {
        return Date.from(now().plus(ttl));
    }
}

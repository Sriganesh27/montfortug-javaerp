package com.erp.montfortuganda.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final String TOKEN_TYPE_CLAIM =
            "token_type";

    private static final String CREDENTIAL_VERSION_CLAIM =
            "credential_version";

    @Value("${jwt.secret}")
    private String secret;

    @Getter
    @Value("${jwt.expiration}")
    private Long jwtExpirationInMs;

    @Value("${jwt.password-change-expiration:900000}")
    private Long passwordChangeExpirationInMs;

    public String extractUsername(
            String token
    ) {
        return extractClaim(
                token,
                Claims::getSubject
        );
    }

    public Date extractExpiration(
            String token
    ) {
        return extractClaim(
                token,
                Claims::getExpiration
        );
    }

    public TokenType extractTokenType(
            String token
    ) {
        String tokenType =
                extractClaim(
                        token,
                        claims ->
                                claims.get(
                                        TOKEN_TYPE_CLAIM,
                                        String.class
                                )
                );

        if (
                tokenType == null
                        || tokenType.isBlank()
        ) {
            return TokenType.ACCESS;
        }

        try {
            return TokenType.valueOf(tokenType);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "JWT contains an unsupported token type.",
                    exception
            );
        }
    }

    public Integer extractCredentialVersion(
            String token
    ) {
        Object claimValue =
                extractClaim(
                        token,
                        claims ->
                                claims.get(
                                        CREDENTIAL_VERSION_CLAIM
                                )
                );

        if (claimValue == null) {
            return null;
        }

        if (claimValue instanceof Number number) {
            return number.intValue();
        }

        try {
            return Integer.valueOf(
                    claimValue.toString()
            );
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "JWT contains an invalid credential version.",
                    exception
            );
        }
    }

    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver
    ) {
        Claims claims =
                extractAllClaims(token);

        return claimsResolver.apply(claims);
    }

    public String generateToken(
            UserDetails userDetails
    ) {
        Map<String, Object> claims =
                new HashMap<>();

        claims.put(
                TOKEN_TYPE_CLAIM,
                TokenType.ACCESS.name()
        );

        return createToken(
                claims,
                userDetails.getUsername(),
                jwtExpirationInMs
        );
    }

    @SuppressWarnings("unused")
    public String generatePasswordChangeToken(
            UserDetails userDetails,
            Integer credentialVersion
    ) {
        if (
                credentialVersion == null
                        || credentialVersion < 1
        ) {
            throw new IllegalArgumentException(
                    "A valid credential version is required."
            );
        }

        Map<String, Object> claims =
                new HashMap<>();

        claims.put(
                TOKEN_TYPE_CLAIM,
                TokenType.PASSWORD_CHANGE.name()
        );

        claims.put(
                CREDENTIAL_VERSION_CLAIM,
                credentialVersion
        );

        return createToken(
                claims,
                userDetails.getUsername(),
                passwordChangeExpirationInMs
        );
    }

    public boolean validateToken(
            String token,
            UserDetails userDetails
    ) {
        return validateAccessToken(
                token,
                userDetails
        );
    }

    public boolean validateAccessToken(
            String token,
            UserDetails userDetails
    ) {
        return isTokenValidForUser(
                token,
                userDetails
        ) && extractTokenType(token)
                == TokenType.ACCESS;
    }

    @SuppressWarnings("unused")
    public boolean validatePasswordChangeToken(
            String token,
            UserDetails userDetails,
            Integer currentCredentialVersion
    ) {
        if (
                currentCredentialVersion == null
                        || currentCredentialVersion < 1
        ) {
            return false;
        }

        Integer tokenCredentialVersion =
                extractCredentialVersion(token);

        return isTokenValidForUser(
                token,
                userDetails
        )
                && extractTokenType(token)
                == TokenType.PASSWORD_CHANGE
                && currentCredentialVersion.equals(
                tokenCredentialVersion
        );
    }

    private boolean isTokenValidForUser(
            String token,
            UserDetails userDetails
    ) {
        if (userDetails == null) {
            return false;
        }

        String username =
                extractUsername(token);

        return username.equalsIgnoreCase(
                userDetails.getUsername()
        ) && !isTokenExpired(token);
    }

    private Claims extractAllClaims(
            String token
    ) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(
            String token
    ) {
        return extractExpiration(token)
                .before(new Date());
    }

    private String createToken(
            Map<String, Object> claims,
            String subject,
            long expirationInMs
    ) {
        long issuedAt =
                System.currentTimeMillis();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(
                        new Date(issuedAt)
                )
                .setExpiration(
                        new Date(
                                issuedAt
                                        + expirationInMs
                        )
                )
                .signWith(
                        getSigningKey(),
                        SignatureAlgorithm.HS256
                )
                .compact();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(
                secret.getBytes(
                        StandardCharsets.UTF_8
                )
        );
    }

    public enum TokenType {
        ACCESS,
        PASSWORD_CHANGE
    }
}
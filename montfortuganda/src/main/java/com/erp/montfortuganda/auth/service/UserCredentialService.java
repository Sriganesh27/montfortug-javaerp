package com.erp.montfortuganda.auth.service;

import com.erp.montfortuganda.auth.dto.ChangeTemporaryPasswordRequest;
import com.erp.montfortuganda.auth.entity.CredentialDeliveryStatus;
import com.erp.montfortuganda.auth.entity.User;
import com.erp.montfortuganda.auth.jwt.JwtUtil;
import com.erp.montfortuganda.auth.repository.UserRepository;
import com.erp.montfortuganda.infrastructure.service.PasswordService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;

@Service
public class UserCredentialService {

    private static final String BEARER_PREFIX =
            "Bearer ";

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtUtil jwtUtil;

    public UserCredentialService(
            UserRepository userRepository,
            PasswordService passwordService,
            JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public void changeTemporaryPassword(
            String authorizationHeader,
            ChangeTemporaryPasswordRequest request
    ) {
        requireRequest(request);

        String token =
                extractPasswordChangeToken(
                        authorizationHeader
                );

        User user =
                findUserFromToken(token);

        validateAccountState(user);
        validateTemporaryPasswordExpiry(user);
        validatePasswordChangeToken(token, user);
        validateCurrentPassword(
                request.getCurrentPassword(),
                user
        );

        String newPassword =
                validateAndGetNewPassword(request);

        savePermanentPassword(
                user,
                newPassword
        );
    }

    private void requireRequest(
            ChangeTemporaryPasswordRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Password-change information is required."
            );
        }
    }

    private String extractPasswordChangeToken(
            String authorizationHeader
    ) {
        requireBearerHeader(authorizationHeader);

        String token =
                authorizationHeader
                        .substring(BEARER_PREFIX.length())
                        .trim();

        if (token.isEmpty()) {
            throw new SecurityException(
                    "Authorization token is required."
            );
        }

        return token;
    }

    private void requireBearerHeader(
            String authorizationHeader
    ) {
        if (
                authorizationHeader == null
                        || authorizationHeader.isBlank()
        ) {
            throw new SecurityException(
                    "Authorization token is required."
            );
        }

        if (
                !authorizationHeader.regionMatches(
                        true,
                        0,
                        BEARER_PREFIX,
                        0,
                        BEARER_PREFIX.length()
                )
        ) {
            throw new SecurityException(
                    "Authorization token must use the Bearer scheme."
            );
        }
    }

    private User findUserFromToken(
            String token
    ) {
        String username =
                jwtUtil.extractUsername(token);

        return userRepository
                .findByUsername(username)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "User account was not found."
                        )
                );
    }

    private void validateAccountState(
            User user
    ) {
        if (
                !Integer.valueOf(1)
                        .equals(user.getIsActive())
        ) {
            throw new IllegalStateException(
                    "This user account is inactive."
            );
        }

        if (
                !Boolean.TRUE.equals(
                        user.getMustChangePassword()
                )
        ) {
            throw new IllegalStateException(
                    "A temporary password change is not required for this account."
            );
        }
    }

    private void validateTemporaryPasswordExpiry(
            User user
    ) {
        LocalDateTime expiresAt =
                user.getTemporaryPasswordExpiresAt();

        if (
                expiresAt == null
                        || !expiresAt.isAfter(
                        LocalDateTime.now(ZoneOffset.UTC)
                )
        ) {
            user.setCredentialDeliveryStatus(
                    CredentialDeliveryStatus.EXPIRED
            );

            userRepository.save(user);

            throw new IllegalStateException(
                    "The temporary password has expired."
            );
        }
    }

    private void validatePasswordChangeToken(
            String token,
            User user
    ) {
        UserDetails userDetails =
                createUserDetails(user);

        boolean validToken =
                jwtUtil.validatePasswordChangeToken(
                        token,
                        userDetails,
                        user.getCredentialVersion()
                );

        if (!validToken) {
            throw new SecurityException(
                    "The password-change token is invalid or expired."
            );
        }
    }

    private UserDetails createUserDetails(
            User user
    ) {
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(
                        resolveAuthority(user)
                )
                .build();
    }

    private void validateCurrentPassword(
            String currentPassword,
            User user
    ) {
        if (
                currentPassword == null
                        || currentPassword.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Current temporary password is required."
            );
        }

        if (
                !passwordService.matches(
                        currentPassword,
                        user.getPassword()
                )
        ) {
            throw new IllegalArgumentException(
                    "The current temporary password is incorrect."
            );
        }
    }

    private String validateAndGetNewPassword(
            ChangeTemporaryPasswordRequest request
    ) {
        String newPassword =
                requireNewPassword(
                        request.getNewPassword()
                );

        validatePasswordConfirmation(
                newPassword,
                request.getConfirmPassword()
        );

        validatePasswordIsDifferent(
                request.getCurrentPassword(),
                newPassword
        );

        validatePasswordLength(newPassword);
        validatePasswordStrength(newPassword);

        return newPassword;
    }

    private String requireNewPassword(
            String newPassword
    ) {
        if (
                newPassword == null
                        || newPassword.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "New password is required."
            );
        }

        return newPassword;
    }

    private void validatePasswordConfirmation(
            String newPassword,
            String confirmPassword
    ) {
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException(
                    "New password and confirmation do not match."
            );
        }
    }

    private void validatePasswordIsDifferent(
            String currentPassword,
            String newPassword
    ) {
        if (newPassword.equals(currentPassword)) {
            throw new IllegalArgumentException(
                    "The new password must be different from the temporary password."
            );
        }
    }

    private void validatePasswordLength(
            String newPassword
    ) {
        if (
                newPassword.length() < 8
                        || newPassword.length() > 100
        ) {
            throw new IllegalArgumentException(
                    "New password must contain between 8 and 100 characters."
            );
        }
    }

    private void validatePasswordStrength(
            String newPassword
    ) {
        boolean hasLowercase =
                newPassword
                        .chars()
                        .anyMatch(Character::isLowerCase);

        boolean hasUppercase =
                newPassword
                        .chars()
                        .anyMatch(Character::isUpperCase);

        boolean hasNumber =
                newPassword
                        .chars()
                        .anyMatch(Character::isDigit);

        boolean hasSpecialCharacter =
                newPassword
                        .chars()
                        .anyMatch(
                                character ->
                                        !Character.isLetterOrDigit(
                                                character
                                        )
                        );

        if (
                !hasLowercase
                        || !hasUppercase
                        || !hasNumber
                        || !hasSpecialCharacter
        ) {
            throw new IllegalArgumentException(
                    "New password must include an uppercase letter, "
                            + "a lowercase letter, a number and a special character."
            );
        }
    }

    private void savePermanentPassword(
            User user,
            String newPassword
    ) {
        user.setPassword(
                passwordService.hashPassword(
                        newPassword
                )
        );

        user.setMustChangePassword(false);
        user.setTemporaryPasswordCreatedAt(null);
        user.setTemporaryPasswordExpiresAt(null);
        user.setPasswordChangedAt(
                LocalDateTime.now(ZoneOffset.UTC)
        );
        user.setCredentialDeliveryStatus(
                CredentialDeliveryStatus.ACCEPTED
        );
        user.setCredentialVersion(
                nextCredentialVersion(
                        user.getCredentialVersion()
                )
        );

        userRepository.save(user);
    }

    private String resolveAuthority(
            User user
    ) {
        String role = user.getRole();

        if (role == null || role.isBlank()) {
            return "ROLE_USER";
        }

        String normalizedRole =
                role.trim()
                        .toUpperCase(Locale.ROOT);

        return normalizedRole.startsWith("ROLE_")
                ? normalizedRole
                : "ROLE_" + normalizedRole;
    }

    private int nextCredentialVersion(
            Integer currentVersion
    ) {
        return currentVersion == null
                ? 1
                : currentVersion + 1;
    }
}
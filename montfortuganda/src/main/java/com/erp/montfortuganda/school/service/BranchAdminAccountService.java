package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.auth.entity.CredentialDeliveryStatus;
import com.erp.montfortuganda.auth.entity.ErpRole;
import com.erp.montfortuganda.auth.entity.ErpUserRole;
import com.erp.montfortuganda.auth.entity.User;
import com.erp.montfortuganda.auth.repository.ErpRoleRepository;
import com.erp.montfortuganda.auth.repository.UserRepository;
import com.erp.montfortuganda.infrastructure.service.PasswordService;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.service.model.BranchAdminCredentials;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Objects;

@Service
public class BranchAdminAccountService {

    private static final String BRANCH_ADMIN_ROLE_CODE =
            "BRANCH_ADMIN";

    private static final String USERNAME_DOMAIN =
            "@montfort.ug";

    private static final long TEMPORARY_PASSWORD_VALIDITY_HOURS =
            72L;

    private final UserRepository userRepository;
    private final ErpRoleRepository erpRoleRepository;
    private final PasswordService passwordService;

    public BranchAdminAccountService(
            UserRepository userRepository,
            ErpRoleRepository erpRoleRepository,
            PasswordService passwordService
    ) {
        this.userRepository = userRepository;
        this.erpRoleRepository = erpRoleRepository;
        this.passwordService = passwordService;
    }

    /**
     * Creates the initial Branch Admin account.
     * The returned temporary password exists only in application memory.
     * It must be passed to the email flow and must never be stored or logged
     * as plain text.
     */
    @Transactional
    public BranchAdminCredentials createBranchAdmin(
            Branch branch
    ) {
        validateBranch(branch);

        String username =
                buildBranchAdminUsername(
                        branch.getSchoolCode()
                );

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException(
                    "A Branch Admin account already exists with username "
                            + username
                            + "."
            );
        }

        ErpRole branchAdminRole =
                erpRoleRepository
                        .findByRoleCode(
                                BRANCH_ADMIN_ROLE_CODE
                        )
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "The BRANCH_ADMIN role is not configured."
                                )
                        );

        String temporaryPassword =
                passwordService
                        .generateSecureTemporaryPassword();

        LocalDateTime createdAt =
                LocalDateTime.now(ZoneOffset.UTC);

        LocalDateTime expiresAt =
                createdAt.plusHours(
                        TEMPORARY_PASSWORD_VALIDITY_HOURS
                );

        User user = new User();
        user.setUsername(username);
        user.setPassword(
                passwordService.hashPassword(
                        temporaryPassword
                )
        );
        user.setRole(BRANCH_ADMIN_ROLE_CODE);
        user.setAssignedBranch(branch);
        user.setIsActive(1);
        user.setMustChangePassword(true);
        user.setTemporaryPasswordCreatedAt(createdAt);
        user.setTemporaryPasswordExpiresAt(expiresAt);
        user.setPasswordChangedAt(null);
        user.setCredentialDeliveryStatus(
                CredentialDeliveryStatus.PENDING
        );
        user.setCredentialsSentAt(null);
        user.setCredentialDeliveryAttempts(0);
        user.setCredentialVersion(1);

        ErpUserRole userRole = new ErpUserRole();
        userRole.setRole(branchAdminRole);
        userRole.setActive(true);

        user.addRole(userRole);

        User savedUser =
                userRepository.saveAndFlush(user);

        return new BranchAdminCredentials(
                savedUser.getId(),
                branch.getBranchId(),
                username,
                temporaryPassword,
                expiresAt,
                savedUser.getCredentialVersion()
        );
    }

    public String buildBranchAdminUsername(
            String schoolCode
    ) {
        if (
                schoolCode == null
                        || schoolCode.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "School code is required to create a Branch Admin username."
            );
        }

        return schoolCode
                .trim()
                .toLowerCase(Locale.ROOT)
                + USERNAME_DOMAIN;
    }

    /**
     * Replaces the Branch Admin password with a new 72-hour temporary
     * credential. The plain-text value exists only in the returned
     * in-memory object so it can be delivered after the transaction commits.
     */
    @Transactional
    public BranchAdminCredentials resetTemporaryCredentials(
            Branch branch
    ) {
        validateBranch(branch);

        String username =
                buildBranchAdminUsername(
                        branch.getSchoolCode()
                );

        User user =
                userRepository
                        .findByUsernameWithAssignedBranch(
                                username
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "The Branch Admin account was not found for "
                                                + username
                                                + "."
                                )
                        );

        if (
                user.getAssignedBranch() == null
                        || !Objects.equals(
                        user.getAssignedBranch()
                                .getBranchId(),
                        branch.getBranchId()
                )
        ) {
            throw new IllegalStateException(
                    "The Branch Admin account is not assigned to this branch."
            );
        }

        if (!Integer.valueOf(1).equals(user.getIsActive())) {
            throw new IllegalStateException(
                    "The Branch Admin account is inactive."
            );
        }

        String temporaryPassword =
                passwordService
                        .generateSecureTemporaryPassword();

        LocalDateTime createdAt =
                LocalDateTime.now(ZoneOffset.UTC);

        LocalDateTime expiresAt =
                createdAt.plusHours(
                        TEMPORARY_PASSWORD_VALIDITY_HOURS
                );

        user.setPassword(
                passwordService.hashPassword(
                        temporaryPassword
                )
        );
        user.setMustChangePassword(true);
        user.setTemporaryPasswordCreatedAt(createdAt);
        user.setTemporaryPasswordExpiresAt(expiresAt);
        user.setPasswordChangedAt(null);
        user.setCredentialDeliveryStatus(
                CredentialDeliveryStatus.PENDING
        );
        user.setCredentialsSentAt(null);
        user.setCredentialDeliveryAttempts(0);
        user.setCredentialVersion(
                nextCredentialVersion(
                        user.getCredentialVersion()
                )
        );

        User savedUser =
                userRepository.saveAndFlush(user);

        return new BranchAdminCredentials(
                savedUser.getId(),
                branch.getBranchId(),
                username,
                temporaryPassword,
                expiresAt,
                savedUser.getCredentialVersion()
        );
    }

    private int nextCredentialVersion(
            Integer currentVersion
    ) {
        if (currentVersion == null || currentVersion < 1) {
            return 1;
        }

        if (currentVersion == Integer.MAX_VALUE) {
            throw new IllegalStateException(
                    "The credential version cannot be increased."
            );
        }

        return currentVersion + 1;
    }

    private void validateBranch(
            Branch branch
    ) {
        if (branch == null) {
            throw new IllegalArgumentException(
                    "Branch is required."
            );
        }

        if (
                branch.getBranchId() == null
                        || branch.getBranchId() <= 0
        ) {
            throw new IllegalArgumentException(
                    "The branch must be saved before creating its administrator account."
            );
        }

        if (
                branch.getSchoolCode() == null
                        || branch.getSchoolCode().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "The branch school code is required."
            );
        }
    }
}

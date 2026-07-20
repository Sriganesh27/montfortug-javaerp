package com.erp.montfortuganda.auth.service;

import com.erp.montfortuganda.exception.BranchAccessDeniedException;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class BranchAccessService {

    private final BranchRepository branchRepository;
    private final CurrentUserService currentUserService;

    public Integer getAccessibleBranchId(
            Integer requestedBranchId
    ) {
        CurrentUserContext context =
                currentUserService
                        .getCurrentUserContext();

        return resolveAccessibleBranchId(
                context,
                requestedBranchId
        );
    }

    public void validateBranchAccess(
            Integer entityBranchId
    ) {
        if (entityBranchId == null) {
            throw new BranchAccessDeniedException(
                    "The record has no associated branch."
            );
        }

        CurrentUserContext context =
                currentUserService
                        .getCurrentUserContext();

        if (
                !isSuperAdmin(context)
                        && !entityBranchId.equals(
                        context.getBranchId()
                )
        ) {
            throw new BranchAccessDeniedException(
                    "Unauthorized: Cannot modify a record "
                            + "belonging to another branch."
            );
        }
    }

    public Branch getAccessibleBranch(
            Integer requestedBranchId
    ) {
        Integer effectiveBranchId =
                getAccessibleBranchId(
                        requestedBranchId
                );

        return branchRepository
                .findById(effectiveBranchId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Branch not found with ID: "
                                        + effectiveBranchId
                        )
                );
    }

    /**
     * Compatibility method used by existing services that already hold
     * a CurrentUserContext.
     */
    public Integer getValidatedBranchId(
            CurrentUserContext context
    ) {
        return resolveAccessibleBranchId(
                context,
                null
        );
    }

    /**
     * Compatibility overload used by existing services.
     */
    public Integer validateBranchAccess(
            CurrentUserContext context
    ) {
        return resolveAccessibleBranchId(
                context,
                null
        );
    }

    private Integer resolveAccessibleBranchId(
            CurrentUserContext context,
            Integer requestedBranchId
    ) {
        if (context == null) {
            throw new BranchAccessDeniedException(
                    "Current user context is unavailable."
            );
        }

        if (
                isSuperAdmin(context)
                        && requestedBranchId != null
        ) {
            return requestedBranchId;
        }

        Integer currentBranchId =
                context.getBranchId();

        if (currentBranchId == null) {
            throw new BranchAccessDeniedException(
                    "User has no associated branch context."
            );
        }

        if (
                requestedBranchId != null
                        && !requestedBranchId.equals(
                        currentBranchId
                )
        ) {
            throw new BranchAccessDeniedException(
                    "Unauthorized: Cannot access data for branch ID "
                            + requestedBranchId
                            + "."
            );
        }

        return currentBranchId;
    }

    private boolean isSuperAdmin(
            CurrentUserContext context
    ) {
        if (
                context == null
                        || context.getRoles() == null
        ) {
            return false;
        }

        for (String role : context.getRoles()) {
            if (role == null) {
                continue;
            }

            String normalizedRole =
                    role.trim()
                            .toUpperCase(Locale.ROOT);

            if (
                    normalizedRole.equals("SUPER_ADMIN")
                            || normalizedRole.equals(
                            "ROLE_SUPER_ADMIN"
                    )
            ) {
                return true;
            }
        }

        return false;
    }
}
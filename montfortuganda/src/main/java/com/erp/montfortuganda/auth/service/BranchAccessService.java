package com.erp.montfortuganda.auth.service;

import com.erp.montfortuganda.exception.BranchAccessDeniedException;
import com.erp.montfortuganda.school.Branch;
import com.erp.montfortuganda.school.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BranchAccessService {

    private final BranchRepository branchRepository;
    private final CurrentUserService currentUserService;

    public Integer getAccessibleBranchId(Integer requestedBranchId) {
        CurrentUserContext ctx = currentUserService.getCurrentUserContext();
        boolean isSuperAdmin = ctx.getRoles() != null &&
                (ctx.getRoles().contains("SUPER_ADMIN") || ctx.getRoles().contains("ROLE_SUPER_ADMIN"));

        if (isSuperAdmin && requestedBranchId != null) {
            return requestedBranchId;
        }

        if (ctx.getBranchId() == null) {
            throw new BranchAccessDeniedException("User has no associated branch context.");
        }

        if (requestedBranchId != null && !requestedBranchId.equals(ctx.getBranchId()) && !isSuperAdmin) {
            throw new BranchAccessDeniedException("Unauthorized: Cannot access data for branch ID " + requestedBranchId);
        }

        return ctx.getBranchId();
    }

    public void validateBranchAccess(Integer entityBranchId) {
        CurrentUserContext ctx = currentUserService.getCurrentUserContext();
        boolean isSuperAdmin = ctx.getRoles() != null &&
                (ctx.getRoles().contains("SUPER_ADMIN") || ctx.getRoles().contains("ROLE_SUPER_ADMIN"));

        if (!isSuperAdmin && !entityBranchId.equals(ctx.getBranchId())) {
            throw new BranchAccessDeniedException("Unauthorized: Cannot modify record belonging to another branch.");
        }
    }

    public Branch getAccessibleBranch(Integer requestedBranchId) {
        Integer effectiveBranchId = getAccessibleBranchId(requestedBranchId);
        return branchRepository.findById(effectiveBranchId)
                .orElseThrow(() -> new RuntimeException("Branch not found with ID: " + effectiveBranchId));
    }
    @Deprecated
    public Integer getValidatedBranchId(CurrentUserContext ctx) {
        return getAccessibleBranchId(null);
    }
    @Deprecated
    public Integer validateBranchAccess(CurrentUserContext ctx) {
        return getAccessibleBranchId(null);
    }
}
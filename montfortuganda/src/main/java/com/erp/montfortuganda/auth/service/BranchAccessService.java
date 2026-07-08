package com.erp.montfortuganda.auth.service;

import com.erp.montfortuganda.exception.BranchNotAssignedException;
import org.springframework.stereotype.Service;

@Service
public class BranchAccessService {

    // Centralized validation!
    public Integer getValidatedBranchId(CurrentUserContext ctx) {
        if (ctx.getBranchId() == null) {
            throw new BranchNotAssignedException("User does not have an assigned branch.");
        }
        return ctx.getBranchId();
    }
}
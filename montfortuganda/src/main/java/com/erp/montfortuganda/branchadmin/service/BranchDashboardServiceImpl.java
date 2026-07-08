package com.erp.montfortuganda.branchadmin.service;

import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.admission.repository.ErpApplicationRepository;
import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.BranchAccessService;
import com.erp.montfortuganda.branchadmin.dto.BranchDashboardStatsDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BranchDashboardServiceImpl implements BranchDashboardService {

    private final ErpApplicationRepository applicationRepository;
    private final BranchAccessService branchAccessService;

    public BranchDashboardServiceImpl(
            ErpApplicationRepository applicationRepository,
            BranchAccessService branchAccessService) {
        this.applicationRepository = applicationRepository;
        this.branchAccessService = branchAccessService;
    }

    @Override
    @Transactional(readOnly = true)
    public BranchDashboardStatsDTO getDashboardStats(CurrentUserContext ctx) {

        Integer branchId = branchAccessService.getValidatedBranchId(ctx);

        BranchDashboardStatsDTO stats = new BranchDashboardStatsDTO();
        stats.setBranchName(ctx.getBranchName()); // Context already has it!

        stats.setTotalApplications(applicationRepository.countByBranch_BranchId(branchId));
        stats.setPendingVerification(applicationRepository.countByBranch_BranchIdAndApplicationStatus(branchId, ErpApplication.ApplicationStatus.SUBMITTED));
        stats.setAssignedToTeacher(applicationRepository.countByBranch_BranchIdAndApplicationStatus(branchId, ErpApplication.ApplicationStatus.UNDER_REVIEW));
        stats.setSelected(applicationRepository.countByBranch_BranchIdAndApplicationStatus(branchId, ErpApplication.ApplicationStatus.APPROVED));
        stats.setWaitlisted(applicationRepository.countByBranch_BranchIdAndApplicationStatus(branchId, ErpApplication.ApplicationStatus.WAITLISTED));
        stats.setRejected(applicationRepository.countByBranch_BranchIdAndApplicationStatus(branchId, ErpApplication.ApplicationStatus.REJECTED));
        stats.setEnrolled(applicationRepository.countByBranch_BranchIdAndApplicationStatus(branchId, ErpApplication.ApplicationStatus.ADMITTED));

        // TODO: Replace with ScholarshipService counts
        stats.setScholarshipPending(0);
        // TODO: Replace with FeeService counts
        stats.setFeePending(0);

        return stats;
    }
}
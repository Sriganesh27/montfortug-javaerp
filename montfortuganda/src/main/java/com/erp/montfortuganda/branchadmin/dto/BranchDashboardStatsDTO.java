package com.erp.montfortuganda.branchadmin.dto;

import lombok.Data;

@Data
public class BranchDashboardStatsDTO {
    private long totalApplications;
    private long pendingVerification;
    private long assignedToTeacher;
    private long waitingRecommendation;
    private long selected;
    private long waitlisted;
    private long rejected;
    private long scholarshipPending;
    private long feePending;
    private long enrolled;
    private String branchName;
}
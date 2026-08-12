package com.erp.montfortuganda.admission.dto;

import com.erp.montfortuganda.admission.entity.ErpApplication;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Branch-facing School Visit state for one admission application.
 */
@Data
public class ApplicationSchoolVisitResponseDTO {

    private Long applicationId;
    private String applicationNo;

    private ErpApplication.CurrentStage currentStage;
    private ErpApplication.SchoolVisitStatus schoolVisitStatus;

    /**
     * Employee assigned when the parent/student attends the School Visit.
     */
    private Long employeeId;
    private String employeeNo;
    private String employeeName;

    /**
     * Planned School Visit date/time.
     */
    private LocalDateTime scheduledAt;

    /**
     * Actual date/time when the School Visit took place.
     */
    private LocalDateTime visitedAt;

    private Boolean studentAttended;
    private Boolean parentAttended;
    private String remarks;

    private Long completedBy;
    private LocalDateTime completedAt;

    /**
     * Backend-computed UI capabilities.
     */
    private Boolean canSchedule;
    private Boolean canReschedule;
    private Boolean canAssignEmployee;
    private Boolean canComplete;
    private Boolean canProceedToEntranceTest;
}

package com.erp.montfortuganda.admission.dto;

import com.erp.montfortuganda.admission.dto.ApplicationStageTransitionRequestDTO.TransitionAction;
import com.erp.montfortuganda.admission.entity.ErpApplication;
import java.time.LocalDateTime;

/**
 * Compact branch-facing admission application list row.
 *
 * <p>This DTO contains the authoritative workflow and document states needed
 * by the Applications list. It also carries one backend-approved primary
 * action so the browser does not need to open every applicant profile merely
 * to discover the next step.</p>
 */
public class ApplicationSummaryDTO {

    // ---------------------------------------------------------------------
    // Application identity
    // ---------------------------------------------------------------------

    private Long applicationId;
    private String applicationNo;
    private String studentName;

    /**
     * Student gender used by the branch list and Gender filter.
     */
    private ErpApplication.Gender gender;

    /**
     * Resolved academic level name, for example Nursery or Primary.
     */
    private String levelName;

    /**
     * Resolved class name, for example Baby Class or Primary 2.
     */
    private String className;

    // ---------------------------------------------------------------------
    // Status values displayed in the list
    // ---------------------------------------------------------------------

    /**
     * Existing compatibility field used by the current applications.js.
     * It contains the same value as applicationStatus.name().
     */
    private String status;

    private ErpApplication.ApplicationStatus applicationStatus;
    private ErpApplication.CurrentStage currentStage;

    /**
     * Human-readable status/requirement for the CURRENT admission stage.
     *
     * Examples:
     * Documents: Pending
     * Visit: Scheduled
     * Test: Passed
     * Fee: Decision Pending
     * Scholarship: Submitted
     * Payment: Pending
     *
     * The list must never show an older-stage status under a later stage.
     */
    private String currentStageStatus;

    private ErpApplication.DocumentStatus documentStatus;
    private ErpApplication.VerificationStatus verificationStatus;

    /**
     * Kept as String because ErpApplication.scholarshipStatus currently
     * supports legacy database values.
     */
    private String scholarshipStatus;

    private ErpApplication.AdmissionStatus admissionStatus;

    private Boolean workflowLocked;

    // ---------------------------------------------------------------------
    // School Visit list state
    // ---------------------------------------------------------------------

    /**
     * Current School Visit workflow status.
     */
    private ErpApplication.SchoolVisitStatus schoolVisitStatus;

    /**
     * Planned School Visit date and time shown directly in the Applications
     * list after scheduling/rescheduling.
     */
    private LocalDateTime schoolVisitScheduledAt;

    /**
     * Employee is intentionally null while the visit is only scheduled.
     * It is populated later when the parent/student actually attends.
     */
    private Long schoolVisitEmployeeId;

    // ---------------------------------------------------------------------
    // Primary list action
    // ---------------------------------------------------------------------

    /**
     * First backend-approved non-destructive transition for the current stage.
     * Reject, close, return and reopen remain inside the full profile.
     */
    private TransitionAction nextAction;

    private ErpApplication.CurrentStage nextTargetStage;

    /**
     * Human-readable action label, for example:
     * "Start verification", "Move to school visit", or "Open profile".
     */
    private String nextActionLabel;

    /**
     * Indicates whether the list may show the primary action button.
     */
    private Boolean nextActionAvailable;

    // ---------------------------------------------------------------------
    // Dates used for sorting and display
    // ---------------------------------------------------------------------

    private LocalDateTime submittedDate;
    private LocalDateTime updatedAt;

    // ---------------------------------------------------------------------
    // Explicit accessors
    // ---------------------------------------------------------------------
    //
    // Kept explicit here so both IntelliJ's incremental Java compiler and
    // Maven see the same DTO API even if IDE Lombok annotation processing is
    // temporarily unavailable.

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationNo() {
        return applicationNo;
    }

    public void setApplicationNo(String applicationNo) {
        this.applicationNo = applicationNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public ErpApplication.Gender getGender() {
        return gender;
    }

    public void setGender(ErpApplication.Gender gender) {
        this.gender = gender;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ErpApplication.ApplicationStatus getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(
            ErpApplication.ApplicationStatus applicationStatus
    ) {
        this.applicationStatus = applicationStatus;
    }

    public ErpApplication.CurrentStage getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(
            ErpApplication.CurrentStage currentStage
    ) {
        this.currentStage = currentStage;
    }

    public String getCurrentStageStatus() {
        return currentStageStatus;
    }

    public void setCurrentStageStatus(
            String currentStageStatus
    ) {
        this.currentStageStatus = currentStageStatus;
    }

    public ErpApplication.DocumentStatus getDocumentStatus() {
        return documentStatus;
    }

    public void setDocumentStatus(
            ErpApplication.DocumentStatus documentStatus
    ) {
        this.documentStatus = documentStatus;
    }

    public ErpApplication.VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(
            ErpApplication.VerificationStatus verificationStatus
    ) {
        this.verificationStatus = verificationStatus;
    }

    public String getScholarshipStatus() {
        return scholarshipStatus;
    }

    public void setScholarshipStatus(
            String scholarshipStatus
    ) {
        this.scholarshipStatus = scholarshipStatus;
    }

    public ErpApplication.AdmissionStatus getAdmissionStatus() {
        return admissionStatus;
    }

    public void setAdmissionStatus(
            ErpApplication.AdmissionStatus admissionStatus
    ) {
        this.admissionStatus = admissionStatus;
    }

    public Boolean getWorkflowLocked() {
        return workflowLocked;
    }

    public void setWorkflowLocked(
            Boolean workflowLocked
    ) {
        this.workflowLocked = workflowLocked;
    }

    public ErpApplication.SchoolVisitStatus getSchoolVisitStatus() {
        return schoolVisitStatus;
    }

    public void setSchoolVisitStatus(
            ErpApplication.SchoolVisitStatus schoolVisitStatus
    ) {
        this.schoolVisitStatus = schoolVisitStatus;
    }

    public LocalDateTime getSchoolVisitScheduledAt() {
        return schoolVisitScheduledAt;
    }

    public void setSchoolVisitScheduledAt(
            LocalDateTime schoolVisitScheduledAt
    ) {
        this.schoolVisitScheduledAt = schoolVisitScheduledAt;
    }

    public Long getSchoolVisitEmployeeId() {
        return schoolVisitEmployeeId;
    }

    public void setSchoolVisitEmployeeId(
            Long schoolVisitEmployeeId
    ) {
        this.schoolVisitEmployeeId = schoolVisitEmployeeId;
    }

    public TransitionAction getNextAction() {
        return nextAction;
    }

    public void setNextAction(
            TransitionAction nextAction
    ) {
        this.nextAction = nextAction;
    }

    public ErpApplication.CurrentStage getNextTargetStage() {
        return nextTargetStage;
    }

    public void setNextTargetStage(
            ErpApplication.CurrentStage nextTargetStage
    ) {
        this.nextTargetStage = nextTargetStage;
    }

    public String getNextActionLabel() {
        return nextActionLabel;
    }

    public void setNextActionLabel(
            String nextActionLabel
    ) {
        this.nextActionLabel = nextActionLabel;
    }

    public Boolean getNextActionAvailable() {
        return nextActionAvailable;
    }

    public void setNextActionAvailable(
            Boolean nextActionAvailable
    ) {
        this.nextActionAvailable = nextActionAvailable;
    }

    public LocalDateTime getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(
            LocalDateTime submittedDate
    ) {
        this.submittedDate = submittedDate;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(
            LocalDateTime updatedAt
    ) {
        this.updatedAt = updatedAt;
    }

}

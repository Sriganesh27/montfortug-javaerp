package com.erp.montfortuganda.scholarship.dto;

import java.math.BigDecimal;

/**
 * Calculated Scholarship Overview values for one reporting scope.
 *
 * This DTO is intentionally independent of the existing Scholarship
 * application/list DTOs. It is used only by the Scholarship Overview
 * backend and keeps the financial and student/application calculations
 * grouped together.
 */
public class ScholarshipOverviewPeriodDTO {

    /*
     * Financial / fund-flow figures.
     *
     * totalRequestedUgx is the total amount requested by students in this
     * reporting scope.
     *
     * totalFundsReceivedUgx, totalFundsSpentUgx and totalFundsRemainingUgx
     * represent the actual school fund-flow figures. The remaining value
     * must be calculated by the service as:
     *
     *     received - spent
     */
    private BigDecimal totalRequestedUgx = BigDecimal.ZERO;
    private BigDecimal totalFundsReceivedUgx = BigDecimal.ZERO;
    private BigDecimal totalFundsSpentUgx = BigDecimal.ZERO;
    private BigDecimal totalFundsRemainingUgx = BigDecimal.ZERO;

    /*
     * Application amount outcome figures.
     */
    private BigDecimal amountRequestedByStudentsUgx = BigDecimal.ZERO;
    private BigDecimal amountApprovedUgx = BigDecimal.ZERO;
    private BigDecimal amountRejectedUgx = BigDecimal.ZERO;

    /*
     * Student/application counts.
     *
     * These are counts of distinct students as determined by the service.
     */
    private long studentsApplied;
    private long studentsApproved;
    private long studentsRejected;
    private long studentsPending;
    private long studentsUnderVerification;
    private long studentsShortlisted;
    private long studentsBenefited;

    public ScholarshipOverviewPeriodDTO() {
    }

    public BigDecimal getTotalRequestedUgx() {
        return totalRequestedUgx;
    }

    public void setTotalRequestedUgx(BigDecimal totalRequestedUgx) {
        this.totalRequestedUgx =
                totalRequestedUgx == null
                        ? BigDecimal.ZERO
                        : totalRequestedUgx;
    }

    public BigDecimal getTotalFundsReceivedUgx() {
        return totalFundsReceivedUgx;
    }

    public void setTotalFundsReceivedUgx(BigDecimal totalFundsReceivedUgx) {
        this.totalFundsReceivedUgx =
                totalFundsReceivedUgx == null
                        ? BigDecimal.ZERO
                        : totalFundsReceivedUgx;
    }

    public BigDecimal getTotalFundsSpentUgx() {
        return totalFundsSpentUgx;
    }

    public void setTotalFundsSpentUgx(BigDecimal totalFundsSpentUgx) {
        this.totalFundsSpentUgx =
                totalFundsSpentUgx == null
                        ? BigDecimal.ZERO
                        : totalFundsSpentUgx;
    }

    public BigDecimal getTotalFundsRemainingUgx() {
        return totalFundsRemainingUgx;
    }

    public void setTotalFundsRemainingUgx(BigDecimal totalFundsRemainingUgx) {
        this.totalFundsRemainingUgx =
                totalFundsRemainingUgx == null
                        ? BigDecimal.ZERO
                        : totalFundsRemainingUgx;
    }

    public BigDecimal getAmountRequestedByStudentsUgx() {
        return amountRequestedByStudentsUgx;
    }

    public void setAmountRequestedByStudentsUgx(
            BigDecimal amountRequestedByStudentsUgx
    ) {
        this.amountRequestedByStudentsUgx =
                amountRequestedByStudentsUgx == null
                        ? BigDecimal.ZERO
                        : amountRequestedByStudentsUgx;
    }

    public BigDecimal getAmountApprovedUgx() {
        return amountApprovedUgx;
    }

    public void setAmountApprovedUgx(BigDecimal amountApprovedUgx) {
        this.amountApprovedUgx =
                amountApprovedUgx == null
                        ? BigDecimal.ZERO
                        : amountApprovedUgx;
    }

    public BigDecimal getAmountRejectedUgx() {
        return amountRejectedUgx;
    }

    public void setAmountRejectedUgx(BigDecimal amountRejectedUgx) {
        this.amountRejectedUgx =
                amountRejectedUgx == null
                        ? BigDecimal.ZERO
                        : amountRejectedUgx;
    }

    public long getStudentsApplied() {
        return studentsApplied;
    }

    public void setStudentsApplied(long studentsApplied) {
        this.studentsApplied = studentsApplied;
    }

    public long getStudentsApproved() {
        return studentsApproved;
    }

    public void setStudentsApproved(long studentsApproved) {
        this.studentsApproved = studentsApproved;
    }

    public long getStudentsRejected() {
        return studentsRejected;
    }

    public void setStudentsRejected(long studentsRejected) {
        this.studentsRejected = studentsRejected;
    }

    public long getStudentsPending() {
        return studentsPending;
    }

    public void setStudentsPending(long studentsPending) {
        this.studentsPending = studentsPending;
    }

    public long getStudentsUnderVerification() {
        return studentsUnderVerification;
    }

    public void setStudentsUnderVerification(long studentsUnderVerification) {
        this.studentsUnderVerification = studentsUnderVerification;
    }

    public long getStudentsShortlisted() {
        return studentsShortlisted;
    }

    public void setStudentsShortlisted(long studentsShortlisted) {
        this.studentsShortlisted = studentsShortlisted;
    }

    public long getStudentsBenefited() {
        return studentsBenefited;
    }

    public void setStudentsBenefited(long studentsBenefited) {
        this.studentsBenefited = studentsBenefited;
    }
}

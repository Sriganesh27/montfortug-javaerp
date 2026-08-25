package com.erp.montfortuganda.scholarship.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "erp_branch_fund_allocations")
public class ErpBranchFundAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "branch_id",
            nullable = false
    )
    private Long branchId;

    @Column(
            name = "donation_id"
    )
    private Long donationId;

    @Column(
            name = "allocated_amount_ugx",
            precision = 15,
            scale = 2,
            nullable = false
    )
    private BigDecimal allocatedAmountUgx;

    @Column(
            name = "purpose",
            length = 255
    )
    private String purpose = "Branch Scholarship Pool";

    @Column(
            name = "academic_year",
            length = 20,
            nullable = false
    )
    private String academicYear;

    @Column(
            name = "allocated_by_user_id",
            nullable = false
    )
    private Long allocatedByUserId;

    @Column(
            name = "created_at",
            insertable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "created_by"
    )
    private String createdBy;

    @Column(
            name = "updated_at"
    )
    private LocalDateTime updatedAt;

    @Column(
            name = "updated_by"
    )
    private String updatedBy;

    @Column(
            name = "term",
            length = 50,
            nullable = false
    )
    private String term;
}
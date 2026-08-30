package com.erp.montfortuganda.scholarship.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "erp_scholarship_allocations")
public class ErpScholarshipAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scholarship_allocation_id")
    private Long scholarshipAllocationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "scholarship_history_id"
    )
    private ErpScholarshipHistory scholarshipHistory;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "donation_id", nullable = false)
    private Long donationId;

    @Column(
            name = "allocated_amount_ugx",
            precision = 15,
            scale = 2,
            nullable = false
    )
    private BigDecimal allocatedAmountUgx;

    @Column(name = "terms_covered", length = 100, nullable = false)
    private String termsCovered;

    @Column(name = "academic_year", length = 20, nullable = false)
    private String academicYear;

    @Column(name = "allocated_by_user_id", nullable = false)
    private Long allocatedByUserId;

    @Column(
            name = "created_at",
            insertable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "term", length = 50, nullable = false)
    private String term;
}

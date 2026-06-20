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
    private Long id;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donation_id", nullable = false)
    private WebDonation donation;

    @Column(name = "allocated_amount_ugx", precision = 38, scale = 2, nullable = false)
    private BigDecimal allocatedAmountUgx;

    @Column(name = "term", length = 50, nullable = false)
    private String term;

    @Column(name = "academic_year", length = 20, nullable = false)
    private String academicYear;

    @Column(name = "allocated_by_user_id", nullable = false)
    private Long allocatedByUserId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
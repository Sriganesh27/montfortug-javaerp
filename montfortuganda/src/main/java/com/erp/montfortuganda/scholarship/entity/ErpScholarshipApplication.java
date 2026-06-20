package com.erp.montfortuganda.scholarship.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "erp_scholarship_applications")
public class ErpScholarshipApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    // Notice these two are optional (nullable). We can link to either!
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "StudentID", nullable = true)
    private ErpStudent student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", referencedColumnName = "app_id", nullable = true)
    private ErpApplication application;

    @Column(name = "amount_requested_ugx", precision = 38, scale = 2, nullable = false)
    private BigDecimal amountRequestedUgx;

    @Column(name = "term_requested", length = 50, nullable = false)
    private String termRequested;

    @Column(name = "category", length = 100, nullable = false)
    private String category;

    @Column(name = "status", length = 50)
    private String status = "Pending";

    @Column(name = "academic_year", length = 20, nullable = false)
    private String academicYear;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
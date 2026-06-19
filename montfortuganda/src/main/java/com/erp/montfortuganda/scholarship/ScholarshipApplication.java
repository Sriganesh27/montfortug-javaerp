package com.erp.montfortuganda.scholarship;

import com.erp.montfortuganda.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "erp_scholarship_applications")
@Data
public class ScholarshipApplication extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "branch_id", nullable = false)
    private Integer branchId;

    @Column(name = "student_id", nullable = false)
    private Integer studentId;

    @Column(name = "amount_requested_ugx", nullable = false)
    private BigDecimal amountRequestedUgx;

    @Column(name = "term_requested", nullable = false, length = 50)
    private String termRequested;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "status", length = 50)
    private String status = "Pending";

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;
}
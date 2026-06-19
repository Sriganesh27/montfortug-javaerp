package com.erp.montfortuganda.scholarship;

import com.erp.montfortuganda.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "erp_scholarship_allocations")
@Data
public class ScholarshipAllocation extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "branch_id", nullable = false)
    private Integer branchId;

    @Column(name = "student_id", nullable = false)
    private Integer studentId;

    @Column(name = "donation_id")
    private Integer donationId;

    @Column(name = "allocated_amount_ugx", nullable = false)
    private BigDecimal allocatedAmountUgx;

    @Column(name = "term", nullable = false, length = 50)
    private String term;

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;
}
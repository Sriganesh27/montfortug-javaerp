package com.erp.montfortuganda.infrastructure.sequence.student.entity;

import com.erp.montfortuganda.model.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "erp_student_sequences",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_student_sequence",
                        columnNames = {
                                "branch_id",
                                "module_code",
                                "running_year"
                        }
                )
        }
)
public class ErpStudentSequence extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_sequences_id")
    private Long studentSequencesId;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "module_code", nullable = false, length = 255)
    private String moduleCode;

    @Column(name = "running_year", nullable = false)
    private Integer runningYear;

    @Column(name = "current_sequence", nullable = false)
    private Long currentSequence = 0L;
}

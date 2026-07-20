package com.erp.montfortuganda.employee.entity;

import com.erp.montfortuganda.employee.enums.EmployeeCategory;
import com.erp.montfortuganda.school.entity.Branch;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "erp_employee_sequences",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_employee_sequence",
                        columnNames = {
                                "branch_id",
                                "employee_category",
                                "sequence_year"
                        }
                )
        }
)
public class ErpEmployeeSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sequence_id")
    private Long sequenceId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "branch_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_employee_sequence_branch"
            )
    )
    private Branch branch;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "employee_category",
            nullable = false,
            length = 50
    )
    private EmployeeCategory employeeCategory;

    @Column(
            name = "sequence_year",
            nullable = false
    )
    private Integer sequenceYear;

    @Column(
            name = "last_number",
            nullable = false
    )
    private Integer lastNumber = 0;

    @Version
    @Column(
            name = "version",
            nullable = false
    )
    private Long version = 0L;

    public ErpEmployeeSequence(
            Branch branch,
            EmployeeCategory employeeCategory,
            Integer sequenceYear
    ) {
        this.branch = branch;
        this.employeeCategory = employeeCategory;
        this.sequenceYear = sequenceYear;
        this.lastNumber = 0;
        this.version = 0L;
    }

    /**
     * Increments and returns the next employee sequence number.
     */
    public int nextNumber() {
        if (lastNumber == null) {
            lastNumber = 0;
        }

        lastNumber++;
        return lastNumber;
    }
}
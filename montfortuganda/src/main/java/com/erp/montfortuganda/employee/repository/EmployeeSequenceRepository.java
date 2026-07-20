package com.erp.montfortuganda.employee.repository;

import com.erp.montfortuganda.employee.entity.ErpEmployeeSequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeSequenceRepository
        extends JpaRepository<ErpEmployeeSequence, Long> {

    /**
     * Atomically creates the first sequence row or increments the existing row.

     * The database UNIQUE constraint on:
     * branch_id + employee_category + sequence_year
     * prevents duplicate sequence rows during concurrent requests.
     */
    @Modifying(flushAutomatically = true)
    @Query(
            value = """
                    INSERT INTO erp_employee_sequences (
                        branch_id,
                        employee_category,
                        sequence_year,
                        last_number,
                        version
                    )
                    VALUES (
                        :branchId,
                        :employeeCategory,
                        :sequenceYear,
                        1,
                        0
                    )
                    ON DUPLICATE KEY UPDATE
                        last_number = last_number + 1,
                        version = version + 1
                    """,
            nativeQuery = true
    )
    int incrementSequence(
            @Param("branchId") Integer branchId,
            @Param("employeeCategory") String employeeCategory,
            @Param("sequenceYear") Integer sequenceYear
    );

    /**
     * Reads the number reserved by incrementSequence().
     *
     * This must be called in the same transaction immediately after
     * incrementSequence().
     */
    @Query(
            value = """
                    SELECT last_number
                    FROM erp_employee_sequences
                    WHERE branch_id = :branchId
                      AND employee_category = :employeeCategory
                      AND sequence_year = :sequenceYear
                    """,
            nativeQuery = true
    )
    Integer findCurrentNumber(
            @Param("branchId") Integer branchId,
            @Param("employeeCategory") String employeeCategory,
            @Param("sequenceYear") Integer sequenceYear
    );
}
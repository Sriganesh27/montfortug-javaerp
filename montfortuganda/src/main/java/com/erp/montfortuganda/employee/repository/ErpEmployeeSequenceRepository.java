package com.erp.montfortuganda.employee.repository;

import com.erp.montfortuganda.employee.entity.ErpEmployeeSequence;
import com.erp.montfortuganda.employee.enums.EmployeeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository used for transactional Employee-number sequence locking.
 */
@Repository
@SuppressWarnings({
        "SqlNoDataSourceInspection",
        "SqlDialectInspection"
})
public interface ErpEmployeeSequenceRepository
        extends JpaRepository<ErpEmployeeSequence, Long> {

    /**
     * Locks the matching sequence row until the surrounding transaction
     * commits or rolls back.
     *
     * <p>The native {@code FOR UPDATE} syntax is compatible with the legacy
     * remote MySQL/MariaDB database and avoids Hibernate 7 generating
     * {@code FOR UPDATE OF alias}.</p>
     */
    @Query(
            value = """
                    select *
                    from erp_employee_sequences
                    where branch_id = :branchId
                      and employee_category =
                          :#{#employeeCategory.name()}
                      and sequence_year = :sequenceYear
                    for update
                    """,
            nativeQuery = true
    )
    Optional<ErpEmployeeSequence> findForUpdate(
            @Param("branchId")
            Integer branchId,

            @Param("employeeCategory")
            EmployeeCategory employeeCategory,

            @Param("sequenceYear")
            Integer sequenceYear
    );
}

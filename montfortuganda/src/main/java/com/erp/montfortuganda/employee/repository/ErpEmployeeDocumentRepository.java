package com.erp.montfortuganda.employee.repository;

import com.erp.montfortuganda.employee.entity.ErpEmployeeDocument;
import com.erp.montfortuganda.employee.enums.EmployeeDocumentType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Branch-safe persistence operations for Employee documents.

 * Every browser-supplied document or Employee ID is resolved together with
 * the authenticated branch ID. Private file access must use the branch-scoped
 * lookup in this repository before reading anything from storage.
 */
@Repository
public interface ErpEmployeeDocumentRepository
        extends JpaRepository<ErpEmployeeDocument, Long> {

    /**
     * Returns all document records for one Employee, ordered by type and name.
     */
    @EntityGraph(
            attributePaths = {
                    "employee",
                    "employeeDocumentVerifiedBy"
            }
    )
    List<ErpEmployeeDocument>
    findAllByEmployee_EmployeeIdAndEmployee_Branch_BranchIdOrderByEmployeeDocumentTypeAscEmployeeDocumentNameAsc(
            Long employeeId,
            Integer branchId
    );

    /**
     * Branch-safe lookup used while viewing, replacing, updating or deleting
     * one Employee document.
     */
    @EntityGraph(
            attributePaths = {
                    "employee",
                    "employeeDocumentVerifiedBy"
            }
    )
    Optional<ErpEmployeeDocument>
    findByEmployeeDocumentIdAndEmployee_EmployeeIdAndEmployee_Branch_BranchId(
            Long employeeDocumentId,
            Long employeeId,
            Integer branchId
    );

    /**
     * Checks whether an active document of the same standard type already
     * exists for the Employee.
     */
    boolean
    existsByEmployee_EmployeeIdAndEmployee_Branch_BranchIdAndEmployeeDocumentTypeAndEmployeeDocumentActiveTrue(
            Long employeeId,
            Integer branchId,
            EmployeeDocumentType employeeDocumentType
    );

    /**
     * Duplicate-type check used during updates while excluding the document
     * currently being edited.
     */
    boolean
    existsByEmployee_EmployeeIdAndEmployee_Branch_BranchIdAndEmployeeDocumentTypeAndEmployeeDocumentActiveTrueAndEmployeeDocumentIdNot(
            Long employeeId,
            Integer branchId,
            EmployeeDocumentType employeeDocumentType,
            Long employeeDocumentId
    );

    /**
     * Supports custom OTHER documents where more than one record may exist,
     * while still preventing duplicate active custom names.
     */
    boolean
    existsByEmployee_EmployeeIdAndEmployee_Branch_BranchIdAndEmployeeDocumentTypeAndEmployeeDocumentNameIgnoreCaseAndEmployeeDocumentActiveTrue(
            Long employeeId,
            Integer branchId,
            EmployeeDocumentType employeeDocumentType,
            String employeeDocumentName
    );

    /**
     * Custom-document duplicate check used during updates while excluding the
     * current document.
     */
    boolean
    existsByEmployee_EmployeeIdAndEmployee_Branch_BranchIdAndEmployeeDocumentTypeAndEmployeeDocumentNameIgnoreCaseAndEmployeeDocumentActiveTrueAndEmployeeDocumentIdNot(
            Long employeeId,
            Integer branchId,
            EmployeeDocumentType employeeDocumentType,
            String employeeDocumentName,
            Long employeeDocumentId
    );

    long countByEmployee_EmployeeIdAndEmployee_Branch_BranchId(
            Long employeeId,
            Integer branchId
    );

    void deleteAllByEmployee_EmployeeIdAndEmployee_Branch_BranchId(
            Long employeeId,
            Integer branchId
    );
}
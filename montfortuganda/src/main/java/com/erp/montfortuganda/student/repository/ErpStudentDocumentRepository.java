package com.erp.montfortuganda.student.repository;

import com.erp.montfortuganda.student.entity.ErpStudentDocument;
import com.erp.montfortuganda.student.entity.ErpStudentDocument.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ErpStudentDocumentRepository
        extends JpaRepository<ErpStudentDocument, Long>,
        JpaSpecificationExecutor<ErpStudentDocument> {

    /**
     * Loads one document while enforcing branch ownership.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentDocument> findByDocumentIdAndBranch_BranchId(
            Long documentId,
            Integer branchId
    );

    /**
     * Loads one document while verifying student and branch ownership.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentDocument>
    findByDocumentIdAndStudent_StudentIdAndBranch_BranchId(
            Long documentId,
            Long studentId,
            Integer branchId
    );

    /**
     * Returns all documents belonging to one student.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    List<ErpStudentDocument>
    findByStudent_StudentIdAndBranch_BranchIdOrderByCreatedAtDesc(
            Long studentId,
            Integer branchId
    );

    /**
     * Returns active documents belonging to one student.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    List<ErpStudentDocument>
    findByStudent_StudentIdAndBranch_BranchIdAndActiveTrueOrderByCreatedAtDesc(
            Long studentId,
            Integer branchId
    );

    /**
     * Returns active documents by document type.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    List<ErpStudentDocument>
    findByStudent_StudentIdAndBranch_BranchIdAndDocumentTypeIgnoreCaseAndActiveTrueOrderByCreatedAtDesc(
            Long studentId,
            Integer branchId,
            String documentType
    );

    /**
     * Returns active documents by verification status.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    List<ErpStudentDocument>
    findByStudent_StudentIdAndBranch_BranchIdAndDocumentStatusAndActiveTrueOrderByCreatedAtDesc(
            Long studentId,
            Integer branchId,
            DocumentStatus documentStatus
    );

    /**
     * Lists all documents in a branch.
     */
    Page<ErpStudentDocument> findByBranch_BranchId(
            Integer branchId,
            Pageable pageable
    );

    /**
     * Lists active documents in a branch.
     */
    Page<ErpStudentDocument> findByBranch_BranchIdAndActiveTrue(
            Integer branchId,
            Pageable pageable
    );

    /**
     * Lists branch documents by verification status.
     */
    Page<ErpStudentDocument>
    findByBranch_BranchIdAndDocumentStatusAndActiveTrue(
            Integer branchId,
            DocumentStatus documentStatus,
            Pageable pageable
    );

    /**
     * Checks for a duplicate document number for the same student.
     */
    boolean existsByStudent_StudentIdAndDocumentTypeIgnoreCaseAndDocumentNumberIgnoreCaseAndActiveTrue(
            Long studentId,
            String documentType,
            String documentNumber
    );

    /**
     * Duplicate document check during an update.
     */
    boolean existsByStudent_StudentIdAndDocumentTypeIgnoreCaseAndDocumentNumberIgnoreCaseAndActiveTrueAndDocumentIdNot(
            Long studentId,
            String documentType,
            String documentNumber,
            Long documentId
    );

    /**
     * Checks whether a student has an active document of a given type.
     */
    boolean existsByStudent_StudentIdAndBranch_BranchIdAndDocumentTypeIgnoreCaseAndActiveTrue(
            Long studentId,
            Integer branchId,
            String documentType
    );

    long countByStudent_StudentIdAndBranch_BranchIdAndActiveTrue(
            Long studentId,
            Integer branchId
    );

    long countByBranch_BranchIdAndDocumentStatusAndActiveTrue(
            Integer branchId,
            DocumentStatus documentStatus
    );
}
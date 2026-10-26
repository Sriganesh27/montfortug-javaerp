package com.erp.montfortuganda.student.service;

import com.erp.montfortuganda.auth.service.BranchAccessService;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.fees.entity.ErpStudentFeeAssignment;
import com.erp.montfortuganda.student.dto.response.FeePaymentAssignmentResponse;
import com.erp.montfortuganda.student.repository.ErpStudentFeeAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeePaymentAssignmentService {

    private final ErpStudentFeeAssignmentRepository assignmentRepository;
    private final CurrentUserService currentUserService;
    private final BranchAccessService branchAccessService;

    /**
     * Returns active fee assignments for a student in the authenticated
     * user's branch. The caller cannot choose the branch in the request.
     */
    public List<FeePaymentAssignmentResponse> getStudentFeeAssignments(
            Long studentId
    ) {
        if (studentId == null || studentId <= 0) {
            throw new IllegalArgumentException(
                    "Student ID must be greater than zero."
            );
        }

        Integer branchId = branchAccessService.getValidatedBranchId(
                currentUserService.getCurrentUserContext()
        );

        return assignmentRepository
                .findByStudent_StudentIdAndBranch_BranchIdAndActiveTrueOrderByAssignmentDateDescCreatedAtDesc(
                        studentId,
                        branchId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private FeePaymentAssignmentResponse toResponse(
            ErpStudentFeeAssignment assignment
    ) {
        String studentName = assignment.getStudent().getFullName();

        if (studentName == null || studentName.isBlank()) {
            studentName = buildStudentName(assignment);
        }

        return new FeePaymentAssignmentResponse(
                assignment.getFeeAssignmentId(),
                assignment.getStudent().getStudentId(),
                assignment.getAdmissionNo(),
                studentName,
                assignment.getAcademicYear(),
                assignment.getTerm(),
                assignment.getFeeName(),
                assignment.getFeeType(),
                assignment.getTotalFee(),
                assignment.getScholarshipAmount(),
                assignment.getConcessionAmount(),
                assignment.getDiscountAmount(),
                assignment.getFineAmount(),
                assignment.getPayableAmount(),
                assignment.getPaidAmount(),
                assignment.getBalanceAmount(),
                assignment.getAssignmentDate(),
                assignment.getDueDate(),
                assignment.getFeeStatus(),
                Boolean.TRUE.equals(assignment.getActive())
        );
    }

    private String buildStudentName(ErpStudentFeeAssignment assignment) {
        var student = assignment.getStudent();

        return java.util.stream.Stream.of(
                        student.getFirstName(),
                        student.getMiddleName(),
                        student.getLastName()
                )
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .collect(java.util.stream.Collectors.joining(" "));
    }
}

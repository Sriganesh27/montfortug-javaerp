package com.erp.montfortuganda.fees.service;

import com.erp.montfortuganda.auth.service.BranchAccessService;
import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.fees.dto.FeeStructureAssignmentRequest;
import com.erp.montfortuganda.fees.dto.FeeStructureAssignmentResponse;
import com.erp.montfortuganda.fees.entity.ErpFeeStructure;
import com.erp.montfortuganda.fees.entity.ErpFeeStructure.StructureStatus;
import com.erp.montfortuganda.fees.entity.ErpFeeStructureItem;
import com.erp.montfortuganda.fees.entity.ErpFeeStructureItem.ChargingFrequency;
import com.erp.montfortuganda.fees.repository.ErpFeeStructureItemRepository;
import com.erp.montfortuganda.fees.repository.ErpFeeStructureRepository;
import com.erp.montfortuganda.school.entity.ErpAcademicTerm;
import com.erp.montfortuganda.school.entity.ErpAcademicYear;
import com.erp.montfortuganda.student.entity.ErpStudent;
import com.erp.montfortuganda.student.entity.ErpStudentEnrollment;
import com.erp.montfortuganda.student.entity.ErpStudentFeeLedger;
import com.erp.montfortuganda.fees.entity.ErpStudentFeeAssignment;
import com.erp.montfortuganda.student.repository.ErpStudentEnrollmentRepository;
import com.erp.montfortuganda.student.repository.ErpStudentFeeAssignmentRepository;
import com.erp.montfortuganda.student.repository.ErpStudentFeeLedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Creates student fee assignments from a published fee structure.
 * Authorization, enrollment, charging-frequency, and duplicate checks are
 * performed server-side before any assignment is inserted.
 */
@Service
@RequiredArgsConstructor
public class FeeStructureAssignmentService {

    private final ErpFeeStructureRepository structureRepository;
    private final ErpFeeStructureItemRepository itemRepository;
    private final ErpStudentEnrollmentRepository enrollmentRepository;
    private final ErpStudentFeeAssignmentRepository assignmentRepository;
    private final ErpStudentFeeLedgerRepository ledgerRepository;
    private final CurrentUserService currentUserService;
    private final BranchAccessService branchAccessService;

    @Transactional
    public FeeStructureAssignmentResponse assign(Long feeStructureId, FeeStructureAssignmentRequest request) {
        Integer branchId = currentBranchId();
        Long userId = currentUserId();

        ErpFeeStructure structure = structureRepository
                .findForUpdate(feeStructureId, branchId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Fee structure was not found for this branch."));

        if (structure.getStructureStatus() != StructureStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only a published fee structure can be assigned to students.");
        }

        List<ErpFeeStructureItem> items = itemRepository
                .findAllByFeeStructure_FeeStructureIdOrderByDisplayOrderAscFeeStructureItemIdAsc(feeStructureId)
                .stream()
                .filter(item -> Boolean.TRUE.equals(item.getActive()))
                .toList();
        if (items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The published fee structure has no active fee items.");
        }

        List<Long> studentIds = request.studentIds().stream().distinct().toList();
        if (studentIds.size() != request.studentIds().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The student list contains duplicate IDs. Remove duplicates and try again.");
        }

        ErpAcademicYear academicYear = structure.getAcademicYear();
        ErpAcademicTerm term = structure.getTerm();
        if (term.getAcademicYear() == null
                || !academicYear.getAcademicYearId().equals(term.getAcademicYear().getAcademicYearId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The fee structure academic year does not match its term.");
        }

        Set<String> itemNames = new HashSet<>();
        for (ErpFeeStructureItem item : items) {
            String normalizedName = item.getFeeName().trim().toLowerCase(Locale.ROOT);
            if (!itemNames.add(normalizedName)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "The fee structure contains duplicate fee names; correct the structure before assigning it.");
            }
            if (item.getChargingFrequency() == null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Fee item '" + item.getFeeName() + "' has no charging frequency.");
            }
        }

        List<StudentWork> work = new ArrayList<>();

        // Validate every student and every fee item before inserting anything.
        for (Long studentId : studentIds) {
            ErpStudentEnrollment enrollment = enrollmentRepository
                    .findByStudent_StudentIdAndBranch_BranchIdAndActiveTrue(studentId, branchId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Student " + studentId + " has no active enrollment in this branch."));

            if (enrollment.getEnrollmentStatus() != ErpStudentEnrollment.EnrollmentStatus.ACTIVE) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Student " + studentId + " does not have an active enrollment.");
            }
            if (!academicYear.getAcademicYearId().equals(enrollment.getAcademicYearId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Student " + studentId + " is not enrolled in the academic year of this fee structure.");
            }
            if (!structure.getSchoolClass().getClassId().equals(enrollment.getClassId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Student " + studentId + " is not enrolled in the class of this fee structure.");
            }

            ErpStudent student = enrollment.getStudent();
            if (student == null || !Boolean.TRUE.equals(student.getActive())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Student " + studentId + " is inactive and cannot receive a fee assignment.");
            }

            List<String> duplicates = new ArrayList<>();
            for (ErpFeeStructureItem item : items) {
                boolean duplicate = switch (item.getChargingFrequency()) {
                    case PER_TERM, AS_REQUIRED ->
                            assignmentRepository
                                    .existsByStudent_StudentIdAndBranch_BranchIdAndAcademicYearIgnoreCaseAndTermIgnoreCaseAndFeeNameIgnoreCaseAndActiveTrue(
                                            studentId, branchId, academicYear.getAcademicYearName(),
                                            term.getTermName(), item.getFeeName());
                    case PER_ACADEMIC_YEAR ->
                            assignmentRepository
                                    .existsByStudent_StudentIdAndBranch_BranchIdAndAcademicYearIgnoreCaseAndFeeNameIgnoreCaseAndFeeTypeIgnoreCaseAndActiveTrue(
                                            studentId, branchId, academicYear.getAcademicYearName(),
                                            item.getFeeName(), item.getFeeType());
                    case ONE_TIME, PER_ADMISSION ->
                            assignmentRepository
                                    .existsByStudent_StudentIdAndBranch_BranchIdAndFeeNameIgnoreCaseAndFeeTypeIgnoreCaseAndActiveTrue(
                                            studentId, branchId, item.getFeeName(), item.getFeeType());
                };
                if (duplicate) {
                    duplicates.add(item.getFeeName() + " (" + item.getChargingFrequency().name().replace('_', ' ') + ")");
                }
            }

            if (!duplicates.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Student " + studentId + " already has assignment(s) that conflict with the charging frequency: "
                                + String.join(", ", duplicates) + ". No assignments were created.");
            }

            work.add(new StudentWork(student, items));
        }

        List<FeeStructureAssignmentResponse.StudentAssignmentResult> studentResults = new ArrayList<>();
        BigDecimal totalAssigned = BigDecimal.ZERO;
        int totalItems = 0;

        try {
            for (StudentWork studentWork : work) {
                ErpStudent student = studentWork.student();
                List<Long> assignmentIds = new ArrayList<>();
                BigDecimal studentTotal = BigDecimal.ZERO;

                for (ErpFeeStructureItem item : studentWork.items()) {
                    ErpStudentFeeAssignment assignment = new ErpStudentFeeAssignment();
                    assignment.setStudent(student);
                    assignment.setBranch(structure.getBranch());
                    assignment.setAdmissionNo(student.getAdmissionNo());
                    assignment.setAcademicYear(academicYear.getAcademicYearName());
                    assignment.setTerm(term.getTermName());
                    assignment.setFeeName(item.getFeeName());
                    assignment.setFeeType(item.getFeeType());
                    assignment.setFeeStructureItemId(item.getFeeStructureItemId());
                    assignment.setTotalFee(item.getAmount());
                    assignment.setScholarshipAmount(BigDecimal.ZERO);
                    assignment.setConcessionAmount(BigDecimal.ZERO);
                    assignment.setDiscountAmount(BigDecimal.ZERO);
                    assignment.setFineAmount(BigDecimal.ZERO);
                    assignment.setPaidAmount(BigDecimal.ZERO);
                    assignment.setBalanceAmount(item.getAmount());
                    assignment.setPayableAmount(item.getAmount());
                    assignment.setAssignmentDate(LocalDate.now());
                    assignment.setDueDate(item.getDueDate());
                    assignment.setFeeStatus(ErpStudentFeeAssignment.FeeStatus.PENDING);
                    assignment.setActive(true);
                    assignment.setRemarks("Assigned from published fee structure #" + feeStructureId
                            + "; charging frequency: " + item.getChargingFrequency().name()
                            + (item.getRemarks() == null || item.getRemarks().isBlank() ? "" : ". " + item.getRemarks()));
                    assignment.setCreatedBy(userId);
                    assignment.setUpdatedBy(userId);

                    ErpStudentFeeAssignment saved = assignmentRepository.save(assignment);

                    // Record the full assigned amount as a debit in the ledger.
                    // Both records participate in this service's transaction.
                    ErpStudentFeeLedger ledgerEntry = new ErpStudentFeeLedger();
                    ledgerEntry.setFeeAssignment(saved);
                    ledgerEntry.setStudent(student);
                    ledgerEntry.setBranch(structure.getBranch());
                    ledgerEntry.setAdmissionNo(student.getAdmissionNo());
                    ledgerEntry.setAcademicYear(academicYear.getAcademicYearName());
                    ledgerEntry.setTerm(term.getTermName());
                    ledgerEntry.setFeeName(item.getFeeName());
                    ledgerEntry.setFeeType(item.getFeeType());
                    ledgerEntry.setTransactionType(ErpStudentFeeLedger.TransactionType.FEE_ASSIGNED);
                    ledgerEntry.setTransactionDateTime(java.time.LocalDateTime.now());
                    ledgerEntry.setDebitAmount(saved.getPayableAmount());
                    ledgerEntry.setCreditAmount(BigDecimal.ZERO);
                    ledgerEntry.setRunningBalance(saved.getBalanceAmount());
                    ledgerEntry.setCurrency("UGX");
                    ledgerEntry.setLedgerStatus(ErpStudentFeeLedger.LedgerStatus.ACTIVE);
                    ledgerEntry.setRemarks("Fee assigned from published fee structure #" + feeStructureId);
                    ledgerEntry.setCreatedBy(userId);
                    ledgerEntry.setUpdatedBy(userId);
                    ledgerRepository.save(ledgerEntry);

                    assignmentIds.add(saved.getFeeAssignmentId());
                    studentTotal = studentTotal.add(item.getAmount());
                }

                totalItems += assignmentIds.size();
                totalAssigned = totalAssigned.add(studentTotal);
                String studentName = student.getFullName();
                if (studentName == null || studentName.isBlank()) {
                    studentName = String.join(" ", safe(student.getFirstName()), safe(student.getLastName())).trim();
                }
                studentResults.add(new FeeStructureAssignmentResponse.StudentAssignmentResult(
                        student.getStudentId(), student.getAdmissionNo(), studentName,
                        assignmentIds.size(), studentTotal, List.copyOf(assignmentIds)));
            }
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A fee assignment conflicts with an existing assignment. No assignments were saved.", exception);
        }

        return new FeeStructureAssignmentResponse(feeStructureId, studentResults.size(), totalItems,
                totalAssigned, List.copyOf(studentResults));
    }

    private Integer currentBranchId() {
        CurrentUserContext context = currentUserService.getCurrentUserContext();
        return branchAccessService.getValidatedBranchId(context);
    }

    private Long currentUserId() {
        Integer userId = currentUserService.getCurrentUserContext().getUserId();
        return userId == null ? null : userId.longValue();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private record StudentWork(ErpStudent student, List<ErpFeeStructureItem> items) { }
}


package com.erp.montfortuganda.scholarship.service;

import com.erp.montfortuganda.admission.entity.ErpApplicationInterview;
import com.erp.montfortuganda.admission.repository.ErpApplicationInterviewRepository;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.employee.entity.ErpEmployee;
import com.erp.montfortuganda.employee.repository.ErpEmployeeRepository;
import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.scholarship.dto.*;
import com.erp.montfortuganda.scholarship.entity.ErpBranchFundAllocation;
import com.erp.montfortuganda.scholarship.entity.ErpScholarshipAllocation;
import com.erp.montfortuganda.scholarship.entity.ErpScholarshipApplication;
import com.erp.montfortuganda.school.entity.ErpAcademicTerm;
import com.erp.montfortuganda.school.entity.ErpAcademicYear;
import com.erp.montfortuganda.school.entity.SchoolClass;
import com.erp.montfortuganda.school.repository.AcademicTermRepository;
import com.erp.montfortuganda.school.repository.AcademicYearRepository;
import com.erp.montfortuganda.school.repository.SchoolClassRepository;
import com.erp.montfortuganda.scholarship.repository.ErpBranchFundAllocationRepository;
import com.erp.montfortuganda.scholarship.repository.ErpScholarshipAllocationRepository;
import com.erp.montfortuganda.scholarship.repository.ErpScholarshipApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.math.BigDecimal;

@Service
@Transactional
public class ScholarshipVerificationServiceImpl
        implements ScholarshipVerificationService {

    private final ErpScholarshipApplicationRepository applicationRepo;
    private final ErpEmployeeRepository employeeRepo;
    private final ErpApplicationInterviewRepository interviewRepo;
    private final ScholarshipService scholarshipService;
    private final CurrentUserService currentUserService;
    private final ErpBranchFundAllocationRepository branchFundRepo;
    private final ErpScholarshipAllocationRepository allocationRepo;
    private final SchoolClassRepository schoolClassRepo;
    private final AcademicYearRepository academicYearRepo;
    private final AcademicTermRepository academicTermRepo;

    public ScholarshipVerificationServiceImpl(
            ErpScholarshipApplicationRepository applicationRepo,
            ErpEmployeeRepository employeeRepo,
            ErpApplicationInterviewRepository interviewRepo,
            ScholarshipService scholarshipService,
            CurrentUserService currentUserService,
            ErpBranchFundAllocationRepository branchFundRepo,
            ErpScholarshipAllocationRepository allocationRepo,
            SchoolClassRepository schoolClassRepo,
            AcademicYearRepository academicYearRepo,
            AcademicTermRepository academicTermRepo
    ) {
        this.applicationRepo = applicationRepo;
        this.employeeRepo = employeeRepo;
        this.interviewRepo = interviewRepo;
        this.scholarshipService = scholarshipService;
        this.currentUserService = currentUserService;
        this.branchFundRepo = branchFundRepo;
        this.allocationRepo = allocationRepo;
        this.schoolClassRepo = schoolClassRepo;
        this.academicYearRepo = academicYearRepo;
        this.academicTermRepo = academicTermRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getBranchScholarships() {
        Integer branchId = branchId();

        List<ErpScholarshipApplication> applications =
                applicationRepo.findAllByBranchIdAndActiveTrueOrderBySubmittedAtDesc(
                        branchId.longValue()
                );

        Set<Integer> classIds = new HashSet<>();
        for (ErpScholarshipApplication s : applications) {
            if (s.getApplication() != null
                    && s.getApplication().getBranchClassId() != null) {
                classIds.add(s.getApplication().getBranchClassId());
            }
        }

        Map<Integer, SchoolClass> classesById = new HashMap<>();
        if (!classIds.isEmpty()) {
            for (SchoolClass schoolClass : schoolClassRepo.findAllById(classIds)) {
                classesById.put(schoolClass.getClassId(), schoolClass);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (ErpScholarshipApplication s : applications) {

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("scholarshipAppId", s.getScholarshipAppId());
            row.put("applicationId",
                    s.getApplication() != null
                            ? s.getApplication().getApplicationId()
                            : null);
            row.put("applicationNo",
                    s.getApplication() != null
                            ? s.getApplication().getApplicationNo()
                            : null);

            String studentName = null;
            if (s.getApplication() != null) {
                studentName = joinName(
                        s.getApplication().getFirstName(),
                        s.getApplication().getMiddleName(),
                        s.getApplication().getLastName()
                );
            } else if (s.getStudent() != null) {
                studentName = joinName(
                        s.getStudent().getFirstName(),
                        s.getStudent().getMiddleName(),
                        s.getStudent().getLastName()
                );
            }

            row.put("studentName", studentName);
            row.put("academicYear", s.getAcademicYear());
            row.put("term", s.getTermRequested());
            row.put("amountRequestedUgx", s.getAmountRequestedUgx());
            row.put("requestedPercentage", s.getRequestedPercentage());
            row.put("approvedAmount", s.getApprovedAmount());
            row.put("approvedPercentage", s.getApprovedPercentage());

            row.put("verificationEmployeeId",
                    s.getVerificationEmployeeId());
            row.put("verificationEmployeeName",
                    employeeName(
                            s.getVerificationEmployeeId(),
                            branchId
                    ));
            row.put("verificationStatus", s.getVerificationStatus());
            row.put("verificationAssignedAt",
                    s.getVerificationAssignedAt());
            row.put("verificationCompletedAt",
                    s.getVerificationCompletedAt());
            row.put("verificationRemarks", s.getVerificationRemarks());

            row.put("schoolReviewStatus",
                    enumName(s.getSchoolReviewStatus()));
            row.put("schoolReviewRemarks",
                    s.getSchoolReviewRemarks());

            row.put("superAdminReviewStatus",
                    enumName(s.getSuperAdminReviewStatus()));
            row.put("superAdminReviewRemarks",
                    s.getSuperAdminReviewRemarks());

            row.put("scholarshipStatus", s.getStatus());
            row.put("status", s.getStatus());
            row.put("scholarshipType",
                    enumName(s.getScholarshipType()));
            row.put("applicationMethod",
                    enumName(s.getApplicationMethod()));
            row.put("submittedAt", s.getSubmittedAt());
            row.put("createdAt", s.getCreatedAt());

            /*
             * Resolve the actual Class / Level names from the existing
             * SchoolClass reference. Do not expose the internal class ID
             * as the UI value.
             */
            String className = null;
            String levelName = null;

            if (s.getApplication() != null
                    && s.getApplication().getBranchClassId() != null) {
                SchoolClass schoolClass =
                        classesById.get(
                                s.getApplication().getBranchClassId()
                        );

                if (schoolClass != null) {
                    className = schoolClass.getClassName();
                    if (schoolClass.getLevel() != null) {
                        levelName =
                                schoolClass.getLevel().getLevelName();
                    }
                }
            }

            row.put("className", className);
            row.put("levelName", levelName);
            row.put(
                    "classLevel",
                    className != null && levelName != null
                            ? className + " • " + levelName
                            : className != null
                                    ? className
                                    : levelName
            );

            result.add(row);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public ScholarshipApplicationFormResponseDTO getBranchScholarshipDetail(
            Long scholarshipAppId
    ) {
        ErpScholarshipApplication scholarship =
                getBranchApplication(scholarshipAppId);

        if (scholarship.getApplication() == null) {
            throw new BadRequestException(
                    "Scholarship application is not linked to an admission application."
            );
        }

        return scholarshipService.getApplicationFormForBranch(
                scholarship.getApplication().getApplicationId()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getBranchFundSummary() {
        Long branchId = branchId().longValue();

        BigDecimal totalFund =
                branchFundRepo.findAllByBranchId(branchId)
                        .stream()
                        .map(ErpBranchFundAllocation::getAllocatedAmountUgx)
                        .filter(java.util.Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal allocated =
                allocationRepo.findAllByBranchId(branchId)
                        .stream()
                        .map(ErpScholarshipAllocation::getAllocatedAmountUgx)
                        .filter(java.util.Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal committedApproved =
                applicationRepo
                        .findAllByBranchIdAndActiveTrueOrderBySubmittedAtDesc(
                                branchId
                        )
                        .stream()
                        .filter(this::isFinalApproved)
                        .map(ErpScholarshipApplication::getApprovedAmount)
                        .filter(java.util.Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        /*
         * Committed represents approved money not yet physically allocated.
         * This prevents approved/allocated amounts from being double-counted.
         */
        BigDecimal committed =
                committedApproved.subtract(allocated)
                        .max(BigDecimal.ZERO);

        BigDecimal remaining =
                totalFund.subtract(committed).subtract(allocated)
                        .max(BigDecimal.ZERO);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalFundUgx", totalFund);
        result.put("committedUgx", committed);
        result.put("allocatedUgx", allocated);
        result.put("remainingUgx", remaining);

        return result;
    }

    private boolean isFinalApproved(ErpScholarshipApplication application) {
        if (application == null) {
            return false;
        }

        String review =
                enumName(application.getSuperAdminReviewStatus());

        return "APPROVED".equals(review)
                || "PARTIALLY_APPROVED".equals(review);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getBranchScholarshipOverview(
            String academicYear,
            String term,
            Integer levelId,
            Integer classId
    ) {
        Integer branch = branchId();

        ErpAcademicYear currentAcademicYear =
                academicYearRepo
                        .findAllByBranchBranchIdAndActiveTrueOrderByStartDateDesc(
                                branch
                        )
                        .stream()
                        .filter(
                                year -> Boolean.TRUE.equals(
                                        year.getCurrentYear()
                                )
                        )
                        .findFirst()
                        .orElseThrow(
                                () -> new BadRequestException(
                                        "No current active Academic Year is configured for this branch."
                                )
                        );

        ErpAcademicTerm currentAcademicTerm =
                academicTermRepo
                        .findAllActiveByBranchAndAcademicYear(
                                branch,
                                currentAcademicYear.getAcademicYearId()
                        )
                        .stream()
                        .filter(
                                currentTerm -> Boolean.TRUE.equals(
                                        currentTerm.getCurrentTerm()
                                )
                        )
                        .findFirst()
                        .orElseThrow(
                                () -> new BadRequestException(
                                        "No current active Academic Term is configured for this branch."
                                )
                        );

        String currentYearCode =
                currentAcademicYear.getAcademicYearCode();

        String currentTermCode =
                currentAcademicTerm.getTermCode();

        String selectedYear =
                academicYear == null || academicYear.isBlank()
                        ? currentYearCode
                        : academicYear.trim();

        String selectedTerm =
                term == null || term.isBlank()
                        ? currentTermCode
                        : term.trim();

        List<ErpScholarshipApplication> selectedApplications =
                applicationRepo.findActiveOverviewApplications(
                        branch.longValue(),
                        selectedYear,
                        selectedTerm,
                        classId,
                        levelId
                );

        List<ErpScholarshipApplication> currentYearApplications =
                applicationRepo.findActiveOverviewApplications(
                        branch.longValue(),
                        selectedYear,
                        null,
                        null
                );

        List<ErpScholarshipApplication> branchApplications =
                applicationRepo.findActiveOverviewApplications(
                        branch.longValue(),
                        null,
                        null,
                        null
                );

        ScholarshipOverviewPeriodDTO selectedPeriod =
                buildOverviewPeriod(
                        selectedApplications,
                        branch.longValue(),
                        selectedYear,
                        selectedTerm,
                        true
                );

        ScholarshipOverviewPeriodDTO currentYearPeriod =
                buildOverviewPeriod(
                        currentYearApplications,
                        branch.longValue(),
                        currentYearCode,
                        null,
                        false
                );

        ScholarshipOverviewPeriodDTO branchOverall =
                buildOverviewPeriod(
                        branchApplications,
                        branch.longValue(),
                        null,
                        null,
                        false
                );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("academicYear", selectedYear);
        result.put("term", selectedTerm);
        result.put("levelId", levelId);
        result.put("classId", classId);
        result.put("currentAcademicYear", currentYearCode);
        result.put("currentTerm", currentTermCode);
        result.put("selectedPeriod", selectedPeriod);
        result.put("academicYearOverall", currentYearPeriod);
        result.put("branchOverall", branchOverall);

        return result;
    }

    private List<ErpScholarshipApplication>
    filterOverviewApplicationsByLevel(
            List<ErpScholarshipApplication> applications,
            Integer levelId
    ) {
        if (levelId == null) {
            return applications;
        }

        if (applications == null || applications.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Integer> classIds = new HashSet<>();

        for (ErpScholarshipApplication application : applications) {
            if (application != null
                    && application.getApplication() != null
                    && application.getApplication().getBranchClassId() != null) {
                classIds.add(
                        application.getApplication().getBranchClassId()
                );
            }
        }

        if (classIds.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Integer, SchoolClass> classesById =
                new HashMap<>();

        for (SchoolClass schoolClass :
                schoolClassRepo.findAllById(classIds)) {
            classesById.put(
                    schoolClass.getClassId(),
                    schoolClass
            );
        }

        List<ErpScholarshipApplication> filtered =
                new ArrayList<>();

        for (ErpScholarshipApplication application : applications) {
            if (application == null
                    || application.getApplication() == null
                    || application.getApplication().getBranchClassId() == null) {
                continue;
            }

            SchoolClass schoolClass =
                    classesById.get(
                            application.getApplication().getBranchClassId()
                    );

            if (schoolClass != null
                    && schoolClass.getLevel() != null
                    && schoolClass.getLevel().getLevelId() != null
                    && schoolClass.getLevel().getLevelId()
                    .equals(levelId.longValue())) {
                filtered.add(application);
            }
        }

        return filtered;
    }

    private ScholarshipOverviewPeriodDTO buildOverviewPeriod(
            List<ErpScholarshipApplication> applications,
            Long branchId,
            String academicYear,
            String term,
            boolean selectedPeriod
    ) {
        ScholarshipOverviewPeriodDTO overview =
                new ScholarshipOverviewPeriodDTO();

        List<ErpScholarshipApplication> list =
                applications == null
                        ? new ArrayList<>()
                        : applications;

        BigDecimal requested =
                list.stream()
                        .map(
                                ErpScholarshipApplication
                                        ::getAmountRequestedUgx
                        )
                        .filter(
                                java.util.Objects::nonNull
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal approved =
                list.stream()
                        .filter(this::isFinalApproved)
                        .map(
                                ErpScholarshipApplication
                                        ::getApprovedAmount
                        )
                        .filter(
                                java.util.Objects::nonNull
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal rejected =
                list.stream()
                        .filter(
                                application ->
                                        application != null
                                                && application
                                                .getSuperAdminReviewStatus()
                                                == ErpScholarshipApplication
                                                .SuperAdminReviewStatus
                                                .REJECTED
                        )
                        .map(
                                ErpScholarshipApplication
                                        ::getAmountRequestedUgx
                        )
                        .filter(
                                java.util.Objects::nonNull
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        Set<Long> appliedStudents =
                new HashSet<>();

        Set<Long> approvedStudents =
                new HashSet<>();

        Set<Long> rejectedStudents =
                new HashSet<>();

        Set<Long> shortlistedStudents =
                new HashSet<>();

        Set<Long> pendingStudents =
                new HashSet<>();

        Set<Long> verificationStudents =
                new HashSet<>();

        for (ErpScholarshipApplication application : list) {
            if (application == null) {
                continue;
            }

            Long studentId =
                    application.getStudent() != null
                            ? application.getStudent().getStudentId()
                            : application.getApplication() != null
                                    ? application.getApplication()
                                            .getApplicationId()
                                    : application.getScholarshipAppId();

            if (studentId == null) {
                continue;
            }

            appliedStudents.add(studentId);

            String finalReview =
                    enumName(
                            application
                                    .getSuperAdminReviewStatus()
                    );

            String schoolReview =
                    enumName(
                            application
                                    .getSchoolReviewStatus()
                    );

            String verification =
                    application.getVerificationStatus();

            if ("APPROVED".equals(finalReview)
                    || "PARTIALLY_APPROVED".equals(finalReview)) {
                approvedStudents.add(studentId);
                continue;
            }

            if ("REJECTED".equals(finalReview)) {
                rejectedStudents.add(studentId);
                continue;
            }

            if ("SHORTLISTED".equals(schoolReview)) {
                shortlistedStudents.add(studentId);
                continue;
            }

            if ("ASSIGNED".equalsIgnoreCase(verification)
                    || "IN_PROGRESS".equalsIgnoreCase(verification)
                    || "UNDER_REVIEW".equalsIgnoreCase(verification)) {
                verificationStudents.add(studentId);
                continue;
            }

            pendingStudents.add(studentId);
        }

        List<ErpBranchFundAllocation> fundAllocations;

        if (academicYear == null || academicYear.isBlank()) {
            fundAllocations =
                    branchFundRepo.findAllByBranchId(
                            branchId
                    );
        } else if (term == null || term.isBlank()) {
            fundAllocations =
                    branchFundRepo.findAllByBranchIdAndAcademicYear(
                            branchId,
                            academicYear
                    );
        } else {
            fundAllocations =
                    branchFundRepo
                            .findAllByBranchIdAndAcademicYearAndTerm(
                                    branchId,
                                    academicYear,
                                    term
                            );
        }

        BigDecimal received =
                fundAllocations.stream()
                        .map(
                                ErpBranchFundAllocation
                                        ::getAllocatedAmountUgx
                        )
                        .filter(
                                java.util.Objects::nonNull
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        List<ErpScholarshipAllocation> scholarshipAllocations;

        if (academicYear == null || academicYear.isBlank()) {
            scholarshipAllocations =
                    allocationRepo.findAllByBranchId(
                            branchId
                    );
        } else if (term == null || term.isBlank()) {
            scholarshipAllocations =
                    allocationRepo.findAllByBranchIdAndAcademicYear(
                            branchId,
                            academicYear
                    );
        } else {
            scholarshipAllocations =
                    allocationRepo
                            .findAllByBranchIdAndAcademicYearAndTerm(
                                    branchId,
                                    academicYear,
                                    term
                            );
        }

        BigDecimal spent =
                scholarshipAllocations.stream()
                        .map(
                                ErpScholarshipAllocation
                                        ::getAllocatedAmountUgx
                        )
                        .filter(
                                java.util.Objects::nonNull
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal remaining =
                received.subtract(spent);

        overview.setTotalRequestedUgx(requested);
        overview.setTotalFundsReceivedUgx(received);
        overview.setTotalFundsSpentUgx(spent);
        overview.setTotalFundsRemainingUgx(remaining);
        overview.setAmountRequestedByStudentsUgx(requested);
        overview.setAmountApprovedUgx(approved);
        overview.setAmountRejectedUgx(rejected);

        overview.setStudentsApplied(
                appliedStudents.size()
        );
        overview.setStudentsApproved(
                approvedStudents.size()
        );
        overview.setStudentsRejected(
                rejectedStudents.size()
        );
        overview.setStudentsPending(
                pendingStudents.size()
        );
        overview.setStudentsUnderVerification(
                verificationStudents.size()
        );
        overview.setStudentsShortlisted(
                shortlistedStudents.size()
        );

        Set<Long> benefitedStudents =
                new HashSet<>();

        for (ErpScholarshipAllocation allocation :
                scholarshipAllocations) {
            if (allocation != null
                    && allocation.getStudentId() != null) {
                benefitedStudents.add(
                        allocation.getStudentId()
                );
            }
        }

        if (selectedPeriod
                && (list.size() < applications.size())) {
            benefitedStudents.retainAll(
                    appliedStudents
            );
        }

        overview.setStudentsBenefited(
                benefitedStudents.size()
        );

        return overview;
    }


    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getVerificationEmployees() {
        Integer branchId = currentUserService
                .getCurrentUserContext()
                .getBranchId();

        if (branchId == null) {
            throw new BadRequestException(
                    "Authenticated Branch Admin has no assigned branch."
            );
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (ErpEmployee employee :
                employeeRepo
                        .findAllByBranch_BranchIdAndActiveTrueOrderByFullNameAsc(
                                branchId
                        )) {

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("employeeId", employee.getEmployeeId());
            item.put(
                    "employeeName",
                    employee.getFullName() != null
                            ? employee.getFullName()
                            : joinName(
                                    employee.getFirstName(),
                                    employee.getMiddleName(),
                                    employee.getLastName()
                            )
            );
            item.put("employeeNo", employee.getEmployeeNo());
            result.add(item);
        }

        return result;
    }

    @Override
    public void assignVerificationEmployee(
            Long scholarshipAppId,
            ScholarshipVerificationAssignmentRequestDTO request
    ) {
        if (request == null || !request.isAssignmentChoiceValid()) {
            throw new BadRequestException(
                    "Select a verification employee or choose the Entrance Test employee."
            );
        }

        ErpScholarshipApplication scholarship =
                getBranchApplication(scholarshipAppId);

        Long employeeId = request.getEmployeeId();

        if (Boolean.TRUE.equals(request.getUseEntranceTestEmployee())) {
            if (scholarship.getApplication() == null) {
                throw new BadRequestException(
                        "Scholarship application has no admission application."
                );
            }

            ErpApplicationInterview interview =
                    interviewRepo
                            .findByApplication_ApplicationIdAndActiveTrue(
                                    scholarship.getApplication()
                                            .getApplicationId()
                            )
                            .orElseThrow(() ->
                                    new BadRequestException(
                                            "No Entrance Test employee is assigned to this application."
                                    )
                            );

            employeeId = interview.getEmployeeId();
        }

        validateEmployee(employeeId);

        scholarship.setVerificationEmployeeId(employeeId);
        scholarship.setVerificationStatus("ASSIGNED");
        scholarship.setVerificationAssignedAt(
                java.time.LocalDateTime.now()
        );
        scholarship.setVerificationStartedAt(null);
        scholarship.setVerificationCompletedAt(null);
        scholarship.setVerificationRemarks(null);
        scholarship.setUpdatedBy(currentUserId());

        applicationRepo.save(scholarship);
    }

    @Override
    public void completeVerification(
            Long scholarshipAppId,
            ScholarshipVerificationCompletionRequestDTO request
    ) {
        ErpScholarshipApplication scholarship =
                getBranchApplication(scholarshipAppId);

        if (scholarship.getVerificationEmployeeId() == null) {
            throw new BadRequestException(
                    "Assign a Scholarship verification employee first."
            );
        }

        String status =
                scholarship.getVerificationStatus();

        if (!"ASSIGNED".equalsIgnoreCase(status)
                && !"IN_PROGRESS".equalsIgnoreCase(status)) {
            throw new BadRequestException(
                    "Scholarship verification is not ready to be completed."
            );
        }

        scholarship.setVerificationStatus("COMPLETED");
        scholarship.setVerificationCompletedAt(
                java.time.LocalDateTime.now()
        );
        scholarship.setVerificationRemarks(
                request != null
                        ? request.getRemarks()
                        : null
        );
        scholarship.setUpdatedBy(currentUserId());

        applicationRepo.save(scholarship);
    }

    @Override
    public void shortlist(
            Long scholarshipAppId,
            ScholarshipShortlistRequestDTO request
    ) {
        ErpScholarshipApplication scholarship =
                getBranchApplication(scholarshipAppId);

        if (!"COMPLETED".equalsIgnoreCase(
                scholarship.getVerificationStatus()
        )) {
            throw new BadRequestException(
                    "Complete Scholarship verification before making a shortlist decision."
            );
        }

        String decision =
                request != null
                        ? request.getDecision()
                        : null;

        if (decision == null || decision.isBlank()) {
            throw new BadRequestException(
                    "Scholarship shortlist decision is required."
            );
        }

        ErpScholarshipApplication.SchoolReviewStatus reviewStatus;

        try {
            reviewStatus =
                    ErpScholarshipApplication.SchoolReviewStatus
                            .valueOf(decision.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(
                    "Invalid Scholarship shortlist decision."
            );
        }

        if (reviewStatus != ErpScholarshipApplication.SchoolReviewStatus.SHORTLISTED
                && reviewStatus != ErpScholarshipApplication.SchoolReviewStatus.NOT_SHORTLISTED) {
            throw new BadRequestException(
                    "Branch Admin may only shortlist or reject a verified Scholarship application."
            );
        }

        scholarship.setSchoolReviewStatus(reviewStatus);
        scholarship.setSchoolReviewedBy(currentUserId());
        scholarship.setSchoolReviewedAt(
                java.time.LocalDateTime.now()
        );
        scholarship.setSchoolReviewRemarks(
                request.getRemarks()
        );
        scholarship.setUpdatedBy(currentUserId());

        applicationRepo.save(scholarship);
    }

    @Override
    public void finalDecision(
            Long scholarshipAppId,
            ScholarshipFinalDecisionRequestDTO request
    ) {
        ErpScholarshipApplication scholarship =
                getBranchApplication(scholarshipAppId);

        if (request == null
                || request.getDecision() == null
                || request.getDecision().isBlank()) {
            throw new BadRequestException(
                    "Final Scholarship decision is required."
            );
        }

        ErpScholarshipApplication.SuperAdminReviewStatus status;

        try {
            status =
                    ErpScholarshipApplication.SuperAdminReviewStatus
                            .valueOf(
                                    request.getDecision()
                                            .trim()
                                            .toUpperCase()
                            );
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(
                    "Invalid final Scholarship decision."
            );
        }

        scholarship.setSuperAdminReviewStatus(status);
        scholarship.setApprovedAmount(
                request.getApprovedAmount()
        );
        scholarship.setApprovedPercentage(
                request.getApprovedPercentage()
        );
        scholarship.setSuperAdminReviewedBy(currentUserId());
        scholarship.setSuperAdminReviewedAt(
                java.time.LocalDateTime.now()
        );
        scholarship.setSuperAdminReviewRemarks(
                request.getRemarks()
        );
        scholarship.setUpdatedBy(currentUserId());

        if (status == ErpScholarshipApplication.SuperAdminReviewStatus.APPROVED
                || status == ErpScholarshipApplication.SuperAdminReviewStatus.PARTIALLY_APPROVED) {
            scholarship.setStatus("Approved");
            scholarship.setApprovedAt(
                    java.time.LocalDateTime.now()
            );
        } else if (status
                == ErpScholarshipApplication.SuperAdminReviewStatus.REJECTED) {
            scholarship.setStatus("Rejected");
        }

        applicationRepo.save(scholarship);
    }

    private ErpScholarshipApplication getBranchApplication(
            Long scholarshipAppId
    ) {
        Integer branch = currentUserService
                .getCurrentUserContext()
                .getBranchId();

        if (branch == null) {
            throw new BadRequestException(
                    "Authenticated user has no assigned branch."
            );
        }

        return applicationRepo
                .findActiveByScholarshipAppIdAndBranch(
                        scholarshipAppId,
                        branch.longValue()
                )
                .orElseThrow(() ->
                        new BadRequestException(
                                "Scholarship application not found."
                        )
                );
    }

    private void validateEmployee(Long employeeId) {
        Integer branch =
                currentUserService
                        .getCurrentUserContext()
                        .getBranchId();

        if (employeeId == null || branch == null) {
            throw new BadRequestException(
                    "A valid verification employee is required."
            );
        }

        employeeRepo
                .findByEmployeeIdAndBranch_BranchId(
                        employeeId,
                        branch
                )
                .orElseThrow(() ->
                        new BadRequestException(
                                "Selected employee does not belong to this branch."
                        )
                );
    }

    private String employeeName(
            Long employeeId,
            Integer branchId
    ) {
        if (employeeId == null) {
            return null;
        }

        return employeeRepo
                .findByEmployeeIdAndBranch_BranchId(
                        employeeId,
                        branchId
                )
                .map(employee ->
                        employee.getFullName() != null
                                ? employee.getFullName()
                                : joinName(
                                        employee.getFirstName(),
                                        employee.getMiddleName(),
                                        employee.getLastName()
                                )
                )
                .orElse(null);
    }

    private Integer branchId() {
        Integer branch =
                currentUserService
                        .getCurrentUserContext()
                        .getBranchId();

        if (branch == null) {
            throw new BadRequestException(
                    "Authenticated user has no assigned branch."
            );
        }

        return branch;
    }

    private Long currentUserId() {
        return Long.valueOf(
                currentUserService
                        .getCurrentUserContext()
                        .getUserId()
        );
    }

    private String enumName(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private String joinName(
            String first,
            String middle,
            String last
    ) {
        return java.util.stream.Stream.of(first, middle, last)
                .filter(v -> v != null && !v.isBlank())
                .map(String::trim)
                .reduce((a, b) -> a + " " + b)
                .orElse(null);
    }
}


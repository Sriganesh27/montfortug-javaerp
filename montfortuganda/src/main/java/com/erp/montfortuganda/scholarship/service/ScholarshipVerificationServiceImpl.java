
package com.erp.montfortuganda.scholarship.service;

import com.erp.montfortuganda.admission.entity.ErpApplicationInterview;
import com.erp.montfortuganda.admission.repository.ErpApplicationInterviewRepository;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.employee.entity.ErpEmployee;
import com.erp.montfortuganda.employee.repository.ErpEmployeeRepository;
import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.notification.service.EmailService;
import com.erp.montfortuganda.scholarship.dto.*;
import com.erp.montfortuganda.scholarship.entity.ErpBranchFundAllocation;
import com.erp.montfortuganda.scholarship.entity.ErpScholarshipAllocation;
import com.erp.montfortuganda.scholarship.entity.ErpScholarshipApplication;
import com.erp.montfortuganda.scholarship.entity.ErpScholarshipHistory;
import com.erp.montfortuganda.school.entity.ErpAcademicTerm;
import com.erp.montfortuganda.school.entity.ErpAcademicYear;
import com.erp.montfortuganda.school.entity.SchoolClass;
import com.erp.montfortuganda.school.repository.AcademicTermRepository;
import com.erp.montfortuganda.school.repository.AcademicYearRepository;
import com.erp.montfortuganda.school.repository.SchoolClassRepository;
import com.erp.montfortuganda.scholarship.repository.ErpBranchFundAllocationRepository;
import com.erp.montfortuganda.scholarship.repository.ErpScholarshipAllocationRepository;
import com.erp.montfortuganda.scholarship.repository.ErpScholarshipApplicationRepository;
import com.erp.montfortuganda.scholarship.repository.ErpScholarshipHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
    private final ErpScholarshipHistoryRepository historyRepo;
    private final ErpEmployeeRepository employeeRepo;
    private final ErpApplicationInterviewRepository interviewRepo;
    private final ScholarshipService scholarshipService;
    private final CurrentUserService currentUserService;
    private final ErpBranchFundAllocationRepository branchFundRepo;
    private final ErpScholarshipAllocationRepository allocationRepo;
    private final SchoolClassRepository schoolClassRepo;
    private final AcademicYearRepository academicYearRepo;
    private final AcademicTermRepository academicTermRepo;
    private final EmailService emailService;

    public ScholarshipVerificationServiceImpl(
            ErpScholarshipApplicationRepository applicationRepo,
            ErpScholarshipHistoryRepository historyRepo,
            ErpEmployeeRepository employeeRepo,
            ErpApplicationInterviewRepository interviewRepo,
            ScholarshipService scholarshipService,
            CurrentUserService currentUserService,
            ErpBranchFundAllocationRepository branchFundRepo,
            ErpScholarshipAllocationRepository allocationRepo,
            SchoolClassRepository schoolClassRepo,
            AcademicYearRepository academicYearRepo,
            AcademicTermRepository academicTermRepo,
            EmailService emailService
    ) {
        this.applicationRepo = applicationRepo;
        this.historyRepo = historyRepo;
        this.employeeRepo = employeeRepo;
        this.interviewRepo = interviewRepo;
        this.scholarshipService = scholarshipService;
        this.currentUserService = currentUserService;
        this.branchFundRepo = branchFundRepo;
        this.allocationRepo = allocationRepo;
        this.schoolClassRepo = schoolClassRepo;
        this.academicYearRepo = academicYearRepo;
        this.academicTermRepo = academicTermRepo;
        this.emailService = emailService;
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
            row.put("term", historyOf(s).getTermRequested());
            row.put("amountRequestedUgx", historyOf(s).getAmountRequestedUgx());
            row.put("requestedPercentage", historyOf(s).getRequestedPercentage());
            row.put("approvedAmount", historyOf(s).getApprovedAmount());
            row.put("approvedPercentage", historyOf(s).getApprovedPercentage());

            row.put("verificationEmployeeId",
                    historyOf(s).getVerificationEmployeeId());
            row.put("verificationEmployeeName",
                    employeeName(
                            historyOf(s).getVerificationEmployeeId(),
                            branchId
                    ));
            row.put("verificationStatus", historyOf(s).getVerificationStatus());
            row.put("verificationAssignedAt",
                    historyOf(s).getVerificationAssignedAt());
            row.put("verificationCompletedAt",
                    historyOf(s).getVerificationCompletedAt());
            row.put("verificationRemarks", historyOf(s).getVerificationRemarks());

            row.put("schoolReviewStatus",
                    historyOf(s).getSchoolReviewStatus());
            row.put("schoolReviewRemarks",
                    historyOf(s).getSchoolReviewRemarks());

            row.put("superAdminReviewStatus",
                    historyOf(s).getSuperadminReviewStatus());
            row.put("superAdminReviewRemarks",
                    historyOf(s).getSuperadminReviewRemarks());

            row.put("scholarshipStatus", s.getStatus());
            row.put("status", s.getStatus());
            row.put("scholarshipType",
                    enumName(historyOf(s).getScholarshipType()));
            row.put("applicationMethod",
                    historyOf(s).getApplicationMethod());
            row.put("submittedAt", historyOf(s).getSubmittedAt());
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
    public org.springframework.data.domain.Page<ScholarshipApplicationListItemDTO>
    searchBranchScholarships(
            ScholarshipApplicationSearchRequestDTO request
    ) {
        Integer branch = branchId();

        final ScholarshipApplicationSearchRequestDTO safeRequest =
                request == null
                        ? new ScholarshipApplicationSearchRequestDTO()
                        : request;

        List<ErpScholarshipApplication> source =
                applicationRepo.findAllByBranchIdAndActiveTrueOrderBySubmittedAtDesc(
                        branch.longValue()
                );

        List<ErpScholarshipApplication> filtered =
                source.stream()
                        .filter(application ->
                                matchesScholarshipSearch(
                                        application,
                                        safeRequest
                                )
                        )
                        .toList();

        int pageNumber =
                safeRequest.getPage() == null || safeRequest.getPage() < 0
                        ? 0
                        : safeRequest.getPage();

        int pageSize =
                safeRequest.getSize() == null || safeRequest.getSize() <= 0
                        ? 20
                        : Math.min(safeRequest.getSize(), 100);

        int fromIndex =
                Math.min(
                        pageNumber * pageSize,
                        filtered.size()
                );

        int toIndex =
                Math.min(
                        fromIndex + pageSize,
                        filtered.size()
                );

        List<ScholarshipApplicationListItemDTO> rows =
                filtered.subList(fromIndex, toIndex)
                        .stream()
                        .map(this::toScholarshipApplicationListItem)
                        .toList();

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(
                        pageNumber,
                        pageSize
                );

        return new org.springframework.data.domain.PageImpl<>(
                rows,
                pageable,
                filtered.size()
        );
    }

    private boolean matchesScholarshipSearch(
            ErpScholarshipApplication scholarship,
            ScholarshipApplicationSearchRequestDTO request
    ) {
        if (scholarship == null) {
            return false;
        }

        var application = scholarship.getApplication();

        String search = blankToNull(request.getSearch());

        if (search != null) {
            String value = search.toLowerCase();

            String name =
                    application == null
                            ? ""
                            : joinName(
                                    application.getFirstName(),
                                    application.getMiddleName(),
                                    application.getLastName()
                            );

            String applicationNo =
                    application == null
                            ? ""
                            : application.getApplicationNo();

            if (!containsIgnoreCase(name, value)
                    && !containsIgnoreCase(applicationNo, value)) {
                return false;
            }
        }

        String requestedStatus = blankToNull(request.getStatus());

        if (requestedStatus != null
                && (
                        application == null
                        || application.getApplicationStatus() == null
                        || !application.getApplicationStatus()
                                .name()
                                .equalsIgnoreCase(requestedStatus)
                )) {
            return false;
        }

        String verificationStatus =
                blankToNull(request.getVerificationStatus());

        if (verificationStatus != null
                && !enumOrTextEquals(
                        historyOf(scholarship).getVerificationStatus(),
                        verificationStatus
                )) {
            return false;
        }

        String scholarshipStatus =
                blankToNull(request.getScholarshipStatus());

        if (scholarshipStatus != null
                && !enumOrTextEquals(
                        scholarship.getStatus(),
                        scholarshipStatus
                )) {
            return false;
        }

        String schoolReviewStatus =
                blankToNull(request.getSchoolReviewStatus());

        if (schoolReviewStatus != null
                && !enumOrTextEquals(
                        historyOf(scholarship).getSchoolReviewStatus(),
                        schoolReviewStatus
                )) {
            return false;
        }

        String superAdminReviewStatus =
                blankToNull(request.getSuperAdminReviewStatus());

        if (superAdminReviewStatus != null
                && !superAdminReviewStatus.equalsIgnoreCase(
                        historyOf(scholarship).getSuperadminReviewStatus()
                )) {
            return false;
        }

        if (request.getVerificationEmployeeId() != null
                && !request.getVerificationEmployeeId()
                        .equals(historyOf(scholarship).getVerificationEmployeeId())) {
            return false;
        }

        if (request.getLevelId() != null
                || request.getClassId() != null) {

            if (application == null
                    || application.getBranchClassId() == null) {
                return false;
            }

            SchoolClass schoolClass =
                    schoolClassRepo.findById(
                            application.getBranchClassId()
                    ).orElse(null);

            if (schoolClass == null) {
                return false;
            }

            if (request.getClassId() != null
                    && !request.getClassId().equals(
                            schoolClass.getClassId().longValue()
                    )) {
                return false;
            }

            if (request.getLevelId() != null
                    && (
                            schoolClass.getLevel() == null
                            || schoolClass.getLevel().getLevelId() == null
                            || !request.getLevelId().equals(
                                    schoolClass.getLevel().getLevelId().longValue()
                            )
                    )) {
                return false;
            }
        }

        if (!matchesText(
                scholarship.getAcademicYear(),
                request.getAcademicYear()
        )) {
            return false;
        }

        if (!matchesText(
                historyOf(scholarship).getTermRequested(),
                request.getTerm()
        )) {
            return false;
        }

        if (request.getApplicationType() != null
                && !request.getApplicationType().isBlank()) {

            if (application == null
                    || application.getAdmissionType() == null) {
                return false;
            }

            String expected =
                    request.getApplicationType().trim();

            String actual =
                    application.getAdmissionType().name();

            boolean match =
                    "NEW".equalsIgnoreCase(expected)
                    && "NEW".equalsIgnoreCase(actual);

            if ("EXISTING".equalsIgnoreCase(expected)) {
                match =
                        "READMISSION".equalsIgnoreCase(actual)
                        || "TRANSFER".equalsIgnoreCase(actual);
            }

            if (!match) {
                return false;
            }
        }

        if (request.getGender() != null
                && !request.getGender().isBlank()) {

            if (application == null
                    || application.getGender() == null
                    || !application.getGender()
                            .name()
                            .equalsIgnoreCase(
                                    request.getGender().trim()
                            )) {
                return false;
            }
        }

        String familySituation =
                blankToNull(request.getFamilySituation());

        if (familySituation != null
                && !"ALL".equalsIgnoreCase(familySituation)
                && !matchesFamilySituation(
                        scholarship,
                        familySituation
                )) {
            return false;
        }

        return true;
    }

    private boolean matchesFamilySituation(
            ErpScholarshipApplication scholarship,
            String familySituation
    ) {
        if ("ORPHAN".equalsIgnoreCase(familySituation)) {
            return "ORPHAN".equalsIgnoreCase(
                    historyOf(scholarship).getOrphanStatus()
            )
                    || "YES".equalsIgnoreCase(
                    historyOf(scholarship).getOrphanStatus()
            )
                    || "TRUE".equalsIgnoreCase(
                    historyOf(scholarship).getOrphanStatus()
            );
        }

        if ("SINGLE_PARENT".equalsIgnoreCase(familySituation)) {
            boolean fatherDeceased =
                    "DECEASED".equalsIgnoreCase(
                            historyOf(scholarship).getFatherStatus()
                    );

            boolean motherDeceased =
                    "DECEASED".equalsIgnoreCase(
                            historyOf(scholarship).getMotherStatus()
                    );

            return fatherDeceased ^ motherDeceased;
        }

        if ("TWO_PARENTS".equalsIgnoreCase(familySituation)) {
            return "ALIVE".equalsIgnoreCase(
                    historyOf(scholarship).getFatherStatus()
            )
                    && "ALIVE".equalsIgnoreCase(
                    historyOf(scholarship).getMotherStatus()
            );
        }

        return false;
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private boolean matchesText(
            String actual,
            String requested
    ) {
        String value = blankToNull(requested);

        return value == null
                || (
                    actual != null
                    && actual.trim().equalsIgnoreCase(value)
                );
    }

    private boolean containsIgnoreCase(
            String actual,
            String expected
    ) {
        return actual != null
                && expected != null
                && actual.toLowerCase()
                        .contains(expected.toLowerCase());
    }

    private boolean enumOrTextEquals(
            String actual,
            String expected
    ) {
        return actual != null
                && expected != null
                && actual.trim().equalsIgnoreCase(expected.trim());
    }

    private boolean enumEquals(
            Enum<?> actual,
            String expected
    ) {
        return actual != null
                && expected != null
                && actual.name().equalsIgnoreCase(expected.trim());
    }


    private ScholarshipApplicationListItemDTO
    toScholarshipApplicationListItem(
            ErpScholarshipApplication scholarship
    ) {
        ScholarshipApplicationListItemDTO dto =
                new ScholarshipApplicationListItemDTO();

        var application = scholarship.getApplication();

        if (application != null) {
            dto.setApplicationId(
                    application.getApplicationId()
            );
            dto.setApplicationNo(
                    application.getApplicationNo()
            );
            dto.setStudentName(
                    joinName(
                            application.getFirstName(),
                            application.getMiddleName(),
                            application.getLastName()
                    )
            );

            if (application.getGender() != null) {
                dto.setGender(
                        application.getGender().name()
                );
            }

            if (application.getAdmissionType() != null) {
                dto.setAdmissionType(
                        application.getAdmissionType().name()
                );
            }

            Integer classId =
                    application.getBranchClassId();

            if (classId != null) {
                dto.setClassId(
                        classId.longValue()
                );

                schoolClassRepo.findById(classId)
                        .ifPresent(schoolClass -> {
                            dto.setClassName(
                                    schoolClass.getClassName()
                            );

                            if (schoolClass.getLevel() != null) {
                                dto.setLevelId(
                                        schoolClass.getLevel().getLevelId().longValue()
                                );
                                dto.setLevelName(
                                        schoolClass.getLevel().getLevelName()
                                );
                            }
                        });
            }
        }

        dto.setAcademicYear(
                scholarship.getAcademicYear()
        );
        dto.setTerm(
                historyOf(scholarship).getTermRequested()
        );
        dto.setAmountRequestedUgx(
                historyOf(scholarship).getAmountRequestedUgx()
        );
        dto.setVerificationStatus(
                historyOf(scholarship).getVerificationStatus()
        );
        dto.setScholarshipStatus(
                scholarship.getStatus()
        );
        dto.setSchoolReviewStatus(
                historyOf(scholarship).getSchoolReviewStatus()
        );
        dto.setSuperAdminReviewStatus(
                historyOf(scholarship).getSuperadminReviewStatus()
        );
        dto.setSubmittedAt(
                historyOf(scholarship).getSubmittedAt()
        );

        return dto;
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
                        .map(app -> historyOf(app).getApprovedAmount())
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
                historyOf(application).getSuperadminReviewStatus();

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
                                app -> historyOf(app).getAmountRequestedUgx()
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
                                app -> historyOf(app).getApprovedAmount()
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
                                                && "REJECTED".equalsIgnoreCase(
                                                historyOf(application)
                                                        .getSuperadminReviewStatus()
                                        )
                        )
                        .map(
                                app -> historyOf(app).getAmountRequestedUgx()
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
                    historyOf(application)
                            .getSuperadminReviewStatus();

            String schoolReview =
                    historyOf(application)
                            .getSchoolReviewStatus();

            String verification =
                    historyOf(application).getVerificationStatus();

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

    /**
     * Allocates existing Super Admin-provided branch scholarship funds
     * to an approved scholarship application.
     *
     * <p>The authenticated branch is always taken from the current user
     * context. The caller cannot supply a branch ID or donor ID.</p>
     *
     * <p>If the branch pool contains more than one donor/source allocation,
     * the requested amount is split across the available source balances.
     * This preserves the existing donor/source traceability required by
     * erp_scholarship_allocations.</p>
     */
    @Override
    @Transactional
    public void allocateBranchFundToScholarship(
            Long scholarshipAppId,
            BigDecimal amountUgx
    ) {
        if (scholarshipAppId == null || scholarshipAppId <= 0L) {
            throw new BadRequestException(
                    "A valid Scholarship Application ID is required."
            );
        }

        if (amountUgx == null
                || amountUgx.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
                    "Scholarship allocation amount must be greater than zero."
            );
        }

        /*
         * The database stores Scholarship financial values with two decimal
         * places. Reject extra precision instead of allowing the JDBC/database
         * layer to round or truncate a client-supplied amount implicitly.
         */
        if (amountUgx.scale() > 2) {
            throw new BadRequestException(
                    "Scholarship allocation amount may contain at most two decimal places."
            );
        }

        /*
         * Currency values must be represented consistently before any
         * balance calculation or persistence.
         */
        amountUgx = amountUgx.setScale(2);

        Integer branch = branchId();
        Long branchLong = branch.longValue();

        ErpScholarshipApplication scholarship =
                applicationRepo
                        .findActiveByScholarshipAppIdAndBranchForUpdate(
                                scholarshipAppId,
                                branchLong
                        )
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Scholarship application not found."
                                )
                        );

        if (!branchLong.equals(scholarship.getBranchId())) {
            throw new BadRequestException(
                    "Scholarship Application does not belong to your branch."
            );
        }

        if (!isFinalApproved(scholarship)) {
            throw new BadRequestException(
                    "Only an approved or partially approved Scholarship Application can receive branch funds."
            );
        }

        Long studentId =
                scholarship.getStudent() != null
                        ? scholarship.getStudent().getStudentId()
                        : null;

        if (studentId == null) {
            throw new BadRequestException(
                    "Scholarship Application is not linked to a student."
            );
        }

        String academicYear =
                scholarship.getAcademicYear();

        String term =
                historyOf(scholarship).getTermRequested();

        if (academicYear == null || academicYear.isBlank()
                || term == null || term.isBlank()) {
            throw new BadRequestException(
                    "Scholarship academic year and term are required before allocation."
            );
        }

        /*
         * Never allow the Branch Admin to allocate more than the amount
         * approved by Super Admin for this scholarship.
         */
        ErpScholarshipHistory scholarshipHistory =
                historyOf(scholarship);

        BigDecimal approvedAmount =
                scholarshipHistory.getApprovedAmount();

        if (approvedAmount == null
                || approvedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
                    "No approved Scholarship amount is available for allocation."
            );
        }

        /*
         * Build source balances from the existing Super Admin bulk-fund
         * transfers. Each source is reduced by scholarship allocations
         * already consumed from that same source and period.
         */
        /*
         * Lock the complete branch fund pool for this academic year/term
         * before calculating source balances. This serializes concurrent
         * allocations across different Scholarship applications that are
         * competing for the same donor/source pool.
         */
        List<ErpBranchFundAllocation> branchFunds =
                branchFundRepo
                        .findAllByBranchIdAndAcademicYearAndTerm(
                                branchLong,
                                academicYear,
                                term
                        );

        if (branchFunds.isEmpty()) {
            throw new BadRequestException(
                    "No Scholarship funds have been allocated to this branch for the selected academic year and term."
            );
        }

        /*
         * IMPORTANT: Read existing Scholarship allocations only AFTER the
         * branch fund pool has been pessimistically locked. This guarantees
         * concurrent Scholarship allocation transactions see the committed
         * allocations from transactions that acquired the same pool lock
         * before them.
         */
        List<ErpScholarshipAllocation> periodAllocations =
                allocationRepo
                        .findAllByBranchIdAndAcademicYearAndTerm(
                                branchLong,
                                academicYear,
                                term
                        );

        BigDecimal alreadyAllocatedToStudent =
                periodAllocations.stream()
                        .filter(
                                allocation ->
                                        allocation != null
                                                && studentId.equals(
                                                        allocation.getStudentId()
                                                )
                                                && allocation.getScholarshipHistory() != null
                                                && scholarshipHistory.getScholarshipHistoryId()
                                                        .equals(
                                                                allocation.getScholarshipHistory()
                                                                        .getScholarshipHistoryId()
                                                        )
                        )
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

        BigDecimal remainingEligible =
                approvedAmount
                        .subtract(alreadyAllocatedToStudent)
                        .max(BigDecimal.ZERO);

        if (amountUgx.compareTo(remainingEligible) > 0) {
            throw new BadRequestException(
                    "Allocation exceeds the remaining approved Scholarship amount."
            );
        }

        Map<Long, BigDecimal> fundByDonation =
                new LinkedHashMap<>();

        for (ErpBranchFundAllocation fund : branchFunds) {
            if (fund == null
                    || fund.getDonationId() == null
                    || fund.getAllocatedAmountUgx() == null
                    || fund.getAllocatedAmountUgx()
                    .compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            fundByDonation.merge(
                    fund.getDonationId(),
                    fund.getAllocatedAmountUgx(),
                    BigDecimal::add
            );
        }

        if (fundByDonation.isEmpty()) {
            throw new BadRequestException(
                    "The branch Scholarship fund has no valid funding source."
            );
        }

        Map<Long, BigDecimal> usedByDonation =
                new HashMap<>();

        for (ErpScholarshipAllocation allocation :
                periodAllocations) {

            if (allocation == null
                    || allocation.getDonationId() == null
                    || allocation.getAllocatedAmountUgx() == null) {
                continue;
            }

            usedByDonation.merge(
                    allocation.getDonationId(),
                    allocation.getAllocatedAmountUgx(),
                    BigDecimal::add
            );
        }

        BigDecimal remainingToAllocate = amountUgx;
        Long userId = currentUserId();

        for (Map.Entry<Long, BigDecimal> entry :
                fundByDonation.entrySet()) {

            if (remainingToAllocate.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            Long donationId = entry.getKey();

            BigDecimal sourceUsed =
                    usedByDonation.getOrDefault(
                            donationId,
                            BigDecimal.ZERO
                    );

            BigDecimal sourceAvailable =
                    entry.getValue()
                            .subtract(sourceUsed)
                            .max(BigDecimal.ZERO);

            if (sourceAvailable.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal sourceAllocation =
                    sourceAvailable.min(
                            remainingToAllocate
                    );

            ErpScholarshipAllocation allocation =
                    new ErpScholarshipAllocation();

            allocation.setScholarshipHistory(
                    scholarshipHistory
            );
            allocation.setBranchId(branchLong);
            allocation.setStudentId(studentId);
            allocation.setDonationId(donationId);
            allocation.setAllocatedAmountUgx(
                    sourceAllocation
            );
            allocation.setTermsCovered(term);
            allocation.setAcademicYear(academicYear);
            allocation.setTerm(term);
            allocation.setAllocatedByUserId(userId);

            allocationRepo.save(
                    allocation
            );

            remainingToAllocate =
                    remainingToAllocate
                            .subtract(sourceAllocation);
        }

        if (remainingToAllocate.compareTo(BigDecimal.ZERO) > 0) {
            throw new BadRequestException(
                    "The branch has insufficient available Scholarship funds for this allocation."
            );
        }
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

        historyOf(scholarship).setVerificationEmployeeId(employeeId);
        historyOf(scholarship).setVerificationStatus("ASSIGNED");
        historyOf(scholarship).setVerificationAssignedAt(
                java.time.LocalDateTime.now()
        );
        historyOf(scholarship).setVerificationStartedAt(null);
        historyOf(scholarship).setVerificationCompletedAt(null);
        historyOf(scholarship).setVerificationRemarks(null);
        scholarship.setUpdatedBy(currentUserId());

        saveHistory(scholarship);
        applicationRepo.save(scholarship);
    }

    @Override
    public void completeVerification(
            Long scholarshipAppId,
            ScholarshipVerificationCompletionRequestDTO request
    ) {
        ErpScholarshipApplication scholarship =
                getBranchApplication(scholarshipAppId);

        if (historyOf(scholarship).getVerificationEmployeeId() == null) {
            throw new BadRequestException(
                    "Assign a Scholarship verification employee first."
            );
        }

        String status =
                historyOf(scholarship).getVerificationStatus();

        if (!"ASSIGNED".equalsIgnoreCase(status)
                && !"IN_PROGRESS".equalsIgnoreCase(status)) {
            throw new BadRequestException(
                    "Scholarship verification is not ready to be completed."
            );
        }

        historyOf(scholarship).setVerificationStatus("COMPLETED");
        historyOf(scholarship).setVerificationCompletedAt(
                java.time.LocalDateTime.now()
        );
        historyOf(scholarship).setVerificationRemarks(
                request != null
                        ? request.getRemarks()
                        : null
        );
        scholarship.setUpdatedBy(currentUserId());

        saveHistory(scholarship);
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
                historyOf(scholarship).getVerificationStatus()
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

        historyOf(scholarship).setSchoolReviewStatus(reviewStatus.name());
        historyOf(scholarship).setSchoolReviewedBy(currentUserId());
        historyOf(scholarship).setSchoolReviewedAt(
                java.time.LocalDateTime.now()
        );
        historyOf(scholarship).setSchoolReviewRemarks(
                request.getRemarks()
        );

        /*
         * A shortlisted Scholarship is now ready for Super Admin review.
         * This stage transition is essential because the Super Admin final
         * decision endpoint accepts requests only from SUPER_ADMIN_REVIEW.
         *
         * A NOT_SHORTLISTED application must not enter Super Admin review.
         */
        if (reviewStatus
                == ErpScholarshipApplication.SchoolReviewStatus.SHORTLISTED) {
            historyOf(scholarship).setCurrentStage(
                    "SUPER_ADMIN_REVIEW"
            );
        } else if (
                reviewStatus
                        == ErpScholarshipApplication.SchoolReviewStatus.NOT_SHORTLISTED
        ) {
            /*
             * NOT_SHORTLISTED is a terminal Branch Admin decision for this
             * review cycle. It must never leave the application waiting for
             * Super Admin review.
             */
            historyOf(scholarship).setCurrentStage(
                    "BRANCH_NOT_SHORTLISTED"
            );
        }

        scholarship.setUpdatedBy(currentUserId());

        saveHistory(scholarship);
        applicationRepo.save(scholarship);
    }


    public void decideFromWaitlist(
            Long scholarshipAppId,
            ScholarshipShortlistRequestDTO request
    ) {
        ErpScholarshipApplication scholarship =
                getBranchApplication(scholarshipAppId);

        ErpScholarshipHistory history =
                historyOf(scholarship);

        if (!ErpScholarshipApplication.SchoolReviewStatus.WAITLISTED.name()
                .equalsIgnoreCase(history.getSchoolReviewStatus())) {
            throw new BadRequestException(
                    "Only a Scholarship currently on WAITLIST can use this action."
            );
        }

        String decision =
                request != null
                        ? request.getDecision()
                        : null;

        if (decision == null || decision.isBlank()) {
            throw new BadRequestException(
                    "Scholarship waitlist decision is required."
            );
        }

        ErpScholarshipApplication.SchoolReviewStatus status;

        try {
            status =
                    ErpScholarshipApplication.SchoolReviewStatus
                            .valueOf(decision.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(
                    "Invalid Scholarship waitlist decision."
            );
        }

        if (status != ErpScholarshipApplication.SchoolReviewStatus.SHORTLISTED
                && status != ErpScholarshipApplication.SchoolReviewStatus.REJECTED) {
            throw new BadRequestException(
                    "A waitlisted Scholarship may only be shortlisted or rejected."
            );
        }

        if (status == ErpScholarshipApplication.SchoolReviewStatus.REJECTED
                && (request.getRemarks() == null
                || request.getRemarks().isBlank())) {
            throw new BadRequestException(
                    "Rejection reason is required."
            );
        }

        history.setSchoolReviewStatus(status.name());
        history.setSchoolReviewedBy(currentUserId());
        history.setSchoolReviewedAt(
                java.time.LocalDateTime.now()
        );
        history.setSchoolReviewRemarks(
                request.getRemarks()
        );

        /*
         * A waitlisted Scholarship may re-enter the Super Admin workflow
         * only after Branch Admin explicitly shortlists it. A rejection
         * remains outside the Super Admin workflow.
         */
        if (status
                == ErpScholarshipApplication.SchoolReviewStatus.SHORTLISTED) {
            history.setCurrentStage(
                    "SUPER_ADMIN_REVIEW"
            );
        } else if (
                status
                        == ErpScholarshipApplication.SchoolReviewStatus.REJECTED
        ) {
            history.setCurrentStage(
                    "BRANCH_WAITLIST_REJECTED"
            );
        }

        scholarship.setUpdatedBy(currentUserId());

        /*
         * No email is sent from the WAITLISTED state itself.
         * Email #1 for SHORTLISTED/REJECTED is handled by the dedicated
         * decision-email integration, not by this state transition method.
         */
        saveHistory(scholarship);
        applicationRepo.save(scholarship);
    }

    @Transactional
    public void completeBranchDistribution(
            Long scholarshipAppId,
            ScholarshipBranchDistributionCompletionRequestDTO request
    ) {
        if (scholarshipAppId == null || scholarshipAppId <= 0L) {
            throw new BadRequestException(
                    "A valid Scholarship Application ID is required."
            );
        }

        if (request == null
                || request.getScholarshipHistoryId() == null) {
            throw new BadRequestException(
                    "Scholarship History ID is required."
            );
        }

        ErpScholarshipApplication scholarship =
                getBranchApplication(scholarshipAppId);

        Integer branch = branchId();
        Long branchLong = branch.longValue();

        if (!branchLong.equals(scholarship.getBranchId())) {
            throw new BadRequestException(
                    "Scholarship Application does not belong to your branch."
            );
        }

        ErpScholarshipHistory history =
                historyOf(scholarship);

        if (history == null
                || !request.getScholarshipHistoryId()
                .equals(history.getScholarshipHistoryId())) {
            throw new BadRequestException(
                    "Scholarship History does not belong to this Scholarship Application."
            );
        }

        if (history.getApprovedAmount() == null
                || history.getApprovedAmount()
                .compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
                    "No approved Scholarship amount is available for distribution."
            );
        }

        /*
         * Distribution completion is only valid after the approved amount
         * has actually been allocated to the student for the same branch,
         * academic year and term. Do not allow a status-only distribution.
         */
        Long studentId =
                scholarship.getStudent() != null
                        ? scholarship.getStudent().getStudentId()
                        : null;

        if (studentId == null) {
            throw new BadRequestException(
                    "Scholarship Application is not linked to a student."
            );
        }

        String academicYear = scholarship.getAcademicYear();
        String term = history.getTermRequested();

        if (academicYear == null || academicYear.isBlank()
                || term == null || term.isBlank()) {
            throw new BadRequestException(
                    "Scholarship academic year and term are required before distribution."
            );
        }

        BigDecimal allocatedAmount =
                allocationRepo
                        .findAllByBranchIdAndAcademicYearAndTerm(
                                branchLong,
                                academicYear,
                                term
                        )
                        .stream()
                        .filter(allocation ->
                                allocation != null
                                        && studentId.equals(
                                        allocation.getStudentId()
                                )
                                        && allocation.getScholarshipHistory() != null
                                        && history.getScholarshipHistoryId()
                                                .equals(
                                                        allocation.getScholarshipHistory()
                                                                .getScholarshipHistoryId()
                                                ))
                        .map(ErpScholarshipAllocation::getAllocatedAmountUgx)
                        .filter(java.util.Objects::nonNull)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        if (allocatedAmount.compareTo(history.getApprovedAmount()) < 0) {
            throw new BadRequestException(
                    "Scholarship distribution cannot be completed until the full approved amount has been allocated."
            );
        }

        /*
         * Distribution completion is idempotency-safe. Once completed,
         * a repeated request must not rewrite the workflow or audit state.
         */
        if ("DISTRIBUTION_COMPLETED".equalsIgnoreCase(
                history.getCurrentStage()
        )) {
            throw new BadRequestException(
                    "Scholarship distribution has already been completed."
            );
        }

        history.setCurrentStage(
                "DISTRIBUTION_COMPLETED"
        );
        history.setStatus(
                "Distributed"
        );
        history.setReviewerRemarks(
                request.getRemarks()
        );
        history.setReviewedBy(
                currentUserId()
        );

        scholarship.setUpdatedBy(
                currentUserId()
        );

        saveHistory(scholarship);
        applicationRepo.save(scholarship);
    }

    @Override
    public void finalDecision(
            Long scholarshipAppId,
            ScholarshipFinalDecisionRequestDTO request
    ) {
        /*
         * Super Admin is not branch-scoped. The Super Admin controller is
         * protected by SUPER_ADMIN, so this lookup must not require the
         * authenticated user's branch. The scholarship record itself carries
         * the authoritative branch_id.
         */
        ErpScholarshipApplication scholarship =
                applicationRepo
                        .findActiveByScholarshipAppIdForUpdate(
                                scholarshipAppId
                        )
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Scholarship application not found."
                                )
                        );

        if (request == null
                || request.getDecision() == null
                || request.getDecision().isBlank()) {
            throw new BadRequestException(
                    "Final Scholarship decision is required."
            );
        }

        ErpScholarshipHistory history =
                historyOf(scholarship);

        /*
         * A final Super Admin decision is allowed only while the Scholarship
         * is actually waiting for Super Admin review. This prevents an old
         * or already-finalized Scholarship from being decided again.
         */
        String currentStage =
                history.getCurrentStage();

        if (currentStage == null
                || !"SUPER_ADMIN_REVIEW".equalsIgnoreCase(
                        currentStage.trim()
                )) {
            throw new BadRequestException(
                    "Scholarship application is not currently awaiting Super Admin review."
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

        BigDecimal approvedAmount =
                request.getApprovedAmount();

        BigDecimal requestedAmount =
                history.getAmountRequestedUgx();

        BigDecimal approvedPercentage =
                request.getApprovedPercentage();

        /*
         * Financial values are validated against the authoritative
         * Scholarship History request, never trusted blindly from the client.
         */
        if (status
                == ErpScholarshipApplication.SuperAdminReviewStatus.APPROVED
                || status
                == ErpScholarshipApplication.SuperAdminReviewStatus.PARTIALLY_APPROVED) {

            if (approvedAmount == null
                    || approvedAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException(
                        "A positive approved Scholarship amount is required."
                );
            }

            if (requestedAmount != null
                    && approvedAmount.compareTo(requestedAmount) > 0) {
                throw new BadRequestException(
                        "Approved Scholarship amount cannot exceed the requested amount."
                );
            }

            if (approvedPercentage == null
                    || approvedPercentage.compareTo(BigDecimal.ZERO) < 0
                    || approvedPercentage.compareTo(new BigDecimal("100")) > 0) {
                throw new BadRequestException(
                        "Approved Scholarship percentage must be between 0 and 100."
                );
            }

        } else if (
                status
                        == ErpScholarshipApplication.SuperAdminReviewStatus.REJECTED
        ) {

            if (request.getRemarks() == null
                    || request.getRemarks().isBlank()) {
                throw new BadRequestException(
                        "Rejection reason is required for a rejected Scholarship."
                );
            }

            /*
             * A rejected Scholarship cannot carry a financial approval.
             * Store zero rather than trusting a client-supplied amount.
             */
            approvedAmount = BigDecimal.ZERO;
            approvedPercentage = BigDecimal.ZERO;

        } else {
            throw new BadRequestException(
                    "Unsupported final Scholarship decision."
            );
        }

        history.setSuperadminReviewStatus(status.name());
        history.setApprovedAmount(approvedAmount);
        history.setApprovedPercentage(approvedPercentage);
        history.setSuperadminReviewedBy(currentUserId());
        history.setSuperadminReviewedAt(
                java.time.LocalDateTime.now()
        );
        history.setSuperadminReviewRemarks(
                request.getRemarks()
        );
        scholarship.setUpdatedBy(currentUserId());

        if (status
                == ErpScholarshipApplication.SuperAdminReviewStatus.APPROVED
                || status
                == ErpScholarshipApplication.SuperAdminReviewStatus.PARTIALLY_APPROVED) {

            scholarship.setStatus("Approved");
            history.setApprovedAt(
                    java.time.LocalDateTime.now()
            );
            history.setCurrentStage(
                    "RETURNED_TO_BRANCH_ADMIN"
            );

        } else if (
                status
                        == ErpScholarshipApplication.SuperAdminReviewStatus.REJECTED
        ) {

            scholarship.setStatus("Rejected");
            history.setCurrentStage(
                    "RETURNED_TO_BRANCH_ADMIN"
            );
        }

        saveHistory(scholarship);
        applicationRepo.saveAndFlush(scholarship);

        /*
         * Email #2 is sent only after the Super Admin result has been
         * successfully committed. It is deliberately not sent from
         * DISTRIBUTION_COMPLETED or any earlier Scholarship stage.
         */
        scheduleScholarshipSuperAdminResultEmail(scholarship);
    }

    private void scheduleScholarshipSuperAdminResultEmail(
            ErpScholarshipApplication scholarship
    ) {
        if (scholarship == null
                || scholarship.getScholarshipAppId() == null
                || scholarship.getApplication() == null
                || scholarship.getApplication().getBranch() == null) {
            return;
        }

        Long scholarshipAppId =
                scholarship.getScholarshipAppId();

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            safelySendScholarshipSuperAdminResultEmail(
                    scholarshipAppId
            );
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        safelySendScholarshipSuperAdminResultEmail(
                                scholarshipAppId
                        );
                    }
                }
        );
    }

    private void safelySendScholarshipSuperAdminResultEmail(
            Long scholarshipAppId
    ) {
        try {
            ErpScholarshipApplication scholarship =
                    applicationRepo.findById(
                            scholarshipAppId
                    ).orElse(null);

            if (scholarship == null
                    || !Boolean.TRUE.equals(scholarship.getActive())
                    || scholarship.getApplication() == null
                    || scholarship.getApplication().getBranch() == null) {
                return;
            }

            ErpScholarshipHistory history =
                    historyOf(scholarship);

            String reviewStatus =
                    history.getSuperadminReviewStatus();

            String resultType =
                    "REJECTED".equalsIgnoreCase(reviewStatus)
                            ? "REJECTED"
                            : "ALLOCATION";

            BigDecimal allocatedAmount =
                    history.getApprovedAmount();

            String donorInformation =
                    buildScholarshipDonorInformation(
                            scholarship,
                            history
                    );

            String rejectionReason =
                    "REJECTED".equalsIgnoreCase(reviewStatus)
                            ? history.getSuperadminReviewRemarks()
                            : null;

            String studentName =
                    joinName(
                            scholarship.getApplication().getFirstName(),
                            scholarship.getApplication().getMiddleName(),
                            scholarship.getApplication().getLastName()
                    );

            String applicationNumber =
                    scholarship.getApplication().getApplicationNo();

            emailService.sendScholarshipSuperAdminResult(
                    scholarship.getApplication().getBranch(),
                    studentName,
                    applicationNumber,
                    resultType,
                    allocatedAmount,
                    donorInformation,
                    rejectionReason
            );

        } catch (RuntimeException ignored) {
            /*
             * The Super Admin decision is already committed. Email delivery
             * failure must never roll back or invalidate that decision.
             * EmailService owns the delivery logging.
             */
        }
    }

    private String buildScholarshipDonorInformation(
            ErpScholarshipApplication scholarship,
            ErpScholarshipHistory history
    ) {
        if (scholarship == null
                || history == null
                || scholarship.getBranchId() == null
                || scholarship.getStudent() == null
                || scholarship.getStudent().getStudentId() == null) {
            return null;
        }

        String academicYear =
                scholarship.getAcademicYear();

        String term =
                history.getTermRequested();

        if (academicYear == null
                || academicYear.isBlank()
                || term == null
                || term.isBlank()) {
            return null;
        }

        Long branchId =
                scholarship.getBranchId();

        Long studentId =
                scholarship.getStudent().getStudentId();

        Long historyId =
                history.getScholarshipHistoryId();

        List<ErpScholarshipAllocation> allocations =
                allocationRepo
                        .findAllByBranchIdAndAcademicYearAndTerm(
                                branchId,
                                academicYear,
                                term
                        )
                        .stream()
                        .filter(allocation ->
                                allocation != null
                                        && studentId.equals(
                                        allocation.getStudentId()
                                )
                                        && allocation.getScholarshipHistory() != null
                                        && historyId.equals(
                                        allocation.getScholarshipHistory()
                                                .getScholarshipHistoryId()
                                )
                        )
                        .toList();

        if (allocations.isEmpty()) {
            return "Branch Scholarship Fund allocation to be completed by Branch Admin.";
        }

        StringBuilder donorInformation =
                new StringBuilder();

        for (ErpScholarshipAllocation allocation : allocations) {
            if (donorInformation.length() > 0) {
                donorInformation.append("; ");
            }

            donorInformation
                    .append("Donor ID ")
                    .append(
                            allocation.getDonationId() != null
                                    ? allocation.getDonationId()
                                    : "General Fund"
                    )
                    .append(": UGX ")
                    .append(
                            allocation.getAllocatedAmountUgx() != null
                                    ? allocation.getAllocatedAmountUgx()
                                    : BigDecimal.ZERO
                    );
        }

        return donorInformation.toString();
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

    private ErpScholarshipHistory historyOf(
            ErpScholarshipApplication application
    ) {
        if (application == null
                || application.getScholarshipAppId() == null) {
            throw new BadRequestException(
                    "Scholarship history is not available for this application."
            );
        }

        return historyRepo
                .findFirstByScholarshipApplicationScholarshipAppIdOrderByScholarshipHistoryIdDesc(
                        application.getScholarshipAppId()
                )
                .orElseThrow(() ->
                        new BadRequestException(
                                "Scholarship history record not found."
                        )
                );
    }

    private void saveHistory(ErpScholarshipApplication application) {
        ErpScholarshipHistory history = historyOf(application);
        history.setUpdatedBy(currentUserId());
        historyRepo.save(history);
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


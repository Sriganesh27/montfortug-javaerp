package com.erp.montfortuganda.student.service;

import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.exception.DuplicateResourceException;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.student.dto.request.StudentCreateRequest;
import com.erp.montfortuganda.student.dto.request.StudentEnrollmentUpdateRequest;
import com.erp.montfortuganda.student.dto.request.StudentListFilterRequest;
import com.erp.montfortuganda.student.dto.request.StudentStatusChangeRequest;
import com.erp.montfortuganda.student.dto.request.StudentUpdateRequest;
import com.erp.montfortuganda.student.dto.response.PagedStudentResponse;
import com.erp.montfortuganda.student.dto.response.StudentAcademicHistoryResponse;
import com.erp.montfortuganda.student.dto.response.StudentCreateResponse;
import com.erp.montfortuganda.student.dto.response.StudentDocumentResponse;
import com.erp.montfortuganda.student.dto.response.StudentEnrollmentHistoryResponse;
import com.erp.montfortuganda.student.dto.response.StudentEnrollmentResponse;
import com.erp.montfortuganda.student.dto.response.StudentHostelResponse;
import com.erp.montfortuganda.student.dto.response.StudentMedicalResponse;
import com.erp.montfortuganda.student.dto.response.StudentParentResponse;
import com.erp.montfortuganda.student.dto.response.StudentPersonalResponse;
import com.erp.montfortuganda.student.dto.response.StudentProfileResponse;
import com.erp.montfortuganda.student.dto.response.StudentReferenceDataResponse;
import com.erp.montfortuganda.student.dto.response.StudentSummaryResponse;
import com.erp.montfortuganda.student.dto.response.StudentTransportResponse;
import com.erp.montfortuganda.student.entity.ErpParent;
import com.erp.montfortuganda.student.entity.ErpStudent;
import com.erp.montfortuganda.student.entity.ErpStudentAcademicHistory;
import com.erp.montfortuganda.student.entity.ErpStudentEnrollment;
import com.erp.montfortuganda.student.entity.ErpStudentEnrollmentHistory;
import com.erp.montfortuganda.student.entity.ErpStudentHostel;
import com.erp.montfortuganda.student.entity.ErpStudentMedical;
import com.erp.montfortuganda.student.entity.ErpStudentTransport;
import com.erp.montfortuganda.student.mapper.StudentMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Core Student business-service implementation.
 *
 * All Student operations are restricted to the authenticated user's branch.
 */
@Service
public class StudentServiceImpl implements StudentService {

    private static final int DEFAULT_PAGE_SIZE =
            20;

    private static final int MAX_PAGE_SIZE =
            100;

    private static final Map<String, String> ALLOWED_SORT_FIELDS =
            Map.ofEntries(
                    Map.entry(
                            "studentId",
                            "student.studentId"
                    ),
                    Map.entry(
                            "studentCode",
                            "student.studentCode"
                    ),
                    Map.entry(
                            "admissionNo",
                            "student.admissionNo"
                    ),
                    Map.entry(
                            "fullName",
                            "student.fullName"
                    ),
                    Map.entry(
                            "firstName",
                            "student.firstName"
                    ),
                    Map.entry(
                            "admissionYear",
                            "student.admissionYear"
                    ),
                    Map.entry(
                            "dateOfBirth",
                            "student.dateOfBirth"
                    ),
                    Map.entry(
                            "studentStatus",
                            "student.studentStatus"
                    ),
                    Map.entry(
                            "createdAt",
                            "student.createdAt"
                    )
            );

    private final StudentValidationService validationService;
    private final StudentNumberService numberService;
    private final StudentDocumentService documentService;
    private final StudentMapper studentMapper;
    private final EntityManager entityManager;

    public StudentServiceImpl(
            StudentValidationService validationService,
            StudentNumberService numberService,
            StudentDocumentService documentService,
            StudentMapper studentMapper,
            EntityManager entityManager
    ) {
        this.validationService = validationService;
        this.numberService = numberService;
        this.documentService = documentService;
        this.studentMapper = studentMapper;
        this.entityManager = entityManager;
    }

    // =====================================================================
    // CREATE
    // =====================================================================

    @Override
    @Transactional
    public StudentCreateResponse createStudent(
            StudentCreateRequest request
    ) {
        StudentValidationService.CreateReferences references =
                validationService.validateForCreate(
                        request
                );

        StudentValidationService.BranchContext branchContext =
                references.branchContext();

        Branch branch =
                branchContext.branch();

        Long authenticatedUserId =
                toLongUserId(
                        branchContext.userId()
                );

        ErpApplication application =
                resolveApplication(
                        request.applicationId()
                );

        String studentCode =
                numberService.generateStudentCode(
                        branch,
                        request.enrollment().academicYearId(),
                        branchContext.userId()
                );

        String admissionNo =
                numberService.generateAdmissionNumber(
                        branch,
                        branchContext.userId()
                );

        ErpStudent student =
                studentMapper.toNewStudent(
                        request.personal(),
                        branch,
                        application,
                        admissionNo,
                        studentCode,
                        authenticatedUserId
                );

        /*
         * Student must be persisted first because Parent, Enrollment,
         * Medical and Academic History all reference student_id.
         */
        entityManager.persist(
                student
        );

        entityManager.flush();

        ErpParent parent =
                studentMapper.toNewParent(
                        request.parent(),
                        student,
                        branch,
                        authenticatedUserId
                );

        entityManager.persist(
                parent
        );

        ErpStudentEnrollment enrollment =
                studentMapper.toNewEnrollment(
                        request.enrollment(),
                        student,
                        branch,
                        authenticatedUserId
                );

        entityManager.persist(
                enrollment
        );

        entityManager.flush();

        ErpStudentEnrollmentHistory initialHistory =
                studentMapper.toInitialEnrollmentHistory(
                        enrollment,
                        authenticatedUserId
                );

        entityManager.persist(
                initialHistory
        );

        if (
                studentMapper.hasMedicalData(
                        request.medical()
                )
        ) {
            ErpStudentMedical medical =
                    studentMapper.toNewMedical(
                            request.medical(),
                            student,
                            branch,
                            authenticatedUserId
                    );

            entityManager.persist(
                    medical
            );
        }

        if (
                studentMapper.hasAcademicHistoryData(
                        request.academicHistory()
                )
        ) {
            ErpStudentAcademicHistory academicHistory =
                    studentMapper.toNewAcademicHistory(
                            request.academicHistory(),
                            student,
                            branch,
                            authenticatedUserId
                    );

            entityManager.persist(
                    academicHistory
            );
        }

        if (request.hostel() != null) {
            ErpStudentHostel hostel =
                    studentMapper.toNewHostel(
                            request.hostel(),
                            student,
                            branch,
                            references.academicYearCode(),
                            authenticatedUserId
                    );

            entityManager.persist(
                    hostel
            );
        }

        if (request.transport() != null) {
            ErpStudentTransport transport =
                    studentMapper.toNewTransport(
                            request.transport(),
                            student,
                            branch,
                            references.academicYearCode(),
                            authenticatedUserId
                    );

            entityManager.persist(
                    transport
            );
        }

        linkApplicationToStudent(
                request.applicationId(),
                student,
                authenticatedUserId
        );

        entityManager.flush();

        entityManager.refresh(
                student
        );

        entityManager.refresh(
                enrollment
        );

        return studentMapper.toCreateResponse(
                student,
                enrollment
        );
    }

    // =====================================================================
    // PROFILE
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public StudentProfileResponse getStudentProfile(
            Long studentId
    ) {
        StudentValidationService.BranchContext branchContext =
                validationService.requireAuthenticatedBranch();

        Integer branchId =
                branchContext.branch()
                        .getBranchId();

        ErpStudent student =
                validationService.requireStudent(
                        studentId,
                        branchId
                );

        return buildStudentProfile(
                student,
                branchId
        );
    }

    // =====================================================================
    // LIST
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public PagedStudentResponse getStudents(
            StudentListFilterRequest filter,
            Pageable pageable
    ) {
        StudentValidationService.BranchContext branchContext =
                validationService.requireAuthenticatedBranch();

        Integer branchId =
                branchContext.branch()
                        .getBranchId();

        PageDetails pageDetails =
                resolvePageDetails(
                        pageable
                );

        QueryParts queryParts =
                buildStudentListQuery(
                        filter,
                        branchId,
                        pageable
                );

        Query countQuery =
                entityManager.createQuery(
                        "select count(distinct student.studentId) "
                                + queryParts.fromAndWhere()
                );

        applyParameters(
                countQuery,
                queryParts.parameters()
        );

        long totalElements =
                ((Number) countQuery.getSingleResult())
                        .longValue();

        Query studentQuery =
                entityManager.createQuery(
                        "select distinct student "
                                + queryParts.fromAndWhere()
                                + queryParts.orderBy(),
                        ErpStudent.class
                );

        applyParameters(
                studentQuery,
                queryParts.parameters()
        );

        studentQuery.setFirstResult(
                pageDetails.page()
                        * pageDetails.size()
        );

        studentQuery.setMaxResults(
                pageDetails.size()
        );

        @SuppressWarnings("unchecked")
        List<ErpStudent> students =
                studentQuery.getResultList();

        List<StudentSummaryResponse> responses =
                mapStudentSummaries(
                        students,
                        branchId
                );

        int totalPages =
                totalElements == 0
                        ? 0
                        : (int) (
                        (
                                totalElements
                                + pageDetails.size()
                                - 1
                        )
                        / pageDetails.size()
                );

        boolean first =
                pageDetails.page() == 0;

        boolean last =
                totalPages == 0
                        || pageDetails.page()
                        >= totalPages - 1;

        boolean hasNext =
                pageDetails.page() + 1
                        < totalPages;

        boolean hasPrevious =
                pageDetails.page() > 0;

        return new PagedStudentResponse(
                responses,
                pageDetails.page(),
                pageDetails.size(),
                totalElements,
                totalPages,
                first,
                last,
                hasNext,
                hasPrevious
        );
    }

    // =====================================================================
    // REFERENCE DATA
    // REFERENCE_DATA_IMPLEMENTATION_V1
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public StudentReferenceDataResponse getReferenceData() {
        StudentValidationService.BranchContext branchContext =
                validationService.requireAuthenticatedBranch();

        Integer branchId =
                branchContext.branch()
                        .getBranchId();

        // ================================================================
        // ACADEMIC YEARS
        // ================================================================

        @SuppressWarnings("unchecked")
        List<Object[]> academicYearRows =
                entityManager
                        .createNativeQuery(
                                """
                                select academic_year.academic_year_id,
                                       academic_year.academic_year_code,
                                       academic_year.academic_year_name,
                                       academic_year.start_date,
                                       academic_year.end_date,
                                       academic_year.status,
                                       academic_year.current_year
                                from erp_academic_years academic_year
                                where academic_year.active = 1
                                  and upper(academic_year.status) in ('PLANNED', 'ACTIVE')
                                order by academic_year.current_year desc,
                                         academic_year.start_date desc,
                                         academic_year.academic_year_id desc
                                """
                        )
                        .getResultList();

        List<StudentReferenceDataResponse.AcademicYearOption> academicYears =
                academicYearRows.stream()
                        .map(row ->
                                new StudentReferenceDataResponse.AcademicYearOption(
                                        referenceLong(row[0]),
                                        referenceText(row[1]),
                                        referenceText(row[2]),
                                        referenceDate(row[3]),
                                        referenceDate(row[4]),
                                        referenceText(row[5]),
                                        referenceBoolean(row[6])
                                )
                        )
                        .toList();

        // ================================================================
        // ACADEMIC TERMS
        // ================================================================

        @SuppressWarnings("unchecked")
        List<Object[]> academicTermRows =
                entityManager
                        .createNativeQuery(
                                """
                                select academic_term.term_id,
                                       academic_term.academic_year_id,
                                       academic_term.term_code,
                                       academic_term.term_name,
                                       academic_term.start_date,
                                       academic_term.end_date,
                                       academic_term.display_order,
                                       academic_term.status,
                                       academic_term.current_term
                                from erp_academic_terms academic_term
                                join erp_academic_years academic_year
                                  on academic_year.academic_year_id =
                                     academic_term.academic_year_id
                                where academic_term.active = 1
                                  and academic_year.active = 1
                                  and upper(academic_year.status) in ('PLANNED', 'ACTIVE')
                                order by academic_year.current_year desc,
                                         academic_year.start_date desc,
                                         academic_term.display_order asc,
                                         academic_term.term_id asc
                                """
                        )
                        .getResultList();

        List<StudentReferenceDataResponse.AcademicTermOption> academicTerms =
                academicTermRows.stream()
                        .map(row ->
                                new StudentReferenceDataResponse.AcademicTermOption(
                                        referenceLong(row[0]),
                                        referenceLong(row[1]),
                                        referenceText(row[2]),
                                        referenceText(row[3]),
                                        referenceDate(row[4]),
                                        referenceDate(row[5]),
                                        referenceInteger(row[6]),
                                        referenceText(row[7]),
                                        referenceBoolean(row[8])
                                )
                        )
                        .toList();

        // ================================================================
        // BRANCH LEVELS
        // ================================================================

        @SuppressWarnings("unchecked")
        List<Object[]> levelRows =
                entityManager
                        .createNativeQuery(
                                """
                                select distinct level.level_id,
                                                level.level_name,
                                                level.display_order
                                from erp_levels level
                                join erp_branch_levels branch_level
                                  on branch_level.level_id = level.level_id
                                where branch_level.branch_id = :branchId
                                  and level.status = 1
                                order by level.display_order asc,
                                         level.level_name asc
                                """
                        )
                        .setParameter(
                                "branchId",
                                branchId
                        )
                        .getResultList();

        List<StudentReferenceDataResponse.LevelOption> levels =
                levelRows.stream()
                        .map(row ->
                                new StudentReferenceDataResponse.LevelOption(
                                        referenceInteger(row[0]),
                                        referenceText(row[1]),
                                        referenceInteger(row[2])
                                )
                        )
                        .toList();

        // ================================================================
        // BRANCH CLASSES
        // ================================================================

        @SuppressWarnings("unchecked")
        List<Object[]> classRows =
                entityManager
                        .createNativeQuery(
                                """
                                select distinct school_class.class_id,
                                                school_class.level_id,
                                                school_class.class_code,
                                                school_class.class_name,
                                                school_class.display_order
                                from erp_classes school_class
                                join erp_branch_levels branch_level
                                  on branch_level.level_id = school_class.level_id
                                where branch_level.branch_id = :branchId
                                  and school_class.status = 1
                                order by school_class.display_order asc,
                                         school_class.class_name asc
                                """
                        )
                        .setParameter(
                                "branchId",
                                branchId
                        )
                        .getResultList();

        List<StudentReferenceDataResponse.ClassOption> classes =
                classRows.stream()
                        .map(row ->
                                new StudentReferenceDataResponse.ClassOption(
                                        referenceInteger(row[0]),
                                        referenceInteger(row[1]),
                                        referenceText(row[2]),
                                        referenceText(row[3]),
                                        referenceInteger(row[4])
                                )
                        )
                        .toList();

        // ================================================================
        // BRANCH SECTIONS
        // ================================================================

        @SuppressWarnings("unchecked")
        List<Object[]> sectionRows =
                entityManager
                        .createNativeQuery(
                                """
                                select section.section_id,
                                       section.branch_id,
                                       section.academic_year_id,
                                       section.class_id,
                                       section.section_code,
                                       section.section_name,
                                       section.capacity
                                from erp_sections section
                                join erp_academic_years academic_year
                                  on academic_year.academic_year_id =
                                     section.academic_year_id
                                where section.branch_id = :branchId
                                  and section.active = 1
                                  and upper(section.status) = 'ACTIVE'
                                  and academic_year.active = 1
                                  and upper(academic_year.status) in ('PLANNED', 'ACTIVE')
                                order by academic_year.current_year desc,
                                         academic_year.start_date desc,
                                         section.class_id asc,
                                         section.section_code asc,
                                         section.section_id asc
                                """
                        )
                        .setParameter(
                                "branchId",
                                branchId
                        )
                        .getResultList();

        List<StudentReferenceDataResponse.SectionOption> sections =
                sectionRows.stream()
                        .map(row ->
                                new StudentReferenceDataResponse.SectionOption(
                                        referenceLong(row[0]),
                                        referenceInteger(row[1]),
                                        referenceLong(row[2]),
                                        referenceInteger(row[3]),
                                        referenceText(row[4]),
                                        referenceText(row[5]),
                                        referenceInteger(row[6])
                                )
                        )
                        .toList();

        return new StudentReferenceDataResponse(
                academicYears,
                academicTerms,
                levels,
                classes,
                sections
        );
    }

    // =====================================================================
    // UPDATE STUDENT
    // =====================================================================

    @Override
    @Transactional
    public StudentProfileResponse updateStudent(
            Long studentId,
            StudentUpdateRequest request
    ) {
        StudentValidationService.UpdateReferences references =
                validationService.validateForUpdate(
                        studentId,
                        request
                );

        StudentValidationService.BranchContext branchContext =
                references.branchContext();

        Branch branch =
                branchContext.branch();

        Integer branchId =
                branch.getBranchId();

        Long authenticatedUserId =
                toLongUserId(
                        branchContext.userId()
                );

        ErpStudent student =
                references.student();

        studentMapper.updateStudent(
                request.personal(),
                student
        );

        ErpParent parent =
                findParent(
                        studentId,
                        branchId
                );

        if (parent == null) {
            parent =
                    studentMapper.toNewParent(
                            request.parent(),
                            student,
                            branch,
                            authenticatedUserId
                    );

            entityManager.persist(
                    parent
            );
        } else {
            studentMapper.updateParent(
                    request.parent(),
                    parent,
                    authenticatedUserId
            );
        }

        updateMedicalSection(
                request,
                student,
                branch,
                authenticatedUserId
        );

        updateAcademicHistorySection(
                request,
                student,
                branch,
                authenticatedUserId
        );

        entityManager.flush();

        entityManager.refresh(
                student
        );

        return buildStudentProfile(
                student,
                branchId
        );
    }

    // =====================================================================
    // UPDATE ENROLLMENT
    // =====================================================================

    @Override
    @Transactional
    public StudentEnrollmentResponse updateEnrollment(
            Long studentId,
            StudentEnrollmentUpdateRequest request
    ) {
        StudentValidationService.EnrollmentUpdateReferences references =
                validationService.validateForEnrollmentUpdate(
                        studentId,
                        request
                );

        StudentValidationService.BranchContext branchContext =
                references.branchContext();

        Long authenticatedUserId =
                toLongUserId(
                        branchContext.userId()
                );

        ErpStudentEnrollment enrollment =
                references.enrollment();

        /*
         * Preserve the previous placement before modifying the current
         * enrollment record.
         */
        ErpStudentEnrollmentHistory history =
                studentMapper.toEnrollmentHistory(
                        enrollment,
                        request,
                        authenticatedUserId
                );

        entityManager.persist(
                history
        );

        studentMapper.updateEnrollment(
                request,
                enrollment
        );

        enrollment.setApprovedBy(
                authenticatedUserId
        );

        enrollment.setApprovedAt(
                LocalDateTime.now()
        );

        entityManager.flush();

        entityManager.refresh(
                enrollment
        );

        PlacementNames names =
                resolvePlacementNames(
                        enrollment
                );

        return studentMapper.toEnrollmentResponse(
                enrollment,
                names.academicYearName(),
                names.className(),
                names.sectionName(),
                names.streamName(),
                resolveEmployeeName(
                        enrollment.getClassTeacherId()
                )
        );
    }

    // =====================================================================
    // STATUS CHANGE
    // =====================================================================

    @Override
    @Transactional
    public StudentPersonalResponse changeStudentStatus(
            Long studentId,
            StudentStatusChangeRequest request
    ) {
        StudentValidationService.StatusChangeReferences references =
                validationService.validateForStatusChange(
                        studentId,
                        request
                );

        ErpStudent student =
                references.student();

        studentMapper.applyStudentStatus(
                student,
                request.newStatus()
        );

        entityManager.flush();

        entityManager.refresh(
                student
        );

        /*
         * The reason and effective date are validated by the validation
         * service. A dedicated Student status-history table can later persist
         * those details without changing this API contract.
         */
        return studentMapper.toPersonalResponse(
                student
        );
    }

    // =====================================================================
    // PROFILE ASSEMBLY
    // =====================================================================

    private StudentProfileResponse buildStudentProfile(
            ErpStudent student,
            Integer branchId
    ) {
        Long studentId =
                student.getStudentId();

        ErpParent parent =
                findParent(
                        studentId,
                        branchId
                );

        ErpStudentEnrollment enrollment =
                findEnrollment(
                        studentId,
                        branchId
                );

        ErpStudentMedical medical =
                findMedical(
                        studentId,
                        branchId
                );

        ErpStudentAcademicHistory academicHistory =
                findAcademicHistory(
                        studentId,
                        branchId
                );

        ErpStudentHostel hostel =
                findHostel(
                        studentId,
                        branchId
                );

        ErpStudentTransport transport =
                findTransport(
                        studentId,
                        branchId
                );

        List<ErpStudentEnrollmentHistory> historyEntities =
                findEnrollmentHistory(
                        studentId,
                        branchId
                );

        StudentPersonalResponse personalResponse =
                studentMapper.toPersonalResponse(
                        student
                );

        StudentParentResponse parentResponse =
                studentMapper.toParentResponse(
                        parent
                );

        StudentEnrollmentResponse enrollmentResponse =
                null;

        if (enrollment != null) {
            PlacementNames names =
                    resolvePlacementNames(
                            enrollment
                    );

            enrollmentResponse =
                    studentMapper.toEnrollmentResponse(
                            enrollment,
                            names.academicYearName(),
                            names.className(),
                            names.sectionName(),
                            names.streamName(),
                            resolveEmployeeName(
                                    enrollment.getClassTeacherId()
                            )
                    );
        }

        List<StudentEnrollmentHistoryResponse> historyResponses =
                historyEntities
                        .stream()
                        .map(history -> {
                            PlacementNames names =
                                    resolvePlacementNames(
                                            history.getAcademicYearId(),
                                            history.getClassId(),
                                            history.getSectionId()
                                    );
                            return studentMapper
                                    .toEnrollmentHistoryResponse(
                                            history,
                                            names.academicYearName(),
                                            names.className(),
                                            names.sectionName(),
                                            names.streamName(),
                                            resolveUsername(
                                                    history.getApprovedBy()
                                            ),
                                            resolveUsername(
                                                    history.getCreatedBy()
                                            )
                                    );
                        })
                        .toList();

        StudentMedicalResponse medicalResponse =
                studentMapper.toMedicalResponse(
                        medical
                );

        StudentAcademicHistoryResponse academicHistoryResponse =
                studentMapper.toAcademicHistoryResponse(
                        academicHistory,
                        academicHistory != null
                                ? resolveUsername(
                                academicHistory.getVerifiedBy()
                        )
                                : null
                );

        StudentHostelResponse hostelResponse =
                studentMapper.toHostelResponse(
                        hostel,
                        null,
                        null,
                        null
                );

        StudentTransportResponse transportResponse =
                studentMapper.toTransportResponse(
                        transport,
                        null,
                        null,
                        null
                );

        List<StudentDocumentResponse> documentResponses =
                documentService.getDocumentsForProfile(
                        studentId,
                        branchId
                );

        return studentMapper.toProfileResponse(
                personalResponse,
                parentResponse,
                enrollmentResponse,
                historyResponses,
                medicalResponse,
                academicHistoryResponse,
                hostelResponse,
                transportResponse,
                documentResponses
        );
    }

    // =====================================================================
    // OPTIONAL UPDATE SECTIONS
    // =====================================================================

    private void updateMedicalSection(
            StudentUpdateRequest request,
            ErpStudent student,
            Branch branch,
            Long authenticatedUserId
    ) {
        if (
                request.medical() == null
                        || !studentMapper.hasMedicalData(
                        request.medical()
                )
        ) {
            return;
        }

        ErpStudentMedical medical =
                findMedical(
                        student.getStudentId(),
                        branch.getBranchId()
                );

        if (medical == null) {
            medical =
                    studentMapper.toNewMedical(
                            request.medical(),
                            student,
                            branch,
                            authenticatedUserId
                    );

            entityManager.persist(
                    medical
            );
        } else {
            studentMapper.updateMedical(
                    request.medical(),
                    medical,
                    authenticatedUserId
            );
        }
    }

    private void updateAcademicHistorySection(
            StudentUpdateRequest request,
            ErpStudent student,
            Branch branch,
            Long authenticatedUserId
    ) {
        if (
                request.academicHistory() == null
                        || !studentMapper
                        .hasAcademicHistoryData(
                                request.academicHistory()
                        )
        ) {
            return;
        }

        ErpStudentAcademicHistory academicHistory =
                findAcademicHistory(
                        student.getStudentId(),
                        branch.getBranchId()
                );

        if (academicHistory == null) {
            academicHistory =
                    studentMapper.toNewAcademicHistory(
                            request.academicHistory(),
                            student,
                            branch,
                            authenticatedUserId
                    );

            entityManager.persist(
                    academicHistory
            );
        } else {
            studentMapper.updateAcademicHistory(
                    request.academicHistory(),
                    academicHistory
            );
        }
    }

    // =====================================================================
    // APPLICATION CONVERSION
    // =====================================================================

    private ErpApplication resolveApplication(
            Long applicationId
    ) {
        if (applicationId == null) {
            return null;
        }

        return entityManager.find(
                ErpApplication.class,
                applicationId
        );
    }

    private void linkApplicationToStudent(
            Long applicationId,
            ErpStudent student,
            Long authenticatedUserId
    ) {
        if (applicationId == null) {
            return;
        }

        int updatedRows =
                entityManager
                        .createNativeQuery(
                                """
                                update erp_applications
                                set student_id = :studentId,
                                    student_created = 1,
                                    updated_by = :updatedBy,
                                    updated_at = current_timestamp
                                where application_id = :applicationId
                                  and student_created = 0
                                """
                        )
                        .setParameter(
                                "studentId",
                                student.getStudentId()
                        )
                        .setParameter(
                                "updatedBy",
                                authenticatedUserId
                        )
                        .setParameter(
                                "applicationId",
                                applicationId
                        )
                        .executeUpdate();

        if (updatedRows != 1) {
            throw new DuplicateResourceException(
                    "The admission application has already been converted into a Student."
            );
        }
    }

    // =====================================================================
    // LIST QUERY
    // =====================================================================

    private QueryParts buildStudentListQuery(
            StudentListFilterRequest filter,
            Integer branchId,
            Pageable pageable
    ) {
        StringBuilder fromAndWhere =
                new StringBuilder(
                        """
                        from ErpStudent student
                        left join ErpStudentEnrollment enrollment
                               on enrollment.student = student
                        where student.branch.branchId = :branchId
                        """
                );

        Map<String, Object> parameters =
                new LinkedHashMap<>();

        parameters.put(
                "branchId",
                branchId
        );

        if (filter != null) {
            addTextFilter(
                    fromAndWhere,
                    parameters,
                    "student.studentCode",
                    "studentCode",
                    filter.studentCode()
            );

            addTextFilter(
                    fromAndWhere,
                    parameters,
                    "student.admissionNo",
                    "admissionNo",
                    filter.admissionNo()
            );

            addTextFilter(
                    fromAndWhere,
                    parameters,
                    "student.learnerLin",
                    "learnerLin",
                    filter.learnerLin()
            );

            if (
                    StringUtils.hasText(
                            filter.studentName()
                    )
            ) {
                fromAndWhere.append(
                        """
                         and lower(student.fullName)
                             like :studentName
                        """
                );

                parameters.put(
                        "studentName",
                        "%"
                                + filter.studentName()
                                .trim()
                                .toLowerCase(Locale.ROOT)
                                + "%"
                );
            }

            if (filter.admissionYear() != null) {
                fromAndWhere.append(
                        " and student.admissionYear = :admissionYear"
                );

                parameters.put(
                        "admissionYear",
                        filter.admissionYear()
                );
            }

            if (filter.academicYearId() != null) {
                fromAndWhere.append(
                        " and enrollment.academicYearId = :academicYearId"
                );

                parameters.put(
                        "academicYearId",
                        filter.academicYearId()
                );
            }

            if (filter.classId() != null) {
                fromAndWhere.append(
                        " and enrollment.classId = :classId"
                );

                parameters.put(
                        "classId",
                        filter.classId()
                );
            }

            if (filter.sectionId() != null) {
                fromAndWhere.append(
                        " and enrollment.sectionId = :sectionId"
                );

                parameters.put(
                        "sectionId",
                        filter.sectionId()
                );
            }

            if (filter.gender() != null) {
                fromAndWhere.append(
                        " and student.gender = :gender"
                );

                parameters.put(
                        "gender",
                        filter.gender().name()
                );
            }

            if (filter.studentStatus() != null) {
                fromAndWhere.append(
                        " and student.studentStatus = :studentStatus"
                );

                parameters.put(
                        "studentStatus",
                        filter.studentStatus().name()
                );
            }

            if (filter.enrollmentStatus() != null) {
                fromAndWhere.append(
                        " and enrollment.enrollmentStatus = :enrollmentStatus"
                );

                parameters.put(
                        "enrollmentStatus",
                        filter.enrollmentStatus()
                );
            }

            if (filter.active() != null) {
                fromAndWhere.append(
                        " and student.active = :active"
                );

                parameters.put(
                        "active",
                        filter.active()
                );
            }
        }

        return new QueryParts(
                fromAndWhere.toString(),
                buildOrderBy(pageable),
                parameters
        );
    }

    private void addTextFilter(
            StringBuilder query,
            Map<String, Object> parameters,
            String property,
            String parameterName,
            String value
    ) {
        if (!StringUtils.hasText(value)) {
            return;
        }

        query.append(" and upper(trim(")
                .append(property)
                .append(")) = upper(trim(:")
                .append(parameterName)
                .append("))");

        parameters.put(
                parameterName,
                value.trim()
        );
    }

    private String buildOrderBy(
            Pageable pageable
    ) {
        if (
                pageable == null
                        || pageable.getSort().isUnsorted()
        ) {
            return """
                     order by student.fullName asc,
                              student.studentId desc
                    """;
        }

        List<String> orderParts =
                new ArrayList<>();

        for (Sort.Order order : pageable.getSort()) {
            String property =
                    ALLOWED_SORT_FIELDS.get(
                            order.getProperty()
                    );

            if (property == null) {
                throw new BadRequestException(
                        "Unsupported Student sort field: "
                                + order.getProperty()
                                + "."
                );
            }

            orderParts.add(
                    property
                            + (
                            order.isAscending()
                                    ? " asc"
                                    : " desc"
                    )
            );
        }

        if (orderParts.isEmpty()) {
            return " order by student.fullName asc";
        }

        return " order by "
                + String.join(
                ", ",
                orderParts
        );
    }

    private PageDetails resolvePageDetails(
            Pageable pageable
    ) {
        if (
                pageable == null
                        || pageable.isUnpaged()
        ) {
            return new PageDetails(
                    0,
                    DEFAULT_PAGE_SIZE
            );
        }

        int page =
                Math.max(
                        pageable.getPageNumber(),
                        0
                );

        int size =
                Math.clamp(
                        pageable.getPageSize(),
                        1,
                        MAX_PAGE_SIZE
                );

        return new PageDetails(
                page,
                size
        );
    }

    private void applyParameters(
            Query query,
            Map<String, Object> parameters
    ) {
        for (
                Map.Entry<String, Object> parameter
                : parameters.entrySet()
        ) {
            query.setParameter(
                    parameter.getKey(),
                    parameter.getValue()
            );
        }
    }

    // =====================================================================
    // LIST BATCH MAPPING
    // =====================================================================

    private List<StudentSummaryResponse> mapStudentSummaries(
            List<ErpStudent> students,
            Integer branchId
    ) {
        if (students.isEmpty()) {
            return List.of();
        }

        List<Long> studentIds =
                students.stream()
                        .map(ErpStudent::getStudentId)
                        .filter(Objects::nonNull)
                        .toList();

        Map<Long, ErpStudentEnrollment> enrollments =
                findEnrollmentMap(
                        studentIds,
                        branchId
                );

        Map<Long, ErpParent> parents =
                findParentMap(
                        studentIds,
                        branchId
                );

        List<Long> academicYearIds =
                enrollments.values()
                        .stream()
                        .map(
                                ErpStudentEnrollment
                                        ::getAcademicYearId
                        )
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();

        List<Integer> classIds =
                enrollments.values()
                        .stream()
                        .map(
                                ErpStudentEnrollment
                                        ::getClassId
                        )
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();

        List<Long> sectionIds =
                enrollments.values()
                        .stream()
                        .map(
                                ErpStudentEnrollment
                                        ::getSectionId
                        )
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();

        Map<Long, String> academicYearNames =
                fetchNameMap(
                        "erp_academic_years",
                        "academic_year_id",
                        "academic_year_name",
                        academicYearIds
                );

        Map<Long, String> classNames =
                fetchNameMap(
                        "erp_classes",
                        "class_id",
                        "class_name",
                        classIds
                );

        Map<Long, String> sectionNames =
                fetchNameMap(
                        "erp_sections",
                        "section_id",
                        "section_name",
                        sectionIds
                );

        List<StudentSummaryResponse> responses =
                new ArrayList<>(
                        students.size()
                );

        for (ErpStudent student : students) {
            ErpStudentEnrollment enrollment =
                    enrollments.get(
                            student.getStudentId()
                    );

            ErpParent parent =
                    parents.get(
                            student.getStudentId()
                    );

            responses.add(
                    studentMapper.toSummaryResponse(
                            student,
                            enrollment,
                            parent,
                            enrollment != null
                                    ? academicYearNames.get(
                                    enrollment.getAcademicYearId()
                            )
                                    : null,
                            enrollment != null
                                    && enrollment.getClassId() != null
                                    ? classNames.get(
                                    enrollment.getClassId()
                                            .longValue()
                            )
                                    : null,
                            enrollment != null
                                    ? sectionNames.get(
                                    enrollment.getSectionId()
                            )
                                    : null
                    )
            );
        }

        return List.copyOf(
                responses
        );
    }

    private Map<Long, ErpStudentEnrollment> findEnrollmentMap(
            List<Long> studentIds,
            Integer branchId
    ) {
        List<ErpStudentEnrollment> enrollments =
                entityManager
                        .createQuery(
                                """
                                select enrollment
                                from ErpStudentEnrollment enrollment
                                where enrollment.student.studentId
                                      in :studentIds
                                  and enrollment.branch.branchId = :branchId
                                """,
                                ErpStudentEnrollment.class
                        )
                        .setParameter(
                                "studentIds",
                                studentIds
                        )
                        .setParameter(
                                "branchId",
                                branchId
                        )
                        .getResultList();

        Map<Long, ErpStudentEnrollment> mapped =
                new LinkedHashMap<>();

        for (ErpStudentEnrollment enrollment : enrollments) {
            if (
                    enrollment.getStudent() != null
                            && enrollment.getStudent()
                            .getStudentId() != null
            ) {
                mapped.put(
                        enrollment.getStudent()
                                .getStudentId(),
                        enrollment
                );
            }
        }

        return mapped;
    }

    private Map<Long, ErpParent> findParentMap(
            List<Long> studentIds,
            Integer branchId
    ) {
        List<ErpParent> parents =
                entityManager
                        .createQuery(
                                """
                                select parent
                                from ErpParent parent
                                where parent.student.studentId in :studentIds
                                  and parent.branch.branchId = :branchId
                                  and parent.active = true
                                """,
                                ErpParent.class
                        )
                        .setParameter(
                                "studentIds",
                                studentIds
                        )
                        .setParameter(
                                "branchId",
                                branchId
                        )
                        .getResultList();

        Map<Long, ErpParent> mapped =
                new LinkedHashMap<>();

        for (ErpParent parent : parents) {
            if (
                    parent.getStudent() != null
                            && parent.getStudent()
                            .getStudentId() != null
            ) {
                mapped.put(
                        parent.getStudent()
                                .getStudentId(),
                        parent
                );
            }
        }

        return mapped;
    }

    // =====================================================================
    // ENTITY LOOKUPS
    // =====================================================================

    private ErpParent findParent(
            Long studentId,
            Integer branchId
    ) {
        return entityManager
                .createQuery(
                        """
                        select parent
                        from ErpParent parent
                        where parent.student.studentId = :studentId
                          and parent.branch.branchId = :branchId
                          and parent.active = true
                        """,
                        ErpParent.class
                )
                .setParameter(
                        "studentId",
                        studentId
                )
                .setParameter(
                        "branchId",
                        branchId
                )
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    private ErpStudentEnrollment findEnrollment(
            Long studentId,
            Integer branchId
    ) {
        return entityManager
                .createQuery(
                        """
                        select enrollment
                        from ErpStudentEnrollment enrollment
                        where enrollment.student.studentId = :studentId
                          and enrollment.branch.branchId = :branchId
                        """,
                        ErpStudentEnrollment.class
                )
                .setParameter(
                        "studentId",
                        studentId
                )
                .setParameter(
                        "branchId",
                        branchId
                )
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    private ErpStudentMedical findMedical(
            Long studentId,
            Integer branchId
    ) {
        return entityManager
                .createQuery(
                        """
                        select medical
                        from ErpStudentMedical medical
                        where medical.student.studentId = :studentId
                          and medical.branch.branchId = :branchId
                          and medical.active = true
                        """,
                        ErpStudentMedical.class
                )
                .setParameter(
                        "studentId",
                        studentId
                )
                .setParameter(
                        "branchId",
                        branchId
                )
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    private ErpStudentAcademicHistory findAcademicHistory(
            Long studentId,
            Integer branchId
    ) {
        return entityManager
                .createQuery(
                        """
                        select history
                        from ErpStudentAcademicHistory history
                        where history.student.studentId = :studentId
                          and history.branch.branchId = :branchId
                          and history.active = true
                        """,
                        ErpStudentAcademicHistory.class
                )
                .setParameter(
                        "studentId",
                        studentId
                )
                .setParameter(
                        "branchId",
                        branchId
                )
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    private ErpStudentHostel findHostel(
            Long studentId,
            Integer branchId
    ) {
        return entityManager
                .createQuery(
                        """
                        select hostel
                        from ErpStudentHostel hostel
                        where hostel.student.studentId = :studentId
                          and hostel.branch.branchId = :branchId
                          and hostel.active = true
                        """,
                        ErpStudentHostel.class
                )
                .setParameter(
                        "studentId",
                        studentId
                )
                .setParameter(
                        "branchId",
                        branchId
                )
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    private ErpStudentTransport findTransport(
            Long studentId,
            Integer branchId
    ) {
        return entityManager
                .createQuery(
                        """
                        select transport
                        from ErpStudentTransport transport
                        where transport.student.studentId = :studentId
                          and transport.branch.branchId = :branchId
                          and transport.active = true
                        """,
                        ErpStudentTransport.class
                )
                .setParameter(
                        "studentId",
                        studentId
                )
                .setParameter(
                        "branchId",
                        branchId
                )
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    private List<ErpStudentEnrollmentHistory> findEnrollmentHistory(
            Long studentId,
            Integer branchId
    ) {
        return entityManager
                .createQuery(
                        """
                        select history
                        from ErpStudentEnrollmentHistory history
                        where history.student.studentId = :studentId
                          and history.branch.branchId = :branchId
                        order by history.effectiveDate desc,
                                 history.createdAt desc
                        """,
                        ErpStudentEnrollmentHistory.class
                )
                .setParameter(
                        "studentId",
                        studentId
                )
                .setParameter(
                        "branchId",
                        branchId
                )
                .getResultList();
    }

    // =====================================================================
    // MASTER-DATA NAMES
    // =====================================================================

    private PlacementNames resolvePlacementNames(
            ErpStudentEnrollment enrollment
    ) {
        return resolvePlacementNames(
                enrollment.getAcademicYearId(),
                enrollment.getClassId(),
                enrollment.getSectionId()
        );
    }

    private PlacementNames resolvePlacementNames(
            Long academicYearId,
            Integer classId,
            Long sectionId
    ) {
        return new PlacementNames(
                resolveName(
                        "erp_academic_years",
                        "academic_year_id",
                        "academic_year_name",
                        academicYearId
                ),
                resolveName(
                        "erp_classes",
                        "class_id",
                        "class_name",
                        classId
                ),
                resolveName(
                        "erp_sections",
                        "section_id",
                        "section_name",
                        sectionId
                ),
                null
        );
    }

    private String resolveEmployeeName(
            Long employeeId
    ) {
        return resolveName(
                "erp_employees",
                "employee_id",
                "full_name",
                employeeId
        );
    }

    private String resolveUsername(
            Long userId
    ) {
        return resolveName(
                "erp_users",
                "id",
                "username",
                userId
        );
    }

    private String resolveName(
            String tableName,
            String idColumn,
            String nameColumn,
            Number id
    ) {
        if (id == null) {
            return null;
        }

        String sql =
                "select "
                        + nameColumn
                        + " from "
                        + tableName
                        + " where "
                        + idColumn
                        + " = :id";

        List<?> results =
                entityManager
                        .createNativeQuery(sql)
                        .setParameter(
                                "id",
                                id
                        )
                        .setMaxResults(1)
                        .getResultList();

        if (results.isEmpty()) {
            return null;
        }

        String value =
                Objects.toString(
                        results.getFirst(),
                        null
                );

        return StringUtils.hasText(value)
                ? value.trim()
                : null;
    }

    private Map<Long, String> fetchNameMap(
            String tableName,
            String idColumn,
            String nameColumn,
            Collection<? extends Number> ids
    ) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }

        String sql =
                "select "
                        + idColumn
                        + ", "
                        + nameColumn
                        + " from "
                        + tableName
                        + " where "
                        + idColumn
                        + " in (:ids)";

        @SuppressWarnings("unchecked")
        List<Object[]> results =
                entityManager
                        .createNativeQuery(sql)
                        .setParameter(
                                "ids",
                                ids
                        )
                        .getResultList();

        Map<Long, String> mapped =
                new LinkedHashMap<>();

        for (Object[] row : results) {
            if (
                    row.length < 2
                            || !(row[0] instanceof Number id)
            ) {
                continue;
            }

            String name =
                    Objects.toString(
                            row[1],
                            null
                    );

            mapped.put(
                    id.longValue(),
                    name
            );
        }

        return mapped;
    }

    // =====================================================================
    // HELPERS
    // =====================================================================

    private Long referenceLong(
            Object value
    ) {
        return value instanceof Number number
                ? number.longValue()
                : null;
    }

    private Integer referenceInteger(
            Object value
    ) {
        return value instanceof Number number
                ? number.intValue()
                : null;
    }

    private Boolean referenceBoolean(
            Object value
    ) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }

        if (value instanceof Number number) {
            return number.intValue() != 0;
        }

        if (value instanceof String text) {
            String normalized =
                    text.trim();

            return "1".equals(normalized)
                    || "true".equalsIgnoreCase(normalized)
                    || "yes".equalsIgnoreCase(normalized);
        }

        return false;
    }

    private String referenceText(
            Object value
    ) {
        if (value == null) {
            return null;
        }

        String text =
                value.toString()
                        .trim();

        return text.isEmpty()
                ? null
                : text;
    }

    private java.time.LocalDate referenceDate(
            Object value
    ) {
        if (value instanceof java.time.LocalDate localDate) {
            return localDate;
        }

        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }

        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp
                    .toLocalDateTime()
                    .toLocalDate();
        }

        if (value instanceof java.time.LocalDateTime localDateTime) {
            return localDateTime.toLocalDate();
        }

        if (value instanceof String text) {
            try {
                return java.time.LocalDate.parse(
                        text.trim()
                );
            } catch (java.time.format.DateTimeParseException ignored) {
                return null;
            }
        }

        return null;
    }

    private Long toLongUserId(
            Integer userId
    ) {
        if (userId == null || userId <= 0) {
            throw new BadRequestException(
                    "Authenticated user ID is unavailable."
            );
        }

        return userId.longValue();
    }

    // =====================================================================
    // INTERNAL RECORDS
    // =====================================================================

    private record PageDetails(
            int page,
            int size
    ) {
    }

    private record QueryParts(
            String fromAndWhere,
            String orderBy,
            Map<String, Object> parameters
    ) {
    }

    private record PlacementNames(
            String academicYearName,
            String className,
            String sectionName,
            String streamName
    ) {
    }
}
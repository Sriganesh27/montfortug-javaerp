package com.erp.montfortuganda.admission.service;

import com.erp.montfortuganda.admission.dto.ApplicationInterviewCompleteRequestDTO;
import com.erp.montfortuganda.admission.dto.ApplicationInterviewMarkRequestDTO;
import com.erp.montfortuganda.admission.dto.ApplicationInterviewResponseDTO;
import com.erp.montfortuganda.admission.dto.ApplicationInterviewScheduleRequestDTO;
import com.erp.montfortuganda.admission.dto.ApplicationInterviewWaitlistResultRequestDTO;
import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.admission.entity.ErpApplicationInterview;
import com.erp.montfortuganda.admission.entity.ErpApplicationInterviewMark;
import com.erp.montfortuganda.admission.entity.ErpApplicationStatusHistory;
import com.erp.montfortuganda.admission.repository.ErpApplicationInterviewMarkRepository;
import com.erp.montfortuganda.admission.repository.ErpApplicationInterviewRepository;
import com.erp.montfortuganda.admission.repository.ErpApplicationRepository;
import com.erp.montfortuganda.admission.repository.ErpApplicationStatusHistoryRepository;
import com.erp.montfortuganda.auth.service.BranchAccessService;
import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.employee.entity.ErpEmployee;
import com.erp.montfortuganda.employee.enums.EmploymentStatus;
import com.erp.montfortuganda.employee.repository.ErpEmployeeRepository;
import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.school.entity.ErpSubject;
import com.erp.montfortuganda.school.repository.ErpSubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Branch-safe Entrance Test workflow.
 *
 * <p>The interview row stores workflow/scheduling/result state. Subject-wise
 * marks are stored only in erp_application_interview_marks. Overall totals are
 * derived when the response is built and are never duplicated in the interview
 * row.</p>
 */
@Service
@Transactional(readOnly = true)
public class ApplicationInterviewServiceImpl
        implements ApplicationInterviewService {

    private final ErpApplicationRepository applicationRepository;
    private final ErpApplicationInterviewRepository interviewRepository;
    private final ErpApplicationInterviewMarkRepository markRepository;
    private final ErpEmployeeRepository employeeRepository;
    private final BranchAccessService branchAccessService;
    private final ErpSubjectRepository subjectRepository;
    private final ErpApplicationStatusHistoryRepository historyRepository;

    public ApplicationInterviewServiceImpl(
            ErpApplicationRepository applicationRepository,
            ErpApplicationInterviewRepository interviewRepository,
            ErpApplicationInterviewMarkRepository markRepository,
            ErpEmployeeRepository employeeRepository,
            BranchAccessService branchAccessService,
            ErpSubjectRepository subjectRepository,
            ErpApplicationStatusHistoryRepository historyRepository
    ) {
        this.applicationRepository = applicationRepository;
        this.interviewRepository = interviewRepository;
        this.markRepository = markRepository;
        this.employeeRepository = employeeRepository;
        this.branchAccessService = branchAccessService;
        this.subjectRepository = subjectRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    @Transactional
    public ApplicationInterviewResponseDTO getInterview(
            CurrentUserContext context,
            Long applicationId
    ) {
        Integer branchId =
                requireBranchId(context);

        Long userId =
                requireUserId(context);

        Long validApplicationId =
                requirePositiveId(
                        applicationId,
                        "Application ID"
                );

        ErpApplication application =
                loadApplicationForUpdate(
                        validApplicationId,
                        branchId
                );

        ErpApplicationInterview interview =
                interviewRepository
                        .findActiveByApplicationAndBranchForUpdate(
                                validApplicationId,
                                branchId
                        )
                        .orElse(null);

        /*
         * Backward-compatible repair for applications that were moved to
         * ENTRANCE_TEST before the direct-marks linkage was added.
         *
         * Two legacy states are supported:
         * 1) no interview row exists;
         * 2) an interview row exists but is still NOT_SCHEDULED.
         *
         * The responsible employee is already assigned during School Visit,
         * so reuse that employee instead of asking for another assignment.
         */
        boolean entranceTestStage =
                application.getCurrentStage()
                        == ErpApplication.CurrentStage.ENTRANCE_TEST;

        Long schoolVisitEmployeeId =
                application.getSchoolVisitEmployeeId();

        boolean hasSchoolVisitEmployee =
                schoolVisitEmployeeId != null
                        && schoolVisitEmployeeId > 0;

        boolean needsDirectMarksRepair =
                entranceTestStage
                        && hasSchoolVisitEmployee
                        && (
                        interview == null
                                || interview.getStatus()
                                == ErpApplicationInterview.Status.NOT_SCHEDULED
                        );

        if (needsDirectMarksRepair) {

            requireActiveBranchEmployee(
                    schoolVisitEmployeeId,
                    branchId
            );

            if (interview == null) {
                interview =
                        new ErpApplicationInterview();

                interview.setApplication(application);
                interview.setCreatedBy(userId);
                interview.setActive(true);
            }

            interview.setEmployeeId(
                    schoolVisitEmployeeId
            );

            /*
             * There is no second Entrance Test scheduling step.
             * SCHEDULED is used here as the existing backend state meaning
             * "ready for marks". The UI displays this as Ready for Marks.
             */
            interview.setStatus(
                    ErpApplicationInterview.Status.SCHEDULED
            );

            if (interview.getResult() == null) {
                interview.setResult(
                        ErpApplicationInterview.Result.PENDING
                );
            }

            interview.setUpdatedBy(userId);

            interview =
                    interviewRepository.saveAndFlush(
                            interview
                    );

            application.setTestStatus(
                    ErpApplication.TestStatus.SCHEDULED
            );
            application.setUpdatedBy(userId);

            applicationRepository.saveAndFlush(
                    application
            );
        }

        return toResponse(
                application,
                interview,
                branchId
        );
    }

    @Override
    @Transactional
    public ApplicationInterviewResponseDTO scheduleInterview(
            CurrentUserContext context,
            Long applicationId,
            ApplicationInterviewScheduleRequestDTO request
    ) {
        Integer branchId =
                requireBranchId(context);

        Long userId =
                requireUserId(context);

        validateScheduleRequest(request);

        ErpApplication application =
                loadApplicationForUpdate(
                        requirePositiveId(
                                applicationId,
                                "Application ID"
                        ),
                        branchId
                );

        requireEntranceTestStage(application);
        requireWorkflowEditable(application);

        ErpEmployee employee =
                requireActiveBranchEmployee(
                        request.employeeId(),
                        branchId
                );

        ErpApplicationInterview interview =
                interviewRepository
                        .findActiveByApplicationAndBranchForUpdate(
                                application.getApplicationId(),
                                branchId
                        )
                        .orElse(null);

        boolean schedulingRetest =
                interview != null
                        && interview.getStatus()
                        == ErpApplicationInterview.Status.COMPLETED
                        && (
                        interview.getResult()
                                == ErpApplicationInterview.Result.FAILED
                                || interview.getResult()
                                == ErpApplicationInterview.Result.RETEST_REQUIRED
                );

        if (interview != null
                && !schedulingRetest
                && interview.getStatus()
                != ErpApplicationInterview.Status.NOT_SCHEDULED
                && interview.getStatus()
                != ErpApplicationInterview.Status.CANCELLED
                && interview.getStatus()
                != ErpApplicationInterview.Status.NO_SHOW) {
            throw new BadRequestException(
                    "The Entrance Test is already scheduled. "
                            + "Use the reschedule action instead."
            );
        }

        if (schedulingRetest) {
            long previousAttemptMarks =
                    markRepository
                            .countByInterview_InterviewIdAndActiveFalse(
                                    interview.getInterviewId()
                            );

            if (previousAttemptMarks > 0) {
                throw new BadRequestException(
                        "The maximum of two Entrance Test attempts "
                                + "has already been used."
                );
            }

            /*
             * Preserve first-attempt marks for audit/history, but remove them
             * from the active/current attempt before scheduling the retest.
             */
            markRepository.deactivateAllByInterviewId(
                    interview.getInterviewId(),
                    userId
            );
        }

        if (interview == null) {
            interview =
                    new ErpApplicationInterview();

            interview.setApplication(application);
            interview.setCreatedBy(userId);
            interview.setActive(true);
        }

        interview.setEmployeeId(
                employee.getEmployeeId()
        );
        interview.setScheduledAt(
                requireFutureOrPresent(
                        request.scheduledAt(),
                        "Entrance Test date and time"
                )
        );
        interview.setStartedAt(null);
        interview.setCompletedAt(null);
        interview.setStatus(
                ErpApplicationInterview.Status.SCHEDULED
        );
        interview.setResult(
                ErpApplicationInterview.Result.PENDING
        );
        interview.setEmployeeRemarks(
                trimToNull(
                        request.employeeRemarks()
                )
        );
        interview.setInternalRemarks(
                trimToNull(
                        request.internalRemarks()
                )
        );
        interview.setUpdatedBy(userId);

        ErpApplicationInterview saved =
                interviewRepository.saveAndFlush(
                        interview
                );

        application.setTestStatus(
                ErpApplication.TestStatus.SCHEDULED
        );
        application.setUpdatedBy(userId);

        applicationRepository.saveAndFlush(
                application
        );

        return toResponse(
                application,
                saved,
                branchId
        );
    }

    @Override
    @Transactional
    public ApplicationInterviewResponseDTO rescheduleInterview(
            CurrentUserContext context,
            Long applicationId,
            ApplicationInterviewScheduleRequestDTO request
    ) {
        Integer branchId =
                requireBranchId(context);

        Long userId =
                requireUserId(context);

        validateScheduleRequest(request);

        ErpApplication application =
                loadApplicationForUpdate(
                        requirePositiveId(
                                applicationId,
                                "Application ID"
                        ),
                        branchId
                );

        requireEntranceTestStage(application);
        requireWorkflowEditable(application);

        ErpApplicationInterview interview =
                requireInterviewForUpdate(
                        application.getApplicationId(),
                        branchId
                );

        if (interview.getStatus()
                != ErpApplicationInterview.Status.SCHEDULED) {
            throw new BadRequestException(
                    "Only a scheduled Entrance Test can be rescheduled."
            );
        }

        ErpEmployee employee =
                requireActiveBranchEmployee(
                        request.employeeId(),
                        branchId
                );

        interview.setEmployeeId(
                employee.getEmployeeId()
        );
        interview.setScheduledAt(
                requireFutureOrPresent(
                        request.scheduledAt(),
                        "Entrance Test date and time"
                )
        );
        interview.setEmployeeRemarks(
                trimToNull(
                        request.employeeRemarks()
                )
        );
        interview.setInternalRemarks(
                trimToNull(
                        request.internalRemarks()
                )
        );
        interview.setUpdatedBy(userId);

        ErpApplicationInterview saved =
                interviewRepository.saveAndFlush(
                        interview
                );

        application.setTestStatus(
                ErpApplication.TestStatus.SCHEDULED
        );
        application.setUpdatedBy(userId);

        applicationRepository.saveAndFlush(
                application
        );

        return toResponse(
                application,
                saved,
                branchId
        );
    }

    @Override
    @Transactional
    public ApplicationInterviewResponseDTO startInterview(
            CurrentUserContext context,
            Long applicationId
    ) {
        Integer branchId =
                requireBranchId(context);

        Long userId =
                requireUserId(context);

        ErpApplication application =
                loadApplicationForUpdate(
                        requirePositiveId(
                                applicationId,
                                "Application ID"
                        ),
                        branchId
                );

        requireEntranceTestStage(application);
        requireWorkflowEditable(application);

        ErpApplicationInterview interview =
                requireInterviewForUpdate(
                        application.getApplicationId(),
                        branchId
                );

        if (interview.getStatus()
                != ErpApplicationInterview.Status.SCHEDULED) {
            throw new BadRequestException(
                    "The Entrance Test must be scheduled before it can start."
            );
        }

        requireActiveBranchEmployee(
                interview.getEmployeeId(),
                branchId
        );

        interview.setStartedAt(
                LocalDateTime.now()
        );
        interview.setStatus(
                ErpApplicationInterview.Status.IN_PROGRESS
        );
        interview.setUpdatedBy(userId);

        ErpApplicationInterview saved =
                interviewRepository.saveAndFlush(
                        interview
                );

        application.setTestStatus(
                ErpApplication.TestStatus.CONDUCTED
        );
        application.setUpdatedBy(userId);

        applicationRepository.saveAndFlush(
                application
        );

        return toResponse(
                application,
                saved,
                branchId
        );
    }

    @Override
    @Transactional
    public ApplicationInterviewResponseDTO completeInterview(
            CurrentUserContext context,
            Long applicationId,
            ApplicationInterviewCompleteRequestDTO request
    ) {
        Integer branchId =
                requireBranchId(context);

        Long userId =
                requireUserId(context);

        if (request == null) {
            throw new BadRequestException(
                    "Entrance Test completion details are required."
            );
        }

        if (request.marks() == null
                || request.marks().isEmpty()) {
            throw new BadRequestException(
                    "At least one subject mark is required."
            );
        }

        if (request.result() == null
                || request.result()
                == ErpApplicationInterview.Result.PENDING) {
            throw new BadRequestException(
                    "A final Entrance Test result is required."
            );
        }

        ErpApplication application =
                loadApplicationForUpdate(
                        requirePositiveId(
                                applicationId,
                                "Application ID"
                        ),
                        branchId
                );

        requireEntranceTestStage(application);
        requireWorkflowEditable(application);

        ErpApplicationInterview interview =
                requireInterviewForUpdate(
                        application.getApplicationId(),
                        branchId
                );

        /*
         * The test itself happens offline at the school. Marks may be entered
         * directly after a scheduled test; a separate Start Test action is
         * optional and is not required for completion.
         */
        if (interview.getStatus()
                != ErpApplicationInterview.Status.IN_PROGRESS
                && interview.getStatus()
                != ErpApplicationInterview.Status.SCHEDULED) {
            throw new BadRequestException(
                    "Only a scheduled or in-progress Entrance Test "
                            + "can be completed."
            );
        }

        requireActiveBranchEmployee(
                interview.getEmployeeId(),
                branchId
        );

        long previousAttemptMarks =
                markRepository
                        .countByInterview_InterviewIdAndActiveFalse(
                                interview.getInterviewId()
                        );

        boolean secondAttempt =
                previousAttemptMarks > 0;

        if (secondAttempt
                && request.result()
                == ErpApplicationInterview.Result.RETEST_REQUIRED) {
            throw new BadRequestException(
                    "A third Entrance Test attempt is not allowed. "
                            + "This retest must be completed as PASSED, "
                            + "FAILED or WAITLIST."
            );
        }

        validateSubjectMarks(
                request.marks()
        );

        Map<Long, SubjectInfo> subjectInfoById =
                loadActiveSubjects(
                        request.marks(),
                        branchId
                );

        List<ErpApplicationInterviewMark> existingMarks =
                markRepository
                        .findAllActiveByInterviewAndBranchForUpdate(
                                interview.getInterviewId(),
                                branchId
                        );

        if (!existingMarks.isEmpty()) {
            throw new BadRequestException(
                    "Entrance Test marks have already been recorded."
            );
        }

        List<ErpApplicationInterviewMark> marks =
                new ArrayList<>(
                        request.marks().size()
                );

        for (ApplicationInterviewMarkRequestDTO markRequest
                : request.marks()) {

            SubjectInfo subject =
                    subjectInfoById.get(
                            markRequest.subjectId()
                    );

            if (subject == null) {
                throw new BadRequestException(
                        "One or more selected subjects are inactive "
                                + "or do not exist."
                );
            }

            ErpApplicationInterviewMark mark =
                    new ErpApplicationInterviewMark();

            mark.setInterview(interview);
            mark.setSubjectId(
                    subject.subjectId()
            );
            mark.setMaximumMarks(
                    markRequest.maximumMarks()
            );
            mark.setObtainedMarks(
                    markRequest.obtainedMarks()
            );
            mark.setRemarks(
                    trimToNull(
                            markRequest.remarks()
                    )
            );
            mark.setCreatedBy(userId);
            mark.setUpdatedBy(userId);
            mark.setActive(true);

            marks.add(mark);
        }

        markRepository.saveAllAndFlush(
                marks
        );

        LocalDateTime now =
                LocalDateTime.now();

        LocalDateTime completedAt =
                request.completedAt() == null
                        ? now
                        : request.completedAt();

        if (completedAt.isAfter(now)) {
            throw new BadRequestException(
                    "Entrance Test completion time cannot be in the future."
            );
        }

        if (interview.getStartedAt() == null) {
            interview.setStartedAt(
                    completedAt
            );
        }

        interview.setCompletedAt(
                completedAt
        );
        interview.setResult(
                request.result()
        );
        interview.setStatus(
                ErpApplicationInterview.Status.COMPLETED
        );
        interview.setEmployeeRemarks(
                trimToNull(
                        request.employeeRemarks()
                )
        );
        interview.setInternalRemarks(
                trimToNull(
                        request.internalRemarks()
                )
        );
        interview.setUpdatedBy(userId);

        ErpApplicationInterview saved =
                interviewRepository.saveAndFlush(
                        interview
                );

        application.setTestStatus(
                mapApplicationTestStatus(
                        request.result()
                )
        );
        application.setUpdatedBy(userId);

        applicationRepository.saveAndFlush(
                application
        );

        return toResponse(
                application,
                saved,
                branchId
        );
    }

    @Override
    @Transactional
    public ApplicationInterviewResponseDTO updateWaitlistResult(
            CurrentUserContext context,
            Long applicationId,
            ApplicationInterviewWaitlistResultRequestDTO request
    ) {
        Integer branchId =
                requireBranchId(context);

        Long userId =
                requireUserId(context);

        if (request == null) {
            throw new BadRequestException(
                    "Waitlist final decision details are required."
            );
        }

        ErpApplicationInterview.Result newResult =
                request.result();

        if (newResult
                != ErpApplicationInterview.Result.PASSED
                && newResult
                != ErpApplicationInterview.Result.FAILED) {
            throw new BadRequestException(
                    "A waitlisted Entrance Test can only be changed "
                            + "to PASSED or FAILED."
            );
        }

        String decisionRemarks =
                trimToNull(
                        request.remarks()
                );

        if (decisionRemarks == null) {
            throw new BadRequestException(
                    "Decision remarks are required."
            );
        }

        ErpApplication application =
                loadApplicationForUpdate(
                        requirePositiveId(
                                applicationId,
                                "Application ID"
                        ),
                        branchId
                );

        requireEntranceTestStage(
                application
        );

        requireWorkflowEditable(
                application
        );

        ErpApplicationInterview interview =
                requireInterviewForUpdate(
                        application.getApplicationId(),
                        branchId
                );

        if (interview.getStatus()
                != ErpApplicationInterview.Status.COMPLETED) {
            throw new BadRequestException(
                    "Only a completed Entrance Test can have "
                            + "its waitlist result updated."
            );
        }

        if (interview.getResult()
                != ErpApplicationInterview.Result.WAITLIST) {
            throw new BadRequestException(
                    "Only an Entrance Test currently on WAITLIST "
                            + "can use this action."
            );
        }

        List<ErpApplicationInterviewMark> activeMarks =
                markRepository
                        .findAllActiveByInterviewAndBranchForUpdate(
                                interview.getInterviewId(),
                                branchId
                        );

        if (activeMarks.isEmpty()) {
            throw new BadRequestException(
                    "The waitlisted Entrance Test has no recorded marks."
            );
        }

        ErpApplicationInterview.Result oldResult =
                interview.getResult();

        /*
         * Important: do not alter marks, responsible employee,
         * startedAt or completedAt. This action changes only the
         * management decision on the already-completed test.
         */
        interview.setResult(
                newResult
        );

        interview.setUpdatedBy(
                userId
        );

        ErpApplicationInterview saved =
                interviewRepository.saveAndFlush(
                        interview
                );

        application.setTestStatus(
                mapApplicationTestStatus(
                        newResult
                )
        );

        application.setUpdatedBy(
                userId
        );

        applicationRepository.saveAndFlush(
                application
        );

        ErpApplicationStatusHistory history =
                new ErpApplicationStatusHistory();

        history.setApplication(
                application
        );

        history.setStage(
                "ENTRANCE_TEST"
        );

        history.setOldStatus(
                oldResult
        );

        history.setNewStatus(
                newResult
        );

        history.setChangedBy(
                userId
        );

        history.setRemarks(
                decisionRemarks
        );

        history.setInternalRemarks(
                decisionRemarks
        );

        history.setTransitionSource(
                "ERP"
        );

        history.setEmailRequired(
                false
        );

        history.setEmailStatus(
                ErpApplicationStatusHistory.EMAIL_NOT_REQUIRED
        );

        history.setActive(
                true
        );

        historyRepository.save(
                history
        );

        return toResponse(
                application,
                saved,
                branchId
        );
    }

    private ErpApplication loadApplication(
            Long applicationId,
            Integer branchId
    ) {
        return applicationRepository
                .findActiveBranchApplication(
                        applicationId,
                        branchId
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Application was not found."
                        )
                );
    }

    private ErpApplication loadApplicationForUpdate(
            Long applicationId,
            Integer branchId
    ) {
        return applicationRepository
                .findActiveBranchApplicationForUpdate(
                        applicationId,
                        branchId
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Application was not found."
                        )
                );
    }

    private ErpApplicationInterview requireInterviewForUpdate(
            Long applicationId,
            Integer branchId
    ) {
        return interviewRepository
                .findActiveByApplicationAndBranchForUpdate(
                        applicationId,
                        branchId
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Entrance Test record was not found."
                        )
                );
    }

    private ErpEmployee requireActiveBranchEmployee(
            Long employeeId,
            Integer branchId
    ) {
        Long validEmployeeId =
                requirePositiveId(
                        employeeId,
                        "Employee ID"
                );

        ErpEmployee employee =
                employeeRepository
                        .findByEmployeeIdAndBranch_BranchIdAndActiveTrue(
                                validEmployeeId,
                                branchId
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Selected employee was not found."
                                )
                        );

        if (employee.getEmploymentStatus()
                != EmploymentStatus.ACTIVE) {
            throw new BadRequestException(
                    "Only an employee with ACTIVE employment status "
                            + "can conduct an Entrance Test."
            );
        }

        return employee;
    }

    private void requireEntranceTestStage(
            ErpApplication application
    ) {
        if (application.getCurrentStage()
                != ErpApplication.CurrentStage.ENTRANCE_TEST) {
            throw new BadRequestException(
                    "Entrance Test actions are available only while "
                            + "the application is in the ENTRANCE_TEST stage."
            );
        }
    }

    private void requireWorkflowEditable(
            ErpApplication application
    ) {
        if (Boolean.TRUE.equals(
                application.getWorkflowLocked()
        )) {
            throw new BadRequestException(
                    "The admission workflow is locked."
            );
        }
    }

    private void validateScheduleRequest(
            ApplicationInterviewScheduleRequestDTO request
    ) {
        if (request == null) {
            throw new BadRequestException(
                    "Entrance Test schedule details are required."
            );
        }

        requirePositiveId(
                request.employeeId(),
                "Employee ID"
        );

        requireFutureOrPresent(
                request.scheduledAt(),
                "Entrance Test date and time"
        );
    }

    private void validateSubjectMarks(
            List<ApplicationInterviewMarkRequestDTO> marks
    ) {
        Set<Long> subjectIds =
                new HashSet<>();

        for (ApplicationInterviewMarkRequestDTO mark
                : marks) {

            if (mark == null) {
                throw new BadRequestException(
                        "Entrance Test subject mark is invalid."
                );
            }

            Long subjectId =
                    requirePositiveId(
                            mark.subjectId(),
                            "Subject ID"
                    );

            if (!subjectIds.add(subjectId)) {
                throw new BadRequestException(
                        "The same subject cannot be entered more than once."
                );
            }

            if (mark.maximumMarks() == null
                    || mark.maximumMarks()
                    .compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException(
                        "Maximum marks must be greater than zero."
                );
            }

            if (mark.obtainedMarks() == null
                    || mark.obtainedMarks()
                    .compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException(
                        "Obtained marks cannot be negative."
                );
            }

            if (mark.obtainedMarks()
                    .compareTo(mark.maximumMarks()) > 0) {
                throw new BadRequestException(
                        "Obtained marks cannot exceed maximum marks."
                );
            }
        }
    }

    private Map<Long, SubjectInfo> loadActiveSubjects(
            List<ApplicationInterviewMarkRequestDTO> marks,
            Integer branchId
    ) {
        Set<Long> requestedSubjectIds =
                new HashSet<>();

        for (ApplicationInterviewMarkRequestDTO mark
                : marks) {
            requestedSubjectIds.add(
                    mark.subjectId()
            );
        }

        Map<Long, SubjectInfo> activeSubjectById =
                loadActiveSubjectInfoById(
                        branchId
                );

        Map<Long, SubjectInfo> result =
                new HashMap<>();

        for (Long subjectId : requestedSubjectIds) {
            SubjectInfo subject =
                    activeSubjectById.get(
                            subjectId
                    );

            if (subject == null) {
                throw new BadRequestException(
                        "One or more selected subjects are inactive, "
                                + "do not exist, or do not belong to this branch."
                );
            }

            result.put(
                    subjectId,
                    subject
            );
        }

        return result;
    }

    private ApplicationInterviewResponseDTO toResponse(
            ErpApplication application,
            ErpApplicationInterview interview,
            Integer branchId
    ) {
        if (interview == null) {
            boolean editable =
                    application.getCurrentStage()
                            == ErpApplication.CurrentStage.ENTRANCE_TEST
                            && !Boolean.TRUE.equals(
                            application.getWorkflowLocked()
                    );

            return new ApplicationInterviewResponseDTO(
                    application.getApplicationId(),
                    application.getApplicationNo(),
                    null,
                    application.getCurrentStage() == null
                            ? null
                            : application.getCurrentStage().name(),
                    ErpApplicationInterview.Status.NOT_SCHEDULED,
                    ErpApplicationInterview.Result.PENDING,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    loadAvailableSubjects(branchId),
                    List.of(),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO.setScale(
                            2,
                            RoundingMode.HALF_UP
                    ),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    editable,
                    false,
                    false,
                    false,
                    false,
                    false
            );
        }

        List<ErpApplicationInterviewMark> marks =
                markRepository
                        .findAllByInterview_InterviewIdAndActiveTrueOrderByInterviewMarkIdAsc(
                                interview.getInterviewId()
                        );

        Map<Long, SubjectInfo> subjects =
                loadSubjectInfoForExistingMarks(
                        marks,
                        branchId
                );

        List<ApplicationInterviewResponseDTO.SubjectMark>
                responseMarks =
                new ArrayList<>(
                        marks.size()
                );

        BigDecimal maximumTotal =
                BigDecimal.ZERO;

        BigDecimal obtainedTotal =
                BigDecimal.ZERO;

        for (ErpApplicationInterviewMark mark
                : marks) {

            SubjectInfo subject =
                    subjects.get(
                            mark.getSubjectId()
                    );

            responseMarks.add(
                    new ApplicationInterviewResponseDTO.SubjectMark(
                            mark.getInterviewMarkId(),
                            mark.getSubjectId(),
                            subject == null
                                    ? null
                                    : subject.subjectCode(),
                            subject == null
                                    ? null
                                    : subject.subjectName(),
                            subject == null
                                    ? null
                                    : subject.subjectShortName(),
                            mark.getMaximumMarks(),
                            mark.getObtainedMarks(),
                            mark.getPercentage(),
                            mark.getRemarks()
                    )
            );

            if (mark.getMaximumMarks() != null) {
                maximumTotal =
                        maximumTotal.add(
                                mark.getMaximumMarks()
                        );
            }

            if (mark.getObtainedMarks() != null) {
                obtainedTotal =
                        obtainedTotal.add(
                                mark.getObtainedMarks()
                        );
            }
        }

        BigDecimal overallPercentage =
                calculatePercentage(
                        maximumTotal,
                        obtainedTotal
                );

        String employeeNo = null;
        String employeeName = null;

        if (interview.getEmployeeId() != null) {
            ErpEmployee employee =
                    employeeRepository
                            .findByEmployeeIdAndBranch_BranchId(
                                    interview.getEmployeeId(),
                                    branchId
                            )
                            .orElse(null);

            if (employee != null) {
                employeeNo =
                        employee.getEmployeeNo();
                employeeName =
                        employee.getFullName();
            }
        }

        boolean editable =
                application.getCurrentStage()
                        == ErpApplication.CurrentStage.ENTRANCE_TEST
                        && !Boolean.TRUE.equals(
                        application.getWorkflowLocked()
                );

        ErpApplicationInterview.Status status =
                interview.getStatus() == null
                        ? ErpApplicationInterview.Status.NOT_SCHEDULED
                        : interview.getStatus();

        long previousAttemptMarks =
                interview.getInterviewId() == null
                        ? 0
                        : markRepository
                        .countByInterview_InterviewIdAndActiveFalse(
                                interview.getInterviewId()
                        );

        boolean firstRetestAvailable =
                previousAttemptMarks == 0
                        && status
                        == ErpApplicationInterview.Status.COMPLETED
                        && (
                        interview.getResult()
                                == ErpApplicationInterview.Result.FAILED
                                || interview.getResult()
                                == ErpApplicationInterview.Result.RETEST_REQUIRED
                );

        boolean canSchedule =
                editable
                        && (
                        status
                                == ErpApplicationInterview.Status.NOT_SCHEDULED
                                || status
                                == ErpApplicationInterview.Status.CANCELLED
                                || status
                                == ErpApplicationInterview.Status.NO_SHOW
                                || firstRetestAvailable
                );

        boolean canReschedule =
                editable
                        && status
                        == ErpApplicationInterview.Status.SCHEDULED;

        boolean canStart =
                editable
                        && status
                        == ErpApplicationInterview.Status.SCHEDULED;

        boolean canComplete =
                editable
                        && (
                        status
                                == ErpApplicationInterview.Status.SCHEDULED
                                || status
                                == ErpApplicationInterview.Status.IN_PROGRESS
                );

        List<ApplicationInterviewResponseDTO.SubjectOption>
                availableSubjects =
                loadAvailableSubjects(branchId);

        boolean canProceed =
                editable
                        && status
                        == ErpApplicationInterview.Status.COMPLETED
                        && interview.getResult()
                        == ErpApplicationInterview.Result.PASSED;

        boolean canUpdateWaitlistResult =
                editable
                        && application.getCurrentStage()
                        == ErpApplication.CurrentStage.ENTRANCE_TEST
                        && status
                        == ErpApplicationInterview.Status.COMPLETED
                        && interview.getResult()
                        == ErpApplicationInterview.Result.WAITLIST;

        return new ApplicationInterviewResponseDTO(
                application.getApplicationId(),
                application.getApplicationNo(),
                interview.getInterviewId(),
                application.getCurrentStage() == null
                        ? null
                        : application.getCurrentStage().name(),
                status,
                interview.getResult(),
                interview.getEmployeeId(),
                employeeNo,
                employeeName,
                interview.getScheduledAt(),
                interview.getStartedAt(),
                interview.getCompletedAt(),
                availableSubjects,
                responseMarks,
                maximumTotal,
                obtainedTotal,
                overallPercentage,
                interview.getEmployeeRemarks(),
                interview.getInternalRemarks(),
                interview.getCreatedBy(),
                interview.getCreatedAt(),
                interview.getUpdatedBy(),
                interview.getUpdatedAt(),
                canSchedule,
                canReschedule,
                canStart,
                canComplete,
                canProceed,
                canUpdateWaitlistResult
        );
    }

    private List<ApplicationInterviewResponseDTO.SubjectOption>
    loadAvailableSubjects(
            Integer branchId
    ) {
        return subjectRepository
                .findAllByBranch_BranchIdAndActiveTrueAndStatusOrderByDisplayOrderAscSubjectNameAsc(
                        branchId,
                        ErpSubject.Status.ACTIVE
                )
                .stream()
                .map(
                        subject ->
                                new ApplicationInterviewResponseDTO.SubjectOption(
                                        subject.getSubjectId(),
                                        subject.getSubjectCode(),
                                        subject.getSubjectName(),
                                        subject.getSubjectShortName()
                                )
                )
                .toList();
    }

    private Map<Long, SubjectInfo> loadSubjectInfoForExistingMarks(
            List<ErpApplicationInterviewMark> marks,
            Integer branchId
    ) {
        if (marks == null
                || marks.isEmpty()) {
            return Map.of();
        }

        Map<Long, SubjectInfo> activeSubjectById =
                loadActiveSubjectInfoById(
                        branchId
                );

        Map<Long, SubjectInfo> result =
                new HashMap<>();

        for (ErpApplicationInterviewMark mark
                : marks) {
            if (mark.getSubjectId() == null) {
                continue;
            }

            SubjectInfo subject =
                    activeSubjectById.get(
                            mark.getSubjectId()
                    );

            if (subject != null) {
                result.put(
                        subject.subjectId(),
                        subject
                );
            }
        }

        return result;
    }

    private Map<Long, SubjectInfo> loadActiveSubjectInfoById(
            Integer branchId
    ) {
        List<ErpSubject> subjects =
                subjectRepository
                        .findAllByBranch_BranchIdAndActiveTrueAndStatusOrderByDisplayOrderAscSubjectNameAsc(
                                branchId,
                                ErpSubject.Status.ACTIVE
                        );

        Map<Long, SubjectInfo> result =
                new HashMap<>(
                        Math.max(
                                16,
                                subjects.size() * 2
                        )
                );

        for (ErpSubject subject : subjects) {
            result.put(
                    subject.getSubjectId(),
                    new SubjectInfo(
                            subject.getSubjectId(),
                            subject.getSubjectCode(),
                            subject.getSubjectName(),
                            subject.getSubjectShortName()
                    )
            );
        }

        return result;
    }

    private ErpApplication.TestStatus mapApplicationTestStatus(
            ErpApplicationInterview.Result result
    ) {
        return switch (result) {
            case PASSED ->
                    ErpApplication.TestStatus.PASSED;
            case FAILED ->
                    ErpApplication.TestStatus.FAILED;
            case WAITLIST ->
                    ErpApplication.TestStatus.WAITLISTED;
            case RETEST_REQUIRED ->
                    ErpApplication.TestStatus.RETEST_REQUIRED;
            case PENDING ->
                    ErpApplication.TestStatus.CONDUCTED;
        };
    }

    private BigDecimal calculatePercentage(
            BigDecimal maximum,
            BigDecimal obtained
    ) {
        if (maximum == null
                || maximum.compareTo(
                BigDecimal.ZERO
        ) <= 0) {
            return BigDecimal.ZERO.setScale(
                    2,
                    RoundingMode.HALF_UP
            );
        }

        return obtained
                .multiply(
                        BigDecimal.valueOf(100)
                )
                .divide(
                        maximum,
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private LocalDateTime requireFutureOrPresent(
            LocalDateTime value,
            String fieldName
    ) {
        if (value == null) {
            throw new BadRequestException(
                    fieldName + " is required."
            );
        }

        /*
         * Small tolerance prevents a value selected as "now" from failing
         * merely because request binding and service execution took a moment.
         */
        if (value.isBefore(
                LocalDateTime.now()
                        .minusMinutes(1)
        )) {
            throw new BadRequestException(
                    fieldName + " cannot be in the past."
            );
        }

        return value;
    }

    private Integer requireBranchId(
            CurrentUserContext context
    ) {
        return branchAccessService
                .getValidatedBranchId(
                        context
                );
    }

    private Long requireUserId(
            CurrentUserContext context
    ) {
        if (context == null
                || context.getUserId() == null
                || context.getUserId() <= 0) {
            throw new BadRequestException(
                    "Authenticated user ID is unavailable."
            );
        }

        return context.getUserId()
                .longValue();
    }

    private Long requirePositiveId(
            Long value,
            String fieldName
    ) {
        if (value == null
                || value <= 0) {
            throw new BadRequestException(
                    fieldName + " must be greater than zero."
            );
        }

        return value;
    }

    private String trimToNull(
            String value
    ) {
        if (value == null
                || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private record SubjectInfo(
            Long subjectId,
            String subjectCode,
            String subjectName,
            String subjectShortName
    ) {
    }
}

package com.erp.montfortuganda.admission.service;

import com.erp.montfortuganda.admission.dto.ApplicationSchoolVisitCompleteRequestDTO;
import com.erp.montfortuganda.admission.dto.ApplicationSchoolVisitResponseDTO;
import com.erp.montfortuganda.admission.dto.ApplicationSchoolVisitScheduleRequestDTO;
import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.admission.entity.ErpApplicationDocument;
import com.erp.montfortuganda.admission.entity.ErpApplicationDocumentRequest;
import com.erp.montfortuganda.admission.repository.ErpApplicationRepository;
import com.erp.montfortuganda.admission.repository.ErpApplicationDocumentRepository;
import com.erp.montfortuganda.admission.repository.ErpApplicationDocumentRequestRepository;
import com.erp.montfortuganda.auth.service.BranchAccessService;
import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.employee.entity.ErpEmployee;
import com.erp.montfortuganda.employee.enums.EmploymentStatus;
import com.erp.montfortuganda.employee.repository.ErpEmployeeRepository;
import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class ApplicationSchoolVisitServiceImpl
        implements ApplicationSchoolVisitService {

    private final ErpApplicationRepository applicationRepository;
    private final ErpApplicationDocumentRepository documentRepository;
    private final ErpApplicationDocumentRequestRepository documentRequestRepository;
    private final ErpEmployeeRepository employeeRepository;
    private final BranchAccessService branchAccessService;
    private final ApplicationEventPublisher eventPublisher;

    public ApplicationSchoolVisitServiceImpl(
            ErpApplicationRepository applicationRepository,
            ErpApplicationDocumentRepository documentRepository,
            ErpApplicationDocumentRequestRepository documentRequestRepository,
            ErpEmployeeRepository employeeRepository,
            BranchAccessService branchAccessService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.applicationRepository = applicationRepository;
        this.documentRepository = documentRepository;
        this.documentRequestRepository = documentRequestRepository;
        this.employeeRepository = employeeRepository;
        this.branchAccessService = branchAccessService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ApplicationSchoolVisitResponseDTO getSchoolVisit(
            CurrentUserContext context,
            Long applicationId
    ) {
        Integer branchId = requireBranchId(context);

        ErpApplication application =
                loadApplication(
                        requirePositiveId(applicationId, "Application ID"),
                        branchId
                );

        return toResponse(application, branchId);
    }

    @Override
    @Transactional
    public ApplicationSchoolVisitResponseDTO scheduleSchoolVisit(
            CurrentUserContext context,
            Long applicationId,
            ApplicationSchoolVisitScheduleRequestDTO request
    ) {
        Integer branchId = requireBranchId(context);
        Integer userId = requireUserId(context);

        requireScheduleRequest(request);

        ErpApplication application =
                loadApplicationForUpdate(
                        requirePositiveId(applicationId, "Application ID"),
                        branchId
                );

        requireSchoolVisitStage(application);
        requireWorkflowEditable(application);

        if (!areVerificationDocumentsResolved(application, branchId)) {
            throw new BadRequestException(
                    "All existing application documents must be verified and "
                            + "all document requests must be resolved before "
                            + "proceeding to the Entrance Test."
            );
        }

        ErpApplication.SchoolVisitStatus currentStatus =
                safeSchoolVisitStatus(application);

        if (currentStatus != ErpApplication.SchoolVisitStatus.NOT_SCHEDULED
                && currentStatus != ErpApplication.SchoolVisitStatus.CANCELLED
                && currentStatus != ErpApplication.SchoolVisitStatus.NO_SHOW) {
            throw new BadRequestException(
                    "The school visit is already scheduled. Use the reschedule action instead."
            );
        }

        LocalDateTime scheduledAt =
                requireFutureOrPresent(
                        request.getScheduledAt(),
                        "School visit date and time"
                );

        /*
         * Scheduling does NOT assign an employee.
         * Employee assignment happens only when the parent/student attends.
         */
        application.setSchoolVisitEmployeeId(null);
        application.setSchoolVisitScheduledAt(scheduledAt);
        application.setSchoolVisitStatus(
                ErpApplication.SchoolVisitStatus.SCHEDULED
        );

        application.setSchoolVisitAt(null);
        application.setSchoolVisitCompletedBy(null);
        application.setSchoolVisitCompletedAt(null);
        application.setSchoolVisitStudentAttended(false);
        application.setSchoolVisitParentAttended(false);

        application.setSchoolVisitRemarks(
                trimToNull(request.getRemarks())
        );
        application.setUpdatedBy(userId.longValue());

        ErpApplication saved =
                applicationRepository.saveAndFlush(application);

        eventPublisher.publishEvent(
                new ApplicationSchoolVisitEmailRequestedEvent(
                        saved.getApplicationId(),
                        ApplicationSchoolVisitEmailRequestedEvent.Action.SCHEDULED
                )
        );

        return toResponse(saved, branchId);
    }

    @Override
    @Transactional
    public ApplicationSchoolVisitResponseDTO rescheduleSchoolVisit(
            CurrentUserContext context,
            Long applicationId,
            ApplicationSchoolVisitScheduleRequestDTO request
    ) {
        Integer branchId = requireBranchId(context);
        Integer userId = requireUserId(context);

        requireScheduleRequest(request);

        ErpApplication application =
                loadApplicationForUpdate(
                        requirePositiveId(applicationId, "Application ID"),
                        branchId
                );

        requireSchoolVisitStage(application);
        requireWorkflowEditable(application);

        ErpApplication.SchoolVisitStatus currentStatus =
                safeSchoolVisitStatus(application);

        if (currentStatus != ErpApplication.SchoolVisitStatus.SCHEDULED
                && currentStatus != ErpApplication.SchoolVisitStatus.RESCHEDULED) {
            throw new BadRequestException(
                    "Only a scheduled school visit can be rescheduled."
            );
        }

        if (application.getSchoolVisitEmployeeId() != null) {
            throw new BadRequestException(
                    "The school visit cannot be rescheduled after an employee has been assigned."
            );
        }

        LocalDateTime scheduledAt =
                requireFutureOrPresent(
                        request.getScheduledAt(),
                        "School visit date and time"
                );

        application.setSchoolVisitScheduledAt(scheduledAt);
        application.setSchoolVisitStatus(
                ErpApplication.SchoolVisitStatus.RESCHEDULED
        );

        application.setSchoolVisitAt(null);
        application.setSchoolVisitCompletedBy(null);
        application.setSchoolVisitCompletedAt(null);
        application.setSchoolVisitStudentAttended(false);
        application.setSchoolVisitParentAttended(false);

        application.setSchoolVisitRemarks(
                trimToNull(request.getRemarks())
        );
        application.setUpdatedBy(userId.longValue());

        ErpApplication saved =
                applicationRepository.saveAndFlush(application);

        eventPublisher.publishEvent(
                new ApplicationSchoolVisitEmailRequestedEvent(
                        saved.getApplicationId(),
                        ApplicationSchoolVisitEmailRequestedEvent.Action.RESCHEDULED
                )
        );

        return toResponse(saved, branchId);
    }

    @Override
    @Transactional
    public ApplicationSchoolVisitResponseDTO completeSchoolVisit(
            CurrentUserContext context,
            Long applicationId,
            ApplicationSchoolVisitCompleteRequestDTO request
    ) {
        Integer branchId = requireBranchId(context);
        Integer userId = requireUserId(context);

        if (request == null) {
            throw new BadRequestException(
                    "School visit attendance details are required."
            );
        }

        ErpApplication application =
                loadApplicationForUpdate(
                        requirePositiveId(applicationId, "Application ID"),
                        branchId
                );

        requireSchoolVisitStage(application);
        requireWorkflowEditable(application);

        ErpApplication.SchoolVisitStatus currentStatus =
                safeSchoolVisitStatus(application);

        if (currentStatus != ErpApplication.SchoolVisitStatus.SCHEDULED
                && currentStatus != ErpApplication.SchoolVisitStatus.RESCHEDULED) {
            throw new BadRequestException(
                    "The school visit must be scheduled before attendance can be recorded."
            );
        }

        ErpEmployee employee =
                requireActiveBranchEmployee(
                        request.getEmployeeId(),
                        branchId
                );

        if (request.getStudentAttended() == null) {
            throw new BadRequestException(
                    "Student attendance selection is required."
            );
        }

        if (request.getParentAttended() == null) {
            throw new BadRequestException(
                    "Parent or guardian attendance selection is required."
            );
        }

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime visitedAt =
                request.getVisitedAt() == null
                        ? now
                        : request.getVisitedAt();

        if (visitedAt.isAfter(now)) {
            throw new BadRequestException(
                    "School visit attendance time cannot be in the future."
            );
        }

        application.setSchoolVisitEmployeeId(
                employee.getEmployeeId()
        );
        application.setSchoolVisitAt(visitedAt);
        application.setSchoolVisitStudentAttended(
                request.getStudentAttended()
        );
        application.setSchoolVisitParentAttended(
                request.getParentAttended()
        );
        application.setSchoolVisitRemarks(
                trimToNull(request.getRemarks())
        );
        application.setSchoolVisitStatus(
                ErpApplication.SchoolVisitStatus.ATTENDED
        );

        /*
         * Do not mark the School Visit COMPLETED here.
         * The parent/student has attended and the responsible employee has
         * been recorded, but the Entrance Test process is still pending.
         */
        application.setSchoolVisitCompletedBy(null);
        application.setSchoolVisitCompletedAt(null);
        application.setUpdatedBy(userId.longValue());

        ErpApplication saved =
                applicationRepository.saveAndFlush(application);

        return toResponse(saved, branchId);
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

        if (employee.getEmploymentStatus() != EmploymentStatus.ACTIVE) {
            throw new BadRequestException(
                    "Only an employee with ACTIVE employment status can be assigned to a school visit."
            );
        }

        return employee;
    }

    private void requireSchoolVisitStage(
            ErpApplication application
    ) {
        if (application.getCurrentStage()
                != ErpApplication.CurrentStage.SCHOOL_VISIT) {
            throw new BadRequestException(
                    "School Visit actions are available only while the application is in the SCHOOL_VISIT stage."
            );
        }
    }

    private void requireWorkflowEditable(
            ErpApplication application
    ) {
        if (Boolean.TRUE.equals(application.getWorkflowLocked())) {
            throw new BadRequestException(
                    "The admission workflow is locked."
            );
        }
    }

    private void requireScheduleRequest(
            ApplicationSchoolVisitScheduleRequestDTO request
    ) {
        if (request == null) {
            throw new BadRequestException(
                    "School visit schedule details are required."
            );
        }

        requireFutureOrPresent(
                request.getScheduledAt(),
                "School visit date and time"
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

        if (value.isBefore(LocalDateTime.now())) {
            throw new BadRequestException(
                    fieldName + " cannot be in the past."
            );
        }

        return value;
    }

    private ErpApplication.SchoolVisitStatus safeSchoolVisitStatus(
            ErpApplication application
    ) {
        return application.getSchoolVisitStatus() == null
                ? ErpApplication.SchoolVisitStatus.NOT_SCHEDULED
                : application.getSchoolVisitStatus();
    }

    private ApplicationSchoolVisitResponseDTO toResponse(
            ErpApplication application,
            Integer branchId
    ) {
        ApplicationSchoolVisitResponseDTO response =
                new ApplicationSchoolVisitResponseDTO();

        response.setApplicationId(
                application.getApplicationId()
        );
        response.setApplicationNo(
                application.getApplicationNo()
        );
        response.setCurrentStage(
                application.getCurrentStage()
        );

        ErpApplication.SchoolVisitStatus visitStatus =
                safeSchoolVisitStatus(application);

        response.setSchoolVisitStatus(visitStatus);

        Long employeeId =
                application.getSchoolVisitEmployeeId();

        response.setEmployeeId(employeeId);

        if (employeeId != null && employeeId > 0) {
            employeeRepository
                    .findByEmployeeIdAndBranch_BranchId(
                            employeeId,
                            branchId
                    )
                    .ifPresent(
                            employee -> {
                                response.setEmployeeNo(
                                        employee.getEmployeeNo()
                                );
                                response.setEmployeeName(
                                        employee.getFullName()
                                );
                            }
                    );
        }

        response.setScheduledAt(
                application.getSchoolVisitScheduledAt()
        );
        response.setVisitedAt(
                application.getSchoolVisitAt()
        );
        response.setStudentAttended(
                Boolean.TRUE.equals(
                        application.getSchoolVisitStudentAttended()
                )
        );
        response.setParentAttended(
                Boolean.TRUE.equals(
                        application.getSchoolVisitParentAttended()
                )
        );
        response.setRemarks(
                application.getSchoolVisitRemarks()
        );
        response.setCompletedBy(
                application.getSchoolVisitCompletedBy()
        );
        response.setCompletedAt(
                application.getSchoolVisitCompletedAt()
        );

        boolean inSchoolVisitStage =
                application.getCurrentStage()
                        == ErpApplication.CurrentStage.SCHOOL_VISIT;

        boolean editable =
                inSchoolVisitStage
                        && !Boolean.TRUE.equals(
                        application.getWorkflowLocked()
                );

        boolean scheduled =
                visitStatus == ErpApplication.SchoolVisitStatus.SCHEDULED
                        || visitStatus == ErpApplication.SchoolVisitStatus.RESCHEDULED;

        boolean employeeAssigned =
                employeeId != null && employeeId > 0;

        boolean documentsResolved =
                areVerificationDocumentsResolved(
                        application,
                        branchId
                );

        response.setCanSchedule(
                editable
                        && (
                        visitStatus == ErpApplication.SchoolVisitStatus.NOT_SCHEDULED
                                || visitStatus == ErpApplication.SchoolVisitStatus.CANCELLED
                                || visitStatus == ErpApplication.SchoolVisitStatus.NO_SHOW
                )
        );

        response.setCanReschedule(
                editable
                        && scheduled
                        && !employeeAssigned
        );

        /*
         * Employee assignment is no longer a separate dashboard action.
         * The employee is selected inside the visit-day / proceed-to-test
         * action together with attendance details.
         */
        response.setCanAssignEmployee(false);

        response.setCanComplete(
                editable
                        && scheduled
                        && documentsResolved
        );

        response.setCanProceedToEntranceTest(
                inSchoolVisitStage
                        && visitStatus
                        == ErpApplication.SchoolVisitStatus.ATTENDED
        );

        return response;
    }

    /**
     * Returns true when document verification no longer blocks the visit-day
     * action.
     *
     * <p>No documents is a valid state and does not block progression.
     * When documents exist, every current active document must be VERIFIED.
     * PENDING or UPLOADED additional-document requests remain unresolved and
     * therefore block progression.</p>
     */
    private boolean areVerificationDocumentsResolved(
            ErpApplication application,
            Integer branchId
    ) {
        if (application == null
                || application.getApplicationId() == null
                || branchId == null) {
            return false;
        }

        Long applicationId =
                application.getApplicationId();

        var requests =
                documentRequestRepository
                        .findAllByApplication_ApplicationIdAndApplication_Branch_BranchIdAndActiveTrueOrderByRequestedAtDesc(
                                applicationId,
                                branchId
                        );

        boolean unresolvedRequest =
                requests.stream()
                        .anyMatch(request ->
                                request.getRequestStatus()
                                        == ErpApplicationDocumentRequest.RequestStatus.PENDING
                                        || request.getRequestStatus()
                                        == ErpApplicationDocumentRequest.RequestStatus.UPLOADED
                        );

        if (unresolvedRequest) {
            return false;
        }

        var documents =
                documentRepository
                        .findAllByApplication_ApplicationIdAndApplication_Branch_BranchIdAndCurrentTrueAndActiveTrueOrderByUploadedAtAsc(
                                applicationId,
                                branchId
                        );

        if (documents.isEmpty()) {
            return true;
        }

        return documents.stream()
                .allMatch(document ->
                        document.getVerificationStatus()
                                == ErpApplicationDocument.VerificationStatus.VERIFIED
                );
    }

    private Integer requireBranchId(
            CurrentUserContext context
    ) {
        return branchAccessService
                .getValidatedBranchId(context);
    }

    private Integer requireUserId(
            CurrentUserContext context
    ) {
        if (context == null
                || context.getUserId() == null
                || context.getUserId() <= 0) {
            throw new BadRequestException(
                    "Authenticated user ID is unavailable."
            );
        }

        return context.getUserId();
    }

    private Long requirePositiveId(
            Long value,
            String fieldName
    ) {
        if (value == null || value <= 0) {
            throw new BadRequestException(
                    fieldName + " must be greater than zero."
            );
        }

        return value;
    }

    private String trimToNull(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}

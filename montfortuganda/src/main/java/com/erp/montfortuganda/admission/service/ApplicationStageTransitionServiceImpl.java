package com.erp.montfortuganda.admission.service;

import com.erp.montfortuganda.admission.dto.ApplicationStageTransitionRequestDTO;
import com.erp.montfortuganda.admission.dto.ApplicationStageTransitionRequestDTO.TransitionAction;
import com.erp.montfortuganda.admission.dto.ApplicationStageTransitionResponseDTO;
import com.erp.montfortuganda.admission.dto.ApplicationStageTransitionResponseDTO.AvailableTransition;
import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.admission.entity.ErpApplicationStatusHistory;
import com.erp.montfortuganda.admission.repository.ErpApplicationRepository;
import com.erp.montfortuganda.admission.repository.ErpApplicationStatusHistoryRepository;
import com.erp.montfortuganda.auth.service.BranchAccessService;
import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Branch-safe admission workflow transition implementation.
 *
 * <p>Every mutation loads the active application using the existing
 * branch-scoped pessimistic-write repository query. The browser supplies an
 * expected stage, but the locked database stage remains authoritative.</p>
 */
@Service
@Transactional(readOnly = true)
public class ApplicationStageTransitionServiceImpl
        implements ApplicationStageTransitionService {

    private static final String HISTORY_STAGE =
            "APPLICATION_WORKFLOW";

    private static final String TRANSITION_SOURCE =
            "BRANCH_ADMIN";

    private static final String EMAIL_PENDING =
            "PENDING";

    private static final String EMAIL_TYPE =
            "APPLICATION_STAGE_TRANSITION";

    private final ErpApplicationRepository applicationRepository;
    private final ErpApplicationStatusHistoryRepository historyRepository;
    private final BranchAccessService branchAccessService;
    private final ApplicationStageTransitionValidator transitionValidator;
    private final ApplicationEventPublisher eventPublisher;

    public ApplicationStageTransitionServiceImpl(
            ErpApplicationRepository applicationRepository,
            ErpApplicationStatusHistoryRepository historyRepository,
            BranchAccessService branchAccessService,
            ApplicationStageTransitionValidator transitionValidator,
            ApplicationEventPublisher eventPublisher
    ) {
        this.applicationRepository = applicationRepository;
        this.historyRepository = historyRepository;
        this.branchAccessService = branchAccessService;
        this.transitionValidator = transitionValidator;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public ApplicationStageTransitionResponseDTO transition(
            CurrentUserContext context,
            Long applicationId,
            ApplicationStageTransitionRequestDTO request
    ) {
        Integer branchId =
                requireBranchId(context);

        Integer userId =
                requireUserId(context);

        Long validApplicationId =
                requirePositiveId(
                        applicationId,
                        "Application ID"
                );

        ErpApplication application =
                applicationRepository
                        .findActiveBranchApplicationForUpdate(
                                validApplicationId,
                                branchId
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Application was not found."
                                )
                        );

        ensureTransitionAllowedForLockState(
                application,
                request
        );

        ErpApplication.CurrentStage previousStage =
                application.getCurrentStage();

        transitionValidator.validate(
                previousStage,
                request
        );

        validateStageReadiness(
                application,
                request
        );

        boolean emailRequired =
                shouldNotifyApplicant(
                        application,
                        request
                );

        applyTransition(
                application,
                previousStage,
                request,
                userId.longValue()
        );

        ErpApplication savedApplication =
                applicationRepository.saveAndFlush(
                        application
                );

        ErpApplicationStatusHistory history =
                createHistory(
                        savedApplication,
                        previousStage,
                        request,
                        userId.longValue(),
                        emailRequired
                );

        ErpApplicationStatusHistory savedHistory =
                historyRepository.saveAndFlush(
                        history
                );

        if (emailRequired) {
            eventPublisher.publishEvent(
                    new ApplicationStageTransitionEmailRequestedEvent(
                            savedHistory.getHistoryId()
                    )
            );
        }

        return toResponse(
                savedApplication,
                previousStage,
                request.getAction(),
                savedHistory
        );
    }

    @Override
    public List<AvailableTransition> getAvailableTransitions(
            CurrentUserContext context,
            Long applicationId
    ) {
        Integer branchId =
                requireBranchId(context);

        Long validApplicationId =
                requirePositiveId(
                        applicationId,
                        "Application ID"
                );

        ErpApplication application =
                applicationRepository
                        .findActiveBranchApplication(
                                validApplicationId,
                                branchId
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Application was not found."
                                )
                        );

        return buildAvailableTransitions(
                application
        );
    }

    private void ensureTransitionAllowedForLockState(
            ErpApplication application,
            ApplicationStageTransitionRequestDTO request
    ) {
        if (!Boolean.TRUE.equals(
                application.getWorkflowLocked()
        )) {
            return;
        }

        TransitionAction action =
                request == null
                        ? null
                        : request.getAction();

        boolean permittedLockedAction =
                action == TransitionAction.REOPEN
                        || action == TransitionAction.CLOSE;

        if (!permittedLockedAction) {
            throw new BadRequestException(
                    "The admission workflow is locked. "
                            + "Only an approved close or reopen action is allowed."
            );
        }
    }

    /**
     * Prevents the generic stage endpoint from bypassing the domain result
     * required by completed workflow areas.
     */
    private void validateStageReadiness(
            ErpApplication application,
            ApplicationStageTransitionRequestDTO request
    ) {
        if (request.getAction() != TransitionAction.ADVANCE) {
            return;
        }

        ErpApplication.CurrentStage current =
                application.getCurrentStage();

        ErpApplication.CurrentStage target =
                request.getTargetStage();

        if (current
                == ErpApplication.CurrentStage.APPLICATION_VERIFICATION
                && target
                == ErpApplication.CurrentStage.SCHOOL_VISIT) {

            if (application.getDocumentStatus()
                    != ErpApplication.DocumentStatus.VERIFIED) {
                throw new BadRequestException(
                        "All current application documents must be verified "
                                + "before moving to the school-visit stage."
                );
            }
        }

        if (current
                == ErpApplication.CurrentStage.ENTRANCE_TEST
                && target
                == ErpApplication.CurrentStage.PARENT_FEE_DISCUSSION) {

            ErpApplication.TestStatus testStatus =
                    application.getTestStatus();

            boolean completed =
                    testStatus == ErpApplication.TestStatus.PASSED
                            || testStatus
                            == ErpApplication.TestStatus.COMPLETED;

            if (!completed) {
                throw new BadRequestException(
                        "The entrance test must be passed or completed "
                                + "before starting the parent fee discussion."
                );
            }
        }

        if (current
                == ErpApplication.CurrentStage.SCHOLARSHIP
                && target
                == ErpApplication.CurrentStage.PAYMENT) {

            String scholarshipStatus =
                    normalizeStatus(
                            application.getScholarshipStatus()
                    );

            boolean decisionCompleted =
                    scholarshipStatus.equals("APPROVED")
                            || scholarshipStatus.equals("REJECTED")
                            || scholarshipStatus.equals("DECLINED")
                            || scholarshipStatus.equals("COMPLETED")
                            || scholarshipStatus.equals("NOT_APPROVED");

            if (!decisionCompleted) {
                throw new BadRequestException(
                        "The scholarship decision must be completed "
                                + "before moving to payment."
                );
            }
        }

        if (current
                == ErpApplication.CurrentStage.PAYMENT
                && target
                == ErpApplication.CurrentStage.FINAL_ADMISSION) {

            ErpApplication.PaymentStatus paymentStatus =
                    application.getPaymentStatus();

            boolean paymentCompleted =
                    paymentStatus == ErpApplication.PaymentStatus.PAID
                            || paymentStatus
                            == ErpApplication.PaymentStatus.COMPLETED;

            if (!paymentCompleted) {
                throw new BadRequestException(
                        "Required admission payment must be completed "
                                + "before final admission approval."
                );
            }
        }

        if (current
                == ErpApplication.CurrentStage.FINAL_ADMISSION
                && target
                == ErpApplication.CurrentStage.ENROLLED) {

            if (application.getAdmissionStatus()
                    != ErpApplication.AdmissionStatus.APPROVED) {
                throw new BadRequestException(
                        "Final admission must be approved before enrollment."
                );
            }

            if (!Boolean.TRUE.equals(
                    application.getStudentCreated()
            )) {
                throw new BadRequestException(
                        "The Student and enrollment records must be created "
                                + "before marking the application as enrolled."
                );
            }
        }
    }

    private void applyTransition(
            ErpApplication application,
            ErpApplication.CurrentStage previousStage,
            ApplicationStageTransitionRequestDTO request,
            Long userId
    ) {
        ErpApplication.CurrentStage target =
                request.getTargetStage();

        application.setCurrentStage(target);
        application.setUpdatedBy(userId);
        application.setRemarks(
                firstNonBlank(
                        trimToNull(
                                request.getInternalRemarks()
                        ),
                        trimToNull(
                                request.getPublicRemarks()
                        ),
                        application.getRemarks()
                )
        );

        if (request.getAction() == TransitionAction.REJECT) {
            application.setRejectionReason(
                    firstNonBlank(
                            trimToNull(
                                    request.getPublicRemarks()
                            ),
                            trimToNull(
                                    request.getInternalRemarks()
                            )
                    )
            );
        }

        if (request.getAction() == TransitionAction.REOPEN) {
            application.setRejectionReason(null);
            application.setWorkflowLocked(false);
        }

        if (request.getAction() == TransitionAction.RETURN) {
            applyReturnState(
                    application,
                    target
            );
            return;
        }

        applyTargetState(
                application,
                previousStage,
                target,
                request.getAction(),
                userId
        );
    }

    private void applyTargetState(
            ErpApplication application,
            ErpApplication.CurrentStage previousStage,
            ErpApplication.CurrentStage target,
            TransitionAction action,
            Long userId
    ) {
        switch (target) {
            case APPLICATION_DRAFT -> {
                application.setApplicationStatus(
                        ErpApplication.ApplicationStatus.DRAFT
                );
                application.setAdmissionStatus(
                        ErpApplication.AdmissionStatus.PENDING
                );
                application.setWorkflowLocked(false);
            }

            case APPLICATION_VERIFICATION -> {
                application.setApplicationStatus(
                        ErpApplication.ApplicationStatus.UNDER_REVIEW
                );
                application.setVerificationStatus(
                        ErpApplication.VerificationStatus.PENDING
                );
                application.setAdmissionStatus(
                        ErpApplication.AdmissionStatus.PENDING
                );
                application.setWorkflowLocked(false);
            }

            case SCHOOL_VISIT -> {
                application.setApplicationStatus(
                        ErpApplication.ApplicationStatus.UNDER_REVIEW
                );
                application.setVerificationStatus(
                        ErpApplication.VerificationStatus.APPROVED
                );
                application.setVerificationDecisionBy(userId);
                application.setVerificationDecisionAt(
                        LocalDateTime.now()
                );

                if (application.getSchoolVisitAt() == null) {
                    application.setSchoolVisitAt(
                            LocalDateTime.now()
                    );
                }
            }

            case ENTRANCE_TEST -> {
                application.setApplicationStatus(
                        ErpApplication.ApplicationStatus.UNDER_REVIEW
                );

                if (application.getTestStatus()
                        == ErpApplication.TestStatus.NOT_SCHEDULED) {
                    application.setTestStatus(
                            ErpApplication.TestStatus.SCHEDULED
                    );
                }
            }

            case PARENT_FEE_DISCUSSION -> {
                application.setApplicationStatus(
                        ErpApplication.ApplicationStatus.UNDER_REVIEW
                );

                if (application.getFeeDecisionStatus()
                        == ErpApplication.FeeDecisionStatus.NOT_STARTED) {
                    application.setFeeDecisionStatus(
                            ErpApplication.FeeDecisionStatus.DECISION_PENDING
                    );
                }
            }

            case SCHOLARSHIP -> {
                application.setApplicationStatus(
                        ErpApplication.ApplicationStatus.UNDER_REVIEW
                );
                application.setFeeDecisionStatus(
                        ErpApplication.FeeDecisionStatus.SCHOLARSHIP_REQUESTED
                );

                if (!hasMeaningfulScholarshipStatus(
                        application.getScholarshipStatus()
                )) {
                    application.setScholarshipStatus(
                            "PENDING"
                    );
                }
            }

            case PAYMENT -> {
                application.setApplicationStatus(
                        ErpApplication.ApplicationStatus.UNDER_REVIEW
                );

                if (previousStage
                        == ErpApplication.CurrentStage.PARENT_FEE_DISCUSSION) {
                    application.setFeeDecisionStatus(
                            ErpApplication.FeeDecisionStatus.FEE_ACCEPTED
                    );
                } else if (previousStage
                        == ErpApplication.CurrentStage.SCHOLARSHIP) {
                    application.setFeeDecisionStatus(
                            ErpApplication.FeeDecisionStatus.COMPLETED
                    );
                }

                if (application.getPaymentStatus()
                        == ErpApplication.PaymentStatus.NOT_STARTED) {
                    application.setPaymentStatus(
                            ErpApplication.PaymentStatus.PENDING
                    );
                }
            }

            case FINAL_ADMISSION -> {
                application.setApplicationStatus(
                        ErpApplication.ApplicationStatus.APPROVED
                );
                application.setAdmissionStatus(
                        ErpApplication.AdmissionStatus.APPROVED
                );
                application.setWorkflowLocked(false);
            }

            case ENROLLED -> {
                application.setApplicationStatus(
                        ErpApplication.ApplicationStatus.ADMITTED
                );
                application.setAdmissionStatus(
                        ErpApplication.AdmissionStatus.ENROLLED
                );
                application.setWorkflowLocked(true);
            }

            case CLOSED -> {
                if (action == TransitionAction.REJECT) {
                    application.setApplicationStatus(
                            ErpApplication.ApplicationStatus.REJECTED
                    );
                    application.setAdmissionStatus(
                            ErpApplication.AdmissionStatus.REJECTED
                    );

                    if (previousStage
                            == ErpApplication.CurrentStage.APPLICATION_VERIFICATION) {
                        application.setVerificationStatus(
                                ErpApplication.VerificationStatus.REJECTED
                        );
                        application.setVerificationDecisionBy(userId);
                        application.setVerificationDecisionAt(
                                LocalDateTime.now()
                        );
                    }
                } else {
                    application.setAdmissionStatus(
                            ErpApplication.AdmissionStatus.CLOSED
                    );
                }

                application.setWorkflowLocked(true);
            }
        }
    }

    private void applyReturnState(
            ErpApplication application,
            ErpApplication.CurrentStage target
    ) {
        application.setApplicationStatus(
                ErpApplication.ApplicationStatus.UNDER_REVIEW
        );
        application.setAdmissionStatus(
                ErpApplication.AdmissionStatus.PENDING
        );
        application.setWorkflowLocked(false);

        switch (target) {
            case APPLICATION_DRAFT ->
                    application.setApplicationStatus(
                            ErpApplication.ApplicationStatus.DRAFT
                    );

            case APPLICATION_VERIFICATION -> {
                application.setVerificationStatus(
                        ErpApplication.VerificationStatus.PENDING
                );
                application.setVerificationDecisionBy(null);
                application.setVerificationDecisionAt(null);
            }

            case SCHOOL_VISIT ->
                    application.setTestStatus(
                            ErpApplication.TestStatus.NOT_SCHEDULED
                    );

            case ENTRANCE_TEST -> {
                application.setFeeDecisionStatus(
                        ErpApplication.FeeDecisionStatus.NOT_STARTED
                );
                application.setPaymentStatus(
                        ErpApplication.PaymentStatus.NOT_STARTED
                );
            }

            case PARENT_FEE_DISCUSSION -> {
                application.setFeeDecisionStatus(
                        ErpApplication.FeeDecisionStatus.DECISION_PENDING
                );
                application.setPaymentStatus(
                        ErpApplication.PaymentStatus.NOT_STARTED
                );
            }

            case SCHOLARSHIP -> {
                application.setFeeDecisionStatus(
                        ErpApplication.FeeDecisionStatus.SCHOLARSHIP_REQUESTED
                );
                application.setPaymentStatus(
                        ErpApplication.PaymentStatus.NOT_STARTED
                );
            }

            case PAYMENT -> {
                application.setAdmissionStatus(
                        ErpApplication.AdmissionStatus.PENDING
                );
                application.setWorkflowLocked(false);
            }

            case FINAL_ADMISSION, ENROLLED, CLOSED -> {
                // These stages are not valid RETURN targets in the validator.
            }
        }
    }

    private ErpApplicationStatusHistory createHistory(
            ErpApplication application,
            ErpApplication.CurrentStage previousStage,
            ApplicationStageTransitionRequestDTO request,
            Long userId,
            boolean emailRequired
    ) {
        ErpApplicationStatusHistory history =
                new ErpApplicationStatusHistory();

        history.setApplication(application);
        history.setStage(HISTORY_STAGE);
        history.setOldStatus(previousStage);
        history.setNewStatus(
                request.getTargetStage()
        );
        history.setChangedBy(userId);
        history.setPublicRemarks(
                trimToNull(
                        request.getPublicRemarks()
                )
        );
        history.setInternalRemarks(
                trimToNull(
                        request.getInternalRemarks()
                )
        );
        history.setTransitionSource(
                TRANSITION_SOURCE
        );
        history.setEmailRequired(emailRequired);
        history.setEmailStatus(
                emailRequired
                        ? EMAIL_PENDING
                        : ErpApplicationStatusHistory
                        .EMAIL_NOT_REQUIRED
        );
        history.setEmailType(
                emailRequired
                        ? EMAIL_TYPE
                        : null
        );

        return history;
    }

    private ApplicationStageTransitionResponseDTO toResponse(
            ErpApplication application,
            ErpApplication.CurrentStage previousStage,
            TransitionAction action,
            ErpApplicationStatusHistory history
    ) {
        ApplicationStageTransitionResponseDTO response =
                new ApplicationStageTransitionResponseDTO();

        response.setApplicationId(
                application.getApplicationId()
        );
        response.setApplicationNo(
                application.getApplicationNo()
        );

        response.setPreviousStage(previousStage);
        response.setCurrentStage(
                application.getCurrentStage()
        );
        response.setAction(action);

        response.setPublicRemarks(
                history.getPublicRemarks()
        );
        response.setInternalRemarks(
                history.getInternalRemarks()
        );

        response.setApplicationStatus(
                application.getApplicationStatus()
        );
        response.setVerificationStatus(
                application.getVerificationStatus()
        );
        response.setDocumentStatus(
                application.getDocumentStatus()
        );
        response.setTestStatus(
                application.getTestStatus()
        );
        response.setFeeDecisionStatus(
                application.getFeeDecisionStatus()
        );
        response.setScholarshipWorkflowStatus(
                application.getScholarshipStatus()
        );
        response.setPaymentStatus(
                application.getPaymentStatus()
        );
        response.setAdmissionStatus(
                application.getAdmissionStatus()
        );
        response.setWorkflowLocked(
                application.getWorkflowLocked()
        );

        response.setHistoryId(
                history.getHistoryId()
        );
        response.setHistoryStage(
                history.getStage()
        );
        response.setTransitionSource(
                history.getTransitionSource()
        );
        response.setChangedBy(
                history.getChangedBy()
        );
        response.setChangedAt(
                history.getChangedAt()
        );
        response.setEmailRequired(
                history.getEmailRequired()
        );
        response.setEmailStatus(
                history.getEmailStatus()
        );
        response.setEmailType(
                history.getEmailType()
        );
        response.setEmailSentAt(
                history.getEmailSentAt()
        );

        response.setAvailableTransitions(
                buildAvailableTransitions(
                        application
                )
        );

        return response;
    }

    private List<AvailableTransition> buildAvailableTransitions(
            ErpApplication application
    ) {
        List<AvailableTransition> transitions =
                new ArrayList<>();

        ErpApplication.CurrentStage currentStage =
                application.getCurrentStage();

        if (currentStage == null) {
            return transitions;
        }

        for (TransitionAction action
                : TransitionAction.values()) {

            if (!isActionAvailableForLockState(
                    application,
                    action
            )) {
                continue;
            }

            Set<ErpApplication.CurrentStage> targets =
                    transitionValidator.allowedTargets(
                            currentStage,
                            action
                    );

            for (ErpApplication.CurrentStage target
                    : targets) {
                transitions.add(
                        buildAvailableTransition(
                                application,
                                action,
                                target
                        )
                );
            }
        }

        return transitions;
    }

    private boolean isActionAvailableForLockState(
            ErpApplication application,
            TransitionAction action
    ) {
        if (!Boolean.TRUE.equals(
                application.getWorkflowLocked()
        )) {
            return true;
        }

        return action == TransitionAction.REOPEN
                || action == TransitionAction.CLOSE;
    }

    private AvailableTransition buildAvailableTransition(
            ErpApplication application,
            TransitionAction action,
            ErpApplication.CurrentStage target
    ) {
        AvailableTransition transition =
                new AvailableTransition();

        transition.setAction(action);
        transition.setTargetStage(target);
        transition.setLabel(
                buildActionLabel(
                        action,
                        target
                )
        );

        boolean notificationSupported =
                hasText(
                        application.getPrimaryEmail()
                );

        transition.setApplicantNotificationSupported(
                notificationSupported
        );
        transition.setApplicantNotificationRequired(false);
        transition.setPublicRemarksSupported(true);
        transition.setInternalRemarksSupported(true);
        transition.setRemarksRequired(
                action == TransitionAction.REJECT
        );

        return transition;
    }

    private String buildActionLabel(
            TransitionAction action,
            ErpApplication.CurrentStage target
    ) {
        if (action == TransitionAction.REJECT) {
            return "Reject application";
        }

        if (action == TransitionAction.CLOSE) {
            return "Close application";
        }

        if (action == TransitionAction.REOPEN) {
            return "Reopen for verification";
        }

        if (action == TransitionAction.RETURN) {
            return "Return to "
                    + humanizeStage(target);
        }

        return switch (target) {
            case APPLICATION_DRAFT ->
                    "Move to application draft";
            case APPLICATION_VERIFICATION ->
                    "Start application verification";
            case SCHOOL_VISIT ->
                    "Move to school visit";
            case ENTRANCE_TEST ->
                    "Move to entrance test";
            case PARENT_FEE_DISCUSSION ->
                    "Move to parent fee discussion";
            case SCHOLARSHIP ->
                    "Send for scholarship review";
            case PAYMENT ->
                    "Move to payment";
            case FINAL_ADMISSION ->
                    "Move to final admission";
            case ENROLLED ->
                    "Mark as enrolled";
            case CLOSED ->
                    "Close application";
        };
    }

    private String humanizeStage(
            ErpApplication.CurrentStage stage
    ) {
        String normalized =
                stage.name()
                        .toLowerCase(Locale.ROOT)
                        .replace('_', ' ');

        return Character.toUpperCase(
                normalized.charAt(0)
        ) + normalized.substring(1);
    }

    private boolean shouldNotifyApplicant(
            ErpApplication application,
            ApplicationStageTransitionRequestDTO request
    ) {
        if (!Boolean.TRUE.equals(
                request.getNotifyApplicant()
        )) {
            return false;
        }

        if (!hasText(
                application.getPrimaryEmail()
        )) {
            throw new BadRequestException(
                    "Applicant notification was requested, "
                            + "but the application has no email address."
            );
        }

        return true;
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

    private boolean hasMeaningfulScholarshipStatus(
            String value
    ) {
        String normalized =
                normalizeStatus(value);

        return !normalized.isEmpty()
                && !normalized.equals("NOT_APPLIED");
    }

    private String normalizeStatus(
            String value
    ) {
        return value == null
                ? ""
                : value.trim()
                .toUpperCase(Locale.ROOT);
    }

    private String firstNonBlank(
            String... values
    ) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (hasText(value)) {
                return value.trim();
            }
        }

        return null;
    }

    private String trimToNull(
            String value
    ) {
        return hasText(value)
                ? value.trim()
                : null;
    }

    private boolean hasText(
            String value
    ) {
        return value != null
                && !value.isBlank();
    }
}

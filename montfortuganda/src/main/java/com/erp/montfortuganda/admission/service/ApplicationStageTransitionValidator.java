package com.erp.montfortuganda.admission.service;

import com.erp.montfortuganda.admission.dto.ApplicationStageTransitionRequestDTO;
import com.erp.montfortuganda.admission.dto.ApplicationStageTransitionRequestDTO.TransitionAction;
import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Central validator for branch-admin admission workflow stage transitions.
 *
 * <p>This class is the single source of truth for allowed stage/action
 * combinations. Controllers and browsers must never decide whether a
 * transition is valid.</p>
 */
@Component
public class ApplicationStageTransitionValidator {

    private static final Map<ErpApplication.CurrentStage,
            Set<ErpApplication.CurrentStage>> ADVANCE_TRANSITIONS;

    private static final Map<ErpApplication.CurrentStage,
            Set<ErpApplication.CurrentStage>> RETURN_TRANSITIONS;

    private static final Map<ErpApplication.CurrentStage,
            Set<ErpApplication.CurrentStage>> REJECT_TRANSITIONS;

    private static final Map<ErpApplication.CurrentStage,
            Set<ErpApplication.CurrentStage>> CLOSE_TRANSITIONS;

    private static final Map<ErpApplication.CurrentStage,
            Set<ErpApplication.CurrentStage>> REOPEN_TRANSITIONS;

    static {
        ADVANCE_TRANSITIONS = buildAdvanceTransitions();
        RETURN_TRANSITIONS = buildReturnTransitions();
        REJECT_TRANSITIONS = buildRejectTransitions();
        CLOSE_TRANSITIONS = buildCloseTransitions();
        REOPEN_TRANSITIONS = buildReopenTransitions();
    }

    /**
     * Validates a requested transition against the stage currently stored in
     * the locked application record.
     */
    public void validate(
            ErpApplication.CurrentStage actualCurrentStage,
            ApplicationStageTransitionRequestDTO request
    ) {
        if (actualCurrentStage == null) {
            throw new BadRequestException(
                    "The application does not have a valid current workflow stage."
            );
        }

        if (request == null) {
            throw new BadRequestException(
                    "Workflow transition request is required."
            );
        }

        if (request.getExpectedCurrentStage() == null) {
            throw new BadRequestException(
                    "Expected current stage is required."
            );
        }

        if (request.getTargetStage() == null) {
            throw new BadRequestException(
                    "Target stage is required."
            );
        }

        if (request.getAction() == null) {
            throw new BadRequestException(
                    "Workflow action is required."
            );
        }

        if (request.getExpectedCurrentStage() != actualCurrentStage) {
            throw new BadRequestException(
                    "The application stage has changed. Refresh the application and try again."
            );
        }

        if (request.getTargetStage() == actualCurrentStage) {
            throw new BadRequestException(
                    "The target stage must be different from the current stage."
            );
        }

        if (!isAllowed(
                actualCurrentStage,
                request.getTargetStage(),
                request.getAction()
        )) {
            throw new BadRequestException(
                    "The requested workflow transition is not allowed: "
                            + actualCurrentStage.name()
                            + " -> "
                            + request.getTargetStage().name()
                            + " using "
                            + request.getAction().name()
                            + "."
            );
        }

        validateRequiredRemarks(request);
    }

    /**
     * Returns whether the supplied transition is explicitly permitted.
     */
    public boolean isAllowed(
            ErpApplication.CurrentStage currentStage,
            ErpApplication.CurrentStage targetStage,
            TransitionAction action
    ) {
        if (currentStage == null || targetStage == null || action == null) {
            return false;
        }

        return transitionsFor(action)
                .getOrDefault(currentStage, Collections.emptySet())
                .contains(targetStage);
    }

    /**
     * Returns the permitted targets for one current stage and action.
     * The returned set is immutable.
     */
    public Set<ErpApplication.CurrentStage> allowedTargets(
            ErpApplication.CurrentStage currentStage,
            TransitionAction action
    ) {
        if (currentStage == null || action == null) {
            return Collections.emptySet();
        }

        return transitionsFor(action)
                .getOrDefault(currentStage, Collections.emptySet());
    }

    private void validateRequiredRemarks(
            ApplicationStageTransitionRequestDTO request
    ) {
        if (request.getAction() != TransitionAction.REJECT) {
            return;
        }

        if (hasText(request.getPublicRemarks())
                || hasText(request.getInternalRemarks())) {
            return;
        }

        throw new BadRequestException(
                "Public remarks or internal remarks are required when rejecting an application."
        );
    }

    private Map<ErpApplication.CurrentStage,
            Set<ErpApplication.CurrentStage>> transitionsFor(
            TransitionAction action
    ) {
        return switch (action) {
            case ADVANCE -> ADVANCE_TRANSITIONS;
            case RETURN -> RETURN_TRANSITIONS;
            case REJECT -> REJECT_TRANSITIONS;
            case CLOSE -> CLOSE_TRANSITIONS;
            case REOPEN -> REOPEN_TRANSITIONS;
        };
    }

    private static Map<ErpApplication.CurrentStage,
            Set<ErpApplication.CurrentStage>> buildAdvanceTransitions() {
        EnumMap<ErpApplication.CurrentStage,
                Set<ErpApplication.CurrentStage>> transitions =
                new EnumMap<>(ErpApplication.CurrentStage.class);

        allow(
                transitions,
                ErpApplication.CurrentStage.APPLICATION_DRAFT,
                ErpApplication.CurrentStage.APPLICATION_VERIFICATION
        );
        allow(
                transitions,
                ErpApplication.CurrentStage.APPLICATION_VERIFICATION,
                ErpApplication.CurrentStage.SCHOOL_VISIT
        );
        allow(
                transitions,
                ErpApplication.CurrentStage.SCHOOL_VISIT,
                ErpApplication.CurrentStage.ENTRANCE_TEST
        );
        allow(
                transitions,
                ErpApplication.CurrentStage.ENTRANCE_TEST,
                ErpApplication.CurrentStage.PARENT_FEE_DISCUSSION
        );
        allow(
                transitions,
                ErpApplication.CurrentStage.PARENT_FEE_DISCUSSION,
                ErpApplication.CurrentStage.PAYMENT,
                ErpApplication.CurrentStage.SCHOLARSHIP
        );
        allow(
                transitions,
                ErpApplication.CurrentStage.SCHOLARSHIP,
                ErpApplication.CurrentStage.PAYMENT
        );
        allow(
                transitions,
                ErpApplication.CurrentStage.PAYMENT,
                ErpApplication.CurrentStage.FINAL_ADMISSION
        );
        allow(
                transitions,
                ErpApplication.CurrentStage.FINAL_ADMISSION,
                ErpApplication.CurrentStage.ENROLLED
        );

        return immutableCopy(transitions);
    }

    private static Map<ErpApplication.CurrentStage,
            Set<ErpApplication.CurrentStage>> buildReturnTransitions() {
        EnumMap<ErpApplication.CurrentStage,
                Set<ErpApplication.CurrentStage>> transitions =
                new EnumMap<>(ErpApplication.CurrentStage.class);

        allow(
                transitions,
                ErpApplication.CurrentStage.APPLICATION_VERIFICATION,
                ErpApplication.CurrentStage.APPLICATION_DRAFT
        );
        allow(
                transitions,
                ErpApplication.CurrentStage.SCHOOL_VISIT,
                ErpApplication.CurrentStage.APPLICATION_VERIFICATION
        );
        allow(
                transitions,
                ErpApplication.CurrentStage.ENTRANCE_TEST,
                ErpApplication.CurrentStage.SCHOOL_VISIT
        );
        allow(
                transitions,
                ErpApplication.CurrentStage.PARENT_FEE_DISCUSSION,
                ErpApplication.CurrentStage.ENTRANCE_TEST
        );
        allow(
                transitions,
                ErpApplication.CurrentStage.SCHOLARSHIP,
                ErpApplication.CurrentStage.PARENT_FEE_DISCUSSION
        );
        allow(
                transitions,
                ErpApplication.CurrentStage.PAYMENT,
                ErpApplication.CurrentStage.PARENT_FEE_DISCUSSION,
                ErpApplication.CurrentStage.SCHOLARSHIP
        );
        allow(
                transitions,
                ErpApplication.CurrentStage.FINAL_ADMISSION,
                ErpApplication.CurrentStage.PAYMENT
        );

        return immutableCopy(transitions);
    }

    private static Map<ErpApplication.CurrentStage,
            Set<ErpApplication.CurrentStage>> buildRejectTransitions() {
        EnumMap<ErpApplication.CurrentStage,
                Set<ErpApplication.CurrentStage>> transitions =
                new EnumMap<>(ErpApplication.CurrentStage.class);

        allow(
                transitions,
                ErpApplication.CurrentStage.APPLICATION_VERIFICATION,
                ErpApplication.CurrentStage.CLOSED
        );
        allow(
                transitions,
                ErpApplication.CurrentStage.SCHOOL_VISIT,
                ErpApplication.CurrentStage.CLOSED
        );
        allow(
                transitions,
                ErpApplication.CurrentStage.ENTRANCE_TEST,
                ErpApplication.CurrentStage.CLOSED
        );
        allow(
                transitions,
                ErpApplication.CurrentStage.PARENT_FEE_DISCUSSION,
                ErpApplication.CurrentStage.CLOSED
        );
        allow(
                transitions,
                ErpApplication.CurrentStage.SCHOLARSHIP,
                ErpApplication.CurrentStage.CLOSED
        );
        allow(
                transitions,
                ErpApplication.CurrentStage.PAYMENT,
                ErpApplication.CurrentStage.CLOSED
        );
        allow(
                transitions,
                ErpApplication.CurrentStage.FINAL_ADMISSION,
                ErpApplication.CurrentStage.CLOSED
        );

        return immutableCopy(transitions);
    }

    private static Map<ErpApplication.CurrentStage,
            Set<ErpApplication.CurrentStage>> buildCloseTransitions() {
        EnumMap<ErpApplication.CurrentStage,
                Set<ErpApplication.CurrentStage>> transitions =
                new EnumMap<>(ErpApplication.CurrentStage.class);

        allow(
                transitions,
                ErpApplication.CurrentStage.ENROLLED,
                ErpApplication.CurrentStage.CLOSED
        );

        return immutableCopy(transitions);
    }

    private static Map<ErpApplication.CurrentStage,
            Set<ErpApplication.CurrentStage>> buildReopenTransitions() {
        EnumMap<ErpApplication.CurrentStage,
                Set<ErpApplication.CurrentStage>> transitions =
                new EnumMap<>(ErpApplication.CurrentStage.class);

        /*
         * Reopening always returns to verification. This prevents a closed
         * application from bypassing document and branch review stages.
         */
        allow(
                transitions,
                ErpApplication.CurrentStage.CLOSED,
                ErpApplication.CurrentStage.APPLICATION_VERIFICATION
        );

        return immutableCopy(transitions);
    }

    @SafeVarargs
    private static void allow(
            EnumMap<ErpApplication.CurrentStage,
                    Set<ErpApplication.CurrentStage>> transitions,
            ErpApplication.CurrentStage source,
            ErpApplication.CurrentStage... targets
    ) {
        EnumSet<ErpApplication.CurrentStage> allowed =
                EnumSet.noneOf(ErpApplication.CurrentStage.class);

        Collections.addAll(allowed, targets);
        transitions.put(source, Collections.unmodifiableSet(allowed));
    }

    private static Map<ErpApplication.CurrentStage,
            Set<ErpApplication.CurrentStage>> immutableCopy(
            EnumMap<ErpApplication.CurrentStage,
                    Set<ErpApplication.CurrentStage>> source
    ) {
        return Collections.unmodifiableMap(new EnumMap<>(source));
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}

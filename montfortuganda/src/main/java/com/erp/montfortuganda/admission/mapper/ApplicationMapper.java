package com.erp.montfortuganda.admission.mapper;

import com.erp.montfortuganda.admission.dto.ApplicationStageTransitionRequestDTO.TransitionAction;
import com.erp.montfortuganda.admission.dto.ApplicationSummaryDTO;
import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.school.entity.Level;
import com.erp.montfortuganda.school.entity.SchoolClass;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Maps admission applications to branch-facing list responses.
 *
 * <p>Reference names are resolved by the service in batches before this
 * mapper is called, preventing N+1 queries. The mapper also exposes one safe
 * primary list action based on the authoritative saved workflow state.</p>
 */
@Component
public class ApplicationMapper {

    public ApplicationSummaryDTO toSummaryDTO(
            ErpApplication application,
            SchoolClass schoolClass
    ) {
        ApplicationSummaryDTO response =
                new ApplicationSummaryDTO();

        response.setApplicationId(
                application.getApplicationId()
        );
        response.setApplicationNo(
                application.getApplicationNo()
        );
        response.setStudentName(
                buildStudentName(application)
        );
        response.setGender(
                application.getGender()
        );
        response.setLevelName(
                resolveLevelName(schoolClass)
        );
        response.setClassName(
                resolveClassName(schoolClass)
        );

        response.setApplicationStatus(
                application.getApplicationStatus()
        );
        response.setStatus(
                application.getApplicationStatus() == null
                        ? null
                        : application.getApplicationStatus().name()
        );

        response.setCurrentStage(
                application.getCurrentStage()
        );
        response.setDocumentStatus(
                application.getDocumentStatus()
        );
        response.setVerificationStatus(
                application.getVerificationStatus()
        );
        response.setScholarshipStatus(
                application.getScholarshipStatus()
        );
        response.setAdmissionStatus(
                application.getAdmissionStatus()
        );
        response.setWorkflowLocked(
                application.getWorkflowLocked()
        );

        response.setSubmittedDate(
                application.getCreatedAt()
        );
        response.setUpdatedAt(
                application.getUpdatedAt()
        );

        populatePrimaryAction(
                response,
                application
        );

        return response;
    }

    /**
     * Compatibility overload for any older caller. It intentionally returns
     * a neutral Class label instead of exposing a raw Class ID.
     */
    public ApplicationSummaryDTO toSummaryDTO(
            ErpApplication application
    ) {
        return toSummaryDTO(
                application,
                null
        );
    }

    /**
     * Supplies one non-destructive list action only when the saved application
     * state is ready for that transition.
     *
     * <p>Return, reject, close, and reopen remain inside the full profile.
     * When prerequisites are incomplete, the label tells the Branch Admin
     * which profile area needs attention, while nextActionAvailable remains
     * false so the list does not bypass domain validation.</p>
     */
    private void populatePrimaryAction(
            ApplicationSummaryDTO response,
            ErpApplication application
    ) {
        response.setNextAction(null);
        response.setNextTargetStage(null);
        response.setNextActionAvailable(false);
        response.setNextActionLabel("Open profile");

        ErpApplication.CurrentStage currentStage =
                application.getCurrentStage();

        if (currentStage == null) {
            return;
        }

        if (Boolean.TRUE.equals(
                application.getWorkflowLocked()
        )) {
            response.setNextActionLabel(
                    currentStage
                            == ErpApplication.CurrentStage.ENROLLED
                            ? "View enrolled application"
                            : "View locked application"
            );
            return;
        }

        switch (currentStage) {
            case APPLICATION_DRAFT ->
                    setAdvanceAction(
                            response,
                            ErpApplication.CurrentStage
                                    .APPLICATION_VERIFICATION,
                            "Start verification"
                    );

            case APPLICATION_VERIFICATION -> {
                if (application.getDocumentStatus()
                        == ErpApplication.DocumentStatus.VERIFIED) {
                    setAdvanceAction(
                            response,
                            ErpApplication.CurrentStage.SCHOOL_VISIT,
                            "Move to school visit"
                    );
                } else {
                    response.setNextActionLabel(
                            "Review documents"
                    );
                }
            }

            case SCHOOL_VISIT ->
                    setAdvanceAction(
                            response,
                            ErpApplication.CurrentStage.ENTRANCE_TEST,
                            "Move to entrance test"
                    );

            case ENTRANCE_TEST -> {
                ErpApplication.TestStatus testStatus =
                        application.getTestStatus();

                if (testStatus
                        == ErpApplication.TestStatus.PASSED
                        || testStatus
                        == ErpApplication.TestStatus.COMPLETED) {
                    setAdvanceAction(
                            response,
                            ErpApplication.CurrentStage
                                    .PARENT_FEE_DISCUSSION,
                            "Start fee discussion"
                    );
                } else {
                    response.setNextActionLabel(
                            "Manage entrance test"
                    );
                }
            }

            case PARENT_FEE_DISCUSSION -> {
                if (application.getFeeDecisionStatus()
                        == ErpApplication.FeeDecisionStatus
                        .SCHOLARSHIP_REQUESTED) {
                    setAdvanceAction(
                            response,
                            ErpApplication.CurrentStage.SCHOLARSHIP,
                            "Open scholarship review"
                    );
                } else {
                    setAdvanceAction(
                            response,
                            ErpApplication.CurrentStage.PAYMENT,
                            "Move to payment"
                    );
                }
            }

            case SCHOLARSHIP -> {
                if (isScholarshipDecisionCompleted(
                        application.getScholarshipStatus()
                )) {
                    setAdvanceAction(
                            response,
                            ErpApplication.CurrentStage.PAYMENT,
                            "Move to payment"
                    );
                } else {
                    response.setNextActionLabel(
                            "Review scholarship"
                    );
                }
            }

            case PAYMENT -> {
                ErpApplication.PaymentStatus paymentStatus =
                        application.getPaymentStatus();

                if (paymentStatus
                        == ErpApplication.PaymentStatus.PAID
                        || paymentStatus
                        == ErpApplication.PaymentStatus.COMPLETED) {
                    setAdvanceAction(
                            response,
                            ErpApplication.CurrentStage.FINAL_ADMISSION,
                            "Approve final admission"
                    );
                } else {
                    response.setNextActionLabel(
                            "Review payment"
                    );
                }
            }

            case FINAL_ADMISSION -> {
                boolean readyForEnrollment =
                        application.getAdmissionStatus()
                                == ErpApplication.AdmissionStatus.APPROVED
                                && Boolean.TRUE.equals(
                                application.getStudentCreated()
                        );

                if (readyForEnrollment) {
                    setAdvanceAction(
                            response,
                            ErpApplication.CurrentStage.ENROLLED,
                            "Mark as enrolled"
                    );
                } else {
                    response.setNextActionLabel(
                            Boolean.TRUE.equals(
                                    application.getStudentCreated()
                            )
                                    ? "Review final admission"
                                    : "Create student record"
                    );
                }
            }

            case ENROLLED ->
                    response.setNextActionLabel(
                            "View enrolled application"
                    );

            case CLOSED ->
                    response.setNextActionLabel(
                            "View closed application"
                    );
        }
    }

    private void setAdvanceAction(
            ApplicationSummaryDTO response,
            ErpApplication.CurrentStage targetStage,
            String label
    ) {
        response.setNextAction(
                TransitionAction.ADVANCE
        );
        response.setNextTargetStage(targetStage);
        response.setNextActionLabel(label);
        response.setNextActionAvailable(true);
    }

    private boolean isScholarshipDecisionCompleted(
            String scholarshipStatus
    ) {
        if (!StringUtils.hasText(
                scholarshipStatus
        )) {
            return false;
        }

        String normalized =
                scholarshipStatus.trim()
                        .toUpperCase();

        return normalized.equals("APPROVED")
                || normalized.equals("REJECTED")
                || normalized.equals("DECLINED")
                || normalized.equals("COMPLETED")
                || normalized.equals("NOT_APPROVED");
    }

    private String buildStudentName(
            ErpApplication application
    ) {
        StringBuilder name =
                new StringBuilder();

        appendName(
                name,
                application.getFirstName()
        );
        appendName(
                name,
                application.getMiddleName()
        );
        appendName(
                name,
                application.getLastName()
        );

        return name.isEmpty()
                ? "Not Available"
                : name.toString();
    }

    private void appendName(
            StringBuilder target,
            String value
    ) {
        if (!StringUtils.hasText(value)) {
            return;
        }

        if (!target.isEmpty()) {
            target.append(' ');
        }

        target.append(value.trim());
    }

    private String resolveLevelName(
            SchoolClass schoolClass
    ) {
        if (schoolClass == null) {
            return "Not Available";
        }

        Level level =
                schoolClass.getLevel();

        if (level == null
                || !StringUtils.hasText(
                level.getLevelName()
        )) {
            return "Not Available";
        }

        return level.getLevelName().trim();
    }

    private String resolveClassName(
            SchoolClass schoolClass
    ) {
        if (schoolClass == null) {
            return "Not Available";
        }

        if (StringUtils.hasText(
                schoolClass.getClassName()
        )) {
            return schoolClass
                    .getClassName()
                    .trim();
        }

        if (StringUtils.hasText(
                schoolClass.getClassCode()
        )) {
            return schoolClass
                    .getClassCode()
                    .trim();
        }

        return "Not Available";
    }
}

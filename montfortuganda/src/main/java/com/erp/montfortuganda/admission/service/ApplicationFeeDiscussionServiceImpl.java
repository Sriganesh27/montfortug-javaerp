package com.erp.montfortuganda.admission.service;

import com.erp.montfortuganda.admission.dto.ApplicationFeeDiscussionRequestDTO;
import com.erp.montfortuganda.admission.dto.ApplicationFeeDiscussionResponseDTO;
import com.erp.montfortuganda.admission.dto.ApplicationStageTransitionRequestDTO;
import com.erp.montfortuganda.admission.dto.ApplicationStageTransitionResponseDTO;
import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.admission.entity.ErpApplicationFee;
import com.erp.montfortuganda.admission.entity.ErpApplicationFeeHistory;
import com.erp.montfortuganda.admission.repository.ErpApplicationFeeRepository;
import com.erp.montfortuganda.admission.repository.ErpApplicationFeeHistoryRepository;
import com.erp.montfortuganda.admission.repository.ErpApplicationRepository;
import com.erp.montfortuganda.auth.service.BranchAccessService;
import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.scholarship.entity.ErpScholarshipApplication;
import com.erp.montfortuganda.scholarship.entity.ErpScholarshipHistory;
import com.erp.montfortuganda.scholarship.repository.ErpScholarshipApplicationRepository;
import com.erp.montfortuganda.scholarship.repository.ErpScholarshipHistoryRepository;
import com.erp.montfortuganda.school.entity.ErpAcademicYear;
import com.erp.montfortuganda.school.repository.AcademicYearRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class ApplicationFeeDiscussionServiceImpl
        implements ApplicationFeeDiscussionService {

    private final ErpApplicationRepository applicationRepository;
    private final ErpApplicationFeeRepository applicationFeeRepository;
    private final ErpApplicationFeeHistoryRepository applicationFeeHistoryRepository;
    private final ErpScholarshipApplicationRepository scholarshipApplicationRepository;
    private final ErpScholarshipHistoryRepository scholarshipHistoryRepository;
    private final AcademicYearRepository academicYearRepository;
    private final BranchAccessService branchAccessService;
    private final ApplicationStageTransitionService stageTransitionService;

    public ApplicationFeeDiscussionServiceImpl(
            ErpApplicationRepository applicationRepository,
            ErpApplicationFeeRepository applicationFeeRepository,
            ErpApplicationFeeHistoryRepository applicationFeeHistoryRepository,
            ErpScholarshipApplicationRepository scholarshipApplicationRepository,
            ErpScholarshipHistoryRepository scholarshipHistoryRepository,
            AcademicYearRepository academicYearRepository,
            BranchAccessService branchAccessService,
            ApplicationStageTransitionService stageTransitionService
    ) {
        this.applicationRepository = applicationRepository;
        this.applicationFeeRepository = applicationFeeRepository;
        this.applicationFeeHistoryRepository =
                applicationFeeHistoryRepository;
        this.scholarshipApplicationRepository =
                scholarshipApplicationRepository;
        this.scholarshipHistoryRepository =
                scholarshipHistoryRepository;
        this.academicYearRepository =
                academicYearRepository;
        this.branchAccessService =
                branchAccessService;
        this.stageTransitionService =
                stageTransitionService;
    }

    @Override
    public ApplicationFeeDiscussionResponseDTO getFeeDiscussion(
            CurrentUserContext context,
            Long applicationId
    ) {
        Integer branchId = requireBranchId(context);
        Long safeApplicationId =
                requirePositiveId(applicationId, "Application ID");

        ErpApplication application =
                applicationRepository
                        .findActiveBranchApplication(
                                safeApplicationId,
                                branchId
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Application not found."
                                )
                        );

        return applicationFeeRepository
                .findActiveByApplicationAndBranch(
                        safeApplicationId,
                        branchId
                )
                .map(this::toResponse)
                .orElseGet(
                        () -> emptyResponse(application)
                );
    }

    @Override
    @Transactional
    public ApplicationFeeDiscussionResponseDTO saveFeeDiscussion(
            CurrentUserContext context,
            Long applicationId,
            ApplicationFeeDiscussionRequestDTO request
    ) {
        Integer branchId = requireBranchId(context);
        Integer userId = requireUserId(context);

        Long safeApplicationId =
                requirePositiveId(applicationId, "Application ID");

        if (request == null) {
            throw new BadRequestException(
                    "Fee discussion details are required."
            );
        }

        ErpApplication application =
                applicationRepository
                        .findActiveBranchApplicationForUpdate(
                                safeApplicationId,
                                branchId
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Application not found."
                                )
                        );

        requireFeeDiscussionEditableStage(
                application
        );
        requireWorkflowEditable(application);

        BigDecimal termFee =
                amountOrZero(request.termFee());
        BigDecimal transportFee =
                amountOrZero(request.transportFee());
        BigDecimal hostelFee =
                amountOrZero(request.hostelFee());
        BigDecimal uniformFee =
                amountOrZero(request.uniformFee());
        BigDecimal booksFee =
                amountOrZero(request.booksFee());
        BigDecimal admissionFee =
                amountOrZero(request.admissionFee());
        BigDecimal otherFee =
                amountOrZero(request.otherFee());

        validateAmount(termFee, "Term fee");
        validateAmount(transportFee, "Transport fee");
        validateAmount(hostelFee, "Hostel fee");
        validateAmount(uniformFee, "Uniform fee");
        validateAmount(booksFee, "Books fee");
        validateAmount(admissionFee, "Admission fee");
        validateAmount(otherFee, "Other fee");

        BigDecimal baseFeeAmount =
                termFee
                        .add(transportFee)
                        .add(hostelFee)
                        .add(uniformFee)
                        .add(booksFee)
                        .add(admissionFee)
                        .add(otherFee);

        BigDecimal parentCanPay =
                amountOrZero(request.parentCanPay());

        validateAmount(
                parentCanPay,
                "Parent can pay amount"
        );

        if (parentCanPay.compareTo(baseFeeAmount) > 0) {
            throw new BadRequestException(
                    "Parent can pay amount cannot exceed the total fee."
            );
        }

        BigDecimal assistanceRequired =
                baseFeeAmount
                        .subtract(parentCanPay)
                        .max(BigDecimal.ZERO);

        ErpApplicationFee.FeeDecision feeDecision =
                request.feeDecision() == null
                        ? ErpApplicationFee.FeeDecision.PENDING
                        : request.feeDecision();

        validateFeeDecision(
                feeDecision,
                baseFeeAmount,
                parentCanPay,
                assistanceRequired
        );

        LocalDateTime discussionDate =
                LocalDateTime.now();

        var existingFee =
                applicationFeeRepository
                        .findActiveByApplicationAndBranchForUpdate(
                                safeApplicationId,
                                branchId
                        );

        ErpApplicationFee fee =
                existingFee.orElseGet(
                        () -> createNewFeeRecord(
                                application,
                                userId
                        )
                );

        if (
                application.getCurrentStage()
                        == ErpApplication.CurrentStage.PAYMENT
                && amountOrZero(
                        fee.getAmountPaid()
                ).compareTo(BigDecimal.ZERO) > 0
        ) {
            throw new BadRequestException(
                    "The fee structure cannot be changed after a payment has been collected."
            );
        }

        if (existingFee.isPresent()) {
            String changeReason =
                    trimToNull(
                            request.changeReason()
                    );

            if (changeReason == null) {
                throw new BadRequestException(
                        "Reason for changing the existing fee structure is required."
                );
            }

            saveFeeHistorySnapshot(
                    fee,
                    application,
                    branchId,
                    userId,
                    changeReason
            );
        }

        fee.setTermFee(termFee);
        fee.setTransportFee(transportFee);
        fee.setHostelFee(hostelFee);
        fee.setUniformFee(uniformFee);
        fee.setBooksFee(booksFee);
        fee.setAdmissionFee(admissionFee);
        fee.setOtherFee(otherFee);

        fee.setBaseFeeAmount(baseFeeAmount);
        fee.setParentCanPay(parentCanPay);
        fee.setAssistanceRequired(
                assistanceRequired
        );
        fee.setFeeDecision(feeDecision);
        fee.setDiscussionDate(
                discussionDate
        );
        fee.setDiscussionRemarks(
                trimToNull(
                        request.discussionRemarks()
                )
        );

        BigDecimal scholarshipDiscount =
                amountOrZero(
                        fee.getScholarshipDiscount()
                );

        if (scholarshipDiscount.compareTo(baseFeeAmount) > 0) {
            scholarshipDiscount = baseFeeAmount;
            fee.setScholarshipDiscount(
                    scholarshipDiscount
            );
        }

        fee.setFinalPayable(
                baseFeeAmount.subtract(
                        scholarshipDiscount
                )
        );

        fee.setUpdatedBy(userId.longValue());
        fee.setActive(true);

        /*
         * Saving the discussion does not itself move the workflow.
         * The dedicated workflow action will later decide whether the
         * application advances to PAYMENT or SCHOLARSHIP.
         */
        application.setFeeDecisionStatus(
                switch (feeDecision) {
                    case PENDING ->
                            ErpApplication.FeeDecisionStatus.DECISION_PENDING;

                    case FULL_PAYMENT ->
                            ErpApplication.FeeDecisionStatus.FEE_ACCEPTED;

                    case PARTIAL_ASSISTANCE,
                         FULL_ASSISTANCE ->
                            ErpApplication.FeeDecisionStatus.SCHOLARSHIP_REQUESTED;
                }
        );
        application.setUpdatedBy(userId.longValue());

        syncScholarshipApplication(
                application,
                branchId,
                userId,
                feeDecision,
                baseFeeAmount,
                parentCanPay,
                assistanceRequired
        );

        applicationRepository.saveAndFlush(application);

        ErpApplicationFee saved =
                applicationFeeRepository.saveAndFlush(fee);

        /*
         * Saving/updating Fee Discussion is deliberately non-transitional.
         *
         * Staff may keep entering or revising the fee discussion while the
         * application remains in PARENT_FEE_DISCUSSION. Movement to PAYMENT
         * or SCHOLARSHIP must happen only through an explicit Finalize /
         * Continue workflow action.
         */

        return toResponse(saved);
    }

    @Override
    @Transactional
    public ApplicationStageTransitionResponseDTO finalizeFeeDiscussion(
            CurrentUserContext context,
            Long applicationId
    ) {
        Integer branchId =
                requireBranchId(context);

        Long safeApplicationId =
                requirePositiveId(
                        applicationId,
                        "Application ID"
                );

        ErpApplication application =
                applicationRepository
                        .findActiveBranchApplicationForUpdate(
                                safeApplicationId,
                                branchId
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Application not found."
                                )
                        );

        requireWorkflowEditable(application);

        if (application.getCurrentStage()
                != ErpApplication.CurrentStage.PARENT_FEE_DISCUSSION) {
            throw new BadRequestException(
                    "Fee Discussion can be finalized only while the application "
                            + "is in the Parent Fee Discussion stage."
            );
        }

        ErpApplicationFee fee =
                applicationFeeRepository
                        .findActiveByApplicationAndBranchForUpdate(
                                safeApplicationId,
                                branchId
                        )
                        .orElseThrow(
                                () -> new BadRequestException(
                                        "Save the Fee Discussion before finalizing it."
                                )
                        );

        ErpApplicationFee.FeeDecision feeDecision =
                fee.getFeeDecision();

        if (feeDecision == null
                || feeDecision
                == ErpApplicationFee.FeeDecision.PENDING) {
            throw new BadRequestException(
                    "Select a final Fee Discussion decision before continuing."
            );
        }

        ErpApplication.CurrentStage targetStage =
                switch (feeDecision) {
                    case FULL_PAYMENT ->
                            ErpApplication.CurrentStage.PAYMENT;

                    case PARTIAL_ASSISTANCE,
                         FULL_ASSISTANCE ->
                            ErpApplication.CurrentStage.SCHOLARSHIP;

                    case PENDING ->
                            throw new BadRequestException(
                                    "Pending Fee Discussion cannot be finalized."
                            );
                };

        ApplicationStageTransitionRequestDTO transitionRequest =
                new ApplicationStageTransitionRequestDTO();

        transitionRequest.setExpectedCurrentStage(
                ErpApplication.CurrentStage.PARENT_FEE_DISCUSSION
        );
        transitionRequest.setTargetStage(
                targetStage
        );
        transitionRequest.setAction(
                ApplicationStageTransitionRequestDTO
                        .TransitionAction
                        .ADVANCE
        );

        /*
         * Finalizing Fee Discussion is an internal workflow decision.
         * Payment and scholarship communications remain owned by their
         * dedicated workflow actions.
         */
        transitionRequest.setNotifyApplicant(false);

        return stageTransitionService.transition(
                context,
                safeApplicationId,
                transitionRequest
        );
    }

    private void syncScholarshipApplication(
            ErpApplication application,
            Integer branchId,
            Integer userId,
            ErpApplicationFee.FeeDecision feeDecision,
            BigDecimal baseFeeAmount,
            BigDecimal parentCanPay,
            BigDecimal assistanceRequired
    ) {
        boolean scholarshipRequired =
                feeDecision == ErpApplicationFee.FeeDecision.PARTIAL_ASSISTANCE
                        || feeDecision == ErpApplicationFee.FeeDecision.FULL_ASSISTANCE;

        var existing =
                scholarshipApplicationRepository
                        .findActiveByApplicationAndBranchForUpdate(
                                application.getApplicationId(),
                                branchId.longValue()
                        );

        if (!scholarshipRequired) {
            existing.ifPresent(scholarship -> {
                String currentStatus =
                        normalizeScholarshipStatus(scholarship.getStatus());

                if (isScholarshipProgressLocked(currentStatus)) {
                    throw new BadRequestException(
                            "The scholarship application has already progressed beyond the initial stage. "
                                    + "The fee decision cannot be changed to Full Payment from Fee Discussion."
                    );
                }

                scholarship.setPublicTokenHash(null);
                scholarship.setTokenExpiresAt(null);
                scholarship.setTokenUsedAt(null);
                scholarship.setSchoolAccessTokenHash(null);
                scholarship.setSchoolAccessExpiresAt(null);
                scholarship.setSchoolAccessIssuedAt(null);
                scholarship.setSchoolAccessIssuedBy(null);
                scholarship.setStatus("CANCELLED_BY_FEE_DECISION");
                scholarship.setUpdatedBy(userId.longValue());

                scholarshipApplicationRepository.save(scholarship);

                scholarshipHistoryRepository
                        .findFirstByScholarshipApplicationScholarshipAppIdOrderByScholarshipHistoryIdDesc(
                                scholarship.getScholarshipAppId()
                        )
                        .ifPresent(history -> {
                            history.setStatus("CANCELLED_BY_FEE_DECISION");
                            history.setUpdatedBy(userId.longValue());
                            history.setUpdatedAt(LocalDateTime.now());
                            scholarshipHistoryRepository.save(history);
                        });
            });
            return;
        }

        if (baseFeeAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
                    "Total fee must be greater than 0 before requesting scholarship assistance."
            );
        }

        String academicYearCode =
                resolveAcademicYearCode(application, branchId);

        BigDecimal requestedPercentage =
                assistanceRequired
                        .multiply(BigDecimal.valueOf(100))
                        .divide(
                                baseFeeAmount,
                                2,
                                java.math.RoundingMode.HALF_UP
                        );

        ErpScholarshipApplication scholarship =
                existing.orElseGet(ErpScholarshipApplication::new);

        if (scholarship.getScholarshipAppId() == null) {
            scholarship.setApplication(application);
            scholarship.setBranchId(branchId.longValue());
            scholarship.setCreatedBy(userId.longValue());
        }

        /*
         * The Scholarship Application academic year belongs to the Scholarship
         * Application record itself. It is set only when that record is first
         * created. A later requested year belongs to Scholarship History and
         * must never overwrite the Scholarship Application year.
         */
        if (scholarship.getScholarshipAppId() == null) {
            scholarship.setAcademicYear(academicYearCode);
        }

        if (scholarship.getScholarshipAppId() != null
                && isScholarshipProgressLocked(
                        normalizeScholarshipStatus(scholarship.getStatus()))) {
            throw new BadRequestException(
                    "The scholarship application has already been submitted or reviewed. "
                            + "Fee Discussion can no longer overwrite its scholarship request."
            );
        }

        /*
         * Scholarship request/review data belongs to History.
         * Application remains the master record for identity, tokens and
         * lifecycle/audit data.
         */
        String termRequested =
                normalizeTermRequested(application.getTerm());

        /*
         * Scholarship History is a year/term snapshot. A later requested
         * academic year must receive a new History record rather than
         * overwriting the previous year's snapshot. The same year + term may
         * reuse its existing current-cycle record.
         */
        ErpScholarshipHistory history =
                scholarship.getScholarshipAppId() == null
                        ? new ErpScholarshipHistory()
                        : scholarshipHistoryRepository
                                .findFirstByScholarshipApplicationScholarshipAppIdAndAcademicYearAndTermRequestedOrderByScholarshipHistoryIdDesc(
                                        scholarship.getScholarshipAppId(),
                                        academicYearCode,
                                        termRequested
                                )
                                .orElseGet(ErpScholarshipHistory::new);

        if (history.getScholarshipHistoryId() == null) {
            history.setScholarshipApplication(scholarship);
            history.setBranchId(branchId.longValue());
            history.setStudentId(
                    scholarship.getStudent() != null
                            ? scholarship.getStudent().getStudentId()
                            : null
            );
            history.setApplicationId(application.getApplicationId());
            history.setAcademicYear(academicYearCode);
            history.setCreatedBy(userId.longValue());
            history.setCreatedAt(LocalDateTime.now());
        }

        history.setTermRequested(termRequested);
        history.setCategory("NEED_BASED");
        history.setScholarshipType(
                ErpScholarshipHistory.ScholarshipType.NEED_BASED
        );
        history.setApplicationMethod("SCHOOL_ASSISTED");
        history.setAmountRequestedUgx(assistanceRequired);
        history.setRequestedPercentage(requestedPercentage);
        history.setReason(
                feeDecision == ErpApplicationFee.FeeDecision.FULL_ASSISTANCE
                        ? "Full scholarship requested during parent fee discussion."
                        : "Partial scholarship requested during parent fee discussion."
        );
        history.setStatus("NOT_STARTED");
        history.setActive(true);
        history.setUpdatedBy(userId.longValue());
        history.setUpdatedAt(LocalDateTime.now());

        /*
         * ErpScholarshipHistory has a non-nullable foreign key to
         * ErpScholarshipApplication. Persist a new Scholarship Application
         * first so Hibernate has its generated ID before the History insert.
         */
        scholarship.setStatus("NOT_STARTED");
        scholarship.setPublicTokenHash(null);
        scholarship.setTokenExpiresAt(null);
        scholarship.setTokenUsedAt(null);
        scholarship.setSchoolAccessTokenHash(null);
        scholarship.setSchoolAccessExpiresAt(null);
        scholarship.setSchoolAccessIssuedAt(null);
        scholarship.setSchoolAccessIssuedBy(null);
        scholarship.setActive(true);
        scholarship.setUpdatedBy(userId.longValue());

        ErpScholarshipApplication savedScholarship =
                scholarshipApplicationRepository.saveAndFlush(scholarship);

        history.setScholarshipApplication(savedScholarship);
        scholarshipHistoryRepository.saveAndFlush(history);
    }

    private boolean isScholarshipProgressLocked(
            String status
    ) {
        return switch (status) {
            case "SUBMITTED",
                 "UNDER_SCHOOL_REVIEW",
                 "SHORTLISTED",
                 "NOT_SHORTLISTED",
                 "UNDER_SUPERADMIN_REVIEW",
                 "APPROVED",
                 "PARTIALLY_APPROVED",
                 "REJECTED" -> true;

            default -> false;
        };
    }

    private String normalizeScholarshipStatus(
            String status
    ) {
        String value =
                trimToNull(status);

        return value == null
                ? ""
                : value.trim()
                        .toUpperCase(
                                java.util.Locale.ROOT
                        );
    }

    private String resolveAcademicYearCode(
            ErpApplication application,
            Integer branchId
    ) {
        if (application.getAcademicYearId() == null) {
            throw new BadRequestException(
                    "Academic year is unavailable for this application."
            );
        }

        ErpAcademicYear academicYear =
                academicYearRepository
                        .findByAcademicYearIdAndBranchBranchIdAndActiveTrue(
                                application.getAcademicYearId(),
                                branchId
                        )
                        .orElseThrow(
                                () -> new BadRequestException(
                                        "Academic year could not be resolved for this application."
                                )
                        );

        String code =
                trimToNull(
                        academicYear.getAcademicYearCode()
                );

        if (code == null) {
            throw new BadRequestException(
                    "Academic year code is unavailable."
            );
        }

        return code;
    }

    private String normalizeTermRequested(
            String term
    ) {
        String value =
                trimToNull(term);

        return value == null
                ? "TERM_1"
                : value;
    }

    private void saveFeeHistorySnapshot(
            ErpApplicationFee fee,
            ErpApplication application,
            Integer branchId,
            Integer userId,
            String changeReason
    ) {
        if (fee == null || fee.getFeeId() == null) {
            return;
        }

        ErpApplicationFeeHistory history =
                new ErpApplicationFeeHistory();

        history.setFeeId(
                fee.getFeeId()
        );
        history.setApplicationId(
                application.getApplicationId()
        );
        history.setBranchId(
                branchId
        );

        history.setTermFee(
                amountOrZero(fee.getTermFee())
        );
        history.setTransportFee(
                amountOrZero(fee.getTransportFee())
        );
        history.setHostelFee(
                amountOrZero(fee.getHostelFee())
        );
        history.setUniformFee(
                amountOrZero(fee.getUniformFee())
        );
        history.setBooksFee(
                amountOrZero(fee.getBooksFee())
        );
        history.setAdmissionFee(
                amountOrZero(fee.getAdmissionFee())
        );
        history.setOtherFee(
                amountOrZero(fee.getOtherFee())
        );

        history.setBaseFeeAmount(
                amountOrZero(fee.getBaseFeeAmount())
        );
        history.setParentCanPay(
                amountOrZero(fee.getParentCanPay())
        );
        history.setAssistanceRequired(
                amountOrZero(fee.getAssistanceRequired())
        );

        history.setFeeDecision(
                fee.getFeeDecision() == null
                        ? ErpApplicationFee.FeeDecision.PENDING
                        : fee.getFeeDecision()
        );

        history.setDiscussionRemarks(
                fee.getDiscussionRemarks()
        );

        history.setScholarshipDiscount(
                amountOrZero(
                        fee.getScholarshipDiscount()
                )
        );
        history.setFinalPayable(
                amountOrZero(
                        fee.getFinalPayable()
                )
        );
        history.setAmountPaid(
                amountOrZero(
                        fee.getAmountPaid()
                )
        );

        history.setPaymentStatus(
                fee.getPaymentStatus() == null
                        ? ErpApplicationFee.PaymentStatus.PENDING
                        : fee.getPaymentStatus()
        );

        history.setChangeReason(
                changeReason
        );
        history.setChangedBy(
                userId.longValue()
        );
        history.setChangedAt(
                LocalDateTime.now()
        );
        history.setPreviousVersion(
                fee.getVersion()
        );

        applicationFeeHistoryRepository.save(
                history
        );
    }

    private ErpApplicationFee createNewFeeRecord(
            ErpApplication application,
            Integer userId
    ) {
        ErpApplicationFee fee =
                new ErpApplicationFee();

        fee.setApplication(application);
        fee.setTermFee(BigDecimal.ZERO);
        fee.setTransportFee(BigDecimal.ZERO);
        fee.setHostelFee(BigDecimal.ZERO);
        fee.setUniformFee(BigDecimal.ZERO);
        fee.setBooksFee(BigDecimal.ZERO);
        fee.setAdmissionFee(BigDecimal.ZERO);
        fee.setOtherFee(BigDecimal.ZERO);

        fee.setBaseFeeAmount(BigDecimal.ZERO);
        fee.setParentCanPay(BigDecimal.ZERO);
        fee.setAssistanceRequired(BigDecimal.ZERO);
        fee.setFeeDecision(
                ErpApplicationFee.FeeDecision.PENDING
        );
        fee.setDiscussionDate(null);
        fee.setDiscussionRemarks(null);

        fee.setScholarshipDiscount(BigDecimal.ZERO);
        fee.setFinalPayable(BigDecimal.ZERO);
        fee.setAmountPaid(BigDecimal.ZERO);
        fee.setPaymentStatus(
                ErpApplicationFee.PaymentStatus.PENDING
        );

        fee.setActive(true);
        fee.setCreatedBy(userId.longValue());
        fee.setUpdatedBy(userId.longValue());

        return fee;
    }

    private ApplicationFeeDiscussionResponseDTO emptyResponse(
            ErpApplication application
    ) {
        return new ApplicationFeeDiscussionResponseDTO(
                null,
                application.getApplicationId(),

                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,

                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                ErpApplicationFee.FeeDecision.PENDING.name(),
                null,
                null,

                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,

                ErpApplicationFee.PaymentStatus.PENDING.name(),
                null
        );
    }

    private ApplicationFeeDiscussionResponseDTO toResponse(
            ErpApplicationFee fee
    ) {
        return new ApplicationFeeDiscussionResponseDTO(
                fee.getFeeId(),
                fee.getApplication().getApplicationId(),

                amountOrZero(fee.getTermFee()),
                amountOrZero(fee.getTransportFee()),
                amountOrZero(fee.getHostelFee()),
                amountOrZero(fee.getUniformFee()),
                amountOrZero(fee.getBooksFee()),
                amountOrZero(fee.getAdmissionFee()),
                amountOrZero(fee.getOtherFee()),

                amountOrZero(fee.getBaseFeeAmount()),
                amountOrZero(fee.getParentCanPay()),
                amountOrZero(fee.getAssistanceRequired()),
                fee.getFeeDecision() == null
                        ? ErpApplicationFee.FeeDecision.PENDING.name()
                        : fee.getFeeDecision().name(),
                fee.getDiscussionDate(),
                fee.getDiscussionRemarks(),

                amountOrZero(fee.getScholarshipDiscount()),
                amountOrZero(fee.getFinalPayable()),
                amountOrZero(fee.getAmountPaid()),

                fee.getPaymentStatus() == null
                        ? ErpApplicationFee.PaymentStatus.PENDING.name()
                        : fee.getPaymentStatus().name(),

                fee.getRemarks()
        );
    }

    private void validateFeeDecision(
            ErpApplicationFee.FeeDecision feeDecision,
            BigDecimal baseFeeAmount,
            BigDecimal parentCanPay,
            BigDecimal assistanceRequired
    ) {
        if (
                feeDecision
                        == ErpApplicationFee.FeeDecision.FULL_PAYMENT
                && parentCanPay.compareTo(baseFeeAmount) != 0
        ) {
            throw new BadRequestException(
                    "For Full Payment, Parent Can Pay must equal the Total Fee."
            );
        }

        if (
                feeDecision
                        == ErpApplicationFee.FeeDecision.PARTIAL_ASSISTANCE
                && (
                    parentCanPay.compareTo(BigDecimal.ZERO) <= 0
                    || assistanceRequired.compareTo(BigDecimal.ZERO) <= 0
                )
        ) {
            throw new BadRequestException(
                    "Partial Assistance requires the parent to pay part of the fee and assistance to cover the remaining amount."
            );
        }

        if (
                feeDecision
                        == ErpApplicationFee.FeeDecision.FULL_ASSISTANCE
                && parentCanPay.compareTo(BigDecimal.ZERO) != 0
        ) {
            throw new BadRequestException(
                    "For Full Assistance, Parent Can Pay must be 0."
            );
        }
    }

    private void requireFeeDiscussionEditableStage(
            ErpApplication application
    ) {
        ErpApplication.CurrentStage stage =
                application.getCurrentStage();

        if (
                stage
                        != ErpApplication.CurrentStage.PARENT_FEE_DISCUSSION
                && stage
                        != ErpApplication.CurrentStage.SCHOLARSHIP
                && stage
                        != ErpApplication.CurrentStage.PAYMENT
        ) {
            throw new BadRequestException(
                    "The fee structure can be edited only during Parent Fee Discussion, "
                            + "Scholarship, or before payment collection."
            );
        }
    }

    private void requireWorkflowEditable(
            ErpApplication application
    ) {
        if (Boolean.TRUE.equals(application.getWorkflowLocked())) {
            throw new BadRequestException(
                    "This application workflow is locked."
            );
        }

        if (
                application.getCurrentStage()
                        == ErpApplication.CurrentStage.ENROLLED
                        || application.getCurrentStage()
                        == ErpApplication.CurrentStage.CLOSED
        ) {
            throw new BadRequestException(
                    "This application can no longer be modified."
            );
        }
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
        if (
                context == null
                        || context.getUserId() == null
                        || context.getUserId() <= 0
        ) {
            throw new BadRequestException(
                    "Current user ID is unavailable."
            );
        }

        return context.getUserId();
    }

    private Long requirePositiveId(
            Long value,
            String fieldName
    ) {
        if (value == null || value <= 0L) {
            throw new BadRequestException(
                    fieldName + " must be a positive number."
            );
        }

        return value;
    }

    private BigDecimal amountOrZero(
            BigDecimal amount
    ) {
        return amount == null
                ? BigDecimal.ZERO
                : amount;
    }

    private void validateAmount(
            BigDecimal amount,
            String fieldName
    ) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(
                    fieldName + " cannot be negative."
            );
        }

        if (amount.scale() > 2) {
            throw new BadRequestException(
                    fieldName
                            + " cannot contain more than 2 decimal places."
            );
        }

        if (amount.precision() - amount.scale() > 10) {
            throw new BadRequestException(
                    fieldName
                            + " exceeds the maximum supported amount."
            );
        }
    }

    private String trimToNull(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String trimmed =
                value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }
}

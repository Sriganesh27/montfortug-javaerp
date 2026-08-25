package com.erp.montfortuganda.scholarship.service;

import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.admission.entity.ErpApplicationFee;
import com.erp.montfortuganda.admission.repository.ErpApplicationFeeRepository;
import com.erp.montfortuganda.scholarship.dto.*;
import com.erp.montfortuganda.scholarship.entity.*;
import com.erp.montfortuganda.scholarship.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class ScholarshipServiceImpl implements ScholarshipService {

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private static final long PUBLIC_TOKEN_VALID_HOURS = 72L;

    /*
     * School-assisted access uses the same 72-hour validity window as the
     * parent/public token. A fresh key can be reissued by Branch Admin.
     */
    private static final long SCHOOL_ACCESS_VALID_HOURS = 72L;

    private final WebDonationReadService donationReadService;
    private final ErpScholarshipApplicationRepository applicationRepo;
    private final ErpScholarshipSiblingRepository siblingRepo;
    private final ErpApplicationFeeRepository feeRepo;
    private final ErpScholarshipAllocationRepository allocationRepo;
    private final ErpBranchFundAllocationRepository branchFundRepo;
    private final CurrentUserService currentUserService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public FundsSummaryDTO getFundsSummary() {
        List<WebDonation> donations =
                donationReadService.findAll();

        // 1. Sum up ONLY successful payments, using the converted UGX amount (amount_received)
        // This safely ignores the NULL value in row 3!
        BigDecimal totalRaised = donations.stream()
                .filter(d -> "success".equalsIgnoreCase(d.getPaymentStatus()))
                .map(d -> d.getAmountReceived() != null ? d.getAmountReceived() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

        // 2. Sum up total spent
        BigDecimal totalSpent = donations.stream()
                .filter(d -> "success".equalsIgnoreCase(d.getPaymentStatus()))
                .map(d -> d.getAmountSpent() != null ? d.getAmountSpent() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

        // 3. Calculate available balance (UGX)
        BigDecimal available = totalRaised.subtract(totalSpent);

        // 4. Calculate total students sponsored
        int studentsSponsored = donations.stream()
                .filter(d -> "success".equalsIgnoreCase(d.getPaymentStatus()))
                .mapToInt(d -> d.getStudentsBenefited() != null ? d.getStudentsBenefited() : 0)
                .sum();

        return new FundsSummaryDTO(totalRaised, totalSpent, available, studentsSponsored);
    }

    @Override
    public List<DonorDTO> getAllDonors() {
        return donationReadService
                .findAll()
                .stream()
                .map(d -> {
            // Map the database entity directly to the exact fields JS is looking for
            return new DonorDTO(
                    d.getId(),
                    d.getReceiptNumber() != null ? d.getReceiptNumber() : "N/A",
                    d.getFullName(),
                    d.getEmail(),
                    d.getCurrency() != null ? d.getCurrency() : "UGX",
                    d.getAmount() != null ? d.getAmount() : BigDecimal.ZERO, // <-- ADDED THIS LINE
                    d.getAmountReceived() != null ? d.getAmountReceived() : BigDecimal.ZERO,
                    d.getAmountSpent() != null ? d.getAmountSpent() : BigDecimal.ZERO,
                    d.getStudentsBenefited() != null ? d.getStudentsBenefited() : 0,
                    "Term 1" // Default term until added to web_donations table
            );
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PendingStudentDTO> getPendingStudents() {
        /*
         * Submitted scholarship applications use the status SUBMITTED, not
         * the legacy literal "Pending". The previous filter therefore
         * returned an empty list even though active scholarship applications
         * existed in erp_scholarship_applications.
         *
         * Keep only active applications that have not reached a final
         * decision. This makes the endpoint compatible with the current
         * scholarship workflow while preserving the existing DTO contract.
         */
        List<ErpScholarshipApplication> apps =
                applicationRepo.findAll()
                        .stream()
                        .filter(app ->
                                Boolean.TRUE.equals(
                                        app.getActive()
                                )
                        )
                        .filter(app -> {
                            String status =
                                    app.getStatus() != null
                                            ? app.getStatus().trim()
                                            : "";

                            return !"APPROVED".equalsIgnoreCase(status)
                                    && !"PARTIALLY_APPROVED".equalsIgnoreCase(status)
                                    && !"REJECTED".equalsIgnoreCase(status);
                        })
                        .toList();

        List<PendingStudentDTO> dtos =
                new ArrayList<>();

        for (ErpScholarshipApplication app : apps) {
            String studentName = "Unknown";

            if (app.getStudent() != null) {
                studentName =
                        (
                                app.getStudent().getFirstName()
                                        + " "
                                        + app.getStudent().getLastName()
                        ).trim();
            } else if (app.getApplication() != null) {
                studentName =
                        (
                                app.getApplication().getFirstName()
                                        + " "
                                        + app.getApplication().getLastName()
                        ).trim();
            }

            dtos.add(
                    new PendingStudentDTO(
                            app.getScholarshipAppId(),
                            studentName,
                            "Campus " + app.getBranchId(),
                            app.getBranchId(),
                            app.getCategory(),
                            app.getAmountRequestedUgx(),
                            app.getAmountRequestedUgx()
                    )
            );
        }

        return dtos;
    }

    @Override
    public List<BranchDemandDTO> getBranchDemands() {
        return new ArrayList<>();
    }

    @Override
    public List<ActiveSponsorshipDTO> getActiveSponsorships() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

        List<ErpScholarshipAllocation> allocations =
                allocationRepo.findAll();

        List<Long> donationIds = allocations
                .stream()
                .map(
                        ErpScholarshipAllocation::getDonationId
                )
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, WebDonation> donationsById =
                new HashMap<>();

        for (
                WebDonation donation
                : donationReadService.findAllById(
                        donationIds
                )
        ) {
            donationsById.put(
                    donation.getId(),
                    donation
            );
        }

        return allocations.stream().map(alloc -> {
            String dateStr = alloc.getCreatedAt() != null ? alloc.getCreatedAt().format(formatter) : "N/A";
            WebDonation donation = donationsById.get(
                    alloc.getDonationId()
            );

            return new ActiveSponsorshipDTO(
                    alloc.getId(),
                    "Student " + alloc.getStudentId(),
                    "Campus " + alloc.getBranchId(),
                    donation != null
                            ? donation.getFullName()
                            : "General Fund",
                    alloc.getAllocatedAmountUgx(),
                    dateStr
            );
        }).toList();
    }

    @Override
    public void allocateToBranch(AllocationRequestDTO request) {
        validateDonationReference(
                request.getDonationId()
        );

        ErpBranchFundAllocation alloc = new ErpBranchFundAllocation();
        alloc.setBranchId(request.getBranchId());
        alloc.setAllocatedAmountUgx(request.getAmountUgx());
        alloc.setTerm(request.getTerm());
        alloc.setAcademicYear(request.getAcademicYear());
        alloc.setAllocatedByUserId(
                currentUserId()
        );
        alloc.setDonationId(request.getDonationId());
        branchFundRepo.save(alloc);
    }

    @Override
    public void allocateToStudent(AllocationRequestDTO request) {
        validateDonationReference(
                request.getDonationId()
        );

        ErpScholarshipAllocation alloc = new ErpScholarshipAllocation();
        alloc.setBranchId(request.getBranchId());
        alloc.setStudentId(request.getStudentId());
        alloc.setAllocatedAmountUgx(request.getAmountUgx());
        alloc.setTerm(request.getTerm());
        alloc.setAcademicYear(request.getAcademicYear());
        alloc.setAllocatedByUserId(
                currentUserId()
        );
        alloc.setDonationId(request.getDonationId());
        allocationRepo.save(alloc);
    }

    @Override
    public PublicScholarshipLinkToken issuePublicApplicationToken(
            Long applicationId
    ) {
        if (applicationId == null || applicationId <= 0L) {
            throw new BadRequestException(
                    "A valid admission application ID is required."
            );
        }

        var context =
                currentUserService
                        .getCurrentUserContext();

        if (context == null
                || context.getBranchId() == null) {
            throw new BadRequestException(
                    "Current branch context is unavailable."
            );
        }

        Long branchId =
                Long.valueOf(
                        context.getBranchId()
                );

        ErpScholarshipApplication scholarshipApplication =
                applicationRepo
                        .findActiveByApplicationAndBranchForUpdate(
                                applicationId,
                                branchId
                        )
                        .orElseThrow(
                                () -> new BadRequestException(
                                        "Scholarship application has not been created for this admission application."
                                )
                        );

        ErpApplication application =
                scholarshipApplication.getApplication();

        if (application == null) {
            throw new BadRequestException(
                    "Admission application is unavailable for this scholarship record."
            );
        }

        if (application.getCurrentStage()
                != ErpApplication.CurrentStage.SCHOLARSHIP) {
            throw new BadRequestException(
                    "Finalize the Scholarship decision in Fee Discussion before sending the Scholarship Application link."
            );
        }

        ensureScholarshipFormStillEditable(
                scholarshipApplication
        );

        byte[] tokenBytes =
                new byte[32];

        SECURE_RANDOM.nextBytes(tokenBytes);

        String rawToken =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(tokenBytes);

        LocalDateTime expiresAt =
                LocalDateTime.now()
                        .plusHours(
                                PUBLIC_TOKEN_VALID_HOURS
                        );

        scholarshipApplication.setApplicationMethod(
                ErpScholarshipApplication.ApplicationMethod.EMAIL_LINK
        );

        /*
         * The same scholarship application may use either the parent/public
         * route or the authenticated school-assisted route, never both at the
         * same time. Issuing a parent link revokes any school launch key.
         */
        scholarshipApplication.setSchoolAccessTokenHash(null);
        scholarshipApplication.setSchoolAccessExpiresAt(null);
        scholarshipApplication.setSchoolAccessIssuedAt(null);
        scholarshipApplication.setSchoolAccessIssuedBy(null);

        scholarshipApplication.setPublicTokenHash(
                sha256(rawToken)
        );
        scholarshipApplication.setTokenExpiresAt(
                expiresAt
        );
        scholarshipApplication.setTokenUsedAt(
                null
        );
        scholarshipApplication.setStatus(
                "LINK_PENDING"
        );
        scholarshipApplication.setUpdatedBy(
                Long.valueOf(
                        context.getUserId()
                )
        );

        ErpScholarshipApplication saved =
                applicationRepo.saveAndFlush(
                        scholarshipApplication
                );

        applicationEventPublisher.publishEvent(
                new ScholarshipApplicationLinkEmailRequestedEvent(
                        saved.getScholarshipAppId(),
                        rawToken
                )
        );

        return new PublicScholarshipLinkToken(
                expiresAt,
                "LINK_PENDING"
        );
    }


    @Override
    public SchoolScholarshipAccessKey issueSchoolApplicationAccess(
            Long applicationId
    ) {
        if (applicationId == null || applicationId <= 0L) {
            throw new BadRequestException(
                    "A valid admission application ID is required."
            );
        }

        var context =
                currentUserService
                        .getCurrentUserContext();

        if (context == null
                || context.getBranchId() == null
                || context.getUserId() == null) {
            throw new BadRequestException(
                    "Current branch user context is unavailable."
            );
        }

        Long branchId =
                Long.valueOf(
                        context.getBranchId()
                );

        Long userId =
                Long.valueOf(
                        context.getUserId()
                );

        ErpScholarshipApplication scholarship =
                applicationRepo
                        .findActiveByApplicationAndBranchForUpdate(
                                applicationId,
                                branchId
                        )
                        .orElseThrow(
                                () -> new BadRequestException(
                                        "Scholarship application has not been created for this admission application."
                                )
                        );

        ErpApplication application =
                scholarship.getApplication();

        if (application == null) {
            throw new BadRequestException(
                    "Admission application is unavailable for this scholarship record."
            );
        }

        /*
         * The school form must not be launched while Fee Discussion is still
         * merely being edited. The protected workflow action will finalize the
         * scholarship decision first; only then can a school launch key exist.
         */
        if (application.getCurrentStage()
                != ErpApplication.CurrentStage.SCHOLARSHIP) {
            throw new BadRequestException(
                    "Finalize the Scholarship decision in Fee Discussion before opening the school-assisted Scholarship Application."
            );
        }

        ensureScholarshipFormStillEditable(
                scholarship
        );

        String rawAccessKey =
                generateOpaqueToken();

        LocalDateTime now =
                LocalDateTime.now();

        LocalDateTime expiresAt =
                now.plusHours(
                        SCHOOL_ACCESS_VALID_HOURS
                );

        /*
         * Route switch: an authenticated school-assisted launch invalidates
         * any parent/public email token for this same scholarship application.
         */
        scholarship.setPublicTokenHash(null);
        scholarship.setTokenExpiresAt(null);
        scholarship.setTokenUsedAt(null);

        scholarship.setApplicationMethod(
                ErpScholarshipApplication.ApplicationMethod.SCHOOL_ASSISTED
        );
        scholarship.setStatus(
                "IN_PROGRESS"
        );
        scholarship.setSchoolAccessTokenHash(
                sha256(rawAccessKey)
        );
        scholarship.setSchoolAccessIssuedAt(
                now
        );
        scholarship.setSchoolAccessExpiresAt(
                expiresAt
        );
        scholarship.setSchoolAccessIssuedBy(
                userId
        );
        scholarship.setUpdatedBy(
                userId
        );

        applicationRepo.saveAndFlush(
                scholarship
        );

        /*
         * Returning the raw school key here is intentional: unlike the public
         * email token, the authenticated Branch Admin browser needs this
         * one-time value to open the new-tab scholarship form. It is not
         * persisted and must never be logged or placed in page text.
         */
        return new SchoolScholarshipAccessKey(
                rawAccessKey,
                expiresAt
        );
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolScholarshipAccessValidation validateSchoolApplicationAccess(
            String accessKey
    ) {
        String rawAccessKey =
                cleanRequired(
                        accessKey,
                        "School scholarship access key is required."
                );

        Long branchId =
                currentBranchId();

        ErpScholarshipApplication scholarship =
                applicationRepo
                        .findBySchoolAccessTokenHashAndActiveTrue(
                                sha256(rawAccessKey)
                        )
                        .orElseThrow(
                                () -> new BadRequestException(
                                        "School scholarship access link is invalid."
                                )
                        );

        if (!Objects.equals(
                scholarship.getBranchId(),
                branchId
        )) {
            /*
             * Keep the response generic so a key copied to another branch
             * does not disclose whether a scholarship record exists.
             */
            throw new BadRequestException(
                    "School scholarship access link is invalid."
            );
        }

        if (scholarship.getApplicationMethod()
                != ErpScholarshipApplication.ApplicationMethod.SCHOOL_ASSISTED) {
            throw new BadRequestException(
                    "School scholarship access link is no longer active."
            );
        }

        LocalDateTime expiresAt =
                scholarship.getSchoolAccessExpiresAt();

        if (expiresAt == null
                || !expiresAt.isAfter(
                        LocalDateTime.now()
                )) {
            throw new BadRequestException(
                    "School scholarship access link has expired. Generate a new link from the applicant workflow."
            );
        }

        ErpApplication application =
                scholarship.getApplication();

        if (application == null
                || application.getCurrentStage()
                != ErpApplication.CurrentStage.SCHOLARSHIP) {
            throw new BadRequestException(
                    "School scholarship access link is no longer available for this application."
            );
        }

        ensureScholarshipFormStillEditable(
                scholarship
        );

        return new SchoolScholarshipAccessValidation(
                true,
                expiresAt,
                normalizeScholarshipStatus(
                        scholarship.getStatus()
                )
        );
    }


    @Override
    @Transactional(readOnly = true)
    public ScholarshipApplicationFormResponseDTO getApplicationFormForBranch(
            Long applicationId
    ) {
        Long branchId = currentBranchId();

        ErpScholarshipApplication scholarship =
                applicationRepo
                        .findActiveByApplicationAndBranch(
                                applicationId,
                                branchId
                        )
                        .orElseThrow(
                                () -> new BadRequestException(
                                        "Scholarship application has not been created for this admission application."
                                )
                        );

        return buildFormResponse(
                scholarship,
                branchId
        );
    }

    @Override
    public ScholarshipApplicationFormResponseDTO saveApplicationFormForBranch(
            Long applicationId,
            ScholarshipApplicationFormRequestDTO request
    ) {
        return saveScholarshipForm(
                applicationId,
                request,
                false
        );
    }

    @Override
    public ScholarshipApplicationFormResponseDTO submitApplicationFormForBranch(
            Long applicationId,
            ScholarshipApplicationFormRequestDTO request
    ) {
        return saveScholarshipForm(
                applicationId,
                request,
                true
        );
    }

    private ScholarshipApplicationFormResponseDTO saveScholarshipForm(
            Long applicationId,
            ScholarshipApplicationFormRequestDTO request,
            boolean submit
    ) {
        if (request == null) {
            throw new BadRequestException(
                    "Scholarship application form is required."
            );
        }

        Long branchId = currentBranchId();
        Long userId = currentUserId();

        ErpScholarshipApplication scholarship =
                applicationRepo
                        .findActiveByApplicationAndBranchForUpdate(
                                applicationId,
                                branchId
                        )
                        .orElseThrow(
                                () -> new BadRequestException(
                                        "Scholarship application has not been created for this admission application."
                                )
                        );

        validateScholarshipStage(scholarship);
        ensureScholarshipFormStillEditable(scholarship);

        return persistScholarshipForm(
                scholarship,
                request,
                submit,
                userId,
                ErpScholarshipApplication.ApplicationMethod.SCHOOL_ASSISTED,
                submit,
                true
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ScholarshipApplicationFormResponseDTO getApplicationFormForSchoolAccess(
            String accessKey
    ) {
        ErpScholarshipApplication scholarship =
                resolveSchoolAccessForRead(accessKey);

        return buildFormResponse(
                scholarship,
                scholarship.getBranchId(),
                false
        );
    }

    @Override
    public ScholarshipApplicationFormResponseDTO saveApplicationFormForSchoolAccess(
            String accessKey,
            ScholarshipApplicationFormRequestDTO request
    ) {
        if (request == null) {
            throw new BadRequestException(
                    "Scholarship application form is required."
            );
        }

        Long branchId = currentBranchId();

        ErpScholarshipApplication scholarship =
                applicationRepo
                        .findActiveBySchoolAccessTokenHashForUpdate(
                                sha256(
                                        cleanRequired(
                                                accessKey,
                                                "School scholarship access key is required."
                                        )
                                )
                        )
                        .orElseThrow(
                                () -> new BadRequestException(
                                        "School scholarship access link is invalid."
                                )
                        );

        validateSchoolAccess(
                scholarship,
                branchId
        );

        return persistScholarshipForm(
                scholarship,
                request,
                false,
                currentUserId(),
                ErpScholarshipApplication.ApplicationMethod.SCHOOL_ASSISTED,
                false,
                false
        );
    }

    @Override
    public ScholarshipApplicationFormResponseDTO submitApplicationFormForSchoolAccess(
            String accessKey,
            ScholarshipApplicationFormRequestDTO request
    ) {
        if (request == null) {
            throw new BadRequestException(
                    "Scholarship application form is required."
            );
        }

        Long branchId = currentBranchId();

        ErpScholarshipApplication scholarship =
                applicationRepo
                        .findActiveBySchoolAccessTokenHashForUpdate(
                                sha256(
                                        cleanRequired(
                                                accessKey,
                                                "School scholarship access key is required."
                                        )
                                )
                        )
                        .orElseThrow(
                                () -> new BadRequestException(
                                        "School scholarship access link is invalid."
                                )
                        );

        validateSchoolAccess(
                scholarship,
                branchId
        );

        return persistScholarshipForm(
                scholarship,
                request,
                true,
                currentUserId(),
                ErpScholarshipApplication.ApplicationMethod.SCHOOL_ASSISTED,
                true,
                false
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ScholarshipApplicationFormResponseDTO getApplicationFormForPublicToken(
            String publicToken
    ) {
        ErpScholarshipApplication scholarship =
                resolvePublicTokenForRead(publicToken);

        return buildFormResponse(
                scholarship,
                scholarship.getBranchId(),
                false
        );
    }

    @Override
    public ScholarshipApplicationFormResponseDTO saveApplicationFormForPublicToken(
            String publicToken,
            ScholarshipApplicationFormRequestDTO request
    ) {
        if (request == null) {
            throw new BadRequestException(
                    "Scholarship application form is required."
            );
        }

        ErpScholarshipApplication scholarship =
                applicationRepo
                        .findActiveByTokenHashForUpdate(
                                sha256(
                                        cleanRequired(
                                                publicToken,
                                                "Scholarship access token is required."
                                        )
                                )
                        )
                        .orElseThrow(
                                () -> new BadRequestException(
                                        "Scholarship application link is invalid."
                                )
                        );

        validatePublicAccess(scholarship);

        return persistScholarshipForm(
                scholarship,
                request,
                false,
                null,
                ErpScholarshipApplication.ApplicationMethod.EMAIL_LINK,
                false,
                false
        );
    }

    @Override
    public ScholarshipApplicationFormResponseDTO submitApplicationFormForPublicToken(
            String publicToken,
            ScholarshipApplicationFormRequestDTO request
    ) {
        if (request == null) {
            throw new BadRequestException(
                    "Scholarship application form is required."
            );
        }

        ErpScholarshipApplication scholarship =
                applicationRepo
                        .findActiveByTokenHashForUpdate(
                                sha256(
                                        cleanRequired(
                                                publicToken,
                                                "Scholarship access token is required."
                                        )
                                )
                        )
                        .orElseThrow(
                                () -> new BadRequestException(
                                        "Scholarship application link is invalid."
                                )
                        );

        validatePublicAccess(scholarship);

        return persistScholarshipForm(
                scholarship,
                request,
                true,
                null,
                ErpScholarshipApplication.ApplicationMethod.EMAIL_LINK,
                false,
                false
        );
    }

    private ScholarshipApplicationFormResponseDTO persistScholarshipForm(
            ErpScholarshipApplication scholarship,
            ScholarshipApplicationFormRequestDTO request,
            boolean submit,
            Long actorUserId,
            ErpScholarshipApplication.ApplicationMethod applicationMethod,
            boolean consumeSchoolAccessOnSubmit,
            boolean includeInternalIds
    ) {
        ErpApplication application = scholarship.getApplication();

        if (application == null) {
            throw new BadRequestException(
                    "Admission application is unavailable for this scholarship record."
            );
        }

        validateScholarshipStage(scholarship);
        ensureScholarshipFormStillEditable(scholarship);

        application.setFatherName(clean(request.getFatherName()));
        application.setFatherContact(clean(request.getFatherContact()));
        application.setFatherEmail(clean(request.getFatherEmail()));
        application.setFatherOccupation(clean(request.getFatherOccupation()));

        application.setMotherName(clean(request.getMotherName()));
        application.setMotherContact(clean(request.getMotherContact()));
        application.setMotherEmail(clean(request.getMotherEmail()));
        application.setMotherOccupation(clean(request.getMotherOccupation()));

        application.setGuardianName(clean(request.getGuardianName()));
        application.setGuardianRelation(clean(request.getGuardianRelation()));
        application.setGuardianContact(clean(request.getGuardianContact()));
        application.setGuardianEmail(clean(request.getGuardianEmail()));
        application.setGuardianOccupation(clean(request.getGuardianOccupation()));

        scholarship.setFatherStatus(clean(request.getFatherStatus()));
        scholarship.setFatherAnnualIncome(nonNegative(request.getFatherAnnualIncome()));

        scholarship.setMotherStatus(clean(request.getMotherStatus()));
        scholarship.setMotherAnnualIncome(nonNegative(request.getMotherAnnualIncome()));

        scholarship.setResponsiblePersonType(clean(request.getResponsiblePersonType()));
        scholarship.setResponsiblePersonName(clean(request.getResponsiblePersonName()));
        scholarship.setResponsiblePersonRelation(clean(request.getResponsiblePersonRelation()));
        scholarship.setResponsiblePersonOccupation(clean(request.getResponsiblePersonOccupation()));
        scholarship.setResponsiblePersonMobile(clean(request.getResponsiblePersonMobile()));
        scholarship.setResponsiblePersonAnnualIncome(
                nonNegative(request.getResponsiblePersonAnnualIncome())
        );

        scholarship.setOrphanStatus(clean(request.getOrphanStatus()));

        scholarship.setHouseholdSize(nonNegativeInt(request.getHouseholdSize()));
        scholarship.setDependantsCount(nonNegativeInt(request.getDependantsCount()));
        scholarship.setSchoolGoingChildren(nonNegativeInt(request.getSchoolGoingChildren()));
        scholarship.setMainIncomeEarner(clean(request.getMainIncomeEarner()));
        scholarship.setIncomeSource(clean(request.getIncomeSource()));
        scholarship.setOtherHouseholdIncome(
                nonNegative(request.getOtherHouseholdIncome())
        );
        scholarship.setHousingStatus(clean(request.getHousingStatus()));

        /*
         * Housing details are driven by housingStatus in the UI.
         * Estimated values are assets, not household income.
         */
        scholarship.setHouseType(clean(request.getHouseType()));
        scholarship.setHouseRoomCount(
                nonNegativeInt(request.getHouseRoomCount())
        );
        scholarship.setHouseLocation(clean(request.getHouseLocation()));
        scholarship.setHouseEstimatedValue(
                nonNegative(request.getHouseEstimatedValue())
        );
        scholarship.setHouseMortgaged(
                Boolean.TRUE.equals(request.getHouseMortgaged())
        );
        scholarship.setMonthlyRent(
                nonNegative(request.getMonthlyRent())
        );

        boolean landOwned =
                Boolean.TRUE.equals(request.getLandOwned());

        scholarship.setLandOwned(landOwned);
        scholarship.setLandArea(
                landOwned
                        ? nonNegative(request.getLandArea())
                        : null
        );
        scholarship.setLandUnit(
                landOwned
                        ? clean(request.getLandUnit())
                        : null
        );
        scholarship.setLandPlotCount(
                landOwned
                        ? nonNegativeInt(request.getLandPlotCount())
                        : null
        );
        scholarship.setLandLocation(
                landOwned
                        ? clean(request.getLandLocation())
                        : null
        );
        scholarship.setLandUsage(
                landOwned
                        ? clean(request.getLandUsage())
                        : null
        );
        scholarship.setLandEstimatedValue(
                landOwned
                        ? nonNegative(request.getLandEstimatedValue())
                        : null
        );

        boolean landGeneratesIncome =
                landOwned
                        && Boolean.TRUE.equals(
                                request.getLandGeneratesIncome()
                        );

        scholarship.setLandGeneratesIncome(
                landGeneratesIncome
        );
        scholarship.setLandAnnualIncome(
                landGeneratesIncome
                        ? nonNegative(request.getLandAnnualIncome())
                        : null
        );

        boolean vehiclesOwned =
                Boolean.TRUE.equals(request.getVehiclesOwned());

        scholarship.setVehiclesOwned(vehiclesOwned);
        scholarship.setVehicleCount(
                vehiclesOwned
                        ? nonNegativeInt(request.getVehicleCount())
                        : null
        );
        scholarship.setVehicleDescription(
                vehiclesOwned
                        ? clean(request.getVehicleDescription())
                        : null
        );
        scholarship.setVehicleType(
                vehiclesOwned
                        ? clean(request.getVehicleType())
                        : null
        );
        scholarship.setVehicleUsage(
                vehiclesOwned
                        ? clean(request.getVehicleUsage())
                        : null
        );
        scholarship.setVehicleEstimatedValue(
                vehiclesOwned
                        ? nonNegative(request.getVehicleEstimatedValue())
                        : null
        );
        scholarship.setVehicleFinanced(
                vehiclesOwned
                        && Boolean.TRUE.equals(
                                request.getVehicleFinanced()
                        )
        );

        boolean businessOwned =
                Boolean.TRUE.equals(request.getBusinessOwned());

        scholarship.setBusinessOwned(businessOwned);
        scholarship.setBusinessName(
                businessOwned
                        ? clean(request.getBusinessName())
                        : null
        );
        scholarship.setBusinessType(
                businessOwned
                        ? clean(request.getBusinessType())
                        : null
        );
        scholarship.setBusinessLocation(
                businessOwned
                        ? clean(request.getBusinessLocation())
                        : null
        );
        scholarship.setBusinessEmployeeCount(
                businessOwned
                        ? nonNegativeInt(request.getBusinessEmployeeCount())
                        : null
        );
        scholarship.setBusinessAnnualIncome(
                businessOwned
                        ? nonNegative(request.getBusinessAnnualIncome())
                        : null
        );

        boolean livestockOwned =
                Boolean.TRUE.equals(request.getLivestockOwned());

        scholarship.setLivestockOwned(livestockOwned);
        scholarship.setLivestockDescription(
                livestockOwned
                        ? clean(request.getLivestockDescription())
                        : null
        );
        scholarship.setLivestockEstimatedValue(
                livestockOwned
                        ? nonNegative(request.getLivestockEstimatedValue())
                        : null
        );
        scholarship.setLivestockAnnualIncome(
                livestockOwned
                        ? nonNegative(request.getLivestockAnnualIncome())
                        : null
        );

        scholarship.setOtherAssetsDescription(
                clean(request.getOtherAssetsDescription())
        );
        scholarship.setOtherAssetsEstimatedValue(
                nonNegative(request.getOtherAssetsEstimatedValue())
        );
        scholarship.setOtherAssetsAnnualIncome(
                nonNegative(request.getOtherAssetsAnnualIncome())
        );

        scholarship.setFinancialHardshipReason(
                clean(request.getFinancialHardshipReason())
        );
        scholarship.setFamilySituationRemarks(
                clean(request.getFamilySituationRemarks())
        );

        scholarship.setParentGuardianName(clean(request.getParentGuardianName()));
        scholarship.setParentGuardianRelation(clean(request.getParentGuardianRelation()));
        scholarship.setParentGuardianMobile(clean(request.getParentGuardianMobile()));
        scholarship.setDeclarationAccepted(
                Boolean.TRUE.equals(request.getDeclarationAccepted())
        );

        replaceSiblings(
                scholarship,
                request.getSiblings(),
                actorUserId
        );

        scholarship.setHouseholdIncome(
                calculateAnnualHouseholdIncome(
                        scholarship.getFatherAnnualIncome(),
                        scholarship.getMotherAnnualIncome(),
                        scholarship.getResponsiblePersonAnnualIncome(),
                        scholarship.getOtherHouseholdIncome(),
                        scholarship.getLandAnnualIncome(),
                        scholarship.getBusinessAnnualIncome(),
                        scholarship.getLivestockAnnualIncome(),
                        scholarship.getOtherAssetsAnnualIncome(),
                        request.getSiblings()
                )
        );

        scholarship.setApplicationMethod(applicationMethod);

        LocalDateTime now = LocalDateTime.now();

        if (submit) {
            if (!Boolean.TRUE.equals(
                    scholarship.getDeclarationAccepted()
            )) {
                throw new BadRequestException(
                        "Declaration must be accepted before submitting the scholarship application."
                );
            }

            scholarship.setStatus("SUBMITTED");
            scholarship.setSubmittedAt(now);

            if (applicationMethod
                    == ErpScholarshipApplication.ApplicationMethod.EMAIL_LINK) {
                scholarship.setTokenUsedAt(now);
                scholarship.setPublicTokenHash(null);
                scholarship.setTokenExpiresAt(null);
            }

            if (consumeSchoolAccessOnSubmit
                    || applicationMethod
                    == ErpScholarshipApplication.ApplicationMethod.SCHOOL_ASSISTED) {
                scholarship.setSchoolAccessTokenHash(null);
                scholarship.setSchoolAccessExpiresAt(null);
                scholarship.setSchoolAccessIssuedAt(null);
                scholarship.setSchoolAccessIssuedBy(null);
            }
        } else {
            scholarship.setStatus("IN_PROGRESS");
        }

        if (actorUserId != null) {
            scholarship.setUpdatedBy(actorUserId);
        }

        ErpScholarshipApplication saved =
                applicationRepo.saveAndFlush(scholarship);

        return buildFormResponse(
                saved,
                saved.getBranchId(),
                includeInternalIds
        );
    }

    private ErpScholarshipApplication resolveSchoolAccessForRead(
            String accessKey
    ) {
        Long branchId = currentBranchId();

        ErpScholarshipApplication scholarship =
                applicationRepo
                        .findBySchoolAccessTokenHashAndActiveTrue(
                                sha256(
                                        cleanRequired(
                                                accessKey,
                                                "School scholarship access key is required."
                                        )
                                )
                        )
                        .orElseThrow(
                                () -> new BadRequestException(
                                        "School scholarship access link is invalid."
                                )
                        );

        validateSchoolAccess(
                scholarship,
                branchId
        );

        return scholarship;
    }

    private ErpScholarshipApplication resolvePublicTokenForRead(
            String publicToken
    ) {
        ErpScholarshipApplication scholarship =
                applicationRepo
                        .findByPublicTokenHashAndActiveTrue(
                                sha256(
                                        cleanRequired(
                                                publicToken,
                                                "Scholarship access token is required."
                                        )
                                )
                        )
                        .orElseThrow(
                                () -> new BadRequestException(
                                        "Scholarship application link is invalid."
                                )
                        );

        validatePublicAccess(scholarship);

        return scholarship;
    }

    private void validateSchoolAccess(
            ErpScholarshipApplication scholarship,
            Long branchId
    ) {
        if (scholarship == null
                || !Objects.equals(
                        scholarship.getBranchId(),
                        branchId
                )) {
            throw new BadRequestException(
                    "School scholarship access link is invalid."
            );
        }

        if (scholarship.getApplicationMethod()
                != ErpScholarshipApplication.ApplicationMethod.SCHOOL_ASSISTED) {
            throw new BadRequestException(
                    "School scholarship access link is no longer active."
            );
        }

        LocalDateTime expiresAt =
                scholarship.getSchoolAccessExpiresAt();

        if (expiresAt == null
                || !expiresAt.isAfter(LocalDateTime.now())) {
            throw new BadRequestException(
                    "School scholarship access link has expired. Generate a new link from the applicant workflow."
            );
        }

        validateScholarshipStage(scholarship);
        ensureScholarshipFormStillEditable(scholarship);
    }

    private void validatePublicAccess(
            ErpScholarshipApplication scholarship
    ) {
        if (scholarship == null
                || scholarship.getApplicationMethod()
                != ErpScholarshipApplication.ApplicationMethod.EMAIL_LINK) {
            throw new BadRequestException(
                    "Scholarship application link is invalid."
            );
        }

        if (scholarship.getTokenUsedAt() != null) {
            throw new BadRequestException(
                    "Scholarship application link has already been used."
            );
        }

        LocalDateTime expiresAt =
                scholarship.getTokenExpiresAt();

        if (expiresAt == null
                || !expiresAt.isAfter(LocalDateTime.now())) {
            throw new BadRequestException(
                    "Scholarship application link has expired. Request a new link from the school."
            );
        }

        validateScholarshipStage(scholarship);
        ensureScholarshipFormStillEditable(scholarship);
    }

    private void validateScholarshipStage(
            ErpScholarshipApplication scholarship
    ) {
        ErpApplication application =
                scholarship != null
                        ? scholarship.getApplication()
                        : null;

        if (application == null
                || application.getCurrentStage()
                != ErpApplication.CurrentStage.SCHOLARSHIP) {
            throw new BadRequestException(
                    "Scholarship Application is not available at the current admission stage."
            );
        }
    }

    private void replaceSiblings(
            ErpScholarshipApplication scholarship,
            List<ScholarshipApplicationFormRequestDTO.SiblingRequest> siblings,
            Long userId
    ) {
        siblingRepo.deleteByScholarshipApplicationScholarshipAppId(
                scholarship.getScholarshipAppId()
        );

        if (siblings == null || siblings.isEmpty()) {
            return;
        }

        for (
                ScholarshipApplicationFormRequestDTO.SiblingRequest request
                : siblings
        ) {
            if (request == null
                    || clean(request.getSiblingName()) == null) {
                continue;
            }

            ErpScholarshipSibling sibling =
                    new ErpScholarshipSibling();

            sibling.setScholarshipApplication(
                    scholarship
            );
            sibling.setSiblingName(
                    clean(request.getSiblingName())
            );
            sibling.setAge(
                    nonNegativeInt(request.getAge())
            );

            try {
                sibling.setCurrentStatus(
                        ErpScholarshipSibling.CurrentStatus.valueOf(
                                cleanRequired(
                                        request.getCurrentStatus(),
                                        "Sibling status is required."
                                ).toUpperCase()
                        )
                );
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException(
                        "Invalid sibling status."
                );
            }

            sibling.setClassOrCourse(
                    clean(request.getClassOrCourse())
            );
            sibling.setInstitution(
                    clean(request.getInstitution())
            );
            sibling.setOccupation(
                    clean(request.getOccupation())
            );
            sibling.setAnnualIncome(
                    nonNegative(request.getAnnualIncome())
            );
            sibling.setCreatedBy(userId);
            sibling.setUpdatedBy(userId);

            siblingRepo.save(sibling);
        }

        siblingRepo.flush();
    }

    private ScholarshipApplicationFormResponseDTO buildFormResponse(
            ErpScholarshipApplication scholarship,
            Long branchId
    ) {
        return buildFormResponse(
                scholarship,
                branchId,
                true
        );
    }

    private ScholarshipApplicationFormResponseDTO buildFormResponse(
            ErpScholarshipApplication scholarship,
            Long branchId,
            boolean includeInternalIds
    ) {
        ScholarshipApplicationFormResponseDTO response =
                new ScholarshipApplicationFormResponseDTO();

        ErpApplication application =
                scholarship.getApplication();

        if (includeInternalIds) {
            response.setScholarshipAppId(
                    scholarship.getScholarshipAppId()
            );
            response.setApplicationId(
                    application != null
                            ? application.getApplicationId()
                            : null
            );
        } else {
            response.setScholarshipAppId(null);
            response.setApplicationId(null);
        }

        if (application != null) {
            response.setApplicationNo(
                    application.getApplicationNo()
            );
            response.setStudentName(
                    fullName(
                            application.getFirstName(),
                            application.getMiddleName(),
                            application.getLastName()
                    )
            );
            response.setClassName(
                    application.getBranchClassId() != null
                            ? "Class " + application.getBranchClassId()
                            : null
            );
            response.setAcademicYear(
                    scholarship.getAcademicYear()
            );
            response.setTerm(
                    application.getTerm()
            );

            response.setFatherName(application.getFatherName());
            response.setFatherContact(application.getFatherContact());
            response.setFatherEmail(application.getFatherEmail());
            response.setFatherOccupation(application.getFatherOccupation());

            response.setMotherName(application.getMotherName());
            response.setMotherContact(application.getMotherContact());
            response.setMotherEmail(application.getMotherEmail());
            response.setMotherOccupation(application.getMotherOccupation());

            response.setGuardianName(application.getGuardianName());
            response.setGuardianRelation(application.getGuardianRelation());
            response.setGuardianContact(application.getGuardianContact());
            response.setGuardianEmail(application.getGuardianEmail());
            response.setGuardianOccupation(application.getGuardianOccupation());

            feeRepo.findActiveByApplicationAndBranch(
                            application.getApplicationId(),
                            branchId.intValue()
                    )
                    .ifPresent(
                            fee -> {
                                response.setTotalFee(
                                        fee.getBaseFeeAmount()
                                );
                                response.setParentContribution(
                                        fee.getParentCanPay()
                                );
                                response.setScholarshipRequiredAmount(
                                        fee.getAssistanceRequired()
                                );
                            }
                    );
        }

        response.setScholarshipType(
                scholarship.getScholarshipType() != null
                        ? scholarship.getScholarshipType().name()
                        : null
        );
        response.setApplicationMethod(
                scholarship.getApplicationMethod() != null
                        ? scholarship.getApplicationMethod().name()
                        : null
        );
        response.setStatus(
                scholarship.getStatus()
        );

        response.setFatherStatus(scholarship.getFatherStatus());
        response.setFatherAnnualIncome(scholarship.getFatherAnnualIncome());
        response.setMotherStatus(scholarship.getMotherStatus());
        response.setMotherAnnualIncome(scholarship.getMotherAnnualIncome());

        response.setResponsiblePersonType(scholarship.getResponsiblePersonType());
        response.setResponsiblePersonName(scholarship.getResponsiblePersonName());
        response.setResponsiblePersonRelation(scholarship.getResponsiblePersonRelation());
        response.setResponsiblePersonOccupation(scholarship.getResponsiblePersonOccupation());
        response.setResponsiblePersonMobile(scholarship.getResponsiblePersonMobile());
        response.setResponsiblePersonAnnualIncome(
                scholarship.getResponsiblePersonAnnualIncome()
        );

        response.setOrphanStatus(scholarship.getOrphanStatus());

        response.setHouseholdSize(scholarship.getHouseholdSize());
        response.setDependantsCount(scholarship.getDependantsCount());
        response.setSchoolGoingChildren(scholarship.getSchoolGoingChildren());
        response.setMainIncomeEarner(scholarship.getMainIncomeEarner());
        response.setIncomeSource(scholarship.getIncomeSource());
        response.setOtherHouseholdIncome(scholarship.getOtherHouseholdIncome());
        response.setHouseholdIncome(scholarship.getHouseholdIncome());
        response.setHousingStatus(scholarship.getHousingStatus());

        response.setHouseType(scholarship.getHouseType());
        response.setHouseRoomCount(scholarship.getHouseRoomCount());
        response.setHouseLocation(scholarship.getHouseLocation());
        response.setHouseEstimatedValue(scholarship.getHouseEstimatedValue());
        response.setHouseMortgaged(scholarship.getHouseMortgaged());
        response.setMonthlyRent(scholarship.getMonthlyRent());

        response.setLandOwned(scholarship.getLandOwned());
        response.setLandArea(scholarship.getLandArea());
        response.setLandUnit(scholarship.getLandUnit());
        response.setLandPlotCount(scholarship.getLandPlotCount());
        response.setLandLocation(scholarship.getLandLocation());
        response.setLandUsage(scholarship.getLandUsage());
        response.setLandEstimatedValue(scholarship.getLandEstimatedValue());
        response.setLandGeneratesIncome(scholarship.getLandGeneratesIncome());
        response.setLandAnnualIncome(scholarship.getLandAnnualIncome());

        response.setVehiclesOwned(scholarship.getVehiclesOwned());
        response.setVehicleCount(scholarship.getVehicleCount());
        response.setVehicleDescription(scholarship.getVehicleDescription());
        response.setVehicleType(scholarship.getVehicleType());
        response.setVehicleUsage(scholarship.getVehicleUsage());
        response.setVehicleEstimatedValue(scholarship.getVehicleEstimatedValue());
        response.setVehicleFinanced(scholarship.getVehicleFinanced());

        response.setBusinessOwned(scholarship.getBusinessOwned());
        response.setBusinessName(scholarship.getBusinessName());
        response.setBusinessType(scholarship.getBusinessType());
        response.setBusinessLocation(scholarship.getBusinessLocation());
        response.setBusinessEmployeeCount(scholarship.getBusinessEmployeeCount());
        response.setBusinessAnnualIncome(scholarship.getBusinessAnnualIncome());

        response.setLivestockOwned(scholarship.getLivestockOwned());
        response.setLivestockDescription(scholarship.getLivestockDescription());
        response.setLivestockEstimatedValue(scholarship.getLivestockEstimatedValue());
        response.setLivestockAnnualIncome(scholarship.getLivestockAnnualIncome());

        response.setOtherAssetsDescription(scholarship.getOtherAssetsDescription());
        response.setOtherAssetsEstimatedValue(
                scholarship.getOtherAssetsEstimatedValue()
        );
        response.setOtherAssetsAnnualIncome(
                scholarship.getOtherAssetsAnnualIncome()
        );

        response.setFinancialHardshipReason(
                scholarship.getFinancialHardshipReason()
        );
        response.setFamilySituationRemarks(
                scholarship.getFamilySituationRemarks()
        );

        response.setParentGuardianName(scholarship.getParentGuardianName());
        response.setParentGuardianRelation(scholarship.getParentGuardianRelation());
        response.setParentGuardianMobile(scholarship.getParentGuardianMobile());
        response.setDeclarationAccepted(scholarship.getDeclarationAccepted());
        response.setSubmittedAt(scholarship.getSubmittedAt());

        List<ScholarshipApplicationFormResponseDTO.SiblingResponse>
                siblingResponses = new ArrayList<>();

        for (
                ErpScholarshipSibling sibling
                : siblingRepo
                        .findByScholarshipApplicationScholarshipAppIdAndActiveTrueOrderBySiblingIdAsc(
                                scholarship.getScholarshipAppId()
                        )
        ) {
            ScholarshipApplicationFormResponseDTO.SiblingResponse item =
                    new ScholarshipApplicationFormResponseDTO.SiblingResponse();

            item.setSiblingId(
                    includeInternalIds
                            ? sibling.getSiblingId()
                            : null
            );
            item.setSiblingName(sibling.getSiblingName());
            item.setAge(sibling.getAge());
            item.setCurrentStatus(
                    sibling.getCurrentStatus() != null
                            ? sibling.getCurrentStatus().name()
                            : null
            );
            item.setClassOrCourse(sibling.getClassOrCourse());
            item.setInstitution(sibling.getInstitution());
            item.setOccupation(sibling.getOccupation());
            item.setAnnualIncome(sibling.getAnnualIncome());

            siblingResponses.add(item);
        }

        response.setSiblings(
                siblingResponses
        );

        return response;
    }

    private BigDecimal calculateAnnualHouseholdIncome(
            BigDecimal fatherIncome,
            BigDecimal motherIncome,
            BigDecimal responsibleIncome,
            BigDecimal otherIncome,
            BigDecimal landIncome,
            BigDecimal businessIncome,
            BigDecimal livestockIncome,
            BigDecimal otherAssetsIncome,
            List<ScholarshipApplicationFormRequestDTO.SiblingRequest> siblings
    ) {
        BigDecimal total =
                zero(fatherIncome)
                        .add(zero(motherIncome))
                        .add(zero(responsibleIncome))
                        .add(zero(otherIncome))
                        .add(zero(landIncome))
                        .add(zero(businessIncome))
                        .add(zero(livestockIncome))
                        .add(zero(otherAssetsIncome));

        if (siblings != null) {
            for (
                    ScholarshipApplicationFormRequestDTO.SiblingRequest sibling
                    : siblings
            ) {
                if (sibling != null) {
                    total = total.add(
                            zero(sibling.getAnnualIncome())
                    );
                }
            }
        }

        return total;
    }

    private BigDecimal zero(BigDecimal value) {
        return value != null
                ? value
                : BigDecimal.ZERO;
    }

    private BigDecimal nonNegative(BigDecimal value) {
        if (value == null) {
            return null;
        }

        if (value.signum() < 0) {
            throw new BadRequestException(
                    "Income and asset values cannot be negative."
            );
        }

        return value;
    }

    private Integer nonNegativeInt(Integer value) {
        if (value == null) {
            return null;
        }

        if (value < 0) {
            throw new BadRequestException(
                    "Numeric values cannot be negative."
            );
        }

        return value;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        String cleaned =
                value.trim();

        return cleaned.isEmpty()
                ? null
                : cleaned;
    }

    private String cleanRequired(
            String value,
            String message
    ) {
        String cleaned = clean(value);

        if (cleaned == null) {
            throw new BadRequestException(
                    message
            );
        }

        return cleaned;
    }

    private String fullName(
            String first,
            String middle,
            String last
    ) {
        return String.join(
                " ",
                java.util.stream.Stream.of(
                                first,
                                middle,
                                last
                        )
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(value -> !value.isEmpty())
                        .toList()
        );
    }

    private Long currentBranchId() {
        var context =
                currentUserService
                        .getCurrentUserContext();

        if (context == null
                || context.getBranchId() == null) {
            throw new BadRequestException(
                    "Current branch context is unavailable."
            );
        }

        return Long.valueOf(
                context.getBranchId()
        );
    }

    private String normalizeScholarshipStatus(
            String status
    ) {
        if (status == null || status.isBlank()) {
            return "";
        }

        return status
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace(' ', '_')
                .replace('-', '_');
    }

    private String generateOpaqueToken() {
        byte[] tokenBytes =
                new byte[32];

        SECURE_RANDOM.nextBytes(
                tokenBytes
        );

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        tokenBytes
                );
    }

    private void ensureScholarshipFormStillEditable(
            ErpScholarshipApplication scholarship
    ) {
        String status =
                normalizeScholarshipStatus(
                        scholarship.getStatus()
                );

        switch (status) {
            case "SUBMITTED",
                 "UNDER_SCHOOL_REVIEW",
                 "SHORTLISTED",
                 "NOT_SHORTLISTED",
                 "UNDER_SUPERADMIN_REVIEW",
                 "APPROVED",
                 "PARTIALLY_APPROVED",
                 "REJECTED" ->
                    throw new BadRequestException(
                            "Scholarship Application has already been submitted or moved into review."
                    );

            default -> {
                // Initial/link/in-progress states remain editable.
            }
        }
    }

    private String sha256(
            String value
    ) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            value.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return java.util.HexFormat
                    .of()
                    .formatHex(hash);

        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "SHA-256 is unavailable.",
                    ex
            );
        }
    }

    private void validateDonationReference(
            Long donationId
    ) {
        if (donationId == null) {
            return;
        }

        if (
                donationReadService
                        .findById(donationId)
                        .isEmpty()
        ) {
            throw new BadRequestException(
                    "Selected donation does not exist."
            );
        }
    }

    private Long currentUserId() {
        return Long.valueOf(
                currentUserService
                        .getCurrentUserContext()
                        .getUserId()
        );
    }
}

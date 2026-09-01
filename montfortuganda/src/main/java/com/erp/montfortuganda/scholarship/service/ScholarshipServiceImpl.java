package com.erp.montfortuganda.scholarship.service;

import com.erp.montfortuganda.scholarship.dto.ScholarshipMultipleDonorAllocationRequestDTO;

import com.erp.montfortuganda.scholarship.dto.ScholarshipBulkAllocationRequestDTO;

import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.admission.entity.ErpApplication;
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
    private final ErpScholarshipHistoryRepository historyRepo;
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
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Sum up total spent
        BigDecimal totalSpent = donations.stream()
                .filter(d -> "success".equalsIgnoreCase(d.getPaymentStatus()))
                .map(d -> d.getAmountSpent() != null ? d.getAmountSpent() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

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

        List<PendingStudentDTO> pendingStudents =
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

            pendingStudents.add(
                    new PendingStudentDTO(
                            app.getScholarshipAppId(),
                            studentName,
                            "Campus " + app.getBranchId(),
                            app.getBranchId(),
                            latestHistory(app).map(ErpScholarshipHistory::getCategory).orElse(null),
                            latestHistory(app).map(ErpScholarshipHistory::getAmountRequestedUgx).orElse(BigDecimal.ZERO),
                            latestHistory(app).map(ErpScholarshipHistory::getAmountRequestedUgx).orElse(BigDecimal.ZERO)
                    )
            );
        }

        return pendingStudents;
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
                    alloc.getScholarshipAllocationId(),
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

    private BigDecimal requirePositiveAllocationAmount(
            BigDecimal amount
    ) {
        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
                    "Allocation amount must be greater than zero."
            );
        }

        return amount.setScale(
                2,
                java.math.RoundingMode.HALF_UP
        );
    }

    private ErpScholarshipHistory requireAllocationHistory(
            Long scholarshipHistoryId,
            Long branchId
    ) {
        if (scholarshipHistoryId == null) {
            throw new BadRequestException(
                    "Scholarship History ID is required for an allocation."
            );
        }

        if (branchId == null) {
            throw new BadRequestException(
                    "Branch ID is required."
            );
        }

        ErpScholarshipHistory history =
                historyRepo.findById(scholarshipHistoryId)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Scholarship history record not found."
                                )
                        );

        if (history.getBranchId() == null
                || !branchId.equals(history.getBranchId())) {
            throw new BadRequestException(
                    "Scholarship history does not belong to the selected branch."
            );
        }

        if (history.getApprovedAmount() == null
                || history.getApprovedAmount()
                .compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(
                    "Scholarship approved amount is not available for allocation."
            );
        }

        return history;
    }

    private BigDecimal allocatedAmountForHistory(
            Long scholarshipHistoryId
    ) {
        BigDecimal totalAllocated = BigDecimal.ZERO;

        for (ErpScholarshipAllocation allocation :
                allocationRepo
                        .findAllByScholarshipHistoryScholarshipHistoryId(
                                scholarshipHistoryId
                        )) {

            if (allocation != null
                    && allocation.getAllocatedAmountUgx() != null) {
                totalAllocated =
                        totalAllocated.add(
                                allocation.getAllocatedAmountUgx()
                        );
            }
        }

        return totalAllocated;
    }

    private void validateAllocationAgainstApproval(
            ErpScholarshipHistory history,
            BigDecimal newAmount
    ) {
        BigDecimal alreadyAllocated =
                allocatedAmountForHistory(
                        history.getScholarshipHistoryId()
                );

        BigDecimal approved =
                history.getApprovedAmount();

        BigDecimal remaining =
                approved.subtract(alreadyAllocated);

        if (newAmount.compareTo(remaining) > 0) {
            throw new BadRequestException(
                    "Allocation amount exceeds the remaining approved Scholarship amount. "
                            + "Approved: " + approved
                            + ", Already allocated: " + alreadyAllocated
                            + ", Remaining: " + remaining
            );
        }
    }

    private void validateAllocationPeriod(
            String academicYear,
            String term
    ) {
        if (academicYear == null || academicYear.isBlank()) {
            throw new BadRequestException(
                    "Academic year is required for a Scholarship allocation."
            );
        }

        if (term == null || term.isBlank()) {
            throw new BadRequestException(
                    "Term is required for a Scholarship allocation."
            );
        }
    }

    @Override
    public void allocateToBranch(AllocationRequestDTO request) {
        if (request == null) {
            throw new BadRequestException(
                    "Allocation request is required."
            );
        }

        if (request.getBranchId() == null) {
            throw new BadRequestException(
                    "Branch ID is required."
            );
        }

        validateDonationReference(
                request.getDonationId()
        );

        BigDecimal amount =
                requirePositiveAllocationAmount(
                        request.getAmountUgx()
                );

        validateAllocationPeriod(
                request.getAcademicYear(),
                request.getTerm()
        );

        ErpBranchFundAllocation alloc = new ErpBranchFundAllocation();
        alloc.setBranchId(request.getBranchId());
        alloc.setAllocatedAmountUgx(amount);
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
        if (request == null) {
            throw new BadRequestException(
                    "Allocation request is required."
            );
        }

        if (request.getBranchId() == null) {
            throw new BadRequestException(
                    "Branch ID is required."
            );
        }

        if (request.getStudentId() == null) {
            throw new BadRequestException(
                    "Student ID is required."
            );
        }

        validateDonationReference(
                request.getDonationId()
        );

        BigDecimal amount =
                requirePositiveAllocationAmount(
                        request.getAmountUgx()
                );

        validateAllocationPeriod(
                request.getAcademicYear(),
                request.getTerm()
        );

        ErpScholarshipHistory history =
                requireAllocationHistory(
                        request.getScholarshipHistoryId(),
                        request.getBranchId()
                );

        validateAllocationAgainstApproval(
                history,
                amount
        );

        ErpScholarshipAllocation alloc = new ErpScholarshipAllocation();
        alloc.setScholarshipHistory(history);
        alloc.setBranchId(request.getBranchId());
        alloc.setStudentId(request.getStudentId());
        alloc.setAllocatedAmountUgx(amount);
        alloc.setTerm(request.getTerm());
        alloc.setAcademicYear(request.getAcademicYear());
        alloc.setAllocatedByUserId(
                currentUserId()
        );
        alloc.setDonationId(request.getDonationId());
        allocationRepo.save(alloc);
    }

    @SuppressWarnings("unused")
    @Transactional
    public void allocateToMultipleStudents(
            ScholarshipBulkAllocationRequestDTO request
    ) {
        if (request == null) {
            throw new BadRequestException(
                    "Bulk allocation request is required."
            );
        }

        if (request.getBranchId() == null) {
            throw new BadRequestException(
                    "Branch ID is required."
            );
        }

        if (request.getDonationId() == null) {
            throw new BadRequestException(
                    "Donation ID is required."
            );
        }

        if (request.getScholarshipHistoryId() == null) {
            throw new BadRequestException(
                    "Scholarship History ID is required."
            );
        }

        if (request.getAllocations() == null
                || request.getAllocations().isEmpty()) {
            throw new BadRequestException(
                    "At least one student allocation is required."
            );
        }

        validateDonationReference(
                request.getDonationId()
        );

        ErpScholarshipHistory history =
                requireAllocationHistory(
                        request.getScholarshipHistoryId(),
                        request.getBranchId()
                );

        BigDecimal batchTotal = BigDecimal.ZERO;

        for (ScholarshipBulkAllocationRequestDTO.StudentAllocationItem item :
                request.getAllocations()) {

            if (item == null || item.getStudentId() == null) {
                throw new BadRequestException(
                        "Every bulk allocation must contain a student ID."
                );
            }

            BigDecimal amount =
                    requirePositiveAllocationAmount(
                            item.getAmountUgx()
                    );

            validateAllocationPeriod(
                    item.getAcademicYear(),
                    item.getTerm()
            );

            batchTotal =
                    batchTotal.add(
                            amount
                    );
        }

        validateAllocationAgainstApproval(
                history,
                batchTotal
        );

        for (ScholarshipBulkAllocationRequestDTO.StudentAllocationItem item :
                request.getAllocations()) {

            BigDecimal amount =
                    requirePositiveAllocationAmount(
                            item.getAmountUgx()
                    );

            ErpScholarshipAllocation allocation =
                    new ErpScholarshipAllocation();

            allocation.setScholarshipHistory(
                    history
            );
            allocation.setBranchId(
                    request.getBranchId()
            );
            allocation.setStudentId(
                    item.getStudentId()
            );
            allocation.setAllocatedAmountUgx(
                    amount
            );
            allocation.setTerm(
                    item.getTerm()
            );
            allocation.setAcademicYear(
                    item.getAcademicYear()
            );
            allocation.setAllocatedByUserId(
                    currentUserId()
            );
            allocation.setDonationId(
                    request.getDonationId()
            );

            allocationRepo.save(
                    allocation
            );
        }
    }

    @SuppressWarnings("unused")
    @Transactional
    public void allocateMultipleDonorsToStudent(
            ScholarshipMultipleDonorAllocationRequestDTO request
    ) {
        if (request == null) {
            throw new BadRequestException(
                    "Multiple-donor allocation request is required."
            );
        }

        if (request.getBranchId() == null) {
            throw new BadRequestException(
                    "Branch ID is required."
            );
        }

        if (request.getStudentId() == null) {
            throw new BadRequestException(
                    "Student ID is required."
            );
        }

        if (request.getScholarshipHistoryId() == null) {
            throw new BadRequestException(
                    "Scholarship History ID is required."
            );
        }

        if (request.getAllocations() == null
                || request.getAllocations().isEmpty()) {
            throw new BadRequestException(
                    "At least one donor allocation is required."
            );
        }

        ErpScholarshipHistory history =
                requireAllocationHistory(
                        request.getScholarshipHistoryId(),
                        request.getBranchId()
                );

        BigDecimal batchTotal = BigDecimal.ZERO;

        for (ScholarshipMultipleDonorAllocationRequestDTO.DonorAllocationItem item :
                request.getAllocations()) {

            if (item == null || item.getDonationId() == null) {
                throw new BadRequestException(
                        "Every allocation must contain a donor reference."
                );
            }

            validateDonationReference(
                    item.getDonationId()
            );

            BigDecimal amount =
                    requirePositiveAllocationAmount(
                            item.getAmountUgx()
                    );

            validateAllocationPeriod(
                    item.getAcademicYear(),
                    item.getTerm()
            );

            batchTotal =
                    batchTotal.add(
                            amount
                    );
        }

        validateAllocationAgainstApproval(
                history,
                batchTotal
        );

        for (ScholarshipMultipleDonorAllocationRequestDTO.DonorAllocationItem item :
                request.getAllocations()) {

            BigDecimal amount =
                    requirePositiveAllocationAmount(
                            item.getAmountUgx()
                    );

            ErpScholarshipAllocation allocation =
                    new ErpScholarshipAllocation();

            allocation.setScholarshipHistory(
                    history
            );
            allocation.setBranchId(
                    request.getBranchId()
            );
            allocation.setStudentId(
                    request.getStudentId()
            );
            allocation.setDonationId(
                    item.getDonationId()
            );
            allocation.setAllocatedAmountUgx(
                    amount
            );
            allocation.setTerm(
                    item.getTerm()
            );
            allocation.setAcademicYear(
                    item.getAcademicYear()
            );
            allocation.setAllocatedByUserId(
                    currentUserId()
            );

            allocationRepo.save(
                    allocation
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ScholarshipApplicationStatus getApplicationStatus(
            Long applicationId
    ) {
        if (applicationId == null || applicationId <= 0L) {
            throw new BadRequestException(
                    "A valid admission application ID is required."
            );
        }

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

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime publicExpiresAt =
                scholarship.getTokenExpiresAt();

        LocalDateTime schoolExpiresAt =
                scholarship.getSchoolAccessExpiresAt();

        boolean publicLinkActive =
                publicExpiresAt != null
                && publicExpiresAt.isAfter(now)
                && scholarship.getPublicTokenHash() != null
                && !scholarship.getPublicTokenHash().isBlank();

        boolean schoolAccessActive =
                schoolExpiresAt != null
                && schoolExpiresAt.isAfter(now)
                && scholarship.getSchoolAccessTokenHash() != null
                && !scholarship.getSchoolAccessTokenHash().isBlank();

        String applicationMethod =
                latestHistory(scholarship)
                        .map(ErpScholarshipHistory::getApplicationMethod)
                        .orElse(null);

        String status =
                normalizeScholarshipStatus(
                        scholarship.getStatus()
                );

        if (publicLinkActive) {
            status = "LINK_PENDING";
        } else if (
                "LINK_PENDING".equals(status)
                && publicExpiresAt != null
                && !publicExpiresAt.isAfter(now)
        ) {
            status = "LINK_EXPIRED";
        } else if (
                schoolAccessActive
                && "SCHOOL_ASSISTED".equalsIgnoreCase(
                        applicationMethod
                )
        ) {
            status = "IN_PROGRESS";
        } else if (
                "IN_PROGRESS".equals(status)
                && "SCHOOL_ASSISTED".equalsIgnoreCase(
                        applicationMethod
                )
                && schoolExpiresAt != null
                && !schoolExpiresAt.isAfter(now)
        ) {
            status = "SCHOOL_ACCESS_EXPIRED";
        }

        return new ScholarshipApplicationStatus(
                status,
                applicationMethod,
                publicExpiresAt,
                schoolExpiresAt,
                publicLinkActive,
                schoolAccessActive
        );
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

        ErpScholarshipHistory history = getOrCreateCurrentHistory(
                scholarshipApplication,
                Long.valueOf(context.getUserId())
        );
        history.setApplicationMethod(
                ErpScholarshipApplication.ApplicationMethod.EMAIL_LINK.name()
        );
        historyRepo.save(history);

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

        ErpScholarshipHistory history = getOrCreateCurrentHistory(
                scholarship,
                userId
        );
        history.setApplicationMethod(
                ErpScholarshipApplication.ApplicationMethod.SCHOOL_ASSISTED.name()
        );
        historyRepo.save(history);

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

        if (isNotHistoryApplicationMethod(
                scholarship,
                ErpScholarshipApplication.ApplicationMethod.SCHOOL_ASSISTED
        )) {
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

        ErpScholarshipHistory history =
                getOrCreateCurrentHistory(scholarship, actorUserId);

        // Admission-family identity remains on the admission application.
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

        history.setFatherStatus(clean(request.getFatherStatus()));
        history.setFatherAnnualIncome(nonNegative(request.getFatherAnnualIncome()));
        history.setMotherStatus(clean(request.getMotherStatus()));
        history.setMotherAnnualIncome(nonNegative(request.getMotherAnnualIncome()));

        history.setResponsiblePersonType(clean(request.getResponsiblePersonType()));
        history.setResponsiblePersonName(clean(request.getResponsiblePersonName()));
        history.setResponsiblePersonRelation(clean(request.getResponsiblePersonRelation()));
        history.setResponsiblePersonOccupation(clean(request.getResponsiblePersonOccupation()));
        history.setResponsiblePersonMobile(clean(request.getResponsiblePersonMobile()));
        history.setResponsiblePersonAnnualIncome(
                nonNegative(request.getResponsiblePersonAnnualIncome())
        );

        history.setOrphanStatus(clean(request.getOrphanStatus()));
        history.setHouseholdSize(nonNegativeInt(request.getHouseholdSize()));
        history.setDependantsCount(nonNegativeInt(request.getDependantsCount()));
        history.setSchoolGoingChildren(nonNegativeInt(request.getSchoolGoingChildren()));
        history.setMainIncomeEarner(clean(request.getMainIncomeEarner()));
        history.setIncomeSource(clean(request.getIncomeSource()));
        history.setOtherHouseholdIncome(nonNegative(request.getOtherHouseholdIncome()));
        history.setHousingStatus(clean(request.getHousingStatus()));

        history.setHouseType(clean(request.getHouseType()));
        history.setHouseRoomCount(nonNegativeInt(request.getHouseRoomCount()));
        history.setHouseLocation(clean(request.getHouseLocation()));
        history.setHouseEstimatedValue(nonNegative(request.getHouseEstimatedValue()));
        history.setHouseMortgaged(Boolean.TRUE.equals(request.getHouseMortgaged()));
        history.setMonthlyRent(nonNegative(request.getMonthlyRent()));

        boolean landOwned = Boolean.TRUE.equals(request.getLandOwned());
        history.setLandOwned(landOwned);
        history.setLandArea(landOwned ? nonNegative(request.getLandArea()) : null);
        history.setLandUnit(landOwned ? clean(request.getLandUnit()) : null);
        history.setLandPlotCount(landOwned ? nonNegativeInt(request.getLandPlotCount()) : null);
        history.setLandLocation(landOwned ? clean(request.getLandLocation()) : null);
        history.setLandUsage(landOwned ? clean(request.getLandUsage()) : null);
        history.setLandEstimatedValue(
                landOwned ? nonNegative(request.getLandEstimatedValue()) : null
        );

        boolean landGeneratesIncome =
                landOwned && Boolean.TRUE.equals(request.getLandGeneratesIncome());
        history.setLandGeneratesIncome(landGeneratesIncome);
        history.setLandAnnualIncome(
                landGeneratesIncome
                        ? nonNegative(request.getLandAnnualIncome())
                        : null
        );

        boolean vehiclesOwned = Boolean.TRUE.equals(request.getVehiclesOwned());
        history.setVehiclesOwned(vehiclesOwned);
        history.setVehicleCount(
                vehiclesOwned ? nonNegativeInt(request.getVehicleCount()) : null
        );
        history.setVehicleDescription(
                vehiclesOwned ? clean(request.getVehicleDescription()) : null
        );
        history.setVehicleType(
                vehiclesOwned ? clean(request.getVehicleType()) : null
        );
        history.setVehicleUsage(
                vehiclesOwned ? clean(request.getVehicleUsage()) : null
        );
        history.setVehicleEstimatedValue(
                vehiclesOwned ? nonNegative(request.getVehicleEstimatedValue()) : null
        );
        history.setVehicleFinanced(
                vehiclesOwned && Boolean.TRUE.equals(request.getVehicleFinanced())
        );

        boolean businessOwned = Boolean.TRUE.equals(request.getBusinessOwned());
        history.setBusinessOwned(businessOwned);
        history.setBusinessName(businessOwned ? clean(request.getBusinessName()) : null);
        history.setBusinessType(businessOwned ? clean(request.getBusinessType()) : null);
        history.setBusinessLocation(
                businessOwned ? clean(request.getBusinessLocation()) : null
        );
        history.setBusinessEmployeeCount(
                businessOwned ? nonNegativeInt(request.getBusinessEmployeeCount()) : null
        );
        history.setBusinessAnnualIncome(
                businessOwned ? nonNegative(request.getBusinessAnnualIncome()) : null
        );

        boolean livestockOwned = Boolean.TRUE.equals(request.getLivestockOwned());
        history.setLivestockOwned(livestockOwned);
        history.setLivestockDescription(
                livestockOwned ? clean(request.getLivestockDescription()) : null
        );
        history.setLivestockEstimatedValue(
                livestockOwned ? nonNegative(request.getLivestockEstimatedValue()) : null
        );
        history.setLivestockAnnualIncome(
                livestockOwned ? nonNegative(request.getLivestockAnnualIncome()) : null
        );

        history.setOtherAssetsDescription(clean(request.getOtherAssetsDescription()));
        history.setOtherAssetsEstimatedValue(
                nonNegative(request.getOtherAssetsEstimatedValue())
        );
        history.setOtherAssetsAnnualIncome(
                nonNegative(request.getOtherAssetsAnnualIncome())
        );
        history.setFinancialHardshipReason(clean(request.getFinancialHardshipReason()));
        history.setFamilySituationRemarks(clean(request.getFamilySituationRemarks()));

        history.setParentGuardianName(clean(request.getParentGuardianName()));
        history.setParentGuardianRelation(clean(request.getParentGuardianRelation()));
        history.setParentGuardianMobile(clean(request.getParentGuardianMobile()));
        history.setDeclarationAccepted(
                Boolean.TRUE.equals(request.getDeclarationAccepted())
        );

        replaceSiblings(scholarship, request.getSiblings(), actorUserId);

        history.setHouseholdIncome(
                calculateAnnualHouseholdIncome(
                        history.getFatherAnnualIncome(),
                        history.getMotherAnnualIncome(),
                        history.getResponsiblePersonAnnualIncome(),
                        history.getOtherHouseholdIncome(),
                        history.getLandAnnualIncome(),
                        history.getBusinessAnnualIncome(),
                        history.getLivestockAnnualIncome(),
                        history.getOtherAssetsAnnualIncome(),
                        request.getSiblings()
                )
        );

        history.setApplicationMethod(applicationMethod.name());

        LocalDateTime now = LocalDateTime.now();

        if (submit) {
            if (!Boolean.TRUE.equals(history.getDeclarationAccepted())) {
                throw new BadRequestException(
                        "Declaration must be accepted before submitting the scholarship application."
                );
            }

            scholarship.setStatus("SUBMITTED");
            history.setStatus("SUBMITTED");
            history.setSubmittedAt(now);

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
            history.setStatus("IN_PROGRESS");
        }

        if (actorUserId != null) {
            scholarship.setUpdatedBy(actorUserId);
            history.setUpdatedBy(actorUserId);
        }

        history.setUpdatedAt(now);
        historyRepo.saveAndFlush(history);
        ErpScholarshipApplication saved = applicationRepo.saveAndFlush(scholarship);

        return buildFormResponse(
                saved,
                saved.getBranchId(),
                includeInternalIds
        );
    }

    private ErpScholarshipHistory getOrCreateCurrentHistory(
            ErpScholarshipApplication scholarship,
            Long actorUserId
    ) {
        return historyRepo
                .findFirstByScholarshipApplicationScholarshipAppIdOrderByScholarshipHistoryIdDesc(
                        scholarship.getScholarshipAppId()
                )
                .orElseGet(() -> {
                    ErpScholarshipHistory history =
                            new ErpScholarshipHistory();

                    history.setScholarshipApplication(
                            scholarship
                    );

                    history.setBranchId(
                            scholarship.getBranchId()
                    );

                    history.setStudentId(
                            scholarship.getStudent() != null
                                    ? scholarship.getStudent().getStudentId()
                                    : null
                    );

                    history.setApplicationId(
                            scholarship.getApplication() != null
                                    ? scholarship.getApplication().getApplicationId()
                                    : null
                    );

                    history.setAcademicYear(
                            scholarship.getAcademicYear()
                    );

                    /*
                     * Scholarship request data belongs to History.
                     * Do not read these values from ErpScholarshipApplication.
                     */

                    String term =
                            scholarship.getApplication() != null
                                    ? scholarship.getApplication().getTerm()
                                    : null;

                    history.setTermRequested(
                            term != null && !term.isBlank()
                                    ? term
                                    : "TERM_1"
                    );

                    history.setCategory(
                            "GENERAL"
                    );

                    history.setScholarshipType(
                            ErpScholarshipHistory.ScholarshipType.OTHER
                    );

                    /*
                     * The Fee Discussion is the source for the financial
                     * scholarship request. Copy its values into the new
                     * History snapshot so the NOT NULL database columns
                     * are populated when the access link is issued.
                     */
                    BigDecimal amountRequested =
                            BigDecimal.ZERO;

                    BigDecimal requestedPercentage =
                            BigDecimal.ZERO;

                    if (scholarship.getApplication() != null
                            && scholarship.getApplication().getApplicationId() != null
                            && scholarship.getBranchId() != null) {

                        feeRepo.findActiveByApplicationAndBranch(
                                        scholarship.getApplication()
                                                .getApplicationId(),
                                        scholarship.getBranchId().intValue()
                                )
                                .ifPresent(fee -> {

                                    BigDecimal assistance =
                                            fee.getAssistanceRequired();

                                    BigDecimal baseFee =
                                            fee.getBaseFeeAmount();

                                    if (assistance != null) {
                                        history.setAmountRequestedUgx(
                                                assistance
                                        );
                                    }

                                    if (assistance != null
                                            && baseFee != null
                                            && baseFee.compareTo(
                                            BigDecimal.ZERO
                                    ) > 0) {

                                        history.setRequestedPercentage(
                                                assistance
                                                        .multiply(
                                                                BigDecimal.valueOf(100)
                                                        )
                                                        .divide(
                                                                baseFee,
                                                                2,
                                                                java.math.RoundingMode.HALF_UP
                                                        )
                                        );
                                    }
                                });
                    }

                    /*
                     * The database requires both values to be non-null.
                     * If Fee Discussion has no amount yet, retain zero rather
                     * than inserting NULL.
                     */
                    if (history.getAmountRequestedUgx() == null) {
                        history.setAmountRequestedUgx(
                                amountRequested
                        );
                    }

                    if (history.getRequestedPercentage() == null) {
                        history.setRequestedPercentage(
                                requestedPercentage
                        );
                    }

                    history.setApplicationMethod(
                            ErpScholarshipApplication.ApplicationMethod
                                    .SCHOOL_ASSISTED
                                    .name()
                    );

                    history.setCreatedBy(
                            actorUserId
                    );

                    history.setCreatedAt(
                            LocalDateTime.now()
                    );

                    history.setActive(
                            true
                    );

                    return history;
                });
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

        if (isNotHistoryApplicationMethod(
                scholarship,
                ErpScholarshipApplication.ApplicationMethod.SCHOOL_ASSISTED
        )) {
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
                || isNotHistoryApplicationMethod(
                        scholarship,
                        ErpScholarshipApplication.ApplicationMethod.EMAIL_LINK
                )) {
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

    private boolean isNotHistoryApplicationMethod(
            ErpScholarshipApplication scholarship,
            ErpScholarshipApplication.ApplicationMethod expected
    ) {
        return latestHistory(scholarship)
                .map(ErpScholarshipHistory::getApplicationMethod)
                .map(value -> !expected.name().equalsIgnoreCase(value))
                .orElse(true);
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
        return buildFormResponse(scholarship, branchId, true);
    }

    private ScholarshipApplicationFormResponseDTO buildFormResponse(
            ErpScholarshipApplication scholarship,
            Long branchId,
            boolean includeInternalIds
    ) {
        ScholarshipApplicationFormResponseDTO response =
                new ScholarshipApplicationFormResponseDTO();

        ErpApplication application = scholarship.getApplication();
        ErpScholarshipHistory history =
                latestHistory(scholarship).orElse(null);

        if (includeInternalIds) {
            response.setScholarshipAppId(scholarship.getScholarshipAppId());
            response.setApplicationId(
                    application != null ? application.getApplicationId() : null
            );
        }

        if (application != null) {
            response.setApplicationNo(application.getApplicationNo());
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
            response.setAcademicYear(scholarship.getAcademicYear());
            response.setTerm(application.getTerm());

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
                    .ifPresent(fee -> {
                        response.setTotalFee(fee.getBaseFeeAmount());
                        response.setParentContribution(fee.getParentCanPay());
                        response.setScholarshipRequiredAmount(fee.getAssistanceRequired());
                    });
        }

        if (history != null) {
            response.setScholarshipType(
                    history.getScholarshipType() != null
                            ? history.getScholarshipType().name()
                            : null
            );
            response.setApplicationMethod(history.getApplicationMethod());
            response.setStatus(history.getStatus() != null
                    ? history.getStatus()
                    : scholarship.getStatus());

            response.setFatherStatus(history.getFatherStatus());
            response.setFatherAnnualIncome(history.getFatherAnnualIncome());
            response.setMotherStatus(history.getMotherStatus());
            response.setMotherAnnualIncome(history.getMotherAnnualIncome());
            response.setResponsiblePersonType(history.getResponsiblePersonType());
            response.setResponsiblePersonName(history.getResponsiblePersonName());
            response.setResponsiblePersonRelation(history.getResponsiblePersonRelation());
            response.setResponsiblePersonOccupation(history.getResponsiblePersonOccupation());
            response.setResponsiblePersonMobile(history.getResponsiblePersonMobile());
            response.setResponsiblePersonAnnualIncome(history.getResponsiblePersonAnnualIncome());
            response.setOrphanStatus(history.getOrphanStatus());

            response.setHouseholdSize(history.getHouseholdSize());
            response.setDependantsCount(history.getDependantsCount());
            response.setSchoolGoingChildren(history.getSchoolGoingChildren());
            response.setMainIncomeEarner(history.getMainIncomeEarner());
            response.setIncomeSource(history.getIncomeSource());
            response.setOtherHouseholdIncome(history.getOtherHouseholdIncome());
            response.setHouseholdIncome(history.getHouseholdIncome());
            response.setHousingStatus(history.getHousingStatus());

            response.setHouseType(history.getHouseType());
            response.setHouseRoomCount(history.getHouseRoomCount());
            response.setHouseLocation(history.getHouseLocation());
            response.setHouseEstimatedValue(history.getHouseEstimatedValue());
            response.setHouseMortgaged(history.getHouseMortgaged());
            response.setMonthlyRent(history.getMonthlyRent());

            response.setLandOwned(history.getLandOwned());
            response.setLandArea(history.getLandArea());
            response.setLandUnit(history.getLandUnit());
            response.setLandPlotCount(history.getLandPlotCount());
            response.setLandLocation(history.getLandLocation());
            response.setLandUsage(history.getLandUsage());
            response.setLandEstimatedValue(history.getLandEstimatedValue());
            response.setLandGeneratesIncome(history.getLandGeneratesIncome());
            response.setLandAnnualIncome(history.getLandAnnualIncome());

            response.setVehiclesOwned(history.getVehiclesOwned());
            response.setVehicleCount(history.getVehicleCount());
            response.setVehicleDescription(history.getVehicleDescription());
            response.setVehicleType(history.getVehicleType());
            response.setVehicleUsage(history.getVehicleUsage());
            response.setVehicleEstimatedValue(history.getVehicleEstimatedValue());
            response.setVehicleFinanced(history.getVehicleFinanced());

            response.setBusinessOwned(history.getBusinessOwned());
            response.setBusinessName(history.getBusinessName());
            response.setBusinessType(history.getBusinessType());
            response.setBusinessLocation(history.getBusinessLocation());
            response.setBusinessEmployeeCount(history.getBusinessEmployeeCount());
            response.setBusinessAnnualIncome(history.getBusinessAnnualIncome());

            response.setLivestockOwned(history.getLivestockOwned());
            response.setLivestockDescription(history.getLivestockDescription());
            response.setLivestockEstimatedValue(history.getLivestockEstimatedValue());
            response.setLivestockAnnualIncome(history.getLivestockAnnualIncome());

            response.setOtherAssetsDescription(history.getOtherAssetsDescription());
            response.setOtherAssetsEstimatedValue(history.getOtherAssetsEstimatedValue());
            response.setOtherAssetsAnnualIncome(history.getOtherAssetsAnnualIncome());

            response.setFinancialHardshipReason(history.getFinancialHardshipReason());
            response.setFamilySituationRemarks(history.getFamilySituationRemarks());
            response.setParentGuardianName(history.getParentGuardianName());
            response.setParentGuardianRelation(history.getParentGuardianRelation());
            response.setParentGuardianMobile(history.getParentGuardianMobile());
            response.setDeclarationAccepted(history.getDeclarationAccepted());
            response.setSubmittedAt(history.getSubmittedAt());
        }

        List<ScholarshipApplicationFormResponseDTO.SiblingResponse> siblingResponses =
                new ArrayList<>();

        for (ErpScholarshipSibling sibling :
                siblingRepo.findByScholarshipApplicationScholarshipAppIdAndActiveTrueOrderBySiblingIdAsc(
                        scholarship.getScholarshipAppId()
                )) {
            ScholarshipApplicationFormResponseDTO.SiblingResponse item =
                    new ScholarshipApplicationFormResponseDTO.SiblingResponse();

            item.setSiblingId(
                    includeInternalIds ? sibling.getScholarshipSiblingId() : null
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

        response.setSiblings(siblingResponses);
        return response;
    }

    private java.util.Optional<ErpScholarshipHistory> latestHistory(
            ErpScholarshipApplication scholarship
    ) {
        if (scholarship == null || scholarship.getScholarshipAppId() == null) {
            return java.util.Optional.empty();
        }

        return historyRepo
                .findFirstByScholarshipApplicationScholarshipAppIdOrderByScholarshipHistoryIdDesc(
                        scholarship.getScholarshipAppId()
                );
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

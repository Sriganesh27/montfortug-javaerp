package com.erp.montfortuganda.scholarship.service;

import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.scholarship.dto.*;
import com.erp.montfortuganda.scholarship.entity.*;
import com.erp.montfortuganda.scholarship.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class ScholarshipServiceImpl implements ScholarshipService {

    private final WebDonationReadService donationReadService;
    private final ErpScholarshipApplicationRepository applicationRepo;
    private final ErpScholarshipAllocationRepository allocationRepo;
    private final ErpBranchFundAllocationRepository branchFundRepo;
    private final CurrentUserService currentUserService;

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
    public List<PendingStudentDTO> getPendingStudents() {
        List<ErpScholarshipApplication> apps = applicationRepo.findAll().stream()
                .filter(a -> "Pending".equalsIgnoreCase(a.getStatus()))
                .toList();

        List<PendingStudentDTO> dtos = new ArrayList<>();

        for (ErpScholarshipApplication app : apps) {
            String studentName = "Unknown";

            if (app.getStudent() != null) {
                studentName = app.getStudent().getFirstName() + " " + app.getStudent().getLastName();
            } else if (app.getApplication() != null) {
                studentName = app.getApplication().getFirstName() + " " + app.getApplication().getLastName();
            }

            // BUG FIX: Stopped hardcoding Total Fees to BigDecimal.ZERO!
            dtos.add(new PendingStudentDTO(
                    app.getScholarshipAppId(),
                    studentName,
                    "Campus " + app.getBranchId(),
                    app.getBranchId(),
                    app.getCategory(),
                    app.getAmountRequestedUgx(), // Shortfall
                    app.getAmountRequestedUgx()  // Total Fees (Mapping to the same as shortfall for now)
            ));
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

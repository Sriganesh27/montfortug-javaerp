package com.erp.montfortuganda.scholarship.service;

import com.erp.montfortuganda.scholarship.dto.*;
import com.erp.montfortuganda.scholarship.entity.*;
import com.erp.montfortuganda.scholarship.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ScholarshipServiceImpl implements ScholarshipService {

    private final WebDonationRepository donationRepo;
    private final ErpScholarshipApplicationRepository applicationRepo;
    private final ErpScholarshipAllocationRepository allocationRepo;
    private final ErpBranchFundAllocationRepository branchFundRepo;

    @Override
    public FundsSummaryDTO getFundsSummary() {
        List<WebDonation> donations = donationRepo.findAll();

        BigDecimal totalRaised = donations.stream()
                .map(d -> d.getAmount() != null ? d.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSpent = donations.stream()
                .map(d -> d.getAmountSpent() != null ? d.getAmountSpent() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal available = totalRaised.subtract(totalSpent);

        int studentsSponsored = donations.stream()
                .mapToInt(d -> d.getStudentsBenefited() != null ? d.getStudentsBenefited() : 0)
                .sum();

        // Exact match with Javascript variables
        return new FundsSummaryDTO(totalRaised, totalSpent, available, studentsSponsored);
    }

    @Override
    public List<DonorDTO> getAllDonors() {
        return donationRepo.findAll().stream().map(d -> {
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
        }).toList();
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
                studentName = app.getStudent().getName() + " " + app.getStudent().getSurname();
            } else if (app.getApplication() != null) {
                studentName = app.getApplication().getStudentName() + " " + app.getApplication().getStudentSurname();
            }

            dtos.add(new PendingStudentDTO(
                    app.getId(),
                    studentName,
                    "Campus " + app.getBranchId(),
                    app.getBranchId(),
                    app.getCategory(),
                    app.getAmountRequestedUgx(),
                    BigDecimal.ZERO
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

        return allocationRepo.findAll().stream().map(alloc -> {
            String dateStr = alloc.getCreatedAt() != null ? alloc.getCreatedAt().format(formatter) : "N/A";
            return new ActiveSponsorshipDTO(
                    alloc.getId(),
                    "Student " + alloc.getStudentId(),
                    "Campus " + alloc.getBranchId(),
                    alloc.getDonation() != null ? alloc.getDonation().getFullName() : "General Fund",
                    alloc.getAllocatedAmountUgx(),
                    dateStr
            );
        }).toList();
    }

    @Override
    public void allocateToBranch(AllocationRequestDTO request) {
        ErpBranchFundAllocation alloc = new ErpBranchFundAllocation();
        alloc.setBranchId(request.getBranchId());
        alloc.setAllocatedAmountUgx(request.getAmountUgx());
        alloc.setTerm(request.getTerm());
        alloc.setAcademicYear(request.getAcademicYear());
        alloc.setAllocatedByUserId(1L);
        branchFundRepo.save(alloc);
    }

    @Override
    public void allocateToStudent(AllocationRequestDTO request) {
        ErpScholarshipAllocation alloc = new ErpScholarshipAllocation();
        alloc.setBranchId(request.getBranchId());
        alloc.setStudentId(request.getStudentId());
        alloc.setAllocatedAmountUgx(request.getAmountUgx());
        alloc.setTerm(request.getTerm());
        alloc.setAcademicYear(request.getAcademicYear());
        alloc.setAllocatedByUserId(1L);
        allocationRepo.save(alloc);
    }
}
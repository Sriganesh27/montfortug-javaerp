package com.erp.montfortuganda.scholarship.service;

import com.erp.montfortuganda.scholarship.ScholarshipAllocation;
import com.erp.montfortuganda.scholarship.ScholarshipBranchAllocation;
import com.erp.montfortuganda.scholarship.dto.ScholarshipAllocationRequestDTO;
import com.erp.montfortuganda.scholarship.dto.ScholarshipBranchDemandDTO;
import com.erp.montfortuganda.scholarship.dto.ScholarshipFundsSummaryDTO;
import com.erp.montfortuganda.scholarship.repository.ScholarshipAllocationRepository;
import com.erp.montfortuganda.scholarship.repository.ScholarshipBranchAllocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScholarshipService {

    private final ScholarshipAllocationRepository studentAllocationRepo;
    private final ScholarshipBranchAllocationRepository branchAllocationRepo;
    private final EntityManager entityManager;

    public ScholarshipFundsSummaryDTO getFundsSummary() {
        BigDecimal totalRaised = (BigDecimal) entityManager.createNativeQuery(
                "SELECT COALESCE(SUM(amount), 0) FROM web_donations WHERE project_id = 'SSP001'"
        ).getSingleResult();

        BigDecimal branchSpent = (BigDecimal) entityManager.createNativeQuery(
                "SELECT COALESCE(SUM(allocated_amount_ugx), 0) FROM erp_branch_fund_allocations"
        ).getSingleResult();

        BigDecimal studentSpent = (BigDecimal) entityManager.createNativeQuery(
                "SELECT COALESCE(SUM(allocated_amount_ugx), 0) FROM erp_scholarship_allocations"
        ).getSingleResult();

        BigDecimal totalSpent = branchSpent.add(studentSpent);
        BigDecimal availableBalance = totalRaised.subtract(totalSpent);

        Number studentsSponsored = (Number) entityManager.createNativeQuery(
                "SELECT COUNT(DISTINCT student_id) FROM erp_scholarship_allocations"
        ).getSingleResult();

        ScholarshipFundsSummaryDTO dto = new ScholarshipFundsSummaryDTO();
        dto.setTotalRaisedUgx(totalRaised);
        dto.setTotalSpentUgx(totalSpent);
        dto.setAvailableBalanceUgx(availableBalance);
        dto.setStudentsSponsored(studentsSponsored.intValue());

        return dto;
    }

    public List<ScholarshipBranchDemandDTO> getGlobalDemands() {
        List<Object[]> results = entityManager.createNativeQuery(
                "SELECT branch_id, COUNT(id) as total_applicants, COALESCE(SUM(amount_requested_ugx), 0) as total_requested " +
                        "FROM erp_scholarship_applications WHERE status = 'Pending' GROUP BY branch_id"
        ).getResultList();

        return results.stream().map(row -> {
            ScholarshipBranchDemandDTO dto = new ScholarshipBranchDemandDTO();
            dto.setBranchId((Integer) row[0]);
            dto.setTotalApplicants(((Number) row[1]).longValue());
            dto.setTotalRequestedAmountUgx((BigDecimal) row[2]);
            return dto;
        }).toList();
    }

    

    public List<com.erp.montfortuganda.scholarship.dto.ScholarshipDonorDTO> getDonors() {
        List<Object[]> results = entityManager.createNativeQuery(
            "SELECT id, receipt_number, full_name, email, currency, amount as amount_received_ugx, 0 as amount_spent_ugx FROM web_donations WHERE project_id = 'SSP001'"
        ).getResultList();
        
        return results.stream().map(row -> {
            com.erp.montfortuganda.scholarship.dto.ScholarshipDonorDTO dto = new com.erp.montfortuganda.scholarship.dto.ScholarshipDonorDTO();
            dto.setId(((Number) row[0]).intValue());
            dto.setReceiptNumber((String) row[1]);
            dto.setFullName((String) row[2]);
            dto.setEmail((String) row[3]);
            dto.setCurrency((String) row[4]);
            dto.setAmountReceivedUgx((java.math.BigDecimal) row[5]);
            dto.setAmountSpentUgx(java.math.BigDecimal.ZERO);
            return dto;
        }).toList();
    }

    public List<com.erp.montfortuganda.scholarship.dto.ScholarshipPendingStudentDTO> getPendingStudents() {
        List<Object[]> results = entityManager.createNativeQuery(
            "SELECT id as student_id, 'Student ' || id as student_name, branch_id as campus, amount_requested_ugx as shortfall_ugx, 'N/A' as current_class, 0 as fees_ugx, 'None' as hardship_reason, 0 as academic_score FROM erp_scholarship_applications WHERE status = 'Pending'"
        ).getResultList();
        
        return results.stream().map(row -> {
            com.erp.montfortuganda.scholarship.dto.ScholarshipPendingStudentDTO dto = new com.erp.montfortuganda.scholarship.dto.ScholarshipPendingStudentDTO();
            dto.setStudentId(((Number) row[0]).intValue());
            dto.setStudentName((String) row[1]);
            dto.setCampus(String.valueOf(row[2]));
            dto.setShortfallUgx((java.math.BigDecimal) row[3]);
            dto.setCurrentClass((String) row[4]);
            dto.setFeesUgx(java.math.BigDecimal.ZERO);
            dto.setHardshipReason((String) row[6]);
            dto.setAcademicScore(java.math.BigDecimal.ZERO);
            return dto;
        }).toList();
    }

    public List<com.erp.montfortuganda.scholarship.dto.ScholarshipActiveSponsorshipDTO> getActiveSponsorships() {
        List<Object[]> results = entityManager.createNativeQuery(
            "SELECT a.id, a.donation_id, a.student_id, a.branch_id, 'Active & Paid' as status FROM erp_scholarship_allocations a"
        ).getResultList();
        
        return results.stream().map(row -> {
            com.erp.montfortuganda.scholarship.dto.ScholarshipActiveSponsorshipDTO dto = new com.erp.montfortuganda.scholarship.dto.ScholarshipActiveSponsorshipDTO();
            dto.setId(((Number) row[0]).intValue());
            dto.setSponsorId(String.valueOf(row[1]));
            dto.setSponsorName("Sponsor " + row[1]);
            dto.setStudentId(String.valueOf(row[2]));
            dto.setStudentName("Student " + row[2]);
            dto.setCampus("Branch " + row[3]);
            dto.setStatus((String) row[4]);
            return dto;
        }).toList();
    }

public List<ScholarshipBranchAllocation> getBranchHistory(Integer branchId) {
        return branchAllocationRepo.findByBranchIdOrderByCreatedAtDesc(branchId);
    }

    @Transactional
    public ScholarshipBranchAllocation allocateToBranch(ScholarshipAllocationRequestDTO request) {
        ScholarshipFundsSummaryDTO summary = getFundsSummary();
        if (summary.getAvailableBalanceUgx().compareTo(request.getAmountUgx()) < 0) {
            throw new IllegalStateException("Insufficient Treasury Funds! Available Balance: " + summary.getAvailableBalanceUgx());
        }

        ScholarshipBranchAllocation allocation = new ScholarshipBranchAllocation();
        allocation.setBranchId(request.getBranchId());
        allocation.setAllocatedAmountUgx(request.getAmountUgx());
        allocation.setTerm(request.getTerm());
        allocation.setAcademicYear(request.getAcademicYear());

        return branchAllocationRepo.save(allocation);
    }

    @Transactional
    public ScholarshipAllocation allocateToStudent(ScholarshipAllocationRequestDTO request) {
        ScholarshipFundsSummaryDTO summary = getFundsSummary();
        if (summary.getAvailableBalanceUgx().compareTo(request.getAmountUgx()) < 0) {
            throw new IllegalStateException("Insufficient Treasury Funds! Available Balance: " + summary.getAvailableBalanceUgx());
        }

        ScholarshipAllocation allocation = new ScholarshipAllocation();
        allocation.setBranchId(request.getBranchId());
        allocation.setStudentId(request.getStudentId());
        allocation.setDonationId(request.getDonationId());
        allocation.setAllocatedAmountUgx(request.getAmountUgx());
        allocation.setTerm(request.getTerm());
        allocation.setAcademicYear(request.getAcademicYear());

        return studentAllocationRepo.save(allocation);
    }
}

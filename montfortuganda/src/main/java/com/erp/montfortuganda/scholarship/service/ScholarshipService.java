package com.erp.montfortuganda.scholarship.service;

import com.erp.montfortuganda.scholarship.dto.*;
import java.util.List;

public interface ScholarshipService {
    FundsSummaryDTO getFundsSummary();
    List<DonorDTO> getAllDonors();
    List<PendingStudentDTO> getPendingStudents();
    List<BranchDemandDTO> getBranchDemands();
    List<ActiveSponsorshipDTO> getActiveSponsorships();

    void allocateToBranch(AllocationRequestDTO request);
    void allocateToStudent(AllocationRequestDTO request);
}
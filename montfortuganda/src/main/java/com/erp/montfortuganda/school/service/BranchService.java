package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.school.dto.BranchDTO;
import java.util.List;

public interface BranchService {
    List<BranchDTO> getAllBranches();
    BranchDTO createBranch(BranchDTO branchDTO);
    BranchDTO updateBranch(Integer id, BranchDTO branchDTO);
    void softDeleteBranch(Integer id);
}
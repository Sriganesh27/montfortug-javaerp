package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.school.dto.BranchDTO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface BranchService {
    List<BranchDTO> getAllBranches();

    // Notice these now take List<MultipartFile> documents
    BranchDTO createBranch(BranchDTO branchDTO, MultipartFile photo, List<MultipartFile> documents);
    BranchDTO updateBranch(Integer id, BranchDTO branchDTO, MultipartFile photo, List<MultipartFile> documents);

    void toggleBranchActive(Integer id);
}
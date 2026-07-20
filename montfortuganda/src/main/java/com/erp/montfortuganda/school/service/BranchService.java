package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.school.dto.BranchDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BranchService {

    List<BranchDTO> getAllBranches();

    BranchDTO getBranchById(
            Integer branchId
    );

    BranchDTO createBranch(
            BranchDTO branchDTO,
            MultipartFile logo,
            MultipartFile photo,
            List<MultipartFile> documents
    );

    BranchDTO updateBranch(
            Integer branchId,
            BranchDTO branchDTO,
            MultipartFile logo,
            MultipartFile photo,
            List<MultipartFile> documents
    );

    void toggleBranchActive(
            Integer branchId
    );
}
package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.school.Branch;
import com.erp.montfortuganda.school.BranchRepository;
import com.erp.montfortuganda.school.dto.BranchDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BranchServiceImpl implements BranchService {

    @Autowired
    private BranchRepository branchRepository;

    @Override
    public List<BranchDTO> getAllBranches() {
        return branchRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BranchDTO createBranch(BranchDTO dto) {
        Branch branch = mapToEntity(dto);
        branch.setIsActive(1);
        Branch savedBranch = branchRepository.save(branch);

        System.out.println("AUDIT: Branch created safely via Service Engine");
        return mapToDTO(savedBranch);
    }

    @Override
    public BranchDTO updateBranch(Integer id, BranchDTO dto) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        branch.setBranchName(dto.getBranchName());
        branch.setSchoolCode(dto.getSchoolCode());
        branch.setBranchType(dto.getBranchType());
        branch.setBranchLocation(dto.getBranchLocation());

        Branch updatedBranch = branchRepository.save(branch);
        return mapToDTO(updatedBranch);
    }

    @Override
    public void softDeleteBranch(Integer id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found"));
        branch.setIsActive(0);
        branchRepository.save(branch);
    }

    private BranchDTO mapToDTO(Branch branch) {
        BranchDTO dto = new BranchDTO();
        dto.setBranchId(branch.getBranchId());
        dto.setBranchName(branch.getBranchName());
        dto.setSchoolCode(branch.getSchoolCode());
        dto.setBranchType(branch.getBranchType());
        dto.setBranchLocation(branch.getBranchLocation());
        dto.setIsActive(branch.getIsActive());
        return dto;
    }

    private Branch mapToEntity(BranchDTO dto) {
        Branch branch = new Branch();
        branch.setBranchName(dto.getBranchName());
        branch.setSchoolCode(dto.getSchoolCode());
        branch.setBranchType(dto.getBranchType());
        branch.setBranchLocation(dto.getBranchLocation());
        return branch;
    }
}
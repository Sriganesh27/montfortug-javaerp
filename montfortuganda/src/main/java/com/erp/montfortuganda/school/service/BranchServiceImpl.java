package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.school.Branch;
import com.erp.montfortuganda.school.BranchRepository;
import com.erp.montfortuganda.school.dto.BranchDTO;
import com.erp.montfortuganda.auth.User;
import com.erp.montfortuganda.auth.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BranchServiceImpl implements BranchService {

    @Autowired private BranchRepository branchRepository;
    @Autowired private FileStorageService fileStorageService;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public List<BranchDTO> getAllBranches() {
        return branchRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public BranchDTO createBranch(BranchDTO dto, MultipartFile photo, List<MultipartFile> documents) {
        Branch branch = mapToEntity(dto);
        branch.setIsActive(1);

        // Handle Single Photo Upload
        String photoUrl = fileStorageService.saveFile(dto.getSchoolCode(), dto.getBranchName(), dto.getBranchLocation(), "school_documents", photo);
        if (photoUrl != null) branch.setSchoolPhotoUrl(photoUrl);

        // MAGIC: Handle Multiple Document Uploads and merge paths with commas
        if (documents != null && !documents.isEmpty()) {
            java.util.List<String> docUrls = new java.util.ArrayList<>();
            for (MultipartFile doc : documents) {
                String docUrl = fileStorageService.saveFile(dto.getSchoolCode(), dto.getBranchName(), dto.getBranchLocation(), "school_documents", doc);
                if (docUrl != null) docUrls.add(docUrl);
            }
            if (!docUrls.isEmpty()) {
                branch.setGovDocumentUrl(String.join(",", docUrls));
            }
        }

        Branch savedBranch = branchRepository.save(branch);

        // Auto-Generate User Account
        autoGenerateBranchAdmin(savedBranch);

        return mapToDTO(savedBranch);
    }

    @Override
    public BranchDTO updateBranch(Integer id, BranchDTO dto, MultipartFile photo, List<MultipartFile> documents) {
        Branch branch = branchRepository.findById(id).orElseThrow(() -> new RuntimeException("Branch not found"));

        branch.setBranchName(dto.getBranchName());
        branch.setSchoolCode(dto.getSchoolCode());
        branch.setBranchType(dto.getBranchType());
        branch.setBranchLocation(dto.getBranchLocation());
        branch.setContactDetails(dto.getContactDetails());
        branch.setInchargeDetails(dto.getInchargeDetails());

        // Handle Single Photo Upload
        String photoUrl = fileStorageService.saveFile(dto.getSchoolCode(), dto.getBranchName(), dto.getBranchLocation(), "school_documents", photo);
        if (photoUrl != null) branch.setSchoolPhotoUrl(photoUrl);

        // MAGIC: Handle Multiple Document Uploads and merge paths with commas
        if (documents != null && !documents.isEmpty()) {
            java.util.List<String> docUrls = new java.util.ArrayList<>();
            for (MultipartFile doc : documents) {
                String docUrl = fileStorageService.saveFile(dto.getSchoolCode(), dto.getBranchName(), dto.getBranchLocation(), "school_documents", doc);
                if (docUrl != null) docUrls.add(docUrl);
            }
            if (!docUrls.isEmpty()) {
                branch.setGovDocumentUrl(String.join(",", docUrls));
            }
        }

        return mapToDTO(branchRepository.save(branch));
    }

    @Override
    public void toggleBranchActive(Integer id) {
        Branch branch = branchRepository.findById(id).orElseThrow(() -> new RuntimeException("Branch not found"));
        branch.setIsActive(branch.getIsActive() == 1 ? 0 : 1);
        branchRepository.save(branch);
    }

    private void autoGenerateBranchAdmin(Branch branch) {
        // 1. Username: schoolcode@montfort.ug
        String username = branch.getSchoolCode().toLowerCase() + "@montfort.ug";

        // Prevent duplicate user creation
        if (userRepository.findByUsername(username).isPresent()) return;

        // 2. Extract Year from foundationDate (Assuming format "YYYY-MM-DD")
        String year = "";
        if (branch.getFoundationDate() != null && branch.getFoundationDate().length() >= 4) {
            year = branch.getFoundationDate().substring(0, 4);
        }

        // 3. Password: 1st 6 letters of name @ code + year
        String cleanName = branch.getBranchName().replaceAll("\\s+", ""); // Remove spaces
        String namePrefix = cleanName.length() >= 6 ? cleanName.substring(0, 6) : cleanName;
        String password = namePrefix.toUpperCase() + "@" + branch.getSchoolCode() + year;

        // 4. Save User to DB
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("School Admin");
        user.setAssignedBranch(branch);
        user.setIsActive(1);
        userRepository.save(user);

        System.out.println("AUDIT: Auto-Created Branch Admin -> User: " + username + " | Pass: " + password);
    }

    private BranchDTO mapToDTO(Branch branch) {
        BranchDTO dto = new BranchDTO();
        dto.setBranchId(branch.getBranchId());
        dto.setBranchName(branch.getBranchName());
        dto.setSchoolCode(branch.getSchoolCode());
        dto.setBranchType(branch.getBranchType());
        dto.setBranchLocation(branch.getBranchLocation());
        dto.setContactDetails(branch.getContactDetails());
        dto.setInchargeDetails(branch.getInchargeDetails());
        dto.setSchoolPhotoUrl(branch.getSchoolPhotoUrl());
        dto.setGovDocumentUrl(branch.getGovDocumentUrl());
        dto.setFoundationDate(branch.getFoundationDate());
        dto.setIsActive(branch.getIsActive());
        return dto;
    }

    private Branch mapToEntity(BranchDTO dto) {
        Branch branch = new Branch();
        branch.setBranchName(dto.getBranchName());
        branch.setSchoolCode(dto.getSchoolCode());
        branch.setBranchType(dto.getBranchType());
        branch.setBranchLocation(dto.getBranchLocation());
        branch.setContactDetails(dto.getContactDetails());
        branch.setInchargeDetails(dto.getInchargeDetails());
        branch.setFoundationDate(dto.getFoundationDate());
        return branch;
    }
}
package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.school.Branch;
import com.erp.montfortuganda.school.repository.BranchRepository;
import com.erp.montfortuganda.school.Level;
import com.erp.montfortuganda.school.LevelRepository;
import com.erp.montfortuganda.school.dto.BranchDTO;
import com.erp.montfortuganda.school.dto.LevelDTO;
import com.erp.montfortuganda.auth.User;
import com.erp.montfortuganda.auth.repository.UserRepository;
import com.erp.montfortuganda.auth.ErpRole;
import com.erp.montfortuganda.auth.ErpUserRole;
import com.erp.montfortuganda.auth.repository.ErpRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Service
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final LevelRepository levelRepository;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ErpRoleRepository erpRoleRepository;

    @Autowired
    public BranchServiceImpl(BranchRepository branchRepository, LevelRepository levelRepository, FileStorageService fileStorageService, UserRepository userRepository, PasswordEncoder passwordEncoder, ErpRoleRepository erpRoleRepository) {
        this.branchRepository = branchRepository;
        this.levelRepository = levelRepository;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.erpRoleRepository = erpRoleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchDTO> getAllBranches() {
        return branchRepository.findAll().stream().map(this::mapToDTO).toList();
    }

    @Override
    @Transactional
    public BranchDTO createBranch(BranchDTO dto, MultipartFile photo, List<MultipartFile> documents) {
        Branch branch = mapToEntity(dto);
        branch.setIsActive(1);

        handleFileUploads(branch, photo, documents);

        assignLevelsToBranch(branch, dto.getLevelIds());
        Branch savedBranch = branchRepository.save(branch);
        autoGenerateBranchAdmin(savedBranch);
        return mapToDTO(savedBranch);
    }

    @Override
    @Transactional
    public BranchDTO updateBranch(Integer id, BranchDTO dto, MultipartFile photo, List<MultipartFile> documents) {
        // FIXED: Using method parameter id
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found"));
        branch.setBranchName(dto.getBranchName());
        branch.setSchoolCode(dto.getSchoolCode());
        branch.setBranchLocation(dto.getBranchLocation());
        branch.setContactDetails(dto.getContactDetails());
        branch.setInchargeDetails(dto.getInchargeDetails());

        handleFileUploads(branch, photo, documents);

        List<Integer> newLevelIds = dto.getLevelIds();
        if (newLevelIds == null) newLevelIds = new java.util.ArrayList<>();

        List<Integer> finalNewLevelIds = newLevelIds;
        branch.getBranchLevels().removeIf(bl -> !finalNewLevelIds.contains(bl.getLevel().getLevelId()));

        List<Integer> existingLevelIds = branch.getBranchLevels().stream()
                .map(bl -> bl.getLevel().getLevelId())
                .toList();

        List<Integer> levelsToAdd = newLevelIds.stream()
                .filter(levelId -> !existingLevelIds.contains(levelId))
                .distinct()
                .toList();

        assignLevelsToBranch(branch, levelsToAdd);

        return mapToDTO(branchRepository.save(branch));
    }

    @Override
    @Transactional
    public void toggleBranchActive(Integer id) {
        // FIXED: Using method parameter id
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found"));
        branch.setIsActive(branch.getIsActive() == 1 ? 0 : 1);
        branchRepository.save(branch);
    }

    private void handleFileUploads(Branch branch, MultipartFile photo, List<MultipartFile> documents) {
        if (photo != null && !photo.isEmpty()) {
            String photoUrl = fileStorageService.saveFile(branch.getSchoolCode(), branch.getBranchName(), branch.getBranchLocation(), "photo", photo);
            branch.setSchoolPhotoUrl(photoUrl);
        }

        if (documents != null && !documents.isEmpty()) {
            List<String> docUrls = documents.stream()
                    .filter(doc -> doc != null && !doc.isEmpty())
                    .map(doc -> fileStorageService.saveFile(branch.getSchoolCode(), branch.getBranchName(), branch.getBranchLocation(), "documents", doc))
                    .toList();
            if (!docUrls.isEmpty()) {
                branch.setGovDocumentUrl(String.join(",", docUrls));
            }
        }
    }

    private void assignLevelsToBranch(Branch branch, List<Integer> levelIds) {
        if (levelIds == null || levelIds.isEmpty()) return;
        levelIds.stream().distinct().forEach(levelId -> {
            // Level is likely still using Integer ID
            Level level = levelRepository.findById(levelId).orElseThrow(() -> new IllegalArgumentException("Invalid Level ID"));
            branch.addLevel(level, "SUPER_ADMIN");
        });
    }

    private void autoGenerateBranchAdmin(Branch branch) {
        String username = branch.getSchoolCode().toLowerCase() + "@montfort.ug";
        if (userRepository.findByUsername(username).isPresent()) return;
        String year = (branch.getFoundationDate() != null && branch.getFoundationDate().length() >= 4) ? branch.getFoundationDate().substring(0, 4) : "";
        String cleanName = branch.getBranchName().replaceAll("\\s+", "");
        String password = (cleanName.length() >= 6 ? cleanName.substring(0, 6) : cleanName).toUpperCase() + "@" + branch.getSchoolCode() + year;

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("BRANCH_ADMIN");
        user.setAssignedBranch(branch);
        user.setIsActive(1);

        erpRoleRepository.findByRoleCode("BRANCH_ADMIN").ifPresent(role -> {
            ErpUserRole userRole = new ErpUserRole();
            userRole.setRole(role);
            userRole.setActive(true);
            user.addRole(userRole);
        });

        userRepository.save(user);
    }

    private BranchDTO mapToDTO(Branch branch) {
        BranchDTO dto = new BranchDTO();
        // FIXED: Cast Long ID back to Integer for the DTO
        dto.setBranchId(branch.getBranchId() != null ? branch.getBranchId().intValue() : null);
        dto.setBranchName(branch.getBranchName());
        dto.setSchoolCode(branch.getSchoolCode());

        if (branch.getBranchLevels() != null) {
            List<LevelDTO> levelDTOs = branch.getBranchLevels().stream()
                    .map(bl -> new LevelDTO(bl.getLevel().getLevelId(), bl.getLevel().getLevelName()))
                    .toList();
            dto.setLevels(levelDTOs);
            dto.setLevelIds(levelDTOs.stream().map(LevelDTO::getLevelId).toList());
        }

        dto.setBranchLocation(branch.getBranchLocation());
        dto.setContactDetails(branch.getContactDetails());
        dto.setInchargeDetails(branch.getInchargeDetails());
        dto.setIsActive(branch.getIsActive());
        return dto;
    }

    private Branch mapToEntity(BranchDTO dto) {
        Branch branch = new Branch();
        branch.setBranchName(dto.getBranchName());
        branch.setSchoolCode(dto.getSchoolCode());
        branch.setBranchLocation(dto.getBranchLocation());
        branch.setContactDetails(dto.getContactDetails());
        branch.setInchargeDetails(dto.getInchargeDetails());
        return branch;
    }
}

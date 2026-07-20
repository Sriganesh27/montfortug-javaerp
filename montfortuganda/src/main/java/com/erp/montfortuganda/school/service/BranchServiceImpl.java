package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.auth.entity.CredentialDeliveryStatus;
import com.erp.montfortuganda.auth.entity.User;
import com.erp.montfortuganda.auth.repository.UserRepository;
import com.erp.montfortuganda.notification.service.EmailService;
import com.erp.montfortuganda.school.dto.BranchDTO;
import com.erp.montfortuganda.school.dto.LevelDTO;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.entity.Level;
import com.erp.montfortuganda.school.repository.BranchRepository;
import com.erp.montfortuganda.school.repository.LevelRepository;
import com.erp.montfortuganda.school.service.model.BranchAdminCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
public class BranchServiceImpl implements BranchService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(BranchServiceImpl.class);

    private final BranchRepository branchRepository;
    private final LevelRepository levelRepository;
    private final FileStorageService fileStorageService;
    private final BranchAdminAccountService branchAdminAccountService;
    private final EmailService emailService;
    private final UserRepository userRepository;

    public BranchServiceImpl(
            BranchRepository branchRepository,
            LevelRepository levelRepository,
            FileStorageService fileStorageService,
            BranchAdminAccountService branchAdminAccountService,
            EmailService emailService,
            UserRepository userRepository
    ) {
        this.branchRepository = branchRepository;
        this.levelRepository = levelRepository;
        this.fileStorageService = fileStorageService;
        this.branchAdminAccountService = branchAdminAccountService;
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchDTO> getAllBranches() {
        return branchRepository
                .findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BranchDTO getBranchById(
            Integer branchId
    ) {
        return mapToDTO(
                findBranch(branchId)
        );
    }

    @Override
    @Transactional
    public BranchDTO createBranch(
            BranchDTO branchDTO,
            MultipartFile logo,
            MultipartFile photo,
            List<MultipartFile> documents
    ) {
        Branch branch =
                mapToEntity(branchDTO);

        branch.setIsActive(1);

        assignLevelsToBranch(
                branch,
                branchDTO.getLevelIds()
        );

        /*
         * The branch must be saved first because private storage and the
         * Branch Admin account both require the generated branch ID.
         */
        Branch savedBranch =
                branchRepository.saveAndFlush(branch);

        handleFileUploads(
                savedBranch,
                logo,
                photo,
                documents
        );

        savedBranch =
                branchRepository.saveAndFlush(
                        savedBranch
                );

        /*
         * Creates:
         * username: {schoolCode}@montfort.ug
         * secure random temporary password
         * 72-hour expiry
         * must_change_password = true
         * credential_delivery_status = PENDING
         */
        BranchAdminCredentials credentials =
                branchAdminAccountService
                        .createBranchAdmin(
                                savedBranch
                        );

        scheduleBranchAdminCredentialEmail(
                savedBranch,
                credentials
        );

        return mapToDTO(savedBranch);
    }

    @Override
    @Transactional
    public BranchDTO updateBranch(
            Integer branchId,
            BranchDTO branchDTO,
            MultipartFile logo,
            MultipartFile photo,
            List<MultipartFile> documents
    ) {
        Branch branch =
                findBranch(branchId);

        updateEntityFromDTO(
                branch,
                branchDTO
        );

        synchronizeLevels(
                branch,
                branchDTO.getLevelIds()
        );

        handleFileUploads(
                branch,
                logo,
                photo,
                documents
        );

        return mapToDTO(
                branchRepository.save(branch)
        );
    }

    @Override
    @Transactional
    public void toggleBranchActive(
            Integer branchId
    ) {
        Branch branch =
                findBranch(branchId);

        branch.setIsActive(
                Integer.valueOf(1).equals(
                        branch.getIsActive()
                )
                        ? 0
                        : 1
        );

        branchRepository.save(branch);
    }

    private void scheduleBranchAdminCredentialEmail(
            Branch branch,
            BranchAdminCredentials credentials
    ) {
        if (
                !TransactionSynchronizationManager
                        .isSynchronizationActive()
        ) {
            deliverBranchAdminCredentials(
                    branch,
                    credentials
            );
            return;
        }

        TransactionSynchronizationManager
                .registerSynchronization(
                        new TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                deliverBranchAdminCredentials(
                                        branch,
                                        credentials
                                );
                            }
                        }
                );
    }

    private void deliverBranchAdminCredentials(
            Branch branch,
            BranchAdminCredentials credentials
    ) {
        try {
            emailService.sendBranchAdminWelcomeEmail(
                    branch,
                    credentials
            );

            updateCredentialDeliveryStatus(
                    credentials.getUserId(),
                    CredentialDeliveryStatus.SENT,
                    true
            );

        } catch (RuntimeException exception) {
            updateCredentialDeliveryStatus(
                    credentials.getUserId(),
                    CredentialDeliveryStatus.FAILED,
                    false
            );

            LOGGER.error(
                    "Branch Admin credentials email failed for branch ID: {}",
                    branch.getBranchId(),
                    exception
            );
        }
    }

    private void updateCredentialDeliveryStatus(
            Integer userId,
            CredentialDeliveryStatus status,
            boolean sentSuccessfully
    ) {
        User user =
                userRepository
                        .findById(userId)
                        .orElse(null);

        if (user == null) {
            LOGGER.error(
                    "Credential delivery status could not be updated because "
                            + "ERP user ID {} was not found.",
                    userId
            );
            return;
        }

        Integer currentAttempts =
                user.getCredentialDeliveryAttempts();

        user.setCredentialDeliveryAttempts(
                currentAttempts == null
                        ? 1
                        : currentAttempts + 1
        );

        user.setCredentialDeliveryStatus(status);

        if (sentSuccessfully) {
            user.setCredentialsSentAt(
                    LocalDateTime.now(
                            ZoneOffset.UTC
                    )
            );
        }

        userRepository.save(user);
    }

    private Branch findBranch(
            Integer branchId
    ) {
        if (branchId == null) {
            throw new IllegalArgumentException(
                    "Branch ID is required."
            );
        }

        return branchRepository
                .findById(branchId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Branch not found with ID: "
                                        + branchId
                        )
                );
    }

    private void handleFileUploads(
            Branch branch,
            MultipartFile logo,
            MultipartFile photo,
            List<MultipartFile> documents
    ) {
        if (logo != null && !logo.isEmpty()) {
            branch.setBranchLogoUrl(
                    fileStorageService.saveBranchLogo(
                            branch.getBranchId(),
                            branch.getSchoolCode(),
                            branch.getBranchName(),
                            branch.getBranchLocation(),
                            logo
                    )
            );
        }

        if (photo != null && !photo.isEmpty()) {
            branch.setSchoolPhotoUrl(
                    fileStorageService.saveBranchPhoto(
                            branch.getBranchId(),
                            branch.getSchoolCode(),
                            branch.getBranchName(),
                            branch.getBranchLocation(),
                            photo
                    )
            );
        }

        List<String> documentPaths =
                fileStorageService.saveBranchDocuments(
                        branch.getBranchId(),
                        branch.getSchoolCode(),
                        branch.getBranchName(),
                        branch.getBranchLocation(),
                        documents
                );

        if (!documentPaths.isEmpty()) {
            branch.setGovDocumentUrl(
                    String.join(
                            ",",
                            documentPaths
                    )
            );
        }
    }

    private void synchronizeLevels(
            Branch branch,
            List<Integer> requestedLevelIds
    ) {
        List<Integer> normalizedLevelIds =
                requestedLevelIds == null
                        ? new ArrayList<>()
                        : requestedLevelIds
                        .stream()
                        .distinct()
                        .toList();

        branch.getBranchLevels()
                .removeIf(branchLevel ->
                        !normalizedLevelIds.contains(
                                branchLevel
                                        .getLevel()
                                        .getLevelId()
                        )
                );

        List<Integer> existingLevelIds =
                branch.getBranchLevels()
                        .stream()
                        .map(branchLevel ->
                                branchLevel
                                        .getLevel()
                                        .getLevelId()
                        )
                        .toList();

        List<Integer> levelsToAdd =
                normalizedLevelIds
                        .stream()
                        .filter(levelId ->
                                !existingLevelIds.contains(
                                        levelId
                                )
                        )
                        .toList();

        assignLevelsToBranch(
                branch,
                levelsToAdd
        );
    }

    private void assignLevelsToBranch(
            Branch branch,
            List<Integer> levelIds
    ) {
        if (levelIds == null || levelIds.isEmpty()) {
            return;
        }

        levelIds.stream()
                .distinct()
                .forEach(levelId -> {
                    Level level =
                            levelRepository
                                    .findById(levelId)
                                    .orElseThrow(
                                            () -> new IllegalArgumentException(
                                                    "Invalid Level ID: "
                                                            + levelId
                                            )
                                    );

                    branch.addLevel(
                            level,
                            "SUPER_ADMIN"
                    );
                });
    }

    private BranchDTO mapToDTO(
            Branch branch
    ) {
        BranchDTO dto =
                new BranchDTO();

        dto.setBranchId(
                branch.getBranchId()
        );
        dto.setBranchName(
                branch.getBranchName()
        );
        dto.setSchoolCode(
                branch.getSchoolCode()
        );
        dto.setBranchLocation(
                branch.getBranchLocation()
        );
        dto.setAddressLine1(
                branch.getAddressLine1()
        );
        dto.setAddressLine2(
                branch.getAddressLine2()
        );
        dto.setPoBox(
                branch.getPoBox()
        );
        dto.setLocality(
                branch.getLocality()
        );
        dto.setCity(
                branch.getCity()
        );
        dto.setDistrict(
                branch.getDistrict()
        );
        dto.setRegion(
                branch.getRegion()
        );
        dto.setCountry(
                branch.getCountry()
        );
        dto.setPostalCode(
                branch.getPostalCode()
        );
        dto.setPrimaryPhone(
                branch.getPrimaryPhone()
        );
        dto.setSecondaryPhone(
                branch.getSecondaryPhone()
        );
        dto.setWhatsappPhone(
                branch.getWhatsappPhone()
        );
        dto.setBranchEmail(
                branch.getBranchEmail()
        );
        dto.setEmailFromName(
                branch.getEmailFromName()
        );
        dto.setEmailReplyTo(
                branch.getEmailReplyTo()
        );
        dto.setEmailEnabled(
                branch.getEmailEnabled()
        );
        dto.setFoundationDate(
                branch.getFoundationDate()
        );
        dto.setContactDetails(
                branch.getContactDetails()
        );
        dto.setInchargeDetails(
                branch.getInchargeDetails()
        );
        dto.setBranchLogoUrl(
                branch.getBranchLogoUrl()
        );
        dto.setSchoolPhotoUrl(
                branch.getSchoolPhotoUrl()
        );
        dto.setGovDocumentUrl(
                branch.getGovDocumentUrl()
        );
        dto.setIsActive(
                branch.getIsActive()
        );

        if (branch.getBranchLevels() != null) {
            List<LevelDTO> levelDTOs =
                    branch.getBranchLevels()
                            .stream()
                            .map(branchLevel ->
                                    new LevelDTO(
                                            branchLevel
                                                    .getLevel()
                                                    .getLevelId(),
                                            branchLevel
                                                    .getLevel()
                                                    .getLevelName()
                                    )
                            )
                            .toList();

            dto.setLevels(levelDTOs);
            dto.setLevelIds(
                    levelDTOs.stream()
                            .map(LevelDTO::getLevelId)
                            .toList()
            );
        }

        return dto;
    }

    private Branch mapToEntity(
            BranchDTO dto
    ) {
        Branch branch =
                new Branch();

        updateEntityFromDTO(
                branch,
                dto
        );

        return branch;
    }

    private void updateEntityFromDTO(
            Branch branch,
            BranchDTO dto
    ) {
        branch.setBranchName(
                dto.getBranchName()
        );
        branch.setSchoolCode(
                dto.getSchoolCode()
        );
        branch.setBranchLocation(
                dto.getBranchLocation()
        );
        branch.setAddressLine1(
                dto.getAddressLine1()
        );
        branch.setAddressLine2(
                dto.getAddressLine2()
        );
        branch.setPoBox(
                dto.getPoBox()
        );
        branch.setLocality(
                dto.getLocality()
        );
        branch.setCity(
                dto.getCity()
        );
        branch.setDistrict(
                dto.getDistrict()
        );
        branch.setRegion(
                dto.getRegion()
        );
        branch.setCountry(
                dto.getCountry()
        );
        branch.setPostalCode(
                dto.getPostalCode()
        );
        branch.setPrimaryPhone(
                dto.getPrimaryPhone()
        );
        branch.setSecondaryPhone(
                dto.getSecondaryPhone()
        );
        branch.setWhatsappPhone(
                dto.getWhatsappPhone()
        );
        branch.setBranchEmail(
                dto.getBranchEmail()
        );
        branch.setEmailFromName(
                dto.getEmailFromName()
        );
        branch.setEmailReplyTo(
                dto.getEmailReplyTo()
        );
        branch.setEmailEnabled(
                dto.getEmailEnabled()
        );
        branch.setFoundationDate(
                dto.getFoundationDate()
        );
        branch.setContactDetails(
                dto.getContactDetails()
        );
        branch.setInchargeDetails(
                dto.getInchargeDetails()
        );
    }
}

package com.erp.montfortuganda.branchadmin.service;

import com.erp.montfortuganda.auth.service.BranchAccessService;
import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.branchadmin.dto.BranchProfileResponseDTO;
import com.erp.montfortuganda.branchadmin.dto.BranchProfileUpdateDTO;
import com.erp.montfortuganda.school.dto.LevelDTO;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.repository.BranchRepository;
import com.erp.montfortuganda.school.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class BranchProfileServiceImpl implements BranchProfileService {

    private final BranchRepository branchRepository;
    private final FileStorageService fileStorageService;
    private final BranchAccessService branchAccessService;

    @Override
    @Transactional(readOnly = true)
    public BranchProfileResponseDTO getProfile(CurrentUserContext context) {
        Integer branchId = branchAccessService.getValidatedBranchId(context);

        Branch branch = branchRepository.findByIdWithLevels(branchId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Branch not found with ID: " + branchId
                ));

        return mapToResponse(branch, context);
    }

    @Override
    @Transactional
    public BranchProfileResponseDTO updateProfile(
            CurrentUserContext context,
            BranchProfileUpdateDTO updateDTO,
            MultipartFile logo,
            MultipartFile photo,
            List<MultipartFile> documents,
            MultipartFile qrCode
    ) {
        if (updateDTO == null) {
            throw new IllegalArgumentException("Branch profile data is required.");
        }

        Integer branchId = branchAccessService.getValidatedBranchId(context);

        Branch branch = branchRepository.findByIdWithLevels(branchId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Branch not found with ID: " + branchId
                ));

        applyEditableFields(branch, updateDTO);
        // Keep immutable/system-controlled values untouched:
        // branchId, branchName, schoolCode and isActive are never accepted
        // from Branch Admin input.
        branch = branchRepository.saveAndFlush(branch);

        String previousLogoPath = branch.getBranchLogoUrl();
        String previousPhotoPath = branch.getSchoolPhotoUrl();
        String previousQrPath = branch.getQrCodeUrl();
        String previousDocuments = branch.getGovDocumentUrl();

        List<String> newlyStoredPaths = new ArrayList<>();

        try {
            if (hasFile(logo)) {
                validateImageFile(
                        logo,
                        "Logo",
                        2 * 1024 * 1024
                );

                String storedPath = fileStorageService.saveBranchLogo(
                        branch.getBranchId(),
                        branch.getSchoolCode(),
                        branch.getBranchName(),
                        branch.getBranchLocation(),
                        logo
                );

                if (storedPath != null) {
                    newlyStoredPaths.add(storedPath);
                    branch.setBranchLogoUrl(storedPath);
                }
            }

            if (hasFile(photo)) {
                validateImageFile(
                        photo,
                        "School photo",
                        5 * 1024 * 1024
                );

                String storedPath = fileStorageService.saveBranchPhoto(
                        branch.getBranchId(),
                        branch.getSchoolCode(),
                        branch.getBranchName(),
                        branch.getBranchLocation(),
                        photo
                );

                if (storedPath != null) {
                    newlyStoredPaths.add(storedPath);
                    branch.setSchoolPhotoUrl(storedPath);
                }
            }

            if (hasFile(qrCode)) {
                validateImageFile(
                        qrCode,
                        "QR code",
                        2 * 1024 * 1024
                );

                String storedPath = fileStorageService.saveBranchQrCode(
                        branch.getBranchId(),
                        branch.getSchoolCode(),
                        branch.getBranchName(),
                        branch.getBranchLocation(),
                        qrCode
                );

                if (storedPath != null) {
                    newlyStoredPaths.add(storedPath);
                    branch.setQrCodeUrl(storedPath);
                }
            }

            validateGovernmentDocuments(documents);

            List<String> documentPaths = fileStorageService.saveBranchDocuments(
                    branch.getBranchId(),
                    branch.getSchoolCode(),
                    branch.getBranchName(),
                    branch.getBranchLocation(),
                    documents
            );

            newlyStoredPaths.addAll(documentPaths);

            if (!documentPaths.isEmpty()) {
                List<String> allDocumentPaths = new ArrayList<>();

                if (previousDocuments != null && !previousDocuments.isBlank()) {
                    for (String previousPath : previousDocuments.split(",")) {
                        String normalizedPath = previousPath.trim();
                        if (!normalizedPath.isBlank()
                                && !allDocumentPaths.contains(normalizedPath)) {
                            allDocumentPaths.add(normalizedPath);
                        }
                    }
                }

                for (String documentPath : documentPaths) {
                    String normalizedPath = documentPath == null
                            ? ""
                            : documentPath.trim();

                    if (!normalizedPath.isBlank()
                            && !allDocumentPaths.contains(normalizedPath)) {
                        allDocumentPaths.add(normalizedPath);
                    }
                }

                branch.setGovDocumentUrl(String.join(",", allDocumentPaths));
            }

            Branch savedBranch = branchRepository.saveAndFlush(branch);

            // Government documents are append-only in this update operation.
            // Existing document files remain referenced and are never deleted
            // merely because new documents were uploaded.
            registerOldFileCleanupAfterCommit(
                    previousLogoPath,
                    savedBranch.getBranchLogoUrl(),
                    previousPhotoPath,
                    savedBranch.getSchoolPhotoUrl(),
                    previousQrPath,
                    savedBranch.getQrCodeUrl()
            );

            return mapToResponse(savedBranch, context);

        } catch (RuntimeException exception) {
            cleanupStoredFiles(newlyStoredPaths);

            // The surrounding transaction will roll back the database
            // changes. Restore the in-memory file references as well.
            branch.setBranchLogoUrl(previousLogoPath);
            branch.setSchoolPhotoUrl(previousPhotoPath);
            branch.setQrCodeUrl(previousQrPath);
            branch.setGovDocumentUrl(previousDocuments);

            throw exception;
        }
    }

    private void applyEditableFields(
            Branch branch,
            BranchProfileUpdateDTO dto
    ) {
        branch.setFoundationDate(dto.getFoundationDate());
        branch.setBranchLocation(dto.getBranchLocation());
        branch.setAddressLine1(dto.getAddressLine1());
        branch.setAddressLine2(dto.getAddressLine2());
        branch.setPoBox(dto.getPoBox());
        branch.setLocality(dto.getLocality());
        branch.setCity(dto.getCity());
        branch.setDistrict(dto.getDistrict());
        branch.setRegion(dto.getRegion());
        branch.setCountry(dto.getCountry());
        branch.setPostalCode(dto.getPostalCode());

        branch.setPrimaryPhone(dto.getPrimaryPhone());
        branch.setSecondaryPhone(dto.getSecondaryPhone());
        branch.setWhatsappPhone(dto.getWhatsappPhone());
        branch.setBranchEmail(dto.getBranchEmail());
        branch.setEmailFromName(dto.getEmailFromName());
        branch.setEmailReplyTo(dto.getEmailReplyTo());

        branch.setInchargeDetails(dto.getInchargeDetails());

        branch.setBankName(dto.getBankName());
        branch.setBankAccountName(dto.getBankAccountName());
        branch.setBankAccountNumber(dto.getBankAccountNumber());
        branch.setBankBranch(dto.getBankBranch());
        branch.setAirtelPayNumber(dto.getAirtelPayNumber());
        branch.setAirtelPayName(dto.getAirtelPayName());
    }

    private BranchProfileResponseDTO mapToResponse(
            Branch branch,
            CurrentUserContext context
    ) {
        BranchProfileResponseDTO dto = new BranchProfileResponseDTO();

        dto.setUsername(
                context == null ? null : context.getUsername()
        );
        dto.setCanEdit(hasEditPermission());

        dto.setBranchName(branch.getBranchName());
        dto.setSchoolCode(branch.getSchoolCode());
        dto.setIsActive(branch.getIsActive());
        dto.setFoundationDate(branch.getFoundationDate());

        dto.setBranchLocation(branch.getBranchLocation());
        dto.setAddressLine1(branch.getAddressLine1());
        dto.setAddressLine2(branch.getAddressLine2());
        dto.setPoBox(branch.getPoBox());
        dto.setLocality(branch.getLocality());
        dto.setCity(branch.getCity());
        dto.setDistrict(branch.getDistrict());
        dto.setRegion(branch.getRegion());
        dto.setCountry(branch.getCountry());
        dto.setPostalCode(branch.getPostalCode());

        dto.setPrimaryPhone(branch.getPrimaryPhone());
        dto.setSecondaryPhone(branch.getSecondaryPhone());
        dto.setWhatsappPhone(branch.getWhatsappPhone());
        dto.setBranchEmail(branch.getBranchEmail());
        dto.setEmailFromName(branch.getEmailFromName());
        dto.setEmailReplyTo(branch.getEmailReplyTo());

        dto.setInchargeDetails(branch.getInchargeDetails());

        dto.setBankName(branch.getBankName());
        dto.setBankAccountName(branch.getBankAccountName());
        dto.setBankAccountNumber(branch.getBankAccountNumber());
        dto.setBankBranch(branch.getBankBranch());
        dto.setAirtelPayNumber(branch.getAirtelPayNumber());
        dto.setAirtelPayName(branch.getAirtelPayName());
        dto.setQrCodeUrl(branch.getQrCodeUrl());

        dto.setBranchLogoUrl(branch.getBranchLogoUrl());
        dto.setSchoolPhotoUrl(branch.getSchoolPhotoUrl());
        dto.setGovDocumentUrl(branch.getGovDocumentUrl());

        List<LevelDTO> levels = new ArrayList<>();
        if (branch.getBranchLevels() != null) {
            for (var branchLevel : branch.getBranchLevels()) {
                if (branchLevel == null || branchLevel.getLevel() == null) {
                    continue;
                }
                levels.add(new LevelDTO(
                        branchLevel.getLevel().getLevelId(),
                        branchLevel.getLevel().getLevelName()
                ));
            }
        }

        dto.setLevels(levels);

        return dto;
    }

    private boolean hasEditPermission() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        "BRANCH_PROFILE_EDIT".equals(
                                authority.getAuthority()
                        )
                );
    }

    private void registerOldFileCleanupAfterCommit(
            String previousLogoPath,
            String currentLogoPath,
            String previousPhotoPath,
            String currentPhotoPath,
            String previousQrPath,
            String currentQrPath
    ) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        deleteReplacedFile(
                                previousLogoPath,
                                currentLogoPath
                        );
                        deleteReplacedFile(
                                previousPhotoPath,
                                currentPhotoPath
                        );
                        deleteReplacedFile(
                                previousQrPath,
                                currentQrPath
                        );
                    }
                }
        );
    }

    private void deleteReplacedFile(
            String previousPath,
            String currentPath
    ) {
        if (previousPath == null
                || previousPath.isBlank()
                || previousPath.equals(currentPath)) {
            return;
        }

        try {
            fileStorageService.deletePrivateFile(previousPath);
        } catch (RuntimeException cleanupException) {
            // The database transaction has already committed.
            // Do not turn successful profile updates into failed responses.
        }
    }

    private void cleanupStoredFiles(
            List<String> storedPaths
    ) {
        for (String storedPath : storedPaths) {
            if (storedPath == null || storedPath.isBlank()) {
                continue;
            }

            try {
                fileStorageService.deletePrivateFile(storedPath);
            } catch (RuntimeException cleanupException) {
                // Preserve the original update failure.
            }
        }
    }

    private boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private void validateImageFile(
            MultipartFile file,
            String label,
            long maximumSize
    ) {
        if (!hasFile(file)) {
            return;
        }

        if (file.getSize() > maximumSize) {
            throw new IllegalArgumentException(
                    label + " is too large. Maximum allowed size is "
                            + formatMegabytes(maximumSize)
                            + "."
            );
        }

        String contentType = normalizeContentType(file.getContentType());

        if (!List.of(
                "image/jpeg",
                "image/png",
                "image/webp"
        ).contains(contentType)) {
            throw new IllegalArgumentException(
                    label + " must be a JPEG, PNG, or WebP image."
            );
        }

        validateImageContent(file, label);
    }

    private void validateGovernmentDocuments(
            List<MultipartFile> documents
    ) {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        final long maximumSize = 10 * 1024 * 1024;

        for (MultipartFile document : documents) {
            if (!hasFile(document)) {
                continue;
            }

            if (document.getSize() > maximumSize) {
                throw new IllegalArgumentException(
                        "Government document is too large. "
                                + "Maximum allowed size is "
                                + formatMegabytes(maximumSize)
                                + "."
                );
            }

            String contentType =
                    normalizeContentType(document.getContentType());

            if (!List.of(
                    "application/pdf",
                    "image/jpeg",
                    "image/png"
            ).contains(contentType)) {
                throw new IllegalArgumentException(
                        "Government documents must be PDF, JPEG, or PNG files."
                );
            }

            if (contentType.startsWith("image/")) {
                validateImageContent(
                        document,
                        "Government document"
                );
            }
        }
    }

    private void validateImageContent(
            MultipartFile file,
            String label
    ) {
        try (InputStream inputStream = file.getInputStream()) {
            byte[] header = inputStream.readNBytes(12);

            if (header.length < 8) {
                throw new IllegalArgumentException(
                        label + " is not a valid image file."
                );
            }

            boolean jpeg =
                    (header[0] & 0xFF) == 0xFF
                            && (header[1] & 0xFF) == 0xD8
                            && (header[2] & 0xFF) == 0xFF;

            boolean png =
                    header[0] == (byte) 0x89
                            && header[1] == 0x50
                            && header[2] == 0x4E
                            && header[3] == 0x47
                            && header[4] == 0x0D
                            && header[5] == 0x0A
                            && header[6] == 0x1A
                            && header[7] == 0x0A;

            boolean webp =
                    header.length >= 12
                            && header[0] == 0x52
                            && header[1] == 0x49
                            && header[2] == 0x46
                            && header[3] == 0x46
                            && header[8] == 0x57
                            && header[9] == 0x45
                            && header[10] == 0x42
                            && header[11] == 0x50;

            if (!jpeg && !png && !webp) {
                throw new IllegalArgumentException(
                        label + " content does not match a supported image format."
                );
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException(
                    "Could not validate " + label.toLowerCase(Locale.ROOT) + ".",
                    exception
            );
        }
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null) {
            return "";
        }

        return contentType
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String formatMegabytes(long bytes) {
        return (bytes / (1024 * 1024)) + " MB";
    }
}

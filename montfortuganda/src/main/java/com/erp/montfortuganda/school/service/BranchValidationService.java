package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.school.dto.BranchDTO;
import com.erp.montfortuganda.school.repository.BranchRepository;
import com.erp.montfortuganda.school.repository.LevelRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class BranchValidationService {

    private static final long MAX_LOGO_SIZE =
            500L * 1024L;

    private static final long MAX_PHOTO_SIZE =
            100L * 1024L;

    private static final long MAX_DOCUMENT_PDF_SIZE =
            2L * 1024L * 1024L;

    private static final long MAX_DOCUMENT_IMAGE_SIZE =
            100L * 1024L;

    private static final Set<String> WHATSAPP_OPTIONS =
            Set.of(
                    "NONE",
                    "PRIMARY",
                    "SECONDARY",
                    "BOTH"
            );

    private static final Set<String> IMAGE_CONTENT_TYPES =
            Set.of(
                    "image/jpeg",
                    "image/png"
            );

    private static final Set<String> IMAGE_EXTENSIONS =
            Set.of(
                    "jpg",
                    "jpeg",
                    "png"
            );

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile(
                    "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+"
                            + "@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}"
                            + "[A-Za-z0-9])?"
                            + "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}"
                            + "[A-Za-z0-9])?)+$"
            );

    private static final Pattern PHONE_PATTERN =
            Pattern.compile(
                    "^\\+?[0-9][0-9()\\-\\s]{6,28}[0-9]$"
            );

    private static final Pattern SCHOOL_CODE_PATTERN =
            Pattern.compile("^[A-Za-z0-9_-]+$");

    private final BranchRepository branchRepository;
    private final LevelRepository levelRepository;

    public BranchValidationService(
            BranchRepository branchRepository,
            LevelRepository levelRepository
    ) {
        this.branchRepository = branchRepository;
        this.levelRepository = levelRepository;
    }

    public void validateForCreate(
            BranchDTO branchDTO,
            MultipartFile logo,
            MultipartFile photo,
            List<MultipartFile> documents
    ) {
        validateCommon(
                branchDTO,
                logo,
                photo,
                documents
        );

        String schoolCode =
                normalize(branchDTO.getSchoolCode());

        if (
                branchRepository
                        .existsBySchoolCodeIgnoreCase(
                                schoolCode
                        )
        ) {
            throw new IllegalArgumentException(
                    "A branch already exists with school code "
                            + schoolCode
                            + "."
            );
        }

        String branchEmail =
                normalize(branchDTO.getBranchEmail());

        if (
                branchRepository
                        .existsByBranchEmailIgnoreCase(
                                branchEmail
                        )
        ) {
            throw new IllegalArgumentException(
                    "A branch already exists with email "
                            + branchEmail
                            + "."
            );
        }
    }

    public void validateForUpdate(
            Integer branchId,
            BranchDTO branchDTO,
            MultipartFile logo,
            MultipartFile photo,
            List<MultipartFile> documents
    ) {
        if (branchId == null || branchId <= 0) {
            throw new IllegalArgumentException(
                    "A valid branch ID is required."
            );
        }

        validateCommon(
                branchDTO,
                logo,
                photo,
                documents
        );

        String schoolCode =
                normalize(branchDTO.getSchoolCode());

        if (
                branchRepository
                        .existsBySchoolCodeIgnoreCaseAndBranchIdNot(
                                schoolCode,
                                branchId
                        )
        ) {
            throw new IllegalArgumentException(
                    "Another branch already uses school code "
                            + schoolCode
                            + "."
            );
        }

        String branchEmail =
                normalize(branchDTO.getBranchEmail());

        if (
                branchRepository
                        .existsByBranchEmailIgnoreCaseAndBranchIdNot(
                                branchEmail,
                                branchId
                        )
        ) {
            throw new IllegalArgumentException(
                    "Another branch already uses email "
                            + branchEmail
                            + "."
            );
        }
    }

    private void validateCommon(
            BranchDTO branchDTO,
            MultipartFile logo,
            MultipartFile photo,
            List<MultipartFile> documents
    ) {
        if (branchDTO == null) {
            throw new IllegalArgumentException(
                    "Branch information is required."
            );
        }

        requireText(
                branchDTO.getBranchName(),
                "Branch name"
        );

        requireText(
                branchDTO.getSchoolCode(),
                "School code"
        );

        requireText(
                branchDTO.getBranchLocation(),
                "Short location"
        );

        requireText(
                branchDTO.getAddressLine1(),
                "Address line 1"
        );

        requireText(
                branchDTO.getCountry(),
                "Country"
        );

        requireText(
                branchDTO.getPrimaryPhone(),
                "Primary phone number"
        );

        requireText(
                branchDTO.getBranchEmail(),
                "Branch email"
        );

        validateLength(
                branchDTO.getBranchName(),
                255,
                "Branch name"
        );

        validateSchoolCode(
                branchDTO.getSchoolCode()
        );

        validateLength(
                branchDTO.getBranchLocation(),
                255,
                "Short location"
        );

        validateLength(
                branchDTO.getAddressLine1(),
                255,
                "Address line 1"
        );

        validateLength(
                branchDTO.getAddressLine2(),
                255,
                "Address line 2"
        );

        validateLength(
                branchDTO.getPoBox(),
                100,
                "P.O. Box"
        );

        validateLength(
                branchDTO.getLocality(),
                150,
                "Locality"
        );

        validateLength(
                branchDTO.getCity(),
                150,
                "City"
        );

        validateLength(
                branchDTO.getDistrict(),
                150,
                "District"
        );

        validateLength(
                branchDTO.getRegion(),
                150,
                "Region"
        );

        validateLength(
                branchDTO.getCountry(),
                100,
                "Country"
        );

        validateLength(
                branchDTO.getPostalCode(),
                30,
                "Postal code"
        );

        validatePhone(
                branchDTO.getPrimaryPhone(),
                "Primary phone number",
                true
        );

        validatePhone(
                branchDTO.getSecondaryPhone(),
                "Secondary phone number",
                false
        );

        validatePhoneCombination(branchDTO);

        validateEmail(
                branchDTO.getBranchEmail(),
                "Branch email",
                true
        );

        validateEmail(
                branchDTO.getEmailReplyTo(),
                "Email Reply-To",
                false
        );

        validateLength(
                branchDTO.getEmailFromName(),
                150,
                "Email sender name"
        );

        validateFoundationDate(
                branchDTO.getFoundationDate()
        );

        validateLevelIds(
                branchDTO.getLevelIds()
        );

        validateImage(
                logo,
                "Branch logo",
                MAX_LOGO_SIZE
        );

        validateImage(
                photo,
                "School photo",
                MAX_PHOTO_SIZE
        );

        validateDocuments(documents);
    }

    private void validateSchoolCode(
            String schoolCode
    ) {
        String normalizedCode =
                normalize(schoolCode);

        if (normalizedCode.length() > 10) {
            throw new IllegalArgumentException(
                    "School code cannot exceed 10 characters."
            );
        }

        if (
                !SCHOOL_CODE_PATTERN
                        .matcher(normalizedCode)
                        .matches()
        ) {
            throw new IllegalArgumentException(
                    "School code may contain only letters, "
                            + "numbers, hyphens and underscores."
            );
        }
    }

    private void validatePhoneCombination(
            BranchDTO branchDTO
    ) {
        String primaryPhone =
                normalizePhone(
                        branchDTO.getPrimaryPhone()
                );

        String secondaryPhone =
                normalizePhone(
                        branchDTO.getSecondaryPhone()
                );

        if (
                !secondaryPhone.isEmpty()
                        && primaryPhone.equals(secondaryPhone)
        ) {
            throw new IllegalArgumentException(
                    "Primary and secondary phone numbers "
                            + "must be different."
            );
        }

        String whatsappPhone =
                normalize(
                        branchDTO.getWhatsappPhone()
                ).toUpperCase(Locale.ROOT);

        if (whatsappPhone.isEmpty()) {
            whatsappPhone = "NONE";
        }

        if (!WHATSAPP_OPTIONS.contains(whatsappPhone)) {
            throw new IllegalArgumentException(
                    "WhatsApp selection must be NONE, "
                            + "PRIMARY, SECONDARY or BOTH."
            );
        }

        if (
                "SECONDARY".equals(whatsappPhone)
                        && secondaryPhone.isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "Enter the secondary phone number selected "
                            + "for WhatsApp."
            );
        }

        if (
                "BOTH".equals(whatsappPhone)
                        && secondaryPhone.isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "Enter both phone numbers when both are "
                            + "selected for WhatsApp."
            );
        }
    }

    private void validateLevelIds(
            List<Integer> levelIds
    ) {
        if (levelIds == null || levelIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Select at least one education level."
            );
        }

        Set<Integer> uniqueLevelIds =
                new HashSet<>();

        for (Integer levelId : levelIds) {
            if (levelId == null || levelId <= 0) {
                throw new IllegalArgumentException(
                        "One or more selected education levels "
                                + "are invalid."
                );
            }

            uniqueLevelIds.add(levelId);
        }

        int existingLevels =
                levelRepository
                        .findAllById(uniqueLevelIds)
                        .size();

        if (existingLevels != uniqueLevelIds.size()) {
            throw new IllegalArgumentException(
                    "One or more selected education levels "
                            + "do not exist."
            );
        }
    }

    private void validateFoundationDate(
            LocalDate foundationDate
    ) {
        if (
                foundationDate != null
                        && foundationDate.isAfter(
                        LocalDate.now()
                )
        ) {
            throw new IllegalArgumentException(
                    "Foundation date cannot be in the future."
            );
        }
    }

    private void validatePhone(
            String phone,
            String fieldName,
            boolean required
    ) {
        String value = normalize(phone);

        if (value.isEmpty()) {
            if (required) {
                throw new IllegalArgumentException(
                        fieldName + " is required."
                );
            }

            return;
        }

        if (value.length() > 30) {
            throw new IllegalArgumentException(
                    fieldName
                            + " cannot exceed 30 characters."
            );
        }

        if (!PHONE_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    fieldName
                            + " contains an invalid phone number."
            );
        }
    }

    private void validateEmail(
            String email,
            String fieldName,
            boolean required
    ) {
        String value = normalize(email);

        if (value.isEmpty()) {
            if (required) {
                throw new IllegalArgumentException(
                        fieldName + " is required."
                );
            }

            return;
        }

        if (value.length() > 150) {
            throw new IllegalArgumentException(
                    fieldName
                            + " cannot exceed 150 characters."
            );
        }

        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    fieldName
                            + " contains an invalid email address."
            );
        }
    }

    private void validateImage(
            MultipartFile file,
            String fieldName,
            long maximumSize
    ) {
        if (file == null || file.isEmpty()) {
            return;
        }

        if (file.getSize() > maximumSize) {
            throw new IllegalArgumentException(
                    fieldName
                            + " exceeds the maximum size of "
                            + formatKilobytes(maximumSize)
                            + "."
            );
        }

        String contentType =
                normalizeContentType(
                        file.getContentType()
                );

        String extension =
                getExtension(
                        file.getOriginalFilename()
                );

        if (
                !IMAGE_CONTENT_TYPES.contains(contentType)
                        || !IMAGE_EXTENSIONS.contains(extension)
        ) {
            throw new IllegalArgumentException(
                    fieldName
                            + " must be a JPG, JPEG or PNG file."
            );
        }
    }

    private void validateDocuments(
            List<MultipartFile> documents
    ) {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        for (MultipartFile document : documents) {
            if (document == null || document.isEmpty()) {
                continue;
            }

            String contentType =
                    normalizeContentType(
                            document.getContentType()
                    );

            String extension =
                    getExtension(
                            document.getOriginalFilename()
                    );

            if ("application/pdf".equals(contentType)) {
                if (!"pdf".equals(extension)) {
                    throw new IllegalArgumentException(
                            "A government document has an invalid "
                                    + "PDF filename."
                    );
                }

                if (
                        document.getSize()
                                > MAX_DOCUMENT_PDF_SIZE
                ) {
                    throw new IllegalArgumentException(
                            "Government PDF documents cannot exceed 2 MB."
                    );
                }

                continue;
            }

            if (
                    IMAGE_CONTENT_TYPES.contains(contentType)
                            && IMAGE_EXTENSIONS.contains(extension)
            ) {
                if (
                        document.getSize()
                                > MAX_DOCUMENT_IMAGE_SIZE
                ) {
                    throw new IllegalArgumentException(
                            "Government document images cannot "
                                    + "exceed 100 KB."
                    );
                }

                continue;
            }

            throw new IllegalArgumentException(
                    "Government documents must be PDF, JPG, JPEG "
                            + "or PNG files."
            );
        }
    }

    private void requireText(
            String value,
            String fieldName
    ) {
        if (normalize(value).isEmpty()) {
            throw new IllegalArgumentException(
                    fieldName + " is required."
            );
        }
    }

    private void validateLength(
            String value,
            int maximumLength,
            String fieldName
    ) {
        String normalizedValue = normalize(value);

        if (
                !normalizedValue.isEmpty()
                        && normalizedValue.length()
                        > maximumLength
        ) {
            throw new IllegalArgumentException(
                    fieldName
                            + " cannot exceed "
                            + maximumLength
                            + " characters."
            );
        }
    }

    private String normalize(
            String value
    ) {
        return value == null
                ? ""
                : value.trim();
    }

    private String normalizePhone(
            String value
    ) {
        return normalize(value)
                .replaceAll("[^0-9+]", "");
    }

    private String normalizeContentType(
            String contentType
    ) {
        return normalize(contentType)
                .toLowerCase(Locale.ROOT);
    }

    private String getExtension(
            String originalFilename
    ) {
        String filename =
                normalize(originalFilename);

        int extensionIndex =
                filename.lastIndexOf('.');

        if (
                extensionIndex < 0
                        || extensionIndex
                        == filename.length() - 1
        ) {
            return "";
        }

        return filename
                .substring(extensionIndex + 1)
                .toLowerCase(Locale.ROOT);
    }

    private String formatKilobytes(
            long bytes
    ) {
        return (bytes / 1024L) + " KB";
    }
}
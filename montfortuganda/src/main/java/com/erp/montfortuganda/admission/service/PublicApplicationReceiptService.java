package com.erp.montfortuganda.admission.service;

import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.admission.repository.ErpApplicationRepository;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Receipt-specific public application data and branding.
 *
 * <p>The caller must supply only the application ID recovered from the
 * already-verified public HTTP session. No application number, branch ID,
 * private logo path, or filesystem path is accepted from the browser.</p>
 */
@Service
@Transactional(readOnly = true)
public class PublicApplicationReceiptService {

    private final PublicApplicationService publicApplicationService;
    private final ErpApplicationRepository applicationRepository;
    private final FileStorageService fileStorageService;
    private final Path publicStorageRoot;

    public PublicApplicationReceiptService(
            PublicApplicationService publicApplicationService,
            ErpApplicationRepository applicationRepository,
            FileStorageService fileStorageService,
            @Value("${erp.storage.location:uploads}")
            String publicStorageLocation
    ) {
        this.publicApplicationService = publicApplicationService;
        this.applicationRepository = applicationRepository;
        this.fileStorageService = fileStorageService;
        this.publicStorageRoot =
                Path.of(publicStorageLocation)
                        .toAbsolutePath()
                        .normalize();
    }

    /**
     * Returns the existing printable application details enriched with the
     * latest authoritative admission-workflow state.
     */
    public Map<String, Object> getReceiptDetails(
            Long applicationId
    ) {
        ErpApplication application =
                findApplication(applicationId);

        Map<String, Object> existingResponse =
                publicApplicationService
                        .getApplicationDetails(applicationId);

        if (!Boolean.TRUE.equals(
                existingResponse.get("success")
        )) {
            return existingResponse;
        }

        Map<String, Object> receiptData =
                copyData(existingResponse.get("data"));

        receiptData.put(
                "application_status",
                enumName(application.getApplicationStatus())
        );
        receiptData.put(
                "current_stage",
                enumName(application.getCurrentStage())
        );
        receiptData.put(
                "verification_status",
                enumName(application.getVerificationStatus())
        );
        receiptData.put(
                "document_status",
                enumName(application.getDocumentStatus())
        );
        receiptData.put(
                "test_status",
                enumName(application.getTestStatus())
        );
        receiptData.put(
                "fee_decision_status",
                enumName(application.getFeeDecisionStatus())
        );
        receiptData.put(
                "scholarship_workflow_status",
                application.getScholarshipStatus()
        );
        receiptData.put(
                "payment_status",
                enumName(application.getPaymentStatus())
        );
        receiptData.put(
                "admission_status",
                enumName(application.getAdmissionStatus())
        );
        receiptData.put(
                "workflow_locked",
                Boolean.TRUE.equals(
                        application.getWorkflowLocked()
                )
        );

        /*
         * The receipt should communicate the actual current process step.
         * Keep the legacy status field for compatibility, but expose a
         * dedicated receipt_status that prefers the workflow stage.
         */
        receiptData.put(
                "receipt_status",
                application.getCurrentStage() != null
                        ? application.getCurrentStage().name()
                        : enumName(
                                application.getApplicationStatus()
                        )
        );

        Branch branch = application.getBranch();

        receiptData.put(
                "school_logo_available",
                branch != null
                        && StringUtils.hasText(
                        branch.getBranchLogoUrl()
                )
        );

        receiptData.put(
                "applicant_photo_available",
                StringUtils.hasText(
                        application.getPhotoPath()
                )
        );

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("success", true);
        response.put("data", receiptData);

        return response;
    }

    /**
     * Loads the selected application's Branch logo from secure private
     * Branch/Admin storage without exposing its stored path.
     */
    public PublicReceiptLogoResource loadSchoolLogo(
            Long applicationId
    ) {
        ErpApplication application =
                findApplication(applicationId);

        Branch branch =
                application.getBranch();

        if (branch == null
                || !StringUtils.hasText(
                branch.getBranchLogoUrl()
        )) {
            throw new ResourceNotFoundException(
                    "The selected school logo is not available."
            );
        }

        String relativeLogoPath =
                branch.getBranchLogoUrl()
                        .trim();

        Resource resource;

        try {
            resource =
                    fileStorageService.loadPrivateFile(
                            relativeLogoPath
                    );
        } catch (RuntimeException exception) {
            throw new ResourceNotFoundException(
                    "The selected school logo is not available."
            );
        }

        String contentType =
                fileStorageService.detectContentType(
                        relativeLogoPath
                );

        if (!StringUtils.hasText(contentType)
                || !contentType
                .toLowerCase(Locale.ROOT)
                .startsWith("image/")) {
            throw new ResourceNotFoundException(
                    "The selected school logo is not a valid image."
            );
        }

        String fileName =
                Path.of(relativeLogoPath)
                        .getFileName()
                        .toString();

        return new PublicReceiptLogoResource(
                resource,
                contentType,
                fileName
        );
    }

    /**
     * Loads the selected applicant's passport/photo from the configured
     * public application storage without exposing the stored filesystem path.
     */
    public PublicReceiptPhotoResource loadApplicantPhoto(
            Long applicationId
    ) {
        ErpApplication application =
                findApplication(applicationId);

        String storedPhotoPath =
                application.getPhotoPath();

        if (!StringUtils.hasText(storedPhotoPath)) {
            throw new ResourceNotFoundException(
                    "Applicant photo is not available."
            );
        }

        String relativePath =
                storedPhotoPath.trim()
                        .replace('\\', '/');

        while (relativePath.startsWith("/")) {
            relativePath =
                    relativePath.substring(1);
        }

        /*
         * Existing DB values are stored as:
         * /uploads/applications/<branch>/<application>/<file>
         *
         * erp.storage.location already points at the physical uploads root,
         * so strip only the logical "uploads/" prefix before resolving.
         */
        if (relativePath.startsWith("uploads/")) {
            relativePath =
                    relativePath.substring(
                            "uploads/".length()
                    );
        }

        if (!StringUtils.hasText(relativePath)) {
            throw new ResourceNotFoundException(
                    "Applicant photo is not available."
            );
        }

        Path photoPath =
                publicStorageRoot
                        .resolve(relativePath)
                        .normalize();

        if (!photoPath.startsWith(publicStorageRoot)) {
            throw new ResourceNotFoundException(
                    "Applicant photo is not available."
            );
        }

        try {
            if (!Files.exists(photoPath)
                    || !Files.isRegularFile(photoPath)
                    || !Files.isReadable(photoPath)) {
                throw new ResourceNotFoundException(
                        "Applicant photo is not available."
                );
            }

            String contentType =
                    Files.probeContentType(photoPath);

            if (!StringUtils.hasText(contentType)
                    || !contentType
                    .toLowerCase(Locale.ROOT)
                    .startsWith("image/")) {
                throw new ResourceNotFoundException(
                        "Applicant photo is not a valid image."
                );
            }

            Resource resource =
                    new UrlResource(
                            photoPath.toUri()
                    );

            if (!resource.exists()
                    || !resource.isReadable()) {
                throw new ResourceNotFoundException(
                        "Applicant photo is not available."
                );
            }

            return new PublicReceiptPhotoResource(
                    resource,
                    contentType,
                    photoPath
                            .getFileName()
                            .toString()
            );

        } catch (ResourceNotFoundException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResourceNotFoundException(
                    "Applicant photo is not available."
            );
        }
    }

    private ErpApplication findApplication(
            Long applicationId
    ) {
        if (applicationId == null
                || applicationId <= 0) {
            throw new ResourceNotFoundException(
                    "Application was not found."
            );
        }

        return applicationRepository
                .findById(applicationId)
                .filter(application ->
                        application.getStatus() == null
                                || application.getStatus() == 1
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Application was not found."
                        )
                );
    }

    private Map<String, Object> copyData(
            Object value
    ) {
        Map<String, Object> copy =
                new LinkedHashMap<>();

        if (!(value instanceof Map<?, ?> source)) {
            return copy;
        }

        source.forEach((key, item) -> {
            if (key != null) {
                copy.put(
                        String.valueOf(key),
                        item
                );
            }
        });

        return copy;
    }

    private String enumName(
            Enum<?> value
    ) {
        return value == null
                ? ""
                : value.name();
    }

    public record PublicReceiptLogoResource(
            Resource resource,
            String contentType,
            String fileName
    ) {
    }

    public record PublicReceiptPhotoResource(
            Resource resource,
            String contentType,
            String fileName
    ) {
    }
}

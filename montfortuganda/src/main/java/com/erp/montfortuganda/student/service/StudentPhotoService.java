package com.erp.montfortuganda.student.service;

import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.student.dto.request.StudentPhotoUploadRequest;
import com.erp.montfortuganda.student.dto.response.StudentPersonalResponse;
import com.erp.montfortuganda.student.entity.ErpStudent;
import com.erp.montfortuganda.student.mapper.StudentMapper;
import com.erp.montfortuganda.student.repository.ErpStudentRepository;
import jakarta.persistence.EntityManager;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Objects;

/**
 * Handles secure Student profile-photo upload, replacement,
 * protected retrieval and removal.
 *
 * Physical file paths are never exposed directly to the frontend.
 */
@Service
public class StudentPhotoService {

    private final ErpStudentRepository studentRepository;
    private final StudentValidationService validationService;
    private final StudentFileService fileService;
    private final StudentMapper studentMapper;
    private final EntityManager entityManager;

    public StudentPhotoService(
            ErpStudentRepository studentRepository,
            StudentValidationService validationService,
            StudentFileService fileService,
            StudentMapper studentMapper,
            EntityManager entityManager
    ) {
        this.studentRepository = studentRepository;
        this.validationService = validationService;
        this.fileService = fileService;
        this.studentMapper = studentMapper;
        this.entityManager = entityManager;
    }

    // =====================================================================
    // UPLOAD OR REPLACE
    // =====================================================================

    /**
     * Uploads a new Student photo or replaces the current photo.
     *
     * If the database transaction fails, the newly stored file is removed.
     * If replacement succeeds, the previous file is removed after commit.
     */
    @Transactional
    public StudentPersonalResponse uploadOrReplacePhoto(
            Long studentId,
            StudentPhotoUploadRequest request
    ) {
        validateUploadRequest(request);

        StudentValidationService.BranchContext branchContext =
                validationService.requireAuthenticatedBranch();

        ErpStudent student =
                validationService.requireStudent(
                        studentId,
                        branchContext.branch().getBranchId()
                );

        validateVersion(
                student,
                request.version()
        );

        String previousPhotoPath =
                student.getPhotoPath();

        StudentFileService.StoredFile storedPhoto;

        if (StringUtils.hasText(previousPhotoPath)) {
            storedPhoto =
                    fileService.replaceStudentPhoto(
                            request.photo(),
                            student,
                            previousPhotoPath
                    );
        } else {
            storedPhoto =
                    fileService.storeStudentPhoto(
                            request.photo(),
                            student
                    );
        }

        student.setPhotoPath(
                storedPhoto.relativePath()
        );

        ErpStudent savedStudent =
                studentRepository.saveAndFlush(
                        student
                );

        /*
         * Refreshes database-generated audit and version values
         * before preparing the API response.
         */
        entityManager.refresh(
                savedStudent
        );

        return studentMapper.toPersonalResponse(
                savedStudent
        );
    }

    // =====================================================================
    // PROTECTED PHOTO RETRIEVAL
    // =====================================================================

    @Transactional(readOnly = true)
    public StudentPhotoFile loadStudentPhoto(
            Long studentId
    ) {
        StudentValidationService.BranchContext branchContext =
                validationService.requireAuthenticatedBranch();

        ErpStudent student =
                validationService.requireStudent(
                        studentId,
                        branchContext.branch().getBranchId()
                );

        String photoPath =
                student.getPhotoPath();

        if (!StringUtils.hasText(photoPath)) {
            throw new ResourceNotFoundException(
                    "Student photo was not found."
            );
        }

        Resource resource =
                fileService.loadPrivateFile(
                        photoPath
                );

        String contentType =
                fileService.detectContentType(
                        photoPath
                );

        validatePhotoContentType(
                contentType
        );

        long fileSize =
                fileService.getStoredFileSize(
                        photoPath
                );

        String fileName =
                buildPhotoFilename(
                        student,
                        contentType
                );

        return new StudentPhotoFile(
                resource,
                fileName,
                contentType,
                fileSize
        );
    }

    // =====================================================================
    // REMOVE PHOTO
    // =====================================================================

    /**
     * Removes the Student photo reference from the database.
     *
     * The physical file is deleted only after the database transaction
     * commits successfully.
     */
    @Transactional
    public StudentPersonalResponse removeStudentPhoto(
            Long studentId,
            Long version
    ) {
        StudentValidationService.BranchContext branchContext =
                validationService.requireAuthenticatedBranch();

        ErpStudent student =
                validationService.requireStudent(
                        studentId,
                        branchContext.branch().getBranchId()
                );

        validateVersion(
                student,
                version
        );

        String currentPhotoPath =
                student.getPhotoPath();

        if (!StringUtils.hasText(currentPhotoPath)) {
            throw new ResourceNotFoundException(
                    "Student photo was not found."
            );
        }

        student.setPhotoPath(null);

        ErpStudent savedStudent =
                studentRepository.saveAndFlush(
                        student
                );

        fileService.scheduleDeleteAfterCommit(
                currentPhotoPath
        );

        entityManager.refresh(
                savedStudent
        );

        return studentMapper.toPersonalResponse(
                savedStudent
        );
    }

    // =====================================================================
    // VALIDATION
    // =====================================================================

    private void validateUploadRequest(
            StudentPhotoUploadRequest request
    ) {
        if (request == null) {
            throw new BadRequestException(
                    "Student photo upload request is required."
            );
        }

        if (
                request.photo() == null
                        || request.photo().isEmpty()
        ) {
            throw new BadRequestException(
                    "Student photo is required."
            );
        }

        if (request.version() == null) {
            throw new BadRequestException(
                    "Student version is required."
            );
        }

        if (
                !StringUtils.hasText(
                        request.operationId()
                )
        ) {
            throw new BadRequestException(
                    "Operation ID is required."
            );
        }
    }

    private void validateVersion(
            ErpStudent student,
            Long submittedVersion
    ) {
        if (submittedVersion == null) {
            throw new BadRequestException(
                    "Student version is required."
            );
        }

        if (
                !Objects.equals(
                        student.getVersion(),
                        submittedVersion
                )
        ) {
            throw new BadRequestException(
                    "This Student record was changed by another user. "
                            + "Reload the Student and try again."
            );
        }
    }

    private void validatePhotoContentType(
            String contentType
    ) {
        if (!StringUtils.hasText(contentType)) {
            throw new ResourceNotFoundException(
                    "Student photo format could not be determined."
            );
        }

        String normalizedContentType =
                contentType.trim()
                        .toLowerCase(Locale.ROOT);

        boolean supported =
                normalizedContentType.equals("image/jpeg")
                        || normalizedContentType.equals("image/png")
                        || normalizedContentType.equals("image/webp");

        if (!supported) {
            throw new ResourceNotFoundException(
                    "Stored Student photo has an unsupported format."
            );
        }
    }

    // =====================================================================
    // HELPERS
    // =====================================================================

    private String buildPhotoFilename(
            ErpStudent student,
            String contentType
    ) {
        String extension =
                extensionForContentType(
                        contentType
                );

        String studentCode =
                StringUtils.hasText(student.getStudentCode())
                        ? student.getStudentCode().trim()
                        : "student";

        return studentCode
                + "-photo."
                + extension;
    }

    private String extensionForContentType(
            String contentType
    ) {
        return switch (
                contentType.trim()
                        .toLowerCase(Locale.ROOT)
                ) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";

            default -> throw new ResourceNotFoundException(
                    "Stored Student photo has an unsupported format."
            );
        };
    }

    // =====================================================================
    // RESULT
    // =====================================================================

    public record StudentPhotoFile(
            Resource resource,
            String fileName,
            String contentType,
            long fileSize
    ) {
    }
}
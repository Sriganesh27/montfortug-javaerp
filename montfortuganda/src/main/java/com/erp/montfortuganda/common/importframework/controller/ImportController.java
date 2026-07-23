package com.erp.montfortuganda.common.importframework.controller;

import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.common.importframework.engine.ImportFacade;
import com.erp.montfortuganda.common.importframework.lifecycle.ImportMode;
import com.erp.montfortuganda.common.importframework.model.ErpImportJob;
import com.erp.montfortuganda.common.importframework.service.ImportJobService;
import com.erp.montfortuganda.employee.bulkimport.plugin.EmployeeImportPlugin;
import com.erp.montfortuganda.employee.bulkimport.processor.EmployeeBulkImportProcessor;
import com.erp.montfortuganda.exception.BranchNotAssignedException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportController {

    private static final long MAXIMUM_IMPORT_FILE_SIZE =
            10L * 1024L * 1024L;

    private final ImportFacade importFacade;
    private final ImportJobService importJobService;
    private final CurrentUserService currentUserService;

    @PostMapping("/{moduleName}")
    public ResponseEntity<String> startImport(
            @PathVariable
            String moduleName,

            @RequestParam("file")
            MultipartFile file,

            @RequestParam(defaultValue = "INSERT")
            ImportMode mode,

            @RequestParam(
                    defaultValue = "false"
            )
            boolean createCredentials,

            @RequestParam(
                    defaultValue = "false"
            )
            boolean sendEmail,

            @RequestParam(
                    required = false
            )
            Long roleId
    ) {
        Path temporaryFile = null;

        try {
            CurrentUserContext currentUser =
                    requireCurrentUser();

            String normalizedModule =
                    normalizeModuleName(
                            moduleName
                    );

            validateFile(file);

            Map<String, Object> importOptions =
                    buildImportOptions(
                            normalizedModule,
                            currentUser,
                            createCredentials,
                            sendEmail,
                            roleId
                    );

            String fileHash;

            try (
                    InputStream inputStream =
                            file.getInputStream()
            ) {
                fileHash =
                        DigestUtils.sha256Hex(
                                inputStream
                        );
            }

            String originalName =
                    sanitizeOriginalFilename(
                            file.getOriginalFilename()
                    );

            String uniqueFileName =
                    UUID.randomUUID()
                            + "_"
                            + originalName;

            temporaryFile =
                    Path.of(
                                    System.getProperty(
                                            "java.io.tmpdir"
                                    ),
                                    uniqueFileName
                            )
                            .toAbsolutePath()
                            .normalize();

            file.transferTo(
                    temporaryFile
            );

            String jobId =
                    importFacade.submitImportJob(
                            normalizedModule,
                            String.valueOf(
                                    currentUser.getBranchId()
                            ),
                            String.valueOf(
                                    currentUser.getUserId()
                            ),
                            mode,
                            fileHash,
                            uniqueFileName,
                            importOptions
                    );

            /*
             * The asynchronous import worker now owns this file.
             */
            temporaryFile = null;

            return ResponseEntity
                    .accepted()
                    .body(jobId);
        } catch (IllegalArgumentException exception) {
            deleteTemporaryFileQuietly(
                    temporaryFile
            );

            return ResponseEntity
                    .badRequest()
                    .body(
                            safeErrorMessage(exception)
                    );
        } catch (
                BranchNotAssignedException
                | AccessDeniedException exception
        ) {
            deleteTemporaryFileQuietly(
                    temporaryFile
            );

            return ResponseEntity
                    .status(
                            HttpStatus.FORBIDDEN
                    )
                    .body(
                            safeErrorMessage(exception)
                    );
        } catch (Exception exception) {
            deleteTemporaryFileQuietly(
                    temporaryFile
            );

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            "The import job could not be started."
                    );
        }
    }

    @GetMapping("/progress/{jobId}")
    public ResponseEntity<ErpImportJob> getProgress(
            @PathVariable
            String jobId
    ) {
        return importJobService
                .getJobStatus(jobId)
                .map(
                        ResponseEntity::ok
                )
                .orElse(
                        ResponseEntity
                                .notFound()
                                .build()
                );
    }

    @GetMapping("/history/{moduleName}")
    public ResponseEntity<List<ErpImportJob>> getHistory(
            @PathVariable
            String moduleName
    ) {
        return ResponseEntity.ok(
                importJobService.getRecentJobs(
                        normalizeModuleName(
                                moduleName
                        )
                )
        );
    }

    @GetMapping("/errors/{jobId}")
    public ResponseEntity<byte[]> downloadErrorReport(
            @PathVariable
            String jobId
    ) {
        byte[] excelData =
                importJobService.generateErrorReport(
                        jobId
                );

        if (excelData == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument."
                                + "spreadsheetml.sheet"
                )
        );

        headers.setContentDispositionFormData(
                "attachment",
                "Error_Report_"
                        + jobId
                        + ".xlsx"
        );

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(excelData);
    }

    private CurrentUserContext requireCurrentUser() {
        CurrentUserContext currentUser =
                currentUserService
                        .getCurrentUserContext();

        if (
                currentUser.getUserId() == null
                        || currentUser.getUserId() <= 0
        ) {
            throw new AccessDeniedException(
                    "The authenticated user could not be resolved."
            );
        }

        if (
                currentUser.getUsername() == null
                        || currentUser.getUsername()
                        .isBlank()
        ) {
            throw new AccessDeniedException(
                    "The authenticated username could not be resolved."
            );
        }

        if (
                currentUser.getBranchId() == null
                        || currentUser.getBranchId() <= 0
        ) {
            throw new BranchNotAssignedException(
                    "The authenticated user is not assigned to a branch."
            );
        }

        return currentUser;
    }

    private Map<String, Object> buildImportOptions(
            String moduleName,
            CurrentUserContext currentUser,
            boolean createCredentials,
            boolean sendEmail,
            Long roleId
    ) {
        if (
                !EmployeeImportPlugin.MODULE_NAME
                        .equals(moduleName)
        ) {
            if (
                    createCredentials
                            || sendEmail
                            || roleId != null
            ) {
                throw new IllegalArgumentException(
                        "Employee account options can be used only "
                                + "with the Employee import module."
                );
            }

            return Map.of();
        }

        if (
                sendEmail
                        && !createCredentials
        ) {
            throw new IllegalArgumentException(
                    "Send Email requires Create Credentials."
            );
        }

        if (
                createCredentials
                        && (
                        roleId == null
                                || roleId <= 0
                )
        ) {
            throw new IllegalArgumentException(
                    "A valid Employee login role is required when "
                            + "Create Credentials is enabled."
            );
        }

        if (
                !createCredentials
                        && roleId != null
        ) {
            throw new IllegalArgumentException(
                    "A login role cannot be selected when "
                            + "Create Credentials is disabled."
            );
        }

        Map<String, Object> options =
                new HashMap<>();

        options.put(
                EmployeeBulkImportProcessor
                        .OPTION_CREATE_CREDENTIALS,
                createCredentials
        );

        options.put(
                EmployeeBulkImportProcessor
                        .OPTION_SEND_EMAIL,
                sendEmail
        );

        /*
         * Map.copyOf(...) does not accept null values, so roleId is added
         * only when account generation is enabled.
         */
        if (roleId != null) {
            options.put(
                    EmployeeBulkImportProcessor
                            .OPTION_ROLE_ID,
                    roleId
            );
        }

        options.put(
                EmployeeBulkImportProcessor
                        .OPTION_SUBMITTED_BY_USERNAME,
                currentUser.getUsername()
        );

        return Map.copyOf(options);
    }

    private void validateFile(
            MultipartFile file
    ) {
        if (
                file == null
                        || file.isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "An Employee import Excel file is required."
            );
        }

        if (
                file.getSize() <= 0
                        || file.getSize()
                        > MAXIMUM_IMPORT_FILE_SIZE
        ) {
            throw new IllegalArgumentException(
                    "The import file must not exceed 10 MB."
            );
        }

        String originalName =
                file.getOriginalFilename();

        if (
                originalName == null
                        || !originalName
                        .toLowerCase(
                                Locale.ROOT
                        )
                        .endsWith(".xlsx")
        ) {
            throw new IllegalArgumentException(
                    "Only XLSX import files are supported."
            );
        }
    }

    private String normalizeModuleName(
            String moduleName
    ) {
        if (
                moduleName == null
                        || moduleName.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Import module is required."
            );
        }

        return moduleName
                .trim()
                .toUpperCase(
                        Locale.ROOT
                );
    }

    private String sanitizeOriginalFilename(
            String originalFilename
    ) {
        String sanitized =
                originalFilename == null
                        ? "import.xlsx"
                        : originalFilename.replaceAll(
                        "[^a-zA-Z0-9._-]",
                        "_"
                );

        if (
                sanitized.isBlank()
                        || !sanitized
                        .toLowerCase(
                                Locale.ROOT
                        )
                        .endsWith(".xlsx")
        ) {
            return "import.xlsx";
        }

        if (sanitized.length() > 150) {
            String extension =
                    ".xlsx";

            sanitized =
                    sanitized.substring(
                            0,
                            150 - extension.length()
                    )
                            + extension;
        }

        return sanitized;
    }

    private String safeErrorMessage(
            RuntimeException exception
    ) {
        String message =
                exception.getMessage();

        if (
                message == null
                        || message.isBlank()
        ) {
            return "The import request is invalid.";
        }

        return message;
    }

    private void deleteTemporaryFileQuietly(
            Path temporaryFile
    ) {
        if (temporaryFile == null) {
            return;
        }

        try {
            Files.deleteIfExists(
                    temporaryFile
            );
        } catch (Exception ignored) {
            /*
             * Request failure must not be replaced by temporary-file
             * cleanup failure.
             */
        }
    }
}
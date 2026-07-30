package com.erp.montfortuganda.common.importframework.controller;

import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.common.importframework.engine.ImportFacade;
import com.erp.montfortuganda.common.importframework.lifecycle.ImportMode;
import com.erp.montfortuganda.common.importframework.model.ErpImportJob;
import com.erp.montfortuganda.common.importframework.service.ImportJobService;
import com.erp.montfortuganda.common.importframework.service.RetryWorkbookMetadataService;
import com.erp.montfortuganda.employee.bulkimport.plugin.EmployeeImportPlugin;
import com.erp.montfortuganda.employee.bulkimport.processor.EmployeeBulkImportProcessor;
import com.erp.montfortuganda.exception.BranchNotAssignedException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
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

    private static final MediaType XLSX_MEDIA_TYPE =
            MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument."
                            + "spreadsheetml.sheet"
            );

    private final ImportFacade importFacade;
    private final ImportJobService importJobService;
    private final RetryWorkbookMetadataService retryWorkbookMetadataService;
    private final CurrentUserService currentUserService;

    @PostMapping("/{moduleName}")
    public ResponseEntity<String> startImport(
            @PathVariable
            String moduleName,

            @RequestParam("file")
            MultipartFile file,

            @RequestParam(defaultValue = "INSERT")
            ImportMode mode,

            @RequestParam(defaultValue = "false")
            boolean createCredentials,

            @RequestParam(defaultValue = "false")
            boolean sendEmail,

            @RequestParam(required = false)
            Long roleId
    ) {
        Path temporaryFile = null;

        try {
            CurrentUserContext currentUser =
                    requireCurrentUser();

            String normalizedModule =
                    normalizeModuleName(moduleName);

            validateFile(file);

            Map<String, Object> importOptions =
                    buildImportOptions(
                            normalizedModule,
                            currentUser,
                            createCredentials,
                            sendEmail,
                            roleId
                    );

            String fileHash =
                    calculateFileHash(file);

            String uniqueFileName =
                    buildUniqueFilename(file);

            temporaryFile =
                    resolveTemporaryFile(uniqueFileName);

            file.transferTo(temporaryFile);

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
             * The asynchronous import worker now owns the file.
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
        } catch (IllegalStateException exception) {
            deleteTemporaryFileQuietly(
                    temporaryFile
            );

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
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
                    .status(HttpStatus.FORBIDDEN)
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

    /**
     * Retries only the failed physical Excel rows from an earlier job.
     *
     * The module, branch and failed row numbers are resolved by the backend
     * from the authenticated user's original import job.
     */
    @PostMapping("/retry/{originalJobId}")
    public ResponseEntity<String> retryFailedRows(
            @PathVariable
            String originalJobId,

            @RequestParam("file")
            MultipartFile file,

            @RequestParam(defaultValue = "false")
            boolean createCredentials,

            @RequestParam(defaultValue = "false")
            boolean sendEmail,

            @RequestParam(required = false)
            Long roleId
    ) {
        Path temporaryFile = null;

        try {
            CurrentUserContext currentUser =
                    requireCurrentUser();

            validateFile(file);

            ImportJobService.RetryImportDetails retryDetails =
                    importJobService.prepareRetry(
                            originalJobId
                    );

            String moduleName =
                    normalizeModuleName(
                            retryDetails.module()
                    );

            Map<String, Object> moduleOptions =
                    buildImportOptions(
                            moduleName,
                            currentUser,
                            createCredentials,
                            sendEmail,
                            roleId
                    );

            String fileHash =
                    calculateFileHash(file);

            String uniqueFileName =
                    buildUniqueFilename(file);

            temporaryFile =
                    resolveTemporaryFile(uniqueFileName);

            file.transferTo(temporaryFile);

            RetryWorkbookMetadataService
                    .RetryWorkbookMetadata retryMetadata =
                    retryWorkbookMetadataService
                            .readAndValidate(
                                    temporaryFile,
                                    retryDetails.originalJobId(),
                                    retryDetails.targetRowNumbers()
                            );

            Map<String, Object> retryOptions =
                    buildRetryImportOptions(
                            moduleOptions,
                            retryMetadata
                    );

            String retryJobId =
                    importFacade.submitRetryJob(
                            moduleName,
                            retryDetails.branchId(),
                            String.valueOf(
                                    currentUser.getUserId()
                            ),
                            fileHash,
                            uniqueFileName,
                            retryOptions,
                            retryMetadata.workbookRowNumbers()
                    );

            /*
             * The asynchronous retry worker now owns the file.
             */
            temporaryFile = null;

            return ResponseEntity
                    .accepted()
                    .body(retryJobId);
        } catch (IllegalArgumentException exception) {
            deleteTemporaryFileQuietly(
                    temporaryFile
            );

            return ResponseEntity
                    .badRequest()
                    .body(
                            safeErrorMessage(exception)
                    );
        } catch (IllegalStateException exception) {
            deleteTemporaryFileQuietly(
                    temporaryFile
            );

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
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
                    .status(HttpStatus.FORBIDDEN)
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
                            "The failed import rows could not be retried."
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
                .map(ResponseEntity::ok)
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
                        normalizeModuleName(moduleName)
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
                createDownloadHeaders(
                        "Error_Report_"
                                + jobId
                                + ".xlsx"
                );

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(excelData.length)
                .body(excelData);
    }

    @GetMapping("/corrected/{jobId}")
    public ResponseEntity<Resource> downloadCorrectedWorkbook(
            @PathVariable
            String jobId
    ) {
        return importJobService
                .findCorrectedWorkbook(jobId)
                .map(path ->
                        buildCorrectedWorkbookResponse(
                                jobId,
                                path
                        )
                )
                .orElseGet(() ->
                        ResponseEntity
                                .notFound()
                                .build()
                );
    }

    private ResponseEntity<Resource>
    buildCorrectedWorkbookResponse(
            String jobId,
            Path correctedWorkbook
    ) {
        try {
            if (
                    !Files.isRegularFile(correctedWorkbook)
                            || !Files.isReadable(
                            correctedWorkbook
                    )
            ) {
                return ResponseEntity
                        .notFound()
                        .build();
            }

            Resource resource =
                    new FileSystemResource(
                            correctedWorkbook
                    );

            HttpHeaders headers =
                    createDownloadHeaders(
                            "Corrected_Import_"
                                    + jobId
                                    + ".xlsx"
                    );

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentLength(
                            Files.size(correctedWorkbook)
                    )
                    .body(resource);
        } catch (Exception exception) {
            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .build();
        }
    }

    private String calculateFileHash(
            MultipartFile file
    ) throws Exception {
        try (
                InputStream inputStream =
                        file.getInputStream()
        ) {
            return DigestUtils.sha256Hex(
                    inputStream
            );
        }
    }

    private String buildUniqueFilename(
            MultipartFile file
    ) {
        String originalName =
                sanitizeOriginalFilename(
                        file.getOriginalFilename()
                );

        return UUID.randomUUID()
                + "_"
                + originalName;
    }

    private Path resolveTemporaryFile(
            String uniqueFileName
    ) {
        Path temporaryDirectory =
                Path.of(
                                System.getProperty(
                                        "java.io.tmpdir"
                                )
                        )
                        .toAbsolutePath()
                        .normalize();

        Path temporaryFile =
                temporaryDirectory
                        .resolve(uniqueFileName)
                        .normalize();

        if (!temporaryFile.startsWith(
                temporaryDirectory
        )) {
            throw new SecurityException(
                    "The temporary import file path is invalid."
            );
        }

        return temporaryFile;
    }

    private HttpHeaders createDownloadHeaders(
            String filename
    ) {
        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                XLSX_MEDIA_TYPE
        );

        headers.setContentDispositionFormData(
                "attachment",
                filename
        );

        headers.setCacheControl(
                "no-store, no-cache, must-revalidate"
        );

        headers.setPragma(
                "no-cache"
        );

        return headers;
    }

    private CurrentUserContext requireCurrentUser() {
        CurrentUserContext currentUser =
                currentUserService
                        .getCurrentUserContext();

        if (
                currentUser == null
                        || currentUser.getUserId() == null
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

    /**
     * Adds backend-verified retry metadata without modifying the existing
     * module-specific import options.
     *
     * <p>The compact workbook row numbers are used by GenericExcelReader.
     * The original row mapping is carried through ImportContext for exact
     * error reporting and secure retry traceability.</p>
     */
    private Map<String, Object> buildRetryImportOptions(
            Map<String, Object> moduleOptions,
            RetryWorkbookMetadataService
                    .RetryWorkbookMetadata retryMetadata
    ) {
        if (retryMetadata == null) {
            throw new IllegalArgumentException(
                    "Verified retry workbook metadata is required."
            );
        }

        Map<String, Object> options =
                new HashMap<>();

        if (moduleOptions != null) {
            options.putAll(moduleOptions);
        }

        options.put(
                RetryWorkbookMetadataService
                        .RETRY_ROW_MAPPING_OPTION,
                retryMetadata.originalRowByWorkbookRow()
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
                    "An import Excel file is required."
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
                        .toLowerCase(Locale.ROOT)
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

        String normalizedModule =
                moduleName.trim()
                        .toUpperCase(Locale.ROOT);

        if (
                normalizedModule.length() > 50
                        || !normalizedModule.matches(
                        "[A-Z0-9_]+"
                )
        ) {
            throw new IllegalArgumentException(
                    "Import module is invalid."
            );
        }

        return normalizedModule;
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
                        .toLowerCase(Locale.ROOT)
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
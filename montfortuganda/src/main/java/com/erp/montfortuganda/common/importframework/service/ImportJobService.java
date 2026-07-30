package com.erp.montfortuganda.common.importframework.service;

import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.common.importframework.lifecycle.ImportStatus;
import com.erp.montfortuganda.common.importframework.model.ErpImportErrorRepository;
import com.erp.montfortuganda.common.importframework.model.ErpImportJob;
import com.erp.montfortuganda.common.importframework.model.ErpImportJobRepository;
import com.erp.montfortuganda.common.importframework.report.CorrectedWorkbookService;
import com.erp.montfortuganda.common.importframework.report.ErrorExcelReportGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ImportJobService {

    private final ErpImportJobRepository jobRepository;
    private final ErpImportErrorRepository errorRepository;
    private final ErrorExcelReportGenerator errorReportGenerator;
    private final CorrectedWorkbookService correctedWorkbookService;
    private final CurrentUserService currentUserService;

    /**
     * Returns the requested job only when it belongs to the authenticated
     * user's branch.
     */
    public Optional<ErpImportJob> getJobStatus(
            String jobId
    ) {
        String normalizedJobId =
                requireJobId(jobId);

        String branchId =
                requireAuthenticatedBranchId();

        return jobRepository.findByJobIdAndBranchId(
                normalizedJobId,
                branchId
        );
    }

    /**
     * Returns the latest 50 jobs for the requested module and authenticated
     * branch only.
     */
    public List<ErpImportJob> getRecentJobs(
            String moduleName
    ) {
        String normalizedModule =
                requireModuleName(moduleName);

        String branchId =
                requireAuthenticatedBranchId();

        return jobRepository
                .findTop50ByModuleAndBranchIdOrderByStartedAtDesc(
                        normalizedModule,
                        branchId
                );
    }

    /**
     * Generates the detailed error report only when the job belongs to the
     * authenticated branch.
     */
    public byte[] generateErrorReport(
            String jobId
    ) {
        String normalizedJobId =
                requireJobId(jobId);

        String branchId =
                requireAuthenticatedBranchId();

        boolean jobBelongsToBranch =
                jobRepository.findByJobIdAndBranchId(
                                normalizedJobId,
                                branchId
                        )
                        .isPresent();

        if (!jobBelongsToBranch) {
            return null;
        }

        return errorReportGenerator.generateErrorReport(
                normalizedJobId
        );
    }

    /**
     * Returns the corrected workbook only when the job belongs to the
     * authenticated branch and the file exists.
     */
    public Optional<Path> findCorrectedWorkbook(
            String jobId
    ) {
        String normalizedJobId =
                requireJobId(jobId);

        String branchId =
                requireAuthenticatedBranchId();

        boolean jobBelongsToBranch =
                jobRepository.findByJobIdAndBranchId(
                                normalizedJobId,
                                branchId
                        )
                        .isPresent();

        if (!jobBelongsToBranch) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                correctedWorkbookService.findCorrectedWorkbook(
                        normalizedJobId
                )
        );
    }

    /**
     * Validates the original import job and returns backend-controlled row
     * numbers for a Retry Failed Rows import.
     */
    public RetryImportDetails prepareRetry(
            String originalJobId
    ) {
        String normalizedJobId =
                requireJobId(originalJobId);

        String branchId =
                requireAuthenticatedBranchId();

        ErpImportJob originalJob =
                jobRepository.findByJobIdAndBranchId(
                                normalizedJobId,
                                branchId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "The original import job was not found."
                                )
                        );

        if (
                originalJob.getStatus()
                        != ImportStatus.COMPLETED_WITH_ERRORS
        ) {
            throw new IllegalStateException(
                    "Only an import completed with row errors can be retried."
            );
        }

        Set<Integer> failedRowNumbers =
                errorRepository.findDistinctFailedRowNumbers(
                        normalizedJobId
                );

        if (
                failedRowNumbers == null
                        || failedRowNumbers.isEmpty()
        ) {
            throw new IllegalStateException(
                    "No failed Excel rows were found for this import job."
            );
        }

        return new RetryImportDetails(
                originalJob.getJobId(),
                originalJob.getModule(),
                originalJob.getBranchId(),
                Set.copyOf(failedRowNumbers)
        );
    }

    // =====================================================================
    // AUTHENTICATED BRANCH
    // =====================================================================

    private String requireAuthenticatedBranchId() {
        CurrentUserContext currentUser =
                currentUserService.getCurrentUserContext();

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
                currentUser.getBranchId() == null
                        || currentUser.getBranchId() <= 0
        ) {
            throw new AccessDeniedException(
                    "The authenticated user is not assigned to a branch."
            );
        }

        return String.valueOf(
                currentUser.getBranchId()
        );
    }

    // =====================================================================
    // INPUT VALIDATION
    // =====================================================================

    private String requireJobId(
            String jobId
    ) {
        if (
                jobId == null
                        || jobId.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Import job ID is required."
            );
        }

        String normalizedJobId =
                jobId.trim();

        if (
                normalizedJobId.length() > 64
                        || !normalizedJobId.matches(
                        "[A-Za-z0-9-]+"
                )
        ) {
            throw new IllegalArgumentException(
                    "Import job ID is invalid."
            );
        }

        return normalizedJobId;
    }

    private String requireModuleName(
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

    /**
     * Retry information resolved entirely from trusted backend data.
     */
    public record RetryImportDetails(
            String originalJobId,
            String module,
            String branchId,
            Set<Integer> targetRowNumbers
    ) {
    }
}
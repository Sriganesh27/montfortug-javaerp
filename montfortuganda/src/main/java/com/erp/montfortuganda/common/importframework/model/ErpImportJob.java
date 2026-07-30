package com.erp.montfortuganda.common.importframework.model;

import com.erp.montfortuganda.common.importframework.lifecycle.ImportMode;
import com.erp.montfortuganda.common.importframework.lifecycle.ImportStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "erp_import_jobs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpImportJob {

    @Id
    @Column(
            name = "job_id",
            nullable = false,
            length = 64
    )
    private String jobId;

    @Column(
            name = "module",
            nullable = false,
            length = 50
    )
    private String module;

    @Column(
            name = "branch_id",
            nullable = false,
            length = 50
    )
    private String branchId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 50
    )
    private ImportStatus status;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "import_mode",
            nullable = false,
            length = 50
    )
    private ImportMode importMode;

    @Column(
            name = "file_hash",
            nullable = false,
            length = 128
    )
    private String fileHash;

    @Column(
            name = "uploaded_file_name",
            nullable = false,
            length = 255
    )
    private String uploadedFileName;

    /**
     * Total non-header rows selected for this import job.
     *
     * <p>For a normal import this is the number of non-blank workbook rows.
     * For RETRY_FAILED_ROWS this is the number of backend-authorized failed
     * physical rows selected from the original job.</p>
     */
    @Builder.Default
    @Column(
            name = "total_rows",
            nullable = false
    )
    private int totalRows = 0;

    @Builder.Default
    @Column(
            name = "processed_rows",
            nullable = false
    )
    private int processedRows = 0;

    @Builder.Default
    @Column(
            name = "success_rows",
            nullable = false
    )
    private int successRows = 0;

    @Builder.Default
    @Column(
            name = "failed_rows",
            nullable = false
    )
    private int failedRows = 0;

    @Column(
            name = "last_checkpoint",
            length = 500
    )
    private String lastCheckpoint;

    @Column(
            name = "started_at",
            nullable = false
    )
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
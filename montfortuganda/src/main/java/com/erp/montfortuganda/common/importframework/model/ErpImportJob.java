package com.erp.montfortuganda.common.importframework.model;

import com.erp.montfortuganda.common.importframework.lifecycle.ImportMode;
import com.erp.montfortuganda.common.importframework.lifecycle.ImportStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "erp_import_jobs")
@Getter    // Fix: Required to resolve getStatus() and getJobId()
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpImportJob {

    @Id
    private String jobId;
    private String module;
    private String branchId;

    @Enumerated(EnumType.STRING)
    private ImportStatus status;

    @Enumerated(EnumType.STRING)
    private ImportMode importMode;

    private String fileHash;
    private String uploadedFileName;
    private int processedRows;
    private int successRows;
    private int failedRows;

    @Column(length = 500)
    private String lastCheckpoint;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
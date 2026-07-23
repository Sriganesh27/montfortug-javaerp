package com.erp.montfortuganda.common.importframework.model;

import com.erp.montfortuganda.model.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "erp_import_errors",
        indexes = {
                @Index(
                        name = "idx_import_errors_job_id",
                        columnList = "job_id"
                ),
                @Index(
                        name = "idx_import_errors_job_row",
                        columnList = "job_id, excel_row_number"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErpImportError extends AuditableEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(
            name = "job_id",
            length = 36,
            nullable = false
    )
    private String jobId;

    @Column(
            name = "excel_row_number",
            nullable = false
    )
    private int rowNumber;

    @Column(
            name = "column_name",
            length = 100
    )
    private String columnName;

    @Column(
            name = "cell_value",
            length = 1000
    )
    private String cellValue;

    @Column(
            name = "error_code",
            length = 50,
            nullable = false
    )
    private String errorCode;

    @Column(
            name = "severity",
            length = 20
    )
    private String severity;

    @Column(
            name = "message",
            length = 1000,
            nullable = false
    )
    private String message;

    @Column(
            name = "suggested_fix",
            length = 1000
    )
    private String suggestedFix;
}
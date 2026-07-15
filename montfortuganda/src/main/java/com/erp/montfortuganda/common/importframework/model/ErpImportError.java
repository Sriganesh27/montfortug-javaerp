package com.erp.montfortuganda.common.importframework.model;

import com.erp.montfortuganda.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "erp_import_errors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErpImportError extends AuditableEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", length = 36, nullable = false)
    private String jobId;
    
    @Column(name = "row_number")
    private int rowNumber;

    @Column(name = "column_name", length = 100)
    private String columnName;
    
    @Column(name = "cell_value", length = 1000)
    private String cellValue;
    
    @Column(name = "error_code", length = 20, nullable = false)
    private String errorCode;
    
    @Column(name = "severity", length = 20)
    private String severity;

    @Column(name = "message", length = 1000, nullable = false)
    private String message;
    
    @Column(name = "suggested_fix", length = 1000)
    private String suggestedFix;
}

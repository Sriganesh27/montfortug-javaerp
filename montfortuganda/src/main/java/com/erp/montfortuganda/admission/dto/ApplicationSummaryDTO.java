package com.erp.montfortuganda.admission.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApplicationSummaryDTO {
    private Long applicationId;
    private String applicationNo;
    private String studentName;
    private String className;
    private String status;
    private LocalDateTime submittedDate;
}
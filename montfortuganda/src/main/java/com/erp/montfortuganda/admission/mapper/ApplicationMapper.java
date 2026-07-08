package com.erp.montfortuganda.admission.mapper;

import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.admission.dto.ApplicationSummaryDTO;
import org.springframework.stereotype.Component;

@Component
public class ApplicationMapper {

    public ApplicationSummaryDTO toSummaryDTO(ErpApplication app) {
        ApplicationSummaryDTO dto = new ApplicationSummaryDTO();
        dto.setApplicationId(app.getApplicationId());
        dto.setApplicationNo(app.getApplicationNo());
        dto.setStudentName(app.getFirstName() + " " + (app.getLastName() != null ? app.getLastName() : ""));
        dto.setClassName(app.getBranchClassId() != null ? "Class ID " + app.getBranchClassId() : "N/A");
        dto.setStatus(app.getApplicationStatus().name());
        dto.setSubmittedDate(app.getCreatedAt());
        return dto;
    }
}
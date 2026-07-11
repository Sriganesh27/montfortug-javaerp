package com.erp.montfortuganda.school.dto;

import com.erp.montfortuganda.model.enums.RecordStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DesignationDTO {
    private Long id;

    @NotBlank(message = "Designation Code is required")
    @Size(max = 20, message = "Code must be at most 20 characters")
    private String designationCode;

    @NotBlank(message = "Designation Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String designationName;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    private RecordStatus status = RecordStatus.ACTIVE;
    private Boolean active = true;
    private Long version = 0L;
}
package com.erp.montfortuganda.settings.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SiteSettingDTO {
    private Integer settingId;

    @NotBlank(message = "Setting Key is required")
    private String settingKey;

    @NotBlank(message = "Setting Value is required")
    private String settingValue;
}
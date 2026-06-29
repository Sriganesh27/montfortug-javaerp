package com.erp.montfortuganda.school.dto;

import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Data
public class BranchDTO {
    private Integer branchId;
    private String branchName;
    private String schoolCode;

    // NEW LISTS
    private List<Integer> levelIds = new ArrayList<>();
    private List<LevelDTO> levels = new ArrayList<>();

    private String branchLocation;
    private String contactDetails;
    private String inchargeDetails;
    private String schoolPhotoUrl;
    private String govDocumentUrl;
    private String foundationDate;
    private Integer isActive;
}
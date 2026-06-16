package com.erp.montfortuganda.school.dto;

import lombok.Data;

@Data
public class BranchDTO {
    private Integer branchId;
    private String branchName;
    private String schoolCode;
    private String branchType; // Will hold comma-separated string e.g. "Primary, Secondary"
    private String branchLocation;
    private String contactDetails;
    private String inchargeDetails;
    private String schoolPhotoUrl;
    private String govDocumentUrl;
    private String foundationDate;
    private Integer isActive;
}
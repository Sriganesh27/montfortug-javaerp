package com.erp.montfortuganda.branchadmin.dto;

import com.erp.montfortuganda.school.dto.LevelDTO;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class BranchProfileResponseDTO {

    private String username;
    private Boolean canEdit;

    private String branchName;
    private String schoolCode;
    private Integer isActive;
    private LocalDate foundationDate;

    private List<Integer> levelIds = new ArrayList<>();
    private List<LevelDTO> levels = new ArrayList<>();

    private String branchLocation;
    private String addressLine1;
    private String addressLine2;
    private String poBox;
    private String locality;
    private String city;
    private String district;
    private String region;
    private String country;
    private String postalCode;

    private String primaryPhone;
    private String secondaryPhone;
    private String whatsappPhone;
    private String branchEmail;
    private String emailFromName;
    private String emailReplyTo;

    private String inchargeDetails;

    private String bankName;
    private String bankAccountName;
    private String bankAccountNumber;
    private String bankBranch;
    private String airtelPayNumber;
    private String airtelPayName;
    private String qrCodeUrl;

    private String branchLogoUrl;
    private String schoolPhotoUrl;
    private String govDocumentUrl;
}

package com.erp.montfortuganda.school.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class BranchDTO {

    private Integer branchId;

    private String branchName;

    private String schoolCode;

    /*
     * Academic levels assigned through erp_branch_levels.
     */
    private List<Integer> levelIds =
            new ArrayList<>();

    private List<LevelDTO> levels =
            new ArrayList<>();

    /*
     * Short location used in tables and dropdowns.
     * Example: Mpala, Entebbe
     */
    private String branchLocation;

    /*
     * Structured branch address.
     */
    private String addressLine1;

    private String addressLine2;

    private String poBox;

    private String locality;

    private String city;

    private String district;

    private String region;

    private String country = "Uganda";

    private String postalCode;

    /*
     * Legacy combined contact value retained temporarily
     * for backward compatibility with older screens.
     */
    private String contactDetails;

    /*
     * Structured communication details.
     */
    private String primaryPhone;

    private String secondaryPhone;

    /*
     * Supported values:
     * NONE, PRIMARY, SECONDARY, BOTH
     */
    private String whatsappPhone = "NONE";

    /*
     * Public branch email identity.
     * SMTP credentials remain in environment variables.
     */
    private String branchEmail;

    private String emailFromName;

    private String emailReplyTo;

    private Boolean emailEnabled = true;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate foundationDate;

    /*
     * Stored relative file paths or secured access URLs.
     */
    private String govDocumentUrl;

    private String inchargeDetails;

    private String schoolPhotoUrl;

    private String branchLogoUrl;

    private Integer isActive = 1;
}
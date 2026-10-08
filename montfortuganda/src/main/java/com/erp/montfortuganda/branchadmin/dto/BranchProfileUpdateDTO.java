package com.erp.montfortuganda.branchadmin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class BranchProfileUpdateDTO {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate foundationDate;

    @Size(max = 255)
    private String branchLocation;

    @Size(max = 255)
    private String addressLine1;

    @Size(max = 255)
    private String addressLine2;

    @Size(max = 100)
    private String poBox;

    @Size(max = 150)
    private String locality;

    @Size(max = 150)
    private String city;

    @Size(max = 150)
    private String district;

    @Size(max = 150)
    private String region;

    @Size(max = 100)
    private String country;

    @Size(max = 30)
    private String postalCode;

    @Size(max = 50)
    private String primaryPhone;

    @Size(max = 50)
    private String secondaryPhone;

    @Size(max = 50)
    private String whatsappPhone;

    @Email
    @Size(max = 255)
    private String branchEmail;

    @Size(max = 255)
    private String emailFromName;

    @Email
    @Size(max = 255)
    private String emailReplyTo;

    @Size(max = 5000)
    private String inchargeDetails;

    @Size(max = 255)
    private String bankName;

    @Size(max = 255)
    private String bankAccountName;

    @Size(max = 100)
    private String bankAccountNumber;

    @Size(max = 255)
    private String bankBranch;

    @Size(max = 50)
    private String airtelPayNumber;

    @Size(max = 255)
    private String airtelPayName;
}

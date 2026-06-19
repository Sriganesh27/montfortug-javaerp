package com.erp.montfortuganda.scholarship.dto;

import lombok.Data;

@Data
public class ScholarshipActiveSponsorshipDTO {
    private Integer id;
    private String sponsorName;
    private String sponsorId;
    private String studentName;
    private String studentId;
    private String campus;
    private String status;
}

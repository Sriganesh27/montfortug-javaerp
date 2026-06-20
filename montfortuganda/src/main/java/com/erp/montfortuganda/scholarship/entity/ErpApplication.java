package com.erp.montfortuganda.scholarship.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "erp_applications")
public class ErpApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "app_id")
    private Long appId;

    @Column(name = "student_name", length = 100, nullable = false)
    private String studentName;

    @Column(name = "student_surname", length = 100, nullable = false)
    private String studentSurname;

    @Column(name = "gender", length = 10, nullable = false)
    private String gender;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "scholarship_status", length = 50)
    private String scholarshipStatus;
}
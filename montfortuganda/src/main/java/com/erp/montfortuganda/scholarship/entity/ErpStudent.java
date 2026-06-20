package com.erp.montfortuganda.scholarship.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "erp_students")
public class ErpStudent {

    @Id
    @Column(name = "StudentID", length = 50)
    private String studentId;

    @Column(name = "AdmissionNo")
    private Integer admissionNo;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "Name", length = 100)
    private String name;

    @Column(name = "Surname", length = 100)
    private String surname;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "Status", length = 50)
    private String status;
}
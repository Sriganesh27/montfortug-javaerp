package com.montfort.erp.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "erp_branches")
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "branch_id")
    private Integer branchId;

    @Column(name = "branch_name", nullable = false)
    private String branchName;

    @Column(name = "school_code", length = 10)
    private String schoolCode;

    @Column(name = "branch_type", length = 50)
    private String branchType;

    @Column(name = "branch_location")
    private String branchLocation;
}

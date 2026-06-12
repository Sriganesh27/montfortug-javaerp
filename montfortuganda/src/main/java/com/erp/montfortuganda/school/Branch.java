package com.erp.montfortuganda.school;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "erp_branches")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
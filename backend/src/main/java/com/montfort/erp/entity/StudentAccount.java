package com.montfort.erp.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "erp_student_accounts")
@Data
@NoArgsConstructor
public class StudentAccount {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;
    
    @Column(name = "student_id")
    private Long studentId;
    
    @Column(name = "branch_id")
    private Long branchId;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String role;
    
    @Column(name = "is_active")
    private Integer isActive;
}

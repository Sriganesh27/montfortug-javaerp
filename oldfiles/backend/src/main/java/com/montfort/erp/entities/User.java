package com.montfort.erp.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "erp_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role", length = 50)
    private String role;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_branch", referencedColumnName = "branch_id")
    private Branch assignedBranch;

    @Column(name = "is_active")
    private Integer isActive = 1;
}

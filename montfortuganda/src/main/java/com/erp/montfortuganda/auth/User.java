package com.erp.montfortuganda.auth;

import com.erp.montfortuganda.school.Branch;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "erp_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @JsonIgnore // CRITICAL: Never send the password to the frontend!
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role", length = 50)
    private String role;

    // 🚨 CRITICAL: Stops the LazyInitializationException crash!
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_branch", referencedColumnName = "branch_id")
    private Branch assignedBranch;

    @Column(name = "is_active", columnDefinition = "integer default 1")
    private Integer isActive = 1;
}
package com.erp.montfortuganda.auth;

import com.erp.montfortuganda.model.AuditableEntity;
import com.erp.montfortuganda.school.Branch;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "erp_login_history")
@EqualsAndHashCode(callSuper = true, exclude = {"user", "branch"})
@ToString(callSuper = true, exclude = {"user", "branch"})
public class ErpLoginHistory extends AuditableEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "login_history_id")
    private Long loginHistoryId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_loginhistory_user")
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "branch_id",
            referencedColumnName = "branch_id",
            foreignKey = @ForeignKey(name = "fk_loginhistory_branch")
    )
    private Branch branch;

    @Column(name = "login_time", nullable = false)
    private LocalDateTime loginTime;

    @Column(name = "logout_time")
    private LocalDateTime logoutTime;

    @Size(max = 100)
    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    @Size(max = 255)
    @Column(name = "device_name", length = 255)
    private String deviceName;

    @Size(max = 255)
    @Column(name = "browser_name", length = 255)
    private String browserName;

    /**
     * SUCCESS, FAILED, LOCKED, LOGOUT
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "login_status", nullable = false, length = 20)
    private LoginStatus loginStatus = LoginStatus.SUCCESS;

    @Size(max = 500)
    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @PrePersist
    private void onCreate() {
        if (active == null) {
            active = true;
        }

        if (loginStatus == null) {
            loginStatus = LoginStatus.SUCCESS;
        }

        if (loginTime == null) {
            loginTime = LocalDateTime.now();
        }
    }
}
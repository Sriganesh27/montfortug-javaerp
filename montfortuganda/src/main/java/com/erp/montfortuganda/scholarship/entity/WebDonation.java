package com.erp.montfortuganda.scholarship.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "web_donations")
public class WebDonation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "receipt_number", length = 20)
    private String receiptNumber;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "contact_number", nullable = false, length = 50)
    private String contactNumber;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "is_anonymous")
    private Boolean isAnonymous = false;

    @Column(name = "contribution_purpose", nullable = false, length = 100)
    private String contributionPurpose;

    @Column(name = "project_id", length = 20)
    private String projectId;

    @Column(name = "currency", nullable = false, length = 20)
    private String currency;

    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "amount_received", precision = 15, scale = 2)
    private BigDecimal amountReceived;

    @Column(name = "amount_spent", precision = 15, scale = 2)
    private BigDecimal amountSpent = BigDecimal.ZERO;

    @Column(name = "payment_status", nullable = false)
    private String paymentStatus;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "students_benefited")
    private Integer studentsBenefited = 0;

    @Column(name = "terms_benefited")
    private Integer termsBenefited = 0;
}
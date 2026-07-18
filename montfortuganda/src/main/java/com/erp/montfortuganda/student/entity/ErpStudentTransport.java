package com.erp.montfortuganda.student.entity;

import com.erp.montfortuganda.school.entity.Branch;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Enterprise Student Transport Entity.
 * Tracks a student's enrollment in the transport system for a given academic year.
 */
@Data
@Entity
@DynamicUpdate
@Table(name = "erp_student_transport",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_student_transport",
                        columnNames = {"student_id", "academic_year"}
                )
        },
        indexes = {
                @Index(name = "idx_transport_student", columnList = "student_id"),
                @Index(name = "idx_transport_branch", columnList = "branch_id"),
                @Index(name = "idx_transport_route", columnList = "route_id"),
                @Index(name = "idx_transport_vehicle", columnList = "vehicle_id"),
                @Index(name = "idx_transport_pickup", columnList = "pickup_point_id"),
                @Index(name = "idx_transport_status", columnList = "transport_status"),
                @Index(name = "idx_transport_payment", columnList = "payment_status"),
                @Index(name = "idx_transport_year", columnList = "academic_year"),
                @Index(name = "idx_transport_admission", columnList = "admission_no")
        }
)
@EqualsAndHashCode(exclude = {"student", "branch"})
@ToString(exclude = {"student", "branch"})
public class ErpStudentTransport implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ==========================================
    // ENUMS
    // ==========================================
    public enum TransportStatus {
        ACTIVE, INACTIVE, SUSPENDED, COMPLETED, CANCELLED
    }

    public enum PaymentStatus {
        PENDING, PARTIAL, PAID
    }

    // ==========================================
    // PRIMARY KEY
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transport_id")
    private Long transportId;

    // ==========================================
    // STUDENT REFERENCES
    // ==========================================
    @NotNull(message = "Student is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private ErpStudent student;

    @NotNull(message = "Branch is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @NotBlank(message = "Admission number is required")
    @Size(max = 50, message = "Admission number cannot exceed 50 characters")
    @Column(name = "admission_no", nullable = false, length = 50)
    private String admissionNo;

    @NotBlank(message = "Academic year is required")
    @Size(max = 20, message = "Academic year cannot exceed 20 characters")
    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    // ==========================================
    // TRANSPORT MASTER
    // Note: Mapped as Longs until Transport entities are created.
    // ==========================================
    @NotNull(message = "Route ID is required")
    @Column(name = "route_id", nullable = false)
    private Long routeId;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "pickup_point_id")
    private Long pickupPointId;

    // ==========================================
    // ENROLLMENT
    // ==========================================
    @NotNull(message = "Transport start date is required")
    @Column(name = "transport_start_date", nullable = false)
    private LocalDate transportStartDate;

    @Column(name = "transport_end_date")
    private LocalDate transportEndDate;

    // ==========================================
    // FEES
    // ==========================================
    @NotNull(message = "Monthly fee is required")
    @DecimalMin(value = "0.00", message = "Monthly fee cannot be negative")
    @Column(name = "monthly_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyFee = BigDecimal.ZERO;

    @NotNull(message = "Annual fee is required")
    @DecimalMin(value = "0.00", message = "Annual fee cannot be negative")
    @Column(name = "annual_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal annualFee = BigDecimal.ZERO;

    @NotNull(message = "Discount amount is required")
    @DecimalMin(value = "0.00", message = "Discount amount cannot be negative")
    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @NotNull(message = "Payable amount is required")
    @DecimalMin(value = "0.00", message = "Payable amount cannot be negative")
    @Column(name = "payable_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal payableAmount = BigDecimal.ZERO;

    // ==========================================
    // STATUS
    // ==========================================
    @NotNull(message = "Transport status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "transport_status", nullable = false, length = 20)
    private TransportStatus transportStatus = TransportStatus.ACTIVE;

    @NotNull(message = "Payment status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    // ==========================================
    // OPTIONAL DETAILS
    // ==========================================
    @Size(max = 20)
    @Column(name = "seat_number", length = 20)
    private String seatNumber;

    @Size(max = 100)
    @Column(name = "emergency_contact", length = 100)
    private String emergencyContact;

    @Pattern(regexp = "^[0-9+\\- ]{7,20}$", message = "Invalid mobile number format")
    @Size(max = 20)
    @Column(name = "emergency_mobile", length = 20)
    private String emergencyMobile;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    @Column(name = "remarks", length = 500)
    private String remarks;

    @NotNull(message = "Active status is required")
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    // ==========================================
    // AUDIT & LOCKING
    // ==========================================
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==========================================
    // JPA LIFECYCLE CALLBACKS
    // ==========================================
    @PrePersist
    protected void onCreate() {
        if (transportStatus == null) transportStatus = TransportStatus.ACTIVE;
        if (paymentStatus == null) paymentStatus = PaymentStatus.PENDING;
        if (active == null) active = true;
        if (version == null) version = 0L;

        if (transportStartDate == null) {
            transportStartDate = LocalDate.now();
        }

        recalculateAmounts();
        validateDates();

        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        recalculateAmounts();
        validateDates();
        updatedAt = LocalDateTime.now();
    }

    // ==========================================
    // HELPER LOGIC
    // ==========================================
    private void recalculateAmounts() {
        if (monthlyFee == null) monthlyFee = BigDecimal.ZERO;
        if (annualFee == null) annualFee = BigDecimal.ZERO;
        if (discountAmount == null) discountAmount = BigDecimal.ZERO;

        if (monthlyFee.compareTo(BigDecimal.ZERO) < 0) monthlyFee = BigDecimal.ZERO;
        if (annualFee.compareTo(BigDecimal.ZERO) < 0) annualFee = BigDecimal.ZERO;
        if (discountAmount.compareTo(BigDecimal.ZERO) < 0) discountAmount = BigDecimal.ZERO;

        payableAmount = annualFee.subtract(discountAmount);

        if (payableAmount.signum() < 0) {
            payableAmount = BigDecimal.ZERO;
        }
    }

    private void validateDates() {
        if (transportEndDate != null && transportEndDate.isBefore(transportStartDate)) {
            throw new IllegalArgumentException("Transport end date cannot be before start date");
        }
    }
}
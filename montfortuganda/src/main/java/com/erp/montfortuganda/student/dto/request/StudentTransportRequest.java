package com.erp.montfortuganda.student.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Optional transport-allocation information supplied during
 * Student registration or profile update.
 *
 * Academic year, fees, transport status, payment status,
 * branch ownership and audit fields are controlled by the backend.
 */
public record StudentTransportRequest(

        @NotNull(
                message = "Transport route is required."
        )
        @Positive(
                message = "Route ID must be greater than zero."
        )
        Long routeId,

        @Positive(
                message = "Vehicle ID must be greater than zero."
        )
        Long vehicleId,

        @Positive(
                message = "Pickup point ID must be greater than zero."
        )
        Long pickupPointId,

        @NotNull(
                message = "Transport start date is required."
        )
        LocalDate transportStartDate,

        LocalDate transportEndDate,

        @Size(
                max = 20,
                message = "Seat number cannot exceed 20 characters."
        )
        String seatNumber,

        @Size(
                max = 100,
                message = "Transport emergency contact cannot exceed 100 characters."
        )
        String emergencyContact,

        @Size(
                max = 20,
                message = "Transport emergency mobile cannot exceed 20 characters."
        )
        String emergencyMobile,

        @Size(
                max = 500,
                message = "Transport remarks cannot exceed 500 characters."
        )
        String remarks

) {

    /**
     * Transport end date cannot be earlier than its start date.
     */
    @AssertTrue(
            message = "Transport end date cannot be earlier than the start date."
    )
    public boolean isTransportDateRangeValid() {
        return transportStartDate == null
                || transportEndDate == null
                || !transportEndDate.isBefore(
                transportStartDate
        );
    }

    /**
     * Emergency contact details are optional, but the name and mobile
     * number must be supplied together.
     */
    @AssertTrue(
            message = "Transport emergency contact and mobile must be entered together."
    )
    public boolean isEmergencyContactComplete() {
        boolean hasContact =
                hasText(emergencyContact);

        boolean hasMobile =
                hasText(emergencyMobile);

        return hasContact == hasMobile;
    }

    /**
     * A pickup point must belong to a selected transport route.
     * Route is already mandatory, so this protects future partial DTO use.
     */
    @AssertTrue(
            message = "Transport route is required when a pickup point is selected."
    )
    public boolean isPickupPointSelectionValid() {
        return pickupPointId == null
                || routeId != null;
    }

    private static boolean hasText(
            String value
    ) {
        return value != null
                && !value.isBlank();
    }
}
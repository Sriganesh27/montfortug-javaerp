package com.erp.montfortuganda.student.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Optional hostel-allocation information supplied during
 * Student registration or profile update.
 *
 * Academic year, fees, allocation status, payment status,
 * audit fields and branch ownership are controlled by the backend.
 */
public record StudentHostelRequest(

        @NotNull(
                message = "Hostel is required."
        )
        @Positive(
                message = "Hostel ID must be greater than zero."
        )
        Long hostelId,

        @Positive(
                message = "Room ID must be greater than zero."
        )
        Long roomId,

        @Positive(
                message = "Bed ID must be greater than zero."
        )
        Long bedId,

        @NotNull(
                message = "Hostel allocation start date is required."
        )
        LocalDate allocationStartDate,

        LocalDate allocationEndDate,

        @Size(
                max = 150,
                message = "Local guardian name cannot exceed 150 characters."
        )
        String localGuardianName,

        @Size(
                max = 20,
                message = "Local guardian mobile cannot exceed 20 characters."
        )
        String localGuardianMobile,

        @Size(
                max = 50,
                message = "Local guardian relationship cannot exceed 50 characters."
        )
        String localGuardianRelation,

        @Size(
                max = 500,
                message = "Hostel remarks cannot exceed 500 characters."
        )
        String remarks

) {

    /**
     * A bed cannot be selected without selecting its room.
     */
    @AssertTrue(
            message = "Room is required when a hostel bed is selected."
    )
    public boolean isBedSelectionValid() {
        return bedId == null || roomId != null;
    }

    /**
     * Allocation end date cannot be earlier than start date.
     */
    @AssertTrue(
            message = "Hostel allocation end date cannot be earlier than the start date."
    )
    public boolean isAllocationDateRangeValid() {
        return allocationStartDate == null
                || allocationEndDate == null
                || !allocationEndDate.isBefore(
                allocationStartDate
        );
    }

    /**
     * Local guardian information is optional. When one field is supplied,
     * all three fields must be supplied.
     */
    @AssertTrue(
            message = "Local guardian name, mobile and relationship must be entered together."
    )
    public boolean isLocalGuardianComplete() {
        boolean hasName =
                hasText(localGuardianName);

        boolean hasMobile =
                hasText(localGuardianMobile);

        boolean hasRelation =
                hasText(localGuardianRelation);

        boolean noneEntered =
                !hasName
                        && !hasMobile
                        && !hasRelation;

        boolean allEntered =
                hasName
                        && hasMobile
                        && hasRelation;

        return noneEntered || allEntered;
    }

    private static boolean hasText(
            String value
    ) {
        return value != null
                && !value.isBlank();
    }
}
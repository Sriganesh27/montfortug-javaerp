package com.erp.montfortuganda.student.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Student identity, personal information and residential address.
 *
 * The physical photo_path value must never be returned to the frontend.
 * photoUrl should point to a protected Student photo endpoint.
 */
public record StudentPersonalResponse(

        Long studentId,

        Long applicationId,

        Integer branchId,

        String branchCode,

        String branchName,

        String admissionNo,

        String studentCode,

        String learnerLin,

        Integer admissionYear,

        String firstName,

        String middleName,

        String lastName,

        String fullName,

        String gender,

        LocalDate dateOfBirth,

        String nationality,

        String houseNo,

        String street,

        String village,

        String townCity,

        String district,

        String state,

        String country,

        String postalCode,

        /**
         * Protected API URL, for example:
         * /api/students/{studentId}/photo
         */
        String photoUrl,

        String studentStatus,

        Boolean active,

        Long version,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}
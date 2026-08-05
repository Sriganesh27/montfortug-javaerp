package com.erp.montfortuganda.student.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Student identity, personal information and residential address.
 *
 * <p>The physical photo_path value must never be returned to the frontend.
 * photoUrl points to the protected Student photo endpoint.</p>
 */
public record StudentPersonalResponse(

        Long studentId,

        Long applicationId,

        Integer branchId,

        String branchCode,

        String branchName,

        String admissionNo,

        String learnerLin,

        Integer admissionYear,

        Integer joiningClassId,

        Long joiningTermId,

        String firstName,

        String middleName,

        String lastName,

        String fullName,

        String gender,

        LocalDate dateOfBirth,

        String nationality,

        String nationalIdPassport,

        String houseNo,

        String street,

        String village,

        String townCity,

        String district,

        String county,

        String subCounty,

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
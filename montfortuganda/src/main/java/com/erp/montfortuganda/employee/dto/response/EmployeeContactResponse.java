package com.erp.montfortuganda.employee.dto.response;

import com.erp.montfortuganda.employee.enums.ContactRelationship;
import com.erp.montfortuganda.employee.enums.ContactType;

import java.time.LocalDateTime;

/**
 * Complete Employee contact data returned by the authenticated, branch-scoped
 * Employee APIs.

 * The version value must be returned to the frontend so later contact updates
 * can use optimistic locking safely.
 */
@SuppressWarnings("unused")
public record EmployeeContactResponse(

        Long employeeContactId,

        Long employeeId,

        String employeeContactName,

        ContactRelationship employeeContactRelationship,

        ContactType employeeContactType,

        String employeeContactMobile,

        String employeeContactAlternateMobile,

        String employeeContactEmail,

        String employeeContactCountry,

        String employeeContactState,

        String employeeContactDistrict,

        String employeeContactVillage,

        String employeeContactStreet,

        String employeeContactPostalCode,

        String employeeContactOccupation,

        String employeeContactWorkplace,

        Boolean employeeContactIsPrimary,

        Boolean employeeContactIsEmergency,

        Boolean employeeContactActive,

        String employeeContactRemarks,

        Long version,

        String createdBy,

        LocalDateTime createdAt,

        String updatedBy,

        LocalDateTime updatedAt
) {
}
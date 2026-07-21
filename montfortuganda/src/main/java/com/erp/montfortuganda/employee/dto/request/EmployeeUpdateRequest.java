package com.erp.montfortuganda.employee.dto.request;

import com.erp.montfortuganda.employee.enums.EmployeeCategory;
import com.erp.montfortuganda.employee.enums.EmployeeType;
import com.erp.montfortuganda.employee.enums.EmploymentMode;
import com.erp.montfortuganda.employee.enums.EmploymentStatus;
import com.erp.montfortuganda.employee.enums.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * Complete, branch-scoped Employee update request.

 * * The Employee ID is supplied in the URL. Branch ownership is resolved from
 * the authenticated user. Employee number, full name, login-enabled state,
 * active state, employment-end date, stored file paths, audit fields and
 * entity version changes are controlled by the backend.

 * The submitted version is used only as an optimistic-locking token to prevent
 * one editor from silently overwriting another editor's changes.
 */
@SuppressWarnings("unused")
public record EmployeeUpdateRequest(

        @NotNull(message = "Employee version is required.")
        @PositiveOrZero(message = "Employee version cannot be negative.")
        Long version,

        @Size(
                max = 20,
                message = "Employee title cannot exceed 20 characters."
        )
        String title,

        @NotBlank(message = "Employee first name is required.")
        @Size(
                max = 100,
                message = "Employee first name cannot exceed 100 characters."
        )
        String firstName,

        @Size(
                max = 100,
                message = "Employee middle name cannot exceed 100 characters."
        )
        String middleName,

        @NotBlank(message = "Employee last name is required.")
        @Size(
                max = 100,
                message = "Employee last name cannot exceed 100 characters."
        )
        String lastName,

        @NotNull(message = "Employee gender is required.")
        Gender gender,

        @NotNull(message = "Employee date of birth is required.")
        @Past(message = "Employee date of birth must be in the past.")
        LocalDate dateOfBirth,

        @Size(
                max = 50,
                message = "Marital status cannot exceed 50 characters."
        )
        String maritalStatus,

        @Size(
                max = 20,
                message = "Blood group cannot exceed 20 characters."
        )
        String bloodGroup,

        @Size(
                max = 100,
                message = "Religion cannot exceed 100 characters."
        )
        String religion,

        @Size(
                max = 100,
                message = "Sub-religion cannot exceed 100 characters."
        )
        String subReligion,

        String profilePhotoData,

        @Size(
                max = 255,
                message = "Employee profile-photo file name cannot exceed 255 characters."
        )
        String profilePhotoFileName,

        @Size(
                max = 100,
                message = "Employee profile-photo content type cannot exceed 100 characters."
        )
        String profilePhotoContentType,

        @Positive(message = "Employee profile-photo file size must be greater than zero.")
        Long profilePhotoFileSize,

        String signatureFileData,

        @Size(
                max = 255,
                message = "Employee signature file name cannot exceed 255 characters."
        )
        String signatureFileName,

        @Size(
                max = 100,
                message = "Employee signature content type cannot exceed 100 characters."
        )
        String signatureContentType,

        @Positive(message = "Employee signature file size must be greater than zero.")
        Long signatureFileSize,

        @Email(message = "Official email must be valid.")
        @Size(
                max = 150,
                message = "Official email cannot exceed 150 characters."
        )
        String officialEmail,

        @Email(message = "Personal email must be valid.")
        @Size(
                max = 150,
                message = "Personal email cannot exceed 150 characters."
        )
        String personalEmail,

        @NotBlank(message = "Employee mobile number is required.")
        @Size(
                max = 30,
                message = "Employee mobile number cannot exceed 30 characters."
        )
        String mobileNo,

        @Size(
                max = 30,
                message = "Employee alternate mobile number cannot exceed 30 characters."
        )
        String alternateMobile,

        @NotNull(message = "Employee department is required.")
        @Positive(message = "Employee department ID must be greater than zero.")
        Long departmentId,

        @NotNull(message = "Employee designation is required.")
        @Positive(message = "Employee designation ID must be greater than zero.")
        Long designationId,

        @Positive(message = "Reporting-manager Employee ID must be greater than zero.")
        Long reportingManagerId,

        @NotNull(message = "Employee category is required.")
        EmployeeCategory employeeCategory,

        @NotNull(message = "Employee type is required.")
        EmployeeType employeeType,

        @NotNull(message = "Employment mode is required.")
        EmploymentMode employmentMode,

        @NotNull(message = "Employment status is required.")
        EmploymentStatus employmentStatus,

        @NotNull(message = "Employee joining date is required.")
        LocalDate joiningDate,

        LocalDate probationEndDate,

        LocalDate confirmationDate,

        LocalDate retirementDate,

        LocalDate resignationDate,

        LocalDate terminationDate,

        @Size(
                max = 10000,
                message = "Employee exit reason cannot exceed 10000 characters."
        )
        String exitReason,

        @Size(
                max = 100,
                message = "Nationality cannot exceed 100 characters."
        )
        String nationality,

        @Size(
                max = 100,
                message = "National ID cannot exceed 100 characters."
        )
        String nationalId,

        @Size(
                max = 100,
                message = "TIN number cannot exceed 100 characters."
        )
        String tinNumber,

        @Size(
                max = 100,
                message = "Passport number cannot exceed 100 characters."
        )
        String passportNo,

        LocalDate passportExpiryDate,

        @Size(
                max = 100,
                message = "Work-permit number cannot exceed 100 characters."
        )
        String workPermitNumber,

        LocalDate workPermitExpiryDate,

        @Size(
                max = 100,
                message = "Address country cannot exceed 100 characters."
        )
        String addressCountry,

        @Size(
                max = 100,
                message = "Address state cannot exceed 100 characters."
        )
        String addressState,

        @Size(
                max = 100,
                message = "Address district cannot exceed 100 characters."
        )
        String addressDistrict,

        @Size(
                max = 100,
                message = "Address county cannot exceed 100 characters."
        )
        String addressCounty,

        @Size(
                max = 100,
                message = "Address sub-county cannot exceed 100 characters."
        )
        String addressSubCounty,

        @Size(
                max = 100,
                message = "Address parish cannot exceed 100 characters."
        )
        String addressParish,

        @Size(
                max = 150,
                message = "Address village cannot exceed 150 characters."
        )
        String addressVillage,

        @Size(
                max = 255,
                message = "Address street cannot exceed 255 characters."
        )
        String addressStreet,

        @Size(
                max = 30,
                message = "Postal code cannot exceed 30 characters."
        )
        String postalCode,

        @Size(
                max = 10000,
                message = "Employee skills cannot exceed 10000 characters."
        )
        String skills,

        @Size(
                max = 10000,
                message = "Languages spoken cannot exceed 10000 characters."
        )
        String languagesSpoken,

        @Size(
                max = 10000,
                message = "Employee remarks cannot exceed 10000 characters."
        )
        String employeeRemarks,

        @NotNull(message = "Employee contacts collection is required.")
        @Size(
                max = 10,
                message = "An Employee cannot have more than 10 contacts."
        )
        @Valid
        List<EmployeeContactRequest> contacts,

        @NotNull(message = "Employee qualifications collection is required.")
        @Size(
                max = 25,
                message = "An Employee cannot have more than 25 qualifications."
        )
        @Valid
        List<EmployeeQualificationRequest> qualifications,

        @NotNull(message = "Employee experiences collection is required.")
        @Size(
                max = 25,
                message = "An Employee cannot have more than 25 experience records."
        )
        @Valid
        List<EmployeeExperienceRequest> experiences,

        @NotNull(message = "Employee documents collection is required.")
        @Size(
                max = 50,
                message = "An Employee cannot have more than 50 documents."
        )
        @Valid
        List<EmployeeDocumentRequest> documents
) {

    @AssertTrue(
            message = "Employee must be at least 18 years old on the joining date."
    )
    public boolean isMinimumJoiningAgeValid() {
        return dateOfBirth == null
                || joiningDate == null
                || !dateOfBirth.plusYears(18).isAfter(
                joiningDate
        );
    }

    @AssertTrue(
            message = "Employee date of birth must be earlier than the joining date."
    )
    public boolean isBirthAndJoiningDateValid() {
        return dateOfBirth == null
                || joiningDate == null
                || dateOfBirth.isBefore(joiningDate);
    }

    @AssertTrue(
            message = "Probation end date cannot be earlier than the joining date."
    )
    public boolean isProbationDateValid() {
        return joiningDate == null
                || probationEndDate == null
                || !probationEndDate.isBefore(joiningDate);
    }

    @AssertTrue(
            message = "Confirmation date cannot be earlier than the joining date."
    )
    public boolean isConfirmationDateValid() {
        return joiningDate == null
                || confirmationDate == null
                || !confirmationDate.isBefore(joiningDate);
    }

    @AssertTrue(
            message = "Retirement date cannot be earlier than the joining date."
    )
    public boolean isRetirementDateValid() {
        return joiningDate == null
                || retirementDate == null
                || !retirementDate.isBefore(joiningDate);
    }

    @AssertTrue(
            message = "Resignation date cannot be earlier than the joining date."
    )
    public boolean isResignationDateValid() {
        return joiningDate == null
                || resignationDate == null
                || !resignationDate.isBefore(joiningDate);
    }

    @AssertTrue(
            message = "Termination date cannot be earlier than the joining date."
    )
    public boolean isTerminationDateValid() {
        return joiningDate == null
                || terminationDate == null
                || !terminationDate.isBefore(joiningDate);
    }

    @AssertTrue(
            message = "Resignation date is required only when employment status is RESIGNED."
    )
    public boolean isResignationStatusValid() {
        return employmentStatus == null
                || (
                employmentStatus == EmploymentStatus.RESIGNED
                        ? resignationDate != null
                          && terminationDate == null
                        : resignationDate == null
        );
    }

    @AssertTrue(
            message = "Termination date is required only when employment status is TERMINATED."
    )
    public boolean isTerminationStatusValid() {
        return employmentStatus == null
                || (
                employmentStatus == EmploymentStatus.TERMINATED
                        ? terminationDate != null
                          && resignationDate == null
                        : terminationDate == null
        );
    }

    @AssertTrue(
            message = "Retirement date is required when employment status is RETIRED."
    )
    public boolean isRetirementStatusValid() {
        return employmentStatus != EmploymentStatus.RETIRED
                || retirementDate != null;
    }

    @AssertTrue(
            message = "Exit reason is required for resigned or terminated Employees."
    )
    public boolean isExitReasonValid() {
        return employmentStatus == null
                || (
                employmentStatus != EmploymentStatus.RESIGNED
                        && employmentStatus != EmploymentStatus.TERMINATED
        )
                || hasText(exitReason);
    }

    @AssertTrue(
            message = "Employee profile-photo file data and metadata must be supplied together."
    )
    public boolean isProfilePhotoMetadataValid() {
        return isCompleteOptionalFile(
                profilePhotoData,
                profilePhotoFileName,
                profilePhotoContentType,
                profilePhotoFileSize
        );
    }

    @AssertTrue(
            message = "Employee signature file data and metadata must be supplied together."
    )
    public boolean isSignatureMetadataValid() {
        return isCompleteOptionalFile(
                signatureFileData,
                signatureFileName,
                signatureContentType,
                signatureFileSize
        );
    }

    @AssertTrue(
            message = "Passport expiry date requires a passport number."
    )
    public boolean isPassportDataValid() {
        return passportExpiryDate == null
                || hasText(passportNo);
    }

    @AssertTrue(
            message = "Work-permit expiry date requires a work-permit number."
    )
    public boolean isWorkPermitDataValid() {
        return workPermitExpiryDate == null
                || hasText(workPermitNumber);
    }

    private boolean isCompleteOptionalFile(
            String fileData,
            String fileName,
            String contentType,
            Long fileSize
    ) {
        boolean anyValueSupplied =
                hasText(fileData)
                        || hasText(fileName)
                        || hasText(contentType)
                        || fileSize != null;

        return !anyValueSupplied
                || (
                hasText(fileData)
                        && hasText(fileName)
                        && hasText(contentType)
                        && fileSize != null
        );
    }

    private boolean hasText(
            String value
    ) {
        return value != null
                && !value.isBlank();
    }
}
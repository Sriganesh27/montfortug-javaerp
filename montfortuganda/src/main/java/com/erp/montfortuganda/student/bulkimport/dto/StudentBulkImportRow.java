package com.erp.montfortuganda.student.bulkimport.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Raw values read from one row of the Student Excel workbook.
 *
 * All Excel values remain Strings at this stage.
 *
 * Dates, numbers, Yes/No values, enums and database references are
 * converted only after validation. This prevents invalid Excel text such
 * as "ENTER VALID DATA" from reaching database columns.
 */
@Getter
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class StudentBulkImportRow {

    /**
     * Actual one-based Excel row number.
     *
     * The header is normally row 1, so Student data normally begins
     * from row 2.
     */
    @ToString.Include
    private final int excelRowNumber;

    // =====================================================================
    // STUDENT IDENTIFICATION AND PERSONAL INFORMATION
    // =====================================================================

    /**
     * Optional legacy Admission Number supplied in the workbook.
     *
     * The final validator and processor will decide whether this value is
     * accepted or whether StudentNumberService generates a new number.
     */
    private final String admissionNo;

    private final String admissionYear;

    private final String firstName;

    private final String middleName;

    private final String lastName;

    private final String gender;

    /**
     * Kept as text until successfully parsed and validated.
     */
    private final String dateOfBirth;

    // =====================================================================
    // BRANCH AND CURRENT ENROLLMENT
    // =====================================================================

    /**
     * Used only to confirm that the row belongs to the authenticated branch.
     *
     * The importer must never use this value to choose or change the
     * authenticated branch.
     */
    private final String branch;

    private final String educationLevel;

    private final String className;

    private final String section;

    private final String academicYear;

    private final String admissionType;

    /**
     * Kept as text until successfully parsed and validated.
     */
    private final String joiningDate;

    // =====================================================================
    // PARENT / GUARDIAN CONTACT
    // =====================================================================

    private final String fatherOrGuardianName;

    private final String motherOrGuardianName;

    private final String guardianRelationship;

    private final String mobileNumber;

    private final String alternateMobile;

    private final String email;

    private final String preferredContact;

    private final String feeResponsibility;

    private final String parentsLivingTogether;

    // =====================================================================
    // NATIONALITY AND ADDRESS
    // =====================================================================

    private final String nationality;

    private final String nationalIdOrPassport;

    private final String addressCountry;

    private final String state;

    private final String district;

    private final String county;

    private final String subCounty;

    private final String parish;

    private final String village;

    private final String street;

    // =====================================================================
    // PREVIOUS EDUCATION, RELIGION AND MEDICAL INFORMATION
    // =====================================================================

    private final String previousSchool;

    private final String religion;

    private final String bloodGroup;

    private final String transportRequired;

    private final String hostelRequired;

    private final String scholarship;

    private final String medicalConditions;

    private final String remarks;

    /**
     * Returns true when the Excel row contains no Student information.
     *
     * Completely blank rows must be ignored instead of being reported as
     * failed Student records.
     */
    public boolean isBlank() {
        return isBlankValue(admissionNo)
                && isBlankValue(admissionYear)
                && isBlankValue(firstName)
                && isBlankValue(middleName)
                && isBlankValue(lastName)
                && isBlankValue(gender)
                && isBlankValue(dateOfBirth)
                && isBlankValue(branch)
                && isBlankValue(educationLevel)
                && isBlankValue(className)
                && isBlankValue(section)
                && isBlankValue(academicYear)
                && isBlankValue(admissionType)
                && isBlankValue(joiningDate)
                && isBlankValue(fatherOrGuardianName)
                && isBlankValue(motherOrGuardianName)
                && isBlankValue(guardianRelationship)
                && isBlankValue(mobileNumber)
                && isBlankValue(alternateMobile)
                && isBlankValue(email)
                && isBlankValue(preferredContact)
                && isBlankValue(feeResponsibility)
                && isBlankValue(parentsLivingTogether)
                && isBlankValue(nationality)
                && isBlankValue(nationalIdOrPassport)
                && isBlankValue(addressCountry)
                && isBlankValue(state)
                && isBlankValue(district)
                && isBlankValue(county)
                && isBlankValue(subCounty)
                && isBlankValue(parish)
                && isBlankValue(village)
                && isBlankValue(street)
                && isBlankValue(previousSchool)
                && isBlankValue(religion)
                && isBlankValue(bloodGroup)
                && isBlankValue(transportRequired)
                && isBlankValue(hostelRequired)
                && isBlankValue(scholarship)
                && isBlankValue(medicalConditions)
                && isBlankValue(remarks);
    }

    private boolean isBlankValue(
            String value
    ) {
        return value == null || value.isBlank();
    }
}
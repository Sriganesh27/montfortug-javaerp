package com.erp.montfortuganda.student.bulkimport.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Raw values read from one Student Excel row.
 *
 * <p>All physical workbook values remain Strings until validation and secure
 * request mapping. Branch ownership, permanent admission number, audit data
 * and database identifiers are never read from Excel.</p>
 */
@Getter
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class StudentBulkImportRow {

    @ToString.Include
    private final int excelRowNumber;

    // Original admission information.
    private final String admissionYear;
    private final String admissionDate;
    private final String joiningClass;
    private final String joinedTerm;

    // Personal information.
    private final String firstName;
    private final String middleName;
    private final String lastName;
    private final String gender;
    private final String dateOfBirth;

    // Present enrollment.
    private final String presentEducationLevel;
    private final String presentClass;
    private final String presentTerm;
    private final String section;
    private final String academicYear;

    // Backend-generated logical values.
    private final String admissionType;

    // Parent and guardian information.
    private final String fatherName;
    private final String motherName;
    private final String guardianName;
    private final String guardianRelationship;
    private final String presentResponsiblePerson;
    private final String mobileNumber;
    private final String alternateMobile;
    private final String email;
    private final String preferredContact;
    private final String feeResponsibility;
    private final String parentsLivingTogether;

    // Nationality and address.
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

    // Previous education, service and medical information.
    private final String previousSchool;
    private final String religion;
    private final String bloodGroup;
    private final String transportRequired;
    private final String hostelRequired;
    private final String scholarship;
    private final String medicalConditions;
    private final String remarks;

    /**
     * Returns true only when all physical Excel cells are blank.
     *
     * <p>Backend defaults such as Admission Type and Preferred Contact are
     * intentionally excluded, otherwise a completely blank Excel row would
     * never be skipped.</p>
     */
    public boolean isBlank() {
        return isBlankValue(admissionYear)
                && isBlankValue(admissionDate)
                && isBlankValue(joiningClass)
                && isBlankValue(joinedTerm)
                && isBlankValue(firstName)
                && isBlankValue(middleName)
                && isBlankValue(lastName)
                && isBlankValue(gender)
                && isBlankValue(dateOfBirth)
                && isBlankValue(presentEducationLevel)
                && isBlankValue(presentClass)
                && isBlankValue(presentTerm)
                && isBlankValue(section)
                && isBlankValue(academicYear)
                && isBlankValue(fatherName)
                && isBlankValue(motherName)
                && isBlankValue(guardianName)
                && isBlankValue(guardianRelationship)
                && isBlankValue(presentResponsiblePerson)
                && isBlankValue(mobileNumber)
                && isBlankValue(alternateMobile)
                && isBlankValue(email)
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

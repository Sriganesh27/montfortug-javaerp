package com.erp.montfortuganda.student.bulkimport.mapper;

import com.erp.montfortuganda.student.bulkimport.dto.StudentBulkImportRow;
import com.erp.montfortuganda.student.bulkimport.excel.StudentExcelValueParser;
import com.erp.montfortuganda.student.bulkimport.reference.StudentBulkReferenceService.AcademicYearReference;
import com.erp.montfortuganda.student.bulkimport.reference.StudentBulkReferenceService.ClassReference;
import com.erp.montfortuganda.student.bulkimport.reference.StudentBulkReferenceService.SectionReference;
import com.erp.montfortuganda.student.bulkimport.reference.StudentBulkReferenceService.StudentBulkReferenceData;
import com.erp.montfortuganda.student.dto.request.StudentAcademicHistoryRequest;
import com.erp.montfortuganda.student.dto.request.StudentCreateRequest;
import com.erp.montfortuganda.student.dto.request.StudentEnrollmentRequest;
import com.erp.montfortuganda.student.dto.request.StudentMedicalRequest;
import com.erp.montfortuganda.student.dto.request.StudentParentRequest;
import com.erp.montfortuganda.student.dto.request.StudentPersonalRequest;
import com.erp.montfortuganda.student.entity.ErpParent.FeeResponsibility;
import com.erp.montfortuganda.student.entity.ErpParent.PreferredContact;
import com.erp.montfortuganda.student.entity.ErpStudentEnrollment.AdmissionType;
import com.erp.montfortuganda.student.entity.ErpStudentMedical.BloodGroup;
import com.erp.montfortuganda.student.enums.StudentGender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Converts validated Student Excel rows into the existing
 * {@link StudentCreateRequest} registration structure.
 * <p>
 * Student Code, Admission Number, authenticated branch ownership,
 * statuses, audit fields and entity versions remain backend-controlled.
 */
@Component
@RequiredArgsConstructor
public class StudentBulkRequestMapper {

    private final StudentExcelValueParser valueParser;

    /**
     * Converts one validated Student row into a Student registration request.
     * <p>
     * This method will be used by StudentBulkImportTransactionService.
     * Server-managed identifiers are never accepted from Excel.
     */
    @SuppressWarnings("unused")
    public StudentCreateRequest toCreateRequest(
            StudentBulkImportRow row,
            StudentBulkReferenceData references,
            String operationId
    ) {
        Objects.requireNonNull(
                row,
                "Student bulk-import row is required."
        );

        Objects.requireNonNull(
                references,
                "Student bulk-import references are required."
        );

        AcademicYearReference academicYear =
                requireAcademicYear(
                        row,
                        references
                );

        ClassReference schoolClass =
                requireClass(
                        row,
                        references
                );

        SectionReference section =
                resolveSection(
                        row,
                        references,
                        academicYear,
                        schoolClass
                );

        StudentPersonalRequest personalRequest =
                buildPersonalRequest(
                        row
                );

        StudentParentRequest parentRequest =
                buildParentRequest(
                        row
                );

        StudentEnrollmentRequest enrollmentRequest =
                buildEnrollmentRequest(
                        row,
                        academicYear,
                        schoolClass,
                        section
                );

        StudentMedicalRequest medicalRequest =
                buildMedicalRequest(
                        row
                );

        StudentAcademicHistoryRequest academicHistoryRequest =
                buildAcademicHistoryRequest(
                        row
                );

        return new StudentCreateRequest(
                null,
                personalRequest,
                parentRequest,
                enrollmentRequest,
                medicalRequest,
                academicHistoryRequest,
                null,
                null,
                requireOperationId(operationId)
        );
    }

    // =====================================================================
    // PERSONAL INFORMATION
    // =====================================================================

    private StudentPersonalRequest buildPersonalRequest(
            StudentBulkImportRow row
    ) {
        Integer admissionYear =
                valueParser.requiredInteger(
                        row.getAdmissionYear(),
                        "Admission Year"
                );

        String firstName =
                valueParser.requiredText(
                        row.getFirstName(),
                        "First Name"
                );

        StudentGender gender =
                valueParser.requiredGender(
                        row.getGender()
                );

        LocalDate dateOfBirth =
                valueParser.requiredDate(
                        row.getDateOfBirth(),
                        "Date of Birth"
                );

        return new StudentPersonalRequest(
                null,
                admissionYear,
                firstName,
                valueParser.nullableText(
                        row.getMiddleName()
                ),
                valueParser.nullableText(
                        row.getLastName()
                ),
                gender,
                dateOfBirth,
                valueParser.nullableText(
                        row.getNationality()
                ),
                null,
                valueParser.nullableText(
                        row.getStreet()
                ),
                valueParser.nullableText(
                        row.getVillage()
                ),
                resolveTownOrCity(row),
                valueParser.nullableText(
                        row.getDistrict()
                ),
                valueParser.nullableText(
                        row.getState()
                ),
                valueParser.nullableText(
                        row.getAddressCountry()
                ),
                null
        );
    }

    /**
     * Maps the nearest available locality into the current town/city field.
     */
    private String resolveTownOrCity(
            StudentBulkImportRow row
    ) {
        return firstNonBlank(
                row.getParish(),
                row.getSubCounty(),
                row.getCounty()
        );
    }

    // =====================================================================
    // PARENT AND GUARDIAN INFORMATION
    // =====================================================================

    private StudentParentRequest buildParentRequest(
            StudentBulkImportRow row
    ) {
        PreferredContact preferredContact =
                valueParser.requiredPreferredContact(
                        row.getPreferredContact()
                );

        FeeResponsibility feeResponsibility =
                valueParser.requiredFeeResponsibility(
                        row.getFeeResponsibility()
                );

        Boolean parentsLivingTogether =
                valueParser.requiredYesNo(
                        row.getParentsLivingTogether(),
                        "Parents Living Together"
                );

        ContactMapping contact =
                mapPrimaryContact(
                        row,
                        preferredContact
                );

        validateFeeResponsibilityCompatibility(
                preferredContact,
                feeResponsibility,
                contact
        );

        return new StudentParentRequest(
                // Father
                contact.fatherName(),
                null,
                contact.fatherPhone(),
                contact.fatherAlternatePhone(),
                contact.fatherEmail(),
                null,
                null,
                null,
                null,

                // Mother
                contact.motherName(),
                null,
                contact.motherPhone(),
                contact.motherAlternatePhone(),
                contact.motherEmail(),
                null,
                null,
                null,
                null,

                // Guardian
                contact.guardianName(),
                null,
                contact.guardianRelationship(),
                contact.guardianPhone(),
                contact.guardianAlternatePhone(),
                contact.guardianEmail(),
                null,

                // Communication and family context
                preferredContact,
                feeResponsibility,
                parentsLivingTogether,

                // Emergency contact
                null,
                null,
                null,

                // Parent remarks
                null
        );
    }

    /**
     * Assigns the workbook's primary phone, alternate phone and email to the
     * contact selected in Preferred Contact.
     */
    private ContactMapping mapPrimaryContact(
            StudentBulkImportRow row,
            PreferredContact preferredContact
    ) {
        String fatherOrGuardianName =
                valueParser.nullableText(
                        row.getFatherOrGuardianName()
                );

        String motherOrGuardianName =
                valueParser.nullableText(
                        row.getMotherOrGuardianName()
                );

        String mobile =
                valueParser.requiredText(
                        row.getMobileNumber(),
                        "Mobile No"
                );

        String alternateMobile =
                valueParser.nullableText(
                        row.getAlternateMobile()
                );

        String email =
                valueParser.nullableText(
                        row.getEmail()
                );

        String guardianRelationship =
                valueParser.nullableText(
                        row.getGuardianRelationship()
                );

        if (preferredContact == PreferredContact.FATHER) {
            return new ContactMapping(
                    fatherOrGuardianName,
                    mobile,
                    alternateMobile,
                    email,

                    motherOrGuardianName,
                    null,
                    null,
                    null,

                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        if (preferredContact == PreferredContact.MOTHER) {
            return new ContactMapping(
                    fatherOrGuardianName,
                    null,
                    null,
                    null,

                    motherOrGuardianName,
                    mobile,
                    alternateMobile,
                    email,

                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        return new ContactMapping(
                null,
                null,
                null,
                null,

                null,
                null,
                null,
                null,

                firstNonBlank(
                        fatherOrGuardianName,
                        motherOrGuardianName
                ),
                guardianRelationship,
                mobile,
                alternateMobile,
                email
        );
    }

    /**
     * The current workbook contains one complete phone and email channel.
     * Non-sponsor fee responsibility must therefore match the preferred
     * contact represented by that channel.
     */
    private void validateFeeResponsibilityCompatibility(
            PreferredContact preferredContact,
            FeeResponsibility feeResponsibility,
            ContactMapping contact
    ) {
        if (feeResponsibility == FeeResponsibility.SPONSOR) {
            return;
        }

        if (
                !feeResponsibility.name()
                        .equals(
                                preferredContact.name()
                        )
        ) {
            throw new IllegalArgumentException(
                    "Fee Responsibility must match Preferred Contact "
                            + "or be SPONSOR because the Student import "
                            + "template contains only one complete contact."
            );
        }

        if (feeResponsibility == FeeResponsibility.FATHER) {
            requireNameAndPhone(
                    contact.fatherName(),
                    contact.fatherPhone(),
                    "Father"
            );
            return;
        }

        if (feeResponsibility == FeeResponsibility.MOTHER) {
            requireNameAndPhone(
                    contact.motherName(),
                    contact.motherPhone(),
                    "Mother"
            );
            return;
        }

        requireNameAndPhone(
                contact.guardianName(),
                contact.guardianPhone(),
                "Guardian"
        );
    }

    private void requireNameAndPhone(
            String name,
            String phone,
            String contactType
    ) {
        if (
                isBlank(name)
                        || isBlank(phone)
        ) {
            throw new IllegalArgumentException(
                    contactType
                            + " name and phone are required."
            );
        }
    }

    // =====================================================================
    // CURRENT ENROLLMENT
    // =====================================================================

    private StudentEnrollmentRequest buildEnrollmentRequest(
            StudentBulkImportRow row,
            AcademicYearReference academicYear,
            ClassReference schoolClass,
            SectionReference section
    ) {
        AdmissionType admissionType =
                valueParser.requiredAdmissionType(
                        row.getAdmissionType()
                );

        LocalDate joiningDate =
                valueParser.requiredDate(
                        row.getJoiningDate(),
                        "Joining Date"
                );

        return new StudentEnrollmentRequest(
                academicYear.getAcademicYearId(),
                schoolClass.getClassId(),
                section == null
                        ? null
                        : section.getSectionId(),
                null,
                admissionType,
                joiningDate,
                valueParser.nullableText(
                        row.getRemarks()
                )
        );
    }

    // =====================================================================
    // MEDICAL INFORMATION
    // =====================================================================

    private StudentMedicalRequest buildMedicalRequest(
            StudentBulkImportRow row
    ) {
        BloodGroup bloodGroup =
                valueParser.nullableBloodGroup(
                        row.getBloodGroup()
                );

        String medicalConditions =
                valueParser.nullableText(
                        row.getMedicalConditions()
                );

        if (
                bloodGroup == null
                        && medicalConditions == null
        ) {
            return null;
        }

        return new StudentMedicalRequest(
                bloodGroup,
                null,
                null,
                null,
                medicalConditions,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    // =====================================================================
    // PREVIOUS EDUCATION
    // =====================================================================

    private StudentAcademicHistoryRequest buildAcademicHistoryRequest(
            StudentBulkImportRow row
    ) {
        String previousSchool =
                valueParser.nullableText(
                        row.getPreviousSchool()
                );

        if (previousSchool == null) {
            return null;
        }

        return new StudentAcademicHistoryRequest(
                previousSchool,
                null,
                null,
                null,
                null,
                null,

                null,
                null,
                null,
                null,

                null,
                null,

                null,
                null,

                null,
                null,

                null,
                null
        );
    }

    // =====================================================================
    // REFERENCE RESOLUTION
    // =====================================================================

    private AcademicYearReference requireAcademicYear(
            StudentBulkImportRow row,
            StudentBulkReferenceData references
    ) {
        String key =
                valueParser.normalizeLookupKey(
                        row.getAcademicYear()
                );

        AcademicYearReference academicYear =
                references.findAcademicYear(
                        key
                );

        if (
                academicYear == null
                        || academicYear.getAcademicYearId() == null
        ) {
            throw new IllegalArgumentException(
                    "Academic Year does not exist or is inactive."
            );
        }

        return academicYear;
    }

    private ClassReference requireClass(
            StudentBulkImportRow row,
            StudentBulkReferenceData references
    ) {
        String key =
                valueParser.normalizeLookupKey(
                        row.getClassName()
                );

        ClassReference schoolClass =
                references.findClass(
                        key
                );

        if (
                schoolClass == null
                        || schoolClass.getClassId() == null
        ) {
            throw new IllegalArgumentException(
                    "Class does not exist or is unavailable for this branch."
            );
        }

        return schoolClass;
    }

    private SectionReference resolveSection(
            StudentBulkImportRow row,
            StudentBulkReferenceData references,
            AcademicYearReference academicYear,
            ClassReference schoolClass
    ) {
        String sectionValue =
                valueParser.nullableText(
                        row.getSection()
                );

        if (sectionValue == null) {
            return null;
        }

        SectionReference section =
                references.findSection(
                        academicYear.getAcademicYearId(),
                        schoolClass.getClassId(),
                        valueParser.normalizeLookupKey(
                                sectionValue
                        )
                );

        if (section == null) {
            throw new IllegalArgumentException(
                    "Section does not belong to the selected "
                            + "Academic Year and Class."
            );
        }

        return section;
    }

    // =====================================================================
    // HELPERS
    // =====================================================================

    private String requireOperationId(
            String operationId
    ) {
        String normalized =
                valueParser.requiredText(
                        operationId,
                        "Operation ID"
                );

        if (normalized.length() > 100) {
            throw new IllegalArgumentException(
                    "Operation ID cannot exceed 100 characters."
            );
        }

        return normalized;
    }

    private String firstNonBlank(
            String... values
    ) {
        for (String value : values) {
            String normalized =
                    valueParser.nullableText(
                            value
                    );

            if (normalized != null) {
                return normalized;
            }
        }

        return null;
    }

    private boolean isBlank(
            String value
    ) {
        return value == null
                || value.isBlank();
    }

    private record ContactMapping(
            String fatherName,
            String fatherPhone,
            String fatherAlternatePhone,
            String fatherEmail,

            String motherName,
            String motherPhone,
            String motherAlternatePhone,
            String motherEmail,

            String guardianName,
            String guardianRelationship,
            String guardianPhone,
            String guardianAlternatePhone,
            String guardianEmail
    ) {
    }
}
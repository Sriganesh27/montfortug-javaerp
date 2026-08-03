package com.erp.montfortuganda.student.bulkimport.mapper;

import com.erp.montfortuganda.student.bulkimport.dto.StudentBulkImportRow;
import com.erp.montfortuganda.student.bulkimport.excel.StudentExcelValueParser;
import com.erp.montfortuganda.student.bulkimport.reference.StudentEducationClassNormalizer;
import com.erp.montfortuganda.student.bulkimport.reference.StudentBulkReferenceService.AcademicYearReference;
import com.erp.montfortuganda.student.bulkimport.reference.StudentBulkReferenceService.ClassReference;
import com.erp.montfortuganda.student.bulkimport.reference.StudentBulkReferenceService.LevelReference;
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

    private static final String DEFAULT_FIRST_NAME_PREFIX =
            "UNNAMED STUDENT ROW ";

    private static final String DEFAULT_GUARDIAN_RELATIONSHIP =
            "OTHER";

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
                resolveAcademicYear(
                        row,
                        references
                );

        NormalizedPlacement placement =
                normalizePlacement(row);

        LevelReference level =
                resolveLevel(
                        row,
                        references,
                        placement
                );

        ClassReference schoolClass =
                resolveClass(
                        row,
                        references,
                        level,
                        placement
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
                        row,
                        schoolClass.getClassId()
                );

        StudentParentRequest parentRequest =
                buildParentRequest(
                        row
                );

        StudentEnrollmentRequest enrollmentRequest =
                buildEnrollmentRequest(
                        row,
                        personalRequest.admissionYear(),
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
            StudentBulkImportRow row,
            Integer joiningClassId
    ) {
        Integer admissionYear =
                valueParser.nullableInteger(
                        row.getAdmissionYear(),
                        "Admission Year"
                );

        if (admissionYear == null) {
            admissionYear =
                    LocalDate.now()
                            .getYear();
        }

        String firstName =
                firstNonBlank(
                        row.getFirstName(),
                        row.getMiddleName(),
                        row.getLastName()
                );

        if (firstName == null) {
            firstName =
                    DEFAULT_FIRST_NAME_PREFIX
                            + row.getExcelRowNumber();
        }

        StudentGender gender =
                valueParser.nullableGender(
                        row.getGender()
                );

        LocalDate dateOfBirth =
                valueParser.nullableDate(
                        row.getDateOfBirth(),
                        "Date of Birth"
                );

        return new StudentPersonalRequest(
                null,
                admissionYear,
                joiningClassId,
                null, // joiningTermId — updated Excel mapping will be added later
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
                valueParser.nullablePreferredContact(
                        row.getPreferredContact()
                );

        if (preferredContact == null) {
            preferredContact =
                    inferPreferredContact(row);
        }

        FeeResponsibility feeResponsibility =
                valueParser.nullableFeeResponsibility(
                        row.getFeeResponsibility()
                );

        if (feeResponsibility == null) {
            feeResponsibility =
                    hasResponsibleContact(row)
                            ? FeeResponsibility.valueOf(
                            preferredContact.name()
                    )
                            : FeeResponsibility.SPONSOR;
        }

        Boolean parentsLivingTogether =
                valueParser.nullableYesNo(
                        row.getParentsLivingTogether(),
                        "Parents Living Together"
                );

        if (parentsLivingTogether == null) {
            parentsLivingTogether = Boolean.FALSE;
        }

        ContactMapping contact =
                mapPrimaryContact(
                        row,
                        preferredContact
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

    private PreferredContact inferPreferredContact(
            StudentBulkImportRow row
    ) {
        String relationship =
                valueParser.normalizeEnumValue(
                        row.getGuardianRelationship()
                );

        if (
                "MOTHER".equals(relationship)
                        && !isBlank(
                        row.getMotherOrGuardianName()
                )
        ) {
            return PreferredContact.MOTHER;
        }

        if (
                "FATHER".equals(relationship)
                        && !isBlank(
                        row.getFatherOrGuardianName()
                )
        ) {
            return PreferredContact.FATHER;
        }

        if (!isBlank(row.getFatherOrGuardianName())) {
            return PreferredContact.FATHER;
        }

        if (!isBlank(row.getMotherOrGuardianName())) {
            return PreferredContact.MOTHER;
        }

        return PreferredContact.GUARDIAN;
    }

    private boolean hasResponsibleContact(
            StudentBulkImportRow row
    ) {
        return !isBlank(
                row.getMobileNumber()
        ) && (
                !isBlank(
                        row.getFatherOrGuardianName()
                )
                        || !isBlank(
                        row.getMotherOrGuardianName()
                )
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
                valueParser.nullableText(
                        row.getMobileNumber()
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

        if (
                preferredContact == PreferredContact.GUARDIAN
                        && guardianRelationship == null
                        && (
                        fatherOrGuardianName != null
                                || motherOrGuardianName != null
                                || mobile != null
                )
        ) {
            guardianRelationship =
                    DEFAULT_GUARDIAN_RELATIONSHIP;
        }

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
            Integer admissionYear,
            AcademicYearReference academicYear,
            ClassReference schoolClass,
            SectionReference section
    ) {
        AdmissionType admissionType =
                valueParser.nullableAdmissionType(
                        row.getAdmissionType()
                );

        if (admissionType == null) {
            admissionType = AdmissionType.NEW;
        }

        LocalDate joiningDate =
                valueParser.nullableDate(
                        row.getJoiningDate(),
                        "Joining Date"
                );

        if (joiningDate == null) {
            int resolvedAdmissionYear =
                    admissionYear == null
                            ? LocalDate.now()
                            .getYear()
                            : admissionYear;

            joiningDate =
                    LocalDate.of(
                            resolvedAdmissionYear,
                            1,
                            1
                    );

            if (joiningDate.isAfter(LocalDate.now())) {
                joiningDate = LocalDate.now();
            }
        }

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

    private AcademicYearReference resolveAcademicYear(
            StudentBulkImportRow row,
            StudentBulkReferenceData references
    ) {
        String key =
                valueParser.normalizeLookupKey(
                        row.getAcademicYear()
                );

        AcademicYearReference academicYear =
                key == null
                        ? references.findDefaultAcademicYear()
                        : references.findAcademicYear(
                        key
                );

        if (
                academicYear == null
                        || academicYear.getAcademicYearId() == null
        ) {
            throw new IllegalArgumentException(
                    key == null
                            ? "No active or planned Academic Year is configured."
                            : "Academic Year does not exist or is inactive."
            );
        }

        return academicYear;
    }

    private NormalizedPlacement normalizePlacement(
            StudentBulkImportRow row
    ) {
        String rawLevel =
                valueParser.nullableText(
                        row.getEducationLevel()
                );

        String rawClass =
                valueParser.nullableText(
                        row.getClassName()
                );

        String canonicalLevel =
                rawLevel == null
                        ? null
                        : StudentEducationClassNormalizer
                        .normalizeLevel(rawLevel);

        String canonicalClass =
                rawClass == null
                        ? null
                        : StudentEducationClassNormalizer
                        .normalizeClass(
                                rawClass,
                                canonicalLevel
                        );

        String inferredLevel =
                canonicalClass == null
                        ? null
                        : StudentEducationClassNormalizer
                        .inferLevelFromClass(
                                canonicalClass
                        );

        if (
                canonicalLevel != null
                        && inferredLevel != null
                        && !canonicalLevel.equals(
                        inferredLevel
                )
        ) {
            throw new IllegalArgumentException(
                    "Class "
                            + canonicalClass
                            + " belongs to "
                            + inferredLevel
                            + ", but Education Level is "
                            + canonicalLevel
                            + "."
            );
        }

        if (canonicalLevel == null) {
            canonicalLevel = inferredLevel;
        }

        return new NormalizedPlacement(
                canonicalLevel,
                canonicalClass
        );
    }

    private LevelReference resolveLevel(
            StudentBulkImportRow row,
            StudentBulkReferenceData references,
            NormalizedPlacement placement
    ) {
        String canonicalLevel =
                placement.educationLevel();

        LevelReference level =
                canonicalLevel == null
                        ? references.findDefaultLevel()
                        : references.findLevel(
                        canonicalLevel
                );

        if (
                level == null
                        || level.getLevelId() == null
        ) {
            throw new IllegalArgumentException(
                    canonicalLevel == null
                            ? "No active Education Level is configured for this branch."
                            : "Education Level '"
                              + row.getEducationLevel()
                              + "' was normalized to "
                              + canonicalLevel
                              + ", but it is not configured for this branch."
            );
        }

        return level;
    }

    private ClassReference resolveClass(
            StudentBulkImportRow row,
            StudentBulkReferenceData references,
            LevelReference level,
            NormalizedPlacement placement
    ) {
        String canonicalClass =
                placement.classCode();

        ClassReference schoolClass =
                references.findClass(
                        level,
                        canonicalClass
                );

        if (
                schoolClass == null
                        || schoolClass.getClassId() == null
        ) {
            throw new IllegalArgumentException(
                    canonicalClass == null
                            ? "No active Class is configured under the resolved Education Level."
                            : "Class '"
                              + row.getClassName()
                              + "' was normalized to "
                              + canonicalClass
                              + ", but it is not configured under "
                              + placement.educationLevel()
                              + " for this branch. "
                              + acceptedClassMessage(
                            placement.educationLevel()
                    )
            );
        }

        return schoolClass;
    }

    private String acceptedClassMessage(
            String canonicalLevel
    ) {
        if (
                StudentEducationClassNormalizer.LEVEL_NURSERY
                        .equals(canonicalLevel)
        ) {
            return "Accepted Nursery values include Baby/N1/KG1, "
                    + "Middle/M-C/N2/KG2, and Top/N3/KG3.";
        }

        if (
                StudentEducationClassNormalizer.LEVEL_PRIMARY
                        .equals(canonicalLevel)
        ) {
            return "Accepted Primary values include P1 to P7, "
                    + "including P.1, P-1 and Primary 1.";
        }

        if (
                StudentEducationClassNormalizer.LEVEL_SECONDARY
                        .equals(canonicalLevel)
        ) {
            return "Accepted Secondary values include S1 to S4, "
                    + "including S.1, S-1 and Secondary 1.";
        }

        if (
                StudentEducationClassNormalizer
                        .LEVEL_SENIOR_SECONDARY
                        .equals(canonicalLevel)
        ) {
            return "Accepted Senior Secondary values are S5 and S6, "
                    + "including S.5, S-5 and Senior Secondary 5.";
        }

        return "Use a Class configured for the resolved Education Level.";
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
                    "Section does not belong to the resolved "
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

    private record NormalizedPlacement(
            String educationLevel,
            String classCode
    ) {
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
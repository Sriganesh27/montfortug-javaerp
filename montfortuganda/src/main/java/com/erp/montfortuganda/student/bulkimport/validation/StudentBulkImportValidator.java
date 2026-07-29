package com.erp.montfortuganda.student.bulkimport.validation;

import com.erp.montfortuganda.common.importframework.context.ImportContext;
import com.erp.montfortuganda.common.importframework.plugin.ImportValidatorChain;
import com.erp.montfortuganda.common.importframework.plugin.ValidationResult;
import com.erp.montfortuganda.student.bulkimport.dto.StudentBulkImportRow;
import com.erp.montfortuganda.student.bulkimport.excel.StudentExcelHeaders;
import com.erp.montfortuganda.student.bulkimport.excel.StudentExcelValueParser;
import com.erp.montfortuganda.student.bulkimport.reference.StudentBulkReferenceService;
import com.erp.montfortuganda.student.bulkimport.reference.StudentBulkReferenceService.AcademicYearReference;
import com.erp.montfortuganda.student.bulkimport.reference.StudentBulkReferenceService.ClassReference;
import com.erp.montfortuganda.student.bulkimport.reference.StudentBulkReferenceService.LevelReference;
import com.erp.montfortuganda.student.bulkimport.reference.StudentBulkReferenceService.SectionReference;
import com.erp.montfortuganda.student.bulkimport.reference.StudentBulkReferenceService.StudentBulkReferenceData;
import com.erp.montfortuganda.student.entity.ErpParent.PreferredContact;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Validates Student Excel rows without creating or updating database records.
 * Invalid rows receive field-level errors and are excluded from insertion.
 */
@Component
@RequiredArgsConstructor
public class StudentBulkImportValidator
        implements ImportValidatorChain<StudentBulkImportRow> {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile(
                    "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern MOBILE_PATTERN =
            Pattern.compile(
                    "^[+0-9][0-9()\\-\\s]{6,29}$"
            );

    private static final String REFERENCE_CACHE_KEY =
            "student.bulk.references";

    private static final String STUDENT_IDENTITY_CACHE_KEY =
            "student.bulk.studentIdentities";

    private final StudentExcelValueParser valueParser;

    private final StudentBulkReferenceService referenceService;

    @Override
    public ValidationResult validate(
            StudentBulkImportRow row,
            int rowNum,
            ImportContext context
    ) {
        List<ValidationResult.ValidationError> errors =
                new ArrayList<>();

        if (row == null || row.isBlank()) {
            return ValidationResult.builder()
                    .success(true)
                    .skipRow(true)
                    .errors(List.of())
                    .warnings(List.of())
                    .build();
        }

        Integer branchId =
                parseBranchId(context);

        StudentBulkReferenceData references =
                getReferences(
                        context,
                        branchId
                );

        validateAdmissionNumber(
                row,
                errors
        );

        validateAdmissionYear(
                row,
                errors
        );

        validateStudentNames(
                row,
                errors
        );

        validateGender(
                row,
                errors
        );

        validateDateOfBirth(
                row,
                errors
        );

        validateBranch(
                row,
                references,
                errors
        );

        validateAcademicPlacement(
                row,
                references,
                errors
        );

        validateAdmissionType(
                row,
                errors
        );

        validateJoiningDate(
                row,
                errors
        );

        validateDateRelationship(
                row,
                errors
        );

        validateParentAndGuardian(
                row,
                errors
        );

        validateEmail(
                row,
                errors
        );

        validateMobileNumbers(
                row,
                errors
        );

        validateBloodGroup(
                row,
                errors
        );

        validateYesNoFields(
                row,
                errors
        );

        validateMaximumLengths(
                row,
                errors
        );

        validateInFileDuplicateStudent(
                row,
                context,
                errors
        );

        return ValidationResult.builder()
                .success(errors.isEmpty())
                .skipRow(false)
                .errors(List.copyOf(errors))
                .warnings(List.of())
                .build();
    }

    // =====================================================================
    // IMPORT CONTEXT
    // =====================================================================

    private Integer parseBranchId(
            ImportContext context
    ) {
        if (
                context == null
                        || context.getBranchId() == null
                        || context.getBranchId().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Student import branch context is missing."
            );
        }

        try {
            int branchId =
                    Integer.parseInt(
                            context.getBranchId()
                                    .trim()
                    );

            if (branchId <= 0) {
                throw new NumberFormatException(
                        "Branch ID must be positive."
                );
            }

            return branchId;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Student import branch context is invalid."
            );
        }
    }

    private StudentBulkReferenceData getReferences(
            ImportContext context,
            Integer branchId
    ) {
        Object cached =
                context.getJobStateCache()
                        .computeIfAbsent(
                                REFERENCE_CACHE_KEY,
                                ignored ->
                                        referenceService
                                                .loadReferences(
                                                        branchId
                                                )
                        );

        if (
                !(cached
                        instanceof StudentBulkReferenceData references)
        ) {
            throw new IllegalStateException(
                    "Student import reference cache is invalid."
            );
        }

        if (
                references.getBranchId() == null
                        || !branchId.equals(
                        references.getBranchId()
                )
        ) {
            throw new IllegalStateException(
                    "Student import branch reference mismatch."
            );
        }

        return references;
    }

    // =====================================================================
    // ADMISSION NUMBER
    // =====================================================================

    private void validateAdmissionNumber(
            StudentBulkImportRow row,
            List<ValidationResult.ValidationError> errors
    ) {
        validateOptionalLength(
                row.getAdmissionNo(),
                StudentExcelHeaders.ADMISSION_NO,
                50,
                errors
        );
    }

    // =====================================================================
    // ADMISSION YEAR
    // =====================================================================

    private void validateAdmissionYear(
            StudentBulkImportRow row,
            List<ValidationResult.ValidationError> errors
    ) {
        Integer admissionYear;

        try {
            admissionYear =
                    valueParser.nullableInteger(
                            row.getAdmissionYear(),
                            StudentExcelHeaders.ADMISSION_YEAR
                    );
        } catch (IllegalArgumentException exception) {
            addError(
                    errors,
                    StudentExcelHeaders.ADMISSION_YEAR,
                    row.getAdmissionYear(),
                    "STUDENT_ADMISSION_YEAR_INVALID",
                    exception.getMessage()
            );
            return;
        }

        if (admissionYear == null) {
            addError(
                    errors,
                    StudentExcelHeaders.ADMISSION_YEAR,
                    row.getAdmissionYear(),
                    "STUDENT_ADMISSION_YEAR_REQUIRED",
                    "Admission Year is required."
            );
            return;
        }

        if (
                admissionYear < 1900
                        || admissionYear > 2100
        ) {
            addError(
                    errors,
                    StudentExcelHeaders.ADMISSION_YEAR,
                    row.getAdmissionYear(),
                    "STUDENT_ADMISSION_YEAR_RANGE_INVALID",
                    "Admission Year must be between 1900 and 2100."
            );
        }
    }

    // =====================================================================
    // STUDENT NAMES
    // =====================================================================

    private void validateStudentNames(
            StudentBulkImportRow row,
            List<ValidationResult.ValidationError> errors
    ) {
        String firstName =
                valueParser.nullableText(
                        row.getFirstName()
                );

        if (firstName == null) {
            addError(
                    errors,
                    StudentExcelHeaders.FIRST_NAME,
                    row.getFirstName(),
                    "STUDENT_REQUIRED_VALUE_MISSING",
                    "First Name is required."
            );
        } else if (firstName.length() > 100) {
            addError(
                    errors,
                    StudentExcelHeaders.FIRST_NAME,
                    row.getFirstName(),
                    "STUDENT_VALUE_TOO_LONG",
                    "First Name cannot exceed 100 characters."
            );
        }

        validateOptionalLength(
                row.getMiddleName(),
                StudentExcelHeaders.MIDDLE_NAME,
                100,
                errors
        );

        validateOptionalLength(
                row.getLastName(),
                StudentExcelHeaders.LAST_NAME,
                100,
                errors
        );
    }

    // =====================================================================
    // GENDER
    // =====================================================================

    private void validateGender(
            StudentBulkImportRow row,
            List<ValidationResult.ValidationError> errors
    ) {
        try {
            valueParser.requiredGender(
                    row.getGender()
            );
        } catch (IllegalArgumentException exception) {
            addError(
                    errors,
                    StudentExcelHeaders.GENDER,
                    row.getGender(),
                    "STUDENT_GENDER_INVALID",
                    exception.getMessage()
            );
        }
    }

    // =====================================================================
    // DATE OF BIRTH
    // =====================================================================

    private void validateDateOfBirth(
            StudentBulkImportRow row,
            List<ValidationResult.ValidationError> errors
    ) {
        LocalDate dateOfBirth;

        try {
            dateOfBirth =
                    valueParser.requiredDate(
                            row.getDateOfBirth(),
                            StudentExcelHeaders.DATE_OF_BIRTH
                    );
        } catch (IllegalArgumentException exception) {
            addError(
                    errors,
                    StudentExcelHeaders.DATE_OF_BIRTH,
                    row.getDateOfBirth(),
                    "STUDENT_DATE_OF_BIRTH_INVALID",
                    exception.getMessage()
            );
            return;
        }

        if (!dateOfBirth.isBefore(LocalDate.now())) {
            addError(
                    errors,
                    StudentExcelHeaders.DATE_OF_BIRTH,
                    row.getDateOfBirth(),
                    "STUDENT_DATE_OF_BIRTH_NOT_PAST",
                    "Date of Birth must be before today."
            );
        }
    }

    // =====================================================================
    // BRANCH
    // =====================================================================

    private void validateBranch(
            StudentBulkImportRow row,
            StudentBulkReferenceData references,
            List<ValidationResult.ValidationError> errors
    ) {
        String branch =
                valueParser.nullableText(
                        row.getBranch()
                );

        if (branch == null) {
            addError(
                    errors,
                    StudentExcelHeaders.BRANCH,
                    row.getBranch(),
                    "STUDENT_BRANCH_REQUIRED",
                    "Branch is required."
            );
            return;
        }

        String normalizedBranch =
                valueParser.normalizeLookupKey(
                        branch
                );

        if (
                !references.matchesBranch(
                        normalizedBranch
                )
        ) {
            addError(
                    errors,
                    StudentExcelHeaders.BRANCH,
                    row.getBranch(),
                    "STUDENT_BRANCH_MISMATCH",
                    "Branch does not match the authenticated Branch Admin branch."
            );
        }
    }

    // =====================================================================
    // ACADEMIC PLACEMENT
    // =====================================================================

    private void validateAcademicPlacement(
            StudentBulkImportRow row,
            StudentBulkReferenceData references,
            List<ValidationResult.ValidationError> errors
    ) {
        AcademicYearReference academicYear =
                resolveAcademicYear(
                        row,
                        references,
                        errors
                );

        LevelReference level =
                resolveLevel(
                        row,
                        references,
                        errors
                );

        ClassReference schoolClass =
                resolveClass(
                        row,
                        references,
                        errors
                );

        if (
                level != null
                        && schoolClass != null
                        && !references.classBelongsToLevel(
                        schoolClass,
                        level
                )
        ) {
            addError(
                    errors,
                    StudentExcelHeaders.CLASS_NAME,
                    row.getClassName(),
                    "STUDENT_CLASS_LEVEL_MISMATCH",
                    "Class does not belong to the selected Education Level."
            );
        }

        validateSection(
                row,
                references,
                academicYear,
                schoolClass,
                errors
        );
    }

    private AcademicYearReference resolveAcademicYear(
            StudentBulkImportRow row,
            StudentBulkReferenceData references,
            List<ValidationResult.ValidationError> errors
    ) {
        String academicYearValue =
                valueParser.nullableText(
                        row.getAcademicYear()
                );

        if (academicYearValue == null) {
            addError(
                    errors,
                    StudentExcelHeaders.ACADEMIC_YEAR,
                    row.getAcademicYear(),
                    "STUDENT_ACADEMIC_YEAR_REQUIRED",
                    "Academic Year is required."
            );
            return null;
        }

        AcademicYearReference academicYear =
                references.findAcademicYear(
                        valueParser.normalizeLookupKey(
                                academicYearValue
                        )
                );

        if (academicYear == null) {
            addError(
                    errors,
                    StudentExcelHeaders.ACADEMIC_YEAR,
                    row.getAcademicYear(),
                    "STUDENT_ACADEMIC_YEAR_NOT_FOUND",
                    "Academic Year does not exist or is inactive."
            );
        }

        return academicYear;
    }

    private LevelReference resolveLevel(
            StudentBulkImportRow row,
            StudentBulkReferenceData references,
            List<ValidationResult.ValidationError> errors
    ) {
        String levelValue =
                valueParser.nullableText(
                        row.getEducationLevel()
                );

        if (levelValue == null) {
            addError(
                    errors,
                    StudentExcelHeaders.EDUCATION_LEVEL,
                    row.getEducationLevel(),
                    "STUDENT_EDUCATION_LEVEL_REQUIRED",
                    "Education Level is required."
            );
            return null;
        }

        LevelReference level =
                references.findLevel(
                        valueParser.normalizeLookupKey(
                                levelValue
                        )
                );

        if (level == null) {
            addError(
                    errors,
                    StudentExcelHeaders.EDUCATION_LEVEL,
                    row.getEducationLevel(),
                    "STUDENT_EDUCATION_LEVEL_NOT_FOUND",
                    "Education Level is not available for this branch."
            );
        }

        return level;
    }

    private ClassReference resolveClass(
            StudentBulkImportRow row,
            StudentBulkReferenceData references,
            List<ValidationResult.ValidationError> errors
    ) {
        String classValue =
                valueParser.nullableText(
                        row.getClassName()
                );

        if (classValue == null) {
            addError(
                    errors,
                    StudentExcelHeaders.CLASS_NAME,
                    row.getClassName(),
                    "STUDENT_CLASS_REQUIRED",
                    "Class is required."
            );
            return null;
        }

        ClassReference schoolClass =
                references.findClass(
                        valueParser.normalizeLookupKey(
                                classValue
                        )
                );

        if (schoolClass == null) {
            addError(
                    errors,
                    StudentExcelHeaders.CLASS_NAME,
                    row.getClassName(),
                    "STUDENT_CLASS_NOT_FOUND",
                    "Class does not exist or is unavailable for this branch."
            );
        }

        return schoolClass;
    }

    private void validateSection(
            StudentBulkImportRow row,
            StudentBulkReferenceData references,
            AcademicYearReference academicYear,
            ClassReference schoolClass,
            List<ValidationResult.ValidationError> errors
    ) {
        String sectionValue =
                valueParser.nullableText(
                        row.getSection()
                );

        if (sectionValue == null) {
            return;
        }

        if (
                academicYear == null
                        || schoolClass == null
        ) {
            return;
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
            addError(
                    errors,
                    StudentExcelHeaders.SECTION,
                    row.getSection(),
                    "STUDENT_SECTION_NOT_FOUND",
                    "Section does not belong to the selected Academic Year and Class."
            );
        }
    }

    // =====================================================================
    // ADMISSION TYPE
    // =====================================================================

    private void validateAdmissionType(
            StudentBulkImportRow row,
            List<ValidationResult.ValidationError> errors
    ) {
        try {
            valueParser.requiredAdmissionType(
                    row.getAdmissionType()
            );
        } catch (IllegalArgumentException exception) {
            addError(
                    errors,
                    StudentExcelHeaders.ADMISSION_TYPE,
                    row.getAdmissionType(),
                    "STUDENT_ADMISSION_TYPE_INVALID",
                    exception.getMessage()
            );
        }
    }

    // =====================================================================
    // JOINING DATE
    // =====================================================================

    private void validateJoiningDate(
            StudentBulkImportRow row,
            List<ValidationResult.ValidationError> errors
    ) {
        LocalDate joiningDate;

        try {
            joiningDate =
                    valueParser.requiredDate(
                            row.getJoiningDate(),
                            StudentExcelHeaders.JOINING_DATE
                    );
        } catch (IllegalArgumentException exception) {
            addError(
                    errors,
                    StudentExcelHeaders.JOINING_DATE,
                    row.getJoiningDate(),
                    "STUDENT_JOINING_DATE_INVALID",
                    exception.getMessage()
            );
            return;
        }

        if (joiningDate.isAfter(LocalDate.now())) {
            addError(
                    errors,
                    StudentExcelHeaders.JOINING_DATE,
                    row.getJoiningDate(),
                    "STUDENT_JOINING_DATE_FUTURE",
                    "Joining Date cannot be in the future."
            );
        }
    }

    private void validateDateRelationship(
            StudentBulkImportRow row,
            List<ValidationResult.ValidationError> errors
    ) {
        LocalDate dateOfBirth;
        LocalDate joiningDate;

        try {
            dateOfBirth =
                    valueParser.nullableDate(
                            row.getDateOfBirth(),
                            StudentExcelHeaders.DATE_OF_BIRTH
                    );

            joiningDate =
                    valueParser.nullableDate(
                            row.getJoiningDate(),
                            StudentExcelHeaders.JOINING_DATE
                    );
        } catch (IllegalArgumentException exception) {
            return;
        }

        if (
                dateOfBirth != null
                        && joiningDate != null
                        && !dateOfBirth.isBefore(
                        joiningDate
                )
        ) {
            addError(
                    errors,
                    StudentExcelHeaders.JOINING_DATE,
                    row.getJoiningDate(),
                    "STUDENT_DATE_ORDER_INVALID",
                    "Joining Date must be after Date of Birth."
            );
        }
    }

    // =====================================================================
    // PARENT / GUARDIAN
    // =====================================================================

    private void validateParentAndGuardian(
            StudentBulkImportRow row,
            List<ValidationResult.ValidationError> errors
    ) {
        PreferredContact preferredContact =
                parsePreferredContact(
                        row,
                        errors
                );

        validateFeeResponsibility(
                row,
                errors
        );

        validateParentsLivingTogether(
                row,
                errors
        );

        if (preferredContact == null) {
            return;
        }

        switch (preferredContact) {
            case FATHER ->
                    validatePreferredName(
                            row.getFatherOrGuardianName(),
                            StudentExcelHeaders
                                    .FATHER_OR_GUARDIAN_NAME,
                            "Father Name is required when Preferred Contact is FATHER.",
                            errors
                    );

            case MOTHER ->
                    validatePreferredName(
                            row.getMotherOrGuardianName(),
                            StudentExcelHeaders
                                    .MOTHER_OR_GUARDIAN_NAME,
                            "Mother Name is required when Preferred Contact is MOTHER.",
                            errors
                    );

            case GUARDIAN -> {
                validatePreferredName(
                        row.getFatherOrGuardianName(),
                        StudentExcelHeaders
                                .FATHER_OR_GUARDIAN_NAME,
                        "Guardian Name is required when Preferred Contact is GUARDIAN.",
                        errors
                );

                String relationship =
                        valueParser.nullableText(
                                row.getGuardianRelationship()
                        );

                if (relationship == null) {
                    addError(
                            errors,
                            StudentExcelHeaders
                                    .GUARDIAN_RELATIONSHIP,
                            row.getGuardianRelationship(),
                            "STUDENT_GUARDIAN_RELATIONSHIP_REQUIRED",
                            "Guardian Relationship is required when Preferred Contact is GUARDIAN."
                    );
                }
            }
        }
    }

    private PreferredContact parsePreferredContact(
            StudentBulkImportRow row,
            List<ValidationResult.ValidationError> errors
    ) {
        try {
            return valueParser.requiredPreferredContact(
                    row.getPreferredContact()
            );
        } catch (IllegalArgumentException exception) {
            addError(
                    errors,
                    StudentExcelHeaders.PREFERRED_CONTACT,
                    row.getPreferredContact(),
                    "STUDENT_PREFERRED_CONTACT_INVALID",
                    exception.getMessage()
            );

            return null;
        }
    }

    private void validateFeeResponsibility(
            StudentBulkImportRow row,
            List<ValidationResult.ValidationError> errors
    ) {
        try {
            valueParser.requiredFeeResponsibility(
                    row.getFeeResponsibility()
            );
        } catch (IllegalArgumentException exception) {
            addError(
                    errors,
                    StudentExcelHeaders.FEE_RESPONSIBILITY,
                    row.getFeeResponsibility(),
                    "STUDENT_FEE_RESPONSIBILITY_INVALID",
                    exception.getMessage()
            );
        }
    }

    private void validateParentsLivingTogether(
            StudentBulkImportRow row,
            List<ValidationResult.ValidationError> errors
    ) {
        try {
            valueParser.requiredYesNo(
                    row.getParentsLivingTogether(),
                    StudentExcelHeaders.PARENTS_LIVING_TOGETHER
            );
        } catch (IllegalArgumentException exception) {
            addError(
                    errors,
                    StudentExcelHeaders
                            .PARENTS_LIVING_TOGETHER,
                    row.getParentsLivingTogether(),
                    "STUDENT_PARENTS_LIVING_TOGETHER_INVALID",
                    exception.getMessage()
            );
        }
    }

    private void validatePreferredName(
            String value,
            String columnName,
            String message,
            List<ValidationResult.ValidationError> errors
    ) {
        if (
                valueParser.nullableText(
                        value
                ) == null
        ) {
            addError(
                    errors,
                    columnName,
                    value,
                    "STUDENT_PREFERRED_CONTACT_NAME_REQUIRED",
                    message
            );
        }
    }

    // =====================================================================
    // EMAIL AND MOBILE
    // =====================================================================

    private void validateEmail(
            StudentBulkImportRow row,
            List<ValidationResult.ValidationError> errors
    ) {
        String email =
                valueParser.nullableText(
                        row.getEmail()
                );

        if (email == null) {
            return;
        }

        if (
                email.length() > 150
                        || !EMAIL_PATTERN
                        .matcher(email)
                        .matches()
        ) {
            addError(
                    errors,
                    StudentExcelHeaders.EMAIL,
                    row.getEmail(),
                    "STUDENT_EMAIL_INVALID",
                    "Email must contain a valid email address."
            );
        }
    }

    private void validateMobileNumbers(
            StudentBulkImportRow row,
            List<ValidationResult.ValidationError> errors
    ) {
        validateRequiredMobile(
                row.getMobileNumber(),
                errors
        );

        validateOptionalAlternateMobile(
                row.getAlternateMobile(),
                errors
        );
    }

    private void validateRequiredMobile(
            String value,
            List<ValidationResult.ValidationError> errors
    ) {
        String mobile =
                valueParser.nullableText(
                        value
                );

        if (mobile == null) {
            addError(
                    errors,
                    StudentExcelHeaders.MOBILE_NUMBER,
                    value,
                    "STUDENT_MOBILE_REQUIRED",
                    "Mobile No is required."
            );
            return;
        }

        if (!MOBILE_PATTERN.matcher(mobile).matches()) {
            addError(
                    errors,
                    StudentExcelHeaders.MOBILE_NUMBER,
                    value,
                    "STUDENT_MOBILE_INVALID",
                    "Mobile No contains an invalid mobile number."
            );
        }
    }

    private void validateOptionalAlternateMobile(
            String value,
            List<ValidationResult.ValidationError> errors
    ) {
        String mobile =
                valueParser.nullableText(
                        value
                );

        if (mobile == null) {
            return;
        }

        if (!MOBILE_PATTERN.matcher(mobile).matches()) {
            addError(
                    errors,
                    StudentExcelHeaders.ALTERNATE_MOBILE,
                    value,
                    "STUDENT_ALTERNATE_MOBILE_INVALID",
                    "Alternate Mobile contains an invalid mobile number."
            );
        }
    }

    // =====================================================================
    // BLOOD GROUP
    // =====================================================================

    private void validateBloodGroup(
            StudentBulkImportRow row,
            List<ValidationResult.ValidationError> errors
    ) {
        try {
            valueParser.nullableBloodGroup(
                    row.getBloodGroup()
            );
        } catch (IllegalArgumentException exception) {
            addError(
                    errors,
                    StudentExcelHeaders.BLOOD_GROUP,
                    row.getBloodGroup(),
                    "STUDENT_BLOOD_GROUP_INVALID",
                    exception.getMessage()
            );
        }
    }

    // =====================================================================
    // YES / NO FIELDS
    // =====================================================================

    private void validateYesNoFields(
            StudentBulkImportRow row,
            List<ValidationResult.ValidationError> errors
    ) {
        validateOptionalYesNo(
                row.getTransportRequired(),
                StudentExcelHeaders.TRANSPORT_REQUIRED,
                errors
        );

        validateOptionalYesNo(
                row.getHostelRequired(),
                StudentExcelHeaders.HOSTEL_REQUIRED,
                errors
        );

        validateOptionalYesNo(
                row.getScholarship(),
                StudentExcelHeaders.SCHOLARSHIP,
                errors
        );
    }

    private void validateOptionalYesNo(
            String value,
            String column,
            List<ValidationResult.ValidationError> errors
    ) {
        try {
            valueParser.nullableYesNo(
                    value,
                    column
            );
        } catch (IllegalArgumentException exception) {
            addError(
                    errors,
                    column,
                    value,
                    "STUDENT_YES_NO_INVALID",
                    exception.getMessage()
            );
        }
    }

    // =====================================================================
    // FIELD LENGTHS
    // =====================================================================

    private void validateMaximumLengths(
            StudentBulkImportRow row,
            List<ValidationResult.ValidationError> errors
    ) {
        validateOptionalLength(
                row.getBranch(),
                StudentExcelHeaders.BRANCH,
                150,
                errors
        );

        validateOptionalLength(
                row.getEducationLevel(),
                StudentExcelHeaders.EDUCATION_LEVEL,
                100,
                errors
        );

        validateOptionalLength(
                row.getClassName(),
                StudentExcelHeaders.CLASS_NAME,
                100,
                errors
        );

        validateOptionalLength(
                row.getSection(),
                StudentExcelHeaders.SECTION,
                100,
                errors
        );

        validateOptionalLength(
                row.getAcademicYear(),
                StudentExcelHeaders.ACADEMIC_YEAR,
                100,
                errors
        );

        validateOptionalLength(
                row.getFatherOrGuardianName(),
                StudentExcelHeaders.FATHER_OR_GUARDIAN_NAME,
                150,
                errors
        );

        validateOptionalLength(
                row.getMotherOrGuardianName(),
                StudentExcelHeaders.MOTHER_OR_GUARDIAN_NAME,
                150,
                errors
        );

        validateOptionalLength(
                row.getGuardianRelationship(),
                StudentExcelHeaders.GUARDIAN_RELATIONSHIP,
                100,
                errors
        );

        validateOptionalLength(
                row.getMobileNumber(),
                StudentExcelHeaders.MOBILE_NUMBER,
                30,
                errors
        );

        validateOptionalLength(
                row.getAlternateMobile(),
                StudentExcelHeaders.ALTERNATE_MOBILE,
                30,
                errors
        );

        validateOptionalLength(
                row.getNationality(),
                StudentExcelHeaders.NATIONALITY,
                100,
                errors
        );

        validateOptionalLength(
                row.getNationalIdOrPassport(),
                StudentExcelHeaders.NATIONAL_ID_OR_PASSPORT,
                100,
                errors
        );

        validateOptionalLength(
                row.getAddressCountry(),
                StudentExcelHeaders.ADDRESS_COUNTRY,
                100,
                errors
        );

        validateOptionalLength(
                row.getState(),
                StudentExcelHeaders.STATE,
                100,
                errors
        );

        validateOptionalLength(
                row.getDistrict(),
                StudentExcelHeaders.DISTRICT,
                100,
                errors
        );

        validateOptionalLength(
                row.getCounty(),
                StudentExcelHeaders.COUNTY,
                100,
                errors
        );

        validateOptionalLength(
                row.getSubCounty(),
                StudentExcelHeaders.SUB_COUNTY,
                100,
                errors
        );

        validateOptionalLength(
                row.getParish(),
                StudentExcelHeaders.PARISH,
                100,
                errors
        );

        validateOptionalLength(
                row.getVillage(),
                StudentExcelHeaders.VILLAGE,
                100,
                errors
        );

        validateOptionalLength(
                row.getStreet(),
                StudentExcelHeaders.STREET,
                150,
                errors
        );

        validateOptionalLength(
                row.getPreviousSchool(),
                StudentExcelHeaders.PREVIOUS_SCHOOL,
                255,
                errors
        );

        validateOptionalLength(
                row.getReligion(),
                StudentExcelHeaders.RELIGION,
                100,
                errors
        );

        validateOptionalLength(
                row.getMedicalConditions(),
                StudentExcelHeaders.MEDICAL_CONDITIONS,
                500,
                errors
        );

        validateOptionalLength(
                row.getRemarks(),
                StudentExcelHeaders.REMARKS,
                5000,
                errors
        );
    }

    private void validateOptionalLength(
            String value,
            String column,
            int maximumLength,
            List<ValidationResult.ValidationError> errors
    ) {
        String normalized =
                valueParser.nullableText(
                        value
                );

        if (
                normalized != null
                        && normalized.length() > maximumLength
        ) {
            addError(
                    errors,
                    column,
                    value,
                    "STUDENT_VALUE_TOO_LONG",
                    column
                            + " cannot exceed "
                            + maximumLength
                            + " characters."
            );
        }
    }

    // =====================================================================
    // IN-FILE DUPLICATES
    // =====================================================================

    private void validateInFileDuplicateStudent(
            StudentBulkImportRow row,
            ImportContext context,
            List<ValidationResult.ValidationError> errors
    ) {
        String identity =
                buildStudentIdentity(
                        row
                );

        if (identity == null) {
            return;
        }

        Object cached =
                context.getJobStateCache()
                        .computeIfAbsent(
                                STUDENT_IDENTITY_CACHE_KEY,
                                ignored ->
                                        ConcurrentHashMap
                                                .newKeySet()
                        );

        if (!(cached instanceof Set<?> rawSet)) {
            throw new IllegalStateException(
                    "Student duplicate cache is invalid."
            );
        }

        @SuppressWarnings("unchecked")
        Set<String> identities =
                (Set<String>) rawSet;

        if (!identities.add(identity)) {
            addError(
                    errors,
                    StudentExcelHeaders.FIRST_NAME,
                    row.getFirstName(),
                    "STUDENT_DUPLICATE_INSIDE_WORKBOOK",
                    "The same Student name, Date of Birth and Mobile No are duplicated inside the workbook."
            );
        }
    }

    private String buildStudentIdentity(
            StudentBulkImportRow row
    ) {
        String firstName =
                normalizeDuplicateValue(
                        row.getFirstName()
                );

        String middleName =
                normalizeDuplicateValue(
                        row.getMiddleName()
                );

        String lastName =
                normalizeDuplicateValue(
                        row.getLastName()
                );

        String dateOfBirth =
                normalizeDuplicateValue(
                        row.getDateOfBirth()
                );

        String mobile =
                normalizeDuplicateValue(
                        row.getMobileNumber()
                );

        if (
                firstName == null
                        || dateOfBirth == null
                        || mobile == null
        ) {
            return null;
        }

        return firstName
                + "|"
                + valueOrEmpty(middleName)
                + "|"
                + valueOrEmpty(lastName)
                + "|"
                + dateOfBirth
                + "|"
                + mobile;
    }

    private String normalizeDuplicateValue(
            String value
    ) {
        String normalized =
                valueParser.nullableText(
                        value
                );

        if (normalized == null) {
            return null;
        }

        return normalized
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");
    }

    private String valueOrEmpty(
            String value
    ) {
        return value == null
                ? ""
                : value;
    }

    // =====================================================================
    // ERROR CREATION
    // =====================================================================

    private void addError(
            List<ValidationResult.ValidationError> errors,
            String columnName,
            String cellValue,
            String errorCode,
            String message
    ) {
        errors.add(
                ValidationResult.ValidationError
                        .builder()
                        .columnName(columnName)
                        .cellValue(cellValue)
                        .errorCode(errorCode)
                        .message(message)
                        .suggestedFix(
                                StudentExcelHeaders
                                        .ENTER_VALID_DATA
                        )
                        .build()
        );
    }
}
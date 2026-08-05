package com.erp.montfortuganda.student.bulkimport.validation;

import com.erp.montfortuganda.common.importframework.context.ImportContext;
import com.erp.montfortuganda.common.importframework.plugin.ImportValidatorChain;
import com.erp.montfortuganda.common.importframework.plugin.ValidationResult;
import com.erp.montfortuganda.student.bulkimport.dto.StudentBulkImportRow;
import com.erp.montfortuganda.student.bulkimport.excel.StudentExcelHeaders;
import com.erp.montfortuganda.student.bulkimport.excel.StudentExcelValueParser;
import com.erp.montfortuganda.student.bulkimport.reference.StudentBulkReferenceService;
import com.erp.montfortuganda.student.bulkimport.reference.StudentEducationClassNormalizer;
import com.erp.montfortuganda.student.bulkimport.reference.StudentBulkReferenceService.AcademicYearReference;
import com.erp.montfortuganda.student.bulkimport.reference.StudentBulkReferenceService.ClassReference;
import com.erp.montfortuganda.student.bulkimport.reference.StudentBulkReferenceService.LevelReference;
import com.erp.montfortuganda.student.bulkimport.reference.StudentBulkReferenceService.SectionReference;
import com.erp.montfortuganda.student.bulkimport.reference.StudentBulkReferenceService.StudentBulkReferenceData;
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
 *
 * <p>Every physical Student Excel cell is optional. Blank values are accepted
 * and are resolved later by the secure bulk-request mapper. Values that are
 * supplied by the user are still checked for valid format, length and
 * branch-scoped reference compatibility.</p>
 *
 * <p>Completely blank rows are skipped.</p>
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

        validateAcademicPlacement(
                row,
                references,
                errors
        );

        validateAdmissionType(
                row
        );

        validateAdmissionDate(
                row,
                errors
        );

        validateAdmissionYearDateConsistency(
                row,
                errors
        );

        validateDateRelationship(
                row,
                errors
        );

        validateParentAndGuardian(
                row
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

        if (
                firstName != null
                        && firstName.length() > 100
        ) {
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
        if (
                valueParser.nullableText(
                        row.getGender()
                ) == null
        ) {
            return;
        }

        try {
            valueParser.nullableGender(
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
                    valueParser.nullableDate(
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

        if (dateOfBirth == null) {
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

        NormalizedPlacement normalizedPlacement =
                normalizePlacement(
                        row,
                        errors
                );

        LevelReference level =
                resolveLevel(
                        row,
                        references,
                        normalizedPlacement,
                        errors
                );

        ClassReference schoolClass =
                resolveClass(
                        row,
                        references,
                        level,
                        normalizedPlacement,
                        errors
                );

        validateSection(
                row,
                references,
                academicYear,
                schoolClass,
                errors
        );

        validateJoiningClass(
                row,
                references,
                errors
        );
    }

    /**
     * Blank Academic Year uses the current active Academic Year. When no
     * current year is marked, the latest active/planned year is used.
     */
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
            AcademicYearReference defaultYear =
                    references.findDefaultAcademicYear();

            if (defaultYear == null) {
                addError(
                        errors,
                        StudentExcelHeaders.ACADEMIC_YEAR,
                        row.getAcademicYear(),
                        "STUDENT_ACADEMIC_YEAR_NOT_CONFIGURED",
                        "No active or planned Academic Year is configured."
                );
            }

            return defaultYear;
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
                    "Academic Year '"
                            + academicYearValue
                            + "' does not exist or is inactive."
            );
        }

        return academicYear;
    }

    /**
     * Converts workbook Education Level and Class labels into canonical ERP
     * values before branch-scoped reference resolution.
     *
     * <p>Supported canonical placement:</p>
     *
     * <ul>
     *     <li>NURSERY: N1, N2, N3</li>
     *     <li>PRIMARY: P1 to P7</li>
     *     <li>SECONDARY: S1 to S4</li>
     *     <li>SENIOR SECONDARY: S5 and S6</li>
     * </ul>
     */
    private NormalizedPlacement normalizePlacement(
            StudentBulkImportRow row,
            List<ValidationResult.ValidationError> errors
    ) {
        String rawLevel =
                valueParser.nullableText(
                        row.getPresentEducationLevel()
                );

        String rawClass =
                valueParser.nullableText(
                        row.getPresentClass()
                );

        String canonicalLevel = null;
        String canonicalClass = null;

        if (rawLevel != null) {
            try {
                canonicalLevel =
                        StudentEducationClassNormalizer
                                .normalizeLevel(
                                        rawLevel
                                );
            } catch (IllegalArgumentException exception) {
                addError(
                        errors,
                        StudentExcelHeaders.PRESENT_EDUCATION_LEVEL,
                        row.getPresentEducationLevel(),
                        "STUDENT_EDUCATION_LEVEL_INVALID",
                        exception.getMessage()
                );
            }
        }

        if (rawClass != null) {
            try {
                canonicalClass =
                        StudentEducationClassNormalizer
                                .normalizeClass(
                                        rawClass,
                                        canonicalLevel
                                );
            } catch (IllegalArgumentException exception) {
                addError(
                        errors,
                        StudentExcelHeaders.PRESENT_CLASS,
                        row.getPresentClass(),
                        "STUDENT_CLASS_FORMAT_INVALID",
                        exception.getMessage()
                );
            }
        }

        String inferredLevel = null;

        if (canonicalClass != null) {
            inferredLevel =
                    StudentEducationClassNormalizer
                            .inferLevelFromClass(
                                    canonicalClass
                            );
        }

        if (
                canonicalLevel != null
                        && inferredLevel != null
                        && !canonicalLevel.equals(
                        inferredLevel
                )
        ) {
            addError(
                    errors,
                    StudentExcelHeaders.PRESENT_CLASS,
                    row.getPresentClass(),
                    "STUDENT_LEVEL_CLASS_MISMATCH",
                    "Class "
                            + canonicalClass
                            + " belongs to "
                            + inferredLevel
                            + ", but Education Level is "
                            + canonicalLevel
                            + "."
            );

            return new NormalizedPlacement(
                    canonicalLevel,
                    canonicalClass,
                    true
            );
        }

        if (canonicalLevel == null) {
            canonicalLevel = inferredLevel;
        }

        return new NormalizedPlacement(
                canonicalLevel,
                canonicalClass,
                false
        );
    }

    /**
     * Blank Education Level is inferred from Class when possible. When both
     * are blank, the branch default level is used.
     */
    private LevelReference resolveLevel(
            StudentBulkImportRow row,
            StudentBulkReferenceData references,
            NormalizedPlacement normalizedPlacement,
            List<ValidationResult.ValidationError> errors
    ) {
        if (normalizedPlacement.conflict()) {
            return null;
        }

        String canonicalLevel =
                normalizedPlacement.educationLevel();

        LevelReference level;

        if (canonicalLevel == null) {
            level = references.findDefaultLevel();
        } else {
            level = references.findLevel(
                    canonicalLevel
            );
        }

        if (level != null) {
            return level;
        }

        if (canonicalLevel == null) {
            addError(
                    errors,
                    StudentExcelHeaders.PRESENT_EDUCATION_LEVEL,
                    row.getPresentEducationLevel(),
                    "STUDENT_EDUCATION_LEVEL_NOT_CONFIGURED",
                    "No active Education Level is configured for this branch."
            );
        } else {
            addError(
                    errors,
                    StudentExcelHeaders.PRESENT_EDUCATION_LEVEL,
                    row.getPresentEducationLevel(),
                    "STUDENT_EDUCATION_LEVEL_NOT_FOUND",
                    "Education Level '"
                            + row.getPresentEducationLevel()
                            + "' was normalized to "
                            + canonicalLevel
                            + ", but that level is not configured for this branch."
            );
        }

        return null;
    }

    /**
     * Resolves the canonical Class inside the selected branch Education
     * Level. Blank Class uses the first active class in that level.
     */
    private ClassReference resolveClass(
            StudentBulkImportRow row,
            StudentBulkReferenceData references,
            LevelReference level,
            NormalizedPlacement normalizedPlacement,
            List<ValidationResult.ValidationError> errors
    ) {
        if (
                level == null
                        || normalizedPlacement.conflict()
        ) {
            return null;
        }

        String canonicalClass =
                normalizedPlacement.classCode();

        ClassReference schoolClass =
                references.findClass(
                        level,
                        canonicalClass
                );

        if (schoolClass != null) {
            return schoolClass;
        }

        if (canonicalClass == null) {
            addError(
                    errors,
                    StudentExcelHeaders.PRESENT_CLASS,
                    row.getPresentClass(),
                    "STUDENT_CLASS_NOT_CONFIGURED",
                    "No active Class is configured under the resolved Education Level."
            );

            return null;
        }

        addError(
                errors,
                StudentExcelHeaders.PRESENT_CLASS,
                row.getPresentClass(),
                "STUDENT_CLASS_NOT_FOUND",
                "Class '"
                        + row.getPresentClass()
                        + "' was normalized to "
                        + canonicalClass
                        + ", but it is not configured under the resolved Education Level. "
                        + acceptedClassMessage(
                        normalizedPlacement.educationLevel()
                )
        );

        return null;
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
                    + "including forms such as P.1, P-1 and Primary 1.";
        }

        if (
                StudentEducationClassNormalizer.LEVEL_SECONDARY
                        .equals(canonicalLevel)
        ) {
            return "Accepted Secondary values include S1 to S4, "
                    + "including forms such as S.1, S-1 and Secondary 1.";
        }

        if (
                StudentEducationClassNormalizer
                        .LEVEL_SENIOR_SECONDARY
                        .equals(canonicalLevel)
        ) {
            return "Accepted Senior Secondary values are S5 and S6, "
                    + "including forms such as S.5 and S-5.";
        }

        return "Use a class configured for the selected Education Level.";
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
                    "Section '"
                            + sectionValue
                            + "' does not belong to the resolved Academic Year and Class."
            );
        }
    }

    private void validateJoiningClass(
            StudentBulkImportRow row,
            StudentBulkReferenceData references,
            List<ValidationResult.ValidationError> errors
    ) {
        String rawJoiningClass =
                valueParser.nullableText(
                        row.getJoiningClass()
                );

        if (rawJoiningClass == null) {
            return;
        }

        String canonicalClass;

        try {
            canonicalClass =
                    StudentEducationClassNormalizer.normalizeClass(
                            rawJoiningClass,
                            null
                    );
        } catch (IllegalArgumentException exception) {
            addError(
                    errors,
                    StudentExcelHeaders.JOINING_CLASS,
                    row.getJoiningClass(),
                    "STUDENT_JOINING_CLASS_INVALID",
                    exception.getMessage()
            );
            return;
        }

        String canonicalLevel =
                StudentEducationClassNormalizer.inferLevelFromClass(
                        canonicalClass
                );

        LevelReference level =
                references.findLevel(canonicalLevel);

        ClassReference joiningClass =
                references.findClass(
                        level,
                        canonicalClass
                );

        if (joiningClass == null) {
            addError(
                    errors,
                    StudentExcelHeaders.JOINING_CLASS,
                    row.getJoiningClass(),
                    "STUDENT_JOINING_CLASS_NOT_FOUND",
                    "Joining Class '"
                            + rawJoiningClass
                            + "' was normalized to "
                            + canonicalClass
                            + ", but it is not configured for this branch."
            );
        }
    }

    private record NormalizedPlacement(
            String educationLevel,
            String classCode,
            boolean conflict
    ) {
    }

    // =====================================================================
    // ADMISSION TYPE
    // =====================================================================

    private void validateAdmissionType(
            StudentBulkImportRow row
    ) {
        try {
            valueParser.requiredAdmissionType(
                    row.getAdmissionType()
            );
        } catch (IllegalArgumentException exception) {
            /*
             * Admission Type is generated internally as NEW.
             * An invalid value therefore indicates a backend mapping
             * problem rather than an editable Excel-cell error.
             */
            throw new IllegalStateException(
                    "Generated Student Admission Type is invalid.",
                    exception
            );
        }
    }

    // =====================================================================
    // ADMISSION DATE
    // =====================================================================

    private void validateAdmissionDate(
            StudentBulkImportRow row,
            List<ValidationResult.ValidationError> errors
    ) {
        LocalDate joiningDate;

        try {
            joiningDate =
                    valueParser.nullableDate(
                            row.getAdmissionDate(),
                            StudentExcelHeaders.ADMISSION_DATE
                    );
        } catch (IllegalArgumentException exception) {
            /*
             * Admission Date is entered separately from Admission Year.
             * The correction must point to the Admission Date column.
             */
            addError(
                    errors,
                    StudentExcelHeaders.ADMISSION_DATE,
                    row.getAdmissionDate(),
                    "STUDENT_ADMISSION_DATE_INVALID",
                    "Admission Date must contain a supported date."
            );
            return;
        }

        if (joiningDate == null) {
            return;
        }

        if (joiningDate.isAfter(LocalDate.now())) {
            addError(
                    errors,
                    StudentExcelHeaders.ADMISSION_DATE,
                    row.getAdmissionDate(),
                    "STUDENT_ADMISSION_DATE_FUTURE",
                    "Admission Date cannot be in the future."
            );
        }
    }

    private void validateAdmissionYearDateConsistency(
            StudentBulkImportRow row,
            List<ValidationResult.ValidationError> errors
    ) {
        Integer admissionYear;
        LocalDate admissionDate;

        try {
            admissionYear =
                    valueParser.nullableInteger(
                            row.getAdmissionYear(),
                            StudentExcelHeaders.ADMISSION_YEAR
                    );

            admissionDate =
                    valueParser.nullableDate(
                            row.getAdmissionDate(),
                            StudentExcelHeaders.ADMISSION_DATE
                    );
        } catch (IllegalArgumentException exception) {
            return;
        }

        if (
                admissionYear != null
                        && admissionDate != null
                        && admissionYear != admissionDate.getYear()
        ) {
            addError(
                    errors,
                    StudentExcelHeaders.ADMISSION_DATE,
                    row.getAdmissionDate(),
                    "STUDENT_ADMISSION_YEAR_DATE_MISMATCH",
                    "Admission Date must fall inside Admission Year "
                            + admissionYear
                            + "."
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
                            row.getAdmissionDate(),
                            StudentExcelHeaders.ADMISSION_DATE
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
                    StudentExcelHeaders.DATE_OF_BIRTH,
                    row.getDateOfBirth(),
                    "STUDENT_DATE_ORDER_INVALID",
                    "Date of Birth must be before the Admission Date."
            );
        }
    }

    // =====================================================================
    // PARENT / GUARDIAN
    // =====================================================================

    private void validateParentAndGuardian(
            StudentBulkImportRow row
    ) {
        /*
         * Parent and Guardian Excel cells are optional.
         *
         * Preferred Contact, Fee Responsibility and
         * Parents Living Together are generated by the backend mapper.
         * Therefore, a blank Parent/Guardian group does not invalidate
         * the Student row.
         */
        String preferredContactValue =
                valueParser.nullableText(
                        row.getPreferredContact()
                );

        if (preferredContactValue == null) {
            return;
        }

        try {
            valueParser.nullablePreferredContact(
                    preferredContactValue
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "Generated Student Preferred Contact is invalid.",
                    exception
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
        validateOptionalMobile(
                row.getMobileNumber(),
                errors
        );

        validateOptionalAlternateMobile(
                row.getAlternateMobile(),
                errors
        );
    }

    private void validateOptionalMobile(
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
                row.getAdmissionYear(),
                StudentExcelHeaders.ADMISSION_YEAR,
                4,
                errors
        );

        validateOptionalLength(
                row.getAdmissionDate(),
                StudentExcelHeaders.ADMISSION_DATE,
                30,
                errors
        );

        validateOptionalLength(
                row.getJoiningClass(),
                StudentExcelHeaders.JOINING_CLASS,
                100,
                errors
        );

        validateOptionalLength(
                row.getJoinedTerm(),
                StudentExcelHeaders.JOINED_TERM,
                100,
                errors
        );

        validateOptionalLength(
                row.getPresentTerm(),
                StudentExcelHeaders.PRESENT_TERM,
                100,
                errors
        );

        validateOptionalLength(
                row.getPresentEducationLevel(),
                StudentExcelHeaders.PRESENT_EDUCATION_LEVEL,
                100,
                errors
        );

        validateOptionalLength(
                row.getPresentClass(),
                StudentExcelHeaders.PRESENT_CLASS,
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
                row.getFatherName(),
                StudentExcelHeaders.FATHER,
                150,
                errors
        );

        validateOptionalLength(
                row.getMotherName(),
                StudentExcelHeaders.MOTHER,
                150,
                errors
        );

        validateOptionalLength(
                row.getGuardianName(),
                StudentExcelHeaders.GUARDIAN_NAME,
                150,
                errors
        );

        validateOptionalLength(
                row.getPresentResponsiblePerson(),
                StudentExcelHeaders.PRESENT_RESPONSIBLE_PERSON,
                30,
                errors
        );

        validateOptionalLength(
                row.getGuardianRelationship(),
                StudentExcelHeaders.GUARDIAN_RELATION,
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
                    "Student",
                    buildStudentDisplayName(row),
                    "STUDENT_DUPLICATE_INSIDE_WORKBOOK",
                    "The same Student full name, Date of Birth and Mobile No "
                            + "are duplicated inside the workbook."
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
                normalizeDuplicateDate(
                        row.getDateOfBirth()
                );

        String mobile =
                normalizeDuplicatePhone(
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

    private String normalizeDuplicateDate(
            String value
    ) {
        try {
            LocalDate parsedDate =
                    valueParser.nullableDate(
                            value,
                            StudentExcelHeaders.DATE_OF_BIRTH
                    );

            return parsedDate == null
                    ? null
                    : parsedDate.toString();
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String normalizeDuplicatePhone(
            String value
    ) {
        String normalized =
                valueParser.nullableText(
                        value
                );

        if (normalized == null) {
            return null;
        }

        String digits =
                normalized.replaceAll(
                        "\\D",
                        ""
                );

        if (digits.isBlank()) {
            return null;
        }

        if (
                digits.length() == 10
                        && digits.startsWith("0")
        ) {
            digits =
                    "256"
                            + digits.substring(1);
        }

        return digits;
    }

    private String buildStudentDisplayName(
            StudentBulkImportRow row
    ) {
        String firstName =
                valueOrEmpty(
                        valueParser.nullableText(
                                row.getFirstName()
                        )
                );

        String middleName =
                valueOrEmpty(
                        valueParser.nullableText(
                                row.getMiddleName()
                        )
                );

        String lastName =
                valueOrEmpty(
                        valueParser.nullableText(
                                row.getLastName()
                        )
                );

        String fullName =
                String.join(
                                " ",
                                firstName,
                                middleName,
                                lastName
                        )
                        .trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        );

        return fullName.isBlank()
                ? firstName
                : fullName;
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
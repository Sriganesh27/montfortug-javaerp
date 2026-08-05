package com.erp.montfortuganda.student.bulkimport.excel;

import com.erp.montfortuganda.common.importframework.plugin.ExcelRowMapper;
import com.erp.montfortuganda.student.bulkimport.dto.StudentBulkImportRow;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Maps the approved Student workbook into raw import values.
 *
 * <p>The current workbook has separate Admission Year and Admission Date
 * columns. The former combined "Joining Date / Year" header is accepted by
 * the plugin as an alias of Admission Year; this mapper safely separates a
 * legacy full date from a four-digit year.</p>
 */
@Component
public class StudentExcelRowMapper
        implements ExcelRowMapper<StudentBulkImportRow> {

    private static final String DEFAULT_ADMISSION_TYPE =
            "NEW";

    private static final String DEFAULT_PARENTS_LIVING_TOGETHER =
            "YES";

    private static final Pattern EXACT_FOUR_DIGIT_YEAR =
            Pattern.compile("^(19\\d{2}|20\\d{2}|2100)$");

    private static final Pattern EMBEDDED_FOUR_DIGIT_YEAR =
            Pattern.compile("(?<!\\d)(19\\d{2}|20\\d{2}|2100)(?!\\d)");

    @Override
    public StudentBulkImportRow mapRow(
            Object rowData,
            int rowNumber
    ) {
        if (!(rowData instanceof Map<?, ?> rawRow)) {
            throw new IllegalArgumentException(
                    "Student import row must be a header-value map."
            );
        }

        AdmissionValues admissionValues =
                resolveAdmissionValues(
                        value(
                                rawRow,
                                StudentExcelHeaders.ADMISSION_YEAR
                        ),
                        value(
                                rawRow,
                                StudentExcelHeaders.ADMISSION_DATE
                        )
                );

        String fatherName =
                value(rawRow, StudentExcelHeaders.FATHER);

        String motherName =
                value(rawRow, StudentExcelHeaders.MOTHER);

        String guardianName =
                value(rawRow, StudentExcelHeaders.GUARDIAN_NAME);

        String responsiblePerson =
                resolveResponsiblePerson(
                        value(
                                rawRow,
                                StudentExcelHeaders.PRESENT_RESPONSIBLE_PERSON
                        ),
                        fatherName,
                        motherName,
                        guardianName
                );

        return StudentBulkImportRow.builder()
                .excelRowNumber(rowNumber)
                .admissionYear(admissionValues.year())
                .admissionDate(admissionValues.date())
                .joiningClass(
                        value(rawRow, StudentExcelHeaders.JOINING_CLASS)
                )
                .joinedTerm(
                        value(rawRow, StudentExcelHeaders.JOINED_TERM)
                )
                .firstName(
                        value(rawRow, StudentExcelHeaders.FIRST_NAME)
                )
                .middleName(
                        value(rawRow, StudentExcelHeaders.MIDDLE_NAME)
                )
                .lastName(
                        value(rawRow, StudentExcelHeaders.LAST_NAME)
                )
                .gender(
                        value(rawRow, StudentExcelHeaders.GENDER)
                )
                .dateOfBirth(
                        value(rawRow, StudentExcelHeaders.DATE_OF_BIRTH)
                )
                .presentEducationLevel(
                        value(
                                rawRow,
                                StudentExcelHeaders.PRESENT_EDUCATION_LEVEL
                        )
                )
                .presentClass(
                        value(rawRow, StudentExcelHeaders.PRESENT_CLASS)
                )
                .presentTerm(
                        value(rawRow, StudentExcelHeaders.PRESENT_TERM)
                )
                .section(
                        value(rawRow, StudentExcelHeaders.SECTION)
                )
                .academicYear(
                        value(rawRow, StudentExcelHeaders.ACADEMIC_YEAR)
                )
                .admissionType(DEFAULT_ADMISSION_TYPE)
                .fatherName(fatherName)
                .motherName(motherName)
                .guardianName(guardianName)
                .guardianRelationship(
                        value(rawRow, StudentExcelHeaders.GUARDIAN_RELATION)
                )
                .presentResponsiblePerson(responsiblePerson)
                .mobileNumber(
                        value(rawRow, StudentExcelHeaders.MOBILE_NUMBER)
                )
                .alternateMobile(
                        value(rawRow, StudentExcelHeaders.ALTERNATE_MOBILE)
                )
                .email(
                        value(rawRow, StudentExcelHeaders.EMAIL)
                )
                .preferredContact(responsiblePerson)
                .feeResponsibility(responsiblePerson)
                .parentsLivingTogether(
                        DEFAULT_PARENTS_LIVING_TOGETHER
                )
                .nationality(
                        value(rawRow, StudentExcelHeaders.NATIONALITY)
                )
                .nationalIdOrPassport(
                        value(
                                rawRow,
                                StudentExcelHeaders.NATIONAL_ID_OR_PASSPORT
                        )
                )
                .addressCountry(
                        value(rawRow, StudentExcelHeaders.ADDRESS_COUNTRY)
                )
                .state(
                        value(rawRow, StudentExcelHeaders.STATE)
                )
                .district(
                        value(rawRow, StudentExcelHeaders.DISTRICT)
                )
                .county(
                        value(rawRow, StudentExcelHeaders.COUNTY)
                )
                .subCounty(
                        value(rawRow, StudentExcelHeaders.SUB_COUNTY)
                )
                .parish(
                        value(rawRow, StudentExcelHeaders.PARISH)
                )
                .village(
                        value(rawRow, StudentExcelHeaders.VILLAGE)
                )
                .street(
                        value(rawRow, StudentExcelHeaders.STREET)
                )
                .previousSchool(
                        value(rawRow, StudentExcelHeaders.PREVIOUS_SCHOOL)
                )
                .religion(
                        value(rawRow, StudentExcelHeaders.RELIGION)
                )
                .bloodGroup(
                        value(rawRow, StudentExcelHeaders.BLOOD_GROUP)
                )
                .transportRequired(
                        value(
                                rawRow,
                                StudentExcelHeaders.TRANSPORT_REQUIRED
                        )
                )
                .hostelRequired(
                        value(rawRow, StudentExcelHeaders.HOSTEL_REQUIRED)
                )
                .scholarship(
                        value(rawRow, StudentExcelHeaders.SCHOLARSHIP)
                )
                .medicalConditions(
                        value(rawRow, StudentExcelHeaders.MEDICAL_CONDITIONS)
                )
                .remarks(
                        value(rawRow, StudentExcelHeaders.REMARKS)
                )
                .build();
    }

    /**
     * Separates the legacy combined value without inventing a date.
     */
    private AdmissionValues resolveAdmissionValues(
            String rawAdmissionYear,
            String rawAdmissionDate
    ) {
        String admissionYear = rawAdmissionYear;
        String admissionDate = rawAdmissionDate;

        if (hasText(admissionDate)) {
            if (!hasText(admissionYear)) {
                admissionYear = extractFourDigitYear(admissionDate);
            }

            return new AdmissionValues(
                    admissionYear,
                    admissionDate
            );
        }

        if (!hasText(admissionYear)) {
            return new AdmissionValues(null, null);
        }

        String trimmed = admissionYear.trim();

        if (EXACT_FOUR_DIGIT_YEAR.matcher(trimmed).matches()) {
            return new AdmissionValues(trimmed, null);
        }

        /*
         * The old "Joining Date / Year" column is canonicalized as
         * Admission Year. A non-year value in that column is therefore
         * treated as the legacy admission date and validated later.
         */
        return new AdmissionValues(
                extractFourDigitYear(trimmed),
                trimmed
        );
    }

    private String extractFourDigitYear(
            String value
    ) {
        if (!hasText(value)) {
            return null;
        }

        Matcher matcher =
                EMBEDDED_FOUR_DIGIT_YEAR.matcher(value);

        return matcher.find()
                ? matcher.group()
                : null;
    }

    private String resolveResponsiblePerson(
            String rawResponsiblePerson,
            String fatherName,
            String motherName,
            String guardianName
    ) {
        String normalized =
                normalizeResponsiblePerson(rawResponsiblePerson);

        if ("FATHER".equals(normalized)) {
            return hasText(fatherName) ? "FATHER" : null;
        }

        if ("MOTHER".equals(normalized)) {
            return hasText(motherName) ? "MOTHER" : null;
        }

        if ("GUARDIAN".equals(normalized)) {
            return hasText(guardianName) ? "GUARDIAN" : null;
        }

        if (hasText(fatherName)) {
            return "FATHER";
        }

        if (hasText(motherName)) {
            return "MOTHER";
        }

        if (hasText(guardianName)) {
            return "GUARDIAN";
        }

        return null;
    }

    private String normalizeResponsiblePerson(
            String value
    ) {
        if (!hasText(value)) {
            return null;
        }

        String token =
                value.trim()
                        .toUpperCase(Locale.ROOT)
                        .replace('-', ' ')
                        .replace('_', ' ')
                        .replaceAll("\\s+", " ");

        return switch (token) {
            case "FATHER", "DAD" -> "FATHER";
            case "MOTHER", "MOM", "MUM" -> "MOTHER";
            case "GUARDIAN", "CARETAKER" -> "GUARDIAN";
            default -> null;
        };
    }

    private String value(
            Map<?, ?> row,
            String header
    ) {
        Object rawValue = row.get(header);

        if (rawValue == null) {
            return null;
        }

        String normalizedValue =
                rawValue.toString().trim();

        return normalizedValue.isEmpty()
                ? null
                : normalizedValue;
    }

    private boolean hasText(
            String value
    ) {
        return value != null && !value.isBlank();
    }

    private record AdmissionValues(
            String year,
            String date
    ) {
    }
}

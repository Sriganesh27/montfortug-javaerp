package com.erp.montfortuganda.student.bulkimport.excel;

import com.erp.montfortuganda.common.importframework.plugin.ExcelRowMapper;
import com.erp.montfortuganda.student.bulkimport.dto.StudentBulkImportRow;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

/**
 * Maps the existing approved 36-column Student workbook.
 *
 * Fields required by the Student backend but unavailable in Excel are
 * generated internally without changing the workbook or database.
 */
@Component
public class StudentExcelRowMapper
        implements ExcelRowMapper<StudentBulkImportRow> {

        private static final String DEFAULT_ADMISSION_TYPE =
                "NEW";

        private static final String DEFAULT_PARENTS_LIVING_TOGETHER =
                "YES";

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

                String admissionYear =
                        value(
                                rawRow,
                                StudentExcelHeaders.ADMISSION_YEAR
                        );

                String fatherOrGuardianName =
                        value(
                                rawRow,
                                StudentExcelHeaders
                                        .FATHER_OR_GUARDIAN_NAME
                        );

                String motherOrGuardianName =
                        value(
                                rawRow,
                                StudentExcelHeaders
                                        .MOTHER_OR_GUARDIAN_NAME
                        );

                String guardianRelationship =
                        value(
                                rawRow,
                                StudentExcelHeaders
                                        .GUARDIAN_RELATIONSHIP
                        );

                String preferredContact =
                        resolvePreferredContact(
                                fatherOrGuardianName,
                                motherOrGuardianName,
                                guardianRelationship
                        );

                return StudentBulkImportRow.builder()
                        .excelRowNumber(rowNumber)

                        // Personal information
                        .admissionNo(
                                value(
                                        rawRow,
                                        StudentExcelHeaders.ADMISSION_NO
                                )
                        )
                        .admissionYear(admissionYear)
                        .firstName(
                                value(
                                        rawRow,
                                        StudentExcelHeaders.FIRST_NAME
                                )
                        )
                        .middleName(
                                value(
                                        rawRow,
                                        StudentExcelHeaders.MIDDLE_NAME
                                )
                        )
                        .lastName(
                                value(
                                        rawRow,
                                        StudentExcelHeaders.LAST_NAME
                                )
                        )
                        .gender(
                                value(
                                        rawRow,
                                        StudentExcelHeaders.GENDER
                                )
                        )
                        .dateOfBirth(
                                value(
                                        rawRow,
                                        StudentExcelHeaders.DATE_OF_BIRTH
                                )
                        )
                        .educationLevel(
                                value(
                                        rawRow,
                                        StudentExcelHeaders.EDUCATION_LEVEL
                                )
                        )
                        .className(
                                value(
                                        rawRow,
                                        StudentExcelHeaders.CLASS_NAME
                                )
                        )
                        .section(
                                value(
                                        rawRow,
                                        StudentExcelHeaders.SECTION
                                )
                        )
                        .academicYear(
                                value(
                                        rawRow,
                                        StudentExcelHeaders.ACADEMIC_YEAR
                                )
                        )

                        /*
                         * These values are required by the backend but are not
                         * available in the approved Excel workbook.
                         */
                        .admissionType(
                                DEFAULT_ADMISSION_TYPE
                        )
                        .joiningDate(
                                buildJoiningDate(admissionYear)
                        )

                        // Parent and guardian
                        .fatherOrGuardianName(
                                fatherOrGuardianName
                        )
                        .motherOrGuardianName(
                                motherOrGuardianName
                        )
                        .guardianRelationship(
                                guardianRelationship
                        )
                        .mobileNumber(
                                value(
                                        rawRow,
                                        StudentExcelHeaders.MOBILE_NUMBER
                                )
                        )
                        .alternateMobile(
                                value(
                                        rawRow,
                                        StudentExcelHeaders.ALTERNATE_MOBILE
                                )
                        )
                        .email(
                                value(
                                        rawRow,
                                        StudentExcelHeaders.EMAIL
                                )
                        )
                        .preferredContact(
                                preferredContact
                        )
                        .feeResponsibility(
                                preferredContact
                        )
                        .parentsLivingTogether(
                                DEFAULT_PARENTS_LIVING_TOGETHER
                        )

                        // Nationality and address
                        .nationality(
                                value(
                                        rawRow,
                                        StudentExcelHeaders.NATIONALITY
                                )
                        )
                        .nationalIdOrPassport(
                                value(
                                        rawRow,
                                        StudentExcelHeaders
                                                .NATIONAL_ID_OR_PASSPORT
                                )
                        )
                        .addressCountry(
                                value(
                                        rawRow,
                                        StudentExcelHeaders.ADDRESS_COUNTRY
                                )
                        )
                        .state(
                                value(
                                        rawRow,
                                        StudentExcelHeaders.STATE
                                )
                        )
                        .district(
                                value(
                                        rawRow,
                                        StudentExcelHeaders.DISTRICT
                                )
                        )
                        .county(
                                value(
                                        rawRow,
                                        StudentExcelHeaders.COUNTY
                                )
                        )
                        .subCounty(
                                value(
                                        rawRow,
                                        StudentExcelHeaders.SUB_COUNTY
                                )
                        )
                        .parish(
                                value(
                                        rawRow,
                                        StudentExcelHeaders.PARISH
                                )
                        )
                        .village(
                                value(
                                        rawRow,
                                        StudentExcelHeaders.VILLAGE
                                )
                        )
                        .street(
                                value(
                                        rawRow,
                                        StudentExcelHeaders.STREET
                                )
                        )

                        // Previous education and medical information
                        .previousSchool(
                                value(
                                        rawRow,
                                        StudentExcelHeaders.PREVIOUS_SCHOOL
                                )
                        )
                        .religion(
                                value(
                                        rawRow,
                                        StudentExcelHeaders.RELIGION
                                )
                        )
                        .bloodGroup(
                                value(
                                        rawRow,
                                        StudentExcelHeaders.BLOOD_GROUP
                                )
                        )
                        .transportRequired(
                                value(
                                        rawRow,
                                        StudentExcelHeaders.TRANSPORT_REQUIRED
                                )
                        )
                        .hostelRequired(
                                value(
                                        rawRow,
                                        StudentExcelHeaders.HOSTEL_REQUIRED
                                )
                        )
                        .scholarship(
                                value(
                                        rawRow,
                                        StudentExcelHeaders.SCHOLARSHIP
                                )
                        )
                        .medicalConditions(
                                value(
                                        rawRow,
                                        StudentExcelHeaders.MEDICAL_CONDITIONS
                                )
                        )
                        .remarks(
                                value(
                                        rawRow,
                                        StudentExcelHeaders.REMARKS
                                )
                        )
                        .build();
        }

        /**
         * Creates a deterministic backend joining date without parsing the raw
         * Excel value into LocalDate at the reading stage.
         */
        private String buildJoiningDate(
                String admissionYear
        ) {
                if (
                        admissionYear == null
                                || !admissionYear.matches("\\d{4}")
                ) {
                        return null;
                }

                return admissionYear + "-01-01";
        }

        /**
         * Determines which contact receives the workbook's single phone and
         * email channel.
         */
        private String resolvePreferredContact(
                String fatherOrGuardianName,
                String motherOrGuardianName,
                String guardianRelationship
        ) {
                String relationship =
                        normalizeToken(
                                guardianRelationship
                        );

                if ("FATHER".equals(relationship)) {
                        return hasText(fatherOrGuardianName)
                                ? "FATHER"
                                : null;
                }

                if ("MOTHER".equals(relationship)) {
                        return hasText(motherOrGuardianName)
                                ? "MOTHER"
                                : null;
                }

                if (relationship != null) {
                        return hasText(fatherOrGuardianName)
                                || hasText(motherOrGuardianName)
                                ? "GUARDIAN"
                                : null;
                }

                if (hasText(fatherOrGuardianName)) {
                        return "FATHER";
                }

                if (hasText(motherOrGuardianName)) {
                        return "MOTHER";
                }

                return null;
        }

        private String normalizeToken(
                String value
        ) {
                if (!hasText(value)) {
                        return null;
                }

                return value.trim()
                        .toUpperCase(Locale.ROOT)
                        .replace(' ', '_')
                        .replace('-', '_');
        }

        /**
         * Reads one Excel cell while preserving all values as Strings.
         */
        private String value(
                Map<?, ?> row,
                String header
        ) {
                Object rawValue = row.get(header);

                if (rawValue == null) {
                        return null;
                }

                String normalizedValue =
                        rawValue.toString()
                                .trim();

                return normalizedValue.isEmpty()
                        ? null
                        : normalizedValue;
        }

        private boolean hasText(
                String value
        ) {
                return value != null
                        && !value.isBlank();
        }
}
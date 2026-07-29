package com.erp.montfortuganda.student.bulkimport.excel;

import com.erp.montfortuganda.common.importframework.plugin.ExcelRowMapper;
import com.erp.montfortuganda.student.bulkimport.dto.StudentBulkImportRow;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Converts one Excel Student row into StudentBulkImportRow.
 *
 * All values remain Strings during Excel reading. Dates, numbers,
 * enums and database references are converted only after validation.
 */
@Component
public class StudentExcelRowMapper
        implements ExcelRowMapper<StudentBulkImportRow> {

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

        return StudentBulkImportRow.builder()
                .excelRowNumber(rowNumber)

                // Personal information
                .admissionNo(
                        value(
                                rawRow,
                                StudentExcelHeaders.ADMISSION_NO
                        )
                )
                .admissionYear(
                        value(
                                rawRow,
                                StudentExcelHeaders.ADMISSION_YEAR
                        )
                )
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

                // Branch and enrollment
                .branch(
                        value(
                                rawRow,
                                StudentExcelHeaders.BRANCH
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
                .admissionType(
                        value(
                                rawRow,
                                StudentExcelHeaders.ADMISSION_TYPE
                        )
                )
                .joiningDate(
                        value(
                                rawRow,
                                StudentExcelHeaders.JOINING_DATE
                        )
                )

                // Parent and guardian
                .fatherOrGuardianName(
                        value(
                                rawRow,
                                StudentExcelHeaders
                                        .FATHER_OR_GUARDIAN_NAME
                        )
                )
                .motherOrGuardianName(
                        value(
                                rawRow,
                                StudentExcelHeaders
                                        .MOTHER_OR_GUARDIAN_NAME
                        )
                )
                .guardianRelationship(
                        value(
                                rawRow,
                                StudentExcelHeaders
                                        .GUARDIAN_RELATIONSHIP
                        )
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
                        value(
                                rawRow,
                                StudentExcelHeaders.PREFERRED_CONTACT
                        )
                )
                .feeResponsibility(
                        value(
                                rawRow,
                                StudentExcelHeaders.FEE_RESPONSIBILITY
                        )
                )
                .parentsLivingTogether(
                        value(
                                rawRow,
                                StudentExcelHeaders
                                        .PARENTS_LIVING_TOGETHER
                        )
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

                // Previous education and medical details
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
     * Reads one Excel cell and returns a normalized String.
     *
     * Blank cells are returned as null.
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
}
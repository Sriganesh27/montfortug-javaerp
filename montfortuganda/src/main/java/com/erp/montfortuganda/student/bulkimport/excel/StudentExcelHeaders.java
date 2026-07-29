package com.erp.montfortuganda.student.bulkimport.excel;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Exact header contract for the approved Student bulk-import workbook.
 *
 * The Student Excel template contains the base columns and these additional
 * backend-required columns:
 *
 * 1. Admission Type
 * 2. Joining Date
 * 3. Preferred Contact
 * 4. Fee Responsibility
 * 5. Parents Living Together
 *
 * Do not rename or reorder these constants unless the approved Excel
 * template is changed at the same time.
 */
public final class StudentExcelHeaders {

    public static final String STUDENT_SHEET =
            "Students Import";

    public static final String REFERENCE_LISTS_SHEET =
            "Reference Lists";

    // =====================================================================
    // STUDENT IDENTIFICATION AND PERSONAL INFORMATION
    // =====================================================================

    public static final String ADMISSION_NO =
            "Admission No (Optional)";

    public static final String ADMISSION_YEAR =
            "Admission Year";

    public static final String FIRST_NAME =
            "First Name";

    public static final String MIDDLE_NAME =
            "Middle Name";

    public static final String LAST_NAME =
            "Last Name";

    public static final String GENDER =
            "Gender";

    public static final String DATE_OF_BIRTH =
            "Date of Birth";

    // =====================================================================
    // BRANCH AND CURRENT ENROLLMENT
    // =====================================================================

    /**
     * Used only to verify the row against the authenticated branch.
     * The importer must never trust this value to select another branch.
     */
    public static final String BRANCH =
            "Branch";

    public static final String EDUCATION_LEVEL =
            "Education Level";

    public static final String CLASS_NAME =
            "Class";

    public static final String SECTION =
            "Section";

    public static final String ACADEMIC_YEAR =
            "Academic Year";

    public static final String ADMISSION_TYPE =
            "Admission Type";

    public static final String JOINING_DATE =
            "Joining Date (YYYY-MM-DD)";

    // =====================================================================
    // PARENT / GUARDIAN CONTACT
    // =====================================================================

    public static final String FATHER_OR_GUARDIAN_NAME =
            "Father/Guardian Name";

    public static final String MOTHER_OR_GUARDIAN_NAME =
            "Mother/Guardian Name";

    public static final String GUARDIAN_RELATIONSHIP =
            "Guardian Relationship";

    public static final String MOBILE_NUMBER =
            "Mobile No";

    public static final String ALTERNATE_MOBILE =
            "Alternate Mobile";

    public static final String EMAIL =
            "Email";

    public static final String PREFERRED_CONTACT =
            "Preferred Contact";

    public static final String FEE_RESPONSIBILITY =
            "Fee Responsibility";

    public static final String PARENTS_LIVING_TOGETHER =
            "Parents Living Together (Yes/No)";

    // =====================================================================
    // NATIONALITY AND ADDRESS
    // =====================================================================

    public static final String NATIONALITY =
            "Nationality";

    public static final String NATIONAL_ID_OR_PASSPORT =
            "National ID/Passport";

    public static final String ADDRESS_COUNTRY =
            "Address Country";

    public static final String STATE =
            "State";

    public static final String DISTRICT =
            "District";

    public static final String COUNTY =
            "County";

    public static final String SUB_COUNTY =
            "Sub County";

    public static final String PARISH =
            "Parish";

    public static final String VILLAGE =
            "Village";

    public static final String STREET =
            "Street";

    // =====================================================================
    // PREVIOUS EDUCATION, RELIGION AND MEDICAL INFORMATION
    // =====================================================================

    public static final String PREVIOUS_SCHOOL =
            "Previous School";

    public static final String RELIGION =
            "Religion";

    public static final String BLOOD_GROUP =
            "Blood Group";

    public static final String TRANSPORT_REQUIRED =
            "Transport Required (Yes/No)";

    public static final String HOSTEL_REQUIRED =
            "Hostel Required (Yes/No)";

    public static final String SCHOLARSHIP =
            "Scholarship (Yes/No)";

    public static final String MEDICAL_CONDITIONS =
            "Medical Conditions";

    public static final String REMARKS =
            "Remarks";

    /**
     * Written only to invalid cells in the corrected workbook.
     *
     * This value must never be parsed or persisted to a database date,
     * number or reference column.
     */
    public static final String ENTER_VALID_DATA =
            "ENTER VALID DATA";

    /**
     * Exact final column order for the Student import workbook.
     */
    public static final List<String> ALL_HEADERS = List.of(
            ADMISSION_NO,
            ADMISSION_YEAR,
            FIRST_NAME,
            MIDDLE_NAME,
            LAST_NAME,
            GENDER,
            DATE_OF_BIRTH,
            BRANCH,
            EDUCATION_LEVEL,
            CLASS_NAME,
            SECTION,
            ACADEMIC_YEAR,
            ADMISSION_TYPE,
            JOINING_DATE,
            FATHER_OR_GUARDIAN_NAME,
            MOTHER_OR_GUARDIAN_NAME,
            GUARDIAN_RELATIONSHIP,
            MOBILE_NUMBER,
            ALTERNATE_MOBILE,
            EMAIL,
            PREFERRED_CONTACT,
            FEE_RESPONSIBILITY,
            PARENTS_LIVING_TOGETHER,
            NATIONALITY,
            NATIONAL_ID_OR_PASSPORT,
            ADDRESS_COUNTRY,
            STATE,
            DISTRICT,
            COUNTY,
            SUB_COUNTY,
            PARISH,
            VILLAGE,
            STREET,
            PREVIOUS_SCHOOL,
            RELIGION,
            BLOOD_GROUP,
            TRANSPORT_REQUIRED,
            HOSTEL_REQUIRED,
            SCHOLARSHIP,
            MEDICAL_CONDITIONS,
            REMARKS
    );

    /**
     * Columns that must be present in the Excel template and must contain
     * valid row values.
     *
     * Parent or Guardian name is a conditional requirement and is validated
     * by StudentBulkImportValidator.
     */
    public static final Set<String> REQUIRED_HEADERS = Set.of(
            ADMISSION_YEAR,
            FIRST_NAME,
            GENDER,
            DATE_OF_BIRTH,
            BRANCH,
            EDUCATION_LEVEL,
            CLASS_NAME,
            ACADEMIC_YEAR,
            ADMISSION_TYPE,
            JOINING_DATE,
            MOBILE_NUMBER,
            PREFERRED_CONTACT,
            FEE_RESPONSIBILITY,
            PARENTS_LIVING_TOGETHER
    );

    /**
     * Date fields must remain Strings while being read from Excel.
     *
     * They are converted to LocalDate only after successful validation.
     */
    public static final Set<String> DATE_HEADERS = Set.of(
            DATE_OF_BIRTH,
            JOINING_DATE
    );

    /**
     * Columns accepting Yes or No values.
     */
    public static final Set<String> YES_NO_HEADERS = Set.of(
            PARENTS_LIVING_TOGETHER,
            TRANSPORT_REQUIRED,
            HOSTEL_REQUIRED,
            SCHOLARSHIP
    );

    /**
     * Columns populated from fixed values or reference-data dropdowns.
     */
    public static final Set<String> DROPDOWN_HEADERS = Set.of(
            GENDER,
            BRANCH,
            EDUCATION_LEVEL,
            CLASS_NAME,
            SECTION,
            ACADEMIC_YEAR,
            ADMISSION_TYPE,
            GUARDIAN_RELATIONSHIP,
            PREFERRED_CONTACT,
            FEE_RESPONSIBILITY,
            PARENTS_LIVING_TOGETHER,
            RELIGION,
            BLOOD_GROUP,
            TRANSPORT_REQUIRED,
            HOSTEL_REQUIRED,
            SCHOLARSHIP
    );

    /**
     * Branch-scoped or master-data fields resolved by name or code to
     * internal database IDs.
     */
    public static final Set<String> REFERENCE_HEADERS = Set.of(
            BRANCH,
            EDUCATION_LEVEL,
            CLASS_NAME,
            SECTION,
            ACADEMIC_YEAR,
            RELIGION
    );

    /**
     * Zero-based positions matching ALL_HEADERS.
     */
    public static final Map<String, Integer> COLUMN_INDEX =
            Map.ofEntries(
                    Map.entry(ADMISSION_NO, 0),
                    Map.entry(ADMISSION_YEAR, 1),
                    Map.entry(FIRST_NAME, 2),
                    Map.entry(MIDDLE_NAME, 3),
                    Map.entry(LAST_NAME, 4),
                    Map.entry(GENDER, 5),
                    Map.entry(DATE_OF_BIRTH, 6),
                    Map.entry(BRANCH, 7),
                    Map.entry(EDUCATION_LEVEL, 8),
                    Map.entry(CLASS_NAME, 9),
                    Map.entry(SECTION, 10),
                    Map.entry(ACADEMIC_YEAR, 11),
                    Map.entry(ADMISSION_TYPE, 12),
                    Map.entry(JOINING_DATE, 13),
                    Map.entry(FATHER_OR_GUARDIAN_NAME, 14),
                    Map.entry(MOTHER_OR_GUARDIAN_NAME, 15),
                    Map.entry(GUARDIAN_RELATIONSHIP, 16),
                    Map.entry(MOBILE_NUMBER, 17),
                    Map.entry(ALTERNATE_MOBILE, 18),
                    Map.entry(EMAIL, 19),
                    Map.entry(PREFERRED_CONTACT, 20),
                    Map.entry(FEE_RESPONSIBILITY, 21),
                    Map.entry(PARENTS_LIVING_TOGETHER, 22),
                    Map.entry(NATIONALITY, 23),
                    Map.entry(NATIONAL_ID_OR_PASSPORT, 24),
                    Map.entry(ADDRESS_COUNTRY, 25),
                    Map.entry(STATE, 26),
                    Map.entry(DISTRICT, 27),
                    Map.entry(COUNTY, 28),
                    Map.entry(SUB_COUNTY, 29),
                    Map.entry(PARISH, 30),
                    Map.entry(VILLAGE, 31),
                    Map.entry(STREET, 32),
                    Map.entry(PREVIOUS_SCHOOL, 33),
                    Map.entry(RELIGION, 34),
                    Map.entry(BLOOD_GROUP, 35),
                    Map.entry(TRANSPORT_REQUIRED, 36),
                    Map.entry(HOSTEL_REQUIRED, 37),
                    Map.entry(SCHOLARSHIP, 38),
                    Map.entry(MEDICAL_CONDITIONS, 39),
                    Map.entry(REMARKS, 40)
            );

    public static int columnIndex(
            String header
    ) {
        Integer index =
                COLUMN_INDEX.get(header);

        if (index == null) {
            throw new IllegalArgumentException(
                    "Unknown Student import header: "
                            + header
            );
        }

        return index;
    }

    public static boolean isRequired(
            String header
    ) {
        return REQUIRED_HEADERS.contains(header);
    }

    public static boolean isDateHeader(
            String header
    ) {
        return DATE_HEADERS.contains(header);
    }

    public static boolean isYesNoHeader(
            String header
    ) {
        return YES_NO_HEADERS.contains(header);
    }

    public static boolean isDropdownHeader(
            String header
    ) {
        return DROPDOWN_HEADERS.contains(header);
    }

    public static boolean isReferenceHeader(
            String header
    ) {
        return REFERENCE_HEADERS.contains(header);
    }

    private StudentExcelHeaders() {
        throw new IllegalStateException(
                "StudentExcelHeaders is a utility class."
        );
    }
}
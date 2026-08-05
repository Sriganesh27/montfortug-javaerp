package com.erp.montfortuganda.student.bulkimport.excel;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Physical header contract for the Student bulk-import workbook.
 *
 * <p>Admission Year and Admission Date are separate physical columns.
 * Branch ownership, admission number, audit fields and every other
 * server-managed value are never accepted from Excel.</p>
 *
 * <p>All row cells remain optional. The header structure is controlled by
 * {@code StudentImportPlugin}, which also accepts the former combined
 * "Joining Date / Year" column as a legacy alias.</p>
 */
public final class StudentExcelHeaders {

    public static final String STUDENT_SHEET =
            "Students Import";

    public static final String REFERENCE_LISTS_SHEET =
            "Reference Lists";

    // =====================================================================
    // APPROVED PHYSICAL EXCEL COLUMNS
    // =====================================================================

    public static final String ADMISSION_YEAR =
            "Admission Year";

    public static final String ADMISSION_DATE =
            "Admission Date";

    public static final String JOINING_CLASS =
            "Joining Class";

    public static final String JOINED_TERM =
            "Joined term";

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

    public static final String PRESENT_EDUCATION_LEVEL =
            "Present Education Level";

    public static final String PRESENT_CLASS =
            "Present Class";

    public static final String PRESENT_TERM =
            "Present Term";

    public static final String SECTION =
            "Section";

    public static final String ACADEMIC_YEAR =
            "Academic Year";

    public static final String FATHER =
            "Father";

    public static final String MOTHER =
            "Mother";

    public static final String GUARDIAN_NAME =
            "Guardian Name";

    public static final String GUARDIAN_RELATION =
            "Guardian Relation";

    public static final String PRESENT_RESPONSIBLE_PERSON =
            "Present Responsible Person";

    public static final String MOBILE_NUMBER =
            "Mobile No";

    public static final String ALTERNATE_MOBILE =
            "Alternate Mobile";

    public static final String EMAIL =
            "Email";

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

    // =====================================================================
    // BACKEND LOGICAL LABELS — NOT PHYSICAL EXCEL COLUMNS
    // =====================================================================

    public static final String ADMISSION_TYPE =
            "Admission Type";

    public static final String PREFERRED_CONTACT =
            "Preferred Contact";

    public static final String FEE_RESPONSIBILITY =
            "Fee Responsibility";

    public static final String PARENTS_LIVING_TOGETHER =
            "Parents Living Together";

    public static final String ENTER_VALID_DATA =
            "ENTER VALID DATA";

    /**
     * Exact approved workbook order.
     *
     * <p>Total physical columns: 40. Individual row cells may be blank.</p>
     */
    public static final List<String> ALL_HEADERS = List.of(
            ADMISSION_YEAR,
            ADMISSION_DATE,
            JOINING_CLASS,
            JOINED_TERM,
            FIRST_NAME,
            MIDDLE_NAME,
            LAST_NAME,
            GENDER,
            DATE_OF_BIRTH,
            PRESENT_EDUCATION_LEVEL,
            PRESENT_CLASS,
            PRESENT_TERM,
            SECTION,
            ACADEMIC_YEAR,
            FATHER,
            MOTHER,
            GUARDIAN_NAME,
            GUARDIAN_RELATION,
            PRESENT_RESPONSIBLE_PERSON,
            MOBILE_NUMBER,
            ALTERNATE_MOBILE,
            EMAIL,
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
     * No Student Excel cell is mandatory. Missing database values are
     * resolved by the secure backend defaults before persistence.
     */
    public static final Set<String> REQUIRED_HEADERS = Set.of();

    public static final Set<String> DATE_HEADERS = Set.of(
            ADMISSION_DATE,
            DATE_OF_BIRTH
    );

    public static final Set<String> YES_NO_HEADERS = Set.of(
            TRANSPORT_REQUIRED,
            HOSTEL_REQUIRED,
            SCHOLARSHIP
    );

    public static final Set<String> DROPDOWN_HEADERS = Set.of(
            JOINING_CLASS,
            JOINED_TERM,
            GENDER,
            PRESENT_EDUCATION_LEVEL,
            PRESENT_CLASS,
            PRESENT_TERM,
            SECTION,
            ACADEMIC_YEAR,
            GUARDIAN_RELATION,
            PRESENT_RESPONSIBLE_PERSON,
            RELIGION,
            BLOOD_GROUP,
            TRANSPORT_REQUIRED,
            HOSTEL_REQUIRED,
            SCHOLARSHIP
    );

    public static final Set<String> REFERENCE_HEADERS = Set.of(
            JOINING_CLASS,
            JOINED_TERM,
            PRESENT_EDUCATION_LEVEL,
            PRESENT_CLASS,
            PRESENT_TERM,
            SECTION,
            ACADEMIC_YEAR,
            RELIGION
    );

    public static final Map<String, Integer> COLUMN_INDEX =
            Map.ofEntries(
                    Map.entry(ADMISSION_YEAR, 0),
                    Map.entry(ADMISSION_DATE, 1),
                    Map.entry(JOINING_CLASS, 2),
                    Map.entry(JOINED_TERM, 3),
                    Map.entry(FIRST_NAME, 4),
                    Map.entry(MIDDLE_NAME, 5),
                    Map.entry(LAST_NAME, 6),
                    Map.entry(GENDER, 7),
                    Map.entry(DATE_OF_BIRTH, 8),
                    Map.entry(PRESENT_EDUCATION_LEVEL, 9),
                    Map.entry(PRESENT_CLASS, 10),
                    Map.entry(PRESENT_TERM, 11),
                    Map.entry(SECTION, 12),
                    Map.entry(ACADEMIC_YEAR, 13),
                    Map.entry(FATHER, 14),
                    Map.entry(MOTHER, 15),
                    Map.entry(GUARDIAN_NAME, 16),
                    Map.entry(GUARDIAN_RELATION, 17),
                    Map.entry(PRESENT_RESPONSIBLE_PERSON, 18),
                    Map.entry(MOBILE_NUMBER, 19),
                    Map.entry(ALTERNATE_MOBILE, 20),
                    Map.entry(EMAIL, 21),
                    Map.entry(NATIONALITY, 22),
                    Map.entry(NATIONAL_ID_OR_PASSPORT, 23),
                    Map.entry(ADDRESS_COUNTRY, 24),
                    Map.entry(STATE, 25),
                    Map.entry(DISTRICT, 26),
                    Map.entry(COUNTY, 27),
                    Map.entry(SUB_COUNTY, 28),
                    Map.entry(PARISH, 29),
                    Map.entry(VILLAGE, 30),
                    Map.entry(STREET, 31),
                    Map.entry(PREVIOUS_SCHOOL, 32),
                    Map.entry(RELIGION, 33),
                    Map.entry(BLOOD_GROUP, 34),
                    Map.entry(TRANSPORT_REQUIRED, 35),
                    Map.entry(HOSTEL_REQUIRED, 36),
                    Map.entry(SCHOLARSHIP, 37),
                    Map.entry(MEDICAL_CONDITIONS, 38),
                    Map.entry(REMARKS, 39)
            );

    public static int columnIndex(
            String header
    ) {
        Integer index = COLUMN_INDEX.get(header);

        if (index == null) {
            throw new IllegalArgumentException(
                    "Unknown Student import Excel header: " + header
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

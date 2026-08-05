package com.erp.montfortuganda.employee.bulkimport.excel;

import com.erp.montfortuganda.employee.enums.EmployeeCategory;
import com.erp.montfortuganda.employee.enums.EmployeeType;
import com.erp.montfortuganda.employee.enums.EmploymentMode;
import com.erp.montfortuganda.employee.enums.Gender;
import org.apache.poi.ss.usermodel.DateUtil;
import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts user-friendly Employee Excel values into backend-supported types.
 *
 * <p>This class does not save anything and does not generate Employee
 * numbers. Employee-number generation remains handled by
 * {@code EmployeeNumberService} using the parsed Joining Date year.</p>
 */
@Component
public class EmployeeExcelValueParser {

    /**
     * A four-digit year is accepted only for Employee bulk-import
     * Joining Date. It is converted to 1 January of that year.
     *
     * <p>This rule is not used for Date of Birth and is not used by
     * individual/manual Employee creation.</p>
     */
    private static final Pattern YEAR_ONLY_PATTERN =
            Pattern.compile("^\\d{4}$");

    private static final Pattern NUMERIC_DATE_PATTERN =
            Pattern.compile(
                    "^(\\d{1,4})[./-](\\d{1,2})[./-](\\d{1,4})$"
            );

    private static final Pattern COMPACT_DATE_PATTERN =
            Pattern.compile(
                    "^(\\d{4})(\\d{2})(\\d{2})$"
            );

    private static final Pattern EXCEL_SERIAL_PATTERN =
            Pattern.compile(
                    "^\\d{5}(?:\\.\\d+)?$"
            );

    private static final Pattern TIME_SUFFIX_PATTERN =
            Pattern.compile(
                    "(?i)[T\\s]+\\d{1,2}:\\d{2}"
                            + "(?::\\d{2}(?:\\.\\d+)?)?"
                            + "(?:\\s*[AP]M)?$"
            );

    private static final Pattern ORDINAL_DAY_PATTERN =
            Pattern.compile(
                    "(?i)\\b(\\d{1,2})(ST|ND|RD|TH)\\b"
            );

    private static final List<DateTimeFormatter> TEXT_DATE_FORMATTERS =
            List.of(
                    strictFormatter("d MMM uuuu"),
                    strictFormatter("d MMMM uuuu"),
                    strictFormatter("MMM d uuuu"),
                    strictFormatter("MMMM d uuuu"),
                    strictFormatter("d-MMM-uuuu"),
                    strictFormatter("d-MMMM-uuuu"),
                    strictFormatter("MMM-d-uuuu"),
                    strictFormatter("MMMM-d-uuuu")
            );

    /**
     * Normalizes a normal text field.
     *
     * <p>Blank and ENTER VALID DATA are treated as unresolved values.</p>
     */
    public String nullableText(
            String value
    ) {
        String normalized =
                trimToNull(value);

        if (
                normalized == null
                        || isCorrectionMarker(normalized)
        ) {
            return null;
        }

        return normalized;
    }

    /**
     * Reads a required text field.
     */
    public String requiredText(
            String value,
            String fieldName
    ) {
        String normalized =
                nullableText(value);

        if (normalized == null) {
            throw new IllegalArgumentException(
                    fieldName + " is required"
            );
        }

        return normalized;
    }

    /**
     * Parses common Employee workbook date formats.
     *
     * <p>Supported examples:</p>
     *
     * <ul>
     *     <li>2024-06-20, 2024/06/20, 2024.06.20</li>
     *     <li>6/20/2024, 06/20/2024, 6-20-2024</li>
     *     <li>20/06/2024, 20-06-2024, 20.06.2024</li>
     *     <li>6/20/24 and 20/06/24</li>
     *     <li>20 Jun 2024 and June 20, 2024</li>
     *     <li>20240620</li>
     *     <li>Excel date serial values</li>
     *     <li>The same values with a trailing time component</li>
     * </ul>
     *
     * <p>For an ambiguous numeric date where both the first and second
     * numbers are at most 12, month/day/year is attempted first. This keeps
     * values produced by Excel formats such as {@code 6/8/26} consistent
     * with unambiguous values such as {@code 6/20/24}.</p>
     */
    public LocalDate nullableDate(
            String value,
            String fieldName
    ) {
        String normalized =
                nullableText(value);

        if (normalized == null) {
            return null;
        }

        String dateText =
                normalizeDateText(normalized);

        LocalDate parsed =
                parseExcelSerial(dateText);

        if (parsed == null) {
            parsed = parseCompactDate(dateText);
        }

        if (parsed == null) {
            parsed = parseNumericSeparatedDate(dateText);
        }

        if (parsed == null) {
            parsed = parseTextDate(dateText);
        }

        if (parsed != null) {
            return parsed;
        }

        throw new IllegalArgumentException(
                fieldName
                        + " contains an invalid date. Accepted examples: "
                        + "2024-06-20, 6/20/2024, 20/06/2024, "
                        + "6/20/24, 20-06-2024, 20 Jun 2024 "
                        + "or June 20, 2024."
        );
    }

    public LocalDate requiredDate(
            String value,
            String fieldName
    ) {
        LocalDate date =
                nullableDate(
                        value,
                        fieldName
                );

        if (date == null) {
            throw new IllegalArgumentException(
                    fieldName + " is required"
            );
        }

        return date;
    }

    /**
     * Parses the Joining Date used by Employee bulk import.
     *
     * <p>A complete date is stored exactly as supplied. A four-digit year
     * such as {@code 2025} is converted to {@code 2025-01-01}. This allows
     * Employee Number generation to use year {@code 25} without adding a
     * second database column.</p>
     *
     * <p>This method is intentionally separate from {@link #nullableDate}
     * so a year-only value is never accepted for Date of Birth.</p>
     */
    public LocalDate nullableJoiningDate(
            String value,
            String fieldName
    ) {
        String normalized =
                nullableText(value);

        if (normalized == null) {
            return null;
        }

        String dateText =
                normalizeDateText(normalized);

        if (YEAR_ONLY_PATTERN.matcher(dateText).matches()) {
            int year =
                    Integer.parseInt(dateText);

            if (year < 1900 || year > 2100) {
                throw new IllegalArgumentException(
                        fieldName
                                + " contains an invalid year. "
                                + "Enter a year between 1900 and 2100 "
                                + "or a complete date."
                );
            }

            return LocalDate.of(
                    year,
                    1,
                    1
            );
        }

        return nullableDate(
                normalized,
                fieldName
        );
    }

    /**
     * Required Joining Date parser for Employee bulk import only.
     */
    public LocalDate requiredJoiningDate(
            String value,
            String fieldName
    ) {
        LocalDate joiningDate =
                nullableJoiningDate(
                        value,
                        fieldName
                );

        if (joiningDate == null) {
            throw new IllegalArgumentException(
                    fieldName
                            + " is required. Enter a complete date "
                            + "or a four-digit year such as 2025."
            );
        }

        return joiningDate;
    }

    private LocalDate parseNumericSeparatedDate(
            String value
    ) {
        Matcher matcher =
                NUMERIC_DATE_PATTERN.matcher(value);

        if (!matcher.matches()) {
            return null;
        }

        String firstToken =
                matcher.group(1);

        int first =
                Integer.parseInt(firstToken);

        int second =
                Integer.parseInt(matcher.group(2));

        String thirdToken =
                matcher.group(3);

        int third =
                Integer.parseInt(thirdToken);

        if (firstToken.length() == 4) {
            return createDate(
                    first,
                    second,
                    third
            );
        }

        if (
                thirdToken.length() != 2
                        && thirdToken.length() != 4
        ) {
            return null;
        }

        int year =
                normalizeYear(
                        third,
                        thirdToken.length()
                );

        if (first > 12 && second <= 12) {
            return createDate(
                    year,
                    second,
                    first
            );
        }

        if (second > 12 && first <= 12) {
            return createDate(
                    year,
                    first,
                    second
            );
        }

        if (first <= 12 && second <= 12) {
            LocalDate monthFirst =
                    createDate(
                            year,
                            first,
                            second
                    );

            if (monthFirst != null) {
                return monthFirst;
            }

            return createDate(
                    year,
                    second,
                    first
            );
        }

        return null;
    }

    private LocalDate parseCompactDate(
            String value
    ) {
        Matcher matcher =
                COMPACT_DATE_PATTERN.matcher(value);

        if (!matcher.matches()) {
            return null;
        }

        return createDate(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3))
        );
    }

    private LocalDate parseTextDate(
            String value
    ) {
        for (
                DateTimeFormatter formatter
                : TEXT_DATE_FORMATTERS
        ) {
            try {
                return LocalDate.parse(
                        value,
                        formatter
                );
            } catch (DateTimeParseException ignored) {
                // Try the next supported text format.
            }
        }

        return null;
    }

    private LocalDate parseExcelSerial(
            String value
    ) {
        if (
                !EXCEL_SERIAL_PATTERN
                        .matcher(value)
                        .matches()
        ) {
            return null;
        }

        try {
            double serial =
                    Double.parseDouble(value);

            /*
             * Modern Employee DOB and joining dates represented as Excel
             * serials are normally above 20,000. This prevents a plain year
             * such as 2024 from being interpreted as an Excel serial date.
             */
            if (
                    serial < 20_000
                            || serial > 100_000
                            || !DateUtil.isValidExcelDate(serial)
            ) {
                return null;
            }

            return DateUtil
                    .getLocalDateTime(serial)
                    .toLocalDate();
        } catch (
                NumberFormatException
                        | DateTimeException exception
        ) {
            return null;
        }
    }

    private LocalDate createDate(
            int year,
            int month,
            int day
    ) {
        try {
            return LocalDate.of(
                    year,
                    month,
                    day
            );
        } catch (DateTimeException exception) {
            return null;
        }
    }

    private int normalizeYear(
            int year,
            int tokenLength
    ) {
        if (tokenLength == 4) {
            return year;
        }

        return year <= 49
                ? 2000 + year
                : 1900 + year;
    }

    private String normalizeDateText(
            String value
    ) {
        String normalized =
                value.trim()
                        .replace(',', ' ')
                        .replaceAll("\\s+", " ");

        Matcher ordinalMatcher =
                ORDINAL_DAY_PATTERN.matcher(normalized);

        normalized =
                ordinalMatcher.replaceAll("$1");

        return TIME_SUFFIX_PATTERN
                .matcher(normalized)
                .replaceFirst("")
                .trim();
    }

    private static DateTimeFormatter strictFormatter(
            String pattern
    ) {
        return new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern(pattern)
                .toFormatter(Locale.ENGLISH)
                .withResolverStyle(
                        ResolverStyle.STRICT
                );
    }

    public Gender nullableGender(
            String value
    ) {
        String normalized =
                normalizeEnumValue(value);

        if (normalized == null) {
            return null;
        }

        try {
            return Gender.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Gender must be MALE, FEMALE or OTHER"
            );
        }
    }

    public EmployeeCategory nullableEmployeeCategory(
            String value
    ) {
        String normalized =
                normalizeEnumValue(value);

        if (normalized == null) {
            return null;
        }

        try {
            return EmployeeCategory.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Employee Category must be one of: "
                            + "TEACHING, NON_TEACHING, "
                            + "MANAGEMENT_TEACHING, "
                            + "MANAGEMENT_NON_TEACHING or SUPPORT_STAFF"
            );
        }
    }

    public EmployeeType requiredEmployeeType(
            String value
    ) {
        String normalized =
                normalizeEnumValue(value);

        if (normalized == null) {
            throw new IllegalArgumentException(
                    "Employee Type is required"
            );
        }

        try {
            return EmployeeType.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Employee Type must be one of: "
                            + "REGULARISED, CONTRACT, TEMPORARY, PART_TIME, "
                            + "INTERN, VOLUNTEER or HONORY"
            );
        }
    }

    public EmploymentMode requiredEmploymentMode(
            String value
    ) {
        String normalized =
                normalizeEnumValue(value);

        if (normalized == null) {
            throw new IllegalArgumentException(
                    "Employment Mode is required"
            );
        }

        try {
            return EmploymentMode.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Employment Mode must be one of: "
                            + "FULL_TIME, PART_TIME, REMOTE or ON_CALL"
            );
        }
    }

    /**
     * Accepts user-friendly Excel values:
     * YES, NO, TRUE, FALSE, 1 and 0.
     */
    public Boolean nullableYesNo(
            String value,
            String fieldName
    ) {
        String normalized =
                nullableText(value);

        if (normalized == null) {
            return null;
        }

        return switch (normalized.toUpperCase(Locale.ROOT)) {
            case "YES", "TRUE", "1" -> Boolean.TRUE;
            case "NO", "FALSE", "0" -> Boolean.FALSE;
            default -> throw new IllegalArgumentException(
                    fieldName + " must be YES or NO"
            );
        };
    }

    public boolean requiredYesNo(
            String value,
            String fieldName
    ) {
        Boolean parsed =
                nullableYesNo(
                        value,
                        fieldName
                );

        if (parsed == null) {
            throw new IllegalArgumentException(
                    fieldName + " is required"
            );
        }

        return parsed;
    }

    /**
     * Converts values such as:
     *
     * <pre>
     * Full Time              -> FULL_TIME
     * Non Teaching           -> NON_TEACHING
     * Management Teaching    -> MANAGEMENT_TEACHING
     * </pre>
     */
    public String normalizeEnumValue(
            String value
    ) {
        String normalized =
                nullableText(value);

        if (normalized == null) {
            return null;
        }

        return normalized
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replaceAll("\\s+", "_");
    }

    public String normalizeLookupKey(
            String value
    ) {
        String normalized =
                nullableText(value);

        if (normalized == null) {
            return null;
        }

        return normalized
                .trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[\\s_-]+", "");
    }

    public boolean isCorrectionMarker(
            String value
    ) {
        return value != null
                && EmployeeExcelHeaders.ENTER_VALID_DATA
                .equalsIgnoreCase(value.trim());
    }

    private String trimToNull(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String trimmed =
                value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }
}

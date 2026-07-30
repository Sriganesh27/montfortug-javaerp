package com.erp.montfortuganda.student.bulkimport.reference;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Normalizes Student Education Level and Class values supplied through Excel.
 *
 * <p>Canonical structure:</p>
 *
 * <ul>
 *     <li>NURSERY: N1, N2, N3</li>
 *     <li>PRIMARY: P1 to P7</li>
 *     <li>SECONDARY: S1 to S4</li>
 *     <li>SENIOR SECONDARY: S5 to S6</li>
 * </ul>
 *
 * <p>This class never returns database IDs. The canonical level and class
 * values must still be resolved against active branch reference data.</p>
 */
public final class StudentEducationClassNormalizer {

    public static final String LEVEL_NURSERY =
            "NURSERY";

    public static final String LEVEL_PRIMARY =
            "PRIMARY";

    public static final String LEVEL_SECONDARY =
            "SECONDARY";

    public static final String LEVEL_SENIOR_SECONDARY =
            "SENIOR SECONDARY";

    private static final Pattern PRIMARY_CLASS_PATTERN =
            Pattern.compile(
                    "^(?:P|PRIMARY|PRIMARYCLASS)([1-7])$"
            );

    private static final Pattern SECONDARY_CLASS_PATTERN =
            Pattern.compile(
                    "^(?:S|SECONDARY|SECONDARYCLASS)([1-6])$"
            );

    private static final Pattern NURSERY_CLASS_PATTERN =
            Pattern.compile(
                    "^(?:N|KG|NURSERY|NURSERYCLASS)([1-3])$"
            );

    private static final Pattern GENERIC_CLASS_PATTERN =
            Pattern.compile(
                    "^(?:CLASS)?([1-7])$"
            );

    private static final Set<String> NURSERY_LEVEL_ALIASES =
            Set.of(
                    "NURSERY",
                    "PREPRIMARY",
                    "PREPRIMARYEDUCATION",
                    "PREPRIMARYLEVEL",
                    "EARLYCHILDHOOD",
                    "EARLYCHILDHOODEDUCATION",
                    "ECD"
            );

    private static final Set<String> PRIMARY_LEVEL_ALIASES =
            Set.of(
                    "PRIMARY",
                    "PRIMARYEDUCATION",
                    "PRIMARYLEVEL",
                    "PRI"
            );

    private static final Set<String> SECONDARY_LEVEL_ALIASES =
            Set.of(
                    "SECONDARY",
                    "SECONDARYEDUCATION",
                    "SECONDARYLEVEL",
                    "LOWERSECONDARY",
                    "ORDINARYLEVEL",
                    "OLEVEL"
            );

    private static final Set<String> SENIOR_SECONDARY_LEVEL_ALIASES =
            Set.of(
                    "SENIORSECONDARY",
                    "SENIORSECONDARYEDUCATION",
                    "SENIORSECONDARYLEVEL",
                    "UPPERSECONDARY",
                    "ADVANCEDLEVEL",
                    "ALEVEL",
                    "SENIOR"
            );

    private static final Map<String, String> NUMBER_WORDS =
            Map.ofEntries(
                    Map.entry("ONE", "1"),
                    Map.entry("TWO", "2"),
                    Map.entry("THREE", "3"),
                    Map.entry("FOUR", "4"),
                    Map.entry("FIVE", "5"),
                    Map.entry("SIX", "6"),
                    Map.entry("SEVEN", "7")
            );

    private StudentEducationClassNormalizer() {
        throw new IllegalStateException(
                "StudentEducationClassNormalizer is a utility class."
        );
    }

    /**
     * Resolves supplied Education Level and Class values into canonical
     * values and detects conflicts between them.
     *
     * @param rawLevel Excel Education Level value
     * @param rawClass Excel Class value
     * @return canonical level and class
     */
    public static EducationClassResolution resolve(
            String rawLevel,
            String rawClass
    ) {
        String canonicalLevel =
                normalizeLevel(rawLevel);

        String canonicalClass =
                normalizeClass(
                        rawClass,
                        canonicalLevel
                );

        String inferredLevel =
                inferLevelFromClass(
                        canonicalClass
                );

        if (
                canonicalLevel != null
                        && inferredLevel != null
                        && !canonicalLevel.equals(inferredLevel)
        ) {
            throw new IllegalArgumentException(
                    "Class "
                            + canonicalClass
                            + " belongs to "
                            + inferredLevel
                            + ", but the supplied Education Level is "
                            + canonicalLevel
                            + "."
            );
        }

        if (canonicalLevel == null) {
            canonicalLevel = inferredLevel;
        }

        return new EducationClassResolution(
                canonicalLevel,
                canonicalClass
        );
    }

    /**
     * Converts an Education Level label into the canonical ERP level.
     */
    public static String normalizeLevel(
            String rawLevel
    ) {
        String token =
                compactToken(rawLevel);

        if (token == null) {
            return null;
        }

        if (NURSERY_LEVEL_ALIASES.contains(token)) {
            return LEVEL_NURSERY;
        }

        if (PRIMARY_LEVEL_ALIASES.contains(token)) {
            return LEVEL_PRIMARY;
        }

        if (SECONDARY_LEVEL_ALIASES.contains(token)) {
            return LEVEL_SECONDARY;
        }

        if (
                SENIOR_SECONDARY_LEVEL_ALIASES.contains(
                        token
                )
        ) {
            return LEVEL_SENIOR_SECONDARY;
        }

        throw new IllegalArgumentException(
                "Unsupported Education Level '"
                        + rawLevel
                        + "'. Expected Nursery, Primary, Secondary, "
                        + "or Senior Secondary."
        );
    }

    /**
     * Converts a Class label into N1-N3, P1-P7 or S1-S6.
     *
     * <p>For generic labels such as "Class 1", the supplied canonical level
     * is used to determine the class prefix.</p>
     */
    public static String normalizeClass(
            String rawClass,
            String canonicalLevel
    ) {
        String token =
                compactToken(rawClass);

        if (token == null) {
            return null;
        }

        token = replaceNumberWord(token);

        String nurseryAlias =
                normalizeNurseryAlias(token);

        if (nurseryAlias != null) {
            return nurseryAlias;
        }

        Matcher nurseryMatcher =
                NURSERY_CLASS_PATTERN.matcher(token);

        if (nurseryMatcher.matches()) {
            return "N"
                    + nurseryMatcher.group(1);
        }

        Matcher primaryMatcher =
                PRIMARY_CLASS_PATTERN.matcher(token);

        if (primaryMatcher.matches()) {
            return "P"
                    + primaryMatcher.group(1);
        }

        Matcher secondaryMatcher =
                SECONDARY_CLASS_PATTERN.matcher(token);

        if (secondaryMatcher.matches()) {
            return "S"
                    + secondaryMatcher.group(1);
        }

        Matcher genericMatcher =
                GENERIC_CLASS_PATTERN.matcher(token);

        if (
                genericMatcher.matches()
                        && canonicalLevel != null
        ) {
            int classNumber =
                    Integer.parseInt(
                            genericMatcher.group(1)
                    );

            return classForLevel(
                    canonicalLevel,
                    classNumber
            );
        }

        throw new IllegalArgumentException(
                "Unsupported Class '"
                        + rawClass
                        + "'. Expected Nursery N1-N3, Primary P1-P7, "
                        + "Secondary S1-S4, or Senior Secondary S5-S6."
        );
    }

    /**
     * Determines the canonical Education Level from a canonical class code.
     */
    public static String inferLevelFromClass(
            String canonicalClass
    ) {
        if (
                canonicalClass == null
                        || canonicalClass.isBlank()
        ) {
            return null;
        }

        String normalized =
                canonicalClass.trim()
                        .toUpperCase(Locale.ROOT);

        if (normalized.matches("N[1-3]")) {
            return LEVEL_NURSERY;
        }

        if (normalized.matches("P[1-7]")) {
            return LEVEL_PRIMARY;
        }

        if (normalized.matches("S[1-4]")) {
            return LEVEL_SECONDARY;
        }

        if (normalized.matches("S[5-6]")) {
            return LEVEL_SENIOR_SECONDARY;
        }

        throw new IllegalArgumentException(
                "Unsupported canonical Student class: "
                        + canonicalClass
        );
    }

    /**
     * Returns true only when the class belongs to the supplied level.
     */
    public static boolean classBelongsToLevel(
            String canonicalLevel,
            String canonicalClass
    ) {
        if (
                canonicalLevel == null
                        || canonicalClass == null
        ) {
            return false;
        }

        return canonicalLevel.equals(
                inferLevelFromClass(
                        canonicalClass
                )
        );
    }

    private static String normalizeNurseryAlias(
            String token
    ) {
        return switch (token) {
            case "BABY",
                 "BABYCLASS",
                 "LOWER",
                 "LOWERCLASS" -> "N1";

            case "MIDDLE",
                 "MC",
                 "MIDDLECLASS" -> "N2";

            case "TOP",
                 "TOPCLASS",
                 "UPPER",
                 "UPPERCLASS" -> "N3";

            default -> null;
        };
    }

    private static String classForLevel(
            String canonicalLevel,
            int classNumber
    ) {
        return switch (canonicalLevel) {
            case LEVEL_NURSERY -> {
                if (
                        classNumber < 1
                                || classNumber > 3
                ) {
                    throw new IllegalArgumentException(
                            "Nursery supports only N1, N2 and N3."
                    );
                }

                yield "N" + classNumber;
            }

            case LEVEL_PRIMARY -> {
                if (
                        classNumber < 1
                                || classNumber > 7
                ) {
                    throw new IllegalArgumentException(
                            "Primary supports only P1 to P7."
                    );
                }

                yield "P" + classNumber;
            }

            case LEVEL_SECONDARY -> {
                if (
                        classNumber < 1
                                || classNumber > 4
                ) {
                    throw new IllegalArgumentException(
                            "Secondary supports only S1 to S4."
                    );
                }

                yield "S" + classNumber;
            }

            case LEVEL_SENIOR_SECONDARY -> {
                if (
                        classNumber < 5
                                || classNumber > 6
                ) {
                    throw new IllegalArgumentException(
                            "Senior Secondary supports only S5 and S6."
                    );
                }

                yield "S" + classNumber;
            }

            default -> throw new IllegalArgumentException(
                    "Unsupported canonical Education Level: "
                            + canonicalLevel
            );
        };
    }

    private static String replaceNumberWord(
            String token
    ) {
        for (
                Map.Entry<String, String> entry
                : NUMBER_WORDS.entrySet()
        ) {
            if (token.endsWith(entry.getKey())) {
                return token.substring(
                        0,
                        token.length()
                                - entry.getKey().length()
                ) + entry.getValue();
            }
        }

        return token;
    }

    /**
     * Removes spaces and punctuation and applies case-insensitive matching.
     *
     * <p>Examples:</p>
     *
     * <ul>
     *     <li>P.1, P-1 and p 1 become P1</li>
     *     <li>S.5, S-5 and s 5 become S5</li>
     *     <li>M/C and m / c become MC</li>
     *     <li>KG.2 and KG-2 become KG2</li>
     * </ul>
     */
    private static String compactToken(
            String value
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            return null;
        }

        String normalized =
                value.trim()
                        .toUpperCase(Locale.ROOT)
                        .replaceAll(
                                "[^A-Z0-9]",
                                ""
                        );

        return normalized.isEmpty()
                ? null
                : normalized;
    }

    public record EducationClassResolution(
            String educationLevel,
            String classCode
    ) {
    }
}
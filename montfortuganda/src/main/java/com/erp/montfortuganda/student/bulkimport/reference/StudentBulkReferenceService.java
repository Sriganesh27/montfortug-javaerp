package com.erp.montfortuganda.student.bulkimport.reference;

import com.erp.montfortuganda.student.bulkimport.excel.StudentExcelValueParser;
import jakarta.persistence.EntityManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Loads and caches all Student import reference data for one branch.
 *
 * Reference data is loaded once for an import job and reused while validating
 * every Student row. This prevents repeated database queries for Academic
 * Year, Education Level, Class and Section.
 *
 * Branch ownership is taken only from the authenticated import context.
 * Excel cannot choose or change the branch.
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("SpellCheckingInspection")
public class StudentBulkReferenceService {

    private final EntityManager entityManager;
    private final StudentExcelValueParser valueParser;

    /**
     * Loads active reference data for the authenticated branch.
     *
     * Expected complexity:
     *
     * Time: O(y + l + c + s)
     * Space: O(y + l + c + s)
     *
     * y = Academic Years
     * l = Branch Education Levels
     * c = Branch Classes
     * s = Branch Sections
     */
    @Transactional(readOnly = true)
    public StudentBulkReferenceData loadReferences(
            Integer branchId
    ) {
        validateBranchId(branchId);

        BranchReference branch =
                loadBranch(branchId);

        Map<String, AcademicYearReference> academicYears =
                loadAcademicYears();

        Map<String, LevelReference> levels =
                loadBranchLevels(branchId);

        Map<String, ClassReference> classes =
                loadBranchClasses(branchId);

        Map<String, SectionReference> sections =
                loadBranchSections(branchId);

        Set<String> branchKeys =
                createBranchKeys(branch);

        return new StudentBulkReferenceData(
                branch,
                branchKeys,
                academicYears,
                levels,
                classes,
                sections
        );
    }

    // =====================================================================
    // BRANCH
    // =====================================================================

    private BranchReference loadBranch(
            Integer branchId
    ) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows =
                entityManager
                        .createNativeQuery(
                                """
                                select branch.branch_id,
                                       branch.branch_name,
                                       branch.school_code
                                from erp_branches branch
                                where branch.branch_id = :branchId
                                  and branch.is_active = 1
                                """
                        )
                        .setParameter(
                                "branchId",
                                branchId
                        )
                        .setMaxResults(1)
                        .getResultList();

        if (rows.isEmpty()) {
            throw new IllegalArgumentException(
                    "Authenticated branch was not found or is inactive."
            );
        }

        Object[] row =
                rows.getFirst();

        return new BranchReference(
                toInteger(row[0]),
                toText(row[1]),
                toText(row[2])
        );
    }

    private Set<String> createBranchKeys(
            BranchReference branch
    ) {
        Set<String> keys =
                new HashSet<>();

        putKey(
                keys,
                String.valueOf(
                        branch.getBranchId()
                )
        );

        putKey(
                keys,
                branch.getBranchName()
        );

        putKey(
                keys,
                branch.getSchoolCode()
        );

        return Set.copyOf(keys);
    }

    // =====================================================================
    // ACADEMIC YEARS
    // =====================================================================

    private Map<String, AcademicYearReference> loadAcademicYears() {
        @SuppressWarnings("unchecked")
        List<Object[]> rows =
                entityManager
                        .createNativeQuery(
                                """
                                select academic_year.academic_year_id,
                                       academic_year.academic_year_code,
                                       academic_year.academic_year_name,
                                       academic_year.start_date,
                                       academic_year.end_date,
                                       academic_year.status,
                                       academic_year.current_year
                                from erp_academic_years academic_year
                                where academic_year.active = 1
                                  and upper(academic_year.status)
                                      in ('PLANNED', 'ACTIVE')
                                order by academic_year.current_year desc,
                                         academic_year.start_date desc,
                                         academic_year.academic_year_id desc
                                """
                        )
                        .getResultList();

        Map<String, AcademicYearReference> references =
                new HashMap<>();

        for (Object[] row : rows) {
            AcademicYearReference academicYear =
                    new AcademicYearReference(
                            toLong(row[0]),
                            toText(row[1]),
                            toText(row[2]),
                            toLocalDate(row[3]),
                            toLocalDate(row[4]),
                            toText(row[5]),
                            toBoolean(row[6])
                    );

            putReference(
                    references,
                    academicYear.getAcademicYearCode(),
                    academicYear
            );

            putReference(
                    references,
                    academicYear.getAcademicYearName(),
                    academicYear
            );

            if (academicYear.getAcademicYearId() != null) {
                putReference(
                        references,
                        String.valueOf(
                                academicYear.getAcademicYearId()
                        ),
                        academicYear
                );
            }
        }

        return Map.copyOf(references);
    }

    // =====================================================================
    // BRANCH LEVELS
    // =====================================================================

    private Map<String, LevelReference> loadBranchLevels(
            Integer branchId
    ) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows =
                entityManager
                        .createNativeQuery(
                                """
                                select distinct level.level_id,
                                                level.level_name,
                                                level.display_order
                                from erp_levels level
                                join erp_branch_levels branch_level
                                  on branch_level.level_id = level.level_id
                                where branch_level.branch_id = :branchId
                                  and level.status = 1
                                order by level.display_order asc,
                                         level.level_name asc
                                """
                        )
                        .setParameter(
                                "branchId",
                                branchId
                        )
                        .getResultList();

        Map<String, LevelReference> references =
                new HashMap<>();

        for (Object[] row : rows) {
            LevelReference level =
                    new LevelReference(
                            toInteger(row[0]),
                            toText(row[1]),
                            toInteger(row[2])
                    );

            putReference(
                    references,
                    level.getLevelName(),
                    level
            );

            if (level.getLevelId() != null) {
                putReference(
                        references,
                        String.valueOf(
                                level.getLevelId()
                        ),
                        level
                );
            }
        }

        return Map.copyOf(references);
    }

    // =====================================================================
    // BRANCH CLASSES
    // =====================================================================

    private Map<String, ClassReference> loadBranchClasses(
            Integer branchId
    ) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows =
                entityManager
                        .createNativeQuery(
                                """
                                select distinct school_class.class_id,
                                                school_class.level_id,
                                                school_class.class_code,
                                                school_class.class_name,
                                                school_class.display_order
                                from erp_classes school_class
                                join erp_branch_levels branch_level
                                  on branch_level.level_id =
                                     school_class.level_id
                                join erp_levels level
                                  on level.level_id =
                                     school_class.level_id
                                where branch_level.branch_id = :branchId
                                  and school_class.status = 1
                                  and level.status = 1
                                order by school_class.display_order asc,
                                         school_class.class_name asc
                                """
                        )
                        .setParameter(
                                "branchId",
                                branchId
                        )
                        .getResultList();

        Map<String, ClassReference> references =
                new HashMap<>();

        for (Object[] row : rows) {
            ClassReference schoolClass =
                    new ClassReference(
                            toInteger(row[0]),
                            toInteger(row[1]),
                            toText(row[2]),
                            toText(row[3]),
                            toInteger(row[4])
                    );

            putReference(
                    references,
                    schoolClass.getClassCode(),
                    schoolClass
            );

            putReference(
                    references,
                    schoolClass.getClassName(),
                    schoolClass
            );

            if (schoolClass.getClassId() != null) {
                putReference(
                        references,
                        String.valueOf(
                                schoolClass.getClassId()
                        ),
                        schoolClass
                );
            }
        }

        return Map.copyOf(references);
    }

    // =====================================================================
    // BRANCH SECTIONS
    // =====================================================================

    private Map<String, SectionReference> loadBranchSections(
            Integer branchId
    ) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows =
                entityManager
                        .createNativeQuery(
                                """
                                select section.section_id,
                                       section.academic_year_id,
                                       section.class_id,
                                       section.section_code,
                                       section.section_name
                                from erp_sections section
                                join erp_academic_years academic_year
                                  on academic_year.academic_year_id =
                                     section.academic_year_id
                                where section.branch_id = :branchId
                                  and section.active = 1
                                  and upper(section.status) = 'ACTIVE'
                                  and academic_year.active = 1
                                  and upper(academic_year.status)
                                      in ('PLANNED', 'ACTIVE')
                                order by section.academic_year_id asc,
                                         section.class_id asc,
                                         section.section_code asc,
                                         section.section_id asc
                                """
                        )
                        .setParameter(
                                "branchId",
                                branchId
                        )
                        .getResultList();

        Map<String, SectionReference> references =
                new HashMap<>();

        for (Object[] row : rows) {
            SectionReference section =
                    new SectionReference(
                            toLong(row[0]),
                            toLong(row[1]),
                            toInteger(row[2]),
                            toText(row[3]),
                            toText(row[4])
                    );

            putSectionReference(
                    references,
                    section,
                    section.getSectionCode()
            );

            putSectionReference(
                    references,
                    section,
                    section.getSectionName()
            );

            if (section.getSectionId() != null) {
                putSectionReference(
                        references,
                        section,
                        String.valueOf(
                                section.getSectionId()
                        )
                );
            }
        }

        return Map.copyOf(references);
    }

    // =====================================================================
    // REFERENCE INDEXING
    // =====================================================================

    private <T> void putReference(
            Map<String, T> target,
            String rawKey,
            T value
    ) {
        String normalizedKey =
                valueParser.normalizeLookupKey(
                        rawKey
                );

        if (normalizedKey != null) {
            target.putIfAbsent(
                    normalizedKey,
                    value
            );
        }
    }

    private void putSectionReference(
            Map<String, SectionReference> target,
            SectionReference section,
            String rawKey
    ) {
        String normalizedKey =
                valueParser.normalizeLookupKey(
                        rawKey
                );

        String compositeKey =
                sectionKey(
                        section.getAcademicYearId(),
                        section.getClassId(),
                        normalizedKey
                );

        if (compositeKey != null) {
            target.putIfAbsent(
                    compositeKey,
                    section
            );
        }
    }

    private void putKey(
            Set<String> target,
            String rawKey
    ) {
        String normalizedKey =
                valueParser.normalizeLookupKey(
                        rawKey
                );

        if (normalizedKey != null) {
            target.add(normalizedKey);
        }
    }

    private String sectionKey(
            Long academicYearId,
            Integer classId,
            String normalizedSectionKey
    ) {
        if (
                academicYearId == null
                        || classId == null
                        || normalizedSectionKey == null
        ) {
            return null;
        }

        return academicYearId
                + ":"
                + classId
                + ":"
                + normalizedSectionKey;
    }

    // =====================================================================
    // VALUE CONVERSION
    // =====================================================================

    private void validateBranchId(
            Integer branchId
    ) {
        if (branchId == null || branchId <= 0) {
            throw new IllegalArgumentException(
                    "A valid branch is required for Student import."
            );
        }
    }

    private Integer toInteger(
            Object value
    ) {
        if (value instanceof Number number) {
            return number.intValue();
        }

        if (value == null) {
            return null;
        }

        try {
            return Integer.valueOf(
                    value.toString()
                            .trim()
            );
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Long toLong(
            Object value
    ) {
        if (value instanceof Number number) {
            return number.longValue();
        }

        if (value == null) {
            return null;
        }

        try {
            return Long.valueOf(
                    value.toString()
                            .trim()
            );
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String toText(
            Object value
    ) {
        if (value == null) {
            return null;
        }

        String text =
                Objects.toString(
                                value,
                                ""
                        )
                        .trim();

        return text.isEmpty()
                ? null
                : text;
    }

    private LocalDate toLocalDate(
            Object value
    ) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }

        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }

        if (value instanceof Timestamp timestamp) {
            return timestamp
                    .toLocalDateTime()
                    .toLocalDate();
        }

        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.toLocalDate();
        }

        if (value instanceof String text) {
            try {
                return LocalDate.parse(
                        text.trim()
                );
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }

        return null;
    }

    private Boolean toBoolean(
            Object value
    ) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }

        if (value instanceof Number number) {
            return number.intValue() != 0;
        }

        if (value instanceof byte[] bytes) {
            return bytes.length > 0
                    && bytes[0] != 0;
        }

        if (value instanceof String text) {
            String normalized =
                    text.trim();

            return "1".equals(normalized)
                    || "true".equalsIgnoreCase(normalized)
                    || "yes".equalsIgnoreCase(normalized);
        }

        return false;
    }

    // =====================================================================
    // CACHED REFERENCE DATA
    // =====================================================================

    @Getter
    public static final class StudentBulkReferenceData {

        private final BranchReference branch;
        private final Set<String> branchKeys;

        private final Map<String, AcademicYearReference>
                academicYearsByKey;

        private final Map<String, LevelReference>
                levelsByKey;

        private final Map<String, ClassReference>
                classesByKey;

        private final Map<String, SectionReference>
                sectionsByCompositeKey;

        private final List<AcademicYearReference>
                academicYearsOrdered;

        private final List<LevelReference>
                levelsOrdered;

        private final Map<Integer, LevelReference>
                levelsById;

        private final Map<Integer, List<ClassReference>>
                classesByLevelId;

        private final Map<String, ClassReference>
                classesByLevelAndKey;

        /**
         * Canonical branch Education Levels keyed by:
         * NURSERY, PRIMARY, SECONDARY and SENIOR SECONDARY.
         */
        private final Map<String, LevelReference>
                levelsByCanonicalKey;

        /**
         * Canonical branch Classes keyed by:
         * canonical-level + ':' + canonical-class.
         *
         * Examples:
         * NURSERY:N1, PRIMARY:P4, SECONDARY:S3,
         * SENIOR SECONDARY:S6.
         */
        private final Map<String, ClassReference>
                classesByCanonicalKey;

        private StudentBulkReferenceData(
                BranchReference branch,
                Set<String> branchKeys,
                Map<String, AcademicYearReference> academicYearsByKey,
                Map<String, LevelReference> levelsByKey,
                Map<String, ClassReference> classesByKey,
                Map<String, SectionReference> sectionsByCompositeKey
        ) {
            this.branch = branch;
            this.branchKeys = Set.copyOf(branchKeys);
            this.academicYearsByKey =
                    Map.copyOf(academicYearsByKey);
            this.levelsByKey =
                    Map.copyOf(levelsByKey);
            this.classesByKey =
                    Map.copyOf(classesByKey);
            this.sectionsByCompositeKey =
                    Map.copyOf(sectionsByCompositeKey);

            this.academicYearsOrdered =
                    buildAcademicYears(
                            academicYearsByKey
                    );

            this.levelsOrdered =
                    buildLevels(
                            levelsByKey
                    );

            this.levelsById =
                    buildLevelsById(
                            this.levelsOrdered
                    );

            this.classesByLevelId =
                    buildClassesByLevel(
                            classesByKey
                    );

            this.classesByLevelAndKey =
                    buildClassesByLevelAndKey(
                            classesByKey
                    );

            this.levelsByCanonicalKey =
                    buildCanonicalLevels(
                            this.levelsOrdered
                    );

            this.classesByCanonicalKey =
                    buildCanonicalClasses(
                            classesByKey,
                            this.levelsById
                    );
        }

        public Integer getBranchId() {
            return branch != null
                    ? branch.getBranchId()
                    : null;
        }

        public AcademicYearReference findAcademicYear(
                String normalizedKey
        ) {
            if (normalizedKey == null) {
                return null;
            }

            return academicYearsByKey.get(
                    normalizedKey
            );
        }

        /**
         * Returns the current Academic Year when configured, otherwise the
         * latest active/planned Academic Year loaded by the reference query.
         */
        public AcademicYearReference findDefaultAcademicYear() {
            return academicYearsOrdered.isEmpty()
                    ? null
                    : academicYearsOrdered.getFirst();
        }

        public LevelReference findLevel(
                String normalizedKey
        ) {
            if (normalizedKey == null) {
                return null;
            }

            LevelReference exact =
                    levelsByKey.get(
                            normalizedKey
                    );

            if (exact != null) {
                return exact;
            }

            String canonicalLevel =
                    safelyNormalizeLevel(
                            normalizedKey
                    );

            return canonicalLevel == null
                    ? null
                    : levelsByCanonicalKey.get(
                    canonicalLevel
            );
        }

        public LevelReference findLevelById(
                Integer levelId
        ) {
            if (levelId == null) {
                return null;
            }

            return levelsById.get(levelId);
        }

        /**
         * Infers a level from a supplied class. Nursery aliases such as
         * Baby, KG1, KG2, KG3, Middle and Top resolve to the Nursery level.
         */
        public LevelReference findLevelForClass(
                String normalizedClassKey
        ) {
            if (normalizedClassKey == null) {
                return null;
            }

            ClassReference exactClass =
                    classesByKey.get(
                            normalizedClassKey
                    );

            if (
                    exactClass != null
                            && exactClass.getLevelId() != null
            ) {
                return findLevelById(
                        exactClass.getLevelId()
                );
            }

            String canonicalClass =
                    safelyNormalizeClass(
                            normalizedClassKey,
                            null
                    );

            if (canonicalClass == null) {
                return null;
            }

            String canonicalLevel =
                    StudentEducationClassNormalizer
                            .inferLevelFromClass(
                                    canonicalClass
                            );

            return levelsByCanonicalKey.get(
                    canonicalLevel
            );
        }

        /**
         * Blank Education Level defaults to Nursery when the branch offers
         * Nursery. Otherwise, the first active branch level is used.
         */
        public LevelReference findDefaultLevel() {
            LevelReference nursery =
                    findNurseryLevel();

            if (nursery != null) {
                return nursery;
            }

            return levelsOrdered.isEmpty()
                    ? null
                    : levelsOrdered.getFirst();
        }

        public ClassReference findClass(
                String normalizedKey
        ) {
            if (normalizedKey == null) {
                return null;
            }

            return classesByKey.get(
                    normalizedKey
            );
        }

        /**
         * Resolves a class inside the selected Education Level.
         *
         * <p>For Nursery, common workbook labels are accepted even when the
         * database uses different names:</p>
         *
         * <ul>
         *     <li>Baby / Baby Class / KG1 / Nursery 1</li>
         *     <li>KG2 / Middle / Middle Class / Nursery 2</li>
         *     <li>KG3 / Top / Top Class / Nursery 3</li>
         * </ul>
         *
         * <p>When Class is blank, the first active class in the selected
         * level is returned.</p>
         */
        public ClassReference findClass(
                LevelReference level,
                String normalizedClassKey
        ) {
            if (level == null || level.getLevelId() == null) {
                return findClass(normalizedClassKey);
            }

            if (normalizedClassKey == null) {
                return findDefaultClass(level);
            }

            String compositeKey =
                    level.getLevelId()
                            + ":"
                            + normalizedClassKey;

            ClassReference exact =
                    classesByLevelAndKey.get(
                            compositeKey
                    );

            if (exact != null) {
                return exact;
            }

            ClassReference global =
                    classesByKey.get(
                            normalizedClassKey
                    );

            if (
                    global != null
                            && level.getLevelId()
                            .equals(
                                    global.getLevelId()
                            )
            ) {
                return global;
            }

            String canonicalLevel =
                    safelyNormalizeLevel(
                            level.getLevelName()
                    );

            String canonicalClass =
                    safelyNormalizeClass(
                            normalizedClassKey,
                            canonicalLevel
                    );

            if (
                    canonicalLevel != null
                            && canonicalClass != null
            ) {
                ClassReference canonicalMatch =
                        classesByCanonicalKey.get(
                                canonicalLevel
                                        + ":"
                                        + canonicalClass
                        );

                if (canonicalMatch != null) {
                    return canonicalMatch;
                }
            }

            /*
             * Final Nursery fallback for branch databases whose class names
             * are non-standard but whose display order is correct.
             */
            if (isNurseryLevel(level)) {
                int nurseryIndex =
                        nurseryClassIndex(
                                normalizedClassKey
                        );

                if (nurseryIndex >= 0) {
                    List<ClassReference> nurseryClasses =
                            classesForLevel(level);

                    if (nurseryIndex < nurseryClasses.size()) {
                        return nurseryClasses.get(
                                nurseryIndex
                        );
                    }
                }
            }

            return null;
        }

        public ClassReference findDefaultClass(
                LevelReference level
        ) {
            List<ClassReference> classes =
                    classesForLevel(level);

            return classes.isEmpty()
                    ? null
                    : classes.getFirst();
        }

        public SectionReference findSection(
                Long academicYearId,
                Integer classId,
                String normalizedSectionKey
        ) {
            if (
                    academicYearId == null
                            || classId == null
                            || normalizedSectionKey == null
            ) {
                return null;
            }

            String key =
                    academicYearId
                            + ":"
                            + classId
                            + ":"
                            + normalizedSectionKey;

            return sectionsByCompositeKey.get(
                    key
            );
        }

        private LevelReference findNurseryLevel() {
            for (LevelReference level : levelsOrdered) {
                if (isNurseryLevel(level)) {
                    return level;
                }
            }

            return null;
        }

        private boolean isNurseryLevel(
                LevelReference level
        ) {
            if (level == null) {
                return false;
            }

            return StudentEducationClassNormalizer
                    .LEVEL_NURSERY
                    .equals(
                            safelyNormalizeLevel(
                                    level.getLevelName()
                            )
                    );
        }

        private List<ClassReference> classesForLevel(
                LevelReference level
        ) {
            if (level == null || level.getLevelId() == null) {
                return List.of();
            }

            return classesByLevelId.getOrDefault(
                    level.getLevelId(),
                    List.of()
            );
        }

        private static int nurseryClassIndex(
                String normalizedClassKey
        ) {
            if (normalizedClassKey == null) {
                return -1;
            }

            return switch (normalizedClassKey) {
                case "BABY",
                     "BABYCLASS",
                     "KG1",
                     "KGI",
                     "NURSERY1",
                     "N1" -> 0;

                case "KG2",
                     "KGII",
                     "MIDDLE",
                     "MIDDLECLASS",
                     "NURSERY2",
                     "N2" -> 1;

                case "KG3",
                     "KGIII",
                     "TOP",
                     "TOPCLASS",
                     "NURSERY3",
                     "N3" -> 2;

                default -> -1;
            };
        }

        private static List<AcademicYearReference>
        buildAcademicYears(
                Map<String, AcademicYearReference> source
        ) {
            Map<Long, AcademicYearReference> byId =
                    new HashMap<>();

            for (
                    AcademicYearReference reference
                    : source.values()
            ) {
                if (
                        reference != null
                                && reference.getAcademicYearId() != null
                ) {
                    byId.putIfAbsent(
                            reference.getAcademicYearId(),
                            reference
                    );
                }
            }

            List<AcademicYearReference> ordered =
                    new ArrayList<>(
                            byId.values()
                    );

            ordered.sort(
                    (left, right) -> {
                        int currentYearComparison =
                                Boolean.compare(
                                        Boolean.TRUE.equals(
                                                right.getCurrentYear()
                                        ),
                                        Boolean.TRUE.equals(
                                                left.getCurrentYear()
                                        )
                                );

                        if (currentYearComparison != 0) {
                            return currentYearComparison;
                        }

                        LocalDate leftStart =
                                left.getStartDate();

                        LocalDate rightStart =
                                right.getStartDate();

                        if (
                                leftStart != null
                                        || rightStart != null
                        ) {
                            if (leftStart == null) {
                                return 1;
                            }

                            if (rightStart == null) {
                                return -1;
                            }

                            int startComparison =
                                    rightStart.compareTo(
                                            leftStart
                                    );

                            if (startComparison != 0) {
                                return startComparison;
                            }
                        }

                        return right.getAcademicYearId()
                                .compareTo(
                                        left.getAcademicYearId()
                                );
                    }
            );

            return List.copyOf(ordered);
        }

        private static List<LevelReference> buildLevels(
                Map<String, LevelReference> source
        ) {
            Map<Integer, LevelReference> byId =
                    new HashMap<>();

            for (LevelReference reference : source.values()) {
                if (
                        reference != null
                                && reference.getLevelId() != null
                ) {
                    byId.putIfAbsent(
                            reference.getLevelId(),
                            reference
                    );
                }
            }

            List<LevelReference> ordered =
                    new ArrayList<>(
                            byId.values()
                    );

            ordered.sort(
                    (left, right) -> {
                        Integer leftOrder =
                                left.getDisplayOrder();

                        Integer rightOrder =
                                right.getDisplayOrder();

                        if (
                                leftOrder != null
                                        || rightOrder != null
                        ) {
                            if (leftOrder == null) {
                                return 1;
                            }

                            if (rightOrder == null) {
                                return -1;
                            }

                            int orderComparison =
                                    leftOrder.compareTo(
                                            rightOrder
                                    );

                            if (orderComparison != 0) {
                                return orderComparison;
                            }
                        }

                        return safeText(
                                left.getLevelName()
                        ).compareTo(
                                safeText(
                                        right.getLevelName()
                                )
                        );
                    }
            );

            return List.copyOf(ordered);
        }

        private static Map<Integer, LevelReference>
        buildLevelsById(
                List<LevelReference> levels
        ) {
            Map<Integer, LevelReference> byId =
                    new HashMap<>();

            for (LevelReference level : levels) {
                byId.putIfAbsent(
                        level.getLevelId(),
                        level
                );
            }

            return Map.copyOf(byId);
        }

        private static Map<Integer, List<ClassReference>>
        buildClassesByLevel(
                Map<String, ClassReference> source
        ) {
            Map<Integer, Map<Integer, ClassReference>>
                    groupedById =
                    new HashMap<>();

            for (ClassReference schoolClass : source.values()) {
                if (
                        schoolClass == null
                                || schoolClass.getLevelId() == null
                                || schoolClass.getClassId() == null
                ) {
                    continue;
                }

                groupedById
                        .computeIfAbsent(
                                schoolClass.getLevelId(),
                                ignored -> new HashMap<>()
                        )
                        .putIfAbsent(
                                schoolClass.getClassId(),
                                schoolClass
                        );
            }

            Map<Integer, List<ClassReference>> result =
                    new HashMap<>();

            for (
                    Map.Entry<Integer, Map<Integer, ClassReference>>
                            entry
                    : groupedById.entrySet()
            ) {
                List<ClassReference> ordered =
                        new ArrayList<>(
                                entry.getValue()
                                        .values()
                        );

                ordered.sort(
                        (left, right) -> {
                            Integer leftOrder =
                                    left.getDisplayOrder();

                            Integer rightOrder =
                                    right.getDisplayOrder();

                            if (
                                    leftOrder != null
                                            || rightOrder != null
                            ) {
                                if (leftOrder == null) {
                                    return 1;
                                }

                                if (rightOrder == null) {
                                    return -1;
                                }

                                int orderComparison =
                                        leftOrder.compareTo(
                                                rightOrder
                                        );

                                if (orderComparison != 0) {
                                    return orderComparison;
                                }
                            }

                            return safeText(
                                    left.getClassName()
                            ).compareTo(
                                    safeText(
                                            right.getClassName()
                                    )
                            );
                        }
                );

                result.put(
                        entry.getKey(),
                        List.copyOf(ordered)
                );
            }

            return Map.copyOf(result);
        }

        private static Map<String, ClassReference>
        buildClassesByLevelAndKey(
                Map<String, ClassReference> source
        ) {
            Map<String, ClassReference> result =
                    new HashMap<>();

            Map<Integer, ClassReference> uniqueById =
                    new HashMap<>();

            for (ClassReference schoolClass : source.values()) {
                if (
                        schoolClass != null
                                && schoolClass.getClassId() != null
                ) {
                    uniqueById.putIfAbsent(
                            schoolClass.getClassId(),
                            schoolClass
                    );
                }
            }

            for (ClassReference schoolClass : uniqueById.values()) {
                putLevelClassKey(
                        result,
                        schoolClass,
                        schoolClass.getClassCode()
                );

                putLevelClassKey(
                        result,
                        schoolClass,
                        schoolClass.getClassName()
                );

                putLevelClassKey(
                        result,
                        schoolClass,
                        String.valueOf(
                                schoolClass.getClassId()
                        )
                );
            }

            return Map.copyOf(result);
        }

        private static void putLevelClassKey(
                Map<String, ClassReference> target,
                ClassReference schoolClass,
                String rawKey
        ) {
            if (
                    schoolClass == null
                            || schoolClass.getLevelId() == null
            ) {
                return;
            }

            String normalized =
                    normalizeKey(rawKey);

            if (normalized == null) {
                return;
            }

            target.putIfAbsent(
                    schoolClass.getLevelId()
                            + ":"
                            + normalized,
                    schoolClass
            );
        }

        private static Map<String, LevelReference>
        buildCanonicalLevels(
                List<LevelReference> levels
        ) {
            Map<String, LevelReference> result =
                    new HashMap<>();

            for (LevelReference level : levels) {
                if (level == null) {
                    continue;
                }

                String canonicalLevel =
                        safelyNormalizeLevel(
                                level.getLevelName()
                        );

                if (canonicalLevel != null) {
                    result.putIfAbsent(
                            canonicalLevel,
                            level
                    );
                }
            }

            return Map.copyOf(result);
        }

        private static Map<String, ClassReference>
        buildCanonicalClasses(
                Map<String, ClassReference> source,
                Map<Integer, LevelReference> levelsById
        ) {
            Map<Integer, ClassReference> uniqueById =
                    new HashMap<>();

            for (ClassReference schoolClass : source.values()) {
                if (
                        schoolClass != null
                                && schoolClass.getClassId() != null
                ) {
                    uniqueById.putIfAbsent(
                            schoolClass.getClassId(),
                            schoolClass
                    );
                }
            }

            Map<String, ClassReference> result =
                    new HashMap<>();

            for (ClassReference schoolClass : uniqueById.values()) {
                LevelReference level =
                        levelsById.get(
                                schoolClass.getLevelId()
                        );

                String canonicalLevel =
                        level == null
                                ? null
                                : safelyNormalizeLevel(
                                level.getLevelName()
                        );

                if (canonicalLevel == null) {
                    continue;
                }

                String canonicalClass =
                        safelyNormalizeClass(
                                schoolClass.getClassCode(),
                                canonicalLevel
                        );

                if (canonicalClass == null) {
                    canonicalClass =
                            safelyNormalizeClass(
                                    schoolClass.getClassName(),
                                    canonicalLevel
                            );
                }

                if (canonicalClass != null) {
                    result.putIfAbsent(
                            canonicalLevel
                                    + ":"
                                    + canonicalClass,
                            schoolClass
                    );
                }
            }

            return Map.copyOf(result);
        }

        private static String safelyNormalizeLevel(
                String value
        ) {
            try {
                return StudentEducationClassNormalizer
                        .normalizeLevel(value);
            } catch (IllegalArgumentException exception) {
                return null;
            }
        }

        private static String safelyNormalizeClass(
                String value,
                String canonicalLevel
        ) {
            try {
                return StudentEducationClassNormalizer
                        .normalizeClass(
                                value,
                                canonicalLevel
                        );
            } catch (IllegalArgumentException exception) {
                return null;
            }
        }

        private static String normalizeKey(
                String value
        ) {
            if (value == null) {
                return null;
            }

            String normalized =
                    value.trim()
                            .toUpperCase(Locale.ROOT)
                            .replaceAll(
                                    "[\\s_-]+",
                                    ""
                            );

            return normalized.isEmpty()
                    ? null
                    : normalized;
        }

        private static String safeText(
                String value
        ) {
            return value == null
                    ? ""
                    : value;
        }
    }

    // =====================================================================
    // REFERENCE TYPES
    // =====================================================================

    @Getter
    public static final class BranchReference {

        private final Integer branchId;
        private final String branchName;
        private final String schoolCode;

        private BranchReference(
                Integer branchId,
                String branchName,
                String schoolCode
        ) {
            this.branchId = branchId;
            this.branchName = branchName;
            this.schoolCode = schoolCode;
        }
    }

    @Getter
    public static final class AcademicYearReference {

        private final Long academicYearId;
        private final String academicYearCode;
        private final String academicYearName;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final String status;
        private final Boolean currentYear;

        private AcademicYearReference(
                Long academicYearId,
                String academicYearCode,
                String academicYearName,
                LocalDate startDate,
                LocalDate endDate,
                String status,
                Boolean currentYear
        ) {
            this.academicYearId = academicYearId;
            this.academicYearCode = academicYearCode;
            this.academicYearName = academicYearName;
            this.startDate = startDate;
            this.endDate = endDate;
            this.status = status;
            this.currentYear = currentYear;
        }
    }

    @Getter
    public static final class LevelReference {

        private final Integer levelId;
        private final String levelName;
        private final Integer displayOrder;

        private LevelReference(
                Integer levelId,
                String levelName,
                Integer displayOrder
        ) {
            this.levelId = levelId;
            this.levelName = levelName;
            this.displayOrder = displayOrder;
        }
    }

    @Getter
    public static final class ClassReference {

        private final Integer classId;
        private final Integer levelId;
        private final String classCode;
        private final String className;
        private final Integer displayOrder;

        private ClassReference(
                Integer classId,
                Integer levelId,
                String classCode,
                String className,
                Integer displayOrder
        ) {
            this.classId = classId;
            this.levelId = levelId;
            this.classCode = classCode;
            this.className = className;
            this.displayOrder = displayOrder;
        }
    }

    @Getter
    public static final class SectionReference {

        private final Long sectionId;
        private final Long academicYearId;
        private final Integer classId;
        private final String sectionCode;
        private final String sectionName;

        private SectionReference(
                Long sectionId,
                Long academicYearId,
                Integer classId,
                String sectionCode,
                String sectionName
        ) {
            this.sectionId = sectionId;
            this.academicYearId = academicYearId;
            this.classId = classId;
            this.sectionCode = sectionCode;
            this.sectionName = sectionName;
        }
    }
}
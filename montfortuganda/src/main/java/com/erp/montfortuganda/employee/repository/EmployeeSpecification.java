package com.erp.montfortuganda.employee.repository;

import com.erp.montfortuganda.employee.dto.request.EmployeeSearchRequest;
import com.erp.montfortuganda.employee.entity.ErpEmployee;
import com.erp.montfortuganda.school.entity.Department;
import com.erp.montfortuganda.school.entity.Designation;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Builds secure, branch-scoped Employee search specifications.

 * The authenticated branch ID is mandatory and is always included in the
 * generated query. A branch ID is never accepted from the browser.
 */
public final class EmployeeSpecification {

    private static final char LIKE_ESCAPE_CHARACTER = '\\';

    private EmployeeSpecification() {
    }

    public static Specification<ErpEmployee> getSearchSpecification(
            Integer branchId,
            EmployeeSearchRequest request
    ) {
        Objects.requireNonNull(
                branchId,
                "Authenticated branch ID is required for Employee search."
        );

        return (
                root,
                query,
                criteriaBuilder
        ) -> {
            List<Predicate> predicates =
                    new ArrayList<>();

            predicates.add(
                    criteriaBuilder.equal(
                            root.get("branch")
                                    .get("branchId"),
                            branchId
                    )
            );

            if (request == null) {
                return criteriaBuilder.and(
                        predicates.toArray(
                                Predicate[]::new
                        )
                );
            }

            addKeywordPredicate(
                    predicates,
                    root,
                    criteriaBuilder,
                    request.keyword()
            );

            addContainsIgnoreCase(
                    predicates,
                    criteriaBuilder,
                    root.get("employeeNo"),
                    request.employeeNo()
            );

            addContainsIgnoreCase(
                    predicates,
                    criteriaBuilder,
                    root.get("officialEmail"),
                    request.officialEmail()
            );

            addContainsIgnoreCase(
                    predicates,
                    criteriaBuilder,
                    root.get("mobileNo"),
                    request.mobileNo()
            );

            addEqual(
                    predicates,
                    criteriaBuilder,
                    root.get("department")
                            .get("departmentId"),
                    request.departmentId()
            );

            addEqual(
                    predicates,
                    criteriaBuilder,
                    root.get("designation")
                            .get("designationId"),
                    request.designationId()
            );

            addEqual(
                    predicates,
                    criteriaBuilder,
                    root.get("reportingManager")
                            .get("employeeId"),
                    request.reportingManagerId()
            );

            addEqual(
                    predicates,
                    criteriaBuilder,
                    root.get("employeeCategory"),
                    request.employeeCategory()
            );

            addEqual(
                    predicates,
                    criteriaBuilder,
                    root.get("employeeType"),
                    request.employeeType()
            );

            addEqual(
                    predicates,
                    criteriaBuilder,
                    root.get("employmentMode"),
                    request.employmentMode()
            );

            addEqual(
                    predicates,
                    criteriaBuilder,
                    root.get("employmentStatus"),
                    request.employmentStatus()
            );

            addEqual(
                    predicates,
                    criteriaBuilder,
                    root.get("gender"),
                    request.gender()
            );

            addDateRange(
                    predicates,
                    criteriaBuilder,
                    root.get("dateOfBirth"),
                    request.dateOfBirthFrom(),
                    request.dateOfBirthTo()
            );

            addDateRange(
                    predicates,
                    criteriaBuilder,
                    root.get("joiningDate"),
                    request.joiningDateFrom(),
                    request.joiningDateTo()
            );

            addDateRange(
                    predicates,
                    criteriaBuilder,
                    root.get("employmentEndDate"),
                    request.employmentEndDateFrom(),
                    request.employmentEndDateTo()
            );

            addEqual(
                    predicates,
                    criteriaBuilder,
                    root.get("loginEnabled"),
                    request.loginEnabled()
            );

            addEqual(
                    predicates,
                    criteriaBuilder,
                    root.get("active"),
                    request.active()
            );

            return criteriaBuilder.and(
                    predicates.toArray(
                            Predicate[]::new
                    )
            );
        };
    }

    private static void addKeywordPredicate(
            List<Predicate> predicates,
            Root<ErpEmployee> root,
            CriteriaBuilder criteriaBuilder,
            String keyword
    ) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }

        String pattern =
                toContainsPattern(keyword);

        Join<ErpEmployee, Department> department =
                root.join(
                        "department",
                        JoinType.LEFT
                );

        Join<ErpEmployee, Designation> designation =
                root.join(
                        "designation",
                        JoinType.LEFT
                );

        Join<ErpEmployee, ErpEmployee> reportingManager =
                root.join(
                        "reportingManager",
                        JoinType.LEFT
                );

        predicates.add(
                criteriaBuilder.or(
                        likeIgnoreCase(
                                criteriaBuilder,
                                root.get("employeeNo"),
                                pattern
                        ),
                        likeIgnoreCase(
                                criteriaBuilder,
                                root.get("fullName"),
                                pattern
                        ),
                        likeIgnoreCase(
                                criteriaBuilder,
                                root.get("officialEmail"),
                                pattern
                        ),
                        likeIgnoreCase(
                                criteriaBuilder,
                                root.get("personalEmail"),
                                pattern
                        ),
                        likeIgnoreCase(
                                criteriaBuilder,
                                root.get("mobileNo"),
                                pattern
                        ),
                        likeIgnoreCase(
                                criteriaBuilder,
                                root.get("alternateMobile"),
                                pattern
                        ),
                        likeIgnoreCase(
                                criteriaBuilder,
                                department.get("departmentCode"),
                                pattern
                        ),
                        likeIgnoreCase(
                                criteriaBuilder,
                                department.get("departmentName"),
                                pattern
                        ),
                        likeIgnoreCase(
                                criteriaBuilder,
                                designation.get("designationCode"),
                                pattern
                        ),
                        likeIgnoreCase(
                                criteriaBuilder,
                                designation.get("designationName"),
                                pattern
                        ),
                        likeIgnoreCase(
                                criteriaBuilder,
                                reportingManager.get("employeeNo"),
                                pattern
                        ),
                        likeIgnoreCase(
                                criteriaBuilder,
                                reportingManager.get("fullName"),
                                pattern
                        )
                )
        );
    }

    private static Predicate likeIgnoreCase(
            CriteriaBuilder criteriaBuilder,
            jakarta.persistence.criteria.Expression<String> field,
            String pattern
    ) {
        return criteriaBuilder.like(
                criteriaBuilder.lower(field),
                pattern,
                LIKE_ESCAPE_CHARACTER
        );
    }

    private static void addContainsIgnoreCase(
            List<Predicate> predicates,
            CriteriaBuilder criteriaBuilder,
            jakarta.persistence.criteria.Expression<String> field,
            String value
    ) {
        if (!StringUtils.hasText(value)) {
            return;
        }

        predicates.add(
                likeIgnoreCase(
                        criteriaBuilder,
                        field,
                        toContainsPattern(value)
                )
        );
    }

    private static <T> void addEqual(
            List<Predicate> predicates,
            CriteriaBuilder criteriaBuilder,
            jakarta.persistence.criteria.Expression<T> field,
            T value
    ) {
        if (value == null) {
            return;
        }

        predicates.add(
                criteriaBuilder.equal(
                        field,
                        value
                )
        );
    }

    private static void addDateRange(
            List<Predicate> predicates,
            CriteriaBuilder criteriaBuilder,
            jakarta.persistence.criteria.Expression<LocalDate> field,
            LocalDate from,
            LocalDate to
    ) {
        if (from != null) {
            predicates.add(
                    criteriaBuilder.greaterThanOrEqualTo(
                            field,
                            from
                    )
            );
        }

        if (to != null) {
            predicates.add(
                    criteriaBuilder.lessThanOrEqualTo(
                            field,
                            to
                    )
            );
        }
    }

    private static String toContainsPattern(
            String value
    ) {
        String normalized =
                value.trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        String escaped =
                normalized
                        .replace(
                                "\\",
                                "\\\\"
                        )
                        .replace(
                                "%",
                                "\\%"
                        )
                        .replace(
                                "_",
                                "\\_"
                        );

        return "%"
                + escaped
                + "%";
    }
}
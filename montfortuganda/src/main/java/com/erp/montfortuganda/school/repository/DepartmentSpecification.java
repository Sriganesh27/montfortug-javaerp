package com.erp.montfortuganda.school.repository;

import com.erp.montfortuganda.model.enums.RecordStatus;
import com.erp.montfortuganda.school.entity.Department;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class DepartmentSpecification {

    public static Specification<Department> getSearchSpecification(
            String keyword, Integer branchId, RecordStatus status, Boolean active, Boolean isAcademic) {

        return (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            if (branchId != null) {
                predicates.add(cb.equal(root.get("branch").get("branchId"), branchId));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            if (isAcademic != null) {
                predicates.add(cb.equal(root.get("isAcademic"), isAcademic));
            }

            if (StringUtils.hasText(keyword)) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("departmentName")), likePattern),
                        cb.like(cb.lower(root.get("departmentCode")), likePattern)
                ));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
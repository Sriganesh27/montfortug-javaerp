package com.erp.montfortuganda.school.repository;

import com.erp.montfortuganda.model.enums.RecordStatus;
import com.erp.montfortuganda.school.entity.Designation;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class DesignationSpecification {

    public static Specification<Designation> getSearchSpecification(
            String keyword, RecordStatus status, Boolean active) {

        return (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            if (StringUtils.hasText(keyword)) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("designationName")), likePattern),
                        cb.like(cb.lower(root.get("designationCode")), likePattern)
                ));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
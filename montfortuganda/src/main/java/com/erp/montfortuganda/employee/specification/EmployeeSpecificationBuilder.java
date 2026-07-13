package com.erp.montfortuganda.employee.specification;

import com.erp.montfortuganda.employee.dto.EmployeeSearchCriteria;
import com.erp.montfortuganda.employee.entity.ErpEmployee;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Component
public class EmployeeSpecificationBuilder {

    public Specification<ErpEmployee> build(EmployeeSearchCriteria criteria, Integer branchId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter by branch!
            predicates.add(cb.equal(root.get("branch").get("branchId"), branchId));
            
            // Active filter (Boolean true)
            predicates.add(cb.isTrue(root.get("active")));

            if (criteria.getKeyword() != null && !criteria.getKeyword().isEmpty()) {
                String pattern = "%" + criteria.getKeyword().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("fullName")), pattern);
                Predicate emailMatch = cb.like(cb.lower(root.get("officialEmail")), pattern);
                Predicate codeMatch = cb.like(cb.lower(root.get("employeeNo")), pattern);
                predicates.add(cb.or(nameMatch, emailMatch, codeMatch));
            }

            if (criteria.getDepartmentId() != null) {
                predicates.add(cb.equal(root.get("department").get("departmentId"), criteria.getDepartmentId()));
            }

            if (criteria.getDesignationId() != null) {
                predicates.add(cb.equal(root.get("designation").get("designationId"), criteria.getDesignationId()));
            }

            if (criteria.getCategory() != null) {
                predicates.add(cb.equal(root.get("employeeCategory"), criteria.getCategory()));
            }

            if (criteria.getStatus() != null) {
                predicates.add(cb.equal(root.get("employmentStatus"), criteria.getStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
// File: src/main/java/com/erp/montfortuganda/employee/generator/EmployeeCodeGeneratorImpl.java
package com.erp.montfortuganda.employee.generator;

import com.erp.montfortuganda.employee.enums.EmployeeCategory;
import com.erp.montfortuganda.employee.repository.EmployeeRepository;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EmployeeCodeGeneratorImpl implements EmployeeCodeGenerator {

    private final EmployeeRepository employeeRepository;
    private final BranchRepository branchRepository;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String generateCode(Integer branchId, EmployeeCategory category, LocalDate joiningDate) {

        // 1. Fetch School Code (e.g., U011, U021).
        // IDE fix: No need for 'code != null' inside filter, Optional handles it.
        String schoolCode = branchRepository.findById(branchId)
                .map(Branch::getSchoolCode)
                .filter(code -> !code.isEmpty())
                .orElse("NA");

        // 2. Map the Category Enum to a strict short prefix
        String categoryPrefix = getCategoryPrefix(category);

        // 3. Extract 2-digit joining year (e.g., 2026 -> "26")
        String yearCode = joiningDate != null
                ? joiningDate.format(DateTimeFormatter.ofPattern("yy"))
                : LocalDate.now().format(DateTimeFormatter.ofPattern("yy"));

        // 4. Build exact Prefix: "U011-T-26-"
        String prefix = String.format("%s-%s-%s-", schoolCode, categoryPrefix, yearCode);

        // 5. Fetch the highest existing sequence for this prefix
        String maxCode = employeeRepository.findMaxEmployeeNoByPrefix(branchId, prefix);

        int nextSequence = 1;

        if (maxCode != null && !maxCode.isEmpty()) {
            try {
                // Extract the last part (e.g. from U011-T-26-001 -> "001")
                String[] parts = maxCode.split("-");
                String lastPart = parts[parts.length - 1];
                nextSequence = Integer.parseInt(lastPart) + 1;
            } catch (Exception ignored) {
                // IDE fix: Removed redundant 'nextSequence = 1' since it's already 1
            }
        }

        // 6. Format sequence as 3 digits and append it (e.g., U011-T-26-001)
        return prefix + String.format("%03d", nextSequence);
    }

    /**
     * Maps the strict EmployeeCategory enum to a short string prefix.
     */
    private String getCategoryPrefix(EmployeeCategory category) {
        if (category == null) return "EMP";

        // IDE fix: Removed 'default' branch because all Enum values are covered.
        return switch (category) {
            case TEACHING -> "T";
            case NON_TEACHING -> "NT";
            case MANAGEMENT_TEACHING, MANAGEMENT_NON_TEACHING -> "MGT";
            case SUPPORT_STAFF -> "SS";
        };
    }
}
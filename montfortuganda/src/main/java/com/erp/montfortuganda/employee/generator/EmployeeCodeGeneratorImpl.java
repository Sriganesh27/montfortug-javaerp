package com.erp.montfortuganda.employee.generator;

import com.erp.montfortuganda.employee.enums.EmployeeCategory;
import com.erp.montfortuganda.employee.repository.EmployeeSequenceRepository;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmployeeCodeGeneratorImpl implements EmployeeCodeGenerator {

    private static final DateTimeFormatter YEAR_FORMAT =
            DateTimeFormatter.ofPattern("yy");

    private static final int MAX_SEQUENCE_NUMBER = 999;

    private final EmployeeSequenceRepository employeeSequenceRepository;
    private final BranchRepository branchRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public String generateCode(
            Integer branchId,
            EmployeeCategory category,
            LocalDate joiningDate
    ) {
        validateRequest(branchId, category, joiningDate);

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Branch not found with ID: " + branchId
                ));

        String schoolCode = normalizeSchoolCode(
                branch.getSchoolCode()
        );

        int sequenceYear = joiningDate.getYear();

        employeeSequenceRepository.incrementSequence(
                branchId,
                category.name(),
                sequenceYear
        );

        Integer sequenceNumber =
                employeeSequenceRepository.findCurrentNumber(
                        branchId,
                        category.name(),
                        sequenceYear
                );

        if (sequenceNumber == null || sequenceNumber <= 0) {
            throw new IllegalStateException(
                    "Employee sequence number could not be generated."
            );
        }

        if (sequenceNumber > MAX_SEQUENCE_NUMBER) {
            throw new IllegalStateException(
                    "Employee sequence limit exceeded for branch "
                            + schoolCode
                            + ", category "
                            + category.name()
                            + " and year "
                            + sequenceYear
            );
        }

        String yearCode = joiningDate.format(YEAR_FORMAT);
        String categoryCode = getCategoryCode(category);

        return String.format(
                Locale.ROOT,
                "%s-%s-%s-%03d",
                schoolCode,
                categoryCode,
                yearCode,
                sequenceNumber
        );
    }

    private String getCategoryCode(
            EmployeeCategory category
    ) {
        return switch (category) {
            case TEACHING -> "T";
            case NON_TEACHING -> "NT";
            case MANAGEMENT_TEACHING -> "MT";
            case MANAGEMENT_NON_TEACHING -> "MNT";
            case SUPPORT_STAFF -> "SS";
        };
    }

    private String normalizeSchoolCode(
            String schoolCode
    ) {
        if (schoolCode == null || schoolCode.isBlank()) {
            throw new IllegalStateException(
                    "The selected branch does not have a school code."
            );
        }

        return schoolCode
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private void validateRequest(
            Integer branchId,
            EmployeeCategory category,
            LocalDate joiningDate
    ) {
        if (branchId == null || branchId <= 0) {
            throw new IllegalArgumentException(
                    "A valid branch ID is required."
            );
        }

        if (category == null) {
            throw new IllegalArgumentException(
                    "Employee category is required."
            );
        }

        if (joiningDate == null) {
            throw new IllegalArgumentException(
                    "Joining date is required."
            );
        }
    }
}
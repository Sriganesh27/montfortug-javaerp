package com.erp.montfortuganda.admission.service;

import com.erp.montfortuganda.admission.dto.ApplicationEmployeeOptionDTO;
import com.erp.montfortuganda.auth.service.BranchAccessService;
import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.employee.entity.ErpEmployee;
import com.erp.montfortuganda.employee.enums.EmploymentStatus;
import com.erp.montfortuganda.employee.repository.ErpEmployeeRepository;
import com.erp.montfortuganda.school.entity.Department;
import com.erp.montfortuganda.school.entity.Designation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Branch-scoped employee option provider for Admission workflow assignments.
 */
@Service
@Transactional(readOnly = true)
public class ApplicationEmployeeOptionServiceImpl
        implements ApplicationEmployeeOptionService {

    private final ErpEmployeeRepository employeeRepository;
    private final BranchAccessService branchAccessService;

    public ApplicationEmployeeOptionServiceImpl(
            ErpEmployeeRepository employeeRepository,
            BranchAccessService branchAccessService
    ) {
        this.employeeRepository = employeeRepository;
        this.branchAccessService = branchAccessService;
    }

    @Override
    public List<ApplicationEmployeeOptionDTO> getEligibleEmployees(
            CurrentUserContext context
    ) {
        Integer branchId =
                branchAccessService.getValidatedBranchId(
                        context
                );

        return employeeRepository
                .findAllByBranch_BranchIdAndActiveTrueOrderByFullNameAsc(
                        branchId
                )
                .stream()
                .filter(this::isEligible)
                .map(this::toOption)
                .toList();
    }

    private boolean isEligible(
            ErpEmployee employee
    ) {
        return employee != null
                && Boolean.TRUE.equals(
                        employee.getActive()
                )
                && employee.getEmploymentStatus()
                == EmploymentStatus.ACTIVE;
    }

    private ApplicationEmployeeOptionDTO toOption(
            ErpEmployee employee
    ) {
        Department department =
                employee.getDepartment();

        Designation designation =
                employee.getDesignation();

        return new ApplicationEmployeeOptionDTO(
                employee.getEmployeeId(),
                employee.getEmployeeNo(),
                employee.getFullName(),
                department == null
                        ? null
                        : department.getDepartmentName(),
                designation == null
                        ? null
                        : designation.getDesignationName()
        );
    }
}

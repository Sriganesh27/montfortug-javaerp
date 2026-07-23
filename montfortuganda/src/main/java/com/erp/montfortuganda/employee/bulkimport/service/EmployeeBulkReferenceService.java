package com.erp.montfortuganda.employee.bulkimport.service;

import com.erp.montfortuganda.employee.bulkimport.excel.EmployeeExcelValueParser;
import com.erp.montfortuganda.employee.entity.ErpEmployee;
import com.erp.montfortuganda.employee.repository.ErpEmployeeRepository;
import com.erp.montfortuganda.school.entity.Department;
import com.erp.montfortuganda.school.entity.Designation;
import com.erp.montfortuganda.school.repository.DepartmentRepository;
import com.erp.montfortuganda.school.repository.DesignationRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads branch/reference data once for the complete Employee import job.

 * This avoids querying the database separately for every Excel row.
 */
@Service
@RequiredArgsConstructor
public class EmployeeBulkReferenceService {

    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final ErpEmployeeRepository employeeRepository;
    private final EmployeeExcelValueParser valueParser;

    /**
     * Expected complexity:

     * Time: O(d + g + e)
     * Space: O(d + g + e)

     * d = departments
     * g = designations
     * e = active branch employees
     */
    @Transactional(readOnly = true)
    public EmployeeBulkReferenceData loadReferences(
            Integer branchId
    ) {
        if (branchId == null || branchId <= 0) {
            throw new IllegalArgumentException(
                    "A valid branch is required for Employee import"
            );
        }

        List<Department> departments =
                departmentRepository
                        .findAllByBranch_BranchIdAndActiveTrueOrderByDepartmentNameAsc(
                                branchId
                        );

        List<Designation> designations =
                designationRepository
                        .findAllByActiveTrueOrderByDesignationNameAsc();

        List<ErpEmployee> employees =
                employeeRepository
                        .findAllByBranch_BranchIdAndActiveTrueOrderByFullNameAsc(
                                branchId
                        );

        Map<String, Department> departmentsByName =
                new HashMap<>();

        for (Department department : departments) {
            String key = valueParser.normalizeLookupKey(
                    department.getDepartmentName()
            );

            if (key != null) {
                departmentsByName.put(key, department);
            }
        }

        Map<String, Designation> designationsByName =
                new HashMap<>();

        for (Designation designation : designations) {
            String key = valueParser.normalizeLookupKey(
                    designation.getDesignationName()
            );

            if (key != null) {
                designationsByName.put(key, designation);
            }
        }

        Map<String, ErpEmployee> employeesByEmployeeNo =
                new HashMap<>();

        for (ErpEmployee employee : employees) {
            String key = valueParser.normalizeLookupKey(
                    employee.getEmployeeNo()
            );

            if (key != null) {
                employeesByEmployeeNo.put(key, employee);
            }
        }

        return new EmployeeBulkReferenceData(
                branchId,
                departmentsByName,
                designationsByName,
                employeesByEmployeeNo
        );
    }

    @Getter
    public static final class EmployeeBulkReferenceData {

        private final Integer branchId;

        private final Map<String, Department> departmentsByName;

        private final Map<String, Designation> designationsByName;

        private final Map<String, ErpEmployee> employeesByEmployeeNo;

        private EmployeeBulkReferenceData(
                Integer branchId,
                Map<String, Department> departmentsByName,
                Map<String, Designation> designationsByName,
                Map<String, ErpEmployee> employeesByEmployeeNo
        ) {
            this.branchId = branchId;

            this.departmentsByName =
                    Map.copyOf(departmentsByName);

            this.designationsByName =
                    Map.copyOf(designationsByName);

            this.employeesByEmployeeNo =
                    Map.copyOf(employeesByEmployeeNo);
        }

        public Department findDepartment(
                String normalizedName
        ) {
            if (normalizedName == null) {
                return null;
            }

            return departmentsByName.get(normalizedName);
        }

        public Designation findDesignation(
                String normalizedName
        ) {
            if (normalizedName == null) {
                return null;
            }

            return designationsByName.get(normalizedName);
        }

        public ErpEmployee findReportingManager(
                String normalizedEmployeeNo
        ) {
            if (normalizedEmployeeNo == null) {
                return null;
            }

            return employeesByEmployeeNo.get(
                    normalizedEmployeeNo
            );
        }
    }
}
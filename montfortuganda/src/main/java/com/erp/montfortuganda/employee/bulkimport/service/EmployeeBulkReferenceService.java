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
 *
 * <p>Both master-data names and codes are indexed. Therefore values such as
 * {@code Head Teacher}, {@code HEAD_TEACHER} and {@code HEADTEACHER} resolve to
 * the same active designation when that designation exists.</p>
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
     *
     * <p>Time: O(d + g + e)</p>
     * <p>Space: O(d + g + e)</p>
     *
     * <p>d = departments, g = designations, e = active branch employees</p>
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

        Map<String, Department> departmentsByKey =
                new HashMap<>();

        for (Department department : departments) {
            putReference(
                    departmentsByKey,
                    department.getDepartmentName(),
                    department
            );
            putReference(
                    departmentsByKey,
                    department.getDepartmentCode(),
                    department
            );
        }

        Map<String, Designation> designationsByKey =
                new HashMap<>();

        for (Designation designation : designations) {
            putReference(
                    designationsByKey,
                    designation.getDesignationName(),
                    designation
            );
            putReference(
                    designationsByKey,
                    designation.getDesignationCode(),
                    designation
            );
        }

        Map<String, ErpEmployee> employeesByEmployeeNo =
                new HashMap<>();

        for (ErpEmployee employee : employees) {
            putReference(
                    employeesByEmployeeNo,
                    employee.getEmployeeNo(),
                    employee
            );
        }

        return new EmployeeBulkReferenceData(
                branchId,
                departmentsByKey,
                designationsByKey,
                employeesByEmployeeNo
        );
    }

    private <T> void putReference(
            Map<String, T> target,
            String rawKey,
            T value
    ) {
        String normalizedKey =
                valueParser.normalizeLookupKey(rawKey);

        if (normalizedKey != null) {
            target.putIfAbsent(normalizedKey, value);
        }
    }

    @Getter
    public static final class EmployeeBulkReferenceData {

        private final Integer branchId;
        private final Map<String, Department> departmentsByKey;
        private final Map<String, Designation> designationsByKey;
        private final Map<String, ErpEmployee> employeesByEmployeeNo;

        private EmployeeBulkReferenceData(
                Integer branchId,
                Map<String, Department> departmentsByKey,
                Map<String, Designation> designationsByKey,
                Map<String, ErpEmployee> employeesByEmployeeNo
        ) {
            this.branchId = branchId;
            this.departmentsByKey = Map.copyOf(departmentsByKey);
            this.designationsByKey = Map.copyOf(designationsByKey);
            this.employeesByEmployeeNo = Map.copyOf(employeesByEmployeeNo);
        }

        public Department findDepartment(String normalizedKey) {
            if (normalizedKey == null) {
                return null;
            }

            return departmentsByKey.get(normalizedKey);
        }

        public Designation findDesignation(String normalizedKey) {
            if (normalizedKey == null) {
                return null;
            }

            return designationsByKey.get(normalizedKey);
        }

        public ErpEmployee findReportingManager(
                String normalizedEmployeeNo
        ) {
            if (normalizedEmployeeNo == null) {
                return null;
            }

            return employeesByEmployeeNo.get(normalizedEmployeeNo);
        }
    }
}

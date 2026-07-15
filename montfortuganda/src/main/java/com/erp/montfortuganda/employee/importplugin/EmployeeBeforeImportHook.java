package com.erp.montfortuganda.employee.importplugin;

import com.erp.montfortuganda.common.importframework.context.ImportContext;
import com.erp.montfortuganda.common.importframework.spi.BeforeImportHook;
import com.erp.montfortuganda.school.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmployeeBeforeImportHook implements BeforeImportHook {

    private final BranchRepository branchRepository;

    // Fix: Renamed from execute() to onBeforeImport() to match the BeforeImportHook interface
    @Override
    public void onBeforeImport(ImportContext context) {
        if (context.getBranchId() != null) {
            boolean exists = branchRepository.existsById(Integer.valueOf(context.getBranchId()));
            if (!exists) {
                throw new IllegalStateException("The specified Branch (ID: " + context.getBranchId() + ") does not exist or was deleted.");
            }
        }
    }
}
// File: src/main/java/com/erp/montfortuganda/employee/service/EmployeeImportService.java
package com.erp.montfortuganda.employee.service;

import com.erp.montfortuganda.auth.service.CurrentUserContext;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

public interface EmployeeImportService {
    // Returns a map of success count and failure logs
    Map<String, Object> processExcelImport(MultipartFile file, CurrentUserContext ctx);
}
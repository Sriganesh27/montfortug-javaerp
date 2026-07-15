package com.erp.montfortuganda.employee.importplugin;

import com.erp.montfortuganda.common.importframework.context.ImportContext;
import com.erp.montfortuganda.common.importframework.model.ErpImportError;
import com.erp.montfortuganda.common.importframework.plugin.ChunkProcessingResult;
import com.erp.montfortuganda.common.importframework.plugin.PluginProcessor;
import com.erp.montfortuganda.employee.entity.ErpEmployee;
import com.erp.montfortuganda.employee.enums.*;
import com.erp.montfortuganda.employee.repository.EmployeeRepository;
import com.erp.montfortuganda.school.repository.BranchRepository;
import com.erp.montfortuganda.school.Branch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmployeePluginProcessor implements PluginProcessor<EmployeeImportDTO> {

    private final EmployeeRepository employeeRepository;
    private final BranchRepository branchRepository;
    private final TransactionTemplate transactionTemplate;

    @Override
    public ChunkProcessingResult processChunk(List<EmployeeImportDTO> validDtos, ImportContext context) {

        int succeeded = 0;
        int failed = 0;
        long start = System.currentTimeMillis();
        List<ErpImportError> processingErrors = new ArrayList<>(); // To track DB Rejections

        Branch branch = null;
        if (context.getBranchId() != null) {
            try {
                Optional<Branch> branchOpt = branchRepository.findById(Integer.valueOf(context.getBranchId()));
                if (branchOpt.isPresent()) branch = branchOpt.get();
            } catch (NumberFormatException ignored) {}
        }
        final Branch finalBranch = branch;

        for (EmployeeImportDTO dto : validDtos) {
            if (finalBranch == null) {
                failed++;
                continue;
            }

            Boolean result = transactionTemplate.execute(status -> {
                try {
                    ErpEmployee employee = new ErpEmployee();
                    employee.setEmployeeNo("EMP-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                    employee.setBranch(finalBranch);

                    employee.setFirstName(dto.getFirstName());
                    employee.setMiddleName(dto.getMiddleName());
                    employee.setLastName(dto.getLastName());
                    employee.setTitle(dto.getTitle());

                    StringBuilder fullName = new StringBuilder();
                    if (dto.getFirstName() != null) fullName.append(dto.getFirstName()).append(" ");
                    if (dto.getMiddleName() != null && !dto.getMiddleName().isBlank()) fullName.append(dto.getMiddleName()).append(" ");
                    if (dto.getLastName() != null) fullName.append(dto.getLastName());
                    employee.setFullName(fullName.toString().trim());

                    if (dto.getGender() != null && !dto.getGender().isBlank()) {
                        try { employee.setGender(Gender.valueOf(dto.getGender().trim().toUpperCase())); }
                        catch (IllegalArgumentException ignored) {}
                    }

                    employee.setOfficialEmail(dto.getOfficialEmail());
                    employee.setPersonalEmail(dto.getPersonalEmail());
                    employee.setMobileNo(dto.getMobileNumber());
                    employee.setAlternateMobile(dto.getAlternateMobile());
                    employee.setNationality(dto.getNationality());
                    employee.setNationalId(dto.getNationalId());
                    employee.setAddressDistrict(dto.getDistrict());
                    employee.setAddressCounty(dto.getCounty());
                    employee.setAddressSubCounty(dto.getSubCounty());
                    employee.setAddressParish(dto.getParish());
                    employee.setAddressVillage(dto.getVillage());
                    employee.setAddressStreet(dto.getStreet());
                    employee.setPostalCode(dto.getPostalCode());

                    EmployeeType empType = EmployeeType.PERMANENT;
                    if (dto.getEmployeeType() != null && !dto.getEmployeeType().isBlank()) {
                        try { empType = EmployeeType.valueOf(dto.getEmployeeType().trim().toUpperCase().replace(" ", "_")); }
                        catch (IllegalArgumentException ignored) {}
                    }
                    employee.setEmployeeType(empType);

                    if (dto.getEmploymentMode() != null && !dto.getEmploymentMode().isBlank()) {
                        try { employee.setEmploymentMode(EmploymentMode.valueOf(dto.getEmploymentMode().trim().toUpperCase().replace(" ", "_"))); }
                        catch (IllegalArgumentException ignored) {}
                    }

                    if (dto.getEmployeeCategory() != null && !dto.getEmployeeCategory().isBlank()) {
                        try { employee.setEmployeeCategory(EmployeeCategory.valueOf(dto.getEmployeeCategory().trim().toUpperCase().replace(" ", "_"))); }
                        catch (IllegalArgumentException ignored) {}
                    }

                    employee.setEmploymentStatus(EmploymentStatus.ACTIVE);
                    employee.setDateOfBirth(parseDate(dto.getDateOfBirth()));
                    employee.setJoiningDate(parseDate(dto.getJoiningDate()));

                    String loginVal = dto.getLoginEnabled();
                    employee.setLoginEnabled(loginVal != null && (loginVal.equalsIgnoreCase("yes") || loginVal.equalsIgnoreCase("true")));
                    employee.setEmployeeRemarks(dto.getRemarks());
                    employee.setActive(true);

                    employeeRepository.save(employee);
                    return true;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("Database persistence failed for employee '{}': {}", dto.getFirstName(), e.getMessage());

                    // FIX: Pass database constraint violation to Error Excel
                    processingErrors.add(ErpImportError.builder()
                            .jobId(context.getJobId())
                            .rowNumber(-1) // DB Level rejection
                            .columnName("DATABASE_SAVE")
                            .cellValue("N/A")
                            .errorCode("CONSTRAINT_VIOLATION")
                            .severity("ERROR")
                            .message("Database rejected record: " + e.getMessage())
                            .build());

                    return false;
                }
            });

            if (Boolean.TRUE.equals(result)) succeeded++;
            else failed++;
        }

        return ChunkProcessingResult.builder()
                .processed(validDtos.size())
                .succeeded(succeeded)
                .validationFailed(0)
                .processingFailed(failed)
                .processingTimeMs(System.currentTimeMillis() - start)
                .processingErrors(processingErrors) // Sent to coordinator
                .build();
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        String cleanStr = dateStr.trim();
        if (cleanStr.matches("\\d+(\\.\\d+)?")) {
            try {
                double serial = Double.parseDouble(cleanStr);
                return LocalDate.of(1899, 12, 30).plusDays((long) serial);
            } catch (NumberFormatException ignored) {}
        }
        String[] formats = { "yyyy-MM-dd", "MM/dd/yyyy", "dd/MM/yyyy", "dd-MMM-yyyy", "dd-MM-yyyy" };
        for (String format : formats) {
            try { return LocalDate.parse(cleanStr, DateTimeFormatter.ofPattern(format)); }
            catch (DateTimeParseException ignored) {}
        }
        return null;
    }
}
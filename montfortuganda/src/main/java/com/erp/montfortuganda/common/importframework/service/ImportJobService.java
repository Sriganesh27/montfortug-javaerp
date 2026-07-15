package com.erp.montfortuganda.common.importframework.service;

import com.erp.montfortuganda.common.importframework.model.ErpImportJob;
import com.erp.montfortuganda.common.importframework.report.ErrorExcelReportGenerator;
import com.erp.montfortuganda.common.importframework.model.ErpImportJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportJobService {

    private final ErpImportJobRepository jobRepository;
    private final ErrorExcelReportGenerator errorReportGenerator;

    public Optional<ErpImportJob> getJobStatus(String jobId) {
        return jobRepository.findById(jobId);
    }

    public List<ErpImportJob> getRecentJobs(String moduleName) {
        return jobRepository.findAll();
    }

    public byte[] generateErrorReport(String jobId) {
        // FIX: Ensure the method name matches ErrorExcelReportGenerator exactly
        return errorReportGenerator.generateErrorReport(jobId);
    }
}
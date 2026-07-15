package com.erp.montfortuganda.common.importframework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ImportFrameworkAsyncConfig {

    @Bean(name = "importVirtualThreadExecutor", destroyMethod = "close")
    public ExecutorService importVirtualThreadExecutor() {
        // Managed Java 21 Virtual Thread Executor for safe shutdowns
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
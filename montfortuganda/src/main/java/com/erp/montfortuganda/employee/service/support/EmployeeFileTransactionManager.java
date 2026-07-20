package com.erp.montfortuganda.employee.service.support;

import com.erp.montfortuganda.infrastructure.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmployeeFileTransactionManager {

    private final StorageService storageService;

    /**
     * Must be created inside an active employee database transaction.
     */
    public FileChangeSet begin() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException(
                    "Employee file handling requires an active transaction."
            );
        }

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException(
                    "Transaction synchronization is not active."
            );
        }

        FileChangeSet changeSet = new FileChangeSet();

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCompletion(int status) {
                        if (status == TransactionSynchronization.STATUS_COMMITTED) {
                            deleteFiles(
                                    changeSet.filesToDeleteAfterCommit,
                                    "old employee file"
                            );
                        } else {
                            deleteFiles(
                                    changeSet.newlyCreatedFiles,
                                    "rolled-back employee file"
                            );
                        }
                    }
                }
        );

        return changeSet;
    }

    private void deleteFiles(
            List<StoredFile> files,
            String description
    ) {
        for (int index = files.size() - 1; index >= 0; index--) {
            StoredFile file = files.get(index);

            try {
                storageService.deleteStoredFile(
                        file.relativePath(),
                        file.privateStorage()
                );

                log.info(
                        "Deleted {}: {}",
                        description,
                        file.relativePath()
                );

            } catch (Exception exception) {
                log.error(
                        "Failed to delete {}: {}",
                        description,
                        file.relativePath(),
                        exception
                );
            }
        }
    }

    public static final class FileChangeSet {

        private final List<StoredFile> newlyCreatedFiles =
                new ArrayList<>();

        private final List<StoredFile> filesToDeleteAfterCommit =
                new ArrayList<>();

        private FileChangeSet() {
        }

        /**
         * Register a file immediately after it has been uploaded.
         * It will be deleted automatically when the transaction rolls back.
         */
        public void registerNewFile(
                String relativePath,
                boolean privateStorage
        ) {
            if (relativePath == null || relativePath.isBlank()) {
                return;
            }

            newlyCreatedFiles.add(
                    new StoredFile(
                            relativePath,
                            privateStorage
                    )
            );
        }

        /**
         * Register an old/replaced file.
         * It will be deleted only after the database transaction commits.
         */
        public void deleteAfterCommit(
                String relativePath,
                boolean privateStorage
        ) {
            if (relativePath == null || relativePath.isBlank()) {
                return;
            }

            filesToDeleteAfterCommit.add(
                    new StoredFile(
                            relativePath,
                            privateStorage
                    )
            );
        }
    }

    private record StoredFile(
            String relativePath,
            boolean privateStorage
    ) {
    }
}
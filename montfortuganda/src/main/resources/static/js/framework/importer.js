/* global showErrorMessage, showSuccessMessage, getAuthHeaders */

const AppImporter = (() => {
    'use strict';

    const MAX_FILE_SIZE = 10 * 1024 * 1024;
    const ALLOWED_EXTENSIONS = ['xlsx', 'xls', 'csv'];
    const TERMINAL_STATUSES = new Set([
        'COMPLETED',
        'COMPLETED_WITH_ERRORS',
        'FAILED',
        'CANCELLED'
    ]);

    let initialized = false;
    let modal;
    let dialog;
    let title;
    let description;
    let closeButton;
    let cancelButton;
    let uploadButton;
    let fileInput;
    let dropZone;
    let selectedFileCard;
    let selectedFileName;
    let selectedFileSize;
    let removeFileButton;
    let createCredentials;
    let sendEmail;
    let validationMessage;
    let fileStep;
    let progressStep;
    let resultStep;
    let stageLabel;
    let statusText;
    let progressPercent;
    let progressTrack;
    let progressBar;
    let downloadErrorsButton;
    let resultIcon;
    let resultTitle;
    let resultMessage;

    let selectedFile = null;
    let running = false;
    let currentJobId = null;
    let pollTimer = null;
    let currentModule = null;
    let currentSuccessCallback = null;
    let activeRequest = null;

    function getRequiredBranchId() {
        const possibleValues = [
            localStorage.getItem('user_branch'),
            localStorage.getItem('branch_id'),
            localStorage.getItem('school_id')
        ];

        for (const rawValue of possibleValues) {
            const parsed = Number.parseInt(rawValue, 10);
            if (Number.isInteger(parsed) && parsed > 0) {
                return parsed;
            }
        }

        throw new Error(
            'No valid branch is assigned to the current user. Please sign in again.'
        );
    }

    function init() {
        if (initialized) return;

        modal = document.getElementById('erp-import-modal');
        if (!modal) {
            console.error('Bulk import modal was not found.');
            return;
        }

        dialog = document.getElementById('erp-import-modal-box');
        title = document.getElementById('import-title');
        description = document.getElementById('import-desc');
        closeButton = document.getElementById('import-btn-close');
        cancelButton = document.getElementById('import-btn-cancel');
        uploadButton = document.getElementById('import-btn-upload');
        fileInput = document.getElementById('import-file-input');
        dropZone = document.getElementById('import-drop-zone');
        selectedFileCard = document.getElementById('import-selected-file');
        selectedFileName = document.getElementById('import-file-name');
        selectedFileSize = document.getElementById('import-file-size');
        removeFileButton = document.getElementById('import-remove-file');
        createCredentials = document.getElementById('import-create-credentials');
        sendEmail = document.getElementById('import-send-email');
        validationMessage = document.getElementById('import-validation-message');
        fileStep = document.getElementById('import-step-file');
        progressStep = document.getElementById('import-progress-container');
        resultStep = document.getElementById('import-result-container');
        stageLabel = document.getElementById('import-stage-label');
        statusText = document.getElementById('import-status-text');
        progressPercent = document.getElementById('import-progress-percent');
        progressTrack = document.getElementById('import-progress-track');
        progressBar = document.getElementById('import-progress-bar');
        downloadErrorsButton = document.getElementById('import-download-errors');
        resultIcon = document.getElementById('import-result-icon');
        resultTitle = document.getElementById('import-result-title');
        resultMessage = document.getElementById('import-result-msg');

        dropZone.addEventListener('click', () => {
            if (!running) fileInput.click();
        });

        dropZone.addEventListener('keydown', event => {
            if (
                !running
                && (event.key === 'Enter' || event.key === ' ')
            ) {
                event.preventDefault();
                fileInput.click();
            }
        });

        ['dragenter', 'dragover'].forEach(eventName => {
            dropZone.addEventListener(eventName, event => {
                event.preventDefault();
                if (!running) dropZone.classList.add('dragging');
            });
        });

        ['dragleave', 'drop'].forEach(eventName => {
            dropZone.addEventListener(eventName, event => {
                event.preventDefault();
                dropZone.classList.remove('dragging');
            });
        });

        dropZone.addEventListener('drop', event => {
            if (running) return;
            const file = event.dataTransfer?.files?.[0];
            if (file) selectFile(file);
        });

        fileInput.addEventListener('change', () => {
            const file = fileInput.files?.[0];
            if (file) selectFile(file);
        });

        removeFileButton.addEventListener('click', clearSelectedFile);

        createCredentials.addEventListener('change', () => {
            sendEmail.disabled = !createCredentials.checked;
            if (!createCredentials.checked) {
                sendEmail.checked = false;
            }
        });

        uploadButton.addEventListener('click', startImport);
        cancelButton.addEventListener('click', requestClose);
        closeButton.addEventListener('click', requestClose);

        modal.addEventListener('mousedown', event => {
            if (event.target === modal) requestClose();
        });

        document.addEventListener('keydown', event => {
            if (
                event.key === 'Escape'
                && !modal.classList.contains('hidden')
            ) {
                requestClose();
            }
        });

        initialized = true;
    }

    function selectFile(file) {
        hideValidation();

        const error = validateFile(file);
        if (error) {
            clearSelectedFile();
            showValidation(error);
            return;
        }

        selectedFile = file;
        selectedFileName.textContent = file.name;
        selectedFileSize.textContent = formatBytes(file.size);
        dropZone.classList.add('hidden');
        selectedFileCard.classList.remove('hidden');
        uploadButton.disabled = false;
    }

    function validateFile(file) {
        const extension = String(file.name || '')
            .split('.')
            .pop()
            .toLowerCase();

        if (!ALLOWED_EXTENSIONS.includes(extension)) {
            return 'Choose an XLSX, XLS or CSV file.';
        }

        if (file.size <= 0) {
            return 'The selected file is empty.';
        }

        if (file.size > MAX_FILE_SIZE) {
            return 'The selected file is larger than 10 MB.';
        }

        return null;
    }

    function clearSelectedFile() {
        selectedFile = null;
        fileInput.value = '';
        selectedFileName.textContent = 'No file selected';
        selectedFileSize.textContent = '';
        selectedFileCard.classList.add('hidden');
        dropZone.classList.remove('hidden');
        uploadButton.disabled = true;
    }

    function showValidation(message) {
        validationMessage.textContent = message;
        validationMessage.classList.remove('hidden');
    }

    function hideValidation() {
        validationMessage.textContent = '';
        validationMessage.classList.add('hidden');
    }

    function reset() {
        stopPolling();
        activeRequest?.abort?.();
        activeRequest = null;
        running = false;
        currentJobId = null;
        currentModule = null;
        currentSuccessCallback = null;

        clearSelectedFile();
        hideValidation();

        createCredentials.checked = false;
        sendEmail.checked = false;
        sendEmail.disabled = true;

        fileStep.classList.remove('hidden');
        progressStep.classList.add('hidden');
        resultStep.classList.add('hidden');

        uploadButton.classList.remove('hidden');
        cancelButton.textContent = 'Cancel';
        closeButton.disabled = false;

        setProgress(0);
        setStage('UPLOAD');
        setCounters({});
        setResultCounters({});
        downloadErrorsButton.classList.add('hidden');
        downloadErrorsButton.onclick = null;
    }

    function open(
        moduleName,
        modalTitle = 'Import Data',
        modalDescription = 'Upload an Excel file.',
        onSuccess = null
    ) {
        init();
        if (!initialized) {
            showErrorMessage?.('Bulk importer could not be initialized.');
            return;
        }

        reset();

        currentModule = String(moduleName || '').trim().toLowerCase();
        currentSuccessCallback =
            typeof onSuccess === 'function' ? onSuccess : null;

        title.textContent = modalTitle;
        description.textContent = modalDescription;
        modal.classList.remove('hidden');

        window.setTimeout(() => dropZone.focus(), 50);
    }

    function requestClose() {
        if (running) {
            showValidation(
                'The import is still running. Keep this window open until processing finishes.'
            );
            return;
        }

        modal.classList.add('hidden');
        reset();
    }

    async function startImport() {
        hideValidation();

        if (running) return;

        if (!selectedFile) {
            showValidation('Select an import file before continuing.');
            return;
        }

        let branchId;
        try {
            branchId = getRequiredBranchId();
        } catch (error) {
            showValidation(error.message);
            return;
        }

        running = true;
        uploadButton.disabled = true;
        closeButton.disabled = true;
        cancelButton.textContent = 'Import running…';

        fileStep.classList.add('hidden');
        resultStep.classList.add('hidden');
        progressStep.classList.remove('hidden');

        setStage('UPLOAD');
        setProgress(2);
        statusText.textContent = 'Uploading the selected file securely…';

        const formData = new FormData();
        formData.append('file', selectedFile);

        const query = new URLSearchParams({
            branchId: String(branchId),
            createCredentials: String(createCredentials.checked),
            sendEmail: String(sendEmail.checked)
        });

        try {
            const response = await uploadMultipartWithProgress(
                `/api/import/${encodeURIComponent(currentModule)}?${query}`,
                formData,
                uploadPercent => {
                    const mapped = Math.min(
                        35,
                        Math.max(2, Math.round(uploadPercent * 0.35))
                    );
                    setProgress(mapped);
                }
            );

            currentJobId = extractJobId(response);

            if (!currentJobId) {
                throw new Error(
                    'The server started the import but did not return a job ID.'
                );
            }

            setStage('VALIDATE');
            setProgress(40);
            statusText.textContent =
                'Upload complete. Validating headers and rows…';

            startPolling(currentJobId);
        } catch (error) {
            showFailedStart(error);
        }
    }

    function uploadMultipartWithProgress(url, formData, onProgress) {
        return new Promise((resolve, reject) => {
            const xhr = new XMLHttpRequest();
            activeRequest = xhr;

            xhr.open('POST', url, true);
            xhr.withCredentials = true;

            const headers =
                typeof getAuthHeaders === 'function'
                    ? getAuthHeaders(true)
                    : {};

            Object.entries(headers || {}).forEach(([name, value]) => {
                if (value != null) xhr.setRequestHeader(name, value);
            });

            xhr.upload.addEventListener('progress', event => {
                if (event.lengthComputable && onProgress) {
                    onProgress((event.loaded / event.total) * 100);
                }
            });

            xhr.addEventListener('load', () => {
                activeRequest = null;

                let payload = null;
                const raw = xhr.responseText || '';

                try {
                    payload = raw ? JSON.parse(raw) : null;
                } catch {
                    payload = raw;
                }

                if (xhr.status >= 200 && xhr.status < 300) {
                    resolve(payload);
                    return;
                }

                const message =
                    payload?.message
                    || payload?.error
                    || `Import request failed with status ${xhr.status}.`;

                const error = new Error(message);
                error.status = xhr.status;
                error.data = payload;
                reject(error);
            });

            xhr.addEventListener('error', () => {
                activeRequest = null;
                reject(new Error(
                    'The import upload could not reach the server. Check the connection and try again.'
                ));
            });

            xhr.addEventListener('timeout', () => {
                activeRequest = null;
                reject(new Error('The import upload timed out.'));
            });

            xhr.timeout = 120000;
            xhr.send(formData);
        });
    }

    function extractJobId(response) {
        const candidates = [
            response?.jobId,
            response?.data?.jobId,
            response?.data,
            response?.id,
            response
        ];

        for (const value of candidates) {
            if (
                typeof value === 'string'
                || typeof value === 'number'
            ) {
                const normalized = String(value).trim();
                if (normalized && normalized !== '[object Object]') {
                    return normalized;
                }
            }
        }

        return null;
    }

    function startPolling(jobId) {
        stopPolling();

        const poll = async () => {
            try {
                const response = await fetch(
                    `/api/import/progress/${encodeURIComponent(jobId)}`,
                    {
                        method: 'GET',
                        headers:
                            typeof getAuthHeaders === 'function'
                                ? getAuthHeaders()
                                : {},
                        credentials: 'include',
                        cache: 'no-store'
                    }
                );

                const payload = await parseResponse(response);
                const data = payload?.data || payload || {};

                updateFromProgress(data);

                const status = normalizeStatus(data.status);
                if (TERMINAL_STATUSES.has(status)) {
                    stopPolling();
                    finishImport(status, data);
                }
            } catch (error) {
                console.error('Bulk import polling failed:', error);
                statusText.textContent =
                    'Waiting for the server to report progress…';
            }
        };

        void poll();
        pollTimer = window.setInterval(poll, 1800);
    }

    async function parseResponse(response) {
        const raw = await response.text();
        let payload = null;

        try {
            payload = raw ? JSON.parse(raw) : null;
        } catch {
            payload = raw;
        }

        if (!response.ok) {
            throw new Error(
                payload?.message
                || `Request failed with status ${response.status}.`
            );
        }

        return payload;
    }

    function updateFromProgress(data) {
        const status = normalizeStatus(data.status);
        const total = firstNumber(
            data.totalRows,
            data.total,
            data.totalCount,
            data.recordsTotal
        );
        const processed = firstNumber(
            data.processedRows,
            data.processed,
            data.processedCount,
            data.recordsProcessed
        );
        const successful = firstNumber(
            data.successRows,
            data.successfulRows,
            data.successCount,
            data.importedRows
        );
        const failed = firstNumber(
            data.failedRows,
            data.errorRows,
            data.failureCount,
            data.failedCount
        );

        setCounters({
            total,
            processed,
            successful,
            failed
        });

        let percent = firstNumber(
            data.progressPercentage,
            data.progress,
            data.percentage
        );

        if (percent == null && total > 0 && processed != null) {
            percent = Math.round((processed / total) * 100);
        }

        if (status.includes('VALIDAT')) {
            setStage('VALIDATE');
            percent = Math.max(40, percent ?? 45);
        } else if (
            status.includes('PROCESS')
            || status.includes('RUNNING')
            || status.includes('IMPORT')
        ) {
            setStage('PROCESS');
            percent = Math.max(50, percent ?? 55);
        }

        if (!TERMINAL_STATUSES.has(status)) {
            setProgress(Math.min(96, Math.max(40, percent ?? 45)));
        }

        stageLabel.textContent =
            humanizeStatus(status) || 'Processing import';

        statusText.textContent =
            data.message
            || data.currentStage
            || buildProgressMessage(processed, total);
    }

    function finishImport(status, data) {
        running = false;
        activeRequest = null;
        closeButton.disabled = false;
        cancelButton.textContent = 'Close';

        setStage('COMPLETE');
        setProgress(100);

        const total = firstNumber(
            data.totalRows,
            data.total,
            data.totalCount,
            0
        ) ?? 0;
        const successful = firstNumber(
            data.successRows,
            data.successfulRows,
            data.successCount,
            data.importedRows,
            0
        ) ?? 0;
        const failed = firstNumber(
            data.failedRows,
            data.errorRows,
            data.failureCount,
            data.failedCount,
            Math.max(0, total - successful)
        ) ?? 0;

        setResultCounters({ total, successful, failed });

        progressStep.classList.add('hidden');
        resultStep.classList.remove('hidden');
        uploadButton.classList.add('hidden');

        resultIcon.className = 'erp-import-result-icon';
        downloadErrorsButton.classList.add('hidden');

        if (status === 'COMPLETED') {
            resultIcon.classList.add('success');
            resultIcon.innerHTML = '<i class="bi bi-check-lg"></i>';
            resultTitle.textContent = 'Import completed';
            resultMessage.textContent =
                data.message
                || `${successful || total} employee record(s) were imported successfully.`;

            showSuccessMessage?.('Employee import completed successfully.');
            currentSuccessCallback?.();
            return;
        }

        if (status === 'COMPLETED_WITH_ERRORS') {
            resultIcon.classList.add('warning');
            resultIcon.innerHTML = '<i class="bi bi-exclamation-lg"></i>';
            resultTitle.textContent = 'Import completed with errors';
            resultMessage.textContent =
                data.message
                || 'Valid rows were imported. Download the error report, correct the failed rows and import them again.';

            configureErrorDownload(currentJobId);
            currentSuccessCallback?.();
            return;
        }

        resultIcon.classList.add('danger');
        resultIcon.innerHTML = '<i class="bi bi-x-lg"></i>';
        resultTitle.textContent =
            status === 'CANCELLED'
                ? 'Import cancelled'
                : 'Import failed';
        resultMessage.textContent =
            data.message
            || data.errorMessage
            || 'The employee file could not be imported. Download the error report for details.';

        if (currentJobId) configureErrorDownload(currentJobId);
    }

    function showFailedStart(error) {
        running = false;
        closeButton.disabled = false;
        cancelButton.textContent = 'Cancel';
        progressStep.classList.add('hidden');
        fileStep.classList.remove('hidden');
        uploadButton.disabled = false;

        showValidation(
            error?.message || 'The import could not be started.'
        );
        showErrorMessage?.(
            error?.message || 'The import could not be started.'
        );
    }

    function configureErrorDownload(jobId) {
        if (!jobId) return;

        downloadErrorsButton.classList.remove('hidden');
        downloadErrorsButton.onclick = async () => {
            downloadErrorsButton.disabled = true;

            try {
                const response = await fetch(
                    `/api/import/errors/${encodeURIComponent(jobId)}`,
                    {
                        method: 'GET',
                        headers:
                            typeof getAuthHeaders === 'function'
                                ? getAuthHeaders()
                                : {},
                        credentials: 'include',
                        cache: 'no-store'
                    }
                );

                if (!response.ok) {
                    throw new Error(
                        `Error report download failed with status ${response.status}.`
                    );
                }

                const blob = await response.blob();
                const disposition =
                    response.headers.get('Content-Disposition') || '';
                const match =
                    disposition.match(/filename="?([^"]+)"?/i);
                const filename =
                    match?.[1]
                    || `Employee_Import_Errors_${jobId}.xlsx`;

                const url = URL.createObjectURL(blob);
                const anchor = document.createElement('a');
                anchor.href = url;
                anchor.download = filename;
                document.body.appendChild(anchor);
                anchor.click();
                anchor.remove();
                URL.revokeObjectURL(url);
            } catch (error) {
                showErrorMessage?.(
                    error.message || 'Could not download the error report.'
                );
            } finally {
                downloadErrorsButton.disabled = false;
            }
        };
    }

    function setProgress(value) {
        const safeValue = Math.max(
            0,
            Math.min(100, Number(value) || 0)
        );

        progressBar.style.width = `${safeValue}%`;
        progressPercent.textContent = `${Math.round(safeValue)}%`;
        progressTrack.setAttribute(
            'aria-valuenow',
            String(Math.round(safeValue))
        );
    }

    function setStage(stageName) {
        const order = ['UPLOAD', 'VALIDATE', 'PROCESS', 'COMPLETE'];
        const activeIndex = order.indexOf(stageName);

        document
            .querySelectorAll('#erp-import-modal .erp-import-stage')
            .forEach(element => {
                const index = order.indexOf(element.dataset.stage);
                element.classList.toggle('active', index === activeIndex);
                element.classList.toggle(
                    'complete',
                    index >= 0 && index < activeIndex
                );
            });
    }

    function setCounters({
        total = null,
        successful = null,
        failed = null,
        processed = null
    }) {
        setText('import-total-count', formatCount(total));
        setText('import-success-count', formatCount(successful));
        setText('import-failed-count', formatCount(failed));
        setText('import-processed-count', formatCount(processed));
    }

    function setResultCounters({
        total = 0,
        successful = 0,
        failed = 0
    }) {
        setText('import-result-total', formatCount(total, '0'));
        setText('import-result-success', formatCount(successful, '0'));
        setText('import-result-failed', formatCount(failed, '0'));
    }

    function setText(id, value) {
        const element = document.getElementById(id);
        if (element) element.textContent = value;
    }

    function firstNumber(...values) {
        for (const value of values) {
            if (
                value !== null
                && value !== undefined
                && value !== ''
                && Number.isFinite(Number(value))
            ) {
                return Number(value);
            }
        }
        return null;
    }

    function formatCount(value, fallback = '—') {
        return Number.isFinite(Number(value))
            ? Number(value).toLocaleString()
            : fallback;
    }

    function formatBytes(bytes) {
        if (!Number.isFinite(bytes) || bytes <= 0) return '0 bytes';
        const units = ['bytes', 'KB', 'MB', 'GB'];
        const index = Math.min(
            Math.floor(Math.log(bytes) / Math.log(1024)),
            units.length - 1
        );
        const value = bytes / (1024 ** index);
        return `${value.toFixed(index === 0 ? 0 : 1)} ${units[index]}`;
    }

    function normalizeStatus(status) {
        return String(status || '')
            .trim()
            .toUpperCase()
            .replaceAll(' ', '_');
    }

    function humanizeStatus(status) {
        return String(status || '')
            .toLowerCase()
            .replaceAll('_', ' ')
            .replace(/\b\w/g, char => char.toUpperCase());
    }

    function buildProgressMessage(processed, total) {
        if (
            Number.isFinite(processed)
            && Number.isFinite(total)
            && total > 0
        ) {
            return `Processed ${processed.toLocaleString()} of ${total.toLocaleString()} rows.`;
        }

        return 'The server is validating and importing employee rows…';
    }

    function stopPolling() {
        if (pollTimer) {
            window.clearInterval(pollTimer);
            pollTimer = null;
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init, {
            once: true
        });
    } else {
        init();
    }

    return Object.freeze({
        open
    });
})();

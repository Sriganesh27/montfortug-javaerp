/* global showErrorMessage, getAuthHeaders */

const AppImporter = (() => {
    'use strict';

    const MAX_FILE_SIZE = 10 * 1024 * 1024;
    const ALLOWED_EXTENSIONS = new Set(['xlsx']);
    const TERMINAL_STATUSES = new Set([
        'COMPLETED',
        'COMPLETED_WITH_ERRORS',
        'FAILED',
        'CANCELLED'
    ]);

    let initialized = false;

    let modal;
    let title;
    let description;
    let closeButton;
    let cancelButton;
    let uploadButton;
    let fileInput;
    let dropZone;
    let dropTitle;
    let dropHelp;
    let dropFormats;
    let selectedFileCard;
    let selectedFileName;
    let selectedFileSize;
    let removeFileButton;
    let createCredentials;
    let sendEmail;
    let employeeOptions;
    let informationText;
    let validationMessage;
    let fileStep;
    let progressStep;
    let resultStep;
    let stageLabel;
    let statusText;
    let progressPercent;
    let progressTrack;
    let progressBar;
    let resultActionButton;
    let resultIcon;
    let resultTitle;
    let resultMessage;

    let selectedFile = null;
    let running = false;
    let currentJobId = null;
    let currentModule = null;
    let currentSuccessCallback = null;
    let retryOriginalJobId = null;
    let resultActionPhase = null;
    let pollTimer = null;
    let pollRequestInFlight = false;
    let pollGeneration = 0;
    let terminalStatusHandled = false;
    let successCallbackInvoked = false;
    let currentProgressValue = 0;
    let currentStageIndex = 0;
    let activeRequest = null;

    function init() {
        if (initialized) {
            return;
        }

        modal = document.getElementById('erp-import-modal');

        if (!(modal instanceof HTMLElement)) {
            console.error('Bulk import modal was not found.');
            return;
        }

        title = document.getElementById('import-title');
        description = document.getElementById('import-desc');
        closeButton = document.getElementById('import-btn-close');
        cancelButton = document.getElementById('import-btn-cancel');
        uploadButton = document.getElementById('import-btn-upload');
        fileInput = document.getElementById('import-file-input');
        dropZone = document.getElementById('import-drop-zone');
        selectedFileCard = document.getElementById(
            'import-selected-file'
        );
        selectedFileName = document.getElementById(
            'import-file-name'
        );
        selectedFileSize = document.getElementById(
            'import-file-size'
        );
        removeFileButton = document.getElementById(
            'import-remove-file'
        );
        createCredentials = document.getElementById(
            'import-create-credentials'
        );
        sendEmail = document.getElementById(
            'import-send-email'
        );
        validationMessage = document.getElementById(
            'import-validation-message'
        );
        fileStep = document.getElementById('import-step-file');
        progressStep = document.getElementById(
            'import-progress-container'
        );
        resultStep = document.getElementById(
            'import-result-container'
        );
        stageLabel = document.getElementById(
            'import-stage-label'
        );
        statusText = document.getElementById(
            'import-status-text'
        );
        progressPercent = document.getElementById(
            'import-progress-percent'
        );
        progressTrack = document.getElementById(
            'import-progress-track'
        );
        progressBar = document.getElementById(
            'import-progress-bar'
        );
        resultActionButton = document.getElementById(
            'import-download-errors'
        );
        resultIcon = document.getElementById(
            'import-result-icon'
        );
        resultTitle = document.getElementById(
            'import-result-title'
        );
        resultMessage = document.getElementById(
            'import-result-msg'
        );

        if (
            !(title instanceof HTMLElement)
            || !(description instanceof HTMLElement)
            || !(closeButton instanceof HTMLButtonElement)
            || !(cancelButton instanceof HTMLButtonElement)
            || !(uploadButton instanceof HTMLButtonElement)
            || !(fileInput instanceof HTMLInputElement)
            || !(dropZone instanceof HTMLElement)
            || !(selectedFileCard instanceof HTMLElement)
            || !(selectedFileName instanceof HTMLElement)
            || !(selectedFileSize instanceof HTMLElement)
            || !(removeFileButton instanceof HTMLButtonElement)
            || !(createCredentials instanceof HTMLInputElement)
            || !(sendEmail instanceof HTMLInputElement)
            || !(validationMessage instanceof HTMLElement)
            || !(fileStep instanceof HTMLElement)
            || !(progressStep instanceof HTMLElement)
            || !(resultStep instanceof HTMLElement)
            || !(stageLabel instanceof HTMLElement)
            || !(statusText instanceof HTMLElement)
            || !(progressPercent instanceof HTMLElement)
            || !(progressTrack instanceof HTMLElement)
            || !(progressBar instanceof HTMLElement)
            || !(resultActionButton instanceof HTMLButtonElement)
            || !(resultIcon instanceof HTMLElement)
            || !(resultTitle instanceof HTMLElement)
            || !(resultMessage instanceof HTMLElement)
        ) {
            console.error(
                'Bulk import modal does not match importer.js.'
            );
            return;
        }

        dropTitle = dropZone.querySelector('strong');
        dropHelp = dropZone.querySelector('span');
        dropFormats = dropZone.querySelector('small');
        employeeOptions = createCredentials.closest(
            '.erp-import-options'
        );
        informationText = modal.querySelector(
            '.erp-import-information p'
        );

        fileInput.accept = '.xlsx';

        bindEvents();
        initialized = true;
    }

    function bindEvents() {
        dropZone.addEventListener('click', () => {
            if (!running) {
                fileInput.click();
            }
        });

        dropZone.addEventListener('keydown', event => {
            if (
                !running
                && (
                    event.key === 'Enter'
                    || event.key === ' '
                )
            ) {
                event.preventDefault();
                fileInput.click();
            }
        });

        ['dragenter', 'dragover'].forEach(eventName => {
            dropZone.addEventListener(eventName, event => {
                event.preventDefault();

                if (!running) {
                    dropZone.classList.add('dragging');
                }
            });
        });

        ['dragleave', 'drop'].forEach(eventName => {
            dropZone.addEventListener(eventName, event => {
                event.preventDefault();
                dropZone.classList.remove('dragging');
            });
        });

        dropZone.addEventListener('drop', event => {
            if (running) {
                return;
            }

            const file = event.dataTransfer?.files?.[0];

            if (file) {
                selectFile(file);
            }
        });

        fileInput.addEventListener('change', () => {
            const file = fileInput.files?.[0];

            if (file) {
                selectFile(file);
            }
        });

        removeFileButton.addEventListener(
            'click',
            clearSelectedFile
        );

        createCredentials.addEventListener('change', () => {
            sendEmail.disabled =
                !createCredentials.checked;

            if (!createCredentials.checked) {
                sendEmail.checked = false;
            }
        });

        uploadButton.addEventListener(
            'click',
            startImport
        );

        cancelButton.addEventListener(
            'click',
            requestClose
        );

        closeButton.addEventListener(
            'click',
            requestClose
        );

        modal.addEventListener('mousedown', event => {
            if (event.target === modal) {
                requestClose();
            }
        });

        document.addEventListener('keydown', event => {
            if (
                event.key === 'Escape'
                && !modal.classList.contains('hidden')
            ) {
                requestClose();
            }
        });
    }

    function open(
        moduleName,
        modalTitle = 'Import Data',
        modalDescription = 'Upload an Excel file.',
        onSuccess = null
    ) {
        init();

        if (!initialized) {
            showErrorMessage?.(
                'Bulk importer could not be initialized.'
            );
            return;
        }

        const normalizedModule = normalizeModule(moduleName);

        if (!normalizedModule) {
            showErrorMessage?.(
                'The import module is invalid.'
            );
            return;
        }

        reset();

        currentModule = normalizedModule;
        currentSuccessCallback =
            typeof onSuccess === 'function'
                ? onSuccess
                : null;

        title.textContent = modalTitle;
        description.textContent = modalDescription;

        configureModuleView();
        modal.classList.remove('hidden');

        window.setTimeout(
            () => dropZone.focus(),
            50
        );
    }

    function configureModuleView() {
        const label = moduleLabel();
        const employeeModule =
            currentModule === 'employee';

        employeeOptions?.classList.toggle(
            'hidden',
            !employeeModule
        );

        if (!employeeModule) {
            createCredentials.checked = false;
            sendEmail.checked = false;
            sendEmail.disabled = true;
        }

        if (dropTitle) {
            dropTitle.textContent =
                `Drop the ${label.toLowerCase()} XLSX file here`;
        }

        if (dropHelp) {
            dropHelp.textContent =
                'or click to browse from your computer';
        }

        if (dropFormats) {
            dropFormats.textContent =
                'Accepted: XLSX • Maximum 10 MB';
        }

        if (informationText) {
            informationText.textContent = employeeModule
                ? 'Rows missing required backend fields will be rejected and included in the downloadable error report. Correct those rows and import them again.'
                : `${label} rows with valid values will continue. `
                + 'Only failed rows will be returned in a correction workbook with exact errors.';
        }

        setButtonLabel(
            uploadButton,
            'Start Import',
            'bi-upload'
        );
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
        selectedFileSize.textContent =
            formatBytes(file.size);

        dropZone.classList.add('hidden');
        selectedFileCard.classList.remove('hidden');
        uploadButton.disabled = false;
    }

    function validateFile(file) {
        const extension = String(file?.name || '')
            .split('.')
            .pop()
            .toLowerCase();

        if (!ALLOWED_EXTENSIONS.has(extension)) {
            return 'Choose an XLSX file.';
        }

        if (!Number.isFinite(file?.size) || file.size <= 0) {
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
        selectedFileName.textContent =
            'No file selected';
        selectedFileSize.textContent = '';

        selectedFileCard.classList.add('hidden');
        dropZone.classList.remove('hidden');
        uploadButton.disabled = true;
    }

    function reset() {
        stopPolling();
        activeRequest?.abort?.();
        activeRequest = null;

        running = false;
        selectedFile = null;
        currentJobId = null;
        currentModule = null;
        currentSuccessCallback = null;
        retryOriginalJobId = null;
        resultActionPhase = null;
        terminalStatusHandled = false;
        successCallbackInvoked = false;
        currentProgressValue = 0;
        currentStageIndex = 0;

        clearSelectedFile();
        hideValidation();

        createCredentials.checked = false;
        sendEmail.checked = false;
        sendEmail.disabled = true;

        fileStep.classList.remove('hidden');
        progressStep.classList.add('hidden');
        resultStep.classList.add('hidden');

        uploadButton.classList.remove('hidden');
        uploadButton.disabled = true;

        cancelButton.textContent = 'Cancel';
        closeButton.disabled = false;

        setProgress(0, true);
        setStage('UPLOAD');
        setCounters({});
        setResultCounters({});
        setResultIcon('bi-check-lg');

        resultActionButton.classList.add('hidden');
        resultActionButton.disabled = false;
        resultActionButton.onclick = null;
        setButtonLabel(
            resultActionButton,
            'Download Error Report',
            'bi-download'
        );
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

        if (running) {
            return;
        }

        if (!selectedFile) {
            showValidation(
                'Select an import file before continuing.'
            );
            return;
        }

        running = true;
        terminalStatusHandled = false;
        successCallbackInvoked = false;
        currentProgressValue = 0;
        currentStageIndex = 0;
        uploadButton.disabled = true;
        closeButton.disabled = true;
        cancelButton.textContent =
            retryOriginalJobId
                ? 'Retry running…'
                : 'Import running…';

        fileStep.classList.add('hidden');
        resultStep.classList.add('hidden');
        progressStep.classList.remove('hidden');

        setStage('UPLOAD');
        setProgress(2, true);

        statusText.textContent =
            retryOriginalJobId
                ? 'Uploading the failed-rows workbook securely…'
                : 'Uploading the selected file securely…';

        const formData = new FormData();
        formData.append('file', selectedFile);

        const endpoint = buildSubmissionEndpoint();

        try {
            const response =
                await uploadMultipartWithProgress(
                    endpoint,
                    formData,
                    uploadPercent => {
                        const mapped = Math.min(
                            35,
                            Math.max(
                                2,
                                Math.round(
                                    uploadPercent * 0.35
                                )
                            )
                        );

                        setProgress(mapped);
                    }
                );

            currentJobId = extractJobId(response);

            if (!currentJobId) {
                showFailedStart(
                    'The server started the import but did not return a job ID.'
                );
                return;
            }

            retryOriginalJobId = null;

            setStage('VALIDATE');
            setProgress(40);

            statusText.textContent =
                'Upload complete. Validating headers and rows…';

            startPolling(currentJobId);
        } catch (error) {
            showFailedStart(error);
        }
    }

    function buildSubmissionEndpoint() {
        const employeeQuery =
            buildEmployeeOptionsQuery();
        const studentRetry =
            currentModule === 'student'
            && Boolean(retryOriginalJobId);

        const baseEndpoint = studentRetry
            ? `/api/import/retry/${
                encodeURIComponent(String(retryOriginalJobId))
            }`
            : `/api/import/${
                encodeURIComponent(String(currentModule))
            }`;

        return employeeQuery
            ? `${baseEndpoint}?${employeeQuery}`
            : baseEndpoint;
    }

    function buildEmployeeOptionsQuery() {
        if (currentModule !== 'employee') {
            return '';
        }

        return new URLSearchParams({
            createCredentials: String(
                createCredentials.checked
            ),
            sendEmail: String(
                sendEmail.checked
            )
        }).toString();
    }

    function uploadMultipartWithProgress(
        url,
        formData,
        onProgress
    ) {
        return new Promise((resolve, reject) => {
            const xhr = new XMLHttpRequest();
            activeRequest = xhr;

            xhr.open('POST', url, true);
            xhr.withCredentials = true;

            const headers =
                typeof getAuthHeaders === 'function'
                    ? getAuthHeaders(true)
                    : {};

            Object.entries(headers || {})
                .forEach(([name, value]) => {
                    if (value !== null && value !== undefined) {
                        xhr.setRequestHeader(
                            name,
                            String(value)
                        );
                    }
                });

            xhr.upload.addEventListener(
                'progress',
                event => {
                    if (
                        event.lengthComputable
                        && onProgress
                    ) {
                        onProgress(
                            (event.loaded / event.total) * 100
                        );
                    }
                }
            );

            xhr.addEventListener('load', () => {
                activeRequest = null;

                const payload =
                    parseRawPayload(xhr.responseText);

                if (
                    xhr.status >= 200
                    && xhr.status < 300
                ) {
                    resolve(payload);
                    return;
                }

                const message =
                    payload?.message
                    || payload?.error
                    || (
                        typeof payload === 'string'
                            ? payload
                            : null
                    )
                    || `Import request failed with status ${xhr.status}.`;

                const error = new Error(message);
                error.status = xhr.status;
                error.data = payload;
                reject(error);
            });

            xhr.addEventListener('error', () => {
                activeRequest = null;

                reject(
                    new Error(
                        'The import upload could not reach the server. Check the connection and try again.'
                    )
                );
            });

            xhr.addEventListener('timeout', () => {
                activeRequest = null;

                reject(
                    new Error(
                        'The import upload timed out.'
                    )
                );
            });

            xhr.timeout = 120000;
            xhr.send(formData);
        });
    }

    function parseRawPayload(rawValue) {
        const raw = String(rawValue || '');

        if (!raw) {
            return null;
        }

        try {
            return JSON.parse(raw);
        } catch {
            return raw;
        }
    }

    /**
     * @param {unknown} response
     * @returns {string|null}
     */
    function extractJobId(response) {
        if (isPrimitiveIdentifier(response)) {
            return normalizeIdentifier(response);
        }

        if (!isRecord(response)) {
            return null;
        }

        const nestedData = response.data;
        const nestedJobId = isRecord(nestedData)
            ? nestedData.jobId
            : null;

        const candidates = [
            response.jobId,
            nestedJobId,
            nestedData,
            response.id
        ];

        for (const value of candidates) {
            if (isPrimitiveIdentifier(value)) {
                const normalized = normalizeIdentifier(value);

                if (normalized) {
                    return normalized;
                }
            }
        }

        return null;
    }

    /**
     * @param {unknown} value
     * @returns {value is Record<string, unknown>}
     */
    function isRecord(value) {
        return typeof value === 'object'
            && value !== null
            && !Array.isArray(value);
    }

    /**
     * @param {unknown} value
     * @returns {value is string|number}
     */
    function isPrimitiveIdentifier(value) {
        return typeof value === 'string'
            || typeof value === 'number';
    }

    /**
     * @param {string|number} value
     * @returns {string|null}
     */
    function normalizeIdentifier(value) {
        const normalized = String(value).trim();

        return normalized
        && normalized !== '[object Object]'
            ? normalized
            : null;
    }

    /** @param {string} jobId */
    function startPolling(jobId) {
        stopPolling();

        terminalStatusHandled = false;
        pollRequestInFlight = false;

        const generation = ++pollGeneration;

        const scheduleNextPoll = () => {
            if (
                generation !== pollGeneration
                || terminalStatusHandled
                || !running
            ) {
                return;
            }

            pollTimer = window.setTimeout(
                poll,
                1800
            );
        };

        const poll = async () => {
            if (
                generation !== pollGeneration
                || terminalStatusHandled
                || pollRequestInFlight
                || !running
            ) {
                return;
            }

            pollRequestInFlight = true;

            try {
                const response = await fetch(
                    `/api/import/progress/${
                        encodeURIComponent(jobId)
                    }`,
                    {
                        method: 'GET',
                        headers: authHeaders(),
                        credentials: 'include',
                        cache: 'no-store'
                    }
                );

                const payload =
                    await parseResponse(response);

                if (
                    generation !== pollGeneration
                    || currentJobId !== jobId
                    || terminalStatusHandled
                ) {
                    return;
                }

                const data =
                    payload?.data || payload || {};

                updateFromProgress(data);

                const status =
                    normalizeStatus(data.status);

                if (TERMINAL_STATUSES.has(status)) {
                    if (terminalStatusHandled) {
                        return;
                    }

                    terminalStatusHandled = true;
                    stopPolling();
                    finishImport(status, data);
                }
            } catch (error) {
                if (
                    generation !== pollGeneration
                    || terminalStatusHandled
                ) {
                    return;
                }

                console.error(
                    'Bulk import polling failed:',
                    error
                );

                statusText.textContent =
                    'Waiting for the server to report progress…';
            } finally {
                pollRequestInFlight = false;
                scheduleNextPoll();
            }
        };

        pollTimer = window.setTimeout(
            poll,
            0
        );
    }

    async function parseResponse(response) {
        const raw = await response.text();
        const payload = parseRawPayload(raw);

        if (!response.ok) {
            throw new Error(
                payload?.message
                || payload?.error
                || (
                    typeof payload === 'string'
                        ? payload
                        : null
                )
                || `Request failed with status ${response.status}.`
            );
        }

        return payload;
    }

    function updateFromProgress(data) {
        const status =
            normalizeStatus(data.status);

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

        const total = firstNumber(
            data.totalRows,
            data.total,
            data.totalCount,
            data.recordsTotal,
            processed
        );

        setCounters({
            total,
            processed,
            successful,
            failed
        });

        const backendPercent = firstNumber(
            data.progressPercentage,
            data.progress,
            data.percentage
        );

        const validationPercent =
            total > 0
                ? Math.min(
                    100,
                    Math.max(
                        0,
                        (processed / total) * 100
                    )
                )
                : Math.min(
                    100,
                    Math.max(
                        0,
                        backendPercent ?? 0
                    )
                );

        /*
         * During SAVING_BATCH, processedRows can already equal totalRows
         * because row validation has completed. Successful + failed is the
         * accurate count of rows whose final outcome is known.
         */
        const completedOutcomeRows =
            Math.min(
                total,
                Math.max(
                    0,
                    (successful ?? 0)
                    + (failed ?? 0)
                )
            );

        const outcomePercent =
            total > 0
                ? Math.min(
                    100,
                    Math.max(
                        0,
                        (completedOutcomeRows / total) * 100
                    )
                )
                : 0;

        let displayPercent =
            currentProgressValue;

        switch (status) {
            case 'CREATED':
                setStage('VALIDATE');
                displayPercent = 38;
                break;

            case 'QUEUED':
                setStage('VALIDATE');
                displayPercent = 40;
                break;

            case 'INITIALIZING':
                setStage('VALIDATE');
                displayPercent = 43;
                break;

            case 'VALIDATING_FILE':
                setStage('VALIDATE');
                displayPercent = 46;
                break;

            case 'READING_ROWS':
                setStage('VALIDATE');
                displayPercent = 50;
                break;

            case 'VALIDATING_ROWS':
                setStage('VALIDATE');
                displayPercent =
                    52 + validationPercent * 0.23;
                break;

            case 'MAPPING':
                setStage('PROCESS');
                displayPercent = 76;
                break;

            case 'SAVING_BATCH':
                setStage('PROCESS');
                displayPercent =
                    77 + outcomePercent * 0.18;
                break;

            case 'GENERATING_REPORT':
                setStage('PROCESS');
                displayPercent = 96;
                break;

            case 'NOTIFYING':
                setStage('PROCESS');
                displayPercent = 98;
                break;

            case 'PAUSED':
            case 'RESUMED':
                setStage('PROCESS');
                displayPercent = Math.max(
                    currentProgressValue,
                    77
                );
                break;

            default:
                if (!TERMINAL_STATUSES.has(status)) {
                    setStage('PROCESS');
                    displayPercent = Math.max(
                        currentProgressValue,
                        76
                    );
                }
                break;
        }

        if (!TERMINAL_STATUSES.has(status)) {
            setProgress(
                Math.min(
                    97,
                    displayPercent
                )
            );
        }

        stageLabel.textContent =
            progressStageLabel(status);

        statusText.textContent =
            data.message
            || data.lastCheckpoint
            || data.currentStage
            || buildProgressMessage(
                processed,
                total
            );
    }

    /**
     * Returns a user-facing progress phase without exposing internal
     * lifecycle names.
     *
     * @param {string} status
     * @returns {string}
     */
    function progressStageLabel(status) {
        switch (status) {
            case 'CREATED':
            case 'QUEUED':
            case 'INITIALIZING':
            case 'VALIDATING_FILE':
                return 'Preparing and validating file';

            case 'READING_ROWS':
                return 'Reading and counting rows';

            case 'VALIDATING_ROWS':
                return 'Validating student records';

            case 'MAPPING':
                return 'Preparing valid records';

            case 'SAVING_BATCH':
                return 'Registering valid records';

            case 'GENERATING_REPORT':
                return 'Preparing failed-row report';

            case 'NOTIFYING':
                return 'Finalizing import';

            case 'PAUSED':
                return 'Import paused';

            case 'RESUMED':
                return 'Import resumed';

            default:
                return humanizeStatus(status)
                    || 'Processing import';
        }
    }

    function finishImport(status, data) {
        running = false;
        activeRequest = null;
        closeButton.disabled = false;
        cancelButton.textContent = 'Close';

        setStage('COMPLETE');
        setProgress(100);

        const processed = firstNumber(
            data.processedRows,
            data.processed,
            data.processedCount,
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
            Math.max(0, processed - successful)
        ) ?? 0;

        const total = firstNumber(
            data.totalRows,
            data.total,
            data.totalCount,
            processed,
            successful + failed
        ) ?? 0;

        setResultCounters({
            total,
            successful,
            failed
        });

        progressStep.classList.add('hidden');
        resultStep.classList.remove('hidden');
        uploadButton.classList.add('hidden');

        resultActionButton.classList.add('hidden');
        resultActionButton.disabled = false;
        resultActionButton.onclick = null;

        const label = moduleLabel();

        if (status === 'COMPLETED') {
            setResultIcon(
                'bi-check-lg',
                'success'
            );

            resultTitle.textContent =
                'Import completed';

            resultMessage.textContent =
                data.message
                || `${successful || total} ${label.toLowerCase()} record(s) were imported successfully.`;

            invokeSuccessCallbackOnce();
            return;
        }

        if (status === 'COMPLETED_WITH_ERRORS') {
            setResultIcon(
                'bi-exclamation-lg',
                'warning'
            );

            resultTitle.textContent =
                'Import completed with errors';

            if (currentModule === 'student') {
                resultMessage.textContent =
                    data.message
                    || 'Valid Student rows were imported. Download the failed-rows workbook, correct only those records, and retry them securely.';

                if (currentJobId) {
                    configureResultActions(currentJobId);
                }
            } else {
                resultMessage.textContent =
                    data.message
                    || 'Valid Employee rows were imported. Download the error report, correct the failed rows and import them again.';

                if (currentJobId) {
                    configureErrorOnlyAction(currentJobId);
                }
            }

            invokeSuccessCallbackOnce();
            return;
        }

        setResultIcon(
            'bi-x-lg',
            'danger'
        );

        resultTitle.textContent =
            status === 'CANCELLED'
                ? 'Import cancelled'
                : 'Import failed';

        resultMessage.textContent =
            data.message
            || data.errorMessage
            || data.lastCheckpoint
            || `The ${label.toLowerCase()} file could not be imported.`;

        if (currentJobId) {
            configureErrorOnlyAction(currentJobId);
        }
    }

    function invokeSuccessCallbackOnce() {
        if (successCallbackInvoked) {
            return;
        }

        successCallbackInvoked = true;

        if (typeof currentSuccessCallback === 'function') {
            currentSuccessCallback();
        }
    }

    /**
     * @param {unknown} failure
     */
    function showFailedStart(failure) {
        running = false;
        closeButton.disabled = false;
        cancelButton.textContent = 'Cancel';

        progressStep.classList.add('hidden');
        fileStep.classList.remove('hidden');
        uploadButton.classList.remove('hidden');
        uploadButton.disabled = false;

        const message = getErrorMessage(
            failure,
            'The import could not be started.'
        );

        showValidation(message);
        showErrorMessage?.(message);
    }

    /**
     * @param {unknown} failure
     * @param {string} fallback
     * @returns {string}
     */
    function getErrorMessage(failure, fallback) {
        if (typeof failure === 'string' && failure.trim()) {
            return failure.trim();
        }

        if (failure instanceof Error && failure.message.trim()) {
            return failure.message.trim();
        }

        if (isRecord(failure)) {
            const message = failure.message;

            if (typeof message === 'string' && message.trim()) {
                return message.trim();
            }
        }

        return fallback;
    }

    /** @param {string} jobId */
    function configureResultActions(jobId) {
        if (!jobId) {
            return;
        }

        if (currentModule !== 'student') {
            configureErrorOnlyAction(jobId);
            return;
        }

        resultActionPhase = 'CORRECTED';
        resultActionButton.classList.remove('hidden');

        updateResultActionButton();

        resultActionButton.onclick = async () => {
            if (resultActionButton.disabled) {
                return;
            }

            resultActionButton.disabled = true;

            try {
                if (resultActionPhase === 'CORRECTED') {
                    await downloadJobWorkbook(
                        `/api/import/corrected/${
                            encodeURIComponent(jobId)
                        }`,
                        `Failed_Student_Rows_${jobId}.xlsx`
                    );

                    resultActionPhase = 'ERRORS';
                    updateResultActionButton();
                    return;
                }

                if (resultActionPhase === 'ERRORS') {
                    await downloadJobWorkbook(
                        `/api/import/errors/${
                            encodeURIComponent(jobId)
                        }`,
                        `${moduleLabel()}_Import_Errors_${jobId}.xlsx`
                    );

                    resultActionPhase = 'RETRY';
                    updateResultActionButton();
                    return;
                }

                prepareRetrySelection(jobId);
            } catch (error) {
                showErrorMessage?.(
                    getErrorMessage(
                        error,
                        'The import file could not be downloaded.'
                    )
                );
            } finally {
                resultActionButton.disabled = false;
            }
        };
    }

    /** @param {string} jobId */
    function configureErrorOnlyAction(jobId) {
        resultActionPhase = 'ERRORS';
        resultActionButton.classList.remove('hidden');

        updateResultActionButton();

        resultActionButton.onclick = async () => {
            resultActionButton.disabled = true;

            try {
                await downloadJobWorkbook(
                    `/api/import/errors/${
                        encodeURIComponent(jobId)
                    }`,
                    `${moduleLabel()}_Import_Errors_${jobId}.xlsx`
                );
            } catch (error) {
                showErrorMessage?.(
                    getErrorMessage(
                        error,
                        'Could not download the error report.'
                    )
                );
            } finally {
                resultActionButton.disabled = false;
            }
        };
    }

    function updateResultActionButton() {
        if (resultActionPhase === 'CORRECTED') {
            setButtonLabel(
                resultActionButton,
                'Download Failed Rows Workbook',
                'bi-file-earmark-spreadsheet'
            );
            return;
        }

        if (resultActionPhase === 'ERRORS') {
            setButtonLabel(
                resultActionButton,
                'Download Error Report',
                'bi-download'
            );
            return;
        }

        setButtonLabel(
            resultActionButton,
            'Retry Failed Rows',
            'bi-arrow-repeat'
        );
    }

    async function downloadJobWorkbook(
        endpoint,
        fallbackFilename
    ) {
        const response = await fetch(endpoint, {
            method: 'GET',
            headers: authHeaders(),
            credentials: 'include',
            cache: 'no-store'
        });

        if (!response.ok) {
            throw new Error(
                `Download failed with status ${response.status}.`
            );
        }

        const blob = await response.blob();

        if (blob.size <= 0) {
            throw new Error(
                'The downloaded workbook is empty.'
            );
        }

        const filename =
            resolveDownloadFilename(
                response,
                fallbackFilename
            );

        triggerBrowserDownload(blob, filename);
    }

    function resolveDownloadFilename(
        response,
        fallbackFilename
    ) {
        const disposition =
            response.headers.get(
                'Content-Disposition'
            ) || '';

        const utfMatch = disposition.match(
            /filename\*=UTF-8''([^;]+)/i
        );

        if (utfMatch?.[1]) {
            try {
                return decodeURIComponent(
                    utfMatch[1].replaceAll('"', '')
                );
            } catch {
                return utfMatch[1].replaceAll('"', '');
            }
        }

        const basicMatch = disposition.match(
            /filename="?([^";]+)"?/i
        );

        return basicMatch?.[1]
            || fallbackFilename;
    }

    function triggerBrowserDownload(
        blob,
        filename
    ) {
        const url = URL.createObjectURL(blob);
        const anchor = document.createElement('a');

        anchor.href = url;
        anchor.download = filename;
        anchor.hidden = true;

        document.body.appendChild(anchor);
        anchor.click();
        anchor.remove();

        window.setTimeout(
            () => URL.revokeObjectURL(url),
            1000
        );
    }

    /** @param {string} originalJobId */
    function prepareRetrySelection(originalJobId) {
        if (currentModule !== 'student') {
            return;
        }

        retryOriginalJobId = originalJobId;
        resultActionPhase = null;
        currentJobId = null;
        terminalStatusHandled = false;
        successCallbackInvoked = false;
        currentProgressValue = 0;
        currentStageIndex = 0;
        setProgress(0, true);

        clearSelectedFile();
        hideValidation();

        resultStep.classList.add('hidden');
        progressStep.classList.add('hidden');
        fileStep.classList.remove('hidden');

        uploadButton.classList.remove('hidden');
        uploadButton.disabled = true;

        cancelButton.textContent = 'Cancel';
        closeButton.disabled = false;

        title.textContent =
            `Retry Failed ${moduleLabel()} Records`;

        description.textContent =
            'Select the failed-rows workbook downloaded from the previous import.';

        if (informationText) {
            informationText.textContent =
                'Only the row numbers that failed in the original import will be processed.';
        }

        if (dropTitle) {
            dropTitle.textContent =
                'Drop the failed-rows XLSX workbook here';
        }

        setButtonLabel(
            uploadButton,
            'Retry Failed Rows',
            'bi-arrow-repeat'
        );

        window.setTimeout(
            () => dropZone.focus(),
            50
        );
    }

    function authHeaders() {
        return typeof getAuthHeaders === 'function'
            ? getAuthHeaders()
            : {};
    }

    function showValidation(message) {
        validationMessage.textContent =
            String(message || '');

        validationMessage.classList.remove('hidden');
    }

    function hideValidation() {
        validationMessage.textContent = '';
        validationMessage.classList.add('hidden');
    }

    /**
     * Updates the visible progress value.
     *
     * @param {number} value
     * @param {boolean} [allowDecrease=false]
     */
    function setProgress(
        value,
        allowDecrease = false
    ) {
        const safeValue = Math.max(
            0,
            Math.min(
                100,
                Number(value) || 0
            )
        );

        const nextValue = allowDecrease
            ? safeValue
            : Math.max(
                currentProgressValue,
                safeValue
            );

        currentProgressValue = nextValue;

        progressBar.style.width =
            `${nextValue}%`;

        progressPercent.textContent =
            `${Math.round(nextValue)}%`;

        progressTrack.setAttribute(
            'aria-valuenow',
            String(Math.round(nextValue))
        );
    }

    /**
     * Moves the import stage forward only.
     *
     * Backend progress responses can arrive slightly out of order. A late
     * validation response must never move the UI from Process back to
     * Validate, and no backend status may move it back to Upload.
     *
     * @param {string} stageName
     */
    function setStage(stageName) {
        const order = [
            'UPLOAD',
            'VALIDATE',
            'PROCESS',
            'COMPLETE'
        ];

        const requestedIndex =
            order.indexOf(stageName);

        if (requestedIndex < 0) {
            return;
        }

        currentStageIndex = Math.max(
            currentStageIndex,
            requestedIndex
        );

        modal.querySelectorAll(
            '.erp-import-stage'
        ).forEach(element => {
            const index =
                order.indexOf(
                    element.dataset.stage
                );

            element.classList.toggle(
                'active',
                index === currentStageIndex
            );

            element.classList.toggle(
                'complete',
                index >= 0
                && index < currentStageIndex
            );
        });
    }

    /**
     * @param {{
     *     total?: number|null,
     *     successful?: number|null,
     *     failed?: number|null,
     *     processed?: number|null
     * }} values
     */
    function setCounters(values = {}) {
        const {
            total = null,
            successful = null,
            failed = null,
            processed = null
        } = values;
        setText(
            'import-total-count',
            formatCount(total)
        );

        setText(
            'import-success-count',
            formatCount(successful)
        );

        setText(
            'import-failed-count',
            formatCount(failed)
        );

        setText(
            'import-processed-count',
            formatCount(processed)
        );
    }

    function setResultCounters({
                                   total = 0,
                                   successful = 0,
                                   failed = 0
                               }) {
        setText(
            'import-result-total',
            formatCount(total, '0')
        );

        setText(
            'import-result-success',
            formatCount(successful, '0')
        );

        setText(
            'import-result-failed',
            formatCount(failed, '0')
        );
    }

    function setText(id, value) {
        const element =
            document.getElementById(id);

        if (element) {
            element.textContent = value;
        }
    }

    function setResultIcon(
        iconClass,
        stateClass = null
    ) {
        resultIcon.className =
            'erp-import-result-icon';

        if (stateClass) {
            resultIcon.classList.add(stateClass);
        }

        const icon =
            resultIcon.querySelector('i');

        if (icon) {
            icon.className =
                `bi ${iconClass}`;
        }
    }

    function setButtonLabel(
        button,
        label,
        iconClass
    ) {
        if (!(button instanceof HTMLButtonElement)) {
            return;
        }

        const icon = button.querySelector('i');

        if (icon) {
            icon.className = `bi ${iconClass}`;
        }

        const textNode = Array.from(
            button.childNodes
        ).find(node =>
            node.nodeType === Node.TEXT_NODE
            && String(node.textContent || '').trim()
        );

        if (textNode) {
            textNode.textContent = ` ${label}`;
            return;
        }

        button.append(
            document.createTextNode(` ${label}`)
        );
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

    function formatCount(
        value,
        fallback = '—'
    ) {
        return Number.isFinite(Number(value))
            ? Number(value).toLocaleString()
            : fallback;
    }

    function formatBytes(bytes) {
        if (
            !Number.isFinite(bytes)
            || bytes <= 0
        ) {
            return '0 bytes';
        }

        const units = [
            'bytes',
            'KB',
            'MB',
            'GB'
        ];

        const index = Math.min(
            Math.floor(
                Math.log(bytes)
                / Math.log(1024)
            ),
            units.length - 1
        );

        const value =
            bytes / (1024 ** index);

        return `${value.toFixed(
            index === 0 ? 0 : 1
        )} ${units[index]}`;
    }

    function normalizeStatus(status) {
        return String(status || '')
            .trim()
            .toUpperCase()
            .replaceAll(' ', '_');
    }

    function normalizeModule(moduleName) {
        const normalized =
            String(moduleName || '')
                .trim()
                .toLowerCase();

        return /^[a-z0-9_]+$/.test(normalized)
            ? normalized
            : '';
    }

    function moduleLabel() {
        if (!currentModule) {
            return 'Data';
        }

        return currentModule
            .replaceAll('_', ' ')
            .replace(
                /\b\w/g,
                character =>
                    character.toUpperCase()
            );
    }

    function humanizeStatus(status) {
        return String(status || '')
            .toLowerCase()
            .replaceAll('_', ' ')
            .replace(
                /\b\w/g,
                character =>
                    character.toUpperCase()
            );
    }

    function buildProgressMessage(
        processed,
        total
    ) {
        if (
            Number.isFinite(processed)
            && Number.isFinite(total)
            && total > 0
        ) {
            return `Processed ${
                processed.toLocaleString()
            } of ${
                total.toLocaleString()
            } rows.`;
        }

        return `The server is validating and importing ${
            moduleLabel().toLowerCase()
        } rows…`;
    }

    function stopPolling() {
        pollGeneration += 1;

        if (pollTimer !== null) {
            window.clearTimeout(pollTimer);
            pollTimer = null;
        }

        pollRequestInFlight = false;
    }

    if (document.readyState === 'loading') {
        document.addEventListener(
            'DOMContentLoaded',
            init,
            { once: true }
        );
    } else {
        init();
    }

    return Object.freeze({
        open
    });
})();
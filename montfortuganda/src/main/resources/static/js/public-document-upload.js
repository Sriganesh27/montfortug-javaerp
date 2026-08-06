(() => {
    'use strict';

    const INFO_ENDPOINT =
        '/api/public/applications/document-upload/info';

    const UPLOAD_ENDPOINT =
        '/api/public/applications/document-upload';

    const SCHOOL_LOGO_ENDPOINT =
        '/api/public/applications/document-upload/logo';

    const MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024;

    const ALLOWED_EXTENSIONS =
        ['pdf', 'jpg', 'jpeg', 'png'];

    let secureToken = null;
    let uploadAllowed = false;

    document.addEventListener('DOMContentLoaded', initialize);

    function initialize() {
        secureToken = readTokenFromUrl();

        /*
         * Remove the raw token from the address bar and browser history as
         * soon as it has been copied into memory.
         */
        if (secureToken) {
            window.history.replaceState(
                null,
                document.title,
                '/apply/document-upload'
            );
        }

        document
            .getElementById('uploadForm')
            .addEventListener('submit', handleUpload);

        if (!secureToken) {
            showUnavailable(
                'The document upload link is missing or invalid.'
            );
            return;
        }

        loadRequestInformation();
    }

    function readTokenFromUrl() {
        const params =
            new URLSearchParams(window.location.search);

        const token =
            params.get('token');

        return token && token.trim()
            ? token.trim()
            : null;
    }

    async function loadRequestInformation() {
        try {
            const body =
                new URLSearchParams();

            body.set('token', secureToken);

            const response =
                await fetch(INFO_ENDPOINT, {
                    method: 'POST',
                    headers: {
                        'Content-Type':
                            'application/x-www-form-urlencoded;charset=UTF-8'
                    },
                    cache: 'no-store',
                    credentials: 'same-origin',
                    body: body.toString()
                });

            const payload =
                await readJsonSafely(response);

            if (!response.ok
                    || !payload
                    || payload.success !== true
                    || !payload.data) {
                throw new Error(
                    getApiMessage(
                        payload,
                        'The document upload link is invalid or unavailable.'
                    )
                );
            }

            renderRequest(payload.data);

        } catch (error) {
            showUnavailable(
                safeErrorMessage(
                    error,
                    'The secure upload request could not be loaded.'
                )
            );
        }
    }

    function renderRequest(data) {
        uploadAllowed =
            data.uploadAllowed === true;

        setText('schoolName', data.schoolName);
        renderSchoolBranding(data);
        setText('applicationNo', data.applicationNo);
        setText('studentName', data.studentName);
        setText(
            'documentName',
            data.requestedDocumentName
                || data.requestedDocumentType
        );
        setText('requestReason', data.requestReason);

        toggleDetail(
            'remarksDetail',
            'publicRemarks',
            data.publicRemarks
        );

        toggleDetail(
            'deadlineDetail',
            'uploadDeadline',
            formatDateTime(
                data.uploadDeadline
                    || data.uploadTokenExpiresAt
            )
        );

        hide('loadingState');

        if (!uploadAllowed) {
            showUnavailable(
                data.uploadUnavailableReason
                    || 'This document upload request is unavailable.'
            );
            return;
        }

        show('requestSection');
    }

    function renderSchoolBranding(data) {
        const schoolName =
            data && data.schoolName
                ? String(data.schoolName).trim()
                : 'School';

        setText(
            'headerSchoolName',
            schoolName
        );

        setText(
            'headerSchoolLogoFallback',
            buildSchoolInitials(
                schoolName
            )
        );

        document.title =
            `Upload Requested Document | ${schoolName}`;

        const logo =
            document.getElementById(
                'headerSchoolLogo'
            );

        if (logo) {
            logo.alt =
                `${schoolName} logo`;
        }

        void loadSchoolLogo();
    }

    async function loadSchoolLogo() {
        if (!secureToken) {
            showSchoolLogoFallback();
            return;
        }

        try {
            const body =
                new URLSearchParams();

            body.set(
                'token',
                secureToken
            );

            const response =
                await fetch(
                    SCHOOL_LOGO_ENDPOINT,
                    {
                        method: 'POST',
                        headers: {
                            'Content-Type':
                                'application/x-www-form-urlencoded;charset=UTF-8'
                        },
                        cache: 'no-store',
                        credentials: 'same-origin',
                        body: body.toString()
                    }
                );

            if (!response.ok) {
                showSchoolLogoFallback();
                return;
            }

            const logoBlob =
                await response.blob();

            if (!logoBlob.type.startsWith('image/')) {
                showSchoolLogoFallback();
                return;
            }

            const dataUrl =
                await blobToDataUrl(
                    logoBlob
                );

            const logo =
                document.getElementById(
                    'headerSchoolLogo'
                );

            if (!logo) {
                return;
            }

            logo.onload = () => {
                hide(
                    'headerSchoolLogoFallback'
                );
                show(
                    'headerSchoolLogo'
                );
            };

            logo.onerror = () => {
                logo.removeAttribute('src');
                showSchoolLogoFallback();
            };

            logo.src = dataUrl;

        } catch (error) {
            showSchoolLogoFallback();
        }
    }

    function showSchoolLogoFallback() {
        const logo =
            document.getElementById(
                'headerSchoolLogo'
            );

        if (logo) {
            logo.removeAttribute('src');
        }

        hide('headerSchoolLogo');
        show('headerSchoolLogoFallback');
    }

    function blobToDataUrl(blob) {
        return new Promise(
            (resolve, reject) => {
                const reader =
                    new FileReader();

                reader.addEventListener(
                    'load',
                    () => resolve(
                        String(
                            reader.result || ''
                        )
                    )
                );

                reader.addEventListener(
                    'error',
                    () => reject(
                        new Error(
                            'School logo could not be read.'
                        )
                    )
                );

                reader.readAsDataURL(blob);
            }
        );
    }

    function buildSchoolInitials(
        schoolName
    ) {
        const meaningfulWords =
            String(schoolName || '')
                .replace(
                    /[^A-Za-z0-9 ]+/g,
                    ' '
                )
                .split(/\s+/)
                .map(word => word.trim())
                .filter(Boolean)
                .filter(word =>
                    ![
                        'school',
                        'nursery',
                        'primary',
                        'secondary',
                        'college',
                        'academy'
                    ].includes(
                        word.toLowerCase()
                    )
                );

        const initials =
            meaningfulWords
                .slice(0, 2)
                .map(word =>
                    word.charAt(0)
                        .toUpperCase()
                )
                .join('');

        if (initials) {
            return initials;
        }

        return 'S';
    }

    function handleUpload(event) {
        event.preventDefault();

        if (!uploadAllowed || !secureToken) {
            showFormError(
                'This upload link is not available.'
            );
            return;
        }

        const fileInput =
            document.getElementById('documentFile');

        const file =
            fileInput.files
                ? fileInput.files[0]
                : null;

        const validationError =
            validateFile(file);

        if (validationError) {
            showFormError(validationError);
            return;
        }

        clearFormError();
        setUploadingState(true);
        resetProgress();

        const formData =
            new FormData();

        formData.append('token', secureToken);
        formData.append('file', file);

        const request =
            new XMLHttpRequest();

        request.open(
            'POST',
            UPLOAD_ENDPOINT,
            true
        );

        request.withCredentials = true;
        request.responseType = 'json';

        request.upload.addEventListener(
            'progress',
            updateProgress
        );

        request.addEventListener(
            'load',
            () => handleUploadComplete(request)
        );

        request.addEventListener(
            'error',
            () => {
                setUploadingState(false);
                showFormError(
                    'A secure network connection could not be established. Please try again.'
                );
            }
        );

        request.addEventListener(
            'abort',
            () => {
                setUploadingState(false);
                showFormError(
                    'The upload was cancelled.'
                );
            }
        );

        request.send(formData);
    }

    function validateFile(file) {
        if (!file) {
            return 'Please select a document to upload.';
        }

        if (file.size <= 0) {
            return 'The selected document is empty.';
        }

        if (file.size > MAX_FILE_SIZE_BYTES) {
            return 'The document is larger than 10 MB.';
        }

        const extension =
            getFileExtension(file.name);

        if (!ALLOWED_EXTENSIONS.includes(extension)) {
            return 'Only PDF, JPG, JPEG, and PNG files are allowed.';
        }

        return null;
    }

    function handleUploadComplete(request) {
        const payload =
            request.response
            || parseJsonText(request.responseText);

        if (request.status < 200
                || request.status >= 300
                || !payload
                || payload.success !== true
                || !payload.data) {
            setUploadingState(false);

            showFormError(
                getApiMessage(
                    payload,
                    'The document could not be uploaded.'
                )
            );
            return;
        }

        uploadAllowed = false;
        secureToken = null;

        renderSuccess(payload.data);
    }

    function renderSuccess(data) {
        hide('requestSection');
        hide('unavailableSection');
        show('successSection');

        setText(
            'successMessage',
            data.message
                || 'The document was uploaded successfully and is awaiting school review.'
        );

        setText(
            'successApplicationNo',
            data.applicationNo
        );

        setText(
            'successFileName',
            data.uploadedFileName
        );

        const fileInput =
            document.getElementById('documentFile');

        fileInput.value = '';
    }

    function updateProgress(event) {
        show('progressWrap');

        if (!event.lengthComputable) {
            setText('progressText', 'Uploading...');
            return;
        }

        const percent =
            Math.min(
                100,
                Math.round(
                    (event.loaded / event.total) * 100
                )
            );

        document
            .getElementById('progressBar')
            .style.width = `${percent}%`;

        setText(
            'progressText',
            `${percent}%`
        );
    }

    function resetProgress() {
        show('progressWrap');

        document
            .getElementById('progressBar')
            .style.width = '0%';

        setText('progressText', '0%');
    }

    function setUploadingState(uploading) {
        const button =
            document.getElementById('uploadButton');

        const fileInput =
            document.getElementById('documentFile');

        button.disabled = uploading;
        fileInput.disabled = uploading;

        button.textContent =
            uploading
                ? 'Uploading...'
                : 'Upload Document';
    }

    function showUnavailable(message) {
        hide('loadingState');
        hide('requestSection');
        hide('successSection');
        show('unavailableSection');

        setText(
            'unavailableMessage',
            message
        );
    }

    function showFormError(message) {
        const element =
            document.getElementById('formMessage');

        element.textContent = message;
        element.classList.remove('hidden');
    }

    function clearFormError() {
        const element =
            document.getElementById('formMessage');

        element.textContent = '';
        element.classList.add('hidden');
    }

    function toggleDetail(
        containerId,
        valueId,
        value
    ) {
        if (value) {
            setText(valueId, value);
            show(containerId);
        } else {
            hide(containerId);
        }
    }

    function formatDateTime(value) {
        if (!value) {
            return '';
        }

        const date =
            new Date(value);

        if (Number.isNaN(date.getTime())) {
            return value;
        }

        return new Intl.DateTimeFormat(
            undefined,
            {
                dateStyle: 'medium',
                timeStyle: 'short'
            }
        ).format(date);
    }

    function getFileExtension(fileName) {
        if (!fileName || !fileName.includes('.')) {
            return '';
        }

        return fileName
            .split('.')
            .pop()
            .toLowerCase();
    }

    async function readJsonSafely(response) {
        try {
            return await response.json();
        } catch (error) {
            return null;
        }
    }

    function parseJsonText(value) {
        if (!value) {
            return null;
        }

        try {
            return JSON.parse(value);
        } catch (error) {
            return null;
        }
    }

    function getApiMessage(
        payload,
        fallback
    ) {
        return payload
            && typeof payload.message === 'string'
            && payload.message.trim()
            ? payload.message.trim()
            : fallback;
    }

    function safeErrorMessage(
        error,
        fallback
    ) {
        return error
            && typeof error.message === 'string'
            && error.message.trim()
            ? error.message.trim()
            : fallback;
    }

    function setText(
        elementId,
        value
    ) {
        const element =
            document.getElementById(elementId);

        if (element) {
            element.textContent =
                value == null
                    ? ''
                    : String(value);
        }
    }

    function show(elementId) {
        const element =
            document.getElementById(elementId);

        if (element) {
            element.classList.remove('hidden');
        }
    }

    function hide(elementId) {
        const element =
            document.getElementById(elementId);

        if (element) {
            element.classList.add('hidden');
        }
    }
})();

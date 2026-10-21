/*
 * Branch Profile frontend module
 *
 * Step 4A:
 * - Loads the authenticated branch profile.
 * - Populates the existing Branch Profile view.
 * - Uses backend canEdit to decide whether the Edit Profile control is available.
 *
 * This step intentionally does NOT:
 * - save profile changes
 * - upload files
 * - call private file endpoints
 * - modify education levels
 */

(() => {
    'use strict';

    const PROFILE_ENDPOINT = '/branchadmin/profile';
    const PROFILE_MEDIA_ENDPOINT = '/api/branchadmin/profile/files';
    let initializationInProgress = false;
    let initializationPromise = null;
    let profileLogoRequestUrl = null;
    let profileLogoPreloadPromise = null;

    const state = {
        profile: null,
        canEdit: false,
        editMode: false,
        saving: false
    };

    function getElement(id) {
        return document.getElementById(id);
    }

    function setText(id, value) {
        const element = getElement(id);

        if (!element) {
            return;
        }

        const text = value === null || value === undefined || value === ''
            ? '—'
            : String(value);

        element.textContent = text;
    }

    function setInputValue(id, value) {
        const element = getElement(id);

        if (!element) {
            return;
        }

        element.value = value === null || value === undefined
            ? ''
            : String(value);
    }

    function setHidden(id, hidden) {
        const element = getElement(id);

        if (!element) {
            return;
        }

        element.classList.toggle('hidden', hidden);
    }

    function normalizeList(value) {
        return Array.isArray(value) ? value : [];
    }

    function getLevelNames(profile) {
        const levels = normalizeList(profile?.levels);

        return levels
            .map(level => {
                if (typeof level === 'string') {
                    return level.trim();
                }

                if (!level || typeof level !== 'object') {
                    return '';
                }

                return String(
                    level.levelName
                    ?? level.name
                    ?? level.label
                    ?? ''
                ).trim();
            })
            .filter(Boolean);
    }

    function parseInchargeDetails(value) {
        if (Array.isArray(value)) {
            return value;
        }

        if (!value || typeof value !== 'string') {
            return [];
        }

        try {
            const parsed = JSON.parse(value);
            return Array.isArray(parsed) ? parsed : [];
        } catch (error) {
            console.warn(
                'Unable to parse branch incharge details.',
                error
            );
            return [];
        }
    }

    function renderIncharge(profile) {
        const body = getElement('ba-profileInchargeBody');

        if (!body) {
            return;
        }

        body.replaceChildren();

        const entries = parseInchargeDetails(
            profile?.inchargeDetails
        );

        if (entries.length === 0) {
            const row = document.createElement('tr');
            const cell = document.createElement('td');

            cell.colSpan = 4;
            cell.className = 'text-muted';
            cell.textContent = 'No incharge details available.';

            row.appendChild(cell);
            body.appendChild(row);
            return;
        }

        entries.forEach(entry => {
            const row = document.createElement('tr');

            const nameCell = document.createElement('td');
            const roleCell = document.createElement('td');
            const phoneCell = document.createElement('td');
            const actionCell = document.createElement('td');

            nameCell.textContent = entry?.name || '—';
            roleCell.textContent = entry?.role || '—';
            phoneCell.textContent = entry?.phone || '—';
            actionCell.textContent = '—';

            row.append(
                nameCell,
                roleCell,
                phoneCell,
                actionCell
            );

            body.appendChild(row);
        });
    }

    function renderEducationLevels(profile) {
        const names = getLevelNames(profile);

        setText(
            'ba-viewEducationLevels',
            names.length > 0 ? names.join(', ') : '—'
        );
    }

    function formatDisplayDate(value) {
        if (!value) return '—';
        const match = String(value).match(/^(\d{4})-(\d{2})-(\d{2})/);
        if (match) return `${match[3]}-${match[2]}-${match[1]}`;
        return String(value);
    }

    function preloadProfileLogo() {
        // Start the private logo request as soon as Branch Profile initializes,
        // in parallel with the profile API request. Reuse this exact URL below
        // so the browser can use the already-loaded image instead of requesting
        // the logo a second time.
        if (profileLogoPreloadPromise) {
            return profileLogoPreloadPromise;
        }

        profileLogoRequestUrl = `${PROFILE_MEDIA_ENDPOINT}/logo?v=${Date.now()}`;
        profileLogoPreloadPromise = new Promise(resolve => {
            const preloader = new Image();
            preloader.decoding = 'async';
            preloader.onload = () => resolve(true);
            preloader.onerror = () => resolve(false);
            preloader.src = profileLogoRequestUrl;
        });

        return profileLogoPreloadPromise;
    }

    function renderImage(imageId, placeholderId, endpoint, storedPath) {
        const image = getElement(imageId);
        const placeholder = getElement(placeholderId);
        if (!image || !placeholder) return;

        image.onerror = () => {
            image.classList.add('hidden');
            image.removeAttribute('src');
            placeholder.classList.remove('hidden');
        };
        image.onload = () => {
            image.classList.remove('hidden');
            placeholder.classList.add('hidden');
        };

        if (!storedPath || !String(storedPath).trim()) {
            image.classList.add('hidden');
            image.removeAttribute('src');
            placeholder.classList.remove('hidden');
            return;
        }

        // Private media is served by an authenticated endpoint scoped to the
        // current Branch Admin's branch. Never put stored filesystem paths in URLs.
        // The logo reuses the preloaded URL to avoid a second late request.
        image.src = endpoint === 'logo' && profileLogoRequestUrl
            ? profileLogoRequestUrl
            : `${PROFILE_MEDIA_ENDPOINT}/${endpoint}?v=${Date.now()}`;
    }

    function renderGovernmentDocuments(profile) {
        const container = getElement('ba-profileDocumentsList');
        if (!container) return;
        container.replaceChildren();

        const stored = typeof profile?.govDocumentUrl === 'string'
            ? profile.govDocumentUrl.split(',').map(path => path.trim()).filter(Boolean)
            : [];

        if (!stored.length) {
            const empty = document.createElement('p');
            empty.className = 'text-muted';
            empty.textContent = 'No government documents available.';
            container.appendChild(empty);
            return;
        }

        stored.forEach((_, index) => {
            const row = document.createElement('div');
            row.className = 'ba-profile-document-item';
            const link = document.createElement('a');
            link.href = `${PROFILE_MEDIA_ENDPOINT}/documents/${index}`;
            link.target = '_blank';
            link.rel = 'noopener noreferrer';
            link.textContent = `Government document ${index + 1}`;
            row.appendChild(link);
            container.appendChild(row);
        });
    }

    function populateView(profile) {
        setText('ba-profileSchoolName', profile?.branchName);
        setText('ba-profileSchoolCode', profile?.schoolCode);
        setText('ba-profileUsername', profile?.username);
        setText(
            'ba-profileStatus',
            Number(profile?.isActive) === 1 ? 'Active' : 'Inactive'
        );

        setText('ba-viewFoundationDate', formatDisplayDate(profile?.foundationDate));

        renderImage('ba-profileLogo', 'ba-profileLogoPlaceholder', 'logo', profile?.branchLogoUrl);
        renderImage('ba-profileQrImage', 'ba-profileQrPlaceholder', 'qr-code', profile?.qrCodeUrl);
        renderImage('ba-profileSchoolPhoto', 'ba-profileSchoolPhotoPlaceholder', 'photo', profile?.schoolPhotoUrl);
        renderGovernmentDocuments(profile);

        setText('ba-viewBranchLocation', profile?.branchLocation);
        setText('ba-viewPoBox', profile?.poBox);
        setText('ba-viewAddressLine1', profile?.addressLine1);
        setText('ba-viewAddressLine2', profile?.addressLine2);
        setText('ba-viewLocality', profile?.locality);
        setText('ba-viewCity', profile?.city);
        setText('ba-viewDistrict', profile?.district);
        setText('ba-viewRegion', profile?.region);
        setText('ba-viewCountry', profile?.country);
        setText('ba-viewPostalCode', profile?.postalCode);

        setText('ba-viewPrimaryPhone', profile?.primaryPhone);
        setText('ba-viewSecondaryPhone', profile?.secondaryPhone);
        setText('ba-viewWhatsappPhone', profile?.whatsappPhone);
        setText('ba-viewBranchEmail', profile?.branchEmail);
        setText('ba-viewEmailFromName', profile?.emailFromName);
        setText('ba-viewEmailReplyTo', profile?.emailReplyTo);

        setText('ba-viewBankName', profile?.bankName);
        setText(
            'ba-viewBankAccountName',
            profile?.bankAccountName
        );
        setText(
            'ba-viewBankAccountNumber',
            profile?.bankAccountNumber
        );
        setText('ba-viewBankBranch', profile?.bankBranch);
        setText(
            'ba-viewAirtelPayNumber',
            profile?.airtelPayNumber
        );
        setText(
            'ba-viewAirtelPayName',
            profile?.airtelPayName
        );

        setInputValue(
            'ba-editFoundationDate',
            profile?.foundationDate
        );

        setInputValue(
            'ba-editBranchLocation',
            profile?.branchLocation
        );
        setInputValue('ba-editPoBox', profile?.poBox);
        setInputValue(
            'ba-editAddressLine1',
            profile?.addressLine1
        );
        setInputValue(
            'ba-editAddressLine2',
            profile?.addressLine2
        );
        setInputValue('ba-editLocality', profile?.locality);
        setInputValue('ba-editCity', profile?.city);
        setInputValue('ba-editDistrict', profile?.district);
        setInputValue('ba-editRegion', profile?.region);
        setInputValue('ba-editCountry', profile?.country);
        setInputValue(
            'ba-editPostalCode',
            profile?.postalCode
        );

        setInputValue(
            'ba-editPrimaryPhone',
            profile?.primaryPhone
        );
        setInputValue(
            'ba-editSecondaryPhone',
            profile?.secondaryPhone
        );
        setInputValue(
            'ba-editWhatsappPhone',
            profile?.whatsappPhone
        );
        setInputValue(
            'ba-editBranchEmail',
            profile?.branchEmail
        );
        setInputValue(
            'ba-editEmailFromName',
            profile?.emailFromName
        );
        setInputValue(
            'ba-editEmailReplyTo',
            profile?.emailReplyTo
        );

        setInputValue(
            'ba-editBankName',
            profile?.bankName
        );
        setInputValue(
            'ba-editBankAccountName',
            profile?.bankAccountName
        );
        setInputValue(
            'ba-editBankAccountNumber',
            profile?.bankAccountNumber
        );
        setInputValue(
            'ba-editBankBranch',
            profile?.bankBranch
        );
        setInputValue(
            'ba-editAirtelPayNumber',
            profile?.airtelPayNumber
        );
        setInputValue(
            'ba-editAirtelPayName',
            profile?.airtelPayName
        );

        renderEducationLevels(profile);
        renderIncharge(profile);
        populateInchargeEditor(profile);
    }

    function setEditMode(active) {
        state.editMode = active;

        const root = getElement('ba-branch-profile-view');
        const editButton = getElement('ba-branchProfileEditBtn');
        const cancelButton = getElement('ba-branchProfileCancelBtn');
        const saveButton = getElement('ba-branchProfileSaveBtn');

        root?.setAttribute('data-mode', active ? 'edit' : 'view');

        document
            .querySelectorAll('.ba-profile-view-control')
            .forEach(element => {
                element.classList.toggle('hidden', active);
            });

        document
            .querySelectorAll('.ba-profile-edit-control')
            .forEach(element => {
                element.classList.toggle(
                    'hidden',
                    !active
                );
            });

        /*
         * Education levels are permanently read-only for Branch Admin.
         * The existing editor container must remain hidden even in edit mode.
         */
        setHidden('ba-editEducationLevels', true);

        if (editButton) {
            editButton.classList.toggle(
                'hidden',
                active || !state.canEdit
            );
        }

        cancelButton?.classList.toggle('hidden', !active);
        saveButton?.classList.toggle('hidden', !active);

        if (!active) {
            getElement('ba-branchProfileValidationSummary')
                ?.classList.add('hidden');
        }
    }

    function applyEditPermission(canEdit) {
        state.canEdit = canEdit === true;

        /*
         * Backend permission is authoritative. The browser never grants
         * itself edit access.
         */
        if (!state.canEdit) {
            setEditMode(false);
            setHidden('ba-branchProfileEditBtn', true);
            return;
        }

        setEditMode(false);
        setHidden('ba-branchProfileEditBtn', false);
    }

    function populateInchargeEditor(profile) {
        const editor = getElement('ba-profileInchargeEditor');

        if (!editor) {
            return;
        }

        editor.replaceChildren();

        const entries = parseInchargeDetails(
            profile?.inchargeDetails
        );

        const rows = entries.length > 0
            ? entries
            : [{ name: '', role: '', phone: '' }];

        const table = document.createElement('table');
        table.className = 'data-table ba-profile-incharge-edit-table';

        const tbody = document.createElement('tbody');

        rows.forEach(entry => {
            tbody.appendChild(createInchargeEditRow(entry));
        });

        table.appendChild(tbody);
        editor.appendChild(table);

        const addButton = document.createElement('button');
        addButton.type = 'button';
        addButton.className = 'btn-secondary btn-sm';
        addButton.innerHTML =
            '<i class="bi bi-plus-lg"></i> Add Incharge';

        addButton.addEventListener('click', () => {
            tbody.appendChild(
                createInchargeEditRow({
                    name: '',
                    role: '',
                    phone: ''
                })
            );
        });

        editor.appendChild(addButton);
    }

    function createInchargeEditRow(entry = {}) {
        const row = document.createElement('tr');

        const nameCell = document.createElement('td');
        const roleCell = document.createElement('td');
        const phoneCell = document.createElement('td');
        const actionCell = document.createElement('td');

        const nameInput = document.createElement('input');
        nameInput.type = 'text';
        nameInput.className = 'w-100';
        nameInput.maxLength = 255;
        nameInput.value = entry?.name || '';
        nameInput.dataset.inchargeField = 'name';

        const roleInput = document.createElement('input');
        roleInput.type = 'text';
        roleInput.className = 'w-100';
        roleInput.maxLength = 255;
        roleInput.value = entry?.role || '';
        roleInput.dataset.inchargeField = 'role';

        const phoneInput = document.createElement('input');
        phoneInput.type = 'text';
        phoneInput.className = 'w-100';
        phoneInput.maxLength = 50;
        phoneInput.value = entry?.phone || '';
        phoneInput.dataset.inchargeField = 'phone';

        const removeButton = document.createElement('button');
        removeButton.type = 'button';
        removeButton.className = 'btn-secondary btn-sm';
        removeButton.title = 'Remove';
        removeButton.setAttribute('aria-label', 'Remove incharge');
        removeButton.innerHTML = '<i class="bi bi-trash"></i>';

        removeButton.addEventListener('click', () => {
            row.remove();
        });

        nameCell.appendChild(nameInput);
        roleCell.appendChild(roleInput);
        phoneCell.appendChild(phoneInput);
        actionCell.appendChild(removeButton);

        row.append(
            nameCell,
            roleCell,
            phoneCell,
            actionCell
        );

        return row;
    }

    function collectInchargeDetails() {
        const editor = getElement('ba-profileInchargeEditor');

        if (!editor) {
            return [];
        }

        return Array.from(
            editor.querySelectorAll('tbody tr')
        )
            .map(row => {
                const get = field =>
                    row.querySelector(
                        `[data-incharge-field="${field}"]`
                    )?.value?.trim() || '';

                return {
                    name: get('name'),
                    role: get('role'),
                    phone: get('phone')
                };
            })
            .filter(entry =>
                entry.name || entry.role || entry.phone
            );
    }

    function buildUpdateFormData() {
        const formData = new FormData();

        const fields = [
            ['foundationDate', 'ba-editFoundationDate'],
            ['branchLocation', 'ba-editBranchLocation'],
            ['poBox', 'ba-editPoBox'],
            ['addressLine1', 'ba-editAddressLine1'],
            ['addressLine2', 'ba-editAddressLine2'],
            ['locality', 'ba-editLocality'],
            ['city', 'ba-editCity'],
            ['district', 'ba-editDistrict'],
            ['region', 'ba-editRegion'],
            ['country', 'ba-editCountry'],
            ['postalCode', 'ba-editPostalCode'],
            ['primaryPhone', 'ba-editPrimaryPhone'],
            ['secondaryPhone', 'ba-editSecondaryPhone'],
            ['whatsappPhone', 'ba-editWhatsappPhone'],
            ['branchEmail', 'ba-editBranchEmail'],
            ['emailFromName', 'ba-editEmailFromName'],
            ['emailReplyTo', 'ba-editEmailReplyTo'],
            ['bankName', 'ba-editBankName'],
            ['bankAccountName', 'ba-editBankAccountName'],
            ['bankAccountNumber', 'ba-editBankAccountNumber'],
            ['bankBranch', 'ba-editBankBranch'],
            ['airtelPayNumber', 'ba-editAirtelPayNumber'],
            ['airtelPayName', 'ba-editAirtelPayName']
        ];

        fields.forEach(([name, id]) => {
            const element = getElement(id);

            if (element) {
                formData.append(name, element.value.trim());
            }
        });

        formData.append(
            'inchargeDetails',
            JSON.stringify(collectInchargeDetails())
        );

        const fileMappings = [
            ['logo', 'ba-profileLogoInput'],
            ['photo', 'ba-profileSchoolPhotoInput'],
            ['qrCode', 'ba-profileQrInput']
        ];

        fileMappings.forEach(([fieldName, id]) => {
            const input = getElement(id);
            const file = input?.files?.[0];

            if (file) {
                formData.append(fieldName, file);
            }
        });

        const documentsInput =
            getElement('ba-profileDocumentsInput');

        Array.from(documentsInput?.files || [])
            .forEach(file => {
                formData.append('documents', file);
            });

        return formData;
    }

    function validateEditForm() {
        const form = getElement('ba-branchProfileForm');
        const summary = getElement(
            'ba-branchProfileValidationSummary'
        );
        const text = getElement(
            'ba-branchProfileValidationText'
        );

        if (!form) {
            return true;
        }

        form.querySelectorAll('[aria-invalid="true"]')
            .forEach(element => {
                element.removeAttribute('aria-invalid');
            });

        if (form.checkValidity()) {
            summary?.classList.add('hidden');
            return true;
        }

        const invalidFields = form.querySelectorAll(
            'input:not(.hidden):invalid'
        );

        invalidFields.forEach(element => {
            element.setAttribute('aria-invalid', 'true');
        });

        if (text) {
            text.textContent =
                'Please check the highlighted fields and try again.';
        }

        summary?.classList.remove('hidden');

        invalidFields[0]?.focus();

        return false;
    }

    async function saveProfile() {
        if (!state.canEdit || state.saving) {
            return;
        }

        if (!validateEditForm()) {
            return;
        }

        const saveButton = getElement(
            'ba-branchProfileSaveBtn'
        );

        state.saving = true;

        if (saveButton) {
            saveButton.disabled = true;
        }

        try {
            const formData = buildUpdateFormData();

            const response = await window.apiMultipart(
                PROFILE_ENDPOINT,
                'PUT',
                formData
            );

            const profile = extractResponseData(response);

            if (!profile) {
                throw new Error(
                    'Branch profile was saved but no updated profile was returned.'
                );
            }

            state.profile = profile;

            populateView(profile);
            setEditMode(false);
            applyEditPermission(profile.canEdit);

            if (typeof showSuccessMessage === 'function') {
                showSuccessMessage(
                    'Branch profile updated successfully.'
                );
            }
        } catch (error) {
            console.error(
                'Unable to save Branch Profile:',
                error
            );

            if (typeof showErrorMessage === 'function') {
                showErrorMessage(
                    error?.message ||
                    'Unable to save the Branch Profile. Please try again.'
                );
            }
        } finally {
            state.saving = false;

            if (saveButton) {
                saveButton.disabled = false;
            }
        }
    }

    function wireEditControls() {
        const root = getElement('ba-branch-profile-view');
        if (root?.dataset.profileEventsBound === 'true') return;
        if (root) root.dataset.profileEventsBound = 'true';

        getElement('ba-branchProfileEditBtn')
            ?.addEventListener('click', () => {
                if (!state.canEdit) {
                    return;
                }

                populateInchargeEditor(state.profile);
                setEditMode(true);
            });

        getElement('ba-branchProfileCancelBtn')
            ?.addEventListener('click', () => {
                if (!state.editMode) {
                    return;
                }

                populateView(state.profile);
                setEditMode(false);
            });

        getElement('ba-branchProfileSaveBtn')
            ?.addEventListener('click', () => {
                void saveProfile();
            });

        [
            ['ba-profileLogoInput', 'ba-profileLogoFileName'],
            ['ba-profileSchoolPhotoInput', 'ba-profileSchoolPhotoFileName'],
            ['ba-profileQrInput', 'ba-profileQrFileName']
        ].forEach(([inputId, labelId]) => {
            const input = getElement(inputId);
            const label = getElement(labelId);

            input?.addEventListener('change', () => {
                label.textContent =
                    input.files?.[0]?.name ||
                    'No new file selected';
            });
        });
    }

    function extractResponseData(response) {
        if (!response) {
            return null;
        }

        /*
         * ApiResponse uses `data` for the successful payload.
         * Keep a small fallback for an already-unwrapped object.
         */
        if (
            response.data
            && typeof response.data === 'object'
            && !Array.isArray(response.data)
        ) {
            return response.data;
        }

        if (
            response.branchName
            || response.schoolCode
            || Object.prototype.hasOwnProperty.call(
                response,
                'canEdit'
            )
        ) {
            return response;
        }

        return null;
    }

    function unwrapArrayResponse(response) {
        if (Array.isArray(response)) return response;
        if (Array.isArray(response?.data)) return response.data;
        if (Array.isArray(response?.content)) return response.content;
        return null;
    }

    function formatSecurityDate(value) {
        if (value === null || value === undefined || value === '') {
            return '—';
        }

        // The API returns Java LocalDateTime values as ISO local timestamps.
        // Format their components directly; do not parse them as browser-local
        // or UTC Date objects, which can shift the displayed time.
        const text = String(value).trim();
        const match = text.match(
            /^(\d{4})-(\d{2})-(\d{2})(?:[T ](\d{2}):(\d{2})(?::(\d{2})(?:\.\d{1,9})?)?)?$/
        );

        if (!match) {
            return '—';
        }

        const year = Number(match[1]);
        const month = Number(match[2]);
        const day = Number(match[3]);
        const hour = match[4] === undefined ? null : Number(match[4]);
        const minute = match[5] === undefined ? null : Number(match[5]);
        const second = match[6] === undefined ? null : Number(match[6]);
        const daysInMonth = new Date(Date.UTC(year, month, 0)).getUTCDate();

        if (
            month < 1 || month > 12 || day < 1 || day > daysInMonth ||
            (hour !== null && (hour < 0 || hour > 23)) ||
            (minute !== null && (minute < 0 || minute > 59)) ||
            (second !== null && (second < 0 || second > 59))
        ) {
            return '—';
        }

        const datePart = `${match[3]}-${match[2]}-${match[1]}`;
        if (hour === null) {
            return datePart;
        }

        const timePart = `${match[4]}:${match[5]}${second === null ? '' : `:${match[6]}`}`;
        return `${datePart} ${timePart}`;
    }

    function appendSecurityCell(row, value) {
        const cell = document.createElement('td');
        cell.textContent = value === null || value === undefined || value === ''
            ? '—'
            : String(value);
        row.appendChild(cell);
    }

    function renderSecurityRows(bodyId, rows, columns, emptyMessage) {
        const body = getElement(bodyId);
        if (!body) return;
        body.replaceChildren();

        if (!rows.length) {
            const row = document.createElement('tr');
            const cell = document.createElement('td');
            cell.colSpan = columns;
            cell.className = 'text-muted';
            cell.textContent = emptyMessage;
            row.appendChild(cell);
            body.appendChild(row);
            return;
        }

        rows.forEach(values => {
            const row = document.createElement('tr');
            values.forEach(value => appendSecurityCell(row, value));
            body.appendChild(row);
        });
    }

    function ensureAccountSecurityUI() {
        const root = getElement('ba-branch-profile-view');
        if (!root) return;

        const devicesBody = getElement('ba-profileActiveDevicesBody');
        const historyBody = getElement('ba-profileLoginHistoryBody');
        const historyContainer = getElement('ba-profileLoginHistoryContainer');
        const historyToggle = getElement('ba-profileToggleLoginHistoryBtn');

        // Account Security structure is defined in the view HTML. Do not
        // construct or reposition UI elements from JavaScript.
        if (!devicesBody || !historyBody || !historyContainer || !historyToggle) {
            console.error('Account Security markup is incomplete.');
            return;
        }

        historyContainer.hidden = true;
        historyToggle.setAttribute('aria-expanded', 'false');
        historyToggle.innerHTML = '<i class="bi bi-clock-history" aria-hidden="true"></i> View History';

        if (historyToggle.dataset.handlerAttached === 'true') return;
        historyToggle.dataset.handlerAttached = 'true';

        historyToggle.addEventListener('click', async () => {
            const expanded = historyToggle.getAttribute('aria-expanded') === 'true';
            if (expanded) {
                historyContainer.hidden = true;
                historyToggle.setAttribute('aria-expanded', 'false');
                historyToggle.innerHTML = '<i class="bi bi-clock-history" aria-hidden="true"></i> View History';
                return;
            }

            historyToggle.disabled = true;
            historyToggle.innerHTML = '<i class="bi bi-arrow-repeat" aria-hidden="true"></i> Loading…';
            try {
                await loadLoginHistory();
                historyContainer.hidden = false;
                historyToggle.setAttribute('aria-expanded', 'true');
                historyToggle.innerHTML = '<i class="bi bi-chevron-up" aria-hidden="true"></i> Hide History';
            } finally {
                historyToggle.disabled = false;
            }
        });
    }

    async function loadActiveDevices() {
        const bodyId = 'ba-profileActiveDevicesBody';

        if (typeof window.apiGet !== 'function') {
            renderSecurityRows(bodyId, [], 5, 'Unavailable');
            return;
        }

        try {
            const response = await window.apiGet('/auth/security/sessions');
            const devices = unwrapArrayResponse(response);
            if (!devices) {
                throw new Error('Active sessions API returned an invalid response.');
            }

            renderSecurityRows(bodyId, devices.map(device => [
                device.deviceName,
                [device.browser, device.operatingSystem].filter(Boolean).join(' / '),
                device.ipAddress,
                formatSecurityDate(device.lastActivityTime),
                formatSecurityDate(device.expiryTime)
            ]), 5, 'No active devices');
        } catch (error) {
            console.warn('Unable to load active account devices.', error);
            renderSecurityRows(bodyId, [], 5, 'Unable to load devices');
        }
    }


    let loginHistoryLoadedBody = null;
    let loginHistoryRequestInProgress = false;

    async function loadLoginHistory() {
        const historyBody = getElement('ba-profileLoginHistoryBody');
        if (!historyBody) return;
        if (loginHistoryLoadedBody === historyBody || loginHistoryRequestInProgress) return;
        if (typeof window.apiGet !== 'function') {
            renderSecurityRows('ba-profileLoginHistoryBody', [], 6, 'Unavailable');
            return;
        }

        loginHistoryRequestInProgress = true;
        try {
            const response = await window.apiGet('/auth/security/login-history');
            const history = unwrapArrayResponse(response);
            if (!history) {
                throw new Error('Login history API returned an invalid response.');
            }
            renderSecurityRows('ba-profileLoginHistoryBody', history.map(entry => [
                formatSecurityDate(entry.loginTime),
                entry.deviceName,
                entry.browser,
                entry.ipAddress,
                entry.status,
                formatSecurityDate(entry.logoutTime)
            ]), 6, 'No login history');
            loginHistoryLoadedBody = historyBody;
        } catch (error) {
            console.warn('Unable to load account login history.', error);
            renderSecurityRows('ba-profileLoginHistoryBody', [], 6, 'Unable to load history');
        } finally {
            loginHistoryRequestInProgress = false;
        }
    }

    async function loadProfile() {
        if (typeof window.apiGet !== 'function') {
            throw new Error(
                'The global apiGet function is not available.'
            );
        }

        const response = await window.apiGet(
            PROFILE_ENDPOINT
        );

        const profile = extractResponseData(response);

        if (!profile) {
            throw new Error(
                'Branch profile response did not contain profile data.'
            );
        }

        state.profile = profile;

        populateView(profile);
        applyEditPermission(profile.canEdit);

        return profile;
    }

    function showProfileLoadError(error) {
        console.error(
            'Unable to load Branch Profile:',
            error
        );

        const root = getElement(
            'ba-branch-profile-view'
        );

        if (root) {
            root.setAttribute('aria-busy', 'false');
        }

        if (typeof showErrorMessage === 'function') {
            showErrorMessage(
                'Unable to load the Branch Profile. Please try again.'
            );
        }
    }

    function initializeBranchProfile() {
        const root = getElement('ba-branch-profile-view');
        if (!root) return Promise.resolve();
        if (initializationPromise) return initializationPromise;

        initializationInProgress = true;
        root.setAttribute('aria-busy', 'true');
        wireEditControls();

        initializationPromise = (async () => {
            try {
                preloadProfileLogo();
                ensureAccountSecurityUI();
                await Promise.all([
                    loadProfile(),
                    // Active Devices is part of view preparation and must finish
                    // before the global dynamic-view loader reveals the page.
                    loadActiveDevices()
                ]);
            } catch (error) {
                showProfileLoadError(error);
            } finally {
                root.setAttribute('aria-busy', 'false');
                initializationInProgress = false;
                initializationPromise = null;
            }
        })();

        return initializationPromise;
    }

    window.BranchProfileModule = {
        init: initializeBranchProfile,
        getState: () => ({
            profile: state.profile,
            canEdit: state.canEdit
        })
    };

    /*
     * Compatibility entry point for existing Branch Admin code that may
     * initialize the profile directly.
     */
    window.initBranchProfile = initializeBranchProfile;

    /*
     * The router mounts the view dynamically. `viewLoaded` is therefore
     * the appropriate lifecycle event instead of DOMContentLoaded.
     */
    document.addEventListener(
        'viewLoaded',
        event => {
            const detail = event?.detail;
            if (
                detail?.view !== 'branch-profile' &&
                detail?.viewName !== 'branch-profile'
            ) return;

            // global.js dispatches this event on document and waits for
            // promises registered through detail.waitUntil() before reveal.
            // here before removing its loading state and showing the view.
            const initialization = initializeBranchProfile();
            if (typeof detail.waitUntil === 'function') {
                detail.waitUntil(initialization);
            }
        }
    );

    /*
     * Also initialize immediately when this module is loaded directly
     * into an already-mounted Branch Profile view.
     */
    if (document.readyState !== 'loading') {
        void initializeBranchProfile();
    } else {
        document.addEventListener(
            'DOMContentLoaded',
            () => {
                void initializeBranchProfile();
            },
            { once: true }
        );
    }
})();

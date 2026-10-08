/* global apiGet, apiMultipart, showLoader, hideLoader, showErrorMessage, showPremiumModal */
// SUPER ADMIN — BRANCH MODULE (Add / View / Edit)

async function populateDynamicLevels(containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;

    try {
        const json = await apiGet('/public/levels');
        const levels = json.data || [];

        container.innerHTML = ''; // Clear loading text

        levels.forEach(level => {
            const label = document.createElement('label');
            label.className = 'cb-container';


            const checkbox = document.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.className = 'level-cb';
            checkbox.value = level.levelId;

            const textNode = document.createTextNode(' ' + level.levelName);
            const span = document.createElement('span');
            span.className = 'checkmark';

            label.appendChild(checkbox);
            label.appendChild(textNode);
            label.appendChild(span);

            container.appendChild(label);
        });
    } catch (e) {
        console.error("Failed to load dynamic levels", e);
        container.textContent = '';
        const errorText = document.createElement('span');
        errorText.className = 'loading-text level-load-error';
        errorText.textContent = 'Failed to load database levels.';
        container.appendChild(errorText);
    }
}

function extractInchargeDetails(viewContainer, tbodyId, nameClass, roleClass, phoneClass) {
    const incharges = [];
    viewContainer.querySelectorAll('#' + tbodyId + ' tr').forEach(row => {
        const name = row.querySelector('.' + nameClass).value.trim();
        const role = row.querySelector('.' + roleClass).value.trim();
        const phone = row.querySelector('.' + phoneClass).value.trim();
        if (name || role || phone) {
            incharges.push({ name, role, phone });
        }
    });
    return incharges;
}

const branchViewNavigationState = {
    reference: null,
    loaderToken: null
};

function setBranchViewReferenceInUrl(reference) {
    const normalizedReference = String(reference || '').trim();
    if (!normalizedReference) return;

    const url = new URL(window.location.href);
    url.searchParams.delete('branchId');
    url.searchParams.set('ref', normalizedReference);

    window.history.replaceState(
        {
            ...(window.history.state || {}),
            branchViewReference: normalizedReference
        },
        '',
        `${url.pathname}${url.search}${url.hash}`
    );
}

async function initBranchesView(routeParams = []) {
    const viewContainer = document.querySelector('#superadmin-branches-view');
    if (!viewContainer) return;

    let tableBody = viewContainer.querySelector('#sa-branchesTableBody');
    if (tableBody) {
        const newTableBody = tableBody.cloneNode(true);
        tableBody.parentNode.replaceChild(newTableBody, tableBody);
        tableBody = newTableBody;
    }

    const tableView = viewContainer.querySelector('#sa-branchTableView');
    const detailView = viewContainer.querySelector('#sa-branchDetailView');
    const validationSummary = viewContainer.querySelector('#edit-branch-validation-summary');
    const validationList = viewContainer.querySelector('#edit-branch-validation-list');

    let currentDetailBranchId = null;
    let currentBranch = null;
    let loadedBranches = [];
    let filteredBranches = [];
    let openingBranchId = null;
    let branchCurrentPage = 1;
    let branchPageSize = 10;
    let editLevelsPromise = null;

    const whatsappLabels = {
        NONE: 'No WhatsApp number',
        PRIMARY: 'Phone Number 1',
        SECONDARY: 'Phone Number 2',
        BOTH: 'Both phone numbers'
    };

    const normalizeText = value => {
        if (value === undefined || value === null) return '';
        return String(value).trim();
    };

    const displayValue = value => {
        const normalized = normalizeText(value);
        return normalized || '-';
    };

    /**
     * @param {number|string} branchId
     * @param {'logo'|'photo'|'document'} fileType
     * @param {number|null} [documentIndex]
     * @returns {string|null}
     */
    const buildPrivateBranchFileUrl = (
        branchId,
        fileType,
        documentIndex = null
    ) => {
        const normalizedBranchId = Number(branchId);

        if (
            !Number.isInteger(normalizedBranchId)
            || normalizedBranchId <= 0
        ) {
            return null;
        }

        const baseUrl =
            `/api/superadmin/branches/${normalizedBranchId}/files`;

        if (fileType === 'logo') {
            return `${baseUrl}/logo`;
        }

        if (fileType === 'photo') {
            return `${baseUrl}/photo`;
        }

        if (fileType === 'qrCode') {
            return `${baseUrl}/qr-code`;
        }

        if (
            fileType === 'document'
            && Number.isInteger(documentIndex)
            && documentIndex >= 0
        ) {
            return `${baseUrl}/documents/${documentIndex}`;
        }

        return null;
    };

    /**
     * @param {Object|null|undefined} branch
     * @returns {string[]}
     */
    const extractStoredDocumentEntries = branch => {
        if (Array.isArray(branch?.govDocumentUrls)) {
            return branch.govDocumentUrls
                .map(normalizeText)
                .filter(Boolean);
        }

        const storedValue =
            normalizeText(branch?.govDocumentUrl);

        if (!storedValue) {
            return [];
        }

        return storedValue
            .split(',')
            .map(normalizeText)
            .filter(Boolean);
    };

    /**
     * Render the existing branch government documents using the same
     * document-card pattern used by Student View. Stored paths are never
     * displayed; the secure branch document endpoint is used for opening.
     * @param {Object} branch
     * @param {number|string} branchId
     */
    const renderBranchGovernmentDocuments = (branch, branchId) => {
        const container = viewContainer.querySelector('#view-govDocuments');
        if (!(container instanceof HTMLElement)) {
            return;
        }

        container.replaceChildren();

        const entries = extractStoredDocumentEntries(branch);
        if (entries.length === 0) {
            const empty = document.createElement('div');
            empty.className = 'branch-government-documents-empty';
            empty.textContent = 'No government documents uploaded.';
            container.appendChild(empty);
            return;
        }

        entries.forEach((entry, index) => {
            const card = document.createElement('article');
            card.className = 'branch-government-document-card';

            const header = document.createElement('div');
            header.className = 'branch-government-document-header';

            const titleBlock = document.createElement('div');
            titleBlock.className = 'branch-government-document-title-block';

            const title = document.createElement('strong');
            title.className = 'branch-government-document-name';
            title.textContent = `Government Document ${index + 1}`;

            const fileName = entry.split(/[\\/]/).pop() || '';
            const extension = fileName.includes('.')
                ? fileName.split('.').pop().toUpperCase()
                : 'FILE';

            const type = document.createElement('span');
            type.className = 'branch-government-document-type';
            type.textContent = extension;

            titleBlock.appendChild(title);
            titleBlock.appendChild(type);
            header.appendChild(titleBlock);
            card.appendChild(header);

            const meta = document.createElement('div');
            meta.className = 'branch-government-document-meta';
            meta.textContent = fileName || 'Government document';
            card.appendChild(meta);

            const actions = document.createElement('div');
            actions.className = 'branch-government-document-actions';

            const documentUrl = buildPrivateBranchFileUrl(branchId, 'document', index);
            const openButton = document.createElement('a');
            openButton.className = 'btn-secondary btn-sm branch-government-document-open';
            openButton.target = '_blank';
            openButton.rel = 'noopener noreferrer';
            openButton.textContent = 'Open Document';
            openButton.href = documentUrl || '#';
            openButton.setAttribute('aria-label', `Open Government Document ${index + 1}`);

            if (!documentUrl) {
                openButton.setAttribute('aria-disabled', 'true');
                openButton.classList.add('disabled');
            }

            actions.appendChild(openButton);
            card.appendChild(actions);
            container.appendChild(card);
        });
    };

    /**
     * @param {{
     *   imageElement: HTMLImageElement|null,
     *   emptyElement?: HTMLElement|null,
     *   hasStoredFile: boolean,
     *   secureUrl: string|null
     * }} options
     */
    const renderPrivateBranchImage = ({
        imageElement,
        emptyElement = null,
        hasStoredFile,
        secureUrl
    }) => {
        if (!imageElement) return Promise.resolve();

        if (!hasStoredFile || !secureUrl) {
            imageElement.removeAttribute('src');
            imageElement.classList.add('hidden');
            emptyElement?.classList.remove('hidden');
            return Promise.resolve();
        }

        imageElement.removeAttribute('src');
        imageElement.classList.add('hidden');
        emptyElement?.classList.remove('hidden');

        return fetch(secureUrl, {
            method: 'GET',
            credentials: 'include',
            cache: 'no-store'
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`Private branch file request failed: ${response.status}`);
                }
                return response.blob();
            })
            .then(blob => new Promise((resolve, reject) => {
                const reader = new FileReader();
                reader.onload = () => resolve(reader.result);
                reader.onerror = () => reject(new Error('Unable to read private branch image.'));
                reader.readAsDataURL(blob);
            }))
            .then(dataUrl => {
                imageElement.src = dataUrl;
                imageElement.classList.remove('hidden');
                emptyElement?.classList.add('hidden');
            })
            .catch(error => {
                console.warn('Unable to load private branch image:', secureUrl, error);
                imageElement.removeAttribute('src');
                imageElement.classList.add('hidden');
                emptyElement?.classList.remove('hidden');
            });
    };

    const setText = (selector, value) => {
        const element = viewContainer.querySelector(selector);
        if (element) element.textContent = displayValue(value);
    };

    const setInput = (selector, value) => {
        const element = viewContainer.querySelector(selector);
        if (!element) return;

        if (element.type === 'checkbox') {
            element.checked = value === true || value === 1 || value === '1' || value === 'true';
            return;
        }

        element.value = value ?? '';
    };

    const getInputValue = selector =>
        viewContainer.querySelector(selector)?.value?.trim() || '';

    const getEmailEnabled = () =>
        Boolean(viewContainer.querySelector('#edit-emailEnabled')?.checked);

    const wireEditFileTriggers = () => {
        const triggerMap = [
            { inputId: 'edit-branchLogo', labels: ['change logo'] },
            { inputId: 'edit-qrCode', labels: ['replace qr', 'replace qr code'] }
        ];

        triggerMap.forEach(({ inputId, labels }) => {
            const input = viewContainer.querySelector(`#${inputId}`);
            if (!(input instanceof HTMLInputElement)) return;

            viewContainer.querySelectorAll('button, a, label').forEach(control => {
                if (control.dataset.erpFileTrigger === inputId) return;

                const text = normalizeText(control.textContent).toLowerCase();
                if (!labels.some(label => text === label || text.includes(label))) return;

                control.dataset.erpFileTrigger = inputId;

                // Native labels already open their associated input.
                if (control.tagName.toLowerCase() === 'label') return;

                control.addEventListener('click', event => {
                    event.preventDefault();
                    input.click();
                });
            });
        });
    };

    const ensureEditLevelsLoaded = () => {
        if (viewContainer.querySelector('#edit-branchLevels .level-cb')) {
            return Promise.resolve();
        }

        if (!editLevelsPromise) {
            editLevelsPromise = populateDynamicLevels('edit-branchLevels')
                .catch(error => {
                    editLevelsPromise = null;
                    throw error;
                });
        }

        return editLevelsPromise;
    };

    const buildFullAddress = branch => {
        const parts = [
            branch?.addressLine1,
            branch?.addressLine2,
            branch?.poBox,
            branch?.locality,
            branch?.city,
            branch?.district,
            branch?.region,
            branch?.country,
            branch?.postalCode
        ]
            .map(normalizeText)
            .filter(Boolean);

        if (parts.length > 0) return parts.join(', ');
        return normalizeText(branch?.branchLocation) || '-';
    };

    const clearEditValidation = () => {
        validationSummary?.classList.add('hidden');
        if (validationList) validationList.textContent = '';

        viewContainer
            .querySelectorAll('.branch-field-invalid')
            .forEach(element => element.classList.remove('branch-field-invalid'));
    };

    const showEditValidation = errors => {
        clearEditValidation();

        if (!Array.isArray(errors) || errors.length === 0) return;

        errors.forEach(error => {
            const element = viewContainer.querySelector(error.selector);
            element?.classList.add('branch-field-invalid');

            if (validationList) {
                const item = document.createElement('li');
                item.textContent = error.message;
                validationList.appendChild(item);
            }
        });

        validationSummary?.classList.remove('hidden');
        validationSummary?.scrollIntoView({ behavior: 'smooth', block: 'center' });

        const firstField = viewContainer.querySelector(errors[0].selector);
        if (firstField && typeof firstField.focus === 'function') {
            firstField.focus({ preventScroll: true });
        }
    };

    const validateEditForm = () => {
        const errors = [];

        const requiredFields = [
            ['#edit-branchName', 'School name is required.'],
            ['#edit-schoolCode', 'School code is required.'],
            ['#edit-foundationDate', 'Foundation date is required.'],
            ['#edit-branchLocation', 'Short location is required.'],
            ['#edit-addressLine1', 'Campus or Address Line 1 is required.'],
            ['#edit-country', 'Country is required.'],
            ['#edit-primaryPhone', 'Phone Number 1 is required.'],
            ['#edit-branchEmail', 'Branch email is required.']
        ];

        requiredFields.forEach(([selector, message]) => {
            if (!getInputValue(selector)) errors.push({ selector, message });
        });

        const branchEmail = getInputValue('#edit-branchEmail');
        const replyTo = getInputValue('#edit-emailReplyTo');
        const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

        if (branchEmail && !emailPattern.test(branchEmail)) {
            errors.push({ selector: '#edit-branchEmail', message: 'Enter a valid branch email address.' });
        }

        if (replyTo && !emailPattern.test(replyTo)) {
            errors.push({ selector: '#edit-emailReplyTo', message: 'Enter a valid Reply-To email address.' });
        }

        const primaryPhone = getInputValue('#edit-primaryPhone');
        const secondaryPhone = getInputValue('#edit-secondaryPhone');
        const whatsappPhone = getInputValue('#edit-whatsappPhone') || 'NONE';

        if (primaryPhone && secondaryPhone && primaryPhone === secondaryPhone) {
            errors.push({ selector: '#edit-secondaryPhone', message: 'Phone Number 2 must be different from Phone Number 1.' });
        }

        if (whatsappPhone === 'SECONDARY' && !secondaryPhone) {
            errors.push({ selector: '#edit-secondaryPhone', message: 'Enter Phone Number 2 because it is selected for WhatsApp.' });
        }

        if (whatsappPhone === 'BOTH' && !secondaryPhone) {
            errors.push({ selector: '#edit-secondaryPhone', message: 'Enter both phone numbers when both are selected for WhatsApp.' });
        }

        const airtelPayNumber = getInputValue('#edit-airtelPayNumber');
        if (airtelPayNumber && !/^[+0-9()\s-]+$/.test(airtelPayNumber)) {
            errors.push({
                selector: '#edit-airtelPayNumber',
                message: 'Airtel Pay Number contains unsupported characters.'
            });
        }

        const selectedLevels = viewContainer.querySelectorAll('#edit-branchLevels .level-cb:checked');
        if (selectedLevels.length === 0) {
            errors.push({ selector: '#edit-branchLevels', message: 'Select at least one education level.' });
        }

        const inchargeRows = Array.from(viewContainer.querySelectorAll('#edit-incharge-tbody tr'));
        inchargeRows.forEach((row, index) => {
            const name = row.querySelector('.inc-name')?.value?.trim() || '';
            const role = row.querySelector('.inc-role')?.value?.trim() || '';
            const phone = row.querySelector('.inc-phone')?.value?.trim() || '';
            const hasAnyValue = Boolean(name || role || phone);

            if (hasAnyValue && (!name || !role || !phone)) {
                errors.push({
                    selector: '#edit-inchargeDetails-container',
                    message: `Incharge row ${index + 1} requires name, role and phone number.`
                });
            }
        });

        const logoFile = viewContainer.querySelector('#edit-branchLogo')?.files?.[0] || null;
        if (logoFile) {
            const allowedLogoTypes = ['image/jpeg', 'image/png'];
            const maxLogoSize = 500 * 1024;

            if (!allowedLogoTypes.includes(logoFile.type)) {
                errors.push({ selector: '#edit-branchLogo', message: 'Branch logo must be JPG or PNG.' });
            } else if (logoFile.size > maxLogoSize) {
                errors.push({ selector: '#edit-branchLogo', message: 'Branch logo must not exceed 500 KB.' });
            }
        }

        const photoFile = viewContainer.querySelector('#edit-schoolPhoto')?.files?.[0] || null;
        if (photoFile) {
            const allowedPhotoTypes = ['image/jpeg', 'image/png'];
            const maxPhotoSize = 100 * 1024;

            if (!allowedPhotoTypes.includes(photoFile.type)) {
                errors.push({ selector: '#edit-schoolPhoto', message: 'School photo must be JPG or PNG.' });
            } else if (photoFile.size > maxPhotoSize) {
                errors.push({ selector: '#edit-schoolPhoto', message: 'School photo must not exceed 100 KB.' });
            }
        }

        const documentFile = viewContainer.querySelector('#edit-govDocument')?.files?.[0] || null;
        if (documentFile) {
            const allowedDocumentTypes = ['image/jpeg', 'image/png', 'application/pdf'];
            const maxDocumentSize = documentFile.type === 'application/pdf'
                ? 2 * 1024 * 1024
                : 100 * 1024;

            if (!allowedDocumentTypes.includes(documentFile.type)) {
                errors.push({ selector: '#edit-govDocument', message: 'Government document must be PDF, JPG or PNG.' });
            } else if (documentFile.size > maxDocumentSize) {
                errors.push({
                    selector: '#edit-govDocument',
                    message: documentFile.type === 'application/pdf'
                        ? 'Government PDF must not exceed 2 MB.'
                        : 'Government image must not exceed 100 KB.'
                });
            }
        }

        return errors;
    };

    function addEditInchargeRow(name = '', role = '', phone = '') {
        const template = viewContainer.querySelector('#incharge-edit-row-template');
        const tbody = viewContainer.querySelector('#edit-incharge-tbody');
        if (!template || !tbody) return;

        const clone = template.content.cloneNode(true);
        const nameInput = clone.querySelector('.inc-name');
        const roleInput = clone.querySelector('.inc-role');
        const phoneInput = clone.querySelector('.inc-phone');
        const removeButton = clone.querySelector('.remove-inc-btn');

        if (nameInput) nameInput.value = name;
        if (roleInput) roleInput.value = role;
        if (phoneInput) phoneInput.value = phone;

        removeButton?.addEventListener('click', event => {
            event.currentTarget.closest('tr')?.remove();
        });

        tbody.appendChild(clone);
    }

    const addPersonBtn = viewContainer.querySelector('#edit-addInchargeRowBtn');
    if (addPersonBtn) {
        const newButton = addPersonBtn.cloneNode(true);
        addPersonBtn.parentNode.replaceChild(newButton, addPersonBtn);
        newButton.addEventListener('click', () => addEditInchargeRow());
    }

    const branchSearchInput = viewContainer.querySelector('#sa-branch-search');
    const branchLevelFilter = viewContainer.querySelector('#sa-branch-level-filter');
    const branchStatusFilter = viewContainer.querySelector('#sa-branch-status-filter');
    const branchSearchButton = viewContainer.querySelector('#sa-branch-search-btn');
    const branchResetButton = viewContainer.querySelector('#sa-branch-reset-btn');
    const branchPageSizeSelect = viewContainer.querySelector('#sa-branch-page-size');
    const branchPageInfo = viewContainer.querySelector('#sa-branch-page-info');
    const branchPrevButton = viewContainer.querySelector('#sa-branch-prev');
    const branchNextButton = viewContainer.querySelector('#sa-branch-next');

    const branchMatchesFilters = branch => {
        const search = normalizeText(branchSearchInput?.value).toLowerCase();
        const levelId = normalizeText(branchLevelFilter?.value);
        const status = normalizeText(branchStatusFilter?.value);

        const name = normalizeText(branch.branchName).toLowerCase();
        const code = normalizeText(branch.schoolCode).toLowerCase();
        const matchesSearch = !search || name.includes(search) || code.includes(search);

        const levels = Array.isArray(branch.levels) ? branch.levels : [];
        const matchesLevel = !levelId || levels.some(level => String(level?.levelId ?? '') === levelId);
        const isActive = branch.isActive === true || branch.isActive === 1;
        const matchesStatus = !status || (status === 'ACTIVE' ? isActive : !isActive);

        return matchesSearch && matchesLevel && matchesStatus;
    };

    const updateBranchPagination = () => {
        const total = filteredBranches.length;
        const totalPages = Math.max(1, Math.ceil(total / branchPageSize));
        branchCurrentPage = Math.min(Math.max(branchCurrentPage, 1), totalPages);

        if (branchPageInfo) {
            branchPageInfo.textContent = `Showing page ${branchCurrentPage} of ${totalPages}`;
        }
        if (branchPrevButton) branchPrevButton.disabled = branchCurrentPage <= 1;
        if (branchNextButton) branchNextButton.disabled = branchCurrentPage >= totalPages;
    };

    const preloadBranchLogos = async branches => {
        const logoLoads = branches.map(branch => {
            const branchId = Number(branch?.branchId);
            const logoUrl = buildPrivateBranchFileUrl(branchId, 'logo');

            if (!logoUrl) return Promise.resolve();

            return new Promise(resolve => {
                const image = new Image();
                let settled = false;

                const finish = () => {
                    if (settled) return;
                    settled = true;
                    image.onload = null;
                    image.onerror = null;
                    resolve();
                };

                image.onload = finish;
                image.onerror = finish;
                image.src = logoUrl;

                // Never hold the table indefinitely because of a slow/missing logo.
                window.setTimeout(finish, 2500);
            });
        });

        await Promise.all(logoLoads);
    };

    const renderBranchPage = async () => {
        if (!tableBody) return;

        const startIndex = (branchCurrentPage - 1) * branchPageSize;
        const pageBranches = filteredBranches.slice(startIndex, startIndex + branchPageSize);
        tableBody.textContent = '';

        if (pageBranches.length === 0) {
            renderEmptyTableMessage(tableBody, 8, 'No branches found.');
            updateBranchPagination();
            return;
        }

        // Preload all logos for the visible page before inserting rows.
        // This prevents the table from visually changing as individual logos arrive.
        const pageKey = pageBranches.map(branch => String(branch?.branchId)).join('|');
        await preloadBranchLogos(pageBranches);

        // The filters/page may have changed while logos were loading.
        const currentStartIndex = (branchCurrentPage - 1) * branchPageSize;
        const currentPageBranches = filteredBranches.slice(
            currentStartIndex,
            currentStartIndex + branchPageSize
        );
        const currentPageKey = currentPageBranches
            .map(branch => String(branch?.branchId))
            .join('|');

        if (currentPageKey !== pageKey) {
            return renderBranchPage();
        }

        currentPageBranches.forEach(branch => {
            const template = viewContainer.querySelector('#branch-row-template');
            if (!template) return;

            const clone = template.content.cloneNode(true);
            const branchId = Number(branch.branchId);
            const levels = Array.isArray(branch.levels) ? branch.levels : [];

            const row = clone.querySelector('tr');
            if (row) row.dataset.id = String(branchId);

            const logoImage = clone.querySelector('.branch-list-logo');
            const logoFallback = clone.querySelector('.branch-list-logo-fallback');
            const logoUrl = buildPrivateBranchFileUrl(branchId, 'logo');

            if (logoImage && logoFallback && logoUrl) {
                logoImage.classList.add('hidden');
                logoFallback.classList.remove('hidden');
                logoImage.onload = () => {
                    logoImage.classList.remove('hidden');
                    logoFallback.classList.add('hidden');
                };
                logoImage.onerror = () => {
                    logoImage.removeAttribute('src');
                    logoImage.classList.add('hidden');
                    logoFallback.classList.remove('hidden');
                };
                logoImage.src = logoUrl;
            }

            clone.querySelector('.col-code').textContent = displayValue(branch.schoolCode);
            clone.querySelector('.col-name strong').textContent = displayValue(branch.branchName);
            const contactCell = clone.querySelector('.col-contact');
            if (contactCell) {
                contactCell.textContent = '';

                const primaryPhone = normalizeText(
                    branch.primaryPhone ?? branch.contactDetails
                );
                const secondaryPhone = normalizeText(branch.secondaryPhone);

                if (primaryPhone) {
                    const primary = document.createElement('div');
                    primary.className = 'branch-contact-line';
                    primary.textContent = primaryPhone;
                    contactCell.appendChild(primary);
                }

                if (secondaryPhone) {
                    const secondary = document.createElement('div');
                    secondary.className = 'branch-contact-line';
                    secondary.textContent = secondaryPhone;
                    contactCell.appendChild(secondary);
                }

                if (!primaryPhone && !secondaryPhone) {
                    contactCell.textContent = '-';
                }
            }
            clone.querySelector('.col-location').textContent = displayValue(
                branch.branchLocation ?? branch.locality ?? branch.city ?? branch.district
            );
            clone.querySelector('.col-levels').textContent = levels.length > 0
                ? levels.map(level => level?.levelName).filter(Boolean).join(', ')
                : 'N/A';

            const toggle = clone.querySelector('.status-toggle');
            if (toggle) {
                toggle.checked = branch.isActive === true || branch.isActive === 1;
                toggle.dataset.id = String(branchId);
            }

            const viewButton = clone.querySelector('.view-more-btn');
            if (viewButton) viewButton.dataset.id = String(branchId);

            tableBody.appendChild(clone);
        });

        updateBranchPagination();
    };

    const applyBranchFilters = async () => {
        filteredBranches = loadedBranches.filter(branchMatchesFilters);
        branchCurrentPage = 1;
        await renderBranchPage();
    };

    const populateBranchLevelFilter = () => {
        if (!branchLevelFilter) return;

        const selected = branchLevelFilter.value;
        const levels = new Map();
        loadedBranches.forEach(branch => {
            (Array.isArray(branch.levels) ? branch.levels : []).forEach(level => {
                if (level?.levelId != null && level?.levelName) {
                    levels.set(String(level.levelId), String(level.levelName));
                }
            });
        });

        branchLevelFilter.innerHTML = '<option value="">All Levels</option>';
        [...levels.entries()]
            .sort((a, b) => a[1].localeCompare(b[1]))
            .forEach(([id, name]) => {
                const option = document.createElement('option');
                option.value = id;
                option.textContent = name;
                branchLevelFilter.appendChild(option);
            });

        if ([...branchLevelFilter.options].some(option => option.value === selected)) {
            branchLevelFilter.value = selected;
        }
    };

    async function loadBranches() {
        const loaderToken = showLoader('Loading branches...');

        if (tableBody) {
            renderFetchingMessage(tableBody, 8, 'Fetching branches...');
        }

        try {
            const response = await apiGet('/superadmin/branches');
            loadedBranches = Array.isArray(response?.data) ? response.data : [];
            populateBranchLevelFilter();
            await applyBranchFilters();
        } catch (error) {
            console.error(error);
            loadedBranches = [];
            filteredBranches = [];

            if (tableBody) {
                renderEmptyTableMessage(tableBody, 8, 'Failed to load branches.');
            }
            updateBranchPagination();
        } finally {
            hideLoader(loaderToken);
        }
    }

    branchSearchButton?.addEventListener('click', () => {
        void applyBranchFilters();
    });
    branchResetButton?.addEventListener('click', () => {
        if (branchSearchInput) branchSearchInput.value = '';
        if (branchLevelFilter) branchLevelFilter.value = '';
        if (branchStatusFilter) branchStatusFilter.value = '';
        void applyBranchFilters();
    });

    branchSearchInput?.addEventListener('keydown', event => {
        if (event.key === 'Enter') {
            event.preventDefault();
            void applyBranchFilters();
        }
    });

    branchLevelFilter?.addEventListener('change', () => {
        void applyBranchFilters();
    });
    branchStatusFilter?.addEventListener('change', () => {
        void applyBranchFilters();
    });

    branchPageSizeSelect?.addEventListener('change', () => {
        const value = Number.parseInt(branchPageSizeSelect.value, 10);
        branchPageSize = Number.isInteger(value) && value > 0 ? value : 10;
        branchCurrentPage = 1;
        void renderBranchPage();
    });

    branchPrevButton?.addEventListener('click', () => {
        if (branchCurrentPage <= 1) return;
        branchCurrentPage -= 1;
        void renderBranchPage();
    });

    branchNextButton?.addEventListener('click', () => {
        const totalPages = Math.max(1, Math.ceil(filteredBranches.length / branchPageSize));
        if (branchCurrentPage >= totalPages) return;
        branchCurrentPage += 1;
        void renderBranchPage();
    });

    tableBody?.addEventListener('click', event => {
        const viewButton = event.target.closest('.view-more-btn');
        const toggleButton = event.target.closest('.status-toggle');
        const row = event.target.closest('tr[data-id]');

        if (viewButton) {
            const id = Number.parseInt(viewButton.dataset.id, 10);
            if (Number.isInteger(id) && id > 0) {
                void navigateToBranchView(id);
            }
            return;
        }

        // Keep the existing status-toggle behaviour.
        if (toggleButton) {
            event.preventDefault();
            const id = Number.parseInt(toggleButton.dataset.id, 10);
            if (!Number.isInteger(id) || id <= 0) return;

            showPremiumModal({
                title: 'Confirm Action',
                type: 'warning',
                contentText: 'Are you sure you want to change the active status of this branch?',
                confirmText: 'Yes, Change Status',
                cancelText: 'Cancel',
                onConfirm: async modal => {
                    await modal.close();
                    const loaderToken = showLoader('Updating branch status...');

                    try {
                        await apiPut(`/superadmin/branches/${id}/toggle`, {});
                        toggleButton.checked = !toggleButton.checked;
                        showSuccessMessage('Branch status updated successfully.');
                    } catch (error) {
                        console.error(error);
                        showErrorMessage('Failed to update branch status.');
                    } finally {
                        hideLoader(loaderToken);
                    }
                }
            });
            return;
        }

        // Do not turn clicks on other interactive controls into row navigation.
        if (event.target.closest('button, input, select, textarea, label, a')) return;

        // Clicking anywhere else on the row opens the existing Branch View.
        if (!row) return;

        const rowBranchId = Number.parseInt(row.dataset.id, 10);
        if (Number.isInteger(rowBranchId) && rowBranchId > 0) {
            void navigateToBranchView(rowBranchId);
        }
    });

    const navigateToBranchView = async id => {
        const normalizedId = Number.parseInt(id, 10);
        if (!Number.isInteger(normalizedId) || normalizedId <= 0) return;

        const loaderToken = showLoader('Loading branch details...');
        let branchViewReference = '';

        try {
            const response = await apiGet(
                `/superadmin/branches/${normalizedId}/view-reference`
            );
            branchViewReference = String(response?.data || '').trim();

            if (!branchViewReference) {
                showErrorMessage(
                    'Unable to open the Branch View. Please try again.'
                );
                return;
            }

            // Pass the opaque reference through the navigation lifecycle.
            // The router may rewrite the URL while switching views, so the
            // Branch View initializer restores this exact reference after
            // the view is mounted.
            branchViewNavigationState.reference = branchViewReference;
            branchViewNavigationState.loaderToken = loaderToken;

            const navigated = await window.erpNavigate({
                role: 'superadmin',
                view: 'branches',
                routeParams: [String(normalizedId)],
                title: 'Branch Details',
                historyMode: 'push'
            });

            if (!navigated) return;

            // Final URL synchronization after navigation completes. The
            // initializer also performs this before resolving the reference,
            // so refresh always has the opaque reference available.
            setBranchViewReferenceInUrl(branchViewReference);
        } catch (error) {
            console.error('Unable to open Branch View:', error);
            showErrorMessage(
                'Unable to open the Branch View. Please try again.'
            );
        } finally {
            if (branchViewNavigationState.loaderToken === loaderToken) {
                branchViewNavigationState.loaderToken = null;
            }
            branchViewNavigationState.reference = null;
            hideLoader(loaderToken);
        }
    };

    let backButton = viewContainer.querySelector('#sa-backToTableBtn');
    if (backButton) {
        const newButton = backButton.cloneNode(true);
        backButton.parentNode.replaceChild(newButton, backButton);
        backButton = newButton;

        backButton.addEventListener('click', async () => {
            clearEditValidation();
            const url = new URL(window.location.href);
            url.searchParams.delete('branchId');
            url.searchParams.delete('ref');
            window.history.replaceState(
                window.history.state || {},
                '',
                `${url.pathname}${url.search}${url.hash}`
            );

            await window.erpNavigate({
                role: 'superadmin',
                view: 'branches',
                routeParams: [],
                title: 'Manage Branches',
                historyMode: 'push'
            });
        });
    }

    async function openViewMore(
        id,
        {
            forceReload = false,
            showBusy = true
        } = {}
    ) {
        if (openingBranchId === id) return;

        openingBranchId = id;
        currentDetailBranchId = id;
        const loaderToken = showBusy
            ? showLoader('Loading branch details...')
            : null;

        try {
            const branchesRequest =
                forceReload || loadedBranches.length === 0
                    ? apiGet('/superadmin/branches')
                    : Promise.resolve({ data: loadedBranches });
            const statsRequest = apiGet(
                `/superadmin/branches/${id}/stats`
            ).catch(() => null);
            const logsRequest = apiGet(
                `/superadmin/branches/${id}/logs`
            ).catch(() => null);

            const response = await branchesRequest;
            const branches =
                Array.isArray(response?.data)
                    ? response.data
                    : [];
            loadedBranches = branches;
            const branch = branches.find(item => Number(item.branchId) === Number(id));

            if (!branch) {
                showErrorMessage('Branch not found.');
                return;
            }

            currentBranch = branch;

            setText('#detail-schoolNameHeader', branch.branchName);
            setText('#view-branchName', branch.branchName);
            setText('#view-schoolCode', branch.schoolCode);
            setText('#view-adminUsername', `${normalizeText(branch.schoolCode).toLowerCase()}@montfort.ug`);
            setText(
                '#view-branchType',
                Array.isArray(branch.levels) && branch.levels.length > 0
                    ? branch.levels.map(level => level.levelName).filter(Boolean).join(', ')
                    : 'N/A'
            );
            setText(
            '#view-foundationDate',
            branch.foundationDate && window.erpDate
                ? window.erpDate.formatDate(branch.foundationDate, '-')
                : (branch.foundationDate || '-')
        );

            setText('#view-branchLocation-field', branch.branchLocation);
            setText('#view-addressLine1', branch.addressLine1);
            setText('#view-addressLine2', branch.addressLine2);
            setText('#view-poBox', branch.poBox);
            setText('#view-locality', branch.locality);
            setText('#view-city', branch.city);
            setText('#view-district', branch.district);
            setText('#view-region', branch.region);
            setText('#view-country', branch.country || 'Uganda');
            setText('#view-postalCode', branch.postalCode);
            setText('#view-fullAddress', buildFullAddress(branch));

            setText('#view-primaryPhone', branch.primaryPhone || branch.contactDetails);
            setText('#view-secondaryPhone', branch.secondaryPhone);
            setText('#view-whatsappPhone', whatsappLabels[branch.whatsappPhone || 'NONE']);
            setText('#view-branchEmail', branch.branchEmail);
            setText('#view-emailFromName', branch.emailFromName || branch.branchName);
            setText('#view-emailReplyTo', branch.emailReplyTo || branch.branchEmail);
            setText(
                '#view-emailEnabled',
                branch.emailEnabled === false || branch.emailEnabled === 0 ? 'Disabled' : 'Enabled'
            );
            setText('#view-contactDetails', branch.contactDetails);
            setText('#view-airtelPayNumber', branch.airtelPayNumber);
            setText('#view-airtelPayName', branch.airtelPayName);

            renderBranchGovernmentDocuments(branch, id);

            const emailStatus = viewContainer.querySelector('#view-emailEnabled');
            if (emailStatus) {
                const enabled = !(branch.emailEnabled === false || branch.emailEnabled === 0);
                emailStatus.classList.toggle('branch-status-enabled', enabled);
                emailStatus.classList.toggle('branch-status-disabled', !enabled);
            }

            const inchargeView = viewContainer.querySelector('#view-inchargeDetails');
            const inchargeEditTbody = viewContainer.querySelector('#edit-incharge-tbody');
            if (inchargeView) inchargeView.textContent = '';
            if (inchargeEditTbody) inchargeEditTbody.textContent = '';

            let incharges = [];
            const rawIncharges = branch.inchargeDetails;

            if (Array.isArray(rawIncharges)) {
                incharges = rawIncharges;
            } else if (rawIncharges && rawIncharges !== 'null' && rawIncharges !== '[object Object]') {
                try {
                    const parsed = JSON.parse(rawIncharges);
                    incharges = Array.isArray(parsed) ? parsed : [];
                } catch (error) {
                    console.warn('Corrupted legacy incharge data ignored:', rawIncharges, error);
                }
            }

            if (incharges.length === 0) {
                const emptyMessage = document.createElement('div');
                emptyMessage.className = 'view-incharge-empty';
                emptyMessage.textContent = 'No incharge details provided.';
                inchargeView?.appendChild(emptyMessage);
            } else {
                const viewTemplate = viewContainer.querySelector('#incharge-view-table-template');
                if (viewTemplate && inchargeView) {
                    const tableClone = viewTemplate.content.cloneNode(true);
                    const tbody = tableClone.querySelector('.incharge-view-tbody');

                    incharges.forEach(incharge => {
                        const row = document.createElement('tr');
                        [incharge.name, incharge.role, incharge.phone].forEach(value => {
                            const cell = document.createElement('td');
                            cell.textContent = displayValue(value);
                            row.appendChild(cell);
                        });
                        tbody.appendChild(row);
                        addEditInchargeRow(incharge.name || '', incharge.role || '', incharge.phone || '');
                    });

                    inchargeView.appendChild(tableClone);
                }
            }

            /** @type {HTMLImageElement|null} */
            const logoImage = viewContainer.querySelector('#view-branchLogo');
            const logoEmpty = viewContainer.querySelector('#view-branchLogoEmpty');
            const hasStoredLogo = Boolean(normalizeText(branch.branchLogoUrl || branch.logoUrl));

            const photoImage = viewContainer.querySelector('#view-schoolPhoto');
            const hasStoredPhoto = Boolean(normalizeText(branch.schoolPhotoUrl));

            const qrImage = viewContainer.querySelector('#view-qrCode');
            const qrEmpty = viewContainer.querySelector('#view-qrCodeEmpty');
            const hasStoredQr = Boolean(normalizeText(branch.qrCodeUrl));

            // Start all mandatory branch assets together. Nothing is revealed until
            // these requests settle, preventing logo/photo/QR from appearing one-by-one.
            const assetRequests = [
                renderPrivateBranchImage({
                    imageElement: logoImage,
                    emptyElement: logoEmpty,
                    hasStoredFile: hasStoredLogo,
                    secureUrl: buildPrivateBranchFileUrl(id, 'logo')
                }),
                renderPrivateBranchImage({
                    imageElement: photoImage,
                    hasStoredFile: hasStoredPhoto,
                    secureUrl: buildPrivateBranchFileUrl(id, 'photo')
                }),
                renderPrivateBranchImage({
                    imageElement: qrImage,
                    emptyElement: qrEmpty,
                    hasStoredFile: hasStoredQr,
                    secureUrl: buildPrivateBranchFileUrl(id, 'qrCode')
                })
            ];

            try {
                const [statsResponse, logResponse] =
                    await Promise.all([
                        statsRequest,
                        logsRequest,
                        ...assetRequests
                    ]).then(results => [results[0], results[1]]);

                const stats = statsResponse?.data || null;

                setText('#view-statStudents', stats ? stats.students || 0 : 'N/A');
                setText('#view-statStaff', stats ? stats.staff || 0 : 'N/A');
                setText('#view-statAttendance', stats ? `${stats.attendance || 0}%` : 'N/A');

                const logBody = viewContainer.querySelector('#view-auditLogTbody');
                const logTemplate = viewContainer.querySelector('#audit-log-row-template');

                if (logBody && logTemplate) {
                    logBody.textContent = '';
                    const logs = Array.isArray(logResponse?.data) ? logResponse.data : [];

                    if (logs.length === 0) {
                        const row = document.createElement('tr');
                        const cell = document.createElement('td');
                        cell.colSpan = 3;
                        cell.className = 'branch-audit-empty';
                        cell.textContent = 'No recent activity found.';
                        row.appendChild(cell);
                        logBody.appendChild(row);
                    } else {
                        logs.forEach(log => {
                            const clone = logTemplate.content.cloneNode(true);
                            clone.querySelector('.log-date').textContent = log.date && window.erpDate ? window.erpDate.formatDateTime(log.date, displayValue(log.date)) : displayValue(log.date);
                            clone.querySelector('.log-user').textContent = displayValue(log.user);
                            clone.querySelector('.log-action').textContent = displayValue(log.action);
                            logBody.appendChild(clone);
                        });
                    }
                }
            } catch (error) {
                console.error('Branch stats, logs or assets failed:', error);
            }

            // Reveal the complete page only after all required branch data/assets settle.
            tableView?.classList.add('hidden');
            detailView?.classList.remove('hidden');
            resetEditMode();
        } catch (error) {
            console.error(error);
            showErrorMessage(error.message || 'Failed to fetch branch details.');
        } finally {
            if (openingBranchId === id) {
                openingBranchId = null;
            }

            if (loaderToken) hideLoader(loaderToken);
        }
    }

    let editButton = viewContainer.querySelector('#sa-editBranchBtn');
    if (editButton) {
        const newButton = editButton.cloneNode(true);
        editButton.parentNode.replaceChild(newButton, editButton);
        editButton = newButton;
    }

    let saveButton = viewContainer.querySelector('#sa-saveBranchBtn');
    if (saveButton) {
        const newButton = saveButton.cloneNode(true);
        saveButton.parentNode.replaceChild(newButton, saveButton);
        saveButton = newButton;
    }

    let cancelEditButton = viewContainer.querySelector('#sa-cancelEditBtn');
    if (cancelEditButton) {
        const newButton = cancelEditButton.cloneNode(true);
        cancelEditButton.parentNode.replaceChild(newButton, cancelEditButton);
        cancelEditButton = newButton;
    }

    editButton?.addEventListener('click', async () => {
        if (!currentBranch) return;

        clearEditValidation();

        // Keep only the editable Incharge table visible in Edit mode.
        // The read-only table must never appear at the same time.
        const inchargeView = viewContainer.querySelector('#view-inchargeDetails');
        const inchargeEdit = viewContainer.querySelector('#edit-inchargeDetails-container');
        inchargeView?.classList.add('hidden');
        inchargeEdit?.classList.remove('hidden');

        viewContainer
            .querySelectorAll('.detail-text:not(.readonly-always)')
            .forEach(element => element.classList.add('hidden'));

        viewContainer
            .querySelectorAll('.detail-input:not([type=\"file\"])')
            .forEach(element => element.classList.remove('hidden'));

        // File inputs stay visually hidden. Their existing labels/buttons
        // remain the single upload control for logo, photo, document and QR.
        viewContainer
            .querySelectorAll('.detail-input[type=\"file\"]')
            .forEach(element => element.classList.add('hidden'));

        setInput('#edit-branchName', currentBranch.branchName);
        setInput('#edit-schoolCode', currentBranch.schoolCode);
        setInput('#edit-foundationDate', currentBranch.foundationDate);
        setInput('#edit-branchLocation', currentBranch.branchLocation);
        setInput('#edit-addressLine1', currentBranch.addressLine1);
        setInput('#edit-addressLine2', currentBranch.addressLine2);
        setInput('#edit-poBox', currentBranch.poBox);
        setInput('#edit-locality', currentBranch.locality);
        setInput('#edit-city', currentBranch.city);
        setInput('#edit-district', currentBranch.district);
        setInput('#edit-region', currentBranch.region);
        setInput('#edit-country', currentBranch.country || 'Uganda');
        setInput('#edit-postalCode', currentBranch.postalCode);
        setInput('#edit-primaryPhone', currentBranch.primaryPhone || currentBranch.contactDetails);
        setInput('#edit-secondaryPhone', currentBranch.secondaryPhone);
        setInput('#edit-whatsappPhone', currentBranch.whatsappPhone || 'NONE');
        setInput('#edit-branchEmail', currentBranch.branchEmail);
        setInput('#edit-emailFromName', currentBranch.emailFromName || currentBranch.branchName);
        setInput('#edit-emailReplyTo', currentBranch.emailReplyTo || currentBranch.branchEmail);
        setInput('#edit-airtelPayNumber', currentBranch.airtelPayNumber);
        setInput('#edit-airtelPayName', currentBranch.airtelPayName);
        setInput(
            '#edit-emailEnabled',
            !(currentBranch.emailEnabled === false || currentBranch.emailEnabled === 0)
        );

        // Reveal all Edit-only media controls immediately.
        // Do not wait for the asynchronous level lookup before showing them.
        viewContainer
            .querySelector('.branch-modern-logo-editor')
            ?.classList.remove('hidden');
        viewContainer
            .querySelector('.branch-media-editor')
            ?.classList.remove('hidden');
        viewContainer
            .querySelector('.branch-government-documents-editor')
            ?.classList.remove('hidden');
        viewContainer
            .querySelector('.branch-payment-qr-editor')
            ?.classList.remove('hidden');

        await ensureEditLevelsLoaded();

        const levelIds = Array.isArray(currentBranch.levelIds)
            ? currentBranch.levelIds.map(Number)
            : Array.isArray(currentBranch.levels)
                ? currentBranch.levels.map(level => Number(level.levelId))
                : [];

        viewContainer.querySelectorAll('#edit-branchLevels .level-cb').forEach(checkbox => {
            checkbox.checked = levelIds.includes(Number(checkbox.value));
        });

        editButton.classList.add('hidden');
        saveButton?.classList.remove('hidden');
        cancelEditButton?.classList.remove('hidden');
    });

    cancelEditButton?.addEventListener('click', () => {
        resetEditMode();
    });

    let resetPasswordButton = viewContainer.querySelector('#sa-resetBranchAdminPwdBtn');
    if (resetPasswordButton) {
        const newButton = resetPasswordButton.cloneNode(true);
        resetPasswordButton.parentNode.replaceChild(newButton, resetPasswordButton);
        resetPasswordButton = newButton;

        resetPasswordButton.addEventListener('click', () => {
            showPremiumModal({
                title: 'Reset Admin Password',
                type: 'warning',
                contentText: 'Are you sure you want to reset the School Admin password for this branch?',
                confirmText: 'Yes, Reset',
                cancelText: 'Cancel',
                onConfirm: async modal => {
                    await modal.close();
                    const loaderToken = showLoader(
                        'Resetting administrator password...'
                    );

                    try {
                        await apiPut(`/superadmin/branches/${currentDetailBranchId}/reset-admin-password`, {});
                        hideLoader(loaderToken);
                        showSuccessMessage(
                            'New administrator credentials are being sent to the branch email.'
                        );
                    } catch (error) {
                        console.error(error);
                        hideLoader(loaderToken);
                        showErrorMessage('Failed to reset the branch administrator password.');
                    }
                }
            });
        });
    }

    let backupButton = viewContainer.querySelector('#sa-backupBranchBtn');
    if (backupButton) {
        const newButton = backupButton.cloneNode(true);
        backupButton.parentNode.replaceChild(newButton, backupButton);
        backupButton = newButton;

        backupButton.addEventListener('click', () => {
            confirmAction(
                'Export Branch Data',
                'info',
                'This will compile all database records for this branch into a secure file. Do you want to proceed?',
                'Start Export',
                `/superadmin/branches/${currentDetailBranchId}/export`,
                false,
                'Export initiated.',
                'Failed to trigger the branch export.',
                null
            );
        });
    }

    const editQrInput = viewContainer.querySelector('#edit-qrCode');
    editQrInput?.addEventListener('change', () => {
        const file = editQrInput.files?.[0];
        const qrImage = viewContainer.querySelector('#view-qrCode');
        const qrEmpty = viewContainer.querySelector('#view-qrCodeEmpty');
        if (!file || !qrImage) return;

        if (!['image/png', 'image/jpeg'].includes(file.type) || file.size > 500 * 1024) {
            editQrInput.value = '';
            showErrorMessage('QR Code must be JPG or PNG and no larger than 500 KB.');
            return;
        }

        const reader = new FileReader();
        reader.onload = () => {
            if (typeof reader.result !== 'string') {
                editQrInput.value = '';
                qrImage.classList.add('hidden');
                qrEmpty?.classList.remove('hidden');
                showErrorMessage('Unable to preview the selected QR Code.');
                return;
            }

            qrImage.src = reader.result;
            qrImage.classList.remove('hidden');
            qrEmpty?.classList.add('hidden');
        };
        reader.onerror = () => {
            editQrInput.value = '';
            qrImage.classList.add('hidden');
            qrEmpty?.classList.remove('hidden');
            showErrorMessage('Unable to preview the selected QR Code.');
        };
        reader.readAsDataURL(file);
    });

    saveButton?.addEventListener('click', () => {
        const errors = validateEditForm();
        if (errors.length > 0) {
            showEditValidation(errors);
            return;
        }

        showPremiumModal({
            title: 'Save Changes',
            type: 'info',
            contentText: 'Apply the updated branch address, communication and email settings?',
            confirmText: 'Save Changes',
            cancelText: 'Cancel',
            onConfirm: async modal => {
                await modal.close();

                const branchName = getInputValue('#edit-branchName');
                const branchEmail = getInputValue('#edit-branchEmail');
                const primaryPhone = getInputValue('#edit-primaryPhone');
                const secondaryPhone = getInputValue('#edit-secondaryPhone');

                const formData = new FormData();
                formData.append('branchName', branchName);
                formData.append('schoolCode', getInputValue('#edit-schoolCode'));
                formData.append('foundationDate', getInputValue('#edit-foundationDate'));
                formData.append('branchLocation', getInputValue('#edit-branchLocation'));
                formData.append('addressLine1', getInputValue('#edit-addressLine1'));
                formData.append('addressLine2', getInputValue('#edit-addressLine2'));
                formData.append('poBox', getInputValue('#edit-poBox'));
                formData.append('locality', getInputValue('#edit-locality'));
                formData.append('city', getInputValue('#edit-city'));
                formData.append('district', getInputValue('#edit-district'));
                formData.append('region', getInputValue('#edit-region'));
                formData.append('country', getInputValue('#edit-country'));
                formData.append('postalCode', getInputValue('#edit-postalCode'));
                formData.append('primaryPhone', primaryPhone);
                formData.append('secondaryPhone', secondaryPhone);
                formData.append('whatsappPhone', getInputValue('#edit-whatsappPhone') || 'NONE');
                formData.append('branchEmail', branchEmail);
                formData.append('airtelPayNumber', getInputValue('#edit-airtelPayNumber'));
                formData.append('airtelPayName', getInputValue('#edit-airtelPayName'));
                formData.append('emailFromName', getInputValue('#edit-emailFromName') || branchName);
                formData.append('emailReplyTo', getInputValue('#edit-emailReplyTo') || branchEmail);
                formData.append('emailEnabled', String(getEmailEnabled()));

                const legacyContactDetails = [primaryPhone, secondaryPhone, branchEmail]
                    .filter(Boolean)
                    .join(' | ');
                formData.append('contactDetails', legacyContactDetails);

                viewContainer
                    .querySelectorAll('#edit-branchLevels .level-cb:checked')
                    .forEach(checkbox =>
                        formData.append('levelIds', String(checkbox.value))
                    );

                const updatedIncharges = extractInchargeDetails(
                    viewContainer,
                    'edit-incharge-tbody',
                    'inc-name',
                    'inc-role',
                    'inc-phone'
                );
                formData.append('inchargeDetails', JSON.stringify(updatedIncharges));

                const logoFile = viewContainer.querySelector('#edit-branchLogo')?.files?.[0] || null;
                const photoFile = viewContainer.querySelector('#edit-schoolPhoto')?.files?.[0] || null;
                const documentFile = viewContainer.querySelector('#edit-govDocument')?.files?.[0] || null;
                const qrFile = viewContainer.querySelector('#edit-qrCode')?.files?.[0] || null;
                if (logoFile) formData.append('logo', logoFile);
                if (photoFile) formData.append('photo', photoFile);
                if (documentFile) formData.append('documents', documentFile);
                if (qrFile) formData.append('qrCode', qrFile);

                const loaderToken = showLoader(
                    'Saving branch changes...'
                );
                saveButton.disabled = true;
                let saveError = null;
                let savedSuccessfully = false;

                try {
                    await apiMultipart(`/superadmin/branches/${currentDetailBranchId}`, 'PUT', formData);
                    clearEditValidation();
                    await openViewMore(
                        currentDetailBranchId,
                        {
                            forceReload: true,
                            showBusy: false
                        }
                    );
                    savedSuccessfully = true;
                } catch (error) {
                    console.error(error);
                    saveError = error;
                } finally {
                    saveButton.disabled = false;
                    hideLoader(loaderToken);
                }

                if (savedSuccessfully) {
                    showSuccessMessage(
                        'Branch details updated successfully.'
                    );
                } else {
                    showErrorMessage(
                        saveError?.message ||
                        'Failed to save branch changes.'
                    );
                }
            }
        });
    });

    function resetEditMode() {
        clearEditValidation();

        viewContainer
            .querySelectorAll('.detail-text')
            .forEach(element => element.classList.remove('hidden'));

        viewContainer
            .querySelectorAll('.detail-input')
            .forEach(element => element.classList.add('hidden'));

        // Restore only the read-only Incharge table in View mode.
        const inchargeView = viewContainer.querySelector('#view-inchargeDetails');
        const inchargeEdit = viewContainer.querySelector('#edit-inchargeDetails-container');
        inchargeView?.classList.remove('hidden');
        inchargeEdit?.classList.add('hidden');

        viewContainer
            .querySelectorAll('.branch-profile-edit-label')
            .forEach(element => element.classList.add('hidden'));

        editButton?.classList.remove('hidden');
        saveButton?.classList.add('hidden');
        cancelEditButton?.classList.add('hidden');

        // Return all Edit-only media controls to View mode.
        viewContainer
            .querySelector('.branch-modern-logo-editor')
            ?.classList.add('hidden');
        viewContainer
            .querySelector('.branch-media-editor')
            ?.classList.add('hidden');
        viewContainer
            .querySelector('.branch-government-documents-editor')
            ?.classList.add('hidden');
        viewContainer
            .querySelector('.branch-payment-qr-editor')
            ?.classList.add('hidden');

        const logoInput = viewContainer.querySelector('#edit-branchLogo');
        const photoInput = viewContainer.querySelector('#edit-schoolPhoto');
        const documentInput = viewContainer.querySelector('#edit-govDocument');
        const qrInput = viewContainer.querySelector('#edit-qrCode');
        if (logoInput) logoInput.value = '';
        if (photoInput) photoInput.value = '';
        if (documentInput) documentInput.value = '';
        if (qrInput) qrInput.value = '';
    }

    wireEditFileTriggers();

    const editLogoInput = viewContainer.querySelector('#edit-branchLogo');
    editLogoInput?.addEventListener('change', () => {
        const file = editLogoInput.files?.[0];
        const logoImage = viewContainer.querySelector('#view-branchLogo');
        const logoEmpty = viewContainer.querySelector('#view-branchLogoEmpty');
        if (!file || !logoImage) return;

        if (!['image/png', 'image/jpeg'].includes(file.type) || file.size > 500 * 1024) {
            editLogoInput.value = '';
            showErrorMessage('Branch logo must be JPG or PNG and no larger than 500 KB.');
            return;
        }

        const reader = new FileReader();
        reader.onload = () => {
            if (typeof reader.result !== 'string') return;
            logoImage.src = reader.result;
            logoImage.classList.remove('hidden');
            logoEmpty?.classList.add('hidden');
        };
        reader.onerror = () => {
            editLogoInput.value = '';
            showErrorMessage('Unable to preview the selected branch logo.');
        };
        reader.readAsDataURL(file);
    });

    let printButton = viewContainer.querySelector('#sa-printBranchBtn');
    if (printButton) {
        const newButton = printButton.cloneNode(true);
        printButton.parentNode.replaceChild(newButton, printButton);
        printButton = newButton;

        printButton.addEventListener('click', () => window.print());
    }

    /*
     * Branch View URL restoration
     *
     * The visible URL is the source of truth after a browser refresh.
     * Do not depend on history.state or routeParams because those are
     * not guaranteed to survive a full document reload.
     *
     * Priority:
     *   1. ?ref=<opaque-reference> from the address bar
     *   2. routeParams/history state as an in-app navigation fallback
     */
    const currentUrl = new URL(window.location.href);
    const branchViewReference = String(
        currentUrl.searchParams.get('ref')
        || branchViewNavigationState.reference
        || ''
    ).trim();

    // If this is an in-app Branch View navigation, make the opaque reference
    // part of the URL before resolving it. This prevents the router's initial
    // /superadmin/branches URL from becoming the final visible URL.
    if (branchViewNavigationState.reference) {
        setBranchViewReferenceInUrl(branchViewNavigationState.reference);
    }

    const routeBranchId = Number.parseInt(routeParams?.[0], 10);

    let requestedBranchId = null;

    if (branchViewReference) {
        try {
            const response = await apiGet(
                `/superadmin/branches/view-reference?ref=${encodeURIComponent(branchViewReference)}`
            );

            const resolvedBranchId = Number(response?.data);

            if (Number.isInteger(resolvedBranchId) && resolvedBranchId > 0) {
                requestedBranchId = resolvedBranchId;
            } else {
                showErrorMessage('The Branch View reference is invalid.');
            }
        } catch (error) {
            console.error(
                'Unable to resolve Branch View URL reference:',
                error
            );
            showErrorMessage(
                'The Branch View could not be restored from this URL.'
            );
        }
    }

    /*
     * Only use routeParams when there is no URL reference.
     * This keeps the opaque reference authoritative and makes refresh
     * deterministic.
     */
    if (
        !requestedBranchId
        && Number.isInteger(routeBranchId)
        && routeBranchId > 0
    ) {
        requestedBranchId = routeBranchId;
    }

    /*
     * Load the table only when we are actually displaying the Branch list.
     * On a direct Branch View URL, loading the entire list first was
     * unnecessary and could delay/interrupt reference restoration.
     */
    if (Number.isInteger(requestedBranchId) && requestedBranchId > 0) {
        await openViewMore(requestedBranchId, {
            showBusy: !branchViewNavigationState.loaderToken
        });
    } else {
        await loadBranches();
    }

    void ensureEditLevelsLoaded().catch(error => {
        console.warn('Edit branch levels could not be preloaded:', error);
    });
}

async function initAddBranchView() {
    const viewContainer = document.querySelector('#superadmin-add-branch-view');
    if (!viewContainer) return;

    const oldForm = viewContainer.querySelector('#add-branch-full-form');
    if (!oldForm) return;

    const form = oldForm.cloneNode(true);
    oldForm.parentNode.replaceChild(form, oldForm);
    form.reset();

    const getElement = selector => form.querySelector(selector);
    const getValue = selector => getElement(selector)?.value?.trim() || '';

    // ---------------------------------------------------------
    // QR CODE UPLOAD — CSP-SAFE PREVIEW
    // ---------------------------------------------------------
    const qrInput = getElement('#add-qrCode');
    const qrUploadBox = getElement('.branch-qr-upload-box');
    const qrPreview = getElement('#add-qrCode-preview');
    const qrPreviewImage = getElement('#add-qrCode-preview-image');
    const qrFileName = getElement('#add-qrCode-file-name');
    const qrRemoveButton = getElement('#add-qrCode-remove');

    // Use a data URL because the application CSP allows data: images
    // but blocks blob: URLs.
    let qrPreviewDataUrl = null;

    const clearQrPreview = () => {
        qrPreviewDataUrl = null;

        if (qrInput) qrInput.value = '';
        qrPreviewImage?.removeAttribute('src');

        if (qrFileName) {
            qrFileName.textContent = 'QR Code selected';
        }

        qrPreview?.classList.add('hidden');
        qrUploadBox?.classList.remove('hidden');
    };

    const showQrError = message => {
        clearQrPreview();

        if (typeof showErrorMessage === 'function') {
            showErrorMessage(message);
        }
    };

    if (
        qrInput &&
        qrUploadBox &&
        qrPreview &&
        qrPreviewImage &&
        qrFileName &&
        qrRemoveButton
    ) {
        qrInput.addEventListener('change', () => {
            const file = qrInput.files?.[0];

            if (!file) {
                clearQrPreview();
                return;
            }

            const allowedTypes = [
                'image/png',
                'image/jpeg'
            ];

            const maxSize = 500 * 1024;

            if (!allowedTypes.includes(file.type)) {
                showQrError('QR Code must be a PNG or JPG image.');
                return;
            }

            if (file.size > maxSize) {
                showQrError('QR Code exceeds the 500 KB limit.');
                return;
            }

            const reader = new FileReader();

            reader.onload = () => {
                if (typeof reader.result !== 'string') {
                    showQrError('Unable to preview the selected QR Code.');
                    return;
                }

                qrPreviewDataUrl = reader.result;
                qrPreviewImage.src = qrPreviewDataUrl;
                qrFileName.textContent = file.name;

                qrUploadBox.classList.add('hidden');
                qrPreview.classList.remove('hidden');
            };

            reader.onerror = () => {
                showQrError('Unable to read the selected QR Code.');
            };

            reader.readAsDataURL(file);
        });

        qrRemoveButton.addEventListener('click', event => {
            event.preventDefault();
            event.stopPropagation();
            clearQrPreview();
        });
    }

    const validationSummary = getElement('#add-branch-validation-summary');
    const validationList = getElement('#add-branch-validation-list');

    const setClass = (element, className, enabled) => {
        if (!element) return;
        if (enabled) {
            element.classList.add(className);
        } else {
            element.classList.remove(className);
        }
    };

    const clearValidation = () => {
        form.querySelectorAll('.branch-field-invalid').forEach(element => {
            element.classList.remove('branch-field-invalid');
            element.removeAttribute('aria-invalid');
        });

        setClass(validationSummary, 'hidden', true);
        if (validationList) validationList.textContent = '';
    };

    const clearElementValidation = element => {
        if (!element) return;
        element.classList.remove('branch-field-invalid');
        element.removeAttribute('aria-invalid');

        const field = element.closest('.branch-form-field');
        field?.classList.remove('branch-field-invalid');
    };

    const markInvalid = element => {
        if (!element) return;
        element.classList.add('branch-field-invalid');
        element.setAttribute('aria-invalid', 'true');

        const field = element.closest('.branch-form-field');
        field?.classList.add('branch-field-invalid');
    };

    const addError = (errors, message, element = null) => {
        errors.push({ message, element });
        markInvalid(element);
    };

    const displayValidationErrors = errors => {
        if (!errors.length) {
            setClass(validationSummary, 'hidden', true);
            return;
        }

        if (validationList) {
            validationList.textContent = '';

            errors.forEach(error => {
                const item = document.createElement('li');
                item.textContent = error.message;
                validationList.appendChild(item);
            });
        }

        setClass(validationSummary, 'hidden', false);

        const firstElement = errors.find(error => error.element)?.element;
        const scrollTarget = firstElement || validationSummary;

        scrollTarget?.scrollIntoView({
            behavior: 'smooth',
            block: 'center'
        });

        window.setTimeout(() => {
            if (
                firstElement &&
                typeof firstElement.focus === 'function'
            ) {
                firstElement.focus({ preventScroll: true });
            }
        }, 350);
    };

    const isValidPhone = value =>
        !value || /^[0-9+()\-\s]{7,30}$/.test(value);

    const validateForm = () => {
        clearValidation();

        const errors = [];
        const schoolName = getValue('#add-schoolName');
        const schoolCode = getValue('#add-schoolCode');
        const foundationDate = getValue('#add-foundationDate');
        const shortLocation = getValue('#add-shortLocation');
        const addressLine1 = getValue('#add-addressLine1');
        const country = getValue('#add-country');
        const primaryPhone = getValue('#add-primaryPhone');
        const secondaryPhone = getValue('#add-secondaryPhone');
        const whatsappPhone = getValue('#add-whatsappPhone') || 'NONE';
        const branchEmail = getValue('#add-branchEmail');
        const airtelPayNumber = getValue('#add-airtelPayNumber');

        if (!schoolName) {
            addError(errors, 'School Name is required.', getElement('#add-schoolName'));
        }

        if (!schoolCode) {
            addError(errors, 'School Code is required.', getElement('#add-schoolCode'));
        } else if (!/^[A-Za-z0-9_-]{2,20}$/.test(schoolCode)) {
            addError(
                errors,
                'School Code may contain only letters, numbers, hyphens and underscores.',
                getElement('#add-schoolCode')
            );
        }

        if (!foundationDate) {
            addError(errors, 'Foundation Date is required.', getElement('#add-foundationDate'));
        } else {
            const selectedDate = new Date(`${foundationDate}T00:00:00`);
            const today = new Date();
            today.setHours(0, 0, 0, 0);

            if (selectedDate > today) {
                addError(
                    errors,
                    'Foundation Date cannot be in the future.',
                    getElement('#add-foundationDate')
                );
            }
        }

        if (!shortLocation) {
            addError(errors, 'Short Location is required.', getElement('#add-shortLocation'));
        }

        if (!addressLine1) {
            addError(
                errors,
                'Campus / Address Line 1 is required.',
                getElement('#add-addressLine1')
            );
        }

        if (!country) {
            addError(errors, 'Country is required.', getElement('#add-country'));
        }

        if (!primaryPhone) {
            addError(errors, 'Phone Number 1 is required.', getElement('#add-primaryPhone'));
        } else if (!isValidPhone(primaryPhone)) {
            addError(
                errors,
                'Phone Number 1 contains unsupported characters.',
                getElement('#add-primaryPhone')
            );
        }

        if (secondaryPhone && !isValidPhone(secondaryPhone)) {
            addError(
                errors,
                'Phone Number 2 contains unsupported characters.',
                getElement('#add-secondaryPhone')
            );
        }

        if (
            primaryPhone &&
            secondaryPhone &&
            primaryPhone.replace(/\D/g, '') === secondaryPhone.replace(/\D/g, '')
        ) {
            addError(
                errors,
                'Phone Number 2 must be different from Phone Number 1.',
                getElement('#add-secondaryPhone')
            );
        }

        if (whatsappPhone === 'PRIMARY' && !primaryPhone) {
            addError(
                errors,
                'Enter Phone Number 1 because it is selected for WhatsApp.',
                getElement('#add-primaryPhone')
            );
        }

        if (whatsappPhone === 'SECONDARY' && !secondaryPhone) {
            addError(
                errors,
                'Enter Phone Number 2 because it is selected for WhatsApp.',
                getElement('#add-secondaryPhone')
            );
        }

        if (
            whatsappPhone === 'BOTH' &&
            (!primaryPhone || !secondaryPhone)
        ) {
            addError(
                errors,
                'Enter both phone numbers when both are selected for WhatsApp.',
                !primaryPhone
                    ? getElement('#add-primaryPhone')
                    : getElement('#add-secondaryPhone')
            );
        }

        const airtelPayNumberInput = getElement('#add-airtelPayNumber');
        if (airtelPayNumber && !isValidPhone(airtelPayNumber)) {
            addError(
                errors,
                'Airtel Pay Number contains unsupported characters.',
                airtelPayNumberInput
            );
        }

        const emailInput = getElement('#add-branchEmail');
        if (!branchEmail) {
            addError(errors, 'Official Branch Email is required.', emailInput);
        } else if (emailInput?.validity?.typeMismatch) {
            addError(errors, 'Enter a valid official branch email address.', emailInput);
        }

        const selectedLevels = Array.from(
            form.querySelectorAll('.level-cb:checked')
        );

        if (selectedLevels.length === 0) {
            const levelContainer = getElement('#add-branchLevels');
            addError(
                errors,
                'Select at least one education level.',
                levelContainer
            );
        }

        form.querySelectorAll('#incharge-tbody tr').forEach((row, index) => {
            const nameInput = row.querySelector('.incharge-name');
            const roleInput = row.querySelector('.incharge-role');
            const phoneInput = row.querySelector('.incharge-phone');

            const name = nameInput?.value?.trim() || '';
            const role = roleInput?.value?.trim() || '';
            const phone = phoneInput?.value?.trim() || '';
            const hasAnyValue = Boolean(name || role || phone);

            if (!hasAnyValue) return;

            if (!name) {
                addError(
                    errors,
                    `Incharge row ${index + 1}: Full Name is required.`,
                    nameInput
                );
            }

            if (!role) {
                addError(
                    errors,
                    `Incharge row ${index + 1}: Role / Position is required.`,
                    roleInput
                );
            }

            if (!phone) {
                addError(
                    errors,
                    `Incharge row ${index + 1}: Phone Number is required.`,
                    phoneInput
                );
            } else if (!isValidPhone(phone)) {
                addError(
                    errors,
                    `Incharge row ${index + 1}: Enter a valid phone number.`,
                    phoneInput
                );
            }
        });

        const logoFile = getElement('#add-logo')?.files?.[0] || null;
        const allowedImageTypes = ['image/jpeg', 'image/png'];
        const maxLogoSize = 500 * 1024;
        const maxImageSize = 100 * 1024;

        if (logoFile) {
            if (!allowedImageTypes.includes(logoFile.type)) {
                addError(
                    errors,
                    'Branch Logo must be a JPG or PNG image.',
                    getElement('#add-logo')
                );
            } else if (logoFile.size > maxLogoSize) {
                addError(
                    errors,
                    'Branch Logo exceeds the 500 KB limit.',
                    getElement('#add-logo')
                );
            }
        }

        const photoFile = getElement('#add-photo')?.files?.[0] || null;

        if (photoFile) {
            if (!allowedImageTypes.includes(photoFile.type)) {
                addError(
                    errors,
                    'School Photo must be a JPG or PNG image.',
                    getElement('#add-photo')
                );
            } else if (photoFile.size > maxImageSize) {
                addError(
                    errors,
                    'School Photo exceeds the 100 KB limit.',
                    getElement('#add-photo')
                );
            }
        }

        const documentFiles = Array.from(
            getElement('#add-doc')?.files || []
        );
        const allowedDocumentTypes = [
            'image/jpeg',
            'image/png',
            'application/pdf'
        ];
        const maxPdfSize = 2 * 1024 * 1024;

        documentFiles.forEach(file => {
            if (!allowedDocumentTypes.includes(file.type)) {
                addError(
                    errors,
                    `Government document "${file.name}" must be JPG, PNG or PDF.`,
                    getElement('#add-doc')
                );
                return;
            }

            if (file.type === 'application/pdf' && file.size > maxPdfSize) {
                addError(
                    errors,
                    `PDF "${file.name}" exceeds the 2 MB limit.`,
                    getElement('#add-doc')
                );
            }

            if (
                (file.type === 'image/jpeg' || file.type === 'image/png') &&
                file.size > maxImageSize
            ) {
                addError(
                    errors,
                    `Image "${file.name}" exceeds the 100 KB limit.`,
                    getElement('#add-doc')
                );
            }
        });

        displayValidationErrors(errors);

        return {
            valid: errors.length === 0,
            selectedLevelIds: selectedLevels.map(checkbox => checkbox.value)
        };
    };

    const createInchargeRow = () => {
        const template = viewContainer.querySelector('#incharge-row-template');
        const tbody = getElement('#incharge-tbody');
        if (!template || !tbody) return;

        const clone = template.content.cloneNode(true);
        const row = clone.querySelector('tr');

        clone.querySelector('.remove-incharge-btn')?.addEventListener('click', () => {
            row?.remove();
        });

        tbody.appendChild(clone);
    };

    await populateDynamicLevels('add-branchLevels');

    const tbody = getElement('#incharge-tbody');
    if (tbody) {
        tbody.textContent = '';
        createInchargeRow();
    }

    const oldAddRowButton = getElement('#addInchargeRowBtn');
    if (oldAddRowButton) {
        const addRowButton = oldAddRowButton.cloneNode(true);
        oldAddRowButton.parentNode.replaceChild(addRowButton, oldAddRowButton);
        addRowButton.addEventListener('click', createInchargeRow);
    }

    form.querySelectorAll('.upload-title').forEach(title => {
        title.textContent = 'Click or drag file here';
    });

    form.querySelectorAll('.file-hidden-input').forEach(input => {
        input.addEventListener('change', event => {
            const files = Array.from(event.target.files || []);
            let fileName = 'Click or drag file here';

            if (files.length > 1) {
                fileName = `${files.length} files selected`;
            } else if (files.length === 1) {
                fileName = files[0].name;
            }

            const title = input.parentElement?.querySelector('.upload-title');
            if (title) title.textContent = fileName;

            const zone = input.closest('.upload-zone');
            zone?.querySelector('.branch-file-preview')?.remove();

            const selectedFile = files[0];

            if (selectedFile && selectedFile.type.startsWith('image/')) {
                const reader = new FileReader();

                reader.onload = () => {
                    if (typeof reader.result !== 'string') return;

                    const preview = document.createElement('img');
                    preview.className = 'branch-file-preview';
                    preview.alt = 'Selected file preview';
                    preview.src = reader.result;
                    zone?.appendChild(preview);
                };

                reader.onerror = () => {
                    console.error(
                        'Unable to create image preview:',
                        selectedFile.name
                    );
                };

                reader.readAsDataURL(selectedFile);
            }

            clearElementValidation(input);
        });
    });

    form.addEventListener('input', event => {
        clearElementValidation(event.target);
    });

    form.addEventListener('change', event => {
        clearElementValidation(event.target);

        if (event.target.matches('.level-cb')) {
            clearElementValidation(getElement('#add-branchLevels'));
        }
    });

    const navigateToBranches = () =>
        window.erpNavigate({
            role: 'superadmin',
            view: 'branches',
            title: 'Manage Branches',
            historyMode: 'push'
        });

    const oldBackButton = viewContainer.querySelector('#backToBranchesBtn');
    if (oldBackButton) {
        const backButton = oldBackButton.cloneNode(true);
        oldBackButton.parentNode.replaceChild(backButton, oldBackButton);
        backButton.addEventListener('click', navigateToBranches);
    }

    const cancelButton = getElement('#cancelAddBranchBtn');
    cancelButton?.addEventListener('click', navigateToBranches);

    form.addEventListener('submit', async event => {
        event.preventDefault();

        const validation = validateForm();
        if (!validation.valid) return;

        const submitButton = form.querySelector('button[type="submit"]');
        const originalButtonHtml = submitButton?.innerHTML || '';

        if (submitButton) {
            submitButton.disabled = true;
            submitButton.innerHTML =
                '<i class="bi bi-hourglass-split" aria-hidden="true"></i> Saving...';
        }

        const branchName = getValue('#add-schoolName');
        const schoolCode = getValue('#add-schoolCode').toUpperCase();
        const foundationDate = getValue('#add-foundationDate');
        const primaryPhone = getValue('#add-primaryPhone');
        const secondaryPhone = getValue('#add-secondaryPhone');
        const whatsappPhone = getValue('#add-whatsappPhone') || 'NONE';
        const branchEmail = getValue('#add-branchEmail').toLowerCase();

        const whatsappLabels = {
            NONE: 'None',
            PRIMARY: 'Phone Number 1',
            SECONDARY: 'Phone Number 2',
            BOTH: 'Both phone numbers'
        };

        const contactDetails = [
            primaryPhone ? `Primary: ${primaryPhone}` : null,
            secondaryPhone ? `Secondary: ${secondaryPhone}` : null,
            `WhatsApp: ${whatsappLabels[whatsappPhone] || 'None'}`,
            branchEmail ? `Email: ${branchEmail}` : null
        ].filter(Boolean).join(' | ');

        const incharges = extractInchargeDetails(
            form,
            'incharge-tbody',
            'incharge-name',
            'incharge-role',
            'incharge-phone'
        );

        const formData = new FormData();
        formData.append('branchName', branchName);
        formData.append('schoolCode', schoolCode);
        formData.append('foundationDate', foundationDate);
        formData.append('branchLocation', getValue('#add-shortLocation'));

        formData.append('addressLine1', getValue('#add-addressLine1'));
        formData.append('addressLine2', getValue('#add-addressLine2'));
        formData.append('poBox', getValue('#add-poBox'));
        formData.append('locality', getValue('#add-locality'));
        formData.append('city', getValue('#add-city'));
        formData.append('district', getValue('#add-district'));
        formData.append('region', getValue('#add-region'));
        formData.append('country', getValue('#add-country'));
        formData.append('postalCode', getValue('#add-postalCode'));

        formData.append('primaryPhone', primaryPhone);
        formData.append('secondaryPhone', secondaryPhone);
        formData.append('whatsappPhone', whatsappPhone);
        formData.append('branchEmail', branchEmail);

        formData.append('bankName', getValue('#add-bankName'));
        formData.append('bankAccountName', getValue('#add-bankAccountName'));
        formData.append('bankAccountNumber', getValue('#add-bankAccountNumber'));
        formData.append('bankBranch', getValue('#add-bankBranch'));
        formData.append('airtelPayNumber', getValue('#add-airtelPayNumber'));
        formData.append('airtelPayName', getValue('#add-airtelPayName'));

        const qrFile = getElement('#add-qrCode')?.files?.[0] || null;
        if (qrFile) {
            formData.append('qrCode', qrFile);
        }

        formData.append('emailFromName', branchName);
        formData.append('emailReplyTo', branchEmail);
        formData.append('emailEnabled', 'true');

        formData.append('contactDetails', contactDetails);
        formData.append('inchargeDetails', JSON.stringify(incharges));

        validation.selectedLevelIds.forEach(levelId => {
            formData.append('levelIds', String(levelId));
        });

        const logoFile = getElement('#add-logo')?.files?.[0] || null;
        if (logoFile) {
            formData.append('logo', logoFile);
        }

        const photoFile = getElement('#add-photo')?.files?.[0] || null;
        if (photoFile) {
            formData.append('photo', photoFile);
        }

        Array.from(getElement('#add-doc')?.files || []).forEach(file => {
            if (file instanceof File) {
                formData.append('documents', file);
            }
        });

        const loaderToken = showLoader('Creating branch...');

        try {
            await apiMultipart(
                '/superadmin/branches',
                'POST',
                formData
            );

            const successTemplate =
                document.getElementById(
                    'branch-success-content-template'
                );

            if (successTemplate) {
                const successClone =
                    successTemplate.content.cloneNode(true);

                const branchNameElement =
                    successClone.querySelector(
                        '.success-branch-name'
                    );

                const credentialsBox =
                    successClone.querySelector(
                        '.credentials-box'
                    );

                if (branchNameElement) {
                    branchNameElement.textContent =
                        branchName;
                }

                if (credentialsBox) {
                    credentialsBox.innerHTML = `
            <p>
                A secure Branch Administrator account
                will be created and the temporary
                credentials will be sent to the
                registered branch email.
            </p>
        `;
                }

                showPremiumModal({
                    title: 'Branch Created!',
                    type: 'success',
                    contentNode: successClone,
                    confirmText: 'Go to Manage Branches',
                    onConfirm: async modal => {
                        await modal.close();
                        await navigateToBranches();
                    }
                });
            } else {
                showPremiumModal({
                    title: 'Branch Created!',
                    type: 'success',
                    contentText:
                        'Branch created successfully.',
                    confirmText: 'Go to Manage Branches',
                    onConfirm: async modal => {
                        await modal.close();
                        await navigateToBranches();
                    }
                });
            }
        } catch (error) {
            console.error('Branch save error:', error);
            showErrorMessage(
                error?.message ||
                'Failed to create the branch. Confirm that the backend supports the new address and communication fields.'
            );
        } finally {
            hideLoader(loaderToken);

            if (submitButton) {
                submitButton.disabled = false;
                submitButton.innerHTML = originalButtonHtml;
            }
        }
    });
}
document.addEventListener('viewLoaded', function (e) {
    if (e.detail.role !== 'superadmin') return;

    if (e.detail.view === 'branches') {
        const task = initBranchesView(e.detail.routeParams || []);
        if (typeof e.detail.waitUntil === 'function') e.detail.waitUntil(task);
    } else if (e.detail.view === 'add-branch') {
        const task = initAddBranchView();
        if (typeof e.detail.waitUntil === 'function') e.detail.waitUntil(task);
    }
});

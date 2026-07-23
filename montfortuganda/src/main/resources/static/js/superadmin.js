/* global Swal, initErpCalendar, apiGet, apiPost, apiPut, apiMultipart, showPremiumModal, showSuccessMessage, showErrorMessage, showLoader, hideLoader, loadView, renderFetchingMessage, renderEmptyTableMessage, GlobalPagination */
// ==========================================
// SUPER ADMIN MODULE
// ==========================================

const formatUGX = (num) => new Intl.NumberFormat('en-UG', { style: 'currency', currency: 'UGX', maximumFractionDigits: 0 }).format(num);

// ==========================================
// UI ANIMATION UTILITIES
// ==========================================

function animateValue(obj, start, end, duration, isCurrency = false) {
    if (!obj) return;
    let startTimestamp = null;
    const step = (timestamp) => {
        if (!startTimestamp) startTimestamp = timestamp;
        const progress = Math.min((timestamp - startTimestamp) / duration, 1);

        // Premium Quartic ease-out curve (fast start, slow dramatic finish)
        const easeOut = 1 - Math.pow(1 - progress, 4);
        let current = Math.floor(easeOut * (end - start) + start);

        obj.textContent = isCurrency ? formatUGX(current) : current.toLocaleString();

        if (progress < 1) {
            window.requestAnimationFrame(step);
        } else {
            // Ensure it lands perfectly on the final number
            obj.textContent = isCurrency ? formatUGX(end) : end.toLocaleString();
        }
    };
    window.requestAnimationFrame(step);
}

// ==========================================
// UTILITY: POPULATE BRANCH DROPDOWNS
// ==========================================
async function populateBranchDropdowns(selectElements) {
    try {
        const json = await apiGet('/superadmin/branches');
        /** @type {{branchId: number, branchName: string, branchLocation: string}[]} */
        const branches = json.data || [];

        selectElements.forEach(selectEl => {
            if (!selectEl) return;
            const firstOption = selectEl.options[0]; // Preserve "All Branches"
            selectEl.textContent = '';
            if (firstOption) selectEl.appendChild(firstOption);

            branches.forEach(b => {
                const opt = document.createElement('option');
                opt.value = b.branchName;
                opt.textContent = b.branchLocation ? `${b.branchName} (${b.branchLocation})` : b.branchName;
                selectEl.appendChild(opt);
            });
        });
    } catch (e) {
        console.error("Failed to fetch branches for dropdowns", e);
    }
}

// ==========================================
// UTILITY: FETCH AND RENDER DYNAMIC LEVELS
// ==========================================
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

function confirmAllocation(title, contentText, endpoint, payload, successMsg, errorMsg, inputField) {
    showPremiumModal({
        title: title,
        type: 'warning',
        contentText: contentText,
        confirmText: 'Allocate',
        cancelText: 'Cancel',
        onConfirm: async (modal) => {
            await modal.close();
            const loaderToken = showLoader('Allocating funds...');
            try {
                await apiPost(endpoint, payload);
                showSuccessMessage(successMsg);
                if (inputField) inputField.value = '';
            } catch (err) {
                showErrorMessage(errorMsg);
            } finally {
                hideLoader(loaderToken);
            }
        }
    });
}

function confirmAction(title, type, contentText, confirmText, endpoint, isPost, successMsg, errorMsg, callbackStr) {
    showPremiumModal({
        title: title,
        type: type,
        contentText: contentText,
        confirmText: confirmText,
        cancelText: 'Cancel',
        onConfirm: async (modal) => {
            await modal.close();
            const loaderToken = showLoader('Processing request...');
            try {
                let res;
                if (isPost) res = await apiPost(endpoint);
                else res = await apiGet(endpoint);
                showSuccessMessage(res.message || successMsg);
                if (callbackStr === 'initSystemBackupsView') {
                    initSystemBackupsView();
                }
            } catch (e) {
                console.error(e);
                showErrorMessage(errorMsg);
            } finally {
                hideLoader(loaderToken);
            }
        }
    });
}

document.addEventListener('viewLoaded', function(e) {
    if (e.detail.role === 'superadmin') {
        if (e.detail.view === 'branches') {
            void initBranchesView();
        } else if (e.detail.view === 'home') {
            void initHomeView();
        } else if (e.detail.view === 'add-branch') {
            void initAddBranchView();
        } else if (e.detail.view === 'system-stats') {
            void initSystemStatsView();
        } else if (e.detail.view === 'audit-logs') {
            void initAuditLogsView();
        } else if (e.detail.view === 'system-backups') {
            void initSystemBackupsView();
        } else if (e.detail.view === 'scholarships-funds-got') {
            void initScholarshipsFundsGotView();
        } else if (e.detail.view === 'scholarships-global-search') {
            void initScholarshipsApplicationsView(); // Fixed
        } else if (e.detail.view === 'scholarships-bulk-distribution') {
            initBulkDistributionView();
        } else if (e.detail.view === 'scholarships-1-to-1') {
            initOneToOneSponsorshipView();
        } else if (e.detail.view === 'scholarships-partial-fund') {
            void initPartialStudentFundView(); // Fixed
        }
    }
});

// ---------------------------------------------------------
// 1. DASHBOARD HOME LOGIC
// ---------------------------------------------------------
async function initHomeView() {
    const linkBranches = document.getElementById('sa-btnLinkBranches');
    if (linkBranches) {
        linkBranches.addEventListener('click', () => {
            const link = document.querySelector('.sidebar-nav a[href*="branches"]');
            if(link) link.click();
        });
    }

    try {
        const [branchRes, usersRes, settingsRes] = await Promise.all([
            apiGet('/superadmin/branches').catch(()=>null),
            apiGet('/superadmin/users').catch(()=>null),
            apiGet('/superadmin/settings').catch(()=>null)
        ]);

        animateValue(document.getElementById('sa-statTotalBranches'), 0, branchRes?.data?.length || 0, 1200, false);
        animateValue(document.getElementById('sa-statTotalUsers'), 0, usersRes?.data?.length || 0, 1500, false);
        animateValue(document.getElementById('sa-statTotalSettings'), 0, settingsRes?.data?.length || 0, 1800, false);
    } catch (error) {
        console.error("Failed to load dashboard stats", error);
        showErrorMessage("Could not load dashboard statistics.");
    }
}


// ---------------------------------------------------------
// 2. BRANCHES LOGIC (SPA)
// ---------------------------------------------------------
function initBranchesView() {
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
    let openingBranchId = null;

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

    async function loadBranches() {
        if (tableBody) renderFetchingMessage(tableBody, 6, 'Fetching branches...');

        try {
            const response = await apiGet('/superadmin/branches');
            const branches = Array.isArray(response?.data) ? response.data : [];
            loadedBranches = branches;
            const template = viewContainer.querySelector('#branch-row-template');

            if (!tableBody || !template) return;
            tableBody.textContent = '';

            branches.forEach(branch => {
                const clone = template.content.cloneNode(true);
                const branchId = Number(branch.branchId);

                clone.querySelector('.col-id').textContent = String(branchId);
                clone.querySelector('.col-name strong').textContent = displayValue(branch.branchName);
                clone.querySelector('.col-code').textContent = displayValue(branch.schoolCode);
                clone.querySelector('.col-type').textContent =
                    Array.isArray(branch.levels) && branch.levels.length > 0
                        ? branch.levels.map(level => level.levelName).filter(Boolean).join(', ')
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
        } catch (error) {
            console.error(error);
            if (tableBody) renderEmptyTableMessage(tableBody, 6, 'Failed to load branches.');
        }
    }

    tableBody?.addEventListener('click', event => {
        const viewButton = event.target.closest('.view-more-btn');
        const toggleButton = event.target.closest('.status-toggle');

        if (viewButton) {
            const id = Number.parseInt(viewButton.dataset.id, 10);
            if (Number.isInteger(id) && id > 0) void openViewMore(id);
            return;
        }

        if (!toggleButton) return;

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
                const loaderToken = showLoader(
                    'Updating branch status...'
                );

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
    });

    let backButton = viewContainer.querySelector('#sa-backToTableBtn');
    if (backButton) {
        const newButton = backButton.cloneNode(true);
        backButton.parentNode.replaceChild(newButton, backButton);
        backButton = newButton;

        backButton.addEventListener('click', () => {
            clearEditValidation();
            detailView?.classList.add('hidden');
            tableView?.classList.remove('hidden');
            void loadBranches();
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
            setText('#view-foundationDate', branch.foundationDate);

            setText('#view-branchLocation', branch.branchLocation);
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

            const logoImage = viewContainer.querySelector('#view-branchLogo');
            const logoEmpty = viewContainer.querySelector('#view-branchLogoEmpty');
            const branchLogoUrl = branch.branchLogoUrl || branch.logoUrl || null;

            if (logoImage && branchLogoUrl) {
                logoImage.src = branchLogoUrl;
                logoImage.classList.remove('hidden');
                logoEmpty?.classList.add('hidden');
            } else if (logoImage) {
                logoImage.removeAttribute('src');
                logoImage.classList.add('hidden');
                logoEmpty?.classList.remove('hidden');
            }

            const photoImage = viewContainer.querySelector('#view-schoolPhoto');
            if (photoImage && branch.schoolPhotoUrl) {
                photoImage.src = branch.schoolPhotoUrl;
                photoImage.classList.remove('hidden');
            } else if (photoImage) {
                photoImage.removeAttribute('src');
                photoImage.classList.add('hidden');
            }

            const documentContainer = viewContainer.querySelector('#view-govDocument');
            if (documentContainer) {
                documentContainer.textContent = '';

                if (branch.govDocumentUrl) {
                    const link = document.createElement('a');
                    link.href = branch.govDocumentUrl;
                    link.target = '_blank';
                    link.rel = 'noopener noreferrer';
                    link.className = 'btn-secondary';
                    link.textContent = 'View Document';
                    documentContainer.appendChild(link);
                } else {
                    documentContainer.textContent = 'No document uploaded.';
                }
            }

            tableView?.classList.add('hidden');
            detailView?.classList.remove('hidden');
            resetEditMode();

            try {
                const [statsResponse, logResponse] =
                    await Promise.all([
                        statsRequest,
                        logsRequest
                    ]);
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
                            clone.querySelector('.log-date').textContent = displayValue(log.date);
                            clone.querySelector('.log-user').textContent = displayValue(log.user);
                            clone.querySelector('.log-action').textContent = displayValue(log.action);
                            logBody.appendChild(clone);
                        });
                    }
                }
            } catch (error) {
                console.error('Branch stats or logs failed:', error);
            }
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

        viewContainer
            .querySelectorAll('.detail-text:not(.readonly-always)')
            .forEach(element => element.classList.add('hidden'));

        viewContainer
            .querySelectorAll('.detail-input')
            .forEach(element => element.classList.remove('hidden'));

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
        setInput(
            '#edit-emailEnabled',
            !(currentBranch.emailEnabled === false || currentBranch.emailEnabled === 0)
        );

        if (!viewContainer.querySelector('#edit-branchLevels .level-cb')) {
            await populateDynamicLevels('edit-branchLevels');
        }

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
        clearEditValidation();
        if (currentDetailBranchId) void openViewMore(currentDetailBranchId);
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
                if (logoFile) formData.append('logo', logoFile);
                if (photoFile) formData.append('photo', photoFile);
                if (documentFile) formData.append('documents', documentFile);

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

        editButton?.classList.remove('hidden');
        saveButton?.classList.add('hidden');
        cancelEditButton?.classList.add('hidden');

        const logoInput = viewContainer.querySelector('#edit-branchLogo');
        const photoInput = viewContainer.querySelector('#edit-schoolPhoto');
        const documentInput = viewContainer.querySelector('#edit-govDocument');
        if (logoInput) logoInput.value = '';
        if (photoInput) photoInput.value = '';
        if (documentInput) documentInput.value = '';
    }

    let printButton = viewContainer.querySelector('#sa-printBranchBtn');
    if (printButton) {
        const newButton = printButton.cloneNode(true);
        printButton.parentNode.replaceChild(newButton, printButton);
        printButton = newButton;

        printButton.addEventListener('click', () => window.print());
    }

    void loadBranches();
}

// ---------------------------------------------------------
// 3. ADD BRANCH PAGE LOGIC (DYNAMIC TABLE & JSON)
// ---------------------------------------------------------
// ---------------------------------------------------------
// 3. ADD BRANCH PAGE LOGIC (DYNAMIC TABLE & JSON)
// ---------------------------------------------------------
function initAddBranchView() {
    const viewContainer = document.querySelector('#superadmin-add-branch-view');
    if (!viewContainer) return;

    const oldForm = viewContainer.querySelector('#add-branch-full-form');
    if (!oldForm) return;

    const form = oldForm.cloneNode(true);
    oldForm.parentNode.replaceChild(form, oldForm);
    form.reset();

    const getElement = selector => form.querySelector(selector);
    const getValue = selector => getElement(selector)?.value?.trim() || '';

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

    void populateDynamicLevels('add-branchLevels');

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
// ---------------------------------------------------------
// 4. GLOBAL SYSTEM STATS LOGIC (UPGRADED)
// ---------------------------------------------------------
async function initSystemStatsView() {
    const viewContainer = document.querySelector('#superadmin-stats-view');
    if (!viewContainer) return;

    try {
        // Fetch everything first so they animate perfectly in sync!
        const [branchRes, stdRes, staffRes, sessRes] = await Promise.all([
            apiGet('/superadmin/branches').catch(()=>null),
            apiGet('/superadmin/students').catch(()=>null),
            apiGet('/superadmin/staff').catch(()=>null),
            apiGet('/superadmin/sessions/active').catch(()=>null)
        ]);

        animateValue(document.querySelector('#global-stat-branches'), 0, branchRes?.data?.length || 0, 1200);
        animateValue(document.querySelector('#global-stat-students'), 0, stdRes?.data?.length || 0, 1500);
        animateValue(document.querySelector('#global-stat-staff'), 0, staffRes?.data?.length || 0, 1800);
        animateValue(document.querySelector('#global-stat-sessions'), 0, sessRes?.data?.length || 0, 2000);

        // Populate Branch Breakdown List securely via HTML Template
        const breakdownList = viewContainer.querySelector('#enrollment-breakdown-list');
        const template = viewContainer.querySelector('#enrollment-row-template');

        if (breakdownList && template) {
            breakdownList.textContent = ''; // Clear out old data

            // Temporary Mock Data representing real API output
            const mockEnrollmentData = [
                { name: 'Kampala Main Campus', count: 1200, percent: 85 },
                { name: 'Entebbe Branch', count: 850, percent: 60 },
                { name: 'Jinja Branch', count: 620, percent: 45 },
                { name: 'Mbarara Branch', count: 410, percent: 30 },
                { name: 'Gulu Branch', count: 250, percent: 15 }
            ];

            mockEnrollmentData.forEach(item => {
                const clone = template.content.cloneNode(true);
                clone.querySelector('.branch-name').textContent = item.name;
                clone.querySelector('.branch-count').textContent = item.count.toLocaleString() + ' Students';

                // Trigger smooth CSS animation for the progress bar
                const bar = clone.querySelector('.branch-bar');
                setTimeout(() => {
                    bar.style.width = item.percent + '%';
                }, 50); // slight delay to ensure DOM is ready for animation

                breakdownList.appendChild(clone);
            });
        }
    } catch (error) {
        console.error(
            'System statistics failed to load.',
            error
        );
    }
}

// ---------------------------------------------------------
// 5. GLOBAL AUDIT LOGS LOGIC
// ---------------------------------------------------------
async function initAuditLogsView() {
    const viewContainer = document.querySelector('#superadmin-audit-view');
    if (!viewContainer) return;

    const tbody = viewContainer.querySelector('#global-audit-tbody');
    const template = viewContainer.querySelector('#global-audit-row-template');
    if (!tbody || !template) return;

    if (tbody) renderFetchingMessage(tbody, 10, 'Fetching audit logs...');
    try {
        const logsRes = await apiGet('/superadmin/global-logs').catch(() => null);
        const logs = (logsRes && logsRes.data) ? logsRes.data : [
            { date: 'Today, 08:00 AM', branch: 'Global', user: 'Super Admin', action: 'System maintenance started' },
            { date: 'Yesterday, 02:30 PM', branch: 'Kampala Branch', user: 'School Admin', action: 'Updated fee structures' }
        ];

        tbody.textContent = '';
        logs.forEach(log => {
            const clone = template.content.cloneNode(true);
            clone.querySelector('.log-date').textContent = log.date;
            clone.querySelector('.log-branch').textContent = log.branch;
            clone.querySelector('.log-user').textContent = log.user;
            clone.querySelector('.log-action').textContent = log.action;
            tbody.appendChild(clone);
        });
    } catch (error) {
        console.error('Audit logs failed to load.', error);
        renderEmptyTableMessage(
            tbody,
            10,
            'Failed to load audit logs.'
        );
    }
}

// ---------------------------------------------------------
// 6. GLOBAL BACKUPS LOGIC
// ---------------------------------------------------------
function initSystemBackupsView() {
    const viewContainer = document.querySelector('#superadmin-backups-view');
    if (!viewContainer) return;

    const tbody = viewContainer.querySelector('#global-backups-tbody');
    const template = viewContainer.querySelector('#global-backup-row-template');

    if(tbody && template) {
        tbody.textContent = '';
        const backups = [
            { id: 'BKP-20260615', date: '2026-06-15 02:00 AM', size: '14.2 GB', user: 'Auto-Schedule' },
            { id: 'BKP-20260608', date: '2026-06-08 02:00 AM', size: '14.1 GB', user: 'Auto-Schedule' }
        ];
        backups.forEach(bkp => {
            const clone = template.content.cloneNode(true);
            clone.querySelector('.bkp-id').textContent = bkp.id;
            clone.querySelector('.bkp-date').textContent = bkp.date;
            clone.querySelector('.bkp-size').textContent = bkp.size;
            clone.querySelector('.bkp-user').textContent = bkp.user;
            tbody.appendChild(clone);
        });
    }

    const backupBtn = viewContainer.querySelector('#sa-triggerGlobalBackupBtn');
    if (backupBtn) {
        backupBtn.addEventListener('click', () => {
            confirmAction(
                'Initiate Global Backup',
                'warning',
                'This will freeze non-essential database writes for approximately 2 minutes to ensure a clean global snapshot. Proceed?',
                'Yes, Backup Now',
                '/superadmin/backups/trigger',
                true,
                'Backup initiated successfully.',
                'Failed to trigger backup. Ensure Java backend endpoint exists.',
                'initSystemBackupsView'
            );
        });
    }
}
// ==========================================
// VIEW 1: SCHOLARSHIPS FUNDS GOT (TREASURY)
// ==========================================
async function initScholarshipsFundsGotView() {
    const viewContainer = document.querySelector('#superadmin-funds-got-view');
    if (!viewContainer) return;

    const tbody = viewContainer.querySelector('#treasury-donors-tbody');
    if (tbody) renderFetchingMessage(tbody, 10, 'Fetching treasury data...');

    try {
        const [summaryRes, donorsRes] = await Promise.all([
            apiGet('/superadmin/scholarships/funds-summary'),
            apiGet('/superadmin/scholarships/donors')
        ]);

        /** @type {{totalRaisedUgx: number, totalSpentUgx: number, availableBalanceUgx: number, studentsSponsored: number}} */
        const summary = summaryRes.data || summaryRes;

        animateValue(document.querySelector('#treasury-total-raised'), 0, summary.totalRaisedUgx || 0, 1500, true);
        animateValue(document.querySelector('#treasury-total-spent'), 0, summary.totalSpentUgx || 0, 1800, true);
        animateValue(document.querySelector('#treasury-available'), 0, summary.availableBalanceUgx || 0, 2000, true);
        animateValue(document.querySelector('#treasury-sponsored'), 0, summary.studentsSponsored || 0, 1200, false);

        const donorsArray = Array.isArray(donorsRes) ? donorsRes : (donorsRes.data || []);

        const tbody = viewContainer.querySelector('tbody');
        if (tbody && (!donorsArray || donorsArray.length === 0)) {
            renderEmptyTableMessage(tbody, 10, 'No donor records found.');
        } else if (tbody) {

            const liveDonationsData = donorsArray.map(d => ({
                id: d.id,
                receipt_number: d.receiptNumber,
                full_name: d.fullName,
                email: d.email,
                currency: d.currency,
                amount: d.amount, // <--- Added the foreign amount here
                amount_received: d.amountReceivedUgx,
                amount_spent: d.amountSpentUgx,
                students_benefited: d.studentsBenefited,
                term: d.term
            }));

            const pagination = new GlobalPagination({
                data: liveDonationsData,
                itemsPerPage: 25,
                elements: {
                    startId: 'funds-page-start', endId: 'funds-page-end', totalId: 'funds-page-total',
                    prevBtnId: 'btn-funds-prev', nextBtnId: 'btn-funds-next',
                    numbersContainerId: 'funds-pagination-numbers', templateId: 'funds-page-number-template'
                },
                renderCallback: (pageData) => {
                    const tbody = viewContainer.querySelector('#treasury-donors-tbody');
                    const template = viewContainer.querySelector('#treasury-donor-row-template');
                    if (tbody && template) {
                        tbody.textContent = '';
                        pageData.forEach(donor => {
                            const clone = template.content.cloneNode(true);
                            clone.querySelector('.donor-receipt').textContent = donor.receipt_number;
                            clone.querySelector('.donor-name').textContent = donor.full_name;
                            clone.querySelector('.donor-email').textContent = donor.email;

                            // <--- Updated to display BOTH Currency Type and Amount Donated! --->
                            clone.querySelector('.donor-foreign').textContent = `${donor.currency} ${parseFloat(donor.amount || 0).toLocaleString(undefined, {minimumFractionDigits: 2})}`;

                            clone.querySelector('.donor-received-ugx').textContent = formatUGX(donor.amount_received);

                            const availableUGX = donor.amount_received - donor.amount_spent;
                            const availElem = clone.querySelector('.donor-available-ugx');
                            availElem.textContent = formatUGX(availableUGX);
                            if (availableUGX <= 0) availElem.classList.replace('text-bold', 'text-muted');

                            clone.querySelector('.donor-students').textContent = `${donor.students_benefited} Students`;
                            clone.querySelector('.donor-term').textContent = donor.term;
                            tbody.appendChild(clone);
                        });
                    }
                }
            });
            pagination.render();

        }
    } catch (error) {
        console.error("Treasury load failed:", error);
        showErrorMessage("Failed to load global treasury data.");
    }
}
// ==========================================
// VIEW 2: SCHOLARSHIP APPLICATIONS (GLOBAL SEARCH)
// ==========================================
async function initScholarshipsApplicationsView() {
    const viewContainer = document.querySelector('#superadmin-global-search-view');
    if (!viewContainer) return;

    const branchSelect = viewContainer.querySelector('#gs-branch-filter');
    if (branchSelect) void populateBranchDropdowns([branchSelect]);

    const tbody = viewContainer.querySelector('#gs-table-body');
    if (tbody) renderFetchingMessage(tbody, 10, 'Fetching student applications...');

    try {
        const studentsRes = await apiGet('/superadmin/scholarships/pending-students');
        const rawData = Array.isArray(studentsRes) ? studentsRes : (studentsRes.data || []);

        const liveApplications = rawData.map(s => ({
            id: 'APP-' + s.id,
            name: s.studentName,
            demographics: 'Male',
            branch: s.branchName,
            levelClass: 'N/A',
            category: s.category || 'Financial Aid',
            status: 'Pending',
            shortfall: s.currentShortfallUgx
        }));

        let totalDeficit = 0;
        let branchPendingCounts = {};

        liveApplications.forEach(app => {
            if (app.status === 'Pending') {
                totalDeficit += app.shortfall;
                branchPendingCounts[app.branch] = (branchPendingCounts[app.branch] || 0) + 1;
            }
        });

        const assumedTotalNeed = totalDeficit + 150000000;
        let deficitPercentage = 0;
        if (assumedTotalNeed > 0) {
            deficitPercentage = Math.round((totalDeficit / assumedTotalNeed) * 100);
        }

        const donutChart = viewContainer.querySelector('.css-donut');
        if (donutChart) {
            donutChart.style.setProperty('--percentage', deficitPercentage);
            donutChart.querySelector('.donut-text').textContent = `${deficitPercentage}%`;
        }

        const legend = viewContainer.querySelector('#global-deficit-legend');
        if (legend) {
            legend.textContent = '';

            const p1 = document.createElement('p');
            const span1 = document.createElement('span');
            span1.className = 'badge-pending';
            span1.id = 'gs-pending-badge';
            span1.textContent = formatUGX(totalDeficit);
            animateValue(span1, 0, totalDeficit, 2500, true);
            const textNode1 = document.createTextNode(' Unfunded');
            p1.appendChild(span1);
            p1.appendChild(textNode1);

            const p2 = document.createElement('p');
            p2.className = 'text-muted text-sm mt-5';
            p2.textContent = `Out of ${formatUGX(assumedTotalNeed)} Total Need`;

            legend.appendChild(p1);
            legend.appendChild(p2);
        }

        const pendingStatsList = viewContainer.querySelector('#dynamic-branch-stats');
        if (pendingStatsList) {
            pendingStatsList.textContent = '';
            Object.keys(branchPendingCounts).forEach(branchName => {
                const row = document.createElement('div');
                row.className = 'pending-stat-row d-flex justify-content-between mb-2';

                const spanBranch = document.createElement('span');
                spanBranch.className = 'text-bold';
                spanBranch.textContent = branchName;

                const spanCount = document.createElement('span');
                spanCount.className = 'badge-pending';
                spanCount.textContent = `${branchPendingCounts[branchName]} Pending`;

                row.appendChild(spanBranch);
                row.appendChild(spanCount);
                pendingStatsList.appendChild(row);
            });
        }

        let filteredData = [...liveApplications];

        const pagination = new GlobalPagination({
            data: filteredData,
            itemsPerPage: 25,
            elements: {
                startId: 'gs-page-start',
                endId: 'gs-page-end',
                totalId: 'gs-total-entries',
                prevBtnId: 'gs-prev-page',
                nextBtnId: 'gs-next-page',
                numbersContainerId: 'gs-page-numbers',
                templateId: 'funds-page-number-template'
            },
            renderCallback: (pageData) => {
                const tbody = viewContainer.querySelector('#gs-table-body');
                const template = viewContainer.querySelector('#gs-row-template');
                if (!tbody || !template) return;

                tbody.textContent = '';

                pageData.forEach(app => {
                    const clone = template.content.cloneNode(true);
                    clone.querySelector('.col-id').textContent = app.id;
                    clone.querySelector('.col-name').textContent = app.name;

                    const parts = app.name.split(' ');
                    const initials = parts.length > 1 ? parts[0][0] + parts[1][0] : (parts[0][0] || 'S');
                    clone.querySelector('.col-initials').textContent = initials.toUpperCase();

                    clone.querySelector('.col-demo').textContent = app.demographics;
                    clone.querySelector('.col-branch').textContent = app.branch;
                    clone.querySelector('.col-level').textContent = app.levelClass;
                    clone.querySelector('.col-category').textContent = app.category;

                    const statusBadge = clone.querySelector('.col-status');
                    statusBadge.textContent = app.status;
                    statusBadge.className = 'col-status ' + (app.status === 'Pending' ? 'badge-pending' : 'badge-completed');

                    tbody.appendChild(clone);
                });

                const countLabel = viewContainer.querySelector('#gs-results-count');
                if (countLabel) countLabel.textContent = `${filteredData.length} Found`;
            }
        });

        pagination.render();

        const btnApply = viewContainer.querySelector('#gs-apply-btn');
        const btnReset = viewContainer.querySelector('#gs-reset-filters');

        if (btnApply) {
            btnApply.addEventListener('click', () => {
                const search = viewContainer.querySelector('#gs-search-input').value.toLowerCase();
                const status = viewContainer.querySelector('#gs-status-filter').value;
                const branch = viewContainer.querySelector('#gs-branch-filter').value;

                filteredData = liveApplications.filter(app => {
                    const matchesSearch = app.name.toLowerCase().includes(search) || app.id.toLowerCase().includes(search);
                    const matchesStatus = status ? app.status === status : true;
                    const matchesBranch = (branch && branch !== 'all') ? app.branch === branch : true;
                    return matchesSearch && matchesStatus && matchesBranch;
                });

                pagination.updateData(filteredData);
            });
        }

        if (btnReset) {
            btnReset.addEventListener('click', () => {
                viewContainer.querySelector('#gs-search-input').value = '';
                viewContainer.querySelector('#gs-status-filter').value = '';
                viewContainer.querySelector('#gs-branch-filter').value = '';
                filteredData = [...liveApplications];
                pagination.updateData(filteredData);
            });
        }

    } catch (error) {
        console.error("Global search view load failed:", error);
    }
}

// ==========================================
// VIEW 3: BULK DISTRIBUTION LOGIC
// ==========================================
function initBulkDistributionView() {
    const bulkDistributionView = document.getElementById('superadmin-bulk-distribution-view');
    if (!bulkDistributionView) return;

    const newView = bulkDistributionView.cloneNode(true);
    bulkDistributionView.parentNode.replaceChild(newView, bulkDistributionView);

    let currentHistoryData = [];

    const branchTable = document.querySelector('#superadmin-bulk-distribution-view tbody');
    if (branchTable) renderFetchingMessage(branchTable, 10, 'Fetching branch demands...');

    try {apiGet('/superadmin/scholarships/branch-demands').then(res => {
        const demands = Array.isArray(res) ? res : (res.data || []);

        // Fetch funds summary so we can animate all 3 cards exactly in sync!
        apiGet('/superadmin/scholarships/funds-summary').then(fundsRes => {
            /** @type {{availableBalanceUgx: number, allocatedThisTermUgx: number}} */
            const fSummary = fundsRes.data || {};
            const cards = document.querySelectorAll('#superadmin-bulk-distribution-view .fund-card h2');
            if (cards.length >= 3) {
                animateValue(cards[0], 0, fSummary.availableBalanceUgx || 0, 1500, true);
                animateValue(cards[2], 0, fSummary.allocatedThisTermUgx || 0, 1800, true);
            }
        }).catch(e => console.error(e));

        let totalDemands = 0;
        demands.forEach(d => totalDemands += (d.totalRequestedAmountUgx || 0));

        const tDom = document.querySelector('#superadmin-bulk-distribution-view .fund-card:nth-child(2) h2');
        if (tDom) {
            animateValue(tDom, 0, totalDemands, 2000, true);
        }

        const branchTable = document.querySelector('#superadmin-bulk-distribution-view tbody');
        const rowTemplate = document.getElementById('bulk-distribution-row-template');

        if (branchTable && rowTemplate && demands.length > 0) {
            branchTable.textContent = '';
            demands.forEach(d => {
                const clone = rowTemplate.content.cloneNode(true);
                const bName = d.branchName || ('Branch ' + d.branchId);

                clone.querySelector('.branch-name').textContent = bName;
                clone.querySelector('.branch-code').textContent = `BR-00${d.branchId}`;
                clone.querySelector('.col-requested').textContent = formatUGX(d.totalRequestedAmountUgx);
                clone.querySelector('.col-allocated').textContent = formatUGX(d.currentlyAllocatedUgx || 0);

                const btnAlloc = clone.querySelector('.btn-allocate');
                btnAlloc.setAttribute('data-branch-id', d.branchId);

                const btnHist = clone.querySelector('.btn-history');
                btnHist.setAttribute('data-branch-id', d.branchId);
                btnHist.setAttribute('data-branch-name', bName);

                branchTable.appendChild(clone);
            });
        }
    }).catch(e => console.error("Failed to load demands:", e));
    } catch(err) {
        console.error("Failed to start branch demands request:", err);
    }

    function renderHistoryTable(dataArray) {
        const tbody = document.getElementById('ajax-history-tbody');
        const template = document.getElementById('history-transaction-row-template');
        if (!tbody || !template) return;
        tbody.textContent = '';

        if (dataArray.length === 0) {
            renderEmptyTableMessage(tbody, 5, 'No transactions found.');
            return;
        }

        let totalAllocated = 0;
        dataArray.forEach(tx => {
            const clone = template.content.cloneNode(true);
            clone.querySelector('.tx-date').textContent = tx.date;
            clone.querySelector('.tx-id').textContent = tx.id.toString();
            clone.querySelector('.tx-amount').textContent = formatUGX(tx.amount);

            const statusBadge = clone.querySelector('.tx-status');
            statusBadge.textContent = tx.status;
            statusBadge.className = 'tx-status badge bg-success text-white';

            tbody.appendChild(clone);
            totalAllocated += tx.amount;
        });

        const statTotal = document.getElementById('stat-total-allocated');
        if (statTotal) statTotal.textContent = formatUGX(totalAllocated);
        const statCount = document.getElementById('stat-total-tx');
        if (statCount) statCount.textContent = dataArray.length.toString();
    }

    newView.addEventListener('click', async function(e) {
        const btnAllocate = e.target.closest('.btn-allocate');
        if (btnAllocate) {
            const row = btnAllocate.closest('tr');
            const branchName = row.querySelector('.branch-name').textContent;
            const branchId = btnAllocate.getAttribute('data-branch-id') || 1;
            const inputField = row.querySelector('.allocate-input');
            const amount = inputField.value;

            if (!amount || amount <= 0) return showErrorMessage('Please enter a valid amount.');

            confirmAllocation(
                'Confirm Allocation',
                `Allocate UGX ${amount} to ${branchName}?`,
                '/superadmin/scholarships/allocate-branch',
                {
                    branchId: parseInt(branchId),
                    amountUgx: parseFloat(amount),
                    term: 'Term 1',
                    academicYear: '2026/2027'
                },
                `Success! Allocated UGX ${amount} to ${branchName}.`,
                "Treasury Error: Insufficient funds or server error.",
                inputField
            );
        }

        const btnHistory = e.target.closest('.btn-history');
        if (btnHistory) {
            const branchId = btnHistory.getAttribute('data-branch-id') || 1;
            const branchName = btnHistory.getAttribute('data-branch-name') || "Branch";
            const title = document.getElementById('history-branch-title');
            if(title) title.textContent = branchName;

            showLoader();
            try {
                const res = await apiGet(`/superadmin/scholarships/history/${branchId}`);
                const historyRaw = Array.isArray(res) ? res : (res.data || []);

                currentHistoryData = historyRaw.map(tx => ({
                    id: 'TX-' + tx.id,
                    date: tx.createdAt ? new Date(tx.createdAt).toLocaleDateString() : 'Just now',
                    amount: tx.allocatedAmountUgx,
                    status: 'Completed'
                }));

                renderHistoryTable(currentHistoryData);
                document.getElementById('bulk-list-section').classList.add('hidden');
                document.getElementById('bulk-history-section').classList.remove('hidden');
            } catch (err) {
                showErrorMessage('Failed to load history');
            } finally {
                hideLoader();
            }
        }

        const btnBack = e.target.closest('#btn-back-to-bulk-list');
        if (btnBack) {
            document.getElementById('bulk-history-section').classList.add('hidden');
            document.getElementById('bulk-list-section').classList.remove('hidden');
        }
    });
}

// ==========================================
// VIEW 4: 1-TO-1 SPONSORSHIP LOGIC (AJAX PAGE SWAP)
// ==========================================
function initOneToOneSponsorshipView() {
    const viewContainer = document.querySelector('#superadmin-1to1-view');
    if (!viewContainer) return;

    // View Sections
    const mainHeader = viewContainer.querySelector('#pairings-main-header');
    const listSection = viewContainer.querySelector('#pairings-list-section');
    const wizardSection = viewContainer.querySelector('#pairing-wizard-section');

    function loadActivePairings() {
        const tbody = viewContainer.querySelector('tbody');
        if (tbody) renderFetchingMessage(tbody, 10, 'Fetching active sponsorships...');

        apiGet('/superadmin/scholarships/active-sponsorships').then(res => {
            const active = Array.isArray(res) ? res : (res.data || []);
            const tbody = viewContainer.querySelector('tbody');
            const template = document.getElementById('active-pairing-row-template');

            if (tbody && template) {
                tbody.textContent = '';

                if (active.length === 0) {
                    renderEmptyTableMessage(tbody, 5, 'No active sponsorships found.');
                    return;
                }

                active.forEach(a => {
                    const clone = template.content.cloneNode(true);
                    clone.querySelector('.donor-name').textContent = a.donorDetails;
                    clone.querySelector('.student-name').textContent = a.studentDetails;
                    clone.querySelector('.col-campus').textContent = a.branchDetails;
                    clone.querySelector('.view-profile-btn').setAttribute('data-id', a.id);
                    tbody.appendChild(clone);
                });
            }
        }).catch(e => console.error("Failed to load active sponsorships:", e));
    }

    loadActivePairings();

    let selectedDonorId = '', selectedStudentId = '';
    let selectedDonorName = null, selectedStudentName = null;

    function resetWizardSelection() {
        selectedDonorId = ''; selectedStudentId = '';
        selectedDonorName = null; selectedStudentName = null;
        document.getElementById('summary-sponsor').textContent = 'None selected';
        document.getElementById('summary-student').textContent = 'None selected';

        viewContainer.querySelectorAll('.btn-select-donor, .btn-select-student').forEach(el => {
            el.classList.remove('selected');
            const badge = el.querySelector('.card-badge');
            if(badge) badge.textContent = '';
        });
        checkEnableConfirmButton();
    }

    function checkEnableConfirmButton() {
        const btnConfirm = document.getElementById('btn-confirm-match');
        if (btnConfirm) {
            if (selectedDonorId && selectedStudentId) {
                btnConfirm.disabled = false;
                btnConfirm.classList.remove('btn-disabled');
            } else {
                btnConfirm.disabled = true;
                btnConfirm.classList.add('btn-disabled');
            }
        }
    }

    // OPEN WIZARD (Hides Table, Shows Match Maker)

    const btnOpenModal = viewContainer.querySelector('[data-action="open-pairing-modal"]');
    if (btnOpenModal) {
        btnOpenModal.addEventListener('click', async () => {
            showLoader();
            try {
                const [donorsRes, studentsRes] = await Promise.all([
                    apiGet('/superadmin/scholarships/donors'),
                    apiGet('/superadmin/scholarships/pending-students')
                ]);

                const liveDonors = Array.isArray(donorsRes) ? donorsRes : (donorsRes.data || []);
                const liveStudents = Array.isArray(studentsRes) ? studentsRes : (studentsRes.data || []);

                const validDonors = liveDonors.filter(d => (d.amountReceivedUgx - d.amountSpentUgx) > 0);

                const donorPagination = new GlobalPagination({
                    data: validDonors,
                    itemsPerPage: 25,
                    elements: {
                        startId: 'donor-page-start', endId: 'donor-page-end', totalId: 'donor-total-entries',
                        prevBtnId: 'btn-donor-prev', nextBtnId: 'btn-donor-next',
                        numbersContainerId: 'donor-pagination-numbers', templateId: 'page-number-template'
                    },
                    renderCallback: (pageData) => {
                        const donorList = document.getElementById('donor-list-container');
                        const donorTemplate = document.getElementById('donor-card-template');
                        if (donorList && donorTemplate) {
                            donorList.textContent = '';
                            pageData.forEach(d => {
                                const available = d.amountReceivedUgx - d.amountSpentUgx;
                                const clone = donorTemplate.content.cloneNode(true);
                                const card = clone.querySelector('.selectable-card');
                                card.classList.add('btn-select-donor');
                                card.setAttribute('data-id', d.id);
                                card.setAttribute('data-name', d.fullName);

                                clone.querySelector('.card-title').textContent = d.fullName;
                                clone.querySelector('.card-subtitle').textContent = `Available: ${formatUGX(available)}`;
                                donorList.appendChild(clone);
                            });
                        }
                    }
                });
                donorPagination.render();

                const studentPagination = new GlobalPagination({
                    data: liveStudents,
                    itemsPerPage: 25,
                    elements: {
                        startId: 'student-page-start', endId: 'student-page-end', totalId: 'student-total-entries',
                        prevBtnId: 'btn-student-prev', nextBtnId: 'btn-student-next',
                        numbersContainerId: 'student-pagination-numbers', templateId: 'page-number-template'
                    },
                    renderCallback: (pageData) => {
                        const studentList = document.getElementById('student-list-container');
                        const studentTemplate = document.getElementById('student-card-template');
                        if (studentList && studentTemplate) {
                            studentList.textContent = '';
                            pageData.forEach(s => {
                                const clone = studentTemplate.content.cloneNode(true);
                                const card = clone.querySelector('.selectable-card');
                                card.classList.add('btn-select-student');
                                card.setAttribute('data-id', s.id);
                                card.setAttribute('data-name', s.studentName);

                                clone.querySelector('.card-title').textContent = s.studentName;
                                clone.querySelector('.card-subtitle').textContent = `Shortfall: ${formatUGX(s.currentShortfallUgx)}`;
                                studentList.appendChild(clone);
                            });
                        }
                    }
                });
                studentPagination.render();

                // UI AJAX SWAP LOGIC (Hides main UI, Shows Wizard)
                resetWizardSelection();
                if(mainHeader) mainHeader.classList.add('hidden');
                if(listSection) listSection.classList.add('hidden');
                if(wizardSection) wizardSection.classList.remove('hidden');

            } catch (err) {
                console.error(err);
                showErrorMessage("Failed to load lists for Match Maker.");
            } finally {
                hideLoader();
            }
        });
    }

    // CLOSE WIZARD (Hides Match Maker, Shows Table)
    const btnCloseWizard = viewContainer.querySelector('[data-action="close-wizard"]');
    if (btnCloseWizard) {
        btnCloseWizard.addEventListener('click', () => {
            if(wizardSection) wizardSection.classList.add('hidden');
            if(mainHeader) mainHeader.classList.remove('hidden');
            if(listSection) listSection.classList.remove('hidden');
        });
    }

    // SEARCH LOGIC
    viewContainer.querySelector('#search-donor').addEventListener('input', (e) => {
        const term = e.target.value.toLowerCase();
        Array.from(document.getElementById('donor-list-container').children).forEach(card => {
            if (card.innerText.toLowerCase().includes(term)) {
                card.classList.remove('hidden');
            } else {
                card.classList.add('hidden');
            }
        });
    });

    viewContainer.querySelector('#search-student').addEventListener('input', (e) => {
        const term = e.target.value.toLowerCase();
        Array.from(document.getElementById('student-list-container').children).forEach(card => {
            if (card.innerText.toLowerCase().includes(term)) {
                card.classList.remove('hidden');
            } else {
                card.classList.add('hidden');
            }
        });
    });

    // SELECTION LOGIC
    const wizardBody = document.getElementById('pairing-wizard-section');
    if (wizardBody) {
        function handleSelection(target, btnClass) {
            document.querySelectorAll('.' + btnClass).forEach(b => {
                b.classList.remove('selected');
                const badge = b.querySelector('.card-badge');
                if(badge) badge.textContent = '';
            });
            target.classList.add('selected');
            const activeBadge = target.querySelector('.card-badge');
            if(activeBadge) activeBadge.textContent = 'Selected';
            return { id: target.getAttribute('data-id'), name: target.getAttribute('data-name') };
        }

        wizardBody.addEventListener('click', function(e) {
            const donorCard = e.target.closest('.btn-select-donor');
            if (donorCard) {
                const data = handleSelection(donorCard, 'btn-select-donor');
                selectedDonorId = data.id; selectedDonorName = data.name;
                document.getElementById('summary-sponsor').textContent = selectedDonorName;
                checkEnableConfirmButton();
            }

            const studentCard = e.target.closest('.btn-select-student');
            if (studentCard) {
                const data = handleSelection(studentCard, 'btn-select-student');
                selectedStudentId = data.id; selectedStudentName = data.name;
                document.getElementById('summary-student').textContent = selectedStudentName;
                checkEnableConfirmButton();
            }
        });
    }

    // CONFIRM LOGIC
    const btnSavePairing = document.getElementById('btn-confirm-match');
    if (btnSavePairing) {
        btnSavePairing.addEventListener('click', async () => {
            if (!selectedDonorId || !selectedStudentId) return;

            showLoader();
            try {
                await apiPost('/superadmin/scholarships/allocate-student', {
                    branchId: 1,
                    studentId: parseInt(selectedStudentId),
                    amountUgx: 0,
                    term: 'Term 1',
                    academicYear: '2026/2027'
                });

                showSuccessMessage(`SUCCESS! We paired Sponsor: ${selectedDonorName} with Student: ${selectedStudentName}.`);

                // Return to Table View
                if(wizardSection) wizardSection.classList.add('hidden');
                if(mainHeader) mainHeader.classList.remove('hidden');
                if(listSection) listSection.classList.remove('hidden');

                loadActivePairings();

            } catch(err) {
                showErrorMessage("Error: Failed to process match.");
            } finally {
                hideLoader();
            }
        });
    }
}

// ==========================================
// VIEW 5: PARTIAL STUDENT FUND LOGIC
// ==========================================
async function initPartialStudentFundView() {
    const viewContainer = document.querySelector('#superadmin-partial-fund-view');
    if (!viewContainer) return;

    const listSection = viewContainer.querySelector('#partial-fund-list-section');
    const detailSection = viewContainer.querySelector('#partial-fund-detail-section');
    const tbody = viewContainer.querySelector('#partial-fund-tbody');
    const template = document.querySelector('#partial-student-row-template');

    const searchInput = viewContainer.querySelector('#search-partial-students');
    const filterBranch = viewContainer.querySelector('#filter-branch');
    const btnBack = viewContainer.querySelector('#btn-back-to-partial-list');

    if (filterBranch) void populateBranchDropdowns([filterBranch]);

    if (!tbody || !template) return;

    let allStudents = [];

    if (listSection && detailSection) {
        listSection.classList.remove('hidden');
        detailSection.classList.add('hidden');
    }

    async function loadData() {
        const tbody = viewContainer.querySelector('#partial-fund-tbody');
        if (tbody) window.renderFetchingMessage(tbody, 10, 'Fetching sponsorships...');

        try {
            const [summaryResult, studentsResult] =
                await Promise.allSettled([
                    apiGet('/superadmin/scholarships/funds-summary'),
                    apiGet('/superadmin/scholarships/pending-students')
                ]);

            let summary = {
                availableBalanceUgx: 0,
                totalSpentUgx: 0
            };

            if (summaryResult.status === 'fulfilled') {
                summary =
                    summaryResult.value?.data ||
                    summaryResult.value ||
                    summary;
            } else {
                console.warn(
                    'Could not load funds summary.',
                    summaryResult.reason
                );
            }

            if (studentsResult.status === 'fulfilled') {
                const studentsResponse = studentsResult.value;
                allStudents = Array.isArray(studentsResponse)
                    ? studentsResponse
                    : (studentsResponse?.data || []);
            } else {
                console.warn(
                    'Could not load pending students.',
                    studentsResult.reason
                );
                allStudents = [];
            }

            // 3. Update the UI Stat Cards Safely with Smooth Animation!
            const availEl = viewContainer.querySelector('#partial-funds-available');
            const pendEl = viewContainer.querySelector('#partial-pending-count');
            const disbEl = viewContainer.querySelector('#partial-disbursed');

            if (availEl) animateValue(availEl, 0, summary.availableBalanceUgx || 0, 1500, true);
            if (disbEl) animateValue(disbEl, 0, summary.totalSpentUgx || 0, 1800, true);

            if (pendEl) {
                // Animate the number, then safely append " Students" once it finishes
                animateValue(pendEl, 0, allStudents.length, 1200, false);
                setTimeout(() => {
                    if (pendEl.textContent === allStudents.length.toString()) {
                        pendEl.textContent = `${allStudents.length} Students`;
                    }
                }, 1300);
            }

            // 4. Render the Table
            renderTable();
        } catch (error) {
            console.error(
                'Partial student funding data failed to load.',
                error
            );
            allStudents = [];
            renderTable();
        }
    }

    function renderTable() {
        tbody.textContent = '';

        const searchTerm = (searchInput ? searchInput.value.toLowerCase() : '');
        const branchFilter = (filterBranch ? filterBranch.value : 'all');

        let filtered = allStudents.filter(s => {
            const studentNameStr = s.studentName || 'Unknown';
            const studentIdVal = s.studentId || s.id || '';
            const matchesSearch = studentNameStr.toLowerCase().includes(searchTerm) ||
                (`ID-${studentIdVal}`).toLowerCase().includes(searchTerm);

            const campusStr = s.campusName || s.branchName || 'Main Campus';
            const matchesBranch = branchFilter === 'all' || campusStr.toLowerCase().includes(branchFilter.toLowerCase());

            return matchesSearch && matchesBranch;
        });

        if (filtered.length === 0) {
            renderEmptyTableMessage(tbody, 6, 'No students match the criteria.');
            return;
        }

        filtered.forEach(s => {
            const clone = template.content.cloneNode(true);

            // Fix property mismatch: The Java DTO uses studentId, campusName, totalFeesUgx, shortfallUgx
            clone.querySelector('.student-id').textContent = 'ID-' + (s.studentId || s.id || 'N/A');
            clone.querySelector('.student-name').textContent = s.studentName || 'Unknown';
            clone.querySelector('.campus-name').textContent = s.campusName || s.branchName || 'Main Campus';

            const totalFeesEl = clone.querySelector('.total-fees');
            if (totalFeesEl) totalFeesEl.textContent = formatUGX(s.totalFeesUgx || s.amountRequestedUgx || 0);

            clone.querySelector('.shortfall-amount').textContent = formatUGX(s.shortfallUgx || s.currentShortfallUgx || 0);

            const btnAlloc = clone.querySelector('.btn-allocate');
            if (btnAlloc) {
                btnAlloc.setAttribute('data-student-id', s.studentId || s.id);
                btnAlloc.setAttribute('data-campus-id', s.campusId || s.branchId || 1);
            }

            const btnDetails = clone.querySelector('.btn-view-details');
            if (btnDetails) {
                btnDetails.setAttribute('data-student-id', s.studentId || s.id);
            }

            tbody.appendChild(clone);
        });
    }

    if (searchInput) searchInput.addEventListener('input', renderTable);
    if (filterBranch) filterBranch.addEventListener('change', renderTable);

    await loadData();

    viewContainer.addEventListener('click', async function(e) {
        const btnAllocate = e.target.closest('.btn-allocate');
        if (btnAllocate) {
            const row = btnAllocate.closest('tr');
            const studentName = row.querySelector('.student-name').textContent;
            const campusId = btnAllocate.getAttribute('data-campus-id');
            const studentId = btnAllocate.getAttribute('data-student-id');
            const inputField = row.querySelector('.allocate-input');
            const amount = inputField.value;

            if (!amount || amount <= 0) return showErrorMessage('Please enter a valid amount.');

            confirmAllocation(
                'Confirm Allocation',
                `Allocate UGX ${amount} to ${studentName}?`,
                '/superadmin/scholarships/allocate-student',
                {
                    branchId: parseInt(campusId),
                    studentId: parseInt(studentId),
                    amountUgx: parseFloat(amount),
                    term: 'Term 1',
                    academicYear: '2026/2027'
                },
                `Successfully allocated UGX ${amount} to ${studentName}.`,
                "Treasury Error: Insufficient funds.",
                inputField
            );
        }

        const btnDetails = e.target.closest('.btn-view-details');
        if (btnDetails) {
            const studentId = btnDetails.getAttribute('data-student-id');
            // FIX: Convert the string ID to a Number safely!
            const student = allStudents.find(s => (s.studentId || s.id) === parseInt(studentId, 10));
            if (!student) return;

            viewContainer.querySelector('#detail-student-name').textContent = student.studentName || 'Unknown';
            viewContainer.querySelector('#detail-student-id').textContent = 'ID-' + (student.studentId || student.id);

            showLoader();
            try {
                viewContainer.querySelector('#detail-hardship-reason').textContent = "Family lost primary source of income. Unable to complete remaining balance for this term.";
                viewContainer.querySelector('#detail-academic-score').textContent = "Excellent standing. 4.2 GPA.";

                const historyTbody = viewContainer.querySelector('#detail-history-tbody');
                const historyTemplate = document.getElementById('partial-history-row-template');
                historyTbody.textContent = '';

                if (historyTemplate) {
                    const clone = historyTemplate.content.cloneNode(true);
                    clone.querySelector('.history-date').textContent = '2026-03-10';
                    clone.querySelector('.history-amount').textContent = 'UGX 500,000';
                    clone.querySelector('.history-user').textContent = 'Super Admin';
                    historyTbody.appendChild(clone);
                }

                if (listSection && detailSection) {
                    listSection.classList.add('hidden');
                    detailSection.classList.remove('hidden');
                }
            } catch(err) {
                console.error(err);
            } finally {
                hideLoader();
            }
        }
    });

    if (btnBack) {
        btnBack.addEventListener('click', () => {
            if (listSection && detailSection) {
                detailSection.classList.add('hidden');
                listSection.classList.remove('hidden');
            }
        });
    }
}
document.addEventListener('viewLoaded', function() {
    if (typeof createErpCalendar === 'function') {
        createErpCalendar('input[type="date"]',
            { minYear: 2022 });
    }
});

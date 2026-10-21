/* global apiGet, apiPost, apiPut, showSuccessMessage, showErrorMessage */

(() => {
    'use strict';

    const VIEW_NAME = 'academic-years';
    const API_PATH = '/academic-years';

    document.addEventListener('viewLoaded', event => {
        if (
            event.detail?.role !== 'admin'
            || event.detail?.view !== VIEW_NAME
        ) {
            return;
        }

        const initialization = initAcademicYearsView();

        if (
            initialization
            && typeof event.detail?.waitUntil === 'function'
        ) {
            event.detail.waitUntil(initialization);
        }
    });

    function initAcademicYearsView() {
        const view = document.querySelector('#ba-academic-years-view');

        if (!view || view.dataset.initialized === 'true') {
            return null;
        }

        view.dataset.initialized = 'true';

        const elements = collectElements(view);
        const state = {
            allRecords: [],
            filteredRecords: [],
            page: 0,
            size: Number(elements.pageSize?.value || 10),
            editingId: null,
            viewingRecord: null,
            returnToDetailsAfterEdit: false,
            viewOnly: false,
            busy: false,
            confirmationResolver: null,
            previousFocus: null
        };

        bindEvents(elements, state);
        resetForm(elements, state);
        hideOperationOverlay(elements);

        if (typeof window.erpRegisterModuleSync === 'function') {
            window.erpRegisterModuleSync(
                'academic-years',
                async () => {
                    if (!document.querySelector('#ba-academic-years-view')) return false;
                    await loadAcademicYears(elements, state);
                    return true;
                }
            );
        }

        return loadAcademicYears(elements, state);
    }

    function collectElements(view) {
        return {
            view,
            tableView: view.querySelector('#academic-year-tableView'),
            detailsView: view.querySelector('#academic-year-details-view'),
            detailsBackButton: view.querySelector('#academic-year-details-back-btn'),
            detailsEditButton: view.querySelector('#academic-year-details-edit-btn'),
            formView: view.querySelector('#academic-year-form-view'),
            tableBody: view.querySelector('#academic-year-table-body'),
            rowTemplate: view.querySelector('#academic-year-row-template'),
            loadingTemplate: view.querySelector('#academic-year-loading-template'),
            emptyTemplate: view.querySelector('#academic-year-empty-template'),

            addButton: view.querySelector('#btn-add-academic-year'),
            refreshButton: view.querySelector('#academic-year-refresh-btn'),
            backButton: view.querySelector('#academic-year-back-btn'),
            cancelButton: view.querySelector('#academic-year-cancel-btn'),
            saveButton: view.querySelector('#academic-year-save-btn'),

            filterForm: view.querySelector('#academic-year-filter-form'),
            searchInput: view.querySelector('#academic-year-search-input'),
            statusFilter: view.querySelector('#academic-year-status-filter'),
            activeFilter: view.querySelector('#academic-year-active-filter'),
            resetButton: view.querySelector('#academic-year-reset-btn'),

            pageSize: view.querySelector('#academic-year-page-size'),
            pageInfo: view.querySelector('#academic-year-page-info'),
            previousButton: view.querySelector('#academic-year-prev-btn'),
            nextButton: view.querySelector('#academic-year-next-btn'),

            currentCard: view.querySelector('#academic-year-current-card'),
            currentName: view.querySelector('#academic-year-current-name'),
            currentDates: view.querySelector('#academic-year-current-dates'),

            form: view.querySelector('#academic-year-form'),
            viewCode: view.querySelector('#ay-view-code'),
            viewName: view.querySelector('#ay-view-name'),
            viewStartDate: view.querySelector('#ay-view-start-date'),
            viewEndDate: view.querySelector('#ay-view-end-date'),
            viewAdmissionStartDate: view.querySelector('#ay-view-admission-start-date'),
            viewAdmissionEndDate: view.querySelector('#ay-view-admission-end-date'),
            viewStatus: view.querySelector('#ay-view-status'),
            viewCurrent: view.querySelector('#ay-view-current'),
            viewActive: view.querySelector('#ay-view-active'),
            viewDescription: view.querySelector('#ay-view-description'),
            formTitle: view.querySelector('#academic-year-form-title'),
            formSubtitle: view.querySelector('#academic-year-form-subtitle'),
            idInput: view.querySelector('#academic-year-id'),
            versionInput: view.querySelector('#academic-year-version'),
            codeInput: view.querySelector('#academic-year-code'),
            nameInput: view.querySelector('#academic-year-name'),
            startDateInput: view.querySelector('#academic-year-start-date'),
            endDateInput: view.querySelector('#academic-year-end-date'),
            admissionStartInput: view.querySelector('#academic-year-admission-start-date'),
            admissionEndInput: view.querySelector('#academic-year-admission-end-date'),
            statusInput: view.querySelector('#academic-year-status'),
            activeInput: view.querySelector('#academic-year-active'),
            currentInput: view.querySelector('#academic-year-current'),
            descriptionInput: view.querySelector('#academic-year-description'),
            formError: view.querySelector('#academic-year-form-error'),
            formErrorText: view.querySelector('#academic-year-form-error-text'),

            confirmationOverlay: view.querySelector('#academic-year-confirm-overlay'),
            confirmationDialog: view.querySelector('#academic-year-confirm-dialog'),
            confirmationTitle: view.querySelector('#academic-year-confirm-title'),
            confirmationMessage: view.querySelector('#academic-year-confirm-message'),
            confirmationSubmit: view.querySelector('#academic-year-confirm-submit'),
            confirmationCancel: view.querySelector('#academic-year-confirm-cancel'),

            overlay: view.querySelector('#academic-year-operation-overlay'),
            overlayTitle: view.querySelector('#academic-year-operation-title'),
            overlayMessage: view.querySelector('#academic-year-operation-message')
        };
    }

    function bindEvents(elements, state) {
        elements.detailsBackButton?.addEventListener('click', () => showTableView(elements));
        elements.detailsEditButton?.addEventListener('click', () => {
            if (!state.viewingRecord) return;
            state.returnToDetailsAfterEdit = true;
            populateForm(elements, state, state.viewingRecord);
            setFormMode(elements, state, 'edit');
            showFormView(elements, true);
        });

        elements.addButton?.addEventListener('click', () => {
            resetForm(elements, state);
            showFormView(elements, false);
        });

        elements.refreshButton?.addEventListener('click', () => {
            void loadAcademicYears(elements, state);
        });

        elements.backButton?.addEventListener('click', () => {
            if (state.returnToDetailsAfterEdit && state.viewingRecord) {
                state.returnToDetailsAfterEdit = false;
                showDetailsView(elements, state, state.viewingRecord);
                return;
            }
            showTableView(elements);
        });

        elements.cancelButton?.addEventListener('click', () => {
            if (state.returnToDetailsAfterEdit && state.viewingRecord) {
                state.returnToDetailsAfterEdit = false;
                showDetailsView(elements, state, state.viewingRecord);
                return;
            }
            showTableView(elements);
        });

        elements.filterForm?.addEventListener('submit', event => {
            event.preventDefault();
            state.page = 0;
            applyFiltersAndRender(elements, state);
        });

        elements.resetButton?.addEventListener('click', () => {
            elements.searchInput.value = '';
            elements.statusFilter.value = '';
            elements.activeFilter.value = '';
            state.page = 0;
            applyFiltersAndRender(elements, state);
        });

        elements.pageSize?.addEventListener('change', () => {
            state.size = Number(elements.pageSize.value || 10);
            state.page = 0;
            renderTable(elements, state);
        });

        elements.previousButton?.addEventListener('click', () => {
            if (state.page > 0) {
                state.page -= 1;
                renderTable(elements, state);
            }
        });

        elements.nextButton?.addEventListener('click', () => {
            const pageCount = getPageCount(state);

            if (state.page + 1 < pageCount) {
                state.page += 1;
                renderTable(elements, state);
            }
        });

        elements.form?.addEventListener('submit', event => {
            event.preventDefault();
            void saveAcademicYear(elements, state);
        });

        elements.startDateInput?.addEventListener('change', () => {
            syncDateLimits(elements);
        });

        elements.endDateInput?.addEventListener('change', () => {
            syncDateLimits(elements);
        });

        elements.confirmationSubmit?.addEventListener('click', () => {
            closeConfirmation(elements, state, true);
        });

        elements.confirmationCancel?.addEventListener('click', () => {
            closeConfirmation(elements, state, false);
        });

        elements.confirmationOverlay?.addEventListener('click', event => {
            if (event.target === elements.confirmationOverlay) {
                closeConfirmation(elements, state, false);
            }
        });

        elements.confirmationOverlay?.addEventListener('keydown', event => {
            if (event.key === 'Escape') {
                event.preventDefault();
                closeConfirmation(elements, state, false);
            }
        });
    }

    async function loadAcademicYears(elements, state) {
        if (state.busy) {
            return;
        }

        state.busy = true;
        showTableLoading(elements);

        try {
            const response = await apiGet(API_PATH);
            state.allRecords = normalizeArray(response?.data);
            state.page = 0;

            applyFiltersAndRender(elements, state);
            renderCurrentAcademicYear(elements, state.allRecords);
        } catch (error) {
            state.allRecords = [];
            state.filteredRecords = [];
            renderTableError(
                elements,
                readErrorMessage(error, 'Unable to load Academic Years.')
            );
            notifyError(
                readErrorMessage(error, 'Unable to load Academic Years.')
            );
        } finally {
            state.busy = false;
        }
    }

    function applyFiltersAndRender(elements, state) {
        const search = String(elements.searchInput?.value || '')
            .trim()
            .toLowerCase();
        const status = String(elements.statusFilter?.value || '').trim();
        const activeValue = String(elements.activeFilter?.value || '').trim();

        state.filteredRecords = state.allRecords.filter(record => {
            const matchesSearch =
                !search
                || String(record.academicYearCode || '')
                    .toLowerCase()
                    .includes(search)
                || String(record.academicYearName || '')
                    .toLowerCase()
                    .includes(search);

            const matchesStatus =
                !status
                || record.status === status;

            const matchesActive =
                !activeValue
                || String(Boolean(record.active)) === activeValue;

            return matchesSearch && matchesStatus && matchesActive;
        });

        const pageCount = getPageCount(state);

        if (state.page >= pageCount) {
            state.page = Math.max(pageCount - 1, 0);
        }

        renderTable(elements, state);
    }

    function renderTable(elements, state) {
        if (!elements.tableBody) {
            return;
        }

        elements.tableBody.replaceChildren();

        if (state.filteredRecords.length === 0) {
            appendTemplate(elements.tableBody, elements.emptyTemplate);
            updatePagination(elements, state);
            return;
        }

        const start = state.page * state.size;
        const pageRecords = state.filteredRecords.slice(
            start,
            start + state.size
        );

        pageRecords.forEach((record, index) => {
            const fragment = elements.rowTemplate?.content.cloneNode(true);
            const row = fragment?.querySelector('tr');

            if (!row) {
                return;
            }

            setText(row, '.col-serial', start + index + 1);
            setText(row, '.col-code', record.academicYearCode || '-');
            setText(row, '.col-name strong', record.academicYearName || '-');
            setText(
                row,
                '.col-duration',
                formatDateRange(record.startDate, record.endDate)
            );
            setText(
                row,
                '.col-admission-period',
                formatDateRange(
                    record.admissionStartDate,
                    record.admissionEndDate
                )
            );

            configureBadge(
                row.querySelector('.col-status'),
                record.status || 'UNKNOWN',
                statusClass(record.status)
            );

            configureBadge(
                row.querySelector('.col-current'),
                record.currentYear ? 'CURRENT' : 'NO',
                record.currentYear ? 'active' : 'inactive'
            );

            configureBadge(
                row.querySelector('.col-active'),
                record.active ? 'ACTIVE' : 'INACTIVE',
                record.active ? 'active' : 'inactive'
            );

            const editButton = row.querySelector('.academic-year-edit-btn');
            const viewButton = row.querySelector('.academic-year-view-btn');
            const currentButton = row.querySelector('.academic-year-current-btn');
            const activeButton = row.querySelector('.academic-year-active-btn');

            row.setAttribute('tabindex', '0');
            row.setAttribute('role', 'button');
            row.setAttribute('aria-label', `View Academic Year ${record.academicYearName || record.academicYearCode || ''}`);
            row.style.cursor = 'pointer';
            const openDetails = () => showDetailsView(elements, state, record);
            row.addEventListener('click', event => {
                if (event.target.closest('button, a, input, select, textarea')) return;
                openDetails();
            });
            row.addEventListener('keydown', event => {
                if (event.target !== row) return;
                if (event.key === 'Enter' || event.key === ' ') {
                    event.preventDefault();
                    openDetails();
                }
            });
            viewButton?.addEventListener('click', event => {
                event.stopPropagation();
                openDetails();
            });

            if (record.currentYear) {
                currentButton?.setAttribute(
                    'disabled',
                    'disabled'
                );
                currentButton?.setAttribute('data-tooltip',
                    'This is already the current Academic Year'
                );
            } else if (!record.active) {
                currentButton?.setAttribute(
                    'disabled',
                    'disabled'
                );
                currentButton?.setAttribute('data-tooltip',
                    'Activate this Academic Year before making it current'
                );
            } else if (
                String(record.status || '').toUpperCase() === 'CLOSED'
            ) {
                currentButton?.setAttribute(
                    'disabled',
                    'disabled'
                );
                currentButton?.setAttribute('data-tooltip',
                    'A closed Academic Year cannot be made current'
                );
            } else {
                currentButton?.addEventListener('click', () => {
                    void makeCurrent(
                        elements,
                        state,
                        record
                    );
                });
            }

            activeButton?.setAttribute('data-tooltip',
                record.active
                    ? 'Deactivate Academic Year'
                    : 'Activate Academic Year'
            );

            activeButton?.addEventListener('click', () => {
                void changeActiveStatus(elements, state, record);
            });

            elements.tableBody.appendChild(fragment);
        });

        updatePagination(elements, state);
    }

    function showDetailsView(elements, state, record) {
        state.viewingRecord = record;
        const set = (element, value) => { if (element) element.textContent = value || '-'; };
        set(elements.viewCode, record.academicYearCode);
        set(elements.viewName, record.academicYearName);
        set(elements.viewStartDate, formatDate(record.startDate));
        set(elements.viewEndDate, formatDate(record.endDate));
        set(elements.viewAdmissionStartDate, formatDate(record.admissionStartDate));
        set(elements.viewAdmissionEndDate, formatDate(record.admissionEndDate));
        set(elements.viewStatus, record.status);
        set(elements.viewCurrent, record.currentYear ? 'Yes' : 'No');
        set(elements.viewActive, record.active ? 'Active' : 'Inactive');
        set(elements.viewDescription, record.description);
        elements.tableView?.classList.add('hidden');
        elements.formView?.classList.add('hidden');
        elements.detailsView?.classList.remove('hidden');
        scrollMainContentToTop();
    }

    function populateForm(elements, state, record) {
        state.editingId = Number(record.academicYearId);

        elements.idInput.value = String(record.academicYearId ?? '');
        elements.versionInput.value = String(record.version ?? '');
        elements.codeInput.value = record.academicYearCode || '';
        elements.nameInput.value = record.academicYearName || '';
        // These fields are initialized by the shared ERP Flatpickr component
        // with altInput=true. The original inputs become hidden, so assigning
        // .value alone does not update the visible DD-MM-YYYY inputs.
        setCalendarDate(elements.startDateInput, record.startDate);
        setCalendarDate(elements.endDateInput, record.endDate);
        setCalendarDate(elements.admissionStartInput, record.admissionStartDate);
        setCalendarDate(elements.admissionEndInput, record.admissionEndDate);
        elements.statusInput.value = record.status || '';
        elements.activeInput.value = String(Boolean(record.active));
        elements.currentInput.value = String(Boolean(record.currentYear));
        elements.descriptionInput.value = record.description || '';

        elements.formTitle.textContent = 'Edit Academic Year';
        elements.formSubtitle.textContent =
            'Update this Academic Year for your branch.';

        clearFormError(elements);
        syncDateLimits(elements);
    }

    function resetForm(elements, state) {
        state.editingId = null;
        elements.form?.reset();

        elements.idInput.value = '';
        elements.versionInput.value = '';
        elements.activeInput.value = 'true';
        elements.currentInput.value = 'false';
        elements.statusInput.value = 'PLANNED';

        elements.formTitle.textContent = 'Create Academic Year';
        elements.formSubtitle.textContent =
            'Create an Academic Year for your branch.';

        setFormMode(elements, state, 'create');
        clearFormError(elements);
        syncDateLimits(elements);
    }

    function setFormMode(elements, state, mode) {
        const readOnly = mode === 'view';
        const creating = mode === 'create';
        state.viewOnly = readOnly;

        elements.form?.querySelectorAll('input:not([type="hidden"]), textarea')
            .forEach(field => {
                field.readOnly = readOnly;
                field.disabled = readOnly;
            });

        elements.form?.querySelectorAll('select')
            .forEach(field => {
                field.disabled = readOnly || field === elements.currentInput;
            });

        if (creating && elements.currentInput) {
            elements.currentInput.value = 'false';
        }

        const currentHelp = document.getElementById(
            'academic-year-current-help'
        );
        if (currentHelp) {
            currentHelp.hidden = !creating;
        }

        if (elements.saveButton) {
            elements.saveButton.hidden = readOnly;
            elements.saveButton.classList.toggle('hidden', readOnly);
        }
        if (elements.cancelButton) {
            elements.cancelButton.hidden = readOnly;
            elements.cancelButton.classList.toggle('hidden', readOnly);
        }

        if (mode === 'view') {
            elements.formTitle.textContent = 'View Academic Year';
            elements.formSubtitle.textContent =
                'Read-only details for this Academic Year.';
        } else if (mode === 'edit') {
            elements.formTitle.textContent = 'Edit Academic Year';
            elements.formSubtitle.textContent =
                'Update this Academic Year for your branch.';
        }
    }


    function cancelQueuedGlobalSync() {
        if (
            typeof window.erpCancelPendingDataSync
            === 'function'
        ) {
            window.erpCancelPendingDataSync();
        }
    }

    async function saveAcademicYear(elements, state) {
        if (state.viewOnly) {
            return;
        }

        clearFormError(elements);

        const validationMessage = validateForm(elements);

        if (validationMessage) {
            showFormError(elements, validationMessage);
            return;
        }

        const payload = buildPayload(elements);
        const editing = Number.isInteger(state.editingId) && state.editingId > 0;

        showOperationOverlay(
            elements,
            editing ? 'Updating Academic Year' : 'Creating Academic Year',
            'Please wait while the Academic Year is saved.'
        );

        disableButton(elements.saveButton, true);

        try {
            const response = editing
                ? await apiPut(`${API_PATH}/${state.editingId}`, payload)
                : await apiPost(API_PATH, payload);

            cancelQueuedGlobalSync();

            notifySuccess(
                response?.message
                || (
                    editing
                        ? 'Academic Year updated successfully.'
                        : 'Academic Year created successfully.'
                )
            );

            const returnToDetails = editing && state.returnToDetailsAfterEdit;
            state.returnToDetailsAfterEdit = false;
            await loadAcademicYears(elements, state);
            if (returnToDetails) {
                const updatedRecord = state.allRecords.find(item =>
                    Number(item.academicYearId) === Number(state.editingId)
                );
                if (updatedRecord) {
                    showDetailsView(elements, state, updatedRecord);
                } else {
                    showTableView(elements);
                }
            } else {
                showTableView(elements);
            }
        } catch (error) {
            const message = readErrorMessage(
                error,
                'Unable to save Academic Year.'
            );
            showFormError(elements, message);
            notifyError(message);
        } finally {
            disableButton(elements.saveButton, false);
            hideOperationOverlay(elements);
        }
    }

    async function makeCurrent(elements, state, record) {
        if (!record.active) {
            notifyError(
                'Activate this Academic Year before making it current.'
            );
            return;
        }

        if (
            String(record.status || '').toUpperCase() === 'CLOSED'
        ) {
            notifyError(
                'A closed Academic Year cannot be made current.'
            );
            return;
        }

        const confirmed = await showConfirmation(
            elements,
            state,
            {
                title: 'Make Current Academic Year',
                message:
                    `Make "${record.academicYearName}" the current Academic Year? `
                    + 'The current flag will be removed from any other Academic Year in this branch.',
                confirmText: 'Make Current',
                cancelText: 'Review Again'
            }
        );

        if (!confirmed) {
            return;
        }

        showOperationOverlay(
            elements,
            'Updating Current Academic Year',
            'Please wait while the current Academic Year is updated.'
        );

        try {
            const response = await apiPatch(
                `${API_PATH}/${record.academicYearId}/current`
            );

            cancelQueuedGlobalSync();

            notifySuccess(
                response?.message
                || 'Current Academic Year updated successfully.'
            );

            await loadAcademicYears(elements, state);
        } catch (error) {
            notifyError(
                readErrorMessage(
                    error,
                    'Unable to update the current Academic Year.'
                )
            );
        } finally {
            hideOperationOverlay(elements);
        }
    }

    async function changeActiveStatus(elements, state, record) {
        const nextActive = !Boolean(record.active);
        const action = nextActive ? 'activate' : 'deactivate';

        const confirmed = await showConfirmation(
            elements,
            state,
            {
                title: nextActive
                    ? 'Activate Academic Year'
                    : 'Deactivate Academic Year',
                message:
                    `Are you sure you want to ${action} `
                    + `"${record.academicYearName}"?`,
                confirmText: nextActive ? 'Activate' : 'Deactivate',
                cancelText: 'Cancel'
            }
        );

        if (!confirmed) {
            return;
        }

        showOperationOverlay(
            elements,
            nextActive
                ? 'Activating Academic Year'
                : 'Deactivating Academic Year',
            'Please wait while the record state is updated.'
        );

        try {
            const query = new URLSearchParams({
                active: String(nextActive),
                version: String(record.version ?? 0)
            });

            const response = await apiPatch(
                `${API_PATH}/${record.academicYearId}/active-status?${query}`
            );

            cancelQueuedGlobalSync();

            notifySuccess(
                response?.message
                || (
                    nextActive
                        ? 'Academic Year activated successfully.'
                        : 'Academic Year deactivated successfully.'
                )
            );

            await loadAcademicYears(elements, state);
        } catch (error) {
            notifyError(
                readErrorMessage(
                    error,
                    'Unable to change the Academic Year record state.'
                )
            );
        } finally {
            hideOperationOverlay(elements);
        }
    }

    async function apiPatch(endpoint) {
        const response = await fetch(
            `/api${endpoint}`,
            {
                method: 'PATCH',
                credentials: 'include',
                cache: 'no-store',
                headers: {
                    'Accept': 'application/json'
                }
            }
        );

        const text = await response.text();
        let body = null;

        try {
            body = text ? JSON.parse(text) : null;
        } catch (ignore) {
            body = text;
        }

        if (!response.ok) {
            const error = new Error(
                body?.message
                || `HTTP Error: ${response.status}`
            );
            error.status = response.status;
            error.data = body;
            throw error;
        }

        document.dispatchEvent(new CustomEvent('erp:data-mutated', {
            detail: { method: 'PATCH', endpoint, responseData: body, occurredAt: Date.now() }
        }));

        return body;
    }

    function buildPayload(elements) {
        return {
            academicYearCode: elements.codeInput.value.trim(),
            academicYearName: elements.nameInput.value.trim(),
            startDate: dateForApi(elements.startDateInput.value),
            endDate: dateForApi(elements.endDateInput.value),
            admissionStartDate: dateForApi(
                elements.admissionStartInput.value
            ),
            admissionEndDate: dateForApi(
                elements.admissionEndInput.value
            ),
            status: elements.statusInput.value,
            currentYear: elements.currentInput.value === 'true',
            description: valueOrNull(elements.descriptionInput.value.trim()),
            active: elements.activeInput.value === 'true',
            version: valueOrNull(elements.versionInput.value)
        };
    }

    function validateForm(elements) {
        const code = elements.codeInput.value.trim();
        const name = elements.nameInput.value.trim();
        const startDate = elements.startDateInput.value;
        const endDate = elements.endDateInput.value;
        const admissionStart = elements.admissionStartInput.value;
        const admissionEnd = elements.admissionEndInput.value;
        const status = elements.statusInput.value;

        if (!code) {
            elements.codeInput.focus();
            return 'Academic Year code is required.';
        }

        if (!/^[A-Za-z0-9][A-Za-z0-9_/-]*$/.test(code)) {
            elements.codeInput.focus();
            return 'Academic Year code contains unsupported characters.';
        }

        if (!name) {
            elements.nameInput.focus();
            return 'Academic Year name is required.';
        }

        if (!startDate || !endDate) {
            return 'Start Date and End Date are required.';
        }

        if (endDate < startDate) {
            elements.endDateInput.focus();
            return 'End Date cannot be before Start Date.';
        }

        if (
            admissionStart
            && admissionEnd
            && admissionEnd < admissionStart
        ) {
            elements.admissionEndInput.focus();
            return 'Admission End Date cannot be before Admission Start Date.';
        }

        if (!status) {
            elements.statusInput.focus();
            return 'Academic Year status is required.';
        }

        return '';
    }

    function syncDateLimits(elements) {
        const startDate = elements.startDateInput?.value || '';
        const endDate = elements.endDateInput?.value || '';

        if (elements.endDateInput) {
            elements.endDateInput.min = startDate;
        }

        // Admission can open before the academic year begins and can close
        // before the first academic day. Only the admission-window order is
        // validated; admission dates are not constrained to the academic year.
        if (elements.admissionStartInput) {
            elements.admissionStartInput.removeAttribute('min');
            elements.admissionStartInput.removeAttribute('max');
        }

        if (elements.admissionEndInput) {
            elements.admissionEndInput.removeAttribute('min');
            elements.admissionEndInput.removeAttribute('max');
        }
    }

    function renderCurrentAcademicYear(elements, records) {
        const current = records.find(record => {
            return Boolean(record.currentYear) && Boolean(record.active);
        });

        if (!current) {
            elements.currentCard?.classList.add('hidden');
            return;
        }

        elements.currentName.textContent =
            `${current.academicYearCode} - ${current.academicYearName}`;

        elements.currentDates.textContent =
            formatDateRange(current.startDate, current.endDate);

        elements.currentCard?.classList.remove('hidden');
    }

    function showFormView(elements, editing) {
        elements.detailsView?.classList.add('hidden');
        elements.tableView?.classList.add('hidden');
        elements.formView?.classList.remove('hidden');

        if (!editing) {
            elements.codeInput?.focus();
        }

        scrollMainContentToTop();
    }

    function showTableView(elements) {
        elements.detailsView?.classList.add('hidden');
        elements.formView?.classList.add('hidden');
        elements.tableView?.classList.remove('hidden');

        scrollMainContentToTop();
    }

    function scrollMainContentToTop() {
        const mainContent = document.getElementById('main-content-area');

        if (mainContent) {
            mainContent.scrollTo({ top: 0, behavior: 'smooth' });
            return;
        }

        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    function showTableLoading(elements) {
        if (!elements.tableBody) {
            return;
        }

        elements.tableBody.replaceChildren();
        appendTemplate(elements.tableBody, elements.loadingTemplate);
    }

    function renderTableError(elements, message) {
        if (!elements.tableBody) {
            return;
        }

        const row = document.createElement('tr');
        const cell = document.createElement('td');

        cell.colSpan = 9;
        cell.className = 'text-center py-4 text-danger';
        cell.textContent = message;

        row.appendChild(cell);
        elements.tableBody.replaceChildren(row);
    }

    function appendTemplate(container, template) {
        if (template instanceof HTMLTemplateElement) {
            container.appendChild(
                template.content.cloneNode(true)
            );
        }
    }

    function updatePagination(elements, state) {
        const total = state.filteredRecords.length;
        const pageCount = getPageCount(state);
        const currentPage = total === 0 ? 0 : state.page + 1;

        elements.pageInfo.textContent =
            total === 0
                ? 'No records'
                : `Showing page ${currentPage} of ${pageCount} `
                + `(${total} record${total === 1 ? '' : 's'})`;

        elements.previousButton.disabled =
            total === 0 || state.page <= 0;

        elements.nextButton.disabled =
            total === 0 || state.page + 1 >= pageCount;
    }

    function getPageCount(state) {
        return Math.max(
            Math.ceil(state.filteredRecords.length / state.size),
            1
        );
    }

    function configureBadge(element, text, className) {
        if (!element) {
            return;
        }

        element.textContent = text;
        element.className = `status-badge badge ${className}`;
    }

    function statusClass(status) {
        switch (String(status || '').toUpperCase()) {
            case 'ACTIVE':
                return 'active';
            case 'CLOSED':
                return 'inactive';
            case 'PLANNED':
                return 'pending';
            default:
                return 'inactive';
        }
    }

    function setText(parent, selector, value) {
        const element = parent.querySelector(selector);

        if (element) {
            element.textContent = String(value ?? '');
        }
    }

    function formatDateRange(startDate, endDate) {
        if (!startDate && !endDate) {
            return '-';
        }

        if (startDate && !endDate) {
            return formatDate(startDate);
        }

        if (!startDate && endDate) {
            return formatDate(endDate);
        }

        return `${formatDate(startDate)} - ${formatDate(endDate)}`;
    }

    function formatDate(value) {
        if (!value) return '-';
        const text = String(value).trim();
        let match = text.match(/^(\d{4})-(\d{2})-(\d{2})/);
        if (match) return `${match[3]}-${match[2]}-${match[1]}`;
        match = text.match(/^(\d{1,2})[/-](\d{1,2})[/-](\d{4})$/);
        if (match) return `${match[1].padStart(2, '0')}-${match[2].padStart(2, '0')}-${match[3]}`;
        return text;
    }


    function setCalendarDate(input, value) {
        if (!input) return;
        const isoDate = dateForInput(value);

        if (input._flatpickr) {
            input._flatpickr.setDate(isoDate || null, false, 'Y-m-d');
            return;
        }

        // Fallback if the shared calendar has not initialized this field.
        input.value = isoDate;
        input.dispatchEvent(new Event('input', { bubbles: true }));
        input.dispatchEvent(new Event('change', { bubbles: true }));
    }

    function dateForInput(value) {
        if (!value) return '';
        const text = String(value).trim();
        let match = text.match(/^(\d{4})-(\d{2})-(\d{2})/);
        if (match) return `${match[1]}-${match[2]}-${match[3]}`;
        match = text.match(/^(\d{1,2})[/-](\d{1,2})[/-](\d{4})$/);
        if (match) return `${match[3]}-${match[2].padStart(2, '0')}-${match[1].padStart(2, '0')}`;
        return '';
    }

    function dateForApi(value) {
        if (!value) return null;
        const match = String(value).match(/^(\d{4})-(\d{2})-(\d{2})$/);
        return match ? `${match[3]}-${match[2]}-${match[1]}` : null;
    }


    function normalizeArray(value) {
        if (Array.isArray(value)) {
            return value;
        }

        if (Array.isArray(value?.content)) {
            return value.content;
        }

        return [];
    }

    function valueOrNull(value) {
        if (
            value === undefined
            || value === null
            || String(value).trim() === ''
        ) {
            return null;
        }

        return value;
    }

    function showConfirmation(elements, state, options) {
        const {
            title = 'Confirm Action',
            message = 'Please confirm this action.',
            confirmText = 'Confirm',
            cancelText = 'Cancel'
        } = options || {};

        if (
            !elements.confirmationOverlay
            || !elements.confirmationDialog
            || !elements.confirmationTitle
            || !elements.confirmationMessage
            || !elements.confirmationSubmit
            || !elements.confirmationCancel
        ) {
            console.error('Academic Year confirmation dialog elements are missing.');
            return Promise.resolve(false);
        }

        if (state.confirmationResolver) {
            return Promise.resolve(false);
        }

        elements.confirmationTitle.textContent = title;
        elements.confirmationMessage.textContent = message;
        elements.confirmationSubmit.textContent = confirmText;
        elements.confirmationCancel.textContent = cancelText;

        state.previousFocus =
            document.activeElement instanceof HTMLElement
                ? document.activeElement
                : null;

        elements.confirmationOverlay.classList.add('is-visible');
        elements.confirmationOverlay.setAttribute('aria-hidden', 'false');
        document.body.classList.add('modal-open');

        window.requestAnimationFrame(() => {
            elements.confirmationDialog.focus();
        });

        return new Promise(resolve => {
            state.confirmationResolver = value => resolve(Boolean(value));
        });
    }

    function closeConfirmation(elements, state, accepted) {
        if (!state.confirmationResolver) {
            return;
        }

        elements.confirmationOverlay?.classList.remove('is-visible');
        elements.confirmationOverlay?.setAttribute('aria-hidden', 'true');
        document.body.classList.remove('modal-open');

        const resolver = state.confirmationResolver;
        state.confirmationResolver = null;

        window.setTimeout(() => {
            if (state.previousFocus?.isConnected) {
                state.previousFocus.focus();
            }

            state.previousFocus = null;
            resolver(Boolean(accepted));
        }, 180);
    }

    function showOperationOverlay(elements, title, message) {
        if (!elements.overlay) {
            return;
        }

        elements.overlayTitle.textContent = title;
        elements.overlayMessage.textContent = message;
        elements.overlay.classList.add('is-visible');
        elements.overlay.setAttribute('aria-hidden', 'false');
        document.body.classList.add('modal-open');
    }

    function hideOperationOverlay(elements) {
        if (!elements.overlay) {
            return;
        }

        elements.overlay.classList.remove('is-visible');
        elements.overlay.setAttribute('aria-hidden', 'true');
        document.body.classList.remove('modal-open');
    }

    function showFormError(elements, message) {
        elements.formErrorText.textContent = message;
        elements.formError.classList.remove('hidden');
    }

    function clearFormError(elements) {
        elements.formErrorText.textContent = '';
        elements.formError.classList.add('hidden');
    }

    function disableButton(button, disabled) {
        if (button) {
            button.disabled = disabled;
        }
    }

    function notifySuccess(message) {
        if (typeof showSuccessMessage === 'function') {
            showSuccessMessage(message);
            return;
        }

        console.log(message);
    }

    function notifyError(message) {
        if (typeof showErrorMessage === 'function') {
            showErrorMessage(message);
            return;
        }

        console.error(message);
    }

    function readErrorMessage(error, fallback) {
        if (error?.data?.errors && typeof error.data.errors === 'object') {
            return Object.values(error.data.errors)
                .filter(Boolean)
                .join('\n');
        }

        return error?.data?.message
            || error?.message
            || fallback;
    }
})();

/* global apiGet, apiPost, apiPut, apiDelete, showErrorMessage, showLoader, hideLoader */
(() => {
    'use strict';

    const ROOT_SELECTOR = '#ba-fee-structures-view';
    const API = {
        structures: '/branchadmin/fees/structures',
        independent: '/branchadmin/fees/structures/independent-items',
        years: '/academic-years?active=true',
        referenceData: '/students/reference-data',
        classes: (yearId, levelId) => {
            const params = new URLSearchParams({ levelId: String(levelId) });
            if (yearId) params.set('academicYearId', String(yearId));
            return `/branchadmin/fees/structures/available-classes?${params.toString()}`;
        }
    };

    let root;
    let state;
    const FEE_TYPES = ['TUITION', 'REGISTRATION', 'ADMISSION', 'EXAMINATION', 'TRANSPORT', 'BOARDING', 'DEVELOPMENT', 'LIBRARY', 'SPORTS', 'UNIFORM', 'OTHER'];

    document.addEventListener('viewLoaded', event => {
        if (event.detail?.role !== 'admin' || event.detail?.view !== 'fee-structures') return;
        const promise = initialize(event.detail.viewElement);
        if (typeof event.detail.waitUntil === 'function') event.detail.waitUntil(promise);
        else promise.catch(error => {
            console.error('Fee Structures initialization failed.', error);
            notifyError('Fee Structures could not be loaded. Please refresh the page.');
        });
    });

    async function initialize(viewElement) {
        root = viewElement?.querySelector(ROOT_SELECTOR) || document.querySelector(ROOT_SELECTOR);
        if (!root || root.dataset.initialized === 'true' || root.dataset.initialized === 'initializing') return;
        root.dataset.initialized = 'initializing';
        state = { source: 'MANDATORY', page: 0, size: Number(el('#fee-structures-page-size').value) || 10, totalPages: 0, totalElements: 0, requestId: 0, loading: false, saving: false, years: [], levels: [], selectedRecord: null, editingRecord: null, confirmationResolver: null, previousFocus: null, appliedFilters: { source: '', academicYearId: '', levelId: '', classId: '', status: '' } };
        bindEvents();
        renderMessage('Loading fees...');
        setSelectLabel('#fee-structures-filter-year', 'Loading academic years...', true);
        setSelectLabel('#fee-structures-filter-level', 'Loading levels...', true);

        const [yearsResult, referenceResult] = await Promise.allSettled([
            apiGet(API.years, { silent: true }),
            apiGet(API.referenceData, { silent: true })
        ]);
        if (yearsResult.status === 'fulfilled') {
            state.years = asList(yearsResult.value);
            fillSelect('#fee-structures-filter-year', state.years, 'All Academic Years', ['academicYearId', 'id'], ['academicYearName', 'yearName', 'name', 'academicYearCode']);
        } else setSelectError('#fee-structures-filter-year', 'Unable to load academic years');

        if (referenceResult.status === 'fulfilled') {
            const reference = unwrapObject(referenceResult.value);
            state.levels = asList(reference.levels || reference.levelOptions || reference.availableLevels);
            fillSelect('#fee-structures-filter-level', state.levels, 'All Levels', ['levelId', 'id'], ['levelName', 'name', 'levelCode']);
            fillSelect(root.querySelector('#fee-create-year'), state.years, 'Select Academic Year', ['academicYearId', 'id'], ['academicYearName', 'yearName', 'name', 'academicYearCode']);
            fillSelect(root.querySelector('#fee-create-level'), state.levels, 'Select Level', ['levelId', 'id'], ['levelName', 'name', 'levelCode']);
        } else {
            setSelectError('#fee-structures-filter-level', 'Unable to load levels');
            showCreateError('Level options could not be loaded. Refresh the page and try again.');
        }
        await loadTable();
        root.dataset.initialized = 'true';
        if (yearsResult.status === 'rejected' || referenceResult.status === 'rejected') notifyError('Some reference data could not be loaded. Use Refresh to retry.');
    }

    function bindEvents() {
        el('#fee-structures-confirm-submit').addEventListener('click', () => closeConfirmation(true));
        el('#fee-structures-confirm-cancel').addEventListener('click', () => closeConfirmation(false));
        el('#fee-structures-confirm-overlay').addEventListener('click', event => {
            if (event.target === el('#fee-structures-confirm-overlay')) closeConfirmation(false);
        });
        el('#fee-structures-confirm-overlay').addEventListener('keydown', event => {
            if (event.key === 'Escape') {
                event.preventDefault();
                closeConfirmation(false);
            }
        });
        el('#fee-structures-create-mandatory').addEventListener('click', () => openCreateForm('MANDATORY'));
        el('#fee-structures-create-optional').addEventListener('click', () => openCreateForm('OPTIONAL'));
        el('#fee-structures-form-back').addEventListener('click', handleFormBack);
        el('#fee-mandatory-view-back').addEventListener('click', closeViewPanel);
        el('#fee-optional-view-back').addEventListener('click', closeViewPanel);
        el('#fee-mandatory-view-edit').addEventListener('click', () => { if (state.selectedRecord) void openEditForm(state.selectedRecord, 'MANDATORY'); });
        el('#fee-mandatory-view-publish').addEventListener('click', () => void runMandatoryWorkflowAction('publish'));
        el('#fee-mandatory-view-archive').addEventListener('click', () => void runMandatoryWorkflowAction('archive'));
        el('#fee-optional-view-edit').addEventListener('click', () => { if (state.selectedRecord) void openEditForm(state.selectedRecord, 'OPTIONAL'); });
        // Cancel/back must preserve the selected record when the form is editing.
        el('#fee-create-cancel').addEventListener('click', handleFormBack);
        el('#fee-optional-cancel').addEventListener('click', handleFormBack);
        el('#fee-structures-create-form').addEventListener('submit', event => void submitCreateForm(event));
        el('#fee-structures-optional-form').addEventListener('submit', event => void submitCreateForm(event));
        root.querySelector('#fee-create-year').addEventListener('change', () => { state.source = 'MANDATORY'; void loadCreateClasses(); });
        root.querySelector('#fee-create-level').addEventListener('change', () => { state.source = 'MANDATORY'; void loadCreateClasses(); });
        root.querySelector('#fee-create-start-class').addEventListener('change', () => { state.source = 'MANDATORY';
            const end = el('#fee-create-end-class');
            end.disabled = !el('#fee-create-start-class').value;
            if (!el('#fee-create-start-class').value) end.value = '';
        });
        el('#fee-structures-filter-form').addEventListener('submit', event => { event.preventDefault(); captureAppliedFilters(); state.page = 0; void loadTable(); });
        el('#fee-structures-refresh').addEventListener('click', () => void refreshAll());
        el('#fee-structures-reset-filters').addEventListener('click', async () => {
            el('#fee-structures-filter-source').value = '';
            el('#fee-structures-filter-year').value = '';
            el('#fee-structures-filter-level').value = '';
            el('#fee-structures-filter-class').value = '';
            el('#fee-structures-filter-status').value = '';
            captureAppliedFilters();
            state.page = 0;
            await loadFilterClasses();
            await loadTable();
        });
        // Filter controls stage selections only. The table changes only after Apply Filters.
        // Year/Level may refresh the linked Class choices, but must not query the table.
        el('#fee-structures-filter-year').addEventListener('change', async () => {
            el('#fee-structures-filter-class').value = '';
            await loadFilterClasses();
        });
        el('#fee-structures-filter-level').addEventListener('change', async () => {
            el('#fee-structures-filter-class').value = '';
            await loadFilterClasses();
        });
        el('#fee-structures-page-size').addEventListener('change', () => { state.size = Number(el('#fee-structures-page-size').value) || 10; state.page = 0; void loadTable(); });
        el('#fee-structures-prev').addEventListener('click', () => { if (!state.loading && state.page > 0) { state.page--; void loadTable(); } });
        el('#fee-structures-next').addEventListener('click', () => { if (!state.loading && state.page + 1 < state.totalPages) { state.page++; void loadTable(); } });
        el('#fee-structures-table-body').addEventListener('click', event => void handleRowAction(event));
        el('#fee-structures-table-body').addEventListener('change', event => {
            const toggle = event.target.closest('input[data-action="set-active"]');
            if (toggle) void handleActiveToggle(toggle);
        });
        el('#fee-structures-table-body').addEventListener('keydown', event => {
            if ((event.key === 'Enter' || event.key === ' ') && event.target.closest('tr[data-fee-row="true"]') && !event.target.closest('button, a, input, select, textarea')) {
                event.preventDefault(); void openRowView(event.target.closest('tr[data-fee-row="true"]'));
            }
        });
    }

    async function openCreateForm(source) {
        state.source = source;
        state.editingRecord = null;
        el('#fee-structures-list-view').classList.add('hidden');
        el('#fee-structures-form-view').classList.remove('hidden');
        el('#fee-structures-form-title').textContent = source === 'MANDATORY' ? 'Create Mandatory Fee' : 'Create Optional Fee';
        root.querySelector('#fee-structures-create-form').classList.toggle('hidden', source !== 'MANDATORY');
        el('#fee-structures-optional-form-wrap').classList.toggle('hidden', source !== 'OPTIONAL');
        resetCreateForm();
        const mandatorySave = el('#fee-create-save'); if (mandatorySave) mandatorySave.innerHTML = '<i class="bi bi-check-circle"></i> Save Fee';
        const optionalSave = el('#fee-optional-save'); if (optionalSave) optionalSave.innerHTML = '<i class="bi bi-check-circle"></i> Save Optional Fee';
        if (source === 'MANDATORY') await loadCreateClasses();
    }

    function handleFormBack() {
        // One shared back path for the header Back button and both form Cancel
        // buttons. Existing records return to their own details panel; new
        // records return to the list.
        closeCreateForm();
    }

    function closeCreateForm() {
        const wasEditing = Boolean(state?.editingRecord && state?.selectedRecord);
        const record = wasEditing ? { ...state.selectedRecord } : null;
        const source = state?.source || 'MANDATORY';

        el('#fee-structures-form-view').classList.add('hidden');
        clearCreateError();

        if (wasEditing && record) {
            // Clear edit mode only after preserving the selected record.
            state.editingRecord = null;
            showViewPanel(record, source);
            return;
        }

        state.editingRecord = null;
        el('#fee-structures-list-view').classList.remove('hidden');
        el('#fee-structures-create-mandatory').classList.remove('hidden');
        el('#fee-structures-create-optional').classList.remove('hidden');
        el('#fee-structures-refresh').classList.remove('hidden');
        el('#fee-mandatory-view-panel').classList.add('hidden');
        el('#fee-optional-view-panel').classList.add('hidden');
    }

    function resetCreateForm() {
        const mandatoryForm = el('#fee-structures-create-form');
        const optionalForm = el('#fee-structures-optional-form');
        if (mandatoryForm) mandatoryForm.reset();
        if (optionalForm) optionalForm.reset();
        clearCreateError();
        // The two forms are independent. Some page versions may omit fields
        // that do not apply to the active form, so reset helpers must be null-safe.
        setSelectLabel('#fee-create-start-class', 'Select Level First', true);
        setSelectLabel('#fee-create-end-class', 'Same as Start Class', true);
        if (state) state.editingRecord = null;
    }

    async function loadCreateClasses() {
        const year = el('#fee-create-year').value;
        const level = el('#fee-create-level').value;
        const start = el('#fee-create-start-class');
        const end = el('#fee-create-end-class');
        if (!level) {
            setSelectLabel(start, 'Select Level First', true);
            setSelectLabel(end, 'Select Start Class First', true);
            return;
        }
        setSelectLabel(start, 'Loading classes...', true);
        setSelectLabel(end, 'Select Start Class First', true);
        try {
            const result = await apiGet(API.classes(year, level), { silent: true });
            if (year !== el('#fee-create-year').value || level !== el('#fee-create-level').value) return;
            const classes = asList(result);
            fillSelect(start, classes, 'Select Start Class', ['classId', 'id'], ['className', 'name', 'classCode']);
            fillSelect(end, classes, 'Same as Start Class', ['classId', 'id'], ['className', 'name', 'classCode']);
            end.disabled = !start.value;
        } catch (error) {
            setSelectError(start, 'Unable to load classes');
            setSelectError(end, 'Unable to load classes');
            showCreateError(error?.message || 'Unable to load classes for the selected level.');
        }
    }

    async function loadFilterClasses() {
        const year = el('#fee-structures-filter-year').value;
        const level = el('#fee-structures-filter-level').value;
        const select = el('#fee-structures-filter-class');
        if (!level) { setSelectLabel(select, 'All Classes', false); return; }
        setSelectLabel(select, 'Loading classes...', true);
        try {
            const result = await apiGet(API.classes(year, level), { silent: true });
            if (year !== el('#fee-structures-filter-year').value || level !== el('#fee-structures-filter-level').value) return;
            fillSelect(select, asList(result), 'All Classes', ['classId', 'id'], ['className', 'name', 'classCode']);
        } catch (error) { setSelectError(select, 'Unable to load classes'); console.error(error); }
    }

    async function submitCreateForm(event) {
        event.preventDefault();
        if (state.saving) return;
        clearCreateError();
        const source = state.source;
        const name = el('#fee-create-name').value.trim();
        const code = el('#fee-create-code').value.trim();
        const type = el('#fee-create-type').value;
        const amountText = el('#fee-create-amount').value;
        const frequency = el('#fee-create-frequency').value;
        if (!type || !name || !code || amountText === '' || !frequency || Number(amountText) < 0 || !Number.isFinite(Number(amountText))) {
            showCreateError('Complete all required fee fields. Amount must be zero or greater.'); return;
        }
        const common = { feeType: type, feeCode: code, feeName: name, amount: Number(amountText), chargingFrequency: frequency, remarks: el('#fee-create-remarks').value.trim() || null };
        if (source === 'OPTIONAL') common.structureStatus = el('#fee-optional-structure-status').value;
        let endpoint;
        let payload;
        if (source === 'MANDATORY') {
            const year = el('#fee-create-year').value;
            const level = el('#fee-create-level').value;
            const startClass = el('#fee-create-start-class').value;
            if (!year || !level || !startClass) { showCreateError('Select an academic year, level, and start class for a mandatory fee.'); return; }
            payload = { ...common, academicYearId: Number(year), levelId: Number(level), startClassId: Number(startClass), endClassId: el('#fee-create-end-class').value ? Number(el('#fee-create-end-class').value) : null };
            endpoint = state.editingRecord ? `${API.structures}/${encodeURIComponent(state.editingRecord.id)}` : API.structures;
        } else {
            payload = common;
            endpoint = state.editingRecord ? `${API.independent}/${encodeURIComponent(state.editingRecord.id)}` : API.independent;
        }
        const button = el('#fee-create-save');
        state.saving = true; button.disabled = true; button.textContent = 'Saving...';
        const wasEditing = Boolean(state.editingRecord);
        try {
            if (wasEditing) await apiPut(endpoint, payload);
            else await apiPost(endpoint, payload);
            state.editingRecord = null;
            state.selectedRecord = null;
            closeCreateForm();
            state.page = 0;
            await loadTable();
            notifySuccess(wasEditing ? 'Fee updated successfully.' : (source === 'MANDATORY' ? 'Mandatory fee created successfully.' : 'Optional fee created successfully.'));
        } catch (error) {
            showCreateError(error?.message || 'Fee could not be saved.');
        } finally {
            state.saving = false; button.disabled = false; button.innerHTML = state.editingRecord ? '<i class="bi bi-check-circle"></i> Save Changes' : (source === 'MANDATORY' ? '<i class="bi bi-check-circle"></i> Save Fee' : '<i class="bi bi-check-circle"></i> Save Optional Fee');
        }
    }

    function captureAppliedFilters() {
        state.appliedFilters = {
            source: el('#fee-structures-filter-source').value,
            academicYearId: el('#fee-structures-filter-year').value,
            levelId: el('#fee-structures-filter-level').value,
            classId: el('#fee-structures-filter-class').value,
            status: el('#fee-structures-filter-status').value
        };
    }

    async function loadTable() {
        if (!root || !state) return;
        const requestId = ++state.requestId;
        const body = el('#fee-structures-table-body');
        const hasRows = !!body?.querySelector('tr[data-fee-row="true"]');
        state.loading = true; setTableBusy(true);
        // Keep the current rows visible during background refreshes; only show
        // the initial loading state when there is nothing useful to display.
        if (!hasRows) renderMessage('Loading fees...');
        const params = new URLSearchParams();
        // Pagination, page-size changes, refreshes, and toggle updates reuse
        // the last explicitly applied filters, never the unsaved form selections.
        Object.entries(state.appliedFilters || {}).forEach(([key, rawValue]) => {
            const value = String(rawValue ?? '').trim();
            if (value) params.set(key, value);
        });
        params.set('page', String(state.page)); params.set('size', String(state.size));
        try {
            const response = await apiGet(`${API.structures}?${params.toString()}`, { silent: true });
            if (requestId !== state.requestId) return;
            const payload = unwrapObject(response);
            const rows = asList(Array.isArray(payload) ? payload : (payload.items || payload.content || payload.records));
            state.totalElements = numberOr(payload.totalElements ?? payload.totalCount, rows.length);
            state.totalPages = numberOr(payload.totalPages, state.totalElements ? Math.ceil(state.totalElements / state.size) : 0);
            renderRows(rows); updatePagination();
        } catch (error) {
            if (requestId !== state.requestId) return;
            console.error('Fee list request failed.', error);
            renderMessage('Fees could not be loaded. Use Refresh to try again.');
            state.totalElements = 0; state.totalPages = 0; updatePagination();
            notifyError(error?.message || 'Unable to load fees.');
        } finally {
            if (requestId === state.requestId) { state.loading = false; setTableBusy(false); }
        }
    }


    async function handleActiveToggle(toggle) {
        if (!toggle || toggle.disabled) return;
        const id = toggle.dataset.id;
        const source = toggle.dataset.source;
        const previousActive = toggle.dataset.active === 'true';
        const nextActive = toggle.checked;
        if (!id || !source || previousActive === nextActive) return;
        toggle.disabled = true;
        try {
            const endpoint = source === 'MANDATORY'
                ? `${API.structures}/${encodeURIComponent(id)}/active?active=${nextActive}`
                : `${API.independent}/${encodeURIComponent(id)}/active?active=${nextActive}`;
            await apiPost(endpoint, {});
            toggle.dataset.active = String(nextActive);
            toggle.setAttribute('aria-label', `${nextActive ? 'Deactivate' : 'Activate'} fee`);
            toggle.title = nextActive ? 'On — click to deactivate fee' : 'Off — click to activate fee';
            notifySuccess(nextActive ? 'Fee activated.' : 'Fee deactivated.');
            await loadTable();
        } catch (error) {
            toggle.checked = previousActive;
            notifyError(error?.message || 'Unable to change the fee activation state.');
        } finally {
            toggle.disabled = false;
        }
    }

    async function handleRowAction(event) {
        const switchInput = event.target.closest('input[data-action="set-active"]');
        if (switchInput) {
            event.stopPropagation();
            return;
        }
        const button = event.target.closest('button[data-action]');
        if (button) {
            event.preventDefault();
            event.stopPropagation();
            if (button.disabled) return;
            const id = button.dataset.id;
            const action = button.dataset.action;
            const source = button.dataset.source;
            if (!id || !action) return;
            if (action === 'publish' && !await confirmAction({
                title: 'Publish Fee Structure',
                message: 'Publish this mandatory fee? Published fees cannot be edited directly.',
                confirmText: 'Publish'
            })) return;
            if (action === 'archive' && !await confirmAction({
                title: 'Archive Fee Structure',
                message: 'Archive this mandatory fee? The record and financial history will be retained.',
                confirmText: 'Archive'
            })) return;
            if (action === 'deactivate' && !await confirmAction({
                title: 'Deactivate Optional Fee',
                message: 'Deactivate this optional fee? The record will be retained.',
                confirmText: 'Deactivate'
            })) return;
            if (action === 'edit') {
                try {
                    const record = await fetchFeeRecord(id, source);
                    await openEditForm(record, source);
                } catch (error) {
                    notifyError(error?.message || 'Unable to open the fee for editing.');
                }
                return;
            }
            button.disabled = true;
            try {
                if (action === 'publish') await apiPost(`${API.structures}/${encodeURIComponent(id)}/publish`, {});
                else if (action === 'archive') await apiPost(`${API.structures}/${encodeURIComponent(id)}/archive`, {});
                else if (action === 'deactivate' && source === 'OPTIONAL') await apiDelete(`${API.independent}/${encodeURIComponent(id)}`);
                else if (action === 'set-active') {
                    const active = button.dataset.active !== 'true';
                    const endpoint = source === 'MANDATORY'
                        ? `${API.structures}/${encodeURIComponent(id)}/active?active=${active}`
                        : `${API.independent}/${encodeURIComponent(id)}/active?active=${active}`;
                    await apiPost(endpoint, {});
                }
                await loadTable();
            } catch (error) {
                notifyError(error?.message || 'The fee action failed.');
                button.disabled = false;
            }
            return;
        }
        const row = event.target.closest('tr[data-fee-row="true"]');
        if (row) await openRowView(row);
    }

    async function openRowView(row) {
        if (!row || row.dataset.opening === 'true') return;
        const id = row.dataset.id;
        const source = row.dataset.source;
        if (!id || !source) return;

        row.dataset.opening = 'true';
        let loaderToken;
        let loaderStarted = false;
        try {
            // Use the ERP's existing global loader. Never insert loading text
            // into the table row or mutate the fee row while details are fetched.
            if (typeof window.showLoader === 'function') {
                loaderToken = window.showLoader('Loading fee details...');
                loaderStarted = true;
            }
            const record = await fetchFeeRecord(id, source);
            showViewPanel(record, source);
        } catch (error) {
            console.error('Unable to open fee details.', error);
            notifyError(error?.message || 'Unable to load fee details.');
        } finally {
            if (loaderStarted && typeof window.hideLoader === 'function') {
                if (loaderToken !== undefined && loaderToken !== null) window.hideLoader(loaderToken);
                else window.hideLoader();
            }
            delete row.dataset.opening;
        }
    }

    async function fetchFeeRecord(id, source) {
        if (source === 'MANDATORY') {
            const response = await apiGet(`${API.structures}/${encodeURIComponent(id)}`, { silent: true });
            const record = unwrapObject(response);
            return { ...record, id: record.feeStructureId ?? record.id ?? id, feeSource: 'MANDATORY', status: record.structureStatus ?? record.status };
        }
        const response = await apiGet(`${API.independent}?activeOnly=false`, { silent: true });
        const items = asList(response);
        const item = items.find(row => String(row.feeStructureItemId ?? row.id) === String(id));
        if (!item) throw new Error('Optional fee was not found. Refresh the list and try again.');
        return { ...item, id: item.feeStructureItemId ?? item.id ?? id, feeSource: 'OPTIONAL', status: item.structureStatus ?? 'DRAFT' };
    }

    function showViewPanel(record, source) {
        const isMandatory = source === 'MANDATORY';
        state.selectedRecord = { ...record, id: record.id ?? record.feeStructureId ?? record.feeStructureItemId, feeSource: source };
        const status = String(record.structureStatus ?? record.status ?? 'DRAFT').toUpperCase();
        const prefix = isMandatory ? '#fee-mandatory-view-' : '#fee-optional-view-';
        const fields = {
            [`${prefix}status`]: statusLabel(status),
            [`${prefix}structure-status`]: statusLabel(record.structureStatus ?? status),
            [`${prefix}active`]: (record.active === true || record.active === 1 || record.active === '1') ? 'Active' : 'Inactive',
            [`${prefix}name`]: record.feeName,
            [`${prefix}type`]: record.feeType,
            [`${prefix}code`]: record.feeCode,
            [`${prefix}amount`]: formatAmount(record.amount),
            [`${prefix}frequency`]: frequencyLabel(record.chargingFrequency),
            [`${prefix}created`]: formatDateTime(record.createdAt),
            [`${prefix}updated`]: formatDateTime(record.updatedAt),
            [`${prefix}remarks`]: record.remarks
        };
        if (isMandatory) {
            fields[`${prefix}year`] = record.academicYearName || record.academicYearCode;
            fields[`${prefix}level`] = record.levelName;
            fields[`${prefix}start-class`] = record.startClassName;
            fields[`${prefix}end-class`] = record.endClassName || record.startClassName;
            fields[`${prefix}version`] = record.versionNo;
            fields[`${prefix}published`] = formatDateTime(record.publishedAt);
        }
        Object.entries(fields).forEach(([selector, value]) => {
            const node = el(selector);
            if (node) node.textContent = value == null || value === '' ? '—' : String(value);
        });
        const mandatoryEdit = el('#fee-mandatory-view-edit');
        const optionalEdit = el('#fee-optional-view-edit');
        const publishButton = el('#fee-mandatory-view-publish');
        const archiveButton = el('#fee-mandatory-view-archive');
        // Workflow actions live in View Details so the list can remain free of an Actions column.
        mandatoryEdit.hidden = !isMandatory;
        mandatoryEdit.disabled = isMandatory && status !== 'DRAFT';
        mandatoryEdit.title = mandatoryEdit.disabled ? 'Only draft mandatory fees can be edited' : 'Edit fee';
        mandatoryEdit.setAttribute('aria-label', mandatoryEdit.title);
        publishButton.hidden = !isMandatory || status !== 'DRAFT';
        archiveButton.hidden = !isMandatory || status === 'ARCHIVED';
        optionalEdit.hidden = isMandatory;
        optionalEdit.disabled = false;
        optionalEdit.title = 'Edit fee';
        optionalEdit.setAttribute('aria-label', 'Edit fee');
        el('#fee-structures-list-view').classList.add('hidden');
        el('#fee-structures-form-view').classList.add('hidden');
        el('#fee-mandatory-view-panel').classList.toggle('hidden', !isMandatory);
        el('#fee-optional-view-panel').classList.toggle('hidden', isMandatory);
        el('#fee-structures-create-mandatory').classList.add('hidden');
        el('#fee-structures-create-optional').classList.add('hidden');
        el('#fee-structures-refresh').classList.add('hidden');
    }

    async function runMandatoryWorkflowAction(action) {
        const record = state?.selectedRecord;
        if (!record || record.feeSource !== 'MANDATORY') return;
        const id = record.id ?? record.feeStructureId;
        if (!id || !['publish', 'archive'].includes(action)) return;
        const confirmed = await confirmAction(action === 'publish' ? {
            title: 'Publish Fee Structure',
            message: 'Publish this mandatory fee? Published fees cannot be edited directly.',
            confirmText: 'Publish'
        } : {
            title: 'Archive Fee Structure',
            message: 'Archive this mandatory fee? The record and financial history will be retained.',
            confirmText: 'Archive'
        });
        if (!confirmed) return;

        const button = el(action === 'publish' ? '#fee-mandatory-view-publish' : '#fee-mandatory-view-archive');
        if (button.disabled) return;
        button.disabled = true;
        try {
            await apiPost(`${API.structures}/${encodeURIComponent(id)}/${action}`, {});
            const updated = await fetchFeeRecord(id, 'MANDATORY');
            showViewPanel(updated, 'MANDATORY');
            await loadTable();
            notifySuccess(action === 'publish' ? 'Fee published successfully.' : 'Fee archived successfully.');
        } catch (error) {
            notifyError(error?.message || `Unable to ${action} this fee.`);
        } finally {
            button.disabled = false;
        }
    }

    function confirmAction(options = {}) {
        const overlay = el('#fee-structures-confirm-overlay');
        const dialog = el('#fee-structures-confirm-dialog');
        const titleNode = el('#fee-structures-confirm-title');
        const messageNode = el('#fee-structures-confirm-message');
        const submit = el('#fee-structures-confirm-submit');
        const cancel = el('#fee-structures-confirm-cancel');
        if (!overlay || !dialog || !titleNode || !messageNode || !submit || !cancel) {
            console.error('Fee Structures custom confirmation modal is missing.');
            notifyError('Confirmation dialog is unavailable. Please refresh the page and try again.');
            return Promise.resolve(false);
        }
        if (state.confirmationResolver) return Promise.resolve(false);

        titleNode.textContent = options.title || 'Confirm Action';
        messageNode.textContent = options.message || 'Please confirm this action.';
        submit.textContent = options.confirmText || 'Confirm';
        cancel.textContent = options.cancelText || 'Cancel';
        state.previousFocus = document.activeElement instanceof HTMLElement ? document.activeElement : null;
        overlay.classList.add('is-visible');
        overlay.setAttribute('aria-hidden', 'false');
        document.body.classList.add('modal-open');
        window.requestAnimationFrame(() => dialog.focus());

        return new Promise(resolve => {
            state.confirmationResolver = value => resolve(Boolean(value));
        });
    }

    function closeConfirmation(accepted) {
        if (!state?.confirmationResolver) return;
        const overlay = el('#fee-structures-confirm-overlay');
        overlay?.classList.remove('is-visible');
        overlay?.setAttribute('aria-hidden', 'true');
        document.body.classList.remove('modal-open');
        const resolver = state.confirmationResolver;
        state.confirmationResolver = null;
        window.setTimeout(() => {
            if (state.previousFocus?.isConnected) state.previousFocus.focus();
            state.previousFocus = null;
            resolver(Boolean(accepted));
        }, 180);
    }

    function closeViewPanel() {
        el('#fee-mandatory-view-panel').classList.add('hidden');
        el('#fee-optional-view-panel').classList.add('hidden');
        el('#fee-structures-list-view').classList.remove('hidden');
        el('#fee-structures-create-mandatory').classList.remove('hidden');
        el('#fee-structures-create-optional').classList.remove('hidden');
        el('#fee-structures-refresh').classList.remove('hidden');
        state.selectedRecord = null;
    }

    async function openEditForm(record, source) {
        const status = String(record.structureStatus ?? record.status ?? 'DRAFT').toUpperCase();
        if (source === 'MANDATORY' && status !== 'DRAFT') {
            notifyError('Only draft mandatory fees can be edited.');
            return;
        }
        state.source = source;
        // Hide the view panels and reset the form before assigning edit state.
        // resetCreateForm() clears editingRecord, so assigning it before that call
        // would make Back incorrectly return to the list instead of View Details.
        el('#fee-mandatory-view-panel').classList.add('hidden');
        el('#fee-optional-view-panel').classList.add('hidden');
        el('#fee-structures-list-view').classList.add('hidden');
        el('#fee-structures-form-view').classList.remove('hidden');
        el('#fee-structures-form-title').textContent = source === 'MANDATORY' ? 'Edit Mandatory Fee' : 'Edit Optional Fee';
        root.querySelector('#fee-structures-create-form').classList.toggle('hidden', source !== 'MANDATORY');
        el('#fee-structures-optional-form-wrap').classList.toggle('hidden', source !== 'OPTIONAL');
        resetCreateForm();
        state.editingRecord = { ...record, id: record.id ?? record.feeStructureId ?? record.feeStructureItemId };
        state.selectedRecord = { ...state.editingRecord, feeSource: source };
        const prefix = source === 'MANDATORY' ? 'fee-create-' : 'fee-optional-';
        el(`#${prefix}type`).value = record.feeType || '';
        el(`#${prefix}name`).value = record.feeName || '';
        el(`#${prefix}code`).value = record.feeCode || '';
        el(`#${prefix}amount`).value = record.amount ?? '';
        el(`#${prefix}frequency`).value = record.chargingFrequency || '';
        const statusSelect = el(source === 'MANDATORY' ? '#fee-create-structure-status' : '#fee-optional-structure-status');
        if (statusSelect) statusSelect.value = record.structureStatus || 'DRAFT';
        el(`#${prefix}remarks`).value = record.remarks || '';
        if (source === 'MANDATORY') {
            el('#fee-create-year').value = String(record.academicYearId ?? '');
            el('#fee-create-level').value = String(record.levelId ?? '');
            await loadCreateClasses();
            el('#fee-create-start-class').value = String(record.startClassId ?? '');
            el('#fee-create-end-class').value = String(record.endClassId ?? '');
            el('#fee-create-end-class').disabled = !el('#fee-create-start-class').value;
        }
        const save = el('#fee-create-save');
        if (save) save.innerHTML = '<i class="bi bi-check-circle"></i> Save Changes';
        const optionalSave = el('#fee-optional-save');
        if (optionalSave) optionalSave.innerHTML = '<i class="bi bi-check-circle"></i> Save Changes';
    }

    function formatDateTime(value) {
        if (!value) return '—';
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) return String(value);
        return new Intl.DateTimeFormat('en-GB', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit', hour12: true }).format(date).replace(',', '');
    }

    async function refreshAll() {
        if (state.loading) return;
        try {
            const [years, reference] = await Promise.all([apiGet(API.years, { silent: true }), apiGet(API.referenceData, { silent: true })]);
            state.years = asList(years);
            const ref = unwrapObject(reference);
            state.levels = asList(ref.levels || ref.levelOptions || ref.availableLevels);
            fillSelect('#fee-structures-filter-year', state.years, 'All Academic Years', ['academicYearId', 'id'], ['academicYearName', 'yearName', 'name', 'academicYearCode']);
            fillSelect('#fee-structures-filter-level', state.levels, 'All Levels', ['levelId', 'id'], ['levelName', 'name', 'levelCode']);
            fillSelect(root.querySelector('#fee-create-year'), state.years, 'Select Academic Year', ['academicYearId', 'id'], ['academicYearName', 'yearName', 'name', 'academicYearCode']);
            fillSelect(root.querySelector('#fee-create-level'), state.levels, 'Select Level', ['levelId', 'id'], ['levelName', 'name', 'levelCode']);
            await loadFilterClasses(); await loadTable();
        } catch (error) { notifyError(error?.message || 'Unable to refresh fee data.'); }
    }

    function renderRows(rows) {
        const body = el('#fee-structures-table-body'); body.replaceChildren();
        if (!rows.length) { renderMessage('No fees match the selected filters.'); return; }
        const fragment = document.createDocumentFragment();
        rows.forEach(record => {
            const row = document.createElement('tr');
            const source = String(record.feeSource || '').toUpperCase();
            const status = String(record.status || record.structureStatus || '').toUpperCase();
            const id = record.id ?? record.feeStructureId ?? record.feeStructureItemId;
            row.dataset.feeRow = 'true';
            row.dataset.id = String(id ?? '');
            row.dataset.source = source;
            row.tabIndex = 0;
            row.setAttribute('role', 'button');
            row.setAttribute('aria-label', `View ${source.toLowerCase()} fee ${record.feeName || ''}`.trim());
            row.title = 'Click to view fee details';
            row.classList.add('fee-structure-clickable-row');
            appendBadgeCell(row, source, source === 'MANDATORY' ? 'bg-primary' : 'bg-info');
            appendCell(row, record.feeName);
            appendCell(row, record.feeType);
            appendCell(row, record.feeCode);
            const isOptional = source === 'OPTIONAL';
            appendCell(row, isOptional ? 'Branch-wide' : (record.academicYearName || record.academicYearCode));
            appendCell(row, isOptional ? null : record.levelName);
            const startClassLabel = record.startClassCode || record.startClassName;
            const endClassLabel = record.endClassCode || record.endClassName;
            const classRange = !isOptional && startClassLabel
                ? `${startClassLabel}${endClassLabel && endClassLabel !== startClassLabel ? ` – ${endClassLabel}` : ''}`
                : null;
            appendCell(row, classRange);
            appendCell(row, frequencyLabel(record.chargingFrequency));
            appendCell(row, formatAmount(record.amount));
            appendBadgeCell(row, statusLabel(status), statusClass(status));
            const isActive = record.active === true || record.active === 1 || record.active === '1';
            const activeCell = document.createElement('td');
            activeCell.className = 'text-center align-middle';
            const switchWrap = document.createElement('div');
            switchWrap.className = 'fee-active-switch-wrap d-inline-flex align-items-center justify-content-center';
            const toggle = document.createElement('input');
            toggle.type = 'checkbox';
            toggle.className = 'fee-active-toggle';
            toggle.role = 'switch';
            toggle.dataset.action = 'set-active';
            toggle.dataset.id = String(id ?? '');
            toggle.dataset.source = source;
            toggle.dataset.active = String(isActive);
            toggle.checked = isActive;
            toggle.setAttribute('aria-label', `${isActive ? 'Deactivate' : 'Activate'} ${record.feeName || 'fee'}`);
            toggle.title = isActive ? 'On — click to deactivate fee' : 'Off — click to activate fee';
            switchWrap.appendChild(toggle);
            activeCell.appendChild(switchWrap);
            row.appendChild(activeCell);
            fragment.appendChild(row);
        });
        body.appendChild(fragment);
    }

    function actionButton(action, id, source, tooltip, className, iconClass) {
        const button = document.createElement('button');
        button.type = 'button';
        button.dataset.action = action;
        button.dataset.id = String(id);
        button.dataset.source = source;
        button.className = `${className} fee-structure-icon-action`;
        button.title = tooltip;
        button.setAttribute('aria-label', tooltip);
        button.setAttribute('data-bs-toggle', 'tooltip');
        const icon = document.createElement('i');
        icon.className = iconClass;
        icon.setAttribute('aria-hidden', 'true');
        button.appendChild(icon);
        return button;
    }

    function appendBadgeCell(row, label, className) { const td = document.createElement('td'); const badge = document.createElement('span'); badge.className = `badge ${className}`; badge.textContent = label || '—'; td.appendChild(badge); row.appendChild(td); }
    function appendCell(row, value) { const td = document.createElement('td'); td.textContent = value == null || value === '' ? '—' : String(value); row.appendChild(td); }
    function updatePagination() {
        const start = state.totalElements ? state.page * state.size + 1 : 0;
        const end = Math.min((state.page + 1) * state.size, state.totalElements);
        el('#fee-structures-page-info').textContent = state.totalElements ? `Showing ${start}–${end} of ${state.totalElements} fees` : 'Showing 0 of 0 fees';
        el('#fee-structures-prev').disabled = state.loading || state.page <= 0;
        el('#fee-structures-next').disabled = state.loading || state.page + 1 >= state.totalPages;
    }
    function setTableBusy(busy) { ['#fee-structures-refresh', '#fee-structures-apply-filters', '#fee-structures-reset-filters', '#fee-structures-page-size'].forEach(selector => { el(selector).disabled = busy; }); updatePagination(); }
    function renderMessage(message) { const body = el('#fee-structures-table-body'); body.replaceChildren(); const row = document.createElement('tr'); const cell = document.createElement('td'); cell.colSpan = 11; cell.className = 'text-center py-4 text-muted'; cell.textContent = message; row.appendChild(cell); body.appendChild(row); }
    function fillSelect(selectorOrSelect, records, placeholder, idKeys, labelKeys) { const select = typeof selectorOrSelect === 'string' ? el(selectorOrSelect) : selectorOrSelect; const previous = select.value; select.replaceChildren(makeOption('', placeholder)); records.forEach(record => { const id = getValue(record, idKeys); if (id != null && id !== '') select.appendChild(makeOption(String(id), String(getValue(record, labelKeys) ?? id))); }); if ([...select.options].some(option => option.value === previous)) select.value = previous; select.disabled = false; }
    function setSelectLabel(selectorOrSelect, label, disabled) {
        const select = typeof selectorOrSelect === 'string' ? el(selectorOrSelect) : selectorOrSelect;
        if (!select || typeof select.replaceChildren !== 'function') return;
        select.replaceChildren(makeOption('', label));
        select.disabled = Boolean(disabled);
    }
    function setSelectError(selector, label) { setSelectLabel(selector, label, true); }
    function makeOption(value, label) { const option = document.createElement('option'); option.value = value; option.textContent = label; return option; }
    function unwrapObject(value) { return value && value.data !== undefined ? value.data || {} : value || {}; }
    function asList(value) { const data = unwrapObject(value); if (Array.isArray(data)) return data; for (const key of ['items', 'content', 'records']) if (Array.isArray(data[key])) return data[key]; return []; }
    function getValue(record, keys) { for (const key of keys) if (record?.[key] !== undefined && record[key] !== null && record[key] !== '') return record[key]; return null; }
    function numberOr(value, fallback) { const number = Number(value); return Number.isFinite(number) && number >= 0 ? number : fallback; }
    function formatAmount(value) { if (value == null || value === '') return '—'; const amount = Number(value); return Number.isFinite(amount) ? amount.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : String(value); }
    function frequencyLabel(value) { return ({ PER_TERM: 'Per Term', PER_ACADEMIC_YEAR: 'Per Academic Year', ONE_TIME: 'One Time', PER_ADMISSION: 'Per Admission', AS_REQUIRED: 'As Required' })[value] || value || '—'; }
    function statusLabel(value) { return ({ DRAFT: 'Draft', PUBLISHED: 'Published', ARCHIVED: 'Archived', ACTIVE: 'Active', INACTIVE: 'Inactive' })[value] || value || 'Unknown'; }
    function statusClass(value) { return ({ DRAFT: 'bg-secondary', PUBLISHED: 'bg-success', ARCHIVED: 'bg-dark', ACTIVE: 'bg-success', INACTIVE: 'bg-secondary' })[value] || 'bg-secondary'; }
    function showCreateError(message) { const box = el('#fee-create-error'); if (!box) return; box.classList.remove('hidden'); el('#fee-create-error-text').textContent = message; }
    function clearCreateError() { const box = el('#fee-create-error'); if (box) { box.classList.add('hidden'); el('#fee-create-error-text').textContent = ''; } }
    function notifyError(message) { if (typeof showErrorMessage === 'function') showErrorMessage(message); else console.error(message); }
    function notifySuccess(message) { if (typeof window.showSuccessMessage === 'function') window.showSuccessMessage(message); else if (typeof window.showToast === 'function') window.showToast(message, 'success'); else console.info(message); }
    function el(selector) {
        if (state?.source === 'OPTIONAL') {
            if (selector === '#fee-structures-create-form') selector = '#fee-structures-optional-form';
            else if (selector.startsWith('#fee-create-')) selector = selector.replace('#fee-create-', '#fee-optional-');
        }
        return root.querySelector(selector);
    }
})();

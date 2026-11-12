/* global apiGet, apiPost, apiPut, apiPatch, showSuccessMessage, showErrorMessage */
(() => {
    'use strict';

    const VIEW_NAME = 'course-combinations';

    const API = Object.freeze({
        subjects: '/subjects',
        combinations: '/school/a-level-combinations'
    });

    document.addEventListener('viewLoaded', event => {
        if (
            event.detail?.role !== 'admin' ||
            event.detail?.view !== VIEW_NAME
        ) {
            return;
        }

        const initialization = initCourseCombinations();

        if (typeof event.detail.waitUntil === 'function') {
            event.detail.waitUntil(initialization);
        }

        initialization.catch(error => {
            console.error(
                'Course Combinations initialization failed.',
                error
            );

            notifyError(
                readError(error, 'Unable to open Course Combinations.')
            );
        });
    });

    async function initCourseCombinations() {

        const root = document.querySelector(
            '#ba-course-combinations-view'
        );

        if (!root || root.dataset.initialized === 'true') {
            return null;
        }

        root.dataset.initialized = 'true';

        const q = selector => root.querySelector(selector);

        const el = {
            tableView: q('#course-combinations-table-view'),
            formView: q('#course-combinations-form-view'),
            detailsView: q('#course-combinations-details-view'),

            add: q('#course-combinations-add-btn'),
            refresh: q('#course-combinations-refresh-btn'),

            filterForm: q('#course-combinations-filter-form'),
            reset: q('#course-combinations-reset-btn'),

            statusFilter: q('#course-combinations-status-filter'),
            search: q('#course-combinations-search'),

            // Optional: supported when present in the HTML.
            subsidiaryFilter: q(
                '#course-combinations-subsidiary-filter'
            ),

            body: q('#course-combinations-table-body'),
            template: q('#course-combinations-row-template'),

            pageSize: q('#course-combinations-page-size'),
            pageInfo: q('#course-combinations-page-info'),
            prev: q('#course-combinations-prev-btn'),
            next: q('#course-combinations-next-btn'),

            form: q('#course-combinations-form'),
            recordId: q('#course-combinations-record-id'),
            code: q('#course-combinations-code'),
            name: q('#course-combinations-name'),

            principal: [
                q('#course-combinations-principal-1'),
                q('#course-combinations-principal-2'),
                q('#course-combinations-principal-3')
            ],

            subsidiary: q('#course-combinations-subsidiary'),

            formBack: q('#course-combinations-form-back-btn'),
            cancel: q('#course-combinations-cancel-btn'),
            save: q('#course-combinations-save-btn'),

            formError: q('#course-combinations-form-error'),
            formErrorText: q('#course-combinations-form-error-text'),

            detailName: q('#course-combinations-detail-name'),
            detailStatus: q('#course-combinations-detail-status'),

            detailPrincipal: [
                q('#course-combinations-detail-principal-1'),
                q('#course-combinations-detail-principal-2'),
                q('#course-combinations-detail-principal-3')
            ],

            detailSubsidiary: q(
                '#course-combinations-detail-subsidiary'
            ),

            detailBack: q('#course-combinations-details-back-btn'),
            detailEdit: q('#course-combinations-details-edit-btn'),
            detailStatusBtn: q(
                '#course-combinations-details-status-btn'
            )
        };

        const state = {
            subjects: [],
            principalSubjects: [],
            subsidiarySubjects: [],

            records: [],
            filtered: [],

            page: 0,
            size: Number(el.pageSize.value) || 10,

            viewingRecord: null,
            busy: false,
            loadFailed: false,
            requestId: 0
        };

        bindEvents();

        let initialLoaderToken = null;

        try {

            if (typeof window.showLoader === 'function') {
                initialLoaderToken = window.showLoader(
                    'Loading Course Combinations...'
                );
            }

            await loadReferenceData();

            await loadCombinations({
                inline: false
            });

        } catch (error) {

            state.loadFailed = true;

            showTableMessage(
                readError(
                    error,
                    'Unable to load Course Combinations.'
                )
            );

            notifyError(
                readError(
                    error,
                    'Unable to load Course Combinations.'
                )
            );

            el.add.disabled = true;

        } finally {

            if (
                initialLoaderToken != null &&
                typeof window.hideLoader === 'function'
            ) {
                window.hideLoader(initialLoaderToken);
            }
        }

        // ================================================
        // EVENTS
        // ================================================

        function bindEvents() {

            el.add.addEventListener('click', () => {

                if (state.busy) return;

                populateForm(null);
                showView('form');
            });

            el.refresh.addEventListener('click', () => {
                void refresh();
            });

            el.filterForm.addEventListener('submit', event => {

                event.preventDefault();

                state.page = 0;

                applyFilters();
            });

            el.reset.addEventListener('click', () => {

                el.statusFilter.value = '';
                el.search.value = '';

                if (el.subsidiaryFilter) {
                    el.subsidiaryFilter.value = '';
                }

                state.page = 0;

                applyFilters();
            });

            el.statusFilter.addEventListener('change', () => {

                state.page = 0;

                applyFilters();
            });

            el.subsidiaryFilter?.addEventListener('change', () => {

                state.page = 0;

                applyFilters();
            });

            el.pageSize.addEventListener('change', () => {

                state.size = Number(el.pageSize.value) || 10;
                state.page = 0;

                renderTable();
            });

            el.prev.addEventListener('click', () => {

                if (state.page > 0) {
                    state.page--;
                    renderTable();
                }
            });

            el.next.addEventListener('click', () => {

                if (state.page + 1 < pageCount()) {
                    state.page++;
                    renderTable();
                }
            });

            el.formBack.addEventListener('click', () => {
                showView('table');
            });

            el.cancel.addEventListener('click', () => {
                showView('table');
            });

            el.form.addEventListener('submit', event => {

                event.preventDefault();

                void saveRecord();
            });

            [...el.principal, el.subsidiary].forEach(select => {

                select.addEventListener(
                    'change',
                    refreshSubjectOptions
                );
            });

            el.detailBack.addEventListener('click', () => {
                showView('table');
            });

            el.detailEdit.addEventListener('click', () => {

                if (!state.viewingRecord || state.busy) return;

                populateForm(state.viewingRecord);

                showView('form');
            });

            el.detailStatusBtn.addEventListener('click', () => {

                if (state.viewingRecord) {
                    void toggleStatus(state.viewingRecord);
                }
            });

            // Row click opens Details.

            el.body.addEventListener('click', event => {

                const toggle = event.target.closest(
                    '.course-combinations-status-toggle'
                );

                if (toggle) {
                    event.preventDefault();
                    event.stopPropagation();
                    return;
                }

                const row = event.target.closest(
                    'tr[data-combination-id]'
                );

                if (!row || state.busy) return;

                const record = state.records.find(item =>
                    String(item.combinationId) ===
                    row.dataset.combinationId
                );

                if (record) {
                    viewRecord(record);
                }
            });

            el.body.addEventListener('keydown', event => {

                const row = event.target.closest(
                    'tr[data-combination-id]'
                );

                if (
                    row &&
                    (event.key === 'Enter' || event.key === ' ')
                ) {

                    event.preventDefault();

                    if (state.busy) return;

                    const record = state.records.find(item =>
                        String(item.combinationId) ===
                        row.dataset.combinationId
                    );

                    if (record) {
                        viewRecord(record);
                    }
                }
            });

            // Status switch.

            el.body.addEventListener('click', event => {

                const toggle = event.target.closest(
                    '.course-combinations-status-toggle'
                );

                if (!toggle) return;

                const row = toggle.closest(
                    'tr[data-combination-id]'
                );

                const record = row && state.records.find(item =>
                    String(item.combinationId) ===
                    row.dataset.combinationId
                );

                if (record && !state.busy) {
                    void toggleStatus(record);
                }
            });
        }

        // ================================================
        // SUBJECT MASTER DATA
        // ================================================

        async function loadReferenceData() {

            /*
             * Subjects module is the master source.
             *
             * No frontend Senior Secondary level validation.
             * No Class Subjects API dependency.
             * Backend owns branch eligibility and subject validation.
             */

            const response = await apiGet(API.subjects);

            state.subjects = asArray(
                responseData(response)
            ).filter(item =>
                item.active !== false &&
                String(item.status || '').toUpperCase() !== 'INACTIVE'
            );

            state.principalSubjects = state.subjects.filter(
                subject =>
                    String(subject.subjectType || '').toUpperCase()
                    !== 'SUBSIDIARY'
            );

            state.subsidiarySubjects = state.subjects.filter(
                subject =>
                    String(subject.subjectType || '').toUpperCase()
                    === 'SUBSIDIARY'
            );

            if (el.subsidiaryFilter) {

                fillOptions(
                    el.subsidiaryFilter,
                    state.subsidiarySubjects,
                    'All Subjects',
                    subject => subject.subjectId,
                    subject => subjectLabel(
                        subject.subjectCode,
                        subject.subjectName
                    )
                );
            }

            fillSubjectSelects();

            el.add.disabled = false;
        }

        // ================================================
        // COMBINATIONS
        // ================================================

        async function loadCombinations(options = {}) {

            const requestId = ++state.requestId;

            if (options.inline !== false) {
                showTableLoading(
                    'Loading Course Combinations...'
                );
            }

            try {

                const response = await apiGet(API.combinations);

                if (requestId !== state.requestId) return;

                state.records = asArray(
                    responseData(response)
                );

                state.loadFailed = false;
                state.page = 0;

                applyFilters();

            } catch (error) {

                if (requestId !== state.requestId) return;

                state.records = [];
                state.filtered = [];
                state.loadFailed = true;

                showTableMessage(
                    readError(
                        error,
                        'Unable to load Course Combinations.'
                    )
                );

                throw error;
            }
        }

        async function refresh() {

            if (state.busy) return;

            el.refresh.disabled = true;

            let token = null;

            try {

                if (typeof window.showLoader === 'function') {
                    token = window.showLoader(
                        'Refreshing Course Combinations...'
                    );
                }

                await loadReferenceData();

                await loadCombinations({
                    inline: false
                });

            } catch (error) {

                notifyError(
                    readError(
                        error,
                        'Unable to refresh Course Combinations.'
                    )
                );

            } finally {

                if (
                    token != null &&
                    typeof window.hideLoader === 'function'
                ) {
                    window.hideLoader(token);
                }

                el.refresh.disabled = false;
            }
        }

        // ================================================
        // FILTERS
        // ================================================

        function applyFilters() {

            const status = el.statusFilter.value;

            const search = el.search.value
                .trim()
                .toLocaleLowerCase();

            const subsidiaryId = el.subsidiaryFilter
                ? el.subsidiaryFilter.value
                : '';

            state.filtered = state.records.filter(record => {

                const recordStatus =
                    record.active === false
                        ? 'INACTIVE'
                        : 'ACTIVE';

                const text = `
                    ${record.combinationCode || ''}
                    ${record.combinationName || ''}
                    ${record.principalSubject1Name || ''}
                    ${record.principalSubject2Name || ''}
                    ${record.principalSubject3Name || ''}
                    ${record.subsidiarySubjectName || ''}
                `.toLocaleLowerCase();

                return (
                    (!status || recordStatus === status) &&
                    (!search || text.includes(search)) &&
                    (
                        !subsidiaryId ||
                        String(record.subsidiarySubjectId) === subsidiaryId
                    )
                );
            });

            if (state.page >= pageCount()) {
                state.page = Math.max(pageCount() - 1, 0);
            }

            renderTable();
        }

        // ================================================
        // TABLE
        // ================================================

        function renderTable() {

            el.body.replaceChildren();

            if (!state.filtered.length) {

                showTableMessage(
                    state.loadFailed
                        ? 'Combinations could not be loaded. Select Refresh to try again.'
                        : state.records.length
                            ? 'No combinations match the selected filters.'
                            : 'No Course Combinations configured.'
                );

                return;
            }

            const start = state.page * state.size;

            state.filtered
                .slice(start, start + state.size)
                .forEach(record => {

                    const fragment =
                        el.template.content.cloneNode(true);

                    const row = fragment.querySelector('tr');

                    row.dataset.combinationId =
                        String(record.combinationId);

                    row.tabIndex = 0;
                    row.setAttribute('role', 'button');

                    row.setAttribute(
                        'aria-label',
                        `View ${record.combinationName || 'course combination'} details`
                    );

                    setText(
                        row,
                        '.col-name strong',
                        record.combinationName
                    );

                    setText(
                        row,
                        '.col-principal-1',
                        record.principalSubject1Name
                    );

                    setText(
                        row,
                        '.col-principal-2',
                        record.principalSubject2Name
                    );

                    setText(
                        row,
                        '.col-principal-3',
                        record.principalSubject3Name
                    );

                    setText(
                        row,
                        '.col-subsidiary',
                        record.subsidiarySubjectName
                    );

                    const active = record.active !== false;

                    const toggle = row.querySelector(
                        '.course-combinations-status-toggle'
                    );

                    toggle.setAttribute(
                        'aria-checked',
                        String(active)
                    );

                    toggle.setAttribute(
                        'aria-label',
                        `${active ? 'Deactivate' : 'Activate'} ${record.combinationName || 'combination'}`
                    );

                    toggle.classList.toggle(
                        'is-active',
                        active
                    );

                    const label = toggle.querySelector(
                        '.course-combinations-toggle-label'
                    );

                    if (label) {
                        label.textContent = active ? 'ON' : 'OFF';
                    }

                    el.body.appendChild(fragment);
                });

            updatePager();
        }

        // ================================================
        // DETAILS
        // ================================================

        function viewRecord(record) {

            if (state.busy) return;

            state.viewingRecord = record;

            setText(
                root,
                '#course-combinations-detail-name',
                record.combinationName
            );

            setText(
                root,
                '#course-combinations-detail-status',
                record.active === false ? 'Inactive' : 'Active'
            );

            setText(
                root,
                '#course-combinations-detail-principal-1',
                subjectLabel(
                    record.principalSubject1Code,
                    record.principalSubject1Name
                )
            );

            setText(
                root,
                '#course-combinations-detail-principal-2',
                subjectLabel(
                    record.principalSubject2Code,
                    record.principalSubject2Name
                )
            );

            setText(
                root,
                '#course-combinations-detail-principal-3',
                subjectLabel(
                    record.principalSubject3Code,
                    record.principalSubject3Name
                )
            );

            setText(
                root,
                '#course-combinations-detail-subsidiary',
                subjectLabel(
                    record.subsidiarySubjectCode,
                    record.subsidiarySubjectName
                )
            );

            el.detailStatusBtn.textContent =
                record.active === false
                    ? 'Activate'
                    : 'Deactivate';

            el.detailEdit.disabled =
                record.active === false;

            showView('details');
        }

        // ================================================
        // FORM
        // ================================================

        function populateForm(record) {

            clearFormError();

            el.form.reset();

            el.recordId.value = record
                ? String(record.combinationId)
                : '';

            q('#course-combinations-form-title').textContent =
                record
                    ? 'Edit Course Combination'
                    : 'Add Course Combination';

            el.code.value = record?.combinationCode || '';
            el.name.value = record?.combinationName || '';

            const principalValues = record
                ? [
                    record.principalSubject1Id,
                    record.principalSubject2Id,
                    record.principalSubject3Id
                ].map(String)
                : [];

            const subsidiaryValue = record
                ? String(record.subsidiarySubjectId)
                : '';

            fillSubjectSelects(
                principalValues,
                subsidiaryValue
            );
        }

        function fillSubjectSelects(
            principalValues = [],
            subsidiaryValue = ''
        ) {

            const allValues = [
                ...principalValues,
                subsidiaryValue
            ];

            el.principal.forEach((select, index) => {

                const others = new Set(
                    allValues.filter(
                        (value, otherIndex) =>
                            value && otherIndex !== index
                    )
                );

                const available =
                    state.principalSubjects.filter(subject =>
                        !others.has(String(subject.subjectId)) ||
                        String(subject.subjectId) ===
                        String(principalValues[index])
                    );

                fillOptions(
                    select,
                    available,
                    'Select Subject',
                    subject => subject.subjectId,
                    subject => subjectLabel(
                        subject.subjectCode,
                        subject.subjectName
                    )
                );

                select.value = principalValues[index] || '';
            });

            const selectedPrincipal = new Set(
                principalValues.filter(Boolean)
            );

            const availableSubsidiaries =
                state.subsidiarySubjects.filter(subject =>
                    !selectedPrincipal.has(String(subject.subjectId)) ||
                    String(subject.subjectId) === String(subsidiaryValue)
                );

            fillOptions(
                el.subsidiary,
                availableSubsidiaries,
                'Select Subsidiary Subject',
                subject => subject.subjectId,
                subject => subjectLabel(
                    subject.subjectCode,
                    subject.subjectName
                )
            );

            el.subsidiary.value = subsidiaryValue || '';
        }

        function refreshSubjectOptions() {

            const principalValues = el.principal.map(
                select => select.value
            );

            fillSubjectSelects(
                principalValues,
                el.subsidiary.value
            );
        }

        // ================================================
        // SAVE
        // ================================================

        async function saveRecord() {

            if (state.busy) return;

            clearFormError();

            const principalIds = el.principal.map(
                select => Number(select.value)
            );

            const subsidiaryId = Number(el.subsidiary.value);

            const code = el.code.value.trim().toUpperCase();
            const name = el.name.value.trim();

            const payload = {
                combinationCode: code,
                combinationName: name,

                principalSubject1Id: principalIds[0],
                principalSubject2Id: principalIds[1],
                principalSubject3Id: principalIds[2],

                subsidiarySubjectId: subsidiaryId
            };

            state.busy = true;

            setBusy(true);

            let token = null;

            try {

                if (typeof window.showLoader === 'function') {
                    token = window.showLoader(
                        el.recordId.value
                            ? 'Updating Course Combination...'
                            : 'Saving Course Combination...'
                    );
                }

                const id = el.recordId.value;

                const response = id
                    ? await apiPut(
                        `${API.combinations}/${encodeURIComponent(id)}`,
                        payload,
                        { silent: true }
                    )
                    : await apiPost(
                        API.combinations,
                        payload,
                        { silent: true }
                    );

                notifySuccess(
                    response?.message ||
                    (
                        id
                            ? 'Course Combination updated successfully.'
                            : 'Course Combination created successfully.'
                    )
                );

                state.viewingRecord = null;

                showView('table');

                await loadCombinations({
                    inline: false
                });

            } catch (error) {

                const message = readError(
                    error,
                    'Unable to save Course Combination.'
                );

                showFormError(message);
                notifyError(message);

            } finally {

                if (
                    token != null &&
                    typeof window.hideLoader === 'function'
                ) {
                    window.hideLoader(token);
                }

                state.busy = false;

                setBusy(false);
            }
        }

        // ================================================
        // STATUS
        // ================================================

        async function toggleStatus(record) {

            if (state.busy) return;

            const activate = record.active === false;

            const confirmed = window.confirm(
                `${activate ? 'Activate' : 'Deactivate'} ${record.combinationName || 'this combination'}?`
            );

            if (!confirmed) return;

            state.busy = true;

            setBusy(true);

            let token = null;

            try {

                if (typeof window.showLoader === 'function') {
                    token = window.showLoader(
                        `${activate ? 'Activating' : 'Deactivating'} Course Combination...`
                    );
                }

                const response = await apiPatch(
                    `${API.combinations}/${encodeURIComponent(record.combinationId)}/${activate ? 'activate' : 'deactivate'}`,
                    {},
                    { silent: true }
                );

                notifySuccess(
                    response?.message ||
                    (
                        activate
                            ? 'Course Combination activated successfully.'
                            : 'Course Combination deactivated successfully.'
                    )
                );

                state.viewingRecord = null;

                showView('table');

                await loadCombinations({
                    inline: false
                });

            } catch (error) {

                notifyError(
                    readError(
                        error,
                        `Unable to ${activate ? 'activate' : 'deactivate'} Course Combination.`
                    )
                );

            } finally {

                if (
                    token != null &&
                    typeof window.hideLoader === 'function'
                ) {
                    window.hideLoader(token);
                }

                state.busy = false;

                setBusy(false);
            }
        }

        // ================================================
        // UI STATE
        // ================================================

        function setBusy(busy) {

            el.add.disabled = busy;

            el.refresh.disabled = busy;

            el.statusFilter.disabled = busy;
            el.search.disabled = busy;

            if (el.subsidiaryFilter) {
                el.subsidiaryFilter.disabled = busy;
            }

            el.save.disabled = busy;

            el.cancel.disabled = busy;
            el.formBack.disabled = busy;

            el.detailEdit.disabled =
                busy || state.viewingRecord?.active === false;

            el.detailStatusBtn.disabled = busy;

            el.body.querySelectorAll(
                '.course-combinations-status-toggle'
            ).forEach(toggle => {
                toggle.disabled = busy;
            });

            [
                ...el.principal,
                el.subsidiary,
                el.code,
                el.name
            ].forEach(control => {
                control.disabled = busy;
            });

            root.setAttribute(
                'aria-busy',
                String(busy)
            );
        }

        function showView(view) {

            el.tableView.classList.toggle(
                'hidden',
                view !== 'table'
            );

            el.formView.classList.toggle(
                'hidden',
                view !== 'form'
            );

            el.detailsView.classList.toggle(
                'hidden',
                view !== 'details'
            );

            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            });

            if (view === 'form') {
                window.setTimeout(
                    () => el.name.focus(),
                    0
                );
            }
        }

        // ================================================
        // TABLE MESSAGES / PAGINATION
        // ================================================

        function showTableLoading(message) {

            if (
                typeof window.renderFetchingMessage === 'function'
            ) {
                window.renderFetchingMessage(
                    el.body,
                    6,
                    message
                );
            } else {
                showTableMessage(message);
            }
        }

        function showTableMessage(message) {

            el.body.replaceChildren();

            const row = document.createElement('tr');
            const cell = document.createElement('td');

            cell.colSpan = 6;
            cell.className = 'text-center text-muted p-4';
            cell.textContent = message;

            row.appendChild(cell);

            el.body.appendChild(row);

            updatePager();
        }

        function updatePager() {

            const pages = pageCount();

            el.pageInfo.textContent = state.filtered.length
                ? `Showing page ${state.page + 1} of ${pages} (${state.filtered.length} combinations)`
                : 'No records';

            el.prev.disabled =
                state.page <= 0 || !state.filtered.length;

            el.next.disabled =
                state.page + 1 >= pages || !state.filtered.length;
        }

        function pageCount() {

            return Math.max(
                1,
                Math.ceil(
                    state.filtered.length /
                    Math.max(1, state.size)
                )
            );
        }

        // ================================================
        // HELPERS
        // ================================================

        function fillOptions(
            select,
            records,
            placeholder,
            valueOf,
            labelOf
        ) {

            const previous = select.value;

            select.replaceChildren(
                new Option(placeholder, '')
            );

            records.forEach(record => {

                const value = valueOf(record);
                const label = labelOf(record);

                if (value != null && label != null) {
                    select.add(
                        new Option(
                            String(label),
                            String(value)
                        )
                    );
                }
            });

            if (
                previous &&
                [...select.options].some(option =>
                    option.value === previous
                )
            ) {
                select.value = previous;
            }
        }

        function setText(container, selector, value) {

            const target = container.querySelector(selector);

            if (target) {
                target.textContent =
                    value == null || value === ''
                        ? '—'
                        : String(value);
            }
        }

        function subjectLabel(code, name) {

            return [code, name]
                .filter(Boolean)
                .join(' — ') || '—';
        }

        function clearFormError() {

            el.formError.classList.add('hidden');
            el.formErrorText.textContent = '';
        }

        function showFormError(message) {

            el.formErrorText.textContent = message;
            el.formError.classList.remove('hidden');
        }

        function asArray(value) {

            if (Array.isArray(value)) return value;

            if (Array.isArray(value?.content)) {
                return value.content;
            }

            return [];
        }

        function responseData(response) {

            return response &&
            Object.prototype.hasOwnProperty.call(response, 'data')
                ? response.data
                : response;
        }

        function readError(error, fallback) {

            return error?.data?.message ||
                error?.message ||
                fallback;
        }

        function notifySuccess(message) {

            if (typeof showSuccessMessage === 'function') {
                showSuccessMessage(message);
            } else {
                console.info(message);
            }
        }

        function notifyError(message) {

            if (typeof showErrorMessage === 'function') {
                showErrorMessage(message);
            } else {
                console.error(message);
            }
        }
    }
})();
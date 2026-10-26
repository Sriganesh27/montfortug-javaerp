/* global apiGet, apiPost, apiPut, apiPatch, showSuccessMessage, showErrorMessage, showLoader, hideLoader */
(() => {
    'use strict';

    const VIEW_NAME = 'class-subjects';
    const API = Object.freeze({ years: '/academic-years', classes: '/classes', subjects: '/subjects', configurations: '/class-subjects' });

    document.addEventListener('viewLoaded', event => {
        if (event.detail?.role !== 'admin' || event.detail?.view !== VIEW_NAME) return;

        // Let the shared ERP router keep its navigation loader active until
        // the view has finished its first data load and is ready to display.
        const initialization = initClassSubjects();
        if (typeof event.detail.waitUntil === 'function') {
            event.detail.waitUntil(initialization);
        }
        initialization.catch(error => {
            console.error('Class Subjects initialization failed.', error);
            notifyError(readError(error, 'Unable to open Class Subjects. Please refresh and try again.'));
        });
    });

    async function initClassSubjects() {
        const root = document.querySelector('#ba-class-subjects-view');
        if (!root || root.dataset.initialized === 'true') return null;
        root.dataset.initialized = 'true';
        const q = selector => root.querySelector(selector);
        const el = {
            tableView:q('#class-subjects-table-view'), formView:q('#class-subjects-form-view'), detailsView:q('#class-subjects-details-view'),
            add:q('#class-subjects-add-btn'), refresh:q('#class-subjects-refresh-btn'), filterForm:q('#class-subjects-filter-form'), reset:q('#class-subjects-reset-btn'),
            yearFilter:q('#class-subjects-year-filter'), classFilter:q('#class-subjects-class-filter'), requirementFilter:q('#class-subjects-requirement-filter'), search:q('#class-subjects-search'),
            body:q('#class-subjects-table-body'), template:q('#class-subjects-row-template'), pageSize:q('#class-subjects-page-size'), pageInfo:q('#class-subjects-page-info'), prev:q('#class-subjects-prev-btn'), next:q('#class-subjects-next-btn'),
            form:q('#class-subjects-form'), recordId:q('#class-subjects-record-id'), formTitle:q('#class-subjects-form-title'), formSubtitle:q('#class-subjects-form-subtitle'),
            year:q('#class-subjects-year'), level:q('#class-subjects-level'), subject:q('#class-subjects-subject'), requirement:q('#class-subjects-requirement'), startClass:q('#class-subjects-start-class'), endClass:q('#class-subjects-end-class'),
            formBack:q('#class-subjects-form-back-btn'), cancel:q('#class-subjects-cancel-btn'), save:q('#class-subjects-save-btn'), formError:q('#class-subjects-form-error'), formErrorText:q('#class-subjects-form-error-text'),
            detailCode:q('#class-subjects-detail-code'), detailName:q('#class-subjects-detail-name'), detailRequirement:q('#class-subjects-detail-requirement'), detailStart:q('#class-subjects-detail-start-class'), detailEnd:q('#class-subjects-detail-end-class'), detailYear:q('#class-subjects-detail-year'), detailRange:q('#class-subjects-detail-range'), detailBack:q('#class-subjects-details-back-btn'), detailEdit:q('#class-subjects-details-edit-btn'), detailDeactivate:q('#class-subjects-details-deactivate-btn')
        };
        const state = { years:[], levels:[], classes:[], subjects:[], records:[], filtered:[], page:0, size:Number(el.pageSize.value)||10, defaultYearId:null, viewingRecord:null, busy:false, loadFailed:false, loadRequestId:0 };

        bindEvents();
        let initialLoaderToken = null;
        try {
            if (typeof window.showLoader === 'function') {
                initialLoaderToken = window.showLoader('Loading Class Subjects...');
            }
            await loadReferenceData();
            if (!state.years.length) {
                showTableMessage('No active academic years are available for this branch. Create or activate an academic year before configuring class subjects.');
                el.add.disabled = true;
                return;
            }
            const currentYear = state.years.find(year => year.currentYear === true) || state.years[0];
            state.defaultYearId = String(currentYear.academicYearId);
            el.yearFilter.value = state.defaultYearId;
            fillClassFilter();
            await loadYearConfigurations({ showGlobalLoader: false, showInlineLoading: false });
        } catch (error) {
            state.loadFailed = true;
            state.records = []; state.filtered = [];
            showTableMessage(readError(error, 'Unable to load Class Subjects. Please refresh and try again.'));
            notifyError(readError(error, 'Unable to load Class Subjects.'));
            el.add.disabled = true;
        } finally {
            if (initialLoaderToken && typeof window.hideLoader === 'function') {
                window.hideLoader(initialLoaderToken);
            }
        }

        function bindEvents() {
            el.add.addEventListener('click', () => { if (!state.busy && el.yearFilter.value) { populateForm(null); showView('form'); } });
            el.refresh.addEventListener('click', () => void refresh());
            el.yearFilter.addEventListener('change', () => { state.page = 0; fillClassFilter(); void loadYearConfigurations(); });
            el.classFilter.addEventListener('change', () => { state.page = 0; applyFilters(); });
            el.requirementFilter.addEventListener('change', () => { state.page = 0; applyFilters(); });
            el.filterForm.addEventListener('submit', event => { event.preventDefault(); state.page = 0; applyFilters(); });
            el.reset.addEventListener('click', () => {
                const yearChanged = el.yearFilter.value !== state.defaultYearId;
                el.yearFilter.value = state.defaultYearId || '';
                el.classFilter.value = ''; el.requirementFilter.value = ''; el.search.value = ''; state.page = 0;
                fillClassFilter();
                if (yearChanged) void loadYearConfigurations(); else applyFilters();
            });
            el.pageSize.addEventListener('change', () => { state.size = Number(el.pageSize.value) || 10; state.page = 0; renderTable(); });
            el.prev.addEventListener('click', () => { if (state.page > 0) { state.page--; renderTable(); } });
            el.next.addEventListener('click', () => { if (state.page + 1 < pageCount()) { state.page++; renderTable(); } });
            el.formBack.addEventListener('click', () => showView('table'));
            el.cancel.addEventListener('click', () => showView('table'));
            el.form.addEventListener('submit', event => { event.preventDefault(); void saveRecord(); });
            el.year.addEventListener('change', () => { fillEditorLevelOptions(); fillEditorClassOptions(); });
            el.level.addEventListener('change', () => { el.startClass.value = ''; el.endClass.value = ''; fillEditorClassOptions(); });
            el.startClass.addEventListener('change', () => { fillEndClassOptions(); });
            el.detailBack.addEventListener('click', () => showView('table'));
            el.detailEdit.addEventListener('click', () => { if (state.viewingRecord) { populateForm(state.viewingRecord); showView('form'); } });
            el.detailDeactivate.addEventListener('click', () => { if (state.viewingRecord) void deactivateRecord(state.viewingRecord); });
            el.body.addEventListener('click', event => {
                const button = event.target.closest('button[data-id]');
                if (button) {
                    event.stopPropagation();
                    if (state.busy) return;
                    const record = state.records.find(item => String(item.classSubjectId) === button.dataset.id);
                    if (!record) { notifyError('Configuration details are no longer available. Refresh the list and try again.'); return; }
                    if (button.classList.contains('class-subjects-view-btn')) { viewRecord(record); return; }
                    if (button.classList.contains('class-subjects-edit-btn')) { populateForm(record); showView('form'); return; }
                    if (button.classList.contains('class-subjects-deactivate-btn')) { void deactivateRecord(record); return; }
                    return;
                }
                const row = event.target.closest('tr[data-configuration-id]');
                if (row) { const record = state.records.find(item => String(item.classSubjectId) === row.dataset.configurationId); if (record) viewRecord(record); }
            });
            el.body.addEventListener('keydown', event => {
                const row = event.target.closest('tr[data-configuration-id]');
                if (row && (event.key === 'Enter' || event.key === ' ')) { event.preventDefault(); const record = state.records.find(item => String(item.classSubjectId) === row.dataset.configurationId); if (record) viewRecord(record); }
            });
        }

        async function loadReferenceData() {
            const [yearsResponse, classesResponse, subjectsResponse] = await Promise.all([apiGet(API.years), apiGet(API.classes), apiGet(API.subjects)]);
            state.years = asArray(yearsResponse?.data).filter(year => year.active !== false && String(year.status || '').toUpperCase() !== 'INACTIVE');
            state.years.sort((a,b) => Number(b.currentYear === true) - Number(a.currentYear === true) || String(b.academicYearName || '').localeCompare(String(a.academicYearName || '')));
            state.classes = asArray(classesResponse?.data).filter(item => Number(item.status) === 1);
            state.levels = [...new Map(state.classes.filter(item => item.levelId != null).map(item => [String(item.levelId), { levelId:item.levelId, levelName:item.levelName }])).values()].sort((a,b) => String(a.levelName).localeCompare(String(b.levelName)));
            fillOptions(el.level, state.levels, 'Select Level', item => item.levelId, item => item.levelName);
            state.subjects = asArray(subjectsResponse?.data).filter(item => item.active !== false && String(item.status || '').toUpperCase() !== 'INACTIVE');
            fillOptions(el.yearFilter, state.years, 'Select Academic Year', item => item.academicYearId, item => item.academicYearName);
            fillOptions(el.year, state.years, 'Select Academic Year', item => item.academicYearId, item => item.academicYearName);
            fillOptions(el.subject, state.subjects, 'Select Subject', item => item.subjectId, item => `${item.subjectCode ? `${item.subjectCode} — ` : ''}${item.subjectName || ''}`);
        }

        async function refresh() {
            if (state.busy) return;
            el.refresh.disabled = true;
            let loaderToken = null;
            try {
                if (typeof window.showLoader === 'function') loaderToken = window.showLoader('Refreshing Class Subjects...');
                await loadReferenceData();
                if (!state.years.length) {
                    state.records = []; state.filtered = []; el.add.disabled = true;
                    showTableMessage('No active academic years are available for this branch.');
                    return;
                }
                if (!state.years.some(item => String(item.academicYearId) === String(el.yearFilter.value))) {
                    el.yearFilter.value = String((state.years.find(year => year.currentYear === true) || state.years[0]).academicYearId);
                }
                fillClassFilter();
                await loadYearConfigurations({ showGlobalLoader: false, showInlineLoading: false });
            } catch (error) {
                state.loadFailed = true; state.records = []; state.filtered = [];
                showTableMessage(readError(error, 'Unable to refresh Class Subjects.'));
                notifyError(readError(error, 'Unable to refresh Class Subjects.'));
            } finally {
                if (loaderToken && typeof window.hideLoader === 'function') window.hideLoader(loaderToken);
                el.refresh.disabled = false;
            }
        }

        async function loadYearConfigurations(options = {}) {
            const showGlobalLoader = options.showGlobalLoader !== false;
            const showInlineLoading = options.showInlineLoading !== false;
            const yearId = el.yearFilter.value;
            if (!yearId) {
                state.records = []; state.filtered = []; el.add.disabled = true;
                showTableMessage('Select an academic year to load configurations.');
                return;
            }
            if (showInlineLoading) showTableLoading('Loading Class Subject configurations...');
            el.add.disabled = true;
            const requestId = ++state.loadRequestId;
            let loaderToken = null;
            try {
                if (showGlobalLoader && typeof window.showLoader === 'function') loaderToken = window.showLoader('Loading Class Subjects...');
                const response = await apiGet(`${API.configurations}?academicYearId=${encodeURIComponent(yearId)}`);
                if (requestId !== state.loadRequestId || yearId !== el.yearFilter.value) return;
                state.records = asArray(response?.data);
                state.loadFailed = false; state.page = 0;
                el.add.disabled = !state.classes.some(item => String(item.academicYearId) === String(yearId)) || !state.subjects.length;
                fillClassFilter();
                applyFilters();
            } catch (error) {
                if (requestId !== state.loadRequestId || yearId !== el.yearFilter.value) return;
                state.records = []; state.filtered = []; state.loadFailed = true;
                showTableMessage(readError(error, 'Unable to load Class Subject configurations.'));
                notifyError(readError(error, 'Unable to load Class Subject configurations.'));
                el.add.disabled = true;
            } finally {
                if (showGlobalLoader && loaderToken != null && typeof window.hideLoader === 'function') window.hideLoader(loaderToken);
            }
        }

        function fillClassFilter() {
            const yearId = el.yearFilter.value;
            const selected = el.classFilter.value;
            const yearClasses = classesForYear(yearId);
            fillOptions(el.classFilter, yearClasses, 'All Classes', item => item.classId, item => `${item.classCode} — ${item.className}`);
            if (yearClasses.some(item => String(item.classId) === selected)) el.classFilter.value = selected;
        }

        function fillEditorLevelOptions(selectedLevelId = null) {
            fillOptions(el.level, state.levels, 'Select Level', item => item.levelId, item => item.levelName);
            if (selectedLevelId != null) el.level.value = String(selectedLevelId);
        }

        function fillEditorClassOptions(startId = null, endId = null) {
            const yearId = el.year.value;
            const levelId = el.level.value;
            const availableClasses = classesForYear(yearId, levelId);
            fillOptions(el.startClass, availableClasses, 'Select Starting Class', item => item.classId, item => `${item.classCode} — ${item.className}`);
            if (startId != null && availableClasses.some(item => String(item.classId) === String(startId))) el.startClass.value = String(startId);
            fillEndClassOptions(endId);
        }

        function fillEndClassOptions(selectedEndId = null) {
            const startId = el.startClass.value;
            const orderedClasses = classesForYear(el.year.value, el.level.value);
            const startIndex = orderedClasses.findIndex(item => String(item.classId) === String(startId));
            const available = startIndex >= 0 ? orderedClasses.slice(startIndex) : [];
            const current = selectedEndId == null ? el.endClass.value : String(selectedEndId);
            fillOptions(el.endClass, available, 'Same as Starting Class', item => item.classId, item => `${item.classCode} — ${item.className}`);
            if (current && available.some(item => String(item.classId) === current)) el.endClass.value = current;
        }

        function classesForYear(yearId, levelId = null) {
            const collator = new Intl.Collator(undefined, { numeric: true, sensitivity: 'base' });
            return state.classes
                .filter(item => String(item.academicYearId) === String(yearId) && (levelId == null || String(item.levelId) === String(levelId)))
                .sort((a, b) => collator.compare(String(a.classCode || a.className || ''), String(b.classCode || b.className || '')) || collator.compare(String(a.className || ''), String(b.className || '')));
        }

        function applyFilters() {
            const classId = el.classFilter.value;
            const requirement = el.requirementFilter.value;
            const search = el.search.value.trim().toLocaleLowerCase();
            state.filtered = state.records.filter(record => {
                const rangeIds = getRangeClassIds(record);
                const classMatch = !classId || rangeIds.some(id => String(id) === classId);
                const requirementMatch = !requirement || record.subjectRequirement === requirement;
                const subjectText = `${record.subjectCode || ''} ${record.subjectName || ''}`.toLocaleLowerCase();
                return classMatch && requirementMatch && (!search || subjectText.includes(search));
            });
            if (state.page >= pageCount()) state.page = Math.max(pageCount() - 1, 0);
            renderTable();
        }

        function renderTable() {
            el.body.replaceChildren();
            if (!state.filtered.length) {
                const message = state.loadFailed ? 'Configurations could not be loaded. Select Refresh to try again.' : state.records.length ? 'No configurations match the selected filters.' : 'No active Class Subject configurations are configured for this academic year. Select Add Configuration to begin.';
                showTableMessage(message); return;
            }
            const startIndex = state.page * state.size;
            state.filtered.slice(startIndex, startIndex + state.size).forEach((record, index) => {
                const fragment = el.template.content.cloneNode(true);
                const row = fragment.querySelector('tr');
                row.dataset.configurationId = String(record.classSubjectId); row.tabIndex = 0; row.setAttribute('role','button');
                row.setAttribute('aria-label', `View ${record.subjectName || 'subject'} configuration`);
                setText(row, '.col-serial', startIndex + index + 1); setText(row, '.col-code', record.subjectCode); setText(row, '.col-name strong', record.subjectName);
                const requirement = row.querySelector('.col-requirement');
                requirement.textContent = prettyRequirement(record.subjectRequirement);
                requirement.classList.add(record.subjectRequirement === 'COMPULSORY' ? 'active' : 'inactive');
                setText(row, '.col-range', rangeLabel(record)); setText(row, '.col-year', record.academicYearName || getYearName(record.academicYearId));
                row.querySelectorAll('button[data-id]').forEach(button => button.dataset.id = String(record.classSubjectId));
                row.querySelector('.class-subjects-view-btn').setAttribute('aria-label', `View ${record.subjectName || 'subject'} configuration`);
                row.querySelector('.class-subjects-edit-btn').setAttribute('aria-label', `Edit ${record.subjectName || 'subject'} configuration`);
                row.querySelector('.class-subjects-deactivate-btn').setAttribute('aria-label', `Deactivate ${record.subjectName || 'subject'} configuration`);
                el.body.appendChild(fragment);
            });
            updatePager();
        }

        function viewRecord(record) {
            if (state.busy) return;
            state.viewingRecord = record;
            setText(el.detailsView, '#class-subjects-detail-code', record.subjectCode);
            setText(el.detailsView, '#class-subjects-detail-name', record.subjectName);
            setText(el.detailsView, '#class-subjects-detail-requirement', prettyRequirement(record.subjectRequirement));
            setText(el.detailsView, '#class-subjects-detail-start-class', classLabel(record.startClassId, record.startClassCode));
            setText(el.detailsView, '#class-subjects-detail-end-class', record.endClassId == null ? 'Same as starting class' : classLabel(record.endClassId, record.endClassCode));
            setText(el.detailsView, '#class-subjects-detail-year', record.academicYearName || getYearName(record.academicYearId));
            setText(el.detailsView, '#class-subjects-detail-range', rangeLabel(record));
            showView('details');
        }

        function populateForm(record) {
            clearFormError();
            el.form.reset();
            el.recordId.value = record ? String(record.classSubjectId) : '';
            el.formTitle.replaceChildren();
            const titleIcon = document.createElement('i'); titleIcon.className = 'bi bi-journal-bookmark me-2';
            el.formTitle.append(titleIcon, document.createTextNode(record ? 'Edit Class Subject Configuration' : 'Add Class Subject Configuration'));
            el.formSubtitle.textContent = record ? 'Update the level, subject requirement and class range.' : 'Configure a subject requirement for a school level and class range.';
            el.save.replaceChildren();
            const icon = document.createElement('i'); icon.className = 'bi bi-check-lg';
            el.save.append(icon, document.createTextNode(record ? ' Save Changes' : ' Save Configuration'));
            el.year.value = String(record ? record.academicYearId : (el.yearFilter.value || ''));
            fillEditorLevelOptions(record?.levelId ?? null);
            el.subject.value = record ? String(record.subjectId) : '';
            el.requirement.value = record?.subjectRequirement || 'COMPULSORY';
            fillEditorClassOptions(record?.startClassId ?? null, record?.endClassId ?? null);
        }

        async function saveRecord() {
            if (state.busy) return;
            clearFormError();
            const yearId = Number(el.year.value), levelId = Number(el.level.value), subjectId = Number(el.subject.value), startClassId = Number(el.startClass.value);
            const selectedEndId = el.endClass.value ? Number(el.endClass.value) : null;
            if (![yearId, levelId, subjectId, startClassId].every(value => Number.isInteger(value) && value > 0)) {
                showFormError('Select an academic year, level, subject, and starting class.'); return;
            }
            if (!['COMPULSORY','OPTIONAL'].includes(el.requirement.value)) {
                showFormError('Select whether the subject is compulsory or optional.'); return;
            }
            const yearClassList = classesForYear(String(yearId), String(levelId));
            const start = yearClassList.find(item => Number(item.classId) === startClassId);
            const end = selectedEndId == null ? null : yearClassList.find(item => Number(item.classId) === selectedEndId);
            if (!start) { showFormError('The selected Starting Class is not available for this academic year.'); return; }
            const startIndex = yearClassList.findIndex(item => Number(item.classId) === startClassId);
            const endIndex = selectedEndId == null ? startIndex : yearClassList.findIndex(item => Number(item.classId) === selectedEndId);
            if (selectedEndId != null && (!end || endIndex < startIndex)) {
                showFormError('Ending Class must be the same as or after Starting Class.'); return;
            }
            const payload = { academicYearId:yearId, levelId, subjectId, startClassId, endClassId:selectedEndId === startClassId ? null : selectedEndId, subjectRequirement:el.requirement.value };
            state.busy = true; setBusy(true);
            let loaderToken = null;
            try {
                if (typeof showLoader === 'function') loaderToken = showLoader(el.recordId.value ? 'Updating Class Subject configuration...' : 'Saving Class Subject configuration...');
                const id = el.recordId.value;
                const response = id ? await apiPut(`${API.configurations}/${encodeURIComponent(id)}`, payload) : await apiPost(API.configurations, payload);
                notifySuccess(response?.message || (id ? 'Class Subject configuration updated successfully.' : 'Class Subject configuration created successfully.'));
                el.yearFilter.value = String(yearId);
                showView('table');
                await loadYearConfigurations({ showGlobalLoader: false, showInlineLoading: false });
            } catch (error) {
                const message = readError(error, 'Unable to save Class Subject configuration.');
                showFormError(message); notifyError(message);
            } finally {
                if (loaderToken && typeof hideLoader === 'function') hideLoader(loaderToken);
                state.busy = false; setBusy(false);
            }
        }

        async function deactivateRecord(record) {
            if (state.busy) return;
            const classRange = rangeLabel(record);
            const confirmed = window.confirm(`Deactivate the ${record.subjectName || 'selected subject'} configuration for ${classRange}? It will no longer appear in active Class Subject configurations.`);
            if (!confirmed) return;
            state.busy = true; setBusy(true);
            let loaderToken = null;
            try {
                if (typeof showLoader === 'function') loaderToken = showLoader('Deactivating Class Subject configuration...');
                const response = await apiPatch(`${API.configurations}/${encodeURIComponent(record.classSubjectId)}/deactivate`, {});
                notifySuccess(response?.message || 'Class Subject configuration deactivated successfully.');
                state.viewingRecord = null; showView('table'); await loadYearConfigurations({ showGlobalLoader: false, showInlineLoading: false });
            } catch (error) { notifyError(readError(error, 'Unable to deactivate Class Subject configuration.')); }
            finally { if (loaderToken && typeof hideLoader === 'function') hideLoader(loaderToken); state.busy = false; setBusy(false); }
        }

        function setBusy(busy) {
            el.add.disabled = busy || !el.yearFilter.value || !classesForYear(el.yearFilter.value).length || !state.subjects.length;
            el.refresh.disabled = busy; el.yearFilter.disabled = busy; el.classFilter.disabled = busy; el.requirementFilter.disabled = busy; el.search.disabled = busy;
            el.save.disabled = busy; el.cancel.disabled = busy; el.formBack.disabled = busy; el.detailEdit.disabled = busy; el.detailDeactivate.disabled = busy;
            root.setAttribute('aria-busy', String(busy));
        }

        function showView(view) {
            el.tableView.classList.toggle('hidden', view !== 'table');
            el.formView.classList.toggle('hidden', view !== 'form');
            el.detailsView.classList.toggle('hidden', view !== 'details');
            window.scrollTo({ top:0, behavior:'smooth' });
            if (view === 'form') window.setTimeout(() => el.year.focus(), 0);
        }
        function showTableLoading(message) { if (typeof window.renderFetchingMessage === 'function') window.renderFetchingMessage(el.body, 7, message); else showTableMessage(message); }
        function showTableMessage(message) {
            el.body.replaceChildren(); const row = document.createElement('tr'), cell = document.createElement('td');
            cell.colSpan = 7; cell.className = 'text-center text-muted p-4'; cell.textContent = message; row.appendChild(cell); el.body.appendChild(row); updatePager();
        }
        function updatePager() {
            const pages = pageCount();
            el.pageInfo.textContent = state.filtered.length ? `Showing page ${state.page + 1} of ${pages} (${state.filtered.length} configurations)` : 'No records';
            el.prev.disabled = state.page <= 0 || !state.filtered.length; el.next.disabled = state.page + 1 >= pages || !state.filtered.length;
        }
        function pageCount() { return Math.max(1, Math.ceil(state.filtered.length / Math.max(1, state.size))); }
        function fillOptions(select, records, placeholder, valueOf, labelOf) {
            const previous = select.value; select.replaceChildren(new Option(placeholder, ''));
            records.forEach(record => { const id = valueOf(record), label = labelOf(record); if (id != null && label != null) select.add(new Option(String(label), String(id))); });
            if (previous && [...select.options].some(option => option.value === previous)) select.value = previous;
        }
        function setText(container, selector, value) { const target = container.querySelector(selector); if (target) target.textContent = value == null || value === '' ? '—' : String(value); }
        function getYearName(id) { return state.years.find(item => String(item.academicYearId) === String(id))?.academicYearName || '—'; }
        function classLabel(id, fallbackCode) { const item = state.classes.find(entry => String(entry.classId) === String(id)); return item ? `${item.classCode} — ${item.className}` : (fallbackCode || '—'); }
        function rangeLabel(record) {
            const start = record.startClassCode || state.classes.find(item => String(item.classId) === String(record.startClassId))?.classCode || '—';
            const end = record.endClassId == null ? start : (record.endClassCode || state.classes.find(item => String(item.classId) === String(record.endClassId))?.classCode || start);
            return `${record.levelName || state.levels.find(item => String(item.levelId) === String(record.levelId))?.levelName || 'Level'}: ${start === end ? start : `${start} – ${end}`}`;
        }
        function getRangeClassIds(record) {
            const ordered = classesForYear(record.academicYearId, record.levelId); const startId = record.startClassId, endId = record.endClassId;
            if (endId == null) return [startId];
            const start = ordered.findIndex(item => String(item.classId) === String(startId)); const end = ordered.findIndex(item => String(item.classId) === String(endId));
            if (start < 0 || end < start) return [startId, endId];
            return ordered.slice(start, end + 1).map(item => item.classId);
        }
        function clearFormError() { el.formError.classList.add('hidden'); el.formErrorText.textContent = ''; }
        function showFormError(message) { el.formErrorText.textContent = message; el.formError.classList.remove('hidden'); }
                function asArray(value) { return Array.isArray(value) ? value : Array.isArray(value?.content) ? value.content : []; }
        function prettyRequirement(value) { return value === 'COMPULSORY' ? 'Compulsory' : value === 'OPTIONAL' ? 'Optional' : String(value || '—'); }
        function readError(error, fallback) { return error?.data?.message || error?.message || fallback; }
        function notifySuccess(message) { if (typeof showSuccessMessage === 'function') showSuccessMessage(message); else console.info(message); }
        function notifyError(message) { if (typeof showErrorMessage === 'function') showErrorMessage(message); else console.error(message); }
    }
})();

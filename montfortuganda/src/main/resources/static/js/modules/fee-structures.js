/* global apiGet, apiPost, apiPut, apiPatch, showSuccessMessage, showErrorMessage, showLoader, hideLoader */
(() => {
    'use strict';

    const ROOT = '#ba-fee-structures-view';
    const API = Object.freeze({
        structures: '/branchadmin/fees/structures',
        years: '/academic-years',
        terms: yearId => `/academic-terms?academicYearId=${encodeURIComponent(yearId)}`,
        classes: '/classes'
    });
    const PAGE_SIZE = 10;
    let state = { root: null, structures: [], years: [], terms: [], classes: [], page: 1,
        selected: null, editingItem: null, itemModal: null, assignModal: null, busy: false };

    document.addEventListener('viewLoaded', event => {
        if (String(event.detail?.role || '').toLowerCase().replace(/_/g, '-') !== 'admin' || event.detail?.view !== 'fee-structures') return;
        const init = initialize();
        if (typeof event.detail.waitUntil === 'function') event.detail.waitUntil(init);
        init.catch(error => { console.error('Fee Structures initialization failed.', error); notifyError(error.message || 'Unable to load Fee Structures.'); });
    });

    async function initialize() {
        const root = document.querySelector(ROOT);
        if (!root || root.dataset.initialized === 'true') return;
        state.root = root;
        state.itemModal = window.bootstrap?.Modal ? new bootstrap.Modal(root.querySelector('#fee-structure-item-modal')) : null;
        state.assignModal = window.bootstrap?.Modal ? new bootstrap.Modal(root.querySelector('#fee-structure-assign-modal')) : null;
        bindEvents();
        root.dataset.initialized = 'true';
        // Independent reference lists load concurrently; no serial await waterfall.
        // Load independent lists together. Terms depend on an academic year,
        // so they are requested only after the years list has returned.
        const results = await Promise.allSettled([
            apiGet(API.years, { silent: true }),
            apiGet(API.classes, { silent: true }),
            loadStructures(true)
        ]);
        state.years = results[0].status === 'fulfilled' ? unwrapList(results[0].value) : [];
        state.classes = results[1].status === 'fulfilled' ? unwrapList(results[1].value) : [];
        populateSelects();
        const filterYear = state.root.querySelector('#fee-structures-filter-year');
        const editorYear = state.root.querySelector('#fee-structures-year');
        const initialYearId = filterYear.value || editorYear.value || (state.years[0] ? String(yearId(state.years[0])) : '');
        const termResult = initialYearId ? await loadTerms(initialYearId) : { ok: true };
        if (filterYear && !filterYear.value && initialYearId) filterYear.value = initialYearId;
        if (editorYear && !editorYear.value && initialYearId) editorYear.value = initialYearId;
        updateTermsFilter(); updateTermsEditor();
        const failed = results.slice(0, 2).filter(x => x.status === 'rejected');
        if (failed.length || !termResult.ok) notifyError('Some reference lists could not be loaded. Check the selected academic year and your access, then refresh.');
        renderTable();
        if (results[2].status === 'rejected') throw results[2].reason;
    }

    function bindEvents() {
        const $ = selector => state.root.querySelector(selector);
        $('#fee-structures-refresh').addEventListener('click', () => refreshAll());
        $('#fee-structures-filter-form').addEventListener('submit', e => { e.preventDefault(); state.page = 1; loadStructures().then(renderTable).catch(report); });
        $('#fee-structures-reset-filters').addEventListener('click', () => {
            $('#fee-structures-filter-year').value = ''; $('#fee-structures-filter-term').value = '';
            $('#fee-structures-filter-class').value = ''; $('#fee-structures-filter-status').value = '';
            state.page = 1; loadStructures().then(renderTable).catch(report);
        });
        $('#fee-structures-add').addEventListener('click', () => openCreate());
        $('#fee-structures-editor-back').addEventListener('click', showList);
        $('#fee-structures-save-details').addEventListener('click', saveDetails);
        $('#fee-structures-add-item').addEventListener('click', () => openItemEditor(null));
        $('#fee-structure-item-form').addEventListener('submit', saveItem);
        $('#fee-structure-assign-form').addEventListener('submit', assignStructure);
        $('#fee-structures-publish').addEventListener('click', () => mutateSelected('publish', 'Publish this fee structure? Published structures cannot be edited.'));
        $('#fee-structures-archive').addEventListener('click', () => mutateSelected('archive', 'Archive this fee structure?'));
        $('#fee-structures-assign').addEventListener('click', () => {
            if (!state.selected || state.selected.structureStatus !== 'PUBLISHED') return notifyError('Only published structures can be assigned.');
            $('#fee-assign-student-ids').value = '';
            state.assignModal?.show();
            if (!state.assignModal) $('#fee-structure-assign-modal').hidden = false;
        });
        $('#fee-structures-prev').addEventListener('click', () => { if (state.page > 1) { state.page--; renderTable(); } });
        $('#fee-structures-next').addEventListener('click', () => { if (state.page < pageCount()) { state.page++; renderTable(); } });
        $('#fee-structures-table-body').addEventListener('click', handleTableAction);
        $('#fee-structures-filter-year').addEventListener('change', async () => {
            const yearIdValue = $('#fee-structures-filter-year').value;
            if (yearIdValue) await loadTerms(yearIdValue);
            else state.terms = [];
            updateTermsFilter();
        });
        $('#fee-structures-year').addEventListener('change', async () => {
            const yearIdValue = $('#fee-structures-year').value;
            if (yearIdValue) await loadTerms(yearIdValue);
            else state.terms = [];
            updateTermsEditor();
        });
    }

    async function refreshAll() {
        if (state.busy) return;
        state.busy = true;
        try {
            const results = await Promise.allSettled([
                apiGet(API.years, { silent: true }),
                apiGet(API.classes, { silent: true }),
                loadStructures(true)
            ]);
            if (results[0].status === 'fulfilled') state.years = unwrapList(results[0].value);
            if (results[1].status === 'fulfilled') state.classes = unwrapList(results[1].value);
            populateSelects();
            const yearIdValue = state.root.querySelector('#fee-structures-filter-year').value
                || state.root.querySelector('#fee-structures-year').value
                || (state.years[0] ? String(yearId(state.years[0])) : '');
            const termsResult = yearIdValue ? await loadTerms(yearIdValue) : { ok: true };
            updateTermsFilter(); updateTermsEditor(); renderTable();
            const error = results.find(x => x.status === 'rejected');
            if (error) report(error.reason);
            else if (!termsResult.ok) notifyError('Academic terms could not be loaded for the selected year.');
            else notifySuccess('Fee Structures refreshed.');
        } finally { state.busy = false; }
    }

    async function loadStructures(silent = false) {
        const $ = selector => state.root.querySelector(selector);
        const params = new URLSearchParams();
        const year = $('#fee-structures-filter-year').value, term = $('#fee-structures-filter-term').value;
        const cls = $('#fee-structures-filter-class').value, status = $('#fee-structures-filter-status').value;
        if (year) params.set('academicYearId', year);
        if (term) params.set('termId', term);
        if (cls) params.set('classId', cls);
        if (status) params.set('status', status);
        const result = await apiGet(`${API.structures}${params.size ? '?' + params.toString() : ''}`, { silent: true });
        state.structures = unwrapList(result);
        if (!silent) state.page = 1;
        return state.structures;
    }

    function populateSelects() {
        const $ = selector => state.root.querySelector(selector);
        fillSelect($('#fee-structures-filter-year'), state.years, 'All academic years', yearId, yearLabel);
        fillSelect($('#fee-structures-year'), state.years, 'Select academic year', yearId, yearLabel);
        fillSelect($('#fee-structures-filter-term'), state.terms, 'All terms', termId, termLabel);
        fillSelect($('#fee-structures-term'), state.terms, 'Select term', termId, termLabel);
        fillSelect($('#fee-structures-filter-class'), state.classes, 'All classes', classId, classLabel);
        fillSelect($('#fee-structures-class'), state.classes, 'Select class', classId, classLabel);
    }

    async function loadTerms(academicYearId) {
        if (!academicYearId) { state.terms = []; return { ok: true }; }
        try {
            const result = await apiGet(API.terms(academicYearId), { silent: true });
            state.terms = unwrapList(result);
            return { ok: true };
        } catch (error) {
            state.terms = [];
            console.error(`Unable to load academic terms for academic year ${academicYearId}.`, error);
            return { ok: false, error };
        }
    }
    function updateTermsFilter() {
        const yearIdValue = state.root.querySelector('#fee-structures-filter-year').value;
        const filtered = yearIdValue ? state.terms.filter(t => !termYearId(t) || String(termYearId(t)) === yearIdValue) : state.terms;
        fillSelect(state.root.querySelector('#fee-structures-filter-term'), filtered, 'All terms', termId, termLabel);
    }
    function updateTermsEditor() {
        const yearIdValue = state.root.querySelector('#fee-structures-year').value;
        const filtered = yearIdValue ? state.terms.filter(t => !termYearId(t) || String(termYearId(t)) === yearIdValue) : state.terms;
        const select = state.root.querySelector('#fee-structures-term'), current = select.value;
        fillSelect(select, filtered, 'Select term', termId, termLabel);
        if (filtered.some(t => String(termId(t)) === current)) select.value = current;
    }

    function renderTable() {
        const $ = selector => state.root.querySelector(selector);
        const tbody = $('#fee-structures-table-body');
        const pages = pageCount();
        if (state.page > pages) state.page = pages;
        const start = (state.page - 1) * PAGE_SIZE;
        const visible = state.structures.slice(start, start + PAGE_SIZE);
        if (!visible.length) tbody.innerHTML = '<tr><td colspan="9" class="text-center py-4 text-muted">No fee structures found.</td></tr>';
        else tbody.innerHTML = visible.map(s => {
            const activeItems = (s.items || []).filter(i => i.active !== false && String(i.status || '').toUpperCase() !== 'INACTIVE');
            const total = activeItems.reduce((sum, i) => sum + Number(i.amount || 0), 0);
            return `<tr>
                <td>${esc(s.academicYearName || s.academicYearCode || '—')}</td>
                <td>${esc(s.termName || s.termCode || '—')}</td>
                <td>${esc([s.classCode, s.className].filter(Boolean).join(' — ') || '—')}</td>
                <td>${esc(s.versionNo ?? '—')}</td><td>${activeItems.length}</td><td>${money(total)}</td>
                <td><span class="badge ${statusClass(s.structureStatus)}">${esc(s.structureStatus === 'DRAFT' ? 'In Progress' : (s.structureStatus || 'UNKNOWN'))}</span></td>
                <td>${esc(formatDate(s.updatedAt || s.createdAt))}</td>
                <td class="text-end text-nowrap"><button class="btn-secondary btn-sm" data-action="open" data-id="${s.feeStructureId}">Open</button></td>
            </tr>`;
        }).join('');
        $('#fee-structures-count').textContent = `${state.structures.length} record${state.structures.length === 1 ? '' : 's'}`;
        $('#fee-structures-page').textContent = `Page ${state.page} of ${pages}`;
        $('#fee-structures-prev').disabled = state.page <= 1;
        $('#fee-structures-next').disabled = state.page >= pages;
    }
    function pageCount() { return Math.max(1, Math.ceil(state.structures.length / PAGE_SIZE)); }

    async function handleTableAction(event) {
        const button = event.target.closest('[data-action="open"]');
        if (!button) return;
        const record = state.structures.find(s => String(s.feeStructureId) === button.dataset.id);
        if (!record) return;
        try {
            const response = await apiGet(`${API.structures}/${record.feeStructureId}`, { silent: true });
            state.selected = unwrap(response);
            openEditor(state.selected);
        } catch (error) { report(error); }
    }

    function openCreate() {
        state.selected = null;
        const $ = selector => state.root.querySelector(selector);
        $('#fee-structures-editor-title').textContent = 'Create Fee Structure';
        $('#fee-structures-year').disabled = false; $('#fee-structures-term').disabled = false; $('#fee-structures-class').disabled = false;
        $('#fee-structures-year').value = ''; $('#fee-structures-term').value = ''; $('#fee-structures-class').value = '';
        $('#fee-structures-remarks').value = ''; $('#fee-structures-save-details').hidden = false;
        $('#fee-structures-items-panel').hidden = true; $('#fee-structures-edit-note').hidden = true;
        showEditor();
    }
    function openEditor(record) {
        const $ = selector => state.root.querySelector(selector);
        $('#fee-structures-editor-title').textContent = `Fee Structure — ${record.classCode || record.className || 'Class'} / ${record.termName || record.termCode || 'Term'}`;
        $('#fee-structures-year').value = String(record.academicYearId || '');
        updateTermsEditor(); $('#fee-structures-term').value = String(record.termId || '');
        $('#fee-structures-class').value = String(record.classId || '');
        $('#fee-structures-remarks').value = record.remarks || '';
        const locked = record.structureStatus !== 'DRAFT';
        $('#fee-structures-year').disabled = true; $('#fee-structures-term').disabled = true; $('#fee-structures-class').disabled = true;
        $('#fee-structures-save-details').hidden = locked;
        $('#fee-structures-items-panel').hidden = false;
        $('#fee-structures-edit-note').hidden = false;
        $('#fee-structures-add-item').disabled = locked;
        $('#fee-structures-publish').disabled = locked;
        $('#fee-structures-archive').disabled = record.structureStatus === 'ARCHIVED';
        $('#fee-structures-assign').disabled = record.structureStatus !== 'PUBLISHED';
        $('#fee-structures-status-note').textContent = locked ? 'This structure is not a draft. Its details and components are read-only.' : 'Add the required fee components, review the total, then publish.';
        renderItems(); showEditor();
    }
    function showEditor() { state.root.querySelector('#fee-structures-list-view').hidden = true; state.root.querySelector('#fee-structures-editor-view').hidden = false; }
    function showList() { state.root.querySelector('#fee-structures-editor-view').hidden = true; state.root.querySelector('#fee-structures-list-view').hidden = false; loadStructures(true).then(renderTable).catch(report); }

    async function saveDetails() {
        const $ = selector => state.root.querySelector(selector);
        if (state.busy) return;
        const academicYearId = $('#fee-structures-year').value, termIdValue = $('#fee-structures-term').value, classIdValue = $('#fee-structures-class').value;
        if (!academicYearId || !termIdValue || !classIdValue) return notifyError('Select academic year, term and class.');
        state.busy = true; $('#fee-structures-save-details').disabled = true;
        try {
            if (!state.selected) {
                const result = await apiPost(API.structures, { academicYearId: Number(academicYearId), termId: Number(termIdValue), classId: Number(classIdValue), remarks: $('#fee-structures-remarks').value.trim() });
                state.selected = unwrap(result);
                notifySuccess('Fee structure details saved. Continue adding fee components.');
            } else {
                const result = await apiPut(`${API.structures}/${state.selected.feeStructureId}`, { remarks: $('#fee-structures-remarks').value.trim() });
                state.selected = unwrap(result); notifySuccess('Fee structure details updated.');
            }
            openEditor(state.selected);
            await loadStructures(true); renderTable();
        } catch (error) { report(error); }
        finally { state.busy = false; $('#fee-structures-save-details').disabled = false; }
    }

    function openItemEditor(item) {
        if (!state.selected || state.selected.structureStatus !== 'DRAFT') return notifyError('Only draft structures can be edited.');
        state.editingItem = item || null;
        const $ = selector => state.root.querySelector(selector);
        $('#fee-structure-item-modal-title').textContent = item ? 'Edit Fee Component' : 'Add Fee Component';
        $('#fee-item-type').value = item?.feeType || ''; $('#fee-item-name').value = item?.feeName || '';
        $('#fee-item-amount').value = item?.amount ?? ''; $('#fee-item-frequency').value = item?.chargingFrequency || 'ONE_TIME';
        $('#fee-item-due-date').value = item?.dueDate || ''; $('#fee-item-order').value = item?.displayOrder ?? (state.selected.items || []).length;
        $('#fee-item-mandatory').checked = item?.mandatory !== false; $('#fee-item-remarks').value = item?.remarks || '';
        if (state.itemModal) state.itemModal.show(); else $('#fee-structure-item-modal').hidden = false;
    }

    async function saveItem(event) {
        event.preventDefault();
        if (state.busy || !state.selected) return;
        const $ = selector => state.root.querySelector(selector);
        const amount = Number($('#fee-item-amount').value);
        if (!Number.isFinite(amount) || amount < 0) return notifyError('Enter a valid fee amount.');
        const payload = {
            feeType: $('#fee-item-type').value.trim(), feeName: $('#fee-item-name').value.trim(), amount,
            chargingFrequency: $('#fee-item-frequency').value, dueDate: $('#fee-item-due-date').value || null,
            mandatory: $('#fee-item-mandatory').checked, displayOrder: Number($('#fee-item-order').value || 0),
            remarks: $('#fee-item-remarks').value.trim() || null
        };
        if (!payload.feeType || !payload.feeName) return notifyError('Fee type and fee name are required.');
        state.busy = true; $('#fee-item-save').disabled = true;
        try {
            const base = `${API.structures}/${state.selected.feeStructureId}/items`;
            const response = state.editingItem
                ? await apiPut(`${base}/${state.editingItem.feeStructureItemId}`, payload)
                : await apiPost(base, payload);
            const saved = unwrap(response);
            const items = state.selected.items || [];
            const index = items.findIndex(i => String(i.feeStructureItemId) === String(saved.feeStructureItemId));
            if (index >= 0) items[index] = saved; else items.push(saved);
            state.selected.items = items;
            state.itemModal?.hide();
            if (!state.itemModal) $('#fee-structure-item-modal').hidden = true;
            renderItems(); await loadStructures(true); renderTable(); notifySuccess('Fee component saved.');
        } catch (error) { report(error); }
        finally { state.busy = false; $('#fee-item-save').disabled = false; }
    }

    async function deactivateItem(itemId) {
        if (!window.confirm('Deactivate this fee component? Its record will be retained for audit.')) return;
        try {
            const response = await fetch(`/api${API.structures}/${state.selected.feeStructureId}/items/${itemId}`, {
                method: 'DELETE', credentials: 'include', headers: { 'Content-Type': 'application/json' }
            });
            const body = await response.json().catch(() => ({}));
            if (!response.ok) throw new Error(body.message || `Request failed (${response.status})`);
            const data = body.data ?? body;
            const items = state.selected.items || [];
            const idx = items.findIndex(i => String(i.feeStructureItemId) === String(itemId));
            if (idx >= 0) items[idx] = data;
            renderItems(); await loadStructures(true); renderTable(); notifySuccess('Fee component deactivated.');
        } catch (error) { report(error); }
    }
    function renderItems() {
        const tbody = state.root.querySelector('#fee-structures-items-body');
        const items = [...(state.selected?.items || [])].sort((a, b) => Number(a.displayOrder || 0) - Number(b.displayOrder || 0));
        const active = items.filter(i => i.active !== false && String(i.status || '').toUpperCase() !== 'INACTIVE');
        tbody.innerHTML = items.length ? items.map(i => `<tr>
            <td>${esc(i.feeType)}</td><td>${esc(i.feeName)}</td><td>${money(i.amount)}</td><td>${esc(i.chargingFrequency)}</td><td>${esc(i.dueDate || '—')}</td>
            <td>${i.mandatory ? 'Mandatory' : 'Optional'}</td><td>${i.active === false ? '<span class="badge bg-secondary">Inactive</span>' : '<span class="badge bg-success">Active</span>'}</td>
            <td class="text-end text-nowrap">${state.selected.structureStatus === 'DRAFT' && i.active !== false && String(i.status || '').toUpperCase() !== 'INACTIVE' ? `<button type="button" class="btn-secondary btn-sm" data-item-action="edit" data-id="${i.feeStructureItemId}">Edit</button> <button type="button" class="btn-secondary btn-sm" data-item-action="deactivate" data-id="${i.feeStructureItemId}">Deactivate</button>` : '—'}</td>
        </tr>`).join('') : '<tr><td colspan="8" class="text-center py-3 text-muted">No fee components added.</td></tr>';
        state.root.querySelector('#fee-structures-total').textContent = money(active.reduce((sum, i) => sum + Number(i.amount || 0), 0));
        tbody.onclick = event => {
            const btn = event.target.closest('[data-item-action]'); if (!btn) return;
            const item = items.find(i => String(i.feeStructureItemId) === btn.dataset.id); if (!item) return;
            if (btn.dataset.itemAction === 'edit') openItemEditor(item);
            if (btn.dataset.itemAction === 'deactivate') deactivateItem(btn.dataset.id);
        };
    }

    async function mutateSelected(action, promptText) {
        if (!state.selected || state.busy) return;
        if (action === 'publish' && !(state.selected.items || []).some(i => i.active !== false && String(i.status || '').toUpperCase() !== 'INACTIVE')) return notifyError('Add at least one active fee component before publishing.');
        if (!window.confirm(promptText)) return;
        state.busy = true;
        try {
            const response = await apiPost(`${API.structures}/${state.selected.feeStructureId}/${action}`, {});
            state.selected = unwrap(response); openEditor(state.selected);
            await loadStructures(true); renderTable(); notifySuccess(`Fee structure ${action === 'publish' ? 'published' : 'archived'}.`);
        } catch (error) { report(error); }
        finally { state.busy = false; }
    }
    async function assignStructure(event) {
        event.preventDefault(); if (!state.selected || state.busy) return;
        const $ = selector => state.root.querySelector(selector);
        const studentIds = $('#fee-assign-student-ids').value
            .split(/[,\s;]+/).map(value => value.trim()).filter(Boolean).map(Number);
        if (!studentIds.length || studentIds.some(id => !Number.isSafeInteger(id) || id <= 0)) {
            return notifyError('Enter one or more valid positive student IDs.');
        }
        if (new Set(studentIds).size !== studentIds.length) return notifyError('Remove duplicate student IDs.');
        state.busy = true;
        try {
            const response = await apiPost(`${API.structures}/${state.selected.feeStructureId}/assignments`, { studentIds });
            const data = unwrap(response);
            state.assignModal?.hide(); if (!state.assignModal) $('#fee-structure-assign-modal').hidden = true;
            const summary = data && typeof data === 'object' ? Object.entries(data).map(([k,v]) => `${k}: ${v}`).join('\n') : '';
            window.alert(`Assignment request completed.${summary ? '\n' + summary : ''}`);
            await loadStructures(true); renderTable();
        } catch (error) { report(error); }
        finally { state.busy = false; }
    }

    function extractList(result, label) {
        if (result.status === 'rejected') { console.warn(`Unable to load ${label}.`, result.reason); return []; }
        return unwrapList(result.value);
    }
    function unwrapList(result) { const value = unwrap(result); return Array.isArray(value) ? value : (Array.isArray(value?.content) ? value.content : []); }
    function unwrap(result) { return result && Object.prototype.hasOwnProperty.call(result, 'data') ? result.data : result; }
    function fillSelect(select, items, placeholder, getId, getLabel) {
        const current = select.value;
        select.innerHTML = `<option value="">${esc(placeholder)}</option>` + items.map(item => `<option value="${esc(getId(item))}">${esc(getLabel(item))}</option>`).join('');
        if (items.some(item => String(getId(item)) === current)) select.value = current;
    }
    function yearId(x) { return x.academicYearId ?? x.id ?? ''; }
    function yearLabel(x) { return x.academicYearName || x.academicYearCode || x.yearName || x.name || String(yearId(x)); }
    function termId(x) { return x.termId ?? x.academicTermId ?? x.id ?? ''; }
    function termLabel(x) { return x.termName || x.termCode || x.name || String(termId(x)); }
    function termYearId(x) { return x.academicYearId ?? x.yearId ?? null; }
    function classId(x) { return x.classId ?? x.id ?? ''; }
    function classLabel(x) { return [x.classCode, x.className].filter(Boolean).join(' — ') || x.name || String(classId(x)); }
    function statusClass(status) { return status === 'PUBLISHED' ? 'bg-success' : status === 'ARCHIVED' ? 'bg-secondary' : 'bg-warning text-dark'; }
    function money(value) { return Number(value || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }); }
    function formatDate(value) { if (!value) return '—'; const d = new Date(value); return Number.isNaN(d.getTime()) ? String(value) : d.toLocaleDateString(); }
    function esc(value) { return String(value ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c])); }
    function notifySuccess(message) { if (typeof showSuccessMessage === 'function') showSuccessMessage(message); else window.alert(message); }
    function notifyError(message) { if (typeof showErrorMessage === 'function') showErrorMessage(message); else window.alert(message); }
    function report(error) { console.error(error); notifyError(error?.message || 'The operation could not be completed.'); }
})();

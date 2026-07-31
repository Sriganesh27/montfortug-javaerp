/* global apiGet, apiPost, apiPut, showSuccessMessage, showErrorMessage */
(() => {
    'use strict';
    document.addEventListener('viewLoaded', event => {
        if (event.detail?.role === 'admin' && event.detail?.view === 'academic-terms') {
            const task = init();
            if (typeof event.detail.waitUntil === 'function') event.detail.waitUntil(task);
        }
    });

    async function init() {
        const root = document.querySelector('#ba-academic-terms-view');
        if (!root || root.dataset.initialized === 'true') return;
        root.dataset.initialized = 'true';

        const q = selector => root.querySelector(selector);
        const el = {
            tableView: q('#academic-term-tableView'), formView: q('#academic-term-form-view'),
            body: q('#academic-term-table-body'), rowTemplate: q('#academic-term-row-template'),
            add: q('#btn-add-academic-term'), refresh: q('#academic-term-refresh-btn'),
            back: q('#academic-term-back-btn'), cancel: q('#academic-term-cancel-btn'),
            filterForm: q('#academic-term-filter-form'), reset: q('#academic-term-reset-btn'),
            yearFilter: q('#academic-term-year-filter'), search: q('#academic-term-search-input'),
            statusFilter: q('#academic-term-status-filter'), activeFilter: q('#academic-term-active-filter'),
            pageSize: q('#academic-term-page-size'), pageInfo: q('#academic-term-page-info'),
            prev: q('#academic-term-prev-btn'), next: q('#academic-term-next-btn'),
            currentCard: q('#academic-term-current-card'), currentName: q('#academic-term-current-name'),
            currentDates: q('#academic-term-current-dates'), form: q('#academic-term-form'),
            title: q('#academic-term-form-title'), subtitle: q('#academic-term-form-subtitle'),
            id: q('#academic-term-id'), version: q('#academic-term-version'),
            year: q('#academic-term-year'), code: q('#academic-term-code'), name: q('#academic-term-name'),
            start: q('#academic-term-start-date'), end: q('#academic-term-end-date'),
            order: q('#academic-term-display-order'), status: q('#academic-term-status'),
            active: q('#academic-term-active'), current: q('#academic-term-current'),
            description: q('#academic-term-description'), error: q('#academic-term-form-error'),
            errorText: q('#academic-term-form-error-text'), save: q('#academic-term-save-btn'),
            overlay: q('#academic-term-operation-overlay'), overlayTitle: q('#academic-term-operation-title'),
            overlayMessage: q('#academic-term-operation-message')
        };
        const state = { years: [], records: [], filtered: [], page: 0, size: 10, editingId: null };

        bind();
        resetForm();
        await loadYears();
        await loadTerms();

        function bind() {
            el.add.addEventListener('click', () => { resetForm(); showForm(); });
            el.refresh.addEventListener('click', () => void loadTerms());
            el.back.addEventListener('click', showTable);
            el.cancel.addEventListener('click', showTable);
            el.filterForm.addEventListener('submit', e => { e.preventDefault(); state.page = 0; applyFilters(); });
            el.reset.addEventListener('click', () => {
                el.search.value = ''; el.statusFilter.value = ''; el.activeFilter.value = '';
                state.page = 0; applyFilters();
            });
            el.yearFilter.addEventListener('change', () => void loadTerms());
            el.pageSize.addEventListener('change', () => { state.size = Number(el.pageSize.value); state.page = 0; render(); });
            el.prev.addEventListener('click', () => { if (state.page > 0) { state.page--; render(); } });
            el.next.addEventListener('click', () => { if (state.page + 1 < pages()) { state.page++; render(); } });
            el.form.addEventListener('submit', e => { e.preventDefault(); void save(); });
            el.start.addEventListener('change', () => { el.end.min = el.start.value; });
        }

        async function loadYears() {
            const res = await apiGet('/academic-years?active=true');
            state.years = arrayOf(res?.data);
            fillSelect(el.yearFilter, state.years, 'All Academic Years');
            fillSelect(el.year, state.years, 'Select Academic Year');
            const current = state.years.find(x => x.currentYear) || state.years[0];
            if (!el.yearFilter.value && current) el.yearFilter.value = String(current.academicYearId);
        }

        async function loadTerms() {
            const yearId = Number(el.yearFilter.value || 0);
            if (!yearId) { state.records = []; applyFilters(); renderCurrent(); return; }
            showTableLoading();
            try {
                const res = await apiGet(`/academic-terms?academicYearId=${yearId}`);
                state.records = arrayOf(res?.data);
                state.page = 0;
                applyFilters();
                renderCurrent();
            } catch (e) {
                state.records = []; applyFilters(); errorNotify(readError(e, 'Failed to load Academic Terms.'));
            }
        }

        function applyFilters() {
            const s = el.search.value.trim().toLowerCase();
            const status = el.statusFilter.value;
            const active = el.activeFilter.value;
            state.filtered = state.records.filter(r =>
                (!s || String(r.termCode || '').toLowerCase().includes(s) || String(r.termName || '').toLowerCase().includes(s))
                && (!status || r.status === status)
                && (!active || String(Boolean(r.active)) === active)
            );
            if (state.page >= pages()) state.page = Math.max(pages() - 1, 0);
            render();
        }

        function render() {
            el.body.replaceChildren();
            if (!state.filtered.length) {
                const tr = document.createElement('tr'); const td = document.createElement('td');
                td.colSpan = 10; td.className = 'text-center py-4 text-muted'; td.textContent = 'No Academic Terms found.';
                tr.appendChild(td); el.body.appendChild(tr); updatePager(); return;
            }
            const start = state.page * state.size;
            state.filtered.slice(start, start + state.size).forEach((r, i) => {
                const f = el.rowTemplate.content.cloneNode(true); const row = f.querySelector('tr');
                text(row, '.col-serial', start + i + 1); text(row, '.col-code', r.termCode);
                text(row, '.col-name strong', r.termName); text(row, '.col-year', r.academicYearName || r.academicYearCode);
                text(row, '.col-duration', range(r.startDate, r.endDate)); text(row, '.col-order', r.displayOrder);
                badge(row.querySelector('.col-status'), r.status, statusClass(r.status));
                badge(row.querySelector('.col-current'), r.currentTerm ? 'CURRENT' : 'NO', r.currentTerm ? 'active' : 'inactive');
                badge(row.querySelector('.col-active'), r.active ? 'ACTIVE' : 'INACTIVE', r.active ? 'active' : 'inactive');
                row.querySelector('.academic-term-edit-btn').addEventListener('click', () => edit(r));
                const currentBtn = row.querySelector('.academic-term-current-btn');
                if (r.currentTerm) currentBtn.disabled = true;
                else currentBtn.addEventListener('click', () => void makeCurrent(r));
                row.querySelector('.academic-term-active-btn').addEventListener('click', () => void changeActive(r));
                el.body.appendChild(f);
            });
            updatePager();
        }

        function edit(r) {
            state.editingId = Number(r.termId); el.id.value = r.termId; el.version.value = r.version ?? '';
            el.year.value = r.academicYearId; el.code.value = r.termCode || ''; el.name.value = r.termName || '';
            el.start.value = r.startDate || ''; el.end.value = r.endDate || ''; el.order.value = r.displayOrder || 1;
            el.status.value = r.status || 'PLANNED'; el.active.value = String(Boolean(r.active));
            el.current.value = String(Boolean(r.currentTerm)); el.description.value = r.description || '';
            el.title.textContent = 'Edit Academic Term'; el.subtitle.textContent = 'Update this Term for your branch.';
            clearError(); showForm();
        }

        function resetForm() {
            state.editingId = null; el.form.reset(); el.id.value = ''; el.version.value = '';
            el.active.value = 'true'; el.current.value = 'false'; el.status.value = 'PLANNED'; el.order.value = '1';
            if (el.yearFilter.value) el.year.value = el.yearFilter.value;
            el.title.textContent = 'Create Academic Term';
            el.subtitle.textContent = 'Create a Term under an Academic Year of your branch.';
            clearError();
        }

        async function save() {
            const message = validate(); if (message) { showError(message); return; }
            const payload = {
                academicYearId: Number(el.year.value), termCode: el.code.value.trim(),
                termName: el.name.value.trim(), startDate: el.start.value, endDate: el.end.value,
                displayOrder: Number(el.order.value), status: el.status.value,
                currentTerm: el.current.value === 'true', description: nullable(el.description.value),
                active: el.active.value === 'true', version: nullable(el.version.value)
            };
            showOverlay(state.editingId ? 'Updating Academic Term' : 'Creating Academic Term', 'Please wait while the Academic Term is saved.');
            el.save.disabled = true;
            try {
                const res = state.editingId
                    ? await apiPut(`/academic-terms/${state.editingId}`, payload)
                    : await apiPost('/academic-terms', payload);
                successNotify(res?.message || 'Academic Term saved successfully.');
                el.yearFilter.value = String(payload.academicYearId); showTable(); await loadTerms();
            } catch (e) { const m = readError(e, 'Unable to save Academic Term.'); showError(m); errorNotify(m); }
            finally { el.save.disabled = false; hideOverlay(); }
        }

        async function makeCurrent(r) {
            if (!confirm(`Make "${r.termName}" the current Term?`)) return;
            showOverlay('Updating Current Term', 'Please wait while the current Term is updated.');
            try { const res = await patch(`/academic-terms/${r.termId}/current`); successNotify(res?.message || 'Current Term updated.'); await loadTerms(); }
            catch (e) { errorNotify(readError(e, 'Unable to update current Term.')); } finally { hideOverlay(); }
        }

        async function changeActive(r) {
            const next = !Boolean(r.active); if (!confirm(`${next ? 'Activate' : 'Deactivate'} "${r.termName}"?`)) return;
            showOverlay(next ? 'Activating Academic Term' : 'Deactivating Academic Term', 'Please wait while the record state is updated.');
            try {
                const qs = new URLSearchParams({ active: String(next), version: String(r.version ?? 0) });
                const res = await patch(`/academic-terms/${r.termId}/active-status?${qs}`);
                successNotify(res?.message || 'Academic Term state updated.'); await loadTerms();
            } catch (e) { errorNotify(readError(e, 'Unable to change Academic Term state.')); } finally { hideOverlay(); }
        }

        function validate() {
            if (!el.year.value) return 'Academic Year is required.';
            if (!el.code.value.trim()) return 'Term code is required.';
            if (!el.name.value.trim()) return 'Term name is required.';
            if (!el.start.value || !el.end.value) return 'Start Date and End Date are required.';
            if (el.end.value < el.start.value) return 'End Date cannot be before Start Date.';
            if (!Number(el.order.value) || Number(el.order.value) < 1) return 'Display Order must be greater than zero.';
            if (!el.status.value) return 'Term status is required.';
            return '';
        }

        function renderCurrent() {
            const r = state.records.find(x => x.currentTerm && x.active);
            if (!r) { el.currentCard.classList.add('hidden'); return; }
            el.currentName.textContent = `${r.termCode} - ${r.termName}`;
            el.currentDates.textContent = `${r.academicYearName || r.academicYearCode} | ${range(r.startDate, r.endDate)}`;
            el.currentCard.classList.remove('hidden');
        }

        function fillSelect(select, records, first) {
            const selected = select.value; select.replaceChildren(new Option(first, ''));
            records.forEach(r => select.add(new Option(`${r.academicYearCode} - ${r.academicYearName}`, r.academicYearId)));
            if ([...select.options].some(o => o.value === selected)) select.value = selected;
        }
        function showForm() { el.tableView.classList.add('hidden'); el.formView.classList.remove('hidden'); window.scrollTo({top:0, behavior:'smooth'}); }
        function showTable() { el.formView.classList.add('hidden'); el.tableView.classList.remove('hidden'); window.scrollTo({top:0, behavior:'smooth'}); }
        function showTableLoading() { el.body.innerHTML = '<tr><td colspan="10" class="text-center py-4">Loading Academic Terms...</td></tr>'; }
        function pages() { return Math.max(Math.ceil(state.filtered.length / state.size), 1); }
        function updatePager() { const total = state.filtered.length; el.pageInfo.textContent = total ? `Showing page ${state.page + 1} of ${pages()} (${total} records)` : 'No records'; el.prev.disabled = !total || state.page <= 0; el.next.disabled = !total || state.page + 1 >= pages(); }
        function showOverlay(t,m){ el.overlayTitle.textContent=t; el.overlayMessage.textContent=m; el.overlay.classList.add('is-visible'); document.body.classList.add('modal-open'); }
        function hideOverlay(){ el.overlay.classList.remove('is-visible'); document.body.classList.remove('modal-open'); }
        function showError(m){ el.errorText.textContent=m; el.error.classList.remove('hidden'); }
        function clearError(){ el.errorText.textContent=''; el.error.classList.add('hidden'); }
    }

    async function patch(path) {
        const response = await fetch(`/api${path}`, { method:'PATCH', credentials:'include', cache:'no-store', headers:{'Content-Type':'application/json'} });
        const text = await response.text(); let body; try { body = text ? JSON.parse(text) : null; } catch { body = text; }
        if (!response.ok) { const e = new Error(body?.message || `HTTP Error: ${response.status}`); e.data = body; throw e; }
        return body;
    }
    const arrayOf = v => Array.isArray(v) ? v : (Array.isArray(v?.content) ? v.content : []);
    const nullable = v => String(v ?? '').trim() === '' ? null : v;
    const text = (p,s,v) => { const n=p.querySelector(s); if(n) n.textContent=String(v ?? '-'); };
    const badge = (n,t,c) => { if(n){ n.textContent=t || '-'; n.className=`status-badge badge ${c}`; } };
    const statusClass = s => s === 'ACTIVE' ? 'active' : (s === 'PLANNED' ? 'pending' : 'inactive');
    const formatDate = v => { const p=String(v||'').split('-'); return p.length===3 ? `${p[2]}/${p[1]}/${p[0]}` : (v||'-'); };
    const range = (a,b) => a || b ? `${formatDate(a)} - ${formatDate(b)}` : '-';
    const readError = (e,f) => e?.data?.message || e?.message || f;
    const successNotify = m => typeof showSuccessMessage === 'function' ? showSuccessMessage(m) : console.log(m);
    const errorNotify = m => typeof showErrorMessage === 'function' ? showErrorMessage(m) : console.error(m);
})();
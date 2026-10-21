/* global apiGet, apiPost, apiPut, showSuccessMessage, showErrorMessage, showLoader, hideLoader */
(() => {
    'use strict';
    document.addEventListener('viewLoaded', event => {
        if (event.detail?.role !== 'admin' || event.detail?.view !== 'classes') return;
        // Do not attach the Classes initialization promise to the dashboard's
        // view transition. A slow reference-data request must not hold or reset
        // navigation while the user is opening Add/Edit/View.
        void initClasses().catch(error => {
            console.error('Classes view initialization failed.', error);
            if (typeof showErrorMessage === 'function') {
                showErrorMessage('Classes could not be loaded. Please refresh and try again.');
            }
        });
    });

    async function initClasses() {
        const root = document.querySelector('#ba-classes-view');
        if (!root || root.dataset.initialized === 'true') return;
        root.dataset.initialized = 'true';
        const q = selector => root.querySelector(selector);
        const el = {
            tableView:q('#class-table-view'), formView:q('#class-form-view'), detailsView:q('#class-details-view'), body:q('#class-table-body'), template:q('#class-row-template'),
            add:q('#class-add-btn'), refresh:q('#class-refresh-btn'), filterForm:q('#class-filter-form'), reset:q('#class-reset-btn'), yearFilter:q('#class-year-filter'), levelFilter:q('#class-level-filter'), search:q('#class-search'), statusFilter:q('#class-status-filter'),
            pageSize:q('#class-page-size'), pageInfo:q('#class-page-info'), prev:q('#class-prev-btn'), next:q('#class-next-btn'),
            form:q('#class-form'), formTitle:q('#class-form-title'), formSubtitle:q('#class-form-subtitle'), id:q('#class-id'), year:q('#class-year'), level:q('#class-level'), code:q('#class-code'), name:q('#class-name'), status:q('#class-status'),
            formError:q('#class-form-error'), formErrorText:q('#class-form-error-text'), save:q('#class-save-btn'), back:q('#class-back-btn'), cancel:q('#class-cancel-btn'),
            detailCode:q('#class-detail-code'), detailName:q('#class-detail-name'), detailLevel:q('#class-detail-level'), detailYear:q('#class-detail-year'), detailStatus:q('#class-detail-status'), detailEdit:q('#class-details-edit-btn'), detailBack:q('#class-details-back-btn')
        };
        const state = { years:[], levels:[], records:[], filtered:[], page:0, size:10, editingId:null, viewedRecord:null, busy:false };

        bind(); resetForm();
        showTableLoading('Loading classes...');
        try { await loadReferences(); await loadClasses(); }
        catch (error) { notifyError(readError(error, 'Unable to load Classes.')); }

        function bind() {
            el.add.addEventListener('click', () => { resetForm(); showView('form'); });
            el.refresh.addEventListener('click', () => void refreshClasses());
            el.back.addEventListener('click', () => showView('table')); el.cancel.addEventListener('click', () => showView('table')); el.detailBack.addEventListener('click', () => showView('table'));
            el.detailEdit.addEventListener('click', () => { if (state.viewedRecord) { populateForm(state.viewedRecord); showView('form'); } });
            el.filterForm.addEventListener('submit', event => { event.preventDefault(); state.page=0; applyFilters(); });
            el.reset.addEventListener('click', () => { el.yearFilter.value=''; el.levelFilter.value=''; el.search.value=''; el.statusFilter.value=''; state.page=0; applyFilters(); });
            el.pageSize.addEventListener('change', () => { state.size=Number(el.pageSize.value)||10; state.page=0; render(); });
            el.prev.addEventListener('click', () => { if (state.page>0) { state.page--; render(); } });
            el.next.addEventListener('click', () => { if (state.page+1<pageCount()) { state.page++; render(); } });
            el.body.addEventListener('click', event => {
                const button=event.target.closest('button');
                if (button) {
                    const id=button.dataset.id; if (!id) return;
                    if (button.classList.contains('class-view-btn')) { event.stopPropagation(); void viewRecord(id); return; }
                    if (button.classList.contains('class-edit-btn')) { event.stopPropagation(); void editRecord(id); return; }
                    return;
                }
                const rowElement=event.target.closest('tr[data-class-id]');
                if (rowElement) void viewRecord(rowElement.dataset.classId);
            });
            el.body.addEventListener('keydown', event => {
                const row=event.target.closest('tr[data-class-id]');
                if (row && (event.key==='Enter' || event.key===' ')) { event.preventDefault(); void viewRecord(row.dataset.classId); }
            });
            el.form.addEventListener('submit', event => { event.preventDefault(); void saveClass(); });
        }

        async function loadReferences() {
            const [yearsResponse, referenceResponse] = await Promise.all([apiGet('/academic-years'), apiGet('/students/reference-data')]);
            state.years = asArray(yearsResponse?.data);
            const reference = referenceResponse?.data || {};
            state.levels = asArray(reference.levels || reference.levelOptions);
            fillOptions(el.year, state.years, 'Select Academic Year', ['academicYearId','id'], ['academicYearName','yearName','name']);
            fillOptions(el.yearFilter, state.years, 'All Academic Years', ['academicYearId','id'], ['academicYearName','yearName','name']);
            fillOptions(el.level, state.levels, 'Select Level', ['levelId','id'], ['levelName','name']);
            fillOptions(el.levelFilter, state.levels, 'All Levels', ['levelId','id'], ['levelName','name']);
        }

        async function refreshClasses() {
            showTableLoading('Refreshing classes...');
            await loadClasses();
        }

        async function loadClasses() {
            try {
                const response=await apiGet('/classes');
                state.records=asArray(response?.data); state.page=0; applyFilters();
            } catch (error) {
                state.records=[]; state.filtered=[]; tableMessage(readError(error, 'Unable to load Classes.')); notifyError(readError(error, 'Unable to load Classes.'));
            }
        }

        function applyFilters() {
            const year=el.yearFilter.value, level=el.levelFilter.value, search=el.search.value.trim().toLowerCase(), status=el.statusFilter.value;
            state.filtered=state.records.filter(record => (!year || String(record.academicYearId)===year) && (!level || String(record.levelId)===level)
                && (!search || String(record.classCode||'').toLowerCase().includes(search) || String(record.className||'').toLowerCase().includes(search))
                && (!status || String(record.status)===status));
            if (state.page>=pageCount()) state.page=Math.max(pageCount()-1,0);
            render();
        }

        function render() {
            el.body.replaceChildren();
            if (!state.filtered.length) { tableMessage(state.records.length ? 'No Classes match the selected filters.' : 'No Classes found.'); updatePager(); return; }
            const start=state.page*state.size;
            state.filtered.slice(start,start+state.size).forEach((record,index) => {
                const fragment=el.template.content.cloneNode(true), row=fragment.querySelector('tr');
                row.dataset.classId=String(record.classId); row.tabIndex=0; row.setAttribute('role','button'); row.setAttribute('aria-label',`View class ${record.classCode || record.className || ''}`);
                setText(row,'.col-serial',start+index+1); setText(row,'.col-code',record.classCode); setText(row,'.col-name strong',record.className);
                setText(row,'.col-level',record.levelName || lookupName(state.levels,record.levelId,['levelId','id'],['levelName','name']));
                setText(row,'.col-year',record.academicYearName || lookupName(state.years,record.academicYearId,['academicYearId','id'],['academicYearName','yearName','name']));
                const active=Number(record.status)===1, badge=row.querySelector('.col-status'); badge.classList.add(active?'active':'inactive'); badge.textContent=active?'Active':'Inactive';
                row.querySelector('.class-view-btn').dataset.id=String(record.classId); row.querySelector('.class-edit-btn').dataset.id=String(record.classId); el.body.appendChild(fragment);
            });
            updatePager();
        }

        async function viewRecord(id) {
            if (state.busy) return;
            const record = state.records.find(item => String(item.classId) === String(id));
            if (!record) { notifyError('Class details are not available. Refresh the list and try again.'); return; }
            state.viewedRecord = record;
            el.detailCode.textContent = record.classCode || '—';
            el.detailName.textContent = record.className || '—';
            el.detailLevel.textContent = record.levelName || lookupName(state.levels, record.levelId, ['levelId','id'], ['levelName','name']);
            el.detailYear.textContent = record.academicYearName || lookupName(state.years, record.academicYearId, ['academicYearId','id'], ['academicYearName','yearName','name']);
            el.detailStatus.textContent = Number(record.status) === 1 ? 'Active' : 'Inactive';
            showView('details');
        }

        async function editRecord(id) {
            if (state.busy) return;
            const record = state.records.find(item => String(item.classId) === String(id));
            if (!record) { notifyError('Class details are not available. Refresh the list and try again.'); return; }
            populateForm(record);
            showView('form');
        }

        function populateForm(record) {
            clearFormError(); state.editingId=record ? record.classId : null; el.id.value=record?.classId ?? '';
            el.year.value=record?.academicYearId == null ? '' : String(record.academicYearId); el.level.value=record?.levelId == null ? '' : String(record.levelId);
            el.code.value=record?.classCode || ''; el.name.value=record?.className || ''; el.status.value=String(record?.status ?? 1);
            el.formTitle.textContent=record ? 'Edit Class' : 'Add Class'; el.formSubtitle.textContent=record ? 'Update class details for your branch.' : 'Add a class for your branch.';
            el.save.replaceChildren(); const icon=document.createElement('i'); icon.className='bi bi-check-lg'; el.save.append(icon,document.createTextNode(record ? ' Save Changes' : ' Save Class'));
        }
        function resetForm() { el.form.reset(); el.status.value='1'; populateForm(null); }

        async function saveClass() {
            if (state.busy) return;
            clearFormError();
            const payload={ academicYearId: el.year.value ? Number(el.year.value) : null, levelId:el.level.value ? Number(el.level.value) : null,
                classCode:el.code.value.trim(), className:el.name.value.trim(), status:el.status.value==='' ? null : Number(el.status.value) };
            state.busy=true; el.save.disabled=true;
            let loaderShown = false;
            try {
                if (typeof showLoader === 'function') { showLoader(); loaderShown = true; }
                const response=state.editingId ? await apiPut(`/classes/${encodeURIComponent(state.editingId)}`,payload) : await apiPost('/classes',payload);
                notifySuccess(response?.message || (state.editingId ? 'Class updated successfully.' : 'Class created successfully.'));
                showView('table'); showTableLoading('Refreshing classes...'); await loadClasses();
            } catch (error) { const message=readError(error,'Unable to save Class.'); showFormError(message); notifyError(message); }
            finally { if (loaderShown && typeof hideLoader === 'function') hideLoader(); state.busy=false; el.save.disabled=false; }
        }

        function fillOptions(select,records,placeholder,idKeys,labelKeys) {
            const current=select.value; select.replaceChildren(new Option(placeholder,''));
            records.forEach(record=>{ const id=firstValue(record,idKeys), label=firstValue(record,labelKeys); if(id!=null&&label!=null) select.add(new Option(String(label),String(id))); });
            if(current && [...select.options].some(option=>option.value===current)) select.value=current;
        }
        function lookupName(records,id,idKeys,labelKeys) { const record=records.find(item=>String(firstValue(item,idKeys))===String(id)); return firstValue(record,labelKeys) || '—'; }
        function firstValue(record,keys) { for(const key of keys) if(record?.[key]!=null) return record[key]; return null; }
        function setText(root,selector,value) { const target=root.querySelector(selector); if(target) target.textContent=String(value??'—'); }
        function tableMessage(message) { const row=document.createElement('tr'),cell=document.createElement('td'); cell.colSpan=7; cell.className='text-center py-4 text-muted'; cell.textContent=message; row.appendChild(cell); el.body.replaceChildren(row); }
        function updatePager() { const pages=pageCount(); el.pageInfo.textContent=state.filtered.length ? `Showing page ${state.page+1} of ${pages} (${state.filtered.length} records)` : 'No records'; el.prev.disabled=!state.filtered.length||state.page<=0; el.next.disabled=!state.filtered.length||state.page+1>=pages; }
        function pageCount() { return Math.max(1,Math.ceil(state.filtered.length/Math.max(1,state.size))); }
        function showView(view) { el.tableView.classList.toggle('hidden',view!=='table'); el.formView.classList.toggle('hidden',view!=='form'); el.detailsView.classList.toggle('hidden',view!=='details'); window.scrollTo({top:0,behavior:'smooth'}); if(view==='form') el.code.focus(); }
        function showTableLoading(message) { if (typeof window.renderFetchingMessage === 'function') window.renderFetchingMessage(el.body, 7, message); else tableMessage(message); }
        function showFormError(message) { el.formErrorText.textContent=message; el.formError.classList.remove('hidden'); }
        function clearFormError() { el.formErrorText.textContent=''; el.formError.classList.add('hidden'); }
        function asArray(value) { return Array.isArray(value)?value:Array.isArray(value?.content)?value.content:[]; }
        function readError(error,fallback) { return error?.data?.message || error?.message || fallback; }
        function notifySuccess(message) { if(typeof showSuccessMessage==='function') showSuccessMessage(message); else console.info(message); }
        function notifyError(message) { if(typeof showErrorMessage==='function') showErrorMessage(message); else console.error(message); }
    }
})();

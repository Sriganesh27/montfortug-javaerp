/* global apiGet, apiPost, apiPut, showSuccessMessage, showErrorMessage */
(() => {
    'use strict';
    document.addEventListener('viewLoaded', event => {
        if (event.detail?.role === 'admin' && event.detail?.view === 'sections') {
            const task = init();
            if (typeof event.detail.waitUntil === 'function') event.detail.waitUntil(task);
        }
    });

    async function init() {
        const root = document.querySelector('#ba-sections-view');
        if (!root || root.dataset.initialized === 'true') return;
        root.dataset.initialized = 'true';
        const q = s => root.querySelector(s);
        const el = {
            tableView:q('#section-tableView'), formView:q('#section-form-view'), body:q('#section-table-body'),
            rowTemplate:q('#section-row-template'), add:q('#btn-add-section'), refresh:q('#section-refresh-btn'),
            back:q('#section-back-btn'), cancel:q('#section-cancel-btn'), filterForm:q('#section-filter-form'),
            reset:q('#section-reset-btn'), yearFilter:q('#section-year-filter'), classFilter:q('#section-class-filter'),
            search:q('#section-search-input'), statusFilter:q('#section-status-filter'), activeFilter:q('#section-active-filter'),
            pageSize:q('#section-page-size'), pageInfo:q('#section-page-info'), prev:q('#section-prev-btn'), next:q('#section-next-btn'),
            form:q('#section-form'), title:q('#section-form-title'), subtitle:q('#section-form-subtitle'),
            id:q('#section-id'), version:q('#section-version'), year:q('#section-year'), schoolClass:q('#section-class'),
            code:q('#section-code'), name:q('#section-name'), capacity:q('#section-capacity'), status:q('#section-status'),
            active:q('#section-active'), description:q('#section-description'), error:q('#section-form-error'),
            errorText:q('#section-form-error-text'), save:q('#section-save-btn'), overlay:q('#section-operation-overlay'),
            overlayTitle:q('#section-operation-title'), overlayMessage:q('#section-operation-message')
        };
        const state = { years:[], classes:[], records:[], filtered:[], page:0, size:10, editingId:null };

        if (typeof window.erpRegisterModuleSync === 'function') {
            window.erpRegisterModuleSync(
                'sections',
                async () => {
                    if (!document.querySelector('#ba-sections-view')) return false;
                    await loadReferences();
                    await loadSections();
                    return true;
                }
            );
        }

        bind(); resetForm(); await loadReferences(); await loadSections();

        function bind() {
            el.add.addEventListener('click',()=>{ resetForm(); showForm(); });
            el.refresh.addEventListener('click',()=>void loadSections());
            el.back.addEventListener('click',showTable); el.cancel.addEventListener('click',showTable);
            el.filterForm.addEventListener('submit',e=>{e.preventDefault(); state.page=0; applyFilters();});
            el.reset.addEventListener('click',()=>{el.yearFilter.value='';el.classFilter.value='';el.search.value='';el.statusFilter.value='';el.activeFilter.value='';state.page=0;applyFilters();});
            el.pageSize.addEventListener('change',()=>{state.size=Number(el.pageSize.value);state.page=0;render();});
            el.prev.addEventListener('click',()=>{if(state.page>0){state.page--;render();}});
            el.next.addEventListener('click',()=>{if(state.page+1<pages()){state.page++;render();}});
            el.form.addEventListener('submit',e=>{e.preventDefault();void save();});
        }

        async function loadReferences() {
            const [yearsRes, refRes] = await Promise.all([
                apiGet('/academic-years?active=true'),
                apiGet('/students/reference-data')
            ]);
            state.years = arrayOf(yearsRes?.data);
            const ref = refRes?.data || {};
            state.classes = arrayOf(ref.classes || ref.schoolClasses || ref.classOptions);
            fillYear(el.yearFilter, 'All Academic Years'); fillYear(el.year, 'Select Academic Year');
            fillClass(el.classFilter, 'All Classes'); fillClass(el.schoolClass, 'Select Class');
        }

        async function loadSections() {
            showTableLoading();
            try {
                const res = await apiGet('/sections');
                state.records = arrayOf(res?.data); state.page=0; applyFilters();
            } catch(e) { state.records=[]; applyFilters(); errorNotify(readError(e,'Failed to load Sections.')); }
        }

        function applyFilters() {
            const year=el.yearFilter.value, cls=el.classFilter.value, s=el.search.value.trim().toLowerCase();
            const status=el.statusFilter.value, active=el.activeFilter.value;
            state.filtered=state.records.filter(r =>
                (!year || String(r.academicYearId)===year)
                && (!cls || String(r.classId)===cls)
                && (!s || String(r.sectionCode||'').toLowerCase().includes(s) || String(r.sectionName||'').toLowerCase().includes(s))
                && (!status || r.status===status)
                && (!active || String(Boolean(r.active))===active)
            );
            if(state.page>=pages()) state.page=Math.max(pages()-1,0); render();
        }

        function render() {
            el.body.replaceChildren();
            if(!state.filtered.length){const tr=document.createElement('tr'),td=document.createElement('td');td.colSpan=9;td.className='text-center py-4 text-muted';td.textContent='No Sections found.';tr.appendChild(td);el.body.appendChild(tr);updatePager();return;}
            const start=state.page*state.size;
            state.filtered.slice(start,start+state.size).forEach((r,i)=>{
                const f=el.rowTemplate.content.cloneNode(true),row=f.querySelector('tr');
                text(row,'.col-serial',start+i+1);text(row,'.col-code',r.sectionCode);text(row,'.col-name strong',r.sectionName);
                text(row,'.col-year',r.academicYearName||r.academicYearCode);text(row,'.col-class',r.className||r.classCode);
                text(row,'.col-capacity',r.capacity);badge(row.querySelector('.col-status'),r.status,statusClass(r.status));
                badge(row.querySelector('.col-active'),r.active?'ACTIVE':'INACTIVE',r.active?'active':'inactive');
                row.querySelector('.section-edit-btn').addEventListener('click',()=>edit(r));
                row.querySelector('.section-active-btn').addEventListener('click',()=>void changeActive(r));
                el.body.appendChild(f);
            });updatePager();
        }

        function edit(r){
            state.editingId=Number(r.sectionId);el.id.value=r.sectionId;el.version.value=r.version??'';
            el.year.value=r.academicYearId;el.schoolClass.value=r.classId;el.code.value=r.sectionCode||'';
            el.name.value=r.sectionName||'';el.capacity.value=r.capacity||'';el.status.value=r.status||'ACTIVE';
            el.active.value=String(Boolean(r.active));el.description.value=r.description||'';
            el.title.textContent='Edit Section';el.subtitle.textContent='Update this Section for your branch.';clearError();showForm();
        }

        function resetForm(){
            state.editingId=null;el.form.reset();el.id.value='';el.version.value='';el.active.value='true';el.status.value='ACTIVE';
            el.title.textContent='Create Section';el.subtitle.textContent='Create a Section for your branch.';clearError();
        }

        async function save(){
            const message=validate();if(message){showError(message);return;}
            const payload={academicYearId:Number(el.year.value),classId:Number(el.schoolClass.value),sectionCode:el.code.value.trim(),
                sectionName:el.name.value.trim(),capacity:Number(el.capacity.value),description:nullable(el.description.value),
                status:el.status.value,active:el.active.value==='true',version:nullable(el.version.value)};
            showOverlay(state.editingId?'Updating Section':'Creating Section','Please wait while the Section is saved.');el.save.disabled=true;
            try{
                const res=state.editingId?await apiPut(`/sections/${state.editingId}`,payload):await apiPost('/sections',payload);
                cancelQueuedGlobalSync();
                successNotify(res?.message||'Section saved successfully.');showTable();await loadSections();
            }catch(e){const m=readError(e,'Unable to save Section.');showError(m);errorNotify(m);}
            finally{el.save.disabled=false;hideOverlay();}
        }

        async function changeActive(r){
            const next=!Boolean(r.active);if(!confirm(`${next?'Activate':'Deactivate'} "${r.sectionName}"?`))return;
            showOverlay(next?'Activating Section':'Deactivating Section','Please wait while the record state is updated.');
            try{
                const qs=new URLSearchParams({active:String(next),version:String(r.version??0)});
                const res=await patch(`/sections/${r.sectionId}/active-status?${qs}`);
                cancelQueuedGlobalSync();
                successNotify(res?.message||'Section state updated.');await loadSections();
            }catch(e){errorNotify(readError(e,'Unable to change Section state.'));}finally{hideOverlay();}
        }

        function validate(){
            if(!el.year.value)return'Academic Year is required.';if(!el.schoolClass.value)return'Class is required.';
            if(!el.code.value.trim())return'Section code is required.';if(!el.name.value.trim())return'Section name is required.';
            if(!Number(el.capacity.value)||Number(el.capacity.value)<1)return'Capacity must be greater than zero.';
            if(!el.status.value)return'Section status is required.';return'';
        }

        function fillYear(select,first){const selected=select.value;select.replaceChildren(new Option(first,''));state.years.forEach(r=>select.add(new Option(`${r.academicYearCode} - ${r.academicYearName}`,r.academicYearId)));if([...select.options].some(o=>o.value===selected))select.value=selected;}
        function fillClass(select,first){const selected=select.value;select.replaceChildren(new Option(first,''));state.classes.forEach(r=>{const id=r.classId??r.id;const code=r.classCode??r.code??'';const name=r.className??r.name??code;select.add(new Option(code?`${code} - ${name}`:name,id));});if([...select.options].some(o=>o.value===selected))select.value=selected;}
        function showForm(){el.tableView.classList.add('hidden');el.formView.classList.remove('hidden');window.scrollTo({top:0,behavior:'smooth'});}
        function showTable(){el.formView.classList.add('hidden');el.tableView.classList.remove('hidden');window.scrollTo({top:0,behavior:'smooth'});}
        function showTableLoading(){el.body.innerHTML='<tr><td colspan="9" class="text-center py-4">Loading Sections...</td></tr>';}
        function pages(){return Math.max(Math.ceil(state.filtered.length/state.size),1);}
        function updatePager(){const total=state.filtered.length;el.pageInfo.textContent=total?`Showing page ${state.page+1} of ${pages()} (${total} records)`:'No records';el.prev.disabled=!total||state.page<=0;el.next.disabled=!total||state.page+1>=pages();}
        function showOverlay(t,m){el.overlayTitle.textContent=t;el.overlayMessage.textContent=m;el.overlay.classList.add('is-visible');document.body.classList.add('modal-open');}
        function hideOverlay(){el.overlay.classList.remove('is-visible');document.body.classList.remove('modal-open');}
        function showError(m){el.errorText.textContent=m;el.error.classList.remove('hidden');}
        function clearError(){el.errorText.textContent='';el.error.classList.add('hidden');}
    }


    function cancelQueuedGlobalSync() {
        if (
            typeof window.erpCancelPendingDataSync
            === 'function'
        ) {
            window.erpCancelPendingDataSync();
        }
    }

    async function patch(path){const response=await fetch(`/api${path}`,{method:'PATCH',credentials:'include',cache:'no-store',headers:{'Content-Type':'application/json'}});const text=await response.text();let body;try{body=text?JSON.parse(text):null;}catch{body=text;}if(!response.ok){const e=new Error(body?.message||`HTTP Error: ${response.status}`);e.data=body;throw e;}document.dispatchEvent(new CustomEvent('erp:data-mutated',{detail:{method:'PATCH',endpoint:path,responseData:body,occurredAt:Date.now()}}));return body;}
    const arrayOf=v=>Array.isArray(v)?v:(Array.isArray(v?.content)?v.content:[]);
    const nullable=v=>String(v??'').trim()===''?null:v;
    const text=(p,s,v)=>{const n=p.querySelector(s);if(n)n.textContent=String(v??'-');};
    const badge=(n,t,c)=>{if(n){n.textContent=t||'-';n.className=`status-badge badge ${c}`;}};
    const statusClass=s=>s==='ACTIVE'?'active':'inactive';
    const readError=(e,f)=>e?.data?.message||e?.message||f;
    const successNotify=m=>typeof showSuccessMessage==='function'?showSuccessMessage(m):console.log(m);
    const errorNotify=m=>typeof showErrorMessage==='function'?showErrorMessage(m):console.error(m);
})();
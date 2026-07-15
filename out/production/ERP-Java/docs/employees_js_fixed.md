# Complete Fixed `employees.js`

I understand it can be tricky to manually swap out the payload blocks. I have taken your exact `employees.js` code and perfectly integrated the payload fixes for both adding and editing employees!

You just need to select everything in your `employees.js` file (Ctrl+A), delete it, and paste this entire code block in its place:

```javascript
/* global apiGet, apiPost, apiPut, apiDelete, showLoader, hideLoader, showPremiumModal, showSuccessMessage, showErrorMessage, CrudTable, loadView, createErpCalendar */

document.addEventListener('viewLoaded', function(e) {
    if (e.detail.role === 'admin') {
        if (e.detail.view === 'employees') {
            initEmployeesView();
        } else if (e.detail.view === 'add-employee') {
            initAddEmployeeView();
        }
    }
});

let currentDetailEmpId = null;

const EmpCollections = {
    contactFields: [
        { placeholder: 'Name', className: 'c-name', dataKey: 'contactName' },
        { placeholder: 'Relation', className: 'c-relation', dataKey: 'relationship' },
        { placeholder: 'Phone', className: 'c-phone', dataKey: 'phone' },
        { placeholder: 'Email', className: 'c-email', type: 'email', dataKey: 'email' }
    ],
    qualFields: [
        { placeholder: 'Level', className: 'q-level', dataKey: 'qualificationLevel' },
        { placeholder: 'Institution', className: 'q-inst', dataKey: 'institution' },
        { placeholder: 'Year', className: 'q-year', type: 'number', dataKey: 'passingYear' },
        { placeholder: 'Upload Cert', className: 'q-file', type: 'file', dataKey: 'fileData' }
    ],
    expFields: [
        { placeholder: 'Company', className: 'e-company', dataKey: 'companyName' },
        { placeholder: 'Role', className: 'e-role', dataKey: 'jobRole' },
        { placeholder: 'Start Date', className: 'e-start', type: 'date', dataKey: 'startDate' },
        { placeholder: 'End Date', className: 'e-end', type: 'date', dataKey: 'endDate' },
        { placeholder: 'Upload Doc', className: 'e-file', type: 'file', dataKey: 'fileData' }
    ],
    docFields: [
        { placeholder: 'Type', className: 'd-type', dataKey: 'documentType' },
        { placeholder: 'Number', className: 'd-num', dataKey: 'documentNumber' },
        { placeholder: 'Remarks', className: 'd-remarks', dataKey: 'remarks' },
        { placeholder: 'Upload Doc', className: 'd-file', type: 'file', dataKey: 'fileData' }
    ],
    createRow: function(containerElement, fieldsDef, rowClass, data = null, isEditMode = false) {
        if (!containerElement) return;
        const row = document.createElement('div');
        row.className = `emp-child-row emp-grid-${fieldsDef.length}-cols mb-3 ${rowClass}`;

        fieldsDef.forEach(field => {
            const wrapper = document.createElement('div');

            const span = document.createElement('span');
            span.className = `detail-text d-block ${isEditMode ? 'hidden' : ''}`;

            const input = document.createElement('input');
            input.type = field.type || 'text';
            input.className = `detail-input w-100 ${field.className} ${isEditMode ? '' : 'hidden'}`;
            input.placeholder = field.placeholder;

            if (data && data[field.dataKey]) {
                if(field.type === 'date' && data[field.dataKey]) {
                    input.value = data[field.dataKey].split('T')[0];
                    span.textContent = data[field.dataKey].split('T')[0];
                } else {
                    input.value = data[field.dataKey];
                    span.textContent = data[field.dataKey];
                }
            } else {
                span.textContent = '-';
            }

            wrapper.appendChild(span);
            wrapper.appendChild(input);
            row.appendChild(wrapper);
        });

        const removeBtn = document.createElement('button');
        removeBtn.type = 'button';
        removeBtn.className = `btn-secondary text-danger btn-sm detail-input ${isEditMode ? '' : 'hidden'}`;
        removeBtn.textContent = 'X';
        removeBtn.addEventListener('click', () => row.remove());
        row.appendChild(removeBtn);

        containerElement.appendChild(row);
        return row;
    },
    initSection: function(viewContainer, sectionId, containerId, btnText, fieldsDef, rowClass, isEditMode = false) {
        const section = viewContainer.querySelector(sectionId);
        const container = viewContainer.querySelector(containerId);
        if (!section || !container || section.querySelector('.add-row-btn')) return;

        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = `btn btn-outline-primary btn-sm mt-2 mb-3 add-row-btn ${isEditMode ? '' : 'hidden'}`;
        btn.innerHTML = btnText;
        btn.addEventListener('click', () => this.createRow(container, fieldsDef, rowClass, null, true));
        section.appendChild(btn);
    },
    gather: function(viewContainer, containerId, rowClass, extractFn) {
        const container = viewContainer.querySelector(containerId);
        if (!container) return [];
        const rows = container.querySelectorAll(`.${rowClass}`);
        const data = [];
        rows.forEach(row => {
            const item = extractFn(row);
            if (item && Object.values(item).some(v => v !== null && v !== '')) {
                data.push(item);
            }
        });
        return data;
    },
    gatherAsync: async function(viewContainer, containerId, rowClass, extractFnAsync) {
        const container = viewContainer.querySelector(containerId);
        if (!container) return [];
        const rows = Array.from(container.querySelectorAll(`.${rowClass}`));
        const data = [];
        for (const row of rows) {
            const item = await extractFnAsync(row);
            if (item && Object.values(item).some(v => v !== null && v !== '')) {
                data.push(item);
            }
        }
        return data;
    },
    fileToBase64: function(file) {
        return new Promise((resolve, reject) => {
            if(!file) return resolve(null);
            const reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = () => resolve(reader.result);
            reader.onerror = error => reject(error);
        });
    }
};

function initEmployeesView() {
    const viewContainer = document.querySelector('#ba-employees-view');
    if (!viewContainer) return;

    if (typeof createErpCalendar === 'function') {
        const today = new Date();
        const maxDobDate = new Date(today.getFullYear() - 18, today.getMonth(), today.getDate());

        createErpCalendar('#edit-empDob', {
            maxDate: maxDobDate
        });
        createErpCalendar('#edit-empJoiningDate');
        createErpCalendar('#edit-empProbationEndDate');
        createErpCalendar('#edit-empConfirmationDate');
        createErpCalendar('#edit-empPassportExpiry');
        createErpCalendar('#edit-empWorkPermitExpiry');
    }

    const addEmpBtn = viewContainer.querySelector('#btn-add-employee');
    if (addEmpBtn) {
        addEmpBtn.addEventListener('click', () => {
            const mainContent = document.getElementById('main-content-area');
            window.history.pushState({ view: 'add-employee', title: 'Add Employee' }, "", "/admin/add-employee");
            const pageTitleElement = document.getElementById('pageTitle');
            if (pageTitleElement) pageTitleElement.textContent = "Add Employee";
            void loadView('admin', 'add-employee', mainContent);
        });
    }

    const tableView = viewContainer.querySelector('#emp-tableView');
    const detailView = viewContainer.querySelector('#emp-detailView');

    EmpCollections.initSection(viewContainer, '#contacts-section', '#contacts-container', '+ Add Contact', EmpCollections.contactFields, 'contact-row');
    EmpCollections.initSection(viewContainer, '#qualifications-section', '#qualifications-container', '+ Add Qualification', EmpCollections.qualFields, 'qual-row');
    EmpCollections.initSection(viewContainer, '#experiences-section', '#experiences-container', '+ Add Experience', EmpCollections.expFields, 'exp-row');
    EmpCollections.initSection(viewContainer, '#documents-section', '#documents-container', '+ Add Document', EmpCollections.docFields, 'doc-row');

    const state = { page: 0, size: 10, sort: 'employeeId,desc' };
    let table;

    async function loadEmployees() {
        if (table) table.showLoading();
        try {
            const searchVal = viewContainer.querySelector('#emp-searchInput').value.trim();
            const categoryVal = viewContainer.querySelector('#emp-categoryFilter').value;
            const statusVal = viewContainer.querySelector('#emp-statusFilter').value;

            const payload = {
                keyword: searchVal || null,
                category: categoryVal || null,
                status: statusVal || null
            };

            const url = `/branchadmin/employees/search?page=${state.page}&size=${state.size}&sort=${state.sort}`;
            const res = await apiPost(url, payload);
            const pageData = res.data;

            table.render(pageData.content, (emp, rowNode) => {
                rowNode.querySelector('.col-code').textContent = emp.employeeNo;

                const nameNode = rowNode.querySelector('.col-name strong');
                nameNode.textContent = emp.fullName;
                rowNode.querySelector('.col-name').addEventListener('click', async () => await openEmpDetail(emp.employeeId));

                rowNode.querySelector('.col-category').textContent = formatEnum(emp.category);

                rowNode.querySelector('.email-val').textContent = emp.email || 'No email';
                rowNode.querySelector('.phone-val').textContent = emp.phone || 'No phone';

                const statusBadge = rowNode.querySelector('.status-badge');
                statusBadge.textContent = emp.status || 'ACTIVE';
                statusBadge.className = `status-badge badge-${(emp.status || 'ACTIVE').toLowerCase()}`;

                rowNode.querySelector('.view-more-btn').addEventListener('click', async () => await openEmpDetail(emp.employeeId));
                rowNode.querySelector('.delete-btn').addEventListener('click', async () => await deleteEmp(emp.employeeId));

                return rowNode;
            });

            table.renderPagination(pageData.pageNumber, pageData.totalPages, pageData.totalElements);
        } catch (e) {
            console.error(e);
            showErrorMessage("Failed to load employees.");
            table.render([]);
        }
    }

    table = new CrudTable(
        {
            tbody: document.getElementById('emp-tableBody'),
            pageSize: document.getElementById('emp-pageSize'),
            pageInfo: document.getElementById('emp-pageInfo'),
            btnPrev: document.getElementById('btn-emp-prev'),
            btnNext: document.getElementById('btn-emp-next'),
            tplLoading: document.getElementById('global-table-fetching-template'),
            tplEmpty: document.getElementById('global-table-empty-template'),
            tplRow: document.getElementById('tpl-emp-row'),
            table: document.getElementById('emp-tableComponent')
        },
        {
            onPageChange: async (dir) => { state.page += dir; await loadEmployees(); },
            onSizeChange: async (size) => { state.size = size; state.page = 0; await loadEmployees(); },
            onSort: async (field) => { state.sort = field; await loadEmployees(); }
        }
    );

    const searchBtn = viewContainer.querySelector('#emp-searchBtn');
    if (searchBtn) searchBtn.addEventListener('click', async () => { state.page = 0; await loadEmployees(); });

    async function loadSelectOptions() {
        try {
            const branchId = parseInt(localStorage.getItem('user_branch')) || 1;
            const [deptRes, designationRes] = await Promise.all([
                apiGet(`/departments?branchId=${branchId}&size=100`),
                apiGet(`/designations?branchId=${branchId}&size=100`)
            ]);

            const deptSelect = viewContainer.querySelector('#edit-empDepartment');
            const designationSelect = viewContainer.querySelector('#edit-empDesignation');

            deptSelect.innerHTML = '<option value="">-- None --</option>';
            designationSelect.innerHTML = '<option value="">-- None --</option>';

            deptRes.data.content.forEach(d => {
                deptSelect.innerHTML += `<option value="${d.departmentId}">${d.departmentName}</option>`;
            });
            designationRes.data.content.forEach(d => {
                designationSelect.innerHTML += `<option value="${d.id}">${d.designationName}</option>`;
            });
        } catch (e) {
            console.warn("Could not load departments/designations for dropdowns", e);
        }
    }

    async function openEmpDetail(id) {
        currentDetailEmpId = id;
        showLoader();
        try {
            await loadSelectOptions();
            const res = await apiGet(`/branchadmin/employees/${id}`);
            const emp = res.data;

            viewContainer.querySelector('#detail-empNameHeader').textContent = emp.fullName || `${emp.firstName} ${emp.lastName}`;

            const bindField = (fieldKey, value) => {
                const viewEl = document.getElementById(`view-${fieldKey}`);
                const editEl = document.getElementById(`edit-${fieldKey}`);
                if (viewEl) viewEl.textContent = value || '-';
                if (editEl) {
                    if (editEl['_flatpickr'] && value) {
                        editEl['_flatpickr'].setDate(value);
                    } else {
                        editEl.value = value || '';
                    }
                }
            };

            bindField('empCode', emp.employeeNo);
            bindField('empTitle', emp.title);

            document.getElementById('view-empName').textContent = emp.fullName || `${emp.firstName} ${emp.lastName}`;
            document.getElementById('edit-empFirstName').value = emp.firstName || '';
            document.getElementById('edit-empMiddleName').value = emp.middleName || '';
            document.getElementById('edit-empLastName').value = emp.lastName || '';

            bindField('empGender', emp.gender);
            bindField('empDob', emp.dateOfBirth);
            bindField('empMaritalStatus', emp.maritalStatus);
            bindField('empBloodGroup', emp.bloodGroup);

            bindField('empCategory', emp.category);
            bindField('empType', emp.employeeType);
            bindField('empMode', emp.employmentMode);
            bindField('empDepartment', emp.departmentId);
            bindField('empDesignation', emp.designationId);
            bindField('empJoiningDate', emp.joiningDate);
            bindField('empProbationEndDate', emp.probationEndDate);
            bindField('empConfirmationDate', emp.confirmationDate);

            bindField('empNationality', emp.nationality);
            bindField('empNationalId', emp.nationalId);
            bindField('empTin', emp.tinNumber);
            bindField('empPassportNo', emp.passportNo);
            bindField('empWorkPermit', emp.workPermitNumber);

            bindField('empEmail', emp.email);
            bindField('empPersonalEmail', emp.personalEmail);
            bindField('empPhone', emp.phone);
            bindField('empAlternatePhone', emp.alternatePhone);
            bindField('empCountry', emp.addressCountry);
            bindField('empState', emp.addressState);
            bindField('empDistrict', emp.addressDistrict);
            bindField('empVillage', emp.addressVillage);
            bindField('empStreet', emp.addressStreet);

            const deptSelect = document.getElementById('edit-empDepartment');
            if (deptSelect && deptSelect.options[deptSelect.selectedIndex]) {
                document.getElementById('view-empDepartment').textContent = deptSelect.options[deptSelect.selectedIndex].text;
            }
            const desigSelect = document.getElementById('edit-empDesignation');
            if (desigSelect && desigSelect.options[desigSelect.selectedIndex]) {
                document.getElementById('view-empDesignation').textContent = desigSelect.options[desigSelect.selectedIndex].text;
            }

            viewContainer.querySelector('#contacts-container').innerHTML = '';
            viewContainer.querySelector('#qualifications-container').innerHTML = '';
            viewContainer.querySelector('#experiences-container').innerHTML = '';
            viewContainer.querySelector('#documents-container').innerHTML = '';

            if (emp.contacts) {
                emp.contacts.forEach(c => EmpCollections.createRow(viewContainer.querySelector('#contacts-container'), EmpCollections.contactFields, 'contact-row', c));
            }
            if (emp.qualifications) {
                emp.qualifications.forEach(q => EmpCollections.createRow(viewContainer.querySelector('#qualifications-container'), EmpCollections.qualFields, 'qual-row', q));
            }
            if (emp.experiences) {
                emp.experiences.forEach(e => EmpCollections.createRow(viewContainer.querySelector('#experiences-container'), EmpCollections.expFields, 'exp-row', e));
            }
            if (emp.documents) {
                emp.documents.forEach(d => EmpCollections.createRow(viewContainer.querySelector('#documents-container'), EmpCollections.docFields, 'doc-row', d));
            }

            tableView.classList.add('hidden');
            detailView.classList.remove('hidden');
            resetEmpEditMode();
        } catch (e) {
            console.error(e);
            showErrorMessage("Failed to load employee details.");
        } finally {
            hideLoader();
        }
    }

    async function deleteEmp(id) {
        showPremiumModal({
            title: 'Terminate Employee',
            type: 'warning',
            contentText: 'Are you sure you want to terminate this employee? They will lose access to the system.',
            confirmText: 'Yes, Terminate',
            cancelText: 'Cancel',
            onConfirm: async (modal) => {
                modal.close();
                showLoader();
                try {
                    await apiDelete(`/branchadmin/employees/${id}`);
                    showSuccessMessage('Employee terminated successfully.');
                    await loadEmployees();
                } catch (e) {
                    showErrorMessage('Failed to terminate employee.');
                } finally {
                    hideLoader();
                }
            }
        });
    }

    const editBtn = viewContainer.querySelector('#emp-editBtn');
    const saveBtn = viewContainer.querySelector('#emp-saveBtn');
    const cancelEditBtn = viewContainer.querySelector('#emp-cancelEditBtn');
    const backBtn = viewContainer.querySelector('#emp-backToTableBtn');

    if (backBtn) {
        backBtn.addEventListener('click', async () => {
            detailView.classList.add('hidden');
            tableView.classList.remove('hidden');
            await loadEmployees();
        });
    }

    if (editBtn) {
        editBtn.addEventListener('click', () => {
            viewContainer.querySelectorAll('.detail-text').forEach(el => el.classList.add('hidden'));
            viewContainer.querySelectorAll('.detail-input:not([disabled])').forEach(el => el.classList.remove('hidden'));
            viewContainer.querySelector('#edit-empNameContainer').classList.remove('hidden');
            const codeEl = viewContainer.querySelector('#edit-empCode');
            if(codeEl) codeEl.classList.remove('hidden');

            editBtn.classList.add('hidden');
            saveBtn.classList.remove('hidden');
            cancelEditBtn.classList.remove('hidden');
        });
    }

    if (cancelEditBtn) {
        cancelEditBtn.addEventListener('click', () => resetEmpEditMode());
    }

    if (saveBtn) {
        saveBtn.addEventListener('click', async () => {
            if (!currentDetailEmpId) return;

            const valOrNull = (id) => {
                const el = viewContainer.querySelector(id);
                if (!el) return null;
                const v = el.value.trim();
                return v === "" ? null : v;
            };

            showLoader();
            try {
                // FIXED EDIT PAYLOAD
                const payload = {
                    title: valOrNull('#edit-empTitle'),
                    firstName: valOrNull('#edit-empFirstName'),
                    middleName: valOrNull('#edit-empMiddleName'),
                    lastName: valOrNull('#edit-empLastName'),
                    gender: valOrNull('#edit-empGender'),
                    dateOfBirth: valOrNull('#edit-empDob'),
                    maritalStatus: valOrNull('#edit-empMaritalStatus'),
                    bloodGroup: valOrNull('#edit-empBloodGroup'),

                    officialEmail: valOrNull('#edit-empEmail'),
                    personalEmail: valOrNull('#edit-empPersonalEmail'),
                    mobileNo: valOrNull('#edit-empPhone'),
                    alternateMobile: valOrNull('#edit-empAlternatePhone'),

                    departmentId: valOrNull('#edit-empDepartment'),
                    designationId: valOrNull('#edit-empDesignation'),
                    employeeCategory: valOrNull('#edit-empCategory'),
                    employeeType: valOrNull('#edit-empType'),
                    employmentMode: valOrNull('#edit-empMode'),
                    employmentStatus: 'ACTIVE',

                    joiningDate: valOrNull('#edit-empJoiningDate'),
                    probationEndDate: valOrNull('#edit-empProbationEndDate'),
                    confirmationDate: valOrNull('#edit-empConfirmationDate'),

                    nationality: valOrNull('#edit-empNationality'),
                    nationalId: valOrNull('#edit-empNationalId'),
                    tinNumber: valOrNull('#edit-empTin'),
                    passportNo: valOrNull('#edit-empPassportNo'),
                    passportExpiryDate: valOrNull('#edit-empPassportExpiry'),
                    workPermitNumber: valOrNull('#edit-empWorkPermit'),
                    workPermitExpiryDate: valOrNull('#edit-empWorkPermitExpiry'),

                    addressCountry: valOrNull('#edit-empCountry'),
                    addressState: valOrNull('#edit-empState'),
                    addressDistrict: valOrNull('#edit-empDistrict'),
                    addressVillage: valOrNull('#edit-empVillage'),
                    addressStreet: valOrNull('#edit-empStreet'),

                    contacts: EmpCollections.gather(viewContainer, '#contacts-container', 'contact-row', row => ({
                        contactName: row.querySelector('.c-name').value.trim() || null,
                        relationship: row.querySelector('.c-relation').value.trim() || null,
                        phone: row.querySelector('.c-phone').value.trim() || null,
                        email: row.querySelector('.c-email').value.trim() || null
                    })),
                    qualifications: await EmpCollections.gatherAsync(viewContainer, '#qualifications-container', 'qual-row', async row => ({
                        employeeQualificationLevel: row.querySelector('.q-level').value.trim() || null,
                        employeeQualificationName: row.querySelector('.q-level').value.trim() || 'N/A',
                        employeeQualificationInstitutionName: row.querySelector('.q-inst').value.trim() || null,
                        employeeQualificationCompletionYear: parseInt(row.querySelector('.q-year').value) || null,
                        employeeQualificationPercentage: row.querySelector('.q-score')?.value?.trim() || null,
                        fileData: await EmpCollections.fileToBase64(row.querySelector('.q-file')?.files[0]),
                        fileName: row.querySelector('.q-file')?.files[0]?.name || null
                    })),
                    experiences: await EmpCollections.gatherAsync(viewContainer, '#experiences-container', 'exp-row', async row => ({
                        companyName: row.querySelector('.e-company').value.trim() || null,
                        jobRole: row.querySelector('.e-role').value.trim() || null,
                        startDate: row.querySelector('.e-start').value || null,
                        endDate: row.querySelector('.e-end').value || null,
                        fileData: await EmpCollections.fileToBase64(row.querySelector('.e-file')?.files[0]),
                        fileName: row.querySelector('.e-file')?.files[0]?.name || null
                    })),
                    documents: await EmpCollections.gatherAsync(viewContainer, '#documents-container', 'doc-row', async row => ({
                        documentType: row.querySelector('.d-type').value.trim() || null,
                        documentNumber: row.querySelector('.d-num').value.trim() || null,
                        remarks: row.querySelector('.d-remarks').value.trim() || null,
                        fileData: await EmpCollections.fileToBase64(row.querySelector('.d-file')?.files[0]),
                        fileName: row.querySelector('.d-file')?.files[0]?.name || null
                    }))
                };

                await apiPut(`/branchadmin/employees/${currentDetailEmpId}`, payload);
                showSuccessMessage('Employee updated successfully.');
                resetEmpEditMode();
                await openEmpDetail(currentDetailEmpId);
                await loadEmployees();
            } catch (e) {
                console.error(e);
                showErrorMessage('Failed to update employee.');
            } finally {
                hideLoader();
            }
        });
    }

    function resetEmpEditMode() {
        viewContainer.querySelectorAll('.detail-text').forEach(el => el.classList.remove('hidden'));
        viewContainer.querySelectorAll('.detail-input').forEach(el => el.classList.add('hidden'));
        const nameContainer = viewContainer.querySelector('#edit-empNameContainer');
        if(nameContainer) nameContainer.classList.add('hidden');
        if (editBtn) editBtn.classList.remove('hidden');
        if (saveBtn) saveBtn.classList.add('hidden');
        if (cancelEditBtn) cancelEditBtn.classList.add('hidden');
    }

    function formatEnum(val) {
        if(!val) return '';
        return val.replace(/_/g, ' ').replace(/\w\S*/g, (txt) => txt.charAt(0).toUpperCase() + txt.substring(1).toLowerCase());
    }

    // Initial Load
    void loadEmployees();
}

function initAddEmployeeView() {
    const viewContainer = document.querySelector('#ba-add-employee-view');
    if (!viewContainer) return;

    async function loadAddSelectOptions() {
        try {
            const branchId = parseInt(localStorage.getItem('user_branch')) || 1;
            const [deptRes, designationRes] = await Promise.all([
                apiGet(`/departments?branchId=${branchId}&size=100`),
                apiGet(`/designations?branchId=${branchId}&size=100`)
            ]);

            const deptSelect = viewContainer.querySelector('#add-empDepartment');
            const designationSelect = viewContainer.querySelector('#add-empDesignation');

            if(deptSelect) {
                deptSelect.innerHTML = '<option value="">-- Select Department --</option>';
                deptRes.data.content.forEach(d => {
                    deptSelect.innerHTML += `<option value="${d.departmentId}">${d.departmentName}</option>`;
                });
            }
            if(designationSelect) {
                designationSelect.innerHTML = '<option value="">-- Select Designation --</option>';
                designationRes.data.content.forEach(d => {
                    designationSelect.innerHTML += `<option value="${d.id}">${d.designationName}</option>`;
                });
            }
        } catch (e) {
            console.warn("Could not load departments/designations", e);
        }
    }

    const oldForm = viewContainer.querySelector('#add-emp-form');
    let form = oldForm;
    if (oldForm) {
        form = oldForm.cloneNode(true);
        oldForm.parentNode.replaceChild(form, oldForm);
        form.reset();

        viewContainer.querySelectorAll('.detail-text').forEach(el => el.classList.add('hidden'));
        viewContainer.querySelectorAll('.detail-input').forEach(el => el.classList.remove('hidden'));

        viewContainer.querySelector('#contacts-container').innerHTML = '';
        viewContainer.querySelector('#qualifications-container').innerHTML = '';
        viewContainer.querySelector('#experiences-container').innerHTML = '';
        viewContainer.querySelector('#documents-container').innerHTML = '';

        EmpCollections.initSection(viewContainer, '#contacts-section', '#contacts-container', '+ Add Contact', EmpCollections.contactFields, 'contact-row', true);
        EmpCollections.initSection(viewContainer, '#qualifications-section', '#qualifications-container', '+ Add Qualification', EmpCollections.qualFields, 'qual-row', true);
        EmpCollections.initSection(viewContainer, '#experiences-section', '#experiences-container', '+ Add Experience', EmpCollections.expFields, 'exp-row', true);
        EmpCollections.initSection(viewContainer, '#documents-section', '#documents-container', '+ Add Document', EmpCollections.docFields, 'doc-row', true);

        // Initialize calendars AFTER cloning the form
        if (typeof createErpCalendar === 'function') {
            const today = new Date();
            const maxDobDate = new Date(today.getFullYear() - 18, today.getMonth(), today.getDate());

            createErpCalendar('#add-empDob', {
                maxDate: maxDobDate,
                defaultDate: maxDobDate
            });
            createErpCalendar('#add-empJoiningDate');
            createErpCalendar('#add-empProbationEndDate');
            createErpCalendar('#add-empConfirmationDate');
            createErpCalendar('#add-empPassportExpiry');
            createErpCalendar('#add-empWorkPermitExpiry');
        }

        // Default Joining Date
        const todayStr = new Date().toISOString().split('T')[0];
        const joinInput = viewContainer.querySelector('#add-empJoiningDate');
        if (joinInput && joinInput['_flatpickr']) {
            joinInput['_flatpickr'].setDate(new Date());
        } else if (joinInput) {
            joinInput.value = todayStr;
        }

        // Populate dropdowns!
        void loadAddSelectOptions();
    }

    const backBtn = viewContainer.querySelector('#backToEmployeesBtn');
    if (backBtn) {
        const newBackBtn = backBtn.cloneNode(true);
        backBtn.parentNode.replaceChild(newBackBtn, backBtn);
        newBackBtn.addEventListener('click', () => {
            const mainContent = document.getElementById('main-content-area');
            window.history.pushState({ view: 'employees', title: 'Manage Employees' }, "", "/admin/employees");
            const pageTitleElement = document.getElementById('pageTitle');
            if (pageTitleElement) pageTitleElement.textContent = "Manage Employees";
            void loadView('admin', 'employees', mainContent);
        });
    }

    if (form) {
        form.addEventListener('submit', async function(e) {
            e.preventDefault();

            const dobVal = viewContainer.querySelector('#add-empDob')?.value || viewContainer.querySelector('#edit-empDob')?.value;
            if (!dobVal) {
                showErrorMessage('Date of Birth is required.');
                return;
            }

            const birthDate = new Date(dobVal);
            const today = new Date();
            let age = today.getFullYear() - birthDate.getFullYear();
            const m = today.getMonth() - birthDate.getMonth();
            if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) {
                age--;
            }

            if (age < 18) {
                showErrorMessage('Employee must be at least 18 years old.');
                return;
            }

            const valOrNull = (id) => {
                const el = viewContainer.querySelector(id);
                if (!el) return null;
                const v = el.value.trim();
                return v === "" ? null : v;
            };

            const submitBtn = form.querySelector('button[type="submit"]');
            const originalText = submitBtn.innerHTML;
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<i class="bi bi-hourglass-split"></i> Saving...';

            showLoader();
            try {
                // FIXED ADD PAYLOAD
                const payload = {
                    title: valOrNull('#add-empTitle') || valOrNull('#edit-empTitle'),
                    firstName: valOrNull('#add-empFirstName') || valOrNull('#edit-empFirstName'),
                    middleName: valOrNull('#add-empMiddleName') || valOrNull('#edit-empMiddleName'),
                    lastName: valOrNull('#add-empLastName') || valOrNull('#edit-empLastName'),
                    gender: valOrNull('#add-empGender') || valOrNull('#edit-empGender'),
                    dateOfBirth: dobVal,
                    maritalStatus: valOrNull('#add-empMaritalStatus') || valOrNull('#edit-empMaritalStatus'),
                    bloodGroup: valOrNull('#add-empBloodGroup') || valOrNull('#edit-empBloodGroup'),

                    officialEmail: valOrNull('#add-empEmail') || valOrNull('#edit-empEmail'),
                    personalEmail: valOrNull('#add-empPersonalEmail') || valOrNull('#edit-empPersonalEmail'),
                    mobileNo: valOrNull('#add-empPhone') || valOrNull('#edit-empPhone'),
                    alternateMobile: valOrNull('#add-empAlternatePhone') || valOrNull('#edit-empAlternatePhone'),

                    departmentId: valOrNull('#add-empDepartment') || valOrNull('#edit-empDepartment'),
                    designationId: valOrNull('#add-empDesignation') || valOrNull('#edit-empDesignation'),
                    employeeCategory: valOrNull('#add-empCategory') || valOrNull('#edit-empCategory'),
                    employeeType: valOrNull('#add-empType') || valOrNull('#edit-empType'),
                    employmentMode: valOrNull('#add-empMode') || valOrNull('#edit-empMode'),
                    employmentStatus: 'ACTIVE',

                    joiningDate: valOrNull('#add-empJoiningDate') || valOrNull('#edit-empJoiningDate'),
                    probationEndDate: valOrNull('#add-empProbationEndDate') || valOrNull('#edit-empProbationEndDate'),
                    confirmationDate: valOrNull('#add-empConfirmationDate') || valOrNull('#edit-empConfirmationDate'),

                    nationality: valOrNull('#add-empNationality') || valOrNull('#edit-empNationality'),
                    nationalId: valOrNull('#add-empNationalId') || valOrNull('#edit-empNationalId'),
                    tinNumber: valOrNull('#add-empTin') || valOrNull('#edit-empTin'),
                    passportNo: valOrNull('#add-empPassportNo') || valOrNull('#edit-empPassportNo'),
                    passportExpiryDate: valOrNull('#add-empPassportExpiry') || valOrNull('#edit-empPassportExpiry'),
                    workPermitNumber: valOrNull('#add-empWorkPermit') || valOrNull('#edit-empWorkPermit'),
                    workPermitExpiryDate: valOrNull('#add-empWorkPermitExpiry') || valOrNull('#edit-empWorkPermitExpiry'),

                    addressCountry: valOrNull('#add-empCountry') || valOrNull('#edit-empCountry'),
                    addressState: valOrNull('#add-empState') || valOrNull('#edit-empState'),
                    addressDistrict: valOrNull('#add-empDistrict') || valOrNull('#edit-empDistrict'),
                    addressVillage: valOrNull('#add-empVillage') || valOrNull('#edit-empVillage'),
                    addressStreet: valOrNull('#add-empStreet') || valOrNull('#edit-empStreet'),

                    contacts: EmpCollections.gather(viewContainer, '#contacts-container', 'contact-row', row => ({
                        contactName: row.querySelector('.c-name').value.trim() || null,
                        relationship: row.querySelector('.c-relation').value.trim() || null,
                        phone: row.querySelector('.c-phone').value.trim() || null,
                        email: row.querySelector('.c-email').value.trim() || null
                    })),
                    qualifications: await EmpCollections.gatherAsync(viewContainer, '#qualifications-container', 'qual-row', async row => ({
                        employeeQualificationLevel: row.querySelector('.q-level').value.trim() || null,
                        employeeQualificationName: row.querySelector('.q-level').value.trim() || 'N/A',
                        employeeQualificationInstitutionName: row.querySelector('.q-inst').value.trim() || null,
                        employeeQualificationCompletionYear: parseInt(row.querySelector('.q-year').value) || null,
                        employeeQualificationPercentage: row.querySelector('.q-score')?.value?.trim() || null,
                        fileData: await EmpCollections.fileToBase64(row.querySelector('.q-file')?.files[0]),
                        fileName: row.querySelector('.q-file')?.files[0]?.name || null
                    })),
                    experiences: await EmpCollections.gatherAsync(viewContainer, '#experiences-container', 'exp-row', async row => ({
                        companyName: row.querySelector('.e-company').value.trim() || null,
                        jobRole: row.querySelector('.e-role').value.trim() || null,
                        startDate: row.querySelector('.e-start').value || null,
                        endDate: row.querySelector('.e-end').value || null,
                        fileData: await EmpCollections.fileToBase64(row.querySelector('.e-file')?.files[0]),
                        fileName: row.querySelector('.e-file')?.files[0]?.name || null
                    })),
                    documents: await EmpCollections.gatherAsync(viewContainer, '#documents-container', 'doc-row', async row => ({
                        documentType: row.querySelector('.d-type').value.trim() || null,
                        documentNumber: row.querySelector('.d-num').value.trim() || null,
                        remarks: row.querySelector('.d-remarks').value.trim() || null,
                        fileData: await EmpCollections.fileToBase64(row.querySelector('.d-file')?.files[0]),
                        fileName: row.querySelector('.d-file')?.files[0]?.name || null
                    }))
                };

                const res = await apiPost('/branchadmin/employees', payload);
                showPremiumModal({
                    title: 'Employee Registered',
                    type: 'success',
                    contentText: `Employee ${res.data.firstName} ${res.data.lastName} registered successfully. Generated Code: ${res.data.employeeNo}`,
                    confirmText: 'Done'
                });
                form.reset();
                const joinInputReset = viewContainer.querySelector('#add-empJoiningDate');
                if (joinInputReset && joinInputReset['_flatpickr']) {
                    joinInputReset['_flatpickr'].setDate(new Date());
                } else if (joinInputReset) {
                    joinInputReset.value = new Date().toISOString().split('T')[0];
                }
            } catch (err) {
                console.error(err);
                showErrorMessage(err.message || 'Failed to register employee.');
            } finally {
                hideLoader();
                submitBtn.disabled = false;
                submitBtn.innerHTML = originalText;
            }
        });
    }
}
```

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
function getRequiredBranchId() {
    const branchId = Number.parseInt(
        localStorage.getItem('user_branch'),
        10
    );

    if (!Number.isInteger(branchId) || branchId <= 0) {
        throw new Error(
            'No valid branch is assigned to the current user.'
        );
    }

    return branchId;
}
let currentDetailEmpId = null;

const EmpCollections = {
    contactFields: [
        {
            placeholder: 'Name',
            className: 'c-name',
            dataKey: 'employeeContactName'
        },
        {
            placeholder: 'Relationship',
            className: 'c-relation',
            dataKey: 'employeeContactRelationship',
            type: 'select',
            options: [
                { value: '', text: '-- Select Relationship --' },
                { value: 'FATHER', text: 'Father' },
                { value: 'MOTHER', text: 'Mother' },
                { value: 'BROTHER', text: 'Brother' },
                { value: 'SISTER', text: 'Sister' },
                { value: 'SPOUSE', text: 'Spouse' },
                { value: 'SON', text: 'Son' },
                { value: 'DAUGHTER', text: 'Daughter' },
                { value: 'GUARDIAN', text: 'Guardian' },
                { value: 'RELATIVE', text: 'Relative' },
                { value: 'FRIEND', text: 'Friend' },
                { value: 'MANAGER', text: 'Manager' },
                { value: 'REFERENCE', text: 'Reference' },
                { value: 'OTHER', text: 'Other' }
            ]
        },
        {
            placeholder: 'Phone',
            className: 'c-phone',
            dataKey: 'employeeContactMobile'
        },
        {
            placeholder: 'Email',
            className: 'c-email',
            type: 'email',
            dataKey: 'employeeContactEmail'
        }
    ],
        qualFields: [
            {
                placeholder: 'Qualification Level',
                className: 'q-level',
                dataKey: 'employeeQualificationLevel',
                type: 'select',
                options: [
                    { value: '', text: '-- Select Level --' },
                    { value: 'PRIMARY', text: 'Primary' },
                    { value: 'SECONDARY', text: 'Secondary' },
                    { value: 'SENIOR_SECONDARY', text: 'Senior Secondary' },
                    { value: 'CERTIFICATE', text: 'Certificate' },
                    { value: 'DIPLOMA', text: 'Diploma' },
                    { value: 'GRADUATION', text: 'Graduation' },
                    { value: 'POST_GRADUATION', text: 'Post Graduation' },
                    { value: 'DR_PHD', text: 'Doctorate / PhD' },
                    { value: 'OTHER', text: 'Other' }
                ]
            },
            {
                placeholder: 'Enter Other Qualification',
                className: 'q-custom-level',
                dataKey: 'employeeQualificationName',
                conditional: 'other-level'
            },
            {
                placeholder: 'Institution / School / College',
                className: 'q-institution',
                dataKey: 'employeeQualificationInstitutionName'
            },
            {
                placeholder: 'Specialization / Subject',
                className: 'q-specialization',
                dataKey: 'employeeQualificationSpecialization',
                conditional: 'specialization'
            },
            {
                placeholder: 'Division / Grade',
                className: 'q-grade',
                dataKey: 'employeeQualificationGrade'
            },
            {
                placeholder: 'Completion Year',
                className: 'q-year',
                type: 'number',
                dataKey: 'employeeQualificationCompletionYear'
            },
            {
                placeholder: 'Upload Certificate',
                className: 'q-file',
                type: 'file',
                dataKey: 'fileData'
            }
        ],
    expFields: [
        { placeholder: 'Organisation', className: 'e-company', dataKey: 'companyName' },
        {
            placeholder: 'Type',
            className: 'e-type',
            dataKey: 'employeeExperienceType',
            type: 'select',
            options: [
                { value: 'FULL_TIME', text: 'Full Time' },
                { value: 'PART_TIME', text: 'Part Time' },
                { value: 'CONTRACT', text: 'Contract' },
                { value: 'TEMPORARY', text: 'Temporary' },
                { value: 'INTERNSHIP', text: 'Internship' },
                { value: 'CONSULTANT', text: 'Consultant' },
                { value: 'VOLUNTEER', text: 'Volunteer' },
                { value: 'SELF_EMPLOYED', text: 'Self Employed' },
                { value: 'OTHER', text: 'Other' }
            ]
        },
        { placeholder: 'Post Held', className: 'e-role', dataKey: 'jobRole' },
        { placeholder: 'Start Date', className: 'e-start', type: 'date', dataKey: 'startDate' },
        { placeholder: 'End Date', className: 'e-end', type: 'date', dataKey: 'endDate' },
        { placeholder: 'Upload Doc', className: 'e-file', type: 'file', dataKey: 'fileData' }
    ],
    docFields: [
        {
            placeholder: 'Document Type',
            className: 'd-type',
            dataKey: 'documentType',
            type: 'select',
            options: [
                { value: '', text: '-- Select Document Type --' },
                { value: 'NATIONAL_ID', text: 'National ID' },
                { value: 'PASSPORT', text: 'Passport' },
                { value: 'WORK_PERMIT', text: 'Work Permit' },
                { value: 'VISA', text: 'Visa' },
                { value: 'BIRTH_CERTIFICATE', text: 'Birth Certificate' },
                { value: 'MARRIAGE_CERTIFICATE', text: 'Marriage Certificate' },
                { value: 'ACADEMIC_CERTIFICATE', text: 'Academic Certificate' },
                { value: 'EXPERIENCE_CERTIFICATE', text: 'Experience Certificate' },
                { value: 'TEACHING_LICENSE', text: 'Teaching License' },
                { value: 'PROFESSIONAL_LICENSE', text: 'Professional License' },
                { value: 'MEDICAL_CERTIFICATE', text: 'Medical Certificate' },
                { value: 'POLICE_CLEARANCE', text: 'Police Clearance' },
                { value: 'EMPLOYMENT_CONTRACT', text: 'Employment Contract' },
                { value: 'APPOINTMENT_LETTER', text: 'Appointment Letter' },
                { value: 'RELIEVING_LETTER', text: 'Relieving Letter' },
                { value: 'SALARY_CERTIFICATE', text: 'Salary Certificate' },
                { value: 'TIN_CERTIFICATE', text: 'TIN Certificate' },
                { value: 'NSSF_DOCUMENT', text: 'NSSF Document' },
                { value: 'BANK_DOCUMENT', text: 'Bank Document' },
                { value: 'CURRICULUM_VITAE', text: 'Curriculum Vitae' },
                { value: 'RESUME', text: 'Resume' },
                { value: 'PASSPORT_PHOTO', text: 'Passport Photo' },
                { value: 'SIGNATURE', text: 'Signature' },
                { value: 'CODE_OF_CONDUCT_AGREEMENT', text: 'Code of Conduct Agreement' },
                { value: 'CONFIDENTIALITY_AGREEMENT', text: 'Confidentiality Agreement' },
                { value: 'OTHER', text: 'Other' }
            ]
        },
        {
            placeholder: 'Number',
            className: 'd-num',
            dataKey: 'documentNumber'
        },
        {
            placeholder: 'Remarks',
            className: 'd-remarks',
            dataKey: 'remarks'
        },
        {
            placeholder: 'Upload Doc',
            className: 'd-file',
            type: 'file',
            dataKey: 'fileData'
        }
    ],
    createRow: function(
        containerElement,
        fieldsDef,
        rowClass,
        data = null,
        isEditMode = false
    ) {
        if (!containerElement || !Array.isArray(fieldsDef)) {
            return null;
        }

        const row = document.createElement('div');

        row.className =
            `emp-child-row emp-grid-${fieldsDef.length}-cols mb-3 ${rowClass}`;

        const conditionalFields = {};

        fieldsDef.forEach(field => {
            const wrapper = document.createElement('div');
            wrapper.className = 'emp-child-field';

            const span = document.createElement('span');

            span.className =
                `detail-text d-block ${isEditMode ? 'hidden' : ''}`;

            let input;

            if (field.type === 'select') {
                input = document.createElement('select');

                field.options.forEach(optionDefinition => {
                    const option =
                        document.createElement('option');

                    option.value =
                        optionDefinition.value;

                    option.textContent =
                        optionDefinition.text;

                    input.appendChild(option);
                });
            } else {
                input = document.createElement('input');

                input.type =
                    field.type || 'text';

                input.placeholder =
                    field.placeholder;
            }

            input.className =
                `detail-input w-100 ${field.className} ${isEditMode ? '' : 'hidden'}`;

            if (data && data[field.dataKey] !== undefined &&
                data[field.dataKey] !== null) {

                const value = data[field.dataKey];

                if (field.type === 'date' && value) {
                    input.value =
                        String(value).split('T')[0];

                    span.textContent =
                        String(value).split('T')[0];
                } else {
                    input.value = value;
                    span.textContent = value;
                }
            } else {
                span.textContent = '-';
            }

            if (field.conditional) {
                wrapper.dataset.conditional =
                    field.conditional;

                conditionalFields[field.conditional] =
                    wrapper;
            }

            wrapper.appendChild(span);
            wrapper.appendChild(input);
            row.appendChild(wrapper);
        });

        const qualificationLevel =
            row.querySelector('.q-level');

        const updateQualificationVisibility = () => {
            if (!qualificationLevel) return;

            const selectedLevel =
                qualificationLevel.value;

            const otherWrapper =
                conditionalFields['other-level'];

            const specializationWrapper =
                conditionalFields.specialization;

            const showOther =
                selectedLevel === 'OTHER';

            const showSpecialization =
                selectedLevel !== '' &&
                selectedLevel !== 'PRIMARY' &&
                selectedLevel !== 'SECONDARY';

            if (otherWrapper) {
                otherWrapper.classList.toggle(
                    'hidden',
                    !showOther
                );

                const otherInput =
                    otherWrapper.querySelector(
                        '.q-custom-level'
                    );

                if (!showOther && otherInput) {
                    otherInput.value = '';
                }
            }

            if (specializationWrapper) {
                specializationWrapper.classList.toggle(
                    'hidden',
                    !showSpecialization
                );

                const specializationInput =
                    specializationWrapper.querySelector(
                        '.q-specialization'
                    );

                if (
                    !showSpecialization &&
                    specializationInput
                ) {
                    specializationInput.value = '';
                }
            }
        };

        if (qualificationLevel) {
            qualificationLevel.addEventListener(
                'change',
                updateQualificationVisibility
            );

            updateQualificationVisibility();
        }

        const removeButton =
            document.createElement('button');

        removeButton.type = 'button';

        removeButton.className =
            `btn-secondary text-danger btn-sm detail-input ${isEditMode ? '' : 'hidden'}`;

        removeButton.textContent = 'X';

        removeButton.addEventListener(
            'click',
            () => row.remove()
        );

        row.appendChild(removeButton);

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
    gather: function(
        viewContainer,
        containerId,
        rowClass,
        extractFn
    ) {
        const container =
            viewContainer.querySelector(containerId);

        if (!container) return [];

        const rows =
            container.querySelectorAll(`.${rowClass}`);

        const data = [];

        rows.forEach((row, index) => {
            const item = extractFn(row, index);

            if (
                item &&
                Object.values(item).some(
                    value =>
                        value !== null &&
                        value !== ''
                )
            ) {
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
            const branchId = getRequiredBranchId();
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
            const setEditVal = (id, val) => {
                const el = document.getElementById(id);
                if (el) el.value = val || '';
            };
            setEditVal('edit-empFirstName', emp.firstName);
            setEditVal('edit-empMiddleName', emp.middleName);
            setEditVal('edit-empLastName', emp.lastName);

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
                    religion: valOrNull('#edit-empReligion'),
                    subReligion: valOrNull('#edit-empSubReligion'),

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
                    addressCounty: valOrNull('#edit-empCounty'),
                    addressSubCounty: valOrNull('#edit-empSubCounty'),
                    addressParish: valOrNull('#edit-empParish'),
                    addressVillage: valOrNull('#edit-empVillage'),
                    addressStreet: valOrNull('#edit-empStreet'),
                    skills: valOrNull('#edit-empSkills'),
                    languagesSpoken: valOrNull('#edit-empLanguages'),

                    contacts: (() => {
                        const rows = Array.from(
                            viewContainer.querySelectorAll(
                                '#contacts-container .contact-row'
                            )
                        );

                        return rows
                            .map((row, index) => ({
                                employeeContactName:
                                    row.querySelector('.c-name')
                                        ?.value
                                        ?.trim() || null,

                                employeeContactRelationship:
                                    row.querySelector('.c-relation')
                                        ?.value
                                        ?.trim() || null,

                                employeeContactMobile:
                                    row.querySelector('.c-phone')
                                        ?.value
                                        ?.trim() || null,

                                employeeContactEmail:
                                    row.querySelector('.c-email')
                                        ?.value
                                        ?.trim() || null,

                                employeeContactType:
                                    'EMERGENCY',

                                employeeContactIsPrimary:
                                    index === 0,

                                employeeContactIsEmergency:
                                    true
                            }))
                            .filter(contact =>
                                contact.employeeContactName ||
                                contact.employeeContactMobile
                            );
                    })(),
                    qualifications:
                        await EmpCollections.gatherAsync(
                            viewContainer,
                            '#qualifications-container',
                            'qual-row',
                            async row => {
                                const level =
                                    row.querySelector('.q-level')
                                        ?.value
                                        ?.trim() || null;

                                const customLevel =
                                    row.querySelector(
                                        '.q-custom-level'
                                    )
                                        ?.value
                                        ?.trim() || null;

                                const qualificationName =
                                    level === 'OTHER'
                                        ? customLevel
                                        : level;

                                return {
                                    employeeQualificationLevel:
                                    level,

                                    employeeQualificationName:
                                    qualificationName,

                                    employeeQualificationInstitutionName:
                                        row.querySelector(
                                            '.q-institution'
                                        )
                                            ?.value
                                            ?.trim() || null,

                                    employeeQualificationSpecialization:
                                        row.querySelector(
                                            '.q-specialization'
                                        )
                                            ?.value
                                            ?.trim() || null,

                                    employeeQualificationGrade:
                                        row.querySelector(
                                            '.q-grade'
                                        )
                                            ?.value
                                            ?.trim() || null,

                                    employeeQualificationCompletionYear:
                                        Number.parseInt(
                                            row.querySelector(
                                                '.q-year'
                                            )?.value,
                                            10
                                        ) || null,

                                    fileData:
                                        await EmpCollections.fileToBase64(
                                            row.querySelector(
                                                '.q-file'
                                            )?.files[0]
                                        ),

                                    fileName:
                                        row.querySelector(
                                            '.q-file'
                                        )?.files[0]?.name || null
                                };
                            }
                        ),
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
    const viewContainer =
        document.querySelector('#ba-add-employee-view');

    if (!viewContainer) return;

    async function loadAddSelectOptions() {
        try {
            const branchId = getRequiredBranchId();

            const [deptRes, designationRes] =
                await Promise.all([
                    apiGet(
                        `/departments?branchId=${branchId}&size=100`
                    ),
                    apiGet(
                        `/designations?branchId=${branchId}&size=100`
                    )
                ]);

            const deptSelect =
                viewContainer.querySelector(
                    '#add-empDepartment'
                );

            const designationSelect =
                viewContainer.querySelector(
                    '#add-empDesignation'
                );

            if (deptSelect) {
                deptSelect.innerHTML =
                    '<option value="">-- Select Department --</option>';

                deptRes.data.content.forEach(department => {
                    deptSelect.innerHTML += `
                        <option value="${department.departmentId}">
                            ${department.departmentName}
                        </option>
                    `;
                });
            }

            if (designationSelect) {
                designationSelect.innerHTML =
                    '<option value="">-- Select Designation --</option>';

                designationRes.data.content.forEach(designation => {
                    designationSelect.innerHTML += `
                        <option value="${designation.id}">
                            ${designation.designationName}
                        </option>
                    `;
                });
            }
        } catch (error) {
            console.warn(
                'Could not load departments/designations',
                error
            );
        }
    }

    const oldForm =
        viewContainer.querySelector('#add-emp-form');

    let form = oldForm;

    if (oldForm) {
        form = oldForm.cloneNode(true);

        oldForm.parentNode.replaceChild(
            form,
            oldForm
        );

        form.reset();

        const generateLoginCheckbox =
            viewContainer.querySelector(
                '#add-generateLogin'
            );

        const loginOptions =
            viewContainer.querySelector(
                '#add-loginOptions'
            );

        const sendLoginEmailCheckbox =
            viewContainer.querySelector(
                '#add-sendLoginEmail'
            );

        const officialEmailInput =
            viewContainer.querySelector(
                '#add-empEmail'
            );

        if (generateLoginCheckbox && loginOptions) {
            generateLoginCheckbox.addEventListener(
                'change',
                () => {
                    const enabled =
                        generateLoginCheckbox.checked;

                    loginOptions.classList.toggle(
                        'hidden',
                        !enabled
                    );

                    if (
                        !enabled &&
                        sendLoginEmailCheckbox
                    ) {
                        sendLoginEmailCheckbox.checked =
                            false;
                    }
                }
            );
        }

        if (
            sendLoginEmailCheckbox &&
            officialEmailInput
        ) {
            sendLoginEmailCheckbox.addEventListener(
                'change',
                () => {
                    if (
                        sendLoginEmailCheckbox.checked &&
                        !officialEmailInput.value.trim()
                    ) {
                        sendLoginEmailCheckbox.checked =
                            false;

                        showErrorMessage(
                            'Enter the official email before enabling credential email.'
                        );

                        officialEmailInput.focus();
                    }
                }
            );
        }

        viewContainer
            .querySelectorAll('.detail-text')
            .forEach(element => {
                element.classList.add('hidden');
            });

        viewContainer
            .querySelectorAll('.detail-input')
            .forEach(element => {
                element.classList.remove('hidden');
            });

        const contactsContainer =
            viewContainer.querySelector(
                '#contacts-container'
            );

        const qualificationsContainer =
            viewContainer.querySelector(
                '#qualifications-container'
            );

        const experiencesContainer =
            viewContainer.querySelector(
                '#experiences-container'
            );

        const documentsContainer =
            viewContainer.querySelector(
                '#documents-container'
            );

        if (contactsContainer) {
            contactsContainer.innerHTML = '';
        }

        if (qualificationsContainer) {
            qualificationsContainer.innerHTML = '';
        }

        if (experiencesContainer) {
            experiencesContainer.innerHTML = '';
        }

        if (documentsContainer) {
            documentsContainer.innerHTML = '';
        }

        EmpCollections.initSection(
            viewContainer,
            '#contacts-section',
            '#contacts-container',
            '+ Add Contact',
            EmpCollections.contactFields,
            'contact-row',
            true
        );

        EmpCollections.initSection(
            viewContainer,
            '#qualifications-section',
            '#qualifications-container',
            '+ Add Qualification',
            EmpCollections.qualFields,
            'qual-row',
            true
        );

        EmpCollections.initSection(
            viewContainer,
            '#experiences-section',
            '#experiences-container',
            '+ Add Experience',
            EmpCollections.expFields,
            'exp-row',
            true
        );

        EmpCollections.initSection(
            viewContainer,
            '#documents-section',
            '#documents-container',
            '+ Add Document',
            EmpCollections.docFields,
            'doc-row',
            true
        );

        if (
            typeof createErpCalendar === 'function'
        ) {
            const today = new Date();

            const maxDobDate = new Date(
                today.getFullYear() - 18,
                today.getMonth(),
                today.getDate()
            );

            createErpCalendar('#add-empDob', {
                maxDate: maxDobDate,
                defaultDate: maxDobDate
            });

            createErpCalendar(
                '#add-empJoiningDate'
            );

            createErpCalendar(
                '#add-empProbationEndDate'
            );

            createErpCalendar(
                '#add-empConfirmationDate'
            );

            createErpCalendar(
                '#add-empPassportExpiry'
            );

            createErpCalendar(
                '#add-empWorkPermitExpiry'
            );
        }

        const todayString =
            new Date()
                .toISOString()
                .split('T')[0];

        const joiningDateInput =
            viewContainer.querySelector(
                '#add-empJoiningDate'
            );

        if (
            joiningDateInput &&
            joiningDateInput['_flatpickr']
        ) {
            joiningDateInput['_flatpickr']
                .setDate(new Date());
        } else if (joiningDateInput) {
            joiningDateInput.value =
                todayString;
        }

        void loadAddSelectOptions();
    }

    const backButton =
        viewContainer.querySelector(
            '#backToEmployeesBtn'
        );

    if (backButton) {
        const newBackButton =
            backButton.cloneNode(true);

        backButton.parentNode.replaceChild(
            newBackButton,
            backButton
        );

        newBackButton.addEventListener(
            'click',
            () => {
                const mainContent =
                    document.getElementById(
                        'main-content-area'
                    );

                window.history.pushState(
                    {
                        view: 'employees',
                        title: 'Manage Employees'
                    },
                    '',
                    '/admin/employees'
                );

                const pageTitleElement =
                    document.getElementById(
                        'pageTitle'
                    );

                if (pageTitleElement) {
                    pageTitleElement.textContent =
                        'Manage Employees';
                }

                void loadView(
                    'admin',
                    'employees',
                    mainContent
                );
            }
        );
    }

    const importButton =
        viewContainer.querySelector(
            '#btn-import-employee'
        );

    if (importButton) {
        const newImportButton =
            importButton.cloneNode(true);

        importButton.parentNode.replaceChild(
            newImportButton,
            importButton
        );

        newImportButton.addEventListener(
            'click',
            () => {
                if (
                    typeof AppImporter !==
                    'undefined'
                ) {
                    AppImporter.open(
                        'employee',
                        'Import Employees',
                        'Upload the Excel file containing Employee records.',
                        () => {
                            const mainContent =
                                document.getElementById(
                                    'main-content-area'
                                );

                            window.history.pushState(
                                {
                                    view: 'employees',
                                    title: 'Manage Employees'
                                },
                                '',
                                '/admin/employees'
                            );

                            const pageTitleElement =
                                document.getElementById(
                                    'pageTitle'
                                );

                            if (pageTitleElement) {
                                pageTitleElement.textContent =
                                    'Manage Employees';
                            }

                            void loadView(
                                'admin',
                                'employees',
                                mainContent
                            );
                        }
                    );
                } else {
                    if (
                        typeof AppToast !==
                        'undefined'
                    ) {
                        AppToast.error(
                            'Importer module not found.'
                        );
                    }

                    console.warn(
                        'AppImporter is not defined.'
                    );
                }
            }
        );
    }

    if (!form) return;

    form.addEventListener(
        'submit',
        async function(event) {
            event.preventDefault();

            const dobValue =
                viewContainer.querySelector(
                    '#add-empDob'
                )?.value || null;

            if (!dobValue) {
                showErrorMessage(
                    'Date of Birth is required.'
                );
                return;
            }

            const birthDate =
                new Date(dobValue);

            const today =
                new Date();

            let age =
                today.getFullYear() -
                birthDate.getFullYear();

            const monthDifference =
                today.getMonth() -
                birthDate.getMonth();

            if (
                monthDifference < 0 ||
                (
                    monthDifference === 0 &&
                    today.getDate() <
                    birthDate.getDate()
                )
            ) {
                age--;
            }

            if (age < 18) {
                showErrorMessage(
                    'Employee must be at least 18 years old.'
                );
                return;
            }

            const valOrNull = selector => {
                const element =
                    viewContainer.querySelector(
                        selector
                    );

                if (!element) return null;

                const value =
                    element.value.trim();

                return value === ''
                    ? null
                    : value;
            };

            const generateLogin =
                viewContainer.querySelector(
                    '#add-generateLogin'
                )?.checked ?? false;

            const sendLoginEmail =
                viewContainer.querySelector(
                    '#add-sendLoginEmail'
                )?.checked ?? false;

            const officialEmail =
                valOrNull('#add-empEmail');

            if (
                generateLogin &&
                sendLoginEmail &&
                !officialEmail
            ) {
                showErrorMessage(
                    'Official email is required to send login credentials.'
                );

                viewContainer
                    .querySelector(
                        '#add-empEmail'
                    )
                    ?.focus();

                return;
            }

            const submitButton =
                form.querySelector(
                    'button[type="submit"]'
                );

            if (!submitButton) return;

            const originalButtonText =
                submitButton.innerHTML;

            submitButton.disabled = true;

            submitButton.innerHTML =
                '<i class="bi bi-hourglass-split"></i> Saving...';

            showLoader();

            try {
                const payload = {
                    title:
                        valOrNull(
                            '#add-empTitle'
                        ),

                    firstName:
                        valOrNull(
                            '#add-empFirstName'
                        ),

                    middleName:
                        valOrNull(
                            '#add-empMiddleName'
                        ),

                    lastName:
                        valOrNull(
                            '#add-empLastName'
                        ),

                    gender:
                        valOrNull(
                            '#add-empGender'
                        ),

                    dateOfBirth:
                    dobValue,

                    maritalStatus:
                        valOrNull(
                            '#add-empMaritalStatus'
                        ),

                    bloodGroup:
                        valOrNull(
                            '#add-empBloodGroup'
                        ),

                    religion:
                        valOrNull(
                            '#add-empReligion'
                        ),

                    subReligion:
                        valOrNull(
                            '#add-empSubReligion'
                        ),

                    officialEmail:
                    officialEmail,

                    personalEmail:
                        valOrNull(
                            '#add-empPersonalEmail'
                        ),

                    mobileNo:
                        valOrNull(
                            '#add-empPhone'
                        ),

                    alternateMobile:
                        valOrNull(
                            '#add-empAlternatePhone'
                        ),

                    departmentId:
                        valOrNull(
                            '#add-empDepartment'
                        ),

                    designationId:
                        valOrNull(
                            '#add-empDesignation'
                        ),

                    employeeCategory:
                        valOrNull(
                            '#add-empCategory'
                        ),

                    employeeType:
                        valOrNull(
                            '#add-empType'
                        ),

                    employmentMode:
                        valOrNull(
                            '#add-empMode'
                        ),

                    employmentStatus:
                        'ACTIVE',

                    joiningDate:
                        valOrNull(
                            '#add-empJoiningDate'
                        ),

                    probationEndDate:
                        valOrNull(
                            '#add-empProbationEndDate'
                        ),

                    confirmationDate:
                        valOrNull(
                            '#add-empConfirmationDate'
                        ),

                    nationality:
                        valOrNull(
                            '#add-empNationality'
                        ),

                    nationalId:
                        valOrNull(
                            '#add-empNationalId'
                        ),

                    tinNumber:
                        valOrNull(
                            '#add-empTin'
                        ),

                    passportNo:
                        valOrNull(
                            '#add-empPassportNo'
                        ),

                    passportExpiryDate:
                        valOrNull(
                            '#add-empPassportExpiry'
                        ),

                    workPermitNumber:
                        valOrNull(
                            '#add-empWorkPermit'
                        ),

                    workPermitExpiryDate:
                        valOrNull(
                            '#add-empWorkPermitExpiry'
                        ),

                    addressCountry:
                        valOrNull(
                            '#add-empCountry'
                        ),

                    addressState:
                        valOrNull(
                            '#add-empState'
                        ),

                    addressDistrict:
                        valOrNull(
                            '#add-empDistrict'
                        ),

                    addressCounty:
                        valOrNull(
                            '#add-empCounty'
                        ),

                    addressSubCounty:
                        valOrNull(
                            '#add-empSubCounty'
                        ),

                    addressParish:
                        valOrNull(
                            '#add-empParish'
                        ),

                    addressVillage:
                        valOrNull(
                            '#add-empVillage'
                        ),

                    addressStreet:
                        valOrNull(
                            '#add-empStreet'
                        ),

                    skills:
                        valOrNull(
                            '#add-empSkills'
                        ),

                    languagesSpoken:
                        valOrNull(
                            '#add-empLanguages'
                        ),

                    contacts: (() => {
                        const rows = Array.from(
                            viewContainer.querySelectorAll(
                                '#contacts-container .contact-row'
                            )
                        );

                        return rows
                            .map((row, index) => ({
                                employeeContactName:
                                    row.querySelector('.c-name')
                                        ?.value
                                        ?.trim() || null,

                                employeeContactRelationship:
                                    row.querySelector('.c-relation')
                                        ?.value
                                        ?.trim() || null,

                                employeeContactMobile:
                                    row.querySelector('.c-phone')
                                        ?.value
                                        ?.trim() || null,

                                employeeContactEmail:
                                    row.querySelector('.c-email')
                                        ?.value
                                        ?.trim() || null,

                                employeeContactType:
                                    'EMERGENCY',

                                employeeContactIsPrimary:
                                    index === 0,

                                employeeContactIsEmergency:
                                    true
                            }))
                            .filter(contact =>
                                contact.employeeContactName ||
                                contact.employeeContactMobile
                            );
                    })(),

                    qualifications:
                        await EmpCollections.gatherAsync(
                            viewContainer,
                            '#qualifications-container',
                            'qual-row',
                            async row => {
                                const level =
                                    row.querySelector('.q-level')
                                        ?.value
                                        ?.trim() || null;

                                const customLevel =
                                    row.querySelector(
                                        '.q-custom-level'
                                    )
                                        ?.value
                                        ?.trim() || null;

                                const qualificationName =
                                    level === 'OTHER'
                                        ? customLevel
                                        : level;

                                return {
                                    employeeQualificationLevel:
                                    level,

                                    employeeQualificationName:
                                    qualificationName,

                                    employeeQualificationInstitutionName:
                                        row.querySelector(
                                            '.q-institution'
                                        )
                                            ?.value
                                            ?.trim() || null,

                                    employeeQualificationSpecialization:
                                        row.querySelector(
                                            '.q-specialization'
                                        )
                                            ?.value
                                            ?.trim() || null,

                                    employeeQualificationGrade:
                                        row.querySelector(
                                            '.q-grade'
                                        )
                                            ?.value
                                            ?.trim() || null,

                                    employeeQualificationCompletionYear:
                                        Number.parseInt(
                                            row.querySelector(
                                                '.q-year'
                                            )?.value,
                                            10
                                        ) || null,

                                    fileData:
                                        await EmpCollections.fileToBase64(
                                            row.querySelector(
                                                '.q-file'
                                            )?.files[0]
                                        ),

                                    fileName:
                                        row.querySelector(
                                            '.q-file'
                                        )?.files[0]?.name || null
                                };
                            }
                        ),

                    experiences:
                        await EmpCollections
                            .gatherAsync(
                                viewContainer,
                                '#experiences-container',
                                'exp-row',
                                async row => ({
                                    companyName:
                                        row.querySelector(
                                            '.e-company'
                                        )
                                            .value
                                            .trim() ||
                                        null,

                                    employeeExperienceEmploymentType:
                                        row.querySelector('.e-type')
                                            ?.value
                                            ?.trim() || null,

                                    jobRole:
                                        row.querySelector(
                                            '.e-role'
                                        )
                                            .value
                                            .trim() ||
                                        null,

                                    startDate:
                                        row.querySelector(
                                            '.e-start'
                                        ).value ||
                                        null,

                                    endDate:
                                        row.querySelector(
                                            '.e-end'
                                        ).value ||
                                        null,

                                    fileData:
                                        await EmpCollections
                                            .fileToBase64(
                                                row.querySelector(
                                                    '.e-file'
                                                )
                                                    ?.files[0]
                                            ),

                                    fileName:
                                        row.querySelector(
                                            '.e-file'
                                        )
                                            ?.files[0]
                                            ?.name ||
                                        null
                                })
                            ),

                    documents:
                        await EmpCollections
                            .gatherAsync(
                                viewContainer,
                                '#documents-container',
                                'doc-row',
                                async row => ({
                                    documentType:
                                        row.querySelector(
                                            '.d-type'
                                        )
                                            .value
                                            .trim() ||
                                        null,

                                    documentNumber:
                                        row.querySelector(
                                            '.d-num'
                                        )
                                            .value
                                            .trim() ||
                                        null,

                                    remarks:
                                        row.querySelector(
                                            '.d-remarks'
                                        )
                                            .value
                                            .trim() ||
                                        null,

                                    fileData:
                                        await EmpCollections
                                            .fileToBase64(
                                                row.querySelector(
                                                    '.d-file'
                                                )
                                                    ?.files[0]
                                            ),

                                    fileName:
                                        row.querySelector(
                                            '.d-file'
                                        )
                                            ?.files[0]
                                            ?.name ||
                                        null
                                })
                            ),

                    accountRequest: {
                        generateLogin:
                        generateLogin,

                        roleId:
                            viewContainer.querySelector(
                                '#add-employeeRoleId'
                            )?.value
                                ? Number(
                                    viewContainer.querySelector(
                                        '#add-employeeRoleId'
                                    ).value
                                )
                                : null,

                        sendEmail:
                        sendLoginEmail
                    }
                };

                const response =
                    await apiPost(
                        '/branchadmin/employees',
                        payload
                    );

                showPremiumModal({
                    title:
                        'Employee Registered',

                    type:
                        'success',

                    contentText:
                        `Employee ${response.data.firstName} ${response.data.lastName} registered successfully. Generated Code: ${response.data.employeeNo}`,

                    confirmText:
                        'Done'
                });

                form.reset();

                const loginOptionsReset =
                    viewContainer.querySelector(
                        '#add-loginOptions'
                    );

                if (loginOptionsReset) {
                    loginOptionsReset.classList.add(
                        'hidden'
                    );
                }

                const contactsContainerReset =
                    viewContainer.querySelector(
                        '#contacts-container'
                    );

                const qualificationsContainerReset =
                    viewContainer.querySelector(
                        '#qualifications-container'
                    );

                const experiencesContainerReset =
                    viewContainer.querySelector(
                        '#experiences-container'
                    );

                const documentsContainerReset =
                    viewContainer.querySelector(
                        '#documents-container'
                    );

                if (contactsContainerReset) {
                    contactsContainerReset.innerHTML =
                        '';
                }

                if (qualificationsContainerReset) {
                    qualificationsContainerReset.innerHTML =
                        '';
                }

                if (experiencesContainerReset) {
                    experiencesContainerReset.innerHTML =
                        '';
                }

                if (documentsContainerReset) {
                    documentsContainerReset.innerHTML =
                        '';
                }

                const joiningDateReset =
                    viewContainer.querySelector(
                        '#add-empJoiningDate'
                    );

                if (
                    joiningDateReset &&
                    joiningDateReset['_flatpickr']
                ) {
                    joiningDateReset['_flatpickr']
                        .setDate(new Date());
                } else if (joiningDateReset) {
                    joiningDateReset.value =
                        new Date()
                            .toISOString()
                            .split('T')[0];
                }
            } catch (error) {
                console.error(error);

                showErrorMessage(
                    error.message ||
                    'Failed to register employee.'
                );
            } finally {
                hideLoader();

                submitButton.disabled =
                    false;

                submitButton.innerHTML =
                    originalButtonText;
            }
        }
    );
}


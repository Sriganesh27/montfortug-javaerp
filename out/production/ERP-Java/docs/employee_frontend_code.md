# Employee Frontend Code

Here is the updated complete frontend code for the Employee Module, with the `valueAsDate` bug resolved and IDE warnings addressed.

---

### 1. Update `sidebar.html`
**File:** `src/main/resources/static/components/sidebar.html`
**Action:** Find the `<!-- DEPARTMENTS MODULE -->` section (around line 88), and insert this block right **below** it:

```html
        <!-- EMPLOYEES MODULE -->
        <li data-role="BRANCH_ADMIN" class="has-dropdown">
            <a href="#" class="dropdown-toggle" data-tooltip="Employees">
                <div class="dropdown-label">
                    <i class="bi bi-person-lines-fill"></i> <span>Employees</span>
                </div>
            </a>
            <ul class="sidebar-dropdown">
                <li data-role="BRANCH_ADMIN"><a href="/employees.html">Manage Employees</a></li>
                <li data-role="BRANCH_ADMIN"><a href="/add-employee.html">Add Employee</a></li>
            </ul>
        </li>
```

---

### 2. Update `dashboard.html`
**File:** `src/main/resources/static/dashboard.html`
**Action:** Find `<!-- 4. Load Business Logic Modules -->` (near the bottom), and add the `employees.js` script so it looks like this:

```html
<!-- 4. Load Business Logic Modules -->
<script defer src="/js/modules/applications.js"></script>
<script defer src="/js/modules/departments.js"></script>
<script defer src="/js/modules/designations.js"></script>
<script defer src="/js/modules/employees.js"></script>
```

---

### 3. Create `employees.html`
**File:** `src/main/resources/static/views/admin/employees.html`
**Action:** Create a new file with the following content:

```html
<div id="ba-employees-view" class="fade-in">
    <!-- Header -->
    <div class="view-header">
        <div>
            <h2>Manage Employees</h2>
            <p class="text-muted">View and manage branch employees</p>
        </div>
        <button id="btn-add-employee" class="btn-primary">
            <i class="bi bi-person-plus-fill"></i> Add Employee
        </button>
    </div>

    <!-- MAIN TABLE VIEW -->
    <div id="emp-tableView">
        <!-- Filter Card -->
        <div class="card mb-3 p-3">
            <div class="ba-flex-panel">
                <div class="form-group ba-flex-1">
                    <label>Search Employees</label>
                    <input type="text" id="emp-searchInput" class="detail-input w-100" placeholder="Search by name, email, or code...">
                </div>
                <div class="form-group ba-flex-1">
                    <label>Category</label>
                    <select id="emp-categoryFilter" class="detail-input w-100">
                        <option value="">All Categories</option>
                        <option value="TEACHING">Teaching</option>
                        <option value="NON_TEACHING">Non-Teaching</option>
                        <option value="MANAGEMENT">Management</option>
                        <option value="SUPPORT_STAFF">Support Staff</option>
                    </select>
                </div>
                <div class="form-group ba-flex-1">
                    <label>Status</label>
                    <select id="emp-statusFilter" class="detail-input w-100">
                        <option value="">All Statuses</option>
                        <option value="ACTIVE">Active</option>
                        <option value="ON_LEAVE">On Leave</option>
                        <option value="SUSPENDED">Suspended</option>
                        <option value="TERMINATED">Terminated</option>
                    </select>
                </div>
                <div class="form-group ba-pagination-controls">
                    <button id="emp-searchBtn" class="btn-primary"><i class="bi bi-search"></i> Search</button>
                </div>
            </div>
        </div>

        <!-- Table Card -->
        <div class="card">
            <div class="table-responsive">
                <table id="emp-tableComponent" class="data-table">
                    <thead>
                    <tr>
                        <th data-sort="employeeNo">Code <i class="bi bi-arrow-down-up"></i></th>
                        <th data-sort="fullName">Name <i class="bi bi-arrow-down-up"></i></th>
                        <th data-sort="employeeCategory">Category <i class="bi bi-arrow-down-up"></i></th>
                        <th>Email / Phone</th>
                        <th data-sort="employmentStatus">Status <i class="bi bi-arrow-down-up"></i></th>
                        <th class="col-action align-center">Actions</th>
                    </tr>
                    </thead>
                    <tbody id="emp-tableBody">
                    <!-- Rows rendered via CrudTable -->
                    </tbody>
                </table>
            </div>

            <!-- CrudTable Pagination Footer -->
            <div class="ba-pagination-footer">
                <div class="ba-pagination-controls">
                    <select id="emp-pageSize" class="detail-input w-auto">
                        <option value="5">5</option>
                        <option value="10" selected>10</option>
                        <option value="25">25</option>
                        <option value="50">50</option>
                    </select>
                    <span id="emp-pageInfo" class="text-muted">Showing page 1 of 1</span>
                </div>
                <div class="ba-pagination-controls">
                    <button id="btn-emp-prev" class="btn-secondary btn-sm" disabled>
                        <i class="bi bi-chevron-left"></i> Prev
                    </button>
                    <button id="btn-emp-next" class="btn-secondary btn-sm" disabled>
                        Next <i class="bi bi-chevron-right"></i>
                    </button>
                </div>
            </div>
        </div>
    </div>

    <!-- DETAIL VIEW -->
    <div id="emp-detailView" class="hidden">
        <div class="view-header">
            <h2>Employee: <span id="detail-empNameHeader"></span></h2>
            <div class="action-btn-group">
                <button id="emp-backToTableBtn" class="btn-secondary"><i class="bi bi-arrow-left"></i> Back</button>
                <button id="emp-editBtn" class="btn-primary"><i class="bi bi-pencil"></i> Edit Details</button>
                <button id="emp-cancelEditBtn" class="btn-secondary hidden">Cancel</button>
                <button id="emp-saveBtn" class="btn-success hidden"><i class="bi bi-save"></i> Save Changes</button>
            </div>
        </div>
        <div class="card">
            <div class="detail-grid p-4" style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px;">
                <div class="form-group">
                    <label>Employee Code</label>
                    <span id="view-empCode" class="detail-text text-strong font-monospace text-muted"></span>
                    <input type="text" id="edit-empCode" class="detail-input hidden w-100" disabled title="Code is auto-generated">
                </div>
                <div class="form-group">
                    <label>Full Name</label>
                    <span id="view-empName" class="detail-text text-strong"></span>
                    <div id="edit-empNameContainer" class="hidden" style="display:flex; gap:10px;">
                        <input type="text" id="edit-empFirstName" class="detail-input w-100" placeholder="First Name">
                        <input type="text" id="edit-empLastName" class="detail-input w-100" placeholder="Last Name">
                    </div>
                </div>
                <div class="form-group">
                    <label>Official Email</label>
                    <span id="view-empEmail" class="detail-text text-strong"></span>
                    <input type="email" id="edit-empEmail" class="detail-input hidden w-100">
                </div>
                <div class="form-group">
                    <label>Mobile Number</label>
                    <span id="view-empPhone" class="detail-text text-strong"></span>
                    <input type="text" id="edit-empPhone" class="detail-input hidden w-100">
                </div>
                <div class="form-group">
                    <label>Category</label>
                    <span id="view-empCategory" class="detail-text text-strong"></span>
                    <select id="edit-empCategory" class="detail-input hidden w-100">
                        <option value="TEACHING">Teaching</option>
                        <option value="NON_TEACHING">Non-Teaching</option>
                        <option value="MANAGEMENT">Management</option>
                        <option value="SUPPORT_STAFF">Support Staff</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>Date of Birth</label>
                    <span id="view-empDob" class="detail-text text-strong"></span>
                    <input type="date" id="edit-empDob" class="detail-input hidden w-100">
                </div>
                <div class="form-group">
                    <label>Department</label>
                    <span id="view-empDepartment" class="detail-text text-strong"></span>
                    <select id="edit-empDepartment" class="detail-input hidden w-100">
                        <option value="">-- None --</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>Designation</label>
                    <span id="view-empDesignation" class="detail-text text-strong"></span>
                    <select id="edit-empDesignation" class="detail-input hidden w-100">
                        <option value="">-- None --</option>
                    </select>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- TEMPLATES -->
<template id="tpl-emp-row">
    <tr>
        <td class="col-code font-monospace text-muted"></td>
        <td class="col-name text-primary cursor-pointer"><strong></strong></td>
        <td class="col-category"></td>
        <td class="col-contact"><small class="d-block email-val"></small><small class="text-muted phone-val"></small></td>
        <td><span class="status-badge badge"></span></td>
        <td class="col-action align-center">
            <button class="btn-primary btn-sm view-more-btn me-1" title="View Details"><i class="bi bi-eye"></i></button>
            <button class="btn-danger btn-sm delete-btn" title="Terminate"><i class="bi bi-trash"></i></button>
        </td>
    </tr>
</template>
```

---

### 4. Create `add-employee.html`
**File:** `src/main/resources/static/views/admin/add-employee.html`
**Action:** Create a new file with the following content:

```html
<div id="ba-add-employee-view" class="fade-in hidden">
    <div class="card max-w-2xl mx-auto mt-4">
        <div class="view-header p-3 border-bottom">
            <h2 class="mb-0"><i class="bi bi-person-plus me-2"></i>Register New Employee</h2>
            <button id="backToEmployeesBtn" class="btn-secondary btn-sm"><i class="bi bi-arrow-left"></i> Back</button>
        </div>
        <div class="card-body p-4">
            <form id="add-emp-form">
                
                <div class="detail-grid mb-3" style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px;">
                    <div class="form-group">
                        <label class="detail-text text-strong d-block mb-1">First Name <span class="text-danger">*</span></label>
                        <input type="text" id="add-empFirstName" class="detail-input w-100" required placeholder="John">
                    </div>
                    <div class="form-group">
                        <label class="detail-text text-strong d-block mb-1">Last Name <span class="text-danger">*</span></label>
                        <input type="text" id="add-empLastName" class="detail-input w-100" required placeholder="Doe">
                    </div>
                </div>

                <div class="detail-grid mb-3" style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px;">
                    <div class="form-group">
                        <label class="detail-text text-strong d-block mb-1">Email <span class="text-danger">*</span></label>
                        <input type="email" id="add-empEmail" class="detail-input w-100" required placeholder="johndoe@school.edu">
                    </div>
                    <div class="form-group">
                        <label class="detail-text text-strong d-block mb-1">Mobile No <span class="text-danger">*</span></label>
                        <input type="text" id="add-empPhone" class="detail-input w-100" required placeholder="07XX XXX XXX">
                    </div>
                </div>

                <div class="detail-grid mb-3" style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px;">
                    <div class="form-group">
                        <label class="detail-text text-strong d-block mb-1">Category <span class="text-danger">*</span></label>
                        <select id="add-empCategory" class="detail-input w-100" required>
                            <option value="TEACHING">Teaching</option>
                            <option value="NON_TEACHING">Non-Teaching</option>
                            <option value="MANAGEMENT">Management</option>
                            <option value="SUPPORT_STAFF">Support Staff</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label class="detail-text text-strong d-block mb-1">Joining Date <span class="text-danger">*</span></label>
                        <input type="date" id="add-empJoiningDate" class="detail-input w-100" required>
                    </div>
                </div>

                <div class="form-group mb-4">
                    <label class="detail-text text-strong d-block mb-1">Date of Birth</label>
                    <input type="date" id="add-empDob" class="detail-input w-100">
                </div>

                <div class="text-end border-top pt-3">
                    <button type="submit" class="btn-primary px-4"><i class="bi bi-check-lg"></i> Register Employee</button>
                </div>
            </form>
        </div>
    </div>
</div>
```

---

### 5. Create `employees.js`
**File:** `src/main/resources/static/js/modules/employees.js`
**Action:** Replace the existing `employees.js` with this updated file that resolves the `valueAsDate` issue caused by flatpickr.

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

function initEmployeesView() {
    const viewContainer = document.querySelector('#ba-employees-view');
    if (!viewContainer) return;

    // Initialize calendar if available
    if (typeof createErpCalendar === 'function') {
        createErpCalendar('#edit-empDob');
    }

    // --- SPA NAVIGATION ROUTING FOR "ADD EMPLOYEE" ---
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

    const state = { page: 0, size: 10, sort: 'employeeId,desc' };
    let table;

    async function loadEmployees() {
        if (table) table.showLoading();
        try {
            const searchVal = viewContainer.querySelector('#emp-searchInput').value.trim();
            const categoryVal = viewContainer.querySelector('#emp-categoryFilter').value;
            const statusVal = viewContainer.querySelector('#emp-statusFilter').value;

            // Search Payload
            const payload = {
                keyword: searchVal || null,
                category: categoryVal || null,
                status: statusVal || null
            };

            const url = `/branchadmin/employees/search?page=${state.page}&size=${state.size}&sort=${state.sort}`;
            
            // Note: API requires POST for complex search criteria
            const res = await apiPost(url, payload);
            const pageData = res.data;

            // Render Rows
            table.render(pageData.content, (emp, rowNode) => {
                rowNode.querySelector('.col-code').textContent = emp.employeeNo;

                const nameNode = rowNode.querySelector('.col-name strong');
                nameNode.textContent = `${emp.firstName} ${emp.lastName}`;
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

            table.renderPagination(pageData.pageable ? pageData.pageable.pageNumber : 0, pageData.totalPages, pageData.totalElements);

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

    // Bind Search Filters
    const searchBtn = viewContainer.querySelector('#emp-searchBtn');
    if (searchBtn) searchBtn.addEventListener('click', async () => { state.page = 0; await loadEmployees(); });

    async function loadSelectOptions() {
        try {
            const branchId = parseInt(localStorage.getItem('user_branch')) || 1;
            // Fetch Departments and Designations to populate edit selects
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
                designationSelect.innerHTML += `<option value="${d.designationId}">${d.designationName}</option>`;
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

            viewContainer.querySelector('#detail-empNameHeader').textContent = `${emp.firstName} ${emp.lastName}`;
            
            viewContainer.querySelector('#view-empCode').textContent = emp.employeeNo;
            viewContainer.querySelector('#view-empName').textContent = `${emp.firstName} ${emp.lastName}`;
            viewContainer.querySelector('#view-empEmail').textContent = emp.email || 'N/A';
            viewContainer.querySelector('#view-empPhone').textContent = emp.phone || 'N/A';
            viewContainer.querySelector('#view-empCategory').textContent = formatEnum(emp.category);
            viewContainer.querySelector('#view-empDob').textContent = emp.dateOfBirth || 'N/A';
            
            // Set underlying edit inputs
            viewContainer.querySelector('#edit-empCode').value = emp.employeeNo;
            viewContainer.querySelector('#edit-empFirstName').value = emp.firstName;
            viewContainer.querySelector('#edit-empLastName').value = emp.lastName;
            viewContainer.querySelector('#edit-empEmail').value = emp.email || '';
            viewContainer.querySelector('#edit-empPhone').value = emp.phone || '';
            viewContainer.querySelector('#edit-empCategory').value = emp.category;
            
            // Flatpickr safe assignment
            const dobInput = viewContainer.querySelector('#edit-empDob');
            if (dobInput._flatpickr && emp.dateOfBirth) {
                dobInput._flatpickr.setDate(emp.dateOfBirth);
            } else {
                dobInput.value = emp.dateOfBirth || '';
            }
            
            if(emp.departmentId) viewContainer.querySelector('#edit-empDepartment').value = emp.departmentId;
            if(emp.designationId) viewContainer.querySelector('#edit-empDesignation').value = emp.designationId;

            tableView.classList.add('hidden');
            detailView.classList.remove('hidden');
            resetEmpEditMode();
        } catch (e) {
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
            viewContainer.querySelector('#edit-empCode').classList.remove('hidden');

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
            const payload = {
                firstName: viewContainer.querySelector('#edit-empFirstName').value,
                lastName: viewContainer.querySelector('#edit-empLastName').value,
                email: viewContainer.querySelector('#edit-empEmail').value,
                phone: viewContainer.querySelector('#edit-empPhone').value,
                category: viewContainer.querySelector('#edit-empCategory').value,
                dateOfBirth: viewContainer.querySelector('#edit-empDob').value || null,
                departmentId: viewContainer.querySelector('#edit-empDepartment').value || null,
                designationId: viewContainer.querySelector('#edit-empDesignation').value || null
            };

            showLoader();
            try {
                await apiPut(`/branchadmin/employees/${currentDetailEmpId}`, payload);
                showSuccessMessage('Employee updated successfully.');
                await openEmpDetail(currentDetailEmpId);
            } catch (e) {
                showErrorMessage('Failed to update employee.');
            } finally {
                hideLoader();
            }
        });
    }

    function resetEmpEditMode() {
        viewContainer.querySelectorAll('.detail-text').forEach(el => el.classList.remove('hidden'));
        viewContainer.querySelectorAll('.detail-input').forEach(el => el.classList.add('hidden'));
        viewContainer.querySelector('#edit-empNameContainer').classList.add('hidden');
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

    // Initialize calendar if available
    if (typeof createErpCalendar === 'function') {
        createErpCalendar('#add-empDob');
        createErpCalendar('#add-empJoiningDate');
    }

    const oldForm = viewContainer.querySelector('#add-emp-form');
    let form = oldForm;
    if (oldForm) {
        form = oldForm.cloneNode(true);
        oldForm.parentNode.replaceChild(form, oldForm);
        form.reset();
        
        // Fix for Flatpickr overriding input types
        const todayStr = new Date().toISOString().split('T')[0];
        const joinInput = viewContainer.querySelector('#add-empJoiningDate');
        if (joinInput._flatpickr) {
            joinInput._flatpickr.setDate(new Date());
        } else {
            joinInput.value = todayStr;
        }
    }

    // SPA NAVIGATION ROUTING FOR "BACK" BUTTON
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

            const submitBtn = form.querySelector('button[type="submit"]');
            const originalText = submitBtn.innerHTML;
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<i class="bi bi-hourglass-split"></i> Saving...';

            const payload = {
                firstName: viewContainer.querySelector('#add-empFirstName').value,
                lastName: viewContainer.querySelector('#add-empLastName').value,
                email: viewContainer.querySelector('#add-empEmail').value,
                phone: viewContainer.querySelector('#add-empPhone').value,
                category: viewContainer.querySelector('#add-empCategory').value,
                joiningDate: viewContainer.querySelector('#add-empJoiningDate').value,
                dateOfBirth: viewContainer.querySelector('#add-empDob').value || null
            };

            showLoader();
            try {
                const res = await apiPost('/branchadmin/employees', payload);
                showPremiumModal({
                    title: 'Employee Registered',
                    type: 'success',
                    contentText: `Employee ${res.data.firstName} ${res.data.lastName} registered successfully. Generated Code: ${res.data.employeeNo}`,
                    confirmText: 'Done'
                });
                form.reset();
                const joinInputReset = viewContainer.querySelector('#add-empJoiningDate');
                if (joinInputReset._flatpickr) {
                    joinInputReset._flatpickr.setDate(new Date());
                } else {
                    joinInputReset.value = new Date().toISOString().split('T')[0];
                }
            } catch (err) {
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

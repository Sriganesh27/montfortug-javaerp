# Departments Module - Enterprise UI

Here is the robust, SPA-compliant code for the Departments module. It strictly uses DOM manipulation (`cloneNode`) instead of `innerHTML`, integrates with `GlobalPagination`, and matches your `superadmin` design.

### 1. `views/admin/departments.html`
*(Create this file to handle the data table and the inline "View More" panel)*

```html
<div id="admin-departments-view">
    <!-- MAIN TABLE VIEW -->
    <div id="dept-tableView">
        <div class="view-header">
            <h2>Manage Departments</h2>
            <div class="action-btn-group">
                <a href="/add-department.html" class="btn-primary" id="btn-addDepartment"><i class="bi bi-plus-circle"></i> Add Department</a>
            </div>
        </div>

        <!-- Filter/Search Bar -->
        <div class="card filter-card">
            <div class="detail-grid" style="grid-template-columns: 1fr 1fr 1fr auto; align-items: end;">
                <div class="form-group">
                    <label>Search</label>
                    <input type="text" id="dept-searchInput" placeholder="Search by name or code...">
                </div>
                <div class="form-group">
                    <label>Status</label>
                    <select id="dept-statusFilter">
                        <option value="">All Statuses</option>
                        <option value="ACTIVE">Active</option>
                        <option value="INACTIVE">Inactive</option>
                        <option value="DELETED">Deleted</option>
                    </select>
                </div>
                <div class="form-group">
                    <button id="dept-searchBtn" class="btn-primary" style="margin-bottom: 5px;"><i class="bi bi-search"></i> Search</button>
                </div>
            </div>
        </div>

        <div class="card">
            <div class="table-responsive">
                <table class="data-table" id="dept-table">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Department Code</th>
                        <th>Department Name</th>
                        <th>Parent Dept ID</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                    </thead>
                    <tbody id="dept-tableBody">
                    </tbody>
                </table>
            </div>
            
            <!-- Pagination Container -->
            <div id="dept-pagination-container" class="pagination-container"></div>
        </div>
    </div>

    <!-- VIEW MORE / INLINE EDIT PAGE -->
    <div id="dept-detailView" class="hidden">
        <div class="view-header">
            <h2>Department Details: <span id="detail-deptNameHeader"></span></h2>
            <div class="action-btn-group">
                <button id="dept-backToTableBtn" class="btn-secondary"><i class="bi bi-arrow-left"></i> Back</button>
                <button id="dept-editBtn" class="btn-primary"><i class="bi bi-pencil"></i> Edit Mode</button>
                <button id="dept-cancelEditBtn" class="btn-secondary hidden"><i class="bi bi-x-circle"></i> Cancel</button>
                <button id="dept-saveBtn" class="btn-success hidden"><i class="bi bi-save"></i> Save Changes</button>
            </div>
        </div>

        <div class="card">
            <div class="detail-grid">
                <!-- Basic Info -->
                <div class="form-group">
                    <label>Department Code</label>
                    <span class="detail-text" id="view-deptCode"></span>
                    <input type="text" class="detail-input hidden" id="edit-deptCode">
                </div>
                <div class="form-group">
                    <label>Department Name</label>
                    <span class="detail-text" id="view-deptName"></span>
                    <input type="text" class="detail-input hidden" id="edit-deptName">
                </div>
                <div class="form-group">
                    <label>Parent Department ID</label>
                    <span class="detail-text" id="view-parentDeptId"></span>
                    <input type="number" class="detail-input hidden" id="edit-parentDeptId">
                </div>
                <div class="form-group full-width-group">
                    <label>Description</label>
                    <span class="detail-text" id="view-deptDescription"></span>
                    <textarea class="detail-input hidden" id="edit-deptDescription" rows="3"></textarea>
                </div>
            </div>
        </div>
    </div>

    <!-- PURE HTML TEMPLATE FOR ROWS (No JS innerHTML) -->
    <template id="dept-row-template">
        <tr>
            <td class="col-id"></td>
            <td class="col-code"></td>
            <td class="col-name"><strong></strong></td>
            <td class="col-parent"></td>
            <td class="col-status">
                <span class="status-badge"></span>
            </td>
            <td class="col-actions">
                <button class="action-btn view-more-btn"><i class="bi bi-eye"></i> View</button>
                <button class="action-btn delete-btn"><i class="bi bi-trash"></i></button>
            </td>
        </tr>
    </template>
</div>
```

---

### 2. `views/admin/add-department.html`
*(Create this file for the dedicated "Add" form)*

```html
<div id="admin-add-department-view">
    <div class="view-header">
        <h2>Add New Department</h2>
        <button id="backToDepartmentsBtn" class="btn-secondary"><i class="bi bi-arrow-left"></i> Back to Departments</button>
    </div>

    <form id="add-dept-form">
        <div class="card add-branch-card">
            <h3 class="card-header">Department Information</h3>
            <div class="detail-grid">
                <div class="form-group">
                    <label for="add-deptCode">Department Code <span style="color:red">*</span></label>
                    <input type="text" id="add-deptCode" placeholder="e.g. MATH01" required />
                </div>
                <div class="form-group">
                    <label for="add-deptName">Department Name <span style="color:red">*</span></label>
                    <input type="text" id="add-deptName" required />
                </div>
                <div class="form-group">
                    <label for="add-parentDeptId">Parent Department ID (Optional)</label>
                    <input type="number" id="add-parentDeptId" placeholder="Leave blank if main department" />
                </div>
                <div class="form-group full-width-group">
                    <label for="add-deptDescription">Description</label>
                    <textarea id="add-deptDescription" rows="3" placeholder="Enter department description..."></textarea>
                </div>
            </div>
        </div>

        <div class="form-actions add-branch-actions">
            <button type="submit" class="btn-primary btn-large"><i class="bi bi-save"></i> Save Department</button>
        </div>
    </form>
</div>
```

---

### 3. `js/modules/departments.js`
*(Create this JS file and add `<script src="/js/modules/departments.js"></script>` to your `dashboard.html`)*

```javascript
/* global apiGet, apiPost, apiPut, apiDelete, showLoader, hideLoader, showPremiumModal, showSuccessMessage, showErrorMessage, GlobalPagination */

document.addEventListener('viewLoaded', function(e) {
    if (e.detail.role === 'admin') { // Note: Matching your branch admin routing
        if (e.detail.view === 'departments') {
            initDepartmentsView();
        } else if (e.detail.view === 'add-department') {
            initAddDepartmentView();
        }
    }
});

let deptPagination = null;
let currentDetailDeptId = null;

function initDepartmentsView() {
    const viewContainer = document.querySelector('#admin-departments-view');
    if (!viewContainer) return;

    const tableView = viewContainer.querySelector('#dept-tableView');
    const detailView = viewContainer.querySelector('#dept-detailView');
    const tableBody = viewContainer.querySelector('#dept-tableBody');
    const template = viewContainer.querySelector('#dept-row-template');

    // Clean up old instances of pagination to prevent memory leaks
    if (deptPagination) {
        deptPagination.destroy();
    }

    deptPagination = new GlobalPagination({
        endpoint: '/api/departments',
        tableBodyId: 'dept-tableBody',
        paginationContainerId: 'dept-pagination-container',
        templateId: 'global-table-empty-template', // Uses your layout.js empty template
        renderRowCallback: renderDeptRow
    });

    function renderDeptRow(dept, tbody) {
        if (!template) return;
        const clone = template.content.cloneNode(true);
        
        clone.querySelector('.col-id').textContent = dept.departmentId;
        clone.querySelector('.col-code').textContent = dept.departmentCode;
        clone.querySelector('.col-name strong').textContent = dept.departmentName;
        clone.querySelector('.col-parent').textContent = dept.parentDepartmentId || 'None';
        
        const statusBadge = clone.querySelector('.status-badge');
        statusBadge.textContent = dept.recordStatus || 'ACTIVE';
        statusBadge.className = `status-badge badge-${(dept.recordStatus || 'ACTIVE').toLowerCase()}`;
        
        const viewBtn = clone.querySelector('.view-more-btn');
        viewBtn.setAttribute('data-id', dept.departmentId);
        viewBtn.addEventListener('click', () => openDeptDetail(dept.departmentId));

        const delBtn = clone.querySelector('.delete-btn');
        delBtn.setAttribute('data-id', dept.departmentId);
        delBtn.addEventListener('click', () => deleteDept(dept.departmentId));

        tbody.appendChild(clone);
    }

    // Search Logic
    const searchBtn = viewContainer.querySelector('#dept-searchBtn');
    if (searchBtn) {
        searchBtn.addEventListener('click', () => {
            const searchVal = viewContainer.querySelector('#dept-searchInput').value;
            const statusVal = viewContainer.querySelector('#dept-statusFilter').value;
            
            deptPagination.setAdditionalParams({
                search: searchVal || null,
                status: statusVal || null
            });
            deptPagination.loadPage(0);
        });
    }

    // Detail View Logic
    async function openDeptDetail(id) {
        currentDetailDeptId = id;
        showLoader();
        try {
            const res = await apiGet(`/api/departments/${id}`);
            const dept = res.data;

            viewContainer.querySelector('#detail-deptNameHeader').textContent = dept.departmentName;
            viewContainer.querySelector('#view-deptCode').textContent = dept.departmentCode;
            viewContainer.querySelector('#view-deptName').textContent = dept.departmentName;
            viewContainer.querySelector('#view-parentDeptId').textContent = dept.parentDepartmentId || 'None';
            viewContainer.querySelector('#view-deptDescription').textContent = dept.description || 'No description provided.';

            tableView.classList.add('hidden');
            detailView.classList.remove('hidden');
            resetDeptEditMode();
        } catch (e) {
            console.error(e);
            showErrorMessage("Failed to load department details.");
        } finally {
            hideLoader();
        }
    }

    async function deleteDept(id) {
        showPremiumModal({
            title: 'Delete Department',
            type: 'warning',
            contentText: 'Are you sure you want to delete this department? This may affect associated designations.',
            confirmText: 'Yes, Delete',
            cancelText: 'Cancel',
            onConfirm: async (modal) => {
                modal.close();
                showLoader();
                try {
                    await apiDelete(`/api/departments/${id}`);
                    showSuccessMessage('Department deleted successfully.');
                    deptPagination.loadPage(deptPagination.currentPage);
                } catch (e) {
                    showErrorMessage('Failed to delete department.');
                } finally {
                    hideLoader();
                }
            }
        });
    }

    // Edit Logic
    const editBtn = viewContainer.querySelector('#dept-editBtn');
    const saveBtn = viewContainer.querySelector('#dept-saveBtn');
    const cancelEditBtn = viewContainer.querySelector('#dept-cancelEditBtn');
    const backBtn = viewContainer.querySelector('#dept-backToTableBtn');

    if (backBtn) {
        backBtn.addEventListener('click', () => {
            detailView.classList.add('hidden');
            tableView.classList.remove('hidden');
            deptPagination.loadPage(deptPagination.currentPage); // Refresh table
        });
    }

    if (editBtn) {
        editBtn.addEventListener('click', () => {
            viewContainer.querySelectorAll('.detail-text').forEach(el => el.classList.add('hidden'));
            viewContainer.querySelectorAll('.detail-input').forEach(el => el.classList.remove('hidden'));
            
            viewContainer.querySelector('#edit-deptCode').value = viewContainer.querySelector('#view-deptCode').textContent;
            viewContainer.querySelector('#edit-deptName').value = viewContainer.querySelector('#view-deptName').textContent;
            
            const parentIdText = viewContainer.querySelector('#view-parentDeptId').textContent;
            viewContainer.querySelector('#edit-parentDeptId').value = parentIdText !== 'None' ? parentIdText : '';
            
            const descText = viewContainer.querySelector('#view-deptDescription').textContent;
            viewContainer.querySelector('#edit-deptDescription').value = descText !== 'No description provided.' ? descText : '';

            editBtn.classList.add('hidden');
            saveBtn.classList.remove('hidden');
            cancelEditBtn.classList.remove('hidden');
        });
    }

    if (cancelEditBtn) {
        cancelEditBtn.addEventListener('click', () => {
            resetDeptEditMode();
        });
    }

    if (saveBtn) {
        saveBtn.addEventListener('click', async () => {
            const payload = {
                departmentCode: viewContainer.querySelector('#edit-deptCode').value,
                departmentName: viewContainer.querySelector('#edit-deptName').value,
                description: viewContainer.querySelector('#edit-deptDescription').value,
                parentDepartmentId: viewContainer.querySelector('#edit-parentDeptId').value || null
            };

            showLoader();
            try {
                await apiPut(`/api/departments/${currentDetailDeptId}`, payload);
                showSuccessMessage('Department updated successfully.');
                openDeptDetail(currentDetailDeptId); // Reload view
            } catch (e) {
                showErrorMessage('Failed to update department.');
            } finally {
                hideLoader();
            }
        });
    }

    function resetDeptEditMode() {
        viewContainer.querySelectorAll('.detail-text').forEach(el => el.classList.remove('hidden'));
        viewContainer.querySelectorAll('.detail-input').forEach(el => el.classList.add('hidden'));
        if (editBtn) editBtn.classList.remove('hidden');
        if (saveBtn) saveBtn.classList.add('hidden');
        if (cancelEditBtn) cancelEditBtn.classList.add('hidden');
    }

    // Initial Load
    deptPagination.loadPage(0);
}

function initAddDepartmentView() {
    const viewContainer = document.querySelector('#admin-add-department-view');
    if (!viewContainer) return;

    // SPA Event clean-up: clone form
    const oldForm = viewContainer.querySelector('#add-dept-form');
    let form = oldForm;
    if (oldForm) {
        form = oldForm.cloneNode(true);
        oldForm.parentNode.replaceChild(form, oldForm);
        form.reset();
    }

    const backBtn = viewContainer.querySelector('#backToDepartmentsBtn');
    if (backBtn) {
        const newBackBtn = backBtn.cloneNode(true);
        backBtn.parentNode.replaceChild(newBackBtn, backBtn);
        newBackBtn.addEventListener('click', () => {
            const mainContent = document.getElementById('main-content-area');
            window.history.pushState({ view: 'departments', title: 'Departments' }, "", "/admin/departments");
            const pageTitleElement = document.getElementById('pageTitle');
            if (pageTitleElement) pageTitleElement.textContent = "Manage Departments";
            // Important: Uses your global layout.js loadView function
            void loadView('admin', 'departments', mainContent); 
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
                departmentCode: viewContainer.querySelector('#add-deptCode').value,
                departmentName: viewContainer.querySelector('#add-deptName').value,
                description: viewContainer.querySelector('#add-deptDescription').value,
                parentDepartmentId: viewContainer.querySelector('#add-parentDeptId').value || null
            };

            showLoader();
            try {
                await apiPost('/api/departments', payload);
                showSuccessMessage('Department created successfully!');
                form.reset();
            } catch (err) {
                showErrorMessage('Failed to create department.');
            } finally {
                hideLoader();
                submitBtn.disabled = false;
                submitBtn.innerHTML = originalText;
            }
        });
    }
}
```

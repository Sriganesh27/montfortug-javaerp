# Designations Module - Enterprise UI

Here is the corresponding code for the Designations module following the same pure-DOM enterprise architecture.

### 1. `views/admin/designations.html`

```html
<div id="admin-designations-view">
    <!-- MAIN TABLE VIEW -->
    <div id="desig-tableView">
        <div class="view-header">
            <h2>Manage Designations</h2>
            <div class="action-btn-group">
                <a href="/add-designation.html" class="btn-primary" id="btn-addDesignation"><i class="bi bi-plus-circle"></i> Add Designation</a>
            </div>
        </div>

        <!-- Filter/Search Bar -->
        <div class="card filter-card">
            <div class="detail-grid" style="grid-template-columns: 1fr 1fr 1fr auto; align-items: end;">
                <div class="form-group">
                    <label>Search</label>
                    <input type="text" id="desig-searchInput" placeholder="Search title or code...">
                </div>
                <div class="form-group">
                    <label>Status</label>
                    <select id="desig-statusFilter">
                        <option value="">All Statuses</option>
                        <option value="ACTIVE">Active</option>
                        <option value="INACTIVE">Inactive</option>
                        <option value="DELETED">Deleted</option>
                    </select>
                </div>
                <div class="form-group">
                    <button id="desig-searchBtn" class="btn-primary" style="margin-bottom: 5px;"><i class="bi bi-search"></i> Search</button>
                </div>
            </div>
        </div>

        <div class="card">
            <div class="table-responsive">
                <table class="data-table" id="desig-table">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Designation Code</th>
                        <th>Designation Title</th>
                        <th>Level</th>
                        <th>Dept ID</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                    </thead>
                    <tbody id="desig-tableBody">
                    </tbody>
                </table>
            </div>
            
            <div id="desig-pagination-container" class="pagination-container"></div>
        </div>
    </div>

    <!-- VIEW MORE / INLINE EDIT PAGE -->
    <div id="desig-detailView" class="hidden">
        <div class="view-header">
            <h2>Designation Details: <span id="detail-desigTitleHeader"></span></h2>
            <div class="action-btn-group">
                <button id="desig-backToTableBtn" class="btn-secondary"><i class="bi bi-arrow-left"></i> Back</button>
                <button id="desig-editBtn" class="btn-primary"><i class="bi bi-pencil"></i> Edit Mode</button>
                <button id="desig-cancelEditBtn" class="btn-secondary hidden"><i class="bi bi-x-circle"></i> Cancel</button>
                <button id="desig-saveBtn" class="btn-success hidden"><i class="bi bi-save"></i> Save Changes</button>
            </div>
        </div>

        <div class="card">
            <div class="detail-grid">
                <div class="form-group">
                    <label>Designation Code</label>
                    <span class="detail-text" id="view-desigCode"></span>
                    <input type="text" class="detail-input hidden" id="edit-desigCode">
                </div>
                <div class="form-group">
                    <label>Designation Title</label>
                    <span class="detail-text" id="view-desigTitle"></span>
                    <input type="text" class="detail-input hidden" id="edit-desigTitle">
                </div>
                <div class="form-group">
                    <label>Hierarchy Level</label>
                    <span class="detail-text" id="view-hierarchyLevel"></span>
                    <input type="number" class="detail-input hidden" id="edit-hierarchyLevel">
                </div>
                <div class="form-group">
                    <label>Department ID</label>
                    <span class="detail-text" id="view-deptId"></span>
                    <input type="number" class="detail-input hidden" id="edit-deptId">
                </div>
                <div class="form-group full-width-group">
                    <label>Description</label>
                    <span class="detail-text" id="view-desigDescription"></span>
                    <textarea class="detail-input hidden" id="edit-desigDescription" rows="3"></textarea>
                </div>
            </div>
        </div>
    </div>

    <template id="desig-row-template">
        <tr>
            <td class="col-id"></td>
            <td class="col-code"></td>
            <td class="col-title"><strong></strong></td>
            <td class="col-level"></td>
            <td class="col-dept"></td>
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

### 2. `views/admin/add-designation.html`

```html
<div id="admin-add-designation-view">
    <div class="view-header">
        <h2>Add New Designation</h2>
        <button id="backToDesignationsBtn" class="btn-secondary"><i class="bi bi-arrow-left"></i> Back to Designations</button>
    </div>

    <form id="add-desig-form">
        <div class="card add-branch-card">
            <h3 class="card-header">Designation Information</h3>
            <div class="detail-grid">
                <div class="form-group">
                    <label for="add-desigCode">Designation Code <span style="color:red">*</span></label>
                    <input type="text" id="add-desigCode" placeholder="e.g. TCH" required />
                </div>
                <div class="form-group">
                    <label for="add-desigTitle">Designation Title <span style="color:red">*</span></label>
                    <input type="text" id="add-desigTitle" placeholder="e.g. Senior Teacher" required />
                </div>
                <div class="form-group">
                    <label for="add-hierarchyLevel">Hierarchy Level</label>
                    <input type="number" id="add-hierarchyLevel" placeholder="e.g. 1 (Top Level)" />
                </div>
                <div class="form-group">
                    <label for="add-deptId">Department ID <span style="color:red">*</span></label>
                    <!-- In a real scenario, you'd populate this with a dropdown of Departments via JS -->
                    <input type="number" id="add-deptId" required /> 
                </div>
                <div class="form-group full-width-group">
                    <label for="add-desigDescription">Description</label>
                    <textarea id="add-desigDescription" rows="3" placeholder="Enter job description or responsibilities..."></textarea>
                </div>
            </div>
        </div>

        <div class="form-actions add-branch-actions">
            <button type="submit" class="btn-primary btn-large"><i class="bi bi-save"></i> Save Designation</button>
        </div>
    </form>
</div>
```

---

### 3. `js/modules/designations.js`
*(Create this JS file and add `<script src="/js/modules/designations.js"></script>` to your `dashboard.html`)*

```javascript
/* global apiGet, apiPost, apiPut, apiDelete, showLoader, hideLoader, showPremiumModal, showSuccessMessage, showErrorMessage, GlobalPagination */

document.addEventListener('viewLoaded', function(e) {
    if (e.detail.role === 'admin') {
        if (e.detail.view === 'designations') {
            initDesignationsView();
        } else if (e.detail.view === 'add-designation') {
            initAddDesignationView();
        }
    }
});

let desigPagination = null;
let currentDetailDesigId = null;

function initDesignationsView() {
    const viewContainer = document.querySelector('#admin-designations-view');
    if (!viewContainer) return;

    const tableView = viewContainer.querySelector('#desig-tableView');
    const detailView = viewContainer.querySelector('#desig-detailView');
    const tableBody = viewContainer.querySelector('#desig-tableBody');
    const template = viewContainer.querySelector('#desig-row-template');

    if (desigPagination) {
        desigPagination.destroy();
    }

    desigPagination = new GlobalPagination({
        endpoint: '/api/designations',
        tableBodyId: 'desig-tableBody',
        paginationContainerId: 'desig-pagination-container',
        templateId: 'global-table-empty-template', 
        renderRowCallback: renderDesigRow
    });

    function renderDesigRow(desig, tbody) {
        if (!template) return;
        const clone = template.content.cloneNode(true);
        
        clone.querySelector('.col-id').textContent = desig.designationId;
        clone.querySelector('.col-code').textContent = desig.designationCode;
        clone.querySelector('.col-title strong').textContent = desig.designationTitle;
        clone.querySelector('.col-level').textContent = desig.hierarchyLevel || 'N/A';
        clone.querySelector('.col-dept').textContent = desig.departmentId || 'None';
        
        const statusBadge = clone.querySelector('.status-badge');
        statusBadge.textContent = desig.recordStatus || 'ACTIVE';
        statusBadge.className = `status-badge badge-${(desig.recordStatus || 'ACTIVE').toLowerCase()}`;
        
        const viewBtn = clone.querySelector('.view-more-btn');
        viewBtn.setAttribute('data-id', desig.designationId);
        viewBtn.addEventListener('click', () => openDesigDetail(desig.designationId));

        const delBtn = clone.querySelector('.delete-btn');
        delBtn.setAttribute('data-id', desig.designationId);
        delBtn.addEventListener('click', () => deleteDesig(desig.designationId));

        tbody.appendChild(clone);
    }

    const searchBtn = viewContainer.querySelector('#desig-searchBtn');
    if (searchBtn) {
        searchBtn.addEventListener('click', () => {
            const searchVal = viewContainer.querySelector('#desig-searchInput').value;
            const statusVal = viewContainer.querySelector('#desig-statusFilter').value;
            
            desigPagination.setAdditionalParams({
                search: searchVal || null,
                status: statusVal || null
            });
            desigPagination.loadPage(0);
        });
    }

    async function openDesigDetail(id) {
        currentDetailDesigId = id;
        showLoader();
        try {
            const res = await apiGet(`/api/designations/${id}`);
            const desig = res.data;

            viewContainer.querySelector('#detail-desigTitleHeader').textContent = desig.designationTitle;
            viewContainer.querySelector('#view-desigCode').textContent = desig.designationCode;
            viewContainer.querySelector('#view-desigTitle').textContent = desig.designationTitle;
            viewContainer.querySelector('#view-hierarchyLevel').textContent = desig.hierarchyLevel || 'N/A';
            viewContainer.querySelector('#view-deptId').textContent = desig.departmentId || 'None';
            viewContainer.querySelector('#view-desigDescription').textContent = desig.description || 'No description provided.';

            tableView.classList.add('hidden');
            detailView.classList.remove('hidden');
            resetDesigEditMode();
        } catch (e) {
            showErrorMessage("Failed to load designation details.");
        } finally {
            hideLoader();
        }
    }

    async function deleteDesig(id) {
        showPremiumModal({
            title: 'Delete Designation',
            type: 'warning',
            contentText: 'Are you sure you want to soft delete this designation?',
            confirmText: 'Yes, Delete',
            cancelText: 'Cancel',
            onConfirm: async (modal) => {
                modal.close();
                showLoader();
                try {
                    await apiDelete(`/api/designations/${id}`);
                    showSuccessMessage('Designation deleted successfully.');
                    desigPagination.loadPage(desigPagination.currentPage);
                } catch (e) {
                    showErrorMessage('Failed to delete designation.');
                } finally {
                    hideLoader();
                }
            }
        });
    }

    const editBtn = viewContainer.querySelector('#desig-editBtn');
    const saveBtn = viewContainer.querySelector('#desig-saveBtn');
    const cancelEditBtn = viewContainer.querySelector('#desig-cancelEditBtn');
    const backBtn = viewContainer.querySelector('#desig-backToTableBtn');

    if (backBtn) {
        backBtn.addEventListener('click', () => {
            detailView.classList.add('hidden');
            tableView.classList.remove('hidden');
            desigPagination.loadPage(desigPagination.currentPage); 
        });
    }

    if (editBtn) {
        editBtn.addEventListener('click', () => {
            viewContainer.querySelectorAll('.detail-text').forEach(el => el.classList.add('hidden'));
            viewContainer.querySelectorAll('.detail-input').forEach(el => el.classList.remove('hidden'));
            
            viewContainer.querySelector('#edit-desigCode').value = viewContainer.querySelector('#view-desigCode').textContent;
            viewContainer.querySelector('#edit-desigTitle').value = viewContainer.querySelector('#view-desigTitle').textContent;
            
            const levelText = viewContainer.querySelector('#view-hierarchyLevel').textContent;
            viewContainer.querySelector('#edit-hierarchyLevel').value = levelText !== 'N/A' ? levelText : '';

            const deptText = viewContainer.querySelector('#view-deptId').textContent;
            viewContainer.querySelector('#edit-deptId').value = deptText !== 'None' ? deptText : '';
            
            const descText = viewContainer.querySelector('#view-desigDescription').textContent;
            viewContainer.querySelector('#edit-desigDescription').value = descText !== 'No description provided.' ? descText : '';

            editBtn.classList.add('hidden');
            saveBtn.classList.remove('hidden');
            cancelEditBtn.classList.remove('hidden');
        });
    }

    if (cancelEditBtn) {
        cancelEditBtn.addEventListener('click', () => resetDesigEditMode());
    }

    if (saveBtn) {
        saveBtn.addEventListener('click', async () => {
            const payload = {
                designationCode: viewContainer.querySelector('#edit-desigCode').value,
                designationTitle: viewContainer.querySelector('#edit-desigTitle').value,
                description: viewContainer.querySelector('#edit-desigDescription').value,
                hierarchyLevel: viewContainer.querySelector('#edit-hierarchyLevel').value || null,
                departmentId: viewContainer.querySelector('#edit-deptId').value || null
            };

            showLoader();
            try {
                await apiPut(`/api/designations/${currentDetailDesigId}`, payload);
                showSuccessMessage('Designation updated successfully.');
                openDesigDetail(currentDetailDesigId);
            } catch (e) {
                showErrorMessage('Failed to update designation.');
            } finally {
                hideLoader();
            }
        });
    }

    function resetDesigEditMode() {
        viewContainer.querySelectorAll('.detail-text').forEach(el => el.classList.remove('hidden'));
        viewContainer.querySelectorAll('.detail-input').forEach(el => el.classList.add('hidden'));
        if (editBtn) editBtn.classList.remove('hidden');
        if (saveBtn) saveBtn.classList.add('hidden');
        if (cancelEditBtn) cancelEditBtn.classList.add('hidden');
    }

    desigPagination.loadPage(0);
}

function initAddDesignationView() {
    const viewContainer = document.querySelector('#admin-add-designation-view');
    if (!viewContainer) return;

    const oldForm = viewContainer.querySelector('#add-desig-form');
    let form = oldForm;
    if (oldForm) {
        form = oldForm.cloneNode(true);
        oldForm.parentNode.replaceChild(form, oldForm);
        form.reset();
    }

    const backBtn = viewContainer.querySelector('#backToDesignationsBtn');
    if (backBtn) {
        const newBackBtn = backBtn.cloneNode(true);
        backBtn.parentNode.replaceChild(newBackBtn, backBtn);
        newBackBtn.addEventListener('click', () => {
            const mainContent = document.getElementById('main-content-area');
            window.history.pushState({ view: 'designations', title: 'Designations' }, "", "/admin/designations");
            const pageTitleElement = document.getElementById('pageTitle');
            if (pageTitleElement) pageTitleElement.textContent = "Manage Designations";
            void loadView('admin', 'designations', mainContent); 
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
                designationCode: viewContainer.querySelector('#add-desigCode').value,
                designationTitle: viewContainer.querySelector('#add-desigTitle').value,
                description: viewContainer.querySelector('#add-desigDescription').value,
                hierarchyLevel: viewContainer.querySelector('#add-hierarchyLevel').value || null,
                departmentId: viewContainer.querySelector('#add-deptId').value || null
            };

            showLoader();
            try {
                await apiPost('/api/designations', payload);
                showSuccessMessage('Designation created successfully!');
                form.reset();
            } catch (err) {
                showErrorMessage('Failed to create designation.');
            } finally {
                hideLoader();
                submitBtn.disabled = false;
                submitBtn.innerHTML = originalText;
            }
        });
    }
}
```

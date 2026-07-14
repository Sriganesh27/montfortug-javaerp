/* global apiGet, apiPost, apiPut, apiDelete, showLoader, hideLoader, showPremiumModal, showSuccessMessage, showErrorMessage, CrudTable, loadView */

document.addEventListener('viewLoaded', function(e) {
    if (e.detail.role === 'admin') {
        if (e.detail.view === 'departments') {
            initDepartmentsView();
        } else if (e.detail.view === 'add-department') {
            initAddDepartmentView();
        }
    }
});

let currentDetailDeptId = null;

function initDepartmentsView() {
    const viewContainer = document.querySelector('#ba-departments-view');
    if (!viewContainer) return;

    // --- SPA NAVIGATION ROUTING FOR "ADD DEPARTMENT" ---
    const addDeptBtn = viewContainer.querySelector('#btn-add-department');
    if (addDeptBtn) {
        addDeptBtn.addEventListener('click', () => {
            const mainContent = document.getElementById('main-content-area');
            window.history.pushState({ view: 'add-department', title: 'Add Department' }, "", "/admin/add-department");
            const pageTitleElement = document.getElementById('pageTitle');
            if (pageTitleElement) pageTitleElement.textContent = "Add Department";
            void loadView('admin', 'add-department', mainContent);
        });
    }

    const tableView = viewContainer.querySelector('#dept-tableView');
    const detailView = viewContainer.querySelector('#dept-detailView');

    // State for pagination and sorting
    const state = { page: 0, size: 10, sort: 'departmentId,asc' };
    let table;

    async function loadDepartments() {
        if (table) table.showLoading();
        try {
            const branchId = parseInt(localStorage.getItem('user_branch')) || 1;

            // Build query params
            const searchVal = viewContainer.querySelector('#dept-searchInput').value.trim();
            const statusVal = viewContainer.querySelector('#dept-statusFilter').value;

            let url = `/departments?branchId=${branchId}&page=${state.page}&size=${state.size}&sort=${state.sort}`;
            if (searchVal) url += `&keyword=${encodeURIComponent(searchVal)}`;
            if (statusVal) url += `&status=${statusVal}`;

            const res = await apiGet(url);
            const pageData = res.data;

            // Render Rows
            table.render(pageData.content, (dept, rowNode) => {
                rowNode.querySelector('.col-id').textContent = dept.departmentId;
                rowNode.querySelector('.col-code').textContent = dept.departmentCode;

                const nameNode = rowNode.querySelector('.col-name strong');
                nameNode.textContent = dept.departmentName;
                rowNode.querySelector('.col-name').addEventListener('click', () => openDeptDetail(dept.departmentId));

                rowNode.querySelector('.col-type').textContent = dept.isAcademic ? 'Academic' : 'Non-Academic';

                const statusBadge = rowNode.querySelector('.status-badge');
                statusBadge.textContent = dept.recordStatus || dept.status || 'ACTIVE';
                statusBadge.className = `status-badge badge-${(dept.recordStatus || dept.status || 'ACTIVE').toLowerCase()}`;

                rowNode.querySelector('.view-more-btn').addEventListener('click', () => openDeptDetail(dept.departmentId));
                const editBtn = rowNode.querySelector('.edit-btn');
                if (editBtn) editBtn.addEventListener('click', () => openDeptDetail(dept.departmentId, true));
                rowNode.querySelector('.delete-btn').addEventListener('click', () => deleteDept(dept.departmentId));

                return rowNode;
            });

            // Render Pagination
            table.renderPagination(pageData.pageNumber, pageData.totalPages, pageData.totalElements);

        } catch (e) {
            console.error(e);
            showErrorMessage("Failed to load departments.");
            table.render([]); // render empty state
        }
    }

    table = new CrudTable(
        {
            tbody: document.getElementById('dept-tableBody'),
            pageSize: document.getElementById('dept-pageSize'),
            pageInfo: document.getElementById('dept-pageInfo'),
            btnPrev: document.getElementById('btn-dept-prev'),
            btnNext: document.getElementById('btn-dept-next'),

            // USE GLOBAL TEMPLATES HERE
            tplLoading: document.getElementById('global-table-fetching-template'),
            tplEmpty: document.getElementById('global-table-empty-template'),

            tplRow: document.getElementById('tpl-dept-row'),
            table: document.getElementById('dept-tableComponent')
        },
        {
            onPageChange: (dir) => { state.page += dir; loadDepartments(); },
            onSizeChange: (size) => { state.size = size; state.page = 0; loadDepartments(); },
            onSort: (field) => { state.sort = field; loadDepartments(); }
        }
    );

    // Bind Search Filters
    const searchBtn = viewContainer.querySelector('#dept-searchBtn');
    if (searchBtn) {
        searchBtn.addEventListener('click', () => {
            state.page = 0;
            loadDepartments();
        });
    }

    async function openDeptDetail(id, isEditMode = false) {
        currentDetailDeptId = id;
        showLoader();
        try {
            const res = await apiGet(`/departments/${id}`);
            const dept = res.data;

            viewContainer.querySelector('#detail-deptNameHeader').textContent = dept.departmentName;
            viewContainer.querySelector('#view-deptCode').textContent = dept.departmentCode;
            viewContainer.querySelector('#view-deptName').textContent = dept.departmentName;
            viewContainer.querySelector('#view-isAcademic').textContent = dept.isAcademic ? 'Academic' : 'Non-Academic';
            viewContainer.querySelector('#view-deptDescription').textContent = dept.description || 'No description provided.';

            tableView.classList.add('hidden');
            detailView.classList.remove('hidden');
            resetDeptEditMode();
        } catch (e) {
            showErrorMessage("Failed to load department details.");
        } finally {
            hideLoader();
        }
    }

    async function deleteDept(id) {
        showPremiumModal({
            title: 'Delete Department',
            type: 'warning',
            contentText: 'Are you sure you want to delete this department? This is a soft-delete operation.',
            confirmText: 'Yes, Delete',
            cancelText: 'Cancel',
            onConfirm: async (modal) => {
                modal.close();
                showLoader();
                try {
                    await apiDelete(`/departments/${id}`);
                    showSuccessMessage('Department deleted successfully.');
                    loadDepartments();
                } catch (e) {
                    showErrorMessage('Failed to delete department.');
                } finally {
                    hideLoader();
                }
            }
        });
    }

    const editBtn = viewContainer.querySelector('#dept-editBtn');
    const saveBtn = viewContainer.querySelector('#dept-saveBtn');
    const cancelEditBtn = viewContainer.querySelector('#dept-cancelEditBtn');
    const backBtn = viewContainer.querySelector('#dept-backToTableBtn');

    if (backBtn) {
        backBtn.addEventListener('click', () => {
            detailView.classList.add('hidden');
            tableView.classList.remove('hidden');
            loadDepartments();
        });
    }

    if (editBtn) {
        editBtn.addEventListener('click', () => {
            viewContainer.querySelectorAll('.detail-text').forEach(el => el.classList.add('hidden'));
            viewContainer.querySelectorAll('.detail-input').forEach(el => el.classList.remove('hidden'));

            viewContainer.querySelector('#edit-deptCode').value = viewContainer.querySelector('#view-deptCode').textContent;
            viewContainer.querySelector('#edit-deptName').value = viewContainer.querySelector('#view-deptName').textContent;
            viewContainer.querySelector('#edit-isAcademic').value = viewContainer.querySelector('#view-isAcademic').textContent === 'Academic' ? 'true' : 'false';

            const descText = viewContainer.querySelector('#view-deptDescription').textContent;
            viewContainer.querySelector('#edit-deptDescription').value = descText !== 'No description provided.' ? descText : '';

            editBtn.classList.add('hidden');
            saveBtn.classList.remove('hidden');
            cancelEditBtn.classList.remove('hidden');
        });
    }

    if (cancelEditBtn) {
        cancelEditBtn.addEventListener('click', () => resetDeptEditMode());
    }

    if (saveBtn) {
        saveBtn.addEventListener('click', async () => {
            const payload = {
                branchId: parseInt(localStorage.getItem('user_branch')) || 1,
                departmentCode: viewContainer.querySelector('#edit-deptCode').value,
                departmentName: viewContainer.querySelector('#edit-deptName').value,
                isAcademic: viewContainer.querySelector('#edit-isAcademic').value === 'true',
                description: viewContainer.querySelector('#edit-deptDescription').value
            };

            showLoader();
            try {
                await apiPut(`/departments/${currentDetailDeptId}`, payload);
                showSuccessMessage('Department updated successfully.');
                openDeptDetail(currentDetailDeptId);
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
    loadDepartments();
}

function initAddDepartmentView() {
    const viewContainer = document.querySelector('#ba-add-dept-view');
    if (!viewContainer) return;

    const oldForm = viewContainer.querySelector('#add-dept-form');
    let form = oldForm;
    if (oldForm) {
        form = oldForm.cloneNode(true);
        oldForm.parentNode.replaceChild(form, oldForm);
        form.reset();
    }

    // --- SPA NAVIGATION ROUTING FOR "BACK" BUTTON ---
    const backBtn = viewContainer.querySelector('#backToDepartmentsBtn');
    if (backBtn) {
        const newBackBtn = backBtn.cloneNode(true);
        backBtn.parentNode.replaceChild(newBackBtn, backBtn);
        newBackBtn.addEventListener('click', () => {
            const mainContent = document.getElementById('main-content-area');
            window.history.pushState({ view: 'departments', title: 'Departments' }, "", "/admin/departments");
            const pageTitleElement = document.getElementById('pageTitle');
            if (pageTitleElement) pageTitleElement.textContent = "Manage Departments";
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
                branchId: parseInt(localStorage.getItem('user_branch')) || 1,
                departmentCode: viewContainer.querySelector('#add-deptCode').value,
                departmentName: viewContainer.querySelector('#add-deptName').value,
                isAcademic: viewContainer.querySelector('#add-isAcademic').value === 'true',
                description: viewContainer.querySelector('#add-deptDescription').value
            };

            showLoader();
            try {
                await apiPost('/departments', payload);
                showSuccessMessage('Department created successfully!');
                form.reset();
            } catch (err) {
                showErrorMessage(err.message || 'Failed to create department.');
            } finally {
                hideLoader();
                submitBtn.disabled = false;
                submitBtn.innerHTML = originalText;
            }
        });
    }
}


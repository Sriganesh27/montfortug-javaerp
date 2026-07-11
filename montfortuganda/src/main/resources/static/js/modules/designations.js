/* global apiGet, apiPost, apiPut, apiDelete, showLoader, hideLoader, showPremiumModal, showSuccessMessage, showErrorMessage, CrudTable, loadView */

document.addEventListener('viewLoaded', function(e) {
    if (e.detail.role === 'admin') {
        if (e.detail.view === 'designations') {
            initDesignationsView();
        } else if (e.detail.view === 'add-designation') {
            initAddDesignationView();
        }
    }
});

let currentDetailDesigId = null;

function initDesignationsView() {
    const viewContainer = document.querySelector('#ba-designations-view');
    if (!viewContainer) return;

    // --- SPA NAVIGATION ROUTING FOR "ADD DESIGNATION" ---
    const addDesigBtn = viewContainer.querySelector('#btn-add-designation');
    if (addDesigBtn) {
        addDesigBtn.addEventListener('click', () => {
            const mainContent = document.getElementById('main-content-area');
            window.history.pushState({ view: 'add-designation', title: 'Add Designation' }, "", "/admin/add-designation");
            const pageTitleElement = document.getElementById('pageTitle');
            if (pageTitleElement) pageTitleElement.textContent = "Add Designation";
            void loadView('admin', 'add-designation', mainContent);
        });
    }

    const tableView = viewContainer.querySelector('#desig-tableView');
    const detailView = viewContainer.querySelector('#desig-detailView');

    // State for pagination and sorting
    const state = { page: 0, size: 10, sort: 'id,desc' };
    let table;

    async function loadDesignations() {
        if (table) table.showLoading();
        try {
            const searchVal = viewContainer.querySelector('#desig-searchInput').value.trim();
            const statusVal = viewContainer.querySelector('#desig-statusFilter').value;

            let url = `/designations?page=${state.page}&size=${state.size}&sort=${state.sort}`;
            if (searchVal) url += `&keyword=${encodeURIComponent(searchVal)}`;
            if (statusVal) url += `&status=${statusVal}`;

            const res = await apiGet(url);
            const pageData = res.data;

            table.render(pageData.content, (desig, rowNode) => {
                const desigId = desig.id || desig.designationId;

                rowNode.querySelector('.col-id').textContent = desigId;
                rowNode.querySelector('.col-code').textContent = desig.designationCode;

                const nameNode = rowNode.querySelector('.col-name strong');
                nameNode.textContent = desig.designationName;
                rowNode.querySelector('.col-name').addEventListener('click', () => openDesigDetail(desigId));

                const statusBadge = rowNode.querySelector('.status-badge');
                statusBadge.textContent = desig.recordStatus || desig.status || 'ACTIVE';
                statusBadge.className = `status-badge badge-${(desig.recordStatus || desig.status || 'ACTIVE').toLowerCase()}`;

                rowNode.querySelector('.view-more-btn').addEventListener('click', () => openDesigDetail(desigId));
                rowNode.querySelector('.delete-btn').addEventListener('click', () => deleteDesig(desigId));

                return rowNode;
            });

            table.renderPagination(pageData.pageNumber, pageData.totalPages, pageData.totalElements);

        } catch (e) {
            console.error(e);
            showErrorMessage("Failed to load designations.");
            table.render([]);
        }
    }

    // Initialize CrudTable Framework
    table = new CrudTable(
        {
            tbody: document.getElementById('desig-tableBody'),
            pageSize: document.getElementById('desig-pageSize'),
            pageInfo: document.getElementById('desig-pageInfo'),
            btnPrev: document.getElementById('btn-desig-prev'),
            btnNext: document.getElementById('btn-desig-next'),

            // USE GLOBAL TEMPLATES HERE
            tplLoading: document.getElementById('global-table-fetching-template'),
            tplEmpty: document.getElementById('global-table-empty-template'),

            tplRow: document.getElementById('tpl-desig-row'),
            table: document.getElementById('desig-tableComponent')
        },
        {
            onPageChange: (dir) => { state.page += dir; loadDesignations(); },
            onSizeChange: (size) => { state.size = size; state.page = 0; loadDesignations(); },
            onSort: (field) => { state.sort = field; loadDesignations(); }
        }
    );

    const searchBtn = viewContainer.querySelector('#desig-searchBtn');
    if (searchBtn) {
        searchBtn.addEventListener('click', () => {
            state.page = 0;
            loadDesignations();
        });
    }

    async function openDesigDetail(id) {
        currentDetailDesigId = id;
        showLoader();
        try {
            const res = await apiGet(`/designations/${id}`);
            const desig = res.data;

            viewContainer.querySelector('#detail-desigNameHeader').textContent = desig.designationName;
            viewContainer.querySelector('#view-desigCode').textContent = desig.designationCode;
            viewContainer.querySelector('#view-desigName').textContent = desig.designationName;
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
                    await apiDelete(`/designations/${id}`);
                    showSuccessMessage('Designation deleted successfully.');
                    loadDesignations();
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
            loadDesignations();
        });
    }

    if (editBtn) {
        editBtn.addEventListener('click', () => {
            viewContainer.querySelectorAll('.detail-text').forEach(el => el.classList.add('hidden'));
            viewContainer.querySelectorAll('.detail-input').forEach(el => el.classList.remove('hidden'));

            viewContainer.querySelector('#edit-desigCode').value = viewContainer.querySelector('#view-desigCode').textContent;
            viewContainer.querySelector('#edit-desigName').value = viewContainer.querySelector('#view-desigName').textContent;

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
                designationName: viewContainer.querySelector('#edit-desigName').value,
                description: viewContainer.querySelector('#edit-desigDescription').value
            };

            showLoader();
            try {
                await apiPut(`/designations/${currentDetailDesigId}`, payload);
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

    // Initial load
    loadDesignations();
}

function initAddDesignationView() {
    const viewContainer = document.querySelector('#ba-add-desig-view');
    if (!viewContainer) return;

    const oldForm = viewContainer.querySelector('#add-desig-form');
    let form = oldForm;
    if (oldForm) {
        form = oldForm.cloneNode(true);
        oldForm.parentNode.replaceChild(form, oldForm);
        form.reset();
    }

    // --- SPA NAVIGATION ROUTING FOR "BACK" BUTTON ---
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
                designationName: viewContainer.querySelector('#add-desigName').value,
                description: viewContainer.querySelector('#add-desigDescription').value
            };

            showLoader();
            try {
                await apiPost('/designations', payload);
                showSuccessMessage('Designation created successfully!');
                form.reset();
            } catch (err) {
                showErrorMessage(err.message || 'Failed to create designation.');
            } finally {
                hideLoader();
                submitBtn.disabled = false;
                submitBtn.innerHTML = originalText;
            }
        });
    }
}
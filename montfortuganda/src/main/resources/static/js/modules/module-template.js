/* global AppApi, AppToast */
/**
 * Module Template Boilerplate
 * Complies with strict ERP JS rules: IIFE, DOMContentLoaded, No inline handlers.
 */
const ModuleBoilerplate = (function() {
    
    // --- DOM Elements ---
    let listView, detailView;
    let tableBody;
    let addBtn, backBtn;
    let form, detailTitle, saveBtn;
    
    // --- State ---
    let currentRecordId = null;

    // --- Initialization ---
    function init() {
        // Cache DOM elements
        listView = document.getElementById('ba-module-list-view');
        detailView = document.getElementById('ba-module-detail-view');
        tableBody = document.getElementById('module-table-body');
        
        addBtn = document.getElementById('btn-add-record');
        backBtn = document.getElementById('btn-back-to-list');
        form = document.getElementById('module-form');
        detailTitle = document.getElementById('module-detail-title');
        saveBtn = document.getElementById('btn-save-record');

        // Attach Event Listeners
        addBtn.addEventListener('click', showAddView);
        backBtn.addEventListener('click', showListView);
        form.addEventListener('submit', handleFormSubmit);

        // Event Delegation for Table Actions
        tableBody.addEventListener('click', handleTableClick);

        // Load Initial Data
        loadData();
    }

    // --- View Routing ---
    function showListView() {
        detailView.classList.add('hidden');
        listView.classList.remove('hidden');
        // Push state to browser history
        window.history.pushState({ view: 'module-list' }, '', '/admin/module');
    }

    function showAddView() {
        currentRecordId = null;
        form.reset();
        detailTitle.textContent = "Add New Record";
        listView.classList.add('hidden');
        detailView.classList.remove('hidden');
        window.history.pushState({ view: 'module-add' }, '', '/admin/module/add');
    }
    
    function showEditView(record) {
        currentRecordId = record.id;
        form.reset();
        
        // Populate form
        document.getElementById('record-name').value = record.name || '';
        document.getElementById('record-status').value = record.active ? "true" : "false";
        
        detailTitle.textContent = "Edit Record";
        listView.classList.add('hidden');
        detailView.classList.remove('hidden');
        window.history.pushState({ view: 'module-edit', id: record.id }, '', `/admin/module/${record.id}`);
    }

    // --- Data Operations (Async/Await) ---
    async function loadData() {
        try {
            tableBody.innerHTML = '<tr><td colspan="4" class="text-center">Loading...</td></tr>';
            // Example GET using global api wrapper
            const response = await apiGet('/v1/module');
            
            // Assuming response contains a data array
            const records = response.data || [];
            
            if (records.length === 0) {
                tableBody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">No records found.</td></tr>';
                return;
            }
            
            renderTable(records);
        } catch (error) {
            console.error("Failed to load records", error);
            AppToast.error("Failed to load records: " + error.message);
            tableBody.innerHTML = '<tr><td colspan="4" class="text-center text-danger">Error loading data.</td></tr>';
        }
    }

    function renderTable(records) {
        tableBody.innerHTML = '';
        records.forEach(r => {
            const tr = document.createElement('tr');
            
            const statusBadge = r.active 
                ? '<span class="badge bg-success">Active</span>' 
                : '<span class="badge bg-secondary">Inactive</span>';

            tr.innerHTML = `
                <td>${r.id}</td>
                <td class="text-strong">${r.name}</td>
                <td>${statusBadge}</td>
                <td>
                    <button type="button" class="btn-icon btn-edit" data-id="${r.id}" title="Edit">
                        <i class="bi bi-pencil-square"></i>
                    </button>
                    <button type="button" class="btn-icon btn-delete text-danger" data-id="${r.id}" title="Delete">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            `;
            tableBody.appendChild(tr);
        });
    }

    // --- Event Handlers ---
    async function handleFormSubmit(e) {
        e.preventDefault();
        saveBtn.disabled = true;

        const payload = {
            name: document.getElementById('record-name').value,
            active: document.getElementById('record-status').value === 'true'
        };

        try {
            if (currentRecordId) {
                await apiPut(`/v1/module/${currentRecordId}`, payload);
                AppToast.success("Record updated successfully!");
            } else {
                await apiPost(`/v1/module`, payload);
                AppToast.success("Record created successfully!");
            }
            
            showListView();
            await loadData(); // Reload table
        } catch (error) {
            console.error("Save failed", error);
            AppToast.error("Failed to save: " + error.message);
        } finally {
            saveBtn.disabled = false;
        }
    }

    function handleTableClick(e) {
        const editBtn = e.target.closest('.btn-edit');
        const deleteBtn = e.target.closest('.btn-delete');

        if (editBtn) {
            const id = editBtn.dataset.id;
            // Fetch single record details before editing
            fetchRecordAndEdit(id);
        } else if (deleteBtn) {
            const id = deleteBtn.dataset.id;
            if (confirm(`Are you sure you want to delete record #${id}?`)) {
                deleteRecord(id);
            }
        }
    }
    
    async function fetchRecordAndEdit(id) {
        try {
            const record = await apiGet(`/v1/module/${id}`);
            showEditView(record.data || record);
        } catch (err) {
            AppToast.error("Could not load record details.");
        }
    }

    async function deleteRecord(id) {
        try {
            await apiDelete(`/v1/module/${id}`);
            AppToast.success("Record deleted.");
            loadData();
        } catch (err) {
            AppToast.error("Deletion failed: " + err.message);
        }
    }

    // --- Bootstrapper ---
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    // Export public methods if necessary
    return {
        reload: loadData
    };

})();

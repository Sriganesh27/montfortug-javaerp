<div id="ba-departments-view">
    <!-- Header -->
    <div class="view-header">
        <div>
            <h2>Manage Departments</h2>
            <p class="text-muted">View and manage branch departments</p>
        </div>
    </div>

    <!-- MAIN TABLE VIEW -->
    <div id="dept-tableView">
        <!-- Filter Card -->
        <div class="card mb-3 p-3">
            <div class="ba-flex-panel">
                <div class="form-group ba-flex-1">
                    <label>Search Departments</label>
                    <input type="text" id="dept-searchInput" class="detail-input w-100" placeholder="Search by name or code...">
                </div>
                <div class="form-group ba-flex-1">
                    <label>Status</label>
                    <select id="dept-statusFilter" class="detail-input w-100">
                        <option value="">All Statuses</option>
                        <option value="ACTIVE">Active</option>
                        <option value="INACTIVE">Inactive</option>
                    </select>
                </div>
                <div class="form-group ba-pagination-controls">
                    <button id="dept-searchBtn" class="btn-primary"><i class="bi bi-search"></i> Search</button>
                </div>
            </div>
        </div>

        <!-- Table Card -->
        <div class="card">
            <div class="table-responsive">
                <table id="dept-tableComponent" class="data-table">
                    <thead>
                        <tr>
                            <th data-sort="departmentId">ID <i class="bi bi-arrow-down-up"></i></th>
                            <th data-sort="departmentCode">Code <i class="bi bi-arrow-down-up"></i></th>
                            <th data-sort="departmentName">Name <i class="bi bi-arrow-down-up"></i></th>
                            <th>Type</th>
                            <th>Status</th>
                            <th class="col-action align-center">Actions</th>
                        </tr>
                    </thead>
                    <tbody id="dept-tableBody">
                        <!-- Rows rendered via CrudTable -->
                    </tbody>
                </table>
            </div>
            
            <!-- CrudTable Pagination Footer -->
            <div class="ba-pagination-footer">
                <div class="ba-pagination-controls">
                    <select id="dept-pageSize" class="detail-input w-auto">
                        <option value="5">5</option>
                        <option value="10" selected>10</option>
                        <option value="25">25</option>
                        <option value="50">50</option>
                    </select>
                    <span id="dept-pageInfo" class="text-muted">Showing page 1 of 1</span>
                </div>
                <div class="ba-pagination-controls">
                    <button id="btn-dept-prev" class="btn-secondary btn-sm" disabled>
                        <i class="bi bi-chevron-left"></i> Prev
                    </button>
                    <button id="btn-dept-next" class="btn-secondary btn-sm" disabled>
                        Next <i class="bi bi-chevron-right"></i>
                    </button>
                </div>
            </div>
        </div>
    </div>

    <!-- DETAIL VIEW -->
    <div id="dept-detailView" class="hidden">
        <div class="view-header">
            <h2>Department: <span id="detail-deptNameHeader"></span></h2>
            <div class="action-btn-group">
                <button id="dept-backToTableBtn" class="btn-secondary"><i class="bi bi-arrow-left"></i> Back</button>
                <button id="dept-editBtn" class="btn-primary"><i class="bi bi-pencil"></i> Edit Details</button>
                <button id="dept-cancelEditBtn" class="btn-secondary hidden">Cancel</button>
                <button id="dept-saveBtn" class="btn-success hidden"><i class="bi bi-save"></i> Save Changes</button>
            </div>
        </div>
        <div class="card">
            <div class="detail-grid p-4" style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px;">
                <!-- Code -->
                <div class="form-group">
                    <label>Department Code</label>
                    <span id="view-deptCode" class="detail-text text-strong"></span>
                    <input type="text" id="edit-deptCode" class="detail-input hidden w-100">
                </div>
                <!-- Name -->
                <div class="form-group">
                    <label>Department Name</label>
                    <span id="view-deptName" class="detail-text text-strong"></span>
                    <input type="text" id="edit-deptName" class="detail-input hidden w-100">
                </div>
                <!-- Is Academic -->
                <div class="form-group">
                    <label>Type</label>
                    <span id="view-isAcademic" class="detail-text text-strong"></span>
                    <select id="edit-isAcademic" class="detail-input hidden w-100">
                        <option value="true">Academic</option>
                        <option value="false">Non-Academic</option>
                    </select>
                </div>
                <!-- Description -->
                <div class="form-group" style="grid-column: span 2;">
                    <label>Description</label>
                    <div id="view-deptDescription" class="detail-text p-3 bg-gray-50 rounded border"></div>
                    <textarea id="edit-deptDescription" class="detail-input hidden w-100" rows="3"></textarea>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- TEMPLATES FOR CRUD-TABLE -->
<!-- IMPORTANT: Do not delete these when copying! -->
<template id="tpl-dept-loading">
    <tr>
        <td colspan="6" class="text-center py-4">
            <div class="spinner-border text-primary spinner-border-sm me-2"></div> Loading departments...
        </td>
    </tr>
</template>

<template id="tpl-dept-empty">
    <tr>
        <td colspan="6" class="text-center py-4 text-muted">
            <i class="bi bi-inbox fs-4 d-block mb-2"></i> No departments found.
        </td>
    </tr>
</template>

<template id="tpl-dept-row">
    <tr>
        <td class="col-id font-monospace text-muted"></td>
        <td class="col-code font-medium"></td>
        <td class="col-name text-primary cursor-pointer"><strong></strong></td>
        <td class="col-type"></td>
        <td><span class="status-badge badge"></span></td>
        <td class="col-action align-center">
            <button class="btn-primary btn-sm view-more-btn me-1" title="View Details"><i class="bi bi-eye"></i></button>
            <button class="btn-danger btn-sm delete-btn" title="Delete"><i class="bi bi-trash"></i></button>
        </td>
    </tr>
</template>

const DepartmentView = {
    init: () => ({
        // Main Component Wrappers
        tableView: document.getElementById('ba-deptTableComponent'),
        formView: document.getElementById('ba-deptFormComponent'),
        tbody: document.getElementById('ba-deptTableBody'),
        table: document.getElementById('ba-deptTable'),

        // Search & Filter Controls (Updated to match ba- HTML IDs)
        searchKeyword: document.getElementById('ba-searchKeyword'),
        searchType: document.getElementById('ba-searchType'), // Ensure this exists in HTML if used
        searchStatus: document.getElementById('ba-searchStatus'), // Ensure this exists in HTML if used
        searchBtn: document.getElementById('ba-searchBtn'),
        resetBtn: document.getElementById('ba-resetSearchBtn'), // Ensure this exists in HTML if used

        // Pagination Controls (Updated to match ba- HTML IDs)
        pageSize: document.getElementById('ba-pageSize'),
        pageInfo: document.getElementById('ba-pageInfo'),
        btnPrev: document.getElementById('ba-prevPageBtn'),
        btnNext: document.getElementById('ba-nextPageBtn'),

        // Form Elements
        formFields: document.getElementById('ba-deptFormFields'),
        formOverlay: document.getElementById('ba-formOverlay'),
        fId: document.getElementById('edit-deptId'),
        fName: document.getElementById('edit-deptName'),
        fType: document.getElementById('edit-deptType'),
        fOrder: document.getElementById('edit-deptOrder'),
        fDesc: document.getElementById('edit-deptDesc'),
        headerName: document.getElementById('view-deptNameHeader'),

        // Templates
        tplLoading: document.getElementById('tpl-dept-loading'),
        tplEmpty: document.getElementById('tpl-dept-empty'),
        tplRow: document.getElementById('tpl-dept-row'),

        // Action Buttons
        addBtn: document.getElementById('ba-addDeptBtn'), // Ensure you have this in your HTML to open the form
        backBtn: document.getElementById('ba-backToTableBtn'),
        saveBtn: document.getElementById('ba-saveDeptBtn')
    })
};
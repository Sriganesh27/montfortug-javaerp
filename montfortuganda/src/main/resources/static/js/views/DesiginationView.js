const DesignationView = {
    init: () => {
        return {
            // Manage View
            tableView: document.getElementById('ba-designations-view'),
            tbody: document.getElementById('ba-desigTableBody'),
            table: document.getElementById('ba-desigTable'),

            // Search & Filter Controls
            searchKeyword: document.getElementById('ba-searchKeyword'),
            searchStatus: document.getElementById('ba-searchStatus'),
            searchBtn: document.getElementById('ba-searchBtn'),
            resetBtn: document.getElementById('ba-resetSearchBtn'),
            pageSize: document.getElementById('ba-pageSize'),
            pageInfo: document.getElementById('ba-pageInfo'),
            btnPrev: document.getElementById('ba-prevPageBtn'),
            btnNext: document.getElementById('ba-nextPageBtn'),

            // Form Elements
            formFields: document.getElementById('ba-desigFormFields'),
            formOverlay: document.getElementById('ba-formOverlay'),
            headerName: document.getElementById('view-desigNameHeader'),
            fId: document.getElementById('edit-desigId'),
            fName: document.getElementById('edit-desigName'),
            fOrder: document.getElementById('edit-desigOrder'),
            fDesc: document.getElementById('edit-desigDesc'),

            // Templates
            tplLoading: document.getElementById('tpl-desig-loading'),
            tplEmpty: document.getElementById('tpl-desig-empty'),
            tplRow: document.getElementById('tpl-desig-row'),

            // Action Buttons
            addBtn: document.getElementById('ba-addDesigBtn'),
            saveBtn: document.getElementById('ba-saveDesigBtn'),
            backBtn: document.getElementById('ba-backToTableBtn')
        }
    }
};

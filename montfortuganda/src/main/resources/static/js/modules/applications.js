const handleApplicationsLoad = (e) => {
    if (e.detail.view === 'applications') ApplicationsController.init();
};
document.removeEventListener('viewLoaded', handleApplicationsLoad);
document.addEventListener('viewLoaded', handleApplicationsLoad);

const ApplicationsController = (function() {
    let view, table;
    let state = { page: 0, size: 20, sort: 'submittedDate', direction: 'DESC' };

    function cacheDOM() {
        return {
            tableComponent: document.getElementById('ba-appTableComponent'),
            detailComponent: document.getElementById('ba-appDetailComponent'),
            searchKeyword: document.getElementById('ba-appSearchKeyword'),
            searchStatus: document.getElementById('ba-appSearchStatus'),
            searchBtn: document.getElementById('ba-appSearchBtn'),
            resetBtn: document.getElementById('ba-appResetBtn'),
            backBtn: document.getElementById('ba-backToAppTableBtn'),

            // Detail Fields
            viewAppNo: document.getElementById('view-appNoHeader'),
            viewName: document.getElementById('view-studentName'),
            viewClass: document.getElementById('view-className'),
            viewStatus: document.getElementById('view-appStatus'),
            viewDob: document.getElementById('view-dob')
        };
    }

    async function loadData() {
        table.showLoading();
        try {
            // Re-using the exact API endpoint from your branchadmin.js dashboard
            const url = `/admission/branch/applications?page=${state.page}&size=${state.size}&sortBy=${state.sort}&direction=${state.direction}&keyword=${view.searchKeyword.value.trim()}&status=${view.searchStatus.value}`;

            const res = await apiGet(url);
            table.render(res.data.content, renderRow);
            table.renderPagination(state.page, res.data.totalPages, res.data.totalElements);
        } catch (err) {
            Toast.error('Failed to load applications securely');
        }
    }

    function renderRow(record, node) {
        node.querySelector('.td-appno strong').textContent = record.applicationNo;
        node.querySelector('.td-name').textContent = record.studentName;
        node.querySelector('.td-class').textContent = record.className;
        node.querySelector('.td-date').textContent = new Date(record.submittedDate).toLocaleDateString();

        // Match standard ERP colors
        const badge = document.createElement('span');
        badge.className = `badge ${getErpBadgeClass(record.status)}`;
        badge.textContent = record.status;
        node.querySelector('.td-status').appendChild(badge);

        // Open Detail View
        node.querySelector('.btn-view').addEventListener('click', () => openDetailView(record));
        return node;
    }

    function openDetailView(record) {
        // Swap UI
        view.tableComponent.classList.add('hidden');
        view.detailComponent.classList.remove('hidden');

        // Populate fields
        view.viewAppNo.textContent = record.applicationNo;
        view.viewName.textContent = record.studentName;
        view.viewClass.textContent = record.className;
        view.viewDob.textContent = record.dateOfBirth ? new Date(record.dateOfBirth).toLocaleDateString() : 'N/A';

        view.viewStatus.className = `badge ${getErpBadgeClass(record.status)}`;
        view.viewStatus.textContent = record.status;
    }

    function closeDetailView() {
        view.detailComponent.classList.add('hidden');
        view.tableComponent.classList.remove('hidden');
    }

    function getErpBadgeClass(status) {
        switch(status) {
            case 'APPROVED': case 'ADMITTED': return 'bg-success';
            case 'REJECTED': return 'bg-danger';
            case 'SUBMITTED': case 'WAITLISTED': return 'bg-warning text-dark';
            case 'UNDER_REVIEW': return 'bg-primary';
            default: return 'bg-secondary';
        }
    }

    return {
        init: () => {
            view = cacheDOM();

            // Connect to our global CrudTable (Notice we pass dummy DOM IDs for table to let CrudTable auto-discover using view object)
            table = new CrudTable(
                {
                    tableBody: document.getElementById('ba-appTableBody'),
                    pageSize: document.getElementById('ba-appPageSize'),
                    pageInfo: document.getElementById('ba-appPageInfo'),
                    prevBtn: document.getElementById('ba-appPrevPageBtn'),
                    nextBtn: document.getElementById('ba-appNextPageBtn'),
                    loadingTemplate: 'tpl-desig-loading', // reuse loading
                    emptyTemplate: 'tpl-desig-empty',     // reuse empty
                    rowTemplate: 'tpl-app-row'
                },
                {
                    onPageChange: (dir) => { state.page += dir; loadData(); },
                    onSizeChange: (size) => { state.size = size; state.page = 0; loadData(); },
                    onSort: (field) => { state.sort = field; loadData(); }
                }
            );

            // Bind Events
            view.searchBtn.addEventListener('click', () => { state.page = 0; loadData(); });
            view.resetBtn.addEventListener('click', () => {
                view.searchKeyword.value = '';
                view.searchStatus.value = '';
                state.page = 0;
                loadData();
            });

            view.backBtn.addEventListener('click', closeDetailView);

            loadData();
        }
    };
})();
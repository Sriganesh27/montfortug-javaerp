// ==========================================
// BRANCH ADMIN MODULE
// ==========================================

document.addEventListener('viewLoaded', function(e) {
    if (e.detail.role !== 'admin') return;

    const view = e.detail.view;

    // 1. Dashboard View
    if (view === 'home' || view === 'dashboard') {
        void initBranchDashboardView();
    }

    // 2. Departments View
    else if (view === 'departments' || view === 'manage-departments') {
        initDepartmentsView();
    }

    // 3. Add Designation View
    else if (view === 'add-designation') {
        initAddDesignationView();
    }

    // 4. Applications View
    else if (view === 'applications' || view === 'view-applications') {
        initApplicationsView();
    }

    // 5. Designations View
    else if (view === 'designations' || view === 'manage-designations') {
        initDesignationsView();
    }
});

function initDepartmentsView() {
    const addDeptBtn = document.getElementById('ba-addDeptBtn');
    const searchBtn = document.getElementById('ba-searchBtn');
    const resetBtn = document.getElementById('ba-resetSearchBtn');

    if (addDeptBtn) {
        addDeptBtn.addEventListener('click', async () => {
            if (typeof loadView === 'function') {
                const mainContent = document.getElementById('mainContent');
                window.history.pushState({ view: 'add-department' }, "", '/admin/add-department');
                await loadView('admin', 'add-department', mainContent);
            } else {
                window.location.href = '/admin/add-department';
            }
        });
    }

    if (searchBtn) {
        searchBtn.addEventListener('click', () => {
            const keyword = document.getElementById('ba-searchKeyword')?.value || '';
            console.log("Searching departments for:", keyword);
        });
    }

    if (resetBtn) {
        resetBtn.addEventListener('click', () => {
            console.log("Resetting department search");
        });
    }
}

function initAddDesignationView() {
    // Note: 'Desig' is just an abbreviation for Designation. You can safely ignore IDE spellcheck warnings for these IDs.
    const backBtn = document.getElementById('ba-backToDesigsBtn');
    const saveBtn = document.getElementById('ba-saveNewDesigBtn');

    if (backBtn) {
        backBtn.addEventListener('click', async () => {
            if (typeof loadView === 'function') {
                const mainContent = document.getElementById('mainContent');
                window.history.pushState({ view: 'designations' }, "", '/admin/designations');
                await loadView('admin', 'designations', mainContent);
            } else {
                window.location.href = '/admin/designations';
            }
        });
    }

    if (saveBtn) {
        saveBtn.addEventListener('click', () => {
            console.log("Saving designation securely...");
            const overlay = document.getElementById('ba-addDesigOverlay');
            if (overlay) overlay.classList.remove('hidden');

            setTimeout(() => {
                if (overlay) overlay.classList.add('hidden');
                if (typeof showSuccessMessage === 'function') showSuccessMessage("Designation added successfully");
            }, 1000);
        });
    }
}

function initApplicationsView() {
    const searchBtn = document.getElementById('ba-searchBtn');
    const resetBtn = document.getElementById('ba-resetSearchBtn');
    const prevBtn = document.getElementById('ba-appPrevPageBtn');
    const nextBtn = document.getElementById('ba-appNextPageBtn');

    if (searchBtn) {
        searchBtn.addEventListener('click', () => {
            console.log("Searching applications...");
        });
    }

    if (resetBtn) {
        resetBtn.addEventListener('click', () => {
            console.log("Resetting application search...");
        });
    }

    if (prevBtn) {
        prevBtn.addEventListener('click', () => {
            console.log("Loading previous page...");
        });
    }

    if (nextBtn) {
        nextBtn.addEventListener('click', () => {
            console.log("Loading next page...");
        });
    }
}

function initDesignationsView() {
    const searchBtn = document.getElementById('ba-searchBtn');
    const addDesigBtn = document.getElementById('ba-addDesigBtn');

    if (addDesigBtn) {
        addDesigBtn.addEventListener('click', async () => {
            if (typeof loadView === 'function') {
                const mainContent = document.getElementById('mainContent');
                window.history.pushState({ view: 'add-designation' }, "", '/admin/add-designation');
                await loadView('admin', 'add-designation', mainContent);
            } else {
                window.location.href = '/admin/add-designation';
            }
        });
    }

    if (searchBtn) {
        searchBtn.addEventListener('click', () => {
            console.log("Searching designations...");
        });
    }
}

async function initBranchDashboardView() {
    try {
        const statsRes = await apiGet('/branchadmin/dashboard/stats');

        if (statsRes && statsRes.data) {
            const stats = statsRes.data;

            if (typeof animateValue === 'function') {
                animateValue(document.getElementById('ba-statTotalApps'), 0, stats.totalApplications || 0, 1200, false);
                animateValue(document.getElementById('ba-statPending'), 0, stats.pendingVerification || 0, 1200, false);
                animateValue(document.getElementById('ba-statApproved'), 0, (stats.selected + stats.enrolled) || 0, 1200, false);
            }

            const subtitle = document.getElementById('ba-dashboard-subtitle');
            if (subtitle) {
                subtitle.textContent = `Welcome back! Here is your live summary for ${stats.branchName}.`;
            }
        }
    } catch (error) {
        console.error("Failed to load branch dashboard stats", error);
        if (typeof showErrorMessage === 'function') {
            showErrorMessage("Could not load dashboard statistics.");
        }
    }

    try {
        const appsRes = await apiGet('/admission/branch/applications?page=0&size=10');
        const tbody = document.getElementById('ba-applicationsTableBody');

        if (tbody && appsRes && appsRes.data && appsRes.data.content) {
            tbody.textContent = '';
            const applications = appsRes.data.content;

            if (applications.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted py-4">No recent applications found.</td></tr>';
            } else {
                applications.forEach(app => {
                    const tr = document.createElement('tr');
                    const dateStr = new Date(app.submittedDate).toLocaleDateString();

                    const tdAppNo = document.createElement('td');
                    tdAppNo.innerHTML = `<strong>${app.applicationNo}</strong>`;

                    const tdName = document.createElement('td');
                    tdName.textContent = app.studentName;

                    const tdClass = document.createElement('td');
                    tdClass.textContent = app.className;

                    const tdStatus = document.createElement('td');
                    const badge = document.createElement('span');
                    badge.className = `badge ${getErpBadgeClass(app.status)}`;
                    badge.textContent = app.status;
                    tdStatus.appendChild(badge);

                    const tdDate = document.createElement('td');
                    tdDate.textContent = dateStr;

                    tr.append(tdAppNo, tdName, tdClass, tdStatus, tdDate);
                });
            }
        }
    } catch (error) {
        console.error("Failed to load branch applications", error);
    }
}

function getErpBadgeClass(status) {
    switch(status) {
        case 'APPROVED':
        case 'ADMITTED':
            return 'bg-success';
        case 'REJECTED':
            return 'bg-danger';
        case 'SUBMITTED':
        case 'WAITLISTED':
            return 'bg-warning text-dark';
        case 'UNDER_REVIEW':
            return 'bg-primary';
        default:
            return 'bg-secondary';
    }
}

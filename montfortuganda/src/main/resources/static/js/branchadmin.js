/* global apiGet, animateValue, showErrorMessage */

// ==========================================
// BRANCH ADMIN MODULE
// ==========================================

let branchDashboardRequestRunning = false;

document.addEventListener('viewLoaded', function (event) {
    if (!event.detail || event.detail.role !== 'admin') {
        return;
    }

    const view = event.detail.view;

    if (view === 'home' || view === 'dashboard') {
        void initBranchDashboardView();
        return;
    }

    if (
        view === 'applications' ||
        view === 'view-applications'
    ) {
        initApplicationsView();
    }
});

function initApplicationsView() {
    const searchBtn =
        document.getElementById('ba-searchBtn');

    const resetBtn =
        document.getElementById('ba-resetSearchBtn');

    const prevBtn =
        document.getElementById('ba-appPrevPageBtn');

    const nextBtn =
        document.getElementById('ba-appNextPageBtn');

    addSingleClickListener(
        searchBtn,
        'searchListenerAdded',
        () => {
            console.log('Searching applications...');
        }
    );

    addSingleClickListener(
        resetBtn,
        'resetListenerAdded',
        () => {
            console.log('Resetting application search...');
        }
    );

    addSingleClickListener(
        prevBtn,
        'previousListenerAdded',
        () => {
            console.log('Loading previous page...');
        }
    );

    addSingleClickListener(
        nextBtn,
        'nextListenerAdded',
        () => {
            console.log('Loading next page...');
        }
    );
}

function addSingleClickListener(
    element,
    datasetKey,
    listener
) {
    if (!element) {
        return;
    }

    if (element.dataset[datasetKey] === 'true') {
        return;
    }

    element.dataset[datasetKey] = 'true';

    element.addEventListener(
        'click',
        listener
    );
}

async function initBranchDashboardView() {
    if (branchDashboardRequestRunning) {
        console.warn(
            'Branch dashboard request is already running.'
        );

        return;
    }

    const dashboardElement =
        document.getElementById(
            'ba-dashboard-subtitle'
        );

    /*
     * This check prevents dashboard API calls when
     * the dashboard HTML is not currently displayed.
     */
    if (!dashboardElement) {
        return;
    }

    branchDashboardRequestRunning = true;

    try {
        await Promise.allSettled([
            loadBranchDashboardStats(),
            loadRecentBranchApplications()
        ]);
    } finally {
        branchDashboardRequestRunning = false;
    }
}

async function loadBranchDashboardStats() {
    try {
        const response =
            await apiGet(
                '/branchadmin/dashboard/stats'
            );

        const stats =
            response?.data;

        if (!stats) {
            throw new Error(
                'Dashboard statistics response is empty.'
            );
        }

        updateDashboardNumber(
            'ba-statTotalApps',
            stats.totalApplications
        );

        updateDashboardNumber(
            'ba-statPending',
            stats.pendingVerification
        );

        const totalApproved =
            Number(stats.selected || 0) +
            Number(stats.enrolled || 0);

        updateDashboardNumber(
            'ba-statApproved',
            totalApproved
        );

        const subtitle =
            document.getElementById(
                'ba-dashboard-subtitle'
            );

        if (subtitle) {
            const branchName =
                stats.branchName ||
                'your branch';

            subtitle.textContent =
                `Welcome back! Here is your live summary for ${branchName}.`;
        }
    } catch (error) {
        console.error(
            'Failed to load branch dashboard stats',
            error
        );

        if (
            typeof showErrorMessage ===
            'function'
        ) {
            showErrorMessage(
                'Could not load dashboard statistics.'
            );
        }
    }
}

function updateDashboardNumber(
    elementId,
    value
) {
    const element =
        document.getElementById(elementId);

    if (!element) {
        return;
    }

    const finalValue =
        Number(value || 0);

    if (typeof animateValue === 'function') {
        animateValue(
            element,
            0,
            finalValue,
            1200,
            false
        );

        return;
    }

    element.textContent =
        String(finalValue);
}

async function loadRecentBranchApplications() {
    const tbody =
        document.getElementById(
            'ba-applicationsTableBody'
        );

    if (!tbody) {
        return;
    }

    showApplicationsLoadingRow(tbody);

    try {
        const response =
            await apiGet(
                '/admission/branch/applications?page=0&size=10'
            );

        const applications =
            Array.isArray(
                response?.data?.content
            )
                ? response.data.content
                : [];

        renderBranchApplications(
            tbody,
            applications
        );
    } catch (error) {
        console.error(
            'Failed to load branch applications',
            error
        );

        showApplicationsErrorRow(tbody);
    }
}

function showApplicationsLoadingRow(tbody) {
    tbody.innerHTML = '';

    const row =
        document.createElement('tr');

    const cell =
        document.createElement('td');

    cell.colSpan = 5;
    cell.className =
        'text-center text-muted py-4';

    cell.textContent =
        'Loading recent applications...';

    row.appendChild(cell);
    tbody.appendChild(row);
}

function showApplicationsErrorRow(tbody) {
    tbody.innerHTML = '';

    const row =
        document.createElement('tr');

    const cell =
        document.createElement('td');

    cell.colSpan = 5;
    cell.className =
        'text-center text-danger py-4';

    cell.textContent =
        'Could not load recent applications.';

    row.appendChild(cell);
    tbody.appendChild(row);
}

function renderBranchApplications(
    tbody,
    applications
) {
    tbody.innerHTML = '';

    if (applications.length === 0) {
        const row =
            document.createElement('tr');

        const cell =
            document.createElement('td');

        cell.colSpan = 5;
        cell.className =
            'text-center text-muted py-4';

        cell.textContent =
            'No recent applications found.';

        row.appendChild(cell);
        tbody.appendChild(row);

        return;
    }

    applications.forEach(application => {
        const row =
            createApplicationRow(
                application
            );

        tbody.appendChild(row);
    });
}

function createApplicationRow(application) {
    const row =
        document.createElement('tr');

    const applicationNumberCell =
        document.createElement('td');

    const applicationNumber =
        document.createElement('strong');

    applicationNumber.textContent =
        application?.applicationNo ||
        '-';

    applicationNumberCell.appendChild(
        applicationNumber
    );

    const studentNameCell =
        document.createElement('td');

    studentNameCell.textContent =
        application?.studentName ||
        '-';

    const classNameCell =
        document.createElement('td');

    classNameCell.textContent =
        application?.className ||
        '-';

    const statusCell =
        document.createElement('td');

    const statusBadge =
        document.createElement('span');

    const status =
        application?.status ||
        'UNKNOWN';

    statusBadge.className =
        `badge ${getErpBadgeClass(status)}`;

    statusBadge.textContent =
        formatEnum(status);

    statusCell.appendChild(
        statusBadge
    );

    const submittedDateCell =
        document.createElement('td');

    submittedDateCell.textContent =
        formatApplicationDate(
            application?.submittedDate
        );

    row.append(
        applicationNumberCell,
        studentNameCell,
        classNameCell,
        statusCell,
        submittedDateCell
    );

    return row;
}

function formatApplicationDate(dateValue) {
    if (!dateValue) {
        return '-';
    }

    const date =
        new Date(dateValue);

    if (Number.isNaN(date.getTime())) {
        return '-';
    }

    return date.toLocaleDateString();
}

function formatEnum(value) {
    if (!value) {
        return '-';
    }

    return String(value)
        .replaceAll('_', ' ')
        .toLowerCase()
        .replace(
            /\b\w/g,
            character =>
                character.toUpperCase()
        );
}

function getErpBadgeClass(status) {
    switch (status) {
        case 'APPROVED':
        case 'ADMITTED':
        case 'ENROLLED':
        case 'SELECTED':
            return 'bg-success';

        case 'REJECTED':
        case 'CANCELLED':
            return 'bg-danger';

        case 'SUBMITTED':
        case 'WAITLISTED':
        case 'PENDING':
            return 'bg-warning text-dark';

        case 'UNDER_REVIEW':
        case 'VERIFIED':
            return 'bg-primary';

        default:
            return 'bg-secondary';
    }
}
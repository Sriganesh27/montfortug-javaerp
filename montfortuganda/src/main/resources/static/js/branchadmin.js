// ==========================================
// BRANCH ADMIN MODULE
// ==========================================

document.addEventListener('viewLoaded', function(e) {
    // When layout.js loads the admin home page
    if (e.detail.role === 'admin' && (e.detail.view === 'home' || e.detail.view === 'dashboard')) {
        void initBranchDashboardView();
    }
});

async function initBranchDashboardView() {

    // 1. Fetch Dashboard Stats
    try {
        const statsRes = await apiGet('/branchadmin/dashboard/stats');

        if (statsRes && statsRes.data) {
            const stats = statsRes.data;

            // Reusing your global 'animateValue' function from layout.js / superadmin.js
            animateValue(document.getElementById('ba-statTotalApps'), 0, stats.totalApplications || 0, 1200, false);
            animateValue(document.getElementById('ba-statPending'), 0, stats.pendingVerification || 0, 1200, false);
            animateValue(document.getElementById('ba-statApproved'), 0, (stats.selected + stats.enrolled) || 0, 1200, false);

            // Update the subtitle dynamically
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

    // 2. Fetch Paginated Data Table
    try {
        const appsRes = await apiGet('/admission/branch/applications?page=0&size=10');
        const tbody = document.getElementById('ba-applicationsTableBody');

        if (tbody && appsRes && appsRes.data && appsRes.data.content) {
            tbody.textContent = ''; // Clear loader securely
            const applications = appsRes.data.content;

            if (applications.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted py-4">No recent applications found.</td></tr>';
            } else {
                applications.forEach(app => {
                    const tr = document.createElement('tr');

                    // Format date generically
                    const dateStr = new Date(app.submittedDate).toLocaleDateString();

                    // Note: Using standard Bootstrap classes for badges (assuming you use BS5)
                    tr.innerHTML = `
                        <td><strong>${app.applicationNo}</strong></td>
                        <td>${app.studentName}</td>
                        <td>${app.className}</td>
                        <td><span class="badge ${getErpBadgeClass(app.status)}">${app.status}</span></td>
                        <td>${dateStr}</td>
                    `;
                    tbody.appendChild(tr);
                });
            }
        }
    } catch (error) {
        console.error("Failed to load branch applications", error);
    }
}

// Utility to match the standard colors of your ERP
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
// Global Listener for the SPA Router
document.addEventListener('viewLoaded', function(e) {
    if (e.detail.role === 'superadmin') {
        if (e.detail.view === 'branches') {
            initBranchesView();
        } else if (e.detail.view === 'home') {
            initHomeView();
        }
    }
});

// ---------------------------------------------------------
// 1. DASHBOARD HOME LOGIC
// ---------------------------------------------------------
function initHomeView() {
    const token = localStorage.getItem('jwt_token');
    if (!token) return;

    const authHeaders = {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
    };

    async function loadDashboardStats() {
        try {
            const branchRes = await fetch('/api/superadmin/branches', { headers: authHeaders });
            if(branchRes.ok) {
                const branches = await branchRes.json();
                document.getElementById('sa-statTotalBranches').textContent = branches.length;
            }

            const userRes = await fetch('/api/superadmin/users', { headers: authHeaders });
            if(userRes.ok) {
                const users = await userRes.json();
                document.getElementById('sa-statTotalUsers').textContent = users.length;
            }

            const settingRes = await fetch('/api/superadmin/settings', { headers: authHeaders });
            if(settingRes.ok) {
                const settings = await settingRes.json();
                document.getElementById('sa-statTotalSettings').textContent = settings.length;
            }
        } catch (error) {
            console.error("Failed to load dashboard stats", error);
        }
    }

    // Quick Links Navigation
    document.getElementById('sa-btnLinkBranches').addEventListener('click', () => {
        const link = document.querySelector('.sidebar-nav a[href*="branches"]');
        if(link) link.click();
    });

    document.getElementById('sa-btnLinkUsers').addEventListener('click', () => {
        const link = document.querySelector('.sidebar-nav a[href*="users"]');
        if(link) link.click();
    });

    loadDashboardStats();
}

// ---------------------------------------------------------
// 2. BRANCHES LOGIC
// ---------------------------------------------------------
function initBranchesView() {
    const token = localStorage.getItem('jwt_token');
    if (!token) return;

    const authHeaders = {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
    };

    const tableBody = document.getElementById('sa-branchesTableBody');
    const modal = document.getElementById('sa-branchModal');
    const form = document.getElementById('sa-branchForm');
    const addBranchBtn = document.getElementById('sa-addBranchBtn');
    const closeModalBtn = document.getElementById('sa-closeModalBtn');

    // Fetch & Display
    async function loadBranches() {
        try {
            const response = await fetch('/api/superadmin/branches', { headers: authHeaders });
            const branches = await response.json();

            tableBody.innerHTML = '';

            branches.forEach(branch => {
                const statusClass = branch.isActive === 1 ? 'badge-active' : 'badge-inactive';
                const statusText = branch.isActive === 1 ? 'Active' : 'Inactive';

                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${branch.branchId}</td>
                    <td><strong>${branch.branchName}</strong></td>
                    <td>${branch.schoolCode}</td>
                    <td>${branch.branchType}</td>
                    <td>${branch.branchLocation}</td>
                    <td><span class="${statusClass}">${statusText}</span></td>
                    <td>
                        <button class="action-btn edit-btn" data-id="${branch.branchId}"><i class="bi bi-pencil-square"></i></button>
                        <button class="action-btn delete-btn" data-id="${branch.branchId}"><i class="bi bi-trash"></i></button>
                    </td>
                `;
                tableBody.appendChild(tr);
            });
        } catch (error) {
            tableBody.innerHTML = '<tr><td colspan="7" class="text-center badge-inactive">Failed to load branches.</td></tr>';
        }
    }

    // Edit & Delete clicks
    tableBody.addEventListener('click', async function(e) {
        const editBtn = e.target.closest('.edit-btn');
        const deleteBtn = e.target.closest('.delete-btn');

        if (editBtn) {
            const id = parseInt(editBtn.getAttribute('data-id'));
            handleEdit(id);
        } else if (deleteBtn) {
            const id = parseInt(deleteBtn.getAttribute('data-id'));
            handleDelete(id);
        }
    });

    // Modal Controls
    addBranchBtn.addEventListener('click', () => {
        form.reset();
        document.getElementById('sa-branchId').value = '';
        document.getElementById('sa-modalTitle').textContent = 'Add New Branch';
        modal.classList.remove('hidden');
    });

    closeModalBtn.addEventListener('click', () => {
        modal.classList.add('hidden');
    });

    // Form Submit (CREATE & UPDATE)
    form.addEventListener('submit', async function(e) {
        e.preventDefault();

        const branchId = document.getElementById('sa-branchId').value;
        const payload = {
            branchName: document.getElementById('sa-branchName').value,
            schoolCode: document.getElementById('sa-schoolCode').value,
            branchType: document.getElementById('sa-branchType').value,
            branchLocation: document.getElementById('sa-branchLocation').value
        };

        const method = branchId ? 'PUT' : 'POST';
        const url = branchId ? `/api/superadmin/branches/${branchId}` : `/api/superadmin/branches`;

        try {
            const response = await fetch(url, {
                method: method,
                headers: authHeaders,
                body: JSON.stringify(payload)
            });

            if (response.ok) {
                modal.classList.add('hidden');
                loadBranches();
            }
        } catch (error) {
            console.error("Save error:", error);
        }
    });

    // Handlers
    async function handleEdit(id) {
        try {
            const response = await fetch('/api/superadmin/branches', { headers: authHeaders });
            const branches = await response.json();
            const branch = branches.find(b => b.branchId === id);

            if (branch) {
                document.getElementById('sa-branchId').value = branch.branchId;
                document.getElementById('sa-branchName').value = branch.branchName;
                document.getElementById('sa-schoolCode').value = branch.schoolCode;
                document.getElementById('sa-branchType').value = branch.branchType;
                document.getElementById('sa-branchLocation').value = branch.branchLocation;

                document.getElementById('sa-modalTitle').textContent = 'Edit Branch';
                modal.classList.remove('hidden');
            }
        } catch(error) {}
    }

    async function handleDelete(id) {
        if(confirm("Are you sure you want to deactivate this branch?")) {
            try {
                const response = await fetch(`/api/superadmin/branches/${id}`, {
                    method: 'DELETE',
                    headers: authHeaders
                });
                if(response.ok) loadBranches();
            } catch(error) {}
        }
    }

    loadBranches();
}
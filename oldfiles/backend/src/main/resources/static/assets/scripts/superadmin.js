function logout() {
    localStorage.clear();
    window.location.replace('/login');
}

// UI Toggles
let isSidebarOpen = true;
let currentTheme = 'light';
const FULLSCREEN_STORAGE_KEY = 'montfort_is_fullscreen';

function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const content = document.getElementById('content');
    const menuIcon = document.getElementById('menu-toggle').querySelector('i');
    isSidebarOpen = !isSidebarOpen;
    
    if(window.innerWidth <= 768) {
        sidebar.classList.toggle('open', isSidebarOpen);
        document.getElementById('overlay').classList.toggle('active', isSidebarOpen);
        menuIcon.className = isSidebarOpen ? 'bi bi-x-lg' : 'bi bi-list';
    } else {
        sidebar.classList.toggle('collapsed', !isSidebarOpen);
        content.classList.toggle('collapsed', !isSidebarOpen);
        menuIcon.className = isSidebarOpen ? 'bi bi-chevron-left' : 'bi bi-list';
    }
}

function toggleTheme() {
    document.body.classList.remove(currentTheme + '-theme');
    currentTheme = currentTheme === 'light' ? 'dark' : 'light';
    document.body.classList.add(currentTheme + '-theme');
}

function toggleFullscreen() {
    const fullscreenIcon = document.getElementById('fullscreen-btn').querySelector('i');
    if (!document.fullscreenElement) {
        document.documentElement.requestFullscreen();
        fullscreenIcon.className = 'bi bi-fullscreen-exit';
    } else if (document.exitFullscreen) {
        document.exitFullscreen();
        fullscreenIcon.className = 'bi bi-arrows-fullscreen';
    }
}

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('menu-toggle').addEventListener('click', toggleSidebar);
    document.getElementById('overlay').addEventListener('click', toggleSidebar);
    document.getElementById('fullscreen-btn').addEventListener('click', toggleFullscreen);
    
    const themeBtn = document.getElementById('settings-btn');
    if(themeBtn) themeBtn.addEventListener('click', toggleTheme);

    // Initial mobile setup
    if(window.innerWidth <= 768) {
        isSidebarOpen = false;
        document.getElementById('sidebar').classList.add('collapsed');
        document.getElementById('menu-toggle').querySelector('i').className = 'bi bi-list';
    }
});

const token = localStorage.getItem('jwtToken');
const headers = {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
};

let allBranches = [];

async function loadData() {
    try {
        // Fetch Branches
        const branchRes = await fetch('/api/superadmin/branches', { headers });
        if(!branchRes.ok) throw new Error('Failed to load branches');
        allBranches = await branchRes.json();
        
        // Fetch Users
        const userRes = await fetch('/api/superadmin/users', { headers });
        if(!userRes.ok) throw new Error('Failed to load users');
        const users = await userRes.json();

        // Update Stats
        document.getElementById('totalBranches').textContent = allBranches.length;
        document.getElementById('totalUsers').textContent = users.length;

        // Populate Branches Table & Select Dropdown
        const bTbody = document.getElementById('branchesTableBody');
        const branchSelect = document.getElementById('assignBranchSelect');
        
        bTbody.innerHTML = '';
        branchSelect.innerHTML = '<option value="">Global (No Branch)</option>';
        
        allBranches.forEach(b => {
            bTbody.innerHTML += `<tr>
                <td>${b.branchId}</td>
                <td><strong>${b.branchName}</strong></td>
                <td><span class="badge bg-secondary">${b.schoolCode || 'N/A'}</span></td>
                <td>${b.branchLocation || 'Unknown'}</td>
            </tr>`;
            
            branchSelect.innerHTML += `<option value="${b.branchId}">${b.branchName}</option>`;
        });

        // Populate Users Table
        const uTbody = document.getElementById('usersTableBody');
        uTbody.innerHTML = '';
        users.forEach(u => {
            const branchName = u.assignedBranch ? u.assignedBranch.branchName : '<span class="text-danger">Global</span>';
            const status = u.isActive === 1 ? '<span class="badge bg-success">Active</span>' : '<span class="badge bg-danger">Suspended</span>';
            uTbody.innerHTML += `<tr>
                <td>${u.id}</td>
                <td>${u.username}</td>
                <td>${u.role}</td>
                <td>${branchName}</td>
                <td>${status}</td>
            </tr>`;
        });

    } catch (e) {
        console.error(e);
        if(e.message.includes('Failed to load')) {
            alert('Your session has expired or you are unauthorized. Redirecting to login.');
            logout();
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const welcomeEl = document.getElementById('welcomeUser');
    if (welcomeEl) welcomeEl.textContent = "Welcome, " + localStorage.getItem('username');
    loadData();

    // Handle Add Branch
    document.getElementById('addBranchForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const formData = new FormData(e.target);
        const payload = Object.fromEntries(formData.entries());
        
        try {
            const res = await fetch('/api/superadmin/branches', {
                method: 'POST',
                headers: headers,
                body: JSON.stringify(payload)
            });
            if(res.ok) {
                alert('School Branch created successfully!');
                e.target.reset();
                bootstrap.Modal.getInstance(document.getElementById('branchModal')).hide();
                loadData();
            } else {
                alert('Failed to create branch.');
            }
        } catch(err) { console.error(err); }
    });

    // Handle Add User
    document.getElementById('addUserForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const formData = new FormData(e.target);
        const payload = Object.fromEntries(formData.entries());
        
        // Format assignedBranch for backend
        if(payload.assignedBranch) {
            payload.assignedBranch = { branchId: parseInt(payload.assignedBranch) };
        } else {
            payload.assignedBranch = null;
        }
        
        try {
            const res = await fetch('/api/superadmin/users', {
                method: 'POST',
                headers: headers,
                body: JSON.stringify(payload)
            });
            if(res.ok) {
                alert('Administrator created successfully!');
                e.target.reset();
                bootstrap.Modal.getInstance(document.getElementById('userModal')).hide();
                loadData();
            } else {
                alert('Failed to create user. Username might already exist.');
            }
        } catch(err) { console.error(err); }
    });
});

// erp/public/assets/scripts/base.js

let isSidebarOpen = true;
let currentTheme = 'light';
let activeModule = 'dashboard';
let isSettingsOpen = false;

// =========================================================================
// 0. BACKWARD COMPATIBILITY BRIDGE
// =========================================================================
window.loadModule = function(target) {
    if (target.includes('.php')) {
        window.location.href = target;
    } else {
        handleNavigate(target);
    }
};

// DOM Elements
const body = document.body;
const menuToggle = document.getElementById('menu-toggle');
const menuIcon = menuToggle ? menuToggle.querySelector('i') : null;
const sidebar = document.getElementById('sidebar');
const content = document.getElementById('content'); 
const settingsBtn = document.getElementById('settings-btn');
const settingsPanel = document.getElementById('settings-panel');
const settingsCloseBtn = document.getElementById('settings-close-btn');
const themeToggleBtn = document.getElementById('theme-toggle-btn');
const fullscreenBtn = document.getElementById('fullscreen-btn');
const fullscreenIcon = fullscreenBtn ? fullscreenBtn.querySelector('i') : null;
const navLinks = document.querySelectorAll('.sidebar .main-item, .sidebar .sub-menu a');
const mobileOverlay = document.getElementById('overlay');

const FULLSCREEN_STORAGE_KEY = 'montfort_is_fullscreen';

// =========================================================================
// 1. SPA ROUTING LOGIC
// =========================================================================
const moduleRoutes = {
    'dashboard': '/views/admin/dashboard_home.html',
    'admission': '/views/admin/students/add_student.html',
    'manage_applications': '/views/admin/applications/manage_applications.html'
};

async function handleNavigate(targetModule) {
    if (!targetModule) return;
    activeModule = targetModule;

    const mainContent = document.getElementById('main-content');
    
    // If not on modular dashboard yet, use legacy routing
    if (!mainContent) {
        const dashboard = document.getElementById('dashboard-module');
        if (dashboard) dashboard.style.display = 'none';

        document.querySelectorAll('.module').forEach(module => {
            module.style.display = 'none';
        });

        const targetEl = document.getElementById(`${targetModule}-module`);
        
        if (targetEl) {
            targetEl.style.display = 'block';
            
            if (targetModule === 'list' && typeof window.loadStudentList === 'function') {
                const listContainer = document.getElementById('student-list-results');
                if (listContainer && !listContainer.hasChildNodes()) {
                    window.loadStudentList();
                }
            }
            
            if (targetModule === 'summary' && typeof window.loadSummary === 'function') {
                window.loadSummary();
            }
        }
    } else {
        // --- NEW SPA DYNAMIC LOADING ---
        const route = moduleRoutes[targetModule];
        if (route) {
            try {
                mainContent.innerHTML = '<div style="padding:40px;text-align:center;"><i class="bi bi-arrow-repeat" style="font-size:2rem;animation:spin 1s linear infinite;"></i><p>Loading module...</p></div>';
                const response = await fetch(route);
                if (response.ok) {
                    const html = await response.text();
                    mainContent.innerHTML = html;
                    
                    // Trigger initialization scripts for specific modules after injecting HTML
                    if (targetModule === 'dashboard') {
                        if (typeof window.loadSummary === 'function') window.loadSummary();
                        if (typeof window.updateDashboardUserInfo === 'function') window.updateDashboardUserInfo();
                    }
                    if (targetModule === 'manage_applications' && typeof loadApplications === 'function') {
                        loadApplications();
                    }
                    if (targetModule === 'admission' && typeof window.initAdmissionForm === 'function') {
                        window.initAdmissionForm();
                    }
                } else {
                    mainContent.innerHTML = `<div style="padding:40px;text-align:center;color:red;"><h2>HTTP Error ${response.status}</h2><p>Could not load the module from ${route}.</p></div>`;
                }
            } catch (error) {
                mainContent.innerHTML = `<div style="padding:40px;text-align:center;color:red;"><h2>Network Error</h2><p>Could not reach the server.</p></div>`;
            }
        } else {
            mainContent.innerHTML = `<div style="padding:40px;text-align:center;"><h2>Module Under Construction</h2><p>The module for '${targetModule}' has not been migrated to the new architecture yet.</p></div>`;
        }
    }

        // Note: Profile routing can be added here if needed


    if(history.pushState) {
        history.pushState(null, null, '#' + targetModule);
    } else {
        window.location.hash = targetModule;
    }

    navLinks.forEach(link => {
        link.classList.remove('active');
        if (link.dataset.menu === targetModule || link.getAttribute('href') === '#' + targetModule) {
            link.classList.add('active');
            const parentMenuListItem = link.closest('li');
            if (parentMenuListItem) {
                const subMenu = parentMenuListItem.closest('.sub-menu');
                if (subMenu) {
                    subMenu.classList.add('show');
                    const parentTrigger = subMenu.previousElementSibling;
                    if (parentTrigger) parentTrigger.classList.add('active');
                }
            }
        }
    });

    if (window.innerWidth <= 768 && isSidebarOpen) { 
        toggleSidebar();
    }
}

// =========================================================================
// 2. UI LAYOUT FUNCTIONS
// =========================================================================
function toggleSidebar() {
    if(!sidebar) return;
    isSidebarOpen = !isSidebarOpen;
    
    if(window.innerWidth <= 768) { 
        sidebar.classList.toggle('open', isSidebarOpen);
        if(mobileOverlay) mobileOverlay.classList.toggle('active', isSidebarOpen);
        sidebar.classList.remove('collapsed');
        if(menuIcon) menuIcon.className = isSidebarOpen ? 'bi bi-x-lg' : 'bi bi-list'; 
    }
    else {
        sidebar.classList.toggle('collapsed', !isSidebarOpen);
        if(content) content.classList.toggle('collapsed', !isSidebarOpen);
        if(menuIcon) menuIcon.className = isSidebarOpen ? 'bi bi-chevron-left' : 'bi bi-list';
    }
}

function toggleSettings() {
    isSettingsOpen = !isSettingsOpen;
    if (settingsPanel) settingsPanel.classList.toggle('show');
}

function toggleTheme() {
    body.classList.remove(currentTheme + '-theme');
    currentTheme = (currentTheme === 'light') ? 'dark' : 'light';
    body.classList.add(currentTheme + '-theme');
    if (themeToggleBtn) themeToggleBtn.textContent = (currentTheme === 'light') ? 'Switch to Dark' : 'Switch to Light';
}

function toggleFullscreen() {
    if (!fullscreenIcon) return; 
    if (!document.fullscreenElement) {
        document.documentElement.requestFullscreen().then(() => {
            localStorage.setItem(FULLSCREEN_STORAGE_KEY, 'true');
        }).catch(err => console.error("Fullscreen error:", err));
        fullscreenIcon.className = 'bi bi-fullscreen-exit';
    } else {
        if (document.exitFullscreen) {
            document.exitFullscreen().then(() => {
                localStorage.removeItem(FULLSCREEN_STORAGE_KEY);
            }).catch(err => console.error("Fullscreen exit error:", err));
            fullscreenIcon.className = 'bi bi-arrows-fullscreen';
        }
    }
}

function restoreFullscreen() {
    if (localStorage.getItem(FULLSCREEN_STORAGE_KEY) === 'true') {
        setTimeout(() => {
             document.documentElement.requestFullscreen().then(() => {
                if (fullscreenIcon) fullscreenIcon.className = 'bi bi-fullscreen-exit';
            }).catch(() => {
                localStorage.removeItem(FULLSCREEN_STORAGE_KEY);
                if (fullscreenIcon) fullscreenIcon.className = 'bi bi-arrows-fullscreen';
            });
        }, 100);
    }
}

// FIX: Highly robust Accordion Handler
function handleAccordion(e){
    const item = e.currentTarget;
    const subMenu = item.nextElementSibling;
    
    // Check if the clicked item is trying to open a dropdown menu
    if (subMenu && subMenu.classList.contains('sub-menu')) {
        e.preventDefault(); // Stop jump to top of page
        
        const isDesktopCollapsed = (window.innerWidth > 768 && !isSidebarOpen);
        if (isDesktopCollapsed) toggleSidebar(); 
        
        // Close all other open menus first
        document.querySelectorAll('.sidebar .sub-menu').forEach(menu => {
            if (menu !== subMenu) {
                menu.classList.remove('show');
            }
        });
        
        // Toggle the clicked menu
        subMenu.classList.toggle('show');
        
    } else {
        // If it's just a normal link, route it!
        if (item.dataset.menu) {
            e.preventDefault();
            handleNavigate(item.dataset.menu);
        } else if (item.hasAttribute('href')) {
            const href = item.getAttribute('href');
            if (href && href.startsWith('#')) {
                e.preventDefault();
                handleNavigate(href.substring(1));
            }
        }
    }
}

// =========================================================================
// 3. INITIALIZATION
// =========================================================================
document.addEventListener('DOMContentLoaded', () => {
    // 1. Update Username/Role (Extracted to window function so it can be called after AJAX)
    window.updateDashboardUserInfo = function() {
        const role = localStorage.getItem('role') || 'User';
        const roleDisplay = document.getElementById('user-role-display');
        if (roleDisplay) {
            roleDisplay.textContent = role.charAt(0).toUpperCase() + role.slice(1);
        }
    };
    
    // Call it initially in case it's not a dynamic SPA load
    window.updateDashboardUserInfo();

    // 2. Fetch Branch Info
    fetch('/api/admin/applications/branch-info', {
        headers: { 'Authorization': 'Bearer ' + localStorage.getItem('jwtToken') }
    })
    .then(res => res.json())
    .then(data => {
        if(data.success && data.data) {
            document.querySelectorAll('.school-name-display').forEach(el => {
                el.textContent = data.data.branch_name;
            });
        }
    })
    .catch(err => console.error("Error fetching branch info:", err));

    // 3. Fetch Dashboard Stats
    fetch('/api/admin/dashboard/stats', {
        headers: { 'Authorization': 'Bearer ' + localStorage.getItem('jwtToken') }
    })
    .then(res => res.json())
    .then(data => {
        if(data.success && data.data) {
            const countEl = document.getElementById('total-students-count');
            if (countEl) countEl.textContent = data.data.total_students;
        }
    })
    .catch(err => console.error("Error fetching dashboard stats:", err));

    if (menuToggle) menuToggle.addEventListener('click', toggleSidebar);
    if (mobileOverlay) mobileOverlay.addEventListener('click', toggleSidebar);
    if (settingsBtn) settingsBtn.addEventListener('click', toggleSettings);
    if (settingsCloseBtn) settingsCloseBtn.addEventListener('click', toggleSettings);
    if (themeToggleBtn) themeToggleBtn.addEventListener('click', toggleTheme);
    if (fullscreenBtn) fullscreenBtn.addEventListener('click', toggleFullscreen);

    navLinks.forEach(link => link.addEventListener('click', handleAccordion));
    
    if(sidebar) {
        if(window.innerWidth <= 768) { 
            isSidebarOpen = false;
            sidebar.classList.remove('open');
            sidebar.classList.add('collapsed'); 
            if (menuIcon) menuIcon.className = 'bi bi-list';
        } else {
            isSidebarOpen = true;
            sidebar.classList.remove('collapsed');
            if (content) content.classList.remove('collapsed');
            if (menuIcon) menuIcon.className = 'bi bi-chevron-left';
        }
    }
    
    restoreFullscreen();

    const hash = window.location.hash.substring(1);
    if (hash && hash !== 'dashboard') {
        const dash = document.getElementById('dashboard-module');
        if(dash) dash.style.display = 'none';
    }

    handleNavigate(hash || 'dashboard');

    window.addEventListener('hashchange', () => {
        const newHash = window.location.hash.substring(1);
        handleNavigate(newHash);
    });
    
    const logoutLink = document.getElementById('logout-link');
    if (logoutLink) {
        logoutLink.addEventListener('click', (e) => {
            e.preventDefault();
            localStorage.removeItem('jwtToken');
            window.location.href = '/login.html';
        });
    }

    const scholarshipDropdown = document.getElementById('scholarship');
    const scholarshipDetailsContainer = document.getElementById('scholarship-details-container');
    if (scholarshipDropdown && scholarshipDetailsContainer) {
        scholarshipDropdown.addEventListener('change', (e) => {
            if (e.target.value === 'Applied') {
                scholarshipDetailsContainer.style.display = 'block';
            } else {
                scholarshipDetailsContainer.style.display = 'none';
            }
        });
    }

    // FIX: Secure Logout Routing 
    document.querySelectorAll('.main-item[data-menu="logout"]').forEach(button => {
        button.addEventListener('click', (e) => {
            e.preventDefault();
            localStorage.removeItem('jwtToken');
            window.location.href = '/login.html';
        });
    });
});

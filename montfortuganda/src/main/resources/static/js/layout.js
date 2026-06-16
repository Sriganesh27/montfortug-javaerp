document.addEventListener('DOMContentLoaded', async function() {
    // 1. Strict Security Check
    const userRole = localStorage.getItem('user_role'); // e.g., "SUPER_ADMIN"
    if (!userRole) {
        window.location.href = '/login';
        return;
    }

    // Convert SUPER_ADMIN to superadmin for the secure clean URL
    let urlRole = 'admin'; // Default fallback
    let safeUserRole = userRole ? userRole.toUpperCase().replace(/\s+/g, '_') : '';

    if (safeUserRole === 'SUPER_ADMIN' || safeUserRole === 'ROLE_SUPER_ADMIN') {
        urlRole = 'superadmin';
    } else if (safeUserRole === 'ROLE_SCHOOL_ADMIN' || safeUserRole === 'SCHOOL_ADMIN') {
        urlRole = 'admin';
    } else if (safeUserRole.startsWith('ROLE_')) {
        urlRole = safeUserRole.replace('ROLE_', '').toLowerCase();
    }

    // 2. Load Core Layout Components
    try {
        const sidebarRes = await fetch('/components/sidebar.html');
        document.getElementById('sidebar-container').innerHTML = await sidebarRes.text();

        const headerRes = await fetch('/components/header.html');
        document.getElementById('header-container').innerHTML = await headerRes.text();

        // Enforce Role-Based Visibility in Sidebar (Uses Pure CSS Class)
        document.querySelectorAll('#sidebarMenu li').forEach(li => {
            const requiredRole = li.getAttribute('data-role');

            if (requiredRole === 'ALL' && userRole === 'SUPER_ADMIN') {
                li.classList.add('hidden');
            } else if (requiredRole !== 'ALL' && requiredRole !== userRole) {
                li.classList.add('hidden');
            }
        });

        const userNameElement = document.getElementById('userNameText');
        if (userNameElement) {
            const savedName = localStorage.getItem('username') || userRole;
            userNameElement.textContent = savedName;
        }

        document.getElementById('logoutBtn').addEventListener('click', async function() {
            // 1. Tell the backend to destroy the secure cookie
            await fetch('/api/auth/logout', { method: 'POST' });

            // 2. Clear local UI variables and redirect
            localStorage.clear();
            window.location.href = '/login';
        });

        const sidebarToggleBtn = document.getElementById('sidebarToggle');
        if (sidebarToggleBtn) {
            sidebarToggleBtn.addEventListener('click', function() {
                if (window.innerWidth <= 768) {
                    document.body.classList.toggle('mobile-sidebar-active');
                } else {
                    const isCollapsed = document.body.classList.toggle('sidebar-collapsed');
                    if (isCollapsed) {
                        sidebarToggleBtn.classList.remove('bi-chevron-left');
                        sidebarToggleBtn.classList.add('bi-list');
                    } else {
                        sidebarToggleBtn.classList.remove('bi-list');
                        sidebarToggleBtn.classList.add('bi-chevron-left');
                    }
                }
            });
        }

        // Close overlay on mobile
        const overlay = document.getElementById('mobile-sidebar-overlay');
        if (overlay) {
            overlay.addEventListener('click', () => {
                document.body.classList.remove('mobile-sidebar-active');
            });
        }

        // 3. Initialize the Router
        setupRouter(urlRole);

        // 4. Initialize Smart Sidebar Clicks (Auto-Expand & Dropdowns)
        document.querySelectorAll('.sidebar-nav > li > a').forEach(link => {
            link.addEventListener('click', function(e) {
                let wasCollapsed = false;

                // Auto-expand sidebar if it is currently collapsed
                if (document.body.classList.contains('sidebar-collapsed')) {
                    wasCollapsed = true;
                    document.body.classList.remove('sidebar-collapsed');

                    const toggleBtn = document.getElementById('sidebarToggle');
                    if (toggleBtn) {
                        toggleBtn.classList.remove('bi-list');
                        toggleBtn.classList.add('bi-chevron-left');
                    }
                }

                // Handle the Dropdown Menu Opening
                if (this.classList.contains('dropdown-toggle')) {
                    e.preventDefault(); // Stop from navigating
                    const parentLi = this.closest('.has-dropdown');

                    // --- SMOOTH SEQUENTIAL ACCORDION LOGIC ---
                    const openDropdowns = document.querySelectorAll('.sidebar-nav .has-dropdown.open');
                    let animationDelay = 0;

                    // 1. Find any open menus and smoothly close them first
                    openDropdowns.forEach(dropdown => {
                        if (dropdown !== parentLi) {
                            dropdown.classList.remove('open');
                            animationDelay = 250; // Wait exactly 250ms for the CSS sliding animation to finish
                        }
                    });

                    // 2. Wait for the old menu to completely close, THEN open the new one!
                    setTimeout(() => {
                        if (wasCollapsed && !parentLi.classList.contains('open')) {
                            parentLi.classList.add('open');
                        } else {
                            parentLi.classList.toggle('open');
                        }
                    }, animationDelay || (wasCollapsed ? 50 : 0));
                }
            });
        });

    } catch (error) {
        console.error("Critical Failure: Unable to load base components", error);
    }
});

function setupRouter(urlRole) {
    const mainContent = document.getElementById('main-content-area');

    document.querySelectorAll('.sidebar-nav a:not(.dropdown-toggle)').forEach(link => {
        link.addEventListener('click', async function(e) {
            e.preventDefault();

            let viewName = this.getAttribute('href').replace('.html', '').replace('/', '');
            if (viewName === 'dashboard' || viewName === 'superadmin') {
                viewName = 'home';
            }

            const newTitle = this.textContent.trim();
            const pageTitleElement = document.getElementById('pageTitle');
            if (pageTitleElement) pageTitleElement.textContent = newTitle;

            document.querySelectorAll('.sidebar-nav a').forEach(nav => nav.classList.remove('active'));
            this.classList.add('active');

            const newUrl = `/${urlRole}/${viewName}`;
            window.history.pushState({ view: viewName, title: newTitle }, "", newUrl);

            await loadView(urlRole, viewName, mainContent);
        });
    });

    window.addEventListener('popstate', async function(event) {
        const viewName = event.state ? event.state.view : 'home';
        const title = event.state && event.state.title ? event.state.title : 'Dashboard';

        const pageTitleElement = document.getElementById('pageTitle');
        if (pageTitleElement) pageTitleElement.textContent = title;

        await loadView(urlRole, viewName, mainContent);
    });

    let initialView = window.location.pathname.split('/').pop();
    if (initialView === urlRole || initialView === 'dashboard' || initialView === '') {
        initialView = 'home';
    }

    // SMART REFRESH MAGIC
    document.querySelectorAll('.sidebar-nav a').forEach(nav => nav.classList.remove('active'));

    let matchedLink = document.querySelector(`.sidebar-nav a[href*="${initialView}"]`);
    if (!matchedLink && initialView === 'home') {
        matchedLink = document.querySelector(`.sidebar-nav a[href*="${urlRole}"]`);
    }

    let pageTitle = "Dashboard";

    if (matchedLink) {
        matchedLink.classList.add('active');
        pageTitle = matchedLink.textContent.trim();

        const parentDropdown = matchedLink.closest('.has-dropdown');
        if (parentDropdown) parentDropdown.classList.add('open');

    } else if (initialView === 'add-branch') {
        const branchesLink = document.querySelector(`.sidebar-nav a[href*="branches"]`);
        if (branchesLink) {
            branchesLink.classList.add('active');
            const parentDropdown = branchesLink.closest('.has-dropdown');
            if (parentDropdown) parentDropdown.classList.add('open');
        }
        pageTitle = "Add New Branch";
    } else if (initialView === 'add-user') {
        const usersLink = document.querySelector(`.sidebar-nav a[href*="users"]`);
        if (usersLink) {
            usersLink.classList.add('active');
            const parentDropdown = usersLink.closest('.has-dropdown');
            if (parentDropdown) parentDropdown.classList.add('open');
        }
        pageTitle = "Add New User";
    }

    const pageTitleElement = document.getElementById('pageTitle');
    if (pageTitleElement) pageTitleElement.textContent = pageTitle;

    loadView(urlRole, initialView, mainContent);
}

async function loadView(urlRole, viewName, container) {
    try {
        const loader = document.getElementById('loaderTemplate').content.cloneNode(true);
        container.textContent = '';
        container.appendChild(loader);

        const response = await fetch(`/views/${urlRole}/${viewName}.html`);

        if (response.ok) {
            container.innerHTML = await response.text();
            document.dispatchEvent(new CustomEvent('viewLoaded', { detail: { role: urlRole, view: viewName } }));
        } else {
            container.textContent = '';
            container.appendChild(document.getElementById('errorTemplate').content.cloneNode(true));
        }
    } catch (error) {
        container.textContent = '';
        container.appendChild(document.getElementById('systemErrorTemplate').content.cloneNode(true));
    }
}

// UI UTILITIES
function showLoader() { const l = document.getElementById('global-loader'); if(l) l.classList.remove('hidden'); }
function hideLoader() { const l = document.getElementById('global-loader'); if(l) l.classList.add('hidden'); }
function showErrorMessage(m) { alert(`❌ ERROR: ${m}`); }
function showSuccessMessage(m) { alert(`✅ SUCCESS: ${m}`); }
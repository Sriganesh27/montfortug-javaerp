document.addEventListener('DOMContentLoaded', async function() {
    // 1. Strict Security Check
    const token = localStorage.getItem('jwt_token');
    const userRole = localStorage.getItem('user_role'); // e.g., "SUPER_ADMIN"

    if (!token || !userRole) {
        window.location.href = '/login';
        return;
    }

    // Convert SUPER_ADMIN to superadmin for the secure clean URL
    const urlRole = userRole.toLowerCase().replace('_', '');

    // 2. Load Core Layout Components
    try {
        const sidebarRes = await fetch('/components/sidebar.html');
        document.getElementById('sidebar-container').innerHTML = await sidebarRes.text();

        const headerRes = await fetch('/components/header.html');
        document.getElementById('header-container').innerHTML = await headerRes.text();

        // Enforce Role-Based Visibility in Sidebar (Uses Pure CSS Class)
        document.querySelectorAll('#sidebarMenu li').forEach(li => {
            const requiredRole = li.getAttribute('data-role');
            if (requiredRole !== 'ALL' && requiredRole !== userRole) {
                li.classList.add('hidden');
            }
        });

        // Set User Profile Name (Strictly no HTML strings, pure text injection!)
        const userNameElement = document.getElementById('userNameText');
        if (userNameElement) {
            userNameElement.textContent = userRole;
        }

        // Secure Logout Function
        document.getElementById('logoutBtn').addEventListener('click', function() {
            localStorage.clear();
            window.location.href = '/login';
        });

        const sidebarToggleBtn = document.getElementById('sidebarToggle');
        sidebarToggleBtn.addEventListener('click', function() {
            // Toggle the body class and get the new state
            const isCollapsed = document.body.classList.toggle('sidebar-collapsed');

            // Swap the icons based on the new state
            if (isCollapsed) {
                // When Closed: Show the hamburger menu
                sidebarToggleBtn.classList.remove('bi-chevron-left');
                sidebarToggleBtn.classList.add('bi-list');
            } else {
                // When Open: Show the left chevron
                sidebarToggleBtn.classList.remove('bi-list');
                sidebarToggleBtn.classList.add('bi-chevron-left');
            }
        });

        // 3. Initialize the Router
        setupRouter(urlRole);

    } catch (error) {
        console.error("Critical Failure: Unable to load base components", error);
    }
});

function setupRouter(urlRole) {
    const mainContent = document.getElementById('main-content-area');

    // Intercept clicks on sidebar navigation links
    document.querySelectorAll('.sidebar-nav a').forEach(link => {
        link.addEventListener('click', async function(e) {
            e.preventDefault();

            let viewName = this.getAttribute('href').replace('.html', '').replace('/', '');
            if (viewName === 'dashboard' || viewName === 'superadmin') {
                viewName = 'home';
            }

            // Update URL using HTML5 History API for copy-paste sharing
            const newUrl = `/${urlRole}/${viewName}`;
            window.history.pushState({ view: viewName }, "", newUrl);

            // Fetch and render the view
            await loadView(urlRole, viewName, mainContent);
        });
    });

    // Handle Browser Back/Forward buttons
    window.addEventListener('popstate', async function(event) {
        const viewName = event.state ? event.state.view : 'home';
        await loadView(urlRole, viewName, mainContent);
    });

    // Initial Load Logic (Reads the current URL on first page load)
    let initialView = window.location.pathname.split('/').pop();
    if (initialView === urlRole || initialView === 'dashboard') {
        initialView = 'home';
    }

    loadView(urlRole, initialView, mainContent);
}

// ---------------------------------------------------------
// THE PERFECT WAY: Zero HTML strings in Javascript!
// ---------------------------------------------------------
async function loadView(urlRole, viewName, container) {
    try {
        // 1. Get the loader template and put it on the screen
        const loader = document.getElementById('loaderTemplate').content.cloneNode(true);
        container.innerHTML = ''; // clear the screen
        container.appendChild(loader);

        // 2. Fetch the specific file based on the secure role folder
        const response = await fetch(`/views/${urlRole}/${viewName}.html`);

        if (response.ok) {
            container.innerHTML = await response.text();
        } else {
            // 3. Get the 404 error template
            const errorView = document.getElementById('errorTemplate').content.cloneNode(true);
            container.innerHTML = '';
            container.appendChild(errorView);
        }
    } catch (error) {
        // 4. Get the System Error template
        const sysErrorView = document.getElementById('systemErrorTemplate').content.cloneNode(true);
        container.innerHTML = '';
        container.appendChild(sysErrorView);
    }
}
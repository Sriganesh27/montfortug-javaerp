// ==========================================
// GLOBAL IDLE SESSION TIMEOUT
// ==========================================
const ERP_IDLE_TIMEOUT_MS = 60 * 60 * 1000; // 1 hour
const ERP_LAST_ACTIVITY_KEY = 'erp_last_activity';

let erpIdleTimer = null;
let erpLogoutStarted = false;

function normalizeErpRole(role) {
    let normalizedRole = String(role || '')
        .trim()
        .toUpperCase()
        .replace(/\s+/g, '_');

    while (normalizedRole.startsWith('ROLE_')) {
        normalizedRole =
            normalizedRole.substring(5);
    }

    if (normalizedRole === 'SCHOOL_ADMIN') {
        return 'BRANCH_ADMIN';
    }

    if (normalizedRole === 'SUPER_USER') {
        return 'SUPER_ADMIN';
    }

    return normalizedRole;
}

function resolvePortalRole(userRole) {
    const portalByRole = {
        SUPER_ADMIN: 'superadmin',
        BRANCH_ADMIN: 'admin',
        PARENT: 'parent',
        ACADEMIC_COORDINATOR:
            'academiccoordinator',
        ADMISSION_STAFF: 'admissionstaff',
        FEE_OFFICER: 'feeofficer',
        TEACHER: 'teacher',
        AUDITOR: 'auditor'
    };

    return portalByRole[userRole] ||
        userRole
            .toLowerCase()
            .replaceAll('_', '');
}

function clearLocalSessionData() {
    localStorage.removeItem('user_role');
    localStorage.removeItem('username');
    localStorage.removeItem('user_branch');
    localStorage.removeItem('school_id');
    localStorage.removeItem('branch_id');
    localStorage.removeItem('permissions');
    localStorage.removeItem(ERP_LAST_ACTIVITY_KEY);
}

async function logoutDueToInactivity() {
    if (erpLogoutStarted) return;
    erpLogoutStarted = true;

    clearTimeout(erpIdleTimer);

    try {
        await fetch('/api/auth/logout', {
            method: 'POST',
            credentials: 'include'
        });
    } catch (error) {
        console.warn('Backend logout failed during inactivity timeout');
    }

    clearLocalSessionData();

    if (typeof window.showSessionTimeoutModal === 'function') {
        window.showSessionTimeoutModal({
            title: 'Session Expired',
            message: 'You were logged out because there was no activity for 1 hour.',
            buttonText: 'Login Again',
            redirectUrl: '/login.html'
        });
    } else {
        window.location.href = '/login.html';
    }
}

function scheduleIdleTimeout() {
    clearTimeout(erpIdleTimer);

    const lastActivity = Number(
        localStorage.getItem(ERP_LAST_ACTIVITY_KEY) || Date.now()
    );

    const elapsed = Date.now() - lastActivity;
    const remaining = ERP_IDLE_TIMEOUT_MS - elapsed;

    if (remaining <= 0) {
        void logoutDueToInactivity();
        return;
    }

    erpIdleTimer = setTimeout(
        logoutDueToInactivity,
        remaining
    );
}

function recordErpActivity() {
    if (erpLogoutStarted) return;

    localStorage.setItem(
        ERP_LAST_ACTIVITY_KEY,
        Date.now().toString()
    );

    scheduleIdleTimeout();
}
window.renderEmptyTableMessage = function(tbody, colSpan, message) {
    if (!tbody) return;
    tbody.textContent = '';
    const template = document.getElementById('global-table-empty-template');
    if (!template) return;
    const clone = template.content.cloneNode(true);
    const td = clone.querySelector('.message-cell');
    if (td) {
        td.colSpan = colSpan;
        td.textContent = message;
    }
    tbody.appendChild(clone);
};

window.renderFetchingMessage = function(tbody, colSpan, message) {
    if (!tbody) return;
    tbody.textContent = '';
    const template = document.getElementById('global-table-fetching-template');
    if (!template) return;
    const clone = template.content.cloneNode(true);
    const td = clone.querySelector('.message-cell');
    const span = clone.querySelector('.fetching-text');
    if (td) td.colSpan = colSpan;
    if (span) span.textContent = message;
    tbody.appendChild(clone);
};

document.addEventListener('DOMContentLoaded', async function() {
    // 1. Strict Security Check
    const storedUserRole =
        localStorage.getItem('user_role');

    if (!storedUserRole) {
        window.location.href = '/login';
        return;
    }

    const userRole =
        normalizeErpRole(storedUserRole);

    if (!userRole) {
        clearLocalSessionData();
        window.location.href = '/login';
        return;
    }

    localStorage.setItem(
        'user_role',
        userRole
    );
    const activityEvents = [
        'mousedown',
        'keydown',
        'scroll',
        'touchstart'
    ];

    activityEvents.forEach(eventName => {
        document.addEventListener(eventName, recordErpActivity, {
            passive: true
        });
    });

    if (!localStorage.getItem(ERP_LAST_ACTIVITY_KEY)) {
        localStorage.setItem(
            ERP_LAST_ACTIVITY_KEY,
            Date.now().toString()
        );
    }

    scheduleIdleTimeout();

    window.addEventListener('storage', function(event) {
        if (event.key === ERP_LAST_ACTIVITY_KEY) {
            scheduleIdleTimeout();
        }
    });
    const urlRole =
        resolvePortalRole(userRole);

    // 2. Load Core Layout Components
    try {
        const sidebarRes = await fetch('/components/sidebar.html');
        document.getElementById('sidebar-container').innerHTML = await sidebarRes.text();

        const headerRes = await fetch('/components/header.html');
        document.getElementById('header-container').innerHTML = await headerRes.text();

        // Enforce Role-Based Visibility in Sidebar (Uses Pure CSS Class)
        document.querySelectorAll('#sidebarMenu li').forEach(li => {
            const requiredRole =
                normalizeErpRole(
                    li.getAttribute('data-role')
                );

            if (requiredRole === 'ALL' && userRole === 'SUPER_ADMIN') {
                li.classList.add('hidden');
            } else if (requiredRole !== 'ALL' && requiredRole !== userRole) {
                li.classList.add('hidden');
            } else {
                li.classList.remove('hidden');
            }
        });

        const userNameElement = document.getElementById('userNameText');
        if (userNameElement) {
            userNameElement.textContent = localStorage.getItem('username') || userRole;
        }

        document.getElementById('logoutBtn').addEventListener('click', async function() {
            // 1. Tell the backend to destroy the secure cookie securely
            try {
                await fetch('/api/auth/logout', {
                    method: 'POST',
                    credentials: 'include'
                });
            } catch (e) {
                console.warn("Logout API failed, forcing local logout");
            }

            // 2. Clear local UI variables and redirect
            erpLogoutStarted = true;
            clearTimeout(erpIdleTimer);
            clearLocalSessionData();

            window.location.href = '/login.html';
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
    const mainContent =
        document.getElementById('main-content-area');
    const sidebarLinks = () => Array.from(
        document.querySelectorAll(
            '.sidebar-nav a:not(.dropdown-toggle)'
        )
    );

    function decodePathSegment(segment) {
        try {
            return decodeURIComponent(segment);
        } catch (error) {
            console.warn('Invalid encoded route segment.', error);
            return segment;
        }
    }

    function readCurrentRoute(historyState = null) {
        const pathSegments = window.location.pathname
            .split('/')
            .filter(Boolean)
            .map(decodePathSegment);
        const pathRole = String(pathSegments[0] || '')
            .toLowerCase();
        const routeRole =
            historyState?.role ||
            (
                pathRole === 'admin' ||
                pathRole === 'superadmin'
                    ? pathRole
                    : urlRole
            );
        let viewName =
            historyState?.view ||
            (
                pathRole === 'admin' ||
                pathRole === 'superadmin'
                    ? pathSegments[1]
                    : null
            ) ||
            'home';

        viewName = String(viewName)
            .trim()
            .toLowerCase()
            .replace(/\.html$/i, '');

        if (
            !viewName ||
            viewName === 'dashboard' ||
            viewName === 'admin' ||
            viewName === 'superadmin' ||
            viewName === routeRole
        ) {
            viewName = 'home';
        }

        const routeParams = Array.isArray(
            historyState?.routeParams
        )
            ? historyState.routeParams.map(String)
            : (
                pathRole === 'admin' ||
                pathRole === 'superadmin'
                    ? pathSegments.slice(2)
                    : []
            );

        return {
            role: routeRole,
            view: viewName,
            routeParams,
            title: String(historyState?.title || '').trim()
        };
    }

    function findLinkForView(viewName) {
        return sidebarLinks().find(link => {
            return window.getSidebarViewName?.(link) === viewName;
        }) || null;
    }

    function resolveRouteTitle(route, matchedLink = null) {
        if (route.title) return route.title;

        if (
            route.view === 'employees' &&
            route.routeParams.length > 0
        ) {
            return route.routeParams[1] === 'edit'
                ? 'Edit Employee'
                : 'Employee Details';
        }

        const titleOverrides = {
            'add-branch': 'Add New Branch',
            'add-user': 'Add New User',
            'add-employee': 'Add Employee'
        };

        return titleOverrides[route.view] ||
            matchedLink?.textContent?.trim() ||
            'Dashboard';
    }

    sidebarLinks().forEach(link => {
        link.addEventListener('click', async function(event) {
            event.preventDefault();

            const viewName =
                window.getSidebarViewName?.(this);
            if (!viewName) return;

            await window.erpNavigate({
                role: urlRole,
                view: viewName,
                routeParams: [],
                title: this.textContent.trim(),
                historyMode: 'push',
                container: mainContent,
                sidebarLink: this
            });
        });
    });

    window.addEventListener('popstate', async event => {
        const route = readCurrentRoute(event.state);
        const matchedLink = findLinkForView(route.view);

        route.title = resolveRouteTitle(route, matchedLink);

        await window.erpNavigate({
            ...route,
            historyMode: 'none',
            container: mainContent,
            sidebarLink: matchedLink
        });
    });

    const initialRoute = readCurrentRoute();
    const matchedLink = findLinkForView(initialRoute.view);
    initialRoute.title = resolveRouteTitle(
        initialRoute,
        matchedLink
    );

    void window.erpNavigate({
        ...initialRoute,
        historyMode: 'replace',
        container: mainContent,
        sidebarLink: matchedLink
    });
}

// ==========================================
// UI UTILITIES & GLOBAL MODAL ENGINE
// ==========================================
/* showLoader() and hideLoader() are provided by global.js. */

// 1. The Engine is now global for all modules (superadmin, admin, teacher, etc.)
/**
 * @param {Object} config
 * @param {string} config.title
 * @param {string} [config.type='info']
 * @param {Node|null} [config.contentNode=null]
 * @param {string|null} [config.contentText=null]
 * @param {string} [config.confirmText='OK']
 * @param {string|null} [config.cancelText=null]
 * @param {Function|null} [config.onConfirm=null]
 */
function showPremiumModal({ title, type = 'info', contentNode = null, contentText = null, confirmText = 'OK', cancelText = null, onConfirm = null }) {
    let modalWrapper = document.getElementById('premium-modal-wrapper');
    if (!modalWrapper) {
        modalWrapper = document.createElement('div');
        modalWrapper.id = 'premium-modal-wrapper';
        document.body.appendChild(modalWrapper);
    }

    // Pure DOM clear
    while (modalWrapper.firstChild) modalWrapper.removeChild(modalWrapper.firstChild);
    document.body.classList.add('erp-modal-open');

    const template = document.getElementById('premium-modal-template');
    if (!template) {
        document.body.classList.remove('erp-modal-open');
        // Ultimate fallback just in case the template hasn't loaded yet
        if (type === 'error') alert("ERROR: " + (contentText || "An error occurred"));
        else if (type === 'success') alert("SUCCESS: " + (contentText || "Action completed"));
        else alert(contentText || "Action completed");
        return;
    }

    const clone = template.content.cloneNode(true);
    const overlay = clone.querySelector('.premium-modal-overlay');
    const iconContainer = clone.querySelector('.premium-modal-icon');
    const iconElement = clone.querySelector('.premium-modal-icon i');
    const titleElement = clone.querySelector('.pm-title');
    const bodyElement = clone.querySelector('.premium-modal-body');
    const cancelBtn = clone.querySelector('.pm-btn-cancel');
    const confirmBtn = clone.querySelector('.pm-btn-confirm');

    // Assign standard Bootstrap icons dynamically based on type
    if (type === 'success') {
        iconContainer.classList.add('success');
        iconElement.classList.add('bi-check-circle-fill');
    } else if (type === 'warning') {
        iconContainer.classList.add('warning');
        iconElement.classList.add('bi-exclamation-triangle-fill');
    } else if (type === 'error') {
        iconContainer.classList.add('error');
        iconElement.classList.add('bi-x-circle-fill');
    } else {
        iconContainer.classList.add('info');
        iconElement.classList.add('bi-info-circle-fill');
    }

    titleElement.textContent = title;

    if (contentNode) bodyElement.appendChild(contentNode);
    else if (contentText) bodyElement.textContent = contentText;

    let isClosing = false;
    const closeObj = {
        close: () => new Promise(resolve => {
            if (isClosing) {
                resolve();
                return;
            }

            isClosing = true;
            overlay.classList.add('pm-fade-out');
            setTimeout(() => {
                if (modalWrapper.contains(overlay)) {
                    overlay.remove();
                }

                if (!modalWrapper.firstChild) {
                    document.body.classList.remove('erp-modal-open');
                }

                resolve();
            }, 200);
        })
    };

    const runModalAction = async action => {
        if (confirmBtn.disabled) return;

        confirmBtn.disabled = true;
        cancelBtn.disabled = true;

        try {
            await action();
        } catch (error) {
            console.error('Premium modal action failed.', error);
            confirmBtn.disabled = false;
            cancelBtn.disabled = false;
        }
    };

    if (cancelText) {
        cancelBtn.textContent = cancelText;
        cancelBtn.classList.remove('hidden');
        cancelBtn.addEventListener(
            'click',
            () => void runModalAction(() => closeObj.close())
        );
    }

    confirmBtn.textContent = confirmText;
    if (type === 'warning' || type === 'error') {
        confirmBtn.classList.remove('premium-modal-btn-primary');
        confirmBtn.classList.add('premium-modal-btn-danger'); // Makes button red!
    }

    confirmBtn.addEventListener(
        'click',
        () => void runModalAction(async () => {
            if (onConfirm) {
                await onConfirm(closeObj);
                return;
            }

            await closeObj.close();
        })
    );

    modalWrapper.appendChild(clone);
    window.requestAnimationFrame(() => confirmBtn.focus());
}

// 2. Overwrite the generic alerts so ANY system error or success automatically uses the premium modal!
function showErrorMessage(m) {
    showPremiumModal({
        title: 'Error',
        type: 'error',
        contentText: m,
        confirmText: 'Dismiss'
    });
}

function showSuccessMessage(m) {
    showPremiumModal({
        title: 'Success',
        type: 'success',
        contentText: m,
        confirmText: 'OK'
    });
}

/* ========================================= */
/* --- GLOBAL PAGINATION UTILITY         --- */
/* --- Added to layout.js for centralization */
/* ========================================= */

class GlobalPagination {
    constructor(config) {
        this.data = config.data || [];
        this.itemsPerPage = config.itemsPerPage || 25;
        this.currentPage = 1;
        this.renderCallback = config.renderCallback;

        this.startEl = document.getElementById(config.elements.startId);
        this.endEl = document.getElementById(config.elements.endId);
        this.totalEl = document.getElementById(config.elements.totalId);
        this.prevBtn = document.getElementById(config.elements.prevBtnId);
        this.nextBtn = document.getElementById(config.elements.nextBtnId);
        this.numbersContainer = document.getElementById(config.elements.numbersContainerId);
        this.numTemplate = document.getElementById(config.elements.templateId);

        this.bindEvents();
    }

    updateData(newData) {
        this.data = newData;
        this.currentPage = 1;
        this.render();
    }

    bindEvents() {
        if(this.prevBtn) this.prevBtn.addEventListener('click', () => this.changePage(this.currentPage - 1));
        if(this.nextBtn) this.nextBtn.addEventListener('click', () => this.changePage(this.currentPage + 1));
    }

    changePage(newPage) {
        const totalPages = Math.ceil(this.data.length / this.itemsPerPage);
        if (newPage >= 1 && newPage <= totalPages) {
            this.currentPage = newPage;
            this.render();
        }
    }

    render() {
        const startIdx = (this.currentPage - 1) * this.itemsPerPage;
        const endIdx = startIdx + this.itemsPerPage;
        const pageData = this.data.slice(startIdx, endIdx);

        if (this.renderCallback) {
            this.renderCallback(pageData);
        }

        this.updateUI();
    }

    updateUI() {
        const totalItems = this.data.length;
        const totalPages = Math.ceil(totalItems / this.itemsPerPage);

        // STRICTURE FIX: Force conversion to string to satisfy the IDE type-checker
        if(this.startEl) this.startEl.textContent = (totalItems === 0 ? 0 : ((this.currentPage - 1) * this.itemsPerPage) + 1).toString();
        if(this.endEl) this.endEl.textContent = Math.min(this.currentPage * this.itemsPerPage, totalItems).toString();
        if(this.totalEl) this.totalEl.textContent = totalItems.toString();

        if(this.prevBtn) this.prevBtn.disabled = this.currentPage === 1 || totalItems === 0;
        if(this.nextBtn) this.nextBtn.disabled = this.currentPage === totalPages || totalPages === 0;

        if(this.numbersContainer && this.numTemplate) {
            while (this.numbersContainer.firstChild) {
                this.numbersContainer.removeChild(this.numbersContainer.firstChild);
            }

            for (let i = 1; i <= totalPages; i++) {
                const btnNode = this.numTemplate.content.cloneNode(true);
                const btn = btnNode.querySelector('button');
                btn.textContent = i.toString(); // Force to string

                if (i === this.currentPage) btn.classList.add('active');

                btn.addEventListener('click', () => this.changePage(i));
                this.numbersContainer.appendChild(btnNode);
            }
        }
    }
}

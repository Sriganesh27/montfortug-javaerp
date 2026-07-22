// ========================================================
// GLOBAL ACTION FEEDBACK
// ========================================================

(function initializeErpActionFeedback() {
    const activeOperations = new Map();
    const legacyTokens = [];
    const buttonStates = new WeakMap();
    let nextTokenId = 0;

    function getFeedbackElement() {
        let feedback =
            document.getElementById('erp-action-feedback');

        if (feedback) return feedback;

        feedback = document.createElement('div');
        feedback.id = 'erp-action-feedback';
        feedback.className = 'erp-action-feedback';
        feedback.setAttribute('role', 'status');
        feedback.setAttribute('aria-live', 'polite');
        feedback.setAttribute('aria-atomic', 'true');

        const spinner = document.createElement('span');
        spinner.className = 'erp-action-spinner';
        spinner.setAttribute('aria-hidden', 'true');

        const message = document.createElement('span');
        message.className = 'erp-action-message';
        message.textContent = 'Processing...';

        feedback.append(spinner, message);
        document.body.appendChild(feedback);
        return feedback;
    }

    function renderActiveOperation() {
        const feedback = getFeedbackElement();
        const operations = Array.from(
            activeOperations.values()
        );
        const current = operations.at(-1);

        if (!current) {
            feedback.classList.remove('is-visible');
            document.body.classList.remove(
                'erp-action-in-progress'
            );
            return;
        }

        const message = feedback.querySelector(
            '.erp-action-message'
        );

        if (message) {
            message.textContent = current.message;
        }

        feedback.classList.add('is-visible');
        document.body.classList.add(
            'erp-action-in-progress'
        );
    }

    function start(message = 'Processing...') {
        const token = `erp-operation-${++nextTokenId}`;
        const operation = {
            message: String(message || 'Processing...').trim()
        };

        operation.longWaitTimer = window.setTimeout(() => {
            if (!activeOperations.has(token)) return;

            operation.message =
                'Still working. Please wait...';
            renderActiveOperation();
        }, 8000);

        activeOperations.set(token, operation);
        renderActiveOperation();
        return token;
    }

    function end(token) {
        const operation = activeOperations.get(token);

        if (!operation) return;

        window.clearTimeout(operation.longWaitTimer);
        activeOperations.delete(token);
        renderActiveOperation();
    }

    window.erpActionFeedback = {
        start,
        end,
        isBusy() {
            return activeOperations.size > 0;
        }
    };

    /* Backward-compatible API used by existing modules. */
    window.showLoader = function showLoader(
        message = 'Processing...'
    ) {
        const token = start(message);
        legacyTokens.push(token);
        return token;
    };

    window.hideLoader = function hideLoader(token = null) {
        if (token) {
            const index = legacyTokens.lastIndexOf(token);
            if (index >= 0) legacyTokens.splice(index, 1);
            end(token);
            return;
        }

        const latestToken = legacyTokens.pop();
        if (latestToken) end(latestToken);
    };

    window.erpWithButtonFeedback =
        async function erpWithButtonFeedback(
            button,
            loadingText,
            operation
        ) {
            if (typeof operation !== 'function') {
                throw new TypeError(
                    'A button operation function is required.'
                );
            }

            if (!(button instanceof HTMLElement)) {
                return operation();
            }

            if (buttonStates.has(button)) return false;

            const state = {
                html: button.innerHTML,
                disabled: Boolean(button.disabled)
            };

            buttonStates.set(button, state);
            button.disabled = true;
            button.setAttribute('aria-busy', 'true');
            button.classList.add('erp-button-busy');
            button.replaceChildren();

            const spinner = document.createElement('span');
            spinner.className = 'erp-button-spinner';
            spinner.setAttribute('aria-hidden', 'true');

            const label = document.createElement('span');
            label.textContent =
                String(loadingText || 'Processing...');

            button.append(spinner, label);

            try {
                return await operation();
            } finally {
                const savedState = buttonStates.get(button);

                if (savedState) {
                    button.innerHTML = savedState.html;
                    button.disabled = savedState.disabled;
                }

                button.removeAttribute('aria-busy');
                button.classList.remove('erp-button-busy');
                buttonStates.delete(button);
            }
        };
})();

// ========================================================
// GLOBAL DYNAMIC VIEW LOADER
// ========================================================

(function initializeGlobalViewLoader() {
    let activeViewRequest = null;
    let latestViewRequestId = 0;

    const SAFE_PATH_SEGMENT =
        /^[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$/;

    function normalizePathSegment(value) {
        return String(value || '')
            .trim()
            .toLowerCase();
    }

    function normalizeRouteParams(routeParams) {
        if (!Array.isArray(routeParams)) return [];

        return routeParams
            .map(value => String(value ?? '').trim())
            .filter(value => value.length > 0)
            .map(value => value.slice(0, 128));
    }

    function cloneViewTemplate(templateId) {
        const template = document.getElementById(templateId);

        if (!(template instanceof HTMLTemplateElement)) {
            return null;
        }

        return template.content.cloneNode(true);
    }

    function replaceContainerContent(container, content) {
        container.replaceChildren();
        if (content) container.appendChild(content);
    }

    function renderViewError(
        container,
        templateId,
        heading,
        message
    ) {
        const templateContent = cloneViewTemplate(templateId);

        if (templateContent) {
            replaceContainerContent(container, templateContent);
            return;
        }

        const errorContainer = document.createElement('div');
        errorContainer.className = 'error-container';

        const title = document.createElement('h2');
        title.textContent = heading;

        const description = document.createElement('p');
        description.textContent = message;

        errorContainer.append(title, description);
        replaceContainerContent(container, errorContainer);
    }

    window.loadView = async function loadView(
        portalRole,
        viewName,
        targetContainer = null,
        routeParams = []
    ) {
        const normalizedRole =
            normalizePathSegment(portalRole);
        const normalizedView =
            normalizePathSegment(viewName);
        const normalizedRouteParams =
            normalizeRouteParams(routeParams);
        const container =
            targetContainer ||
            document.getElementById('main-content-area');

        if (!(container instanceof HTMLElement)) {
            console.error('Dynamic view container was not found.');
            return false;
        }

        if (
            !SAFE_PATH_SEGMENT.test(normalizedRole) ||
            !SAFE_PATH_SEGMENT.test(normalizedView)
        ) {
            renderViewError(
                container,
                'errorTemplate',
                'Invalid View',
                'The requested module name is invalid.'
            );
            return false;
        }

        activeViewRequest?.abort();

        const requestController = new AbortController();
        activeViewRequest = requestController;
        const requestId = ++latestViewRequestId;
        const readableViewName = normalizedView
            .replace(/-/g, ' ')
            .replace(/\b\w/g, letter => letter.toUpperCase());
        const feedbackToken = window.erpActionFeedback.start(
            `Loading ${readableViewName}...`
        );

        container.setAttribute('aria-busy', 'true');
        container.classList.add('erp-navigation-busy');

        document.dispatchEvent(
            new CustomEvent('viewLoading', {
                detail: {
                    role: normalizedRole,
                    view: normalizedView,
                    routeParams: normalizedRouteParams,
                    container
                }
            })
        );

        try {
            const response = await fetch(
                `/views/${encodeURIComponent(normalizedRole)}/${encodeURIComponent(normalizedView)}.html`,
                {
                    method: 'GET',
                    credentials: 'same-origin',
                    cache: 'no-store',
                    headers: {
                        'X-Requested-With': 'XMLHttpRequest'
                    },
                    signal: requestController.signal
                }
            );

            if (requestId !== latestViewRequestId) return false;

            if (!response.ok) {
                renderViewError(
                    container,
                    'errorTemplate',
                    'View Not Found',
                    'The requested module is unavailable.'
                );

                document.dispatchEvent(
                    new CustomEvent('viewLoadFailed', {
                        detail: {
                            role: normalizedRole,
                            view: normalizedView,
                            routeParams: normalizedRouteParams,
                            status: response.status,
                            reason: 'HTTP_ERROR'
                        }
                    })
                );
                return false;
            }

            const htmlText = await response.text();
            if (requestId !== latestViewRequestId) return false;

            const wrapper = document.createElement('div');
            wrapper.className =
                'view-transition-wrapper erp-view-preparing';
            wrapper.innerHTML = htmlText;
            replaceContainerContent(container, wrapper);

            const initializationPromises = [];
            let acceptsInitializationPromises = true;
            const viewDetail = {
                role: normalizedRole,
                view: normalizedView,
                routeParams: normalizedRouteParams,
                container,
                viewElement: wrapper,
                waitUntil(promise) {
                    if (
                        !acceptsInitializationPromises ||
                        promise === null ||
                        promise === undefined
                    ) {
                        return;
                    }

                    initializationPromises.push(
                        Promise.resolve(promise)
                    );
                }
            };

            document.dispatchEvent(
                new CustomEvent('viewLoaded', {
                    detail: viewDetail
                })
            );
            acceptsInitializationPromises = false;

            const results = await Promise.allSettled(
                initializationPromises
            );

            if (requestId !== latestViewRequestId) return false;

            const rejectedResult = results.find(
                result => result.status === 'rejected'
            );

            if (rejectedResult) {
                throw rejectedResult.reason;
            }

            wrapper.classList.remove('erp-view-preparing');
            wrapper.classList.add('erp-view-ready');
            return true;
        } catch (error) {
            if (error?.name === 'AbortError') return false;

            console.error('Unable to load dynamic ERP view.', error);

            if (requestId === latestViewRequestId) {
                renderViewError(
                    container,
                    'systemErrorTemplate',
                    'System Error',
                    'The page could not be prepared. Please try again.'
                );

                document.dispatchEvent(
                    new CustomEvent('viewLoadFailed', {
                        detail: {
                            role: normalizedRole,
                            view: normalizedView,
                            routeParams: normalizedRouteParams,
                            status: null,
                            reason: 'INITIALIZATION_ERROR'
                        }
                    })
                );
            }
            return false;
        } finally {
            window.erpActionFeedback.end(feedbackToken);

            if (requestId === latestViewRequestId) {
                container.removeAttribute('aria-busy');
                container.classList.remove('erp-navigation-busy');
                activeViewRequest = null;
            }
        }
    };
})();

// ========================================================
// GLOBAL ERP VIEW NAVIGATION
// ========================================================

(function initializeGlobalViewNavigation() {
    function normalizePathSegment(value) {
        return String(value || '').trim().toLowerCase();
    }

    function normalizeRouteParams(routeParams) {
        if (!Array.isArray(routeParams)) return [];

        return routeParams
            .map(value => String(value ?? '').trim())
            .filter(Boolean)
            .map(value => value.slice(0, 128));
    }

    function getSidebarViewName(link) {
        if (!(link instanceof HTMLAnchorElement)) return '';

        const href = link.getAttribute('href') || '';
        if (
            !href ||
            href === '#' ||
            link.classList.contains('dropdown-toggle')
        ) {
            return '';
        }

        const pathname = new URL(
            href,
            window.location.origin
        ).pathname;
        let viewName = pathname
            .split('/')
            .filter(Boolean)
            .pop() || '';

        viewName = viewName.replace(/\.html$/i, '');

        if (
            viewName === 'dashboard' ||
            viewName === 'superadmin' ||
            viewName === 'admin'
        ) {
            return 'home';
        }

        return normalizePathSegment(viewName);
    }

    function findSidebarLink(viewName) {
        const normalizedView = normalizePathSegment(viewName);

        return Array.from(
            document.querySelectorAll(
                '#sidebarMenu a:not(.dropdown-toggle)'
            )
        ).find(link => {
            return getSidebarViewName(link) === normalizedView;
        }) || null;
    }

    function updateSidebarSelection(
        viewName,
        preferredLink = null
    ) {
        const sidebar = document.getElementById('sidebarMenu');
        if (!sidebar) return;

        sidebar.querySelectorAll('a.active').forEach(link => {
            link.classList.remove('active');
        });
        sidebar
            .querySelectorAll('.has-dropdown.open')
            .forEach(dropdown => {
                dropdown.classList.remove('open');
            });

        const activeLink =
            preferredLink instanceof HTMLAnchorElement &&
            sidebar.contains(preferredLink)
                ? preferredLink
                : findSidebarLink(viewName);

        if (!activeLink) return;

        activeLink.classList.add('active');
        activeLink.closest('.has-dropdown')?.classList.add('open');
    }

    function updatePageTitle(title) {
        const normalizedTitle = String(title || '').trim();
        if (!normalizedTitle) return;

        const pageTitle = document.getElementById('pageTitle');
        if (pageTitle) pageTitle.textContent = normalizedTitle;
    }

    window.erpNavigate = async function erpNavigate({
        role,
        view,
        routeParams = [],
        title = '',
        historyMode = 'push',
        container = null,
        sidebarLink = null
    } = {}) {
        const normalizedRole = normalizePathSegment(role);
        let normalizedView = normalizePathSegment(view)
            .replace(/\.html$/i, '');
        const normalizedRouteParams =
            normalizeRouteParams(routeParams);

        if (
            normalizedView === 'dashboard' ||
            normalizedView === 'admin' ||
            normalizedView === 'superadmin'
        ) {
            normalizedView = 'home';
        }

        const targetContainer =
            container ||
            document.getElementById('main-content-area');

        if (typeof window.loadView !== 'function') {
            console.error('Global dynamic view loader is unavailable.');
            return false;
        }

        const loaded = await window.loadView(
            normalizedRole,
            normalizedView,
            targetContainer,
            normalizedRouteParams
        );

        if (!loaded) return false;

        const normalizedTitle =
            String(title || '').trim() ||
            sidebarLink?.textContent?.trim() ||
            'Dashboard';
        const navigationState = {
            role: normalizedRole,
            view: normalizedView,
            routeParams: normalizedRouteParams,
            title: normalizedTitle
        };
        const newUrl = [
            '',
            encodeURIComponent(normalizedRole),
            encodeURIComponent(normalizedView),
            ...normalizedRouteParams.map(value =>
                encodeURIComponent(value)
            )
        ].join('/');

        if (historyMode === 'replace') {
            window.history.replaceState(
                navigationState,
                '',
                newUrl
            );
        } else if (
            historyMode === 'push' &&
            window.location.pathname !== newUrl
        ) {
            window.history.pushState(
                navigationState,
                '',
                newUrl
            );
        }

        updatePageTitle(normalizedTitle);
        updateSidebarSelection(normalizedView, sidebarLink);
        document.body.classList.remove('mobile-sidebar-active');

        document.dispatchEvent(
            new CustomEvent('viewNavigated', {
                detail: navigationState
            })
        );
        return true;
    };

    window.getSidebarViewName = getSidebarViewName;
    window.updateSidebarSelection = updateSidebarSelection;
})();

// ========================================================
// GLOBAL ERP CALENDAR COMPONENT
// ========================================================

/**
 * Factory method for initializing an ERP-grade Flatpickr calendar.
 * @param {string} selector - The CSS selector for the input field.
 * @param {Object} customConfig - Field-specific Flatpickr configurations.
 */
function createErpCalendar(selector, customConfig = {}) {
    if (typeof flatpickr === "undefined") return;
    // -------------------------------------------------------------
    // NEW LOGIC: Inject missing placeholders BEFORE flatpickr initializes
    // -------------------------------------------------------------
    const inputs = document.querySelectorAll(selector);
    inputs.forEach(input => {
        if (!input.hasAttribute('placeholder') || input.getAttribute('placeholder').trim() === '') {
            input.setAttribute('placeholder', 'YYYY-MM-DD');
        }
    });

    const defaultERPConfig = {
        dateFormat: "Y-m-d",
        disableMobile: true, // Force consistent desktop UI
        allowInput: true,    // Allow manual keyboard typing

        // Custom Configuration Properties
        footerActions: ['today', 'clear', 'close'],
        minYear: 1950,
        maxYear: new Date().getFullYear() + 10,

        onReady: function(selectedDates, dateStr, instance) {
            if (instance.calendarContainer) {
                instance.calendarContainer.classList.add('erp-calendar-theme');
            }

            const defaultYearWrapper = instance.currentYearElement.parentNode;
            if (defaultYearWrapper) {
                defaultYearWrapper.style.display = "none";
            }

            buildYearDropdown(instance, this.config.minYear, this.config.maxYear);

            if (this.config.footerActions && this.config.footerActions.length > 0) {
                buildActionFooter(instance, this.config.footerActions);
            }

            attachBlurParser(instance);
        },
        onYearChange: function(selectedDates, dateStr, instance) {
            if (instance.customYearSelect) {
                instance.customYearSelect.value = instance.currentYear.toString();
            }
        },
        onMonthChange: function(selectedDates, dateStr, instance) {
            if (instance.customYearSelect) {
                instance.customYearSelect.value = instance.currentYear.toString();
            }
        }
    };

    const finalConfig = Object.assign({}, defaultERPConfig, customConfig);
    return flatpickr(selector, finalConfig);
}

function buildYearDropdown(instance, minYear, maxYear) {
    const container = document.createElement("div");
    container.className = "erp-year-dropdown-container";

    const toggle = document.createElement("button");
    toggle.type = "button";
    toggle.className = "erp-year-dropdown-toggle";
    toggle.textContent = instance.currentYear.toString();

    const list = document.createElement("ul");
    list.className = "erp-year-dropdown-list";

    for (let y = maxYear; y >= minYear; y--) {
        const li = document.createElement("li");
        li.textContent = y.toString();
        li.setAttribute("data-year", y.toString());

        li.addEventListener("click", function(e) {
            e.stopPropagation();
            e.preventDefault();
            const selectedYear = parseInt(this.getAttribute("data-year"), 10);
            instance.changeYear(selectedYear);
            toggle.textContent = selectedYear.toString();
            list.classList.remove("show");
        });

        list.appendChild(li);
    }

    toggle.addEventListener("click", function(e) {
        e.stopPropagation();
        e.preventDefault();
        document.querySelectorAll('.erp-year-dropdown-list').forEach(el => {
            if (el !== list) el.classList.remove('show');
        });
        list.classList.toggle("show");
        if (list.classList.contains("show")) {
            const activeLi = Array.from(list.children).find(li => li.textContent === instance.currentYear.toString());
            if (activeLi) {
                list.scrollTop = activeLi.offsetTop - (list.offsetHeight / 2) + (activeLi.offsetHeight / 2);
            }
        }
    });

    document.addEventListener("click", function(e) {
        if (!container.contains(e.target)) {
            list.classList.remove("show");
        }
    });

    container.appendChild(toggle);
    container.appendChild(list);
    instance.customYearSelect = toggle;

    Object.defineProperty(instance.customYearSelect, "value", {
        set: function(val) { this.textContent = val; },
        get: function() { return this.textContent; }
    });

    if (instance.monthNav) {
        instance.monthNav.appendChild(container);
    }
}

function buildActionFooter(instance, actions) {
    const footer = document.createElement("div");
    footer.className = "erp-calendar-footer";

    actions.forEach(action => {
        const btn = document.createElement("button");
        btn.type = "button";
        btn.className = "erp-footer-btn erp-btn-" + action;

        if (action === 'today') {
            btn.textContent = "Today";
            btn.addEventListener("click", () => {
                instance.setDate(new Date(), true);
                instance.close();
            });
        } else if (action === 'clear') {
            btn.textContent = "Clear";
            btn.addEventListener("click", () => instance.clear());
        } else if (action === 'close') {
            btn.textContent = "Close";
            btn.addEventListener("click", () => instance.close());
        }
        footer.appendChild(btn);
    });
    instance.calendarContainer.appendChild(footer);
}

function attachBlurParser(instance) {
    instance.input.addEventListener("blur", function(e) {
        const typedVal = e.target.value.trim();
        if (!typedVal) return;
        const euDateMatch = typedVal.match(/^(\d{1,2})[\/\-](\d{1,2})[\/\-](\d{4})$/);
        if (euDateMatch) {
            const day = parseInt(euDateMatch[1], 10);
            const month = parseInt(euDateMatch[2], 10) - 1;
            const year = parseInt(euDateMatch[3], 10);
            const typedDate = new Date(year, month, day);

            if (instance.config.maxDate) {
                const max = new Date(instance.config.maxDate);
                max.setHours(23, 59, 59, 999);
                if (typedDate > max) {
                    if (typeof showAlert !== 'undefined') showAlert('Invalid Date', 'You cannot select a date in the future.', 'warning');
                    instance.setDate(max, true);
                    return;
                }
            }
            instance.setDate(typedDate, true);
        }
    });
}
/**
 * Creates and displays a highly secure, accessibility-compliant Session Timeout Modal.
 * @param {Object} options Configuration object
 * @param {string} options.title Modal title
 * @param {string} options.message Modal description
 * @param {string} options.buttonText Button text
 * @param {string} options.redirectUrl URL to redirect to upon clicking the button
 */
window.showSessionTimeoutModal = function(options) {
    // Prevent duplicate modals
    if (document.getElementById('erp-global-session-overlay')) {
        return;
    }

    const title = options.title || "Session Expired";
    const message = options.message || "Your secure session has expired.";
    const buttonText = options.buttonText || "Login Again";
    const redirectUrl = options.redirectUrl || "/login.html";

    // Lock page scrolling
    document.body.style.overflow = 'hidden';

    // Create DOM Elements safely (No innerHTML string execution for outer elements)
    const overlay = document.createElement('div');
    overlay.id = 'erp-global-session-overlay';
    overlay.className = 'erp-session-modal-overlay';

    const modal = document.createElement('div');
    modal.className = 'erp-session-modal';
    modal.setAttribute('role', 'dialog');
    modal.setAttribute('aria-modal', 'true');
    modal.setAttribute('aria-labelledby', 'erp-session-title');

    const iconWrapper = document.createElement('div');
    iconWrapper.className = 'erp-session-icon';
    iconWrapper.innerHTML = `<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
    </svg>`;

    const titleEl = document.createElement('h2');
    titleEl.id = 'erp-session-title';
    titleEl.className = 'erp-session-title';
    titleEl.textContent = title;

    const messageEl = document.createElement('p');
    messageEl.className = 'erp-session-message';
    messageEl.textContent = message;

    const button = document.createElement('button');
    button.className = 'erp-session-button';
    button.textContent = buttonText;

    // Assemble DOM Tree
    modal.appendChild(iconWrapper);
    modal.appendChild(titleEl);
    modal.appendChild(messageEl);
    modal.appendChild(button);
    overlay.appendChild(modal);
    document.body.appendChild(overlay);

    // Trigger Fade In Animation
    setTimeout(() => {
        overlay.classList.add('erp-session-active');
        button.focus();
    }, 10);

    // Event Listeners for Accessibility & Redirection
    const cleanupAndRedirect = () => {
        document.body.style.overflow = '';
        document.body.removeChild(overlay);
        window.location.href = redirectUrl;
    };

    button.addEventListener('click', cleanupAndRedirect);

    // Strict Focus Trap & Keyboard Handling
    const keydownHandler = (e) => {
        if (e.key === 'Tab') {
            e.preventDefault();
            button.focus();
        }
        if (e.key === 'Escape') {
            e.preventDefault();
            button.focus();
        }
    };

    overlay.addEventListener('keydown', keydownHandler);
};

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
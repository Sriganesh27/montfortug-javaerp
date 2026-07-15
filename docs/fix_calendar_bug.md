# Fix Calendar Dropdown Bug

The reason the calendar isn't opening is because of **when** we initialize it. The code was cloning the form (to wipe out old data/events) *after* initializing Flatpickr. This destroyed Flatpickr's click event listeners!

We just need to initialize the calendar **after** the form is cloned.

### How to fix it:

Open `src/main/resources/static/js/modules/employees.js`.

Find the `initAddEmployeeView()` function (it's near the bottom of the file). **Replace that entire function** with the corrected one below:

```javascript
function initAddEmployeeView() {
    const viewContainer = document.querySelector('#ba-add-employee-view');
    if (!viewContainer) return;

    const oldForm = viewContainer.querySelector('#add-emp-form');
    let form = oldForm;
    
    if (oldForm) {
        form = oldForm.cloneNode(true);
        oldForm.parentNode.replaceChild(form, oldForm);
        form.reset();
        
        // IMPORTANT FIX: Initialize calendars AFTER cloning the form
        if (typeof createErpCalendar === 'function') {
            createErpCalendar('#add-empDob');
            createErpCalendar('#add-empJoiningDate');
        }

        // Set default Joining Date
        const todayStr = new Date().toISOString().split('T')[0];
        const joinInput = viewContainer.querySelector('#add-empJoiningDate');
        if (joinInput._flatpickr) {
            joinInput._flatpickr.setDate(new Date());
        } else {
            joinInput.value = todayStr;
        }
    }

    // SPA NAVIGATION ROUTING FOR "BACK" BUTTON
    const backBtn = viewContainer.querySelector('#backToEmployeesBtn');
    if (backBtn) {
        const newBackBtn = backBtn.cloneNode(true);
        backBtn.parentNode.replaceChild(newBackBtn, backBtn);
        newBackBtn.addEventListener('click', () => {
            const mainContent = document.getElementById('main-content-area');
            window.history.pushState({ view: 'employees', title: 'Manage Employees' }, "", "/admin/employees");
            const pageTitleElement = document.getElementById('pageTitle');
            if (pageTitleElement) pageTitleElement.textContent = "Manage Employees";
            void loadView('admin', 'employees', mainContent);
        });
    }

    if (form) {
        form.addEventListener('submit', async function(e) {
            e.preventDefault();

            const submitBtn = form.querySelector('button[type="submit"]');
            const originalText = submitBtn.innerHTML;
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<i class="bi bi-hourglass-split"></i> Saving...';

            const payload = {
                firstName: viewContainer.querySelector('#add-empFirstName').value,
                lastName: viewContainer.querySelector('#add-empLastName').value,
                email: viewContainer.querySelector('#add-empEmail').value,
                phone: viewContainer.querySelector('#add-empPhone').value,
                category: viewContainer.querySelector('#add-empCategory').value,
                joiningDate: viewContainer.querySelector('#add-empJoiningDate').value,
                dateOfBirth: viewContainer.querySelector('#add-empDob').value || null
            };

            showLoader();
            try {
                const res = await apiPost('/branchadmin/employees', payload);
                showPremiumModal({
                    title: 'Employee Registered',
                    type: 'success',
                    contentText: `Employee ${res.data.firstName} ${res.data.lastName} registered successfully. Generated Code: ${res.data.employeeNo}`,
                    confirmText: 'Done'
                });
                form.reset();
                const joinInputReset = viewContainer.querySelector('#add-empJoiningDate');
                if (joinInputReset._flatpickr) {
                    joinInputReset._flatpickr.setDate(new Date());
                } else {
                    joinInputReset.value = new Date().toISOString().split('T')[0];
                }
            } catch (err) {
                showErrorMessage(err.message || 'Failed to register employee.');
            } finally {
                hideLoader();
                submitBtn.disabled = false;
                submitBtn.innerHTML = originalText;
            }
        });
    }
}
```

Save the file and do a **Hard Refresh (Ctrl + F5)**. The calendar will now pop up instantly when clicked!

# Sync Frontend with Backend Validation

You are absolutely right. The backend has strict constraints requiring **Department**, **Designation**, **Employee Type**, and **Employment Mode** to prevent orphaned records, but our frontend form was missing those fields! 

Here is the exact code to sync the frontend perfectly with the backend constraints.

### 1. Update `add-employee.html`

Replace the entire contents of your `src/main/resources/static/views/admin/add-employee.html` with this updated version that includes the missing fields:

```html
<div id="ba-add-employee-view" class="fade-in">
    <div class="card max-w-2xl mx-auto mt-4">
        <div class="view-header p-3 border-bottom">
            <h2 class="mb-0"><i class="bi bi-person-plus me-2"></i>Register New Employee</h2>
            <button id="backToEmployeesBtn" class="btn-secondary btn-sm"><i class="bi bi-arrow-left"></i> Back</button>
        </div>
        <div class="card-body p-4">
            <form id="add-emp-form">
                
                <div class="detail-grid mb-3" style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px;">
                    <div class="form-group">
                        <label class="detail-text text-strong d-block mb-1">First Name <span class="text-danger">*</span></label>
                        <input type="text" id="add-empFirstName" class="detail-input w-100" required placeholder="John">
                    </div>
                    <div class="form-group">
                        <label class="detail-text text-strong d-block mb-1">Last Name <span class="text-danger">*</span></label>
                        <input type="text" id="add-empLastName" class="detail-input w-100" required placeholder="Doe">
                    </div>
                </div>

                <div class="detail-grid mb-3" style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px;">
                    <div class="form-group">
                        <label class="detail-text text-strong d-block mb-1">Email <span class="text-danger">*</span></label>
                        <input type="email" id="add-empEmail" class="detail-input w-100" required placeholder="johndoe@school.edu">
                    </div>
                    <div class="form-group">
                        <label class="detail-text text-strong d-block mb-1">Mobile No <span class="text-danger">*</span></label>
                        <input type="text" id="add-empPhone" class="detail-input w-100" required placeholder="07XX XXX XXX">
                    </div>
                </div>

                <div class="detail-grid mb-3" style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px;">
                    <div class="form-group">
                        <label class="detail-text text-strong d-block mb-1">Department <span class="text-danger">*</span></label>
                        <select id="add-empDepartment" class="detail-input w-100" required>
                            <option value="">-- Loading --</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label class="detail-text text-strong d-block mb-1">Designation <span class="text-danger">*</span></label>
                        <select id="add-empDesignation" class="detail-input w-100" required>
                            <option value="">-- Loading --</option>
                        </select>
                    </div>
                </div>

                <div class="detail-grid mb-3" style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 15px;">
                    <div class="form-group">
                        <label class="detail-text text-strong d-block mb-1">Category <span class="text-danger">*</span></label>
                        <select id="add-empCategory" class="detail-input w-100" required>
                            <option value="TEACHING">Teaching</option>
                            <option value="NON_TEACHING">Non-Teaching</option>
                            <option value="MANAGEMENT">Management</option>
                            <option value="SUPPORT_STAFF">Support Staff</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label class="detail-text text-strong d-block mb-1">Type <span class="text-danger">*</span></label>
                        <select id="add-empType" class="detail-input w-100" required>
                            <option value="PERMANENT">Permanent</option>
                            <option value="CONTRACT">Contract</option>
                            <option value="TEMPORARY">Temporary</option>
                            <option value="PART_TIME">Part Time</option>
                            <option value="INTERN">Intern</option>
                            <option value="VOLUNTEER">Volunteer</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label class="detail-text text-strong d-block mb-1">Mode <span class="text-danger">*</span></label>
                        <select id="add-empMode" class="detail-input w-100" required>
                            <option value="FULL_TIME">Full Time</option>
                            <option value="PART_TIME">Part Time</option>
                            <option value="REMOTE">Remote</option>
                            <option value="ON_CALL">On Call</option>
                        </select>
                    </div>
                </div>

                <div class="detail-grid mb-4" style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px;">
                    <div class="form-group">
                        <label class="detail-text text-strong d-block mb-1">Joining Date <span class="text-danger">*</span></label>
                        <input type="date" id="add-empJoiningDate" class="detail-input w-100" required>
                    </div>
                    <div class="form-group">
                        <label class="detail-text text-strong d-block mb-1">Date of Birth <span class="text-danger">*</span></label>
                        <input type="date" id="add-empDob" class="detail-input w-100" required>
                    </div>
                </div>

                <div class="text-end border-top pt-3">
                    <button type="submit" class="btn-primary px-4"><i class="bi bi-check-lg"></i> Register Employee</button>
                </div>
            </form>
        </div>
    </div>
</div>
```

---

### 2. Update `employees.js`

In your `src/main/resources/static/js/modules/employees.js`, entirely replace your `initAddEmployeeView()` function (at the bottom) with this one. It automatically fetches the Departments and Designations from the backend and includes the missing fields in the payload:

```javascript
function initAddEmployeeView() {
    const viewContainer = document.querySelector('#ba-add-employee-view');
    if (!viewContainer) return;

    // Dynamically fetch Departments & Designations to populate the selects
    async function loadAddSelectOptions() {
        try {
            const branchId = parseInt(localStorage.getItem('user_branch')) || 1;
            const [deptRes, designationRes] = await Promise.all([
                apiGet(`/departments?branchId=${branchId}&size=100`),
                apiGet(`/designations?branchId=${branchId}&size=100`)
            ]);
            
            const deptSelect = viewContainer.querySelector('#add-empDepartment');
            const designationSelect = viewContainer.querySelector('#add-empDesignation');
            
            if(deptSelect) {
                deptSelect.innerHTML = '<option value="">-- Select Department --</option>';
                deptRes.data.content.forEach(d => {
                    deptSelect.innerHTML += `<option value="${d.departmentId}">${d.departmentName}</option>`;
                });
            }
            if(designationSelect) {
                designationSelect.innerHTML = '<option value="">-- Select Designation --</option>';
                designationRes.data.content.forEach(d => {
                    designationSelect.innerHTML += `<option value="${d.designationId}">${d.designationName}</option>`;
                });
            }
        } catch (e) {
            console.warn("Could not load departments/designations", e);
        }
    }

    const oldForm = viewContainer.querySelector('#add-emp-form');
    let form = oldForm;
    if (oldForm) {
        form = oldForm.cloneNode(true);
        oldForm.parentNode.replaceChild(form, oldForm);
        form.reset();
        
        // Initialize calendars AFTER cloning the form
        if (typeof createErpCalendar === 'function') {
            const today = new Date();
            const maxDobDate = new Date(today.getFullYear() - 18, today.getMonth(), today.getDate());

            createErpCalendar('#add-empDob', {
                maxDate: maxDobDate,
                defaultDate: maxDobDate
            });
            createErpCalendar('#add-empJoiningDate');
        }

        // Default Joining Date
        const todayStr = new Date().toISOString().split('T')[0];
        const joinInput = viewContainer.querySelector('#add-empJoiningDate');
        if (joinInput._flatpickr) {
            joinInput._flatpickr.setDate(new Date());
        } else {
            joinInput.value = todayStr;
        }

        // Populate dropdowns!
        void loadAddSelectOptions();
    }

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

            const dobVal = viewContainer.querySelector('#add-empDob').value;
            if (!dobVal) {
                showErrorMessage('Date of Birth is required.');
                return;
            }

            const birthDate = new Date(dobVal);
            const today = new Date();
            let age = today.getFullYear() - birthDate.getFullYear();
            const m = today.getMonth() - birthDate.getMonth();
            if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) {
                age--;
            }

            if (age < 18) { 
                showErrorMessage('Employee must be at least 18 years old.');
                return;
            }

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
                dateOfBirth: dobVal,
                // --- NEW REQUIRED FIELDS ---
                departmentId: viewContainer.querySelector('#add-empDepartment').value,
                designationId: viewContainer.querySelector('#add-empDesignation').value,
                employeeType: viewContainer.querySelector('#add-empType').value,
                employmentMode: viewContainer.querySelector('#add-empMode').value
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

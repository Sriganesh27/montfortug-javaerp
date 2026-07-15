# Add Age Validation (Minimum 18 Years)

The backend is already configured to block any employee registration if they are under 18 years old. However, we should also enforce this on the **frontend** so the user gets immediate feedback before the form even submits!

### 1. Make Date of Birth Required

First, open your `src/main/resources/static/views/admin/add-employee.html` file and make the Date of Birth field mandatory by adding `required`.

Find this block:
```html
                <div class="form-group mb-4">
                    <label class="detail-text text-strong d-block mb-1">Date of Birth</label>
                    <input type="date" id="add-empDob" class="detail-input w-100">
                </div>
```

Change it to:
```html
                <div class="form-group mb-4">
                    <label class="detail-text text-strong d-block mb-1">Date of Birth <span class="text-danger">*</span></label>
                    <input type="date" id="add-empDob" class="detail-input w-100" required>
                </div>
```

---

### 2. Add UI Validation Check

Next, open your `src/main/resources/static/js/modules/employees.js`.

Inside `initAddEmployeeView()`, locate the `form.addEventListener('submit', async function(e) { ... }` block. We need to add a quick age check right at the beginning before it hits the API.

Update the form submit listener to look like this:

```javascript
    if (form) {
        form.addEventListener('submit', async function(e) {
            e.preventDefault();

            // --- NEW AGE VALIDATION ---
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

            if (age < 18) { // You can change this to 20 if your school requires 20
                showErrorMessage('Employee must be at least 18 years old.');
                return;
            }
            // --------------------------

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
                dateOfBirth: dobVal
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
```

This ensures the user gets a clean, immediate error modal if they try to enter someone under 18!

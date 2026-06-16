document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    const errorMessage = document.getElementById('errorMessage');
    const passwordInput = document.getElementById('password');
    const loginBtn = document.getElementById('loginBtn');
    const btnText = loginBtn ? loginBtn.querySelector('.btn-text') : null;

    const roleSelect = document.getElementById('role');
    const campusSelect = document.getElementById('branchId');
    const campusGroup = document.getElementById('campusGroup');

    // Hide Campus Selection for Super Admin (Pure JS, no inline CSS)
    if (roleSelect && campusSelect && campusGroup) {
        roleSelect.addEventListener('change', function() {
            if (this.value === 'SUPER_ADMIN' || this.value === 'ROLE_SUPER_ADMIN') {
                campusGroup.classList.add('hidden');
                campusSelect.removeAttribute('required');
                campusSelect.value = '';
            } else {
                campusGroup.classList.remove('hidden');
                campusSelect.setAttribute('required', 'required');
            }
        });
    }

    // Handle Form Submission
    if (loginForm) {
        loginForm.addEventListener('submit', async function(e) {
            e.preventDefault();

            // Clear previous alerts (Pure JS)
            if (errorMessage) {
                errorMessage.className = 'error-box hidden';
                errorMessage.textContent = '';
            }

            // Button loading state (Pure text injection)
            const originalText = btnText ? btnText.textContent : 'Login';
            if (btnText) btnText.textContent = 'Authenticating...';
            loginBtn.disabled = true;

            const role = roleSelect.value;
            const branchId = campusSelect.value;
            const username = document.getElementById('username').value;
            const password = passwordInput.value;

            const finalRole = role.startsWith('ROLE_') ? role : 'ROLE_' + role;
            const finalBranchId = branchId ? parseInt(branchId) : null;

            try {
                const response = await fetch('/api/auth/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        username: username,
                        password: password,
                        role: finalRole,
                        branchId: finalBranchId
                    })
                });

                const data = await response.json();

                // We only check response.ok now, because the token is invisible!
                if (response.ok) {
                    let rawRole = data.role ? data.role.toUpperCase().replace(/\s+/g, '_') : '';
                    let finalRoleString = rawRole;

                    // Catch ALL variations of Super Admin
                    if (rawRole === 'SUPER_USER' || rawRole === 'ROLE_SUPER_ADMIN' || rawRole === 'SUPER_ADMIN') {
                        finalRoleString = 'SUPER_ADMIN';
                    }

                    // We MUST keep saving these two so the UI knows which sidebar to show!
                    localStorage.setItem('user_role', finalRoleString);
                    localStorage.setItem('username', username);

                    if (data.branchId) {
                        localStorage.setItem('user_branch', data.branchId);
                    }

                    // Success Alert (Pure CSS Classes)
                    if (errorMessage) {
                        errorMessage.className = 'error-box alert-success';
                        errorMessage.textContent = 'Login successful! Redirecting...';
                    }

                    setTimeout(() => {
                        if (finalRoleString === 'SUPER_ADMIN') {
                            window.location.href = '/superadmin';
                        } else {
                            window.location.href = '/dashboard';
                        }
                    }, 1000);

                } else {
                    // API Error Alert (Pure CSS Classes)
                    if (errorMessage) {
                        errorMessage.className = 'error-box alert-error';
                        errorMessage.textContent = data.message || 'Invalid credentials. Please try again.';
                    }
                }
            } catch (error) {
                console.error('Login error:', error);
                // System Error Alert (Pure CSS Classes)
                if (errorMessage) {
                    errorMessage.className = 'error-box alert-error';
                    errorMessage.textContent = 'Unable to connect to the server. Please check your connection.';
                }
            } finally {
                // Restore button state
                if (btnText) btnText.textContent = originalText;
                loginBtn.disabled = false;
            }
        });
    }
});
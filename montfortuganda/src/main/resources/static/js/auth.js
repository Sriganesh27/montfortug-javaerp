document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    const errorMessage = document.getElementById('errorMessage');
    const passwordInput = document.getElementById('password');
    const loginBtn = document.getElementById('loginBtn');
    const btnText = loginBtn ? loginBtn.querySelector('.btn-text') : null;

    const roleSelect = document.getElementById('role');
    const campusSelect = document.getElementById('branchId');
    const campusGroup = document.getElementById('campusGroup');

    // Hide Campus Selection for Super Admin
    if (roleSelect && campusSelect && campusGroup) {
        roleSelect.addEventListener('change', function() {
            // Check if Super Admin is selected
            if (this.value === 'SUPER_ADMIN' || this.value === 'ROLE_SUPER_ADMIN') {
                campusGroup.style.display = 'none';
                campusSelect.removeAttribute('required');
                campusSelect.value = ''; // clear selection
            } else {
                campusGroup.style.display = 'block';
                campusSelect.setAttribute('required', 'required');
            }
        });
    }

    // Handle Form Submission
    if (loginForm) {
        loginForm.addEventListener('submit', async function(e) {
            e.preventDefault();

            // Clear previous alerts
            if (errorMessage) {
                errorMessage.style.display = 'none';
                errorMessage.textContent = '';
            }

            // Button loading state
            const originalText = btnText ? btnText.innerHTML : 'Login';
            if (btnText) btnText.innerHTML = 'Authenticating...';
            loginBtn.disabled = true;

            const role = roleSelect.value;
            const branchId = campusSelect.value;
            const username = document.getElementById('username').value;
            const password = passwordInput.value;

            // Ensure the backend receives the 'ROLE_' prefix it expects
            const finalRole = role.startsWith('ROLE_') ? role : 'ROLE_' + role;

            // Parse branchId to integer, or send null if it's a Super Admin
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

                if (response.ok && data.token) {
                    // Success! Save token and role to LocalStorage
                    localStorage.setItem('jwt_token', data.token);
                    localStorage.setItem('user_role', data.role);
                    if (data.branchId) {
                        localStorage.setItem('user_branch', data.branchId);
                    }

                    if (errorMessage) {
                        errorMessage.style.display = 'block';
                        errorMessage.style.backgroundColor = 'rgba(34, 197, 94, 0.2)'; // success green
                        errorMessage.style.color = '#fff';
                        errorMessage.style.border = '1px solid #22c55e';
                        errorMessage.style.padding = '10px';
                        errorMessage.textContent = 'Login successful! Redirecting...';
                    }

                    // Redirect based on role
                    setTimeout(() => {
                        if (data.role === 'ROLE_SUPER_ADMIN') {
                            window.location.href = '/superadmin.html';
                        } else {
                            window.location.href = '/dashboard.html';
                        }
                    }, 1000);

                } else {
                    // Show API error message
                    if (errorMessage) {
                        errorMessage.style.display = 'block';
                        errorMessage.style.backgroundColor = 'rgba(239, 68, 68, 0.2)'; // error red
                        errorMessage.style.color = '#fff';
                        errorMessage.style.border = '1px solid #ef4444';
                        errorMessage.style.padding = '10px';
                        errorMessage.textContent = data.message || 'Invalid credentials. Please try again.';
                    }
                }
            } catch (error) {
                console.error('Login error:', error);
                if (errorMessage) {
                    errorMessage.style.display = 'block';
                    errorMessage.style.backgroundColor = 'rgba(239, 68, 68, 0.2)';
                    errorMessage.style.color = '#fff';
                    errorMessage.style.border = '1px solid #ef4444';
                    errorMessage.style.padding = '10px';
                    errorMessage.textContent = 'Unable to connect to the server. Please check your connection.';
                }
            } finally {
                // Restore button state
                if (btnText) btnText.innerHTML = originalText;
                loginBtn.disabled = false;
            }
        });
    }
});
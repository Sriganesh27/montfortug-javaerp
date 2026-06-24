document.addEventListener('DOMContentLoaded', function() {
    // Both pages use the same script, but have different form IDs
    const loginForm = document.getElementById('loginForm');           // The Public Form
    const secureForm = document.getElementById('secureAuthForm');     // The Secret Admin Form

    // We attach the listener to whichever form exists on the current page
    const activeForm = loginForm || secureForm;

    const errorMessage = document.getElementById('errorMessage');
    const passwordInput = document.getElementById('password');
    const loginBtn = document.getElementById('loginBtn');
    const btnText = loginBtn ? loginBtn.querySelector('.btn-text') : null;

    // 1. Password Visibility Toggle Logic
    const togglePassword = document.getElementById('toggle-password');
    const eyeIcon = document.getElementById('eye-icon');

    if (togglePassword && eyeIcon) {
        togglePassword.addEventListener('click', function() {
            if (passwordInput.type === 'password') {
                passwordInput.type = 'text';
                eyeIcon.className = 'bi bi-eye';
            } else {
                passwordInput.type = 'password';
                eyeIcon.className = 'bi bi-eye-slash';
            }
        });
    }

    // 2. Handle Form Submission
    if (activeForm) {
        activeForm.addEventListener('submit', async function(e) {
            e.preventDefault();

            // Clear previous alerts
            if (errorMessage) {
                errorMessage.className = 'error-box hidden';
                errorMessage.textContent = '';
            }

            // --- CSP-SAFE PUBLIC LOGIN BLOCKER ---
            // If they are using the public login.html, stop them and show the error!
            if (activeForm.id === 'loginForm') {
                if (errorMessage) {
                    errorMessage.className = 'error-box alert-error';
                    errorMessage.textContent = 'The Student and Parent portals are currently under construction. Please check back later!';
                }
                return; // Stop right here! Do not call the Java backend.
            }
            // -------------------------------------

            // If they made it past the blocker, they must be on mbsg-auth.html!
            const originalText = btnText ? btnText.textContent : 'Login';
            if (btnText) btnText.textContent = 'Authenticating...';
            loginBtn.disabled = true;

            const username = document.getElementById('username').value;
            const password = passwordInput.value;

            try {
                const response = await fetch('/api/auth/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        username: username,
                        password: password,
                        role: "SECURE_ADMIN_GATEWAY" // Send the secret flag to Java!
                    })
                });

                const data = await response.json();

                if (response.ok) {
                    let rawRole = data.role ? data.role.toUpperCase().replace(/\s+/g, '_') : '';
                    let finalRoleString = rawRole;

                    if (rawRole === 'SUPER_USER' || rawRole === 'ROLE_SUPER_ADMIN' || rawRole === 'SUPER_ADMIN') {
                        finalRoleString = 'SUPER_ADMIN';
                    }

                    // Save to localStorage
                    localStorage.setItem('user_role', finalRoleString);
                    localStorage.setItem('username', username);
                    if (data.branchId) {
                        localStorage.setItem('user_branch', data.branchId);
                    }

                    if (errorMessage) {
                        errorMessage.className = 'error-box alert-success';
                        errorMessage.textContent = 'Authorized! Redirecting...';
                    }

                    setTimeout(() => {
                        if (finalRoleString === 'SUPER_ADMIN') {
                            window.location.href = '/superadmin';
                        } else {
                            window.location.href = '/dashboard';
                        }
                    }, 1000);

                } else {
                    if (errorMessage) {
                        errorMessage.className = 'error-box alert-error';
                        errorMessage.textContent = data.message || 'Invalid credentials. Please try again.';
                    }
                }
            } catch (error) {
                if (errorMessage) {
                    errorMessage.className = 'error-box alert-error';
                    errorMessage.textContent = 'Unable to connect to the server.';
                }
            } finally {
                if (btnText) btnText.textContent = originalText;
                loginBtn.disabled = false;
            }
        });
    }
});
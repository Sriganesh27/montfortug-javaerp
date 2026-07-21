document.addEventListener('DOMContentLoaded', () => {
    const publicLoginForm =
        document.getElementById('loginForm');

    const secureLoginForm =
        document.getElementById('secureAuthForm');

    const activeLoginForm =
        publicLoginForm || secureLoginForm;

    const loginPanel =
        document.getElementById('loginPanel');

    const temporaryPasswordPanel =
        document.getElementById(
            'temporaryPasswordPanel'
        );

    const passwordChangeForm =
        document.getElementById(
            'temporaryPasswordChangeForm'
        );

    const loginMessage =
        document.getElementById('errorMessage');

    const passwordChangeMessage =
        document.getElementById(
            'passwordChangeMessage'
        );

    const usernameInput =
        document.getElementById('username');

    const passwordInput =
        document.getElementById('password');

    const newPasswordInput =
        document.getElementById('newPassword');

    const confirmPasswordInput =
        document.getElementById(
            'confirmPassword'
        );

    const loginButton =
        document.getElementById('loginBtn');

    const changePasswordButton =
        document.getElementById(
            'changePasswordBtn'
        );

    const backToLoginButton =
        document.getElementById(
            'backToLoginBtn'
        );

    const expiryElement =
        document.getElementById(
            'temporaryPasswordExpiry'
        );

    const currentYearElement =
        document.getElementById(
            'currentYear'
        );

    let passwordChangeToken = null;
    let temporaryPassword = null;

    if (currentYearElement) {
        currentYearElement.textContent =
            String(new Date().getFullYear());
    }

    function showMessage(
        element,
        message,
        type = 'error'
    ) {
        if (!element) {
            return;
        }

        element.className =
            type === 'success'
                ? 'error-box alert-success'
                : 'error-box alert-error';

        element.textContent = message;
    }

    function clearMessage(element) {
        if (!element) {
            return;
        }

        element.className =
            'error-box hidden';

        element.textContent = '';
    }

    function setButtonLoading(
        button,
        loadingText,
        loading
    ) {
        if (!button) {
            return '';
        }

        const textElement =
            button.querySelector('.btn-text');

        const originalText =
            textElement
                ? textElement.textContent
                : button.textContent;

        button.disabled = loading;

        if (loading) {
            if (textElement) {
                textElement.textContent =
                    loadingText;
            } else {
                button.textContent =
                    loadingText;
            }
        }

        return originalText;
    }

    function restoreButtonText(
        button,
        originalText
    ) {
        if (!button) {
            return;
        }

        const textElement =
            button.querySelector('.btn-text');

        if (textElement) {
            textElement.textContent =
                originalText;
        } else {
            button.textContent =
                originalText;
        }

        button.disabled = false;
    }

    async function readJsonResponse(response) {
        const responseText =
            await response.text();

        if (!responseText) {
            return {};
        }

        try {
            return JSON.parse(responseText);
        } catch (error) {
            console.error(
                'Invalid JSON response:',
                error
            );

            return {};
        }
    }

    function normalizeRole(role) {
        if (!role) {
            return '';
        }

        let normalizedRole =
            String(role)
                .trim()
                .toUpperCase()
                .replace(/\s+/g, '_');

        while (
            normalizedRole.startsWith('ROLE_')
        ) {
            normalizedRole =
                normalizedRole.substring(5);
        }

        if (normalizedRole === 'SUPER_USER') {
            return 'SUPER_ADMIN';
        }

        if (normalizedRole === 'SCHOOL_ADMIN') {
            return 'BRANCH_ADMIN';
        }

        return normalizedRole;
    }

    function saveAuthenticatedUser(
        responseData,
        username
    ) {
        const role =
            normalizeRole(responseData.role);

        localStorage.setItem(
            'user_role',
            role
        );

        localStorage.setItem(
            'username',
            username
        );

        if (
            responseData.branchId !== null
                    && responseData.branchId !== undefined
        ) {
            localStorage.setItem(
                'user_branch',
                String(responseData.branchId)
            );
        } else {
            localStorage.removeItem(
                'user_branch'
            );
        }

        return role;
    }

    function redirectAuthenticatedUser(role) {
        window.setTimeout(() => {
            window.location.href =
                role === 'SUPER_ADMIN'
                    ? '/superadmin'
                    : '/dashboard';
        }, 900);
    }

    function formatExpiry(expiryValue) {
        if (!expiryValue) {
            return null;
        }

        const includesTimezone =
            /(?:Z|[+-]\d{2}:\d{2})$/.test(
                expiryValue
            );

        const normalizedValue =
            includesTimezone
                ? expiryValue
                : `${expiryValue}Z`;

        const expiryDate =
            new Date(normalizedValue);

        if (Number.isNaN(expiryDate.getTime())) {
            return String(expiryValue);
        }

        return expiryDate.toLocaleString(
            undefined,
            {
                dateStyle: 'medium',
                timeStyle: 'short'
            }
        );
    }

    function showPasswordChangePanel(
        responseData,
        currentPassword
    ) {
        passwordChangeToken =
            responseData.passwordChangeToken;

        temporaryPassword =
            currentPassword;

        if (!passwordChangeToken) {
            showMessage(
                loginMessage,
                'The password-change token was not returned. Contact the ERP administrator.'
            );
            return;
        }

        clearMessage(loginMessage);
        clearMessage(passwordChangeMessage);

        loginPanel?.classList.add('hidden');

        temporaryPasswordPanel
            ?.classList.remove('hidden');

        const formattedExpiry =
            formatExpiry(
                responseData
                    .temporaryPasswordExpiresAt
            );

        if (expiryElement) {
            expiryElement.textContent =
                formattedExpiry
                    ? `Credentials expire on ${formattedExpiry} (your local time).`
                    : 'These temporary credentials are valid for 72 hours.';
        }

        if (newPasswordInput) {
            newPasswordInput.value = '';
            newPasswordInput.focus();
        }

        if (confirmPasswordInput) {
            confirmPasswordInput.value = '';
        }
    }

    function returnToLogin(
        successMessage = null
    ) {
        passwordChangeToken = null;
        temporaryPassword = null;

        passwordChangeForm?.reset();

        temporaryPasswordPanel
            ?.classList.add('hidden');

        loginPanel?.classList.remove('hidden');

        clearMessage(passwordChangeMessage);

        if (passwordInput) {
            passwordInput.value = '';
            passwordInput.focus();
        }

        if (successMessage) {
            showMessage(
                loginMessage,
                successMessage,
                'success'
            );
        } else {
            clearMessage(loginMessage);
        }
    }

    function validateNewPassword(
        newPassword,
        confirmedPassword
    ) {
        if (
            newPassword.length < 8
                    || newPassword.length > 100
        ) {
            return 'Password must contain between 8 and 100 characters.';
        }

        if (!/[A-Z]/.test(newPassword)) {
            return 'Password must contain at least one uppercase letter.';
        }

        if (!/[a-z]/.test(newPassword)) {
            return 'Password must contain at least one lowercase letter.';
        }

        if (!/\d/.test(newPassword)) {
            return 'Password must contain at least one number.';
        }

        if (!/[^A-Za-z0-9]/.test(newPassword)) {
            return 'Password must contain at least one special character.';
        }

        if (newPassword !== confirmedPassword) {
            return 'New password and confirmation password do not match.';
        }

        if (newPassword === temporaryPassword) {
            return 'New password must be different from the temporary password.';
        }

        return null;
    }

    function bindPasswordToggle(
        toggleElement,
        fallbackTargetId = null
    ) {
        if (
            !toggleElement
                    || toggleElement.dataset.passwordToggleBound === 'true'
        ) {
            return;
        }

        const targetId =
            toggleElement.dataset.target
                    || fallbackTargetId;

        const targetInput =
            targetId
                ? document.getElementById(targetId)
                : null;

        if (!targetInput) {
            console.warn(
                'Password toggle target was not found:',
                targetId
            );
            return;
        }

        toggleElement.dataset.passwordToggleBound =
            'true';

        toggleElement.addEventListener(
            'click',
            event => {
                event.preventDefault();
                event.stopPropagation();

                const showPassword =
                    targetInput.type === 'password';

                targetInput.type =
                    showPassword
                        ? 'text'
                        : 'password';

                const icon =
                    toggleElement.querySelector('i')
                    || (
                        toggleElement.id === 'toggle-password'
                            ? document.getElementById('eye-icon')
                            : null
                    );

                if (icon) {
                    icon.className =
                        showPassword
                            ? 'bi bi-eye'
                            : 'bi bi-eye-slash';
                }

                toggleElement.setAttribute(
                    'aria-label',
                    showPassword
                        ? 'Hide password'
                        : 'Show password'
                );

                toggleElement.setAttribute(
                    'aria-pressed',
                    String(showPassword)
                );

                targetInput.focus({
                    preventScroll: true
                });
            }
        );
    }

    document
        .querySelectorAll('[data-password-toggle]')
        .forEach(toggleElement => {
            bindPasswordToggle(toggleElement);
        });

    /*
     * Backward compatibility for the earlier login HTML that used:
     * <span id="toggle-password"><i id="eye-icon"></i></span>
     */
    bindPasswordToggle(
        document.getElementById('toggle-password'),
        'password'
    );

    if (activeLoginForm) {
        activeLoginForm.addEventListener(
            'submit',
            async event => {
                event.preventDefault();
                clearMessage(loginMessage);

                if (
                    activeLoginForm.id ===
                    'loginForm'
                ) {
                    showMessage(
                        loginMessage,
                        'The Student and Parent portals are currently under construction. Please check back later!'
                    );
                    return;
                }

                if (
                    !usernameInput
                            || !passwordInput
                            || !loginButton
                ) {
                    showMessage(
                        loginMessage,
                        'The login form is incomplete. Refresh the page.'
                    );
                    return;
                }

                const username =
                    usernameInput.value.trim();

                const password =
                    passwordInput.value;

                if (!username || !password) {
                    showMessage(
                        loginMessage,
                        'Username and password are required.'
                    );
                    return;
                }

                const originalText =
                    setButtonLoading(
                        loginButton,
                        'Authenticating...',
                        true
                    );

                try {
                    const response =
                        await fetch(
                            '/api/auth/login',
                            {
                                method: 'POST',
                                credentials: 'same-origin',
                                headers: {
                                    'Content-Type':
                                        'application/json'
                                },
                                body: JSON.stringify({
                                    username,
                                    password,
                                    role:
                                        'SECURE_ADMIN_GATEWAY'
                                })
                            }
                        );

                    const responseData =
                        await readJsonResponse(
                            response
                        );

                    if (
                        responseData.status ===
                        'PASSWORD_CHANGE_REQUIRED'
                    ) {
                        showPasswordChangePanel(
                            responseData,
                            password
                        );
                        return;
                    }

                    if (!response.ok) {
                        throw new Error(
                            responseData.message
                                || 'Invalid credentials. Please try again.'
                        );
                    }

                    if (
                        responseData.status
                                && responseData.status
                                !== 'AUTHENTICATED'
                    ) {
                        throw new Error(
                            responseData.message
                                || 'Login could not be completed.'
                        );
                    }

                    const role =
                        saveAuthenticatedUser(
                            responseData,
                            username
                        );

                    showMessage(
                        loginMessage,
                        'Login successful. Redirecting...',
                        'success'
                    );

                    redirectAuthenticatedUser(
                        role
                    );

                } catch (error) {
                    console.error(
                        'Login failed:',
                        error
                    );

                    showMessage(
                        loginMessage,
                        error.message
                            || 'Unable to connect to the server.'
                    );

                } finally {
                    restoreButtonText(
                        loginButton,
                        originalText
                    );
                }
            }
        );
    }

    if (passwordChangeForm) {
        passwordChangeForm.addEventListener(
            'submit',
            async event => {
                event.preventDefault();
                clearMessage(passwordChangeMessage);

                if (
                    !newPasswordInput
                            || !confirmPasswordInput
                            || !changePasswordButton
                ) {
                    showMessage(
                        passwordChangeMessage,
                        'The password-change form is incomplete.'
                    );
                    return;
                }

                if (
                    !passwordChangeToken
                            || !temporaryPassword
                ) {
                    showMessage(
                        passwordChangeMessage,
                        'The temporary login session is missing. Return to login and try again.'
                    );
                    return;
                }

                const newPassword =
                    newPasswordInput.value;

                const confirmPassword =
                    confirmPasswordInput.value;

                const validationError =
                    validateNewPassword(
                        newPassword,
                        confirmPassword
                    );

                if (validationError) {
                    showMessage(
                        passwordChangeMessage,
                        validationError
                    );
                    return;
                }

                const originalText =
                    setButtonLoading(
                        changePasswordButton,
                        'Changing Password...',
                        true
                    );

                if (backToLoginButton) {
                    backToLoginButton.disabled = true;
                }

                try {
                    const response =
                        await fetch(
                            '/api/auth/change-temporary-password',
                            {
                                method: 'POST',
                                credentials: 'same-origin',
                                headers: {
                                    'Content-Type':
                                        'application/json',
                                    Authorization:
                                        `Bearer ${passwordChangeToken}`
                                },
                                body: JSON.stringify({
                                    currentPassword:
                                        temporaryPassword,
                                    newPassword,
                                    confirmPassword
                                })
                            }
                        );

                    const responseData =
                        await readJsonResponse(
                            response
                        );

                    if (!response.ok) {
                        throw new Error(
                            responseData.message
                                || 'The password could not be changed.'
                        );
                    }

                    returnToLogin(
                        responseData.message
                            || 'Password changed successfully. Log in using your new password.'
                    );

                } catch (error) {
                    console.error(
                        'Password change failed:',
                        error
                    );

                    showMessage(
                        passwordChangeMessage,
                        error.message
                            || 'Unable to change the temporary password.'
                    );

                } finally {
                    restoreButtonText(
                        changePasswordButton,
                        originalText
                    );

                    if (backToLoginButton) {
                        backToLoginButton.disabled =
                            false;
                    }
                }
            }
        );
    }

    backToLoginButton?.addEventListener(
        'click',
        () => returnToLogin()
    );
});

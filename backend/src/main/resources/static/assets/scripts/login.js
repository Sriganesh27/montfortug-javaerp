// erp/assets/scripts/login.js

document.addEventListener('DOMContentLoaded', function() {
    const roleSelect = document.querySelector('select[name="role"]');
    const branchGroup = document.getElementById('branchGroup');
    const branchSelect = document.querySelector('select[name="branch_id"]');
    const loginForm = document.getElementById('loginForm');
    const messageDiv = document.getElementById('message');


    // --- 2. Login Submission Logic ---
    loginForm.addEventListener('submit', function(e) {
        e.preventDefault();
        const formData = new FormData(this);

        // Reset visibility
        messageDiv.style.display = 'none';

        const payload = Object.fromEntries(formData.entries());

        fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        })
        .then(async response => {
            const data = await response.json().catch(() => ({ message: 'Server error' }));
            if (!response.ok) {
                throw new Error(data.message || 'Invalid credentials');
            }
            return data;
        })
        .then(data => {
            messageDiv.style.display = 'block'; // Make it visible
            if (data.token) {
                // Save JWT to local storage
                localStorage.setItem('jwtToken', data.token);
                messageDiv.style.color = '#155724';
                messageDiv.style.backgroundColor = '#d4edda';
                messageDiv.textContent = "Redirecting...";
                // Redirect logic based on role could go here
                window.location.href = '/dashboard'; 
            } else {
                messageDiv.style.color = '#721c24';
                messageDiv.style.backgroundColor = '#f8d7da';
                messageDiv.textContent = data.message || "Invalid credentials";
            }
        })
        .catch(error => {
            console.error('Error:', error);
            messageDiv.style.display = 'block';
            messageDiv.style.color = '#721c24';
            messageDiv.style.backgroundColor = '#f8d7da';
            messageDiv.textContent = error.message || "Error connecting to server.";
        });
    });
});
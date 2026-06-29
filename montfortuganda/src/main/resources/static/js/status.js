document.getElementById('statusForm').addEventListener('submit', function(e) {
    e.preventDefault();

    const btn = document.getElementById('searchBtn');
    const btnText = document.getElementById('searchBtnText');
    const btnIcon = document.getElementById('searchBtnIcon');
    const errorDiv = document.getElementById('errorMessage');
    const resultCard = document.getElementById('resultCard');
    const refInput = document.getElementById('ref_number').value.trim();

    // --- SECURITY: HONEYPOT INTERCEPTOR ---
    // If a bot fills out the hidden fax_number field, silently kill the request
    const honeypot = document.getElementById('fax_number_trap');
    if (honeypot && honeypot.value.trim() !== "") {
        console.warn("Bot detected by honeypot. Request killed.");
        // Fake an error to the bot
        errorDiv.textContent = "Application not found.";
        errorDiv.classList.remove('hidden-element');
        return;
    }

    // --- SECURITY: BUTTON THROTTLING ---
    // Strict separation: No innerHTML, manipulate classes and textContent
    btnText.textContent = 'Searching...';
    btnIcon.classList.add('hidden-element');
    btn.disabled = true; // Button disabled instantly to prevent double-submit
    errorDiv.classList.add('hidden-element');
    resultCard.classList.add('hidden-element');

    const formData = new FormData();
    formData.append('ref_number', refInput);

    fetch('/api/public/applications/status', { method: 'POST', body: formData })
        .then(response => response.json())
        .then(data => {
            btnText.textContent = 'Check Status';
            btnIcon.classList.remove('hidden-element');
            btn.disabled = false;

            if(data.success) {
                document.getElementById('resName').textContent = data.data.student_name;
                document.getElementById('resClass').textContent = data.data.applied_class;

                const scholVal = data.data.scholarship_status ? data.data.scholarship_status.trim() : '';
                const scholDiv = document.getElementById('scholDiv');
                if(scholVal && scholVal.toLowerCase() !== 'none') {
                    document.getElementById('resSchol').textContent = scholVal;
                    scholDiv.classList.remove('hidden-element');
                } else {
                    scholDiv.classList.add('hidden-element');
                }

                const statusSpan = document.getElementById('resStatus');
                statusSpan.textContent = data.data.status;

                statusSpan.className = 'status-badge';
                let statLower = data.data.status.toLowerCase();
                if(statLower === 'admitted' || statLower === 'selected' || statLower === 'shortlisted') {
                    statusSpan.classList.add('status-admitted');
                } else if(statLower === 'rejected') {
                    statusSpan.classList.add('status-rejected');
                } else {
                    statusSpan.classList.add('status-pending');
                }

                document.getElementById('printLink').href = '/apply/print?ref=' + encodeURIComponent(data.data.ref_number);
                resultCard.classList.remove('hidden-element');
            } else {
                errorDiv.textContent = data.message || "Application not found.";
                errorDiv.classList.remove('hidden-element');
            }
        })
        .catch(err => {
            btnText.textContent = 'Check Status';
            btnIcon.classList.remove('hidden-element');
            btn.disabled = false;
            errorDiv.textContent = "A secure network connection could not be established. Please try again.";
            errorDiv.classList.remove('hidden-element');
        });
});
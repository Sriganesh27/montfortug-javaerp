document.addEventListener('DOMContentLoaded', () => {
    // 1. Initialize Global ERP Calendar on DOB field
    const currentYear = new Date().getFullYear();
    const exactToday = new Date();
    exactToday.setHours(23, 59, 59, 999);

    createErpCalendar("input[name='dob']", {
        maxDate: exactToday,
        minYear: currentYear -50,// Valid DOB Range
        maxYear: currentYear
    });
});
document.getElementById('statusForm').addEventListener('submit', function(e) {
    e.preventDefault();

    const btn = document.getElementById('searchBtn');
    const btnText = document.getElementById('searchBtnText');
    const btnIcon = document.getElementById('searchBtnIcon');
    const errorDiv = document.getElementById('errorMessage');
    const resultCard = document.getElementById('resultCard');

    const refInput = document.getElementById('ref_number').value.trim();
    const dobInput = document.getElementById('dob').value.trim(); // NEW

    // --- SECURITY: HONEYPOT INTERCEPTOR ---
    const honeypot = document.getElementById('fax_number_trap');
    if (honeypot && honeypot.value.trim() !== "") {
        console.warn("Bot detected by honeypot. Request killed.");
        errorDiv.textContent = "Application not found.";
        errorDiv.classList.remove('hidden-element');
        return;
    }

    // --- SECURITY: BUTTON THROTTLING ---
    btnText.textContent = 'Searching...';
    btnIcon.classList.add('hidden-element');
    btn.disabled = true;
    errorDiv.classList.add('hidden-element');
    resultCard.classList.add('hidden-element');

    const formData = new FormData();
    formData.append('ref_number', refInput);
    formData.append('dob', dobInput); // NEW: Send DOB for Session Verification

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

                // NEW: Use a cosmetic URL instead of exposing the Ref Number!
                const safeName = data.data.student_name.replace(/\s+/g, '-');
                document.getElementById('printLink').href = '/apply/print?student=' + encodeURIComponent(safeName);

                resultCard.classList.remove('hidden-element');
            } else {
                errorDiv.textContent = data.message || "Invalid Reference Number or Date of Birth.";
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
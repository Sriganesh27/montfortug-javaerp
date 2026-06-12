document.addEventListener("DOMContentLoaded", function() {
    const finalSubmitBtn = document.getElementById('btnFinalizeAdmission');
    
    if (finalSubmitBtn) {
        finalSubmitBtn.addEventListener('click', function() {
            const form = document.getElementById('admissionForm');
            
            // Basic HTML5 validation trigger
            if (!form.checkValidity()) {
                form.reportValidity();
                return;
            }

            const fd = new FormData(form);
            fetch('api/admin/students/admission.php', { method: 'POST', body: fd }) // Or wherever your new StudentController is mapped
            .then(r => r.json())
            .then(d => {
                if(d.success) { 
                    alert(d.message); 
                    // Return to the applications dashboard
                    if (typeof loadModule === 'function') {
                        loadModule('modules/manage_applications/manage_applications.php');
                    } else {
                        window.location.reload();
                    }
                } else {
                    alert(d.message);
                }
            })
            .catch(err => alert("Network Error: Could not finalize admission."));
        });
    }
});
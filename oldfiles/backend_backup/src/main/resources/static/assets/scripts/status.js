document.getElementById('statusForm').addEventListener('submit', function(e) {
    e.preventDefault();
    
    const btn = document.getElementById('searchBtn');
    const errorDiv = document.getElementById('errorMessage');
    const resultCard = document.getElementById('resultCard');
    const refInput = document.getElementById('ref_number').value.trim();
    
    btn.innerText = 'Searching...';
    btn.disabled = true;
    errorDiv.style.display = 'none';
    resultCard.style.display = 'none';

    const formData = new FormData();
    formData.append('ref_number', refInput);

    fetch('/api/public/applications/status', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        btn.innerText = 'Check Status';
        btn.disabled = false;

        if(data.success) {
            document.getElementById('resName').innerText = data.data.student_name;
            document.getElementById('resClass').innerText = data.data.applied_class;
            
            const scholVal = data.data.scholarship_status ? data.data.scholarship_status.trim() : '';
            const scholDiv = document.getElementById('scholDiv');
            if(scholVal && scholVal.toLowerCase() !== 'none') {
                document.getElementById('resSchol').innerText = scholVal;
                scholDiv.style.display = 'block';
            } else {
                scholDiv.style.display = 'none';
            }
            
            const statusSpan = document.getElementById('resStatus');
            statusSpan.innerText = data.data.status;
            statusSpan.className = 'status-badge';
            
            let statLower = data.data.status.toLowerCase();
            if(statLower === 'admitted' || statLower === 'selected' || statLower === 'shortlisted') { 
                statusSpan.classList.add('status-admitted'); 
            }
            else if(statLower === 'rejected') { 
                statusSpan.classList.add('status-rejected'); 
            }
            else { 
                statusSpan.classList.add('status-pending'); 
            }

            document.getElementById('printLink').href = '/apply/print?ref=' + data.data.ref_number;
            
            resultCard.style.display = 'block';
        } else {
            errorDiv.innerText = data.message;
            errorDiv.style.display = 'block';
        }
    })
    .catch(err => {
        btn.innerText = 'Check Status';
        btn.disabled = false;
        errorDiv.innerText = "A network error occurred. Please try again.";
        errorDiv.style.display = 'block';
    });
});

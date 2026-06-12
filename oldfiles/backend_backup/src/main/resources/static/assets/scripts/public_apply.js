document.addEventListener("DOMContentLoaded", function() {
    let dynamicBranchLevels = {};
    const classData = { 
        'Nursery': [{code: 'N1', name: 'Baby Class'}, {code: 'N2', name: 'Middle Class'}, {code: 'N3', name: 'Top Class'}], 
        'Primary': ['P1', 'P2', 'P3', 'P4', 'P5', 'P6', 'P7'].map(c => ({code: c, name: c})), 
        'Secondary': ['S1', 'S2', 'S3', 'S4', 'S5', 'S6'].map(c => ({code: c, name: c})) 
    };

    const baseSystemUrl = document.getElementById('systemBaseUrl').value;

    const FETCH_BRANCHES_URL = '/api/public/branches';
    const SUBMIT_URL = '/api/public/applications/submit';

    // 1. Fetch Branches
    fetch(FETCH_BRANCHES_URL).then(r => r.json()).then(d => {
        if (d.success) {
            const bs = document.getElementById('branchSelect'); bs.innerHTML = '<option value="">Select School...</option>';
            d.data.forEach(b => {
                bs.innerHTML += `<option value="${b.branch_id}">${b.branch_name}</option>`;
                dynamicBranchLevels[b.branch_id] = b.branch_type ? b.branch_type.split('/').map(l => l.trim()) : ['Nursery', 'Primary', 'Secondary'];
            });
        }
    }).catch(e => console.error("Router error or Controller missing:", e));

    // 2. Form Logic
    document.getElementById('branchSelect').addEventListener('change', function() {
        const ls = document.getElementById('levelSelect'); ls.innerHTML = '<option value="">Select Level...</option>';
        if (this.value && dynamicBranchLevels[this.value]) dynamicBranchLevels[this.value].forEach(l => ls.innerHTML += `<option value="${l.charAt(0).toUpperCase() + l.slice(1)}">${l}</option>`);
        ls.dispatchEvent(new Event('change'));
    });

    document.getElementById('levelSelect').addEventListener('change', function() {
        const cs = document.getElementById('classSelect'); cs.innerHTML = '<option value="">Select Class...</option>';
        if (this.value && classData[this.value]) classData[this.value].forEach(c => cs.innerHTML += `<option value="${c.code}|${c.name}">${c.name}</option>`);
        document.getElementById('subjectsContainer').style.display = 'none'; document.getElementById('examContainer').style.display = 'none';
    });

    // 3. Smart Academic History Logic
    document.getElementById('classSelect').addEventListener('change', function() {
        const cv = this.value.split('|')[0]; 
        const ex = document.getElementById('examContainer'); 
        const subCont = document.getElementById('subjectsContainer');
        const addSubBtn = document.getElementById('addSubjectBtn');
        
        ex.innerHTML = ''; 
        ex.style.display = 'block';

        if (cv === 'S1') {
            ex.innerHTML = `
                <h4 class="erp-subheading">Primary Leaving Examinations (PLE) Details</h4>
                <div class="form-grid">
                    <div class="form-group"><label>PLE Index Number</label><input class="app-input" type="text" name="ple_ref" required></div>
                    <div class="form-group"><label>Aggregate Score</label><input class="app-input" type="number" name="ple_score" required></div>
                </div>
                <h4 class="erp-subheading">Academic Record</h4>
                <div class="form-group">
                    <label> Upload PLE Pass Slip (PDF/Image)</label>
                    <input class="app-input" type="file" name="prev_marks_doc" accept="application/pdf, image/jpeg, image/png">
                </div>
            `;
            subCont.style.display = 'block';
            addSubBtn.style.display = 'inline-block';
        } 
        else if (cv === 'S5') {
            ex.innerHTML = `
                <h4 class="erp-subheading">Uganda Certificate of Education (UCE) Details</h4>
                <div class="form-grid">
                    <div class="form-group"><label>UCE Index Number</label><input class="app-input" type="text" name="uce_ref"></div>
                    <div class="form-group"><label>Aggregate Score</label><input class="app-input" type="number" name="uce_score"></div>
                </div>
                <div class="form-group">
                    <label> Upload UCE Results Slip</label>
                    <input class="app-input" type="file" name="prev_marks_doc" accept="application/pdf, image/jpeg, image/png">
                </div>
            `;
            subCont.style.display = 'block';
            addSubBtn.style.display = 'inline-block';
        } 
        else {
            ex.innerHTML = `
                <h4 class="erp-subheading">Previous Academic Record</h4>
                <div class="form-group">
                    <label> Upload Previous School Report Card (PDF/Image)</label>
                    <input class="app-input" type="file" name="prev_marks_doc" accept="application/pdf, image/jpeg, image/png">
                </div>
            `;
            subCont.style.display = 'block';
            addSubBtn.style.display = 'inline-block';
        }
    });

    document.getElementById('addSubjectBtn').addEventListener('click', () => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><input class="app-input" type="text" name="subject_name[]" placeholder="Subject Name"></td>
            <td><input class="app-input" type="number" name="subject_mark[]" placeholder="Marks" max="100" oninput="window.calcTotalSubjectScore()"></td>
            <td><input class="app-input" type="text" name="subject_grade[]" placeholder="Grade"></td>
            <td class="erp-table-th-center">
                <button type="button" class="btn-remove" onclick="this.closest('tr').remove(); window.calcTotalSubjectScore()">X</button>
            </td>`;
        document.getElementById('subjectsBody').appendChild(tr);
    });

    window.calcTotalSubjectScore = function() {
        const inputs = document.querySelectorAll('input[name="subject_mark[]"]');
        let total = 0;
        inputs.forEach(inp => {
            const val = parseFloat(inp.value);
            if (!isNaN(val)) total += val;
        });
        const scoreEl = document.getElementById('totalSubjectScore');
        if (scoreEl) scoreEl.textContent = total;
    };

    // 4. Photo Validation (Max 50KB)
    document.querySelector('input[name="photo"]').addEventListener('change', function() {
        if (this.files[0] && (this.files[0].size / 1024) > 50) {
            Swal.fire('File Too Large', 'Maximum photo size is 50KB.', 'error');
            this.value = '';
        }
    });

    // 5. Submit Form
    document.getElementById('publicAppForm').addEventListener('submit', function(e) {
        e.preventDefault();
        const submitBtn = document.getElementById('submitBtn');
        submitBtn.innerHTML = 'Processing Application...'; 
        submitBtn.disabled = true;

        fetch(SUBMIT_URL, { method: 'POST', body: new FormData(this) })
        .then(r => r.json())
        .then(d => {
            if(d.success) { 
                document.getElementById('publicAppForm').style.display = 'none'; 
                
                Swal.fire({ 
                    title: 'Application Successful!', 
                    html: `<p style="font-size: 16px;">Your Official Reference Number is: <br><strong style="color: #28a745; font-size: 22px;">${d.ref_number}</strong></p><p>Please download your application copy below.</p>`, 
                    icon: 'success', 
                    showCancelButton: true, 
                    confirmButtonText: 'Download Application PDF', 
                    cancelButtonText: 'Finish & Close',
                    confirmButtonColor: '#3085d6',
                    cancelButtonColor: '#6c757d',
                    allowOutsideClick: false
                }).then((result) => { 
                    if (result.isConfirmed) { 
                        // THIS OPENS THE NEW A4 PRINT VIEW WE BUILT!
                        window.open('/apply/print?ref=' + d.ref_number, '_blank');
                        // Reload the page to clear the form for the next student
                        window.location.reload();
                    } else {
                        // Reload the page if they just click Close
                        window.location.reload();
                    }
                });
            } else {
                Swal.fire('Error', d.message, 'error');
                submitBtn.innerHTML = 'Submit Application form'; 
                submitBtn.disabled = false;
            }
        });
    });
});
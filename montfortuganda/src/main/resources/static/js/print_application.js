// Remove inline onclick and bind in JS
document.addEventListener('DOMContentLoaded', () => {
    const printBtn = document.getElementById('triggerPrintBtn');
    if (printBtn) {
        printBtn.addEventListener('click', () => {
            window.print();
        });
    }

    // Remove inline onerror and bind in JS
    const schoolLogo = document.getElementById('schoolLogo');
    if (schoolLogo) {
        schoolLogo.addEventListener('error', () => {
            schoolLogo.classList.add('hidden-element');
        });
    }
});

function displayField(val) {
    return (val && val.trim() !== '') ? val : '-';
}

function renderSubjectsSecurely(containerId, jsonStr) {
    const container = document.getElementById(containerId);

    // Completely secure DOM clear without innerHTML
    while(container.firstChild) {
        container.removeChild(container.firstChild);
    }

    if (!jsonStr || jsonStr.trim() === '') {
        container.textContent = 'No specific subjects declared.';
        return;
    }

    try {
        let arr = JSON.parse(jsonStr);
        if (arr.length === 0) {
            container.textContent = 'No subjects declared.';
            return;
        }

        // Strict DOM Element Creation (Zero HTML Strings)
        const table = document.createElement('table');
        table.className = 'secure-marks-table';

        const thead = document.createElement('thead');
        const trHead = document.createElement('tr');
        const th1 = document.createElement('th'); th1.textContent = 'Subject Name';
        const th2 = document.createElement('th'); th2.textContent = 'Marks Submitted';
        const th3 = document.createElement('th'); th3.textContent = 'Grade';
        trHead.appendChild(th1);
        trHead.appendChild(th2);
        trHead.appendChild(th3);
        thead.appendChild(trHead);
        table.appendChild(thead);

        const tbody = document.createElement('tbody');
        arr.forEach(sub => {
            let name = sub.name || sub.subject || Object.values(sub)[0] || '-';
            let mark = sub.mark || sub.marks || Object.values(sub)[1] || '-';
            let grade = sub.grade || Object.values(sub)[2] || '-';

            const tr = document.createElement('tr');
            const tdName = document.createElement('td'); tdName.textContent = name;
            const tdMark = document.createElement('td'); tdMark.textContent = mark;
            const tdGrade = document.createElement('td'); tdGrade.textContent = grade;

            tr.appendChild(tdName);
            tr.appendChild(tdMark);
            tr.appendChild(tdGrade);
            tbody.appendChild(tr);
        });

        table.appendChild(tbody);
        container.appendChild(table);
    } catch (e) {
        container.textContent = jsonStr;
    }
}

document.addEventListener("DOMContentLoaded", function() {
    const urlParams = new URLSearchParams(window.location.search);
    const ref = urlParams.get('ref');

    if (ref) {
        document.title = "Application Receipt - " + ref;

        fetch('/api/public/applications/details?ref=' + encodeURIComponent(ref))
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    const app = data.data;

                    document.getElementById('branch_name').textContent = displayField(app.branch_name || 'General Campus');
                    document.getElementById('branch_location').textContent = displayField(app.branch_location || 'KAMPALA');
                    document.getElementById('ref_number').textContent = displayField(app.ref_number);
                    document.getElementById('date_of_registration').textContent = displayField(app.date_of_registration);

                    const schol = app.scholarship_status ? app.scholarship_status.trim() : '';
                    const scholContainer = document.getElementById('schol_container');
                    if (schol && schol.toLowerCase() !== 'none') {
                        document.getElementById('schol_val').textContent = schol;
                        scholContainer.classList.remove('hidden-element');
                    }

                    const stat = app.status ? app.status.trim() : 'Pending';
                    let statClass = 'status-pending';
                    const statLower = stat.toLowerCase();
                    if (statLower === 'admitted' || statLower === 'selected') { statClass = 'status-admitted'; }
                    else if (statLower === 'rejected') { statClass = 'status-rejected'; }

                    const statusElem = document.getElementById('status_val');
                    statusElem.textContent = stat;
                    statusElem.className = 'meta-value ' + statClass;

                    // Section 1
                    const fullName = `${app.student_name || ''} ${app.middle_name || ''} ${app.student_surname || ''}`.trim();
                    document.getElementById('full_name').textContent = displayField(fullName);
                    document.getElementById('gender').textContent = displayField(app.gender);
                    document.getElementById('dob').textContent = displayField(app.dob);
                    document.getElementById('nationality').textContent = displayField(app.nationality);
                    document.getElementById('acad_term').textContent = `${displayField(app.academic_year)} / ${displayField(app.term)}`;

                    const classCode = app.class_code ? `[${app.class_code}]` : '';
                    document.getElementById('applied_class').textContent = `${displayField(app.applied_class)} ${classCode} (${displayField(app.level)})`;

                    if (app.photo_path) {
                        let finalPhotoPath = app.photo_path;
                        if (finalPhotoPath.includes('/assets/uploads/')) {
                            finalPhotoPath = finalPhotoPath.substring(finalPhotoPath.indexOf('/assets/uploads/'));
                        }
                        document.getElementById('student_photo').src = finalPhotoPath;
                        document.getElementById('student_photo').classList.remove('hidden-element');
                        document.getElementById('no_photo').classList.add('hidden-element');
                    }

                    // Section 2
                    document.getElementById('father_name').textContent = displayField(app.father_name);
                    document.getElementById('father_contact').textContent = displayField(app.father_contact);
                    document.getElementById('father_email').textContent = displayField(app.father_email);
                    document.getElementById('father_occupation').textContent = displayField(app.father_occupation);
                    document.getElementById('father_education').textContent = displayField(app.father_education);
                    document.getElementById('father_age').textContent = displayField(app.father_age);

                    document.getElementById('mother_name').textContent = displayField(app.mother_name);
                    document.getElementById('mother_contact').textContent = displayField(app.mother_contact);
                    document.getElementById('mother_email').textContent = displayField(app.mother_email);
                    document.getElementById('mother_occupation').textContent = displayField(app.mother_occupation);
                    document.getElementById('mother_education').textContent = displayField(app.mother_education);
                    document.getElementById('mother_age').textContent = displayField(app.mother_age);

                    document.getElementById('guardian_name').textContent = displayField(app.guardian_name);
                    document.getElementById('guardian_relation').textContent = displayField(app.guardian_relation);
                    document.getElementById('guardian_contact').textContent = displayField(app.guardian_contact);
                    document.getElementById('guardian_email').textContent = displayField(app.guardian_email);
                    document.getElementById('guardian_occupation').textContent = displayField(app.guardian_occupation);
                    document.getElementById('guardian_edu_age').textContent = `${displayField(app.guardian_education)} | Age: ${displayField(app.guardian_age)}`;
                    document.getElementById('guardian_location').textContent = displayField(app.guardian_location);

                    // Section 3
                    document.getElementById('address_house_street').textContent = `${displayField(app.address_house)}, ${displayField(app.address_street)}`;
                    document.getElementById('address_village_district').textContent = `${displayField(app.address_village)} / ${displayField(app.address_district)} / ${displayField(app.address_state)}`;
                    document.getElementById('address_postal_country').textContent = `${displayField(app.address_postal)} / ${displayField(app.address_country)}`;

                    // Section 4
                    document.getElementById('former_school').textContent = displayField(app.former_school);
                    document.getElementById('former_school_code').textContent = displayField(app.former_school_code);
                    document.getElementById('former_school_lin').textContent = displayField(app.former_school_lin);

                    document.getElementById('ple_ref_score').textContent = `${displayField(app.ple_ref)} / ${displayField(app.ple_score)}`;
                    document.getElementById('uce_ref_score').textContent = `${displayField(app.uce_ref)} / ${displayField(app.uce_score)}`;

                    renderSubjectsSecurely('subject_marks_container', app.subject_marks);

                    document.getElementById('more_info').textContent = displayField(app.more_info || 'None declared.');
                } else {
                    alert("Could not securely load application details: " + data.message);
                }
            })
            .catch(err => {
                console.error(err);
                alert("Secure network error loading application receipt.");
            });
    }
});

// --- Set Generated Timestamp (Strictly JS separated) ---
document.addEventListener('DOMContentLoaded', () => {
    const timestampEl = document.getElementById('generated_timestamp');
    if (timestampEl) {
        const now = new Date();
        const options = {
            year: 'numeric', month: 'short', day: 'numeric',
            hour: '2-digit', minute: '2-digit', second: '2-digit'
        };
        timestampEl.textContent = now.toLocaleDateString('en-US', options);
    }
});
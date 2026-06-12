function displayField(val) {
    return (val && val.trim() !== '') ? val : '-';
}

function displaySubjects(jsonStr) {
    if (!jsonStr || jsonStr.trim() === '') return '<span style="font-size:13px;">No specific subjects declared.</span>';
    try {
        let arr = JSON.parse(jsonStr);
        if (arr.length === 0) return '<span style="font-size:13px;">No subjects declared.</span>';
        
        let out = '<table style="width:100%; border-collapse:collapse; margin-top:5px; font-size:13px;">';
        out += '<tr style="background:#e2e8f0;"><th style="border:1px solid #000; padding:6px; text-align:left;">Subject</th><th style="border:1px solid #000; padding:6px; width:100px; text-align:center;">Marks</th><th style="border:1px solid #000; padding:6px; width:100px; text-align:center;">Grade</th></tr>';
        
        arr.forEach(sub => {
            let name = sub.name || sub.subject || Object.values(sub)[0] || '';
            let mark = sub.mark || Object.values(sub)[1] || '-';
            let grade = sub.grade || Object.values(sub)[2] || '-';
            out += `<tr><td style='border:1px solid #000; padding:6px;'>${name}</td><td style='border:1px solid #000; padding:6px; text-align:center;'>${mark}</td><td style='border:1px solid #000; padding:6px; text-align:center;'>${grade}</td></tr>`;
        });
        out += '</table>';
        return out;
    } catch (e) {
        return jsonStr;
    }
}

document.addEventListener("DOMContentLoaded", function() {
    const urlParams = new URLSearchParams(window.location.search);
    const ref = urlParams.get('ref');

    if (ref) {
        document.title = "Application Receipt - " + ref;
        
        // Fetch the application details from the Java API
        fetch('/api/public/applications/details?ref=' + encodeURIComponent(ref))
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                const app = data.data;
                
                document.getElementById('branch_name').textContent = displayField(app.branch_name || 'General Branch');
                document.getElementById('ref_number').textContent = displayField(app.ref_number);
                document.getElementById('date_of_registration').textContent = displayField(app.date_of_registration);

                const schol = app.scholarship_status ? app.scholarship_status.trim() : '';
                if (schol && schol.toLowerCase() !== 'none') {
                    document.getElementById('schol_val').textContent = schol;
                    document.getElementById('schol_container').style.display = 'block';
                }

                const stat = app.status ? app.status.trim() : 'Pending';
                let color = '#b8860b'; 
                const statLower = stat.toLowerCase();
                if (statLower === 'admitted' || statLower === 'selected' || statLower === 'shortlisted') { color = '#2f855a'; } 
                else if (statLower === 'rejected') { color = '#d32f2f'; }
                
                const statusElem = document.getElementById('status_val');
                statusElem.textContent = stat;
                statusElem.style.color = color;

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
                    // Fix legacy PHP/XAMPP paths from the database
                    if (finalPhotoPath.includes('/assets/uploads/')) {
                        finalPhotoPath = finalPhotoPath.substring(finalPhotoPath.indexOf('/assets/uploads/'));
                    }
                    
                    document.getElementById('student_photo').src = finalPhotoPath;
                    document.getElementById('student_photo').style.display = 'block';
                    document.getElementById('no_photo').style.display = 'none';
                }

                // Section 2 - Father
                document.getElementById('father_name').textContent = displayField(app.father_name);
                document.getElementById('father_contact').textContent = displayField(app.father_contact);
                document.getElementById('father_email').textContent = displayField(app.father_email);
                document.getElementById('father_occupation').textContent = displayField(app.father_occupation);
                document.getElementById('father_education').textContent = displayField(app.father_education);
                document.getElementById('father_age').textContent = displayField(app.father_age);

                // Section 2 - Mother
                document.getElementById('mother_name').textContent = displayField(app.mother_name);
                document.getElementById('mother_contact').textContent = displayField(app.mother_contact);
                document.getElementById('mother_email').textContent = displayField(app.mother_email);
                document.getElementById('mother_occupation').textContent = displayField(app.mother_occupation);
                document.getElementById('mother_education').textContent = displayField(app.mother_education);
                document.getElementById('mother_age').textContent = displayField(app.mother_age);

                // Section 2 - Guardian
                document.getElementById('guardian_name').textContent = displayField(app.guardian_name);
                document.getElementById('guardian_relation').textContent = displayField(app.guardian_relation);
                document.getElementById('guardian_contact').textContent = displayField(app.guardian_contact);
                document.getElementById('guardian_email').textContent = displayField(app.guardian_email);
                document.getElementById('guardian_occupation').textContent = displayField(app.guardian_occupation);
                document.getElementById('guardian_edu_age').textContent = `${displayField(app.guardian_education)} | Age: ${displayField(app.guardian_age)}`;

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
                
                document.getElementById('subject_marks').innerHTML = displaySubjects(app.subject_marks);
                document.getElementById('more_info').textContent = displayField(app.more_info || 'None declared.');
                
            } else {
                alert("Could not load application details: " + data.message);
            }
        })
        .catch(err => {
            console.error(err);
            alert("Error loading application receipt.");
        });
    }
});

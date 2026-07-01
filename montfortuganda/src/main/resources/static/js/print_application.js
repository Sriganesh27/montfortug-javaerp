/**
 * @typedef {Object} ErpApplicationData
 * @property {string} [branch_name]
 * @property {string} [branch_location]
 * @property {string} [ref_number]
 * @property {string} [date_of_registration]
 * @property {string} [scholarship_status]
 * @property {string} [status]
 * @property {string} [student_name]
 * @property {string} [middle_name]
 * @property {string} [student_surname]
 * @property {string} [gender]
 * @property {string} [dob]
 * @property {string} [nationality]
 * @property {string} [academic_year]
 * @property {string} [term]
 * @property {string} [applied_class]
 * @property {string} [class_code]
 * @property {string} [level]
 * @property {string} [photo_path]
 * @property {string} [primary_email]
 * @property {string} [primary_mobile]
 * @property {string} [father_name]
 * @property {string} [father_contact]
 * @property {string} [father_email]
 * @property {string} [father_occupation]
 * @property {string} [father_education]
 * @property {string} [father_age]
 * @property {string} [mother_name]
 * @property {string} [mother_contact]
 * @property {string} [mother_email]
 * @property {string} [mother_occupation]
 * @property {string} [mother_education]
 * @property {string} [mother_age]
 * @property {string} [guardian_name]
 * @property {string} [guardian_relation]
 * @property {string} [guardian_contact]
 * @property {string} [guardian_email]
 * @property {string} [guardian_occupation]
 * @property {string} [guardian_education]
 * @property {string} [guardian_age]
 * @property {string} [guardian_location]
 * @property {string} [address_house]
 * @property {string} [address_street]
 * @property {string} [address_village]
 * @property {string} [address_district]
 * @property {string} [address_state]
 * @property {string} [address_postal]
 * @property {string} [former_school]
 * @property {string} [former_school_code]
 * @property {string} [former_school_lin]
 * @property {string} [ple_ref]
 * @property {string} [ple_score]
 * @property {string} [uce_ref]
 * @property {string} [uce_score]
 * @property {string} [subject_marks]
 * @property {string} [more_info]
 */

// Remove inline onclick and bind in JS
document.addEventListener('DOMContentLoaded', () => {
    const printBtn = document.getElementById('triggerPrintBtn');
    if (printBtn) {
        printBtn.addEventListener('click', () => window.print());
    }

    const schoolLogo = document.getElementById('schoolLogo');
    if (schoolLogo) {
        schoolLogo.addEventListener('error', () => schoolLogo.classList.add('hidden-element'));
    }
});

/**
 * Fix #1 & #2: Smart Field Combiner (No more "-, -" or double spaces)
 * @param {Array<any>} fields
 * @param {string} separator
 * @returns {string}
 */
function combineFields(fields, separator) {
    const valid = fields.filter(f => f !== null && f !== undefined && String(f).trim() !== '');
    return valid.length > 0 ? valid.join(separator) : '-';
}

/**
 * @param {any} val
 * @returns {string}
 */
function displayField(val) {
    if (val === null || val === undefined || val === '') return '-';
    return String(val).trim() !== '' ? String(val).trim() : '-';
}

/**
 * Fix #3: Bulletproof DOM Setter (Prevents the script from crashing if an ID is missing)
 * @param {string} id
 * @param {string} text
 */
function setElementText(id, text) {
    const el = document.getElementById(id);
    if (el) el.textContent = text;
}

function renderSubjectsSecurely(containerId, jsonStr) {
    const container = document.getElementById(containerId);
    if (!container) return;

    while(container.firstChild) {
        container.removeChild(container.firstChild);
    }

    if (!jsonStr || jsonStr.trim() === '') {
        const span = document.createElement('span');
        span.className = 'value';
        span.textContent = 'No specific subjects declared.';
        container.appendChild(span);
        return;
    }

    try {
        let arr = JSON.parse(jsonStr);
        if (arr.length === 0) {
            const span = document.createElement('span');
            span.className = 'value';
            span.textContent = 'No subjects declared.';
            container.appendChild(span);
            return;
        }

        const table = document.createElement('table');
        table.className = 'secure-marks-table';

        const thead = document.createElement('thead');
        const trHead = document.createElement('tr');
        const th1 = document.createElement('th'); th1.textContent = 'Subject Name';
        const th2 = document.createElement('th'); th2.textContent = 'Marks Submitted';
        const th3 = document.createElement('th'); th3.textContent = 'Grade';
        trHead.appendChild(th1); trHead.appendChild(th2); trHead.appendChild(th3);
        thead.appendChild(trHead); table.appendChild(thead);

        const tbody = document.createElement('tbody');
        arr.forEach(sub => {
            let name = sub.name || sub.subject || Object.values(sub)[0] || '-';
            let mark = sub.mark || sub.marks || Object.values(sub)[1] || '-';
            let grade = sub.grade || Object.values(sub)[2] || '-';

            const tr = document.createElement('tr');
            const tdName = document.createElement('td'); tdName.textContent = name;
            const tdMark = document.createElement('td'); tdMark.textContent = mark;
            const tdGrade = document.createElement('td'); tdGrade.textContent = grade;

            tr.appendChild(tdName); tr.appendChild(tdMark); tr.appendChild(tdGrade);
            tbody.appendChild(tr);
        });

        table.appendChild(tbody);
        container.appendChild(table);
    } catch (e) {
        const span = document.createElement('span');
        span.className = 'value';
        span.textContent = jsonStr;
        container.appendChild(span);
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

                    /** @type {ErpApplicationData} */
                    const app = data.data;

                    setElementText('branch_name', displayField(app.branch_name || 'General Campus'));
                    setElementText('branch_location', displayField(app.branch_location || 'KAMPALA'));
                    setElementText('ref_number', displayField(app.ref_number));
                    setElementText('date_of_registration', displayField(app.date_of_registration));

                    // Scholarship Status
                    const schol = app.scholarship_status ? app.scholarship_status.trim() : '';
                    const scholContainer = document.getElementById('schol_container');
                    if (scholContainer && schol && schol.toLowerCase() !== 'none') {
                        setElementText('schol_val', schol);
                        scholContainer.classList.remove('hidden-element');
                    }

                    // Admission Status
                    const stat = app.status ? app.status.trim() : 'Pending';
                    let statClass = 'status-pending';
                    const statLower = stat.toLowerCase();
                    if (statLower === 'admitted' || statLower === 'selected') { statClass = 'status-admitted'; }
                    else if (statLower === 'rejected') { statClass = 'status-rejected'; }

                    const statusElem = document.getElementById('status_val');
                    if (statusElem) {
                        statusElem.textContent = stat;
                        statusElem.className = 'meta-value ' + statClass;
                    }

                    // Section 1: Student Details
                    setElementText('full_name', combineFields([app.student_name, app.middle_name, app.student_surname], ' '));
                    setElementText('gender', displayField(app.gender));
                    setElementText('dob', displayField(app.dob));
                    setElementText('nationality', displayField(app.nationality));
                    setElementText('acad_year', displayField(app.academic_year));
                    setElementText('acad_term_only', displayField(app.term));

                    const classCode = app.class_code ? `[${app.class_code}]` : '';
                    setElementText('applied_class', combineFields([app.applied_class, classCode], ' '));
                    setElementText('level', displayField(app.level));

                    // --- PHOTO LOADING LOGIC ---
                    if (app.photo_path) {
                        let finalPhotoPath = app.photo_path;
                        if (finalPhotoPath.includes('/assets/uploads/')) {
                            finalPhotoPath = finalPhotoPath.substring(finalPhotoPath.indexOf('/assets/uploads/'));
                        }
                        const photoEl = document.getElementById('student_photo');
                        const noPhotoEl = document.getElementById('no_photo');

                        if (photoEl && noPhotoEl) {
                            photoEl.src = finalPhotoPath;
                            photoEl.classList.remove('hidden-element');
                            noPhotoEl.classList.add('hidden-element');
                            noPhotoEl.style.display = 'none';
                        }
                    }

                    // Section 2: Primary Account Contact
                    setElementText('primary_email', displayField(app.primary_email));
                    setElementText('primary_mobile', displayField(app.primary_mobile));

                    // Section 3: Parents & Guardian
                    setElementText('father_name', displayField(app.father_name));
                    setElementText('father_contact', displayField(app.father_contact));
                    setElementText('father_email', displayField(app.father_email));
                    setElementText('father_occupation', displayField(app.father_occupation));
                    setElementText('father_education', displayField(app.father_education));
                    setElementText('father_age', displayField(app.father_age));

                    setElementText('mother_name', displayField(app.mother_name));
                    setElementText('mother_contact', displayField(app.mother_contact));
                    setElementText('mother_email', displayField(app.mother_email));
                    setElementText('mother_occupation', displayField(app.mother_occupation));
                    setElementText('mother_education', displayField(app.mother_education));
                    setElementText('mother_age', displayField(app.mother_age));

                    setElementText('guardian_name', displayField(app.guardian_name));
                    setElementText('guardian_relation', displayField(app.guardian_relation));
                    setElementText('guardian_contact', displayField(app.guardian_contact));
                    setElementText('guardian_email', displayField(app.guardian_email));
                    setElementText('guardian_occupation', displayField(app.guardian_occupation));

                    const eduAge = combineFields([app.guardian_education, app.guardian_age ? `Age: ${app.guardian_age}` : null], ' | ');
                    setElementText('guardian_edu_age', eduAge);
                    setElementText('guardian_location', displayField(app.guardian_location));

                    // Section 4: Residential Address
                    setElementText('address_house_street', combineFields([app.address_house, app.address_street], ' / '));
                    setElementText('address_village_district', combineFields([app.address_village, app.address_district], ' / '));
                    setElementText('address_region_postal', combineFields([app.address_state, app.address_postal], ' / '));

                    // Section 5: Academic History
                    setElementText('former_school', displayField(app.former_school));
                    setElementText('former_school_code', displayField(app.former_school_code));
                    setElementText('former_school_lin', displayField(app.former_school_lin));

                    setElementText('ple_ref_score', combineFields([app.ple_ref, app.ple_score], ' / '));
                    setElementText('uce_ref_score', combineFields([app.uce_ref, app.uce_score], ' / '));

                    renderSubjectsSecurely('subject_marks_container', app.subject_marks);

                    setElementText('more_info', displayField(app.more_info || 'None declared.'));
                    const moreInfoEl = document.getElementById('more_info');
                    if (moreInfoEl) {
                        moreInfoEl.classList.add('value');
                    }

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

// --- Set Generated Timestamp ---
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
/* global Swal, window, flatpickr */

/**
 * @typedef {Object} Level
 * @property {number} levelId
 * @property {string} levelName
 */

/**
 * @typedef {Object} BranchLevel
 * @property {Level} level
 */

/**
 * @typedef {Object} AcademicTerm
 * @property {number} termId
 * @property {string} termCode
 * @property {string} termName
 * @property {number} displayOrder
 * @property {boolean} currentTerm
 * @property {string} status
 */

/**
 * @typedef {Object} AcademicYear
 * @property {number} academicYearId
 * @property {string} academicYearCode
 * @property {string} academicYearName
 * @property {boolean} currentYear
 * @property {'CURRENT'|'UPCOMING'} admissionYearType
 * @property {AcademicTerm[]} terms
 */

/**
 * @typedef {Object} Branch
 * @property {number} branchId
 * @property {string} branchName
 * @property {string} schoolCode
 * @property {BranchLevel[]} branchLevels
 * @property {AcademicYear[]} academicYears
 */

/**
 * @typedef {Object} SchoolClass
 * @property {number} classId
 * @property {string} classCode
 * @property {string} className
 * @property {number} levelId
 */

/** @type {Branch[]} */
let branchList = [];

/** @type {SchoolClass[]} */
let classList = [];

// Helper to safely trigger SweetAlert without ARIA focus-retention violations
function showAlert(title, text, icon) {
    if (document.activeElement && typeof document.activeElement.blur === 'function') {
        document.activeElement.blur();
    }

    if (typeof Swal !== 'undefined') {
        return Swal.fire(title, text, icon);
    } else {
        alert(title + "\n\n" + text);
        return Promise.resolve();
    }
}

document.addEventListener("DOMContentLoaded", () => {
    startJavascriptSlider();

    // ========================================================
    // ENTERPRISE CALENDAR INITIALIZATION
    // ========================================================
    const currentYear = new Date().getFullYear();

    // For Registration Date (Max allowed is today)
    const exactToday = new Date();
    exactToday.setHours(23, 59, 59, 999);

    // For Date of Birth (Max allowed is Dec 31st of Current Year - 4)
    const maxDobYear = currentYear - 4;
    const exactMaxDob = new Date(maxDobYear, 11, 31, 23, 59, 59, 999);

    // 1. Date of Birth Picker
    createErpCalendar("input[name='dob']", {
        maxDate: exactMaxDob,      // Blocks any date younger than Dec 31 of (Year-4)
        minYear: currentYear - 25,
        maxYear: maxDobYear,       // Updates the dropdown so it stops at (Year-4)
        footerActions: ['today', 'clear', 'close']
    });

    // 2. Admission/Registration Date Picker
    createErpCalendar("input[name='dateOfRegistration']", {
        defaultDate: "today",      // Automatically fills in today! (Still fully editable)
        maxDate: exactToday,
        minYear: currentYear - 5,
        maxYear: currentYear,
        footerActions: ['today', 'clear', 'close']
    });

    // Fetch both branches and classes simultaneously on page load
    Promise.all([fetchBranches(), fetchClasses()]).catch(e => console.error("Initialization error:", e));

    document.getElementById("branchSelect").addEventListener("change", handleBranchChange);
    document.getElementById("academicYear").addEventListener("change", handleAcademicYearChange);
    document.getElementById("level").addEventListener("change", handleLevelChange);
    document.getElementById("classSelect").addEventListener("change", handleClassChange);
    document.getElementById("addSubjectBtn").addEventListener("click", addSubjectRow);
    document.getElementById("subjectsBody").addEventListener("click", handleTableClick);
    document.getElementById("subjectsBody").addEventListener("input", calculateSubjectTotal);
    document.getElementById("admission-form").addEventListener("submit", handleFormSubmit);
    document.addEventListener("change", handleFileUploadUI);
    initialisePrimaryContactSelection();

    // CSP-Compliant Event Delegation for Navigation
    document.addEventListener("click", function(e) {
        const targetElement = e.target.closest('[data-step-target]');
        if (targetElement) {
            const stepNum = parseInt(targetElement.getAttribute('data-step-target'), 10);
            if (!isNaN(stepNum)) {
                validateAndGoTo(stepNum);
            }
        }
    });
});
// ========================================================
// EXISTING UTILITIES AND VALIDATION
// ========================================================

// We create a master storage object to remember ALL selected files
const accumulatedDocs = new DataTransfer();

// --- NEW: Event Listener to handle clicking the Trash Can ---
document.addEventListener('click', function(e) {
    const removeBtn = e.target.closest('.btn-remove-file');
    if (removeBtn) {
        e.preventDefault();
        e.stopPropagation(); // Prevents the file dialog from opening when clicking trash

        const fileNameToRemove = removeBtn.getAttribute('data-filename');
        const dropzone = removeBtn.closest('.file-dropzone');
        const inputElement = dropzone.querySelector('input[type="file"]');

        if (inputElement && inputElement.multiple) {
            // Find and remove the specific file from our master list
            for (let i = 0; i < accumulatedDocs.files.length; i++) {
                if (accumulatedDocs.files[i].name === fileNameToRemove) {
                    accumulatedDocs.items.remove(i);
                    break;
                }
            }
            // Update the actual HTML input
            inputElement.files = accumulatedDocs.files;

            // Force the UI to refresh and show the updated list
            inputElement.dispatchEvent(new Event('change', { bubbles: true }));
        }
    }
});

function handleFileUploadUI(e) {
    if (e.target.type === "file") {
        const dropzone = e.target.closest('.file-dropzone');
        const span = dropzone.querySelector('span');

        if (e.target.multiple) {
            const existingNames = Array.from(accumulatedDocs.files).map(f => f.name);
            for (let i = 0; i < e.target.files.length; i++) {
                if (!existingNames.includes(e.target.files[i].name)) {
                    accumulatedDocs.items.add(e.target.files[i]);
                }
            }
            e.target.files = accumulatedDocs.files;
        }

        const files = e.target.files;
        const oldPreview = dropzone.querySelector('.passport-preview, .file-list-preview');
        if (oldPreview) oldPreview.remove();

        if (files && files.length > 0) {
            span.textContent = files.length === 1 ? files[0].name : files.length + " files selected";
            span.classList.add("file-selected-text");

            if (e.target.id === "photoInput" && files[0].size > 51200) {
                showAlert('Warning', 'Photo is larger than 50KB. The system might reject it.', 'warning');
            }

            if (e.target.id === "photoInput" && files[0].type.startsWith("image/")) {
                const img = document.createElement("img");
                img.src = URL.createObjectURL(files[0]);
                img.className = "passport-preview";
                dropzone.appendChild(img);
            }
            // Multiple Files Preview List
            else if (files.length > 0) {
                const list = document.createElement("div");
                list.className = "file-list-preview";

                const ul = document.createElement("ul");

                for(let i=0; i<files.length; i++) {
                    const li = document.createElement("li");

                    // Create Icon
                    const icon = document.createElement("i");
                    icon.className = "bi bi-file-earmark-check text-success";

                    // Create Safe Text Node for Filename
                    const textNode = document.createTextNode(" " + files[i].name + " ");

                    // Create Delete Button
                    const btn = document.createElement("button");
                    btn.type = "button";
                    btn.className = "btn-remove-file";
                    btn.setAttribute("data-filename", files[i].name);

                    // Create Trash Icon
                    const trashIcon = document.createElement("i");
                    trashIcon.className = "bi bi-trash3";

                    // Assemble all pieces securely
                    btn.appendChild(trashIcon);
                    li.appendChild(icon);
                    li.appendChild(textNode);
                    li.appendChild(btn);

                    ul.appendChild(li);
                }

                list.appendChild(ul);
                dropzone.appendChild(list);
            }
        } else {
            span.textContent = "Choose a file or drag it here";
            span.classList.remove("file-selected-text");
        }
    }
}

function startJavascriptSlider() {
    const slides = document.querySelectorAll('#montfort-portal-wrapper img.slide');
    if (slides.length === 0) return;
    let currentSlide = 0;
    setInterval(() => {
        slides[currentSlide].classList.remove('active-slide');
        currentSlide = (currentSlide + 1) % slides.length;
        slides[currentSlide].classList.add('active-slide');
    }, 6000);
}

function initialisePrimaryContactSelection() {
    const choices = document.querySelectorAll("input[name='primaryContactType']");
    choices.forEach(choice => choice.addEventListener("change", updatePrimaryContactSelection));
    updatePrimaryContactSelection();
}

function updatePrimaryContactSelection() {
    const selected = document.querySelector("input[name='primaryContactType']:checked");
    const contactType = selected ? selected.value : "";
    const otherSection = document.getElementById("otherPrimaryContactSection");
    const isOther = contactType === "OTHER";

    if (otherSection) otherSection.classList.toggle("hidden-element", !isOther);

    setRequired("fatherContact_display", contactType === "FATHER");
    setRequiredByName("fatherEmail", contactType === "FATHER");
    setRequired("motherContact_display", contactType === "MOTHER");
    setRequiredByName("motherEmail", contactType === "MOTHER");
    setRequiredByName("guardianName", isOther);
    setRequired("guardianContact_display", isOther);
    setRequiredByName("guardianEmail", isOther);
    setRequiredByName("guardianRelation", isOther);

    if (!isOther) clearGuardianFields();
}

function setRequired(id, required) {
    const input = document.getElementById(id);
    if (input) input.required = required;
}

function setRequiredByName(name, required) {
    const input = document.querySelector(`[name='${name}']`);
    if (input) input.required = required;
}

function clearGuardianFields() {
    document.querySelectorAll("#otherPrimaryContactSection input, #otherPrimaryContactSection select").forEach(input => {
        if (input.tagName === "SELECT") input.selectedIndex = 0;
        else input.value = "";
        input.classList.remove("error-border");
    });
}

function getPrimaryContactType() {
    const selected = document.querySelector("input[name='primaryContactType']:checked");
    return selected ? selected.value : "";
}

// VALIDATION ENGINE
function validateAndGoTo(targetStepNumber) {
    const activeStepDiv = document.querySelector(".form-step.active-step");
    if (!activeStepDiv) { goToStep(targetStepNumber); return; }
    const currentStep = parseInt(activeStepDiv.id.replace("step-", ""));
    if (targetStepNumber < currentStep) {
        goToStep(targetStepNumber);
        return;
    }
    const requiredInputs = activeStepDiv.querySelectorAll("input[required], select[required]");
    let isValid = true;
    let firstInvalid = null;
    // Remove red borders automatically as user types
    requiredInputs.forEach(input => {
        input.addEventListener("input", function() {
            this.classList.remove("error-border");
        }, { once: true });
        input.addEventListener("change", function() {
            this.classList.remove("error-border");
        }, { once: true });
    });
    requiredInputs.forEach(input => {
        if (input.checkValidity && !input.checkValidity()) {
            isValid = false;
            if (!firstInvalid) firstInvalid = input;
            input.classList.add("error-border");
        } else {
            input.classList.remove("error-border");
        }
    });
    // Step 4: the selected parent supplies the primary contact details;
    // a separate guardian is required only when "Other" is selected.
    if (currentStep === 4) {
        const contactType = getPrimaryContactType();
        if (!contactType) {
            const choice = document.querySelector("input[name='primaryContactType']");
            isValid = false;
            if (choice) {
                choice.classList.add("error-border");
                if (!firstInvalid) firstInvalid = choice;
            }
        }
    }
    if (!isValid) {
        showAlert('Missing Fields', 'Please fill in all required fields marked in red.', 'warning')
            .then(() => { if (firstInvalid && firstInvalid.focus) firstInvalid.focus(); });
        return;
    }
    if (targetStepNumber > currentStep + 1) {
        validateAndGoTo(currentStep + 1);
        if (document.querySelector(".form-step.active-step").id !== "step-" + (currentStep + 1)) return;
    }
    goToStep(targetStepNumber);
}

function goToStep(targetStepNumber) {
    document.querySelectorAll("#montfort-portal-wrapper .form-step").forEach(step => {
        step.classList.add("hidden-element");
        step.classList.remove("active-step");
    });

    if (targetStepNumber === 6) buildReviewSummary();

    const targetStep = document.getElementById("step-" + targetStepNumber);
    if (targetStep) {
        targetStep.classList.remove("hidden-element");
        targetStep.classList.add("active-step");
    }

    if (targetStepNumber <= 6) {
        document.querySelectorAll("#wizard-stepper .stepper-item").forEach((item, index) => {
            let stepNum = index + 1;
            item.classList.remove("active", "completed");
            const counter = item.querySelector(".step-counter");
            counter.textContent = '';
            if (stepNum < targetStepNumber) {
                item.classList.add("completed");
                const icon = document.createElement("i");
                icon.className = "bi bi-check2";
                counter.appendChild(icon);
            } else if (stepNum === targetStepNumber) {
                item.classList.add("active");
                counter.textContent = String(stepNum);
            } else {
                counter.textContent = String(stepNum);
            }
        });
    }
    document.querySelector("#montfort-portal-wrapper .form-document-card").scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function buildReviewSummary() {
    const container = document.getElementById("review-summary-container");
    container.textContent = '';
    const form = document.getElementById("admission-form");

    const getVal = (name) => {
        const el = form.querySelector(`input[name="${name}"], select[name="${name}"], textarea[name="${name}"]`);
        return el ? el.value : '';
    };
    const getRadio = (name) => {
        const el = form.querySelector(`input[name="${name}"]:checked`);
        return el ? el.value : 'Not Selected';
    };
    const getSelectText = (id) => {
        const el = document.getElementById(id);
        return (el && el.selectedIndex > 0) ? el.options[el.selectedIndex].text : 'Not Selected';
    };

    let fullAddress = [];
    if(getVal('addressHouse')) fullAddress.push(getVal('addressHouse'));
    if(getVal('addressStreet')) fullAddress.push(getVal('addressStreet'));
    if(getVal('addressVillage')) fullAddress.push(getVal('addressVillage'));
    if(getVal('addressDistrict')) fullAddress.push(getVal('addressDistrict'));
    if(getVal('addressState')) fullAddress.push(getVal('addressState'));
    if(getVal('addressCountry')) fullAddress.push(getVal('addressCountry'));
    let addressString = fullAddress.join(', ');
    if(getVal('addressPostal')) addressString += ` (P.O. Box: ${getVal('addressPostal')})`;

    const appendSectionBreak = (title) => {
        const div = document.createElement("div");
        div.classList.add("review-section-break");
        div.textContent = title;
        container.appendChild(div);
    };

    const appendItem = (label, value, fullWidth = false) => {
        const div = document.createElement("div");
        div.classList.add("review-item");
        if (fullWidth) div.classList.add("full-width-grid");
        const strong = document.createElement("strong");
        strong.textContent = label;
        const span = document.createElement("span");
        span.textContent = value || 'Not provided';
        div.appendChild(strong);
        div.appendChild(span);
        container.appendChild(div);
    };

    const displayValue = value => String(value || '').trim() || 'Not provided';
    const formatDate = value => value
        ? (window.erpDate ? window.erpDate.formatDate(value, 'N/A') : value)
        : 'Not provided';
    const getFileNames = name => {
        const input = form.querySelector(`input[name="${name}"]`);
        return input && input.files && input.files.length
            ? Array.from(input.files).map(file => file.name).join(', ')
            : 'Not provided';
    };
    const hasField = name => Boolean(form.querySelector(`[name="${name}"]`));
    const appendPerson = (prefix, title) => {
        appendSectionBreak(title);
        appendItem('Full Name', getVal(`${prefix}Name`));
        appendItem('Contact', getVal(`${prefix}Contact`));
        appendItem('Email', getVal(`${prefix}Email`));
        appendItem('Occupation', getVal(`${prefix}Occupation`));
        appendItem('Education', getVal(`${prefix}Education`));
        appendItem('Age', getVal(`${prefix}Age`));
    };

    appendSectionBreak('Enrollment Details');
    appendItem('School Branch', getSelectText('branchSelect'));
    appendItem('Education Level', getSelectText('level'));
    appendItem('Applying For Class', getSelectText('classSelect'));
    appendItem('Admission Year', getSelectText('academicYear'));
    appendItem('Admission Term', getSelectText('joiningTerm'));
    appendItem('Registration Date', formatDate(getVal('dateOfRegistration')));

    appendSectionBreak('Student Details');
    appendItem('First Name', getVal('studentName'));
    appendItem('Middle Name', getVal('middleName'));
    appendItem('Surname', getVal('studentSurname'));
    appendItem('Gender', getRadio('gender'));
    appendItem('Date of Birth', formatDate(getVal('dob')));
    appendItem('Nationality', getVal('nationality'));

    appendSectionBreak('Residential Address');
    appendItem('House / Plot No.', getVal('addressHouse'));
    appendItem('Street / Zone', getVal('addressStreet'));
    appendItem('Village / Town', getVal('addressVillage'));
    appendItem('District', getVal('addressDistrict'));
    appendItem('Region', getVal('addressState'));
    appendItem('P.O. Box', getVal('addressPostal'));
    appendItem('Complete Address', addressString || 'Not provided', true);

    appendPerson('father', "Father's Details");
    appendPerson('mother', "Mother's Details");

    const primaryContactType = getPrimaryContactType();
    const primaryContactLabel = primaryContactType
        ? primaryContactType.charAt(0) + primaryContactType.slice(1).toLowerCase()
        : 'Not provided';
    const primaryPrefix = primaryContactType === 'FATHER'
        ? 'father'
        : primaryContactType === 'MOTHER'
            ? 'mother'
            : 'guardian';
    appendSectionBreak('Primary Contact');
    appendItem('Selected Contact', primaryContactLabel);
    appendItem('Primary Mobile', getVal(`${primaryPrefix}Contact`));
    appendItem('Primary Email', getVal(`${primaryPrefix}Email`));

    if (primaryContactType === 'OTHER') {
        appendSectionBreak('Other Primary Contact / Guardian');
        appendItem('Full Name', getVal('guardianName'));
        appendItem('Contact', getVal('guardianContact'));
        appendItem('Email', getVal('guardianEmail'));
        appendItem('Relationship', getVal('guardianRelation'));
        appendItem('Occupation', getVal('guardianOccupation'));
        appendItem('Education', getVal('guardianEducation'));
        appendItem('Age', getVal('guardianAge'));
        appendItem('Location / Address', getVal('guardianLocation'));
    }

    appendSectionBreak('Academic Details');
    appendItem('Former School Name', getVal('formerSchool'));
    appendItem('Former School Code', getVal('formerSchoolCode'));
    appendItem('Former School LIN', getVal('formerSchoolLin'));
    if (hasField('pleRef')) {
        appendItem('PLE Index Number', getVal('pleRef'));
        appendItem('PLE Aggregate Score', getVal('pleScore'));
    }
    if (hasField('uceRef')) {
        appendItem('UCE Index Number', getVal('uceRef'));
        appendItem('UCE Aggregate Score', getVal('uceScore'));
    }

    const reviewSubjectMarks = collectSubjectMarks();
    const subjectSummary = reviewSubjectMarks.length
        ? reviewSubjectMarks.map(mark => `${mark.subject || 'Subject'}: ${mark.marks || '—'}${mark.grade ? ` (${mark.grade})` : ''}`).join(' | ')
        : 'Not provided';
    appendItem('Subject Marks', subjectSummary, true);

    appendSectionBreak('Documents & Additional Information');
    appendItem('Academic Results / Legal Documents', getFileNames('prevMarks'), true);
    appendItem('Passport Photo', getFileNames('photo'));
    appendItem('Medical / Additional Information', displayValue(getVal('moreInfo')), true);
}

function setupDefaultOption(selectElem, text="Select...") {
    selectElem.options.length = 0;
    const defaultOpt = document.createElement("option");
    defaultOpt.value = ""; defaultOpt.disabled = true; defaultOpt.selected = true; defaultOpt.textContent = text;
    selectElem.appendChild(defaultOpt);
}

// API FETCH CALLS
async function fetchBranches() {
    setupDefaultOption(document.getElementById("branchSelect"), "Select Branch");
    const response = await fetch("/api/public/branches");
    const result = await response.json();

    if (result.success && result.data) {
        branchList = result.data;
        result.data.forEach(branch => {
            const opt = document.createElement("option");
            opt.value = String(branch.branchId);

            // Format requested: (code)name, location
            const codeStr = branch.schoolCode ? `(${branch.schoolCode})` : '';
            const locationStr = branch.branchLocation ? `, ${branch.branchLocation}` : '';

            opt.textContent = `${codeStr}${branch.branchName}${locationStr}`;

            document.getElementById("branchSelect").appendChild(opt);
        });
    }
}

async function fetchClasses() {
    const response = await fetch("/api/public/classes");
    const result = await response.json();
    if (result.success && result.data) {
        classList = result.data;
    }
}

// DYNAMIC CASCADING LOGIC
function handleBranchChange() {
    const selectedId = parseInt(this.value, 10);
    const branch = branchList.find(
        b => Number(b.branchId) === selectedId
    );

    const levelSelect = document.getElementById("level");
    const classSelect = document.getElementById("classSelect");
    const academicYearSelect = document.getElementById("academicYear");
    const joiningTermSelect = document.getElementById("joiningTerm");

    setupDefaultOption(levelSelect, "Select Level");
    setupDefaultOption(classSelect, "Select Class");
    setupDefaultOption(
        academicYearSelect,
        "Select Admission Year"
    );
    setupDefaultOption(
        joiningTermSelect,
        "Select Admission Year First"
    );

    academicYearSelect.disabled = true;
    joiningTermSelect.disabled = true;

    document.getElementById("dynamic-exam-container").textContent = '';
    document.getElementById("dynamic-subjects-container").classList.add("hidden-element");

    const hiddenCode = document.getElementById("hiddenClassCode");
    const hiddenApplied = document.getElementById("hiddenAppliedClass");

    if (hiddenCode) hiddenCode.value = "";
    if (hiddenApplied) hiddenApplied.value = "";

    if (branch && Array.isArray(branch.branchLevels)) {
        branch.branchLevels.forEach(bl => {
            const lvl = bl.level;

            if (lvl && lvl.levelName) {
                levelSelect.appendChild(
                    new Option(
                        lvl.levelName,
                        lvl.levelName
                    )
                );
            }
        });
    }

    populateAcademicYears(
        academicYearSelect,
        joiningTermSelect,
        branch
    );
}

function populateAcademicYears(
    academicYearSelect,
    joiningTermSelect,
    branch
) {
    const academicYears =
        branch && Array.isArray(branch.academicYears)
            ? branch.academicYears
            : [];

    setupDefaultOption(
        academicYearSelect,
        academicYears.length > 0
            ? "Select Admission Year"
            : "No Admission-Open Year"
    );

    setupDefaultOption(
        joiningTermSelect,
        "Select Admission Year First"
    );

    academicYearSelect.disabled = true;
    joiningTermSelect.disabled = true;

    if (academicYears.length === 0) {
        return;
    }

    let preferredAcademicYearId = null;
    let availableYearCount = 0;

    academicYears.forEach(academicYear => {
        const academicYearId =
            Number(academicYear.academicYearId);

        if (!Number.isInteger(academicYearId)
                || academicYearId <= 0) {
            return;
        }

        const code =
            academicYear.academicYearCode
            || String(academicYearId);

        const suffix =
            academicYear.admissionYearType === "CURRENT"
                ? " (Current)"
                : academicYear.admissionYearType === "UPCOMING"
                    ? " (Upcoming)"
                    : "";

        const option =
            new Option(
                `${code}${suffix}`,
                String(academicYearId)
            );

        academicYearSelect.appendChild(option);
        availableYearCount++;

        if (academicYear.admissionYearType === "CURRENT"
                && preferredAcademicYearId === null) {
            preferredAcademicYearId =
                academicYearId;
        }
    });

    if (availableYearCount === 0) {
        setupDefaultOption(
            academicYearSelect,
            "No Admission-Open Year"
        );
        return;
    }

    if (preferredAcademicYearId === null) {
        preferredAcademicYearId =
            Number(
                academicYearSelect.options[1].value
            );
    }

    academicYearSelect.value =
        String(preferredAcademicYearId);

    /*
     * One admission-open year is selected and locked. When the school opens
     * both the current and an upcoming year, the dropdown becomes editable.
     */
    academicYearSelect.disabled =
        availableYearCount === 1;

    populateTermsForAcademicYear(
        joiningTermSelect,
        branch,
        preferredAcademicYearId
    );
}

function handleAcademicYearChange() {
    const branch = getSelectedBranch();

    populateTermsForAcademicYear(
        document.getElementById("joiningTerm"),
        branch,
        Number(this.value)
    );
}

function populateTermsForAcademicYear(
    joiningTermSelect,
    branch,
    academicYearId
) {
    setupDefaultOption(
        joiningTermSelect,
        "Select Admission Term"
    );

    joiningTermSelect.disabled = true;

    if (!branch
            || !Number.isInteger(academicYearId)
            || academicYearId <= 0) {
        return;
    }

    const academicYear =
        Array.isArray(branch.academicYears)
            ? branch.academicYears.find(
                year =>
                    Number(year.academicYearId)
                    === academicYearId
            )
            : null;

    const terms =
        academicYear
        && Array.isArray(academicYear.terms)
            ? academicYear.terms
            : [];

    let preferredTermId = null;
    let availableTermCount = 0;

    terms.forEach(term => {
        const termId = Number(term.termId);

        if (!Number.isInteger(termId)
                || termId <= 0) {
            return;
        }

        const code =
            term.termCode
            || String(termId);

        const name =
            term.termName;

        const termLabel =
            name && name !== code
                ? name
                : code;

        const suffix =
            term.currentTerm === true
                ? " (Current)"
                : " (Upcoming)";

        const option =
            new Option(
                `${termLabel}${suffix}`,
                String(termId)
            );

        joiningTermSelect.appendChild(option);
        availableTermCount++;

        if (term.currentTerm === true
                && preferredTermId === null) {
            preferredTermId = termId;
        }
    });

    if (availableTermCount === 0) {
        setupDefaultOption(
            joiningTermSelect,
            "No Active Admission Term"
        );
        return;
    }

    if (preferredTermId === null) {
        preferredTermId =
            Number(
                joiningTermSelect.options[1].value
            );
    }

    joiningTermSelect.value =
        String(preferredTermId);

    joiningTermSelect.disabled =
        availableTermCount === 1;
}

function getSelectedBranch() {
    const branchId =
        Number(
            document.getElementById("branchSelect").value
        );

    if (!Number.isInteger(branchId)
            || branchId <= 0) {
        return null;
    }

    return branchList.find(
        branch =>
            Number(branch.branchId) === branchId
    ) || null;
}

function handleLevelChange() {
    const classSelect = document.getElementById("classSelect");
    setupDefaultOption(classSelect, "Select Class");
    document.getElementById("dynamic-exam-container").textContent = '';
    document.getElementById("dynamic-subjects-container").classList.add("hidden-element");

    const hiddenCode = document.getElementById("hiddenClassCode");
    const hiddenApplied = document.getElementById("hiddenAppliedClass");
    if (hiddenCode) hiddenCode.value = "";
    if (hiddenApplied) hiddenApplied.value = "";

    const selectedLevelName = this.value;
    const selectedBranchId = parseInt(document.getElementById("branchSelect").value);
    const branch = branchList.find(b => b.branchId === selectedBranchId);

    if (branch && branch.branchLevels) {
        const blObj = branch.branchLevels.find(bl => bl.level.levelName === selectedLevelName);
        if (blObj) {
            const levelObj = blObj.level;
            // Filter dynamic classList by mapped Level ID
            const filteredClasses = classList.filter(c => c.levelId === levelObj.levelId);
            filteredClasses.forEach(c => {
                classSelect.appendChild(new Option('[' + c.classCode + '] ' + c.className, c.classCode));
            });
        }
    }
}

function handleClassChange() {
    const code = this.value;

    const hiddenCode = document.getElementById("hiddenClassCode");
    const hiddenApplied = document.getElementById("hiddenAppliedClass");

    if (hiddenCode) hiddenCode.value = code;
    if (hiddenApplied) hiddenApplied.value = this.options[this.selectedIndex].text;

    const dynExam = document.getElementById("dynamic-exam-container");
    const dynSubj = document.getElementById("dynamic-subjects-container");
    dynExam.textContent = '';
    dynSubj.classList.add("hidden-element");

    if (code === "S1") {
        const tmpl = document.getElementById("tmpl-exam-s1");
        dynExam.appendChild(tmpl.content.cloneNode(true));
    }
    else if (code === "S5") {
        const tmpl = document.getElementById("tmpl-exam-s5");
        dynExam.appendChild(tmpl.content.cloneNode(true));
    }
    else if (code) {
        const tmpl = document.getElementById("tmpl-exam-other");
        dynExam.appendChild(tmpl.content.cloneNode(true));
    }

    if (code !== "") {
        dynSubj.classList.remove("hidden-element");
        if (document.getElementById("subjectsBody").children.length === 0) addSubjectRow();
    }
}

function addSubjectRow() {
    const tmpl = document.getElementById("tmpl-subject-row");
    document.getElementById("subjectsBody").appendChild(tmpl.content.cloneNode(true));
}
function handleTableClick(e) { if (e.target.closest(".btn-icon")) { e.target.closest("tr").remove(); calculateSubjectTotal(); } }

function calculateSubjectTotal() {
    let total = 0;
    document.querySelectorAll("input.subject-mark-input").forEach(input => {
        const val = parseFloat(input.value);
        if (!isNaN(val)) total += val;
    });
    document.getElementById("totalSubjectScore").textContent = String(total);
}

function collectSubjectMarks() {
    const rows =
        document.querySelectorAll(
            "#subjectsBody tr"
        );

    const subjectMarks = [];

    rows.forEach(row => {
        const nameInput =
            row.querySelector(
                "input[name='subject_name[]']"
            );

        const markInput =
            row.querySelector(
                "input[name='subject_mark[]']"
            );

        const gradeInput =
            row.querySelector(
                "input[name='subject_grade[]']"
            );

        if (!nameInput && !markInput && !gradeInput) {
            return;
        }

        const subject =
            nameInput
                ? nameInput.value.trim()
                : "";

        const marks =
            markInput
                ? markInput.value.trim()
                : "";

        const grade =
            gradeInput
                ? gradeInput.value.trim()
                : "";

        /*
         * Subject Marks are stored as JSON on the application.
         * Preserve the existing data shape used by the backend:
         * { subject, marks, grade }.
         */
        if (
            subject !== ""
            || marks !== ""
            || grade !== ""
        ) {
            subjectMarks.push({
                subject,
                marks,
                grade
            });
        }
    });

    return subjectMarks;
}

async function handleFormSubmit(e) {
    e.preventDefault();
    const form = e.target;
    const btnSubmit = document.getElementById("btn-submit");
    const btnText = document.getElementById("btn-submit-text");
    const btnSpinner = document.getElementById("btn-submit-spinner");

    const honeypot = document.getElementById("fax_number_trap");
    if (honeypot && honeypot.value.trim() !== "") {
        console.warn("Bot detected by honeypot. Request killed.");
        document.getElementById("final-ref-number").textContent = "APP-2026-U011-" + Math.floor(Math.random() * 900 + 100);
        goToStep(7);
        return;
    }

    const subjectMarks = collectSubjectMarks();

    const incompleteSubject =
        subjectMarks.find(mark =>
            mark.subject === ""
            || mark.marks === ""
            || mark.grade === ""
        );

    if (incompleteSubject) {
        showAlert(
            "Incomplete Subject Marks",
            "Please complete Subject, Marks /100, and Grade for every subject row, or remove the row.",
            "warning"
        );
        return;
    }

    if (btnSubmit) btnSubmit.disabled = true;
    if (btnText) btnText.classList.add("hidden-element");
    if (btnSpinner) btnSpinner.classList.remove("hidden-element");

    // Look up the TRUE classId from the selected classCode
    const selectedClassCode = document.getElementById("classSelect").value;
    const selectedClassObj = classList.find(c => c.classCode === selectedClassCode);
    const trueClassId = selectedClassObj ? selectedClassObj.classId : null;
    const primaryContactType = getPrimaryContactType();
    const primaryPrefix = primaryContactType === "FATHER"
        ? "father"
        : primaryContactType === "MOTHER"
            ? "mother"
            : "guardian";
    const primaryEmail = form.querySelector(`[name='${primaryPrefix}Email']`);
    const primaryMobile = form.querySelector(`[name='${primaryPrefix}Contact']`);
    const hasOtherPrimaryContact = primaryContactType === "OTHER";

    // MAPPING FRONTEND TO BACKEND: Create the clean ApplicationCreateDTO JSON payload
    const payload = {
        branchId: document.getElementById("branchSelect").value ? parseInt(document.getElementById("branchSelect").value) : null,
        academicYearId: document.getElementById("academicYear")
            && document.getElementById("academicYear").value
                ? parseInt(
                    document.getElementById("academicYear").value,
                    10
                )
                : null,
        joiningTermId: document.getElementById("joiningTerm")
            && document.getElementById("joiningTerm").value
                ? parseInt(
                    document.getElementById("joiningTerm").value,
                    10
                )
                : null,
        branchClassId: trueClassId,

        // Primary contact selection is persisted by the backend.
        primaryContactType: primaryContactType,
        // Keep the existing API fields for compatibility; the backend derives
        // them again from the selected contact to prevent mismatched data.
        primaryEmail: primaryEmail ? primaryEmail.value : "",
        primaryMobile: primaryMobile ? primaryMobile.value : "",

        firstName: form.querySelector("[name='studentName']").value,
        middleName: form.querySelector("[name='middleName']") ? form.querySelector("[name='middleName']").value : "",
        lastName: form.querySelector("[name='studentSurname']").value,
        gender: form.querySelector("input[name='gender']:checked") ? form.querySelector("input[name='gender']:checked").value.toUpperCase() : "MALE",
        dateOfBirth: form.querySelector("[name='dob']") && form.querySelector("[name='dob']").value ? form.querySelector("[name='dob']").value : null,
        dateOfRegistration: form.querySelector("[name='dateOfRegistration']") ? form.querySelector("[name='dateOfRegistration']").value : null,
        nationality: form.querySelector("[name='nationality']") ? form.querySelector("[name='nationality']").value : "Uganda",
        religionId: document.getElementById("religionId") && document.getElementById("religionId").value ? parseInt(document.getElementById("religionId").value) : null,
        bloodGroupId: document.getElementById("bloodGroupId") && document.getElementById("bloodGroupId").value ? parseInt(document.getElementById("bloodGroupId").value) : null,
        categoryId: document.getElementById("categoryId") && document.getElementById("categoryId").value ? parseInt(document.getElementById("categoryId").value) : null,

        addressHouse: form.querySelector("[name='addressHouse']") ? form.querySelector("[name='addressHouse']").value : "",
        addressStreet: form.querySelector("[name='addressStreet']") ? form.querySelector("[name='addressStreet']").value : "",
        addressVillage: form.querySelector("[name='addressVillage']") ? form.querySelector("[name='addressVillage']").value : "",
        addressDistrict: form.querySelector("[name='addressDistrict']") ? form.querySelector("[name='addressDistrict']").value : "",
        addressState: form.querySelector("[name='addressState']") ? form.querySelector("[name='addressState']").value : "",
        addressPostal: form.querySelector("[name='addressPostal']") ? form.querySelector("[name='addressPostal']").value : "",

        fatherName: form.querySelector("[name='fatherName']") ? form.querySelector("[name='fatherName']").value : "",
        fatherAge: form.querySelector("[name='fatherAge']") && form.querySelector("[name='fatherAge']").value ? parseInt(form.querySelector("[name='fatherAge']").value) : null,
        fatherContact: form.querySelector("[name='fatherContact']") ? form.querySelector("[name='fatherContact']").value : "",
        fatherEducation: form.querySelector("[name='fatherEducation']") ? form.querySelector("[name='fatherEducation']").value : "",
        fatherOccupation: form.querySelector("[name='fatherOccupation']") ? form.querySelector("[name='fatherOccupation']").value : "",
        fatherEmail: form.querySelector("[name='fatherEmail']") ? form.querySelector("[name='fatherEmail']").value : "",

        motherName: form.querySelector("[name='motherName']") ? form.querySelector("[name='motherName']").value : "",
        motherAge: form.querySelector("[name='motherAge']") && form.querySelector("[name='motherAge']").value ? parseInt(form.querySelector("[name='motherAge']").value) : null,
        motherContact: form.querySelector("[name='motherContact']") ? form.querySelector("[name='motherContact']").value : "",
        motherEducation: form.querySelector("[name='motherEducation']") ? form.querySelector("[name='motherEducation']").value : "",
        motherOccupation: form.querySelector("[name='motherOccupation']") ? form.querySelector("[name='motherOccupation']").value : "",
        motherEmail: form.querySelector("[name='motherEmail']") ? form.querySelector("[name='motherEmail']").value : "",

        guardianName: hasOtherPrimaryContact && form.querySelector("[name='guardianName']") ? form.querySelector("[name='guardianName']").value : "",
        guardianMobile: hasOtherPrimaryContact && form.querySelector("[name='guardianContact']") ? form.querySelector("[name='guardianContact']").value : "",
        guardianEmail: hasOtherPrimaryContact && form.querySelector("[name='guardianEmail']") ? form.querySelector("[name='guardianEmail']").value : "",
        guardianAge: hasOtherPrimaryContact && form.querySelector("[name='guardianAge']") && form.querySelector("[name='guardianAge']").value ? parseInt(form.querySelector("[name='guardianAge']").value) : null,
        guardianEducation: hasOtherPrimaryContact && form.querySelector("[name='guardianEducation']") ? form.querySelector("[name='guardianEducation']").value : "",
        guardianOccupation: hasOtherPrimaryContact && form.querySelector("[name='guardianOccupation']") ? form.querySelector("[name='guardianOccupation']").value : "",
        guardianRelation: hasOtherPrimaryContact && form.querySelector("[name='guardianRelation']") ? form.querySelector("[name='guardianRelation']").value : "",
        guardianLocation: hasOtherPrimaryContact && form.querySelector("[name='guardianLocation']") ? form.querySelector("[name='guardianLocation']").value : "",

        previousSchool: form.querySelector("[name='formerSchool']") ? form.querySelector("[name='formerSchool']").value : "",
        formerSchoolCode: form.querySelector("[name='formerSchoolCode']") ? form.querySelector("[name='formerSchoolCode']").value : "",
        formerSchoolLin: form.querySelector("[name='formerSchoolLin']") ? form.querySelector("[name='formerSchoolLin']").value : "",
        pleRef: form.querySelector("[name='pleRef']") ? form.querySelector("[name='pleRef']").value : "",
        pleScore: form.querySelector("[name='pleScore']") && form.querySelector("[name='pleScore']").value ? parseFloat(form.querySelector("[name='pleScore']").value) : null,
        uceRef: form.querySelector("[name='uceRef']") ? form.querySelector("[name='uceRef']").value : "",
        uceScore: form.querySelector("[name='uceScore']") && form.querySelector("[name='uceScore']").value ? parseFloat(form.querySelector("[name='uceScore']").value) : null,
        /*
         * Subject Marks are entered through the dynamic table, not through a
         * single subjectMarks form control. Serialize those rows explicitly
         * before sending the ApplicationCreateDTO JSON payload.
         */
        subjectMarks: JSON.stringify(
            subjectMarks
        ),
        scholarshipStatus: form.querySelector("[name='scholarshipStatus']") ? form.querySelector("[name='scholarshipStatus']").value : "",
        moreInfo: form.querySelector("[name='moreInfo']") ? form.querySelector("[name='moreInfo']").value : ""
    };

    try {
        const response = await fetch("/api/public/applications/submit", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        const result = await response.json();

        if (response.ok && result.applicationNo) {
            const fileData = new FormData();
            const photoInput = document.getElementById("photoInput");
            if (photoInput && photoInput.files.length > 0) {
                fileData.append("photo", photoInput.files[0]);
            }
            const docInputs = document.querySelectorAll("input[type='file'][name='prevMarks']");
            docInputs.forEach(input => {
                if (input.files && input.files.length > 0) {
                    for (let i = 0; i < input.files.length; i++) {
                        fileData.append("documents", input.files[i]);
                    }
                }
            });
            if (fileData.has("photo") || fileData.has("documents")) {
                try {
                    await fetch('/api/public/applications/' + result.applicationNo + '/upload', {
                        method: 'POST',
                        body: fileData
                    });
                } catch (uploadErr) {
                    console.warn("File upload issue:", uploadErr);
                }
            }
            try {
                const sessionForm = new URLSearchParams();
                sessionForm.append("ref_number", result.applicationNo);
                sessionForm.append("dob", payload.dateOfBirth || "");

                const sessionRes = await fetch('/api/public/applications/status', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: sessionForm
                });

                const sessionData = await sessionRes.json();
                sessionData.guest_auth_token = undefined;
                if (sessionData.success && sessionData.guest_auth_token) {
                    sessionStorage.setItem('guest_auth_token', sessionData.guest_auth_token);
                }
            } catch (sessionErr) {
                console.warn("Could not generate guest print session:", sessionErr);
            }
            document.getElementById("final-ref-number").textContent = String(result.applicationNo);
            goToStep(7);

            const printBtn = document.getElementById("downloadPdfBtn");
            const newBtn = printBtn.cloneNode(true);
            printBtn.parentNode.replaceChild(newBtn, printBtn);

            newBtn.addEventListener("click", () => {
                window.open('/apply/print_application?ref=' + result.applicationNo, '_blank');
            });

        } else {
            showAlert('Error', String(result.message || result.error || "Submission failed."), 'error');
            if (btnSubmit) btnSubmit.disabled = false;
            if (btnText) btnText.classList.remove("hidden-element");
            if (btnSpinner) btnSpinner.classList.add("hidden-element");
        }
    } catch (error) {
        showAlert('Network Error', 'Please check connection.', 'error');
        if (btnSubmit) btnSubmit.disabled = false;
        if (btnText) btnText.classList.remove("hidden-element");
        if (btnSpinner) btnSpinner.classList.add("hidden-element");
    }
}
// =======================================================
// CSP-SAFE: ENFORCE +256 PREFIX ON PHONE FIELDS
// =======================================================
document.addEventListener("DOMContentLoaded", function() {
    // Select all the visible display inputs
    const displayInputs = document.querySelectorAll('.phone-padded');

    displayInputs.forEach(displayInput => {
        displayInput.addEventListener('input', function() {
            // Find the hidden input that matches this display input
            const hiddenId = this.id.replace('_display', '_hidden');
            const hiddenInput = document.getElementById(hiddenId);

            if (hiddenInput) {
                if (this.id === 'primaryMobile_display') {
                    // Primary is required, so always prepend +256
                    hiddenInput.value = '+256 ' + this.value;
                } else {
                    // Others are optional, so only prepend if they typed something
                    hiddenInput.value = this.value ? '+256 ' + this.value : '';
                }
            }
        });
    });
});

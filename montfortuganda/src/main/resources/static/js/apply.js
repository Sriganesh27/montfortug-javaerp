/* global Swal, window */

const classData = {
    "Nursery": [{ code: "N1", name: "Baby Class" }, { code: "N2", name: "Middle Class" }, { code: "N3", name: "Top Class" }],
    "Primary": [{ code: "P1", name: "Primary 1" }, { code: "P2", name: "Primary 2" }, { code: "P3", name: "Primary 3" }, { code: "P4", name: "Primary 4" }, { code: "P5", name: "Primary 5" }, { code: "P6", name: "Primary 6" }, { code: "P7", name: "Primary 7" }],
    "Secondary": [{ code: "S1", name: "Senior 1" }, { code: "S2", name: "Senior 2" }, { code: "S3", name: "Senior 3" }, { code: "S4", name: "Senior 4" }, { code: "S5", name: "Senior 5" }, { code: "S6", name: "Senior 6" }]
};
let branchList = [];

// Helper to safely trigger SweetAlert without ARIA focus-retention violations
function showAlert(title, text, icon) {
    // FIX: Explicitly remove focus from the currently active element (e.g. the select box or button)
    // before the modal opens. This prevents Chrome from throwing "Blocked aria-hidden on an element
    // because its descendant retained focus" errors.
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
    fetchBranches().catch(e => console.error("Initialization error:", e));

    document.getElementById("branchSelect").addEventListener("change", handleBranchChange);
    document.getElementById("level").addEventListener("change", handleLevelChange);
    document.getElementById("classSelect").addEventListener("change", handleClassChange);
    document.getElementById("addSubjectBtn").addEventListener("click", addSubjectRow);
    document.getElementById("subjectsBody").addEventListener("click", handleTableClick);
    document.getElementById("subjectsBody").addEventListener("input", calculateSubjectTotal);
    document.getElementById("admission-form").addEventListener("submit", handleFormSubmit);

    document.addEventListener("change", handleFileUploadUI);

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

function handleFileUploadUI(e) {
    if (e.target.type === "file") {
        const file = e.target.files[0];
        const span = e.target.closest('.file-dropzone').querySelector('span');
        if (file) {
            span.textContent = file.name;
            span.classList.add("file-selected-text");
            if (e.target.id === "photoInput" && file.size > 51200) {
                showAlert('Warning', 'Photo is larger than 50KB. The system might reject it.', 'warning');
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

// VALIDATION ENGINE
function validateAndGoTo(targetStepNumber) {
    const activeStepDiv = document.querySelector(".form-step.active-step");
    if (!activeStepDiv) { goToStep(targetStepNumber); return; }

    const currentStep = parseInt(activeStepDiv.id.replace("step-", ""));

    // If they are going backwards, no need to validate
    if (targetStepNumber < currentStep) {
        goToStep(targetStepNumber);
        return;
    }

    // Check validation for the current active step
    const requiredInputs = activeStepDiv.querySelectorAll("input[required], select[required]");
    let isValid = true;
    let firstInvalid = null;

    requiredInputs.forEach(input => {
        // Duck-typing validity to satisfy IDE without redundant variable
        if (input.checkValidity && !input.checkValidity()) {
            isValid = false;
            if (!firstInvalid) firstInvalid = input;
            input.classList.add("error-border");
        } else {
            input.classList.remove("error-border");
        }
    });

    if (!isValid) {
        showAlert('Missing Fields', 'Please fill in all required fields marked in red.', 'warning')
            .then(() => {
                if (firstInvalid && firstInvalid.focus) firstInvalid.focus();
            });
        return;
    }

    if (targetStepNumber > currentStep + 1) {
        showAlert('Notice', 'Please complete the form steps in order.', 'info');
        goToStep(currentStep + 1);
        return;
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

            // Safe DOM rendering
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
    container.textContent = ''; // Safely clear container
    const form = document.getElementById("admission-form");

    // Using input[name=...] helps IDE naturally infer it's an input
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

    // Helper to generate DOM elements without HTML strings
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
        span.textContent = value;

        div.appendChild(strong);
        div.appendChild(span);
        container.appendChild(div);
    };

    appendSectionBreak("Enrollment");
    appendItem("Branch", getSelectText('branchSelect'));
    appendItem("Class", getSelectText('classSelect'));
    appendItem("Year & Term", `${getVal('academicYear')} - ${getRadio('term')}`);

    appendSectionBreak("Student Info");
    appendItem("Full Name", `${getVal('studentName')} ${getVal('middleName')} ${getVal('studentSurname')}`);
    appendItem("Gender", getRadio('gender'));
    appendItem("DOB & Nationality", `${getVal('dob') || 'N/A'} | ${getVal('nationality')}`);

    appendSectionBreak("Residential Address");
    appendItem("Full Address", addressString || 'Not Provided', true);

    appendSectionBreak("Family Contact");
    appendItem("Father", `${getVal('fatherName') || 'N/A'} ${getVal('fatherContact') ? '('+getVal('fatherContact')+')' : ''}`);
    appendItem("Mother", `${getVal('motherName') || 'N/A'} ${getVal('motherContact') ? '('+getVal('motherContact')+')' : ''}`);
    appendItem("Guardian", `${getVal('guardianName') || 'N/A'} ${getVal('guardianContact') ? '('+getVal('guardianContact')+')' : ''} ${getVal('guardianLocation') ? '- '+getVal('guardianLocation') : ''}`);

    appendSectionBreak("Academic Details");
    appendItem("Former School", `${getVal('formerSchool') || 'N/A'} ${getVal('formerSchoolLin') ? '(LIN: '+getVal('formerSchoolLin')+')' : ''}`);
    appendItem("Attachments", "Photo & Documents Ready for Upload");
}

function setupDefaultOption(selectElem, text="Select...") {
    selectElem.options.length = 0;
    const defaultOpt = document.createElement("option");
    defaultOpt.value = ""; defaultOpt.disabled = true; defaultOpt.selected = true; defaultOpt.textContent = text;
    selectElem.appendChild(defaultOpt);
}

async function fetchBranches() {
    setupDefaultOption(document.getElementById("branchSelect"), "Select Branch");
    const response = await fetch("/api/public/branches");
    const result = await response.json();
    if (result.success && result.data) {
        branchList = result.data;
        result.data.forEach(branch => {
            const opt = document.createElement("option");
            opt.value = String(branch.branchId);
            opt.textContent = `${branch.schoolCode ? '['+branch.schoolCode+'] ' : ''}${branch.branchName}`;
            document.getElementById("branchSelect").appendChild(opt);
        });
    }
}

function handleBranchChange() {
    const selectedId = parseInt(this.value);
    const branch = branchList.find(b => b.branchId === selectedId);

    const levelSelect = document.getElementById("level");

    setupDefaultOption(levelSelect, "Select Level");
    setupDefaultOption(document.getElementById("classSelect"), "Select Class");
    document.getElementById("dynamic-exam-container").textContent = '';
    document.getElementById("dynamic-subjects-container").classList.add("hidden-element");

    if (branch && branch.branchType) {
        const bType = branch.branchType.toLowerCase();
        Object.keys(classData).forEach(lvl => {
            let add = false;
            if (bType.includes("primary") && (lvl === "Primary" || lvl === "Nursery")) add = true;
            else if (bType.includes("secondary") && lvl === "Secondary") add = true;
            else if (bType === lvl.toLowerCase()) add = true;
            if (add) levelSelect.appendChild(new Option(lvl, lvl));
        });
    }
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

    if(this.value && classData[this.value]) {
        classData[this.value].forEach(c => classSelect.appendChild(new Option(c.name, c.code)));
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

    if (!code.startsWith("N") && code !== "") {
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
    // By querying 'input.subject-mark-input', IDEs naturally infer this is an HTMLInputElement!
    document.querySelectorAll("input.subject-mark-input").forEach(input => {
        const val = parseFloat(input.value);
        if (!isNaN(val)) total += val;
    });
    document.getElementById("totalSubjectScore").textContent = String(total);
}

async function handleFormSubmit(e) {
    e.preventDefault();
    const btnSubmit = document.getElementById("btn-submit");
    const btnText = document.getElementById("btn-submit-text");
    const btnSpinner = document.getElementById("btn-submit-spinner");

    // HONEYPOT INTERCEPTOR
    const honeypot = document.getElementById("fax_number_trap");
    if (honeypot && honeypot.value.trim() !== "") {
        console.warn("Bot detected by honeypot. Request killed.");
        document.getElementById("final-ref-number").textContent = "APP-U011-26-" + Math.floor(Math.random() * 900 + 100);
        goToStep(7);
        return;
    }

    // BUTTON THROTTLING
    if (btnSubmit) btnSubmit.disabled = true;
    if (btnText) btnText.classList.add("hidden-element");
    if (btnSpinner) btnSpinner.classList.remove("hidden-element");

    let subjectArray = [];
    document.querySelectorAll("#subjectsBody tr").forEach(row => {
        const nameInput = row.querySelector("input[name='subject_name[]']");
        const markInput = row.querySelector("input[name='subject_mark[]']");
        const gradeInput = row.querySelector("input[name='subject_grade[]']");

        const sName = nameInput ? nameInput.value.trim() : "";
        const sMark = markInput ? markInput.value.trim() : "";
        const sGrade = gradeInput ? gradeInput.value.trim() : "";
        if (sName) subjectArray.push({ subject: sName, marks: sMark, grade: sGrade });
    });

    const formData = new FormData(this);
    if (subjectArray.length > 0) formData.append("subjectMarks", JSON.stringify(subjectArray));

    try {
        const response = await fetch("/api/public/applications/submit", { method: "POST", body: formData });
        const result = await response.json();

        if (result.success) {
            document.getElementById("final-ref-number").textContent = String(result['ref_number']);
            goToStep(7);
            document.getElementById("downloadPdfBtn").addEventListener("click", () => generatePDF(result['ref_number'], formData));
        } else {
            showAlert('Error', String(result.message) || "Submission failed.", 'error');
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

function generatePDF(refNum, formData) {
    const { jsPDF } = window['jspdf'];
    const doc = new jsPDF();
    const form = document.getElementById("admission-form");

    doc.setFont("helvetica", "bold"); doc.setFontSize(22); doc.setTextColor(15, 23, 42);
    doc.text("Montfort Brothers of St. Gabriel", 105, 20, null, null, "center");
    doc.setFontSize(14); doc.setTextColor(107, 114, 128); doc.text("Official Application Copy", 105, 30, null, null, "center");
    doc.setFontSize(16); doc.setTextColor(15, 23, 42); doc.text(`Ref Number: ${refNum}`, 105, 45, null, null, "center");
    doc.setFontSize(11); doc.setTextColor(0, 0, 0); doc.setFont("helvetica", "normal");

    let y = 60;
    doc.text(`Student Name: ${formData.get("studentName")} ${formData.get("studentSurname")}`, 20, y); y+=10;
    doc.text(`Class Applied: ${formData.get("appliedClass")}`, 20, y); y+=10;

    const termInput = form.querySelector('input[name="term"]:checked');
    doc.text(`Term: ${termInput ? termInput.value : ''}`, 20, y); y+=10;

    doc.setLineWidth(0.5); doc.setDrawColor(229, 231, 235); doc.line(20, y+5, 190, y+5); y+=15;
    doc.setFontSize(10); doc.setTextColor(100, 100, 100);
    doc.text("Please carry this document to the school administration block.", 105, y, null, null, "center");
    doc.save(`Montfort_Application_${refNum}.pdf`);
}
/* global Swal, jspdf */

const classData = {
    "Nursery": [{ code: "N1", name: "Baby Class" }, { code: "N2", name: "Middle Class" }, { code: "N3", name: "Top Class" }],
    "Primary": [{ code: "P1", name: "Primary 1" }, { code: "P2", name: "Primary 2" }, { code: "P3", name: "Primary 3" }, { code: "P4", name: "Primary 4" }, { code: "P5", name: "Primary 5" }, { code: "P6", name: "Primary 6" }, { code: "P7", name: "Primary 7" }],
    "Secondary": [{ code: "S1", name: "Senior 1" }, { code: "S2", name: "Senior 2" }, { code: "S3", name: "Senior 3" }, { code: "S4", name: "Senior 4" }, { code: "S5", name: "Senior 5" }, { code: "S6", name: "Senior 6" }]
};
let branchList = [];

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
});

function handleFileUploadUI(e) {
    if (e.target.type === "file") {
        const file = e.target.files[0];
        const span = e.target.closest('.file-dropzone').querySelector('span');
        if (file) {
            span.textContent = file.name;
            span.style.color = "#0f172a"; span.style.fontWeight = "600";
            if (e.target.id === "photoInput" && file.size > 51200) {
                if (typeof Swal !== 'undefined') Swal.fire('Warning', 'Photo is larger than 50KB. The system might reject it.', 'warning');
                else alert('Warning: Photo is larger than 50KB.');
            }
        } else {
            span.textContent = "Choose a file or drag it here";
            span.style.color = ""; span.style.fontWeight = "";
        }
    }
}

function startJavascriptSlider() {
    const slides = document.querySelectorAll('#montfort-portal-wrapper .slide');
    if (slides.length === 0) return;
    let currentSlide = 0;
    setInterval(() => {
        slides[currentSlide].classList.remove('active-slide');
        currentSlide = (currentSlide + 1) % slides.length;
        slides[currentSlide].classList.add('active-slide');
    }, 6000);
}

// NEW VALIDATION ENGINE
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
    const requiredInputs = activeStepDiv.querySelectorAll("[required]");
    let isValid = true;
    let firstInvalid = null;

    requiredInputs.forEach(input => {
        if (!input.checkValidity()) {
            isValid = false;
            if (!firstInvalid) firstInvalid = input;
            input.classList.add("error-border");
        } else {
            input.classList.remove("error-border");
        }
    });

    // If validation fails, alert user and focus on the red field
    if (!isValid) {
        if (typeof Swal !== 'undefined') Swal.fire('Missing Fields', 'Please fill in all required fields marked in red.', 'warning');
        else alert("Please fill in all required fields.");
        if (firstInvalid) firstInvalid.focus();
        return;
    }

    // Force them to complete steps in order (prevent jumping from Step 1 straight to Step 6)
    if (targetStepNumber > currentStep + 1) {
        if (typeof Swal !== 'undefined') Swal.fire('Notice', 'Please complete the form steps in order.', 'info');
        goToStep(currentStep + 1);
        return;
    }

    // If all clear, proceed to the requested step
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

            if (stepNum < targetStepNumber) {
                item.classList.add("completed");
                counter.innerHTML = '<i class="bi bi-check2"></i>';
            } else if (stepNum === targetStepNumber) {
                item.classList.add("active");
                counter.innerHTML = stepNum;
            } else {
                counter.innerHTML = stepNum;
            }
        });
    }
    document.querySelector("#montfort-portal-wrapper .form-document-card").scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function buildReviewSummary() {
    const container = document.getElementById("review-summary-container");
    const form = document.getElementById("admission-form");

    const getVal = (name) => form.querySelector(`[name="${name}"]`) ? form.querySelector(`[name="${name}"]`).value : '';
    const getRadio = (name) => form.querySelector(`[name="${name}"]:checked`) ? form.querySelector(`[name="${name}"]:checked`).value : 'Not Selected';
    const getSelectText = (id) => { const el = document.getElementById(id); return (el && el.selectedIndex > 0) ? el.options[el.selectedIndex].text : 'Not Selected'; };

    let fullAddress = [];
    if(getVal('addressHouse')) fullAddress.push(getVal('addressHouse'));
    if(getVal('addressStreet')) fullAddress.push(getVal('addressStreet'));
    if(getVal('addressVillage')) fullAddress.push(getVal('addressVillage'));
    if(getVal('addressDistrict')) fullAddress.push(getVal('addressDistrict'));
    if(getVal('addressState')) fullAddress.push(getVal('addressState'));
    if(getVal('addressCountry')) fullAddress.push(getVal('addressCountry'));
    let addressString = fullAddress.join(', ');
    if(getVal('addressPostal')) addressString += ` (P.O. Box: ${getVal('addressPostal')})`;

    container.innerHTML = `
        <div class="review-section-break">Enrollment</div>
        <div class="review-item"><strong>Branch</strong><span>${getSelectText('branchSelect')}</span></div>
        <div class="review-item"><strong>Class</strong><span>${getSelectText('classSelect')}</span></div>
        <div class="review-item"><strong>Year & Term</strong><span>${getVal('academicYear')} - ${getRadio('term')}</span></div>
        
        <div class="review-section-break">Student Info</div>
        <div class="review-item"><strong>Full Name</strong><span>${getVal('studentName')} ${getVal('middleName')} ${getVal('studentSurname')}</span></div>
        <div class="review-item"><strong>Gender</strong><span>${getRadio('gender')}</span></div>
        <div class="review-item"><strong>DOB & Nationality</strong><span>${getVal('dob') || 'N/A'} | ${getVal('nationality')}</span></div>
        
        <div class="review-section-break">Residential Address</div>
        <div class="review-item" style="grid-column: 1 / -1;"><strong>Full Address</strong><span>${addressString || 'Not Provided'}</span></div>

        <div class="review-section-break">Family Contact</div>
        <div class="review-item"><strong>Father</strong><span>${getVal('fatherName') || 'N/A'} ${getVal('fatherContact') ? '('+getVal('fatherContact')+')' : ''}</span></div>
        <div class="review-item"><strong>Mother</strong><span>${getVal('motherName') || 'N/A'} ${getVal('motherContact') ? '('+getVal('motherContact')+')' : ''}</span></div>
        <div class="review-item"><strong>Guardian</strong><span>${getVal('guardianName') || 'N/A'} ${getVal('guardianContact') ? '('+getVal('guardianContact')+')' : ''} ${getVal('guardianLocation') ? '- '+getVal('guardianLocation') : ''}</span></div>
        
        <div class="review-section-break">Academic Details</div>
        <div class="review-item"><strong>Former School</strong><span>${getVal('formerSchool') || 'N/A'} ${getVal('formerSchoolLin') ? '(LIN: '+getVal('formerSchoolLin')+')' : ''}</span></div>
        <div class="review-item"><strong>Attachments</strong><span>Photo & Documents Ready for Upload</span></div>
    `;
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
            opt.value = branch.branchId;
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
    document.getElementById("dynamic-exam-container").innerHTML = '';
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
    document.getElementById("dynamic-exam-container").innerHTML = '';
    document.getElementById("dynamic-subjects-container").classList.add("hidden-element");
    document.getElementById("hiddenClassCode").value = "";
    document.getElementById("hiddenAppliedClass").value = "";

    if(this.value && classData[this.value]) {
        classData[this.value].forEach(c => classSelect.appendChild(new Option(c.name, c.code)));
    }
}

function handleClassChange() {
    const code = this.value;
    document.getElementById("hiddenClassCode").value = code;
    document.getElementById("hiddenAppliedClass").value = this.options[this.selectedIndex].text;

    const dynExam = document.getElementById("dynamic-exam-container");
    const dynSubj = document.getElementById("dynamic-subjects-container");
    dynExam.innerHTML = '';
    dynSubj.classList.add("hidden-element");

    if (code === "S1") dynExam.appendChild(document.getElementById("tmpl-exam-s1").content.cloneNode(true));
    else if (code === "S5") dynExam.appendChild(document.getElementById("tmpl-exam-s5").content.cloneNode(true));
    else if (code) dynExam.appendChild(document.getElementById("tmpl-exam-other").content.cloneNode(true));

    if (!code.startsWith("N") && code !== "") {
        dynSubj.classList.remove("hidden-element");
        if (document.getElementById("subjectsBody").children.length === 0) addSubjectRow();
    }
}

function addSubjectRow() { document.getElementById("subjectsBody").appendChild(document.getElementById("tmpl-subject-row").content.cloneNode(true)); }
function handleTableClick(e) { if (e.target.closest(".btn-icon")) { e.target.closest("tr").remove(); calculateSubjectTotal(); } }
function calculateSubjectTotal() {
    let total = 0;
    document.querySelectorAll(".subject-mark-input").forEach(input => {
        const val = parseFloat(input.value);
        if (!isNaN(val)) total += val;
    });
    document.getElementById("totalSubjectScore").textContent = total;
}

async function handleFormSubmit(e) {
    e.preventDefault();
    const btnSubmit = document.getElementById("btn-submit");
    const btnText = document.getElementById("btn-submit-text");
    btnSubmit.disabled = true; btnText.innerHTML = "Submitting... <span class='spinner-border spinner-border-sm'></span>";

    let subjectArray = [];
    document.querySelectorAll("#subjectsBody tr").forEach(row => {
        const sName = row.querySelector("input[name='subject_name[]']").value.trim();
        const sMark = row.querySelector("input[name='subject_mark[]']").value.trim();
        const sGrade = row.querySelector("input[name='subject_grade[]']").value.trim();
        if (sName) subjectArray.push({ subject: sName, marks: sMark, grade: sGrade });
    });

    const formData = new FormData(this);
    if (subjectArray.length > 0) formData.append("subjectMarks", JSON.stringify(subjectArray));

    try {
        const response = await fetch("/api/public/applications/submit", { method: "POST", body: formData });
        const result = await response.json();

        if (result.success) {
            document.getElementById("final-ref-number").textContent = result['ref_number'];
            goToStep(7);
            document.getElementById("downloadPdfBtn").onclick = () => generatePDF(result['ref_number'], formData);
        } else {
            if(typeof Swal !== 'undefined') Swal.fire('Error', result.message || "Submission failed.", 'error');
            else alert("Error: " + result.message);
            btnSubmit.disabled = false; btnText.innerHTML = "Confirm & Submit <i class='bi bi-check2-circle'></i>";
        }
    } catch (error) {
        if(typeof Swal !== 'undefined') Swal.fire('Network Error', 'Please check connection.', 'error');
        else alert("Network error.");
        btnSubmit.disabled = false; btnText.innerHTML = "Confirm & Submit <i class='bi bi-check2-circle'></i>";
    }
}

function generatePDF(refNum, formData) {
    const { jsPDF } = window.jspdf;
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
    doc.text(`Term: ${form.querySelector('[name="term"]:checked') ? form.querySelector('[name="term"]:checked').value : ''}`, 20, y); y+=10;

    doc.setLineWidth(0.5); doc.setDrawColor(229, 231, 235); doc.line(20, y+5, 190, y+5); y+=15;
    doc.setFontSize(10); doc.setTextColor(100, 100, 100);
    doc.text("Please carry this document to the school administration block.", 105, y, null, null, "center");
    doc.save(`Montfort_Application_${refNum}.pdf`);
}
document.addEventListener("DOMContentLoaded", () => {
    const classData = {
        "Nursery": [
            { code: "N1", name: "Baby Class" },
            { code: "N2", name: "Middle Class" },
            { code: "N3", name: "Top Class" }
        ],
        "Primary": [
            { code: "P1", name: "Primary 1" },
            { code: "P2", name: "Primary 2" },
            { code: "P3", name: "Primary 3" },
            { code: "P4", name: "Primary 4" },
            { code: "P5", name: "Primary 5" },
            { code: "P6", name: "Primary 6" },
            { code: "P7", name: "Primary 7" }
        ],
        "Secondary": [
            { code: "S1", name: "Senior 1" },
            { code: "S2", name: "Senior 2" },
            { code: "S3", name: "Senior 3" },
            { code: "S4", name: "Senior 4" },
            { code: "S5", name: "Senior 5" },
            { code: "S6", name: "Senior 6" }
        ]
    };

    let branchList = []; // Store fetched branches here for logic filtering

    const form = document.getElementById("admission-form");
    const branchSelect = document.getElementById("branchSelect");
    const levelSelect = document.getElementById("level");
    const classSelect = document.getElementById("classSelect");

    const hiddenClassCode = document.getElementById("hiddenClassCode");
    const hiddenAppliedClass = document.getElementById("hiddenAppliedClass");

    const dynamicExamContainer = document.getElementById("dynamic-exam-container");
    const dynamicSubjectsContainer = document.getElementById("dynamic-subjects-container");
    const subjectsBody = document.getElementById("subjectsBody");

    const tmplS1 = document.getElementById("tmpl-exam-s1");
    const tmplS5 = document.getElementById("tmpl-exam-s5");
    const tmplOther = document.getElementById("tmpl-exam-other");
    const tmplRow = document.getElementById("tmpl-subject-row");

    const step1 = document.getElementById("step-1");
    const step2 = document.getElementById("step-2");
    const step3 = document.getElementById("step-3");
    const step4 = document.getElementById("step-4");

    const btnNext1 = document.getElementById("btn-next-1");
    const btnPrev2 = document.getElementById("btn-prev-2");
    const btnNext2 = document.getElementById("btn-next-2");
    const btnPrev3 = document.getElementById("btn-prev-3");
    const btnNext3 = document.getElementById("btn-next-3");
    const btnPrev4 = document.getElementById("btn-prev-4");

    const btnSubmit = document.getElementById("btn-submit");
    const btnSubmitText = document.getElementById("btn-submit-text");
    const btnSubmitIcon = document.getElementById("btn-submit-icon");

    fetchBranches().catch(e => console.error("Initialization error:", e));

    function setupDefaultOption(selectElem, text) {
        selectElem.options.length = 0;
        const defaultOpt = document.createElement("option");
        defaultOpt.value = "";
        defaultOpt.disabled = true;
        defaultOpt.selected = true;
        defaultOpt.textContent = text;
        selectElem.appendChild(defaultOpt);
    }

    async function fetchBranches() {
        setupDefaultOption(branchSelect, "Select Campus");
        const response = await fetch("/api/public/branches");
        if (!response.ok) throw new Error("Network response was not ok");
        const result = await response.json();

        if (result.success && result.data) {
            branchList = result.data; // Cache for level filtering
            result.data.forEach(branch => {
                const option = document.createElement("option");
                option.value = branch.branchId;
                const code = branch.schoolCode ? `[${branch.schoolCode}] ` : '';
                const location = branch.branchLocation ? ` (${branch.branchLocation})` : '';
                option.textContent = `${code}${branch.branchName}${location}`;
                branchSelect.appendChild(option);
            });
        }
    }

    // ==========================================
    // BRAND NEW BRANCH LOGIC (Level Filtering)
    // ==========================================
    branchSelect.addEventListener("change", function() {
        const selectedId = parseInt(this.value);
        const branch = branchList.find(b => b.branchId === selectedId);

        setupDefaultOption(levelSelect, "Select Level");
        setupDefaultOption(classSelect, "Select Class");

        // Reset Exam logic when branch changes
        while (dynamicExamContainer.firstChild) {
            dynamicExamContainer.removeChild(dynamicExamContainer.firstChild);
        }
        dynamicSubjectsContainer.classList.add("hidden-element");

        if (branch && branch.branchType) {
            const bType = branch.branchType.toLowerCase();

            Object.keys(classData).forEach(lvl => {
                let add = false;
                // If the branch is a Primary school, allow Nursery & Primary
                if (bType.includes("primary") && (lvl === "Primary" || lvl === "Nursery")) add = true;
                // If it's a Secondary school, allow Secondary
                else if (bType.includes("secondary") && lvl === "Secondary") add = true;
                // Perfect Match fallback
                else if (bType === lvl.toLowerCase()) add = true;

                if (add) {
                    const opt = document.createElement("option");
                    opt.value = lvl;
                    opt.textContent = lvl;
                    levelSelect.appendChild(opt);
                }
            });

            // Failsafe: if the backend branchType string was totally unrecognizable, show all
            if (levelSelect.options.length <= 1) {
                Object.keys(classData).forEach(lvl => {
                    const opt = document.createElement("option");
                    opt.value = lvl;
                    opt.textContent = lvl;
                    levelSelect.appendChild(opt);
                });
            }
        } else {
            // No branchType specified, show all
            Object.keys(classData).forEach(lvl => {
                const opt = document.createElement("option");
                opt.value = lvl;
                opt.textContent = lvl;
                levelSelect.appendChild(opt);
            });
        }
    });

    levelSelect.addEventListener("change", function() {
        setupDefaultOption(classSelect, "Select Class");
        if(this.value && classData[this.value]) {
            classData[this.value].forEach(c => {
                const opt = document.createElement("option");
                opt.value = c.code;
                opt.textContent = c.name;
                classSelect.appendChild(opt);
            });
        }
    });

    classSelect.addEventListener("change", function() {
        const code = this.value;
        const name = this.options[this.selectedIndex].text;

        hiddenClassCode.value = code;
        hiddenAppliedClass.value = name;

        while (dynamicExamContainer.firstChild) {
            dynamicExamContainer.removeChild(dynamicExamContainer.firstChild);
        }
        dynamicSubjectsContainer.classList.add("hidden-element");

        if (code === "S1") {
            dynamicExamContainer.appendChild(tmplS1.content.cloneNode(true));
        } else if (code === "S5") {
            dynamicExamContainer.appendChild(tmplS5.content.cloneNode(true));
        } else if (code) {
            dynamicExamContainer.appendChild(tmplOther.content.cloneNode(true));
        }

        if (code.startsWith("S")) {
            dynamicSubjectsContainer.classList.remove("hidden-element");
            if (subjectsBody.children.length === 0) {
                addSubjectRow();
            }
        } else {
            while (subjectsBody.firstChild) {
                subjectsBody.removeChild(subjectsBody.firstChild);
            }
            document.getElementById("totalSubjectScore").textContent = "0";
        }
    });


    // ==========================================
    // PROGRESS & WIZARD NAVIGATION
    // ==========================================
    function updateProgressTracker(stepIndex) {
        document.querySelectorAll(".tracker-step").forEach(el => el.classList.remove("active"));
        document.getElementById(`indicator-${stepIndex}`).classList.add("active");
    }

    function showNextStep(stepToShow, stepToHide, stepIndex) {
        const requiredFields = stepToHide.querySelectorAll("[required]");
        let allValid = true;
        requiredFields.forEach(field => {
            if (!field.value) {
                field.reportValidity();
                allValid = false;
            }
        });

        if (allValid) {
            stepToHide.classList.remove("active-step");
            stepToHide.classList.add("hidden-element");
            stepToShow.classList.remove("hidden-element");
            stepToShow.classList.add("active-step");
            updateProgressTracker(stepIndex);
            window.scrollTo({ top: 0, behavior: 'smooth' });
        }
    }

    function showPrevStep(stepToShow, stepToHide, stepIndex) {
        stepToHide.classList.remove("active-step");
        stepToHide.classList.add("hidden-element");
        stepToShow.classList.remove("hidden-element");
        stepToShow.classList.add("active-step");
        updateProgressTracker(stepIndex);
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    btnNext1.addEventListener("click", () => showNextStep(step2, step1, 2));
    btnPrev2.addEventListener("click", () => showPrevStep(step1, step2, 1));
    btnNext2.addEventListener("click", () => showNextStep(step3, step2, 3));
    btnPrev3.addEventListener("click", () => showPrevStep(step2, step3, 2));
    btnNext3.addEventListener("click", () => showNextStep(step4, step3, 4));
    btnPrev4.addEventListener("click", () => showPrevStep(step3, step4, 3));

    // ==========================================
    // DYNAMIC TABLE LOGIC
    // ==========================================
    function addSubjectRow() {
        const rowNode = tmplRow.content.cloneNode(true);
        subjectsBody.appendChild(rowNode);
    }

    document.getElementById("addSubjectBtn").addEventListener("click", addSubjectRow);

    subjectsBody.addEventListener("click", (e) => {
        if (e.target.closest(".btn-remove")) {
            e.target.closest("tr").remove();
            calculateSubjectTotal();
        }
    });

    subjectsBody.addEventListener("input", (e) => {
        if (e.target.classList.contains("subject-mark-input")) {
            calculateSubjectTotal();
        }
    });

    function calculateSubjectTotal() {
        let total = 0;
        document.querySelectorAll(".subject-mark-input").forEach(input => {
            const val = parseFloat(input.value);
            if (!isNaN(val)) total += val;
        });
        document.getElementById("totalSubjectScore").textContent = total;
    }

    // ==========================================
    // SUBMISSION & PHOTO VALIDATION
    // ==========================================
    document.getElementById("photoInput").addEventListener("change", function() {
        const file = this.files[0];
        if (file && file.size > 50 * 1024) {
            Swal.fire({
                icon: 'warning',
                title: 'File Too Large',
                text: 'Passport photo must be less than 50KB.',
                confirmButtonColor: '#0f172a'
            });
            this.value = '';
        }
    });

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        let subjectArray = [];
        const rows = subjectsBody.querySelectorAll("tr");
        rows.forEach(row => {
            const sName = row.querySelector("input[name='subject_name[]']").value.trim();
            const sMark = row.querySelector("input[name='subject_mark[]']").value.trim();
            const sGrade = row.querySelector("input[name='subject_grade[]']").value.trim();
            if (sName) {
                subjectArray.push({ subject: sName, marks: sMark, grade: sGrade });
            }
        });

        const formData = new FormData(form);
        if (subjectArray.length > 0) {
            formData.append("subjectMarks", JSON.stringify(subjectArray));
        }

        btnSubmitText.textContent = "Submitting...";
        btnSubmitIcon.className = "btn-spinner";
        btnSubmit.disabled = true;

        try {
            const response = await fetch("/api/public/applications/submit", {
                method: "POST",
                body: formData
            });

            const result = await response.json();

            if (result.success) {
                const refNum = result['ref_number'];

                const popupContent = document.getElementById("tmpl-success-popup").content.cloneNode(true);
                popupContent.querySelector(".success-popup-ref").textContent = refNum;

                const div = document.createElement("div");
                div.appendChild(popupContent);

                Swal.fire({
                    title: 'Application Successful!',
                    html: div,
                    icon: 'success',
                    showCancelButton: true,
                    confirmButtonColor: '#0f172a',
                    cancelButtonColor: '#3b82f6',
                    confirmButtonText: 'Download Copy',
                    cancelButtonText: 'Close',
                    allowOutsideClick: false
                }).then((r) => {
                    if (r.isConfirmed) {
                        generatePDF(refNum, formData);
                    }
                    window.location.reload();
                });
            } else {
                Swal.fire('Error', result.message || "Failed to submit.", 'error');
                btnSubmitText.textContent = "Submit Application";
                btnSubmitIcon.className = "bi bi-check-circle-fill";
                btnSubmit.disabled = false;
            }
        } catch (error) {
            Swal.fire('Network Error', 'Please check your connection and try again.', 'error');
            btnSubmitText.textContent = "Submit Application";
            btnSubmitIcon.className = "bi bi-check-circle-fill";
            btnSubmit.disabled = false;
        }
    });

    function generatePDF(refNum, formData) {
        const { jsPDF } = window.jspdf;
        const doc = new jsPDF();

        doc.setFont("helvetica", "bold");
        doc.setFontSize(22);
        doc.setTextColor(15, 23, 42);
        doc.text("Montfort Brothers of St. Gabriel", 105, 20, null, null, "center");

        doc.setFontSize(14);
        doc.setTextColor(100, 116, 139);
        doc.text("Official Application Copy", 105, 30, null, null, "center");

        doc.setFontSize(16);
        doc.setTextColor(22, 163, 74);
        doc.text(`Ref Number: ${refNum}`, 105, 45, null, null, "center");

        doc.setFontSize(11);
        doc.setTextColor(0, 0, 0);
        doc.setFont("helvetica", "normal");

        let y = 60;
        doc.text(`Student Name: ${formData.get("studentName")} ${formData.get("studentSurname")}`, 20, y); y += 10;
        doc.text(`Class Applied: ${formData.get("appliedClass")}`, 20, y); y += 10;
        doc.text(`Academic Year: ${formData.get("academicYear")} - ${formData.get("term")}`, 20, y); y += 10;
        doc.text(`Guardian Name: ${formData.get("guardianName")} (${formData.get("guardianContact")})`, 20, y); y += 10;

        doc.setLineWidth(0.5);
        doc.line(20, y + 5, 190, y + 5);
        y += 15;

        doc.setFontSize(10);
        doc.setTextColor(100, 100, 100);
        doc.text("Please carry this document to the school administration block.", 105, y, null, null, "center");

        doc.save(`Montfort_Application_${refNum}.pdf`);
    }
});
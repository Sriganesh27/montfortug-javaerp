# Total `add-employee.html` Redesign

This artifact contains two parts:
1. The **Full HTML** for `add-employee.html` (zero inline CSS, zero JS attributes).
2. The **Scoped CSS** for `admin.css` to make it look like a highly polished, modern ERP form.

---

## 1. HTML (`add-employee.html`)
Replace all the contents of `src/main/resources/static/views/admin/add-employee.html` with this code. I have grouped the fields into logical "Cards" for a much better user experience.

```html
<div id="ba-add-employee-view" class="container-fluid py-4">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2 class="h3 mb-0 page-main-title">Add New Employee</h2>
        <button id="backToEmployeesBtn" class="btn btn-secondary back-btn">
            <i class="bi bi-arrow-left me-2"></i>Back to List
        </button>
    </div>

    <form id="add-emp-form">
        
        <!-- Card 1: Personal Information -->
        <div class="card mb-4 shadow-sm border-0 section-card">
            <div class="card-header bg-transparent border-bottom-0 pt-4 pb-0 px-4">
                <h5 class="text-primary section-title"><i class="bi bi-person-fill me-2"></i>Personal Information</h5>
                <hr class="title-divider">
            </div>
            <div class="card-body px-4 pb-4">
                <div class="row g-3">
                    <div class="col-md-2">
                        <label class="form-label">Title</label>
                        <select id="add-empTitle" class="form-control detail-input">
                            <option value="">Select...</option>
                            <option value="MR">Mr.</option>
                            <option value="MS">Ms.</option>
                            <option value="MRS">Mrs.</option>
                            <option value="DR">Dr.</option>
                        </select>
                    </div>
                    <div class="col-md-3">
                        <label class="form-label">First Name *</label>
                        <input type="text" id="add-empFirstName" class="form-control detail-input" required>
                    </div>
                    <div class="col-md-3">
                        <label class="form-label">Middle Name</label>
                        <input type="text" id="add-empMiddleName" class="form-control detail-input">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label">Last Name *</label>
                        <input type="text" id="add-empLastName" class="form-control detail-input" required>
                    </div>

                    <div class="col-md-3">
                        <label class="form-label">Gender *</label>
                        <select id="add-empGender" class="form-control detail-input" required>
                            <option value="">Select...</option>
                            <option value="MALE">Male</option>
                            <option value="FEMALE">Female</option>
                            <option value="OTHER">Other</option>
                        </select>
                    </div>
                    <div class="col-md-3">
                        <label class="form-label">Date of Birth *</label>
                        <input type="text" id="add-empDob" class="form-control detail-input" placeholder="YYYY-MM-DD" required>
                    </div>
                    <div class="col-md-3">
                        <label class="form-label">Marital Status</label>
                        <select id="add-empMaritalStatus" class="form-control detail-input">
                            <option value="">Select...</option>
                            <option value="SINGLE">Single</option>
                            <option value="MARRIED">Married</option>
                            <option value="DIVORCED">Divorced</option>
                            <option value="WIDOWED">Widowed</option>
                        </select>
                    </div>
                    <div class="col-md-3">
                        <label class="form-label">Blood Group</label>
                        <select id="add-empBloodGroup" class="form-control detail-input">
                            <option value="">Select...</option>
                            <option value="A_POSITIVE">A+</option>
                            <option value="A_NEGATIVE">A-</option>
                            <option value="B_POSITIVE">B+</option>
                            <option value="B_NEGATIVE">B-</option>
                            <option value="O_POSITIVE">O+</option>
                            <option value="O_NEGATIVE">O-</option>
                            <option value="AB_POSITIVE">AB+</option>
                            <option value="AB_NEGATIVE">AB-</option>
                        </select>
                    </div>
                </div>
            </div>
        </div>

        <!-- Card 2: Contact Details -->
        <div class="card mb-4 shadow-sm border-0 section-card">
            <div class="card-header bg-transparent border-bottom-0 pt-4 pb-0 px-4">
                <h5 class="text-primary section-title"><i class="bi bi-telephone-fill me-2"></i>Contact Details</h5>
                <hr class="title-divider">
            </div>
            <div class="card-body px-4 pb-4">
                <div class="row g-3">
                    <div class="col-md-3">
                        <label class="form-label">Official Email</label>
                        <input type="email" id="add-empEmail" class="form-control detail-input">
                    </div>
                    <div class="col-md-3">
                        <label class="form-label">Personal Email</label>
                        <input type="email" id="add-empPersonalEmail" class="form-control detail-input">
                    </div>
                    <div class="col-md-3">
                        <label class="form-label">Mobile No *</label>
                        <input type="text" id="add-empPhone" class="form-control detail-input" required>
                    </div>
                    <div class="col-md-3">
                        <label class="form-label">Alternate Phone</label>
                        <input type="text" id="add-empAlternatePhone" class="form-control detail-input">
                    </div>
                </div>
            </div>
        </div>

        <!-- Card 3: Job Details -->
        <div class="card mb-4 shadow-sm border-0 section-card">
            <div class="card-header bg-transparent border-bottom-0 pt-4 pb-0 px-4">
                <h5 class="text-primary section-title"><i class="bi bi-briefcase-fill me-2"></i>Job Details</h5>
                <hr class="title-divider">
            </div>
            <div class="card-body px-4 pb-4">
                <div class="row g-3">
                    <div class="col-md-3">
                        <label class="form-label">Category *</label>
                        <select id="add-empCategory" class="form-control detail-input" required>
                            <option value="">Select...</option>
                            <option value="TEACHING">Teaching</option>
                            <option value="NON_TEACHING">Non-Teaching</option>
                        </select>
                    </div>
                    <div class="col-md-3">
                        <label class="form-label">Type</label>
                        <select id="add-empType" class="form-control detail-input">
                            <option value="">Select...</option>
                            <option value="PERMANENT">Permanent</option>
                            <option value="PROBATION">Probation</option>
                            <option value="CONTRACT">Contract</option>
                            <option value="INTERN">Intern</option>
                        </select>
                    </div>
                    <div class="col-md-3">
                        <label class="form-label">Employment Mode</label>
                        <select id="add-empMode" class="form-control detail-input">
                            <option value="">Select...</option>
                            <option value="FULL_TIME">Full Time</option>
                            <option value="PART_TIME">Part Time</option>
                            <option value="VISITING">Visiting</option>
                        </select>
                    </div>
                    <div class="col-md-3">
                        <label class="form-label">Joining Date</label>
                        <input type="text" id="add-empJoiningDate" class="form-control detail-input" placeholder="YYYY-MM-DD">
                    </div>
                    
                    <div class="col-md-4">
                        <label class="form-label">Department</label>
                        <select id="add-empDepartment" class="form-control detail-input"></select>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label">Designation</label>
                        <select id="add-empDesignation" class="form-control detail-input"></select>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label">Probation End Date</label>
                        <input type="text" id="add-empProbationEndDate" class="form-control detail-input" placeholder="YYYY-MM-DD">
                    </div>
                </div>
            </div>
        </div>

        <!-- Card 4: Address -->
        <div class="card mb-4 shadow-sm border-0 section-card">
            <div class="card-header bg-transparent border-bottom-0 pt-4 pb-0 px-4">
                <h5 class="text-primary section-title"><i class="bi bi-geo-alt-fill me-2"></i>Address Details</h5>
                <hr class="title-divider">
            </div>
            <div class="card-body px-4 pb-4">
                <div class="row g-3">
                    <div class="col-md-4">
                        <label class="form-label">Country</label>
                        <input type="text" id="add-empCountry" class="form-control detail-input">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label">State / Region</label>
                        <input type="text" id="add-empState" class="form-control detail-input">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label">District</label>
                        <input type="text" id="add-empDistrict" class="form-control detail-input">
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">Village / Locality</label>
                        <input type="text" id="add-empVillage" class="form-control detail-input">
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">Street Address</label>
                        <input type="text" id="add-empStreet" class="form-control detail-input">
                    </div>
                </div>
            </div>
        </div>

        <!-- Card 5: Legal & Identification -->
        <div class="card mb-4 shadow-sm border-0 section-card">
            <div class="card-header bg-transparent border-bottom-0 pt-4 pb-0 px-4">
                <h5 class="text-primary section-title"><i class="bi bi-shield-check me-2"></i>Legal & Identification</h5>
                <hr class="title-divider">
            </div>
            <div class="card-body px-4 pb-4">
                <div class="row g-3">
                    <div class="col-md-3">
                        <label class="form-label">Nationality</label>
                        <input type="text" id="add-empNationality" class="form-control detail-input">
                    </div>
                    <div class="col-md-3">
                        <label class="form-label">National ID (NIN)</label>
                        <input type="text" id="add-empNationalId" class="form-control detail-input">
                    </div>
                    <div class="col-md-3">
                        <label class="form-label">TIN Number</label>
                        <input type="text" id="add-empTin" class="form-control detail-input">
                    </div>
                    <div class="col-md-3">
                        <label class="form-label">Passport No</label>
                        <input type="text" id="add-empPassportNo" class="form-control detail-input">
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">Passport Expiry</label>
                        <input type="text" id="add-empPassportExpiry" class="form-control detail-input" placeholder="YYYY-MM-DD">
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">Work Permit No</label>
                        <input type="text" id="add-empWorkPermit" class="form-control detail-input">
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">Work Permit Expiry</label>
                        <input type="text" id="add-empWorkPermitExpiry" class="form-control detail-input" placeholder="YYYY-MM-DD">
                    </div>
                </div>
            </div>
        </div>

        <!-- Card 6: Dynamic Sections (Contacts, Qualifications, Experiences, Documents) -->
        
        <div class="card mb-4 shadow-sm border-0 section-card" id="contacts-section">
            <div class="card-header bg-transparent border-bottom-0 pt-4 pb-0 px-4">
                <h5 class="text-primary section-title"><i class="bi bi-person-lines-fill me-2"></i>Emergency Contacts</h5>
                <hr class="title-divider">
            </div>
            <div class="card-body px-4 pb-4">
                <div id="contacts-container"></div>
            </div>
        </div>

        <div class="card mb-4 shadow-sm border-0 section-card" id="qualifications-section">
            <div class="card-header bg-transparent border-bottom-0 pt-4 pb-0 px-4">
                <h5 class="text-primary section-title"><i class="bi bi-mortarboard-fill me-2"></i>Educational Qualifications</h5>
                <hr class="title-divider">
            </div>
            <div class="card-body px-4 pb-4">
                <div id="qualifications-container"></div>
            </div>
        </div>

        <div class="card mb-4 shadow-sm border-0 section-card" id="experiences-section">
            <div class="card-header bg-transparent border-bottom-0 pt-4 pb-0 px-4">
                <h5 class="text-primary section-title"><i class="bi bi-file-earmark-bar-graph me-2"></i>Work Experience</h5>
                <hr class="title-divider">
            </div>
            <div class="card-body px-4 pb-4">
                <div id="experiences-container"></div>
            </div>
        </div>

        <div class="card mb-4 shadow-sm border-0 section-card" id="documents-section">
            <div class="card-header bg-transparent border-bottom-0 pt-4 pb-0 px-4">
                <h5 class="text-primary section-title"><i class="bi bi-folder-fill me-2"></i>Other Documents</h5>
                <hr class="title-divider">
            </div>
            <div class="card-body px-4 pb-4">
                <div id="documents-container"></div>
            </div>
        </div>

        <!-- Submit Button -->
        <div class="d-flex justify-content-end mb-5">
            <button type="submit" class="btn btn-primary btn-lg submit-btn">
                <i class="bi bi-check2-circle me-2"></i>Save Employee Record
            </button>
        </div>
        
    </form>
</div>
```

---

## 2. CSS (`admin.css`)
Add this entire block to the bottom of `src/main/resources/static/css/admin.css`. It scopes everything properly to `#ba-add-employee-view` and avoids all inline CSS.

```css
/* =========================================================
   Add Employee UI Design System
   ========================================================= */

/* Main View & Layout adjustments */
#ba-add-employee-view {
    background-color: #f4f6f9; /* Soft background */
}
#ba-add-employee-view .page-main-title {
    color: #1e293b;
    font-weight: 700;
}
#ba-add-employee-view .back-btn {
    background-color: #e2e8f0;
    color: #475569;
    border: none;
    font-weight: 500;
}
#ba-add-employee-view .back-btn:hover {
    background-color: #cbd5e1;
}

/* Card Wrappers */
#ba-add-employee-view .section-card {
    background-color: #ffffff;
    border-radius: 10px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03) !important;
}

/* Titles */
#ba-add-employee-view .section-title {
    font-weight: 600;
    font-size: 1.1rem;
    color: #1e3a8a; /* Deep blue professional tone */
}

/* Dividers */
#ba-add-employee-view .title-divider {
    opacity: 0.1;
    margin-top: 10px;
    margin-bottom: 0;
    border-top: 2px solid #1e3a8a;
}

/* Standard Labels */
#ba-add-employee-view .form-label {
    font-weight: 600;
    color: #4b5563;
    font-size: 0.8rem;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    margin-bottom: 6px;
}

/* Inputs and Selects */
#ba-add-employee-view .detail-input {
    background-color: #f8fafc;
    border: 1px solid #cbd5e1;
    border-radius: 6px;
    padding: 0.6rem 0.8rem;
    font-size: 0.95rem;
    color: #1e293b;
    transition: all 0.2s;
    height: auto;
}
#ba-add-employee-view .detail-input:focus {
    background-color: #ffffff;
    border-color: #3b82f6;
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
    outline: none;
}
#ba-add-employee-view select.detail-input {
    appearance: none;
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 16 16'%3E%3Cpath fill='none' stroke='%234b5563' stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M2 5l6 6 6-6'/%3E%3C/svg%3E");
    background-repeat: no-repeat;
    background-position: right 0.75rem center;
    background-size: 16px 12px;
}

/* Submit Button */
#ba-add-employee-view .submit-btn {
    padding: 12px 30px;
    font-weight: 600;
    border-radius: 8px;
    background-color: #2563eb;
    border-color: #2563eb;
    box-shadow: 0 4px 6px rgba(37, 99, 235, 0.2);
    transition: all 0.2s;
}
#ba-add-employee-view .submit-btn:hover {
    background-color: #1d4ed8;
    border-color: #1d4ed8;
    transform: translateY(-1px);
}

/* =========================================================
   Dynamic Sections Styling (Contacts, Quals, etc)
   ========================================================= */

/* The Add "+ New" Buttons */
#ba-add-employee-view .add-row-btn {
    background-color: #f4f7f6;
    color: #0d6efd;
    border: 2px dashed #a5c5f6;
    padding: 8px 16px;
    border-radius: 6px;
    font-weight: 600;
    font-size: 0.9rem;
    transition: all 0.2s ease;
    display: block;
    width: fit-content;
    cursor: pointer;
    margin-top: 10px;
}
#ba-add-employee-view .add-row-btn:hover {
    background-color: #e6f0ff;
    border-color: #0d6efd;
}

/* The Row Container (Soft Background Card) */
#ba-add-employee-view .emp-child-row {
    background-color: #f8f9fa;
    border: 1px solid #e9ecef;
    padding: 12px 15px;
    border-radius: 8px;
    display: grid;
    gap: 15px;
    align-items: center;
    margin-bottom: 12px;
}

/* Grid Layouts based on columns + 1 for delete button */
#ba-add-employee-view .emp-grid-4-cols {
    grid-template-columns: repeat(4, 1fr) 35px;
}
#ba-add-employee-view .emp-grid-5-cols {
    grid-template-columns: repeat(5, 1fr) 35px;
}

/* File Upload Specific Styling */
#ba-add-employee-view .emp-child-row input[type="file"].detail-input {
    padding: 0.35rem;
    background-color: #ffffff;
}
#ba-add-employee-view .emp-child-row input[type="file"].detail-input::file-selector-button {
    background-color: #e9ecef;
    color: #495057;
    border: 1px solid #ced4da;
    border-radius: 4px;
    padding: 4px 12px;
    margin-right: 10px;
    cursor: pointer;
    font-weight: 500;
    transition: background-color 0.2s;
}
#ba-add-employee-view .emp-child-row input[type="file"].detail-input::file-selector-button:hover {
    background-color: #dde0e3;
}

/* Delete (X) Button */
#ba-add-employee-view .emp-child-row .text-danger.btn-sm {
    background-color: #ffe6e6;
    color: #dc3545;
    border: none;
    border-radius: 6px;
    height: 35px;
    width: 35px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
    cursor: pointer;
    transition: all 0.2s;
    padding: 0;
}
#ba-add-employee-view .emp-child-row .text-danger.btn-sm:hover {
    background-color: #dc3545;
    color: white;
}
```

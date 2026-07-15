# Add Employee Fieldset Design

Based on your exact HTML layout, here is the updated version using `<fieldset>` and `<legend>` for a classic but perfectly polished form structure. I've also removed all the inline `style="..."` attributes (like `grid-column: span 2`) and replaced them with CSS classes.

---

## 1. HTML (`add-employee.html`)
Replace your form with this HTML:

```html
<div id="ba-add-employee-view">
    <form id="add-emp-form">
        <div id="emp-detailView">
            <div class="view-header mb-4 d-flex justify-content-between align-items-center">
                <h2>Add New Employee</h2>
                <div class="action-btn-group">
                    <button type="button" id="backToEmployeesBtn" class="btn btn-secondary me-2"><i class="bi bi-arrow-left"></i> Back</button>
                    <button type="submit" class="btn btn-primary"><i class="bi bi-check-lg"></i> Register Employee</button>
                </div>
            </div>
            
            <div class="form-container p-4 bg-white shadow-sm rounded">
                <!-- SECTION 1: Personal Details -->
                <fieldset class="emp-fieldset">
                    <legend class="emp-legend"><i class="bi bi-person me-2"></i>Personal Details</legend>
                    <div class="emp-grid emp-grid-4 mb-3">
                        <div class="form-group">
                            <label class="text-muted small">Title</label>
                            <select id="add-empTitle" class="detail-input w-100">
                                <option value="">--</option>
                                <option value="Mr.">Mr.</option>
                                <option value="Ms.">Ms.</option>
                                <option value="Mrs.">Mrs.</option>
                                <option value="Dr.">Dr.</option>
                            </select>
                        </div>
                        <div class="form-group span-2-cols">
                            <label class="text-muted small">Full Name</label>
                            <div id="add-empNameContainer" class="emp-name-container d-flex gap-2">
                                <input type="text" id="add-empFirstName" class="detail-input flex-fill" placeholder="First">
                                <input type="text" id="add-empMiddleName" class="detail-input flex-fill" placeholder="Middle">
                                <input type="text" id="add-empLastName" class="detail-input flex-fill" placeholder="Last">
                            </div>
                        </div>
                    </div>

                    <div class="emp-grid emp-grid-4 mb-2">
                        <div class="form-group">
                            <label class="text-muted small">Gender</label>
                            <select id="add-empGender" class="detail-input w-100">
                                <option value="">-- Select --</option>
                                <option value="MALE">Male</option>
                                <option value="FEMALE">Female</option>
                                <option value="OTHER">Other</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label class="text-muted small">Date of Birth</label>
                            <input type="date" id="add-empDob" class="detail-input w-100">
                        </div>
                        <div class="form-group">
                            <label class="text-muted small">Marital Status</label>
                            <select id="add-empMaritalStatus" class="detail-input w-100">
                                <option value="">-- Select --</option>
                                <option value="SINGLE">Single</option>
                                <option value="MARRIED">Married</option>
                                <option value="DIVORCED">Divorced</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label class="text-muted small">Blood Group</label>
                            <input type="text" id="add-empBloodGroup" class="detail-input w-100">
                        </div>
                    </div>
                </fieldset>

                <!-- SECTION 2: Employment Details -->
                <fieldset class="emp-fieldset">
                    <legend class="emp-legend"><i class="bi bi-briefcase me-2"></i>Employment Details</legend>
                    <div class="emp-grid emp-grid-2 mb-3">
                        <div class="form-group">
                            <label class="text-muted small">Department</label>
                            <select id="add-empDepartment" class="detail-input w-100">
                                <option value="">-- None --</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label class="text-muted small">Designation</label>
                            <select id="add-empDesignation" class="detail-input w-100">
                                <option value="">-- None --</option>
                            </select>
                        </div>
                    </div>

                    <div class="emp-grid emp-grid-3 mb-3">
                        <div class="form-group">
                            <label class="text-muted small">Category</label>
                            <select id="add-empCategory" class="detail-input w-100">
                                <option value="TEACHING">Teaching</option>
                                <option value="NON_TEACHING">Non-Teaching</option>
                                <option value="MANAGEMENT">Management</option>
                                <option value="SUPPORT_STAFF">Support Staff</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label class="text-muted small">Type</label>
                            <select id="add-empType" class="detail-input w-100">
                                <option value="PERMANENT">Permanent</option>
                                <option value="CONTRACT">Contract</option>
                                <option value="TEMPORARY">Temporary</option>
                                <option value="PART_TIME">Part Time</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label class="text-muted small">Mode</label>
                            <select id="add-empMode" class="detail-input w-100">
                                <option value="FULL_TIME">Full Time</option>
                                <option value="PART_TIME">Part Time</option>
                                <option value="REMOTE">Remote</option>
                            </select>
                        </div>
                    </div>

                    <div class="emp-grid emp-grid-3 mb-2">
                        <div class="form-group">
                            <label class="text-muted small">Joining Date</label>
                            <input type="date" id="add-empJoiningDate" class="detail-input w-100">
                        </div>
                        <div class="form-group">
                            <label class="text-muted small">Probation End Date</label>
                            <input type="date" id="add-empProbationEndDate" class="detail-input w-100">
                        </div>
                        <div class="form-group">
                            <label class="text-muted small">Confirmation Date</label>
                            <input type="date" id="add-empConfirmationDate" class="detail-input w-100">
                        </div>
                    </div>
                </fieldset>

                <!-- SECTION 3: Identifications & Permits -->
                <fieldset class="emp-fieldset">
                    <legend class="emp-legend"><i class="bi bi-shield-check me-2"></i>Identifications</legend>
                    <div class="emp-grid emp-grid-3 mb-3">
                        <div class="form-group">
                            <label class="text-muted small">Nationality</label>
                            <input type="text" id="add-empNationality" class="detail-input w-100">
                        </div>
                        <div class="form-group">
                            <label class="text-muted small">National ID</label>
                            <input type="text" id="add-empNationalId" class="detail-input w-100">
                        </div>
                        <div class="form-group">
                            <label class="text-muted small">TIN Number</label>
                            <input type="text" id="add-empTin" class="detail-input w-100">
                        </div>
                    </div>
                    <div class="emp-grid emp-grid-2 mb-2">
                        <div class="form-group">
                            <label class="text-muted small">Passport No.</label>
                            <input type="text" id="add-empPassportNo" class="detail-input w-100">
                        </div>
                        <div class="form-group">
                            <label class="text-muted small">Work Permit No.</label>
                            <input type="text" id="add-empWorkPermit" class="detail-input w-100">
                        </div>
                    </div>
                </fieldset>

                <!-- SECTION 4: Contact & Address -->
                <fieldset class="emp-fieldset">
                    <legend class="emp-legend"><i class="bi bi-geo-alt me-2"></i>Contact & Address</legend>
                    <div class="emp-grid emp-grid-2 mb-3">
                        <div class="form-group">
                            <label class="text-muted small">Official Email</label>
                            <input type="email" id="add-empEmail" class="detail-input w-100">
                        </div>
                        <div class="form-group">
                            <label class="text-muted small">Personal Email</label>
                            <input type="email" id="add-empPersonalEmail" class="detail-input w-100">
                        </div>
                        <div class="form-group">
                            <label class="text-muted small">Mobile No</label>
                            <input type="text" id="add-empPhone" class="detail-input w-100">
                        </div>
                        <div class="form-group">
                            <label class="text-muted small">Alternate Phone</label>
                            <input type="text" id="add-empAlternatePhone" class="detail-input w-100">
                        </div>
                    </div>

                    <div class="emp-grid emp-grid-3 mb-3">
                        <div class="form-group">
                            <label class="text-muted small">Country</label>
                            <input type="text" id="add-empCountry" class="detail-input w-100">
                        </div>
                        <div class="form-group">
                            <label class="text-muted small">State/Region</label>
                            <input type="text" id="add-empState" class="detail-input w-100">
                        </div>
                        <div class="form-group">
                            <label class="text-muted small">District</label>
                            <input type="text" id="add-empDistrict" class="detail-input w-100">
                        </div>
                        <div class="form-group">
                            <label class="text-muted small">Village</label>
                            <input type="text" id="add-empVillage" class="detail-input w-100">
                        </div>
                        <div class="form-group span-2-cols">
                            <label class="text-muted small">Street Address</label>
                            <input type="text" id="add-empStreet" class="detail-input w-100">
                        </div>
                    </div>
                </fieldset>

                <!-- DYNAMIC SECTIONS (Now also fieldsets!) -->
                <fieldset class="emp-fieldset" id="contacts-section">
                    <legend class="emp-legend"><i class="bi bi-person-lines-fill me-2"></i>Emergency Contacts</legend>
                    <div id="contacts-container"></div>
                </fieldset>

                <fieldset class="emp-fieldset" id="qualifications-section">
                    <legend class="emp-legend"><i class="bi bi-mortarboard me-2"></i>Qualifications</legend>
                    <div id="qualifications-container"></div>
                </fieldset>

                <fieldset class="emp-fieldset" id="experiences-section">
                    <legend class="emp-legend"><i class="bi bi-journal-text me-2"></i>Experiences</legend>
                    <div id="experiences-container"></div>
                </fieldset>

                <fieldset class="emp-fieldset" id="documents-section">
                    <legend class="emp-legend"><i class="bi bi-file-earmark-check me-2"></i>Other Documents</legend>
                    <div id="documents-container"></div>
                </fieldset>
            </div>
        </div>
    </form>
</div>
```

---

## 2. CSS (`admin.css`)
Add this entire block to the bottom of `src/main/resources/static/css/admin.css` to beautifully style the fieldsets, legends, and dynamic grids.

```css
/* =========================================================
   Add Employee FIELDSET UI Design System
   ========================================================= */

#ba-add-employee-view .span-2-cols {
    grid-column: span 2;
}

/* Fieldset Styling */
#ba-add-employee-view .emp-fieldset {
    border: 1px solid #ced4da;
    border-radius: 8px;
    padding: 1.5rem 1.5rem 1rem 1.5rem;
    margin-bottom: 2rem;
    background-color: #fafbfc;
    box-shadow: 0 1px 3px rgba(0,0,0,0.02);
}

/* Legend Styling */
#ba-add-employee-view .emp-legend {
    width: auto;
    padding: 0.3rem 1rem;
    margin-bottom: 0;
    font-size: 1rem;
    font-weight: 600;
    color: #0d6efd;
    background-color: #ffffff;
    border: 1px solid #ced4da;
    border-radius: 6px;
    box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

/* Dynamic JS Rows and Buttons Inside Fieldsets */
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

#ba-add-employee-view .emp-child-row {
    background-color: #ffffff;
    border: 1px solid #e9ecef;
    padding: 12px 15px;
    border-radius: 8px;
    display: grid;
    gap: 15px;
    align-items: center;
    margin-bottom: 12px;
}

#ba-add-employee-view .emp-grid-4-cols {
    grid-template-columns: repeat(4, 1fr) 35px;
}
#ba-add-employee-view .emp-grid-5-cols {
    grid-template-columns: repeat(5, 1fr) 35px;
}

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

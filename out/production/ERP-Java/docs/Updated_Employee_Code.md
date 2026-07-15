# Employee Management - Complete Updated Code

Here is the complete source code for all the files we have modified. Since I do not have write access, you can copy and paste the contents into your respective files to completely overwrite the old versions.

---

## 1. Enums

### `src/main/java/com/erp/montfortuganda/employee/enums/EmployeeCategory.java`
```java
package com.erp.montfortuganda.employee.enums;

public enum EmployeeCategory { 
    TEACHING, 
    NON_TEACHING, 
    MANAGEMENT_TEACHING, 
    MANAGEMENT_NON_TEACHING, 
    SUPPORT_STAFF 
}
```

### `src/main/java/com/erp/montfortuganda/employee/enums/QualificationLevel.java`
```java
package com.erp.montfortuganda.employee.enums;

public enum QualificationLevel {
    PRIMARY, 
    SECONDARY, 
    SENIOR_SECONDARY, 
    DIPLOMA, 
    CERTIFICATE, 
    GRADUATION, 
    POST_GRADUATION, 
    DR_PHD, 
    OTHER
}
```

### `src/main/java/com/erp/montfortuganda/employee/enums/ExperienceEmploymentType.java`
```java
package com.erp.montfortuganda.employee.enums;

public enum ExperienceEmploymentType {
    FULL_TIME, 
    PART_TIME, 
    CONTRACT, 
    TEMPORARY, 
    INTERNSHIP, 
    CONSULTANT, 
    VOLUNTEER, 
    SELF_EMPLOYED,
    OTHER
}
```

---

## 2. Entities

### `src/main/java/com/erp/montfortuganda/employee/entity/ErpEmployee.java`
```java
package com.erp.montfortuganda.employee.entity;

import com.erp.montfortuganda.auth.User;
import com.erp.montfortuganda.employee.enums.*;
import com.erp.montfortuganda.model.AuditableEntity;
import com.erp.montfortuganda.school.Branch;
import com.erp.montfortuganda.school.entity.Department;
import com.erp.montfortuganda.school.entity.Designation;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "erp_employees", indexes = {
        @Index(name = "idx_emp_branch", columnList = "branch_id"),
        @Index(name = "idx_emp_department", columnList = "department_id"),
        @Index(name = "idx_emp_designation", columnList = "designation_id"),
        @Index(name = "idx_emp_status", columnList = "employment_status"),
        @Index(name = "idx_emp_category", columnList = "employee_category")
})
public class ErpEmployee extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long employeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designation_id")
    private Designation designation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporting_manager_id")
    private ErpEmployee reportingManager;

    @Column(name = "employee_no", nullable = false, unique = true, length = 50)
    private String employeeNo;

    @Column(length = 20)
    private String title;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "profile_photo", length = 500)
    private String profilePhoto;

    @Column(name = "signature_file", length = 500)
    private String signatureFile;

    @Column(length = 100)
    private String nationality;

    @Column(name = "national_id", length = 100)
    private String nationalId;

    @Column(name = "passport_no", length = 100)
    private String passportNo;

    @Column(name = "tin_number", length = 100)
    private String tinNumber;

    @Column(name = "marital_status", length = 50)
    private String maritalStatus;

    @Column(name = "blood_group", length = 20)
    private String bloodGroup;

    @Column(length = 100)
    private String religion;

    @Column(name = "sub_religion", length = 100)
    private String subReligion;

    @Column(name = "official_email", length = 150)
    private String officialEmail;

    @Column(name = "personal_email", length = 150)
    private String personalEmail;

    @Column(name = "mobile_no", length = 30)
    private String mobileNo;

    @Column(name = "alternate_mobile", length = 30)
    private String alternateMobile;

    @Column(name = "address_country", length = 100)
    private String addressCountry;

    @Column(name = "address_state", length = 100)
    private String addressState;

    @Column(name = "address_district", length = 100)
    private String addressDistrict;

    @Column(name = "address_county", length = 100)
    private String addressCounty;

    @Column(name = "address_sub_county", length = 100)
    private String addressSubCounty;

    @Column(name = "address_parish", length = 100)
    private String addressParish;

    @Column(name = "address_village", length = 150)
    private String addressVillage;

    @Column(name = "address_street", length = 255)
    private String addressStreet;

    @Column(name = "postal_code", length = 30)
    private String postalCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_category", length = 30)
    private EmployeeCategory employeeCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_type", nullable = false, length = 30)
    private EmployeeType employeeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_mode", nullable = false, length = 30)
    private EmploymentMode employmentMode = EmploymentMode.FULL_TIME;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", nullable = false, length = 30)
    private EmploymentStatus employmentStatus = EmploymentStatus.ACTIVE;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Column(name = "probation_end_date")
    private LocalDate probationEndDate;

    @Column(name = "confirmation_date")
    private LocalDate confirmationDate;

    @Column(name = "retirement_date")
    private LocalDate retirementDate;

    @Column(name = "resignation_date")
    private LocalDate resignationDate;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Column(name = "work_permit_number", length = 100)
    private String workPermitNumber;

    @Column(name = "work_permit_expiry_date")
    private LocalDate workPermitExpiryDate;

    @Column(name = "passport_expiry_date")
    private LocalDate passportExpiryDate;

    @Column(name = "employment_end_date")
    private LocalDate employmentEndDate;

    @Column(name = "exit_reason", columnDefinition = "TEXT")
    private String exitReason;

    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills;

    @Column(name = "languages_spoken", columnDefinition = "TEXT")
    private String languagesSpoken;

    @Column(name = "employee_remarks", columnDefinition = "TEXT")
    private String employeeRemarks;

    @Column(name = "login_enabled", nullable = false)
    private Boolean loginEnabled = false;

    @Column(nullable = false)
    private Boolean active = true;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @PrePersist
    @PreUpdate
    private void generateFullName() {
        this.fullName = (firstName != null ? firstName : "") +
                (middleName != null && !middleName.isBlank() ? " " + middleName : "") +
                (lastName != null && !lastName.isBlank() ? " " + lastName : "");
        this.fullName = this.fullName.trim();
    }
}
```

### `src/main/java/com/erp/montfortuganda/employee/entity/ErpEmployeeQualification.java`
*(Add the new properties right before the `@Version` field)*
```java
    @Column(name = "qualification_grade", length = 100)
    private String qualificationGrade;

    @Column(name = "custom_level", length = 255)
    private String customLevel;
```

---

## 3. DTOs

### `src/main/java/com/erp/montfortuganda/employee/dto/EmployeeCreateRequest.java`
*(Add these anywhere inside your `EmployeeCreateRequest` class, keeping existing fields)*
```java
    @Size(max = 100)
    private String addressCounty;

    @Size(max = 100)
    private String addressSubCounty;

    @Size(max = 100)
    private String addressParish;
    
    private String skills;
    private String languagesSpoken;
    private String subReligion;
```
*(And inside your static `EmployeeQualificationRequest` inner class)*
```java
    private String qualificationGrade;
    private String customLevel;
```

*(You must also add these exact same fields to `EmployeeUpdateRequest.java` and `EmployeeResponse.java`)*

---

## 4. Frontend - HTML

### `add-employee.html`

**A. Category Dropdown:**
```html
<div class="form-group">
    <label class="text-muted small">Category</label>
    <select id="add-empCategory" class="detail-input w-100">
        <option value="TEACHING">Teaching</option>
        <option value="NON_TEACHING">Non-Teaching</option>
        <option value="MANAGEMENT_TEACHING">Management (Teaching)</option>
        <option value="MANAGEMENT_NON_TEACHING">Management (Non-Teaching)</option>
        <option value="SUPPORT_STAFF">Support Staff</option>
    </select>
</div>
```

**B. Religion & Sub Religion (Insert inside Personal Details Fieldset):**
```html
<div class="form-group">
    <label class="text-muted small">Religion</label>
    <select id="add-empReligion" class="detail-input w-100">
        <option value="">Select</option>
        <option value="CHRISTIANITY">Christianity</option>
        <option value="ISLAM">Islam</option>
        <option value="HINDUISM">Hinduism</option>
        <option value="OTHER">Other</option>
    </select>
</div>
<div class="form-group">
    <label class="text-muted small">Sub-Religion (Denomination)</label>
    <input type="text" id="add-empSubReligion" class="detail-input w-100" placeholder="e.g. Catholic, Sunni">
</div>
```

**C. Contact & Address Fieldset (Replacing old 3-column one):**
```html
<fieldset class="emp-fieldset">
    <legend class="emp-legend">Contact & Address</legend>
    <div class="emp-grid emp-grid-2 mb-3">
        <div class="form-group">
            <label class="text-muted small">Official Email</label>
            <input type="email" id="add-empEmail" class="detail-input w-100">
        </div>
        <div class="form-group">
            <label class="text-muted small">Mobile No.</label>
            <input type="text" id="add-empMobile" class="detail-input w-100">
        </div>
    </div>
    
    <div class="emp-grid emp-grid-5 mb-3">
        <div class="form-group">
            <label class="text-muted small">District</label>
            <input type="text" id="add-empDistrict" class="detail-input w-100">
        </div>
        <div class="form-group">
            <label class="text-muted small">County</label>
            <input type="text" id="add-empCounty" class="detail-input w-100">
        </div>
        <div class="form-group">
            <label class="text-muted small">Sub-County (LC-III)</label>
            <input type="text" id="add-empSubCounty" class="detail-input w-100">
        </div>
        <div class="form-group">
            <label class="text-muted small">Parish (LC-II)</label>
            <input type="text" id="add-empParish" class="detail-input w-100">
        </div>
        <div class="form-group">
            <label class="text-muted small">Village (LC-I)</label>
            <input type="text" id="add-empVillage" class="detail-input w-100">
        </div>
    </div>
</fieldset>
```

**D. Professional Profile (Insert before Contacts Fieldset):**
```html
<fieldset class="emp-fieldset">
    <legend class="emp-legend">Professional Profile</legend>
    <div class="emp-grid emp-grid-2 mb-3">
        <div class="form-group">
            <label class="text-muted small">Skills</label>
            <input type="text" id="add-empSkills" class="detail-input w-100" placeholder="e.g. Project Management, Accounting">
        </div>
        <div class="form-group">
            <label class="text-muted small">Languages Spoken</label>
            <input type="text" id="add-empLanguages" class="detail-input w-100" placeholder="e.g. English, Luganda, Swahili">
        </div>
    </div>
</fieldset>
```

---

## 5. Frontend - JS (`employees.js`)

**A. `createRow` Select Support (Inside `const EmpCollections`):**
```javascript
createRow: function(containerElement, fieldsDef, rowClass, data = null, isEditMode = false) {
    if (!containerElement) return;
    const row = document.createElement('div');
    row.className = `emp-child-row emp-grid-${fieldsDef.length}-cols mb-3 ${rowClass}`;

    fieldsDef.forEach(field => {
        const wrapper = document.createElement('div');

        const span = document.createElement('span');
        span.className = `detail-text d-block ${isEditMode ? 'hidden' : ''}`;

        let input;
        if (field.type === 'select') {
            input = document.createElement('select');
            field.options.forEach(opt => {
                const option = document.createElement('option');
                option.value = opt.value;
                option.textContent = opt.text;
                input.appendChild(option);
            });
        } else {
            input = document.createElement('input');
            input.type = field.type || 'text';
            input.placeholder = field.placeholder;
        }
        
        input.className = `detail-input w-100 ${field.className} ${isEditMode ? '' : 'hidden'}`;

        // ... Keep the rest of your original data binding code the same ...
```

**B. Update Data Mappings (`qualFields` & `expFields`):**
```javascript
    qualFields: [
        { 
            placeholder: 'Level', 
            className: 'q-level', 
            dataKey: 'qualificationLevel',
            type: 'select',
            options: [
                { value: 'PRIMARY', text: 'Primary' },
                { value: 'SECONDARY', text: 'Secondary' },
                { value: 'SENIOR_SECONDARY', text: 'Senior Secondary' },
                { value: 'CERTIFICATE', text: 'Certificate' },
                { value: 'DIPLOMA', text: 'Diploma' },
                { value: 'GRADUATION', text: 'Graduation' },
                { value: 'POST_GRADUATION', text: 'Post Graduation' },
                { value: 'DR_PHD', text: 'Dr (PHD)' },
                { value: 'OTHER', text: 'Other' }
            ]
        },
        { placeholder: 'Custom Level (If Other)', className: 'q-custom', dataKey: 'customLevel' },
        { placeholder: 'Subject / Specialization', className: 'q-inst', dataKey: 'institution' },
        { placeholder: 'Division / Grade', className: 'q-grade', dataKey: 'qualificationGrade' },
        { placeholder: 'Year', className: 'q-year', type: 'number', dataKey: 'passingYear' },
        { placeholder: 'Upload Cert', className: 'q-file', type: 'file', dataKey: 'fileData' }
    ],
    expFields: [
        { placeholder: 'Organisation', className: 'e-company', dataKey: 'companyName' },
        { 
            placeholder: 'Type', 
            className: 'e-type', 
            dataKey: 'employeeExperienceType',
            type: 'select',
            options: [
                { value: 'EMPLOYEE', text: 'Employee' },
                { value: 'SELF_EMPLOYED', text: 'Self Employed' }
            ]
        },
        { placeholder: 'Post Held', className: 'e-role', dataKey: 'jobRole' },
        { placeholder: 'Start Date', className: 'e-start', type: 'date', dataKey: 'startDate' },
        { placeholder: 'End Date', className: 'e-end', type: 'date', dataKey: 'endDate' },
        { placeholder: 'Upload Doc', className: 'e-file', type: 'file', dataKey: 'fileData' }
    ],
```

**C. Update `gatherAsync` parsing in your `save` function:**
```javascript
                // (Make sure to also include the top-level inputs you added)
                religion: viewContainer.querySelector('#add-empReligion').value || null,
                subReligion: viewContainer.querySelector('#add-empSubReligion').value.trim() || null,
                addressCounty: viewContainer.querySelector('#add-empCounty').value.trim() || null,
                addressSubCounty: viewContainer.querySelector('#add-empSubCounty').value.trim() || null,
                addressParish: viewContainer.querySelector('#add-empParish').value.trim() || null,
                skills: viewContainer.querySelector('#add-empSkills').value.trim() || null,
                languagesSpoken: viewContainer.querySelector('#add-empLanguages').value.trim() || null,

                qualifications: await EmpCollections.gatherAsync(viewContainer, '#qualifications-container', 'qual-row', async row => ({
                    employeeQualificationLevel: row.querySelector('.q-level').value.trim() || null,
                    customLevel: row.querySelector('.q-custom').value.trim() || null,
                    employeeQualificationSpecialization: row.querySelector('.q-inst').value.trim() || null,
                    qualificationGrade: row.querySelector('.q-grade').value.trim() || null,
                    employeeQualificationCompletionYear: parseInt(row.querySelector('.q-year').value) || null
                    // (Keep your file upload logic here)
                })),
                experiences: await EmpCollections.gatherAsync(viewContainer, '#experiences-container', 'exp-row', async row => ({
                    employeeExperienceCompanyName: row.querySelector('.e-company').value.trim() || null,
                    employeeExperienceType: row.querySelector('.e-type').value.trim() || null,
                    employeeExperienceJobRole: row.querySelector('.e-role').value.trim() || null,
                    employeeExperienceStartDate: row.querySelector('.e-start').value || null,
                    employeeExperienceEndDate: row.querySelector('.e-end').value || null
                    // (Keep your file upload logic here)
                })),
```

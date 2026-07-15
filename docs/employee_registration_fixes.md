# Fixes for Employee Validation & Button Layout

You hit the nail on the head! 
1. The **Register Employee** button looks terrible sitting at the top next to the Back button. 
2. The **validation errors** are happening because the JavaScript payload object is using old keys (e.g., `phone` instead of `mobileNo`, and `passingYear` instead of `employeeQualificationCompletionYear`), which do not match your backend Java DTO.

Here is how to fix both issues perfectly:

---

## 1. Move the Submit Button to the Bottom (HTML)

In your `add-employee.html`:

**First**, remove the Register button from the top `view-header`, so it just looks like this:
```html
<div class="view-header mb-4 d-flex justify-content-between align-items-center">
    <h2>Add New Employee</h2>
    <div class="action-btn-group">
        <button type="button" id="backToEmployeesBtn" class="btn btn-secondary me-2">
            <i class="bi bi-arrow-left"></i> Back
        </button>
    </div>
</div>
```

**Second**, scroll to the very bottom of the form (right below the `<fieldset id="documents-section">...</fieldset>`) and paste this before the closing `</div>` of the `form-container`:
```html
<!-- Submit Button at the bottom -->
<div class="d-flex justify-content-end mt-4 mb-2">
    <button type="submit" class="btn btn-primary btn-lg">
        <i class="bi bi-check-lg me-2"></i> Register Employee
    </button>
</div>
```

---

## 2. Fix the JavaScript Payload Mapping (JS)

Open your `src/main/resources/static/js/modules/employees.js` file.

You build the `payload` variable in **two places** (one for Editing around line 433, and one for Adding around line 665). 

Replace **BOTH** of those `const payload = { ... };` blocks with this exact mapped version:

```javascript
const payload = {
    title: valOrNull('#add-empTitle') || valOrNull('#edit-empTitle'),
    firstName: valOrNull('#add-empFirstName') || valOrNull('#edit-empFirstName'),
    middleName: valOrNull('#add-empMiddleName') || valOrNull('#edit-empMiddleName'),
    lastName: valOrNull('#add-empLastName') || valOrNull('#edit-empLastName'),
    gender: valOrNull('#add-empGender') || valOrNull('#edit-empGender'),
    dateOfBirth: dobVal, // NOTE: Make sure dobVal is passed correctly here, or keep your old line for dateOfBirth if it differs slightly between Add and Edit functions
    maritalStatus: valOrNull('#add-empMaritalStatus') || valOrNull('#edit-empMaritalStatus'),
    bloodGroup: valOrNull('#add-empBloodGroup') || valOrNull('#edit-empBloodGroup'),

    // FIXED Contact Mappings
    officialEmail: valOrNull('#add-empEmail') || valOrNull('#edit-empEmail'),
    personalEmail: valOrNull('#add-empPersonalEmail') || valOrNull('#edit-empPersonalEmail'),
    mobileNo: valOrNull('#add-empPhone') || valOrNull('#edit-empPhone'),
    alternateMobile: valOrNull('#add-empAlternatePhone') || valOrNull('#edit-empAlternatePhone'),

    // FIXED Job Mappings
    departmentId: valOrNull('#add-empDepartment') || valOrNull('#edit-empDepartment'),
    designationId: valOrNull('#add-empDesignation') || valOrNull('#edit-empDesignation'),
    employeeCategory: valOrNull('#add-empCategory') || valOrNull('#edit-empCategory'),
    employeeType: valOrNull('#add-empType') || valOrNull('#edit-empType'),
    employmentMode: valOrNull('#add-empMode') || valOrNull('#edit-empMode'),
    employmentStatus: 'ACTIVE', // Automatically defaults to Active

    joiningDate: valOrNull('#add-empJoiningDate') || valOrNull('#edit-empJoiningDate'),
    probationEndDate: valOrNull('#add-empProbationEndDate') || valOrNull('#edit-empProbationEndDate'),
    confirmationDate: valOrNull('#add-empConfirmationDate') || valOrNull('#edit-empConfirmationDate'),

    nationality: valOrNull('#add-empNationality') || valOrNull('#edit-empNationality'),
    nationalId: valOrNull('#add-empNationalId') || valOrNull('#edit-empNationalId'),
    tinNumber: valOrNull('#add-empTin') || valOrNull('#edit-empTin'),
    passportNo: valOrNull('#add-empPassportNo') || valOrNull('#edit-empPassportNo'),
    passportExpiryDate: valOrNull('#add-empPassportExpiry') || valOrNull('#edit-empPassportExpiry'),
    workPermitNumber: valOrNull('#add-empWorkPermit') || valOrNull('#edit-empWorkPermit'),
    workPermitExpiryDate: valOrNull('#add-empWorkPermitExpiry') || valOrNull('#edit-empWorkPermitExpiry'),

    addressCountry: valOrNull('#add-empCountry') || valOrNull('#edit-empCountry'),
    addressState: valOrNull('#add-empState') || valOrNull('#edit-empState'),
    addressDistrict: valOrNull('#add-empDistrict') || valOrNull('#edit-empDistrict'),
    addressVillage: valOrNull('#add-empVillage') || valOrNull('#edit-empVillage'),
    addressStreet: valOrNull('#add-empStreet') || valOrNull('#edit-empStreet'),

    contacts: EmpCollections.gather(viewContainer, '#contacts-container', 'contact-row', row => ({
        contactName: row.querySelector('.c-name').value.trim() || null,
        relationship: row.querySelector('.c-relation').value.trim() || null,
        phone: row.querySelector('.c-phone').value.trim() || null,
        email: row.querySelector('.c-email').value.trim() || null
    })),
    
    // FIXED Qualification Mappings
    qualifications: await EmpCollections.gatherAsync(viewContainer, '#qualifications-container', 'qual-row', async row => ({
        employeeQualificationLevel: row.querySelector('.q-level').value.trim() || null,
        employeeQualificationName: row.querySelector('.q-level').value.trim() || 'N/A',
        employeeQualificationInstitutionName: row.querySelector('.q-inst').value.trim() || null,
        employeeQualificationCompletionYear: parseInt(row.querySelector('.q-year').value) || null,
        fileData: await EmpCollections.fileToBase64(row.querySelector('.q-file')?.files[0]),
        fileName: row.querySelector('.q-file')?.files[0]?.name || null
    })),
    
    experiences: await EmpCollections.gatherAsync(viewContainer, '#experiences-container', 'exp-row', async row => ({
        companyName: row.querySelector('.e-company').value.trim() || null,
        jobRole: row.querySelector('.e-role').value.trim() || null,
        startDate: row.querySelector('.e-start').value || null,
        endDate: row.querySelector('.e-end').value || null,
        fileData: await EmpCollections.fileToBase64(row.querySelector('.e-file')?.files[0]),
        fileName: row.querySelector('.e-file')?.files[0]?.name || null
    })),
    
    documents: await EmpCollections.gatherAsync(viewContainer, '#documents-container', 'doc-row', async row => ({
        documentType: row.querySelector('.d-type').value.trim() || null,
        documentNumber: row.querySelector('.d-num').value.trim() || null,
        remarks: row.querySelector('.d-remarks').value.trim() || null,
        fileData: await EmpCollections.fileToBase64(row.querySelector('.d-file')?.files[0]),
        fileName: row.querySelector('.d-file')?.files[0]?.name || null
    }))
};
```

**Note**: In the Edit payload (around line 433), you might just have `dateOfBirth: valOrNull('#edit-empDob')` instead of `dateOfBirth: dobVal`. Just make sure that line matches what was originally there for DOB, and copy the rest of the fixed keys!

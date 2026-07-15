# Dynamic Sections HTML

Here is the perfect HTML layout for the 4 dynamic sections (`Contacts`, `Qualifications`, `Experiences`, and `Documents`). 

Instead of just plain containers, this HTML wraps each section in a beautiful, modern **Bootstrap Card** with an icon and a divider line. This will make your "Add Employee" screen look highly structured and professional.

### Where to put this:
Replace your existing `<div id="contacts-section">...</div>` and the other sections in `add-employee.html` with this code:

```html
<!-- ========================================== -->
<!-- 1. Contacts Section                        -->
<!-- ========================================== -->
<div class="card mb-4 shadow-sm border-0" id="contacts-section" style="background-color: #ffffff;">
    <div class="card-header bg-transparent pb-0 border-bottom-0 pt-4 px-4">
        <h5 class="mb-0 text-primary" style="font-weight: 600;">
            <i class="bi bi-person-lines-fill me-2"></i>Emergency Contacts
        </h5>
        <hr class="mt-2 mb-0" style="opacity: 0.15;">
    </div>
    <div class="card-body px-4">
        <!-- JS will inject rows here -->
        <div id="contacts-container"></div>
        <!-- JS will inject "+ Add Contact" button here -->
    </div>
</div>

<!-- ========================================== -->
<!-- 2. Qualifications Section                  -->
<!-- ========================================== -->
<div class="card mb-4 shadow-sm border-0" id="qualifications-section" style="background-color: #ffffff;">
    <div class="card-header bg-transparent pb-0 border-bottom-0 pt-4 px-4">
        <h5 class="mb-0 text-primary" style="font-weight: 600;">
            <i class="bi bi-mortarboard-fill me-2"></i>Educational Qualifications
        </h5>
        <hr class="mt-2 mb-0" style="opacity: 0.15;">
    </div>
    <div class="card-body px-4">
        <!-- JS will inject rows here -->
        <div id="qualifications-container"></div>
        <!-- JS will inject "+ Add Qualification" button here -->
    </div>
</div>

<!-- ========================================== -->
<!-- 3. Experiences Section                     -->
<!-- ========================================== -->
<div class="card mb-4 shadow-sm border-0" id="experiences-section" style="background-color: #ffffff;">
    <div class="card-header bg-transparent pb-0 border-bottom-0 pt-4 px-4">
        <h5 class="mb-0 text-primary" style="font-weight: 600;">
            <i class="bi bi-briefcase-fill me-2"></i>Work Experience
        </h5>
        <hr class="mt-2 mb-0" style="opacity: 0.15;">
    </div>
    <div class="card-body px-4">
        <!-- JS will inject rows here -->
        <div id="experiences-container"></div>
        <!-- JS will inject "+ Add Experience" button here -->
    </div>
</div>

<!-- ========================================== -->
<!-- 4. Documents Section                       -->
<!-- ========================================== -->
<div class="card mb-4 shadow-sm border-0" id="documents-section" style="background-color: #ffffff;">
    <div class="card-header bg-transparent pb-0 border-bottom-0 pt-4 px-4">
        <h5 class="mb-0 text-primary" style="font-weight: 600;">
            <i class="bi bi-file-earmark-text-fill me-2"></i>Other Documents
        </h5>
        <hr class="mt-2 mb-0" style="opacity: 0.15;">
    </div>
    <div class="card-body px-4">
        <!-- JS will inject rows here -->
        <div id="documents-container"></div>
        <!-- JS will inject "+ Add Document" button here -->
    </div>
</div>
```

### CSS Reminder
Just to ensure you applied it, your `admin.css` should have this code to style the grids inside these containers (as some have 4 columns and others have 5 columns due to the File Uploads):

```css
#ba-add-employee-view .emp-grid-4-cols {
    grid-template-columns: repeat(4, 1fr) 35px; /* 4 inputs + 1 delete button */
}
#ba-add-employee-view .emp-grid-5-cols {
    grid-template-columns: repeat(5, 1fr) 35px; /* 5 inputs + 1 delete button */
}
```
*(The JavaScript automatically figures out if it needs `emp-grid-4-cols` or `emp-grid-5-cols` based on how many fields we defined in `EmpCollections`!)*

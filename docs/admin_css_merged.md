# Merged `admin.css`

I have taken the exact `admin.css` code you provided and seamlessly merged it with the new `fieldset`, `legend`, and dynamic grid styling. 

You can replace your entire CSS with this block. It maintains all your base layouts, dashboard cards, and pagination, while perfectly styling the new fieldset form we just created.

```css
/* ========================================= */
/* --- STRICTLY SCOPED BRANCH ADMIN CSS ---  */
/* ========================================= */

/* ----------------------------------------- */
/* 1. BASE LAYOUT & HEADERS                  */
/* ----------------------------------------- */
#admin-branch-dashboard-view,
#ba-applications-view,
#ba-departments-view,
#ba-designations-view,
#ba-add-dept-view,
#ba-add-desig-view,
#ba-employees-view,
#ba-add-employee-view {
    max-width: 1600px;
    margin: 0 auto;
    padding: 0.75rem;
    animation: fadeIn 0.4s ease-out;
}

/* ----------------------------------------- */
/* 2. DASHBOARD HOME CARDS                   */
/* ----------------------------------------- */
#admin-branch-dashboard-view .stats-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 1rem;
    margin-top: 1.5rem;
}
#admin-branch-dashboard-view .stat-card {
    background: #ffffff;
    border-radius: 8px;
    padding: 1rem;
    display: flex;
    align-items: center;
    border: 1px solid #e2e8f0;
    box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
    transition: all 0.2s ease;
}
#admin-branch-dashboard-view .stat-card:hover {
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
    border-color: #cbd5e1;
    transform: translateY(-2px);
}
#admin-branch-dashboard-view .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: clamp(1rem, 1.5vw, 1.25rem);
    margin-right: 1rem;
}
#admin-branch-dashboard-view .icon-blue { background: #eff6ff; color: #2563eb; }
#admin-branch-dashboard-view .icon-green { background: #ecfdf5; color: #059669; }
#admin-branch-dashboard-view .icon-orange { background: #fffbeb; color: #d97706; }
#admin-branch-dashboard-view .stat-details h3 {
    margin: 0;
    font-size: clamp(0.7rem, 1.2vw, 0.8rem);
    color: #64748b;
    text-transform: uppercase;
    font-weight: 600;
}
#admin-branch-dashboard-view .stat-details h1 {
    margin: 0.15rem 0 0 0;
    font-size: clamp(1.25rem, 2.5vw, 1.75rem);
    color: #0f172a;
    font-weight: 700;
}

/* ----------------------------------------- */
/* 3. BRANCH ADMIN SPECIFIC MODALS & PANELS  */
/* ----------------------------------------- */
.ba-modal-backdrop {
    position: fixed;
    top: 0; left: 0; width: 100vw; height: 100vh;
    background-color: rgba(0, 0, 0, 0.5);
    z-index: 1050;
    display: flex;
    align-items: center;
    justify-content: center;
}
.ba-modal-box {
    background-color: #ffffff;
    width: 400px;
    padding: 15px;
    border-radius: 6px;
    box-shadow: 0 4px 15px rgba(0,0,0,0.2);
}
.ba-modal-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    margin-top: 15px;
}
.ba-form-container {
    position: relative;
}
.ba-form-overlay {
    position: absolute;
    top: 0; left: 0; width: 100%; height: 100%;
    background-color: rgba(255, 255, 255, 0.7);
    z-index: 10;
    display: flex;
    align-items: center;
    justify-content: center;
}
.ba-form-locked {
    pointer-events: none;
    opacity: 0.5;
}
.ba-flex-panel {
    display: flex;
    gap: 10px;
    align-items: flex-end;
    flex-wrap: wrap;
    margin-bottom: 12px;
}
.ba-flex-1 {
    flex: 1;
    min-width: 150px;
}

/* ----------------------------------------- */
/* 4. PAGINATION UI                          */
/* ----------------------------------------- */
.ba-pagination-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 15px;
    border-top: 1px solid #e2e8f0;
    background: #ffffff;
    border-bottom-left-radius: 8px;
    border-bottom-right-radius: 8px;
    flex-wrap: wrap;
    gap: 10px;
}
.ba-pagination-controls {
    display: flex;
    align-items: center;
    gap: 8px;
}
.ba-pagination-controls span.text-muted {
    color: #64748b;
    font-size: 0.85rem;
    font-weight: 500;
}

/* ----------------------------------------- */
/* 5. MODULE UTILITIES & FORMS               */
/* ----------------------------------------- */
.filter-grid-layout {
    grid-template-columns: 1fr 1fr 1fr auto;
    align-items: end;
}
#ba-departments-view .ba-filter-grid,
#ba-designations-view .ba-filter-grid {
    display: grid;
    gap: 10px;
}
.btn-filter-search {
    margin-bottom: 4px;
}
.required-mark {
    color: #ef4444;
    margin-left: 2px;
}

/* --- ADD DEPARTMENT & DESIGNATION FORM STYLES --- */
#ba-add-dept-view .form-card,
#ba-add-employee-view .form-card,
#ba-add-desig-view .form-card {
    max-width: 600px;
    margin: 0 auto;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}
#ba-add-dept-view .form-card .card-body,
#ba-add-employee-view .form-card .card-body,
#ba-add-desig-view .form-card .card-body {
    padding: 1rem 1.25rem;
}

/* Shared 2-Column Detail Grid for both List Views and Add Forms */
#ba-departments-view .ba-detail-grid,
#ba-designations-view .ba-detail-grid,
#ba-add-dept-view .ba-detail-grid,
#ba-add-employee-view .ba-detail-grid,
#ba-add-desig-view .ba-detail-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 0.75rem 1rem;
    margin-bottom: 1rem;
}
#ba-departments-view .ba-detail-full-width,
#ba-add-employee-view .ba-detail-full-width,
#ba-designations-view .ba-detail-full-width {
    grid-column: span 2;
}

/* Remove compounding margins inside the grid */
#ba-add-dept-view .ba-detail-grid .form-group,
#ba-add-employee-view .ba-detail-grid .form-group,
#ba-add-desig-view .ba-detail-grid .form-group {
    margin-bottom: 0;
}
/* Standard tight form group spacing */
#ba-add-dept-view .form-group,
#ba-add-employee-view .form-group,
#ba-add-desig-view .form-group {
    margin-bottom: 0.75rem;
}
/* Crisp, professional labels */
#ba-add-dept-view .form-group label,
#ba-add-employee-view .form-group label,
#ba-add-desig-view .form-group label {
    display: block;
    margin-bottom: 0.25rem;
    font-size: 0.8rem;
    font-weight: 600;
    color: #475569;
}

/* Sleek, dense enterprise inputs */
#ba-add-dept-view .detail-input,
#ba-add-employee-view .detail-input,
#ba-add-desig-view .detail-input {
    background-color: #f8fafc;
    border: 1px solid #cbd5e1;
    color: #0f172a;
    border-radius: 4px;
    padding: 0.4rem 0.6rem;
    font-size: 0.85rem;
    box-shadow: inset 0 1px 2px rgba(0,0,0,0.02);
    transition: all 0.2s ease;
}
#ba-add-dept-view .detail-input::placeholder,
#ba-add-employee-view .detail-input::placeholder,
#ba-add-desig-view .detail-input::placeholder {
    color: #94a3b8;
}
#ba-add-dept-view .detail-input:hover,
#ba-add-employee-view .detail-input:hover,
#ba-add-desig-view .detail-input:hover {
    background-color: #f1f5f9;
    border-color: #94a3b8;
}
#ba-add-dept-view .detail-input:focus,
#ba-add-employee-view .detail-input:focus,
#ba-add-desig-view .detail-input:focus {
    background-color: #ffffff;
    border-color: #d4af37;
    box-shadow: 0 0 0 3px rgba(212, 175, 55, 0.15);
    outline: none;
}
#ba-add-dept-view textarea.detail-input,
#ba-add-employee-view textarea.detail-input,
#ba-add-desig-view textarea.detail-input {
    min-height: 60px;
    resize: vertical;
}
#ba-add-dept-view .form-actions,
#ba-add-employee-view .form-actions,
#ba-add-desig-view .form-actions {
    margin-top: 0.5rem;
    padding-top: 1rem;
    border-top: 1px solid #e2e8f0;
}

/* ========================================================= */
/* --- ADD EMPLOYEE: FIELDSET & GRID LAYOUTS (NEW) ---       */
/* ========================================================= */

#ba-add-employee-view .emp-grid {
    display: grid;
    gap: 15px;
}
#ba-add-employee-view .emp-grid-2 { grid-template-columns: repeat(2, 1fr); }
#ba-add-employee-view .emp-grid-3 { grid-template-columns: repeat(3, 1fr); }
#ba-add-employee-view .emp-grid-4 { grid-template-columns: repeat(4, 1fr); }

/* Replaces inline style="grid-column: span 2;" */
#ba-add-employee-view .span-2-cols { grid-column: span 2; } 

#ba-add-employee-view .emp-name-container {
    display: flex;
    gap: 10px;
}

/* --- Fieldset & Legend Styling --- */
#ba-add-employee-view .emp-fieldset {
    border: 1px solid #ced4da;
    border-radius: 8px;
    padding: 1.5rem 1.5rem 1rem 1.5rem;
    margin-bottom: 2rem;
    background-color: #fafbfc;
    box-shadow: 0 1px 3px rgba(0,0,0,0.02);
}

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

/* --- Dynamic Sections Container (Contacts, Qualifications etc.) --- */
#ba-add-employee-view .emp-child-row {
    background: #ffffff;
    border: 1px solid #eef2f5;
    padding: 12px;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0,0,0,0.02);
    align-items: center;
    gap: 12px;
    display: grid;
    margin-bottom: 10px;
}
#ba-add-employee-view .emp-child-row > * {
    min-width: 0;
}

/* Grid sizes for the dynamic rows - explicitly fixed to allow space for the delete button */
#ba-add-employee-view .emp-grid-3-cols, #ba-employees-view .emp-grid-3-cols { grid-template-columns: repeat(3, 1fr) 35px; }
#ba-add-employee-view .emp-grid-4-cols, #ba-employees-view .emp-grid-4-cols { grid-template-columns: repeat(4, 1fr) 35px; }
#ba-add-employee-view .emp-grid-5-cols, #ba-employees-view .emp-grid-5-cols { grid-template-columns: repeat(5, 1fr) 35px; }

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

/* Delete (X) Button inside dynamic rows */
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

/* --- Modern File Input Styling --- */
#ba-add-employee-view input[type="file"].detail-input {
    padding: 0.375rem 0.75rem;
    background-color: #f8f9fa;
    border: 1px dashed #ced4da;
    border-radius: 6px;
    color: #495057;
    cursor: pointer;
    transition: all 0.2s ease-in-out;
}
#ba-add-employee-view input[type="file"].detail-input:hover {
    background-color: #e2e6ea;
    border-color: #0d6efd;
}
#ba-add-employee-view input[type="file"].detail-input::file-selector-button {
    background-color: #0d6efd;
    color: white;
    border: none;
    border-radius: 4px;
    padding: 4px 12px;
    margin-right: 10px;
    cursor: pointer;
    font-weight: 500;
    transition: background-color 0.2s;
}
#ba-add-employee-view input[type="file"].detail-input::file-selector-button:hover {
    background-color: #0b5ed7;
}
```

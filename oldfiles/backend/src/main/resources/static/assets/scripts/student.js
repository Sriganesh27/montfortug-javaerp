// school_administration1/assets/scripts/student.js
console.log("STATUS: Student.js loaded.");

// =========================================================================
// 0. SMART DYNAMIC BASE URL & API PATH HELPER
// =========================================================================
window.getSystemBaseUrl = function() {
    const baseUrl = window.location.origin;
    const pathname = window.location.pathname;
    const erpPos = pathname.indexOf('/erp');
    
    if (erpPos !== -1) {
        return baseUrl + pathname.substring(0, erpPos + 4) + '/public';
    }
    return baseUrl; // Fallback for live server where /public is the root
};

const getApiPrefix = () => {
    return window.getSystemBaseUrl() + '/';
};

window.getCleanMediaUrl = function(dbPath) {
    if (!dbPath || dbPath.trim() === '') return '';
    if (dbPath.startsWith('http')) return dbPath; // Already a full URL
    
    const assetsIndex = dbPath.indexOf('assets/');
    if (assetsIndex !== -1) {
        return window.getSystemBaseUrl() + '/' + dbPath.substring(assetsIndex);
    }
    
    return window.getSystemBaseUrl() + '/' + dbPath.replace(/^\/+/, '');
};

// =========================================================================
// 1. GLOBAL HELPERS
// =========================================================================
const classesMap = {
    'nursery': [{val: 'N1', label: 'Baby Class (N1)'}, {val: 'N2', label: 'Middle Class (N2)'}, {val: 'N3', label: 'Top Class (N3)'}],
    'pre-primary': [{val: 'N1', label: 'Baby Class (N1)'}, {val: 'N2', label: 'Middle Class (N2)'}, {val: 'N3', label: 'Top Class (N3)'}],
    'primary': ['P1', 'P2', 'P3', 'P4', 'P5', 'P6', 'P7'].map(c => ({val: c, label: c})),
    'secondary': ['S1', 'S2', 'S3', 'S4', 'S5', 'S6'].map(c => ({val: c, label: c}))
};

window.populateLevelDropdown = async function(levelEl, includeAll = false) {
    if (!levelEl) return;
    levelEl.innerHTML = includeAll ? '<option value="">All</option>' : '<option value="">Select Level</option>';
    
    let branchType = window.currentBranchType;
    if (!branchType) {
        try {
            const res = await fetch('/api/admin/applications/branch-info', { headers: { 'Authorization': 'Bearer ' + localStorage.getItem('jwtToken') } });
            const data = await res.json();
            if(data.success && data.data) {
                branchType = data.data.branch_type;
                window.currentBranchType = branchType;
            }
        } catch (e) { console.error("Error fetching branch info for levels", e); }
    }
    branchType = branchType || 'Nursery / Primary / Secondary';
    let levels = branchType.split('/').map(l => l.trim());
    levels.forEach(l => {
        levelEl.innerHTML += `<option value="${l}">${l}</option>`;
    });
};

window.populateClassDropdown = function(levelEl, classEl, includeAll = false) {
    if (!levelEl || !classEl) return;
    
    let selectedLevel = levelEl.value ? levelEl.value.toLowerCase().trim() : '';
    let targetValue = classEl.getAttribute('data-initial-value');
    if (!targetValue) targetValue = classEl.value;

    const isEditForm = classEl.closest('#edit-mode-form') !== null;

    classEl.innerHTML = '';

    if (includeAll) {
        const allOpt = document.createElement('option');
        allOpt.value = "";
        allOpt.textContent = "All Classes";
        classEl.appendChild(allOpt);
    } else if (!isEditForm) {
        const selOpt = document.createElement('option');
        selOpt.value = "";
        selOpt.textContent = "Select Class";
        classEl.appendChild(selOpt);
    }
    
    // Bind toggleExamFields to classEl onchange if it's the admission form class select
    if (classEl.id === 'class') {
        classEl.onchange = function() {
            window.toggleExamFields(this.value);
            window.populateStreamDropdown(this, document.getElementById('stream'));
        };
    }
    
    if (classesMap[selectedLevel]) {
        classesMap[selectedLevel].forEach(cls => {
            const opt = document.createElement('option');
            opt.value = cls.val;
            opt.textContent = cls.label;
            classEl.appendChild(opt);
        });
    } else if (!includeAll && !isEditForm) {
        classEl.innerHTML = '<option value="">Select Level First</option>';
    }
    
    if (targetValue) {
        for (let i = 0; i < classEl.options.length; i++) {
            if (classEl.options[i].value === targetValue) {
                classEl.selectedIndex = i;
                break;
            }
        }
    }
    
    // Initial stream population if class has a value
    if (classEl.id === 'class') {
        window.populateStreamDropdown(classEl, document.getElementById('stream'));
    }
};

window.populateStreamDropdown = function(classEl, streamEl, includeAll = false) {
    if (!classEl || !streamEl) return;
    
    let targetStream = streamEl.getAttribute('data-initial-value');
    if (!targetStream) targetStream = streamEl.value;

    if (includeAll) {
        streamEl.innerHTML = '<option value="">All</option>';
    } else {
        streamEl.innerHTML = '<option value="">Select Stream</option>';
    }
    const cVal = (classEl.value || '').trim().toUpperCase();
    if (!cVal) return;
    
    const streamsMap = {
        'S1': ['A', 'B', 'C', 'D'],
        'S2': ['A', 'B', 'C', 'D'],
        'S3': ['Arts', 'Sciences'],
        'S4': ['Arts', 'Sciences'],
        'S5': ['Arts', 'Sciences'],
        'S6': ['Arts', 'Sciences'],
        'P1': ['A', 'B', 'C'], 'P2': ['A', 'B', 'C'], 'P3': ['A', 'B', 'C'], 'P4': ['A', 'B', 'C'], 'P5': ['A', 'B', 'C'], 'P6': ['A', 'B', 'C'], 'P7': ['A', 'B', 'C'],
        'default': ['A', 'B', 'C']
    };
    
    let streams = streamsMap[cVal] || streamsMap['default'];
    streams.forEach(s => {
        streamEl.appendChild(new Option(s, s));
    });
    
    if (targetStream && Array.from(streamEl.options).some(o => o.value === targetStream)) {
        streamEl.value = targetStream;
    }
};

// --- POPUP SYSTEM ---

window.showCustomAlert = function(type, title, message) {
    let modalContainer = document.getElementById('custom-alert-modal');
    if (!modalContainer) {
        modalContainer = document.createElement('div');
        modalContainer.id = 'custom-alert-modal';
        modalContainer.className = 'modal-backdrop';
        document.body.appendChild(modalContainer);
    }
    
    const colorMap = { success: 'var(--success-color)', error: 'var(--danger-color)', warning: 'var(--warning-color)', info: 'var(--info-color)' };
    let variantClass = type === 'error' ? 'error' : (type === 'warning' ? 'warning' : 'success');
    let iconClass = type === 'error' ? 'bi-x-circle' : (type === 'warning' ? 'bi-exclamation-triangle' : 'bi-check-circle');

    modalContainer.innerHTML = `
        <div class="modal-content confirm-modal animated-pop">
            <div class="success-header">
                <div class="confirm-icon-circle ${variantClass}" style="color: ${colorMap[type]}; font-size: 3rem; margin-bottom: 10px; animation: popIn 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);">
                    <i class="bi ${iconClass}"></i>
                </div>
            </div>
            <h4 class="modal-title" style="margin-bottom:10px;">${title}</h4>
            <div class="modal-message" style="color:var(--text-color); margin-bottom:20px;">${message}</div>
            <div class="modal-actions centered"><button class="btn-primary modal-close-btn">OK</button></div>
        </div>`;
    
    const closeBtn = modalContainer.querySelector('.modal-close-btn');
    if(closeBtn) closeBtn.onclick = function() { modalContainer.classList.remove('show'); };
    setTimeout(() => modalContainer.classList.add('show'), 10);
};

window.showActionConfirm = function(title, message, confirmBtnText, confirmBtnColor, callback) {
    let modal = document.getElementById('action-confirm-modal');
    if (!modal) {
        modal = document.createElement('div');
        modal.id = 'action-confirm-modal';
        modal.className = 'modal-backdrop';
        document.body.appendChild(modal);
    }
    modal.innerHTML = `
        <div class="modal-content confirm-modal animated-pop">
            <div class="success-header">
                <div class="confirm-icon-circle warning" style="color: var(--warning-color); font-size: 3rem; margin-bottom: 10px; animation: popIn 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);">
                    <i class="bi bi-question-circle"></i>
                </div>
            </div>
            <h4 class="modal-title">${title}</h4>
            <div class="modal-message" style="color:var(--text-color); margin-bottom:20px;">${message}</div>
            <div class="modal-actions centered" style="gap:15px; display:flex; justify-content:center;">
                <button class="btn-primary" id="confirm-modal-cancel" style="background:var(--secondary-color); color:var(--text-color-inverse);">No, Return</button>
                <button class="btn-primary" id="confirm-modal-yes" style="background:${confirmBtnColor};">${confirmBtnText}</button>
            </div>
        </div>`;
    const close = () => modal.classList.remove('show');
    modal.querySelector('#confirm-modal-cancel').onclick = close;
    modal.querySelector('#confirm-modal-yes').onclick = function() { close(); if (callback) callback(); };
    setTimeout(() => modal.classList.add('show'), 10);
};

window.confirmAction = function(actionType, id, extraData = null) {
    const actions = {
        'quick_save': { title: 'Update Student?', message: 'Are you sure you want to save these details?', btnText: 'Yes, Update', color: 'var(--success-color)', callback: () => saveQuickRow(id) },
        'quick_cancel': { title: 'Discard Changes?', message: 'Unsaved changes will be lost.', btnText: 'Yes, Discard', color: 'var(--danger-color)', callback: () => cancelQuickRow(id) },
        'academic_save': { title: 'Save Records?', message: 'Update these exam results?', btnText: 'Yes, Save', color: 'var(--success-color)', callback: () => saveAcademicRow(id) },
        'academic_cancel': { title: 'Cancel Editing?', message: 'Changes will be lost.', btnText: 'Yes, Return', color: 'var(--danger-color)', callback: () => cancelAcademicRow(id) },
        'profile_save': { title: 'Save Profile?', message: 'Update student profile details?', btnText: 'Yes, Save', color: 'var(--success-color)', callback: () => executeProfileUpdate() },
        'profile_cancel': { title: 'Stop Editing?', message: 'Unsaved changes will be lost.', btnText: 'Yes, Return', color: 'var(--danger-color)', callback: () => abortProfileEdit() },
        'delete_student': { title: 'Delete Student?', message: `Delete <strong>${extraData || 'this student'}</strong>? <br><span style="color:var(--danger-color);">Cannot be undone.</span>`, btnText: 'Yes, Delete', color: 'var(--danger-color)', callback: () => executeDelete(id) },
        'migration': { title: 'Confirm Migration', message: `<p>Move <strong>${extraData ? extraData.label : ''}</strong>?</p><div class="migration-summary modal-details" style="margin-top:10px;">To: <span style="font-weight:bold;">${extraData ? extraData.targetDetails : ''}</span></div>`, btnText: 'Yes, Proceed', color: 'var(--info-color)', callback: () => executeMigration(extraData ? extraData.payload : null) },
        'photo_save': { title: 'Update Photo?', message: 'Replace the current photo?', btnText: 'Yes, Upload', color: 'var(--success-color)', callback: () => executePhotoUpload(id) }
    };
    const config = actions[actionType];
    if (config) window.showActionConfirm(config.title, config.message, config.btnText, config.color, config.callback);
};

window.showDeleteConfirmModal = function(id, name) { window.confirmAction('delete_student', id, name); };

// =========================================================================
// 2. CORE VIEW FUNCTIONS
// =========================================================================

window.loadStudentList = function(e) {
    if (e) e.preventDefault();
    const container = document.getElementById('student-list-results');
    const form = document.getElementById('student-filter-form');
    if(!container || !form) return;
    const formData = new FormData(form);
    let hasFilter = false;
    for (const [key, value] of formData.entries()) { if (key !== 'module' && value.trim() !== "") hasFilter = true; }
    if (!hasFilter) { container.innerHTML = '<div style="text-align:center; padding: 60px 20px; color: #777;"><i class="bi bi-search" style="font-size: 3rem; color: #ddd; margin-bottom: 15px;"></i><p>Please select a filter.</p></div>'; return; }
    let params = new URLSearchParams(formData).toString();
    container.innerHTML = '<div style="text-align:center;padding:40px;"><i class="bi bi-arrow-repeat fa-spin fa-3x" style="color:var(--primary-color);"></i><p>Searching...</p></div>';
    fetch(`${getApiPrefix()}api/admin/students/quick-edit?${params}`, {
        headers: { 'Authorization': 'Bearer ' + localStorage.getItem('jwtToken') }
    }).then(r => r.json()).then(data => {
        if (!data.success || !data.data || data.data.length === 0) {
            container.innerHTML = '<div style="text-align:center; padding: 60px 20px; color: #777;">No records found.</div>';
            return;
        }
        let html = '<div class="table-container"><table class="data-table"><thead><tr><th>Photo</th><th>Student</th><th>Class Info</th><th>Residence</th><th>Actions</th></tr></thead><tbody>';
        data.data.forEach(student => {
            const photo = student.PhotoPath ? window.getCleanMediaUrl(student.PhotoPath) : 'static/images/default_profile.png';
            html += `<tr>
                <td><img src="${photo}" class="student-photo-thumb" onerror="this.src='https://ui-avatars.com/api/?name=${encodeURIComponent(student.Name + ' ' + student.Surname)}&background=random&color=fff'"></td>
                <td><strong>${student.Name} ${student.Surname}</strong><br><small>Stu-Id: ${student.StudentID || student.AdmissionNo} | ${student.Gender || '-'}</small></td>
                <td>${student.Class} ${student.Stream ? '<span class="badge">'+student.Stream+'</span>' : ''}<br><small>${student.AcademicYear || '-'}</small></td>
                <td>${student.Residence || '-'}<br><small>${student.Term || '-'}</small></td>
                <td>
                    <div style="display:flex; gap:5px; justify-content:center; align-items:center;">
                        <button class="btn-action view-btn" onclick="loadProfileViaAjax(${student.AdmissionNo})"><i class="fa fa-user" style="padding: 2px;"></i> Profile</button>
                        <button class="btn-action delete-btn" onclick="window.showArchiveModal(${student.AdmissionNo}, '${(student.Name + ' ' + student.Surname).replace(/'/g, "\\'")}')"><i class="fa fa-archive"></i> Archive</button>
                    </div>
                </td>
            </tr>`;
        });
        html += '</tbody></table></div>';
        container.innerHTML = html;
    }).catch(err => {
        container.innerHTML = '<div style="text-align:center; padding: 40px; color: red;">Failed to fetch students.</div>';
    });
};

window.loadSummary = function(e) {
    if (e) e.preventDefault();
    const container = document.getElementById('summary-results');
    const form = document.getElementById('summary-filter-form');
    if(!container) return;
    let params = form ? new URLSearchParams(new FormData(form)).toString() : "";
    container.innerHTML = '<div style="text-align:center;padding:40px;"><i class="bi bi-arrow-repeat fa-spin fa-3x" style="color:var(--primary-color);"></i><p>Calculating...</p></div>';
    fetch(`${getApiPrefix()}modules/students/partial/studentsummary.php?${params}`).then(r => r.text()).then(html => { container.innerHTML = html; window.initSummaryTabs(container); });
};

window.initSummaryTabs = function(context) {
    const mainTabs = context.querySelectorAll('.summary-tabs .tab-button');
    if (mainTabs.length) {
        mainTabs.forEach(btn => {
            btn.addEventListener('click', function(e) {
                e.preventDefault();
                const container = this.closest('.tabbed-details-container');
                if (!container) return;
                const tabGroup = this.closest('.summary-tabs');
                tabGroup.querySelectorAll('.tab-button').forEach(b => b.classList.remove('active'));
                const allContents = container.querySelectorAll('.tab-content');
                allContents.forEach(content => { if (content.parentElement === container) { content.style.display = 'none'; content.classList.remove('active'); } });
                this.classList.add('active');
                const targetId = this.dataset.tab;
                const targetContent = document.getElementById(targetId);
                if (targetContent) { targetContent.style.display = 'block'; targetContent.classList.add('active'); }
            });
        });
    }
};

window.showArchiveModal = function(admissionNo, name) {
    const html = `
        <div id="archive-modal-overlay" style="position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.5); z-index:9999; display:flex; justify-content:center; align-items:center;">
            <div style="background:white; padding:20px; border-radius:8px; width:400px; max-width:90%; box-shadow: 0 4px 15px rgba(0,0,0,0.2);">
                <h3 style="color:var(--danger-color); margin-top:0; border-bottom:1px solid #eee; padding-bottom:10px;"><i class="fa fa-archive"></i> Archive Student</h3>
                <p style="margin: 15px 0;">Are you sure you want to archive <strong style="color:var(--primary-color);">${name}</strong>?</p>
                <div style="margin-bottom:20px;">
                    <label style="display:block; font-weight:bold; margin-bottom:5px; color:#555;">Reason for leaving:</label>
                    <select id="archive-reason" style="width:100%; padding:10px; border:1px solid #ccc; border-radius:4px; font-size:1em;">
                        <option value="Graduated / Alumni">Graduated / Alumni</option>
                        <option value="Transferred">Transferred</option>
                        <option value="Dropped Out">Dropped Out</option>
                    </select>
                </div>
                <div style="display:flex; gap:10px; justify-content:flex-end;">
                    <button class="btn-primary" style="background:#6c757d;" onclick="document.getElementById('archive-modal-overlay').remove()">Cancel</button>
                    <button class="btn-primary" style="background:var(--danger-color);" onclick="executeDelete(${admissionNo}, document.getElementById('archive-reason').value); document.getElementById('archive-modal-overlay').remove()">Confirm Archive</button>
                </div>
            </div>
        </div>
    `;
    document.body.insertAdjacentHTML('beforeend', html);
};

window.executeDelete = function(admissionNo, exitReason = "Archived") {
    fetch(`${getApiPrefix()}api/admin/students/delete`, { 
        method: 'POST', 
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'Authorization': 'Bearer ' + localStorage.getItem('jwtToken')
        }, 
        body: `id=${admissionNo}&exitReason=${encodeURIComponent(exitReason)}` 
    })
    .then(r => r.json())
    .then(data => {
        const modal = document.getElementById('action-confirm-modal');
        if(modal) modal.classList.remove('show'); 
        
        if(data.success) {
            window.showCustomAlert('success', 'Archived', data.message || 'Student archived successfully.');
            setTimeout(() => {
                const isSpaProfile = window.location.hash === '#profile';
                const isStandaloneProfile = window.location.href.indexOf('studentprofile.php') > -1;

                if (isSpaProfile || isStandaloneProfile) {
                    if (window.history.length > 1) {
                        window.history.back();
                    } else {
                        window.location.href = isStandaloneProfile ? '../../index.php#student' : '#student';
                        if(!isStandaloneProfile && typeof window.loadStudentList === 'function') {
                            window.loadStudentList(); 
                        }
                    }
                } 
                else if (typeof window.loadStudentList === 'function') {
                    window.loadStudentList();
                }
            }, 500);
        } else {
            window.showCustomAlert('error', 'Delete Failed', data.message);
        }
    })
    .catch(err => {
        console.error("Delete Error:", err);
        window.showCustomAlert('error', 'System Error', 'Could not delete record.');
    });
};

window.printStudentProfile = function(admissionNo) {
    const printUrl = `${getApiPrefix()}modules/students/print_student.php?ad_no=${admissionNo}`;
    let existingFrame = document.getElementById('print-frame');
    if (existingFrame) document.body.removeChild(existingFrame);
    const iframe = document.createElement('iframe');
    iframe.id = 'print-frame';
    iframe.style.position = 'fixed'; iframe.style.right = '0'; iframe.style.bottom = '0'; iframe.style.width = '0'; iframe.style.height = '0'; iframe.style.border = '0';
    iframe.src = printUrl;
    document.body.appendChild(iframe);
    iframe.onload = function() { setTimeout(() => { try { iframe.contentWindow.focus(); iframe.contentWindow.print(); } catch(e) { console.error("Print Error:", e); } }, 500); };
};

window.toggleStudentHistory = function(admissionNo) {
    const wrapper = document.getElementById('history-content-wrapper');
    const tbody = document.getElementById('history-table-body');
    const emptyMsg = document.getElementById('history-empty-msg');
    if (wrapper.style.display === 'block') { wrapper.style.display = 'none'; return; }
    wrapper.style.display = 'block';
    tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:20px;"><i class="bi bi-arrow-repeat fa-spin"></i> Loading...</td></tr>';
    if(emptyMsg) emptyMsg.style.display = 'none';

    fetch(`${getApiPrefix()}api/students/get_student_history.php?id=${admissionNo}`).then(response => response.json()).then(data => {
        tbody.innerHTML = ''; 
        if (data.success && data.data.length > 0) {
            data.data.forEach(row => {
                const tr = document.createElement('tr');
                tr.innerHTML = `<td style="font-weight:600; color:#1e88e5;">${row.DateMoved}</td><td>${row.AcademicYear}</td><td>${row.Term}</td><td>${row.Class}</td><td>${row.Stream ? row.Stream : '-'}</td><td>${row.Residence}</td><td><span class="badge">${row.EntryStatus}</span></td>`;
                tbody.appendChild(tr);
            });
        } else {
            if (data.success) { if(emptyMsg) emptyMsg.style.display = 'block'; }
            else { tbody.innerHTML = `<tr><td colspan="7" style="color:red; text-align:center;">Error: ${data.message}</td></tr>`; }
        }
    }).catch(err => { console.error(err); tbody.innerHTML = '<tr><td colspan="7" style="color:red; text-align:center;">Failed to load history.</td></tr>'; });
};

// =========================================================================
// 3. QUICK EDIT MODULE
// =========================================================================
let currentQuickEditPage = 1;
function initQuickEditModule() {
    const qeLevel = document.getElementById('qe-level'), qeClass = document.getElementById('qe-class'), qeForm = document.getElementById('quick-edit-filter-form');
    if (qeLevel && qeClass) { qeLevel.addEventListener('change', () => window.populateClassDropdown(qeLevel, qeClass, true)); window.populateClassDropdown(qeLevel, qeClass, true); }
    if (qeForm) { qeForm.addEventListener('submit', (e) => { e.preventDefault(); currentQuickEditPage = 1; loadQuickEditData(currentQuickEditPage); }); }
    const prevBtn = document.getElementById('qe-prev-btn'), nextBtn = document.getElementById('qe-next-btn');
    if (prevBtn) prevBtn.addEventListener('click', () => { if (currentQuickEditPage > 1) loadQuickEditData(--currentQuickEditPage); });
    if (nextBtn) nextBtn.addEventListener('click', () => { loadQuickEditData(++currentQuickEditPage); });
    if (document.getElementById('qe-table-body')) loadQuickEditData(1);
}

window.loadQuickEditData = function(page) {
    currentQuickEditPage = page;
    const form = document.getElementById('quick-edit-filter-form');
    if (!form) return;
    const limit = document.getElementById('qe-limit').value, tbody = document.getElementById('qe-table-body');
    const params = new URLSearchParams(new FormData(form));
    params.append('page', page); params.append('limit', limit);
    tbody.innerHTML = '<tr><td colspan="10" style="text-align:center; padding:20px;"><i class="bi bi-arrow-repeat fa-spin"></i> Loading...</td></tr>';
    fetch(`${getApiPrefix()}api/admin/students/quick-edit?${params.toString()}`).then(r => r.json()).then(data => {
        if (data.success) { renderQuickEditTable(data); updateQuickEditPagination(data); } else { tbody.innerHTML = `<tr><td colspan="10" class="text-center" style="color:red;">Error: ${data.message}</td></tr>`; }
    }).catch(err => { console.error(err); tbody.innerHTML = '<tr><td colspan="10" class="text-center" style="color:red;">Connection Error</td></tr>'; });
};

function renderQuickEditTable(data) {
    const tbody = document.getElementById('qe-table-body'); tbody.innerHTML = '';
    if (!data.data || data.data.length === 0) { tbody.innerHTML = '<tr><td colspan="10" class="text-center" style="padding:20px;">No records found.</td></tr>'; return; }
    data.data.forEach(row => {
        const tr = document.createElement('tr'); tr.id = `row-${row.AdmissionNo}`; tr.dataset.original = JSON.stringify(row);
        tr.innerHTML = `<td><strong>${row.AdmissionNo}</strong></td><td class="cell-name">${row.Name} ${row.Surname}</td><td class="cell-lin">${row.LIN || '-'}</td><td class="cell-gender">${row.Gender}</td><td class="cell-class">${row.Class}</td><td class="cell-stream">${row.Stream || ''}</td><td class="cell-residence">${row.Residence}</td><td class="cell-entry">${row.EntryStatus}</td><td class="cell-contact">${row.Contact || ''}</td><td class="cell-paycode">${row.PayCode || '-'}</td><td class="action-cell"><button class="btn-action view-btn" onclick="editQuickRow(${row.AdmissionNo})"><i class="bi bi-pencil"></i> Edit</button></td>`;
        tbody.appendChild(tr);
    });
}

function updateQuickEditPagination(data) {
    const prevBtn = document.getElementById('qe-prev-btn'), nextBtn = document.getElementById('qe-next-btn'), pageNumbers = document.getElementById('qe-page-numbers'), info = document.getElementById('qe-pagination-info');
    const start = (data.page - 1) * data.limit + 1, end = Math.min(start + data.limit - 1, data.total);
    info.textContent = `Showing ${start}-${end} of ${data.total}`;
    if(prevBtn) prevBtn.disabled = data.page <= 1;
    if(nextBtn) nextBtn.disabled = data.page >= data.total_pages;
    if(pageNumbers) {
        pageNumbers.innerHTML = ''; const totalPages = data.total_pages, current = parseInt(data.page);
        let pagesToShow = [];
        if (totalPages <= 7) pagesToShow = Array.from({length: totalPages}, (_, i) => i + 1);
        else { if (current <= 4) pagesToShow = [1, 2, 3,'...', totalPages]; else if (current >= totalPages - 3) pagesToShow = [1, '...', totalPages - 4, totalPages - 3, totalPages - 2, totalPages - 1, totalPages]; else pagesToShow = [1, '...', current - 1, current, current + 1, '...', totalPages]; }
        pagesToShow.forEach(p => {
            const btn = document.createElement('button'); const isActive = p === current;
            btn.className = `btn-primary`; btn.style.padding = '5px'; btn.style.margin = '0 2px'; btn.style.maxWidth = '20px';
            if (isActive) { btn.style.background = 'var(--primary-color)'; btn.style.color = 'var(--card-bg)'; } else btn.style.background = 'none';
            btn.textContent = p;
            if (p === '...') { btn.disabled = true; btn.style.border = 'none'; btn.style.color = 'var(--text-color)'; } else { if (!isActive) btn.onclick = () => loadQuickEditData(p); }
            pageNumbers.appendChild(btn);
        });
    }
}

window.editQuickRow = function(id) {
    const row = document.getElementById(`row-${id}`); if (!row) return;
    const data = JSON.parse(row.dataset.original);
    row.querySelector('.cell-name').innerHTML = `<input type="text" id="edit-name-${id}" value="${data.Name}" style="width:80px; padding:4px;" placeholder="Name"> <input type="text" id="edit-surname-${id}" value="${data.Surname}" style="width:80px; padding:4px;" placeholder="Surname">`;
    row.querySelector('.cell-lin').innerHTML = `<input type="text" id="edit-lin-${id}" value="${data.LIN || ''}" style="padding:4px; width:70px;" placeholder="LIN">`;
    row.querySelector('.cell-gender').innerHTML = `<select id="edit-gender-${id}" style="padding:4px;"><option value="Male" ${data.Gender === 'Male' ? 'selected' : ''}>Male</option><option value="Female" ${data.Gender === 'Female' ? 'selected' : ''}>Female</option></select>`;
    let classOpts = ''; ['N1','N2','N3','P1','P2','P3','P4','P5','P6','P7','S1','S2','S3','S4','S5','S6'].forEach(c => { classOpts += `<option value="${c}" ${data.Class === c ? 'selected' : ''}>${c}</option>`; });
    row.querySelector('.cell-class').innerHTML = `<select id="edit-class-${id}" style="padding:4px; width:70px;">${classOpts}</select>`;
    let streamOpts = '<option value="">-</option>'; ['A','B','C','D','E'].forEach(s => { streamOpts += `<option value="${s}" ${data.Stream === s ? 'selected' : ''}>${s}</option>`; });
    row.querySelector('.cell-stream').innerHTML = `<select id="edit-stream-${id}" style="padding:4px;">${streamOpts}</select>`;
    row.querySelector('.cell-residence').innerHTML = `<select id="edit-residence-${id}" style="padding:4px;"><option value="Day" ${data.Residence === 'Day' ? 'selected' : ''}>Day</option><option value="Boarding" ${data.Residence === 'Boarding' ? 'selected' : ''}>Boarding</option></select>`;
    row.querySelector('.cell-entry').innerHTML = `<select id="edit-entry-${id}" style="padding:4px;"><option value="New" ${data.EntryStatus === 'New' ? 'selected' : ''}>New</option><option value="Continuing" ${data.EntryStatus === 'Continuing' ? 'selected' : ''}>Continuing</option></select>`;
    row.querySelector('.cell-contact').innerHTML = `<input type="text" id="edit-contact-${id}" value="${data.Contact || ''}" style="width:120px; padding:4px;">`;
    row.querySelector('.action-cell').innerHTML = `<div style="display:flex; gap:4px;"><button class="btn-action btn-success" onclick="confirmAction('quick_save', ${id})" style="background:var(--success-color); color:white; padding:4px 8px;"><i class="bi bi-floppy"></i></button><button class="btn-action btn-gray" onclick="confirmAction('quick_cancel', ${id})" style="background:#888; color:white; padding:4px 8px;"><i class="bi bi-x-lg"></i></button></div>`;
};

window.cancelQuickRow = function(id) {
    const row = document.getElementById(`row-${id}`); if (!row) return;
    const data = JSON.parse(row.dataset.original);
    row.querySelector('.cell-name').textContent = `${data.Name} ${data.Surname}`;
    row.querySelector('.cell-lin').innerHTML = `${data.LIN || ''}`;
    row.querySelector('.cell-gender').textContent = data.Gender;
    row.querySelector('.cell-class').textContent = data.Class;
    row.querySelector('.cell-stream').textContent = data.Stream || '';
    row.querySelector('.cell-residence').textContent = data.Residence;
    row.querySelector('.cell-entry').textContent = data.EntryStatus;
    row.querySelector('.cell-contact').textContent = data.Contact || '';
    row.querySelector('.action-cell').innerHTML = `<button class="btn-action view-btn" onclick="editQuickRow(${data.AdmissionNo})"><i class="bi bi-pencil"></i> Edit</button>`;
};

window.saveQuickRow = function(id) {
    const row = document.getElementById(`row-${id}`);
    const payload = new FormData();
    payload.append('AdmissionNo', id);
    
    const fieldMap = {
        'Name': 'name',
        'Surname': 'surname',
        'LIN': 'lin',
        'Gender': 'gender',
        'Class': 'class',
        'Stream': 'stream',
        'Residence': 'residence',
        'EntryStatus': 'entry', 
        'Contact': 'contact'
    };

    try {
        for (const [apiKey, domSuffix] of Object.entries(fieldMap)) {
            const el = document.getElementById(`edit-${domSuffix}-${id}`);
            if (el) {
                payload.append(apiKey, el.value);
            }
        }

        row.querySelector('.action-cell').innerHTML = '<i class="bi bi-arrow-repeat fa-spin" style="color:var(--primary-color);"></i>';
        fetch(`${getApiPrefix()}api/students/update_quick_edit.php`, { method: 'POST', body: payload }).then(r => r.json()).then(resp => {
            if(resp.success) {
                const oldData = JSON.parse(row.dataset.original);
                const newData = { ...oldData };
                for(let [k,v] of payload.entries()) if(k!=='AdmissionNo') newData[k]=v;
                row.dataset.original = JSON.stringify(newData);
                cancelQuickRow(id); 
                window.showCustomAlert('success', 'Saved', 'Changes saved successfully.');
            } else { 
                window.showCustomAlert('error', 'Save Failed', resp.message); 
                cancelQuickRow(id); 
                editQuickRow(id); 
            }
        }).catch(err => { 
            console.error(err); 
            window.showCustomAlert('error', 'Network Error', 'Could not save changes.'); 
            row.querySelector('.action-cell').innerHTML = `<button class="btn-action view-btn" onclick="editQuickRow(${id})"><i class="bi bi-pencil"></i> Edit</button>`; 
        });
    } catch (e) {
        console.error("Quick Edit Error:", e);
        window.showCustomAlert('error', 'System Error', 'An error occurred while preparing data.');
    }
};

// =========================================================================
// 4. ACADEMIC EDIT MODULE
// =========================================================================
let currentAcademicPage = 1;
function initAcademicEditModule() {
    const aeLevel = document.getElementById('ae-level'), aeClass = document.getElementById('ae-class'), aeForm = document.getElementById('academic-edit-filter-form');
    if (aeLevel && aeClass) { aeLevel.addEventListener('change', () => window.populateClassDropdown(aeLevel, aeClass, true)); window.populateClassDropdown(aeLevel, aeClass, true); }
    if (aeForm) { aeForm.addEventListener('submit', (e) => { e.preventDefault(); currentAcademicPage = 1; loadAcademicEditData(currentAcademicPage); }); }
    const pBtn = document.getElementById('ae-prev-btn'), nBtn = document.getElementById('ae-next-btn');
    if(pBtn) pBtn.addEventListener('click', () => {if(currentAcademicPage > 1) loadAcademicEditData(currentAcademicPage - 1);});
    if(nBtn) nBtn.addEventListener('click', () => {loadAcademicEditData(currentAcademicPage + 1);});
    if(document.getElementById('ae-table-body')) loadAcademicEditData(1);
}

window.loadAcademicEditData = function(page) {
    currentAcademicPage = page;
    const form = document.getElementById('academic-edit-filter-form'); if (!form) return;
    const limit=document.getElementById('ae-limit').value, tbody = document.getElementById('ae-table-body');
    const params = new URLSearchParams(new FormData(form)); params.append('page', page); params.append('limit', limit); 
    tbody.innerHTML = '<tr><td colspan="10" class="text-center" style="padding:20px;"><i class="bi bi-arrow-repeat fa-spin"></i> Loading Academics...</td></tr>';
    fetch(`${getApiPrefix()}api/students/fetch_academic_edit.php?${params.toString()}`).then(r => r.json()).then(data => {
        if (data.success) { renderAcademicEditTable(data); updateAcademicPagination(data); } else { tbody.innerHTML = `<tr><td colspan="10" class="text-center" style="color:red;">Error: ${data.message}</td></tr>`; }
    }).catch(err => { console.error(err); tbody.innerHTML = '<tr><td colspan="10" class="text-center" style="color:red;">Connection Error</td></tr>'; });
};

function renderAcademicEditTable(data) {
    const tbody = document.getElementById('ae-table-body'); tbody.innerHTML = '';
    if (!data.data || data.data.length === 0) { tbody.innerHTML = '<tr><td colspan="10" class="text-center" style="padding:20px;">No records found.</td></tr>'; return; }
    data.data.forEach(row => {
        const tr = document.createElement('tr'); tr.id = `ac-row-${row.AdmissionNo}`; tr.dataset.original = JSON.stringify(row);
        tr.innerHTML = `<td><strong>${row.AdmissionNo}</strong></td><td>${row.Name} ${row.Surname}</td><td class="cell-lin">${row.LIN || '-'}</td><td class="cell-stream">${row.Stream || '-'}</td><td class="cell-comb">${row.Combination || '-'}</td><td class="cell-ple-idx">${row.PLEIndexNumber || ''}</td><td class="cell-ple-agg">${row.PLEAggregate || ''}</td><td class="cell-uce-idx">${row.UCEIndexNumber || ''}</td><td class="cell-uce-res">${row.UCEResult || ''}</td><td class="action-cell"><button class="btn-action view-btn" onclick="editAcademicRow(${row.AdmissionNo})"><i class="bi bi-pencil"></i> Edit</button></td>`;
        tbody.appendChild(tr);
    });
}

function updateAcademicPagination(data) {
    const prevBtn = document.getElementById('ae-prev-btn'), nextBtn = document.getElementById('ae-next-btn'), pageNumbers = document.getElementById('ae-page-numbers'), info = document.getElementById('ae-pagination-info');
    const limit = data.limit || parseInt(document.getElementById('ae-limit').value); 
    const start = (data.page - 1) * limit + 1, end = Math.min(start + limit - 1, data.total);
    info.textContent = `Showing ${start}-${end} of ${data.total}`;
    if(prevBtn) prevBtn.disabled = data.page <= 1; if(nextBtn) nextBtn.disabled = data.page >= data.total_pages;
    if(pageNumbers) {
        pageNumbers.innerHTML = ''; const totalPages = data.total_pages, current = parseInt(data.page);
        let pagesToShow = [];
        if (totalPages <= 7) pagesToShow = Array.from({length: totalPages}, (_, i) => i + 1);
        else { if (current <= 4) pagesToShow = [1, 2, 3,'...', totalPages]; else if (current >= totalPages - 3) pagesToShow = [1, '...', totalPages - 4, totalPages - 3, totalPages - 2, totalPages - 1, totalPages]; else pagesToShow = [1, '...', current - 1, current, current + 1, '...', totalPages]; }
        pagesToShow.forEach(p => {
            const btn = document.createElement('button'); const isActive = p === current;
            btn.className = `btn-primary`; btn.style.padding = '5px'; btn.style.margin = '0 2px'; btn.style.maxWidth = '20px';
            if (isActive) { btn.style.background = 'var(--primary-color)'; btn.style.color = 'var(--card-bg)'; } else btn.style.background = 'none';
            btn.textContent = p;
            if (p === '...') { btn.disabled = true; btn.style.background = 'transparent'; btn.style.border = 'none'; } else { if (!isActive) btn.onclick = () => loadAcademicEditData(p); }
            pageNumbers.appendChild(btn);
        });
    }
}

window.editAcademicRow = function(id) {
    const row = document.getElementById(`ac-row-${id}`); if (!row) return;
    const data = JSON.parse(row.dataset.original);
    row.querySelector('.cell-lin').innerHTML = `<input type="text" id="ac-lin-${id}" value="${data.LIN || ''}" style="width:100px; padding:4px;">`;
    let strOpts = `<option value="">-</option>`; ['A','B','C','D','E'].forEach(s => strOpts += `<option value="${s}" ${data.Stream === s ? 'selected' : ''}>${s}</option>`);
    row.querySelector('.cell-stream').innerHTML = `<select id="ac-stream-${id}" style="padding:4px;">${strOpts}</select>`;
    let combOpts = `<option value="">Select</option>`; ['PEM/ICT', 'PCM/ICT', 'MEG/ICT', 'BEM/ICT', 'PCB/SUBMATH', 'BCM/ICT', 'HEG/ICT', 'DEG/ICT', 'HEL/ICT'].forEach(c => combOpts += `<option value="${c}" ${data.Combination === c ? 'selected' : ''}>${c}</option>`);
    row.querySelector('.cell-comb').innerHTML = `<select id="ac-comb-${id}" style="padding:4px; width:110px;">${combOpts}</select>`;
    row.querySelector('.cell-ple-idx').innerHTML = `<input type="text" id="ac-ple-idx-${id}" value="${data.PLEIndexNumber || ''}" style="width:120px; padding:4px;">`;
    row.querySelector('.cell-ple-agg').innerHTML = `<input type="number" id="ac-ple-agg-${id}" value="${data.PLEAggregate || ''}" style="width:50px; padding:4px;">`;
    row.querySelector('.cell-uce-idx').innerHTML = `<input type="text" id="ac-uce-idx-${id}" value="${data.UCEIndexNumber || ''}" style="width:120px; padding:4px;">`;
    row.querySelector('.cell-uce-res').innerHTML = `<input type="text" id="ac-uce-res-${id}" value="${data.UCEResult || ''}" style="width:80px; padding:4px;">`;
    row.querySelector('.action-cell').innerHTML = `<div style="display:flex; gap:4px;"><button class="btn-action btn-success" onclick="confirmAction('academic_save', ${id})" title="Save"><i class="bi bi-floppy"></i></button><button class="btn-action btn-gray" onclick="confirmAction('academic_cancel', ${id})" title="Cancel"><i class="bi bi-x-lg"></i></button></div>`;
};

window.cancelAcademicRow = function(id) {
    const row = document.getElementById(`ac-row-${id}`); if (!row) return;
    const data = JSON.parse(row.dataset.original);
    row.querySelector('.cell-lin').textContent = data.LIN || '-'; row.querySelector('.cell-stream').textContent = data.Stream || '-'; row.querySelector('.cell-comb').textContent = data.Combination || '-'; row.querySelector('.cell-ple-idx').textContent = data.PLEIndexNumber || ''; row.querySelector('.cell-ple-agg').textContent = data.PLEAggregate || ''; row.querySelector('.cell-uce-idx').textContent = data.UCEIndexNumber || ''; row.querySelector('.cell-uce-res').textContent = data.UCEResult || '';
    row.querySelector('.action-cell').innerHTML = `<button class="btn-action view-btn" onclick="editAcademicRow(${id})"><i class="bi bi-pencil"></i> Edit</button>`;
};

window.saveAcademicRow = function(id) {
    const row = document.getElementById(`ac-row-${id}`);
    const payload = new FormData();
    payload.append('AdmissionNo', id);
    ['LIN','Stream','Combination','PLEIndexNumber','PLEAggregate','UCEIndexNumber','UCEResult'].forEach(f => payload.append(f, document.getElementById(`ac-${f.toLowerCase().replace('pleindexnumber','ple-idx').replace('pleaggregate','ple-agg').replace('uceindexnumber','uce-idx').replace('uceresult','uce-res').replace('combination','comb')}-${id}`).value));
    row.querySelector('.action-cell').innerHTML = '<i class="bi bi-arrow-repeat fa-spin" style="color:var(--primary-color);"></i>';
    fetch(`${getApiPrefix()}api/students/update_academic_edit.php`, { method: 'POST', body: payload }).then(r => r.json()).then(resp => {
        if(resp.success) {
            const oldData = JSON.parse(row.dataset.original);
            const newData = { ...oldData }; for(let [k,v] of payload.entries()) if(k!=='AdmissionNo') newData[k]=v;
        row.dataset.original = JSON.stringify(newData); cancelAcademicRow(id); window.showCustomAlert('success', 'Saved', 'Academic details updated.');
        } else { window.showCustomAlert('error', 'Error', resp.message); cancelAcademicRow(id); }
    }).catch(err => { console.error(err); window.showCustomAlert('error', 'Error', 'Connection failed.'); row.querySelector('.action-cell').innerHTML = `<button class="btn-action view-btn" onclick="editAcademicRow(${id})"><i class="bi bi-pencil"></i> Edit</button>`; });
};

// =========================================================================
// 5. BULK SELECTION & INLINE EDITING LOGIC
// =========================================================================

window.toggleAllStudents = function(masterCheckbox) {
    const checkboxes = document.querySelectorAll('.student-row-checkbox');
    checkboxes.forEach(cb => cb.checked = masterCheckbox.checked);
    window.updateBulkSelection();
};

window.updateBulkSelection = function() {
    const checkboxes = document.querySelectorAll('.student-row-checkbox:checked');
    const bulkBar = document.getElementById('bulk-actions-bar');
    const countSpan = document.getElementById('selected-count');
    if (!bulkBar || !countSpan) return;
    
    if (checkboxes.length > 0) {
        bulkBar.style.display = 'flex';
        countSpan.innerText = `${checkboxes.length} selected`;
    } else {
        bulkBar.style.display = 'none';
        countSpan.innerText = '0 selected';
    }
};

window.openBulkMigrate = function() {
    const selectedIds = Array.from(document.querySelectorAll('.student-row-checkbox:checked')).map(cb => cb.value);
    if (selectedIds.length === 0) {
        window.showCustomAlert('error', 'Selection Required', 'Please select at least one student to migrate.');
        return;
    }
    // Switch to migrate module and pass selected IDs
    window.showCustomAlert('info', 'Migrate', `Opening migration UI for ${selectedIds.length} students... (UI under construction)`);
};

window.bulkArchive = function() {
    const selectedIds = Array.from(document.querySelectorAll('.student-row-checkbox:checked')).map(cb => cb.value);
    if (selectedIds.length === 0) return;
    
    window.confirmAction('bulk_archive', null, `${selectedIds.length} students`);
    // Note: executeBulkArchive needs to be implemented in base.js or here
};

// =========================================================================
// 6. PROFILE & ADMISSION LOGIC
// =========================================================================

window.initEditProfileDropdowns = function() {
    const editLevel = document.getElementById('i-level'), editClass = document.getElementById('i-class');
    if (editLevel && editClass) { const newLevel = editLevel.cloneNode(true); editLevel.parentNode.replaceChild(newLevel, editLevel); window.populateClassDropdown(newLevel, editClass, false); newLevel.addEventListener('change', () => window.populateClassDropdown(newLevel, editClass)); }
};

window.loadProfileViaAjax = function(admissionNo) {
    localStorage.setItem('currentStudentId', admissionNo); 
    const pMod = document.getElementById('profile-module');
    if (pMod) {
        if (typeof handleNavigate === 'function' && window.activeModule !== 'profile') {
            handleNavigate('profile');
        }
        pMod.style.display = 'block'; document.querySelectorAll('.module').forEach(m => { if(m.id !== 'profile-module') m.style.display = 'none'; });
        pMod.innerHTML = '<div style="text-align:center;padding:50px;"><i class="bi bi-arrow-repeat fa-spin fa-3x" style="color:var(--primary-color);"></i><br>Loading Profile...</div>';
        fetch(`${getApiPrefix()}api/students/get_student_profile.php?id=${admissionNo}`).then(r => r.text()).then(html => { pMod.innerHTML = html; if (typeof window.initEditProfileDropdowns === 'function') { window.initEditProfileDropdowns(); } }).catch(err => { console.error(err); pMod.innerHTML = '<div style="text-align:center; color:red; padding:30px;">Failed to load profile data.</div>'; });
    }
};

window.togglePageEditMode = function(mode) {
    const viewContent = document.getElementById('view-mode-content'), editForm = document.getElementById('edit-mode-form'), editBtn = document.getElementById('edit-btn'), saveCancelGroup = document.getElementById('save-cancel-group'), imgContainer = document.querySelector('.profile-image-container'); 
    if (!viewContent || !editForm) return;
    if (mode === 'edit') { viewContent.style.display = 'none'; editForm.style.display = 'block'; if(editBtn) editBtn.style.display = 'none'; if(saveCancelGroup) saveCancelGroup.style.display = 'flex'; if(imgContainer) imgContainer.classList.add('editable-mode'); } 
    else { viewContent.style.display = 'block'; editForm.style.display = 'none'; if(editBtn) editBtn.style.display = 'inline-flex'; if(saveCancelGroup) saveCancelGroup.style.display = 'none'; if(imgContainer) imgContainer.classList.remove('editable-mode'); }
};

window.triggerPhotoUpload = function() { const fileInput = document.getElementById('edit-photo-input'); if (fileInput) fileInput.click(); };

window.executeProfileUpdate = function() {
    const form = document.getElementById('edit-mode-form'); if (!form) return;
    const formData = new FormData(form); formData.append('action', 'update');
    fetch(`${getApiPrefix()}api/students/edit_student.php`, { method: 'POST', body: formData }).then(r => r.json()).then(data => {
        if (data.success) { window.showCustomAlert('success', 'Update Successful', 'Student details updated.'); const tempInput = document.getElementById('temp-photo-filename'); if (tempInput) tempInput.value = ''; const id = formData.get('AdmissionNo'); window.loadProfileViaAjax(id); } 
        else { window.showCustomAlert('error', 'Update Failed', data.message); }
    }).catch(error => { console.error(error); window.showCustomAlert('error', 'Network Error', 'Failed to connect.'); });
};

window.abortProfileEdit = function() {
    const tmpInput = document.getElementById('temp-photo-filename');
    const eForm = document.getElementById('edit-mode-form');
    
    if (tmpInput && tmpInput.value) {
        const admInput = eForm ? eForm.querySelector('input[name="AdmissionNo"]') : null;
        const admNo = admInput ? admInput.value : null;
        if(admNo) {
             const fd = new FormData();
             fd.append('action', 'delete_temp');
             fd.append('fileName', tmpInput.value);
             fd.append('AdmissionNo', admNo);
             fetch(`${getApiPrefix()}api/students/edit_student.php`, { method: 'POST', body: fd });
        }
        tmpInput.value = '';
    }

    const originalSrc = document.getElementById('original-photo-src') ? document.getElementById('original-photo-src').value : '';
    const displayPhoto = document.getElementById('display-photo');
    const formPreview = document.getElementById('form-photo-preview');
    if (originalSrc) {
        if (displayPhoto) displayPhoto.src = originalSrc;
        if (formPreview) formPreview.src = originalSrc;
    }
    const fileInput = document.getElementById('edit-photo-input');
    if (fileInput) fileInput.value = '';

    if (eForm) {
        eForm.reset(); 
        const inputs = eForm.querySelectorAll('input:not([type="hidden"]), select, textarea');
        inputs.forEach(input => {
             if (input.hasAttribute('value')) {
                 input.value = input.getAttribute('value');
             }
        });

        const lvl = document.getElementById('i-level');
        const cls = document.getElementById('i-class');
        if (lvl && cls) {
            window.populateClassDropdown(lvl, cls);
        }
    }

    window.togglePageEditMode('view');
}

window.submitEditForm = function() { const form = document.getElementById('edit-mode-form'); if (!form) return; if (!form.checkValidity()) { form.reportValidity(); return; } window.confirmAction('profile_save'); };

window.handleAdmission = function(e) {
    e.preventDefault(); const form = e.target; const formData = new FormData(form); 
    
    // Process Scholarship
    let scholarship = formData.get('scholarship');
    let moreInfo = formData.get('more_info') || '';
    if (scholarship === 'Yes') {
        moreInfo += (moreInfo ? ' | ' : '') + 'Scholarship Applied: Yes';
        formData.set('more_info', moreInfo);
    }
    
    const btn = form.querySelector('button[type="submit"]'); if(btn) { btn.disabled = true; btn.textContent = "Processing..."; }
    fetch(window.getSystemBaseUrl() + '/api/admin/students/admit', { 
        method: 'POST', 
        body: formData,
        headers: { 'Authorization': 'Bearer ' + localStorage.getItem('jwtToken') } 
    }).then(r => r.json()).then(data => {
        if(data.success) { window.showCustomAlert('success', 'Admission Complete', data.message); form.reset(); } else { window.showCustomAlert('error', 'Admission Failed', data.message); }
    }).catch(err => { window.showCustomAlert('error', 'Network Error', 'Check console.'); }).finally(() => { if(btn) { btn.disabled = false; btn.textContent = "Submit Application"; } });
};

// =========================================================================
// ADMISSION FORM CONDITIONAL EXAM FIELDS & SUBJECT ROW LOGIC
// =========================================================================

window.toggleExamFields = function(className) {
    const examContainer = document.getElementById('examContainer');
    const subjectsContainer = document.getElementById('subjectsContainer');
    const pleFields = document.querySelectorAll('.ple-field');
    const uceFields = document.querySelectorAll('.uce-field');

    if (!examContainer || !subjectsContainer) return;

    // Reset all
    examContainer.style.display = 'none';
    subjectsContainer.style.display = 'none';
    pleFields.forEach(f => f.style.display = 'none');
    uceFields.forEach(f => f.style.display = 'none');

    const lowerClass = className ? className.toLowerCase().trim() : '';

    if (lowerClass === 's1') {
        // Show PLE only
        examContainer.style.display = 'flex';
        pleFields.forEach(f => f.style.display = 'block');
        subjectsContainer.style.display = 'block';
    } else if (lowerClass === 's5') {
        // Show UCE only
        examContainer.style.display = 'flex';
        uceFields.forEach(f => f.style.display = 'block');
        subjectsContainer.style.display = 'block';
    } else if (lowerClass !== '') {
        // For all other classes, show Add Subject Pattern
        subjectsContainer.style.display = 'block';
    }
};

window.addSubjectRow = function(name = '', mark = '', grade = '') {
    const tbody = document.getElementById('subjectsBody');
    if (!tbody) return;
    const tr = document.createElement('tr');
    tr.innerHTML = `
        <td><input type="text" name="subject_name[]" value="${name}" placeholder="e.g. Mathematics" class="erp-table-input"></td>
        <td><input type="number" name="subject_mark[]" value="${mark}" placeholder="Marks" max="100" class="erp-table-input" oninput="window.calcTotalSubjectScore()"></td>
        <td><input type="text" name="subject_grade[]" value="${grade}" placeholder="Grade" class="erp-table-input"></td>
        <td class="erp-table-th-center"><button type="button" class="btn-action erp-btn-danger" onclick="this.closest('tr').remove(); window.calcTotalSubjectScore()">X</button></td>
    `;
    tbody.appendChild(tr);
    window.calcTotalSubjectScore();
};

window.calcTotalSubjectScore = function() {
    let total = 0;
    const inputs = document.querySelectorAll('input[name="subject_mark[]"]');
    inputs.forEach(inp => {
        const val = parseInt(inp.value, 10);
        if (!isNaN(val)) total += val;
    });
    const span = document.getElementById('totalSubjectScore');
    if (span) span.textContent = total;
};

// =========================================================================
// PRE-FILL ADMISSION FORM FIX (NO DOUBLE URLS)
// =========================================================================
window.autoFillAdmissionForm = function(overrideAppId) {
    const urlParams = new URLSearchParams(window.location.search);
    const appId = overrideAppId || urlParams.get('prefill_app_id');
    
    if (appId && window.activeModule === 'admission') {
        
        // --- 1. SHOW PROCESSING ANIMATION ---
        const overlay = document.createElement('div');
        overlay.id = 'admission-processing-overlay';
        overlay.innerHTML = `
            <div style="position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.4); z-index:9999; display:flex; justify-content:center; align-items:center; backdrop-filter:blur(4px);">
                <div style="background:white; padding:40px; border-radius:16px; text-align:center; box-shadow:0 10px 30px rgba(0,0,0,0.15); animation: popIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);">
                    <i class="bi bi-arrow-repeat" style="font-size:3rem; color:var(--primary-color); display:inline-block; animation: spin 1s linear infinite;"></i>
                    <h3 style="margin-top:15px; color:#333; font-weight:600;">Processing Application...</h3>
                    <p style="color:#666; margin:0;">Loading student data into admission form.</p>
                </div>
            </div>
        `;
        document.body.appendChild(overlay);

        if (!document.getElementById('anim-style')) {
            const style = document.createElement('style');
            style.id = 'anim-style';
            style.innerHTML = `@keyframes popIn { 0% { transform: scale(0.8); opacity: 0; } 100% { transform: scale(1); opacity: 1; } } @keyframes spin { 100% { transform: rotate(360deg); } }`;
            document.head.appendChild(style);
        }

        const btn = document.querySelector('#admission-form button[type="submit"]');
        if(btn) { btn.disabled = true; btn.textContent = "Loading Application Data..."; }

        fetch(window.getSystemBaseUrl() + '/api/admin/applications/' + appId, {
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('jwtToken') }
        })
        .then(r => r.json())
        .then(d => {
            if (d.success) {
                const app = d.data;
                document.getElementById('link_app_id').value = app.app_id;

                const safeSet = (name, value) => {
                    const el = document.querySelector(`#admission-form [name="${name}"]`);
                    if (el && value) {
                        if (el.tagName.toLowerCase() === 'select') {
                            const match = Array.from(el.options).find(opt => opt.value.toLowerCase() === value.toString().toLowerCase());
                            if (match) {
                                el.value = match.value;
                                el.dispatchEvent(new Event('change'));
                            }
                        } else {
                            el.value = value;
                            el.dispatchEvent(new Event('change'));
                        }
                    }
                };

                // Personal Details
                safeSet('registration_number', app.ref_number);
                safeSet('name', app.student_name);
                safeSet('middle_name', app.middle_name || app.mname);
                safeSet('surname', app.student_surname || app.surname);
                safeSet('gender', app.gender);
                safeSet('dob', app.dob);
                safeSet('nationality', app.nationality);
                
                // Address
                safeSet('postal_code', app.address_postal || app.postal || app.postal_code);
                safeSet('house_no', app.address_house || app.house || app.house_no);
                safeSet('street', app.address_street || app.street);
                safeSet('village', app.address_village || app.village);
                safeSet('district', app.address_district || app.district);
                safeSet('state', app.address_state || app.state);

                // Parents
                safeSet('father_name', app.father_name || app.f_name);
                safeSet('father_contact', app.father_contact || app.f_con);
                safeSet('father_nin', app.father_nin || app.f_nin); 
                safeSet('father_email', app.father_email || app.f_email);
                
                safeSet('mother_name', app.mother_name || app.m_name);
                safeSet('mother_contact', app.mother_contact || app.m_con);
                safeSet('mother_nin', app.mother_nin || app.m_nin);
                safeSet('mother_email', app.mother_email || app.m_email);

                safeSet('guardian_name', app.guardian_name || app.g_name);
                safeSet('guardian_contact', app.guardian_contact || app.g_con);
                safeSet('guardian_nin', app.guardian_nin || app.g_nin);
                safeSet('guardian_relation', app.guardian_relation || app.g_rel);

                // History
                safeSet('former_school', app.former_school);
                safeSet('former_school_code', app.former_school_code);
                safeSet('former_school_lin', app.former_school_lin);
                safeSet('ple_index', app.ple_ref);
                safeSet('ple_agg', app.ple_score);
                safeSet('uce_index', app.uce_ref);
                safeSet('uce_result', app.uce_score);

                safeSet('admission_year', app.academic_year || app.admission_year || app.year);
                
                // Term Mapping
                const termSelect = document.getElementById('term');
                if (termSelect && app.term) {
                    const cleanTerm = app.term.replace('Term I', 'Term 1').replace('Term II', 'Term 2').replace('Term III', 'Term 3');
                    safeSet('term', cleanTerm);
                }

                safeSet('more_info', app.more_info);
                safeSet('scholarship', app.scholarship_status || 'No');

                // Level Mapping
                const levelSelect = document.getElementById('level');
                const classSelect = document.getElementById('class');
                let appLevel = app.level || app.applied_level || '';
                const appClass = app.class_code || app.applied_class || '';

                if (!appLevel && appClass) {
                    const cleanClassForSearch = appClass.split('|')[0].trim();
                    for (const [lvl, classes] of Object.entries(classesMap)) {
                        if (classes.some(c => c.val === cleanClassForSearch)) {
                            appLevel = lvl;
                            break;
                        }
                    }
                }

                if (levelSelect && appLevel) {
                    Array.from(levelSelect.options).forEach(opt => {
                        if (opt.text.toLowerCase() === appLevel.toLowerCase() || opt.value.toLowerCase() === appLevel.toLowerCase()) {
                            levelSelect.value = opt.value;
                        }
                    });
                    
                    if (classSelect && appClass) {
                        const cleanClass = appClass.split('|')[0].trim(); 
                        classSelect.setAttribute('data-initial-value', cleanClass);
                        window.populateClassDropdown(levelSelect, classSelect);
                        
                        setTimeout(() => {
                            let matched = false;
                            Array.from(classSelect.options).forEach(opt => {
                                if (opt.value === cleanClass || opt.text === cleanClass) {
                                    classSelect.value = opt.value;
                                    matched = true;
                                }
                            });
                            if (!matched && cleanClass) {
                                classSelect.add(new Option(cleanClass, cleanClass, true, true));
                            }
                            // Trigger toggleExamFields to show correct containers
                            window.toggleExamFields(cleanClass);
                        }, 100);
                    }
                }

                // Populate subjects if they exist
                const tbody = document.getElementById('subjectsBody');
                if (tbody) {
                    tbody.innerHTML = '';
                    if (app.subject_marks && app.subject_marks.trim() !== '' && app.subject_marks !== '[]') {
                        try {
                            const marks = JSON.parse(app.subject_marks);
                            if (marks && marks.length > 0) {
                                marks.forEach(m => {
                                    let sName = m.name || m.subject || (Object.values(m).length > 0 ? Object.values(m)[0] : '');
                                    let sMark = m.mark || (Object.values(m).length > 1 ? Object.values(m)[1] : '');
                                    let sGrade = m.grade || (Object.values(m).length > 2 ? Object.values(m)[2] : '');
                                    window.addSubjectRow(sName, sMark, sGrade);
                                });
                            } else {
                                window.addSubjectRow(); // empty row
                            }
                        } catch(e) {
                            console.error("Failed to parse subject_marks JSON: ", e);
                            window.addSubjectRow();
                        }
                    } else {
                        window.addSubjectRow(); // empty row
                    }
                }

                safeSet('more_info', app.more_info);

                // --- FIX 1: THE DOUBLE URL AND DEFAULT PHOTO BUG ---
                const photoPreview = document.getElementById('admission-photo-preview');
                const hiddenPhoto = document.getElementById('existing_photo_path');
                
                if (photoPreview) {
                    let photoUrl = window.getCleanMediaUrl(app.photo_path);
                    const fallback = `https://ui-avatars.com/api/?name=${encodeURIComponent((app.student_name||'') + ' ' + (app.student_surname||''))}&background=random&color=fff`;
                    
                    // If no valid photo was provided or it's empty, use the fallback
                    if (!photoUrl || photoUrl === '' || photoUrl.includes('default_profile')) {
                        photoPreview.src = fallback;
                    } else {
                        photoPreview.src = photoUrl;
                        photoPreview.onerror = function() { this.src = fallback; };
                    }
                    if(hiddenPhoto) hiddenPhoto.value = app.photo_path || '';
                }

                // --- FIX 2: THE DOCUMENT URL BUG ---
                const docLink = document.getElementById('admission-old-doc-link');
                const noDocText = document.getElementById('no-doc-text');
                const hiddenDoc = document.getElementById('existing_doc_path');
                
                if (app.prev_marks_doc && app.prev_marks_doc.trim() !== '') {
                    let docUrl = window.getCleanMediaUrl(app.prev_marks_doc);
                    
                    if(docLink) { 
                        docLink.innerHTML = `<a href="${docUrl}" target="_blank" class="btn-primary btn-gray erp-btn-small" style="text-decoration:none; display:inline-block; margin-top:10px;"><i class="bi bi-file-earmark-pdf"></i> View Document</a>`; 
                        docLink.style.display = 'block'; 
                    }
                    if(noDocText) noDocText.style.display = 'none';
                    if(hiddenDoc) hiddenDoc.value = app.prev_marks_doc;
                } else {
                    if(docLink) { docLink.innerHTML = ''; docLink.style.display = 'none'; }
                    if(noDocText) noDocText.style.display = 'block';
                    if(hiddenDoc) hiddenDoc.value = '';
                }

                // --- 2. SHOW SUCCESS ANIMATION WITH REF ID ---
                overlay.innerHTML = `
                    <div style="position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.4); z-index:9999; display:flex; justify-content:center; align-items:center; backdrop-filter:blur(4px);">
                        <div style="background:white; padding:40px; border-radius:16px; text-align:center; box-shadow:0 10px 30px rgba(0,0,0,0.15); animation: popIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);">
                            <i class="bi bi-check-circle-fill" style="font-size:3.5rem; color:#28a745; display:inline-block;"></i>
                            <h3 style="margin-top:15px; color:#333; font-weight:600;">Data Loaded Successfully!</h3>
                            <p style="color:#666; margin:10px 0 0 0; font-size:16px;">Reference ID: <strong style="color:var(--primary-color);">${app.ref_number || app.app_id}</strong></p>
                        </div>
                    </div>
                `;
                
                // Hide success overlay after 2 seconds
                setTimeout(() => {
                    overlay.style.transition = "opacity 0.3s ease";
                    overlay.style.opacity = "0";
                    setTimeout(() => overlay.remove(), 300);
                }, 2000);

            } else {
                const overlay = document.getElementById('admission-processing-overlay');
                if(overlay) overlay.remove();
                if(btn) { btn.disabled = false; btn.textContent = "Save Admission"; }
                window.showCustomAlert('error', 'Error', 'Error loading application data: ' + d.message);
            }
        })
        .catch(error => {
            console.error(error);
            const overlay = document.getElementById('admission-processing-overlay');
            if(overlay) overlay.remove();
            if(btn) { btn.disabled = false; btn.textContent = "Save Admission"; }
            window.showCustomAlert('error', 'Error', 'Failed to connect to the server.');
        })
        .finally(() => {
            if(btn) { btn.disabled = false; btn.textContent = "Save Student Admission"; }
            window.history.replaceState({}, document.title, window.getSystemBaseUrl() + "/admin#admission");
        });
    }
};

// =========================================================================
// 6. SEARCH MODULE (Restored functionality)
// =========================================================================

function initSearchModule() {
    const searchInput = document.getElementById('global-student-search');
    const resultsContainer = document.getElementById('global-search-results');

    if (searchInput) {
        let timeout = null;
        searchInput.focus(); 

        searchInput.addEventListener('input', function(e) {
            const query = e.target.value.trim();
            clearTimeout(timeout);
            timeout = setTimeout(() => {
                if (query.length > 1) {
                    performGlobalSearch(query, resultsContainer);
                } else {
                    resultsContainer.innerHTML = '';
                }
            }, 300); 
        });
    }
}

function performGlobalSearch(query, container) {
    container.innerHTML = `<div class="search-loading-state"><i class="bi bi-arrow-repeat fa-spin search-loading-icon"></i></div>`;
    fetch(`${getApiPrefix()}api/admin/students/search?query=${encodeURIComponent(query)}`)
    .then(r => r.json())
    .then(data => {
        container.innerHTML = '';
        if (data.success && data.data.length > 0) {
            data.data.forEach(s => {
                const card = document.createElement('div');
                card.className = 'search-result-card';
                card.onclick = () => window.loadProfileViaAjax(s.AdmissionNo);
card.innerHTML = `<img src="${window.getCleanMediaUrl(s.PhotoPath)}" class="search-card-photo" alt="Student" onerror="this.src='https://ui-avatars.com/api/?name=${encodeURIComponent(s.Name + ' ' + s.Surname)}&background=random&color=fff'"><div class="search-card-info"><div class="search-card-header"><span class="highlight-label">NAME:</span> ${s.Name} ${s.Surname} | <span class="highlight-label">CLASS:</span> ${s.Class} | <span class="highlight-label">STREAM:</span> ${s.Stream || '-'} | <span class="highlight-label">TERM:</span> ${s.Term}</div><div class="search-card-details"><span class="highlight-label">PARENT:</span> ${s.ParentDisplay} | <span class="highlight-label">CONTACT:</span> ${s.ContactDisplay}</div><div class="search-card-address"><span class="highlight-label">ADDRESS:</span> ${s.FormattedAddress}</div></div>`;                container.appendChild(card);
            });
        } else {
            container.innerHTML = `<div class="search-empty-state"><p class="search-empty-text">No students found matching "${query}".</p></div>`;
        }
    })
    .catch(err => {
        console.error(err);
        container.innerHTML = `<div class="search-empty-state"><p class="search-error-text">Error loading results. Check connection.</p></div>`;
    });
}

// =========================================================================
// 7. MIGRATION MODULE (FIXED & RESTORED)
// =========================================================================

function initMigrationModule() {
    const migSourceLevel = document.getElementById('mig-source-level');
    const migSourceClass = document.getElementById('mig-source-class');
    
    // Target Inputs
    const migTargetLevel = document.getElementById('mig-target-level');
    const migTargetClass = document.getElementById('mig-target-class');
    const migTargetStream = document.getElementById('mig-target-stream');

    // Source Dropdown Logic
    if (migSourceLevel && migSourceClass) { 
        migSourceLevel.addEventListener('change', () => window.populateClassDropdown(migSourceLevel, migSourceClass)); 
    }
    
    // Target Dropdown Logic (Updated for Alumni)
    if (migTargetLevel && migTargetClass) { 
        migTargetLevel.addEventListener('change', () => {
            const isAlumni = migTargetLevel.value === 'alumni';
            
            // Find the container divs (assuming class="field") to hide them completely
            const classField = migTargetClass.closest('.field');
            const streamField = migTargetStream ? migTargetStream.closest('.field') : null;

            if (isAlumni) {
                // HIDE inputs
                if(classField) classField.style.display = 'none';
                if(streamField) streamField.style.display = 'none';
                
                // Remove Validation & Clear Values
                migTargetClass.removeAttribute('required');
                migTargetClass.value = '';
                if(migTargetStream) migTargetStream.value = '';
                
            } else {
                // SHOW inputs
                if(classField) classField.style.display = 'block';
                if(streamField) streamField.style.display = 'block';
                
                // Restore Validation
                migTargetClass.setAttribute('required', 'true');
                
                // Populate dropdown as normal
                window.populateClassDropdown(migTargetLevel, migTargetClass); 
            }
        });
    }

    // Button Listeners
    const btnFetch = document.getElementById('btn-fetch-students'); 
    if (btnFetch) btnFetch.addEventListener('click', fetchMigrationStudents);
    
    const selectAll = document.getElementById('mig-select-all'); 
    if (selectAll) { 
        selectAll.addEventListener('change', function() { 
            document.querySelectorAll('.mig-checkbox').forEach(cb => cb.checked = this.checked); 
            updateSelectionCount(); 
        }); 
    }
    
    const btnSelected = document.getElementById('btn-migrate-selected'); 
    if (btnSelected) { 
        btnSelected.addEventListener('click', () => { 
            const selected = Array.from(document.querySelectorAll('.mig-checkbox:checked')).map(cb => cb.value); 
            if (selected.length === 0) return window.showCustomAlert('warning', 'No Selection', 'Please select at least one student.'); 
            initiateMigration(selected, `${selected.length} Selected Students`); 
        }); 
    }
    
    const btnClass = document.getElementById('btn-migrate-class'); 
    if (btnClass) { 
        btnClass.addEventListener('click', () => { 
            const allCheckboxes = document.querySelectorAll('.mig-checkbox'); 
            if (allCheckboxes.length === 0) return window.showCustomAlert('warning', 'No Data', 'Fetch students first.'); 
            const allIds = Array.from(allCheckboxes).map(cb => cb.value); 
            initiateMigration(allIds, 'Entire Class'); 
        }); 
    }
}

function fetchMigrationStudents() {
    // Get values safely
    const levelEl = document.getElementById('mig-source-level');
    const classEl = document.getElementById('mig-source-class');
    const streamEl = document.getElementById('mig-source-stream');
    const yearEl = document.getElementById('mig-source-year');

    const level = levelEl ? levelEl.value : '';
    const cls = classEl ? classEl.value : '';
    const stream = streamEl ? streamEl.value : '';
    const year = yearEl ? yearEl.value : '';

    const tableBody = document.querySelector('#migration-table tbody');
    const resultsArea = document.getElementById('migration-results-area');

    // UPDATED CHECK: Only require Year to be selected
    if (!year) {
        return window.showCustomAlert('warning', 'Missing Selection', 'Please select a Source Academic Year.');
    }

    // Show loading state
    if (resultsArea) resultsArea.style.display = 'block';
    if (tableBody) tableBody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:20px;"><i class="bi bi-arrow-repeat fa-spin"></i> Loading students...</td></tr>';

    const url = `${getApiPrefix()}api/students/fetch_students_migration.php?level=${encodeURIComponent(level)}&class=${encodeURIComponent(cls)}&stream=${encodeURIComponent(stream)}&year=${encodeURIComponent(year)}`;

    fetch(url)
        .then(r => r.json())
        .then(data => {
            if (!tableBody) return;
            tableBody.innerHTML = '';

            if (data.success) {
                if (data.data.length === 0) {
                    tableBody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:20px;">No students found for this Academic Year.</td></tr>';
                    return;
                }

                data.data.forEach(s => {
                    const photo = (s.PhotoPath && s.PhotoPath.trim() !== '') 
                        ? window.getCleanMediaUrl(s.PhotoPath) 
                        : `https://ui-avatars.com/api/?name=${encodeURIComponent(s.Name + ' ' + s.Surname)}&background=random&color=fff`;
                    const row = `
                        <tr>
                            <td style="text-align:center;">
                                <input type="checkbox" class="mig-checkbox" value="${s.AdmissionNo}">
                            </td>
                            <td><img src="${photo}" class="student-photo-thumb" style="width:40px;height:40px;border-radius:50%;object-fit:cover;"></td>
                            <td>${s.Name} ${s.Surname}</td>
                            <td>${s.Class} ${s.Stream || ''}</td>
                            <td>${s.AcademicYear}</td>
                            <td>
                                <button class="btn-action view-btn mig-single-btn" data-id="${s.AdmissionNo}" data-name="${s.Name}">Move</button>
                            </td>
                        </tr>`;
                    tableBody.insertAdjacentHTML('beforeend', row);
                });

                // Re-attach listeners
                document.querySelectorAll('.mig-checkbox').forEach(cb => cb.addEventListener('change', updateSelectionCount));
                document.querySelectorAll('.mig-single-btn').forEach(btn => btn.addEventListener('click', function(e) {
                    e.preventDefault(); 
                    initiateMigration([this.dataset.id], this.dataset.name);
                }));

            } else {
                tableBody.innerHTML = `<tr><td colspan="7" style="color:red;text-align:center;">Error: ${data.message}</td></tr>`;
            }
        })
        .catch(err => {
            console.error("Fetch Error:", err);
            if (tableBody) tableBody.innerHTML = `<tr><td colspan="7" style="color:red;text-align:center;">Connection Failed. See console.</td></tr>`;
        });
}

function updateSelectionCount() {
    const count = document.querySelectorAll('.mig-checkbox:checked').length;
    const disp = document.getElementById('selection-count');
    if(disp) disp.textContent = count;
}

function initiateMigration(ids, label) {
    const tYear = document.getElementById('mig-target-year').value;
    const tLevel = document.getElementById('mig-target-level').value;
    const tClass = document.getElementById('mig-target-class').value;
    const tTerm = document.getElementById('mig-target-term').value;
    const tStream = document.getElementById('mig-target-stream').value;
    const sYear = document.getElementById('mig-source-year').value;

    // Validation Logic
    if(!tYear || !tLevel) {
        return window.showCustomAlert('warning', 'Destination?', 'Please select Target Year and Level.');
    }

    if (tLevel !== 'alumni' && !tClass) {
        return window.showCustomAlert('warning', 'Destination?', 'Please select Target Class.');
    }

    // Dynamic details for Confirmation Modal
    let targetDetails = '';
    if (tLevel === 'alumni') {
        // UPDATED: Calculate Single Completion Year (e.g., 2025-26 -> 2026)
        // Uses Source Year if available, otherwise Target Year
        const basis = sYear ? sYear : tYear;
        const startYear = parseInt(basis.split('-')[0]); // Extract "2025" from "2025-26"
        const completionYear = isNaN(startYear) ? basis : (startYear + 1);
        
        targetDetails = `Alumni / Completed (${completionYear})`;
    } else {
        targetDetails = `${tLevel} - ${tClass} ${tStream || ''} (${tYear})`;
    }

    const payload = { 
        student_ids: ids, 
        target_year: tYear, 
        source_year: sYear, 
        target_term: tTerm, 
        target_level: tLevel, 
        target_class: tClass, 
        target_stream: tStream 
    };

    window.confirmAction('migration', null, { 
        label: label, 
        targetDetails: targetDetails, 
        payload: payload 
    });
}

function executeMigration(payload) {
    fetch(`${getApiPrefix()}api/students/migrate_backend.php`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(payload)
    })
    .then(r => r.json())
    .then(data => {
        if(data.success) {
            const classDisplay = data.target_class || 'Alumni'; 
            const streamDisplay = data.target_stream ? ` ${data.target_stream}` : '';
            const successMsg = `<div class="migration-summary" style="text-align:center; background-color:#f8f9fa; padding:15px; border-radius:8px; border:1px solid #eee;"><div style="display:flex; flex-direction:column; justify-content:center; margin-bottom:10px;"><span style="color:#666; font-size:0.9em;">Total Students</span><span style="font-weight:bold; font-size:1.4em; color:#28a745;">${data.count || '0'}</span></div><div style="display:grid; grid-template-columns: 1fr 1fr; gap:10px; text-align:left; font-size:0.95em;"><div style="background:white; padding:8px; border-radius:4px; border:1px solid #ddd;"><span style="color:#888; font-size:0.8em; display:block;">New Class</span><span style="font-weight:bold; color:#333;">${classDisplay}${streamDisplay}</span></div><div style="background:white; padding:8px; border-radius:4px; border:1px solid #ddd;"><span style="color:#888; font-size:0.8em; display:block;">Academic Year</span><span style="font-weight:bold; color:#333;">${data.target_year || 'N/A'}</span></div></div></div>`;
            window.showCustomAlert('success', 'Migration Successful', successMsg);
            if(typeof fetchMigrationStudents === 'function') { fetchMigrationStudents(); }
        } else {
            window.showCustomAlert('error', 'Migration Failed', data.message);
        }
    })
    .catch(err => {
        console.error(err);
        window.showCustomAlert('error', 'Error', 'An unexpected error occurred. Check console.');
    });
}

// =========================================================================
// 8. STUDENT PHOTO EDIT MODULE (Updated with Temp Logic)
// =========================================================================
let currentPhotoPage = 1;
function initPhotoEditModule() {
    const peLevel = document.getElementById('pe-level'), peClass = document.getElementById('pe-class');
    if (peLevel && peClass) { peLevel.addEventListener('change', () => window.populateClassDropdown(peLevel, peClass, true)); window.populateClassDropdown(peLevel, peClass, true); }
    const pBtn = document.getElementById('pe-prev-btn'), nBtn = document.getElementById('pe-next-btn');
    if (pBtn) pBtn.addEventListener('click', () => { if (currentPhotoPage > 1) loadPhotoEditData(currentPhotoPage - 1); });
    if (nBtn) nBtn.addEventListener('click', () => { loadPhotoEditData(currentPhotoPage + 1); });
    if (document.getElementById('pe-table-body')) { loadPhotoEditData(1); }
}

window.loadPhotoEditData = function(page) {
    currentPhotoPage = page; const form = document.getElementById('student-photo-edit'), tbody = document.getElementById('pe-table-body'); if (!form || !tbody) return;
    const limit = document.getElementById('pe-limit').value, params = new URLSearchParams(new FormData(form)); params.append('page', page); params.append('limit', limit);
    tbody.innerHTML = '<tr><td colspan="7" class="text-center" style="padding:20px;"><i class="bi bi-arrow-repeat fa-spin"></i> Loading Photos...</td></tr>';
    fetch(`${getApiPrefix()}api/students/fetch_photo_edit.php?${params.toString()}`).then(r => r.json()).then(data => { if (data.success) { renderPhotoTable(data); updatePhotoPagination(data); } else { tbody.innerHTML = `<tr><td colspan="7" class="text-center error-text">Error: ${data.message}</td></tr>`; } }).catch(err => { console.error(err); tbody.innerHTML = '<tr><td colspan="7" class="text-center error-text">Connection Error</td></tr>'; });
};

function renderPhotoTable(data) {
    const tbody = document.getElementById('pe-table-body');
    tbody.innerHTML = '';
    if (!data.data || data.data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center">No students found.</td></tr>';
        return;
    }

    data.data.forEach(row => {
        const photoSrc = (row.PhotoPath && row.PhotoPath.trim() !== '') 
            ? window.getCleanMediaUrl(row.PhotoPath) 
            : `https://ui-avatars.com/api/?name=${encodeURIComponent(row.Name + ' ' + row.Surname)}&background=random&color=fff`;
        const tr = document.createElement('tr');
        tr.id = `pe-row-${row.AdmissionNo}`;

        tr.innerHTML = `
            <td><strong>${row.AdmissionNo}</strong></td>
            <td>${row.username || '-'}</td>
            <td>${row.Name} ${row.Surname}</td>
            <td>${row.Class}</td>
            <td>${row.Stream || '-'}</td>
            <td class="photo-cell">
                <img src="${photoSrc}" id="pe-img-${row.AdmissionNo}" style="width:80px; height:80px; object-fit:cover; border-radius:50%; border:1px solid #ddd; align-self:center;">
            </td>
            <td class="action-cell">
                <button class="btn-action view-btn" onclick="editPhotoRow(${row.AdmissionNo})">
                    <i class="bi bi-pencil"></i> Change
                </button>
            </td>`;

        tbody.appendChild(tr);
    });
}


function updatePhotoPagination(data) {
    const info = document.getElementById('pe-pagination-info'), prevBtn = document.getElementById('pe-prev-btn'), nextBtn = document.getElementById('pe-next-btn'), pageNumbers = document.getElementById('pe-page-numbers');
    const start = (data.page - 1) * data.limit + 1, end = Math.min(start + data.limit - 1, data.total);
    if(info) info.textContent = `Showing ${start}-${end} of ${data.total}`;
    if(prevBtn) prevBtn.disabled = data.page <= 1; if(nextBtn) nextBtn.disabled = data.page >= data.total_pages;
    if(pageNumbers) {
        pageNumbers.innerHTML = ''; const totalPages = data.total_pages, current = parseInt(data.page);
        let pagesToShow = []; if (totalPages <= 7) pagesToShow = Array.from({length: totalPages}, (_, i) => i + 1); else { if (current <= 4) pagesToShow = [1, 2, 3,'...', totalPages]; else if (current >= totalPages - 3) pagesToShow = [1, '...', totalPages - 4, totalPages - 3, totalPages - 2, totalPages - 1, totalPages]; else pagesToShow = [1, '...', current - 1, current, current + 1, '...', totalPages]; }
        pagesToShow.forEach(p => {
            const btn = document.createElement('button'); const isActive = p === current; btn.className = `btn-primary`; btn.style.maxWidth = '35px'; btn.style.padding = '5px'; btn.style.boxShadow = 'none'; btn.style.margin = '0 2px'; 
            if (isActive) { btn.style.background = 'var(--primary-color)'; btn.style.color = 'var(--card-bg)'; } else { btn.style.background = 'none'; btn.style.color = 'var(--text-color)'; }
            btn.textContent = p; if (p === '...') { btn.disabled = true; btn.style.background = 'transparent'; btn.style.border = 'none'; } else { if (!isActive) { btn.onclick = () => loadPhotoEditData(p); } }
            pageNumbers.appendChild(btn);
        });
    }
}

// --- PHOTO EDITING ACTIONS ---

window.editPhotoRow = function(id) {
    const row = document.getElementById(`pe-row-${id}`), actionCell = row.querySelector('.action-cell'), img = document.getElementById(`pe-img-${id}`);
    row.dataset.originalSrc = img.src;
    actionCell.innerHTML = `<div style="display:flex; gap:5px; align-items:center;"><input type="file" id="pe-input-${id}" accept="image/*" style="width:180px; font-size:0.8em;" onchange="uploadTempPhoto(${id})"><button class="btn-action btn-success" id="pe-confirm-${id}" onclick="savePhotoRow(${id})" title="Confirm Save" disabled><i class="bi bi-check-lg"></i></button><button class="btn-action btn-gray" onclick="cancelPhotoRow(${id})" title="Cancel"><i class="bi bi-x-lg"></i></button></div>`;
};

window.cancelPhotoRow = function(id) {
    const row = document.getElementById(`pe-row-${id}`), img = document.getElementById(`pe-img-${id}`), input = document.getElementById(`pe-input-${id}`);
    if (input && input.dataset.tempFile) { window.deleteTempFile(id, input.dataset.tempFile); delete input.dataset.tempFile; }
    if (row.dataset.originalSrc) { img.src = row.dataset.originalSrc; }
    row.querySelector('.action-cell').innerHTML = `<button class="btn-action view-btn" onclick="editPhotoRow(${id})"><i class="bi bi-pencil"></i> Change</button>`;
};

window.deleteTempFile = function(admissionNo, fileName) {
    if (!admissionNo || !fileName) return;
    const formData = new FormData(); formData.append('action', 'delete_temp'); formData.append('AdmissionNo', admissionNo); formData.append('fileName', fileName);
    fetch(`${getApiPrefix()}api/students/edit_student.php`, { method: 'POST', body: formData }).then(r => r.json()).then(data => { if(data.success) console.log("Temp file cleaned up:", fileName); }).catch(err => console.error("Cleanup failed:", err));
};

window.uploadTempPhoto = function(id) {
    const input = document.getElementById(`pe-input-${id}`), confirmBtn = document.getElementById(`pe-confirm-${id}`), img = document.getElementById(`pe-img-${id}`);
    if (!input.files[0]) return;
    if (input.dataset.tempFile) { window.deleteTempFile(id, input.dataset.tempFile); }
    img.style.opacity = '0.5';
    const formData = new FormData(); formData.append('AdmissionNo', id); formData.append('photo', input.files[0]);
    fetch(`${getApiPrefix()}api/students/upload_temp.php`, { method: 'POST', body: formData }).then(r => r.json()).then(data => {
        img.style.opacity = '1';
        if (data.success) { img.src = data.previewUrl + '?t=' + new Date().getTime(); input.dataset.tempFile = data.tempFileName; confirmBtn.disabled = false; confirmBtn.classList.add('pulse-animation'); } 
        else { window.showCustomAlert('error', 'Preview Error', data.message); input.value = ''; }
    }).catch(err => { console.error(err); img.style.opacity = '1'; window.showCustomAlert('error', 'Network Error', 'Could not upload preview.'); });
};

window.savePhotoRow = function(id) {
    const input = document.getElementById(`pe-input-${id}`);
    if (!input.dataset.tempFile) { window.showCustomAlert('warning', 'Wait', 'Please wait for the photo preview to load.'); return; }
    window.confirmAction('photo_save', id);
};

window.executePhotoUpload = function(id) {
    const row = document.getElementById(`pe-row-${id}`), actionCell = row.querySelector('.action-cell'), input = document.getElementById(`pe-input-${id}`), tempFile = input.dataset.tempFile, originalContent = actionCell.innerHTML;
    actionCell.innerHTML = '<i class="bi bi-arrow-repeat fa-spin" style="color:var(--primary-color);"></i> Saving...';
    const formData = new FormData(); formData.append('AdmissionNo', id); formData.append('temp_photo_filename', tempFile);
    fetch(`${getApiPrefix()}api/students/update_student_photo.php`, { method: 'POST', body: formData }).then(r => r.json()).then(data => {
        if (data.success) {
            const img = document.getElementById(`pe-img-${id}`); if(img) { img.src = data.newPath + '?t=' + new Date().getTime(); const row = document.getElementById(`pe-row-${id}`); if(row) row.dataset.originalSrc = img.src; }
            if(input) delete input.dataset.tempFile; 
            cancelPhotoRow(id);
            window.showCustomAlert('success', 'Saved', 'Student photo updated successfully.');
        } else { window.showCustomAlert('error', 'Save Failed', data.message); actionCell.innerHTML = originalContent; }
    }).catch(err => { console.error(err); window.showCustomAlert('error', 'Connection Error', 'Failed to save changes.'); actionCell.innerHTML = originalContent; });
};

// =========================================================================
// 9. STUDENT ACCOUNTS MODULE
// =========================================================================
let curAccPage = 1;

function initAccountsModule() {
    const lvl = document.getElementById('acc-level');
    const cls = document.getElementById('acc-class');
    const form = document.getElementById('accounts-filter-form');

    if (lvl && cls) {
        lvl.addEventListener('change', () => window.populateClassDropdown(lvl, cls, true));
    }

    if (form) {
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            curAccPage = 1;
            loadAccountsData(1);
        });
    }

    const prev = document.getElementById('acc-prev-btn');
    const next = document.getElementById('acc-next-btn');
    if (prev) prev.addEventListener('click', () => { if (curAccPage > 1) loadAccountsData(--curAccPage); });
    if (next) next.addEventListener('click', () => { loadAccountsData(++curAccPage); });

    if (document.getElementById('accounts-table-body')) {
        loadAccountsData(1);
    }
}

window.loadAccountsData = function(page) {
    curAccPage = page;
    const tbody = document.getElementById('accounts-table-body');
    const form = document.getElementById('accounts-filter-form');
    if (!tbody || !form) return;

    tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; padding:30px;"><i class="bi bi-arrow-repeat fa-spin"></i> Loading...</td></tr>';

    const params = new URLSearchParams(new FormData(form));
    params.append('page', page);
    const limit = document.getElementById('acc-limit').value;
    params.append('limit', limit);

    fetch(`${getApiPrefix()}api/students/fetch_student_accounts.php?${params.toString()}`)
        .then(r => r.json())
        .then(res => {
            if (res.success) {
                renderAccountsRows(res.data);
                updateAccPagination(res);
            } else {
                tbody.innerHTML = '<tr><td colspan="8" class="text-center">Error loading data</td></tr>';
            }
        })
        .catch(err => { console.error(err); tbody.innerHTML = '<tr><td colspan="8" class="text-center">Connection Error</td></tr>'; });
};
function renderAccountsRows(data) {
    const tbody = document.getElementById('accounts-table-body');
    tbody.innerHTML = '';
    if (data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center">No records found</td></tr>';
        return;
    }
    data.forEach(row => {
        const tr = document.createElement('tr');
        const passVal = row.Password || '';
        
        // UPDATED: Added style="max-width: 100px;" to the input
        const passDisplay = passVal ? `
            <div class="pwd-wrapper">
                <input type="password" class="pwd-input" value="${passVal}" readonly id="pwd-${row.AdmissionNo}" style="max-width: 100px;">
                <i class="bi bi-eye pwd-toggle" onclick="togglePwdVisibility('pwd-${row.AdmissionNo}')" title="Show/Hide"></i>
            </div>` : '<span style="color:#999; font-size:0.9em;">Not Set</span>';
            
        const userDisplay = row.Username ? row.Username : '<span style="color:#999; font-style:italic;">Empty</span>';
        const checked = row.IsActive == 1 ? 'checked' : '';
        const toggleSwitch = `
            <label class="toggle-switch">
                <input type="checkbox" ${checked} onchange="toggleAccStatus(${row.AdmissionNo}, this)">
                <span class="slider"></span>
            </label>`;
        
        const safeName = row.FullName.replace(/'/g, "\\'");
        const safeDetails = `${row.Class} ${row.Stream || ''}`.trim();
        
        tr.innerHTML = `
            <td><strong>${row.AdmissionNo}</strong></td>
            <td>${row.FullName}</td>
           <td>${row.Class} / ${row.Stream || '-'}</td>
            <td>${userDisplay}</td>
            <td>${passDisplay}</td>
            <td><small>${row.LoginStatus}</small></td>
            <td>
                <button class="btn-action btn-sm" style="background:var(--primary-color); color:#fff; border:none; padding:4px 8px; border-radius:4px; cursor:pointer;" onclick="promptPasswordChange(${row.AdmissionNo}, '${safeName}', '${safeDetails}')">
                    <i class="bi bi-pencil"></i> Edit Pwd
                </button>
            </td>
            <td>${toggleSwitch}</td>
        `;
        tbody.appendChild(tr);
    });
}
window.promptPasswordChange = function(id, name, details) {
    let modal = document.getElementById('pwd-change-modal');
    if (!modal) {
        modal = document.createElement('div');
        modal.id = 'pwd-change-modal';
        modal.className = 'modal-backdrop';
        document.body.appendChild(modal);
    }
    
    // FIX: Updated HTML to show Name and Class details
    modal.innerHTML = `
        <div class="modal-content confirm-modal animated-pop">
            <h4 class="modal-title">Change Password</h4>
            <div class="modal-message">
                <p style="margin-bottom:5px; color:#555;">Editing: <strong>${name}</strong></p>
                <p style="margin-bottom:15px; font-size:0.9em; color:#777;">(${details || 'No Class'})</p>
                
                <input type="text" id="new-pwd-input" class="form-control" 
                       style="max-width:100%; padding:10px; border:1px solid #ddd; border-radius:4px; font-size:1.1rem; text-align:center;" 
                       placeholder="Enter New Password" autocomplete="off">
            </div>
            <div class="modal-actions centered" style="gap:15px; display:flex; justify-content:center;">
                <button class="btn-primary" id="pwd-modal-cancel" style="background:var(--secondary-color); color:var(--text-color-inverse);">Cancel</button>
                <button class="btn-primary" id="pwd-modal-save" style="background:var(--success-color);">Update Password</button>
            </div>
        </div>`;
    
    const close = () => modal.classList.remove('show');
    const save = () => {
        const val = document.getElementById('new-pwd-input').value.trim();
        if(!val) { window.showCustomAlert('warning', 'Required', 'Please enter a password'); return; }
        close();
        // Confirmation now includes the student's name
        window.showActionConfirm(
            'Confirm Change', 
            `Update password for <strong>${name}</strong> to: <br><span style="font-size:1.2em; color:var(--primary-color); font-weight:bold;">${val}</span>?`, 
            'Yes, Update', 
            'var(--success-color)', 
            () => executePasswordUpdate(id, val)
        );
    };

    modal.querySelector('#pwd-modal-cancel').onclick = close;
    modal.querySelector('#pwd-modal-save').onclick = save;
    setTimeout(() => {
        modal.classList.add('show');
        document.getElementById('new-pwd-input').focus();
    }, 10);
};
window.executePasswordUpdate = function(id, newPwd) {
    const fd = new FormData();
    fd.append('action', 'update_password'); // New action type
    fd.append('admission_no', id);
    fd.append('new_password', newPwd);
    
    fetch(`${getApiPrefix()}api/students/manage_account.php`, { method: 'POST', body: fd })
        .then(r => r.json())
        .then(res => {
            if (res.success) {
                window.showCustomAlert('success', 'Updated', res.message);
                loadAccountsData(curAccPage);
            } else {
                window.showCustomAlert('error', 'Error', res.message);
            }
        })
        .catch(err => {
            console.error(err);
            window.showCustomAlert('error', 'Error', 'Connection failed.');
        });
};
window.togglePwdVisibility = function(id) {
    const input = document.getElementById(id);
    if (input.type === "password") input.type = "text";
    else input.type = "password";
};

window.resetAccPassword = function(id) {
    window.showActionConfirm('Reset Password?', 'This will set a default password and generate a username.', 'Yes, Reset', 'var(--warning-color)', () => {
        const fd = new FormData();
        fd.append('action', 'reset_password');
        fd.append('admission_no', id);
        
        fetch(`${getApiPrefix()}api/students/manage_account.php`, { method: 'POST', body: fd })
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    window.showCustomAlert('success', 'Reset Complete', res.message);
                    loadAccountsData(curAccPage);
                } else {
                    window.showCustomAlert('error', 'Error', res.message);
                }
            });
    });
};

window.toggleAccStatus = function(id, checkbox) {
    const status = checkbox.checked ? 1 : 0;
    const fd = new FormData();
    fd.append('action', 'toggle_status');
    fd.append('admission_no', id);
    fd.append('status', status);
    fetch(`${getApiPrefix()}api/students/manage_account.php`, { method: 'POST', body: fd })
        .then(r => r.json())
        .then(res => {
            if (!res.success) {
                checkbox.checked = !checkbox.checked; 
                window.showCustomAlert('error', 'Error', 'Failed to update status.');
            }
        });
};

function updateAccPagination(res) {
    const info = document.getElementById('acc-pagination-info');
    const pBtn = document.getElementById('acc-prev-btn');
    const nBtn = document.getElementById('acc-next-btn');
    const pageNumbers = document.getElementById('acc-page-numbers'); 
    const limitInput = document.getElementById('acc-limit');
    const limit = limitInput ? parseInt(limitInput.value) : 25;
    const start = (res.page - 1) * limit + 1;
    const end = Math.min(start + res.data.length - 1, res.total);
    if (info) info.textContent = `Showing ${res.total > 0 ? start : 0}-${end} of ${res.total}`;
    if (pBtn) pBtn.disabled = res.page <= 1;
    if (nBtn) nBtn.disabled = res.page >= res.total_pages;

    if (pageNumbers) {
        pageNumbers.innerHTML = ''; 
        const totalPages = res.total_pages;
        const current = parseInt(res.page);
        let pagesToShow = [];
        if (totalPages <= 7) { pagesToShow = Array.from({length: totalPages}, (_, i) => i + 1); } 
        else {
            if (current <= 4) { pagesToShow = [1, 2, 3, '...', totalPages]; } 
            else if (current >= totalPages - 3) { pagesToShow = [1, '...', totalPages - 4, totalPages - 3, totalPages - 2, totalPages - 1, totalPages]; } 
            else { pagesToShow = [1, '...', current - 1, current, current + 1, '...', totalPages]; }
        }
        pagesToShow.forEach(p => {
            const btn = document.createElement('button');
            const isActive = p === current;
            btn.className = `btn-primary`; btn.style.padding = '5px'; btn.style.margin = '0 2px'; btn.style.boxShadow='none'; btn.style.maxWidth = '20px';
            if (isActive) { btn.style.background = 'var(--primary-color)'; btn.style.color = 'var(--card-bg)'; } 
            else { btn.style.background = 'transparent'; btn.style.color = 'var(--text-color)'; }
            btn.textContent = p;
            if (p === '...') { btn.disabled = true; } 
            else { if (!isActive) { btn.onclick = () => loadAccountsData(p); } }
            pageNumbers.appendChild(btn);
        });
    }
}
// =========================================================================
// 11. ALUMNI DEBTS MODULE
// =========================================================================
let curDebtPage = 1;

function initAlumniDebtsModule() {
    const form = document.getElementById('debts-filter-form');
    if (!form) return;

    form.addEventListener('submit', (e) => {
        e.preventDefault();
        curDebtPage = 1;
        loadAlumniDebtsData(1);
    });

    const prev = document.getElementById('debts-prev-btn');
    const next = document.getElementById('debts-next-btn');
    if (prev) prev.addEventListener('click', () => { if (curDebtPage > 1) loadAlumniDebtsData(--curDebtPage); });
    if (next) next.addEventListener('click', () => { loadAlumniDebtsData(++curDebtPage); });

    loadAlumniDebtsData(1);
}

window.loadAlumniDebtsData = function(page) {
    curDebtPage = page;
    const tbody = document.getElementById('debts-table-body');
    const form = document.getElementById('debts-filter-form');
    if (!tbody || !form) return;

    tbody.innerHTML = '<tr><td colspan="9" style="text-align:center; padding:30px;"><i class="bi bi-arrow-repeat fa-spin"></i> Loading...</td></tr>';

    const params = new URLSearchParams(new FormData(form));
    params.append('page', page);
    params.append('limit', document.getElementById('debts-limit').value);

    fetch(`${getApiPrefix()}api/students/fetch_alumni_debts.php?${params.toString()}`)
        .then(r => r.json())
        .then(res => {
            if (res.success) {
                renderDebtRows(res.data);
                updateDebtPagination(res);
            } else {
                tbody.innerHTML = '<tr><td colspan="9" class="text-center">Error loading data</td></tr>';
            }
        })
        .catch(err => {
            console.error(err);
            tbody.innerHTML = '<tr><td colspan="9" class="text-center">Connection Error</td></tr>';
        });
};

function renderDebtRows(data) {
    const tbody = document.getElementById('debts-table-body');
    tbody.innerHTML = '';
    
    if (data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="text-center">No alumni records found.</td></tr>';
        return;
    }

    data.forEach(row => {
        const tr = document.createElement('tr');
        
        const fmt = (val) => (parseFloat(val) > 0) ? new Intl.NumberFormat('en-UG').format(val) : '-';
        
        tr.innerHTML = `
            <td><strong>${row.AdmissionNo}</strong></td>
            <td>${row.FullName}</td>
            <td>${row.CompletionYear}</td>
            <td>${row.ParentName || '-'}</td>
            <td>${row.Contact || '-'}</td>
            <td>${fmt(row.TotalAmount)}</td>
            <td>${fmt(row.AmountPaid)}</td>
            <td>${fmt(row.Balance)}</td>
            <td style="text-align:center;">
                <div class="action-buttons" style="display:flex; gap:5px;">
                    <button class="btn-action btn-success" onclick="openPayModal(${row.AdmissionNo})" title="Pay">
                        Pay
                    </button>
                    <button class="btn-action btn-primary" style="max-width: 50px;" onclick="editDebt(${row.AdmissionNo})" title="Edit">
                        Edit
                    </button>
                    <button class="btn-action btn-danger" onclick="deleteDebt(${row.AdmissionNo})" title="Delete">Delete
                    </button>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function updateDebtPagination(res) {
    const info = document.getElementById('debts-pagination-info');
    const pBtn = document.getElementById('debts-prev-btn');
    const nBtn = document.getElementById('debts-next-btn');
    const pageNumbers = document.getElementById('debts-page-numbers'); 
    
    const limit = parseInt(document.getElementById('debts-limit').value);
    const start = (res.page - 1) * limit + 1;
    const end = Math.min(start + res.data.length - 1, res.total);
    
    if (info) info.textContent = `Showing ${res.total > 0 ? start : 0}-${end} of ${res.total}`;
    if (pBtn) pBtn.disabled = res.page <= 1;
    if (nBtn) nBtn.disabled = res.page >= res.total_pages;

    if (pageNumbers) {
        pageNumbers.innerHTML = ''; 
        const totalPages = res.total_pages;
        const current = parseInt(res.page);
        let pagesToShow = [];
        
        if (totalPages <= 7) { pagesToShow = Array.from({length: totalPages}, (_, i) => i + 1); } 
        else {
            if (current <= 4) { pagesToShow = [1, 2, 3, '...', totalPages]; } 
            else if (current >= totalPages - 3) { pagesToShow = [1, '...', totalPages - 4, totalPages - 3, totalPages - 2, totalPages - 1, totalPages]; } 
            else { pagesToShow = [1, '...', current - 1, current, current + 1, '...', totalPages]; }
        }
        
        pagesToShow.forEach(p => {
            const btn = document.createElement('button');
            const isActive = p === current;
            btn.className = `btn-primary`; 
            btn.style.padding = '5px'; btn.style.margin = '0 2px'; btn.style.maxWidth = '25px';
            
            if (isActive) { btn.style.background = 'var(--primary-color)'; btn.style.color = '#fff'; } 
            else { btn.style.background = 'transparent'; btn.style.color = 'var(--text-color)'; }
            
            btn.textContent = p;
            
            if (p !== '...' && !isActive) { 
                btn.onclick = () => loadAlumniDebtsData(p); 
            } else if (p === '...') {
                btn.disabled = true;
            }
            pageNumbers.appendChild(btn);
        });
    }
}

// Action Placeholders
window.openPayModal = function(id) { window.showCustomAlert('info', 'Pay Debt', 'Payment module pending for ID: ' + id); };
window.editDebt = function(id) { window.showCustomAlert('info', 'Edit Debt', 'Edit module pending for ID: ' + id); };
window.deleteDebt = function(id) { window.confirmAction('delete_student', id, 'Student from Alumni'); };

  // =========================================================================
// 12. ALUMNI RECEIPTS MODULE
// =========================================================================
let curReceiptPage = 1;

function initAlumniReceiptsModule() {
    const form = document.getElementById('receipts-filter-form');
    if (!form) return;

    form.addEventListener('submit', (e) => {
        e.preventDefault();
        curReceiptPage = 1;
        loadAlumniReceiptsData(1);
    });

    const prev = document.getElementById('receipts-prev-btn');
    const next = document.getElementById('receipts-next-btn');
    if (prev) prev.addEventListener('click', () => { if (curReceiptPage > 1) loadAlumniReceiptsData(--curReceiptPage); });
    if (next) next.addEventListener('click', () => { loadAlumniReceiptsData(++curReceiptPage); });

    loadAlumniReceiptsData(1);
}

window.loadAlumniReceiptsData = function(page) {
    curReceiptPage = page;
    const tbody = document.getElementById('receipts-table-body');
    const form = document.getElementById('receipts-filter-form');
    if (!tbody || !form) return;

    tbody.innerHTML = '<tr><td colspan="9" style="text-align:center; padding:30px;"><i class="bi bi-arrow-repeat fa-spin"></i> Loading...</td></tr>';

    const params = new URLSearchParams(new FormData(form));
    params.append('page', page);
    params.append('limit', document.getElementById('receipts-limit').value);

    fetch(`${getApiPrefix()}api/students/fetch_alumni_receipts.php?${params.toString()}`)
        .then(r => r.json())
        .then(res => {
            if (res.success) {
                renderReceiptRows(res.data);
                updateReceiptPagination(res);
            } else {
                tbody.innerHTML = '<tr><td colspan="9" class="text-center">Error loading data</td></tr>';
            }
        })
        .catch(err => {
            console.error(err);
            tbody.innerHTML = '<tr><td colspan="9" class="text-center">Connection Error</td></tr>';
        });
};

function renderReceiptRows(data) {
    const tbody = document.getElementById('receipts-table-body');
    tbody.innerHTML = '';
    
    if (data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="text-center">No receipts found.</td></tr>';
        return;
    }

    data.forEach(row => {
        const tr = document.createElement('tr');
        // Check if amount is actually present before formatting
        const fmtAmount = (row.AmountPaid > 0) ? new Intl.NumberFormat('en-UG').format(row.AmountPaid) : '-';
        
        tr.innerHTML = `
            <td><strong>${row.AdmissionNo}</strong></td>
            <td>${row.FullName}</td>
            <td>${row.CompletionYear}</td>
            <td style="font-weight:bold; color:var(--success-color);">${fmtAmount}</td>
            <td>${row.PaymentDate}</td>
            <td>${row.ReceiptNo}</td>
            <td>${row.PaidBy}</td>
            <td>${row.PaymentMode}</td>
            <td style="text-align:center;">
                <div class="action-buttons" style="display:flex; justify-content:center; gap:5px;">
                    <button class="btn-action btn-primary" style="max-width:50px;" onclick="printReceipt(${row.AdmissionNo})" title="Print">
                        Print
                    </button>
                    <button class="btn-action btn-danger" onclick="deleteReceipt(${row.AdmissionNo})" title="Delete">
                        Delete
                    </button>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function updateReceiptPagination(res) {
    const info = document.getElementById('receipts-pagination-info');
    const pBtn = document.getElementById('receipts-prev-btn');
    const nBtn = document.getElementById('receipts-next-btn');
    const pageNumbers = document.getElementById('receipts-page-numbers'); 
    
    const limit = parseInt(document.getElementById('receipts-limit').value);
    const start = (res.page - 1) * limit + 1;
    const end = Math.min(start + res.data.length - 1, res.total);
    
    if (info) info.textContent = `Showing ${res.total > 0 ? start : 0}-${end} of ${res.total}`;
    if (pBtn) pBtn.disabled = res.page <= 1;
    if (nBtn) nBtn.disabled = res.page >= res.total_pages;

    if (pageNumbers) {
        pageNumbers.innerHTML = ''; 
        const totalPages = res.total_pages;
        const current = parseInt(res.page);
        let pagesToShow = [];
        
        if (totalPages <= 7) { pagesToShow = Array.from({length: totalPages}, (_, i) => i + 1); } 
        else {
            if (current <= 4) { pagesToShow = [1, 2, 3, '...', totalPages]; } 
            else if (current >= totalPages - 3) { pagesToShow = [1, '...', totalPages - 4, totalPages - 3, totalPages - 2, totalPages - 1, totalPages]; } 
            else { pagesToShow = [1, '...', current - 1, current, current + 1, '...', totalPages]; }
        }
        
        pagesToShow.forEach(p => {
            const btn = document.createElement('button');
            const isActive = p === current;
            btn.className = `btn-primary`; 
            btn.style.padding = '5px'; btn.style.margin = '0 2px'; btn.style.maxWidth = '25px';
            
            if (isActive) { btn.style.background = 'var(--primary-color)'; btn.style.color = '#fff'; } 
            else { btn.style.background = 'transparent'; btn.style.color = 'var(--text-color)'; }
            
            btn.textContent = p;
            
            if (p !== '...' && !isActive) { btn.onclick = () => loadAlumniReceiptsData(p); } 
            else if (p === '...') { btn.disabled = true; }
            pageNumbers.appendChild(btn);
        });
    }
}

// Placeholder Actions
window.printReceipt = function(id) { window.showCustomAlert('info', 'Print', 'Receipt printing coming soon for ID: ' + id); };
window.deleteReceipt = function(id) { window.confirmAction('delete_student', id, 'this receipt'); };



// =========================================================================
// 10. EVENT LISTENERS & INIT
// =========================================================================

window.initAdmissionForm = () => {
    const admissionForm = document.getElementById('admission-form');
    if (admissionForm) { admissionForm.addEventListener('submit', window.handleAdmission); admissionForm.addEventListener('reset', () => { setTimeout(() => { const previewImg = document.getElementById('admission-photo-preview'), photoWrapper = document.getElementById('admission-photo-wrapper'), placeholder = document.getElementById('upload-placeholder'); if (previewImg) { previewImg.src = ''; previewImg.style.display = 'none'; } if (photoWrapper) photoWrapper.classList.remove('has-file'); if (placeholder) placeholder.style.display = 'flex'; const pInput = document.getElementById('photo'); if(pInput) pInput.value = ''; }, 50); }); }
    const resetAdmBtn = document.getElementById('admission-reset-btn'); if(resetAdmBtn) resetAdmBtn.addEventListener('click', () => { if(admissionForm) admissionForm.reset(); });
    const admLevel = document.getElementById('level'), admClass = document.getElementById('class'), admStream = document.getElementById('stream'); 
    if (admLevel) {
        window.populateLevelDropdown(admLevel, false).then(() => {
            if (admClass) window.populateClassDropdown(admLevel, admClass);
        });
        admLevel.addEventListener('change', () => { 
            if (admClass) window.populateClassDropdown(admLevel, admClass);
            if (admStream) window.populateStreamDropdown(admClass, admStream, false);
        });
    }
    if (admClass && admStream) {
        admClass.addEventListener('change', () => window.populateStreamDropdown(admClass, admStream, false));
    }
    const photoInput = document.getElementById('photo'); if (photoInput) { photoInput.addEventListener('change', function(e) { const file = e.target.files[0], previewImg = document.getElementById('admission-photo-preview'), wrapper = document.getElementById('admission-photo-wrapper'), placeholder = document.getElementById('upload-placeholder'); if (file && previewImg) { const reader = new FileReader(); reader.onload = function(evt) { previewImg.src = evt.target.result; previewImg.style.display = 'block'; if(wrapper) wrapper.classList.add('has-file'); if(placeholder) placeholder.style.display = 'none'; }; reader.readAsDataURL(file); } }); }
    
    // Scholarship listener logic
    const scholarshipDropdown = document.getElementById('scholarship');
    const scholarshipDetailsContainer = document.getElementById('scholarship-details-container');
    if (scholarshipDropdown && scholarshipDetailsContainer) {
        scholarshipDropdown.addEventListener('change', (e) => {
            if (e.target.value === 'Applied') {
                scholarshipDetailsContainer.style.display = 'block';
            } else {
                scholarshipDetailsContainer.style.display = 'none';
            }
        });
    }
};
window.initStudentListForm = function() {
    const filterForm = document.getElementById('student-filter-form'); 
    if (filterForm) filterForm.addEventListener('submit', (e) => { e.preventDefault(); window.loadStudentList(e); });
    
    const filterLevel = document.getElementById('filter-level');
    const filterClass = document.getElementById('filter-class');
    const filterStream = document.getElementById('filter-stream');
    const resetFilterBtn = document.getElementById('reset-filter-btn'); 
    
    if (filterLevel && filterClass) { 
        window.populateLevelDropdown(filterLevel, true).then(() => {
            window.populateClassDropdown(filterLevel, filterClass, true);
        });
        filterLevel.addEventListener('change', () => {
            window.populateClassDropdown(filterLevel, filterClass, true);
            if (filterStream) window.populateStreamDropdown(filterClass, filterStream, true);
        }); 
    }
    
    if (filterClass && filterStream) {
        filterClass.addEventListener('change', () => {
            window.populateStreamDropdown(filterClass, filterStream, true);
        });
    }

    if (resetFilterBtn) { 
        resetFilterBtn.addEventListener('click', () => { 
            if(filterForm) filterForm.reset(); 
            if(filterClass) filterClass.innerHTML = '<option value="">All</option>'; 
            if(filterStream) filterStream.innerHTML = '<option value="">All</option>'; 
            const container = document.getElementById('student-list-results'); 
            if(container) container.innerHTML = `<div style="text-align:center; padding: 60px 20px; color: #777;"><i class="bi bi-search" style="font-size: 3rem; color: #ddd; margin-bottom: 15px;"></i><p>Use filters to find students.</p></div>`; 
        }); 
    }
};

document.addEventListener('DOMContentLoaded', () => {
    window.initAdmissionForm();
    window.initStudentListForm();

    document.addEventListener('change', function(e) {
        if (e.target && e.target.id === 'edit-photo-input') { 
            const file = e.target.files[0];
            if (file) {
                const form = e.target.closest('#edit-mode-form'); const studentIdInput = form ? form.querySelector('input[name="AdmissionNo"]') : null; const admissionNo = studentIdInput ? studentIdInput.value : null;
                if (!admissionNo) { window.showCustomAlert('error', 'Configuration Error', 'Could not locate Admission No.'); return; }
                const tempInput = document.getElementById('temp-photo-filename');
                if (tempInput && tempInput.value) { window.deleteTempFile(admissionNo, tempInput.value); }
                const formData = new FormData(); formData.append('AdmissionNo', admissionNo); formData.append('photo', file);
                fetch(`${getApiPrefix()}api/students/upload_temp.php`, { method: 'POST', body: formData }).then(r => r.json()).then(data => {
                    if(data.success) {
                        const newSrc = data.previewUrl; const displayPhoto = document.getElementById('display-photo'); const formPreview = document.getElementById('form-photo-preview'); const timestamp = new Date().getTime();
                        if (displayPhoto) displayPhoto.src = newSrc + '?t=' + timestamp; if (formPreview) formPreview.src = newSrc + '?t=' + timestamp;
                        if (tempInput) tempInput.value = data.tempFileName; window.togglePageEditMode('edit');
                    } else { window.showCustomAlert('error', 'Upload Failed', data.message); }
                }).catch(err => { console.error(err); window.showCustomAlert('error', 'Network Error', 'Check console.'); });
            }
        }
    });

    document.addEventListener('click', function(e) {
        if (e.target.closest('.photo-edit-overlay') && !e.target.closest('[onclick]')) { window.triggerPhotoUpload(); }
        const editBtn = e.target.closest('.js-edit-btn'); if (editBtn) { e.preventDefault(); window.togglePageEditMode('edit'); }
        
        // Use stopImmediatePropagation to prevent any inline onclicks from double-firing
        const saveBtn = e.target.closest('.js-save-profile'); 
        if (saveBtn) { 
            e.preventDefault(); 
            e.stopImmediatePropagation(); 
            window.confirmAction('profile_save'); 
        }
        
        // UPDATED: Cancel button logic with new function call
        const cancelBtn = e.target.closest('.action-cancel-edit'); 
        if (cancelBtn) { 
            e.preventDefault(); 
            e.stopImmediatePropagation(); 
            window.confirmAction('profile_cancel'); 
        }
        
        const deleteBtn = e.target.closest('.js-delete-student'); 
        if (deleteBtn) { 
            e.preventDefault(); 
            window.confirmAction('delete_student', deleteBtn.dataset.id, deleteBtn.dataset.name || deleteBtn.dataset.ad_no); 
        }
    });
    
    
    initQuickEditModule(); initAcademicEditModule(); initMigrationModule(); initSearchModule(); initPhotoEditModule();  initAccountsModule(); initAlumniDebtsModule(); initAlumniReceiptsModule();window.autoFillAdmissionForm();
});
/**
 * student.js - External Exam Marks Logic
 * Handles dynamic views for PLE and UCE/UACE
 */

// 1. Navigation & View Switching
window.loadMarksModule = function() {
    const level = document.getElementById('examLevelSelect').value || 'PLE';
    switchMarksView(level);
};

window.switchMarksView = function(level) {
    const header = document.getElementById('marksTableHeader');
    if (!header) return;

    let headerHtml = '';
    // Format based on the provided results document
    if (level === 'PLE') {
        headerHtml = `
            <tr>
                <th>School</th>
                <th>Student Name</th>
                <th>Index No.</th>
                <th>English</th>
                <th>Maths</th>
                <th>Science</th>
                <th>SST</th>
                <th>Agg.</th>
                <th>Div.</th>
                <th>Actions</th>
            </tr>`;
    } else {
        // UCE/UACE Format with extended subjects
        headerHtml = `
            <tr>
                <th>Student Name</th>
                <th>Index No.</th>
                <th>Eng</th>
                <th>Lit</th>
                <th>Hist</th>
                <th>Geo</th>
                <th>Math</th>
                <th>Phy</th>
                <th>Chem</th>
                <th>Bio</th>
                <th>Opt 1</th>
                <th>Opt 2</th>
                <th>Div/Points</th>
                <th>Actions</th>
            </tr>`;
    }
    header.innerHTML = headerHtml;
    fetchMarksData(level);
};

// 2. Data Fetching & Rendering
function fetchMarksData(level) {
    const body = document.getElementById('marksDataBody');
    if (!body) return;

    fetch(`${getApiPrefix()}api/students/fetch_marks.php?level=${level}`)
        .then(res => res.json())
        .then(data => {
            body.innerHTML = data.map(row => renderMarkRow(level, row)).join('');
        })
        .catch(err => console.error("Error loading marks:", err));
}

function renderMarkRow(level, row) {
    if (level === 'PLE') {
        return `
            <tr>
                <td><span contenteditable="true" onblur="updateMark(${row.id}, 'school', this.innerText)">${row.school || ''}</span></td>
                <td>${row.student_name}</td>
                <td><span contenteditable="true" onblur="updateMark(${row.id}, 'index_no', this.innerText)">${row.index_no || ''}</span></td>
                <td><input type="text" class="table-input" value="${row.english || ''}" onchange="updateMark(${row.id}, 'english', this.value)"></td>
                <td><input type="text" class="table-input" value="${row.maths || ''}" onchange="updateMark(${row.id}, 'maths', this.value)"></td>
                <td><input type="text" class="table-input" value="${row.science || ''}" onchange="updateMark(${row.id}, 'science', this.value)"></td>
                <td><input type="text" class="table-input" value="${row.sst || ''}" onchange="updateMark(${row.id}, 'sst', this.value)"></td>
                <td>${row.aggregate || ''}</td>
                <td>${row.division || ''}</td>
                <td><button onclick="deleteMark(${row.id})" class="btn-delete"><i class="bi bi-trash"></i></button></td>
            </tr>`;
    } else {
        // UCE/UACE detailed subjects
        return `
            <tr>
                <td>${row.student_name}</td>
                <td><span contenteditable="true" onblur="updateMark(${row.id}, 'index_no', this.innerText)">${row.index_no || ''}</span></td>
                <td><input type="text" class="table-input" value="${row.english || ''}" onchange="updateMark(${row.id}, 'english', this.value)"></td>
                <td><input type="text" class="table-input" value="${row.literature || ''}" onchange="updateMark(${row.id}, 'literature', this.value)"></td>
                <td><input type="text" class="table-input" value="${row.history || ''}" onchange="updateMark(${row.id}, 'history', this.value)"></td>
                <td><input type="text" class="table-input" value="${row.geography || ''}" onchange="updateMark(${row.id}, 'geography', this.value)"></td>
                <td><input type="text" class="table-input" value="${row.maths || ''}" onchange="updateMark(${row.id}, 'maths', this.value)"></td>
                <td><input type="text" class="table-input" value="${row.physics || ''}" onchange="updateMark(${row.id}, 'physics', this.value)"></td>
                <td><input type="text" class="table-input" value="${row.chemistry || ''}" onchange="updateMark(${row.id}, 'chemistry', this.value)"></td>
                <td><input type="text" class="table-input" value="${row.biology || ''}" onchange="updateMark(${row.id}, 'biology', this.value)"></td>
                <td><input type="text" class="table-input" value="${row.opt1 || ''}" onchange="updateMark(${row.id}, 'opt1', this.value)"></td>
                <td><input type="text" class="table-input" value="${row.opt2 || ''}" onchange="updateMark(${row.id}, 'opt2', this.value)"></td>
                <td>${row.division || ''}</td>
                <td><button onclick="deleteMark(${row.id})" class="btn-delete"><i class="bi bi-trash"></i></button></td>
            </tr>`;
    }
}

// 3. Manual Edit Handler
window.updateMark = function(id, column, value) {
    const formData = new FormData();
    formData.append('id', id);
    formData.append('column', column);
    formData.append('value', value);

    fetch(`${getApiPrefix()}api/students/update_mark.php`, {
        method: 'POST',
        body: formData
    })
    .then(res => res.json())
    .then(data => {
        if (data.status !== 'success') {
            alert("Error updating record: " + data.message);
        }
    })
    .catch(err => console.error("Update failed:", err));
};

// 4. Excel Import Helper
window.handleMarksImport = function(input) {
    if (!input.files || !input.files[0]) return;
    
    const formData = new FormData();
    formData.append('file', input.files[0]);
    formData.append('level', document.getElementById('examLevelSelect').value);

    fetch(`${getApiPrefix()}api/students/import_marks.php`, {
        method: 'POST',
        body: formData
    })
    .then(res => res.json())
    .then(data => {
        alert(data.message);
        loadMarksModule(); // Refresh table
    });
};

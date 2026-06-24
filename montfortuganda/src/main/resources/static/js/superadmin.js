// ==========================================
// SUPER ADMIN MODULE
// ==========================================

const formatUGX = (num) => new Intl.NumberFormat('en-UG', { style: 'currency', currency: 'UGX', maximumFractionDigits: 0 }).format(num);

// ==========================================
// UI ANIMATION UTILITIES
// ==========================================

function animateValue(obj, start, end, duration, isCurrency = false) {
    if (!obj) return;
    let startTimestamp = null;
    const step = (timestamp) => {
        if (!startTimestamp) startTimestamp = timestamp;
        const progress = Math.min((timestamp - startTimestamp) / duration, 1);

        // Premium Quartic ease-out curve (fast start, slow dramatic finish)
        const easeOut = 1 - Math.pow(1 - progress, 4);
        let current = Math.floor(easeOut * (end - start) + start);

        obj.textContent = isCurrency ? formatUGX(current) : current.toLocaleString();

        if (progress < 1) {
            window.requestAnimationFrame(step);
        } else {
            // Ensure it lands perfectly on the final number
            obj.textContent = isCurrency ? formatUGX(end) : end.toLocaleString();
        }
    };
    window.requestAnimationFrame(step);
}

// ==========================================
// UTILITY: POPULATE BRANCH DROPDOWNS
// ==========================================
async function populateBranchDropdowns(selectElements) {
    try {
        const json = await apiGet('/superadmin/branches');
        /** @type {{branchId: number, branchName: string, branchLocation: string}[]} */
        const branches = json.data || [];

        selectElements.forEach(selectEl => {
            if (!selectEl) return;
            const firstOption = selectEl.options[0]; // Preserve "All Branches"
            selectEl.textContent = '';
            if (firstOption) selectEl.appendChild(firstOption);

            branches.forEach(b => {
                const opt = document.createElement('option');
                opt.value = b.branchName;
                opt.textContent = b.branchLocation ? `${b.branchName} (${b.branchLocation})` : b.branchName;
                selectEl.appendChild(opt);
            });
        });
    } catch (e) {
        console.error("Failed to fetch branches for dropdowns", e);
    }
}

function extractInchargeDetails(viewContainer, tbodyId, nameClass, roleClass, phoneClass) {
    const incharges = [];
    viewContainer.querySelectorAll('#' + tbodyId + ' tr').forEach(row => {
        const name = row.querySelector('.' + nameClass).value.trim();
        const role = row.querySelector('.' + roleClass).value.trim();
        const phone = row.querySelector('.' + phoneClass).value.trim();
        if (name || role || phone) {
            incharges.push({ name, role, phone });
        }
    });
    return incharges;
}

function confirmAllocation(title, contentText, endpoint, payload, successMsg, errorMsg, inputField) {
    showPremiumModal({
        title: title,
        type: 'warning',
        contentText: contentText,
        confirmText: 'Allocate',
        cancelText: 'Cancel',
        onConfirm: async (modal) => {
            modal.close();
            showLoader();
            try {
                await apiPost(endpoint, payload);
                showSuccessMessage(successMsg);
                if (inputField) inputField.value = '';
            } catch (err) {
                showErrorMessage(errorMsg);
            } finally {
                hideLoader();
            }
        }
    });
}

function confirmAction(title, type, contentText, confirmText, endpoint, isPost, successMsg, errorMsg, callbackStr) {
    showPremiumModal({
        title: title,
        type: type,
        contentText: contentText,
        confirmText: confirmText,
        cancelText: 'Cancel',
        onConfirm: async (modal) => {
            modal.close();
            showLoader();
            try {
                let res;
                if (isPost) res = await apiPost(endpoint);
                else res = await apiGet(endpoint);
                showSuccessMessage(res.message || successMsg);
                if (callbackStr === 'initSystemBackupsView') {
                    initSystemBackupsView();
                }
            } catch (e) {
                console.error(e);
                showErrorMessage(errorMsg);
            } finally {
                hideLoader();
            }
        }
    });
}

document.addEventListener('viewLoaded', function(e) {
    if (e.detail.role === 'superadmin') {
        if (e.detail.view === 'branches') {
            void initBranchesView();
        } else if (e.detail.view === 'home') {
            void initHomeView();
        } else if (e.detail.view === 'add-branch') {
            void initAddBranchView();
        } else if (e.detail.view === 'system-stats') {
            void initSystemStatsView();
        } else if (e.detail.view === 'audit-logs') {
            void initAuditLogsView();
        } else if (e.detail.view === 'system-backups') {
            void initSystemBackupsView();
        } else if (e.detail.view === 'scholarships-funds-got') {
            void initScholarshipsFundsGotView();
        } else if (e.detail.view === 'scholarships-global-search') {
            void initScholarshipsApplicationsView(); // Fixed
        } else if (e.detail.view === 'scholarships-bulk-distribution') {
            initBulkDistributionView();
        } else if (e.detail.view === 'scholarships-1-to-1') {
            initOneToOneSponsorshipView();
        } else if (e.detail.view === 'scholarships-partial-fund') {
            void initPartialStudentFundView(); // Fixed
        }
    }
});

// ---------------------------------------------------------
// 1. DASHBOARD HOME LOGIC
// ---------------------------------------------------------
async function initHomeView() {
    const linkBranches = document.getElementById('sa-btnLinkBranches');
    if (linkBranches) {
        linkBranches.addEventListener('click', () => {
            const link = document.querySelector('.sidebar-nav a[href*="branches"]');
            if(link) link.click();
        });
    }

    try {
        const [branchRes, usersRes, settingsRes] = await Promise.all([
            apiGet('/superadmin/branches').catch(()=>null),
            apiGet('/superadmin/users').catch(()=>null),
            apiGet('/superadmin/settings').catch(()=>null)
        ]);

        animateValue(document.getElementById('sa-statTotalBranches'), 0, branchRes?.data?.length || 0, 1200, false);
        animateValue(document.getElementById('sa-statTotalUsers'), 0, usersRes?.data?.length || 0, 1500, false);
        animateValue(document.getElementById('sa-statTotalSettings'), 0, settingsRes?.data?.length || 0, 1800, false);
    } catch (error) {
        console.error("Failed to load dashboard stats", error);
        showErrorMessage("Could not load dashboard statistics.");
    }
}


// ---------------------------------------------------------
// 2. BRANCHES LOGIC (SPA)
// ---------------------------------------------------------
function initBranchesView() {

    const viewContainer = document.querySelector('#superadmin-branches-view');
    if (!viewContainer) return;

    const tableBody = viewContainer.querySelector('#sa-branchesTableBody');
    const tableView = viewContainer.querySelector('#sa-branchTableView');
    const detailView = viewContainer.querySelector('#sa-branchDetailView');

    let currentDetailBranchId = '';

    function addEditInchargeRow(name = '', role = '', phone = '') {
        const template = viewContainer.querySelector('#incharge-edit-row-template');
        if(!template) return;

        const clone = template.content.cloneNode(true);
        clone.querySelector('.inc-name').value = name;
        clone.querySelector('.inc-role').value = role;
        clone.querySelector('.inc-phone').value = phone;

        clone.querySelector('.remove-inc-btn').addEventListener('click', function() {
            this.closest('tr').remove();
        });

        viewContainer.querySelector('#edit-incharge-tbody').appendChild(clone);
    }

    const addPersonBtn = viewContainer.querySelector('#edit-addInchargeRowBtn');
    if (addPersonBtn) {
        const newBtn = addPersonBtn.cloneNode(true);
        addPersonBtn.parentNode.replaceChild(newBtn, addPersonBtn);
        newBtn.addEventListener('click', () => addEditInchargeRow());
    }

    async function loadBranches() {
        const tbody = viewContainer.querySelector('#sa-branchesTableBody');
        if (tbody) renderFetchingMessage(tbody, 10, 'Fetching branches...');
        try {
            const json = await apiGet('/superadmin/branches');
            const branches = json.data;

            const template = viewContainer.querySelector('#branch-row-template');
            if (!tableBody || !template) return;

            tableBody.textContent = '';

            branches.forEach(branch => {
                const clone = template.content.cloneNode(true);

                clone.querySelector('.col-id').textContent = branch.branchId.toString();
                clone.querySelector('.col-name strong').textContent = branch.branchName;
                clone.querySelector('.col-code').textContent = branch.schoolCode;
                clone.querySelector('.col-type').textContent = branch.branchType;

                const toggle = clone.querySelector('.status-toggle');
                toggle.checked = branch.isActive === 1;
                toggle.setAttribute('data-id', branch.branchId);

                clone.querySelector('.view-more-btn').setAttribute('data-id', branch.branchId);

                tableBody.appendChild(clone);
            });
        } catch (error) {
            console.error(error);
            tableBody.textContent = 'Failed to load branches.';
        } finally {
            hideLoader();
        }
    }

    if (tableBody) {
        tableBody.addEventListener('click', async function(e) {
            const viewBtn = e.target.closest('.view-more-btn');
            const toggleBtn = e.target.closest('.status-toggle');

            if (viewBtn) {
                const id = parseInt(viewBtn.getAttribute('data-id'));
                void openViewMore(id);
            } else if (toggleBtn) {
                e.preventDefault(); // Stop instant toggle
                const id = parseInt(toggleBtn.getAttribute('data-id'));

                showPremiumModal({
                    title: 'Confirm Action',
                    type: 'warning',
                    contentText: 'Are you sure you want to change the active status of this branch?',
                    confirmText: 'Yes, Change Status',
                    cancelText: 'Cancel',
                    onConfirm: async (modal) => {
                        modal.close();
                        showLoader();
                        try {
                            await apiPut(`/superadmin/branches/${id}/toggle`, {});
                            toggleBtn.checked = !toggleBtn.checked; // Change UI visually securely
                            showPremiumModal({ title: 'Success', type: 'success', contentText: 'Branch status successfully updated.', confirmText: 'OK' });
                        } catch (err) {
                            showErrorMessage("Failed to update status");
                        } finally {
                            hideLoader();
                        }
                    }
                });
            }
        });
    }

    const backBtn = viewContainer.querySelector('#sa-backToTableBtn');
    if (backBtn) {
        backBtn.addEventListener('click', () => {
            detailView.classList.add('hidden');
            tableView.classList.remove('hidden');
            void loadBranches();
        });
    }

    async function openViewMore(id) {
        currentDetailBranchId = id;
        showLoader();
        try {
            const json = await apiGet('/superadmin/branches');
            const branch = json.data.find(b => b.branchId === id);

            if (branch) {
                viewContainer.querySelector('#detail-schoolNameHeader').textContent = branch.branchName;
                viewContainer.querySelector('#view-branchName').textContent = branch.branchName;
                viewContainer.querySelector('#view-schoolCode').textContent = branch.schoolCode;
                viewContainer.querySelector('#view-adminUsername').textContent = branch.schoolCode.toLowerCase() + "@montfort.ug";
                viewContainer.querySelector('#view-branchType').textContent = branch.branchType;
                viewContainer.querySelector('#view-foundationDate').textContent = branch.foundationDate || '';
                viewContainer.querySelector('#view-branchLocation').textContent = branch.branchLocation;
                viewContainer.querySelector('#view-contactDetails').textContent = branch.contactDetails;

                const inchargeView = viewContainer.querySelector('#view-inchargeDetails');
                const inchargeEditTbody = viewContainer.querySelector('#edit-incharge-tbody');

                if (inchargeView) inchargeView.textContent = '';
                if (inchargeEditTbody) inchargeEditTbody.textContent = '';

                try {
                    const incharges = JSON.parse(branch.inchargeDetails || '[]');
                    if (incharges.length === 0) {
                        const emptyDiv = document.createElement('div');
                        emptyDiv.className = 'view-incharge-empty';
                        emptyDiv.textContent = 'No incharge details provided.';
                        inchargeView.appendChild(emptyDiv);
                    } else {
                        const viewTableTemplate = viewContainer.querySelector('#incharge-view-table-template');
                        if (viewTableTemplate) {
                            const viewTableClone = viewTableTemplate.content.cloneNode(true);
                            const viewTbody = viewTableClone.querySelector('.incharge-view-tbody');

                            incharges.forEach(inc => {
                                const trView = document.createElement('tr');
                                const tdName = document.createElement('td'); tdName.textContent = inc.name;
                                const tdRole = document.createElement('td'); tdRole.textContent = inc.role;
                                const tdPhone = document.createElement('td'); tdPhone.textContent = inc.phone;
                                trView.appendChild(tdName); trView.appendChild(tdRole); trView.appendChild(tdPhone);
                                viewTbody.appendChild(trView);

                                addEditInchargeRow(inc.name, inc.role, inc.phone);
                            });
                            inchargeView.appendChild(viewTableClone);
                        }
                    }
                } catch(e) {
                    const errorDiv = document.createElement('div');
                    errorDiv.className = 'view-incharge-error';
                    errorDiv.textContent = 'Invalid format.';
                    inchargeView.appendChild(errorDiv);
                }

                const photoImg = viewContainer.querySelector('#view-schoolPhoto');
                if(branch.schoolPhotoUrl) {
                    photoImg.src = branch.schoolPhotoUrl;
                    photoImg.classList.remove('hidden');
                } else {
                    photoImg.classList.add('hidden');
                }

                const docDiv = viewContainer.querySelector('#view-govDocument');
                docDiv.textContent = '';
                if(branch.govDocumentUrl) {
                    const link = document.createElement('a');
                    link.href = branch.govDocumentUrl;
                    link.target = '_blank';
                    link.className = 'btn-secondary';
                    link.textContent = 'View Document';
                    docDiv.appendChild(link);
                } else {
                    docDiv.textContent = "No document uploaded.";
                }

                // --- NEW: Load Stats & Audit Logs ---
                try {
                    const statsRes = await apiGet(`/superadmin/branches/${id}/stats`).catch(() => null);
                    if(statsRes && statsRes.data) {
                        viewContainer.querySelector('#view-statStudents').textContent = (statsRes.data['students'] || 0).toString();
                        viewContainer.querySelector('#view-statStaff').textContent = (statsRes.data['staff'] || 0).toString();
                        viewContainer.querySelector('#view-statAttendance').textContent = (statsRes.data['attendance'] || 0) + '%';
                    } else {
                        viewContainer.querySelector('#view-statStudents').textContent = 'N/A';
                        viewContainer.querySelector('#view-statStaff').textContent = 'N/A';
                        viewContainer.querySelector('#view-statAttendance').textContent = 'N/A';
                    }

                    const logTbody = viewContainer.querySelector('#view-auditLogTbody');
                    const logTemplate = viewContainer.querySelector('#audit-log-row-template');
                    if (logTbody && logTemplate) {
                        logTbody.textContent = ''; // Clear old logs
                        const logsRes = await apiGet(`/superadmin/branches/${id}/logs`).catch(() => null);
                        // Use mocked data if backend endpoint throws 404
                        const logs = (logsRes && logsRes.data) ? logsRes.data : [
                            { date: 'Today, 10:30 AM', user: 'School Admin', action: 'Logged into portal' },
                            { date: 'Yesterday, 4:15 PM', user: 'System', action: 'Automated weekly backup completed' }
                        ];

                        if (logs.length === 0) {
                            const tr = document.createElement('tr'); const td = document.createElement('td'); td.colSpan = 3; td.style.textAlign = 'center'; td.style.color = '#94a3b8'; td.style.padding = '20px'; td.textContent = 'No recent activity found.'; tr.appendChild(td); logTbody.appendChild(tr);
                        } else {
                            logs.forEach(log => {
                                const clone = logTemplate.content.cloneNode(true);
                                clone.querySelector('.log-date').textContent = log.date;
                                clone.querySelector('.log-user').textContent = log.user;
                                clone.querySelector('.log-action').textContent = log.action;
                                logTbody.appendChild(clone);
                            });
                        }
                    }
                } catch(e) { console.error("Stats Error:", e); }
                // --- END NEW ---

                tableView.classList.add('hidden');
                detailView.classList.remove('hidden');
                resetEditMode();
            }
        } catch(error) {
            console.error(error);
            showErrorMessage("Failed to fetch branch details");
        } finally {
            hideLoader();
        }
    }

    const editBtn = viewContainer.querySelector('#sa-editBranchBtn');
    const saveBtn = viewContainer.querySelector('#sa-saveBranchBtn');
    const cancelEditBtn = viewContainer.querySelector('#sa-cancelEditBtn');

    if (editBtn) {
        editBtn.addEventListener('click', () => {
            viewContainer.querySelectorAll('.detail-text:not(.readonly-always)').forEach(el => el.classList.add('hidden'));
            viewContainer.querySelectorAll('.detail-input').forEach(el => el.classList.remove('hidden'));

            viewContainer.querySelector('#edit-branchName').value = viewContainer.querySelector('#view-branchName').textContent;
            viewContainer.querySelector('#edit-schoolCode').value = viewContainer.querySelector('#view-schoolCode').textContent;
            viewContainer.querySelector('#edit-branchType').value = viewContainer.querySelector('#view-branchType').textContent;
            viewContainer.querySelector('#edit-foundationDate').value = viewContainer.querySelector('#view-foundationDate').textContent;
            viewContainer.querySelector('#edit-branchLocation').value = viewContainer.querySelector('#view-branchLocation').textContent;
            viewContainer.querySelector('#edit-contactDetails').value = viewContainer.querySelector('#view-contactDetails').textContent;

            editBtn.classList.add('hidden');
            saveBtn.classList.remove('hidden');
            if (cancelEditBtn) cancelEditBtn.classList.remove('hidden');
        });
    }

    if (cancelEditBtn) {
        cancelEditBtn.addEventListener('click', () => {
            void openViewMore(currentDetailBranchId);
        });
    }

    // NEW: Branch Admin Password Reset
    const resetPwdBtn = viewContainer.querySelector('#sa-resetBranchAdminPwdBtn');
    if (resetPwdBtn) {
        resetPwdBtn.addEventListener('click', () => {
            showPremiumModal({
                title: 'Reset Admin Password',
                type: 'warning',
                contentText: 'Are you sure you want to reset the School Admin password for this branch? It will revert to the default format.',
                confirmText: 'Yes, Reset',
                cancelText: 'Cancel',
                onConfirm: async (modal) => {
                    modal.close();
                    showLoader();
                    try {
                        await apiPut(`/superadmin/branches/${currentDetailBranchId}/reset-admin-password`, {});
                        showSuccessMessage('The administrator password has been reset successfully.');
                    } catch (e) {
                        console.error(e);
                        showErrorMessage('Failed to reset the password. Ensure backend is configured.');
                    } finally {
                        hideLoader();
                    }
                }
            });
        });
    }

    // NEW: Data Backup Logic using Premium Modal
    const backupBtn = viewContainer.querySelector('#sa-backupBranchBtn');
    if (backupBtn) {
        backupBtn.addEventListener('click', () => {
            confirmAction(
                'Export Branch Data',
                'info',
                'This will compile all database records for this branch into a secure file. Do you want to proceed?',
                'Start Export',
                `/superadmin/branches/${currentDetailBranchId}/export`,
                false,
                'Export initiated.',
                'Failed to trigger backup. Ensure backend endpoint is ready.',
                null
            );
        });
    }

    if (saveBtn) {
        saveBtn.addEventListener('click', async () => {
            showPremiumModal({
                title: 'Save Changes',
                type: 'info',
                contentText: 'Are you sure you want to apply these updates to the branch?',
                confirmText: 'Save Changes',
                cancelText: 'Cancel',
                onConfirm: async (modal) => {
                    modal.close();

                    const formData = new FormData();
                    formData.append("branchName", viewContainer.querySelector('#edit-branchName').value);
                    formData.append("schoolCode", viewContainer.querySelector('#edit-schoolCode').value);
                    formData.append("branchType", viewContainer.querySelector('#edit-branchType').value);
                    formData.append("foundationDate", viewContainer.querySelector('#edit-foundationDate').value);
                    formData.append("branchLocation", viewContainer.querySelector('#edit-branchLocation').value);
                    formData.append("contactDetails", viewContainer.querySelector('#edit-contactDetails').value);

                    const updatedIncharges = extractInchargeDetails(viewContainer, 'edit-incharge-tbody', 'inc-name', 'inc-role', 'inc-phone');
                    formData.append("inchargeDetails", JSON.stringify(updatedIncharges));

                    const photoFile = viewContainer.querySelector('#edit-schoolPhoto').files[0];
                    const docFile = viewContainer.querySelector('#edit-govDocument').files[0];
                    if(photoFile) formData.append("photo", photoFile);
                    if(docFile) formData.append("document", docFile);

                    showLoader();
                    try {
                        await apiMultipart(`/superadmin/branches/${currentDetailBranchId}`, 'PUT', formData);
                        showPremiumModal({ title: 'Saved!', type: 'success', contentText: 'Changes applied successfully.', confirmText: 'Done' });
                        void openViewMore(currentDetailBranchId);
                    } catch(e) {
                        console.error(e);
                        showErrorMessage("Failed to save changes.");
                    } finally {
                        hideLoader();
                    }
                }
            });
        });
    }

    function resetEditMode() {
        viewContainer.querySelectorAll('.detail-text').forEach(el => el.classList.remove('hidden'));
        viewContainer.querySelectorAll('.detail-input').forEach(el => el.classList.add('hidden'));
        if (editBtn) editBtn.classList.remove('hidden');
        if (saveBtn) saveBtn.classList.add('hidden');
        if (cancelEditBtn) cancelEditBtn.classList.add('hidden');
    }

    const printBtn = viewContainer.querySelector('#sa-printBranchBtn');
    if (printBtn) {
        printBtn.addEventListener('click', () => {
            const printContent = viewContainer.querySelector('#printable-area').cloneNode(true);
            const originalChildren = Array.from(document.body.childNodes);

            document.body.textContent = '';
            const header = document.createElement('h2');
            header.textContent = 'Montfort School Branch Report';
            document.body.appendChild(header);
            document.body.appendChild(printContent);

            window.print();

            document.body.textContent = '';
            originalChildren.forEach(child => document.body.appendChild(child));
        });
    }

    void loadBranches();
}

// ---------------------------------------------------------
// 3. ADD BRANCH PAGE LOGIC (DYNAMIC TABLE & JSON)
// ---------------------------------------------------------
function initAddBranchView() {
    const viewContainer = document.querySelector('#superadmin-add-branch-view');
    if (!viewContainer) return;

    // Changes "Click or drag photo here" to the actual file name when selected!
    viewContainer.querySelectorAll('.file-hidden-input').forEach(input => {
        input.addEventListener('change', function(e) {
            let fileName = "Click or drag file here";
            if (e.target.files && e.target.files.length > 1) {
                fileName = `${e.target.files.length} files selected`;
            } else if (e.target.files && e.target.files.length === 1) {
                fileName = e.target.files[0].name;
            }
            const titleSpan = this.parentElement.querySelector('.upload-title');
            if (titleSpan) titleSpan.textContent = fileName;
        });
    });


    const backBtn = viewContainer.querySelector('#backToBranchesBtn');
    if (backBtn) {
        backBtn.addEventListener('click', () => {
            const mainContent = document.getElementById('main-content-area');
            window.history.pushState({ view: 'branches', title: 'Manage Branches' }, "", "/superadmin/branches");
            const pageTitleElement = document.getElementById('pageTitle');
            if (pageTitleElement) pageTitleElement.textContent = "Manage Branches";
            void loadView('superadmin', 'branches', mainContent);
        });
    }

    const tbody = viewContainer.querySelector('#incharge-tbody');
    const template = viewContainer.querySelector('#incharge-row-template');
    const addRowBtn = viewContainer.querySelector('#addInchargeRowBtn');

    if (addRowBtn && template && tbody) {
        addRowBtn.addEventListener('click', () => {
            const clone = template.content.cloneNode(true);
            clone.querySelector('.remove-incharge-btn').addEventListener('click', function() {
                this.closest('tr').remove();
            });
            tbody.appendChild(clone);
        });
    }

    viewContainer.querySelectorAll('.remove-incharge-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            this.closest('tr').remove();
        });
    });

    const form = viewContainer.querySelector('#add-branch-full-form');
    if (form) {
        form.addEventListener('submit', async function(e) {
            e.preventDefault();

            const incharges = extractInchargeDetails(viewContainer, 'incharge-tbody', 'incharge-name', 'incharge-role', 'incharge-phone');
            const inchargeJson = JSON.stringify(incharges);

            const formData = new FormData();
            formData.append("branchName", viewContainer.querySelector('#add-schoolName').value);
            formData.append("schoolCode", viewContainer.querySelector('#add-schoolCode').value);
            formData.append("foundationDate", viewContainer.querySelector('#add-foundationDate').value);
            formData.append("branchLocation", viewContainer.querySelector('#add-location').value);
            formData.append("contactDetails", viewContainer.querySelector('#add-contact').value);

            const branchTypes = Array.from(viewContainer.querySelectorAll('.type-cb:checked')).map(cb => cb.value);
            formData.append("branchType", branchTypes.join(", "));
            formData.append("inchargeDetails", inchargeJson);

            const photoFile = viewContainer.querySelector('#add-photo').files[0];
            if (photoFile) formData.append("photo", photoFile);

            const docFiles = viewContainer.querySelector('#add-doc').files;
            const allowedTypes = ['image/jpeg', 'image/png', 'application/pdf'];
            const MAX_PDF_SIZE = 2 * 1024 * 1024;
            const MAX_IMG_SIZE = 100 * 1024;

            for (let i = 0; i < docFiles.length; i++) {
                const file = docFiles[i];
                if (!allowedTypes.includes(file.type)) {
                    showErrorMessage(`Upload blocked: "${file.name}" is not a JPG, PNG, or PDF.`);
                    return;
                }
                if (file.type === 'application/pdf' && file.size > MAX_PDF_SIZE) {
                    showErrorMessage(`Upload blocked: PDF "${file.name}" exceeds the 2MB limit.`);
                    return;
                }
                if ((file.type === 'image/jpeg' || file.type === 'image/png') && file.size > MAX_IMG_SIZE) {
                    showErrorMessage(`Upload blocked: Image "${file.name}" exceeds the 100KB limit.`);
                    return;
                }
                formData.append("documents", file);
            }

            showLoader();
            try {
                await apiMultipart('/superadmin/branches', 'POST', formData);

                // Pure JS Password generation reverse-engineered from your backend
                const branchName = viewContainer.querySelector('#add-schoolName').value;
                const schoolCode = viewContainer.querySelector('#add-schoolCode').value;
                const foundationDate = viewContainer.querySelector('#add-foundationDate').value;

                const cleanName = branchName.replace(/\s+/g, '');
                const namePrefix = cleanName.length >= 6 ? cleanName.substring(0, 6) : cleanName;
                const year = foundationDate && foundationDate.length >= 4 ? foundationDate.substring(0, 4) : "";

                const generatedUsername = schoolCode.toLowerCase() + "@montfort.ug";
                const generatedPassword = namePrefix.toUpperCase() + "@" + schoolCode + year;

                // Clone the success template using strict DOM methods
                const successTemplate = document.getElementById('branch-success-content-template');
                const successClone = successTemplate.content.cloneNode(true);

                // Inject the generated text securely
                successClone.querySelector('.success-branch-name').textContent = branchName;
                successClone.querySelector('.cred-username').textContent = generatedUsername;
                successClone.querySelector('.cred-password').textContent = generatedPassword;

                // Fire the custom Modal!
                showPremiumModal({
                    title: 'Branch Created!',
                    type: 'success',
                    contentNode: successClone,
                    confirmText: 'Go to Manage Branches',
                    onConfirm: (modal) => {
                        modal.close();
                        if (backBtn) backBtn.click();
                    }
                });

            } catch (error) {
                console.error("Save error:", error);
                showErrorMessage("Upload failed. Ensure files are within limits and valid types.");
            } finally {
                hideLoader();
            }
        });
    }
}

// ---------------------------------------------------------
// 4. GLOBAL SYSTEM STATS LOGIC (UPGRADED)
// ---------------------------------------------------------
async function initSystemStatsView() {
    const viewContainer = document.querySelector('#superadmin-stats-view');
    if (!viewContainer) return;

    try {
        // Fetch everything first so they animate perfectly in sync!
        const [branchRes, stdRes, staffRes, sessRes] = await Promise.all([
            apiGet('/superadmin/branches').catch(()=>null),
            apiGet('/superadmin/students').catch(()=>null),
            apiGet('/superadmin/staff').catch(()=>null),
            apiGet('/superadmin/sessions/active').catch(()=>null)
        ]);

        animateValue(document.querySelector('#global-stat-branches'), 0, branchRes?.data?.length || 0, 1200);
        animateValue(document.querySelector('#global-stat-students'), 0, stdRes?.data?.length || 0, 1500);
        animateValue(document.querySelector('#global-stat-staff'), 0, staffRes?.data?.length || 0, 1800);
        animateValue(document.querySelector('#global-stat-sessions'), 0, sessRes?.data?.length || 0, 2000);

        // Populate Branch Breakdown List securely via HTML Template
        const breakdownList = viewContainer.querySelector('#enrollment-breakdown-list');
        const template = viewContainer.querySelector('#enrollment-row-template');

        if (breakdownList && template) {
            breakdownList.textContent = ''; // Clear out old data

            // Temporary Mock Data representing real API output
            const mockEnrollmentData = [
                { name: 'Kampala Main Campus', count: 1200, percent: 85 },
                { name: 'Entebbe Branch', count: 850, percent: 60 },
                { name: 'Jinja Branch', count: 620, percent: 45 },
                { name: 'Mbarara Branch', count: 410, percent: 30 },
                { name: 'Gulu Branch', count: 250, percent: 15 }
            ];

            mockEnrollmentData.forEach(item => {
                const clone = template.content.cloneNode(true);
                clone.querySelector('.branch-name').textContent = item.name;
                clone.querySelector('.branch-count').textContent = item.count.toLocaleString() + ' Students';

                // Trigger smooth CSS animation for the progress bar
                const bar = clone.querySelector('.branch-bar');
                setTimeout(() => {
                    bar.style.width = item.percent + '%';
                }, 50); // slight delay to ensure DOM is ready for animation

                breakdownList.appendChild(clone);
            });
        }

    } finally {
        hideLoader();
    }
}

// ---------------------------------------------------------
// 5. GLOBAL AUDIT LOGS LOGIC
// ---------------------------------------------------------
async function initAuditLogsView() {
    const viewContainer = document.querySelector('#superadmin-audit-view');
    if (!viewContainer) return;

    const tbody = viewContainer.querySelector('#global-audit-tbody');
    const template = viewContainer.querySelector('#global-audit-row-template');
    if (!tbody || !template) return;

    if (tbody) renderFetchingMessage(tbody, 10, 'Fetching audit logs...');
    try {
        const logsRes = await apiGet('/superadmin/global-logs').catch(() => null);
        const logs = (logsRes && logsRes.data) ? logsRes.data : [
            { date: 'Today, 08:00 AM', branch: 'Global', user: 'Super Admin', action: 'System maintenance started' },
            { date: 'Yesterday, 02:30 PM', branch: 'Kampala Branch', user: 'School Admin', action: 'Updated fee structures' }
        ];

        tbody.textContent = '';
        logs.forEach(log => {
            const clone = template.content.cloneNode(true);
            clone.querySelector('.log-date').textContent = log.date;
            clone.querySelector('.log-branch').textContent = log.branch;
            clone.querySelector('.log-user').textContent = log.user;
            clone.querySelector('.log-action').textContent = log.action;
            tbody.appendChild(clone);
        });
    } finally {
        hideLoader();
    }
}

// ---------------------------------------------------------
// 6. GLOBAL BACKUPS LOGIC
// ---------------------------------------------------------
function initSystemBackupsView() {
    const viewContainer = document.querySelector('#superadmin-backups-view');
    if (!viewContainer) return;

    const tbody = viewContainer.querySelector('#global-backups-tbody');
    const template = viewContainer.querySelector('#global-backup-row-template');

    if(tbody && template) {
        tbody.textContent = '';
        const backups = [
            { id: 'BKP-20260615', date: '2026-06-15 02:00 AM', size: '14.2 GB', user: 'Auto-Schedule' },
            { id: 'BKP-20260608', date: '2026-06-08 02:00 AM', size: '14.1 GB', user: 'Auto-Schedule' }
        ];
        backups.forEach(bkp => {
            const clone = template.content.cloneNode(true);
            clone.querySelector('.bkp-id').textContent = bkp.id;
            clone.querySelector('.bkp-date').textContent = bkp.date;
            clone.querySelector('.bkp-size').textContent = bkp.size;
            clone.querySelector('.bkp-user').textContent = bkp.user;
            tbody.appendChild(clone);
        });
    }

    const backupBtn = viewContainer.querySelector('#sa-triggerGlobalBackupBtn');
    if (backupBtn) {
        backupBtn.addEventListener('click', () => {
            confirmAction(
                'Initiate Global Backup',
                'warning',
                'This will freeze non-essential database writes for approximately 2 minutes to ensure a clean global snapshot. Proceed?',
                'Yes, Backup Now',
                '/superadmin/backups/trigger',
                true,
                'Backup initiated successfully.',
                'Failed to trigger backup. Ensure Java backend endpoint exists.',
                'initSystemBackupsView'
            );
        });
    }
}
// ==========================================
// VIEW 1: SCHOLARSHIPS FUNDS GOT (TREASURY)
// ==========================================
async function initScholarshipsFundsGotView() {
    const viewContainer = document.querySelector('#superadmin-funds-got-view');
    if (!viewContainer) return;

    const tbody = viewContainer.querySelector('#treasury-donors-tbody');
    if (tbody) renderFetchingMessage(tbody, 10, 'Fetching treasury data...');

    try {
        const summaryRes = await apiGet('/superadmin/scholarships/funds-summary');

        /** @type {{totalRaisedUgx: number, totalSpentUgx: number, availableBalanceUgx: number, studentsSponsored: number}} */
        const summary = summaryRes.data || summaryRes;

        animateValue(document.querySelector('#treasury-total-raised'), 0, summary.totalRaisedUgx || 0, 1500, true);
        animateValue(document.querySelector('#treasury-total-spent'), 0, summary.totalSpentUgx || 0, 1800, true);
        animateValue(document.querySelector('#treasury-available'), 0, summary.availableBalanceUgx || 0, 2000, true);
        animateValue(document.querySelector('#treasury-sponsored'), 0, summary.studentsSponsored || 0, 1200, false);

        const donorsRes = await apiGet('/superadmin/scholarships/donors');
        const donorsArray = Array.isArray(donorsRes) ? donorsRes : (donorsRes.data || []);

        const tbody = viewContainer.querySelector('tbody');
        if (tbody && (!donorsArray || donorsArray.length === 0)) {
            renderEmptyTableMessage(tbody, 10, 'No donor records found.');
        } else if (tbody) {

            const liveDonationsData = donorsArray.map(d => ({
                id: d.id,
                receipt_number: d.receiptNumber,
                full_name: d.fullName,
                email: d.email,
                currency: d.currency,
                amount: d.amount, // <--- Added the foreign amount here
                amount_received: d.amountReceivedUgx,
                amount_spent: d.amountSpentUgx,
                students_benefited: d.studentsBenefited,
                term: d.term
            }));

            const pagination = new GlobalPagination({
                data: liveDonationsData,
                itemsPerPage: 25,
                elements: {
                    startId: 'funds-page-start', endId: 'funds-page-end', totalId: 'funds-page-total',
                    prevBtnId: 'btn-funds-prev', nextBtnId: 'btn-funds-next',
                    numbersContainerId: 'funds-pagination-numbers', templateId: 'funds-page-number-template'
                },
                renderCallback: (pageData) => {
                    const tbody = viewContainer.querySelector('#treasury-donors-tbody');
                    const template = viewContainer.querySelector('#treasury-donor-row-template');
                    if (tbody && template) {
                        tbody.textContent = '';
                        pageData.forEach(donor => {
                            const clone = template.content.cloneNode(true);
                            clone.querySelector('.donor-receipt').textContent = donor.receipt_number;
                            clone.querySelector('.donor-name').textContent = donor.full_name;
                            clone.querySelector('.donor-email').textContent = donor.email;

                            // <--- Updated to display BOTH Currency Type and Amount Donated! --->
                            clone.querySelector('.donor-foreign').textContent = `${donor.currency} ${parseFloat(donor.amount || 0).toLocaleString(undefined, {minimumFractionDigits: 2})}`;

                            clone.querySelector('.donor-received-ugx').textContent = formatUGX(donor.amount_received);

                            const availableUGX = donor.amount_received - donor.amount_spent;
                            const availElem = clone.querySelector('.donor-available-ugx');
                            availElem.textContent = formatUGX(availableUGX);
                            if (availableUGX <= 0) availElem.classList.replace('text-bold', 'text-muted');

                            clone.querySelector('.donor-students').textContent = `${donor.students_benefited} Students`;
                            clone.querySelector('.donor-term').textContent = donor.term;
                            tbody.appendChild(clone);
                        });
                    }
                }
            });
            pagination.render();

        }
    } catch (error) {
        console.error("Treasury load failed:", error);
        showErrorMessage("Failed to load global treasury data.");
    } finally {
        hideLoader();
    }
}
// ==========================================
// VIEW 2: SCHOLARSHIP APPLICATIONS (GLOBAL SEARCH)
// ==========================================
async function initScholarshipsApplicationsView() {
    const viewContainer = document.querySelector('#superadmin-global-search-view');
    if (!viewContainer) return;

    const branchSelect = viewContainer.querySelector('#gs-branch-filter');
    if (branchSelect) void populateBranchDropdowns([branchSelect]);

    const tbody = viewContainer.querySelector('#gs-table-body');
    if (tbody) renderFetchingMessage(tbody, 10, 'Fetching student applications...');

    try {
        const studentsRes = await apiGet('/superadmin/scholarships/pending-students');
        const rawData = Array.isArray(studentsRes) ? studentsRes : (studentsRes.data || []);

        const liveApplications = rawData.map(s => ({
            id: 'APP-' + s.id,
            name: s.studentName,
            demographics: 'Male',
            branch: s.branchName,
            levelClass: 'N/A',
            category: s.category || 'Financial Aid',
            status: 'Pending',
            shortfall: s.currentShortfallUgx
        }));

        let totalDeficit = 0;
        let branchPendingCounts = {};

        liveApplications.forEach(app => {
            if (app.status === 'Pending') {
                totalDeficit += app.shortfall;
                branchPendingCounts[app.branch] = (branchPendingCounts[app.branch] || 0) + 1;
            }
        });

        const assumedTotalNeed = totalDeficit + 150000000;
        let deficitPercentage = 0;
        if (assumedTotalNeed > 0) {
            deficitPercentage = Math.round((totalDeficit / assumedTotalNeed) * 100);
        }

        const donutChart = viewContainer.querySelector('.css-donut');
        if (donutChart) {
            donutChart.style.setProperty('--percentage', deficitPercentage);
            donutChart.querySelector('.donut-text').textContent = `${deficitPercentage}%`;
        }

        const legend = viewContainer.querySelector('#global-deficit-legend');
        if (legend) {
            legend.textContent = '';

            const p1 = document.createElement('p');
            const span1 = document.createElement('span');
            span1.className = 'badge-pending';
            span1.id = 'gs-pending-badge';
            span1.textContent = formatUGX(totalDeficit);
            animateValue(span1, 0, totalDeficit, 2500, true);
            const textNode1 = document.createTextNode(' Unfunded');
            p1.appendChild(span1);
            p1.appendChild(textNode1);

            const p2 = document.createElement('p');
            p2.className = 'text-muted text-sm mt-5';
            p2.textContent = `Out of ${formatUGX(assumedTotalNeed)} Total Need`;

            legend.appendChild(p1);
            legend.appendChild(p2);
        }

        const pendingStatsList = viewContainer.querySelector('#dynamic-branch-stats');
        if (pendingStatsList) {
            pendingStatsList.textContent = '';
            Object.keys(branchPendingCounts).forEach(branchName => {
                const row = document.createElement('div');
                row.className = 'pending-stat-row d-flex justify-content-between mb-2';

                const spanBranch = document.createElement('span');
                spanBranch.className = 'text-bold';
                spanBranch.textContent = branchName;

                const spanCount = document.createElement('span');
                spanCount.className = 'badge-pending';
                spanCount.textContent = `${branchPendingCounts[branchName]} Pending`;

                row.appendChild(spanBranch);
                row.appendChild(spanCount);
                pendingStatsList.appendChild(row);
            });
        }

        let filteredData = [...liveApplications];

        const pagination = new GlobalPagination({
            data: filteredData,
            itemsPerPage: 25,
            elements: {
                startId: 'gs-page-start',
                endId: 'gs-page-end',
                totalId: 'gs-total-entries',
                prevBtnId: 'gs-prev-page',
                nextBtnId: 'gs-next-page',
                numbersContainerId: 'gs-page-numbers',
                templateId: 'funds-page-number-template'
            },
            renderCallback: (pageData) => {
                const tbody = viewContainer.querySelector('#gs-table-body');
                const template = viewContainer.querySelector('#gs-row-template');
                if (!tbody || !template) return;

                tbody.textContent = '';

                pageData.forEach(app => {
                    const clone = template.content.cloneNode(true);
                    clone.querySelector('.col-id').textContent = app.id;
                    clone.querySelector('.col-name').textContent = app.name;

                    const parts = app.name.split(' ');
                    const initials = parts.length > 1 ? parts[0][0] + parts[1][0] : (parts[0][0] || 'S');
                    clone.querySelector('.col-initials').textContent = initials.toUpperCase();

                    clone.querySelector('.col-demo').textContent = app.demographics;
                    clone.querySelector('.col-branch').textContent = app.branch;
                    clone.querySelector('.col-level').textContent = app.levelClass;
                    clone.querySelector('.col-category').textContent = app.category;

                    const statusBadge = clone.querySelector('.col-status');
                    statusBadge.textContent = app.status;
                    statusBadge.className = 'col-status ' + (app.status === 'Pending' ? 'badge-pending' : 'badge-completed');

                    tbody.appendChild(clone);
                });

                const countLabel = viewContainer.querySelector('#gs-results-count');
                if (countLabel) countLabel.textContent = `${filteredData.length} Found`;
            }
        });

        pagination.render();

        const btnApply = viewContainer.querySelector('#gs-apply-btn');
        const btnReset = viewContainer.querySelector('#gs-reset-filters');

        if (btnApply) {
            btnApply.addEventListener('click', () => {
                const search = viewContainer.querySelector('#gs-search-input').value.toLowerCase();
                const status = viewContainer.querySelector('#gs-status-filter').value;
                const branch = viewContainer.querySelector('#gs-branch-filter').value;

                filteredData = liveApplications.filter(app => {
                    const matchesSearch = app.name.toLowerCase().includes(search) || app.id.toLowerCase().includes(search);
                    const matchesStatus = status ? app.status === status : true;
                    const matchesBranch = (branch && branch !== 'all') ? app.branch === branch : true;
                    return matchesSearch && matchesStatus && matchesBranch;
                });

                pagination.updateData(filteredData);
            });
        }

        if (btnReset) {
            btnReset.addEventListener('click', () => {
                viewContainer.querySelector('#gs-search-input').value = '';
                viewContainer.querySelector('#gs-status-filter').value = '';
                viewContainer.querySelector('#gs-branch-filter').value = '';
                filteredData = [...liveApplications];
                pagination.updateData(filteredData);
            });
        }

    } catch (error) {
        console.error("Global search view load failed:", error);
    } finally {
        hideLoader();
    }
}

// ==========================================
// VIEW 3: BULK DISTRIBUTION LOGIC
// ==========================================
function initBulkDistributionView() {
    const bulkDistributionView = document.getElementById('superadmin-bulk-distribution-view');
    if (!bulkDistributionView) return;

    const newView = bulkDistributionView.cloneNode(true);
    bulkDistributionView.parentNode.replaceChild(newView, bulkDistributionView);

    let currentHistoryData = [];

    const branchTable = document.querySelector('#superadmin-bulk-distribution-view tbody');
    if (branchTable) renderFetchingMessage(branchTable, 10, 'Fetching branch demands...');

    try {apiGet('/superadmin/scholarships/branch-demands').then(res => {
        const demands = Array.isArray(res) ? res : (res.data || []);

        // Fetch funds summary so we can animate all 3 cards exactly in sync!
        apiGet('/superadmin/scholarships/funds-summary').then(fundsRes => {
            /** @type {{availableBalanceUgx: number, allocatedThisTermUgx: number}} */
            const fSummary = fundsRes.data || {};
            const cards = document.querySelectorAll('#superadmin-bulk-distribution-view .fund-card h2');
            if (cards.length >= 3) {
                animateValue(cards[0], 0, fSummary.availableBalanceUgx || 0, 1500, true);
                animateValue(cards[2], 0, fSummary.allocatedThisTermUgx || 0, 1800, true);
            }
        }).catch(e => console.error(e));

        let totalDemands = 0;
        demands.forEach(d => totalDemands += (d.totalRequestedAmountUgx || 0));

        const tDom = document.querySelector('#superadmin-bulk-distribution-view .fund-card:nth-child(2) h2');
        if (tDom) {
            animateValue(tDom, 0, totalDemands, 2000, true);
        }

        const branchTable = document.querySelector('#superadmin-bulk-distribution-view tbody');
        const rowTemplate = document.getElementById('bulk-distribution-row-template');

        if (branchTable && rowTemplate && demands.length > 0) {
            branchTable.textContent = '';
            demands.forEach(d => {
                const clone = rowTemplate.content.cloneNode(true);
                const bName = d.branchName || ('Branch ' + d.branchId);

                clone.querySelector('.branch-name').textContent = bName;
                clone.querySelector('.branch-code').textContent = `BR-00${d.branchId}`;
                clone.querySelector('.col-requested').textContent = formatUGX(d.totalRequestedAmountUgx);
                clone.querySelector('.col-allocated').textContent = formatUGX(d.currentlyAllocatedUgx || 0);

                const btnAlloc = clone.querySelector('.btn-allocate');
                btnAlloc.setAttribute('data-branch-id', d.branchId);

                const btnHist = clone.querySelector('.btn-history');
                btnHist.setAttribute('data-branch-id', d.branchId);
                btnHist.setAttribute('data-branch-name', bName);

                branchTable.appendChild(clone);
            });
        }
    }).catch(e => console.error("Failed to load demands:", e));
    } catch(err) { hideLoader(); }

    function renderHistoryTable(dataArray) {
        const tbody = document.getElementById('ajax-history-tbody');
        const template = document.getElementById('history-transaction-row-template');
        if (!tbody || !template) return;
        tbody.textContent = '';

        if (dataArray.length === 0) {
            renderEmptyTableMessage(tbody, 5, 'No transactions found.');
            return;
        }

        let totalAllocated = 0;
        dataArray.forEach(tx => {
            const clone = template.content.cloneNode(true);
            clone.querySelector('.tx-date').textContent = tx.date;
            clone.querySelector('.tx-id').textContent = tx.id.toString();
            clone.querySelector('.tx-amount').textContent = formatUGX(tx.amount);

            const statusBadge = clone.querySelector('.tx-status');
            statusBadge.textContent = tx.status;
            statusBadge.className = 'tx-status badge bg-success text-white';

            tbody.appendChild(clone);
            totalAllocated += tx.amount;
        });

        const statTotal = document.getElementById('stat-total-allocated');
        if (statTotal) statTotal.textContent = formatUGX(totalAllocated);
        const statCount = document.getElementById('stat-total-tx');
        if (statCount) statCount.textContent = dataArray.length.toString();
    }

    newView.addEventListener('click', async function(e) {
        const btnAllocate = e.target.closest('.btn-allocate');
        if (btnAllocate) {
            const row = btnAllocate.closest('tr');
            const branchName = row.querySelector('.branch-name').textContent;
            const branchId = btnAllocate.getAttribute('data-branch-id') || 1;
            const inputField = row.querySelector('.allocate-input');
            const amount = inputField.value;

            if (!amount || amount <= 0) return showErrorMessage('Please enter a valid amount.');

            confirmAllocation(
                'Confirm Allocation',
                `Allocate UGX ${amount} to ${branchName}?`,
                '/superadmin/scholarships/allocate-branch',
                {
                    branchId: parseInt(branchId),
                    amountUgx: parseFloat(amount),
                    term: 'Term 1',
                    academicYear: '2026/2027'
                },
                `Success! Allocated UGX ${amount} to ${branchName}.`,
                "Treasury Error: Insufficient funds or server error.",
                inputField
            );
        }

        const btnHistory = e.target.closest('.btn-history');
        if (btnHistory) {
            const branchId = btnHistory.getAttribute('data-branch-id') || 1;
            const branchName = btnHistory.getAttribute('data-branch-name') || "Branch";
            const title = document.getElementById('history-branch-title');
            if(title) title.textContent = branchName;

            showLoader();
            try {
                const res = await apiGet(`/superadmin/scholarships/history/${branchId}`);
                const historyRaw = Array.isArray(res) ? res : (res.data || []);

                currentHistoryData = historyRaw.map(tx => ({
                    id: 'TX-' + tx.id,
                    date: tx.createdAt ? new Date(tx.createdAt).toLocaleDateString() : 'Just now',
                    amount: tx.allocatedAmountUgx,
                    status: 'Completed'
                }));

                renderHistoryTable(currentHistoryData);
                document.getElementById('bulk-list-section').classList.add('hidden');
                document.getElementById('bulk-history-section').classList.remove('hidden');
            } catch (err) {
                showErrorMessage('Failed to load history');
            } finally {
                hideLoader();
            }
        }

        const btnBack = e.target.closest('#btn-back-to-bulk-list');
        if (btnBack) {
            document.getElementById('bulk-history-section').classList.add('hidden');
            document.getElementById('bulk-list-section').classList.remove('hidden');
        }
    });
}

// ==========================================
// VIEW 4: 1-TO-1 SPONSORSHIP LOGIC (AJAX PAGE SWAP)
// ==========================================
function initOneToOneSponsorshipView() {
    const viewContainer = document.querySelector('#superadmin-1to1-view');
    if (!viewContainer) return;

    // View Sections
    const mainHeader = viewContainer.querySelector('#pairings-main-header');
    const listSection = viewContainer.querySelector('#pairings-list-section');
    const wizardSection = viewContainer.querySelector('#pairing-wizard-section');

    function loadActivePairings() {
        const tbody = viewContainer.querySelector('tbody');
        if (tbody) renderFetchingMessage(tbody, 10, 'Fetching active sponsorships...');

        apiGet('/superadmin/scholarships/active-sponsorships').then(res => {
            const active = Array.isArray(res) ? res : (res.data || []);
            const tbody = viewContainer.querySelector('tbody');
            const template = document.getElementById('active-pairing-row-template');

            if (tbody && template) {
                tbody.textContent = '';

                if (active.length === 0) {
                    renderEmptyTableMessage(tbody, 5, 'No active sponsorships found.');
                    return;
                }

                active.forEach(a => {
                    const clone = template.content.cloneNode(true);
                    clone.querySelector('.donor-name').textContent = a.donorDetails;
                    clone.querySelector('.student-name').textContent = a.studentDetails;
                    clone.querySelector('.col-campus').textContent = a.branchDetails;
                    clone.querySelector('.view-profile-btn').setAttribute('data-id', a.id);
                    tbody.appendChild(clone);
                });
            }
        }).catch(e => console.error("Failed to load active sponsorships:", e));
    }

    loadActivePairings();

    let selectedDonorId = '', selectedStudentId = '';
    let selectedDonorName = null, selectedStudentName = null;

    function resetWizardSelection() {
        selectedDonorId = ''; selectedStudentId = '';
        selectedDonorName = null; selectedStudentName = null;
        document.getElementById('summary-sponsor').textContent = 'None selected';
        document.getElementById('summary-student').textContent = 'None selected';

        viewContainer.querySelectorAll('.btn-select-donor, .btn-select-student').forEach(el => {
            el.classList.remove('selected');
            const badge = el.querySelector('.card-badge');
            if(badge) badge.textContent = '';
        });
        checkEnableConfirmButton();
    }

    function checkEnableConfirmButton() {
        const btnConfirm = document.getElementById('btn-confirm-match');
        if (btnConfirm) {
            if (selectedDonorId && selectedStudentId) {
                btnConfirm.disabled = false;
                btnConfirm.classList.remove('btn-disabled');
            } else {
                btnConfirm.disabled = true;
                btnConfirm.classList.add('btn-disabled');
            }
        }
    }

    // OPEN WIZARD (Hides Table, Shows Match Maker)

    const btnOpenModal = viewContainer.querySelector('[data-action="open-pairing-modal"]');
    if (btnOpenModal) {
        btnOpenModal.addEventListener('click', async () => {
            showLoader();
            try {
                const [donorsRes, studentsRes] = await Promise.all([
                    apiGet('/superadmin/scholarships/donors'),
                    apiGet('/superadmin/scholarships/pending-students')
                ]);

                const liveDonors = Array.isArray(donorsRes) ? donorsRes : (donorsRes.data || []);
                const liveStudents = Array.isArray(studentsRes) ? studentsRes : (studentsRes.data || []);

                const donorList = document.getElementById('donor-list-container');
                const donorTemplate = document.getElementById('donor-card-template');
                if (donorList && donorTemplate) {
                    donorList.textContent = '';
                    liveDonors.forEach(d => {
                        const available = d.amountReceivedUgx - d.amountSpentUgx;
                        if (available <= 0) return;

                        const clone = donorTemplate.content.cloneNode(true);
                        const card = clone.querySelector('.selectable-card');
                        card.classList.add('btn-select-donor');
                        card.setAttribute('data-id', d.id);
                        card.setAttribute('data-name', d.fullName);

                        clone.querySelector('.card-title').textContent = d.fullName;
                        clone.querySelector('.card-subtitle').textContent = `Available: ${formatUGX(available)}`;
                        donorList.appendChild(clone);
                    });
                }

                const studentList = document.getElementById('student-list-container');
                const studentTemplate = document.getElementById('student-card-template');
                if (studentList && studentTemplate) {
                    studentList.textContent = '';
                    liveStudents.forEach(s => {
                        const clone = studentTemplate.content.cloneNode(true);
                        const card = clone.querySelector('.selectable-card');
                        card.classList.add('btn-select-student');
                        card.setAttribute('data-id', s.id);
                        card.setAttribute('data-name', s.studentName);

                        clone.querySelector('.card-title').textContent = s.studentName;
                        clone.querySelector('.card-subtitle').textContent = `Shortfall: ${formatUGX(s.currentShortfallUgx)}`;
                        studentList.appendChild(clone);
                    });
                }

                // UI AJAX SWAP LOGIC (Hides main UI, Shows Wizard)
                resetWizardSelection();
                if(mainHeader) mainHeader.classList.add('hidden');
                if(listSection) listSection.classList.add('hidden');
                if(wizardSection) wizardSection.classList.remove('hidden');

            } catch (err) {
                console.error(err);
                showErrorMessage("Failed to load lists for Match Maker.");
            } finally {
                hideLoader();
            }
        });
    }

    // CLOSE WIZARD (Hides Match Maker, Shows Table)
    const btnCloseWizard = viewContainer.querySelector('[data-action="close-wizard"]');
    if (btnCloseWizard) {
        btnCloseWizard.addEventListener('click', () => {
            if(wizardSection) wizardSection.classList.add('hidden');
            if(mainHeader) mainHeader.classList.remove('hidden');
            if(listSection) listSection.classList.remove('hidden');
        });
    }

    // SEARCH LOGIC
    viewContainer.querySelector('#search-donor').addEventListener('input', (e) => {
        const term = e.target.value.toLowerCase();
        Array.from(document.getElementById('donor-list-container').children).forEach(card => {
            if (card.innerText.toLowerCase().includes(term)) {
                card.classList.remove('hidden');
            } else {
                card.classList.add('hidden');
            }
        });
    });

    viewContainer.querySelector('#search-student').addEventListener('input', (e) => {
        const term = e.target.value.toLowerCase();
        Array.from(document.getElementById('student-list-container').children).forEach(card => {
            if (card.innerText.toLowerCase().includes(term)) {
                card.classList.remove('hidden');
            } else {
                card.classList.add('hidden');
            }
        });
    });

    // SELECTION LOGIC
    const wizardBody = document.getElementById('pairing-wizard-section');
    if (wizardBody) {
        function handleSelection(target, btnClass) {
            document.querySelectorAll('.' + btnClass).forEach(b => {
                b.classList.remove('selected');
                const badge = b.querySelector('.card-badge');
                if(badge) badge.textContent = '';
            });
            target.classList.add('selected');
            const activeBadge = target.querySelector('.card-badge');
            if(activeBadge) activeBadge.textContent = 'Selected';
            return { id: target.getAttribute('data-id'), name: target.getAttribute('data-name') };
        }

        wizardBody.addEventListener('click', function(e) {
            const donorCard = e.target.closest('.btn-select-donor');
            if (donorCard) {
                const data = handleSelection(donorCard, 'btn-select-donor');
                selectedDonorId = data.id; selectedDonorName = data.name;
                document.getElementById('summary-sponsor').textContent = selectedDonorName;
                checkEnableConfirmButton();
            }

            const studentCard = e.target.closest('.btn-select-student');
            if (studentCard) {
                const data = handleSelection(studentCard, 'btn-select-student');
                selectedStudentId = data.id; selectedStudentName = data.name;
                document.getElementById('summary-student').textContent = selectedStudentName;
                checkEnableConfirmButton();
            }
        });
    }

    // CONFIRM LOGIC
    const btnSavePairing = document.getElementById('btn-confirm-match');
    if (btnSavePairing) {
        btnSavePairing.addEventListener('click', async () => {
            if (!selectedDonorId || !selectedStudentId) return;

            showLoader();
            try {
                await apiPost('/superadmin/scholarships/allocate-student', {
                    branchId: 1,
                    studentId: parseInt(selectedStudentId),
                    amountUgx: 0,
                    term: 'Term 1',
                    academicYear: '2026/2027'
                });

                showSuccessMessage(`SUCCESS! We paired Sponsor: ${selectedDonorName} with Student: ${selectedStudentName}.`);

                // Return to Table View
                if(wizardSection) wizardSection.classList.add('hidden');
                if(mainHeader) mainHeader.classList.remove('hidden');
                if(listSection) listSection.classList.remove('hidden');

                loadActivePairings();

            } catch(err) {
                showErrorMessage("Error: Failed to process match.");
            } finally {
                hideLoader();
            }
        });
    }
}

// ==========================================
// VIEW 5: PARTIAL STUDENT FUND LOGIC
// ==========================================
async function initPartialStudentFundView() {
    const viewContainer = document.querySelector('#superadmin-partial-fund-view');
    if (!viewContainer) return;

    const listSection = viewContainer.querySelector('#partial-fund-list-section');
    const detailSection = viewContainer.querySelector('#partial-fund-detail-section');
    const tbody = viewContainer.querySelector('#partial-fund-tbody');
    const template = document.querySelector('#partial-student-row-template');

    const searchInput = viewContainer.querySelector('#search-partial-students');
    const filterBranch = viewContainer.querySelector('#filter-branch');
    const btnBack = viewContainer.querySelector('#btn-back-to-partial-list');

    if (filterBranch) void populateBranchDropdowns([filterBranch]);

    if (!tbody || !template) return;

    let allStudents = [];

    if (listSection && detailSection) {
        listSection.classList.remove('hidden');
        detailSection.classList.add('hidden');
    }

    async function loadData() {
        const tbody = viewContainer.querySelector('#partial-fund-tbody');
        if (tbody) window.renderFetchingMessage(tbody, 10, 'Fetching sponsorships...');

        try {
            // 1. Fetch Global Treasury Summary Independently
            let summary = { availableBalanceUgx: 0, totalSpentUgx: 0 };
            try {
                const summaryRes = await apiGet('/superadmin/scholarships/funds-summary');
                summary = summaryRes.data || summaryRes;
            } catch (summaryErr) {
                console.warn("Could not load funds summary. Your web_donations table might have an issue.", summaryErr);
            }

            // 2. Fetch Pending Students Independently
            try {
                const studentsRes = await apiGet('/superadmin/scholarships/pending-students');
                allStudents = Array.isArray(studentsRes) ? studentsRes : (studentsRes.data || []);
            } catch (studentsErr) {
                console.warn("Could not load pending students. Does the erp_scholarship_applications table exist?", studentsErr);
                allStudents = []; // Default to empty array so the UI doesn't crash
            }

            // 3. Update the UI Stat Cards Safely with Smooth Animation!
            const availEl = viewContainer.querySelector('#partial-funds-available');
            const pendEl = viewContainer.querySelector('#partial-pending-count');
            const disbEl = viewContainer.querySelector('#partial-disbursed');

            if (availEl) animateValue(availEl, 0, summary.availableBalanceUgx || 0, 1500, true);
            if (disbEl) animateValue(disbEl, 0, summary.totalSpentUgx || 0, 1800, true);

            if (pendEl) {
                // Animate the number, then safely append " Students" once it finishes
                animateValue(pendEl, 0, allStudents.length, 1200, false);
                setTimeout(() => {
                    if (pendEl.textContent === allStudents.length.toString()) {
                        pendEl.textContent = `${allStudents.length} Students`;
                    }
                }, 1300);
            }

            // 4. Render the Table
            renderTable();

        } finally {
            hideLoader();
        }
    }

    function renderTable() {
        tbody.textContent = '';

        const searchTerm = (searchInput ? searchInput.value.toLowerCase() : '');
        const branchFilter = (filterBranch ? filterBranch.value : 'all');

        let filtered = allStudents.filter(s => {
            const studentNameStr = s.studentName || 'Unknown';
            const studentIdVal = s.studentId || s.id || '';
            const matchesSearch = studentNameStr.toLowerCase().includes(searchTerm) ||
                (`ID-${studentIdVal}`).toLowerCase().includes(searchTerm);

            const campusStr = s.campusName || s.branchName || 'Main Campus';
            const matchesBranch = branchFilter === 'all' || campusStr.toLowerCase().includes(branchFilter.toLowerCase());

            return matchesSearch && matchesBranch;
        });

        if (filtered.length === 0) {
            renderEmptyTableMessage(tbody, 6, 'No students match the criteria.');
            return;
        }

        filtered.forEach(s => {
            const clone = template.content.cloneNode(true);

            // Fix property mismatch: The Java DTO uses studentId, campusName, totalFeesUgx, shortfallUgx
            clone.querySelector('.student-id').textContent = 'ID-' + (s.studentId || s.id || 'N/A');
            clone.querySelector('.student-name').textContent = s.studentName || 'Unknown';
            clone.querySelector('.campus-name').textContent = s.campusName || s.branchName || 'Main Campus';

            const totalFeesEl = clone.querySelector('.total-fees');
            if (totalFeesEl) totalFeesEl.textContent = formatUGX(s.totalFeesUgx || s.amountRequestedUgx || 0);

            clone.querySelector('.shortfall-amount').textContent = formatUGX(s.shortfallUgx || s.currentShortfallUgx || 0);

            const btnAlloc = clone.querySelector('.btn-allocate');
            if (btnAlloc) {
                btnAlloc.setAttribute('data-student-id', s.studentId || s.id);
                btnAlloc.setAttribute('data-campus-id', s.campusId || s.branchId || 1);
            }

            const btnDetails = clone.querySelector('.btn-view-details');
            if (btnDetails) {
                btnDetails.setAttribute('data-student-id', s.studentId || s.id);
            }

            tbody.appendChild(clone);
        });
    }

    if (searchInput) searchInput.addEventListener('input', renderTable);
    if (filterBranch) filterBranch.addEventListener('change', renderTable);

    await loadData();

    viewContainer.addEventListener('click', async function(e) {
        const btnAllocate = e.target.closest('.btn-allocate');
        if (btnAllocate) {
            const row = btnAllocate.closest('tr');
            const studentName = row.querySelector('.student-name').textContent;
            const campusId = btnAllocate.getAttribute('data-campus-id');
            const studentId = btnAllocate.getAttribute('data-student-id');
            const inputField = row.querySelector('.allocate-input');
            const amount = inputField.value;

            if (!amount || amount <= 0) return showErrorMessage('Please enter a valid amount.');

            confirmAllocation(
                'Confirm Allocation',
                `Allocate UGX ${amount} to ${studentName}?`,
                '/superadmin/scholarships/allocate-student',
                {
                    branchId: parseInt(campusId),
                    studentId: parseInt(studentId),
                    amountUgx: parseFloat(amount),
                    term: 'Term 1',
                    academicYear: '2026/2027'
                },
                `Successfully allocated UGX ${amount} to ${studentName}.`,
                "Treasury Error: Insufficient funds.",
                inputField
            );
        }

        const btnDetails = e.target.closest('.btn-view-details');
        if (btnDetails) {
            const studentId = btnDetails.getAttribute('data-student-id');
            // FIX: Convert the string ID to a Number safely!
            const student = allStudents.find(s => (s.studentId || s.id) === parseInt(studentId, 10));
            if (!student) return;

            viewContainer.querySelector('#detail-student-name').textContent = student.studentName || 'Unknown';
            viewContainer.querySelector('#detail-student-id').textContent = 'ID-' + (student.studentId || student.id);

            showLoader();
            try {
                viewContainer.querySelector('#detail-hardship-reason').textContent = "Family lost primary source of income. Unable to complete remaining balance for this term.";
                viewContainer.querySelector('#detail-academic-score').textContent = "Excellent standing. 4.2 GPA.";

                const historyTbody = viewContainer.querySelector('#detail-history-tbody');
                const historyTemplate = document.getElementById('partial-history-row-template');
                historyTbody.textContent = '';

                if (historyTemplate) {
                    const clone = historyTemplate.content.cloneNode(true);
                    clone.querySelector('.history-date').textContent = '2026-03-10';
                    clone.querySelector('.history-amount').textContent = 'UGX 500,000';
                    clone.querySelector('.history-user').textContent = 'Super Admin';
                    historyTbody.appendChild(clone);
                }

                if (listSection && detailSection) {
                    listSection.classList.add('hidden');
                    detailSection.classList.remove('hidden');
                }
            } catch(err) {
                console.error(err);
            } finally {
                hideLoader();
            }
        }
    });

    if (btnBack) {
        btnBack.addEventListener('click', () => {
            if (listSection && detailSection) {
                detailSection.classList.add('hidden');
                listSection.classList.remove('hidden');
            }
        });
    }
}
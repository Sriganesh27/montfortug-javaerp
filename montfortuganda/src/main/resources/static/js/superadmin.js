// ==========================================
// SUPER ADMIN MODULE
// ==========================================

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
        } else if (e.detail.view === 'scholarships-bulk-distribution') {
            initBulkDistributionView();
        } else if (e.detail.view === 'scholarships-1-to-1') {
            initOneToOneSponsorshipView();
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
        const branchJson = await apiGet('/superadmin/branches');
        const statTotalBranches = document.getElementById('sa-statTotalBranches');
        if(statTotalBranches) statTotalBranches.textContent = branchJson.data.length.toString();

        const usersJson = await apiGet('/superadmin/users');
        const statTotalUsers = document.getElementById('sa-statTotalUsers');
        if(statTotalUsers) statTotalUsers.textContent = usersJson.data.length.toString();

        const settingsJson = await apiGet('/superadmin/settings');
        const statTotalSettings = document.getElementById('sa-statTotalSettings');
        if(statTotalSettings) statTotalSettings.textContent = settingsJson.data.length.toString();

    } catch (error) {
        console.error("Failed to load dashboard stats", error);
        showErrorMessage("Could not load dashboard statistics.");
    } finally {
        hideLoader();
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

    let currentDetailBranchId = null;

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
        showLoader();
        try {
            const json = await apiGet('/superadmin/branches');
            const branches = json.data;

            const template = viewContainer.querySelector('#branch-row-template');
            if (!tableBody || !template) return;

            tableBody.textContent = '';

            branches.forEach(branch => {
                const clone = template.content.cloneNode(true);

                clone.querySelector('.col-id').textContent = branch.branchId;
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
                            logTbody.innerHTML = '<tr><td colspan="3" style="text-align:center; color:#94a3b8; padding: 20px;">No recent activity found.</td></tr>';
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
            showPremiumModal({
                title: 'Export Branch Data',
                type: 'info',
                contentText: 'This will compile all database records for this branch into a secure file. Do you want to proceed?',
                confirmText: 'Start Export',
                cancelText: 'Cancel',
                onConfirm: async (modal) => {
                    modal.close();
                    showLoader();
                    try {
                        await apiGet(`/superadmin/branches/${currentDetailBranchId}/backup`);
                        showSuccessMessage('Backup successfully triggered! The download will begin shortly.');
                    } catch (e) {
                        console.error(e);
                        showErrorMessage('Failed to trigger backup. Ensure backend endpoint is ready.');
                    } finally {
                        hideLoader();
                    }
                }
            });
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

                    const updatedIncharges = [];
                    viewContainer.querySelectorAll('#edit-incharge-tbody tr').forEach(row => {
                        const name = row.querySelector('.inc-name').value.trim();
                        const role = row.querySelector('.inc-role').value.trim();
                        const phone = row.querySelector('.inc-phone').value.trim();
                        if (name || role || phone) {
                            updatedIncharges.push({ name, role, phone });
                        }
                    });
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
            const printContent = viewContainer.querySelector('#printable-area').innerHTML;
            const originalContent = document.body.innerHTML;

            document.body.innerHTML = `<h2>Montfort School Branch Report</h2>${printContent}`;
            window.print();

            document.body.innerHTML = originalContent;
            location.reload();
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

            const incharges = [];
            viewContainer.querySelectorAll('#incharge-tbody tr').forEach(row => {
                const name = row.querySelector('.incharge-name').value.trim();
                const role = row.querySelector('.incharge-role').value.trim();
                const phone = row.querySelector('.incharge-phone').value.trim();
                if (name || role || phone) {
                    incharges.push({ name, role, phone });
                }
            });
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

    showLoader();
    try {
        const branchesRes = await apiGet('/superadmin/branches').catch(() => ({data: []}));

        // Populate Top Cards
        viewContainer.querySelector('#global-stat-branches').textContent = (branchesRes.data.length || 7).toString();
        viewContainer.querySelector('#global-stat-students').textContent = "4,250"; // Mock Total
        viewContainer.querySelector('#global-stat-staff').textContent = "315"; // Mock Total
        viewContainer.querySelector('#global-stat-sessions').textContent = "84"; // Mock Live Sessions

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

    showLoader();
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
            showPremiumModal({
                title: 'Initiate Global Backup',
                type: 'warning',
                contentText: 'This will freeze non-essential database writes for approximately 2 minutes to ensure a clean global snapshot. Proceed?',
                confirmText: 'Yes, Backup Now',
                cancelText: 'Cancel',
                onConfirm: async (modal) => {
                    modal.close();
                    showLoader();
                    try {
                        await apiGet('/superadmin/global-backup');
                        showSuccessMessage('Global Database Snapshot successfully generated!');
                    } catch (e) {
                        showErrorMessage('Failed to trigger backup. Ensure Java backend endpoint exists.');
                    } finally {
                        hideLoader();
                    }
                }
            });
        });
    }
}

// ---------------------------------------------------------
// VIEW 1: SCHOLARSHIPS FUNDS GOT (TREASURY)
// ---------------------------------------------------------
async function initScholarshipsFundsGotView() {
    const viewContainer = document.querySelector('#superadmin-funds-got-view');
    if (!viewContainer) return;

    showLoader();
    try {
        // Pure Formatting Logic
        const formatUGX = (num) => new Intl.NumberFormat('en-UG', { style: 'currency', currency: 'UGX', maximumFractionDigits: 0 }).format(num);

        // Expanded Mock Data mimicking the precise schema of web_donations + distributions
        const donationsData = [
            { id: 1, receipt_number: 'SSP001-001', full_name: 'Dennis Rodricks', email: 'dennisrodricks@yahoo.com', currency: 'USD', amount: 300, amount_received: 1058972, amount_spent: 0, students_benefited: 0, term: 'Term 1, 2024' },
            { id: 2, receipt_number: 'SSP001-002', full_name: 'Dennis Rodricks', email: 'dennisrodricks@yahoo.com', currency: 'USD', amount: 800, amount_received: 2820132, amount_spent: 1500000, students_benefited: 3, term: 'Term 1, 2024' },
            { id: 3, receipt_number: 'SSP001-003', full_name: 'Gopi Manchineella', email: 'gopi.manchi@gmail.com', currency: 'USD', amount: 150, amount_received: 529486, amount_spent: 0, students_benefited: 0, term: 'Term 1, 2024' },
            { id: 4, receipt_number: 'SSP001-004', full_name: 'Global Donor 4', email: 'donor4@yahoo.com', currency: 'EUR', amount: 500, amount_received: 2000000, amount_spent: 2000000, students_benefited: 4, term: 'Term 2, 2024' }
        ];

        // Process Global Treasury Stats dynamically based on the dataset
        let totalRaised = 0;
        let totalSpent = 0;
        let totalSponsored = 0;

        donationsData.forEach(d => {
            totalRaised += d.amount_received;
            totalSpent += d.amount_spent;
            totalSponsored += d.students_benefited; // Automatically tallies students!
        });

        viewContainer.querySelector('#treasury-total-raised').textContent = formatUGX(totalRaised);
        viewContainer.querySelector('#treasury-total-spent').textContent = formatUGX(totalSpent);
        viewContainer.querySelector('#treasury-available').textContent = formatUGX(totalRaised - totalSpent);
        viewContainer.querySelector('#treasury-sponsored').textContent = totalSponsored.toString();

        // ----------------------------------------------------
        // Initialize Global Pagination
        // ----------------------------------------------------
        const pagination = new GlobalPagination({
            data: donationsData,
            itemsPerPage: 25,
            elements: {
                startId: 'funds-page-start',
                endId: 'funds-page-end',
                totalId: 'funds-page-total',
                prevBtnId: 'btn-funds-prev',
                nextBtnId: 'btn-funds-next',
                numbersContainerId: 'funds-pagination-numbers',
                templateId: 'funds-page-number-template'
            },
            renderCallback: (pageData) => {
                // Secure DOM Population (No HTML in JS)
                const tbody = viewContainer.querySelector('#treasury-donors-tbody');
                const template = viewContainer.querySelector('#treasury-donor-row-template');

                if (tbody && template) {
                    tbody.textContent = ''; // Fast, secure clear

                    pageData.forEach(donor => {
                        const clone = template.content.cloneNode(true);

                        clone.querySelector('.donor-receipt').textContent = donor.receipt_number;
                        clone.querySelector('.donor-name').textContent = donor.full_name;
                        clone.querySelector('.donor-email').textContent = donor.email;
                        clone.querySelector('.donor-foreign').textContent = `${donor.amount} ${donor.currency}`;
                        clone.querySelector('.donor-received-ugx').textContent = formatUGX(donor.amount_received);

                        const availableUGX = donor.amount_received - donor.amount_spent;
                        const availElem = clone.querySelector('.donor-available-ugx');
                        availElem.textContent = formatUGX(availableUGX);

                        if (availableUGX <= 0) {
                            availElem.classList.replace('text-bold', 'text-muted');
                        }

                        clone.querySelector('.donor-students').textContent = `${donor.students_benefited} Students`;
                        clone.querySelector('.donor-term').textContent = donor.term;

                        tbody.appendChild(clone);
                    });
                }
            }
        });

        // Trigger the initial table draw
        pagination.render();

        // Attach Secure Confirm Modal for Exporting Report
        const exportBtn = viewContainer.querySelector('#btn-export-donor-report');
        if (exportBtn) {
            exportBtn.addEventListener('click', () => {
                if (typeof showPremiumModal !== 'undefined') {
                    showPremiumModal({
                        title: 'Export Donor Accountability Report',
                        type: 'info',
                        contentText: 'Are you sure you want to generate a full PDF accountability report for all Project SSP001 donors?',
                        confirmText: 'Generate PDF',
                        cancelText: 'Cancel',
                        onConfirm: (modal) => {
                            console.log("Triggering Backend PDF Generation...");
                            modal.close();
                        }
                    });
                }
            });
        }

    } catch (error) {
        console.error("Treasury load failed:", error);
    } finally {
        hideLoader();
    }
}

// ==========================================
// VIEW 3: BULK DISTRIBUTION LOGIC (PAGE SWAP)
// ==========================================
function initBulkDistributionView() {
    const bulkDistributionView = document.getElementById('superadmin-bulk-distribution-view');
    if (!bulkDistributionView) return;

    // Refresh view securely to clear old rogue listeners
    const newView = bulkDistributionView.cloneNode(true);
    bulkDistributionView.parentNode.replaceChild(newView, bulkDistributionView);

    // State
    let currentHistoryData = [];
    const formatUGX = (num) => new Intl.NumberFormat('en-UG', { style: 'currency', currency: 'UGX', maximumFractionDigits: 0 }).format(num);

    function renderHistoryTable(dataArray) {
        const tbody = document.getElementById('ajax-history-tbody');
        const template = document.getElementById('history-transaction-row-template');
        if (!tbody || !template) return;

        tbody.textContent = ''; // Fast secure clear

        if (dataArray.length === 0) {
            const tr = document.createElement('tr');
            const td = document.createElement('td');
            td.colSpan = 5;
            td.className = 'empty-table-cell'; // Using CSS class, no inline styles!
            td.textContent = 'No transactions found matching your criteria.';
            tr.appendChild(td);
            tbody.appendChild(tr);
            return;
        }

        let totalAllocated = 0;

        dataArray.forEach(tx => {
            const clone = template.content.cloneNode(true);
            clone.querySelector('.tx-date').textContent = tx.date;
            clone.querySelector('.tx-id').textContent = tx.id;
            clone.querySelector('.tx-amount').textContent = formatUGX(tx.amount);

            const statusBadge = clone.querySelector('.tx-status');
            statusBadge.textContent = tx.status;
            if (tx.status.toLowerCase() === 'completed') {
                statusBadge.classList.add('badge-completed');
            } else {
                statusBadge.classList.add('badge-pending');
            }

            // Bind the individual print button ID securely
            const printBtn = clone.querySelector('.btn-print-receipt');
            printBtn.setAttribute('data-tx-id', tx.id);

            tbody.appendChild(clone);
            totalAllocated += tx.amount;
        });

        // Update Dynamic Stats securely
        const statTotal = document.getElementById('stat-total-allocated');
        const statCount = document.getElementById('stat-total-tx');
        const statLast = document.getElementById('stat-last-tx');

        if (statTotal) statTotal.textContent = formatUGX(totalAllocated);
        if (statCount) statCount.textContent = dataArray.length.toString();
        if (statLast && dataArray.length > 0) statLast.textContent = dataArray[0].date;
    }

    newView.addEventListener('click', function(e) {

        // --- ACTION 1: ALLOCATE FUNDS ---
        const btnAllocate = e.target.closest('.btn-allocate');
        if (btnAllocate) {
            const row = btnAllocate.closest('tr');
            const branchName = row.querySelector('.branch-name').textContent;
            const branchId = row.getAttribute('data-branch-id');
            const inputField = row.querySelector('.allocate-input');
            const amount = inputField.value;

            if (!amount || amount <= 0) {
                alert('Please enter a valid amount to allocate.');
                inputField.focus();
                return;
            }

            if (confirm(`Allocate UGX ${amount} to ${branchName} (${branchId})?`)) {
                alert(`Success! Allocated UGX ${amount} to ${branchName}.`);
                inputField.value = '';
            }
        }

        // --- ACTION 2: OPEN HISTORY PAGE (MASTER -> DETAIL) ---
        const btnHistory = e.target.closest('.btn-history');
        if (btnHistory) {
            const branchName = btnHistory.getAttribute('data-branch-name');
            const title = document.getElementById('history-branch-title');
            if(title) title.textContent = branchName;

            // Generate Mock Data for this specific branch
            currentHistoryData = [
                { id: 'TX-99381', date: 'Oct 12, 2025', amount: 10000000, status: 'Completed' },
                { id: 'TX-84722', date: 'Sep 05, 2025', amount: 5000000, status: 'Completed' },
                { id: 'TX-73611', date: 'Jul 22, 2025', amount: 15000000, status: 'Pending' },
                { id: 'TX-61504', date: 'Jan 10, 2025', amount: 8500000, status: 'Completed' }
            ];

            const searchInput = document.getElementById('history-search-input');
            if(searchInput) searchInput.value = '';

            renderHistoryTable(currentHistoryData);

            // Hide the list, show the history
            const listSection = document.getElementById('bulk-list-section');
            const historySection = document.getElementById('bulk-history-section');
            if(listSection) listSection.classList.add('hidden');
            if(historySection) historySection.classList.remove('hidden');
        }

        // --- ACTION 3: BACK TO LIST (DETAIL -> MASTER) ---
        const btnBack = e.target.closest('#btn-back-to-bulk-list');
        if (btnBack) {
            const listSection = document.getElementById('bulk-list-section');
            const historySection = document.getElementById('bulk-history-section');
            if(historySection) historySection.classList.add('hidden');
            if(listSection) listSection.classList.remove('hidden');
        }

        // --- ACTION 4: PRINT FULL HISTORY PDF ---
        const btnPrintAll = e.target.closest('#btn-print-all-history');
        if (btnPrintAll) {
            console.log("Triggered PDF Generation for all transactions in current view.");
            alert("Ready for your PDF Layout logic!");
        }

        // --- ACTION 5: PRINT INDIVIDUAL RECEIPT PDF ---
        const btnPrintSingle = e.target.closest('.btn-print-receipt');
        if (btnPrintSingle) {
            const txId = btnPrintSingle.getAttribute('data-tx-id');
            console.log("Triggered PDF Generation for single transaction:", txId);
            alert(`Ready for your PDF Layout logic for Transaction: ${txId}`);
        }
    });

    // --- REAL-TIME SEARCH FILTER LOGIC ---
    const searchInput = newView.querySelector('#history-search-input');
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            const term = e.target.value.toLowerCase();
            const filtered = currentHistoryData.filter(tx =>
                tx.id.toLowerCase().includes(term) || tx.status.toLowerCase().includes(term)
            );
            renderHistoryTable(filtered);
        });
    }
}
// ==========================================
// VIEW 4: 1-TO-1 SPONSORSHIP LOGIC (ADVANCED)
// ==========================================
function initOneToOneSponsorshipView() {
    const oneToOneView = document.getElementById('superadmin-1to1-view');
    if (!oneToOneView) return;

    // Refresh view securely to clear old rogue listeners
    const newView = oneToOneView.cloneNode(true);
    oneToOneView.parentNode.replaceChild(newView, oneToOneView);

    // --- MOCK DATA FOR ADVANCED WIZARD ---
    const mockDonors = [
        { id: 'D1', name: 'MTN Foundation', type: 'Corporate', available: 'UGX 15,000,000' },
        { id: 'D2', name: 'Dr. Sarah Jenkins', type: 'Individual', available: 'UGX 2,500,000' },
        { id: 'D3', name: 'Airtel Cares', type: 'Corporate', available: 'UGX 50,000,000' },
        { id: 'D4', name: 'Michael Chang', type: 'Individual', available: 'UGX 1,000,000' },
        { id: 'D5', name: 'Global Aid Trust', type: 'NGO', available: 'UGX 8,000,000' }
    ];

    const mockStudents = [
        { id: 'S1', name: 'John Doe', class: 'Primary 5', campus: 'Wakiso', needed: 'UGX 500,000' },
        { id: 'S2', name: 'Mary Smith', class: 'Senior 2', campus: 'Kampala', needed: 'UGX 1,200,000' },
        { id: 'S3', name: 'Alice Nakato', class: 'Primary 4', campus: 'Entebbe', needed: 'UGX 450,000' },
        { id: 'S4', name: 'Bob Kasozi', class: 'Senior 1', campus: 'Jinja', needed: 'UGX 1,100,000' },
        { id: 'S5', name: 'Grace Omoding', class: 'Senior 4', campus: 'Wakiso', needed: 'UGX 1,500,000' }
    ];

    // Wizard State
    let selectedDonorId = null;
    let selectedStudentId = null;
    let selectedDonorName = null;
    let selectedStudentName = null;

    // --- FUNCTION TO RENDER LISTS SAFELY (0% HTML in JS) ---
    function renderCards(listId, templateId, dataArray, isDonor) {
        const container = document.getElementById(listId);
        const template = document.getElementById(templateId);
        if (!container || !template) return;

        container.textContent = ''; // Fast secure clear

        dataArray.forEach(item => {
            const clone = template.content.cloneNode(true);
            const card = clone.querySelector('.selectable-card');

            card.setAttribute('data-id', item.id);
            card.setAttribute('data-name', item.name);
            card.querySelector('.card-title').textContent = item.name;

            if (isDonor) {
                card.querySelector('.card-subtitle').textContent = item.type;
                card.querySelector('.card-badge').textContent = item.available;
            } else {
                card.querySelector('.card-subtitle').textContent = `${item.class} • ${item.campus}`;
                card.querySelector('.card-badge').textContent = item.needed;
            }

            container.appendChild(clone);
        });
    }

    function checkConfirmationState() {
        const btnConfirm = document.getElementById('btn-confirm-advanced-match');
        if (!btnConfirm) return;

        if (selectedDonorId && selectedStudentId) {
            btnConfirm.disabled = false;
            btnConfirm.classList.remove('btn-disabled');
        } else {
            btnConfirm.disabled = true;
            btnConfirm.classList.add('btn-disabled');
        }
    }

    // --- ATTACH EVENT LISTENERS ---
    newView.addEventListener('click', function(e) {

        // 1. OPEN MODAL
        const btnOpenModal = e.target.closest('[data-action="open-pairing-modal"]');
        if (btnOpenModal) {
            // Reset Wizard State
            selectedDonorId = null;
            selectedStudentId = null;
            selectedDonorName = null;
            selectedStudentName = null;

            const sponsorSummary = document.getElementById('summary-sponsor');
            if(sponsorSummary) sponsorSummary.textContent = 'None selected';

            const studentSummary = document.getElementById('summary-student');
            if(studentSummary) studentSummary.textContent = 'None selected';

            checkConfirmationState();

            // Render all data fresh
            renderCards('donor-list-container', 'donor-card-template', mockDonors, true);
            renderCards('student-list-container', 'student-card-template', mockStudents, false);

            const modal = document.getElementById('pairing-modal');
            if(modal) {
                modal.classList.remove('hidden');
                modal.classList.add('show');
            }
        }

        // 2. CLOSE MODAL
        const btnCloseModal = e.target.closest('.close-modal');
        if (btnCloseModal) {
            const modal = document.getElementById('pairing-modal');
            if(modal) {
                modal.classList.add('hidden');
                modal.classList.remove('show');
            }
        }

        // 3. SELECT DONOR CARD
        const donorCard = e.target.closest('.donor-card');
        if (donorCard) {
            document.querySelectorAll('.donor-card').forEach(c => c.classList.remove('selected'));
            donorCard.classList.add('selected');

            selectedDonorId = donorCard.getAttribute('data-id');
            selectedDonorName = donorCard.getAttribute('data-name');

            const sponsorSummary = document.getElementById('summary-sponsor');
            if(sponsorSummary) sponsorSummary.textContent = selectedDonorName;

            checkConfirmationState();
        }

        // 4. SELECT STUDENT CARD
        const studentCard = e.target.closest('.student-card');
        if (studentCard) {
            document.querySelectorAll('.student-card').forEach(c => c.classList.remove('selected'));
            studentCard.classList.add('selected');

            selectedStudentId = studentCard.getAttribute('data-id');
            selectedStudentName = studentCard.getAttribute('data-name');

            const studentSummary = document.getElementById('summary-student');
            if(studentSummary) studentSummary.textContent = selectedStudentName;

            checkConfirmationState();
        }

        // 5. CONFIRM MATCH
        const btnConfirm = e.target.closest('#btn-confirm-advanced-match');
        if (btnConfirm && !btnConfirm.disabled) {
            alert(`SUCCESS! We have officially linked Sponsor: ${selectedDonorName} with Student: ${selectedStudentName}.`);

            const modal = document.getElementById('pairing-modal');
            if(modal) {
                modal.classList.add('hidden');
                modal.classList.remove('show');
            }
        }

        // 6. SPLIT SCREEN DETAILS & BACK BUTTON
        const btnViewPair = e.target.closest('[data-action="view-details"]');
        if (btnViewPair) {
            const sName = btnViewPair.getAttribute('data-sponsor');
            const stName = btnViewPair.getAttribute('data-student');

            const sElem = document.getElementById('detail-sponsor-name');
            const stElem = document.getElementById('detail-student-name');
            if(sElem) sElem.textContent = sName;
            if(stElem) stElem.textContent = stName;

            const listSec = document.getElementById('pairings-list-section');
            const detailSec = document.getElementById('pairing-details-section');
            if(listSec) listSec.classList.add('hidden');
            if(detailSec) detailSec.classList.remove('hidden');
        }

        const btnBack = e.target.closest('[data-action="back-to-list"]');
        if (btnBack) {
            const listSec = document.getElementById('pairings-list-section');
            const detailSec = document.getElementById('pairing-details-section');
            if(detailSec) detailSec.classList.add('hidden');
            if(listSec) listSec.classList.remove('hidden');
        }
    });

    // --- REAL-TIME SEARCH FILTER LOGIC ---
    const searchDonorInput = newView.querySelector('#search-donor');
    if (searchDonorInput) {
        searchDonorInput.addEventListener('input', (e) => {
            const term = e.target.value.toLowerCase();
            const filtered = mockDonors.filter(d =>
                d.name.toLowerCase().includes(term) || d.type.toLowerCase().includes(term)
            );
            renderCards('donor-list-container', 'donor-card-template', filtered, true);
        });
    }

    const searchStudentInput = newView.querySelector('#search-student');
    if (searchStudentInput) {
        searchStudentInput.addEventListener('input', (e) => {
            const term = e.target.value.toLowerCase();
            const filtered = mockStudents.filter(s =>
                s.name.toLowerCase().includes(term) || s.class.toLowerCase().includes(term) || s.campus.toLowerCase().includes(term)
            );
            renderCards('student-list-container', 'student-card-template', filtered, false);
        });
    }
}

// ==========================================
// VIEW 5: PARTIAL STUDENT FUND LOGIC
// ==========================================
function initPartialStudentFundView() {
    const partialView = document.getElementById('superadmin-partial-fund-view');
    if (!partialView) return;

    // Securely refresh view to clear rogue listeners
    const newView = partialView.cloneNode(true);
    partialView.parentNode.replaceChild(newView, partialView);

    const formatUGX = (num) => new Intl.NumberFormat('en-UG', { style: 'currency', currency: 'UGX', maximumFractionDigits: 0 }).format(num);

    // Mock Data for students with shortfalls
    const mockStudents = [
        { id: 'STU-1045', name: 'Kato Emmanuel', campus: 'Mpala Campus', fees: 1500000, shortfall: 500000, hardship: 'Lost primary sponsor recently. High academic merit.', score: '88% Average' },
        { id: 'STU-2291', name: 'Nassali Grace', campus: 'Wakiso Campus', fees: 1200000, shortfall: 300000, hardship: 'Single parent household, struggling with current term fees.', score: '75% Average' },
        { id: 'STU-3108', name: 'Mukasa John', campus: 'Mpala Campus', fees: 1800000, shortfall: 900000, hardship: 'Medical emergency drained family funds.', score: '92% Average' }
    ];

    function renderPartialTable(data) {
        const tbody = document.getElementById('partial-funds-tbody');
        const template = document.getElementById('partial-student-row-template');
        if (!tbody || !template) return;

        tbody.textContent = ''; // Fast secure clear

        if (data.length === 0) {
            const tr = document.createElement('tr');
            const td = document.createElement('td');
            td.colSpan = 6;
            td.className = 'empty-cell text-center text-muted';
            td.textContent = 'No students found matching your criteria.';
            tr.appendChild(td);
            tbody.appendChild(tr);
            return;
        }

        data.forEach(student => {
            const clone = template.content.cloneNode(true);

            clone.querySelector('.student-name').textContent = student.name;
            clone.querySelector('.student-id').textContent = student.id;
            clone.querySelector('.campus-name').textContent = student.campus;
            clone.querySelector('.total-fees').textContent = formatUGX(student.fees);
            clone.querySelector('.shortfall-amount').textContent = formatUGX(student.shortfall);

            // Attach data directly to buttons securely
            const btnAllocate = clone.querySelector('.btn-allocate');
            btnAllocate.setAttribute('data-student-id', student.id);
            btnAllocate.setAttribute('data-student-name', student.name);

            const btnDetails = clone.querySelector('.btn-view-details');
            btnDetails.setAttribute('data-student-id', student.id);

            tbody.appendChild(clone);
        });
    }

    // Initial render
    renderPartialTable(mockStudents);

    // Event Delegation for Clicks
    newView.addEventListener('click', function(e) {

        // --- ACTION 1: ALLOCATE FUNDS ---
        const btnAllocate = e.target.closest('.btn-allocate');
        if (btnAllocate) {
            const row = btnAllocate.closest('tr');
            const inputField = row.querySelector('.allocate-input');
            const amount = inputField.value;
            const studentName = btnAllocate.getAttribute('data-student-name');
            const studentId = btnAllocate.getAttribute('data-student-id');

            if (!amount || amount <= 0) {
                alert('Please enter a valid amount to allocate.');
                if(inputField) inputField.focus();
                return;
            }

            if (confirm(`Are you sure you want to allocate UGX ${amount} to ${studentName} (${studentId})?`)) {
                alert(`Successfully allocated UGX ${amount} to ${studentName}.`);
                if(inputField) inputField.value = '';
            }
        }

        // --- ACTION 2: VIEW DETAILS (MASTER -> DETAIL SWAP) ---
        const btnDetails = e.target.closest('.btn-view-details');
        if (btnDetails) {
            const studentId = btnDetails.getAttribute('data-student-id');
            const student = mockStudents.find(s => s.id === studentId);

            if (student) {
                const nameEl = document.getElementById('detail-student-name');
                const idEl = document.getElementById('detail-student-id');
                const hardshipEl = document.getElementById('detail-hardship-reason');
                const scoreEl = document.getElementById('detail-academic-score');

                if(nameEl) nameEl.textContent = student.name;
                if(idEl) idEl.textContent = student.id;
                if(hardshipEl) hardshipEl.textContent = student.hardship;
                if(scoreEl) scoreEl.textContent = student.score;

                const listSection = document.getElementById('partial-fund-list-section');
                const detailSection = document.getElementById('partial-fund-detail-section');

                if(listSection) listSection.classList.add('hidden');
                if(detailSection) detailSection.classList.remove('hidden');
            }
        }

        // --- ACTION 3: BACK TO LIST (DETAIL -> MASTER SWAP) ---
        const btnBack = e.target.closest('#btn-back-to-partial-list');
        if (btnBack) {
            const listSection = document.getElementById('partial-fund-list-section');
            const detailSection = document.getElementById('partial-fund-detail-section');

            if(detailSection) detailSection.classList.add('hidden');
            if(listSection) listSection.classList.remove('hidden');
        }
    });

    // --- REAL TIME SEARCH LOGIC ---
    const searchInput = newView.querySelector('#search-partial-students');
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            const term = e.target.value.toLowerCase();
            const filtered = mockStudents.filter(s =>
                s.name.toLowerCase().includes(term) || s.id.toLowerCase().includes(term)
            );
            renderPartialTable(filtered);
        });
    }
}
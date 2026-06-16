// ==========================================
// SUPER ADMIN MODULE
// ==========================================

// Global Listener for the SPA Router
document.addEventListener('viewLoaded', function(e) {
    if (e.detail.role === 'superadmin') {
        if (e.detail.view === 'branches') {
            initBranchesView();
        } else if (e.detail.view === 'home') {
            initHomeView();
        } else if (e.detail.view === 'add-branch') {
            initAddBranchView();
        }
    }
});

// ==========================================
// PREMIUM REUSABLE MODAL ENGINE (STRICT DOM)
// ==========================================
function showPremiumModal({ title, type = 'info', contentNode = null, contentText = null, confirmText = 'OK', cancelText = null, onConfirm = null }) {
    let modalWrapper = document.getElementById('premium-modal-wrapper');
    if (!modalWrapper) {
        modalWrapper = document.createElement('div');
        modalWrapper.id = 'premium-modal-wrapper';
        document.body.appendChild(modalWrapper);
    }

    // Pure DOM clear
    while (modalWrapper.firstChild) modalWrapper.removeChild(modalWrapper.firstChild);

    const template = document.getElementById('premium-modal-template');
    if (!template) {
        console.error("Missing premium-modal-template in HTML");
        return;
    }
    const clone = template.content.cloneNode(true);

    const overlay = clone.querySelector('.premium-modal-overlay');
    const iconContainer = clone.querySelector('.premium-modal-icon');
    const iconElement = clone.querySelector('.premium-modal-icon i');
    const titleElement = clone.querySelector('.pm-title');
    const bodyElement = clone.querySelector('.premium-modal-body');
    const cancelBtn = clone.querySelector('.pm-btn-cancel');
    const confirmBtn = clone.querySelector('.pm-btn-confirm');

    // Class manipulation instead of HTML injection
    iconContainer.classList.add(type);
    if (type === 'success') iconElement.classList.add('bi-check-circle-fill');
    else if (type === 'warning') iconElement.classList.add('bi-exclamation-triangle-fill');
    else iconElement.classList.add('bi-info-circle-fill');

    titleElement.textContent = title;

    // Inject either a DOM node or plain text
    if (contentNode) bodyElement.appendChild(contentNode);
    else if (contentText) bodyElement.textContent = contentText;

    const closeObj = {
        close: () => {
            overlay.classList.add('pm-fade-out'); // CSS handles fading!
            setTimeout(() => {
                while (modalWrapper.firstChild) modalWrapper.removeChild(modalWrapper.firstChild);
            }, 300);
        }
    };

    if (cancelText) {
        cancelBtn.textContent = cancelText;
        cancelBtn.classList.remove('hidden');
        cancelBtn.addEventListener('click', () => closeObj.close());
    }

    confirmBtn.textContent = confirmText;
    if (type === 'warning') {
        confirmBtn.classList.remove('premium-modal-btn-primary');
        confirmBtn.classList.add('premium-modal-btn-danger');
    }

    confirmBtn.addEventListener('click', () => {
        if (onConfirm) onConfirm(closeObj);
        else closeObj.close();
    });

    modalWrapper.appendChild(clone);
}


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

    const linkUsers = document.getElementById('sa-btnLinkUsers');
    if (linkUsers) {
        linkUsers.addEventListener('click', () => {
            const link = document.querySelector('.sidebar-nav a[href*="users"]');
            if(link) link.click();
        });
    }

    try {
        const branchJson = await apiGet('/superadmin/branches');
        const statTotalBranches = document.getElementById('sa-statTotalBranches');
        if(statTotalBranches) statTotalBranches.textContent = branchJson.data.length;

        const usersJson = await apiGet('/superadmin/users');
        const statTotalUsers = document.getElementById('sa-statTotalUsers');
        if(statTotalUsers) statTotalUsers.textContent = usersJson.data.length;

        const settingsJson = await apiGet('/superadmin/settings');
        const statTotalSettings = document.getElementById('sa-statTotalSettings');
        if(statTotalSettings) statTotalSettings.textContent = settingsJson.data.length;

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
                openViewMore(id);
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
            loadBranches();
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
            openViewMore(currentDetailBranchId);
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
                        openViewMore(currentDetailBranchId);
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

    loadBranches();
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
            loadView('superadmin', 'branches', mainContent);
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
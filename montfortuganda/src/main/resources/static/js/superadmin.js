/* global Swal, initErpCalendar, apiGet, apiPost, apiPut, apiMultipart, showPremiumModal, showSuccessMessage, showErrorMessage, showLoader, hideLoader, loadView, renderFetchingMessage, renderEmptyTableMessage, GlobalPagination */
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

// ==========================================
// UTILITY: FETCH AND RENDER DYNAMIC LEVELS
// ==========================================




function confirmAllocation(title, contentText, endpoint, payload, successMsg, errorMsg, inputField) {
    showPremiumModal({
        title: title,
        type: 'warning',
        contentText: contentText,
        confirmText: 'Allocate',
        cancelText: 'Cancel',
        onConfirm: async (modal) => {
            await modal.close();
            const loaderToken = showLoader('Allocating funds...');
            try {
                await apiPost(endpoint, payload);
                showSuccessMessage(successMsg);
                if (inputField) inputField.value = '';
            } catch (err) {
                showErrorMessage(errorMsg);
            } finally {
                hideLoader(loaderToken);
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
            await modal.close();
            const loaderToken = showLoader('Processing request...');
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
                hideLoader(loaderToken);
            }
        }
    });
}

document.addEventListener('viewLoaded', function(e) {
    if (e.detail.role === 'superadmin') {
        if (e.detail.view === 'home') {
            if (typeof e.detail.waitUntil === 'function') {
                e.detail.waitUntil(initHomeView());
            } else {
                void initHomeView();
            }
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
    const loaderToken = showLoader('Loading dashboard...');

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
    } finally {
        hideLoader(loaderToken);
    }
}


// ---------------------------------------------------------
// 2. BRANCHES LOGIC (SPA)
// ---------------------------------------------------------


// ---------------------------------------------------------
// 3. ADD BRANCH PAGE LOGIC (DYNAMIC TABLE & JSON)
// ---------------------------------------------------------
// ---------------------------------------------------------
// 3. ADD BRANCH PAGE LOGIC (DYNAMIC TABLE & JSON)
// ---------------------------------------------------------

// ---------------------------------------------------------
// 4. GLOBAL SYSTEM STATS LOGIC (UPGRADED)
// ---------------------------------------------------------
async function initSystemStatsView() {
    const viewContainer = document.querySelector('#superadmin-stats-view');
    if (!viewContainer) return;

    const loaderToken = showLoader('Loading system statistics...');

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
    } catch (error) {
        console.error(
            'System statistics failed to load.',
            error
        );
        showErrorMessage(
            'Could not load system statistics.'
        );
    } finally {
        hideLoader(loaderToken);
    }
}

// ---------------------------------------------------------
// 5. GLOBAL AUDIT LOGS LOGIC
// ---------------------------------------------------------
async function initAuditLogsView() {
    const viewContainer = document.querySelector('#superadmin-audit-view');
    if (!viewContainer) return;

    const loaderToken = showLoader('Loading audit logs...');

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
            clone.querySelector('.log-date').textContent = log.date && window.erpDate ? window.erpDate.formatDateTime(log.date, log.date) : log.date;
            clone.querySelector('.log-branch').textContent = log.branch;
            clone.querySelector('.log-user').textContent = log.user;
            clone.querySelector('.log-action').textContent = log.action;
            tbody.appendChild(clone);
        });
    } catch (error) {
        console.error('Audit logs failed to load.', error);
        renderEmptyTableMessage(
            tbody,
            10,
            'Failed to load audit logs.'
        );
    } finally {
        hideLoader(loaderToken);
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
            clone.querySelector('.bkp-date').textContent = bkp.date && window.erpDate ? window.erpDate.formatDateTime(bkp.date, bkp.date) : bkp.date;
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

    const loaderToken = showLoader('Loading treasury data...');

    const tbody = viewContainer.querySelector('#treasury-donors-tbody');
    if (tbody) renderFetchingMessage(tbody, 10, 'Fetching treasury data...');

    try {
        const [summaryRes, donorsRes] = await Promise.all([
            apiGet('/superadmin/scholarships/funds-summary'),
            apiGet('/superadmin/scholarships/donors')
        ]);

        /** @type {{totalRaisedUgx: number, totalSpentUgx: number, availableBalanceUgx: number, studentsSponsored: number}} */
        const summary = summaryRes.data || summaryRes;

        animateValue(document.querySelector('#treasury-total-raised'), 0, summary.totalRaisedUgx || 0, 1500, true);
        animateValue(document.querySelector('#treasury-total-spent'), 0, summary.totalSpentUgx || 0, 1800, true);
        animateValue(document.querySelector('#treasury-available'), 0, summary.availableBalanceUgx || 0, 2000, true);
        animateValue(document.querySelector('#treasury-sponsored'), 0, summary.studentsSponsored || 0, 1200, false);

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

        if (tbody) {
            renderEmptyTableMessage(
                tbody,
                10,
                'Failed to load treasury data.'
            );
        }

        showErrorMessage("Failed to load global treasury data.");
    } finally {
        hideLoader(loaderToken);
    }
}
// ==========================================
// VIEW 2: SCHOLARSHIP APPLICATIONS (GLOBAL SEARCH)
// ==========================================
async function initScholarshipsApplicationsView() {
    const viewContainer = document.querySelector('#superadmin-global-search-view');
    if (!viewContainer) return;

    const loaderToken = showLoader('Loading scholarship applications...');

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

        if (tbody) {
            renderEmptyTableMessage(
                tbody,
                10,
                'Failed to load scholarship applications.'
            );
        }

        showErrorMessage(
            'Failed to load scholarship applications.'
        );
    } finally {
        hideLoader(loaderToken);
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
    const loaderToken = showLoader('Loading branch scholarship demands...');

    if (branchTable) {
        renderFetchingMessage(
            branchTable,
            10,
            'Fetching branch demands...'
        );
    }

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
    }).catch(e => {
        console.error("Failed to load demands:", e);

        if (branchTable) {
            renderEmptyTableMessage(
                branchTable,
                10,
                'Failed to load branch demands.'
            );
        }
    }).finally(() => {
        hideLoader(loaderToken);
    });
    } catch(err) {
        console.error("Failed to start branch demands request:", err);

        if (branchTable) {
            renderEmptyTableMessage(
                branchTable,
                10,
                'Failed to load branch demands.'
            );
        }

        hideLoader(loaderToken);
    }

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
            clone.querySelector('.tx-date').textContent = tx.date && window.erpDate ? window.erpDate.formatDateTime(tx.date, tx.date) : tx.date;
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
                    date: tx.createdAt && window.erpDate ? window.erpDate.formatDate(tx.createdAt, 'Just now') : (tx.createdAt || 'Just now'),
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

    async function loadActivePairings() {
        const loaderToken = showLoader('Loading active sponsorships...');
        const tbody = viewContainer.querySelector('tbody');

        if (tbody) {
            renderFetchingMessage(
                tbody,
                10,
                'Fetching active sponsorships...'
            );
        }

        try {
            const res =
                await apiGet(
                    '/superadmin/scholarships/active-sponsorships'
                );

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
        } catch (error) {
            console.error(
                "Failed to load active sponsorships:",
                error
            );

            if (tbody) {
                renderEmptyTableMessage(
                    tbody,
                    10,
                    'Failed to load active sponsorships.'
                );
            }
        } finally {
            hideLoader(loaderToken);
        }
    }

    void loadActivePairings();

    let selectedDonorId = '', selectedStudentId = '';
    let selectedDonorName = null, selectedStudentName = null;
    let selectedStudentBranchId = null;
    let selectedStudentShortfallUgx = 0;

    function resetWizardSelection() {
        selectedDonorId = ''; selectedStudentId = '';
        selectedDonorName = null; selectedStudentName = null;
        selectedStudentBranchId = null;
        selectedStudentShortfallUgx = 0;
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

                const validDonors = liveDonors.filter(d => (d.amountReceivedUgx - d.amountSpentUgx) > 0);

                const donorPagination = new GlobalPagination({
                    data: validDonors,
                    itemsPerPage: 25,
                    elements: {
                        startId: 'donor-page-start', endId: 'donor-page-end', totalId: 'donor-total-entries',
                        prevBtnId: 'btn-donor-prev', nextBtnId: 'btn-donor-next',
                        numbersContainerId: 'donor-pagination-numbers', templateId: 'page-number-template'
                    },
                    renderCallback: (pageData) => {
                        const donorList = document.getElementById('donor-list-container');
                        const donorTemplate = document.getElementById('donor-card-template');
                        if (donorList && donorTemplate) {
                            donorList.textContent = '';
                            pageData.forEach(d => {
                                const available = d.amountReceivedUgx - d.amountSpentUgx;
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
                    }
                });
                donorPagination.render();

                const studentPagination = new GlobalPagination({
                    data: liveStudents,
                    itemsPerPage: 25,
                    elements: {
                        startId: 'student-page-start', endId: 'student-page-end', totalId: 'student-total-entries',
                        prevBtnId: 'btn-student-prev', nextBtnId: 'btn-student-next',
                        numbersContainerId: 'student-pagination-numbers', templateId: 'page-number-template'
                    },
                    renderCallback: (pageData) => {
                        const studentList = document.getElementById('student-list-container');
                        const studentTemplate = document.getElementById('student-card-template');
                        if (studentList && studentTemplate) {
                            studentList.textContent = '';
                            pageData.forEach(s => {
                                const clone = studentTemplate.content.cloneNode(true);
                                const card = clone.querySelector('.selectable-card');
                                card.classList.add('btn-select-student');
                                card.setAttribute('data-id', s.id);
                                card.setAttribute('data-name', s.studentName);
                                card.setAttribute('data-branch-id', s.branchId || s.campusId || '');
                                card.setAttribute('data-shortfall', s.currentShortfallUgx || s.shortfallUgx || 0);

                                clone.querySelector('.card-title').textContent = s.studentName;
                                clone.querySelector('.card-subtitle').textContent = `Shortfall: ${formatUGX(s.currentShortfallUgx || s.shortfallUgx || 0)}`;
                                studentList.appendChild(clone);
                            });
                        }
                    }
                });
                studentPagination.render();

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
                selectedStudentId = data.id;
                selectedStudentName = data.name;
                selectedStudentBranchId = Number(studentCard.getAttribute('data-branch-id')) || null;
                selectedStudentShortfallUgx = Number(studentCard.getAttribute('data-shortfall')) || 0;
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
                if (!selectedDonorId) {
                    throw new Error('Select a donor before confirming the mapping.');
                }

                if (!selectedStudentBranchId) {
                    throw new Error('The selected student branch could not be resolved.');
                }

                const amountUgx = selectedStudentShortfallUgx;

                if (!(amountUgx > 0)) {
                    throw new Error('The selected student has no outstanding scholarship shortfall.');
                }

                await apiPost('/superadmin/scholarships/allocate-student', {
                    branchId: selectedStudentBranchId,
                    studentId: parseInt(selectedStudentId, 10),
                    donationId: parseInt(selectedDonorId, 10),
                    amountUgx,
                    term: 'Term 1',
                    academicYear: '2026/2027'
                });

                showSuccessMessage(`SUCCESS! We paired Sponsor: ${selectedDonorName} with Student: ${selectedStudentName}.`);

                // Return to Table View
                if(wizardSection) wizardSection.classList.add('hidden');
                if(mainHeader) mainHeader.classList.remove('hidden');
                if(listSection) listSection.classList.remove('hidden');

                void loadActivePairings();

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
        const loaderToken = showLoader('Loading student sponsorships...');
        const tbody = viewContainer.querySelector('#partial-fund-tbody');

        if (tbody) {
            window.renderFetchingMessage(
                tbody,
                10,
                'Fetching sponsorships...'
            );
        }

        try {
            const [summaryResult, studentsResult] =
                await Promise.allSettled([
                    apiGet('/superadmin/scholarships/funds-summary'),
                    apiGet('/superadmin/scholarships/pending-students')
                ]);

            let summary = {
                availableBalanceUgx: 0,
                totalSpentUgx: 0
            };

            if (summaryResult.status === 'fulfilled') {
                summary =
                    summaryResult.value?.data ||
                    summaryResult.value ||
                    summary;
            } else {
                console.warn(
                    'Could not load funds summary.',
                    summaryResult.reason
                );
            }

            if (studentsResult.status === 'fulfilled') {
                const studentsResponse = studentsResult.value;
                allStudents = Array.isArray(studentsResponse)
                    ? studentsResponse
                    : (studentsResponse?.data || []);
            } else {
                console.warn(
                    'Could not load pending students.',
                    studentsResult.reason
                );
                allStudents = [];
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
        } catch (error) {
            console.error(
                'Partial student funding data failed to load.',
                error
            );
            allStudents = [];
            renderTable();
        } finally {
            hideLoader(loaderToken);
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
document.addEventListener('viewLoaded', function() {
    if (typeof createErpCalendar === 'function') {
        createErpCalendar('input[type="date"]',
            { minYear: 2022 });
    }
});

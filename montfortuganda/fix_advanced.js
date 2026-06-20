const fs = require('fs');
const filePath = 'D:/Java/montforterp/ERP-Java/montfortuganda/src/main/resources/static/js/superadmin.js';
let content = fs.readFileSync(filePath, 'utf8');

// 1. Bulk Distribution API Get
content = content.replace(
    /let currentHistoryData = \[\];[\s\S]*?function renderHistoryTable\(dataArray\)/,
    let currentHistoryData = [];
    const formatUGX = (num) => new Intl.NumberFormat('en-UG', { style: 'currency', currency: 'UGX', maximumFractionDigits: 0 }).format(num);

    // ADD API FETCH FOR DEMANDS
    apiGet('/superadmin/scholarships/demands').then(res => {
        const demands = res.data;
        let totalDemands = 0;
        demands.forEach(d => totalDemands += d.totalRequestedAmountUgx);
        const tDom = document.querySelector('#superadmin-bulk-distribution-view .fund-card:nth-child(2) h2');
        if(tDom) tDom.textContent = formatUGX(totalDemands);
        const branchTable = document.querySelector('#superadmin-bulk-distribution-view tbody');
        if(branchTable && demands.length > 0) {
            branchTable.textContent = '';
            demands.forEach(d => {
                const tr = document.createElement('tr');
                tr.innerHTML = \<td><div class="table-branch-info"><div class="branch-icon"><i class="bi bi-building"></i></div><div><h4 class="branch-name">Branch \</h4><span class="branch-code text-muted">BR-00\</span></div></div></td><td><strong>\</strong></td><td><strong>UGX 0</strong></td><td><div class="input-group"><span class="input-group-text">UGX</span><input type="number" class="form-control allocate-input" placeholder="Enter amount..."></div></td><td><div class="action-buttons"><button class="btn-allocate" data-branch-id="\"><i class="bi bi-send"></i> Allocate</button><button class="btn-history" data-branch-id="\" data-branch-name="Branch \"><i class="bi bi-clock-history"></i> History</button></div></td>\;
                branchTable.appendChild(tr);
            });
        }
    }).catch(e => console.error(e));

    function renderHistoryTable(dataArray)
);

// 2. 1-to-1 API Get
content = content.replace(
    /function initOneToOneSponsorshipView\(\) \{[\s\S]*?const viewContainer = document.querySelector\('#superadmin-1to1-view'\);\s*if \(!viewContainer\) return;/,
    unction initOneToOneSponsorshipView() {
    const viewContainer = document.querySelector('#superadmin-1to1-view');
    if (!viewContainer) return;

    apiGet('/superadmin/scholarships/active-sponsorships').then(res => {
        const active = res.data;
        const tbody = viewContainer.querySelector('tbody');
        if(tbody && active.length > 0) {
            tbody.textContent = '';
            active.forEach(a => {
                const tr = document.createElement('tr');
                tr.innerHTML = \<td><div class="donor-info-cell"><div class="donor-icon"><i class="bi bi-building"></i></div><div><h4 class="donor-name">\</h4><span class="donor-id text-muted">\</span></div></div></td><td><div class="student-info-cell"><div class="student-avatar"><i class="bi bi-person"></i></div><div><h4 class="student-name">\</h4><span class="student-class text-muted">ID: \</span></div></div></td><td><strong>\</strong></td><td><span class="badge-completed">\</span></td><td><button class="btn-outline-sm view-profile-btn" data-id="\"><i class="bi bi-eye"></i> View Profile</button></td>\;
                tbody.appendChild(tr);
            });
        }
    }).catch(e => console.error(e));
);

// 3. Partial Fund API Get
content = content.replace(
    /const studentsRes = await apiGet\('\/superadmin\/scholarships\/pending-students'\);\s*liveStudents = studentsRes\.data\.map\([\s\S]*?\);/,
    const studentsRes = await apiGet('/superadmin/scholarships/pending-students');
        const rawData = studentsRes.data;
        
        let pendingCount = rawData.length;
        let totalAvailable = 85000000; // Mock treasury value for now
        let totalDisbursed = 15500000; // Mock disbursed value for now

        const formatUGX = (num) => new Intl.NumberFormat('en-UG', { style: 'currency', currency: 'UGX', maximumFractionDigits: 0 }).format(num);

        const cardAvailable = viewContainer.querySelector('.fund-card:nth-child(1) h2');
        if(cardAvailable) cardAvailable.textContent = formatUGX(totalAvailable);
        
        const cardPending = viewContainer.querySelector('.fund-card:nth-child(2) h2');
        if(cardPending) cardPending.textContent = pendingCount + ' Students';

        liveStudents = rawData.map(s => ({
            id: s.studentId, name: s.studentName, campus: 'Branch ' + s.campus,
            shortfall: s.shortfallUgx
        }));
);

// 4. Bulk alloc confirm fix
content = content.replace(
    /if \(confirm\(\Allocate UGX \$\{amount\} to \$\{branchName\}\?\\)\) \{[\s\S]*?alert\(\Success! Allocated UGX \$\{amount\} to \$\{branchName\}\.\\);[\s\S]*?finally \{\s*hideLoader\(\);\s*\}\s*\}/,
    showPremiumModal({
                title: 'Confirm Allocation',
                type: 'warning',
                contentText: \Allocate UGX \ to \?\,
                confirmText: 'Allocate',
                cancelText: 'Cancel',
                onConfirm: async (modal) => {
                    modal.close();
                    showLoader();
                    try {
                        await apiPost('/superadmin/scholarships/allocate/branch', {
                            branchId: parseInt(branchId),
                            amountUgx: parseFloat(amount),
                            term: 'Term 1',
                            academicYear: '2024'
                        });
                        showSuccessMessage(\Success! Allocated UGX \ to \.\);
                        inputField.value = '';
                    } catch (err) {
                        showErrorMessage("Treasury Error: Insufficient funds or server error.");
                    } finally {
                        hideLoader();
                    }
                }
            });
);

// 5. Partial alloc confirm fix
content = content.replace(
    /if \(confirm\(\Allocate UGX \$\{amount\} to \$\{studentName\}\?\\)\) \{[\s\S]*?alert\(\Successfully allocated UGX \$\{amount\} to \$\{studentName\}\.\\);[\s\S]*?finally \{\s*hideLoader\(\);\s*\}\s*\}/,
    showPremiumModal({
                title: 'Confirm Allocation',
                type: 'warning',
                contentText: \Allocate UGX \ to \?\,
                confirmText: 'Allocate',
                cancelText: 'Cancel',
                onConfirm: async (modal) => {
                    modal.close();
                    showLoader();
                    try {
                        await apiPost('/superadmin/scholarships/allocate/student', {
                            branchId: parseInt(campusId),
                            studentId: parseInt(studentId),
                            amountUgx: parseFloat(amount),
                            term: 'Term 1',
                            academicYear: '2024'
                        });
                        showSuccessMessage(\Successfully allocated UGX \ to \.\);
                        inputField.value = '';
                    } catch(err) {
                        showErrorMessage("Treasury Error: Insufficient funds.");
                    } finally {
                        hideLoader();
                    }
                }
            });
);

fs.writeFileSync(filePath, content);
console.log('Advanced fixes applied!');

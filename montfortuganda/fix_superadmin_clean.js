const fs = require('fs');
const filePath = 'D:/Java/montforterp/ERP-Java/montfortuganda/src/main/resources/static/js/superadmin.js';
let content = fs.readFileSync(filePath, 'utf8');

// 1. Router string
content = content.replace(
    "} else if (e.detail.view === 'scholarships-applications') {",
    "} else if (e.detail.view === 'scholarships-global-search') {"
);

// 2. innerHTML empty states
content = content.split("tbody.innerHTML = '<tr><td colspan=\"5\" class=\"empty-table-cell\">No transactions found.</td></tr>';").join(
    "const tr = document.createElement('tr'); const td = document.createElement('td'); td.colSpan = 5; td.className = 'empty-table-cell'; td.textContent = 'No transactions found.'; tr.appendChild(td); tbody.appendChild(tr);"
);

content = content.split("tbody.innerHTML = '<tr><td colspan=\"6\" class=\"empty-cell\">No students found.</td></tr>';").join(
    "const tr = document.createElement('tr'); const td = document.createElement('td'); td.colSpan = 6; td.className = 'empty-cell'; td.textContent = 'No students found.'; tr.appendChild(td); tbody.appendChild(tr);"
);

content = content.split("logTbody.innerHTML = '<tr><td colspan=\"3\" style=\"text-align:center; color:#94a3b8; padding: 20px;\">No recent activity found.</td></tr>';").join(
    "const tr = document.createElement('tr'); const td = document.createElement('td'); td.colSpan = 3; td.style.textAlign = 'center'; td.style.color = '#94a3b8'; td.style.padding = '20px'; td.textContent = 'No recent activity found.'; tr.appendChild(td); logTbody.appendChild(tr);"
);

// 3. pendingStatsList innerHTML
let oldPending = "row.innerHTML = <span class=\"text-bold\"></span><span class=\"badge-pending\"> Pending</span>;\n                pendingStatsList.appendChild(row);";
let newPending = "const spanName = document.createElement('span'); spanName.className = 'text-bold'; spanName.textContent = branchName; const spanBadge = document.createElement('span'); spanBadge.className = 'badge-pending'; spanBadge.textContent = branchPendingCounts[branchName] + ' Pending'; row.appendChild(spanName); row.appendChild(spanBadge); pendingStatsList.appendChild(row);";
content = content.replace(oldPending, newPending);

// 4. Alerts and Confirms
content = content.split("if (!amount || amount <= 0) return alert('Please enter a valid amount.');").join(
    "if (!amount || amount <= 0) return showErrorMessage('Please enter a valid amount.');"
);

content = content.split("alert('Failed to load history');").join(
    "showErrorMessage('Failed to load history');"
);

content = content.split("alert(\"Failed to load DB lists.\");").join(
    "showErrorMessage(\"Failed to load DB lists.\");"
);

content = content.split("alert(SUCCESS! We have officially linked Sponsor:  with Student: .);").join(
    "showSuccessMessage(SUCCESS! We have officially linked Sponsor:  with Student: .);"
);

content = content.split("alert(\"Database Error: Insufficient funds or invalid student.\");").join(
    "showErrorMessage(\"Database Error: Insufficient funds or invalid student.\");"
);

content = content.split("alert(Successfully allocated UGX  to .);").join(
    "showSuccessMessage(Successfully allocated UGX  to .);"
);

content = content.split("alert(\"Treasury Error: Insufficient funds.\");").join(
    "showErrorMessage(\"Treasury Error: Insufficient funds.\");"
);

content = content.split("alert(Success! Allocated UGX  to .);").join(
    "showSuccessMessage(Success! Allocated UGX  to .);"
);

content = content.split("alert(\"Treasury Error: Insufficient funds or server error.\");").join(
    "showErrorMessage(\"Treasury Error: Insufficient funds or server error.\");"
);

// 5. Confirms replacement - replacing entire blocks
let bulkOldConfirm = "if (confirm(Allocate UGX  to ?)) {\n                showLoader();\n                try {\n                    await apiPost('/superadmin/scholarships/allocate/branch', {\n                        branchId: parseInt(branchId),\n                        amountUgx: parseFloat(amount),\n                        term: 'Term 1',\n                        academicYear: '2024'\n                    });\n                    showSuccessMessage(Success! Allocated UGX  to .);\n                    inputField.value = '';\n                } catch (err) {\n                    showErrorMessage(\"Treasury Error: Insufficient funds or server error.\");\n                } finally {\n                    hideLoader();\n                }\n            }";
let bulkNewConfirm = "showPremiumModal({ title: 'Confirm Allocation', type: 'warning', contentText: Allocate UGX  to ?, confirmText: 'Allocate', cancelText: 'Cancel', onConfirm: async (modal) => { modal.close(); showLoader(); try { await apiPost('/superadmin/scholarships/allocate/branch', { branchId: parseInt(branchId), amountUgx: parseFloat(amount), term: 'Term 1', academicYear: '2024' }); showSuccessMessage(Success! Allocated UGX  to .); inputField.value = ''; } catch (err) { showErrorMessage(\"Treasury Error: Insufficient funds or server error.\"); } finally { hideLoader(); } } });";
content = content.replace(bulkOldConfirm, bulkNewConfirm);

let studentOldConfirm = "if (confirm(Allocate UGX  to ?)) {\n                showLoader();\n                try {\n                    await apiPost('/superadmin/scholarships/allocate/student', {\n                        branchId: parseInt(campusId),\n                        studentId: parseInt(studentId),\n                        amountUgx: parseFloat(amount),\n                        term: 'Term 1',\n                        academicYear: '2024'\n                    });\n                    showSuccessMessage(Successfully allocated UGX  to .);\n                    inputField.value = '';\n                } catch(err) {\n                    showErrorMessage(\"Treasury Error: Insufficient funds.\");\n                } finally {\n                    hideLoader();\n                }\n            }";
let studentNewConfirm = "showPremiumModal({ title: 'Confirm Allocation', type: 'warning', contentText: Allocate UGX  to ?, confirmText: 'Allocate', cancelText: 'Cancel', onConfirm: async (modal) => { modal.close(); showLoader(); try { await apiPost('/superadmin/scholarships/allocate/student', { branchId: parseInt(campusId), studentId: parseInt(studentId), amountUgx: parseFloat(amount), term: 'Term 1', academicYear: '2024' }); showSuccessMessage(Successfully allocated UGX  to .); inputField.value = ''; } catch(err) { showErrorMessage(\"Treasury Error: Insufficient funds.\"); } finally { hideLoader(); } } });";
content = content.replace(studentOldConfirm, studentNewConfirm);

// Adding apiGet calls for main pages (View 3, 4, 5)
// Let's modify initBulkDistributionView to fetch demands.
let bulkFetchInject = "let currentHistoryData = [];\n    const formatUGX = (num) => new Intl.NumberFormat('en-UG', { style: 'currency', currency: 'UGX', maximumFractionDigits: 0 }).format(num);\n\n    // ADD API FETCH FOR DEMANDS\n    apiGet('/superadmin/scholarships/demands').then(res => {\n        const demands = res.data;\n        let totalDemands = 0;\n        demands.forEach(d => totalDemands += d.totalRequestedAmountUgx);\n        const tDom = document.querySelector('#superadmin-bulk-distribution-view .fund-card:nth-child(2) h2');\n        if(tDom) tDom.textContent = formatUGX(totalDemands);\n        const branchTable = document.querySelector('#superadmin-bulk-distribution-view tbody');\n        if(branchTable && demands.length > 0) {\n            branchTable.textContent = '';\n            demands.forEach(d => {\n                const tr = document.createElement('tr');\n                tr.innerHTML = <td><div class=\"table-branch-info\"><div class=\"branch-icon\"><i class=\"bi bi-building\"></i></div><div><h4 class=\"branch-name\">Branch </h4><span class=\"branch-code text-muted\">BR-00</span></div></div></td><td><strong></strong></td><td><strong>UGX 0</strong></td><td><div class=\"input-group\"><span class=\"input-group-text\">UGX</span><input type=\"number\" class=\"form-control allocate-input\" placeholder=\"Enter amount...\"></div></td><td><div class=\"action-buttons\"><button class=\"btn-allocate\" data-branch-id=\"\"><i class=\"bi bi-send\"></i> Allocate</button><button class=\"btn-history\" data-branch-id=\"\" data-branch-name=\"Branch \"><i class=\"bi bi-clock-history\"></i> History</button></div></td>;\n                branchTable.appendChild(tr);\n            });\n        }\n    }).catch(e => console.error(e));";
content = content.replace("let currentHistoryData = [];\n    const formatUGX = (num) => new Intl.NumberFormat('en-UG', { style: 'currency', currency: 'UGX', maximumFractionDigits: 0 }).format(num);", bulkFetchInject);

// Let's modify initOneToOneSponsorshipView to fetch sponsorships
let sponsorFetchInject = "const viewContainer = document.querySelector('#superadmin-1to1-view');\n    if (!viewContainer) return;\n\n    apiGet('/superadmin/scholarships/active-sponsorships').then(res => {\n        const active = res.data;\n        const tbody = viewContainer.querySelector('tbody');\n        if(tbody && active.length > 0) {\n            tbody.textContent = '';\n            active.forEach(a => {\n                const tr = document.createElement('tr');\n                tr.innerHTML = <td><div class=\"donor-info-cell\"><div class=\"donor-icon\"><i class=\"bi bi-building\"></i></div><div><h4 class=\"donor-name\"></h4><span class=\"donor-id text-muted\"></span></div></div></td><td><div class=\"student-info-cell\"><div class=\"student-avatar\"><i class=\"bi bi-person\"></i></div><div><h4 class=\"student-name\"></h4><span class=\"student-class text-muted\">ID: </span></div></div></td><td><strong></strong></td><td><span class=\"badge-completed\"></span></td><td><button class=\"btn-outline-sm view-profile-btn\" data-id=\"\"><i class=\"bi bi-eye\"></i> View Profile</button></td>;\n                tbody.appendChild(tr);\n            });\n        }\n    }).catch(e => console.error(e));";
content = content.replace("const viewContainer = document.querySelector('#superadmin-1to1-view');\n    if (!viewContainer) return;", sponsorFetchInject);

fs.writeFileSync(filePath, content);
console.log('Fixed js successfully!');

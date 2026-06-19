const fs = require('fs');
const filePath = 'src/main/resources/static/js/superadmin.js';
let content = fs.readFileSync(filePath, 'utf8');

// 1. Fix router string
content = content.replace("e.detail.view === 'scholarships-applications'", "e.detail.view === 'scholarships-global-search'");

// 2. Fix pendingStatsList innerHTML
content = content.replace(
    'row.innerHTML = <span class="text-bold">${branchName}</span><span class="badge-pending">${branchPendingCounts[branchName]} Pending</span>;',
    const spanName = document.createElement('span');
                spanName.className = 'text-bold';
                spanName.textContent = branchName;
                const spanBadge = document.createElement('span');
                spanBadge.className = 'badge-pending';
                spanBadge.textContent = branchPendingCounts[branchName] + ' Pending';
                row.appendChild(spanName);
                row.appendChild(spanBadge);
);

// We need to use regex for multi-line replaces easily or substring matches
content = content.replace(
    /row\.innerHTML = <span class="text-bold">\$\{branchName\}<\/span><span class="badge-pending">\$\{branchPendingCounts\[branchName\]\} Pending<\/span>;/,
    const spanName = document.createElement('span');
                spanName.className = 'text-bold';
                spanName.textContent = branchName;
                const spanBadge = document.createElement('span');
                spanBadge.className = 'badge-pending';
                spanBadge.textContent = branchPendingCounts[branchName] + ' Pending';
                row.appendChild(spanName);
                row.appendChild(spanBadge);
);

content = content.replace(
    /tbody\.innerHTML = '<tr><td colspan="5" class="empty-table-cell">No transactions found\.<\/td><\/tr>';/g,
    const tr = document.createElement('tr');
            const td = document.createElement('td');
            td.colSpan = 5;
            td.className = 'empty-table-cell';
            td.textContent = 'No transactions found.';
            tr.appendChild(td);
            tbody.appendChild(tr);
);

content = content.replace(
    /logTbody\.innerHTML = '<tr><td colspan="3" style="text-align:center; color:#94a3b8; padding: 20px;">No recent activity found\.<\/td><\/tr>';/g,
    const tr = document.createElement('tr');
                            const td = document.createElement('td');
                            td.colSpan = 3;
                            td.style.textAlign = 'center';
                            td.style.color = '#94a3b8';
                            td.style.padding = '20px';
                            td.textContent = 'No recent activity found.';
                            tr.appendChild(td);
                            logTbody.appendChild(tr);
);

content = content.replace(
    /tbody\.innerHTML = '<tr><td colspan="6" class="empty-cell">No students found\.<\/td><\/tr>';/g,
    const tr = document.createElement('tr');
            const td = document.createElement('td');
            td.colSpan = 6;
            td.className = 'empty-cell';
            td.textContent = 'No students found.';
            tr.appendChild(td);
            tbody.appendChild(tr);
);

// Replace bulk alloc alerts
content = content.replace(
    /if \(!amount \|\| amount <= 0\) return alert\('Please enter a valid amount\.'\);/,
    if (!amount || amount <= 0) return showErrorMessage('Please enter a valid amount.');
);

content = content.replace(
    /if \(confirm\(\Allocate UGX \$\{amount\} to \$\{branchName\}\?\\)\) \{/,
    showPremiumModal({
                title: 'Confirm Allocation',
                type: 'warning',
                contentText: \Allocate UGX \$\{amount\} to \$\{branchName\}?\,
                confirmText: 'Allocate',
                cancelText: 'Cancel',
                onConfirm: async (modal) => {
                    modal.close();
);

content = content.replace(
    /alert\(\Success! Allocated UGX \$\{amount\} to \$\{branchName\}\.\\);/,
    showSuccessMessage(\Success! Allocated UGX \$\{amount\} to \$\{branchName\}.\);
);

content = content.replace(
    /alert\("Treasury Error: Insufficient funds or server error\."\);/,
    showErrorMessage("Treasury Error: Insufficient funds or server error.");
);

// Note: we need to balance the closing braces for showPremiumModal. 
// The original had:
// } catch (err) { ... } finally { hideLoader(); } } }
// We need:
// } catch (err) { ... } finally { hideLoader(); } } }); }
content = content.replace(
    /hideLoader\(\);\s*\}\s*\}\s*\}\s*\/\/\s*---\s*ACTION 2/,
    hideLoader();
                    }
                }
            });
        }
        
        // --- ACTION 2
);


// Replace bulk alloc history alert
content = content.replace(
    /alert\('Failed to load history'\);/,
    showErrorMessage('Failed to load history');
);

// Replace 1-to-1 load DB error
content = content.replace(
    /alert\("Failed to load DB lists\."\);/,
    showErrorMessage("Failed to load DB lists.");
);

// Replace 1-to-1 save error
content = content.replace(
    /alert\(\SUCCESS! We have officially linked Sponsor: \$\{selectedDonorName\} with Student: \$\{selectedStudentName\}\.\\);/,
    showSuccessMessage(\SUCCESS! We have officially linked Sponsor: \$\{selectedDonorName\} with Student: \$\{selectedStudentName\}.\);
);

content = content.replace(
    /alert\("Database Error: Insufficient funds or invalid student\."\);/,
    showErrorMessage("Database Error: Insufficient funds or invalid student.");
);

// Replace partial alloc alerts
content = content.replace(
    /if \(!amount \|\| amount <= 0\) return alert\('Please enter a valid amount\.'\);/,
    if (!amount || amount <= 0) return showErrorMessage('Please enter a valid amount.');
);

content = content.replace(
    /if \(confirm\(\Allocate UGX \$\{amount\} to \$\{studentName\}\?\\)\) \{/,
    showPremiumModal({
                title: 'Confirm Allocation',
                type: 'warning',
                contentText: \Allocate UGX \$\{amount\} to \$\{studentName\}?\,
                confirmText: 'Allocate',
                cancelText: 'Cancel',
                onConfirm: async (modal) => {
                    modal.close();
);

content = content.replace(
    /alert\(\Successfully allocated UGX \$\{amount\} to \$\{studentName\}\.\\);/,
    showSuccessMessage(\Successfully allocated UGX \$\{amount\} to \$\{studentName\}.\);
);

content = content.replace(
    /alert\("Treasury Error: Insufficient funds\."\);/,
    showErrorMessage("Treasury Error: Insufficient funds.");
);

content = content.replace(
    /hideLoader\(\);\s*\}\s*\}\s*\}\s*\}\);/,
    hideLoader();
                    }
                }
            });
        }
    });
);

fs.writeFileSync(filePath, content);
console.log('Fixed js!');

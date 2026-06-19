import re

file_path = r'D:\Java\montforterp\ERP-Java\montfortuganda\src\main\resources\static\js\superadmin.js'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Fix router string
content = content.replace("e.detail.view === 'scholarships-applications'", "e.detail.view === 'scholarships-global-search'")

# 2. Fix pendingStatsList innerHTML violation in initScholarshipsApplicationsView
old_pending = """row.innerHTML = <span class="text-bold"></span><span class="badge-pending"> Pending</span>;
                pendingStatsList.appendChild(row);"""
new_pending = """const spanName = document.createElement('span');
                spanName.className = 'text-bold';
                spanName.textContent = branchName;
                const spanBadge = document.createElement('span');
                spanBadge.className = 'badge-pending';
                spanBadge.textContent = branchPendingCounts[branchName] + ' Pending';
                row.appendChild(spanName);
                row.appendChild(spanBadge);
                pendingStatsList.appendChild(row);"""
content = content.replace(old_pending, new_pending)

# 3. Fix innerHTML empty state in history
old_empty_history = """tbody.innerHTML = '<tr><td colspan="5" class="empty-table-cell">No transactions found.</td></tr>';"""
new_empty_history = """const tr = document.createElement('tr');
              const td = document.createElement('td');
              td.colSpan = 5;
              td.className = 'empty-table-cell';
              td.textContent = 'No transactions found.';
              tr.appendChild(td);
              tbody.appendChild(tr);"""
content = content.replace(old_empty_history, new_empty_history)

# 4. Fix innerHTML empty state in audit logs
old_audit_empty = """logTbody.innerHTML = '<tr><td colspan="3" style="text-align:center; color:#94a3b8; padding: 20px;">No recent activity found.</td></tr>';"""
new_audit_empty = """const tr = document.createElement('tr');
                            const td = document.createElement('td');
                            td.colSpan = 3;
                            td.style.textAlign = 'center';
                            td.style.color = '#94a3b8';
                            td.style.padding = '20px';
                            td.textContent = 'No recent activity found.';
                            tr.appendChild(td);
                            logTbody.appendChild(tr);"""
content = content.replace(old_audit_empty, new_audit_empty)

# 5. Fix innerHTML empty state in partial fund
old_partial_empty = """tbody.innerHTML = '<tr><td colspan="6" class="empty-cell">No students found.</td></tr>';"""
new_partial_empty = """const tr = document.createElement('tr');
            const td = document.createElement('td');
            td.colSpan = 6;
            td.className = 'empty-cell';
            td.textContent = 'No students found.';
            tr.appendChild(td);
            tbody.appendChild(tr);"""
content = content.replace(old_partial_empty, new_partial_empty)

# 6. Fix alerts and confirms in bulk distribution
old_bulk_alloc = """if (!amount || amount <= 0) return alert('Please enter a valid amount.');

            if (confirm(Allocate UGX  to ?)) {
                showLoader();
                try {
                    await apiPost('/superadmin/scholarships/allocate/branch', {
                        branchId: parseInt(branchId),
                        amountUgx: parseFloat(amount),
                        term: 'Term 1',
                        academicYear: '2024'
                    });
                    alert(Success! Allocated UGX  to .);
                    inputField.value = '';
                } catch (err) {
                    alert("Treasury Error: Insufficient funds or server error.");
                } finally {
                    hideLoader();
                }
            }"""
new_bulk_alloc = """if (!amount || amount <= 0) return showErrorMessage('Please enter a valid amount.');

            showPremiumModal({
                title: 'Confirm Allocation',
                type: 'warning',
                contentText: Allocate UGX  to ?,
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
                        showSuccessMessage(Success! Allocated UGX  to .);
                        inputField.value = '';
                    } catch (err) {
                        showErrorMessage("Treasury Error: Insufficient funds or server error.");
                    } finally {
                        hideLoader();
                    }
                }
            });"""
content = content.replace(old_bulk_alloc, new_bulk_alloc)

# 7. Fix alerts in bulk distribution history
old_bulk_hist_err = """} catch (err) {
                alert('Failed to load history');"""
new_bulk_hist_err = """} catch (err) {
                showErrorMessage('Failed to load history');"""
content = content.replace(old_bulk_hist_err, new_bulk_hist_err)

# 8. Fix alerts in 1 to 1 load donors
old_1to1_err1 = """} catch (err) {
                alert("Failed to load DB lists.");"""
new_1to1_err1 = """} catch (err) {
                showErrorMessage("Failed to load DB lists.");"""
content = content.replace(old_1to1_err1, new_1to1_err1)

# 9. Fix alerts in 1 to 1 save pairing
old_1to1_save = """alert(SUCCESS! We have officially linked Sponsor:  with Student: .);
                document.getElementById('pairing-modal').classList.add('hidden');
            } catch(err) {
                alert("Database Error: Insufficient funds or invalid student.");"""
new_1to1_save = """showSuccessMessage(SUCCESS! We have officially linked Sponsor:  with Student: .);
                document.getElementById('pairing-modal').classList.add('hidden');
            } catch(err) {
                showErrorMessage("Database Error: Insufficient funds or invalid student.");"""
content = content.replace(old_1to1_save, new_1to1_save)

# 10. Fix alerts in partial fund alloc
old_partial_alloc = """if (!amount || amount <= 0) return alert('Please enter a valid amount.');

            if (confirm(Allocate UGX  to ?)) {
                showLoader();
                try {
                    await apiPost('/superadmin/scholarships/allocate/student', {
                        branchId: parseInt(campusId),
                        studentId: parseInt(studentId),
                        amountUgx: parseFloat(amount),
                        term: 'Term 1',
                        academicYear: '2024'
                    });
                    alert(Successfully allocated UGX  to .);
                    inputField.value = '';
                } catch(err) {
                    alert("Treasury Error: Insufficient funds.");
                } finally {
                    hideLoader();
                }
            }"""
new_partial_alloc = """if (!amount || amount <= 0) return showErrorMessage('Please enter a valid amount.');

            showPremiumModal({
                title: 'Confirm Allocation',
                type: 'warning',
                contentText: Allocate UGX  to ?,
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
                        showSuccessMessage(Successfully allocated UGX  to .);
                        inputField.value = '';
                    } catch(err) {
                        showErrorMessage("Treasury Error: Insufficient funds.");
                    } finally {
                        hideLoader();
                    }
                }
            });"""
content = content.replace(old_partial_alloc, new_partial_alloc)

# Write back
with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Violations replaced successfully!")

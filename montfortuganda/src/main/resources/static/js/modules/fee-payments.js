/* global apiGet, apiPost, showSuccessMessage, showErrorMessage */
(() => {
    'use strict';
    const ROOT = '#ba-fee-payments-view';
    const API = Object.freeze({
        assignments: studentId => `/branchadmin/payments/students/${studentId}/fee-assignments`,
        history: studentId => `/branchadmin/payments/students/${studentId}/history`,
        payment: assignmentId => `/branchadmin/fees/assignments/${assignmentId}/payments`
    });
    const state = { root: null, studentId: null, assignments: [], history: [], selectedAssignment: null, modal: null, busy: false };

    document.addEventListener('viewLoaded', event => {
        if (String(event.detail?.role || '').toLowerCase().replace(/_/g, '-') !== 'admin' || event.detail?.view !== 'fee-payments') return;
        const init = initialize();
        if (typeof event.detail.waitUntil === 'function') event.detail.waitUntil(init);
        init.catch(error => { console.error('Fee Payments initialization failed.', error); notifyError(error.message || 'Unable to open Fee Payments.'); });
    });

    async function initialize() {
        const root = document.querySelector(ROOT);
        if (!root || root.dataset.initialized === 'true') return;
        state.root = root;
        state.modal = window.bootstrap?.Modal ? new bootstrap.Modal(root.querySelector('#fee-payment-record-modal')) : null;
        root.dataset.initialized = 'true';
        root.querySelector('#fee-payments-student-form').addEventListener('submit', loadStudentData);
        root.querySelector('#fee-payments-refresh').addEventListener('click', refresh);
        root.querySelector('#fee-payments-assignments-body').addEventListener('click', assignmentAction);
        root.querySelector('#fee-payment-record-form').addEventListener('submit', recordPayment);
    }

    async function loadStudentData(event) {
        event?.preventDefault();
        const input = state.root.querySelector('#fee-payments-student-id');
        const id = Number(input.value);
        if (!Number.isSafeInteger(id) || id <= 0) return notifyError('Enter a valid positive student ID.');
        if (state.busy) return;
        state.busy = true;
        setLoadingRows();
        try {
            const results = await Promise.allSettled([
                apiGet(API.assignments(id), { silent: true }),
                apiGet(API.history(id), { silent: true })
            ]);
            if (results[0].status === 'rejected') throw results[0].reason;
            state.studentId = id;
            state.assignments = list(results[0].value);
            state.history = results[1].status === 'fulfilled' ? list(results[1].value) : [];
            renderAssignments(); renderHistory();
            const first = state.assignments[0];
            state.root.querySelector('#fee-payments-student-summary').hidden = !first;
            state.root.querySelector('#fee-payments-student-name').textContent = first?.studentName || (first ? `Student ID ${id}` : `Student ID ${id}`);
            state.root.querySelector('#fee-payments-student-meta').textContent = first ? [first.admissionNo, `Student ID: ${id}`].filter(Boolean).join(' · ') : `Student ID: ${id} · No fee assignments found`;
            if (first) {
                state.root.querySelector('#fee-payments-student-name').textContent = first.studentName || `Student ID ${id}`;
                state.root.querySelector('#fee-payments-student-meta').textContent = [first.admissionNo, `Student ID: ${id}`].filter(Boolean).join(' · ');
            }
            if (results[1].status === 'rejected') notifyError('Fee assignments loaded, but payment history could not be loaded.');
        } catch (error) {
            state.assignments = []; state.history = []; renderAssignments(); renderHistory(); report(error);
        } finally { state.busy = false; }
    }

    function setLoadingRows() {
        state.root.querySelector('#fee-payments-assignments-body').innerHTML = '<tr><td colspan="9" class="text-center py-4">Loading fee assignments…</td></tr>';
        state.root.querySelector('#fee-payments-history-body').innerHTML = '<tr><td colspan="7" class="text-center py-4">Loading payment history…</td></tr>';
    }
    function renderAssignments() {
        const body = state.root.querySelector('#fee-payments-assignments-body');
        if (!state.assignments.length) { body.innerHTML = '<tr><td colspan="9" class="text-center py-4 text-muted">No fee assignments found for this student.</td></tr>'; return; }
        body.innerHTML = state.assignments.map(a => `<tr>
            <td>${esc(a.feeName || a.feeType || 'Fee')}</td><td>${esc(a.academicYear || '—')}</td><td>${esc(a.term || '—')}</td>
            <td>${money(a.payableAmount)}</td><td>${money(a.paidAmount)}</td><td>${money(a.balanceAmount)}</td><td>${esc(a.dueDate || '—')}</td>
            <td><span class="badge ${a.feeStatus === 'PAID' ? 'bg-success' : a.feeStatus === 'OVERDUE' ? 'bg-danger' : 'bg-warning text-dark'}">${esc(a.feeStatus || '—')}</span></td>
            <td class="text-end">${a.active !== false && String(a.assignmentStatus || a.status || '').toUpperCase() !== 'CANCELLED' && Number(a.balanceAmount ?? a.outstandingAmount ?? 0) > 0 ? `<button type="button" class="btn-primary btn-sm" data-pay-id="${a.feeAssignmentId}">Record Payment</button>` : '—'}</td>
        </tr>`).join('');
    }
    function renderHistory() {
        const body = state.root.querySelector('#fee-payments-history-body');
        if (!state.history.length) { body.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-muted">No payment history found.</td></tr>'; return; }
        body.innerHTML = state.history.map(p => `<tr><td>${esc(p.receiptNo || '—')}</td><td>${esc(formatDateTime(p.paymentDateTime))}</td><td>${esc(p.feeName || p.feeType || '—')}</td><td>${money(p.paymentAmount)}</td><td>${esc(p.paymentMode || '—')}</td><td>${esc(p.transactionReference || '—')}</td><td><span class="badge ${p.paymentStatus === 'SUCCESS' ? 'bg-success' : 'bg-secondary'}">${esc(p.paymentStatus || '—')}</span></td></tr>`).join('');
    }
    function assignmentAction(event) {
        const button = event.target.closest('[data-pay-id]');
        if (!button) return;
        const assignment = state.assignments.find(a => String(a.feeAssignmentId) === button.dataset.payId);
        if (!assignment) return;
        state.selectedAssignment = assignment;
        const $ = selector => state.root.querySelector(selector);
        $('#fee-payment-modal-fee-name').textContent = `${assignment.feeName || 'Fee'} — ${assignment.studentName || ''}`;
        $('#fee-payment-modal-balance').textContent = `Outstanding balance: ${money(assignment.balanceAmount)}`;
        $('#fee-payment-amount').value = Number(assignment.balanceAmount || 0).toFixed(2);
        $('#fee-payment-mode').value = ''; $('#fee-payment-reference').value = ''; $('#fee-payment-collection-point').value = ''; $('#fee-payment-remarks').value = '';
        state.modal?.show();
        if (!state.modal) {
            const modal = $('#fee-payment-record-modal');
            modal.hidden = false; modal.classList.add('show'); modal.style.display = 'block';
            modal.removeAttribute('aria-hidden');
        }
    }

    async function recordPayment(event) {
        event.preventDefault();
        if (state.busy || !state.selectedAssignment) return;
        const $ = selector => state.root.querySelector(selector);
        const amount = Number($('#fee-payment-amount').value);
        const paymentMode = $('#fee-payment-mode').value;
        const allowedModes = new Set(['CASH','CHEQUE','BANK_TRANSFER','MOBILE_MONEY','CREDIT_CARD','DEBIT_CARD','ONLINE','SCHOLARSHIP','WAIVER']);
        if (!Number.isFinite(amount) || amount <= 0) return notifyError('Payment amount must be greater than zero.');
        const outstanding = Number(state.selectedAssignment.balanceAmount ?? state.selectedAssignment.outstandingAmount ?? 0);
        if (Number.isFinite(outstanding) && outstanding >= 0 && amount - outstanding > 0.000001) {
            return notifyError(`Payment cannot exceed the outstanding balance of ${money(outstanding)}.`);
        }
        if (!allowedModes.has(paymentMode)) return notifyError('Select a valid payment mode.');
        const payload = {
            amount, paymentMode,
            transactionReference: $('#fee-payment-reference').value.trim() || null,
            collectionPoint: $('#fee-payment-collection-point').value.trim() || null,
            remarks: $('#fee-payment-remarks').value.trim() || null
        };
        if (!window.confirm(`Record payment of ${money(amount)} against ${state.selectedAssignment.feeName || 'this fee'}?`)) return;
        state.busy = true; $('#fee-payment-submit').disabled = true;
        try {
            await apiPost(API.payment(state.selectedAssignment.feeAssignmentId), payload);
            state.modal?.hide(); if (!state.modal) {
                const modal = $('#fee-payment-record-modal');
                modal.classList.remove('show'); modal.style.display = 'none'; modal.hidden = true;
                modal.setAttribute('aria-hidden', 'true');
            }
            notifySuccess('Payment recorded successfully.');
            await loadStudentData();
        } catch (error) { report(error); }
        finally { state.busy = false; $('#fee-payment-submit').disabled = false; }
    }
    async function refresh() { if (state.studentId) { state.root.querySelector('#fee-payments-student-id').value = state.studentId; await loadStudentData(); } else notifyError('Enter a student ID first.'); }
    function list(response) { const value = response && Object.prototype.hasOwnProperty.call(response, 'data') ? response.data : response; return Array.isArray(value) ? value : (Array.isArray(value?.content) ? value.content : []); }
    function money(value) { return Number(value || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }); }
    function formatDateTime(value) { if (!value) return '—'; const d = new Date(value); return Number.isNaN(d.getTime()) ? String(value) : d.toLocaleString(); }
    function esc(value) { return String(value ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c])); }
    function notifySuccess(message) { if (typeof showSuccessMessage === 'function') showSuccessMessage(message); else window.alert(message); }
    function notifyError(message) { if (typeof showErrorMessage === 'function') showErrorMessage(message); else window.alert(message); }
    function report(error) { console.error(error); notifyError(error?.message || 'The operation could not be completed.'); }
})();

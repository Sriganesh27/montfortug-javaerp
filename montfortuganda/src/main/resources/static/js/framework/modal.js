const AppModal = (function() {
    let modalEl, titleEl, msgEl, cancelBtn, proceedBtn, currentResolve;

    function init() {
        if (document.getElementById('erp-global-modal')) return;

        const html = `
            <div id="erp-global-modal" class="ba-modal-backdrop hidden">
                <div class="ba-modal-box">
                    <h3 id="erp-modal-title">Confirm</h3>
                    <p id="erp-modal-msg">Proceed?</p>
                    <div class="ba-modal-actions">
                        <button type="button" id="erp-modal-cancel" class="btn-secondary">Cancel</button>
                        <button type="button" id="erp-modal-proceed" class="btn-danger">Yes, Proceed</button>
                    </div>
                </div>
            </div>`;
        document.body.insertAdjacentHTML('beforeend', html);

        modalEl = document.getElementById('erp-global-modal');
        titleEl = document.getElementById('erp-modal-title');
        msgEl = document.getElementById('erp-modal-msg');
        cancelBtn = document.getElementById('erp-modal-cancel');
        proceedBtn = document.getElementById('erp-modal-proceed');

        cancelBtn.addEventListener('click', () => { hide(); if(currentResolve) currentResolve(false); });
        proceedBtn.addEventListener('click', () => { hide(); if(currentResolve) currentResolve(true); });
    }

    function hide() {
        if (modalEl) modalEl.classList.add('hidden');
    }

    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init);
    else init();

    return {
        confirm: function(title, message, isDanger = true) {
            if (!modalEl) init();
            titleEl.textContent = title;
            msgEl.textContent = message;
            proceedBtn.className = isDanger ? 'btn-danger' : 'btn-primary';
            modalEl.classList.remove('hidden');

            return new Promise(resolve => {
                currentResolve = resolve;
            });
        }
    };
})();
class CrudTable {
    constructor(view, callbacks) {
        this.view = view;
        this.callbacks = callbacks;
        this.bindEvents();
    }

    bindEvents() {
        this.view.btnPrev?.addEventListener('click', () => this.callbacks.onPageChange(-1));
        this.view.btnNext?.addEventListener('click', () => this.callbacks.onPageChange(1));
        this.view.pageSize?.addEventListener('change', (e) => this.callbacks.onSizeChange(parseInt(e.target.value)));

        this.view.table?.querySelectorAll('th[data-sort]').forEach(th => {
            th.addEventListener('click', (e) => this.callbacks.onSort(e.currentTarget.getAttribute('data-sort')));
        });
    }

    showLoading() {
        if (!this.view.tbody || !this.view.tplLoading) return;
        this.view.tbody.innerHTML = '';
        this.view.tbody.appendChild(this.view.tplLoading.content.cloneNode(true));
    }

    render(data, renderRowFn) {
        if (!this.view.tbody) return;
        this.view.tbody.innerHTML = '';

        if (!data || data.length === 0) {
            if (this.view.tplEmpty) this.view.tbody.appendChild(this.view.tplEmpty.content.cloneNode(true));
            return;
        }

        data.forEach(record => {
            const rowNode = this.view.tplRow.content.cloneNode(true);
            // The module controller modifies the node and returns it
            const populatedNode = renderRowFn(record, rowNode);
            this.view.tbody.appendChild(populatedNode);
        });
    }

    renderPagination(page, totalPages, totalElements) {
        if (this.view.pageInfo) {
            this.view.pageInfo.textContent = `Showing page ${page + 1} of ${totalPages || 1} (${totalElements} total)`;
        }
        if (this.view.btnPrev) this.view.btnPrev.disabled = page === 0;
        if (this.view.btnNext) this.view.btnNext.disabled = page >= totalPages - 1;
    }
}
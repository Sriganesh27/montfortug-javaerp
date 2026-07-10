class CrudForm {
    constructor(view) {
        this.view = view;
    }

    open() {
        if (this.view.tableView) this.view.tableView.classList.add('hidden');
        if (this.view.formView) this.view.formView.classList.remove('hidden');
    }

    close() {
        if (this.view.formView) this.view.formView.classList.add('hidden');
        if (this.view.tableView) this.view.tableView.classList.remove('hidden');
    }

    lock() {
        if (this.view.formFields) this.view.formFields.classList.add('ba-form-locked');
        if (this.view.formOverlay) this.view.formOverlay.classList.remove('hidden');
    }

    unlock() {
        if (this.view.formFields) this.view.formFields.classList.remove('ba-form-locked');
        if (this.view.formOverlay) this.view.formOverlay.classList.add('hidden');
    }
}
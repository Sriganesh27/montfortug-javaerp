<div id="ba-add-desig-view">
    <div class="card max-w-2xl mx-auto mt-4">
        <div class="view-header p-3 border-bottom">
            <h2 class="mb-0"><i class="bi bi-plus-circle me-2"></i>Create Designation</h2>
            <button id="backToDesignationsBtn" class="btn-secondary btn-sm"><i class="bi bi-arrow-left"></i> Back</button>
        </div>
        <div class="card-body p-4">
            <form id="add-desig-form">
                <div class="form-group mb-3">
                    <label class="detail-text text-strong d-block mb-1">Designation Code <span class="text-danger">*</span></label>
                    <input type="text" id="add-desigCode" class="detail-input w-100" required pattern="^[A-Z0-9_-]+$" title="Alphanumeric uppercase only" placeholder="e.g. HOD">
                </div>
                <div class="form-group mb-3">
                    <label class="detail-text text-strong d-block mb-1">Designation Name <span class="text-danger">*</span></label>
                    <input type="text" id="add-desigName" class="detail-input w-100" required placeholder="e.g. Head of Department">
                </div>
                <div class="form-group mb-4">
                    <label class="detail-text text-strong d-block mb-1">Description</label>
                    <textarea id="add-desigDescription" class="detail-input w-100" rows="3" placeholder="Optional description..."></textarea>
                </div>
                <div class="text-end border-top pt-3">
                    <button type="submit" class="btn-primary px-4"><i class="bi bi-check-lg"></i> Create Designation</button>
                </div>
            </form>
        </div>
    </div>
</div>

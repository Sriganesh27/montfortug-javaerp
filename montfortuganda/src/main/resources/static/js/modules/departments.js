document.addEventListener('viewLoaded', (e) => {
    // A single controller handles both the table view AND the add form view
    if (e.detail.view === 'departments') {
        DepartmentController.initManageView();
    } else if (e.detail.view === 'add-department') {
        DepartmentController.initAddView();
    }
});

const DepartmentController = (function() {

    // --- SHARED REPOSITORY ---
    const Repo = DepartmentRepository;

    // ==========================================
    // 1. MANAGE VIEW LOGIC (Table & Inline Edit)
    // ==========================================
    function initManageView() {
        let state = { page: 0, size: 20, sort: 'NAME', direction: 'ASC' };
        let view = DepartmentView.init();
        let form = new CrudForm(view);
        let table = new CrudTable(view, {
            onPageChange: (dir) => { state.page += dir; loadData(); },
            onSort: (field) => { state.sort = field; loadData(); }
        });

        async function loadData() {
            table.showLoading();
            try {
                const params = {
                    page: state.page, size: state.size, sortBy: state.sort, direction: state.direction,
                    keyword: view.searchKeyword.value.trim()
                };
                const res = await Repo.findAll(params);
                table.render(res.data.content, renderRow);
            } catch (err) { Toast.error('Failed to securely load data'); }
        }

        function renderRow(record, node) {
            node.querySelector('.td-code').textContent = record.departmentCode || '-';
            node.querySelector('.td-name strong').textContent = record.departmentName;

            node.querySelector('.btn-edit').addEventListener('click', () => {
                view.fId.value = record.departmentId;
                view.fName.value = record.departmentName;
                view.headerName.textContent = record.departmentName;
                form.open();
            });
            return node;
        }

        view.searchBtn.addEventListener('click', () => { state.page = 0; loadData(); });
        view.backBtn.addEventListener('click', () => form.close());

        view.saveBtn.addEventListener('click', async () => {
            const payload = { departmentName: view.fName.value.trim() };
            const error = Validator.validate(payload, { departmentName: { required: true } });
            if (error) return Toast.error(error);

            form.lock();
            try {
                await Repo.save(view.fId.value, payload);
                Toast.success('Securely updated.');
                form.close();
                loadData();
            } catch (err) { Toast.error(err.message); }
            finally { form.unlock(); }
        });

        // Listen for creations from the "Add New" page to refresh data
        Events.off('department:created', loadData);
        Events.on('department:created', loadData);


        loadData();
    }

    // ==========================================
    // 2. ADD VIEW LOGIC (Dedicated Form Page)
    // ==========================================
    function initAddView() {
        const view = {
            formFields: document.getElementById('ba-addDeptFields'),
            formOverlay: document.getElementById('ba-addDeptOverlay'),
            fName: document.getElementById('add-deptName'),
            saveBtn: document.getElementById('ba-saveNewDeptBtn'),
            backBtn: document.getElementById('ba-backToDeptsBtn')
        };

        view.fName.value = ''; // Reset on load

        view.saveBtn.addEventListener('click', async () => {
            const payload = { departmentName: view.fName.value.trim() };
            const error = Validator.validate(payload, { departmentName: { required: true } });
            if (error) { Toast.error(error); view.fName.focus(); return; }

            // Hard Lock to prevent double-click duplicates
            view.formFields.classList.add('ba-form-locked');
            view.formOverlay.classList.remove('hidden');
            view.saveBtn.disabled = true;

            try {
                await Repo.save(null, payload);
                Toast.success('Department securely created.');
                Events.emit('department:created'); // Tells the table to refresh!

                // Route back to the Manage screen securely
                document.querySelector('.sidebar-nav a[href*="departments.html"]').click();
            } catch (err) {
                Toast.error(err.message);
                view.formFields.classList.remove('ba-form-locked');
                view.formOverlay.classList.add('hidden');
                view.saveBtn.disabled = false;
            }
        });

        view.backBtn.addEventListener('click', () => {
            document.querySelector('.sidebar-nav a[href*="departments.html"]').click();
        });
    }

    return { initManageView, initAddView };
})();

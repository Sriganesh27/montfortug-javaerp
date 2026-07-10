window.DesignationsModule = (() => {

    async function initManageView() {
        const view = DesignationView.init();
        const state = { page: 0, size: parseInt(view.pageSize.value) || 20, sort: 'displayOrder', direction: 'ASC' };

        // --- SHARED REPOSITORY ---
        const Repo = DesignationRepository;

        // UI Helpers
        const table = {
            showLoading: () => {
                view.tbody.innerHTML = '';
                view.tbody.appendChild(view.tplLoading.content.cloneNode(true));
            },
            render: (records, rowRenderer) => {
                view.tbody.innerHTML = '';
                if (!records || records.length === 0) {
                    view.tbody.appendChild(view.tplEmpty.content.cloneNode(true));
                    return;
                }
                records.forEach(r => {
                    const row = view.tplRow.content.cloneNode(true);
                    view.tbody.appendChild(rowRenderer(r, row));
                });
            },
            renderPagination: (currentPage, totalPages, totalElements) => {
                view.pageInfo.textContent = `Page ${currentPage + 1} of ${totalPages} (${totalElements} total)`;
                view.btnPrev.disabled = currentPage === 0;
                view.btnNext.disabled = currentPage >= totalPages - 1;
            }
        };

        const form = {
            open: () => {
                view.formFields.classList.remove('ba-form-locked');
                view.formOverlay.classList.add('hidden');
                document.getElementById('ba-desigTableWrapper').classList.add('hidden');
                document.getElementById('ba-desigFormWrapper').classList.remove('hidden');
                document.getElementById('view-title').textContent = "Edit Designation";
            },
            close: () => {
                document.getElementById('ba-desigFormWrapper').classList.add('hidden');
                document.getElementById('ba-desigTableWrapper').classList.remove('hidden');
                document.getElementById('view-title').textContent = "Manage Designations";
                loadData();
            },
            lock: () => {
                view.formFields.classList.add('ba-form-locked');
                view.formOverlay.classList.remove('hidden');
                view.saveBtn.disabled = true;
            },
            unlock: () => {
                view.formFields.classList.remove('ba-form-locked');
                view.formOverlay.classList.add('hidden');
                view.saveBtn.disabled = false;
            }
        };

        // Bind events
        view.pageSize.addEventListener('change', (e) => { state.size = parseInt(e.target.value); state.page = 0; loadData(); });
        view.btnPrev.addEventListener('click', () => { if (state.page > 0) { state.page--; loadData(); } });
        view.btnNext.addEventListener('click', () => { state.page++; loadData(); });

        setupTableSorting(view.table, {
            defaultSort: 'displayOrder',
            defaultDir: 'ASC',
            onSort: (field) => { state.sort = field; loadData(); }
        });

        async function loadData() {
            table.showLoading();
            try {
                const params = {
                    page: state.page, size: state.size, sortBy: state.sort, direction: state.direction,
                    keyword: view.searchKeyword.value.trim(),
                    active: view.searchStatus.value
                };
                const res = await Repo.findAll(params);
                table.render(res.data.content, renderRow);
                table.renderPagination(state.page, res.data.totalPages, res.data.totalElements);
            } catch (err) { Toast.error('Failed to securely load data'); }
        }

        function renderRow(record, node) {
            node.querySelector('.td-name strong').textContent = record.designationName;
            node.querySelector('.td-dept').textContent = '-';
            node.querySelector('.td-order').textContent = record.displayOrder;

            const badge = document.createElement('span');
            badge.className = record.active ? 'badge badge-success' : 'badge badge-danger';
            badge.textContent = record.active ? 'Active' : 'Inactive';
            node.querySelector('.td-status').appendChild(badge);

            node.querySelector('.td-emp').textContent = record.employeeCount || 0;

            node.querySelector('.btn-edit').addEventListener('click', () => {
                view.fId.value = record.designationId;
                view.fName.value = record.designationName;
                view.fOrder.value = record.displayOrder || 100;
                view.fDesc.value = record.description || '';
                view.headerName.textContent = record.designationName;
                form.open();
            });

            if (record.active) {
                const delBtn = node.querySelector('.btn-delete');
                delBtn.classList.remove('hidden');
                delBtn.addEventListener('click', () => deactivateRecord(record.designationId));
            }
            return node;
        }

        async function deactivateRecord(id) {
            const proceed = await AppModal.confirm("Deactivate Designation", "Are you sure?");
            if (proceed) {
                try {
                    await Repo.delete(id);
                    Toast.success('Deactivated');
                    loadData();
                } catch(err) { Toast.error(err.message); }
            }
        }

        view.searchBtn.addEventListener('click', () => { state.page = 0; loadData(); });
        view.resetBtn.addEventListener('click', () => {
            view.searchKeyword.value = ''; view.searchStatus.value = '';
            state.page = 0; loadData();
        });

        view.backBtn.addEventListener('click', () => form.close());

        view.saveBtn.addEventListener('click', async () => {
            const payload = {
                designationName: view.fName.value.trim(),
                displayOrder: parseInt(view.fOrder.value) || 100,
                description: view.fDesc.value.trim()
            };

            const error = Validator.validate(payload, {
                designationName: { required: true, message: "Designation Name is required" }
            });

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

        Events.on('designation:created', loadData);
        loadData();
    }

    // ==========================================
    // 2. ADD VIEW LOGIC (Dedicated Form Page)
    // ==========================================
    async function initAddView() {
        const view = {
            formFields: document.getElementById('ba-addDesigFields'),
            formOverlay: document.getElementById('ba-addDesigOverlay'),
            fName: document.getElementById('add-desigName'),
            fOrder: document.getElementById('add-desigOrder'),
            fDesc: document.getElementById('add-desigDesc'),
            saveBtn: document.getElementById('ba-saveNewDesigBtn'),
            backBtn: document.getElementById('ba-backToDesigsBtn')
        };

        // Reset
        view.fName.value = ''; view.fDesc.value = ''; view.fOrder.value = '100';

        view.saveBtn.addEventListener('click', async () => {
            const payload = {
                designationName: view.fName.value.trim(),
                displayOrder: parseInt(view.fOrder.value) || 100,
                description: view.fDesc.value.trim()
            };

            const error = Validator.validate(payload, {
                designationName: { required: true, message: "Designation Name is required" }
            });

            if (error) { Toast.error(error); return; }

            // Hard Lock
            view.formFields.classList.add('ba-form-locked');
            view.formOverlay.classList.remove('hidden');
            view.saveBtn.disabled = true;

            try {
                await Repo.save(null, payload);
                Toast.success('Designation securely created.');
                Events.emit('designation:created');

                // Route back securely
                document.querySelector('.sidebar-nav a[href*="designations.html"]').click();
            } catch (err) {
                Toast.error(err.message);
                view.formFields.classList.remove('ba-form-locked');
                view.formOverlay.classList.add('hidden');
                view.saveBtn.disabled = false;
            }
        });

        view.backBtn.addEventListener('click', () => {
            document.querySelector('.sidebar-nav a[href*="designations.html"]').click();
        });
    }

    return { initManageView, initAddView };
})();
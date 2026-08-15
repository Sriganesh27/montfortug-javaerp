/* global apiGet, apiPost, apiPut, showSuccessMessage, showErrorMessage, loadView */

document.addEventListener('viewLoaded', function (e) {
    if (e.detail.role !== 'admin') {
        return;
    }

    if (e.detail.view === 'subjects') {
        initSubjectsView();
    } else if (e.detail.view === 'add-subject') {
        initAddSubjectView();
    }
});

function initSubjectsView() {
    const view = document.querySelector('#ba-subjects-view');

    if (!view || view.dataset.initialized === 'true') {
        return;
    }

    view.dataset.initialized = 'true';

    const addBtn = view.querySelector('#btn-add-subject');

    if (addBtn) {
        addBtn.addEventListener('click', () => {
            navigateToAddSubject();
        });
    }

    const tableView = view.querySelector('#subject-tableView');
    const detailView = view.querySelector('#subject-detailView');

    const tbody = view.querySelector('#subject-tableBody');
    const rowTemplate = document.getElementById('tpl-subject-row');

    const searchInput = view.querySelector('#subject-searchInput');
    const typeFilter = view.querySelector('#subject-typeFilter');
    const statusFilter = view.querySelector('#subject-statusFilter');
    const searchBtn = view.querySelector('#subject-searchBtn');

    const pageSize = view.querySelector('#subject-pageSize');
    const pageInfo = view.querySelector('#subject-pageInfo');
    const prevBtn = view.querySelector('#btn-subject-prev');
    const nextBtn = view.querySelector('#btn-subject-next');

    const backBtn = view.querySelector('#subject-backToTableBtn');
    const editBtn = view.querySelector('#subject-editBtn');
    const cancelEditBtn = view.querySelector('#subject-cancelEditBtn');
    const saveBtn = view.querySelector('#subject-saveBtn');

    let records = [];
    let filtered = [];
    let currentDetailId = null;

    const state = {
        page: 0,
        size: Number(pageSize?.value || 10)
    };

    searchBtn?.addEventListener('click', () => {
        state.page = 0;
        applyFilters();
    });

    searchInput?.addEventListener('keydown', event => {
        if (event.key === 'Enter') {
            event.preventDefault();
            state.page = 0;
            applyFilters();
        }
    });

    typeFilter?.addEventListener('change', () => {
        state.page = 0;
        applyFilters();
    });

    statusFilter?.addEventListener('change', () => {
        state.page = 0;
        applyFilters();
    });

    pageSize?.addEventListener('change', () => {
        state.size = Number(pageSize.value || 10);
        state.page = 0;
        renderTable();
    });

    prevBtn?.addEventListener('click', () => {
        if (state.page > 0) {
            state.page--;
            renderTable();
        }
    });

    nextBtn?.addEventListener('click', () => {
        if (state.page + 1 < totalPages()) {
            state.page++;
            renderTable();
        }
    });

    backBtn?.addEventListener('click', () => {
        closeDetail();
    });

    editBtn?.addEventListener('click', () => {
        setEditMode(true);
    });

    cancelEditBtn?.addEventListener('click', () => {
        setEditMode(false);
        const current = records.find(
            item => Number(item.subjectId) === Number(currentDetailId)
        );

        if (current) {
            populateDetail(current);
        }
    });

    saveBtn?.addEventListener('click', () => {
        void saveDetail();
    });

    if (typeof window.erpRegisterModuleSync === 'function') {
        window.erpRegisterModuleSync(
            'subjects',
            async () => {
                if (!document.querySelector('#ba-subjects-view')) return false;

                await loadSubjects();

                if (currentDetailId && detailView && !detailView.classList.contains('hidden')) {
                    const current = records.find(
                        item => Number(item.subjectId) === Number(currentDetailId)
                    );
                    if (current) populateDetail(current);
                }

                return true;
            }
        );
    }

    void loadSubjects();


    function cancelQueuedGlobalSync() {
        if (
            typeof window.erpCancelPendingDataSync
            === 'function'
        ) {
            window.erpCancelPendingDataSync();
        }
    }

    async function loadSubjects() {
        renderLoading();

        try {
            const response =
                typeof erpWithLoader === 'function'
                    ? await erpWithLoader(
                        'Loading Subjects...',
                        () => apiGet('/subjects')
                    )
                    : await apiGet('/subjects');

            records = Array.isArray(response?.data)
                ? response.data
                : [];

            state.page = 0;
            applyFilters();
        } catch (error) {
            console.error(error);
            renderEmpty('Failed to load Subjects.');
            showErrorMessage?.(
                error?.data?.message
                || error?.message
                || 'Failed to load Subjects.'
            );
        }
    }

    function applyFilters() {
        const search = String(searchInput?.value || '')
            .trim()
            .toLowerCase();

        const type = String(typeFilter?.value || '');
        const status = String(statusFilter?.value || '');

        filtered = records.filter(subject => {
            const text = [
                subject.subjectCode,
                subject.subjectName,
                subject.subjectShortName
            ]
                .filter(Boolean)
                .join(' ')
                .toLowerCase();

            return (
                (!search || text.includes(search))
                && (!type || subject.subjectType === type)
                && (!status || subject.status === status)
            );
        });

        if (state.page >= totalPages()) {
            state.page = Math.max(totalPages() - 1, 0);
        }

        renderTable();
    }

    function renderTable() {
        tbody.replaceChildren();

        if (!filtered.length) {
            renderEmpty('No Subjects found.');
            updatePagination();
            return;
        }

        const start = state.page * state.size;

        filtered
            .slice(start, start + state.size)
            .forEach((subject, index) => {
                const fragment = rowTemplate.content.cloneNode(true);
                const row = fragment.querySelector('tr');

                row.querySelector('.col-id').textContent =
                    String(start + index + 1);

                row.querySelector('.col-code').textContent =
                    subject.subjectCode || '-';

                const nameCell = row.querySelector('.col-name');
                nameCell.querySelector('strong').textContent =
                    subject.subjectName || '-';

                row.querySelector('.col-short-name').textContent =
                    subject.subjectShortName || '-';

                row.querySelector('.col-type').textContent =
                    pretty(subject.subjectType);

                row.querySelector('.col-practical').textContent =
                    subject.practical ? 'Yes' : 'No';

                row.querySelector('.col-order').textContent =
                    String(subject.displayOrder ?? '-');

                const badge = row.querySelector('.status-badge');
                badge.textContent = pretty(subject.status);
                badge.className =
                    `status-badge badge-${String(
                        subject.status || 'INACTIVE'
                    ).toLowerCase()}`;

                nameCell.addEventListener(
                    'click',
                    () => openDetail(subject)
                );

                row.querySelector('.view-more-btn')
                    ?.addEventListener(
                        'click',
                        () => openDetail(subject)
                    );

                row.querySelector('.edit-btn')
                    ?.addEventListener(
                        'click',
                        () => {
                            openDetail(subject);
                            setEditMode(true);
                        }
                    );

                tbody.appendChild(fragment);
            });

        updatePagination();
    }

    function renderLoading() {
        tbody.innerHTML = `
            <tr>
                <td colspan="9" class="text-center py-4">
                    <div class="spinner-border text-primary spinner-border-sm me-2"></div>
                    Loading Subjects...
                </td>
            </tr>
        `;
    }

    function renderEmpty(message) {
        tbody.innerHTML = `
            <tr>
                <td colspan="9" class="text-center py-4 text-muted">
                    <i class="bi bi-inbox fs-4 d-block mb-2"></i>
                    ${escapeHtml(message)}
                </td>
            </tr>
        `;
    }

    function totalPages() {
        return Math.max(
            Math.ceil(filtered.length / state.size),
            1
        );
    }

    function updatePagination() {
        const pages = totalPages();
        const total = filtered.length;

        if (pageInfo) {
            pageInfo.textContent = total
                ? `Showing page ${state.page + 1} of ${pages}`
                : 'Showing page 1 of 1';
        }

        if (prevBtn) {
            prevBtn.disabled = state.page <= 0 || total === 0;
        }

        if (nextBtn) {
            nextBtn.disabled =
                state.page + 1 >= pages || total === 0;
        }
    }

    function openDetail(subject) {
        currentDetailId = Number(subject.subjectId);
        populateDetail(subject);
        setEditMode(false);

        tableView.classList.add('hidden');
        detailView.classList.remove('hidden');

        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    }

    function closeDetail() {
        currentDetailId = null;
        setEditMode(false);

        detailView.classList.add('hidden');
        tableView.classList.remove('hidden');

        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    }

    function populateDetail(subject) {
        text('#detail-subjectNameHeader', subject.subjectName || '');
        text('#view-subjectCode', subject.subjectCode || '-');
        text('#view-subjectName', subject.subjectName || '-');
        text(
            '#view-subjectShortName',
            subject.subjectShortName || '-'
        );
        text(
            '#view-subjectType',
            pretty(subject.subjectType)
        );
        text(
            '#view-subjectPractical',
            subject.practical ? 'Yes' : 'No'
        );
        text(
            '#view-subjectDisplayOrder',
            subject.displayOrder ?? '-'
        );
        text(
            '#view-subjectStatus',
            pretty(subject.status)
        );
        text(
            '#view-subjectDescription',
            subject.description || '—'
        );

        value('#edit-subjectCode', subject.subjectCode || '');
        value('#edit-subjectName', subject.subjectName || '');
        value(
            '#edit-subjectShortName',
            subject.subjectShortName || ''
        );
        value(
            '#edit-subjectType',
            subject.subjectType || 'CORE'
        );
        value(
            '#edit-subjectPractical',
            String(Boolean(subject.practical))
        );
        value(
            '#edit-subjectDisplayOrder',
            subject.displayOrder ?? 1
        );
        value(
            '#edit-subjectStatus',
            subject.status || 'ACTIVE'
        );
        value(
            '#edit-subjectDescription',
            subject.description || ''
        );
    }

    function setEditMode(editing) {
        view.querySelectorAll(
            '[id^="view-subject"]'
        ).forEach(node => {
            node.classList.toggle('hidden', editing);
        });

        view.querySelectorAll(
            '[id^="edit-subject"]'
        ).forEach(node => {
            node.classList.toggle('hidden', !editing);
        });

        editBtn?.classList.toggle('hidden', editing);
        cancelEditBtn?.classList.toggle('hidden', !editing);
        saveBtn?.classList.toggle('hidden', !editing);
    }

    async function saveDetail() {
        if (!currentDetailId) {
            return;
        }

        const payload = {
            subjectCode:
                view.querySelector('#edit-subjectCode').value.trim(),
            subjectName:
                view.querySelector('#edit-subjectName').value.trim(),
            subjectShortName:
                nullable(
                    view.querySelector('#edit-subjectShortName').value
                ),
            subjectType:
                view.querySelector('#edit-subjectType').value,
            practical:
                view.querySelector('#edit-subjectPractical').value
                === 'true',
            displayOrder:
                Number(
                    view.querySelector(
                        '#edit-subjectDisplayOrder'
                    ).value
                ),
            status:
                view.querySelector('#edit-subjectStatus').value,
            description:
                nullable(
                    view.querySelector(
                        '#edit-subjectDescription'
                    ).value
                )
        };

        try {
            const response =
                typeof erpWithLoader === 'function'
                    ? await erpWithLoader(
                        'Updating Subject...',
                        () => apiPut(
                            `/subjects/${currentDetailId}`,
                            payload
                        )
                    )
                    : await apiPut(
                        `/subjects/${currentDetailId}`,
                        payload
                    );

            cancelQueuedGlobalSync();

            showSuccessMessage?.(
                response?.message
                || 'Subject updated successfully.'
            );

            await loadSubjects();

            const refreshed = records.find(
                item =>
                    Number(item.subjectId)
                    === Number(currentDetailId)
            );

            if (refreshed) {
                populateDetail(refreshed);
            }

            setEditMode(false);
        } catch (error) {
            console.error(error);
            showErrorMessage?.(
                error?.data?.message
                || error?.message
                || 'Failed to update Subject.'
            );
        }
    }

    function text(selector, valueToSet) {
        const node = view.querySelector(selector);

        if (node) {
            node.textContent = String(valueToSet ?? '');
        }
    }

    function value(selector, valueToSet) {
        const node = view.querySelector(selector);

        if (node) {
            node.value = String(valueToSet ?? '');
        }
    }
}

function initAddSubjectView() {
    const view = document.querySelector('#ba-add-subject-view');

    if (!view || view.dataset.initialized === 'true') {
        return;
    }

    view.dataset.initialized = 'true';

    const backBtn = view.querySelector('#backToSubjectsBtn');
    const form = view.querySelector('#add-subject-form');
    const code = view.querySelector('#add-subjectCode');

    backBtn?.addEventListener('click', () => {
        navigateToSubjects();
    });

    code?.addEventListener('input', () => {
        code.value = code.value.toUpperCase();
    });

    form?.addEventListener('submit', event => {
        event.preventDefault();
        void createSubject();
    });

    async function createSubject() {
        const payload = {
            subjectCode:
                view.querySelector('#add-subjectCode').value.trim(),
            subjectName:
                view.querySelector('#add-subjectName').value.trim(),
            subjectShortName:
                nullable(
                    view.querySelector('#add-subjectShortName').value
                ),
            subjectType:
                view.querySelector('#add-subjectType').value,
            practical:
                view.querySelector('#add-subjectPractical').value
                === 'true',
            displayOrder:
                Number(
                    view.querySelector(
                        '#add-subjectDisplayOrder'
                    ).value
                ),
            status:
                view.querySelector('#add-subjectStatus').value,
            description:
                nullable(
                    view.querySelector(
                        '#add-subjectDescription'
                    ).value
                )
        };

        if (!payload.subjectCode) {
            showErrorMessage?.('Subject code is required.');
            return;
        }

        if (!payload.subjectName) {
            showErrorMessage?.('Subject name is required.');
            return;
        }

        if (
            !Number.isInteger(payload.displayOrder)
            || payload.displayOrder < 1
        ) {
            showErrorMessage?.(
                'Display order must be at least 1.'
            );
            return;
        }

        try {
            const response =
                typeof erpWithLoader === 'function'
                    ? await erpWithLoader(
                        'Saving Subject...',
                        () => apiPost('/subjects', payload)
                    )
                    : await apiPost('/subjects', payload);

            showSuccessMessage?.(
                response?.message
                || 'Subject created successfully.'
            );

            form.reset();
            view.querySelector(
                '#add-subjectDisplayOrder'
            ).value = '1';

            view.querySelector(
                '#add-subjectStatus'
            ).value = 'ACTIVE';

            navigateToSubjects();
        } catch (error) {
            console.error(error);
            showErrorMessage?.(
                error?.data?.message
                || error?.message
                || 'Failed to create Subject.'
            );
        }
    }
}

function navigateToAddSubject() {
    const mainContent =
        document.getElementById('main-content-area');

    window.history.pushState(
        {
            view: 'add-subject',
            title: 'Add Subject'
        },
        '',
        '/admin/add-subject'
    );

    const title =
        document.getElementById('pageTitle');

    if (title) {
        title.textContent = 'Add Subject';
    }

    if (mainContent) {
        mainContent.scrollTop = 0;
    }

    void loadView(
        'admin',
        'add-subject',
        mainContent
    );
}

function navigateToSubjects() {
    const mainContent =
        document.getElementById('main-content-area');

    window.history.pushState(
        {
            view: 'subjects',
            title: 'Subjects'
        },
        '',
        '/admin/subjects'
    );

    const title =
        document.getElementById('pageTitle');

    if (title) {
        title.textContent = 'Subjects';
    }

    if (mainContent) {
        mainContent.scrollTop = 0;
    }

    void loadView(
        'admin',
        'subjects',
        mainContent
    );
}

function nullable(value) {
    const text = String(value ?? '').trim();
    return text || null;
}

function pretty(value) {
    return String(value || '-')
        .replaceAll('_', ' ')
        .toLowerCase()
        .replace(
            /\b\w/g,
            character => character.toUpperCase()
        );
}

function escapeHtml(value) {
    return String(value ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}

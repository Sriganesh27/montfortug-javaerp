/* global apiGet, apiPost, showLoader, hideLoader, showSuccessMessage, showErrorMessage */

(function () {
    'use strict';

    /*
     * Scholarship Applications
     *
     * Current Branch Admin API:
     *   GET  /api/branchadmin/scholarships
     *   GET  /api/branchadmin/scholarships/{scholarshipAppId}
     *   GET  /api/branchadmin/scholarships/verification-employees
     *   POST /api/branchadmin/scholarships/{id}/verification/assign
     *   POST /api/branchadmin/scholarships/{id}/verification/complete
     *
     * Full applicant data is loaded from the existing protected
     * Scholarship form endpoint:
     *   GET /api/admission/branch/applications/{applicationId}/workflow/scholarship/application-form
     *
     * Shortlist/final decision remain Super Admin actions in the current
     * backend and are therefore not called from the Branch Admin page.
     */

    const API_ROOT =
        '/api/branchadmin/scholarships';

    const SCHOLARSHIP_FORM_ROOT =
        '/api/admission/branch/applications';

    const state = {
        initialized: false,
        rows: [],
        filteredRows: [],
        selectedId: null,
        currentSummary: null,
        currentForm: null,
        employees: [],
        page: 1,
        pageSize: 10
    };

    const $ = (id) =>
        document.getElementById(id);

    function root() {
        return $('scholarship-applications-view');
    }

    function unwrap(response) {
        if (!response) {
            return null;
        }

        if (
            Object.prototype.hasOwnProperty.call(
                response,
                'data'
            )
        ) {
            return response.data;
        }

        return response;
    }

    function messageFromError(
        error,
        fallback
    ) {
        return error?.message
            || error?.response?.message
            || fallback;
    }

    function text(value) {
        if (
            value === null
            || value === undefined
            || String(value).trim() === ''
        ) {
            return '—';
        }

        return String(value);
    }

    function enumLabel(value) {
        if (
            value === null
            || value === undefined
            || String(value).trim() === ''
        ) {
            return '—';
        }

        return String(value)
            .trim()
            .replace(/[_-]+/g, ' ')
            .replace(/\s+/g, ' ')
            .toLowerCase()
            .replace(/\b\w/g, letter =>
                letter.toUpperCase()
            );
    }

    function normalize(value) {
        return String(value || '')
            .trim()
            .toUpperCase()
            .replace(/[\s-]+/g, '_');
    }

    function dateTime(value) {
        if (!value) {
            return '—';
        }

        const date =
            new Date(value);

        if (
            Number.isNaN(
                date.getTime()
            )
        ) {
            return text(value);
        }

        return date.toLocaleString(
            undefined,
            {
                dateStyle: 'medium',
                timeStyle: 'short'
            }
        );
    }

    function money(value) {
        if (
            value === null
            || value === undefined
            || value === ''
        ) {
            return '—';
        }

        const number =
            Number(value);

        if (!Number.isFinite(number)) {
            return text(value);
        }

        return `UGX ${number.toLocaleString(
            'en-UG',
            {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            }
        )}`;
    }

    function setText(id, value) {
        const node = $(id);

        if (node) {
            node.textContent =
                text(value);
        }
    }

    function setBoolean(id, value) {
        setText(
            id,
            value === true
                ? 'Yes'
                : value === false
                    ? 'No'
                    : '—'
        );
    }

    function setMoney(id, value) {
        setText(
            id,
            money(value)
        );
    }

    function setDateTime(id, value) {
        setText(
            id,
            dateTime(value)
        );
    }

    function setBusy(
        button,
        busy,
        busyText
    ) {
        if (!button) {
            return;
        }

        if (busy) {
            if (!button.dataset.idleText) {
                button.dataset.idleText =
                    button.textContent.trim();
            }

            button.disabled = true;
            button.dataset.busy = 'true';
            button.textContent =
                busyText || 'Please wait...';
            return;
        }

        button.disabled = false;
        button.dataset.busy = 'false';

        if (button.dataset.idleText) {
            button.textContent =
                button.dataset.idleText;
        }
    }

    function showList() {
        $('scholarship-applications-list-view')
            ?.classList.remove('hidden');

        $('scholarship-application-detail')
            ?.classList.add('hidden');
    }

    function showDetail() {
        $('scholarship-applications-list-view')
            ?.classList.add('hidden');

        $('scholarship-application-detail')
            ?.classList.remove('hidden');
    }

    function getSearchValue() {
        return $(
            'scholarship-applications-search'
        )?.value
            ?.trim()
            .toLowerCase()
            || '';
    }

    function currentFilters() {
        return {
            search:
                getSearchValue(),

            status:
                normalize(
                    $('scholarship-applications-status')
                        ?.value
                ),

            verification:
                normalize(
                    $('scholarship-applications-verification')
                        ?.value
                ),

            level:
                normalize(
                    $('scholarship-applications-level')
                        ?.value
                ),

            className:
                normalize(
                    $('scholarship-applications-class')
                        ?.value
                ),

            academicYear:
                String(
                    $('scholarship-applications-academic-year')
                        ?.value
                    || ''
                ).trim(),

            term:
                normalize(
                    $('scholarship-applications-term')
                        ?.value
                ),

            employee:
                String(
                    $('scholarship-applications-employee')
                        ?.value
                    || ''
                ),

            decision:
                normalize(
                    $('scholarship-applications-decision')
                        ?.value
                )
        };
    }

    function rowMatches(
        row,
        filters
    ) {
        const searchText = [
            row?.applicationNo,
            row?.studentName,
            row?.academicYear,
            row?.term,
            row?.scholarshipStatus,
            row?.verificationStatus,
            row?.schoolReviewStatus,
            row?.superAdminReviewStatus
        ]
            .filter(Boolean)
            .join(' ')
            .toLowerCase();

        if (
            filters.search
            && !searchText.includes(
                filters.search
            )
        ) {
            return false;
        }

        if (
            filters.status
            && normalize(row?.scholarshipStatus)
                !== filters.status
        ) {
            return false;
        }

        if (
            filters.verification
            && normalize(row?.verificationStatus)
                !== filters.verification
        ) {
            return false;
        }

        /*
         * The current BranchScholarshipSummaryDTO does not expose
         * level/class names. Keep these filters compatible with future
         * DTO expansion without making additional API calls per row.
         */
        const level =
            row?.levelName
            || row?.level
            || '';

        const className =
            row?.className
            || row?.class
            || '';

        if (
            filters.level
            && normalize(level)
                !== filters.level
        ) {
            return false;
        }

        if (
            filters.className
            && normalize(className)
                !== filters.className
        ) {
            return false;
        }

        if (
            filters.academicYear
            && String(row?.academicYear || '')
                !== filters.academicYear
        ) {
            return false;
        }

        if (
            filters.term
            && normalize(row?.term)
                !== filters.term
        ) {
            return false;
        }

        if (
            filters.employee
            && String(
                row?.verificationEmployeeId
                || ''
            ) !== filters.employee
        ) {
            return false;
        }

        const decision =
            row?.approvedAmount != null
                ? 'APPROVED'
                : row?.superAdminReviewStatus
                    || '';

        if (
            filters.decision
            && normalize(decision)
                !== filters.decision
        ) {
            return false;
        }

        return true;
    }

    function getFilteredRows() {
        const filters =
            currentFilters();

        return state.rows.filter(
            row =>
                rowMatches(
                    row,
                    filters
                )
        );
    }

    function addOptionIfMissing(
        select,
        value,
        label
    ) {
        if (!select || !value) {
            return;
        }

        const exists =
            Array.from(
                select.options
            ).some(
                option =>
                    String(option.value)
                    === String(value)
            );

        if (exists) {
            return;
        }

        const option =
            document.createElement(
                'option'
            );

        option.value =
            String(value);

        option.textContent =
            label || String(value);

        select.appendChild(option);
    }

    function populateFilterOptions() {
        const status =
            $('scholarship-applications-status');

        const verification =
            $('scholarship-applications-verification');

        const academicYear =
            $('scholarship-applications-academic-year');

        const employee =
            $('scholarship-applications-employee');

        const decision =
            $('scholarship-applications-decision');

        const level =
            $('scholarship-applications-level');

        const classSelect =
            $('scholarship-applications-class');

        state.rows.forEach(row => {
            addOptionIfMissing(
                status,
                row?.scholarshipStatus,
                enumLabel(
                    row?.scholarshipStatus
                )
            );

            addOptionIfMissing(
                verification,
                row?.verificationStatus,
                enumLabel(
                    row?.verificationStatus
                )
            );

            addOptionIfMissing(
                academicYear,
                row?.academicYear,
                row?.academicYear
            );

            if (row?.verificationEmployeeId) {
                addOptionIfMissing(
                    employee,
                    row.verificationEmployeeId,
                    row.verificationEmployeeName
                        || `Employee ${row.verificationEmployeeId}`
                );
            }

            if (row?.superAdminReviewStatus) {
                addOptionIfMissing(
                    decision,
                    row.superAdminReviewStatus,
                    enumLabel(
                        row.superAdminReviewStatus
                    )
                );
            }

            if (
                row?.approvedAmount !== null
                && row?.approvedAmount !== undefined
            ) {
                addOptionIfMissing(
                    decision,
                    'APPROVED',
                    'Approved'
                );
            }

            const levelValue =
                row?.levelName
                || row?.level;

            const classValue =
                row?.className
                || row?.class;

            if (levelValue) {
                addOptionIfMissing(
                    level,
                    levelValue,
                    levelValue
                );
            }

            if (classValue) {
                addOptionIfMissing(
                    classSelect,
                    classValue,
                    classValue
                );
            }
        });
    }

    function renderRows() {
        const tbody =
            $('scholarship-applications-table-body');

        if (!tbody) {
            return;
        }

        state.filteredRows =
            getFilteredRows();

        const total =
            state.filteredRows.length;

        const totalPages =
            Math.max(
                1,
                Math.ceil(
                    total / state.pageSize
                )
            );

        if (state.page > totalPages) {
            state.page =
                totalPages;
        }

        const start =
            (state.page - 1)
            * state.pageSize;

        const rows =
            state.filteredRows.slice(
                start,
                start + state.pageSize
            );

        tbody.replaceChildren();

        if (!rows.length) {
            const tr =
                document.createElement('tr');

            const td =
                document.createElement('td');

            td.colSpan = 8;
            td.className =
                'text-center p-4 text-muted';
            td.textContent =
                'No Scholarship applications found.';

            tr.appendChild(td);
            tbody.appendChild(tr);

            updatePagination(
                total,
                totalPages
            );

            return;
        }

        rows.forEach(row => {
            tbody.appendChild(
                buildRow(row)
            );
        });

        updatePagination(
            total,
            totalPages
        );
    }

    function buildRow(row) {
        const tr =
            document.createElement('tr');

        const applicationCell =
            document.createElement('td');

        const applicationLink =
            document.createElement('button');

        applicationLink.type =
            'button';

        applicationLink.className =
            'app-profile-link';

        applicationLink.textContent =
            text(row?.applicationNo);

        applicationLink.dataset.scholarshipId =
            String(row?.scholarshipAppId || '');

        applicationCell.appendChild(
            applicationLink
        );

        const studentCell =
            document.createElement('td');

        const studentLink =
            document.createElement('button');

        studentLink.type =
            'button';

        studentLink.className =
            'app-profile-link';

        studentLink.textContent =
            text(row?.studentName);

        studentLink.dataset.scholarshipId =
            String(row?.scholarshipAppId || '');

        studentCell.appendChild(
            studentLink
        );

        const classCell =
            document.createElement('td');

        classCell.textContent =
            [
                row?.className
                    || row?.class,
                row?.levelName
                    || row?.level
            ]
                .filter(Boolean)
                .join(' • ')
            || '—';

        const requestedCell =
            document.createElement('td');

        requestedCell.textContent =
            money(
                row?.amountRequestedUgx
            );

        const verificationCell =
            document.createElement('td');

        verificationCell.textContent =
            enumLabel(
                row?.verificationStatus
            );

        const statusCell =
            document.createElement('td');

        statusCell.textContent =
            enumLabel(
                row?.scholarshipStatus
            );

        const submittedCell =
            document.createElement('td');

        submittedCell.textContent =
            dateTime(
                row?.submittedAt
                || row?.createdAt
            );

        const actionCell =
            document.createElement('td');

        actionCell.className =
            'align-center';

        const viewButton =
            document.createElement('button');

        viewButton.type =
            'button';

        viewButton.className =
            'btn-secondary btn-sm';

        viewButton.innerHTML =
            '<i class="bi bi-eye"></i> View';

        viewButton.dataset.scholarshipId =
            String(row?.scholarshipAppId || '');

        actionCell.appendChild(
            viewButton
        );

        tr.append(
            applicationCell,
            studentCell,
            classCell,
            requestedCell,
            verificationCell,
            statusCell,
            submittedCell,
            actionCell
        );

        return tr;
    }

    function updatePagination(
        total,
        totalPages
    ) {
        const info =
            $('scholarship-applications-page-info');

        if (info) {
            info.textContent =
                `Showing page ${state.page} of ${totalPages}`;
        }

        const previous =
            $('scholarship-applications-prev-btn');

        const next =
            $('scholarship-applications-next-btn');

        if (previous) {
            previous.disabled =
                state.page <= 1;
        }

        if (next) {
            next.disabled =
                state.page >= totalPages;
        }
    }

    async function loadScholarships() {
        let loaderToken = null;

        if (typeof showLoader === 'function') {
            loaderToken =
                showLoader(
                    'Loading Scholarship applications...'
                );
        }

        try {
            const response =
                await apiGet(
                    API_ROOT
                );

            const data =
                unwrap(response);

            state.rows =
                Array.isArray(data)
                    ? data
                    : [];

            state.page = 1;

            populateFilterOptions();
            renderRows();

        } catch (error) {
            console.error(
                'Scholarship applications could not be loaded.',
                error
            );

            const tbody =
                $('scholarship-applications-table-body');

            if (tbody) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="8"
                            class="text-center p-4 text-danger">
                            ${escapeHtml(
                                messageFromError(
                                    error,
                                    'Unable to load Scholarship applications.'
                                )
                            )}
                        </td>
                    </tr>
                `;
            }

            if (typeof showErrorMessage === 'function') {
                showErrorMessage(
                    messageFromError(
                        error,
                        'Unable to load Scholarship applications.'
                    )
                );
            }
        } finally {
            if (
                loaderToken
                && typeof hideLoader === 'function'
            ) {
                hideLoader(
                    loaderToken
                );
            }
        }
    }

    async function loadEmployees() {
        if (state.employees.length) {
            populateEmployeeSelect();
            return;
        }

        const response =
            await apiGet(
                `${API_ROOT}/verification-employees`
            );

        const data =
            unwrap(response);

        state.employees =
            Array.isArray(data)
                ? data
                : [];

        populateEmployeeSelect();
    }

    function populateEmployeeSelect() {
        const select =
            $('scholarship-processing-employee-select');

        if (!select) {
            return;
        }

        const selected =
            select.value;

        select.replaceChildren();

        const placeholder =
            document.createElement('option');

        placeholder.value = '';
        placeholder.textContent =
            'Select employee';

        select.appendChild(
            placeholder
        );

        state.employees.forEach(
            employee => {
                const option =
                    document.createElement(
                        'option'
                    );

                option.value =
                    String(
                        employee?.employeeId
                    );

                option.textContent =
                    [
                        employee?.fullName,
                        employee?.employeeNo,
                        employee?.designationName
                    ]
                        .filter(Boolean)
                        .join(' • ')
                    || `Employee ${employee?.employeeId}`;

                select.appendChild(
                    option
                );
            }
        );

        if (selected) {
            select.value =
                selected;
        }
    }

    async function openScholarshipApplication(
        scholarshipId
    ) {
        const id =
            Number(scholarshipId);

        if (
            !Number.isInteger(id)
            || id <= 0
        ) {
            return;
        }

        let loaderToken = null;

        try {
            if (typeof showLoader === 'function') {
                loaderToken =
                    showLoader(
                        'Opening Scholarship application...'
                    );
            }

            state.selectedId =
                id;

            const summaryResponse =
                await apiGet(
                    `${API_ROOT}/${encodeURIComponent(id)}`
                );

            const summary =
                unwrap(
                    summaryResponse
                );

            state.currentSummary =
                summary || null;

            let form = null;

            if (
                summary?.applicationId
            ) {
                try {
                    const formResponse =
                        await apiGet(
                            `${SCHOLARSHIP_FORM_ROOT}/${
                                encodeURIComponent(
                                    summary.applicationId
                                )
                            }/workflow/scholarship/application-form`
                        );

                    form =
                        unwrap(
                            formResponse
                        );
                } catch (formError) {
                    /*
                     * The summary endpoint is still useful if the detailed
                     * form endpoint is unavailable for an old/non-editable
                     * record. Do not hide the workflow summary.
                     */
                    console.warn(
                        'Full Scholarship form could not be loaded.',
                        formError
                    );
                }
            }

            state.currentForm =
                form || null;

            renderDetail(
                state.currentSummary,
                state.currentForm
            );

            await loadEmployees();

            showDetail();

            window.scrollTo(
                {
                    top: 0,
                    behavior: 'smooth'
                }
            );

        } catch (error) {
            console.error(
                'Scholarship application could not be opened.',
                error
            );

            if (typeof showErrorMessage === 'function') {
                showErrorMessage(
                    messageFromError(
                        error,
                        'Unable to open Scholarship application.'
                    )
                );
            }
        } finally {
            if (
                loaderToken
                && typeof hideLoader === 'function'
            ) {
                hideLoader(
                    loaderToken
                );
            }
        }
    }

    function renderDetail(
        summary,
        form
    ) {
        const data = {
            ...(summary || {}),
            ...(form || {})
        };

        setText(
            'scholarship-detail-student-name',
            data.studentName
        );

        setText(
            'scholarship-detail-application-number',
            data.applicationNo
        );

        setText(
            'scholarship-detail-application-no',
            data.applicationNo
        );

        setText(
            'scholarship-detail-student',
            data.studentName
        );

        setText(
            'scholarship-detail-class-level',
            data.className
            || [
                data.className,
                data.levelName
            ].filter(Boolean).join(' • ')
        );

        setText(
            'scholarship-detail-academic-year',
            data.academicYear
        );

        setMoney(
            'scholarship-detail-total-fee',
            data.totalFee
        );

        setMoney(
            'scholarship-detail-parent-contribution',
            data.parentContribution
        );

        setMoney(
            'scholarship-detail-required',
            data.scholarshipRequiredAmount
            ?? data.amountRequestedUgx
        );

        setText(
            'scholarship-detail-status',
            enumLabel(
                data.applicationStatus
                || data.currentStage
            )
        );

        setText(
            'scholarship-detail-scholarship-status',
            enumLabel(
                data.scholarshipStatus
                || data.status
            )
        );

        setText(
            'scholarship-detail-father-name',
            data.fatherName
        );

        setText(
            'scholarship-detail-father-contact',
            data.fatherContact
        );

        setText(
            'scholarship-detail-father-email',
            data.fatherEmail
        );

        setText(
            'scholarship-detail-father-occupation',
            data.fatherOccupation
        );

        setText(
            'scholarship-detail-father-status',
            enumLabel(
                data.fatherStatus
            )
        );

        setMoney(
            'scholarship-detail-father-income',
            data.fatherAnnualIncome
        );

        setText(
            'scholarship-detail-mother-name',
            data.motherName
        );

        setText(
            'scholarship-detail-mother-contact',
            data.motherContact
        );

        setText(
            'scholarship-detail-mother-email',
            data.motherEmail
        );

        setText(
            'scholarship-detail-mother-occupation',
            data.motherOccupation
        );

        setText(
            'scholarship-detail-mother-status',
            enumLabel(
                data.motherStatus
            )
        );

        setMoney(
            'scholarship-detail-mother-income',
            data.motherAnnualIncome
        );

        setText(
            'scholarship-detail-responsible-type',
            enumLabel(
                data.responsiblePersonType
            )
        );

        setText(
            'scholarship-detail-responsible-name',
            data.responsiblePersonName
        );

        setText(
            'scholarship-detail-responsible-relation',
            data.responsiblePersonRelation
        );

        setText(
            'scholarship-detail-responsible-occupation',
            data.responsiblePersonOccupation
        );

        setText(
            'scholarship-detail-responsible-mobile',
            data.responsiblePersonMobile
        );

        setMoney(
            'scholarship-detail-responsible-income',
            data.responsiblePersonAnnualIncome
        );

        setText(
            'scholarship-detail-orphan-status',
            enumLabel(
                data.orphanStatus
            )
        );

        setText(
            'scholarship-detail-household-size',
            data.householdSize
        );

        setText(
            'scholarship-detail-dependants',
            data.dependantsCount
        );

        setText(
            'scholarship-detail-school-going',
            data.schoolGoingChildren
        );

        setText(
            'scholarship-detail-main-income-earner',
            data.mainIncomeEarner
        );

        setText(
            'scholarship-detail-income-source',
            data.incomeSource
        );

        setMoney(
            'scholarship-detail-other-income',
            data.otherHouseholdIncome
        );

        setMoney(
            'scholarship-detail-household-income',
            data.householdIncome
        );

        setText(
            'scholarship-detail-housing-status',
            enumLabel(
                data.housingStatus
            )
        );

        setBoolean(
            'scholarship-detail-land-owned',
            data.landOwned
        );

        setText(
            'scholarship-detail-land-area',
            data.landArea
        );

        setText(
            'scholarship-detail-land-unit',
            data.landUnit
        );

        setBoolean(
            'scholarship-detail-vehicles-owned',
            data.vehiclesOwned
        );

        setText(
            'scholarship-detail-vehicle-count',
            data.vehicleCount
        );

        setText(
            'scholarship-detail-vehicle-description',
            data.vehicleDescription
        );

        setText(
            'scholarship-detail-hardship-reason',
            data.financialHardshipReason
        );

        setText(
            'scholarship-detail-family-remarks',
            data.familySituationRemarks
        );

        setText(
            'scholarship-detail-declaration-name',
            data.parentGuardianName
        );

        setText(
            'scholarship-detail-declaration-relation',
            data.parentGuardianRelation
        );

        setText(
            'scholarship-detail-declaration-mobile',
            data.parentGuardianMobile
        );

        setBoolean(
            'scholarship-detail-declaration-accepted',
            data.declarationAccepted
        );

        renderSiblings(
            data.siblings
        );

        renderDocuments(
            data.documents
        );

        renderProcessing(
            summary
        );
    }

    function renderSiblings(
        siblings
    ) {
        const body =
            $('scholarship-detail-siblings-body');

        if (!body) {
            return;
        }

        body.replaceChildren();

        if (
            !Array.isArray(siblings)
            || siblings.length === 0
        ) {
            const tr =
                document.createElement('tr');

            const td =
                document.createElement('td');

            td.colSpan = 7;
            td.className =
                'text-center text-muted';
            td.textContent =
                'No sibling information available.';

            tr.appendChild(td);
            body.appendChild(tr);
            return;
        }

        siblings.forEach(
            sibling => {
                const tr =
                    document.createElement('tr');

                [
                    sibling?.siblingName,
                    sibling?.age,
                    enumLabel(
                        sibling?.currentStatus
                    ),
                    sibling?.classOrCourse,
                    sibling?.institution,
                    sibling?.occupation,
                    money(
                        sibling?.annualIncome
                    )
                ].forEach(
                    value => {
                        const td =
                            document.createElement(
                                'td'
                            );

                        td.textContent =
                            text(value);

                        tr.appendChild(td);
                    }
                );

                body.appendChild(tr);
            }
        );
    }

    function renderDocuments(
        documents
    ) {
        const body =
            $('scholarship-detail-documents-body');

        if (!body) {
            return;
        }

        body.replaceChildren();

        if (
            !Array.isArray(documents)
            || documents.length === 0
        ) {
            const tr =
                document.createElement('tr');

            const td =
                document.createElement('td');

            td.colSpan = 4;
            td.className =
                'text-center text-muted';
            td.textContent =
                'No Scholarship document data is available from the current form response.';

            tr.appendChild(td);
            body.appendChild(tr);
            return;
        }

        documents.forEach(
            documentItem => {
                const tr =
                    document.createElement('tr');

                const values = [
                    documentItem?.documentType
                        || documentItem?.originalFileName
                        || 'Document',
                    enumLabel(
                        documentItem?.verificationStatus
                    ),
                    dateTime(
                        documentItem?.uploadedAt
                    )
                ];

                values.forEach(
                    value => {
                        const td =
                            document.createElement(
                                'td'
                            );

                        td.textContent =
                            text(value);

                        tr.appendChild(td);
                    }
                );

                const action =
                    document.createElement('td');

                action.className =
                    'align-center';

                if (
                    documentItem?.url
                    || documentItem?.downloadUrl
                ) {
                    const link =
                        document.createElement('a');

                    link.href =
                        documentItem.url
                        || documentItem.downloadUrl;

                    link.target =
                        '_blank';

                    link.rel =
                        'noopener';

                    link.className =
                        'btn-secondary btn-sm';

                    link.innerHTML =
                        '<i class="bi bi-eye"></i> View';

                    action.appendChild(link);
                } else {
                    action.textContent =
                        '—';
                }

                tr.appendChild(action);
                body.appendChild(tr);
            }
        );
    }

    function renderProcessing(
        summary
    ) {
        setText(
            'scholarship-processing-employee',
            summary?.verificationEmployeeName
        );

        setText(
            'scholarship-processing-verification-status',
            enumLabel(
                summary?.verificationStatus
            )
        );

        setDateTime(
            'scholarship-processing-verification-completed',
            summary?.verificationCompletedAt
        );

        setText(
            'scholarship-processing-review-verification',
            enumLabel(
                summary?.verificationStatus
            )
        );

        setText(
            'scholarship-processing-shortlist-status',
            enumLabel(
                summary?.schoolReviewStatus
            )
        );

        setText(
            'scholarship-processing-final-decision',
            enumLabel(
                summary?.superAdminReviewStatus
            )
        );

        setText(
            'scholarship-processing-decision-status',
            enumLabel(
                summary?.superAdminReviewStatus
            )
        );

        setMoney(
            'scholarship-processing-approved-amount',
            summary?.approvedAmount
        );

        setText(
            'scholarship-processing-funding',
            summary?.scholarshipType
                || summary?.applicationMethod
        );

        const employeeSelect =
            $('scholarship-processing-employee-select');

        if (employeeSelect) {
            employeeSelect.value =
                summary?.verificationEmployeeId
                    ? String(
                        summary.verificationEmployeeId
                    )
                    : '';
        }

        const completeButton =
            $('scholarship-processing-complete-btn');

        if (completeButton) {
            completeButton.disabled =
                !summary?.verificationEmployeeId
                || normalize(
                    summary?.verificationStatus
                ) === 'COMPLETED';
        }

        /*
         * Current backend exposes shortlist/final decision to SUPER_ADMIN,
         * not BRANCH_ADMIN. Keep these controls non-operative here until the
         * Branch Admin permission/workflow is explicitly changed.
         */
        const shortlist =
            $('scholarship-processing-shortlist-btn');

        const reject =
            $('scholarship-processing-reject-btn');

        const decision =
            $('scholarship-processing-decision-btn');

        [
            shortlist,
            reject,
            decision
        ].forEach(
            button => {
                if (button) {
                    button.disabled = true;
                    button.title =
                        'This action is handled by the authorized Super Admin review workflow.';
                }
            }
        );
    }

    async function assignEmployee() {
        if (
            !state.selectedId
        ) {
            return;
        }

        const employeeSelect =
            $('scholarship-processing-employee-select');

        const employeeId =
            Number(
                employeeSelect?.value
            );

        if (
            !Number.isInteger(employeeId)
            || employeeId <= 0
        ) {
            if (typeof showErrorMessage === 'function') {
                showErrorMessage(
                    'Select a Scholarship verification employee.'
                );
            }
            return;
        }

        const button =
            $('scholarship-processing-assign-btn');

        let loaderToken = null;

        try {
            setBusy(
                button,
                true,
                'Assigning...'
            );

            if (typeof showLoader === 'function') {
                loaderToken =
                    showLoader(
                        'Assigning Scholarship verification employee...'
                    );
            }

            const response =
                await apiPost(
                    `${API_ROOT}/${
                        encodeURIComponent(
                            state.selectedId
                        )
                    }/verification/assign`,
                    {
                        useEntranceTestEmployee:
                            false,
                        employeeId
                    }
                );

            const updated =
                unwrap(response);

            if (updated) {
                state.currentSummary =
                    updated;
            }

            showSuccess(
                response?.message
                || 'Scholarship verification employee assigned successfully.'
            );

            await refreshDetail();
            await loadScholarships();

        } catch (error) {
            console.error(
                'Scholarship verification assignment failed.',
                error
            );

            showError(
                messageFromError(
                    error,
                    'Unable to assign Scholarship verification employee.'
                )
            );
        } finally {
            if (
                loaderToken
                && typeof hideLoader === 'function'
            ) {
                hideLoader(
                    loaderToken
                );
            }

            setBusy(
                button,
                false
            );
        }
    }

    async function completeVerification() {
        if (
            !state.selectedId
        ) {
            return;
        }

        const remarks =
            $('scholarship-processing-verification-remarks')
                ?.value
                ?.trim()
            || '';

        const button =
            $('scholarship-processing-complete-btn');

        let loaderToken = null;

        try {
            setBusy(
                button,
                true,
                'Completing...'
            );

            if (typeof showLoader === 'function') {
                loaderToken =
                    showLoader(
                        'Completing Scholarship verification...'
                    );
            }

            const response =
                await apiPost(
                    `${API_ROOT}/${
                        encodeURIComponent(
                            state.selectedId
                        )
                    }/verification/complete`,
                    {
                        remarks
                    }
                );

            const updated =
                unwrap(response);

            if (updated) {
                state.currentSummary =
                    updated;
            }

            showSuccess(
                response?.message
                || 'Scholarship verification completed successfully.'
            );

            await refreshDetail();
            await loadScholarships();

        } catch (error) {
            console.error(
                'Scholarship verification completion failed.',
                error
            );

            showError(
                messageFromError(
                    error,
                    'Unable to complete Scholarship verification.'
                )
            );
        } finally {
            if (
                loaderToken
                && typeof hideLoader === 'function'
            ) {
                hideLoader(
                    loaderToken
                );
            }

            setBusy(
                button,
                false
            );
        }
    }

    async function refreshDetail() {
        if (!state.selectedId) {
            return;
        }

        const response =
            await apiGet(
                `${API_ROOT}/${
                    encodeURIComponent(
                        state.selectedId
                    )
                }`
            );

        const summary =
            unwrap(response);

        state.currentSummary =
            summary || state.currentSummary;

        let form =
            state.currentForm;

        if (
            summary?.applicationId
        ) {
            try {
                const formResponse =
                    await apiGet(
                        `${SCHOLARSHIP_FORM_ROOT}/${
                            encodeURIComponent(
                                summary.applicationId
                            )
                        }/workflow/scholarship/application-form`
                    );

                form =
                    unwrap(formResponse)
                    || form;
            } catch (error) {
                console.debug(
                    'Scholarship form refresh unavailable.',
                    error
                );
            }
        }

        state.currentForm =
            form || null;

        renderDetail(
            state.currentSummary,
            state.currentForm
        );
    }

    function resetFilters() {
        [
            'scholarship-applications-search',
            'scholarship-applications-status',
            'scholarship-applications-verification',
            'scholarship-applications-level',
            'scholarship-applications-class',
            'scholarship-applications-academic-year',
            'scholarship-applications-term',
            'scholarship-applications-employee',
            'scholarship-applications-decision'
        ].forEach(
            id => {
                const node = $(id);

                if (!node) {
                    return;
                }

                if (
                    node.tagName
                    === 'SELECT'
                ) {
                    node.value = '';
                } else {
                    node.value = '';
                }
            }
        );

        state.page = 1;
        renderRows();
    }

    function toggleMoreFilters() {
        const panel =
            $('scholarship-applications-advanced-filters');

        const button =
            $('scholarship-applications-more-filters-btn');

        if (!panel || !button) {
            return;
        }

        const hidden =
            panel.classList.toggle(
                'hidden'
            );

        button.setAttribute(
            'aria-expanded',
            String(!hidden)
        );
    }

    function bindEvents() {
        const viewRoot =
            root();

        if (!viewRoot) {
            return;
        }

        const refresh =
            $('scholarship-applications-refresh-btn');

        refresh?.addEventListener(
            'click',
            () => void loadScholarships()
        );

        $('scholarship-applications-search-btn')
            ?.addEventListener(
                'click',
                () => {
                    state.page = 1;
                    renderRows();
                }
            );

        $('scholarship-applications-reset-btn')
            ?.addEventListener(
                'click',
                resetFilters
            );

        $('scholarship-applications-more-filters-btn')
            ?.addEventListener(
                'click',
                toggleMoreFilters
            );

        [
            'scholarship-applications-status',
            'scholarship-applications-verification',
            'scholarship-applications-level',
            'scholarship-applications-class',
            'scholarship-applications-academic-year',
            'scholarship-applications-term',
            'scholarship-applications-employee',
            'scholarship-applications-decision'
        ].forEach(
            id => {
                $(id)?.addEventListener(
                    'change',
                    () => {
                        state.page = 1;
                        renderRows();
                    }
                );
            }
        );

        $('scholarship-applications-search')
            ?.addEventListener(
                'keydown',
                event => {
                    if (
                        event.key === 'Enter'
                    ) {
                        event.preventDefault();
                        state.page = 1;
                        renderRows();
                    }
                }
            );

        $('scholarship-applications-page-size')
            ?.addEventListener(
                'change',
                event => {
                    state.pageSize =
                        Number(
                            event.target.value
                        )
                        || 10;

                    state.page = 1;
                    renderRows();
                }
            );

        $('scholarship-applications-prev-btn')
            ?.addEventListener(
                'click',
                () => {
                    if (state.page > 1) {
                        state.page -= 1;
                        renderRows();
                    }
                }
            );

        $('scholarship-applications-next-btn')
            ?.addEventListener(
                'click',
                () => {
                    const totalPages =
                        Math.max(
                            1,
                            Math.ceil(
                                state.filteredRows.length
                                / state.pageSize
                            )
                        );

                    if (
                        state.page
                        < totalPages
                    ) {
                        state.page += 1;
                        renderRows();
                    }
                }
            );

        $('scholarship-detail-back-btn')
            ?.addEventListener(
                'click',
                () => {
                    showList();
                    window.scrollTo(
                        {
                            top: 0,
                            behavior: 'smooth'
                        }
                    );
                }
            );

        $('scholarship-detail-refresh-btn')
            ?.addEventListener(
                'click',
                () => void refreshDetail()
            );

        $('scholarship-processing-assign-btn')
            ?.addEventListener(
                'click',
                () => void assignEmployee()
            );

        $('scholarship-processing-complete-btn')
            ?.addEventListener(
                'click',
                () => void completeVerification()
            );

        viewRoot.addEventListener(
            'click',
            event => {
                const target =
                    event.target.closest(
                        '[data-scholarship-id]'
                    );

                if (!target) {
                    return;
                }

                event.preventDefault();

                void openScholarshipApplication(
                    target.dataset.scholarshipId
                );
            }
        );
    }

    function showSuccess(message) {
        if (
            typeof showSuccessMessage
            === 'function'
        ) {
            showSuccessMessage(
                message
            );
        }
    }

    function showError(message) {
        if (
            typeof showErrorMessage
            === 'function'
        ) {
            showErrorMessage(
                message
            );
        }
    }

    function escapeHtml(value) {
        return String(value || '')
            .replace(
                /&/g,
                '&amp;'
            )
            .replace(
                /</g,
                '&lt;'
            )
            .replace(
                />/g,
                '&gt;'
            )
            .replace(
                /"/g,
                '&quot;'
            )
            .replace(
                /'/g,
                '&#039;'
            );
    }

    async function init() {
        const viewRoot =
            root();

        if (!viewRoot) {
            return;
        }

        if (state.initialized) {
            return;
        }

        state.initialized = true;

        bindEvents();

        await loadScholarships();
    }

    document.addEventListener(
        'viewLoaded',
        event => {
            const detail =
                event?.detail || {};

            const viewName =
                detail.view
                || detail.viewName;

            const role =
                detail.role;

            if (
                role
                && role !== 'admin'
                && role !== 'branchadmin'
            ) {
                return;
            }

            if (
                viewName === 'scholarship-applications'
                || viewName === 'branch-scholarship-applications'
            ) {
                void init();
            }
        }
    );

    document.addEventListener(
        'DOMContentLoaded',
        () => {
            if (root()) {
                void init();
            }
        }
    );

    window.ScholarshipApplications =
        {
            init,
            reload:
                loadScholarships,
            open:
                openScholarshipApplication
        };

})();

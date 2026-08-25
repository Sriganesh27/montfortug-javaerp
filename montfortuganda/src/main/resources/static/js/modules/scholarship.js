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
     * Branch Admin can complete verification and submit the branch shortlist
     * decision. Final decision remains protected by the Super Admin backend role.
     *
     * The same module also drives scholarship-overview.html so there is only
     * one Scholarship Admin JS module.
     */

    const API_ROOT =
        '/branchadmin/scholarships';


    const state = {
        initialized: false,
        rows: [],
        filteredRows: [],
        selectedId: null,
        currentSummary: null,
        currentForm: null,
        employees: [],
        overview: null,
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

    /*
     * Scholarship API payloads intentionally vary between the summary DTO
     * and the full application-form DTO. Use bracket access for optional
     * fields so the module remains compatible with both payload shapes and
     * does not depend on an inferred DTO type in the IDE.
     */
    function field(object, name) {
        if (!object || typeof object !== 'object') {
            return undefined;
        }
        return object[name];
    }

    function normalize(value) {
        return String(value || '')
            .trim()
            .toUpperCase()
            .replace(/[\s-]+/g, '_');
    }

    function scholarshipStatus(row) {
        return field(row, 'scholarshipStatus')
            || field(row, 'status')
            || field(row, 'schoolReviewStatus')
            || '';
    }

    function classLabel(row) {
        return [
            field(row, 'className')
                || field(row, 'class'),
            field(row, 'levelName')
                || field(row, 'level')
        ]
            .filter(value =>
                value !== null
                && value !== undefined
                && String(value).trim() !== ''
            )
            .map(value => String(value).trim())
            .join(' • ')
            || '—';
    }

    function submittedValue(row) {
        return field(row, 'submittedAt')
            || field(row, 'createdAt')
            || field(row, 'applicationSubmittedAt');
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

        if (window.erpDate) {
            return window.erpDate.formatDateTime(
                value,
                ''
            );
        }

        return String(value);
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
            field(row, 'applicationNo'),
            row?.studentName,
            row?.academicYear,
            row?.term,
            scholarshipStatus(row),
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
            && normalize(scholarshipStatus(row))
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
            field(row, 'superAdminReviewStatus')
            || (
                field(row, 'approvedAmount') != null
                    ? 'APPROVED'
                    : ''
            );

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
                scholarshipStatus(row),
                enumLabel(
                    scholarshipStatus(row)
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
                    field(row, 'verificationEmployeeName')
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
                field(row, 'className')
                || field(row, 'class')
                || field(row, 'classLevel');

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
            'app-profile-link app-profile-link-appno';

        applicationLink.textContent =
            text(field(row, 'applicationNo'));

        applicationLink.dataset.scholarshipId =
            String(row?.scholarshipAppId || '');

        applicationCell.className = 'td-appno';
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
            'app-profile-link app-profile-link-name';

        studentLink.textContent =
            text(row?.studentName);

        studentLink.dataset.scholarshipId =
            String(row?.scholarshipAppId || '');

        studentCell.className = 'td-name';
        studentCell.appendChild(
            studentLink
        );

        const classCell =
            document.createElement('td');

        classCell.className = 'td-placement';
        classCell.textContent =
            classLabel(row);

        const requestedCell =
            document.createElement('td');

        requestedCell.className = 'td-scholarship';
        requestedCell.textContent =
            money(
                row?.amountRequestedUgx
            );

        const verificationCell =
            document.createElement('td');

        verificationCell.className = 'td-status';
        verificationCell.textContent =
            enumLabel(
                row?.verificationStatus
            );

        const statusCell =
            document.createElement('td');

        statusCell.className = 'td-status';
        statusCell.textContent =
            enumLabel(
                scholarshipStatus(row)
            );

        const submittedCell =
            document.createElement('td');

        submittedCell.className = 'td-date';
        submittedCell.textContent =
            dateTime(
                submittedValue(row)
            );

        const actionCell =
            document.createElement('td');

        actionCell.className =
            'align-center col-action';

        const viewButton =
            document.createElement('button');

        viewButton.type =
            'button';

        viewButton.className =
            'btn-secondary btn-sm btn-view';

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
            showError(
                'The selected Scholarship application is invalid.'
            );
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

            state.selectedId = id;

            /*
             * The Branch Scholarship list endpoint contains the Scholarship
             * workflow summary (verification, shortlist, final review, etc.).
             * The /{scholarshipAppId} endpoint returns the detailed
             * Scholarship application form. Keep these two payloads separate.
             */
            const summary =
                state.rows.find(
                    row =>
                        Number(row?.scholarshipAppId)
                        === id
                )
                || null;

            if (!summary) {
                throw new Error(
                    'Scholarship application was not found in the current branch list.'
                );
            }

            if (!summary.applicationId) {
                throw new Error(
                    'This Scholarship application is not linked to an admission application.'
                );
            }

            state.currentSummary = summary;

            const detailResponse =
                await apiGet(
                    `${API_ROOT}/${encodeURIComponent(id)}`
                );

            state.currentForm =
                unwrap(detailResponse)
                || null;

            renderDetail(
                state.currentSummary,
                state.currentForm
            );

            /*
             * Employee options are needed only for Scholarship processing.
             * They must never prevent the application detail from opening.
             */
            showDetail();

            window.scrollTo(
                {
                    top: 0,
                    behavior: 'smooth'
                }
            );

            loadEmployees()
                .catch(error => {
                    console.warn(
                        'Scholarship verification employees could not be loaded.',
                        error
                    );
                });

        } catch (error) {
            console.error(
                'Scholarship application could not be opened.',
                error
            );

            showError(
                messageFromError(
                    error,
                    'Unable to open Scholarship application.'
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
            field(data, 'applicationNo')
        );

        setText(
            'scholarship-detail-application-no',
            field(data, 'applicationNo')
        );

        setText(
            'scholarship-detail-student',
            data.studentName
        );

        setText(
            'scholarship-detail-class-level',
            field(data, 'classLevel')
            || [
                field(data, 'levelName'),
                field(data, 'className')
            ].filter(Boolean).join(' • ')
        );

        setText(
            'scholarship-detail-academic-year',
            data.academicYear
        );

        setMoney(
            'scholarship-detail-total-fee',
            field(data, 'totalFee')
        );

        setMoney(
            'scholarship-detail-parent-contribution',
            field(data, 'parentContribution')
        );

        setMoney(
            'scholarship-detail-required',
            field(data, 'scholarshipRequiredAmount')
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
                field(data, 'fatherStatus')
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
                field(data, 'motherStatus')
            )
        );

        setMoney(
            'scholarship-detail-mother-income',
            data.motherAnnualIncome
        );

        setText(
            'scholarship-detail-responsible-type',
            enumLabel(
                field(data, 'responsiblePersonType')
            )
        );

        setText(
            'scholarship-detail-responsible-name',
            field(data, 'responsiblePersonName')
        );

        setText(
            'scholarship-detail-responsible-relation',
            field(data, 'responsiblePersonRelation')
        );

        setText(
            'scholarship-detail-responsible-occupation',
            field(data, 'responsiblePersonOccupation')
        );

        setText(
            'scholarship-detail-responsible-mobile',
            field(data, 'responsiblePersonMobile')
        );

        setMoney(
            'scholarship-detail-responsible-income',
            field(data, 'responsiblePersonAnnualIncome')
        );

        setText(
            'scholarship-detail-orphan-status',
            enumLabel(
                field(data, 'orphanStatus')
            )
        );

        setText(
            'scholarship-detail-household-size',
            field(data, 'householdSize')
        );

        setText(
            'scholarship-detail-dependants',
            field(data, 'dependantsCount')
        );

        setText(
            'scholarship-detail-school-going',
            field(data, 'schoolGoingChildren')
        );

        setText(
            'scholarship-detail-main-income-earner',
            field(data, 'mainIncomeEarner')
        );

        setText(
            'scholarship-detail-income-source',
            field(data, 'incomeSource')
        );

        setMoney(
            'scholarship-detail-other-income',
            field(data, 'otherHouseholdIncome')
        );

        setMoney(
            'scholarship-detail-household-income',
            field(data, 'householdIncome')
        );

        setText(
            'scholarship-detail-housing-status',
            enumLabel(
                field(data, 'housingStatus')
            )
        );

        setBoolean(
            'scholarship-detail-land-owned',
            field(data, 'landOwned')
        );

        setText(
            'scholarship-detail-land-area',
            field(data, 'landArea')
        );

        setText(
            'scholarship-detail-land-unit',
            field(data, 'landUnit')
        );

        setBoolean(
            'scholarship-detail-vehicles-owned',
            field(data, 'vehiclesOwned')
        );

        setText(
            'scholarship-detail-vehicle-count',
            field(data, 'vehicleCount')
        );

        setText(
            'scholarship-detail-vehicle-description',
            field(data, 'vehicleDescription')
        );

        setText(
            'scholarship-detail-hardship-reason',
            field(data, 'financialHardshipReason')
        );

        setText(
            'scholarship-detail-family-remarks',
            field(data, 'familySituationRemarks')
        );

        setText(
            'scholarship-detail-declaration-name',
            field(data, 'parentGuardianName')
        );

        setText(
            'scholarship-detail-declaration-relation',
            field(data, 'parentGuardianRelation')
        );

        setText(
            'scholarship-detail-declaration-mobile',
            field(data, 'parentGuardianMobile')
        );

        setBoolean(
            'scholarship-detail-declaration-accepted',
            field(data, 'declarationAccepted')
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

            td.colSpan = 3;
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
            field(summary, 'verificationEmployeeName')
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

        const shortlist =
            $('scholarship-processing-shortlist-btn');

        const reject =
            $('scholarship-processing-reject-btn');

        const decision =
            $('scholarship-processing-decision-btn');

        const verified =
            normalize(summary?.verificationStatus) === 'COMPLETED';

        const shortlisted =
            normalize(summary?.schoolReviewStatus) === 'SHORTLISTED';

        if (shortlist) {
            shortlist.disabled = !verified || shortlisted;
            shortlist.title =
                !verified
                    ? 'Complete Scholarship verification first.'
                    : 'Shortlist this Scholarship application.';
        }

        if (reject) {
            reject.disabled = !verified;
            reject.title =
                !verified
                    ? 'Complete Scholarship verification first.'
                    : 'Reject this Scholarship application.';
        }

        if (decision) {
            decision.classList.add('hidden');
        }

        renderFinalDecisionForm(summary);
        renderScholarshipHistory(summary);
        renderDonorMapping(summary);
    }


    function toggleElement(element, visible) {
        if (!element) {
            return;
        }
        element.classList.toggle('hidden', !visible);
        element.hidden = !visible;
    }

    function renderFinalDecisionForm(summary) {
        const form = $('scholarship-final-decision-form');

        if (form) {
            form.classList.add('hidden');
        }

        toggleElement($('scholarship-direct-donor-section'), false);
        toggleElement($('scholarship-partial-donor-section'), false);
    }

    function renderScholarshipHistory(summary) {
        const body = $('scholarship-history-body');
        if (!body) {
            return;
        }

        body.replaceChildren();

        const history =
            Array.isArray(summary?.scholarshipHistory)
                ? summary.scholarshipHistory
                : Array.isArray(summary?.history)
                    ? summary.history
                    : [];

        if (!history.length) {
            const tr = document.createElement('tr');
            const td = document.createElement('td');
            td.colSpan = 5;
            td.className = 'text-center text-muted';
            td.textContent = 'No Scholarship history available.';
            tr.appendChild(td);
            body.appendChild(tr);
            return;
        }

        history.forEach(item => {
            const tr = document.createElement('tr');

            [
                item?.date || item?.createdAt || item?.timestamp,
                item?.action || item?.event || item?.type,
                item?.status || item?.newStatus,
                item?.remarks || item?.description,
                item?.by || item?.createdBy || item?.updatedBy
            ].forEach((value, index) => {
                const td = document.createElement('td');
                td.textContent =
                    index === 0
                        ? dateTime(value)
                        : index === 2
                            ? enumLabel(value)
                            : text(value);
                tr.appendChild(td);
            });

            body.appendChild(tr);
        });
    }

    function renderDonorMapping(summary) {
        const body = $('scholarship-donor-mapping-body');
        if (!body) {
            return;
        }

        body.replaceChildren();

        const mappings =
            Array.isArray(summary?.donorMappings)
                ? summary.donorMappings
                : Array.isArray(summary?.donorMapping)
                    ? summary.donorMapping
                    : [];

        if (!mappings.length) {
            const tr = document.createElement('tr');
            const td = document.createElement('td');
            td.colSpan = 3;
            td.className = 'text-center text-muted';
            td.textContent = 'No donor mappings available.';
            tr.appendChild(td);
            body.appendChild(tr);
            return;
        }

        mappings.forEach(mapping => {
            const tr = document.createElement('tr');

            [
                mapping?.donorName || mapping?.donor,
                mapping?.amount || mapping?.allocatedAmount,
                mapping?.percentage,
                mapping?.status
            ].forEach((value, index) => {
                const td = document.createElement('td');
                td.textContent =
                    index === 1
                        ? money(value)
                        : index === 2
                            ? text(value)
                            : enumLabel(value);
                tr.appendChild(td);
            });

            body.appendChild(tr);
        });
    }

    async function submitShortlistDecision(decisionValue) {
        if (!state.selectedId) {
            return;
        }

        if (
            normalize(state.currentSummary?.verificationStatus)
            !== 'COMPLETED'
        ) {
            showError(
                'Complete Scholarship verification before making a shortlist decision.'
            );
            return;
        }

        const remarks =
            $('scholarship-processing-verification-remarks')
                ?.value
                ?.trim()
            || '';

        const button =
            decisionValue === 'SHORTLISTED'
                ? $('scholarship-processing-shortlist-btn')
                : $('scholarship-processing-reject-btn');

        let loaderToken = null;

        try {
            setBusy(
                button,
                true,
                decisionValue === 'SHORTLISTED'
                    ? 'Shortlisting...'
                    : 'Rejecting...'
            );

            if (typeof showLoader === 'function') {
                loaderToken = showLoader(
                    decisionValue === 'SHORTLISTED'
                        ? 'Saving Scholarship shortlist decision...'
                        : 'Saving Scholarship rejection...'
                );
            }

            const response = await apiPost(
                `${API_ROOT}/${encodeURIComponent(state.selectedId)}/shortlist`,
                {
                    decision: decisionValue,
                    remarks
                }
            );

            showSuccess(
                response?.message ||
                (
                    decisionValue === 'SHORTLISTED'
                        ? 'Scholarship application shortlisted successfully.'
                        : 'Scholarship application rejected successfully.'
                )
            );

            await refreshDetail();
            await loadScholarships();

        } catch (error) {
            console.error(
                'Scholarship shortlist decision failed.',
                error
            );

            showError(
                messageFromError(
                    error,
                    'Unable to save Scholarship shortlist decision.'
                )
            );
        } finally {
            if (loaderToken && typeof hideLoader === 'function') {
                hideLoader(loaderToken);
            }
            setBusy(button, false);
        }
    }

    async function saveFinalDecision() {
        showError(
            'Final Scholarship decision is handled by the authorized Super Admin workflow.'
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

        const id =
            Number(state.selectedId);

        if (
            !Number.isInteger(id)
            || id <= 0
        ) {
            return;
        }

        try {
            /*
             * Refresh the Scholarship workflow summary from the list endpoint.
             * This is where verification/shortlist/final-review state lives.
             */
            const listResponse =
                await apiGet(API_ROOT);

            const rows =
                unwrap(listResponse);

            if (Array.isArray(rows)) {
                state.rows = rows;

                const refreshedSummary =
                    rows.find(
                        row =>
                            Number(row?.scholarshipAppId)
                            === id
                    );

                if (refreshedSummary) {
                    state.currentSummary =
                        refreshedSummary;
                }
            }

            /*
             * Refresh the detailed Scholarship form separately.
             */
            const detailResponse =
                await apiGet(
                    `${API_ROOT}/${encodeURIComponent(id)}`
                );

            state.currentForm =
                unwrap(detailResponse)
                || state.currentForm;

            renderDetail(
                state.currentSummary,
                state.currentForm
            );

            loadEmployees()
                .catch(error => {
                    console.warn(
                        'Scholarship verification employees could not be refreshed.',
                        error
                    );
                });

        } catch (error) {
            console.error(
                'Scholarship application detail could not be refreshed.',
                error
            );

            showError(
                messageFromError(
                    error,
                    'Unable to refresh Scholarship application.'
                )
            );
        }
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

        $('scholarship-processing-shortlist-btn')
            ?.addEventListener(
                'click',
                () => void submitShortlistDecision('SHORTLISTED')
            );

        $('scholarship-processing-reject-btn')
            ?.addEventListener(
                'click',
                () => void submitShortlistDecision('NOT_SHORTLISTED')
            );

        $('scholarship-processing-decision-btn')
            ?.addEventListener(
                'click',
                () => void saveFinalDecision()
            );

        $('scholarship-save-final-decision-btn')
            ?.addEventListener(
                'click',
                () => void saveFinalDecision()
            );

        viewRoot.addEventListener(
            'click',
            event => {
                const target =
                    event.target.closest(
                        '.app-profile-link[data-scholarship-id], '
                        + '.btn-view[data-scholarship-id]'
                    );

                if (!target
                        || !viewRoot.contains(target)) {
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


    function overviewRoot() {
        return $('scholarship-overview-view');
    }

    /*
     * Scholarship Overview
     *
     * The Overview is driven by the dedicated backend Overview API.
     * Do not rebuild Overview totals from the Applications list here.
     *
     * Backend endpoint:
     *   GET /api/branchadmin/scholarships/overview
     *
     * Optional query parameters:
     *   academicYear
     *   term
     *   levelId
     *   classId
     *
     * The response contains:
     *   selectedPeriod
     *   academicYearOverall
     *   branchOverall
     *
     * Applications workflow functions above remain unchanged.
     */

    const OVERVIEW_API_ROOT =
        `${API_ROOT}/overview`;

    function overviewFilterValue(
        ...ids
    ) {
        for (const id of ids) {
            const node = $(id);

            if (!node) {
                continue;
            }

            const value =
                String(
                    node.value ?? ''
                ).trim();

            if (value) {
                return value;
            }
        }

        return '';
    }

    function getOverviewFilters() {
        return {
            academicYear:
                overviewFilterValue(
                    'scholarship-overview-academic-year',
                    'scholarship-overview-year'
                ),

            term:
                overviewFilterValue(
                    'scholarship-overview-term'
                ),

            levelId:
                overviewFilterValue(
                    'scholarship-overview-level',
                    'scholarship-overview-level-id'
                ),

            classId:
                overviewFilterValue(
                    'scholarship-overview-class',
                    'scholarship-overview-class-id'
                )
        };
    }

    function buildOverviewUrl() {
        const filters =
            getOverviewFilters();

        const params =
            new URLSearchParams();

        if (filters.academicYear) {
            params.set(
                'academicYear',
                filters.academicYear
            );
        }

        if (filters.term) {
            params.set(
                'term',
                filters.term
            );
        }

        if (filters.levelId) {
            params.set(
                'levelId',
                filters.levelId
            );
        }

        if (filters.classId) {
            params.set(
                'classId',
                filters.classId
            );
        }

        const query =
            params.toString();

        return query
            ? `${OVERVIEW_API_ROOT}?${query}`
            : OVERVIEW_API_ROOT;
    }

    const OVERVIEW_REFERENCE_API =
        '/students/reference-data';

    const overviewReferenceState = {
        loaded: false,
        academicYears: [],
        academicTerms: [],
        levels: [],
        classes: []
    };

    function overviewArray(value) {
        return Array.isArray(value)
            ? value
            : [];
    }

    function overviewFirstDefined(
        object,
        ...names
    ) {
        if (
            !object
            || typeof object !== 'object'
        ) {
            return undefined;
        }

        for (const name of names) {
            const value =
                object[name];

            if (
                value !== null
                && value !== undefined
                && String(value).trim() !== ''
            ) {
                return value;
            }
        }

        return undefined;
    }

    function overviewNumber(value) {
        const number =
            Number(value);

        return Number.isFinite(number)
            ? number
            : null;
    }

    function normalizeOverviewReferenceData(
        responseData
    ) {
        const data =
            responseData
            && typeof responseData === 'object'
                ? responseData
                : {};

        overviewReferenceState.academicYears =
            overviewArray(
                data.academicYears
            );

        overviewReferenceState.academicTerms =
            overviewArray(
                overviewFirstDefined(
                    data,
                    'academicTerms',
                    'academicTermOptions',
                    'terms',
                    'termOptions'
                )
            );

        overviewReferenceState.levels =
            overviewArray(
                overviewFirstDefined(
                    data,
                    'levels',
                    'levelOptions'
                )
            );

        overviewReferenceState.classes =
            overviewArray(
                data.classes
            );

        overviewReferenceState.loaded = true;
    }

    function overviewAcademicYearId(
        academicYear
    ) {
        return overviewNumber(
            overviewFirstDefined(
                academicYear,
                'academicYearId',
                'id'
            )
        );
    }

    function overviewAcademicYearCode(
        academicYear
    ) {
        return String(
            overviewFirstDefined(
                academicYear,
                'academicYearCode',
                'code',
                'academicYearName'
            )
            ?? ''
        ).trim();
    }

    function overviewTermAcademicYearId(
        term
    ) {
        return overviewNumber(
            overviewFirstDefined(
                term,
                'academicYearId',
                'academic_year_id'
            )
        );
    }

    function overviewTermCode(
        term
    ) {
        return String(
            overviewFirstDefined(
                term,
                'termCode',
                'code',
                'termName',
                'name'
            )
            ?? ''
        ).trim();
    }

    function overviewLevelId(
        level
    ) {
        return overviewNumber(
            overviewFirstDefined(
                level,
                'levelId',
                'id'
            )
        );
    }

    function overviewClassId(
        classItem
    ) {
        return overviewNumber(
            overviewFirstDefined(
                classItem,
                'classId',
                'id'
            )
        );
    }

    function overviewClassLevelId(
        classItem
    ) {
        return overviewNumber(
            overviewFirstDefined(
                classItem,
                'levelId',
                'level_id'
            )
            ?? overviewFirstDefined(
                classItem?.level,
                'levelId',
                'id'
            )
        );
    }

    function overviewOptionLabel(
        item,
        ...names
    ) {
        const value =
            overviewFirstDefined(
                item,
                ...names
            );

        return String(
            value
            ?? ''
        ).trim();
    }

    function replaceOverviewSelect(
        select,
        placeholder
    ) {
        if (!(select instanceof HTMLSelectElement)) {
            return;
        }

        select.replaceChildren(
            new Option(
                placeholder,
                ''
            )
        );
    }

    function populateOverviewAcademicYears(
        selectedCode = ''
    ) {
        const select =
            $('scholarship-overview-academic-year');

        if (!(select instanceof HTMLSelectElement)) {
            return '';
        }

        replaceOverviewSelect(
            select,
            'Select Academic Year'
        );

        const years =
            [...overviewReferenceState.academicYears]
                .filter(
                    year =>
                        overviewAcademicYearCode(
                            year
                        )
                );

        years.forEach(
            year => {
                const code =
                    overviewAcademicYearCode(
                        year
                    );

                const option =
                    new Option(
                        overviewOptionLabel(
                            year,
                            'academicYearName',
                            'academicYearCode',
                            'code'
                        ),
                        code
                    );

                option.dataset.academicYearId =
                    String(
                        overviewAcademicYearId(
                            year
                        )
                        ?? ''
                    );

                option.dataset.currentYear =
                    String(
                        overviewFirstDefined(
                            year,
                            'currentYear'
                        ) === true
                    );

                select.appendChild(
                    option
                );
            }
        );

        const requestedCode =
            String(
                selectedCode
                || select.dataset.selectedValue
                || ''
            ).trim();

        const currentOption =
            Array.from(
                select.options
            ).find(
                option =>
                    option.dataset.currentYear
                    === 'true'
            );

        if (
            requestedCode
            && Array.from(
                select.options
            ).some(
                option =>
                    option.value
                    === requestedCode
            )
        ) {
            select.value =
                requestedCode;
        } else if (currentOption) {
            select.value =
                currentOption.value;
        } else if (
            select.options.length > 1
        ) {
            select.selectedIndex = 1;
        }

        return select.value;
    }

    function populateOverviewTerms(
        selectedTerm = ''
    ) {
        const select =
            $('scholarship-overview-term');

        const yearSelect =
            $('scholarship-overview-academic-year');

        if (
            !(select instanceof HTMLSelectElement)
            || !(yearSelect instanceof HTMLSelectElement)
        ) {
            return '';
        }

        const selectedYearOption =
            yearSelect.options[
                yearSelect.selectedIndex
            ];

        const selectedYearId =
            overviewNumber(
                selectedYearOption
                    ?.dataset
                    ?.academicYearId
            );

        replaceOverviewSelect(
            select,
            'All Terms'
        );

        const terms =
            overviewReferenceState.academicTerms
                .filter(
                    term => {
                        if (
                            selectedYearId === null
                        ) {
                            return true;
                        }

                        const termYearId =
                            overviewTermAcademicYearId(
                                term
                            );

                        return (
                            termYearId === null
                            || termYearId
                            === selectedYearId
                        );
                    }
                );

        terms.forEach(
            term => {
                const code =
                    overviewTermCode(
                        term
                    );

                if (!code) {
                    return;
                }

                const option =
                    new Option(
                        overviewOptionLabel(
                            term,
                            'termName',
                            'termCode',
                            'code'
                        ),
                        code
                    );

                option.dataset.currentTerm =
                    String(
                        overviewFirstDefined(
                            term,
                            'currentTerm'
                        ) === true
                    );

                select.appendChild(
                    option
                );
            }
        );

        const requestedTerm =
            String(
                selectedTerm
                || select.dataset.selectedValue
                || ''
            ).trim();

        if (
            requestedTerm
            && Array.from(
                select.options
            ).some(
                option =>
                    option.value
                    === requestedTerm
            )
        ) {
            select.value =
                requestedTerm;
        } else {
            const currentOption =
                Array.from(
                    select.options
                ).find(
                    option =>
                        option.dataset.currentTerm
                        === 'true'
                );

            if (currentOption) {
                select.value =
                    currentOption.value;
            }
        }

        return select.value;
    }

    function populateOverviewLevels(
        selectedLevelId = ''
    ) {
        const select =
            $('scholarship-overview-level');

        if (!(select instanceof HTMLSelectElement)) {
            return '';
        }

        replaceOverviewSelect(
            select,
            'All Levels'
        );

        const levels =
            [...overviewReferenceState.levels]
                .filter(
                    level =>
                        overviewLevelId(
                            level
                        ) !== null
                )
                .sort(
                    (left, right) => {
                        const leftOrder =
                            Number(
                                overviewFirstDefined(
                                    left,
                                    'displayOrder',
                                    'sortOrder'
                                )
                                ?? 0
                            );

                        const rightOrder =
                            Number(
                                overviewFirstDefined(
                                    right,
                                    'displayOrder',
                                    'sortOrder'
                                )
                                ?? 0
                            );

                        if (
                            leftOrder
                            !== rightOrder
                        ) {
                            return (
                                leftOrder
                                - rightOrder
                            );
                        }

                        return overviewOptionLabel(
                            left,
                            'levelName',
                            'name',
                            'levelCode'
                        ).localeCompare(
                            overviewOptionLabel(
                                right,
                                'levelName',
                                'name',
                                'levelCode'
                            )
                        );
                    }
                );

        levels.forEach(
            level => {
                const id =
                    overviewLevelId(
                        level
                    );

                if (id === null) {
                    return;
                }

                select.appendChild(
                    new Option(
                        overviewOptionLabel(
                            level,
                            'levelName',
                            'name',
                            'levelCode',
                            'code'
                        ),
                        String(id)
                    )
                );
            }
        );

        const requested =
            String(
                selectedLevelId
                || ''
            ).trim();

        if (
            requested
            && Array.from(
                select.options
            ).some(
                option =>
                    option.value
                    === requested
            )
        ) {
            select.value =
                requested;
        }

        return select.value;
    }

    function populateOverviewClasses(
        selectedClassId = ''
    ) {
        const select =
            $('scholarship-overview-class');

        const levelSelect =
            $('scholarship-overview-level');

        if (
            !(select instanceof HTMLSelectElement)
            || !(levelSelect instanceof HTMLSelectElement)
        ) {
            return '';
        }

        const selectedLevelId =
            overviewNumber(
                levelSelect.value
            );

        replaceOverviewSelect(
            select,
            selectedLevelId === null
                ? 'All Classes'
                : 'All Classes'
        );

        const classes =
            [...overviewReferenceState.classes]
                .filter(
                    classItem => {
                        const classId =
                            overviewClassId(
                                classItem
                            );

                        if (classId === null) {
                            return false;
                        }

                        if (
                            selectedLevelId
                            === null
                        ) {
                            return true;
                        }

                        return (
                            overviewClassLevelId(
                                classItem
                            )
                            === selectedLevelId
                        );
                    }
                )
                .sort(
                    (left, right) => {
                        const leftOrder =
                            Number(
                                overviewFirstDefined(
                                    left,
                                    'displayOrder',
                                    'sortOrder'
                                )
                                ?? 0
                            );

                        const rightOrder =
                            Number(
                                overviewFirstDefined(
                                    right,
                                    'displayOrder',
                                    'sortOrder'
                                )
                                ?? 0
                            );

                        if (
                            leftOrder
                            !== rightOrder
                        ) {
                            return (
                                leftOrder
                                - rightOrder
                            );
                        }

                        return overviewOptionLabel(
                            left,
                            'className',
                            'name',
                            'classCode',
                            'code'
                        ).localeCompare(
                            overviewOptionLabel(
                                right,
                                'className',
                                'name',
                                'classCode',
                                'code'
                            )
                        );
                    }
                );

        classes.forEach(
            classItem => {
                const id =
                    overviewClassId(
                        classItem
                    );

                if (id === null) {
                    return;
                }

                const code =
                    overviewOptionLabel(
                        classItem,
                        'classCode',
                        'code'
                    );

                const name =
                    overviewOptionLabel(
                        classItem,
                        'className',
                        'name',
                        'classCode',
                        'code',
                        String(id)
                    );

                const label =
                    code
                        ? `[${code}] ${name}`
                        : name;

                const option =
                    new Option(
                        label,
                        String(id)
                    );

                option.dataset.levelId =
                    String(
                        overviewClassLevelId(
                            classItem
                        )
                        ?? ''
                    );

                select.appendChild(
                    option
                );
            }
        );

        const requested =
            String(
                selectedClassId
                || ''
            ).trim();

        if (
            requested
            && Array.from(
                select.options
            ).some(
                option =>
                    option.value
                    === requested
            )
        ) {
            select.value =
                requested;
        }

        return select.value;
    }

    async function loadOverviewReferenceData() {
        if (
            overviewReferenceState.loaded
        ) {
            return;
        }

        const response =
            await apiGet(
                OVERVIEW_REFERENCE_API
            );

        const data =
            unwrap(response);

        if (
            !data
            || typeof data !== 'object'
        ) {
            throw new Error(
                'Scholarship Overview reference data was not returned.'
            );
        }

        normalizeOverviewReferenceData(
            data
        );

        const yearCode =
            populateOverviewAcademicYears();

        populateOverviewTerms();

        populateOverviewLevels();

        populateOverviewClasses();

        return yearCode;
    }

    function setOverviewValue(
        id,
        value
    ) {
        const node = $(id);

        if (node) {
            node.textContent =
                text(value);
        }
    }

    function overviewMoney(
        value
    ) {
        if (
            value === null
            || value === undefined
            || value === ''
        ) {
            return 'UGX 0.00';
        }

        const number =
            Number(value);

        if (!Number.isFinite(number)) {
            return money(value);
        }

        return `UGX ${number.toLocaleString(
            'en-UG',
            {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            }
        )}`;
    }

    /**
     * Backend Scholarship Overview section.
     *
     * @typedef {Object} ScholarshipOverviewSection
     * @property {number} [studentsApplied]
     * @property {number} [studentsPending]
     * @property {number} [studentsUnderVerification]
     * @property {number} [studentsShortlisted]
     * @property {number} [studentsApproved]
     * @property {number} [studentsRejected]
     * @property {number} [studentsBenefited]
     * @property {number|string} [amountRequestedByStudentsUgx]
     * @property {number|string} [totalRequestedUgx]
     * @property {number|string} [amountApprovedUgx]
     * @property {number|string} [amountRejectedUgx]
     * @property {number|string} [totalFundsReceivedUgx]
     * @property {number|string} [totalFundsAllocatedUgx]
     * @property {number|string} [totalFundsSpentUgx]
     * @property {number|string} [totalFundsRemainingUgx]
     */

    /**
     * Render one authoritative Overview section returned by the backend.
     *
     * @param {string} prefix
     * @param {ScholarshipOverviewSection|null|undefined} section
     */
    function renderOverviewSection(
        prefix,
        section
    ) {
        /** @type {ScholarshipOverviewSection} */
        const data =
            section || {};

        /*
         * New Overview IDs.
         */
        setOverviewValue(
            `${prefix}-students-applied`,
            data.studentsApplied ?? 0
        );

        setOverviewValue(
            `${prefix}-students-pending`,
            data.studentsPending ?? 0
        );

        setOverviewValue(
            `${prefix}-students-under-verification`,
            data.studentsUnderVerification ?? 0
        );

        setOverviewValue(
            `${prefix}-students-shortlisted`,
            data.studentsShortlisted ?? 0
        );

        setOverviewValue(
            `${prefix}-students-approved`,
            data.studentsApproved ?? 0
        );

        setOverviewValue(
            `${prefix}-students-rejected`,
            data.studentsRejected ?? 0
        );

        setOverviewValue(
            `${prefix}-students-benefited`,
            data.studentsBenefited ?? 0
        );

        setOverviewValue(
            `${prefix}-amount-requested`,
            overviewMoney(
                data.amountRequestedByStudentsUgx
                ?? data.totalRequestedUgx
            )
        );

        setOverviewValue(
            `${prefix}-amount-approved`,
            overviewMoney(
                data.amountApprovedUgx
            )
        );

        setOverviewValue(
            `${prefix}-amount-rejected`,
            overviewMoney(
                data.amountRejectedUgx
            )
        );

        setOverviewValue(
            `${prefix}-funds-received`,
            overviewMoney(
                data.totalFundsReceivedUgx
            )
        );

        setOverviewValue(
            `${prefix}-funds-allocated`,
            overviewMoney(
                data.totalFundsAllocatedUgx
            )
        );

        setOverviewValue(
            `${prefix}-funds-spent`,
            overviewMoney(
                data.totalFundsSpentUgx
            )
        );

        setOverviewValue(
            `${prefix}-funds-remaining`,
            overviewMoney(
                data.totalFundsRemainingUgx
            )
        );
    }

    function renderLegacyOverviewIds(
        selectedPeriod
    ) {
        /*
         * Keep the existing Overview HTML functional until the new
         * three-section HTML is installed. These IDs are intentionally
         * mapped from the backend Selected Period object.
         */
        const data =
            selectedPeriod || {};

        setOverviewValue(
            'scholarship-overview-total',
            data.studentsApplied ?? 0
        );

        setOverviewValue(
            'scholarship-overview-pending',
            data.studentsPending ?? 0
        );

        setOverviewValue(
            'scholarship-overview-in-verification',
            data.studentsUnderVerification ?? 0
        );

        /*
         * The old "Verified" card is kept compatible. The new backend
         * exposes Under Verification and Shortlisted explicitly; verified
         * is represented by the workflow states and is no longer calculated
         * locally from application rows.
         */
        setOverviewValue(
            'scholarship-overview-verified',
            Math.max(
                Number(data.studentsApplied || 0)
                - Number(data.studentsPending || 0)
                - Number(data.studentsUnderVerification || 0),
                0
            )
        );

        setOverviewValue(
            'scholarship-overview-shortlisted',
            data.studentsShortlisted ?? 0
        );

        setOverviewValue(
            'scholarship-overview-approved',
            data.studentsApproved ?? 0
        );

        /*
         * The old fund cards are mapped to the authoritative selected-period
         * fund figures. No frontend aggregation is performed.
         */
        setOverviewValue(
            'scholarship-overview-total-fund',
            overviewMoney(
                data.totalFundsReceivedUgx
            )
        );

        setOverviewValue(
            'scholarship-overview-committed',
            overviewMoney(
                data.totalRequestedUgx
            )
        );

        setOverviewValue(
            'scholarship-overview-allocated',
            overviewMoney(
                data.amountApprovedUgx
            )
        );

        setOverviewValue(
            'scholarship-overview-remaining',
            overviewMoney(
                data.totalFundsRemainingUgx
            )
        );
    }

    /**
     * @typedef {Object} ScholarshipOverviewResponse
     * @property {string|number} [academicYear]
     * @property {string} [term]
     * @property {string|number} [currentAcademicYear]
     * @property {ScholarshipOverviewSection} [selectedPeriod]
     * @property {ScholarshipOverviewSection} [academicYearOverall]
     * @property {ScholarshipOverviewSection} [branchOverall]
     */

    /**
     * Render the complete Overview response without recalculating
     * statistics in the browser.
     *
     * @param {ScholarshipOverviewResponse|null|undefined} responseData
     */
    function renderOverview(
        responseData
    ) {
        /** @type {ScholarshipOverviewResponse} */
        const data =
            responseData || {};

        const selectedPeriod =
            data.selectedPeriod || {};

        const academicYearOverall =
            data.academicYearOverall || {};

        const branchOverall =
            data.branchOverall || {};

        /*
         * Three authoritative sections returned by the backend.
         */
        renderOverviewSection(
            'scholarship-overview-selected',
            selectedPeriod
        );

        renderOverviewSection(
            'scholarship-overview-academic-year',
            academicYearOverall
        );

        renderOverviewSection(
            'scholarship-overview-branch',
            branchOverall
        );

        /*
         * Context labels used by the new HTML.
         */
        const selectedLevel =
            $('scholarship-overview-level');

        const selectedClass =
            $('scholarship-overview-class');

        const selectedLevelLabel =
            selectedLevel?.value
                ? selectedLevel.options[
                    selectedLevel.selectedIndex
                ]?.textContent?.trim()
                : '';

        const selectedClassLabel =
            selectedClass?.value
                ? selectedClass.options[
                    selectedClass.selectedIndex
                ]?.textContent?.trim()
                : '';

        setOverviewValue(
            'scholarship-overview-selected-period-label',
            [
                data.academicYear,
                data.term,
                selectedLevelLabel,
                selectedClassLabel
            ]
                .filter(Boolean)
                .join(' • ')
        );

        setOverviewValue(
            'scholarship-overview-academic-year-label',
            data.academicYear
                || '—'
        );

        setOverviewValue(
            'scholarship-overview-current-academic-year',
            data.currentAcademicYear
                || '—'
        );

        /*
         * Preserve the existing Overview DOM while the HTML is being
         * upgraded to the three-section design.
         */
        renderLegacyOverviewIds(
            selectedPeriod
        );
    }

    async function loadOverview() {
        if (!overviewRoot()) {
            return;
        }

        let loaderToken = null;

        try {
            if (typeof showLoader === 'function') {
                loaderToken =
                    showLoader(
                        'Loading Scholarship overview...'
                    );
            }

            const response =
                await apiGet(
                    buildOverviewUrl()
                );

            /** @type {ScholarshipOverviewResponse|Object|null} */
            const data =
                unwrap(response);

            if (!data || typeof data !== 'object') {
                throw new Error(
                    'Scholarship Overview data was not returned.'
                );
            }

            state.overview =
                data;

            renderOverview(
                data
            );

        } catch (error) {
            console.error(
                'Scholarship overview could not be loaded.',
                error
            );

            showError(
                messageFromError(
                    error,
                    'Unable to load Scholarship overview.'
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
        }
    }

    function bindOverviewEvents() {
        const refresh =
            $('scholarship-overview-refresh-btn');

        if (
            refresh
            && !refresh.dataset.bound
        ) {
            refresh.dataset.bound =
                'true';

            refresh.addEventListener(
                'click',
                () => void loadOverview()
            );
        }

        const applyFilters =
            $('scholarship-overview-apply-filters-btn');

        if (
            applyFilters
            && !applyFilters.dataset.overviewBound
        ) {
            applyFilters.dataset.overviewBound =
                'true';

            applyFilters.addEventListener(
                'click',
                () => void loadOverview()
            );
        }

        const academicYear =
            $('scholarship-overview-academic-year');

        if (
            academicYear
            && !academicYear.dataset.overviewBound
        ) {
            academicYear.dataset.overviewBound =
                'true';

            academicYear.addEventListener(
                'change',
                () => {
                    /*
                     * Changing the Academic Year only updates the dependent
                     * Term options. Do not query the Overview until the user
                     * explicitly applies the complete filter set.
                     */
                    populateOverviewTerms();
                }
            );
        }

        const term =
            $('scholarship-overview-term');

        if (
            term
            && !term.dataset.overviewBound
        ) {
            term.dataset.overviewBound =
                'true';

            term.addEventListener(
                'change',
                () => {
                    /* Filters are applied together via Apply Filters. */
                }
            );
        }

        const level =
            $('scholarship-overview-level');

        if (
            level
            && !level.dataset.overviewBound
        ) {
            level.dataset.overviewBound =
                'true';

            level.addEventListener(
                'change',
                () => {
                    /*
                     * Level changes only rebuild the dependent Class list.
                     * The Overview request waits for Apply Filters.
                     */
                    populateOverviewClasses();
                }
            );
        }

        const classSelect =
            $('scholarship-overview-class');

        if (
            classSelect
            && !classSelect.dataset.overviewBound
        ) {
            classSelect.dataset.overviewBound =
                'true';

            classSelect.addEventListener(
                'change',
                () => {
                    /* Filters are applied together via Apply Filters. */
                }
            );
        }
    }

    async function initOverview() {
        if (!overviewRoot()) {
            return;
        }

        let loaderToken = null;

        try {
            if (
                typeof showLoader
                === 'function'
            ) {
                loaderToken =
                    showLoader(
                        'Loading Scholarship reference data...'
                    );
            }

            await loadOverviewReferenceData();

        } catch (error) {
            console.error(
                'Scholarship Overview reference data could not be loaded.',
                error
            );

            showError(
                messageFromError(
                    error,
                    'Unable to load Scholarship Overview reference data.'
                )
            );

            return;
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

        bindOverviewEvents();
        await loadOverview();
    }

    async function init() {
        const viewRoot =
            root();

        if (viewRoot) {
            if (!state.initialized) {
                state.initialized = true;
                bindEvents();
            }

            await loadScholarships();
            return;
        }

        await initOverview();
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
                return;
            }

            if (
                viewName === 'scholarship-overview'
                || viewName === 'branch-scholarship-overview'
            ) {
                void initOverview();
            }
        }
    );

    document.addEventListener(
        'DOMContentLoaded',
        () => {
            const applicationsView = root();
            if (applicationsView) {
                void init();
                return;
            }

            if (overviewRoot()) {
                void initOverview();
            }
        }
    );

    window.ScholarshipModule = {
        init,
        reload: loadScholarships,
        reloadOverview: loadOverview,
        open: openScholarshipApplication
    };

})();

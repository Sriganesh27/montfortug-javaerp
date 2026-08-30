/* global apiGet, showLoader, hideLoader, showErrorMessage */

(function () {
    'use strict';

    /*
     * Scholarship Overview module.
     *
     * The previous Scholarship Applications page/module has been removed.
     * This file now contains only the helpers and implementation required by
     * the existing Scholarship Overview page.
     */

    const API_ROOT =
        '/branchadmin/scholarships';

    const $ = (id) =>
        document.getElementById(id);

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

    function money(value) {
        if (
            value === null
            || value === undefined
            || value === ''
        ) {
            return 'UGX 0.00';
        }

        const number = Number(value);

        if (!Number.isFinite(number)) {
            return String(value);
        }

        return `UGX ${number.toLocaleString(
            'en-UG',
            {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            }
        )}`;
    }

    function showError(message) {
        if (
            typeof showErrorMessage
            === 'function'
        ) {
            showErrorMessage(message);
        }
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
                hideLoader(loaderToken);
            }
        }

        bindOverviewEvents();
        await loadOverview();
    }

    function bootOverview() {
        if (overviewRoot()) {
            void initOverview();
        }
    }

    document.addEventListener(
        'viewLoaded',
        event => {
            const detail = event?.detail || {};
            const viewName = detail.view || detail.viewName;
            const role = detail.role;

            if (
                role
                && role !== 'admin'
                && role !== 'branchadmin'
            ) {
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
        bootOverview
    );

    window.ScholarshipModule = {
        init: initOverview,
        reloadOverview: loadOverview
    };

})();

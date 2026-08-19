/* global apiGet, apiPost, CrudTable, Toast, showLoader, hideLoader, erpWithButtonFeedback, showPremiumModal, showSuccessMessage, showErrorMessage, createErpCalendar */

const handleApplicationsLoad = event => {
    const detail =
        event && event.detail
            ? event.detail
            : {};

    if (
        detail.role !== 'admin'
        || detail.view !== 'applications'
    ) {
        return;
    }

    const initializationPromise =
        ApplicationsController
            .init(detail)
            .catch(error => {
                console.error(
                    'Applications view initialization failed.',
                    error
                );

                if (typeof showErrorMessage === 'function') {
                    showErrorMessage(
                        error?.message
                        || 'Applications could not be initialized.'
                    );
                }

                throw error;
            });

    /*
     * Use the dashboard's existing global navigation loader exactly like
     * Manage Students. The page is revealed only after the first Application
     * page has loaded, preventing empty or unresolved reference values from
     * appearing first.
     */
    const waitUntil =
        detail.waitUntil;

    if (typeof waitUntil === 'function') {
        waitUntil(initializationPromise);
        return;
    }

    void initializationPromise.catch(() => {
        /* Error was already reported above. */
    });
};

document.removeEventListener(
    'viewLoaded',
    handleApplicationsLoad
);

document.addEventListener(
    'viewLoaded',
    handleApplicationsLoad
);

const ApplicationsController = (() => {
    const API_ROOT =
        '/admission/branch/applications';

    const EMPLOYEE_OPTIONS_API =
        '/admission/branch/employee-options';

    const state = {
        page: 0,
        size: 20,
        totalPages: 0,
        totalElements: 0,
        currentApplicationId: null,
        currentApplication: null,
        currentRows: [],
        selectedApplications: new Map(),
        profileTransitions: [],
        schoolVisit: null,
        schoolVisitEmployees: [],
        schoolVisitScheduleMode: 'schedule',
        pendingSchoolVisitTransition: null,
        entranceTest: null,
        documentSyncTimer: null,
        documentSyncBusy: false,
        documentSyncSignature: null,
        documentSyncApplicationId: null,
        failedDocumentViewUrls: new Set(),
        applicationListSyncTimer: null,
        applicationListSyncBusy: false,
        applicationListSignature: null,
        initialApplicationListLoaded: false,
        sortField: 'submittedDate',
        sortDirection: 'DESC',
        initializedRoot: null
    };

    let view = null;
    let table = null;

    /**
     * Initializes the dynamically loaded Branch Admin Applications view.
     */
    async function init(routeInfo = {}) {
        const root =
            document.getElementById(
                'ba-applications-view'
            );

        if (!root) {
            return;
        }

        if (state.initializedRoot === root) {
            return;
        }

        state.initializedRoot = root;
        state.page = 0;
        state.currentApplicationId = null;
        state.currentApplication = null;
        state.currentRows = [];
        state.selectedApplications = new Map();
        state.profileTransitions = [];
        state.schoolVisit = null;
        state.schoolVisitEmployees = [];
        state.entranceTest = null;
        state.schoolVisitScheduleMode = 'schedule';
        state.pendingSchoolVisitTransition = null;
        stopDocumentAutoSync();
        stopApplicationListAutoSync();
        state.documentSyncSignature = null;
        state.applicationListSignature = null;
        state.applicationListSyncBusy = false;
        state.initialApplicationListLoaded = false;

        view = cacheDom(root);

        if (!view.tableBody
                || !view.table
                || !view.rowTemplate) {
            notifyError(
                'Application page could not be initialized.'
            );
            return;
        }

        table = new CrudTable(
            {
                tbody: view.tableBody,
                pageSize: view.pageSize,
                pageInfo: view.pageInfo,
                btnPrev: view.previousPageButton,
                btnNext: view.nextPageButton,
                table: view.table,
                tplLoading:
                    document.getElementById(
                        'global-table-fetching-template'
                    ),
                tplEmpty:
                    document.getElementById(
                        'global-table-empty-template'
                    ),
                tplRow: view.rowTemplate
            },
            {
                onPageChange: direction => {
                    const nextPage =
                        state.page + direction;

                    if (nextPage < 0
                            || nextPage
                            >= state.totalPages) {
                        return;
                    }

                    state.page = nextPage;
                    void loadApplications();
                },

                onSizeChange: size => {
                    state.size = size;
                    state.page = 0;
                    void loadApplications();
                },

                onSort: field => {
                    if (state.sortField === field) {
                        state.sortDirection =
                            state.sortDirection === 'ASC'
                                ? 'DESC'
                                : 'ASC';
                    } else {
                        state.sortField = field;
                        state.sortDirection = 'ASC';
                    }

                    sortCurrentRows();
                    renderApplicationRows();
                }
            }
        );

        bindEvents();
        initializeApplicationCalendars();

        document.removeEventListener(
            'visibilitychange',
            handleApplicationsVisibilityChange
        );
        document.addEventListener(
            'visibilitychange',
            handleApplicationsVisibilityChange
        );

        showTableView();

        /*
         * Await the first server page so event.detail.waitUntil keeps the
         * existing dashboard global loader active until the table is ready.
         */
        await loadApplications();
        state.initialApplicationListLoaded = true;
        startApplicationListAutoSync();
    }

    /**
     * Caches every element used by this module.
     *
     * @param {HTMLElement} root
     * @returns {Object}
     */
    function cacheDom(root) {
        const byId = id =>
            root.querySelector(`#${id}`);

        return {
            root,

            tableComponent:
                byId('ba-appTableComponent'),
            detailComponent:
                byId('ba-appDetailComponent'),
            detailContent:
                byId('ba-appDetailContent'),

            table:
                byId('ba-appTable'),
            tableBody:
                byId('ba-appTableBody'),
            rowTemplate:
                byId('tpl-app-row'),

            searchKeyword:
                byId('ba-appSearchKeyword'),
            searchGender:
                byId('ba-appSearchGender'),
            searchLevel:
                byId('ba-appSearchLevel'),
            searchClass:
                byId('ba-appSearchClass'),
            searchStage:
                byId('ba-appSearchStage'),
            searchDocumentStatus:
                byId('ba-appSearchDocumentStatus'),
            searchScholarship:
                byId('ba-appSearchScholarship'),
            searchStatus:
                byId('ba-appSearchStatus'),
            searchFromDate:
                byId('ba-appSearchFromDate'),
            searchToDate:
                byId('ba-appSearchToDate'),
            moreFiltersButton:
                byId('ba-appMoreFiltersBtn'),
            advancedFilters:
                byId('ba-appAdvancedFilters'),
            activeFilterCount:
                byId('ba-appActiveFilterCount'),
            searchButton:
                byId('ba-appSearchBtn'),
            resetButton:
                byId('ba-appResetBtn'),
            refreshButton:
                byId('ba-refreshAppsBtn'),

            selectPage:
                byId('ba-appSelectPage'),
            selectedCount:
                byId('ba-appSelectedCount'),
            bulkNextActionButton:
                byId('ba-appBulkNextActionBtn'),
            bulkClearButton:
                byId('ba-appBulkClearBtn'),

            pageSize:
                byId('ba-appPageSize'),
            pageInfo:
                byId('ba-appPageInfo'),
            previousPageButton:
                byId('ba-appPrevPageBtn'),
            nextPageButton:
                byId('ba-appNextPageBtn'),

            backButton:
                byId('ba-backToAppTableBtn'),
            refreshDetailButton:
                byId('ba-refreshAppDetailBtn'),
            printButton:
                byId('ba-printAppBtn'),
            requestDocumentInlineButton:
                byId(
                    'ba-requestAdditionalDocumentInlineBtn'
                ),
            profileNextStageButton:
                byId('ba-appNextStageBtn'),

            profilePhoto:
                byId('view-appProfilePhoto'),
            profilePhotoPlaceholder:
                byId(
                    'view-appProfilePhotoPlaceholder'
                ),

            documentsContainer:
                byId(
                    'application-documents-view-container'
                ),
            documentCount:
                byId('application-document-count'),

            documentRequestsContainer:
                byId(
                    'application-document-requests-container'
                ),
            documentRequestCount:
                byId(
                    'application-document-request-count'
                ),

            historyContainer:
                byId('application-history-container'),
            historyCount:
                byId('application-history-count'),

            schoolVisitSection:
                byId('application-school-visit-section'),
            schoolVisitStatusCaption:
                byId('school-visit-status-caption'),
            schoolVisitStageMessage:
                byId('school-visit-stage-message'),
            schoolVisitStatus:
                byId('view-schoolVisitStatus'),
            schoolVisitEmployee:
                byId('view-schoolVisitEmployee'),
            schoolVisitEmployeeNo:
                byId('view-schoolVisitEmployeeNo'),
            schoolVisitScheduledAt:
                byId('view-schoolVisitScheduledAt'),
            schoolVisitVisitedAt:
                byId('view-schoolVisitAt'),
            schoolVisitStudentAttendance:
                byId('view-schoolVisitStudentAttendance'),
            schoolVisitParentAttendance:
                byId('view-schoolVisitParentAttendance'),
            schoolVisitRemarks:
                byId('view-schoolVisitRemarks'),
            schoolVisitScheduleButton:
                byId('ba-schoolVisitScheduleBtn'),
            schoolVisitRescheduleButton:
                byId('ba-schoolVisitRescheduleBtn'),
            schoolVisitCompleteButton:
                byId('ba-schoolVisitCompleteBtn'),

            schoolVisitScheduleModal:
                byId('ba-schoolVisitScheduleModal'),
            schoolVisitScheduleForm:
                byId('ba-schoolVisitScheduleForm'),
            schoolVisitScheduleTitle:
                byId('ba-schoolVisitScheduleTitle'),
            schoolVisitScheduleSubtitle:
                byId('ba-schoolVisitScheduleSubtitle'),
            schoolVisitScheduledAtInput:
                byId('ba-schoolVisitScheduledAtInput'),
            schoolVisitScheduleRemarks:
                byId('ba-schoolVisitScheduleRemarks'),
            schoolVisitScheduleError:
                byId('ba-schoolVisitScheduleError'),
            schoolVisitCancelScheduleButton:
                byId('ba-cancelSchoolVisitScheduleBtn'),
            schoolVisitCloseScheduleButton:
                byId('ba-closeSchoolVisitScheduleBtn'),
            schoolVisitSaveScheduleButton:
                byId('ba-saveSchoolVisitScheduleBtn'),

            schoolVisitCompleteModal:
                byId('ba-schoolVisitCompleteModal'),
            schoolVisitCompleteForm:
                byId('ba-schoolVisitCompleteForm'),
            schoolVisitCompleteEmployeeSelect:
                byId('ba-schoolVisitCompleteEmployeeId'),
            schoolVisitVisitedAtInput:
                byId('ba-schoolVisitVisitedAtInput'),
            schoolVisitStudentAttended:
                byId('ba-schoolVisitStudentAttended'),
            schoolVisitParentAttended:
                byId('ba-schoolVisitParentAttended'),
            schoolVisitCompleteRemarks:
                byId('ba-schoolVisitCompleteRemarks'),
            schoolVisitCompleteError:
                byId('ba-schoolVisitCompleteError'),
            schoolVisitCancelCompleteButton:
                byId('ba-cancelSchoolVisitCompleteBtn'),
            schoolVisitCloseCompleteButton:
                byId('ba-closeSchoolVisitCompleteBtn'),
            schoolVisitConfirmCompleteButton:
                byId('ba-confirmSchoolVisitCompleteBtn'),

            entranceTestSection:
                byId('application-entrance-test-section'),
            entranceTestEnterMarksButton:
                byId('ba-entranceTestEnterMarksBtn'),
            entranceTestUpdateResultButton:
                byId('ba-entranceTestUpdateResultBtn'),
            entranceTestStatus:
                byId('view-entranceTestStatus'),
            entranceTestResult:
                byId('view-entranceTestResult'),
            entranceTestEmployee:
                byId('view-entranceTestEmployee'),
            entranceTestCompletedAt:
                byId('view-entranceTestCompletedAt'),
            entranceTestRemarks:
                byId('view-entranceTestRemarks'),
            entranceTestMarksBlock:
                byId('view-entranceTestMarksBlock'),
            entranceTestMarksBody:
                byId('view-entranceTestMarksBody'),
            entranceTestMarksTotal:
                byId('view-entranceTestMarksTotal'),
            entranceTestMarksPercentage:
                byId('view-entranceTestMarksPercentage'),

            entranceTestMarksModal:
                byId('ba-entranceTestMarksModal'),
            entranceTestMarksForm:
                byId('ba-entranceTestMarksForm'),
            entranceTestMarksRows:
                byId('ba-entranceTestMarksRows'),
            entranceTestAddSubjectButton:
                byId('ba-addEntranceTestSubjectBtn'),
            entranceTestResultSelect:
                byId('ba-entranceTestResult'),
            entranceTestCompletedAtInput:
                byId('ba-entranceTestCompletedAt'),
            entranceTestEmployeeRemarks:
                byId('ba-entranceTestEmployeeRemarks'),
            entranceTestInternalRemarks:
                byId('ba-entranceTestInternalRemarks'),
            entranceTestMarksError:
                byId('ba-entranceTestMarksError'),
            entranceTestLiveMaximum:
                byId('ba-entranceTestLiveMaximum'),
            entranceTestLiveObtained:
                byId('ba-entranceTestLiveObtained'),
            entranceTestLivePercentage:
                byId('ba-entranceTestLivePercentage'),
            entranceTestCloseMarksButton:
                byId('ba-closeEntranceTestMarksBtn'),
            entranceTestCancelMarksButton:
                byId('ba-cancelEntranceTestMarksBtn'),

            waitlistResultModal:
                byId('ba-waitlistResultModal'),
            waitlistResultForm:
                byId('ba-waitlistResultForm'),
            waitlistResultMarksBody:
                byId('ba-waitlistResultMarksBody'),
            waitlistFinalResultSelect:
                byId('ba-waitlistFinalResult'),
            waitlistResultRemarks:
                byId('ba-waitlistResultRemarks'),
            waitlistResultError:
                byId('ba-waitlistResultError'),
            waitlistResultCloseButton:
                byId('ba-closeWaitlistResultBtn'),
            waitlistResultCancelButton:
                byId('ba-cancelWaitlistResultBtn'),
            waitlistResultSaveButton:
                byId('ba-saveWaitlistResultBtn'),
            entranceTestSaveMarksButton:
                byId('ba-saveEntranceTestMarksBtn'),

            reviewModal:
                byId('ba-documentReviewModal'),
            reviewForm:
                byId('ba-documentReviewForm'),
            reviewDocumentId:
                byId('ba-reviewDocumentId'),
            reviewSubtitle:
                byId('ba-documentReviewSubtitle'),
            reviewDecision:
                byId('ba-reviewDecision'),
            rejectionReasonGroup:
                byId('ba-rejectionReasonGroup'),
            rejectionReason:
                byId('ba-rejectionReason'),
            reuploadReasonGroup:
                byId('ba-reuploadReasonGroup'),
            reuploadReason:
                byId('ba-reuploadReason'),
            reuploadDeadlineGroup:
                byId('ba-reuploadDeadlineGroup'),
            reuploadDeadline:
                byId('ba-reuploadDeadline'),
            reviewPublicRemarks:
                byId('ba-reviewPublicRemarks'),
            reviewInternalRemarks:
                byId('ba-reviewInternalRemarks'),
            reviewError:
                byId('ba-documentReviewError'),
            cancelReviewButton:
                byId('ba-cancelDocumentReviewBtn'),
            submitReviewButton:
                byId('ba-submitDocumentReviewBtn'),

            requestModal:
                byId('ba-additionalDocumentModal'),
            requestForm:
                byId('ba-additionalDocumentForm'),
            requestDocumentType:
                byId('ba-requestDocumentType'),
            requestDocumentName:
                byId('ba-requestDocumentName'),
            requestReason:
                byId('ba-requestReason'),
            requestPublicRemarks:
                byId('ba-requestPublicRemarks'),
            requestInternalRemarks:
                byId('ba-requestInternalRemarks'),
            requestUploadDeadline:
                byId('ba-requestUploadDeadline'),
            requestError:
                byId('ba-additionalDocumentError'),
            closeRequestModalButton:
                byId('ba-closeAdditionalDocumentModalBtn'),
            cancelRequestFormButton:
                byId('ba-cancelAdditionalDocumentBtn'),
            submitRequestButton:
                byId('ba-submitAdditionalDocumentBtn'),

            cancelRequestModal:
                byId('ba-cancelDocumentRequestModal'),
            cancelRequestForm:
                byId('ba-cancelDocumentRequestForm'),
            cancelRequestId:
                byId('ba-cancelDocumentRequestId'),
            cancelRequestSubtitle:
                byId(
                    'ba-cancelDocumentRequestSubtitle'
                ),
            cancellationReason:
                byId(
                    'ba-documentRequestCancellationReason'
                ),
            cancellationError:
                byId('ba-cancelDocumentRequestError'),
            abortCancellationButton:
                byId(
                    'ba-abortCancelDocumentRequestBtn'
                ),
            confirmCancellationButton:
                byId(
                    'ba-confirmCancelDocumentRequestBtn'
                )
        };
    }

    /**
     * Initializes the shared ERP Flatpickr calendar for Application
     * document deadlines. The visible value is user-friendly while the
     * original input keeps an ISO LocalDateTime value for Spring/Jackson.
     */
    function initializeApplicationCalendars() {
        if (typeof createErpCalendar !== 'function') {
            return;
        }

        const currentYear =
            new Date().getFullYear();

        const commonDeadlineConfig = {
            enableTime: true,
            time_24hr: false,
            minuteIncrement: 5,
            minDate: 'today',
            dateFormat: 'Y-m-d\\TH:i',
            defaultHour: 17,
            defaultMinute: 0,
            footerActions: [
                'today',
                'clear',
                'close'
            ],
            minYear: currentYear,
            maxYear: currentYear + 3
        };

        createErpCalendar(
            '#ba-requestUploadDeadline',
            {
                ...commonDeadlineConfig
            }
        );

        createErpCalendar(
            '#ba-reuploadDeadline',
            {
                ...commonDeadlineConfig
            }
        );

        createErpCalendar(
            '#ba-schoolVisitScheduledAtInput',
            {
                ...commonDeadlineConfig,
                defaultHour: 9,
                defaultMinute: 0
            }
        );

        createErpCalendar(
            '#ba-schoolVisitVisitedAtInput',
            {
                enableTime: true,
                time_24hr: false,
                minuteIncrement: 5,
                maxDate: 'today',
                dateFormat: 'Y-m-d\\TH:i',
                footerActions: [
                    'today',
                    'clear',
                    'close'
                ],
                minYear: currentYear - 1,
                maxYear: currentYear
            }
        );

        createErpCalendar(
            '#ba-entranceTestCompletedAt',
            {
                enableTime: true,
                time_24hr: false,
                minuteIncrement: 5,
                maxDate: 'today',
                dateFormat: 'Y-m-d\\TH:i',
                footerActions: [
                    'today',
                    'clear',
                    'close'
                ],
                minYear: currentYear - 1,
                maxYear: currentYear
            }
        );

        bindApplicationCalendarButtons();
    }

    /**
     * Opens the existing createErpCalendar() instance from the calendar icon.
     * This is Application-view-only and does not change the global calendar.
     */
    function bindApplicationCalendarButtons() {
        view.root
            .querySelectorAll(
                '.app-calendar-button[data-calendar-target]'
            )
            .forEach(button => {
                if (!(button instanceof HTMLButtonElement)) {
                    return;
                }

                if (button.dataset.calendarBound === 'true') {
                    return;
                }

                button.dataset.calendarBound = 'true';

                button.addEventListener(
                    'click',
                    event => {
                        event.preventDefault();
                        event.stopPropagation();

                        const targetId =
                            button.dataset.calendarTarget;

                        if (!targetId) {
                            return;
                        }

                        const input =
                            view.root.querySelector(
                                `#${targetId}`
                            );

                        const calendar =
                            input?._flatpickr;

                        if (
                            calendar
                            && typeof calendar.open
                            === 'function'
                        ) {
                            calendar.open();

                            if (
                                calendar.input
                                && typeof calendar.input.focus
                                === 'function'
                            ) {
                                calendar.input.focus({
                                    preventScroll: true
                                });
                            }

                            return;
                        }

                        input?.focus();
                    }
                );
            });
    }

    /**
     * Clears both the original Flatpickr input and its alternate display.
     *
     * @param {HTMLInputElement|null|undefined} input
     */
    function clearCalendarInput(input) {
        if (!input) {
            return;
        }

        const calendar =
            input._flatpickr;

        if (calendar
                && typeof calendar.clear === 'function') {
            calendar.clear();
            return;
        }

        input.value = '';
    }

    /**
     * Sets both the underlying LocalDateTime value and the global calendar's
     * visible alternate input when Flatpickr is active.
     *
     * @param {HTMLInputElement|null|undefined} input
     * @param {string|Date|null|undefined} value
     */
    function setCalendarDateTimeValue(
        input,
        value
    ) {
        if (!input) {
            return;
        }

        const normalized =
            toDateTimeLocalValue(value);

        const calendar =
            input._flatpickr;

        if (calendar
                && typeof calendar.setDate === 'function') {
            if (normalized) {
                calendar.setDate(
                    normalized,
                    true,
                    'Y-m-d\\TH:i'
                );
            } else if (typeof calendar.clear === 'function') {
                calendar.clear();
            }

            return;
        }

        input.value =
            normalized;
    }

    /**
     * Connects all table, detail and modal actions.
     */
    function bindEvents() {
        view.searchButton?.addEventListener(
            'click',
            handleSearch
        );

        view.resetButton?.addEventListener(
            'click',
            resetSearch
        );

        view.searchKeyword?.addEventListener(
            'keydown',
            event => {
                if (event.key === 'Enter') {
                    event.preventDefault();
                    handleSearch();
                }
            }
        );

        [
            view.searchGender,
            view.searchLevel,
            view.searchClass,
            view.searchStage,
            view.searchDocumentStatus,
            view.searchScholarship,
            view.searchStatus,
            view.searchFromDate,
            view.searchToDate
        ].forEach(control => {
            control?.addEventListener(
                'change',
                handleSearch
            );
        });

        view.moreFiltersButton?.addEventListener(
            'click',
            toggleAdvancedFilters
        );

        view.selectPage?.addEventListener(
            'change',
            handleSelectPage
        );

        view.bulkClearButton?.addEventListener(
            'click',
            clearBulkSelection
        );

        view.bulkNextActionButton?.addEventListener(
            'click',
            openBulkNextActionConfirmation
        );

        view.profileNextStageButton?.addEventListener(
            'click',
            openProfileNextActionConfirmation
        );

        view.refreshButton?.addEventListener(
            'click',
            () => {
                void refreshApplicationsWithLoader();
            }
        );

        view.backButton?.addEventListener(
            'click',
            showTableView
        );

        view.refreshDetailButton?.addEventListener(
            'click',
            () => {
                if (state.currentApplicationId) {
                    void openApplication(
                        state.currentApplicationId
                    );
                }
            }
        );

        view.printButton?.addEventListener(
            'click',
            () => window.print()
        );

        view.requestDocumentInlineButton
            ?.addEventListener(
                'click',
                openAdditionalDocumentModal
            );

        view.reviewDecision?.addEventListener(
            'change',
            updateReviewDecisionFields
        );

        view.cancelReviewButton?.addEventListener(
            'click',
            closeDocumentReviewModal
        );

        view.reviewForm?.addEventListener(
            'submit',
            event => {
                event.preventDefault();
                void submitDocumentReview();
            }
        );

        view.closeRequestModalButton?.addEventListener(
            'click',
            closeAdditionalDocumentModal
        );

        view.cancelRequestFormButton?.addEventListener(
            'click',
            closeAdditionalDocumentModal
        );

        view.requestForm?.addEventListener(
            'submit',
            event => {
                event.preventDefault();
                void submitAdditionalDocumentRequest();
            }
        );

        view.requestDocumentType?.addEventListener(
            'change',
            fillRequestedDocumentName
        );

        view.abortCancellationButton?.addEventListener(
            'click',
            closeCancelRequestModal
        );

        view.cancelRequestForm?.addEventListener(
            'submit',
            event => {
                event.preventDefault();
                void submitRequestCancellation();
            }
        );

        view.schoolVisitScheduleButton?.addEventListener(
            'click',
            () => {
                void openSchoolVisitScheduleModal(false);
            }
        );

        view.schoolVisitRescheduleButton?.addEventListener(
            'click',
            () => {
                void openSchoolVisitScheduleModal(true);
            }
        );

        view.schoolVisitCompleteButton?.addEventListener(
            'click',
            () => {
                void openSchoolVisitCompleteModal();
            }
        );

        view.schoolVisitCancelScheduleButton?.addEventListener(
            'click',
            closeSchoolVisitScheduleModal
        );

        view.schoolVisitCloseScheduleButton?.addEventListener(
            'click',
            closeSchoolVisitScheduleModal
        );

        view.schoolVisitScheduleForm?.addEventListener(
            'submit',
            event => {
                event.preventDefault();
                void submitSchoolVisitSchedule();
            }
        );

        view.schoolVisitCancelCompleteButton?.addEventListener(
            'click',
            closeSchoolVisitCompleteModal
        );

        view.schoolVisitCloseCompleteButton?.addEventListener(
            'click',
            closeSchoolVisitCompleteModal
        );

        view.schoolVisitCompleteForm?.addEventListener(
            'submit',
            event => {
                event.preventDefault();
                void submitSchoolVisitCompletion();
            }
        );

        bindBackdropClose(
            view.schoolVisitScheduleModal,
            closeSchoolVisitScheduleModal
        );

        bindBackdropClose(
            view.schoolVisitCompleteModal,
            closeSchoolVisitCompleteModal
        );

        view.entranceTestEnterMarksButton?.addEventListener(
            'click',
            openEntranceTestMarksModal
        );

        view.entranceTestAddSubjectButton?.addEventListener(
            'click',
            () => addEntranceTestMarkRow()
        );

        view.entranceTestCloseMarksButton?.addEventListener(
            'click',
            closeEntranceTestMarksModal
        );

        view.entranceTestCancelMarksButton?.addEventListener(
            'click',
            closeEntranceTestMarksModal
        );

        view.entranceTestMarksForm?.addEventListener(
            'submit',
            event => {
                event.preventDefault();
                void submitEntranceTestMarks();
            }
        );

        view.entranceTestUpdateResultButton?.addEventListener(
            'click',
            openWaitlistResultModal
        );

        view.waitlistResultCloseButton?.addEventListener(
            'click',
            closeWaitlistResultModal
        );

        view.waitlistResultCancelButton?.addEventListener(
            'click',
            closeWaitlistResultModal
        );

        view.waitlistResultForm?.addEventListener(
            'submit',
            event => {
                event.preventDefault();
                void submitWaitlistResult();
            }
        );

        bindBackdropClose(
            view.waitlistResultModal,
            closeWaitlistResultModal
        );

        bindBackdropClose(
            view.entranceTestMarksModal,
            closeEntranceTestMarksModal
        );

        bindBackdropClose(
            view.reviewModal,
            closeDocumentReviewModal
        );

        bindBackdropClose(
            view.requestModal,
            closeAdditionalDocumentModal
        );

        bindBackdropClose(
            view.cancelRequestModal,
            closeCancelRequestModal
        );
    }

    async function refreshApplicationsWithLoader() {
        let loaderToken = null;

        try {
            if (typeof showLoader === 'function') {
                loaderToken =
                    showLoader(
                        'Refreshing Applications...'
                    );
            }

            await loadApplications();
        } finally {
            if (loaderToken
                    && typeof hideLoader === 'function') {
                hideLoader(loaderToken);
            }

        }
    }

    function buildApplicationListSignature(page) {
        const rows =
            Array.isArray(page?.content)
                ? page.content
                : [];

        return JSON.stringify({
            totalElements:
                Number(page?.totalElements || 0),
            totalPages:
                Number(page?.totalPages || 0),
            rows: rows.map(record => [
                record?.applicationId,
                record?.applicationNo,
                record?.applicationStatus,
                record?.currentStage,
                record?.documentStatus,
                record?.schoolVisitStatus,
                record?.schoolVisitScheduledAt,
                record?.scholarshipStatus,
                record?.nextActionAvailable,
                record?.nextAction,
                record?.nextTargetStage,
                record?.submittedDate
            ])
        });
    }

    /**
     * Loads one server page of branch-scoped applications.
     *
     * @param {boolean} silent no table loading state
     * @param {boolean} onlyIfChanged skip DOM work when server page is unchanged
     * @returns {Promise<boolean>} true when the table was rendered
     */
    async function loadApplications(
        silent = false,
        onlyIfChanged = false
    ) {
        if (!table) {
            return;
        }

        if (!silent) {
            table.showLoading();
        }

        try {
            const response =
                await apiGet(
                    `${API_ROOT}`
                    + `?page=${encodeURIComponent(state.page)}`
                    + `&size=${encodeURIComponent(state.size)}`
                );

            const page =
                response && response.data
                    ? response.data
                    : {};

            const nextSignature =
                buildApplicationListSignature(
                    page
                );

            if (
                onlyIfChanged
                && state.applicationListSignature
                    === nextSignature
            ) {
                return false;
            }

            state.applicationListSignature =
                nextSignature;

            state.currentRows =
                Array.isArray(page.content)
                    ? page.content.slice()
                    : [];

            populateDynamicFilterOptions();

            state.totalPages =
                Number.isInteger(page.totalPages)
                    ? page.totalPages
                    : 0;

            state.totalElements =
                Number.isFinite(
                    Number(page.totalElements)
                )
                    ? Number(page.totalElements)
                    : state.currentRows.length;

            sortCurrentRows();
            renderApplicationRows();

            table.renderPagination(
                state.page,
                state.totalPages,
                state.totalElements
            );

            return true;
        } catch (error) {
            table.render([], renderApplicationRow);
            notifyError(
                readErrorMessage(
                    error,
                    'Applications could not be loaded.'
                )
            );
        }
    }

    /**
     * Applies the current visible search to the loaded server page.
     */
    function renderApplicationRows() {
        const rows =
            getFilteredRows();

        table.render(
            rows,
            renderApplicationRow
        );

        updateBulkSelectionUI(rows);
        updateActiveFilterCount();
    }

    /**
     * Builds one Application table row.
     *
     * @param {Object} record
     * @param {DocumentFragment} node
     * @returns {DocumentFragment}
     */
    function renderApplicationRow(
        record,
        node
    ) {
        setNodeText(
            node.querySelector(
                '.td-appno strong'
            ),
            displayValue(record.applicationNo)
        );

        setNodeText(
            node.querySelector(
                '.app-profile-student-name'
            ),
            displayValue(record.studentName)
        );

        setNodeText(
            node.querySelector('.app-gender-label'),
            formatEnum(record.gender)
        );

        setNodeText(
            node.querySelector('.app-class-value'),
            displayValue(record.className)
        );

        setNodeText(
            node.querySelector('.app-level-label'),
            displayValue(record.levelName)
        );

        setNodeText(
            node.querySelector('.app-stage-value'),
            formatEnum(record.currentStage)
        );

        const documentLabel =
            formatEnum(record.documentStatus);

        setNodeText(
            node.querySelector('.app-document-label'),
            documentLabel === '-'
                ? 'Documents: -'
                : `Documents: ${documentLabel}`
        );

        const schoolVisitDate =
            node.querySelector(
                '.app-school-visit-date'
            );

        const schoolVisitStatus =
            node.querySelector(
                '.app-school-visit-status'
            );

        setNodeText(
            schoolVisitDate,
            record.schoolVisitScheduledAt
                ? formatDateTime(
                    record.schoolVisitScheduledAt
                )
                : '—'
        );

        setNodeText(
            schoolVisitStatus,
            record.schoolVisitStatus
                ? formatEnum(
                    record.schoolVisitStatus
                )
                : ''
        );

        setNodeText(
            node.querySelector('.app-scholarship-value'),
            formatEnum(record.scholarshipStatus)
        );

        setNodeText(
            node.querySelector('.td-date'),
            formatDateTime(
                record.submittedDate,
                false
            )
        );

        const statusCell =
            node.querySelector('.td-status');

        if (statusCell) {
            statusCell.replaceChildren(
                createStatusBadge(
                    record.applicationStatus
                    || record.status
                )
            );
        }

        const applicationId =
            Number(record.applicationId);

        /*
         * The complete Application table row opens the existing Application
         * Profile. Interactive controls inside the row keep their own
         * behavior and never trigger row navigation.
         */
        const tableRow =
            node.querySelector('tr');

        const openRowApplication = () => {
            if (!Number.isInteger(applicationId)
                    || applicationId <= 0) {
                notifyError(
                    'The selected application is invalid.'
                );
                return;
            }

            void openApplication(
                applicationId
            );
        };

        if (tableRow) {
            tableRow.classList.add(
                'app-clickable-row'
            );

            tableRow.style.cursor = 'pointer';
            tableRow.tabIndex = 0;
            tableRow.setAttribute(
                'aria-label',
                `Open application ${
                    displayValue(record.applicationNo)
                } for ${
                    displayValue(record.studentName)
                }`
            );

            tableRow.addEventListener(
                'click',
                event => {
                    const interactive =
                        event.target.closest(
                            'button, a, input, select, textarea, label, '
                            + '[role="button"], [data-no-row-open]'
                        );

                    if (interactive) {
                        return;
                    }

                    openRowApplication();
                }
            );

            tableRow.addEventListener(
                'keydown',
                event => {
                    if (
                        event.key !== 'Enter'
                        && event.key !== ' '
                    ) {
                        return;
                    }

                    const interactive =
                        event.target.closest(
                            'button, a, input, select, textarea, label, '
                            + '[role="button"], [data-no-row-open]'
                        );

                    if (interactive) {
                        return;
                    }

                    event.preventDefault();
                    openRowApplication();
                }
            );
        }

        const selectCheckbox =
            node.querySelector('.app-row-select');

        if (selectCheckbox) {
            selectCheckbox.checked =
                Number.isInteger(applicationId)
                && state.selectedApplications
                    .has(applicationId);

            selectCheckbox.addEventListener(
                'change',
                event => {
                    if (!Number.isInteger(applicationId)
                            || applicationId <= 0) {
                        event.currentTarget.checked = false;
                        return;
                    }

                    if (event.currentTarget.checked) {
                        state.selectedApplications.set(
                            applicationId,
                            record
                        );
                    } else {
                        state.selectedApplications.delete(
                            applicationId
                        );
                    }

                    updateBulkSelectionUI(
                        getFilteredRows()
                    );
                }
            );
        }

        const profileLinks =
            node.querySelectorAll(
                '.app-profile-link'
            );

        profileLinks.forEach(
            profileLink => {
                profileLink.addEventListener(
                    'click',
                    openRowApplication
                );
            }
        );

        const nextActionButton =
            node.querySelector(
                '.btn-next-action'
            );

        const nextActionLabel =
            node.querySelector(
                '.btn-next-action-label'
            );

        const actionAvailable =
            record.nextActionAvailable === true
            && record.nextAction
            && record.nextTargetStage
            && !record.workflowLocked;

        if (nextActionButton
                && actionAvailable) {
            nextActionButton.classList.remove(
                'hidden'
            );

            nextActionButton.title =
                record.nextActionLabel
                    ? String(record.nextActionLabel)
                    : 'Continue to next admission stage';

            setNodeText(
                nextActionLabel,
                compactWorkflowActionLabel(
                    record.nextActionLabel,
                    record.nextTargetStage
                )
            );

            nextActionButton.addEventListener(
                'click',
                () => {
                    void openRowNextActionConfirmation(
                        record
                    );
                }
            );
        } else if (nextActionButton) {
            nextActionButton.classList.add(
                'hidden'
            );
        }

        const noNextAction =
            node.querySelector(
                '.app-no-next-action'
            );

        if (noNextAction) {
            noNextAction.classList.toggle(
                'hidden',
                Boolean(actionAvailable)
            );
        }

        return node;
    }

    /**
     * Loads and renders the complete application profile.
     *
     * @param {number} applicationId
     */
    async function openApplication(
        applicationId,
        options = {}
    ) {
        const {
            preservePosition = false,
            silent = false
        } = options || {};

        const viewportSnapshot =
            preservePosition
            && typeof window.erpCaptureViewport
            === 'function'
                ? window.erpCaptureViewport()
                : null;

        const validatedApplicationId =
            Number(applicationId);

        if (!Number.isInteger(validatedApplicationId)
                || validatedApplicationId <= 0) {
            notifyError(
                'A valid Application ID is required.'
            );
            return false;
        }

        let loaderToken = null;

        stopApplicationListAutoSync();

        view.detailComponent?.setAttribute(
            'aria-busy',
            'true'
        );

        view.detailComponent?.classList.add(
            'app-detail-loading'
        );

        if (
            !silent
            && typeof showLoader === 'function'
        ) {
            loaderToken = showLoader(
                'Opening Application details...'
            );
        }

        try {
            const response =
                await apiGet(
                    `${API_ROOT}/${encodeURIComponent(
                        validatedApplicationId
                    )}`
                );

            const application =
                response && response.data
                    ? response.data
                    : null;

            if (!application) {
                throw new Error(
                    'Application details were not returned.'
                );
            }

            const applicationChanged =
                Number(state.currentApplicationId)
                !== validatedApplicationId;

            if (applicationChanged) {
                state.failedDocumentViewUrls.clear();
            }

            state.currentApplicationId =
                validatedApplicationId;

            state.currentApplication =
                application;

            /*
             * Keep the currently rendered profile untouched while dependent
             * workflow resources are loading. Render only after all data is
             * ready so users never see partially blank sections.
             */
            const [
                schoolVisitLoaded,
                entranceTestLoaded,
                transitionsLoaded
            ] = await Promise.all([
                loadSchoolVisit(
                    validatedApplicationId,
                    {
                        render: false
                    }
                ),
                loadEntranceTestWithRetry(
                    validatedApplicationId,
                    enumEquals(
                        application.currentStage,
                        'ENTRANCE_TEST'
                    )
                        ? 3
                        : 1,
                    {
                        render: false
                    }
                ),
                loadProfileTransitions(
                    validatedApplicationId,
                    {
                        render: false
                    }
                )
            ]);

            /*
             * If the Application is already in ENTRANCE_TEST, the profile is
             * not considered synchronized until the Entrance Test resource is
             * available. This prevents a stale profile from being reported as
             * successfully refreshed.
             */
            if (
                enumEquals(
                    application.currentStage,
                    'ENTRANCE_TEST'
                )
                && entranceTestLoaded !== true
            ) {
                throw new Error(
                    'Entrance Test details are not available yet.'
                );
            }

            /*
             * All refreshed server data is ready. Apply the DOM changes in
             * one pass instead of rendering each section at different times.
             */
            renderApplicationDetails(
                application
            );

            /*
             * renderApplicationDetails() resets the School Visit placeholders
             * while preparing the profile. Restore the authoritative School
             * Visit object that was already fetched above before rendering it.
             *
             * Without this restoration, resetSchoolVisitDisplay() clears
             * state.schoolVisit and the UI incorrectly falls back to
             * "Not Scheduled" even when the backend returned SCHEDULED.
             */
            state.schoolVisit =
                schoolVisitLoaded || null;

            renderSchoolVisit(
                state.schoolVisit
            );

            renderEntranceTest(
                state.entranceTest
            );

            const primaryTransition =
                state.profileTransitions.find(
                    transition =>
                        enumEquals(
                            transition?.action,
                            'ADVANCE'
                        )
                ) || null;

            renderProfileNextAction(
                primaryTransition
            );

            hideElement(view.tableComponent);
            showElement(view.detailComponent);

            startDocumentAutoSync(
                validatedApplicationId
            );

            if (!preservePosition) {
                window.scrollTo({
                    top: 0,
                    behavior: 'auto'
                });
            } else if (
                viewportSnapshot
                && typeof window.erpRestoreViewport
                === 'function'
            ) {
                window.erpRestoreViewport(
                    viewportSnapshot
                );
            }

            return true;
        } catch (error) {
            notifyError(
                readErrorMessage(
                    error,
                    'Application profile could not be loaded.'
                )
            );

            if (
                view.tableComponent
                && !view.tableComponent.classList.contains(
                    'hidden'
                )
            ) {
                startApplicationListAutoSync();
            }

            return false;
        } finally {
            view.detailComponent?.removeAttribute(
                'aria-busy'
            );

            view.detailComponent?.classList.remove(
                'app-detail-loading'
            );

            if (loaderToken
                    && typeof hideLoader === 'function') {
                hideLoader(loaderToken);
            }
        }
    }

    /**
     * Renders all application profile sections.
     *
     * @param {Object} application
     */
    function renderApplicationDetails(
        application
    ) {
        const fullName =
            application.fullName
            || joinNames(
                application.firstName,
                application.middleName,
                application.lastName
            );

        setText(
            'detail-appStudentNameHeader',
            fullName
        );

        setText(
            'detail-appNoHeader',
            application.applicationNo
                ? ` • ${application.applicationNo}`
                : ''
        );

        setText(
            'view-studentName',
            fullName
        );

        setText(
            'view-appNo',
            application.applicationNo
        );

        setText(
            'summary-appClass',
            application.className
        );

        setText(
            'summary-appYearTerm',
            buildYearTerm(application)
        );

        renderBadgeInto(
            'summary-appStatus',
            application.applicationStatus
        );

        setText(
            'summary-appCurrentStage',
            formatEnum(application.currentStage)
        );

        setText(
            'summary-appScholarship',
            formatEnum(
                application.scholarshipWorkflowStatus
                || application.scholarshipStatus
            )
        );

        setText(
            'view-applicationNo',
            application.applicationNo
        );

        setText(
            'view-appStatus',
            formatEnum(application.applicationStatus)
        );

        setText(
            'view-admissionType',
            formatEnum(application.admissionType)
        );

        setText(
            'view-registrationDate',
            formatDateTime(
                application.dateOfRegistration,
                false
            )
        );

        setText(
            'view-academicYear',
            application.academicYearName
            || application.academicYearCode
        );

        setText(
            'view-joiningTerm',
            application.joiningTermName
            || application.term
        );

        setText(
            'view-className',
            application.className
        );

        setText(
            'view-scholarshipStatus',
            application.scholarshipStatus
        );

        setText(
            'view-moreInfo',
            application.moreInfo
        );

        setText(
            'view-remarks',
            application.remarks
        );

        setText(
            'view-firstName',
            application.firstName
        );

        setText(
            'view-middleName',
            application.middleName
        );

        setText(
            'view-lastName',
            application.lastName
        );

        setText(
            'view-gender',
            formatEnum(application.gender)
        );

        setText(
            'view-dob',
            formatDateTime(
                application.dateOfBirth,
                false
            )
        );

        setText(
            'view-nationality',
            application.nationality
        );

        setText(
            'view-primaryMobile',
            application.primaryMobile
        );

        setText(
            'view-primaryEmail',
            application.primaryEmail
        );

        setText(
            'view-addressRegion',
            application.addressRegion
        );

        setText(
            'view-addressDistrict',
            application.addressDistrict
        );

        setText(
            'view-addressVillage',
            application.addressVillage
        );

        setText(
            'view-addressStreet',
            application.addressStreet
        );

        setText(
            'view-addressHouse',
            application.addressHouse
        );

        setText(
            'view-addressPostal',
            application.addressPostal
        );

        setText(
            'view-fatherName',
            application.fatherName
        );

        setText(
            'view-fatherAge',
            application.fatherAge
        );

        setText(
            'view-fatherContact',
            application.fatherContact
        );

        setText(
            'view-fatherEmail',
            application.fatherEmail
        );

        setText(
            'view-fatherOccupation',
            application.fatherOccupation
        );

        setText(
            'view-fatherEducation',
            application.fatherEducation
        );

        setText(
            'view-motherName',
            application.motherName
        );

        setText(
            'view-motherAge',
            application.motherAge
        );

        setText(
            'view-motherContact',
            application.motherContact
        );

        setText(
            'view-motherEmail',
            application.motherEmail
        );

        setText(
            'view-motherOccupation',
            application.motherOccupation
        );

        setText(
            'view-motherEducation',
            application.motherEducation
        );

        setText(
            'view-guardianName',
            application.guardianName
        );

        setText(
            'view-guardianAge',
            application.guardianAge
        );

        setText(
            'view-guardianRelation',
            application.guardianRelation
        );

        setText(
            'view-guardianMobile',
            application.guardianMobile
            || application.guardianContact
        );

        setText(
            'view-guardianEmail',
            application.guardianEmail
        );

        setText(
            'view-guardianOccupation',
            application.guardianOccupation
        );

        setText(
            'view-guardianEducation',
            application.guardianEducation
        );

        setText(
            'view-guardianLocation',
            application.guardianLocation
        );

        setText(
            'view-previousSchool',
            application.previousSchool
        );

        setText(
            'view-formerSchool',
            application.formerSchool
        );

        setText(
            'view-formerSchoolCode',
            application.formerSchoolCode
        );

        setText(
            'view-formerSchoolLin',
            application.formerSchoolLin
        );

        setText(
            'view-pleRef',
            application.pleRef
        );

        setText(
            'view-pleScore',
            application.pleScore
        );

        setText(
            'view-uceRef',
            application.uceRef
        );

        setText(
            'view-uceScore',
            application.uceScore
        );

        setText(
            'view-subjectMarks',
            application.subjectMarks
        );

        setText(
            'view-previousMarksDocument',
            application.previousMarksDocumentAvailable
                ? 'Available'
                : 'Not available'
        );

        setText(
            'view-currentStage',
            formatEnum(application.currentStage)
        );

        setText(
            'view-verificationStatus',
            formatEnum(application.verificationStatus)
        );

        setText(
            'view-documentStatus',
            formatEnum(application.documentStatus)
        );

        setText(
            'view-testStatus',
            formatEnum(application.testStatus)
        );

        setText(
            'view-feeStatus',
            formatEnum(application.feeDecisionStatus)
        );

        setText(
            'view-scholarshipWorkflowStatus',
            formatEnum(
                application.scholarshipWorkflowStatus
            )
        );

        setText(
            'view-paymentStatus',
            formatEnum(application.paymentStatus)
        );

        setText(
            'view-admissionStatus',
            formatEnum(application.admissionStatus)
        );

        resetSchoolVisitDisplay(application);

        setText(
            'view-verificationDecision',
            buildVerificationDecision(application)
        );

        setText(
            'view-rejectionReason',
            application.rejectionReason
        );

        renderApplicationPhoto(application);
        renderDocuments(
            Array.isArray(application.documents)
                ? application.documents
                : []
        );

        renderDocumentRequests(
            Array.isArray(application.documentRequests)
                ? application.documentRequests
                : []
        );

        renderHistory(
            Array.isArray(application.statusHistory)
                ? application.statusHistory
                : []
        );

        setActionAvailability(application);
    }


    /**
     * Loads and renders the existing Entrance Test state.
     */
    async function loadEntranceTest(
        applicationId,
        options = {}
    ) {
        const {
            render = true
        } = options || {};
        if (!applicationId) {
            return;
        }

        try {
            const response =
                await apiGet(
                    `${API_ROOT}/${encodeURIComponent(
                        applicationId
                    )}/workflow/entrance-test`
                );

            state.entranceTest =
                unwrapResponseData(response);

            if (render) {
                renderEntranceTest(
                    state.entranceTest
                );
            }

            return Boolean(
                state.entranceTest
            );
        } catch (error) {
            /*
             * Entrance Test data is stage-specific. Do not break the complete
             * Application profile if the test is not yet available.
             */
            state.entranceTest = null;

            if (enumEquals(
                state.currentApplication?.currentStage,
                'ENTRANCE_TEST'
            )) {
                console.error(
                    'Entrance Test details could not be loaded.',
                    error
                );
            }

            return false;
        }
    }

    async function loadEntranceTestWithRetry(
            applicationId,
            attempts = 3,
            options = {}
    ) {
        const maxAttempts =
            Math.max(
                1,
                Number(attempts) || 1
            );

        for (
            let attempt = 1;
            attempt <= maxAttempts;
            attempt += 1
        ) {
            const loaded =
                await loadEntranceTest(
                    applicationId,
                    options
                );

            if (loaded) {
                return true;
            }

            if (
                attempt < maxAttempts
                && enumEquals(
                    state.currentApplication?.currentStage,
                    'ENTRANCE_TEST'
                )
            ) {
                await new Promise(
                    resolve =>
                        window.setTimeout(
                            resolve,
                            250
                        )
                );
            }
        }

        return false;
    }

    function renderEntranceTest(test) {
        if (!test) {
            toggleElement(
                view.entranceTestSection,
                false
            );
            return;
        }

        const currentStage =
            test.currentStage
            || state.currentApplication?.currentStage;

        const status =
            String(
                test.status || 'NOT_SCHEDULED'
            ).toUpperCase();

        const result =
            String(
                test.result || 'PENDING'
            ).toUpperCase();

        const shouldShow =
            enumEquals(
                currentStage,
                'ENTRANCE_TEST'
            )
            || status !== 'NOT_SCHEDULED'
            || Boolean(test.interviewId);

        toggleElement(
            view.entranceTestSection,
            shouldShow
        );

        const directMarksReady =
            status === 'SCHEDULED'
            && !test.scheduledAt
            && enumEquals(
                currentStage,
                'ENTRANCE_TEST'
            );

        setNodeText(
            view.entranceTestStatus,
            directMarksReady
                ? 'Ready for Marks'
                : formatEnum(status)
        );

        setNodeText(
            view.entranceTestResult,
            result === 'PENDING'
                ? 'Pending'
                : formatEnum(result)
        );

        setNodeText(
            view.entranceTestEmployee,
            displayValue(
                test.employeeName
            )
        );

        setNodeText(
            view.entranceTestCompletedAt,
            displayValue(
                formatDateTime(
                    test.completedAt
                )
            )
        );

        setNodeText(
            view.entranceTestRemarks,
            displayValue(
                test.employeeRemarks
            )
        );

        renderEntranceTestSubjectMarks(
            test
        );

        toggleElement(
            view.entranceTestEnterMarksButton,
            enumEquals(
                currentStage,
                'ENTRANCE_TEST'
            )
            && test.canComplete === true
        );

        toggleElement(
            view.entranceTestUpdateResultButton,
            enumEquals(
                currentStage,
                'ENTRANCE_TEST'
            )
            && status === 'COMPLETED'
            && result === 'WAITLIST'
            && test.canUpdateWaitlistResult === true
        );

        if (state.currentApplication) {
            state.currentApplication.testStatus =
                result === 'PASSED'
                    ? 'PASSED'
                    : result === 'FAILED'
                        ? 'FAILED'
                        : result === 'WAITLIST'
                            ? 'WAITLISTED'
                            : result === 'RETEST_REQUIRED'
                                ? 'RETEST_REQUIRED'
                                : state.currentApplication.testStatus;
        }
    }

    function renderEntranceTestSubjectMarks(test) {
        const marks =
            Array.isArray(test?.marks)
                ? test.marks
                : [];

        toggleElement(
            view.entranceTestMarksBlock,
            marks.length > 0
        );

        if (!view.entranceTestMarksBody) {
            return;
        }

        view.entranceTestMarksBody.replaceChildren();

        if (marks.length === 0) {
            return;
        }

        marks.forEach((mark, index) => {
            const row =
                document.createElement('tr');

            const serial =
                document.createElement('td');
            serial.textContent =
                String(index + 1);

            const subject =
                document.createElement('td');

            const subjectName =
                document.createElement('strong');
            subjectName.textContent =
                displayValue(
                    mark.subjectName
                );

            subject.appendChild(
                subjectName
            );


            const maximum =
                document.createElement('td');
            maximum.textContent =
                formatEntranceTestNumber(
                    mark.maximumMarks
                );

            const obtained =
                document.createElement('td');
            obtained.textContent =
                formatEntranceTestNumber(
                    mark.obtainedMarks
                );

            const percentage =
                document.createElement('td');
            percentage.textContent =
                mark.percentage === null
                || mark.percentage === undefined
                    ? '—'
                    : `${formatEntranceTestNumber(
                        mark.percentage
                    )}%`;

            row.append(
                serial,
                subject,
                maximum,
                obtained,
                percentage
            );

            view.entranceTestMarksBody.appendChild(
                row
            );
        });

        setNodeText(
            view.entranceTestMarksTotal,
            `${formatEntranceTestNumber(
                test.obtainedMarks
            )} / ${formatEntranceTestNumber(
                test.maximumMarks
            )}`
        );

        setNodeText(
            view.entranceTestMarksPercentage,
            test.percentage === null
            || test.percentage === undefined
                ? '—'
                : `${formatEntranceTestNumber(
                    test.percentage
                )}%`
        );
    }

    function openWaitlistResultModal() {
        const test =
            state.entranceTest;

        if (
            !test
            || String(test.status || '').toUpperCase()
            !== 'COMPLETED'
            || String(test.result || '').toUpperCase()
            !== 'WAITLIST'
            || test.canUpdateWaitlistResult !== true
        ) {
            notifyError(
                'This Entrance Test is not available for a waitlist final decision.'
            );
            return;
        }

        if (view.waitlistFinalResultSelect) {
            view.waitlistFinalResultSelect.value =
                '';
        }

        if (view.waitlistResultRemarks) {
            view.waitlistResultRemarks.value =
                '';
        }

        clearInlineError(
            view.waitlistResultError
        );

        renderWaitlistResultMarks(
            test.marks
        );

        openModal(
            view.waitlistResultModal
        );
    }

    function closeWaitlistResultModal() {
        clearInlineError(
            view.waitlistResultError
        );

        closeModal(
            view.waitlistResultModal
        );
    }

    function renderWaitlistResultMarks(marks) {
        const body =
            view.waitlistResultMarksBody;

        if (!body) {
            return;
        }

        body.replaceChildren();

        const rows =
            Array.isArray(marks)
                ? marks
                : [];

        if (rows.length === 0) {
            const row =
                document.createElement('tr');

            const cell =
                document.createElement('td');

            cell.colSpan = 5;
            cell.className =
                'text-center text-muted';
            cell.textContent =
                'No subject marks are available.';

            row.appendChild(cell);
            body.appendChild(row);
            return;
        }

        rows.forEach((mark, index) => {
            const row =
                document.createElement('tr');

            const values = [
                index + 1,
                displayValue(
                    mark.subjectName
                ),
                formatEntranceTestNumber(
                    mark.maximumMarks
                ),
                formatEntranceTestNumber(
                    mark.obtainedMarks
                ),
                mark.percentage === null
                || mark.percentage === undefined
                    ? '—'
                    : `${formatEntranceTestNumber(
                        mark.percentage
                    )}%`
            ];

            values.forEach(value => {
                const cell =
                    document.createElement('td');

                cell.textContent =
                    String(value);

                row.appendChild(cell);
            });

            body.appendChild(row);
        });
    }

    async function submitWaitlistResult() {
        const applicationId =
            Number(
                state.currentApplicationId
            );

        if (
            !Number.isInteger(applicationId)
            || applicationId <= 0
        ) {
            notifyError(
                'A valid Application ID is required.'
            );
            return;
        }

        const result =
            String(
                view.waitlistFinalResultSelect?.value
                || ''
            ).toUpperCase();

        const remarks =
            String(
                view.waitlistResultRemarks?.value
                || ''
            ).trim();

        if (
            result !== 'PASSED'
            && result !== 'FAILED'
        ) {
            showInlineError(
                view.waitlistResultError,
                'Select Pass or Fail.'
            );
            return;
        }

        if (!remarks) {
            showInlineError(
                view.waitlistResultError,
                'Decision remarks are required.'
            );
            return;
        }

        clearInlineError(
            view.waitlistResultError
        );

        setButtonBusy(
            view.waitlistResultSaveButton,
            true,
            'Saving...'
        );

        let loaderToken = null;

        try {
            if (typeof showLoader === 'function') {
                loaderToken =
                    showLoader(
                        'Saving Entrance Test final result...'
                    );
            }

            await apiPatchJson(
                `${API_ROOT}/${encodeURIComponent(
                    applicationId
                )}/workflow/entrance-test/waitlist-result`,
                {
                    result,
                    remarks
                }
            );

            closeWaitlistResultModal();

            /*
             * Keep the global foreground loader and screen lock active while
             * the authoritative profile is synchronized. The user should
             * never see the old WAITLIST state as if the operation finished.
             */
            const refreshed =
                await synchronizeCurrentApplicationAfterMutation(
                    'entrance-test-waitlist-result',
                    applicationId
                );

            if (!refreshed) {
                throw new Error(
                    'The final result was saved, but the refreshed Application profile could not be loaded.'
                );
            }

            notifySuccess(
                result === 'PASSED'
                    ? 'Waitlist result updated to Pass successfully.'
                    : 'Waitlist result updated to Fail successfully.'
            );
        } catch (error) {
            const message =
                readErrorMessage(
                    error,
                    'Entrance Test result could not be updated.'
                );

            /*
             * If the modal is still open, show the problem inline.
             * If the PATCH committed and only the later refresh failed,
             * surface the error globally because the modal has already closed.
             */
            if (
                view.waitlistResultModal
                && !view.waitlistResultModal.classList.contains(
                    'hidden'
                )
            ) {
                showInlineError(
                    view.waitlistResultError,
                    message
                );
            } else {
                notifyError(
                    message
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

            setButtonBusy(
                view.waitlistResultSaveButton,
                false
            );
        }
    }

    function formatEntranceTestNumber(value) {
        if (value === null
                || value === undefined
                || value === '') {
            return '—';
        }

        const number =
            Number(value);

        if (!Number.isFinite(number)) {
            return String(value);
        }

        return Number.isInteger(number)
            ? String(number)
            : number.toFixed(2).replace(
                /\.?0+$/,
                ''
            );
    }

    function openEntranceTestMarksModal() {
        const test =
            state.entranceTest;

        if (!test
                || test.canComplete !== true) {
            notifyError(
                'Entrance Test marks cannot be entered in the current state.'
            );
            return;
        }

        if (!Array.isArray(test.availableSubjects)
                || test.availableSubjects.length === 0) {
            notifyError(
                'No active subjects are available for Entrance Test mark entry.'
            );
            return;
        }

        view.entranceTestMarksRows?.replaceChildren();

        const existingMarks =
            Array.isArray(test.marks)
                ? test.marks
                : [];

        if (existingMarks.length > 0) {
            existingMarks.forEach(mark =>
                addEntranceTestMarkRow(mark)
            );
        } else {
            addEntranceTestMarkRow();
        }

        if (view.entranceTestResultSelect) {
            view.entranceTestResultSelect.value =
                test.result
                && String(test.result).toUpperCase() !== 'PENDING'
                    ? String(test.result).toUpperCase()
                    : '';
        }

        if (view.entranceTestCompletedAtInput) {
            view.entranceTestCompletedAtInput.value =
                toDateTimeLocalValue(
                    test.completedAt
                );
        }

        if (view.entranceTestEmployeeRemarks) {
            view.entranceTestEmployeeRemarks.value =
                test.employeeRemarks || '';
        }

        if (view.entranceTestInternalRemarks) {
            view.entranceTestInternalRemarks.value =
                test.internalRemarks || '';
        }

        updateEntranceTestLiveTotals();

        clearInlineError(
            view.entranceTestMarksError
        );

        openModal(
            view.entranceTestMarksModal
        );
    }

    function closeEntranceTestMarksModal() {
        closeModal(
            view.entranceTestMarksModal
        );

        view.entranceTestMarksForm?.reset();
        view.entranceTestMarksRows?.replaceChildren();

        clearInlineError(
            view.entranceTestMarksError
        );
    }

    function addEntranceTestMarkRow(existingMark = null) {
        const container =
            view.entranceTestMarksRows;

        const test =
            state.entranceTest;

        if (!container || !test) {
            return;
        }

        const row =
            document.createElement('tr');

        row.className =
            'entrance-test-mark-row';

        const subjectCell =
            document.createElement('td');

        const subjectSelect =
            document.createElement('select');

        subjectSelect.className =
            'detail-input w-100 entrance-test-subject';

        subjectSelect.required =
            true;

        const placeholder =
            document.createElement('option');

        placeholder.value = '';

        placeholder.textContent =
            '-- Select Subject --';

        subjectSelect.appendChild(
            placeholder
        );

        test.availableSubjects.forEach(subject => {
            const option =
                document.createElement('option');

            option.value =
                String(subject.subjectId);

            option.textContent =
                displayValue(
                    subject.subjectName
                );

            subjectSelect.appendChild(
                option
            );
        });

        if (existingMark?.subjectId) {
            subjectSelect.value =
                String(existingMark.subjectId);
        }

        subjectCell.appendChild(
            subjectSelect
        );

        const maximumCell =
            document.createElement('td');

        const maximumInput =
            document.createElement('input');

        maximumInput.type = 'number';
        maximumInput.min = '0.01';
        maximumInput.step = '0.01';
        maximumInput.required = true;
        maximumInput.className =
            'detail-input w-100 entrance-test-maximum';
        maximumInput.placeholder = '100';
        maximumInput.value =
            existingMark?.maximumMarks ?? '';

        maximumCell.appendChild(
            maximumInput
        );

        const obtainedCell =
            document.createElement('td');

        const obtainedInput =
            document.createElement('input');

        obtainedInput.type = 'number';
        obtainedInput.min = '0';
        obtainedInput.step = '0.01';
        obtainedInput.required = true;
        obtainedInput.className =
            'detail-input w-100 entrance-test-obtained';
        obtainedInput.placeholder = '0';
        obtainedInput.value =
            existingMark?.obtainedMarks ?? '';

        obtainedCell.appendChild(
            obtainedInput
        );

        const percentageCell =
            document.createElement('td');

        const percentageValue =
            document.createElement('span');

        percentageValue.className =
            'entrance-test-row-percentage text-strong';

        percentageValue.textContent =
            existingMark?.percentage === null
            || existingMark?.percentage === undefined
                ? '—'
                : `${formatEntranceTestNumber(
                    existingMark.percentage
                )}%`;

        percentageCell.appendChild(
            percentageValue
        );

        const actionCell =
            document.createElement('td');

        actionCell.className =
            'col-action align-center';

        const removeButton =
            document.createElement('button');

        removeButton.type = 'button';

        removeButton.className =
            'btn-danger btn-sm';

        removeButton.title =
            'Remove Subject';

        removeButton.innerHTML =
            '<i class="bi bi-trash"></i>';

        removeButton.addEventListener(
            'click',
            () => {
                if (container.children.length <= 1) {
                    notifyError(
                        'At least one subject mark is required.'
                    );
                    return;
                }

                row.remove();

                updateEntranceTestLiveTotals();
            }
        );

        maximumInput.addEventListener(
            'input',
            () => {
                updateEntranceTestMarkRowPercentage(
                    row
                );
            }
        );

        obtainedInput.addEventListener(
            'input',
            () => {
                updateEntranceTestMarkRowPercentage(
                    row
                );
            }
        );

        actionCell.appendChild(
            removeButton
        );

        row.append(
            subjectCell,
            maximumCell,
            obtainedCell,
            percentageCell,
            actionCell
        );

        container.appendChild(
            row
        );

        updateEntranceTestMarkRowPercentage(
            row
        );
    }

    function updateEntranceTestMarkRowPercentage(row) {
        if (!row) {
            return;
        }

        const maximum =
            Number(
                row.querySelector(
                    '.entrance-test-maximum'
                )?.value
            );

        const obtained =
            Number(
                row.querySelector(
                    '.entrance-test-obtained'
                )?.value
            );

        const output =
            row.querySelector(
                '.entrance-test-row-percentage'
            );

        if (!output) {
            return;
        }

        if (
            !Number.isFinite(maximum)
            || maximum <= 0
            || !Number.isFinite(obtained)
            || obtained < 0
        ) {
            output.textContent = '—';
        } else {
            output.textContent =
                `${formatEntranceTestNumber(
                    (obtained / maximum) * 100
                )}%`;
        }

        updateEntranceTestLiveTotals();
    }

    function updateEntranceTestLiveTotals() {
        const rows =
            Array.from(
                view.entranceTestMarksRows
                    ?.querySelectorAll(
                        '.entrance-test-mark-row'
                    )
                || []
            );

        let maximumTotal = 0;
        let obtainedTotal = 0;

        rows.forEach(row => {
            const maximum =
                Number(
                    row.querySelector(
                        '.entrance-test-maximum'
                    )?.value
                );

            const obtained =
                Number(
                    row.querySelector(
                        '.entrance-test-obtained'
                    )?.value
                );

            if (
                Number.isFinite(maximum)
                && maximum > 0
            ) {
                maximumTotal += maximum;
            }

            if (
                Number.isFinite(obtained)
                && obtained >= 0
            ) {
                obtainedTotal += obtained;
            }
        });

        setNodeText(
            view.entranceTestLiveMaximum,
            formatEntranceTestNumber(
                maximumTotal
            )
        );

        setNodeText(
            view.entranceTestLiveObtained,
            formatEntranceTestNumber(
                obtainedTotal
            )
        );

        setNodeText(
            view.entranceTestLivePercentage,
            maximumTotal > 0
                ? `${formatEntranceTestNumber(
                    (obtainedTotal / maximumTotal) * 100
                )}%`
                : '0%'
        );
    }

    function collectEntranceTestMarks() {
        const rows =
            Array.from(
                view.entranceTestMarksRows
                    ?.querySelectorAll(
                        '.entrance-test-mark-row'
                    )
                || []
            );

        if (rows.length === 0) {
            throw new Error(
                'Enter marks for at least one subject.'
            );
        }

        const usedSubjects =
            new Set();

        return rows.map(row => {
            const subjectId =
                Number(
                    row.querySelector(
                        '.entrance-test-subject'
                    )?.value
                );

            const maximumMarks =
                Number(
                    row.querySelector(
                        '.entrance-test-maximum'
                    )?.value
                );

            const obtainedMarks =
                Number(
                    row.querySelector(
                        '.entrance-test-obtained'
                    )?.value
                );

            if (!Number.isInteger(subjectId)
                    || subjectId <= 0) {
                throw new Error(
                    'Select a subject for every marks row.'
                );
            }

            if (usedSubjects.has(subjectId)) {
                throw new Error(
                    'The same subject cannot be entered more than once.'
                );
            }

            usedSubjects.add(
                subjectId
            );

            if (!Number.isFinite(maximumMarks)
                    || maximumMarks <= 0) {
                throw new Error(
                    'Maximum marks must be greater than zero.'
                );
            }

            if (!Number.isFinite(obtainedMarks)
                    || obtainedMarks < 0) {
                throw new Error(
                    'Obtained marks cannot be negative.'
                );
            }

            if (obtainedMarks > maximumMarks) {
                throw new Error(
                    'Obtained marks cannot exceed maximum marks.'
                );
            }

            return {
                subjectId,
                maximumMarks,
                obtainedMarks,
                remarks: null
            };
        });
    }

    async function submitEntranceTestMarks() {
        if (!state.currentApplicationId) {
            return;
        }

        setButtonBusy(
            view.entranceTestSaveMarksButton,
            true,
            'Saving...'
        );

        clearInlineError(
            view.entranceTestMarksError
        );

        let loaderToken = null;

        try {
            const result =
                trimValue(
                    view.entranceTestResultSelect
                );

            if (!result) {
                throw new Error(
                    'Select the Entrance Test result.'
                );
            }

            const marks =
                collectEntranceTestMarks();

            if (typeof showLoader === 'function') {
                loaderToken =
                    showLoader(
                        'Saving Entrance Test result...'
                    );
            }

            const response =
                await apiPatchJson(
                    `${API_ROOT}/${encodeURIComponent(
                        state.currentApplicationId
                    )}/workflow/entrance-test/complete`,
                    {
                        completedAt:
                            trimValue(
                                view.entranceTestCompletedAtInput
                            ) || null,
                        marks,
                        result,
                        employeeRemarks:
                            trimValue(
                                view.entranceTestEmployeeRemarks
                            ) || null,
                        internalRemarks:
                            trimValue(
                                view.entranceTestInternalRemarks
                            ) || null
                    }
                );

            state.entranceTest =
                unwrapResponseData(
                    response
                );

            closeEntranceTestMarksModal();

            renderEntranceTest(
                state.entranceTest
            );

            /*
             * The PATCH already queued one global mutation. Keep the global
             * foreground loader active until the refreshed Entrance Test
             * profile has been synchronized and rendered completely.
             */
            const refreshed =
                await synchronizeCurrentApplicationAfterMutation(
                    'entrance-test-result',
                    Number(
                        state.currentApplicationId
                    )
                );

            if (!refreshed) {
                throw new Error(
                    'The Entrance Test result was saved, but the refreshed Application profile could not be loaded.'
                );
            }

            notifySuccess(
                'Entrance Test result saved successfully.'
            );
        } catch (error) {
            showInlineError(
                view.entranceTestMarksError,
                readErrorMessage(
                    error,
                    'Entrance Test result could not be saved.'
                )
            );
        } finally {
            if (loaderToken
                    && typeof hideLoader === 'function') {
                hideLoader(
                    loaderToken
                );
            }

            setButtonBusy(
                view.entranceTestSaveMarksButton,
                false,
                'Save Result'
            );
        }
    }

    /**
     * Resets the School Visit panel while its authoritative state is loaded.
     *
     * @param {Object} application
     */
    function resetSchoolVisitDisplay(
        application
    ) {
        state.schoolVisit = null;

        const isSchoolVisitStage =
            enumEquals(
                application?.currentStage,
                'SCHOOL_VISIT'
            );

        toggleElement(
            view.schoolVisitSection,
            isSchoolVisitStage
        );

        setNodeText(
            view.schoolVisitStatusCaption,
            'Not Scheduled'
        );
        setNodeText(
            view.schoolVisitStatus,
            'Not Scheduled'
        );
        setNodeText(
            view.schoolVisitEmployee,
            '—'
        );
        setNodeText(
            view.schoolVisitEmployeeNo,
            '—'
        );
        setNodeText(
            view.schoolVisitScheduledAt,
            '—'
        );
        setNodeText(
            view.schoolVisitVisitedAt,
            '—'
        );
        setNodeText(
            view.schoolVisitStudentAttendance,
            '—'
        );
        setNodeText(
            view.schoolVisitParentAttendance,
            '—'
        );
        setNodeText(
            view.schoolVisitRemarks,
            '—'
        );

        [
            view.schoolVisitScheduleButton,
            view.schoolVisitRescheduleButton,
            view.schoolVisitCompleteButton
        ].forEach(button => {
            button?.classList.add('hidden');
        });
    }

    /**
     * Loads the authoritative School Visit state for the open application.
     *
     * @param {number} applicationId
     */
    async function loadSchoolVisit(
        applicationId,
        options = {}
    ) {
        const {
            render = true
        } = options || {};
        const id =
            Number(applicationId);

        if (!Number.isInteger(id)
                || id <= 0) {
            return;
        }

        try {
            const response =
                await apiGet(
                    `${API_ROOT}/${encodeURIComponent(id)}/school-visit`
                );

            if (Number(state.currentApplicationId) !== id) {
                return;
            }

            const schoolVisit =
                unwrapResponseData(response);

            state.schoolVisit =
                schoolVisit || null;

            if (render) {
                renderSchoolVisit(
                    schoolVisit
                );
            }

            return schoolVisit;
        } catch (error) {
            console.error(
                'School Visit details could not be loaded.',
                error
            );

            if (enumEquals(
                state.currentApplication?.currentStage,
                'SCHOOL_VISIT'
            )) {
                showElement(
                    view.schoolVisitSection
                );

                setNodeText(
                    view.schoolVisitStageMessage,
                    'School Visit details could not be loaded. Refresh the application and try again.'
                );
            }

            return null;
        }
    }

    /**
     * Renders the School Visit panel and backend-approved actions.
     *
     * @param {Object|null} schoolVisit
     */
    function renderSchoolVisit(
        schoolVisit
    ) {
        if (!schoolVisit) {
            return;
        }

        const currentStage =
            schoolVisit.currentStage
            || state.currentApplication?.currentStage;

        const visitStatus =
            String(
                schoolVisit.schoolVisitStatus
                || 'NOT_SCHEDULED'
            ).toUpperCase();

        const shouldShow =
            enumEquals(
                currentStage,
                'SCHOOL_VISIT'
            )
            || visitStatus !== 'NOT_SCHEDULED'
            || Boolean(schoolVisit.scheduledAt)
            || Boolean(schoolVisit.visitedAt);

        toggleElement(
            view.schoolVisitSection,
            shouldShow
        );

        setNodeText(
            view.schoolVisitStatusCaption,
            formatEnum(visitStatus)
            || 'Not Scheduled'
        );

        setNodeText(
            view.schoolVisitStatus,
            formatEnum(visitStatus)
            || 'Not Scheduled'
        );

        setNodeText(
            view.schoolVisitEmployee,
            displayValue(
                schoolVisit.employeeName
            )
        );

        setNodeText(
            view.schoolVisitEmployeeNo,
            displayValue(
                schoolVisit.employeeNo
            )
        );

        setNodeText(
            view.schoolVisitScheduledAt,
            displayValue(
                formatDateTime(
                    schoolVisit.scheduledAt
                )
            )
        );

        setNodeText(
            view.schoolVisitVisitedAt,
            displayValue(
                formatDateTime(
                    schoolVisit.visitedAt
                )
            )
        );

        setNodeText(
            view.schoolVisitStudentAttendance,
            formatAttendance(
                schoolVisit.studentAttended,
                visitStatus
            )
        );

        setNodeText(
            view.schoolVisitParentAttendance,
            formatAttendance(
                schoolVisit.parentAttended,
                visitStatus
            )
        );

        setNodeText(
            view.schoolVisitRemarks,
            displayValue(
                schoolVisit.remarks
            )
        );

        if (view.schoolVisitStageMessage) {
            view.schoolVisitStageMessage.textContent =
                schoolVisit.canProceedToEntranceTest === true
                    ? 'Attendance is recorded. The application is ready for the Entrance Test.'
                    : buildSchoolVisitStageMessage(
                        visitStatus
                    );
        }

        const inSchoolVisitStage =
            enumEquals(
                currentStage,
                'SCHOOL_VISIT'
            );

        /*
         * The workflow state decides which actions are relevant/visible.
         * Backend permission flags decide whether each visible action is
         * currently enabled.
         *
         * This avoids a dead-end UI where the profile clearly says
         * "Not Scheduled" but no Schedule Visit action is visible.
         */
        const scheduleRelevant =
            inSchoolVisitStage
            && (
                visitStatus === 'NOT_SCHEDULED'
                || visitStatus === 'CANCELLED'
                || visitStatus === 'NO_SHOW'
            );

        const scheduledVisit =
            visitStatus === 'SCHEDULED'
            || visitStatus === 'RESCHEDULED';

        const rescheduleRelevant =
            inSchoolVisitStage
            && scheduledVisit;

        const completeRelevant =
            inSchoolVisitStage
            && scheduledVisit;

        toggleElement(
            view.schoolVisitScheduleButton,
            scheduleRelevant
        );

        toggleElement(
            view.schoolVisitRescheduleButton,
            rescheduleRelevant
        );

        toggleElement(
            view.schoolVisitCompleteButton,
            completeRelevant
        );

        setButtonActionAvailability(
            view.schoolVisitScheduleButton,
            schoolVisit.canSchedule === true,
            'This School Visit cannot be scheduled yet. Check workflow and document requirements.'
        );

        setButtonActionAvailability(
            view.schoolVisitRescheduleButton,
            schoolVisit.canReschedule === true,
            'This School Visit cannot be rescheduled at the moment.'
        );

        setButtonActionAvailability(
            view.schoolVisitCompleteButton,
            schoolVisit.canComplete === true,
            'Proceed to Entrance Test is available after the visit is scheduled and required documents are resolved.'
        );

        if (state.currentApplication) {
            Object.assign(
                state.currentApplication,
                {
                    schoolVisitStatus:
                        schoolVisit.schoolVisitStatus,
                    schoolVisitEmployeeId:
                        schoolVisit.employeeId,
                    schoolVisitScheduledAt:
                        schoolVisit.scheduledAt,
                    schoolVisitAt:
                        schoolVisit.visitedAt,
                    schoolVisitStudentAttended:
                        schoolVisit.studentAttended,
                    schoolVisitParentAttended:
                        schoolVisit.parentAttended,
                    schoolVisitRemarks:
                        schoolVisit.remarks,
                    schoolVisitCompletedBy:
                        schoolVisit.completedBy,
                    schoolVisitCompletedAt:
                        schoolVisit.completedAt
                }
            );
        }
    }

    function buildSchoolVisitStageMessage(
        visitStatus
    ) {
        if (visitStatus === 'SCHEDULED') {
            return 'The School Visit is scheduled. When the parent / student arrives, use Proceed to Entrance Test to select the responsible employee and record attendance.';
        }

        if (visitStatus === 'RESCHEDULED') {
            return 'The School Visit has been rescheduled. When the parent / student arrives, use Proceed to Entrance Test to select the responsible employee and record attendance.';
        }

        if (visitStatus === 'ATTENDED') {
            return 'Attendance and the responsible employee are recorded. The Entrance Test process can begin.';
        }

        if (visitStatus === 'CANCELLED') {
            return 'The previous School Visit was cancelled. Schedule a new visit to continue.';
        }

        if (visitStatus === 'NO_SHOW') {
            return 'The previous School Visit was marked as no-show. Schedule another visit to continue.';
        }

        if (visitStatus === 'COMPLETED') {
            return 'School Visit processing is complete.';
        }

        return 'Schedule the School Visit before proceeding to the Entrance Test.';
    }

    function formatAttendance(
        value,
        visitStatus
    ) {
        if (visitStatus !== 'ATTENDED'
                && visitStatus !== 'COMPLETED') {
            return '—';
        }

        return value === true
            ? 'Attended'
            : value === false
                ? 'Not Attended'
                : '—';
    }

    /**
     * Loads eligible Branch employees once per Applications view.
     */
    async function loadSchoolVisitEmployees() {
        if (state.schoolVisitEmployees.length > 0) {
            populateSchoolVisitEmployeeOptions();
            return;
        }

        const response =
            await apiGet(
                EMPLOYEE_OPTIONS_API
            );

        const employees =
            unwrapResponseData(response);

        state.schoolVisitEmployees =
            Array.isArray(employees)
                ? employees
                : [];

        populateSchoolVisitEmployeeOptions();
    }

    function populateSchoolVisitEmployeeOptions() {
        const select =
            view.schoolVisitCompleteEmployeeSelect;

        if (!select) {
            return;
        }

        const currentValue =
            select.value;

        select.replaceChildren();

        const placeholder =
            document.createElement('option');

        placeholder.value = '';
        placeholder.textContent =
            '-- Select Employee --';

        select.appendChild(placeholder);

        state.schoolVisitEmployees
            .forEach(employee => {
                const option =
                    document.createElement('option');

                option.value =
                    String(employee.employeeId);

                const details =
                    [
                        employee.employeeNo,
                        employee.designationName,
                        employee.departmentName
                    ]
                        .filter(Boolean)
                        .join(' • ');

                option.textContent =
                    details
                        ? `${displayValue(employee.fullName)} — ${details}`
                        : displayValue(employee.fullName);

                select.appendChild(option);
            });

        if (currentValue) {
            select.value =
                currentValue;
        }
    }

    async function openSchoolVisitScheduleModal(
        reschedule,
        transitionContext = null
    ) {
        if (!view.schoolVisitScheduleModal) {
            return;
        }

        const isInitialTransition =
            transitionContext
            && enumEquals(
                transitionContext?.record?.currentStage,
                'APPLICATION_VERIFICATION'
            )
            && enumEquals(
                transitionContext?.transition?.targetStage,
                'SCHOOL_VISIT'
            );

        if (isInitialTransition) {
            state.currentApplicationId =
                Number(
                    transitionContext.record.applicationId
                );

            state.pendingSchoolVisitTransition =
                transitionContext;

            state.schoolVisitScheduleMode =
                'advance';
        } else {
            if (!state.currentApplicationId) {
                return;
            }

            state.pendingSchoolVisitTransition =
                null;

            state.schoolVisitScheduleMode =
                reschedule
                    ? 'reschedule'
                    : 'schedule';
        }

        const triggerButton =
            reschedule
                ? view.schoolVisitRescheduleButton
                : view.schoolVisitScheduleButton;

        const idleButtonText =
            reschedule
                ? 'Reschedule'
                : 'Schedule Visit';

        clearInlineError(
            view.schoolVisitScheduleError
        );

        view.schoolVisitScheduleForm?.reset();

        if (view.schoolVisitScheduleTitle) {
            view.schoolVisitScheduleTitle.textContent =
                reschedule
                    ? 'Reschedule School Visit'
                    : 'Schedule School Visit';
        }

        if (view.schoolVisitScheduleSubtitle) {
            view.schoolVisitScheduleSubtitle.textContent =
                isInitialTransition
                    ? 'Select the School Visit date and time before continuing from Document Verification.'
                    : reschedule
                        ? 'Choose the new School Visit date and time.'
                        : 'Select the School Visit date and time.';
        }

        if (view.schoolVisitSaveScheduleButton) {
            view.schoolVisitSaveScheduleButton.innerHTML =
                reschedule
                    ? '<i class="bi bi-calendar2-check"></i> Save New Schedule'
                    : isInitialTransition
                        ? '<i class="bi bi-arrow-right-circle"></i> Continue'
                        : '<i class="bi bi-calendar2-check"></i> Save Schedule';
        }

        if (reschedule
                && state.schoolVisit) {
            setCalendarDateTimeValue(
                view.schoolVisitScheduledAtInput,
                state.schoolVisit.scheduledAt
            );

            if (view.schoolVisitScheduleRemarks) {
                view.schoolVisitScheduleRemarks.value =
                    state.schoolVisit.remarks || '';
            }
        }

        openModal(
            view.schoolVisitScheduleModal
        );

        if (triggerButton) {
            setButtonBusy(
                triggerButton,
                false,
                idleButtonText
            );
        }
    }

    function closeSchoolVisitScheduleModal() {
        closeModal(
            view.schoolVisitScheduleModal
        );

        view.schoolVisitScheduleForm?.reset();

        clearCalendarInput(
            view.schoolVisitScheduledAtInput
        );

        clearInlineError(
            view.schoolVisitScheduleError
        );

        state.schoolVisitScheduleMode =
            'schedule';

        state.pendingSchoolVisitTransition =
            null;
    }

    async function submitSchoolVisitSchedule() {
        clearInlineError(
            view.schoolVisitScheduleError
        );

        const scheduledAt =
            trimValue(
                view.schoolVisitScheduledAtInput
            );

        if (!scheduledAt) {
            showInlineError(
                view.schoolVisitScheduleError,
                'Select the School Visit date and time.'
            );
            return;
        }

        const remarks =
            nullIfBlank(
                trimValue(
                    view.schoolVisitScheduleRemarks
                )
            );

        const mode =
            state.schoolVisitScheduleMode;

        const initialTransition =
            mode === 'advance'
                ? state.pendingSchoolVisitTransition
                : null;

        const reschedule =
            mode === 'reschedule';

        setButtonBusy(
            view.schoolVisitSaveScheduleButton,
            true,
            initialTransition
                ? 'Continuing...'
                : reschedule
                    ? 'Rescheduling...'
                    : 'Scheduling...'
        );

        let loaderToken = null;

        try {
            if (typeof showLoader === 'function') {
                loaderToken =
                    showLoader(
                        initialTransition
                            ? 'Scheduling School Visit and updating admission workflow...'
                            : reschedule
                                ? 'Rescheduling School Visit...'
                                : 'Scheduling School Visit...'
                    );
            }

            if (initialTransition) {
                const record =
                    initialTransition.record;

                const transition =
                    initialTransition.transition;

                const transitionResponse =
                    await submitWorkflowTransition(
                        Number(record.applicationId),
                        record.currentStage,
                        transition,
                        scheduledAt,
                        remarks
                    );

                closeSchoolVisitScheduleModal();

                await loadApplications(true);

                if (Number(state.currentApplicationId)
                        === Number(record.applicationId)
                        && state.currentApplication) {
                    applyWorkflowResponseToProfile(
                        transitionResponse
                    );

                    await loadSchoolVisit(
                        Number(record.applicationId)
                    );

                    await loadProfileTransitions(
                        Number(record.applicationId)
                    );
                }

                notifySuccess(
                    'School Visit scheduled and application moved to School Visit successfully.'
                );

                return;
            }

            const payload = {
                scheduledAt,
                remarks
            };

            const endpoint =
                `${API_ROOT}/${encodeURIComponent(
                    state.currentApplicationId
                )}/school-visit/schedule`;

            const response =
                reschedule
                    ? await apiPatchJson(
                        endpoint,
                        payload
                    )
                    : await apiPost(
                        endpoint,
                        payload
                    );

            const schoolVisit =
                unwrapResponseData(response);

            state.schoolVisit =
                schoolVisit || null;

            renderSchoolVisit(
                schoolVisit
            );

            closeSchoolVisitScheduleModal();

            await loadApplications(true);

            await loadProfileTransitions(
                state.currentApplicationId
            );

            if (
                typeof window.erpCancelPendingDataSync
                === 'function'
            ) {
                window.erpCancelPendingDataSync();
            }

            notifySuccess(
                reschedule
                    ? 'School Visit rescheduled successfully.'
                    : 'School Visit scheduled successfully.'
            );
        } catch (error) {
            showInlineError(
                view.schoolVisitScheduleError,
                readErrorMessage(
                    error,
                    initialTransition
                        ? 'The application could not be moved to School Visit.'
                        : reschedule
                            ? 'School Visit could not be rescheduled.'
                            : 'School Visit could not be scheduled.'
                )
            );
        } finally {
            if (loaderToken
                    && typeof hideLoader === 'function') {
                hideLoader(loaderToken);
            }

            setButtonBusy(
                view.schoolVisitSaveScheduleButton,
                false,
                initialTransition
                    ? 'Continue'
                    : reschedule
                        ? 'Save New Schedule'
                        : 'Save Schedule'
            );
        }
    }


    async function openSchoolVisitCompleteModal() {
        if (!state.currentApplicationId
                || !state.schoolVisit
                || !view.schoolVisitCompleteModal) {
            return;
        }

        let loaderToken = null;

        setButtonBusy(
            view.schoolVisitCompleteButton,
            true,
            'Loading...'
        );

        try {
            if (typeof showLoader === 'function') {
                loaderToken =
                    showLoader(
                        'Loading Entrance Test preparation...'
                    );
            }

            /*
             * Re-check the current application/stage immediately before
             * opening the modal. This prevents a stale School Visit button
             * from submitting after another action has already advanced the
             * application.
             */
            const [
                latestApplicationResponse,
                latestSchoolVisitResponse
            ] = await Promise.all([
                apiGet(
                    `${API_ROOT}/${encodeURIComponent(
                        state.currentApplicationId
                    )}`
                ),
                apiGet(
                    `${API_ROOT}/${encodeURIComponent(
                        state.currentApplicationId
                    )}/school-visit`
                )
            ]);

            const latestApplication =
                unwrapResponseData(
                    latestApplicationResponse
                ) || {};

            const latestSchoolVisit =
                unwrapResponseData(
                    latestSchoolVisitResponse
                ) || null;

            state.currentApplication = {
                ...state.currentApplication,
                ...latestApplication
            };

            state.schoolVisit =
                latestSchoolVisit;

            renderSchoolVisit(
                latestSchoolVisit
            );

            if (!enumEquals(
                    latestApplication.currentStage,
                    'SCHOOL_VISIT'
            )) {
                closeSchoolVisitCompleteModal();

                await loadApplications(true);

                await loadProfileTransitions(
                    state.currentApplicationId
                );

                notifyError(
                    'This application has already moved out of the School Visit stage. The page has been updated.'
                );

                return;
            }

            if (latestSchoolVisit?.canComplete !== true) {
                notifyError(
                    'Proceed to Entrance Test is not currently available for this application.'
                );
                return;
            }

            view.schoolVisitCompleteForm?.reset();

            clearCalendarInput(
                view.schoolVisitVisitedAtInput
            );

            clearInlineError(
                view.schoolVisitCompleteError
            );

            await loadSchoolVisitEmployees();

            if (view.schoolVisitVisitedAtInput) {
                setCalendarDateTimeValue(
                    view.schoolVisitVisitedAtInput,
                    new Date()
                );
            }

            if (view.schoolVisitCompleteRemarks) {
                view.schoolVisitCompleteRemarks.value =
                    state.schoolVisit.remarks || '';
            }

            openModal(
                view.schoolVisitCompleteModal
            );
        } catch (error) {
            notifyError(
                readErrorMessage(
                    error,
                    'Eligible employees could not be loaded.'
                )
            );
        } finally {
            if (loaderToken
                    && typeof hideLoader === 'function') {
                hideLoader(loaderToken);
            }

            setButtonBusy(
                view.schoolVisitCompleteButton,
                false,
                'Proceed to Entrance Test'
            );
        }
    }

    function closeSchoolVisitCompleteModal() {
        closeModal(
            view.schoolVisitCompleteModal
        );

        view.schoolVisitCompleteForm?.reset();

        clearInlineError(
            view.schoolVisitCompleteError
        );
    }

    async function synchronizeCurrentApplicationAfterMutation(
            source,
            applicationId
    ) {
        const id =
            Number(
                applicationId
                || state.currentApplicationId
            );

        if (!Number.isInteger(id)
                || id <= 0) {
            return false;
        }

        let synchronized = false;

        if (
            typeof window.erpFlushDataSync
            === 'function'
        ) {
            synchronized =
                await window.erpFlushDataSync({
                    source,
                    applicationId: id
                });
        }

        /*
         * Never allow a successful backend mutation to leave the profile
         * stale merely because the global registry was unavailable or did
         * not handle this active view.
         */
        if (synchronized !== true) {
            synchronized =
                await openApplication(
                    id,
                    {
                        preservePosition: true,
                        silent: true
                    }
                );
        }

        if (synchronized !== true) {
            return false;
        }

        /*
         * Verify the state expected from important workflow mutations instead
         * of trusting a generic "handled" flag.
         */
        if (
            source === 'school-visit-attendance'
        ) {
            return (
                enumEquals(
                    state.currentApplication?.currentStage,
                    'ENTRANCE_TEST'
                )
                && Boolean(
                    state.entranceTest
                )
            );
        }

        if (
            source === 'entrance-test-result'
            || source === 'entrance-test-waitlist-result'
        ) {
            return Boolean(
                state.entranceTest
            );
        }

        return true;
    }

    async function submitSchoolVisitCompletion() {
        clearInlineError(
            view.schoolVisitCompleteError
        );

        const employeeId =
            Number(
                view.schoolVisitCompleteEmployeeSelect?.value
            );

        const studentAttendance =
            view.schoolVisitStudentAttended?.value
            || '';

        const parentAttendance =
            view.schoolVisitParentAttended?.value
            || '';

        if (!Number.isInteger(employeeId)
                || employeeId <= 0) {
            showInlineError(
                view.schoolVisitCompleteError,
                'Select the responsible employee.'
            );
            return;
        }

        if (!studentAttendance) {
            showInlineError(
                view.schoolVisitCompleteError,
                'Select the student attendance.'
            );
            return;
        }

        if (!parentAttendance) {
            showInlineError(
                view.schoolVisitCompleteError,
                'Select the parent or guardian attendance.'
            );
            return;
        }

        const payload = {
            employeeId,
            visitedAt:
                nullIfBlank(
                    trimValue(
                        view.schoolVisitVisitedAtInput
                    )
                ),
            studentAttended:
                studentAttendance === 'true',
            parentAttended:
                parentAttendance === 'true',
            remarks:
                nullIfBlank(
                    trimValue(
                        view.schoolVisitCompleteRemarks
                    )
                )
        };

        setButtonBusy(
            view.schoolVisitConfirmCompleteButton,
            true,
            'Proceeding...'
        );

        let loaderToken = null;

        try {
            if (typeof showLoader === 'function') {
                loaderToken =
                    showLoader(
                        'Recording School Visit attendance and starting Entrance Test...'
                    );
            }

            /*
             * Reuse the existing School Visit endpoint.
             * It now records employee + attendance and sets the School Visit
             * status to ATTENDED. It does not mark the visit COMPLETED.
             */
            const attendanceResponse =
                await apiPatchJson(
                    `${API_ROOT}/${encodeURIComponent(
                        state.currentApplicationId
                    )}/school-visit/complete`,
                    payload
                );

            const schoolVisit =
                unwrapResponseData(
                    attendanceResponse
                );

            state.schoolVisit =
                schoolVisit || null;

            renderSchoolVisit(
                schoolVisit
            );

            /*
             * The visit-day action is one user operation:
             * after attendance is recorded, immediately advance the workflow
             * from SCHOOL_VISIT to ENTRANCE_TEST.
             */
            const transitions =
                await fetchAvailableTransitions(
                    state.currentApplicationId
                );

            const entranceTestTransition =
                transitions.find(
                    transition =>
                        enumEquals(
                            transition?.action,
                            'ADVANCE'
                        )
                        && enumEquals(
                            transition?.targetStage,
                            'ENTRANCE_TEST'
                        )
                );

            if (!entranceTestTransition) {
                throw new Error(
                    'Attendance was recorded, but the Entrance Test transition is not currently available.'
                );
            }

            const transitionResponse =
                await submitWorkflowTransition(
                    state.currentApplicationId,
                    'SCHOOL_VISIT',
                    entranceTestTransition
                );

            const applicationId =
                Number(
                    state.currentApplicationId
                );

            closeSchoolVisitCompleteModal();

            /*
             * Keep the global loader + blocking overlay active while the
             * application is synchronized into Entrance Test. This prevents
             * the attended School Visit profile from becoming visible and
             * interactive before the new stage is ready.
             *
             * Attendance + workflow transition are one logical user action.
             * Both PATCH events have already been coalesced while the loader
             * was active. Consume them with one final synchronization.
             */
            const refreshed =
                await synchronizeCurrentApplicationAfterMutation(
                    'school-visit-attendance',
                    applicationId
                );

            if (!refreshed) {
                throw new Error(
                    'Attendance was recorded and the application moved to Entrance Test, but the refreshed Entrance Test profile could not be loaded.'
                );
            }

            notifySuccess(
                'Attendance recorded and application moved to Entrance Test successfully.'
            );
        } catch (error) {
            /*
             * The attendance request may already have committed before a
             * transition failure. Synchronize the UI silently so the browser
             * always reflects the real backend state without manual refresh.
             */
            await loadApplications(true);

            if (state.currentApplicationId) {
                try {
                    await openApplication(
                        Number(
                            state.currentApplicationId
                        ),
                        {
                            preservePosition: true,
                            silent: true
                        }
                    );
                } catch (refreshError) {
                    console.error(
                        'School Visit recovery refresh failed:',
                        refreshError
                    );
                }
            }

            showInlineError(
                view.schoolVisitCompleteError,
                readErrorMessage(
                    error,
                    'The application could not proceed to Entrance Test.'
                )
            );
        } finally {
            if (loaderToken
                    && typeof hideLoader === 'function') {
                hideLoader(loaderToken);
            }

            setButtonBusy(
                view.schoolVisitConfirmCompleteButton,
                false,
                'Proceed to Entrance Test'
            );
        }
    }

    function setMinimumFutureDateTime(
        input
    ) {
        if (!input) {
            return;
        }

        const minimum =
            new Date(
                Date.now()
                + (5 * 60 * 1000)
            );

        input.min =
            toDateTimeLocalValue(
                minimum
            );
    }

    function toDateTimeLocalValue(
        value
    ) {
        if (!value) {
            return '';
        }

        const date =
            value instanceof Date
                ? value
                : new Date(value);

        if (Number.isNaN(date.getTime())) {
            const raw =
                String(value);

            return raw.length >= 16
                ? raw.slice(0, 16)
                : raw;
        }

        const pad =
            number =>
                String(number).padStart(2, '0');

        return `${
            date.getFullYear()
        }-${
            pad(date.getMonth() + 1)
        }-${
            pad(date.getDate())
        }T${
            pad(date.getHours())
        }:${
            pad(date.getMinutes())
        }`;
    }

    /**
     * Uses the secured document endpoint for an applicant photo.
     *
     * @param {Object} application
     */
    function renderApplicationPhoto(
        application
    ) {
        if (!view.profilePhoto
                || !view.profilePhotoPlaceholder) {
            return;
        }

        const documents =
            Array.isArray(application.documents)
                ? application.documents
                : [];

        const photo =
            documents.find(document =>
                document
                && document.active !== false
                && document.current !== false
                && String(document.documentType)
                    .toUpperCase() === 'PHOTO'
                && String(document.verificationStatus)
                    .toUpperCase() !== 'SUPERSEDED'
            );

        if (!photo
                || !photo.documentId
                || !application.applicationId) {
            hideElement(view.profilePhoto);
            showElement(
                view.profilePhotoPlaceholder
            );
            view.profilePhoto.removeAttribute('src');
            return;
        }

        const source =
            buildDocumentUrl(
                application.applicationId,
                photo.documentId,
                'view'
            );

        if (
            state.failedDocumentViewUrls.has(
                source
            )
        ) {
            view.profilePhoto.removeAttribute(
                'src'
            );
            hideElement(view.profilePhoto);
            showElement(
                view.profilePhotoPlaceholder
            );
            return;
        }

        view.profilePhoto.onload = () => {
            state.failedDocumentViewUrls.delete(
                source
            );
            hideElement(
                view.profilePhotoPlaceholder
            );
            showElement(view.profilePhoto);
        };

        view.profilePhoto.onerror = () => {
            state.failedDocumentViewUrls.add(
                source
            );
            view.profilePhoto.removeAttribute('src');
            hideElement(view.profilePhoto);
            showElement(
                view.profilePhotoPlaceholder
            );
        };

        view.profilePhoto.src = source;
    }

    /**
     * Renders secure Application document cards.
     *
     * @param {Array<Object>} documents
     */
    function renderDocuments(
        documents
    ) {
        if (!view.documentsContainer) {
            return;
        }

        view.documentsContainer.replaceChildren();

        const activeDocuments =
            documents.filter(record =>
                record && record.active !== false
            );

        setCount(
            view.documentCount,
            activeDocuments.length
        );

        activeDocuments.forEach(record => {
            view.documentsContainer.appendChild(
                createDocumentCard(record)
            );
        });
    }

    /**
     * Creates one Employee/Student-style document card.
     *
     * @param {Object} record
     * @returns {HTMLElement}
     */
    function createDocumentCard(
        record
    ) {
        const card =
            window.document.createElement('article');

        card.className = 'emp-document-card';

        const header =
            window.document.createElement('div');

        header.className =
            'emp-document-card-header';

        const heading =
            window.document.createElement('div');

        const name =
            window.document.createElement('div');

        name.className = 'emp-document-name';
        name.textContent =
            displayValue(
                record.originalFileName
                || formatEnum(record.documentType)
            );

        const type =
            window.document.createElement('div');

        type.className = 'emp-document-type';
        type.textContent =
            formatEnum(record.documentType);

        heading.append(name, type);

        const badge =
            createStatusBadge(
                record.verificationStatus
            );

        header.append(heading, badge);

        const meta =
            window.document.createElement('div');

        meta.className = 'emp-document-meta';
        meta.textContent =
            [
                formatFileSize(record.fileSize),
                formatDateTime(record.uploadedAt),
                formatEnum(record.submissionSource)
            ]
                .filter(Boolean)
                .join(' • ');

        card.append(header, meta);

        const remarks =
            [
                record.rejectionReason
                    ? `Reason: ${record.rejectionReason}`
                    : '',
                record.publicRemarks
                    ? `Applicant note: ${record.publicRemarks}`
                    : '',
                record.reuploadDeadline
                    ? `Re-upload deadline: ${
                        formatDateTime(
                            record.reuploadDeadline
                        )
                    }`
                    : ''
            ]
                .filter(Boolean);

        if (remarks.length > 0) {
            const remarksNode =
                window.document.createElement('div');

            remarksNode.className =
                'emp-document-meta';

            remarksNode.textContent =
                remarks.join(' | ');

            card.appendChild(remarksNode);
        }

        const actions =
            window.document.createElement('div');

        actions.className =
            'emp-document-actions';

        actions.appendChild(
            createButton(
                'View',
                'bi-eye',
                'btn-secondary btn-sm emp-document-view-btn',
                () => openDocument(
                    record.documentId,
                    'view'
                )
            )
        );

        actions.appendChild(
            createButton(
                'Download',
                'bi-download',
                'btn-secondary btn-sm',
                () => openDocument(
                    record.documentId,
                    'download'
                )
            )
        );

        const canReview =
            record.current !== false
            && record.active !== false
            && String(record.verificationStatus)
                .toUpperCase() !== 'SUPERSEDED';

        const reviewButton =
            createButton(
                'Review',
                'bi-clipboard-check',
                'btn-primary btn-sm',
                () => openDocumentReviewModal(record)
            );

        reviewButton.disabled = !canReview;
        actions.appendChild(reviewButton);

        card.appendChild(actions);

        return card;
    }

    /**
     * Renders additional-document requests.
     *
     * @param {Array<Object>} requests
     */
    function renderDocumentRequests(
        requests
    ) {
        if (!view.documentRequestsContainer) {
            return;
        }

        view.documentRequestsContainer
            .replaceChildren();

        setCount(
            view.documentRequestCount,
            requests.length
        );

        requests.forEach(request => {
            const card =
                window.document.createElement('article');

            card.className = 'emp-record-card';

            const title =
                window.document.createElement('h4');

            title.textContent =
                displayValue(
                    request.requestedDocumentName
                    || formatEnum(
                        request.requestedDocumentType
                    )
                );

            card.appendChild(title);

            appendRecordItem(
                card,
                'Type',
                formatEnum(
                    request.requestedDocumentType
                )
            );

            appendRecordItem(
                card,
                'Reason',
                request.requestReason
            );

            appendRecordItem(
                card,
                'Requested',
                formatDateTime(request.requestedAt)
            );

            appendRecordItem(
                card,
                'Deadline',
                formatDateTime(request.uploadDeadline)
            );

            appendRecordItem(
                card,
                'Request Status',
                formatEnum(request.requestStatus)
            );

            appendRecordItem(
                card,
                'Email Status',
                formatEnum(request.emailStatus)
            );

            appendRecordItem(
                card,
                'Uploaded Document',
                request.uploadedDocumentName
            );

            appendRecordItem(
                card,
                'Cancellation Reason',
                request.cancellationReason
            );

            if (String(request.requestStatus)
                    .toUpperCase() === 'PENDING'
                    && request.active !== false) {
                const actions =
                    window.document.createElement('div');

                actions.className =
                    'emp-document-actions';

                actions.appendChild(
                    createButton(
                        'Cancel Request',
                        'bi-x-circle',
                        'btn-danger btn-sm',
                        () => openCancelRequestModal(
                            request
                        )
                    )
                );

                card.appendChild(actions);
            }

            view.documentRequestsContainer
                .appendChild(card);
        });
    }

    /**
     * Renders application workflow history.
     *
     * @param {Array<Object>} historyItems
     */
    function renderHistory(
        historyItems
    ) {
        if (!view.historyContainer) {
            return;
        }

        view.historyContainer.replaceChildren();

        setCount(
            view.historyCount,
            historyItems.length
        );

        historyItems.forEach(history => {
            const card =
                window.document.createElement('article');

            card.className = 'emp-record-card';

            const title =
                window.document.createElement('h4');

            title.textContent =
                formatEnum(history.stage)
                || 'Application Update';

            card.appendChild(title);

            appendRecordItem(
                card,
                'Change',
                buildHistoryTransition(history)
            );

            appendRecordItem(
                card,
                'Changed At',
                formatDateTime(history.changedAt)
            );

            appendRecordItem(
                card,
                'Public Remarks',
                history.publicRemarks
            );

            appendRecordItem(
                card,
                'Internal Remarks',
                history.internalRemarks
            );

            appendRecordItem(
                card,
                'Source',
                formatEnum(history.transitionSource)
            );

            appendRecordItem(
                card,
                'Email',
                buildHistoryEmail(history)
            );

            view.historyContainer
                .appendChild(card);
        });
    }

    /**
     * Opens a secure document view or download endpoint.
     *
     * @param {number|string} documentId
     * @param {'view'|'download'} action
     */
    function openDocument(
        documentId,
        action
    ) {
        if (!state.currentApplicationId
                || !documentId) {
            notifyError(
                'The selected document is invalid.'
            );
            return;
        }

        const target =
            buildDocumentUrl(
                state.currentApplicationId,
                documentId,
                action
            );

        window.open(
            target,
            action === 'view'
                ? '_blank'
                : '_self',
            action === 'view'
                ? 'noopener,noreferrer'
                : undefined
        );
    }

    /**
     * Opens the document-review modal.
     *
     * @param {Object} documentRecord
     */
    function openDocumentReviewModal(
        documentRecord
    ) {
        if (!view.reviewModal
                || !documentRecord
                || !documentRecord.documentId) {
            return;
        }

        resetDocumentReviewForm();

        view.reviewDocumentId.value =
            String(documentRecord.documentId);

        view.reviewSubtitle.textContent =
            displayValue(
                documentRecord.originalFileName
                || formatEnum(
                    documentRecord.documentType
                )
            );

        view.reviewPublicRemarks.value =
            documentRecord.publicRemarks || '';

        view.reviewInternalRemarks.value =
            documentRecord.internalRemarks || '';

        openModal(view.reviewModal);
    }

    /**
     * Shows only the fields required for the selected review decision.
     */
    function updateReviewDecisionFields() {
        const decision =
            view.reviewDecision
                ? view.reviewDecision.value
                : '';

        toggleElement(
            view.rejectionReasonGroup,
            decision === 'REJECT'
        );

        toggleElement(
            view.reuploadReasonGroup,
            decision === 'REQUEST_REUPLOAD'
        );

        toggleElement(
            view.reuploadDeadlineGroup,
            decision === 'REQUEST_REUPLOAD'
        );
    }

    /**
     * Submits a Verify, Reject or Request Re-upload decision.
     */
    async function submitDocumentReview() {
        clearInlineError(view.reviewError);

        const documentId =
            Number(view.reviewDocumentId?.value);

        const decision =
            view.reviewDecision?.value || '';

        if (!Number.isInteger(documentId)
                || documentId <= 0) {
            showInlineError(
                view.reviewError,
                'The selected document is invalid.'
            );
            return;
        }

        if (!decision) {
            showInlineError(
                view.reviewError,
                'Select a document review decision.'
            );
            return;
        }

        const rejectionReason =
            trimValue(view.rejectionReason);

        const reuploadReason =
            trimValue(view.reuploadReason);

        if (decision === 'REJECT'
                && !rejectionReason) {
            showInlineError(
                view.reviewError,
                'Enter the document rejection reason.'
            );
            return;
        }

        if (decision === 'REQUEST_REUPLOAD'
                && !reuploadReason) {
            showInlineError(
                view.reviewError,
                'Enter the re-upload reason.'
            );
            return;
        }

        const payload = {
            decision,
            publicRemarks:
                nullIfBlank(
                    trimValue(
                        view.reviewPublicRemarks
                    )
                ),
            internalRemarks:
                nullIfBlank(
                    trimValue(
                        view.reviewInternalRemarks
                    )
                ),
            rejectionReason:
                decision === 'REJECT'
                    ? rejectionReason
                    : null,
            reuploadReason:
                decision === 'REQUEST_REUPLOAD'
                    ? reuploadReason
                    : null,
            reuploadDeadline:
                decision === 'REQUEST_REUPLOAD'
                    ? nullIfBlank(
                        view.reuploadDeadline?.value
                    )
                    : null
        };

        setButtonBusy(
            view.submitReviewButton,
            true,
            'Saving...'
        );

        let loaderToken = null;

        try {
            if (typeof showLoader === 'function') {
                loaderToken =
                    showLoader(
                        'Saving Document Review...'
                    );
            }
            const response =
                await apiPatchJson(
                    `${API_ROOT}/${
                        encodeURIComponent(
                            state.currentApplicationId
                        )
                    }/documents/${
                        encodeURIComponent(documentId)
                    }/review`,
                    payload
                );

            const updatedDocument =
                unwrapResponseData(response);

            upsertCurrentDocument(
                updatedDocument
            );

            closeDocumentReviewModal();

            await refreshAfterDocumentMutation(
                state.currentApplicationId
            );

            notifySuccess(
                decision === 'VERIFY'
                    ? 'Document verified successfully.'
                    : decision === 'REJECT'
                        ? 'Document rejected successfully.'
                        : 'Re-upload request created successfully.'
            );

            if (decision === 'REQUEST_REUPLOAD') {
                scheduleDocumentRequestRefresh();
            }
        } catch (error) {
            showInlineError(
                view.reviewError,
                readErrorMessage(
                    error,
                    'Document review could not be saved.'
                )
            );
        } finally {
            if (loaderToken
                    && typeof hideLoader === 'function') {
                hideLoader(loaderToken);
            }

            setButtonBusy(
                view.submitReviewButton,
                false,
                'Save Review'
            );
        }
    }

    /**
     * Opens the additional-document request modal.
     */
    function openAdditionalDocumentModal() {
        if (!state.currentApplicationId
                || !view.requestModal) {
            return;
        }

        resetAdditionalDocumentForm();
        openModal(view.requestModal);
    }

    /**
     * Populates a friendly default document name.
     */
    function fillRequestedDocumentName() {
        if (!view.requestDocumentType
                || !view.requestDocumentName) {
            return;
        }

        if (trimValue(view.requestDocumentName)) {
            return;
        }

        const selectedOption =
            view.requestDocumentType
                .selectedOptions
                ? view.requestDocumentType
                    .selectedOptions[0]
                : null;

        if (selectedOption
                && selectedOption.value) {
            view.requestDocumentName.value =
                selectedOption.textContent.trim();
        }
    }

    /**
     * Creates one additional-document request.
     */
    async function submitAdditionalDocumentRequest() {
        clearInlineError(view.requestError);

        const requestedDocumentType =
            trimValue(view.requestDocumentType);

        const requestedDocumentName =
            trimValue(view.requestDocumentName);

        const requestReason =
            trimValue(view.requestReason);

        if (!requestedDocumentType) {
            showInlineError(
                view.requestError,
                'Select a document type.'
            );
            return;
        }

        const normalizedType =
            requestedDocumentType === 'MEDICAL_RECORD'
                ? 'MEDICAL_REPORT'
                : requestedDocumentType;

        const payload = {
            requestedDocumentType:
                normalizedType,
            requestedDocumentName:
                nullIfBlank(requestedDocumentName),
            requestReason:
                nullIfBlank(requestReason),
            publicRemarks:
                nullIfBlank(
                    trimValue(
                        view.requestPublicRemarks
                    )
                ),
            internalRemarks:
                nullIfBlank(
                    trimValue(
                        view.requestInternalRemarks
                    )
                ),
            uploadDeadline:
                nullIfBlank(
                    view.requestUploadDeadline?.value
                )
        };

        setButtonBusy(
            view.submitRequestButton,
            true,
            'Creating...'
        );

        let loaderToken = null;

        try {
            if (typeof showLoader === 'function') {
                loaderToken =
                    showLoader(
                        'Creating Document Request...'
                    );
            }
            const response =
                await apiPost(
                    `${API_ROOT}/${
                        encodeURIComponent(
                            state.currentApplicationId
                        )
                    }/documents/requests`,
                    payload
                );

            const createdRequest =
                unwrapResponseData(response);

            upsertCurrentDocumentRequest(
                createdRequest
            );

            updateVisibleDocumentStatus(
                'ADDITIONAL_DOCUMENTS_REQUIRED'
            );

            closeAdditionalDocumentModal();

            await refreshAfterDocumentMutation(
                state.currentApplicationId
            );

            notifySuccess(
                'Document request created. Email status will update '
                + 'automatically without reloading the profile.'
            );

            scheduleDocumentRequestRefresh();
        } catch (error) {
            showInlineError(
                view.requestError,
                readErrorMessage(
                    error,
                    'The document request could not be created.'
                )
            );
        } finally {
            if (loaderToken
                    && typeof hideLoader === 'function') {
                hideLoader(loaderToken);
            }

            setButtonBusy(
                view.submitRequestButton,
                false,
                'Create & Send Request'
            );
        }
    }

    /**
     * Opens the pending-request cancellation modal.
     *
     * @param {Object} request
     */
    function openCancelRequestModal(
        request
    ) {
        if (!request
                || !request.requestId
                || !view.cancelRequestModal) {
            return;
        }

        resetCancelRequestForm();

        view.cancelRequestId.value =
            String(request.requestId);

        view.cancelRequestSubtitle.textContent =
            displayValue(
                request.requestedDocumentName
            );

        openModal(view.cancelRequestModal);
    }

    /**
     * Cancels one pending document request.
     */
    async function submitRequestCancellation() {
        clearInlineError(
            view.cancellationError
        );

        const requestId =
            Number(view.cancelRequestId?.value);

        const cancellationReason =
            trimValue(view.cancellationReason);

        if (!Number.isInteger(requestId)
                || requestId <= 0) {
            showInlineError(
                view.cancellationError,
                'The selected request is invalid.'
            );
            return;
        }

        if (!cancellationReason) {
            showInlineError(
                view.cancellationError,
                'Enter the cancellation reason.'
            );
            return;
        }

        setButtonBusy(
            view.confirmCancellationButton,
            true,
            'Cancelling...'
        );

        let loaderToken = null;

        try {
            if (typeof showLoader === 'function') {
                loaderToken =
                    showLoader(
                        'Cancelling Document Request...'
                    );
            }
            const response =
                await apiPatchJson(
                    `${API_ROOT}/${
                        encodeURIComponent(
                            state.currentApplicationId
                        )
                    }/documents/requests/${
                        encodeURIComponent(requestId)
                    }/cancel`,
                    {
                        cancellationReason
                    }
                );

            const cancelledRequest =
                unwrapResponseData(response);

            upsertCurrentDocumentRequest(
                cancelledRequest
            );

            closeCancelRequestModal();

            await refreshAfterDocumentMutation(
                state.currentApplicationId
            );

            notifySuccess(
                'Document request cancelled successfully.'
            );
        } catch (error) {
            showInlineError(
                view.cancellationError,
                readErrorMessage(
                    error,
                    'The document request could not be cancelled.'
                )
            );
        } finally {
            if (loaderToken
                    && typeof hideLoader === 'function') {
                hideLoader(loaderToken);
            }

            setButtonBusy(
                view.confirmCancellationButton,
                false,
                'Cancel Request'
            );
        }
    }

    /**
     * Extracts the payload from the standard ApiResponse wrapper.
     *
     * @param {*} response
     * @returns {*}
     */
    function unwrapResponseData(response) {
        if (response
                && Object.prototype.hasOwnProperty.call(
                    response,
                    'data'
                )) {
            return response.data;
        }

        return response || null;
    }

    /**
     * Replaces one document in the current profile without reopening the
     * complete Application view.
     *
     * @param {Object|null} updatedDocument
     */
    function upsertCurrentDocument(
        updatedDocument
    ) {
        if (!updatedDocument
                || !updatedDocument.documentId
                || !state.currentApplication) {
            return;
        }

        const documents =
            Array.isArray(
                state.currentApplication.documents
            )
                ? [
                    ...state.currentApplication.documents
                ]
                : [];

        const documentId =
            Number(updatedDocument.documentId);

        const existingIndex =
            documents.findIndex(document =>
                Number(document?.documentId)
                    === documentId
            );

        if (existingIndex >= 0) {
            documents[existingIndex] = {
                ...documents[existingIndex],
                ...updatedDocument
            };
        } else {
            documents.unshift(updatedDocument);
        }

        state.currentApplication.documents =
            documents;

        renderDocuments(documents);
        synchronizeVisibleDocumentStatus(
            documents
        );
    }

    /**
     * Adds or replaces one document request without reopening the complete
     * Application profile.
     *
     * @param {Object|null} updatedRequest
     */
    function upsertCurrentDocumentRequest(
        updatedRequest
    ) {
        if (!updatedRequest
                || !updatedRequest.requestId
                || !state.currentApplication) {
            return;
        }

        const requests =
            Array.isArray(
                state.currentApplication.documentRequests
            )
                ? [
                    ...state.currentApplication.documentRequests
                ]
                : [];

        const requestId =
            Number(updatedRequest.requestId);

        const existingIndex =
            requests.findIndex(request =>
                Number(request?.requestId)
                    === requestId
            );

        if (existingIndex >= 0) {
            requests[existingIndex] = {
                ...requests[existingIndex],
                ...updatedRequest
            };
        } else {
            requests.unshift(updatedRequest);
        }

        state.currentApplication.documentRequests =
            requests;

        renderDocumentRequests(requests);
    }

    async function refreshAfterDocumentMutation(
        applicationId
    ) {
        const validApplicationId =
            Number(applicationId);

        if (!Number.isInteger(validApplicationId)
                || validApplicationId <= 0) {
            return;
        }

        await synchronizeApplicationDocumentState(
            validApplicationId,
            true
        );

        /*
         * Keep the list row synchronized too. This is silent and does not
         * reset the currently open profile or scroll position.
         */
        await loadApplications(true);

        /*
         * Document mutations already synchronized their dedicated profile
         * sections above. Prevent the mutation event from triggering a second
         * full Application-profile synchronization afterward.
         */
        if (
            typeof window.erpCancelPendingDataSync
            === 'function'
        ) {
            window.erpCancelPendingDataSync();
        }
    }

    /**
     * Builds a stable signature so background polling redraws the profile only
     * when document/request/workflow state actually changed.
     */
    function buildDocumentSyncSignature(
        application,
        documents,
        requests,
        transitions
    ) {
        const documentPart =
            (Array.isArray(documents)
                ? documents
                : [])
                .map(document => [
                    document?.documentId,
                    document?.verificationStatus,
                    document?.current,
                    document?.active,
                    document?.uploadedAt
                ].join(':'))
                .join('|');

        const requestPart =
            (Array.isArray(requests)
                ? requests
                : [])
                .map(request => [
                    request?.requestId,
                    request?.requestStatus,
                    request?.emailStatus,
                    request?.uploadedAt
                ].join(':'))
                .join('|');

        const transitionPart =
            (Array.isArray(transitions)
                ? transitions
                : [])
                .map(transition => [
                    transition?.action,
                    transition?.targetStage
                ].join(':'))
                .join('|');

        return [
            application?.documentStatus,
            application?.verificationStatus,
            documentPart,
            requestPart,
            transitionPart
        ].join('||');
    }

    /**
     * Reloads the authoritative document-related application state without
     * reopening the profile or changing the user's scroll position.
     *
     * Parent uploads are independent browser actions, therefore the profile
     * also calls this method periodically while it remains open. The
     * background interval is intentionally conservative because document
     * endpoints may involve database/storage work.
     */
    async function synchronizeApplicationDocumentState(
        applicationId,
        force = false
    ) {
        if (
            !view?.root
            || !document.body.contains(
                view.root
            )
        ) {
            stopDocumentAutoSync();
            return false;
        }

        const expectedApplicationId =
            Number(applicationId);

        if (!Number.isInteger(expectedApplicationId)
                || expectedApplicationId <= 0
                || state.documentSyncBusy) {
            return;
        }

        if (Number(state.currentApplicationId)
                !== expectedApplicationId
                || !state.currentApplication) {
            return;
        }

        state.documentSyncBusy = true;

        try {
            /*
             * Normal background checks fetch only documents + requests.
             * The application/details/transitions endpoints are much heavier
             * and are fetched only when those document states actually change.
             */
            const [
                documentsResponse,
                requestsResponse
            ] = await Promise.all([
                apiGet(
                    `${API_ROOT}/${encodeURIComponent(
                        expectedApplicationId
                    )}/documents`
                ),
                apiGet(
                    `${API_ROOT}/${encodeURIComponent(
                        expectedApplicationId
                    )}/documents/requests`
                )
            ]);

            if (Number(state.currentApplicationId)
                    !== expectedApplicationId
                    || !state.currentApplication) {
                return;
            }

            const documents =
                unwrapResponseData(
                    documentsResponse
                );

            const requests =
                unwrapResponseData(
                    requestsResponse
                );

            const normalizedDocuments =
                Array.isArray(documents)
                    ? documents
                    : [];

            const normalizedRequests =
                Array.isArray(requests)
                    ? requests
                    : [];

            const lightweightSignature = [
                normalizedDocuments
                    .map(document => [
                        document?.documentId,
                        document?.verificationStatus,
                        document?.current,
                        document?.active,
                        document?.uploadedAt
                    ].join(':'))
                    .join('|'),
                normalizedRequests
                    .map(request => [
                        request?.requestId,
                        request?.requestStatus,
                        request?.emailStatus,
                        request?.uploadedAt
                    ].join(':'))
                    .join('|')
            ].join('||');

            if (!force
                    && lightweightSignature
                    === state.documentSyncSignature) {
                return;
            }

            state.documentSyncSignature =
                lightweightSignature;

            /*
             * Something changed (or an admin action forced synchronization).
             * Now fetch the authoritative application state and transitions.
             */
            const [
                applicationResponse,
                transitions
            ] = await Promise.all([
                apiGet(
                    `${API_ROOT}/${encodeURIComponent(
                        expectedApplicationId
                    )}`
                ),
                fetchAvailableTransitions(
                    expectedApplicationId
                )
            ]);

            if (Number(state.currentApplicationId)
                    !== expectedApplicationId
                    || !state.currentApplication) {
                return;
            }

            const latestApplication =
                unwrapResponseData(
                    applicationResponse
                ) || {};

            /*
             * Always trust the backend for the workflow stage/status.
             * Document actions must never infer or rewrite currentStage in JS.
             */
            state.currentApplication = {
                ...state.currentApplication,
                ...latestApplication,
                documents:
                    normalizedDocuments,
                documentRequests:
                    normalizedRequests
            };

            renderDocuments(
                normalizedDocuments
            );

            renderDocumentRequests(
                normalizedRequests
            );

            renderApplicationPhoto(
                state.currentApplication
            );

            updateVisibleDocumentStatus(
                latestApplication.documentStatus
                || 'PENDING'
            );

            setText(
                'view-verificationStatus',
                formatEnum(
                    latestApplication.verificationStatus
                )
            );

            setText(
                'view-currentStage',
                formatEnum(
                    latestApplication.currentStage
                )
            );

            setText(
                'view-documentStatus',
                formatEnum(
                    latestApplication.documentStatus
                )
            );

            /*
             * Do not derive next stage/action locally. They come exclusively
             * from the backend transition endpoint below.
             */
            state.profileTransitions =
                Array.isArray(transitions)
                    ? transitions
                    : [];

            const primaryTransition =
                state.profileTransitions.find(
                    transition =>
                        enumEquals(
                            transition?.action,
                            'ADVANCE'
                        )
                ) || null;

            renderProfileNextAction(
                primaryTransition
            );

            /*
             * If the current workflow stage changed, refresh stage-specific
             * panels too so stale School Visit actions disappear immediately.
             */
            if (!enumEquals(
                    latestApplication.currentStage,
                    'SCHOOL_VISIT'
            )) {
                if (view.schoolVisitCompleteModal
                        && !view.schoolVisitCompleteModal.hidden) {
                    closeSchoolVisitCompleteModal();
                }

                if (state.schoolVisit) {
                    state.schoolVisit.currentStage =
                        latestApplication.currentStage;

                    renderSchoolVisit(
                        state.schoolVisit
                    );
                }
            }

            /*
             * Do not reload the whole Applications table during background
             * document polling. The open profile has already been updated.
             */
        } catch (error) {
            console.warn(
                'Automatic document/workflow synchronization failed.',
                error
            );
        } finally {
            state.documentSyncBusy = false;
        }
    }

    /**
     * While an application profile is open, poll quietly for parent uploads
     * and backend verification/request-state changes.
     */
    function buildCurrentDocumentSyncSignature() {
        const application =
            state.currentApplication || {};

        const documents =
            Array.isArray(application.documents)
                ? application.documents
                : [];

        const requests =
            Array.isArray(application.documentRequests)
                ? application.documentRequests
                : [];

        return [
            documents
                .map(document => [
                    document?.documentId,
                    document?.verificationStatus,
                    document?.current,
                    document?.active,
                    document?.uploadedAt
                ].join(':'))
                .join('|'),
            requests
                .map(request => [
                    request?.requestId,
                    request?.requestStatus,
                    request?.emailStatus,
                    request?.uploadedAt
                ].join(':'))
                .join('|')
        ].join('||');
    }

    function startDocumentAutoSync(
        applicationId
    ) {
        const expectedApplicationId =
            Number(applicationId);

        if (!Number.isInteger(expectedApplicationId)
                || expectedApplicationId <= 0) {
            return;
        }

        /*
         * openApplication() is also reused for silent global synchronization.
         * If this same Application is already being polled, keep the existing
         * timer instead of immediately launching another document refresh.
         */
        if (
            state.documentSyncTimer
            && Number(
                state.documentSyncApplicationId
            ) === expectedApplicationId
        ) {
            return;
        }

        stopDocumentAutoSync();

        state.documentSyncApplicationId =
            expectedApplicationId;

        /*
         * openApplication() already loaded the complete Application including
         * current documents/requests. Do not immediately repeat those GETs.
         * The first lightweight external-document check happens on the timer.
         */
        state.documentSyncSignature =
            buildCurrentDocumentSyncSignature();

        state.documentSyncTimer =
            window.setInterval(
                () => {
                    if (
                        !view?.root
                        || !document.body.contains(
                            view.root
                        )
                    ) {
                        stopDocumentAutoSync();
                        return;
                    }

                    if (document.hidden) {
                        return;
                    }

                    if (Number(state.currentApplicationId)
                            !== expectedApplicationId) {
                        stopDocumentAutoSync();
                        return;
                    }

                    void synchronizeApplicationDocumentState(
                        expectedApplicationId,
                        false
                    );
                },
                10000
            );
    }

    function stopDocumentAutoSync() {
        if (state.documentSyncTimer) {
            window.clearInterval(
                state.documentSyncTimer
            );
        }

        state.documentSyncTimer = null;
        state.documentSyncBusy = false;
        state.documentSyncApplicationId = null;
    }

    /**
     * Silently refreshes only the document-request section. This is used to
     * pick up the after-commit email result without showing the global loader
     * or resetting the current scroll position.
     */
    async function refreshDocumentRequestsSilently(
        applicationId
    ) {
        const expectedApplicationId =
            Number(applicationId);

        if (!Number.isInteger(expectedApplicationId)
                || expectedApplicationId <= 0) {
            return;
        }

        try {
            const response =
                await apiGet(
                    `${API_ROOT}/${
                        encodeURIComponent(
                            expectedApplicationId
                        )
                    }/documents/requests`
                );

            if (Number(state.currentApplicationId)
                    !== expectedApplicationId
                    || !state.currentApplication) {
                return;
            }

            const requests =
                unwrapResponseData(response);

            state.currentApplication.documentRequests =
                Array.isArray(requests)
                    ? requests
                    : [];

            renderDocumentRequests(
                state.currentApplication.documentRequests
            );
        } catch (error) {
            console.warn(
                'Document-request status refresh failed.',
                error
            );
        }
    }

    /**
     * Refreshes email status after the transaction listener has had time to
     * send the message. These are section-only GET requests.
     */
    function scheduleDocumentRequestRefresh() {
        const applicationId =
            Number(state.currentApplicationId);

        if (!Number.isInteger(applicationId)
                || applicationId <= 0) {
            return;
        }

        window.setTimeout(
            () => {
                void refreshDocumentRequestsSilently(
                    applicationId
                );
            },
            1200
        );

        window.setTimeout(
            () => {
                void refreshDocumentRequestsSilently(
                    applicationId
                );
            },
            4500
        );
    }

    /**
     * Updates the document-status field in place.
     *
     * @param {string} status
     */
    function updateVisibleDocumentStatus(status) {
        if (state.currentApplication) {
            state.currentApplication.documentStatus =
                status;
        }

        setText(
            'view-documentStatus',
            formatEnum(status)
        );
    }

    /**
     * Recalculates the visible aggregate document status from the current
     * document cards.
     *
     * @param {Array<Object>} documents
     */
    function synchronizeVisibleDocumentStatus(
        documents
    ) {
        const currentDocuments =
            documents.filter(document =>
                document
                && document.active !== false
                && document.current !== false
                && String(
                    document.verificationStatus || ''
                ).toUpperCase() !== 'SUPERSEDED'
            );

        const statuses =
            currentDocuments.map(document =>
                String(
                    document.verificationStatus || 'PENDING'
                ).toUpperCase()
            );

        let aggregateStatus =
            statuses.length === 0
                ? 'PENDING'
                : 'PENDING';

        if (statuses.some(status =>
            status === 'REUPLOAD_REQUIRED'
        )) {
            aggregateStatus = 'REUPLOAD_REQUIRED';
        } else if (statuses.some(status =>
            status === 'REJECTED'
        )) {
            aggregateStatus = 'REJECTED';
        } else if (statuses.length > 0
                && statuses.every(status =>
                    status === 'VERIFIED'
                )) {
            aggregateStatus = 'VERIFIED';
        }

        updateVisibleDocumentStatus(
            aggregateStatus
        );
    }

    /**
     * Local PATCH helper because api.js currently exposes GET, POST,
     * PUT and DELETE but not PATCH.
     *
     * @param {string} endpoint
     * @param {Object} payload
     * @returns {Promise<*>}
     */
    async function apiPatchJson(
        endpoint,
        payload
    ) {
        const response =
            await fetch(
                `/api${endpoint}`,
                {
                    method: 'PATCH',
                    headers: {
                        'Content-Type':
                            'application/json'
                    },
                    credentials: 'include',
                    cache: 'no-store',
                    body: JSON.stringify(payload)
                }
            );

        if (response.status === 401) {
            window.location.href = '/login.html';
            throw new Error(
                'Session expired. Please log in again.'
            );
        }

        const text =
            await response.text();

        let body = null;

        if (text) {
            try {
                body = JSON.parse(text);
            } catch (error) {
                body = null;
            }
        }

        if (!response.ok) {
            const message =
                body && body.message
                    ? body.message
                    : `Request failed with status ${
                        response.status
                    }.`;

            const validationMessage =
                buildValidationMessage(
                    body && body.errors
                );

            throw new Error(
                validationMessage
                    ? `${message}\n${validationMessage}`
                    : message
            );
        }

        document.dispatchEvent(
            new CustomEvent(
                'erp:data-mutated',
                {
                    detail: {
                        method: 'PATCH',
                        endpoint,
                        responseData: body,
                        occurredAt: Date.now()
                    }
                }
            )
        );

        return body;
    }

    /**
     * Returns a secured Application-document URL.
     */
    function buildDocumentUrl(
        applicationId,
        documentId,
        action
    ) {
        return `/api/admission/branch/applications/${
            encodeURIComponent(applicationId)
        }/documents/${
            encodeURIComponent(documentId)
        }/${action}`;
    }

    /**
     * Controls workflow actions when the Application is locked.
     *
     * @param {Object} application
     */
    function setActionAvailability(
        application
    ) {
        const locked =
            application.workflowLocked === true
            || String(application.currentStage)
                .toUpperCase() === 'CLOSED'
            || String(application.currentStage)
                .toUpperCase() === 'ENROLLED';

        [
            view.requestDocumentButton,
            view.requestDocumentInlineButton
        ].forEach(button => {
            if (button) {
                button.disabled = locked;
            }
        });
    }

    async function synchronizeApplicationListState() {
        if (
            !view?.root
            || !document.body.contains(
                view.root
            )
        ) {
            stopApplicationListAutoSync();
            return false;
        }

        if (
            state.applicationListSyncBusy
            || document.hidden
            || !view?.tableComponent
            || view.tableComponent.classList.contains(
                'hidden'
            )
        ) {
            return false;
        }

        state.applicationListSyncBusy = true;

        try {
            const synchronize = () =>
                loadApplications(
                    true,
                    true
                );

            if (
                typeof window.erpPreserveViewportDuring
                === 'function'
            ) {
                return await window.erpPreserveViewportDuring(
                    synchronize
                );
            }

            return await synchronize();
        } finally {
            state.applicationListSyncBusy = false;
        }
    }

    function startApplicationListAutoSync() {
        if (
            state.applicationListSyncTimer
            || !state.initialApplicationListLoaded
        ) {
            return;
        }

        state.applicationListSyncTimer =
            window.setInterval(
                () => {
                    if (
                        !view?.root
                        || !document.body.contains(
                            view.root
                        )
                    ) {
                        stopApplicationListAutoSync();
                        return;
                    }

                    void synchronizeApplicationListState();
                },
                5000
            );
    }

    function stopApplicationListAutoSync() {
        if (state.applicationListSyncTimer) {
            window.clearInterval(
                state.applicationListSyncTimer
            );
        }

        state.applicationListSyncTimer = null;
        state.applicationListSyncBusy = false;
    }

    /**
     * Shows the table and hides the profile.
     */
    function showTableView() {
        stopDocumentAutoSync();

        showElement(view.tableComponent);
        hideElement(view.detailComponent);

        state.currentApplicationId = null;
        state.currentApplication = null;
        state.profileTransitions = [];
        state.schoolVisit = null;
        state.entranceTest = null;
        renderProfileNextAction(null);

        if (state.initialApplicationListLoaded) {
            startApplicationListAutoSync();
            void synchronizeApplicationListState();
        }

        window.scrollTo({
            top: 0,
            behavior: 'auto'
        });
    }

    function handleApplicationsVisibilityChange() {
        if (
            !document.hidden
            && state.initialApplicationListLoaded
            && view?.tableComponent
            && !view.tableComponent.classList.contains(
                'hidden'
            )
        ) {
            void synchronizeApplicationListState();
        }
    }

    function handleSearch() {
        state.page = 0;
        renderApplicationRows();
    }

    function resetSearch() {
        [
            view.searchKeyword,
            view.searchGender,
            view.searchLevel,
            view.searchClass,
            view.searchStage,
            view.searchDocumentStatus,
            view.searchScholarship,
            view.searchStatus,
            view.searchFromDate,
            view.searchToDate
        ].forEach(control => {
            if (control) {
                control.value = '';
            }
        });

        state.page = 0;
        collapseAdvancedFilters();
        clearBulkSelection();
        renderApplicationRows();
    }

    function getFilteredRows() {
        const keyword =
            trimValue(view.searchKeyword)
                .toLowerCase();

        const gender =
            trimValue(view.searchGender)
                .toUpperCase();

        const level =
            trimValue(view.searchLevel)
                .toLowerCase();

        const className =
            trimValue(view.searchClass)
                .toLowerCase();

        const stage =
            trimValue(view.searchStage)
                .toUpperCase();

        const documentStatus =
            trimValue(view.searchDocumentStatus)
                .toUpperCase();

        const scholarship =
            trimValue(view.searchScholarship)
                .toUpperCase();

        const status =
            trimValue(view.searchStatus)
                .toUpperCase();

        const fromDate =
            trimValue(view.searchFromDate);

        const toDate =
            trimValue(view.searchToDate);

        return state.currentRows.filter(record => {
            const matchesKeyword =
                !keyword
                || [
                    record.applicationNo,
                    record.studentName,
                    record.className,
                    record.levelName
                ]
                    .some(value =>
                        String(value || '')
                            .toLowerCase()
                            .includes(keyword)
                    );

            const matchesGender =
                !gender
                || enumEquals(
                    record.gender,
                    gender
                );

            const matchesLevel =
                !level
                || String(record.levelName || '')
                    .trim()
                    .toLowerCase() === level;

            const matchesClass =
                !className
                || String(record.className || '')
                    .trim()
                    .toLowerCase() === className;

            const matchesStage =
                !stage
                || enumEquals(
                    record.currentStage,
                    stage
                );

            const matchesDocuments =
                !documentStatus
                || enumEquals(
                    record.documentStatus,
                    documentStatus
                );

            const matchesScholarship =
                !scholarship
                || enumEquals(
                    record.scholarshipStatus,
                    scholarship
                );

            const matchesStatus =
                !status
                || enumEquals(
                    record.applicationStatus
                    || record.status,
                    status
                );

            const submittedDay =
                isoDateOnly(record.submittedDate);

            const matchesFromDate =
                !fromDate
                || (
                    submittedDay
                    && submittedDay >= fromDate
                );

            const matchesToDate =
                !toDate
                || (
                    submittedDay
                    && submittedDay <= toDate
                );

            return matchesKeyword
                && matchesGender
                && matchesLevel
                && matchesClass
                && matchesStage
                && matchesDocuments
                && matchesScholarship
                && matchesStatus
                && matchesFromDate
                && matchesToDate;
        });
    }

    function populateDynamicFilterOptions() {
        populateSelectFromRows(
            view.searchLevel,
            state.currentRows,
            record => record.levelName,
            'All Levels'
        );

        populateSelectFromRows(
            view.searchClass,
            state.currentRows,
            record => record.className,
            'All Classes'
        );
    }

    function populateSelectFromRows(
        select,
        rows,
        valueReader,
        emptyLabel
    ) {
        if (!select) {
            return;
        }

        const selectedValue =
            select.value;

        const values =
            Array.from(
                new Set(
                    rows
                        .map(valueReader)
                        .map(value =>
                            String(value || '').trim()
                        )
                        .filter(Boolean)
                )
            )
                .sort((left, right) =>
                    left.localeCompare(
                        right,
                        undefined,
                        { numeric: true }
                    )
                );

        select.replaceChildren();

        const allOption =
            document.createElement('option');
        allOption.value = '';
        allOption.textContent = emptyLabel;
        select.appendChild(allOption);

        values.forEach(value => {
            const option =
                document.createElement('option');
            option.value = value;
            option.textContent = value;
            select.appendChild(option);
        });

        if (values.includes(selectedValue)) {
            select.value = selectedValue;
        }
    }

    function toggleAdvancedFilters() {
        if (!view.advancedFilters
                || !view.moreFiltersButton) {
            return;
        }

        const willOpen =
            view.advancedFilters
                .classList
                .contains('hidden');

        view.advancedFilters.classList.toggle(
            'hidden',
            !willOpen
        );

        view.moreFiltersButton.setAttribute(
            'aria-expanded',
            String(willOpen)
        );
    }

    function collapseAdvancedFilters() {
        view.advancedFilters?.classList.add(
            'hidden'
        );
        view.moreFiltersButton?.setAttribute(
            'aria-expanded',
            'false'
        );
    }

    function updateActiveFilterCount() {
        if (!view.activeFilterCount) {
            return;
        }

        const advancedControls = [
            view.searchGender,
            view.searchDocumentStatus,
            view.searchScholarship,
            view.searchFromDate,
            view.searchToDate
        ];

        const count =
            advancedControls.reduce(
                (total, control) =>
                    total
                    + (trimValue(control) ? 1 : 0),
                0
            );

        view.activeFilterCount.textContent =
            String(count);
        view.activeFilterCount.classList.toggle(
            'hidden',
            count === 0
        );
    }

    function handleSelectPage(event) {
        const checked =
            event.currentTarget.checked;

        getFilteredRows().forEach(record => {
            const applicationId =
                Number(record.applicationId);

            if (!Number.isInteger(applicationId)
                    || applicationId <= 0) {
                return;
            }

            if (checked) {
                state.selectedApplications.set(
                    applicationId,
                    record
                );
            } else {
                state.selectedApplications.delete(
                    applicationId
                );
            }
        });

        renderApplicationRows();
    }

    function clearBulkSelection() {
        state.selectedApplications.clear();
        renderApplicationRows();
    }

    function updateBulkSelectionUI(
        visibleRows = []
    ) {
        const selectedCount =
            state.selectedApplications.size;

        if (view.selectedCount) {
            view.selectedCount.textContent =
                `${selectedCount} selected`;
        }

        if (view.bulkNextActionButton) {
            view.bulkNextActionButton.disabled =
                selectedCount === 0;
        }

        if (view.bulkClearButton) {
            view.bulkClearButton.disabled =
                selectedCount === 0;
        }

        if (view.selectPage) {
            const selectableIds =
                visibleRows
                    .map(record =>
                        Number(record.applicationId)
                    )
                    .filter(id =>
                        Number.isInteger(id)
                        && id > 0
                    );

            const selectedOnPage =
                selectableIds.filter(id =>
                    state.selectedApplications.has(id)
                ).length;

            view.selectPage.checked =
                selectableIds.length > 0
                && selectedOnPage
                    === selectableIds.length;

            view.selectPage.indeterminate =
                selectedOnPage > 0
                && selectedOnPage
                    < selectableIds.length;
        }
    }

    function openBulkNextActionConfirmation() {
        const selected =
            Array.from(
                state.selectedApplications.values()
            );

        if (selected.length === 0) {
            notifyError(
                'Select at least one application.'
            );
            return;
        }

        const first = selected[0];
        const compatible =
            selected.every(record =>
                record.nextActionAvailable === true
                && !record.workflowLocked
                && enumEquals(
                    record.currentStage,
                    first.currentStage
                )
                && enumEquals(
                    record.nextTargetStage,
                    first.nextTargetStage
                )
                && enumEquals(
                    record.nextAction,
                    first.nextAction
                )
            );

        if (!compatible) {
            notifyError(
                'Bulk next-stage action requires selected applications to have the same current stage and same available next action.'
            );
            return;
        }

        showWorkflowConfirmation({
            title: 'Move Selected Applications',
            currentStage: first.currentStage,
            targetStage: first.nextTargetStage,
            actionLabel:
                first.nextActionLabel,
            count: selected.length,
            onConfirm: async () => {
                await performBulkWorkflowTransition(
                    selected
                );
            }
        });
    }

    async function openRowNextActionConfirmation(
        record
    ) {
        if (!record
                || record.nextActionAvailable !== true) {
            return;
        }

        const applicationId =
            Number(record.applicationId);

        if (!Number.isInteger(applicationId)
                || applicationId <= 0) {
            notifyError(
                'The selected application is invalid.'
            );
            return;
        }

        let transition = null;
        let loaderToken = null;

        try {
            if (typeof showLoader === 'function') {
                loaderToken = showLoader(
                    'Checking available admission action...'
                );
            }

            const transitions =
                await fetchAvailableTransitions(
                    applicationId
                );

            transition =
                transitions.find(item =>
                    enumEquals(
                        item.action,
                        record.nextAction
                    )
                    && enumEquals(
                        item.targetStage,
                        record.nextTargetStage
                    )
                ) || null;
        } catch (error) {
            notifyError(
                readErrorMessage(
                    error,
                    'The next admission action could not be checked.'
                )
            );
            return;
        } finally {
            if (loaderToken
                    && typeof hideLoader === 'function') {
                hideLoader(loaderToken);
            }
        }

        if (!transition) {
            notifyError(
                'This action is no longer available. The applications list will now be synchronized.'
            );

            await loadApplications(true);
            return;
        }

        if (
            enumEquals(
                record.currentStage,
                'APPLICATION_VERIFICATION'
            )
            && enumEquals(
                transition.action,
                'ADVANCE'
            )
            && enumEquals(
                transition.targetStage,
                'SCHOOL_VISIT'
            )
        ) {
            await openSchoolVisitScheduleModal(
                false,
                {
                    record,
                    transition
                }
            );
            return;
        }

        showWorkflowConfirmation({
            title: 'Confirm Admission Action',
            currentStage: record.currentStage,
            targetStage: transition.targetStage,
            actionLabel:
                transition.label
                || record.nextActionLabel,
            count: 1,
            onConfirm: async () => {
                await performSingleWorkflowTransition(
                    record,
                    transition
                );
            }
        });
    }

    function openProfileNextActionConfirmation() {
        const transition =
            state.profileTransitions.find(
                item =>
                    enumEquals(
                        item?.action,
                        'ADVANCE'
                    )
            ) || null;

        if (!transition
                || !state.currentApplicationId
                || !state.currentApplication) {
            return;
        }

        if (
            enumEquals(
                state.currentApplication.currentStage,
                'APPLICATION_VERIFICATION'
            )
            && enumEquals(
                transition.action,
                'ADVANCE'
            )
            && enumEquals(
                transition.targetStage,
                'SCHOOL_VISIT'
            )
        ) {
            void openSchoolVisitScheduleModal(
                false,
                {
                    record: {
                        applicationId:
                            state.currentApplicationId,
                        currentStage:
                            state.currentApplication.currentStage,
                        nextAction:
                            transition.action,
                        nextTargetStage:
                            transition.targetStage,
                        nextActionLabel:
                            transition.label
                    },
                    transition
                }
            );
            return;
        }

        showWorkflowConfirmation({
            title: 'Confirm Admission Action',
            currentStage:
                state.currentApplication.currentStage,
            targetStage:
                transition.targetStage,
            actionLabel:
                transition.label,
            count: 1,
            onConfirm: async () => {
                const record = {
                    applicationId:
                        state.currentApplicationId,
                    currentStage:
                        state.currentApplication.currentStage,
                    nextAction:
                        transition.action,
                    nextTargetStage:
                        transition.targetStage,
                    nextActionLabel:
                        transition.label
                };

                await performSingleWorkflowTransition(
                    record,
                    transition,
                    true
                );
            }
        });
    }

    function showWorkflowConfirmation({
        title,
        currentStage,
        targetStage,
        actionLabel,
        count,
        onConfirm
    }) {
        if (typeof showPremiumModal
                !== 'function') {
            notifyError(
                'Confirmation dialog is unavailable.'
            );
            return;
        }

        const content =
            document.createElement('div');
        content.className =
            'app-workflow-confirm-content';

        const summary =
            document.createElement('p');
        summary.className = 'text-muted';
        summary.textContent =
            count > 1
                ? `${count} selected applications will be processed.`
                : (actionLabel
                    ? String(actionLabel)
                    : 'Continue to the next admission stage.');
        content.appendChild(summary);

        const stageGrid =
            document.createElement('div');
        stageGrid.className =
            'app-workflow-confirm-grid';

        stageGrid.appendChild(
            createWorkflowSummaryItem(
                'Current Stage',
                formatEnum(currentStage)
            )
        );
        stageGrid.appendChild(
            createWorkflowSummaryItem(
                'Next Stage',
                formatEnum(targetStage)
            )
        );
        content.appendChild(stageGrid);

        showPremiumModal({
            title,
            type: 'warning',
            contentNode: content,
            confirmText:
                count > 1
                    ? 'Move Selected'
                    : 'Continue',
            cancelText: 'Cancel',
            onConfirm: async modal => {
                await modal.close();
                await onConfirm();
            }
        });
    }

    function createWorkflowSummaryItem(
        label,
        value
    ) {
        const item =
            document.createElement('div');
        item.className =
            'app-workflow-confirm-item';

        const labelNode =
            document.createElement('span');
        labelNode.className = 'text-muted';
        labelNode.textContent = label;

        const valueNode =
            document.createElement('strong');
        valueNode.textContent =
            displayValue(value);

        item.append(
            labelNode,
            valueNode
        );

        return item;
    }

    async function performSingleWorkflowTransition(
        record,
        transition,
        keepProfileOpen = false
    ) {
        const applicationId =
            Number(record.applicationId);

        let loaderToken = null;
        let errorMessage = null;
        let transitionResponse = null;

        try {
            if (typeof showLoader === 'function') {
                loaderToken = showLoader(
                    'Updating admission workflow...'
                );
            }

            transitionResponse =
                await submitWorkflowTransition(
                    applicationId,
                    record.currentStage,
                    transition
                );

            await loadApplications(true);

            if (
                typeof window.erpCancelPendingDataSync
                === 'function'
            ) {
                window.erpCancelPendingDataSync();
            }

            if (keepProfileOpen
                    && transitionResponse) {
                applyWorkflowResponseToProfile(
                    transitionResponse
                );
                await loadSchoolVisit(
                    applicationId
                );

                await loadProfileTransitions(
                    applicationId
                );
            }
        } catch (error) {
            errorMessage =
                readErrorMessage(
                    error,
                    'Admission workflow could not be updated.'
                );
        } finally {
            if (loaderToken
                    && typeof hideLoader === 'function') {
                hideLoader(loaderToken);
            }
        }

        if (errorMessage) {
            notifyError(errorMessage);
            return;
        }

        if (!keepProfileOpen) {
            showTableView();
        }

        notifySuccess(
            'Application moved to the next admission stage successfully.'
        );
    }

    async function performBulkWorkflowTransition(
        records
    ) {
        let loaderToken = null;
        let completed = 0;
        const failures = [];

        try {
            if (typeof showLoader === 'function') {
                loaderToken = showLoader(
                    `Updating ${records.length} applications...`
                );
            }

            for (const record of records) {
                try {
                    const transitions =
                        await fetchAvailableTransitions(
                            Number(record.applicationId)
                        );

                    const transition =
                        transitions.find(item =>
                            enumEquals(
                                item.action,
                                record.nextAction
                            )
                            && enumEquals(
                                item.targetStage,
                                record.nextTargetStage
                            )
                        );

                    if (!transition) {
                        throw new Error(
                            'Next action is no longer available.'
                        );
                    }

                    await submitWorkflowTransition(
                        Number(record.applicationId),
                        record.currentStage,
                        transition
                    );

                    completed++;
                } catch (error) {
                    failures.push(
                        `${displayValue(record.applicationNo)}: ${readErrorMessage(error, 'Failed')}`
                    );
                }
            }

            state.selectedApplications.clear();
            await loadApplications(true);

            if (
                typeof window.erpCancelPendingDataSync
                === 'function'
            ) {
                window.erpCancelPendingDataSync();
            }
        } finally {
            if (loaderToken
                    && typeof hideLoader === 'function') {
                hideLoader(loaderToken);
            }
        }

        if (failures.length > 0) {
            notifyError(
                `${completed} application(s) updated. ${failures.length} failed. ${failures.slice(0, 3).join(' | ')}`
            );
            return;
        }

        notifySuccess(
            `${completed} application(s) moved to the next admission stage successfully.`
        );
    }

    async function fetchAvailableTransitions(
        applicationId
    ) {
        const response =
            await apiGet(
                `${API_ROOT}/${encodeURIComponent(applicationId)}/workflow/transitions`
            );

        const data =
            unwrapResponseData(response);

        return Array.isArray(data)
            ? data
            : [];
    }

    async function submitWorkflowTransition(
        applicationId,
        expectedCurrentStage,
        transition,
        schoolVisitScheduledAt = null,
        internalRemarks = null
    ) {
        const response =
            await apiPatchJson(
                `${API_ROOT}/${encodeURIComponent(applicationId)}/workflow/transition`,
                {
                    expectedCurrentStage,
                    targetStage:
                        transition.targetStage,
                    action:
                        transition.action,
                    schoolVisitScheduledAt,
                    publicRemarks: null,
                    internalRemarks,
                    /*
                     * APPLICATION_VERIFICATION -> SCHOOL_VISIT uses the
                     * dedicated School Visit scheduling email. The backend
                     * suppresses the generic transition email for that route.
                     */
                    notifyApplicant:
                        transition.applicantNotificationRequired === true
                        || transition.applicantNotificationSupported === true
                }
            );

        return unwrapResponseData(response);
    }

    async function loadProfileTransitions(
        applicationId,
        options = {}
    ) {
        const {
            render = true
        } = options || {};
        try {
            state.profileTransitions =
                await fetchAvailableTransitions(
                    applicationId
                );
        } catch (error) {
            console.error(
                'Application workflow transitions could not be loaded.',
                error
            );
            state.profileTransitions = [];
        }

        const primaryTransition =
            state.profileTransitions.find(
                transition =>
                    enumEquals(
                        transition?.action,
                        'ADVANCE'
                    )
            ) || null;

        if (render) {
            renderProfileNextAction(
                primaryTransition
            );
        }

        return state.profileTransitions;
    }

    function renderProfileNextAction(
        transition
    ) {

        if (state.currentApplication
                && enumEquals(
                    state.currentApplication.currentStage,
                    'SCHOOL_VISIT'
                )
                && transition
                && enumEquals(
                    transition.targetStage,
                    'SCHOOL_VISIT'
                )) {
            transition = null;
        }

        setText(
            'view-nextStage',
            transition
                ? formatEnum(transition.targetStage)
                : '-'
        );

        setText(
            'view-nextActionLabel',
            transition
                ? displayValue(transition.label)
                : '-'
        );

        if (!view.profileNextStageButton) {
            return;
        }

        view.profileNextStageButton.classList.remove(
            'hidden'
        );

        const continueEnabled =
            Boolean(transition);

        view.profileNextStageButton.disabled =
            !continueEnabled;

        view.profileNextStageButton.setAttribute(
            'aria-disabled',
            String(!continueEnabled)
        );

        view.profileNextStageButton.title =
            continueEnabled
                ? (
                    transition?.label
                        ? `Continue: ${transition.label}`
                        : 'Continue to the next application stage.'
                )
                : 'Continue is unavailable until all pending requirements are completed.';
    }

    function applyWorkflowResponseToProfile(
        response
    ) {
        if (!response) {
            return;
        }

        if (state.currentApplication) {
            Object.assign(
                state.currentApplication,
                {
                    currentStage:
                        response.currentStage,
                    applicationStatus:
                        response.applicationStatus,
                    verificationStatus:
                        response.verificationStatus,
                    documentStatus:
                        response.documentStatus,
                    testStatus:
                        response.testStatus,
                    feeDecisionStatus:
                        response.feeDecisionStatus,
                    scholarshipWorkflowStatus:
                        response.scholarshipWorkflowStatus,
                    paymentStatus:
                        response.paymentStatus,
                    admissionStatus:
                        response.admissionStatus,
                    workflowLocked:
                        response.workflowLocked
                }
            );
        }

        setText(
            'summary-appCurrentStage',
            formatEnum(response.currentStage)
        );
        renderBadgeInto(
            'summary-appStatus',
            response.applicationStatus
        );
        setText(
            'view-appStatus',
            formatEnum(response.applicationStatus)
        );
        setText(
            'view-currentStage',
            formatEnum(response.currentStage)
        );
        setText(
            'view-verificationStatus',
            formatEnum(response.verificationStatus)
        );
        setText(
            'view-documentStatus',
            formatEnum(response.documentStatus)
        );
        setText(
            'view-testStatus',
            formatEnum(response.testStatus)
        );
        setText(
            'view-feeStatus',
            formatEnum(response.feeDecisionStatus)
        );
        setText(
            'view-scholarshipWorkflowStatus',
            formatEnum(response.scholarshipWorkflowStatus)
        );
        setText(
            'summary-appScholarship',
            formatEnum(response.scholarshipWorkflowStatus)
        );
        setText(
            'view-paymentStatus',
            formatEnum(response.paymentStatus)
        );
        setText(
            'view-admissionStatus',
            formatEnum(response.admissionStatus)
        );
    }

    function compactWorkflowActionLabel(
        label,
        targetStage
    ) {
        const normalized =
            String(label || '')
                .trim()
                .toLowerCase();

        const compact = new Map([
            ['start verification', 'Verify'],
            ['move to school visit', 'School Visit'],
            ['move to entrance test', 'Entrance Test'],
            ['start fee discussion', 'Fee Discussion'],
            ['open scholarship review', 'Scholarship'],
            ['move to payment', 'Payment'],
            ['approve final admission', 'Final Admission'],
            ['mark as enrolled', 'Enroll']
        ]);

        return compact.get(normalized)
            || formatEnum(targetStage)
            || 'Next';
    }

    function enumEquals(
        left,
        right
    ) {
        return String(left || '')
            .trim()
            .toUpperCase()
            === String(right || '')
                .trim()
                .toUpperCase();
    }

    function isoDateOnly(
        value
    ) {
        if (!value) {
            return '';
        }

        const raw =
            String(value).trim();

        const match =
            raw.match(/^\d{4}-\d{2}-\d{2}/);

        if (match) {
            return match[0];
        }

        const date = new Date(raw);
        if (Number.isNaN(date.getTime())) {
            return '';
        }

        return date.toISOString().slice(0, 10);
    }

    function sortCurrentRows() {
        const direction =
            state.sortDirection === 'ASC'
                ? 1
                : -1;

        const field =
            state.sortField;

        state.currentRows.sort(
            (left, right) => {
                const leftValue =
                    normalizeSortValue(
                        left ? left[field] : null
                    );

                const rightValue =
                    normalizeSortValue(
                        right ? right[field] : null
                    );

                if (leftValue < rightValue) {
                    return -1 * direction;
                }

                if (leftValue > rightValue) {
                    return 1 * direction;
                }

                return 0;
            }
        );
    }

    function normalizeSortValue(
        value
    ) {
        if (value == null) {
            return '';
        }

        const date =
            Date.parse(value);

        if (!Number.isNaN(date)
                && String(value).includes('-')) {
            return date;
        }

        return String(value)
            .toLowerCase();
    }

    /**
     * Exports the currently loaded and filtered server page.
     */
    function exportCurrentRows() {
        const rows =
            getFilteredRows();

        if (rows.length === 0) {
            notifyError(
                'There are no visible applications to export.'
            );
            return;
        }

        const data = [
            [
                'Application Number',
                'Student Name',
                'Class',
                'Submitted Date',
                'Status'
            ],
            ...rows.map(record => [
                record.applicationNo,
                record.studentName,
                record.className,
                record.submittedDate,
                record.status
            ])
        ];

        const csv =
            data
                .map(row =>
                    row.map(csvValue).join(',')
                )
                .join('\r\n');

        const blob =
            new Blob(
                [csv],
                {
                    type:
                        'text/csv;charset=utf-8'
                }
            );

        const link =
            window.document.createElement('a');

        const objectUrl =
            URL.createObjectURL(blob);

        link.href = objectUrl;
        link.download =
            `branch-applications-${
                new Date()
                    .toISOString()
                    .slice(0, 10)
            }.csv`;

        window.document.body.appendChild(link);
        link.click();
        link.remove();

        URL.revokeObjectURL(objectUrl);
    }

    function csvValue(
        value
    ) {
        const text =
            String(value == null ? '' : value);

        return `"${text.replace(/"/g, '""')}"`;
    }

    function resetDocumentReviewForm() {
        view.reviewForm?.reset();
        clearCalendarInput(
            view.reuploadDeadline
        );

        if (view.reviewDocumentId) {
            view.reviewDocumentId.value = '';
        }

        if (view.reviewSubtitle) {
            view.reviewSubtitle.textContent = '';
        }

        clearInlineError(view.reviewError);
        updateReviewDecisionFields();
    }

    function closeDocumentReviewModal() {
        closeModal(view.reviewModal);
        resetDocumentReviewForm();
    }

    function resetAdditionalDocumentForm() {
        view.requestForm?.reset();
        clearCalendarInput(
            view.requestUploadDeadline
        );
        clearInlineError(view.requestError);
    }

    function closeAdditionalDocumentModal() {
        closeModal(view.requestModal);
        resetAdditionalDocumentForm();
    }

    function resetCancelRequestForm() {
        view.cancelRequestForm?.reset();

        if (view.cancelRequestId) {
            view.cancelRequestId.value = '';
        }

        if (view.cancelRequestSubtitle) {
            view.cancelRequestSubtitle.textContent = '';
        }

        clearInlineError(
            view.cancellationError
        );
    }

    function closeCancelRequestModal() {
        closeModal(view.cancelRequestModal);
        resetCancelRequestForm();
    }

    function openModal(
        modal
    ) {
        if (!modal) {
            return;
        }

        modal.classList.remove('hidden');
        modal.setAttribute(
            'aria-hidden',
            'false'
        );

        window.document.body.classList.add(
            'erp-modal-open'
        );
    }

    function closeModal(
        modal
    ) {
        if (!modal) {
            return;
        }

        modal.classList.add('hidden');
        modal.setAttribute(
            'aria-hidden',
            'true'
        );

        if (!view.root.querySelector(
            '.ba-modal-backdrop:not(.hidden)'
        )) {
            window.document.body.classList.remove(
                'erp-modal-open'
            );
        }
    }

    function bindBackdropClose(
        modal,
        closeHandler
    ) {
        modal?.addEventListener(
            'click',
            event => {
                if (event.target === modal) {
                    closeHandler();
                }
            }
        );
    }

    function appendRecordItem(
        card,
        label,
        value
    ) {
        const normalized =
            displayValue(value);

        if (normalized === '—') {
            return;
        }

        const row =
            window.document.createElement('div');

        row.className = 'emp-record-item';

        const labelNode =
            window.document.createElement('span');

        labelNode.className =
            'emp-record-label';

        labelNode.textContent = label;

        const valueNode =
            window.document.createElement('span');

        valueNode.className =
            'emp-record-value';

        valueNode.textContent =
            normalized;

        row.append(labelNode, valueNode);
        card.appendChild(row);
    }

    function createButton(
        label,
        iconClass,
        className,
        onClick
    ) {
        const button =
            window.document.createElement('button');

        button.type = 'button';
        button.className = className;

        const icon =
            window.document.createElement('i');

        icon.className = `bi ${iconClass}`;

        const text =
            window.document.createTextNode(
                ` ${label}`
            );

        button.append(icon, text);
        button.addEventListener(
            'click',
            onClick
        );

        return button;
    }

    function createStatusBadge(
        status
    ) {
        const badge =
            window.document.createElement('span');

        badge.className =
            `badge ${getBadgeClass(status)}`;

        badge.textContent =
            formatEnum(status) || 'Not Set';

        return badge;
    }

    function renderBadgeInto(
        id,
        status
    ) {
        const element =
            view.root.querySelector(`#${id}`);

        if (!element) {
            return;
        }

        element.className =
            `status-badge badge ${
                getBadgeClass(status)
            }`;

        element.textContent =
            formatEnum(status) || 'Not Set';
    }

    function getBadgeClass(
        status
    ) {
        const normalized =
            String(status || '')
                .trim()
                .toUpperCase();

        if ([
            'APPROVED',
            'ADMITTED',
            'ENROLLED',
            'VERIFIED',
            'COMPLETED',
            'SENT',
            'PAID',
            'PASSED'
        ].includes(normalized)) {
            return 'bg-success';
        }

        if ([
            'REJECTED',
            'FAILED',
            'CANCELLED',
            'CLOSED',
            'EXPIRED'
        ].includes(normalized)) {
            return 'bg-danger';
        }

        if ([
            'SUBMITTED',
            'WAITLISTED',
            'PENDING',
            'REUPLOAD_REQUIRED',
            'ADDITIONAL_DOCUMENTS_REQUIRED'
        ].includes(normalized)) {
            return 'bg-warning text-dark';
        }

        if ([
            'UNDER_REVIEW',
            'IN_PROGRESS',
            'UPLOADED',
            'SCHEDULED'
        ].includes(normalized)) {
            return 'bg-primary';
        }

        return 'bg-secondary';
    }

    function buildYearTerm(
        application
    ) {
        const year =
            application.academicYearCode
            || application.academicYearName;

        const term =
            application.joiningTermName
            || application.term;

        return [year, term]
            .filter(Boolean)
            .join(' / ')
            || '—';
    }

    function buildVerificationDecision(
        application
    ) {
        if (!application.verificationDecisionBy
                && !application.verificationDecisionAt) {
            return 'Not decided';
        }

        const parts = [];

        if (application.verificationDecisionBy) {
            parts.push(
                `User ${application.verificationDecisionBy}`
            );
        }

        if (application.verificationDecisionAt) {
            parts.push(
                formatDateTime(
                    application.verificationDecisionAt
                )
            );
        }

        return parts.join(' • ');
    }

    function buildHistoryTransition(
        history
    ) {
        const oldStatus =
            formatEnum(history.oldStatus);

        const newStatus =
            formatEnum(history.newStatus);

        if (oldStatus && newStatus) {
            return `${oldStatus} → ${newStatus}`;
        }

        return newStatus
            || oldStatus
            || 'Status updated';
    }

    function buildHistoryEmail(
        history
    ) {
        if (history.emailRequired !== true
                && !history.emailStatus) {
            return '';
        }

        const parts = [
            formatEnum(history.emailType),
            formatEnum(history.emailStatus),
            formatDateTime(history.emailSentAt)
        ].filter(Boolean);

        return parts.join(' • ');
    }

    function formatEnum(
        value
    ) {
        if (value == null
                || String(value).trim() === '') {
            return '';
        }

        return String(value)
            .trim()
            .replace(/[_-]+/g, ' ')
            .toLowerCase()
            .replace(
                /\b\w/g,
                character =>
                    character.toUpperCase()
            );
    }

    function formatDateTime(
        value,
        includeTime = true
    ) {
        if (!value) {
            return '';
        }

        /*
         * Never use browser/device locale for ERP dates.
         * The shared formatter always displays:
         *   dd-MM-yyyy
         *   dd-MM-yyyy hh:mm AM/PM
         */
        if (window.erpDate) {
            return includeTime
                ? window.erpDate.formatDateTime(value, '')
                : window.erpDate.formatDate(value, '');
        }

        return String(value);
    }

    function formatFileSize(
        value
    ) {
        const bytes =
            Number(value);

        if (!Number.isFinite(bytes)
                || bytes < 0) {
            return '';
        }

        if (bytes < 1024) {
            return `${bytes} B`;
        }

        if (bytes < 1024 * 1024) {
            return `${
                (bytes / 1024).toFixed(1)
            } KB`;
        }

        return `${
            (bytes / (1024 * 1024))
                .toFixed(1)
        } MB`;
    }

    function joinNames(
        ...names
    ) {
        return names
            .filter(name =>
                name != null
                && String(name).trim()
            )
            .map(name =>
                String(name).trim()
            )
            .join(' ');
    }

    function setText(
        id,
        value
    ) {
        const element =
            view.root.querySelector(`#${id}`);

        setNodeText(
            element,
            displayValue(value)
        );
    }

    function setNodeText(
        element,
        value
    ) {
        if (element) {
            element.textContent =
                value == null
                    ? ''
                    : String(value);
        }
    }

    function displayValue(
        value
    ) {
        if (value == null
                || String(value).trim() === '') {
            return '—';
        }

        return String(value);
    }

    function setCount(
        element,
        count
    ) {
        if (element) {
            element.textContent =
                `(${Number(count) || 0})`;
        }
    }

    function trimValue(
        element
    ) {
        if (!element) {
            return '';
        }

        return String(element.value || '')
            .trim();
    }

    function nullIfBlank(
        value
    ) {
        return value
                && String(value).trim()
            ? String(value).trim()
            : null;
    }

    function showElement(
        element
    ) {
        element?.classList.remove('hidden');
    }

    function hideElement(
        element
    ) {
        element?.classList.add('hidden');
    }

    function toggleElement(
        element,
        show
    ) {
        if (show) {
            showElement(element);
        } else {
            hideElement(element);
        }
    }

    function setButtonActionAvailability(
        button,
        enabled,
        disabledReason = ''
    ) {
        if (!button) {
            return;
        }

        button.disabled = !enabled;
        button.setAttribute(
            'aria-disabled',
            enabled ? 'false' : 'true'
        );

        if (enabled) {
            button.removeAttribute('title');
        } else if (disabledReason) {
            button.setAttribute(
                'title',
                disabledReason
            );
        }
    }

    function showInlineError(
        element,
        message
    ) {
        if (!element) {
            return;
        }

        element.textContent = message;
        showElement(element);
    }

    function clearInlineError(
        element
    ) {
        if (!element) {
            return;
        }

        element.textContent = '';
        hideElement(element);
    }

    function setButtonBusy(
        button,
        busy,
        busyText
    ) {
        if (!button) {
            return;
        }

        if (!button.dataset.defaultText) {
            button.dataset.defaultText =
                button.textContent.trim();
        }

        button.disabled = busy;

        button.textContent =
            busy
                ? busyText
                : button.dataset.defaultText;
    }

    function buildValidationMessage(
        errors
    ) {
        if (!errors
                || typeof errors !== 'object') {
            return '';
        }

        return Object.entries(errors)
            .map(([field, message]) =>
                `${field}: ${message}`
            )
            .join('\n');
    }

    function readErrorMessage(
        error,
        fallback
    ) {
        if (error
                && typeof error.message === 'string'
                && error.message.trim()) {
            return error.message.trim();
        }

        return fallback;
    }

    function notifySuccess(
        message
    ) {
        if (typeof showSuccessMessage
                === 'function') {
            showSuccessMessage(message);
            return;
        }

        if (typeof Toast !== 'undefined'
                && Toast
                && typeof Toast.success
                === 'function') {
            Toast.success(message);
            return;
        }

        console.log(message);
    }

    function notifyError(
        message
    ) {
        if (typeof showErrorMessage
                === 'function') {
            showErrorMessage(message);
            return;
        }

        if (typeof Toast !== 'undefined'
                && Toast
                && typeof Toast.error
                === 'function') {
            Toast.error(message);
            return;
        }

        console.error(message);
    }

    // =========================================================
    // GLOBAL ERP DATA SYNC — APPLICATIONS MODULE
    // =========================================================

    async function synchronizeApplicationsView(
            mutation = {}
    ) {
        /*
         * Return false when Applications is not the active DOM view.
         * The global registry will then allow another module handler
         * to process the mutation.
         */
        if (
            !view?.root
            || !document.body.contains(
                view.root
            )
        ) {
            return false;
        }

        const applicationId =
            Number(
                state.currentApplicationId
            );

        /*
         * Detail/profile mode:
         * reload the authoritative application profile and all workflow
         * resources in place. openApplication() already reloads details,
         * School Visit, Entrance Test, document state and transitions.
         */
        if (
            Number.isInteger(applicationId)
            && applicationId > 0
            && view?.detailComponent
            && !view.detailComponent
                .classList.contains('hidden')
        ) {
            const refreshed =
                await openApplication(
                    applicationId,
                    {
                        preservePosition: true,
                        silent: true
                    }
                );

            /*
             * Report the real refresh result to global.js. Returning true
             * unconditionally here previously masked failed/stale profile
             * refreshes and made the page appear dependent on manual reload.
             */
            return refreshed === true;
        }

        /*
         * List mode:
         * refresh data only. No loadView(), no browser refresh.
         */
        await loadApplications(
            true
        );

        return true;
    }

    if (
        typeof window.erpRegisterModuleSync
        === 'function'
    ) {
        window.erpRegisterModuleSync(
            'applications',
            synchronizeApplicationsView
        );
    }


    return {
        init
    };


})();

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
        showTableView();

        /*
         * Await the first server page so event.detail.waitUntil keeps the
         * existing dashboard global loader active until the table is ready.
         */
        await loadApplications();
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
            requestDocumentButton:
                byId('ba-requestAdditionalDocumentBtn'),
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
            minYear: new Date().getFullYear(),
            maxYear: new Date().getFullYear() + 3
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
                void loadApplications();
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

        view.requestDocumentButton?.addEventListener(
            'click',
            openAdditionalDocumentModal
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

    /**
     * Loads one server page of branch-scoped applications.
     */
    async function loadApplications() {
        if (!table) {
            return;
        }

        table.showLoading();

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
                    () => {
                        if (!Number.isInteger(applicationId)
                                || applicationId <= 0) {
                            notifyError(
                                'The selected application is invalid.'
                            );
                            return;
                        }

                        /*
                         * Open the existing Application Profile directly.
                         * Do not navigate, reload, or trigger the workflow
                         * action. openApplication() already uses the existing
                         * global loader and preserves all profile behavior.
                         */
                        void openApplication(
                            applicationId
                        );
                    }
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
        applicationId
    ) {
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

        view.detailComponent?.setAttribute(
            'aria-busy',
            'true'
        );

        view.detailComponent?.classList.add(
            'app-detail-loading'
        );

        if (typeof showLoader === 'function') {
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

            state.currentApplicationId =
                validatedApplicationId;

            state.currentApplication =
                application;

            renderApplicationDetails(application);
            await loadProfileTransitions(
                validatedApplicationId
            );

            hideElement(view.tableComponent);
            showElement(view.detailComponent);

            window.scrollTo({
                top: 0,
                behavior: 'auto'
            });

            return true;
        } catch (error) {
            notifyError(
                readErrorMessage(
                    error,
                    'Application profile could not be loaded.'
                )
            );

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
            application.dateOfRegistration
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

        setText(
            'view-schoolVisitAt',
            formatDateTime(application.schoolVisitAt)
        );

        setText(
            'view-schoolVisitRemarks',
            application.schoolVisitRemarks
        );

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

        view.profilePhoto.onload = () => {
            hideElement(
                view.profilePhotoPlaceholder
            );
            showElement(view.profilePhoto);
        };

        view.profilePhoto.onerror = () => {
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

        try {
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

        try {
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

        try {
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

        let aggregateStatus = 'PENDING';

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

    /**
     * Shows the table and hides the profile.
     */
    function showTableView() {
        showElement(view.tableComponent);
        hideElement(view.detailComponent);

        state.currentApplicationId = null;
        state.currentApplication = null;
        state.profileTransitions = [];
        renderProfileNextAction(null);

        window.scrollTo({
            top: 0,
            behavior: 'auto'
        });
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
                'This action is no longer available. Refresh the applications list.'
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
            state.profileTransitions[0];

        if (!transition
                || !state.currentApplicationId
                || !state.currentApplication) {
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

            await loadApplications();

            if (keepProfileOpen
                    && transitionResponse) {
                applyWorkflowResponseToProfile(
                    transitionResponse
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
            await loadApplications();
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
        transition
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
                    publicRemarks: null,
                    internalRemarks: null,
                    notifyApplicant:
                        transition.applicantNotificationRequired === true
                        || transition.applicantNotificationSupported === true
                }
            );

        return unwrapResponseData(response);
    }

    async function loadProfileTransitions(
        applicationId
    ) {
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

        renderProfileNextAction(
            state.profileTransitions[0] || null
        );
    }

    function renderProfileNextAction(
        transition
    ) {
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

        view.profileNextStageButton.classList.toggle(
            'hidden',
            !transition
        );
        view.profileNextStageButton.disabled =
            !transition;
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

        const date =
            new Date(value);

        if (Number.isNaN(date.getTime())) {
            return String(value);
        }

        const options =
            includeTime
                ? {
                    dateStyle: 'medium',
                    timeStyle: 'short'
                }
                : {
                    dateStyle: 'medium'
                };

        return new Intl.DateTimeFormat(
            undefined,
            options
        ).format(date);
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

    return {
        init
    };
})();

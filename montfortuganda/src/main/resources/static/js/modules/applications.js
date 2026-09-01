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
        feeDiscussion: null,
        feeDiscussionModalMode: null,
        feeDiscussionStep: 1,
        feeDiscussionScholarshipSaved: false,
        feeDiscussionOriginalFeeAmounts: null,
        feeCreateModalAutoOpenedForApplicationId: null,
        documentSyncTimer: null,
        documentSyncBusy: false,
        documentSyncSignature: null,
        documentSyncApplicationId: null,
        profileLoadBusy: false,
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
     * Returns an element from the current Applications view.
     * Falls back to document lookup for safety.
     */
    function byId(id) {
        if (!id) {
            return null;
        }

        return (
            view?.root?.querySelector(`#${id}`)
            || document.getElementById(id)
        );
    }

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
        state.feeDiscussion = null;
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
        const localById = id =>
            root.querySelector(`#${id}`);

        return {
            root,

            tableComponent:
                localById('ba-appTableComponent'),
            detailComponent:
                localById('ba-appDetailComponent'),
            detailContent:
                localById('ba-appDetailContent'),

            table:
                localById('ba-appTable'),
            tableBody:
                localById('ba-appTableBody'),
            rowTemplate:
                localById('tpl-app-row'),

            searchKeyword:
                localById('ba-appSearchKeyword'),
            searchGender:
                localById('ba-appSearchGender'),
            searchLevel:
                localById('ba-appSearchLevel'),
            searchClass:
                localById('ba-appSearchClass'),
            searchStage:
                localById('ba-appSearchStage'),
            searchDocumentStatus:
                localById('ba-appSearchDocumentStatus'),
            searchScholarship:
                localById('ba-appSearchScholarship'),
            searchStatus:
                localById('ba-appSearchStatus'),
            searchFromDate:
                localById('ba-appSearchFromDate'),
            searchToDate:
                localById('ba-appSearchToDate'),
            moreFiltersButton:
                localById('ba-appMoreFiltersBtn'),
            advancedFilters:
                localById('ba-appAdvancedFilters'),
            activeFilterCount:
                localById('ba-appActiveFilterCount'),
            searchButton:
                localById('ba-appSearchBtn'),
            resetButton:
                localById('ba-appResetBtn'),
            refreshButton:
                localById('ba-refreshAppsBtn'),

            selectPage:
                localById('ba-appSelectPage'),
            selectedCount:
                localById('ba-appSelectedCount'),
            bulkNextActionButton:
                localById('ba-appBulkNextActionBtn'),
            bulkClearButton:
                localById('ba-appBulkClearBtn'),

            pageSize:
                localById('ba-appPageSize'),
            pageInfo:
                localById('ba-appPageInfo'),
            previousPageButton:
                localById('ba-appPrevPageBtn'),
            nextPageButton:
                localById('ba-appNextPageBtn'),

            backButton:
                localById('ba-backToAppTableBtn'),
            refreshDetailButton:
                localById('ba-refreshAppDetailBtn'),
            printButton:
                localById('ba-printAppBtn'),
            requestDocumentInlineButton:
                localById(
                    'ba-requestAdditionalDocumentInlineBtn'
                ),
            profileNextStageButton:
                localById('ba-appNextStageBtn'),
            profileNextStageButtonLabel:
                localById('ba-appNextStageBtnLabel'),

            profilePhoto:
                localById('view-appProfilePhoto'),
            profilePhotoPlaceholder:
                localById(
                    'view-appProfilePhotoPlaceholder'
                ),
            workflowProgress:
                localById('ba-workflowProgress'),

            documentsContainer:
                localById(
                    'application-documents-view-container'
                ),
            documentCount:
                localById('application-document-count'),

            documentRequestsContainer:
                localById(
                    'application-document-requests-container'
                ),
            documentRequestCount:
                localById(
                    'application-document-request-count'
                ),

            historyContainer:
                localById('application-history-container'),
            historyCount:
                localById('application-history-count'),

            schoolVisitSection:
                localById('application-school-visit-section'),
            schoolVisitStatusCaption:
                localById('school-visit-status-caption'),
            schoolVisitStageMessage:
                localById('school-visit-stage-message'),
            schoolVisitStatus:
                localById('view-schoolVisitStatus'),
            schoolVisitEmployee:
                localById('view-schoolVisitEmployee'),
            schoolVisitEmployeeNo:
                localById('view-schoolVisitEmployeeNo'),
            schoolVisitScheduledAt:
                localById('view-schoolVisitScheduledAt'),
            schoolVisitVisitedAt:
                localById('view-schoolVisitAt'),
            schoolVisitStudentAttendance:
                localById('view-schoolVisitStudentAttendance'),
            schoolVisitParentAttendance:
                localById('view-schoolVisitParentAttendance'),
            schoolVisitRemarks:
                localById('view-schoolVisitRemarks'),
            schoolVisitScheduleButton:
                localById('ba-schoolVisitScheduleBtn'),
            schoolVisitRescheduleButton:
                localById('ba-schoolVisitRescheduleBtn'),
            schoolVisitCompleteButton:
                localById('ba-schoolVisitCompleteBtn'),

            schoolVisitScheduleModal:
                localById('ba-schoolVisitScheduleModal'),
            schoolVisitScheduleForm:
                localById('ba-schoolVisitScheduleForm'),
            schoolVisitScheduleTitle:
                localById('ba-schoolVisitScheduleTitle'),
            schoolVisitScheduleSubtitle:
                localById('ba-schoolVisitScheduleSubtitle'),
            schoolVisitScheduledAtInput:
                localById('ba-schoolVisitScheduledAtInput'),
            schoolVisitScheduleRemarks:
                localById('ba-schoolVisitScheduleRemarks'),
            schoolVisitScheduleError:
                localById('ba-schoolVisitScheduleError'),
            schoolVisitCancelScheduleButton:
                localById('ba-cancelSchoolVisitScheduleBtn'),
            schoolVisitCloseScheduleButton:
                localById('ba-closeSchoolVisitScheduleBtn'),
            schoolVisitSaveScheduleButton:
                localById('ba-saveSchoolVisitScheduleBtn'),

            schoolVisitCompleteModal:
                localById('ba-schoolVisitCompleteModal'),
            schoolVisitCompleteForm:
                localById('ba-schoolVisitCompleteForm'),
            schoolVisitCompleteEmployeeSelect:
                localById('ba-schoolVisitCompleteEmployeeId'),
            schoolVisitVisitedAtInput:
                localById('ba-schoolVisitVisitedAtInput'),
            schoolVisitStudentAttended:
                localById('ba-schoolVisitStudentAttended'),
            schoolVisitParentAttended:
                localById('ba-schoolVisitParentAttended'),
            schoolVisitCompleteRemarks:
                localById('ba-schoolVisitCompleteRemarks'),
            schoolVisitCompleteError:
                localById('ba-schoolVisitCompleteError'),
            schoolVisitCancelCompleteButton:
                localById('ba-cancelSchoolVisitCompleteBtn'),
            schoolVisitCloseCompleteButton:
                localById('ba-closeSchoolVisitCompleteBtn'),
            schoolVisitConfirmCompleteButton:
                localById('ba-confirmSchoolVisitCompleteBtn'),

            feeDiscussionSection:
                localById('application-fee-discussion-section'),
            feeInitialForm:
                localById('ba-feeInitialForm'),
            feeCreatePrompt:
                localById('ba-feeCreatePrompt'),
            feeOpenDiscussionButton:
                localById('ba-openFeeDiscussionBtn'),
            feeSavedView:
                localById('ba-feeSavedView'),
            feeTermFee:
                localById('ba-feeTermFee'),
            feeTransportFee:
                localById('ba-feeTransportFee'),
            feeHostelFee:
                localById('ba-feeHostelFee'),
            feeUniformFee:
                localById('ba-feeUniformFee'),
            feeBooksFee:
                localById('ba-feeBooksFee'),
            feeAdmissionFee:
                localById('ba-feeAdmissionFee'),
            feeOtherFee:
                localById('ba-feeOtherFee'),
            feeBaseFeeAmount:
                localById('ba-feeBaseFeeAmount'),
            feeDiscussionRemarks:
                localById('ba-feeDiscussionRemarks'),
            feeDecisionMainOptions:
                Array.from(
                    root.querySelectorAll(
                        'input[name="ba-feeDecisionMainOption"]'
                    )
                ),
            feeDecisionOptions:
                Array.from(
                    root.querySelectorAll(
                        'input[name="ba-feeDecisionOption"]'
                    )
                ),
            feeContributionPanel:
                localById('ba-feeContributionPanel'),
            feeParentCanPay:
                localById('ba-feeParentCanPay'),
            feeAssistanceRequired:
                localById('ba-feeAssistanceRequired'),
            feeDiscussionEditButton:
                localById('ba-editFeeDiscussionBtn'),
            feeDiscussionFinalizeButton:
                localById('ba-finalizeFeeDiscussionBtn'),
            feeDiscussionCancelApplicationButton:
                localById('ba-cancelApplicationFromFeeBtn'),
            cancelApplicationFeeModal:
                localById('ba-cancelApplicationFeeModal'),
            cancelApplicationFeeReason:
                localById('ba-cancelApplicationFeeReason'),
            cancelApplicationFeeError:
                localById('ba-cancelApplicationFeeError'),
            closeCancelApplicationFeeButton:
                localById('ba-closeCancelApplicationFeeBtn'),
            abortCancelApplicationFeeButton:
                localById('ba-abortCancelApplicationFeeBtn'),
            confirmCancelApplicationFeeButton:
                localById('ba-confirmCancelApplicationFeeBtn'),
            feeDiscussionSaveButton:
                localById('ba-saveFeeDiscussionBtn'),
            feeDiscussionSavedSummary:
                localById('ba-feeDiscussionSavedSummary'),

            scholarshipSection:
                localById('application-scholarship-section'),
            scholarshipFormStatus:
                localById('ba-scholarshipFormStatus'),
            scholarshipStartPanel:
                localById('ba-scholarshipStartPanel'),
            scholarshipFillAtSchoolButton:
                localById('ba-scholarshipFillAtSchoolBtn'),
            scholarshipSendLinkButton:
                localById('ba-scholarshipSendLinkBtn'),
            feeViewTermFee:
                localById('view-feeTermFee'),
            feeViewTransportFee:
                localById('view-feeTransportFee'),
            feeViewHostelFee:
                localById('view-feeHostelFee'),
            feeViewUniformFee:
                localById('view-feeUniformFee'),
            feeViewBooksFee:
                localById('view-feeBooksFee'),
            feeViewAdmissionFee:
                localById('view-feeAdmissionFee'),
            feeViewOtherFee:
                localById('view-feeOtherFee'),
            feeViewBaseFeeAmount:
                localById('view-feeBaseFeeAmount'),
            feeViewDecision:
                localById('view-feeDecision'),
            feeViewParentCanPay:
                localById('view-feeParentCanPay'),
            feeViewAssistanceRequired:
                localById('view-feeAssistanceRequired'),
            feeViewDiscussionRemarks:
                localById('view-feeDiscussionRemarks'),

            feeEditModal:
                localById('ba-feeEditModal'),
            feeEditTitle:
                localById('ba-feeEditTitle'),
            feeEditSubtitle:
                localById('ba-feeEditSubtitle'),
            feeEditForm:
                localById('ba-feeEditForm'),
            feeEditCloseButton:
                localById('ba-closeFeeEditModalBtn'),
            feeEditCancelButton:
                localById('ba-cancelFeeEditBtn'),
            feeEditSaveButton:
                localById('ba-saveFeeEditBtn'),
            feeEditSaveButtonLabel:
                localById('ba-saveFeeEditBtnLabel'),
            feeEditBackButton:
                localById('ba-feeEditBackBtn'),
            feeEditNextButton:
                localById('ba-feeEditNextBtn'),
            feeConfirmDetails:
                localById('ba-feeConfirmDetails'),
            feeStepIndicator1:
                localById('ba-feeStepIndicator1'),
            feeStepIndicator2:
                localById('ba-feeStepIndicator2'),
            feeStepIndicator3:
                localById('ba-feeStepIndicator3'),
            feeStep1:
                localById('ba-feeStep1'),
            feeStep2:
                localById('ba-feeStep2'),
            feeStep3:
                localById('ba-feeStep3'),
            feeStep2Total:
                localById('ba-feeStep2Total'),
            feeStep3Total:
                localById('ba-feeStep3Total'),
            feeScholarshipTypePanel:
                localById('ba-feeScholarshipTypePanel'),
            feeDecisionScholarship:
                localById('ba-feeDecisionScholarship'),
            feeScholarshipMethodSchool:
                localById('ba-feeScholarshipMethodSchool'),
            feeScholarshipMethodEmail:
                localById('ba-feeScholarshipMethodEmail'),
            feeEditError:
                localById('ba-feeEditError'),
            editFeeTermFee:
                localById('ba-editFeeTermFee'),
            editFeeTransportFee:
                localById('ba-editFeeTransportFee'),
            editFeeHostelFee:
                localById('ba-editFeeHostelFee'),
            editFeeUniformFee:
                localById('ba-editFeeUniformFee'),
            editFeeBooksFee:
                localById('ba-editFeeBooksFee'),
            editFeeAdmissionFee:
                localById('ba-editFeeAdmissionFee'),
            editFeeOtherFee:
                localById('ba-editFeeOtherFee'),
            editFeeBaseFeeAmount:
                localById('ba-editFeeBaseFeeAmount'),
            editFeeDecisionMainOptions:
                Array.from(
                    root.querySelectorAll(
                        'input[name="ba-feeDecisionMainOption"]'
                    )
                ),
            editFeeDecisionOptions:
                Array.from(
                    root.querySelectorAll(
                        'input[name="ba-feeDecisionOption"]'
                    )
                ),
            editFeeContributionPanel:
                localById('ba-editFeeContributionPanel'),
            editFeeParentCanPay:
                localById('ba-editFeeParentCanPay'),
            editFeeAssistanceRequired:
                localById('ba-editFeeAssistanceRequired'),
            editFeeParentContributionHelp:
                localById('ba-editFeeParentContributionHelp'),
            editFeeScholarshipMethodPanel:
                localById('ba-editFeeScholarshipMethodPanel'),
            editFeeScholarshipMethodHelp:
                localById('ba-editFeeScholarshipMethodHelp'),
            editFeeFillInSchoolButton:
                localById('ba-editFeeFillInSchoolBtn'),
            editFeeSendLinkButton:
                localById('ba-editFeeSendLinkBtn'),
            editFeeDiscussionRemarks:
                localById('ba-editFeeDiscussionRemarks'),
            editFeeChangeReasonGroup:
                localById('ba-editFeeChangeReasonGroup'),
            editFeeChangeReason:
                localById('ba-editFeeChangeReason'),

            entranceTestSection:
                localById('application-entrance-test-section'),
            entranceTestEnterMarksButton:
                localById('ba-entranceTestEnterMarksBtn'),
            entranceTestUpdateResultButton:
                localById('ba-entranceTestUpdateResultBtn'),
            entranceTestRetestButton:
                localById('ba-entranceTestRetestBtn'),
            entranceTestStatus:
                localById('view-entranceTestStatus'),
            entranceTestResult:
                localById('view-entranceTestResult'),
            entranceTestEmployee:
                localById('view-entranceTestEmployee'),
            entranceTestCompletedAt:
                localById('view-entranceTestCompletedAt'),
            entranceTestRemarks:
                localById('view-entranceTestRemarks'),
            entranceTestMarksBlock:
                localById('view-entranceTestMarksBlock'),
            entranceTestMarksBody:
                localById('view-entranceTestMarksBody'),
            entranceTestMarksTotal:
                localById('view-entranceTestMarksTotal'),
            entranceTestMarksPercentage:
                localById('view-entranceTestMarksPercentage'),

            entranceTestMarksModal:
                localById('ba-entranceTestMarksModal'),
            entranceTestMarksForm:
                localById('ba-entranceTestMarksForm'),
            entranceTestMarksRows:
                localById('ba-entranceTestMarksRows'),
            entranceTestAddSubjectButton:
                localById('ba-addEntranceTestSubjectBtn'),
            entranceTestResultSelect:
                localById('ba-entranceTestResult'),
            entranceTestCompletedAtInput:
                localById('ba-entranceTestCompletedAt'),
            entranceTestEmployeeRemarks:
                localById('ba-entranceTestEmployeeRemarks'),
            entranceTestInternalRemarks:
                localById('ba-entranceTestInternalRemarks'),
            entranceTestMarksError:
                localById('ba-entranceTestMarksError'),
            entranceTestLiveMaximum:
                localById('ba-entranceTestLiveMaximum'),
            entranceTestLiveObtained:
                localById('ba-entranceTestLiveObtained'),
            entranceTestLivePercentage:
                localById('ba-entranceTestLivePercentage'),
            entranceTestCloseMarksButton:
                localById('ba-closeEntranceTestMarksBtn'),
            entranceTestCancelMarksButton:
                localById('ba-cancelEntranceTestMarksBtn'),

            waitlistResultModal:
                localById('ba-waitlistResultModal'),
            waitlistResultForm:
                localById('ba-waitlistResultForm'),
            waitlistResultMarksBody:
                localById('ba-waitlistResultMarksBody'),
            waitlistResultTitle:
                localById('ba-waitlistResultTitle'),
            waitlistResultDescription:
                localById('ba-waitlistResultDescription'),
            waitlistFinalDecisionGroup:
                localById('ba-waitlistFinalDecisionGroup'),
            waitlistFinalResultSelect:
                localById('ba-waitlistFinalResult'),
            waitlistCurrentResultInput:
                localById('ba-waitlistCurrentResult'),
            waitlistResultRemarksLabel:
                localById('ba-waitlistResultRemarksLabel'),
            waitlistResultRemarks:
                localById('ba-waitlistResultRemarks'),
            waitlistResultError:
                localById('ba-waitlistResultError'),
            waitlistResultCloseButton:
                localById('ba-closeWaitlistResultBtn'),
            waitlistResultCancelButton:
                localById('ba-cancelWaitlistResultBtn'),
            waitlistResultSaveButton:
                localById('ba-saveWaitlistResultBtn'),
            entranceTestSaveMarksButton:
                localById('ba-saveEntranceTestMarksBtn'),

            entranceTestRetestModal:
                localById('ba-entranceTestRetestModal'),
            entranceTestRetestForm:
                localById('ba-entranceTestRetestForm'),
            entranceTestRetestScheduledAt:
                localById('ba-entranceTestRetestScheduledAt'),
            entranceTestRetestRemarks:
                localById('ba-entranceTestRetestRemarks'),
            entranceTestRetestError:
                localById('ba-entranceTestRetestError'),
            entranceTestRetestCloseButton:
                localById('ba-closeEntranceTestRetestBtn'),
            entranceTestRetestCancelButton:
                localById('ba-cancelEntranceTestRetestBtn'),
            entranceTestRetestSaveButton:
                localById('ba-saveEntranceTestRetestBtn'),

            reviewModal:
                localById('ba-documentReviewModal'),
            reviewForm:
                localById('ba-documentReviewForm'),
            reviewDocumentId:
                localById('ba-reviewDocumentId'),
            reviewSubtitle:
                localById('ba-documentReviewSubtitle'),
            reviewDecision:
                localById('ba-reviewDecision'),
            rejectionReasonGroup:
                localById('ba-rejectionReasonGroup'),
            rejectionReason:
                localById('ba-rejectionReason'),
            reuploadReasonGroup:
                localById('ba-reuploadReasonGroup'),
            reuploadReason:
                localById('ba-reuploadReason'),
            reuploadDeadlineGroup:
                localById('ba-reuploadDeadlineGroup'),
            reuploadDeadline:
                localById('ba-reuploadDeadline'),
            reviewPublicRemarks:
                localById('ba-reviewPublicRemarks'),
            reviewInternalRemarks:
                localById('ba-reviewInternalRemarks'),
            reviewError:
                localById('ba-documentReviewError'),
            cancelReviewButton:
                localById('ba-cancelDocumentReviewBtn'),
            submitReviewButton:
                localById('ba-submitDocumentReviewBtn'),

            requestModal:
                localById('ba-additionalDocumentModal'),
            requestForm:
                localById('ba-additionalDocumentForm'),
            requestDocumentType:
                localById('ba-requestDocumentType'),
            requestDocumentName:
                localById('ba-requestDocumentName'),
            requestReason:
                localById('ba-requestReason'),
            requestPublicRemarks:
                localById('ba-requestPublicRemarks'),
            requestInternalRemarks:
                localById('ba-requestInternalRemarks'),
            requestUploadDeadline:
                localById('ba-requestUploadDeadline'),
            requestError:
                localById('ba-additionalDocumentError'),
            closeRequestModalButton:
                localById('ba-closeAdditionalDocumentModalBtn'),
            cancelRequestFormButton:
                localById('ba-cancelAdditionalDocumentBtn'),
            submitRequestButton:
                localById('ba-submitAdditionalDocumentBtn'),

            cancelRequestModal:
                localById('ba-cancelDocumentRequestModal'),
            cancelRequestForm:
                localById('ba-cancelDocumentRequestForm'),
            cancelRequestId:
                localById('ba-cancelDocumentRequestId'),
            cancelRequestSubtitle:
                localById(
                    'ba-cancelDocumentRequestSubtitle'
                ),
            cancellationReason:
                localById(
                    'ba-documentRequestCancellationReason'
                ),
            cancellationError:
                localById('ba-cancelDocumentRequestError'),
            abortCancellationButton:
                localById(
                    'ba-abortCancelDocumentRequestBtn'
                ),
            confirmCancellationButton:
                localById(
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

        createErpCalendar(
            '#ba-entranceTestRetestScheduledAt',
            {
                ...commonDeadlineConfig,
                defaultHour: 9,
                defaultMinute: 0
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

        [
            view.feeTermFee,
            view.feeTransportFee,
            view.feeHostelFee,
            view.feeUniformFee,
            view.feeBooksFee,
            view.feeAdmissionFee,
            view.feeOtherFee,
            view.feeParentCanPay
        ].forEach(input => {
            input?.addEventListener(
                'input',
                updateFeeDiscussionAmounts
            );
        });

        view.feeOpenDiscussionButton?.addEventListener(
            'click',
            openFeeCreateModal
        );

        view.feeDiscussionEditButton?.addEventListener(
            'click',
            openFeeEditModal
        );

        view.feeDiscussionFinalizeButton?.addEventListener(
            'click',
            () => {
                void finalizeFeeDiscussion();
            }
        );

        view.feeDiscussionCancelApplicationButton?.addEventListener(
            'click',
            openCancelApplicationFromFeeModal
        );

        view.closeCancelApplicationFeeButton?.addEventListener(
            'click',
            closeCancelApplicationFromFeeModal
        );

        view.abortCancelApplicationFeeButton?.addEventListener(
            'click',
            closeCancelApplicationFromFeeModal
        );

        view.confirmCancelApplicationFeeButton?.addEventListener(
            'click',
            () => {
                void cancelApplicationFromFeeDiscussion();
            }
        );

        view.cancelApplicationFeeModal?.addEventListener(
            'click',
            event => {
                if (event.target === view.cancelApplicationFeeModal) {
                    closeCancelApplicationFromFeeModal();
                }
            }
        );

        view.feeDiscussionSaveButton?.addEventListener(
            'click',
            () => {
                void saveFeeDiscussion();
            }
        );

        [
            view.editFeeTermFee,
            view.editFeeTransportFee,
            view.editFeeHostelFee,
            view.editFeeUniformFee,
            view.editFeeBooksFee,
            view.editFeeAdmissionFee,
            view.editFeeOtherFee
        ].forEach(input => {
            input?.addEventListener(
                'input',
                () => {
                    if (view.feeConfirmDetails) {
                        view.feeConfirmDetails.checked = false;
                    }
                    updateFeeEditAmounts();
                    setFeeEditError('');
                    updateFeeDiscussionWizardButtonState();
                }
            );
        });

        view.editFeeParentCanPay?.addEventListener(
            'input',
            () => {
                updateFeeEditAmounts();
                setFeeEditError('');
                updateFeeDiscussionWizardButtonState();
            }
        );

        view.feeConfirmDetails?.addEventListener(
            'change',
            () => {
                setFeeEditError('');
                updateFeeDiscussionWizardButtonState();
            }
        );

        view.editFeeDiscussionRemarks?.addEventListener(
            'input',
            () => {
                setFeeEditError('');
                updateFeeDiscussionWizardButtonState();
            }
        );

        view.editFeeDecisionMainOptions.forEach(option => {
            option.addEventListener(
                'change',
                () => {
                    const selected =
                        String(option.value || '').toUpperCase();

                    if (selected === 'FULL_PAYMENT') {
                        view.editFeeDecisionOptions.forEach(item => {
                            item.checked = false;
                        });

                        if (view.feeScholarshipMethodSchool) {
                            view.feeScholarshipMethodSchool.checked = false;
                        }
                        if (view.feeScholarshipMethodEmail) {
                            view.feeScholarshipMethodEmail.checked = false;
                        }
                    } else if (selected === 'PENDING') {
                        view.editFeeDecisionOptions.forEach(item => {
                            item.checked = false;
                        });

                        if (view.feeScholarshipMethodSchool) {
                            view.feeScholarshipMethodSchool.checked = false;
                        }
                        if (view.feeScholarshipMethodEmail) {
                            view.feeScholarshipMethodEmail.checked = false;
                        }
                    } else if (selected === 'SCHOLARSHIP') {
                        if (view.feeScholarshipMethodSchool) {
                            view.feeScholarshipMethodSchool.checked = false;
                        }
                        if (view.feeScholarshipMethodEmail) {
                            view.feeScholarshipMethodEmail.checked = false;
                        }
                    }

                    renderFeeEditDecisionFields(
                        getFeeEditDecision()
                    );
                    updateFeeEditAmounts();
                    setFeeEditError('');
                    updateFeeDiscussionWizardButtonState();
                    setFeeDiscussionStep(2);
                }
            );
        });

        view.editFeeDecisionOptions.forEach(option => {
            option.addEventListener(
                'change',
                () => {
                    const selected =
                        String(option.value || '').toUpperCase();

                    if (
                        selected === 'PARTIAL_ASSISTANCE'
                        || selected === 'FULL_ASSISTANCE'
                    ) {
                        const scholarshipMain =
                            view.editFeeDecisionMainOptions.find(item =>
                                String(item.value || '').toUpperCase()
                                    === 'SCHOLARSHIP'
                            );

                        if (scholarshipMain) {
                            scholarshipMain.checked = true;
                        }
                    }

                    renderFeeEditDecisionFields(
                        getFeeEditDecision()
                    );
                    updateFeeEditAmounts();
                    setFeeEditError('');
                    updateFeeDiscussionWizardButtonState();
                    setFeeDiscussionStep(
                        state.feeDiscussionStep || 2
                    );
                }
            );
        });

        view.feeScholarshipMethodSchool?.addEventListener(
            'change',
            () => {
                setFeeEditError('');
                updateFeeDiscussionWizardButtonState();
            }
        );

        view.feeScholarshipMethodEmail?.addEventListener(
            'change',
            () => {
                setFeeEditError('');
                updateFeeDiscussionWizardButtonState();
            }
        );

        view.feeEditNextButton?.addEventListener(
            'click',
            () => {
                void handleFeeDiscussionWizardNext();
            }
        );

        view.feeEditBackButton?.addEventListener(
            'click',
            () => {
                const step = state.feeDiscussionStep || 1;
                if (step === 3) {
                    setFeeDiscussionStep(2);
                } else if (step === 2) {
                    setFeeDiscussionStep(1);
                }
                setFeeEditError('');
            }
        );

        view.feeEditCloseButton?.addEventListener(
            'click',
            closeFeeEditModal
        );

        view.feeEditCancelButton?.addEventListener(
            'click',
            closeFeeEditModal
        );

        view.feeEditForm?.addEventListener(
            'submit',
            event => {
                event.preventDefault();
                const step = state.feeDiscussionStep || 1;
                const mainDecision =
                    view.editFeeDecisionMainOptions.find(option =>
                        option.checked
                    );

                const mainValue =
                    String(mainDecision?.value || '')
                        .trim()
                        .toUpperCase();

                const saveAtStep2 =
                    step === 2
                    && (
                        mainValue === 'FULL_PAYMENT'
                        || (
                            state.feeDiscussionModalMode === 'CREATE'
                            && mainValue === 'DECIDE_LATER'
                        )
                    );

                if (saveAtStep2) {
                    void saveFeeDiscussionModal();
                    return;
                }

                if (step === 3) {
                    void processFeeDiscussionScholarshipStep();
                    return;
                }

                void handleFeeDiscussionWizardNext();
            }
        );

        bindBackdropClose(
            view.feeEditModal,
            closeFeeEditModal
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

        view.entranceTestRetestButton?.addEventListener(
            'click',
            openEntranceTestRetestModal
        );

        view.entranceTestRetestCloseButton?.addEventListener(
            'click',
            closeEntranceTestRetestModal
        );

        view.entranceTestRetestCancelButton?.addEventListener(
            'click',
            closeEntranceTestRetestModal
        );

        view.entranceTestRetestForm?.addEventListener(
            'submit',
            event => {
                event.preventDefault();
                void submitEntranceTestRetest();
            }
        );

        bindBackdropClose(
            view.entranceTestRetestModal,
            closeEntranceTestRetestModal
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

        const currentStageStatus =
            String(
                record.currentStageStatus
                || ''
            ).trim();

        setNodeText(
            node.querySelector('.app-document-label'),
            currentStageStatus
                || resolveLegacyStageStatus(record)
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

        /*
         * Prevent background document polling from overlapping the
         * authoritative profile reload. openApplication() already performs
         * several API calls; allowing the timed document poll to fire at the
         * same time creates unnecessary concurrent DB transactions.
         */
        stopDocumentAutoSync();
        state.profileLoadBusy = true;

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
                feeDiscussionLoaded,
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
                loadFeeDiscussion(
                    validatedApplicationId,
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

            state.feeDiscussion =
                feeDiscussionLoaded || null;

            renderFeeDiscussion(
                state.feeDiscussion
            );

            renderScholarshipSection(
                application
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

            view.root?.classList.add(
                'app-profile-mode'
            );

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
            state.profileLoadBusy = false;

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

    function resolveProfileStageStatus(application) {
        return (
            application?.currentStageStatus
            || resolveLegacyStageStatus(application)
            || '—'
        );
    }

    function renderWorkflowProgress(profile) {
    const container =
        document.getElementById('ba-workflowProgress');

    if (!container) {
        return;
    }

    /*
     * Backend is the workflow authority.
     *
     * The profile/transition response can contain the authoritative
     * currentStage plus stage-specific sub-status fields. This renderer
     * only presents those values; it never calculates a new workflow path.
     */
    const currentStage =
        normalizeStageCode(
            profile?.currentStage
        );

    const stageDefinitions = [
        {
            code: 'APPLICATION_VERIFICATION',
            label: 'Application Verification',
            subStage: getBackendSubStage(
                profile,
                'APPLICATION_VERIFICATION'
            )
        },
        {
            code: 'SCHOOL_VISIT',
            label: 'School Visit',
            subStage: getBackendSubStage(
                profile,
                'SCHOOL_VISIT'
            )
        },
        {
            code: 'ENTRANCE_TEST',
            label: 'Entrance Test',
            subStage: getBackendSubStage(
                profile,
                'ENTRANCE_TEST'
            )
        },
        {
            code: 'PARENT_FEE_DISCUSSION',
            label: 'Parent Fee Discussion',
            subStage: getBackendSubStage(
                profile,
                'PARENT_FEE_DISCUSSION'
            )
        },
        {
            code: 'SCHOLARSHIP',
            label: 'Scholarship',
            subStage: getBackendSubStage(
                profile,
                'SCHOLARSHIP'
            )
        },
        {
            code: 'PAYMENT',
            label: 'Payment',
            subStage: getBackendSubStage(
                profile,
                'PAYMENT'
            )
        },
        {
            code: 'FINAL_ADMISSION',
            label: 'Final Admission',
            subStage: getBackendSubStage(
                profile,
                'FINAL_ADMISSION'
            )
        },
        {
            code: 'ENROLLED',
            label: 'Enrolled',
            subStage: getBackendSubStage(
                profile,
                'ENROLLED'
            )
        }
    ];

    const currentIndex =
        stageDefinitions.findIndex(
            stage =>
                stage.code === currentStage
        );

    container.innerHTML =
        stageDefinitions.map(
            (stage, index) => {
                const isCurrent =
                    stage.code === currentStage;

                const isCompleted =
                    currentIndex >= 0
                    && index < currentIndex;

                const stateClass =
                    isCurrent
                        ? 'current'
                        : isCompleted
                            ? 'completed'
                            : 'pending';

                const subStageText =
                    stage.subStage
                        ? escapeHtml(
                            formatBackendSubStage(
                                stage.subStage
                            )
                        )
                        : '';

                return `
                    <div class="workflow-step ${stateClass}"
                         data-stage="${escapeHtml(stage.code)}">
                        <div class="workflow-step-label">
                            ${escapeHtml(stage.label)}
                        </div>
                        ${
                            subStageText
                                ? `<div class="workflow-step-substage">${subStageText}</div>`
                                : ''
                        }
                    </div>
                `;
            }
        ).join('');
}

function normalizeStageCode(value) {
    if (value === null
            || value === undefined) {
        return '';
    }

    return String(value)
        .trim()
        .toUpperCase();
}

function getBackendSubStage(
    profile,
    stageCode
) {
    if (!profile) {
        return '';
    }

    /*
     * These fields are supplied by the backend workflow/profile response.
     * Do not infer a sub-stage from unrelated fields when the backend has
     * not supplied one.
     */
    const fieldMap = {
        APPLICATION_VERIFICATION:
            'verificationStatus',
        SCHOOL_VISIT:
            'schoolVisitStatus',
        ENTRANCE_TEST:
            'testStatus',
        PARENT_FEE_DISCUSSION:
            'feeDecisionStatus',
        SCHOLARSHIP:
            'scholarshipWorkflowStatus',
        PAYMENT:
            'paymentStatus',
        FINAL_ADMISSION:
            'admissionStatus',
        ENROLLED:
            'admissionStatus'
    };

    const field =
        fieldMap[stageCode];

    if (!field) {
        return '';
    }

    return profile[field] ?? '';
}

function formatBackendSubStage(value) {
    if (value === null
            || value === undefined
            || String(value).trim() === '') {
        return '';
    }

    return String(value)
        .trim()
        .toLowerCase()
        .split('_')
        .map(
            word =>
                word
                    ? word.charAt(0).toUpperCase()
                        + word.slice(1)
                    : ''
        )
        .join(' ');
}

    /**
     * Renders all application profile sections.
     *
     * @param {Object} application
     */
    function buildProfileClassLevel(application) {
        const className =
            String(
                application?.className
                || ''
            ).trim();

        const levelName =
            String(
                application?.levelName
                || application?.classLevelName
                || application?.level
                || ''
            ).trim();

        const parts =
            [className, levelName]
                .filter(Boolean);

        return parts.length
            ? parts.join(' • ')
            : '—';
    }

    function updateCompactSchoolVisitSummary(schoolVisit) {
        if (!schoolVisit) {
            setText(
                'summary-appSchoolVisit',
                formatEnum(
                    state.currentApplication?.schoolVisitStatus
                )
            );

            setText(
                'summary-appResponsibleEmployee',
                '—'
            );

            setText(
                'summary-appResponsibleEmployeeNo',
                '—'
            );

            return;
        }

        setText(
            'summary-appSchoolVisit',
            formatEnum(
                schoolVisit.schoolVisitStatus
            )
        );

        setText(
            'summary-appResponsibleEmployee',
            schoolVisit.employeeName
            || '—'
        );

        setText(
            'summary-appResponsibleEmployeeNo',
            schoolVisit.employeeNo
            || '—'
        );
    }

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
            buildProfileClassLevel(application)
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
            'summary-appCurrentStageStatus',
            resolveProfileStageStatus(application)
        );

        updateCompactSchoolVisitSummary(
            state.schoolVisit
        );

        setText(
            'summary-appSubmitted',
            application.createdAt
                ? formatDateTime(
                    application.createdAt,
                    false
                )
                : (
                    application.dateOfRegistration
                    || '—'
                )
        );

        setText(
            'summary-appUpdated',
            application.updatedAt
                ? formatDateTime(application.updatedAt)
                : '—'
        );

        renderWorkflowProgress(application);

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

        renderPreviousSubjectMarks(
            application
        );

        /*
         * Previous-school academic results are dependent on the student's
         * applied class. The public application form already collects:
         *
         * Subject Marks are independent of class. They are rendered from
         * the stored JSON data by renderPreviousSubjectMarks().
         * Class-based presentation applies only to PLE/UCE.
         */
        renderPreviousSchoolResultsForClass(
            application
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



    function renderPreviousSubjectMarks(
        application
    ) {
        const group =
            document.getElementById(
                'view-subjectMarksGroup'
            );

        const body =
            document.getElementById(
                'view-subjectMarksBody'
            );

        const totalElement =
            document.getElementById(
                'view-subjectMarksTotal'
            );

        if (!group || !body || !totalElement) {
            return;
        }

        const subjectMarks =
            parsePreviousSubjectMarks(
                application?.subjectMarks
            );

        body.replaceChildren();

        if (subjectMarks.length === 0) {
            /*
             * Do not show an empty Subject Marks section. Subject Marks are
             * stored as JSON, so an empty/null value means there is currently
             * no subject-level data to render.
             */
            group.hidden = true;
            totalElement.textContent = '—';
            return;
        }

        let totalScore = 0;
        let numericMarkCount = 0;

        subjectMarks.forEach(
            item => {
                const numericMark =
                    parseFiniteNumber(
                        item.marks
                    );

                if (numericMark !== null) {
                    totalScore += numericMark;
                    numericMarkCount += 1;
                }

                const row =
                    document.createElement(
                        'tr'
                    );

                appendPreviousSubjectCell(
                    row,
                    item.subject || '—'
                );

                appendPreviousSubjectCell(
                    row,
                    formatPreviousSubjectMark(
                        item.marks
                    )
                );

                appendPreviousSubjectCell(
                    row,
                    item.grade || '—'
                );

                body.appendChild(row);
            }
        );

        totalElement.textContent =
            numericMarkCount > 0
                ? formatPreviousSubjectTotal(
                    totalScore
                )
                : '—';

        group.hidden = false;
    }

    function parsePreviousSubjectMarks(
        rawValue
    ) {
        if (
            rawValue === null
            || rawValue === undefined
        ) {
            return [];
        }

        let value = rawValue;

        /*
         * The database/API currently exposes subjectMarks as a String
         * containing JSON. Accept a real array as well so the renderer stays
         * compatible if the DTO is later changed to return structured JSON.
         */
        if (typeof value === 'string') {
            const normalized =
                value.trim();

            if (
                normalized === ''
                || normalized === '-'
                || normalized === 'null'
                || normalized === '[]'
            ) {
                return [];
            }

            try {
                value =
                    JSON.parse(
                        normalized
                    );
            } catch (error) {
                console.warn(
                    'Subject marks JSON could not be parsed.',
                    error
                );
                return [];
            }
        }

        if (!Array.isArray(value)) {
            return [];
        }

        return value
            .map(item => {
                if (
                    !item
                    || typeof item !== 'object'
                ) {
                    return null;
                }

                const subject =
                    item.subject
                    ?? item.subjectName
                    ?? item.name
                    ?? '';

                const marks =
                    item.marks
                    ?? item.mark
                    ?? item.obtainedMarks
                    ?? '';

                const grade =
                    item.grade
                    ?? '';

                return {
                    subject:
                        String(
                            subject
                        ).trim(),

                    marks,

                    grade:
                        String(
                            grade
                        ).trim()
                };
            })
            .filter(item =>
                item
                && (
                    item.subject !== ''
                    || (
                        item.marks !== null
                        && item.marks !== undefined
                        && String(
                            item.marks
                        ).trim() !== ''
                    )
                    || item.grade !== ''
                )
            );
    }

    function parseFiniteNumber(
        value
    ) {
        if (
            value === null
            || value === undefined
            || String(value).trim() === ''
        ) {
            return null;
        }

        const number =
            Number(value);

        return Number.isFinite(number)
            ? number
            : null;
    }

    function formatPreviousSubjectMark(
        value
    ) {
        if (
            value === null
            || value === undefined
            || String(value).trim() === ''
        ) {
            return '—';
        }

        return String(value).trim();
    }

    function formatPreviousSubjectTotal(
        value
    ) {
        if (!Number.isFinite(value)) {
            return '—';
        }

        return Number.isInteger(value)
            ? String(value)
            : value.toFixed(2);
    }

    function appendPreviousSubjectCell(
        row,
        value
    ) {
        const cell =
            document.createElement(
                'td'
            );

        cell.textContent =
            String(value);

        row.appendChild(cell);
    }

    /**
     * Resolves the class code supplied by the backend Application Details
     * response. This is presentation-only; it does not calculate or change
     * the admission workflow stage.
     *
     * @param {Object} application
     * @returns {string}
     */
    function resolveApplicationClassCode(
        application
    ) {
        if (!application) {
            return '';
        }

        const candidates = [
            application.classCode,
            application.appliedClassCode,
            application.classLevelCode,
            application.appliedClass
        ];

        for (const candidate of candidates) {
            const value =
                String(
                    candidate ?? ''
                )
                    .trim()
                    .toUpperCase();

            if (value) {
                return value;
            }
        }

        return '';
    }

    function renderPreviousSchoolResultsForClass(
        application
    ) {
        const classCode =
            resolveApplicationClassCode(
                application
            );

        const pleFields = [
            'view-pleRef',
            'view-pleScore'
        ];

        const uceFields = [
            'view-uceRef',
            'view-uceScore'
        ];

        const getFormGroup =
            id => {
                const element =
                    view.root.querySelector(
                        `#${id}`
                    );

                return element
                    ? element.closest(
                        '.form-group'
                    )
                    : null;
            };

        const hasValue =
            value =>
                value !== null
                && value !== undefined
                && String(value).trim() !== ''
                && String(value).trim() !== '-';

        const hasPleData =
            hasValue(application?.pleRef)
            || application?.pleScore !== null
            && application?.pleScore !== undefined;

        const hasUceData =
            hasValue(application?.uceRef)
            || application?.uceScore !== null
            && application?.uceScore !== undefined;

        /*
         * Class rules are the normal applicability rules, while real backend
         * data is preserved if a legacy application contains it.
         */
        const showPle =
            classCode === 'S1'
            || hasPleData;

        const showUce =
            classCode === 'S5'
            || hasUceData;

        const setFieldsVisible =
            (fieldIds, visible) => {
                fieldIds.forEach(fieldId => {
                    const group =
                        getFormGroup(fieldId);

                    if (group) {
                        group.hidden = !visible;
                    }
                });
            };

        setFieldsVisible(
            pleFields,
            showPle
        );

        setFieldsVisible(
            uceFields,
            showUce
        );

    }



    async function loadFeeDiscussion(
        applicationId,
        options = {}
    ) {
        const {
            render = true
        } = options || {};

        const id = Number(applicationId);

        if (!Number.isInteger(id) || id <= 0) {
            return null;
        }

        const currentStage =
            state.currentApplication?.currentStage;

        /*
         * Fee Discussion remains visible after the application advances
         * beyond PARENT_FEE_DISCUSSION. Always load the saved fee record for
         * PARENT_FEE_DISCUSSION and all later admission stages so the profile
         * and Edit Fee Structure modal receive the authoritative stored data.
         */
        if (!isFeeDiscussionStageOrLater(
            currentStage
        )) {
            state.feeDiscussion = null;

            if (render) {
                renderFeeDiscussion(null);
            }

            return null;
        }

        try {
            const response =
                await apiGet(
                    `${API_ROOT}/${encodeURIComponent(
                        id
                    )}/workflow/fee-discussion`
                );

            if (Number(state.currentApplicationId) !== id) {
                return null;
            }

            const feeDiscussion =
                unwrapResponseData(response);

            state.feeDiscussion =
                feeDiscussion || null;

            if (render) {
                renderFeeDiscussion(
                    state.feeDiscussion
                );
            }

            return state.feeDiscussion;
        } catch (error) {
            state.feeDiscussion = null;

            if (isFeeDiscussionStageOrLater(
                state.currentApplication?.currentStage
            )) {
                console.error(
                    'Fee Discussion details could not be loaded.',
                    error
                );
            }

            if (render) {
                renderFeeDiscussion(null);
            }

            return null;
        }
    }


    function renderFeeDiscussion(feeDiscussion) {
        const currentStage =
            state.currentApplication?.currentStage;

        const shouldShow =
            isFeeDiscussionStageOrLater(
                currentStage
            );

        toggleElement(
            view.feeDiscussionSection,
            shouldShow
        );

        if (!shouldShow) {
            return;
        }

        const fee = feeDiscussion || {};
        const hasSavedFee =
            Boolean(fee.feeId);

        /*
         * First entry into PARENT_FEE_DISCUSSION:
         * show the editable form directly.
         *
         * Once a fee record exists:
         * hide the form and show the same read-only summary
         * style used by School Visit / Entrance Test.
         */
        toggleElement(
            view.feeInitialForm,
            false
        );

        toggleElement(
            view.feeCreatePrompt,
            !hasSavedFee
        );

        toggleElement(
            view.feeSavedView,
            hasSavedFee
        );

        toggleElement(
            view.feeDiscussionEditButton,
            hasSavedFee
            && isFeeDiscussionEditableStage(
                currentStage,
                fee
            )
        );

        const feeDecision =
            String(
                fee.feeDecision
                || ''
            )
                .trim()
                .toUpperCase();

        const canFinalizeFeeDiscussion =
            hasSavedFee
            && enumEquals(
                currentStage,
                'PARENT_FEE_DISCUSSION'
            )
            && feeDecision === 'FULL_PAYMENT';

        toggleElement(
            view.feeDiscussionFinalizeButton,
            canFinalizeFeeDiscussion
        );

        const canCancelApplicationFromFee =
            enumEquals(
                currentStage,
                'PARENT_FEE_DISCUSSION'
            )
            && !Boolean(
                state.currentApplication?.workflowLocked
            );

        toggleElement(
            view.feeDiscussionCancelApplicationButton,
            canCancelApplicationFromFee
        );

        if (view.feeDiscussionFinalizeButton) {
            view.feeDiscussionFinalizeButton.innerHTML =
                '<i class="bi bi-arrow-right-circle"></i> Finalize & Continue to Payment';
        }

        if (!hasSavedFee) {
            toggleElement(
                view.feeDiscussionSaveButton,
                false
            );

            const applicationId =
                Number(state.currentApplicationId);

            const shouldAutoOpen =
                enumEquals(
                    currentStage,
                    'PARENT_FEE_DISCUSSION'
                )
                && Number.isInteger(applicationId)
                && applicationId > 0
                && state.feeCreateModalAutoOpenedForApplicationId
                    !== applicationId
                && !isModalOpen(view.feeEditModal);

            if (shouldAutoOpen) {
                state.feeCreateModalAutoOpenedForApplicationId =
                    applicationId;

                /*
                 * Defer until the current profile render is complete.
                 * This uses the same ERP modal UI as Edit Fee Structure.
                 */
                window.setTimeout(
                    () => {
                        if (
                            Number(state.currentApplicationId)
                                === applicationId
                            && !state.feeDiscussion?.feeId
                            && enumEquals(
                                state.currentApplication?.currentStage,
                                'PARENT_FEE_DISCUSSION'
                            )
                        ) {
                            openFeeCreateModal();
                        }
                    },
                    0
                );
            }

            return;
        }

        setFeeSummaryText(
            view.feeViewTermFee,
            fee.termFee
        );
        setFeeSummaryText(
            view.feeViewTransportFee,
            fee.transportFee
        );
        setFeeSummaryText(
            view.feeViewHostelFee,
            fee.hostelFee
        );
        setFeeSummaryText(
            view.feeViewUniformFee,
            fee.uniformFee
        );
        setFeeSummaryText(
            view.feeViewBooksFee,
            fee.booksFee
        );
        setFeeSummaryText(
            view.feeViewAdmissionFee,
            fee.admissionFee
        );
        setFeeSummaryText(
            view.feeViewOtherFee,
            fee.otherFee
        );
        setFeeSummaryText(
            view.feeViewBaseFeeAmount,
            fee.baseFeeAmount
        );
        setFeeSummaryText(
            view.feeViewParentCanPay,
            fee.parentCanPay
        );
        setFeeSummaryText(
            view.feeViewAssistanceRequired,
            fee.assistanceRequired
        );

        setNodeText(
            view.feeViewDecision,
            formatFeeDecision(
                fee.feeDecision
            )
        );

        setNodeText(
            view.feeViewDiscussionRemarks,
            fee.discussionRemarks
            || fee.remarks
            || '—'
        );
    }

    function setFeeSummaryText(element, value) {
        const numeric = Number(value);

        setNodeText(
            element,
            Number.isFinite(numeric)
                ? formatFeeAmount(numeric)
                : '—'
        );
    }

    function formatFeeDecision(value) {
        const normalized =
            String(value || '')
                .trim()
                .toUpperCase();

        if (normalized === 'FULL_PAYMENT') {
            return 'Parent Can Pay Full Fee';
        }

        if (normalized === 'PARTIAL_ASSISTANCE') {
            return 'Needs Partial Scholarship';
        }

        if (normalized === 'FULL_ASSISTANCE') {
            return 'Needs Full Scholarship';
        }

        return 'Pending';
    }

    function isFeeDiscussionStageOrLater(stage) {
        const order = [
            'APPLICATION_DRAFT',
            'APPLICATION_VERIFICATION',
            'SCHOOL_VISIT',
            'ENTRANCE_TEST',
            'PARENT_FEE_DISCUSSION',
            'SCHOLARSHIP',
            'PAYMENT',
            'FINAL_ADMISSION',
            'ENROLLED',
            'CLOSED'
        ];

        const currentIndex =
            order.indexOf(
                String(stage || '')
                    .trim()
                    .toUpperCase()
            );

        return currentIndex >=
            order.indexOf(
                'PARENT_FEE_DISCUSSION'
            );
    }

    function isFeeDiscussionEditableStage(
            stage,
            feeDiscussion
    ) {
        const normalizedStage =
            String(stage || '')
                .trim()
                .toUpperCase();

        if (
            normalizedStage === 'PARENT_FEE_DISCUSSION'
            || normalizedStage === 'SCHOLARSHIP'
        ) {
            return true;
        }

        if (normalizedStage === 'PAYMENT') {
            const amountPaid =
                Number(
                    feeDiscussion?.amountPaid
                    ?? 0
                );

            return Number.isFinite(amountPaid)
                && amountPaid <= 0;
        }

        return false;
    }

    function setFeeInputValue(input, value) {
        if (!input) {
            return;
        }

        const numeric = Number(value);

        input.value =
            Number.isFinite(numeric)
                ? String(numeric)
                : '0';
    }

    function readFeeInputValue(input) {
        if (!input) {
            return 0;
        }

        const value = Number(input.value || 0);

        if (!Number.isFinite(value) || value < 0) {
            return 0;
        }

        return value;
    }

    function formatFeeAmount(value) {
        const numeric = Number(value);

        return (
            Number.isFinite(numeric)
                ? numeric
                : 0
        ).toLocaleString(
            'en-UG',
            {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            }
        );
    }

    function getFeeDecisionValue() {
        const mainDecision =
            view.feeDecisionMainOptions.find(option =>
                option.checked
            );

        const mainValue =
            String(
                mainDecision?.value || 'PENDING'
            )
                .trim()
                .toUpperCase();

        if (mainValue === 'DECIDE_LATER') {
            return 'PENDING';
        }

        if (mainValue === 'FULL_PAYMENT') {
            return 'FULL_PAYMENT';
        }

        if (mainValue === 'SCHOLARSHIP') {
            const scholarshipType =
                view.feeDecisionOptions.find(option =>
                    option.checked
                    && (
                        String(option.value).toUpperCase() === 'PARTIAL_ASSISTANCE'
                        || String(option.value).toUpperCase() === 'FULL_ASSISTANCE'
                    )
                );

            return String(
                scholarshipType?.value || 'PENDING'
            ).toUpperCase();
        }

        return 'PENDING';
    }

    function renderFeeDecisionFields(decision) {
        const normalized =
            String(decision || 'PENDING').toUpperCase();

        const scholarshipSelected =
            normalized === 'PARTIAL_ASSISTANCE'
            || normalized === 'FULL_ASSISTANCE';

        toggleElement(
            view.feeScholarshipTypePanel,
            scholarshipSelected
        );

        toggleElement(
            view.feeContributionPanel,
            normalized === 'PARTIAL_ASSISTANCE'
                || normalized === 'FULL_ASSISTANCE'
        );

        if (view.feeParentCanPay) {
            view.feeParentCanPay.readOnly =
                normalized !== 'PARTIAL_ASSISTANCE';
        }

        if (normalized === 'FULL_PAYMENT') {
            view.feeParentCanPay.value =
                String(updateFeeDiscussionAmounts().total);
        } else if (normalized === 'FULL_ASSISTANCE') {
            view.feeParentCanPay.value = '0';
        }
    }

    function updateFeeDiscussionAmounts() {
        const total = [
            view.feeTermFee,
            view.feeTransportFee,
            view.feeHostelFee,
            view.feeUniformFee,
            view.feeBooksFee,
            view.feeAdmissionFee,
            view.feeOtherFee
        ].reduce(
            (sum, input) =>
                sum + readFeeInputValue(input),
            0
        );

        const decision =
            getFeeDecisionValue();

        let parentCanPay =
            readFeeInputValue(
                view.feeParentCanPay
            );

        if (decision === 'FULL_PAYMENT') {
            parentCanPay = total;

            if (view.feeParentCanPay) {
                view.feeParentCanPay.value =
                    String(total);
            }
        }

        if (decision === 'FULL_ASSISTANCE') {
            parentCanPay = 0;

            if (view.feeParentCanPay) {
                view.feeParentCanPay.value = '0';
            }
        }

        const assistanceRequired =
            decision === 'PENDING'
                ? 0
                : Math.max(
                    total - parentCanPay,
                    0
                );

        if (view.feeBaseFeeAmount) {
            view.feeBaseFeeAmount.value =
                formatFeeAmount(total);
        }

        if (view.feeAssistanceRequired) {
            view.feeAssistanceRequired.value =
                formatFeeAmount(
                    assistanceRequired
                );
        }

        return {
            total,
            parentCanPay,
            assistanceRequired,
            decision
        };
    }

    function validateFeeDecision(amounts) {
        if (amounts.decision === 'PENDING') {
            return '';
        }

        if (
            amounts.parentCanPay
            > amounts.total
        ) {
            return 'Parent Contribution cannot exceed the Total Fee.';
        }

        if (
            amounts.decision === 'PARTIAL_ASSISTANCE'
            && (
                amounts.parentCanPay <= 0
                || amounts.assistanceRequired <= 0
            )
        ) {
            return 'Partial Scholarship requires a parent contribution and a remaining scholarship amount.';
        }

        return '';
    }

    function renderScholarshipSection(
            application = state.currentApplication
    ) {
        const feeDecision =
            String(
                state.feeDiscussion?.feeDecision
                || ''
            )
                .trim()
                .toUpperCase();

        const assistanceSelected =
            feeDecision === 'PARTIAL_ASSISTANCE'
            || feeDecision === 'FULL_ASSISTANCE';

        const currentStage =
            String(
                application?.currentStage
                || ''
            )
                .trim()
                .toUpperCase();

        const rawStatus =
            String(
                application?.scholarshipWorkflowStatus
                || application?.scholarshipStatus
                || ''
            )
                .trim()
                .toUpperCase();

        /*
         * Scholarship visibility and Scholarship initiation are two different
         * concerns. A previous Scholarship record/status may legitimately be
         * present while the admission application is already in a later stage.
         * That historical/status information may remain visible in the profile,
         * but it must NEVER make the Scholarship 'Fill Application' initiation
         * panel appear for every admission stage.
         *
         * The Scholarship section is therefore relevant when:
         *  1) the admission is currently in SCHOLARSHIP,
         *  2) Parent Fee Discussion has selected an assistance route, or
         *  3) there is an actual Scholarship record/status to summarize.
         *
         * Initiation is separately restricted below to the two states in which
         * the school is actually allowed to start/continue that route.
         */
        /*
         * If Parent Fee Discussion has explicitly selected Parents Pay
         * (FULL_PAYMENT) or Decide Later (PENDING), an old Scholarship
         * record/status must not keep the Scholarship initiation section
         * visible. The current Fee Discussion decision is authoritative for
         * this admission stage.
         *
         * Scholarship remains visible when:
         *  1) the application is currently in SCHOLARSHIP, or
         *  2) Parent Fee Discussion currently has a Scholarship decision, or
         *  3) the application is outside Parent Fee Discussion and has an
         *     existing Scholarship status to display.
         */
        const feeDecisionClosesScholarship =
            feeDecision === 'FULL_PAYMENT'
            || feeDecision === 'PENDING';

        const scholarshipRelevant =
            !feeDecisionClosesScholarship
            && (
                currentStage === 'SCHOLARSHIP'
                || (
                    currentStage === 'PARENT_FEE_DISCUSSION'
                    && Boolean(state.feeDiscussion?.feeId)
                    && assistanceSelected
                )
                || (
                    currentStage !== 'PARENT_FEE_DISCUSSION'
                    && Boolean(
                        rawStatus
                        && rawStatus !== 'NOT_APPLIED'
                        && rawStatus !== 'NOT_STARTED'
                    )
                )
            );

        toggleElement(
            view.scholarshipSection,
            scholarshipRelevant
        );

        if (!scholarshipRelevant) {
            return;
        }

        let displayStatus =
            rawStatus
                ? formatEnum(rawStatus)
                : 'Awaiting Method';

        /*
         * Fee Discussion has already selected an assistance path, but no
         * school/public Scholarship route has been chosen yet.
         */
        if (
            currentStage === 'PARENT_FEE_DISCUSSION'
            && assistanceSelected
            && (
                !rawStatus
                || rawStatus === 'NOT_APPLIED'
                || rawStatus === 'NOT_STARTED'
                || rawStatus === 'PENDING'
            )
        ) {
            displayStatus =
                'Awaiting Method';
        } else if (
            rawStatus === 'LINK_SENT'
        ) {
            displayStatus =
                'Link Sent - Awaiting Submission';
        } else if (
            rawStatus === 'IN_PROGRESS'
        ) {
            displayStatus =
                'Scholarship Application In Progress';
        } else if (
            rawStatus === 'SUBMITTED'
        ) {
            displayStatus =
                'Submitted';
        }

        if (view.scholarshipFormStatus) {
            view.scholarshipFormStatus.textContent =
                displayStatus;

            view.scholarshipFormStatus.className =
                'status-badge badge';
        }

        const submitted =
            rawStatus === 'SUBMITTED';

        /*
         * The Admission dashboard is initiation/status only.
         * The Scholarship form itself exists exclusively on
         * /scholarship-application.html.
         *
         * IMPORTANT: an old Scholarship status must not expose the initiation
         * panel after the admission has moved beyond the Scholarship entry
         * point. The panel is available only while the admission is actually
         * at SCHOLARSHIP, or while Parent Fee Discussion has just selected an
         * assistance route and is waiting for Scholarship initiation.
         */
        const scholarshipInitiationAllowed =
            !feeDecisionClosesScholarship
            && currentStage === 'SCHOLARSHIP';

        toggleElement(
            view.scholarshipStartPanel,
            scholarshipInitiationAllowed
                && !submitted
        );
    }

    async function ensureScholarshipStageForInitiation(
            applicationId
    ) {
        const currentStage =
            String(
                state.currentApplication?.currentStage
                || ''
            )
                .trim()
                .toUpperCase();

        /*
         * Fee Discussion is saved separately from workflow movement.
         * Scholarship initiation is allowed only after the existing
         * Parent Fee Discussion -> Scholarship workflow action has been
         * explicitly performed by the user.
         *
         * This function intentionally does NOT call the Fee Discussion
         * finalize endpoint. That endpoint belongs to the explicit workflow
         * Next/Process action, not to Fill in School or Send Link.
         */
        if (currentStage !== 'SCHOLARSHIP') {
            throw new Error(
                'Move the application to the Scholarship stage using the Next action before starting the Scholarship Application.'
            );
        }

        const fee =
            state.feeDiscussion;

        const decision =
            String(
                fee?.feeDecision
                || ''
            )
                .trim()
                .toUpperCase();

        if (!fee?.feeId) {
            throw new Error(
                'Save the Fee Discussion before starting the Scholarship Application.'
            );
        }

        if (
            decision !== 'PARTIAL_ASSISTANCE'
            && decision !== 'FULL_ASSISTANCE'
        ) {
            throw new Error(
                'The saved Fee Discussion does not contain a Scholarship / Assistance decision.'
            );
        }
    }

    async function openScholarshipFormAtSchool() {
        const applicationId =
            Number(
                state.currentApplicationId
            );

        if (!Number.isInteger(applicationId)
                || applicationId <= 0) {
            notifyError(
                'A valid Application ID is required.'
            );
            return;
        }

        /*
         * Open the tab synchronously from the user click so browsers do not
         * block it after the asynchronous finalize/key-generation requests.
         */
        let scholarshipWindow = null;

        /*
         * The Scholarship Application must always open in a separate tab.
         * Never fall back to navigating the current application page.
         */
        try {
            scholarshipWindow =
                window.open(
                    'about:blank',
                    '_blank'
                );

            if (scholarshipWindow) {
                scholarshipWindow.opener = null;
            }
        } catch (popupError) {
            scholarshipWindow = null;

            console.debug(
                'Scholarship tab was blocked. User must allow pop-ups.',
                popupError
            );

            notifyError(
                'Your browser blocked the Scholarship tab. Please allow pop-ups for this site and click Fill in School again.'
            );

            return;
        }

        let loaderToken = null;

        try {
            setButtonBusy(
                view.scholarshipFillAtSchoolButton,
                true,
                'Opening...'
            );

            if (typeof showLoader === 'function') {
                loaderToken = showLoader(
                    'Preparing Scholarship Application...'
                );
            }

            await ensureScholarshipStageForInitiation(
                applicationId
            );

            const response =
                await apiPost(
                    `${API_ROOT}/${encodeURIComponent(
                        applicationId
                    )}/workflow/scholarship/school-access`,
                    {}
                );

            const payload =
                response?.data
                ?? response;

            const accessKey =
                String(
                    payload?.accessKey
                    || ''
                ).trim();

            if (!accessKey) {
                throw new Error(
                    'Secure school Scholarship access could not be generated.'
                );
            }

            const targetUrl =
                `/scholarship-application#schoolKey=${encodeURIComponent(
                    accessKey
                )}`;

            if (scholarshipWindow) {
                scholarshipWindow.location.replace(
                    targetUrl
                );

                /*
                 * Release the blocking dashboard loader immediately after the
                 * secure Scholarship page has been launched. A second full
                 * applicant/profile reload is not part of opening the form and
                 * must never keep the dashboard blocked.
                 */
                setButtonBusy(
                    view.scholarshipFillAtSchoolButton,
                    false
                );

                if (
                    loaderToken
                    && typeof hideLoader === 'function'
                ) {
                    hideLoader(
                        loaderToken
                    );
                    loaderToken = null;
                }

                if (view.scholarshipFormStatus) {
                    view.scholarshipFormStatus.textContent =
                        'School Assisted - In Progress';
                }

                notifyIntermediateSuccess(
                    'School-assisted Scholarship Application opened in a new tab.'
                );

                /*
                 * Refresh the dashboard state in the background only.
                 * Failure here must not affect the already-opened Scholarship
                 * Application or re-block the operator.
                 */
                void synchronizeCurrentApplicationAfterMutation(
                    'scholarship-school-access',
                    applicationId
                ).catch(error => {
                    console.warn(
                        'Scholarship dashboard background refresh failed.',
                        error
                    );
                });

                return;
            }

            /*
             * No current-tab fallback is allowed. If the browser did not
             * provide a separate tab, stop here and ask the operator to allow
             * pop-ups. The current application page must remain open.
             */
            throw new Error(
                'Your browser blocked the Scholarship tab. Please allow pop-ups for this site and click Fill in School again.'
            );
        } catch (error) {
            if (scholarshipWindow) {
                try {
                    scholarshipWindow.close();
                } catch (closeError) {
                    console.warn(
                        'Could not close the unused Scholarship tab.',
                        closeError
                    );
                }
            }

            notifyError(
                readErrorMessage(
                    error,
                    'Scholarship Application could not be opened.'
                )
            );
        } finally {
            setButtonBusy(
                view.scholarshipFillAtSchoolButton,
                false
            );

            if (
                loaderToken
                && typeof hideLoader === 'function'
            ) {
                hideLoader(loaderToken);
            }
        }
    }

    async function sendScholarshipApplicationLink() {
        const applicationId =
            Number(
                state.currentApplicationId
            );

        if (!Number.isInteger(applicationId)
                || applicationId <= 0) {
            notifyError(
                'A valid Application ID is required.'
            );
            return;
        }

        let loaderToken = null;

        try {
            setButtonBusy(
                view.feeEditSaveButton,
                true,
                'Sending...'
            );

            if (typeof showLoader === 'function') {
                loaderToken = showLoader(
                    'Preparing and Sending Scholarship Link...'
                );
            }

            await ensureScholarshipStageForInitiation(
                applicationId
            );

            await apiPost(
                `${API_ROOT}/${encodeURIComponent(
                    applicationId
                )}/workflow/scholarship/application-link`,
                {}
            );

            await synchronizeCurrentApplicationAfterMutation(
                'scholarship-link',
                applicationId
            );

            notifyIntermediateSuccess(
                'Scholarship application link sent successfully.'
            );
        } catch (error) {
            notifyError(
                readErrorMessage(
                    error,
                    'Scholarship application link could not be sent.'
                )
            );
        } finally {
            setButtonBusy(
                view.feeEditSaveButton,
                false
            );

            if (
                loaderToken
                && typeof hideLoader === 'function'
            ) {
                hideLoader(loaderToken);
            }
        }
    }

    async function saveFeeDiscussion() {
        const applicationId =
            Number(state.currentApplicationId);

        if (!Number.isInteger(applicationId)
                || applicationId <= 0) {
            notifyError(
                'A valid Application ID is required.'
            );
            return;
        }

        const amounts =
            updateFeeDiscussionAmounts();

        const validationMessage =
            validateFeeDecision(amounts);

        if (validationMessage) {
            notifyError(validationMessage);
            return;
        }

        const payload = {
            termFee:
                readFeeInputValue(view.feeTermFee),
            transportFee:
                readFeeInputValue(view.feeTransportFee),
            hostelFee:
                readFeeInputValue(view.feeHostelFee),
            uniformFee:
                readFeeInputValue(view.feeUniformFee),
            booksFee:
                readFeeInputValue(view.feeBooksFee),
            admissionFee:
                readFeeInputValue(view.feeAdmissionFee),
            otherFee:
                readFeeInputValue(view.feeOtherFee),
            parentCanPay:
                amounts.parentCanPay,
            feeDecision:
                amounts.decision,
            discussionRemarks:
                String(
                    view.feeDiscussionRemarks?.value
                    || ''
                ).trim(),
            changeReason:
                null
        };

        let loaderToken = null;

        try {
            setButtonBusy(
                view.feeDiscussionSaveButton,
                true,
                'Saving...'
            );

            if (typeof showLoader === 'function') {
                loaderToken = showLoader(
                    'Saving Fee Discussion...'
                );
            }

            const response =
                await apiPatchJson(
                    `${API_ROOT}/${encodeURIComponent(
                        applicationId
                    )}/workflow/fee-discussion`,
                    payload
                );

            state.feeDiscussion =
                unwrapResponseData(response);

            renderFeeDiscussion(
                state.feeDiscussion
            );

            notifyIntermediateSuccess(
                'Fee discussion saved successfully.'
            );
        } catch (error) {
            notifyError(
                readErrorMessage(
                    error,
                    'Fee discussion could not be saved.'
                )
            );
        } finally {
            setButtonBusy(
                view.feeDiscussionSaveButton,
                false
            );

            if (
                loaderToken != null
                && typeof hideLoader === 'function'
            ) {
                hideLoader(loaderToken);
            }
        }
    }

    function isModalOpen(modal) {
        return Boolean(
            modal
            && !modal.classList.contains('hidden')
        );
    }

    function configureFeeDiscussionModal(mode) {
        const normalized =
            String(mode || '')
                .trim()
                .toUpperCase();

        const creating =
            normalized === 'CREATE';

        state.feeDiscussionModalMode =
            creating
                ? 'CREATE'
                : 'EDIT';

        setNodeText(
            view.feeEditTitle,
            creating
                ? 'Parent Fee Discussion'
                : 'Edit Fee Structure'
        );

        setNodeText(
            view.feeEditSubtitle,
            ''
        );

        toggleElement(
            view.editFeeChangeReasonGroup,
            !creating
        );

        const decideLaterInput =
            view.root?.querySelector(
                '#ba-feeDecisionDecideLater'
            );
        const decideLaterLabel =
            decideLaterInput?.closest(
                '.app-fee-simple-radio'
            );

        toggleElement(
            decideLaterLabel,
            creating
        );

        if (decideLaterInput && !creating) {
            decideLaterInput.checked = false;
        }

        if (view.editFeeChangeReason) {
            view.editFeeChangeReason.value = '';
            view.editFeeChangeReason.required =
                !creating;
        }

        if (view.editFeeDiscussionRemarks) {
            view.editFeeDiscussionRemarks.required =
                !creating;
        }

        toggleElement(
            view.root?.querySelector(
                '#ba-editFeeDiscussionRemarksRequired'
            ),
            !creating
        );

        setNodeText(
            view.feeEditSaveButtonLabel,
            creating
                ? 'Save Details'
                : 'Save Changes'
        );
    }


    function openCancelApplicationFromFeeModal() {
        if (!state.currentApplicationId
                || !state.currentApplication) {
            notifyError('Open an application before cancelling it.');
            return;
        }

        if (!enumEquals(
                state.currentApplication.currentStage,
                'PARENT_FEE_DISCUSSION'
        )) {
            notifyError(
                'Application cancellation from this action is available only during Parent Fee Discussion.'
            );
            return;
        }

        const rejectTransition =
            state.profileTransitions.find(
                transition =>
                    enumEquals(transition?.action, 'REJECT')
                    && enumEquals(transition?.targetStage, 'CLOSED')
            ) || null;

        if (!rejectTransition) {
            notifyError(
                'The application cannot be cancelled at the current workflow state.'
            );
            return;
        }

        if (view.cancelApplicationFeeReason) {
            view.cancelApplicationFeeReason.value = '';
        }

        setNodeText(view.cancelApplicationFeeError, '');
        toggleElement(view.cancelApplicationFeeError, false);

        openModal(view.cancelApplicationFeeModal);

        window.setTimeout(
            () => view.cancelApplicationFeeReason?.focus(),
            0
        );
    }

    function closeCancelApplicationFromFeeModal() {
        closeModal(view.cancelApplicationFeeModal);

        if (view.cancelApplicationFeeReason) {
            view.cancelApplicationFeeReason.value = '';
        }

        setNodeText(view.cancelApplicationFeeError, '');
        toggleElement(view.cancelApplicationFeeError, false);
    }

    async function cancelApplicationFromFeeDiscussion() {
        const applicationId = state.currentApplicationId;
        const application = state.currentApplication;

        if (!applicationId || !application) {
            notifyError('Application details are unavailable.');
            return;
        }

        const reason =
            trimValue(view.cancelApplicationFeeReason);

        if (!reason) {
            setNodeText(
                view.cancelApplicationFeeError,
                'Cancellation reason is required.'
            );
            toggleElement(view.cancelApplicationFeeError, true);
            view.cancelApplicationFeeReason?.focus();
            return;
        }

        const rejectTransition =
            state.profileTransitions.find(
                transition =>
                    enumEquals(transition?.action, 'REJECT')
                    && enumEquals(transition?.targetStage, 'CLOSED')
            ) || null;

        if (!rejectTransition) {
            setNodeText(
                view.cancelApplicationFeeError,
                'The application can no longer be cancelled from Parent Fee Discussion.'
            );
            toggleElement(view.cancelApplicationFeeError, true);
            return;
        }

        let loaderToken = null;

        try {
            setButtonBusy(
                view.confirmCancelApplicationFeeButton,
                true,
                'Cancelling...'
            );

            if (typeof showLoader === 'function') {
                loaderToken =
                    showLoader('Cancelling admission application...');
            }

            const transitionResponse =
                await submitWorkflowTransition(
                    applicationId,
                    application.currentStage,
                    rejectTransition,
                    null,
                    reason
                );

            closeCancelApplicationFromFeeModal();

            applyWorkflowResponseToProfile(
                transitionResponse
            );

            try {
                await openApplication(
                    applicationId,
                    {
                        preserveScroll: true,
                        silent: true
                    }
                );
            } catch (refreshError) {
                console.warn(
                    'Application was cancelled, but the refreshed profile could not be loaded.',
                    refreshError
                );
            }

            notifySuccess(
                'Admission application cancelled successfully.'
            );
        } catch (error) {
            console.error(
                'Cancel application from Fee Discussion failed:',
                error
            );

            const message =
                extractApiErrorMessage(
                    error,
                    'Application could not be cancelled.'
                );

            setNodeText(
                view.cancelApplicationFeeError,
                message
            );
            toggleElement(
                view.cancelApplicationFeeError,
                true
            );
        } finally {
            setButtonBusy(
                view.confirmCancelApplicationFeeButton,
                false
            );

            if (loaderToken !== null
                    && typeof hideLoader === 'function') {
                hideLoader(loaderToken);
            }
        }
    }

    async function finalizeFeeDiscussion() {
        const applicationId =
            Number(state.currentApplicationId);

        if (!Number.isInteger(applicationId)
                || applicationId <= 0) {
            notifyError(
                'A valid Application ID is required.'
            );
            return;
        }

        const fee =
            state.feeDiscussion;

        if (!fee?.feeId) {
            notifyError(
                'Save the Fee Discussion before finalizing it.'
            );
            return;
        }

        const currentStage =
            String(
                state.currentApplication?.currentStage
                || ''
            )
                .trim()
                .toUpperCase();

        if (currentStage !== 'PARENT_FEE_DISCUSSION') {
            notifyError(
                'Fee Discussion can be finalized only while the application is in the Parent Fee Discussion stage.'
            );
            return;
        }

        const decision =
            String(
                fee.feeDecision
                || ''
            )
                .trim()
                .toUpperCase();

        if (!decision
                || decision === 'PENDING') {
            notifyError(
                'Select and save a final Fee Discussion decision before continuing.'
            );
            return;
        }

        const destination =
            decision === 'FULL_PAYMENT'
                ? 'Payment'
                : 'Scholarship';

        const confirmed =
            window.confirm(
                `Finalize Fee Discussion and continue to ${destination}?`
            );

        if (!confirmed) {
            return;
        }

        let loaderToken = null;

        try {
            setButtonBusy(
                view.feeDiscussionFinalizeButton,
                true,
                'Finalizing...'
            );

            if (typeof showLoader === 'function') {
                loaderToken =
                    showLoader(
                        'Finalizing Fee Discussion...'
                    );
            }

            await apiPatchJson(
                `${API_ROOT}/${encodeURIComponent(
                    applicationId
                )}/workflow/fee-discussion/finalize`,
                {}
            );

            const refreshed =
                await synchronizeCurrentApplicationAfterMutation(
                    'fee-discussion-finalized',
                    applicationId
                );

            if (!refreshed) {
                throw new Error(
                    'Fee Discussion was finalized, but the refreshed application profile could not be loaded.'
                );
            }

            notifyIntermediateSuccess(
                `Fee Discussion finalized. Application moved to ${destination}.`
            );
        } catch (error) {
            notifyError(
                readErrorMessage(
                    error,
                    'Fee Discussion could not be finalized.'
                )
            );
        } finally {
            setButtonBusy(
                view.feeDiscussionFinalizeButton,
                false
            );

            if (loaderToken != null
                    && typeof hideLoader === 'function') {
                hideLoader(loaderToken);
            }
        }
    }

    function hasFeeStructureChanged() {
        const original =
            state.feeDiscussionOriginalFeeAmounts;

        if (!original) {
            return false;
        }

        const current = {
            termFee: readFeeInputValue(view.editFeeTermFee),
            transportFee: readFeeInputValue(view.editFeeTransportFee),
            hostelFee: readFeeInputValue(view.editFeeHostelFee),
            uniformFee: readFeeInputValue(view.editFeeUniformFee),
            booksFee: readFeeInputValue(view.editFeeBooksFee),
            admissionFee: readFeeInputValue(view.editFeeAdmissionFee),
            otherFee: readFeeInputValue(view.editFeeOtherFee)
        };

        return Object.keys(current).some(key =>
            Math.abs(
                Number(current[key] || 0)
                - Number(original[key] || 0)
            ) > 0.000001
        );
    }

    function updateFeeDiscussionWizardButtonState() {
        const step = state.feeDiscussionStep || 1;
        const creating =
            state.feeDiscussionModalMode === 'CREATE';

        if (view.feeEditNextButton) {
            let disabled = false;

            if (step === 1) {
                const amounts = updateFeeEditAmounts();
                const feeChanged =
                    !creating && hasFeeStructureChanged();
                const confirmationRequired =
                    creating || feeChanged;

                disabled =
                    amounts.total <= 0
                    || (
                        confirmationRequired
                        && !Boolean(
                            view.feeConfirmDetails?.checked
                        )
                    );
            } else if (step === 2) {
                const mainDecision =
                    view.editFeeDecisionMainOptions.find(option =>
                        option.checked
                    );

                const decision =
                    String(mainDecision?.value || '')
                        .trim()
                        .toUpperCase();

                const decisionSelected =
                    decision === 'FULL_PAYMENT'
                    || decision === 'DECIDE_LATER'
                    || decision === 'SCHOLARSHIP';

                const remarksElement =
                    view.editFeeDiscussionRemarks
                    || document.getElementById(
                        'ba-editFeeDiscussionRemarks'
                    );

                const remarksEntered =
                    Boolean(
                        String(
                            remarksElement?.value || ''
                        ).trim()
                    );

                /*
                 * CREATE:
                 * Discussion Remarks are optional.
                 *
                 * EDIT:
                 * Discussion Remarks are mandatory before Next.
                 */
                disabled =
                    !decisionSelected
                    || (
                        !creating
                        && !remarksEntered
                    );

                if (!creating && decision === 'DECIDE_LATER') {
                    disabled = true;
                }
            } else if (step === 3) {
                const decision = getFeeEditDecision();
                const methodSelected =
                    Boolean(view.feeScholarshipMethodSchool?.checked)
                    || Boolean(view.feeScholarshipMethodEmail?.checked);

                disabled =
                    (decision !== 'PARTIAL_ASSISTANCE'
                        && decision !== 'FULL_ASSISTANCE')
                    || !methodSelected;
            }

            view.feeEditNextButton.disabled = disabled;
            view.feeEditNextButton.setAttribute(
                'aria-disabled',
                disabled ? 'true' : 'false'
            );
        }

        if (view.feeEditSaveButton) {
            const mainDecision =
                view.editFeeDecisionMainOptions.find(option =>
                    option.checked
                );

            const mainValue =
                String(mainDecision?.value || '')
                    .trim()
                    .toUpperCase();

            const isStep2Save =
                step === 2
                && (
                    mainValue === 'FULL_PAYMENT'
                    || (
                        creating
                        && mainValue === 'DECIDE_LATER'
                    )
                );

            const remarksElement =
                view.editFeeDiscussionRemarks
                || document.getElementById(
                    'ba-editFeeDiscussionRemarks'
                );

            const remarksEntered =
                Boolean(
                    String(
                        remarksElement?.value || ''
                    ).trim()
                );

            const isStep3 = step === 3;

            let saveDisabled =
                (
                    isStep3
                    && (
                        (
                            !Boolean(
                                view.feeScholarshipMethodSchool?.checked
                            )
                            && !Boolean(
                                view.feeScholarshipMethodEmail?.checked
                            )
                        )
                        || (
                            getFeeEditDecision() !== 'PARTIAL_ASSISTANCE'
                            && getFeeEditDecision() !== 'FULL_ASSISTANCE'
                        )
                    )
                );

            if (
                !creating
                && step === 2
                && !remarksEntered
            ) {
                saveDisabled = true;
            }

            view.feeEditSaveButton.disabled = saveDisabled;
            view.feeEditSaveButton.setAttribute(
                'aria-disabled',
                saveDisabled ? 'true' : 'false'
            );

            if (isStep2Save) {
                view.feeEditSaveButton.disabled =
                    !creating && !remarksEntered;
                view.feeEditSaveButton.setAttribute(
                    'aria-disabled',
                    view.feeEditSaveButton.disabled
                        ? 'true'
                        : 'false'
                );
            }
        }
    }

    function setFeeDiscussionStep(step) {
        let normalizedStep =
            Math.max(1, Math.min(3, Number(step) || 1));

        const scholarshipSelected =
            view.editFeeDecisionMainOptions.some(option =>
                option.checked
                && String(option.value).toUpperCase()
                    === 'SCHOLARSHIP'
            );

        if (normalizedStep === 3 && !scholarshipSelected) {
            normalizedStep = 2;
        }

        state.feeDiscussionStep = normalizedStep;

        toggleElement(
            view.feeStep1,
            normalizedStep === 1
        );
        toggleElement(
            view.feeStep2,
            normalizedStep === 2
        );
        toggleElement(
            view.feeStep3,
            normalizedStep === 3 && scholarshipSelected
        );

        const showScholarshipStep =
            scholarshipSelected
            || normalizedStep === 3;

        toggleElement(
            view.feeStepIndicator3,
            showScholarshipStep
        );

        const indicators = [
            view.feeStepIndicator1,
            view.feeStepIndicator2,
            view.feeStepIndicator3
        ];

        indicators.forEach((indicator, index) => {
            if (!indicator) {
                return;
            }

            const stepNumber = index + 1;
            const isVisible =
                stepNumber < 3
                || showScholarshipStep;
            const isCurrent =
                stepNumber === normalizedStep
                && isVisible;
            const isCompleted =
                isVisible
                && stepNumber < normalizedStep;

            indicator.classList.toggle(
                'is-active',
                isCurrent
            );
            indicator.classList.toggle(
                'is-completed',
                isCompleted
            );
            indicator.classList.toggle(
                'is-upcoming',
                isVisible
                && !isCurrent
                && !isCompleted
            );

            if (isCurrent) {
                indicator.setAttribute(
                    'aria-current',
                    'step'
                );
            } else {
                indicator.removeAttribute(
                    'aria-current'
                );
            }
        });

        if (view.feeEditBackButton) {
            view.feeEditBackButton.classList.toggle(
                'hidden',
                normalizedStep === 1
            );
        }

        const mainDecision =
            view.editFeeDecisionMainOptions.find(option =>
                option.checked
            );

        const mainValue =
            String(mainDecision?.value || '')
                .trim()
                .toUpperCase();

        const saveAndProcessAtStep2 =
            normalizedStep === 2
            && (
                mainValue === 'FULL_PAYMENT'
                || (
                    state.feeDiscussionModalMode === 'CREATE'
                    && mainValue === 'DECIDE_LATER'
                )
            );

        if (view.feeEditNextButton) {
            const showNextButton =
                normalizedStep === 1
                || (
                    normalizedStep === 2
                    && !saveAndProcessAtStep2
                );

            view.feeEditNextButton.classList.toggle(
                'hidden',
                !showNextButton
            );

            view.feeEditNextButton.querySelector('span')?.replaceChildren(
                document.createTextNode(
                    normalizedStep === 3
                        ? 'Save & Process'
                        : 'Next'
                )
            );
        }

        if (view.feeEditSaveButton) {
            const showSaveButton =
                saveAndProcessAtStep2
                || normalizedStep === 3;

            view.feeEditSaveButton.classList.toggle(
                'hidden',
                !showSaveButton
            );

            setNodeText(
                view.feeEditSaveButtonLabel,
                normalizedStep === 3
                    ? 'Save & Process'
                    : state.feeDiscussionModalMode === 'CREATE'
                        ? 'Save Details'
                        : 'Save Changes'
            );
        }

        if (normalizedStep === 2) {
            const amounts = updateFeeEditAmounts();
            setNodeText(
                view.feeStep2Total,
                formatFeeAmount(amounts.total)
            );
            renderFeeEditDecisionFields(
                getFeeEditDecision()
            );
        }

        if (normalizedStep === 3 && scholarshipSelected) {
            const amounts = updateFeeEditAmounts();
            setNodeText(
                view.feeStep3Total,
                formatFeeAmount(amounts.total)
            );
            renderFeeEditDecisionFields(
                getFeeEditDecision()
            );
        }

        updateFeeDiscussionWizardButtonState();
    }

    function resetFeeDiscussionWizard() {
        state.feeDiscussionStep = 1;
        state.feeDiscussionScholarshipSaved = false;

        if (view.feeConfirmDetails) {
            view.feeConfirmDetails.checked = false;
        }
        view.editFeeDecisionMainOptions.forEach(option => {
            option.checked = false;
        });

        view.editFeeDecisionMainOptions.forEach(option => {
            option.checked = false;
        });

        view.editFeeDecisionOptions.forEach(option => {
            option.checked = false;
        });

        if (view.feeScholarshipMethodSchool) {
            view.feeScholarshipMethodSchool.checked = false;
        }
        if (view.feeScholarshipMethodEmail) {
            view.feeScholarshipMethodEmail.checked = false;
        }

        toggleElement(
            view.feeStepIndicator3,
            false
        );
        setFeeDiscussionStep(1);
    }

    async function handleFeeDiscussionWizardNext() {
        const step = state.feeDiscussionStep || 1;

        if (step === 1) {
            const amounts = updateFeeEditAmounts();

            if (amounts.total <= 0) {
                setFeeEditError('Enter at least one fee amount.');
                updateFeeDiscussionWizardButtonState();
                return;
            }

            const creating =
                state.feeDiscussionModalMode === 'CREATE';
            const feeChanged =
                !creating && hasFeeStructureChanged();
            const confirmationRequired =
                creating || feeChanged;

            if (
                confirmationRequired
                && !view.feeConfirmDetails?.checked
            ) {
                setFeeEditError(
                    'Confirm the fee details because the fee structure was changed.'
                );
                updateFeeDiscussionWizardButtonState();
                return;
            }

            setFeeEditError('');
            setFeeDiscussionStep(2);
            return;
        }

        if (step === 2) {
            const mainDecision =
                view.editFeeDecisionMainOptions.find(option =>
                    option.checked
                );

            const selected =
                String(mainDecision?.value || '')
                    .trim()
                    .toUpperCase();

            if (
                selected !== 'FULL_PAYMENT'
                && selected !== 'DECIDE_LATER'
                && selected !== 'SCHOLARSHIP'
            ) {
                setFeeEditError('Select the payment decision.');
                updateFeeDiscussionWizardButtonState();
                return;
            }

            if (
                state.feeDiscussionModalMode === 'EDIT'
                && selected === 'DECIDE_LATER'
            ) {
                setFeeEditError(
                    'Decide Later is available only when creating the Fee Discussion.'
                );
                updateFeeDiscussionWizardButtonState();
                return;
            }

            if (
                state.feeDiscussionModalMode === 'EDIT'
                && !String(
                    view.editFeeDiscussionRemarks?.value || ''
                ).trim()
            ) {
                setFeeEditError(
                    'Discussion Remarks are required when editing the Fee Discussion.'
                );
                view.editFeeDiscussionRemarks?.focus();
                updateFeeDiscussionWizardButtonState();
                return;
            }

            setFeeEditError('');

            if (
                selected === 'FULL_PAYMENT'
                || selected === 'DECIDE_LATER'
            ) {
                await saveFeeDiscussionModal();
                return;
            }

            setFeeDiscussionStep(3);
            return;
        }

        const decision = getFeeEditDecision();

        if (
            decision !== 'PARTIAL_ASSISTANCE'
            && decision !== 'FULL_ASSISTANCE'
        ) {
            setFeeEditError(
                'Select Partial Scholarship or Full Scholarship.'
            );
            updateFeeDiscussionWizardButtonState();
            return;
        }

        const method =
            view.feeScholarshipMethodSchool?.checked
                ? 'SCHOOL'
                : view.feeScholarshipMethodEmail?.checked
                    ? 'EMAIL'
                    : '';

        if (!method) {
            setFeeEditError(
                'Select the Scholarship Application method.'
            );
            updateFeeDiscussionWizardButtonState();
            return;
        }

        const amounts = updateFeeEditAmounts();
        const validationMessage = validateFeeDecision(amounts);

        if (validationMessage) {
            setFeeEditError(validationMessage);
            updateFeeDiscussionWizardButtonState();
            return;
        }

        const saved = await saveFeeDiscussionModal({
            keepOpen: true,
            silentSuccess: true
        });

        if (!saved) {
            return;
        }

        closeFeeEditModal();

        const currentStage =
            String(
                state.currentApplication?.currentStage
                || ''
            )
                .trim()
                .toUpperCase();

        if (currentStage === 'PARENT_FEE_DISCUSSION') {
            notifyIntermediateSuccess(
                'Fee Discussion saved. Use the Next action to continue to Scholarship.'
            );
            return;
        }

        if (currentStage !== 'SCHOLARSHIP') {
            setFeeEditError(
                'Fee Discussion was saved, but the application is not in the Scholarship stage yet.'
            );
            return;
        }

        if (method === 'SCHOOL') {
            await openScholarshipFormAtSchool();
        } else {
            await sendScholarshipApplicationLink();
        }
    }

    /**
     * Completes Fee Discussion Step 3 for a Scholarship decision.
     * The Fee Discussion record is saved first; the selected Scholarship
     * application method then performs the existing secure initiation flow.
     */
    async function processFeeDiscussionScholarshipStep() {
        const decision =
            getFeeEditDecision();

        if (
            decision !== 'PARTIAL_ASSISTANCE'
            && decision !== 'FULL_ASSISTANCE'
        ) {
            setFeeEditError(
                'Select Partial Scholarship or Full Scholarship.'
            );
            updateFeeDiscussionWizardButtonState();
            return;
        }

        const method =
            view.feeScholarshipMethodSchool?.checked
                ? 'SCHOOL'
                : view.feeScholarshipMethodEmail?.checked
                    ? 'EMAIL'
                    : '';

        if (!method) {
            setFeeEditError(
                'Select the Scholarship Application method.'
            );
            updateFeeDiscussionWizardButtonState();
            return;
        }

        const amounts =
            updateFeeEditAmounts();

        const validationMessage =
            validateFeeDecision(amounts);

        if (validationMessage) {
            setFeeEditError(validationMessage);
            updateFeeDiscussionWizardButtonState();
            return;
        }

        const saved =
            await saveFeeDiscussionModal({
                keepOpen: true,
                silentSuccess: true
            });

        if (!saved) {
            return;
        }

        closeFeeEditModal();

        /*
         * When this was an EDIT of an existing Payment/Scholarship decision,
         * the backend intentionally reopens Parent Fee Discussion. The
         * application must remain there until the user explicitly uses the
         * normal Next/Process workflow action.
         *
         * Do not finalize Fee Discussion or start Scholarship from this
         * button while the application is still in Parent Fee Discussion.
         */
        const currentStage =
            String(
                state.currentApplication?.currentStage
                || ''
            )
                .trim()
                .toUpperCase();

        if (currentStage === 'PARENT_FEE_DISCUSSION') {
            notifyIntermediateSuccess(
                'Fee Discussion saved. Use the Next action to continue to Scholarship.'
            );
            return;
        }

        if (currentStage !== 'SCHOLARSHIP') {
            setFeeEditError(
                'Fee Discussion was saved, but the application is not in the Scholarship stage yet.'
            );
            return;
        }

        if (method === 'SCHOOL') {
            await openScholarshipFormAtSchool();
            return;
        }

        await sendScholarshipApplicationLink();
    }

    function openFeeCreateModal() {
        if (!enumEquals(
            state.currentApplication?.currentStage,
            'PARENT_FEE_DISCUSSION'
        )) {
            return;
        }

        if (state.feeDiscussion?.feeId) {
            openFeeEditModal();
            return;
        }

        configureFeeDiscussionModal('CREATE');
        state.feeDiscussionOriginalFeeAmounts = null;

        [
            view.editFeeTermFee,
            view.editFeeTransportFee,
            view.editFeeHostelFee,
            view.editFeeUniformFee,
            view.editFeeBooksFee,
            view.editFeeAdmissionFee,
            view.editFeeOtherFee,
            view.editFeeParentCanPay
        ].forEach(input => setFeeInputValue(input, 0));

        view.editFeeDecisionOptions.forEach(option => {
            option.checked = false;
        });

        if (view.editFeeDiscussionRemarks) {
            view.editFeeDiscussionRemarks.value = '';
        }
        if (view.editFeeChangeReason) {
            view.editFeeChangeReason.value = '';
        }

        setFeeEditError('');
        resetFeeDiscussionWizard();
        updateFeeEditAmounts();
        openModal(view.feeEditModal);
        view.editFeeTermFee?.focus();
    }

    function openFeeEditModal() {
        const fee =
            state.feeDiscussion;

        configureFeeDiscussionModal(
            'EDIT'
        );

        if (!fee?.feeId) {
            notifyError(
                'No saved fee structure is available to edit.'
            );
            return;
        }

        setFeeInputValue(view.editFeeTermFee, fee.termFee);
        setFeeInputValue(view.editFeeTransportFee, fee.transportFee);
        setFeeInputValue(view.editFeeHostelFee, fee.hostelFee);
        setFeeInputValue(view.editFeeUniformFee, fee.uniformFee);
        setFeeInputValue(view.editFeeBooksFee, fee.booksFee);
        setFeeInputValue(view.editFeeAdmissionFee, fee.admissionFee);
        setFeeInputValue(view.editFeeOtherFee, fee.otherFee);
        setFeeInputValue(view.editFeeParentCanPay, fee.parentCanPay);

        state.feeDiscussionOriginalFeeAmounts = {
            termFee: Number(fee.termFee || 0),
            transportFee: Number(fee.transportFee || 0),
            hostelFee: Number(fee.hostelFee || 0),
            uniformFee: Number(fee.uniformFee || 0),
            booksFee: Number(fee.booksFee || 0),
            admissionFee: Number(fee.admissionFee || 0),
            otherFee: Number(fee.otherFee || 0)
        };

        const decision =
            String(
                fee.feeDecision || 'PENDING'
            ).toUpperCase();

        view.editFeeDecisionMainOptions.forEach(option => {
            option.checked =
                (
                    decision === 'FULL_PAYMENT'
                    && String(option.value).toUpperCase() === 'FULL_PAYMENT'
                )
                || (
                    (
                        decision === 'PARTIAL_ASSISTANCE'
                        || decision === 'FULL_ASSISTANCE'
                    )
                    && String(option.value).toUpperCase() === 'SCHOLARSHIP'
                );
        });

        view.editFeeDecisionOptions.forEach(option => {
            option.checked =
                String(option.value).toUpperCase()
                === decision;
        });

        if (view.editFeeDiscussionRemarks) {
            view.editFeeDiscussionRemarks.value =
                fee.discussionRemarks
                || fee.remarks
                || '';
        }

        if (view.editFeeChangeReason) {
            view.editFeeChangeReason.value = '';
        }

        setFeeEditError('');
        resetFeeDiscussionWizard();

        view.editFeeDecisionMainOptions.forEach(option => {
            option.checked =
                (
                    decision === 'FULL_PAYMENT'
                    && String(option.value).toUpperCase() === 'FULL_PAYMENT'
                )
                || (
                    (
                        decision === 'PARTIAL_ASSISTANCE'
                        || decision === 'FULL_ASSISTANCE'
                    )
                    && String(option.value).toUpperCase() === 'SCHOLARSHIP'
                );
        });

        view.editFeeDecisionOptions.forEach(option => {
            option.checked =
                String(option.value).toUpperCase()
                === decision;
        });

        renderFeeEditDecisionFields(decision);
        updateFeeEditAmounts();
        setFeeDiscussionStep(1);
        updateFeeDiscussionWizardButtonState();

        openModal(
            view.feeEditModal
        );
    }

    function closeFeeEditModal() {
        setFeeEditError('');

        if (view.editFeeChangeReason) {
            view.editFeeChangeReason.value = '';
        }

        state.feeDiscussionModalMode = null;
        state.feeDiscussionStep = 1;
        state.feeDiscussionScholarshipSaved = false;
        state.feeDiscussionOriginalFeeAmounts = null;

        closeModal(
            view.feeEditModal
        );
    }

    function getFeeEditDecision() {
        const mainDecision =
            view.editFeeDecisionMainOptions.find(option =>
                option.checked
            );

        const mainValue =
            String(mainDecision?.value || '')
                .trim()
                .toUpperCase();

        if (mainValue === 'FULL_PAYMENT') {
            return 'FULL_PAYMENT';
        }

        if (mainValue === 'DECIDE_LATER') {
            return 'PENDING';
        }

        if (mainValue === 'SCHOLARSHIP') {
            const subtype =
                view.editFeeDecisionOptions.find(option =>
                    option.checked
                    && (
                        String(option.value).toUpperCase() === 'PARTIAL_ASSISTANCE'
                        || String(option.value).toUpperCase() === 'FULL_ASSISTANCE'
                    )
                );

            return String(subtype?.value || 'SCHOLARSHIP').toUpperCase();
        }

        return 'PENDING';
    }

    function renderFeeEditDecisionFields(decision) {
        const normalized =
            String(decision || 'PENDING').toUpperCase();

        const scholarshipSelected =
            normalized === 'PARTIAL_ASSISTANCE'
            || normalized === 'FULL_ASSISTANCE';

        toggleElement(
            view.editFeeContributionPanel,
            scholarshipSelected
        );

        /*
         * Scholarship Application method belongs to Step 3.
         * The previous implementation always hid this panel, including when
         * Step 3 was active, which made Fill in School / Send Link impossible
         * to select and caused the wizard to stop at the last step.
         */
        const scholarshipMethodVisible =
            scholarshipSelected
            && (state.feeDiscussionStep || 1) === 3;

        toggleElement(
            view.editFeeScholarshipMethodPanel,
            scholarshipMethodVisible
        );

        if (view.editFeeParentCanPay) {
            view.editFeeParentCanPay.readOnly =
                normalized !== 'PARTIAL_ASSISTANCE';
        }
    }

    function updateFeeEditAmounts() {
        const total = [
            view.editFeeTermFee,
            view.editFeeTransportFee,
            view.editFeeHostelFee,
            view.editFeeUniformFee,
            view.editFeeBooksFee,
            view.editFeeAdmissionFee,
            view.editFeeOtherFee
        ].reduce(
            (sum, input) =>
                sum + readFeeInputValue(input),
            0
        );

        const decision =
            getFeeEditDecision();

        let parentCanPay =
            readFeeInputValue(
                view.editFeeParentCanPay
            );

        if (decision === 'FULL_PAYMENT') {
            parentCanPay = total;

            if (view.editFeeParentCanPay) {
                view.editFeeParentCanPay.value =
                    String(total);
            }
        }

        if (decision === 'FULL_ASSISTANCE') {
            parentCanPay = 0;

            if (view.editFeeParentCanPay) {
                view.editFeeParentCanPay.value = '0';
            }
        }

        const assistanceRequired =
            decision === 'PENDING'
                ? 0
                : Math.max(
                    total - parentCanPay,
                    0
                );

        if (view.editFeeBaseFeeAmount) {
            view.editFeeBaseFeeAmount.value =
                formatFeeAmount(total);
        }

        if (view.editFeeAssistanceRequired) {
            view.editFeeAssistanceRequired.value =
                formatFeeAmount(
                    assistanceRequired
                );
        }

        return {
            total,
            parentCanPay,
            assistanceRequired,
            decision
        };
    }

    function setFeeEditError(message) {
        if (!view.feeEditError) {
            return;
        }

        const text =
            String(message || '').trim();

        view.feeEditError.textContent = text;

        toggleElement(
            view.feeEditError,
            Boolean(text)
        );
    }

    async function saveFeeDiscussionModal(options = {}) {
        const {
            keepOpen = false,
            silentSuccess = false
        } = options || {};
        const applicationId =
            Number(state.currentApplicationId);

        if (!Number.isInteger(applicationId)
                || applicationId <= 0) {
            setFeeEditError(
                'A valid Application ID is required.'
            );
            return false;
        }

        const creating =
            state.feeDiscussionModalMode
                === 'CREATE';

        if (
            creating
            && !enumEquals(
                state.currentApplication?.currentStage,
                'PARENT_FEE_DISCUSSION'
            )
        ) {
            setFeeEditError(
                'Fee Discussion is not available at the current stage.'
            );
            return false;
        }

        const amounts =
            updateFeeEditAmounts();

        const validationMessage =
            validateFeeDecision(amounts);

        if (validationMessage) {
            setFeeEditError(validationMessage);
            return false;
        }

        const discussionRemarks =
            String(
                view.editFeeDiscussionRemarks?.value
                || ''
            ).trim();

        const changeReason =
            creating
                ? ''
                : discussionRemarks;

        if (!creating && !changeReason) {
            setFeeEditError(
                'Discussion Remarks are required when editing the Fee Discussion.'
            );
            view.editFeeDiscussionRemarks?.focus();
            return false;
        }

        if (!creating && amounts.decision === 'PENDING') {
            setFeeEditError(
                'Decide Later is available only when creating the Fee Discussion.'
            );
            return false;
        }

        const payload = {
            termFee:
                readFeeInputValue(view.editFeeTermFee),
            transportFee:
                readFeeInputValue(view.editFeeTransportFee),
            hostelFee:
                readFeeInputValue(view.editFeeHostelFee),
            uniformFee:
                readFeeInputValue(view.editFeeUniformFee),
            booksFee:
                readFeeInputValue(view.editFeeBooksFee),
            admissionFee:
                readFeeInputValue(view.editFeeAdmissionFee),
            otherFee:
                readFeeInputValue(view.editFeeOtherFee),
            parentCanPay:
                amounts.parentCanPay,
            feeDecision:
                amounts.decision,
            discussionRemarks:
                String(
                    view.editFeeDiscussionRemarks?.value
                    || ''
                ).trim(),
            changeReason:
                creating
                    ? null
                    : changeReason
        };

        let loaderToken = null;

        try {
            setFeeEditError('');

            setButtonBusy(
                view.feeEditSaveButton,
                true,
                creating
                    ? 'Saving...'
                    : 'Updating...'
            );

            if (typeof showLoader === 'function') {
                loaderToken = showLoader(
                    creating
                        ? 'Saving Fee Discussion...'
                        : 'Updating Fee Structure...'
                );
            }

            const response =
                await apiPatchJson(
                    `${API_ROOT}/${encodeURIComponent(
                        applicationId
                    )}/workflow/fee-discussion`,
                    payload
                );

            state.feeDiscussion =
                unwrapResponseData(response);

            const scholarshipDecision =
                amounts.decision === 'PARTIAL_ASSISTANCE'
                || amounts.decision === 'FULL_ASSISTANCE';

            const shouldKeepOpen =
                keepOpen
                || scholarshipDecision;

            if (!shouldKeepOpen) {
                closeFeeEditModal();
            }

            /*
             * Saving Fee Discussion is non-transitional. Reload the
             * authoritative profile so the dashboard and modal use the latest
             * persisted values before a later explicit Payment/Scholarship
             * continuation action.
             */
            const synchronized =
                await synchronizeCurrentApplicationAfterMutation(
                    creating
                        ? 'fee-discussion-create'
                        : 'fee-discussion-edit',
                    applicationId
                );

            if (synchronized !== true) {
                renderFeeDiscussion(
                    state.feeDiscussion
                );

                await loadProfileTransitions(
                    applicationId
                );
            }

            if (shouldKeepOpen) {
                state.feeDiscussionModalMode = 'EDIT';
                state.feeDiscussionScholarshipSaved = true;
                setFeeEditError('');
            }

            if (!silentSuccess) {
                notifyIntermediateSuccess(
                    creating
                        ? 'Fee discussion saved successfully.'
                        : 'Fee structure updated successfully.'
                );
            }

            return true;
        } catch (error) {
            setFeeEditError(
                readErrorMessage(
                    error,
                    creating
                        ? 'Fee discussion could not be saved.'
                        : 'Fee structure could not be updated.'
                )
            );

            return false;
        } finally {
            setButtonBusy(
                view.feeEditSaveButton,
                false
            );

            if (
                loaderToken != null
                && typeof hideLoader === 'function'
            ) {
                hideLoader(loaderToken);
            }
        }
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

        const canScheduleRetest =
            enumEquals(
                currentStage,
                'ENTRANCE_TEST'
            )
            && status === 'COMPLETED'
            && result === 'RETEST_REQUIRED'
            && test.canRequestRetest === true
            && test.canSchedule === true
            && test.applicationWaitlisted !== true;

        toggleElement(
            view.entranceTestRetestButton,
            canScheduleRetest
        );

        const legacyWaitlistResultAction =
            enumEquals(
                currentStage,
                'ENTRANCE_TEST'
            )
            && status === 'COMPLETED'
            && result === 'WAITLIST'
            && test.canUpdateWaitlistResult === true;

        const applicationWaitlistAction =
            enumEquals(
                currentStage,
                'ENTRANCE_TEST'
            )
            && status === 'COMPLETED'
            && (result === 'PASSED' || result === 'FAILED')
            && (
                test.canPlaceOnWaitlist === true
                || test.canReleaseFromWaitlist === true
            );

        toggleElement(
            view.entranceTestUpdateResultButton,
            legacyWaitlistResultAction
            || applicationWaitlistAction
        );

        if (view.entranceTestUpdateResultButton) {
            if (legacyWaitlistResultAction) {
                view.entranceTestUpdateResultButton.innerHTML =
                    '<i class="bi bi-arrow-repeat"></i> Update Waitlist Result';
            } else if (test.applicationWaitlisted === true) {
                view.entranceTestUpdateResultButton.innerHTML =
                    '<i class="bi bi-box-arrow-up-right"></i> Release from Waitlist';
            } else {
                view.entranceTestUpdateResultButton.innerHTML =
                    '<i class="bi bi-hourglass-split"></i> Place on Waitlist';
            }
        }

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

            if (test.applicationStatus) {
                state.currentApplication.applicationStatus =
                    test.applicationStatus;
            }
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


    function openEntranceTestRetestModal() {
        const test =
            state.entranceTest;

        if (!test
                || !state.currentApplicationId
                || test.canSchedule !== true
                || test.canRequestRetest !== true
                || String(test.status || '').toUpperCase() !== 'COMPLETED'
                || String(test.result || '').toUpperCase() !== 'RETEST_REQUIRED'
                || test.applicationWaitlisted === true) {
            return;
        }

        view.entranceTestRetestForm?.reset();
        clearCalendarInput(
            view.entranceTestRetestScheduledAt
        );
        clearInlineError(
            view.entranceTestRetestError
        );

        openModal(
            view.entranceTestRetestModal
        );
    }

    function closeEntranceTestRetestModal() {
        closeModal(
            view.entranceTestRetestModal
        );

        view.entranceTestRetestForm?.reset();

        clearCalendarInput(
            view.entranceTestRetestScheduledAt
        );

        clearInlineError(
            view.entranceTestRetestError
        );
    }

    async function submitEntranceTestRetest() {
        const test =
            state.entranceTest;

        if (!test || !state.currentApplicationId) {
            return;
        }

        clearInlineError(
            view.entranceTestRetestError
        );

        const scheduledAt =
            trimValue(
                view.entranceTestRetestScheduledAt
            );

        if (!scheduledAt) {
            showInlineError(
                view.entranceTestRetestError,
                'Retest date and time are required.'
            );
            return;
        }

        const employeeId =
            Number(test.employeeId);

        if (!Number.isFinite(employeeId)
                || employeeId <= 0) {
            showInlineError(
                view.entranceTestRetestError,
                'The responsible Entrance Test employee is unavailable.'
            );
            return;
        }

        const remarks =
            trimValue(
                view.entranceTestRetestRemarks
            );

        let loaderToken = null;

        try {
            setButtonBusy(
                view.entranceTestRetestSaveButton,
                true,
                'Scheduling...'
            );

            if (typeof showLoader === 'function') {
                loaderToken =
                    showLoader(
                        'Scheduling Entrance Test retest...'
                    );
            }

            const response =
                await apiPost(
                    `${API_ROOT}/${encodeURIComponent(
                        state.currentApplicationId
                    )}/workflow/entrance-test/schedule`,
                    {
                        employeeId,
                        scheduledAt,
                        employeeRemarks:
                            remarks || null,
                        internalRemarks:
                            null
                    }
                );

            state.entranceTest =
                unwrapResponseData(
                    response
                );

            closeEntranceTestRetestModal();

            renderEntranceTest(
                state.entranceTest
            );

            const refreshed =
                await synchronizeCurrentApplicationAfterMutation(
                    'entrance-test-retest-scheduled',
                    Number(
                        state.currentApplicationId
                    )
                );

            if (!refreshed) {
                throw new Error(
                    'The retest was scheduled, but the refreshed Application profile could not be loaded.'
                );
            }

            notifyIntermediateSuccess(
                'Entrance Test retest scheduled successfully.'
            );
        } catch (error) {
            showInlineError(
                view.entranceTestRetestError,
                error?.message
                    || 'Unable to schedule the Entrance Test retest.'
            );
        } finally {
            setButtonBusy(
                view.entranceTestRetestSaveButton,
                false,
                '<i class="bi bi-arrow-repeat"></i> Schedule Retest'
            );

            if (loaderToken != null
                    && typeof hideLoader === 'function') {
                hideLoader(loaderToken);
            }
        }
    }

    function openWaitlistResultModal() {
        const test =
            state.entranceTest;

        if (!test) {
            notifyError(
                'Entrance Test details are unavailable.'
            );
            return;
        }

        const status =
            String(test.status || '').toUpperCase();

        const result =
            String(test.result || '').toUpperCase();

        const legacyWaitlistResultAction =
            status === 'COMPLETED'
            && result === 'WAITLIST'
            && test.canUpdateWaitlistResult === true;

        const applicationWaitlistAction =
            status === 'COMPLETED'
            && (result === 'PASSED' || result === 'FAILED')
            && (
                test.canPlaceOnWaitlist === true
                || test.canReleaseFromWaitlist === true
            );

        if (
            !legacyWaitlistResultAction
            && !applicationWaitlistAction
        ) {
            notifyError(
                'This Entrance Test is not available for a waitlist action.'
            );
            return;
        }

        state.waitlistActionMode =
            legacyWaitlistResultAction
                ? 'LEGACY_RESULT'
                : 'APPLICATION_WAITLIST';

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

        const releasing =
            test.applicationWaitlisted === true;

        toggleElement(
            view.waitlistFinalDecisionGroup,
            legacyWaitlistResultAction
        );

        if (view.waitlistCurrentResultInput) {
            view.waitlistCurrentResultInput.value =
                legacyWaitlistResultAction
                    ? 'Waitlist'
                    : formatEnum(result);
        }

        if (legacyWaitlistResultAction) {
            setNodeText(
                view.waitlistResultTitle,
                'Update Legacy Waitlist Result'
            );

            setNodeText(
                view.waitlistResultDescription,
                'Finalize this legacy waitlisted Entrance Test as Pass or Fail.'
            );

            setNodeText(
                view.waitlistResultRemarksLabel,
                'Decision Remarks *'
            );

            if (view.waitlistResultSaveButton) {
                view.waitlistResultSaveButton.innerHTML =
                    '<i class="bi bi-check-circle"></i> Save Final Result';
            }
        } else {
            setNodeText(
                view.waitlistResultTitle,
                releasing
                    ? 'Release from Waitlist'
                    : 'Place on Waitlist'
            );

            setNodeText(
                view.waitlistResultDescription,
                releasing
                    ? 'Release this application from the waitlist without changing its Entrance Test result or marks.'
                    : 'Place this application on the waitlist without changing its Entrance Test result or marks.'
            );

            setNodeText(
                view.waitlistResultRemarksLabel,
                releasing
                    ? 'Release Remarks *'
                    : 'Waitlist Remarks *'
            );

            if (view.waitlistResultSaveButton) {
                view.waitlistResultSaveButton.innerHTML =
                    releasing
                        ? '<i class="bi bi-box-arrow-up-right"></i> Release from Waitlist'
                        : '<i class="bi bi-hourglass-split"></i> Place on Waitlist';
            }
        }

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

        const test =
            state.entranceTest;

        if (!test) {
            notifyError(
                'Entrance Test details are unavailable.'
            );
            return;
        }

        const remarks =
            String(
                view.waitlistResultRemarks?.value
                || ''
            ).trim();

        if (!remarks) {
            showInlineError(
                view.waitlistResultError,
                'Remarks are required.'
            );
            return;
        }

        const legacyMode =
            state.waitlistActionMode
            === 'LEGACY_RESULT';

        let result = null;

        if (legacyMode) {
            result =
                String(
                    view.waitlistFinalResultSelect?.value
                    || ''
                ).toUpperCase();

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
                        legacyMode
                            ? 'Saving Entrance Test final result...'
                            : test.applicationWaitlisted === true
                                ? 'Releasing application from waitlist...'
                                : 'Placing application on waitlist...'
                    );
            }

            if (legacyMode) {
                await apiPatchJson(
                    `${API_ROOT}/${encodeURIComponent(
                        applicationId
                    )}/workflow/entrance-test/waitlist-result`,
                    {
                        result,
                        remarks
                    }
                );
            } else {
                await apiPatchJson(
                    `${API_ROOT}/${encodeURIComponent(
                        applicationId
                    )}/workflow/entrance-test/waitlist`,
                    {
                        waitlisted:
                            test.applicationWaitlisted !== true,
                        remarks
                    }
                );
            }

            const wasWaitlisted =
                test.applicationWaitlisted === true;

            closeWaitlistResultModal();

            const refreshed =
                await synchronizeCurrentApplicationAfterMutation(
                    legacyMode
                        ? 'entrance-test-waitlist-result'
                        : 'entrance-test-application-waitlist',
                    applicationId
                );

            if (!refreshed) {
                throw new Error(
                    legacyMode
                        ? 'The final result was saved, but the refreshed Application profile could not be loaded.'
                        : 'The waitlist action was saved, but the refreshed Application profile could not be loaded.'
                );
            }

            notifyIntermediateSuccess(
                legacyMode
                    ? result === 'PASSED'
                        ? 'Legacy waitlist result updated to Pass successfully.'
                        : 'Legacy waitlist result updated to Fail successfully.'
                    : wasWaitlisted
                        ? 'Application released from waitlist successfully.'
                        : 'Application placed on waitlist successfully.'
            );
        } catch (error) {
            const message =
                readErrorMessage(
                    error,
                    legacyMode
                        ? 'Entrance Test result could not be updated.'
                        : 'Application waitlist status could not be updated.'
                );

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
                hideLoader(loaderToken);
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

    function configureEntranceTestResultOptions(test) {
        const select =
            view.entranceTestResultSelect;

        if (!select) {
            return;
        }

        const currentValue =
            String(select.value || '').toUpperCase();

        const canRequestRetest =
            test?.canRequestRetest === true;

        select.replaceChildren();

        const options = [
            { value: '', label: '-- Select Result --' },
            { value: 'PASSED', label: 'Pass' },
            { value: 'FAILED', label: 'Fail' },
            { value: 'WAITLIST', label: 'Waitlist' }
        ];

        if (canRequestRetest) {
            options.push({
                value: 'RETEST_REQUIRED',
                label: 'Retest Required'
            });
        }

        options.forEach(optionData => {
            const option =
                document.createElement('option');
            option.value = optionData.value;
            option.textContent = optionData.label;
            select.appendChild(option);
        });

        if (options.some(option => option.value === currentValue)) {
            select.value = currentValue;
        } else {
            select.value = '';
        }
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

        configureEntranceTestResultOptions(test);

        if (view.entranceTestResultSelect) {
            const result =
                test.result
                && String(test.result).toUpperCase() !== 'PENDING'
                    ? String(test.result).toUpperCase()
                    : '';

            const allowedResult =
                Array.from(
                    view.entranceTestResultSelect.options
                ).some(
                    option => option.value === result
                );

            view.entranceTestResultSelect.value =
                allowedResult ? result : '';
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

            if (result === 'RETEST_REQUIRED'
                    && state.entranceTest?.canRequestRetest !== true) {
                throw new Error(
                    'Retest Required is available only for the first Entrance Test attempt.'
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

            notifyIntermediateSuccess(
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

        updateCompactSchoolVisitSummary(
            schoolVisit
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

                notifyIntermediateSuccess(
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

            notifyIntermediateSuccess(
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

        /*
         * The active Application Profile is the authoritative UI for the
         * admission workflow. After every successful mutation, reload the
         * application and all stage-owned resources FIRST.
         *
         * Do not trust a generic global-sync "handled" flag to mean that the
         * current profile was actually refreshed.
         */
        let synchronized =
            await openApplication(
                id,
                {
                    preservePosition: true,
                    silent: true
                }
            );

        if (synchronized !== true) {
            return false;
        }

        /*
         * Refresh list/global observers only after the active profile is
         * authoritative. Failure here must not make the workflow appear to
         * have failed when the profile itself is already synchronized.
         */
        if (
            typeof window.erpFlushDataSync
            === 'function'
        ) {
            try {
                await window.erpFlushDataSync({
                    source,
                    applicationId: id
                });
            } catch (error) {
                console.warn(
                    'Global Applications synchronization failed after the active profile was refreshed.',
                    error
                );
            }
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

            notifyIntermediateSuccess(
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

            notifyIntermediateSuccess(
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

            notifyIntermediateSuccess(
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

            notifyIntermediateSuccess(
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
                || state.documentSyncBusy
                || state.profileLoadBusy) {
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

            const previousStage =
                state.currentApplication?.currentStage;

            /*
             * A stage change invalidates all stage-owned UI. Do not patch only
             * documents/transitions while leaving Fee Discussion, Scholarship,
             * School Visit or Entrance Test stale.
             *
             * Perform one complete authoritative profile reload instead.
             */
            if (
                latestApplication?.currentStage
                && !enumEquals(
                    previousStage,
                    latestApplication.currentStage
                )
            ) {
                await openApplication(
                    expectedApplicationId,
                    {
                        preservePosition: true,
                        silent: true
                    }
                );

                return;
            }

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

                    if (document.hidden
                            || state.profileLoadBusy
                            || state.documentSyncBusy) {
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

        view.root?.classList.remove(
            'app-profile-mode'
        );

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

                /*
                 * A successful workflow transition can change both the major
                 * stage and the stage-owned UI/data that must now be shown.
                 *
                 * Do not refresh only School Visit + transitions here.
                 * Reopen/synchronize the authoritative Application Profile so
                 * the correct stage section, actions and stage-specific data
                 * are loaded immediately.
                 *
                 * Examples:
                 * ENTRANCE_TEST -> PARENT_FEE_DISCUSSION
                 *     loads the first-time manual Fee Discussion form.
                 *
                 * PARENT_FEE_DISCUSSION -> SCHOLARSHIP/PAYMENT
                 *     loads the new stage and its actions immediately.
                 */
                applyWorkflowResponseToProfile(
                    transitionResponse
                );

                const synchronized =
                    await synchronizeCurrentApplicationAfterMutation(
                        'workflow-transition',
                        applicationId
                    );

                if (synchronized !== true) {
                    /*
                     * Last-resort local refresh. The synchronization helper
                     * already attempts openApplication(), but keep transitions
                     * fresh if a host/global synchronizer reports failure.
                     */
                    await loadProfileTransitions(
                        applicationId
                    );
                }
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

        notifyIntermediateSuccess(
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

        notifyIntermediateSuccess(
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

        const actionAvailable =
            Boolean(transition);

        toggleElement(
            view.profileNextStageButton,
            actionAvailable
        );

        view.profileNextStageButton.disabled =
            !actionAvailable;

        view.profileNextStageButton.setAttribute(
            'aria-disabled',
            String(!actionAvailable)
        );

        if (!actionAvailable) {
            setNodeText(
                view.profileNextStageButtonLabel,
                'Next Action'
            );

            view.profileNextStageButton.title =
                'No stage transition is currently available. Use the active stage section for any required action.';

            return;
        }

        const buttonLabel =
            compactWorkflowActionLabel(
                transition.label,
                transition.targetStage
            );

        setNodeText(
            view.profileNextStageButtonLabel,
            buttonLabel
        );

        view.profileNextStageButton.title =
            transition?.label
                ? String(transition.label)
                : buttonLabel;
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

        if (state.currentApplication) {
            setText(
                'summary-appCurrentStageStatus',
                resolveProfileStageStatus(
                    state.currentApplication
                )
            );

            renderWorkflowProgress(
                state.currentApplication
            );
        }

        if (state.currentApplication) {
            setText(
                'summary-appCurrentStageStatus',
                resolveProfileStageStatus(
                    state.currentApplication
                )
            );

            setText(
                'summary-appDocuments',
                formatEnum(
                    state.currentApplication.documentStatus
                )
            );

            renderWorkflowProgress(
                state.currentApplication
            );
        }
        setText(
            'view-paymentStatus',
            formatEnum(response.paymentStatus)
        );
        setText(
            'view-admissionStatus',
            formatEnum(response.admissionStatus)
        );
    }

    function resolveLegacyStageStatus(record) {
        const stage =
            String(
                record?.currentStage
                || ''
            ).trim().toUpperCase();

        if (stage === 'APPLICATION_VERIFICATION') {
            return `Documents: ${
                formatEnum(record?.documentStatus)
            }`;
        }

        if (stage === 'SCHOOL_VISIT') {
            return `Visit: ${
                formatEnum(record?.schoolVisitStatus)
            }`;
        }

        if (stage === 'ENTRANCE_TEST') {
            return `Test: ${
                formatEnum(record?.testStatus)
            }`;
        }

        if (stage === 'PARENT_FEE_DISCUSSION') {
            return `Fee: ${
                formatEnum(record?.feeDecisionStatus)
            }`;
        }

        if (stage === 'SCHOLARSHIP') {
            return `Scholarship: ${
                formatEnum(record?.scholarshipStatus)
            }`;
        }

        if (stage === 'PAYMENT') {
            return `Payment: ${
                formatEnum(record?.paymentStatus)
            }`;
        }

        if (stage === 'FINAL_ADMISSION') {
            return `Admission: ${
                formatEnum(record?.admissionStatus)
            }`;
        }

        if (stage === 'ENROLLED') {
            return 'Completed';
        }

        if (stage === 'CLOSED') {
            return 'Application: Closed';
        }

        return 'Stage: Not Available';
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

        const activeElement =
            window.document.activeElement;

        modal.__erpPreviouslyFocusedElement =
            activeElement
                    && activeElement !== window.document.body
                    && !modal.contains(activeElement)
                ? activeElement
                : null;

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

        /*
         * A focused element must not remain inside a container that is about
         * to become aria-hidden. Move focus out first, then hide the modal.
         */
        const activeElement =
            window.document.activeElement;

        if (activeElement
                && modal.contains(activeElement)
                && typeof activeElement.blur === 'function') {
            activeElement.blur();
        }

        const previousFocus =
            modal.__erpPreviouslyFocusedElement;

        if (previousFocus
                && previousFocus.isConnected
                && typeof previousFocus.focus === 'function') {
            try {
                previousFocus.focus({
                    preventScroll: true
                });
            } catch (_) {
                previousFocus.focus();
            }
        }

        modal.__erpPreviouslyFocusedElement =
            null;

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

    /**
     * Non-blocking success feedback for intermediate admission-workflow steps.
     *
     * Never opens the global success modal/window. The workflow should refresh
     * and continue immediately to the next required section.
     */
    function notifyIntermediateSuccess(
        message
    ) {
        if (typeof Toast !== 'undefined'
                && Toast
                && typeof Toast.success === 'function') {
            Toast.success(message);
            return;
        }

        console.log(message);
    }

    /**
     * Blocking success confirmation.
     *
     * Reserve this for major completion/final-decision points only, such as
     * final Scholarship submission, completed verification, completed payment,
     * final admission, or enrollment.
     */
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

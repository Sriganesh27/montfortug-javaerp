(() => {
    'use strict';

    const SCHOOL_BASE =
        '/api/admission/branch/scholarship/application-form';

    const PUBLIC_BASE =
        '/api/public/scholarship/application-form';

    const SCHOOL_HEADER =
        'X-Scholarship-School-Access';

    const PUBLIC_HEADER =
        'X-Scholarship-Access';

    let accessMode = null;
    let secureCredential = null;
    let currentFormData = null;
    let currentStep = 1;
    let schoolLogoObjectUrl = null;

    document.addEventListener(
        'DOMContentLoaded',
        initialize
    );

    function initialize() {
        showPageLoader(
            'Checking secure Scholarship access...'
        );

        const credentials =
            readCredentialFromUrl();

        accessMode = credentials.mode;
        secureCredential = credentials.credential;

        if (secureCredential) {
            window.history.replaceState(
                null,
                document.title,
                '/scholarship-application'
            );
        }

        bindEvents();

        if (!secureCredential || !accessMode) {
            showUnavailable(
                'The Scholarship Application link is missing or invalid.'
            );
            hidePageLoader();
            return;
        }

        void loadForm();
    }

    function readCredentialFromUrl() {
        /*
         * Preferred format:
         *   /scholarship-application#schoolKey=...
         *   /scholarship-application#token=...
         *
         * URL fragments are not sent to the web server during the page
         * request, which keeps the raw credential out of normal access logs.
         *
         * Query-string parsing remains temporarily for migration compatibility
         * with links generated before this change.
         */
        const fragment =
            window.location.hash
                ? window.location.hash.substring(1)
                : '';

        const fragmentParams =
            new URLSearchParams(
                fragment
            );

        const queryParams =
            new URLSearchParams(
                window.location.search
            );

        const schoolKey =
            cleanString(
                fragmentParams.get('schoolKey')
                || queryParams.get('schoolKey')
            );

        const publicToken =
            cleanString(
                fragmentParams.get('token')
                || queryParams.get('token')
            );

        if (schoolKey) {
            return {
                mode: 'school',
                credential: schoolKey
            };
        }

        if (publicToken) {
            return {
                mode: 'public',
                credential: publicToken
            };
        }

        return {
            mode: null,
            credential: null
        };
    }

    function bindEvents() {
        byId('scholarshipForm')
            ?.addEventListener(
                'submit',
                event => {
                    event.preventDefault();
                    void submitForm();
                }
            );

        byId('saveDraftBtn')
            ?.addEventListener(
                'click',
                () => {
                    void saveDraft();
                }
            );

        byId('addSiblingBtn')
            ?.addEventListener(
                'click',
                () => {
                    appendSiblingRow({});
                }
            );

        [
            'housingStatus',
            'landOwned',
            'landGeneratesIncome',
            'vehiclesOwned',
            'businessOwned',
            'livestockOwned'
        ].forEach(
            id => {
                byId(id)?.addEventListener(
                    'change',
                    updateAssetVisibility
                );
            }
        );

        [
            'fatherStatus',
            'motherStatus'
        ].forEach(id => {
            byId(id)?.addEventListener(
                'change',
                updateFamilyVisibility
            );
        });

        byId('otherAssetsOwned')?.addEventListener(
            'change',
            () => {
                updateAssetVisibility();
                clearInactiveAssetValues();
            }
        );

        document.querySelectorAll(
            '[data-scholarship-next]'
        ).forEach(button => {
            button.addEventListener(
                'click',
                () => void saveDraftAndMove(
                    Number(button.dataset.scholarshipNext),
                    button
                )
            );
        });

        document.querySelectorAll(
            '[data-scholarship-back]'
        ).forEach(button => {
            button.addEventListener(
                'click',
                () => showStep(
                    Number(button.dataset.scholarshipBack)
                )
            );
        });

        document.querySelectorAll(
            '#scholarship-stepper [data-step-target]'
        ).forEach(item => {
            item.addEventListener(
                'click',
                () => {
                    const target =
                        Number(item.dataset.stepTarget);

                    if (target <= currentStep) {
                        showStep(target);
                    }
                }
            );
        });

        byId('returnToApplicationBtn')
            ?.addEventListener(
                'click',
                returnToApplicant
            );
    }

    async function loadForm() {
        showPageLoader(
            'Loading Scholarship Application...'
        );

        try {
            /*
             * Start the independent school-logo request at the same time as
             * the secure application request. The page remains hidden until
             * both operations have completed.
             */
            const schoolLogoPromise =
                loadSchoolLogo(true);

            const response =
                await secureFetch(
                    endpointBase(),
                    {
                        method: 'GET'
                    }
                );

            const payload =
                await readPayload(
                    response
                );

            await schoolLogoPromise;

            const data =
                extractData(
                    payload
                );

            if (!response.ok || !data) {
                throw new Error(
                    extractMessage(
                        payload,
                        'Scholarship Application could not be loaded.'
                    )
                );
            }

            currentFormData = data;

            renderForm(
                data
            );

            /*
             * The page is revealed only after the complete application
             * response has been rendered. The school logo request is started
             * together with the form request and its failure is non-fatal.
             */
            await loadSchoolLogo(true);

            hide('scholarshipLoadingShell');
            show('scholarshipForm');
            showStep(
                1,
                false
            );

        } catch (error) {
            showUnavailable(
                safeMessage(
                    error,
                    'Scholarship Application could not be loaded.'
                )
            );
        } finally {
            hidePageLoader();
        }
    }

    function renderForm(data) {
        renderSchoolIdentity(data);

        setText(
            'scholarshipProfileStudentName',
            data.studentName
        );
        setText(
            'scholarshipProfileApplicationNo',
            data.applicationNo ? `Application ${data.applicationNo}` : 'Application —'
        );
        setText(
            'scholarshipProfileClass',
            data.className ? `Class ${data.className}` : 'Class —'
        );
        setText(
            'scholarshipProfilePeriod',
            [data.academicYear, data.term].filter(Boolean).join(' / ') || 'Academic period —'
        );
        setText(
            'scholarshipProfileAdmissionStatus',
            `Admission ${formatStatus(data.admissionStatus || '—')}`
        );
        setText(
            'scholarshipProfileScholarshipStatus',
            `Scholarship ${formatStatus(data.status || '—')}`
        );
        setText('profileTotalFee', formatMoney(data.totalFee));
        setText('profileParentContribution', formatMoney(data.parentContribution));
        setText('profileScholarshipRequired', formatMoney(data.scholarshipRequiredAmount));

        setText(
            'admissionGender',
            formatStatus(data.gender || '—')
        );
        setText('admissionDob', data.dateOfBirth || '—');
        setText('admissionNationality', data.nationality || '—');
        setText('admissionLevel', data.levelName || data.level || '—');
        setText('admissionType', formatStatus(data.admissionType || '—'));
        setText('admissionMobile', data.primaryMobile || data.mobile || '—');
        setText('admissionEmail', data.primaryEmail || data.email || '—');
        setText(
            'admissionLocation',
            [data.addressDistrict, data.addressVillage, data.addressStreet]
                .filter(Boolean)
                .join(', ') || data.location || '—'
        );

        setText(
            'applicationNo',
            data.applicationNo
        );
        setText(
            'studentName',
            data.studentName
        );
        setText(
            'className',
            data.className
        );
        setText(
            'academicPeriod',
            [
                data.academicYear,
                data.term
            ].filter(Boolean).join(' / ')
        );

        setText(
            'totalFee',
            formatMoney(
                data.totalFee
            )
        );
        setText(
            'parentContribution',
            formatMoney(
                data.parentContribution
            )
        );
        setText(
            'scholarshipRequiredAmount',
            formatMoney(
                data.scholarshipRequiredAmount
            )
        );

        setText(
            'scholarshipModeText',
            accessMode === 'school'
                ? 'School-assisted Scholarship Application'
                : 'Parent / Guardian Scholarship Application'
        );

        const scalarFields = [
            'fatherName',
            'fatherContact',
            'fatherEmail',
            'fatherOccupation',
            'fatherStatus',
            'fatherAnnualIncome',
            'motherName',
            'motherContact',
            'motherEmail',
            'motherOccupation',
            'motherStatus',
            'motherAnnualIncome',
            'guardianName',
            'guardianRelation',
            'guardianContact',
            'guardianEmail',
            'guardianOccupation',
            'responsiblePersonType',
            'responsiblePersonName',
            'responsiblePersonRelation',
            'responsiblePersonOccupation',
            'responsiblePersonMobile',
            'responsiblePersonAnnualIncome',
            'orphanStatus',
            'householdSize',
            'dependantsCount',
            'schoolGoingChildren',
            'mainIncomeEarner',
            'incomeSource',
            'otherHouseholdIncome',
            'housingStatus',

            'houseType',
            'houseRoomCount',
            'houseLocation',
            'houseEstimatedValue',
            'monthlyRent',

            'landArea',
            'landUnit',
            'landPlotCount',
            'landLocation',
            'landUsage',
            'landEstimatedValue',
            'landAnnualIncome',

            'vehicleCount',
            'vehicleDescription',
            'vehicleType',
            'vehicleUsage',
            'vehicleEstimatedValue',

            'businessName',
            'businessType',
            'businessLocation',
            'businessEmployeeCount',
            'businessAnnualIncome',

            'livestockDescription',
            'livestockEstimatedValue',
            'livestockAnnualIncome',

            'otherAssetsDescription',
            'otherAssetsEstimatedValue',
            'otherAssetsAnnualIncome',

            'financialHardshipReason',
            'familySituationRemarks',
            'parentGuardianName',
            'parentGuardianRelation',
            'parentGuardianMobile'
        ];

        scalarFields.forEach(
            fieldName => {
                setValue(
                    fieldName,
                    data[fieldName]
                );
            }
        );

        setValue(
            'householdIncome',
            formatMoney(
                data.householdIncome
            )
        );

        byId('houseMortgaged').checked =
            data.houseMortgaged === true;

        byId('landOwned').checked =
            data.landOwned === true;

        byId('landGeneratesIncome').checked =
            data.landGeneratesIncome === true;

        byId('vehiclesOwned').checked =
            data.vehiclesOwned === true;

        byId('vehicleFinanced').checked =
            data.vehicleFinanced === true;

        byId('businessOwned').checked =
            data.businessOwned === true;

        byId('livestockOwned').checked =
            data.livestockOwned === true;

        byId('declarationAccepted').checked =
            data.declarationAccepted === true;

        renderSiblings(
            Array.isArray(data.siblings)
                ? data.siblings
                : []
        );

        updateAssetVisibility();
    }

    async function saveDraft() {
        await persist(
            false
        );
    }

    async function submitForm() {
        if (!byId('declarationAccepted')?.checked) {
            showFormMessage(
                'Please accept the declaration before submitting.',
                true
            );
            return;
        }

        await persist(
            true
        );
    }

    async function persist(submit) {
        const saveButton =
            byId('saveDraftBtn');

        const submitButton =
            byId('submitApplicationBtn');

        setBusy(
            saveButton,
            true
        );
        setBusy(
            submitButton,
            true
        );

        hide('formMessage');
        clearInactiveAssetValues();

        showPageLoader(
            submit
                ? 'Submitting Scholarship Application...'
                : 'Saving Scholarship Application...'
        );

        try {
            const url =
                submit
                    ? `${endpointBase()}/submit`
                    : endpointBase();

            const response =
                await secureFetch(
                    url,
                    {
                        method:
                            submit
                                ? 'POST'
                                : 'PATCH',
                        headers: {
                            'Content-Type':
                                'application/json'
                        },
                        body:
                            JSON.stringify(
                                collectFormPayload()
                            )
                    }
                );

            const payload =
                await readPayload(
                    response
                );

            const data =
                extractData(
                    payload
                );

            if (!response.ok || !data) {
                throw new Error(
                    extractMessage(
                        payload,
                        submit
                            ? 'Scholarship Application could not be submitted.'
                            : 'Scholarship Application draft could not be saved.'
                    )
                );
            }

            currentFormData = data;

            if (submit) {
                secureCredential = null;

                hide(
                    'scholarshipForm'
                );
                show(
                    'scholarshipComplete'
                );

                if (accessMode === 'school') {
                    show(
                        'returnToApplicationBtn'
                    );
                }

                return true;
            }

            renderForm(
                data
            );

            showFormMessage(
                'Draft saved successfully.',
                false
            );

            return true;

        } catch (error) {
            showFormMessage(
                safeMessage(
                    error,
                    'Scholarship Application could not be saved.'
                ),
                true
            );

            return false;
        } finally {
            setBusy(
                saveButton,
                false
            );
            setBusy(
                submitButton,
                false
            );

            hidePageLoader();
        }
    }

    function collectFormPayload() {
        return {
            fatherName: value('fatherName'),
            fatherContact: value('fatherContact'),
            fatherEmail: value('fatherEmail'),
            fatherOccupation: value('fatherOccupation'),

            motherName: value('motherName'),
            motherContact: value('motherContact'),
            motherEmail: value('motherEmail'),
            motherOccupation: value('motherOccupation'),

            guardianName: value('guardianName'),
            guardianRelation: value('guardianRelation'),
            guardianContact: value('guardianContact'),
            guardianEmail: value('guardianEmail'),
            guardianOccupation: value('guardianOccupation'),

            fatherStatus: value('fatherStatus'),
            fatherAnnualIncome: numberValue('fatherAnnualIncome'),

            motherStatus: value('motherStatus'),
            motherAnnualIncome: numberValue('motherAnnualIncome'),

            responsiblePersonType: value('responsiblePersonType'),
            responsiblePersonName: value('responsiblePersonName'),
            responsiblePersonRelation: value('responsiblePersonRelation'),
            responsiblePersonOccupation: value('responsiblePersonOccupation'),
            responsiblePersonMobile: value('responsiblePersonMobile'),
            responsiblePersonAnnualIncome:
                numberValue('responsiblePersonAnnualIncome'),

            orphanStatus: value('orphanStatus'),

            householdSize: integerValue('householdSize'),
            dependantsCount: integerValue('dependantsCount'),
            schoolGoingChildren: integerValue('schoolGoingChildren'),

            mainIncomeEarner: value('mainIncomeEarner'),
            incomeSource: value('incomeSource'),
            otherHouseholdIncome:
                numberValue('otherHouseholdIncome'),

            housingStatus: value('housingStatus'),

            houseType:
                value('housingStatus') === 'OWNED'
                    ? value('houseType')
                    : null,
            houseRoomCount:
                value('housingStatus') === 'OWNED'
                    ? integerValue('houseRoomCount')
                    : null,
            houseLocation:
                value('housingStatus') === 'OWNED'
                    ? value('houseLocation')
                    : null,
            houseEstimatedValue:
                value('housingStatus') === 'OWNED'
                    ? numberValue('houseEstimatedValue')
                    : null,
            houseMortgaged:
                value('housingStatus') === 'OWNED'
                && byId('houseMortgaged')?.checked === true,
            monthlyRent:
                value('housingStatus') === 'RENTED'
                    ? numberValue('monthlyRent')
                    : null,

            landOwned: byId('landOwned')?.checked === true,
            landArea:
                byId('landOwned')?.checked
                    ? numberValue('landArea')
                    : null,
            landUnit:
                byId('landOwned')?.checked
                    ? value('landUnit')
                    : null,
            landPlotCount:
                byId('landOwned')?.checked
                    ? integerValue('landPlotCount')
                    : null,
            landLocation:
                byId('landOwned')?.checked
                    ? value('landLocation')
                    : null,
            landUsage:
                byId('landOwned')?.checked
                    ? value('landUsage')
                    : null,
            landEstimatedValue:
                byId('landOwned')?.checked
                    ? numberValue('landEstimatedValue')
                    : null,
            landGeneratesIncome:
                byId('landOwned')?.checked === true
                && byId('landGeneratesIncome')?.checked === true,
            landAnnualIncome:
                byId('landOwned')?.checked
                && byId('landGeneratesIncome')?.checked
                    ? numberValue('landAnnualIncome')
                    : null,

            vehiclesOwned:
                byId('vehiclesOwned')?.checked === true,
            vehicleCount:
                byId('vehiclesOwned')?.checked
                    ? integerValue('vehicleCount')
                    : null,
            vehicleDescription:
                byId('vehiclesOwned')?.checked
                    ? value('vehicleDescription')
                    : null,
            vehicleType:
                byId('vehiclesOwned')?.checked
                    ? value('vehicleType')
                    : null,
            vehicleUsage:
                byId('vehiclesOwned')?.checked
                    ? value('vehicleUsage')
                    : null,
            vehicleEstimatedValue:
                byId('vehiclesOwned')?.checked
                    ? numberValue('vehicleEstimatedValue')
                    : null,
            vehicleFinanced:
                byId('vehiclesOwned')?.checked === true
                && byId('vehicleFinanced')?.checked === true,

            businessOwned:
                byId('businessOwned')?.checked === true,
            businessName:
                byId('businessOwned')?.checked
                    ? value('businessName')
                    : null,
            businessType:
                byId('businessOwned')?.checked
                    ? value('businessType')
                    : null,
            businessLocation:
                byId('businessOwned')?.checked
                    ? value('businessLocation')
                    : null,
            businessEmployeeCount:
                byId('businessOwned')?.checked
                    ? integerValue('businessEmployeeCount')
                    : null,
            businessAnnualIncome:
                byId('businessOwned')?.checked
                    ? numberValue('businessAnnualIncome')
                    : null,

            livestockOwned:
                byId('livestockOwned')?.checked === true,
            livestockDescription:
                byId('livestockOwned')?.checked
                    ? value('livestockDescription')
                    : null,
            livestockEstimatedValue:
                byId('livestockOwned')?.checked
                    ? numberValue('livestockEstimatedValue')
                    : null,
            livestockAnnualIncome:
                byId('livestockOwned')?.checked
                    ? numberValue('livestockAnnualIncome')
                    : null,

            otherAssetsDescription:
                byId('otherAssetsOwned')?.checked
                    ? value('otherAssetsDescription')
                    : null,
            otherAssetsEstimatedValue:
                byId('otherAssetsOwned')?.checked
                    ? numberValue('otherAssetsEstimatedValue')
                    : null,
            otherAssetsAnnualIncome:
                byId('otherAssetsOwned')?.checked
                    ? numberValue('otherAssetsAnnualIncome')
                    : null,

            financialHardshipReason:
                value('financialHardshipReason'),
            familySituationRemarks:
                value('familySituationRemarks'),

            parentGuardianName:
                value('parentGuardianName'),
            parentGuardianRelation:
                value('parentGuardianRelation'),
            parentGuardianMobile:
                value('parentGuardianMobile'),

            declarationAccepted:
                byId('declarationAccepted')?.checked === true,

            siblings:
                collectSiblings()
        };
    }

    function renderSiblings(siblings) {
        const container =
            byId('siblingsContainer');

        if (!container) {
            return;
        }

        container.innerHTML = '';

        siblings.forEach(
            sibling => appendSiblingRow(
                sibling || {}
            )
        );
    }

    function appendSiblingRow(sibling) {
        const container =
            byId('siblingsContainer');

        if (!container) {
            return;
        }

        const wrapper =
            document.createElement('div');

        wrapper.className =
            'instruction-box scholarship-sibling-row';

        wrapper.innerHTML = `
            <div class="form-group">
                <label>Sibling Name</label>
                <input class="form-control"
                       data-field="siblingName"
                       type="text">
            </div>

            <div class="form-group">
                <label>Age</label>
                <input class="form-control"
                       data-field="age"
                       type="number"
                       min="0">
            </div>

            <div class="form-group">
                <label>Current Status</label>
                <select class="form-control"
                        data-field="currentStatus">
                    <option value="">Select</option>
                    <option value="STUDYING">Studying</option>
                    <option value="WORKING">Working</option>
                    <option value="STUDYING_AND_WORKING">
                        Studying and Working
                    </option>
                    <option value="OTHER">Other</option>
                </select>
            </div>

            <div class="form-group">
                <label>Class / Course</label>
                <input class="form-control"
                       data-field="classOrCourse"
                       type="text">
            </div>

            <div class="form-group">
                <label>Institution</label>
                <input class="form-control"
                       data-field="institution"
                       type="text">
            </div>

            <div class="form-group">
                <label>Occupation</label>
                <input class="form-control"
                       data-field="occupation"
                       type="text">
            </div>

            <div class="form-group">
                <label>Annual Income (UGX)</label>
                <input class="form-control"
                       data-field="annualIncome"
                       type="number"
                       min="0"
                       step="0.01">
            </div>

            <button type="button"
                    class="btn-secondary"
                    data-action="remove-sibling">
                Remove
            </button>
        `;

        setSiblingValue(
            wrapper,
            'siblingName',
            sibling.siblingName
        );
        setSiblingValue(
            wrapper,
            'age',
            sibling.age
        );
        setSiblingValue(
            wrapper,
            'currentStatus',
            sibling.currentStatus
        );
        setSiblingValue(
            wrapper,
            'classOrCourse',
            sibling.classOrCourse
        );
        setSiblingValue(
            wrapper,
            'institution',
            sibling.institution
        );
        setSiblingValue(
            wrapper,
            'occupation',
            sibling.occupation
        );
        setSiblingValue(
            wrapper,
            'annualIncome',
            sibling.annualIncome
        );

        wrapper
            .querySelector(
                '[data-action="remove-sibling"]'
            )
            ?.addEventListener(
                'click',
                () => {
                    wrapper.remove();
                    updateSiblingEmptyState();
                }
            );

        container.appendChild(
            wrapper
        );

        updateSiblingEmptyState();
    }

    function collectSiblings() {
        return Array.from(
            document.querySelectorAll(
                '.scholarship-sibling-row'
            )
        ).map(
            row => ({
                siblingName:
                    siblingValue(
                        row,
                        'siblingName'
                    ),
                age:
                    siblingInteger(
                        row,
                        'age'
                    ),
                currentStatus:
                    siblingValue(
                        row,
                        'currentStatus'
                    ),
                classOrCourse:
                    siblingValue(
                        row,
                        'classOrCourse'
                    ),
                institution:
                    siblingValue(
                        row,
                        'institution'
                    ),
                occupation:
                    siblingValue(
                        row,
                        'occupation'
                    ),
                annualIncome:
                    siblingNumber(
                        row,
                        'annualIncome'
                    )
            })
        );
    }

    function endpointBase() {
        return accessMode === 'school'
            ? SCHOOL_BASE
            : PUBLIC_BASE;
    }

    async function secureFetch(url, options) {
        const headers =
            new Headers(
                options?.headers || {}
            );

        headers.set(
            accessMode === 'school'
                ? SCHOOL_HEADER
                : PUBLIC_HEADER,
            secureCredential
        );

        return fetch(
            url,
            {
                ...options,
                headers,
                credentials: 'same-origin',
                cache: 'no-store'
            }
        );
    }

    function extractData(payload) {
        if (!payload) {
            return null;
        }

        if (payload.data !== undefined) {
            return payload.data;
        }

        return payload;
    }

    function extractMessage(payload, fallback) {
        if (!payload) {
            return fallback;
        }

        return payload.message
            || payload.error
            || fallback;
    }

    async function readPayload(response) {
        const contentType =
            response.headers.get(
                'content-type'
            ) || '';

        if (!contentType.includes('application/json')) {
            return null;
        }

        try {
            return await response.json();
        } catch (error) {
            return null;
        }
    }

    function updateAssetVisibility() {
        const housingStatus = value('housingStatus');

        toggle('ownedHouseDetails', housingStatus === 'OWNED');
        toggle('rentedHouseDetails', housingStatus === 'RENTED');

        const landOwned = byId('landOwned')?.checked === true;
        toggle('landDetails', landOwned);
        toggle(
            'landIncomeDetails',
            landOwned && byId('landGeneratesIncome')?.checked === true
        );

        toggle('vehicleDetails', byId('vehiclesOwned')?.checked === true);
        toggle('businessDetails', byId('businessOwned')?.checked === true);
        toggle('livestockDetails', byId('livestockOwned')?.checked === true);
        toggle('otherAssetsDetails', byId('otherAssetsOwned')?.checked === true);
    }

    function clearInactiveAssetValues() {
        const housingStatus = value('housingStatus');

        if (housingStatus !== 'OWNED') {
            clearFields(['houseType','houseRoomCount','houseLocation','houseEstimatedValue']);
            if (byId('houseMortgaged')) byId('houseMortgaged').checked = false;
        }
        if (housingStatus !== 'RENTED') clearFields(['monthlyRent']);

        if (!byId('landOwned')?.checked) {
            clearFields(['landPlotCount','landArea','landUnit','landLocation','landUsage','landEstimatedValue','landAnnualIncome']);
            if (byId('landGeneratesIncome')) byId('landGeneratesIncome').checked = false;
        } else if (!byId('landGeneratesIncome')?.checked) {
            clearFields(['landAnnualIncome']);
        }

        if (!byId('vehiclesOwned')?.checked) {
            clearFields(['vehicleCount','vehicleDescription','vehicleType','vehicleUsage','vehicleEstimatedValue']);
            if (byId('vehicleFinanced')) byId('vehicleFinanced').checked = false;
        }
        if (!byId('businessOwned')?.checked) clearFields(['businessName','businessType','businessLocation','businessEmployeeCount','businessAnnualIncome']);
        if (!byId('livestockOwned')?.checked) clearFields(['livestockDescription','livestockEstimatedValue','livestockAnnualIncome']);
        if (!byId('otherAssetsOwned')?.checked) clearFields(['otherAssetsDescription','otherAssetsEstimatedValue','otherAssetsAnnualIncome']);
    }

    function updateFamilyVisibility() {
        updateParentStatusVisibility('father');
        updateParentStatusVisibility('mother');
    }

    function updateParentStatusVisibility(prefix) {
        const deceased = value(`${prefix}Status`) === 'DECEASED';
        [`${prefix}Contact`,`${prefix}Email`,`${prefix}Occupation`,`${prefix}AnnualIncome`].forEach(id => {
            const field = byId(id);
            field?.closest('.form-group')?.classList.toggle('hidden', deceased);
            if (deceased && field) field.value = '';
        });
    }

    async function saveDraftAndMove(nextStep, button) {
        setBusy(button, true);
        try {
            const saved = await persist(false);
            if (saved) showStep(nextStep);
        } finally {
            setBusy(button, false);
        }
    }

    function showStep(step, scroll = true) {
        const target = Math.max(1, Math.min(5, Number(step) || 1));
        currentStep = target;

        document.querySelectorAll('[data-scholarship-step]').forEach(panel => {
            const panelStep = Number(panel.dataset.scholarshipStep);
            panel.classList.toggle('active-step', panelStep === target);
            panel.classList.toggle('hidden-element', panelStep !== target);
        });

        document.querySelectorAll('#scholarship-stepper .stepper-item').forEach(item => {
            const itemStep = Number(item.dataset.stepTarget);
            item.classList.toggle('active', itemStep === target);
            item.classList.toggle('completed', itemStep < target);
        });

        hide('formMessage');
        if (scroll) document.querySelector('.scholarship-form-heading')?.scrollIntoView({behavior:'smooth',block:'start'});
    }

    function updateSiblingEmptyState() {
        toggle('siblingsEmptyState', document.querySelectorAll('.scholarship-sibling-row').length === 0);
    }

    function clearFields(ids) {
        ids.forEach(id => {
            const field = byId(id);
            if (!field) return;
            if (field.type === 'checkbox' || field.type === 'radio') field.checked = false;
            else field.value = '';
        });
    }

    function renderSchoolIdentity(data) {
        const schoolName = cleanString(data?.schoolName) || 'Selected Montfort School';
        const schoolCode = cleanString(data?.schoolCode);
        const schoolLocation = cleanString(data?.schoolLocation);
        const meta = [schoolCode, schoolLocation].filter(Boolean).join(' • ');
        setText('scholarshipSchoolName', schoolName);
        setText('scholarshipSchoolMeta', meta || 'Scholarship Application');
        setText('applyingSchoolName', schoolName);
        setText('applyingSchoolLocation', meta);
    }

    async function loadSchoolLogo(logoAvailable) {
        const image = byId('scholarshipSchoolLogo');
        if (!image) return;
        if (!logoAvailable) { image.src='/assets/Images/logo_MBSG_UG_8.webp'; return; }
        const endpoint = accessMode === 'school' ? `${SCHOOL_BASE}/logo` : `${PUBLIC_BASE}/logo`;
        try {
            const response = await fetch(endpoint,{method:'GET',headers:{[accessMode === 'school' ? SCHOOL_HEADER : PUBLIC_HEADER]:secureCredential},credentials:accessMode === 'school' ? 'same-origin' : 'omit',cache:'no-store'});
            if (!response.ok) throw new Error('School logo could not be loaded.');
            const blob = await response.blob();
            if (schoolLogoObjectUrl) URL.revokeObjectURL(schoolLogoObjectUrl);
            schoolLogoObjectUrl=URL.createObjectURL(blob);
            image.src=schoolLogoObjectUrl;
        } catch (error) {
            console.debug('School logo unavailable. Using Montfort fallback logo.',error);
            image.src='/assets/Images/logo_MBSG_UG_8.webp';
        }
    }

    function showPageLoader(message) {
        setText('scholarshipLoaderMessage',message);
        document.body.classList.add('erp-action-in-progress');
        byId('scholarshipPageLoader')?.classList.add('is-visible');
    }

    function hidePageLoader() {
        document.body.classList.remove('erp-action-in-progress');
        byId('scholarshipPageLoader')?.classList.remove('is-visible');
    }

    function returnToApplicant() {
        if (window.opener && !window.opener.closed) {
            try { window.opener.focus(); window.close(); return; } catch (error) { console.debug('Could not return using opener.',error); }
        }
        let returnUrl='';
        try { returnUrl=sessionStorage.getItem('erpScholarshipReturnUrl') || ''; } catch (error) { returnUrl=''; }
        window.location.assign(returnUrl || '/admin/applications');
    }

    function showUnavailable(message) {
        hide('scholarshipLoadingShell');
        hide('scholarshipForm');
        hide('scholarshipComplete');
        hide('scholarshipComplete');

        setText(
            'scholarshipUnavailableMessage',
            message
        );

        show(
            'scholarshipUnavailable'
        );
    }

    function showFormMessage(message, isError) {
        const node =
            byId('formMessage');

        if (!node) {
            return;
        }

        node.textContent =
            message || '';

        node.classList.remove(
            'hidden'
        );

        node.setAttribute(
            'role',
            isError
                ? 'alert'
                : 'status'
        );
    }

    function setBusy(button, busy) {
        if (!button) {
            return;
        }

        button.disabled =
            busy === true;
    }

    function formatMoney(value) {
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

        return `UGX ${number.toLocaleString(
            'en-UG',
            {
                maximumFractionDigits: 2
            }
        )}`;
    }

    function numberValue(id) {
        const raw =
            value(id);

        if (!raw) {
            return null;
        }

        const parsed =
            Number(raw);

        return Number.isFinite(parsed)
            ? parsed
            : null;
    }

    function integerValue(id) {
        const raw =
            value(id);

        if (!raw) {
            return null;
        }

        const parsed =
            Number.parseInt(
                raw,
                10
            );

        return Number.isFinite(parsed)
            ? parsed
            : null;
    }

    function siblingValue(row, field) {
        return cleanString(
            row.querySelector(
                `[data-field="${field}"]`
            )?.value
        );
    }

    function siblingNumber(row, field) {
        const raw =
            siblingValue(
                row,
                field
            );

        if (!raw) {
            return null;
        }

        const parsed =
            Number(raw);

        return Number.isFinite(parsed)
            ? parsed
            : null;
    }

    function siblingInteger(row, field) {
        const raw =
            siblingValue(
                row,
                field
            );

        if (!raw) {
            return null;
        }

        const parsed =
            Number.parseInt(
                raw,
                10
            );

        return Number.isFinite(parsed)
            ? parsed
            : null;
    }

    function setSiblingValue(row, field, valueToSet) {
        const node =
            row.querySelector(
                `[data-field="${field}"]`
            );

        if (node) {
            node.value =
                valueToSet === null
                || valueToSet === undefined
                    ? ''
                    : String(valueToSet);
        }
    }

    function value(id) {
        return cleanString(
            byId(id)?.value
        );
    }

    function setValue(id, valueToSet) {
        const node =
            byId(id);

        if (!node) {
            return;
        }

        node.value =
            valueToSet === null
            || valueToSet === undefined
                ? ''
                : String(valueToSet);
    }

    function cleanString(valueToClean) {
        if (valueToClean === null
            || valueToClean === undefined) {
            return '';
        }

        return String(
            valueToClean
        ).trim();
    }

    function safeMessage(error, fallback) {
        return cleanString(
            error?.message
        ) || fallback;
    }

    function setText(id, text) {
        const node =
            byId(id);

        if (node) {
            node.textContent =
                text === null
                || text === undefined
                || text === ''
                    ? '—'
                    : String(text);
        }
    }

    function byId(id) {
        return document.getElementById(
            id
        );
    }

    function show(id) {
        byId(id)?.classList.remove(
            'hidden'
        );
    }

    function hide(id) {
        byId(id)?.classList.add(
            'hidden'
        );
    }

    function toggle(id, visible) {
        if (visible) {
            show(id);
        } else {
            hide(id);
        }
    }
})();
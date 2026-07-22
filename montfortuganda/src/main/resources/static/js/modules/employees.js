// noinspection SpellCheckingInspection
/* global apiGet, apiPost, apiPut, apiDelete, showLoader, hideLoader, showPremiumModal, showSuccessMessage, showErrorMessage, CrudTable, createErpCalendar */

/**
 * @typedef {Object} EmployeeDepartmentData
 * @property {(number|string|null)} [departmentId]
 * @property {(number|string|null)} [id]
 * @property {(string|null)} [departmentName]
 * @property {(string|null)} [name]
 */

/**
 * @typedef {Object} EmployeeDesignationData
 * @property {(number|string|null)} [designationId]
 * @property {(number|string|null)} [id]
 * @property {(string|null)} [designationName]
 * @property {(string|null)} [name]
 */

/**
 * @typedef {Object} EmployeeUserData
 * @property {(number|string|null)} [id]
 * @property {(string|null)} [username]
 * @property {(string|null)} [status]
 */

/**
 * @typedef {Object} EmployeeData
 * @property {(number|string|null)} [employeeId]
 * @property {(number|string|null)} [id]
 * @property {(string|null)} [employeeNo]
 * @property {(string|null)} [code]
 * @property {(string|null)} [title]
 * @property {(string|null)} [firstName]
 * @property {(string|null)} [middleName]
 * @property {(string|null)} [lastName]
 * @property {(string|null)} [fullName]
 * @property {(string|null)} [employeeName]
 * @property {(string|null)} [gender]
 * @property {(string|null)} [dateOfBirth]
 * @property {(string|null)} [maritalStatus]
 * @property {(string|null)} [bloodGroup]
 * @property {(string|null)} [religion]
 * @property {(string|null)} [subReligion]
 * @property {(number|string|null)} [departmentId]
 * @property {(number|string|null)} [designationId]
 * @property {(string|null)} [departmentName]
 * @property {(string|null)} [designationName]
 * @property {(EmployeeDepartmentData|null)} [department]
 * @property {(EmployeeDesignationData|null)} [designation]
 * @property {(string|null)} [employeeCategory]
 * @property {(string|null)} [category]
 * @property {(string|null)} [employeeType]
 * @property {(string|null)} [employmentMode]
 * @property {(string|null)} [employmentStatus]
 * @property {(string|null)} [status]
 * @property {(string|null)} [joiningDate]
 * @property {(string|null)} [probationEndDate]
 * @property {(string|null)} [confirmationDate]
 * @property {(string|null)} [retirementDate]
 * @property {(string|null)} [resignationDate]
 * @property {(string|null)} [terminationDate]
 * @property {(string|null)} [employmentEndDate]
 * @property {(string|null)} [exitReason]
 * @property {(string|null)} [reportingManagerName]
 * @property {(EmployeeData|null)} [reportingManager]
 * @property {(string|null)} [nationality]
 * @property {(string|null)} [nationalId]
 * @property {(string|null)} [tinNumber]
 * @property {(string|null)} [passportNo]
 * @property {(string|null)} [passportExpiryDate]
 * @property {(string|null)} [workPermitNumber]
 * @property {(string|null)} [workPermitExpiryDate]
 * @property {(string|null)} [officialEmail]
 * @property {(string|null)} [email]
 * @property {(string|null)} [personalEmail]
 * @property {(string|null)} [mobileNo]
 * @property {(string|null)} [phone]
 * @property {(string|null)} [alternateMobile]
 * @property {(string|null)} [addressCountry]
 * @property {(string|null)} [addressState]
 * @property {(string|null)} [addressDistrict]
 * @property {(string|null)} [addressCounty]
 * @property {(string|null)} [addressSubCounty]
 * @property {(string|null)} [addressParish]
 * @property {(string|null)} [addressVillage]
 * @property {(string|null)} [addressStreet]
 * @property {(string|null)} [postalCode]
 * @property {(string|null)} [skills]
 * @property {(string|null)} [languagesSpoken]
 * @property {(string|null)} [employeeRemarks]
 * @property {(number|string|null)} [reportingManagerId]
 * @property {(boolean|null)} [loginEnabled]
 * @property {(number|string|null)} [userId]
 * @property {(string|null)} [loginId]
 * @property {(string|null)} [username]
 * @property {(string|null)} [loginRole]
 * @property {Array<string>} [loginRoles]
 * @property {(string|null)} [roleName]
 * @property {(string|null)} [loginStatus]
 * @property {(boolean|null)} [mustChangePassword]
 * @property {(string|null)} [temporaryPasswordExpiresAt]
 * @property {(string|null)} [credentialDeliveryStatus]
 * @property {(string|null)} [credentialsSentAt]
 * @property {(number|null)} [credentialDeliveryAttempts]
 * @property {(EmployeeUserData|null)} [user]
 * @property {(boolean|null)} [profilePhotoAvailable]
 * @property {(string|null)} [profilePhotoUrl]
 * @property {(string|null)} [profilePhoto]
 * @property {(boolean|null)} [signatureFileAvailable]
 * @property {(string|null)} [signatureFileUrl]
 * @property {(string|null)} [signatureFile]
 * @property {Array<Object>} [contacts]
 * @property {Array<Object>} [qualifications]
 * @property {Array<Object>} [experiences]
 * @property {Array<Object>} [documents]
 * @property {(boolean|null)} [active]
 * @property {(number|string|null)} [version]
 * @property {(string|null)} [createdBy]
 * @property {(string|null)} [createdAt]
 * @property {(string|null)} [updatedBy]
 * @property {(string|null)} [updatedAt]
 */

/**
 * @typedef {Object} EmployeeRegistrationResult
 * @property {(string|null)} [operationId]
 * @property {(string|null)} [status]
 * @property {(string|null)} [stage]
 * @property {(string|null)} [stageTitle]
 * @property {(number|string|null)} [percentage]
 * @property {(string|null)} [message]
 * @property {(number|string|null)} [employeeId]
 * @property {(string|null)} [employeeNo]
 * @property {(string|null)} [fullName]
 * @property {(string|null)} [departmentName]
 * @property {(string|null)} [designationName]
 * @property {(string|null)} [reportingManagerName]
 * @property {(string|null)} [loginAccountStatus]
 * @property {(boolean|null)} [loginCreated]
 * @property {(number|string|null)} [completedItems]
 * @property {(number|string|null)} [totalItems]
 * @property {(string|null)} [itemMessage]
 * @property {Array<*>} [errors]
 * @property {*} [error]
 */

/**
 * @typedef {Object} FlatpickrAdapter
 * @property {(value: (string|Date|null), triggerChange?: boolean) => void} setDate
 */

/**
 * @typedef {HTMLInputElement & {_flatpickr?: FlatpickrAdapter}} CalendarInput
 */

/**
 * @param {Element|null|undefined} element
 * @returns {(FlatpickrAdapter|null)}
 */
function getCalendarAdapter(element) {
    const input = /** @type {(CalendarInput|null)} */ (element);
    return input?._flatpickr || null;
}


/**
 * Adds one or more CSS classes without relying on DOMTokenList.
 * This keeps JetBrains inspections from incorrectly flagging
 * classList.add(), classList.remove() and classList.toggle().
 *
 * @param {Element|null|undefined} element
 * @param {...string} classNames
 */
function addCssClasses(element, ...classNames) {
    if (!element) return;

    const currentClasses = String(
        element.getAttribute('class') || ''
    )
        .split(/\s+/)
        .filter(Boolean);

    classNames
        .flatMap(name => String(name || '').split(/\s+/))
        .filter(Boolean)
        .forEach(name => {
            if (currentClasses.indexOf(name) === -1) {
                currentClasses.push(name);
            }
        });

    element.setAttribute(
        'class',
        currentClasses.join(' ')
    );
}

/**
 * Removes one or more CSS classes without relying on DOMTokenList.
 *
 * @param {Element|null|undefined} element
 * @param {...string} classNames
 */
function removeCssClasses(element, ...classNames) {
    if (!element) return;

    const namesToRemove = classNames
        .flatMap(name => String(name || '').split(/\s+/))
        .filter(Boolean);

    const remainingClasses = String(
        element.getAttribute('class') || ''
    )
        .split(/\s+/)
        .filter(Boolean)
        .filter(
            name =>
                namesToRemove.indexOf(name) === -1
        );

    element.setAttribute(
        'class',
        remainingClasses.join(' ')
    );
}

/**
 * Adds or removes one CSS class based on the requested state.
 *
 * @param {Element|null|undefined} element
 * @param {string} className
 * @param {boolean=} force
 * @returns {boolean}
 */
function toggleCssClass(
    element,
    className,
    force
) {
    if (!element) return false;

    const normalizedClassName =
        String(className || '').trim();

    if (!normalizedClassName) {
        return false;
    }

    const currentClasses = String(
        element.getAttribute('class') || ''
    )
        .split(/\s+/)
        .filter(Boolean);

    const currentlyPresent =
        currentClasses.indexOf(
            normalizedClassName
        ) !== -1;

    const shouldBePresent =
        typeof force === 'boolean'
            ? force
            : !currentlyPresent;

    if (shouldBePresent) {
        addCssClasses(
            element,
            normalizedClassName
        );
    } else {
        removeCssClasses(
            element,
            normalizedClassName
        );
    }

    return shouldBePresent;
}

/**
 * Safely removes an element without using Element.remove().
 *
 * @param {Node|null|undefined} element
 */
function removeDomElement(element) {
    if (
        element &&
        element.parentNode
    ) {
        element.parentNode.removeChild(element);
    }
}


document.addEventListener('viewLoaded', function(event) {
    if (event.detail.role !== 'admin') return;

    if (event.detail.view === 'employees') {
        event.detail.waitUntil(
            initEmployeesView(event.detail)
        );
    } else if (event.detail.view === 'add-employee') {
        event.detail.waitUntil(
            Promise.resolve().then(() => {
                initAddEmployeeView();
            })
        );
    }
});
function getRequiredBranchId() {
    const branchId = Number.parseInt(
        localStorage.getItem('user_branch'),
        10
    );

    if (!Number.isInteger(branchId) || branchId <= 0) {
        throw new Error(
            'No valid branch is assigned to the current user.'
        );
    }

    return branchId;
}
function resolveEmployeeCategory(
    viewContainer,
    prefix = 'add'
) {
    const category =
        viewContainer.querySelector(
            `#${prefix}-empCategory`
        )?.value?.trim() || null;

    if (!category) {
        return null;
    }

    if (category !== 'MANAGEMENT') {
        return category;
    }

    const managementType =
        viewContainer.querySelector(
            `#${prefix}-empManagementType`
        )?.value?.trim() || null;

    if (managementType === 'TEACHING') {
        return 'MANAGEMENT_TEACHING';
    }

    if (managementType === 'NON_TEACHING') {
        return 'MANAGEMENT_NON_TEACHING';
    }

    return null;
}
let currentDetailEmpId = null;

/** @type {WeakMap<HTMLElement, Object>} */
const employeeCollectionRecordByRow = new WeakMap();

/**
 * @param {HTMLElement|null|undefined} row
 * @returns {(number|null)}
 */
function getRowId(row) {
    const raw = row?.dataset?.recordId;
    if (!raw) return null;

    const value = Number.parseInt(raw, 10);
    return Number.isInteger(value) && value > 0 ? value : null;
}

function collectContacts(viewContainer) {
    const rows =
        /** @type {HTMLElement[]} */ (
            Array.from(
                viewContainer.querySelectorAll(
                    '#contacts-container .contact-row'
                )
            )
        );

    return rows
        .map((row, index) => {
            const existing =
                employeeCollectionRecordByRow.get(row) || {};

            const employeeContactId = getRowId(row);
            const employeeContactName =
                row.querySelector('.c-name')
                    ?.value
                    ?.trim() || null;
            const employeeContactRelationship =
                row.querySelector('.c-relation')
                    ?.value
                    ?.trim() || null;
            const employeeContactMobile =
                row.querySelector('.c-phone')
                    ?.value
                    ?.trim() || null;
            const employeeContactEmail =
                row.querySelector('.c-email')
                    ?.value
                    ?.trim() || null;

            const hasAnyValue = Boolean(
                employeeContactId ||
                employeeContactName ||
                employeeContactRelationship ||
                employeeContactMobile ||
                employeeContactEmail
            );

            if (!hasAnyValue) {
                return null;
            }

            if (
                !employeeContactName ||
                !employeeContactRelationship ||
                !employeeContactMobile
            ) {
                throw new Error(
                    'Each contact requires name, relationship and mobile number.'
                );
            }

            return {
                employeeContactId,
                employeeContactName,
                employeeContactRelationship,
                employeeContactType:
                    existing.employeeContactType || 'EMERGENCY',
                employeeContactMobile,
                employeeContactAlternateMobile:
                    existing.employeeContactAlternateMobile || null,
                employeeContactEmail,
                employeeContactCountry:
                    existing.employeeContactCountry || null,
                employeeContactState:
                    existing.employeeContactState || null,
                employeeContactDistrict:
                    existing.employeeContactDistrict || null,
                employeeContactVillage:
                    existing.employeeContactVillage || null,
                employeeContactStreet:
                    existing.employeeContactStreet || null,
                employeeContactPostalCode:
                    existing.employeeContactPostalCode || null,
                employeeContactOccupation:
                    existing.employeeContactOccupation || null,
                employeeContactWorkplace:
                    existing.employeeContactWorkplace || null,
                employeeContactIsPrimary:
                    existing.employeeContactIsPrimary ?? index === 0,
                employeeContactIsEmergency:
                    existing.employeeContactIsEmergency ?? true,
                employeeContactActive:
                    existing.employeeContactActive ?? true,
                employeeContactRemarks:
                    existing.employeeContactRemarks || null
            };
        })
        .filter(Boolean);
}

async function collectQualifications(viewContainer) {
    const rows =
        /** @type {HTMLElement[]} */ (
            Array.from(
                viewContainer.querySelectorAll(
                    '#qualifications-container .qual-row'
                )
            )
        );

    const qualifications = [];

    for (const row of rows) {
        const existing =
            employeeCollectionRecordByRow.get(row) || {};

        const employeeQualificationId = getRowId(row);
        const employeeQualificationLevel =
            row.querySelector('.q-level')
                ?.value
                ?.trim() || null;
        const employeeQualificationName =
            row.querySelector('.q-name')
                ?.value
                ?.trim() || null;
        const customLevel =
            row.querySelector('.q-custom-level')
                ?.value
                ?.trim() || null;
        const employeeQualificationInstitutionName =
            row.querySelector('.q-institution')
                ?.value
                ?.trim() || null;
        const employeeQualificationSpecialization =
            row.querySelector('.q-specialization')
                ?.value
                ?.trim() || null;
        const employeeQualificationGrade =
            row.querySelector('.q-grade')
                ?.value
                ?.trim() || null;
        const startYearValue =
            row.querySelector('.q-start-year')
                ?.value
                ?.trim() || null;
        const employeeQualificationStartYear =
            startYearValue
                ? Number.parseInt(startYearValue, 10)
                : null;
        const completionYearValue =
            row.querySelector('.q-completion-year')
                ?.value
                ?.trim() || null;
        const employeeQualificationCompletionYear =
            completionYearValue
                ? Number.parseInt(completionYearValue, 10)
                : null;

        const hasAnyValue = Boolean(
            employeeQualificationId ||
            employeeQualificationLevel ||
            employeeQualificationName ||
            customLevel ||
            employeeQualificationInstitutionName ||
            employeeQualificationSpecialization ||
            employeeQualificationGrade ||
            employeeQualificationStartYear ||
            employeeQualificationCompletionYear
        );

        if (!hasAnyValue) {
            continue;
        }

        if (
            !employeeQualificationLevel ||
            !employeeQualificationName ||
            !employeeQualificationInstitutionName ||
            !employeeQualificationStartYear ||
            !employeeQualificationCompletionYear
        ) {
            throw new Error(
                'Each qualification requires level, qualification name, institution, start year and completion year.'
            );
        }

        if (
            employeeQualificationCompletionYear <
            employeeQualificationStartYear
        ) {
            throw new Error(
                'Qualification completion year cannot be earlier than the start year.'
            );
        }

        if (
            employeeQualificationLevel === 'SENIOR_SECONDARY' &&
            !employeeQualificationSpecialization
        ) {
            throw new Error(
                'Specialization or subject combination is required for Senior Secondary.'
            );
        }

        if (
            employeeQualificationLevel === 'OTHER' &&
            !customLevel
        ) {
            throw new Error(
                'Enter the custom qualification level for Other.'
            );
        }

        qualifications.push({
            employeeQualificationId,
            employeeQualificationLevel,
            customLevel:
                employeeQualificationLevel === 'OTHER'
                    ? customLevel
                    : null,
            employeeQualificationName,
            employeeQualificationSpecialization,
            employeeQualificationInstitutionName,
            qualificationGrade:
                existing.qualificationGrade ||
                employeeQualificationGrade,
            employeeQualificationBoardUniversity:
                existing.employeeQualificationBoardUniversity || null,
            employeeQualificationCountry:
                existing.employeeQualificationCountry || null,
            employeeQualificationStartYear,
            employeeQualificationCompletionYear,
            employeeQualificationDurationMonths:
                existing.employeeQualificationDurationMonths ?? null,
            employeeQualificationGrade,
            employeeQualificationPercentage:
                existing.employeeQualificationPercentage ?? null,
            employeeQualificationCgpa:
                existing.employeeQualificationCgpa ?? null,
            employeeQualificationCertificateNumber:
                existing.employeeQualificationCertificateNumber || null,
            employeeQualificationRegistrationNumber:
                existing.employeeQualificationRegistrationNumber || null,
            employeeQualificationRemarks:
                existing.employeeQualificationRemarks || null,
            employeeQualificationActive:
                existing.employeeQualificationActive ?? true,
            fileData: null,
            fileName: null,
            contentType: null,
            fileSize: null
        });
    }

    return qualifications;
}

async function collectExperiences(viewContainer) {
    const rows =
        /** @type {HTMLElement[]} */ (
            Array.from(
                viewContainer.querySelectorAll(
                    '#experiences-container .exp-row'
                )
            )
        );

    const experiences = [];

    for (const row of rows) {
        const existing =
            employeeCollectionRecordByRow.get(row) || {};

        const employeeExperienceId = getRowId(row);
        const companyName =
            row.querySelector('.e-company')
                ?.value
                ?.trim() || null;
        const employeeExperienceEmploymentType =
            row.querySelector('.e-type')
                ?.value
                ?.trim() || null;
        const jobRole =
            row.querySelector('.e-role')
                ?.value
                ?.trim() || null;
        const startDate =
            row.querySelector('.e-start')
                ?.value || null;
        const endDate =
            row.querySelector('.e-end')
                ?.value || null;

        const hasAnyValue = Boolean(
            employeeExperienceId ||
            companyName ||
            employeeExperienceEmploymentType ||
            jobRole ||
            startDate ||
            endDate
        );

        if (!hasAnyValue) {
            continue;
        }

        if (
            !companyName ||
            !employeeExperienceEmploymentType ||
            !startDate
        ) {
            throw new Error(
                'Each experience requires organisation, employment type and start date.'
            );
        }

        if (
            startDate &&
            endDate &&
            endDate < startDate
        ) {
            throw new Error(
                'Experience end date cannot be before start date.'
            );
        }

        experiences.push({
            employeeExperienceId,
            employeeExperienceType:
                employeeExperienceEmploymentType,
            employeeExperienceCompanyName: companyName,
            employeeExperienceCompanyAddress:
                existing.employeeExperienceCompanyAddress || null,
            employeeExperienceCompanyCountry:
                existing.employeeExperienceCompanyCountry || null,
            employeeExperienceCompanyState:
                existing.employeeExperienceCompanyState || null,
            employeeExperienceCompanyDistrict:
                existing.employeeExperienceCompanyDistrict || null,
            employeeExperienceDesignation: jobRole,
            employeeExperienceDepartment:
                existing.employeeExperienceDepartment || null,
            employeeExperienceEmploymentType,
            employeeExperienceStartDate: startDate,
            employeeExperienceEndDate: endDate,
            employeeExperienceCurrentJob: !endDate,
            employeeExperienceTotalMonths:
                existing.employeeExperienceTotalMonths ?? null,
            employeeExperienceSalary:
                existing.employeeExperienceSalary ?? null,
            employeeExperienceCurrency:
                existing.employeeExperienceCurrency || null,
            employeeExperienceSupervisorName:
                existing.employeeExperienceSupervisorName || null,
            employeeExperienceSupervisorContact:
                existing.employeeExperienceSupervisorContact || null,
            employeeExperienceReasonForLeaving:
                existing.employeeExperienceReasonForLeaving || null,
            employeeExperienceResponsibilities:
                existing.employeeExperienceResponsibilities || null,
            employeeExperienceAchievements:
                existing.employeeExperienceAchievements || null,
            employeeExperienceActive:
                existing.employeeExperienceActive ?? true,
            employeeExperienceRemarks:
                existing.employeeExperienceRemarks || null,
            fileData: null,
            fileName: null,
            contentType: null,
            fileSize: null
        });
    }

    return experiences;
}

const EmpCollections = {
    contactFields: [
        {
            placeholder: 'Name',
            className: 'c-name',
            dataKey: 'employeeContactName'
        },
        {
            placeholder: 'Relationship',
            className: 'c-relation',
            dataKey: 'employeeContactRelationship',
            type: 'select',
            options: [
                { value: '', text: '-- Select Relationship --' },
                { value: 'FATHER', text: 'Father' },
                { value: 'MOTHER', text: 'Mother' },
                { value: 'BROTHER', text: 'Brother' },
                { value: 'SISTER', text: 'Sister' },
                { value: 'SPOUSE', text: 'Spouse' },
                { value: 'SON', text: 'Son' },
                { value: 'DAUGHTER', text: 'Daughter' },
                { value: 'GUARDIAN', text: 'Guardian' },
                { value: 'RELATIVE', text: 'Relative' },
                { value: 'FRIEND', text: 'Friend' },
                { value: 'MANAGER', text: 'Manager' },
                { value: 'REFERENCE', text: 'Reference' },
                { value: 'OTHER', text: 'Other' }
            ]
        },
        {
            placeholder: 'Phone',
            className: 'c-phone',
            dataKey: 'employeeContactMobile'
        },
        {
            placeholder: 'Email',
            className: 'c-email',
            type: 'email',
            dataKey: 'employeeContactEmail'
        }
    ],
    qualFields: [
        {
            placeholder: 'Qualification Level',
            className: 'q-level',
            dataKey: 'employeeQualificationLevel',
            type: 'select',
            options: [
                { value: '', text: '-- Select Level --' },
                { value: 'PRIMARY', text: 'Primary' },
                { value: 'SECONDARY', text: 'Secondary' },
                { value: 'SENIOR_SECONDARY', text: 'Senior Secondary' },
                { value: 'DIPLOMA', text: 'Diploma' },
                { value: 'CERTIFICATE', text: 'Certificate' },
                { value: 'GRADUATION', text: 'Graduation' },
                { value: 'POST_GRADUATION', text: 'Post Graduation' },
                { value: 'DR_PHD', text: 'Doctorate / PhD' },
                { value: 'OTHER', text: 'Other' }
            ]
        },
        {
            placeholder: 'Qualification Name',
            className: 'q-name',
            dataKey: 'employeeQualificationName'
        },
        {
            placeholder: 'Enter Other Level',
            className: 'q-custom-level',
            dataKey: 'customLevel',
            conditional: 'other-level'
        },
        {
            placeholder: 'Institution / School / College',
            className: 'q-institution',
            dataKey: 'employeeQualificationInstitutionName'
        },
        {
            placeholder: 'Specialization / Subject',
            className: 'q-specialization',
            dataKey: 'employeeQualificationSpecialization',
            conditional: 'specialization'
        },
        {
            placeholder: 'Division / Grade',
            className: 'q-grade',
            dataKey: 'employeeQualificationGrade'
        },
        {
            placeholder: 'Start Year',
            className: 'q-start-year',
            type: 'number',
            dataKey: 'employeeQualificationStartYear'
        },
        {
            placeholder: 'Completion Year',
            className: 'q-completion-year',
            type: 'number',
            dataKey: 'employeeQualificationCompletionYear'
        }
    ],
    expFields: [
        { placeholder: 'Organisation', className: 'e-company', dataKey: 'companyName' },
        {
            placeholder: 'Type',
            className: 'e-type',
            dataKey: 'employeeExperienceEmploymentType',
            type: 'select',
            options: [
                {
                    value: '',
                    text: '-- Select Employment Type --'
                },
                {
                    value: 'FULL_TIME',
                    text: 'Full Time'
                },
                {
                    value: 'PART_TIME',
                    text: 'Part Time'
                },
                {
                    value: 'CONTRACT',
                    text: 'Contract'
                },
                {
                    value: 'TEMPORARY',
                    text: 'Temporary'
                },
                {
                    value: 'INTERNSHIP',
                    text: 'Internship'
                },
                {
                    value: 'CONSULTANT',
                    text: 'Consultant'
                },
                {
                    value: 'VOLUNTEER',
                    text: 'Volunteer'
                },
                {
                    value: 'SELF_EMPLOYED',
                    text: 'Self Employed'
                },
                {
                    value: 'OTHER',
                    text: 'Other'
                }
            ]
        },
        { placeholder: 'Post Held', className: 'e-role', dataKey: 'jobRole' },
        { placeholder: 'Start Date', className: 'e-start', type: 'date', dataKey: 'startDate' },
        { placeholder: 'End Date', className: 'e-end', type: 'date', dataKey: 'endDate' }
    ],
    createRow: function(
        containerElement,
        fieldsDef,
        rowClass,
        data = null,
        isEditMode = false
    ) {
        if (!containerElement || !Array.isArray(fieldsDef)) {
            return null;
        }

        const row = document.createElement('div');

        row.className =
            `emp-child-row emp-grid-${fieldsDef.length}-cols mb-3 ${rowClass}`;

        if (data) {
            employeeCollectionRecordByRow.set(
                row,
                data
            );

            const recordId =
                data.employeeContactId ??
                data.employeeQualificationId ??
                data.employeeExperienceId ??
                data.employeeDocumentId ??
                null;

            if (recordId !== null && recordId !== undefined) {
                row.dataset.recordId = String(recordId);
            }
        }

        const conditionalFields = {};

        const getFieldValue = field => {
            if (!data) return null;

            if (
                data[field.dataKey] !== undefined &&
                data[field.dataKey] !== null
            ) {
                return data[field.dataKey];
            }

            const aliases = {
                companyName:
                    'employeeExperienceCompanyName',
                jobRole:
                    'employeeExperienceDesignation',
                startDate:
                    'employeeExperienceStartDate',
                endDate:
                    'employeeExperienceEndDate',
                documentType:
                    'employeeDocumentType',
                documentName:
                    'employeeDocumentName',
                remarks:
                    'employeeDocumentRemarks'
            };

            const alias = aliases[field.dataKey];

            return alias &&
            data[alias] !== undefined &&
            data[alias] !== null
                ? data[alias]
                : null;
        };

        fieldsDef.forEach(field => {
            const wrapper = document.createElement('div');
            wrapper.className = 'emp-child-field';

            const span = document.createElement('span');

            span.className =
                `detail-text d-block ${isEditMode ? 'hidden' : ''}`;

            let input;

            if (field.type === 'select') {
                input = document.createElement('select');

                field.options.forEach(optionDefinition => {
                    const option =
                        document.createElement('option');

                    option.value =
                        String(
                            optionDefinition.value ?? ''
                        );

                    option.textContent =
                        optionDefinition.text;

                    input.appendChild(option);
                });
            } else {
                input = document.createElement('input');

                input.type =
                    field.type || 'text';

                input.placeholder =
                    String(
                        field.placeholder || ''
                    );
            }

            input.className =
                `detail-input w-100 ${field.className} ${isEditMode ? '' : 'hidden'}`;

            const value = getFieldValue(field);

            if (value !== null && value !== undefined) {

                if (field.type === 'date' && value) {
                    input.value =
                        String(value).split('T')[0];

                    span.textContent =
                        String(value).split('T')[0];
                } else {
                    input.value = String(value);
                    span.textContent = String(value);
                }
            } else {
                span.textContent = '-';
            }

            if (field.conditional) {
                wrapper.dataset.conditional =
                    field.conditional;

                conditionalFields[field.conditional] =
                    wrapper;
            }

            wrapper.appendChild(span);
            wrapper.appendChild(input);
            row.appendChild(wrapper);
        });

        const qualificationLevel =
            row.querySelector('.q-level');

        const updateQualificationVisibility = () => {
            if (!qualificationLevel) return;

            const selectedLevel =
                qualificationLevel.value;

            const otherWrapper =
                conditionalFields['other-level'];

            const specializationWrapper =
                conditionalFields['specialization'];

            const showOther =
                selectedLevel === 'OTHER';

            const showSpecialization =
                selectedLevel !== '' &&
                selectedLevel !== 'PRIMARY' &&
                selectedLevel !== 'SECONDARY';

            if (otherWrapper) {
                toggleCssClass(otherWrapper,
                    'hidden',
                    !showOther
                );

                const otherInput =
                    otherWrapper.querySelector(
                        '.q-custom-level'
                    );

                if (!showOther && otherInput) {
                    otherInput.value = '';
                }
            }

            if (specializationWrapper) {
                toggleCssClass(specializationWrapper,
                    'hidden',
                    !showSpecialization
                );

                const specializationInput =
                    specializationWrapper.querySelector(
                        '.q-specialization'
                    );

                if (
                    !showSpecialization &&
                    specializationInput
                ) {
                    specializationInput.value = '';
                }
            }
        };

        if (qualificationLevel) {
            qualificationLevel.addEventListener(
                'change',
                updateQualificationVisibility
            );

            updateQualificationVisibility();
        }

        const removeButton =
            document.createElement('button');

        removeButton.type = 'button';

        removeButton.className =
            `btn-secondary text-danger btn-sm detail-input ${isEditMode ? '' : 'hidden'}`;

        removeButton.textContent = 'X';

        removeButton.addEventListener(
            'click',
            () => removeDomElement(row)
        );

        row.appendChild(removeButton);

        containerElement.appendChild(row);

        return row;
    },
    initSection: function(viewContainer, sectionId, containerId, btnText, fieldsDef, rowClass, isEditMode = false) {
        const section = viewContainer.querySelector(sectionId);
        const container = viewContainer.querySelector(containerId);
        if (!section || !container || section.querySelector('.add-row-btn')) return;

        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = `btn btn-outline-primary btn-sm mt-2 mb-3 add-row-btn ${isEditMode ? '' : 'hidden'}`;
        btn.innerHTML = btnText;
        btn.addEventListener('click', () => this.createRow(container, fieldsDef, rowClass, null, true));
        section.appendChild(btn);
    },
    fileToBase64: function(file) {
        return new Promise((resolve, reject) => {
            if(!file) return resolve(null);
            const reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = () => resolve(reader.result);
            reader.onerror = error => reject(error);
        });
    }
};

function initEmployeesView(routeInfo = {}) {
    const viewContainer =
        /** @type {(HTMLElement|null)} */ (
        document.querySelector('#ba-employees-view')
    );

    if (!viewContainer) return Promise.resolve();

    const tableView =
        viewContainer.querySelector('#emp-tableView');

    const detailView =
        viewContainer.querySelector('#emp-detailView');

    const addEmpBtn =
        viewContainer.querySelector('#btn-add-employee');

    const editBtn =
        viewContainer.querySelector('#emp-editBtn');

    const saveBtn =
        viewContainer.querySelector('#emp-saveBtn');

    const cancelEditBtn =
        viewContainer.querySelector('#emp-cancelEditBtn');

    const backBtn =
        viewContainer.querySelector('#emp-backToTableBtn');

    const editCategory =
        viewContainer.querySelector('#edit-empCategory');

    const editManagementType =
        viewContainer.querySelector('#edit-empManagementType');

    const editManagementTypeGroup =
        viewContainer.querySelector(
            '#edit-empManagementTypeGroup'
        );

    const state = {
        page: 0,
        size: 10,
        sort: 'employeeId,desc'
    };

    let table = null;
    /** @type {(EmployeeData|null)} */
    let currentEmployee = null;

    /**
     * @param {Object<string, *>} object
     * @param {...string} keys
     * @returns {*}
     */
    const valueOf = (object, ...keys) => {
        for (const key of keys) {
            const value = object?.[key];

            if (value !== undefined && value !== null) {
                return value;
            }
        }

        return null;
    };

    const formatEnum = value => {
        if (!value) return '-';

        return String(value)
            .replace(/_/g, ' ')
            .toLowerCase()
            .replace(/\b\w/g, letter => letter.toUpperCase());
    };

    const formatBoolean = value =>
        value === true ? 'Yes' : value === false ? 'No' : '-';

    const formatDate = value => {
        if (!value) return '-';
        return String(value).split('T')[0];
    };

    const normalizeImageSource = value => {
        if (!value) return null;

        const source = String(value).trim();

        if (!source) return null;

        if (
            source.startsWith('data:') ||
            source.startsWith('blob:') ||
            source.startsWith('https://') ||
            source.startsWith('/')
        ) {
            return source;
        }

        return `/${source.replace(/^\/+/, '')}`;
    };

    const getPageData = response => {
        const data = response?.data ?? response ?? {};

        if (Array.isArray(data)) {
            return {
                content: data,
                pageNumber: 0,
                totalPages: data.length > 0 ? 1 : 0,
                totalElements: data.length
            };
        }

        return {
            content: Array.isArray(data.content)
                ? data.content
                : [],
            pageNumber:
                data.pageNumber ?? data.number ?? 0,
            totalPages:
                data.totalPages ?? 0,
            totalElements:
                data.totalElements ?? 0
        };
    };

    const getCollection = (employee, key) => {
        const value = employee?.[key];
        return Array.isArray(value) ? value : [];
    };

    const setText = (selector, value, formatter = null) => {
        const element =
            viewContainer.querySelector(selector);

        if (!element) return;

        const formatted = formatter
            ? formatter(value)
            : value;

        element.textContent =
            formatted !== undefined &&
            formatted !== null &&
            String(formatted).trim() !== ''
                ? String(formatted)
                : '-';
    };

    const setInput = (selector, value) => {
        const element =
            viewContainer.querySelector(selector);

        if (!element) return;

        const normalized =
            element.type === 'date' && value
                ? String(value).split('T')[0]
                : value ?? '';

        const calendar =
            getCalendarAdapter(element);

        if (calendar) {
            calendar.setDate(
                normalized === ''
                    ? null
                    : String(normalized),
                false
            );
        } else if (
            element instanceof HTMLInputElement ||
            element instanceof HTMLSelectElement ||
            element instanceof HTMLTextAreaElement
        ) {
            element.value = String(normalized);
        }
    };

    const bindField = (
        suffix,
        value,
        formatter = null
    ) => {
        setText(
            `#view-${suffix}`,
            value,
            formatter
        );

        setInput(
            `#edit-${suffix}`,
            value
        );
    };

    const synchronizeEditManagementCategory = () => {
        const isManagement =
            editCategory?.value === 'MANAGEMENT';

        toggleCssClass(editManagementTypeGroup,
            'hidden',
            !isManagement
        );

        if (editManagementType) {
            editManagementType.required = isManagement;
            editManagementType.setAttribute(
                'aria-required',
                String(isManagement)
            );

            if (!isManagement) {
                editManagementType.value = '';
            }
        }
    };

    editCategory?.addEventListener(
        'change',
        synchronizeEditManagementCategory
    );

    if (typeof createErpCalendar === 'function') {
        const today = new Date();
        const maxDobDate = new Date(
            today.getFullYear() - 18,
            today.getMonth(),
            today.getDate()
        );

        createErpCalendar('#edit-empDob', {
            maxDate: maxDobDate
        });
        createErpCalendar('#edit-empJoiningDate');
        createErpCalendar('#edit-empProbationEndDate');
        createErpCalendar('#edit-empConfirmationDate');
        createErpCalendar('#edit-empResignationDate');
        createErpCalendar('#edit-empTerminationDate');
        createErpCalendar('#edit-empPassportExpiry');
        createErpCalendar('#edit-empWorkPermitExpiry');
    }

    const navigateToEmployee = (
        employeeId,
        mode = null
    ) => {
        const normalizedEmployeeId = Number.parseInt(
            String(employeeId || ''),
            10
        );

        if (
            !Number.isInteger(normalizedEmployeeId) ||
            normalizedEmployeeId <= 0
        ) {
            showErrorMessage('A valid Employee ID is required.');
            return Promise.resolve(false);
        }

        return window.erpNavigate({
            role: 'admin',
            view: 'employees',
            routeParams: [
                String(normalizedEmployeeId),
                ...(mode === 'edit' ? ['edit'] : [])
            ],
            title: mode === 'edit'
                ? 'Edit Employee'
                : 'Employee Details'
        });
    };

    addEmpBtn?.addEventListener('click', event => {
        void window.erpWithButtonFeedback(
            event.currentTarget,
            'Opening Form...',
            () => window.erpNavigate({
            role: 'admin',
            view: 'add-employee',
            title: 'Add Employee'
            })
        );
    });

    EmpCollections.initSection(
        viewContainer,
        '#contacts-section',
        '#contacts-container',
        '+ Add Contact',
        EmpCollections.contactFields,
        'contact-row'
    );

    EmpCollections.initSection(
        viewContainer,
        '#qualifications-section',
        '#qualifications-container',
        '+ Add Qualification',
        EmpCollections.qualFields,
        'qual-row'
    );

    EmpCollections.initSection(
        viewContainer,
        '#experiences-section',
        '#experiences-container',
        '+ Add Experience',
        EmpCollections.expFields,
        'exp-row'
    );

    async function loadEmployees() {
        table?.showLoading();

        try {
            const keyword =
                viewContainer.querySelector(
                    '#emp-searchInput'
                )?.value?.trim() || null;

            const employeeCategory =
                viewContainer.querySelector(
                    '#emp-categoryFilter'
                )?.value || null;

            const employmentStatus =
                viewContainer.querySelector(
                    '#emp-statusFilter'
                )?.value || null;

            const payload = {
                keyword,
                employeeCategory,
                employmentStatus
            };

            const response = await apiPost(
                `/branchadmin/employees/search?page=${state.page}&size=${state.size}&sort=${encodeURIComponent(state.sort)}`,
                payload
            );

            const pageData =
                getPageData(response);

            table.render(
                pageData.content,
                (employee, rowNode) => {
                    const employeeId =
                        valueOf(
                            employee,
                            'employeeId',
                            'id'
                        );

                    const employeeNo =
                        valueOf(
                            employee,
                            'employeeNo',
                            'code'
                        ) || '-';

                    const fullName =
                        valueOf(
                            employee,
                            'fullName',
                            'employeeName'
                        ) || [
                            employee.title,
                            employee.firstName,
                            employee.middleName,
                            employee.lastName
                        ].filter(Boolean).join(' ');

                    const employeeCategoryValue =
                        valueOf(
                            employee,
                            'employeeCategory',
                            'category'
                        );

                    const officialEmail =
                        valueOf(
                            employee,
                            'officialEmail',
                            'email'
                        );

                    const mobileNo =
                        valueOf(
                            employee,
                            'mobileNo',
                            'phone'
                        );

                    const employmentStatusValue =
                        valueOf(
                            employee,
                            'employmentStatus',
                            'status'
                        ) || 'ACTIVE';

                    const departmentName =
                        valueOf(
                            employee,
                            'departmentName'
                        ) || employee.department?.departmentName || '-';

                    const designationName =
                        valueOf(
                            employee,
                            'designationName'
                        ) || employee.designation?.designationName || '-';

                    const profilePhoto =
                        normalizeImageSource(
                            valueOf(
                                employee,
                                'profilePhotoUrl',
                                'profilePhoto'
                            )
                        );

                    const codeCell =
                        rowNode.querySelector('.col-code');
                    if (codeCell) {
                        codeCell.textContent = employeeNo;
                    }

                    const nameCell =
                        rowNode.querySelector('.col-name');
                    const nameStrong =
                        nameCell?.querySelector('strong');
                    const nameDesignation =
                        nameCell?.querySelector(
                            '.employee-designation-label'
                        );

                    if (nameStrong) {
                        nameStrong.textContent =
                            fullName || '-';
                    }

                    if (nameDesignation) {
                        nameDesignation.textContent =
                            designationName;
                    }

                    nameCell?.addEventListener(
                        'click',
                        () => {
                            void navigateToEmployee(employeeId);
                        }
                    );

                    const workCell =
                        rowNode.querySelector('.col-work');
                    if (workCell) {
                        const departmentElement =
                            workCell.querySelector(
                                '.department-val'
                            );
                        const designationElement =
                            workCell.querySelector(
                                '.designation-val'
                            );

                        if (departmentElement) {
                            departmentElement.textContent =
                                departmentName;
                        }
                        if (designationElement) {
                            designationElement.textContent =
                                designationName;
                        }
                    }

                    const categoryCell =
                        rowNode.querySelector('.col-category');
                    if (categoryCell) {
                        categoryCell.textContent =
                            formatEnum(employeeCategoryValue);
                    }

                    const emailElement =
                        rowNode.querySelector('.email-val');
                    const phoneElement =
                        rowNode.querySelector('.phone-val');

                    if (emailElement) {
                        emailElement.textContent =
                            officialEmail || 'No email';
                    }
                    if (phoneElement) {
                        phoneElement.textContent =
                            mobileNo || 'No mobile';
                    }

                    const statusBadge =
                        rowNode.querySelector(
                            '.status-badge'
                        );

                    if (statusBadge) {
                        statusBadge.textContent =
                            formatEnum(
                                employmentStatusValue
                            );
                        statusBadge.className =
                            `status-badge badge badge-${String(employmentStatusValue).toLowerCase()}`;
                    }

                    const photoImage =
                        rowNode.querySelector(
                            '.emp-table-photo'
                        );
                    const photoPlaceholder =
                        rowNode.querySelector(
                            '.emp-table-photo-placeholder'
                        );

                    if (photoImage && profilePhoto) {
                        photoImage.src = profilePhoto;
                        removeCssClasses(photoImage, 'hidden');
                        addCssClasses(photoPlaceholder,
                            'hidden'
                        );
                    } else {
                        photoImage?.removeAttribute('src');
                        addCssClasses(photoImage, 'hidden');
                        removeCssClasses(photoPlaceholder,
                            'hidden'
                        );
                    }

                    rowNode
                        .querySelector('.view-more-btn')
                        ?.addEventListener(
                            'click',
                            event => {
                                void window.erpWithButtonFeedback(
                                    event.currentTarget,
                                    'Opening...',
                                    () => navigateToEmployee(
                                        employeeId
                                    )
                                );
                            }
                        );

                    rowNode
                        .querySelector('.edit-row-btn')
                        ?.addEventListener(
                            'click',
                            event => {
                                void window.erpWithButtonFeedback(
                                    event.currentTarget,
                                    'Opening...',
                                    () => navigateToEmployee(
                                        employeeId,
                                        'edit'
                                    )
                                );
                            }
                        );

                    rowNode
                        .querySelector('.delete-btn')
                        ?.addEventListener(
                            'click',
                            () => deleteEmp(
                                employeeId,
                                fullName
                            )
                        );

                    return rowNode;
                }
            );

            table.renderPagination(
                pageData.pageNumber,
                pageData.totalPages,
                pageData.totalElements
            );
        } catch (error) {
            console.error(error);
            showErrorMessage(
                error.message ||
                'Failed to load employees.'
            );
            table?.render([]);
        }
    }

    table = new CrudTable(
        {
            tbody:
                viewContainer.querySelector(
                    '#emp-tableBody'
                ),
            pageSize:
                viewContainer.querySelector(
                    '#emp-pageSize'
                ),
            pageInfo:
                viewContainer.querySelector(
                    '#emp-pageInfo'
                ),
            btnPrev:
                viewContainer.querySelector(
                    '#btn-emp-prev'
                ),
            btnNext:
                viewContainer.querySelector(
                    '#btn-emp-next'
                ),
            tplLoading:
                document.getElementById(
                    'global-table-fetching-template'
                ),
            tplEmpty:
                document.getElementById(
                    'global-table-empty-template'
                ),
            tplRow:
                document.getElementById(
                    'tpl-emp-row'
                ),
            table:
                viewContainer.querySelector(
                    '#emp-tableComponent'
                )
        },
        {
            onPageChange: async direction => {
                state.page += direction;
                await loadEmployees();
            },
            onSizeChange: async size => {
                state.size = size;
                state.page = 0;
                await loadEmployees();
            },
            onSort: async field => {
                state.sort = field;
                await loadEmployees();
            }
        }
    );

    viewContainer
        .querySelector('#emp-searchBtn')
        ?.addEventListener('click', async () => {
            state.page = 0;
            await loadEmployees();
        });

    viewContainer
        .querySelector('#emp-searchInput')
        ?.addEventListener('keydown', async event => {
            if (event.key !== 'Enter') return;
            event.preventDefault();
            state.page = 0;
            await loadEmployees();
        });

    viewContainer
        .querySelector('#emp-resetBtn')
        ?.addEventListener('click', async () => {
            const searchInput =
                viewContainer.querySelector(
                    '#emp-searchInput'
                );
            const categoryFilter =
                viewContainer.querySelector(
                    '#emp-categoryFilter'
                );
            const statusFilter =
                viewContainer.querySelector(
                    '#emp-statusFilter'
                );

            if (searchInput) searchInput.value = '';
            if (categoryFilter) categoryFilter.value = '';
            if (statusFilter) statusFilter.value = '';

            state.page = 0;
            await loadEmployees();
        });

    async function loadSelectOptions(excludeEmployeeId = null) {
        const branchId = getRequiredBranchId();
        const reportingManagerEndpoint =
            excludeEmployeeId
                ? `/branchadmin/employees/reporting-managers?excludeEmployeeId=${encodeURIComponent(excludeEmployeeId)}`
                : '/branchadmin/employees/reporting-managers';

        const [
            departmentResponse,
            designationResponse,
            reportingManagerResponse
        ] = await Promise.all([
            apiGet(
                `/departments?branchId=${branchId}&size=100`
            ),
            apiGet(
                `/designations?branchId=${branchId}&size=100`
            ),
            apiGet(reportingManagerEndpoint)
        ]);

        const departments =
            departmentResponse?.data?.content ??
            departmentResponse?.data ??
            [];

        const designations =
            designationResponse?.data?.content ??
            designationResponse?.data ??
            [];

        const reportingManagers =
            reportingManagerResponse?.data ??
            reportingManagerResponse ??
            [];

        const departmentSelect =
            viewContainer.querySelector(
                '#edit-empDepartment'
            );

        const designationSelect =
            viewContainer.querySelector(
                '#edit-empDesignation'
            );

        const reportingManagerSelect =
            viewContainer.querySelector(
                '#edit-empReportingManager'
            );

        if (departmentSelect) {
            departmentSelect.innerHTML =
                '<option value="">-- Select Department --</option>';

            departments.forEach(department => {
                const option = document.createElement('option');
                option.value =
                    String(
                        department.departmentId ??
                        department.id ??
                        ''
                    );
                option.textContent =
                    department.departmentName ??
                    department.name ??
                    'Unnamed Department';
                departmentSelect.appendChild(option);
            });
        }

        if (designationSelect) {
            designationSelect.innerHTML =
                '<option value="">-- Select Designation --</option>';

            designations.forEach(designation => {
                const option = document.createElement('option');
                option.value =
                    String(
                        designation.designationId ??
                        designation.id ??
                        ''
                    );
                option.textContent =
                    designation.designationName ??
                    designation.name ??
                    'Unnamed Designation';
                designationSelect.appendChild(option);
            });
        }

        if (reportingManagerSelect) {
            reportingManagerSelect.innerHTML =
                '<option value="">-- No Reporting Manager --</option>';

            reportingManagers.forEach(manager => {
                const option = document.createElement('option');
                option.value =
                    String(
                        manager.employeeId ?? ''
                    );
                option.textContent = [
                    manager.employeeNo,
                    manager.fullName,
                    manager.designationName
                ].filter(Boolean).join(' - ');
                reportingManagerSelect.appendChild(option);
            });
        }
    }

    const createDetailItem = (label, value) => {
        const item = document.createElement('div');
        item.className = 'emp-record-item';

        const labelElement = document.createElement('span');
        labelElement.className = 'emp-record-label';
        labelElement.textContent = label;

        const valueElement = document.createElement('strong');
        valueElement.className = 'emp-record-value';
        valueElement.textContent =
            value !== undefined &&
            value !== null &&
            String(value).trim() !== ''
                ? String(value)
                : '-';

        item.appendChild(labelElement);
        item.appendChild(valueElement);
        return item;
    };

    const createSecureViewButton = (
        label,
        url
    ) => {
        if (!url) return null;

        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'btn-primary btn-sm';
        button.innerHTML =
            `<i class="bi bi-box-arrow-up-right"></i> ${label}`;
        button.addEventListener('click', () => {
            window.open(
                url,
                '_blank',
                'noopener,noreferrer'
            );
        });
        return button;
    };

    const renderSimpleRecords = (
        containerSelector,
        records,
        mapper,
        actionFactory = null
    ) => {
        const container =
            viewContainer.querySelector(
                containerSelector
            );

        if (!container) return;
        container.innerHTML = '';

        if (!records.length) {
            const empty = document.createElement('p');
            empty.className = 'text-muted';
            empty.textContent = 'No records available.';
            container.appendChild(empty);
            return;
        }

        records.forEach((record, index) => {
            const card = document.createElement('div');
            card.className = 'emp-record-card';

            const heading = document.createElement('h4');
            heading.textContent = `Record ${index + 1}`;
            card.appendChild(heading);

            mapper(record).forEach(item => {
                card.appendChild(
                    createDetailItem(
                        item.label,
                        item.value
                    )
                );
            });

            if (typeof actionFactory === 'function') {
                const actions = actionFactory(record) || [];
                const validActions = actions.filter(Boolean);

                if (validActions.length) {
                    const actionContainer =
                        document.createElement('div');
                    actionContainer.className =
                        'emp-document-actions mt-2';
                    validActions.forEach(action =>
                        actionContainer.appendChild(action)
                    );
                    card.appendChild(actionContainer);
                }
            }

            container.appendChild(card);
        });
    };

    const renderDocuments = documents => {
        const container =
            viewContainer.querySelector(
                '#documents-view-container'
            );

        if (!container) return;
        container.innerHTML = '';

        if (!documents.length) {
            const empty = document.createElement('p');
            empty.className = 'text-muted';
            empty.textContent = 'No documents uploaded.';
            container.appendChild(empty);
            return;
        }

        documents.forEach(documentRecord => {
            const card = document.createElement('article');
            card.className = 'emp-document-card';

            const header = document.createElement('div');
            header.className = 'emp-document-card-header';

            const headingGroup = document.createElement('div');
            const name = document.createElement('div');
            name.className = 'emp-document-name';
            name.textContent =
                documentRecord.employeeDocumentName ||
                formatEnum(documentRecord.employeeDocumentType) ||
                'Employee Document';

            const type = document.createElement('div');
            type.className = 'emp-document-type';
            type.textContent = formatEnum(
                documentRecord.employeeDocumentType
            );

            headingGroup.appendChild(name);
            headingGroup.appendChild(type);
            header.appendChild(headingGroup);
            card.appendChild(header);

            const meta = document.createElement('div');
            meta.className = 'emp-document-meta';
            meta.textContent = [
                documentRecord.employeeDocumentOriginalFileName,
                documentRecord.employeeDocumentMimeType,
                documentRecord.employeeDocumentFileSize
                    ? `${documentRecord.employeeDocumentFileSize} bytes`
                    : null,
                documentRecord.employeeDocumentVerified === true
                    ? 'Verified'
                    : 'Not Verified'
            ].filter(Boolean).join(' • ') || 'File information unavailable';
            card.appendChild(meta);

            if (documentRecord.employeeDocumentRemarks) {
                const remarks = document.createElement('p');
                remarks.className = 'emp-document-meta';
                remarks.textContent =
                    documentRecord.employeeDocumentRemarks;
                card.appendChild(remarks);
            }

            const employeeId = currentDetailEmpId;
            const documentId =
                documentRecord.employeeDocumentId;
            const available =
                documentRecord.employeeDocumentAvailable === true &&
                Boolean(employeeId && documentId);

            if (available) {
                const actions = document.createElement('div');
                actions.className = 'emp-document-actions';
                const viewButton = createSecureViewButton(
                    'View Document',
                    `/api/branchadmin/employees/${employeeId}/documents/${documentId}/view`
                );
                if (viewButton) {
                    actions.appendChild(viewButton);
                    card.appendChild(actions);
                }
            }

            container.appendChild(card);
        });
    };

    const populateEditCollections = employee => {
        const contactsContainer =
            viewContainer.querySelector(
                '#contacts-container'
            );
        const qualificationsContainer =
            viewContainer.querySelector(
                '#qualifications-container'
            );
        const experiencesContainer =
            viewContainer.querySelector(
                '#experiences-container'
            );

        if (contactsContainer) {
            contactsContainer.innerHTML = '';
            getCollection(employee, 'contacts')
                .forEach(contact => {
                    EmpCollections.createRow(
                        contactsContainer,
                        EmpCollections.contactFields,
                        'contact-row',
                        contact,
                        false
                    );
                });
        }

        if (qualificationsContainer) {
            qualificationsContainer.innerHTML = '';
            getCollection(employee, 'qualifications')
                .forEach(qualification => {
                    EmpCollections.createRow(
                        qualificationsContainer,
                        EmpCollections.qualFields,
                        'qual-row',
                        qualification,
                        false
                    );
                });
        }

        if (experiencesContainer) {
            experiencesContainer.innerHTML = '';
            getCollection(employee, 'experiences')
                .forEach(experience => {
                    EmpCollections.createRow(
                        experiencesContainer,
                        EmpCollections.expFields,
                        'exp-row',
                        experience,
                        false
                    );
                });
        }

    };

    async function openEmpDetail(
        id,
        { showLoading = true } = {}
    ) {
        if (!id) return false;

        currentDetailEmpId = id;

        if (showLoading) {
            showLoader('Opening Employee details...');
        }

        try {
            await loadSelectOptions(id);

            const response = await apiGet(
                `/branchadmin/employees/${id}`
            );

            const employee =
                /** @type {EmployeeData} */ (
                response?.data ?? response
            );

            currentEmployee = employee;

            const fullName =
                employee.fullName ||
                [
                    employee.title,
                    employee.firstName,
                    employee.middleName,
                    employee.lastName
                ].filter(Boolean).join(' ');

            setText(
                '#detail-empNameHeader',
                fullName
            );
            setText(
                '#detail-empCodeHeader',
                employee.employeeNo
                    ? `(${employee.employeeNo})`
                    : ''
            );

            bindField('empCode', employee.employeeNo);
            bindField('empTitle', employee.title);
            bindField('empFirstName', employee.firstName);
            bindField('empMiddleName', employee.middleName);
            bindField('empLastName', employee.lastName);
            bindField('empGender', employee.gender, formatEnum);
            bindField('empDob', employee.dateOfBirth, formatDate);
            bindField('empMaritalStatus', employee.maritalStatus, formatEnum);
            bindField('empBloodGroup', employee.bloodGroup, formatEnum);
            bindField('empReligion', employee.religion, formatEnum);
            bindField('empSubReligion', employee.subReligion);

            setText('#view-empName', fullName);

            const departmentId =
                employee.departmentId ??
                employee.department?.departmentId ??
                employee.department?.id ??
                null;

            const designationId =
                employee.designationId ??
                employee.designation?.designationId ??
                employee.designation?.id ??
                null;

            const departmentName =
                employee.departmentName ??
                employee.department?.departmentName ??
                '-';

            const designationName =
                employee.designationName ??
                employee.designation?.designationName ??
                '-';

            setInput('#edit-empDepartment', departmentId);
            setInput('#edit-empDesignation', designationId);
            setText('#view-empDepartment', departmentName);
            setText('#view-empDesignation', designationName);
            setText('#summary-empDepartment', departmentName);
            setText('#summary-empDesignation', designationName);

            const category =
                employee.employeeCategory ??
                employee.category ??
                null;

            setText(
                '#view-empCategory',
                category,
                formatEnum
            );

            if (category === 'MANAGEMENT_TEACHING') {
                setInput('#edit-empCategory', 'MANAGEMENT');
                setInput('#edit-empManagementType', 'TEACHING');
            } else if (
                category === 'MANAGEMENT_NON_TEACHING'
            ) {
                setInput('#edit-empCategory', 'MANAGEMENT');
                setInput('#edit-empManagementType', 'NON_TEACHING');
            } else {
                setInput('#edit-empCategory', category);
                setInput('#edit-empManagementType', '');
            }

            synchronizeEditManagementCategory();

            bindField('empType', employee.employeeType, formatEnum);
            bindField('empMode', employee.employmentMode, formatEnum);
            bindField('empStatus', employee.employmentStatus, formatEnum);
            bindField('empJoiningDate', employee.joiningDate, formatDate);
            bindField('empProbationEndDate', employee.probationEndDate, formatDate);
            bindField('empConfirmationDate', employee.confirmationDate, formatDate);
            bindField('empResignationDate', employee.resignationDate, formatDate);
            bindField('empTerminationDate', employee.terminationDate, formatDate);
            bindField('empEmploymentEndDate', employee.employmentEndDate, formatDate);
            bindField('empExitReason', employee.exitReason);

            const reportingManagerName =
                employee.reportingManagerName ??
                employee.reportingManager?.fullName ??
                null;

            setText(
                '#view-empReportingManager',
                reportingManagerName ||
                'Not Assigned'
            );
            setInput(
                '#edit-empReportingManager',
                employee.reportingManagerId
            );

            bindField('empNationality', employee.nationality);
            bindField('empNationalId', employee.nationalId);
            bindField('empTin', employee.tinNumber);
            bindField('empPassportNo', employee.passportNo);
            bindField('empPassportExpiry', employee.passportExpiryDate, formatDate);
            bindField('empWorkPermit', employee.workPermitNumber);
            bindField('empWorkPermitExpiry', employee.workPermitExpiryDate, formatDate);

            bindField('empEmail', employee.officialEmail);
            bindField('empPersonalEmail', employee.personalEmail);
            bindField('empPhone', employee.mobileNo);
            bindField('empAlternatePhone', employee.alternateMobile);

            bindField('empCountry', employee.addressCountry);
            bindField('empState', employee.addressState);
            bindField('empDistrict', employee.addressDistrict);
            bindField('empCounty', employee.addressCounty);
            bindField('empSubCounty', employee.addressSubCounty);
            bindField('empParish', employee.addressParish);
            bindField('empVillage', employee.addressVillage);
            bindField('empStreet', employee.addressStreet);
            bindField('empPostalCode', employee.postalCode);

            bindField('empSkills', employee.skills);
            bindField('empLanguages', employee.languagesSpoken);
            bindField('empRemarks', employee.employeeRemarks);

            setText(
                '#summary-empStatus',
                employee.employmentStatus,
                formatEnum
            );

            const summaryStatus =
                viewContainer.querySelector(
                    '#summary-empStatus'
                );
            if (summaryStatus) {
                summaryStatus.className =
                    `status-badge badge badge-${String(employee.employmentStatus || 'ACTIVE').toLowerCase()}`;
            }

            setText(
                '#view-empLoginEnabled',
                employee.loginEnabled,
                formatBoolean
            );
            setText(
                '#view-empLoginId',
                employee.loginId ??
                employee.username ??
                employee.user?.username
            );
            setText(
                '#view-empLoginRole',
                employee.loginRole ??
                employee.roleName
            );
            setText(
                '#view-empLoginStatus',
                employee.loginStatus ??
                employee.user?.status,
                formatEnum
            );
            setText(
                '#view-empMustChangePassword',
                employee.mustChangePassword,
                formatBoolean
            );
            setText(
                '#view-empTemporaryPasswordExpiry',
                employee.temporaryPasswordExpiresAt
                    ? String(employee.temporaryPasswordExpiresAt).replace('T', ' ')
                    : null
            );
            setText(
                '#view-empCredentialStatus',
                employee.credentialDeliveryStatus,
                formatEnum
            );
            setText(
                '#view-empCredentialsSentAt',
                employee.credentialsSentAt
                    ? String(employee.credentialsSentAt).replace('T', ' ')
                    : null
            );
            setText(
                '#view-empCredentialAttempts',
                employee.credentialDeliveryAttempts ?? 0
            );

            const hasLoginAccount =
                employee.loginEnabled === true ||
                Boolean(
                    employee.userId ||
                    employee.username ||
                    employee.loginId ||
                    employee.user?.id ||
                    employee.user?.username
                );
            const employeeIsActive =
                employee.active !== false;
            const createLoginButton =
                viewContainer.querySelector(
                    '#btn-create-employee-login'
                );
            const resetPasswordButton =
                viewContainer.querySelector(
                    '#btn-reset-employee-password'
                );

            toggleCssClass(
                createLoginButton,
                'hidden',
                hasLoginAccount || !employeeIsActive
            );
            toggleCssClass(
                resetPasswordButton,
                'hidden',
                !hasLoginAccount || !employeeIsActive
            );

            if (resetPasswordButton) {
                const deliveryStatus = String(
                    employee.credentialDeliveryStatus || ''
                ).toUpperCase();
                resetPasswordButton.innerHTML =
                    deliveryStatus === 'NOT_REQUIRED' ||
                    deliveryStatus === 'PENDING' ||
                    deliveryStatus === 'FAILED'
                        ? '<i class="bi bi-envelope"></i> Send Temporary Password'
                        : '<i class="bi bi-key"></i> Reset &amp; Send Temporary Password';
            }

            toggleCssClass(
                editBtn,
                'hidden',
                !employeeIsActive
            );

            setText('#view-empActive', employee.active, formatBoolean);
            setText('#view-empVersion', employee.version);
            setText('#view-empCreatedBy', employee.createdBy);
            setText(
                '#view-empCreatedAt',
                employee.createdAt
                    ? String(employee.createdAt).replace('T', ' ')
                    : null
            );
            setText('#view-empUpdatedBy', employee.updatedBy);
            setText(
                '#view-empUpdatedAt',
                employee.updatedAt
                    ? String(employee.updatedAt).replace('T', ' ')
                    : null
            );

            const profilePhoto = normalizeImageSource(
                employee.profilePhotoUrl ??
                employee.profilePhoto
            );

            const profileImage =
                viewContainer.querySelector(
                    '#view-empProfilePhoto'
                );
            const profilePlaceholder =
                viewContainer.querySelector(
                    '#view-empProfilePhotoPlaceholder'
                );

            if (profileImage && profilePhoto) {
                profileImage.src = profilePhoto;
                removeCssClasses(profileImage, 'hidden');
                addCssClasses(profilePlaceholder, 'hidden');
            } else {
                profileImage?.removeAttribute('src');
                addCssClasses(profileImage, 'hidden');
                removeCssClasses(profilePlaceholder, 'hidden');
            }

            const signatureButton =
                viewContainer.querySelector(
                    '#view-empSignatureBtn'
                );

            const signaturePath =
                employee.signatureFileAvailable === true
                    ? (
                        employee.signatureFileUrl ||
                        `/api/branchadmin/employees/${id}/signature`
                    )
                    : null;

            setText(
                '#view-empSignatureStatus',
                signaturePath
                    ? 'Signature Available'
                    : 'Not Uploaded'
            );

            if (signatureButton) {
                toggleCssClass(signatureButton,
                    'hidden',
                    !signaturePath
                );

                signatureButton.onclick = signaturePath
                    ? () => {
                        window.open(
                            normalizeImageSource(signaturePath),
                            '_blank',
                            'noopener,noreferrer'
                        );
                    }
                    : null;
            }

            const contacts = getCollection(employee, 'contacts');
            const qualifications = getCollection(employee, 'qualifications');
            const experiences = getCollection(employee, 'experiences');
            const documents = getCollection(employee, 'documents');

            setText('#emp-contact-count', `(${contacts.length})`);
            setText('#emp-qualification-count', `(${qualifications.length})`);
            setText('#emp-experience-count', `(${experiences.length})`);
            setText('#emp-document-count', `(${documents.length})`);

            renderSimpleRecords(
                '#contacts-view-container',
                contacts,
                contact => [
                    {
                        label: 'Name',
                        value: contact.employeeContactName
                    },
                    {
                        label: 'Relationship',
                        value: formatEnum(
                            contact.employeeContactRelationship
                        )
                    },
                    {
                        label: 'Mobile',
                        value: contact.employeeContactMobile
                    },
                    {
                        label: 'Email',
                        value: contact.employeeContactEmail
                    }
                ]
            );

            renderSimpleRecords(
                '#qualifications-view-container',
                qualifications,
                qualification => [
                    {
                        label: 'Level',
                        value: formatEnum(
                            qualification.employeeQualificationLevel
                        )
                    },
                    {
                        label: 'Qualification',
                        value: qualification.employeeQualificationName
                    },
                    {
                        label: 'Institution',
                        value: qualification.employeeQualificationInstitutionName
                    },
                    {
                        label: 'Specialization',
                        value: qualification.employeeQualificationSpecialization
                    },
                    {
                        label: 'Grade',
                        value:
                            qualification.employeeQualificationGrade ||
                            qualification.qualificationGrade
                    },
                    {
                        label: 'Start Year',
                        value: qualification.employeeQualificationStartYear
                    },
                    {
                        label: 'Completion Year',
                        value: qualification.employeeQualificationCompletionYear
                    },
                    {
                        label: 'Certificate',
                        value:
                            qualification.employeeQualificationDocumentAvailable === true
                                ? 'Uploaded'
                                : 'Not Uploaded'
                    },
                    {
                        label: 'Verification',
                        value:
                            qualification.employeeQualificationVerified === true
                                ? 'Verified'
                                : 'Not Verified'
                    }
                ],
                qualification => {
                    if (
                        qualification.employeeQualificationDocumentAvailable !== true ||
                        !qualification.employeeQualificationId
                    ) {
                        return [];
                    }

                    return [
                        createSecureViewButton(
                            'View Certificate',
                            `/api/branchadmin/employees/${id}/qualifications/${qualification.employeeQualificationId}/view`
                        )
                    ];
                }
            );

            renderSimpleRecords(
                '#experiences-view-container',
                experiences,
                experience => [
                    {
                        label: 'Organisation',
                        value: experience.employeeExperienceCompanyName
                    },
                    {
                        label: 'Employment Type',
                        value: formatEnum(
                            experience.employeeExperienceEmploymentType
                        )
                    },
                    {
                        label: 'Post Held',
                        value: experience.employeeExperienceDesignation
                    },
                    {
                        label: 'Start Date',
                        value: formatDate(
                            experience.employeeExperienceStartDate
                        )
                    },
                    {
                        label: 'End Date',
                        value: experience.employeeExperienceCurrentJob
                            ? 'Current Job'
                            : formatDate(
                                experience.employeeExperienceEndDate
                            )
                    },
                    {
                        label: 'Experience Certificate',
                        value:
                            experience.employeeExperienceExperienceCertificateAvailable === true
                                ? 'Uploaded'
                                : 'Not Uploaded'
                    },
                    {
                        label: 'Relieving Letter',
                        value:
                            experience.employeeExperienceRelievingLetterAvailable === true
                                ? 'Uploaded'
                                : 'Not Uploaded'
                    },
                    {
                        label: 'Verification',
                        value:
                            experience.employeeExperienceVerified === true
                                ? 'Verified'
                                : 'Not Verified'
                    }
                ],
                experience => {
                    const experienceId =
                        experience.employeeExperienceId;
                    if (!experienceId) return [];

                    const actions = [];

                    if (
                        experience.employeeExperienceExperienceCertificateAvailable === true
                    ) {
                        actions.push(
                            createSecureViewButton(
                                'View Experience Certificate',
                                `/api/branchadmin/employees/${id}/experiences/${experienceId}/certificate/view`
                            )
                        );
                    }

                    if (
                        experience.employeeExperienceRelievingLetterAvailable === true
                    ) {
                        actions.push(
                            createSecureViewButton(
                                'View Relieving Letter',
                                `/api/branchadmin/employees/${id}/experiences/${experienceId}/relieving-letter/view`
                            )
                        );
                    }

                    return actions;
                }
            );

            renderDocuments(documents);
            populateEditCollections(employee);

            addCssClasses(tableView, 'hidden');
            removeCssClasses(detailView, 'hidden');
            resetEmpEditMode();
            return true;
        } catch (error) {
            console.error(error);
            showErrorMessage(
                error.message ||
                'Failed to load employee details.'
            );
            return false;
        } finally {
            if (showLoading) {
                hideLoader();
            }
        }
    }

    let pendingDeactivationEmployeeId = null;
    let pendingDeactivationEmployeeName = null;

    const deactivationModal =
        viewContainer.querySelector(
            '#employee-deactivation-modal'
        );
    const deactivationForm =
        viewContainer.querySelector(
            '#employee-deactivation-form'
        );
    const deactivationName =
        viewContainer.querySelector(
            '#employee-deactivation-name'
        );
    const deactivationStatus =
        viewContainer.querySelector(
            '#employee-deactivation-status'
        );
    const deactivationDate =
        viewContainer.querySelector(
            '#employee-deactivation-date'
        );
    const deactivationReason =
        viewContainer.querySelector(
            '#employee-deactivation-reason'
        );

    const closeDeactivationModal = () => {
        addCssClasses(deactivationModal, 'hidden');
        deactivationForm?.reset();
        pendingDeactivationEmployeeId = null;
        pendingDeactivationEmployeeName = null;
    };

    async function deleteEmp(id, employeeName = null) {
        if (!id || !deactivationModal) return;

        pendingDeactivationEmployeeId = id;
        pendingDeactivationEmployeeName =
            employeeName || 'Selected Employee';

        if (deactivationName) {
            deactivationName.textContent =
                pendingDeactivationEmployeeName;
        }

        const today = new Date()
            .toISOString()
            .split('T')[0];

        if (deactivationDate) {
            deactivationDate.value = today;
            deactivationDate.max = today;
        }

        removeCssClasses(deactivationModal, 'hidden');
        deactivationStatus?.focus();
    }

    viewContainer
        .querySelector('#employee-deactivation-cancel')
        ?.addEventListener(
            'click',
            closeDeactivationModal
        );

    deactivationModal?.addEventListener(
        'click',
        event => {
            if (event.target === deactivationModal) {
                closeDeactivationModal();
            }
        }
    );

    deactivationForm?.addEventListener(
        'submit',
        async event => {
            event.preventDefault();

            const employmentStatus =
                deactivationStatus?.value?.trim();
            const effectiveDate =
                deactivationDate?.value?.trim();
            const exitReason =
                deactivationReason?.value?.trim();

            if (
                !pendingDeactivationEmployeeId ||
                !employmentStatus ||
                !effectiveDate ||
                !exitReason
            ) {
                showErrorMessage(
                    'Final status, effective date and exit reason are required.'
                );
                return;
            }

            const loaderToken = showLoader();
            let deactivationSucceeded = false;
            let deactivationErrorMessage = null;

            try {
                await apiDelete(
                    `/branchadmin/employees/${pendingDeactivationEmployeeId}`,
                    {
                        employmentStatus,
                        effectiveDate,
                        exitReason
                    }
                );

                const deactivatedEmployeeId =
                    pendingDeactivationEmployeeId;

                closeDeactivationModal();

                if (
                    currentDetailEmpId ===
                    deactivatedEmployeeId
                ) {
                    currentDetailEmpId = null;
                    currentEmployee = null;
                    addCssClasses(detailView, 'hidden');
                    removeCssClasses(tableView, 'hidden');
                }

                await loadEmployees();
                deactivationSucceeded = true;
            } catch (error) {
                console.error(error);
                deactivationErrorMessage =
                    error.message ||
                    'Failed to deactivate Employee.';
            } finally {
                hideLoader(loaderToken);
            }

            await waitForEmployeeFeedbackExit();

            if (deactivationErrorMessage) {
                showErrorMessage(deactivationErrorMessage);
            } else if (deactivationSucceeded) {
                showSuccessMessage(
                    'Employee deactivated successfully.'
                );
            }
        }
    );

    function enterEmpEditMode() {
        viewContainer
            .querySelectorAll('.detail-text')
            .forEach(element => {
                addCssClasses(element, 'hidden');
            });

        viewContainer
            .querySelectorAll(
                '.detail-input:not([disabled])'
            )
            .forEach(element => {
                removeCssClasses(element, 'hidden');
            });

        [
            '#contacts-view-container',
            '#qualifications-view-container',
            '#experiences-view-container'
        ].forEach(selector => {
            addCssClasses(
                viewContainer.querySelector(selector),
                'hidden'
            );
        });

        [
            '#contacts-container',
            '#qualifications-container',
            '#experiences-container'
        ].forEach(selector => {
            removeCssClasses(
                viewContainer.querySelector(selector),
                'hidden'
            );
        });

        viewContainer
            .querySelectorAll('.add-row-btn')
            .forEach(button => {
                removeCssClasses(button, 'hidden');
            });

        viewContainer
            .querySelectorAll(
                '.emp-child-row .detail-text'
            )
            .forEach(element => {
                addCssClasses(element, 'hidden');
            });

        viewContainer
            .querySelectorAll(
                '.emp-child-row .detail-input'
            )
            .forEach(element => {
                removeCssClasses(element, 'hidden');
            });

        synchronizeEditManagementCategory();

        addCssClasses(editBtn, 'hidden');
        removeCssClasses(saveBtn, 'hidden');
        removeCssClasses(cancelEditBtn, 'hidden');
    }

    function resetEmpEditMode() {
        viewContainer
            .querySelectorAll('.detail-text')
            .forEach(element => {
                removeCssClasses(element, 'hidden');
            });

        viewContainer
            .querySelectorAll('.detail-input')
            .forEach(element => {
                addCssClasses(element, 'hidden');
            });

        [
            '#contacts-view-container',
            '#qualifications-view-container',
            '#experiences-view-container'
        ].forEach(selector => {
            removeCssClasses(
                viewContainer.querySelector(selector),
                'hidden'
            );
        });

        [
            '#contacts-container',
            '#qualifications-container',
            '#experiences-container'
        ].forEach(selector => {
            addCssClasses(
                viewContainer.querySelector(selector),
                'hidden'
            );
        });

        viewContainer
            .querySelectorAll('.add-row-btn')
            .forEach(button => {
                addCssClasses(button, 'hidden');
            });

        addCssClasses(editManagementTypeGroup, 'hidden');

        removeCssClasses(editBtn, 'hidden');
        addCssClasses(saveBtn, 'hidden');
        addCssClasses(cancelEditBtn, 'hidden');
    }

    editBtn?.addEventListener(
        'click',
        event => {
            void window.erpWithButtonFeedback(
                event.currentTarget,
                'Opening Edit Mode...',
                async () => {
                    await new Promise(resolve =>
                        requestAnimationFrame(resolve)
                    );
                    enterEmpEditMode();
                }
            );
        }
    );

    backBtn?.addEventListener('click', event => {
        void window.erpWithButtonFeedback(
            event.currentTarget,
            'Opening Employee List...',
            () => window.erpNavigate({
                role: 'admin',
                view: 'employees',
                routeParams: [],
                title: 'Manage Employees'
            })
        );
    });
    /**
     * @type {{
     *     confirmationResolver:
     *         (((confirmed: boolean) => void) | null),
     *     previousFocus: (HTMLElement | null),
     *     operationRunning: boolean
     * }}
     */
    const employeeDialogState = {
        confirmationResolver: null,
        previousFocus: null,
        operationRunning: false
    };

    function getEmployeeDialogElements() {
        return {
            confirmationOverlay:
                viewContainer.querySelector(
                    '#employee-confirm-overlay'
                ),
            confirmationDialog:
                viewContainer.querySelector(
                    '#employee-confirm-dialog'
                ),
            confirmationTitle:
                viewContainer.querySelector(
                    '#employee-confirm-title'
                ),
            confirmationMessage:
                viewContainer.querySelector(
                    '#employee-confirm-message'
                ),
            confirmButton:
                viewContainer.querySelector(
                    '#employee-confirm-submit'
                ),
            cancelButton:
                viewContainer.querySelector(
                    '#employee-confirm-cancel'
                ),
            operationOverlay:
                viewContainer.querySelector(
                    '#employee-operation-overlay'
                ),
            operationTitle:
                viewContainer.querySelector(
                    '#employee-operation-title'
                ),
            operationMessage:
                viewContainer.querySelector(
                    '#employee-operation-message'
                )
        };
    }

    /**
     * @param {boolean} confirmed
     * @returns {void}
     */
    function closeEmployeeConfirmation(confirmed) {
        const elements = getEmployeeDialogElements();

        removeCssClasses(
            elements.confirmationOverlay,
            'is-visible'
        );

        elements.confirmationOverlay
            ?.setAttribute('aria-hidden', 'true');

        const resolver =
            employeeDialogState.confirmationResolver;

        employeeDialogState.confirmationResolver = null;

        window.setTimeout(() => {
            if (
                employeeDialogState.previousFocus &&
                employeeDialogState.previousFocus.isConnected
            ) {
                employeeDialogState.previousFocus.focus();
            }

            employeeDialogState.previousFocus = null;
            resolver?.(confirmed);
        }, 180);
    }

    /**
     * @param {{
     *     title: string,
     *     message: string,
     *     confirmText?: string,
     *     cancelText?: string
     * }} options
     * @returns {Promise<boolean>}
     */
    function showEmployeeConfirmation(options) {
        const {
            title,
            message,
            confirmText = 'Confirm',
            cancelText = 'Cancel'
        } = options;

        const elements = getEmployeeDialogElements();

        if (
            !elements.confirmationOverlay ||
            !elements.confirmationDialog ||
            !elements.confirmationTitle ||
            !elements.confirmationMessage ||
            !elements.confirmButton ||
            !elements.cancelButton
        ) {
            console.error(
                'Employee confirmation dialog elements are missing.'
            );

            return Promise.resolve(false);
        }

        if (employeeDialogState.confirmationResolver) {
            return Promise.resolve(false);
        }

        elements.confirmationTitle.textContent = title;
        elements.confirmationMessage.textContent = message;
        elements.confirmButton.textContent = confirmText;
        elements.cancelButton.textContent = cancelText;

        employeeDialogState.previousFocus =
            document.activeElement instanceof HTMLElement
                ? document.activeElement
                : null;

        addCssClasses(
            elements.confirmationOverlay,
            'is-visible'
        );

        elements.confirmationOverlay
            .setAttribute('aria-hidden', 'false');

        window.requestAnimationFrame(() => {
            elements.confirmationDialog.focus();
        });

        return new Promise(resolve => {
            employeeDialogState.confirmationResolver =
                confirmed => resolve(Boolean(confirmed));
        });
    }

    /**
     * @param {{
     *     title?: string,
     *     message?: string
     * }} [options]
     * @returns {boolean}
     */
    function showEmployeeOperation(options = {}) {
        const {
            title = 'Processing',
            message = 'Please wait...'
        } = options;

        const elements = getEmployeeDialogElements();

        if (
            !elements.operationOverlay ||
            !elements.operationTitle ||
            !elements.operationMessage
        ) {
            console.error(
                'Employee operation overlay elements are missing.'
            );

            return false;
        }

        elements.operationTitle.textContent = title;
        elements.operationMessage.textContent = message;

        addCssClasses(
            elements.operationOverlay,
            'is-visible'
        );
        elements.operationOverlay.setAttribute(
            'aria-hidden',
            'false'
        );

        return true;
    }

    function hideEmployeeOperation() {
        const elements = getEmployeeDialogElements();

        removeCssClasses(
            elements.operationOverlay,
            'is-visible'
        );

        elements.operationOverlay
            ?.setAttribute('aria-hidden', 'true');
    }

    /**
     * Allows the 180 ms overlay/feedback exit transition to finish before
     * opening a success or error dialog.
     *
     * @returns {Promise<void>}
     */
    function waitForEmployeeFeedbackExit() {
        return new Promise(resolve => {
            window.setTimeout(resolve, 200);
        });
    }

    function initializeEmployeeDialogs() {
        const elements = getEmployeeDialogElements();

        if (
            !elements.confirmationOverlay ||
            !elements.confirmButton ||
            !elements.cancelButton
        ) {
            console.error(
                'Employee dialog initialization failed.'
            );

            return;
        }

        if (
            elements.confirmationOverlay
                .dataset.initialized === 'true'
        ) {
            return;
        }

        elements.confirmationOverlay
            .dataset.initialized = 'true';

        elements.confirmButton.addEventListener(
            'click',
            () => closeEmployeeConfirmation(true)
        );

        elements.cancelButton.addEventListener(
            'click',
            () => closeEmployeeConfirmation(false)
        );

        elements.confirmationOverlay.addEventListener(
            'click',
            event => {
                if (
                    event.target ===
                    elements.confirmationOverlay
                ) {
                    closeEmployeeConfirmation(false);
                }
            }
        );

        elements.confirmationOverlay.addEventListener(
            'keydown',
            event => {
                if (event.key === 'Escape') {
                    closeEmployeeConfirmation(false);
                }
            }
        );
    }

    initializeEmployeeDialogs();

    cancelEditBtn?.addEventListener(
        'click',
        async () => {
            if (employeeDialogState.operationRunning) {
                return;
            }

            const confirmed =
                await showEmployeeConfirmation({
                    title: 'Discard Employee Changes?',
                    message:
                        'Unsaved changes will be removed and the saved Employee details will be restored.',
                    confirmText: 'Discard Changes',
                    cancelText: 'Continue Editing'
                });

            if (!confirmed) {
                return;
            }

            employeeDialogState.operationRunning = true;

            showEmployeeOperation({
                title: 'Restoring Details',
                message:
                    'Please wait while the saved Employee details are restored.'
            });

            let restored = false;

            try {
                restored = true;

                if (currentDetailEmpId) {
                    restored = await openEmpDetail(
                        currentDetailEmpId,
                        { showLoading: false }
                    );
                } else {
                    resetEmpEditMode();
                }

                if (!restored) {
                    return;
                }

            } catch (error) {
                console.error(error);

                showErrorMessage(
                    error?.message ||
                    'Could not restore the saved Employee details.'
                );
            } finally {
                employeeDialogState.operationRunning = false;
                hideEmployeeOperation();
            }

            if (restored) {
                await waitForEmployeeFeedbackExit();
                showSuccessMessage(
                    'Unsaved Employee changes were discarded.'
                );
            }
        }
    );

    const employeeLoginModal =
        viewContainer.querySelector(
            '#employee-login-modal'
        );
    const employeeLoginForm =
        viewContainer.querySelector(
            '#employee-login-form'
        );
    const employeeLoginName =
        viewContainer.querySelector(
            '#employee-login-name'
        );
    const employeeLoginRole =
        viewContainer.querySelector(
            '#employee-login-role'
        );
    const employeeLoginSendEmail =
        viewContainer.querySelector(
            '#employee-login-send-email'
        );

    const closeEmployeeLoginModal = () => {
        addCssClasses(employeeLoginModal, 'hidden');
        employeeLoginModal?.setAttribute(
            'aria-hidden',
            'true'
        );
        document.body.style.overflow = '';
        employeeLoginForm?.reset();
    };

    const loadLoginRoleOptions = async selectElement => {
        if (!selectElement) return;

        const response = await apiGet(
            '/branchadmin/employees/login-role-options'
        );
        const roles = response?.data ?? response ?? [];

        selectElement.innerHTML =
            '<option value="">-- Select Role --</option>';

        roles.forEach(role => {
            const option = document.createElement('option');
            option.value =
                String(
                    role.roleId ?? ''
                );
            option.textContent =
                role.roleName ||
                formatEnum(role.roleCode);
            selectElement.appendChild(option);
        });
    };

    let employeeLoginModalOpening = false;

    const openEmployeeLoginModal = async () => {
        if (
            employeeLoginModalOpening ||
            !currentEmployee ||
            !currentDetailEmpId ||
            !employeeLoginModal
        ) {
            return;
        }

        if (!currentEmployee.officialEmail) {
            showErrorMessage(
                'Official email is required before creating a login account.'
            );
            return;
        }

        employeeLoginModalOpening = true;
        showLoader('Loading Employee account options...');

        try {
            await loadLoginRoleOptions(
                employeeLoginRole
            );

            if (
                !employeeLoginRole ||
                employeeLoginRole.options.length <= 1
            ) {
                showErrorMessage(
                    'No Employee login roles are available.'
                );

                return;
            }

            if (employeeLoginName) {
                employeeLoginName.textContent =
                    currentEmployee.fullName ||
                    currentEmployee.employeeNo ||
                    'Selected Employee';
            }

            if (employeeLoginSendEmail) {
                employeeLoginSendEmail.checked = true;
            }

            employeeLoginModal.setAttribute(
                'aria-hidden',
                'false'
            );

            document.body.style.overflow =
                'hidden';

            removeCssClasses(
                employeeLoginModal,
                'hidden'
            );

            window.setTimeout(
                () => employeeLoginRole?.focus(),
                80
            );
        } catch (error) {
            console.error(error);
            showErrorMessage(
                error?.message ||
                'Could not load Employee login roles.'
            );
        } finally {
            employeeLoginModalOpening = false;
            hideLoader();
        }
    };

    /*
     * Event delegation prevents the Create Login Account button from becoming
     * inactive after Employee detail content is refreshed.
     */
    viewContainer.addEventListener(
        'click',
        event => {
            const target =
                event.target instanceof Element
                    ? event.target
                    : null;

            const createLoginButton =
                target?.closest(
                    '#btn-create-employee-login'
                );

            if (!createLoginButton) {
                return;
            }

            event.preventDefault();
            event.stopPropagation();

            void openEmployeeLoginModal();
        }
    );

    viewContainer
        .querySelector('#employee-login-cancel')
        ?.addEventListener(
            'click',
            closeEmployeeLoginModal
        );

    employeeLoginModal?.addEventListener(
        'click',
        event => {
            if (event.target === employeeLoginModal) {
                closeEmployeeLoginModal();
            }
        }
    );

    employeeLoginForm?.addEventListener(
        'submit',
        async event => {
            event.preventDefault();

            if (!currentDetailEmpId) return;

            const roleId = Number.parseInt(
                employeeLoginRole?.value || '',
                10
            );

            if (!Number.isInteger(roleId) || roleId <= 0) {
                showErrorMessage(
                    'Select a valid login role.'
                );
                return;
            }

            const loaderToken = showLoader();
            let loginCreated = false;
            let loginCreationErrorMessage = null;

            try {
                await apiPost(
                    `/branchadmin/employees/${currentDetailEmpId}/login-account`,
                    {
                        roleId,
                        sendEmail:
                            employeeLoginSendEmail?.checked === true
                    }
                );

                closeEmployeeLoginModal();
                loginCreated = await openEmpDetail(
                    currentDetailEmpId,
                    { showLoading: false }
                );
            } catch (error) {
                console.error(error);
                loginCreationErrorMessage =
                    error.message ||
                    'Failed to create Employee login account.';
            } finally {
                hideLoader(loaderToken);
            }

            await waitForEmployeeFeedbackExit();

            if (loginCreationErrorMessage) {
                showErrorMessage(loginCreationErrorMessage);
            } else if (loginCreated) {
                showSuccessMessage(
                    'Employee login account created successfully.'
                );
            }
        }
    );

    viewContainer
        .querySelector('#btn-reset-employee-password')
        ?.addEventListener('click', () => {
            if (!currentDetailEmpId || !currentEmployee) {
                return;
            }

            showPremiumModal({
                title: 'Send New Temporary Password',
                type: 'warning',
                contentText:
                    'A new temporary password will replace the current password and be emailed to the Employee.',
                confirmText: 'Generate & Send',
                cancelText: 'Cancel',
                onConfirm: async modal => {
                    modal.close();
                    const loaderToken = showLoader();
                    let passwordReset = false;
                    let passwordResetErrorMessage = null;

                    try {
                        await apiPost(
                            `/branchadmin/employees/${currentDetailEmpId}/temporary-password`,
                            { sendEmail: true }
                        );
                        passwordReset = await openEmpDetail(
                            currentDetailEmpId,
                            { showLoading: false }
                        );
                    } catch (error) {
                        console.error(error);
                        passwordResetErrorMessage =
                            error.message ||
                            'Failed to generate temporary password.';
                    } finally {
                        hideLoader(loaderToken);
                    }

                    await waitForEmployeeFeedbackExit();

                    if (passwordResetErrorMessage) {
                        showErrorMessage(
                            passwordResetErrorMessage
                        );
                    } else if (passwordReset) {
                        showSuccessMessage(
                            'New temporary password queued for email delivery.'
                        );
                    }
                }
            });
        });

    const saveEmployeeChanges = async () => {
        if (!currentDetailEmpId || !currentEmployee) {
            return false;
        }

        const valueOrNull = selector => {
            const element =
                viewContainer.querySelector(selector);
            if (!element) return null;
            const value = String(element.value ?? '').trim();
            return value === '' ? null : value;
        };

        const numberOrNull = selector => {
            const value = valueOrNull(selector);
            if (value === null) return null;
            const parsed = Number.parseInt(value, 10);
            return Number.isInteger(parsed) && parsed > 0
                ? parsed
                : null;
        };

        const firstName =
            valueOrNull('#edit-empFirstName');
        const lastName =
            valueOrNull('#edit-empLastName');

        if (!firstName || !lastName) {
            showErrorMessage(
                'First Name and Last Name are required.'
            );
            return false;
        }

        const employeeCategory =
            resolveEmployeeCategory(
                viewContainer,
                'edit'
            );

        if (!employeeCategory) {
            showErrorMessage(
                'Select a valid Employee Category.'
            );
            return false;
        }

        const version = Number(currentEmployee.version);
        if (!Number.isInteger(version) || version < 0) {
            showErrorMessage(
                'Employee version is missing. Reload the Employee details.'
            );
            return false;
        }

        const employmentStatus =
            valueOrNull('#edit-empStatus') || 'ACTIVE';

        if (
            ['RESIGNED', 'RETIRED', 'TERMINATED']
                .includes(employmentStatus) &&
            currentEmployee.active !== false
        ) {
            showErrorMessage(
                'Use Deactivate Employee to set a final employment status.'
            );
            return false;
        }

        const profilePhoto =
            viewContainer.querySelector(
                '#edit-empProfilePhoto'
            )?.files?.[0] || null;

        if (profilePhoto) {
            const allowedTypes = new Set([
                'image/jpeg',
                'image/png',
                'image/webp'
            ]);

            if (!allowedTypes.has(profilePhoto.type)) {
                showErrorMessage(
                    'Profile photo must be JPG, PNG or WEBP.'
                );
                return false;
            }

            if (profilePhoto.size > 2 * 1024 * 1024) {
                showErrorMessage(
                    'Profile photo must not exceed 2 MB.'
                );
                return false;
            }
        }



        try {
            const payload = {
                version,
                title: valueOrNull('#edit-empTitle'),
                firstName,
                middleName: valueOrNull('#edit-empMiddleName'),
                lastName,
                gender: valueOrNull('#edit-empGender'),
                dateOfBirth: valueOrNull('#edit-empDob'),
                maritalStatus: valueOrNull('#edit-empMaritalStatus'),
                bloodGroup: valueOrNull('#edit-empBloodGroup'),
                religion: valueOrNull('#edit-empReligion'),
                subReligion: valueOrNull('#edit-empSubReligion'),

                profilePhotoData:
                    await EmpCollections.fileToBase64(
                        profilePhoto
                    ),
                profilePhotoFileName:
                    profilePhoto?.name || null,
                profilePhotoContentType:
                    profilePhoto?.type || null,
                profilePhotoFileSize:
                    profilePhoto?.size || null,

                signatureFileData: null,
                signatureFileName: null,
                signatureContentType: null,
                signatureFileSize: null,

                officialEmail: valueOrNull('#edit-empEmail'),
                personalEmail: valueOrNull('#edit-empPersonalEmail'),
                mobileNo: valueOrNull('#edit-empPhone'),
                alternateMobile: valueOrNull('#edit-empAlternatePhone'),

                departmentId: numberOrNull('#edit-empDepartment'),
                designationId: numberOrNull('#edit-empDesignation'),
                reportingManagerId:
                    numberOrNull('#edit-empReportingManager'),
                employeeCategory,
                employeeType: valueOrNull('#edit-empType'),
                employmentMode: valueOrNull('#edit-empMode'),
                employmentStatus,

                joiningDate: valueOrNull('#edit-empJoiningDate'),
                probationEndDate: valueOrNull('#edit-empProbationEndDate'),
                confirmationDate: valueOrNull('#edit-empConfirmationDate'),
                retirementDate:
                    currentEmployee.retirementDate || null,
                resignationDate:
                    currentEmployee.resignationDate || null,
                terminationDate:
                    currentEmployee.terminationDate || null,
                exitReason:
                    currentEmployee.exitReason || null,

                nationality: valueOrNull('#edit-empNationality'),
                nationalId: valueOrNull('#edit-empNationalId'),
                tinNumber: valueOrNull('#edit-empTin'),
                passportNo: valueOrNull('#edit-empPassportNo'),
                passportExpiryDate: valueOrNull('#edit-empPassportExpiry'),
                workPermitNumber: valueOrNull('#edit-empWorkPermit'),
                workPermitExpiryDate: valueOrNull('#edit-empWorkPermitExpiry'),

                addressCountry: valueOrNull('#edit-empCountry'),
                addressState: valueOrNull('#edit-empState'),
                addressDistrict: valueOrNull('#edit-empDistrict'),
                addressCounty: valueOrNull('#edit-empCounty'),
                addressSubCounty: valueOrNull('#edit-empSubCounty'),
                addressParish: valueOrNull('#edit-empParish'),
                addressVillage: valueOrNull('#edit-empVillage'),
                addressStreet: valueOrNull('#edit-empStreet'),
                postalCode: valueOrNull('#edit-empPostalCode'),

                skills: valueOrNull('#edit-empSkills'),
                languagesSpoken: valueOrNull('#edit-empLanguages'),
                employeeRemarks: valueOrNull('#edit-empRemarks'),

                contacts: collectContacts(viewContainer),
                qualifications:
                    await collectQualifications(viewContainer),
                experiences:
                    await collectExperiences(viewContainer),
                documents:
                    getCollection(
                        currentEmployee,
                        'documents'
                    ).map(documentRecord => ({
                        employeeDocumentId:
                            documentRecord.employeeDocumentId,
                        employeeDocumentType:
                            documentRecord.employeeDocumentType,
                        employeeDocumentName:
                            documentRecord.employeeDocumentName || null,
                        employeeDocumentDescription:
                            documentRecord.employeeDocumentDescription || null,
                        employeeDocumentIssueDate:
                            documentRecord.employeeDocumentIssueDate || null,
                        employeeDocumentExpiryDate:
                            documentRecord.employeeDocumentExpiryDate || null,
                        employeeDocumentIsMandatory:
                            documentRecord.employeeDocumentIsMandatory ?? false,
                        employeeDocumentActive:
                            documentRecord.employeeDocumentActive ?? true,
                        employeeDocumentRemarks:
                            documentRecord.employeeDocumentRemarks || null,
                        fileData: null,
                        fileName: null,
                        contentType: null,
                        fileSize: null
                    }))
            };

            await apiPut(
                `/branchadmin/employees/${currentDetailEmpId}`,
                payload
            );

            return await openEmpDetail(
                currentDetailEmpId,
                { showLoading: false }
            );
        } catch (error) {
            console.error(error);

            showErrorMessage(
                error.message ||
                'Failed to update Employee.'
            );

            return false;
        }
    };

    saveBtn?.addEventListener(
        'click',
        async () => {
            if (employeeDialogState.operationRunning) {
                return;
            }

            const confirmed =
                await showEmployeeConfirmation({
                    title: 'Confirm Employee Update',
                    message:
                        'Review the edited Employee information. Select Save Changes to update the Employee record.',
                    confirmText: 'Save Changes',
                    cancelText: 'Review Again'
                });

            if (!confirmed) {
                return;
            }

            employeeDialogState.operationRunning = true;

            showEmployeeOperation({
                title: 'Saving Changes',
                message:
                    'Please wait while the Employee record is updated.'
            });

            let saved = false;

            try {
                saved = await saveEmployeeChanges();
            } finally {
                employeeDialogState.operationRunning = false;
                hideEmployeeOperation();
            }

            if (saved) {
                await waitForEmployeeFeedbackExit();
                showSuccessMessage(
                    'Employee updated successfully.'
                );
            }
        }
    );

    const routeEmployeeId = Number.parseInt(
        String(routeInfo?.routeParams?.[0] || ''),
        10
    );
    const routeMode = String(
        routeInfo?.routeParams?.[1] || ''
    ).toLowerCase();

    return (async () => {
        if (
            Number.isInteger(routeEmployeeId) &&
            routeEmployeeId > 0
        ) {
            const opened = await openEmpDetail(routeEmployeeId);

            if (opened && routeMode === 'edit') {
                enterEmpEditMode();
            }
            return;
        }

        await loadEmployees();
    })();
}


const AddEmployeeValidation = {
    staticRequiredFields: [
        {
            selector: '#add-empFirstName',
            message: 'First Name is required.'
        },
        {
            selector: '#add-empLastName',
            message: 'Last Name is required.'
        },
        {
            selector: '#add-empGender',
            message: 'Gender is required.'
        },
        {
            selector: '#add-empDob',
            message: 'Date of Birth is required.'
        },
        {
            selector: '#add-empDepartment',
            message: 'Department is required.'
        },
        {
            selector: '#add-empDesignation',
            message: 'Designation is required.'
        },
        {
            selector: '#add-empCategory',
            message: 'Employee Category is required.'
        },
        {
            selector: '#add-empType',
            message: 'Employee Type is required.'
        },
        {
            selector: '#add-empMode',
            message: 'Employment Mode is required.'
        },
        {
            selector: '#add-empJoiningDate',
            message: 'Joining Date is required.'
        },
        {
            selector: '#add-empNationality',
            message: 'Nationality is required.'
        },
        {
            selector: '#add-empProfilePhoto',
            message: 'Passport-size photo is required.'
        },
        {
            selector: '#add-empPhone',
            message: 'Mobile Number is required.'
        }

    ],

    getValue: function(field) {
        if (!field) return '';

        if (field.type === 'file') {
            return field.files?.length
                ? field.files[0]
                : '';
        }

        if (field.type === 'checkbox') {
            return field.checked;
        }

        return String(field.value || '').trim();
    },

    getErrorContainer: function(field) {
        return field?.closest(
            '.form-group, .emp-child-field'
        ) || null;
    },

    setFieldError: function(field, message) {
        if (!field) return;

        const container =
            this.getErrorContainer(field);

        addCssClasses(field,
            'emp-input-invalid'
        );

        field.setAttribute(
            'aria-invalid',
            'true'
        );

        if (container) {
            addCssClasses(container,
                'emp-field-invalid'
            );

            container.dataset.error =
                message;
        }
    },

    clearFieldError: function(field) {
        if (!field) return;

        const container =
            this.getErrorContainer(field);

        removeCssClasses(field,
            'emp-input-invalid'
        );

        field.removeAttribute(
            'aria-invalid'
        );

        if (container) {
            removeCssClasses(container,
                'emp-field-invalid'
            );

            delete container.dataset.error;
        }
    },

    clearAll: function(viewContainer) {
        viewContainer
            .querySelectorAll(
                '.emp-input-invalid'
            )
            .forEach(field => {
                removeCssClasses(field,
                    'emp-input-invalid'
                );

                field.removeAttribute(
                    'aria-invalid'
                );
            });

        viewContainer
            .querySelectorAll(
                '.emp-field-invalid'
            )
            .forEach(container => {
                removeCssClasses(container,
                    'emp-field-invalid'
                );

                delete container.dataset.error;
            });

        const summary =
            viewContainer.querySelector(
                '#add-emp-validation-summary'
            );

        const summaryText =
            viewContainer.querySelector(
                '#add-emp-validation-summary-text'
            );

        if (summary) {
            addCssClasses(summary, 'hidden');
        }

        if (summaryText) {
            summaryText.textContent = '';
        }
    },

    addError: function(
        errors,
        field,
        message
    ) {
        if (!field) return;

        this.setFieldError(
            field,
            message
        );

        errors.push({
            field,
            message
        });
    },

    hasAnyRowValue: function(row) {
        return Array.from(
            row.querySelectorAll(
                'input, select, textarea'
            )
        ).some(field => {
            if (field.type === 'file') {
                return Boolean(
                    field.files?.length
                );
            }

            if (
                field.type === 'checkbox' ||
                field.type === 'radio'
            ) {
                return field.checked;
            }

            return Boolean(
                String(
                    field.value || ''
                ).trim()
            );
        });
    },

    validateStaticFields: function(
        viewContainer,
        errors
    ) {
        this.staticRequiredFields.forEach(
            rule => {
                const field =
                    viewContainer.querySelector(
                        rule.selector
                    );

                if (
                    !field ||
                    this.getValue(field)
                ) {
                    return;
                }

                this.addError(
                    errors,
                    field,
                    rule.message
                );
            }
        );
    },
    validateManagementCategory: function(
        viewContainer,
        errors
    ) {
        const categoryField =
            viewContainer.querySelector(
                '#add-empCategory'
            );

        const managementTypeField =
            viewContainer.querySelector(
                '#add-empManagementType'
            );

        if (
            this.getValue(categoryField) ===
            'MANAGEMENT' &&
            !this.getValue(managementTypeField)
        ) {
            this.addError(
                errors,
                managementTypeField,
                'Select Teaching or Non-Teaching Management.'
            );
        }
    },
    validateProfilePhoto: function(
        viewContainer,
        errors
    ) {
        const photoInput =
            viewContainer.querySelector(
                '#add-empProfilePhoto'
            );

        const photo =
            photoInput?.files?.[0];

        if (!photo) {
            return;
        }

        const allowedTypes =
            new Set([
                'image/jpeg',
                'image/png',
                'image/webp'
            ]);

        if (!allowedTypes.has(photo.type)) {
            this.addError(
                errors,
                photoInput,
                'Passport photo must be JPG, PNG or WEBP.'
            );

            return;
        }

        const maximumSize =
            2 * 1024 * 1024;

        if (photo.size > maximumSize) {
            this.addError(
                errors,
                photoInput,
                'Passport photo must not exceed 2 MB.'
            );
        }
    },
    validateDateOfBirth: function(
        viewContainer,
        errors
    ) {
        const field =
            viewContainer.querySelector(
                '#add-empDob'
            );

        const value =
            this.getValue(field);

        if (!value) return;

        const birthDate =
            new Date(`${value}T00:00:00`);

        if (
            Number.isNaN(
                birthDate.getTime()
            )
        ) {
            this.addError(
                errors,
                field,
                'Enter a valid Date of Birth.'
            );

            return;
        }

        const today = new Date();

        let age =
            today.getFullYear() -
            birthDate.getFullYear();

        const monthDifference =
            today.getMonth() -
            birthDate.getMonth();

        if (
            monthDifference < 0 ||
            (
                monthDifference === 0 &&
                today.getDate() <
                birthDate.getDate()
            )
        ) {
            age--;
        }

        if (age < 18) {
            this.addError(
                errors,
                field,
                'Employee must be at least 18 years old.'
            );
        }
    },

    validateLogin: function(
        viewContainer,
        errors
    ) {
        const generateLogin =
            viewContainer.querySelector(
                '#add-generateLogin'
            )?.checked ?? false;

        const sendEmail =
            viewContainer.querySelector(
                '#add-sendLoginEmail'
            )?.checked ?? false;

        if (!generateLogin) {
            if (sendEmail) {
                const sendEmailField =
                    viewContainer.querySelector(
                        '#add-sendLoginEmail'
                    );

                this.addError(
                    errors,
                    sendEmailField,
                    'Credential email requires login account creation.'
                );
            }

            return;
        }

        const roleField =
            viewContainer.querySelector(
                '#add-employeeRoleId'
            );

        if (!this.getValue(roleField)) {
            this.addError(
                errors,
                roleField,
                'Select the Employee login role.'
            );
        }

        const emailField =
            viewContainer.querySelector(
                '#add-empEmail'
            );

        const emailValue =
            this.getValue(emailField);

        if (!emailValue) {
            this.addError(
                errors,
                emailField,
                'Official Email is required when creating a login account.'
            );

            return;
        }

        if (
            emailField instanceof HTMLInputElement &&
            !emailField.checkValidity()
        ) {
            this.addError(
                errors,
                emailField,
                'Enter a valid Official Email address.'
            );
        }
    },

    validateEmploymentDates: function(
        viewContainer,
        errors
    ) {
        const dateOfBirthField =
            viewContainer.querySelector(
                '#add-empDob'
            );

        const joiningDateField =
            viewContainer.querySelector(
                '#add-empJoiningDate'
            );

        const probationDateField =
            viewContainer.querySelector(
                '#add-empProbationEndDate'
            );

        const confirmationDateField =
            viewContainer.querySelector(
                '#add-empConfirmationDate'
            );

        const dateOfBirth =
            this.getValue(dateOfBirthField);

        const joiningDate =
            this.getValue(joiningDateField);

        const probationDate =
            this.getValue(probationDateField);

        const confirmationDate =
            this.getValue(confirmationDateField);

        if (
            dateOfBirth &&
            joiningDate &&
            dateOfBirth >= joiningDate
        ) {
            this.addError(
                errors,
                joiningDateField,
                'Joining Date must be later than Date of Birth.'
            );
        }

        if (
            joiningDate &&
            probationDate &&
            probationDate < joiningDate
        ) {
            this.addError(
                errors,
                probationDateField,
                'Probation End Date cannot be earlier than Joining Date.'
            );
        }

        if (
            joiningDate &&
            confirmationDate &&
            confirmationDate < joiningDate
        ) {
            this.addError(
                errors,
                confirmationDateField,
                'Confirmation Date cannot be earlier than Joining Date.'
            );
        }
    },

    validateIdentificationDates: function(
        viewContainer,
        errors
    ) {
        const passportNumberField =
            viewContainer.querySelector(
                '#add-empPassportNo'
            );

        const passportExpiryField =
            viewContainer.querySelector(
                '#add-empPassportExpiry'
            );

        const workPermitNumberField =
            viewContainer.querySelector(
                '#add-empWorkPermit'
            );

        const workPermitExpiryField =
            viewContainer.querySelector(
                '#add-empWorkPermitExpiry'
            );

        if (
            this.getValue(passportExpiryField) &&
            !this.getValue(passportNumberField)
        ) {
            this.addError(
                errors,
                passportNumberField,
                'Passport Number is required when Passport Expiry Date is entered.'
            );
        }

        if (
            this.getValue(workPermitExpiryField) &&
            !this.getValue(workPermitNumberField)
        ) {
            this.addError(
                errors,
                workPermitNumberField,
                'Work Permit Number is required when its Expiry Date is entered.'
            );
        }
    },

    validateContacts: function(
        viewContainer,
        errors
    ) {
        const rows =
            Array.from(
                viewContainer.querySelectorAll(
                    '#contacts-container .contact-row'
                )
            );

        const populatedRows =
            rows.filter(row =>
                this.hasAnyRowValue(row)
            );

        if (populatedRows.length === 0) {
            return;
        }

        populatedRows.forEach((row, index) => {
            const name =
                row.querySelector('.c-name');

            const relationship =
                row.querySelector('.c-relation');

            const mobile =
                row.querySelector('.c-phone');

            if (!this.getValue(name)) {
                this.addError(
                    errors,
                    name,
                    `Contact ${index + 1}: Name is required.`
                );
            }

            if (!this.getValue(relationship)) {
                this.addError(
                    errors,
                    relationship,
                    `Contact ${index + 1}: Relationship is required.`
                );
            }

            if (!this.getValue(mobile)) {
                this.addError(
                    errors,
                    mobile,
                    `Contact ${index + 1}: Mobile Number is required.`
                );
            }
        });
    },

    validateQualifications: function(
        viewContainer,
        errors
    ) {
        const rows =
            viewContainer.querySelectorAll(
                '#qualifications-container .qual-row'
            );

        rows.forEach((row, index) => {
            if (!this.hasAnyRowValue(row)) {
                return;
            }

            const level =
                row.querySelector('.q-level');

            const name =
                row.querySelector('.q-name');

            const customLevel =
                row.querySelector(
                    '.q-custom-level'
                );

            const institution =
                row.querySelector(
                    '.q-institution'
                );

            const specializationField =
                row.querySelector(
                    '.q-specialization'
                );

            const startYear =
                row.querySelector(
                    '.q-start-year'
                );

            const completionYear =
                row.querySelector(
                    '.q-completion-year'
                );

            const selectedLevel =
                this.getValue(level);

            if (!selectedLevel) {
                this.addError(
                    errors,
                    level,
                    `Qualification ${index + 1}: Level is required.`
                );
            }

            if (!this.getValue(name)) {
                this.addError(
                    errors,
                    name,
                    `Qualification ${index + 1}: Qualification Name is required.`
                );
            }

            if (
                selectedLevel ===
                'SENIOR_SECONDARY' &&
                !this.getValue(
                    specializationField
                )
            ) {
                this.addError(
                    errors,
                    specializationField,
                    `Qualification ${index + 1}: Specialization or subject combination is required.`
                );
            }

            if (
                selectedLevel === 'OTHER' &&
                !this.getValue(customLevel)
            ) {
                this.addError(
                    errors,
                    customLevel,
                    `Qualification ${index + 1}: Enter the custom qualification level.`
                );
            }

            if (!this.getValue(institution)) {
                this.addError(
                    errors,
                    institution,
                    `Qualification ${index + 1}: Institution is required.`
                );
            }

            const startYearValue =
                this.getValue(startYear);

            const completionYearValue =
                this.getValue(completionYear);

            if (!startYearValue) {
                this.addError(
                    errors,
                    startYear,
                    `Qualification ${index + 1}: Start Year is required.`
                );
            }

            if (!completionYearValue) {
                this.addError(
                    errors,
                    completionYear,
                    `Qualification ${index + 1}: Completion Year is required.`
                );
            }

            if (
                startYearValue &&
                completionYearValue &&
                Number.parseInt(
                    completionYearValue,
                    10
                ) <
                Number.parseInt(
                    startYearValue,
                    10
                )
            ) {
                this.addError(
                    errors,
                    completionYear,
                    `Qualification ${index + 1}: Completion Year cannot be earlier than Start Year.`
                );
            }
        });
    },

    validateExperiences: function(
        viewContainer,
        errors
    ) {
        const rows =
            viewContainer.querySelectorAll(
                '#experiences-container .exp-row'
            );

        rows.forEach((row, index) => {
            if (!this.hasAnyRowValue(row)) {
                return;
            }

            const company =
                row.querySelector('.e-company');

            const type =
                row.querySelector('.e-type');

            const start =
                row.querySelector('.e-start');

            const end =
                row.querySelector('.e-end');

            if (!this.getValue(company)) {
                this.addError(
                    errors,
                    company,
                    `Experience ${index + 1}: Organisation is required.`
                );
            }

            if (!this.getValue(type)) {
                this.addError(
                    errors,
                    type,
                    `Experience ${index + 1}: Employment Type is required.`
                );
            }

            if (!this.getValue(start)) {
                this.addError(
                    errors,
                    start,
                    `Experience ${index + 1}: Start Date is required.`
                );
            }

            if (
                this.getValue(start) &&
                this.getValue(end) &&
                this.getValue(end) <
                this.getValue(start)
            ) {
                this.addError(
                    errors,
                    end,
                    `Experience ${index + 1}: End Date cannot be before Start Date.`
                );
            }
        });
    },

    showSummary: function(
        viewContainer,
        errors
    ) {
        const summary =
            viewContainer.querySelector(
                '#add-emp-validation-summary'
            );

        const summaryText =
            viewContainer.querySelector(
                '#add-emp-validation-summary-text'
            );

        if (!summary || !summaryText) {
            return;
        }

        const uniqueMessages =
            [
                ...new Set(
                    errors.map(
                        error => error.message
                    )
                )
            ];

        summaryText.textContent =
            uniqueMessages.join(' • ');

        removeCssClasses(summary,
            'hidden'
        );
    },

    validate: function(viewContainer) {
        this.clearAll(viewContainer);

        const errors = [];

        this.validateStaticFields(
            viewContainer,
            errors
        );
        this.validateManagementCategory(
            viewContainer,
            errors
        );

        this.validateProfilePhoto(
            viewContainer,
            errors
        );
        this.validateDateOfBirth(
            viewContainer,
            errors
        );

        this.validateEmploymentDates(
            viewContainer,
            errors
        );

        this.validateIdentificationDates(
            viewContainer,
            errors
        );

        this.validateLogin(
            viewContainer,
            errors
        );

        this.validateContacts(
            viewContainer,
            errors
        );

        this.validateQualifications(
            viewContainer,
            errors
        );

        this.validateExperiences(
            viewContainer,
            errors
        );

        if (errors.length === 0) {
            return true;
        }

        this.showSummary(
            viewContainer,
            errors
        );

        const firstField =
            errors[0]?.field;

        if (firstField instanceof HTMLElement) {
            firstField.scrollIntoView({
                behavior: 'smooth',
                block: 'center'
            });

            window.setTimeout(
                () => firstField.focus(),
                250
            );
        }

        return false;
    },

    bindLiveClearing: function(
        viewContainer
    ) {
        const clearHandler = event => {
            const field = event.target;

            if (
                !field.matches(
                    'input, select, textarea'
                )
            ) {
                return;
            }

            this.clearFieldError(field);

            const summary =
                viewContainer.querySelector(
                    '#add-emp-validation-summary'
                );

            if (summary) {
                addCssClasses(summary,
                    'hidden'
                );
            }
        };

        viewContainer.addEventListener(
            'input',
            clearHandler
        );

        viewContainer.addEventListener(
            'change',
            clearHandler
        );
    }
};


/**
 * @param {HTMLElement} viewContainer
 * @returns {*}
 */
function createEmployeeRegistrationProgressController(
    viewContainer
) {
    const modal =
        /** @type {(HTMLElement|null)} */ (
        viewContainer.querySelector(
            '#employee-registration-modal'
        )
    );

    if (!modal) {
        return null;
    }

    /*
     * Render the registration dialog directly under <body>.
     * The Add Employee view uses animated/transformed containers,
     * which can make a fixed modal center against the full form
     * instead of the visible browser viewport.
     */
    document
        .querySelectorAll(
            '#employee-registration-modal'
        )
        .forEach(existingModal => {
            if (existingModal !== modal) {
                removeDomElement(existingModal);
            }
        });

    if (modal.parentElement !== document.body) {
        document.body.appendChild(modal);
    }

    const processingView =
        modal.querySelector(
            '#employee-registration-processing-view'
        );

    const successView =
        modal.querySelector(
            '#employee-registration-success-view'
        );

    const failureView =
        modal.querySelector(
            '#employee-registration-failure-view'
        );

    const title =
        modal.querySelector(
            '#employee-registration-title'
        );

    const employeeName =
        modal.querySelector(
            '#employee-registration-employee-name'
        );

    const statusIcon =
        modal.querySelector(
            '#employee-registration-status-icon'
        );

    const closeButton =
        modal.querySelector(
            '#employee-registration-close-btn'
        );

    const percentageText =
        modal.querySelector(
            '#employee-registration-percentage'
        );

    const progressBar =
        modal.querySelector(
            '#employee-registration-progress'
        );

    const progressFill =
        modal.querySelector(
            '#employee-registration-progress-fill'
        );

    const stageTitle =
        modal.querySelector(
            '#employee-registration-stage-title'
        );

    const message =
        modal.querySelector(
            '#employee-registration-message'
        );

    const itemProgress =
        modal.querySelector(
            '#employee-registration-item-progress'
        );

    const itemMessage =
        modal.querySelector(
            '#employee-registration-item-message'
        );

    const completedItems =
        modal.querySelector(
            '#employee-registration-completed-items'
        );

    const totalItems =
        modal.querySelector(
            '#employee-registration-total-items'
        );

    const operationWrapper =
        modal.querySelector(
            '#employee-registration-operation-wrapper'
        );

    const operationIdElement =
        modal.querySelector(
            '#employee-registration-operation-id'
        );

    const steps = Array.from(
        modal.querySelectorAll(
            '.emp-registration-step'
        )
    );

    const stageOrder = [
        'REQUEST_ACCEPTED',
        'SERVER_VALIDATION',
        'EMPLOYEE_CREATION',
        'RELATED_RECORDS',
        'REPORTING_MANAGER',
        'LOGIN_ACCOUNT',
        'FINALIZATION'
    ];

    /**
     * @type {{
     *     processing: boolean,
     *     operationId: (string|null),
     *     employeeId: (number|string|null),
     *     previousBodyOverflow: string
     * }}
     */
    const state = {
        processing: false,
        operationId: null,
        employeeId: null,
        previousBodyOverflow: ''
    };

    const clampPercentage = value => {
        const parsed =
            Number.parseInt(value, 10);

        if (!Number.isFinite(parsed)) {
            return 0;
        }

        return Math.min(
            100,
            Math.max(0, parsed)
        );
    };

    const formatStage = value => {
        if (!value) {
            return 'Processing registration';
        }

        return String(value)
            .replace(/_/g, ' ')
            .toLowerCase()
            .replace(
                /\b\w/g,
                character =>
                    character.toUpperCase()
            );
    };

    const setStepState = (
        step,
        stepState
    ) => {
        if (!step) return;

        removeCssClasses(step,
            'is-waiting',
            'is-processing',
            'is-completed',
            'is-failed'
        );

        addCssClasses(step,
            `is-${stepState}`
        );

        const icon =
            step.querySelector('i');

        if (!icon) return;

        icon.className =
            stepState === 'completed'
                ? 'bi bi-check-lg'
                : stepState === 'processing'
                    ? 'bi bi-arrow-repeat'
                    : stepState === 'failed'
                        ? 'bi bi-x-lg'
                        : 'bi bi-circle';
    };

    const setMainIcon = iconState => {
        if (!statusIcon) return;

        removeCssClasses(statusIcon,
            'is-processing',
            'is-success',
            'is-failed'
        );

        addCssClasses(statusIcon,
            `is-${iconState}`
        );

        const icon =
            statusIcon.querySelector('i');

        if (!icon) return;

        icon.className =
            iconState === 'success'
                ? 'bi bi-check-circle-fill'
                : iconState === 'failed'
                    ? 'bi bi-x-circle-fill'
                    : 'bi bi-person-plus-fill';
    };

    const setPercentage = value => {
        const percentage =
            clampPercentage(value);

        if (percentageText) {
            percentageText.textContent =
                `${percentage}%`;
        }

        if (progressBar) {
            progressBar.setAttribute(
                'aria-valuenow',
                String(percentage)
            );
        }

        if (progressFill) {
            progressFill.style.width =
                `${percentage}%`;
        }

        return percentage;
    };

    const updateSteps = (
        currentStage,
        registrationStatus
    ) => {
        const normalizedStage =
            String(
                currentStage ||
                'REQUEST_ACCEPTED'
            ).toUpperCase();

        const currentIndex =
            stageOrder.indexOf(
                normalizedStage
            );

        steps.forEach(step => {
            const stepStage =
                step.dataset.stage;

            const stepIndex =
                stageOrder.indexOf(
                    stepStage
                );

            if (
                registrationStatus ===
                'COMPLETED'
            ) {
                setStepState(
                    step,
                    'completed'
                );
                return;
            }

            if (
                registrationStatus ===
                'FAILED'
            ) {
                if (
                    stepStage ===
                    normalizedStage
                ) {
                    setStepState(
                        step,
                        'failed'
                    );
                } else if (
                    currentIndex >= 0 &&
                    stepIndex < currentIndex
                ) {
                    setStepState(
                        step,
                        'completed'
                    );
                } else {
                    setStepState(
                        step,
                        'waiting'
                    );
                }

                return;
            }

            if (
                currentIndex < 0
            ) {
                setStepState(
                    step,
                    'waiting'
                );
            } else if (
                stepIndex < currentIndex
            ) {
                setStepState(
                    step,
                    'completed'
                );
            } else if (
                stepIndex === currentIndex
            ) {
                setStepState(
                    step,
                    'processing'
                );
            } else {
                setStepState(
                    step,
                    'waiting'
                );
            }
        });
    };

    const showOnlyView = view => {
        [
            processingView,
            successView,
            failureView
        ].forEach(element => {
            addCssClasses(element,
                'hidden'
            );
        });

        removeCssClasses(view,
            'hidden'
        );
    };

    const setOperationId = value => {
        state.operationId =
            value || null;

        if (
            operationIdElement &&
            state.operationId
        ) {
            operationIdElement.textContent =
                state.operationId;

            removeCssClasses(operationWrapper,
                'hidden'
            );
        } else {
            operationIdElement &&
            (
                operationIdElement.textContent =
                    ''
            );

            addCssClasses(operationWrapper,
                'hidden'
            );
        }
    };

    const reset = displayName => {
        state.processing = false;
        state.operationId = null;
        state.employeeId = null;

        showOnlyView(
            processingView
        );

        if (title) {
            title.textContent =
                'Registering Employee';
        }

        if (employeeName) {
            employeeName.textContent =
                displayName ||
                'Preparing employee information';
        }

        setMainIcon('processing');
        setPercentage(0);

        removeCssClasses(progressFill,
            'is-success'
        );
        removeCssClasses(progressFill,
            'is-failed'
        );

        updateSteps(
            'REQUEST_ACCEPTED',
            'PROCESSING'
        );

        if (stageTitle) {
            stageTitle.textContent =
                'Preparing registration';
        }

        if (message) {
            message.textContent =
                'Preparing employee information for registration.';
        }

        addCssClasses(itemProgress,
            'hidden'
        );

        if (itemMessage) {
            itemMessage.textContent =
                'Processing records';
        }

        if (completedItems) {
            completedItems.textContent =
                '0';
        }

        if (totalItems) {
            totalItems.textContent =
                '0';
        }

        setOperationId(null);

        addCssClasses(closeButton,
            'hidden'
        );

        const errorList =
            modal.querySelector(
                '#employee-registration-error-list'
            );

        if (errorList) {
            errorList.innerHTML = '';
        }
    };

    const open = displayName => {
        const wasHidden =
            modal.classList.contains('hidden');

        reset(displayName);

        state.processing = true;

        if (wasHidden) {
            state.previousBodyOverflow =
                document.body.style.overflow;
        }

        document.body.style.overflow =
            'hidden';

        removeCssClasses(modal,
            'hidden'
        );
    };

    const close = (
        force = false
    ) => {
        if (
            state.processing &&
            !force
        ) {
            return false;
        }

        state.processing = false;

        addCssClasses(modal,
            'hidden'
        );

        document.body.style.overflow =
            state.previousBodyOverflow;

        return true;
    };

    /** @param {EmployeeRegistrationResult} progress */
    const update = progress => {
        const registrationStatus =
            String(
                progress?.status ||
                'PROCESSING'
            ).toUpperCase();

        const currentStage =
            String(
                progress?.stage ||
                'REQUEST_ACCEPTED'
            ).toUpperCase();

        const percentage =
            setPercentage(
                progress?.percentage ?? 0
            );

        updateSteps(
            currentStage,
            registrationStatus
        );

        setOperationId(
            progress?.operationId ||
            state.operationId
        );

        if (stageTitle) {
            stageTitle.textContent =
                progress?.stageTitle ||
                formatStage(
                    currentStage
                );
        }

        if (message) {
            message.textContent =
                progress?.message ||
                'Employee registration is in progress.';
        }

        const total =
            Number.parseInt(
                String(
                    progress?.totalItems ?? ''
                ),
                10
            );

        const completed =
            Number.parseInt(
                String(
                    progress?.completedItems ?? ''
                ),
                10
            );

        if (
            Number.isInteger(total) &&
            total > 0
        ) {
            removeCssClasses(itemProgress,
                'hidden'
            );

            if (itemMessage) {
                itemMessage.textContent =
                    progress?.itemMessage ||
                    progress?.message ||
                    'Processing employee records';
            }

            if (completedItems) {
                completedItems.textContent =
                    String(
                        Number.isInteger(completed)
                            ? completed
                            : 0
                    );
            }

            if (totalItems) {
                totalItems.textContent =
                    String(total);
            }
        } else {
            addCssClasses(itemProgress,
                'hidden'
            );
        }

        return percentage;
    };

    /**
     * @param {string} selector
     * @param {*} value
     * @param {string} [fallback]
     * @returns {void}
     */
    const setResultValue = (
        selector,
        value,
        fallback = '-'
    ) => {
        const element =
            modal.querySelector(selector);

        if (element) {
            element.textContent =
                value
                    ? String(value)
                    : fallback;
        }
    };

    /** @param {EmployeeRegistrationResult} result */
    const showSuccess = result => {
        state.processing = false;
        state.employeeId =
            result?.employeeId || null;

        update({
            ...result,
            status: 'COMPLETED',
            stage: 'FINALIZATION',
            percentage: 100
        });

        showOnlyView(
            successView
        );

        setMainIcon('success');

        removeCssClasses(progressFill,
            'is-failed'
        );

        addCssClasses(progressFill,
            'is-success'
        );

        if (title) {
            title.textContent =
                'Employee Registered';
        }

        if (employeeName) {
            employeeName.textContent =
                result?.fullName ||
                'Registration completed successfully';
        }

        setResultValue(
            '#employee-registration-result-name',
            result?.fullName
        );

        setResultValue(
            '#employee-registration-result-number',
            result?.employeeNo
        );

        setResultValue(
            '#employee-registration-result-department',
            result?.departmentName
        );

        setResultValue(
            '#employee-registration-result-designation',
            result?.designationName
        );

        setResultValue(
            '#employee-registration-result-manager',
            result?.reportingManagerName,
            'Not assigned'
        );

        const loginStatus =
            result?.loginAccountStatus ||
            (
                result?.loginCreated === true
                    ? 'Created'
                    : result?.loginCreated === false
                        ? 'Not requested'
                        : null
            );

        setResultValue(
            '#employee-registration-result-login',
            loginStatus,
            'Not requested'
        );

        const successMessage =
            modal.querySelector(
                '#employee-registration-success-message'
            );

        if (successMessage) {
            successMessage.textContent =
                result?.message ||
                'Employee registration has been completed.';
        }

        removeCssClasses(closeButton,
            'hidden'
        );
    };

    const normalizeErrors = progress => {
        if (
            Array.isArray(progress?.errors) &&
            progress.errors.length > 0
        ) {
            return progress.errors.map(
                error =>
                    typeof error === 'string'
                        ? error
                        : (
                            error?.message ||
                            JSON.stringify(error)
                        )
            );
        }

        if (
            progress?.error &&
            typeof progress.error === 'string'
        ) {
            return [
                progress.error
            ];
        }

        return [
            progress?.message ||
            'An unexpected registration error occurred.'
        ];
    };

    /** @param {EmployeeRegistrationResult} progress */
    const showFailure = progress => {
        state.processing = false;

        const currentStage =
            progress?.stage ||
            'REQUEST_ACCEPTED';

        const percentage =
            clampPercentage(
                progress?.percentage ?? 0
            );

        update({
            ...progress,
            status: 'FAILED',
            stage: currentStage,
            percentage
        });

        showOnlyView(
            failureView
        );

        setMainIcon('failed');

        removeCssClasses(progressFill,
            'is-success'
        );

        addCssClasses(progressFill,
            'is-failed'
        );

        if (title) {
            title.textContent =
                'Registration Failed';
        }

        if (employeeName) {
            employeeName.textContent =
                'The employee form has been preserved';
        }

        setResultValue(
            '#employee-registration-failed-stage',
            progress?.stageTitle ||
            formatStage(currentStage)
        );

        setResultValue(
            '#employee-registration-failed-percentage',
            `${percentage}%`
        );

        const failureMessage =
            modal.querySelector(
                '#employee-registration-failure-message'
            );

        if (failureMessage) {
            failureMessage.textContent =
                progress?.message ||
                'Employee registration could not be completed.';
        }

        const errorList =
            modal.querySelector(
                '#employee-registration-error-list'
            );

        if (errorList) {
            errorList.innerHTML = '';

            normalizeErrors(
                progress
            ).forEach(errorMessage => {
                const item =
                    document.createElement('li');

                item.textContent =
                    errorMessage;

                errorList.appendChild(
                    item
                );
            });
        }

        removeCssClasses(closeButton,
            'hidden'
        );
    };

    closeButton?.addEventListener(
        'click',
        () => close()
    );

    return {
        modal,
        open,
        close,
        reset,
        update,
        showSuccess,
        showFailure,
        setOperationId,
        getEmployeeId: () =>
            state.employeeId
    };
}

function initAddEmployeeView() {
    const viewContainer =
        /** @type {(HTMLElement|null)} */ (
        document.querySelector('#ba-add-employee-view')
    );

    if (!viewContainer) return;

    const oldForm =
        viewContainer.querySelector('#add-emp-form');

    if (!oldForm) return;

    const form = oldForm.cloneNode(true);
    oldForm.parentNode.replaceChild(form, oldForm);
    form.reset();

    AddEmployeeValidation.bindLiveClearing(
        viewContainer
    );

    const valueOrNull = selector => {
        const element =
            viewContainer.querySelector(selector);

        if (!element) return null;

        const value =
            String(element.value ?? '').trim();

        return value === '' ? null : value;
    };

    const getResponseContent = response =>
        response?.data?.content ??
        response?.data ??
        [];

    async function loadAddSelectOptions() {
        try {
            const branchId = getRequiredBranchId();

            const [
                departmentResponse,
                designationResponse,
                reportingManagerResponse,
                loginRoleResponse
            ] = await Promise.all([
                apiGet(
                    `/departments?branchId=${branchId}&size=100`
                ),
                apiGet(
                    `/designations?branchId=${branchId}&size=100`
                ),
                apiGet(
                    '/branchadmin/employees/reporting-managers'
                ),
                apiGet(
                    '/branchadmin/employees/login-role-options'
                )
            ]);

            const departmentSelect =
                viewContainer.querySelector(
                    '#add-empDepartment'
                );
            const designationSelect =
                viewContainer.querySelector(
                    '#add-empDesignation'
                );
            const reportingManagerSelect =
                viewContainer.querySelector(
                    '#add-empReportingManager'
                );
            const loginRoleSelect =
                viewContainer.querySelector(
                    '#add-employeeRoleId'
                );

            if (departmentSelect) {
                departmentSelect.innerHTML =
                    '<option value="">-- Select Department --</option>';

                getResponseContent(
                    departmentResponse
                ).forEach(department => {
                    const option =
                        document.createElement('option');
                    option.value =
                        String(
                            department.departmentId ??
                            department.id ??
                            ''
                        );
                    option.textContent =
                        department.departmentName ??
                        department.name ??
                        'Unnamed Department';
                    departmentSelect.appendChild(option);
                });
            }

            if (designationSelect) {
                designationSelect.innerHTML =
                    '<option value="">-- Select Designation --</option>';

                getResponseContent(
                    designationResponse
                ).forEach(designation => {
                    const option =
                        document.createElement('option');
                    option.value =
                        String(
                            designation.designationId ??
                            designation.id ??
                            ''
                        );
                    option.textContent =
                        designation.designationName ??
                        designation.name ??
                        'Unnamed Designation';
                    designationSelect.appendChild(option);
                });
            }

            if (reportingManagerSelect) {
                reportingManagerSelect.innerHTML =
                    '<option value="">-- No Reporting Manager --</option>';
                const managers =
                    reportingManagerResponse?.data ??
                    reportingManagerResponse ??
                    [];

                managers.forEach(manager => {
                    const option =
                        document.createElement('option');
                    option.value =
                        String(
                            manager.employeeId ?? ''
                        );
                    option.textContent = [
                        manager.employeeNo,
                        manager.fullName,
                        manager.designationName
                    ].filter(Boolean).join(' - ');
                    reportingManagerSelect.appendChild(option);
                });
            }

            if (loginRoleSelect) {
                loginRoleSelect.innerHTML =
                    '<option value="">-- Select Role --</option>';
                const roles =
                    loginRoleResponse?.data ??
                    loginRoleResponse ??
                    [];

                roles.forEach(role => {
                    const option =
                        document.createElement('option');
                    option.value =
                        String(
                            role.roleId ?? ''
                        );
                    option.textContent =
                        role.roleName || role.roleCode || 'Role';
                    loginRoleSelect.appendChild(option);
                });
            }
        } catch (error) {
            console.warn(
                'Could not load Employee form options.',
                error
            );
            showErrorMessage(
                'Could not load Department, Designation, Reporting Manager and Login Role options.'
            );
        }
    }

    const categorySelect =
        viewContainer.querySelector(
            '#add-empCategory'
        );

    const managementTypeGroup =
        viewContainer.querySelector(
            '#add-empManagementTypeGroup'
        );

    const managementTypeSelect =
        viewContainer.querySelector(
            '#add-empManagementType'
        );

    const synchronizeManagementCategory = () => {
        const isManagement =
            categorySelect?.value === 'MANAGEMENT';

        toggleCssClass(managementTypeGroup,
            'hidden',
            !isManagement
        );

        if (managementTypeSelect) {
            managementTypeSelect.required = isManagement;
            managementTypeSelect.setAttribute(
                'aria-required',
                String(isManagement)
            );

            if (!isManagement) {
                managementTypeSelect.value = '';
            }
        }
    };

    categorySelect?.addEventListener(
        'change',
        synchronizeManagementCategory
    );
    synchronizeManagementCategory();

    const profilePhotoInput =
        /** @type {(HTMLInputElement|null)} */ (
        viewContainer.querySelector(
            '#add-empProfilePhoto'
        )
    );

    const profilePhotoPreview =
        /** @type {(HTMLImageElement|null)} */ (
        viewContainer.querySelector(
            '#add-empProfilePhotoPreview'
        )
    );

    const profilePhotoPlaceholder =
        viewContainer.querySelector(
            '#add-empProfilePhotoPlaceholder'
        );

    const profilePhotoName =
        viewContainer.querySelector(
            '#add-empProfilePhotoName'
        );

    const resetProfilePhotoPreview = () => {
        profilePhotoPreview?.removeAttribute('src');
        addCssClasses(profilePhotoPreview, 'hidden');

        removeCssClasses(profilePhotoPlaceholder,
            'hidden'
        );

        if (profilePhotoName) {
            profilePhotoName.textContent =
                'No file chosen';
        }
    };

    profilePhotoInput?.addEventListener(
        'change',
        () => {
            const photo =
                profilePhotoInput.files?.[0];

            if (!photo) {
                resetProfilePhotoPreview();
                return;
            }

            const allowedTypes = new Set([
                'image/jpeg',
                'image/png',
                'image/webp'
            ]);

            if (!allowedTypes.has(photo.type)) {
                profilePhotoInput.value = '';
                resetProfilePhotoPreview();

                showErrorMessage(
                    'Select a JPG, PNG or WEBP photo.'
                );
                return;
            }

            if (photo.size > 2 * 1024 * 1024) {
                profilePhotoInput.value = '';
                resetProfilePhotoPreview();

                showErrorMessage(
                    'Passport photo must not exceed 2 MB.'
                );
                return;
            }

            const reader = new FileReader();

            reader.addEventListener(
                'load',
                () => {
                    if (profilePhotoPreview) {
                        profilePhotoPreview.src =
                            String(reader.result);

                        removeCssClasses(profilePhotoPreview,
                            'hidden'
                        );
                    }

                    addCssClasses(profilePhotoPlaceholder,
                        'hidden'
                    );

                    if (profilePhotoName) {
                        profilePhotoName.textContent =
                            photo.name;
                    }

                    AddEmployeeValidation.clearFieldError(
                        profilePhotoInput
                    );
                }
            );

            reader.addEventListener(
                'error',
                () => {
                    profilePhotoInput.value = '';
                    resetProfilePhotoPreview();

                    showErrorMessage(
                        'The selected photo could not be previewed.'
                    );
                }
            );

            reader.readAsDataURL(photo);
        }
    );

    const generateLoginCheckbox =
        viewContainer.querySelector(
            '#add-generateLogin'
        );

    const loginOptions =
        viewContainer.querySelector(
            '#add-loginOptions'
        );

    const sendLoginEmailCheckbox =
        viewContainer.querySelector(
            '#add-sendLoginEmail'
        );

    const officialEmailInput =
        viewContainer.querySelector(
            '#add-empEmail'
        );

    generateLoginCheckbox?.addEventListener(
        'change',
        () => {
            const enabled =
                generateLoginCheckbox.checked;

            toggleCssClass(loginOptions,
                'hidden',
                !enabled
            );

            if (!enabled && sendLoginEmailCheckbox) {
                sendLoginEmailCheckbox.checked = false;
            }
        }
    );

    sendLoginEmailCheckbox?.addEventListener(
        'change',
        () => {
            if (
                sendLoginEmailCheckbox.checked &&
                !officialEmailInput?.value.trim()
            ) {
                sendLoginEmailCheckbox.checked = false;
                showErrorMessage(
                    'Enter the official email before enabling credential email.'
                );
                officialEmailInput?.focus();
            }
        }
    );

    viewContainer
        .querySelectorAll('.detail-text')
        .forEach(element => {
            addCssClasses(element, 'hidden');
        });

    viewContainer
        .querySelectorAll('.detail-input')
        .forEach(element => {
            removeCssClasses(element, 'hidden');
        });

    const dynamicSections = [
        {
            section: '#contacts-section',
            container: '#contacts-container',
            button: '+ Add Contact',
            fields: EmpCollections.contactFields,
            rowClass: 'contact-row'
        },
        {
            section: '#qualifications-section',
            container: '#qualifications-container',
            button: '+ Add Qualification',
            fields: EmpCollections.qualFields,
            rowClass: 'qual-row'
        },
        {
            section: '#experiences-section',
            container: '#experiences-container',
            button: '+ Add Experience',
            fields: EmpCollections.expFields,
            rowClass: 'exp-row'
        }
    ];

    dynamicSections.forEach(definition => {
        const container =
            viewContainer.querySelector(
                definition.container
            );

        if (container) {
            container.innerHTML = '';
        }

        EmpCollections.initSection(
            viewContainer,
            definition.section,
            definition.container,
            definition.button,
            definition.fields,
            definition.rowClass,
            true
        );

    });

    if (typeof createErpCalendar === 'function') {
        const today = new Date();
        const maxDobDate = new Date(
            today.getFullYear() - 18,
            today.getMonth(),
            today.getDate()
        );

        const maxDobDateString =
            maxDobDate.toISOString().split('T')[0];

        createErpCalendar('#add-empDob', {
            maxDate: maxDobDateString
        });
        createErpCalendar('#add-empJoiningDate');
        createErpCalendar('#add-empProbationEndDate');
        createErpCalendar('#add-empConfirmationDate');
        createErpCalendar('#add-empPassportExpiry');
        createErpCalendar('#add-empWorkPermitExpiry');
    }

    const setTodayAsJoiningDate = () => {
        const todayString =
            new Date().toISOString().split('T')[0];

        const joiningDateInput =
            viewContainer.querySelector(
                '#add-empJoiningDate'
            );

        const calendar =
            getCalendarAdapter(
                joiningDateInput
            );

        if (calendar) {
            calendar.setDate(todayString);
        } else if (
            joiningDateInput instanceof HTMLInputElement
        ) {
            joiningDateInput.value = todayString;
        }
    };

    setTodayAsJoiningDate();
    void loadAddSelectOptions();

    const resetAddEmployeeForm = () => {
        form.reset();
        AddEmployeeValidation.clearAll(viewContainer);
        resetProfilePhotoPreview();
        synchronizeManagementCategory();
        addCssClasses(loginOptions, 'hidden');

        dynamicSections.forEach(definition => {
            const container =
                viewContainer.querySelector(
                    definition.container
                );

            if (!container) {
                return;
            }

            container.innerHTML = '';

        });

        setTodayAsJoiningDate();
    };

    viewContainer
        .querySelector('#backToEmployeesBtn')
        ?.addEventListener('click', () => {
            void window.erpNavigate({
                role: 'admin',
                view: 'employees',
                title: 'Manage Employees'
            });
        });

    viewContainer
        .querySelector('#btn-import-employee')
        ?.addEventListener('click', () => {
            if (typeof AppImporter === 'undefined') {
                showErrorMessage(
                    'Importer module not found.'
                );
                return;
            }

            AppImporter.open(
                'employee',
                'Import Employees',
                'Upload the Excel file containing Employee records.',
                () => {
                    void window.erpNavigate({
                        role: 'admin',
                        view: 'employees',
                        title: 'Manage Employees'
                    });
                }
            );
        });

    const registrationProgress =
        createEmployeeRegistrationProgressController(
            viewContainer
        );

    let latestRegistrationPayload = null;
    let latestRegistrationName = null;
    let registrationRunning = false;

    const getEmployeeFormName = () => {
        return [
            valueOrNull('#add-empTitle'),
            valueOrNull('#add-empFirstName'),
            valueOrNull('#add-empMiddleName'),
            valueOrNull('#add-empLastName')
        ]
            .filter(Boolean)
            .join(' ');
    };

    const extractApiFailure = (
        error,
        fallbackStage = 'REQUEST_ACCEPTED',
        fallbackPercentage = 0
    ) => {
        const responseData =
            error?.response?.data ||
            error?.data ||
            null;

        const statusCode =
            error?.response?.status ??
            error?.status ??
            responseData?.status ??
            null;

        const isMissingRegistrationEndpoint =
            Number(statusCode) === 404 ||
            /HTTP\s+Error:\s*404/i.test(
                String(error?.message || '')
            );

        if (isMissingRegistrationEndpoint) {
            return {
                status: 'FAILED',
                stage: fallbackStage,
                stageTitle: 'Backend Endpoint',
                percentage: fallbackPercentage,
                message:
                    'Employee registration backend is not available yet.',
                errors: [
                    'Create POST /api/branchadmin/employees/registrations before testing employee registration.'
                ]
            };
        }

        const errors =
            responseData?.errors ||
            responseData?.fieldErrors ||
            (
                responseData?.message
                    ? [responseData.message]
                    : (
                        error?.message
                            ? [error.message]
                            : [
                                'Employee registration failed.'
                            ]
                    )
            );

        return {
            status: 'FAILED',
            stage:
                responseData?.stage ||
                fallbackStage,
            stageTitle:
                responseData?.stageTitle ||
                null,
            percentage:
                responseData?.percentage ??
                fallbackPercentage,
            message:
                responseData?.message ||
                error?.message ||
                'Employee registration failed.',
            errors:
                Array.isArray(errors)
                    ? errors
                    : [String(errors)]
        };
    };

    const buildEmployeePayload =
        async () => {
            const employeeCategory =
                resolveEmployeeCategory(
                    viewContainer,
                    'add'
                );

            if (!employeeCategory) {
                showErrorMessage(
                    'Select a valid Employee Category.'
                );

                return null;
            }

            const profilePhoto =
                profilePhotoInput
                    ?.files?.[0] || null;

            const generateLogin =
                generateLoginCheckbox
                    ?.checked ?? false;

            const sendLoginEmail =
                sendLoginEmailCheckbox
                    ?.checked ?? false;

            return {
                title:
                    valueOrNull('#add-empTitle'),

                firstName:
                    valueOrNull(
                        '#add-empFirstName'
                    ),

                middleName:
                    valueOrNull(
                        '#add-empMiddleName'
                    ),

                lastName:
                    valueOrNull(
                        '#add-empLastName'
                    ),

                gender:
                    valueOrNull('#add-empGender'),

                dateOfBirth:
                    valueOrNull('#add-empDob'),

                maritalStatus:
                    valueOrNull(
                        '#add-empMaritalStatus'
                    ),

                bloodGroup:
                    valueOrNull(
                        '#add-empBloodGroup'
                    ),

                religion:
                    valueOrNull('#add-empReligion'),

                subReligion:
                    valueOrNull(
                        '#add-empSubReligion'
                    ),

                profilePhotoData:
                    await EmpCollections.fileToBase64(
                        profilePhoto
                    ),

                profilePhotoFileName:
                    profilePhoto?.name || null,

                profilePhotoContentType:
                    profilePhoto?.type || null,

                profilePhotoFileSize:
                    profilePhoto?.size || null,

                signatureData: null,
                signatureFileName: null,
                signatureContentType: null,
                signatureFileSize: null,

                officialEmail:
                    valueOrNull('#add-empEmail'),

                personalEmail:
                    valueOrNull(
                        '#add-empPersonalEmail'
                    ),

                mobileNo:
                    valueOrNull('#add-empPhone'),

                alternateMobile:
                    valueOrNull(
                        '#add-empAlternatePhone'
                    ),

                departmentId:
                    Number(
                        valueOrNull(
                            '#add-empDepartment'
                        )
                    ),

                designationId:
                    Number(
                        valueOrNull(
                            '#add-empDesignation'
                        )
                    ),

                reportingManagerId:
                    valueOrNull(
                        '#add-empReportingManager'
                    )
                        ? Number(
                            valueOrNull(
                                '#add-empReportingManager'
                            )
                        )
                        : null,

                employeeCategory,

                employeeType:
                    valueOrNull('#add-empType'),

                employmentMode:
                    valueOrNull('#add-empMode'),

                employmentStatus:
                    valueOrNull('#add-empStatus') ||
                    'ACTIVE',

                joiningDate:
                    valueOrNull(
                        '#add-empJoiningDate'
                    ),

                probationEndDate:
                    valueOrNull(
                        '#add-empProbationEndDate'
                    ),

                confirmationDate:
                    valueOrNull(
                        '#add-empConfirmationDate'
                    ),

                retirementDate: null,

                nationality:
                    valueOrNull(
                        '#add-empNationality'
                    ),

                nationalId:
                    valueOrNull(
                        '#add-empNationalId'
                    ),

                tinNumber:
                    valueOrNull('#add-empTin'),

                passportNo:
                    valueOrNull(
                        '#add-empPassportNo'
                    ),

                passportExpiryDate:
                    valueOrNull(
                        '#add-empPassportExpiry'
                    ),

                workPermitNumber:
                    valueOrNull(
                        '#add-empWorkPermit'
                    ),

                workPermitExpiryDate:
                    valueOrNull(
                        '#add-empWorkPermitExpiry'
                    ),

                addressCountry:
                    valueOrNull(
                        '#add-empCountry'
                    ),

                addressState:
                    valueOrNull(
                        '#add-empState'
                    ),

                addressDistrict:
                    valueOrNull(
                        '#add-empDistrict'
                    ),

                addressCounty:
                    valueOrNull(
                        '#add-empCounty'
                    ),

                addressSubCounty:
                    valueOrNull(
                        '#add-empSubCounty'
                    ),

                addressParish:
                    valueOrNull(
                        '#add-empParish'
                    ),

                addressVillage:
                    valueOrNull(
                        '#add-empVillage'
                    ),

                addressStreet:
                    valueOrNull(
                        '#add-empStreet'
                    ),

                postalCode:
                    valueOrNull(
                        '#add-empPostalCode'
                    ),

                skills:
                    valueOrNull('#add-empSkills'),

                languagesSpoken:
                    valueOrNull(
                        '#add-empLanguages'
                    ),

                employeeRemarks:
                    valueOrNull('#add-empRemarks'),

                contacts:
                    collectContacts(
                        viewContainer
                    ),

                qualifications:
                    await collectQualifications(
                        viewContainer
                    ),

                experiences:
                    await collectExperiences(
                        viewContainer
                    ),

                documents: [],

                accountRequest: {
                    generateLogin,

                    roleId:
                        valueOrNull(
                            '#add-employeeRoleId'
                        )
                            ? Number(
                                valueOrNull(
                                    '#add-employeeRoleId'
                                )
                            )
                            : null,

                    sendEmail:
                    sendLoginEmail
                }
            };
        };

    /*
     * Employee registration is currently completed synchronously by the
     * backend. Do not poll the progress endpoint until registration is moved
     * to a real background worker that immediately returns HTTP 202.
     */
    /*
     * Employee registration is synchronous on the backend. Keep one Employee
     * registration dialog visible from request start through success/failure;
     * do not overlap it with the global loading indicator.
     */
    const startEmployeeRegistration =
        async payload => {
            if (registrationRunning) {
                return null;
            }

            registrationRunning = true;
            registrationProgress?.open(
                latestRegistrationName
            );
            registrationProgress?.update({
                status: 'PROCESSING',
                stage: 'REQUEST_ACCEPTED',
                stageTitle: 'Creating Employee',
                percentage: 5,
                message:
                    'Please wait while the Employee record is being created.'
            });

            try {
                const response =
                    await apiPost(
                        '/branchadmin/employees/registrations',
                        payload
                    );

                const result =
                    response?.data ??
                    response;

                const completedResult = {
                    ...result,
                    status: 'COMPLETED',
                    stage:
                        result?.stage ||
                        'FINALIZATION',
                    stageTitle:
                        result?.stageTitle ||
                        'Registration Complete',
                    percentage: 100,
                    message:
                        result?.message ||
                        response?.message ||
                        'Employee registered successfully.'
                };

                registrationProgress?.showSuccess(
                    completedResult
                );

                latestRegistrationPayload =
                    null;

                return completedResult;
            } catch (error) {
                console.error(error);

                const failure =
                    extractApiFailure(
                        error,
                        'SERVER_VALIDATION',
                        0
                    );

                registrationProgress?.showFailure(
                    failure
                );

                return failure;
            } finally {
                registrationRunning = false;
            }
        };

    const navigateToEmployeeList = employeeId => {
        const normalizedEmployeeId = Number.parseInt(
            String(employeeId || ''),
            10
        );
        const hasEmployeeId =
            Number.isInteger(normalizedEmployeeId) &&
            normalizedEmployeeId > 0;

        return window.erpNavigate({
            role: 'admin',
            view: 'employees',
            routeParams: hasEmployeeId
                ? [String(normalizedEmployeeId)]
                : [],
            title: hasEmployeeId
                ? 'Employee Details'
                : 'Manage Employees'
        });
    };

    registrationProgress?.modal
        .querySelector(
            '#employee-registration-return-btn'
        )
        ?.addEventListener(
            'click',
            () => {
                registrationProgress.close();
            }
        );

    registrationProgress?.modal
        .querySelector(
            '#employee-registration-retry-btn'
        )
        ?.addEventListener(
            'click',
            () => {
                if (
                    latestRegistrationPayload
                ) {
                    void startEmployeeRegistration(
                        latestRegistrationPayload
                    );
                }
            }
        );

    registrationProgress?.modal
        .querySelector(
            '#employee-registration-add-another-btn'
        )
        ?.addEventListener(
            'click',
            () => {
                registrationProgress.close();
                resetAddEmployeeForm();
                viewContainer.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        );

    registrationProgress?.modal
        .querySelector(
            '#employee-registration-list-btn'
        )
        ?.addEventListener(
            'click',
            async event => {
                const loaded =
                    await window.erpWithButtonFeedback(
                        event.currentTarget,
                        'Opening Employee List...',
                        () => navigateToEmployeeList(null)
                    );

                if (loaded) {
                    registrationProgress.close();
                }
            }
        );

    registrationProgress?.modal
        .querySelector(
            '#employee-registration-view-btn'
        )
        ?.addEventListener(
            'click',
            async event => {
                const employeeId =
                    registrationProgress
                        .getEmployeeId();

                const loaded =
                    await window.erpWithButtonFeedback(
                        event.currentTarget,
                        'Opening Employee...',
                        () => navigateToEmployeeList(
                            employeeId
                        )
                    );

                if (loaded) {
                    registrationProgress.close();
                }
            }
        );

    form.addEventListener(
        'submit',
        async event => {
            event.preventDefault();

            if (registrationRunning) {
                return;
            }

            if (
                !AddEmployeeValidation.validate(
                    viewContainer
                )
            ) {
                showErrorMessage(
                    'Please correct the highlighted fields.'
                );
                return;
            }

            const submitButton =
                form.querySelector(
                    'button[type="submit"]'
                );

            if (!submitButton) {
                return;
            }

            const originalButtonText =
                submitButton.innerHTML;

            submitButton.disabled = true;
            submitButton.innerHTML =
                '<i class="bi bi-hourglass-split"></i> Preparing...';

            try {
                latestRegistrationName =
                    getEmployeeFormName();

                const registrationPayload =
                    await buildEmployeePayload();

                if (!registrationPayload) {
                    return;
                }

                latestRegistrationPayload =
                    registrationPayload;

                submitButton.innerHTML =
                    '<i class="bi bi-arrow-repeat"></i> Registering...';

                await startEmployeeRegistration(
                    latestRegistrationPayload
                );
            } catch (error) {
                console.error(error);

                showErrorMessage(
                    error?.message ||
                    'Could not prepare employee registration.'
                );
            } finally {
                submitButton.disabled = false;
                submitButton.innerHTML =
                    originalButtonText;
            }
        }
    );

}

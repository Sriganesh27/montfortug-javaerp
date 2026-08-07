/**
 * Public printable application receipt.
 *
 * The receipt is session-protected. No application number or branch ID is
 * accepted from the URL for data access.
 */

/**
 * @typedef {Object} ErpApplicationData
 * @property {string} [branch_name]
 * @property {string} [branch_location]
 * @property {string} [ref_number]
 * @property {string} [date_of_registration]
 * @property {string} [scholarship_status]
 * @property {string} [receipt_status]
 * @property {string} [application_status]
 * @property {string} [current_stage]
 * @property {string} [verification_status]
 * @property {string} [document_status]
 * @property {string} [test_status]
 * @property {string} [fee_decision_status]
 * @property {string} [scholarship_workflow_status]
 * @property {string} [payment_status]
 * @property {string} [admission_status]
 * @property {boolean} [workflow_locked]
 * @property {boolean} [school_logo_available]
 * @property {boolean} [applicant_photo_available]
 * @property {string} [student_name]
 * @property {string} [middle_name]
 * @property {string} [student_surname]
 * @property {string} [gender]
 * @property {string} [dob]
 * @property {string} [nationality]
 * @property {string} [academic_year]
 * @property {string} [term]
 * @property {string} [applied_class]
 * @property {string} [class_code]
 * @property {string} [level]
 * @property {string} [photo_path]
 * @property {string} [primary_email]
 * @property {string} [primary_mobile]
 * @property {string} [father_name]
 * @property {string} [father_contact]
 * @property {string} [father_email]
 * @property {string} [father_occupation]
 * @property {string} [father_education]
 * @property {string} [father_age]
 * @property {string} [mother_name]
 * @property {string} [mother_contact]
 * @property {string} [mother_email]
 * @property {string} [mother_occupation]
 * @property {string} [mother_education]
 * @property {string} [mother_age]
 * @property {string} [guardian_name]
 * @property {string} [guardian_relation]
 * @property {string} [guardian_contact]
 * @property {string} [guardian_email]
 * @property {string} [guardian_occupation]
 * @property {string} [guardian_education]
 * @property {string} [guardian_age]
 * @property {string} [guardian_location]
 * @property {string} [address_house]
 * @property {string} [address_street]
 * @property {string} [address_village]
 * @property {string} [address_district]
 * @property {string} [address_state]
 * @property {string} [address_postal]
 * @property {string} [former_school]
 * @property {string} [former_school_code]
 * @property {string} [former_school_lin]
 * @property {string} [ple_ref]
 * @property {string} [ple_score]
 * @property {string} [uce_ref]
 * @property {string} [uce_score]
 * @property {string} [subject_marks]
 * @property {string} [more_info]
 */

document.addEventListener(
    'DOMContentLoaded',
    () => {
        bindPrintButton();
        bindLogoFallback();
        setGeneratedTimestamp();
        loadReceipt();
    }
);

function bindPrintButton() {
    const printButton =
        document.getElementById(
            'triggerPrintBtn'
        );

    if (!printButton) {
        return;
    }

    printButton.addEventListener(
        'click',
        () => window.print()
    );
}

function bindLogoFallback() {
    const schoolLogo =
        document.getElementById(
            'schoolLogo'
        );

    if (!schoolLogo) {
        return;
    }

    /*
     * Do not substitute the global Montfort logo when a Branch logo is
     * unavailable. The receipt must represent the selected school.
     */
    schoolLogo.addEventListener(
        'error',
        () => {
            schoolLogo.classList.add(
                'hidden-element'
            );
        }
    );
}

function combineFields(
    fields,
    separator
) {
    const valid =
        fields.filter(
            value =>
                value !== null
                && value !== undefined
                && String(value).trim() !== ''
        );

    return valid.length > 0
        ? valid.join(separator)
        : '-';
}

function displayField(
    value
) {
    if (value === null
            || value === undefined
            || value === '') {
        return '-';
    }

    const normalized =
        String(value).trim();

    return normalized !== ''
        ? normalized
        : '-';
}

function setElementText(
    id,
    text
) {
    const element =
        document.getElementById(id);

    if (element) {
        element.textContent = text;
    }
}

function humanizeEnum(
    value
) {
    const normalized =
        String(value || '')
            .trim();

    if (!normalized) {
        return '-';
    }

    return normalized
        .toLowerCase()
        .replace(/_/g, ' ')
        .replace(
            /\b\w/g,
            character =>
                character.toUpperCase()
        );
}

function renderSubjectsSecurely(
    containerId,
    jsonValue
) {
    const container =
        document.getElementById(
            containerId
        );

    if (!container) {
        return;
    }

    while (container.firstChild) {
        container.removeChild(
            container.firstChild
        );
    }

    if (!jsonValue
            || String(jsonValue).trim() === '') {
        appendSubjectMessage(
            container,
            'No specific subjects declared.'
        );
        return;
    }

    try {
        const parsed =
            typeof jsonValue === 'string'
                ? JSON.parse(jsonValue)
                : jsonValue;

        if (!Array.isArray(parsed)
                || parsed.length === 0) {
            appendSubjectMessage(
                container,
                'No subjects declared.'
            );
            return;
        }

        const table =
            document.createElement(
                'table'
            );

        table.className =
            'secure-marks-table';

        const thead =
            document.createElement(
                'thead'
            );

        const headingRow =
            document.createElement(
                'tr'
            );

        [
            'Subject Name',
            'Marks Submitted',
            'Grade'
        ].forEach(
            heading => {
                const cell =
                    document.createElement(
                        'th'
                    );

                cell.textContent =
                    heading;

                headingRow.appendChild(
                    cell
                );
            }
        );

        thead.appendChild(
            headingRow
        );

        table.appendChild(
            thead
        );

        const tbody =
            document.createElement(
                'tbody'
            );

        parsed.forEach(
            subject => {
                const values =
                    subject
                    && typeof subject === 'object'
                        ? Object.values(subject)
                        : [];

                const name =
                    subject?.name
                    || subject?.subject
                    || values[0]
                    || '-';

                const mark =
                    subject?.mark
                    || subject?.marks
                    || values[1]
                    || '-';

                const grade =
                    subject?.grade
                    || values[2]
                    || '-';

                const row =
                    document.createElement(
                        'tr'
                    );

                [
                    name,
                    mark,
                    grade
                ].forEach(
                    value => {
                        const cell =
                            document.createElement(
                                'td'
                            );

                        cell.textContent =
                            displayField(value);

                        row.appendChild(
                            cell
                        );
                    }
                );

                tbody.appendChild(
                    row
                );
            }
        );

        table.appendChild(
            tbody
        );

        container.appendChild(
            table
        );
    } catch (error) {
        appendSubjectMessage(
            container,
            displayField(jsonValue)
        );
    }
}

function appendSubjectMessage(
    container,
    message
) {
    const span =
        document.createElement(
            'span'
        );

    span.className = 'value';
    span.textContent = message;

    container.appendChild(span);
}

async function loadReceipt() {
    const urlParams =
        new URLSearchParams(
            window.location.search
        );

    const cosmeticStudentName =
        urlParams.get('student')
        || 'Student';

    document.title =
        `Application Receipt - ${cosmeticStudentName}`;

    try {
        const response =
            await fetch(
                '/api/public/applications/receipt',
                {
                    method: 'GET',
                    credentials: 'same-origin',
                    cache: 'no-store',
                    headers: {
                        'Accept':
                            'application/json'
                    }
                }
            );

        if (response.status === 403) {
            throw new Error(
                'SESSION_EXPIRED'
            );
        }

        if (!response.ok) {
            throw new Error(
                `Receipt request failed with status ${response.status}.`
            );
        }

        const payload =
            await response.json();

        if (!payload
                || payload.success !== true
                || !payload.data) {
            throw new Error(
                payload?.message
                || 'Could not load application receipt.'
            );
        }

        renderReceipt(
            payload.data
        );
    } catch (error) {
        console.error(error);

        if (error.message
                === 'SESSION_EXPIRED') {
            showReceiptError(
                'Session Expired',
                'Your secure session has expired. Please verify your application again.'
            );
            return;
        }

        showReceiptError(
            'Application Error',
            error.message
            || 'Secure network error loading application receipt.'
        );
    }
}

/**
 * @param {ErpApplicationData} app
 */
function renderReceipt(
    app
) {
    setElementText(
        'branch_name',
        displayField(
            app.branch_name
            || 'School'
        )
    );

    setElementText(
        'branch_location',
        displayField(
            app.branch_location
            || 'Uganda'
        )
    );

    setElementText(
        'ref_number',
        displayField(
            app.ref_number
        )
    );

    setElementText(
        'date_of_registration',
        displayField(
            app.date_of_registration
        )
    );

    renderScholarshipStatus(app);
    renderWorkflowStatus(app);
    renderSchoolLogo(app);
    renderStudentDetails(app);
    renderPhoto(app);
    renderContacts(app);
    renderParentGuardianDetails(app);
    renderAddress(app);
    renderAcademicHistory(app);
}

function renderScholarshipStatus(
    app
) {
    const scholarship =
        String(
            app.scholarship_status
            || app.scholarship_workflow_status
            || ''
        ).trim();

    const container =
        document.getElementById(
            'schol_container'
        );

    if (!container) {
        return;
    }

    const normalized =
        scholarship.toLowerCase();

    if (!scholarship
            || normalized === 'none'
            || normalized === 'not_applied'
            || normalized === 'not applied') {
        container.classList.add(
            'hidden-element'
        );
        return;
    }

    setElementText(
        'schol_val',
        humanizeEnum(
            scholarship
        )
    );

    container.classList.remove(
        'hidden-element'
    );
}

function renderWorkflowStatus(
    app
) {
    const rawStatus =
        app.receipt_status
        || app.current_stage
        || app.application_status
        || app.status
        || 'PENDING';

    const status =
        humanizeEnum(
            rawStatus
        );

    const statusElement =
        document.getElementById(
            'status_val'
        );

    if (!statusElement) {
        return;
    }

    const normalized =
        String(rawStatus)
            .trim()
            .toUpperCase();

    let statusClass =
        'status-pending';

    if (normalized === 'ENROLLED'
            || normalized === 'ADMITTED'
            || normalized === 'FINAL_ADMISSION'
            || normalized === 'APPROVED') {
        statusClass =
            'status-admitted';
    } else if (normalized === 'REJECTED'
            || normalized === 'CLOSED') {
        statusClass =
            'status-rejected';
    }

    statusElement.textContent =
        status;

    statusElement.className =
        `meta-value ${statusClass}`;
}

function renderSchoolLogo(
    app
) {
    const schoolLogo =
        document.getElementById(
            'schoolLogo'
        );

    if (!schoolLogo) {
        return;
    }

    if (app.school_logo_available === false) {
        schoolLogo.classList.add(
            'hidden-element'
        );
        return;
    }

    schoolLogo.classList.remove(
        'hidden-element'
    );

    /*
     * Assign after the verified receipt response is available. The timestamp
     * prevents a stale image from surviving a Branch logo change.
     */
    schoolLogo.src =
        `/api/public/applications/receipt/logo?v=${Date.now()}`;
}

function renderStudentDetails(
    app
) {
    setElementText(
        'full_name',
        combineFields(
            [
                app.student_name,
                app.middle_name,
                app.student_surname
            ],
            ' '
        )
    );

    setElementText(
        'gender',
        displayField(app.gender)
    );

    setElementText(
        'dob',
        displayField(app.dob)
    );

    setElementText(
        'nationality',
        displayField(app.nationality)
    );

    setElementText(
        'acad_year',
        displayField(app.academic_year)
    );

    setElementText(
        'acad_term_only',
        displayField(app.term)
    );

    const classCode =
        app.class_code
            ? `[${app.class_code}]`
            : '';

    setElementText(
        'applied_class',
        combineFields(
            [
                app.applied_class,
                classCode
            ],
            ' '
        )
    );

    setElementText(
        'level',
        displayField(app.level)
    );
}

function renderPhoto(
    app
) {
    const photoElement =
        document.getElementById(
            'student_photo'
        );

    const noPhotoElement =
        document.getElementById(
            'no_photo'
        );

    if (!photoElement
        || !noPhotoElement) {
        return;
    }

    if (app.applicant_photo_available === false
        || !app.photo_path) {

        photoElement.classList.add(
            'hidden-element'
        );

        noPhotoElement.classList.remove(
            'hidden-element'
        );

        noPhotoElement.style.display =
            '';

        return;
    }

    photoElement.addEventListener(
        'error',
        () => {
            photoElement.classList.add(
                'hidden-element'
            );

            noPhotoElement.classList.remove(
                'hidden-element'
            );

            noPhotoElement.style.display =
                '';
        },
        {
            once: true
        }
    );

    photoElement.src =
        `/api/public/applications/receipt/photo?v=${Date.now()}`;

    photoElement.classList.remove(
        'hidden-element'
    );

    noPhotoElement.classList.add(
        'hidden-element'
    );

    noPhotoElement.style.display =
        'none';
}

function renderContacts(
    app
) {
    setElementText(
        'primary_email',
        displayField(
            app.primary_email
        )
    );

    setElementText(
        'primary_mobile',
        displayField(
            app.primary_mobile
        )
    );
}

function renderParentGuardianDetails(
    app
) {
    setElementText(
        'father_name',
        displayField(app.father_name)
    );

    setElementText(
        'father_contact',
        displayField(app.father_contact)
    );

    setElementText(
        'father_email',
        displayField(app.father_email)
    );

    setElementText(
        'father_occupation',
        displayField(app.father_occupation)
    );

    setElementText(
        'father_education',
        displayField(app.father_education)
    );

    setElementText(
        'father_age',
        displayField(app.father_age)
    );

    setElementText(
        'mother_name',
        displayField(app.mother_name)
    );

    setElementText(
        'mother_contact',
        displayField(app.mother_contact)
    );

    setElementText(
        'mother_email',
        displayField(app.mother_email)
    );

    setElementText(
        'mother_occupation',
        displayField(app.mother_occupation)
    );

    setElementText(
        'mother_education',
        displayField(app.mother_education)
    );

    setElementText(
        'mother_age',
        displayField(app.mother_age)
    );

    setElementText(
        'guardian_name',
        displayField(app.guardian_name)
    );

    setElementText(
        'guardian_relation',
        displayField(
            app.guardian_relation
        )
    );

    setElementText(
        'guardian_contact',
        displayField(
            app.guardian_contact
        )
    );

    setElementText(
        'guardian_email',
        displayField(
            app.guardian_email
        )
    );

    setElementText(
        'guardian_occupation',
        displayField(
            app.guardian_occupation
        )
    );

    const educationAge =
        combineFields(
            [
                app.guardian_education,
                app.guardian_age
                    ? `Age: ${app.guardian_age}`
                    : null
            ],
            ' | '
        );

    setElementText(
        'guardian_edu_age',
        educationAge
    );

    setElementText(
        'guardian_location',
        displayField(
            app.guardian_location
        )
    );
}

function renderAddress(
    app
) {
    setElementText(
        'address_house_street',
        combineFields(
            [
                app.address_house,
                app.address_street
            ],
            ' / '
        )
    );

    setElementText(
        'address_village_district',
        combineFields(
            [
                app.address_village,
                app.address_district
            ],
            ' / '
        )
    );

    setElementText(
        'address_region_postal',
        combineFields(
            [
                app.address_state,
                app.address_postal
            ],
            ' / '
        )
    );
}

function renderAcademicHistory(
    app
) {
    setElementText(
        'former_school',
        displayField(
            app.former_school
        )
    );

    setElementText(
        'former_school_code',
        displayField(
            app.former_school_code
        )
    );

    setElementText(
        'former_school_lin',
        displayField(
            app.former_school_lin
        )
    );

    setElementText(
        'ple_ref_score',
        combineFields(
            [
                app.ple_ref,
                app.ple_score
            ],
            ' / '
        )
    );

    setElementText(
        'uce_ref_score',
        combineFields(
            [
                app.uce_ref,
                app.uce_score
            ],
            ' / '
        )
    );

    renderSubjectsSecurely(
        'subject_marks_container',
        app.subject_marks
    );

    setElementText(
        'more_info',
        displayField(
            app.more_info
            || 'None declared.'
        )
    );

    const moreInfoElement =
        document.getElementById(
            'more_info'
        );

    if (moreInfoElement) {
        moreInfoElement.classList.add(
            'value'
        );
    }
}

function showReceiptError(
    title,
    message
) {
    if (typeof window.showSessionTimeoutModal
            === 'function') {
        window.showSessionTimeoutModal(
            {
                title,
                message,
                buttonText: 'Return',
                redirectUrl:
                    '/apply/status'
            }
        );
        return;
    }

    window.location.href =
        '/apply/status';
}

function setGeneratedTimestamp() {
    const timestampElement =
        document.getElementById(
            'generated_timestamp'
        );

    if (!timestampElement) {
        return;
    }

    const now =
        new Date();

    const options = {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    };

    timestampElement.textContent =
        now.toLocaleDateString(
            'en-US',
            options
        );
}

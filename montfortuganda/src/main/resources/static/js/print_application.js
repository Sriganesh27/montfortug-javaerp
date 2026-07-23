/**
 * Secure Application Receipt Loader
 *
 * Supports both:
 * 1. Current session-based backend:
 *      GET /api/public/applications/details
 * 2. Future short-lived print-token backend:
 *      GET /api/public/applications/details?token=...
 */

'use strict';

/**
 * @typedef {Object} ErpApplicationData
 * @property {string} [branch_name]
 * @property {string} [branch_location]
 * @property {string} [ref_number]
 * @property {string} [date_of_registration]
 * @property {string} [scholarship_status]
 * @property {string} [status]
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

document.addEventListener('DOMContentLoaded', () => {
    bindReceiptActions();
    setGeneratedTimestamp();
    void loadApplicationReceipt();
});

function bindReceiptActions() {
    const printBtn = document.getElementById('triggerPrintBtn');

    if (printBtn) {
        printBtn.addEventListener('click', () => {
            window.print();
        });
    }

    const schoolLogo = document.getElementById('schoolLogo');

    if (schoolLogo) {
        schoolLogo.addEventListener('error', () => {
            schoolLogo.classList.add('hidden-element');
        });
    }

    const studentPhoto = document.getElementById('student_photo');

    if (studentPhoto) {
        studentPhoto.addEventListener('error', () => {
            studentPhoto.classList.add('hidden-element');

            const noPhoto = document.getElementById('no_photo');

            if (noPhoto) {
                noPhoto.classList.remove('hidden-element');
                noPhoto.style.removeProperty('display');
            }
        });
    }
}

async function loadApplicationReceipt() {
    try {
        const params = new URLSearchParams(window.location.search);
        const printToken = params.get('token');
        const studentName = params.get('student');

        if (studentName) {
            document.title =
                `Application Receipt - ${studentName.replaceAll('-', ' ')}`;
        }

        let detailsUrl = '/api/public/applications/details';

        if (printToken) {
            detailsUrl += `?token=${encodeURIComponent(printToken)}`;
        }

        const response = await fetch(detailsUrl, {
            method: 'GET',
            credentials: 'same-origin',
            cache: 'no-store',
            headers: {
                Accept: 'application/json'
            }
        });

        if (response.status === 401 || response.status === 403) {
            throw new ReceiptLoadError(
                'SESSION_EXPIRED',
                'Your secure verification session has expired.'
            );
        }

        if (!response.ok) {
            throw new ReceiptLoadError(
                'HTTP_ERROR',
                `Application details request failed with status ${response.status}.`
            );
        }

        const result = await readJsonResponse(response);

        if (!result || result.success !== true || !result.data) {
            throw new ReceiptLoadError(
                'INVALID_RESPONSE',
                result?.message || 'The application details response is invalid.'
            );
        }

        renderApplicationReceipt(result.data);
    } catch (error) {
        console.error('Failed to load application receipt:', error);

        const message =
            error instanceof ReceiptLoadError
                ? error.message
                : 'A secure network error occurred while loading the receipt.';

        showReceiptError(message);
    }
}

async function readJsonResponse(response) {
    const contentType = response.headers.get('content-type') || '';

    if (!contentType.toLowerCase().includes('application/json')) {
        throw new ReceiptLoadError(
            'INVALID_CONTENT_TYPE',
            'The server did not return a valid application response.'
        );
    }

    return response.json();
}

/**
 * @param {ErpApplicationData} app
 */
function renderApplicationReceipt(app) {
    setElementText(
        'branch_name',
        displayField(app.branch_name || 'General Campus')
    );

    setElementText(
        'branch_location',
        displayField(app.branch_location || 'Kampala')
    );

    setElementText('ref_number', displayField(app.ref_number));
    setElementText(
        'date_of_registration',
        displayField(app.date_of_registration)
    );

    renderScholarshipStatus(app.scholarship_status);
    renderAdmissionStatus(app.status);

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

    setElementText('gender', displayField(app.gender));
    setElementText('dob', displayField(app.dob));
    setElementText('nationality', displayField(app.nationality));
    setElementText('acad_year', displayField(app.academic_year));
    setElementText('acad_term_only', displayField(app.term));

    const classCode = app.class_code
        ? `[${app.class_code}]`
        : null;

    setElementText(
        'applied_class',
        combineFields(
            [app.applied_class, classCode],
            ' '
        )
    );

    setElementText('level', displayField(app.level));

    renderStudentPhoto(app.photo_path);

    setElementText('primary_email', displayField(app.primary_email));
    setElementText('primary_mobile', displayField(app.primary_mobile));

    setElementText('father_name', displayField(app.father_name));
    setElementText('father_contact', displayField(app.father_contact));
    setElementText('father_email', displayField(app.father_email));
    setElementText(
        'father_occupation',
        displayField(app.father_occupation)
    );
    setElementText(
        'father_education',
        displayField(app.father_education)
    );
    setElementText('father_age', displayField(app.father_age));

    setElementText('mother_name', displayField(app.mother_name));
    setElementText('mother_contact', displayField(app.mother_contact));
    setElementText('mother_email', displayField(app.mother_email));
    setElementText(
        'mother_occupation',
        displayField(app.mother_occupation)
    );
    setElementText(
        'mother_education',
        displayField(app.mother_education)
    );
    setElementText('mother_age', displayField(app.mother_age));

    setElementText('guardian_name', displayField(app.guardian_name));
    setElementText(
        'guardian_relation',
        displayField(app.guardian_relation)
    );
    setElementText(
        'guardian_contact',
        displayField(app.guardian_contact)
    );
    setElementText('guardian_email', displayField(app.guardian_email));
    setElementText(
        'guardian_occupation',
        displayField(app.guardian_occupation)
    );

    setElementText(
        'guardian_edu_age',
        combineFields(
            [
                app.guardian_education,
                app.guardian_age
                    ? `Age: ${app.guardian_age}`
                    : null
            ],
            ' | '
        )
    );

    setElementText(
        'guardian_location',
        displayField(app.guardian_location)
    );

    setElementText(
        'address_house_street',
        combineFields(
            [app.address_house, app.address_street],
            ' / '
        )
    );

    setElementText(
        'address_village_district',
        combineFields(
            [app.address_village, app.address_district],
            ' / '
        )
    );

    setElementText(
        'address_region_postal',
        combineFields(
            [app.address_state, app.address_postal],
            ' / '
        )
    );

    setElementText('former_school', displayField(app.former_school));
    setElementText(
        'former_school_code',
        displayField(app.former_school_code)
    );
    setElementText(
        'former_school_lin',
        displayField(app.former_school_lin)
    );

    setElementText(
        'ple_ref_score',
        combineFields([app.ple_ref, app.ple_score], ' / ')
    );

    setElementText(
        'uce_ref_score',
        combineFields([app.uce_ref, app.uce_score], ' / ')
    );

    renderSubjectsSecurely(
        'subject_marks_container',
        app.subject_marks
    );

    setElementText(
        'more_info',
        displayField(app.more_info || 'None declared.')
    );

    const moreInfo = document.getElementById('more_info');

    if (moreInfo) {
        moreInfo.classList.add('value');
    }
}

function renderScholarshipStatus(value) {
    const scholarship = String(value || '').trim();
    const container = document.getElementById('schol_container');

    if (!container) {
        return;
    }

    const shouldDisplay =
        scholarship.length > 0 &&
        scholarship.toLowerCase() !== 'none';

    container.classList.toggle(
        'hidden-element',
        !shouldDisplay
    );

    if (shouldDisplay) {
        setElementText('schol_val', scholarship);
    }
}

function renderAdmissionStatus(value) {
    const status = String(value || 'Pending').trim();
    const normalized = status.toLowerCase();

    let statusClass = 'status-pending';

    if (
        normalized === 'admitted' ||
        normalized === 'selected'
    ) {
        statusClass = 'status-admitted';
    } else if (normalized === 'rejected') {
        statusClass = 'status-rejected';
    }

    const statusElement =
        document.getElementById('status_val');

    if (!statusElement) {
        return;
    }

    statusElement.textContent = status;
    statusElement.className =
        `meta-value ${statusClass}`;
}

function renderStudentPhoto(photoPath) {
    const photo = document.getElementById('student_photo');
    const noPhoto = document.getElementById('no_photo');

    if (!photo || !noPhoto) {
        return;
    }

    if (!photoPath) {
        photo.classList.add('hidden-element');
        noPhoto.classList.remove('hidden-element');
        noPhoto.style.removeProperty('display');
        return;
    }

    let finalPath = String(photoPath).trim();

    const uploadMarker = '/assets/uploads/';
    const markerIndex = finalPath.indexOf(uploadMarker);

    if (markerIndex >= 0) {
        finalPath = finalPath.substring(markerIndex);
    }

    photo.src = finalPath;
    photo.classList.remove('hidden-element');
    noPhoto.classList.add('hidden-element');
    noPhoto.style.display = 'none';
}

function renderSubjectsSecurely(containerId, rawValue) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.replaceChildren();

    if (!rawValue || String(rawValue).trim() === '') {
        appendSubjectMessage(
            container,
            'No specific subjects declared.'
        );
        return;
    }

    try {
        const values =
            typeof rawValue === 'string'
                ? JSON.parse(rawValue)
                : rawValue;

        if (!Array.isArray(values) || values.length === 0) {
            appendSubjectMessage(
                container,
                'No subjects declared.'
            );
            return;
        }

        const table = document.createElement('table');
        table.className = 'secure-marks-table';

        const header = document.createElement('thead');
        const headerRow = document.createElement('tr');

        ['Subject Name', 'Marks Submitted', 'Grade']
            .forEach(label => {
                const cell = document.createElement('th');
                cell.textContent = label;
                headerRow.appendChild(cell);
            });

        header.appendChild(headerRow);
        table.appendChild(header);

        const body = document.createElement('tbody');

        values.forEach(subject => {
            const row = document.createElement('tr');

            const objectValues =
                subject && typeof subject === 'object'
                    ? Object.values(subject)
                    : [];

            const subjectName =
                subject?.name ||
                subject?.subject ||
                objectValues[0] ||
                '-';

            const marks =
                subject?.mark ||
                subject?.marks ||
                objectValues[1] ||
                '-';

            const grade =
                subject?.grade ||
                objectValues[2] ||
                '-';

            [subjectName, marks, grade].forEach(value => {
                const cell = document.createElement('td');
                cell.textContent = displayField(value);
                row.appendChild(cell);
            });

            body.appendChild(row);
        });

        table.appendChild(body);
        container.appendChild(table);
    } catch (error) {
        console.warn(
            'Subject marks could not be parsed as JSON:',
            error
        );

        appendSubjectMessage(
            container,
            displayField(rawValue)
        );
    }
}

function appendSubjectMessage(container, message) {
    const span = document.createElement('span');
    span.className = 'value';
    span.textContent = message;
    container.appendChild(span);
}

function setGeneratedTimestamp() {
    const element =
        document.getElementById('generated_timestamp');

    if (!element) {
        return;
    }

    element.textContent =
        new Date().toLocaleString('en-UG', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
}

function showReceiptError(message) {
    if (
        typeof window.showSessionTimeoutModal ===
        'function'
    ) {
        window.showSessionTimeoutModal({
            title: 'Application Receipt',
            message,
            buttonText: 'Return to Status Page',
            redirectUrl: '/apply/status'
        });

        return;
    }

    const printableArea =
        document.getElementById('printableArea');

    if (printableArea) {
        printableArea.replaceChildren();

        const errorBox = document.createElement('div');
        errorBox.setAttribute('role', 'alert');
        errorBox.style.padding = '2rem';
        errorBox.style.textAlign = 'center';

        const heading = document.createElement('h2');
        heading.textContent =
            'Unable to load application receipt';

        const paragraph = document.createElement('p');
        paragraph.textContent = message;

        const link = document.createElement('a');
        link.href = '/apply/status';
        link.textContent = 'Return to Application Status';

        errorBox.append(heading, paragraph, link);
        printableArea.appendChild(errorBox);
    }
}

function combineFields(fields, separator) {
    const validValues = fields
        .filter(value =>
            value !== null &&
            value !== undefined &&
            String(value).trim() !== ''
        )
        .map(value => String(value).trim());

    return validValues.length > 0
        ? validValues.join(separator)
        : '-';
}

function displayField(value) {
    if (value === null || value === undefined) {
        return '-';
    }

    const text = String(value).trim();

    return text || '-';
}

function setElementText(id, text) {
    const element = document.getElementById(id);

    if (element) {
        element.textContent = text;
    }
}

class ReceiptLoadError extends Error {
    constructor(code, message) {
        super(message);
        this.name = 'ReceiptLoadError';
        this.code = code;
    }
}

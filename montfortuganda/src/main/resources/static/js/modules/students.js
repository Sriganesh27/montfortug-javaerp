// noinspection SpellCheckingInspection
/* global AppImporter, CrudTable, showLoader, hideLoader, showErrorMessage, showSuccessMessage, createErpCalendar */

(function initializeStudentsModule() {
    'use strict';

    const MAX_PHOTO_BYTES = 2 * 1024 * 1024;
    const ALLOWED_PHOTO_TYPES = new Set([
        'image/jpeg',
        'image/png',
        'image/webp'
    ]);
    const STUDENT_VIEW_STATES = new WeakMap();
    /** @type {WeakMap<HTMLButtonElement, Node[]>} */
    const BUTTON_ORIGINAL_NODES = new WeakMap();

    const BACKEND_FIELD_SELECTORS = Object.freeze({
        applicationId: '#add-studentApplicationId',
        'personal.learnerLin': '#add-studentLearnerLin',
        'personal.admissionYear': '#add-studentAdmissionYear',
        'personal.joiningTermId': '#add-studentJoiningTerm',
        'personal.firstName': '#add-studentFirstName',
        'personal.middleName': '#add-studentMiddleName',
        'personal.lastName': '#add-studentLastName',
        'personal.gender': '#add-studentGender',
        'personal.dateOfBirth': '#add-studentDob',
        'personal.nationality': '#add-studentNationality',
        'personal.houseNo': '#add-studentHouseNo',
        'personal.street': '#add-studentStreet',
        'personal.village': '#add-studentVillage',
        'personal.townCity': '#add-studentTownCity',
        'personal.district': '#add-studentDistrict',
        'personal.state': '#add-studentState',
        'personal.country': '#add-studentCountry',
        'personal.postalCode': '#add-studentPostalCode',
        'parent.fatherName': '#add-studentFatherName',
        'parent.fatherUin': '#add-studentFatherUin',
        'parent.fatherPhone': '#add-studentFatherPhone',
        'parent.fatherAlternatePhone': '#add-studentFatherAltPhone',
        'parent.fatherEmail': '#add-studentFatherEmail',
        'parent.fatherOccupation': '#add-studentFatherOccupation',
        'parent.fatherEmployer': '#add-studentFatherEmployer',
        'parent.fatherDesignation': '#add-studentFatherDesignation',
        'parent.fatherAnnualIncome': '#add-studentFatherIncome',
        'parent.motherName': '#add-studentMotherName',
        'parent.motherUin': '#add-studentMotherUin',
        'parent.motherPhone': '#add-studentMotherPhone',
        'parent.motherAlternatePhone': '#add-studentMotherAltPhone',
        'parent.motherEmail': '#add-studentMotherEmail',
        'parent.motherOccupation': '#add-studentMotherOccupation',
        'parent.motherEmployer': '#add-studentMotherEmployer',
        'parent.motherDesignation': '#add-studentMotherDesignation',
        'parent.motherAnnualIncome': '#add-studentMotherIncome',
        'parent.guardianName': '#add-studentGuardianName',
        'parent.guardianUin': '#add-studentGuardianUin',
        'parent.guardianRelationship': '#add-studentGuardianRelationship',
        'parent.guardianPhone': '#add-studentGuardianPhone',
        'parent.guardianAlternatePhone': '#add-studentGuardianAltPhone',
        'parent.guardianEmail': '#add-studentGuardianEmail',
        'parent.guardianOccupation': '#add-studentGuardianOccupation',
        'parent.preferredContact': '[name="add-studentPreferredContact"]',
        'parent.feeResponsibility': '[name="add-studentFeeResponsibility"]',
        'parent.parentsLivingTogether': '[name="add-studentParentsTogether"]',
        'parent.emergencyContactName': '#add-studentEmergencyName',
        'parent.emergencyContactPhone': '#add-studentEmergencyPhone',
        'parent.emergencyContactRelationship': '#add-studentEmergencyRelationship',
        'parent.remarks': '#add-studentParentRemarks',
        'enrollment.academicYearId': '#add-studentAcademicYear',
        'enrollment.classId': '#add-studentClass',
        'enrollment.sectionId': '#add-studentSection',
        'enrollment.rollNo': '#add-studentRollNo',
        'enrollment.admissionType': '#add-studentAdmissionType',
        'enrollment.joiningDate': '#add-studentJoiningDate',
        'enrollment.remarks': '#add-studentEnrollmentRemarks',
        'medical.bloodGroup': '#add-studentBloodGroup',
        'medical.heightCm': '#add-studentHeightCm',
        'medical.weightKg': '#add-studentWeightKg',
        'medical.fitForSports': '#add-studentFitForSports',
        'medical.allergies': '#add-studentAllergies',
        'medical.chronicConditions': '#add-studentChronicConditions',
        'medical.ongoingMedication': '#add-studentOngoingMedication',
        'medical.specialNeeds': '#add-studentSpecialNeeds',
        'medical.emergencyDoctorName': '#add-studentDoctorName',
        'medical.emergencyDoctorMobile': '#add-studentDoctorMobile',
        'medical.preferredHospital': '#add-studentPreferredHospital',
        'medical.remarks': '#add-studentMedicalRemarks',
        'academicHistory.formerSchoolName': '#add-studentFormerSchoolName',
        'academicHistory.formerSchoolCode': '#add-studentFormerSchoolCode',
        'academicHistory.formerSchoolLin': '#add-studentFormerSchoolLin',
        'academicHistory.formerSchoolAddress': '#add-studentFormerSchoolAddress',
        'academicHistory.schoolType': '#add-studentSchoolType',
        'academicHistory.transferReason': '#add-studentTransferReason',
        'academicHistory.previousAcademicYear': '#add-studentPreviousAcademicYear',
        'academicHistory.previousClass': '#add-studentPreviousClass',
        'academicHistory.previousSection': '#add-studentPreviousSection',
        'academicHistory.previousStream': '#add-studentPreviousStream',
        'academicHistory.pleIndexNumber': '#add-studentPleIndexNumber',
        'academicHistory.pleAggregate': '#add-studentPleAggregate',
        'academicHistory.uceIndexNumber': '#add-studentUceIndexNumber',
        'academicHistory.uceResult': '#add-studentUceResult',
        'academicHistory.uaceIndexNumber': '#add-studentUaceIndexNumber',
        'academicHistory.uaceResult': '#add-studentUaceResult',
        'academicHistory.subjectMarks': '#add-studentSubjectMarks',
        'academicHistory.remarks': '#add-studentAcademicRemarks',
        'hostel.hostelId': '#add-studentHostelId',
        'hostel.roomId': '#add-studentHostelRoomId',
        'hostel.bedId': '#add-studentHostelBedId',
        'hostel.allocationStartDate': '#add-studentHostelStartDate',
        'hostel.allocationEndDate': '#add-studentHostelEndDate',
        'hostel.localGuardianName': '#add-studentHostelGuardianName',
        'hostel.localGuardianMobile': '#add-studentHostelGuardianMobile',
        'hostel.localGuardianRelation': '#add-studentHostelGuardianRelation',
        'hostel.remarks': '#add-studentHostelRemarks',
        'transport.routeId': '#add-studentTransportRouteId',
        'transport.vehicleId': '#add-studentTransportVehicleId',
        'transport.pickupPointId': '#add-studentTransportPickupPointId',
        'transport.transportStartDate': '#add-studentTransportStartDate',
        'transport.transportEndDate': '#add-studentTransportEndDate',
        'transport.seatNumber': '#add-studentTransportSeatNumber',
        'transport.emergencyContact': '#add-studentTransportEmergencyContact',
        'transport.emergencyMobile': '#add-studentTransportEmergencyMobile',
        'transport.remarks': '#add-studentTransportRemarks'
    });

    document.addEventListener('viewLoaded', event => {
        if (!(event instanceof CustomEvent)) {
            return;
        }

        const detail = isRecord(event.detail)
            ? event.detail
            : {};
        const role = readProperty(detail, 'role');
        const viewName = readProperty(detail, 'view');

        if (
            role !== 'admin' ||
            viewName !== 'add-student'
        ) {
            return;
        }

        /*
         * Student reference data can be slow over the development SSH
         * tunnel. Do not attach this work to global view waitUntil(),
         * otherwise #main-content-area keeps pointer-events disabled and
         * the complete page appears frozen.
         */
        void initAddStudentView().catch(error => {
            console.error(
                'Add Student view initialization failed.',
                error
            );

            if (typeof showErrorMessage === 'function') {
                showErrorMessage(
                    'The Add Student form could not be prepared completely.'
                );
            }
        });
    });

    async function initAddStudentView() {
        const view = document.querySelector(
            '#ba-add-student-view'
        );

        if (!(view instanceof HTMLElement)) {
            return;
        }

        const originalForm = view.querySelector(
            '#add-student-form'
        );

        if (!(originalForm instanceof HTMLFormElement)) {
            return;
        }

        const clonedForm = originalForm.cloneNode(true);

        if (
            !(clonedForm instanceof HTMLFormElement) ||
            !(originalForm.parentNode instanceof Node)
        ) {
            return;
        }

        const form = clonedForm;

        originalForm.parentNode.replaceChild(
            form,
            originalForm
        );

        hideValidationSummary(view);
        clearAllValidationErrors(view);

        const state = {
            referenceData: createEmptyReferenceData(),
            submitting: false,
            lastPayload: null,
            lastStudentId: null,
            operationId: null,
            selectedAcademicYearText: '-',
            selectedClassText: '-',
            selectedSectionText: 'Not assigned'
        };

        bindLiveValidationClearing(view);
        bindPhotoPreview(view);
        bindConditionalSections(view);
        bindSubjectResults(view);
        bindPreviousEducation(view, state);
        bindCurrentPlacement(view, state);
        bindNavigation(view, state);
        bindStudentBulkImport(view);
        bindFormReset(view, form, state);
        bindRegistrationModal(view, form, state);
        bindFormSubmission(view, form, state);

        initializeStudentCalendars(view);
        setInitialDateValues(view);
        resetConditionalSections(view);
        updateSubjectSummary(view);

        await loadStudentReferenceData(
            view,
            state
        );
    }

    function createEmptyReferenceData() {
        return {
            academicYears: [],
            levels: [],
            classes: [],
            previousClasses: [],
            sections: [],
            hostels: [],
            hostelRooms: [],
            hostelBeds: [],
            transportRoutes: [],
            vehicles: [],
            pickupPoints: []
        };
    }

    async function loadStudentReferenceData(
        view,
        state
    ) {
        let securedReferenceData = null;

        try {
            const response = await requestGet(
                '/students/reference-data'
            );

            securedReferenceData =
                unwrapResponseData(response);
        } catch (error) {
            console.warn(
                'Student reference-data endpoint is not available yet.',
                error
            );
        }

        const referenceData = normalizeReferenceData(
            securedReferenceData
        );

        /*
         * Current placement must use only the authenticated branch classes
         * returned by /students/reference-data.
         *
         * Public levels/classes are kept separately for previous-school
         * history because that school may have offered levels not configured
         * for the current branch.
         */
        try {
            const [levelResponse, classResponse] =
                await Promise.all([
                    requestGet('/public/levels'),
                    requestGet('/public/classes')
                ]);

            const publicLevels = asArray(
                unwrapResponseData(levelResponse)
            );
            const publicClasses = asArray(
                unwrapResponseData(classResponse)
            );

            if (publicLevels.length > 0) {
                referenceData.levels = publicLevels;
            }

            referenceData.previousClasses =
                publicClasses.length > 0
                    ? publicClasses
                    : [...referenceData.classes];
        } catch (error) {
            console.warn(
                'Public previous-education references could not be loaded.',
                error
            );

            referenceData.previousClasses = [
                ...referenceData.classes
            ];
        }

        state.referenceData = referenceData;

        populateAcademicYears(view, state);
        populateCurrentClasses(view, state);
        populateJoiningTerms(view, state);
        populatePreviousLevels(view, state);
        populateHostelOptions(view, state);
        populateTransportOptions(view, state);
    }

    function normalizeReferenceData(data) {
        const source = isRecord(data)
            ? data
            : {};

        return {
            academicYears: firstArrayProperty(
                source,
                'academicYears',
                'academicYearOptions'
            ),
            academicTerms: firstArrayProperty(
                source,
                'academicTerms',
                'academicTermOptions',
                'terms',
                'termOptions'
            ),
            levels: firstArrayProperty(
                source,
                'levels',
                'levelOptions'
            ),
            classes: firstArrayProperty(
                source,
                'classes',
                'classOptions'
            ),
            previousClasses: [],
            sections: firstArrayProperty(
                source,
                'sections',
                'sectionOptions'
            ),
            hostels: firstArrayProperty(
                source,
                'hostels',
                'hostelOptions'
            ),
            hostelRooms: firstArrayProperty(
                source,
                'hostelRooms',
                'rooms',
                'roomOptions'
            ),
            hostelBeds: firstArrayProperty(
                source,
                'hostelBeds',
                'beds',
                'bedOptions'
            ),
            transportRoutes: firstArrayProperty(
                source,
                'transportRoutes',
                'routes',
                'routeOptions'
            ),
            vehicles: firstArrayProperty(
                source,
                'vehicles',
                'vehicleOptions'
            ),
            pickupPoints: firstArrayProperty(
                source,
                'pickupPoints',
                'pickupPointOptions'
            )
        };
    }

    function populateAcademicYears(view, state) {
        const select = view.querySelector(
            '#add-studentAcademicYear'
        );

        if (!(select instanceof HTMLSelectElement)) {
            return;
        }

        replaceSelectOptions(
            select,
            '-- Select Academic Year --'
        );

        const years = [...state.referenceData.academicYears]
            .sort((first, second) => {
                const firstStart = String(
                    readProperty(first, 'startDate') || ''
                );
                const secondStart = String(
                    readProperty(second, 'startDate') || ''
                );

                return secondStart.localeCompare(
                    firstStart
                );
            });

        years.forEach(year => {
            const academicYearId = firstDefined(
                readProperty(year, 'academicYearId'),
                readProperty(year, 'id')
            );

            if (!isPositiveInteger(academicYearId)) {
                return;
            }

            const label = firstNonBlank(
                readProperty(year, 'academicYearName'),
                readProperty(year, 'name'),
                readProperty(year, 'academicYearCode'),
                readProperty(year, 'code'),
                String(academicYearId)
            );

            const option = new Option(
                label,
                String(academicYearId)
            );

            option.dataset.academicYearCode =
                firstNonBlank(
                    readProperty(year, 'academicYearCode'),
                    readProperty(year, 'code'),
                    label
                );

            option.dataset.startDate = String(
                readProperty(year, 'startDate') || ''
            );
            option.dataset.endDate = String(
                readProperty(year, 'endDate') || ''
            );

            if (
                readProperty(year, 'currentYear') === true ||
                String(readProperty(year, 'status') || '').toUpperCase() === 'ACTIVE'
            ) {
                option.dataset.currentYear = 'true';
            }

            select.appendChild(option);
        });

        const currentOption = Array.from(
            select.options
        ).find(option =>
            option.dataset.currentYear === 'true'
        );

        if (currentOption) {
            select.value = currentOption.value;
            synchronizeAdmissionYearFromAcademicYear(
                view,
                currentOption
            );
        }
    }

    function populateJoiningTerms(view, state) {
        const select = view.querySelector(
            '#add-studentJoiningTerm'
        );

        if (!(select instanceof HTMLSelectElement)) {
            return;
        }

        const previousValue = select.value;
        const admissionYear = integerOrNull(
            valueOf(view, '#add-studentAdmissionYear')
        );

        replaceSelectOptions(
            select,
            '-- Select Joining Term --'
        );

        if (admissionYear === null) {
            select.disabled = true;
            return;
        }

        const matchingYear = state.referenceData.academicYears
            .find(year => {
                const startDate = String(
                    readProperty(year, 'startDate') || ''
                );
                const code = String(
                    firstDefined(
                        readProperty(year, 'academicYearCode'),
                        readProperty(year, 'code'),
                        ''
                    )
                );
                const name = String(
                    firstDefined(
                        readProperty(year, 'academicYearName'),
                        readProperty(year, 'name'),
                        ''
                    )
                );

                return startDate.startsWith(
                    `${admissionYear}-`
                ) || code.includes(
                    String(admissionYear)
                ) || name.includes(
                    String(admissionYear)
                );
            });

        const academicYearId = positiveIntegerOrNull(
            firstDefined(
                readProperty(matchingYear, 'academicYearId'),
                readProperty(matchingYear, 'id')
            )
        );

        if (academicYearId === null) {
            select.disabled = true;
            return;
        }

        const terms = [...state.referenceData.academicTerms]
            .filter(term =>
                positiveIntegerOrNull(
                    firstDefined(
                        readProperty(term, 'academicYearId'),
                        readNestedProperty(
                            term,
                            'academicYear',
                            'academicYearId'
                        )
                    )
                ) === academicYearId
            )
            .sort((first, second) =>
                Number(
                    readProperty(first, 'displayOrder') || 0
                ) - Number(
                    readProperty(second, 'displayOrder') || 0
                )
            );

        terms.forEach(term => {
            const termId = firstDefined(
                readProperty(term, 'termId'),
                readProperty(term, 'id')
            );

            if (!isPositiveInteger(termId)) {
                return;
            }

            const label = firstNonBlank(
                readProperty(term, 'termName'),
                readProperty(term, 'name'),
                readProperty(term, 'termCode'),
                readProperty(term, 'code'),
                String(termId)
            );

            select.appendChild(
                new Option(
                    label,
                    String(termId)
                )
            );
        });

        select.disabled = terms.length === 0;

        if (
            previousValue &&
            Array.from(select.options).some(
                option => option.value === previousValue
            )
        ) {
            select.value = previousValue;
        }
    }

    function populateCurrentClasses(view, state) {
        const select = view.querySelector(
            '#add-studentClass'
        );

        if (!(select instanceof HTMLSelectElement)) {
            return;
        }

        replaceSelectOptions(
            select,
            '-- Select Class --'
        );

        sortClasses(
            state.referenceData.classes
        ).forEach(classItem => {
            const classId = firstDefined(
                readProperty(classItem, 'classId'),
                readProperty(classItem, 'id')
            );

            if (!isPositiveInteger(classId)) {
                return;
            }

            const classCode = firstNonBlank(
                readProperty(classItem, 'classCode'),
                readProperty(classItem, 'code')
            );
            const className = firstNonBlank(
                readProperty(classItem, 'className'),
                readProperty(classItem, 'name'),
                classCode,
                String(classId)
            );
            const optionLabel = classCode
                ? `[${classCode}] ${className}`
                : className;
            const option = new Option(
                optionLabel,
                String(classId)
            );

            option.dataset.classCode = classCode;
            option.dataset.className = className;
            option.dataset.levelId = String(
                firstDefined(
                    readProperty(classItem, 'levelId'),
                    readNestedProperty(classItem, 'level', 'levelId'),
                    ''
                )
            );

            select.appendChild(option);
        });
    }

    function populatePreviousLevels(view, state) {
        const select = view.querySelector(
            '#add-studentPreviousLevel'
        );

        if (!(select instanceof HTMLSelectElement)) {
            return;
        }

        replaceSelectOptions(
            select,
            '-- Select Previous Level --'
        );

        const levels = [...state.referenceData.levels]
            .sort((first, second) => {
                const firstOrder = Number(
                    readProperty(first, 'displayOrder') || 0
                );
                const secondOrder = Number(
                    readProperty(second, 'displayOrder') || 0
                );

                if (firstOrder !== secondOrder) {
                    return firstOrder - secondOrder;
                }

                return firstNonBlank(
                    first.levelName,
                    readProperty(first, 'name')
                ).localeCompare(
                    firstNonBlank(
                        second.levelName,
                        readProperty(second, 'name')
                    )
                );
            });

        levels.forEach(level => {
            const levelId = firstDefined(
                readProperty(level, 'levelId'),
                readProperty(level, 'id')
            );

            if (!isPositiveInteger(levelId)) {
                return;
            }

            select.appendChild(
                new Option(
                    firstNonBlank(
                        readProperty(level, 'levelName'),
                        readProperty(level, 'name'),
                        String(levelId)
                    ),
                    String(levelId)
                )
            );
        });
    }

    function populateHostelOptions(view, state) {
        const hostelSelect = view.querySelector(
            '#add-studentHostelId'
        );

        if (!(hostelSelect instanceof HTMLSelectElement)) {
            return;
        }

        replaceSelectOptions(
            hostelSelect,
            '-- Select Hostel --'
        );

        state.referenceData.hostels.forEach(hostel => {
            const hostelId = firstDefined(
                readProperty(hostel, 'hostelId'),
                readProperty(hostel, 'id')
            );

            if (!isPositiveInteger(hostelId)) {
                return;
            }

            hostelSelect.appendChild(
                new Option(
                    firstNonBlank(
                        readProperty(hostel, 'hostelName'),
                        readProperty(hostel, 'name'),
                        readProperty(hostel, 'hostelCode'),
                        String(hostelId)
                    ),
                    String(hostelId)
                )
            );
        });
    }

    function populateTransportOptions(view, state) {
        const routeSelect = view.querySelector(
            '#add-studentTransportRouteId'
        );
        const vehicleSelect = view.querySelector(
            '#add-studentTransportVehicleId'
        );

        if (routeSelect instanceof HTMLSelectElement) {
            replaceSelectOptions(
                routeSelect,
                '-- Select Route --'
            );

            state.referenceData.transportRoutes
                .forEach(route => {
                    const routeId = firstDefined(
                        readProperty(route, 'routeId'),
                        readProperty(route, 'id')
                    );

                    if (!isPositiveInteger(routeId)) {
                        return;
                    }

                    routeSelect.appendChild(
                        new Option(
                            firstNonBlank(
                                readProperty(route, 'routeName'),
                                readProperty(route, 'name'),
                                readProperty(route, 'routeCode'),
                                String(routeId)
                            ),
                            String(routeId)
                        )
                    );
                });
        }

        if (vehicleSelect instanceof HTMLSelectElement) {
            replaceSelectOptions(
                vehicleSelect,
                '-- Select Vehicle --'
            );

            state.referenceData.vehicles
                .forEach(vehicle => {
                    const vehicleId = firstDefined(
                        readProperty(vehicle, 'vehicleId'),
                        readProperty(vehicle, 'id')
                    );

                    if (!isPositiveInteger(vehicleId)) {
                        return;
                    }

                    vehicleSelect.appendChild(
                        new Option(
                            firstNonBlank(
                                readProperty(vehicle, 'vehicleNumber'),
                                readProperty(vehicle, 'registrationNumber'),
                                readProperty(vehicle, 'name'),
                                String(vehicleId)
                            ),
                            String(vehicleId)
                        )
                    );
                });
        }
    }

    function bindCurrentPlacement(view, state) {
        const academicYearSelect = view.querySelector(
            '#add-studentAcademicYear'
        );
        const classSelect = view.querySelector(
            '#add-studentClass'
        );

        const admissionYearInput = view.querySelector(
            '#add-studentAdmissionYear'
        );

        academicYearSelect?.addEventListener(
            'change',
            () => {
                const option = selectedOption(
                    academicYearSelect
                );

                synchronizeAdmissionYearFromAcademicYear(
                    view,
                    option
                );
                populateJoiningTerms(
                    view,
                    state
                );
                populateCurrentSections(
                    view,
                    state
                );
            }
        );

        admissionYearInput?.addEventListener(
            'input',
            () => {
                populateJoiningTerms(
                    view,
                    state
                );
            }
        );

        classSelect?.addEventListener(
            'change',
            () => {
                populateCurrentSections(
                    view,
                    state
                );
            }
        );
    }

    function populateCurrentSections(view, state) {
        const academicYearId = positiveIntegerOrNull(
            valueOf(view, '#add-studentAcademicYear')
        );
        const classId = positiveIntegerOrNull(
            valueOf(view, '#add-studentClass')
        );
        const select = view.querySelector(
            '#add-studentSection'
        );

        if (!(select instanceof HTMLSelectElement)) {
            return;
        }

        replaceSelectOptions(
            select,
            '-- Select Section --'
        );

        const sections = state.referenceData.sections
            .filter(section => {
                const sectionYearId = positiveIntegerOrNull(
                    firstDefined(
                        readProperty(section, 'academicYearId'),
                        readNestedProperty(section, 'academicYear', 'academicYearId')
                    )
                );
                const sectionClassId = positiveIntegerOrNull(
                    firstDefined(
                        readProperty(section, 'classId'),
                        readNestedProperty(section, 'schoolClass', 'classId')
                    )
                );

                return (
                    academicYearId !== null &&
                    classId !== null &&
                    sectionYearId === academicYearId &&
                    sectionClassId === classId
                );
            });

        sections.forEach(section => {
            const sectionId = firstDefined(
                readProperty(section, 'sectionId'),
                readProperty(section, 'id')
            );

            if (!isPositiveInteger(sectionId)) {
                return;
            }

            select.appendChild(
                new Option(
                    firstNonBlank(
                        readProperty(section, 'sectionName'),
                        readProperty(section, 'name'),
                        readProperty(section, 'sectionCode'),
                        String(sectionId)
                    ),
                    String(sectionId)
                )
            );
        });

        select.disabled = sections.length === 0;
    }

    function synchronizeAdmissionYearFromAcademicYear(
        view,
        option
    ) {
        if (!(option instanceof HTMLOptionElement)) {
            return;
        }

        const yearInput = view.querySelector(
            '#add-studentAdmissionYear'
        );

        if (!(yearInput instanceof HTMLInputElement)) {
            return;
        }

        const startDate = String(
            option.dataset.startDate || ''
        );
        const year = Number.parseInt(
            startDate.slice(0, 4),
            10
        );

        if (Number.isInteger(year)) {
            yearInput.value = String(year);
        }
    }

    function bindPreviousEducation(view, state) {
        const levelSelect = view.querySelector(
            '#add-studentPreviousLevel'
        );
        const classSelect = view.querySelector(
            '#add-studentPreviousClass'
        );

        levelSelect?.addEventListener(
            'change',
            () => {
                populatePreviousClasses(
                    view,
                    state
                );
                updatePreviousClassSections(view);
            }
        );

        classSelect?.addEventListener(
            'change',
            () => {
                updatePreviousClassSections(view);
            }
        );
    }

    function populatePreviousClasses(view, state) {
        const levelId = positiveIntegerOrNull(
            valueOf(view, '#add-studentPreviousLevel')
        );
        const classSelect = view.querySelector(
            '#add-studentPreviousClass'
        );

        if (!(classSelect instanceof HTMLSelectElement)) {
            return;
        }

        replaceSelectOptions(
            classSelect,
            levelId === null
                ? '-- Select Previous Level First --'
                : '-- Select Previous Class --'
        );

        if (levelId === null) {
            classSelect.disabled = true;
            return;
        }

        const matchingClasses = sortClasses(
            state.referenceData.previousClasses.filter(classItem => {
                return positiveIntegerOrNull(
                    firstDefined(
                        readProperty(classItem, 'levelId'),
                        readNestedProperty(classItem, 'level', 'levelId')
                    )
                ) === levelId;
            })
        );

        matchingClasses.forEach(classItem => {
            const className = firstNonBlank(
                readProperty(classItem, 'className'),
                readProperty(classItem, 'name'),
                readProperty(classItem, 'classCode'),
                String(
                    firstDefined(
                        readProperty(classItem, 'classId'),
                        readProperty(classItem, 'id'),
                        ''
                    )
                )
            );
            const classCode = firstNonBlank(
                readProperty(classItem, 'classCode'),
                readProperty(classItem, 'code')
            );
            const option = new Option(
                classCode
                    ? `[${classCode}] ${className}`
                    : className,
                className
            );

            option.dataset.classCode = classCode;
            option.dataset.className = className;
            classSelect.appendChild(option);
        });

        classSelect.disabled = matchingClasses.length === 0;
    }

    function updatePreviousClassSections(view) {
        const classSelect = view.querySelector(
            '#add-studentPreviousClass'
        );
        const option = selectedOption(classSelect);
        const classIdentity = normalizeClassIdentity(
            [
                option?.dataset.classCode,
                option?.dataset.className,
                option?.textContent
            ].filter(Boolean).join(' ')
        );

        const pleFields = view.querySelector(
            '#add-studentPleResultFields'
        );
        const uceFields = view.querySelector(
            '#add-studentUceResultFields'
        );
        const uaceFields = view.querySelector(
            '#add-studentUaceResultFields'
        );
        const subjectSection = view.querySelector(
            '#add-studentSubjectResultsSection'
        );

        const isPle = matchesClassIdentity(
            classIdentity,
            ['P7', 'PRIMARY7', 'PRIMARYSEVEN']
        );
        const isUce = matchesClassIdentity(
            classIdentity,
            ['S4', 'SENIOR4', 'SENIORFOUR']
        );
        const isUace = matchesClassIdentity(
            classIdentity,
            ['S6', 'SENIOR6', 'SENIORSIX']
        );
        const hasPreviousClass = Boolean(
            classSelect?.value
        );

        toggleHidden(pleFields, !isPle);
        toggleHidden(uceFields, !isUce);
        toggleHidden(uaceFields, !isUace);
        toggleHidden(
            subjectSection,
            !hasPreviousClass
        );

        if (!isPle) {
            clearValues(
                view,
                [
                    '#add-studentPleIndexNumber',
                    '#add-studentPleAggregate'
                ]
            );
        }

        if (!isUce) {
            clearValues(
                view,
                [
                    '#add-studentUceIndexNumber',
                    '#add-studentUceResult'
                ]
            );
        }

        if (!isUace) {
            clearValues(
                view,
                [
                    '#add-studentUaceIndexNumber',
                    '#add-studentUaceResult'
                ]
            );
        }
    }

    function bindConditionalSections(view) {
        const hostelCheckbox = view.querySelector(
            '#add-studentRequiresHostel'
        );
        const transportCheckbox = view.querySelector(
            '#add-studentRequiresTransport'
        );
        const hostelSelect = view.querySelector(
            '#add-studentHostelId'
        );
        const roomSelect = view.querySelector(
            '#add-studentHostelRoomId'
        );
        const routeSelect = view.querySelector(
            '#add-studentTransportRouteId'
        );

        hostelCheckbox?.addEventListener(
            'change',
            () => synchronizeHostelSection(view)
        );

        transportCheckbox?.addEventListener(
            'change',
            () => synchronizeTransportSection(view)
        );

        hostelSelect?.addEventListener(
            'change',
            () => populateHostelRooms(view)
        );

        roomSelect?.addEventListener(
            'change',
            () => populateHostelBeds(view)
        );

        routeSelect?.addEventListener(
            'change',
            () => populatePickupPoints(view)
        );
    }

    function synchronizeHostelSection(view) {
        const checked = Boolean(
            view.querySelector(
                '#add-studentRequiresHostel'
            )?.checked
        );
        const fields = view.querySelector(
            '#add-studentHostelFields'
        );

        toggleHidden(fields, !checked);
        setConditionalRequired(
            view,
            'hostel',
            checked
        );

        if (checked) {
            copyJoiningDateIfBlank(
                view,
                '#add-studentHostelStartDate'
            );
        } else {
            clearValues(
                view,
                [
                    '#add-studentHostelId',
                    '#add-studentHostelRoomId',
                    '#add-studentHostelBedId',
                    '#add-studentHostelStartDate',
                    '#add-studentHostelEndDate',
                    '#add-studentHostelGuardianName',
                    '#add-studentHostelGuardianMobile',
                    '#add-studentHostelGuardianRelation',
                    '#add-studentHostelRemarks'
                ]
            );
        }
    }

    function synchronizeTransportSection(view) {
        const checked = Boolean(
            view.querySelector(
                '#add-studentRequiresTransport'
            )?.checked
        );
        const fields = view.querySelector(
            '#add-studentTransportFields'
        );

        toggleHidden(fields, !checked);
        setConditionalRequired(
            view,
            'transport',
            checked
        );

        if (checked) {
            copyJoiningDateIfBlank(
                view,
                '#add-studentTransportStartDate'
            );
        } else {
            clearValues(
                view,
                [
                    '#add-studentTransportRouteId',
                    '#add-studentTransportPickupPointId',
                    '#add-studentTransportVehicleId',
                    '#add-studentTransportStartDate',
                    '#add-studentTransportEndDate',
                    '#add-studentTransportSeatNumber',
                    '#add-studentTransportEmergencyContact',
                    '#add-studentTransportEmergencyMobile',
                    '#add-studentTransportRemarks'
                ]
            );
        }
    }

    function populateHostelRooms(view) {
        const state = getViewState(view);

        if (!state) {
            return;
        }

        const hostelId = positiveIntegerOrNull(
            valueOf(view, '#add-studentHostelId')
        );
        const select = view.querySelector(
            '#add-studentHostelRoomId'
        );

        if (!(select instanceof HTMLSelectElement)) {
            return;
        }

        replaceSelectOptions(
            select,
            '-- Select Room --'
        );

        state.referenceData.hostelRooms
            .filter(room => {
                const roomHostelId = positiveIntegerOrNull(
                    firstDefined(
                        readProperty(room, 'hostelId'),
                        readNestedProperty(room, 'hostel', 'hostelId')
                    )
                );

                return (
                    hostelId !== null &&
                    roomHostelId === hostelId
                );
            })
            .forEach(room => {
                const roomId = firstDefined(
                    readProperty(room, 'roomId'),
                    readProperty(room, 'id')
                );

                if (!isPositiveInteger(roomId)) {
                    return;
                }

                select.appendChild(
                    new Option(
                        firstNonBlank(
                            readProperty(room, 'roomName'),
                            readProperty(room, 'roomNumber'),
                            readProperty(room, 'name'),
                            String(roomId)
                        ),
                        String(roomId)
                    )
                );
            });

        populateHostelBeds(view);
    }

    function populateHostelBeds(view) {
        const state = getViewState(view);

        if (!state) {
            return;
        }

        const roomId = positiveIntegerOrNull(
            valueOf(view, '#add-studentHostelRoomId')
        );
        const select = view.querySelector(
            '#add-studentHostelBedId'
        );

        if (!(select instanceof HTMLSelectElement)) {
            return;
        }

        replaceSelectOptions(
            select,
            '-- Select Bed --'
        );

        state.referenceData.hostelBeds
            .filter(bed => {
                const bedRoomId = positiveIntegerOrNull(
                    firstDefined(
                        readProperty(bed, 'roomId'),
                        readNestedProperty(bed, 'room', 'roomId')
                    )
                );

                return (
                    roomId !== null &&
                    bedRoomId === roomId
                );
            })
            .forEach(bed => {
                const bedId = firstDefined(
                    readProperty(bed, 'bedId'),
                    readProperty(bed, 'id')
                );

                if (!isPositiveInteger(bedId)) {
                    return;
                }

                select.appendChild(
                    new Option(
                        firstNonBlank(
                            readProperty(bed, 'bedName'),
                            readProperty(bed, 'bedNumber'),
                            readProperty(bed, 'name'),
                            String(bedId)
                        ),
                        String(bedId)
                    )
                );
            });
    }

    function populatePickupPoints(view) {
        const state = getViewState(view);

        if (!state) {
            return;
        }

        const routeId = positiveIntegerOrNull(
            valueOf(view, '#add-studentTransportRouteId')
        );
        const select = view.querySelector(
            '#add-studentTransportPickupPointId'
        );

        if (!(select instanceof HTMLSelectElement)) {
            return;
        }

        replaceSelectOptions(
            select,
            '-- Select Pickup Point --'
        );

        state.referenceData.pickupPoints
            .filter(point => {
                const pointRouteId = positiveIntegerOrNull(
                    firstDefined(
                        readProperty(point, 'routeId'),
                        readNestedProperty(point, 'route', 'routeId')
                    )
                );

                return (
                    routeId !== null &&
                    pointRouteId === routeId
                );
            })
            .forEach(point => {
                const pointId = firstDefined(
                    readProperty(point, 'pickupPointId'),
                    readProperty(point, 'id')
                );

                if (!isPositiveInteger(pointId)) {
                    return;
                }

                select.appendChild(
                    new Option(
                        firstNonBlank(
                            readProperty(point, 'pickupPointName'),
                            readProperty(point, 'name'),
                            String(pointId)
                        ),
                        String(pointId)
                    )
                );
            });
    }

    function getViewState(view) {
        return STUDENT_VIEW_STATES.get(view) || null;
    }

    function bindSubjectResults(view) {
        const addButton = view.querySelector(
            '#add-studentAddSubjectBtn'
        );
        const body = view.querySelector(
            '#add-studentSubjectResultsBody'
        );

        addButton?.addEventListener(
            'click',
            () => addSubjectRow(view)
        );

        body?.addEventListener(
            'click',
            event => {
                const button = event.target.closest(
                    '.student-subject-remove-btn'
                );

                if (!(button instanceof HTMLButtonElement)) {
                    return;
                }

                const row = button.closest(
                    '.student-subject-result-row'
                );
                const rows = body.querySelectorAll(
                    '.student-subject-result-row'
                );

                if (!(row instanceof HTMLTableRowElement)) {
                    return;
                }

                if (rows.length <= 1) {
                    row.querySelectorAll('input')
                        .forEach(input => {
                            input.value = '';
                            clearFieldError(input);
                        });
                } else {
                    row.parentNode.removeChild(row);
                }

                updateSubjectSummary(view);
            }
        );

        body?.addEventListener(
            'input',
            event => {
                if (event.target instanceof HTMLInputElement) {
                    clearFieldError(event.target);
                }

                updateSubjectSummary(view);
            }
        );
    }

    function addSubjectRow(view) {
        const template = view.querySelector(
            '#add-studentSubjectRowTemplate'
        );
        const body = view.querySelector(
            '#add-studentSubjectResultsBody'
        );

        if (
            !(template instanceof HTMLTemplateElement) ||
            !(body instanceof HTMLTableSectionElement)
        ) {
            return;
        }

        body.appendChild(
            template.content.cloneNode(true)
        );

        const rows = body.querySelectorAll(
            '.student-subject-result-row'
        );
        const lastRow = rows.item(rows.length - 1);
        const subjectNameInput = lastRow?.querySelector(
            '.student-subject-name'
        );

        if (subjectNameInput instanceof HTMLInputElement) {
            subjectNameInput.focus();
        }

        updateSubjectSummary(view);
    }

    function collectSubjectResults(view, validateRows) {
        const rows = Array.from(
            view.querySelectorAll(
                '#add-studentSubjectResultsBody .student-subject-result-row'
            )
        );
        const results = [];
        const errors = [];

        rows.forEach((row, index) => {
            const nameInput = row.querySelector(
                '.student-subject-name'
            );
            const markInput = row.querySelector(
                '.student-subject-mark'
            );
            const gradeInput = row.querySelector(
                '.student-subject-grade'
            );

            const subjectName = trimmedValue(nameInput);
            const markText = trimmedValue(markInput);
            const grade = trimmedValue(gradeInput);
            const hasAnyValue = Boolean(
                subjectName || markText || grade
            );

            if (!hasAnyValue) {
                return;
            }

            const rowNumber = index + 1;
            const marks = markText === ''
                ? null
                : Number(markText);

            if (validateRows) {
                if (!subjectName) {
                    const message =
                        `Subject ${rowNumber}: enter the subject name.`;
                    setFieldError(nameInput, message);
                    errors.push(message);
                }

                if (
                    marks === null ||
                    !Number.isFinite(marks) ||
                    marks < 0 ||
                    marks > 100
                ) {
                    const message =
                        `Subject ${rowNumber}: enter marks between 0 and 100.`;
                    setFieldError(markInput, message);
                    errors.push(message);
                }

                if (!grade) {
                    const message =
                        `Subject ${rowNumber}: enter the grade.`;
                    setFieldError(gradeInput, message);
                    errors.push(message);
                }
            }

            if (
                subjectName &&
                marks !== null &&
                Number.isFinite(marks) &&
                marks >= 0 &&
                marks <= 100 &&
                grade
            ) {
                results.push({
                    subjectName,
                    marks,
                    grade
                });
            }
        });

        return {
            results,
            errors
        };
    }

    function updateSubjectSummary(view) {
        const collected = collectSubjectResults(
            view,
            false
        );
        const subjectCount = view.querySelector(
            '#add-studentSubjectCount'
        );
        const totalMarks = view.querySelector(
            '#add-studentSubjectTotal'
        );
        const hiddenField = view.querySelector(
            '#add-studentSubjectMarks'
        );
        const total = collected.results.reduce(
            (sum, result) => sum + result.marks,
            0
        );

        if (subjectCount) {
            subjectCount.textContent = String(
                collected.results.length
            );
        }

        if (totalMarks) {
            totalMarks.textContent = formatDecimal(total);
        }

        if (hiddenField instanceof HTMLTextAreaElement) {
            hiddenField.value = collected.results.length > 0
                ? JSON.stringify(collected.results)
                : '';
        }
    }

    function bindPhotoPreview(view) {
        const input = view.querySelector(
            '#add-studentProfilePhoto'
        );

        input?.addEventListener(
            'change',
            () => updatePhotoPreview(view)
        );
    }

    function updatePhotoPreview(view) {
        const input = view.querySelector(
            '#add-studentProfilePhoto'
        );
        const preview = view.querySelector(
            '#add-studentProfilePhotoPreview'
        );
        const placeholder = view.querySelector(
            '#add-studentProfilePhotoPlaceholder'
        );
        const name = view.querySelector(
            '#add-studentProfilePhotoName'
        );
        const photo = input?.files?.[0];

        if (!photo) {
            resetPhotoPreview(view);
            return;
        }

        if (!ALLOWED_PHOTO_TYPES.has(photo.type)) {
            input.value = '';
            resetPhotoPreview(view);
            showError(
                'Select a JPG, PNG or WEBP Student photo.'
            );
            return;
        }

        if (photo.size > MAX_PHOTO_BYTES) {
            input.value = '';
            resetPhotoPreview(view);
            showError(
                'Student photo must not exceed 2 MB.'
            );
            return;
        }

        const reader = new FileReader();

        reader.addEventListener(
            'load',
            () => {
                if (preview instanceof HTMLImageElement) {
                    preview.src = String(reader.result || '');
                    removeClasses(preview, 'hidden');
                }

                addClasses(placeholder, 'hidden');

                if (name) {
                    name.textContent = photo.name;
                }

                clearFieldError(input);
            }
        );

        reader.addEventListener(
            'error',
            () => {
                input.value = '';
                resetPhotoPreview(view);
                showError(
                    'The selected Student photo could not be read.'
                );
            }
        );

        reader.readAsDataURL(photo);
    }

    function resetPhotoPreview(view) {
        const preview = view.querySelector(
            '#add-studentProfilePhotoPreview'
        );
        const placeholder = view.querySelector(
            '#add-studentProfilePhotoPlaceholder'
        );
        const name = view.querySelector(
            '#add-studentProfilePhotoName'
        );

        if (preview instanceof HTMLImageElement) {
            preview.removeAttribute('src');
            addClasses(preview, 'hidden');
        }

        removeClasses(placeholder, 'hidden');

        if (name) {
            name.textContent = 'No file chosen';
        }
    }

    function bindLiveValidationClearing(view) {
        view.addEventListener(
            'input',
            event => {
                if (
                    event.target instanceof HTMLInputElement ||
                    event.target instanceof HTMLSelectElement ||
                    event.target instanceof HTMLTextAreaElement
                ) {
                    clearFieldError(event.target);
                }
            }
        );

        view.addEventListener(
            'change',
            event => {
                if (
                    event.target instanceof HTMLInputElement ||
                    event.target instanceof HTMLSelectElement ||
                    event.target instanceof HTMLTextAreaElement
                ) {
                    clearFieldError(event.target);
                }
            }
        );
    }

    function validateStudentForm(view) {
        clearAllValidationErrors(view);

        const errors = [];
        const requiredFields = Array.from(
            view.querySelectorAll(
                '[required]:not([disabled])'
            )
        );
        const processedRadioNames = new Set();

        requiredFields.forEach(field => {
            if (
                field instanceof HTMLInputElement &&
                field.type === 'radio'
            ) {
                if (processedRadioNames.has(field.name)) {
                    return;
                }

                processedRadioNames.add(field.name);

                const selected = view.querySelector(
                    `[name="${cssEscape(field.name)}"]:checked`
                );

                if (!selected) {
                    const label = validationLabel(field);
                    const message = `${label} is required.`;
                    setFieldError(field, message);
                    errors.push(message);
                }

                return;
            }

            if (!trimmedValue(field)) {
                const label = validationLabel(field);
                const message = `${label} is required.`;
                setFieldError(field, message);
                errors.push(message);
            }
        });

        validateDateOfBirth(view, errors);
        validateJoiningDate(view, errors);
        validateParentContacts(view, errors);
        validateEmergencyContact(view, errors);
        validateHostel(view, errors);
        validateTransport(view, errors);
        validatePhoto(view, errors);

        const subjectValidation = collectSubjectResults(
            view,
            true
        );

        errors.push(...subjectValidation.errors);
        updateSubjectSummary(view);

        if (errors.length > 0) {
            showValidationSummary(view, errors);

            const firstInvalid = view.querySelector(
                '.emp-input-invalid'
            );

            firstInvalid?.scrollIntoView({
                behavior: 'smooth',
                block: 'center'
            });
            firstInvalid?.focus();

            return false;
        }

        hideValidationSummary(view);
        return true;
    }

    function validateDateOfBirth(view, errors) {
        const field = view.querySelector(
            '#add-studentDob'
        );
        const value = trimmedValue(field);

        if (!value) {
            return;
        }

        const today = todayIso();

        if (value >= today) {
            const message =
                'Date of Birth must be before today.';
            setFieldError(field, message);
            errors.push(message);
        }
    }

    function validateJoiningDate(view, errors) {
        const field = view.querySelector(
            '#add-studentJoiningDate'
        );
        const value = trimmedValue(field);

        if (value && value > todayIso()) {
            const message =
                'Joining Date cannot be in the future.';
            setFieldError(field, message);
            errors.push(message);
        }
    }

    function validateParentContacts(view, errors) {
        const contacts = {
            FATHER: {
                name: view.querySelector(
                    '#add-studentFatherName'
                ),
                phone: view.querySelector(
                    '#add-studentFatherPhone'
                )
            },
            MOTHER: {
                name: view.querySelector(
                    '#add-studentMotherName'
                ),
                phone: view.querySelector(
                    '#add-studentMotherPhone'
                )
            },
            GUARDIAN: {
                name: view.querySelector(
                    '#add-studentGuardianName'
                ),
                phone: view.querySelector(
                    '#add-studentGuardianPhone'
                )
            }
        };

        const hasCompleteContact = Object.values(contacts)
            .some(contact =>
                Boolean(
                    trimmedValue(contact.name) &&
                    trimmedValue(contact.phone)
                )
            );

        if (!hasCompleteContact) {
            const message =
                'Enter a name and phone number for at least one Father, Mother or Guardian.';
            setFieldError(
                contacts.FATHER.name,
                message
            );
            setFieldError(
                contacts.FATHER.phone,
                message
            );
            errors.push(message);
        }

        Object.entries(contacts).forEach(
            ([type, contact]) => {
                const hasName = Boolean(
                    trimmedValue(contact.name)
                );
                const hasPhone = Boolean(
                    trimmedValue(contact.phone)
                );

                if (hasName !== hasPhone) {
                    const label = formatEnum(type);
                    const message =
                        `${label} name and phone must be entered together.`;

                    if (!hasName) {
                        setFieldError(
                            contact.name,
                            message
                        );
                    }

                    if (!hasPhone) {
                        setFieldError(
                            contact.phone,
                            message
                        );
                    }

                    errors.push(message);
                }
            }
        );

        const preferred = checkedValue(
            view,
            'add-studentPreferredContact'
        );

        if (preferred && contacts[preferred]) {
            const contact = contacts[preferred];

            if (
                !trimmedValue(contact.name) ||
                !trimmedValue(contact.phone)
            ) {
                const field = view.querySelector(
                    `[name="add-studentPreferredContact"][value="${cssEscape(preferred)}"]`
                );
                const message =
                    'The selected preferred contact must have a name and phone number.';
                setFieldError(field, message);
                errors.push(message);
            }
        }

        const guardianHasAny = [
            '#add-studentGuardianName',
            '#add-studentGuardianUin',
            '#add-studentGuardianRelationship',
            '#add-studentGuardianPhone',
            '#add-studentGuardianAltPhone',
            '#add-studentGuardianEmail',
            '#add-studentGuardianOccupation'
        ].some(selector => Boolean(valueOf(view, selector)));

        if (
            guardianHasAny &&
            !valueOf(view, '#add-studentGuardianRelationship')
        ) {
            const field = view.querySelector(
                '#add-studentGuardianRelationship'
            );
            const message =
                'Guardian relationship is required when Guardian details are entered.';
            setFieldError(field, message);
            errors.push(message);
        }
    }

    function validateEmergencyContact(view, errors) {
        validateCompleteGroup(
            [
                view.querySelector(
                    '#add-studentEmergencyName'
                ),
                view.querySelector(
                    '#add-studentEmergencyPhone'
                ),
                view.querySelector(
                    '#add-studentEmergencyRelationship'
                )
            ],
            'Emergency contact name, phone and relationship must be entered together.',
            errors
        );
    }

    function validateHostel(view, errors) {
        const enabled = Boolean(
            view.querySelector(
                '#add-studentRequiresHostel'
            )?.checked
        );

        if (!enabled) {
            return;
        }

        validateRequiredSelector(
            view,
            '#add-studentHostelId',
            'Hostel is required.',
            errors
        );
        validateRequiredSelector(
            view,
            '#add-studentHostelStartDate',
            'Hostel Allocation Start Date is required.',
            errors
        );

        const roomId = valueOf(
            view,
            '#add-studentHostelRoomId'
        );
        const bedId = valueOf(
            view,
            '#add-studentHostelBedId'
        );

        if (bedId && !roomId) {
            const field = view.querySelector(
                '#add-studentHostelRoomId'
            );
            const message =
                'Hostel Room is required when a bed is selected.';
            setFieldError(field, message);
            errors.push(message);
        }

        validateDateRange(
            view,
            '#add-studentHostelStartDate',
            '#add-studentHostelEndDate',
            'Hostel allocation end date cannot be earlier than the start date.',
            errors
        );

        validateStartAgainstJoiningDate(
            view,
            '#add-studentHostelStartDate',
            'Hostel allocation cannot start before the Student joining date.',
            errors
        );

        validateCompleteGroup(
            [
                view.querySelector(
                    '#add-studentHostelGuardianName'
                ),
                view.querySelector(
                    '#add-studentHostelGuardianMobile'
                ),
                view.querySelector(
                    '#add-studentHostelGuardianRelation'
                )
            ],
            'Local guardian name, mobile and relationship must be entered together.',
            errors
        );
    }

    function validateTransport(view, errors) {
        const enabled = Boolean(
            view.querySelector(
                '#add-studentRequiresTransport'
            )?.checked
        );

        if (!enabled) {
            return;
        }

        validateRequiredSelector(
            view,
            '#add-studentTransportRouteId',
            'Transport Route is required.',
            errors
        );
        validateRequiredSelector(
            view,
            '#add-studentTransportStartDate',
            'Transport Start Date is required.',
            errors
        );

        validateDateRange(
            view,
            '#add-studentTransportStartDate',
            '#add-studentTransportEndDate',
            'Transport end date cannot be earlier than the start date.',
            errors
        );

        validateStartAgainstJoiningDate(
            view,
            '#add-studentTransportStartDate',
            'Transport cannot start before the Student joining date.',
            errors
        );

        validateCompleteGroup(
            [
                view.querySelector(
                    '#add-studentTransportEmergencyContact'
                ),
                view.querySelector(
                    '#add-studentTransportEmergencyMobile'
                )
            ],
            'Transport emergency contact and mobile must be entered together.',
            errors
        );
    }

    function validatePhoto(view, errors) {
        const field = view.querySelector(
            '#add-studentProfilePhoto'
        );
        const photo = field?.files?.[0];

        if (!photo) {
            return;
        }

        if (!ALLOWED_PHOTO_TYPES.has(photo.type)) {
            const message =
                'Student photo must be JPG, PNG or WEBP.';
            setFieldError(field, message);
            errors.push(message);
        }

        if (photo.size > MAX_PHOTO_BYTES) {
            const message =
                'Student photo must not exceed 2 MB.';
            setFieldError(field, message);
            errors.push(message);
        }
    }

    function validateRequiredSelector(
        view,
        selector,
        message,
        errors
    ) {
        const field = view.querySelector(selector);

        if (!trimmedValue(field)) {
            setFieldError(field, message);
            errors.push(message);
        }
    }

    function validateCompleteGroup(
        fields,
        message,
        errors
    ) {
        const entered = fields.map(field =>
            Boolean(trimmedValue(field))
        );
        const enteredCount = entered.filter(Boolean).length;

        if (
            enteredCount === 0 ||
            enteredCount === fields.length
        ) {
            return;
        }

        fields.forEach((field, index) => {
            if (!entered[index]) {
                setFieldError(field, message);
            }
        });
        errors.push(message);
    }

    function validateDateRange(
        view,
        startSelector,
        endSelector,
        message,
        errors
    ) {
        const startField = view.querySelector(startSelector);
        const endField = view.querySelector(endSelector);
        const startDate = trimmedValue(startField);
        const endDate = trimmedValue(endField);

        if (
            startDate &&
            endDate &&
            endDate < startDate
        ) {
            setFieldError(endField, message);
            errors.push(message);
        }
    }

    function validateStartAgainstJoiningDate(
        view,
        startSelector,
        message,
        errors
    ) {
        const joiningDate = valueOf(
            view,
            '#add-studentJoiningDate'
        );
        const startField = view.querySelector(startSelector);
        const startDate = trimmedValue(startField);

        if (
            joiningDate &&
            startDate &&
            startDate < joiningDate
        ) {
            setFieldError(startField, message);
            errors.push(message);
        }
    }

    function buildStudentPayload(view, state) {
        const subjectResults = collectSubjectResults(
            view,
            false
        ).results;
        const subjectMarks = subjectResults.length > 0
            ? JSON.stringify(subjectResults)
            : null;

        const medical = {
            bloodGroup: valueOf(
                view,
                '#add-studentBloodGroup'
            ),
            heightCm: decimalOrNull(
                valueOf(view, '#add-studentHeightCm')
            ),
            weightKg: decimalOrNull(
                valueOf(view, '#add-studentWeightKg')
            ),
            allergies: valueOf(
                view,
                '#add-studentAllergies'
            ),
            chronicConditions: valueOf(
                view,
                '#add-studentChronicConditions'
            ),
            ongoingMedication: valueOf(
                view,
                '#add-studentOngoingMedication'
            ),
            specialNeeds: valueOf(
                view,
                '#add-studentSpecialNeeds'
            ),
            fitForSports: booleanOrNull(
                valueOf(view, '#add-studentFitForSports')
            ),
            emergencyDoctorName: valueOf(
                view,
                '#add-studentDoctorName'
            ),
            emergencyDoctorMobile: valueOf(
                view,
                '#add-studentDoctorMobile'
            ),
            preferredHospital: valueOf(
                view,
                '#add-studentPreferredHospital'
            ),
            remarks: valueOf(
                view,
                '#add-studentMedicalRemarks'
            )
        };

        const academicHistory = {
            formerSchoolName: valueOf(
                view,
                '#add-studentFormerSchoolName'
            ),
            formerSchoolCode: valueOf(
                view,
                '#add-studentFormerSchoolCode'
            ),
            formerSchoolLin: valueOf(
                view,
                '#add-studentFormerSchoolLin'
            ),
            formerSchoolAddress: valueOf(
                view,
                '#add-studentFormerSchoolAddress'
            ),
            schoolType: valueOf(
                view,
                '#add-studentSchoolType'
            ),
            transferReason: valueOf(
                view,
                '#add-studentTransferReason'
            ),
            previousAcademicYear: valueOf(
                view,
                '#add-studentPreviousAcademicYear'
            ),
            previousClass: valueOf(
                view,
                '#add-studentPreviousClass'
            ),
            previousSection: valueOf(
                view,
                '#add-studentPreviousSection'
            ),
            previousStream: valueOf(
                view,
                '#add-studentPreviousStream'
            ),
            pleIndexNumber: valueOf(
                view,
                '#add-studentPleIndexNumber'
            ),
            pleAggregate: valueOf(
                view,
                '#add-studentPleAggregate'
            ),
            uceIndexNumber: valueOf(
                view,
                '#add-studentUceIndexNumber'
            ),
            uceResult: valueOf(
                view,
                '#add-studentUceResult'
            ),
            uaceIndexNumber: valueOf(
                view,
                '#add-studentUaceIndexNumber'
            ),
            uaceResult: valueOf(
                view,
                '#add-studentUaceResult'
            ),
            subjectMarks,
            remarks: valueOf(
                view,
                '#add-studentAcademicRemarks'
            )
        };

        const academicYearOption = selectedOption(
            view.querySelector(
                '#add-studentAcademicYear'
            )
        );
        const classOption = selectedOption(
            view.querySelector(
                '#add-studentClass'
            )
        );
        const sectionOption = selectedOption(
            view.querySelector(
                '#add-studentSection'
            )
        );
        const joiningTermOption = selectedOption(
            view.querySelector(
                '#add-studentJoiningTerm'
            )
        );

        state.selectedAcademicYearText =
            selectedOptionText(academicYearOption, '-');
        state.selectedClassText =
            firstNonBlank(
                classOption?.dataset.className,
                selectedOptionText(classOption, '-')
            );
        state.selectedSectionText =
            selectedOptionText(
                sectionOption,
                'Not assigned'
            );
        state.selectedJoiningTermText =
            selectedOptionText(
                joiningTermOption,
                'Not selected'
            );

        const payload = {
            applicationId: positiveIntegerOrNull(
                valueOf(view, '#add-studentApplicationId')
            ),
            personal: {
                learnerLin: valueOf(
                    view,
                    '#add-studentLearnerLin'
                ),
                admissionYear: integerOrNull(
                    valueOf(view, '#add-studentAdmissionYear')
                ),
                joiningClassId: positiveIntegerOrNull(
                    valueOf(view, '#add-studentClass')
                ),
                joiningTermId: positiveIntegerOrNull(
                    valueOf(view, '#add-studentJoiningTerm')
                ),
                firstName: valueOf(
                    view,
                    '#add-studentFirstName'
                ),
                middleName: valueOf(
                    view,
                    '#add-studentMiddleName'
                ),
                lastName: valueOf(
                    view,
                    '#add-studentLastName'
                ),
                gender: valueOf(
                    view,
                    '#add-studentGender'
                ),
                dateOfBirth: valueOf(
                    view,
                    '#add-studentDob'
                ),
                nationality: valueOf(
                    view,
                    '#add-studentNationality'
                ),
                houseNo: valueOf(
                    view,
                    '#add-studentHouseNo'
                ),
                street: valueOf(
                    view,
                    '#add-studentStreet'
                ),
                village: valueOf(
                    view,
                    '#add-studentVillage'
                ),
                townCity: valueOf(
                    view,
                    '#add-studentTownCity'
                ),
                district: valueOf(
                    view,
                    '#add-studentDistrict'
                ),
                state: valueOf(
                    view,
                    '#add-studentState'
                ),
                country: valueOf(
                    view,
                    '#add-studentCountry'
                ),
                postalCode: valueOf(
                    view,
                    '#add-studentPostalCode'
                )
            },
            parent: {
                fatherName: valueOf(
                    view,
                    '#add-studentFatherName'
                ),
                fatherUin: valueOf(
                    view,
                    '#add-studentFatherUin'
                ),
                fatherPhone: valueOf(
                    view,
                    '#add-studentFatherPhone'
                ),
                fatherAlternatePhone: valueOf(
                    view,
                    '#add-studentFatherAltPhone'
                ),
                fatherEmail: valueOf(
                    view,
                    '#add-studentFatherEmail'
                ),
                fatherOccupation: valueOf(
                    view,
                    '#add-studentFatherOccupation'
                ),
                fatherEmployer: valueOf(
                    view,
                    '#add-studentFatherEmployer'
                ),
                fatherDesignation: valueOf(
                    view,
                    '#add-studentFatherDesignation'
                ),
                fatherAnnualIncome: decimalOrNull(
                    valueOf(view, '#add-studentFatherIncome')
                ),
                motherName: valueOf(
                    view,
                    '#add-studentMotherName'
                ),
                motherUin: valueOf(
                    view,
                    '#add-studentMotherUin'
                ),
                motherPhone: valueOf(
                    view,
                    '#add-studentMotherPhone'
                ),
                motherAlternatePhone: valueOf(
                    view,
                    '#add-studentMotherAltPhone'
                ),
                motherEmail: valueOf(
                    view,
                    '#add-studentMotherEmail'
                ),
                motherOccupation: valueOf(
                    view,
                    '#add-studentMotherOccupation'
                ),
                motherEmployer: valueOf(
                    view,
                    '#add-studentMotherEmployer'
                ),
                motherDesignation: valueOf(
                    view,
                    '#add-studentMotherDesignation'
                ),
                motherAnnualIncome: decimalOrNull(
                    valueOf(view, '#add-studentMotherIncome')
                ),
                guardianName: valueOf(
                    view,
                    '#add-studentGuardianName'
                ),
                guardianUin: valueOf(
                    view,
                    '#add-studentGuardianUin'
                ),
                guardianRelationship: valueOf(
                    view,
                    '#add-studentGuardianRelationship'
                ),
                guardianPhone: valueOf(
                    view,
                    '#add-studentGuardianPhone'
                ),
                guardianAlternatePhone: valueOf(
                    view,
                    '#add-studentGuardianAltPhone'
                ),
                guardianEmail: valueOf(
                    view,
                    '#add-studentGuardianEmail'
                ),
                guardianOccupation: valueOf(
                    view,
                    '#add-studentGuardianOccupation'
                ),
                preferredContact: checkedValue(
                    view,
                    'add-studentPreferredContact'
                ),
                feeResponsibility: checkedValue(
                    view,
                    'add-studentFeeResponsibility'
                ),
                parentsLivingTogether: booleanOrNull(
                    checkedValue(
                        view,
                        'add-studentParentsTogether'
                    )
                ),
                emergencyContactName: valueOf(
                    view,
                    '#add-studentEmergencyName'
                ),
                emergencyContactPhone: valueOf(
                    view,
                    '#add-studentEmergencyPhone'
                ),
                emergencyContactRelationship: valueOf(
                    view,
                    '#add-studentEmergencyRelationship'
                ),
                remarks: valueOf(
                    view,
                    '#add-studentParentRemarks'
                )
            },
            enrollment: {
                academicYearId: positiveIntegerOrNull(
                    valueOf(view, '#add-studentAcademicYear')
                ),
                classId: positiveIntegerOrNull(
                    valueOf(view, '#add-studentClass')
                ),
                sectionId: positiveIntegerOrNull(
                    valueOf(view, '#add-studentSection')
                ),
                rollNo: valueOf(
                    view,
                    '#add-studentRollNo'
                ),
                admissionType: valueOf(
                    view,
                    '#add-studentAdmissionType'
                ),
                joiningDate: valueOf(
                    view,
                    '#add-studentJoiningDate'
                ),
                remarks: valueOf(
                    view,
                    '#add-studentEnrollmentRemarks'
                )
            },
            medical: hasObjectData(medical)
                ? medical
                : null,
            academicHistory: hasObjectData(
                academicHistory
            )
                ? academicHistory
                : null,
            hostel: buildHostelPayload(view),
            transport: buildTransportPayload(view),
            operationId: createOperationId(
                'student-create'
            )
        };

        state.operationId = payload.operationId;
        return payload;
    }

    function buildHostelPayload(view) {
        const enabled = Boolean(
            view.querySelector(
                '#add-studentRequiresHostel'
            )?.checked
        );

        if (!enabled) {
            return null;
        }

        return {
            hostelId: positiveIntegerOrNull(
                valueOf(view, '#add-studentHostelId')
            ),
            roomId: positiveIntegerOrNull(
                valueOf(view, '#add-studentHostelRoomId')
            ),
            bedId: positiveIntegerOrNull(
                valueOf(view, '#add-studentHostelBedId')
            ),
            allocationStartDate: valueOf(
                view,
                '#add-studentHostelStartDate'
            ),
            allocationEndDate: valueOf(
                view,
                '#add-studentHostelEndDate'
            ),
            localGuardianName: valueOf(
                view,
                '#add-studentHostelGuardianName'
            ),
            localGuardianMobile: valueOf(
                view,
                '#add-studentHostelGuardianMobile'
            ),
            localGuardianRelation: valueOf(
                view,
                '#add-studentHostelGuardianRelation'
            ),
            remarks: valueOf(
                view,
                '#add-studentHostelRemarks'
            )
        };
    }

    function buildTransportPayload(view) {
        const enabled = Boolean(
            view.querySelector(
                '#add-studentRequiresTransport'
            )?.checked
        );

        if (!enabled) {
            return null;
        }

        return {
            routeId: positiveIntegerOrNull(
                valueOf(view, '#add-studentTransportRouteId')
            ),
            vehicleId: positiveIntegerOrNull(
                valueOf(view, '#add-studentTransportVehicleId')
            ),
            pickupPointId: positiveIntegerOrNull(
                valueOf(view, '#add-studentTransportPickupPointId')
            ),
            transportStartDate: valueOf(
                view,
                '#add-studentTransportStartDate'
            ),
            transportEndDate: valueOf(
                view,
                '#add-studentTransportEndDate'
            ),
            seatNumber: valueOf(
                view,
                '#add-studentTransportSeatNumber'
            ),
            emergencyContact: valueOf(
                view,
                '#add-studentTransportEmergencyContact'
            ),
            emergencyMobile: valueOf(
                view,
                '#add-studentTransportEmergencyMobile'
            ),
            remarks: valueOf(
                view,
                '#add-studentTransportRemarks'
            )
        };
    }

    function bindFormSubmission(view, form, state) {
        STUDENT_VIEW_STATES.set(view, state);

        form.addEventListener(
            'submit',
            async event => {
                event.preventDefault();

                if (state.submitting) {
                    return;
                }

                if (!validateStudentForm(view)) {
                    /*
                     * Inline field errors and the validation summary already
                     * explain the problem. Do not open the global premium
                     * modal because its full-screen overlay makes the form
                     * appear frozen.
                     */
                    return;
                }

                const submitButton = form.querySelector(
                    '#registerStudentBtn'
                );
                const payload = buildStudentPayload(
                    view,
                    state
                );

                state.lastPayload = payload;

                await runStudentRegistration(
                    view,
                    form,
                    state,
                    submitButton,
                    payload
                );
            }
        );
    }

    async function runStudentRegistration(
        view,
        form,
        state,
        submitButton,
        payload
    ) {
        if (state.submitting) {
            return;
        }

        state.submitting = true;
        setButtonBusy(
            submitButton,
            true,
            'Registering Student...'
        );
        openRegistrationModal(
            view,
            getStudentDisplayName(view)
        );

        try {
            updateRegistrationProgress(
                view,
                'Creating Student',
                'Saving personal, parent and enrollment information.'
            );

            const response = await requestPost(
                '/students/registrations',
                payload
            );
            const result = unwrapResponseData(response);

            const studentId = positiveIntegerOrNull(
                readProperty(result, 'studentId')
            );

            if (studentId === null) {
                showRegistrationFailure(
                    view,
                    [
                        'Student registration response did not contain a valid Student ID.'
                    ]
                );
                return;
            }

            state.lastStudentId = studentId;

            const photo = view.querySelector(
                '#add-studentProfilePhoto'
            )?.files?.[0];
            let photoWarning = null;

            if (photo) {
                updateRegistrationProgress(
                    view,
                    'Uploading Student Photo',
                    'The Student record is saved. Uploading the selected passport photo.'
                );

                try {
                    await uploadStudentPhoto(
                        result,
                        photo
                    );
                } catch (photoError) {
                    console.error(
                        'Student was created, but photo upload failed.',
                        photoError
                    );
                    photoWarning =
                        'The Student was registered, but the photo could not be uploaded. It can be added from the Student profile.';
                }
            }

            showRegistrationSuccess(
                view,
                result,
                state,
                photoWarning
            );

            state.lastPayload = null;
        } catch (error) {
            console.error(
                'Student registration failed.',
                error
            );

            applyBackendFieldErrors(
                view,
                error
            );
            showRegistrationFailure(
                view,
                extractErrorMessages(error)
            );
        } finally {
            state.submitting = false;
            setButtonBusy(
                submitButton,
                false
            );
        }
    }

    async function uploadStudentPhoto(result, photo) {
        const formData = new FormData();
        formData.append('photo', photo);

        const version = Number.isFinite(
            Number(readProperty(result, 'version'))
        )
            ? Number(readProperty(result, 'version'))
            : 0;
        const operationId = createOperationId(
            'student-photo'
        );
        const endpoint =
            `/students/${encodeURIComponent(readProperty(result, 'studentId'))}` +
            `/photo?version=${encodeURIComponent(version)}` +
            `&operationId=${encodeURIComponent(operationId)}`;

        return requestMultipart(
            endpoint,
            'PUT',
            formData
        );
    }

    function bindRegistrationModal(view, form, state) {
        const modal = view.querySelector(
            '#student-registration-modal'
        );

        modal?.querySelector(
            '#student-registration-close-btn'
        )?.addEventListener(
            'click',
            () => closeRegistrationModal(view)
        );

        modal?.querySelector(
            '#student-registration-return-btn'
        )?.addEventListener(
            'click',
            () => closeRegistrationModal(view)
        );

        modal?.querySelector(
            '#student-registration-retry-btn'
        )?.addEventListener(
            'click',
            () => {
                if (
                    state.lastPayload &&
                    !state.submitting
                ) {
                    const submitButton = form.querySelector(
                        '#registerStudentBtn'
                    );

                    void runStudentRegistration(
                        view,
                        form,
                        state,
                        submitButton,
                        state.lastPayload
                    );
                }
            }
        );

        modal?.querySelector(
            '#student-registration-add-another-btn'
        )?.addEventListener(
            'click',
            () => {
                closeRegistrationModal(view);
                resetStudentForm(
                    view,
                    form,
                    state
                );
                view.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        );

        modal?.querySelector(
            '#student-registration-list-btn'
        )?.addEventListener(
            'click',
            () => {
                void navigateToStudents(null);
            }
        );

        modal?.querySelector(
            '#student-registration-view-btn'
        )?.addEventListener(
            'click',
            () => {
                void navigateToStudents(
                    state.lastStudentId
                );
            }
        );
    }

    function openRegistrationModal(view, studentName) {
        const modal = view.querySelector(
            '#student-registration-modal'
        );

        if (!(modal instanceof HTMLElement)) {
            return;
        }

        removeClasses(modal, 'hidden');
        showOnlyModalView(view, 'processing');
        setText(
            view,
            '#student-registration-title',
            'Registering Student'
        );
        setText(
            view,
            '#student-registration-student-name',
            studentName || 'Preparing student information'
        );
        toggleHidden(
            view.querySelector(
                '#student-registration-close-btn'
            ),
            true
        );
        updateRegistrationProgress(
            view,
            'Registering Student',
            'Please wait while the Student registration is completed.'
        );
    }

    function closeRegistrationModal(view) {
        const modal = view.querySelector(
            '#student-registration-modal'
        );

        addClasses(modal, 'hidden');
    }

    function updateRegistrationProgress(
        view,
        title,
        message
    ) {
        setText(
            view,
            '#student-registration-stage-title',
            title
        );
        setText(
            view,
            '#student-registration-message',
            message
        );
    }

    function showRegistrationSuccess(
        view,
        result,
        state,
        warning
    ) {
        showOnlyModalView(view, 'success');
        setText(
            view,
            '#student-registration-title',
            'Registration Complete'
        );
        setText(
            view,
            '#student-registration-success-message',
            warning ||
            'Student registration has been completed.'
        );
        setText(
            view,
            '#student-registration-result-name',
            readProperty(result, 'fullName') || '-'
        );
        setText(
            view,
            '#student-registration-result-admission',
            readProperty(result, 'admissionNo') || '-'
        );
        setText(
            view,
            '#student-registration-result-year',
            state.selectedAcademicYearText
        );
        setText(
            view,
            '#student-registration-result-class',
            state.selectedClassText
        );
        setText(
            view,
            '#student-registration-result-section',
            state.selectedSectionText
        );
        setText(
            view,
            '#student-registration-result-term',
            state.selectedJoiningTermText
        );
        toggleHidden(
            view.querySelector(
                '#student-registration-close-btn'
            ),
            false
        );
    }

    function showRegistrationFailure(view, messages) {
        showOnlyModalView(view, 'failure');
        setText(
            view,
            '#student-registration-title',
            'Registration Failed'
        );
        setText(
            view,
            '#student-registration-failure-message',
            messages[0] ||
            'Student registration could not be completed.'
        );

        const list = view.querySelector(
            '#student-registration-error-list'
        );

        if (list) {
            list.replaceChildren();

            messages.forEach(message => {
                const item = document.createElement('li');
                item.textContent = message;
                list.appendChild(item);
            });
        }

        toggleHidden(
            view.querySelector(
                '#student-registration-close-btn'
            ),
            false
        );
    }

    function showOnlyModalView(view, target) {
        const modalViews = [
            [
                'processing',
                view.querySelector(
                    '#student-registration-processing-view'
                )
            ],
            [
                'success',
                view.querySelector(
                    '#student-registration-success-view'
                )
            ],
            [
                'failure',
                view.querySelector(
                    '#student-registration-failure-view'
                )
            ]
        ];

        modalViews.forEach(([name, element]) => {
            if (!(element instanceof Element)) {
                return;
            }

            toggleHidden(
                element,
                name !== target
            );
        });
    }

    function applyBackendFieldErrors(view, error) {
        const errorData =
            error?.data ||
            error?.response?.data ||
            {};
        const fieldErrors =
            readProperty(errorData, 'errors') ||
            readProperty(errorData, 'fieldErrors') ||
            {};

        if (
            !fieldErrors ||
            typeof fieldErrors !== 'object' ||
            Array.isArray(fieldErrors)
        ) {
            return;
        }

        const summaryMessages = [];

        Object.entries(fieldErrors).forEach(
            ([fieldName, messageValue]) => {
                const message = String(
                    messageValue ||
                    'Invalid value.'
                );
                const selector = resolveBackendFieldSelector(
                    fieldName
                );
                const field = selector
                    ? view.querySelector(selector)
                    : null;

                if (field) {
                    setFieldError(field, message);
                }

                summaryMessages.push(message);
            }
        );

        if (summaryMessages.length > 0) {
            showValidationSummary(
                view,
                summaryMessages
            );
        }
    }

    function resolveBackendFieldSelector(fieldName) {
        const normalized = String(fieldName || '')
            .replace(/^request\./, '')
            .replace(/\[[0-9]+]/g, '');

        if (BACKEND_FIELD_SELECTORS[normalized]) {
            return BACKEND_FIELD_SELECTORS[normalized];
        }

        const suffixMatch = Object.keys(
            BACKEND_FIELD_SELECTORS
        ).find(key =>
            normalized.endsWith(key)
        );

        return suffixMatch
            ? BACKEND_FIELD_SELECTORS[suffixMatch]
            : null;
    }

    function extractErrorMessages(error) {
        const data =
            error?.data ||
            error?.response?.data ||
            {};
        const messages = [];

        const fieldErrors =
            readProperty(data, 'errors') ||
            readProperty(data, 'fieldErrors');

        if (
            fieldErrors &&
            typeof fieldErrors === 'object' &&
            !Array.isArray(fieldErrors)
        ) {
            Object.values(fieldErrors)
                .forEach(message => {
                    const text = String(message || '').trim();
                    if (text) {
                        messages.push(text);
                    }
                });
        }

        if (Array.isArray(readProperty(data, 'errors'))) {
            readProperty(data, 'errors').forEach(item => {
                const text =
                    typeof item === 'string'
                        ? item
                        : firstNonBlank(
                            item?.message,
                            item?.defaultMessage,
                            item?.error
                        );

                if (text) {
                    messages.push(text);
                }
            });
        }

        const mainMessage = firstNonBlank(
            data.message,
            error?.message
        );

        if (mainMessage) {
            mainMessage
                .split(/\r?\n/)
                .map(line => line.trim())
                .filter(Boolean)
                .forEach(line => messages.push(line));
        }

        const uniqueMessages = Array.from(
            new Set(messages)
        );

        return uniqueMessages.length > 0
            ? uniqueMessages
            : ['An unexpected Student registration error occurred.'];
    }

    function bindFormReset(view, form, state) {
        form.addEventListener(
            'reset',
            () => {
                window.setTimeout(
                    () => resetStudentForm(
                        view,
                        form,
                        state,
                        false
                    ),
                    0
                );
            }
        );
    }

    function resetStudentForm(
        view,
        form,
        state,
        callNativeReset = true
    ) {
        if (callNativeReset) {
            form.reset();
        }

        clearAllValidationErrors(view);
        hideValidationSummary(view);
        resetPhotoPreview(view);
        resetSubjectRows(view);
        resetConditionalSections(view);
        setInitialDateValues(view);

        const currentYearOption = Array.from(
            view.querySelector(
                '#add-studentAcademicYear'
            )?.options || []
        ).find(option =>
            option.dataset.currentYear === 'true'
        );

        if (currentYearOption) {
            const academicYearSelect = view.querySelector(
                '#add-studentAcademicYear'
            );
            academicYearSelect.value =
                currentYearOption.value;
            synchronizeAdmissionYearFromAcademicYear(
                view,
                currentYearOption
            );
        }

        populateJoiningTerms(view, state);
        populateCurrentSections(view, state);
        populatePreviousClasses(view, state);
        updatePreviousClassSections(view);

        state.lastPayload = null;
        state.lastStudentId = null;
        state.operationId = null;
    }

    function resetSubjectRows(view) {
        const body = view.querySelector(
            '#add-studentSubjectResultsBody'
        );
        const rows = Array.from(
            body?.querySelectorAll(
                '.student-subject-result-row'
            ) || []
        );

        rows.slice(1).forEach(row => {
            if (row instanceof Element) {
                row.remove();
            }
        });

        rows[0]?.querySelectorAll('input')
            .forEach(input => {
                input.value = '';
            });

        updateSubjectSummary(view);
    }

    function resetConditionalSections(view) {
        synchronizeHostelSection(view);
        synchronizeTransportSection(view);
        updatePreviousClassSections(view);
    }

    function bindStudentBulkImport(view) {
        const bulkImportButton = view.querySelector(
            '#studentBulkImportBtn'
        );

        if (!(bulkImportButton instanceof HTMLButtonElement)) {
            return;
        }

        if (bulkImportButton.dataset.importBound === 'true') {
            return;
        }

        bulkImportButton.dataset.importBound = 'true';

        bulkImportButton.addEventListener(
            'click',
            () => {
                if (
                    typeof AppImporter === 'undefined' ||
                    typeof AppImporter.open !== 'function'
                ) {
                    showError(
                        'Student bulk importer is unavailable. Refresh the page and try again.'
                    );
                    return;
                }

                AppImporter.open(
                    'student',
                    'Import Students',
                    'Upload the approved Student XLSX template. Valid rows will continue and invalid cells will be returned in a corrected workbook.'
                );
            }
        );
    }

    function bindNavigation(view, state) {
        view.querySelector(
            '#backToStudentsBtn'
        )?.addEventListener(
            'click',
            () => {
                if (!state.submitting) {
                    void navigateToStudents(null);
                }
            }
        );
    }

    function navigateToStudents(studentId) {
        const navigate = readProperty(
            window,
            'erpNavigate'
        );

        if (typeof navigate !== 'function') {
            window.location.href = '/students.html';
            return Promise.resolve(false);
        }

        const normalizedStudentId = positiveIntegerOrNull(
            studentId
        );

        return Promise.resolve(
            navigate({
                role: 'admin',
                view: 'students',
                routeParams: normalizedStudentId
                    ? [String(normalizedStudentId)]
                    : [],
                title: normalizedStudentId
                    ? 'Student Details'
                    : 'Manage Students'
            })
        );
    }

    /**
     * Initializes the shared ERP Flatpickr component for every Student date field.
     * The calendar factory and theme are provided by global.js/global.css.
     *
     * @param {HTMLElement} view
     */
    function initializeStudentCalendars(view) {
        const calendarFactory = readProperty(
            window,
            'createErpCalendar'
        );

        if (typeof calendarFactory !== 'function') {
            return;
        }

        const currentYear = new Date().getFullYear();
        const calendarDefinitions = [
            {
                selector: '#add-studentDob',
                config: {
                    maxDate: previousDayIso(),
                    minYear: currentYear - 50,
                    maxYear: currentYear
                }
            },
            {
                selector: '#add-studentJoiningDate',
                config: {
                    maxDate: todayIso(),
                    minYear: currentYear - 10,
                    maxYear: currentYear
                }
            },
            {
                selector: '#add-studentHostelStartDate',
                config: {}
            },
            {
                selector: '#add-studentHostelEndDate',
                config: {}
            },
            {
                selector: '#add-studentTransportStartDate',
                config: {}
            },
            {
                selector: '#add-studentTransportEndDate',
                config: {}
            }
        ];

        calendarDefinitions.forEach(definition => {
            const input = view.querySelector(
                definition.selector
            );

            if (!(input instanceof HTMLInputElement)) {
                return;
            }

            const existingCalendar =
                getCalendarAdapter(input);

            if (
                existingCalendar &&
                typeof existingCalendar.destroy === 'function'
            ) {
                existingCalendar.destroy();
            }

            calendarFactory(
                definition.selector,
                definition.config
            );
        });
    }

    /**
     * @typedef {Object} StudentCalendarAdapter
     * @property {(value: (string|Date|null), triggerChange?: boolean) => void} setDate
     * @property {() => void} clear
     * @property {() => void} destroy
     */

    /**
     * @typedef {HTMLInputElement & {_flatpickr?: StudentCalendarAdapter}} StudentCalendarInput
     */

    /**
     * Returns the Flatpickr instance attached by createErpCalendar.
     *
     * @param {Element|null|undefined} element
     * @returns {StudentCalendarAdapter|null}
     */
    function getCalendarAdapter(element) {
        const input =
            /** @type {StudentCalendarInput|null} */
            (element);

        return input?._flatpickr || null;
    }

    /**
     * Updates both the ERP calendar state and its input fallback.
     *
     * @param {HTMLInputElement} input
     * @param {string} value
     */
    function setCalendarValue(input, value) {
        const calendar = getCalendarAdapter(input);

        if (calendar) {
            calendar.setDate(value);
        } else {
            input.value = value;
        }
    }

    /**
     * Clears both the ERP calendar state and its input fallback.
     *
     * @param {HTMLInputElement} input
     */
    function clearCalendarValue(input) {
        const calendar = getCalendarAdapter(input);

        if (calendar) {
            calendar.clear();
        } else {
            input.value = '';
        }
    }

    function setInitialDateValues(view) {
        const today = todayIso();
        const dob = view.querySelector(
            '#add-studentDob'
        );
        const joining = view.querySelector(
            '#add-studentJoiningDate'
        );
        const admissionYear = view.querySelector(
            '#add-studentAdmissionYear'
        );

        if (dob instanceof HTMLInputElement) {
            dob.max = previousDayIso();
        }

        if (joining instanceof HTMLInputElement) {
            joining.max = today;

            if (!joining.value) {
                setCalendarValue(joining, today);
            }
        }

        if (
            admissionYear instanceof HTMLInputElement &&
            !admissionYear.value
        ) {
            admissionYear.value = String(
                new Date().getFullYear()
            );
        }
    }

    function setConditionalRequired(
        view,
        group,
        required
    ) {
        view.querySelectorAll(
            `[data-conditional-required="${cssEscape(group)}"]`
        ).forEach(field => {
            field.required = required;
            field.setAttribute(
                'aria-required',
                String(required)
            );
        });
    }

    function copyJoiningDateIfBlank(
        view,
        targetSelector
    ) {
        const target = view.querySelector(
            targetSelector
        );

        if (
            target instanceof HTMLInputElement &&
            !target.value
        ) {
            setCalendarValue(
                target,
                valueOf(
                    view,
                    '#add-studentJoiningDate'
                ) || todayIso()
            );
        }
    }

    function showValidationSummary(view, errors) {
        const summary = view.querySelector(
            '#add-student-validation-summary'
        );
        const text = view.querySelector(
            '#add-student-validation-summary-text'
        );
        const uniqueErrors = Array.from(
            new Set(
                errors
                    .map(error => String(error || '').trim())
                    .filter(Boolean)
            )
        );

        if (text) {
            text.textContent = uniqueErrors.join(' ');
        }

        removeClasses(summary, 'hidden');
    }

    function hideValidationSummary(view) {
        addClasses(
            view.querySelector(
                '#add-student-validation-summary'
            ),
            'hidden'
        );
    }

    function setFieldError(field, message) {
        if (!(field instanceof Element)) {
            return;
        }

        const container = field.closest(
            '.form-group, .emp-child-field'
        );

        addClasses(field, 'emp-input-invalid');
        field.setAttribute('aria-invalid', 'true');

        if (container instanceof HTMLElement) {
            addClasses(container, 'emp-field-invalid');
            container.dataset.error = message;
        }
    }

    function clearFieldError(field) {
        if (!(field instanceof Element)) {
            return;
        }

        const container = field.closest(
            '.form-group, .emp-child-field'
        );

        removeClasses(field, 'emp-input-invalid');
        field.removeAttribute('aria-invalid');

        if (container instanceof HTMLElement) {
            removeClasses(container, 'emp-field-invalid');
            delete container.dataset.error;
        }
    }

    function clearAllValidationErrors(view) {
        view.querySelectorAll('.emp-input-invalid')
            .forEach(clearFieldError);
        view.querySelectorAll('.emp-field-invalid')
            .forEach(container => {
                removeClasses(
                    container,
                    'emp-field-invalid'
                );

                if (container instanceof HTMLElement) {
                    delete container.dataset.error;
                }
            });
    }

    function setButtonBusy(
        button,
        busy,
        text = ''
    ) {
        if (!(button instanceof HTMLButtonElement)) {
            return;
        }

        if (busy) {
            if (!BUTTON_ORIGINAL_NODES.has(button)) {
                /** @type {Node[]} */
                const originalNodes = [];

                button.childNodes.forEach(childNode => {
                    if (childNode instanceof Node) {
                        originalNodes.push(
                            childNode.cloneNode(true)
                        );
                    }
                });

                BUTTON_ORIGINAL_NODES.set(
                    button,
                    originalNodes
                );
            }

            button.disabled = true;
            button.setAttribute('aria-busy', 'true');
            button.textContent = text || 'Processing...';
            return;
        }

        button.disabled = false;
        button.removeAttribute('aria-busy');

        /** @type {Node[]|undefined} */
        const originalNodes =
            BUTTON_ORIGINAL_NODES.get(button);

        if (!originalNodes) {
            return;
        }

        const restoredContent =
            document.createDocumentFragment();

        for (
            let index = 0;
            index < originalNodes.length;
            index += 1
        ) {
            const originalNode =
                /** @type {Node} */ (
                originalNodes[index]
            );

            const clonedNode =
                /** @type {Node} */ (
                originalNode.cloneNode(true)
            );

            restoredContent.appendChild(clonedNode);
        }

        button.replaceChildren(restoredContent);
    }

    function getStudentDisplayName(view) {
        return [
            valueOf(view, '#add-studentFirstName'),
            valueOf(view, '#add-studentMiddleName'),
            valueOf(view, '#add-studentLastName')
        ].filter(Boolean).join(' ');
    }

    function selectedOption(select) {
        if (!(select instanceof HTMLSelectElement)) {
            return null;
        }

        return select.selectedIndex >= 0
            ? select.options.item(select.selectedIndex)
            : null;
    }

    function selectedOptionText(option, fallback) {
        if (
            !(option instanceof HTMLOptionElement) ||
            !option.value
        ) {
            return fallback;
        }

        return firstNonBlank(
            option.textContent,
            fallback
        );
    }

    function sortClasses(classes) {
        return [...classes].sort((first, second) => {
            const firstOrder = Number(
                readProperty(first, 'displayOrder') || 0
            );
            const secondOrder = Number(
                readProperty(second, 'displayOrder') || 0
            );

            if (firstOrder !== secondOrder) {
                return firstOrder - secondOrder;
            }

            return firstNonBlank(
                readProperty(first, 'classCode'),
                readProperty(first, 'className'),
                readProperty(first, 'name')
            ).localeCompare(
                firstNonBlank(
                    readProperty(second, 'classCode'),
                    readProperty(second, 'className'),
                    readProperty(second, 'name')
                )
            );
        });
    }

    function valueOf(view, selector) {
        const element = view.querySelector(selector);
        return trimmedValue(element) || null;
    }

    function trimmedValue(element) {
        if (
            element instanceof HTMLInputElement ||
            element instanceof HTMLSelectElement ||
            element instanceof HTMLTextAreaElement
        ) {
            return String(element.value || '').trim();
        }

        return '';
    }

    function checkedValue(view, name) {
        const selected = view.querySelector(
            `[name="${cssEscape(name)}"]:checked`
        );

        return selected instanceof HTMLInputElement
            ? String(selected.value || '').trim() || null
            : null;
    }

    function validationLabel(field) {
        return firstNonBlank(
            field?.dataset?.validationLabel,
            field?.getAttribute?.('aria-label'),
            field?.name,
            field?.id,
            'This field'
        );
    }

    function integerOrNull(value) {
        if (value === null || value === undefined || value === '') {
            return null;
        }

        const parsed = Number.parseInt(String(value), 10);
        return Number.isInteger(parsed)
            ? parsed
            : null;
    }

    function positiveIntegerOrNull(value) {
        const parsed = integerOrNull(value);
        return parsed !== null && parsed > 0
            ? parsed
            : null;
    }

    function decimalOrNull(value) {
        if (value === null || value === undefined || value === '') {
            return null;
        }

        const parsed = Number(value);
        return Number.isFinite(parsed)
            ? parsed
            : null;
    }

    function booleanOrNull(value) {
        if (value === true || value === 'true') {
            return true;
        }

        if (value === false || value === 'false') {
            return false;
        }

        return null;
    }

    function isPositiveInteger(value) {
        return positiveIntegerOrNull(value) !== null;
    }

    function hasObjectData(object) {
        return Object.values(object).some(value => {
            if (value === null || value === undefined) {
                return false;
            }

            if (typeof value === 'string') {
                return value.trim() !== '';
            }

            return true;
        });
    }

    async function requestGet(endpoint) {
        const apiFunction = readProperty(
            window,
            'apiGet'
        );

        if (typeof apiFunction !== 'function') {
            throw new Error(
                'api.js is not loaded: apiGet is unavailable.'
            );
        }

        return apiFunction(endpoint);
    }

    async function requestPost(endpoint, payload) {
        const apiFunction = readProperty(
            window,
            'apiPost'
        );

        if (typeof apiFunction !== 'function') {
            throw new Error(
                'api.js is not loaded: apiPost is unavailable.'
            );
        }

        return apiFunction(
            endpoint,
            payload
        );
    }

    async function requestMultipart(
        endpoint,
        method,
        formData
    ) {
        const apiFunction = readProperty(
            window,
            'apiMultipart'
        );

        if (typeof apiFunction !== 'function') {
            throw new Error(
                'api.js is not loaded: apiMultipart is unavailable.'
            );
        }

        return apiFunction(
            endpoint,
            method,
            formData
        );
    }

    function unwrapResponseData(response) {
        if (
            isRecord(response) &&
            Object.prototype.hasOwnProperty.call(
                response,
                'data'
            )
        ) {
            return readProperty(
                response,
                'data'
            );
        }

        return response;
    }

    function isRecord(value) {
        return (
            value !== null &&
            typeof value === 'object' &&
            !Array.isArray(value)
        );
    }

    function readProperty(value, propertyName) {
        if (!isRecord(value)) {
            return undefined;
        }

        return value[propertyName];
    }

    function readNestedProperty(
        value,
        parentProperty,
        childProperty
    ) {
        return readProperty(
            readProperty(value, parentProperty),
            childProperty
        );
    }

    function firstArrayProperty(
        source,
        ...propertyNames
    ) {
        for (const propertyName of propertyNames) {
            const candidate = readProperty(
                source,
                propertyName
            );

            if (Array.isArray(candidate)) {
                return candidate;
            }
        }

        return [];
    }

    function asArray(value) {
        return Array.isArray(value)
            ? value
            : [];
    }

    function firstDefined(...values) {
        return values.find(value =>
            value !== null &&
            value !== undefined &&
            value !== ''
        );
    }

    function firstNonBlank(...values) {
        for (const value of values) {
            const text = String(value ?? '').trim();

            if (text) {
                return text;
            }
        }

        return '';
    }

    function formatDecimal(value) {
        return Number.isInteger(value)
            ? String(value)
            : Number(value.toFixed(2)).toString();
    }

    function formatEnum(value) {
        return String(value || '')
            .toLowerCase()
            .replace(/_/g, ' ')
            .replace(/\b\w/g, letter =>
                letter.toUpperCase()
            );
    }

    function normalizeClassIdentity(value) {
        return String(value || '')
            .toUpperCase()
            .replace(/[^A-Z0-9]/g, '');
    }

    function matchesClassIdentity(
        classIdentity,
        acceptedValues
    ) {
        return acceptedValues.some(value =>
            classIdentity.includes(
                normalizeClassIdentity(value)
            )
        );
    }

    function createOperationId(prefix) {
        const uuid =
            typeof crypto !== 'undefined' &&
            typeof crypto.randomUUID === 'function'
                ? crypto.randomUUID()
                : `${Date.now()}-${Math.random().toString(16).slice(2)}`;

        return `${prefix}-${uuid}`.slice(0, 100);
    }

    function todayIso() {
        const now = new Date();
        const local = new Date(
            now.getTime() -
            now.getTimezoneOffset() * 60000
        );

        return local.toISOString().slice(0, 10);
    }

    function previousDayIso() {
        const date = new Date();
        date.setDate(date.getDate() - 1);
        const local = new Date(
            date.getTime() -
            date.getTimezoneOffset() * 60000
        );

        return local.toISOString().slice(0, 10);
    }

    function clearValues(view, selectors) {
        selectors.forEach(selector => {
            const field = view.querySelector(selector);

            if (
                field instanceof HTMLInputElement &&
                field.type === 'checkbox'
            ) {
                field.checked = false;
                clearFieldError(field);
                return;
            }

            if (field instanceof HTMLInputElement) {
                if (getCalendarAdapter(field)) {
                    clearCalendarValue(field);
                } else {
                    field.value = '';
                }

                clearFieldError(field);
                return;
            }

            if (
                field instanceof HTMLSelectElement ||
                field instanceof HTMLTextAreaElement
            ) {
                field.value = '';
                clearFieldError(field);
            }
        });
    }

    function setText(view, selector, value) {
        const element = view.querySelector(selector);

        if (element) {
            element.textContent = String(
                value ?? '-'
            );
        }
    }

    function toggleHidden(element, hidden) {
        if (hidden) {
            addClasses(element, 'hidden');
        } else {
            removeClasses(element, 'hidden');
        }
    }

    function addClasses(element, ...classes) {
        if (!(element instanceof Element)) {
            return;
        }

        const current = new Set(
            String(element.getAttribute('class') || '')
                .split(/\s+/)
                .filter(Boolean)
        );

        classes
            .flatMap(name => String(name || '').split(/\s+/))
            .filter(Boolean)
            .forEach(name => current.add(name));

        element.setAttribute(
            'class',
            Array.from(current).join(' ')
        );
    }

    function removeClasses(element, ...classes) {
        if (!(element instanceof Element)) {
            return;
        }

        const remove = new Set(
            classes
                .flatMap(name => String(name || '').split(/\s+/))
                .filter(Boolean)
        );
        const remaining = String(
            element.getAttribute('class') || ''
        )
            .split(/\s+/)
            .filter(Boolean)
            .filter(name => !remove.has(name));

        element.setAttribute(
            'class',
            remaining.join(' ')
        );
    }

    function cssEscape(value) {
        if (
            typeof CSS !== 'undefined' &&
            typeof CSS.escape === 'function'
        ) {
            return CSS.escape(String(value || ''));
        }

        return String(value || '')
            .replace(/[^a-zA-Z0-9_-]/g, '\\$&');
    }

    function showError(message) {
        const notification = readProperty(
            window,
            'showErrorMessage'
        );

        if (typeof notification === 'function') {
            notification(message);
            return;
        }

        console.error(message);
    }

})();

/* =========================================================
   MANAGE STUDENTS — EMPLOYEE-STYLE LIST / PROFILE / EDIT
   The Add Student module above remains unchanged.
   ========================================================= */

(function initializeManageStudentsModule() {
    'use strict';

    const MAX_MANAGE_PHOTO_BYTES = 2 * 1024 * 1024;
    const MANAGE_PHOTO_TYPES = [
        'image/jpeg',
        'image/png',
        'image/webp'
    ];

    /**
     * @typedef {Object<string, *>} JsonRecord
     */

    document.addEventListener('viewLoaded', event => {
        if (!(event instanceof CustomEvent)) {
            return;
        }

        const detail = toRecord(event.detail);

        if (
            read(detail, 'role') !== 'admin' ||
            read(detail, 'view') !== 'students'
        ) {
            return;
        }

        const initializationPromise =
            initManageStudentsView(detail).catch(error => {
                console.error(
                    'Manage Students initialization failed.',
                    error
                );

                notifyError(
                    error,
                    'Manage Students could not be initialized.'
                );

                throw error;
            });

        const waitUntil = read(detail, 'waitUntil');

        /*
         * Keep the global page loader active until Student data and all
         * required reference values are ready. This prevents the table or
         * profile from rendering first with blank/ID values and then being
         * rendered again when Academic Year, Class, Section and Term names
         * arrive.
         */
        if (typeof waitUntil === 'function') {
            waitUntil(initializationPromise);
            return;
        }

        void initializationPromise.catch(() => {
            /* The error was already reported above. */
        });
    });

    /**
     * @param {JsonRecord} routeInfo
     * @returns {Promise<void>}
     */
    async function initManageStudentsView(routeInfo) {
        const view = document.querySelector(
            '#ba-students-view'
        );

        if (!(view instanceof HTMLElement)) {
            return;
        }

        const tableView = view.querySelector(
            '#student-tableView'
        );
        const detailView = view.querySelector(
            '#student-detailView'
        );
        const editForm = view.querySelector(
            '#student-edit-form'
        );

        if (
            !(tableView instanceof HTMLElement) ||
            !(detailView instanceof HTMLElement) ||
            !(editForm instanceof HTMLFormElement)
        ) {
            throw new Error(
                'Manage Students HTML does not match students.js.'
            );
        }

        const context = {
            view,
            tableView,
            detailView,
            editForm,
            table: null,
            referenceData: createManageReferenceData(),
            referencePromise: null,
            currentProfile: null,
            currentStudentId: null,
            currentMode: 'view',
            page: 0,
            size: 10,
            sort: 'studentId,desc',
            confirmationResolver: null,
            previousFocus: null,
            operationRunning: false
        };

        initializeSystemManagedStudentFields(context);
        initializeManageCalendars(context);
        bindManageStudentDialogs(context);
        bindManageStudentActions(context);
        initializeManageStudentTable(context);

        if (typeof window.erpRegisterModuleSync === 'function') {
            window.erpRegisterModuleSync(
                'students',
                async () => {
                    if (!document.querySelector('#ba-students-view')) return false;
                    if (context.currentMode === 'edit') return true;

                    if (context.currentStudentId && !context.detailView.classList.contains('hidden')) {
                        await refreshCurrentStudentProfile(context);
                    } else {
                        await loadStudents(context);
                    }

                    return true;
                }
            );
        }

        const routeParams = Array.isArray(
            read(routeInfo, 'routeParams')
        )
            ? /** @type {Array<*>} */ (
                read(routeInfo, 'routeParams')
            ).map(String)
            : [];

        const studentId = positiveInteger(
            routeParams[0]
        );
        const mode = String(
            routeParams[1] || ''
        ).toLowerCase();

        /*
         * Load all branch-scoped reference values first. Student list and
         * profile rendering depend on these values for Academic Year,
         * Joined Term, Class, Section and Education Level names.
         *
         * Waiting here ensures every visible field is populated in one
         * render instead of loading extra columns after the page appears.
         */
        await prepareManageReferenceData(context);

        /*
         * Open a route-selected Student only after all reference values are
         * available. The profile is rendered once with complete labels.
         */
        if (studentId !== null) {
            const opened = await openStudentDetail(
                context,
                studentId,
                false
            );

            if (!opened) {
                return;
            }

            if (mode === 'edit') {
                await enterStudentEditMode(context);
                return;
            }

            if (mode === 'enrollment') {
                openStudentEnrollmentModal(context);
                return;
            }

            if (mode === 'status') {
                openStudentStatusModal(context);
            }

            return;
        }

        /*
         * Load the Student list only after reference data is ready. Table
         * rows therefore render once and do not receive late column updates.
         */
        await loadStudents(context);
    }

    async function prepareManageReferenceData(context) {
        const referenceData =
            await loadManageReferenceData(context);

        context.referenceData = referenceData;
        populateStudentListReferenceFilters(context);
        populateStudentEnrollmentReferenceFields(context);
        populatePreviousEducationReferenceFields(context);

        return referenceData;
    }

    function createManageReferenceData() {
        return {
            academicYears: [],
            academicTerms: [],
            classes: [],
            previousClasses: [],
            sections: [],
            levels: []
        };
    }

    /**
     * @param {*} value
     * @returns {JsonRecord}
     */
    function toRecord(value) {
        if (
            value &&
            typeof value === 'object' &&
            !Array.isArray(value)
        ) {
            return /** @type {JsonRecord} */ (value);
        }

        return {};
    }

    /**
     * @param {*} object
     * @param {string} key
     * @returns {*}
     */
    function read(object, key) {
        return toRecord(object)[key];
    }

    /**
     * @param {*} object
     * @param {...string} keys
     * @returns {*}
     */
    function readPath(object, ...keys) {
        let current = object;

        for (const key of keys) {
            current = read(current, key);

            if (
                current === null ||
                current === undefined
            ) {
                return null;
            }
        }

        return current;
    }

    /**
     * @param {*} response
     * @returns {*}
     */
    function unwrapManageResponse(response) {
        const data = read(response, 'data');

        return data !== undefined
            ? data
            : response;
    }

    /**
     * @param {*} value
     * @returns {Array<*>}
     */
    function toArray(value) {
        return Array.isArray(value)
            ? value
            : [];
    }

    /**
     * @param {*} value
     * @returns {(number|null)}
     */
    function positiveInteger(value) {
        const parsed = Number.parseInt(
            String(value ?? ''),
            10
        );

        return Number.isInteger(parsed) && parsed > 0
            ? parsed
            : null;
    }

    /**
     * @param {*} value
     * @returns {(number|null)}
     */
    function finiteNumber(value) {
        if (
            value === null ||
            value === undefined ||
            String(value).trim() === ''
        ) {
            return null;
        }

        const parsed = Number(value);
        return Number.isFinite(parsed)
            ? parsed
            : null;
    }

    /**
     * @param {*} value
     * @returns {(boolean|null)}
     */
    function nullableBoolean(value) {
        if (value === true || value === 'true') {
            return true;
        }

        if (value === false || value === 'false') {
            return false;
        }

        return null;
    }

    /**
     * @param {*} value
     * @returns {string}
     */
    function textOrDash(value) {
        if (
            value === null ||
            value === undefined ||
            String(value).trim() === ''
        ) {
            return '-';
        }

        return String(value);
    }

    /**
     * @param {*} value
     * @returns {string}
     */
    function enumLabel(value) {
        if (!value) {
            return '-';
        }

        return String(value)
            .replace(/_/g, ' ')
            .toLowerCase()
            .replace(/\b\w/g, letter =>
                letter.toUpperCase()
            );
    }

    /**
     * @param {*} value
     * @returns {string}
     */
    function dateLabel(value) {
        if (!value) {
            return '-';
        }

        if (window.erpDate) {
            return window.erpDate.formatDate(value, '-');
        }

        return String(value).slice(0, 10);
    }

    /**
     * @param {*} value
     * @returns {string}
     */
    function booleanLabel(value) {
        if (value === true) {
            return 'Yes';
        }

        if (value === false) {
            return 'No';
        }

        return '-';
    }

    /**
     * @param {*} value
     * @returns {string}
     */
    function fileSizeLabel(value) {
        const bytes = Number(value);

        if (!Number.isFinite(bytes) || bytes < 0) {
            return '-';
        }

        if (bytes < 1024) {
            return `${bytes} B`;
        }

        if (bytes < 1024 * 1024) {
            return `${(bytes / 1024).toFixed(1)} KB`;
        }

        return `${(
            bytes / (1024 * 1024)
        ).toFixed(1)} MB`;
    }

    /**
     * @param {*} element
     * @param {...string} classNames
     */
    function addManageClasses(element, ...classNames) {
        if (!(element instanceof Element)) {
            return;
        }

        const current = String(
            element.getAttribute('class') || ''
        )
            .split(/\s+/)
            .filter(Boolean);

        classNames
            .flatMap(name =>
                String(name || '').split(/\s+/)
            )
            .filter(Boolean)
            .forEach(name => {
                if (current.indexOf(name) === -1) {
                    current.push(name);
                }
            });

        element.setAttribute(
            'class',
            current.join(' ')
        );
    }

    /**
     * @param {*} element
     * @param {...string} classNames
     */
    function removeManageClasses(element, ...classNames) {
        if (!(element instanceof Element)) {
            return;
        }

        const names = classNames
            .flatMap(name =>
                String(name || '').split(/\s+/)
            )
            .filter(Boolean);

        const remaining = String(
            element.getAttribute('class') || ''
        )
            .split(/\s+/)
            .filter(Boolean)
            .filter(name => names.indexOf(name) === -1);

        element.setAttribute(
            'class',
            remaining.join(' ')
        );
    }

    /**
     * @param {*} element
     * @param {boolean} hidden
     */
    function setManageHidden(element, hidden) {
        if (hidden) {
            addManageClasses(element, 'hidden');
        } else {
            removeManageClasses(element, 'hidden');
        }
    }

    function createManageOperationId(prefix) {
        const uuid =
            typeof crypto !== 'undefined' &&
            typeof crypto.randomUUID === 'function'
                ? crypto.randomUUID()
                : `${Date.now()}-${Math.random()
                    .toString(16)
                    .slice(2)}`;

        return `${prefix}-${uuid}`.slice(0, 100);
    }

    function manageTodayIso() {
        const now = new Date();
        const local = new Date(
            now.getTime() -
            now.getTimezoneOffset() * 60000
        );

        return local.toISOString().slice(0, 10);
    }

    function maximumStudentDobIso() {
        const today = new Date();
        const year = today.getFullYear() - 3;
        const month = today.getMonth();
        const day = Math.min(
            today.getDate(),
            new Date(year, month + 1, 0).getDate()
        );
        const date = new Date(year, month, day);
        const local = new Date(
            date.getTime() -
            date.getTimezoneOffset() * 60000
        );

        return local.toISOString().slice(0, 10);
    }

    function notifyError(error, fallback) {
        console.error(error);

        const message =
            read(error, 'message') ||
            fallback ||
            'The Student operation could not be completed.';

        if (typeof showErrorMessage === 'function') {
            showErrorMessage(String(message));
            return;
        }

        console.error(message);
    }

    function notifySuccess(message) {
        if (typeof showSuccessMessage === 'function') {
            showSuccessMessage(message);
            return;
        }

        console.info(message);
    }

    function waitForManagePaint() {
        return new Promise(resolve => {
            window.requestAnimationFrame(() => {
                window.requestAnimationFrame(resolve);
            });
        });
    }

    async function runManageOperation(
        context,
        options,
        operation
    ) {
        if (context.operationRunning) {
            return null;
        }

        const overlay = context.view.querySelector(
            '#student-operation-overlay'
        );
        const title = context.view.querySelector(
            '#student-operation-title'
        );
        const message = context.view.querySelector(
            '#student-operation-message'
        );
        const button = read(options, 'button');
        const originalDisabled =
            button instanceof HTMLButtonElement
                ? button.disabled
                : false;

        context.operationRunning = true;

        if (title) {
            title.textContent = String(
                read(options, 'title') ||
                'Processing Student'
            );
        }

        if (message) {
            message.textContent = String(
                read(options, 'message') ||
                'Please wait while the Student operation is completed.'
            );
        }

        if (button instanceof HTMLButtonElement) {
            button.disabled = true;
            button.setAttribute('aria-busy', 'true');
        }

        addManageClasses(overlay, 'is-visible');
        overlay?.setAttribute('aria-hidden', 'false');

        await waitForManagePaint();

        try {
            return await operation();
        } finally {
            removeManageClasses(overlay, 'is-visible');
            overlay?.setAttribute('aria-hidden', 'true');

            if (button instanceof HTMLButtonElement) {
                button.disabled = originalDisabled;
                button.removeAttribute('aria-busy');
            }

            context.operationRunning = false;
        }
    }

    async function navigateManageStudent(
        studentId,
        mode = null
    ) {
        const id = positiveInteger(studentId);

        if (id === null) {
            notifyError(
                new Error('A valid Student ID is required.'),
                'A valid Student ID is required.'
            );
            return false;
        }

        if (typeof window.erpNavigate !== 'function') {
            return false;
        }

        return window.erpNavigate({
            role: 'admin',
            view: 'students',
            routeParams: [
                String(id),
                ...(mode ? [String(mode)] : [])
            ],
            title: mode === 'edit'
                ? 'Edit Student'
                : 'Student Details'
        });
    }

    async function navigateToStudentList() {
        if (typeof window.erpNavigate !== 'function') {
            return false;
        }

        return window.erpNavigate({
            role: 'admin',
            view: 'students',
            routeParams: [],
            title: 'Manage Students'
        });
    }

    function runWithButtonFeedback(
        button,
        label,
        operation
    ) {
        if (
            typeof window.erpWithButtonFeedback === 'function'
        ) {
            return window.erpWithButtonFeedback(
                button,
                label,
                operation
            );
        }

        return Promise.resolve().then(operation);
    }

    async function loadManageReferenceData(context) {
        if (context.referencePromise) {
            return context.referencePromise;
        }

        context.referencePromise = (async () => {
            let secured = {};

            try {
                const securedResponse = await requestManageGet(
                    '/students/reference-data'
                );
                secured = toRecord(
                    unwrapManageResponse(securedResponse)
                );
            } catch (error) {
                console.warn(
                    'Student reference-data endpoint could not be loaded.',
                    error
                );
            }

            /*
             * These classes are branch-authorized by the backend and are the
             * only classes allowed in the Student list filter and current
             * enrollment controls.
             */
            const referenceData = {
                academicYears: toArray(
                    read(secured, 'academicYears')
                ),
                academicTerms: toArray(
                    manageFirstDefined(
                        read(secured, 'academicTerms'),
                        read(secured, 'terms')
                    )
                ),
                classes: toArray(
                    read(secured, 'classes')
                ),
                previousClasses: [],
                sections: toArray(
                    read(secured, 'sections')
                ),
                levels: toArray(
                    read(secured, 'levels')
                )
            };

            /*
             * Public references are isolated to previous-school education.
             * They must never be merged into branch current-placement classes.
             */
            const optionalResponses = await Promise.allSettled([
                requestManageGet('/public/levels'),
                requestManageGet('/public/classes')
            ]);

            if (optionalResponses[0].status === 'fulfilled') {
                const publicLevels = toArray(
                    unwrapManageResponse(
                        optionalResponses[0].value
                    )
                );

                if (publicLevels.length > 0) {
                    referenceData.levels = publicLevels;
                }
            }

            if (optionalResponses[1].status === 'fulfilled') {
                referenceData.previousClasses = toArray(
                    unwrapManageResponse(
                        optionalResponses[1].value
                    )
                );
            }

            if (referenceData.previousClasses.length === 0) {
                referenceData.previousClasses = [
                    ...referenceData.classes
                ];
            }

            context.referenceData = referenceData;
            return referenceData;
        })().finally(() => {
            context.referencePromise = null;
        });

        return context.referencePromise;
    }

    function createOption(value, label) {
        /*
         * HTMLOptionElement constructor order is:
         * new Option(visibleText, submittedValue)
         *
         * Keep the database ID as the option value and display the
         * reference name to the user.
         */
        return new Option(
            String(label ?? ''),
            String(value ?? '')
        );
    }

    function replaceSelectOptions(
        select,
        placeholder,
        options = [],
        selectedValue = ''
    ) {
        if (!(select instanceof HTMLSelectElement)) {
            return;
        }

        const fragment = document.createDocumentFragment();
        fragment.appendChild(
            createOption('', placeholder)
        );

        options.forEach(option => {
            const item = toRecord(option);
            fragment.appendChild(
                createOption(
                    read(item, 'value'),
                    read(item, 'label')
                )
            );
        });

        select.replaceChildren(fragment);
        select.value = String(
            selectedValue ?? ''
        );
    }

    function academicYearOptions(context) {
        return context.referenceData.academicYears
            .map(item => {
                const record = toRecord(item);
                const id = read(record, 'academicYearId') ??
                    read(record, 'id');

                const name = meaningfulReferenceLabel(
                    read(record, 'academicYearName') ??
                    read(record, 'name') ??
                    read(record, 'academicYearCode') ??
                    read(record, 'code'),
                    id
                );

                if (
                    positiveInteger(id) === null ||
                    !name
                ) {
                    return null;
                }

                const current =
                    read(record, 'currentYear') === true;

                return {
                    value: id,
                    label: current
                        ? `${name} (Current)`
                        : name
                };
            })
            .filter(option => option !== null);
    }

    function classOptions(context) {
        return context.referenceData.classes
            .map(item => {
                const record = toRecord(item);
                const id = read(record, 'classId') ??
                    read(record, 'id');

                const name = meaningfulReferenceLabel(
                    read(record, 'className') ??
                    read(record, 'name') ??
                    read(record, 'classDisplayName') ??
                    read(record, 'classCode') ??
                    read(record, 'code'),
                    id
                );

                if (
                    positiveInteger(id) === null ||
                    !name
                ) {
                    return null;
                }

                return {
                    value: id,
                    label: name
                };
            })
            .filter(option => option !== null);
    }

    function sectionOptions(
        context,
        academicYearId,
        classId
    ) {
        const year = positiveInteger(academicYearId);
        const classValue = positiveInteger(classId);

        return context.referenceData.sections
            .filter(item => {
                const record = toRecord(item);
                return (
                    positiveInteger(
                        read(record, 'academicYearId')
                    ) === year &&
                    positiveInteger(
                        read(record, 'classId')
                    ) === classValue
                );
            })
            .map(item => {
                const record = toRecord(item);
                const id = read(record, 'sectionId') ??
                    read(record, 'id');

                const name = meaningfulReferenceLabel(
                    read(record, 'sectionName') ??
                    read(record, 'name') ??
                    read(record, 'sectionCode') ??
                    read(record, 'code'),
                    id
                );

                if (
                    positiveInteger(id) === null ||
                    !name
                ) {
                    return null;
                }

                return {
                    value: id,
                    label: name
                };
            })
            .filter(option => option !== null);
    }

    function populateStudentListReferenceFilters(context) {
        replaceSelectOptions(
            context.view.querySelector(
                '#student-academicYearFilter'
            ),
            'All Academic Years',
            academicYearOptions(context)
        );

        replaceSelectOptions(
            context.view.querySelector(
                '#student-classFilter'
            ),
            'All Classes',
            classOptions(context)
        );
    }

    function populateStudentEnrollmentReferenceFields(
        context,
        selected = {}
    ) {
        const academicYearSelect = context.view.querySelector(
            '#student-enrollment-academicYear'
        );
        const classSelect = context.view.querySelector(
            '#student-enrollment-class'
        );
        const sectionSelect = context.view.querySelector(
            '#student-enrollment-section'
        );

        replaceSelectOptions(
            academicYearSelect,
            '-- Select Academic Year --',
            academicYearOptions(context),
            read(selected, 'academicYearId') ?? ''
        );

        replaceSelectOptions(
            classSelect,
            '-- Select Class --',
            classOptions(context),
            read(selected, 'classId') ?? ''
        );

        const sections = sectionOptions(
            context,
            read(selected, 'academicYearId'),
            read(selected, 'classId')
        );

        replaceSelectOptions(
            sectionSelect,
            '-- No Section --',
            sections,
            read(selected, 'sectionId') ?? ''
        );

        if (sectionSelect instanceof HTMLSelectElement) {
            sectionSelect.disabled = sections.length === 0;
        }
    }

    function populatePreviousEducationReferenceFields(
        context,
        selectedClassName = ''
    ) {
        const levelSelect = context.view.querySelector(
            '#edit-studentPreviousLevel'
        );

        const levels = context.referenceData.levels
            .map(item => {
                const record = toRecord(item);
                return {
                    value: read(record, 'levelId') ??
                        read(record, 'id'),
                    label: read(record, 'levelName') ??
                        read(record, 'name')
                };
            })
            .filter(option =>
                positiveInteger(option.value) !== null
            );

        let selectedLevelId = '';

        if (selectedClassName) {
            const matchingClass = context.referenceData.previousClasses
                .find(item => {
                    const record = toRecord(item);
                    const className = String(
                        read(record, 'className') ??
                        read(record, 'name') ??
                        ''
                    );

                    return className.toLowerCase() ===
                        String(selectedClassName).toLowerCase();
                });

            if (matchingClass) {
                selectedLevelId = String(
                    read(matchingClass, 'levelId') ??
                    readPath(matchingClass, 'level', 'levelId') ??
                    ''
                );
            }
        }

        replaceSelectOptions(
            levelSelect,
            '-- Select Previous Level --',
            levels,
            selectedLevelId
        );

        populatePreviousClassOptions(
            context,
            selectedLevelId,
            selectedClassName
        );
    }

    function populatePreviousClassOptions(
        context,
        levelId,
        selectedClassName = ''
    ) {
        const select = context.view.querySelector(
            '#edit-studentPreviousClass'
        );
        const normalizedLevelId = positiveInteger(levelId);

        const options = context.referenceData.previousClasses
            .filter(item => {
                if (normalizedLevelId === null) {
                    return false;
                }

                const record = toRecord(item);
                return positiveInteger(
                    read(record, 'levelId') ??
                    readPath(record, 'level', 'levelId')
                ) === normalizedLevelId;
            })
            .map(item => {
                const record = toRecord(item);
                const name = read(record, 'className') ??
                    read(record, 'name') ??
                    read(record, 'classCode');
                const code = read(record, 'classCode') ??
                    read(record, 'code');

                return {
                    value: name,
                    label: code
                        ? `[${code}] ${name}`
                        : name
                };
            });

        replaceSelectOptions(
            select,
            normalizedLevelId === null
                ? '-- Select Previous Level First --'
                : '-- Select Previous Class --',
            options,
            selectedClassName
        );

        if (select instanceof HTMLSelectElement) {
            select.disabled =
                normalizedLevelId === null ||
                options.length === 0;
        }
    }

    function initializeSystemManagedStudentFields(context) {
        const selectors = [
            '#edit-studentRequiresHostel',
            '[id^="edit-studentHostel"]',
            '#edit-studentRequiresTransport',
            '[id^="edit-studentTransport"]'
        ].join(', ');

        context.editForm
            .querySelectorAll(selectors)
            .forEach(element => {
                if (
                    element instanceof HTMLInputElement ||
                    element instanceof HTMLSelectElement ||
                    element instanceof HTMLTextAreaElement
                ) {
                    element.disabled = true;
                    element.setAttribute(
                        'aria-disabled',
                        'true'
                    );
                    element.title =
                        'This allocation is managed through its dedicated module.';
                }

                addManageClasses(
                    element,
                    'student-system-managed-control'
                );
            });
    }

    function initializeManageCalendars(context) {
        const dob = context.view.querySelector(
            '#edit-studentDob'
        );
        const statusDate = context.view.querySelector(
            '#student-status-effectiveDate'
        );
        const enrollmentDate = context.view.querySelector(
            '#student-enrollment-effectiveDate'
        );

        if (dob instanceof HTMLInputElement) {
            dob.max = maximumStudentDobIso();
        }

        if (statusDate instanceof HTMLInputElement) {
            statusDate.max = manageTodayIso();
        }

        if (enrollmentDate instanceof HTMLInputElement) {
            enrollmentDate.max = manageTodayIso();
        }

        if (typeof createErpCalendar === 'function') {
            createErpCalendar('#edit-studentDob', {
                maxDate: maximumStudentDobIso()
            });
            createErpCalendar('#student-status-effectiveDate', {
                maxDate: manageTodayIso()
            });
            createErpCalendar('#student-enrollment-effectiveDate', {
                maxDate: manageTodayIso()
            });
        }
    }

    function requestManageGet(endpoint) {
        const fn = read(window, 'apiGet');

        if (typeof fn !== 'function') {
            return Promise.reject(
                new Error('api.js is not loaded: apiGet is unavailable.')
            );
        }

        return Promise.resolve(fn(endpoint));
    }


    function cancelQueuedGlobalSync() {
        if (
            typeof window.erpCancelPendingDataSync
            === 'function'
        ) {
            window.erpCancelPendingDataSync();
        }
    }

    function requestManagePost(endpoint, payload) {
        const fn = read(window, 'apiPost');

        if (typeof fn !== 'function') {
            return Promise.reject(
                new Error('api.js is not loaded: apiPost is unavailable.')
            );
        }

        return Promise.resolve(fn(endpoint, payload));
    }

    function requestManagePut(endpoint, payload) {
        const fn = read(window, 'apiPut');

        if (typeof fn !== 'function') {
            return Promise.reject(
                new Error('api.js is not loaded: apiPut is unavailable.')
            );
        }

        return Promise.resolve(fn(endpoint, payload));
    }

    function requestManageDelete(endpoint) {
        const fn = read(window, 'apiDelete');

        if (typeof fn !== 'function') {
            return Promise.reject(
                new Error('api.js is not loaded: apiDelete is unavailable.')
            );
        }

        return Promise.resolve(fn(endpoint));
    }

    function requestManageMultipart(endpoint, method, formData) {
        const fn = read(window, 'apiMultipart');

        if (typeof fn !== 'function') {
            return Promise.reject(
                new Error('api.js is not loaded: apiMultipart is unavailable.')
            );
        }

        return Promise.resolve(
            fn(endpoint, method, formData)
        );
    }

    function getManagePageData(response) {
        const data = toRecord(
            unwrapManageResponse(response)
        );
        const records = toArray(
            read(data, 'students')
        );

        return {
            records,
            page: Math.max(
                Number(read(data, 'page')) || 0,
                0
            ),
            size: Math.max(
                Number(read(data, 'size')) || records.length || 1,
                1
            ),
            totalElements: Math.max(
                Number(read(data, 'totalElements')) || 0,
                0
            ),
            totalPages: Math.max(
                Number(read(data, 'totalPages')) || 0,
                0
            ),
            first: read(data, 'first') === true,
            last: read(data, 'last') === true,
            hasNext: read(data, 'hasNext') === true,
            hasPrevious: read(data, 'hasPrevious') === true
        };
    }

    function buildStudentListFilter(context) {
        const search = getManageControlValue(
            context.view,
            '#student-searchInput'
        );
        const payload = {
            admissionNo: null,
            learnerLin: null,
            studentName: null,
            admissionYear: null,
            academicYearId: positiveInteger(
                getManageControlValue(
                    context.view,
                    '#student-academicYearFilter'
                )
            ),
            classId: positiveInteger(
                getManageControlValue(
                    context.view,
                    '#student-classFilter'
                )
            ),
            sectionId: null,
            gender: null,
            studentStatus: getManageControlValue(
                context.view,
                '#student-statusFilter'
            ),
            enrollmentStatus: null,
            active: null
        };

        if (search) {
            const normalized = search.trim();
            const compact = normalized.replace(/\s+/g, '');
            const upper = compact.toUpperCase();

            if (/^[0-9]{8,}$/.test(compact)) {
                payload.learnerLin = normalized;
            } else if (
                /^U\d{2,4}[-/]/.test(upper) ||
                upper.includes('ADM') ||
                upper.includes('APP') ||
                normalized.includes('/')
            ) {
                payload.admissionNo = normalized;
            } else {
                payload.studentName = normalized;
            }
        }

        return payload;
    }

    function getManageControlValue(root, selector) {
        const control = root.querySelector(selector);

        if (
            control instanceof HTMLInputElement ||
            control instanceof HTMLSelectElement ||
            control instanceof HTMLTextAreaElement
        ) {
            const value = control.value.trim();
            return value || null;
        }

        return null;
    }

    function initializeManageStudentTable(context) {
        /*
         * crud-table.js declares `class CrudTable` as a global lexical
         * binding. A top-level class is available by its identifier but is
         * not exposed as window.CrudTable, so reading it from window gives
         * undefined even when the framework script loaded correctly.
         */
        if (typeof CrudTable !== 'function') {
            throw new Error(
                'The Student table framework is unavailable. Refresh the page and try again.'
            );
        }

        context.table = new CrudTable(
            {
                tbody: context.view.querySelector(
                    '#student-tableBody'
                ),
                pageSize: context.view.querySelector(
                    '#student-pageSize'
                ),
                pageInfo: context.view.querySelector(
                    '#student-pageInfo'
                ),
                btnPrev: context.view.querySelector(
                    '#btn-student-prev'
                ),
                btnNext: context.view.querySelector(
                    '#btn-student-next'
                ),
                tplLoading: document.getElementById(
                    'global-table-fetching-template'
                ),
                tplEmpty: document.getElementById(
                    'global-table-empty-template'
                ),
                tplRow: context.view.querySelector(
                    '#tpl-student-row'
                ),
                table: context.view.querySelector(
                    '#student-tableComponent'
                )
            },
            {
                onPageChange: direction => {
                    context.page = Math.max(
                        context.page + Number(direction || 0),
                        0
                    );
                    return loadStudents(context);
                },
                onSizeChange: size => {
                    const parsed = positiveInteger(size);
                    context.size = parsed || 10;
                    context.page = 0;
                    return loadStudents(context);
                },
                onSort: sort => {
                    context.sort = String(
                        sort || 'studentId,desc'
                    );
                    context.page = 0;
                    return loadStudents(context);
                }
            }
        );
    }

    async function loadStudents(context) {
        context.table?.showLoading();

        try {
            const response = await requestManagePost(
                `/students/search?page=${context.page}` +
                `&size=${context.size}` +
                `&sort=${encodeURIComponent(context.sort)}`,
                buildStudentListFilter(context)
            );
            const pageData = getManagePageData(response);

            context.page = pageData.page;
            context.size = pageData.size;

            context.table?.render(
                pageData.records,
                (student, rowNode) =>
                    renderStudentTableRow(
                        context,
                        student,
                        rowNode
                    )
            );

            context.table?.renderPagination(
                pageData.page,
                pageData.totalPages,
                pageData.totalElements
            );
        } catch (error) {
            context.table?.render([]);
            context.table?.renderPagination(0, 0, 0);
            notifyError(
                error,
                'Student records could not be loaded.'
            );
        }
    }

    function renderStudentTableRow(
        context,
        studentValue,
        rowNode
    ) {
        const student = toRecord(studentValue);
        const studentId = positiveInteger(
            read(student, 'studentId')
        );
        const fullName = textOrDash(
            read(student, 'fullName')
        );
        const status = String(
            read(student, 'studentStatus') || 'ACTIVE'
        );
        const photoUrl = normalizedManageUrl(
            read(student, 'photoUrl')
        );

        setRowText(
            rowNode,
            '.col-code',
            read(student, 'admissionNo')
        );
        setRowText(
            rowNode,
            '.col-name strong',
            fullName
        );
        setRowText(
            rowNode,
            '.student-gender-label',
            enumLabel(read(student, 'gender'))
        );
        setRowText(
            rowNode,
            '.admission-val',
            read(student, 'admissionNo')
        );
        setRowText(
            rowNode,
            '.lin-val',
            read(student, 'learnerLin')
                ? `LIN: ${read(student, 'learnerLin')}`
                : 'LIN not provided'
        );
        const listClassLabel =
            enrollmentReferenceLabel(
                context,
                student,
                'class'
            );
        const listSectionLabel =
            enrollmentReferenceLabel(
                context,
                student,
                'section'
            );

        setRowText(
            rowNode,
            '.class-val',
            listClassLabel
        );
        setRowText(
            rowNode,
            '.section-val',
            listSectionLabel !== '-'
                ? `Section: ${listSectionLabel}`
                : 'No section'
        );
        setRowText(
            rowNode,
            '.contact-name-val',
            read(student, 'preferredContactName')
        );
        setRowText(
            rowNode,
            '.contact-phone-val',
            read(student, 'preferredContactPhone')
        );

        const badge = rowNode.querySelector(
            '.status-badge'
        );

        if (badge) {
            badge.textContent = enumLabel(status);
            badge.setAttribute(
                'class',
                `status-badge badge badge-${status.toLowerCase()}`
            );
        }

        const photo = rowNode.querySelector(
            '.student-table-photo'
        );
        const placeholder = rowNode.querySelector(
            '.student-table-photo-placeholder'
        );

        if (
            photo instanceof HTMLImageElement &&
            photoUrl
        ) {
            photo.src = photoUrl;
            setManageHidden(photo, false);
            setManageHidden(placeholder, true);
            photo.addEventListener(
                'error',
                () => {
                    photo.removeAttribute('src');
                    setManageHidden(photo, true);
                    setManageHidden(placeholder, false);
                },
                { once: true }
            );
        } else {
            if (photo instanceof HTMLImageElement) {
                photo.removeAttribute('src');
            }
            setManageHidden(photo, true);
            setManageHidden(placeholder, false);
        }

        const nameCell = rowNode.querySelector(
            '.col-name'
        );
        nameCell?.addEventListener('click', () => {
            if (studentId !== null) {
                void navigateManageStudent(studentId);
            }
        });

        bindStudentRowButton(
            rowNode,
            '.view-more-btn',
            'Opening...',
            () => studentId === null
                ? Promise.resolve(false)
                : navigateManageStudent(studentId)
        );
        bindStudentRowButton(
            rowNode,
            '.edit-row-btn',
            'Opening...',
            () => studentId === null
                ? Promise.resolve(false)
                : navigateManageStudent(studentId, 'edit')
        );
        bindStudentRowButton(
            rowNode,
            '.status-row-btn',
            'Opening...',
            () => studentId === null
                ? Promise.resolve(false)
                : navigateManageStudent(studentId, 'status')
        );

        return rowNode;
    }

    function bindStudentRowButton(
        rowNode,
        selector,
        label,
        operation
    ) {
        const button = rowNode.querySelector(selector);

        button?.addEventListener('click', event => {
            const target = event.currentTarget;
            void runWithButtonFeedback(
                target,
                label,
                operation
            );
        });
    }

    function setRowText(rowNode, selector, value) {
        const element = rowNode.querySelector(selector);

        if (element) {
            element.textContent = textOrDash(value);
        }
    }

    function normalizedManageUrl(value) {
        if (!value) {
            return null;
        }

        const source = String(value).trim();

        if (!source) {
            return null;
        }

        if (
            source.startsWith('data:') ||
            source.startsWith('blob:') ||
            source.startsWith('/')
        ) {
            return source;
        }

        try {
            const parsedUrl = new URL(
                source,
                window.location.origin
            );

            if (parsedUrl.protocol === 'https:') {
                return parsedUrl.href;
            }

            if (parsedUrl.protocol === 'http:') {
                if (
                    parsedUrl.hostname ===
                    window.location.hostname
                ) {
                    return parsedUrl.pathname +
                        parsedUrl.search +
                        parsedUrl.hash;
                }

                parsedUrl.protocol = 'https:';
                return parsedUrl.href;
            }
        } catch (error) {
            console.warn(
                'Invalid Student resource URL received.',
                error
            );
        }

        return `/${source.replace(/^\/+/, '')}`;
    }

    function bindManageStudentActions(context) {
        context.view
            .querySelector('#btn-add-student')
            ?.addEventListener('click', event => {
                const button = event.currentTarget;
                void runWithButtonFeedback(
                    button,
                    'Opening Form...',
                    () => window.erpNavigate({
                        role: 'admin',
                        view: 'add-student',
                        routeParams: [],
                        title: 'Add Student'
                    })
                );
            });

        context.view
            .querySelector('#student-searchBtn')
            ?.addEventListener('click', () => {
                context.page = 0;
                void loadStudents(context);
            });

        context.view
            .querySelector('#student-searchInput')
            ?.addEventListener('keydown', event => {
                if (event.key !== 'Enter') {
                    return;
                }

                event.preventDefault();
                context.page = 0;
                void loadStudents(context);
            });

        context.view
            .querySelector('#student-resetBtn')
            ?.addEventListener('click', () => {
                resetStudentListFilters(context);
                context.page = 0;
                void loadStudents(context);
            });

        context.view
            .querySelector('#student-backToTableBtn')
            ?.addEventListener('click', event => {
                const button = event.currentTarget;
                void runWithButtonFeedback(
                    button,
                    'Opening...',
                    navigateToStudentList
                );
            });

        context.view
            .querySelector('#student-editBtn')
            ?.addEventListener('click', event => {
                const button = event.currentTarget;
                void runWithButtonFeedback(
                    button,
                    'Preparing...',
                    () => enterStudentEditMode(context)
                );
            });

        const cancelEdit = () => {
            leaveStudentEditMode(context, true);
        };
        const saveEdit = event => {
            event.preventDefault();
            void requestStudentSave(context);
        };

        context.view
            .querySelector('#student-cancelEditBtn')
            ?.addEventListener('click', cancelEdit);
        context.view
            .querySelector('#student-bottom-cancelEditBtn')
            ?.addEventListener('click', cancelEdit);
        context.view
            .querySelector('#student-saveBtn')
            ?.addEventListener('click', saveEdit);
        context.view
            .querySelector('#student-bottom-saveBtn')
            ?.addEventListener('click', saveEdit);
        context.editForm.addEventListener('submit', saveEdit);

        context.editForm.addEventListener('input', event => {
            if (event.target instanceof Element) {
                clearManageFieldError(event.target);
            }
        });
        context.editForm.addEventListener('change', event => {
            if (event.target instanceof Element) {
                clearManageFieldError(event.target);
            }
        });

        context.view
            .querySelector('#student-enrollmentBtn')
            ?.addEventListener('click', () => {
                openStudentEnrollmentModal(context);
            });
        context.view
            .querySelector('#student-statusBtn')
            ?.addEventListener('click', () => {
                openStudentStatusModal(context);
            });
        context.view
            .querySelector('#btn-upload-student-document')
            ?.addEventListener('click', () => {
                openStudentDocumentModal(context);
            });

        const photoInput = context.view.querySelector(
            '#edit-studentProfilePhoto'
        );
        photoInput?.addEventListener('change', () => {
            const file = photoInput instanceof HTMLInputElement
                ? photoInput.files?.[0] || null
                : null;
            const name = context.view.querySelector(
                '#edit-studentProfilePhotoName'
            );

            if (name) {
                name.textContent = file
                    ? file.name
                    : 'No new photo selected';
            }

            clearManageFieldError(photoInput);
        });

        const previousLevel = context.view.querySelector(
            '#edit-studentPreviousLevel'
        );
        previousLevel?.addEventListener('change', () => {
            populatePreviousClassOptions(
                context,
                getManageControlValue(
                    context.view,
                    '#edit-studentPreviousLevel'
                )
            );
        });
    }

    function resetStudentListFilters(context) {
        [
            '#student-searchInput',
            '#student-academicYearFilter',
            '#student-classFilter',
            '#student-statusFilter'
        ].forEach(selector => {
            const control = context.view.querySelector(selector);

            if (
                control instanceof HTMLInputElement ||
                control instanceof HTMLSelectElement
            ) {
                control.value = '';
            }
        });
    }

    function bindManageStudentDialogs(context) {
        bindManageModal(
            context,
            '#student-enrollment-modal',
            '#student-enrollment-cancel'
        );
        bindManageModal(
            context,
            '#student-status-modal',
            '#student-status-cancel'
        );
        bindManageModal(
            context,
            '#student-document-modal',
            '#student-document-cancel'
        );

        context.view
            .querySelector('#student-enrollment-form')
            ?.addEventListener('submit', event => {
                event.preventDefault();
                void submitStudentEnrollment(context);
            });
        context.view
            .querySelector('#student-status-form')
            ?.addEventListener('submit', event => {
                event.preventDefault();
                void submitStudentStatus(context);
            });
        context.view
            .querySelector('#student-document-form')
            ?.addEventListener('submit', event => {
                event.preventDefault();
                void submitStudentDocument(context);
            });

        const academicYear = context.view.querySelector(
            '#student-enrollment-academicYear'
        );
        const classSelect = context.view.querySelector(
            '#student-enrollment-class'
        );

        academicYear?.addEventListener('change', () => {
            populateEnrollmentSections(context);
        });
        classSelect?.addEventListener('change', () => {
            populateEnrollmentSections(context);
        });

        const confirmOverlay = context.view.querySelector(
            '#student-confirm-overlay'
        );
        const confirmDialog = context.view.querySelector(
            '#student-confirm-dialog'
        );
        const cancel = context.view.querySelector(
            '#student-confirm-cancel'
        );
        const submit = context.view.querySelector(
            '#student-confirm-submit'
        );

        cancel?.addEventListener('click', () => {
            closeStudentConfirmation(context, false);
        });
        submit?.addEventListener('click', () => {
            closeStudentConfirmation(context, true);
        });
        confirmOverlay?.addEventListener('click', event => {
            if (event.target === confirmOverlay) {
                closeStudentConfirmation(context, false);
            }
        });
        confirmDialog?.addEventListener('keydown', event => {
            if (event.key === 'Escape') {
                event.preventDefault();
                closeStudentConfirmation(context, false);
            }
        });
    }

    function bindManageModal(context, modalSelector, cancelSelector) {
        const modal = context.view.querySelector(modalSelector);
        const cancel = context.view.querySelector(cancelSelector);

        cancel?.addEventListener('click', () => {
            closeManageModal(context, modalSelector);
        });
        modal?.addEventListener('click', event => {
            if (event.target === modal) {
                closeManageModal(context, modalSelector);
            }
        });
    }

    function openManageModal(context, selector) {
        const modal = context.view.querySelector(selector);

        if (!(modal instanceof HTMLElement)) {
            return;
        }

        context.previousFocus = document.activeElement instanceof HTMLElement
            ? document.activeElement
            : null;

        setManageHidden(modal, false);
        modal.setAttribute('aria-hidden', 'false');
        document.body.setAttribute('data-student-modal-open', 'true');

        const firstControl = modal.querySelector(
            'input:not([type="hidden"]), select, textarea, button'
        );
        window.setTimeout(() => {
            if (firstControl instanceof HTMLElement) {
                firstControl.focus();
            }
        }, 0);
    }

    function closeManageModal(context, selector) {
        const modal = context.view.querySelector(selector);

        if (!(modal instanceof HTMLElement)) {
            return;
        }

        setManageHidden(modal, true);
        modal.setAttribute('aria-hidden', 'true');

        const visibleModal = context.view.querySelector(
            '.ba-modal-backdrop:not(.hidden)'
        );

        if (!visibleModal) {
            document.body.removeAttribute(
                'data-student-modal-open'
            );
        }

        if (context.previousFocus instanceof HTMLElement) {
            context.previousFocus.focus({ preventScroll: true });
        }
        context.previousFocus = null;
    }

    function askStudentConfirmation(
        context,
        options
    ) {
        if (context.confirmationResolver) {
            context.confirmationResolver(false);
            context.confirmationResolver = null;
        }

        const overlay = context.view.querySelector(
            '#student-confirm-overlay'
        );
        const dialog = context.view.querySelector(
            '#student-confirm-dialog'
        );
        const title = context.view.querySelector(
            '#student-confirm-title'
        );
        const message = context.view.querySelector(
            '#student-confirm-message'
        );
        const submit = context.view.querySelector(
            '#student-confirm-submit'
        );

        if (
            !(overlay instanceof HTMLElement) ||
            !(dialog instanceof HTMLElement)
        ) {
            return Promise.resolve(false);
        }

        if (title) {
            title.textContent = String(
                read(options, 'title') || 'Confirm Action'
            );
        }
        if (message) {
            message.textContent = String(
                read(options, 'message') ||
                'Please confirm this action.'
            );
        }
        if (submit) {
            submit.textContent = String(
                read(options, 'confirmLabel') ||
                'Continue'
            );
            submit.setAttribute(
                'class',
                String(
                    read(options, 'confirmClass') ||
                    'btn-primary'
                )
            );
        }

        context.previousFocus = document.activeElement instanceof HTMLElement
            ? document.activeElement
            : null;

        addManageClasses(overlay, 'is-visible');
        overlay.setAttribute('aria-hidden', 'false');
        document.body.setAttribute(
            'data-student-modal-open',
            'true'
        );

        window.setTimeout(() => dialog.focus(), 0);

        return new Promise(resolve => {
            context.confirmationResolver = resolve;
        });
    }

    function closeStudentConfirmation(context, accepted) {
        const resolver = context.confirmationResolver;
        const overlay = context.view.querySelector(
            '#student-confirm-overlay'
        );

        context.confirmationResolver = null;
        removeManageClasses(overlay, 'is-visible');
        overlay?.setAttribute('aria-hidden', 'true');

        const visibleModal = context.view.querySelector(
            '.ba-modal-backdrop:not(.hidden)'
        );
        if (!visibleModal) {
            document.body.removeAttribute(
                'data-student-modal-open'
            );
        }

        if (context.previousFocus instanceof HTMLElement) {
            context.previousFocus.focus({ preventScroll: true });
        }
        context.previousFocus = null;

        if (typeof resolver === 'function') {
            resolver(Boolean(accepted));
        }
    }

    async function openStudentDetail(
        context,
        studentId,
        showLoading = true
    ) {
        const id = positiveInteger(studentId);

        if (id === null) {
            notifyError(
                new Error('A valid Student ID is required.'),
                'A valid Student ID is required.'
            );
            return false;
        }

        let loaderToken = null;
        context.detailView.setAttribute('aria-busy', 'true');
        addManageClasses(
            context.detailView,
            'student-detail-loading'
        );

        if (
            showLoading &&
            typeof showLoader === 'function'
        ) {
            loaderToken = showLoader(
                'Opening Student details...'
            );
        }

        try {
            const response = await requestManageGet(
                `/students/${id}`
            );
            const profile = toRecord(
                unwrapManageResponse(response)
            );

            context.currentStudentId = id;
            context.currentProfile = profile;

            renderStudentProfile(context, profile);
            setManageHidden(context.tableView, true);
            setManageHidden(context.detailView, false);
            leaveStudentEditMode(context, false);

            window.scrollTo({
                top: 0,
                behavior: 'auto'
            });

            return true;
        } catch (error) {
            notifyError(
                error,
                'Student profile could not be loaded.'
            );
            return false;
        } finally {
            context.detailView.removeAttribute('aria-busy');
            removeManageClasses(
                context.detailView,
                'student-detail-loading'
            );

            if (
                loaderToken &&
                typeof hideLoader === 'function'
            ) {
                hideLoader(loaderToken);
            }
        }
    }

    function profileSection(profile, key) {
        return toRecord(read(profile, key));
    }

    function meaningfulReferenceLabel(value, id) {
        if (value === null || value === undefined) {
            return null;
        }

        const normalizedId = positiveInteger(id);
        const text = String(value).trim();

        if (!text) {
            return null;
        }

        if (
            normalizedId !== null &&
            text === String(normalizedId)
        ) {
            return null;
        }

        return text;
    }

    function referenceRecordLabel(recordValue, type) {
        const record = toRecord(recordValue);

        if (type === 'academicYear') {
            return meaningfulReferenceLabel(
                read(record, 'academicYearName') ??
                read(record, 'name') ??
                read(record, 'academicYearCode') ??
                read(record, 'code'),
                read(record, 'academicYearId') ??
                read(record, 'id')
            );
        }

        if (type === 'class') {
            return meaningfulReferenceLabel(
                read(record, 'className') ??
                read(record, 'name') ??
                read(record, 'classCode') ??
                read(record, 'code'),
                read(record, 'classId') ??
                read(record, 'id')
            );
        }

        return meaningfulReferenceLabel(
            read(record, 'sectionName') ??
            read(record, 'name') ??
            read(record, 'sectionCode') ??
            read(record, 'code'),
            read(record, 'sectionId') ??
            read(record, 'id')
        );
    }

    function enrollmentReferenceLabel(
        context,
        enrollmentValue,
        type
    ) {
        const enrollment = toRecord(enrollmentValue);
        const configuration = {
            academicYear: {
                idKeys: ['academicYearId', 'yearId'],
                nameKeys: [
                    'academicYearName',
                    'yearName',
                    'academicYearCode'
                ],
                objectKeys: ['academicYear', 'year'],
                collection: 'academicYears'
            },
            class: {
                idKeys: ['classId', 'studentClassId'],
                nameKeys: [
                    'className',
                    'studentClassName',
                    'classDisplayName',
                    'classCode'
                ],
                objectKeys: ['studentClass', 'classInfo'],
                collection: 'classes'
            },
            section: {
                idKeys: ['sectionId', 'studentSectionId'],
                nameKeys: [
                    'sectionName',
                    'studentSectionName',
                    'sectionCode'
                ],
                objectKeys: ['section', 'sectionInfo'],
                collection: 'sections'
            }
        }[type];

        if (!configuration) {
            return '-';
        }

        const id = positiveInteger(
            configuration.idKeys
                .map(key => read(enrollment, key))
                .find(value => value !== null && value !== undefined)
        );

        for (const key of configuration.nameKeys) {
            const directLabel = meaningfulReferenceLabel(
                read(enrollment, key),
                id
            );

            if (directLabel) {
                return directLabel;
            }
        }

        for (const key of configuration.objectKeys) {
            const nestedLabel = referenceRecordLabel(
                read(enrollment, key),
                type
            );

            if (nestedLabel) {
                return nestedLabel;
            }
        }

        if (id !== null) {
            const collection = Array.isArray(
                context.referenceData?.[
                    configuration.collection
                    ]
            )
                ? context.referenceData[
                    configuration.collection
                    ]
                : [];

            const matchingRecord = collection.find(item => {
                const record = toRecord(item);
                const recordId = positiveInteger(
                    type === 'academicYear'
                        ? read(record, 'academicYearId') ??
                        read(record, 'id')
                        : type === 'class'
                            ? read(record, 'classId') ??
                            read(record, 'id')
                            : read(record, 'sectionId') ??
                            read(record, 'id')
                );

                return recordId === id;
            });

            const referenceLabel = referenceRecordLabel(
                matchingRecord,
                type
            );

            if (referenceLabel) {
                return referenceLabel;
            }
        }

        /* Never expose internal database IDs as profile labels. */
        return '-';
    }

    function manageFirstDefined(...values) {
        for (const value of values) {
            if (value !== undefined && value !== null) {
                return value;
            }
        }

        return null;
    }

    function joiningClassLabel(context, personal) {
        const joiningClassId = positiveInteger(
            read(personal, 'joiningClassId')
        );

        if (joiningClassId === null) {
            return '-';
        }

        const matchedClass = context.referenceData.classes.find(item =>
            positiveInteger(
                manageFirstDefined(
                    read(item, 'classId'),
                    read(item, 'id')
                )
            ) === joiningClassId
        );

        return textOrDash(
            manageFirstDefined(
                read(matchedClass, 'className'),
                read(matchedClass, 'name'),
                read(matchedClass, 'classCode'),
                read(matchedClass, 'code')
            )
        );
    }

    function joiningTermLabel(context, personal) {
        const joiningTermId = positiveInteger(
            read(personal, 'joiningTermId')
        );

        if (joiningTermId === null) {
            return '-';
        }

        const matchedTerm = context.referenceData.academicTerms.find(item =>
            positiveInteger(
                manageFirstDefined(
                    read(item, 'termId'),
                    read(item, 'id')
                )
            ) === joiningTermId
        );

        return textOrDash(
            manageFirstDefined(
                read(matchedTerm, 'termName'),
                read(matchedTerm, 'name'),
                read(matchedTerm, 'termCode'),
                read(matchedTerm, 'code')
            )
        );
    }

    function renderStudentProfile(context, profileValue) {
        const profile = toRecord(profileValue);
        const personal = profileSection(profile, 'personal');
        const parent = profileSection(profile, 'parent');
        const enrollment = profileSection(
            profile,
            'currentEnrollment'
        );
        const medical = profileSection(profile, 'medical');
        const academic = profileSection(
            profile,
            'academicHistory'
        );
        const hostel = profileSection(profile, 'hostel');
        const transport = profileSection(profile, 'transport');
        const documents = toArray(
            read(profile, 'documents')
        );

        const fullName = resolveStudentFullName(personal);
        const admissionNo = read(personal, 'admissionNo');
        const joiningClass = joiningClassLabel(
            context,
            personal
        );
        const joiningTerm = joiningTermLabel(
            context,
            personal
        );
        const status = read(personal, 'studentStatus');
        const academicYearLabel =
            enrollmentReferenceLabel(
                context,
                enrollment,
                'academicYear'
            );
        const classLabel = enrollmentReferenceLabel(
            context,
            enrollment,
            'class'
        );
        const sectionLabel = enrollmentReferenceLabel(
            context,
            enrollment,
            'section'
        );
        const placement = [
            classLabel,
            sectionLabel
        ].filter(value => value && value !== '-')
            .join(' / ') || '-';

        setManageText(
            context.view,
            '#detail-studentNameHeader',
            fullName
        );
        setManageText(
            context.view,
            '#detail-studentAdmissionNoHeader',
            admissionNo ? `(${admissionNo})` : ''
        );
        setManageText(
            context.view,
            '#view-studentName',
            fullName
        );
        setManageText(
            context.view,
            '#summary-studentAdmissionNo',
            admissionNo
        );
        setManageText(
            context.view,
            '#view-studentJoiningClass',
            joiningClass
        );
        setManageText(
            context.view,
            '#view-studentJoiningTerm',
            joiningTerm
        );
        setManageText(
            context.view,
            '#summary-studentPlacement',
            placement
        );
        setStudentStatusBadge(
            context.view.querySelector(
                '#summary-studentStatus'
            ),
            status
        );

        renderStudentProfilePhoto(context, personal);
        bindStudentPersonalFields(context, personal);
        bindStudentEnrollmentFields(
            context,
            enrollment,
            {
                academicYearLabel,
                classLabel,
                sectionLabel
            }
        );
        bindStudentParentFields(context, parent);
        bindStudentMedicalFields(context, medical);
        bindStudentAcademicFields(context, academic);
        bindStudentHostelFields(context, hostel);
        bindStudentTransportFields(context, transport);
        renderStudentDocuments(
            context,
            documents
        );
    }

    function resolveStudentFullName(personal) {
        const fullName = read(personal, 'fullName');

        if (fullName && String(fullName).trim()) {
            return String(fullName).trim();
        }

        return [
            read(personal, 'firstName'),
            read(personal, 'middleName'),
            read(personal, 'lastName')
        ].filter(value => value && String(value).trim())
            .join(' ') || '-';
    }

    function setManageText(root, selector, value, formatter = null) {
        const element = root.querySelector(selector);

        if (!element) {
            return;
        }

        const result = typeof formatter === 'function'
            ? formatter(value)
            : textOrDash(value);

        element.textContent = String(result);
    }

    function setManageControl(root, selector, value) {
        const control = root.querySelector(selector);

        if (
            !(
                control instanceof HTMLInputElement ||
                control instanceof HTMLSelectElement ||
                control instanceof HTMLTextAreaElement
            )
        ) {
            return;
        }

        const normalized = value === null || value === undefined
            ? ''
            : String(value);
        const calendar = read(control, '_flatpickr');

        if (
            calendar &&
            typeof read(calendar, 'setDate') === 'function'
        ) {
            read(calendar, 'setDate').call(
                calendar,
                normalized || null,
                false
            );
        } else {
            control.value = normalized;
        }
    }

    function bindStudentField(
        context,
        suffix,
        value,
        formatter = null
    ) {
        setManageText(
            context.view,
            `#view-student${suffix}`,
            value,
            formatter
        );
        setManageControl(
            context.view,
            `#edit-student${suffix}`,
            value
        );
    }

    function bindStudentPersonalFields(context, personal) {
        bindStudentField(context, 'FirstName', read(personal, 'firstName'));
        bindStudentField(context, 'MiddleName', read(personal, 'middleName'));
        bindStudentField(context, 'LastName', read(personal, 'lastName'));
        bindStudentField(context, 'LearnerLin', read(personal, 'learnerLin'));
        bindStudentField(context, 'Gender', read(personal, 'gender'), enumLabel);
        bindStudentField(context, 'Dob', read(personal, 'dateOfBirth'), dateLabel);
        bindStudentField(context, 'AdmissionYear', read(personal, 'admissionYear'));
        bindStudentField(context, 'Nationality', read(personal, 'nationality'));
        bindStudentField(
            context,
            'NationalIdPassport',
            read(personal, 'nationalIdPassport')
        );
        bindStudentField(context, 'HouseNo', read(personal, 'houseNo'));
        bindStudentField(context, 'Street', read(personal, 'street'));
        bindStudentField(context, 'Village', read(personal, 'village'));
        bindStudentField(context, 'TownCity', read(personal, 'townCity'));
        bindStudentField(context, 'District', read(personal, 'district'));
        bindStudentField(context, 'County', read(personal, 'county'));
        bindStudentField(context, 'SubCounty', read(personal, 'subCounty'));
        bindStudentField(context, 'State', read(personal, 'state'));
        bindStudentField(context, 'Country', read(personal, 'country'));
        bindStudentField(context, 'PostalCode', read(personal, 'postalCode'));
        setManageText(context.view, '#view-studentApplicationId', read(personal, 'applicationId'));
        setManageText(context.view, '#view-studentAdmissionNo', read(personal, 'admissionNo'));
    }

    function bindStudentEnrollmentFields(
        context,
        enrollment,
        labels = {}
    ) {
        setManageText(
            context.view,
            '#view-studentAcademicYear',
            labels.academicYearLabel ??
            enrollmentReferenceLabel(
                context,
                enrollment,
                'academicYear'
            )
        );
        setManageText(
            context.view,
            '#view-studentClass',
            labels.classLabel ??
            enrollmentReferenceLabel(
                context,
                enrollment,
                'class'
            )
        );
        setManageText(
            context.view,
            '#view-studentSection',
            labels.sectionLabel ??
            enrollmentReferenceLabel(
                context,
                enrollment,
                'section'
            )
        );
        setManageText(context.view, '#view-studentAdmissionType', read(enrollment, 'admissionType'), enumLabel);
        setManageText(context.view, '#view-studentJoiningDate', read(enrollment, 'joiningDate'), dateLabel);
        setManageText(context.view, '#view-studentRollNo', read(enrollment, 'rollNo'));
        setManageText(context.view, '#view-studentEnrollmentStatus', read(enrollment, 'enrollmentStatus'), enumLabel);
        setManageText(
            context.view,
            '#view-studentScholarshipRequired',
            read(enrollment, 'scholarshipRequired'),
            booleanLabel
        );
        setManageText(context.view, '#view-studentEnrollmentRemarks', read(enrollment, 'remarks'));
    }

    function bindStudentParentFields(context, parent) {
        const bindings = [
            ['FatherName', 'fatherName'],
            ['FatherUin', 'fatherUin'],
            ['FatherPhone', 'fatherPhone'],
            ['FatherAltPhone', 'fatherAlternatePhone'],
            ['FatherEmail', 'fatherEmail'],
            ['FatherOccupation', 'fatherOccupation'],
            ['FatherEmployer', 'fatherEmployer'],
            ['FatherDesignation', 'fatherDesignation'],
            ['FatherIncome', 'fatherAnnualIncome'],
            ['MotherName', 'motherName'],
            ['MotherUin', 'motherUin'],
            ['MotherPhone', 'motherPhone'],
            ['MotherAltPhone', 'motherAlternatePhone'],
            ['MotherEmail', 'motherEmail'],
            ['MotherOccupation', 'motherOccupation'],
            ['MotherEmployer', 'motherEmployer'],
            ['MotherDesignation', 'motherDesignation'],
            ['MotherIncome', 'motherAnnualIncome'],
            ['GuardianName', 'guardianName'],
            ['GuardianUin', 'guardianUin'],
            ['GuardianRelationship', 'guardianRelationship'],
            ['GuardianPhone', 'guardianPhone'],
            ['GuardianAltPhone', 'guardianAlternatePhone'],
            ['GuardianEmail', 'guardianEmail'],
            ['GuardianOccupation', 'guardianOccupation'],
            ['PreferredContact', 'preferredContact'],
            ['FeeResponsibility', 'feeResponsibility'],
            ['EmergencyName', 'emergencyContactName'],
            ['EmergencyPhone', 'emergencyContactPhone'],
            ['EmergencyRelationship', 'emergencyContactRelationship'],
            ['ParentRemarks', 'remarks']
        ];

        bindings.forEach(([suffix, key]) => {
            const formatter = [
                'PreferredContact',
                'FeeResponsibility',
                'GuardianRelationship'
            ].includes(suffix)
                ? enumLabel
                : null;
            bindStudentField(
                context,
                suffix,
                read(parent, key),
                formatter
            );
        });

        bindStudentField(
            context,
            'ParentsLivingTogether',
            read(parent, 'parentsLivingTogether'),
            booleanLabel
        );
        setManageControl(
            context.view,
            '#edit-studentParentsLivingTogether',
            read(parent, 'parentsLivingTogether')
        );
    }

    function bindStudentMedicalFields(context, medical) {
        const bloodGroup = read(medical, 'bloodGroup');
        const bloodGroupCode = read(medical, 'bloodGroupCode');

        setManageText(
            context.view,
            '#view-studentBloodGroup',
            bloodGroupCode || enumLabel(bloodGroup)
        );
        setManageControl(
            context.view,
            '#edit-studentBloodGroup',
            bloodGroup
        );

        const bindings = [
            ['HeightCm', 'heightCm'],
            ['WeightKg', 'weightKg'],
            ['Allergies', 'allergies'],
            ['ChronicConditions', 'chronicConditions'],
            ['OngoingMedication', 'ongoingMedication'],
            ['SpecialNeeds', 'specialNeeds'],
            ['DoctorName', 'emergencyDoctorName'],
            ['DoctorMobile', 'emergencyDoctorMobile'],
            ['PreferredHospital', 'preferredHospital'],
            ['MedicalRemarks', 'remarks']
        ];

        bindings.forEach(([suffix, key]) => {
            bindStudentField(context, suffix, read(medical, key));
        });

        bindStudentField(
            context,
            'FitForSports',
            read(medical, 'fitForSports'),
            booleanLabel
        );
        setManageControl(
            context.view,
            '#edit-studentFitForSports',
            read(medical, 'fitForSports')
        );
    }

    function bindStudentAcademicFields(context, academic) {
        const bindings = [
            ['FormerSchoolName', 'formerSchoolName'],
            ['FormerSchoolCode', 'formerSchoolCode'],
            ['FormerSchoolLin', 'formerSchoolLin'],
            ['FormerSchoolAddress', 'formerSchoolAddress'],
            ['SchoolType', 'schoolType'],
            ['TransferReason', 'transferReason'],
            ['PreviousAcademicYear', 'previousAcademicYear'],
            ['PreviousClass', 'previousClass'],
            ['PreviousSection', 'previousSection'],
            ['PreviousStream', 'previousStream'],
            ['PleIndexNumber', 'pleIndexNumber'],
            ['PleAggregate', 'pleAggregate'],
            ['UceIndexNumber', 'uceIndexNumber'],
            ['UceResult', 'uceResult'],
            ['UaceIndexNumber', 'uaceIndexNumber'],
            ['UaceResult', 'uaceResult'],
            ['SubjectMarks', 'subjectMarks'],
            ['AcademicRemarks', 'remarks']
        ];

        bindings.forEach(([suffix, key]) => {
            const formatter = suffix === 'SchoolType'
                ? enumLabel
                : null;
            bindStudentField(
                context,
                suffix,
                read(academic, key),
                formatter
            );
        });

        synchronizePreviousLevelFromClass(
            context,
            read(academic, 'previousClass')
        );
    }

    function synchronizePreviousLevelFromClass(
        context,
        previousClass
    ) {
        const className = String(previousClass || '').trim();
        const matchedClass = context.referenceData.previousClasses.find(item => {
            const record = toRecord(item);
            const name = String(
                read(record, 'className') ||
                read(record, 'name') || ''
            ).trim();
            const code = String(
                read(record, 'classCode') ||
                read(record, 'code') || ''
            ).trim();

            return className &&
                (name === className || code === className);
        });
        const levelId = positiveInteger(
            read(matchedClass, 'levelId') ||
            readPath(matchedClass, 'level', 'levelId')
        );

        setManageControl(
            context.view,
            '#edit-studentPreviousLevel',
            levelId
        );
        populatePreviousClassOptions(
            context,
            levelId,
            className
        );
        setManageText(
            context.view,
            '#view-studentPreviousLevel',
            levelId === null
                ? null
                : resolveLevelName(context, levelId)
        );
    }

    function resolveLevelName(context, levelId) {
        const level = toArray(
            read(context.referenceData, 'levels')
        ).find(item => positiveInteger(
            read(item, 'levelId') || read(item, 'id')
        ) === levelId);

        return read(level, 'levelName') ||
            read(level, 'name') ||
            null;
    }

    function bindStudentHostelFields(context, hostel) {
        const enabled = Object.keys(hostel).length > 0 &&
            read(hostel, 'active') !== false;

        bindStudentField(
            context,
            'RequiresHostel',
            enabled,
            booleanLabel
        );
        setManageControl(
            context.view,
            '#edit-studentRequiresHostel',
            enabled
        );

        const bindings = [
            ['Hostel', 'hostelName'],
            ['HostelRoom', 'roomName'],
            ['HostelBed', 'bedName'],
            ['HostelStartDate', 'allocationStartDate'],
            ['HostelEndDate', 'allocationEndDate'],
            ['HostelGuardianName', 'localGuardianName'],
            ['HostelGuardianMobile', 'localGuardianMobile'],
            ['HostelGuardianRelation', 'localGuardianRelation'],
            ['HostelRemarks', 'remarks']
        ];

        bindings.forEach(([suffix, key]) => {
            bindStudentField(
                context,
                suffix,
                read(hostel, key),
                suffix.endsWith('Date') ? dateLabel : null
            );
        });

        setManageControl(context.view, '#edit-studentHostelId', read(hostel, 'hostelId'));
        setManageControl(context.view, '#edit-studentHostelRoomId', read(hostel, 'roomId'));
        setManageControl(context.view, '#edit-studentHostelBedId', read(hostel, 'bedId'));
    }

    function bindStudentTransportFields(context, transport) {
        const enabled = Object.keys(transport).length > 0 &&
            read(transport, 'active') !== false;

        bindStudentField(
            context,
            'RequiresTransport',
            enabled,
            booleanLabel
        );
        setManageControl(
            context.view,
            '#edit-studentRequiresTransport',
            enabled
        );

        const bindings = [
            ['TransportRoute', 'routeName'],
            ['TransportPickupPoint', 'pickupPointName'],
            ['TransportVehicle', 'vehicleName'],
            ['TransportStartDate', 'transportStartDate'],
            ['TransportEndDate', 'transportEndDate'],
            ['TransportSeatNumber', 'seatNumber'],
            ['TransportEmergencyContact', 'emergencyContact'],
            ['TransportEmergencyMobile', 'emergencyMobile'],
            ['TransportRemarks', 'remarks']
        ];

        bindings.forEach(([suffix, key]) => {
            bindStudentField(
                context,
                suffix,
                read(transport, key),
                suffix.endsWith('Date') ? dateLabel : null
            );
        });

        setManageControl(context.view, '#edit-studentTransportRouteId', read(transport, 'routeId'));
        setManageControl(context.view, '#edit-studentTransportPickupPointId', read(transport, 'pickupPointId'));
        setManageControl(context.view, '#edit-studentTransportVehicleId', read(transport, 'vehicleId'));
    }

    function renderStudentProfilePhoto(context, personal) {
        const image = context.view.querySelector(
            '#view-studentProfilePhoto'
        );
        const placeholder = context.view.querySelector(
            '#view-studentProfilePhotoPlaceholder'
        );
        const photoUrl = normalizedManageUrl(
            read(personal, 'photoUrl')
        );

        if (
            image instanceof HTMLImageElement &&
            photoUrl
        ) {
            image.src = photoUrl;
            setManageHidden(image, false);
            setManageHidden(placeholder, true);
            image.addEventListener(
                'error',
                () => {
                    image.removeAttribute('src');
                    setManageHidden(image, true);
                    setManageHidden(placeholder, false);
                },
                { once: true }
            );
        } else {
            if (image instanceof HTMLImageElement) {
                image.removeAttribute('src');
            }
            setManageHidden(image, true);
            setManageHidden(placeholder, false);
        }

        const fileInput = context.view.querySelector(
            '#edit-studentProfilePhoto'
        );
        if (fileInput instanceof HTMLInputElement) {
            fileInput.value = '';
        }
        setManageText(
            context.view,
            '#edit-studentProfilePhotoName',
            'No new photo selected'
        );
    }

    function setStudentStatusBadge(element, statusValue) {
        if (!(element instanceof Element)) {
            return;
        }

        const status = String(statusValue || 'UNKNOWN');
        element.textContent = enumLabel(status);
        element.setAttribute(
            'class',
            `status-badge badge badge-${status.toLowerCase()}`
        );
    }

    function renderStudentDocuments(context, documentsValue) {
        const container = context.view.querySelector(
            '#student-documents-view-container'
        );
        const count = context.view.querySelector(
            '#student-document-count'
        );
        const documents = toArray(documentsValue)
            .filter(value => read(value, 'active') !== false);

        if (!(container instanceof HTMLElement)) {
            return;
        }

        container.replaceChildren();

        if (count) {
            count.textContent = documents.length > 0
                ? `(${documents.length})`
                : '(0)';
        }

        documents.forEach(documentValue => {
            const documentCard = createStudentDocumentCard(
                context,
                documentValue
            );

            container.appendChild(documentCard);
        });
    }

    /**
     * @param {Object<string, *>} context
     * @param {*} documentValue
     * @returns {HTMLElement}
     */
    function createStudentDocumentCard(
        context,
        documentValue
    ) {
        const documentData = toRecord(documentValue);
        const card = document.createElement('article');
        const header = document.createElement('div');
        const titleBlock = document.createElement('div');
        const title = document.createElement('strong');
        const type = document.createElement('span');
        const status = document.createElement('span');
        const meta = document.createElement('div');
        const actions = document.createElement('div');
        const viewButton = document.createElement('a');
        const verifyButton = document.createElement('button');
        const removeButton = document.createElement('button');

        card.setAttribute('class', 'emp-document-card');
        header.setAttribute('class', 'emp-document-card-header');
        titleBlock.setAttribute('class', 'student-document-title-block');
        title.setAttribute('class', 'emp-document-name');
        type.setAttribute('class', 'emp-document-type');
        status.setAttribute('class', 'status-badge badge');
        meta.setAttribute('class', 'emp-document-meta');
        actions.setAttribute('class', 'emp-document-actions');

        title.textContent = textOrDash(
            read(documentData, 'documentName')
        );
        type.textContent = enumLabel(
            read(documentData, 'documentType')
        );
        setStudentStatusBadge(
            status,
            read(documentData, 'documentStatus') || 'PENDING'
        );

        const metadataParts = [
            read(documentData, 'documentNumber')
                ? `No: ${read(documentData, 'documentNumber')}`
                : null,
            read(documentData, 'originalFileName'),
            fileSizeLabel(read(documentData, 'fileSize')),
            read(documentData, 'uploadedAt')
                ? `Uploaded: ${dateLabel(read(documentData, 'uploadedAt'))}`
                : null
        ].filter(Boolean);
        meta.textContent = metadataParts.join(' · ') || '-';

        titleBlock.appendChild(title);
        titleBlock.appendChild(type);
        header.appendChild(titleBlock);
        header.appendChild(status);
        card.appendChild(header);
        card.appendChild(meta);

        viewButton.setAttribute(
            'class',
            'btn-secondary btn-sm emp-document-view-btn'
        );
        viewButton.setAttribute('target', '_blank');
        viewButton.setAttribute('rel', 'noopener noreferrer');
        viewButton.textContent = 'Open';
        viewButton.href = resolveStudentDocumentUrl(
            context,
            documentData
        );

        verifyButton.type = 'button';
        verifyButton.setAttribute('class', 'btn-primary btn-sm');
        verifyButton.textContent = 'Verify';
        verifyButton.disabled =
            read(documentData, 'documentStatus') === 'VERIFIED';
        verifyButton.addEventListener('click', () => {
            void verifyStudentDocument(
                context,
                documentData,
                verifyButton
            );
        });

        removeButton.type = 'button';
        removeButton.setAttribute('class', 'btn-danger btn-sm');
        removeButton.textContent = 'Remove';
        removeButton.addEventListener('click', () => {
            void removeStudentDocument(
                context,
                documentData,
                removeButton
            );
        });

        actions.appendChild(viewButton);
        actions.appendChild(verifyButton);
        actions.appendChild(removeButton);
        card.appendChild(actions);

        return card;
    }

    function resolveStudentDocumentUrl(context, documentData) {
        const supplied = normalizedManageUrl(
            read(documentData, 'documentUrl')
        );

        if (supplied) {
            return supplied;
        }

        const studentId = positiveInteger(
            context.currentStudentId
        );
        const documentId = positiveInteger(
            read(documentData, 'documentId')
        );

        if (studentId === null || documentId === null) {
            return '#';
        }

        return `/api/students/${studentId}` +
            `/documents/${documentId}/download`;
    }

    async function verifyStudentDocument(
        context,
        documentData,
        button
    ) {
        const studentId = positiveInteger(
            context.currentStudentId
        );
        const documentId = positiveInteger(
            read(documentData, 'documentId')
        );

        if (studentId === null || documentId === null) {
            notifyError(
                new Error('Student document ID is unavailable.'),
                'Student document ID is unavailable.'
            );
            return;
        }

        const accepted = await askStudentConfirmation(
            context,
            {
                title: 'Verify Student Document',
                message:
                    `Mark ${textOrDash(read(documentData, 'documentName'))} as verified?`,
                confirmLabel: 'Verify Document',
                confirmClass: 'btn-primary'
            }
        );

        if (!accepted) {
            return;
        }

        try {
            await runManageOperation(
                context,
                {
                    button,
                    title: 'Verifying Document',
                    message:
                        'Please wait while the Student document is verified.'
                },
                () => requestManagePut(
                    `/students/${studentId}` +
                    `/documents/${documentId}/verification`,
                    {
                        documentStatus: 'VERIFIED',
                        remarks: null,
                        operationId: createManageOperationId(
                            'student-document-verify'
                        )
                    }
                )
            );

            cancelQueuedGlobalSync();
            await refreshCurrentStudentProfile(context);
            notifySuccess('Student document verified successfully.');
        } catch (error) {
            notifyError(
                error,
                'Student document could not be verified.'
            );
        }
    }

    async function removeStudentDocument(
        context,
        documentData,
        button
    ) {
        const studentId = positiveInteger(
            context.currentStudentId
        );
        const documentId = positiveInteger(
            read(documentData, 'documentId')
        );

        if (studentId === null || documentId === null) {
            notifyError(
                new Error('Student document ID is unavailable.'),
                'Student document ID is unavailable.'
            );
            return;
        }

        const accepted = await askStudentConfirmation(
            context,
            {
                title: 'Remove Student Document',
                message:
                    `Remove ${textOrDash(read(documentData, 'documentName'))} from this Student profile?`,
                confirmLabel: 'Remove Document',
                confirmClass: 'btn-danger'
            }
        );

        if (!accepted) {
            return;
        }

        try {
            await runManageOperation(
                context,
                {
                    button,
                    title: 'Removing Document',
                    message:
                        'Please wait while the Student document is removed.'
                },
                () => requestManageDelete(
                    `/students/${studentId}` +
                    `/documents/${documentId}`
                )
            );

            cancelQueuedGlobalSync();
            await refreshCurrentStudentProfile(context);
            notifySuccess('Student document removed successfully.');
        } catch (error) {
            notifyError(
                error,
                'Student document could not be removed.'
            );
        }
    }

    async function refreshCurrentStudentProfile(context) {
        const studentId = positiveInteger(
            context.currentStudentId
        );

        if (studentId === null) {
            return false;
        }

        const response = await requestManageGet(
            `/students/${studentId}`
        );
        const profile = toRecord(
            unwrapManageResponse(response)
        );

        context.currentProfile = profile;
        renderStudentProfile(context, profile);
        return true;
    }

    async function enterStudentEditMode(context) {
        if (!context.currentProfile) {
            return false;
        }

        clearManageEditValidation(context);
        addManageClasses(
            context.detailView,
            'student-edit-preparing'
        );

        try {
            await prepareManageReferenceData(context);

            renderStudentProfile(
                context,
                context.currentProfile
            );
            context.currentMode = 'edit';
            context.detailView.setAttribute(
                'data-mode',
                'edit'
            );
            addManageClasses(
                context.detailView,
                'is-editing'
            );

            context.view
                .querySelectorAll('.student-view-control')
                .forEach(element => {
                    setManageHidden(element, true);
                });
            context.view
                .querySelectorAll('.student-edit-control')
                .forEach(element => {
                    setManageHidden(element, false);
                });

            const firstInput = context.view.querySelector(
                '#edit-studentFirstName'
            );
            window.setTimeout(() => {
                if (firstInput instanceof HTMLElement) {
                    firstInput.focus();
                }
            }, 0);

            return true;
        } finally {
            removeManageClasses(
                context.detailView,
                'student-edit-preparing'
            );
        }
    }

    function leaveStudentEditMode(context, restoreProfile) {
        context.currentMode = 'view';
        context.detailView.setAttribute('data-mode', 'view');
        removeManageClasses(
            context.detailView,
            'is-editing',
            'student-edit-preparing'
        );

        context.view
            .querySelectorAll('.student-view-control')
            .forEach(element => {
                setManageHidden(element, false);
            });
        context.view
            .querySelectorAll('.student-edit-control')
            .forEach(element => {
                setManageHidden(element, true);
            });

        clearManageEditValidation(context);

        if (restoreProfile && context.currentProfile) {
            renderStudentProfile(
                context,
                context.currentProfile
            );
        }
    }

    function clearManageFieldError(field) {
        if (!(field instanceof Element)) {
            return;
        }

        removeManageClasses(field, 'emp-input-invalid');
        field.removeAttribute('aria-invalid');

        const group = field.closest('.form-group');
        if (group) {
            removeManageClasses(group, 'emp-field-invalid');
            group.removeAttribute('data-error');
        }
    }

    function setManageFieldError(field, message) {
        if (!(field instanceof Element)) {
            return;
        }

        addManageClasses(field, 'emp-input-invalid');
        field.setAttribute('aria-invalid', 'true');

        const group = field.closest('.form-group');
        if (group) {
            addManageClasses(group, 'emp-field-invalid');
            group.setAttribute('data-error', message);
        }
    }

    function clearManageEditValidation(context) {
        context.editForm
            .querySelectorAll('.emp-input-invalid')
            .forEach(element => {
                clearManageFieldError(element);
            });
        context.editForm
            .querySelectorAll('.emp-field-invalid')
            .forEach(element => {
                removeManageClasses(
                    element,
                    'emp-field-invalid'
                );
                delete element.dataset.error;
            });

        const summary = context.view.querySelector(
            '#edit-student-validation-summary'
        );
        const summaryText = context.view.querySelector(
            '#edit-student-validation-summary-text'
        );
        setManageHidden(summary, true);
        if (summaryText) {
            summaryText.textContent = '';
        }
    }

    function showManageEditValidation(context, errors) {
        const messages = Array.from(
            new Set(
                errors
                    .map(error => read(error, 'message'))
                    .filter(Boolean)
                    .map(String)
            )
        );
        const summary = context.view.querySelector(
            '#edit-student-validation-summary'
        );
        const summaryText = context.view.querySelector(
            '#edit-student-validation-summary-text'
        );

        if (summaryText) {
            summaryText.textContent = messages.join(' ');
        }
        setManageHidden(summary, false);

        const firstField = read(errors[0], 'field');
        if (firstField instanceof HTMLElement) {
            firstField.scrollIntoView({
                behavior: 'smooth',
                block: 'center'
            });
            window.setTimeout(() => {
                firstField.focus({ preventScroll: true });
            }, 220);
        }
    }

    function validateStudentEdit(context) {
        clearManageEditValidation(context);

        const errors = [];
        const required = [
            ['#edit-studentFirstName', 'First Name is required.'],
            ['#edit-studentGender', 'Gender is required.'],
            ['#edit-studentDob', 'Date of Birth is required.'],
            ['#edit-studentAdmissionYear', 'Admission Year is required.'],
            ['#edit-studentPreferredContact', 'Preferred Contact is required.'],
            ['#edit-studentFeeResponsibility', 'Fee Responsibility is required.'],
            ['#edit-studentParentsLivingTogether', 'Parents Living Together is required.']
        ];

        required.forEach(([selector, message]) => {
            const field = context.editForm.querySelector(selector);

            if (!getManageControlValue(context.editForm, selector)) {
                setManageFieldError(field, message);
                errors.push({ field, message });
            }
        });

        const dobField = context.editForm.querySelector(
            '#edit-studentDob'
        );
        const dob = getManageControlValue(
            context.editForm,
            '#edit-studentDob'
        );

        if (dob && dob > maximumStudentDobIso()) {
            const message =
                'Student must be at least three years old.';
            setManageFieldError(dobField, message);
            errors.push({ field: dobField, message });
        }

        const admissionYearField = context.editForm.querySelector(
            '#edit-studentAdmissionYear'
        );
        const admissionYear = finiteNumber(
            getManageControlValue(
                context.editForm,
                '#edit-studentAdmissionYear'
            )
        );
        if (
            admissionYear !== null &&
            (
                !Number.isInteger(admissionYear) ||
                admissionYear < 1900 ||
                admissionYear > 2100
            )
        ) {
            const message =
                'Admission Year must be between 1900 and 2100.';
            setManageFieldError(admissionYearField, message);
            errors.push({
                field: admissionYearField,
                message
            });
        }

        validateManageEmail(
            context,
            '#edit-studentFatherEmail',
            'Father Email',
            errors
        );
        validateManageEmail(
            context,
            '#edit-studentMotherEmail',
            'Mother Email',
            errors
        );
        validateManageEmail(
            context,
            '#edit-studentGuardianEmail',
            'Guardian Email',
            errors
        );

        validateManageParentContacts(context, errors);
        validateManageEmergencyContact(context, errors);
        validateManageMedical(context, errors);
        validateManageProfilePhoto(context, errors);

        if (errors.length > 0) {
            showManageEditValidation(context, errors);
            return false;
        }

        return true;
    }

    function validateManageEmail(
        context,
        selector,
        label,
        errors
    ) {
        const field = context.editForm.querySelector(selector);

        if (
            field instanceof HTMLInputElement &&
            field.value.trim() &&
            !field.validity.valid
        ) {
            const message = `${label} must be a valid email address.`;
            setManageFieldError(field, message);
            errors.push({ field, message });
        }
    }

    function validateManageParentContacts(context, errors) {
        const contacts = {
            FATHER: {
                name: context.editForm.querySelector(
                    '#edit-studentFatherName'
                ),
                phone: context.editForm.querySelector(
                    '#edit-studentFatherPhone'
                )
            },
            MOTHER: {
                name: context.editForm.querySelector(
                    '#edit-studentMotherName'
                ),
                phone: context.editForm.querySelector(
                    '#edit-studentMotherPhone'
                )
            },
            GUARDIAN: {
                name: context.editForm.querySelector(
                    '#edit-studentGuardianName'
                ),
                phone: context.editForm.querySelector(
                    '#edit-studentGuardianPhone'
                )
            }
        };

        const completeContactExists = Object.values(contacts)
            .some(contact =>
                Boolean(manageElementValue(contact.name)) &&
                Boolean(manageElementValue(contact.phone))
            );

        if (!completeContactExists) {
            const message =
                'Enter a name and phone number for at least one Father, Mother or Guardian.';
            setManageFieldError(contacts.FATHER.name, message);
            setManageFieldError(contacts.FATHER.phone, message);
            errors.push({
                field: contacts.FATHER.name,
                message
            });
        }

        Object.entries(contacts).forEach(([type, contact]) => {
            const hasName = Boolean(
                manageElementValue(contact.name)
            );
            const hasPhone = Boolean(
                manageElementValue(contact.phone)
            );

            if (hasName !== hasPhone) {
                const message =
                    `${enumLabel(type)} name and phone must be entered together.`;

                if (!hasName) {
                    setManageFieldError(contact.name, message);
                }
                if (!hasPhone) {
                    setManageFieldError(contact.phone, message);
                }

                errors.push({
                    field: hasName ? contact.phone : contact.name,
                    message
                });
            }
        });

        const preferred = getManageControlValue(
            context.editForm,
            '#edit-studentPreferredContact'
        );
        const preferredContact = preferred
            ? contacts[preferred]
            : null;

        if (
            preferredContact &&
            (
                !manageElementValue(preferredContact.name) ||
                !manageElementValue(preferredContact.phone)
            )
        ) {
            const field = context.editForm.querySelector(
                '#edit-studentPreferredContact'
            );
            const message =
                'The selected preferred contact must have a name and phone number.';
            setManageFieldError(field, message);
            errors.push({ field, message });
        }

        const guardianFields = [
            '#edit-studentGuardianName',
            '#edit-studentGuardianUin',
            '#edit-studentGuardianRelationship',
            '#edit-studentGuardianPhone',
            '#edit-studentGuardianAltPhone',
            '#edit-studentGuardianEmail',
            '#edit-studentGuardianOccupation'
        ];
        const guardianHasAny = guardianFields.some(selector =>
            Boolean(
                getManageControlValue(
                    context.editForm,
                    selector
                )
            )
        );
        const relationship = context.editForm.querySelector(
            '#edit-studentGuardianRelationship'
        );

        if (
            guardianHasAny &&
            !manageElementValue(relationship)
        ) {
            const message =
                'Guardian Relationship is required when Guardian details are entered.';
            setManageFieldError(relationship, message);
            errors.push({ field: relationship, message });
        }
    }

    function validateManageEmergencyContact(context, errors) {
        const fields = [
            context.editForm.querySelector(
                '#edit-studentEmergencyName'
            ),
            context.editForm.querySelector(
                '#edit-studentEmergencyPhone'
            ),
            context.editForm.querySelector(
                '#edit-studentEmergencyRelationship'
            )
        ];
        const entered = fields.map(field =>
            Boolean(manageElementValue(field))
        );
        const count = entered.filter(Boolean).length;

        if (count > 0 && count < fields.length) {
            const message =
                'Emergency contact name, phone and relationship must be entered together.';
            fields.forEach((field, index) => {
                if (!entered[index]) {
                    setManageFieldError(field, message);
                }
            });
            errors.push({
                field: fields[entered.indexOf(false)],
                message
            });
        }
    }

    function validateManageMedical(context, errors) {
        [
            ['#edit-studentHeightCm', 'Height'],
            ['#edit-studentWeightKg', 'Weight']
        ].forEach(([selector, label]) => {
            const field = context.editForm.querySelector(selector);
            const value = finiteNumber(
                getManageControlValue(
                    context.editForm,
                    selector
                )
            );

            if (value !== null && value <= 0) {
                const message = `${label} must be greater than zero.`;
                setManageFieldError(field, message);
                errors.push({ field, message });
            }
        });
    }

    function validateManageProfilePhoto(context, errors) {
        const field = context.editForm.querySelector(
            '#edit-studentProfilePhoto'
        );
        const photo = field instanceof HTMLInputElement
            ? field.files?.[0] || null
            : null;

        if (!photo) {
            return;
        }

        if (MANAGE_PHOTO_TYPES.indexOf(photo.type) === -1) {
            const message =
                'Student photo must be JPG, PNG or WEBP.';
            setManageFieldError(field, message);
            errors.push({ field, message });
        } else if (photo.size > MAX_MANAGE_PHOTO_BYTES) {
            const message =
                'Student photo must not exceed 2 MB.';
            setManageFieldError(field, message);
            errors.push({ field, message });
        }
    }

    function manageElementValue(element) {
        if (
            element instanceof HTMLInputElement ||
            element instanceof HTMLSelectElement ||
            element instanceof HTMLTextAreaElement
        ) {
            return element.value.trim();
        }

        return '';
    }

    async function requestStudentSave(context) {
        if (!validateStudentEdit(context)) {
            return;
        }

        const accepted = await askStudentConfirmation(
            context,
            {
                title: 'Save Student Changes',
                message:
                    'Save the reviewed Student profile changes?',
                confirmLabel: 'Save Changes',
                confirmClass: 'btn-primary'
            }
        );

        if (!accepted) {
            return;
        }

        await saveStudentProfile(context);
    }

    async function saveStudentProfile(context) {
        const studentId = positiveInteger(
            context.currentStudentId
        );
        const personal = profileSection(
            context.currentProfile,
            'personal'
        );
        const version = finiteNumber(
            read(personal, 'version')
        );
        const saveButton = context.view.querySelector(
            '#student-saveBtn'
        );

        if (studentId === null || version === null) {
            notifyError(
                new Error('Student version is unavailable.'),
                'Reload the Student profile before saving.'
            );
            return;
        }

        const payload = buildStudentUpdatePayload(
            context,
            version
        );

        try {
            const response = await runManageOperation(
                context,
                {
                    button: saveButton,
                    title: 'Saving Student Changes',
                    message:
                        'Please wait while the Student profile is updated.'
                },
                () => requestManagePut(
                    `/students/${studentId}`,
                    payload
                )
            );

            let profile = toRecord(
                unwrapManageResponse(response)
            );
            const photo = getSelectedManagePhoto(context);
            let photoUploadWarning = null;

            if (photo) {
                const updatedPersonal = profileSection(
                    profile,
                    'personal'
                );
                const updatedVersion = finiteNumber(
                    read(updatedPersonal, 'version')
                );

                if (updatedVersion === null) {
                    photoUploadWarning =
                        'Student details were saved, but the photo was not uploaded because the updated version was unavailable.';
                } else {
                    const photoResponse = await runManageOperation(
                        context,
                        {
                            button: saveButton,
                            title: 'Uploading Student Photo',
                            message:
                                'The Student details are saved. Uploading the selected photo.'
                        },
                        () => uploadManageStudentPhoto(
                            studentId,
                            updatedVersion,
                            photo
                        )
                    );
                    const photoPersonal = toRecord(
                        unwrapManageResponse(photoResponse)
                    );

                    if (Object.keys(photoPersonal).length > 0) {
                        profile = {
                            ...profile,
                            personal: photoPersonal
                        };
                    }
                }
            }

            cancelQueuedGlobalSync();

            context.currentProfile = profile;
            renderStudentProfile(context, profile);
            leaveStudentEditMode(context, false);
            await loadStudents(context);

            if (photoUploadWarning) {
                notifyError(
                    new Error(photoUploadWarning),
                    photoUploadWarning
                );
            } else {
                notifySuccess('Student updated successfully.');
            }
        } catch (error) {
            applyManageBackendFieldErrors(context, error);
            notifyError(
                error,
                'Student changes could not be saved.'
            );
        }
    }

    function getSelectedManagePhoto(context) {
        const field = context.editForm.querySelector(
            '#edit-studentProfilePhoto'
        );

        return field instanceof HTMLInputElement
            ? field.files?.[0] || null
            : null;
    }

    function uploadManageStudentPhoto(
        studentId,
        version,
        photo
    ) {
        const formData = new FormData();
        formData.append('photo', photo);

        return requestManageMultipart(
            `/students/${studentId}/photo` +
            `?version=${encodeURIComponent(String(version))}` +
            `&operationId=${encodeURIComponent(
                createManageOperationId('student-photo')
            )}`,
            'PUT',
            formData
        );
    }

    function buildStudentUpdatePayload(context, version) {
        const medical = buildStudentMedicalPayload(context);
        const academicHistory = buildStudentAcademicPayload(context);

        return {
            personal: buildStudentPersonalPayload(context),
            parent: buildStudentParentPayload(context),
            medical: hasManagePayloadData(medical)
                ? medical
                : null,
            academicHistory: hasManagePayloadData(
                academicHistory
            )
                ? academicHistory
                : null,
            version
        };
    }

    function buildStudentPersonalPayload(context) {
        const personal = profileSection(
            context.currentProfile,
            'personal'
        );

        return {
            learnerLin: manageValue(context, 'LearnerLin'),
            admissionYear: finiteNumber(
                manageValue(context, 'AdmissionYear')
            ),
            joiningClassId: positiveIntegerOrNull(
                read(personal, 'joiningClassId')
            ),
            joiningTermId: positiveIntegerOrNull(
                read(personal, 'joiningTermId')
            ),
            firstName: manageValue(context, 'FirstName'),
            middleName: manageValue(context, 'MiddleName'),
            lastName: manageValue(context, 'LastName'),
            gender: manageValue(context, 'Gender'),
            dateOfBirth: manageValue(context, 'Dob'),
            nationality: manageValue(context, 'Nationality'),
            nationalIdPassport: manageValue(
                context,
                'NationalIdPassport'
            ),
            houseNo: manageValue(context, 'HouseNo'),
            street: manageValue(context, 'Street'),
            village: manageValue(context, 'Village'),
            townCity: manageValue(context, 'TownCity'),
            district: manageValue(context, 'District'),
            county: manageValue(context, 'County'),
            subCounty: manageValue(context, 'SubCounty'),
            state: manageValue(context, 'State'),
            country: manageValue(context, 'Country'),
            postalCode: manageValue(context, 'PostalCode')
        };
    }

    function buildStudentParentPayload(context) {
        return {
            fatherName: manageValue(context, 'FatherName'),
            fatherUin: manageValue(context, 'FatherUin'),
            fatherPhone: manageValue(context, 'FatherPhone'),
            fatherAlternatePhone: manageValue(context, 'FatherAltPhone'),
            fatherEmail: manageValue(context, 'FatherEmail'),
            fatherOccupation: manageValue(context, 'FatherOccupation'),
            fatherEmployer: manageValue(context, 'FatherEmployer'),
            fatherDesignation: manageValue(context, 'FatherDesignation'),
            fatherAnnualIncome: finiteNumber(
                manageValue(context, 'FatherIncome')
            ),
            motherName: manageValue(context, 'MotherName'),
            motherUin: manageValue(context, 'MotherUin'),
            motherPhone: manageValue(context, 'MotherPhone'),
            motherAlternatePhone: manageValue(context, 'MotherAltPhone'),
            motherEmail: manageValue(context, 'MotherEmail'),
            motherOccupation: manageValue(context, 'MotherOccupation'),
            motherEmployer: manageValue(context, 'MotherEmployer'),
            motherDesignation: manageValue(context, 'MotherDesignation'),
            motherAnnualIncome: finiteNumber(
                manageValue(context, 'MotherIncome')
            ),
            guardianName: manageValue(context, 'GuardianName'),
            guardianUin: manageValue(context, 'GuardianUin'),
            guardianRelationship: manageValue(context, 'GuardianRelationship'),
            guardianPhone: manageValue(context, 'GuardianPhone'),
            guardianAlternatePhone: manageValue(context, 'GuardianAltPhone'),
            guardianEmail: manageValue(context, 'GuardianEmail'),
            guardianOccupation: manageValue(context, 'GuardianOccupation'),
            preferredContact: manageValue(context, 'PreferredContact'),
            feeResponsibility: manageValue(context, 'FeeResponsibility'),
            parentsLivingTogether: nullableBoolean(
                manageValue(context, 'ParentsLivingTogether')
            ),
            emergencyContactName: manageValue(context, 'EmergencyName'),
            emergencyContactPhone: manageValue(context, 'EmergencyPhone'),
            emergencyContactRelationship: manageValue(context, 'EmergencyRelationship'),
            remarks: manageValue(context, 'ParentRemarks')
        };
    }

    function buildStudentMedicalPayload(context) {
        return {
            bloodGroup: manageValue(context, 'BloodGroup'),
            heightCm: finiteNumber(
                manageValue(context, 'HeightCm')
            ),
            weightKg: finiteNumber(
                manageValue(context, 'WeightKg')
            ),
            allergies: manageValue(context, 'Allergies'),
            chronicConditions: manageValue(context, 'ChronicConditions'),
            ongoingMedication: manageValue(context, 'OngoingMedication'),
            specialNeeds: manageValue(context, 'SpecialNeeds'),
            fitForSports: nullableBoolean(
                manageValue(context, 'FitForSports')
            ),
            emergencyDoctorName: manageValue(context, 'DoctorName'),
            emergencyDoctorMobile: manageValue(context, 'DoctorMobile'),
            preferredHospital: manageValue(context, 'PreferredHospital'),
            remarks: manageValue(context, 'MedicalRemarks')
        };
    }

    function buildStudentAcademicPayload(context) {
        return {
            formerSchoolName: manageValue(context, 'FormerSchoolName'),
            formerSchoolCode: manageValue(context, 'FormerSchoolCode'),
            formerSchoolLin: manageValue(context, 'FormerSchoolLin'),
            formerSchoolAddress: manageValue(context, 'FormerSchoolAddress'),
            schoolType: manageValue(context, 'SchoolType'),
            transferReason: manageValue(context, 'TransferReason'),
            previousAcademicYear: manageValue(context, 'PreviousAcademicYear'),
            previousClass: manageValue(context, 'PreviousClass'),
            previousSection: manageValue(context, 'PreviousSection'),
            previousStream: manageValue(context, 'PreviousStream'),
            pleIndexNumber: manageValue(context, 'PleIndexNumber'),
            pleAggregate: manageValue(context, 'PleAggregate'),
            uceIndexNumber: manageValue(context, 'UceIndexNumber'),
            uceResult: manageValue(context, 'UceResult'),
            uaceIndexNumber: manageValue(context, 'UaceIndexNumber'),
            uaceResult: manageValue(context, 'UaceResult'),
            subjectMarks: manageValue(context, 'SubjectMarks'),
            remarks: manageValue(context, 'AcademicRemarks')
        };
    }

    function manageValue(context, suffix) {
        return getManageControlValue(
            context.editForm,
            `#edit-student${suffix}`
        );
    }

    function hasManagePayloadData(payload) {
        return Object.values(payload).some(value => {
            if (value === null || value === undefined) {
                return false;
            }

            return typeof value !== 'string' ||
                value.trim() !== '';
        });
    }

    function applyManageBackendFieldErrors(context, error) {
        const responseData = toRecord(
            readPath(error, 'response', 'data') ||
            read(error, 'data')
        );
        const fieldErrors = toRecord(
            read(responseData, 'fieldErrors') ||
            read(responseData, 'errors')
        );
        const selectors = manageBackendFieldSelectors();
        const messages = [];

        Object.entries(fieldErrors).forEach(([key, value]) => {
            const normalized = key
                .replace(/^request\./, '')
                .replace(/\[[0-9]+]/g, '');
            const suffix = Object.keys(selectors).find(candidate =>
                normalized === candidate ||
                normalized.endsWith(candidate)
            );
            const selector = suffix
                ? selectors[suffix]
                : null;
            const field = selector
                ? context.editForm.querySelector(selector)
                : null;
            const message = String(value || 'Invalid value.');

            if (field) {
                setManageFieldError(field, message);
            }
            messages.push({ field, message });
        });

        if (messages.length > 0) {
            showManageEditValidation(context, messages);
        }
    }

    function manageBackendFieldSelectors() {
        return {
            'personal.learnerLin': '#edit-studentLearnerLin',
            'personal.admissionYear': '#edit-studentAdmissionYear',
            'personal.firstName': '#edit-studentFirstName',
            'personal.middleName': '#edit-studentMiddleName',
            'personal.lastName': '#edit-studentLastName',
            'personal.gender': '#edit-studentGender',
            'personal.dateOfBirth': '#edit-studentDob',
            'personal.nationality': '#edit-studentNationality',
            'personal.nationalIdPassport': '#edit-studentNationalIdPassport',
            'personal.houseNo': '#edit-studentHouseNo',
            'personal.street': '#edit-studentStreet',
            'personal.village': '#edit-studentVillage',
            'personal.townCity': '#edit-studentTownCity',
            'personal.district': '#edit-studentDistrict',
            'personal.county': '#edit-studentCounty',
            'personal.subCounty': '#edit-studentSubCounty',
            'personal.state': '#edit-studentState',
            'personal.country': '#edit-studentCountry',
            'personal.postalCode': '#edit-studentPostalCode',
            'parent.fatherName': '#edit-studentFatherName',
            'parent.fatherPhone': '#edit-studentFatherPhone',
            'parent.fatherEmail': '#edit-studentFatherEmail',
            'parent.motherName': '#edit-studentMotherName',
            'parent.motherPhone': '#edit-studentMotherPhone',
            'parent.motherEmail': '#edit-studentMotherEmail',
            'parent.guardianName': '#edit-studentGuardianName',
            'parent.guardianRelationship': '#edit-studentGuardianRelationship',
            'parent.guardianPhone': '#edit-studentGuardianPhone',
            'parent.guardianEmail': '#edit-studentGuardianEmail',
            'parent.preferredContact': '#edit-studentPreferredContact',
            'parent.feeResponsibility': '#edit-studentFeeResponsibility',
            'parent.parentsLivingTogether': '#edit-studentParentsLivingTogether',
            'medical.bloodGroup': '#edit-studentBloodGroup',
            'medical.heightCm': '#edit-studentHeightCm',
            'medical.weightKg': '#edit-studentWeightKg',
            'academicHistory.formerSchoolName': '#edit-studentFormerSchoolName',
            'academicHistory.schoolType': '#edit-studentSchoolType',
            'academicHistory.previousClass': '#edit-studentPreviousClass',
            version: '#student-edit-form'
        };
    }

    function populateEnrollmentSections(context) {
        const yearId = getManageControlValue(
            context.view,
            '#student-enrollment-academicYear'
        );
        const classId = getManageControlValue(
            context.view,
            '#student-enrollment-class'
        );
        const select = context.view.querySelector(
            '#student-enrollment-section'
        );
        const options = sectionOptions(
            context,
            yearId,
            classId
        );
        const previousValue = select instanceof HTMLSelectElement
            ? select.value
            : '';

        replaceSelectOptions(
            select,
            '-- No Section --',
            options,
            previousValue
        );

        if (select instanceof HTMLSelectElement) {
            select.disabled = options.length === 0;
        }
    }

    function openStudentEnrollmentModal(context) {
        if (!context.currentProfile) {
            notifyError(
                new Error('Student profile is not loaded.'),
                'Open a Student profile before updating enrollment.'
            );
            return;
        }

        const personal = profileSection(
            context.currentProfile,
            'personal'
        );
        const enrollment = profileSection(
            context.currentProfile,
            'currentEnrollment'
        );
        const form = context.view.querySelector(
            '#student-enrollment-form'
        );

        if (form instanceof HTMLFormElement) {
            form.reset();
        }

        setManageText(
            context.view,
            '#student-enrollment-name',
            resolveStudentFullName(personal)
        );
        populateStudentEnrollmentReferenceFields(
            context,
            enrollment
        );
        setManageControl(
            context.view,
            '#student-enrollment-rollNo',
            read(enrollment, 'rollNo')
        );
        setManageControl(
            context.view,
            '#student-enrollment-status',
            read(enrollment, 'enrollmentStatus') || 'ACTIVE'
        );
        setManageControl(
            context.view,
            '#student-enrollment-effectiveDate',
            manageTodayIso()
        );
        setManageControl(
            context.view,
            '#student-enrollment-reason',
            ''
        );
        setManageControl(
            context.view,
            '#student-enrollment-remarks',
            ''
        );

        openManageModal(
            context,
            '#student-enrollment-modal'
        );
    }

    async function submitStudentEnrollment(context) {
        const studentId = positiveInteger(
            context.currentStudentId
        );
        const currentEnrollment = profileSection(
            context.currentProfile,
            'currentEnrollment'
        );
        const version = finiteNumber(
            read(currentEnrollment, 'version')
        );
        const academicYearId = positiveInteger(
            getManageControlValue(
                context.view,
                '#student-enrollment-academicYear'
            )
        );
        const classId = positiveInteger(
            getManageControlValue(
                context.view,
                '#student-enrollment-class'
            )
        );
        const sectionId = positiveInteger(
            getManageControlValue(
                context.view,
                '#student-enrollment-section'
            )
        );
        const enrollmentStatus = getManageControlValue(
            context.view,
            '#student-enrollment-status'
        );
        const effectiveDate = getManageControlValue(
            context.view,
            '#student-enrollment-effectiveDate'
        );
        const changeReason = getManageControlValue(
            context.view,
            '#student-enrollment-reason'
        );
        const submitButton = context.view.querySelector(
            '#student-enrollment-submit'
        );

        if (
            studentId === null ||
            version === null ||
            academicYearId === null ||
            classId === null ||
            !enrollmentStatus ||
            !effectiveDate ||
            !changeReason
        ) {
            notifyError(
                new Error(
                    'Academic Year, Class, Status, Effective Date and Change Reason are required.'
                ),
                'Complete all required enrollment fields.'
            );
            return;
        }

        if (effectiveDate > manageTodayIso()) {
            notifyError(
                new Error(
                    'Enrollment Effective Date cannot be in the future.'
                ),
                'Enrollment Effective Date cannot be in the future.'
            );
            return;
        }

        const accepted = await askStudentConfirmation(
            context,
            {
                title: 'Update Student Enrollment',
                message:
                    'Save this academic placement change and preserve the current placement in enrollment history?',
                confirmLabel: 'Update Enrollment',
                confirmClass: 'btn-primary'
            }
        );

        if (!accepted) {
            return;
        }

        const payload = {
            academicYearId,
            classId,
            sectionId,
            rollNo: getManageControlValue(
                context.view,
                '#student-enrollment-rollNo'
            ),
            promotionType: inferStudentPromotionType(
                currentEnrollment,
                academicYearId,
                classId,
                sectionId
            ),
            enrollmentStatus,
            effectiveDate,
            leavingDate: null,
            changeReason,
            remarks: getManageControlValue(
                context.view,
                '#student-enrollment-remarks'
            ),
            version,
            operationId: createManageOperationId(
                'student-enrollment'
            )
        };

        try {
            await runManageOperation(
                context,
                {
                    button: submitButton,
                    title: 'Updating Enrollment',
                    message:
                        'Please wait while the Student enrollment is updated.'
                },
                () => requestManagePut(
                    `/students/${studentId}/enrollment`,
                    payload
                )
            );

            cancelQueuedGlobalSync();
            closeManageModal(
                context,
                '#student-enrollment-modal'
            );
            await refreshCurrentStudentProfile(context);
            await loadStudents(context);
            notifySuccess('Student enrollment updated successfully.');
        } catch (error) {
            notifyError(
                error,
                'Student enrollment could not be updated.'
            );
        }
    }

    function inferStudentPromotionType(
        currentEnrollment,
        academicYearId,
        classId,
        sectionId
    ) {
        const oldYearId = positiveInteger(
            read(currentEnrollment, 'academicYearId')
        );
        const oldClassId = positiveInteger(
            read(currentEnrollment, 'classId')
        );
        const oldSectionId = positiveInteger(
            read(currentEnrollment, 'sectionId')
        );

        if (
            oldYearId === academicYearId &&
            oldClassId === classId &&
            oldSectionId === sectionId
        ) {
            return 'RETAINED';
        }

        if (oldYearId === academicYearId) {
            return 'TRANSFERRED';
        }

        return 'PROMOTED';
    }

    function openStudentStatusModal(context) {
        if (!context.currentProfile) {
            notifyError(
                new Error('Student profile is not loaded.'),
                'Open a Student profile before changing status.'
            );
            return;
        }

        const personal = profileSection(
            context.currentProfile,
            'personal'
        );
        const form = context.view.querySelector(
            '#student-status-form'
        );

        if (form instanceof HTMLFormElement) {
            form.reset();
        }

        setManageText(
            context.view,
            '#student-status-name',
            resolveStudentFullName(personal)
        );
        setManageControl(
            context.view,
            '#student-status-value',
            read(personal, 'studentStatus')
        );
        setManageControl(
            context.view,
            '#student-status-effectiveDate',
            manageTodayIso()
        );
        setManageControl(
            context.view,
            '#student-status-reason',
            ''
        );

        openManageModal(
            context,
            '#student-status-modal'
        );
    }

    async function submitStudentStatus(context) {
        const studentId = positiveInteger(
            context.currentStudentId
        );
        const personal = profileSection(
            context.currentProfile,
            'personal'
        );
        const version = finiteNumber(
            read(personal, 'version')
        );
        const newStatus = getManageControlValue(
            context.view,
            '#student-status-value'
        );
        const effectiveDate = getManageControlValue(
            context.view,
            '#student-status-effectiveDate'
        );
        const reason = getManageControlValue(
            context.view,
            '#student-status-reason'
        );
        const submitButton = context.view.querySelector(
            '#student-status-submit'
        );

        if (
            studentId === null ||
            version === null ||
            !newStatus ||
            !effectiveDate ||
            !reason
        ) {
            notifyError(
                new Error(
                    'New Status, Effective Date and Reason are required.'
                ),
                'Complete all required status fields.'
            );
            return;
        }

        if (effectiveDate > manageTodayIso()) {
            notifyError(
                new Error(
                    'Status Effective Date cannot be in the future.'
                ),
                'Status Effective Date cannot be in the future.'
            );
            return;
        }

        if (newStatus === read(personal, 'studentStatus')) {
            notifyError(
                new Error(
                    'Select a status different from the current Student status.'
                ),
                'Select a different Student status.'
            );
            return;
        }

        const accepted = await askStudentConfirmation(
            context,
            {
                title: 'Change Student Status',
                message:
                    `Change the Student status to ${enumLabel(newStatus)}?`,
                confirmLabel: 'Update Status',
                confirmClass: 'btn-primary'
            }
        );

        if (!accepted) {
            return;
        }

        try {
            await runManageOperation(
                context,
                {
                    button: submitButton,
                    title: 'Updating Student Status',
                    message:
                        'Please wait while the Student lifecycle status is updated.'
                },
                () => requestManagePut(
                    `/students/${studentId}/status`,
                    {
                        newStatus,
                        effectiveDate,
                        reason,
                        version,
                        operationId: createManageOperationId(
                            'student-status'
                        )
                    }
                )
            );

            cancelQueuedGlobalSync();
            closeManageModal(
                context,
                '#student-status-modal'
            );
            await refreshCurrentStudentProfile(context);
            await loadStudents(context);
            notifySuccess('Student status updated successfully.');
        } catch (error) {
            notifyError(
                error,
                'Student status could not be updated.'
            );
        }
    }

    function openStudentDocumentModal(context) {
        if (!context.currentProfile) {
            notifyError(
                new Error('Student profile is not loaded.'),
                'Open a Student profile before uploading a document.'
            );
            return;
        }

        const personal = profileSection(
            context.currentProfile,
            'personal'
        );
        const form = context.view.querySelector(
            '#student-document-form'
        );

        if (form instanceof HTMLFormElement) {
            form.reset();
        }

        setManageText(
            context.view,
            '#student-document-name',
            resolveStudentFullName(personal)
        );

        openManageModal(
            context,
            '#student-document-modal'
        );
    }

    async function submitStudentDocument(context) {
        const studentId = positiveInteger(
            context.currentStudentId
        );
        const documentType = getManageControlValue(
            context.view,
            '#student-document-type'
        );
        const documentName = getManageControlValue(
            context.view,
            '#student-document-displayName'
        );
        const fileInput = context.view.querySelector(
            '#student-document-file'
        );
        const file = fileInput instanceof HTMLInputElement
            ? fileInput.files?.[0] || null
            : null;
        const submitButton = context.view.querySelector(
            '#student-document-submit'
        );

        if (
            studentId === null ||
            !documentType ||
            !documentName ||
            !file
        ) {
            notifyError(
                new Error(
                    'Document Type, Document Name and File are required.'
                ),
                'Complete all required document fields.'
            );
            return;
        }

        const allowedTypes = [
            'application/pdf',
            'image/jpeg',
            'image/png',
            'image/webp'
        ];

        if (allowedTypes.indexOf(file.type) === -1) {
            notifyError(
                new Error(
                    'Document must be PDF, JPG, PNG or WEBP.'
                ),
                'Document must be PDF, JPG, PNG or WEBP.'
            );
            return;
        }

        if (file.size > 5 * 1024 * 1024) {
            notifyError(
                new Error('Document must not exceed 5 MB.'),
                'Document must not exceed 5 MB.'
            );
            return;
        }

        const metadata = {
            documentType,
            documentName,
            documentNumber: getManageControlValue(
                context.view,
                '#student-document-number'
            ),
            remarks: getManageControlValue(
                context.view,
                '#student-document-remarks'
            )
        };
        const formData = new FormData();
        formData.append(
            'metadata',
            new Blob(
                [JSON.stringify(metadata)],
                { type: 'application/json' }
            )
        );
        formData.append('file', file);

        try {
            await runManageOperation(
                context,
                {
                    button: submitButton,
                    title: 'Uploading Student Document',
                    message:
                        'Please wait while the document is securely uploaded.'
                },
                () => requestManageMultipart(
                    `/students/${studentId}/documents`,
                    'POST',
                    formData
                )
            );

            cancelQueuedGlobalSync();
            closeManageModal(
                context,
                '#student-document-modal'
            );
            await refreshCurrentStudentProfile(context);
            notifySuccess('Student document uploaded successfully.');
        } catch (error) {
            notifyError(
                error,
                'Student document could not be uploaded.'
            );
        }
    }
})();

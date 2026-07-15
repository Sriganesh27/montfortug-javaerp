# Strict Enterprise Student Import Excel Audit

Based on a comprehensive scan of the Montfort Uganda ERP architecture, here is the exact mapping of existing `student` module entities to the proposed Excel Bulk Import Template.

> [!IMPORTANT]
> The Student Module currently contains **Entities only**. DTOs (`StudentCreateRequest`, `StudentImportDTO`, etc.) and Services (`StudentImportService`) **do not exist yet** and must be created. The following design aligns precisely with the JPA definitions found in the `student/entity` package.

## Step 1: Entity Scan Results

| Entity Name | Database Table | Primary Key | Relationships |
| :--- | :--- | :--- | :--- |
| `ErpStudent` | `erp_students` | `student_id` | Branch (M:1), Application (M:1), CurrentEnrollment (1:1), AcademicHistory (1:1), Documents (1:M) |
| `ErpStudentEnrollment` | `erp_student_enrollment` | `enrollment_id` | Student (1:1), Branch (M:1) |
| `ErpParent` | `erp_parents` | `parent_id` | Student (1:1), Branch (M:1) |
| `ErpStudentMedical` | `erp_student_medical` | `medical_id` | Student (1:1), Branch (M:1) |
| `ErpStudentTransport` | `erp_student_transport` | `transport_id` | Student (M:1), Branch (M:1) |
| `ErpStudentAcademicHistory` | `erp_student_academic_history` | `academic_history_id` | Student (1:1), Branch (M:1) |
| `ErpStudentAccount` | `erp_student_accounts` | `account_id` | Student (1:1), Branch (M:1) |

## Step 2 & 3: Enum & Master Data Scan

### Master Data Lookup Sheets Required
To maintain database integrity, schools must use valid codes or exact names from the following Lookup Tables:
1. **Branch** (`Branch.java`) - `schoolCode`, `branchName`
2. **Academic Year** - ID resolution required (`ErpStudentEnrollment.academicYearId`)
3. **Class** - ID resolution required (`ErpStudentEnrollment.classId`)
4. **Section** - ID resolution required (`ErpStudentEnrollment.sectionId`)
5. **Stream** - ID resolution required (`ErpStudentEnrollment.streamId`)
6. **House** - ID resolution required (`ErpStudentEnrollment.houseId`)
7. **Transport Route** - ID resolution required (`ErpStudentTransport.routeId`)

### Validated Enums
- **BloodGroup** (`ErpStudentMedical`): `A_PLUS`, `A_MINUS`, `B_PLUS`, `B_MINUS`, `AB_PLUS`, `AB_MINUS`, `O_PLUS`, `O_MINUS`, `UNKNOWN`
- **PreferredContact** (`ErpParent`): `FATHER`, `MOTHER`, `GUARDIAN`
- **FeeResponsibility** (`ErpParent`): `FATHER`, `MOTHER`, `GUARDIAN`, `SPONSOR`
- **AdmissionType** (`ErpStudentEnrollment`): `NEW`, `TRANSFER`, `READMISSION`
- **SchoolType** (`ErpStudentAcademicHistory`): `GOVERNMENT`, `PRIVATE`, `INTERNATIONAL`, `OTHER`
- **TransportStatus** (`ErpStudentTransport`): `ACTIVE`, `INACTIVE`, `SUSPENDED`, `COMPLETED`, `CANCELLED`

---

## Step 7-10: Excel Template Design

### Section A: Admission Information (Entity: `ErpStudent`, `ErpStudentEnrollment`)
| Excel Header | DB / DTO Field | Req | Validation / Enum | Example Value |
| :--- | :--- | :--- | :--- | :--- |
| **Admission No** | `admissionNo` | Yes | Unique, Max 50 | ADM-2026-001 |
| **Admission Year** | `admissionYear` | Yes | Integer | 2026 |
| **Student Code** | `studentCode` | Yes | Unique, Max 50 | STU-001 |
| **Admission Type** | `admissionType` | Yes | `NEW`, `TRANSFER`, `READMISSION` | NEW |
| **Joining Date** | `joiningDate` | Yes | `LocalDate` (YYYY-MM-DD) | 2026-02-01 |

### Section B: Student Information (Entity: `ErpStudent`)
| Excel Header | DB / DTO Field | Req | Validation / Enum | Example Value |
| :--- | :--- | :--- | :--- | :--- |
| **First Name** | `firstName` | Yes | Max 100 | John |
| **Middle Name** | `middleName` | No | Max 100 | Doe |
| **Last Name** | `lastName` | No | Max 100 | Smith |
| **Gender** | `gender` | No | Max 20 | Male |
| **Date of Birth** | `dateOfBirth` | No | `LocalDate` (YYYY-MM-DD) | 2012-05-14 |
| **Nationality** | `nationality` | No | Max 100 | Ugandan |
| **Learner LIN** | `learnerLin` | No | Max 50 | LIN123456 |

### Section C: Academic Information (Entity: `ErpStudentEnrollment`)
| Excel Header | DB / DTO Field | Req | Validation / Enum | Example Value |
| :--- | :--- | :--- | :--- | :--- |
| **Academic Year Name** | `academicYearId` | Yes | Lookup -> Academic Year | 2026-2027 |
| **Class Name** | `classId` | Yes | Lookup -> Class | Primary 1 |
| **Section Name** | `sectionId` | No | Lookup -> Section | Section A |
| **Stream Name** | `streamId` | No | Lookup -> Stream | Stream Science |
| **House Name** | `houseId` | No | Lookup -> House | Blue House |
| **Roll Number** | `rollNo` | No | Max 20 | 14 |

### Section D: Parent / Guardian (Entity: `ErpParent`)
| Excel Header | DB / DTO Field | Req | Validation / Enum | Example Value |
| :--- | :--- | :--- | :--- | :--- |
| **Father Name** | `fatherName` | No | Max 150 | Robert Smith |
| **Father Phone** | `fatherPhone` | No | Max 30 | +256700000001 |
| **Mother Name** | `motherName` | No | Max 150 | Mary Smith |
| **Mother Phone** | `motherPhone` | No | Max 30 | +256700000002 |
| **Guardian Name** | `guardianName` | No | Max 150 | James Guardian |
| **Guardian Phone** | `guardianPhone` | No | Max 30 | +256700000003 |
| **Preferred Contact** | `preferredContact` | Yes | `FATHER`, `MOTHER`, `GUARDIAN` | FATHER |
| **Fee Responsibility** | `feeResponsibility`| Yes | `FATHER`, `MOTHER`, `GUARDIAN`, `SPONSOR` | FATHER |

### Section E: Address (Entity: `ErpStudent`)
| Excel Header | DB / DTO Field | Req | Validation / Enum | Example Value |
| :--- | :--- | :--- | :--- | :--- |
| **Village** | `village` | No | Max 100 | Kisaasi |
| **Town / City** | `townCity` | No | Max 100 | Kampala |
| **District** | `district` | No | Max 100 | Kampala |
| **Country** | `country` | No | Max 100 | Uganda |

### Section F: Medical (Entity: `ErpStudentMedical`)
| Excel Header | DB / DTO Field | Req | Validation / Enum | Example Value |
| :--- | :--- | :--- | :--- | :--- |
| **Blood Group** | `bloodGroup` | Yes | `A_PLUS`, `O_MINUS`, `UNKNOWN` | O_PLUS |
| **Allergies** | `allergies` | No | Max 500 | Peanuts |
| **Fit for Sports** | `fitForSports` | Yes | Boolean (`true`, `false`) | true |

### Section G: Emergency Contact (Entity: `ErpParent`, `ErpStudentMedical`)
| Excel Header | DB / DTO Field | Req | Validation / Enum | Example Value |
| :--- | :--- | :--- | :--- | :--- |
| **Emerg. Contact Name**| `emergencyContactName` | No | Max 150 | Peter Pan |
| **Emerg. Contact Phone**| `emergencyContactPhone` | No | Max 30 | +256700000005 |
| **Doctor Name** | `emergencyDoctorName` | No | Max 150 (Medical) | Dr. Kasule |
| **Doctor Mobile** | `emergencyDoctorMobile` | No | RegEx `^[0-9+\- ]{7,20}$` | +256700000006 |

### Section H: Transport (Entity: `ErpStudentTransport`)
| Excel Header | DB / DTO Field | Req | Validation / Enum | Example Value |
| :--- | :--- | :--- | :--- | :--- |
| **Transport Required**| N/A | No | Boolean (`true`, `false`) | true |
| **Route Name** | `routeId` | No* | Lookup -> Route (*If Req) | City Centre Route |
| **Transport Start** | `transportStartDate` | No* | `LocalDate` (*If Req) | 2026-02-01 |
| **Monthly Fee** | `monthlyFee` | No* | Decimal Min 0.00 (*If Req)| 50000.00 |

### Section I: Previous School (Entity: `ErpStudentAcademicHistory`)
| Excel Header | DB / DTO Field | Req | Validation / Enum | Example Value |
| :--- | :--- | :--- | :--- | :--- |
| **Former School Name**| `formerSchoolName` | No | Max 255 | Kampala Primary |
| **School Type** | `schoolType` | No | `GOVERNMENT`, `PRIVATE` | PRIVATE |
| **Previous Class** | `previousClass` | No | Max 50 | Primary 7 |
| **PLE Index Number** | `pleIndexNumber` | No | Max 50 | PLE-0092 |

### Section J: Login Account (Entity: `ErpStudentAccount`)
| Excel Header | DB / DTO Field | Req | Validation / Enum | Example Value |
| :--- | :--- | :--- | :--- | :--- |
| **Generate Login** | N/A | No | Boolean (`true`, `false`) | true |
| **Username** | `username` | No* | Max 100 (*If Generated) | ADM-2026-001 |

---

## Architecture & Error Reporting Strategy

1. **Import Service & DTOs**: A robust `StudentImportService.java` must be created. DTOs like `StudentBulkImportDTO` should map directly to the above sections to handle Jackson/Excel serialization.
2. **Lookup Resolution**: The Excel sheet provides human-readable strings (e.g. `Class Name`). The service MUST cache and resolve these into Database IDs (`classId`) before inserting into `ErpStudentEnrollment`.
3. **Transactional Batching**: Use `@Transactional` to ensure that if `ErpStudent` succeeds but `ErpParent` fails validation, the entire student record rolls back. 
4. **Error Reporting (The EmployeeValidator Pattern)**: 
   - Parse all rows.
   - For every invalid cell (e.g. max string size exceeded, unrecognized lookup value), map an error string directly back to the Row Index.
   - Return a `Map<Integer, List<String>>` to the frontend where `Key = RowNumber`, `Value = List of Error Descriptions`.
   - Prevent any inserts if *any* row in the batch contains validation errors.

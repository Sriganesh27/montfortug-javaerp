# 1. Functional Business Requirement Document (BRD)
## Project: Montfort Uganda Multi-School ERP
## Module: Admission Management System

---

## 1. Executive Summary
The Admission Management System serves as the gateway for the Montfort Uganda Multi-School ERP. This Functional BRD defines the complex business processes, user roles, policies, and business rules governing the complete 24-step lifecycle from public application submission to final student enrollment. It ensures rigorous academic testing, transparent financial evaluation (scholarships and fees), and a strict audit trail before a student is granted access to the ERP.

## 2. The Complete Business Workflow
The admission process follows a strict 24-stage pipeline.

1. **Parent Application:** Parent submits via the public portal.
2. **Application Validation:** System-level validation of required fields and duplicate checks.
3. **Document Verification:** Admission Officer manually verifies birth certificates and past reports.
4. **Missing Document Request:** Automated email loop if documents are rejected.
5. **Teacher Assignment:** Registrar/Admission Officer assigns the application to a specific Teacher.
6. **Teacher Test:** Teacher conducts a written entrance exam.
7. **Teacher Recommendation:** Teacher logs marks, remarks, and formally recommends (or rejects) the candidate.
8. **Admission Officer Review:** Officer reviews teacher marks and consolidates the file.
9. **Shortlisted:** Application status formally changed to Shortlisted.
10. **Parent Meeting:** Officer invites parents to explain school policies, transport, hostel, and uniform rules.
11. **Fee Explanation:** Officer provides a breakdown of all expected fees.
12. **Parent Decision:** Parent Accepts, Declines, Requests Time, or Applies for Scholarship.
13. **Scholarship Application:** Parent submits financial aid request.
14. **School Scholarship Review:** School Scholarship Officer reviews and approves from Branch Funds.
15. **Super Admin Scholarship:** If Branch Funds are insufficient, request forwarded to Super Admin.
16. **Fee Collection:** Officer generates the final invoice.
17. **Partial / Full Payment:** Parent pays either the full amount or an agreed partial deposit.
18. **Pending Payment:** Application paused until minimum threshold is met.
19. **Enrollment Form:** Parent/Admin finalizes emergency contacts and transport/hostel routes.
20. **Student Creation:** System executes conversion of Application to Student Profile.
21. **Auto-Generation:** System generates Admission No, Student Code, Roll Number, Class, Section.
22. **Parent Account:** System generates Parent Portal Login.
23. **Student Account:** System generates Student Portal Login.
24. **Completed:** Automated welcome emails sent; Admission lifecycle ends.

## 3. User Roles & Responsibilities

| Role | Responsibilities |
| :--- | :--- |
| **Parent** | Submits application, provides missing docs, attends meetings, pays fees. |
| **Admission Officer** | Document verification, teacher assignment, conducts parent meeting (rules, hostel, transport, fees), shortlists candidates. |
| **Teacher** | Views applicant profile/docs, conducts written/oral tests, enters subject marks, calculates totals, provides internal remarks and recommendation. |
| **Scholarship Officer** | Reviews local scholarship requests, approves branch funds, forwards to Super Admin if needed. |
| **Principal** | Final authority on admissions and school-level scholarships. |
| **Fee Officer** | Collects full/partial payments, sends email receipts, monitors pending balances, cancels admissions if dues expire. |
| **Registrar** | Oversees enrollment execution, student creation, and auto-generation of IDs/Logins. |
| **Super Admin** | Allocates central scholarship funds when branch demands exceed capacity. |

## 4. Business Rules & Policies

### 4.1. Document Verification Rules
- Verification cannot be bypassed.
- If rejected, a "Missing Document" email must trigger immediately.
- Application cannot proceed to Teacher Assignment until status is `VERIFIED`.

### 4.2. Teacher Assessment Rules
- Teacher must enter individual marks for Written and Oral tests.
- System automatically calculates the `Total Score`.
- Teacher must select a Recommendation Status (`RECOMMENDED`, `WAITLISTED`, `NOT_RECOMMENDED`).
- Teacher internal remarks are completely hidden from the Parent Portal.

### 4.3. Parent Meeting Rules
- Parent must formally accept the School Rules, Uniform Policy, and Fee Structure.
- If Parent selects "Apply Scholarship," the workflow pauses and reroutes to the Scholarship Pipeline.

### 4.4. Scholarship Rules
- **School Fund Limit:** If the requested scholarship is within the branch's allocated budget, the Principal/School Officer approves it directly.
- **Super Admin Escalation:** If the branch fund is exhausted, the application MUST be forwarded to the Super Admin for cross-branch allocation.
- Scholarship adjustments must automatically recalculate the pending Fee Invoice.

### 4.5. Fee & Cancellation Rules
- Enrollment cannot occur if payment is less than the Minimum Required Deposit (Partial Payment rules).
- If the due date passes without payment, the application is auto-flagged for `CANCELLATION`.
- System must generate an automated PDF Receipt upon any payment.

### 4.6. Duplicate & Re-admission Rules
- System blocks application if Applicant Name + DOB + Parent Phone exactly matches an active `erp_students` record.

## 5. Reporting Requirements
The system must generate the following business reports:
1. **Daily & Monthly Admissions:** Volume of incoming applications.
2. **Conversion Rate:** Percentage of Submitted vs Enrolled.
3. **Rejection Analytics:** Breakdown of why applicants were rejected (Failed Test, Missing Docs, Declined Fees).
4. **Scholarship Statistics:** Total funds requested vs allocated.
5. **Teacher Performance:** Time taken to assess; pass/fail rates per teacher.
6. **Fee Collection:** Total admission revenue, partial balances pending.
7. **Pending Parents:** List of parents who requested "More Time" during the meeting.
8. **Missing Documents Report:** Aging report for stalled applications.

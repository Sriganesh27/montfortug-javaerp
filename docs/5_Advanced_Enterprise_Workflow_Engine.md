# 5. Advanced Enterprise Workflow Engine
## Project: Montfort Uganda Multi-School ERP
## Module: Admission Management System
**Date:** 2026-07-14

---

## 1. Decoupled Parallel State Machine
To handle long-running enterprise processes, the Admission Engine abandons a single `status` column in favor of independent, parallel state tracking.

| Status Category | Valid States |
| :--- | :--- |
| **Application Status** | `DRAFT`, `SUBMITTED`, `UNDER_REVIEW`, `CLOSED` |
| **Document Status** | `PENDING`, `VERIFIED`, `MISSING`, `REJECTED` |
| **Assessment Status** | `NOT_ASSIGNED`, `ASSIGNED`, `IN_PROGRESS`, `COMPLETED` |
| **Recommendation Status**| `PENDING`, `RECOMMENDED`, `WAITLIST`, `NOT_RECOMMENDED` |
| **Meeting Status** | `NOT_SCHEDULED`, `SCHEDULED`, `COMPLETED` |
| **Scholarship Status** | `NOT_APPLIED`, `PENDING`, `APPROVED`, `REJECTED` |
| **Fee Status** | `UNPAID`, `PARTIAL`, `PAID` |
| **Enrollment Status** | `NOT_STARTED`, `READY`, `COMPLETED` |

## 2. Dynamic Workflow Configuration
Transitions are no longer hardcoded in Java. The engine relies on a new `erp_workflow_transitions` table.

```sql
CREATE TABLE erp_workflow_transitions (
    transition_id BIGINT PRIMARY KEY,
    module VARCHAR(50), -- e.g., 'ADMISSION'
    from_status VARCHAR(50),
    to_status VARCHAR(50),
    allowed_role VARCHAR(50),
    required_permission VARCHAR(100),
    requires_documents BOOLEAN,
    requires_payment BOOLEAN,
    send_email BOOLEAN,
    send_sms BOOLEAN,
    active BOOLEAN
);
```
*Benefits:* Future changes to the admission pipeline can be made entirely via database configuration without code recompilation.

## 3. Simplified Parent Portal View
While internal admins see complex parallel statuses, parents see a simplified, computed string.
- If `Document Status` = `VERIFIED` and `Assessment Status` = `NOT_ASSIGNED` ➜ **"Under Review"**
- If `Assessment Status` = `ASSIGNED` ➜ **"Test Scheduled"**
- If `Recommendation Status` = `RECOMMENDED` ➜ **"Shortlisted"**

## 4. SLA Monitoring & Escalation Rules
Each stage defines a Service Level Agreement (SLA) in days.

| Stage | Expected SLA | Escalation 1 (Warning) | Escalation 2 (Critical) |
| :--- | :--- | :--- | :--- |
| **Document Verification** | 2 Days | Admission Officer | Principal |
| **Teacher Assessment** | 3 Days | Assigned Teacher | Branch Admin |
| **Parent Meeting** | 5 Days | Admission Officer | Principal |
| **Scholarship Review** | 7 Days | Scholarship Officer | Super Admin |
| **Fee Collection** | 15 Days| Fee Officer | Principal |

*Implementation:* A `@Scheduled` cron job runs nightly, calculates `(CURRENT_DATE - assigned_date) > SLA`, and dispatches escalation emails.

## 5. Task Ownership Engine
Applications are explicitly owned to enable "My Tasks" dashboards.
New columns in `erp_applications`:
- `current_owner_id` (FK to `erp_users`)
- `assigned_role` (e.g., 'TEACHER')
- `assigned_date` (Timestamp)
- `due_date` (Calculated from SLA)

## 6. Multi-Panel Assessment Capability
The `erp_application_marks` table is expanded to support multiple reviewers per applicant.
- An application can have rows for: `Math Teacher`, `Science Teacher`, `Interview Panel`, `Principal`.
- System computes: `Final Score = SUM(marks) / COUNT(assessments)`.

## 7. Rich Workflow History & Analytics
The `erp_application_status_history` table is upgraded to an Analytics Engine.
```sql
CREATE TABLE erp_application_history (
    history_id BIGINT PRIMARY KEY,
    application_id BIGINT,
    status_category VARCHAR(50), -- e.g., 'DOCUMENT_STATUS'
    old_state VARCHAR(50),
    new_state VARCHAR(50),
    changed_by BIGINT,
    changed_on DATETIME,
    remarks VARCHAR(500),
    duration_in_previous_stage INT -- Minutes/Days spent in old_state
);
```
*Use Case:* Generates BI reports like *"Average document verification time: 1.8 days"*.

## 8. Standardized Dashboard Layout
All 9 dashboards must implement this exact layout:
1. **KPI Cards:** Top row (e.g., Pending, Overdue, Completed).
2. **Advanced Filters & Saved Filters:** Standardize across all views.
3. **Data Table with Row Actions:** Consistent UI.
4. **Detail Drawer (Right-side Slide-out):** Clicking a row opens a drawer instead of a new page.
5. **Standard Tabs in Drawer:** Profile, Timeline, Documents, Comms History, Audit Log.

## 9. Pre-Enrollment Checklist
The `READY_FOR_ENROLLMENT` state can only trigger if the checklist is 100% green.
- [ ] Documents Verified
- [ ] Teacher Assessment Logged
- [ ] Parent Meeting Accepted
- [ ] Scholarship Resolved (if applied)
- [ ] Fees Paid (Full or Valid Partial)
- [ ] Transport Route Assigned (if requested)
- [ ] Medical Form Completed

## 10. Execution Roadmap Updates
Phase 1 is now officially: **The Dynamic Workflow Engine & State Configuration Layer**. We will build the `erp_workflow_transitions` logic and SLA schedulers before building any UI.

# Entity Architecture Rules
When working in this project, all AI agents must strictly adhere to these entity standards:
1. Use `@Data`, but strictly combine it with `@EqualsAndHashCode(exclude = {...})` and `@ToString(exclude = {...})` on any relational mappings to prevent infinite recursive loops.
2. Define explicit centralized lifecycle methods (`@PrePersist private void onCreate()` and `@PreUpdate private void onUpdate()`) to handle default values, timestamps, and active flags predictably.
3. Always define explicit `length` limits on `@Column` to match `@Size` validation.
4. All `@OneToMany` relationships MUST be kept as `LAZY` to prevent N+1 query performance degradation. Use `JOIN FETCH` in the Repository when deep loading is required.
5. Map intersection tables (e.g. `erp_role_permissions`) as physical entities with their own PK and audit fields when auditability is required, instead of relying on hidden `@ManyToMany`.
6. Ensure tables are completely normalized. Do not create God-tables.
7. Explicitly track audit fields (`version`, `created_by`, `created_at`, `updated_by`, `updated_at`, `active`, `status`).

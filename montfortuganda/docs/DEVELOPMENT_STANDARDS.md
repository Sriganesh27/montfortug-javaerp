# Development Standards
*Status: Active*

* **Java Standards:** Java 21, Spring Boot 3.x.
* **Controller Standards:** RESTful endpoints, `@RequestMapping`. Must delegate to Services.
* **Service Standards:** House business logic. `@Transactional` required for writes.
* **Entity Standards:** 
  * Use `@Data`, but strictly combine it with `@EqualsAndHashCode(exclude = {...})` and `@ToString(exclude = {...})` on any relational mappings to prevent infinite recursive loops.
  * Define explicit centralized lifecycle methods (`@PrePersist private void onCreate()` and `@PreUpdate private void onUpdate()`) to handle default values, timestamps, and active flags predictably.
  * Always define explicit `length` limits on `@Column` to match `@Size` validation.
  * All `@OneToMany` relationships MUST be kept as `LAZY` to prevent N+1 query performance degradation. Use `JOIN FETCH` in the Repository when deep loading is required.
  * Map intersection tables (e.g. `erp_role_permissions`) as physical entities with their own PK and audit fields when auditability is required, instead of relying on hidden `@ManyToMany`.
* **Validation Standards:** Use `jakarta.validation` (`@Valid`, `@NotBlank`).
* **Frontend Standards:** Vanilla JS, DOM Content Loaded, no inline HTML handlers.

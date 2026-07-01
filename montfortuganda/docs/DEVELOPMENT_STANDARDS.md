# Development Standards
*Status: Observation*

* **Java Standards:** Java 21, Spring Boot 3.x.
* **Controller Standards:** RESTful endpoints, `@RequestMapping`. Must delegate to Services.
* **Service Standards:** House business logic. `@Transactional` required for writes.
* **Entity Standards:** Use `@Getter`/`@Setter`, NOT `@Data`.
* **Validation Standards:** Use `jakarta.validation` (`@Valid`, `@NotBlank`).
* **Frontend Standards:** Vanilla JS, DOM Content Loaded, no inline HTML handlers.

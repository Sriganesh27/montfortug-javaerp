package com.erp.montfortuganda.model.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Extends AuditableEntity to provide a common Long primary key.
 *
 * NOTE ON SOFT DELETES:
 * To fully activate soft deletes for concrete entities extending this,
 * add these annotations to the concrete class (replacing 'table_name' with the actual table):
 *
 * @SQLDelete(sql = "UPDATE table_name SET deleted = true WHERE id=?")
 * @Where(clause = "deleted=false")
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
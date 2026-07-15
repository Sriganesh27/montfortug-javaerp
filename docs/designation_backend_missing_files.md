# Designation Backend Missing Files

Ah, I see exactly what happened! In our last session, I only gave you the underlying files for `Department`. We built the `DesignationController`, but it's throwing errors because we never actually created the `DesignationDTO`, `DesignationService`, and `DesignationMapper`!

Since your `Designation` entity is a **Global Entity** (it doesn't belong to a specific branch like Department does), I have tailored the DTO and Services perfectly for it. 

Here are all 7 missing files. Create them in your `com.erp.montfortuganda.school` packages:

---

### 1. `DesignationDTO.java`
**Location:** `src/main/java/com/erp/montfortuganda/school/dto/DesignationDTO.java`
```java
package com.erp.montfortuganda.school.dto;

import com.erp.montfortuganda.model.enums.RecordStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DesignationDTO {
    private Long id;

    @NotBlank(message = "Designation Code is required")
    @Size(max = 20, message = "Code must be at most 20 characters")
    private String designationCode;

    @NotBlank(message = "Designation Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String designationName;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    private RecordStatus status = RecordStatus.ACTIVE;
    private Boolean active = true;
    private Long version = 0L;
}
```

---

### 2. `DesignationMapper.java`
**Location:** `src/main/java/com/erp/montfortuganda/school/mapper/DesignationMapper.java`
```java
package com.erp.montfortuganda.school.mapper;

import com.erp.montfortuganda.model.enums.RecordStatus;
import com.erp.montfortuganda.school.dto.DesignationDTO;
import com.erp.montfortuganda.school.entity.Designation;
import org.springframework.stereotype.Component;

@Component
public class DesignationMapper {

    public DesignationDTO toDto(Designation entity) {
        if (entity == null) return null;

        DesignationDTO dto = new DesignationDTO();
        dto.setId(entity.getDesignationId());
        dto.setDesignationCode(entity.getDesignationCode());
        dto.setDesignationName(entity.getDesignationName());
        dto.setDescription(entity.getDescription());
        
        try {
            dto.setStatus(RecordStatus.valueOf(entity.getStatus()));
        } catch (IllegalArgumentException | NullPointerException e) {
            dto.setStatus(RecordStatus.ACTIVE);
        }
        
        dto.setActive(entity.getActive());
        dto.setVersion(entity.getVersion());

        return dto;
    }

    public Designation toEntity(DesignationDTO dto) {
        if (dto == null) return null;

        Designation entity = new Designation();
        entity.setDesignationCode(dto.getDesignationCode());
        entity.setDesignationName(dto.getDesignationName());
        entity.setDescription(dto.getDescription());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus().name() : "ACTIVE");
        entity.setActive(dto.getActive() != null ? dto.getActive() : true);
        
        return entity;
    }

    public void updateEntityFromDto(DesignationDTO dto, Designation entity) {
        if (dto.getDesignationCode() != null) entity.setDesignationCode(dto.getDesignationCode());
        if (dto.getDesignationName() != null) entity.setDesignationName(dto.getDesignationName());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus().name());
        if (dto.getActive() != null) entity.setActive(dto.getActive());
    }
}
```

---

### 3. `DesignationRepository.java`
**Location:** `src/main/java/com/erp/montfortuganda/school/repository/DesignationRepository.java`
```java
package com.erp.montfortuganda.school.repository;

import com.erp.montfortuganda.school.entity.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, Long>, JpaSpecificationExecutor<Designation> {
    Optional<Designation> findByDesignationCode(String code);
    Optional<Designation> findByDesignationName(String name);
}
```

---

### 4. `DesignationSpecification.java`
**Location:** `src/main/java/com/erp/montfortuganda/school/repository/DesignationSpecification.java`
```java
package com.erp.montfortuganda.school.repository;

import com.erp.montfortuganda.model.enums.RecordStatus;
import com.erp.montfortuganda.school.entity.Designation;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class DesignationSpecification {

    public static Specification<Designation> getSearchSpecification(
            String keyword, RecordStatus status, Boolean active) {
        
        return (root, query, cb) -> {
            Specification<Designation> spec = Specification.where((Specification<Designation>) null);

            if (status != null) {
                spec = spec.and((r, q, c) -> c.equal(r.get("status"), status.name()));
            }

            if (active != null) {
                spec = spec.and((r, q, c) -> c.equal(r.get("active"), active));
            }

            if (StringUtils.hasText(keyword)) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                spec = spec.and((r, q, c) -> c.or(
                        c.like(c.lower(r.get("designationName")), likePattern),
                        c.like(c.lower(r.get("designationCode")), likePattern)
                ));
            }

            return spec.toPredicate(root, query, cb);
        };
    }
}
```

---

### 5. `DesignationService.java`
**Location:** `src/main/java/com/erp/montfortuganda/school/service/DesignationService.java`
```java
package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.common.dto.PagedResponse;
import com.erp.montfortuganda.model.enums.RecordStatus;
import com.erp.montfortuganda.school.dto.DesignationDTO;
import org.springframework.data.domain.Pageable;

public interface DesignationService {
    DesignationDTO createDesignation(DesignationDTO dto);
    DesignationDTO updateDesignation(Long id, DesignationDTO dto);
    DesignationDTO getDesignationById(Long id);
    PagedResponse<DesignationDTO> searchDesignations(String keyword, Integer branchId, RecordStatus status, Boolean active, Pageable pageable);
}
```

---

### 6. `DesignationServiceImpl.java`
**Location:** `src/main/java/com/erp/montfortuganda/school/service/DesignationServiceImpl.java`
```java
package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.common.dto.PagedResponse;
import com.erp.montfortuganda.model.enums.RecordStatus;
import com.erp.montfortuganda.school.dto.DesignationDTO;
import com.erp.montfortuganda.school.entity.Designation;
import com.erp.montfortuganda.school.mapper.DesignationMapper;
import com.erp.montfortuganda.school.repository.DesignationRepository;
import com.erp.montfortuganda.school.repository.DesignationSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DesignationServiceImpl implements DesignationService {

    private final DesignationRepository designationRepository;
    private final DesignationMapper designationMapper;

    @Override
    @Transactional
    public DesignationDTO createDesignation(DesignationDTO dto) {
        if (designationRepository.findByDesignationCode(dto.getDesignationCode()).isPresent() ||
            designationRepository.findByDesignationName(dto.getDesignationName()).isPresent()) {
            throw new DataIntegrityViolationException("Designation Code or Name already exists");
        }

        Designation entity = designationMapper.toEntity(dto);
        Designation saved = designationRepository.save(entity);
        return designationMapper.toDto(saved);
    }

    @Override
    @Transactional
    public DesignationDTO updateDesignation(Long id, DesignationDTO dto) {
        Designation entity = designationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Designation not found with id: " + id));

        // Optimistic Locking Check
        if (!entity.getVersion().equals(dto.getVersion())) {
            throw new org.springframework.orm.ObjectOptimisticLockingFailureException(Designation.class, id);
        }

        designationMapper.updateEntityFromDto(dto, entity);
        Designation saved = designationRepository.save(entity);
        return designationMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DesignationDTO getDesignationById(Long id) {
        Designation entity = designationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Designation not found with id: " + id));
        return designationMapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<DesignationDTO> searchDesignations(String keyword, Integer branchId, RecordStatus status, Boolean active, Pageable pageable) {
        Specification<Designation> spec = DesignationSpecification.getSearchSpecification(keyword, status, active);
        Page<DesignationDTO> page = designationRepository.findAll(spec, pageable).map(designationMapper::toDto);
        
        return new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
```

---

### 7. `DesignationDeletionService.java`
**Location:** `src/main/java/com/erp/montfortuganda/school/service/DesignationDeletionService.java`
```java
package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.model.enums.RecordStatus;
import com.erp.montfortuganda.school.entity.Designation;
import com.erp.montfortuganda.school.repository.DesignationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DesignationDeletionService {

    private final DesignationRepository designationRepository;

    @Transactional
    public void softDelete(Long id) {
        Designation entity = designationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Designation not found with id: " + id));

        // 1. Mark as Inactive/Deleted
        entity.setActive(false);
        entity.setStatus(RecordStatus.INACTIVE.name());
        designationRepository.save(entity);

        // TODO: Fire Domain Events
        // eventPublisher.publishEvent(new DesignationDeletedEvent(id));

        // TODO: Handle Cascading (e.g., Unassign Employees from this Designation)
    }
}
```

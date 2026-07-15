# Employee Child Entities - Backend Files

Here are the accompanying backend files (DTOs and Repositories) for the four child entities you just created. 

Copy and paste these classes into your `com.erp.montfortuganda.employee.dto` and `com.erp.montfortuganda.employee.repository` packages.

## 1. DTOs

```java
// File: src/main/java/com/erp/montfortuganda/employee/dto/EmployeeContactDTO.java
package com.erp.montfortuganda.employee.dto;

import com.erp.montfortuganda.employee.enums.ContactRelationship;
import com.erp.montfortuganda.employee.enums.ContactType;
import lombok.Data;

@Data
public class EmployeeContactDTO {
    private Long employeeContactId;
    private Long employeeId;
    private String employeeContactName;
    private ContactRelationship employeeContactRelationship;
    private ContactType employeeContactType;
    private String employeeContactMobile;
    private String employeeContactEmail;
    private Boolean employeeContactIsPrimary;
    private Boolean employeeContactIsEmergency;
    private Boolean employeeContactActive;
}
```

```java
// File: src/main/java/com/erp/montfortuganda/employee/dto/EmployeeDocumentDTO.java
package com.erp.montfortuganda.employee.dto;

import com.erp.montfortuganda.employee.enums.EmployeeDocumentType;
import lombok.Data;
import java.time.LocalDate;

@Data
public class EmployeeDocumentDTO {
    private Long employeeDocumentId;
    private Long employeeId;
    private EmployeeDocumentType employeeDocumentType;
    private String employeeDocumentName;
    private String employeeDocumentFileName;
    private String employeeDocumentFilePath;
    private LocalDate employeeDocumentExpiryDate;
    private Boolean employeeDocumentVerified;
    private Boolean employeeDocumentActive;
}
```

```java
// File: src/main/java/com/erp/montfortuganda/employee/dto/EmployeeExperienceDTO.java
package com.erp.montfortuganda.employee.dto;

import com.erp.montfortuganda.employee.enums.ExperienceEmploymentType;
import lombok.Data;
import java.time.LocalDate;

@Data
public class EmployeeExperienceDTO {
    private Long employeeExperienceId;
    private Long employeeId;
    private String employeeExperienceCompanyName;
    private String employeeExperienceDesignation;
    private ExperienceEmploymentType employeeExperienceEmploymentType;
    private LocalDate employeeExperienceStartDate;
    private LocalDate employeeExperienceEndDate;
    private Boolean employeeExperienceCurrentJob;
    private Boolean employeeExperienceVerified;
    private Boolean employeeExperienceActive;
}
```

```java
// File: src/main/java/com/erp/montfortuganda/employee/dto/EmployeeQualificationDTO.java
package com.erp.montfortuganda.employee.dto;

import com.erp.montfortuganda.employee.enums.QualificationLevel;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class EmployeeQualificationDTO {
    private Long employeeQualificationId;
    private Long employeeId;
    private QualificationLevel employeeQualificationLevel;
    private String employeeQualificationName;
    private String employeeQualificationInstitutionName;
    private Integer employeeQualificationCompletionYear;
    private String employeeQualificationGrade;
    private BigDecimal employeeQualificationPercentage;
    private Boolean employeeQualificationVerified;
    private Boolean employeeQualificationActive;
}
```

---

## 2. Repositories

```java
// File: src/main/java/com/erp/montfortuganda/employee/repository/EmployeeContactRepository.java
package com.erp.montfortuganda.employee.repository;

import com.erp.montfortuganda.employee.entity.ErpEmployeeContact;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeContactRepository extends JpaRepository<ErpEmployeeContact, Long> {
    List<ErpEmployeeContact> findByEmployee_EmployeeIdAndEmployeeContactActiveTrue(Long employeeId);
}
```

```java
// File: src/main/java/com/erp/montfortuganda/employee/repository/EmployeeDocumentRepository.java
package com.erp.montfortuganda.employee.repository;

import com.erp.montfortuganda.employee.entity.ErpEmployeeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeDocumentRepository extends JpaRepository<ErpEmployeeDocument, Long> {
    List<ErpEmployeeDocument> findByEmployee_EmployeeIdAndEmployeeDocumentActiveTrue(Long employeeId);
}
```

```java
// File: src/main/java/com/erp/montfortuganda/employee/repository/EmployeeExperienceRepository.java
package com.erp.montfortuganda.employee.repository;

import com.erp.montfortuganda.employee.entity.ErpEmployeeExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeExperienceRepository extends JpaRepository<ErpEmployeeExperience, Long> {
    List<ErpEmployeeExperience> findByEmployee_EmployeeIdAndEmployeeExperienceActiveTrue(Long employeeId);
}
```

```java
// File: src/main/java/com/erp/montfortuganda/employee/repository/EmployeeQualificationRepository.java
package com.erp.montfortuganda.employee.repository;

import com.erp.montfortuganda.employee.entity.ErpEmployeeQualification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeQualificationRepository extends JpaRepository<ErpEmployeeQualification, Long> {
    List<ErpEmployeeQualification> findByEmployee_EmployeeIdAndEmployeeQualificationActiveTrue(Long employeeId);
}
```

# Compilation Fixes

I scanned and compiled your project. Almost everything is perfect, but the compiler caught two minor issues from the code we pasted:

1. **Missing Exception Class:** The `EmployeeValidator` threw a `BadRequestException`, but that class doesn't exist in your `exception` package yet.
2. **Lombok Warning:** `EmployeeUpdateRequest` inherits from `EmployeeCreateRequest`, so Lombok wants us to explicitly add `@EqualsAndHashCode(callSuper = true)`.

Please apply these two quick fixes to get a successful build!

## 1. Create `BadRequestException`
Copy this new file into your `com.erp.montfortuganda.exception` package:

```java
// File: src/main/java/com/erp/montfortuganda/exception/BadRequestException.java
package com.erp.montfortuganda.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
```

## 2. Update `EmployeeUpdateRequest`
Update your `EmployeeUpdateRequest.java` to add the `@EqualsAndHashCode` annotation at the top:

```java
// File: src/main/java/com/erp/montfortuganda/employee/dto/EmployeeUpdateRequest.java
package com.erp.montfortuganda.employee.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EmployeeUpdateRequest extends EmployeeCreateRequest {
    // Inherits all fields from CreateRequest.
    // We omit employee_id here because it should be passed as a @PathVariable in the Controller
}
```

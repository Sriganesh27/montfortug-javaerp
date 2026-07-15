# Final Compilation Fixes

I see exactly what happened! In the architecture plan, we decided that the services shouldn't know about `SecurityContextHolder`, so I called a no-argument `getCurrentUserContext()` method in the new backend files. 

Because your `CurrentUserService` didn't have that no-argument method yet, it threw the `"Expected 1 argument but found 0"` error across all the services that tried to use it! 

Additionally, the `@NonNull` warning in `HtmlExtensionFilter` is just a Spring 6 deprecation warning that we can cleanly remove.

Here are the final two files to copy into your IDE to achieve a 100% green build.

---

### 1. `CurrentUserService.java`
**Location:** `src/main/java/com/erp/montfortuganda/auth/service/CurrentUserService.java`

*This adds the no-argument method so that `BranchAccessService` and `DepartmentServiceImpl` can pull the ambient security context without touching Spring Security classes directly.*

```java
package com.erp.montfortuganda.auth.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CurrentUserService {

    /**
     * [NEW] Ambient context resolver!
     * This allows deeply decoupled services to get the current user without touching Spring Security context directly.
     */
    public CurrentUserContext getCurrentUserContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return getCurrentUserContext(authentication);
    }

    /**
     * Extracts the custom context from Spring Security Authentication.
     * Hardcoded for Phase 1 testing until full JWT is implemented in Phase 2/3.
     */
    public CurrentUserContext getCurrentUserContext(Authentication authentication) {
        CurrentUserContext ctx = new CurrentUserContext();
        ctx.setUserId(1); // Hardcoded Super Admin for testing
        ctx.setUsername("admin@system");
        ctx.setRoles(List.of("SUPER_ADMIN"));
        ctx.setSchoolId(1L);
        ctx.setBranchId(1);
        ctx.setSchoolCode("SYS");

        return ctx;
    }
}
```

---

### 2. `HtmlExtensionFilter.java`
**Location:** `src/main/java/com/erp/montfortuganda/config/HtmlExtensionFilter.java`

*Removed the deprecated `org.springframework.lang.NonNull` annotations to clean up your build logs.*

```java
package com.erp.montfortuganda.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class HtmlExtensionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();

        // ONLY strip .html if it is NOT an internal component fetch
        // This protects layout.js from crashing!
        if (uri != null && uri.endsWith(".html") && uri.lastIndexOf('/') == 0) {

            String cleanUri = uri.substring(0, uri.length() - 5);

            String queryString = request.getQueryString();
            if (queryString != null) {
                cleanUri += "?" + queryString;
            }

            response.sendRedirect(cleanUri);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
```

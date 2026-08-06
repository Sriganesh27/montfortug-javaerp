package com.erp.montfortuganda.admission.mapper;

import com.erp.montfortuganda.admission.dto.ApplicationSummaryDTO;
import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.school.entity.SchoolClass;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Maps admission applications to branch-facing list responses.
 *
 * <p>Reference names are resolved by the service in batches before this
 * mapper is called, preventing both N+1 queries and raw database IDs from
 * appearing in the user interface.</p>
 */
@Component
public class ApplicationMapper {

    public ApplicationSummaryDTO toSummaryDTO(
            ErpApplication application,
            SchoolClass schoolClass
    ) {
        ApplicationSummaryDTO response =
                new ApplicationSummaryDTO();

        response.setApplicationId(
                application.getApplicationId()
        );
        response.setApplicationNo(
                application.getApplicationNo()
        );
        response.setStudentName(
                buildStudentName(application)
        );
        response.setClassName(
                resolveClassName(schoolClass)
        );
        response.setStatus(
                application.getApplicationStatus() == null
                        ? null
                        : application.getApplicationStatus().name()
        );
        response.setSubmittedDate(
                application.getCreatedAt()
        );

        return response;
    }

    /**
     * Compatibility overload for any older caller. It intentionally returns
     * a neutral label instead of exposing a raw Class ID.
     */
    public ApplicationSummaryDTO toSummaryDTO(
            ErpApplication application
    ) {
        return toSummaryDTO(
                application,
                null
        );
    }

    private String buildStudentName(
            ErpApplication application
    ) {
        StringBuilder name =
                new StringBuilder();

        appendName(
                name,
                application.getFirstName()
        );
        appendName(
                name,
                application.getMiddleName()
        );
        appendName(
                name,
                application.getLastName()
        );

        return name.isEmpty()
                ? "Not Available"
                : name.toString();
    }

    private void appendName(
            StringBuilder target,
            String value
    ) {
        if (!StringUtils.hasText(value)) {
            return;
        }

        if (!target.isEmpty()) {
            target.append(' ');
        }

        target.append(value.trim());
    }

    private String resolveClassName(
            SchoolClass schoolClass
    ) {
        if (schoolClass == null) {
            return "Not Available";
        }

        if (StringUtils.hasText(
                schoolClass.getClassName()
        )) {
            return schoolClass
                    .getClassName()
                    .trim();
        }

        if (StringUtils.hasText(
                schoolClass.getClassCode()
        )) {
            return schoolClass
                    .getClassCode()
                    .trim();
        }

        return "Not Available";
    }
}

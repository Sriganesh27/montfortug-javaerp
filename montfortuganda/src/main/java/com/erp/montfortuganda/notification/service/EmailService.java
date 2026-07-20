package com.erp.montfortuganda.notification.service;

import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.employee.entity.ErpEmployee;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.service.FileStorageService;
import com.erp.montfortuganda.school.service.model.BranchAdminCredentials;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Year;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(EmailService.class);

    private static final DateTimeFormatter EXPIRY_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "dd MMM yyyy, hh:mm a 'UTC'"
            );

    private static final String BRANCH_LOGO_CONTENT_ID =
            "branchLogo";

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final FileStorageService fileStorageService;

    @Value("${app.frontend.url:http://localhost:8080}")
    private String frontendUrl;

    @Value("${spring.mail.username:no-reply@montfortuganda.com}")
    private String fromEmail;

    public EmailService(
            JavaMailSender mailSender,
            TemplateEngine templateEngine,
            FileStorageService fileStorageService
    ) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.fileStorageService = fileStorageService;
    }

    @Async
    public void sendApplicationReceipt(
            ErpApplication application
    ) {
        if (
                application == null
                        || !hasText(
                        application.getPrimaryEmail()
                )
        ) {
            return;
        }

        try {
            Context context = new Context();

            String schoolName =
                    buildSchoolName(application);

            context.setVariable(
                    "schoolName",
                    schoolName
            );
            context.setVariable(
                    "schoolLogo",
                    "cid:schoolLogoImage"
            );
            context.setVariable(
                    "studentName",
                    buildFullName(
                            application.getFirstName(),
                            application.getLastName()
                    )
            );
            context.setVariable(
                    "applicationNo",
                    application.getApplicationNo()
            );
            context.setVariable(
                    "currentYear",
                    Year.now().getValue()
            );

            String trackingUrl =
                    normalizeBaseUrl(frontendUrl)
                            + "/apply/status?ref="
                            + application.getApplicationNo();

            context.setVariable(
                    "trackingUrl",
                    trackingUrl
            );
            context.setVariable(
                    "frontendUrl",
                    normalizeBaseUrl(frontendUrl)
            );

            String htmlContent =
                    templateEngine.process(
                            "email/application-confirmation",
                            context
                    );

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            "UTF-8"
                    );

            helper.setFrom(
                    fromEmail,
                    schoolName
            );
            helper.setReplyTo(
                    fromEmail,
                    schoolName + " Admissions"
            );
            helper.setTo(
                    application
                            .getPrimaryEmail()
                            .trim()
            );
            helper.setSubject(
                    "Application Received - "
                            + application.getApplicationNo()
            );
            helper.setText(
                    htmlContent,
                    true
            );

            mailSender.send(message);

            LOGGER.info(
                    "Application receipt email sent for application: {}",
                    application.getApplicationNo()
            );

        } catch (Exception exception) {
            LOGGER.error(
                    "Application receipt email failed for application: {}",
                    application.getApplicationNo(),
                    exception
            );
        }
    }

    @Async
    public void sendEmployeeWelcomeEmail(
            ErpEmployee employee,
            String username,
            String plainTextPassword
    ) {
        if (
                employee == null
                        || !hasText(
                        employee.getOfficialEmail()
                )
        ) {
            return;
        }

        try {
            String schoolName =
                    resolveEmployeeSchoolName(employee);

            Context context = new Context();

            context.setVariable(
                    "schoolName",
                    schoolName
            );
            context.setVariable(
                    "employeeName",
                    buildFullName(
                            employee.getFirstName(),
                            employee.getLastName()
                    )
            );
            context.setVariable(
                    "username",
                    username
            );
            context.setVariable(
                    "tempPassword",
                    plainTextPassword
            );
            context.setVariable(
                    "currentYear",
                    Year.now().getValue()
            );
            context.setVariable(
                    "frontendUrl",
                    normalizeBaseUrl(frontendUrl)
            );

            String htmlContent =
                    templateEngine.process(
                            "email/employee-welcome",
                            context
                    );

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            "UTF-8"
                    );

            helper.setFrom(
                    fromEmail,
                    schoolName + " HR"
            );
            helper.setReplyTo(
                    fromEmail,
                    schoolName + " HR"
            );
            helper.setTo(
                    employee
                            .getOfficialEmail()
                            .trim()
            );
            helper.setSubject(
                    "Welcome to "
                            + schoolName
                            + " - Your Account Details"
            );
            helper.setText(
                    htmlContent,
                    true
            );

            mailSender.send(message);

            LOGGER.info(
                    "Employee welcome email sent to: {}",
                    employee.getOfficialEmail()
            );

        } catch (Exception exception) {
            LOGGER.error(
                    "Employee welcome email failed for: {}",
                    employee.getOfficialEmail(),
                    exception
            );
        }
    }

    /**
     * Sends Branch Admin credentials using the existing working SMTP account.
     * This method is intentionally synchronous. The after-commit email listener
     * will call it asynchronously and must know whether sending succeeded or
     * failed so it can update credential_delivery_status.
     */
    public void sendBranchAdminWelcomeEmail(
            Branch branch,
            BranchAdminCredentials credentials
    ) {
        validateBranchAdminEmailRequest(
                branch,
                credentials
        );

        String recipientEmail =
                branch.getBranchEmail().trim();

        String schoolName =
                resolveBranchSenderName(branch);

        String replyTo =
                resolveBranchReplyTo(branch);

        Resource logoResource =
                loadBranchLogo(branch);

        String logoContentType =
                logoResource == null
                        ? null
                        : fileStorageService.detectContentType(
                        branch.getBranchLogoUrl()
                );

        try {
            Context context = new Context();

            context.setVariable(
                    "schoolName",
                    schoolName
            );
            context.setVariable(
                    "branchName",
                    branch.getBranchName()
            );
            context.setVariable(
                    "branchLocation",
                    branch.getBranchLocation()
            );
            context.setVariable(
                    "schoolEmail",
                    branch.getBranchEmail()
            );
            context.setVariable(
                    "username",
                    credentials.getUsername()
            );
            context.setVariable(
                    "tempPassword",
                    credentials.getTemporaryPassword()
            );
            context.setVariable(
                    "expiresAt",
                    credentials
                            .getExpiresAt()
                            .format(EXPIRY_FORMATTER)
            );
            context.setVariable(
                    "loginUrl",
                    normalizeBaseUrl(frontendUrl)
                            + "/mbsg-auth"
            );
            context.setVariable(
                    "currentYear",
                    Year.now().getValue()
            );
            context.setVariable(
                    "hasSchoolLogo",
                    logoResource != null
            );
            context.setVariable(
                    "schoolLogo",
                    "cid:" + BRANCH_LOGO_CONTENT_ID
            );

            String htmlContent =
                    templateEngine.process(
                            "email/branch-admin-welcome",
                            context
                    );

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            "UTF-8"
                    );

            helper.setFrom(
                    fromEmail,
                    schoolName
            );
            helper.setReplyTo(
                    replyTo,
                    schoolName
            );
            helper.setTo(recipientEmail);
            helper.setSubject(
                    "Branch Administrator Account - "
                            + schoolName
            );
            helper.setText(
                    htmlContent,
                    true
            );

            if (logoResource != null) {
                helper.addInline(
                        BRANCH_LOGO_CONTENT_ID,
                        logoResource,
                        logoContentType
                );
            }

            mailSender.send(message);

            LOGGER.info(
                    "Branch Admin credentials email sent for branch ID: {}",
                    branch.getBranchId()
            );

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Branch Admin credentials email could not be sent.",
                    exception
            );
        }
    }

    private void validateBranchAdminEmailRequest(
            Branch branch,
            BranchAdminCredentials credentials
    ) {
        if (branch == null) {
            throw new IllegalArgumentException(
                    "Branch is required for the credentials email."
            );
        }

        if (credentials == null) {
            throw new IllegalArgumentException(
                    "Branch Admin credentials are required."
            );
        }

        if (!hasText(branch.getBranchEmail())) {
            throw new IllegalArgumentException(
                    "Branch email is required to send administrator credentials."
            );
        }

        if (
                Boolean.FALSE.equals(
                        branch.getEmailEnabled()
                )
        ) {
            throw new IllegalStateException(
                    "Email is disabled for this branch."
            );
        }
    }

    private Resource loadBranchLogo(
            Branch branch
    ) {
        if (!hasText(branch.getBranchLogoUrl())) {
            return null;
        }

        try {
            return fileStorageService.loadPrivateFile(
                    branch.getBranchLogoUrl()
            );
        } catch (RuntimeException exception) {
            LOGGER.warn(
                    "Branch logo could not be loaded for branch ID: {}",
                    branch.getBranchId()
            );

            return null;
        }
    }

    private String resolveBranchSenderName(
            Branch branch
    ) {
        if (hasText(branch.getEmailFromName())) {
            return branch.getEmailFromName().trim();
        }

        if (hasText(branch.getBranchName())) {
            return branch.getBranchName().trim();
        }

        return "Montfort School";
    }

    private String resolveBranchReplyTo(
            Branch branch
    ) {
        if (hasText(branch.getEmailReplyTo())) {
            return branch.getEmailReplyTo().trim();
        }

        return branch.getBranchEmail().trim();
    }

    private String resolveEmployeeSchoolName(
            ErpEmployee employee
    ) {
        if (
                employee.getBranch() != null
                        && hasText(
                        employee
                                .getBranch()
                                .getBranchName()
                )
        ) {
            return employee
                    .getBranch()
                    .getBranchName()
                    .trim();
        }

        return "Montfort School";
    }

    /**
     * Formats the school name as:
     * branch name, branch location
     */
    private String buildSchoolName(
            ErpApplication application
    ) {
        if (application.getBranch() == null) {
            return "Montfort School";
        }

        String name =
                hasText(
                        application
                                .getBranch()
                                .getBranchName()
                )
                        ? application
                        .getBranch()
                        .getBranchName()
                        .trim()
                        : "Montfort School";

        String location =
                hasText(
                        application
                                .getBranch()
                                .getBranchLocation()
                )
                        ? ", "
                        + application
                        .getBranch()
                        .getBranchLocation()
                        .trim()
                        : "";

        return name + location;
    }

    private String buildFullName(
            String firstName,
            String lastName
    ) {
        String first =
                firstName == null
                        ? ""
                        : firstName.trim();

        String last =
                lastName == null
                        ? ""
                        : lastName.trim();

        return (first + " " + last).trim();
    }

    private String normalizeBaseUrl(
            String url
    ) {
        if (url == null || url.isBlank()) {
            return "http://localhost:8080";
        }

        return url.endsWith("/")
                ? url.substring(
                0,
                url.length() - 1
        )
                : url;
    }

    private boolean hasText(
            String value
    ) {
        return value != null
                && !value.isBlank();
    }
}

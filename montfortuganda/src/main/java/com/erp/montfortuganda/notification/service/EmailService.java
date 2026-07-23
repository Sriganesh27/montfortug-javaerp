package com.erp.montfortuganda.notification.service;

import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.employee.entity.ErpEmployee;
import com.erp.montfortuganda.notification.config.BranchMailSenderFactory;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.service.FileStorageService;
import com.erp.montfortuganda.school.service.model.BranchAdminCredentials;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);
    private static final String SCHOOL_LOGO_CONTENT_ID = "schoolLogoImage";
    private static final String DEFAULT_LOGO_PATH = "static/assets/Images/logo_MBSG_UG_8.webp";
    private static final String DEFAULT_LOGO_CONTENT_TYPE = "image/webp";
    private static final DateTimeFormatter EXPIRY_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a 'UTC'");

    private final JavaMailSender centralMailSender;
    private final BranchMailSenderFactory branchMailSenderFactory;
    private final TemplateEngine templateEngine;
    private final FileStorageService fileStorageService;

    @Value("${spring.mail.username}")
    private String centralMailUsername;

    @Value("${erp.mail.from-name:Montfort ERP}")
    private String centralMailFromName;

    @Value("${app.frontend.url:http://localhost:8081}")
    private String frontendUrl;

    public EmailService(
            JavaMailSender centralMailSender,
            BranchMailSenderFactory branchMailSenderFactory,
            TemplateEngine templateEngine,
            FileStorageService fileStorageService
    ) {
        this.centralMailSender = centralMailSender;
        this.branchMailSenderFactory = branchMailSenderFactory;
        this.templateEngine = templateEngine;
        this.fileStorageService = fileStorageService;
    }

    @Async
    public void sendApplicationReceipt(ErpApplication application) {
        if (application == null || !hasText(application.getPrimaryEmail())) {
            return;
        }

        String applicationNumber = application.getApplicationNo();

        try {
            Branch branch = requireBranch(application.getBranch(), "Application");
            String schoolName = resolveSchoolName(branch);
            EmailLogo emailLogo = resolveBranchLogo(branch);

            Context context = new Context();
            context.setVariable("schoolName", schoolName);
            context.setVariable("schoolLogo", "cid:" + SCHOOL_LOGO_CONTENT_ID);
            context.setVariable("studentName", buildFullName(application.getFirstName(), application.getLastName()));
            context.setVariable("applicationNo", applicationNumber);
            context.setVariable("trackingUrl", buildTrackingUrl(applicationNumber));
            context.setVariable("frontendUrl", normalizeBaseUrl(frontendUrl));
            context.setVariable("currentYear", Year.now().getValue());

            String htmlContent = templateEngine.process("email/application-confirmation", context);

            JavaMailSender branchMailSender = branchMailSenderFactory.getMailSender(branch);
            MimeMessage message = branchMailSender.createMimeMessage();
            MimeMessageHelper helper = createMessageHelper(message);

            configureBranchSender(helper, branch, " Admissions");
            helper.setTo(application.getPrimaryEmail().trim());
            helper.setSubject("Application Received - " + applicationNumber);
            helper.setText(htmlContent, true);
            addInlineLogo(helper, emailLogo);

            branchMailSender.send(message);

            LOGGER.info(
                    "Application receipt email sent from branch {} <{}> for application: {}",
                    branch.getSchoolCode(),
                    branch.getBranchEmail(),
                    applicationNumber
            );
        } catch (Exception exception) {
            LOGGER.error(
                    "Application receipt email failed for application: {}",
                    applicationNumber,
                    exception
            );
        }
    }

    public void sendEmployeeWelcomeEmail(
            ErpEmployee employee,
            String username,
            String plainTextPassword
    ) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee is required for credential delivery.");
        }

        requireText(employee.getOfficialEmail(), "Employee official email");
        requireText(username, "Employee username");
        requireText(plainTextPassword, "Employee temporary password");

        String employeeEmail = employee.getOfficialEmail().trim();

        try {
            Branch branch = requireBranch(employee.getBranch(), "Employee");
            String schoolName = resolveSchoolName(branch);
            EmailLogo emailLogo = resolveBranchLogo(branch);

            Context context = new Context();
            context.setVariable("schoolName", schoolName);
            context.setVariable("schoolLogo", "cid:" + SCHOOL_LOGO_CONTENT_ID);
            context.setVariable("employeeName", resolveEmployeeName(employee));
            context.setVariable("username", username.trim());
            context.setVariable("tempPassword", plainTextPassword);
            context.setVariable("loginUrl", buildLoginUrl());
            context.setVariable("frontendUrl", normalizeBaseUrl(frontendUrl));
            context.setVariable("currentYear", Year.now().getValue());

            String htmlContent = templateEngine.process("email/employee-welcome", context);

            JavaMailSender branchMailSender = branchMailSenderFactory.getMailSender(branch);
            MimeMessage message = branchMailSender.createMimeMessage();
            MimeMessageHelper helper = createMessageHelper(message);

            configureBranchSender(helper, branch, " HR");
            helper.setTo(employeeEmail);
            helper.setSubject("Welcome to " + schoolName + " - Your Account Details");
            helper.setText(htmlContent, true);
            addInlineLogo(helper, emailLogo);

            branchMailSender.send(message);

            LOGGER.info(
                    "Employee welcome email sent from branch {} <{}> to: {}",
                    branch.getSchoolCode(),
                    branch.getBranchEmail(),
                    employeeEmail
            );
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Employee credentials email could not be sent.",
                    exception
            );
        }
    }

    public void sendBranchAdminWelcomeEmail(
            Branch branch,
            BranchAdminCredentials credentials
    ) {
        validateBranchAdminRequest(branch, credentials);

        try {
            String schoolName = resolveSchoolName(branch);
            EmailLogo centralLogo = loadCentralMontfortLogo();

            Context context = new Context();
            context.setVariable("schoolName", schoolName);
            context.setVariable("schoolLogo", "cid:" + SCHOOL_LOGO_CONTENT_ID);
            context.setVariable("branchName", branch.getBranchName());
            context.setVariable("branchLocation", branch.getBranchLocation());
            context.setVariable("schoolEmail", branch.getBranchEmail());
            context.setVariable("username", credentials.getUsername());
            context.setVariable("tempPassword", credentials.getTemporaryPassword());
            context.setVariable("expiresAt", credentials.getExpiresAt().format(EXPIRY_FORMATTER));
            context.setVariable("loginUrl", buildLoginUrl());
            context.setVariable("frontendUrl", normalizeBaseUrl(frontendUrl));
            context.setVariable("currentYear", Year.now().getValue());

            String htmlContent = templateEngine.process("email/branch-admin-welcome", context);

            MimeMessage message = centralMailSender.createMimeMessage();
            MimeMessageHelper helper = createMessageHelper(message);

            configureCentralSender(helper);
            helper.setTo(branch.getBranchEmail().trim());
            helper.setSubject("Branch Administrator Account - " + schoolName);
            helper.setText(htmlContent, true);
            addInlineLogo(helper, centralLogo);

            centralMailSender.send(message);

            LOGGER.info(
                    "Branch Admin credentials email sent from central account <{}> for branch ID: {}",
                    centralMailUsername,
                    branch.getBranchId()
            );
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Branch Admin credentials email could not be sent.",
                    exception
            );
        }
    }

    private MimeMessageHelper createMessageHelper(MimeMessage message) throws Exception {
        return new MimeMessageHelper(message, true, "UTF-8");
    }

    private void configureBranchSender(
            MimeMessageHelper helper,
            Branch branch,
            String defaultSuffix
    ) throws Exception {
        validateBranchEmailConfiguration(branch);

        String branchEmail = branch.getBranchEmail().trim();
        String senderName = resolveSenderName(branch, defaultSuffix);

        helper.setFrom(branchEmail, senderName);
        helper.setReplyTo(resolveReplyTo(branch), senderName);
    }

    private void configureCentralSender(MimeMessageHelper helper) throws Exception {
        requireText(centralMailUsername, "Central ERP sender email");

        String senderEmail = centralMailUsername.trim();
        String senderName = hasText(centralMailFromName)
                ? centralMailFromName.trim()
                : "Montfort ERP";

        helper.setFrom(senderEmail, senderName);
        helper.setReplyTo(senderEmail, senderName);
    }

    private EmailLogo resolveBranchLogo(Branch branch) {
        if (!hasText(branch.getBranchLogoUrl())) {
            return loadCentralMontfortLogo();
        }

        try {
            Resource logoResource = fileStorageService.loadPrivateFile(branch.getBranchLogoUrl());
            String contentType = fileStorageService.detectContentType(branch.getBranchLogoUrl());
            return new EmailLogo(logoResource, contentType);
        } catch (RuntimeException exception) {
            LOGGER.warn(
                    "Branch logo could not be loaded for branch ID: {}. The central Montfort logo will be used.",
                    branch.getBranchId()
            );
            return loadCentralMontfortLogo();
        }
    }

    private EmailLogo loadCentralMontfortLogo() {
        Resource logoResource = new ClassPathResource(DEFAULT_LOGO_PATH);

        if (!logoResource.exists()) {
            throw new IllegalStateException(
                    "The central Montfort email logo was not found: " + DEFAULT_LOGO_PATH
            );
        }

        return new EmailLogo(logoResource, DEFAULT_LOGO_CONTENT_TYPE);
    }

    private void addInlineLogo(MimeMessageHelper helper, EmailLogo emailLogo) throws Exception {
        helper.addInline(
                SCHOOL_LOGO_CONTENT_ID,
                emailLogo.resource(),
                emailLogo.contentType()
        );
    }

    private void validateBranchAdminRequest(
            Branch branch,
            BranchAdminCredentials credentials
    ) {
        if (branch == null) {
            throw new IllegalArgumentException("Branch is required.");
        }

        requireText(branch.getBranchEmail(), "Branch email");

        if (credentials == null) {
            throw new IllegalArgumentException("Branch Admin credentials are required.");
        }

        requireText(credentials.getUsername(), "Branch Admin username");
        requireText(credentials.getTemporaryPassword(), "Branch Admin temporary password");

        if (credentials.getExpiresAt() == null) {
            throw new IllegalArgumentException("Branch Admin credential expiry time is required.");
        }
    }

    private void validateBranchEmailConfiguration(Branch branch) {
        requireBranch(branch, "Email");
        requireText(branch.getSchoolCode(), "Branch school code");
        requireText(branch.getBranchEmail(), "Branch email");

        if (Boolean.FALSE.equals(branch.getEmailEnabled())) {
            throw new IllegalStateException("Email is disabled for branch: " + branch.getSchoolCode());
        }
    }

    private Branch requireBranch(Branch branch, String operationName) {
        if (branch == null) {
            throw new IllegalArgumentException(
                    operationName + " branch is required for email delivery."
            );
        }
        return branch;
    }

    private String resolveSchoolName(Branch branch) {
        if (hasText(branch.getBranchName())) {
            return branch.getBranchName().trim();
        }
        return "Montfort School";
    }

    private String resolveSenderName(Branch branch, String defaultSuffix) {
        if (hasText(branch.getEmailFromName())) {
            return branch.getEmailFromName().trim();
        }
        return resolveSchoolName(branch) + defaultSuffix;
    }

    private String resolveReplyTo(Branch branch) {
        if (hasText(branch.getEmailReplyTo())) {
            return branch.getEmailReplyTo().trim();
        }
        return branch.getBranchEmail().trim();
    }

    private String resolveEmployeeName(ErpEmployee employee) {
        if (hasText(employee.getFullName())) {
            return employee.getFullName().trim();
        }
        return buildFullName(employee.getFirstName(), employee.getLastName());
    }

    private String buildTrackingUrl(String applicationNumber) {
        return normalizeBaseUrl(frontendUrl) + "/apply/status?ref=" + applicationNumber;
    }

    private String buildLoginUrl() {
        return normalizeBaseUrl(frontendUrl) + "/mbsg-auth";
    }

    private String buildFullName(String firstName, String lastName) {
        String first = firstName == null ? "" : firstName.trim();
        String last = lastName == null ? "" : lastName.trim();
        return (first + " " + last).trim();
    }

    private String normalizeBaseUrl(String url) {
        if (url == null || url.isBlank()) {
            return "http://localhost:8081";
        }

        String normalizedUrl = url.trim();
        while (normalizedUrl.endsWith("/")) {
            normalizedUrl = normalizedUrl.substring(0, normalizedUrl.length() - 1);
        }
        return normalizedUrl;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private void requireText(String value, String fieldName) {
        if (!hasText(value)) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
    }

    private record EmailLogo(Resource resource, String contentType) {
    }
}
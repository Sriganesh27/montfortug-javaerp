package com.erp.montfortuganda.notification.service;

import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.admission.entity.ErpApplicationDocumentRequest;
import com.erp.montfortuganda.admission.entity.ErpApplicationStatusHistory;
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

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class EmailService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(EmailService.class);

    private static final String SCHOOL_LOGO_CONTENT_ID =
            "schoolLogoImage";

    private static final String DEFAULT_LOGO_PATH =
            "static/assets/Images/logo_MBSG_UG_8.webp";

    private static final String DEFAULT_LOGO_CONTENT_TYPE =
            "image/webp";

    private static final DateTimeFormatter EXPIRY_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "dd MMM yyyy, hh:mm a 'UTC'"
            );

    private static final DateTimeFormatter
            DOCUMENT_DEADLINE_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "dd MMM yyyy, hh:mm a"
            );

    private final JavaMailSender centralMailSender;
    private final BranchMailSenderFactory branchMailSenderFactory;
    private final TemplateEngine templateEngine;
    private final FileStorageService fileStorageService;

    @Value("${spring.mail.username}")
    private String centralMailUsername;

    @Value("${erp.mail.from-name:Montfort ERP}")
    private String centralMailFromName;

    /*
     * These values come from profile-specific property files.
     *
     * Production:
     * app.base-url=${APP_BASE_URL}
     * app.login-url=${APP_LOGIN_URL}
     *
     * Development and test profiles can use localhost.
     */
    @Value("${app.base-url}")
    private String appBaseUrl;

    @Value("${app.login-url}")
    private String appLoginUrl;

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
    public void sendApplicationReceipt(
            ErpApplication application
    ) {
        if (application == null
                || !hasText(application.getPrimaryEmail())) {
            return;
        }

        String applicationNumber = application.getApplicationNo();

        try {
            Branch branch = requireBranch(
                    application.getBranch(),
                    "Application"
            );

            String schoolName = resolveSchoolName(branch);
            EmailLogo emailLogo = resolveBranchLogo(branch);

            Context context = new Context();

            context.setVariable(
                    "schoolName",
                    schoolName
            );

            context.setVariable(
                    "schoolLogo",
                    "cid:" + SCHOOL_LOGO_CONTENT_ID
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
                    applicationNumber
            );

            context.setVariable(
                    "trackingUrl",
                    buildTrackingUrl(applicationNumber)
            );

            context.setVariable(
                    "currentYear",
                    Year.now().getValue()
            );

            String htmlContent = templateEngine.process(
                    "email/application-confirmation",
                    context
            );

            JavaMailSender branchMailSender =
                    branchMailSenderFactory.getMailSender(branch);

            MimeMessage message =
                    branchMailSender.createMimeMessage();

            MimeMessageHelper helper =
                    createMessageHelper(message);

            configureBranchSender(
                    helper,
                    branch,
                    " Admissions"
            );

            helper.setTo(
                    application.getPrimaryEmail().trim()
            );

            helper.setSubject(
                    "Application Received - "
                            + applicationNumber
            );

            helper.setText(htmlContent, true);

            addInlineLogo(helper, emailLogo);

            branchMailSender.send(message);

            LOGGER.info(
                    "Application receipt email sent from branch {} "
                            + "<{}> for application: {}",
                    branch.getSchoolCode(),
                    branch.getBranchEmail(),
                    applicationNumber
            );
        } catch (Exception exception) {
            LOGGER.error(
                    "Application receipt email failed "
                            + "for application: {}",
                    applicationNumber,
                    exception
            );
        }
    }

    /**
     * Sends a branch-branded, time-limited upload link for one requested
     * admission document.
     *
     * <p>This method deliberately does not catch delivery failures. The
     * after-commit listener that invokes it must update the request and
     * admission-history email statuses to SENT or FAILED.</p>
     */
    public void sendAdditionalDocumentRequest(
            ErpApplication application,
            ErpApplicationDocumentRequest documentRequest,
            String rawUploadToken
    ) {
        validateAdditionalDocumentEmailRequest(
                application,
                documentRequest,
                rawUploadToken
        );

        String applicationNumber =
                application.getApplicationNo().trim();

        String recipientEmail =
                application.getPrimaryEmail().trim();

        try {
            Branch branch = requireBranch(
                    application.getBranch(),
                    "Application"
            );

            String schoolName = resolveSchoolName(branch);
            EmailLogo emailLogo = resolveBranchLogo(branch);

            Context context = new Context();

            context.setVariable(
                    "schoolName",
                    schoolName
            );

            context.setVariable(
                    "schoolLogo",
                    "cid:" + SCHOOL_LOGO_CONTENT_ID
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
                    applicationNumber
            );

            context.setVariable(
                    "requestedDocumentName",
                    documentRequest
                            .getRequestedDocumentName()
                            .trim()
            );

            context.setVariable(
                    "requestReason",
                    documentRequest
                            .getRequestReason()
                            .trim()
            );

            context.setVariable(
                    "publicRemarks",
                    trimToNull(
                            documentRequest.getPublicRemarks()
                    )
            );

            context.setVariable(
                    "uploadDeadlineText",
                    formatDocumentUploadDeadline(
                            documentRequest
                                    .getUploadTokenExpiresAt()
                    )
            );

            context.setVariable(
                    "uploadUrl",
                    buildAdditionalDocumentUploadUrl(
                            rawUploadToken
                    )
            );

            context.setVariable(
                    "currentYear",
                    Year.now().getValue()
            );

            String htmlContent = templateEngine.process(
                    "email/application-additional-document-request",
                    context
            );

            JavaMailSender branchMailSender =
                    branchMailSenderFactory.getMailSender(branch);

            MimeMessage message =
                    branchMailSender.createMimeMessage();

            MimeMessageHelper helper =
                    createMessageHelper(message);

            configureBranchSender(
                    helper,
                    branch,
                    " Admissions"
            );

            helper.setTo(recipientEmail);

            helper.setSubject(
                    "Additional Document Required - "
                            + applicationNumber
            );

            helper.setText(htmlContent, true);

            addInlineLogo(helper, emailLogo);

            branchMailSender.send(message);

            LOGGER.info(
                    "Additional-document request email sent "
                            + "from branch {} <{}> for application {} "
                            + "and request {}",
                    branch.getSchoolCode(),
                    branch.getBranchEmail(),
                    applicationNumber,
                    documentRequest.getRequestId()
            );
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Additional-document request email "
                            + "could not be sent for application "
                            + applicationNumber
                            + ".",
                    exception
            );
        }
    }

    /**
     * Sends one branch-branded admission workflow update.
     *
     * <p>This method is intentionally synchronous. It is called by an
     * AFTER_COMMIT listener that must know whether delivery succeeded so the
     * committed history row can be marked SENT or FAILED.</p>
     */
    public void sendApplicationStageTransition(
            ErpApplication application,
            ErpApplicationStatusHistory history
    ) {
        validateApplicationStageTransitionEmail(
                application,
                history
        );

        String applicationNumber =
                application.getApplicationNo().trim();

        String recipientEmail =
                application.getPrimaryEmail().trim();

        String previousStage =
                humanizeWorkflowStatus(
                        history.getOldStatus()
                );

        String currentStage =
                humanizeWorkflowStatus(
                        history.getNewStatus()
                );

        try {
            Branch branch = requireBranch(
                    application.getBranch(),
                    "Application"
            );

            String schoolName = resolveSchoolName(branch);
            EmailLogo emailLogo = resolveBranchLogo(branch);

            Context context = new Context();

            context.setVariable(
                    "schoolName",
                    schoolName
            );

            context.setVariable(
                    "schoolLogo",
                    "cid:" + SCHOOL_LOGO_CONTENT_ID
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
                    applicationNumber
            );

            context.setVariable(
                    "previousStage",
                    previousStage
            );

            context.setVariable(
                    "currentStage",
                    currentStage
            );

            context.setVariable(
                    "stageTitle",
                    buildWorkflowEmailTitle(
                            history.getNewStatus()
                    )
            );

            context.setVariable(
                    "stageMessage",
                    buildWorkflowEmailMessage(
                            history.getNewStatus()
                    )
            );

            context.setVariable(
                    "publicRemarks",
                    trimToNull(
                            history.getPublicRemarks()
                    )
            );

            context.setVariable(
                    "trackingUrl",
                    buildTrackingUrl(applicationNumber)
            );

            context.setVariable(
                    "currentYear",
                    Year.now().getValue()
            );

            String htmlContent = templateEngine.process(
                    "email/application-stage-transition",
                    context
            );

            JavaMailSender branchMailSender =
                    branchMailSenderFactory.getMailSender(branch);

            MimeMessage message =
                    branchMailSender.createMimeMessage();

            MimeMessageHelper helper =
                    createMessageHelper(message);

            configureBranchSender(
                    helper,
                    branch,
                    " Admissions"
            );

            helper.setTo(recipientEmail);

            helper.setSubject(
                    buildWorkflowEmailSubject(
                            history.getNewStatus(),
                            applicationNumber
                    )
            );

            helper.setText(htmlContent, true);

            addInlineLogo(helper, emailLogo);

            branchMailSender.send(message);

            LOGGER.info(
                    "Application workflow email sent from branch {} "
                            + "<{}> for application {} and history {}.",
                    branch.getSchoolCode(),
                    branch.getBranchEmail(),
                    applicationNumber,
                    history.getHistoryId()
            );
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Application workflow email could not be sent "
                            + "for application "
                            + applicationNumber
                            + ".",
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
            throw new IllegalArgumentException(
                    "Employee is required for credential delivery."
            );
        }

        requireText(
                employee.getOfficialEmail(),
                "Employee official email"
        );

        requireText(
                username,
                "Employee username"
        );

        requireText(
                plainTextPassword,
                "Employee temporary password"
        );

        String employeeEmail =
                employee.getOfficialEmail().trim();

        try {
            Branch branch = requireBranch(
                    employee.getBranch(),
                    "Employee"
            );

            String schoolName = resolveSchoolName(branch);
            EmailLogo emailLogo = resolveBranchLogo(branch);

            Context context = new Context();

            context.setVariable(
                    "schoolName",
                    schoolName
            );

            context.setVariable(
                    "schoolLogo",
                    "cid:" + SCHOOL_LOGO_CONTENT_ID
            );

            context.setVariable(
                    "employeeName",
                    resolveEmployeeName(employee)
            );

            context.setVariable(
                    "username",
                    username.trim()
            );

            context.setVariable(
                    "tempPassword",
                    plainTextPassword
            );

            context.setVariable(
                    "loginUrl",
                    buildLoginUrl()
            );

            context.setVariable(
                    "currentYear",
                    Year.now().getValue()
            );

            String htmlContent = templateEngine.process(
                    "email/employee-welcome",
                    context
            );

            JavaMailSender branchMailSender =
                    branchMailSenderFactory.getMailSender(branch);

            MimeMessage message =
                    branchMailSender.createMimeMessage();

            MimeMessageHelper helper =
                    createMessageHelper(message);

            configureBranchSender(
                    helper,
                    branch,
                    " HR"
            );

            helper.setTo(employeeEmail);

            helper.setSubject(
                    "Welcome to "
                            + schoolName
                            + " - Your Account Details"
            );

            helper.setText(htmlContent, true);

            addInlineLogo(helper, emailLogo);

            branchMailSender.send(message);

            LOGGER.info(
                    "Employee welcome email sent from branch {} "
                            + "<{}> to: {}",
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
        validateBranchAdminRequest(
                branch,
                credentials
        );

        try {
            String schoolName = resolveSchoolName(branch);
            EmailLogo centralLogo = loadCentralMontfortLogo();

            Context context = new Context();

            context.setVariable(
                    "schoolName",
                    schoolName
            );

            context.setVariable(
                    "schoolLogo",
                    "cid:" + SCHOOL_LOGO_CONTENT_ID
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
                    credentials.getExpiresAt()
                            .format(EXPIRY_FORMATTER)
            );

            context.setVariable(
                    "loginUrl",
                    buildLoginUrl()
            );

            context.setVariable(
                    "currentYear",
                    Year.now().getValue()
            );

            String htmlContent = templateEngine.process(
                    "email/branch-admin-welcome",
                    context
            );

            MimeMessage message =
                    centralMailSender.createMimeMessage();

            MimeMessageHelper helper =
                    createMessageHelper(message);

            configureCentralSender(helper);

            helper.setTo(
                    branch.getBranchEmail().trim()
            );

            helper.setSubject(
                    "Branch Administrator Account - "
                            + schoolName
            );

            helper.setText(htmlContent, true);

            addInlineLogo(helper, centralLogo);

            centralMailSender.send(message);

            LOGGER.info(
                    "Branch Admin credentials email sent from "
                            + "central account <{}> for branch ID: {}",
                    centralMailUsername,
                    branch.getBranchId()
            );
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Branch Admin credentials email "
                            + "could not be sent.",
                    exception
            );
        }
    }

    private MimeMessageHelper createMessageHelper(
            MimeMessage message
    ) throws Exception {
        return new MimeMessageHelper(
                message,
                true,
                "UTF-8"
        );
    }

    private void configureBranchSender(
            MimeMessageHelper helper,
            Branch branch,
            String defaultSuffix
    ) throws Exception {
        validateBranchEmailConfiguration(branch);

        String branchEmail =
                branch.getBranchEmail().trim();

        String senderName =
                resolveSenderName(
                        branch,
                        defaultSuffix
                );

        helper.setFrom(
                branchEmail,
                senderName
        );

        helper.setReplyTo(
                resolveReplyTo(branch),
                senderName
        );
    }

    private void configureCentralSender(
            MimeMessageHelper helper
    ) throws Exception {
        requireText(
                centralMailUsername,
                "Central ERP sender email"
        );

        String senderEmail =
                centralMailUsername.trim();

        String senderName =
                hasText(centralMailFromName)
                        ? centralMailFromName.trim()
                        : "Montfort ERP";

        helper.setFrom(
                senderEmail,
                senderName
        );

        helper.setReplyTo(
                senderEmail,
                senderName
        );
    }

    private EmailLogo resolveBranchLogo(
            Branch branch
    ) {
        if (!hasText(branch.getBranchLogoUrl())) {
            return loadCentralMontfortLogo();
        }

        try {
            Resource logoResource =
                    fileStorageService.loadPrivateFile(
                            branch.getBranchLogoUrl()
                    );

            String contentType =
                    fileStorageService.detectContentType(
                            branch.getBranchLogoUrl()
                    );

            return new EmailLogo(
                    logoResource,
                    contentType
            );
        } catch (RuntimeException exception) {
            LOGGER.warn(
                    "Branch logo could not be loaded for branch ID: {}. "
                            + "The central Montfort logo will be used.",
                    branch.getBranchId()
            );

            return loadCentralMontfortLogo();
        }
    }

    private EmailLogo loadCentralMontfortLogo() {
        Resource logoResource =
                new ClassPathResource(DEFAULT_LOGO_PATH);

        if (!logoResource.exists()) {
            throw new IllegalStateException(
                    "The central Montfort email logo "
                            + "was not found: "
                            + DEFAULT_LOGO_PATH
            );
        }

        return new EmailLogo(
                logoResource,
                DEFAULT_LOGO_CONTENT_TYPE
        );
    }

    private void addInlineLogo(
            MimeMessageHelper helper,
            EmailLogo emailLogo
    ) throws Exception {
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
            throw new IllegalArgumentException(
                    "Branch is required."
            );
        }

        requireText(
                branch.getBranchEmail(),
                "Branch email"
        );

        if (credentials == null) {
            throw new IllegalArgumentException(
                    "Branch Admin credentials are required."
            );
        }

        requireText(
                credentials.getUsername(),
                "Branch Admin username"
        );

        requireText(
                credentials.getTemporaryPassword(),
                "Branch Admin temporary password"
        );

        if (credentials.getExpiresAt() == null) {
            throw new IllegalArgumentException(
                    "Branch Admin credential expiry time is required."
            );
        }
    }

    private void validateBranchEmailConfiguration(
            Branch branch
    ) {
        requireBranch(
                branch,
                "Email"
        );

        requireText(
                branch.getSchoolCode(),
                "Branch school code"
        );

        requireText(
                branch.getBranchEmail(),
                "Branch email"
        );

        if (Boolean.FALSE.equals(
                branch.getEmailEnabled()
        )) {
            throw new IllegalStateException(
                    "Email is disabled for branch: "
                            + branch.getSchoolCode()
            );
        }
    }

    private void validateApplicationStageTransitionEmail(
            ErpApplication application,
            ErpApplicationStatusHistory history
    ) {
        if (application == null) {
            throw new IllegalArgumentException(
                    "Application is required for workflow email delivery."
            );
        }

        requireText(
                application.getApplicationNo(),
                "Application number"
        );

        requireText(
                application.getPrimaryEmail(),
                "Applicant email"
        );

        if (history == null) {
            throw new IllegalArgumentException(
                    "Application workflow history is required."
            );
        }

        if (history.getHistoryId() == null
                || history.getHistoryId() <= 0) {
            throw new IllegalArgumentException(
                    "Application workflow history ID is required."
            );
        }

        requireText(
                history.getStage(),
                "Workflow history stage"
        );

        requireText(
                history.getNewStatus(),
                "Workflow target stage"
        );

        if (!Boolean.TRUE.equals(
                history.getEmailRequired()
        )) {
            throw new IllegalArgumentException(
                    "The workflow history does not require applicant email delivery."
            );
        }

        if (history.getApplication() != null
                && history.getApplication().getApplicationId() != null
                && application.getApplicationId() != null
                && !application.getApplicationId().equals(
                history.getApplication().getApplicationId()
        )) {
            throw new IllegalArgumentException(
                    "Workflow history does not belong to the application."
            );
        }
    }

    private Branch requireBranch(
            Branch branch,
            String operationName
    ) {
        if (branch == null) {
            throw new IllegalArgumentException(
                    operationName
                            + " branch is required "
                            + "for email delivery."
            );
        }

        return branch;
    }

    private String resolveSchoolName(
            Branch branch
    ) {
        if (hasText(branch.getBranchName())) {
            return branch.getBranchName().trim();
        }

        return "Montfort School";
    }

    private String resolveSenderName(
            Branch branch,
            String defaultSuffix
    ) {
        if (hasText(branch.getEmailFromName())) {
            return branch.getEmailFromName().trim();
        }

        return resolveSchoolName(branch)
                + defaultSuffix;
    }

    private String resolveReplyTo(
            Branch branch
    ) {
        if (hasText(branch.getEmailReplyTo())) {
            return branch.getEmailReplyTo().trim();
        }

        return branch.getBranchEmail().trim();
    }

    private String resolveEmployeeName(
            ErpEmployee employee
    ) {
        if (hasText(employee.getFullName())) {
            return employee.getFullName().trim();
        }

        return buildFullName(
                employee.getFirstName(),
                employee.getLastName()
        );
    }

    private String buildTrackingUrl(
            String applicationNumber
    ) {
        requireText(
                applicationNumber,
                "Application number"
        );

        return normalizeConfiguredUrl(
                appBaseUrl,
                "app.base-url"
        )
                + "/apply/status?ref="
                + applicationNumber.trim();
    }

    private String buildAdditionalDocumentUploadUrl(
            String rawUploadToken
    ) {
        requireText(
                rawUploadToken,
                "Additional-document upload token"
        );

        String encodedToken =
                URLEncoder.encode(
                        rawUploadToken.trim(),
                        StandardCharsets.UTF_8
                );

        return normalizeConfiguredUrl(
                appBaseUrl,
                "app.base-url"
        )
                + "/apply/document-upload?token="
                + encodedToken;
    }

    private String formatDocumentUploadDeadline(
            java.time.LocalDateTime deadline
    ) {
        return deadline == null
                ? null
                : deadline.format(
                        DOCUMENT_DEADLINE_FORMATTER
                );
    }

    private void validateAdditionalDocumentEmailRequest(
            ErpApplication application,
            ErpApplicationDocumentRequest documentRequest,
            String rawUploadToken
    ) {
        if (application == null) {
            throw new IllegalArgumentException(
                    "Application is required for document-request email delivery."
            );
        }

        requireText(
                application.getApplicationNo(),
                "Application number"
        );

        requireText(
                application.getPrimaryEmail(),
                "Applicant email"
        );

        if (documentRequest == null) {
            throw new IllegalArgumentException(
                    "Application document request is required."
            );
        }

        requireText(
                documentRequest.getRequestedDocumentName(),
                "Requested document name"
        );

        requireText(
                documentRequest.getRequestReason(),
                "Document request reason"
        );

        requireText(
                rawUploadToken,
                "Additional-document upload token"
        );

        if (documentRequest.getRequestStatus()
                != ErpApplicationDocumentRequest
                .RequestStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only a pending document request can be emailed."
            );
        }

        if (documentRequest.getApplication() != null
                && documentRequest.getApplication()
                .getApplicationId() != null
                && application.getApplicationId() != null
                && !application.getApplicationId().equals(
                documentRequest.getApplication()
                        .getApplicationId()
        )) {
            throw new IllegalArgumentException(
                    "Document request does not belong to the application."
            );
        }

        if (documentRequest.getUploadTokenExpiresAt() == null) {
            throw new IllegalArgumentException(
                    "Document upload-token expiry is required."
            );
        }
    }

    private String buildWorkflowEmailSubject(
            String targetStage,
            String applicationNumber
    ) {
        String normalized =
                normalizeWorkflowStatus(targetStage);

        String subjectPrefix =
                switch (normalized) {
                    case "SCHOOL_VISIT" ->
                            "School Visit Update";
                    case "ENTRANCE_TEST" ->
                            "Entrance Test Update";
                    case "PARENT_FEE_DISCUSSION" ->
                            "Fee Discussion Update";
                    case "SCHOLARSHIP" ->
                            "Scholarship Review Update";
                    case "PAYMENT" ->
                            "Admission Payment Update";
                    case "FINAL_ADMISSION" ->
                            "Admission Approved";
                    case "ENROLLED" ->
                            "Enrollment Confirmed";
                    case "CLOSED" ->
                            "Admission Application Decision";
                    case "APPLICATION_VERIFICATION" ->
                            "Application Review Update";
                    default ->
                            "Admission Application Update";
                };

        return subjectPrefix
                + " - "
                + applicationNumber;
    }

    private String buildWorkflowEmailTitle(
            String targetStage
    ) {
        String normalized =
                normalizeWorkflowStatus(targetStage);

        return switch (normalized) {
            case "APPLICATION_VERIFICATION" ->
                    "Application Under Review";
            case "SCHOOL_VISIT" ->
                    "School Visit Stage";
            case "ENTRANCE_TEST" ->
                    "Entrance Test Stage";
            case "PARENT_FEE_DISCUSSION" ->
                    "Parent Fee Discussion";
            case "SCHOLARSHIP" ->
                    "Scholarship Review";
            case "PAYMENT" ->
                    "Admission Payment";
            case "FINAL_ADMISSION" ->
                    "Admission Approved";
            case "ENROLLED" ->
                    "Enrollment Confirmed";
            case "CLOSED" ->
                    "Application Closed";
            case "APPLICATION_DRAFT" ->
                    "Application Returned for Review";
            default ->
                    "Admission Application Update";
        };
    }

    private String buildWorkflowEmailMessage(
            String targetStage
    ) {
        String normalized =
                normalizeWorkflowStatus(targetStage);

        return switch (normalized) {
            case "APPLICATION_VERIFICATION" ->
                    "The school is reviewing the application and its supporting documents.";
            case "SCHOOL_VISIT" ->
                    "The application has progressed to the school visit stage. "
                            + "The school will provide visit details when arranged.";
            case "ENTRANCE_TEST" ->
                    "The application has progressed to the entrance test stage. "
                            + "The school will provide the assessment details.";
            case "PARENT_FEE_DISCUSSION" ->
                    "The application has progressed to the parent and school fee discussion stage.";
            case "SCHOLARSHIP" ->
                    "The application has been forwarded for scholarship review.";
            case "PAYMENT" ->
                    "The application has progressed to admission payment processing.";
            case "FINAL_ADMISSION" ->
                    "The admission application has been approved, subject to the school's final enrollment process.";
            case "ENROLLED" ->
                    "The admission process is complete and the learner has been enrolled.";
            case "CLOSED" ->
                    "The admission application has been closed. Please review the school remarks below, when provided.";
            case "APPLICATION_DRAFT" ->
                    "The application has been returned for further review or correction.";
            default ->
                    "The admission application status has been updated by the school.";
        };
    }

    private String humanizeWorkflowStatus(
            String status
    ) {
        if (!hasText(status)) {
            return "Not Available";
        }

        String normalized =
                status.trim()
                        .toLowerCase(Locale.ROOT)
                        .replace('_', ' ')
                        .replaceAll("\\s+", " ");

        StringBuilder result =
                new StringBuilder(normalized.length());

        boolean capitalizeNext = true;

        for (int index = 0;
             index < normalized.length();
             index++) {

            char character =
                    normalized.charAt(index);

            if (capitalizeNext
                    && Character.isLetter(character)) {
                result.append(
                        Character.toUpperCase(character)
                );
                capitalizeNext = false;
            } else {
                result.append(character);
            }

            if (Character.isWhitespace(character)) {
                capitalizeNext = true;
            }
        }

        return result.toString();
    }

    private String normalizeWorkflowStatus(
            String status
    ) {
        return hasText(status)
                ? status.trim()
                .toUpperCase(Locale.ROOT)
                : "";
    }

    private String buildLoginUrl() {
        return normalizeConfiguredUrl(
                appLoginUrl,
                "app.login-url"
        );
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

    private String normalizeConfiguredUrl(
            String url,
            String propertyName
    ) {
        if (!hasText(url)) {
            throw new IllegalStateException(
                    propertyName
                            + " is not configured."
            );
        }

        String normalizedUrl = url.trim();

        while (normalizedUrl.endsWith("/")) {
            normalizedUrl = normalizedUrl.substring(
                    0,
                    normalizedUrl.length() - 1
            );
        }

        try {
            URI parsedUrl = URI.create(normalizedUrl);

            String scheme = parsedUrl.getScheme();

            boolean validScheme =
                    "https".equalsIgnoreCase(scheme)
                            || "http".equalsIgnoreCase(scheme);

            if (!validScheme
                    || parsedUrl.getHost() == null) {
                throw new IllegalArgumentException(
                        "Invalid application URL."
                );
            }
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    propertyName
                            + " must contain a valid "
                            + "HTTP or HTTPS URL.",
                    exception
            );
        }

        return normalizedUrl;
    }

    private String trimToNull(
            String value
    ) {
        return hasText(value)
                ? value.trim()
                : null;
    }

    private boolean hasText(
            String value
    ) {
        return value != null
                && !value.isBlank();
    }

    private void requireText(
            String value,
            String fieldName
    ) {
        if (!hasText(value)) {
            throw new IllegalArgumentException(
                    fieldName + " is required."
            );
        }
    }

    private record EmailLogo(
            Resource resource,
            String contentType
    ) {
    }
}

package com.erp.montfortuganda.notification.service;

import com.erp.montfortuganda.admission.entity.ErpApplication;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.time.Year;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    // Injects the domain from application.properties (defaults to localhost if missing)
    @Value("${app.frontend.url:http://localhost:8080}")
    private String frontendUrl;

    @Async
    public void sendApplicationReceipt(ErpApplication app) {
        try {
            if (app.getPrimaryEmail() == null || app.getPrimaryEmail().trim().isEmpty()) {
                return;
            }

            // 1. Prepare Variables for Thymeleaf
            Context context = new Context();

            // Refactored: Call the extracted helper method
            context.setVariable("schoolName", buildSchoolName(app));
            context.setVariable("schoolLogo", "cid:schoolLogoImage");
            context.setVariable("studentName", app.getFirstName() + " " + app.getLastName());
            context.setVariable("applicationNo", app.getApplicationNo());
            context.setVariable("currentYear", Year.now().getValue());

            // 2. Build the exact tracking URL using your domain!
            String finalTrackingUrl = frontendUrl + "/apply/status?ref=" + app.getApplicationNo();
            context.setVariable("trackingUrl", finalTrackingUrl);
            context.setVariable("frontendUrl", frontendUrl);

            // 3. Process HTML
            String htmlContent = templateEngine.process("email/application-confirmation", context);

            // 4. Create Message
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(app.getPrimaryEmail());
            helper.setSubject("Application Received - " + app.getApplicationNo());
            helper.setText(htmlContent, true);



            // 6. Send Email
            mailSender.send(message);
            System.out.println("Email sent successfully to: " + app.getPrimaryEmail());

        } catch (Exception e) {
            System.err.println("Failed to send Thymeleaf email to " + app.getPrimaryEmail() + ": " + e.getMessage());
        }
    }

    /**
     * Helper method to build the strictly formatted school name: (code)name, location
     */
    private String buildSchoolName(ErpApplication app) {
        if (app.getBranch() == null) {
            return "Montfort School";
        }
        // ONLY get the Branch Name and Location, ignoring the School Code
        String name = app.getBranch().getBranchName() != null ? app.getBranch().getBranchName() : "";
        String loc = app.getBranch().getBranchLocation() != null ? ", " + app.getBranch().getBranchLocation() : "";
        return name + loc;
    }
}
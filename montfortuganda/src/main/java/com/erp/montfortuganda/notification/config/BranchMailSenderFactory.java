package com.erp.montfortuganda.notification.config;

import com.erp.montfortuganda.school.entity.Branch;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Component
@SuppressWarnings("SpellCheckingInspection")
public class BranchMailSenderFactory {

    private static final String DEFAULT_MAIL_HOST = "smtp.gmail.com";
    private static final int DEFAULT_MAIL_PORT = 587;
    private static final String CONNECTION_TIMEOUT = "10000";
    private static final String READ_TIMEOUT = "10000";
    private static final String WRITE_TIMEOUT = "10000";

    private final Environment environment;
    private final Map<SenderCacheKey, JavaMailSender> senderCache = new ConcurrentHashMap<>();

    public BranchMailSenderFactory(Environment environment) {
        this.environment = environment;
    }

    public JavaMailSender getMailSender(Branch branch) {
        validateBranch(branch);

        SenderCacheKey cacheKey = new SenderCacheKey(
                normalizeSchoolCode(branch.getSchoolCode()),
                branch.getBranchEmail().trim().toLowerCase(Locale.ROOT)
        );

        return senderCache.computeIfAbsent(cacheKey, this::createMailSender);
    }

    public void evict(Branch branch) {
        if (branch == null || branch.getSchoolCode() == null || branch.getSchoolCode().isBlank()) {
            return;
        }

        String schoolCode = normalizeSchoolCode(branch.getSchoolCode());
        senderCache.keySet().removeIf(key -> key.schoolCode().equals(schoolCode));
    }

    public void clearCache() {
        senderCache.clear();
    }

    private JavaMailSender createMailSender(SenderCacheKey cacheKey) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setProtocol(environment.getProperty("erp.mail.protocol", "smtp"));
        mailSender.setHost(environment.getProperty("erp.mail.host", DEFAULT_MAIL_HOST));
        mailSender.setPort(environment.getProperty("erp.mail.port", Integer.class, DEFAULT_MAIL_PORT));
        mailSender.setUsername(cacheKey.branchEmail());
        mailSender.setPassword(getBranchAppPassword(cacheKey.schoolCode()));
        mailSender.setDefaultEncoding("UTF-8");
        mailSender.setJavaMailProperties(buildSmtpProperties());

        return mailSender;
    }

    private Properties buildSmtpProperties() {
        Properties properties = new Properties();

        properties.put("mail.smtp.auth", Boolean.toString(getBooleanProperty("erp.mail.auth", true)));
        properties.put("mail.smtp.starttls.enable", Boolean.toString(getBooleanProperty("erp.mail.starttls-enabled", true)));
        properties.put("mail.smtp.starttls.required", Boolean.toString(getBooleanProperty("erp.mail.starttls-required", true)));
        properties.put("mail.smtp.connectiontimeout", environment.getProperty("erp.mail.connection-timeout", CONNECTION_TIMEOUT));
        properties.put("mail.smtp.timeout", environment.getProperty("erp.mail.read-timeout", READ_TIMEOUT));
        properties.put("mail.smtp.writetimeout", environment.getProperty("erp.mail.write-timeout", WRITE_TIMEOUT));
        properties.put("mail.smtp.ssl.trust", environment.getProperty("erp.mail.ssl-trust", DEFAULT_MAIL_HOST));

        return properties;
    }

    private boolean getBooleanProperty(String propertyName, boolean defaultValue) {
        return environment.getProperty(propertyName, Boolean.class, defaultValue);
    }

    private String getBranchAppPassword(String schoolCode) {
        String propertyName = "erp.mail.branch-passwords." + schoolCode;
        String appPassword = environment.getProperty(propertyName);

        if (appPassword == null || appPassword.isBlank()) {
            throw new IllegalStateException(
                    "No branch email App Password is configured for school code: "
                            + schoolCode.toUpperCase(Locale.ROOT)
            );
        }

        return appPassword.trim();
    }

    private void validateBranch(Branch branch) {
        if (branch == null) {
            throw new IllegalArgumentException("Branch is required to create a branch mail sender.");
        }
        if (branch.getSchoolCode() == null || branch.getSchoolCode().isBlank()) {
            throw new IllegalArgumentException("Branch school code is required.");
        }
        if (branch.getBranchEmail() == null || branch.getBranchEmail().isBlank()) {
            throw new IllegalArgumentException("Branch email is required.");
        }
        if (Boolean.FALSE.equals(branch.getEmailEnabled())) {
            throw new IllegalStateException("Email is disabled for branch: " + branch.getSchoolCode());
        }
    }

    private String normalizeSchoolCode(String schoolCode) {
        return schoolCode.trim().toLowerCase(Locale.ROOT);
    }

    private record SenderCacheKey(String schoolCode, String branchEmail) {
    }
}
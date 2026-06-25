package com.erp.montfortuganda.admission.service;

import com.erp.montfortuganda.admission.entity.ErpApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.lang.reflect.Field;

@Service
public class SecuritySanitizerService {

    // Common SQL Injection Signatures
    private static final String[] SQLI_SIGNATURES = {
            "DROP TABLE", "SELECT *", "UNION SELECT", "INSERT INTO", "DELETE FROM", "--", "1=1"
    };

    public void sanitizeAndValidate(ErpApplication app) throws Exception {
        // Use Java Reflection to automatically check and sanitize EVERY string field
        Field[] fields = ErpApplication.class.getDeclaredFields();

        for (Field field : fields) {
            if (field.getType().equals(String.class)) {
                field.setAccessible(true);
                String value = (String) field.get(app);

                if (value != null && !value.isEmpty()) {

                    // 1. SQLi Hostile Signature Check
                    String upperValue = value.toUpperCase();
                    for (String sig : SQLI_SIGNATURES) {
                        if (upperValue.contains(sig)) {
                            throw new Exception("SECURITY ALERT: Hostile SQL payload detected and blocked.");
                        }
                    }

                    // 2. Payload Truncation (Prevent Memory Exhaustion DoS)
                    // If the string is over 250 characters (and isn't the JSON field), aggressively chop it!
                    if (value.length() > 250 && !field.getName().equals("subjectMarks") && !field.getName().equals("moreInfo")) {
                        value = value.substring(0, 250);
                    }

                    // 3. Stored XSS Prevention (HTML Escaping)
                    // Converts malicious <script> into harmless &lt;script&gt;
                    String sanitizedValue = HtmlUtils.htmlEscape(value);

                    // Put the safe value back into the object
                    field.set(app, sanitizedValue);
                }
            }
        }
    }
}
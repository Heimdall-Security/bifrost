package com.heimdallauth.server.configuration;

import jakarta.annotation.PostConstruct;
import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

@ConfigurationProperties(prefix = "heimdall.bifrost.security.oauth2.roles")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Component
public class HeimdallBifrostRoleConfiguration {
    //Management API roles
    private String ROLE_MANAGEMENT_READ;
    private String ROLE_MANAGEMENT_WRITE;
    private String ROLE_MANAGEMENT_SUPPRESSION_ENTRY_READ;
    private String ROLE_MANAGEMENT_SUPPRESSION_ENTRY_WRITE;
    private String ROLE_MANAGEMENT_CONFIGURATION_SET_READ;
    private String ROLE_MANAGEMENT_CONFIGURATION_SET_WRITE;
    private String ROLE_MANAGEMENT_EMAIL_TEMPLATE_READ_TENANT;
    private String ROLE_MANAGEMENT_EMAIL_TEMPLATE_WRITE_TENANT;
    private String ROLE_MANAGEMENT_EMAIL_TEMPLATE_READ;
    private String ROLE_MANAGEMENT_EMAIL_TEMPLATE_WRITE;
    private String ROLE_MANAGEMENT_SMTP_READ;
    private String ROLE_MANAGEMENT_SMTP_WRITE;
    // Send Email Role
    private String ROLE_SEND_EMAIL;
    private String SCOPE_SEND_EMAIL;
    //Management API Scope
    private String SCOPE_MANAGEMENT_READ;
    private String SCOPE_MANAGEMENT_WRITE;

    /**
     * This method is used to update the scope mapping for the roles.
     * It will add the prefix "SCOPE_" to the roles that do not already have it.
     */
    @PostConstruct
    private void updateScopeMapping() {
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field currentField : fields) {
            if (currentField.getName().startsWith("SCOPE")) {
                ReflectionUtils.makeAccessible(currentField);
                try {
                    String currentValue = (String) currentField.get(this);
                    if (currentValue != null && !currentValue.startsWith("SCOPE_")) {
                        currentField.set(this, "SCOPE_%s".formatted(currentValue.toUpperCase()));
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

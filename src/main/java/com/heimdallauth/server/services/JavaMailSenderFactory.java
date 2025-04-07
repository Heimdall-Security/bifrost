package com.heimdallauth.server.services;

import com.heimdallauth.server.constants.bifrost.SmtpAuthenticationMethod;
import com.heimdallauth.server.exceptions.ConfigurationSetNotFound;
import com.heimdallauth.server.models.bifrost.ConfigurationSetModel;
import com.heimdallauth.server.models.bifrost.SmtpProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class JavaMailSenderFactory {
    private final JavaMailSender platformJavaMailSender;
    private final ConfigurationSetManagementService configurationSetManagementService;
    private final Map<String, JavaMailSender> javaMailSenderMap;


    public JavaMailSenderFactory(JavaMailSender platformJavaMailSender, ConfigurationSetManagementService configurationSetManagementService) {
        this.configurationSetManagementService = configurationSetManagementService;
        this.platformJavaMailSender = platformJavaMailSender;
        this.javaMailSenderMap = new HashMap<>();
    }

    private JavaMailSender getPlatformJavaMailSender() {
        return getJavaMailSender(new SmtpProperties(
                "smtp.example.com",
                25,
                "username",
                "password",
                "noreply@local.heimdallauth.com",
                "",
                SmtpAuthenticationMethod.NONE,
                10,
                1000,
                true,
                Collections.emptyMap()
        ));
    }
    /**
     * This method creates a JavaMailSender instance based on the provided SmtpProperties.
     * It sets the host, port, username, password, and encoding for the JavaMailSender.
     * If an authentication method is specified, it adds it to the JavaMail properties.
     *
     * @param smtpProperties The SmtpProperties object containing SMTP configuration.
     * @return A configured JavaMailSender instance.
     */
    private JavaMailSender getJavaMailSender(SmtpProperties smtpProperties) {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(smtpProperties.serverAddress());
        javaMailSender.setPort(smtpProperties.portNumber());
        javaMailSender.setUsername(smtpProperties.loginUsername());
        javaMailSender.setPassword(smtpProperties.loginPassword());
        javaMailSender.setDefaultEncoding("UTF-8");
        if(smtpProperties.authenticationMethod() != null) {
            if(smtpProperties.authenticationMethod().equals(SmtpAuthenticationMethod.NONE)) {
                javaMailSender.getJavaMailProperties().put("mail.smtp.auth", smtpProperties.authenticationMethod().name());
            }
        }
        if(smtpProperties.messageHeaders() != null) {
            for (Map.Entry<String, String> entry : smtpProperties.messageHeaders().entrySet()) {
                javaMailSender.getJavaMailProperties().put(entry.getKey(), entry.getValue());
            }
        }
        return javaMailSender;
    }

    /**
     * This method configures a JavaMailSender instance based on the provided configurationId.
     * It retrieves the SmtpProperties from the ConfigurationSetModel and creates a JavaMailSender.
     * If the configurationId is not found, it returns the default platform mail sender.
     *
     * @param configurationId The UUID of the configuration set.
     * @return A configured JavaMailSender instance.
     */
    private JavaMailSender configureMailSenderFromDataSource(UUID configurationId){
        log.debug("Configuring JavaMailSender from DataSource");
        try{
            Optional<ConfigurationSetModel> configurationSetModel = Optional.ofNullable(this.configurationSetManagementService.getConfigurationSetById(configurationId));
            configurationSetModel.ifPresentOrElse(
                    configuration -> {
                        SmtpProperties smtpProperties = configuration.smtpProperties();
                        JavaMailSender javaMailSender = getJavaMailSender(smtpProperties);
                        javaMailSenderMap.put(configurationId.toString(), javaMailSender);

                    },
                    () -> {
                        throw new ConfigurationSetNotFound("Configuration set not found for id: " + configurationId);
                    }
            );
        }catch (ConfigurationSetNotFound ex){
            log.debug("SMTP Properties not found. Using default platform mail sender");
            return platformJavaMailSender;
        }
        return javaMailSenderMap.get(configurationId.toString());
    }
    /**
     * This method retrieves a JavaMailSender instance based on the provided configurationId.
     * It checks if the JavaMailSender is already present in the cache map. If not, it configures
     * a new JavaMailSender using the configurationId and returns it.
     *
     * @param configurationId The UUID of the configuration set.
     * @return A configured JavaMailSender instance.
     */
    public JavaMailSender getMailSender(UUID configurationId) {
        if(configurationId == null) {
            log.debug("Configuration ID is null. Using default platform mail sender");
            return platformJavaMailSender;
        }
        // Check if present in cache map
        //TODO Implement with caffeine library later
        return javaMailSenderMap.getOrDefault(configurationId.toString(), configureMailSenderFromDataSource(configurationId));
    }
}

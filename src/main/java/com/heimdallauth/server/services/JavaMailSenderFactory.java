package com.heimdallauth.server.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.heimdallauth.server.constants.bifrost.SmtpAuthenticationMethod;
import com.heimdallauth.server.exceptions.ConfigurationSetNotFound;
import com.heimdallauth.server.models.bifrost.ConfigurationSetModel;
import com.heimdallauth.server.models.bifrost.SmtpProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class JavaMailSenderFactory {
    private final JavaMailSender platformJavaMailSender;
    private final ConfigurationSetManagementService configurationSetManagementService;
    private final Cache<String, JavaMailSender> mailSenderCache;

    public JavaMailSenderFactory(JavaMailSender platformJavaMailSender, ConfigurationSetManagementService configurationSetManagementService) {
        this.configurationSetManagementService = configurationSetManagementService;
        this.platformJavaMailSender = platformJavaMailSender;
        this.mailSenderCache = Caffeine.newBuilder().expireAfterWrite(10,TimeUnit.MINUTES).maximumSize(1000).build();
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
                        mailSenderCache.put(configurationId.toString(), javaMailSender);

                    },
                    () -> {
                        throw new ConfigurationSetNotFound("Configuration set not found for id: " + configurationId);
                    }
            );
        }catch (ConfigurationSetNotFound ex){
            log.debug("SMTP Properties not found. Using default platform mail sender");
            return platformJavaMailSender;
        }
        return mailSenderCache.getIfPresent(configurationId.toString());
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
        return mailSenderCache.get(configurationId.toString(), key -> {
            log.debug("JavaMailSender not found in cache. Configuring a new one.");
            return configureMailSenderFromDataSource(configurationId);
        });
    }
}

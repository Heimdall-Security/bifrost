package com.heimdallauth.server.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.heimdallauth.server.constants.bifrost.SmtpAuthenticationMethod;
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
    private final Cache<String, JavaMailSender> mailSenderCache;

    public JavaMailSenderFactory(JavaMailSender platformJavaMailSender) {
        this.platformJavaMailSender = platformJavaMailSender;
        this.mailSenderCache = Caffeine.newBuilder().expireAfterWrite(5,TimeUnit.HOURS).maximumSize(1000).build();
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
        log.debug("Creating new instance of JavaMailSender for smtpProperties: {}", smtpProperties.propertiesId());
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
     * This method retrieves the JavaMailSender instance from cache or creates a new one if it doesn't exist.
     *
     * @param smtpProperties The SmtpProperties from the configuration set
     * @return A configured JavaMailSender instance.
     */
    public JavaMailSender getMailSender(Optional<SmtpProperties> smtpProperties) {
        if(smtpProperties.isEmpty()) {
            log.debug("No smtpProperties found, returning platform returning platform JavaMailSender");
            return platformJavaMailSender;
        }else{
            return mailSenderCache.get(smtpProperties.get().propertiesId(), key -> getJavaMailSender(smtpProperties.get()));
        }
    }

    protected void evictCache(UUID configurationId) {
        mailSenderCache.invalidate(configurationId.toString());
    }
}

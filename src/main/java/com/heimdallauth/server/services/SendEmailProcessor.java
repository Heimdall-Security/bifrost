package com.heimdallauth.server.services;

import com.heimdallauth.server.dto.bifrost.SendEmailDTO;
import com.heimdallauth.server.exceptions.ConfigurationSetNotFound;
import com.heimdallauth.server.exceptions.HeimdallBifrostBadDataException;
import com.heimdallauth.server.exceptions.TemplateNotFound;
import com.heimdallauth.server.models.bifrost.ConfigurationSetModel;
import com.heimdallauth.server.models.bifrost.EmailContext;
import com.heimdallauth.server.models.bifrost.Template;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

@Service
@Slf4j
public class SendEmailProcessor {
    private final EmailTemplatingService emailTemplatingService;
    private final TemplateManagementService templateManagementService;
    private final JavaMailSenderFactory javaMailSenderFactory;
    private final ConfigurationSetManagementService configurationSetManagementService;

    private static final String DEFAULT_FROM_ADDRESS = "noreply@mayanksoni.tech";

    private ThreadLocal<ConfigurationSetModel> currentConfigurationSet = new ThreadLocal<>();

    public SendEmailProcessor(EmailTemplatingService emailTemplatingService, TemplateManagementService templateManagementService, JavaMailSenderFactory javaMailSenderFactory, ConfigurationSetManagementService configurationSetManagementService) {
        this.emailTemplatingService = emailTemplatingService;
        this.templateManagementService = templateManagementService;
        this.javaMailSenderFactory = javaMailSenderFactory;
        this.configurationSetManagementService = configurationSetManagementService;
    }

    public void processSendEmail(SendEmailDTO sendEmailDTO) {
        String processedEmailSubject = null;
        String processedEmailPlainTextBody = null;
        String processedEmailHtmlBody = null;
        try{
            validateSendEmailPayload(sendEmailDTO);
            if(sendEmailDTO.content() != null){
                log.debug("Processing Inline Email Content");
                Map<String, Object> context = convertContextToMap(sendEmailDTO.context());
                processedEmailSubject = emailTemplatingService.processString(sendEmailDTO.content().subject(), context);
                processedEmailHtmlBody = emailTemplatingService.processString(sendEmailDTO.content().htmlBodyContent(), context);
                processedEmailPlainTextBody = emailTemplatingService.processString(sendEmailDTO.content().plainTextContent(), context);
                log.debug("Sending Email using platform sender");
                JavaMailSender platformJavaMailSender = javaMailSenderFactory.getMailSender(null);
                this.connectAndSendEmail(
                        platformJavaMailSender,
                        sendEmailDTO.destination().toDestinationEmailAddress().stream().map(String::new).toArray(String[]::new),
                        processedEmailSubject,
                        processedEmailHtmlBody,
                        processedEmailPlainTextBody,
                        DEFAULT_FROM_ADDRESS
                );
            }else if(sendEmailDTO.templateId() != null){
                try{
                    log.debug("Fetching Template From repository and processing");
                    Template fetchedTemplate = this.templateManagementService.getTemplateById(sendEmailDTO.templateId());
                    ConfigurationSetModel configurationSetModel = this.configurationSetManagementService.getConfigurationSetById(sendEmailDTO.configurationSetId());
                    if(fetchedTemplate.tenantId() != configurationSetModel.tenantId()){
                        log.error("Tenant ID mismatch for Template ID: {} and ConfigurationSet ID: {}", sendEmailDTO.templateId(), sendEmailDTO.configurationSetId());
                        throw new HeimdallBifrostBadDataException("Template does not belong to the same tenant as the configuration set");
                    }
                    Map<String, Object> context = convertContextToMap(sendEmailDTO.context());
                    processedEmailSubject = emailTemplatingService.processString(fetchedTemplate.content().subject(),context );
                    processedEmailHtmlBody = emailTemplatingService.processString(fetchedTemplate.content().htmlBodyContent(),context);
                    processedEmailPlainTextBody = emailTemplatingService.processString(fetchedTemplate.content().plainTextContent(),context);
                    JavaMailSender tenantJavaMailSender = javaMailSenderFactory.getMailSender(sendEmailDTO.configurationSetId());
                    this.connectAndSendEmail(
                            tenantJavaMailSender,
                            sendEmailDTO.destination().toDestinationEmailAddress().stream().map(String::new).toArray(String[]::new),
                            processedEmailSubject,
                            processedEmailHtmlBody,
                            processedEmailPlainTextBody,
                            configurationSetModel.smtpProperties().fromEmailAddress()
                    );
                }catch (TemplateNotFound e){
                    log.error("Template not found for ID: {}", sendEmailDTO.templateId());
                    throw new HeimdallBifrostBadDataException("Template not found", e);
                }catch (ConfigurationSetNotFound e){
                    log.error("ConfigurationSet not found for ID: {}", sendEmailDTO.configurationSetId());
                    throw new HeimdallBifrostBadDataException("ConfigurationSet not found", e);
                }

            }
        }catch (RuntimeException e){
            log.error("Validation failed for SendEmailDTO: {}", e.getMessage());
            throw new HeimdallBifrostBadDataException("Invalid SendEmailDTO", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void connectAndSendEmail(JavaMailSender mailSender, String[] to, String subject, String htmlBody, String plainTextBody, String fromEmailAddress) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage,true);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(plainTextBody, htmlBody);
            mimeMessageHelper.setFrom(fromEmailAddress);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("Error sending email: {}", e.getMessage());
            throw new HeimdallBifrostBadDataException("Error sending email", e);
        }

    }
    private void validateSendEmailPayload(SendEmailDTO sendEmailDTO) {
        assertNotNull(sendEmailDTO, "SendEmailDTO cannot be null");
        assertNotNull(sendEmailDTO.destination(), "Destination cannot be null");
        assertNotNull(sendEmailDTO.context(), "Context cannot be null");

    }
    private void validateTenantForConfigurationSetId(UUID configurationSetId, UUID templateTenantId){
        try{
            this.configurationSetManagementService.getConfigurationSetById(configurationSetId);
        }catch (ConfigurationSetNotFound e){
            log.error("ConfigurationSet not found for ID: {}", configurationSetId);
            throw new HeimdallBifrostBadDataException("ConfigurationSet not found", e);
        }
    }
    private Map<String, Object> convertContextToMap(EmailContext context) {
        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("user", context.user());
        contextMap.put("organization", context.organizationContext());
        contextMap.put("variables", context.variables());
        return contextMap;
    }
}

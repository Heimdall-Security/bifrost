package com.heimdallauth.server.services;

import com.heimdallauth.server.dto.bifrost.SendEmailDTO;
import com.heimdallauth.server.exceptions.ConfigurationSetNotFound;
import com.heimdallauth.server.exceptions.HeimdallBifrostBadDataException;
import com.heimdallauth.server.exceptions.TemplateNotFound;
import com.heimdallauth.server.models.bifrost.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

@Service
@Slf4j
public class SendEmailProcessor {
    private final EmailTemplatingService emailTemplatingService;
    private final TemplateManagementService templateManagementService;
    private final JavaMailSenderFactory javaMailSenderFactory;
    private final ConfigurationSetManagementService configurationSetManagementService;

    private static final String DEFAULT_FROM_ADDRESS = "noreply@mayanksoni.tech";


    public SendEmailProcessor(EmailTemplatingService emailTemplatingService, TemplateManagementService templateManagementService, JavaMailSenderFactory javaMailSenderFactory, ConfigurationSetManagementService configurationSetManagementService) {
        this.emailTemplatingService = emailTemplatingService;
        this.templateManagementService = templateManagementService;
        this.javaMailSenderFactory = javaMailSenderFactory;
        this.configurationSetManagementService = configurationSetManagementService;
    }

    public void processSendEmail(SendEmailDTO sendEmailDTO) {
        try{
            validateSendEmailPayload(sendEmailDTO);
            if(sendEmailDTO.content() != null){
                log.debug("Sending Email using platform sender");
                JavaMailSender platformJavaMailSender = javaMailSenderFactory.getMailSender(Optional.empty());
                this.prepareEmailPayload(
                        sendEmailDTO.destination(),
                        DEFAULT_FROM_ADDRESS,
                        sendEmailDTO.content(),
                        sendEmailDTO.context(),
                        platformJavaMailSender
                );
            }else if(sendEmailDTO.templateId() != null){
                try{
                    log.debug("Fetching Template From repository and processing");
                    Template fetchedTemplate = this.templateManagementService.getTemplateById(sendEmailDTO.templateId());
                    ConfigurationSetModel configurationSetModel = this.configurationSetManagementService.getConfigurationSetById(sendEmailDTO.configurationSetId());
                    if(!Objects.equals(fetchedTemplate.tenantId().toString(), configurationSetModel.tenantId().toString())){
                        log.error("Tenant ID mismatch for Template ID: {} and ConfigurationSet ID: {}", sendEmailDTO.templateId(), sendEmailDTO.configurationSetId());
                        throw new HeimdallBifrostBadDataException("Template does not belong to the same tenant as the configuration set");
                    }
                    this.prepareEmailPayload(sendEmailDTO.destination(),configurationSetModel.smtpProperties().fromEmailAddress(), fetchedTemplate.content(), sendEmailDTO.context(), javaMailSenderFactory.getMailSender(Optional.ofNullable(configurationSetModel.smtpProperties())));
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

    /**
     * This method connects to the mail server and sends the email using the provided JavaMailSender.
     *
     * @param mailSender The JavaMailSender instance to use for sending the email.
     * @param to The recipient email addresses.
     * @param subject The subject of the email.
     * @param htmlBody The HTML body of the email.
     * @param plainTextBody The plain text body of the email.
     * @param fromEmailAddress The sender's email address.
     */
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
    /**
     * This method validates the SendEmailDTO object to ensure it contains the required fields.
     *
     * @param sendEmailDTO The SendEmailDTO object to validate.
     */
    private void validateSendEmailPayload(SendEmailDTO sendEmailDTO) {
        assertNotNull(sendEmailDTO, "SendEmailDTO cannot be null");
        assertNotNull(sendEmailDTO.destination(), "Destination cannot be null");
        assertNotNull(sendEmailDTO.context(), "Context cannot be null");

    }
    /**
     * This method prepares the email payload by processing the template and context.
     *
     * @param destination The email destination.
     * @param fromEmailAddress The Sender's email address
     * @param content The fetched template to use for the email.
     * @param emailContext The email context containing user and organization information.
     * @param identifiedMailSender The JavaMailSender instance to use for sending the email.
     * @throws IOException If an error occurs while processing the template.
     */
    private void prepareEmailPayload(EmailDestination destination, String fromEmailAddress, EmailContent content, EmailContext emailContext, JavaMailSender identifiedMailSender) throws IOException {
        Map<String, Object> context = convertContextToMap(emailContext);
        String processedEmailSubject = emailTemplatingService.processString(content.subject(),context );
        String processedEmailHtmlBody = emailTemplatingService.processString(content.htmlBodyContent(),context);
        String processedEmailPlainTextBody = emailTemplatingService.processString(content.plainTextContent(),context);
        this.connectAndSendEmail(
                identifiedMailSender,
                destination.toDestinationEmailAddress().stream().map(String::new).toArray(String[]::new),
                processedEmailSubject,
                processedEmailHtmlBody,
                processedEmailPlainTextBody,
                fromEmailAddress
        );
    }
    private Map<String, Object> convertContextToMap(EmailContext context) {
        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("user", context.user());
        contextMap.put("organization", context.organizationContext());
        contextMap.put("variables", context.variables());
        return contextMap;
    }
}

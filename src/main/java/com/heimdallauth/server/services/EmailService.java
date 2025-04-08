package com.heimdallauth.server.services;

import com.heimdallauth.server.dto.bifrost.SendEmailDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {
    private final JavaMailSenderFactory javaMailSenderFactory;

    public EmailService(JavaMailSenderFactory javaMailSenderFactory) {
        this.javaMailSenderFactory = javaMailSenderFactory;
    }

    @SneakyThrows
    public void sendEmail(SendEmailDTO sendEmailDTO) throws MessagingException {
        JavaMailSender mailSender = javaMailSenderFactory.getMailSender(sendEmailDTO.configurationSetId());
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage,true);
        messageHelper.setTo(sendEmailDTO.destination().toDestinationEmailAddress().stream().map(String::new).toArray(String[]::new));
        messageHelper.setSubject(sendEmailDTO.content().subject());
        messageHelper.setText(sendEmailDTO.content().plainTextContent(), sendEmailDTO.content().htmlBodyContent());
        messageHelper.setFrom("noreply@mayanksoni.tech");
        mailSender.send(mimeMessage);
        log.debug("Email sent successfully");
    }
}

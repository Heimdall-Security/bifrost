package com.heimdallauth.server.controllers.v1.management;

import com.heimdallauth.server.dto.bifrost.SendEmailDTO;
import com.heimdallauth.server.services.EmailService;
import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/email")
public class MailController {
    private final EmailService emailService;

    public MailController( EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    @PreAuthorize("hasRole(@heimdallBifrostRoleConfiguration.ROLE_SEND_EMAIL)")
    public ResponseEntity<Void> sendEmailWithConfiguration(@RequestBody SendEmailDTO sendEmailDTO) throws MessagingException {
        this.emailService.sendEmail(sendEmailDTO);
        return ResponseEntity.ok().build();
    }
}

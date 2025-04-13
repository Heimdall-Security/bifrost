package com.heimdallauth.server.controllers.v1.management;

import com.heimdallauth.server.dto.bifrost.SendEmailDTO;
import com.heimdallauth.server.services.SendEmailProcessor;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/email")
@Tag(name = "Email API", description = "Email API for sending emails")
public class MailController {
    private final SendEmailProcessor sendEmailProcessor;

    public MailController(SendEmailProcessor sendEmailProcessor) {
        this.sendEmailProcessor = sendEmailProcessor;
    }

    @PostMapping("/send")
    @PreAuthorize("hasRole(@heimdallBifrostRoleConfiguration.ROLE_SEND_EMAIL) or hasAuthority(@heimdallBifrostRoleConfiguration.SCOPE_SEND_EMAIL)")
    public ResponseEntity<Void> sendEmailWithConfiguration(@RequestBody SendEmailDTO sendEmailDTO) {
        this.sendEmailProcessor.processSendEmail(sendEmailDTO);
        return ResponseEntity.ok().build();
    }
}

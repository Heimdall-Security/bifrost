package com.heimdallauth.server.controllers.v1.management;

import com.heimdallauth.server.services.JavaMailSenderFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/email")
public class MailController {
    private final JavaMailSenderFactory javaMailSenderFactory;

    public MailController(JavaMailSenderFactory javaMailSenderFactory) {
        this.javaMailSenderFactory = javaMailSenderFactory;
    }

    @PostMapping("/send")
    public ResponseEntity<Void> sendEmailWithConfiguration(@RequestParam(value = "configurationId", required = false) UUID configurationId){
        this.javaMailSenderFactory.getMailSender(configurationId);
        return ResponseEntity.ok().build();
    }
}

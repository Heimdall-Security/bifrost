package com.heimdallauth.server.models;

import lombok.*;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ConfigurationSetModel {
    private String configurationSetId;
    private String configurationSetName;
    private String configurationSetDescription;
    private List<EmailTemplateModel> templates;
    private SmtpProperties smtpProperties;
}

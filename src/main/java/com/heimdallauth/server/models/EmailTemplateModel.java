package com.heimdallauth.server.models;

import com.heimdallauth.server.utils.HeimdallMetadata;
import lombok.*;
import org.checkerframework.checker.units.qual.N;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EmailTemplateModel {
    private String templateName;
    private String subject;
    private List<HeimdallMetadata> metadata;
    private String richBodyContent;
    private String textBodyContent;
}

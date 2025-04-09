package com.heimdallauth.server.services;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
public class EmailTemplatingService {
    private final Handlebars handlebars = new Handlebars();

    /**
     * Process a templated string using Handlebars.
     *
     * @param templatedString The string to be processed.
     * @param variableMap     The map of variables to be used in the template.
     * @return The processed string.
     * @throws IOException If there is an error during processing.
     */
    public String processString(String templatedString, Map<String, Object> variableMap) throws IOException {
        Template inlineTemplate = handlebars.compileInline(templatedString);
        return inlineTemplate.apply(variableMap);
    }
}

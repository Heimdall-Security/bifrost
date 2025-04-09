package com.heimdallauth.server.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EmailTemplatingServiceTest {
    final EmailTemplatingService emailTemplatingService = new EmailTemplatingService();

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void processString_simpleString() {
        String templateString = "Hello, {{name}}!";
        String expectedOutput = "Hello, John!";
        Map<String, Object> variableMap = Map.of("name", "John");
        try {
            String result = emailTemplatingService.processString(templateString, variableMap);
            assertEquals(expectedOutput, result);
        } catch (Exception e) {
            fail("Exception should not be thrown for simple string processing");
        }
    }
    @Test
    void processString_withNestedLevels(){
        String nestedLevels = "Hello, {{user.name}}! Your organization is {{organization.name}}.";
        String expectedOutput = "Hello, John! Your organization is Heimdall.";
        Map<String, Object> variableMap = Map.of("user", Map.of("name", "John"), "organization", Map.of("name", "Heimdall"));
        try {
            String result = emailTemplatingService.processString(nestedLevels, variableMap);
            assertEquals(expectedOutput, result);
        } catch (Exception e) {
            fail("Exception should not be thrown for nested levels processing");
        }
    }
}
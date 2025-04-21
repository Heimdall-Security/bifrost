package com.heimdallauth.server.exceptions;

import com.heimdallauth.server.constants.bifrost.SuppressionListEntryType;

import java.util.List;

public class SuppressedDestination extends RuntimeException {
    public final String ruleSetName;
    public final List<String> suppressedDestinations;

    public SuppressedDestination(String ruleSetName, List<String> suppressedDestinations) {
        this.ruleSetName = ruleSetName;
        this.suppressedDestinations = suppressedDestinations;
    }
}

package com.heimdallauth.server.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.heimdallauth.server.clients.GlobalSuppressionEntryClient;
import com.heimdallauth.server.constants.bifrost.SuppressionListEntryType;
import com.heimdallauth.server.exceptions.SuppressedDestination;
import com.heimdallauth.server.models.bifrost.SuppressionEntryModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EmailSuppressionProcessor {
    private final GlobalSuppressionEntryClient globalSuppressionEntryClient;
    private final Cache<String, List<SuppressionEntryModel>> cache;
    private final ObjectMapper objectMapper;
    private static final String CACHE_KEY = "globalSuppressionEntries";


    public EmailSuppressionProcessor(GlobalSuppressionEntryClient globalSuppressionEntryClient, ObjectMapper objectMapper) {
        this.globalSuppressionEntryClient = globalSuppressionEntryClient;
        this.objectMapper = objectMapper;
        this.cache = Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.HOURS).maximumSize(1).build();
    }

    /**
     * Checks for suppressed destinations in the global suppression entries and the configuration set.
     *
     * @param destinations List of email destinations to check for suppression.
     * @throws SuppressedDestination if any of the destinations are suppressed.
     */
    public Set<String> checkForSuppressions(List<String> destinations, List<SuppressionEntryModel> configurationSetSuppressionEntries) {
        final List<SuppressionEntryModel> globalSuppressionEntries = getGlobalSuppressionEntries();
        CompletableFuture<Set<String>> globalSuppressionCheckFuture = CompletableFuture.supplyAsync(() -> triggerSuppressionChecks(globalSuppressionEntries, destinations));
        CompletableFuture<Set<String>> configSuppressionCheckFuture = CompletableFuture.supplyAsync(() -> triggerSuppressionChecks(configurationSetSuppressionEntries, destinations));
        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(globalSuppressionCheckFuture, configSuppressionCheckFuture);
        try{
            combinedFuture.join();
            Set<String> globalSuppressedDestinations = globalSuppressionCheckFuture.get();
            Set<String> configSuppressedDestinations = configSuppressionCheckFuture.get();
            if (!globalSuppressedDestinations.isEmpty() || !configSuppressedDestinations.isEmpty()) {
                Set<String> suppressedDestinations = new HashSet<>();
                suppressedDestinations.addAll(globalSuppressedDestinations);
                suppressedDestinations.addAll(configSuppressedDestinations);
                return suppressedDestinations;
            } else {
                log.info("No suppressed destinations found");
                return Collections.emptySet();
            }
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error while checking for suppressions", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks for suppressed destinations in the provided suppression list entries.
     *
     * @param suppressionListEntries List of suppression entries to check against.
     * @param destinations           List of email destinations to check for suppression.
     * @return Set of suppressed destinations.
     */
    private Set<String> triggerSuppressionChecks(List<SuppressionEntryModel> suppressionListEntries, List<String> destinations) {
        log.info("Trigger suppression checks");
        Set<String> suppressedDomainsForList = suppressionListEntries.stream().filter(s -> s.suppressionListEntryType() == SuppressionListEntryType.EMAIL_DOMAIN).map(SuppressionEntryModel::value).collect(Collectors.toSet());
        Set<String> suppressedEmailsForList = suppressionListEntries.stream().filter(s -> s.suppressionListEntryType() == SuppressionListEntryType.EMAIL).map(SuppressionEntryModel::value).collect(Collectors.toSet());
        Set<String> suppressedDestinations = new HashSet<>();
        CompletableFuture<List<String>> suppressedDomainsFuture = CompletableFuture.supplyAsync(() -> checkDomainsForSuppression(suppressedDomainsForList, destinations));
        CompletableFuture<List<String>> suppressedEmailsFuture = CompletableFuture.supplyAsync(() -> checkEmailsForSuppression(suppressedEmailsForList, destinations));
        CompletableFuture.allOf(suppressedDomainsFuture, suppressedEmailsFuture).join();
        suppressedDomainsFuture.thenAccept(suppressedDestinations::addAll);
        suppressedEmailsFuture.thenAccept(suppressedDestinations::addAll);
        if (!suppressedDestinations.isEmpty()) {
            log.info("Suppressed destinations: {}", suppressedDestinations);
        }
        return suppressedDestinations;
    }

    /**
     * Checks for suppressed domains in the provided suppression list entries.
     *
     * @param suppressedDomains Set of suppressed domains to check against.
     * @param destinations      List of email destinations to check for suppression.
     * @return List of suppressed destinations.
     */
    private List<String> checkDomainsForSuppression(Set<String> suppressedDomains, List<String> destinations) {
        Set<String> matchedDestinationDomains = destinations.stream().map(String::toLowerCase).map(s -> s.substring(s.indexOf("@") + 1)).filter(suppressedDomains::contains).collect(Collectors.toSet());
        return destinations.stream().filter(s -> matchedDestinationDomains.contains(s.substring(s.indexOf("@") + 1))).toList();
    }

    /**
     * Checks for suppressed emails in the provided suppression list entries.
     *
     * @param suppressedEmails Set of suppressed emails to check against.
     * @param destinations     List of email destinations to check for suppression.
     * @return List of suppressed destinations.
     */
    private List<String> checkEmailsForSuppression(Set<String> suppressedEmails, List<String> destinations) {
        Set<String> matchedDestinations = destinations.stream().map(String::toLowerCase).filter(suppressedEmails::contains).collect(Collectors.toSet());
        return destinations.stream().filter(matchedDestinations::contains).toList();
    }

    /**
     * Retrieves global suppression entries from the cache or the client if not present in the cache.
     *
     * @return List of global suppression entries.
     */
    private List<SuppressionEntryModel> getGlobalSuppressionEntries() {
        return cache.get(CACHE_KEY, key -> {
            String response = globalSuppressionEntryClient.getGlobalSuppressionEntries();
            try {
                List<SuppressionEntryModel> suppressionEntryModels = objectMapper.readValue(response, objectMapper.getTypeFactory().constructCollectionType(List.class, SuppressionEntryModel.class));
                if (suppressionEntryModels == null || suppressionEntryModels.isEmpty()) {
                    log.warn("No global suppression entries found");
                    return Collections.emptyList();
                }
                return suppressionEntryModels;
            } catch (Exception e) {
                log.error("Error parsing global suppression entries: {}", e.getMessage());
                return List.of();
            }
        });
    }
}

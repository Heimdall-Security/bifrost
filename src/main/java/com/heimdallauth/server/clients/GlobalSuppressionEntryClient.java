package com.heimdallauth.server.clients;

import com.heimdallauth.server.models.bifrost.SuppressionEntryModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "global-suppression-entry-client", url = "${heimdall.bifrost.global-suppression-entry.url}")
public interface GlobalSuppressionEntryClient {
    @GetMapping(consumes = "application/json")
    String getGlobalSuppressionEntries();
}

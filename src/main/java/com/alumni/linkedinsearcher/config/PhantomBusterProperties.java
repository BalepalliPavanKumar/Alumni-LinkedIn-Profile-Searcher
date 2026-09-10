package com.alumni.linkedinsearcher.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "phantombuster")
public record PhantomBusterProperties(
        String apiKey,
        String agentId,
        String baseUrl,
        int resultLimit,
        Polling polling) {

    public record Polling(long intervalMillis, int maxAttempts) {
    }
}

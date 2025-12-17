package com.vodafoneziggo.assignment.order.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient configuration for the external ReqRes service.
 * Centralizes base URL, headers, timeout, and API key handling.
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient reqResWebClient(
            @Value("${integration.reqres.base-url}") String baseUrl,
            @Value("${integration.reqres.api-key}") String apiKey,
            @Value("${integration.reqres.timeout-ms}") long timeoutMs
    ) {
        // Base WebClient setup with common headers
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT,"order-api-assignment");

        // Add API key only if configured (keeps local/dev simple)
        if (apiKey != null && !apiKey.isBlank()) {
            builder.defaultHeader("x-api-key", apiKey);
        }
        // Apply request timeout to avoid hanging external calls
        return builder
                .clientConnector(new org.springframework.http.client.reactive.ReactorClientHttpConnector(
                        reactor.netty.http.client.HttpClient.create()
                                .responseTimeout(Duration.ofMillis(timeoutMs))
                ))
                .build();
    }
}

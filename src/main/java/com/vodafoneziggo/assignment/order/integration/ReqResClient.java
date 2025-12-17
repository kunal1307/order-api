package com.vodafoneziggo.assignment.order.integration;

import java.time.Duration;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class ReqResClient {

    // WebClient configured specifically for ReqRes integration
    private final WebClient webClient;

    public ReqResClient(WebClient reqResWebClient) {
        this.webClient = reqResWebClient;
    }


    /**
     * Searches ReqRes users API page by page to find a user by email.
     * Returns basic user identity if found, otherwise empty.
     */
    public Optional<UserIdentity> findUserByEmail(String email) {

        // Fail fast: no point calling ReqRes for garbage input
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        // Starting page for ReqRes pagination
        int page = 1;

        while (true) {
            final int currentPage = page;
            ReqResUserResponse response;
            try {
                // Call ReqRes users endpoint with pagination
                response = webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/users")
                                .queryParam("page", currentPage)
                                .build()
                        )
                        .retrieve()
                        .bodyToMono(ReqResUserResponse.class)
                        .block();
            } catch (WebClientResponseException ex) {
                // Explicit handling for HTTP-level failure
                throw new ReqResUnavailableException("ReqRes HTTP error: " + ex.getStatusCode());
            } catch (Exception ex) {
                // Covers timeouts, connection issues, etc.
                throw new ReqResUnavailableException("ReqRes unavailable: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
            }
            // Defensive check in case API returns unexpected payload
            if (response == null || response.getData() == null) {
                return Optional.empty();
            }

            // Try to find a matching user by email (case-insensitive)
            Optional<UserIdentity> match = response.getData().stream()
                    .filter(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(email))
                    .findFirst()
                    .map(u -> new UserIdentity(u.getFirstName(), u.getLastName()));

            if (match.isPresent()) {
                return match;
            }
            // Be defensive: treat 0/negative as "1 page"
            int totalPages = Math.max(1, response.getTotalPages());

            // Stop when last page is reached
            if (page >= totalPages) {
                return Optional.empty();
            }
            // Move to next page
            page++;
        }
    }

    /**
     * Minimal user representation used by the order domain.
     */
    public record UserIdentity(String firstName, String lastName) {}

    /**
     * Raised when ReqRes is unreachable or returns unexpected errors.
     */
    public static class ReqResUnavailableException extends RuntimeException {
        public ReqResUnavailableException(String message) {
            super(message);
        }
    }
}

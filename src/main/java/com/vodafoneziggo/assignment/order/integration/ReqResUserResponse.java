package com.vodafoneziggo.assignment.order.integration;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO representing the response returned by the ReqRes /users API.
 * This class is intentionally kept separate from domain models.
 */
public class ReqResUserResponse {

    // Current page number returned by ReqRes
    private int page;

    // Total number of pages available in ReqRes response
    @JsonProperty("total_pages")
    private int totalPages;

    // List of users returned for the current page
    private List<User> data;

    public int getPage() {
        return page;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public List<User> getData() {
        return data;
    }

    /**
     * Nested DTO representing a single user from ReqRes.
     * Only fields relevant to this application are mapped.
     */
    public static class User {

        // User email used for identity matching
        private String email;

        // Mapped from ReqRes snake_case field
        @JsonProperty("first_name")
        private String firstName;

        // Mapped from ReqRes snake_case field
        @JsonProperty("last_name")
        private String lastName;

        public String getEmail() {
            return email;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }
    }
}

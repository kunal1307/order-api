package com.vodafoneziggo.assignment.order;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Full integration test:
 * - Real Spring context
 * - Real PostgreSQL via Testcontainers
 * - External API mocked via WireMock
 */
@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.flyway.enabled=false"
        }
)
class OrderApiIntegrationTest {

    /**
     * Ephemeral PostgreSQL instance for tests.
     * Container lifecycle is managed by Testcontainers.
     */
    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17")
                    .withDatabaseName("order_api")
                    .withUsername("postgres")
                    .withPassword("postgres");

    /**
     * WireMock simulates the external ReqRes API.
     */
    static WireMockServer wireMock;

    /**
     * HTTP client used to call the running application.
     */
    private WebTestClient webTestClient;

    /**
     * Dynamically inject container and mock configuration
     * into Spring before the context is created.
     */
    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        // Useful when debugging test container startup issues
        System.out.println("TESTCONTAINER JDBC = " + postgres.getJdbcUrl());

        // 1. Start PostgreSQL container EARLY
        // Explicit container startup to guarantee availability
        postgres.start();

        // 2. Start WireMock EARLY
        // Start WireMock once on a random free port
        if (wireMock == null) {
            wireMock = new WireMockServer(0); // random free port
            wireMock.start();
            configureFor("localhost", wireMock.port());
        }

        // 3. Datasource (used by JPA)
        // JPA datasource configuration
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // 4. Flyway (EXPLICIT – no ambiguity)
        // Flyway configuration bound to the same container
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");

        // 5. External ReqRes stub
        // External ReqRes API stub configuration
        registry.add("integration.reqres.base-url", wireMock::baseUrl);
        registry.add("integration.reqres.api-key", () -> "");
        registry.add("integration.reqres.timeout-ms", () -> "3000");
    }

    /**
     * Random port injected by Spring Boot.
     */
    @LocalServerPort
    int port;

    /**
     * Create a WebTestClient bound to the running server
     * before each test.
     */

    @BeforeEach
    void setupClient() {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    /**
     * Happy-path test:
     * - External user exists
     * - Order is successfully created
     */
    @Test
    void createOrder_validEmail_returns201() {
        stubFor(get(urlPathEqualTo("/users"))
                .withQueryParam("page", equalTo("1"))
                .willReturn(okJson("""
          {
            "page": 1,
            "total_pages": 1,
            "data": [
              { "email": "george.bluth@reqres.in", "first_name": "George", "last_name": "Bluth" }
            ]
          }
        """)));

        webTestClient.post()
                .uri("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
          { "productId": "TV-1", "email": "george.bluth@reqres.in" }
        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.orderId").exists();
    }

    /**
     * Validation test:
     * - External API returns no matching user
     * - Service responds with 422
     */
    @Test
    void createOrder_emailNotFound_returns422() {
        stubFor(get(urlPathEqualTo("/users"))
                .withQueryParam("page", equalTo("1"))
                .willReturn(okJson("""
          {
            "page": 1,
            "total_pages": 1,
            "data": []
          }
        """)));

        webTestClient.post()
                .uri("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
          { "productId": "TV-2", "email": "missing@example.com" }
        """)
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody()
                .jsonPath("$.code").isEqualTo("EMAIL_NOT_FOUND");
    }

    /**
     * Idempotency test:
     * - First request succeeds
     * - Second request violates unique constraint
     */
    @Test
    void createOrder_duplicate_returns409() {
        stubFor(get(urlPathEqualTo("/users"))
                .withQueryParam("page", equalTo("1"))
                .willReturn(okJson("""
          {
            "page": 1,
            "total_pages": 1,
            "data": [
              { "email": "george.bluth@reqres.in", "first_name": "George", "last_name": "Bluth" }
            ]
          }
        """)));

        webTestClient.post()
                .uri("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
          { "productId": "TV-9", "email": "george.bluth@reqres.in" }
        """)
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post()
                .uri("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
          { "productId": "TV-9", "email": "george.bluth@reqres.in" }
        """)
                .exchange()
                .expectStatus().isEqualTo(409);
    }
}

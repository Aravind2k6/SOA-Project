
package com.klu.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureWebTestClient
class ApiGatewayJwtIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    // Test 1: Reject request without JWT
    @Test
    void shouldRejectMissingJwt() {

        webTestClient.get()
                .uri("/api/meters")
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    // Test 2: Reject malformed JWT
    @Test
    void shouldRejectMalformedJwt() {

        webTestClient.get()
                .uri("/api/meters")
                .headers(headers ->
                        headers.setBearerAuth("abc.def.ghi"))
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    // Test 3: Reject a token with a tampered signature
    @Test
    void shouldRejectTamperedJwt() {

        String tamperedToken =
                "eyJhbGciOiJIUzI1NiJ9."
              + "eyJzdWIiOiJBcmF2aW5kIiwicm9sZSI6IlVTRVIifQ."
              + "invalid-signature";

        webTestClient.get()
                .uri("/api/meters")
                .headers(headers ->
                        headers.setBearerAuth(tamperedToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }
}

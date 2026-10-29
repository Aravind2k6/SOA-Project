
package com.klu.gateway;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServiceDiscoveryIntegrationTest {

    private static final String EUREKA_URL =
            "http://localhost:8761/eureka/apps";

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private HttpResponse<String> getRegistry() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(EUREKA_URL))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        return client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );
    }

    // Test 1: Eureka registry is accessible
    @Test
    void shouldAccessEurekaRegistry() throws Exception {
        HttpResponse<String> response = getRegistry();

        assertEquals(200, response.statusCode());
    }

    // Test 2: All expected applications are registered
    @Test
    void shouldFindAllRegisteredApplications() throws Exception {
        HttpResponse<String> response = getRegistry();

        assertEquals(200, response.statusCode());

        String registry = response.body();

        List<String> expectedServices = List.of(
                "ALERT-SERVICE-41",
                "API-GATEWAY-41",
                "AUTH-SERVICE-41",
                "METER-SERVICE-41",
                "USAGE-SERVICE-41"
        );

        for (String service : expectedServices) {
            assertTrue(
                    registry.contains(service),
                    "Service not found in Eureka: " + service
            );
        }
    }
}

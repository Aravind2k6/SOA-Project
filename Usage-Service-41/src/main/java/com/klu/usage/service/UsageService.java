
package com.klu.usage.service;

import com.klu.usage.entity.Usage;
import com.klu.usage.repository.UsageRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UsageService {

    private static final Logger log =
            LoggerFactory.getLogger(UsageService.class);

    private final UsageRepository usageRepository;
    private final RestTemplate restTemplate;

    public UsageService(
            UsageRepository usageRepository,
            RestTemplate restTemplate) {

        this.usageRepository = usageRepository;
        this.restTemplate = restTemplate;
    }

    // Add usage and automatically check for anomalies
    public Usage addUsage(
            Usage usage,
            String authorization) {

        if (usage.getTimestamp() == null) {
            usage.setTimestamp(LocalDateTime.now());
        }

        // Save the reading first
        Usage savedUsage = usageRepository.save(usage);

        // The JWT is needed for the protected Alert Service
        if (authorization == null || authorization.isBlank()) {
            log.warn(
                    "Usage {} saved, but no Authorization header was received",
                    savedUsage.getUsageId()
            );

            return savedUsage;
        }

        String alertUrl =
                "http://ALERT-SERVICE-41/api/alerts/check"
                + "?meterId={meterId}"
                + "&consumption={consumption}";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(
                    HttpHeaders.AUTHORIZATION,
                    authorization
            );

            HttpEntity<Void> request =
                    new HttpEntity<>(headers);

            // Alert Service returns a JSON object, not a Boolean
            restTemplate.exchange(
                    alertUrl,
                    HttpMethod.POST,
                    request,
                    Map.class,
                    savedUsage.getMeterId(),
                    savedUsage.getConsumptionUnits()
            );

            log.info(
                    "Threshold check completed for meter {}",
                    savedUsage.getMeterId()
            );

        } catch (RestClientException e) {
            // Preserve the saved reading if alert checking fails
            log.error(
                    "Usage {} saved, but Alert Service check failed: {}",
                    savedUsage.getUsageId(),
                    e.getMessage()
            );
        }

        return savedUsage;
    }

    // Get all usage records
    public List<Usage> getAllUsage() {
        return usageRepository.findAll();
    }

    // Get usage by ID
    public Optional<Usage> getUsageById(Long usageId) {
        return usageRepository.findById(usageId);
    }

    // Get usage by meter ID
    public List<Usage> getUsageByMeterId(Long meterId) {
        return usageRepository.findByMeterId(meterId);
    }

    // Update usage
    public Usage updateUsage(Long usageId, Usage usage) {

        Usage existingUsage =
                usageRepository.findById(usageId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Usage record not found"
                                )
                        );

        existingUsage.setMeterId(usage.getMeterId());
        existingUsage.setConsumptionUnits(
                usage.getConsumptionUnits()
        );

        if (usage.getTimestamp() != null) {
            existingUsage.setTimestamp(usage.getTimestamp());
        }

        return usageRepository.save(existingUsage);
    }

    // Delete usage
    public void deleteUsage(Long usageId) {

        if (!usageRepository.existsById(usageId)) {
            throw new RuntimeException(
                    "Usage record not found"
            );
        }

        usageRepository.deleteById(usageId);
    }
}

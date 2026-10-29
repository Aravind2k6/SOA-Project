
package com.klu.usage.service;

import com.klu.usage.entity.Usage;
import com.klu.usage.repository.UsageRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsageServiceTest {

    @Mock
    private UsageRepository usageRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private UsageService usageService;

    @Test
    void shouldSaveUsageAndCheckAlert() {

        Usage usage = new Usage();
        usage.setMeterId(1L);
        usage.setConsumptionUnits(18.7);
        usage.setTimestamp(
                LocalDateTime.parse("2026-10-29T11:00:00")
        );

        when(usageRepository.save(usage))
                .thenReturn(usage);

        Usage saved = usageService.addUsage(
                usage, "Bearer test-token"
        );

        assertNotNull(saved);
        assertEquals(1L, saved.getMeterId());
        assertEquals(18.7, saved.getConsumptionUnits());

        verify(usageRepository).save(usage);

        verify(restTemplate).exchange(
                eq("http://ALERT-SERVICE-41/api/alerts/check"
                        + "?meterId={meterId}"
                        + "&consumption={consumption}"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class),
                eq(1L),
                eq(18.7)
        );
    }

    @Test
    void shouldSetTimestampWhenMissing() {

        Usage usage = new Usage();
        usage.setMeterId(1L);
        usage.setConsumptionUnits(12.5);

        when(usageRepository.save(usage))
                .thenReturn(usage);

        usageService.addUsage(usage, null);

        assertNotNull(usage.getTimestamp());
        verify(usageRepository).save(usage);
        verifyNoInteractions(restTemplate);
    }

    @Test
    void shouldSaveUsageWithoutCallingAlertWhenJwtMissing() {

        Usage usage = new Usage();
        usage.setMeterId(1L);
        usage.setConsumptionUnits(12.5);
        usage.setTimestamp(LocalDateTime.now());

        when(usageRepository.save(usage))
                .thenReturn(usage);

        Usage saved = usageService.addUsage(usage, null);

        assertNotNull(saved);
        assertEquals(1L, saved.getMeterId());

        verify(usageRepository).save(usage);
        verifyNoInteractions(restTemplate);
    }

    @Test
    void shouldReturnAllUsageRecords() {

        Usage first = new Usage();
        first.setMeterId(1L);
        first.setConsumptionUnits(12.5);

        Usage second = new Usage();
        second.setMeterId(1L);
        second.setConsumptionUnits(18.7);

        when(usageRepository.findAll())
                .thenReturn(List.of(first, second));

        List<Usage> result = usageService.getAllUsage();

        assertEquals(2, result.size());
        verify(usageRepository).findAll();
    }
}

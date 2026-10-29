
package com.klu.alert.service;

import com.klu.alert.entity.Alert;
import com.klu.alert.repository.AlertRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private AlertService alertService;

    private Alert createActiveAlert() {
        Alert alert = new Alert();
        alert.setMeterId(1L);
        alert.setThresholdValue(15.0);
        alert.setAlertStatus("ACTIVE");
        return alert;
    }

    @Test
    void consumptionBelowThresholdDoesNotTriggerAlert() {

        Alert alert = createActiveAlert();

        when(alertRepository.findByMeterId(1L))
                .thenReturn(List.of(alert));

        boolean result =
                alertService.checkThreshold(1L, 12.5);

        assertFalse(result);
        assertEquals("ACTIVE", alert.getAlertStatus());

        verify(alertRepository, never())
                .save(any(Alert.class));
    }

    @Test
    void consumptionAtThresholdTriggersAlert() {

        Alert alert = createActiveAlert();

        when(alertRepository.findByMeterId(1L))
                .thenReturn(List.of(alert));

        boolean result =
                alertService.checkThreshold(1L, 15.0);

        assertTrue(result);
        assertEquals("TRIGGERED", alert.getAlertStatus());

        verify(alertRepository).save(alert);
    }

    @Test
    void consumptionAboveThresholdTriggersAlert() {

        Alert alert = createActiveAlert();

        when(alertRepository.findByMeterId(1L))
                .thenReturn(List.of(alert));

        boolean result =
                alertService.checkThreshold(1L, 18.7);

        assertTrue(result);
        assertEquals("TRIGGERED", alert.getAlertStatus());

        verify(alertRepository).save(alert);
    }

    @Test
    void noAlertsReturnsFalse() {

        when(alertRepository.findByMeterId(1L))
                .thenReturn(Collections.emptyList());

        boolean result =
                alertService.checkThreshold(1L, 25.0);

        assertFalse(result);

        verify(alertRepository, never())
                .save(any(Alert.class));
    }

    @Test
    void inactiveAlertIsNotTriggered() {

        Alert alert = createActiveAlert();
        alert.setAlertStatus("RESOLVED");

        when(alertRepository.findByMeterId(1L))
                .thenReturn(List.of(alert));

        boolean result =
                alertService.checkThreshold(1L, 25.0);

        assertFalse(result);
        assertEquals("RESOLVED", alert.getAlertStatus());

        verify(alertRepository, never())
                .save(any(Alert.class));
    }
}

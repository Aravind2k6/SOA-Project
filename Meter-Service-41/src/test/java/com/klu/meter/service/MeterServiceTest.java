
package com.klu.meter.service;

import com.klu.meter.entity.Meter;
import com.klu.meter.repository.MeterRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeterServiceTest {

    @Mock
    private MeterRepository meterRepository;

    @InjectMocks
    private MeterService meterService;

    private Meter createMeter() {
        Meter meter = new Meter();
        meter.setMeterId(1L);
        meter.setUserId(1L);
        meter.setLocation("Building-A");
        meter.setStatus("ACTIVE");
        return meter;
    }

    @Test
    void shouldCreateMeter() {
        Meter meter = createMeter();

        when(meterRepository.save(any(Meter.class)))
                .thenReturn(meter);

        Meter result = meterService.createMeter(meter);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("Building-A", result.getLocation());
        assertEquals("ACTIVE", result.getStatus());

        verify(meterRepository).save(any(Meter.class));
    }

    @Test
    void shouldReturnAllMeters() {
        List<Meter> meters = Arrays.asList(
                createMeter(),
                new Meter(2L, 2L, "Building-B", "ACTIVE")
        );

        when(meterRepository.findAll()).thenReturn(meters);

        List<Meter> result = meterService.getAllMeters();

        assertEquals(2, result.size());
        verify(meterRepository).findAll();
    }

    @Test
    void shouldFindMeterById() {
        Meter meter = createMeter();

        when(meterRepository.findById(1L))
                .thenReturn(Optional.of(meter));

        Optional<Meter> result =
                meterService.getMeterById(1L);

        assertTrue(result.isPresent());
        assertEquals("Building-A", result.get().getLocation());
    }

    @Test
    void shouldReturnEmptyWhenMeterDoesNotExist() {
        when(meterRepository.findById(99L))
                .thenReturn(Optional.empty());

        Optional<Meter> result =
                meterService.getMeterById(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldDeleteExistingMeter() {
        when(meterRepository.existsById(1L))
                .thenReturn(true);

        meterService.deleteMeter(1L);

        verify(meterRepository).deleteById(1L);
    }
}

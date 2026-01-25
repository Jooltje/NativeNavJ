package com.nativenavj.control;

import com.nativenavj.adapter.Connector;
import com.nativenavj.adapter.MockClock;
import com.nativenavj.domain.Memory;
import com.nativenavj.domain.State;
import com.nativenavj.simconnect.TelemetryData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SensorTest {
    private Connector connector;
    private Memory memory;
    private MockClock clock;
    private Sensor sensor;

    @BeforeEach
    void setUp() {
        connector = mock(Connector.class);
        memory = new Memory();
        clock = new MockClock();
        sensor = new Sensor(connector, memory, clock);
    }

    @Test
    void testSensorUpdatesMemory() {
        // Prepare mock telemetry
        TelemetryData telemetry = new TelemetryData(
                10.0, 20.0, 5000.0, 100.0, 180.0, 5.0, 2.0);
        when(connector.getLatestTelemetry()).thenReturn(telemetry);
        when(connector.isReady()).thenReturn(true);

        // Execute one step
        sensor.step();

        // Verify Memory was updated
        State state = memory.getState();
        assertEquals(10.0, state.latitude());
        assertEquals(20.0, state.longitude());
        assertEquals(5000.0, state.altitude());
        assertEquals(100.0, state.speed());
        assertEquals(180.0, state.heading());
        assertEquals(5.0, state.roll());
        assertEquals(2.0, state.pitch());
    }

    @Test
    void testSensorHandlesMissingTelemetry() {
        when(connector.getLatestTelemetry()).thenReturn(null);

        State oldState = memory.getState();
        sensor.step();

        // Memory should not be updated with null/invalid data if not available
        assertEquals(oldState, memory.getState());
    }
}

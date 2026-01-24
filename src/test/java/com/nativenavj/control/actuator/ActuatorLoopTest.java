package com.nativenavj.control.actuator;

import com.nativenavj.control.core.ControlFrame;
import com.nativenavj.control.core.FlightGoal;
import com.nativenavj.control.core.FlightTelemetry;
import com.nativenavj.simconnect.SimConnectService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

public class ActuatorLoopTest {

    @Test
    public void testActuationCallsSimService() {
        SimConnectService mockService = Mockito.mock(SimConnectService.class);
        AtomicReference<FlightGoal> goalRef = new AtomicReference<>(new FlightGoal(true, 5000, 180, 100));
        AtomicReference<ControlFrame> controlRef = new AtomicReference<>(new ControlFrame(5, 10, 0.5));
        AtomicReference<FlightTelemetry> telemetryRef = new AtomicReference<>(
                new FlightTelemetry(5000, 100, 0, 0, 180, 0, System.nanoTime()));

        ActuatorLoop loop = new ActuatorLoop(100, goalRef, controlRef, telemetryRef, mockService);
        loop.step();

        // Verify that actuateSurfaces was called.
        // We don't check exact PID values here (math is tested in GenericPIDTest),
        // but we ensure the pipeline is connected.
        verify(mockService).actuateSurfaces(anyDouble(), anyDouble(), eq(0.0), eq(0.5));
    }

    @Test
    public void testNoActuationWhenSystemOff() {
        SimConnectService mockService = Mockito.mock(SimConnectService.class);
        // System is OFF by default
        AtomicReference<FlightGoal> goalRef = new AtomicReference<>(new FlightGoal(false, 5000, 180, 100));
        AtomicReference<ControlFrame> controlRef = new AtomicReference<>(new ControlFrame(5, 10, 0.5));
        AtomicReference<FlightTelemetry> telemetryRef = new AtomicReference<>(
                new FlightTelemetry(5000, 100, 0, 0, 180, 0, System.nanoTime()));

        ActuatorLoop loop = new ActuatorLoop(100, goalRef, controlRef, telemetryRef, mockService);
        loop.step();

        // Verify that actuateSurfaces was NEVER called
        Mockito.verify(mockService, Mockito.never()).actuateSurfaces(anyDouble(), anyDouble(), anyDouble(),
                anyDouble());
    }
}

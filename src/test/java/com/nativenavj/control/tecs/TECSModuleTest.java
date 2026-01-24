package com.nativenavj.control.tecs;

import com.nativenavj.control.core.ControlFrame;
import com.nativenavj.control.core.FlightGoal;
import com.nativenavj.control.core.FlightTelemetry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

public class TECSModuleTest {

    private AtomicReference<FlightGoal> goalRef;
    private AtomicReference<FlightTelemetry> telemetryRef;
    private AtomicReference<ControlFrame> controlRef;
    private TECSModule tecs;

    @BeforeEach
    public void setup() {
        goalRef = new AtomicReference<>(new FlightGoal(true, 5000, 180, 100));
        telemetryRef = new AtomicReference<>(new FlightTelemetry(5000, 100, 0, 0, 180, 0, System.nanoTime()));
        controlRef = new AtomicReference<>(new ControlFrame(0, 0, 0));
        tecs = new TECSModule(20.0, goalRef, telemetryRef, controlRef);
    }

    @Test
    public void testStallProtection() {
        // Airspeed 30 kts is below MIN_STALL (e.g., 40 kts)
        telemetryRef.set(new FlightTelemetry(5000, 30, 0, 0, 180, 0, System.nanoTime()));

        tecs.step();

        ControlFrame result = controlRef.get();
        // Nose down to -10 deg and Max Throttle (1.0 or 100%)
        assertEquals(-10.0, result.pitchTargetDeg(), 0.01);
        assertEquals(1.0, result.throttlePercent(), 0.01);
    }

    @Test
    public void testEnergyBalancing() {
        // Altitude below target (4000 vs 5000), Speed at target (100)
        // Energy Error (Alt_Err + Spd_Err) -> Should increase Throttle
        // Dist Error (Alt_Err - Spd_Err) -> Should increase Pitch
        telemetryRef.set(new FlightTelemetry(4000, 100, 0, 0, 180, 0, System.nanoTime()));

        tecs.step();

        ControlFrame result = controlRef.get();
        assertTrue(result.pitchTargetDeg() > 0, "Pitch should be positive to gain altitude");
        assertTrue(result.throttlePercent() > 0, "Throttle should be positive to gain energy");
    }
}

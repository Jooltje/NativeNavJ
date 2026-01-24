package com.nativenavj.control.parser;

import com.nativenavj.control.core.FlightGoal;
import com.nativenavj.control.core.FlightTelemetry;
import com.nativenavj.control.core.SystemStatus;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

public class CommandParserTest {

    @Test
    public void testParseAlt() {
        AtomicReference<FlightGoal> goalRef = new AtomicReference<>(FlightGoal.initial());
        AtomicReference<FlightTelemetry> telemetryRef = new AtomicReference<>(null);
        AtomicReference<SystemStatus> statusRef = new AtomicReference<>(new SystemStatus(false));
        CommandParser parser = new CommandParser(goalRef, telemetryRef, statusRef);

        parser.parse("ALT 5000");

        FlightGoal goal = goalRef.get();
        assertEquals(5000.0, goal.targetAltitudeFt(), 0.001);
        assertFalse(statusRef.get().active());
    }

    @Test
    public void testTelemetrySyncOnActivation() {
        AtomicReference<FlightGoal> goalRef = new AtomicReference<>(FlightGoal.initial());
        FlightTelemetry currentTele = new FlightTelemetry(3000, 120, 0, 0, 90, 0, System.nanoTime());
        AtomicReference<FlightTelemetry> telemetryRef = new AtomicReference<>(currentTele);
        AtomicReference<SystemStatus> statusRef = new AtomicReference<>(new SystemStatus(false));
        CommandParser parser = new CommandParser(goalRef, telemetryRef, statusRef);

        // When we turn it ON without having set targets
        parser.parse("ON");

        FlightGoal goal = goalRef.get();
        assertTrue(statusRef.get().active());
        // Should have synced from currentTele
        assertEquals(3000.0, goal.targetAltitudeFt(), 0.001);
        assertEquals(90.0, goal.targetHeadingDeg(), 0.001);
        assertEquals(120.0, goal.targetAirspeedKts(), 0.001);
    }

    @Test
    public void testParseOnOff() {
        AtomicReference<FlightGoal> goalRef = new AtomicReference<>(FlightGoal.initial());
        AtomicReference<FlightTelemetry> telemetryRef = new AtomicReference<>(null);
        AtomicReference<SystemStatus> statusRef = new AtomicReference<>(new SystemStatus(false));
        CommandParser parser = new CommandParser(goalRef, telemetryRef, statusRef);

        parser.parse("ON");
        assertTrue(statusRef.get().active());

        parser.parse("OFF");
        assertFalse(statusRef.get().active());
    }

    @Test
    public void testAtomicSwapPreservesOtherFields() {
        AtomicReference<FlightGoal> goalRef = new AtomicReference<>(new FlightGoal(5000, 180, 100));
        AtomicReference<FlightTelemetry> telemetryRef = new AtomicReference<>(null);
        AtomicReference<SystemStatus> statusRef = new AtomicReference<>(new SystemStatus(true));
        CommandParser parser = new CommandParser(goalRef, telemetryRef, statusRef);

        // Change only heading
        parser.parse("HDG 270");

        FlightGoal goal = goalRef.get();
        assertEquals(270.0, goal.targetHeadingDeg(), 0.001);
        assertEquals(5000.0, goal.targetAltitudeFt(), 0.001);
        assertEquals(100.0, goal.targetAirspeedKts(), 0.001);
        assertTrue(statusRef.get().active());
    }
}

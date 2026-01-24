package com.nativenavj.control.parser;

import com.nativenavj.control.core.FlightGoal;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

public class CommandParserTest {

    @Test
    public void testParseAlt() {
        AtomicReference<FlightGoal> goalRef = new AtomicReference<>(new FlightGoal(false, 0, 0, 0));
        CommandParser parser = new CommandParser(goalRef);

        parser.parse("ALT 5000");

        FlightGoal goal = goalRef.get();
        assertEquals(5000.0, goal.targetAltitudeFt(), 0.001);
        assertFalse(goal.systemActive());
    }

    @Test
    public void testParseOnOff() {
        AtomicReference<FlightGoal> goalRef = new AtomicReference<>(new FlightGoal(false, 0, 0, 0));
        CommandParser parser = new CommandParser(goalRef);

        parser.parse("ON");
        assertTrue(goalRef.get().systemActive());

        parser.parse("OFF");
        assertFalse(goalRef.get().systemActive());
    }

    @Test
    public void testAtomicSwapPreservesOtherFields() {
        AtomicReference<FlightGoal> goalRef = new AtomicReference<>(new FlightGoal(true, 5000, 180, 100));
        CommandParser parser = new CommandParser(goalRef);

        // Change only heading
        parser.parse("HDG 270");

        FlightGoal goal = goalRef.get();
        assertEquals(270.0, goal.targetHeadingDeg(), 0.001);
        assertEquals(5000.0, goal.targetAltitudeFt(), 0.001);
        assertEquals(100.0, goal.targetAirspeedKts(), 0.001);
        assertTrue(goal.systemActive());
    }
}

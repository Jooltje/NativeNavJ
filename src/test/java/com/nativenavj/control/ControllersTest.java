package com.nativenavj.control;

import com.nativenavj.port.Actuator;
import com.nativenavj.port.Clock;
import com.nativenavj.domain.Memory;
import com.nativenavj.domain.State;
import com.nativenavj.domain.Target;
import com.nativenavj.domain.Command;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class ControllersTest {
    private Actuator actuator;
    private Memory memory;
    private Clock clock;
    private Controllers controllers;

    @BeforeEach
    void setUp() {
        actuator = mock(Actuator.class);
        memory = new Memory();
        clock = mock(Clock.class);
        when(clock.nanoTime()).thenReturn(0L);
        controllers = new Controllers(actuator, memory, clock);
    }

    @Test
    void testControllersWriteToActuator() {
        // Set up Blackboard state
        State state = State.neutral();
        Target target = new Target(10.0, 5.0, 0.0, 0.8);
        memory.setState(state);
        memory.setTarget(target);

        when(actuator.isReady()).thenReturn(true);

        // Execute step
        controllers.step();

        // Verify Actuator received a command (we don't check exact values as they
        // depend on PID internal state)
        verify(actuator).write(any(Command.class));
    }
}

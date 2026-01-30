package com.nativenavj.control;

import com.nativenavj.adapter.Connector;
import com.nativenavj.domain.Memory;
import com.nativenavj.domain.Shell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class OrchestratorTest {

    private Memory memory;
    private Connector connector;
    private Computer computer;
    private Shell shell;

    @BeforeEach
    void setUp() {
        memory = new Memory();
        connector = mock(Connector.class);
        computer = mock(Computer.class);
        shell = mock(Shell.class);
        Runnable aiAssistant = mock(Runnable.class);
        new Orchestrator(memory, connector, computer, shell, aiAssistant);
    }

    @Test
    void shouldActivateOrchestratorByDefault() {
        assertTrue(memory.getLoop("ORCHESTRATOR").status(), "Orchestrator should be active");
    }

    @Test
    void shouldActivateShellByDefault() {
        assertTrue(memory.getLoop("SHELL").status(), "Shell should be active");
    }

    @Test
    void shouldDeactivateComputerByDefault() {
        assertFalse(memory.getLoop("COMPUTER").status(), "Computer should be inactive by default");
    }

    @Test
    void shouldDeactivatePitchControllerByDefault() {
        assertFalse(memory.getLoop("PITCH").status(), "Pitch controller should be inactive by default");
    }

    @Test
    void shouldDeactivateRollControllerByDefault() {
        assertFalse(memory.getLoop("ROLL").status(), "Roll controller should be inactive by default");
    }

    @Test
    void shouldDeactivateYawControllerByDefault() {
        assertFalse(memory.getLoop("YAW").status(), "Yaw controller should be inactive by default");
    }

    @Test
    void shouldDeactivateThrottleControllerByDefault() {
        assertFalse(memory.getLoop("THROTTLE").status(), "Throttle controller should be inactive by default");
    }
}

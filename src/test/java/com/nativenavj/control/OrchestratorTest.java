package com.nativenavj.control;

import com.nativenavj.adapter.Connector;
import com.nativenavj.domain.Memory;
import com.nativenavj.domain.Shell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
        new Orchestrator(memory, connector, computer, shell);
    }

    @Test
    void shouldHaveOnlyOrchestratorAndShellActiveByDefault() {
        // Essential services should be active
        assertTrue(memory.getLoop("ORC").active(), "Orchestrator should be active");
        assertTrue(memory.getLoop("SHL").active(), "Shell should be active");

        // Non-essential services should be inactive by default
        assertFalse(memory.getLoop("CPU").active(), "Computer should be inactive by default");
        assertFalse(memory.getLoop("PIT").active(), "Pitch controller should be inactive by default");
        assertFalse(memory.getLoop("ROL").active(), "Roll controller should be inactive by default");
        assertFalse(memory.getLoop("YAW").active(), "Yaw controller should be inactive by default");
        assertFalse(memory.getLoop("THR").active(), "Throttle controller should be inactive by default");
    }
}

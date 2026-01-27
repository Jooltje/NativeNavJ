package com.nativenavj.adapter;

import com.nativenavj.domain.Memory;
import com.nativenavj.domain.State;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConnectorTest {

    private Memory memory;
    private Connector connector;

    @BeforeEach
    void setUp() {
        memory = mock(Memory.class);
        // We can't easily unit test the native SimConnect parts without the DLL and a
        // simulator,
        // but we can verify the class can be instantiated and basic command queuing.
        // instantiation might fail if DLL is missing, so we wrap it.
    }

    @Test
    void testInitialization() {
        try {
            connector = new Connector(memory);
            assertNotNull(connector);
            connector.stop();
        } catch (UnsatisfiedLinkError e) {
            // Expected if DLL or Panama not available in test env
            System.err.println("Skipping testInitialization: " + e.getMessage());
        }
    }

    @Test
    void testCommandQueuing() {
        // This test verifies that calling set methods doesn't crash
        // and that they interact with the queue (indirectly checked by no crash)
        try {
            connector = new Connector(memory);
            connector.setElevator(0.5);
            connector.setAileron(-0.2);
            connector.setRudder(0.1);
            connector.setThrottle(0.8);
            connector.stop();
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Skipping testCommandQueuing: " + e.getMessage());
        }
    }
}

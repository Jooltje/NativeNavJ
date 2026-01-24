package com.nativenavj.adapter;

import com.nativenavj.domain.Command;
import com.nativenavj.port.Actuator;

import java.util.ArrayList;
import java.util.List;

/**
 * Mock actuator for testing.
 * Records all commands written for verification in tests.
 */
public class MockActuator implements Actuator {
    private final List<Command> commandHistory = new ArrayList<>();
    private Command lastCommand = Command.neutral();
    private boolean ready = true;

    /**
     * Sets the ready status.
     */
    public void setReady(boolean ready) {
        this.ready = ready;
    }

    /**
     * Gets the last command written.
     */
    public Command getLastCommand() {
        return lastCommand;
    }

    /**
     * Gets the full command history.
     */
    public List<Command> getCommandHistory() {
        return new ArrayList<>(commandHistory);
    }

    /**
     * Clears the command history.
     */
    public void clearHistory() {
        commandHistory.clear();
    }

    @Override
    public void write(Command command) {
        this.lastCommand = command;
        commandHistory.add(command);
    }

    @Override
    public boolean isReady() {
        return ready;
    }
}

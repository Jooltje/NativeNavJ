package com.nativenavj.domain;

import com.nativenavj.port.Objective;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Shared state for the whole application.
 * Thread-safe and logs changes at debug level.
 */
public class Memory {
    private static final Logger log = LoggerFactory.getLogger(Memory.class);

    private final AtomicReference<Goal> goal = new AtomicReference<>(Goal.defaultGoal());
    private final AtomicReference<State> state = new AtomicReference<>(State.neutral());
    private final AtomicReference<Target> target = new AtomicReference<>(Target.neutral());
    private final AtomicReference<Navigator> navigator = new AtomicReference<>(Navigator.inactive());
    private final AtomicReference<Assistant> assistant = new AtomicReference<>(Assistant.inactive());
    private final AtomicReference<Settings> settings = new AtomicReference<>(Settings.defaultSettings());

    public Goal getGoal() {
        return goal.get();
    }

    public void setGoal(Goal value) {
        goal.set(value);
        log.debug("{}", value);
    }

    public State getState() {
        return state.get();
    }

    public void setState(State value) {
        state.set(value);
        log.debug("{}", value);
    }

    public Target getTarget() {
        return target.get();
    }

    public void setTarget(Target value) {
        target.set(value);
        log.debug("{}", value);
    }

    public Navigator getNavigator() {
        return navigator.get();
    }

    public void setNavigator(Navigator value) {
        navigator.set(value);
        log.debug("{}", value);
    }

    public Assistant getAssistant() {
        return assistant.get();
    }

    public void setAssistant(Assistant value) {
        assistant.set(value);
        log.debug("{}", value);
    }

    public Settings getSettings() {
        return settings.get();
    }

    public void setSettings(Settings value) {
        settings.set(value);
        log.debug("{}", value);
    }
}

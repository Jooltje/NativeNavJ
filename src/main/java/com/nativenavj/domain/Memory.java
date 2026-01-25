package com.nativenavj.domain;

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

    public Goal goal() {
        return goal.get();
    }

    public void updateGoal(Goal newGoal) {
        Goal old = goal.getAndSet(newGoal);
        log.debug("Goal changed: {} -> {}", old, newGoal);
    }

    public State state() {
        return state.get();
    }

    public void updateState(State newState) {
        State old = state.getAndSet(newState);
        log.debug("State changed: {} -> {}", old, newState);
    }

    public Target target() {
        return target.get();
    }

    public void updateTarget(Target newTarget) {
        Target old = target.getAndSet(newTarget);
        log.debug("Target changed: {} -> {}", old, newTarget);
    }

    public Navigator navigator() {
        return navigator.get();
    }

    public void updateNavigator(Navigator newNavigator) {
        Navigator old = navigator.getAndSet(newNavigator);
        log.debug("Navigator changed: {} -> {}", old, newNavigator);
    }

    public Assistant assistant() {
        return assistant.get();
    }

    public void updateAssistant(Assistant newAssistant) {
        Assistant old = assistant.getAndSet(newAssistant);
        log.debug("Assistant changed: {} -> {}", old, newAssistant);
    }
}

package com.nativenavj.domain;

import com.nativenavj.control.Loop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class Memory {
    private static final Logger log = LoggerFactory.getLogger(Memory.class);

    private final AtomicReference<Goal> goal = new AtomicReference<>(Goal.DEFAULT);
    private final AtomicReference<State> state = new AtomicReference<>(State.DEFAULT);
    private final AtomicReference<Target> target = new AtomicReference<>(Target.DEFAULT);
    private final AtomicReference<Navigator> navigator = new AtomicReference<>(Navigator.inactive());
    private final AtomicReference<Assistant> assistant = new AtomicReference<>(Assistant.inactive());

    private final Map<String, Runnable> runnables = new ConcurrentHashMap<>();
    private final Map<String, Loop> loops = new ConcurrentHashMap<>();
    private final Map<String, Configuration> configurations = new ConcurrentHashMap<>();

    public Memory() {
    }

    /**
     * Registers a non-control task.
     */
    public void addTask(String name, Runnable runnable, Loop loop) {
        String key = name.toUpperCase();
        runnables.put(key, runnable);
        loops.put(key, loop);
    }

    /**
     * Registers a control unit with PID parameters.
     */
    public void addController(String name, Runnable runnable, Loop loop, Configuration configuration) {
        String key = name.toUpperCase();
        runnables.put(key, runnable);
        loops.put(key, loop);
        configurations.put(key, configuration);
    }

    public Runnable getRunnable(String name) {
        return runnables.get(name.toUpperCase());
    }

    public Map<String, Runnable> getRunnables() {
        return Collections.unmodifiableMap(runnables);
    }

    public Configuration getConfiguration(String name) {
        return configurations.get(name.toUpperCase());
    }

    public void setConfiguration(String name, Configuration configuration) {
        configurations.put(name.toUpperCase(), configuration);
    }

    public Loop getLoop(String name) {
        return loops.get(name.toUpperCase());
    }

    public void setLoop(String name, Loop loop) {
        loops.put(name.toUpperCase(), loop);
    }

    public boolean isActive(String name) {
        Loop loop = loops.get(name.toUpperCase());
        return loop != null && loop.active();
    }

    public void setActive(String name, boolean active) {
        String key = name.toUpperCase();
        Loop current = loops.get(key);
        double freq = current != null ? current.frequency() : 10.0;
        loops.put(key, new Loop(active, freq));
    }

    public double getFrequency(String name) {
        Loop loop = loops.get(name.toUpperCase());
        return loop != null ? loop.frequency() : 0.0;
    }

    public void setFrequency(String name, double frequency) {
        String key = name.toUpperCase();
        Loop current = loops.get(key);
        boolean active = current != null && current.active();
        loops.put(key, new Loop(active, frequency));
    }

    public Goal getGoal() {
        return goal.get();
    }

    public void setGoal(Goal value) {
        log.debug("{}", value);
        goal.set(value);
    }

    public State getState() {
        return state.get();
    }

    public void setState(State value) {
        log.debug("{}", value);
        state.set(value);
    }

    public Target getTarget() {
        return target.get();
    }

    public void setTarget(Target value) {
        log.debug("{}", value);
        target.set(value);
    }

    public Navigator getNavigator() {
        return navigator.get();
    }

    public void setNavigator(Navigator value) {
        log.debug("{}", value);
        navigator.set(value);
    }

    public Assistant getAssistant() {
        return assistant.get();
    }

    public void setAssistant(Assistant value) {
        log.debug("{}", value);
        assistant.set(value);
    }
}

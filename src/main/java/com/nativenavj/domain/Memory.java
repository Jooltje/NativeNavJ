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
    private final AtomicReference<State> state = new AtomicReference<>(State.neutral());
    private final AtomicReference<Target> target = new AtomicReference<>(Target.neutral());
    private final AtomicReference<Navigator> navigator = new AtomicReference<>(Navigator.inactive());
    private final AtomicReference<Assistant> assistant = new AtomicReference<>(Assistant.inactive());

    private final Map<String, Runnable> registry = new ConcurrentHashMap<>();
    private final Map<String, Loop> schedule = new ConcurrentHashMap<>();
    private final Map<String, Configuration> profile = new ConcurrentHashMap<>();

    public Memory() {
    }

    /**
     * Registers a non-control task.
     */
    public void addTask(String name, Runnable runnable, Loop loop) {
        String key = name.toUpperCase();
        registry.put(key, runnable);
        schedule.put(key, loop);
    }

    /**
     * Registers a control unit with PID parameters.
     */
    public void addController(String name, Runnable runnable, Loop loop, Configuration configuration) {
        String key = name.toUpperCase();
        registry.put(key, runnable);
        schedule.put(key, loop);
        profile.put(key, configuration);
    }

    public Runnable getRunnable(String name) {
        return registry.get(name.toUpperCase());
    }

    public Map<String, Runnable> getRegistry() {
        return Collections.unmodifiableMap(registry);
    }

    public Configuration getProfile(String name) {
        return profile.get(name.toUpperCase());
    }

    public void setProfile(String name, Configuration configuration) {
        profile.put(name.toUpperCase(), configuration);
    }

    public Loop getLoop(String name) {
        return schedule.get(name.toUpperCase());
    }

    public void setSchedule(String name, Loop loop) {
        schedule.put(name.toUpperCase(), loop);
    }

    public boolean isActive(String name) {
        Loop loop = schedule.get(name.toUpperCase());
        return loop != null && loop.status();
    }

    public void setActive(String name, boolean active) {
        String key = name.toUpperCase();
        Loop current = schedule.get(key);
        double freq = current != null ? current.frequency() : 10.0;
        schedule.put(key, new Loop(active, freq));
    }

    public double getFrequency(String name) {
        Loop loop = schedule.get(name.toUpperCase());
        return loop != null ? loop.frequency() : 0.0;
    }

    public void setFrequency(String name, double frequency) {
        String key = name.toUpperCase();
        Loop current = schedule.get(key);
        boolean active = current != null && current.status();
        schedule.put(key, new Loop(active, frequency));
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

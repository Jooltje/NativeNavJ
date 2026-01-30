package com.nativenavj.control;

import com.nativenavj.adapter.Connector;
import com.nativenavj.domain.Memory;
import com.nativenavj.domain.Sample;
import com.nativenavj.domain.Configuration;
import com.nativenavj.port.Objective;
import com.nativenavj.port.Actuator;
import com.nativenavj.port.Sensor;
import com.nativenavj.domain.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Orchestrates the control sources and handles their scheduling.
 */
public class Orchestrator implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Orchestrator.class);

    private final Memory memory;
    private final Connector connector;
    private final Computer computer;
    private final Shell shell;
    private final Runnable assistant;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final Map<String, ScheduledFuture<?>> job = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String, Loop> plan = new java.util.concurrent.ConcurrentHashMap<>();

    public Orchestrator(Memory memory, Connector connector, Computer computer, Shell shell, Runnable assistant) {
        this.memory = memory;
        this.connector = connector;
        this.computer = computer;
        this.shell = shell;
        this.assistant = assistant;

        initialize();
    }

    private void initialize() {
        // Register non-controller tasks
        memory.addTask("COMPUTER", computer, new Loop(false, 10.0));
        memory.addTask("SHELL", shell, new Loop(true, 1.0));
        memory.addTask("ORCHESTRATOR", this, new Loop(true, 0.1));
        memory.addTask("ASSISTANT", assistant, new Loop(false, 0.5));

        // Register strategy profiles
        memory.setProfile("SPEED", Configuration.SPEED_CONTROL);
        memory.setProfile("ALTITUDE", Configuration.ALTITUDE_CONTROL);
        memory.setProfile("HEADING", Configuration.HEADING_CONTROL);
        Actuator pitchActuator = val -> connector.setElevator(val);
        Sensor pitchSensor = () -> new Sample(memory.getState().time(), memory.getState().pitch());
        Objective pitchObjective = () -> memory.getTarget().pitch();
        memory.addController("PITCH", new Controller(pitchObjective, pitchActuator, pitchSensor, Configuration.SURFACE),
                new Loop(false, 50.0), Configuration.SURFACE);

        Actuator rollActuator = val -> connector.setAileron(val);
        Sensor rollSensor = () -> new Sample(memory.getState().time(), memory.getState().roll());
        Objective rollObjective = () -> memory.getTarget().roll();
        memory.addController("ROLL", new Controller(rollObjective, rollActuator, rollSensor, Configuration.SURFACE),
                new Loop(false, 50.0), Configuration.SURFACE);

        Actuator yawActuator = val -> connector.setRudder(val);
        Sensor yawSensor = () -> new Sample(memory.getState().time(), memory.getState().yaw());
        Objective yawObjective = () -> memory.getTarget().yaw();
        memory.addController("YAW", new Controller(yawObjective, yawActuator, yawSensor, Configuration.SURFACE),
                new Loop(false, 50.0), Configuration.SURFACE);

        Actuator throttleActuator = val -> connector.setThrottle(val);
        Sensor throttleSensor = () -> new Sample(memory.getState().time(), memory.getState().climb());
        Objective throttleObjective = () -> memory.getTarget().power();
        memory.addController("THROTTLE",
                new Controller(throttleObjective, throttleActuator, throttleSensor, Configuration.CLIMB),
                new Loop(false, 10.0), Configuration.CLIMB);

        log.info("System registry initialized");
    }

    @Override
    public void run() {
        // Boostrap and dynamic re-scheduling
        Map<String, Runnable> registry = memory.getRegistry();
        for (String name : registry.keySet()) {
            reschedule(name, registry.get(name), memory.getLoop(name));
        }
    }

    public synchronized void configure(String name, Configuration config, Double frequency, Boolean active) {
        String key = name.toUpperCase();
        Runnable runnable = memory.getRunnable(key);
        Loop currentLoop = memory.getLoop(key);

        if (runnable instanceof Controller controller && config != null) {
            Controller updated = controller.setConfiguration(config);
            memory.addController(key, updated, currentLoop, config);
            runnable = updated;
        }

        if (frequency != null || active != null) {
            boolean nextActive = active != null ? active : (currentLoop != null && currentLoop.status());
            double nextFreq = frequency != null ? frequency : (currentLoop != null ? currentLoop.frequency() : 1.0);
            memory.setSchedule(key, new Loop(nextActive, nextFreq));
        }

        reschedule(key, runnable, memory.getLoop(key));
    }

    private void reschedule(String key, Runnable task, Loop loop) {
        Loop scheduled = plan.get(key);
        if (loop != null && loop.equals(scheduled)) {
            return;
        }

        ScheduledFuture<?> future = job.remove(key);
        plan.remove(key);
        if (future != null) {
            future.cancel(false);
        }

        if (loop != null && loop.status() && loop.frequency() > 0) {
            long periodMicros = (long) (1_000_000.0 / loop.frequency());
            job.put(key, scheduler.scheduleAtFixedRate(task, 0, periodMicros, TimeUnit.MICROSECONDS));
            plan.put(key, loop);
            log.debug("Scheduled {} at {}Hz", key, loop.frequency());
        }
    }

    public void stop() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("Orchestrator stopped");
    }

}

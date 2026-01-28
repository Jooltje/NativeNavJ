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
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final Map<String, ScheduledFuture<?>> job = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String, Loop> plan = new java.util.concurrent.ConcurrentHashMap<>();

    public Orchestrator(Memory memory, Connector connector, Computer computer, Shell shell) {
        this.memory = memory;
        this.connector = connector;
        this.computer = computer;
        this.shell = shell;

        initialize();
    }

    private void initialize() {
        // Register non-controller tasks
        memory.addTask("CPU", computer, new Loop(false, 10.0));
        memory.addTask("SHL", shell, new Loop(true, 1.0));
        memory.addTask("ORC", this, new Loop(true, 0.1));

        // Register controllers
        Actuator pitchAct = val -> connector.setElevator(val);
        Sensor pitchSen = () -> new Sample(memory.getState().time(), memory.getState().pitch());
        Objective pitchObj = () -> memory.getTarget().pitch();
        memory.addController("PIT", new Controller(pitchObj, pitchAct, pitchSen, Configuration.SURFACE),
                new Loop(false, 50.0), Configuration.SURFACE);

        Actuator rollAct = val -> connector.setAileron(val);
        Sensor rollSen = () -> new Sample(memory.getState().time(), memory.getState().roll());
        Objective rollObj = () -> memory.getTarget().roll();
        memory.addController("ROL", new Controller(rollObj, rollAct, rollSen, Configuration.SURFACE),
                new Loop(false, 50.0), Configuration.SURFACE);

        Actuator yawAct = val -> connector.setRudder(val);
        Sensor yawSen = () -> new Sample(memory.getState().time(), memory.getState().yaw());
        Objective yawObj = () -> memory.getTarget().yaw();
        memory.addController("YAW", new Controller(yawObj, yawAct, yawSen, Configuration.SURFACE),
                new Loop(false, 50.0), Configuration.SURFACE);

        Actuator thrAct = val -> connector.setThrottle(val);
        Sensor thrSen = () -> new Sample(memory.getState().time(), memory.getState().speed());
        Objective thrObj = () -> memory.getTarget().power();
        memory.addController("THR", new Controller(thrObj, thrAct, thrSen, Configuration.THROTTLE),
                new Loop(false, 10.0), Configuration.THROTTLE);

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

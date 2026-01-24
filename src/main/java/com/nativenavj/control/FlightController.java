package com.nativenavj.control;

import com.nativenavj.control.actuator.ActuatorLoop;
import com.nativenavj.control.core.ControlFrame;
import com.nativenavj.control.core.FlightGoal;
import com.nativenavj.control.core.FlightTelemetry;
import com.nativenavj.control.parser.CommandParser;
import com.nativenavj.control.tecs.TECSModule;
import com.nativenavj.simconnect.SimConnectService;
import com.nativenavj.simconnect.TelemetryData;
import com.nativenavj.util.LogManager;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Orchestrator for the Flight Control System.
 * Manages the lifecycle of the concurrent functional pipeline.
 */
public class FlightController {
    private SimConnectService service;

    // The shared state pipeline
    private final AtomicReference<FlightGoal> goalRef = new AtomicReference<>(new FlightGoal(false, 0, 0, 0));
    private final AtomicReference<FlightTelemetry> telemetryRef = new AtomicReference<>(null);
    private final AtomicReference<ControlFrame> controlRef = new AtomicReference<>(new ControlFrame(0, 0, 0));

    // Components
    private TECSModule tecs;
    private ActuatorLoop actuator;
    private CommandParser parser;

    private Thread tecsThread;
    private Thread actuatorThread;

    public void setService(SimConnectService service) {
        this.service = service;
        this.parser = new CommandParser(goalRef);
        initPipeline();
    }

    private void initPipeline() {
        if (service == null)
            return;

        // Initialize modules
        tecs = new TECSModule(20.0, goalRef, telemetryRef, controlRef);
        actuator = new ActuatorLoop(100.0, goalRef, controlRef, telemetryRef, service);

        // Start threads
        tecsThread = new Thread(tecs, "TECSModule-Thread");
        actuatorThread = new Thread(actuator, "ActuatorLoop-Thread");

        tecsThread.start();
        actuatorThread.start();

        LogManager.info("FCS Pipeline initialized and threads started.");
    }

    /**
     * Updates the telemetry state in the pipeline.
     * This is called whenever new telemetry arrives from SimConnect.
     */
    public void update(TelemetryData telemetry) {
        if (telemetry == null)
            return;

        // Map SimConnect TelemetryData to FCS FlightTelemetry record
        FlightTelemetry ft = new FlightTelemetry(
                telemetry.altitude(),
                telemetry.airspeed(),
                telemetry.pitch(),
                telemetry.bank(),
                telemetry.heading(),
                0.0, // Vertical speed (TODO: add to TelemetryData if needed)
                System.nanoTime());

        telemetryRef.set(ft);
    }

    // Command API
    public void setTargetHeading(double heading) {
        parser.parse("HDG " + heading);
        parser.parse("ON");
    }

    public void setTargetAltitude(double altitude) {
        parser.parse("ALT " + altitude);
        parser.parse("ON");
    }

    public void setTargetAirspeed(double airspeed) {
        parser.parse("SPD " + airspeed);
        parser.parse("ON");
    }

    public void engageAll() {
        // Sync current telemetry to goals before engaging (Sync logic)
        FlightTelemetry current = telemetryRef.get();
        if (current != null) {
            goalRef.set(new FlightGoal(true, current.altitudeFt(), current.headingDeg(), current.airspeedKts()));
        } else {
            parser.parse("ON");
        }
    }

    public void disableAll() {
        parser.parse("OFF");
    }

    public void stop() {
        if (tecs != null)
            tecs.stop();
        if (actuator != null)
            actuator.stop();
        LogManager.info("FCS Threads stopped.");
    }

    public double getTargetHeading() {
        FlightGoal g = goalRef.get();
        return g != null ? g.targetHeadingDeg() : Double.NaN;
    }

    public double getTargetAltitude() {
        FlightGoal g = goalRef.get();
        return g != null ? g.targetAltitudeFt() : Double.NaN;
    }

    public double getTargetAirspeed() {
        FlightGoal g = goalRef.get();
        return g != null ? g.targetAirspeedKts() : Double.NaN;
    }
}

package com.nativenavj.adapter;

import com.nativenavj.domain.Command;
import com.nativenavj.domain.Telemetry;
import com.nativenavj.port.Actuator;
import com.nativenavj.port.Sensor;
import com.nativenavj.simconnect.SimConnectService;
import com.nativenavj.simconnect.TelemetryData;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Adapter bridging SimConnect native layer to domain ports.
 * Implements Sensor and Actuator interfaces for the hexagonal architecture.
 */
public class SimConnectAdapter implements Sensor, Actuator {
    private final SimConnectService simConnect;
    private final AtomicReference<TelemetryData> latestTelemetry = new AtomicReference<>();
    private volatile boolean connected = false;

    // Vertical speed tracking for energy calculations
    private double previousAltitude = 0.0;
    private long previousTimeNanos = System.nanoTime();

    public SimConnectAdapter(SimConnectService simConnect) {
        this.simConnect = simConnect;
    }

    /**
     * Sets the latest telemetry data (called by SimConnect callback).
     */
    public void updateTelemetry(TelemetryData telemetry) {
        latestTelemetry.set(telemetry);
    }

    /**
     * Sets the connection status.
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    @Override
    public Telemetry read() {
        TelemetryData telemetry = latestTelemetry.get();
        if (telemetry == null) {
            return Telemetry.neutral();
        }

        // Calculate vertical speed (fpm)
        long currentTimeNanos = System.nanoTime();
        double dt = (currentTimeNanos - previousTimeNanos) / 1_000_000_000.0;
        double verticalSpeedFpm = 0.0;

        if (dt > 0.0 && previousAltitude != 0.0) {
            double altitudeChange = telemetry.altitude() - previousAltitude;
            verticalSpeedFpm = (altitudeChange / dt) * 60.0; // Convert to feet per minute
        }

        previousAltitude = telemetry.altitude();
        previousTimeNanos = currentTimeNanos;

        return new Telemetry(
                telemetry.altitude(),
                telemetry.airspeed(),
                telemetry.heading(),
                telemetry.pitch(),
                telemetry.bank(),
                0.0, // yaw - not provided by current telemetry
                verticalSpeedFpm);
    }

    @Override
    public boolean isAvailable() {
        return connected && simConnect.isConnected();
    }

    @Override
    public void write(Command command) {
        if (!isReady()) {
            return;
        }

        // Convert Command (domain) to SimConnect surface values
        // Command uses: pitch (degrees), roll (degrees), throttle [0,1], rudder
        // (degrees)
        // SimConnect expects: aileron, elevator, rudder, throttle

        // Convert roll to aileron (simplified - would need proper conversion)
        double aileron = command.roll() / 30.0; // Normalize to roughly [-1, 1]

        // Convert pitch to elevator (simplified)
        double elevator = command.pitch() / 20.0; // Normalize to roughly [-1, 1]

        // Rudder is already in degrees, normalize
        double rudder = command.rudder() / 30.0;

        // Throttle is already [0, 1]
        double throttle = command.throttle();

        simConnect.actuateSurfaces(aileron, elevator, rudder, throttle);
    }

    @Override
    public boolean isReady() {
        return connected && simConnect.isConnected();
    }
}

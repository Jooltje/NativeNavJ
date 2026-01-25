package com.nativenavj.control;

import com.nativenavj.adapter.Connector;
import com.nativenavj.domain.Memory;
import com.nativenavj.domain.State;
import com.nativenavj.port.Clock;
import com.nativenavj.simconnect.TelemetryData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Knowledge Source responsible for gathering telemetry from the Connector
 * and updating the aircraft State in Memory.
 */
public class Sensor extends Loop {
    private static final Logger log = LoggerFactory.getLogger(Sensor.class);

    private final Connector connector;
    private final Memory memory;

    // Vertical speed tracking
    private double previousAltitude = 0.0;
    private long previousTimeNanos = 0;

    public Sensor(Connector connector, Memory memory, Clock clock) {
        super(20.0, clock); // Run at 20Hz as per specification/impl plan
        this.connector = connector;
        this.memory = memory;
    }

    @Override
    protected void step() {
        if (!connector.isReady()) {
            return;
        }

        TelemetryData telemetry = connector.getLatestTelemetry();
        if (telemetry == null) {
            return;
        }

        // Calculate vertical speed (fpm)
        long currentTimeNanos = System.nanoTime();
        double verticalSpeedFpm = 0.0;

        if (previousTimeNanos != 0) {
            double dt = (currentTimeNanos - previousTimeNanos) / 1_000_000_000.0;
            if (dt > 0.0) {
                double altitudeChange = telemetry.altitude() - previousAltitude;
                verticalSpeedFpm = (altitudeChange / dt) * 60.0;
            }
        }

        previousAltitude = telemetry.altitude();
        previousTimeNanos = currentTimeNanos;

        State state = new State(
                telemetry.latitude(),
                telemetry.longitude(),
                telemetry.heading(),
                telemetry.altitude(),
                telemetry.bank(),
                telemetry.pitch(),
                0.0, // yaw rate (would need additional telemetry)
                telemetry.airspeed(),
                verticalSpeedFpm);

        memory.updateState(state);
    }
}

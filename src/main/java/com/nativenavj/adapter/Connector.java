package com.nativenavj.adapter;

import com.nativenavj.simconnect.SimConnectBindings;
import com.nativenavj.simconnect.TelemetryData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Low-level bridge to Microsoft Flight Simulator 2020 using Project Panama.
 * Provides specialized methods for control surface actuation and telemetry for
 * Sensors.
 */
public class Connector {
    private static final Logger logger = LoggerFactory.getLogger(Connector.class);

    private static final int DEFINITION_ID = 1;
    private static final int DEF_AIL = 2;
    private static final int DEF_ELE = 3;
    private static final int DEF_THR = 4;
    private static final int DEF_RUD = 5;
    private static final int REQUEST_ID = 1;

    // SIMCONNECT_RECV_ID
    private static final int SIMCONNECT_RECV_ID_EVENT = 3;
    private static final int SIMCONNECT_RECV_ID_SIMOBJECT_DATA = 8;
    private static final int SIMCONNECT_RECV_ID_QUIT = 1;

    // EVENT_IDs
    private static final int EVENT_ID_SIM_START = 1;

    private MemorySegment hSimConnect = MemorySegment.NULL;
    private final Arena arena = Arena.ofShared();
    private volatile boolean running = false;
    private final AtomicReference<TelemetryData> latestTelemetry = new AtomicReference<>();

    public void connect() {
        logger.info("Connecting to SimConnect via Project Panama...");
        try {
            MemorySegment phSimConnect = arena.allocate(ValueLayout.ADDRESS);
            int result = SimConnectBindings.open(phSimConnect, "NativeNavJ");

            if (result == 0) {
                hSimConnect = phSimConnect.get(ValueLayout.ADDRESS, 0);
                logger.info("Successfully connected to SimConnect. Handle: {}", hSimConnect);
                setupDefinitions();
                startMessageLoop();
            } else {
                logger.error("Failed to connect to SimConnect. HRESULT: 0x{}", Integer.toHexString(result));
            }
        } catch (Throwable t) {
            logger.error("Critical error during SimConnect connection", t);
        }
    }

    private void setupDefinitions() throws Throwable {
        // Telemetry Definition
        SimConnectBindings.addToDataDefinition(hSimConnect, DEFINITION_ID, "PLANE LATITUDE", "degrees", 4);
        SimConnectBindings.addToDataDefinition(hSimConnect, DEFINITION_ID, "PLANE LONGITUDE", "degrees", 4);
        SimConnectBindings.addToDataDefinition(hSimConnect, DEFINITION_ID, "PLANE ALTITUDE", "feet", 4);
        SimConnectBindings.addToDataDefinition(hSimConnect, DEFINITION_ID, "AIRSPEED INDICATED", "knots", 4);
        SimConnectBindings.addToDataDefinition(hSimConnect, DEFINITION_ID, "PLANE HEADING DEGREES MAGNETIC", "degrees",
                4);
        SimConnectBindings.addToDataDefinition(hSimConnect, DEFINITION_ID, "PLANE BANK DEGREES", "degrees", 4);
        SimConnectBindings.addToDataDefinition(hSimConnect, DEFINITION_ID, "PLANE PITCH DEGREES", "degrees", 4);
        SimConnectBindings.addToDataDefinition(hSimConnect, DEFINITION_ID, "ABSOLUTE TIME", "seconds", 4);

        // Request Data: Period 2 = Visual Frame
        SimConnectBindings.requestDataOnSimObject(hSimConnect, REQUEST_ID, DEFINITION_ID, 0, 2);

        // Control Surface Definitions
        SimConnectBindings.addToDataDefinition(hSimConnect, DEF_AIL, "AILERON POSITION", "percent", 4);
        SimConnectBindings.addToDataDefinition(hSimConnect, DEF_ELE, "ELEVATOR POSITION", "percent", 4);
        SimConnectBindings.addToDataDefinition(hSimConnect, DEF_THR, "GENERAL ENG THROTTLE LEVER POSITION:1", "percent",
                4);
        SimConnectBindings.addToDataDefinition(hSimConnect, DEF_RUD, "RUDDER POSITION", "percent", 4);

        // Subscribe to SimStart
        SimConnectBindings.subscribeToSystemEvent(hSimConnect, EVENT_ID_SIM_START, "SimStart");

        logger.info("SimConnect definitions and subscriptions established.");
    }

    private void startMessageLoop() {
        running = true;
        Thread thread = new Thread(this::messageLoop, "Connector-MessageLoop");
        thread.setDaemon(true);
        thread.start();
    }

    private void messageLoop() {
        try {
            MethodHandle handle = MethodHandles.lookup().findVirtual(Connector.class, "onDispatch",
                    MethodType.methodType(void.class, MemorySegment.class, int.class, MemorySegment.class));
            MemorySegment stub = SimConnectBindings.createDispatchStub(handle.bindTo(this), arena);

            while (running && hSimConnect != MemorySegment.NULL) {
                SimConnectBindings.callDispatch(hSimConnect, stub);
                Thread.sleep(20); // Poll frequently
            }
        } catch (Throwable t) {
            logger.error("Error in Connector message loop", t);
        }
    }

    @SuppressWarnings("unused")
    private void onDispatch(MemorySegment pData, int cbData, MemorySegment pContext) {
        MemorySegment sizedSegment = pData.reinterpret(cbData);
        int dwID = sizedSegment.get(ValueLayout.JAVA_INT, 8);

        if (dwID == SIMCONNECT_RECV_ID_SIMOBJECT_DATA) {
            MemorySegment dataSegment = sizedSegment.asSlice(40, TelemetryData.LAYOUT.byteSize());
            latestTelemetry.set(TelemetryData.fromMemory(dataSegment));
        } else if (dwID == SIMCONNECT_RECV_ID_EVENT) {
            int eventID = sizedSegment.get(ValueLayout.JAVA_INT, 12);
            if (eventID == EVENT_ID_SIM_START) {
                logger.info("MSFS Flight Started/Restarted.");
            }
        } else if (dwID == SIMCONNECT_RECV_ID_QUIT) {
            logger.info("SimConnect requested quit.");
            running = false;
        }
    }

    public TelemetryData getLatestTelemetry() {
        return latestTelemetry.get();
    }

    public void setAileron(double value) {
        if (!isReady())
            return;
        try (Arena localArena = Arena.ofConfined()) {
            sendData(DEF_AIL, value, localArena);
        } catch (Throwable t) {
            logger.error("Failed to set aileron", t);
        }
    }

    public void setElevator(double value) {
        if (!isReady())
            return;
        try (Arena localArena = Arena.ofConfined()) {
            sendData(DEF_ELE, value, localArena);
        } catch (Throwable t) {
            logger.error("Failed to set elevator", t);
        }
    }

    public void setRudder(double value) {
        if (!isReady())
            return;
        try (Arena localArena = Arena.ofConfined()) {
            sendData(DEF_RUD, value, localArena);
        } catch (Throwable t) {
            logger.error("Failed to set rudder", t);
        }
    }

    public void setThrottle(double value) {
        if (!isReady())
            return;
        try (Arena localArena = Arena.ofConfined()) {
            sendData(DEF_THR, value, localArena);
        } catch (Throwable t) {
            logger.error("Failed to set throttle", t);
        }
    }

    private void sendData(int defId, double value, Arena localArena) throws Throwable {
        MemorySegment seg = localArena.allocate(ValueLayout.JAVA_DOUBLE);
        seg.set(ValueLayout.JAVA_DOUBLE, 0, value);
        SimConnectBindings.setDataOnSimObject(hSimConnect, defId, 0, 0, 0, 8, seg);
    }

    private boolean isReady() {
        return hSimConnect != MemorySegment.NULL && running;
    }

    public void disconnect() {
        running = false;
        if (hSimConnect != MemorySegment.NULL) {
            try {
                SimConnectBindings.close(hSimConnect);
                logger.info("Disconnected from SimConnect.");
            } catch (Throwable t) {
                logger.error("Error during SimConnect disconnection", t);
            } finally {
                hSimConnect = MemorySegment.NULL;
            }
        }
    }

    public void shutdown() {
        disconnect();
        arena.close();
    }
}

package com.nativenavj.simconnect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import com.nativenavj.control.FlightController;
import com.nativenavj.safety.SafetyGuardrails;
import com.nativenavj.strategy.CognitiveOrchestrator;

public class SimConnectService {
    private static final Logger logger = LoggerFactory.getLogger(SimConnectService.class);

    private MemorySegment hSimConnect = MemorySegment.NULL;
    private final Arena serviceArena = Arena.ofShared();
    private volatile boolean running = false;
    private TelemetryData lastTelemetry = null;

    private final FlightController flightController = new FlightController();
    private final SafetyGuardrails safetyGuardrails = new SafetyGuardrails();
    private CognitiveOrchestrator cognitiveOrchestrator;

    private static final int DEFINITION_ID = 1;
    private static final int REQUEST_ID = 1;

    // SIMCONNECT_RECV_ID
    private static final int SIMCONNECT_RECV_ID_SIMOBJECT_DATA = 8;
    private static final int SIMCONNECT_RECV_ID_QUIT = 1;

    public void init() {
        logger.info("Initializing SimConnectService with Project Panama (Java 25)...");

        // Initialize Cognitive layer
        try {
            this.cognitiveOrchestrator = new CognitiveOrchestrator(flightController, safetyGuardrails);
        } catch (Exception e) {
            logger.warn(
                    "Failed to initialize CognitiveOrchestrator. AI features will be disabled. Check if Ollama is running.");
        }

        try {
            MemorySegment phSimConnect = serviceArena.allocate(ValueLayout.ADDRESS);
            int result = SimConnectBindings.open(phSimConnect, "NativeNavJ");

            if (result == 0) {
                hSimConnect = phSimConnect.get(ValueLayout.ADDRESS, 0);
                logger.info("Successfully connected to SimConnect. Handle: {}", hSimConnect);
                setupTelemetrySubscription();
                startMessageLoop();
            } else {
                logger.error("Failed to connect to SimConnect. HRESULT: 0x{}", Integer.toHexString(result));
            }
        } catch (Throwable t) {
            logger.error("Critical error during SimConnect initialization", t);
        }
    }

    private void setupTelemetrySubscription() throws Throwable {
        // datumType 4 is Float64
        SimConnectBindings.addToDataDefinition(hSimConnect, DEFINITION_ID, "PLANE LATITUDE", "degrees", 4);
        SimConnectBindings.addToDataDefinition(hSimConnect, DEFINITION_ID, "PLANE LONGITUDE", "degrees", 4);
        SimConnectBindings.addToDataDefinition(hSimConnect, DEFINITION_ID, "PLANE ALTITUDE", "feet", 4);
        SimConnectBindings.addToDataDefinition(hSimConnect, DEFINITION_ID, "AIRSPEED INDICATED", "knots", 4);
        SimConnectBindings.addToDataDefinition(hSimConnect, DEFINITION_ID, "PLANE HEADING DEGREES MAGNETIC", "degrees",
                4);

        // Period: 4 = 1 second
        SimConnectBindings.requestDataOnSimObject(hSimConnect, REQUEST_ID, DEFINITION_ID, 0, 4);
        logger.info("Telemetry subscription established.");
    }

    private void startMessageLoop() {
        running = true;
        Thread thread = new Thread(this::messageLoop, "SimConnect-MessageLoop");
        thread.setDaemon(true);
        thread.start();
    }

    private void messageLoop() {
        try {
            MethodHandle handle = MethodHandles.lookup().findVirtual(SimConnectService.class, "onDispatch",
                    MethodType.methodType(void.class, MemorySegment.class, int.class, MemorySegment.class));
            MemorySegment stub = SimConnectBindings.createDispatchStub(handle.bindTo(this), serviceArena);

            while (running && hSimConnect != MemorySegment.NULL) {
                SimConnectBindings.callDispatch(hSimConnect, stub);

                if (lastTelemetry != null) {
                    flightController.update(lastTelemetry);
                }

                Thread.sleep(50); // Poll every 50ms (20Hz)
            }
        } catch (Throwable t) {
            logger.error("Error in SimConnect message loop", t);
        }
    }

    // Callback method for SimConnect_CallDispatch
    @SuppressWarnings("unused")
    private void onDispatch(MemorySegment pData, int cbData, MemorySegment pContext) {
        // In Project Panama upcalls, segments are zero-length by default.
        // We must reinterpret with the actual size provided by SimConnect.
        MemorySegment sizedSegment = pData.reinterpret(cbData);

        // pData points to SIMCONNECT_RECV header: dwSize(4), dwVersion(4), dwID(4)
        int dwID = sizedSegment.get(ValueLayout.JAVA_INT, 8);

        if (dwID == SIMCONNECT_RECV_ID_SIMOBJECT_DATA) {
            // Header for SIMOBJECT_DATA is 40 bytes (12 bytes base + 28 bytes extension)
            MemorySegment dataSegment = sizedSegment.asSlice(40, TelemetryData.LAYOUT.byteSize());
            lastTelemetry = TelemetryData.fromMemory(dataSegment);

            // Explicit verification log
            logger.debug("RAW TELEMETRY RECEIVED: Size={} bytes", cbData);
            logger.info("Telemetry Update: Lat={}\u00B0, Lon={}\u00B0, Alt={}ft, Speed={}kts, Heading={}\u00B0",
                    lastTelemetry.latitude(), lastTelemetry.longitude(),
                    (int) lastTelemetry.altitude(), (int) lastTelemetry.airspeed(), (int) lastTelemetry.heading());
        } else if (dwID == SIMCONNECT_RECV_ID_QUIT) {
            logger.info("SimConnect requested quit.");
            running = false;
        }
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

    public boolean isConnected() {
        return hSimConnect != MemorySegment.NULL;
    }

    public void shutdown() {
        disconnect();
        serviceArena.close();
    }

    public static void main(String[] args) {
        SimConnectService service = new SimConnectService();
        service.init();

        // Add shutdown hook for Ctrl+C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown Hook triggered. Closing SimConnect...");
            service.shutdown();
        }));

        // Simple interactive demonstration
        new Thread(() -> {
            try {
                Thread.sleep(5000); // Wait for connection
                if (service.isConnected()) {
                    service.cognitiveOrchestrator.issueCommand("Climb to 5000 feet and maintain heading 270.");
                }
            } catch (Exception e) {
                logger.error("Error in demo command thread", e);
            }
        }).start();

        // Keep alive until running is set to false (either via SimConnect QUIT or
        // Shutdown Hook)
        try {
            while (service.running) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        logger.info("Service main loop exited.");
    }
}

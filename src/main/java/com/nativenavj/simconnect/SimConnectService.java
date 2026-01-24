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
import com.nativenavj.util.LogManager;

import java.util.Scanner;

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

    public void init() {
        logger.info("Initializing SimConnectService with Project Panama (Java 25)...");

        // Initialize Cognitive layer and Flight Controller link
        this.flightController.setService(this);
        this.flightController.disableAll(); // START IN MANUAL MODE
        try {
            this.cognitiveOrchestrator = new CognitiveOrchestrator(flightController, safetyGuardrails);
        } catch (Exception e) {
            LogManager.warn(
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
        SimConnectBindings.addToDataDefinition(hSimConnect, DEFINITION_ID, "PLANE BANK DEGREES", "degrees", 4);
        SimConnectBindings.addToDataDefinition(hSimConnect, DEFINITION_ID, "PLANE PITCH DEGREES", "degrees", 4);

        // Period: 2 = Visual Frame (~20-60Hz)
        SimConnectBindings.requestDataOnSimObject(hSimConnect, REQUEST_ID, DEFINITION_ID, 0, 2);

        // Define Granular Control Surface Actuation
        SimConnectBindings.addToDataDefinition(hSimConnect, DEF_AIL, "AILERON POSITION", "percent", 4);
        SimConnectBindings.addToDataDefinition(hSimConnect, DEF_ELE, "ELEVATOR POSITION", "percent", 4);
        SimConnectBindings.addToDataDefinition(hSimConnect, DEF_THR, "GENERAL ENG THROTTLE LEVER POSITION:1", "percent",
                4);
        SimConnectBindings.addToDataDefinition(hSimConnect, DEF_RUD, "RUDDER POSITION", "percent", 4);

        // Subscribe to SimStart to auto-reset controller on flight restart
        SimConnectBindings.subscribeToSystemEvent(hSimConnect, EVENT_ID_SIM_START, "SimStart");

        LogManager.info("Telemetry and Granular Control subscriptions established.");
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

            if (lastTelemetry != null) {
                LogManager.logTelemetry(
                        String.format("Lat=%.6f, Lon=%.6f, Alt=%.1f, Speed=%.1f, Heading=%.1f, Bank=%.1f, Pitch=%.1f",
                                lastTelemetry.latitude(), lastTelemetry.longitude(),
                                lastTelemetry.altitude(), lastTelemetry.airspeed(), lastTelemetry.heading(),
                                lastTelemetry.bank(), lastTelemetry.pitch()));
            }
        } else if (dwID == SIMCONNECT_RECV_ID_EVENT) {
            int eventID = sizedSegment.get(ValueLayout.JAVA_INT, 12);
            if (eventID == EVENT_ID_SIM_START) {
                LogManager.info("MSFS Flight Started/Restarted. Disabling all autonomous controls.");
                flightController.disableAll();
            }
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

    public synchronized void actuateSurfaces(double aileron, double elevator, double rudder, double throttle) {
        if (!isConnected())
            return;

        try (Arena arena = Arena.ofConfined()) {
            // Aileron
            if (!Double.isNaN(aileron)) {
                MemorySegment seg = arena.allocate(ValueLayout.JAVA_DOUBLE);
                seg.set(ValueLayout.JAVA_DOUBLE, 0, aileron);
                SimConnectBindings.setDataOnSimObject(hSimConnect, DEF_AIL, 0, 0, 0, 8, seg);
            }
            // Elevator
            if (!Double.isNaN(elevator)) {
                MemorySegment seg = arena.allocate(ValueLayout.JAVA_DOUBLE);
                seg.set(ValueLayout.JAVA_DOUBLE, 0, elevator);
                SimConnectBindings.setDataOnSimObject(hSimConnect, DEF_ELE, 0, 0, 0, 8, seg);
            }
            // Throttle
            if (!Double.isNaN(throttle) && throttle >= 0) {
                MemorySegment seg = arena.allocate(ValueLayout.JAVA_DOUBLE);
                seg.set(ValueLayout.JAVA_DOUBLE, 0, throttle);
                SimConnectBindings.setDataOnSimObject(hSimConnect, DEF_THR, 0, 0, 0, 8, seg);
            }
            // Rudder
            if (!Double.isNaN(rudder)) {
                MemorySegment seg = arena.allocate(ValueLayout.JAVA_DOUBLE);
                seg.set(ValueLayout.JAVA_DOUBLE, 0, rudder);
                SimConnectBindings.setDataOnSimObject(hSimConnect, DEF_RUD, 0, 0, 0, 8, seg);
            }
        } catch (Throwable t) {
            LogManager.error("Failed to actuate surfaces", t);
        }
    }

    public void shutdown() {
        disconnect();
        serviceArena.close();
    }

    public static void main(String[] args) {
        SimConnectService service = new SimConnectService();
        service.init();

        // Update status for user
        System.out.println("\n===============================================");
        System.out.println("   NativeNavJ - Autonomous Flight Control");
        System.out.println("   Connected & Listening. Type 'exit' to quit.");
        System.out.println("===============================================\n");

        // Interactive CLI Loop
        try (Scanner scanner = new Scanner(System.in)) {
            while (service.running) {
                System.out.print("COMMAND > ");
                if (scanner.hasNextLine()) {
                    String input = scanner.nextLine().trim();
                    if ("exit".equalsIgnoreCase(input) || "quit".equalsIgnoreCase(input)) {
                        service.running = false;
                    } else if (!input.isEmpty()) {
                        if (service.cognitiveOrchestrator != null) {
                            LogManager.info("User Command: " + input);
                            String response = service.cognitiveOrchestrator.issueCommand(input);
                            System.out.println("CO-PILOT > " + response);
                        } else {
                            LogManager.warn("AI Orchestrator not available. Command ignored.");
                        }
                    }
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            LogManager.error("CLI error", e);
        }

        service.shutdown();
        LogManager.info("Service main loop exited.");
        LogManager.close();
    }
}

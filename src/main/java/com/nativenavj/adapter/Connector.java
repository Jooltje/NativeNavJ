package com.nativenavj.adapter;

import com.nativenavj.domain.Memory;
import com.nativenavj.domain.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static java.lang.foreign.ValueLayout.*;

/**
 * The Connector object that talks to the simulator.
 * Implements the Single Handler Thread and Non-Blocking Dispatcher patterns.
 */
public class Connector {
    private static final Logger log = LoggerFactory.getLogger(Connector.class);
    private static final Linker LINKER = Linker.nativeLinker();

    private final Memory memory;
    private final BlockingQueue<Consumer<MemorySegment>> commandQueue = new LinkedBlockingQueue<>();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread handlerThread;

    // SimConnect Data Definition IDs
    private static final int DEFINITION_STATE = 1;

    // SimConnect Event IDs
    private static final int EVENT_ELEVATOR = 1;
    private static final int EVENT_AILERON = 2;
    private static final int EVENT_RUDDER = 3;
    private static final int EVENT_THROTTLE = 4;

    // Memory Layout for State data (matches State record order)
    private static final GroupLayout STATE_LAYOUT = MemoryLayout.structLayout(
            JAVA_DOUBLE.withName("latitude"),
            JAVA_DOUBLE.withName("longitude"),
            JAVA_DOUBLE.withName("heading"),
            JAVA_DOUBLE.withName("altitude"),
            JAVA_DOUBLE.withName("roll"),
            JAVA_DOUBLE.withName("pitch"),
            JAVA_DOUBLE.withName("yaw"),
            JAVA_DOUBLE.withName("speed"),
            JAVA_DOUBLE.withName("climb"),
            JAVA_DOUBLE.withName("time"));

    // Static registry for upcalls
    private static Connector instance;

    public Connector(Memory memory) {
        this.memory = memory;
        instance = this;
        start();
    }

    private void start() {
        if (running.getAndSet(true))
            return;

        handlerThread = new Thread(this::handlerLoop, "SimConnect-Handler");
        handlerThread.setDaemon(true);
        handlerThread.start();
    }

    public void stop() {
        running.set(false);
        if (handlerThread != null) {
            try {
                handlerThread.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void handlerLoop() {
        try (Arena arena = Arena.ofShared()) {
            MemorySegment phSimConnect = arena.allocate(ADDRESS);
            int hr = SimConnect.open(phSimConnect, "NativeNavJ");
            if (hr < 0) {
                log.error("Failed to open SimConnect: {}", hr);
                running.set(false);
                return;
            }
            MemorySegment hSimConnect = phSimConnect.get(ADDRESS, 0);
            log.info("SimConnect opened successfully");

            setupDefinitions(hSimConnect);
            setupEvents(hSimConnect);

            // Start receiving data
            SimConnect.requestDataOnSimObject(hSimConnect, DEFINITION_STATE, DEFINITION_STATE,
                    SimConnect.SIMCONNECT_OBJECT_ID_USER, SimConnect.SIMCONNECT_PERIOD_SIM_FRAME);

            MemorySegment callbackStub = LINKER.upcallStub(
                    MethodHandles.lookup().findStatic(Connector.class, "dispatchCallback",
                            java.lang.invoke.MethodType.methodType(void.class, MemorySegment.class, int.class,
                                    MemorySegment.class)),
                    FunctionDescriptor.ofVoid(ADDRESS, JAVA_INT, ADDRESS),
                    arena);
            while (running.get()) {
                // Process queued commands
                Consumer<MemorySegment> command;
                while ((command = commandQueue.poll()) != null) {
                    command.accept(hSimConnect);
                }

                // Poll simulator messages
                hr = SimConnect.callDispatch(hSimConnect, callbackStub, MemorySegment.NULL);
                if (hr < 0) {
                    log.error("SimConnect_CallDispatch failed: {}", hr);
                }

                Thread.sleep(20); // Poll every 20ms (~50Hz)
            }

            SimConnect.close(hSimConnect);
            log.info("SimConnect closed");
        } catch (Throwable t) {
            log.error("SimConnect handler thread error", t);
        } finally {
            running.set(false);
        }
    }

    private void setupDefinitions(MemorySegment hSimConnect) throws Throwable {
        check(SimConnect.addToDataDefinition(hSimConnect, DEFINITION_STATE, "PLANE LATITUDE", "degrees",
                SimConnect.SIMCONNECT_DATATYPE_FLOAT64));
        check(SimConnect.addToDataDefinition(hSimConnect, DEFINITION_STATE, "PLANE LONGITUDE", "degrees",
                SimConnect.SIMCONNECT_DATATYPE_FLOAT64));
        check(SimConnect.addToDataDefinition(hSimConnect, DEFINITION_STATE, "PLANE HEADING DEGREES TRUE", "degrees",
                SimConnect.SIMCONNECT_DATATYPE_FLOAT64));
        check(SimConnect.addToDataDefinition(hSimConnect, DEFINITION_STATE, "PLANE ALTITUDE", "feet",
                SimConnect.SIMCONNECT_DATATYPE_FLOAT64));
        check(SimConnect.addToDataDefinition(hSimConnect, DEFINITION_STATE, "PLANE BANK DEGREES", "degrees",
                SimConnect.SIMCONNECT_DATATYPE_FLOAT64));
        check(SimConnect.addToDataDefinition(hSimConnect, DEFINITION_STATE, "PLANE PITCH DEGREES", "degrees",
                SimConnect.SIMCONNECT_DATATYPE_FLOAT64));
        check(SimConnect.addToDataDefinition(hSimConnect, DEFINITION_STATE, "PLANE HEADING DEGREES MAGNETIC", "degrees",
                SimConnect.SIMCONNECT_DATATYPE_FLOAT64));
        check(SimConnect.addToDataDefinition(hSimConnect, DEFINITION_STATE, "AIRSPEED INDICATED", "knots",
                SimConnect.SIMCONNECT_DATATYPE_FLOAT64));
        check(SimConnect.addToDataDefinition(hSimConnect, DEFINITION_STATE, "VERTICAL SPEED", "feet per second",
                SimConnect.SIMCONNECT_DATATYPE_FLOAT64));
        check(SimConnect.addToDataDefinition(hSimConnect, DEFINITION_STATE, "ABSOLUTE TIME", "seconds",
                SimConnect.SIMCONNECT_DATATYPE_FLOAT64));
    }

    private void check(int hr) {
        if (hr < 0)
            throw new RuntimeException("SimConnect call failed with HRESULT " + hr);
    }

    // SimConnect Group IDs
    private static final int GROUP_CONTROLS = 1;

    private void setupEvents(MemorySegment hSimConnect) throws Throwable {
        SimConnect.mapClientEventToSimEvent(hSimConnect, EVENT_ELEVATOR, "ELEVATOR_SET");
        SimConnect.mapClientEventToSimEvent(hSimConnect, EVENT_AILERON, "AILERON_SET");
        SimConnect.mapClientEventToSimEvent(hSimConnect, EVENT_RUDDER, "RUDDER_SET");
        SimConnect.mapClientEventToSimEvent(hSimConnect, EVENT_THROTTLE, "THROTTLE_SET");

        // Add events to a notification group (required for some transmission cases)
        SimConnect.addClientEventToNotificationGroup(hSimConnect, GROUP_CONTROLS, EVENT_ELEVATOR, 0);
        SimConnect.addClientEventToNotificationGroup(hSimConnect, GROUP_CONTROLS, EVENT_AILERON, 0);
        SimConnect.addClientEventToNotificationGroup(hSimConnect, GROUP_CONTROLS, EVENT_RUDDER, 0);
        SimConnect.addClientEventToNotificationGroup(hSimConnect, GROUP_CONTROLS, EVENT_THROTTLE, 0);

        // Set priority so the simulator processes them immediately
        SimConnect.setNotificationGroupPriority(hSimConnect, GROUP_CONTROLS,
                SimConnect.SIMCONNECT_GROUP_PRIORITY_HIGHEST);
    }

    private static void dispatchCallback(MemorySegment pData, int cbData, MemorySegment pContext) {
        if (instance == null)
            return;

        MemorySegment segment = pData.reinterpret(cbData);
        int dwSize = segment.get(JAVA_INT, 0);
        int dwVersion = segment.get(JAVA_INT, 4);
        int dwID = segment.get(JAVA_INT, 8);

        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("cbData=%d dwSize=%d dwVer=%d dwID=%d Hex: ", cbData, dwSize, dwVersion, dwID));
            for (int i = 0; i < Math.min(cbData, 32); i++) {
                sb.append(String.format("%02X ", segment.get(JAVA_BYTE, i)));
            }
            log.debug(sb.toString());
        }

        if (dwID == SimConnect.SIMCONNECT_RECV_ID_SIMOBJECT_DATA) {
            int expectedSize = 40 + (int) STATE_LAYOUT.byteSize();
            if (dwSize < expectedSize) {
                log.warn("Ignoring SimObject data packet: size {} < expected {}", dwSize, expectedSize);
                return;
            }
            try {
                // Header(12) + Fields(28) = 40 bytes offset to data
                MemorySegment dataSegment = segment.asSlice(40, STATE_LAYOUT.byteSize());
                instance.updateState(dataSegment);
            } catch (Exception e) {
                log.error("Failed to update state from SimConnect data", e);
            }
        } else if (dwID == SimConnect.SIMCONNECT_RECV_ID_OPEN) {
            log.info("SimConnect: Connected to simulator (Version: {})", dwVersion);
        } else if (dwID == SimConnect.SIMCONNECT_RECV_ID_EXCEPTION) {
            int dwException = segment.get(JAVA_INT, 12);
            int dwSendID = segment.get(JAVA_INT, 16);
            int dwIndex = segment.get(JAVA_INT, 20);
            log.error("SimConnect Exception Code: {} at SendID {} index {}", dwException, dwSendID, dwIndex);
        } else if (dwID == SimConnect.SIMCONNECT_RECV_ID_QUIT) {
            log.info("SimConnect: Simulator quit");
            instance.running.set(false);
        }
    }

    private void updateState(MemorySegment data) {
        State newState = new State(
                data.get(JAVA_DOUBLE, 0), // latitude
                data.get(JAVA_DOUBLE, 8), // longitude
                data.get(JAVA_DOUBLE, 16), // heading
                data.get(JAVA_DOUBLE, 24), // altitude
                data.get(JAVA_DOUBLE, 32), // roll
                data.get(JAVA_DOUBLE, 40), // pitch
                data.get(JAVA_DOUBLE, 48), // yaw (actually magnetic heading here)
                data.get(JAVA_DOUBLE, 56), // speed
                data.get(JAVA_DOUBLE, 64), // climb
                data.get(JAVA_DOUBLE, 72) // time
        );
        memory.setState(newState);
    }

    public void setElevator(double value) {
        commandQueue.offer(h -> {
            try {
                int val = (int) (value * 16383);
                SimConnect.transmitClientEvent(h, SimConnect.SIMCONNECT_OBJECT_ID_USER, EVENT_ELEVATOR, val,
                        GROUP_CONTROLS, 0);
            } catch (Throwable t) {
                log.error("Elevator command failed", t);
            }
        });
    }

    public void setAileron(double value) {
        commandQueue.offer(h -> {
            try {
                int val = (int) (value * 16383);
                SimConnect.transmitClientEvent(h, SimConnect.SIMCONNECT_OBJECT_ID_USER, EVENT_AILERON, val,
                        GROUP_CONTROLS, 0);
            } catch (Throwable t) {
                log.error("Aileron command failed", t);
            }
        });
    }

    public void setRudder(double value) {
        commandQueue.offer(h -> {
            try {
                int val = (int) (value * 16383);
                SimConnect.transmitClientEvent(h, SimConnect.SIMCONNECT_OBJECT_ID_USER, EVENT_RUDDER, val,
                        GROUP_CONTROLS, 0);
            } catch (Throwable t) {
                log.error("Rudder command failed", t);
            }
        });
    }

    public void setThrottle(double value) {
        commandQueue.offer(h -> {
            try {
                int val = (int) (value * 16383);
                SimConnect.transmitClientEvent(h, SimConnect.SIMCONNECT_OBJECT_ID_USER, EVENT_THROTTLE, val,
                        GROUP_CONTROLS, 0);
            } catch (Throwable t) {
                log.error("Throttle command failed", t);
            }
        });
    }
}

package com.nativenavj.adapter;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Path;

import static java.lang.foreign.ValueLayout.*;

/**
 * Minimal Project Panama bindings for SimConnect.dll.
 */
public class SimConnect {
        private static final Linker LINKER = Linker.nativeLinker();
        private static final SymbolLookup LOOKUP;

        static {
                // Load SimConnect.dll from the library folder
                Path dllPath = Path.of("library", "SimConnect.dll").toAbsolutePath();
                LOOKUP = SymbolLookup.libraryLookup(dllPath, Arena.global());
        }

        private static final MethodHandle OPEN = LOOKUP.find("SimConnect_Open")
                        .map(symbol -> LINKER.downcallHandle(symbol,
                                        FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS, JAVA_INT, ADDRESS,
                                                        JAVA_INT)))
                        .orElseThrow();

        private static final MethodHandle CLOSE = LOOKUP.find("SimConnect_Close")
                        .map(symbol -> LINKER.downcallHandle(symbol,
                                        FunctionDescriptor.of(JAVA_INT, ADDRESS)))
                        .orElseThrow();

        private static final MethodHandle CALL_DISPATCH = LOOKUP.find("SimConnect_CallDispatch")
                        .map(symbol -> LINKER.downcallHandle(symbol,
                                        FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS)))
                        .orElseThrow();

        private static final MethodHandle ADD_TO_DATA_DEFINITION = LOOKUP.find("SimConnect_AddToDataDefinition")
                        .map(symbol -> LINKER.downcallHandle(symbol,
                                        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS, ADDRESS, JAVA_INT,
                                                        JAVA_FLOAT, JAVA_INT)))
                        .orElseThrow();

        private static final MethodHandle REQUEST_DATA_ON_SIM_OBJECT = LOOKUP.find("SimConnect_RequestDataOnSimObject")
                        .map(symbol -> LINKER.downcallHandle(symbol,
                                        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT,
                                                        JAVA_INT, JAVA_INT,
                                                        JAVA_INT, JAVA_INT)))
                        .orElseThrow();

        private static final MethodHandle MAP_CLIENT_EVENT_TO_SIM_EVENT = LOOKUP
                        .find("SimConnect_MapClientEventToSimEvent")
                        .map(symbol -> LINKER.downcallHandle(symbol,
                                        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS)))
                        .orElseThrow();

        private static final MethodHandle TRANSMIT_CLIENT_EVENT = LOOKUP.find("SimConnect_TransmitClientEvent")
                        .map(symbol -> LINKER.downcallHandle(symbol,
                                        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT,
                                                        JAVA_INT)))
                        .orElseThrow();

        private static final MethodHandle ADD_CLIENT_EVENT_TO_NOTIFICATION_GROUP = LOOKUP
                        .find("SimConnect_AddClientEventToNotificationGroup")
                        .map(symbol -> LINKER.downcallHandle(symbol,
                                        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT, JAVA_INT)))
                        .orElseThrow();

        private static final MethodHandle SET_NOTIFICATION_GROUP_PRIORITY = LOOKUP
                        .find("SimConnect_SetNotificationGroupPriority")
                        .map(symbol -> LINKER.downcallHandle(symbol,
                                        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT)))
                        .orElseThrow();

        // SimConnect Constants
        public static final int SIMCONNECT_UNUSED = -1;
        public static final int SIMCONNECT_OBJECT_ID_USER = 0;
        public static final int SIMCONNECT_PERIOD_SIM_FRAME = 3;
        public static final int SIMCONNECT_PERIOD_SECOND = 4;
        public static final int SIMCONNECT_GROUP_PRIORITY_HIGHEST = 1;

        // Recv IDs
        public static final int SIMCONNECT_RECV_ID_EXCEPTION = 1;
        public static final int SIMCONNECT_RECV_ID_OPEN = 2;
        public static final int SIMCONNECT_RECV_ID_QUIT = 3;
        public static final int SIMCONNECT_RECV_ID_SIMOBJECT_DATA = 8;

        // Data Types
        public static final int SIMCONNECT_DATATYPE_INT32 = 1;
        public static final int SIMCONNECT_DATATYPE_FLOAT64 = 4;

        public static int open(MemorySegment phSimConnect, String szName) throws Throwable {
                try (Arena arena = Arena.ofConfined()) {
                        MemorySegment nameSegment = arena.allocateFrom(szName);
                        return (int) OPEN.invokeExact(phSimConnect, nameSegment, MemorySegment.NULL, 0,
                                        MemorySegment.NULL, 0);
                }
        }

        public static int close(MemorySegment hSimConnect) throws Throwable {
                return (int) CLOSE.invokeExact(hSimConnect);
        }

        public static int callDispatch(MemorySegment hSimConnect, MemorySegment cbDispatch, MemorySegment pContext)
                        throws Throwable {
                return (int) CALL_DISPATCH.invokeExact(hSimConnect, cbDispatch, pContext);
        }

        public static int addToDataDefinition(MemorySegment hSimConnect, int DefineID, String szDatumName,
                        String szUnitsName, int DatumType) throws Throwable {
                try (Arena arena = Arena.ofConfined()) {
                        MemorySegment datumName = arena.allocateFrom(szDatumName);
                        MemorySegment unitsName = szUnitsName != null ? arena.allocateFrom(szUnitsName)
                                        : MemorySegment.NULL;
                        return (int) ADD_TO_DATA_DEFINITION.invokeExact(hSimConnect, DefineID, datumName, unitsName,
                                        DatumType,
                                        0.0f, SIMCONNECT_UNUSED);
                }
        }

        public static int requestDataOnSimObject(MemorySegment hSimConnect, int RequestID, int DefineID, int ObjectID,
                        int Period) throws Throwable {
                return (int) REQUEST_DATA_ON_SIM_OBJECT.invokeExact(hSimConnect, RequestID, DefineID, ObjectID, Period,
                                0, 0, 0,
                                0);
        }

        public static int mapClientEventToSimEvent(MemorySegment hSimConnect, int EventID, String szEventName)
                        throws Throwable {
                try (Arena arena = Arena.ofConfined()) {
                        MemorySegment eventName = arena.allocateFrom(szEventName);
                        return (int) MAP_CLIENT_EVENT_TO_SIM_EVENT.invokeExact(hSimConnect, EventID, eventName);
                }
        }

        public static int transmitClientEvent(MemorySegment hSimConnect, int ObjectID, int EventID, int dwData,
                        int GroupID,
                        int dwFlags) throws Throwable {
                return (int) TRANSMIT_CLIENT_EVENT.invokeExact(hSimConnect, ObjectID, EventID, dwData, GroupID,
                                dwFlags);
        }

        public static int addClientEventToNotificationGroup(MemorySegment hSimConnect, int GroupID, int EventID,
                        int bMaskable) throws Throwable {
                return (int) ADD_CLIENT_EVENT_TO_NOTIFICATION_GROUP.invokeExact(hSimConnect, GroupID, EventID,
                                bMaskable);
        }

        public static int setNotificationGroupPriority(MemorySegment hSimConnect, int GroupID, int uPriority)
                        throws Throwable {
                return (int) SET_NOTIFICATION_GROUP_PRIORITY.invokeExact(hSimConnect, GroupID, uPriority);
        }
}

package com.nativenavj.simconnect;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.*;

public class SimConnectBindings {
        private static final Linker LINKER = Linker.nativeLinker();
        private static final SymbolLookup LOOKUP;

        static {
                // Try to load SimConnect.dll from various locations
                java.nio.file.Path libraryPath = java.nio.file.Paths.get("library", "SimConnect.dll").toAbsolutePath();
                if (!java.nio.file.Files.exists(libraryPath)) {
                        libraryPath = java.nio.file.Paths.get("SimConnect.dll").toAbsolutePath();
                }

                if (java.nio.file.Files.exists(libraryPath)) {
                        LOOKUP = SymbolLookup.libraryLookup(libraryPath, Arena.global());
                } else {
                        // Fallback to system lookup if file not found locally
                        LOOKUP = SymbolLookup.libraryLookup("SimConnect", Arena.global());
                }
        }

        private static final MethodHandle SIMCONNECT_OPEN = LOOKUP.find("SimConnect_Open")
                        .map(symbol -> LINKER.downcallHandle(symbol,
                                        FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS, JAVA_INT, ADDRESS,
                                                        JAVA_INT)))
                        .orElseThrow();

        private static final MethodHandle SIMCONNECT_CLOSE = LOOKUP.find("SimConnect_Close")
                        .map(symbol -> LINKER.downcallHandle(symbol, FunctionDescriptor.of(JAVA_INT, ADDRESS)))
                        .orElseThrow();

        private static final MethodHandle SIMCONNECT_ADD_TO_DATA_DEFINITION = LOOKUP
                        .find("SimConnect_AddToDataDefinition")
                        .map(symbol -> LINKER.downcallHandle(symbol,
                                        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS,
                                                        ADDRESS, JAVA_INT, JAVA_FLOAT, JAVA_INT)))
                        .orElseThrow();

        private static final MethodHandle SIMCONNECT_REQUEST_DATA_ON_SIM_OBJECT = LOOKUP
                        .find("SimConnect_RequestDataOnSimObject")
                        .map(symbol -> LINKER.downcallHandle(symbol,
                                        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT,
                                                        JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT)))
                        .orElseThrow();

        private static final MethodHandle SIMCONNECT_CALL_DISPATCH = LOOKUP.find("SimConnect_CallDispatch")
                        .map(symbol -> LINKER.downcallHandle(symbol,
                                        FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS)))
                        .orElseThrow();

        private static final MethodHandle SIMCONNECT_SET_DATA_ON_SIM_OBJECT = LOOKUP
                        .find("SimConnect_SetDataOnSimObject")
                        .map(symbol -> LINKER.downcallHandle(symbol,
                                        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT,
                                                        JAVA_INT, JAVA_INT, JAVA_INT, ADDRESS)))
                        .orElseThrow();

        private static final MethodHandle SIMCONNECT_MAP_CLIENT_EVENT_TO_SIM_EVENT = LOOKUP
                        .find("SimConnect_MapClientEventToSimEvent")
                        .map(symbol -> LINKER.downcallHandle(symbol,
                                        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS)))
                        .orElseThrow();

        private static final MethodHandle SIMCONNECT_TRANSMIT_CLIENT_EVENT = LOOKUP
                        .find("SimConnect_TransmitClientEvent")
                        .map(symbol -> LINKER.downcallHandle(symbol,
                                        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT,
                                                        JAVA_INT, JAVA_INT, JAVA_INT)))
                        .orElseThrow();

        private static final MethodHandle SIMCONNECT_SUBSCRIBE_TO_SYSTEM_EVENT = LOOKUP
                        .find("SimConnect_SubscribeToSystemEvent")
                        .map(symbol -> LINKER.downcallHandle(symbol,
                                        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS)))
                        .orElseThrow();

        public static int open(MemorySegment phSimConnect, String appName) throws Throwable {
                try (Arena arena = Arena.ofConfined()) {
                        MemorySegment szName = arena.allocateFrom(appName);
                        return (int) SIMCONNECT_OPEN.invokeExact(phSimConnect, szName, MemorySegment.NULL, 0,
                                        MemorySegment.NULL,
                                        0);
                }
        }

        public static int close(MemorySegment hSimConnect) throws Throwable {
                return (int) SIMCONNECT_CLOSE.invokeExact(hSimConnect);
        }

        public static int addToDataDefinition(MemorySegment hSimConnect, int defineId, String datumName,
                        String unitsName,
                        int datumType) throws Throwable {
                try (Arena arena = Arena.ofConfined()) {
                        MemorySegment szDatumName = arena.allocateFrom(datumName);
                        MemorySegment szUnitsName = arena.allocateFrom(unitsName);
                        return (int) SIMCONNECT_ADD_TO_DATA_DEFINITION.invokeExact(hSimConnect, defineId, szDatumName,
                                        szUnitsName,
                                        datumType, 0.0f, -1); // SIMCONNECT_UNUSED = -1
                }
        }

        public static int requestDataOnSimObject(MemorySegment hSimConnect, int requestId, int defineId, int objectId,
                        int period) throws Throwable {
                return (int) SIMCONNECT_REQUEST_DATA_ON_SIM_OBJECT.invokeExact(hSimConnect, requestId, defineId,
                                objectId,
                                period, 0, 0, 0, 0);
        }

        public static int callDispatch(MemorySegment hSimConnect, MemorySegment dispatchStub) throws Throwable {
                return (int) SIMCONNECT_CALL_DISPATCH.invokeExact(hSimConnect, dispatchStub, MemorySegment.NULL);
        }

        public static int setDataOnSimObject(MemorySegment hSimConnect, int defineId, int objectId, int flags,
                        int arrayCount, int unitSize, MemorySegment pDataSet) throws Throwable {
                return (int) SIMCONNECT_SET_DATA_ON_SIM_OBJECT.invokeExact(hSimConnect, defineId, objectId, flags,
                                arrayCount,
                                unitSize, pDataSet);
        }

        public static int mapClientEventToSimEvent(MemorySegment hSimConnect, int eventId, String eventName)
                        throws Throwable {
                try (Arena arena = Arena.ofConfined()) {
                        MemorySegment szEventName = arena.allocateFrom(eventName);
                        return (int) SIMCONNECT_MAP_CLIENT_EVENT_TO_SIM_EVENT.invokeExact(hSimConnect, eventId,
                                        szEventName);
                }
        }

        public static int transmitClientEvent(MemorySegment hSimConnect, int objectId, int eventId, int dwData,
                        int groupId,
                        int flags) throws Throwable {
                return (int) SIMCONNECT_TRANSMIT_CLIENT_EVENT.invokeExact(hSimConnect, objectId, eventId, dwData,
                                groupId,
                                flags);
        }

        public static int subscribeToSystemEvent(MemorySegment hSimConnect, int eventId, String systemEventName)
                        throws Throwable {
                try (Arena arena = Arena.ofConfined()) {
                        MemorySegment szEventName = arena.allocateFrom(systemEventName);
                        return (int) SIMCONNECT_SUBSCRIBE_TO_SYSTEM_EVENT.invokeExact(hSimConnect, eventId,
                                        szEventName);
                }
        }

        public static MemorySegment createDispatchStub(MethodHandle callback, Arena arena) {
                // typedef void (CALLBACK *DispatchProc)(SIMCONNECT_RECV* pData, DWORD cbData,
                // void * pContext);
                FunctionDescriptor desc = FunctionDescriptor.ofVoid(ADDRESS, JAVA_INT, ADDRESS);
                return LINKER.upcallStub(callback, desc, arena);
        }
}

package com.nativenavj.simconnect;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.MemoryLayout.PathElement.groupElement;
import static java.lang.foreign.ValueLayout.JAVA_DOUBLE;

public record TelemetryData(
                double latitude,
                double longitude,
                double altitude,
                double airspeed,
                double heading,
                double bank,
                double pitch,
                double time) {
        public static final StructLayout LAYOUT = MemoryLayout.structLayout(
                        JAVA_DOUBLE.withByteAlignment(1).withName("latitude"),
                        JAVA_DOUBLE.withByteAlignment(1).withName("longitude"),
                        JAVA_DOUBLE.withByteAlignment(1).withName("altitude"),
                        JAVA_DOUBLE.withByteAlignment(1).withName("airspeed"),
                        JAVA_DOUBLE.withByteAlignment(1).withName("heading"),
                        JAVA_DOUBLE.withByteAlignment(1).withName("bank"),
                        JAVA_DOUBLE.withByteAlignment(1).withName("pitch"),
                        JAVA_DOUBLE.withByteAlignment(1).withName("time"));

        private static final VarHandle VH_LATITUDE = LAYOUT.varHandle(groupElement("latitude"));
        private static final VarHandle VH_LONGITUDE = LAYOUT.varHandle(groupElement("longitude"));
        private static final VarHandle VH_ALTITUDE = LAYOUT.varHandle(groupElement("altitude"));
        private static final VarHandle VH_AIRSPEED = LAYOUT.varHandle(groupElement("airspeed"));
        private static final VarHandle VH_HEADING = LAYOUT.varHandle(groupElement("heading"));
        private static final VarHandle VH_BANK = LAYOUT.varHandle(groupElement("bank"));
        private static final VarHandle VH_PITCH = LAYOUT.varHandle(groupElement("pitch"));
        private static final VarHandle VH_TIME = LAYOUT.varHandle(groupElement("time"));

        public static TelemetryData fromMemory(MemorySegment segment) {
                return new TelemetryData(
                                (double) VH_LATITUDE.get(segment, 0L),
                                (double) VH_LONGITUDE.get(segment, 0L),
                                (double) VH_ALTITUDE.get(segment, 0L),
                                (double) VH_AIRSPEED.get(segment, 0L),
                                (double) VH_HEADING.get(segment, 0L),
                                (double) VH_BANK.get(segment, 0L),
                                (double) VH_PITCH.get(segment, 0L),
                                (double) VH_TIME.get(segment, 0L));
        }
}

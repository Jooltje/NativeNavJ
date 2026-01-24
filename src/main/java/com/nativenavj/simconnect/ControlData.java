package com.nativenavj.simconnect;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.MemoryLayout.PathElement.groupElement;
import static java.lang.foreign.ValueLayout.JAVA_DOUBLE;

public record ControlData(
        double aileron,
        double elevator,
        double rudder,
        double throttle) {
    public static final StructLayout LAYOUT = MemoryLayout.structLayout(
            JAVA_DOUBLE.withByteAlignment(1).withName("aileron"),
            JAVA_DOUBLE.withByteAlignment(1).withName("elevator"),
            JAVA_DOUBLE.withByteAlignment(1).withName("rudder"),
            JAVA_DOUBLE.withByteAlignment(1).withName("throttle"));

    private static final VarHandle VH_AILERON = LAYOUT.varHandle(groupElement("aileron"));
    private static final VarHandle VH_ELEVATOR = LAYOUT.varHandle(groupElement("elevator"));
    private static final VarHandle VH_RUDDER = LAYOUT.varHandle(groupElement("rudder"));
    private static final VarHandle VH_THROTTLE = LAYOUT.varHandle(groupElement("throttle"));

    public void toMemory(MemorySegment segment) {
        VH_AILERON.set(segment, 0L, aileron);
        VH_ELEVATOR.set(segment, 0L, elevator);
        VH_RUDDER.set(segment, 0L, rudder);
        VH_THROTTLE.set(segment, 0L, throttle);
    }
}

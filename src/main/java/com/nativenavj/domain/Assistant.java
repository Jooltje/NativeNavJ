package com.nativenavj.domain;

/**
 * Assistant status.
 */
public record Assistant(
        boolean active,
        String status) {
    /**
     * Creates an inactive assistant.
     */
    public static Assistant inactive() {
        return new Assistant(false, "IDLE");
    }
}

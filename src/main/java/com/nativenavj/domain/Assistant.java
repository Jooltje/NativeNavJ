package com.nativenavj.domain;

/**
 * Assistant status.
 */
public record Assistant(
        boolean activity,
        Status status,
        String prompt) {

    public enum Status {
        IDLE,
        THINKING,
        ERROR
    }

    /**
     * Creates an inactive assistant.
     */
    public static Assistant inactive() {
        return new Assistant(false, Status.IDLE, "");
    }
}

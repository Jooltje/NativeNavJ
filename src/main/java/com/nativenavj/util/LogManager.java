package com.nativenavj.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogManager {
    private static final String TELEMETRY_FILE = "telemetry.log";
    private static final String CONTROL_FILE = "control.log";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private static PrintWriter telemetryWriter;
    private static PrintWriter controlWriter;

    static {
        try {
            telemetryWriter = new PrintWriter(new FileWriter(TELEMETRY_FILE, true), true);
            controlWriter = new PrintWriter(new FileWriter(CONTROL_FILE, true), true);
        } catch (IOException e) {
            System.err.println("Failed to initialize LogManager: " + e.getMessage());
        }
    }

    public static void logTelemetry(String message) {
        if (telemetryWriter != null) {
            telemetryWriter.println("[" + getTimestamp() + "] " + message);
        }
    }

    public static void logControl(String message) {
        if (controlWriter != null) {
            controlWriter.println("[" + getTimestamp() + "] " + message);
        }
    }

    public static void info(String message) {
        System.out.println("[INFO] " + message);
        logControl("INFO: " + message);
    }

    public static void warn(String message) {
        System.out.println("[WARN] " + message);
        logControl("WARN: " + message);
    }

    public static void error(String message) {
        System.err.println("[ERROR] " + message);
        logControl("ERROR: " + message);
    }

    public static void error(String message, Throwable t) {
        System.err.println("[ERROR] " + message);
        t.printStackTrace();
        if (controlWriter != null) {
            controlWriter.println("[" + getTimestamp() + "] ERROR: " + message);
            t.printStackTrace(controlWriter);
        }
    }

    private static String getTimestamp() {
        return LocalDateTime.now().format(FORMATTER);
    }

    public static void close() {
        if (telemetryWriter != null)
            telemetryWriter.close();
        if (controlWriter != null)
            controlWriter.close();
    }
}

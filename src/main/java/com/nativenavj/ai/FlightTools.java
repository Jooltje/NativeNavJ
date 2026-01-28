package com.nativenavj.ai;

import com.nativenavj.domain.Goal;
import com.nativenavj.domain.Memory;
import com.nativenavj.domain.Navigator;
import com.nativenavj.domain.Shell;
import dev.langchain4j.agent.tool.Tool;

/**
 * AI-accessible tools for controlling the flight system.
 */
public class FlightTools {

    private final Shell shell;
    private final Memory memory;

    public FlightTools(Shell shell, Memory memory) {
        this.shell = shell;
        this.memory = memory;
    }

    @Tool("Sets the target heading in degrees (0-359).")
    public String setHeading(double heading) {
        return shell.execute("HDG " + heading);
    }

    @Tool("Sets the target altitude in feet.")
    public String setAltitude(double altitude) {
        return shell.execute("ALT " + altitude);
    }

    @Tool("Sets the target airspeed in knots.")
    public String setSpeed(double speed) {
        return shell.execute("SPD " + speed);
    }

    @Tool("Enables or disables the autonomous flight system. Use 'ON' or 'OFF'.")
    public String setSystem(String status) {
        return shell.execute("SYS " + status);
    }

    @Tool("Gets the current status of the flight system and target parameters.")
    public String getStatus() {
        Navigator navigator = memory.getNavigator();
        Goal goal = memory.getGoal();
        return String.format("System: %s, Target: ALT=%.0fft HDG=%.0fdeg SPD=%.0fkts",
                navigator.status() ? "ACTIVE" : "INACTIVE",
                goal.height(),
                goal.direction(),
                goal.velocity());
    }
}

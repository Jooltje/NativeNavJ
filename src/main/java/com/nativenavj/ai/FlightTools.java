package com.nativenavj.ai;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import com.nativenavj.domain.Shell;
import com.nativenavj.safety.SafetyGuardrails;

public class FlightTools {
    private final Shell shell;
    private final SafetyGuardrails safety;

    public FlightTools(Shell shell, SafetyGuardrails safety) {
        this.shell = shell;
        this.safety = safety;
    }

    @Tool("Sets the target heading for the aircraft. Heading should be between 0 and 360 degrees.")
    public String setHeading(@P("The desired heading in degrees") double heading) {
        double validHeading = safety.validateHeading(heading);
        return shell.execute("HDG " + validHeading);
    }

    @Tool("Sets the target altitude for the aircraft in feet.")
    public String setAltitude(@P("The desired altitude in feet") double altitude) {
        double validAltitude = safety.validateAltitude(altitude, 0.0);
        return shell.execute("ALT " + validAltitude);
    }

    @Tool("Sets the target airspeed for the aircraft in knots.")
    public String setAirspeed(@P("The desired airspeed in knots") double airspeed) {
        double validAirspeed = safety.validateAirspeed(airspeed);
        return shell.execute("SPD " + validAirspeed);
    }

    public String getStatus() {
        var navigator = shell.getComputer().getNavigator();
        var goal = shell.getComputer().getGoal();

        return String.format("System: %s, Target: ALT=%.0fft HDG=%.0fdeg SPD=%.0fkts",
                navigator.active() ? "ACTIVE" : "INACTIVE",
                goal.altitude(),
                goal.heading(),
                goal.speed());
    }

    @Tool("Maintains the current flight state (heading, altitude, and airspeed). Engages all autonomous controls at current values.")
    public String maintainCurrentFlight() {
        return shell.execute("SYS ON");
    }

    @Tool("Disables all autonomous flight controls and returns the aircraft to manual control.")
    public String disableAll() {
        return shell.execute("SYS OFF");
    }

    /**
     * Gets the Shell instance for direct access.
     */
    public Shell getShell() {
        return shell;
    }
}

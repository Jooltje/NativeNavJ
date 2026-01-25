package com.nativenavj.ai;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import com.nativenavj.domain.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tools available to the AI Assistant for flight control.
 */
public class FlightTools {
    private static final Logger log = LoggerFactory.getLogger(FlightTools.class);

    private final Shell shell;

    public FlightTools(Shell shell) {
        this.shell = shell;
    }

    @Tool("Sets the target heading for the aircraft. Heading should be between 0 and 360 degrees.")
    public String setHeading(@P("The desired heading in degrees") double heading) {
        log.info("AI Tool: Setting heading to {} degrees", heading);
        return shell.execute("HDG " + heading);
    }

    @Tool("Sets the target altitude for the aircraft in feet.")
    public String setAltitude(@P("The desired altitude in feet") double altitude) {
        log.info("AI Tool: Setting altitude to {} ft", altitude);
        return shell.execute("ALT " + altitude);
    }

    @Tool("Sets the target airspeed for the aircraft in knots.")
    public String setAirspeed(@P("The desired airspeed in knots") double airspeed) {
        log.info("AI Tool: Setting speed to {} kts", airspeed);
        return shell.execute("SPD " + airspeed);
    }

    @Tool("Activates the autonomous flight control system")
    public String activateSystem() {
        log.info("AI Tool: Activating system");
        return shell.execute("SYS ON");
    }

    @Tool("Deactivates the autonomous flight control system")
    public String deactivateSystem() {
        log.info("AI Tool: Deactivating system");
        return shell.execute("SYS OFF");
    }

    public String getStatus() {
        var computer = shell.getComputer();
        var navigator = computer.getNavigator();
        var goal = computer.getGoal();

        return String.format("System: %s, Target: ALT=%.0fft HDG=%.0fdeg SPD=%.0fkts",
                navigator.active() ? "ACTIVE" : "INACTIVE",
                goal.altitude(),
                goal.heading(),
                goal.speed());
    }

    public Shell getShell() {
        return shell;
    }
}

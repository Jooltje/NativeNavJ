package com.nativenavj.ai;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
// import com.nativenavj.control.FlightController; // REMOVED - use Coordinator
import com.nativenavj.safety.SafetyGuardrails;

public class FlightTools {
    private final Object controller; // TODO: Update to use Coordinator
    private final SafetyGuardrails safety;

    public FlightTools(Object controller, SafetyGuardrails safety) {
        this.controller = controller;
        this.safety = safety;
    }

    @Tool("Sets the target heading for the aircraft. Heading should be between 0 and 360 degrees.")
    public String setHeading(@P("The desired heading in degrees") double heading) {
        if (controller == null)
            return "Flight controller not available";
        double validHeading = safety.validateHeading(heading);
        // TODO: Update to use Coordinator.setGoal()
        // controller.setTargetHeading(validHeading);
        return "Heading set to " + validHeading + " degrees (controller disabled).";
    }

    @Tool("Sets the target altitude for the aircraft in feet.")
    public String setAltitude(@P("The desired altitude in feet") double altitude) {
        if (controller == null)
            return "Flight controller not available";
        double validAltitude = safety.validateAltitude(altitude, 0.0);
        // TODO: Update to use Coordinator.setGoal()
        // controller.setTargetAltitude(validAltitude);
        return "Altitude set to " + validAltitude + " feet (controller disabled).";
    }

    @Tool("Sets the target airspeed for the aircraft in knots.")
    public String setAirspeed(@P("The desired airspeed in knots") double airspeed) {
        if (controller == null)
            return "Flight controller not available";
        double validAirspeed = safety.validateAirspeed(airspeed);
        // TODO: Update to use Coordinator.setGoal()
        // controller.setTargetAirspeed(validAirspeed);
        return "Airspeed set to " + validAirspeed + " knots (controller disabled).";
    }

    @Tool("Returns the current status of the flight controls.")
    public String getStatus() {
        if (controller == null)
            return "Flight controller not available";
        return "Status: Controller disabled (TODO: Update to use Coordinator)";
    }

    @Tool("Maintains the current flight state (heading, altitude, and airspeed). Engages all autonomous controls at current values.")
    public String maintainCurrentFlight() {
        if (controller == null)
            return "Flight controller not available";
        // TODO: Update to use Coordinator
        // controller.engageAll();
        return "Maintain flight state (controller disabled).";
    }

    @Tool("Disables all autonomous flight controls and returns the aircraft to manual control.")
    public String disableAll() {
        if (controller == null)
            return "Flight controller not available";
        // TODO: Update to use Coordinator
        // controller.disableAll();
        return "All controls disabled (controller disabled).";
    }
}

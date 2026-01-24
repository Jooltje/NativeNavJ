package com.nativenavj.ai;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import com.nativenavj.control.FlightController;
import com.nativenavj.safety.SafetyGuardrails;

public class FlightTools {
    private final FlightController controller;
    private final SafetyGuardrails safety;

    public FlightTools(FlightController controller, SafetyGuardrails safety) {
        this.controller = controller;
        this.safety = safety;
    }

    @Tool("Sets the target heading for the aircraft. Heading should be between 0 and 360 degrees.")
    public String setHeading(@P("The desired heading in degrees") double heading) {
        double validHeading = safety.validateHeading(heading);
        controller.setTargetHeading(validHeading);
        return "Heading set to " + validHeading + " degrees.";
    }

    @Tool("Sets the target altitude for the aircraft in feet.")
    public String setAltitude(@P("The desired altitude in feet") double altitude) {
        // In a real scenario, we'd pass ground altitude to safety
        double validAltitude = safety.validateAltitude(altitude, 0.0);
        controller.setTargetAltitude(validAltitude);
        return "Altitude set to " + validAltitude + " feet.";
    }

    @Tool("Sets the target airspeed for the aircraft in knots.")
    public String setAirspeed(@P("The desired airspeed in knots") double airspeed) {
        double validAirspeed = safety.validateAirspeed(airspeed);
        controller.setTargetAirspeed(validAirspeed);
        return "Airspeed set to " + validAirspeed + " knots.";
    }

    @Tool("Returns the current status of the flight controls.")
    public String getStatus() {
        return String.format("Current Targets: Heading=%.1f deg, Altitude=%.1f ft, Airspeed=%.1f kts",
                controller.getTargetHeading(), controller.getTargetAltitude(), controller.getTargetAirspeed());
    }

    @Tool("Maintains the current flight state (heading, altitude, and airspeed). Engages all autonomous controls at current values.")
    public String maintainCurrentFlight() {
        controller.engageAll();
        return "Maintaining current flight state. All autonomous holds (Heading, Altitude, Airspeed) engaged at current values.";
    }

    @Tool("Disables all autonomous flight controls and returns the aircraft to manual control.")
    public String disableAll() {
        controller.disableAll();
        return "All autonomous flight controls have been disabled. You have full manual control.";
    }
}

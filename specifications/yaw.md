# Yaw

The Yaw Controller is a specific implementation of the **Controller** responsible for managing the aircraft's heading and turn coordination.

## State

### Memory

This object is a reference to the blackboard.

### Actuator

This object is used to talk to the simulator (Connector).

## Behavior

* It targets the `yaw` (or coordinated turn setpoint) defined in the **Target** object on the Blackboard.
* It reads the current `yaw` or slip indicator from the **State** object on the Blackboard.
* It calculates the rudder deflection command using the PID algorithm.
* It writes the command to the **Actuator**.

## Concurrency

The Yaw Controller operates in its own thread managed by the **Orchestrator**.

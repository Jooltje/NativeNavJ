# Throttle

The Throttle Controller is a specific implementation of the **Controller** responsible for managing the aircraft's engine output.

## State

### Memory

This object is a reference to the blackboard.

### Actuator

This object is used to talk to the simulator (Connector).

## Behavior

* It targets the `throttle` value defined in the **Target** object on the Blackboard.
* It calculates the throttle lever position using the PID algorithm (or direct mapping if simple).
* It writes the command to the **Actuator**.

## Concurrency

The Throttle Controller operates in its own thread managed by the **Orchestrator**.

# Pitch

The Pitch Controller is a specific implementation of the **Controller** responsible for managing the aircraft's pitch angle.

## State

### Memory

This object is a reference to the blackboard.

### Actuator

This object is used to talk to the simulator (Connector).

## Behavior

* It targets the `pitch` value defined in the **Target** object on the Blackboard.
* It reads the current `pitch` from the **State** object on the Blackboard.
* It calculates the elevator deflection command using the PID algorithm.
* It writes the command to the **Actuator**.

## Concurrency

The Pitch Controller operates in its own thread managed by the **Orchestrator**.

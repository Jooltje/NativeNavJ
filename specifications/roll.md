# Roll

The Roll Controller is a specific implementation of the **Controller** responsible for managing the aircraft's bank angle.

## State

### Memory

This object is a reference to the blackboard.

### Actuator

This object is used to talk to the simulator (Connector).

## Behavior

* It targets the `roll` value defined in the **Target** object on the Blackboard.
* It reads the current `roll` from the **State** object on the Blackboard.
* It calculates the aileron deflection command using the PID algorithm.
* It writes the command to the **Actuator**.

## Concurrency

The Roll Controller operates in its own thread managed by the **Orchestrator**.

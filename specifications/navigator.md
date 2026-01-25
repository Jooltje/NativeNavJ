# Navigator

The Navigator is a status container in the **Memory** that controls the execution of autonomous flight logic.

## State

### Active

A boolean flag indicating if the autonomous control system is enabled.

### Mode

A string description of the current control mode (e.g., "MANUAL", "AUTONOMOUS").

## Behavior

* It acts as a status indicator for the **Orchestrator**.
* It is updated by the **Shell** in response to user commands (e.g., `SYS ON`, `SYS OFF`).

## Concurrency

The Navigator object is immutable and accessed by multiple threads.

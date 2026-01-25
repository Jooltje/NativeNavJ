# Sensor

The Sensor is a **Knowledge Source** responsible for gathering telemetry from the simulator.

## State

### Memory

This object is a reference to the blackboard.

### Connector

The object used to talk to the simulator.

## Behavior

* It polls the **Connector** for current aircraft parameters.
* It updates the central state from telemetry.
* It translates raw telemetry into an immutable `State` object.
* It updates the `State` in the `Memory`.
* It runs periodically in its own thread via the `Loop` class.

## Concurrency

The Sensor operates in its own thread. It must update `Memory` in a thread-safe manner.

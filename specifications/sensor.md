# Sensor

The Sensor is a **Knowledge Source** responsible for gathering telemetry from the simulator via the Connector.

## Role

It polls the **Connector** for current aircraft parameters and updates the central state.

## Behavior

* It reads telemetry data from the flight simulator via the **Connector**.
* It translates raw telemetry into an immutable `State` object.
* It updates the `State` in the `Memory` (Blackboard).
* It runs periodically in its own thread via the `Loop` class.


## Concurrency

The Sensor operates in its own thread. It must update `Memory` in a thread-safe manner.

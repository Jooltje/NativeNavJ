# Sensor

The Sensor is a **Port** responsible for providing single-value samples from the simulator.

## State

There are no state requirements for the port interface.

## Behavior

* **getSample**: Returns a **Sample** object containing the current value and the simulator time.

## Concurrency

Implementations of this port must be thread-safe.

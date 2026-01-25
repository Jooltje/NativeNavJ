# Connector

The Connector is the low-level bridge between the application and Microsoft Flight Simulator 2020.

## Role

It handles the raw communication with the simulator using **Project Panama** (Java Foreign Function & Memory API). It serves as both the data provider for the Sensor and the command executor for the Controllers.

## Behavior

* It wraps the SimConnect C API calls.
* It manages the native memory segments and dispatch loop for SimConnect.
* It provides methods to request specific telemetry data.
* It provides methods to set simulation variables (ailerons, elevator, rudder, throttle).
* It handles connection Lifecycle (Connect/Disconnect).

## Concurrency

The Connector is accessed by the Sensor thread (for reading) and the Controllers thread (for writing). It must handle potential concurrency issues or be designed for multi-threaded access to the underlying SimConnect handle.

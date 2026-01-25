# Connector

The Connector is the low-level bridge between the application and Microsoft Flight Simulator 2020. 

## State

### SimConnect

The native connection handle to the flight simulator.

## Behavior

* It handles the raw communication with the simulator using **Project Panama** (Java Foreign Function & Memory API).
* It serves as both the data provider for the Sensor and the implementation of the **Actuator** for the Controllers.
* It wraps the SimConnect C API calls.
* It manages the native memory segments and dispatch loop for SimConnect.
* It provides methods to request specific telemetry data.
* It provides methods to set simulation variables (ailerons, elevator, rudder, throttle).
* It handles connection Lifecycle (Connect/Disconnect).

## Concurrency

The Connector is accessed by multiple Knowledge Source threads. It must handle multi-threaded access to the underlying SimConnect handle.

# Connector

The object that talks to the simulator. It uses `SimConnect.dll` through the **Project Panama** (Foreign Function & Memory API).

## State

**memory**: The object that stores the shared state. (Type: Memory)

## Behavior

* It sets up the connection to the simulator using a **Single Handler Thread**.
* It implements a **Non-Blocking Dispatcher** to poll for simulator messages.
* It stores the required telemetry data from the simulator in the **Memory** object via high-speed native memory copying.
* It transmits control commands (Throttle, Aileron, Elevator, Rudder) to the simulator.

## Concurrency

The Connector follows a thread-isolation model. Only a single dedicated "Handler" thread interacts with the SimConnect API. Other components interact with the Connector by reading the shared `Memory` or by queuing commands for the Handler thread to process.

# Connector

The object that talks to the simulator. It uses the SimConnect.ddl through the **Project Panama**

## State

**memory**: The object that stores the shared state. (Type: Memory)

## Behavior

* It sets up the connection to the simulator.
* It sets up a data request at a specific interval.
* It receives data form the simulator and stores it in the **Memory** object.
* It sends data to the simulator to control the plane. (Throttle, Aileron, Elevator, Rudder)

## Concurrency

The Connector needs to process data from the simulator. It must handle multi-threaded access to the simulator.

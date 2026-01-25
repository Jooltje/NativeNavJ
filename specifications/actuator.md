# Actuator

The Actuator is an interface defining the low-level control operations for the aircraft's control surfaces.

## Behavior

* It provides methods to set the position of primary control surfaces:
    * `setAileron(double)`: [-1.0, 1.0]
    * `setElevator(double)`: [-1.0, 1.0]
    * `setRudder(double)`: [-1.0, 1.0]
    * `setThrottle(double)`: [0.0, 1.0]
* These methods are implemented by the **Connector**.
* It serves as the output port for all low-level **Controllers**.

## Concurrency

The Actuator is accessed by multiple controller threads and must ensure thread-safe interaction with the underlying simulator connection.

# Controller

The object is an abstract proportional-integral-derivative (PID) controller.

## State

**memory**: The object that stores the shared state. (Type: Memory)
**configuration**: The object that stores the configuration of the PID controller. (Type: Configuration)
**sensor**: The port used to read current values. (Type: Sensor)
**actuator**: The port used to send control commands. (Type: Actuator)

## Behavior

* It calculates the controller value based on the configuration.
* It clamps the integral sum to prevent windup.
* It clamps the output value to the limits defined in the configuration.
* It writes the output value to the **actuator**.

## Concurrency

The Controller operates in its own thread. It must interact with **Memory** in a thread-safe manner.

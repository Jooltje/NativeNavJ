# Controller

The object is an abstract proportional-integral-derivative (PID) controller.

## State

**configuration**: The object that stores the configuration of the PID controller. (Type: Configuration)
**objective**: The port used to retrieve the target setpoint. (Type: Objective)
**sensor**: The port used to read current values. (Type: Sensor)
**actuator**: The port used to send control commands. (Type: Actuator)
**previous**: The previous sample. (Type: Sample)
**sum**: The integral sum. (Type: double)


## Behavior

* It implements the `Runnable` interface to support periodic execution.
* It read the target value from the **objective**.
* It read the current sample from the **sensor**.
* It caluclates the controll value based on the configuration.
* It clamps the output value to the limits defined in the configuration.
* It writes the output value to the **actuator**.

## Concurrency

The Controller is executed by an external scheduler (e.g., `ScheduledExecutorService`). It must interact with its ports (`Objective`, `Sensor`, `Actuator`) in a thread-safe manner.

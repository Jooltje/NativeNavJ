# Controller

The Controller is an abstract **Knowledge Source** responsible for low-level surface actuation using a Proportional-Integral-Derivative (PID) algorithm.

## State

### Memory

This object is a reference to the blackboard.

### Actuator

This object is used to talk to the simulator (Connector).

### Kp

The proportional gain constant.

### Ki

The integral gain constant.

### Kd

The derivative gain constant.

### Integral

The accumulated error over time.

### Previous Error

The error from the last execution step.

## Behavior

* It reads the target value and current state from the **Memory**.
* It calculates the error between the target and the current state.
* It calculates the proportional term: `Kp * error`.
* It calculates the integral term: `Ki * (error * dt)`.
* It calculates the derivative term: `Kd * ((error - previousError) / dt)`.
* It calculates the command: `proportional + integral + derivative`.
* It writes the resulting command to the **Actuator**.
* It runs periodically in its own thread via the **Loop** class.

## Concurrency

The Controller operates in its own thread. It must interact with **Memory** in a thread-safe manner.

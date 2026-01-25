# Controllers

The Controllers are a **Knowledge Source** responsible for low-level surface actuation via the Connector.

## Role

They translate intermediate **Targets** and current aircraft **State** into raw control commands sent to the simulator through the **Connector**.

## Behavior

* They read `Target` and `State` from the `Memory` (Blackboard).
* They use PID controllers to calculate required surface deflections.
* They write these commands to the **Connector** (which acts as the Actuator).
* They run periodically in their own thread via the `Loop` class.


## Concurrency

The Controllers operate in their own thread. They must interact with `Memory` in a thread-safe manner.

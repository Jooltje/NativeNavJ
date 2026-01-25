# Computer

The Computer is a **Knowledge Source** responsible for high-level flight logic and energy management (TECS).

## State

### Memory

This object is a reference to the blackboard.

## Behavior

* It translates high-level **Goals** and current aircraft **State** into intermediate **Targets** for the low-level controllers.
* It reads `Goal` and `State` from the `Memory`.
* It calculates the required energy distribution and management.
* It writes the resulting `Target` back to the `Memory`.
* It runs periodically in its own thread via the `Loop` class.

## Concurrency

The Computer operates in its own thread. It must interact with `Memory` in a thread-safe manner, using atomic operations or immutable data objects.

# Loop

The Loop class is a frequency-managed execution wrapper for Knowledge Sources.

## State

### Knowledge Source

The task that is executed in each loop iteration.

### Frequency

The frequency (Hz) at which the task is executed.

## Behavior

* It executes a Knowledge Source at a fixed frequency in its own thread.
* It is managed by the **Orchestrator**.
* It starts a new thread for the Knowledge Source.
* It executes the Knowledge Source's task at the specified interval.
* It handles clean shutdown of the thread.
* It logs errors encountered during execution.

## Concurrency

The Loop class must manage thread lifecycle and ensure that Knowledge Source execution is appropriately periodic and independent.

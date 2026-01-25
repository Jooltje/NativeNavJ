# Loop

The Loop class is responsible for executing a Knowledge Source at a fixed frequency in its own thread.

## Behavior

* It starts a new thread for the Knowledge Source.
* It executes the Knowledge Source's task at the specified interval.
* It handles clean shutdown of the thread.
* It logs errors encountered during execution.

## Concurrency

The Loop class must manage thread lifecycle and ensure that Knowledge Source execution is appropriately periodic and independent.

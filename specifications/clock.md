# Clock

The Clock is a port that abstracts time for the application, enabling deterministic testing.

## Behavior

* It provides the current system time in nanoseconds via `nanoTime()`.
* It provides a mechanism to pause execution for a specified duration via `sleep(nanos)`.
* It allows for mocked time implementations during unit testing.

## Concurrency

The Clock is accessed by all **Loop** based threads and must handle multi-threaded access.

# Actuator

The Actuator is an interface defining the low-level control operations for the aircraft's control surfaces.

## Behavior

* It sets a signal

## Concurrency

The Actuator is accessed by multiple controller threads and must ensure thread-safe interaction with the underlying simulator connection.

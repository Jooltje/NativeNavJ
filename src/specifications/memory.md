# Memory

This is used to store the shared state for the whole application.

## Structure

### Goal

This contains the goals for for the computer to achieve.

### State

The current state of the aircraft.

### Navigator

The current status of the control system.

### Assistant

The current status of the assistant.

## Behavior

* When values in the memory changes, the value is logged with debug level

## Concurrency

The memory is accessed by multiple threads, so it must be thread-safe.

The user can update the goal
The assistant can update the goal
The computer can update the goal
The sensor can update the state
The computer can read the state
The controllers can read the state

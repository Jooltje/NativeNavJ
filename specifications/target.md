# Target

The Target is a container for intermediate control objectives.

## State

### roll

The target bank angle in degrees.

### pitch

The target pitch angle in degrees.

### yaw

The target yaw angle in degrees.

### throttle

The target throttle position in percentage (0.0 to 1.0).

## Behavior

There are no requirements.

## Concurrency

This object is used by multiple threads, so it must be **immutable**.

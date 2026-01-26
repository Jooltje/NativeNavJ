# Configuration

This object contains the configuration for a controller.

# State

**active**: A flag indicating if the controller is active. (Type: boolean)
**frequency**: The frequency at which the controller runs. (Type: double, Unit: Hz)
**proportional**: The non-negative coefficient for the proportional term. (Type: double)
**integral**: The non-negative coefficient for the integral term. (Type: double)
**derivative**: The non-negative coefficient for the derivative term. (Type: double)

We create 3 constants for the configuration: 

**GUIDANCE**: The default configuration for a guidance controller. (10, 1.0, 0.0, 0.0)
**SURFACE**: The default configuration for a surface controller. (20, 0.1, 0.0, 0.0)
**THROTTLE**: The default configuration for a throttle controller. (5, 0.05, 0.01, 0.0)

# Behavior

This object does not have any behavior.

# Concurrency

This object should be thread-safe. This object is immutable.

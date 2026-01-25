# Orchestrator

The Orchestrator is the central coordinator of the application.

## State

### Memory

This object is used as the blackboard in the application.

### Connector

The object used to talk to the simulator.

### Knowledge sources

The resources that it controls: Sensor, Computer, Roll, Pitch, Yaw, Throttle.


## Behavior

* It is responsible for initializing the system and managing the lifecycle of all components.
* It replaces the legacy management logic and serves as the entry point of the application.
* **Initialization**: Sets up the Blackboard and the Connector.
* **Management**: Configures and manages the lifecycle (start/stop) of Knowledge Source threads.
* **Control**: Dispatches user commands to the appropriate resources.

## Concurrency

The Orchestrator runs on the main thread and manages the creation and termination of background Knowledge Source threads.

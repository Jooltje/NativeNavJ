# Orchestrator
 
The Orchestrator is the central coordinator of the application.
 
## State
 
### Memory
 
This object is used as the blackboard in the application.
 
### Connector
 
The object used to talk to the simulator.
 
### Knowledge sources
 
The resources that it controls: Sensor, Computer, Roll, Pitch, Yaw, Throttle, Shell.
 
### Scheduler
 
A `ScheduledExecutorService` used to manage the execution of all `Loop` objects.
 
## Behavior
 
* It is responsible for initializing the system and managing the lifecycle of all components.
* It replaces the legacy management logic and serves as the entry point of the application.
* **Initialization**: Sets up the Blackboard, the Connector, and the **Scheduler** thread pool.
* **Management**: Submits `Loop` tasks to the **Scheduler** to run at fixed rates.
* **Navigation Control**: Monitors the **Navigator** status on the Blackboard. It schedules the **Computer** and **Controllers** loops when the system is activated and cancels them when deactivated.
 
## Concurrency
 
The Orchestrator manages a centralized thread pool via `ScheduledExecutorService`, ensuring efficient resource usage and coordinated lifecycle management of background Knowledge Source tasks.

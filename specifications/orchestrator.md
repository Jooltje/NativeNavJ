# Orchestrator
 
The Orchestrator is the central coordinator of the application.
 
## State
 
### Memory
 
This object is used as the blackboard in the application.
 
### Connector
 
The object used to talk to the simulator.
 
### Components
 
The resources that it controls: Computer, Roll, Pitch, Yaw, Throttle, Shell, Assistant.
 
### Scheduler
 
A `ScheduledExecutorService` used to manage the execution of all components.
 
## Behavior
 
* It is responsible for initializing the system and managing the lifecycle of all components.
* **Initialization**: Sets up the Blackboard (Memory), the Connector, and the **Scheduler** thread pool.
* **Component Setup**: Initializes specialized components (Computer, Shell, Assistant) and flight controllers.
* **Port Injection**: Injects the necessary ports (`Objective`, `Sensor`, `Actuator`) into each component. For controllers, it uses lambdas to bridge the generic `Actuator` port to specialized `Connector` methods.
* **Scheduling**: Submits `Runnable` tasks (Controllers, Assistant) and `Loop` tasks (Computer, Shell) to the **Scheduler** to run at fixed rates.
 
## Concurrency
 
The Orchestrator manages a centralized thread pool via `ScheduledExecutorService`, ensuring efficient resource usage and coordinated lifecycle management of background tasks.

# Loop
 
The Loop class is a frequency-managed execution wrapper for Knowledge Sources using a **ScheduledExecutorService**.
 
## State
 
### Knowledge Source
 
The task that is executed in each loop iteration. Implementing `Runnable`.
 
### Frequency
 
The frequency (Hz) at which the task is executed.
 
## Behavior
 
* It executes a Knowledge Source at a fixed rate using `ScheduledExecutorService.scheduleAtFixedRate`.
* It is managed by the **Orchestrator**.
* It ensures precise periodic execution without drift accumulation.
* It handles clean shutdown by canceling the scheduled task.
* It logs errors encountered during execution.
 
## Concurrency
 
The Loop class leverages the threading pool provided by the `ScheduledExecutorService` to ensure independent and thread-safe execution of Knowledge Sources.

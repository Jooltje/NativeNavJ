# Loop
 
The Loop record is a data object storing the execution state of system components.
 
## State

- Active: boolean
- Frequency: double (Hz)

## Behaviour

- Records the execution state for a specific component.
- Used by the Orchestrator to manage thread scheduling.
- Stored in Memory and can be initialized or updated via Shell commands.
 
## Concurrency
 
Thread-safe access is ensured by the `Memory` component using `ConcurrentHashMap`.

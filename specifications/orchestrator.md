# Orchestrator

The Orchestrator is the central coordinator of the application, responsible for initializing the system and managing the lifecycle of all components. It replaces the legacy management logic and serves as the entry point of the application. Its primary responsibility is to set up the **Blackboard** (Memory), the **Connector**, and all **Knowledge Sources** (KS).

## Behavior

*   **Initialization**: 
    *   Creates the `Memory` instance.
    *   Initializes the `Connector` with its dependencies.
    *   Instantiates and configures all Knowledge Sources (`Sensor`, `Computer`, `Controllers`).
    *   Sets up the `Shell` and `CognitiveOrchestrator` for user interaction.
*   **Lifecycle Management**:
    *   Starts the execution loops for all Knowledge Sources.
    *   Handles clean shutdown and disconnection from the simulator.
*   **User Interface**:
    *   Manages the main command loop (CLI) and routes user input to the `Shell` or `CognitiveOrchestrator`.

## Concurrency

The Orchestrator runs the main thread. It is responsible for spawning and managing the threads used by the Knowledge Sources. It ensures that all components are properly synchronized through the central `Memory`.

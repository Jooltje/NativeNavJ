# Assistant

The Assistant is a **Knowledge Source** that provides natural language interaction and intelligent flight support.

## State

### Online

A boolean flag indicating if the local LLM (Ollama) is available.

### Status

A string describing the current status of the AI (e.g., "READY", "OFFLINE").

## Behavior

* It utilizes a local large language model via **LangChain4j** and **Ollama**.
* It listens for user commands in natural language.
* It translates these commands into system actions by interacting with the **Shell** or **Computer**.
* It provides feedback to the user via the CLI.
* It runs periodically in its own thread via the **Loop** class (or as an asynchronous event handler).

## Concurrency

The Assistant operates in its own thread and must interact with the **Memory** and **Shell** in a thread-safe manner.

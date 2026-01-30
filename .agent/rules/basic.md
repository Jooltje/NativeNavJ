---
trigger: always_on
---

**Use Test Driven Design**: (Except for the connector).

**Use the Red-Green-Refactor Cycle**: Write a failing test, pass it, then clean it up.

**Each test has a single responsibility**: Focus on one outcome per test.

**Use the minimum amount of code required**: Avoid "future-proofing."

**Each test is independent**: No shared state between tests.

**Each test is fast**: Execution must stay in the millisecond range.

**Use clean code principles for tests**: Treat test code as production-grade code.

**Use Java 25 and Maven**: Target the latest LTS/Feature release and standard build tool.

**Each class or field is a single noun**: Keeps the domain model clean (e.g., Processor, not ProcessData).

**Use full words for all identifiers**: Do not use abbreviations; prioritize clarity over brevity.

**Use SLF4J and Logback**: Standardize logging across the board.

**Each specification follows the standard template**: Locate in specifications/*.md with State, Behavior, and Concurrency sections.

**Use Immutable and Atomic values**: Prioritize thread safety by design.

**Use a ScheduledExecutorService**: The orchestrator manages all thread scheduling.

**Use the FILE appender for logging**: Do NOT log to the CONSOLE!
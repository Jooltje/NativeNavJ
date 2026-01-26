# NativeNavJ

## Goal

Create a Autonomous Flight Control System for Microsoft Flight Simulator 2020 that is powered by a local large language model.

Test the capabilities of [Google Antigravity](https://antigravity.google/)
Learn more about how to use AI

## Constraints

* All code should be written by Antigravity
* All code is written in Java
* Use Test Driven Development

## Prompt

Gemini 3 Fast + Deep Research

Result: research.md

```
Goal: I want to create a Autonomous Flight Control System for Microsoft Flight Simulator 2020 that is powered by a local large language model.

Contraints:

 * Java 25
 * Maven
 * Test Driven Design
 * Developer (https://antigravity.google/)

Create a plan and suggest improvements so that we can complete our goal. Determinate the impact of each constraint.
```

## Commands

| Command | Description | Example |
|---------|-------------|---------|
| SYS { ON | OFF } | Enable / Disable the autonomous flight control system | SYS ON |
| HDG &lt;number&gt; | Set the heading in degrees | HDG 90 |
| ALT &lt;number&gt; | Set the altitude in feet | ALT 2500 |
| SPD &lt;number&gt; | Set the air speed in knots | SPD 100 |
| LLM { ON | OFF } | Enable / Disable the input of the large language model | LLM ON |
| ASK &lt;prompt&gt; | Set the goal for the large language model | ASK Fly to EGJJ |

## Flow

* = Actor

### Input

Simulator -> Connector -> Sensor* -> Computer (State)
User* -> Shell -> Computer (Goal)
Computer* -> Controller (SP)
Computer -> Controller* (PV)

### Output

Controller* -> Actuator -> Connector -> Simulator (roll / pitch / yaw / throttle)

## Log

Get-Content L:\code\NativeNavJ\telemetry.log -wait


TODO:
 * Absolute time for the state object
 * Compare with Master Loop or Interrupted Loop
 * Sync Sensor with the fastest Loop
 * Remove old specifications
 * Expand specifications rules
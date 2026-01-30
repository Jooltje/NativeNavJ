# Shell

The Shell is a Knowledge Source for user interaction.

## State

### Memory

This object is a reference to the blackboard.

### Input

The InputStream used to receive commands from the user.

## Behaviour

* The shell reads the input at a frequency of 1 Hz.
* The command SYS ON enables all systems in the memory object.
* The command SYS OFF disables all systems in the memory object.
* The command HDG &lt;number&gt; sets the heading in the memory object.
* The command ALT &lt;number&gt; sets the altitude in the memory object.
* The command SPD &lt;number&gt; sets the airspeed in the memory object.
* The command LLM ON sets the assistant to active in the memory object.
* The command LLM OFF sets the assistant to inactive in the memory object.
* The command ASK &lt;prompt&gt; sets the goal for the assistant in the memory object.

SET <name> <parameter> <value>

| Name      | Object       | Type       | Description                                               |
|-----------|--------------|------------|-----------------------------------------------------------|
| LLM       | Assistant    | task       | This object interacts with the local large language model |
| CPU       | Computer     | task       | This object calculates the new targets for the plane      |
| ROL       | Controller   | controller | This object controls the ailerons of the plane            |
| PIT       | Controller   | controller | This object controls the elevator of the plane            |
| YAW       | Controller   | controller | This object controls the rudder of the plane              |
| THR       | Controller   | controller | This object controls the throttle of the plane            |
| JOB       | Orchestrator | task       | This object manages the other tasks and controllers       |
| SHL       | Shell        | task       | This object interacts with the user                       |

| Parameter | Value   | Description                            | task      | controller |
|-----------|---------|----------------------------------------|-----------|------------|
| SYS       | boolean | The state of the function              | supported | supported  |
| FRQ       | number  | The frequency of the function          | supported | supported  |
| KP        | double  | The coeficient of the proportioal term |           | supported  |
| KI        | double  | The coeficient of the integral term    |           | supported  |
| KD        | double  | The coeficient of the derivative term  |           | supported  |

## Concurrency

The Shell is controlled by the Orchestrator.
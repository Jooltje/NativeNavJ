# Shell

The Shell is a Knowledge Source for user interaction.

## State

### Memory

This object is a reference to the blackboard.

### Input

The InputStream used to receive commands from the user.

## Behaviour

* The shell reads the input at a frequency of 1 Hz.
* The command SYS ON sets the navigator to active in the memory object.
* The command SYS OFF sets the navigator to inactive in the memory object.
* The command HDG &lt;number&gt; sets the heading in the memory object.
* The command ALT &lt;number&gt; sets the altitude in the memory object.
* The command SPD &lt;number&gt; sets the airspeed in the memory object.
* The command LLM ON sets the assistant to active in the memory object.
* The command LLM OFF sets the assistant to inactive in the memory object.
* The command ASK &lt;prompt&gt; sets the goal for the assistant in the memory object.

## Concurrency

The Shell is controlled by the Orchestrator.
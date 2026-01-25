# State

This object contains the current state of the aircraft.

## Structure

### latitude

The current latitude of the aircraft in degrees.

### longitude

The current longitude of the aircraft in degrees.

### heading

The current heading of the aircraft in degrees.

### altitude

The current altitude of the aircraft in feet.

### roll

The current roll of the aircraft in degrees.

### pitch

The current pitch of the aircraft in degrees.

### yaw

The current yaw of the aircraft in degrees.

### speed

The current indicated air speed of the aircraft in knots.

### climb

The current vertical speed of the aircraft in feet per minute.

## Behavior

No required behavior.

## Concurrency

This object is used by multiple threads, so it should be immutable.

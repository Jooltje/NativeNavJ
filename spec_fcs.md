# **Flight Control System (FCS) \- Java Specification**

**Developer:** Antigravity **Version:** 1.0 **Methodology:** Test Driven Development (TDD)

## **1\. TDD Mandate**

All modules must be developed using the **Red-Green-Refactor** cycle.

1. **Fail:** Write a test for the specific behavior (e.g., "PID clamps output at max").  
2. **Pass:** Write the minimum code to pass the test.  
3. **Refactor:** Optimize without breaking the test. *Mocking:* Use Mockito or similar to mock `AtomicReference` inputs during testing of the TECS and Control loops.

## **2\. Architecture Overview**

**Pattern:** Concurrent Functional Pipeline. **Core Principle:** State is immutable. Threads communicate via atomic swaps of state objects.

### **Threading Model**

* **Isolation:** Each major component runs in its own thread.  
* **Timing:** Controlled by a generic `LoopController` (see Sec 4).  
* **Synchronization:** Non-blocking `AtomicReference<T>` for data exchange.

## **3\. Data Contracts (Immutable Records)**

Define these first. They form the API between threads.

### **A. `FlightTelemetry` (Read-Only Sensors)**

Produced by: Sensor Thread Consumed by: TECS, PIDs

```
public record FlightTelemetry(
    double altitudeFt,
    double airspeedKts,
    double pitchDeg,
    double rollDeg,
    double headingDeg,
    double verticalSpeedFpm,
    long timestampNs
) {}
```

### **B. `FlightGoal` (Pilot Intent)**

Produced by: Command Parser Consumed by: TECS

```
public record FlightGoal(
    boolean systemActive,       // ON/OFF master switch
    double targetAltitudeFt,
    double targetHeadingDeg,
    double targetAirspeedKts
) {}
```

### **C. `ControlFrame` (The Plan)**

Produced by: TECS Consumed by: PID Loops

```
public record ControlFrame(
    double pitchTargetDeg,      // Calculated by Energy Balance
    double rollTargetDeg,       // Calculated by Nav Logic
    double throttlePercent      // Calculated by Total Energy
) {}
```

## **4\. Core Infrastructure: `LoopController`**

**Role:** Generic frequency manager. **Requirement:** Abstract class. Implementing classes define the `step()` logic. **TDD Focus:** Verify frequency stability (e.g., 50Hz runs 50 times in 1 sec ± tolerance).

```
public abstract class LoopController implements Runnable {
    private final long periodNs;
    private volatile boolean running;

    public LoopController(double hz) {
        this.periodNs = (long) (1_000_000_000.0 / hz);
    }
    
    // Abstract hook for the specific module logic
    protected abstract void step();

    @Override
    public void run() {
        // Implement precise drift-compensating sleep loop here
    }
}
```

## **5\. Module Logic**

### **A. `GenericPID`**

**Role:** "Dumb" error correction. **State:** Mutable internal state (integral sum, prev error) is allowed *inside* the class, but the class is used synchronously within a Loop thread. **Safeties:** Mechanical limits only (Min/Max Output).

* **Inputs:** `setpoint`, `measurement`, `dt`  
* **Outputs:** `signal` (clamped)  
* **TDD Cases:**  
  * Zero error \-\> Zero P/D term.  
  * Accumulating Integral over ticks.  
  * Output never exceeds `maxOutput` (Clamping).

### **B. `TECSModule` (The "Brain")**

**Role:** Energy Management & Flight Envelope Protection. **Frequency:** \~20 Hz **Extends:** `LoopController`

**Logic Flow (inside `step()`):**

1. **Fetch:** `goal = goalRef.get()`, `telemetry = telemetryRef.get()`.  
2. **Safety Check (Smart Safety):**  
   * `IF telemetry.airspeed < MIN_STALL`: Override Pitch \-\> Nose Down (-10°), Throttle \-\> Max (100%).  
3. **Calculations (If Safe):**  
   * `EnergyError` \= (Alt\_Err) \+ (Spd\_Err) \-\> Drives Throttle.  
   * `DistError` \= (Alt\_Err) \- (Spd\_Err) \-\> Drives Pitch.  
   * `HeadingError` \-\> Drives Roll.  
4. **Publish:** `controlRef.set(new ControlFrame(...))`

### **C. Actuator Loops (The "Muscles")**

**Role:** Drive servos/ESC. **Frequency:** \~100 Hz (Pitch/Roll), \~20 Hz (Throttle) **Extends:** `LoopController`

**Logic Flow:**

1. **Fetch:** `target = controlRef.get()`, `actual = telemetryRef.get()`.  
2. **Execute:**  
   * `output = pid.calculate(target.pitch, actual.pitch)`  
3. **Hardware:** Write `output` to Servo/ESC.

## **6\. Command Parser**

**Role:** Translate String \-\> `FlightGoal`. **Pattern:** Copy-on-Write.

**Logic:**

1. Receive String (e.g., "ALT 3000").  
2. Read current: `oldGoal = goalRef.get()`.  
3. Create new: `newGoal = new FlightGoal(oldGoal.active, 3000, ...)` preserving other fields.  
4. Swap: `goalRef.set(newGoal)`.

## **7\. Implementation Roadmap**

1. **Infra:** Build `LoopController` & write frequency tests.  
2. **Math:** Build `GenericPID` & write clamping/math tests.  
3. **Data:** Define Records (`FlightTelemetry`, etc).  
4. **Logic:** Build `TECSModule` with mocked inputs. Test Stall Protection logic specifically.  
5. **Integration:** Wire `CommandParser` \-\> `AtomicReference` \-\> `TECS`.


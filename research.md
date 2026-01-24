# **Architectural Design of an Autonomous Flight Control System for Microsoft Flight Simulator 2020 Utilizing Java 25, Project Panama, and Test-Driven Development**

The integration of high-fidelity flight simulation environments like Microsoft Flight Simulator 2020 (MSFS) with advanced autonomous control logic represents a critical frontier in aerospace engineering and software development. The pursuit of a robust Autonomous Flight Control System (AFCS) necessitates a synthesis of classical control theory and modern software craftsmanship. By leveraging the specific capabilities of Java 25, including the stabilized Foreign Function and Memory (FFM) API from Project Panama, developers can construct a high-performance bridge to the simulator’s native SimConnect library. However, the technical challenge is not merely one of connectivity but of architectural integrity. The application of Test-Driven Development (TDD) as the primary design driver ensures that the resulting system is modular, verifiable, and resilient to the complexities of flight dynamics.1 This report provides an exhaustive analysis of the architectural strategies required to implement an AFCS using the Total Energy Control System (TECS) and Proportional-Integral-Derivative (PID) controllers, within a Java 25 environment where design is strictly dictated by the Red-Green-Refactor cycle.

## **The Paradigm of Test-Driven Design in Aerospace Software**

Test-Driven Development is frequently mischaracterized as a mere testing activity; however, in the context of safety-critical systems like an AFCS, it serves as a fundamental design methodology. The process of writing a test before the implementation forces the developer to define the interface and the expected behavior of a control module from the perspective of its consumer.2 This "test-first" approach is particularly vital when dealing with the non-linearities of flight, as it establishes a suite of executable specifications that the system must satisfy to remain flight-worthy.4  
The adoption of TDD ensures that the AFCS architecture naturally evolves toward a decoupled, modular state. By focusing on the smallest testable units—such as an energy rate calculator or a PID error accumulator—the system avoids the "big ball of mud" anti-pattern common in complex simulation wrappers.6 For the aerospace engineer, TDD provides a safety net that allows for the confident refactoring of control laws as flight testing reveals nuances in aircraft performance.8

### **The Red-Green-Refactor Cycle for Control Laws**

The implementation of flight control logic begins with the Red phase. For instance, to develop a altitude-hold capability, a test is written to assert that given an altitude error of 500 feet, the controller must output a target pitch angle within a safe performance envelope.1 At this stage, the code does not exist, and the test fails, providing a clear objective for the implementation.  
In the Green phase, the developer writes the minimal amount of code to satisfy the test. This might involve a simple proportional gain where the pitch command is a direct function of the altitude error.11 The focus remains on correctness rather than elegance or optimization. Once the test passes, the Refactor phase allows the developer to improve the design—perhaps by introducing a command limiter or an integrator term—while the existing test ensures that the core requirement is still met.12

| TDD Phase | Action in AFCS Context | Architectural Outcome |
| :---- | :---- | :---- |
| **Red** | Define a requirement (e.g., "The system must limit bank angle to 30 degrees") as a failing test. | Enforces clear boundaries and precise input/output contracts for control modules. |
| **Green** | Implement the simplest mathematical logic to satisfy the bank limit assertion. | Prioritizes functional correctness and avoids over-engineering of control surface responses. |
| **Refactor** | Clean up the implementation, ensuring the bank limit logic is readable and adheres to SOLID principles. | Promotes long-term maintainability and modularity, enabling the swapping of aircraft models. |

This cycle repeats for every feature, from basic stabilization to complex path-following via TECS. By driving design through tests, the AFCS becomes a collection of independent, high-integrity components that can be validated in isolation before integration with the simulator.14

## **Java 25 and the Native Interface Strategy**

The selection of Java 25 for an AFCS project is motivated by its superior blend of high-level productivity and low-level system access. The maturation of Project Panama, finalized as the Foreign Function and Memory API, represents a paradigm shift in how Java interacts with native libraries like MSFS’s SimConnect.dll.16 Unlike the legacy Java Native Interface (JNI), which required the maintenance of fragile C++ wrapper code, the FFM API allows Java to call native functions and access off-heap memory with high performance and safety.18  
Java 25 introduces further refinements to this ecosystem. Generational ZGC and G1 region pinning drastically reduce the garbage collection pauses that were previously the bane of real-time control systems.20 These enhancements ensure that the flight control loop, which typically needs to run at frequencies of 50Hz to 100Hz, remains deterministic and responsive to simulator state changes.22

### **Project Panama: The Foreign Function and Memory API**

The FFM API consists of several core abstractions that enable the Java AFCS to communicate with the simulator. The SymbolLookup interface is used to locate the entry points for SimConnect functions such as SimConnect\_Open and SimConnect\_TransmitClientEvent.16 The Linker then converts these native addresses into MethodHandle objects, which can be invoked directly from Java with near-native performance.23  
Memory management is handled via the Arena and MemorySegment abstractions. An Arena provides a controlled scope for native memory allocation, ensuring that telemetry data from the simulator is cleared from memory as soon as it is processed.17 This is critical for an AFCS, which handles thousands of data packets per second.

| Component | Role in SimConnect Integration | Benefit of Java 25 Implementation |
| :---- | :---- | :---- |
| **Arena** | Manages the lifecycle of telemetry data buffers. | Confined arenas prevent cross-thread memory corruption in the control loop. |
| **MemorySegment** | Represents the actual byte-buffer received from the simulator. | Provides safe, bounds-checked access to SimVars without JNI overhead. |
| **Linker** | Bridges Java method calls to native SimConnect functions. | Enables the use of MethodHandle for low-latency command transmission. |
| **jextract** | Automates the creation of Java bindings from SimConnect.h. | Eliminates manual C-to-Java marshalling errors, increasing system reliability. |

### **The "Impossible" TDD Connector**

The original request acknowledges that TDD might be impossible for the native connector. While it is true that one cannot unit-test the behavior of a closed-source DLL like SimConnect.dll without a running simulator, the *design* of the connector can still be test-driven. The solution lies in the Adapter pattern.24  
By defining a SimulatorAdapter interface (the "Port"), the AFCS domain logic remains isolated from the native implementation details. For TDD purposes, a MockSimulator can be injected into the control modules to verify that they respond correctly to specific telemetry patterns.26 The actual SimConnect implementation (the "Adapter") can then be verified through integration tests. Even the translation logic—converting a native MemorySegment into a Java AircraftState record—can be unit-tested by providing the adapter with a mocked memory region filled with known byte patterns.23 This ensures that even the low-level data handling is driven by the requirements of the high-level control laws.

## **Total Energy Control System (TECS) Implementation**

The longitudinal control of an aircraft is a multivariable problem where changes in thrust affect altitude and changes in pitch affect airspeed.29 The Total Energy Control System (TECS) offers an elegant solution by decoupling these variables through the lens of energy management. In the TECS framework, the aircraft is viewed as a system that exchanges potential energy (altitude) and kinetic energy (speed).31

### **Mathematical Foundation and Specific Energy Rates**

The core of the TECS algorithm is based on the conservation of mechanical energy. The total specific energy ($E\_s$) of the aircraft is the sum of its potential and kinetic energies, normalized by its weight 33:

$$E\_s \= h \+ \\frac{V^2}{2g}$$  
Where $h$ is altitude, $V$ is true airspeed, and $g$ is the acceleration due to gravity. The rate of change of this energy, the specific energy rate ($\\dot{E}\_s$), is what the controller attempts to regulate using the throttle 33:

$$\\dot{E}\_s \= \\frac{\\dot{h}}{V} \+ \\frac{\\dot{V}}{g} \\approx \\gamma \+ \\frac{\\dot{V}}{g}$$  
Where $\\gamma$ is the flight path angle. Simultaneously, the controller manages the specific energy distribution rate ($\\dot{D}\_s$), which represents the balance between altitude and speed 33:

$$\\dot{D}\_s \= \\frac{\\dot{h}}{V} \- \\frac{\\dot{V}}{g} \\approx \\gamma \- \\frac{\\dot{V}}{g}$$  
The specific energy distribution is controlled primarily by the elevator (pitch attitude). By pitching the aircraft down, the controller "trades" potential energy for kinetic energy, increasing speed while losing altitude.29

### **TDD Drivers for TECS Design**

In a TDD-driven design, the TECS controller is not built as a monolithic algorithm but as a series of interacting energy evaluators. The initial tests focus on the calculation of energy errors.

1. **Requirement:** The system must calculate a total energy error based on the difference between commanded and actual altitude/speed.35  
2. **Test:** Assert that if the aircraft is at the target speed but 100 feet too low, the total energy error is positive, signaling a need for more thrust.  
3. **Design Change:** This forces the creation of a getEnergyError() method that implements the specific energy equations.

Subsequent tests drive the implementation of the PI (Proportional-Integral) loops that translate these energy errors into throttle and pitch commands.34 The weighting factor, TECS\_SPDWEIGHT, becomes a central design parameter. A weight of 0.0 forces the pitch loop to prioritize altitude, while a weight of 2.0 forces it to prioritize airspeed—a critical distinction for glider flight or fuel-efficient cruising.32

| TECS Parameter | Logical Impact | AFCS Behavior |
| :---- | :---- | :---- |
| **Specific Energy Rate Error** | Governs throttle demand. | Used to increase or decrease total energy via engine power. |
| **Energy Distribution Error** | Governs pitch angle setpoint. | Used to balance energy between altitude and speed. |
| **TECS\_TIME\_CONST** | Sets the responsiveness of the energy loop. | Lower values provide aggressive tracking; higher values provide smoother passenger comfort. |
| **TECS\_INTEG\_GAIN** | Eliminates steady-state altitude and speed offsets. | Critical for ensuring the aircraft reaches the exact assigned flight level. |

## **Proportional-Integral-Derivative (PID) Inner Loops**

While TECS manages the long-term energy path of the aircraft, the immediate stability of the airframe is maintained by high-frequency PID controllers. These controllers act as the inner loops of the AFCS, stabilizing the roll, pitch, and yaw rates.22 A cascaded architecture is typically employed, where an outer loop calculates a desired rate of change based on an attitude setpoint, and an inner PID loop drives the control surfaces (ailerons, elevators, rudder) to achieve that rate.34

### **Designing for Robust Stability**

The design of a Java-based PID controller through TDD must address real-world phenomena such as integrator windup and derivative noise. These are not merely implementation details but fundamental requirements that should be captured in tests.37

1. **Integrator Windup Prevention:** A test is written where the control surface command is saturated (e.g., full elevator deflection). The test asserts that the integral term does not continue to accumulate error beyond a certain threshold. This forces the design of a "clamping" mechanism within the PIDController class.38  
2. **Derivative Smoothing:** Sudden setpoint changes can cause "derivative kick," leading to jarring control movements. TDD drives the implementation toward calculating the derivative on the feedback signal (e.g., measured pitch rate) rather than the error signal, ensuring smooth transitions.38

The mathematical discrete-time implementation of the PID output $u\[k\]$ at time step $k$ is 22:

$$u\[k\] \= K\_p e\[k\] \+ K\_i \\sum e\[k\]\\Delta t \+ K\_d \\frac{e\[k\] \- e\[k-1\]}{\\Delta t}$$  
In the Java AFCS, the time delta $\\Delta t$ is a critical input. TDD encourages the use of an abstract Clock interface so that tests can simulate precise time intervals without relying on the system's real-time clock, ensuring repeatable test results.40

### **Tuning and Performance Validation**

The effectiveness of a PID loop is determined by its gains ($K\_p, K\_i, K\_d$). While initial tuning often uses heuristic methods like Ziegler-Nichols, the TDD environment allows for the quantification of controller performance through metrics.31

| Metric | Formulation | Application in AFCS TDD |
| :---- | :---- | :---- |
| **IAE (Integral of Absolute Error)** | $\\int | e(t) |
| **ITAE (Integral of Time-weighted Absolute Error)** | $\\int t | e(t) |
| **MAE (Mean Absolute Error)** | $\\frac{1}{T} \\int | e(t) |

By writing tests that assert a maximum allowable ITAE for a standard maneuver (such as a 30-degree bank turn), the developer ensures that the PID parameters are correctly tuned and that the system behavior remains within professional standards.8

## **Hexagonal Architecture and the Domain Model**

The most significant architectural decision in the AFCS is the radical separation of control laws from simulator interfaces. Hexagonal Architecture facilitates this by placing the "Domain" at the center of the application.24 The domain contains the pure logic of TECS and PID, expressed in terms of aircraft states and control demands.  
The domain defines "Ports," which are Java interfaces for input and output.

* **Telemetry Port:** An interface that provides the current state vector (altitude, speed, attitude).43  
* **Control Port:** An interface that receives surface and throttle demands.45

The "Adapters" implement these ports for specific environments. The SimConnectAdapter implements the ports by communicating with MSFS 2020\. This allows the core control logic to be tested using a MockSimulatorAdapter in a purely local JVM environment, free from the overhead of the flight simulator.24

### **Decoupling and Persistence**

A secondary benefit of this architecture is the ability to swap the underlying simulation engine without modifying the control laws. For example, if the system needed to be adapted for MSFS 2024, only the SimConnectAdapter would require updating to handle changes like increased structure sizes for helipad identification.47 The core AFCS logic, protected by its suite of TDD-validated tests, remains unchanged.  
Furthermore, Java 25’s support for record classes and sealed interfaces allows for the creation of an expressive and type-safe domain model. Flight modes—such as Takeoff, Cruise, and Approach—can be represented as a sealed hierarchy, ensuring that the controller’s behavior at any given time is explicitly defined and exhaustive in its handling of different flight phases.20

## **High-Performance Interop with SimConnect**

The SimConnect API is the lifeline of any MSFS add-on. It operates on an asynchronous request-reply model, utilizing a dispatcher loop to process messages.43 In the Java AFCS, the integration of this dispatcher requires a sophisticated implementation of upcalls using Project Panama.

### **The Dispatcher Mechanism and Upcalls**

SimConnect requires the client to provide a callback function, DispatchProc, which the simulator calls whenever a new event or data packet is available.44 In Java 25, this is achieved by creating an upcall stub via the Linker. This stub allows the native simulator code to jump into a specific Java method.16  
To ensure the system remains responsive, the dispatcher loop typically runs in a dedicated thread. Java 25’s virtual threads (Project Loom) provide an ideal mechanism for managing multiple SimConnect clients or parallel telemetry streams without the heavy overhead of OS threads.16

| SimConnect Function | Purpose | Java 25 Panama Implementation |
| :---- | :---- | :---- |
| **SimConnect\_Open** | Establishes the initial pipe to the simulator. | A downcall handle to a native symbol. |
| **SimConnect\_CallDispatch** | Triggers the processing of the next message. | Typically called in a high-frequency loop with Thread.sleep(16). |
| **SimConnect\_MapClientEventToSimEvent** | Associates a Java event ID with an MSFS action. | Mapping enum values to native integers using Panama ValueLayout. |
| **SimConnect\_SetDataOnSimObject** | Updates a control surface in the simulator. | Marshalls a Java data object into a native MemorySegment. |

### **Marshalling Complex Data Structures**

One of the primary responsibilities of the SimConnectAdapter is the marshalling of data between the C-based structures of the SDK and the Java-based records of the domain. SimConnect uses packed structures (e.g., Pack \= 1\) to ensure data integrity.43  
Project Panama’s MemoryLayout API allows the developer to define the exact layout of these structures in memory. For example, the SIMCONNECT\_DATA\_LATLONALT structure is defined as a sequence of three 64-bit doubles.44 By using a StructLayout, the Java AFCS can read this data directly from off-heap memory with zero copying, significantly reducing latency.49

## **Optimizing the Control Loop for Java 25**

The effectiveness of an AFCS is ultimately limited by the frequency and stability of its control loop. In a typical MSFS scenario, the simulator provides state updates at the frame rate (e.g., 60 FPS), which equates to an update every 16.6 milliseconds.48 The Java control loop must receive telemetry, calculate the next control state via TECS and PID, and transmit the results back to the simulator within this window.

### **Garbage Collection and Real-Time Determinism**

A traditional challenge for Java in control applications is the unpredictability of garbage collection pauses. Java 25 addresses this through the maturation of the Z Garbage Collector (ZGC), which is designed for sub-millisecond pause times regardless of heap size.20  
By configuring the AFCS to use Generational ZGC, the JVM can efficiently manage the short-lived telemetry objects produced in each control cycle. Furthermore, the FFM API's use of off-heap memory via Arena means that the vast majority of data being processed never enters the Java heap at all, thereby reducing the workload on the garbage collector and maintaining a consistent 16ms control cadence.20

### **JVM Layout and Object Header Savings**

Project Lilliput, finalized in Java 25, introduces compact object headers, reducing the size of Java objects on 64-bit systems from 128 bits to 64 bits.20 While this may seem like a minor optimization, in an AFCS that maintains a high-frequency history of aircraft states (for derivative calculations or logging), the cumulative memory savings and improved cache locality lead to measurable performance gains. More state data can fit within the L1/L2 caches, accelerating the mathematical operations required for flight stability.

## **Integration Testing and Environment Simulation**

While TDD provides the building blocks, the integration of the AFCS requires a strategy for validating the system against the simulator itself. MSFS provides various debug tools, such as the SimConnect Inspector, which allow developers to monitor the data flowing through the API in real-time.50

### **Software-in-the-Loop (SITL) Strategy**

For advanced validation, a Software-in-the-Loop (SITL) approach is used. The Java AFCS is connected to a simplified physics model of the aircraft, which provides feedback to the control laws.53 This setup is also driven by TDD; a suite of "maneuver tests" is created to verify that the system can perform standard tasks like a 1000-fpm climb or a turn to a specific heading without excessive overshoot.  
These SITL tests are essential for verifying the coordination between the longitudinal TECS and the lateral PID loops. For instance, a climb command (potential energy increase) should be accompanied by a throttle increase (total energy increase) and a temporary bank angle limitation to ensure the aircraft remains within its safe operating envelope.29

### **Handling Simulator Failures and Edge Cases**

MSFS 2020 is a complex software system that can suffer from regressions, crashes-to-desktop (CTDs), or telemetry dropouts.55 A professional AFCS must be designed to handle these events gracefully. TDD is used to define "fail-safe" scenarios:

1. **Requirement:** If SimConnect telemetry is lost for more than 100ms, the system must disengage and alert the pilot.  
2. **Test:** In the test environment, the telemetry stream is interrupted, and the system state is checked to ensure it transitioned to "DISENGAGED."  
3. **Mechanism:** This drives the implementation of a watchdog timer within the SimulatorAdapter.56

| Failure Scenario | AFCS Detection Mechanism | Fail-Safe Action |
| :---- | :---- | :---- |
| **Telemetry Timeout** | Watchdog timer on the SimulatorAdapter. | Neutralize control surfaces and alert pilot. |
| **Integrator Windup** | Clamping logic within the PID loop. | Limit output to maximum physical deflection. |
| **Thrust Saturation** | TECS pseudo-control hedging logic. | Prioritize airspeed over altitude during climbs. |
| **Native Library Crash** | Java 25 FFM error state capturing (errno). | Log error details and initiate emergency shutdown. |

## **Advanced Flight Dynamics and Mode Switching**

As the AFCS matures, it must handle the transition between different flight modes, such as takeoff, cruise, approach, and landing. Each mode requires a different configuration of the TECS and PID gains.34

### **Mode-Specific Control Strategies**

During takeoff, the AFCS priorities are maximum thrust and the maintenance of a safe climb speed ($V\_2$). The TECS\_SPDWEIGHT is typically set to prioritize airspeed to prevent a stall during the initial climb-out.32 In contrast, during a precision approach, the priority shifts to glide path tracking, and the pitch loop is tuned for high-precision altitude control.33  
TDD ensures that the state transitions between these modes are seamless. Tests are written to verify that when switching from "Cruise" to "Approach," the integral terms of the PID loops are not cleared instantly (which would cause a "bump" in flight) but are transitioned according to a smoothing algorithm.33

### **Aerodynamic Modeling and System Identification**

To achieve the highest level of precision, the AFCS can incorporate a dynamic mathematical model of the aircraft, considering its degrees of freedom and the dynamics of motion.36 This model provides a foundation for more advanced control techniques, such as model-following architecture.57  
The development of these models is also test-driven. A suite of identification tests is used to compare the predicted aircraft response against recorded telemetry data from the simulator. This allows for the iterative refinement of the aerodynamic coefficients used by the controller.29

## **Future Outlook: MSFS 2024 and Beyond**

The flight simulation landscape is rapidly evolving with the release of MSFS 2024\. While the core SimConnect API remains largely compatible, the new SDK introduces features like an Electronic Flight Bag (EFB) API and improved AI traffic management.47 The architectural foundations established for the Java 25 AFCS—specifically the use of Project Panama and Hexagonal Architecture—ensure that the system is well-positioned to take advantage of these developments.  
The shift toward network-based telemetry protocols and bidirectional packet-based communication, such as Ethernet-based telemetry in aerospace test ranges, mirrors the evolution of the SimConnect API.59 The Java AFCS, with its modular adapter layer, can easily be extended to support these modern protocols, bridging the gap between desktop simulation and real-world flight testing.

## **Final Synthesis and Strategic Recommendations**

The creation of an Autonomous Flight Control System for Microsoft Flight Simulator 2020 using Java 25 is an endeavor that demands a rigorous adherence to software engineering best practices. The complexity of the flight environment requires that design be driven not by intuition but by the objective, repeatable validation provided by Test-Driven Development.

* **Design Inward:** Begin with the domain logic—the TECS and PID controllers—and use TDD to define their interfaces. Use mocks to isolate these modules from the simulator's complexities.  
* **Leverage Panama:** Use the Foreign Function and Memory API to create a safe, high-performance bridge to SimConnect. Avoid the legacy JNI wherever possible to reduce technical debt and increase stability.  
* **Architect for Isolation:** Employ Hexagonal Architecture to ensure that the core flight control laws are independent of the simulation engine. This facilitates testing and ensures long-term compatibility with future simulator versions.  
* **Optimize for Real-Time:** Configure Java 25's Generational ZGC and utilize Panama Arenas to manage memory deterministically, ensuring that the control loop meets the simulator's 16ms timing requirements.  
* **Validate Performance:** Use quantitative metrics like ITAE and SITL maneuver tests to ensure that the AFCS behavior meets professional aviation standards.

By following this architectural roadmap, developers can build an AFCS that is not only functionally superior but also a benchmark for code quality and reliability in the simulation community. The combination of Java 25’s modern features and the discipline of TDD provides the tools necessary to navigate the virtual skies of MSFS 2020 with unprecedented precision.

## **Conclusions**

The analysis indicates that the development of a professional-grade Autonomous Flight Control System in Java 25 is not only feasible but represents a state-of-the-art approach to simulation control. The maturation of Project Panama provides a robust alternative to JNI, enabling high-performance integration with the Microsoft Flight Simulator SDK. By utilizing Test-Driven Development to drive the design of the Total Energy Control System and the underlying PID stabilization loops, developers create a modular architecture that is inherently testable and maintainable. The decoupling of flight logic from native interfaces through the Ports and Adapters pattern is a critical success factor, allowing the system to be validated through extensive mocking and environment simulation. As Java continues to evolve with features that prioritize both developer productivity and system-level performance, it remains a premier choice for the next generation of autonomous aerospace software.

#### **Works cited**

1. Mastering Test-Driven Development (TDD) in Java: A Comprehensive Guide with Examples | by Ahmet Temel Kundupoglu | Medium, accessed on January 24, 2026, [https://medium.com/@ahmettemelkundupoglu/mastering-test-driven-development-tdd-in-java-a-comprehensive-guide-with-examples-bc350597e6c9](https://medium.com/@ahmettemelkundupoglu/mastering-test-driven-development-tdd-in-java-a-comprehensive-guide-with-examples-bc350597e6c9)  
2. (PDF) Guest Editors' Introduction: TDD--The Art of Fearless Programming \- ResearchGate, accessed on January 24, 2026, [https://www.researchgate.net/publication/3249271\_Guest\_Editors'\_Introduction\_TDD--The\_Art\_of\_Fearless\_Programming](https://www.researchgate.net/publication/3249271_Guest_Editors'_Introduction_TDD--The_Art_of_Fearless_Programming)  
3. Why Test Driven Development Matters: Best Practices to Implement TDD \- Zealous System, accessed on January 24, 2026, [https://www.zealousys.com/blog/what-is-test-driven-development/](https://www.zealousys.com/blog/what-is-test-driven-development/)  
4. How to Implement Test-Driven Development (TDD): A Practical Guide \- TestRail, accessed on January 24, 2026, [https://www.testrail.com/blog/test-driven-development/](https://www.testrail.com/blog/test-driven-development/)  
5. Test-driven development \- Wikipedia, accessed on January 24, 2026, [https://en.wikipedia.org/wiki/Test-driven\_development](https://en.wikipedia.org/wiki/Test-driven_development)  
6. Test-Driven Development with Java | Programming | Paperback \- Packt, accessed on January 24, 2026, [https://www.packtpub.com/en-us/product/test-driven-development-with-java-9781803236230?type=print](https://www.packtpub.com/en-us/product/test-driven-development-with-java-9781803236230?type=print)  
7. Test-Driven Development: Really, It's a Design Technique \- InfoQ, accessed on January 24, 2026, [https://www.infoq.com/articles/test-driven-design-java/](https://www.infoq.com/articles/test-driven-design-java/)  
8. Test-Driven Development In MATLAB \- Meegle, accessed on January 24, 2026, [https://www.meegle.com/en\_us/topics/test-driven-development/test-driven-development-in-matlab](https://www.meegle.com/en_us/topics/test-driven-development/test-driven-development-in-matlab)  
9. Lessons learned about testing and TDD, accessed on January 24, 2026, [http://marco-buttu.github.io/pycon\_testing/](http://marco-buttu.github.io/pycon_testing/)  
10. Test Driven Development for Java using JUnit | Quick Guide \- XenonStack, accessed on January 24, 2026, [https://www.xenonstack.com/blog/test-driven-development-java](https://www.xenonstack.com/blog/test-driven-development-java)  
11. Lesson: Exercise: Using a PID Controller \- STEMRobotics, accessed on January 24, 2026, [https://stemrobotics.cs.pdx.edu/node/7268.html](https://stemrobotics.cs.pdx.edu/node/7268.html)  
12. How to TDD a List Implementation in Java \- Baeldung, accessed on January 24, 2026, [https://www.baeldung.com/java-test-driven-list](https://www.baeldung.com/java-test-driven-list)  
13. Mastering Test-Driven Development and TDD Testing \- XenonStack, accessed on January 24, 2026, [https://www.xenonstack.com/blog/test-driven-development](https://www.xenonstack.com/blog/test-driven-development)  
14. jUAV: A Java Based System for Unmanned Aerial Vehicles \- Steven Y. Ko, accessed on January 24, 2026, [https://steveyko.github.io/assets/pdf/juav-jtres16.pdf](https://steveyko.github.io/assets/pdf/juav-jtres16.pdf)  
15. Leveraging Test-Driven Development (TDD) for AI System Architecture | Galileo, accessed on January 24, 2026, [https://galileo.ai/blog/tdd-ai-system-architecture](https://galileo.ai/blog/tdd-ai-system-architecture)  
16. Java FFM \- Foreign Function & Memory Access API (Project Panama) \- roray.dev, accessed on January 24, 2026, [https://www.roray.dev/blog/java-io-uring-ffm/](https://www.roray.dev/blog/java-io-uring-ffm/)  
17. From JNI to FFM: The future of Java‑native interoperability \- IBM Developer, accessed on January 24, 2026, [https://developer.ibm.com/articles/j-ffm/](https://developer.ibm.com/articles/j-ffm/)  
18. Project Panama Unleashing Native Libraries with Tobi Ajila, accessed on January 24, 2026, [https://devnexus.com/posts/project-panama-unleashing-native-libraries-with-tobi-ajila](https://devnexus.com/posts/project-panama-unleashing-native-libraries-with-tobi-ajila)  
19. Guide to Java Project Panama | Baeldung, accessed on January 24, 2026, [https://www.baeldung.com/java-project-panama](https://www.baeldung.com/java-project-panama)  
20. Java 25: New Features and Changes Since Java 21 | by Baker M Naim \- Medium, accessed on January 24, 2026, [https://medium.com/@baker.m.naim.99/java-25-new-features-and-changes-since-java-21-c43f12476f49](https://medium.com/@baker.m.naim.99/java-25-new-features-and-changes-since-java-21-c43f12476f49)  
21. Intro to Java FFM \- Foreign Function & Memory Access API (Project Panama) \- Reddit, accessed on January 24, 2026, [https://www.reddit.com/r/java/comments/1n25oyb/intro\_to\_java\_ffm\_foreign\_function\_memory\_access/](https://www.reddit.com/r/java/comments/1n25oyb/intro_to_java_ffm_foreign_function_memory_access/)  
22. Flight Control System \- JAviator, accessed on January 24, 2026, [https://javiator.cs.uni-salzburg.at/system/javiator\_software\_system/flight\_control\_system.html](https://javiator.cs.uni-salzburg.at/system/javiator_software_system/flight_control_system.html)  
23. Calling Native Libraries from Java with the Foreign Function & Memory API, accessed on January 24, 2026, [https://dev.to/myfear/calling-native-libraries-from-java-with-the-foreign-function-memory-api-89b](https://dev.to/myfear/calling-native-libraries-from-java-with-the-foreign-function-memory-api-89b)  
24. Implementing Hexagonal Architecture in Java: A Practical Guide for Clean Domain-Centric Design | by Enrique M Montenegro | Medium, accessed on January 24, 2026, [https://medium.com/@emedinam/implementing-hexagonal-architecture-in-java-a-practical-guide-for-clean-domain-centric-design-37c7f8ca3e80](https://medium.com/@emedinam/implementing-hexagonal-architecture-in-java-a-practical-guide-for-clean-domain-centric-design-37c7f8ca3e80)  
25. How I learned to love mocks. Adapters are key to use mocks… | by Matteo Vaccari | Medium, accessed on January 24, 2026, [https://medium.com/@xpmatteo/how-i-learned-to-love-mocks-1-fb341b71328](https://medium.com/@xpmatteo/how-i-learned-to-love-mocks-1-fb341b71328)  
26. Test-Driven Development (TDD) in Spring Boot with JUnit and Mockito | by Esvaramoorthy Vipisanan | Medium, accessed on January 24, 2026, [https://medium.com/@vipisanan1/test-driven-development-tdd-in-spring-boot-with-junit-and-mockito-0da0a247e5ef](https://medium.com/@vipisanan1/test-driven-development-tdd-in-spring-boot-with-junit-and-mockito-0da0a247e5ef)  
27. Mock Testing Java With Mockito \- JRebel, accessed on January 24, 2026, [https://www.jrebel.com/blog/mock-unit-testing-with-mockito](https://www.jrebel.com/blog/mock-unit-testing-with-mockito)  
28. Utilizing Spies in Test Driven Development with Mockito | CodeSignal Learn, accessed on January 24, 2026, [https://codesignal.com/learn/courses/isolating-dependencies-with-test-doubles-with-java-mockito/lessons/integrating-spies-into-tdd-with-java-junit-and-mockito](https://codesignal.com/learn/courses/isolating-dependencies-with-test-doubles-with-java-mockito/lessons/integrating-spies-into-tdd-with-java-junit-and-mockito)  
29. Experimental Validation of Total Energy Control System for UAVs \- MDPI, accessed on January 24, 2026, [https://www.mdpi.com/1996-1073/13/1/14](https://www.mdpi.com/1996-1073/13/1/14)  
30. An Assessment of Aircraft Control via SISO Control Loops and Total Energy Control, accessed on January 24, 2026, [https://d-nb.info/1251903169/34](https://d-nb.info/1251903169/34)  
31. Plane Architecture Overview — Dev documentation \- ArduPilot, accessed on January 24, 2026, [https://ardupilot.org/dev/docs/plane-architecture.html](https://ardupilot.org/dev/docs/plane-architecture.html)  
32. TECS (Total Energy Control System) for Speed and Height Tuning Guide \- ArduPilot, accessed on January 24, 2026, [https://ardupilot.org/plane/docs/tecs-total-energy-control-system-for-speed-height-tuning-guide.html](https://ardupilot.org/plane/docs/tecs-total-energy-control-system-for-speed-height-tuning-guide.html)  
33. Flight Testing Total Energy Control Autopilot Functionalities for High Altitude Aircraft, accessed on January 24, 2026, [https://elib.dlr.de/197691/1/AIAA\_2024\_TECS.pdf](https://elib.dlr.de/197691/1/AIAA_2024_TECS.pdf)  
34. Real-Time TECS Gain Tuning Using Steepest Descent Method for Post-Transition Stability in Unmanned Tilt-Rotor eVTOLs \- MDPI, accessed on January 24, 2026, [https://www.mdpi.com/2504-446X/9/6/414](https://www.mdpi.com/2504-446X/9/6/414)  
35. Total Energy Controller \- ROSflight, accessed on January 24, 2026, [https://docs.rosflight.org/latest/developer-guide/rosplane/controller/controller-total-energy/](https://docs.rosflight.org/latest/developer-guide/rosplane/controller/controller-total-energy/)  
36. Computational Experimental Test on PID Controlled Fixed Wing Aircraft Systems \- ICCK, accessed on January 24, 2026, [https://www.icck.org/article/abs/tscc.2025.731885](https://www.icck.org/article/abs/tscc.2025.731885)  
37. Controller Diagrams | PX4 Guide (main), accessed on January 24, 2026, [https://docs.px4.io/main/en/flight\_stack/controller\_diagrams](https://docs.px4.io/main/en/flight_stack/controller_diagrams)  
38. tekdemo/MiniPID-Java: PID controller designed for quickly and easily implementing stable closed loop control. \- GitHub, accessed on January 24, 2026, [https://github.com/tekdemo/MiniPID-Java](https://github.com/tekdemo/MiniPID-Java)  
39. pid controller \- PID working with sine wave as reference in simulink \- Stack Overflow, accessed on January 24, 2026, [https://stackoverflow.com/questions/31048052/pid-working-with-sine-wave-as-reference-in-simulink](https://stackoverflow.com/questions/31048052/pid-working-with-sine-wave-as-reference-in-simulink)  
40. Mock Java Date/Time for Testing \- DZone, accessed on January 24, 2026, [https://dzone.com/articles/mock-java-datetime-for-testing](https://dzone.com/articles/mock-java-datetime-for-testing)  
41. The Art of Mocking in TDD \- Bits and Pieces, accessed on January 24, 2026, [https://blog.bitsrc.io/the-art-of-mocking-in-tdd-d81edf9f8f02](https://blog.bitsrc.io/the-art-of-mocking-in-tdd-d81edf9f8f02)  
42. Test-Driven Development with Java, published by Packt. \- GitHub, accessed on January 24, 2026, [https://github.com/PacktPublishing/Test-Driven-Development-with-Java](https://github.com/PacktPublishing/Test-Driven-Development-with-Java)  
43. SimConnect Tutorial \- FSDeveloper, accessed on January 24, 2026, [https://fsdeveloper.com/forum/attachments/simconnect-tutorial-0-800-pdf.38681/](https://fsdeveloper.com/forum/attachments/simconnect-tutorial-0-800-pdf.38681/)  
44. SimConnect API Reference \- SDK Documentation, accessed on January 24, 2026, [https://docs.flightsimulator.com/html/Programming\_Tools/SimConnect/SimConnect\_API\_Reference.htm](https://docs.flightsimulator.com/html/Programming_Tools/SimConnect/SimConnect_API_Reference.htm)  
45. SimConnect Samples \- SDK Documentation \- Microsoft Flight Simulator, accessed on January 24, 2026, [https://docs.flightsimulator.com/html/Samples\_And\_Tutorials/Samples/VisualStudio/SimConnect\_Samples.htm](https://docs.flightsimulator.com/html/Samples_And_Tutorials/Samples/VisualStudio/SimConnect_Samples.htm)  
46. How TDD Mocking Can Enhance Test-Driven Development \- Functionize, accessed on January 24, 2026, [https://www.functionize.com/blog/how-test-mocking-can-enhance-tdd](https://www.functionize.com/blog/how-test-mocking-can-enhance-tdd)  
47. SimConnect SDK \- SDK Documentation \- Microsoft Flight Simulator, accessed on January 24, 2026, [https://docs.flightsimulator.com/msfs2024/html/6\_Programming\_APIs/SimConnect/SimConnect\_SDK.htm](https://docs.flightsimulator.com/msfs2024/html/6_Programming_APIs/SimConnect/SimConnect_SDK.htm)  
48. Using the SimConnect SDK \- A SimVar Request handler (and potential SDK replacement) | MSFS 2020 \- Page 4 \- Community Guides \- Microsoft Flight Simulator Forums, accessed on January 24, 2026, [https://forums.flightsimulator.com/t/using-the-simconnect-sdk-a-simvar-request-handler-and-potential-sdk-replacement-msfs-2020/369464?page=4](https://forums.flightsimulator.com/t/using-the-simconnect-sdk-a-simvar-request-handler-and-potential-sdk-replacement-msfs-2020/369464?page=4)  
49. Foreign Function and Memory API in Native Image \- Oracle Help Center, accessed on January 24, 2026, [https://docs.oracle.com/en/graalvm/jdk/25/docs/reference-manual/native-image/native-code-interoperability/ffm-api/](https://docs.oracle.com/en/graalvm/jdk/25/docs/reference-manual/native-image/native-code-interoperability/ffm-api/)  
50. c-true/FsConnect: Wrapper for Flight Simulator 2020 SimConnect library \- GitHub, accessed on January 24, 2026, [https://github.com/c-true/FsConnect](https://github.com/c-true/FsConnect)  
51. 12 Foreign Function and Memory API \- Java \- Oracle Help Center, accessed on January 24, 2026, [https://docs.oracle.com/en/java/javase/25/core/foreign-function-and-memory-api.html](https://docs.oracle.com/en/java/javase/25/core/foreign-function-and-memory-api.html)  
52. MSFS 2020 / SimConnect \- questions \- ClarionHub, accessed on January 24, 2026, [https://clarionhub.com/t/msfs-2020-simconnect/3945](https://clarionhub.com/t/msfs-2020-simconnect/3945)  
53. Automated Testing of Graphical Models in Heterogeneous Test Environments | SE@RWTH, accessed on January 24, 2026, [https://www.se-rwth.de/publications/Automated-Testing-of-Graphical-Models-in-Heterogeneous-Test-Environments.pdf](https://www.se-rwth.de/publications/Automated-Testing-of-Graphical-Models-in-Heterogeneous-Test-Environments.pdf)  
54. ardupilot/libraries/AP\_TECS/AP\_TECS.cpp at master \- GitHub, accessed on January 24, 2026, [https://github.com/ArduPilot/ardupilot/blob/master/libraries/AP\_TECS/AP\_TECS.cpp](https://github.com/ArduPilot/ardupilot/blob/master/libraries/AP_TECS/AP_TECS.cpp)  
55. Implement telemetry collection system to track regressions and CTDs \- Wishlist, accessed on January 24, 2026, [https://forums.flightsimulator.com/t/implement-telemetry-collection-system-to-track-regressions-and-ctds/298790](https://forums.flightsimulator.com/t/implement-telemetry-collection-system-to-track-regressions-and-ctds/298790)  
56. MSFS24 \- I finally made a SimConnect module\! Grok Rocks\! | FSDeveloper, accessed on January 24, 2026, [https://www.fsdeveloper.com/forum/threads/i-finally-made-a-simconnect-module-grok-rocks.459787/](https://www.fsdeveloper.com/forum/threads/i-finally-made-a-simconnect-module-grok-rocks.459787/)  
57. Total Energy Control System (TECS) core algorithm. \- ResearchGate, accessed on January 24, 2026, [https://www.researchgate.net/figure/Total-Energy-Control-System-TECS-core-algorithm\_fig3\_338092918](https://www.researchgate.net/figure/Total-Energy-Control-System-TECS-core-algorithm_fig3_338092918)  
58. ardupilot\_wiki/plane/source/docs/tecs-total-energy-control-system-for-speed-height-tuning-guide.rst at master \- GitHub, accessed on January 24, 2026, [https://github.com/ArduPilot/ardupilot\_wiki/blob/master/plane/source/docs/tecs-total-energy-control-system-for-speed-height-tuning-guide.rst](https://github.com/ArduPilot/ardupilot_wiki/blob/master/plane/source/docs/tecs-total-energy-control-system-for-speed-height-tuning-guide.rst)  
59. Airborne Instrumentation | International Telemetry Conference, accessed on January 24, 2026, [https://telemetry.org/sessions/airborne-instrumentation/](https://telemetry.org/sessions/airborne-instrumentation/)  
60. Faster Than A Speeding Aircraft: Advances in Flight Test Telemetry, accessed on January 24, 2026, [https://aerospacetechreview.com/faster-than-a-speeding-aircraft-advances-in-flight-test-telemetry/](https://aerospacetechreview.com/faster-than-a-speeding-aircraft-advances-in-flight-test-telemetry/)
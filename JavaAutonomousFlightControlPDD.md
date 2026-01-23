# **Project Definition Document: Java-Based Autonomous Flight Control System for Microsoft Flight Simulator 2020 (NativeNavJ)**

## **1\. Executive Summary**

### **1.1 Project Overview and Strategic Imperative**

The simulation domain stands at a critical technological juncture where the deterministic rigidity of traditional state machines interacts with the probabilistic, adaptive capabilities of Generative Artificial Intelligence. This Project Definition Document (PDD) establishes the comprehensive technical and operational framework for "NativeNavJ," a Java-based autonomous flight control system designed specifically for Microsoft Flight Simulator 2020 (MSFS 2020). The primary objective is to engineer a local, autonomous agent capable of piloting complex aircraft through natural language instruction, thereby bridging the gap between high-fidelity flight simulation and cognitive computing.  
NativeNavJ represents a paradigm shift from conventional "autopilot" systems, which strictly adhere to numerical setpoints, to an "intelligent agent" capable of interpreting intent, managing checklists, and executing multi-stage aviation procedures. By leveraging the robustness of the Java ecosystem—specifically the LangChain4j framework for AI orchestration—and the raw performance of local Large Language Models (LLMs) via Ollama, the project aims to demonstrate that enterprise-grade, cognitive autonomy can be achieved within a consumer simulation environment without reliance on cloud-based inference, thus ensuring data privacy and minimizing latency costs.1  
The system is architected to run entirely on the user's local hardware, interacting with MSFS 2020 through a custom-engineered integration layer that navigates the complexities of the SimConnect SDK. This document details the architectural decisions, risk mitigation strategies, and technical specifications required to build a system where a user can issue a command such as "Request clearance, taxi to the active runway, and perform a standard takeoff," and observe the agent execute the thousands of discrete micro-actions required to fulfill that intent safely and effectively.

### **1.2 Business Case and Technical Rationale**

The current landscape of flight simulation add-ons is dominated by C++ and C\# applications due to the native nature of the MSFS SDK.3 However, the global enterprise software ecosystem is heavily invested in Java, and the recent advent of "Project Loom" (Virtual Threads) and AI-integration libraries like LangChain4j has made Java a tier-one contender for high-concurrency, AI-driven applications.1  
Developing NativeNavJ serves multiple strategic purposes:

1. **Proof of Viability for Java in High-Frequency Simulation:** It challenges the hegemony of C++ in simulation by demonstrating that Java's modern Foreign Function & Memory API (Project Panama) and JNI can handle the 30Hz+ control loops required for flight dynamics without garbage collection (GC) pauses destabilizing the aircraft.3  
2. **Autonomous Agent Research:** It functions as a testbed for "Embodied AI," where an LLM controls a physical (simulated) vehicle. The challenges encountered here—hallucination management, latency compensation, and safety guardrails—are directly transferable to real-world autonomous systems robotics and drone control.6  
3. **Local Inference Optimization:** By strictly utilizing local LLMs (Llama 3, Mistral), the project addresses the critical need for offline-capable AI, optimizing inference speeds on consumer hardware to meet real-time control deadlines.2

### **1.3 High-Level Project Objectives**

The successful delivery of NativeNavJ is predicated on achieving the following core objectives:

* **Objective 1: Robust SimConnect Integration.** Develop a fault-tolerant Java interface to MSFS 2020 that overcomes the known stability issues (Exception 31 crashes) associated with legacy wrappers like jSimConnect and simconnect-java-util.8  
* **Objective 2: Cognitive-Kinetic Decoupling.** Implement a "Dual-Loop" architecture that separates the low-frequency, high-latency reasoning of the LLM (Cognitive Layer) from the high-frequency, real-time control execution (Kinetic Layer), ensuring flight stability during inference pauses.7  
* **Objective 3: Natural Language Command & Control.** Enable the system to parse unstructured voice or text commands into structured, type-safe Java tool calls using LangChain4j's function-calling capabilities.11  
* **Objective 4: Deterministic Safety.** Deploy a rigorous "Safety Envelope" subsystem that validates all AI-generated commands against aircraft performance limits and terrain data before execution, neutralizing the risk of LLM hallucinations causing catastrophic flight states.13

## ---

**2\. Project Scope and Boundary Analysis**

### **2.1 In-Scope Deliverables**

The scope of NativeNavJ is strictly defined to ensure focused development on the critical path of autonomous control. The deliverables are categorized by their functional domain.

#### **2.1.1 Core Software Artifacts**

* **The Pilot Agent Application:** A standalone Java 21+ application serving as the central nervous system. It hosts the LangChain4j orchestration, the SimConnect bridge, and the safety guardrail logic.  
* **Custom JNI Bridge Library:** A purpose-built C++ Dynamic Link Library (DLL) and corresponding Java Native Interface (JNI) classes. This component is essential to bypass the limitations of existing, unmaintained Java wrappers and provide direct, crash-resistant access to the MSFS SimConnect.dll.3  
* **LLM Tool Suite:** A comprehensive library of @Tool annotated Java methods allowing the LLM to interact with the aircraft systems. This includes tools for Radio Tuning, Autopilot Management, Mechanization (Flaps/Gear), and Telemetry Retrieval.12  
* **Local Inference Configuration:** A validated configuration profile for Ollama, optimized for running 7B-parameter models (e.g., Llama 3, Mistral) alongside MSFS on a single high-end consumer PC.16

#### **2.1.2 Documentation and Verification**

* **Safety Case Report:** A document detailing the "Guardrails" logic, specifically how the system handles hallucinated inputs (e.g., negative altitudes, impossible frequencies).6  
* **SITL Test Harness:** A "Software-In-The-Loop" mock simulator that mimics the SimConnect API, allowing the AI agent's logic to be tested and refined without the overhead of loading the full MSFS simulator.19

### **2.2 Out-of-Scope Items**

To maintain project velocity and manage complexity, the following elements are explicitly excluded from the current project phase:

* **Custom Aircraft Flight Models:** The system will control *default* MSFS aircraft (Cessna 172, A320). Creating new flight dynamics or 3D models is outside the domain of this control system.  
* **Visual Scenery Parsing:** The agent relies on *data* (SimVars) for navigation, not computer vision. It will not "look" out the window to identify runways; it will query GPS coordinates.21  
* **Cloud-Based API Integration:** To strictly adhere to the "local" requirement and cost constraints, integration with OpenAI (GPT-4) or Anthropic APIs is excluded. The system relies entirely on local quantization.2  
* **Multiplayer Formation Flying:** While the system handles its own aircraft, complex coordination with other human players or AI entities in a networked environment is not a launch requirement.

### **2.3 Assumptions and Constraints**

| Constraint ID | Description | Impact Analysis |
| :---- | :---- | :---- |
| **C-01: SimConnect Fragility** | The MSFS SimConnect API is known to be temperamental. Improper data alignment or invalid Request IDs can crash the entire simulator (Exception 31).8 | **Critical:** The JNI Bridge must implement "defensive marshalling" and strictly validate memory layouts before every call. A "Watchdog" process may be needed to detect sim crashes. |
| **C-02: Hardware Resource Contention** | MSFS 2020 is GPU/CPU intensive. Running a local LLM (Ollama) simultaneously creates resource contention for VRAM and System RAM.16 | **High:** The project assumes a minimum hardware spec of 32GB RAM and 12GB VRAM. LLM Offloading to CPU may be necessary, impacting latency. |
| **C-03: Inference Latency** | Local LLMs on consumer hardware may take 500ms–3000ms to process complex reasoning chains.7 | **High:** The control architecture must be asynchronous. The plane cannot wait for the LLM to "think" before correcting a bank angle. |
| **C-04: Java Native Access** | Direct access to C++ libraries requires careful management of native memory to avoid JVM crashes.3 | **Medium:** Usage of java.lang.foreign (Project Panama) or trusted JNI patterns is required to ensure memory safety. |

## ---

**3\. Comprehensive Technical Architecture**

The architecture of NativeNavJ is defined by the necessity to decouple the slow, probabilistic reasoning of the Large Language Model from the fast, deterministic physics of the flight simulator. This results in a **Hierarchical Control Architecture**, reminiscent of the relationship between a Captain (Strategic/Cognitive) and a First Officer/Autopilot (Tactical/Kinetic).

### **3.1 System Context and Data Flow**

The data flow within the system is circular and continuous, operating on two distinct timebases.

1. **The Kinetic Loop (30Hz \- 60Hz):**  
   * **Input:** Raw telemetry from MSFS (Altitude, Airspeed, Pitch, Bank, GPS Position) via SimConnect.24  
   * **Process:** The Java Executive Layer compares current state against target state (set by the LLM). It runs PID control loops or sends direct SimVar updates to the aircraft's internal autopilot (e.g., updating the Heading Bug).25  
   * **Output:** SimConnect setData calls to update aircraft control surfaces or avionics settings.  
   * **Characteristic:** Deterministic, low-latency, strictly typed.  
2. **The Cognitive Loop (0.2Hz \- 1Hz):**  
   * **Input:** A "State Summary" text or JSON object generated by the Executive Layer (e.g., "Status: Cruising, Alt: 5000ft, Goal: Reach Waypoint ALPHA").  
   * **Process:** The Local LLM (via LangChain4j) analyzes the state against the Mission Plan and User Instructions. It utilizes "Chain of Thought" reasoning to determine the next high-level action.13  
   * **Output:** A structured Tool Call (e.g., set\_altitude(6000), tune\_radio("128.50")).  
   * **Characteristic:** Probabilistic, high-latency, semantic.

### **3.2 Subsystem 1: The Simulator Interface (SimConnect Bridge)**

The foundation of the entire system is the connection to MSFS. Research indicates that existing Java wrappers like jSimConnect and simconnect-java-util rely on older protocols (Named Pipes) or unmaintained codebases that are prone to causing "Exception 31" crashes in MSFS 2020\.8 Consequently, a bespoke integration strategy is required.

#### **3.2.1 The JNI Wrapper Strategy**

To ensure stability, NativeNavJ will utilize the **Java Native Interface (JNI)** to wrap the official MSFS 2020 SimConnect.dll. This allows the Java application to act as a first-class client, indistinguishable from a C++ add-on.

* **Architecture:** A lightweight C++ middleware (NativeNavJ.dll) serves as a translation layer. It handles the raw DispatchProc messages from SimConnect and marshals the data into Java ByteBuffers.3  
* **Data Marshalling:** This is the most critical technical detail. SimConnect returns C-structs. Java must read these bytes exactly.  
  * *Example:* If a C-struct defines double latitude (8 bytes), double longitude (8 bytes), and int rank (4 bytes), the Java ByteBuffer must read them in that exact order and endianness. Any misalignment results in garbage data or simulator crashes.27  
* **Event Handling:** The bridge maps String-based Event IDs (e.g., "AP\_MASTER") to integer handles required by the SimConnect engine, managing the MapClientEventToSimEvent calls internally.

#### **3.2.2 Connectivity and Recovery**

The SimConnect protocol is not stateless; if the simulator restarts, the connection is lost (Exception 5). The Java wrapper must implement a **Self-Healing Connection Mechanism**:

1. **Detection:** Catch IOException or specific SimConnect error codes indicating pipe failure.9  
2. **Back-off:** Enter a "Reconnecting" state, pausing all LLM tool calls.  
3. **Polling:** Attempt to re-establish the handshake every 5 seconds.  
4. **Resynchronization:** Once connected, re-register all Data Definitions and Event Subscriptions before resuming control.28

### **3.3 Subsystem 2: The Cognitive Engine (LangChain4j & Ollama)**

This subsystem provides the "intelligence." It translates vague human intent into precise machine instructions.

#### **3.3.1 LangChain4j Orchestration**

LangChain4j acts as the framework for interacting with the LLM. It provides the **AI Service** abstraction, which creates a proxy around the LLM, allowing it to be called like a standard Java method.1

* **Model Provider:** The system allows hot-swapping models via the OllamaChatModel builder.  
* **Memory:** MessageWindowChatMemory is used to retain the context of the flight (e.g., "Remember I asked you to keep speed below 200 knots").  
* **Structured Outputs:** The system relies heavily on **Tool Calling**. Instead of parsing raw text response, LangChain4j instructs the LLM to generate JSON adhering to a specific schema. The framework then automatically deserializes this JSON into a Java method call.11

#### **3.3.2 Tool Definitions and Schema Generation**

The LLM's ability to control the plane is defined by the tools exposed to it. We define a Java interface FlightTools where methods are annotated with @Tool.  
**Example Tool Definition:**

Java

@Tool("Sets the Autopilot Altitude Hold. Input must be in feet, between 0 and 45000.")  
public void setAltitude(@P("Target altitude in feet") int altitude) {  
    if (altitude \< 0 |

| altitude \> 45000\) {  
        throw new ToolExecutionException("Altitude out of safe bounds.");  
    }  
    simConnect.setData("AUTOPILOT ALTITUDE LOCK VAR", altitude);  
    simConnect.sendEvent("AP\_ALT\_HOLD\_ON");  
}

*Insight:* The description inside the @Tool annotation is not just documentation; it is the **prompt** that the LLM sees. It must be explicit about units (feet vs meters) and constraints to minimize hallucination.12

#### **3.3.3 Handling Hallucinations with RAG**

LLMs often hallucinate procedures (e.g., inventing a "Deploy Speedbrakes" command for a Cessna 172 which has none). To mitigate this, we employ **Retrieval-Augmented Generation (RAG)**.18

* **Implementation:** The Pilot's Operating Handbook (POH) for the active aircraft is chunked and stored in a local vector store (embedded in the Java app).  
* **Workflow:** When a user commands "Prepare for landing," the system retrieves the "Before Landing Checklist" from the vector store and injects it into the prompt. The LLM is then constrained to use *only* the tools relevant to that specific aircraft's capabilities.

### **3.4 Subsystem 3: The Executive Layer (Control & Safety)**

While the LLM provides guidance, the Executive Layer ensures survival. It is the deterministic "Guardian" of the aircraft.

#### **3.4.1 The Safety Envelope (Guardrails)**

Research into autonomous systems emphasizes the need for "Decision-Time Cognitive Guardrails" and "Runtime Safety Envelopes".6 NativeNavJ implements these as a chain of Java interceptors that run *before* any Tool Call is executed.

* **Geospatial Guardrails:** Prevents commands that would fly the aircraft into known terrain. It checks the target altitude against the current GROUND ALTITUDE SimVar.  
* **Aerodynamic Guardrails:** Prevents stalls or overspeeds. If the LLM requests an airspeed of 40 knots (stall territory), the Guardrail overrides it to the minimum safe speed (Vs \+ 10%).13  
* **Syntax Guardrails:** Validates formats (e.g., ensuring COM frequencies are valid channels like "118.50" and not "118.999").33

#### **3.4.2 State Machine for Complex Maneuvers**

Some instructions are processes, not instantaneous actions (e.g., "Fly the traffic pattern"). The LLM cannot simply say "Do Pattern." The Executive Layer implements a **Finite State Machine (FSM)**.

1. **LLM Action:** Calls tool initiateTrafficPattern(Runway 27).  
2. **Executive Layer:** Enters PATTERN\_STATE.  
   * *Sub-state 1 (Upwind):* Maintain runway heading, climb to 1000ft AGL.  
   * *Sub-state 2 (Crosswind):* Turn 90 degrees left.  
   * *Sub-state 3 (Downwind):* Level off, reduce power. The Java code manages the transition between sub-states based on telemetry thresholds, relieving the LLM of the need to micro-manage the flight loop.20

## ---

**4\. Implementation Details and Data Structures**

### **4.1 SimVar Data Mapping**

Efficient communication requires precise mapping of SimVars. The following table illustrates the core variables required for autonomous operation and their units.21

| SimVar Name | Unit | Type | Usage |
| :---- | :---- | :---- | :---- |
| PLANE LATITUDE | Degrees | Double | GPS Navigation |
| PLANE LONGITUDE | Degrees | Double | GPS Navigation |
| PLANE ALTITUDE | Feet | Double | Vertical Guidance |
| AIRSPEED INDICATED | Knots | Double | Speed Control |
| PLANE HEADING DEGREES MAGNETIC | Degrees | Double | Heading Control |
| AUTOPILOT MASTER | Bool | Integer | AP State Management |
| AUTOPILOT ALTITUDE LOCK VAR | Feet | Double | AP Target Setting |
| AUTOPILOT HEADING LOCK DIR | Degrees | Double | AP Target Setting |
| GPS WP NEXT ID | String | String | Flight Plan Tracking |
| FUEL TOTAL QUANTITY | Gallons | Double | Endurance Calculation |

### **4.2 LLM Configuration Strategy**

The "Local" constraint is a significant driver of the technical configuration. The following specifications are derived from hardware benchmark research.16

* **Inference Engine:** Ollama (running as a background service).  
* **Model Selection:**  
  * *Primary:* **Llama 3 8B (Quantized Q4\_K\_M)**. Offers the best balance of reasoning capability and memory footprint (\~5GB RAM).  
  * *Alternative:* **Mistral 7B**. Known for strong instruction following capabilities.  
* **Context Window:** 4096 Tokens. Sufficient to hold the "System Prompt" \+ "Current Telemetry" \+ "Recent Conversation History."  
* **Temperature:** 0.2. Low temperature is critical for agentic tasks to ensure deterministic tool selection and reduce "creative" hallucinations.10

### **4.3 Prompt Engineering: The System Prompt**

The system prompt is the "source code" for the cognitive layer. It must be engineered to enforce the pilot persona.  
**System Prompt Specification:**  
"You are NativeNavJ, an advanced autonomous flight control agent. You have access to the aircraft via specific Tools.  
**CRITICAL PROTOCOLS:**

1. **Safety First:** Never execute a command that endangers the aircraft. Validate altitudes against ground level.  
2. **Tool Usage:** Do not describe actions; PERFORM them using the provided tools. If you need to turn, call setHeading().  
3. **State Awareness:** You are provided with the current telemetry JSON. Read it before deciding on an action.  
4. **Brevity:** Keep conversational responses short. Focus on execution.  
5. **Uncertainty:** If a user command is ambiguous (e.g., 'Turn left'), ask for clarification (e.g., 'Turn left to what heading?')."

## ---

**5\. Risk Management and Mitigation Strategies**

Developing for a closed-source, third-party platform (MSFS) introduces significant risks.

### **5.1 Technical Risks**

| Risk | Probability | Impact | Mitigation Strategy |
| :---- | :---- | :---- | :---- |
| **SimConnect Crash (Exception 31\)** | High | Critical | Implement strictly typed JNI bridges. Use "Try/Catch" blocks around every native call. Implement a "Watchdog" process that restarts the connection without crashing the Java app.8 |
| **LLM Hallucination** | Medium | High | Implement "Deterministic Guardrails" in Java. The code validates every tool parameter (e.g., Altitude \> 0, Heading 0-360) before sending it to SimConnect.6 |
| **Inference Latency (\>2s)** | High | Medium | Use "Dual-Loop" architecture. The LLM sets the *goal* (e.g., "Climb to 5000"), but the Java autopilot handles the *execution* (vertical speed management). The plane flies safely while the LLM thinks.7 |
| **VRAM Saturation** | Medium | High | Use quantized models (GGUF). Monitor VRAM usage. If MSFS performance degrades, offload the LLM calculation to the CPU (accepting higher latency) or use a networked laptop for the Agent.34 |

### **5.2 Operational Risks**

* **Model Updates:** An update to the Llama 3 model might change how it interprets prompts, breaking tool calling reliability. *Mitigation:* Pin the specific model hash in the Ollama configuration.  
* **MSFS SDK Updates:** Microsoft frequently updates the SimConnect library, which can break binary compatibility. *Mitigation:* Isolate the JNI wrapper in a separate module so it can be recompiled independently of the main application.3

## ---

**6\. Implementation Roadmap**

The project will be executed in a phased approach, ensuring that the critical "Communication Bridge" is stable before introducing the "Cognitive Layer."

### **Phase 1: Foundation (Weeks 1-4)**

* **Goal:** Establish stable, crash-free communication between Java and MSFS.  
* **Activities:**  
  * Develop SkyMindBridge.dll (C++) and SimConnectService.java.  
  * Implement connection watchdog and auto-reconnect logic.  
  * Create a simple "Dashboard" app that displays Attitude, Altitude, and GPS coordinates in real-time.  
  * **Milestone:** Java app running for 1 hour connected to MSFS without crashing or leaking memory.

### **Phase 2: The Agent's Body (Weeks 5-8)**

* **Goal:** Create the Executive Layer and Tools.  
* **Activities:**  
  * Implement the FlightTools library (Autopilot, Radios, Gear, Flaps).  
  * Implement the SafetyEnvelope validation logic.  
  * Develop the PID controller logic for managed flight modes.  
  * **Milestone:** Ability to control the plane via hard-coded Java commands (e.g., a script that initiates takeoff and climb).

### **Phase 3: The Agent's Mind (Weeks 9-12)**

* **Goal:** Integrate LangChain4j and Ollama.  
* **Activities:**  
  * Configure local Ollama instance.  
  * Map FlightTools to LangChain4j @Tool annotations.  
  * Refine the System Prompt and Context Injection logic.  
  * Implement the Chat Interface for user input.  
  * **Milestone:** User types "Turn on the lights," and the aircraft landing lights toggle in the sim.

### **Phase 4: Integration and Refinement (Weeks 13-16)**

* **Goal:** Full autonomy and testing.  
* **Activities:**  
  * Implement RAG for aircraft checklists.  
  * Conduct "Software-In-The-Loop" (SITL) stress testing.  
  * Optimize inference latency (KV Cache warming).  
  * Finalize documentation and packaging.  
  * **Milestone:** Complete a full "Gate-to-Gate" flight (Taxi, Takeoff, Cruise, Land) controlled entirely by voice/text commands.

## ---

**7\. Testing Strategy**

Given the complexity of testing AI behavior (non-deterministic) against a physics simulator (deterministic), a robust testing strategy is essential.36

### **7.1 Software-In-The-Loop (SITL)**

Loading MSFS takes several minutes, making the "Code-Test-Debug" cycle painfully slow. To solve this, we will develop a **Mock Simulator**.

* **Concept:** A Java class MockSimConnect that implements the ISimConnect interface.  
* **Function:** It simulates the physics of a plane using simple vector math. If the Agent calls setPitch(10), the Mock Sim increases the Altitude variable over time.  
* **Benefit:** Allows running thousands of automated test scenarios (e.g., "Test Emergency Descent Logic") in seconds without launching MSFS.19

### **7.2 Safety Verification Scenarios**

We must verify the guardrails.

1. **The "Suicide" Test:** Command the agent to "Dive into the ground." *Expected Result:* Agent refuses or Guardrail intercepts and maintains minimum altitude.  
2. **The "Overspeed" Test:** Command maximum thrust in a dive. *Expected Result:* Executive Layer detects Vne (Velocity Never Exceed) and automatically throttles back or deploys speedbrakes.  
3. **The "Hallucination" Test:** Inject false data (e.g., pretend the LLM asked for frequency 999.99). *Expected Result:* Tool validation logic throws exception; Agent is notified of "Invalid Parameter."

## ---

**8\. Conclusion**

NativeNavJ represents a significant engineering challenge that pushes the boundaries of what is possible with Java in the simulation domain. By acknowledging the limitations of legacy interfaces and the latency inherent in generative AI, the proposed architecture delivers a robust solution. The **Dual-Loop design** ensures that flight safety is never compromised by AI processing time, while the **Custom JNI Bridge** provides the stability required for long-duration flights.  
This project will not only deliver a functional "AI Co-Pilot" for flight simulation enthusiasts but also establish a reusable framework for building autonomous agents in Java—a critical capability for the future of intelligent software systems. The path forward is clear: build the bridge, secure the safety envelope, and then empower the mind.

## ---

**9\. Appendices**

### **9.1 Recommended Hardware Specifications**

* **CPU:** AMD Ryzen 7 5800X3D or Intel Core i7-13700K (High single-core clock for MSFS main thread).17  
* **GPU:** NVIDIA RTX 3080 or better (10GB+ VRAM). MSFS consumes \~6-8GB; the remaining is needed for system overhead if LLM is GPU-offloaded.  
* **RAM:** 32GB DDR4/DDR5 Minimum. (16GB for MSFS \+ 8GB for LLM \+ 8GB System). 64GB recommended.16  
* **Storage:** NVMe SSD for fast model loading and MSFS scenery streaming.

### **9.2 Software Prerequisites**

* **Java Development Kit (JDK):** Version 21 (LTS) or higher (Required for Virtual Threads/LangChain4j).  
* **Build Tool:** Maven 3.8+ or Gradle 8.0+.  
* **Inference Server:** Ollama (Latest Release).  
* **Simulator:** Microsoft Flight Simulator 2020 (Steam or MS Store version) with Developer Mode enabled for SDK access.  
* **SDK:** MSFS 2020 SDK (Core SimConnect Libraries).

**End of Project Definition Document**

#### **Works cited**

1. How to use LLMs in Java with LangChain4j and Quarkus | Red Hat Developer, accessed on January 22, 2026, [https://developers.redhat.com/articles/2024/02/07/how-use-llms-java-langchain4j-and-quarkus](https://developers.redhat.com/articles/2024/02/07/how-use-llms-java-langchain4j-and-quarkus)  
2. Running a local LLM on Ollama and LangChain4J \- dplatz.de, accessed on January 22, 2026, [https://dplatz.de/blog/2024/langchain4j-ollama.html](https://dplatz.de/blog/2024/langchain4j-ollama.html)  
3. SimConnect SDK \- SDK Documentation \- Microsoft Flight Simulator, accessed on January 22, 2026, [https://docs.flightsimulator.com/msfs2024/html/6\_Programming\_APIs/SimConnect/SimConnect\_SDK.htm](https://docs.flightsimulator.com/msfs2024/html/6_Programming_APIs/SimConnect/SimConnect_SDK.htm)  
4. SimConnect SDK \- SDK Documentation \- Microsoft Flight Simulator, accessed on January 22, 2026, [https://docs.flightsimulator.com/html/Programming\_Tools/SimConnect/SimConnect\_SDK.htm](https://docs.flightsimulator.com/html/Programming_Tools/SimConnect/SimConnect_SDK.htm)  
5. From LLM orchestration to autonomous agents: Agentic AI patterns with LangChain4j by Clement Escoffi \- YouTube, accessed on January 22, 2026, [https://www.youtube.com/watch?v=SrFXsjeGc6Q](https://www.youtube.com/watch?v=SrFXsjeGc6Q)  
6. Cognitive Guardrails for Open-World Decision Making in Autonomous Drone Swarms \- arXiv, accessed on January 22, 2026, [https://arxiv.org/html/2505.23576v2](https://arxiv.org/html/2505.23576v2)  
7. Strategies for Reducing LLM Inference Latency and making tradeoffs: Lessons from the trenches | by Sumanta Boral | Aug, 2025 | Medium, accessed on January 22, 2026, [https://medium.com/@sumanta.boral/strategies-for-reducing-llm-inference-latency-and-making-tradeoffs-lessons-from-building-9434a98e91bc](https://medium.com/@sumanta.boral/strategies-for-reducing-llm-inference-latency-and-making-tradeoffs-lessons-from-building-9434a98e91bc)  
8. SimConnect crashes MSFS 2020 \- SDK \- Microsoft Flight Simulator Forums, accessed on January 22, 2026, [https://forums.flightsimulator.com/t/simconnect-crashes-msfs-2020/737297](https://forums.flightsimulator.com/t/simconnect-crashes-msfs-2020/737297)  
9. v0.4.6.0 SimConnect Interface, Installer and Auto Increase Clouds Improvements · ResetXPDR MSFS\_AutoFPS · Discussion \#195 \- GitHub, accessed on January 22, 2026, [https://github.com/ResetXPDR/MSFS\_AutoFPS/discussions/195](https://github.com/ResetXPDR/MSFS_AutoFPS/discussions/195)  
10. 9 Smart Ways to Reduce LLM Latency for Faster AI Performance \- Inference.net, accessed on January 22, 2026, [https://inference.net/content/llm-latency](https://inference.net/content/llm-latency)  
11. Calling LLM tools with LangChain4j in MicroProfile and Jakarta EE applications \- Open Liberty, accessed on January 22, 2026, [https://openliberty.io/blog/2025/10/22/open-liberty-with-langchain4j-tools.html](https://openliberty.io/blog/2025/10/22/open-liberty-with-langchain4j-tools.html)  
12. Step-by-Step Guide to Adding AI Features in Spring Boot Using LangChain4j \- Medium, accessed on January 22, 2026, [https://medium.com/@danaprata/step-by-step-guide-to-adding-ai-features-in-spring-boot-using-langchain4j-061ae115858e](https://medium.com/@danaprata/step-by-step-guide-to-adding-ai-features-in-spring-boot-using-langchain4j-061ae115858e)  
13. Robust AI Applications with LangChain4j Guardrails and Spring Boot \- NLJUG, accessed on January 22, 2026, [https://nljug.org/foojay/robust-ai-applications-with-langchain4j-guardrails-and-spring-boot/](https://nljug.org/foojay/robust-ai-applications-with-langchain4j-guardrails-and-spring-boot/)  
14. LLM guardrails: Best practices for deploying LLM apps securely \- Datadog, accessed on January 22, 2026, [https://www.datadoghq.com/blog/llm-guardrails-best-practices/](https://www.datadoghq.com/blog/llm-guardrails-best-practices/)  
15. Part 5: AI Agents with LangChain4j \+ Tool Integration \- DEV Community, accessed on January 22, 2026, [https://dev.to/haraf/part-5-ai-agents-with-langchain4j-tool-integration-2e99](https://dev.to/haraf/part-5-ai-agents-with-langchain4j-tool-integration-2e99)  
16. Ollama \- MindsDB Docs, accessed on January 22, 2026, [https://docs.mindsdb.com/integrations/ai-engines/ollama](https://docs.mindsdb.com/integrations/ai-engines/ollama)  
17. Hardware recommendation help to run Ollama \- Reddit, accessed on January 22, 2026, [https://www.reddit.com/r/ollama/comments/1cn7s9i/hardware\_recommendation\_help\_to\_run\_ollama/](https://www.reddit.com/r/ollama/comments/1cn7s9i/hardware_recommendation_help_to_run_ollama/)  
18. Reducing hallucinations in large language models with custom intervention using Amazon Bedrock Agents | Artificial Intelligence, accessed on January 22, 2026, [https://aws.amazon.com/blogs/machine-learning/reducing-hallucinations-in-large-language-models-with-custom-intervention-using-amazon-bedrock-agents/](https://aws.amazon.com/blogs/machine-learning/reducing-hallucinations-in-large-language-models-with-custom-intervention-using-amazon-bedrock-agents/)  
19. Microsoft Flight Simulator 2020 (MSFS) to Simulink/Matlab using SimConnect\_Part1, accessed on January 22, 2026, [https://www.youtube.com/watch?v=Q7A6mQmPctA](https://www.youtube.com/watch?v=Q7A6mQmPctA)  
20. Microsoft Flight Simulator SITL Roll Controller Simconnect \- File Exchange \- MathWorks, accessed on January 22, 2026, [https://www.mathworks.com/matlabcentral/fileexchange/112235-microsoft-flight-simulator-sitl-roll-controller-simconnect](https://www.mathworks.com/matlabcentral/fileexchange/112235-microsoft-flight-simulator-sitl-roll-controller-simconnect)  
21. Aircraft Radio Navigation Variables \- SDK Documentation \- Microsoft Flight Simulator, accessed on January 22, 2026, [https://docs.flightsimulator.com/html/Programming\_Tools/SimVars/Aircraft\_SimVars/Aircraft\_RadioNavigation\_Variables.htm](https://docs.flightsimulator.com/html/Programming_Tools/SimVars/Aircraft_SimVars/Aircraft_RadioNavigation_Variables.htm)  
22. LLM Inference using 100% Modern Java ☕️ \- DEV Community, accessed on January 22, 2026, [https://dev.to/stephanj/llm-inference-using-100-modern-java-30i2](https://dev.to/stephanj/llm-inference-using-100-modern-java-30i2)  
23. What Hardware Do You Need for Running LLMs on the Desktop? \- Redmondmag.com, accessed on January 22, 2026, [https://redmondmag.com/articles/2025/04/16/what-hardware-do-you-need-for-running-llms-on-the-desktop.aspx](https://redmondmag.com/articles/2025/04/16/what-hardware-do-you-need-for-running-llms-on-the-desktop.aspx)  
24. Simulation Variables \- SDK Documentation, accessed on January 22, 2026, [https://docs.flightsimulator.com/html/Programming\_Tools/SimVars/Simulation\_Variables.htm](https://docs.flightsimulator.com/html/Programming_Tools/SimVars/Simulation_Variables.htm)  
25. Aircraft Autopilot/Assistant Variables \- SDK Documentation \- Microsoft Flight Simulator, accessed on January 22, 2026, [https://docs.flightsimulator.com/html/Programming\_Tools/SimVars/Aircraft\_SimVars/Aircraft\_AutopilotAssistant\_Variables.htm](https://docs.flightsimulator.com/html/Programming_Tools/SimVars/Aircraft_SimVars/Aircraft_AutopilotAssistant_Variables.htm)  
26. jSimConnect \- a simconnect java client library \- GitHub, accessed on January 22, 2026, [https://github.com/mharj/jsimconnect](https://github.com/mharj/jsimconnect)  
27. SimConnect SDK thread safety \- MSFS DevSupport \- Microsoft Flight Simulator, accessed on January 22, 2026, [https://devsupport.flightsimulator.com/t/simconnect-sdk-thread-safety/5102](https://devsupport.flightsimulator.com/t/simconnect-sdk-thread-safety/5102)  
28. Using the SimConnect SDK \- A SimVar Request handler (and potential SDK replacement) | MSFS 2020, accessed on January 22, 2026, [https://forums.flightsimulator.com/t/using-the-simconnect-sdk-a-simvar-request-handler-and-potential-sdk-replacement-msfs-2020/369464](https://forums.flightsimulator.com/t/using-the-simconnect-sdk-a-simvar-request-handler-and-potential-sdk-replacement-msfs-2020/369464)  
29. LangChain4j is an open-source Java library that simplifies the integration of LLMs into Java applications through a unified API, providing access to popular LLMs and vector databases. It makes implementing RAG, tool calling (including support for MCP), and agents easy. LangChain4j integrates seamlessly with various enterprise Java frameworks. \- GitHub, accessed on January 22, 2026, [https://github.com/langchain4j/langchain4j](https://github.com/langchain4j/langchain4j)  
30. langchain4j/docs/docs/tutorials/structured-outputs.md at main \- GitHub, accessed on January 22, 2026, [https://github.com/langchain4j/langchain4j/blob/main/docs/docs/tutorials/structured-outputs.md](https://github.com/langchain4j/langchain4j/blob/main/docs/docs/tutorials/structured-outputs.md)  
31. Tool Calling with LangChain, accessed on January 22, 2026, [https://blog.langchain.com/tool-calling-with-langchain/](https://blog.langchain.com/tool-calling-with-langchain/)  
32. RAG and function calling (Tools) \- Langchain4j on Spring \- OpenAI Developer Community, accessed on January 22, 2026, [https://community.openai.com/t/rag-and-function-calling-tools-langchain4j-on-spring/710966](https://community.openai.com/t/rag-and-function-calling-tools-langchain4j-on-spring/710966)  
33. Ensuring Safe and Reliable AI Interactions with LLM Guardrails \- Snyk, accessed on January 22, 2026, [https://snyk.io/articles/ensuring-safe-and-reliable-ai-interactions-with-llm-guardrails/](https://snyk.io/articles/ensuring-safe-and-reliable-ai-interactions-with-llm-guardrails/)  
34. Best practices for optimizing large language model inference with GPUs on Google Kubernetes Engine (GKE), accessed on January 22, 2026, [https://docs.cloud.google.com/kubernetes-engine/docs/best-practices/machine-learning/inference/llm-optimization](https://docs.cloud.google.com/kubernetes-engine/docs/best-practices/machine-learning/inference/llm-optimization)  
35. The Complete Guide to Running LLMs Locally: Hardware, Software, and Performance Essentials \- IKANGAI, accessed on January 22, 2026, [https://www.ikangai.com/the-complete-guide-to-running-llms-locally-hardware-software-and-performance-essentials/](https://www.ikangai.com/the-complete-guide-to-running-llms-locally-hardware-software-and-performance-essentials/)  
36. MSFS Marketplace Partner Functional Testing of Content \- Microsoft Flight Simulator, accessed on January 22, 2026, [https://www.flightsimulator.com/marketplace-partner-testing/](https://www.flightsimulator.com/marketplace-partner-testing/)  
37. How would you do automated tests for a complex simulation game : r/gamedev \- Reddit, accessed on January 22, 2026, [https://www.reddit.com/r/gamedev/comments/nsy3d1/how\_would\_you\_do\_automated\_tests\_for\_a\_complex/](https://www.reddit.com/r/gamedev/comments/nsy3d1/how_would_you_do_automated_tests_for_a_complex/)
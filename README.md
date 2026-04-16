# PathFuzzer

**Sensitive Information Flow Path-Guided Fuzzing for Intent Vulnerabilities in Android Applications**

PathFuzzer is a prototype system designed to efficiently detect Intent vulnerabilities in Android applications. It leverages Intent-sensitive information flow paths to guide the fuzzing process, significantly improving detection accuracy and efficiency compared to traditional methods.

> **Paper**: *PathFuzzer: Sensitive Information Flow Path-Guided Fuzzing for Intent Vulnerabilities in Android Applications*  
> Published in **IET Information Security**  
> Authors: Zhanhui Yuan, Zhi Yang*, Shuyuan Jin, Jinglei Tan, Hongqi Zhang

---

## Overview

Intent vulnerabilities allow attackers to exploit unverified Intent messages, leading to sensitive data leaks, privilege escalations, or unauthorized actions. Traditional fuzzing methods rely on edge coverage of program-directed graphs and lack the ability to discover vulnerabilities related to sensitive information — especially long-path vulnerabilities.

PathFuzzer addresses this by combining **static path analysis** with **path-guided fuzzing** across four sequential stages:

## Key Features

- **Path-Guided Fuzzing**: Accurately extracts Intent-sensitive information flow paths through static analysis, directing test cases along critical propagation paths instead of testing all components indiscriminately.
- **Long Path Encoding**: Employs a unique path encoding method combined with key node identification, enabling comprehensive representation of sensitive information flow paths.
- **Finite State Automaton Monitoring**: Utilizes FSA-based path state tracking with stack operations (Push/Pop) for real-time monitoring of execution states along long paths.
- **Intelligent Test Case Mutation**: Generates mutated test cases based on path and parameter information (Action, Data, Extras), producing more precise and targeted inputs.
- **Dual Coverage Metrics**: Provides both specific path execution rate and global path coverage rate for comprehensive testing feedback.

## Architecture

PathFuzzer consists of two main components:

- **Server-side Console (PC)**: Handles static analysis and path instrumentation. Analyzes APK files, tracks Intent-sensitive information flow paths, generates path encodings, and instruments target applications.
- **Client Application (Android Device)**: Executes fuzzing and monitors path coverage. Loads path guidance, constructs and injects mutated Intents, and collects real-time coverage feedback.

## Project Structure

```
PathFuzzer/
├── app/                        # Android client application
│   └── src/
│       └── main/
│           ├── java/           # PathFuzzer client source code
│           └── res/            # Android resources
├── gradle/
│   └── wrapper/                # Gradle wrapper files
├── .gradle/                    # Gradle cache
├── .idea/                      # IDE configuration
├── build.gradle                # Project-level build configuration
├── settings.gradle             # Project settings
├── gradle.properties           # Gradle properties
├── gradlew                     # Gradle wrapper script (Unix)
├── gradlew.bat                 # Gradle wrapper script (Windows)
├── local.properties            # Local environment configuration
├── import-summary.txt          # Import summary
└── README.md
```

## Requirements

### Server-side (PC)

- **OS**: Windows 10 or later
- **Java**: JDK 8+
- **SOOT**: For generating inter-procedural control flow graphs (ICFG)
- **FSAFlow**: Provides static path analysis and tracking support

### Client-side (Android)

- **Android SDK**: API Level 29 (Android 10) or above
- **Device/Emulator**: Physical device or emulator (e.g., Pixel 2 with Android 10)

### Build Tools

- **Gradle**: Included via Gradle Wrapper
- **Android Studio**: Recommended IDE

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/PathFuzzer.git
cd PathFuzzer
```

### 2. Build the Client Application

```bash
./gradlew assembleDebug
```

### 3. Run Static Analysis (Server-side)

Perform static analysis on the target APK to extract Intent-sensitive information flow paths:

1. Configure the sensitive information sources and sinks in the policy file.
2. Run the path tracking algorithm to generate path encodings (`P_FSA`).
3. Execute path instrumentation to insert monitoring code at key nodes.
4. Repackage the instrumented APK.

### 4. Install and Run

1. Install the instrumented target APK on the Android device/emulator.
2. Install the PathFuzzer client application.
3. Launch PathFuzzer, select the target application, and begin testing:
   - **Load Path Guidance** — loads path information from static analysis.
   - **Obtain Parameters** — retrieves Intent parameters for test case construction.
   - **Switch Path Guidance** — navigates between different sensitive paths.

## Usage Workflow

```
1. Select Target App     →  Choose from installed applications
2. Load Path Guidance    →  Import static analysis results
3. Obtain Parameters     →  Extract Intent parameters (Action, Data, Extras)
4. Inject Intent         →  Send mutated test cases to target components
5. Monitor Coverage      →  Track path execution states via FSA feedback
6. Analyze Results       →  Review triggered exceptions and confirmed vulnerabilities
```

## How It Works

### Sensitive Source & Sink Selection

- **Sources**: API methods extracting sensitive data from Intent objects, e.g., `getData()`, `getStringExtra()`, `getIntExtra()`, `getLastKnownLocation()`
- **Sinks**: API methods transmitting data externally or executing sensitive operations, e.g., `loadUrl()`, `Runtime.exec()`, `query()`

### Path Tracking

PathFuzzer uses an improved **IFDS (Inter-procedural, Finite, Distributive, Subset)** framework, transforming path tracking into a graph reachability problem. It tracks Intent propagation across all Android communication levels: intra-component, inter-component, and inter-process.

### Test Case Mutation

Mutations target three key Intent parameters:

- **Action**: Standard and custom actions derived from path analysis, with prefix/suffix mutations and illegal character injection.
- **Data**: URI patterns, file paths, MIME types, and structured data formats (JSON/XML) with format-specific mutations.
- **Extras**: Key-value pairs with exceptional values (null, overlong strings, illegal types) and complex nested structures.

## Citation

If you use PathFuzzer in your research, please cite our paper:

```bibtex
@article{yuan2025pathfuzzer,
  title={PathFuzzer: Sensitive Information Flow Path-Guided Fuzzing for Intent Vulnerabilities in Android Applications},
  author={Yuan, Zhanhui and Yang, Zhi and Jin, Shuyuan and Tan, Jinglei and Zhang, Hongqi},
  journal={IET Information Security},
  year={2025}
}
```

## License

Please refer to the LICENSE file for details.
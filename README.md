# PipelineIQ: CI Log Interpreter 🚀

> **A Hybrid AI-Deterministic Engine for Intelligent CI/CD Failure Analysis**

PipelineIQ is a high-performance tool designed to bridge the gap between raw, noisy CI logs and actionable developer insights. It utilizes a hybrid approach: a deterministic regex-based engine to isolate failures, followed by an LLM enrichment layer for root-cause explanation.

## 🛠 Tech Stack

- **Language:** Java 21 (utilizing Records, Virtual Threads, and Modern Switch Expressions)
- **Framework:** Spring Boot 3.4+ (CLI mode)
- **AI Orchestration:** LangChain4j (planned)
- **Containerization:** Docker (planned)

## 🏗 Architecture & Design Principles

The project follows **SOLID principles** and a **Clean Architecture** approach to ensure the system is modular and testable.

*   **Modular Strategy:** High-level business logic depends on abstractions (`LogParser`, `FailureClassifier`, `LLMProvider`) rather than implementations.
*   **Memory Efficiency:** Log parsing is designed for streaming (processing line-by-line) to handle massive enterprise log files without `OutOfMemory` errors.
*   **Privacy-First:** Designed to support local LLMs (via Ollama) to ensure sensitive logs never leave the internal infrastructure.

## 🗺 Project Roadmap

### Phase 1: Core Analysis Engine
*   Development of the deterministic failure classification layer.
*   Implementation of high-performance, streaming log-parsing logic.

### Phase 2: Semantic Intelligence Layer
*   Integration of LLM-based root cause analysis.
*   Development of the "Privacy-First" local inference mode (Ollama support).

### Phase 3: Ecosystem Integration
*   Packaging as a GitHub Action for seamless CI/CD workflow integration.
*   Automated reporting and Pull Request feedback loops.

## 🏗️ Design Philosophy

This system is built with a **Hybrid Intelligence** approach. Unlike pure AI tools that can suffer from hallucinations or high latency, this engine utilizes:

1.  **Deterministic Extraction:** A Java-based logic layer that identifies failure "Hot Zones" using strictly defined build-tool signatures.
2.  **Generative Explanation:** An AI-orchestrated layer that synthesizes human-readable insights from technical stack traces.

By separating these concerns, the engine remains secure (sending minimal data to LLMs), cost-effective, and highly accurate.

## 🚀 Getting Started

### Prerequisites
*   Java 21
*   Maven 3.9+

### Installation & Local Run

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/ci-log-interpreter.git
   ```
2. **Build the project:**
   ```bash
   mvn clean install
   ```
3. **Run the analysis on a sample log:**
   ```bash
   mvn spring-boot:run -Dspring-boot.run.arguments="path/to/your/build.log"
   ```

---

### 🤝 Career Re-entry Note
This project is part of a dedicated career re-entry program focused on modernizing backend expertise in Java 21, Cloud-Native patterns, and AI Integration.
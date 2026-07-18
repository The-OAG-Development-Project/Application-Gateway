# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
- **OAG** The OWASP Application Gateway. 
- **Tech-Stack:** SpringCloudGateway · Java 17 · Gradle 9 · docker · vitepress (for documentation) 

## Build & Run Commands

The Gradle project lives in `oag/`; documentation (vitepress) lives in `www/`.

```bash
# --- Application (backend) — run from oag/ ---
cd oag
./gradlew build              # compile, run unit + integration tests, assemble oag.jar
./gradlew assemble           # build oag.jar without running tests -> build/libs/oag.jar
./gradlew test               # run tests only
./gradlew bootRun            # run the gateway locally (port 8080)
java -jar build/libs/oag.jar # run the assembled jar

# --- Documentation (vitepress) — run from www/ ---
cd www
npm install                  # install doc dependencies (first time)
npm run docs:dev             # serve docs with live reload
npm run docs:build           # build static docs
npm run docs:preview         # preview the built docs

# --- Docker (from repository root) ---
docker build -t owasp/application-gateway:SNAPSHOT .
docker run -p 8080:8080 owasp/application-gateway:SNAPSHOT
```

Default credentials: `admin` / `admin`. App runs on port 8080. Frontend served at `/ui/`.

## Module Architecture

Gradle project paths are flat (top-level), even though some modules are grouped in
subfolders on disk. The arrow shows each folder's Gradle project path.

```
Application-Gateway/                               (repository root)
├── .github                                        Workflow definition for GitHub
├── doc                                            Old documentation directory, now containing mainly pictures, should be cleaned up and pictures moved
├── oag                                            The OAG application, built by gradle
├── www                                            Documentation for the current implementation (vitepress based)
│   ├──docs                                        intermediate directory
│   │  ├──.vitepress                               Vitepress config
│   │  ├──docs                                     documentation root
│   │  │  ├──images                                pictures for documentation
│   │  │  └──*.md                                  The documentation file/text
│   │  ├──public                                   public
│   │  └──index.md                                 Documentation configuration for vitepress
│   └──package.json                                The npm packages used for vitepress/documentation
├── implementation-progress                        Iteration plans (5-digit-numbered .md files) (no gradle module) store also AI documents here.
└── ...                                            Basic files and the Dockerfile 
```

All modules declare `org.owasp.oag` as base package and extend it.

## Working Directives
- Use English in Code, Properties/Preferences, Plan and implementation-progress.
- Use English for documentation. 
- For documentation representing the state of the system (e.g. most of what is in /docs except superpowers directory) as well as comment in code, do not document things like "this was changed in task " or "before it was like y" or "this closes issue z", etc.
- **Work in small increments.** Plan the next small increment — store the `iteration-plan` for each iteration in folder `implementation-progress` as a file `5DigitIterationNumber-NameOfFeature.md`. Then let it review and improve the plan. Only then start the implementation. Track the implementation progress in the plan. Mark each checklist item as done in the plan file immediately after completing it.
- **Always use TDD.** Create unit tests (in folder `test`), integration tests (in folder `integration-test` — it is an integration test as soon as other processes are involved, like file system, Mock or real servers, HTTP server, etc. A `contextLoads()` test alone is never sufficient — every implemented behavior must have a test that verifies its actual output). This is a todo to cleanup into this.
- Prefer clean code. Short, easy to read.
- Where reasonable use patterns.
- Only add comments to explain complicated code.
- Add documentation to all public, protected, package private classes, methods and fields.
- Use existing libraries when it saves 30 or more lines of code.
- For diagrams use mermaid
- **When reviewing code.** Verify that there are
  - No cyclic dependencies between modules and packages
  - Classes are named according to specification/glossar
  - Classes are in proper packages
  - Classes are not to big
  - Clean architecture practices are followed.
  - Tests have been implemented and cover relevant functionality
  - The documentation has been updated to reflect changes and decisions.
  - The glossar has been updated when required.

## Definition of Done

A task is done when:
- The iteration-plan is marked as finished in the plan.
- Tests (Unit, Integration) are written and pass.
- The task's requirements are implemented.
- All documentation is updated

## Configuration Notes

- **Single `spring:` block per YAML file** — SnakeYAML merges duplicate keys unpredictably; keep all Spring config under one top-level `spring:` key.

# AGENTS.md

This file provides guidance to AI Agents when working with code in this repository.

## Project Overview

Java feature playground demonstrating JDK features from version 8 through 26. Each JDK version has its own Maven module (`java8/`, `java9/`, ..., `java26/`) with example code and tests under the `cz.cacek.test` package.

## Build & Test Commands

```bash
# Full build with tests (modules activated depend on your JDK version)
mvn verify

# Build without tests
mvn verify -DskipTests

# Run tests for a specific module
mvn test -pl java21

# Run a single test class
mvn test -pl java21 -Dtest=Java21Test

# Run a single test method
mvn test -pl java21 -Dtest=Java21Test#testSealedClasses
```

## Architecture

- **Multi-module Maven project** with a parent POM that activates modules via profiles based on the build JDK version:
  - JDK 8+: `java8` (always active)
  - JDK 11+: `java9` through `java11`
  - JDK 17+: `java12` through `java17`
  - JDK 21+: `java18` through `java21`
  - JDK 25+: `java22` through `java25`
  - JDK 26+: `java26`
- **java8 module** is the only one with a main class (`App.java`) and uses Hazelcast 5.1.3. It produces a shaded fat JAR.
- **java9–java26 modules** are primarily test-only, showcasing language features in JUnit 5 tests.
- **java21–java26 modules** enable `--enable-preview` for both compilation and test execution.
- Testing uses **JUnit 5 (Jupiter)** with **Hamcrest** matchers.

## CI

- **GitHub Actions PR Builder**: runs `mvn verify` with the latest available Java version (from `actions/setup-java`)

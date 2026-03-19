# Key Java Changes by LTS Release

Final (non-preview, non-incubator, non-experimental) features most relevant to Java developers, grouped by the LTS release that first includes them.

## JDK 8

### Language
* [126](https://openjdk.org/jeps/126) Lambda Expressions & Default Methods
* [104](https://openjdk.org/jeps/104) Annotations on Java Types
* [120](https://openjdk.org/jeps/120) Repeating Annotations
* [118](https://openjdk.org/jeps/118) Access to Parameter Names at Runtime
* [101](https://openjdk.org/jeps/101) Generalized Target-Type Inference

### Libraries & APIs
* [107](https://openjdk.org/jeps/107) Stream API (Bulk Data Operations for Collections)
* [150](https://openjdk.org/jeps/150) Date & Time API (`java.time`)
* [155](https://openjdk.org/jeps/155) Concurrency Updates (`CompletableFuture`, etc.)
* [135](https://openjdk.org/jeps/135) Base64 Encoding & Decoding
* [103](https://openjdk.org/jeps/103) Parallel Array Sorting
* [170](https://openjdk.org/jeps/170) JDBC 4.2
* [109](https://openjdk.org/jeps/109) Enhance Core Libraries with Lambda

### Security
* [114](https://openjdk.org/jeps/114) TLS Server Name Indication (SNI) Extension
* [115](https://openjdk.org/jeps/115) AEAD CipherSuites
* [121](https://openjdk.org/jeps/121) Stronger Algorithms for Password-Based Encryption
* [129](https://openjdk.org/jeps/129) NSA Suite B Cryptographic Algorithms
* [140](https://openjdk.org/jeps/140) Limited `doPrivileged`
* [166](https://openjdk.org/jeps/166) Overhaul JKS-JCEKS-PKCS12 Keystores

### Runtime & JVM
* [122](https://openjdk.org/jeps/122) Remove the Permanent Generation (replaced by Metaspace)
* [174](https://openjdk.org/jeps/174) Nashorn JavaScript Engine

---

## JDK 11 (includes changes from JDK 9, 10, 11)

### Language
* [286](https://openjdk.org/jeps/286) Local-Variable Type Inference (`var`) *(JDK 10)*
* [323](https://openjdk.org/jeps/323) Local-Variable Syntax for Lambda Parameters (`var` in lambdas) *(JDK 11)*
* [213](https://openjdk.org/jeps/213) Milling Project Coin (private methods in interfaces, try-with-resources improvements, diamond with anonymous classes) *(JDK 9)*

### Libraries & APIs
* [321](https://openjdk.org/jeps/321) HTTP Client (`java.net.http`) *(JDK 11)*
* [269](https://openjdk.org/jeps/269) Convenience Factory Methods for Collections (`List.of()`, `Map.of()`, etc.) *(JDK 9)*
* [266](https://openjdk.org/jeps/266) More Concurrency Updates (reactive streams `Flow` API) *(JDK 9)*
* [102](https://openjdk.org/jeps/102) Process API Updates (`ProcessHandle`) *(JDK 9)*
* [259](https://openjdk.org/jeps/259) Stack-Walking API *(JDK 9)*
* [193](https://openjdk.org/jeps/193) Variable Handles (`VarHandle`) *(JDK 9)*

### Modules & Packaging
* [261](https://openjdk.org/jeps/261) Module System (JPMS) *(JDK 9)*
* [238](https://openjdk.org/jeps/238) Multi-Release JAR Files *(JDK 9)*
* [282](https://openjdk.org/jeps/282) jlink: The Java Linker *(JDK 9)*
* [320](https://openjdk.org/jeps/320) Remove the Java EE and CORBA Modules *(JDK 11)*

### Tools & Diagnostics
* [222](https://openjdk.org/jeps/222) jshell: The Java Shell (REPL) *(JDK 9)*
* [330](https://openjdk.org/jeps/330) Launch Single-File Source-Code Programs *(JDK 11)*
* [328](https://openjdk.org/jeps/328) Flight Recorder (open-sourced) *(JDK 11)*

### Security
* [332](https://openjdk.org/jeps/332) Transport Layer Security (TLS) 1.3 *(JDK 11)*
* [329](https://openjdk.org/jeps/329) ChaCha20 and Poly1305 Cryptographic Algorithms *(JDK 11)*
* [324](https://openjdk.org/jeps/324) Key Agreement with Curve25519 and Curve448 *(JDK 11)*
* [229](https://openjdk.org/jeps/229) Create PKCS12 Keystores by Default *(JDK 9)*

### Runtime & JVM
* [248](https://openjdk.org/jeps/248) Make G1 the Default Garbage Collector *(JDK 9)*
* [310](https://openjdk.org/jeps/310) Application Class-Data Sharing *(JDK 10)*
* [254](https://openjdk.org/jeps/254) Compact Strings (internal `String` optimization) *(JDK 9)*
* [318](https://openjdk.org/jeps/318) Epsilon: A No-Op Garbage Collector *(JDK 11)*
* [181](https://openjdk.org/jeps/181) Nest-Based Access Control *(JDK 11)*

---

## JDK 17 (includes changes from JDK 12–17)

### Language
* [361](https://openjdk.org/jeps/361) Switch Expressions *(JDK 14)*
* [378](https://openjdk.org/jeps/378) Text Blocks *(JDK 15)*
* [395](https://openjdk.org/jeps/395) Records *(JDK 16)*
* [394](https://openjdk.org/jeps/394) Pattern Matching for `instanceof` *(JDK 16)*
* [409](https://openjdk.org/jeps/409) Sealed Classes *(JDK 17)*

### Libraries & APIs
* [358](https://openjdk.org/jeps/358) Helpful NullPointerExceptions *(JDK 14)*
* [371](https://openjdk.org/jeps/371) Hidden Classes *(JDK 15)*
* [380](https://openjdk.org/jeps/380) Unix-Domain Socket Channels *(JDK 16)*
* [356](https://openjdk.org/jeps/356) Enhanced Pseudo-Random Number Generators (`RandomGenerator`) *(JDK 17)*
* [415](https://openjdk.org/jeps/415) Context-Specific Deserialization Filters *(JDK 17)*
* [390](https://openjdk.org/jeps/390) Warnings for Value-Based Classes *(JDK 16)*
* [334](https://openjdk.org/jeps/334) JVM Constants API *(JDK 12)*

### Tools & Packaging
* [392](https://openjdk.org/jeps/392) Packaging Tool (`jpackage`) *(JDK 16)*
* [349](https://openjdk.org/jeps/349) JFR Event Streaming *(JDK 14)*

### Security
* [339](https://openjdk.org/jeps/339) Edwards-Curve Digital Signature Algorithm (EdDSA) *(JDK 15)*
* [306](https://openjdk.org/jeps/306) Restore Always-Strict Floating-Point Semantics *(JDK 17)*

### Runtime & JVM
* [377](https://openjdk.org/jeps/377) ZGC: Production-Ready *(JDK 15)*
* [379](https://openjdk.org/jeps/379) Shenandoah: Production-Ready *(JDK 15)*
* [387](https://openjdk.org/jeps/387) Elastic Metaspace *(JDK 16)*
* [403](https://openjdk.org/jeps/403) Strongly Encapsulate JDK Internals *(JDK 17)*
* [341](https://openjdk.org/jeps/341) Default CDS Archives *(JDK 12)*
* [350](https://openjdk.org/jeps/350) Dynamic CDS Archives *(JDK 13)*

### Removals
* [363](https://openjdk.org/jeps/363) Remove the Concurrent Mark Sweep (CMS) Garbage Collector *(JDK 14)*
* [372](https://openjdk.org/jeps/372) Remove the Nashorn JavaScript Engine *(JDK 15)*
* [407](https://openjdk.org/jeps/407) Remove RMI Activation *(JDK 17)*
* [367](https://openjdk.org/jeps/367) Remove the Pack200 Tools and API *(JDK 14)*

---

## JDK 21 (includes changes from JDK 18–21)

### Language
* [441](https://openjdk.org/jeps/441) Pattern Matching for `switch` *(JDK 21)*
* [440](https://openjdk.org/jeps/440) Record Patterns *(JDK 21)*

### Libraries & APIs
* [444](https://openjdk.org/jeps/444) Virtual Threads *(JDK 21)*
* [431](https://openjdk.org/jeps/431) Sequenced Collections (`SequencedCollection`, `SequencedMap`) *(JDK 21)*
* [452](https://openjdk.org/jeps/452) Key Encapsulation Mechanism API *(JDK 21)*
* [418](https://openjdk.org/jeps/418) Internet-Address Resolution SPI *(JDK 18)*
* [408](https://openjdk.org/jeps/408) Simple Web Server (`jwebserver`) *(JDK 18)*

### Tools & Diagnostics
* [413](https://openjdk.org/jeps/413) Code Snippets in Java API Documentation *(JDK 18)*

### Runtime & JVM
* [439](https://openjdk.org/jeps/439) Generational ZGC *(JDK 21)*
* [400](https://openjdk.org/jeps/400) UTF-8 by Default *(JDK 18)*
* [416](https://openjdk.org/jeps/416) Reimplement Core Reflection with Method Handles *(JDK 18)*
* [451](https://openjdk.org/jeps/451) Prepare to Disallow the Dynamic Loading of Agents *(JDK 21)*

---

## JDK 25 (includes changes from JDK 22–25)

### Language
* [456](https://openjdk.org/jeps/456) Unnamed Variables & Patterns (`_`) *(JDK 22)*
* [513](https://openjdk.org/jeps/513) Flexible Constructor Bodies (statements before `super()`) *(JDK 25)*
* [511](https://openjdk.org/jeps/511) Module Import Declarations (`import module java.base`) *(JDK 25)*
* [512](https://openjdk.org/jeps/512) Compact Source Files and Instance Main Methods *(JDK 25)*
* [467](https://openjdk.org/jeps/467) Markdown Documentation Comments *(JDK 23)*

### Libraries & APIs
* [454](https://openjdk.org/jeps/454) Foreign Function & Memory API *(JDK 22)*
* [485](https://openjdk.org/jeps/485) Stream Gatherers *(JDK 24)*
* [484](https://openjdk.org/jeps/484) Class-File API *(JDK 24)*
* [506](https://openjdk.org/jeps/506) Scoped Values *(JDK 25)*
* [510](https://openjdk.org/jeps/510) Key Derivation Function API (HKDF) *(JDK 25)*
* [491](https://openjdk.org/jeps/491) Synchronize Virtual Threads without Pinning *(JDK 24)*

### Tools
* [458](https://openjdk.org/jeps/458) Launch Multi-File Source-Code Programs *(JDK 22)*
* [493](https://openjdk.org/jeps/493) Linking Run-Time Images without JMODs *(JDK 24)*
* [514](https://openjdk.org/jeps/514) Ahead-of-Time Command-Line Ergonomics *(JDK 25)*

### Security
* [496](https://openjdk.org/jeps/496) Quantum-Resistant Module-Lattice-Based Key Encapsulation Mechanism (ML-KEM) *(JDK 24)*
* [497](https://openjdk.org/jeps/497) Quantum-Resistant Module-Lattice-Based Digital Signature Algorithm (ML-DSA) *(JDK 24)*

### Runtime & JVM
* [519](https://openjdk.org/jeps/519) Compact Object Headers *(JDK 25)*
* [521](https://openjdk.org/jeps/521) Generational Shenandoah *(JDK 25)*
* [474](https://openjdk.org/jeps/474) ZGC: Generational Mode by Default *(JDK 23)*
* [490](https://openjdk.org/jeps/490) ZGC: Remove the Non-Generational Mode *(JDK 24)*
* [423](https://openjdk.org/jeps/423) Region Pinning for G1 *(JDK 22)*
* [483](https://openjdk.org/jeps/483) Ahead-of-Time Class Loading & Linking *(JDK 24)*
* [515](https://openjdk.org/jeps/515) Ahead-of-Time Method Profiling *(JDK 25)*
* [472](https://openjdk.org/jeps/472) Prepare to Restrict the Use of JNI *(JDK 24)*
* [498](https://openjdk.org/jeps/498) Warn upon Use of Memory-Access Methods in `sun.misc.Unsafe` *(JDK 24)*

### Diagnostics
* [518](https://openjdk.org/jeps/518) JFR Cooperative Sampling *(JDK 25)*
* [520](https://openjdk.org/jeps/520) JFR Method Timing & Tracing *(JDK 25)*

### Removals
* [486](https://openjdk.org/jeps/486) Permanently Disable the Security Manager *(JDK 24)*
* [479](https://openjdk.org/jeps/479) Remove the Windows 32-bit x86 Port *(JDK 24)*
* [503](https://openjdk.org/jeps/503) Remove the 32-bit x86 Port *(JDK 25)*
* [471](https://openjdk.org/jeps/471) Deprecate the Memory-Access Methods in `sun.misc.Unsafe` for Removal *(JDK 23)*

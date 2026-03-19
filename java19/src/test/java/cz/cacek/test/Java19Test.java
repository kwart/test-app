package cz.cacek.test;

import org.junit.jupiter.api.Test;

/**
 * Check the <a href="https://openjdk.org/projects/jdk/19/">JDK 19 page</a>.
 *
 * JDK 19 features were mostly preview/incubator:
 * - Record Patterns (Preview, JEP 405) - finalized in JDK 21
 * - Virtual Threads (Preview, JEP 425) - finalized in JDK 21
 * - Pattern Matching for switch (Third Preview, JEP 427) - finalized in JDK 21
 * - Foreign Function & Memory API (Preview, JEP 424) - finalized in JDK 22
 * - Structured Concurrency (Incubator, JEP 428) - still in preview
 *
 * Final feature tests for these are in their respective finalized modules.
 */
public class Java19Test {

    // https://openjdk.org/projects/jdk/19/

    @Test
    // https://openjdk.org/jeps/422
    public void linuxRiscVPort() {
        // JEP 422: Linux/RISC-V Port - platform support, not directly testable via API
        String osArch = System.getProperty("os.arch");
        System.out.println("Running on architecture: " + osArch);
    }
}

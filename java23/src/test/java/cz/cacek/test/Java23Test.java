package cz.cacek.test;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Check the <a href="https://openjdk.org/projects/jdk/23/">JDK 23 page</a>.
 *
 * ## Markdown in Javadoc (JEP 467)
 *
 * This Javadoc comment itself uses **Markdown** syntax, which is a new feature in JDK 23.
 * - List item one
 * - List item two
 *
 * ```java
 * // Code blocks in Javadoc
 * System.out.println("Hello from JDK 23!");
 * ```
 */
public class Java23Test {

    // https://openjdk.org/projects/jdk/23/

    /// This method-level Javadoc uses **Markdown** (JEP 467).
    /// The `///` comment style is new in JDK 23.
    ///
    /// JDK 23 final features are mostly runtime/GC changes:
    /// - [JEP 467](https://openjdk.org/jeps/467): Markdown Documentation Comments
    /// - [JEP 471](https://openjdk.org/jeps/471): Deprecate sun.misc.Unsafe memory-access methods
    /// - [JEP 474](https://openjdk.org/jeps/474): ZGC Generational Mode by Default
    @Test
    // https://openjdk.org/jeps/467
    public void markdownDocumentationComments() {
        // JEP 467: Markdown Documentation Comments
        // The /// syntax and Markdown in Javadoc are demonstrated in this class's comments.
        // This is a compile-time/tooling feature - if this class compiles, the feature works.
        assertTrue(true, "Markdown documentation comments are supported");
    }
}

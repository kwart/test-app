package cz.cacek.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Check the <a href="https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-10">baeldung repo</a>.
 */
public class Java10Test {

    @Test
    public void testLocalVariableTypeInference() throws Exception {
        var message = "Hello";
        var counter = 1 + 1;
        assertEquals(2, counter);
        assertInstanceOf(String.class, message);
    }

}

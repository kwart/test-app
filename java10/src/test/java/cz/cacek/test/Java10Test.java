package cz.cacek.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

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

    @Test
    public void tryCopyOfCollection() throws Exception {
        List<Integer> sourceList = Arrays.asList(0, 1, 2, 3, 4, 5);
        Set<Integer> targetSet = Set.copyOf(sourceList);
        assertEquals(6, targetSet.size());
        assertThrows(UnsupportedOperationException.class, () -> targetSet.add(7));
    }

}

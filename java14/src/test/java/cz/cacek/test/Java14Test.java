package cz.cacek.test;

import static cz.cacek.test.Java14Test.Day.FRIDAY;
import static cz.cacek.test.Java14Test.Day.MONDAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.ObjectStreamField;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Check the <a href="https://www.azul.com/blog/whats-new-in-jdk14-latest-release/">azul blog</a>.
 */
public class Java14Test {
    @Test
    public void helpfulNpe() throws Exception {
        List<String> list = null;
        NullPointerException npe = assertThrows(NullPointerException.class, () -> list.add("foo"));
        assertTrue(npe.getMessage().contains("List.add("));
    }

    @Test
    // https://openjdk.org/jeps/361
    public void switchExpressions() throws Exception {
        Day day = MONDAY;
        int numLetters = switch (day) {
            case MONDAY, FRIDAY, SUNDAY -> 6;
            case TUESDAY -> 7;
            case THURSDAY, SATURDAY -> 8;
            case WEDNESDAY -> 9;
        };
        assertEquals(6, numLetters);

        day = FRIDAY;
        int j = switch (day) {
            case MONDAY -> 0;
            case TUESDAY -> 1;
            default -> {
                int k = day.toString().length();
                int result = k + 1;
                yield result;
            }
        };
        assertEquals(7, j);

        int result = switch ("Ahoj") {
            case "Foo":
                yield 1;
            case "Bar":
                yield 2;
            default:
                System.out.println("Neither Foo nor Bar, hmmm...");
                yield 3;
        };
        assertEquals(3, result);
    }

    @Test
    public void characterMethods() {
        assertTrue(Character.isLetter('A'));
        assertTrue(Character.isAlphabetic('A'));
        char ch = 837;
        assertFalse(Character.isLetter(ch));
        assertTrue(Character.isAlphabetic(ch));
    }

    @Test
    public void useClassWithTheSerialAnnotation() {
        TestSerialClass tsc = new TestSerialClass();
    }

    enum Day {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;
    }

    public static class TestSerialClass implements Serializable {

        @Serial
        private static final ObjectStreamField[] serialPersistentFields = null;

        @Serial
        private static final long serialVersionUID = 1;

        @Serial
        private void writeObject(ObjectOutputStream stream) throws IOException {
        }

        @Serial
        private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        }

        @Serial
        private void readObjectNoData() throws ObjectStreamException {
        }

        @Serial
        private Object writeReplace() throws ObjectStreamException {
            return null;
        }

        @Serial
        private Object readResolve() throws ObjectStreamException {
            return null;
        }

    }
}

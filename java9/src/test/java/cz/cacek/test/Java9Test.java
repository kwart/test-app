package cz.cacek.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class Java9Test {

    private int variable = 1;

    @Test
    public void useStackWalker() throws Exception {
        StackWalker stackWalker = StackWalker.getInstance();
        String currentMethod = stackWalker.walk(s -> s.findFirst().map(StackWalker.StackFrame::getMethodName))
                .orElseThrow(AssertionError::new);

        assertEquals("useStackWalker", currentMethod);
    }

    @Test
    public void usePrivateInterfaceMethods() {
        assertEquals(3, new PrivateInterfaceMethods(){}.compute());
    }

    @Test
    public void improvedTryWithResource() throws IOException {
        // Java 9 doesn't need the declaration within the try block e.g. try (BufferedReader br = new ...)
        BufferedReader br = new BufferedReader(new StringReader("love"));
        try (br) {
           br.readLine();
        }
    }


    @Test
    public void useVarHandleInsteadReflection() throws NoSuchFieldException, IllegalAccessException {
        VarHandle varHandle = MethodHandles
          .privateLookupIn(getClass(), MethodHandles.lookup())
          .findVarHandle(getClass(), "variable", int.class);

        varHandle.set(this, 112);
        assertEquals(112, variable);
        assertEquals(112, (int) varHandle.get(this));
    }


    @Test
    public void makeStreamFromOptional() {
        Optional<String> opt = Optional.ofNullable("x");
        Stream<String> stream = opt.stream();
        long filteredCount = stream.filter(String::isEmpty).count();
        assertEquals(0, filteredCount);

    }
    interface PrivateInterfaceMethods {

        private static int privateStatic() {
            return 1;
        }

        private int privateInstance() {
            return 2;
        }

        default int compute() {
            return privateStatic() + privateInstance();
        }
    }
}

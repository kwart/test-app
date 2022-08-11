package cz.cacek.test;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * A test template.
 */
public class AppTest {

    @Test
    @Disabled
    public void testNullToMain() throws Exception {
        App.main(null);
    }

}

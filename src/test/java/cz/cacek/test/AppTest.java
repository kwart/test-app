package cz.cacek.test;

import org.junit.Test;

public class AppTest {

    @Test
    public void test() throws Exception {
        for (int i = 0; i < 100; i++) {
            App.main(null);
        }
    }

}

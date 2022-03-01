package cz.cacek.test;

import java.nio.charset.Charset;
import java.util.Map.Entry;

/**
 * The App!
 */
public class App {

    public static void main(String[] args) {
        for (Entry e:System.getProperties().entrySet()) {
            System.out.println(e.getKey() + ": " + e.getValue());
        }
        System.out.println();
        System.out.println("Default charset: " + Charset.defaultCharset());
        System.out.println("See 🔒 security");
    }
}

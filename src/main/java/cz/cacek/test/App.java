package cz.cacek.test;

/**
 * Hazelcast Hello world!
 */
public class App {

    public static void main(String[] args) {
        TestAuditLogger.LOGGER.userLoggedIn("hezoun", "123.456.789.11");
        
    }
}

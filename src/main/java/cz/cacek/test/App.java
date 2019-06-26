package cz.cacek.test;

/**
 * Hazelcast Hello world!
 */
public class App {

    public static void main(String[] args) {
        ManCenterAuditLogger.LOGGER.userLoggedIn("admin", "192.168.1.5");
        // ...
        ManCenterAuditLogger.LOGGER.userLoggedOut("admin");
    }
}

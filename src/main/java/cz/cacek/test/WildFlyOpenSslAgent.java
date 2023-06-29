package cz.cacek.test;

public class WildFlyOpenSslAgent {

    public static void premain(final String agentArgs) throws Exception {
        try {
            org.wildfly.openssl.OpenSSLProvider.register();
        } catch (Exception e) {
            System.err.println("Failed to register WildFly OpenSSL provider");
            e.printStackTrace();
        }
    }
}

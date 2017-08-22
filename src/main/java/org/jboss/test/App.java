package org.jboss.test;

import java.io.File;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;

import org.ietf.jgss.ChannelBinding;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

/**
 * Reproducer for https://bugzilla.redhat.com/show_bug.cgi?id=1481103.
 * 
 * @author Josef Cacek
 */
public class App {

    private final static String KRB5CC_PROPERTY = "krb5cc.path";
    private final static String KRB5CC_PATH = System.getProperty(KRB5CC_PROPERTY);
    
    public static void main(String[] args) throws Exception {
        if (!System.getProperty("java.vendor").startsWith("IBM")) {
            System.err.println("Run the application on IBM JDK");
            System.exit(1);
        }
        final File credentialCacheFile = (KRB5CC_PATH != null) ? new File(KRB5CC_PATH) : null;

        if (credentialCacheFile == null || !credentialCacheFile.isFile()) {
            System.err.println("Provide a path to your credential cache file as a '"+KRB5CC_PROPERTY+"' system property value");
            System.err.println();
            System.err.println("Example:");
            System.err.println("\tjava -D"+KRB5CC_PROPERTY+"=/tmp/krb5cc_1000 org.jboss.test.App");
            System.exit(2);
        }
        
        Configuration.setConfiguration(new Configuration() {
            @Override
            public AppConfigurationEntry[] getAppConfigurationEntry(String applicationName) {
                final Map<String, String> options = new HashMap<String, String>();
                options.put("useCcache", credentialCacheFile.toURI().toString());
                options.put("debug", "true");
                return new AppConfigurationEntry[] { new AppConfigurationEntry("com.ibm.security.auth.module.Krb5LoginModule",
                        AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options) };
            }
        });

        LoginContext lc = new LoginContext("test");
        lc.login();

        Subject.doAs(lc.getSubject(), (PrivilegedExceptionAction<Void>) () -> {
            GSSManager gssManager = GSSManager.getInstance();

            String acceptorNameStr = "remote@localhost";
            Oid kerberosOid = new Oid("1.2.840.113554.1.2.2");
            GSSName acceptorName = gssManager.createName(acceptorNameStr, GSSName.NT_HOSTBASED_SERVICE, kerberosOid);

            GSSContext gssContext = gssManager.createContext(acceptorName, kerberosOid, null, GSSContext.INDEFINITE_LIFETIME);
            ChannelBinding cb = new ChannelBinding(new byte[8]);
            gssContext.setChannelBinding(cb);
            return null;
        });
    }

}

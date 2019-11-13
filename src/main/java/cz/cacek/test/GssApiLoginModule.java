package cz.cacek.test;

import java.io.IOException;
import java.util.Base64;
 
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
 
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
 
import com.hazelcast.security.ClusterLoginModule;
import com.hazelcast.security.Credentials;
import com.hazelcast.security.CredentialsCallback;
import com.hazelcast.security.TokenCredentials;
 
/**
 * Hazelcast GSS-API LoginModule implementation.
 */
public class GssApiLoginModule extends ClusterLoginModule {
 
    public static final String OPTION_RELAX_FLAGS_CHECK = "relaxFlagsCheck";
 
    private String name;
 
    @Override
    public boolean onLogin() throws LoginException {
        CredentialsCallback cc = new CredentialsCallback();
        try {
            callbackHandler.handle(new Callback[] { cc });
        } catch (IOException | UnsupportedCallbackException e) {
            throw new FailedLoginException("Unable to retrieve Certificates. " + e.getMessage());
        }
        Credentials creds = cc.getCredentials();
        if (creds == null || !(creds instanceof TokenCredentials)) {
            throw new FailedLoginException("No valid TokenCredentials found");
        }
        TokenCredentials tokenCreds = (TokenCredentials) creds;
        byte[] token = tokenCreds.getToken();
        if (token == null) {
            throw new FailedLoginException("No token found in TokenCredentials.");
        }
        if (logger.isFineEnabled()) {
            logger.fine("Received Token: " + Base64.getEncoder().encodeToString(token));
        }
        GSSManager gssManager = GSSManager.getInstance();
        try {
            GSSContext gssContext = gssManager.createContext((GSSCredential) null);
            token = gssContext.acceptSecContext(token, 0, token.length);
 
            boolean relaxChecks = getBoolOption(OPTION_RELAX_FLAGS_CHECK, false);
            if (!gssContext.isEstablished()) {
                throw new FailedLoginException("Multi-step negotiation is not supported by this login module");
            }
            if (!relaxChecks) {
                if (token != null && token.length > 0) {
                    // sending a token back to client is not supported
                    throw new FailedLoginException("Mutual authentication is not supported by this login module");
                }
                if (gssContext.getConfState() || gssContext.getIntegState()) {
                    throw new FailedLoginException("Confidentiality and data integrity is not provided by this login module.");
                }
            }
            name = gssContext.getSrcName().toString();
            if (!getBoolOption(OPTION_SKIP_ROLE, false)) {
                addRole(name);
            }
        } catch (GSSException e) {
            logger.fine("Accepting the GSS-API token failed.", e);
            throw new LoginException("Accepting the GSS-API token failed. " + e.getMessage());
        }
        return true;
    }
 
    @Override
    protected String getName() {
        return name;
    }
 
}
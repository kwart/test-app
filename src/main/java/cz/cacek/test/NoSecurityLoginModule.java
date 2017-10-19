package cz.cacek.test;

import java.security.Principal;
import java.util.Iterator;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

public class NoSecurityLoginModule implements LoginModule {

    private Subject subject;
    private CallbackHandler callbackHandler;
    private String name;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
            Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public boolean login() throws LoginException {
        NameCallback nameCb = new NameCallback("name");
        try {
            callbackHandler.handle(new Callback[] { nameCb });
        } catch (Exception e) {
            throw new LoginException("Unable to retrieve username");
        }
        name = nameCb.getName();
        return true;
    }

    @Override
    public boolean commit() throws LoginException {
        subject.getPrincipals().add(new Principal() {
            @Override
            public String getName() {
                return name;
            }
        });
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        name = null;
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        for (Iterator<Principal> it = subject.getPrincipals().iterator(); it.hasNext();) {
            Principal principal = it.next();
            if (principal instanceof SimplePrincipal) {
                it.remove();
            }
        }
        return true;
    }

    public static class SimplePrincipal implements Principal {

        private final String name;

        public SimplePrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

    }
}

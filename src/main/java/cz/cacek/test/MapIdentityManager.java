package cz.cacek.test;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.idm.PasswordCredential;

/**
 * A simple {@link IdentityManager} implementation, that just takes a map of users to their password.
 *
 * This is in now way suitable for real world production use.
 *
 *
 * @author Stuart Douglas
 */
class MapIdentityManager implements IdentityManager {

    private final Map<String, char[]> users;

    MapIdentityManager(final Map<String, char[]> users) {
        this.users = users;
    }

    @Override
    public Account verify(Account account) {
        // An existing account so for testing assume still valid.
        return account;
    }

    @Override
    public Account verify(String id, Credential credential) {
        Account account = getAccount(id);
        if (account != null && verifyCredential(account, credential)) {
            return account;
        }

        return null;
    }

    @Override
    public Account verify(Credential credential) {
        // TODO Auto-generated method stub
        return null;
    }

    private boolean verifyCredential(Account account, Credential credential) {
        if (credential instanceof PasswordCredential) {
            char[] password = ((PasswordCredential) credential).getPassword();
            char[] expectedPassword = users.get(account.getPrincipal().getName());

            return Arrays.equals(password, expectedPassword);
        }
        return false;
    }

    private Account getAccount(final String id) {
        if (users.containsKey(id)) {
            return new Account() {

                private final Principal principal = new Principal() {

                    @Override
                    public String getName() {
                        return id;
                    }
                };

                @Override
                public Principal getPrincipal() {
                    return principal;
                }

                @Override
                public Set<String> getRoles() {
                    return Collections.emptySet();
                }

            };
        }
        return null;
    }

}
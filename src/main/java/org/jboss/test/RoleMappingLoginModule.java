package org.jboss.test;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Iterator;

import javax.security.auth.login.LoginException;

import org.jboss.security.SimpleGroup;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.auth.spi.AbstractServerLoginModule;

/**
 * A sample custom role mapping LoginModule for JBoss AS.
 * 
 * @author Josef Cacek
 */
public class RoleMappingLoginModule extends AbstractServerLoginModule {

    /**
     * We don't want to authenticate, only assign roles => set loginOk to <code>true</code>.
     * 
     * @return true
     * @throws LoginException
     * @see org.jboss.security.auth.spi.AbstractServerLoginModule#login()
     */
    @Override
    public boolean login() throws LoginException {
        return super.loginOk = true;
    }

    /**
     * Returns the first non-Group Principal from Subject.
     * 
     * @return
     * @see org.jboss.security.auth.spi.AbstractServerLoginModule#getIdentity()
     */
    @Override
    protected Principal getIdentity() {
        //We have an authenticated subject
        Iterator<? extends Principal> iter = subject.getPrincipals().iterator();
        while (iter.hasNext()) {
            Principal p = iter.next();
            if (p instanceof Group == false)
                return p;
        }
        return null;
    }

    /**
     * Returns the "Roles" Group filled with some roles.
     * 
     * @return "Roles" group
     * @throws LoginException
     * @see org.jboss.security.auth.spi.AbstractServerLoginModule#getRoleSets()
     */
    @Override
    protected Group[] getRoleSets() throws LoginException {
        Group rolesGroup = new SimpleGroup("Roles");
        rolesGroup.addMember(new SimplePrincipal("manager"));
        rolesGroup.addMember(new SimplePrincipal("sales"));
        rolesGroup.addMember(new SimplePrincipal("employee"));
        return new Group[] { rolesGroup };
    }
}

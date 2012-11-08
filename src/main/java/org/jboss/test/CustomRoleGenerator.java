package org.jboss.test;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.picketlink.identity.federation.core.interfaces.RoleGenerator;

/**
 * A sample custom RoleGenerator for PicketLink IDP.
 * 
 * @author Josef Cacek
 */
public class CustomRoleGenerator implements RoleGenerator {

    public List<String> generateRoles(Principal principal) {
        List<String> userRoles = new ArrayList<String>();
        userRoles.add("manager");
        userRoles.add("sales");
        userRoles.add("employee");
        return userRoles;
    }
}
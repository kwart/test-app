package org.jboss.test;

import org.jboss.security.identity.plugins.SimpleRole;
import org.jboss.security.identity.plugins.SimpleRoleGroup;
import org.openjdk.jmh.annotations.Benchmark;

/**
 * @author Josef Cacek
 */
public class App {

    private static final SimpleRoleGroup GROUP = createSimpleRoleGroup();

    @Benchmark
    public static void fillRoles() {
        createSimpleRoleGroup();
    }

    @Benchmark
    public static void retrieveRoles() {
        if (GROUP.getRoles().size()<200) {
            throw new RuntimeException();
        }
    }

    private static SimpleRoleGroup createSimpleRoleGroup() {
        SimpleRoleGroup group = new SimpleRoleGroup("Roles");
        for (int i = 0; i < 100; i++) {
            group.addRole(new SimpleRole("test-" + i));
            group.addRole(new SimpleRole("role-" + (100 - i)));
        }
        return group;
    }

}

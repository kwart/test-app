# Custom JACC PolicyConfiguration for testing

# How To

1. Add project JAR to `org.picketbox` WildFly/EAP module and change the `module.xml` accordingly:

    <resource-root path="jacc-custom-policyconfiguration.jar"/>
    
1. Enable the new `PolicyConfiguration` by adding line to `standalone.conf`:

    JAVA_OPTS="$JAVA_OPTS -Djavax.security.jacc.PolicyConfigurationFactory.provider=org.jboss.security.jacc.TestPolicyConfigurationFactory"

1. Deploy a test application and check if the `Admin` role assignment is skipped.

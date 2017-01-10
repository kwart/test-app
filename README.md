# Demo - Use JMS client anonymous access with Elytron

This demo shows how to use use the `ANONYMOUS` SASL authentication mechanism from Elytron as replacement for `unauthenticatedIdentity` login module option in WidlFly legacy security.

We want to send a message from JMS client without authentication, so we want to allow anonymous access and grant `"guest"` role (used in messaging-activemq configuration) to incoming clients.

[The client code](src/main/java/org/jboss/test/App.java) in this demo is based on [helloworld-jms](https://github.com/wildfly/quickstart/tree/11.x/helloworld-jms) WildFly quickstart.

## Configure the Application server

The configuration JBoss CLI script [demo.cli](demo.cli) contains commands to configure the server.

```bash
export JBOSS_HOME=/path/to/your/wildflyOrEap
$JBOSS_HOME/bin/jboss-cli.sh --file=demo.cli
```

## Client configuration

Custom [wildfly-config.xml](src/main/resources/wildfly-config.xml) is used to allow all SASL mechanisms.

## Run the demo

### Start the server (full profile)
```bash
$JBOSS_HOME/bin/standalone.sh -c standalone-full.xml
```

### Run the JMS client
```
mvn clean package exec:java
```

If the client execution fails, check if the issue [JBEAP-8047](https://issues.jboss.org/browse/JBEAP-8047) is fixed already in your version. 
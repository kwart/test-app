# Reproducer for JBoss CLI memory leak

The `org.jboss.as.cli.impl.CommandContextImpl` class calls the `initJaasConfig()` in its constructor, 
which adds a new `JaasConfigurationWrapper` instance to `javax.security.auth.login.Configuration`.
It's done for every new `CommandContext` instance, so it consumes more and more memory. There is no cleanup for the registered 
configurations.

Moreover, the searches for appropriate 
login config take more and more time because they have to go through all the wrapped objects.

## How does the reproducer works

It simply creates new `CommandContextImpl` instances:

	counter=0;
    while (true) {
      CommandContextFactory.getInstance().newCommandContext();
      counter++;
    }

## How to run it

Use the Maven, Luke!

    mvn clean package exec:java
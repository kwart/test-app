# attribution-maven-plugin

The `attribution-maven-plugin` is a Maven Plugin that creates author's attribution for project dependencies.

Example:

```xml
<plugins>
    <plugin>
        <groupId>com.hazelcast.maven</groupId>
        <artifactId>attribution-maven-plugin</artifactId>
        <version>1.0-SNAPSHOT</version>
        <executions>
            <execution>
                <id>attribution</id>
                <goals>
                    <goal>attribution</goal>
                </goals>
                <phase>generate</phase>
            </execution>
        </executions>
    </plugin>
</plugins>
```
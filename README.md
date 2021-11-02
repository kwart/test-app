# attribution-maven-plugin

The `attribution-maven-plugin` is a Maven Plugin that greps copyright lines from source files.
It's intended to be used together with the `maven-dependency-plugin`.

Example:

```xml
<plugins>
    <plugin>
        <artifactId>maven-dependency-plugin</artifactId>
        <executions>
            <execution>
                <id>deps-sources</id>
                <goals>
                    <goal>copy-dependencies</goal>
                </goals>
                <phase>generate-resources</phase>
                <configuration>
                    <classifier>sources</classifier>
                    <includeScope>runtime</includeScope>
                </configuration>
            </execution>
        </executions>
    </plugin>
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
                <phase>generate-resources</phase>
            </execution>
        </executions>
    </plugin>
</plugins>
```
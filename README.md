# jsons2xsd-maven-plugin

Converts JSON schemas to XML schemas using [jsons2xsd](https://github.com/ethlo/jsons2xsd).

```xml
<plugin>
    <groupId>org.swisspush.maven.plugins</groupId>
    <artifactId>jsons2xsd-maven-plugin</artifactId>
    <version>1.0</version>
    <configuration>
        <namespace>http://my/namespace/v1</namespace> <!-- required -->
        <inputDirectory>src/main/xsd</inputDirectory>
        <outputDirectory>src/main/xsd</outputDirectory>
        <namespaceAlias>ns</namespaceAlias>
        <useGenericItemNames>true</useGenericItemNames> <!-- Use "items" for array element names instead of type name -->
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>convert</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```



[![Java CI with Maven](https://github.com/Maritims/velocity-sass-maven-plugin/actions/workflows/maven.yml/badge.svg)](https://github.com/Maritims/velocity-sass-maven-plugin/actions/workflows/maven.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.maritims/velocity-sass-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.maritims/velocity-sass-maven-plugin)

# velocity-sass-maven-plugin
Maven plugin for running compiling SCSS written in Velocity files into CSS.

## Configuration
Add the plugin groupId to the `<pluginGroups />` section of settings.xml. This tells Maven where to look when resolving `mvn` plugin commands and lets you type `mvn velocity-sass:update-stylesheets` rather than the long form.
See [Maven - Guide to Developing Java Plugins](https://maven.apache.org/guides/plugin/guide-java-plugin-development.html) for more details.
```xml
<pluginGroups>
    <pluginGroup>io.github.maritims</pluginGroup>
</pluginGroups>
```

Add the plugin configuration to the pom.xml in your Maven project:
```xml
<build>
    <plugins>
        <plugin>
            <groupId>io.github.maritims</groupId>
            <artifactId>velocity-sass-maven-plugin</artifactId>
            <version>1.1.0</version>
            <configuration/>
            <executions>
                <execution>
                    <goals>
                        <goal>update-stylesheets</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## Options
| Option     | Type    | Default value                                                      | Explanation                                       |
|------------|---------|--------------------------------------------------------------------|---------------------------------------------------|
| targetPath | string  | target/${project.artifactId}/WEB-INF/templates/includes/stylesheet | Path to directory with Velocity files to compile. |

## Run manually
`mvn velocity-sass:update-stylesheets`
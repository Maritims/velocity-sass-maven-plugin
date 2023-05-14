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

## Goals

### update-stylesheets
Compile all stylesheets in targetPath during the install phase. Can be run manually with the command `mvn velocity-sass:update-stylesheets`.

#### Options
| Option     | Type    | Default value                                                      | Explanation                                       |
|------------|---------|--------------------------------------------------------------------|---------------------------------------------------|
| targetPath | string  | target/${project.artifactId}/WEB-INF/templates/includes/stylesheet | Path to directory with Velocity files to compile. |

### watch
Watch targetPath for additions, modifications or deletions. Usually used during development. Can be run manually with the command `mvn velocity-sass:watch`.

#### Options
| Option                      | Type    | Default value                                                      | Explanation                                                                                                                                                                                                   |
|-----------------------------|---------|--------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| targetPath                  | string  | target/${project.artifactId}/WEB-INF/templates/includes/stylesheet | Path to directory with Velocity files to compile.                                                                                                                                                             |
| onlyRecompileOnActualChange | boolean | false                                                              | When true a hashmap is constructed with CRC32 hashes of all files in targetPath. A rehash is done when a file change is detected and a comparison is performed to determine if there's been an actual change. |
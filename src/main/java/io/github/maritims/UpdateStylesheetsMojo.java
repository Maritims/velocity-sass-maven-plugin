package io.github.maritims;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;
import java.nio.file.Paths;

import static io.github.maritims.ArrayUtil.emptyIfNull;

@Mojo(name = "update-stylesheets", defaultPhase = LifecyclePhase.PACKAGE)
public class UpdateStylesheetsMojo extends AbstractStylesheetsMojo {
    private final Log log = getLog();

    public void execute() {
        File dir = Paths.get(targetPath).toFile();
        File[] files = emptyIfNull(dir.listFiles(), File.class);

        for(File file : files) {
            if(!file.getName().endsWith(".vm")) {
                log.warn("Unexpected file in source directory: " + file.getName() + ". Skipping file");
                continue;
            }

            compileScss(file);
        }
    }
}
package io.github.maritims;

import de.larsgrefer.sass.embedded.SassCompilationFailedException;
import de.larsgrefer.sass.embedded.SassCompiler;
import de.larsgrefer.sass.embedded.SassCompilerFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public abstract class AbstractStylesheetsMojo extends AbstractMojo {
    private final Log log = getLog();

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject project;

    @Parameter(defaultValue = "target/${project.artifactId}/WEB-INF/templates/includes/stylesheet")
    protected String targetPath;

    protected boolean compileScss(File file) {
        Document document;

        try {
            document = Jsoup.parse(file, StandardCharsets.UTF_8.toString(), "", Parser.xmlParser());
        } catch(FileNotFoundException e) {
            log.error("Unable to parse file " + file.getName() + ". File does not exist.");
            return false;
        } catch (IOException e) {
            log.error("Unable to parse file " + file.getName(), e);
            return false;
        }

        Element styleElement = document.getElementsByTag("style").first();
        if (styleElement == null) {
            log.error("No style element present in file " + file.getName());
            return false;
        }

        try (SassCompiler compiler = SassCompilerFactory.bundled()) {
            String css = compiler.compileScssString(styleElement.text()).getCss();
            styleElement.text(css);
        } catch (IOException | SassCompilationFailedException e) {
            log.error("Unable to compile SCSS", e);
            return false;
        }

        try {
            Files.write(file.toPath(), document.outerHtml().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("Unable to write document to file " + file.getName(), e);
            return false;
        }

        log.info("Compiled: " + file.getName());
        return true;
    }
}

package io.github.maritims;

import de.larsgrefer.sass.embedded.SassCompilationFailedException;
import de.larsgrefer.sass.embedded.SassCompiler;
import de.larsgrefer.sass.embedded.SassCompilerFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.intellij.lang.annotations.Language;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.function.Consumer;

import static io.github.maritims.ArrayUtil.emptyIfNull;

@Mojo(name = "update-stylesheets", defaultPhase = LifecyclePhase.PACKAGE)
public class VelocitySassMojo extends AbstractMojo {
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "target/${project.artifactId}/WEB-INF/templates/includes/stylesheet")
    private String targetPath;

    public void execute() {
        File dir = new File(project.getBasedir().getPath() + FILE_SEPARATOR + targetPath.replace("/", FILE_SEPARATOR));
        File[] files = emptyIfNull(dir.listFiles(), File.class);
        getLog().info("Detected " + files.length + " files to compile in " + targetPath);

        for(File file : files) {
            if(file.getName().endsWith(".vm")) {
                Document document = getDocument(file);
                if(document != null) {
                    getLog().info("Compiling " + file.toPath());
                    compileScss(document, editedDocument -> {
                        try {
                            Files.write(file.toPath(), editedDocument.outerHtml().getBytes(StandardCharsets.UTF_8));
                        } catch (IOException e) {
                            getLog().error("Unable to write document to file " + file.getName(), e);
                        }
                    });
                } else {
                    getLog().error("Unable to parse file: " + file.getName() + ". Skipping file");
                }
            } else {
                getLog().warn("Unexpected file in source directory: " + file.getName() + ". Skipping file");
            }
        }
    }

    protected Document getDocument(File file) {
        Document document = null;
        try {
            document = Jsoup.parse(file, StandardCharsets.UTF_8.toString(), "", Parser.xmlParser());
        } catch(FileNotFoundException e) {
            getLog().error("File does not exist: " + file.toPath());
        } catch (IOException e) {
            getLog().error("Unable to parse file " + file, e);
        }
        return document;
    }

    private Element getStyleElement(Document document) {
        return document.getElementsByTag("style").first();
    }

    private String compileScss(@Language("SCSS") String scss) {
        String css = null;
        try (SassCompiler compiler = SassCompilerFactory.bundled()) {
            css = compiler.compileScssString(scss).getCss();
        } catch (IOException | SassCompilationFailedException e) {
            getLog().error("Unable to compile SCSS", e);
        }
        return css;
    }

    protected boolean compileScss(Document document, Consumer<Document> consumer) {
        boolean success = false;

        if(document != null) {
            Element styleElement = getStyleElement(document);
            if (styleElement != null) {
                String css = compileScss(styleElement.text());
                styleElement.text(css);
                consumer.accept(document);
                success = true;
            }
        }

        return success;
    }
}
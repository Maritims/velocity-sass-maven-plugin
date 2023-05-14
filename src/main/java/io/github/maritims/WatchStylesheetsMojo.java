package io.github.maritims;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

@Mojo(name = "watch")
public class WatchStylesheetsMojo extends AbstractStylesheetsMojo {
    private final Log log = getLog();

    @Parameter(defaultValue = "false")
    private boolean onlyRecompileOnActualChange;

    private final Map<String, Long> crcHashes = new HashMap<>();

    protected WatchService watch(Path path, boolean onlyRecompileOnActualChange) throws IOException {
        if(!path.toFile().exists()) {
            log.error("Unable to watch path " + path + " for changes. The directory does not exist");
            return null;
        }

        if(onlyRecompileOnActualChange) {
            File[] files = path.toFile().listFiles(File::isFile);
            if (files != null) {
                for (File file : files) {
                    CRC32 crc = new CRC32();
                    crc.update(Files.readAllBytes(file.toPath()));
                    crcHashes.put(file.getName(), crc.getValue());
                }
            }
        }

        WatchService watcher = FileSystems.getDefault().newWatchService();
        path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
        return watcher;
    }

    @Override
    public void execute() {
        WatchService watcher;
        try {
            watcher = watch(Paths.get(targetPath), onlyRecompileOnActualChange);
        } catch(FileNotFoundException e) {
            log.error("Unable to configure watcher. Directory " + targetPath + " does not exist");
            return;
        } catch (IOException e) {
            log.error("Unable to configure watcher", e);
            return;
        }

        boolean keepPolling = true;
        while (keepPolling) {
            keepPolling = compileScss(watcher, onlyRecompileOnActualChange);
        }
    }

    protected boolean isFileModified(Path filePath, boolean onlyRecompileOnActualChange) {
        if(!onlyRecompileOnActualChange) {
            return true;
        }

        String content;
        try {
            content = String.join("", Files.readAllLines(filePath, StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("Unable to get CRC hash for file " + filePath + ". Assuming file has been modified as the watcher notified us about a modification", e);
            return true;
        }

        CRC32 crc = new CRC32();
        crc.update(content.getBytes());
        return crcHashes.get(filePath.getFileName().toString()) != crc.getValue();
    }

    @SuppressWarnings("unchecked")
    protected boolean compileScss(WatchService watcher, boolean onlyRecompileOnActualChange) {
        if(watcher == null) {
            log.error("Watcher is not configured. Unable to identify files for SCSS compilation");
            return false;
        }

        WatchKey key;
        try {
            key = watcher.take();
        } catch (InterruptedException e) {
            log.error("Unable to take key from watcher", e);
            return false;
        }

        List<WatchEvent<?>> events = key.pollEvents();
        Map<String, Boolean> compilationResults = new HashMap<>();
        for (WatchEvent<?> event : events) {
            Path dir = (Path) key.watchable();
            Path filePath = dir.resolve(((WatchEvent<Path>) event).context());

            boolean compile = false;
            if(event.kind().equals(StandardWatchEventKinds.ENTRY_CREATE)) {
                compile = true;
            } else if(event.kind().equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                compile = isFileModified(filePath, onlyRecompileOnActualChange);
            }

            if(compile) {
                compilationResults.put(filePath.toString(), compileScss(filePath.toFile()));
            }
        }

        return compilationResults.size() > 0 && compilationResults.values()
                .stream()
                .allMatch(success -> success);
    }
}

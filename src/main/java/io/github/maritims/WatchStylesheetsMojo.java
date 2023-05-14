package io.github.maritims;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mojo(name = "watch")
public class WatchStylesheetsMojo extends AbstractStylesheetsMojo {
    private final Log log = getLog();

    protected WatchService watch(Path path) throws IOException {
        if(!path.toFile().exists()) {
            log.error("Unable to watch path " + path + " for changes. The directory does not exist");
            return null;
        }

        WatchService watcher = FileSystems.getDefault().newWatchService();
        path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
        return watcher;
    }

    @Override
    public void execute() {
        WatchService watcher;
        try {
            watcher = watch(Paths.get(targetPath));
        } catch(FileNotFoundException e) {
            log.error("Unable to configure watcher. Directory " + targetPath + " does not exist");
            return;
        } catch (IOException e) {
            log.error("Unable to configure watcher", e);
            return;
        }

        boolean keepPolling = true;
        while (keepPolling) {
            keepPolling = compileScss(watcher);
        }
    }

    @SuppressWarnings("unchecked")
    protected boolean compileScss(WatchService watcher) {
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
            WatchEvent.Kind<Path> kind = (WatchEvent.Kind<Path>) event.kind();
            Path dir = (Path) key.watchable();
            Path filePath = dir.resolve(((WatchEvent<Path>) event).context());

            String action;
            boolean compile = false;
            if(event.kind().equals(StandardWatchEventKinds.ENTRY_CREATE)) {
                action = "Added";
                compile = true;
            } else if(event.kind().equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                action = "Modified";
                compile = true;
            } else if(event.kind().equals(StandardWatchEventKinds.ENTRY_DELETE)) {
                action = "Deleted";
            } else {
                action = "Overflowed";
            }
            log.info(action + ": " + filePath);

            if(compile) {
                compilationResults.put(filePath.toString(), compileScss(filePath.toFile()));
            }
        }

        return compilationResults.values()
                .stream()
                .allMatch(success -> success);
    }
}

package io.breen.socrates.watcher;


import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public class DynamicFileTreeWatcher {

    private static Logger logger = Logger.getLogger(DynamicFileTreeWatcher.class.getName());

    private Map<WatchKey, DynamicFileTreeItem> watchKeyToTreeItem;
    private Map<Path, DynamicFileTreeItem> pathToTreeItem;
    private WatchService watchService;
    private boolean includeHidden;
    private WatcherTask task;


    public DynamicFileTreeWatcher(boolean includeHidden) throws IOException {
        watchService = FileSystems.getDefault().newWatchService();

        watchKeyToTreeItem = new HashMap<>();
        pathToTreeItem = new HashMap<>();

        this.includeHidden = includeHidden;

        task = new WatcherTask();

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public DynamicFileTreeWatcher() throws IOException {
        this(false);
    }

    public DynamicFileTreeItem startWatching(Path path) throws IOException {
        return startWatching(path, includeHidden);
    }

    public DynamicFileTreeItem startWatching(Path path, boolean includeHidden) throws IOException {
        DynamicFileTreeItem treeItem = new DynamicFileTreeItem(path, includeHidden, watchService, watchKeyToTreeItem);
        pathToTreeItem.put(path, treeItem);

        logger.info("started watching " + path + ", attached to TreeItem " + treeItem);

        return treeItem;
    }

    public void stopWatching(Path path) {
        if (!pathToTreeItem.containsKey(path))
            throw new IllegalArgumentException("not currently watching " + path);

        stopWatching(pathToTreeItem.get(path));
    }

    public void stopWatching(DynamicFileTreeItem treeItem) {
        treeItem.cancelAll();
        pathToTreeItem.remove(treeItem.getValue());
        watchKeyToTreeItem.remove(treeItem.getWatchKey());

        logger.info("stopped watching " + treeItem.getValue() + ", attached to TreeItem " + treeItem);
    }

    public DynamicFileTreeItem getTreeItemFromPath(Path path) {
        return pathToTreeItem.get(path);
    }

    private class WatcherTask extends Task<Void> {
        @Override
        protected Void call() throws Exception {
            while (true) {
                if (isCancelled())
                    return null;

                WatchKey key;
                try {
                    key = watchService.take();
                } catch (InterruptedException x) {
                    watchService.close();
                    return null;
                }

                DynamicFileTreeItem treeItem = watchKeyToTreeItem.get(key);

                if (treeItem == null) {
                    logger.warning("WatcherTask: took unknown WatchKey: " + key);
                    continue;
                }

                Path dir = treeItem.getValue();

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind kind = event.kind();

                    if (kind == OVERFLOW)
                        continue;

                    @SuppressWarnings("unchecked") WatchEvent<Path> e = (WatchEvent<Path>) event;

                    Path fileName = e.context();
                    Path changedFile = dir.resolve(fileName);

                    Platform.runLater(() -> {
                        try {
                            treeItem.update(e, fileName);
                        } catch (IOException x) {
                            throw new RuntimeException(x);
                        }
                    });
                }

                if (!key.reset())
                    watchKeyToTreeItem.remove(key);
            }
        }
    }
}

package io.breen.socrates.watcher;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.*;

public class DynamicFileTreeItem extends FileTreeItem {

    private final WatchKey watchKey;
    private final WatchService watcher;
    private final Map<WatchKey, DynamicFileTreeItem> map;


    DynamicFileTreeItem(Path path, boolean includeHidden,
                               WatchService watcher, Map<WatchKey, DynamicFileTreeItem> map)
            throws IOException
    {
        super(path, includeHidden);

        this.watcher = watcher;
        this.map = map;

        if (Files.isDirectory(path)) {
            watchKey = path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            map.put(watchKey, this);
        } else {
            watchKey = null;
        }
    }

    WatchKey getWatchKey() {
        return watchKey;
    }

    @Override
    public String toString() {
        return "Dynamic" + super.toString();
    }

    void update(WatchEvent<Path> event, Path name) throws IOException {
        WatchEvent.Kind kind = event.kind();
        Path fullPath = getValue().resolve(name);
        ObservableList<TreeItem<Path>> children = getChildren();

        if (kind == ENTRY_CREATE && (includeHidden || !Files.isHidden(fullPath))) {
            children.add(new DynamicFileTreeItem(fullPath, includeHidden, watcher, map));
        } else if (kind == ENTRY_DELETE) {
            TreeItem<Path> toRemove = null;

            for (TreeItem<Path> child : children) {
                Path childPath = child.getValue();
                if (childPath.equals(fullPath)) {
                    toRemove = child;
                    break;
                }
            }

            if (toRemove != null)
                children.remove(toRemove);

        } else if (kind == ENTRY_MODIFY) {
            // TODO should we do anything?
        }
    }

    /**
     * Cancel the WatchKey associated with this TreeItem, and remove the WatchKey from the map that was used to
     * initialize this TreeItem. Then do the same recursively, until all WatchKeys in the subtree are canceled
     * and removed from the map.
     */
    void cancelAll() {
        watchKey.cancel();
        map.remove(watchKey);

        if (isFirstTimeChildren)
            return;

        for (TreeItem<Path> child : getChildren()) {
            if (child.isLeaf())
                continue;

            DynamicFileTreeItem c = (DynamicFileTreeItem)child;
            c.cancelAll();
        }
    }

    @Override
    protected ObservableList<TreeItem<Path>> buildChildren() throws IOException {
        Path path = getValue();

        if (path != null && Files.isDirectory(path)) {
            DirectoryStream<Path> files = Files.newDirectoryStream(path);
            ObservableList<TreeItem<Path>> children = FXCollections.observableArrayList();

            for (Path p : files) {
                if (!Files.isHidden(p) || includeHidden) {
                    children.add(new DynamicFileTreeItem(p, includeHidden, watcher, map));
                }
            }

            files.close();
            return children;
        }

        return FXCollections.emptyObservableList();
    }
}

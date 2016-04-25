package io.breen.socrates.watcher;

import io.breen.socrates.Globals;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.io.IOException;
import java.nio.file.*;


/**
 * Based on the example found on the JavaFX TreeItem API:
 * https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/TreeItem.html
 *
 * This version uses the new I/O package (java.nio).
 */
class FileTreeItem extends TreeItem<Path> {

    boolean isLeaf;
    boolean includeHidden;

    boolean isFirstTimeChildren = true;
    boolean isFirstTimeLeaf = true;


    FileTreeItem(Path path, boolean includeHidden) throws IOException {
        super(path);

        if (Files.isHidden(path) && !includeHidden)
            throw new IllegalArgumentException();

        this.includeHidden = includeHidden;
    }

    @Override
    public String toString() {
        return "File" + super.toString();
    }

    @Override
    public ObservableList<TreeItem<Path>> getChildren() {
        if (isFirstTimeChildren) {
            isFirstTimeChildren = false;

            try {
                super.getChildren().setAll(buildChildren());
            } catch (IOException x) {
                throw new RuntimeException("error building FileTreeItem children", x);
            }
        }

        return super.getChildren();
    }

    @Override
    public boolean isLeaf() {
        if (isFirstTimeLeaf) {
            isFirstTimeLeaf = false;
            Path p = getValue();
            isLeaf = Files.isSymbolicLink(p) || Files.isRegularFile(p) || Globals.directoryIsOpaque(p);
        }

        return isLeaf;
    }

    protected ObservableList<TreeItem<Path>> buildChildren() throws IOException {
        Path path = getValue();

        if (path != null && Files.isDirectory(path)) {
            DirectoryStream<Path> files = Files.newDirectoryStream(path);
            ObservableList<TreeItem<Path>> children = FXCollections.observableArrayList();

            for (Path p : files) {
                if (!Files.isHidden(p) || includeHidden) {
                    children.add(new FileTreeItem(p, includeHidden));
                }
            }

            files.close();
            return children;
        }

        return FXCollections.emptyObservableList();
    }
}

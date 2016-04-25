package io.breen.socrates.view.cell;

import javafx.scene.control.TreeCell;

import java.nio.file.Path;


public class FileTreeCell extends TreeCell<Path> {
    @Override
    protected void updateItem(Path item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText(item.getFileName().toString());
        }
    }
}

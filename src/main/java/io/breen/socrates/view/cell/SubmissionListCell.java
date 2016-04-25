package io.breen.socrates.view.cell;

import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;

import java.nio.file.Path;


public class SubmissionListCell extends ListCell<Path> {
    private Tooltip tooltip = new Tooltip();

    @Override
    protected void updateItem(Path item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            setTooltip(null);
        } else {
            setText(item.getFileName().toString());
            tooltip.setText(item.toString());
            setTooltip(tooltip);
        }
    }
}

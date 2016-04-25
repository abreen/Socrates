package io.breen.socrates.view;

import io.breen.socrates.util.Pair;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;


public class SubmissionsErrorsAlert extends Alert {
    public SubmissionsErrorsAlert(int successes, List<Pair<Path, String>> errors) {
        super(AlertType.WARNING);

        getDialogPane().setPrefSize(400, 300);

        {
            String numIssues = errors.size() == 1 ? "One issue was " : errors.size() + " issues were ";
            String numSuccesses = successes == 1 ? "One submission was " : successes + " submissions were ";

            String before;
            if (successes == 0)
                before = "No submissions were added. ";
            else
                before = numSuccesses + "added. ";

            setContentText(before + numIssues + "encountered. For more information, see the details below.");
        }

        ObservableList<String> errorsList = FXCollections.observableList(
                errors.stream().map(pair -> pair.first + " (" + pair.second + ")").collect(Collectors.toList())
        );

        ListView<String> listView = new ListView<>(errorsList);
        VBox.setVgrow(listView, Priority.ALWAYS);
        VBox vbox = new VBox(10, listView);

        getDialogPane().setExpandableContent(vbox);
    }
}

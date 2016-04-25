package io.breen.socrates.view;

import io.breen.socrates.Globals;
import io.breen.socrates.criteria.Criteria;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.VBox;


public class ReportFileNameDialog extends Dialog<String> {

    public ReportFileNameDialog(String currentName, String defaultName, String exampleUserName,
                                String exampleAssignmentName)
    {
        getDialogPane().getStylesheets().add(getClass().getClassLoader().getResource("style.css").toExternalForm());

        setTitle("Change Report File Name");

        ButtonType save = new ButtonType("Save", ButtonData.OK_DONE);

        getDialogPane().getButtonTypes().setAll(
                save,
                ButtonType.CANCEL
        );

        Label desc = new Label(
                "This name will be used when Socrates saves a grade report file. The special sequence %u will be " +
                        "substituted with the submission directory name, and the sequence %a will be substituted " +
                        "with the name of the assignment."
        );

        desc.setWrapText(true);

        TextField field = new TextField(currentName);
        field.setPromptText(defaultName);

        Label preview = new Label(currentName);
        preview.getStyleClass().add("preview");

        field.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    preview.setText(Criteria.formatReportFileName(newValue, exampleUserName, exampleAssignmentName));
                }
        );

        VBox vbox = new VBox(
                20,
                desc,
                new VBox(10, field, preview)
        );

        setResultConverter(type -> {
            if (type == save)
                return field.getText();
            else
                return null;
        });

        getDialogPane().setContent(vbox);
        getDialogPane().setMaxWidth(450);
    }
}

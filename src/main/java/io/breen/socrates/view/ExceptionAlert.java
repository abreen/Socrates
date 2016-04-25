package io.breen.socrates.view;

import io.breen.socrates.Globals;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.Arrays;


public class ExceptionAlert extends Alert {

    public ExceptionAlert(Throwable t, String contentText) {
        super(AlertType.ERROR);

        getDialogPane().getStylesheets().add(getClass().getClassLoader().getResource("style.css").toExternalForm());

        ButtonType exitButton;
        if (Globals.operatingSystem == Globals.OS.OSX)
            exitButton = new ButtonType("Quit");
        else
            exitButton = new ButtonType("Exit");

        setContentText(contentText);
        getButtonTypes().setAll(exitButton);

        getDialogPane().setPrefSize(500, 300);

        StringBuilder sb = new StringBuilder();
        for (StackTraceElement el : t.getStackTrace()) {
            sb.append(el);
            sb.append("\n");
        }

        String stackTrace = sb.toString();

        String exceptionStr = t.getClass().getSimpleName();

        if (Arrays.asList('A', 'E', 'I', 'O', 'U').indexOf(exceptionStr.charAt(0)) != -1) {
            exceptionStr = "An " + exceptionStr;
        } else {
            exceptionStr = "A " + exceptionStr;
        }

        Label label = new Label(exceptionStr + " exception was thrown.");

        TextArea exceptionArea = new TextArea(t.toString());
        exceptionArea.setEditable(false);
        exceptionArea.getStyleClass().add("stacktrace");

        GridPane.setHgrow(exceptionArea, Priority.ALWAYS);

        TextArea traceArea = new TextArea(stackTrace);
        traceArea.setEditable(false);
        traceArea.getStyleClass().add("stacktrace");

        GridPane.setVgrow(traceArea, Priority.ALWAYS);
        GridPane.setHgrow(traceArea, Priority.ALWAYS);

        GridPane pane = new GridPane();
        pane.setVgap(10);

        pane.add(label, 0, 0);
        pane.add(exceptionArea, 0, 1);
        pane.add(traceArea, 0, 2);

        getDialogPane().setExpandableContent(pane);
    }
}

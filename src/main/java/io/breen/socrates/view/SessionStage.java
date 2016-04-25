package io.breen.socrates.view;


import io.breen.socrates.Globals;
import io.breen.socrates.factory.Factory;
import io.breen.socrates.presenter.SessionPresenter;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.*;

import java.io.File;
import java.nio.file.Path;
import java.util.Date;
import java.util.Optional;

public class SessionStage extends Stage {

    private static double lastInstanceX = -1;
    private static double lastInstanceY = -1;

    private final SessionPresenter presenter;


    public SessionStage() {
        presenter = new Factory().newSessionPresenter();
        presenter.setStage(this);

        setTitle("Socrates");

        BorderPane root = presenter.getRoot();
        Scene scene = new Scene(root);

        scene.getStylesheets().add(getClass().getClassLoader().getResource("style.css").toExternalForm());

        if (Globals.operatingSystem == Globals.OS.WINDOWS) {
            getIcons().add(
                    new Image(getClass().getClassLoader().getResourceAsStream("icon64.png"))
            );
        }

        setScene(scene);
        setMinWidth(root.getMinWidth());
        setMinHeight(root.getMinHeight() + 100);

        setOnCloseRequest(
                e -> {
                    if (!presenter.needsSave())
                        return;

                    /*
                     * Prevent the Stage from getting any more WINDOW_CLOSE_REQUEST events; this prevents multiple
                     * confirmation dialogs from appearing.
                     */
                    EventHandler consumer = e2 -> { e2.consume(); };
                    addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, consumer);

                    ButtonType save = new ButtonType("Save", ButtonData.YES);
                    ButtonType dontSave = new ButtonType("Don’t Save", ButtonData.NO);
                    ButtonType dontExit;

                    if (Globals.operatingSystem == Globals.OS.OSX)
                        dontExit = new ButtonType("Don’t Quit", ButtonData.CANCEL_CLOSE);
                    else
                        dontExit = new ButtonType("Don’t Exit", ButtonData.CANCEL_CLOSE);

                    Alert a = new Alert(AlertType.CONFIRMATION);
                    a.getDialogPane().setMinHeight(200);
                    a.getButtonTypes().setAll(save, dontSave, dontExit);

                    String exitWord;
                    if (Globals.operatingSystem == Globals.OS.OSX)
                        exitWord = "quit";
                    else
                        exitWord = "exit";

                    a.setContentText(
                            "If you " + exitWord + " now, you will lose your progress on any unsaved grade reports. " +
                                    "Do you want to save your session?"
                    );

                    Optional<ButtonType> result = a.showAndWait();
                    if (result.get() == dontExit) {
                        e.consume();
                    } else if (result.get() == save) {
                        presenter.saveSession(null);
                    }

                    removeEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, consumer);
                }
        );


        /*
         * Show the stage, offset from previous instances of the stage, if necessary.
         */
        if (lastInstanceX < 1 && lastInstanceY < 1) {
            show();
            lastInstanceX = getX();
            lastInstanceY = getY();
        } else {
            lastInstanceX += 25;
            lastInstanceY += 25;
            setX(lastInstanceX);
            setY(lastInstanceY);
            show();
        }
    }

    public SessionPresenter getPresenter() {
        return presenter;
    }
}

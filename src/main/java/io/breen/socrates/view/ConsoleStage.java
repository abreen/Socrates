package io.breen.socrates.view;

import io.breen.socrates.factory.Factory;
import io.breen.socrates.presenter.ConsolePresenter;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ConsoleStage extends Stage {
    private static ConsoleStage instance = null;

    public static ConsoleStage getInstance() {
        if (instance == null)
            instance = new ConsoleStage();

        return instance;
    }

    private ConsoleStage() {
        ConsolePresenter presenter = new Factory().newConsolePresenter();

        setTitle("Developer Console");

        VBox root = presenter.getRoot();
        Scene scene = new Scene(root);

        scene.getStylesheets().add(getClass().getClassLoader().getResource("style.css").toExternalForm());

        setScene(scene);
        setMinWidth(root.getMinWidth());
        setMinHeight(root.getMinHeight() + 100);
    }
}


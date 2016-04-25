package io.breen.socrates.view;


import io.breen.socrates.factory.Factory;
import io.breen.socrates.presenter.AboutPresenter;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AboutStage extends Stage {
    private static AboutStage instance = null;

    public static AboutStage getInstance() {
        if (instance == null)
            instance = new AboutStage();

        return instance;
    }

    private AboutStage() {
        AboutPresenter presenter = new Factory().newAboutPresenter();

        setTitle("About");

        VBox root = presenter.getRoot();
        Scene scene = new Scene(root);

        scene.getStylesheets().add(getClass().getClassLoader().getResource("style.css").toExternalForm());

        setScene(scene);
        setMinWidth(root.getMinWidth());
        setMinHeight(root.getMinHeight() + 100);
        setResizable(false);
    }
}

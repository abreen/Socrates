package io.breen.socrates.view;

import io.breen.socrates.factory.Factory;
import io.breen.socrates.presenter.FileViewerPresenter;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;


public class FileViewerStage extends Stage {

    private final FileViewerPresenter presenter;

    public FileViewerStage(File f) throws IOException {
        presenter = new Factory().newFileViewerPresenter();

        VBox root = presenter.getRoot();
        Scene scene = new Scene(root);

        scene.getStylesheets().add(getClass().getClassLoader().getResource("style.css").toExternalForm());

        setScene(scene);
        setMinWidth(root.getMinWidth());
        setMinHeight(root.getMinHeight() + 100);

        setTitle(f.getName());
        presenter.setFile(f);
    }
}

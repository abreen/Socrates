package io.breen.socrates.factory;

import io.breen.socrates.presenter.AboutPresenter;
import io.breen.socrates.presenter.ConsolePresenter;
import io.breen.socrates.presenter.FileViewerPresenter;
import io.breen.socrates.presenter.SessionPresenter;
import javafx.fxml.FXMLLoader;

import java.io.IOException;


public class Factory {
    public SessionPresenter newSessionPresenter() {
        SessionPresenter presenter;
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.load(getClass().getClassLoader().getResource("session.fxml").openStream());
            presenter = loader.getController();
        } catch (IOException x) {
            throw new RuntimeException("failed loading FXML for SessionPresenter", x);
        }

        return presenter;
    }

    public AboutPresenter newAboutPresenter() {
        AboutPresenter presenter;
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.load(getClass().getClassLoader().getResource("about.fxml").openStream());
            presenter = loader.getController();
        } catch (IOException x) {
            throw new RuntimeException("failed loading FXML for AboutPresenter", x);
        }

        return presenter;
    }

    public FileViewerPresenter newFileViewerPresenter() {
        FileViewerPresenter presenter;
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.load(getClass().getClassLoader().getResource("file_viewer.fxml").openStream());
            presenter = loader.getController();
        } catch (IOException x) {
            throw new RuntimeException("failed loading FXML for FileViewerPresenter", x);
        }

        return presenter;
    }

    public ConsolePresenter newConsolePresenter() {
        ConsolePresenter presenter;
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.load(getClass().getClassLoader().getResource("console.fxml").openStream());
            presenter = loader.getController();
        } catch (IOException x) {
            throw new RuntimeException("failed loading FXML for ConsolePresenter", x);
        }

        return presenter;
    }
}

package io.breen.socrates.presenter;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class AboutPresenter implements Initializable {

    private static Logger logger = Logger.getLogger(AboutPresenter.class.getName());

    @FXML private VBox root;
    @FXML private Label socratesVersion;
    @FXML private Label javaVersion;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        InputStream pom = getClass().getClassLoader().getResourceAsStream("app.properties");
        Properties mavenProps = null;

        if (pom != null) {
            boolean loaded = false;
            try {
                mavenProps = new Properties();
                mavenProps.load(pom);
                loaded = true;

            } catch (IOException x) {
                logger.warning("could not load pom.properties: " + x);
            }

            if (loaded) {
                socratesVersion.setText(mavenProps.getProperty("version"));
            }

        } else {
            logger.warning("could not find pom.properties");

            socratesVersion.setText("?");
        }

        {
            String version = System.getProperty("java.version");
            if (version != null)
                javaVersion.setText(version);
            else
                javaVersion.setText("?");
        }
    }

    public VBox getRoot() {
        return root;
    }
}

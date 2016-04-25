package io.breen.socrates.presenter;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.*;


public class ConsolePresenter implements Initializable {

    private static Logger logger = Logger.getLogger(ConsolePresenter.class.getName());

    @FXML private StyleClassedTextArea textArea;
    @FXML private VBox root;

    private InputStream loggingData;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PipedOutputStream log = new PipedOutputStream();

        try {
            loggingData = new PipedInputStream(log);
        } catch (IOException x) {
            logger.warning("failed setting up PipedInputStreams: " + x);
            return;
        }

        StreamHandler handler = new StreamHandler(log, new SimpleFormatter());

        Logger manager = LogManager.getLogManager().getLogger("");
        manager.addHandler(handler);

        Thread thread = new Thread(
                () -> {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(loggingData));
                    try {
                        String line;

                        while ((line = reader.readLine()) != null) {
                            final String finalLine = line + "\n";
                            Platform.runLater(() -> textArea.appendText(finalLine));
                        }

                    } catch (IOException x) {
                        logger.warning("error: " + x);
                    }
                }
        );
        thread.setDaemon(true);
        thread.start();
    }

    public VBox getRoot() {
        return root;
    }
}

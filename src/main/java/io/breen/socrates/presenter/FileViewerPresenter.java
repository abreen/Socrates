package io.breen.socrates.presenter;

import io.breen.socrates.Globals;
import io.breen.socrates.Globals.OS;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ResourceBundle;


public class FileViewerPresenter implements Initializable {

    @FXML private VBox root;
    @FXML private CodeArea codeArea;
    @FXML private Button open;

    private File file;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

        if (Globals.operatingSystem == OS.OSX)
            open.setText("Open in Default Application");
        else
            open.setText("Open in Default Program");
    }

    public VBox getRoot() {
        return root;
    }

    public void setFile(File file) throws IOException {
        this.file = file;
        updateFile();
    }

    private void updateFile() throws IOException {
        if (file == null)
            return;

        String content = "";
        BufferedReader reader = Files.newBufferedReader(file.toPath());
        String line;
        while ((line = reader.readLine()) != null)
            content += line + "\n";
        reader.close();

        codeArea.appendText(content);
    }

    @FXML
    private void openExternal(ActionEvent e) {
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException ignored) {}
    }
}

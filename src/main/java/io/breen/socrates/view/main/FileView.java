package io.breen.socrates.view.main;

import io.breen.socrates.Globals;
import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.submission.SubmittedFile;
import jsyntaxpane.DefaultSyntaxKit;
import jsyntaxpane.util.Configuration;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.IOException;

public class FileView {

    private JEditorPane editor;
    private JPanel rootPanel;
    private JScrollPane scrollPane;

    private void createUIComponents() {
        DefaultSyntaxKit.initKit();

        Configuration config = DefaultSyntaxKit.getConfig(DefaultSyntaxKit.class);
        config.put("DefaultFont", "Monospaced-PLAIN-14");

        editor = new JEditorPane();

        scrollPane = new JScrollPane(editor);
        if (Globals.operatingSystem == Globals.OS.OSX) {
            Border border = new LineBorder(new Color(197, 197, 197));
            scrollPane.setBorder(border);
        }
    }

    public void update(SubmittedFile submittedFile, File matchingFile)
            throws IOException
    {
        if (matchingFile != null) {
            editor.setContentType(matchingFile.contentType);
        }
        editor.setText(submittedFile.getContents());
    }
}

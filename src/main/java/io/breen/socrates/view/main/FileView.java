package io.breen.socrates.view.main;

import io.breen.socrates.Globals;
import io.breen.socrates.immutable.submission.SubmittedFile;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;

public class FileView {

    private JEditorPane editor;
    private JPanel rootPanel;
    private JScrollPane scrollPane;

    private void createUIComponents() {
        scrollPane = new JScrollPane(editor);
        if (Globals.operatingSystem == Globals.OS.OSX) {
            Border border = new LineBorder(new Color(197, 197, 197));
            scrollPane.setBorder(border);
        }
    }

    public void update(SubmittedFile file) {

    }
}

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
import java.util.Map;

public class FileView {

    private enum ThemeType {
        DEFAULT
    }

    private Configuration config;

    private JEditorPane editor;
    private JPanel rootPanel;
    private JScrollPane scrollPane;

    private void createUIComponents() {
        DefaultSyntaxKit.initKit();

        config = DefaultSyntaxKit.getConfig(DefaultSyntaxKit.class);
        editor = new JEditorPane();

        changeTheme(ThemeType.DEFAULT);

        scrollPane = new JScrollPane(editor);
        if (Globals.operatingSystem == Globals.OS.OSX) {
            Border border = new LineBorder(new Color(197, 197, 197));
            scrollPane.setBorder(border);
        }
    }

    public void update(SubmittedFile submittedFile, File matchingFile) throws IOException
    {
        if (matchingFile != null) {
            editor.setContentType(matchingFile.contentType);
        }
        editor.setText(submittedFile.getContents());
    }

    public void changeTheme(ThemeType t) {
        Map<String, String> themeEntries;

        switch (t) {
        case DEFAULT:
        default:
            editor.setBackground(DefaultTheme.backgroundColor);
            editor.setForeground(DefaultTheme.foregroundColor);
            themeEntries = DefaultTheme.map;
        }

        for (Map.Entry<String, String> entry : themeEntries.entrySet())
            config.put(entry.getKey(), entry.getValue());
    }
}

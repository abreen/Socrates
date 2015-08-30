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

    public enum ThemeType {
        DEFAULT,
        BASE16_LIGHT,
        BASE16_DARK
    }

    private Configuration config;
    private SubmittedFile currentFile;

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

    /**
     * Update the FileView and show the contents of the current file to the user. If the second
     * parameter is null, the content type of the file will be set to plain text.
     */
    public void update(SubmittedFile submittedFile, File matchingFile) throws IOException {
        if (submittedFile == currentFile) return;

        if (matchingFile == null) {
            editor.setContentType("text/plain");
        } else {
            editor.setContentType(matchingFile.contentType);
        }
        editor.setText(submittedFile.getContents());
        editor.setCaretPosition(0);

        currentFile = submittedFile;
    }

    public void update(SubmittedFile submittedFile) throws IOException {
        update(submittedFile, null);
    }

    public void reset() {
        currentFile = null;
        editor.setText(null);
    }

    public void changeTheme(ThemeType t) {
        Map<String, String> themeEntries;

        switch (t) {
        case BASE16_LIGHT:
            editor.setBackground(Base16LightTheme.backgroundColor);
            editor.setForeground(Base16LightTheme.foregroundColor);
            themeEntries = Base16LightTheme.map;
            break;
        case BASE16_DARK:
            editor.setBackground(Base16DarkTheme.backgroundColor);
            editor.setForeground(Base16DarkTheme.foregroundColor);
            themeEntries = Base16DarkTheme.map;
            break;
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

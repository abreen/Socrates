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

    private Configuration config;

    private JEditorPane editor;
    private JPanel rootPanel;
    private JScrollPane scrollPane;

    private void createUIComponents() {
        DefaultSyntaxKit.initKit();

        config = DefaultSyntaxKit.getConfig(DefaultSyntaxKit.class);
        editor = new JEditorPane();

        changeTheme(new DefaultTheme());

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

    public void changeTheme(DefaultTheme theme) {
        editor.setBackground(Color.decode(theme.background));
        editor.setForeground(Color.decode(theme.foreground));

        config.put("SelectionColor", theme.selectionColor);

        config.put("DefaultFont", theme.font);

        config.put("LineNumbers.RightMargin", theme.lineNumberMargin);
        config.put("LineNumbers.Foreground", theme.lineNumberForeground);
        config.put("LineNumbers.Background", theme.lineNumberBackground);
        config.put("LineNumbers.CurrentBack", theme.currentLineBackground);

        config.put("Style.KEYWORD", theme.keyword);
        config.put("Style.KEYWORD2", theme.keyword2);
        config.put("Style.TYPE", theme.type);
        config.put("Style.TYPE2", theme.type2);
        config.put("Style.TYPE3", theme.type3);
        config.put("Style.STRING", theme.string);
        config.put("Style.STRING2", theme.string2);
        config.put("Style.NUMBER", theme.number);
        config.put("Style.REGEX", theme.regex);
        config.put("Style.IDENTIFIER", theme.identifier);
        config.put("Style.DEFAULT", theme.dephault);
        config.put("Style.WARNING", theme.warning);
        config.put("Style.ERROR", theme.error);
        config.put("Style.COMMENT", theme.comment);
        config.put("Style.COMMENT2", theme.comment2);
        config.put("Style.OPERATOR", theme.operator);
        config.put("Style.DELIMITER", theme.delimiter);
    }
}

package io.breen.socrates.view.main;

import io.breen.socrates.Globals;
import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.submission.SubmittedFile;
import io.breen.socrates.model.wrapper.SubmittedFileWrapperNode;
import io.breen.socrates.model.wrapper.UnrecognizedFileWrapperNode;
import jsyntaxpane.DefaultSyntaxKit;
import jsyntaxpane.util.Configuration;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

public class FileView {

    public enum ThemeType {
        DEFAULT,
        BASE16_LIGHT,
        BASE16_DARK
    }

    private static Logger logger = Logger.getLogger(FileView.class.getName());
    public final Action defaultTheme;
    public final Action base16Light;
    public final Action base16Dark;
    private Configuration config;
    private SubmittedFile currentFile;
    private JEditorPane editor;
    private JPanel rootPanel;
    private JScrollPane scrollPane;

    public FileView(MenuBarManager menuBar, SubmissionTree submissionTree) {
        defaultTheme = new AbstractAction(menuBar.defaultTheme.getText()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeTheme(ThemeType.DEFAULT);
            }
        };
        menuBar.defaultTheme.setAction(defaultTheme);

        base16Light = new AbstractAction(menuBar.base16Light.getText()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeTheme(ThemeType.BASE16_LIGHT);
            }
        };
        menuBar.base16Light.setAction(base16Light);

        base16Dark = new AbstractAction(menuBar.base16Dark.getText()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeTheme(ThemeType.BASE16_DARK);
            }
        };
        menuBar.base16Dark.setAction(base16Dark);

        submissionTree.addTreeSelectionListener(
                new TreeSelectionListener() {
                    @Override
                    public void valueChanged(TreeSelectionEvent e) {
                        TreePath path = e.getPath();
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode)path
                                .getLastPathComponent();

                        if (!e.isAddedPath()) node = null;

                        if (node != null && (node instanceof SubmittedFileWrapperNode || node
                                instanceof UnrecognizedFileWrapperNode)) {
                            SubmittedFile sf = (SubmittedFile)node.getUserObject();
                            File matchingFile = null;
                            if (node instanceof SubmittedFileWrapperNode)
                                matchingFile = ((SubmittedFileWrapperNode)node).matchingFile;

                            try {
                                update(sf, matchingFile);
                            } catch (IOException x) {
                                logger.warning("encountered I/O exception updating view");
                            }
                        } else {
                            reset();
                        }
                    }
                }
        );
    }

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

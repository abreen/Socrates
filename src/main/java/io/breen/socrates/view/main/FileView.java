package io.breen.socrates.view.main;

import com.threecrickets.jygments.ResolutionException;
import com.threecrickets.jygments.format.Formatter;
import com.threecrickets.jygments.grammar.Lexer;
import io.breen.socrates.Globals;
import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.submission.SubmittedFile;
import io.breen.socrates.model.wrapper.SubmittedFileWrapperNode;
import io.breen.socrates.model.wrapper.UnrecognizedFileWrapperNode;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.logging.Logger;

public class FileView {

    private static Logger logger = Logger.getLogger(FileView.class.getName());
    private SubmittedFile currentFile;
    private Formatter htmlFormatter;
    private JTextPane textPane;
    private JPanel rootPanel;
    private JScrollPane scrollPane;

    public FileView(MenuBarManager menuBar, SubmissionTree submissionTree) {
        textPane.setContentType("text/html");

        /*
         * See http://elliotth.blogspot.com/2007/10/why-is-my-html-jtextpane-beeping-at-me.html
         */
        textPane.getDocument().putProperty("IgnoreCharsetDirective", true);

        scrollPane.setViewportView(textPane);

        try {
            htmlFormatter = Formatter.getByName("html");
        } catch (ResolutionException x) {
            logger.severe("cannot get HTML formatter");
        }

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
                                reset();
                                logger.warning("encountered I/O exception updating view: " + x);
                            }
                        } else {
                            reset();
                        }
                    }
                }
        );
    }

    private void createUIComponents() {
        scrollPane = new JScrollPane();
        if (Globals.operatingSystem == Globals.OS.OSX) {
            Border border = new LineBorder(new Color(197, 197, 197));
            scrollPane.setBorder(border);
        }
    }

    /**
     * Update the FileView and show the contents of the current file to the user. If the second
     * parameter is null, the file will not be syntax highlighted.
     */
    public void update(SubmittedFile submittedFile, File matchingFile) throws IOException {
        if (submittedFile == currentFile) return;

        String contents = submittedFile.getContents(), text = contents;

        try {
            Lexer lexer = Lexer.getByName(matchingFile != null ? matchingFile.language : null);
            CharArrayWriter w = new CharArrayWriter();
            htmlFormatter.format(lexer.getTokens(contents), w);
            text = w.toString();

        } catch (ResolutionException ignored) {}

        textPane.setText(text);
        textPane.setCaretPosition(0);

        currentFile = submittedFile;
    }

    public void update(SubmittedFile submittedFile) throws IOException {
        update(submittedFile, null);
    }

    public void reset() {
        currentFile = null;
        textPane.setText(null);
    }
}

package io.breen.socrates.view.main;

import io.breen.socrates.Globals;
import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.submission.SubmittedFile;
import io.breen.socrates.model.wrapper.SubmittedFileWrapperNode;
import io.breen.socrates.model.wrapper.UnrecognizedFileWrapperNode;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class FileInfo {

    private enum FileProperty {
        FILE_TYPE(0, "File type", "—"),
        FILE_SIZE(1, "Size", "—"),
        HAS_RECEIPT(2, "Has receipt", "—"),
        SUBMITTED_DATE(3, "Submitted", "—"),
        MODIFIED_DATE(4, "Last modified", "—");

        public final int index;
        public final String labelText;
        public final String defaultText;

        FileProperty(int index, String labelText, String defaultText) {
            this.index = index;
            this.labelText = labelText;
            this.defaultText = defaultText;
        }

        public static String[] getKeys() {
            String[] keys = new String[FileProperty.values().length];
            int i = 0;
            for (FileProperty p : FileProperty.values())
                keys[i++] = p.labelText;
            return keys;
        }

        public static String[] getDefaults() {
            String[] defaults = new String[FileProperty.values().length];
            int i = 0;
            for (FileProperty p : FileProperty.values())
                defaults[i++] = p.defaultText;
            return defaults;
        }
    }
    private static final String NO_FILE_SELECTED_FILENAME = "(no file selected)";
    private final static SimpleDateFormat formatter = new SimpleDateFormat(
            "EEE M/d h:mm:ss a"
    );
    private static Logger logger = Logger.getLogger(FileInfo.class.getName());
    private JPanel rootPanel;
    private JLabel fileName;
    private JPanel innerPanel;
    private PropertiesList properties;

    public FileInfo(MenuBarManager menuBar, SubmissionTree submissionTree) {
        properties = new PropertiesList(
                FileProperty.getKeys(), FileProperty.getDefaults()
        );

        properties.setOpaque(false);
        innerPanel.add(properties, BorderLayout.CENTER);

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
                                logger.warning("encountered I/O exception updating info");
                            }
                        } else {
                            reset();
                        }
                    }
                }
        );
    }

    private static Date getModified(Path path) throws IOException {
        FileTime modified = Files.getLastModifiedTime(path);
        return new Date(modified.toMillis());
    }

    private void createUIComponents() {
        rootPanel = new JPanel();

        /*
         * Make center pane inset on OS X
         */
        if (Globals.operatingSystem == Globals.OS.OSX) {
            rootPanel.setBorder(UIManager.getBorder("InsetBorder.aquaVariant"));
        }
    }

    /**
     * Update the FileInfoView to show information about the current file. If the second parameter
     * is null, the file type property will be reset (e.g., "Unknown").
     */
    public void update(SubmittedFile file, File matchingFile) throws IOException {
        fileName.setText(file.localPath.toString());

        if (matchingFile == null) {
            properties.reset(FileProperty.FILE_TYPE.index);
        } else {
            properties.set(FileProperty.FILE_TYPE.index, matchingFile.getFileTypeName());
        }

        properties.set(
                FileProperty.FILE_SIZE.index, FileUtils.byteCountToDisplaySize(file.size)
        );

        properties.set(
                FileProperty.HAS_RECEIPT.index, file.receipt == null ? "No" : "Yes"
        );

        properties.set(
                FileProperty.SUBMITTED_DATE.index,
                file.receipt == null ? "Unknown" : formatter.format(file.receipt.getLatestDate())
        );

        properties.set(
                FileProperty.MODIFIED_DATE.index, formatter.format(getModified(file.fullPath))
        );
    }

    public void update(SubmittedFile submittedFile) throws IOException {
        update(submittedFile, null);
    }

    public void reset() {
        fileName.setText(NO_FILE_SELECTED_FILENAME);
        properties.resetAll();
    }
}

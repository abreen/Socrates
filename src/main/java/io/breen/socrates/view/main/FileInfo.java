package io.breen.socrates.view.main;

import io.breen.socrates.Globals;
import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.submission.SubmittedFile;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileInfo {

    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
            "EEE L/d h:mm:ss a"
    );

    private enum FileProperty {
        FILE_TYPE(0, "File type", "Unknown"),
        FILE_SIZE(1, "Size", "0 bytes"),
        HAS_RECEIPT(2, "Has receipt", "Unknown"),
        SUBMITTED_DATE(3, "Submitted", "Unknown"),
        MODIFIED_DATE(4, "Last modified", "Unknown");

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

    private JPanel rootPanel;
    private JLabel fileName;
    private JPanel innerPanel;
    private PropertiesList properties;

    public FileInfo() {
        properties = new PropertiesList(
                FileProperty.getKeys(), FileProperty.getDefaults()
        );

        properties.setOpaque(false);
        innerPanel.add(properties, BorderLayout.CENTER);
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

    public void update(SubmittedFile file, File matchingFile) throws IOException {
        fileName.setText(file.localPath.toString());

        if (matchingFile == null) {
            properties.reset(FileProperty.FILE_TYPE.index);
        } else {
            properties.set(FileProperty.FILE_TYPE.index, matchingFile.getFileTypeName());
        }

        properties.set(
                FileProperty.FILE_SIZE.index,
                FileUtils.byteCountToDisplaySize(file.size)
        );

        properties.set(
                FileProperty.HAS_RECEIPT.index,
                file.receipt == null ? "No" : "Yes"
        );

        properties.set(
                FileProperty.SUBMITTED_DATE.index,
                file.receipt == null ? "Unknown" : file.receipt.getLatestDate()
                                                               .format(formatter)
        );

        properties.set(
                FileProperty.MODIFIED_DATE.index,
                getModified(file.fullPath).format(formatter)
        );
    }

    private static LocalDateTime getModified(Path path) throws IOException {
        FileTime modified = Files.getLastModifiedTime(path);
        return LocalDateTime.ofInstant(modified.toInstant(), Globals.getZoneId());
    }
}

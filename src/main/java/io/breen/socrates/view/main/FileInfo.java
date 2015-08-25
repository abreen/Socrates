package io.breen.socrates.view.main;

import io.breen.socrates.Globals;
import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.submission.SubmittedFile;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class FileInfo {

    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
            "EEE L/d hh:mm:ss a"
    );

    private enum FileProperty {
        FILE_TYPE("File type", "Unknown"),
        FILE_SIZE("Size", "0 bytes"),
        HAS_RECEIPT("Has receipt", "Unknown"),
        SUBMITTED_DATE("Submitted", "Unknown"),
        MODIFIED_DATE("Last modified", "Unknown");

        public final String labelText;
        public final String defaultText;

        FileProperty(String labelText, String defaultText) {
            this.labelText = labelText;
            this.defaultText = defaultText;
        }
    }

    private Map<FileProperty, JTextField> properties;

    private JPanel rootPanel;
    private JLabel fileName;
    private JPanel innerPanel;
    private JPanel propertiesPanel;
    private JPanel propertiesKeys;
    private JPanel propertiesValues;

    public FileInfo() {
        propertiesKeys = new JPanel();
        propertiesKeys.setLayout(new BoxLayout(propertiesKeys, BoxLayout.PAGE_AXIS));
        propertiesKeys.setOpaque(false);

        propertiesValues = new JPanel();
        propertiesValues.setLayout(new BoxLayout(propertiesValues, BoxLayout.PAGE_AXIS));
        propertiesValues.setOpaque(false);

        propertiesPanel.add(propertiesKeys, BorderLayout.LINE_START);
        propertiesPanel.add(propertiesValues, BorderLayout.LINE_END);

        properties = new HashMap<>();

        for (FileProperty prop : FileProperty.values()) {
            JLabel key = new JLabel(prop.labelText, SwingConstants.RIGHT);
            key.setFont(Font.decode("Dialog-12"));
            key.setForeground(UIManager.getColor("inactiveCaptionText"));
            key.setAlignmentX(Component.RIGHT_ALIGNMENT);
            propertiesKeys.add(key);
            propertiesKeys.add(Box.createRigidArea(new Dimension(0, 5)));

            JTextField value = new JTextField(prop.defaultText, 13);
            value.setOpaque(false);
            value.setEditable(false);
            value.setBackground(new Color(0, 0, 0, 0));
            value.setBorder(new EmptyBorder(0, 0, 0, 0));
            value.setFont(Font.decode("Dialog-12"));
            value.setAlignmentX(Component.LEFT_ALIGNMENT);
            propertiesValues.add(value);
            propertiesValues.add(Box.createRigidArea(new Dimension(0, 5)));

            properties.put(prop, value);
        }
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
            setDefaultForProperty(FileProperty.FILE_TYPE);
        } else {
            changeLabelForProperty(
                    FileProperty.FILE_TYPE, matchingFile.getFileTypeName()
            );
        }

        changeLabelForProperty(
                FileProperty.FILE_SIZE, FileUtils.byteCountToDisplaySize(file.size)
        );
        changeLabelForProperty(
                FileProperty.HAS_RECEIPT, file.receipt == null ? "No" : "Yes"
        );
        changeLabelForProperty(
                FileProperty.SUBMITTED_DATE,
                file.receipt == null ? "Unknown" : file.receipt.getLatestDate()
                                                               .format(formatter)
        );
        changeLabelForProperty(
                FileProperty.MODIFIED_DATE, getModified(file.fullPath).format(formatter)
        );
    }

    private void setDefaultPropertyLabels() {
        for (FileProperty property : FileProperty.values())
            setDefaultForProperty(property);
    }

    private void setDefaultForProperty(FileProperty property) {
        changeLabelForProperty(property, property.defaultText);
    }

    private void changeLabelForProperty(FileProperty property, String labelText) {
        properties.get(property).setText(labelText);
    }

    private static LocalDateTime getModified(Path path) throws IOException {
        FileTime modified = Files.getLastModifiedTime(path);
        return LocalDateTime.ofInstant(modified.toInstant(), Globals.getZoneId());
    }
}

package io.breen.socrates.view.main;

import io.breen.socrates.Globals;

import javax.swing.*;

public class FileInfo {

    private JPanel rootPanel;
    private JTable properties;
    private JTextArea fileName;

    private void createUIComponents() {
        rootPanel = new JPanel();

        /*
         * Make center pane inset on OS X
         */
        if (Globals.operatingSystem == Globals.OS.OSX) {
            rootPanel.setBorder(UIManager.getBorder("InsetBorder.aquaVariant"));
        }
    }
}

package io.breen.socrates.view.main;

import io.breen.socrates.Globals;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;

public class SubmissionList {

    private JPanel rootPanel;
    private JTree tree;
    private JScrollPane scrollPane;

    private void createUIComponents() {
        scrollPane = new JScrollPane(tree);
        if (Globals.operatingSystem == Globals.OS.OSX) {
            Border border = new LineBorder(new Color(197, 197, 197));
            scrollPane.setBorder(border);
        }
    }
}

package io.breen.socrates.view.main;

import io.breen.socrates.Globals;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;

public class TestList {

    private JPanel rootPanel;
    private JList list;
    private JScrollPane scrollPane;

    private void createUIComponents() {
        scrollPane = new JScrollPane(list);
        if (Globals.operatingSystem == Globals.OS.OSX) {
            Border border = new LineBorder(new Color(197, 197, 197));
            scrollPane.setBorder(border);
        }
    }
}

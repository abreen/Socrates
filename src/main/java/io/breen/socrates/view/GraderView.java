package io.breen.socrates.view;

import javax.swing.*;
import java.awt.*;

public class GraderView extends JFrame {

    private JPanel panel;
    private JTree testTree;
    private JEditorPane editorPane;
    private JComboBox studentComboBox;
    private JTextArea statusText;
    private JList filesList;
    private JScrollPane testTreeScrollPane;
    private JScrollPane editorScrollPane;
    private JScrollPane filesScrollPane;
    private JButton nextButton;
    private JButton previousButton;

    private JMenuBar menuBar;

    public GraderView() {
        super("Socrates");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setContentPane(panel);
        this.setSize(900, 700);
        this.setLocationRelativeTo(null);

        menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);
    }

    public void addMenu(JMenu menu) {
        menuBar.add(menu);
    }

    public void setStatusText(String str) {
        statusText.setText(str);
    }
}

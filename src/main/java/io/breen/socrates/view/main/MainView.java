package io.breen.socrates.view.main;

import javax.swing.*;
import java.awt.*;

public class MainView extends JFrame {

    private SubmissionInfo submissionInfo;
    private SubmissionList submissionList;
    private FileView fileView;
    private FileInfo fileInfo;
    private TestList testList;
    private TestInfo testInfo;
    private JPanel rootPanel;

    public MainView() {
        super("Socrates");

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setContentPane(rootPanel);

        this.setMinimumSize(new Dimension(800, 600));
        this.setSize(new Dimension(1100, 600));
        this.setLocationRelativeTo(null);
    }
}

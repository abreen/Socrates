package io.breen.socrates.view.main;

import javax.swing.*;
import java.awt.*;

public class MainView extends JFrame {

    public SubmissionTree submissionTree;
    public FileView fileView;
    public FileInfo fileInfo;
    public TestTree testTree;
    public TestControls testControls;
    private JPanel rootPanel;

    public MainView() {
        super("Socrates");

        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setContentPane(rootPanel);

        this.setMinimumSize(new Dimension(800, 600));
        this.setSize(new Dimension(1200, 750));
        this.setLocationRelativeTo(null);
    }
}

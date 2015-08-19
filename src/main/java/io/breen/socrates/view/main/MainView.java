package io.breen.socrates.view.main;

import io.breen.socrates.immutable.submission.SubmittedFile;

import javax.swing.*;
import java.awt.*;

public class MainView extends JFrame {

    public SubmissionTree submissionTree;
    public FileView fileView;
    public FileInfo fileInfo;
    public TestList testList;
    public TestInfo testInfo;
    private JPanel rootPanel;

    public MainView() {
        super("Socrates");

        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setContentPane(rootPanel);

        this.setMinimumSize(new Dimension(800, 600));
        this.setSize(new Dimension(1100, 600));
        this.setLocationRelativeTo(null);

        submissionTree.addTreeSelectionListener(
                event -> {
                    SubmittedFile file = submissionTree.getSelectedSubmittedFile();
                    if (file != null)
                        fileInfo.update(file);
                }
        );
    }
}

package io.breen.socrates.view.main;

import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.immutable.submission.SubmittedFile;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainView extends JFrame {

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

    public void addUngradedSubmissions(List<Submission> submissions) {
        submissionList.addUngraded(submissions);
    }

    public void setActiveSubmittedFile(SubmittedFile submittedFile) {
        //submissionList.selectFile(submittedFile);
    }
}

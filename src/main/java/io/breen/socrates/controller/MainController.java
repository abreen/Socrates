package io.breen.socrates.controller;

import io.breen.socrates.immutable.criteria.Criteria;
import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.immutable.submission.SubmittedFile;
import io.breen.socrates.model.TestNode;
import io.breen.socrates.view.main.MainView;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class MainController {

    private static Logger logger = Logger.getLogger(MainController.class.getName());

    private Criteria criteria;
    private List<Submission> submissions;
    private MainView mainView;

    public MainController() {
        mainView = new MainView();

        mainView.submissionTree.addTreeSelectionListener(
                event -> {
                    SubmittedFile submitted = mainView.submissionTree.getSelectedSubmittedFile();
                    if (submitted != null) {
                        File matchingFile = criteria.files.get(submitted.localPath);
                        try {
                            mainView.fileView.update(submitted, matchingFile);
                            mainView.fileInfo.update(submitted, matchingFile);
                            mainView.testTree.update(submitted, matchingFile);
                        } catch (IOException x) {
                            logger.warning("encountered I/O exception updating view");
                        }
                    }
                }
        );

        mainView.testTree.addTreeSelectionListener(
                event -> {
                    TestNode testNode = mainView.testTree.getSelectedTestNode();
                    if (testNode != null) {
                        mainView.testControls.update(testNode);
                    }
                }
        );


    }

    public void start(Criteria criteria, List<Submission> submissions) {
        this.criteria = criteria;
        this.submissions = submissions;

        mainView.setTitle("Socrates — " + criteria.assignmentName);

        mainView.submissionTree.addUngraded(submissions);
        mainView.submissionTree.expandSubmission(submissions.get(0));

        mainView.setVisible(true);

        logger.info("started MainView");
    }
}

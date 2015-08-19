package io.breen.socrates.controller;

import io.breen.socrates.immutable.criteria.Criteria;
import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.immutable.submission.SubmittedFile;
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
                        File matchingFile = findMatchingFile(submitted);
                        try {
                            mainView.fileView.update(matchingFile, submitted);
                        } catch (IOException x) {
                            logger.warning("encountered IOE updating FileView");
                        }
                    }
                }
        );
    }

    public void start(Criteria criteria, List<Submission> submissions) {
        this.criteria = criteria;
        this.submissions = submissions;

        mainView.setTitle("Socrates — " + criteria.assignmentName);

        mainView.submissionTree.addUngraded(submissions);
        mainView.submissionTree.setActiveSubmittedFile(submissions.get(0).files.get(0));

        mainView.setVisible(true);
    }

    private File findMatchingFile(SubmittedFile submittedFile) {
        for (File f : criteria.files)
            if (submittedFile.localPath.equals(f.localPath))
                return f;
        return null;
    }
}

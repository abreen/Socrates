package io.breen.socrates.controller;

import io.breen.socrates.immutable.criteria.Criteria;
import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.view.main.MainView;

import java.util.List;

public class MainController {

    private Criteria criteria;
    private List<Submission> submissions;
    private MainView mainView;

    public MainController() {
        mainView = new MainView();
    }

    public void start(Criteria criteria, List<Submission> submissions) {
        this.criteria = criteria;
        this.submissions = submissions;

        mainView.addUngradedSubmissions(submissions);
        //mainView.setActiveSubmission(submissions.get(0));
        //mainView.setActiveFile(submissions.get(0).files.get(0));

        mainView.setVisible(true);
    }
}

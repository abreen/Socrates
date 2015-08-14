package io.breen.socrates.controller;

import io.breen.socrates.constructor.InvalidCriteriaException;
import io.breen.socrates.immutable.criteria.Criteria;
import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.view.DetailOptionPane;
import io.breen.socrates.view.setup.SetupView;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SetupController {

    private static Logger logger = Logger.getLogger(SetupController.class.getName());

    private Criteria criteria;

    private SetupView view;

    public SetupController(MainController main) {
        view = new SetupView();

        view.addOpenCriteriaButtonActionListener(
                e -> {
                    java.io.File f = view.chooseCriteriaFile();
                    if (f != null) {
                        try {
                            criteria = Criteria.loadFromYAML(f);
                        } catch (Exception x) {
                            DetailOptionPane.showMessageDialog(
                                    view,
                                    "There was an error opening the criteria file you "
                                            + "selected.",
                                    "Error Opening Criteria",
                                    JOptionPane.ERROR_MESSAGE,
                                    x.toString()
                            );
                            return;
                        }

                        // criteria was loaded correctly
                        view.showSubmissionsCard();
                    }
                }
        );

        view.addSubmissionsButtonActionListener(
                e -> {
                    java.io.File[] fs = view.chooseSubmissions();
                    if (fs != null) {
                        List<Submission> submissions = new ArrayList<>(fs.length);
                        for (java.io.File f : fs)
                            submissions.add(Submission.fromDirectory(f));

                        view.setVisible(false);
                        view.dispose();

                        main.start(criteria, submissions);
                    }
                }
        );
    }

    public void start(String criteriaPath) {
        if (criteriaPath != null) {
            try {
                criteria = Criteria.loadFromYAML(new java.io.File(criteriaPath));
            } catch (FileNotFoundException | InvalidCriteriaException e) {
                logger.info(criteriaPath + " was an invalid criteria path");
            }
        }

        if (criteria == null) {
            view.showCriteriaCard();
        } else {
            // we can skip the "Choose a criteria file" step
            view.showSubmissionsCard();
        }

        view.setVisible(true);
    }
}

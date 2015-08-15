package io.breen.socrates.controller;

import io.breen.socrates.constructor.InvalidCriteriaException;
import io.breen.socrates.immutable.criteria.Criteria;
import io.breen.socrates.immutable.submission.ReceiptFormatException;
import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.view.DetailOptionPane;
import io.breen.socrates.view.setup.SetupView;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
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
                    Path path = view.chooseCriteriaFile();
                    if (path != null) {
                        try {
                            criteria = Criteria.loadFromYAML(path);
                        } catch (IOException | InvalidCriteriaException x) {
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
                    List<Path> ps = view.chooseSubmissions();
                    if (ps != null) {
                        List<Submission> submissions = new ArrayList<>(ps.size());
                        for (Path p : ps) {
                            try {
                                submissions.add(Submission.fromDirectory(p));
                            } catch (IOException x) {
                                logger.warning("IOE thrown when adding submission: " + x);
                            } catch (ReceiptFormatException x) {
                                logger.warning("RFE thrown when adding submission: " + x);
                            }
                        }

                        view.setVisible(false);
                        view.dispose();

                        main.start(criteria, submissions);
                    }
                }
        );
    }

    public void start(Path criteriaPath) {
        if (criteriaPath != null) {
            try {
                criteria = Criteria.loadFromYAML(criteriaPath);
            } catch (IOException | InvalidCriteriaException x) {
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

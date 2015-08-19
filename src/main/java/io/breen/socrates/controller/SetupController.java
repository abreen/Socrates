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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                            criteria = Criteria.loadFromPath(path);
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

                        // criteria was successfully loaded
                        logger.info("criteria was successfully loaded");
                        view.showSubmissionsCard();
                    }
                }
        );

        view.addSubmissionsButtonActionListener(
                event -> {
                    List<Path> ps = view.chooseSubmissions();
                    if (ps != null) {
                        Map<Path, Exception> errors = new HashMap<>();
                        List<Submission> submissions = new ArrayList<>(ps.size());
                        for (Path p : ps) {
                            try {
                                submissions.add(Submission.fromDirectory(p));
                            } catch (IOException x) {
                                errors.put(p, x);
                                logger.warning("IOE thrown when adding submission: " + x);
                            } catch (ReceiptFormatException x) {
                                errors.put(p, x);
                                logger.warning("RFE thrown when adding submission: " + x);
                            }
                        }

                        int numErrors = errors.size();
                        int numAdded = submissions.size();
                        if (numErrors > 0) {
                            StringBuilder sb = new StringBuilder();
                            for (Map.Entry<Path, Exception> e : errors.entrySet())
                                sb.append(e.getKey() + ": " + e.getValue() + "\n");

                            String msg = "There was a problem opening " + numErrors + " submission" + (numErrors == 1 ? "" : "s") + ".";
                            if (numAdded > 0) {
                                msg += " The remaining " + numAdded + " submission" + (numAdded == 1 ? " is" : "s are") + " available to grade.";
                            }
                            String title = (numErrors == 1 ? "Error" : "Errors") + " Opening Submissions";

                            DetailOptionPane.showMessageDialog(
                                    view,
                                    msg,
                                    title,
                                    JOptionPane.INFORMATION_MESSAGE,
                                    sb.toString()
                            );
                        }

                        if (numAdded > 0) {
                            view.setVisible(false);
                            view.dispose();

                            main.start(criteria, submissions);
                        } else {
                            logger.warning("no submissions could be added");
                        }
                    }
                }
        );
    }

    public void start(Path criteriaPath) {
        if (criteriaPath != null) {
            try {
                criteria = Criteria.loadFromPath(criteriaPath);
            } catch (IOException | InvalidCriteriaException x) {
                logger.warning(criteriaPath + " specified an invalid criteria");
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

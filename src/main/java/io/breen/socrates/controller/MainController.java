package io.breen.socrates.controller;

import io.breen.socrates.immutable.criteria.Criteria;
import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.immutable.submission.SubmittedFile;
import io.breen.socrates.model.TestNode;
import io.breen.socrates.view.main.FileView;
import io.breen.socrates.view.main.MainView;
import io.breen.socrates.view.main.MenuBarManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class MainController {

    private static Logger logger = Logger.getLogger(MainController.class.getName());

    private Criteria criteria;
    private List<Submission> submissions;
    private MainView mainView;
    private MenuBarManager menuBar;

    public MainController() {
        mainView = new MainView();
        menuBar = new MenuBarManager(mainView);

        // ctrl will end up being Command on OS X
        int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        int shift = InputEvent.SHIFT_DOWN_MASK;

        /*
         * Set up actions and attach them to components.
         */
        Action nextSubmission = newMenuItemAction(
                menuBar.nextSubmission,
                e -> mainView.submissionTree.goToNextSubmission()
        );
        nextSubmission.setEnabled(true);
        nextSubmission.putValue(
                Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ctrl | shift)
        );

        Action previousSubmission = newMenuItemAction(
                menuBar.previousSubmission,
                e -> mainView.submissionTree.goToPreviousSubmission()
        );
        previousSubmission.setEnabled(false);
        previousSubmission.putValue(
                Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ctrl | shift)
        );

        Action revealSubmission = newMenuItemAction(
                menuBar.revealSubmission,
                e -> {
                    Submission s = mainView.submissionTree.getSelectedSubmission();
                    Path path = s.submissionDir;
                    try {
                        Desktop.getDesktop().open(path.toFile());
                    } catch (IOException x) {
                        logger.warning("got I/O exception revealing submission: " + x);
                    }
                }
        );
        revealSubmission.setEnabled(false);
        revealSubmission.putValue(
                Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_R, ctrl | shift)
        );

        mainView.submissionTree.addTreeSelectionListener(
                e -> {
                    if (!mainView.submissionTree.hasSelection()) {
                        nextSubmission.setEnabled(true);
                        previousSubmission.setEnabled(false);
                        revealSubmission.setEnabled(false);
                    } else {
                        revealSubmission.setEnabled(true);

                        if (mainView.submissionTree.lastSubmissionSelected())
                            nextSubmission.setEnabled(false);
                        else
                            nextSubmission.setEnabled(true);

                        if (mainView.submissionTree.firstSubmissionSelected())
                            previousSubmission.setEnabled(false);
                        else
                            previousSubmission.setEnabled(true);
                    }
                }
        );

        newMenuItemAction(
                menuBar.defaultTheme,
                e -> mainView.fileView.changeTheme(FileView.ThemeType.DEFAULT)
        );

        newMenuItemAction(
                menuBar.base16Light,
                e -> mainView.fileView.changeTheme(FileView.ThemeType.BASE16_LIGHT)
        );

        newMenuItemAction(
                menuBar.base16Dark,
                e -> mainView.fileView.changeTheme(FileView.ThemeType.BASE16_DARK)
        );

        /*
         * Set up event listeners.
         */
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

    private static Action newMenuItemAction(JMenuItem item, Consumer<ActionEvent> lambda) {
        Action a = new AbstractAction(item.getText()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                lambda.accept(e);
            }
        };

        item.setAction(a);
        return a;
    }
}

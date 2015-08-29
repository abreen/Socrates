package io.breen.socrates.controller;

import io.breen.socrates.immutable.criteria.Criteria;
import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.immutable.submission.SubmittedFile;
import io.breen.socrates.immutable.test.*;
import io.breen.socrates.model.*;
import io.breen.socrates.view.main.*;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class MainController {

    private static Logger logger = Logger.getLogger(MainController.class.getName());

    private Criteria criteria;
    private List<Submission> submissions;
    private Map<SubmittedFile, FileReport> reports;

    private MainView mainView;
    private MenuBarManager menuBar;

    public MainController() {
        reports = new HashMap<>();

        mainView = new MainView();
        menuBar = new MenuBarManager(mainView);

        // ctrl will end up being Command on OS X
        int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        int shift = InputEvent.SHIFT_DOWN_MASK;
        int alt = InputEvent.ALT_DOWN_MASK;

        /*
         * Set up theme-related options.
         */
        newMenuItemAction(
                menuBar.defaultTheme, e -> mainView.fileView.changeTheme(FileView.ThemeType.DEFAULT)
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
         * Set up test-related actions.
         */
        Action passTest = newMenuItemAction(
                menuBar.passTest, e -> {
                    TestWrapperNode node = mainView.testTree.getSelectedTestWrapperNode();
                    Test test = (Test)node.getUserObject();

                    if (test instanceof Automatable) {
                        AutomationStage stage = node.getAutomationStage();
                        switch (stage) {
                        case STARTED:
                            // cannot change result while test is running
                            return;
                        case FINISHED_NORMAL:
                        case NONE:
                            if (userWantsToOverride()) mainView.testTree.passTest();
                            return;
                        case FINISHED_ERROR:
                            mainView.testTree.passTest();
                        }
                    } else {
                        mainView.testTree.passTest();
                    }
                }
        );
        passTest.setEnabled(false);
        passTest.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_UP, ctrl)
        );
        mainView.testControls.setPassTestAction(passTest);

        Action failTest = newMenuItemAction(
                menuBar.failTest, e -> {
                    TestWrapperNode node = mainView.testTree.getSelectedTestWrapperNode();
                    Test test = (Test)node.getUserObject();

                    if (test instanceof Automatable) {
                        AutomationStage stage = node.getAutomationStage();
                        switch (stage) {
                        case STARTED:
                            // cannot change result while test is running
                            return;
                        case FINISHED_NORMAL:
                        case NONE:
                            if (userWantsToOverride()) mainView.testTree.failTest();
                            return;
                        case FINISHED_ERROR:
                            mainView.testTree.failTest();
                        }
                    } else {
                        mainView.testTree.failTest();
                    }
                }
        );
        failTest.setEnabled(false);
        failTest.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, ctrl)
        );
        mainView.testControls.setFailTestAction(failTest);

        Action resetTest = newMenuItemAction(
                menuBar.resetTest, e -> mainView.testTree.resetTest()
        );
        resetTest.setEnabled(false);
        mainView.testControls.setResetTestAction(resetTest);

        /*
         * Set up test navigation options.
         */
        Action nextTest = newMenuItemAction(
                menuBar.nextTest, e -> mainView.testTree.goToNextTest()
        );
        nextTest.setEnabled(false);
        nextTest.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ctrl)
        );

        Action previousTest = newMenuItemAction(
                menuBar.previousTest, e -> mainView.testTree.goToPreviousTest()
        );
        previousTest.setEnabled(false);
        previousTest.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ctrl)
        );

        Action clearNotes = newMenuItemAction(
                menuBar.clearNotes, e -> mainView.testControls.clearNotes()
        );
        clearNotes.setEnabled(false);
        clearNotes.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_K, ctrl)
        );

        Action focusOnNotes = newMenuItemAction(
                menuBar.focusOnNotes, e -> mainView.testControls.focusOnNotes()
        );
        focusOnNotes.setEnabled(false);
        focusOnNotes.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, ctrl)
        );

        /*
         * TreeSelectionListener for updating the disabled state of the test state
         * and navigation options.
         */
        mainView.testTree.addTreeSelectionListener(
                e -> {
                    TestWrapperNode testNode = mainView.testTree.getSelectedTestWrapperNode();

                    mainView.testControls.update(testNode);

                    if (testNode == null) {
                        nextTest.setEnabled(true);
                        previousTest.setEnabled(false);

                        clearNotes.setEnabled(false);
                        focusOnNotes.setEnabled(false);
                        return;
                    } else {
                        nextTest.setEnabled(true);

                        if (mainView.testTree.firstTestForFileSelected())
                            previousTest.setEnabled(false);
                        else previousTest.setEnabled(true);

                        clearNotes.setEnabled(true);
                        focusOnNotes.setEnabled(true);
                    }

                    Test test = (Test)testNode.getUserObject();
                    if (test instanceof Automatable &&
                            testNode.getResult() == TestResult.NONE &&
                            testNode.getAutomationStage() == AutomationStage.NONE &&
                            !testNode.isConstrained())
                    {
                        SubmittedFile submitted = mainView.submissionTree
                                .getSelectedSubmittedFile();
                        File file = criteria.files.get(submitted.localPath);

                        if (file == null) return;

                        Submission submission = mainView.submissionTree.getSelectedSubmission();

                        (new Thread(
                                () -> {
                                    Automatable a = (Automatable)test;
                                    testNode.setAutomationStage(AutomationStage.STARTED);
                                    try {
                                        boolean passed = a.shouldPass(
                                                file, submitted, submission
                                        );

                                        if (passed) testNode.setResult(TestResult.PASSED);
                                        else testNode.setResult(TestResult.FAILED);

                                        testNode.setAutomationStage(
                                                AutomationStage.FINISHED_NORMAL
                                        );

                                    } catch (CannotBeAutomatedException x) {
                                        testNode.setAutomationStage(
                                                AutomationStage.FINISHED_ERROR
                                        );

                                    } finally {
                                        mainView.testTree.nodeChanged(testNode);
                                    }
                                }
                        )).start();
                    }
                }
        );

        /*
         * Set up submission-related actions.
         */
        Action nextSubmission = newMenuItemAction(
                menuBar.nextSubmission, e -> mainView.submissionTree.goToNextSubmission()
        );
        nextSubmission.setEnabled(true);
        nextSubmission.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ctrl | shift)
        );

        Action previousSubmission = newMenuItemAction(
                menuBar.previousSubmission, e -> mainView.submissionTree.goToPreviousSubmission()
        );
        previousSubmission.setEnabled(false);
        previousSubmission.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ctrl | shift)
        );

        Action revealSubmission = newMenuItemAction(
                menuBar.revealSubmission, e -> {
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
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, ctrl | shift)
        );

        /*
         * Set up file-related actions.
         */
        Action nextFile = newMenuItemAction(
                menuBar.nextFile, e -> mainView.submissionTree.goToNextFile()
        );
        nextFile.setEnabled(true);
        nextFile.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ctrl | alt)
        );

        Action previousFile = newMenuItemAction(
                menuBar.previousFile, e -> mainView.submissionTree.goToPreviousFile()
        );
        previousFile.setEnabled(false);
        previousFile.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ctrl | alt)
        );

        Action openFile = newMenuItemAction(
                menuBar.openFile, e -> {
                    SubmittedFile f = mainView.submissionTree.getSelectedSubmittedFile();
                    Path path = f.fullPath;
                    try {
                        Desktop.getDesktop().open(path.toFile());
                    } catch (IOException x) {
                        logger.warning("got I/O exception revealing file: " + x);
                    }
                }
        );
        openFile.setEnabled(false);

        /*
         * Set up event listeners.
         */
        mainView.submissionTree.addTreeSelectionListener(
                event -> {
                    FileReport report = null;
                    File matchingFile = null;
                    SubmittedFile submitted = mainView.submissionTree.getSelectedSubmittedFile();

                    if (submitted != null) {
                        report = reports.get(submitted);
                        matchingFile = report == null ? null : report.matchingFile;
                        try {
                            mainView.fileView.update(submitted, matchingFile);
                            mainView.fileInfo.update(submitted, matchingFile);
                            mainView.testTree.update(report);
                            mainView.testControls.update(null);
                        } catch (IOException x) {
                            logger.warning("encountered I/O exception updating view");
                        }
                    }

                    /*
                     * Update enabled state of actions.
                     */
                    if (!mainView.submissionTree.hasSelection()) {
                        revealSubmission.setEnabled(false);
                        openFile.setEnabled(false);

                        nextSubmission.setEnabled(true);
                        nextFile.setEnabled(true);
                        previousSubmission.setEnabled(false);

                        nextTest.setEnabled(false);
                        previousTest.setEnabled(false);
                    } else {
                        revealSubmission.setEnabled(true);
                        openFile.setEnabled(true);

                        if (mainView.submissionTree.lastSubmissionSelected())
                            nextSubmission.setEnabled(false);
                        else nextSubmission.setEnabled(true);

                        if (mainView.submissionTree.firstSubmissionSelected())
                            previousSubmission.setEnabled(false);
                        else previousSubmission.setEnabled(true);

                        if (mainView.submissionTree.lastFileInSubmissionSelected())
                            nextFile.setEnabled(false);
                        else nextFile.setEnabled(true);

                        if (mainView.submissionTree.firstFileInSubmissionSelected())
                            previousFile.setEnabled(false);
                        else previousFile.setEnabled(true);

                        if (matchingFile != null) {
                            int n = matchingFile.testRoot.members.size();
                            if (n > 0) nextTest.setEnabled(true);
                        } else {
                            nextTest.setEnabled(false);
                        }
                    }
                }
        );
    }

    private static Action newMenuItemAction(JMenuItem item, Consumer<ActionEvent> lambda)
    {
        Action a = new AbstractAction(item.getText()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                lambda.accept(e);
            }
        };

        item.setAction(a);
        return a;
    }

    private static void addTreeChangedListener(TreeModel model, Consumer<TreeModelEvent> lambda)
    {
        TreeModelListener listener = new TreeModelListener() {
            @Override
            public void treeNodesChanged(TreeModelEvent e) {
                lambda.accept(e);
            }

            @Override
            public void treeNodesInserted(TreeModelEvent e) {
                // should not happen for test tree
            }

            @Override
            public void treeNodesRemoved(TreeModelEvent e) {
                // should not happen for test tree
            }

            @Override
            public void treeStructureChanged(TreeModelEvent e) {
                // should not happen for test tree
            }
        };

        model.addTreeModelListener(listener);
    }

    public void start(Criteria criteria, List<Submission> submissions) {
        this.criteria = criteria;
        this.submissions = submissions;

        for (Submission submission : submissions) {
            for (SubmittedFile submitted : submission.files) {
                File matchingFile = criteria.files.get(submitted.localPath);
                if (matchingFile == null) continue;

                reports.put(submitted, new FileReport(submitted, matchingFile));
            }
        }

        mainView.setTitle("Socrates — " + criteria.assignmentName);

        mainView.submissionTree.addUngraded(submissions);
        mainView.submissionTree.expandFirstSubmission();

        mainView.setVisible(true);

        logger.info("started MainView");
    }

    private boolean userWantsToOverride() {
        int rv = JOptionPane.showConfirmDialog(
                mainView,
                "This is an automated test. Are you sure you want to\n" + "override the" +
                        " automated test's result?",
                "Override Automated Test?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return rv == JOptionPane.YES_OPTION;
    }
}

package io.breen.socrates.controller;

import io.breen.socrates.Globals;
import io.breen.socrates.immutable.TextGradeReportFormatter;
import io.breen.socrates.immutable.criteria.Criteria;
import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.immutable.submission.SubmittedFile;
import io.breen.socrates.immutable.test.*;
import io.breen.socrates.model.AutomationStage;
import io.breen.socrates.model.TestResult;
import io.breen.socrates.model.wrapper.SubmissionWrapperNode;
import io.breen.socrates.model.wrapper.TestWrapperNode;
import io.breen.socrates.util.Pair;
import io.breen.socrates.view.main.MainView;
import io.breen.socrates.view.main.MenuBarManager;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

public class MainController {

    private static Logger logger = Logger.getLogger(MainController.class.getName());

    private Criteria criteria;
    private List<Submission> submissions;

    private MainView mainView;
    private MenuBarManager menuBar;

    public MainController() {
        menuBar = new MenuBarManager();
        mainView = new MainView(this, menuBar);
        menuBar.setView(mainView);

        /*
         * The MainController will listen to the TestTree to see if an automated test
         * gets selected. If so, this thread will spawn a new thread to run the test.
         */
        mainView.testTree.addTreeSelectionListener(
                new TreeSelectionListener() {
                    @Override
                    public void valueChanged(TreeSelectionEvent e) {
                        TreePath path = e.getPath();
                        final TestWrapperNode node = (TestWrapperNode)path.getLastPathComponent();

                        Test testObj = (Test)node.getUserObject();
                        if (testObj instanceof Automatable &&
                                node.getResult() == TestResult.NONE &&
                                node.getAutomationStage() == AutomationStage.NONE &&
                                !node.isConstrained())
                        {
                            @SuppressWarnings("unchecked") final Automatable<File> test =
                                    (Automatable<File>)testObj;

                            final SubmittedFile submitted = mainView.submissionTree
                                    .getSelectedSubmittedFile();
                            final File file = criteria.getFileByLocalPath(submitted.localPath);

                            if (file == null) return;

                            SubmissionWrapperNode swn = mainView.submissionTree
                                    .getCurrentSubmissionNode();
                            final Submission submission = (Submission)swn.getUserObject();

                            (new Thread() {
                                @Override
                                public void run() {
                                    node.setAutomationStage(AutomationStage.STARTED);
                                    try {
                                        boolean passed = test.shouldPass(
                                                file, submitted, submission, criteria
                                        );

                                        if (passed) node.setResult(TestResult.PASSED);
                                        else node.setResult(TestResult.FAILED);

                                        node.setAutomationStage(
                                                AutomationStage.FINISHED_NORMAL
                                        );

                                    } catch (CannotBeAutomatedException x) {
                                        logger.warning("test cannot be automated: " + test);

                                        node.setAutomationStage(
                                                AutomationStage.FINISHED_ERROR
                                        );
                                    } catch (AutomationFailureException x) {
                                        logger.warning("failure automating test: " + x.e);

                                        node.setAutomationStage(
                                                AutomationStage.FINISHED_ERROR
                                        );
                                    }
                                }
                            }).start();
                        }
                    }
                }
        );
    }

    public void start(Path criteriaPath, Criteria criteria, List<Submission> submissions) {
        this.criteria = criteria;
        this.submissions = submissions;

        Map<Submission, List<Pair<SubmittedFile, File>>> map = new HashMap<>();

        for (Submission s : submissions) {
            List<Pair<SubmittedFile, File>> list = new ArrayList<>(s.files.size());
            for (SubmittedFile f : s.files) {
                File matchingFile = criteria.getFileByLocalPath(f.localPath);
                Pair<SubmittedFile, File> pair = new Pair<>(f, matchingFile);
                list.add(pair);
            }
            map.put(s, list);
        }

        if (Globals.operatingSystem == Globals.OS.OSX) {
            JRootPane root = mainView.getRootPane();
            root.putClientProperty("Window.documentFile", criteriaPath.toFile());
            mainView.setTitle(criteria.assignmentName);
        } else {
            mainView.setTitle("Socrates — " + criteria.assignmentName);
        }

        mainView.submissionTree.addUngraded(map);
        mainView.submissionTree.expandFirstSubmission();

        mainView.setVisible(true);

        logger.info("started MainView");
    }

    public void saveGradeReport(SubmissionWrapperNode completed, Path dest) {
        mainView.setEnabled(false);
        TextGradeReportFormatter fmt = new TextGradeReportFormatter(criteria);
        try {
            fmt.toFile(completed, dest);
            completed.setSaved(true);
        } catch (IOException x) {
            logger.warning("could not save grade report: " + x);
        }
        mainView.setEnabled(true);
    }
}

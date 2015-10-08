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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class MainController {

    private static Logger logger = Logger.getLogger(MainController.class.getName());

    private Criteria criteria;
    private List<Submission> submissions;

    private BlockingQueue<TestTask> tasks;

    private MainView mainView;
    private MenuBarManager menuBar;

    public MainController() {
        menuBar = new MenuBarManager();
        mainView = new MainView(this, menuBar);
        menuBar.setView(mainView);

        tasks = new LinkedBlockingQueue<>();

        /*
         * The MainController will listen to the TestTree to see if an automated test
         * gets selected. If so, this thread will queue the test in the TestRunner's queue for
         * execution.
         */
        mainView.testTree.addTreeSelectionListener(
                new TreeSelectionListener() {
                    @Override
                    public void valueChanged(TreeSelectionEvent e) {
                        TreePath path = e.getPath();
                        TestWrapperNode node = (TestWrapperNode)path.getLastPathComponent();

                        Test testObj = (Test)node.getUserObject();
                        if (testObj instanceof Automatable &&
                                node.getResult() == TestResult.NONE &&
                                node.getAutomationStage() == AutomationStage.NONE &&
                                !node.isConstrained())
                        {
                            @SuppressWarnings("unchecked") final Automatable<File> test =
                                    (Automatable<File>)testObj;

                            SubmittedFile submittedFile = mainView.submissionTree
                                    .getSelectedSubmittedFile();
                            File file = criteria.getFileByLocalPath(submittedFile.localPath);

                            if (file == null) return;

                            SubmissionWrapperNode swn = mainView.submissionTree
                                    .getCurrentSubmissionNode();
                            Submission submission = (Submission)swn.getUserObject();

                            tasks.add(
                                    new TestTask(
                                            node, test, file, submittedFile, submission
                                    )
                            );

                            logger.info("added test task");
                        }
                    }
                }
        );

        /*
         * Schedule a thread that runs TestTasks in the queue whenever they appear.
         */
        (new Thread() {
            @Override
            public void run() {
                while (true) {
                    TestTask t;

                    try {
                        t = tasks.take();
                    } catch (InterruptedException x) {
                        return;
                    }

                    t.node.setAutomationStage(AutomationStage.STARTED);
                    try {
                        boolean passed = t.test.shouldPass(
                                t.file, t.submittedFile, t.submission, criteria
                        );

                        if (passed) t.node.setResult(TestResult.PASSED);
                        else t.node.setResult(TestResult.FAILED);

                        t.node.setAutomationStage(
                                AutomationStage.FINISHED_NORMAL
                        );

                    } catch (CannotBeAutomatedException x) {
                        logger.warning("test cannot be automated: " + t.test);

                        t.node.setAutomationStage(
                                AutomationStage.FINISHED_ERROR
                        );
                    } catch (AutomationFailureException x) {
                        logger.warning("failure automating test: " + x.e);

                        t.node.setAutomationStage(
                                AutomationStage.FINISHED_ERROR
                        );
                    }
                }
            }
        }).start();
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

    private class TestTask<T> {

        public final TestWrapperNode node;
        public final Automatable test;
        public final File file;
        public final SubmittedFile submittedFile;
        public final Submission submission;

        public TestTask(TestWrapperNode node, Automatable test, File file,
                        SubmittedFile submittedFile, Submission submission)
        {
            this.node = node;
            this.test = test;
            this.file = file;
            this.submittedFile = submittedFile;
            this.submission = submission;
        }
    }
}

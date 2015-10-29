package io.breen.socrates.controller;

import io.breen.socrates.Globals;
import io.breen.socrates.TextGradeReportFormatter;
import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.File;
import io.breen.socrates.model.AutomationStage;
import io.breen.socrates.model.TestResult;
import io.breen.socrates.model.wrapper.SubmissionWrapperNode;
import io.breen.socrates.model.wrapper.TestWrapperNode;
import io.breen.socrates.submission.Submission;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.test.*;
import io.breen.socrates.util.Pair;
import io.breen.socrates.view.main.MainView;
import io.breen.socrates.view.main.MenuBarManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class MainController {

    private static Logger logger = Logger.getLogger(MainController.class.getName());
    public JFrame transcriptWindow;
    public JTextPane transcriptTextPane;
    public Action showTranscript;
    private Criteria criteria;
    private List<Submission> submissions;
    private BlockingQueue<TestTask> tasks;
    private MainView mainView;
    private MenuBarManager menuBar;

    public MainController() {
        menuBar = new MenuBarManager();
        mainView = new MainView(this, menuBar);
        menuBar.setView(mainView);

        transcriptWindow = new JFrame("Transcript");
        transcriptWindow.setAlwaysOnTop(true);
        transcriptWindow.setMinimumSize(new Dimension(300, 200));
        transcriptWindow.setSize(new Dimension(450, 300));
        transcriptWindow.setLocationRelativeTo(null);

        if (Globals.operatingSystem == Globals.OS.OSX) {
            transcriptWindow.getRootPane().putClientProperty("Window.style", "small");
        }

        transcriptTextPane = new JTextPane();
        transcriptTextPane.setEditable(false);
        transcriptTextPane.setFont(Font.decode(Font.MONOSPACED));

        DefaultCaret caret = (DefaultCaret)transcriptTextPane.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollPane = new JScrollPane(transcriptTextPane);
        scrollPane.setBorder(new EmptyBorder(3, 3, 3, 3));

        transcriptWindow.add(scrollPane);

        int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        showTranscript = new AbstractAction(menuBar.transcriptWindow.getText()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                transcriptWindow.setVisible(true);
            }
        };
        showTranscript.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, ctrl)
        );
        menuBar.transcriptWindow.setAction(showTranscript);

        final Document transcriptDocument = transcriptTextPane.getDocument();

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
                            SubmittedFile submittedFile = mainView.submissionTree
                                    .getSelectedSubmittedFile();
                            File file = criteria.getFileByLocalPath(submittedFile.localPath);

                            if (file == null) return;

                            SubmissionWrapperNode swn = mainView.submissionTree
                                    .getCurrentSubmissionNode();
                            Submission submission = (Submission)swn.getUserObject();

                            tasks.add(
                                    new TestTask(
                                            node, testObj, file, submittedFile, submission
                                    )
                            );
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
                        Automatable automatableTest = (Automatable)t.automatableTest;

                        boolean passed = automatableTest.shouldPass(
                                t.file, t.submittedFile, t.submission, criteria, transcriptDocument
                        );

                        if (passed) t.node.setResult(TestResult.PASSED);
                        else t.node.setResult(TestResult.FAILED);

                        t.node.setAutomationStage(
                                AutomationStage.FINISHED_NORMAL
                        );

                    } catch (CannotBeAutomatedException x) {
                        logger.warning(t.automatableTest + ": cannot be automated: " + x);

                        t.node.setAutomationStage(
                                AutomationStage.FINISHED_ERROR
                        );

                    } catch (AutomationFailureException x) {
                        logger.severe(t.automatableTest + ": failure automating test: " + x);

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

        Map<Submission, List<Pair<SubmittedFile, File>>> map = new TreeMap<>();

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

    private class TestTask {

        public final TestWrapperNode node;
        public final Test automatableTest;
        public final File file;
        public final SubmittedFile submittedFile;
        public final Submission submission;

        public TestTask(TestWrapperNode node, Test automatableTest, File file,
                        SubmittedFile submittedFile, Submission submission)
        {
            this.node = node;
            this.automatableTest = automatableTest;
            this.file = file;
            this.submittedFile = submittedFile;
            this.submission = submission;
        }
    }
}

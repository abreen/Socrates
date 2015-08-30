package io.breen.socrates.controller;

import io.breen.socrates.immutable.criteria.Criteria;
import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.immutable.submission.SubmittedFile;
import io.breen.socrates.immutable.test.*;
import io.breen.socrates.model.AutomationStage;
import io.breen.socrates.model.TestResult;
import io.breen.socrates.model.wrapper.TestWrapperNode;
import io.breen.socrates.util.Pair;
import io.breen.socrates.view.main.MainView;
import io.breen.socrates.view.main.MenuBarManager;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class MainController {

    private static Logger logger = Logger.getLogger(MainController.class.getName());

    private Criteria criteria;
    private List<Submission> submissions;

    private MainView mainView;
    private MenuBarManager menuBar;

    public MainController() {
        menuBar = new MenuBarManager();
        mainView = new MainView(menuBar);
        menuBar.setView(mainView);

        /*
         * The MainController will listen to the TestTree to see if an automated test
         * gets selected. If so, this thread will spawn a new thread to run the test.
         */
        mainView.testTree.addTreeSelectionListener(
                event -> {
                    TreePath path = event.getPath();
                    TestWrapperNode node = (TestWrapperNode)path.getLastPathComponent();

                    Test testObj = (Test)node.getUserObject();
                    if (testObj instanceof Automatable &&
                            node.getResult() == TestResult.NONE &&
                            node.getAutomationStage() == AutomationStage.NONE &&
                            !node.isConstrained())
                    {
                        @SuppressWarnings("unchecked") Automatable<File> test =
                                (Automatable<File>)testObj;

                        SubmittedFile submitted = mainView.submissionTree
                                .getSelectedSubmittedFile();
                        File file = criteria.files.get(submitted.localPath);

                        if (file == null) return;

                        Submission submission = mainView.submissionTree.getSelectedSubmission();

                        (new Thread(
                                () -> {
                                    node.setAutomationStage(AutomationStage.STARTED);
                                    try {
                                        boolean passed = test.shouldPass(
                                                file, submitted, submission
                                        );

                                        if (passed) node.setResult(TestResult.PASSED);
                                        else node.setResult(TestResult.FAILED);

                                        node.setAutomationStage(
                                                AutomationStage.FINISHED_NORMAL
                                        );

                                    } catch (CannotBeAutomatedException x) {
                                        node.setAutomationStage(
                                                AutomationStage.FINISHED_ERROR
                                        );

                                    } finally {
                                        mainView.testTree.nodeChanged(node);
                                    }
                                }
                        )).start();
                    }
                }
        );
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

        Map<Submission, List<Pair<SubmittedFile, File>>> map = new HashMap<>();

        for (Submission s : submissions) {
            List<Pair<SubmittedFile, File>> list = new ArrayList<>(s.files.size());
            for (SubmittedFile f : s.files) {
                File matchingFile = criteria.files.get(f.localPath);
                Pair<SubmittedFile, File> pair = new Pair<>(f, matchingFile);
                list.add(pair);
            }
            map.put(s, list);
        }

        mainView.setTitle("Socrates — " + criteria.assignmentName);

        mainView.submissionTree.addUngraded(map);
        mainView.submissionTree.expandFirstSubmission();

        mainView.setVisible(true);

        logger.info("started MainView");
    }
}

package io.breen.socrates.view.main;

import io.breen.socrates.Globals;
import io.breen.socrates.immutable.test.Test;
import io.breen.socrates.immutable.test.TestGroup;
import io.breen.socrates.immutable.test.ceiling.AtMost;
import io.breen.socrates.immutable.test.ceiling.Ceiling;
import io.breen.socrates.model.FileReport;
import io.breen.socrates.model.TestResult;
import io.breen.socrates.model.TestWrapperNode;
import io.breen.socrates.view.icon.FailedTestIcon;
import io.breen.socrates.view.icon.NoResultTestIcon;
import io.breen.socrates.view.icon.PassedTestIcon;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.text.DecimalFormat;

public class TestTree {

    private JPanel rootPanel;
    private JScrollPane scrollPane;
    private JTree tree;

    private void createUIComponents() {
        tree = new JTree((TreeModel)null) {
            @Override
            public String convertValueToText(Object value, boolean selected,
                                             boolean expanded, boolean leaf, int row,
                                             boolean hasFocus)
            {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
                Object userObject = node.getUserObject();

                if (userObject instanceof TestGroup) {
                    return testGroupToString((TestGroup)userObject);

                } else if (userObject instanceof Test) {
                    return ((Test)userObject).description;
                }

                return super.convertValueToText(
                        value, selected, expanded, leaf, row, hasFocus
                );
            }
        };

        /*
         * This ensures that only tests can be selected.
         */
        tree.setSelectionModel(
                new PredicateTreeSelectionModel(
                        path -> {
                            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                                    path.getLastPathComponent();
                            return node.getUserObject() instanceof Test;
                        }
                )
        );

        Icon defaultIcon = new NoResultTestIcon();
        Icon passedIcon = new PassedTestIcon();
        Icon failedIcon = new FailedTestIcon();

        tree.setCellRenderer(
                new DefaultTreeCellRenderer() {
                    @Override
                    public Component getTreeCellRendererComponent(JTree tree,
                                                                  Object value,
                                                                  boolean selected,
                                                                  boolean expanded,
                                                                  boolean isLeaf, int row,
                                                                  boolean focused)
                    {
                        super.getTreeCellRendererComponent(
                                tree, value, selected, expanded, isLeaf, row, focused
                        );

                        if (isLeaf) {
                            TestWrapperNode testNode = (TestWrapperNode)value;
                            switch (testNode.getResult()) {
                            case PASSED:
                                setIcon(passedIcon);
                                break;
                            case FAILED:
                                setIcon(failedIcon);
                                break;
                            case NONE:
                            default:
                                setIcon(defaultIcon);
                            }
                        } else {
                            setIcon(null);
                        }

                        return this;
                    }
                }
        );

        tree.setShowsRootHandles(true);

        /*
         * Set up scroll pane.
         */
        scrollPane = new JScrollPane(tree);
        if (Globals.operatingSystem == Globals.OS.OSX) {
            Border border = new LineBorder(new Color(197, 197, 197));
            scrollPane.setBorder(border);
        }
    }

    /**
     * Replace the TestTree's model with the specified FileReport. This causes the
     * JTree's contents to be replaced by the state of the specified FileReport. All
     * future method calls on this TestTree will affect the specified FileReport.
     */
    public void update(FileReport report) {
        tree.setModel(report == null ? null : report.treeModel);
        tree.clearSelection();
    }

    private static String testGroupToString(TestGroup group) {
        Ceiling<Integer> maxNum = group.maxNum;
        Ceiling<Double> maxValue = group.maxValue;

        DecimalFormat fmt = new DecimalFormat("#.#");

        if (maxNum == Ceiling.ANY && maxValue == Ceiling.ANY) {
            return "fail any";
        } else if (maxNum != Ceiling.ANY && maxValue == Ceiling.ANY) {
            int max = ((AtMost<Integer>)maxNum).getValue();
            return "fail ≤ " + max;
        } else if (maxNum == Ceiling.ANY && maxValue != Ceiling.ANY) {
            double max = ((AtMost<Double>)maxValue).getValue();
            return "take ≤ " + fmt.format(max) + " points";
        } else {
            int maxN = ((AtMost<Integer>)maxNum).getValue();
            double maxV = ((AtMost<Double>)maxValue).getValue();

            return "fail ≤ " + maxN + " and take ≤ " + fmt.format(maxV) + " points";
        }
    }

    public void addTreeSelectionListener(TreeSelectionListener listener) {
        tree.addTreeSelectionListener(listener);
    }

    public TestWrapperNode getSelectedTestWrapperNode() {
        if (!hasSelection())
            return null;

        return (TestWrapperNode)tree.getLastSelectedPathComponent();
    }

    public boolean hasSelection() {
        return tree.getSelectionPath() != null;
    }

    /**
     * Set the result of the currently selected TestWrapperNode to TestResult.PASSED. If
     * there is no selection, this method does nothing.
     */
    public void passTest() {
        if (!hasSelection()) return;

        TestWrapperNode test = (TestWrapperNode)tree.getLastSelectedPathComponent();
        test.setResult(TestResult.PASSED);

        testResultChanged(tree.getSelectionPath());
    }

    /**
     * Set the result of the currently selected TestWrapperNode to TestResult.FAILED. If
     * there is no selection, this method does nothing.
     */
    public void failTest() {
        if (!hasSelection()) return;

        TestWrapperNode test = (TestWrapperNode)tree.getLastSelectedPathComponent();
        test.setResult(TestResult.FAILED);

        testResultChanged(tree.getSelectionPath());
    }

    /**
     * Resets the result of the currently selected TestWrapperNode to TestResult.NONE. If
     * there is no selection, this method does nothing.
     */
    public void resetTest() {
        if (!hasSelection()) return;

        TestWrapperNode test = (TestWrapperNode)tree.getLastSelectedPathComponent();
        test.setResult(TestResult.NONE);

        testResultChanged(tree.getSelectionPath());
    }

    /**
     * Returns true if the currently selected test is the last test for this file, or
     * false otherwise. This method returns false if there is no selection.
     */
    public boolean lastTestForFileSelected() {
        if (!hasSelection()) return false;
        return getRoot().getLastLeaf() == tree.getLastSelectedPathComponent();
    }

    /**
     * Returns true if the currently selected test is the first test for this file, or
     * false otherwise. This method returns false if there is no selection.
     */
    public boolean firstTestForFileSelected() {
        if (!hasSelection()) return false;
        return getRoot().getFirstLeaf() == tree.getLastSelectedPathComponent();
    }

    public void selectFirstTest() {
        DefaultMutableTreeNode root = getRoot();

        if (root == null) {
            // no file selected
            return;
        }

        DefaultMutableTreeNode first = root.getFirstLeaf();

        if (first == null) return;

        tree.setSelectionPath(new TreePath(first.getPath()));
    }

    /**
     * Sets the test tree's current selection to the next test for this file. If the
     * last test for this file is selected, this method does nothing. If no test is
     * selected, this method selects the first test.
     */
    public void goToNextTest() {
        if (!hasSelection()) {
            selectFirstTest();
            return;
        }

        if (lastTestForFileSelected())
            return;

        DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
        DefaultMutableTreeNode nextSibling = node.getNextSibling();

        if (nextSibling != null) {
            tree.setSelectionPath(new TreePath(nextSibling.getPath()));
            return;
        }

        // current selection is last test in this group
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();

        while (parent.getLastChild() == node) {
            node = parent;
            parent = (DefaultMutableTreeNode)parent.getParent();

            if (parent == null) return;
        }

        DefaultMutableTreeNode newRoot = (DefaultMutableTreeNode)parent.getChildAfter(node);
        tree.setSelectionPath(new TreePath(newRoot.getFirstLeaf().getPath()));
    }

    /**
     * Sets the test tree's current selection to the previous test for this file. If the
     * first test for this file is selected, this method does nothing. If no test is
     * selected, this method does nothing.
     */
    public void goToPreviousTest() {
        if (!hasSelection() || firstTestForFileSelected())
            return;

        DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
        DefaultMutableTreeNode previousSibling = node.getPreviousSibling();

        if (previousSibling != null) {
            tree.setSelectionPath(new TreePath(previousSibling.getPath()));
            return;
        }

        // current selection is first test in this group
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();

        while (parent.getFirstChild() == node) {
            node = parent;
            parent = (DefaultMutableTreeNode)parent.getParent();

            if (parent == null) return;
        }

        DefaultMutableTreeNode newRoot = (DefaultMutableTreeNode)parent.getChildBefore(node);
        tree.setSelectionPath(new TreePath(newRoot.getLastLeaf().getPath()));
    }

    /**
     * Important note: we indicate that a test result changed by using the
     * DefaultTreeModel's valueForPathChanged() method. This method takes the path to
     * the node that changed and the *user object* that is supposed to have changed.
     * Since we store the test result *outside* of the user object, and leave the
     * user object to store the reference to a Test or TestGroup object, we must pass
     * the user object in here, even though it really has not changed. This is the
     * simplest way I can think of to force the TreeModel to send a TreeNodesChanged
     * event to its listeners.
     */
    private void testResultChanged(TreePath testPath) {
        TestWrapperNode node = (TestWrapperNode)testPath.getLastPathComponent();
        tree.getModel().valueForPathChanged(testPath, node.getUserObject());
    }

    private DefaultMutableTreeNode getRoot() {
        TreeModel model = tree.getModel();
        return (DefaultMutableTreeNode)(model == null ? null : model.getRoot());
    }
}

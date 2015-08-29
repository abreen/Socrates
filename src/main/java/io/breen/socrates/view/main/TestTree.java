package io.breen.socrates.view.main;

import io.breen.socrates.Globals;
import io.breen.socrates.immutable.test.Test;
import io.breen.socrates.immutable.test.TestGroup;
import io.breen.socrates.immutable.test.ceiling.AtMost;
import io.breen.socrates.immutable.test.ceiling.Ceiling;
import io.breen.socrates.model.ConstraintUpdater;
import io.breen.socrates.model.FileReport;
import io.breen.socrates.model.TestResult;
import io.breen.socrates.model.TestWrapperNode;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import java.util.Enumeration;

public class TestTree {

    private JPanel rootPanel;
    private JScrollPane scrollPane;
    private JTree tree;

    private ConstraintUpdater updater;

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

        tree.addMouseListener(
                new MouseListener() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        int row = tree.getRowForLocation(e.getX(), e.getY());
                        if (row == -1)
                            tree.clearSelection();
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {

                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {

                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {

                    }

                    @Override
                    public void mouseExited(MouseEvent e) {

                    }
                }
        );

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

                        if (value instanceof TestWrapperNode) {
                            TestWrapperNode testNode = (TestWrapperNode)value;
                            switch (testNode.getResult()) {
                            case PASSED:
                                setIcon(TestControls.ICON_PASSED);
                                break;
                            case FAILED:
                                setIcon(TestControls.ICON_FAILED);
                                break;
                            case NONE:
                            default:
                                setIcon(TestControls.ICON_NORESULT);
                            }

                            if (!selected) {
                                if (testNode.isConstrained())
                                    setForeground(Color.GRAY);
                                else
                                    setForeground(Color.BLACK);
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
        if (report == null) {
            updater = null;
            tree.setModel(null);
        } else {
            DefaultTreeModel treeModel = report.treeModel;
            updater = new ConstraintUpdater(treeModel);

            Enumeration<DefaultMutableTreeNode> dfs = getRoot(treeModel).depthFirstEnumeration();
            while (dfs.hasMoreElements()) {
                DefaultMutableTreeNode node = dfs.nextElement();
                if (node instanceof TestWrapperNode)
                    ((TestWrapperNode)node).addObserver(updater);
            }

            tree.setModel(treeModel);
        }
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

        getModel().nodeChanged(test);
    }

    /**
     * Set the result of the currently selected TestWrapperNode to TestResult.FAILED. If
     * there is no selection, this method does nothing.
     */
    public void failTest() {
        if (!hasSelection()) return;

        TestWrapperNode test = (TestWrapperNode)tree.getLastSelectedPathComponent();
        test.setResult(TestResult.FAILED);

        getModel().nodeChanged(test);
    }

    /**
     * Resets the result of the currently selected TestWrapperNode to TestResult.NONE. If
     * there is no selection, this method does nothing.
     */
    public void resetTest() {
        if (!hasSelection()) return;

        TestWrapperNode test = (TestWrapperNode)tree.getLastSelectedPathComponent();
        test.setResult(TestResult.NONE);

        getModel().nodeChanged(test);
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
            if (nextSibling instanceof TestWrapperNode) {
                tree.setSelectionPath(new TreePath(nextSibling.getPath()));
            } else {
                tree.setSelectionPath(new TreePath(nextSibling.getFirstLeaf().getPath()));
            }
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
            if (previousSibling instanceof TestWrapperNode) {
                tree.setSelectionPath(new TreePath(previousSibling.getPath()));
            } else {
                tree.setSelectionPath(new TreePath(previousSibling.getLastLeaf().getPath()));
            }
            return;
        }

        // current selection is first test in this group
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();

        while (parent.getFirstChild() == node) {
            node = parent;
            parent = (DefaultMutableTreeNode)parent.getParent();

            if (parent == null) return;
        }

        DefaultMutableTreeNode newRoot = (DefaultMutableTreeNode)parent.getChildBefore(
                node
        );
        tree.setSelectionPath(new TreePath(newRoot.getLastLeaf().getPath()));
    }

    private DefaultMutableTreeNode getRoot() {
        TreeModel model = tree.getModel();
        return (DefaultMutableTreeNode)(model == null ? null : model.getRoot());
    }

    private DefaultMutableTreeNode getRoot(TreeModel model) {
        return (DefaultMutableTreeNode)model.getRoot();
    }

    private DefaultTreeModel getModel() {
        return (DefaultTreeModel)tree.getModel();
    }
}

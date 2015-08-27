package io.breen.socrates.view.main;

import io.breen.socrates.Globals;
import io.breen.socrates.immutable.test.Test;
import io.breen.socrates.immutable.test.TestGroup;
import io.breen.socrates.immutable.test.ceiling.AtMost;
import io.breen.socrates.immutable.test.ceiling.Ceiling;
import io.breen.socrates.model.FileReport;
import io.breen.socrates.model.TestGroupNode;
import io.breen.socrates.model.TestNode;
import io.breen.socrates.model.TestResult;
import io.breen.socrates.view.icon.DefaultTestIcon;
import io.breen.socrates.view.icon.FailedTestIcon;
import io.breen.socrates.view.icon.PassedTestIcon;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.text.DecimalFormat;

public class TestTree {

    private JPanel rootPanel;
    private JScrollPane scrollPane;
    private JTree tree;

    private TreeModelListener modelListener;

    private void createUIComponents() {
        tree = new JTree((TreeModel)null) {
            @Override
            public String convertValueToText(Object value, boolean selected,
                                             boolean expanded, boolean leaf, int row,
                                             boolean hasFocus)
            {
                if (value instanceof TestGroupNode) {
                    TestGroup group = ((TestGroupNode)value).testGroup;
                    return testGroupToString(group);

                } else if (value instanceof TestNode) {
                    Test test = ((TestNode)value).test;
                    return test.description;
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
                        path -> path.getLastPathComponent() instanceof TestNode
                )
        );

        Icon defaultIcon = new DefaultTestIcon();
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
                            TestNode testNode = (TestNode)value;
                            switch (testNode.getResult()) {
                            case PASSED:
                                setIcon(new PassedTestIcon());
                                break;
                            case FAILED:
                                setIcon(new FailedTestIcon());
                                break;
                            case NONE:
                            default:
                                setIcon(new DefaultTestIcon());
                            }
                        } else {
                            setIcon(null);
                        }

                        return this;
                    }
                }
        );

        tree.setShowsRootHandles(true);

//        modelListener = new TreeModelListener() {
//            @Override
//            public void treeNodesChanged(TreeModelEvent e) {
//                // may happen when a TestNode's result value is changed
//                TreeNode changedNode = e.getChildren()[0]
//            }
//
//            @Override
//            public void treeNodesInserted(TreeModelEvent e) {
//                // should not happen
//            }
//
//            @Override
//            public void treeNodesRemoved(TreeModelEvent e) {
//                // should not happen
//            }
//
//            @Override
//            public void treeStructureChanged(TreeModelEvent e) {
//                // should not happen
//            }
//        };

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
        tree.setModel(report);
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

    public TestNode getSelectedTestNode() {
        if (!hasSelection())
            return null;

        return (TestNode)tree.getLastSelectedPathComponent();
    }

    public boolean hasSelection() {
        return tree.getSelectionPath() != null;
    }

    /**
     * Set the result of the currently selected TestNode to TestResult.PASSED. If
     * there is no selection, this method does nothing.
     */
    public void passTest() {
        TreePath path = tree.getSelectionPath();
        if (path == null) return;

        FileReport report = (FileReport)tree.getModel();
        report.valueForPathChanged(path, TestResult.PASSED);
    }

    /**
     * Set the result of the currently selected TestNode to TestResult.FAILED. If
     * there is no selection, this method does nothing.
     */
    public void failTest() {
        TreePath path = tree.getSelectionPath();
        if (path == null) return;

        FileReport report = (FileReport)tree.getModel();
        report.valueForPathChanged(path, TestResult.FAILED);
    }

    /**
     * Resets the result of the currently selected TestNode to TestResult.NONE. If
     * there is no selection, this method does nothing.
     */
    public void resetTest() {
        TreePath path = tree.getSelectionPath();
        if (path == null) return;

        FileReport report = (FileReport)tree.getModel();
        report.valueForPathChanged(path, TestResult.NONE);
    }
}

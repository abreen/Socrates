package io.breen.socrates.model;

import io.breen.socrates.immutable.test.Test;
import io.breen.socrates.immutable.test.TestGroup;
import io.breen.socrates.immutable.test.ceiling.AtMost;
import io.breen.socrates.immutable.test.ceiling.Ceiling;
import io.breen.socrates.util.ObservableChangedEvent;
import io.breen.socrates.util.Observer;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.*;

/**
 * Instances of this class can be added as observers to a tree of TestWrapperNodes, and will update
 * the constraints of the other nodes in the tree when test results change.
 *
 * If a TestWrapperNode is marked as "constrained", this means that one or more TestGroupWrapperNode
 * ancestors of that node specified a ceiling (for the maximum number of tests allowed to fail or
 * the maximum point value allowed to be taken) and that ceiling was reached.
 */
public class ConstraintUpdater implements Observer<TestWrapperNode> {

    public final DefaultTreeModel treeModel;

    public ConstraintUpdater(DefaultTreeModel treeModel) {
        this.treeModel = treeModel;
    }

    private static TestGroupWrapperNode getParent(DefaultMutableTreeNode node) {
        return (TestGroupWrapperNode)node.getParent();
    }

    private static boolean mustUpdateTree(TestResult oldResult, TestResult newResult) {
        return ((oldResult == TestResult.PASSED || oldResult == TestResult.NONE) && newResult ==
                TestResult.FAILED) || (oldResult == TestResult.FAILED && (newResult == TestResult
                .PASSED || newResult == TestResult.NONE));
    }

    public void objectChanged(ObservableChangedEvent<TestWrapperNode> eventObj) {
        if (!(eventObj instanceof ResultChangedEvent)) return;

        ResultChangedEvent event = (ResultChangedEvent)eventObj;

        if (!mustUpdateTree(event.oldResult, event.newResult)) return;

        TestGroupWrapperNode parent = getParent(event.source);
        Test test = (Test)event.source.getUserObject();
        TestResult result = event.newResult;

        switch (result) {
        case FAILED:
            // test was just failed from being none or passed
            updateRelevantSubtrees(parent, 1, test.deduction);
            break;
        case PASSED:
        case NONE:
            // test was just passed or reset from being failed
            updateRelevantSubtrees(parent, -1, -test.deduction);
        }
    }

    private void updateRelevantSubtrees(TestGroupWrapperNode parent, int deltaFailed,
                                        double deltaPoints)
    {
        Deque<TestGroupWrapperNode> parents = new LinkedList<>();

        while (parent != null) {
            parents.addFirst(parent);
            parent = getParent(parent);
        }

        /*
         * Starting from the root of this tree, visit each TestGroupWrapperNode root
         * of a subtree that was just modified. As soon as we find a root of a subtree
         * that should be constrained due to a new change, we constrain the entire subtree
         * and stop. If we find a root of a subtree that becomes un-constrained due to a
         * new change, we un-constrain the entire subtree and add that subtree to list
         * of trees we must check for constraints that may need to be applied that are
         * not relevant to this change.
         */
        List<TestGroupWrapperNode> needsCheck = new LinkedList<>();

        for (TestGroupWrapperNode root : parents) {
            TestGroup group = (TestGroup)root.getUserObject();
            Ceiling<Integer> maxNum = group.maxNum;
            Ceiling<Double> maxValue = group.maxValue;
            boolean subtreeShouldBeConstrained = false;
            boolean subtreeShouldBeUnConstrained = false;

            if (maxNum != Ceiling.ANY) {
                int maxN = ((AtMost<Integer>)maxNum).getValue();
                boolean alreadyConstrained = root.getNumFailed() >= maxN;
                if (alreadyConstrained && root.getNumFailed() + deltaFailed < maxN) {
                    // this subtree must be un-constrained due to a new change in number
                    subtreeShouldBeUnConstrained = true;
                } else if (root.getNumFailed() + deltaFailed >= maxN) {
                    // this subtree must be constrained due to a new change in number
                    subtreeShouldBeConstrained = true;
                }
            }

            if (maxValue != Ceiling.ANY) {
                double maxV = ((AtMost<Double>)maxValue).getValue();
                boolean alreadyConstrained = root.getPointsTaken() >= maxV;
                if (alreadyConstrained && root.getPointsTaken() + deltaPoints < maxV) {
                    // this subtree must be un-constrained due to a new change in points
                    subtreeShouldBeUnConstrained |= true;
                } else if (root.getPointsTaken() + deltaPoints >= maxV) {
                    // this subtree must be constrained due to a new change in points
                    subtreeShouldBeConstrained |= true;
                }
            }

            root.updateNumFailed(deltaFailed);
            root.updatedPointsTaken(deltaPoints);

            if (subtreeShouldBeConstrained) {
                constrainTree(root);
                break;
            } else if (subtreeShouldBeUnConstrained) {
                unconstrainTree(root);

                Enumeration<DefaultMutableTreeNode> children = root.children();
                while (children.hasMoreElements()) {
                    DefaultMutableTreeNode child = children.nextElement();
                    if (child instanceof TestGroupWrapperNode && !parents.contains(child))
                        needsCheck.add((TestGroupWrapperNode)child);
                }
            }
        }

        /*
         * We now must check trees that have just become un-constrained due to a change.
         * These trees' leaves were marked un-constrained because an ancestor in the
         * tree became un-constrained. However, an old constraint on these roots may
         * still be in effect. If they are, we will re-constrain the leaves of these
         * trees.
         */
        while (true) {
            List<TestGroupWrapperNode> checkAfter = new LinkedList<>();

            for (TestGroupWrapperNode root : needsCheck) {
                TestGroup group = (TestGroup)root.getUserObject();
                Ceiling<Integer> maxNum = group.maxNum;
                Ceiling<Double> maxValue = group.maxValue;
                boolean needsReConstraining = false;

                if (maxNum != Ceiling.ANY) {
                    int maxN = ((AtMost<Integer>)maxNum).getValue();
                    if (root.getNumFailed() >= maxN) {
                        needsReConstraining = true;
                    }
                }

                if (maxValue != Ceiling.ANY) {
                    double maxV = ((AtMost<Double>)maxValue).getValue();
                    if (root.getPointsTaken() >= maxV) {
                        needsReConstraining = true;
                    }
                }

                if (needsReConstraining) {
                    constrainTree(root);
                } else {
                    Enumeration<DefaultMutableTreeNode> children = root.children();
                    while (children.hasMoreElements()) {
                        DefaultMutableTreeNode child = children.nextElement();
                        if (child instanceof TestGroupWrapperNode)
                            checkAfter.add((TestGroupWrapperNode)child);
                    }
                }
            }

            if (checkAfter.isEmpty()) break;
            else needsCheck = checkAfter;
        }
    }

    private void constrainTree(TestGroupWrapperNode root) {
        Enumeration<DefaultMutableTreeNode> dfs = root.depthFirstEnumeration();
        while (dfs.hasMoreElements()) {
            DefaultMutableTreeNode dfsNode = dfs.nextElement();
            if (dfsNode instanceof TestGroupWrapperNode) continue;

            TestWrapperNode testNode = (TestWrapperNode)dfsNode;
            testNode.setConstrained(true);
            treeModel.nodeChanged(testNode);
        }
    }

    private void unconstrainTree(TestGroupWrapperNode root) {
        Enumeration<DefaultMutableTreeNode> dfs = root.depthFirstEnumeration();
        while (dfs.hasMoreElements()) {
            DefaultMutableTreeNode dfsNode = dfs.nextElement();
            if (dfsNode instanceof TestGroupWrapperNode) continue;

            TestWrapperNode testNode = (TestWrapperNode)dfsNode;
            testNode.setConstrained(false);
            treeModel.nodeChanged(testNode);
        }
    }
}

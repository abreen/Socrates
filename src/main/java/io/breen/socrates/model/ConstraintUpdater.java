package io.breen.socrates.model;

import io.breen.socrates.test.GroupNode;
import io.breen.socrates.test.TestNode;
import io.breen.socrates.util.ObservableChangedEvent;
import io.breen.socrates.util.Observer;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.*;
import java.util.logging.Logger;

/**
 * Instances of this class can be added as observers to a tree of TestWrapperNodes, and will update
 * the constraints of the other nodes in the tree when test results change.
 *
 * If a TestWrapperNode is marked as "constrained", this means that one or more TestGroupWrapperNode
 * ancestors of that node specified a ceiling (for the maximum number of tests allowed to fail or
 * the maximum point value allowed to be taken) and that ceiling was reached.
 */
public class ConstraintUpdater //implements Observer<> {
{
// TODO

//    private static Logger logger = Logger.getLogger(ConstraintUpdater.class.getName());
//
//    private final DefaultTreeModel treeModel;
//
//    public ConstraintUpdater(DefaultTreeModel treeModel) {
//        this.treeModel = treeModel;
//    }
//
//    private static TestGroupWrapperNode getParent(DefaultMutableTreeNode node) {
//        return (TestGroupWrapperNode)node.getParent();
//    }
//
//    private static boolean mustUpdateTree(TestResult oldResult, TestResult newResult) {
//        return ((oldResult == TestResult.PASSED || oldResult == TestResult.NONE) && newResult ==
//                TestResult.FAILED) || (oldResult == TestResult.FAILED && (newResult == TestResult
//                .PASSED || newResult == TestResult.NONE));
//    }
//
//    private static void logTree(DefaultMutableTreeNode root) {
//        logTree(root, "");
//    }
//
//    private static void logTree(DefaultMutableTreeNode root, String prefix) {
//        logger.fine(prefix + root);
//        Enumeration children = root.children();
//        while (children.hasMoreElements()) {
//            Object child = children.nextElement();
//            logTree((DefaultMutableTreeNode)child, prefix + "\t");
//        }
//    }
//
//    @Override
//    public void objectChanged(ObservableChangedEvent<TestWrapperNode> eventObj) {
//        if (!(eventObj instanceof ResultChangedEvent)) return;
//
//        ResultChangedEvent event = (ResultChangedEvent)eventObj;
//
//        if (!mustUpdateTree(event.oldResult, event.newResult)) return;
//
//        TestGroupWrapperNode parent = getParent(event.source);
//        TestNode test = (TestNode)event.source.getUserObject();
//        TestResult result = event.newResult;
//
//        Map<TestWrapperNode, Boolean> initialState = new HashMap<>();
//
//        switch (result) {
//        case FAILED:
//            // test was just failed from being none or passed
//            update(parent, 1, test.getDeduction(), initialState);
//            break;
//        case PASSED:
//        case NONE:
//            // test was just passed or reset from being failed
//            update(parent, -1, -test.getDeduction(), initialState);
//        }
//
//        for (Map.Entry<TestWrapperNode, Boolean> entry : initialState.entrySet()) {
//            TestWrapperNode node = entry.getKey();
//            boolean constrained = entry.getValue();
//
//            if (node.isConstrained() != constrained) treeModel.nodeChanged(node);
//        }
//    }
//
//    private void update(TestGroupWrapperNode parent, int deltaFailed, double deltaPoints,
//                        Map<TestWrapperNode, Boolean> initialState)
//    {
//        TestGroupWrapperNode root = (TestGroupWrapperNode)parent.getRoot();
//
//        parent.updatedPointsTaken(deltaPoints);
//
//        int numFailedBefore = parent.getNumFailed();
//        parent.updateNumFailed(deltaFailed);
//        int numFailedAfter = parent.getNumFailed();
//
//        parent = getParent(parent);
//
//        while (parent != null) {
//            parent.updatedPointsTaken(deltaPoints);
//
//            if (numFailedBefore == 0 && numFailedAfter > 0) {
//                numFailedBefore = parent.getNumFailed();
//                parent.updateNumFailed(1);
//                numFailedAfter = parent.getNumFailed();
//            } else if (numFailedBefore > 0 && numFailedAfter == 0) {
//                numFailedBefore = parent.getNumFailed();
//                parent.updateNumFailed(-1);
//                numFailedAfter = parent.getNumFailed();
//            }
//
//            parent = getParent(parent);
//        }
//
//        reset(root, initialState);
//    }
//
//    private void reset(TestGroupWrapperNode root, Map<TestWrapperNode, Boolean> initialState) {
//        unconstrainSubtree(root, initialState);
//
//        Stack<TestGroupWrapperNode> stack = new Stack<>();
//
//        Enumeration<DefaultMutableTreeNode> bfs = root.breadthFirstEnumeration();
//        while (bfs.hasMoreElements()) {
//            DefaultMutableTreeNode dmtn = bfs.nextElement();
//            if (dmtn instanceof TestGroupWrapperNode) stack.push((TestGroupWrapperNode)dmtn);
//        }
//
//        for (TestGroupWrapperNode node : stack) {
//            GroupNode group = (GroupNode)node.getUserObject();
//
//            if (group.getMaxValue() > 0 && node.getPointsTaken() >= group.getMaxValue()) {
//                constrainSubtree(node, initialState);
//                continue;
//            }
//
//            if (group.getMaxNum() > 0 && node.getNumFailed() >= group.getMaxNum()) {
//                Enumeration<DefaultMutableTreeNode> children = node.children();
//                while (children.hasMoreElements()) {
//                    DefaultMutableTreeNode child = children.nextElement();
//                    if (child instanceof TestWrapperNode) {
//                        TestWrapperNode c = (TestWrapperNode)child;
//                        c.setConstrained(true);
//
//                    } else if (child instanceof TestGroupWrapperNode) {
//                        TestGroupWrapperNode c = (TestGroupWrapperNode)child;
//                        if (c.getNumFailed() == 0) constrainSubtree(c, initialState);
//
//                    }
//                }
//            }
//        }
//    }
//
//    private void constrainSubtree(TestGroupWrapperNode root,
//                                  Map<TestWrapperNode, Boolean> initialState)
//    {
//        logger.fine("constraining tree with root: " + root);
//
//        @SuppressWarnings("unchecked") Enumeration<DefaultMutableTreeNode> dfs = root
//                .depthFirstEnumeration();
//        while (dfs.hasMoreElements()) {
//            DefaultMutableTreeNode dfsNode = dfs.nextElement();
//            if (dfsNode instanceof TestGroupWrapperNode) continue;
//
//            TestWrapperNode testNode = (TestWrapperNode)dfsNode;
//
//            if (!initialState.containsKey(testNode))
//                initialState.put(testNode, testNode.isConstrained());
//
//            testNode.setConstrained(true);
//        }
//    }
//
//    private void unconstrainSubtree(TestGroupWrapperNode root,
//                                    Map<TestWrapperNode, Boolean> initialState)
//    {
//        logger.fine("un-constraining tree with root: " + root);
//
//        @SuppressWarnings("unchecked") Enumeration<DefaultMutableTreeNode> dfs = root
//                .depthFirstEnumeration();
//        while (dfs.hasMoreElements()) {
//            DefaultMutableTreeNode dfsNode = dfs.nextElement();
//            if (dfsNode instanceof TestGroupWrapperNode) continue;
//
//            TestWrapperNode testNode = (TestWrapperNode)dfsNode;
//
//            if (!initialState.containsKey(testNode))
//                initialState.put(testNode, testNode.isConstrained());
//
//            testNode.setConstrained(false);
//        }
//    }
}

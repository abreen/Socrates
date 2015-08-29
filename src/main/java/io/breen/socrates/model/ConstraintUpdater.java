package io.breen.socrates.model;

import io.breen.socrates.immutable.test.Test;
import io.breen.socrates.util.ObservableChangedEvent;
import io.breen.socrates.util.Observer;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Instances of this class can be added as observers to a tree of TestWrapperNodes, and
 * will update the constraints of the other nodes in the tree when test results change.
 *
 * If a TestWrapperNode is marked as "constrained", this means that one or more
 * TestGroupWrapperNode ancestors of that node specified a ceiling (for the maximum number
 * of tests allowed to fail or the maximum point value allowed to be taken) and that
 * ceiling was reached.
 */
public class ConstraintUpdater implements Observer<TestWrapperNode> {

    public void objectChanged(ObservableChangedEvent<TestWrapperNode> eventObj) {
        if (!(eventObj instanceof TestWrapperNode.ResultChangedEvent)) return;

        TestWrapperNode.ResultChangedEvent event = (TestWrapperNode.ResultChangedEvent)
                eventObj;

        if (!mustUpdateTree(event.oldResult, event.newResult)) return;

        TestGroupWrapperNode parent = getParent(event.source);
        Test test = (Test)event.source.getUserObject();
        TestResult result = event.newResult;

        switch (result) {
        case FAILED:
            // test was just failed from being none or passed
            updatePathToRoot(parent, 1, test.deduction);
        case PASSED:
        case NONE:
            // test was just passed or reset from being failed
            updatePathToRoot(parent, -1, -test.deduction);
        }
    }

    private static void updatePathToRoot(TestGroupWrapperNode parent, int deltaFailed,
                                         double deltaPoints)
    {
        while (parent != null) {
            parent.updateNumFailed(deltaFailed);
            parent.updatedPointsTaken(deltaPoints);
            parent = getParent(parent);
        }
    }

    private static TestGroupWrapperNode getParent(DefaultMutableTreeNode node) {
        return (TestGroupWrapperNode)node.getParent();
    }

    private static boolean mustUpdateTree(TestResult oldResult, TestResult newResult) {
        return ((oldResult == TestResult.PASSED || oldResult == TestResult.NONE) &&
                newResult == TestResult.FAILED) || (oldResult == TestResult.FAILED &&
                (newResult == TestResult.PASSED || newResult == TestResult.NONE));
    }
}

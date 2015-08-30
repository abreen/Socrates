package io.breen.socrates.model.wrapper;

import io.breen.socrates.immutable.test.TestGroup;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * This class "wraps" an immutable TestGroup (an interior node in the immutable tree that starts in
 * a File object). It is created when the immutable tree is traversed to create a stateful tree
 * holding the outcome of tests for a particular submission.
 *
 * @see TestWrapperNode
 */
public class TestGroupWrapperNode extends DefaultMutableTreeNode {

    protected int numFailed;
    protected double pointsTaken;

    public TestGroupWrapperNode(TestGroup testGroup) {
        super(testGroup);
        numFailed = 0;
        pointsTaken = 0.0;
    }

    @Override
    public String toString() {
        return "TestGroupWrapperNode(" +
                //"userObject=" + userObject + ", " +
                "maxNum=" + ((TestGroup)userObject).maxNum + ", " +
                "maxValue=" + ((TestGroup)userObject).maxValue + ", " +
                "numFailed=" + numFailed + ", " +
                "pointsTaken=" + pointsTaken + ")";
    }

    public void updateNumFailed(int delta) {
        numFailed += delta;
    }

    public void updatedPointsTaken(double delta) {
        pointsTaken += delta;
    }

    public int getNumFailed() {
        return numFailed;
    }

    public double getPointsTaken() {
        return pointsTaken;
    }
}

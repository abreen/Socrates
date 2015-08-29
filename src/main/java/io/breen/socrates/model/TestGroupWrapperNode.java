package io.breen.socrates.model;

import io.breen.socrates.immutable.test.TestGroup;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * This class "wraps" an immutable TestGroup (an interior node in the immutable tree that
 * starts in a File object). It is created when the immutable tree is traversed to create
 * a stateful tree holding the outcome of tests for a particular submission.
 *
 * @see io.breen.socrates.model.TestWrapperNode
 */
public class TestGroupWrapperNode extends DefaultMutableTreeNode {

    public TestGroupWrapperNode(TestGroup testGroup) {
        super(testGroup);
    }
}

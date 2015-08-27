package io.breen.socrates.model;

import io.breen.socrates.immutable.test.TestGroup;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 */
public class TestGroupWrapperNode extends DefaultMutableTreeNode {

    public TestGroupWrapperNode(TestGroup testGroup) {
        super(testGroup);
    }
}

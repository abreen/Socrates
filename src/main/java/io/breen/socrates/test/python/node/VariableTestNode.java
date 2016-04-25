package io.breen.socrates.test.python.node;

import io.breen.socrates.test.TestNode;

public abstract class VariableTestNode extends TestNode {

    /**
     * This empty constructor is used by SnakeYAML for the extenders of this class that are
     * instantiated from a criteria file.
     */
    public VariableTestNode() {}

    public VariableTestNode(double deduction, String description) {
        super(deduction, description);
    }
}

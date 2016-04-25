package io.breen.socrates.test.python.node;

import io.breen.socrates.test.TestNode;

public abstract class FunctionTestNode extends TestNode {

    /**
     * This empty constructor is used by SnakeYAML for the extenders of this class that are
     * instantiated from a criteria file.
     */
    public FunctionTestNode() {}

    public FunctionTestNode(double deduction, String description) {
        super(deduction, description);
    }
}

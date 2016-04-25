package io.breen.socrates.test.python.node;


public abstract class MethodTestNode extends FunctionTestNode {

    /**
     * This empty constructor is used by SnakeYAML for the extenders of this class that are
     * instantiated from a criteria file.
     */
    public MethodTestNode() {}

    public MethodTestNode(double deduction, String description) {
        super(deduction, description);
    }
}

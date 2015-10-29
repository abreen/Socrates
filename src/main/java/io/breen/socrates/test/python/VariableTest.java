package io.breen.socrates.test.python;

import io.breen.socrates.test.Test;

public abstract class VariableTest extends Test {

    /**
     * This empty constructor is used by SnakeYAML for the extenders of this class that are
     * instantiated from a criteria file.
     */
    public VariableTest() {}

    public VariableTest(double deduction, String description) {
        super(deduction, description);
    }
}

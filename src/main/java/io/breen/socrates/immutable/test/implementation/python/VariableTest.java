package io.breen.socrates.immutable.test.implementation.python;

import io.breen.socrates.immutable.test.Test;

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

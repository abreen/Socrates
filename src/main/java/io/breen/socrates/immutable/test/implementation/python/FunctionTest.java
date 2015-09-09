package io.breen.socrates.immutable.test.implementation.python;

import io.breen.socrates.immutable.test.Test;

public abstract class FunctionTest extends Test {

    /**
     * This empty constructor is used by SnakeYAML for the extenders of this class that are
     * instantiated from a criteria file.
     */
    public FunctionTest() {}

    public FunctionTest(double deduction, String description) {
        super(deduction, description);
    }
}

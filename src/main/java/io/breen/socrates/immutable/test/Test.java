package io.breen.socrates.immutable.test;

/**
 * The class representing a single test that can be run on a file.
 */
public abstract class Test {

    /**
     * Runs the test. Returns a deduction if the fails and null if the test
     * passes.
     */
    public abstract Deduction run();

    public abstract Deduction getDeduction();
}

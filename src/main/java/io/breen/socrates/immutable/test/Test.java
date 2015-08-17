package io.breen.socrates.immutable.test;

/**
 * The class representing a single test that can be run on a file.
 */
public abstract class Test {

    protected final double deduction;

    public Test(double deduction) {
        this.deduction = deduction;
    }

    public String toString() {
        return "Test(deduction=" + deduction + ")";
    }
}

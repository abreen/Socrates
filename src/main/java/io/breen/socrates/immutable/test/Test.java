package io.breen.socrates.immutable.test;

/**
 * The class representing a single test that can be run on a file.
 */
public abstract class Test {

    protected final String description;

    protected final double deduction;

    public Test(String description, double deduction) {
        this.description = description;
        this.deduction = deduction;
    }

    public String toString() {
        return this.getClass().toString() + "(" +
                "description=" + description + ", " +
                "deduction=" + deduction +
                ")";
    }
}

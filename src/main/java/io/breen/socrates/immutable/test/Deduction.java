package io.breen.socrates.immutable.test;

/**
 * Represents a deduction resulting from any test (or group of tests) that
 * are failed because the student submission is not correct.
 */
public class Deduction {

    /**
     * The number of points to deduct from the student's score.
     */
    private final double value;

    /**
     * A string describing the cause of the deduction. The description should
     * use present tense to describe what is wrong that caused a test to fail,
     * or past tense to describe what went wrong during a test that failed.
     *
     * For example: "does not explain why the function returns 2" or
     * "when tested with an input value of -1, the function did not throw an
     * exception".
     */
    private final String description;

    public Deduction(String description, double value) {
        this.value = value;
        this.description = description;
    }
}

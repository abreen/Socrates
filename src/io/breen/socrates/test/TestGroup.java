package io.breen.socrates.test;

import java.util.List;

/**
 * Represents a sequence of one or more tests that should be run together.
 */
public class TestGroup {

    protected final List<Test> members;

    protected final int maxNum;

    protected final int maxValue;

    public TestGroup(List<Test> members, int maxNum, int maxValue) {
        if (maxNum < 0) {
            throw new IllegalArgumentException("max number of tests in group cannot " +
                                                   "be negative");
        }

        if (maxNum == 0) {
            throw new IllegalArgumentException("max number of tests cannot be zero");
        }

        this.members = members;
        this.maxNum = maxNum;
        this.maxValue = maxValue;
    }

    public List<Deduction> runTests() {
        return null;
    }

    public List<Deduction> getPossibleDeductions() {
        return null;
    }
}
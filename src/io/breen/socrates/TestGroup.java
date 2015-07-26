package io.breen.socrates;

import java.util.List;

/**
 * Represents a sequence of one or more tests that should be run together.
 */
public abstract class TestGroup {
    public abstract List<Deduction> runTests();

    public abstract List<Deduction> getPossibleDeductions();
}
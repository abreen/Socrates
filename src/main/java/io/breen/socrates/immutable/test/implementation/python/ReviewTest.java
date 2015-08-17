package io.breen.socrates.immutable.test.implementation.python;

import io.breen.socrates.immutable.test.Test;

public class ReviewTest extends Test {

    protected final String description;

    public ReviewTest(double deduction, String description) {
        super(deduction);
        this.description = description;
    }

    @Override
    public String toString() {
        return "PythonFile:ReviewTest(deduction=" + deduction + ", description=" + description + ")";
    }
}

package io.breen.socrates.immutable.test.implementation.plain;

import io.breen.socrates.immutable.test.Test;

public class ReviewTest extends Test {

    protected final String description;

    public ReviewTest(double deduction, String description) {
        super(deduction);
        this.description = description;
    }

    @Override
    public String toString() {
        return "PlainFile:ReviewTest(deduction=" + deduction + ", description=" + description + ")";
    }
}

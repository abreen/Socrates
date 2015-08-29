package io.breen.socrates.immutable.test.implementation.plain;

import io.breen.socrates.immutable.test.Test;

public class ReviewTest extends Test {

    public ReviewTest(double deduction, String description) {
        super(deduction, description);
    }

    @Override
    public String toString() {
        return "PlainFile:ReviewTest(deduction=" + deduction + ", description=" + description + ")";
    }

    @Override
    public String getTestTypeName() {
        return "review test";
    }
}

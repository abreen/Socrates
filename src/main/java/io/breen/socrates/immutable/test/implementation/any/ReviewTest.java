package io.breen.socrates.immutable.test.implementation.any;

import io.breen.socrates.immutable.test.Test;

public class ReviewTest extends Test {

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public ReviewTest() {}

    public ReviewTest(double deduction, String description) {
        super(deduction, description);
    }

    @Override
    public String toString() {
        return "ReviewTest(deduction=" + deduction + ", description=" + description + ")";
    }

    @Override
    public String getTestTypeName() {
        return "review test";
    }
}
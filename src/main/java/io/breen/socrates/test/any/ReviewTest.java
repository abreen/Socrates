package io.breen.socrates.test.any;

import io.breen.socrates.test.Test;

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

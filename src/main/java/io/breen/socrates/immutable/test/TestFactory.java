package io.breen.socrates.immutable.test;

import java.util.Map;

public class TestFactory {
    public static Test buildTest(TestType type, String description, double deduction, Map map) {
        switch (type) {
        case REVIEW_PLAIN:
            return new io.breen.socrates.immutable.test.plain.ReviewTest(description, deduction);
        case REVIEW_PYTHON:
            return new io.breen.socrates.immutable.test.python.ReviewTest(description, deduction);
        default:
            return null;
        }
    }
}

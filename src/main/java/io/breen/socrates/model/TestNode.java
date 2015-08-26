package io.breen.socrates.model;

import io.breen.socrates.immutable.test.Test;

public class TestNode {

    public final Test test;
    public TestResult result;

    public TestNode(Test test) {
        this.test = test;
        result = TestResult.NONE;
    }
}

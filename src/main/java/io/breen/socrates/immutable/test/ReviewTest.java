package io.breen.socrates.immutable.test;

public class ReviewTest extends Test {

    protected final java.io.File file;

    public ReviewTest(java.io.File file, String description, double deduction) {
        super(description, deduction);
        this.file = file;
    }
}

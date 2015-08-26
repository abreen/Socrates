package io.breen.socrates.model;

import io.breen.socrates.immutable.test.Test;

import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

public class TestNode {

    public final Test test;

    /**
     * The result obtained by running the test on the submission. If this test is
     * not automatable, this is determined by the human grader.
     */
    private TestResult result;

    /**
     * If this string is non-null, a human grader added a note when the test was
     * performed.
     */
    private Document notes;

    public TestNode(Test test) {
        this.test = test;
        result = TestResult.NONE;
        notes = new PlainDocument();
    }

    public TestResult getResult() {
        return result;
    }

    public void setResult(TestResult result) {
        this.result = result;
    }

    public Document getNotesDocument() {
        return notes;
    }
}

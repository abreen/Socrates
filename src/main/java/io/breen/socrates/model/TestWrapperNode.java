package io.breen.socrates.model;

import io.breen.socrates.immutable.test.Test;

import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 */
public class TestWrapperNode extends DefaultMutableTreeNode {

    protected TestResult result;
    protected final Document notes;

    public TestWrapperNode(Test test) {
        super(test);
        this.result = TestResult.NONE;
        this.notes = new PlainDocument();
    }

    public Document getNotesDocument() {
        return notes;
    }

    public TestResult getResult() {
        return result;
    }

    public void setResult(TestResult result) {
        this.result = result;
    }
}

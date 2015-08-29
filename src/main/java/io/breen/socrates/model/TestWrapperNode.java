package io.breen.socrates.model;

import io.breen.socrates.immutable.test.Test;
import io.breen.socrates.util.Observable;
import io.breen.socrates.util.Observer;

import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * This class "wraps" an immutable Test (a leaf node in the immutable tree that starts in
 * a File object) and contains data relevant to the outcome of the wrapped test on a
 * particular submission.
 *
 * @see io.breen.socrates.model.TestGroupWrapperNode
 */
public class TestWrapperNode extends DefaultMutableTreeNode
        implements Observable<TestResult>
{

    protected TestResult result;
    protected final Document notes;
    private Observer<TestResult> currentObserver;

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
        currentObserver.objectChanged(result);
    }

    public void setObserver(Observer<TestResult> observer) {
        currentObserver = observer;
    }

    public void resetObserver() {
        currentObserver = null;
    }
}

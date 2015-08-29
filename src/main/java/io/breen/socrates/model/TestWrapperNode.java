package io.breen.socrates.model;

import io.breen.socrates.immutable.test.Test;
import io.breen.socrates.util.Observable;
import io.breen.socrates.util.Observer;

import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.LinkedList;
import java.util.List;

/**
 * This class "wraps" an immutable Test (a leaf node in the immutable tree that starts in
 * a File object) and contains data relevant to the outcome of the wrapped test on a
 * particular submission.
 *
 * @see io.breen.socrates.model.TestGroupWrapperNode
 */
public class TestWrapperNode extends DefaultMutableTreeNode
        implements Observable<TestWrapperNode>
{

    protected final Document notes;
    protected final List<Observer<TestWrapperNode>> observers;
    protected TestResult result;
    protected boolean constrained;

    public TestWrapperNode(Test test) {
        super(test);
        notes = new PlainDocument();
        observers = new LinkedList<>();
        result = TestResult.NONE;
        constrained = false;
    }

    public Document getNotesDocument() {
        return notes;
    }

    public TestResult getResult() {
        return result;
    }

    public void setResult(TestResult result) {
        this.result = result;
        observers.forEach(o -> o.objectChanged(this));
    }

    public boolean isConstrained() {
        return constrained;
    }

    public void setConstrained(boolean constrained) {
        this.constrained = constrained;
        observers.forEach(o -> o.objectChanged(this));
    }

    public void addObserver(Observer<TestWrapperNode> observer) {
        observers.add(observer);
    }

    public void removeObserver(Observer<TestWrapperNode> observer) {
        observers.remove(observer);
    }
}

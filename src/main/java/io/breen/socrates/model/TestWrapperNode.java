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

    public final Document notes;
    protected final List<Observer<TestWrapperNode>> observers;
    protected TestResult result;
    protected boolean constrained;
    protected AutomationStage stage;

    public TestWrapperNode(Test test) {
        super(test);
        notes = new PlainDocument();
        observers = new LinkedList<>();
        result = TestResult.NONE;
        constrained = false;
        stage = AutomationStage.NONE;       // only used if the test is automatable
    }

    public TestResult getResult() {
        return result;
    }

    public void setResult(TestResult result) {
        if (result == this.result) return;
        TestResult oldResult = this.result;
        this.result = result;
        ResultChangedEvent e = new ResultChangedEvent(this, oldResult, result);
        observers.forEach(o -> o.objectChanged(e));
    }

    public boolean isConstrained() {
        return constrained;
    }

    public void setConstrained(boolean constrained) {
        if (constrained == this.constrained) return;
        this.constrained = constrained;
        observers.forEach(
                o -> o.objectChanged(new ConstraintChangedEvent(this, constrained))
        );
    }

    public AutomationStage getAutomationStage() {
        return stage;
    }

    public void setAutomationStage(AutomationStage stage) {
        if (stage == this.stage) return;
        AutomationStage oldStage = this.stage;
        this.stage = stage;
        StageChangedEvent e = new StageChangedEvent(this, oldStage, stage);
        observers.forEach(o -> o.objectChanged(e));
    }

    @Override
    public void addObserver(Observer<TestWrapperNode> observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer<TestWrapperNode> observer) {
        observers.remove(observer);
    }
}

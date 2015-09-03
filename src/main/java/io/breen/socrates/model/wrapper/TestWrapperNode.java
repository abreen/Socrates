package io.breen.socrates.model.wrapper;

import io.breen.socrates.immutable.test.Test;
import io.breen.socrates.model.AutomationStage;
import io.breen.socrates.model.TestResult;
import io.breen.socrates.model.event.*;
import io.breen.socrates.util.Observable;
import io.breen.socrates.util.Observer;

import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.LinkedList;
import java.util.List;

/**
 * This class "wraps" an immutable Test (a leaf node in the immutable tree that starts in a File
 * object) and contains data relevant to the outcome of the wrapped test on a particular
 * submission.
 *
 * @see TestGroupWrapperNode
 */
public class TestWrapperNode extends DefaultMutableTreeNode implements Observable<TestWrapperNode> {

    /**
     * The Document object that the GUI uses to maintain the user's notes about this test. The
     * contents of this document, if nonempty, will be used in the grade file.
     */
    public final Document notes;
    protected final List<Observer<TestWrapperNode>> observers;
    /**
     * This node's current test result. This might be changed by a GUI thread, or, if the wrapped
     * test is an automated test, a thread running the test.
     */
    protected TestResult result;
    /**
     * Whether this node is constrained. A value of true for this field means that the test's result
     * may not be changed by a user. This is maintained by a ConstraintUpdater object, which
     * observes all TestWrapperNode objects in a given tree.
     *
     * @see io.breen.socrates.model.ConstraintUpdater
     */
    protected boolean constrained;
    /**
     * If this node is an automatable node, this field stores the current "stage" of the test. When
     * the automated test starts on its own thread, this stage is set to STARTED. Then, depending on
     * its termination status, it is updated to FINISHED_NORMAL or FINISHED_ERROR.
     */
    protected AutomationStage stage;

    public TestWrapperNode(Test test) {
        super(test);

        result = TestResult.NONE;
        constrained = false;
        stage = AutomationStage.NONE;

        notes = new PlainDocument();

        observers = new LinkedList<>();
    }

    @Override
    public String toString() {
        return "TestWrapperNode(" +
                //"userObject=" + userObject + ", " +
                "result=" + result + ", " +
                "observers=" + observers + ", " +
                "constrained=" + constrained + ", " +
                "stage=" + stage + ")";
    }

    public TestResult getResult() {
        return result;
    }

    public void setResult(TestResult result) {
        if (result == this.result) return;
        TestResult oldResult = this.result;
        this.result = result;
        ResultChangedEvent e = new ResultChangedEvent(this, oldResult, result);
        for (Observer<TestWrapperNode> o : observers)
            o.objectChanged(e);
    }

    public boolean isConstrained() {
        return constrained;
    }

    public void setConstrained(boolean constrained) {
        if (constrained == this.constrained) return;
        this.constrained = constrained;
        ConstraintChangedEvent e = new ConstraintChangedEvent(this, constrained);
        for (Observer<TestWrapperNode> o : observers)
            o.objectChanged(e);
    }

    public AutomationStage getAutomationStage() {
        return stage;
    }

    public void setAutomationStage(AutomationStage stage) {
        if (stage == this.stage) return;
        AutomationStage oldStage = this.stage;
        this.stage = stage;
        StageChangedEvent e = new StageChangedEvent(this, oldStage, stage);
        for (Observer<TestWrapperNode> o : observers)
            o.objectChanged(e);
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

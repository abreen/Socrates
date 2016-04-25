package io.breen.socrates.test.python.node;

import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.python.PythonFile;
import io.breen.socrates.file.python.Variable;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.test.Automatable;
import io.breen.socrates.test.AutomationFailureException;
import io.breen.socrates.test.CannotBeAutomatedException;
import io.breen.socrates.test.python.PythonError;
import io.breen.socrates.test.python.PythonInspector;

import javax.swing.text.Document;
import java.io.IOException;
import java.nio.file.Path;

public class VariableEvalTestNode extends VariableTestNode implements Automatable<PythonFile> {

    /**
     * The expected value of the variable.
     */
    private Object value;

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public VariableEvalTestNode() {
    }

    public VariableEvalTestNode(Object expectedValue, double deduction, String description) {
        super(deduction, description);
        value = expectedValue;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        checkFrozen();
        this.value = value;
    }

    @Override
    public String toString() {
        return "VariableEvalTestNode(value=" + value + ")";
    }

    @Override
    public boolean shouldPass(PythonFile parent, SubmittedFile target, Path submissionDir,
                              Criteria criteria, Document transcript, Document notes)
            throws CannotBeAutomatedException, AutomationFailureException {
        Variable var = parent.getVariableForTest(this);
        if (var == null) throw new IllegalArgumentException();

        try {
            PythonInspector inspector = new PythonInspector(target.getPath());
            return inspector.variableEquals(var.name, value);
        } catch (IOException x) {
            throw new AutomationFailureException(x);
        } catch (PythonError x) {
            throw new CannotBeAutomatedException(
                    "Python error occurred evaluating variable: " + x
            );
        }
    }
}

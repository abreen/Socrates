package io.breen.socrates.test.python;

import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.python.PythonFile;
import io.breen.socrates.file.python.Variable;
import io.breen.socrates.submission.Submission;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.test.*;

import javax.swing.text.Document;
import java.io.IOException;

public class VariableEvalTest extends VariableTest implements Automatable<PythonFile> {

    /**
     * The expected value of the variable.
     */
    public Object value;

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public VariableEvalTest() {}

    public VariableEvalTest(Object expectedValue, double deduction, String description) {
        super(deduction, description);
        value = expectedValue;
    }

    @Override
    public String toString() {
        return "VariableEvalTest(value=" + value + ")";
    }

    @Override
    public String getTestTypeName() {
        return "variable evaluation";
    }

    @Override
    public boolean shouldPass(PythonFile parent, SubmittedFile target, Submission submission,
                              Criteria criteria, Document transcript, Document notes)
            throws CannotBeAutomatedException, AutomationFailureException
    {
        Variable var = parent.getVariableForTest(this);
        if (var == null) throw new IllegalArgumentException();

        try {
            PythonInspector inspector = new PythonInspector(target.fullPath);
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

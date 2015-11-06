package io.breen.socrates.test.python;

import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.python.PythonFile;
import io.breen.socrates.file.python.Variable;
import io.breen.socrates.submission.Submission;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.test.*;

import javax.swing.text.Document;
import java.io.IOException;

public class VariableExistsTest extends VariableTest implements Automatable<PythonFile> {

    private final Variable variable;

    public VariableExistsTest(Variable variable) {
        super(variable.pointValue, "variable '" + variable.name + "' is missing");
        this.variable = variable;
    }

    @Override
    public String toString() {
        return "VariableExistsTest(variable=" + variable + ")";
    }

    @Override
    public String getTestTypeName() {
        return "variable check";
    }

    @Override
    public boolean shouldPass(PythonFile parent, SubmittedFile target, Submission submission,
                              Criteria criteria, Document transcript, Document notes)
            throws CannotBeAutomatedException, AutomationFailureException
    {
        try {
            PythonInspector inspector = new PythonInspector(target.fullPath);
            return inspector.variableExists(variable.name);
        } catch (IOException x) {
            throw new AutomationFailureException(x);
        } catch (PythonError x) {
            throw new CannotBeAutomatedException(
                    "Python error occurred looking for variable: " + x
            );
        }
    }
}

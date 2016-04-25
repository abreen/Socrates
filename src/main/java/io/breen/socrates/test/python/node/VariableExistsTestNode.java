package io.breen.socrates.test.python.node;

import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.python.PythonFile;
import io.breen.socrates.file.python.Variable;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.test.*;
import io.breen.socrates.test.python.PythonError;
import io.breen.socrates.test.python.PythonInspector;

import javax.swing.text.Document;
import java.io.IOException;
import java.nio.file.Path;

public class VariableExistsTestNode extends VariableTestNode implements Automatable<PythonFile> {

    private final Variable variable;


    public VariableExistsTestNode(Variable variable) {
        super(variable.pointValue, "variable '" + variable.name + "' is missing");
        this.variable = variable;
    }

    @Override
    public String toString() {
        return "VariableExistsTestNode(variable=" + variable + ")";
    }

    @Override
    public boolean shouldPass(PythonFile parent, SubmittedFile target, Path submissionDir,
                              Criteria criteria, Document transcript, Document notes)
            throws CannotBeAutomatedException, AutomationFailureException
    {
        try {
            PythonInspector inspector = new PythonInspector(target.getPath());
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

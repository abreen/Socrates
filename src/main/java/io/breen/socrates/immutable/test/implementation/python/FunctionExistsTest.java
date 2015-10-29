package io.breen.socrates.immutable.test.implementation.python;

import io.breen.socrates.immutable.criteria.Criteria;
import io.breen.socrates.immutable.file.python.Function;
import io.breen.socrates.immutable.file.python.PythonFile;
import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.immutable.submission.SubmittedFile;
import io.breen.socrates.immutable.test.*;

import javax.swing.text.Document;
import java.io.IOException;

public class FunctionExistsTest extends FunctionTest implements Automatable<PythonFile> {

    private final Function function;

    public FunctionExistsTest(Function function) {
        super(function.pointValue, "function '" + function.name + "' is missing");
        this.function = function;
    }

    @Override
    public String toString() {
        return "FunctionExistsTest(function=" + function + ")";
    }

    @Override
    public String getTestTypeName() {
        return "function check";
    }

    @Override
    public boolean shouldPass(PythonFile parent, SubmittedFile target, Submission submission,
                              Criteria criteria, Document transcript)
            throws CannotBeAutomatedException, AutomationFailureException
    {
        try {
            PythonInspector inspector = new PythonInspector(target.fullPath);
            return inspector.variableExists(function.name);
        } catch (IOException x) {
            throw new AutomationFailureException(x);
        } catch (PythonError x) {
            throw new CannotBeAutomatedException(
                    "Python error occurred looking for function: " + x
            );
        }
    }
}

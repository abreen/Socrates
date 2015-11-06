package io.breen.socrates.test.python;

import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.python.Function;
import io.breen.socrates.file.python.PythonFile;
import io.breen.socrates.submission.Submission;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.test.*;

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
                              Criteria criteria, Document transcript, Document notes)
            throws CannotBeAutomatedException, AutomationFailureException
    {
        try {
            PythonInspector inspector = new PythonInspector(target.fullPath);
            return inspector.functionExists(function.name);
        } catch (IOException x) {
            throw new AutomationFailureException(x);
        } catch (PythonError x) {
            throw new CannotBeAutomatedException(
                    "Python error occurred looking for function: " + x
            );
        }
    }
}

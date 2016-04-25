package io.breen.socrates.test.python.node;

import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.python.Function;
import io.breen.socrates.file.python.PythonFile;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.test.*;
import io.breen.socrates.test.python.PythonError;
import io.breen.socrates.test.python.PythonInspector;

import javax.swing.text.Document;
import java.io.IOException;
import java.nio.file.Path;

public class FunctionExistsTestNode extends FunctionTestNode implements Automatable<PythonFile> {

    private final Function function;


    public FunctionExistsTestNode(Function function) {
        super(function.pointValue, "function '" + function.name + "' is missing");
        this.function = function;
    }

    @Override
    public String toString() {
        return "FunctionExistsTestNode(function=" + function + ")";
    }

    @Override
    public boolean shouldPass(PythonFile parent, SubmittedFile target, Path submissionDir,
                              Criteria criteria, Document transcript, Document notes)
            throws CannotBeAutomatedException, AutomationFailureException
    {
        try {
            PythonInspector inspector = new PythonInspector(target.getPath());
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

package io.breen.socrates.test.python.node;

import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.python.PythonClass;
import io.breen.socrates.file.python.*;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.test.*;
import io.breen.socrates.test.python.PythonError;
import io.breen.socrates.test.python.PythonInspector;

import javax.swing.text.Document;
import java.io.IOException;
import java.nio.file.Path;


public class MethodExistsTestNode extends MethodTestNode implements Automatable<PythonFile> {

    private final Method method;


    public MethodExistsTestNode(Method method) {
        super(method.pointValue, "method '" + method.name + "' is missing");
        this.method = method;
    }

    @Override
    public boolean shouldPass(PythonFile parent, SubmittedFile target, Path submissionDir,
                              Criteria criteria, Document transcript, Document notes)
            throws CannotBeAutomatedException, AutomationFailureException
    {
        // TODO transcript
        PythonClass klass = parent.getClassContainingMethod(method);
        if (klass == null) throw new IllegalArgumentException();

        try {
            PythonInspector inspector = new PythonInspector(target.getPath());
            return inspector.methodExists(klass.name, method.name);
        } catch (IOException x) {
            throw new AutomationFailureException(x);
        } catch (PythonError x) {
            throw new CannotBeAutomatedException(
                    "Python error occurred looking for method: " + x
            );
        }
    }
}

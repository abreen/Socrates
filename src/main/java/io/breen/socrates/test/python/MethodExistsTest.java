package io.breen.socrates.test.python;

import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.python.Class;
import io.breen.socrates.file.python.*;
import io.breen.socrates.submission.Submission;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.test.*;

import javax.swing.text.Document;
import java.io.IOException;


public class MethodExistsTest extends MethodTest implements Automatable<PythonFile> {

    private final Method method;

    public MethodExistsTest(Method method) {
        super(method.pointValue, "method '" + method.name + "' is missing");
        this.method = method;
    }

    @Override
    public boolean shouldPass(PythonFile parent, SubmittedFile target, Submission submission,
                              Criteria criteria, Document transcript, Document notes)
            throws CannotBeAutomatedException, AutomationFailureException
    {
        // TODO transcript
        Class klass = parent.getClassContainingMethod(method);
        if (klass == null) throw new IllegalArgumentException();

        try {
            PythonInspector inspector = new PythonInspector(target.fullPath);
            return inspector.methodExists(klass.name, method.name);
        } catch (IOException x) {
            throw new AutomationFailureException(x);
        } catch (PythonError x) {
            throw new CannotBeAutomatedException(
                    "Python error occurred looking for method: " + x
            );
        }
    }

    @Override
    public String getTestTypeName() {
        return "method check";
    }
}

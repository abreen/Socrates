package io.breen.socrates.test.python;

import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.python.Class;
import io.breen.socrates.file.python.PythonFile;
import io.breen.socrates.submission.Submission;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.test.*;

import javax.swing.text.Document;
import java.io.IOException;


public class ClassExistsTest extends Test implements Automatable<PythonFile> {

    private final Class klass;

    public ClassExistsTest(Class klass) {
        super(klass.pointValue, "class '" + klass.name + "' is missing");
        this.klass = klass;
    }

    @Override
    public boolean shouldPass(PythonFile parent, SubmittedFile target, Submission submission,
                              Criteria criteria, Document transcript, Document notes)
            throws CannotBeAutomatedException, AutomationFailureException
    {
        // TODO transcript
        try {
            PythonInspector inspector = new PythonInspector(target.fullPath);
            return inspector.classExists(klass.name);
        } catch (IOException x) {
            throw new AutomationFailureException(x);
        } catch (PythonError x) {
            throw new CannotBeAutomatedException(
                    "Python error occurred looking for class: " + x
            );
        }
    }

    @Override
    public String getTestTypeName() {
        return "class check";
    }
}

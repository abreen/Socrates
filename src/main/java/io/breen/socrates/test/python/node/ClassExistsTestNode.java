package io.breen.socrates.test.python.node;

import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.python.PythonClass;
import io.breen.socrates.file.python.PythonFile;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.test.*;
import io.breen.socrates.test.python.PythonError;
import io.breen.socrates.test.python.PythonInspector;

import javax.swing.text.Document;
import java.io.IOException;
import java.nio.file.Path;


public class ClassExistsTestNode extends TestNode implements Automatable<PythonFile> {

    private final PythonClass klass;


    public ClassExistsTestNode(PythonClass klass) {
        super(klass.pointValue, "class '" + klass.name + "' is missing");
        this.klass = klass;
    }

    @Override
    public boolean shouldPass(PythonFile parent, SubmittedFile target, Path submissionDir,
                              Criteria criteria, Document transcript, Document notes)
            throws CannotBeAutomatedException, AutomationFailureException
    {
        // TODO transcript
        try {
            PythonInspector inspector = new PythonInspector(target.getPath());
            return inspector.classExists(klass.name);
        } catch (IOException x) {
            throw new AutomationFailureException(x);
        } catch (PythonError x) {
            throw new CannotBeAutomatedException(
                    "Python error occurred looking for class: " + x
            );
        }
    }
}

package io.breen.socrates.immutable.test.python;

import io.breen.socrates.immutable.criteria.Criteria;
import io.breen.socrates.immutable.file.python.PythonFile;
import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.immutable.submission.SubmittedFile;
import io.breen.socrates.immutable.test.*;

import javax.swing.text.Document;
import java.io.IOException;

public class ImportTest extends Test implements Automatable<PythonFile> {

    public ImportTest(PythonFile file) {
        super(file.importFailureDeduction, "could not load '" + file.path + "'");
    }

    @Override
    public String toString() {
        return "ImportTest()";
    }

    @Override
    public String getTestTypeName() {
        return "import check";
    }

    @Override
    public boolean shouldPass(PythonFile parent, SubmittedFile target, Submission submission,
                              Criteria criteria, Document transcript)
            throws CannotBeAutomatedException, AutomationFailureException
    {
        try {
            PythonInspector inspector = new PythonInspector(target.fullPath);
            return inspector.canImportModule();
        } catch (IOException x) {
            throw new AutomationFailureException(x);
        } catch (PythonError x) {
            throw new CannotBeAutomatedException(
                    "Python error occurred importing module: " + x
            );
        }
    }
}

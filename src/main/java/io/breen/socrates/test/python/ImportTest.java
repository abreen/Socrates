package io.breen.socrates.test.python;

import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.python.PythonFile;
import io.breen.socrates.submission.Submission;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.test.*;
import io.breen.socrates.util.Pair;

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
                              Criteria criteria, Document transcript, Document notes)
            throws CannotBeAutomatedException, AutomationFailureException
    {
        try {
            PythonInspector inspector = new PythonInspector(target.fullPath);

            appendToDocument(transcript, ">>> from " + parent.getModuleName() + "import *\n");

            Pair<Boolean, String> result = inspector.canImportModule();

            if (result.first) {
                return true;
            } else {
                appendToDocument(transcript, result.second + "\n");
                return false;
            }

        } catch (IOException x) {
            throw new AutomationFailureException(x);
        } catch (PythonError x) {
            // should be thrown if an error occurs with our code, not student's
            String reason = "Python error occurred importing module: " + x;
            appendToDocument(transcript, reason);
            throw new CannotBeAutomatedException(reason);
        }
    }
}

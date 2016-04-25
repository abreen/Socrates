package io.breen.socrates.test.python.node;

import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.python.PythonFile;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.test.*;
import io.breen.socrates.test.python.PythonError;
import io.breen.socrates.test.python.PythonInspector;
import io.breen.socrates.util.Pair;

import javax.swing.text.Document;
import java.io.IOException;
import java.nio.file.Path;

public class ImportTestNode extends TestNode implements Automatable<PythonFile> {

    public ImportTestNode(PythonFile file) {
        super(file.getImportFailureDeduction(), "could not load '" + file.getPath() + "'");
    }

    @Override
    public String toString() {
        return "ImportTestNode()";
    }

    @Override
    public boolean shouldPass(PythonFile parent, SubmittedFile target, Path submissionDir,
                              Criteria criteria, Document transcript, Document notes)
            throws CannotBeAutomatedException, AutomationFailureException
    {
        try {
            PythonInspector inspector = new PythonInspector(target.getPath());

            appendToDocument(transcript, ">>> from " + parent.getModuleName() + " import *\n");

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

package io.breen.socrates.immutable.test.implementation.python;

import io.breen.socrates.immutable.criteria.Criteria;
import io.breen.socrates.immutable.file.python.PythonFile;
import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.immutable.submission.SubmittedFile;
import io.breen.socrates.immutable.test.*;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;

public class ImportTest extends Test implements Automatable<PythonFile> {

    public ImportTest(PythonFile file) {
        super(file.importFailureDeduction, "could not load '" + file.path + "'");
    }

    @Override
    public String getTestTypeName() {
        return "import check";
    }

    @Override
    public boolean shouldPass(PythonFile parent, SubmittedFile target, Submission submission,
                              Criteria criteria)
            throws CannotBeAutomatedException, AutomationFailureException
    {
        try (PythonInspector inspector = new PythonInspector(target.fullPath)) {
            inspector.openModule(parent.getModuleName());
        } catch (IOException | XmlRpcException x) {
            throw new AutomationFailureException(x);
        } catch (PythonError x) {
            return false;
        }

        return true;
    }
}

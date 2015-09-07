package io.breen.socrates.immutable.test.implementation.python;

import io.breen.socrates.immutable.file.python.PythonFile;
import io.breen.socrates.immutable.file.python.Variable;
import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.immutable.submission.SubmittedFile;
import io.breen.socrates.immutable.test.*;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;

public class VariableExistsTest extends VariableTest implements Automatable<PythonFile> {

    private final Variable variable;

    public VariableExistsTest(Variable variable) {
        super(variable.pointValue, "variable '" + variable.name + "' is missing");
        this.variable = variable;
    }

    @Override
    public String getTestTypeName() {
        return "check whether variable exists";
    }

    @Override
    public boolean shouldPass(PythonFile parent, SubmittedFile target, Submission submission)
            throws CannotBeAutomatedException, AutomationFailureException
    {
        try (PythonInspector inspector = new PythonInspector(target.fullPath)) {
            inspector.openModule(parent.getModuleName());
            return inspector.moduleHasVariable(this.variable.name);

        } catch (IOException | XmlRpcException x) {
            throw new AutomationFailureException(x);
        } catch (PythonError x) {
            return false;
        }
    }
}

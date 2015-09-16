package io.breen.socrates.immutable.test.implementation.python;

import io.breen.socrates.immutable.criteria.Criteria;
import io.breen.socrates.immutable.file.python.PythonFile;
import io.breen.socrates.immutable.file.python.Variable;
import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.immutable.submission.SubmittedFile;
import io.breen.socrates.immutable.test.*;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;

public class VariableEvalTest extends VariableTest implements Automatable<PythonFile> {

    /**
     * The expected value of the variable.
     */
    public Object value;

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public VariableEvalTest() {}

    public VariableEvalTest(double deduction, String description) {
        super(deduction, description);
    }

    @Override
    public String getTestTypeName() {
        return "variable evaluation";
    }

    @Override
    public boolean shouldPass(PythonFile parent, SubmittedFile target, Submission submission,
                              Criteria criteria)
            throws CannotBeAutomatedException, AutomationFailureException
    {
        Variable var = parent.getVariableForTest(this);
        if (var == null) throw new IllegalArgumentException();

        try (PythonInspector inspector = new PythonInspector(target.fullPath)) {
            inspector.openModule(parent.getModuleName());
            Object value = inspector.variableEval(var.name);
            return PythonInspector.equals(this.value, value);

        } catch (IOException | XmlRpcException x) {
            throw new AutomationFailureException(x);
        } catch (IllegalArgumentException | PythonError x) {
            throw new CannotBeAutomatedException();
        }
    }
}

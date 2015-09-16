package io.breen.socrates.immutable.test.implementation.python;

import io.breen.socrates.immutable.criteria.Criteria;
import io.breen.socrates.immutable.file.python.Function;
import io.breen.socrates.immutable.file.python.PythonFile;
import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.immutable.submission.SubmittedFile;
import io.breen.socrates.immutable.test.*;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.util.*;

public class FunctionEvalTest extends FunctionTest implements Automatable<PythonFile> {

    /**
     * The expected return value of the function.
     */
    public Object value;

    /**
     * The expected output of the function.
     */
    public String output;

    /**
     * The characters that should be sent to the function after invoking it.
     */
    public String input;

    /**
     * For each parameter, the actual value of the argument to give the function.
     */
    public Map<String, Object> arguments;

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public FunctionEvalTest() {}

    public FunctionEvalTest(double deduction, String description) {
        super(deduction, description);
    }

    @Override
    public String getTestTypeName() {
        return "function evaluation";
    }

    @Override
    public boolean shouldPass(PythonFile parent, SubmittedFile target, Submission submission, Criteria criteria)
            throws CannotBeAutomatedException, AutomationFailureException
    {
        Function func = parent.getFunctionForTest(this);
        if (func == null) throw new IllegalArgumentException();

        try (PythonInspector inspector = new PythonInspector(target.fullPath)) {
            inspector.openModule(parent.getModuleName());

            List<Object> args = new LinkedList<>();

            for (String parameter : func.parameters)
                args.add(arguments.get(parameter));

            Object returnValue = inspector.functionEval(func.name, args);

            return PythonInspector.equals(this.value, returnValue);

        } catch (IOException | XmlRpcException x) {
            throw new AutomationFailureException(x);
        } catch (IllegalArgumentException | PythonError x) {
            throw new CannotBeAutomatedException();
        }
    }
}

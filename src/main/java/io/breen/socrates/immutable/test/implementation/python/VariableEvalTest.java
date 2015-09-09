package io.breen.socrates.immutable.test.implementation.python;

import io.breen.socrates.immutable.file.python.PythonFile;
import io.breen.socrates.immutable.file.python.Variable;
import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.immutable.submission.SubmittedFile;
import io.breen.socrates.immutable.test.*;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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

    public static boolean equals(Object expected, Object other) throws CannotBeAutomatedException {
        if (expected == null) {
            return other == null;

        } else if (expected instanceof Integer) {
            Integer n = (Integer)expected;
            if (other == null || !(other instanceof Integer)) return false;
            return n.equals(other);

        } else if (expected instanceof Double) {
            Double n = (Double)expected;
            if (other == null || !(other instanceof Double)) return false;
            return n.equals(other);

        } else if (expected instanceof String) {
            String s = (String)expected;
            if (other == null || !(other instanceof String)) return false;
            return s.equals(other);

        } else if (expected instanceof List) {
            List list = (List)expected;
            if (other == null) return false;

            if (other instanceof Object[]) {
                Object[] objArr = (Object[])other;
                for (int i = 0; i < objArr.length; i++) {
                    if (!list.get(i).equals(objArr[i])) return false;
                }
                return true;
            }

            return list.equals(other);

        } else if (expected instanceof Map) {
            Map map = (Map)expected;
            if (other == null || !(other instanceof Map)) return false;
            return map.equals(other);

        }

        throw new CannotBeAutomatedException();
    }

    @Override
    public String getTestTypeName() {
        return "Python evaluation test";
    }

    @Override
    public boolean shouldPass(PythonFile parent, SubmittedFile target, Submission submission)
            throws CannotBeAutomatedException, AutomationFailureException
    {
        Variable var = parent.getVariableForTest(this);
        if (var == null) throw new IllegalArgumentException();

        try (PythonInspector inspector = new PythonInspector(target.fullPath)) {
            inspector.openModule(parent.getModuleName());
            Object value = inspector.variableEval(var.name);
            return equals(this.value, value);

        } catch (IOException | XmlRpcException x) {
            throw new AutomationFailureException(x);
        } catch (PythonError x) {
            return false;
        }
    }
}

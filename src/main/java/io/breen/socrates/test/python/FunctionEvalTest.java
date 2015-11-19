package io.breen.socrates.test.python;

import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.python.Function;
import io.breen.socrates.file.python.PythonFile;
import io.breen.socrates.submission.Submission;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.test.*;
import io.breen.socrates.util.Pair;

import javax.swing.text.Document;
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

    private static String callToString(String functionName, List<Object> args) {
        StringBuilder builder = new StringBuilder(functionName);

        builder.append("(");

        for (int i = 0; i < args.size(); i++) {
            builder.append(repr(args.get(i)));

            if (i != args.size() - 1) builder.append(", ");
        }

        builder.append(")");

        return builder.toString();
    }

    private static String repr(Object o) {
        if (o == null) {
            return "None";
        } else if (o instanceof String) {
            String s = (String)o;
            return "'" + s + "'";
        }

        return o.toString();
    }

    @Override
    public String toString() {
        return "FunctionEvalTest(" +
                "value=" + value + ", " +
                "output=" + output + ", " +
                "input=" + input + ", " +
                "arguments=" + arguments + ")";
    }

    @Override
    public String getTestTypeName() {
        return "function evaluation";
    }

    @Override
    public boolean shouldPass(PythonFile parent, SubmittedFile target, Submission submission,
                              Criteria criteria, Document transcript, Document notes)
            throws CannotBeAutomatedException, AutomationFailureException
    {
        Function func = parent.getFunctionForTest(this);
        if (func == null) throw new IllegalArgumentException();

        List<Object> args = new LinkedList<>();
        for (String parameter : func.parameters)
            args.add(arguments.get(parameter));

        try {
            PythonInspector inspector = new PythonInspector(target.fullPath);

            appendToDocument(transcript, ">>> " + callToString(func.name, args) + "\n");

            Pair<Boolean, String> result = inspector.functionProduces(
                    func.name, args, null, input, value, output
            );

            appendToDocument(transcript, result.second + "\n");

            return result.first;

        } catch (IOException x) {
            throw new AutomationFailureException(x);
        } catch (PythonError x) {
            String reason = "Python error occurred evaluating function: " + x;
            appendToDocument(transcript, reason + "\n");
            throw new CannotBeAutomatedException(reason);
        }
    }
}

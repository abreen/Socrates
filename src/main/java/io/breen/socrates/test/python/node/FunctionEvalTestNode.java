package io.breen.socrates.test.python.node;

import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.python.Function;
import io.breen.socrates.file.python.PythonFile;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.test.Automatable;
import io.breen.socrates.test.AutomationFailureException;
import io.breen.socrates.test.CannotBeAutomatedException;
import io.breen.socrates.test.python.PythonError;
import io.breen.socrates.test.python.PythonInspector;
import io.breen.socrates.util.Pair;

import javax.swing.text.Document;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FunctionEvalTestNode extends FunctionTestNode implements Automatable<PythonFile> {

    /**
     * The expected return value of the function (could be a standard Java object as instantiated by
     * SnakeYAML, or an PythonObject defined in io.breen.socrates.file.python.PythonObject).
     */
    private Object value;
    /**
     * The expected output of the function.
     */
    private String output;
    /**
     * The characters that should be sent to the function after invoking it.
     */
    private String input;
    /**
     * For each parameter, the actual value of the argument to give the function. Each value could
     * be a standard Java object, or an PythonObject defined in io.breen.socrates.file.python.PythonObject.
     */
    private Map<String, Object> arguments;

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public FunctionEvalTestNode() {
    }

    public FunctionEvalTestNode(double deduction, String description) {
        super(deduction, description);
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        checkFrozen();
        this.value = value;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        checkFrozen();
        this.output = output;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        checkFrozen();
        this.input = input;
    }

    public Map<String, Object> getArguments() {
        return Collections.unmodifiableMap(arguments);
    }

    public void setArguments(Map<String, Object> arguments) {
        checkFrozen();
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        return "FunctionEvalTestNode(" +
                "value=" + value + ", " +
                "output=" + output + ", " +
                "input=" + input + ", " +
                "arguments=" + arguments + ")";
    }

    @Override
    public boolean shouldPass(PythonFile parent, SubmittedFile target, Path submissionDir,
                              Criteria criteria, Document transcript, Document notes)
            throws CannotBeAutomatedException, AutomationFailureException {
        Function func = parent.getFunctionForTest(this);
        if (func == null) throw new IllegalArgumentException();

        List<Object> args = new LinkedList<>();
        for (String parameter : func.parameters)
            args.add(arguments.get(parameter));

        // TODO kwargs, if any

        try {
            PythonInspector inspector = new PythonInspector(target.getPath());

            appendToDocument(
                    transcript,
                    ">>> " + PythonInspector.callToString(func.name, args) + "\n"
            );

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

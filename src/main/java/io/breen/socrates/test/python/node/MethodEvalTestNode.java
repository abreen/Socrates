package io.breen.socrates.test.python.node;


import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.python.*;
import io.breen.socrates.file.python.PythonObject;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.test.*;
import io.breen.socrates.test.python.PythonError;
import io.breen.socrates.test.python.PythonInspector;
import io.breen.socrates.util.Pair;

import javax.swing.text.Document;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class MethodEvalTestNode extends MethodTestNode implements Automatable<PythonFile> {

    /**
     * The expected return value of the method (could be a standard Java object as instantiated by
     * SnakeYAML, or an PythonObject defined in io.breen.socrates.file.python.PythonObject).
     */
    public Object value;

    /**
     * The expected output of the method.
     */
    public String output;

    /**
     * The characters that should be sent to the method after invoking it.
     */
    public String input;

    /**
     * For each parameter, the actual value of the argument to give the method. Each value could be
     * a standard Java object, or an PythonObject defined in io.breen.socrates.file.python.PythonObject.
     */
    public Map<String, Object> arguments;

    /**
     * An Object storing the fields that the called object should have before the method is called.
     */
    public PythonObject before;

    /**
     * An Object storing the fields that the called object should have after the method is called.
     * Only fields specified here are checked. If this is null, the called object is not checked at
     * all (useful if only the method's return value should be checked).
     */
    public PythonObject after;


    @Override
    public boolean shouldPass(PythonFile parent, SubmittedFile target, Path submissionDir,
                              Criteria criteria, Document transcript, Document notes)
            throws CannotBeAutomatedException, AutomationFailureException
    {
        Method m = parent.getMethodForTest(this);
        if (m == null) throw new IllegalArgumentException();

        // TODO what if parameters are Python objects (need to send fields to tester.py)

        List<Object> args = new LinkedList<>();
        for (String parameter : m.parameters)
            args.add(arguments.get(parameter));

        // TODO kwargs, if any

        try {
            PythonInspector inspector = new PythonInspector(target.getPath());

            if (before != null) {
                appendToDocument(
                        transcript, ">>> obj = " + PythonInspector.toPythonString(before) + "\n"
                );
            }

            appendToDocument(
                    transcript,
                    ">>> obj." + PythonInspector.callToString(m.name, args) + "\n"
            );

            Pair<Boolean, String> result = inspector.methodProduces(
                    m.name, before, args, null, input, after, value, output
            );

            if (!result.second.isEmpty()) appendToDocument(transcript, result.second + "\n");

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

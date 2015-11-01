package io.breen.socrates.test.java;

import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.java.Class;
import io.breen.socrates.file.java.*;
import io.breen.socrates.file.java.Object;
import io.breen.socrates.submission.Submission;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.test.*;
import org.codehaus.janino.JavaSourceClassLoader;

import javax.swing.text.Document;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;


public class MethodEvalTest extends Test implements Automatable<JavaFile> {

    /**
     * The expected return value of the method.
     *
     * @see io.breen.socrates.file.java.Object
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
     * For each parameter, the actual value of the argument to give the function.
     *
     * @see io.breen.socrates.file.java.Object
     */
    public Map<String, Object> arguments;

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public MethodEvalTest() {}

    private static java.lang.Object[] buildArguments(List<Parameter> parameters,
                                                     Map<String, Object> arguments)
    {
        java.lang.Object[] args = new java.lang.Object[parameters.size()];

        for (int i = 0; i < args.length; i++)
            args[i] = arguments.get(parameters.get(i).name).toJavaObject();

        return args;
    }

    @Override
    public String toString() {
        return "MethodEvalTest(" +
                "value=" + value + ", " +
                "output=" + output + ", " +
                "input=" + input + ", " +
                "arguments=" + arguments +
                ")";
    }

    @Override
    public boolean shouldPass(JavaFile parent, SubmittedFile target, Submission submission,
                              Criteria criteria, Document transcript)
            throws CannotBeAutomatedException, AutomationFailureException
    {
        Method method = parent.getMethodForTest(this);
        if (method == null) throw new IllegalArgumentException();

        Class klass = parent.getClassForMethod(method);
        if (klass == null) throw new IllegalArgumentException();

        ClassLoader cl = new JavaSourceClassLoader(
                this.getClass().getClassLoader(),
                new java.io.File[] {submission.submissionDir.toFile()},
                null
        );

        java.lang.Class c;
        try {
            c = cl.loadClass(klass.name);
        } catch (ClassNotFoundException x) {
            throw new AutomationFailureException(x);
        }

        java.lang.reflect.Method[] methods = c.getDeclaredMethods();
        java.lang.reflect.Method studentMethod = null;

        for (java.lang.reflect.Method m : methods) {
            if (m.getName().equals(method.name)) {
                studentMethod = m;
                break;
            }
        }

        if (studentMethod == null) {
            appendToDocument(transcript, "could not locate method: is it named incorrectly?");
            throw new CannotBeAutomatedException("could not locate method");
        }

        // TODO use before/after to create an instance

        java.lang.Object[] args = buildArguments(method.parameters, arguments);

        // TODO buffer & send input to the method

        java.lang.Object returnValue;
        try {
            returnValue = studentMethod.invoke(null, args);
        } catch (IllegalArgumentException x) {
            appendToDocument(transcript, "could not invoke method: " + x.getLocalizedMessage());
            throw new CannotBeAutomatedException("got exception invoking method: " + x);
        } catch (IllegalAccessException x) {
            throw new AutomationFailureException(x);
        } catch (InvocationTargetException x) {
            appendToDocument(transcript, "exception was thrown in student code: " + x.getCause());
            return false;
        }

        // TODO capture and check output

        return returnValue.equals(value.toJavaObject());
    }

    @Override
    public String getTestTypeName() {
        return "method evaluation";
    }
}

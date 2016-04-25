package io.breen.socrates.test.java;

import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.java.JavaClass;
import io.breen.socrates.file.java.*;
import io.breen.socrates.file.java.JavaObject;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.test.*;
import org.codehaus.janino.JavaSourceClassLoader;

import javax.swing.text.Document;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;


public class MethodEvalTestNode extends TestNode implements Automatable<JavaFile> {

    /**
     * The expected return value of the method.
     *
     * @see JavaObject
     */
    public JavaObject value;

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
     * @see JavaObject
     */
    public Map<String, JavaObject> arguments;


    /**
     * This empty constructor is used by SnakeYAML.
     */
    public MethodEvalTestNode() {}

    private static Object[] buildArguments(List<Parameter> parameters,
                                           Map<String, JavaObject> arguments)
    {
        Object[] args = new Object[parameters.size()];

        for (int i = 0; i < args.length; i++)
            args[i] = arguments.get(parameters.get(i).name).toObject();

        return args;
    }

    @Override
    public String toString() {
        return "MethodEvalTestNode(" +
                "value=" + value + ", " +
                "output=" + output + ", " +
                "input=" + input + ", " +
                "arguments=" + arguments +
                ")";
    }

    @Override
    public boolean shouldPass(JavaFile parent, SubmittedFile target, Path submissionDir,
                              Criteria criteria, Document transcript, Document notes)
            throws CannotBeAutomatedException, AutomationFailureException
    {
        Method method = parent.getMethodForTest(this);
        if (method == null) throw new IllegalArgumentException();

        JavaClass klass = parent.getClassForMethod(method);
        if (klass == null) throw new IllegalArgumentException();

        ClassLoader cl = new JavaSourceClassLoader(
                this.getClass().getClassLoader(),
                new java.io.File[] {submissionDir.toFile()},
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

        Object[] args = buildArguments(method.parameters, arguments);

        // TODO buffer & send input to the method

        Object returnValue;
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

        return returnValue.equals(value.toObject());
    }
}

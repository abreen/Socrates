package io.breen.socrates.test.java;

import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.java.Class;
import io.breen.socrates.file.java.*;
import io.breen.socrates.submission.Submission;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.test.*;
import org.codehaus.janino.JavaSourceClassLoader;

import javax.swing.text.Document;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;


public class MethodEvalTest extends Test implements Automatable<JavaFile> {

    /**
     * The expected return value of the method.
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
     */
    public Map<String, Object> arguments;

    public MethodEvalTest() {}

    private static Object[] buildArguments(List<String> parameters, Map<String, Object> arguments) {
        Object[] args = new Object[parameters.size()];

        for (int i = 0; i < args.length; i++) {
            Object o = arguments.get(parameters.get(i));

            if (o instanceof ArrayList) {
                // TODO get rid of this ugly hack to convert SnakeYAML's ArrayLists to arrays
                // TODO what if an ArrayList really should be passed in?!

                o = ((ArrayList)o).toArray(new String[] {});
            }

            args[i] = o;
        }

        return args;
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

        if (studentMethod == null) throw new CannotBeAutomatedException("could not locate method");

        // TODO use before/after to create an instance

        Object[] args = buildArguments(method.parameters, arguments);

        // TODO buffer & send input to the method

        Object returnValue;
        try {
            returnValue = studentMethod.invoke(null, args);
        } catch (IllegalAccessException | InvocationTargetException x) {
            throw new AutomationFailureException(x);
        }

        // TODO capture and check output

        if (Modifier.isStatic(studentMethod.getModifiers())) {
            // do not compare return value
            return true;
        }

        return value.equals(returnValue);
    }

    @Override
    public String getTestTypeName() {
        return "method evaluation";
    }
}

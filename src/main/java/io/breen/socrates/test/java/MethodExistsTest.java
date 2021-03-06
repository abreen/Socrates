package io.breen.socrates.test.java;

import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.java.Class;
import io.breen.socrates.file.java.*;
import io.breen.socrates.submission.Submission;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.test.*;
import org.codehaus.janino.JavaSourceClassLoader;

import javax.swing.text.Document;


public class MethodExistsTest extends Test implements Automatable<JavaFile> {

    private final Method method;

    public MethodExistsTest(Method method) {
        super(method.pointValue, "method '" + method.name + "' is missing");
        this.method = method;
    }

    @Override
    public boolean shouldPass(JavaFile parent, SubmittedFile target, Submission submission,
                              Criteria criteria, Document transcript, Document notes)
            throws CannotBeAutomatedException, AutomationFailureException
    {
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

        // TODO what about overloaded methods?

        for (java.lang.reflect.Method m : methods)
            if (m.getName().equals(method.name)) return true;

        return false;
    }

    @Override
    public String getTestTypeName() {
        return "method check";
    }
}

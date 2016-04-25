package io.breen.socrates.test.java;

import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.java.JavaClass;
import io.breen.socrates.file.java.*;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.test.*;
import org.codehaus.janino.JavaSourceClassLoader;

import javax.swing.text.Document;
import java.nio.file.Path;


public class MethodExistsTestNode extends TestNode implements Automatable<JavaFile> {

    private final Method method;


    public MethodExistsTestNode(Method method) {
        super(method.pointValue, "method '" + method.name + "' is missing");
        this.method = method;
    }

    @Override
    public boolean shouldPass(JavaFile parent, SubmittedFile target, Path submissionDir,
                              Criteria criteria, Document transcript, Document notes)
            throws CannotBeAutomatedException, AutomationFailureException
    {
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

        // TODO what about overloaded methods?

        for (java.lang.reflect.Method m : methods)
            if (m.getName().equals(method.name)) return true;

        return false;
    }
}

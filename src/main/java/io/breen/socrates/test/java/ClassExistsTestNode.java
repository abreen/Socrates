package io.breen.socrates.test.java;

import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.java.JavaClass;
import io.breen.socrates.file.java.JavaFile;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.test.*;
import org.codehaus.janino.JavaSourceClassLoader;

import javax.swing.text.Document;
import java.nio.file.Path;


public class ClassExistsTestNode extends TestNode implements Automatable<JavaFile> {

    private final JavaClass klass;


    public ClassExistsTestNode(JavaClass klass) {
        super(klass.pointValue, "class '" + klass.name + "' is missing");
        this.klass = klass;
    }

    @Override
    public boolean shouldPass(JavaFile parent, SubmittedFile target, Path submissionDir,
                              Criteria criteria, Document transcript, Document notes)
            throws CannotBeAutomatedException, AutomationFailureException
    {
        ClassLoader cl = new JavaSourceClassLoader(
                this.getClass().getClassLoader(),
                new java.io.File[] {submissionDir.toFile()},
                null
        );

        try {
            cl.loadClass(klass.name);
        } catch (ClassNotFoundException x) {
            return false;
        }

        return true;
    }
}

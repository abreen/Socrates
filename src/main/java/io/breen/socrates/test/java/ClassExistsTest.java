package io.breen.socrates.test.java;

import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.java.Class;
import io.breen.socrates.file.java.JavaFile;
import io.breen.socrates.submission.Submission;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.test.*;
import org.codehaus.janino.JavaSourceClassLoader;

import javax.swing.text.Document;


public class ClassExistsTest extends Test implements Automatable<JavaFile> {

    private final Class klass;

    public ClassExistsTest(Class klass) {
        super(klass.pointValue, "class '" + klass.name + "' is missing");
        this.klass = klass;
    }

    @Override
    public boolean shouldPass(JavaFile parent, SubmittedFile target, Submission submission,
                              Criteria criteria, Document transcript)
            throws CannotBeAutomatedException, AutomationFailureException
    {
        ClassLoader cl = new JavaSourceClassLoader(
                this.getClass().getClassLoader(),
                new java.io.File[] {submission.submissionDir.toFile()},
                null
        );

        try {
            cl.loadClass(klass.name);
        } catch (ClassNotFoundException x) {
            return false;
        }

        return true;
    }

    @Override
    public String getTestTypeName() {
        return "class check";
    }
}

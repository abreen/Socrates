package io.breen.socrates.immutable.file.java;

import io.breen.socrates.immutable.PostConstructionAction;
import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.test.Test;
import io.breen.socrates.immutable.test.TestGroup;
import io.breen.socrates.immutable.test.implementation.java.ClassExistsTest;
import io.breen.socrates.immutable.test.implementation.java.MethodExistsTest;

import java.util.*;

/**
 * A JavaFile is a representation of a Java file containing source code. The file should contain at
 * least one class.
 */
public final class JavaFile extends File implements PostConstructionAction {

    /**
     * The deduction taken when attempting to compile the Java file fails.
     */
    public double compileFailureDeduction;

    public List<Class> classes = new ArrayList<>(0);

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public JavaFile() {
        language = "java";
        contentsArePlainText = true;
    }

    public JavaFile(String path, double pointValue, Map<Date, Double> dueDates, List<Object> tests)
    {
        super(path, pointValue, dueDates, tests);
    }

    private static boolean hasTest(List<Object> list, Test test) {
        for (Object o : list)
            if (o instanceof Test && o == test) return true;
            else if (o instanceof TestGroup) return hasTest(((TestGroup)o).members, test);

        return false;
    }

    @Override
    public void afterConstruction() {
        super.afterConstruction();
    }

    @Override
    protected TestGroup createTestRoot() {
        List<Object> tests = new LinkedList<>();

        for (Class c : classes) {
            List<Object> methodRoots = new LinkedList<>();

            for (Method m : c.methods) {
                Test methodExistsTest = new MethodExistsTest(m);

                // either this method doesn't exist, or we should do its tests

                methodRoots.add(
                        new TestGroup(
                                Arrays.asList(
                                        methodExistsTest, new TestGroup(m.tests, 0, 0.0)
                                ), 1, 0.0
                        )
                );
            }

            // either this class doesn't exist, or we should do its test (including above)

            Test classExistsTest = new ClassExistsTest(c);

            methodRoots.addAll(0, c.tests);

            tests.add(
                    new TestGroup(
                            Arrays.asList(
                                    classExistsTest, new TestGroup(methodRoots, 0, 0.0)
                            ), 1, 0.0
                    )
            );
        }

        // either this file doesn't compile, or we do all the tests created above

        TestGroup root = super.createTestRoot();
        root.members.addAll(tests);
        return root;
    }

    @Override
    public String getFileTypeName() {
        return "Java source code";
    }

    public Class getClassForMethod(Method m) {
        for (Class c : classes) {
            if (c.methods.contains(m)) return c;
        }

        return null;
    }

    public Method getMethodForTest(Test t) {
        for (Class c : classes)
            for (Method m : c.methods)
                if (hasTest(m.tests, t)) return m;

        return null;
    }
}

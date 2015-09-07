package io.breen.socrates.immutable.file.python;

import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.test.TestGroup;
import io.breen.socrates.immutable.test.implementation.python.VariableExistsTest;
import io.breen.socrates.immutable.test.implementation.python.VariableTest;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * A PythonFile is a representation of a Python module (a file ending in .py containing valid Python
 * code) containing source code.
 */
public final class PythonFile extends File {

    public List<Variable> variables;

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public PythonFile() {}

    public PythonFile(String path, double pointValue, Map<Date, Double> dueDates,
                      List<Object> tests)
    {
        super(path, pointValue, dueDates, tests);
    }

    @Override
    protected TestGroup createTestRoot() {
        TestGroup root = super.createTestRoot();

        /*
         * We must now add tests specified by the variables, functions and classes/methods in
         * the criteria file.
         */
        for (Variable v : variables) {
            /*
             * We will put this test in a group containing the rest of the tests specified in
             * the criteria file, and limit the new group to fail at most 1. This means the
             * tests in the criteria file will be skipped if the variable does not exist.
             */
            VariableExistsTest t = new VariableExistsTest(v);

            List<Object> members = new LinkedList<>();
            members.add(t);
            members.add(new TestGroup(v.tests, 0, 0.0));

            TestGroup group = new TestGroup(members, 1, 0.0);

            root.members.add(group);
        }

        return root;
    }

    @Override
    public String getFileTypeName() {
        return "Python source code";
    }

    public Variable getVariableForTest(VariableTest test) {
        for (Variable v : variables)
            if (v.tests.contains(test)) return v;

        return null;
    }

    public String getModuleName() {
        Path p = Paths.get(path);
        String fileName = p.getFileName().toString();
        String[] parts = fileName.split("\\.");
        return parts[parts.length - 2];
    }
}

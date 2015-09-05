package io.breen.socrates.immutable.file.python;

import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.test.TestGroup;
import io.breen.socrates.immutable.test.implementation.python.VariableEvalTest;

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
        for (Variable v : variables)
            root.members.addAll(v.tests);

        return root;
    }

    @Override
    public String getFileTypeName() {
        return "Python source code";
    }

    public Variable getVariableForTest(VariableEvalTest test) {
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

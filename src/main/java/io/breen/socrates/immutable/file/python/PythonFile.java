package io.breen.socrates.immutable.file.python;

import io.breen.socrates.immutable.PostConstructionAction;
import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.test.Test;
import io.breen.socrates.immutable.test.TestGroup;
import io.breen.socrates.immutable.test.implementation.python.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * A PythonFile is a representation of a Python module (a file ending in .py containing valid Python
 * code) containing source code.
 */
public final class PythonFile extends File implements PostConstructionAction {

    /**
     * The deduction taken when the Python module cannot be imported due to a serious error (e.g., a
     * syntax error).
     */
    public double importFailureDeduction;

    public List<Variable> variables = new ArrayList<>(0);
    public List<Function> functions = new ArrayList<>(0);

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public PythonFile() {}

    public PythonFile(String path, double pointValue, Map<Date, Double> dueDates,
                      List<Object> tests)
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
        contentType = "text/python";
    }

    @Override
    protected TestGroup createTestRoot() {
        List<Object> tests = new LinkedList<>();

        for (Variable v : variables) {
            Test test = new VariableExistsTest(v);

            if (!v.tests.isEmpty()) {
                List<Object> members = new LinkedList<>();
                members.add(test);
                members.add(new TestGroup(v.tests, 0, 0.0));

                TestGroup group = new TestGroup(members, 1, 0.0);

                tests.add(group);
            } else {
                tests.add(test);
            }
        }

        for (Function f : functions) {
            Test test = new FunctionExistsTest(f);

            if (!f.tests.isEmpty()) {
                List<Object> members = new LinkedList<>();
                members.add(test);
                members.add(new TestGroup(f.tests, 0, 0.0));

                TestGroup group = new TestGroup(members, 1, 0.0);

                tests.add(group);
            } else {
                tests.add(test);
            }
        }

        TestGroup root = super.createTestRoot();

        Test test = new ImportTest(this);

        List<Object> members = new LinkedList<>();
        members.add(test);

        if (!tests.isEmpty()) members.add(new TestGroup(tests, 0, 0.0));

        TestGroup group = new TestGroup(members, 1, 0.0);

        root.members.add(group);

        return root;
    }

    @Override
    public String getFileTypeName() {
        return "Python source code";
    }

    public Variable getVariableForTest(VariableTest test) {
        for (Variable v : variables)
            if (hasTest(v.tests, test)) return v;

        return null;
    }

    public Function getFunctionForTest(FunctionTest test) {
        for (Function f : functions)
            if (hasTest(f.tests, test)) return f;

        return null;
    }

    public String getModuleName() {
        Path p = Paths.get(path);
        String fileName = p.getFileName().toString();
        String[] parts = fileName.split("\\.");
        return parts[parts.length - 2];
    }
}

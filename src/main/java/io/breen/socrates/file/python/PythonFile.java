package io.breen.socrates.file.python;

import io.breen.socrates.file.File;
import io.breen.socrates.test.Node;
import io.breen.socrates.test.TestNode;
import io.breen.socrates.test.python.node.FunctionTestNode;
import io.breen.socrates.test.python.node.MethodTestNode;
import io.breen.socrates.test.python.node.VariableTestNode;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

/**
 * A PythonFile is a representation of a Python module (a file ending in .py containing valid Python
 * code) containing source code.
 */
public class PythonFile extends File {

    /**
     * The deduction taken when the Python module cannot be imported due to a serious error (e.g., a
     * syntax error).
     */
    private double importFailureDeduction;
    private List<Variable> variables = Collections.emptyList();
    private List<Function> functions = Collections.emptyList();
    private List<PythonClass> classes = Collections.emptyList();

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public PythonFile() {
    }

    public PythonFile(double importFailureDeduction) {
        this.importFailureDeduction = importFailureDeduction;
    }

    private static boolean hasTest(List<Node> list, TestNode test) {
        for (Node n : list) {
            if (n == test)
                return true;

            if (!n.isLeaf())
                return hasTest(n.getChildren(), test);
        }

        return false;
    }

    public double getImportFailureDeduction() {
        return importFailureDeduction;
    }

    /**
     * Used by SnakeYAML.
     */
    public void setImportFailureDeduction(double importFailureDeduction) {
        checkFrozen();
        this.importFailureDeduction = importFailureDeduction;
    }

    public List<Variable> getVariables() {
        return Collections.unmodifiableList(variables);
    }

    /**
     * Used by SnakeYAML.
     */
    public void setVariables(List<Variable> variables) {
        checkFrozen();
        this.variables = variables;
    }

    public List<Function> getFunctions() {
        return Collections.unmodifiableList(functions);
    }

    /**
     * Used by SnakeYAML.
     */
    public void setFunctions(List<Function> functions) {
        checkFrozen();
        this.functions = functions;
    }

    public List<PythonClass> getClasses() {
        return Collections.unmodifiableList(classes);
    }

    /**
     * Used by SnakeYAML.
     */
    public void setClasses(List<PythonClass> classes) {
        checkFrozen();
        this.classes = classes;
    }

    public Variable getVariableForTest(VariableTestNode test) {
        for (Variable v : variables)
            if (hasTest(v.tests, test)) return v;

        return null;
    }

    public Function getFunctionForTest(FunctionTestNode test) {
        for (Function f : functions)
            if (hasTest(f.tests, test)) return f;

        return null;
    }

    public PythonClass getClassContainingMethod(Method method) {
        for (PythonClass c : classes)
            if (c.methods.contains(method)) return c;

        return null;
    }

    public Method getMethodForTest(MethodTestNode test) {
        for (PythonClass c : classes)
            for (Method m : c.methods)
                if (hasTest(m.tests, test)) return m;

        return null;
    }

    public String getModuleName() {
        Path p = Paths.get(getPath());
        String fileName = p.getFileName().toString();
        String[] parts = fileName.split("\\.");
        return parts[parts.length - 2];
    }

    public String toString() {
        return "PythonFile(" +
                "path=" + getPath() + ", " +
                "pointValue=" + getPointValue() + ", " +
                "tests=" + getTests() + ", " +
                "variables=" + variables + ", " +
                "functions=" + functions + ", " +
                "classes=" + classes +
                ")";
    }

    /**
     * Freezes this PythonFile object, its tests, and the tests written for its variables,
     * functions, classes, and methods on each class.
     */
    @Override
    public void freeze() {
        variables.forEach(v -> v.tests.forEach(Node::freezeAll));
        functions.forEach(f -> f.tests.forEach(Node::freezeAll));
        classes.forEach(c -> {
            c.tests.forEach(Node::freezeAll);
            c.methods.forEach(m -> m.tests.forEach(Node::freezeAll));
        });
        super.freeze();
    }
}

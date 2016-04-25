package io.breen.socrates.file.java;

import io.breen.socrates.file.File;
import io.breen.socrates.test.Node;
import io.breen.socrates.test.TestNode;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * A JavaFile is a representation of a Java file containing source code. The file should contain at
 * least one class.
 */
public class JavaFile extends File {

    private List<JavaClass> classes = Collections.emptyList();

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public JavaFile() {
    }

    public JavaFile(String path, double pointValue, Map<Date, Double> dueDates, List<Node> tests) {
        super(path, pointValue, dueDates, tests);
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

    public List<JavaClass> getClasses() {
        return Collections.unmodifiableList(classes);
    }

    /**
     * Used by SnakeYAML.
     */
    public void setClasses(List<JavaClass> classes) {
        checkFrozen();
        this.classes = classes;
    }

    public JavaClass getClassForMethod(Method m) {
        for (JavaClass c : classes) {
            if (c.methods.contains(m)) return c;
        }

        return null;
    }

    public Method getMethodForTest(TestNode t) {
        for (JavaClass c : classes)
            for (Method m : c.methods)
                if (hasTest(m.tests, t)) return m;

        return null;
    }

    /**
     * Freezes this JavaFile object, its tests, and the tests written for its classes, and methods
     * on each class.
     */
    @Override
    public void freeze() {
        classes.forEach(c -> {
            c.tests.forEach(Node::freezeAll);
            c.methods.forEach(m -> m.tests.forEach(Node::freezeAll));
        });
        super.freeze();
    }
}

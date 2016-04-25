package io.breen.socrates.file.java;

import io.breen.socrates.test.Node;

import java.util.Collections;
import java.util.List;

/**
 * A class representing a Java class specified in the criteria file.
 */
public class JavaClass {

    public String name;
    public double pointValue;
    public List<Node> tests = Collections.emptyList();
    public List<Method> methods = Collections.emptyList();

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public JavaClass() {}
}

package io.breen.socrates.file.python;


import io.breen.socrates.test.Node;

import java.util.Collections;
import java.util.List;

public class PythonClass {

    public String name;
    public double pointValue;
    public List<Node> tests = Collections.emptyList();
    public List<Method> methods = Collections.emptyList();

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public PythonClass() {}
}

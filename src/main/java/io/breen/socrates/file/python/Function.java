package io.breen.socrates.file.python;

import io.breen.socrates.test.Node;

import java.util.List;

/**
 * A class representing a Python function specified in the criteria file.
 */
public class Function {

    public String name;
    public List<String> parameters;
    public double pointValue;
    public List<Node> tests;

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public Function() {}
}

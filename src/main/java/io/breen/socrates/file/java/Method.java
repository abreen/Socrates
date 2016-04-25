package io.breen.socrates.file.java;

import io.breen.socrates.test.Node;

import java.util.List;

/**
 * A class representing a Java method specified in the criteria file.
 */
public class Method {

    public String name;
    public double pointValue;
    public List<Parameter> parameters;
    public List<Node> tests;

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public Method() {}
}

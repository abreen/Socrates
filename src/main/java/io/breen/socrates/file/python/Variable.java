package io.breen.socrates.file.python;

import io.breen.socrates.test.Node;

import java.util.Collections;
import java.util.List;

/**
 * A class representing a variable specified in the criteria file.
 */
public class Variable {

    public String name;
    public double pointValue;
    public List<Node> tests;

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public Variable() {}

    public Variable(String name, double pointValue) {
        this.name = name;
        this.pointValue = pointValue;
        tests = Collections.emptyList();
    }
}

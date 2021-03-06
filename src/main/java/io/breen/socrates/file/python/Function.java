package io.breen.socrates.file.python;

import java.util.List;

/**
 * A class representing a Python function specified in the criteria file.
 */
public class Function {

    public String name;
    public List<String> parameters;
    public double pointValue;
    public List<java.lang.Object> tests;

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public Function() {}
}

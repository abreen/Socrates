package io.breen.socrates.immutable.file.python;

import java.util.List;

/**
 * A class representing a variable specified in the criteria file.
 */
public class Variable {

    public String name;
    public double pointValue;
    public List<Object> tests;

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public Variable() {}
}

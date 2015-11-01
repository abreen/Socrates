package io.breen.socrates.file.java;

import java.util.List;

/**
 * A class representing a Java method specified in the criteria file.
 */
public class Method {

    public String name;
    public double pointValue;
    public List<Parameter> parameters;
    public List<java.lang.Object> tests;

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public Method() {}
}

package io.breen.socrates.file.java;

import java.util.List;

/**
 * A class representing a Java method specified in the criteria file.
 */
public class Method {

    public String name;
    public double pointValue;
    public List<String> parameters;
    public List<Object> tests;

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public Method() {}
}

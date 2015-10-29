package io.breen.socrates.file.java;

import java.util.ArrayList;
import java.util.List;

/**
 * A class representing a Java class specified in the criteria file.
 */
public class Class {

    public String name;
    public double pointValue;
    public List<Object> tests = new ArrayList<>(0);
    public List<Method> methods = new ArrayList<>(0);

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public Class() {}
}

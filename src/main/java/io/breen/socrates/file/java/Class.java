package io.breen.socrates.file.java;

import java.util.Collections;
import java.util.List;

/**
 * A class representing a Java class specified in the criteria file.
 */
public class Class {

    public String name;
    public double pointValue;
    public List<java.lang.Object> tests = Collections.emptyList();
    public List<Method> methods = Collections.emptyList();

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public Class() {}
}

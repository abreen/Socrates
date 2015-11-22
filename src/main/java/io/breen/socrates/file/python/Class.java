package io.breen.socrates.file.python;


import java.util.Collections;
import java.util.List;

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

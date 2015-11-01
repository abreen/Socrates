package io.breen.socrates.file.java;

/**
 * Instances of this class are used to specify the type of a Java object in a criteria file. At
 * runtime, instances of this class will be used to attempt to convert an actual object produced by
 * SnakeYAML into an object that a Java method expects.
 */
public class Type {

    public String typeName;

    /**
     * This constructor is used by SnakeYAML.
     */
    public Type(String scalar) {
        typeName = scalar;
    }
}

package io.breen.socrates.file.python;


public class Type {

    public String typeName;

    /**
     * This constructor is used by SnakeYAML.
     */
    public Type(String scalar) {
        typeName = scalar;
    }
}

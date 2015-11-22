package io.breen.socrates.file.python;


import java.util.Map;

public class Object {

    /**
     * The fields of this object (produced by SnakeYAML when it parses this
     */
    public Map<String, java.lang.Object> fields;

    /**
     * The type the value should be converted to before being used by in the context of running
     * actual Java code.
     */
    public Type type;

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public Object() {}
}

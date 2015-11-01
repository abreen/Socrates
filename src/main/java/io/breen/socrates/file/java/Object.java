package io.breen.socrates.file.java;

/**
 * Instances of this class represent an object that should be passed into a Java method. They are
 * created when the criteria file is read in. Each object has an associated Type, and will be
 * converted to an actual Java object of the "real" type before a Java method is actually called.
 */
public class Object {

    /**
     * The value of this object, produced by SnakeYAML.
     */
    public java.lang.Object value;

    /**
     * The type the value should be converted to before being used by in the context of running
     * actual Java code.
     */
    public Type type;

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public Object() {}

    /**
     * Returns a java.lang.Object accurately representing the value of this Object. For example, if
     * the io.breen.socrates.file.java.Type of this object is a string, and the value herein is an
     * integer, a new java.lang.String object is created & returned by this method.
     */
    public java.lang.Object toJavaObject() {
        String valueClassName = value.getClass().getSimpleName();
        if (valueClassName.equals(type.typeName)) {
            // no conversion necessary
            return value;
        }

        if (type.typeName.equals("Character")) {
            if (value instanceof String) {
                String s = (String)value;

                if (s.length() != 1)
                    throw new RuntimeException("character string has more than one character");

                return s.charAt(0);
            }
        }

        throw new RuntimeException("could not convert to Java object");
    }
}

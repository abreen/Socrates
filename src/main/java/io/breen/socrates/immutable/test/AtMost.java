package io.breen.socrates.immutable.test;

public class AtMost<T> extends Ceiling<T> {
    private T value;

    public AtMost(T value) {
        if (value == null)
            throw new IllegalArgumentException("an AtMost cannot be null");

        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public static <T> T getValue(Ceiling<T> ceiling) {
        return ((AtMost<T>)ceiling).getValue();
    }

    public String toString() {
        return "AtMost(" + value + ")";
    }
}

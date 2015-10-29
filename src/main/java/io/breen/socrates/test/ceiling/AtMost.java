package io.breen.socrates.test.ceiling;

public class AtMost<T> extends Ceiling<T> {

    private T value;

    public AtMost(T value) {
        if (value == null) throw new IllegalArgumentException("an AtMost cannot be null");

        this.value = value;
    }

    public static <T> T getValue(Ceiling<T> ceiling) {
        return ((AtMost<T>)ceiling).getValue();
    }

    public T getValue() {
        return value;
    }

    public String toString() {
        return "AtMost(" + value + ")";
    }
}

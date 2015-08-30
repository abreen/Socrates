package io.breen.socrates.immutable.test.ceiling;

public abstract class Ceiling<T> {

    public static final Ceiling ANY;

    static {
        ANY = new Any();
    }

    @SuppressWarnings("unchecked")
    public static <T> Ceiling<T> getAny() {
        return (Ceiling<T>)ANY;
    }

    private static class Any extends Ceiling {

        @Override
        public String toString() {
            return "Any";
        }
    }
}

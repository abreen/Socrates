package io.breen.socrates.util;

/**
 * A simple class representing a product type (a pair of two values).
 *
 * @param <A> The type of the first value in the pair
 * @param <B> The type of the second value in the pair
 */
public class Pair<A, B> {

    public A first;
    public B second;

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}

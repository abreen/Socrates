package io.breen.socrates.util;

/**
 * A simple interface describing the observer side of the observer pattern
 * implementation in this package.
 *
 * @see io.breen.socrates.util.Observable
 * @param <T> The type of the data that is being observed
 */
public interface Observer<T> {
    void objectChanged(T object);
}

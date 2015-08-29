package io.breen.socrates.util;

/**
 * A simple interface describing the observer side of the single-observer pattern
 * in conjunction with the Observable interface.
 *
 * @see io.breen.socrates.util.Observable
 * @param <T> The type of the data that is being observed
 */
public interface Observer<T> {
    void objectChanged(T object);
}

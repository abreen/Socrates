package io.breen.socrates.util;

/**
 * A simple interface describing the observer side of the observer pattern implementation in this
 * package.
 *
 * @param <T> The type of the data that is being observed
 *
 * @see io.breen.socrates.util.Observable
 */
public interface Observer<T> {

    void objectChanged(ObservableChangedEvent<T> event);
}

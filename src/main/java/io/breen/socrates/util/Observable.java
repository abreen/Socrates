package io.breen.socrates.util;

/**
 * A simple interface specifying that the implementer can support a single observer that
 * can be notified when the implementer's state changes.
 *
 * @param <T> The type of the data that can be observed
 */
public interface Observable<T> {
    void setObserver(Observer<T> observer);
    void resetObserver();
}

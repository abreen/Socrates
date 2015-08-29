package io.breen.socrates.util;

/**
 * A simple interface specifying that the implementer can maintain an ordered list of observers of
 * any size that are notified when the implementer's state changes. Observers are notified of
 * changes in the order they appear in the implementer's observers list, which is determined by the
 * order in which they call addObserver() to add themselves.
 *
 * @param <T> The type of the data that can be observed
 */
public interface Observable<T> {

    void addObserver(Observer<T> observer);

    void removeObserver(Observer<T> observer);
}

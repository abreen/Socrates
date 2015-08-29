package io.breen.socrates.util;

/**
 * Classes extending this class represent object change events. They are created by an Observable
 * object when some part of the object's state changes, and they should specify how the object's
 * state changes.
 */
public abstract class ObservableChangedEvent<T> {

    public final T source;

    public ObservableChangedEvent(T source) {
        this.source = source;
    }
}
